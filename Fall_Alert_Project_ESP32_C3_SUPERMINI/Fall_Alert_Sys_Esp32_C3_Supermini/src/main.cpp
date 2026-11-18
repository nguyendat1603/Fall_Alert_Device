#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>

// 1. Khai báo thư viện Edge Impulse đã export cho ESP32-C3
#include <Wearable-Fall-Detection-ESP32_C3_SUPERMINI_inferencing.h>

// ==========================================
// CẤU HÌNH CHÂN CHO ESP32-C3 SUPERMINI
// ==========================================
#define I2C_SDA_PIN   8     // Chân SDA mặc định cho ESP32-C3 SuperMini
#define I2C_SCL_PIN   9     // Chân SCL mặc định cho ESP32-C3 SuperMini
#define BUZZER_PIN    3     // Chân điều khiển Buzzer (GPIO 3)

#define FREQUENCY_HZ  EI_CLASSIFIER_FREQUENCY   // Mặc định là 50Hz (20ms/mẫu)
#define INTERVAL_MS   (1000 / FREQUENCY_HZ)

Adafruit_MPU6050 mpu;

// Mảng đệm chứa 2 giây dữ liệu (100 mẫu x 6 trục = 600 phần tử float)
static float features[EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE];

// Module Buzzer 3 chân kích mức THẤP (Active-LOW PNP):
// - Bật còi: Kéo chân I/O xuống LOW (0V)
// - Tắt còi: Đưa chân I/O về HIGH và đưa về ngõ vào INPUT (High-Z) để ngắt hoàn toàn dòng rò
void buzzerOn() {
    pinMode(BUZZER_PIN, OUTPUT);
    digitalWrite(BUZZER_PIN, LOW);
}

void buzzerOff() {
    digitalWrite(BUZZER_PIN, HIGH);
    pinMode(BUZZER_PIN, INPUT);
}

void setup() {
    Serial.begin(115200);
    buzzerOff(); // Tắt còi ngay lập tức khi cấp nguồn

    // Đợi kết nối USB CDC trên ESP32-C3 (tối đa 2.5 giây nếu không cắm Serial Monitor)
    unsigned long start_wait = millis();
    while (!Serial && (millis() - start_wait < 2500)) {
        delay(10);
    }
    delay(500);

    Serial.println("\n\n==================================================");
    Serial.println("  ESP32-C3 SUPERMINI - TINYML FALL DETECTION  ");
    Serial.println("==================================================");

    // Khởi tạo I2C trên chân GPIO 8 (SDA) và GPIO 9 (SCL) của ESP32-C3 SuperMini
    Wire.begin(I2C_SDA_PIN, I2C_SCL_PIN);

    if (!mpu.begin(0x68, &Wire)) {
        Serial.println("❌ LỖI: Không tìm thấy cảm biến MPU6050 trên I2C (SDA=8, SCL=9)!");
        Serial.println("Vui lòng kiểm tra lại dây nối VCC, GND, SDA, SCL.");
        while (1) {
            delay(1000);
        }
    }
    
    // Cấu hình tầm đo cho MPU6050
    mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
    mpu.setGyroRange(MPU6050_RANGE_500_DEG);
    mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);

    Serial.println("✅ MPU6050 đã kết nối thành công!");
    Serial.printf("Kích thước cửa sổ TinyML: %d phần tử (Tần số: %d Hz)\n", 
                  EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE, FREQUENCY_HZ);
    Serial.println("Hệ thống đã sẵn sàng thu thập dữ liệu cổ tay và nhận diện té ngã...");
    Serial.println("==================================================\n");
}

void loop() {
    float max_smv = 0.0; // Lưu đỉnh gia tốc va chạm lớn nhất (Signal Magnitude Vector) trong 2 giây

    // 1. Thu thập đủ 1 cửa sổ 2 giây dữ liệu 6 trục từ MPU6050 (50Hz = 20ms/mẫu)
    for (size_t ix = 0; ix < EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE; ix += 6) {
        uint64_t next_tick = millis() + INTERVAL_MS;

        sensors_event_t a, g, temp;
        mpu.getEvent(&a, &g, &temp);

        float ax = a.acceleration.x;
        float ay = a.acceleration.y;
        float az = a.acceleration.z;

        // Tính tổng hợp lực gia tốc SMV = sqrt(ax^2 + ay^2 + az^2)
        // Bình thường đứng yên hoặc đeo tay tĩnh: SMV ~ 9.8 m/s^2 (~1g)
        // Khi có va chạm tiếp đất: SMV thường vọt lên > 18 m/s^2 - 25 m/s^2 (> 2g)
        float smv = sqrt(ax * ax + ay * ay + az * az);
        if (smv > max_smv) {
            max_smv = smv;
        }

        // Lưu theo đúng thứ tự 6 trục: accX, accY, accZ, gyroX, gyroY, gyroZ
        features[ix + 0] = ax;
        features[ix + 1] = ay;
        features[ix + 2] = az;
        features[ix + 3] = g.gyro.x;
        features[ix + 4] = g.gyro.y;
        features[ix + 5] = g.gyro.z;

        while (millis() < next_tick); // Giữ nhịp lấy mẫu chuẩn 50Hz
    }

    // 2. Đóng gói buffer đưa vào mô hình TinyML
    signal_t signal;
    int err = numpy::signal_from_buffer(features, EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE, &signal);
    if (err != 0) {
        Serial.printf("Lỗi tạo signal buffer (%d)\n", err);
        return;
    }

    // 3. Chạy suy luận mạng nơ-ron Edge Impulse (Inference)
    ei_impulse_result_t result = { 0 };
    EI_IMPULSE_ERROR r = run_classifier(&signal, &result, false);
    if (r != EI_IMPULSE_OK) {
        Serial.printf("Lỗi chạy mô hình (%d)\n", r);
        return;
    }

    // 4. Trích xuất xác suất mô hình dự đoán
    float fall_score = 0.0;
    float adl_score = 0.0;

    for (size_t ix = 0; ix < EI_CLASSIFIER_LABEL_COUNT; ix++) {
        if (strcmp(result.classification[ix].label, "fall") == 0) {
            fall_score = result.classification[ix].value;
        } else if (strcmp(result.classification[ix].label, "adl") == 0) {
            adl_score = result.classification[ix].value;
        }
    }

    // =========================================================================
    // THUẬT TOÁN KẾT HỢP (DUAL-LAYER FUSION):
    // 1. AI Edge Impulse nhận diện động học cú ngã (Fall Score >= 75%)
    // 2. Cảm biến vật lý xác nhận CÓ XUNG LỰC VA CHẠM TIẾP ĐẤT (max_smv > 16.0 m/s^2, ~1.63g)
    // Giúp loại bỏ hoàn toàn báo động giả khi chỉ vung tay mạnh hoặc để tĩnh trên bàn!
    // =========================================================================
    bool is_real_fall = (fall_score >= 0.75) && (max_smv > 16.0);

    Serial.printf("[AI C3] ADL: %5.1f%% | FALL: %5.1f%% | Lực đỉnh: %4.1f m/s2 -> Trạng thái: %s\n", 
                  adl_score * 100.0, fall_score * 100.0, max_smv,
                  is_real_fall ? "🚨 TÉ NGÃ THẬT!" : "Bình thường");

    // 5. CẢNH BÁO CÒI: Chỉ hú còi khi thỏa mãn cả AI + Lực va chạm thực tế
    if (is_real_fall) {
        Serial.println("\n***************************************************");
        Serial.println(">>> CẢNH BÁO: PHÁT HIỆN VA CHẠM & TÉ NGÃ ĐEO TAY! <<<");
        Serial.println("***************************************************\n");
        
        for (int i = 0; i < 3; i++) {
            buzzerOn();
            delay(150);
            buzzerOff();
            delay(100);
        }
    }
}
