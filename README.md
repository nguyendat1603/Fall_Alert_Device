# ⌚ FallGuard C3 — Hệ Thống IoT Đeo Tay Phát Hiện Té Ngã (TinyML) & Giám Sát Sức Khỏe Thời Gian Thực

> **Dự án phát triển bởi:** Nguyễn Đạt (`nguyendat1603`)  
> **Nền tảng phần cứng chính thức:** `ESP32-C3 SuperMini` (RISC-V) + `MPU6050` (6-DOF IMU) + `MAX30102` (PPG Nhịp tim & SpO2) + `Active-Low Buzzer`  
> **Nền tảng di động chính thức:** `Android Native` (`Kotlin` + `Jetpack Compose` — Giao diện **Glacier Crisp Ice Theme**)  
> **Nền tảng AI nhúng:** `Edge Impulse TinyML` (Quantized INT8 Neural Network chạy trực tiếp trên vi điều khiển)

---

## 📌 1. Giới Thiệu Tổng Quan

**FallGuard C3** là hệ thống thiết bị đeo cổ tay thông minh kết hợp ứng dụng di động Android nhằm bảo vệ người cao tuổi và người có nguy cơ té ngã cao. Khác với các hệ thống báo ngã truyền thống chỉ dùng ngưỡng gia tốc đơn thuần (dễ báo động giả khi vung tay, vỗ tay hoặc ngồi phịch xuống ghế), **FallGuard C3** áp dụng **Thuật toán Kiểm chứng Xác thực 2 Lớp (Dual-Layer Verification)**:

1. **Lớp 1 — Nhận diện động học bằng AI TinyML (`Edge Impulse`):** Phân tích cửa sổ trượt 2 giây (100 mẫu × 6 trục gia tốc & con quay hồi chuyển ở tần số 50Hz) được huấn luyện chuyên biệt trên **dữ liệu đeo ở cổ tay (`placement == "wrist"`)** để phân biệt 5 kiểu ngã thực tế với các cử chỉ sinh hoạt hàng ngày (ADL).
2. **Lớp 2 — Kiểm chứng xung lực va chạm vật lý (`SMV Impact Threshold`):** Đo tổng hợp lực gia tốc $SMV = \sqrt{a_x^2 + a_y^2 + a_z^2}$ để xác nhận có va đập tiếp đất thực sự ($SMV > 16.0\text{ m/s}^2 \approx 1.63g$).
3. **Theo dõi chỉ số sinh tồn ngay lúc ngã (`MAX30102`):** Thu thập liên tục **Nhịp tim (BPM)** và **Nồng độ Oxy trong máu (SpO2 %)** trên cùng bus I2C tốc độ cao 400kHz để đánh giá mức độ nguy kịch (ví dụ: té ngã kèm tụt nhịp tim/SpO2 do đột quỵ hoặc choáng ngất).

---

## 📂 2. Cấu Trúc Thư Mục Dự Án (Thư Mục Chính Thức vs. Thư Mục Không Liên Quan)

Để thuận tiện cho việc chấm điểm, bảo trì và phát triển, toàn bộ kho mã nguồn được phân chia rõ ràng như sau:

```text
D:\Fall_Alert\Fall_Alert_Sys\
│
├── 🟢 [CÁC THƯ MỤC & FILE DỰ ÁN CHÍNH THỨC ĐANG SỬ DỤNG]
│   ├── Fall_Alert_Project_ESP32_C3_SUPERMINI/   # (1) FIRMWARE CHÍNH THỨC cho mạch đeo tay ESP32-C3 SuperMini
│   │   └── Fall_Alert_Sys_Esp32_C3_Supermini/
│   │       ├── platformio.ini                   # Cấu hình PlatformIO cho ESP32-C3 (USB CDC, thư viện cảm biến)
│   │       ├── src/main.cpp                     # Code C++ chạy AI TinyML + Lọc SMV + Đọc MAX30102 + Buzzer
│   │       └── lib/wearable_fall_inferencing/   # Thư viện mô hình AI Edge Impulse (C++ INT8) cho cổ tay
│   │
│   ├── FallAlertApp/                            # (2) ỨNG DỤNG ANDROID CHÍNH THỨC (Kotlin + Jetpack Compose)
│   │   └── app/src/main/kotlin/com/iot/fallalert/
│   │       ├── MainActivity.kt                  # Điểm khởi chạy ứng dụng Android FallGuard C3
│   │       └── ui/
│   │           ├── theme/GlacierPalette.kt      # Bảng màu Glacier Crisp Ice & màu trạng thái y tế
│   │           └── main/
│   │               ├── FallGuardModels.kt       # Data Models cho cảm biến, trạng thái AI và lịch sử sự cố
│   │               ├── FallGuardComponents.kt   # Header, Simulator Dock, Bottom Nav, Toast & nút TactileBox
│   │               ├── HomeTabScreen.kt         # Tab 1: Trang chủ (Hero 15s SOS, Bento 2x2, Dual-Layer, Chart)
│   │               ├── HistoryTabScreen.kt      # Tab 2: Lịch sử sự cố, thống kê KPI & bộ lọc danh mục
│   │               ├── SettingsTabScreen.kt     # Tab 3: Cài đặt ngưỡng SMV, danh bạ SOS 115 & test còi Buzzer
│   │               ├── MainScreenViewModel.kt   # ViewModel quản lý luồng dữ liệu, đếm ngược & âm báo động
│   │               └── MainScreen.kt            # Màn hình chính kết nối toàn bộ 3 Tab
│   │
│   ├── wearable_dataset/                        # (3) BỘ DATASET ĐEO CỔ TAY CHÍNH THỨC (Dùng để train AI)
│   │   ├── fall/                                # 23 file CSV kịch bản té ngã cổ tay (50Hz, m/s² & rad/s)
│   │   ├── adl/                                 # 129 file CSV sinh hoạt & cử chỉ giả ngã cổ tay (50Hz)
│   │   ├── wrist_all_timeseries_100Hz.csv       # Toàn bộ 60.800 mẫu dữ liệu cổ tay gốc 100Hz (IMU + HR + SpO2)
│   │   └── wrist_event_metadata.csv             # Bảng metadata chi tiết của 152 sự kiện đeo cổ tay
│   │
│   ├── extract_wearable_wrist_dataset.py        # Script Python trích xuất dữ liệu cổ tay từ Parquet sang CSV
│   ├── design/                                  # (4) BẢN THIẾT KẾ GIAO DIỆN GỐC (Stitch Glacier Theme HTML)
│   │   └── fallguard_c3_glacier_design.html
│   ├── MVP/                                     # (5) TÀI LIỆU ĐẶC TẢ CHỨC NĂNG MVP & BẢN DEMO WEB
│   │   ├── MVP_App_Functions.txt                # Đặc tả chi tiết các nhóm chức năng cốt lõi & nâng cao
│   │   ├── Stitch_Frontend_Prompt.md            # Prompt thiết kế giao diện chuẩn Design System
│   │   └── mobile_app_demo.html                 # Bản mô phỏng tương tác nhanh trên trình duyệt máy tính
│   └── .agents/skills/kotlin-android-iot-health/# Skill hướng dẫn kiến trúc Kotlin Android IoT cho Agent
│
└── ⚪ [folder không liên quan/]                 # (6) THƯ MỤC LƯU TRỮ CÁC BẢN THỬ NGHIỆM CŨ / KHÔNG DÙNG NỮA
    ├── Fall_Alert_Project_ESP32_DevKitV1_WROOM-32/  # Bản code cũ thử nghiệm trên board ESP32 WROOM-32 lớn
    ├── dataset_edge_impulse/                        # Bộ dataset MobiAct bỏ túi quần cũ (không dùng cho cổ tay)
    ├── libs_for_training/                           # Thư viện model thử nghiệm đời đầu
    ├── Dataset/                                     # Kho chứa file nén dataset thô ban đầu
    └── preprocess_for_edgeimpulse.py                # Script xử lý dataset bỏ túi quần cũ
```

> **⚠️ Lưu ý quan trọng:** Khi chấm điểm, biên dịch hoặc nạp code, **chỉ sử dụng các thư mục chính thức ở ngoài gốc** (`Fall_Alert_Project_ESP32_C3_SUPERMINI`, `FallAlertApp`, `wearable_dataset`, `design`, `MVP`). Toàn bộ các thư mục thử nghiệm cũ đã được gom gọn vào thư mục **`folder không liên quan/`**.

---

## 🔌 3. Thiết Kế Phần Cứng & Sơ Đồ Đấu Nối (`ESP32-C3 SuperMini`)

Hệ thống phần cứng đeo tay sử dụng vi điều khiển **ESP32-C3 SuperMini** siêu nhỏ gọn. Hai cảm biến **MPU6050** (địa chỉ I2C `0x68`) và **MAX30102** (địa chỉ I2C `0x57`) được mắc song song trên cùng một bus I2C tốc độ cao (`400kHz`):

| Linh kiện / Module | Chân trên Module | Chân trên ESP32-C3 SuperMini | Chức năng & Ghi chú kỹ thuật |
| :--- | :---: | :---: | :--- |
| **Cảm biến MPU6050** | `VCC` | `3V3` | Cấp nguồn 3.3V ổn định |
| *(Gia tốc & Con quay 6 trục)* | `GND` | `GND` | Nối Mass chung |
| | `SDA` | **`GPIO 8`** | Đường dữ liệu I2C chung (Địa chỉ `0x68`) |
| | `SCL` | **`GPIO 9`** | Đường xung nhịp I2C chung (400kHz Fast Mode) |
| **Cảm biến MAX30102** | `VIN` | `3V3` | Cấp nguồn 3.3V bảo vệ bus logic I2C |
| *(Nhịp tim PPG & SpO2)* | `GND` | `GND` | Nối Mass chung |
| | `SDA` | **`GPIO 8`** | Đấu song song chân SDA với MPU6050 (Địa chỉ `0x57`) |
| | `SCL` | **`GPIO 9`** | Đấu song song chân SCL với MPU6050 |
| **Còi báo động Buzzer** | `VCC` | `3V3` (hoặc `5V`) | Nguồn nuôi mạch còi |
| *(Module 3 chân PNP)* | `GND` | `GND` | Nối Mass chung |
| | `I/O` (SIG) | **`GPIO 3`** | Kích mức THẤP (`LOW` = Kêu, `INPUT High-Z` = Ngắt hẳn) |

---

## 🧠 4. Thuật Toán Nhận Diện Té Ngã 2 Lớp & Tiền Xử Lý Dữ Liệu Cổ Tay

### 4.1. Tại sao phải dùng bộ dữ liệu đeo cổ tay riêng (`wearable_dataset`)?
* Ở các bộ dữ liệu để điện thoại trong **túi quần** (như MobiAct), khi đứng thẳng trọng lực dồn vào trục Y ($a_y \approx 9.8\text{ m/s}^2$), còn khi đặt mạch nằm ngang trên bàn hoặc đeo tay ($a_z \approx 9.8\text{ m/s}^2$) mô hình túi quần sẽ hiểu nhầm là người đang nằm bất động dưới sàn và báo ngã giả liên tục.
* Do đó, dự án sử dụng script [extract_wearable_wrist_dataset.py](file:///d:/Fall_Alert/Fall_Alert_Sys/extract_wearable_wrist_dataset.py) để trích xuất riêng **152 sự kiện đeo ở cổ tay (`placement == "wrist"`, cấu hình `UPFall_Wrist_100Hz`)**:
  * **23 kịch bản té ngã (`wearable_dataset/fall`):** Ngã ngửa (`backward_fall`), ngã chúi về trước (`forward_fall`), ngã nghiêng (`lateral_fall`), choáng ngất tụt huyết áp (`syncope_collapse`), trượt khỏi ghế (`chair_slip_fall`).
  * **129 kịch bản sinh hoạt & giả ngã (`wearable_dataset/adl`):** Đi bộ bình thường (`normal_walking`), đi bộ nhanh (`brisk_walking`), đứng lên ngồi xuống (`sit_to_stand`), ngồi phịch mạnh xuống ghế (`abrupt_sit`), cúi với đồ (`reach_and_bend`), leo cầu thang (`stairs_up_down`), vấp chân nhưng tự giữ thăng bằng (`stumble_recovery`), trở mình trên giường (`bed_turn_rest`).
  * **Đồng bộ đơn vị vật lý với `Adafruit_MPU6050`:** Gia tốc quy đổi từ $g$ sang **$m/s^2$** ($\times 9.80665$), vận tốc góc từ $\text{deg/s}$ sang **$\text{rad/s}$** ($\times \pi / 180$), nội suy tuyến tính khử toàn bộ điểm khuyết `NaN` và chuẩn hóa về tần số **50Hz** (`20ms`/mẫu).

### 4.2. Công thức Kiểm chứng Xác thực 2 Lớp trên ESP32-C3 (`src/main.cpp`)
Trong mỗi cửa sổ 2 giây, vi điều khiển tính biên độ vector gia tốc tổng hợp:

$$SMV = \sqrt{a_x^2 + a_y^2 + a_z^2}$$

Báo động té ngã chỉ được kích hoạt khi **cả 2 điều kiện** cùng thỏa mãn:

$$\text{IsRealFall} = \left( P_{\text{TinyML}}(\text{fall}) \ge 0.75 \right) \land \left( \max(SMV) > 16.0\text{ m/s}^2 \right)$$

* Nếu chỉ vung tay mạnh ($\max(SMV) > 16.0\text{ m/s}^2$ nhưng $P_{\text{TinyML}}(\text{fall}) < 0.75$): **Hệ thống tự động chặn báo giả.**
* Nếu đặt nghiêng hoặc xoay cổ tay giống tư thế nằm nhưng không có va đập ($\max(SMV) \le 16.0\text{ m/s}^2$): **Hệ thống tự động chặn báo giả.**

---

## 📱 5. Ứng Dụng Android `FallGuard C3` (Kotlin + Jetpack Compose)

Ứng dụng di động trong thư mục [FallAlertApp](file:///d:/Fall_Alert/Fall_Alert_Sys/FallAlertApp) được xây dựng 100% bằng **Kotlin** và **Jetpack Compose** theo phong cách thiết kế **Glacier Crisp Ice Theme** (kính mờ Glassmorphism, độ tương phản cao chuẩn y tế, hiệu ứng nút bấm đàn hồi `TactileBox`):

### 🌟 Các tính năng nổi bật trên 3 Tab chính:
1. **Tab 1 — Trang chủ (`HomeTabScreen.kt`):**
   * **Thẻ Hero Dynamic Status (4 trạng thái động):** Tự động chuyển đổi giao diện giữa *An toàn (Emerald)*, *Đã lọc báo động giả (Amber)*, *Bất thường sinh hiệu Tim/SpO2 (Rose)* và *Cảnh báo Té ngã Khẩn cấp (Crimson)*.
   * **Vòng tròn đếm ngược 15 giây SOS:** Khi phát hiện ngã, hiển thị đồng hồ đếm ngược 15 giây kèm âm thanh báo động. Người đeo có thể bấm **"✔ Tôi ổn (Tắt còi)"** để hủy báo động và tắt còi Buzzer trên mạch C3, hoặc bấm **"📞 Gọi Cấp cứu 115"** ngay lập tức.
   * **Lưới Bento 2×2 Thông số Thời gian thực:**
     * ❤️ **Nhịp tim (PPG):** Số BPM lớn, icon tim đập theo nhịp và đồ thị mini ECG.
     * 🩸 **Nồng độ Oxy (SpO2 %):** Kèm biểu đồ sóng mini và thanh tiến trình.
     * 💥 **Gia tốc SMV (MPU6050):** Hiển thị lực đập ($m/s^2$ & quy đổi $G\text{-Force}$) so với vạch ngưỡng đỏ `16.0 m/s²`.
     * 🤖 **TinyML Inference:** Độ tin cậy dự đoán của mạng nơ-ron (`% Fall` vs `% ADL`).
   * **Thanh Kiểm chứng Xác thực 2 Lớp (Dual-Layer Strip)** và **Biểu đồ sóng thời gian thực (Canvas 60 FPS)** cho phép chuyển đổi mượt giữa sóng gia tốc `SMV` và sóng quang học `PPG Tim`.
2. **Tab 2 — Lịch sử Sự kiện (`HistoryTabScreen.kt`):**
   * 3 thẻ KPI thống kê nhanh (*Tổng té ngã*, *Đã gửi SOS*, *Người đeo tự tắt*).
   * Thanh chip lọc theo danh mục (*Tất cả*, *Đã gửi SOS*, *Tự tắt*, *Báo giả đã lọc*, *Tim mạch/SpO2*).
   * Danh sách thẻ Timeline chi tiết từng vụ việc kèm lưới 4 thông số lúc xảy ra sự cố (`LỰC SMV`, `AI TINYML`, `NHỊP TIM`, `SPO2`).
3. **Tab 3 — Cài đặt & Phần cứng (`SettingsTabScreen.kt`):**
   * Thông tin chẩn đoán nhanh phần cứng (`Firmware v2.4.12`, `MPU6050 100Hz`, `MAX30102 0x57`).
   * Thanh trượt hiệu chuẩn **Ngưỡng lực va chạm tiếp đất SMV** (`12.0` – `22.0 m/s²`), tùy chọn thời gian đếm ngược (`10s / 15s / 30s`) và công tắc bật/tắt kiểm chứng kép.
   * Cấu hình danh bạ người thân ưu tiên 1 (tích hợp nút **"📞 Gọi thử"** mở trực tiếp trình quay số điện thoại Android), nút **"🔊 Bấm kêu thử còi Buzzer 85dB trên vòng tay"** và nút đồng bộ cấu hình xuống Flash ESP32-C3.
4. **Thanh giả lập kịch bản `ESP32-C3 IoT Test Simulator` (Nổi phía dưới màn hình):**
   * Cho phép giảng viên và người dùng bấm test trực tiếp 5 chế độ (`✅ An toàn`, `🛡️ Báo giả`, `🚨 Ngã thật`, `❤️ Tim/SpO2`, `🎲 Random`) ngay trên điện thoại.

---

## 🚀 6. Hướng Dẫn Biên Dịch & Chạy Hệ Thống

### Bước 1: Nạp Firmware cho mạch đeo tay `ESP32-C3 SuperMini`
1. Mở VS Code (đã cài tiện ích **PlatformIO IDE**).
2. Mở thư mục dự án phần cứng chính thức:
   ```text
   D:\Fall_Alert\Fall_Alert_Sys\Fall_Alert_Project_ESP32_C3_SUPERMINI\Fall_Alert_Sys_Esp32_C3_Supermini
   ```
3. Cắm cáp USB Type-C vào mạch **ESP32-C3 SuperMini**, bấm nút **Upload (➔)** trên thanh trạng thái PlatformIO và mở **Serial Monitor (`115200 baud`)** để quan sát thông số AI + Nhịp tim + SpO2.

### Bước 2: Cài đặt & Chạy Ứng dụng Android `FallGuard C3`
1. Cắm điện thoại Android (đã bật **USB Debugging / Gỡ lỗi USB**) vào máy tính hoặc bật máy ảo Android Emulator.
2. Mở Terminal tại thư mục `FallAlertApp` và chạy:
   ```powershell
   cd D:\Fall_Alert\Fall_Alert_Sys\FallAlertApp
   .\gradlew.bat installDebug
   ```
3. Mở ứng dụng **FallGuard C3** trên điện thoại để theo dõi thông số và bấm thử nghiệm các kịch bản trên thanh Simulator.
