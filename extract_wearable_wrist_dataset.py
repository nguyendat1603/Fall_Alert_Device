import os
import csv
import io
import math
import struct
import zipfile
import numpy as np

# Đường dẫn file zip gốc trong bộ dataset Synthetic Multirate Time-Series Dataset for Health
ZIP_PATH = r"D:\Fall_Alert\Fall_Alert_Sys\Dataset\Synthetic Multirate Time-Series Dataset for Health\Synthetic Multirate Time-Series Dataset for Health\Elderly_Release_3_2_Mendeley_Repository.zip"

# Thư mục đích theo yêu cầu: wearable_dataset
OUTPUT_DIR = r"D:\Fall_Alert\Fall_Alert_Sys\wearable_dataset"

# Tần số lấy mẫu đầu ra cho Edge Impulse & ESP32 MPU6050 (50Hz = 20ms mỗi mẫu, đồng bộ với code ESP32)
TARGET_FREQ_HZ = 50
G_TO_MS2 = 9.80665
DPS_TO_RADS = math.pi / 180.0


# ==============================================================================
# CÁC HÀM GIẢI MÃ PARQUET (THRIFT + SNAPPY + RLE/DICTIONARY) THUẦN PYTHON + NUMPY
# ==============================================================================
def _read_varint(buf, pos):
    res = 0
    shift = 0
    while True:
        b = buf[pos]
        pos += 1
        res |= (b & 0x7F) << shift
        if not (b & 0x80):
            return res, pos
        shift += 7


def _read_zigzag(buf, pos):
    n, pos = _read_varint(buf, pos)
    return (n >> 1) ^ -(n & 1), pos


def _parse_thrift_struct(buf, pos):
    fields = {}
    last_id = 0
    while pos < len(buf):
        b = buf[pos]
        pos += 1
        if b == 0:
            break
        delta = (b & 0xF0) >> 4
        t = b & 0x0F
        if delta == 0:
            field_id, pos = _read_zigzag(buf, pos)
        else:
            field_id = last_id + delta
        last_id = field_id

        if t in (1, 2):
            fields[field_id] = (t == 1)
        elif t == 3:
            fields[field_id] = buf[pos]
            pos += 1
        elif t in (4, 5, 6):
            fields[field_id], pos = _read_zigzag(buf, pos)
        elif t == 7:
            fields[field_id] = struct.unpack("<d", buf[pos : pos + 8])[0]
            pos += 8
        elif t == 8:
            ln, pos = _read_varint(buf, pos)
            fields[field_id] = buf[pos : pos + ln]
            pos += ln
        elif t == 12:
            fields[field_id], pos = _parse_thrift_struct(buf, pos)
        else:
            raise ValueError(f"Unsupported Thrift type: {t}")
    return fields, pos


def _snappy_decompress(data):
    uncomp_len, pos = _read_varint(data, 0)
    out = bytearray()
    n = len(data)
    while pos < n:
        tag = data[pos]
        pos += 1
        elem_type = tag & 0x03
        if elem_type == 0:  # Literal
            length = (tag >> 2) + 1
            if length > 60:
                eb = length - 60
                length = int.from_bytes(data[pos : pos + eb], "little") + 1
                pos += eb
            out.extend(data[pos : pos + length])
            pos += length
        else:  # Copy
            if elem_type == 1:
                length = ((tag >> 2) & 0x07) + 4
                offset = ((tag & 0xE0) << 3) | data[pos]
                pos += 1
            elif elem_type == 2:
                length = (tag >> 2) + 1
                offset = data[pos] | (data[pos + 1] << 8)
                pos += 2
            else:
                length = (tag >> 2) + 1
                offset = int.from_bytes(data[pos : pos + 4], "little")
                pos += 4

            if offset >= length:
                start = len(out) - offset
                out.extend(out[start : start + length])
            else:
                for _ in range(length):
                    out.append(out[-offset])
    return bytes(out)


def _decode_rle_bitpacked(buf, pos, bit_width, num_values, has_length_prefix=True):
    if has_length_prefix:
        rle_len = struct.unpack("<I", buf[pos : pos + 4])[0]
        pos += 4
        end_pos = pos + rle_len
    else:
        end_pos = len(buf)

    out = []
    byte_width = (bit_width + 7) // 8
    mask = (1 << bit_width) - 1
    while len(out) < num_values and pos < end_pos:
        header, pos = _read_varint(buf, pos)
        if header & 1:  # Bit-packed run
            num_groups = header >> 1
            count = num_groups * 8
            total_bytes = num_groups * bit_width
            chunk = int.from_bytes(buf[pos : pos + total_bytes], "little")
            pos += total_bytes
            for i in range(count):
                out.append((chunk >> (i * bit_width)) & mask)
        else:  # RLE run
            count = header >> 1
            val = int.from_bytes(buf[pos : pos + byte_width], "little") if byte_width > 0 else 0
            pos += byte_width
            out.extend([val] * count)
    return np.array(out[:num_values], dtype=np.int32), pos


def read_parquet_float_column(parquet_bytes, footer_bytes, col_name):
    """Đọc cột số thực (float64) từ file Parquet nén Snappy."""
    col_bytes = col_name.encode("utf-8")
    idx1 = footer_bytes.find(col_bytes)
    idx2 = footer_bytes.find(col_bytes, idx1 + 1)
    pos = idx2 + len(col_bytes)

    codec, pos = _read_zigzag(footer_bytes, pos + 1)
    num_vals, pos = _read_zigzag(footer_bytes, pos + 1)
    uncomp_sz, pos = _read_zigzag(footer_bytes, pos + 1)
    comp_sz, pos = _read_zigzag(footer_bytes, pos + 1)
    data_off, pos = _read_zigzag(footer_bytes, pos + 1)
    dict_off, pos = _read_zigzag(footer_bytes, pos + 1)

    pos = dict_off
    end = dict_off + comp_sz
    dict_vals = None
    col_out = []

    while pos < end:
        hdr, next_pos = _parse_thrift_struct(parquet_bytes, pos)
        page_type = hdr[1]
        comp_page_sz = hdr[3]
        raw_page = _snappy_decompress(parquet_bytes[next_pos : next_pos + comp_page_sz])
        pos = next_pos + comp_page_sz

        if page_type == 2:  # DICTIONARY_PAGE
            dict_vals = np.frombuffer(raw_page, dtype="<f8")
        elif page_type == 0:  # DATA_PAGE
            dp_hdr = hdr[5]
            n_val = dp_hdr[1]
            enc = dp_hdr[2]
            def_levels, p = _decode_rle_bitpacked(raw_page, 0, 1, n_val, has_length_prefix=True)
            n_non_null = int(np.sum(def_levels == 1))
            if enc in (8, 3):  # RLE_DICTIONARY
                bw = raw_page[p]
                p += 1
                indices, p = _decode_rle_bitpacked(raw_page, p, bw, n_non_null, has_length_prefix=False)
                non_null_vals = dict_vals[indices]
            elif enc == 0:  # PLAIN
                non_null_vals = np.frombuffer(raw_page, dtype="<f8", count=n_non_null, offset=p)
            else:
                raise ValueError(f"Unsupported page encoding: {enc}")

            page_arr = np.full(n_val, np.nan, dtype=np.float64)
            page_arr[def_levels == 1] = non_null_vals
            col_out.append(page_arr)

    return np.concatenate(col_out)


def fill_nan_1d(arr):
    """Nội suy tuyến tính các điểm bị khuyết (NaN do chế độ dropout của cảm biến)."""
    arr = np.asarray(arr, dtype=np.float64)
    mask = np.isnan(arr)
    if not np.any(mask):
        return arr
    if np.all(mask):
        return np.zeros_like(arr)
    valid_idx = np.flatnonzero(~mask)
    missing_idx = np.flatnonzero(mask)
    arr_clean = arr.copy()
    arr_clean[mask] = np.interp(missing_idx, valid_idx, arr[valid_idx])
    return arr_clean


def extract_wrist_dataset():
    fall_dir = os.path.join(OUTPUT_DIR, "fall")
    adl_dir = os.path.join(OUTPUT_DIR, "adl")
    os.makedirs(fall_dir, exist_ok=True)
    os.makedirs(adl_dir, exist_ok=True)

    print(f"Đang đọc dữ liệu từ: {ZIP_PATH}")
    with zipfile.ZipFile(ZIP_PATH, "r") as z:
        with z.open("synthetic_event_metadata.csv") as f:
            events_meta = list(csv.DictReader(io.TextIOWrapper(f, encoding="utf-8")))

        with z.open("event_features.csv") as f:
            event_feats = list(csv.DictReader(io.TextIOWrapper(f, encoding="utf-8")))

        with z.open("synthetic_elderly_monitoring_timeseries.parquet") as f:
            parquet_bytes = f.read()

    footer_len = struct.unpack("<I", parquet_bytes[-8:-4])[0]
    footer_bytes = parquet_bytes[-8 - footer_len : -8]

    sensor_cols = [
        "acc_x_g",
        "acc_y_g",
        "acc_z_g",
        "gyro_x_dps",
        "gyro_y_dps",
        "gyro_z_dps",
        "heart_rate_bpm",
        "spo2_pct",
        "temperature_c",
    ]
    decoded_cols = {}
    for col in sensor_cols:
        print(f"  -> Đang giải mã cột Parquet: {col} ...")
        decoded_cols[col] = read_parquet_float_column(parquet_bytes, footer_bytes, col)

    # Xuất thêm bảng CSV tổng hợp toàn bộ dữ liệu đeo cổ tay (native 100Hz) và metadata cổ tay
    raw_wrist_csv_path = os.path.join(OUTPUT_DIR, "wrist_all_timeseries_100Hz.csv")
    meta_wrist_csv_path = os.path.join(OUTPUT_DIR, "wrist_event_metadata.csv")

    count_fall = 0
    count_adl = 0
    row_offset = 0
    max_verify_err = 0.0

    with open(raw_wrist_csv_path, "w", newline="", encoding="utf-8") as raw_f, \
         open(meta_wrist_csv_path, "w", newline="", encoding="utf-8") as meta_f:

        raw_writer = csv.writer(raw_f)
        raw_writer.writerow([
            "subject_id", "event_id", "split", "sensor_profile", "placement",
            "activity_type", "fall_detected", "sensor_degradation",
            "sample_index", "time_from_event_start_s",
            "acc_x_g", "acc_y_g", "acc_z_g",
            "gyro_x_dps", "gyro_y_dps", "gyro_z_dps",
            "heart_rate_bpm", "spo2_pct", "temperature_c"
        ])

        meta_writer = csv.DictWriter(meta_f, fieldnames=events_meta[0].keys())
        meta_writer.writeheader()

        for idx, meta in enumerate(events_meta):
            n_samples = int(meta["n_samples"])
            start = row_offset
            end = row_offset + n_samples
            row_offset = end

            feat_row = event_feats[idx]
            placement = feat_row.get("placement", "")

            # CHỈ LẤY DỮ LIỆU ĐEO Ở CỔ TAY (placement == 'wrist', tức UPFall_Wrist_100Hz)
            if placement != "wrist":
                continue

            meta_writer.writerow(meta)

            # Kiểm tra đối chiếu độ chính xác với event_features.csv
            ax_raw = decoded_cols["acc_x_g"][start:end]
            ay_raw = decoded_cols["acc_y_g"][start:end]
            az_raw = decoded_cols["acc_z_g"][start:end]
            gx_raw = decoded_cols["gyro_x_dps"][start:end]
            gy_raw = decoded_cols["gyro_y_dps"][start:end]
            gz_raw = decoded_cols["gyro_z_dps"][start:end]
            hr_raw = decoded_cols["heart_rate_bpm"][start:end]
            spo2_raw = decoded_cols["spo2_pct"][start:end]
            temp_raw = decoded_cols["temperature_c"][start:end]

            err = abs(float(np.nanmean(ax_raw)) - float(feat_row["acc_x_g_mean"]))
            if err > max_verify_err:
                max_verify_err = err

            # Ghi vào file tổng hợp wrist_all_timeseries_100Hz.csv
            sampling_hz = int(meta["sampling_hz"])
            for s_i in range(n_samples):
                t_sec = s_i / float(sampling_hz)
                raw_writer.writerow([
                    meta["subject_id"], meta["event_id"], meta["split"],
                    meta["sensor_profile"], placement,
                    meta["activity_type"], meta["fall_detected"], meta["sensor_degradation"],
                    s_i, f"{t_sec:.4f}",
                    "" if np.isnan(ax_raw[s_i]) else f"{ax_raw[s_i]:.6f}",
                    "" if np.isnan(ay_raw[s_i]) else f"{ay_raw[s_i]:.6f}",
                    "" if np.isnan(az_raw[s_i]) else f"{az_raw[s_i]:.6f}",
                    "" if np.isnan(gx_raw[s_i]) else f"{gx_raw[s_i]:.6f}",
                    "" if np.isnan(gy_raw[s_i]) else f"{gy_raw[s_i]:.6f}",
                    "" if np.isnan(gz_raw[s_i]) else f"{gz_raw[s_i]:.6f}",
                    f"{hr_raw[s_i]:.2f}", f"{spo2_raw[s_i]:.2f}", f"{temp_raw[s_i]:.2f}"
                ])

            # Nội suy các điểm NaN (do sensor dropout) trước khi xuất sang CSV cho Edge Impulse
            ax_clean = fill_nan_1d(ax_raw) * G_TO_MS2
            ay_clean = fill_nan_1d(ay_raw) * G_TO_MS2
            az_clean = fill_nan_1d(az_raw) * G_TO_MS2
            gx_clean = fill_nan_1d(gx_raw) * DPS_TO_RADS
            gy_clean = fill_nan_1d(gy_raw) * DPS_TO_RADS
            gz_clean = fill_nan_1d(gz_raw) * DPS_TO_RADS

            # Nội suy đồng bộ sang tần số TARGET_FREQ_HZ (50Hz -> mỗi mẫu cách nhau 20ms)
            duration_s = n_samples / float(sampling_hz)
            t_orig_ms = np.arange(n_samples, dtype=np.float64) * (1000.0 / sampling_hz)
            dt_ms = int(1000 / TARGET_FREQ_HZ)
            t_target_ms = np.arange(0, int(duration_s * 1000), dt_ms, dtype=np.int32)

            ax_out = np.interp(t_target_ms, t_orig_ms, ax_clean)
            ay_out = np.interp(t_target_ms, t_orig_ms, ay_clean)
            az_out = np.interp(t_target_ms, t_orig_ms, az_clean)
            gx_out = np.interp(t_target_ms, t_orig_ms, gx_clean)
            gy_out = np.interp(t_target_ms, t_orig_ms, gy_clean)
            gz_out = np.interp(t_target_ms, t_orig_ms, gz_clean)

            is_fall = int(meta["fall_detected"]) == 1
            label = "fall" if is_fall else "adl"
            target_subfolder = fall_dir if is_fall else adl_dir

            act_name = meta["activity_type"]
            event_id = meta["event_id"]
            out_filename = f"{label}.{act_name}_{event_id}.csv"
            out_filepath = os.path.join(target_subfolder, out_filename)

            with open(out_filepath, "w", newline="", encoding="utf-8") as ef:
                ew = csv.writer(ef)
                ew.writerow(["timestamp", "accX", "accY", "accZ", "gyroX", "gyroY", "gyroZ"])
                for i in range(len(t_target_ms)):
                    ew.writerow([
                        int(t_target_ms[i]),
                        f"{ax_out[i]:.4f}", f"{ay_out[i]:.4f}", f"{az_out[i]:.4f}",
                        f"{gx_out[i]:.4f}", f"{gy_out[i]:.4f}", f"{gz_out[i]:.4f}",
                    ])

            if is_fall:
                count_fall += 1
            else:
                count_adl += 1

    print("=" * 75)
    print(f"HOÀN TẤT TRÍCH XUẤT DỮ LIỆU ĐEO CỔ TAY (placement == 'wrist')!")
    print(f"  - Sai số kiểm chứng tối đa so với event_features.csv: {max_verify_err:.2e}")
    print(f"  - Tổng số sự kiện đeo cổ tay: {count_fall + count_adl} files")
    print(f"      + Thư mục fall ({fall_dir}): {count_fall} files CSV")
    print(f"      + Thư mục adl  ({adl_dir}):  {count_adl} files CSV")
    print(f"  - File CSV tổng hợp gốc (100Hz): {raw_wrist_csv_path}")
    print(f"  - File CSV metadata cổ tay:      {meta_wrist_csv_path}")
    print("=" * 75)


if __name__ == "__main__":
    extract_wrist_dataset()
