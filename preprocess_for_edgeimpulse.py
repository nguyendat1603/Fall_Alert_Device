import os
import glob
import re
import csv
import numpy as np

# Đường dẫn thư mục chứa dataset
DATASET_DIR = r"D:\Fall_Alert\Fall_Alert_Sys\Dataset\MobiAct_Dataset_v2.0-MobiFall_Dataset_v2.0-main\MobiAct_Dataset_v2.0-MobiFall_Dataset_v2.0-main"
OUTPUT_DIR = r"D:\Fall_Alert\Fall_Alert_Sys\dataset_edge_impulse"

TARGET_FREQ_HZ = 50  # 50Hz = 20ms mỗi mẫu (chuẩn MPU6050 & ESP32-S3)
DT_NS = int(1e9 / TARGET_FREQ_HZ)

FALL_CODES = {'FOL', 'FKL', 'BSC', 'SDL'}
ADL_CODES = {'STD', 'WAL', 'JOG', 'JUM', 'STU', 'STN', 'SCH', 'CSI', 'CSO'}

def read_sensor_txt(filepath):
    """Đọc dữ liệu bỏ qua các dòng comment (#) và header"""
    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        lines = [line.strip() for line in f if not line.startswith('#') and not line.startswith('@') and line.strip()]
    data = []
    for line in lines:
        parts = [float(x.strip()) for x in line.split(',') if x.strip()]
        if len(parts) >= 4:
            data.append(parts[:4])
    return np.array(data)

def process_dataset():
    fall_dir = os.path.join(OUTPUT_DIR, "fall")
    adl_dir = os.path.join(OUTPUT_DIR, "adl")
    os.makedirs(fall_dir, exist_ok=True)
    os.makedirs(adl_dir, exist_ok=True)
    
    acc_files = glob.glob(os.path.join(DATASET_DIR, "**", "*_acc_*.txt"), recursive=True)
    print(f"Tìm thấy tổng cộng {len(acc_files)} file gia tốc.")
    
    count_saved = 0
    for acc_path in acc_files:
        filename = os.path.basename(acc_path)
        # Tên file mẫu: BSC_acc_1_1.txt
        match = re.match(r"([A-Z]+)_acc_(\d+)_(\d+)\.txt", filename)
        if not match:
            continue
        
        act, sub, trial = match.groups()
        gyro_filename = f"{act}_gyro_{sub}_{trial}.txt"
        gyro_path = os.path.join(os.path.dirname(acc_path), gyro_filename)
        
        if not os.path.exists(gyro_path):
            candidates = glob.glob(os.path.join(DATASET_DIR, "**", gyro_filename), recursive=True)
            if candidates:
                gyro_path = candidates[0]
            else:
                continue
        
        acc_data = read_sensor_txt(acc_path)
        gyro_data = read_sensor_txt(gyro_path)
        
        if len(acc_data) < 10 or len(gyro_data) < 10:
            continue
            
        t_start = max(acc_data[0, 0], gyro_data[0, 0])
        t_end = min(acc_data[-1, 0], gyro_data[-1, 0])
        if t_start >= t_end:
            continue
            
        t_uniform = np.arange(t_start, t_end, DT_NS)
        if len(t_uniform) < 10:
            continue
            
        # Nội suy đồng bộ tần số 50Hz
        ax_interp = np.interp(t_uniform, acc_data[:, 0], acc_data[:, 1])
        ay_interp = np.interp(t_uniform, acc_data[:, 0], acc_data[:, 2])
        az_interp = np.interp(t_uniform, acc_data[:, 0], acc_data[:, 3])
        
        gx_interp = np.interp(t_uniform, gyro_data[:, 0], gyro_data[:, 1])
        gy_interp = np.interp(t_uniform, gyro_data[:, 0], gyro_data[:, 2])
        gz_interp = np.interp(t_uniform, gyro_data[:, 0], gyro_data[:, 3])
        
        t_ms = ((t_uniform - t_uniform[0]) / 1e6).astype(int)
        
        label = "fall" if act in FALL_CODES else "adl"
        target_folder = fall_dir if label == "fall" else adl_dir
        out_filename = f"{label}.{act}_sub{sub}_trial{trial}.csv"
        out_filepath = os.path.join(target_folder, out_filename)
        
        with open(out_filepath, 'w', newline='', encoding='utf-8') as out_f:
            writer = csv.writer(out_f)
            writer.writerow(["timestamp", "accX", "accY", "accZ", "gyroX", "gyroY", "gyroZ"])
            for i in range(len(t_ms)):
                writer.writerow([
                    t_ms[i],
                    f"{ax_interp[i]:.4f}", f"{ay_interp[i]:.4f}", f"{az_interp[i]:.4f}",
                    f"{gx_interp[i]:.4f}", f"{gy_interp[i]:.4f}", f"{gz_interp[i]:.4f}"
                ])
        count_saved += 1

    print(f"Xử lý hoàn tất! Đã xuất {count_saved} file CSV vào thư mục: {OUTPUT_DIR}")

if __name__ == "__main__":
    process_dataset()
