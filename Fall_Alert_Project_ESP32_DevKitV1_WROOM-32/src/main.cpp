#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>

// 1. Khai báo thư viện Edge Impulse (sửa đúng tên file nếu có khác dấu gạch)
#include <Wearable-Fall-Detection_inferencing.h>

#define BUZZER_PIN 4
#define FREQUENCY_HZ EI_CLASSIFIER_FREQUENCY   // Mặc định là 50Hz (20ms/mẫu)
#define INTERVAL_MS  (1000 / FREQUENCY_HZ)

Adafruit_MPU6050 mpu;

// Mảng đệm chứa 2 giây dữ liệu (100 mẫu x 6 trục = 600 phần tử float)
static float features[EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE];

void setup() {
    Serial.begin(115200);
    delay(1000);

    pinMode(BUZZER_PIN, OUTPUT);
    digitalWrite(BUZZER_PIN, LOW);

    // ESP32 DevKit V1: SDA = 21, SCL = 22
    Wire.begin(21, 22);

    if (!mpu.begin()) {
        Serial.println("Lỗi: Không tìm thấy cảm biến MPU6050!");
        while (1) delay(10);
    }
    
    // Cấu hình tầm đo cho MPU6050
    mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
    mpu.setGyroRange(MPU6050_RANGE_500_DEG);
    mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);

    Serial.println("==================================================");
    Serial.println(">> AI TINYML PHÁT HIỆN TÉ NGÃ ĐÃ SẴN SÀNG! <<");
    Serial.printf("Kích thước cửa sổ: %d phần tử (Tần số: %d Hz)\n", 
                  EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE, FREQUENCY_HZ);
    Serial.println("==================================================");
}

void loop() {
    float max_smv = 0.0; // Lưu đỉnh gia tốc va chạm lớn nhất trong 2 giây

    // 1. Thu thập đủ 1 cửa sổ 2 giây dữ liệu 6 trục từ MPU6050
    for (size_t ix = 0; ix < EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE; ix += 6) {
        uint64_t next_tick = millis() + INTERVAL_MS;

        sensors_event_t a, g, temp;
        mpu.getEvent(&a, &g, &temp);

        float ax = a.acceleration.x;
        float ay = a.acceleration.y;
        float az = a.acceleration.z;

        // Tính tổng hợp lực gia tốc SMV = sqrt(ax^2 + ay^2 + az^2)
        // Bình thường đứng yên hoặc nằm im trên bàn: SMV ~ 9.8 m/s^2 (1g)
        float smv = sqrt(ax * ax + ay * ay + az * az);
        if (smv > max_smv) {
            max_smv = smv;
        }

        // Lưu theo đúng thứ tự 6 trục lúc train trên Edge Impulse
        features[ix + 0] = ax;
        features[ix + 1] = ay;
        features[ix + 2] = az;
        features[ix + 3] = g.gyro.x;
        features[ix + 4] = g.gyro.y;
        features[ix + 5] = g.gyro.z;

        while (millis() < next_tick); // Giữ nhịp lấy mẫu đều đặn đúng 50Hz (20ms)
    }

    // 2. Đóng gói buffer đưa vào mô hình TinyML
    signal_t signal;
    int err = numpy::signal_from_buffer(features, EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE, &signal);
    if (err != 0) {
        Serial.printf("Lỗi tạo signal buffer (%d)\n", err);
        return;
    }

    // 3. Chạy suy luận mạng nơ-ron (Inference)
    ei_impulse_result_t result = { 0 };
    EI_IMPULSE_ERROR r = run_classifier(&signal, &result, false);
    if (r != EI_IMPULSE_OK) {
        Serial.printf("Lỗi chạy mô hình (%d)\n", r);
        return;
    }

    // 4. Đọc xác suất và in kết quả ra màn hình Serial Monitor
    float fall_score = 0.0;
    float adl_score = 0.0;

    for (size_t ix = 0; ix < EI_CLASSIFIER_LABEL_COUNT; ix++) {
        if (strcmp(result.classification[ix].label, "fall") == 0) {
            fall_score = result.classification[ix].value;
        } else if (strcmp(result.classification[ix].label, "adl") == 0) {
            adl_score = result.classification[ix].value;
        }
    }

    // Điều kiện té ngã thực sự: AI báo Fall > 80% VÀ có lực va chạm (max_smv > 16.0 m/s^2, tức > 1.6g)
    bool is_real_fall = (fall_score > 0.80) && (max_smv > 16.0);

    Serial.printf("[AI] ADL: %5.1f%% | FALL: %5.1f%% | Lực đỉnh: %4.1f m/s2 -> Trạng thái: %s\n", 
                  adl_score * 100, fall_score * 100, max_smv,
                  is_real_fall ? "TÉ NGÃ THẬT!" : "Bình thường");

    // 5. CẢNH BÁO: Chỉ hú còi khi có cả va chạm + tư thế ngã
    if (is_real_fall) {
        Serial.println("\n***************************************************");
        Serial.println(">>> CẢNH BÁO: PHÁT HIỆN VA CHẠM & TÉ NGÃ! <<<");
        Serial.println("***************************************************\n");
        
        for (int i = 0; i < 3; i++) {
            digitalWrite(BUZZER_PIN, HIGH);
            delay(150);
            digitalWrite(BUZZER_PIN, LOW);
            delay(100);
        }
    }
}