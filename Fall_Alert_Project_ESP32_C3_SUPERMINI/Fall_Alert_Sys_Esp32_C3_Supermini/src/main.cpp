#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include "MAX30105.h"
#include "heartRate.h"

// 1. Khai báo thư viện Edge Impulse đã export cho ESP32-C3
#include <Wearable-Fall-Detection-ESP32_C3_SUPERMINI_inferencing.h>

// ==========================================
// CẤU HÌNH CHÂN CHO ESP32-C3 SUPERMINI
// ==========================================
#define I2C_SDA_PIN   8     // Chân SDA chung cho cả MPU6050 (0x68) và MAX30102 (0x57)
#define I2C_SCL_PIN   9     // Chân SCL chung cho cả MPU6050 (0x68) và MAX30102 (0x57)
#define BUZZER_PIN    3     // Chân điều khiển Buzzer (GPIO 3)

#define FREQUENCY_HZ  EI_CLASSIFIER_FREQUENCY   // Mặc định là 50Hz (20ms/mẫu)
#define INTERVAL_MS   (1000 / FREQUENCY_HZ)

Adafruit_MPU6050 mpu;
MAX30105 particleSensor;
bool max30102_ready = false;

// Mảng đệm chứa 2 giây dữ liệu (100 mẫu x 6 trục = 600 phần tử float)
static float features[EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE];

// ==========================================
// BIẾN ĐO NHỊP TIM (BPM) & NỒNG ĐỘ OXY (SpO2)
// ==========================================
const byte RATE_SIZE = 4;        // Trung bình trượt 4 nhịp tim gần nhất
byte rates[RATE_SIZE];
byte rateSpot = 0;
long lastBeat = 0;
float beatsPerMinute = 0.0;
int beatAvg = 0;
int spo2Value = 0;
bool fingerDetected = false;

// Biến tính tỷ lệ AC/DC của Red và IR để ước lượng SpO2
double irDC = 0.0, redDC = 0.0;
double irAC_SqSum = 0.0, redAC_SqSum = 0.0;
uint32_t ppgSampleCount = 0;

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

// Hàm đọc nhanh FIFO của MAX30102 (không làm trễ nhịp 50Hz của MPU6050)
void updateHeartRateAndSpO2() {
    if (!max30102_ready) return;

    particleSensor.check(); // Kiểm tra dữ liệu mới trong bộ đệm FIFO của MAX30102

    while (particleSensor.available()) {
        uint32_t irValue = particleSensor.getFIFOIR();
        uint32_t redValue = particleSensor.getFIFORed();
        particleSensor.nextSample();

        // Kiểm tra xem ngón tay / cổ tay có tiếp xúc cảm biến không (ngưỡng hồng ngoại > 50,000)
        if (irValue < 50000) {
            fingerDetected = false;
            beatAvg = 0;
            spo2Value = 0;
            beatsPerMinute = 0;
            ppgSampleCount = 0;
            irAC_SqSum = 0;
            redAC_SqSum = 0;
            continue;
        }

        fingerDetected = true;

        // 1. Phát hiện đỉnh nhịp tim (PPG Peak Detection)
        if (checkForBeat(irValue) == true) {
            long delta = millis() - lastBeat;
            lastBeat = millis();

            beatsPerMinute = 60.0 / (delta / 1000.0);

            if (beatsPerMinute < 190 && beatsPerMinute > 40) {
                rates[rateSpot++] = (byte)beatsPerMinute;
                rateSpot %= RATE_SIZE;

                int sum = 0;
                int validCount = 0;
                for (byte x = 0; x < RATE_SIZE; x++) {
                    if (rates[x] > 0) {
                        sum += rates[x];
                        validCount++;
                    }
                }
                if (validCount > 0) {
                    beatAvg = sum / validCount;
                }
            }
        }

        // 2. Tính toán thành phần DC & AC của kênh Red và IR để suy ra SpO2 (%)
        if (ppgSampleCount == 0) {
            irDC = irValue;
            redDC = redValue;
        } else {
            irDC = 0.95 * irDC + 0.05 * irValue;
            redDC = 0.95 * redDC + 0.05 * redValue;
        }

        double irAC = irValue - irDC;
        double redAC = redValue - redDC;
        irAC_SqSum += irAC * irAC;
        redAC_SqSum += redAC * redAC;
        ppgSampleCount++;

        // Mỗi 50 mẫu quang học (~1 giây) cập nhật lại giá trị SpO2 một lần
        if (ppgSampleCount >= 50) {
            if (irDC > 1000 && redDC > 1000 && irAC_SqSum > 100) {
                double irRMS = sqrt(irAC_SqSum / ppgSampleCount);
                double redRMS = sqrt(redAC_SqSum / ppgSampleCount);
                double R = (redRMS / redDC) / (irRMS / irDC);

                // Công thức hiệu chuẩn SpO2 tiêu chuẩn từ tỷ số R
                int calcSpO2 = (int)(110.0 - 25.0 * R);
                if (calcSpO2 > 100) calcSpO2 = 99;
                if (calcSpO2 < 80)  calcSpO2 = 80;

                if (spo2Value == 0) {
                    spo2Value = calcSpO2;
                } else {
                    spo2Value = (int)(0.7 * spo2Value + 0.3 * calcSpO2);
                }
            }
            irAC_SqSum = 0;
            redAC_SqSum = 0;
            ppgSampleCount = 0;
        }
    }
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

    Serial.println("\n\n========================================================");
    Serial.println("  ESP32-C3 SUPERMINI - FALL DETECTION & HEART MONITOR  ");
    Serial.println("========================================================");

    // Khởi tạo bus I2C chung trên chân GPIO 8 (SDA) và GPIO 9 (SCL) ở tốc độ 400kHz
    Wire.begin(I2C_SDA_PIN, I2C_SCL_PIN);
    Wire.setClock(400000);

    // 1. Khởi tạo cảm biến gia tốc & con quay MPU6050 (Địa chỉ I2C: 0x68)
    if (!mpu.begin(0x68, &Wire)) {
        Serial.println("❌ LỖI: Không tìm thấy cảm biến MPU6050 trên I2C (SDA=8, SCL=9)!");
        Serial.println("Vui lòng kiểm tra lại dây nối VCC, GND, SDA, SCL.");
        while (1) {
            delay(1000);
        }
    }
    mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
    mpu.setGyroRange(MPU6050_RANGE_500_DEG);
    mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);
    Serial.println("✅ 1. MPU6050 (0x68) đã kết nối thành công!");

    // 2. Khởi tạo cảm biến nhịp tim & SpO2 MAX30102 (Địa chỉ I2C: 0x57)
    if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) {
        Serial.println("⚠️ CẢNH BÁO: Không tìm thấy MAX30102 (0x57)! Vui lòng kiểm tra dây nối.");
        Serial.println("   -> Hệ thống vẫn tiếp tục chạy tính năng phát hiện té ngã.");
        max30102_ready = false;
    } else {
        // Cấu hình MAX30102 tối ưu cho đo nhịp tim & SpO2 đồng thời với đọc IMU
        byte ledBrightness = 60;  // Cường độ LED (0-255, 60 ~ 12mA giúp đỡ nóng và chống bão hòa)
        byte sampleAverage = 4;   // Trung bình 4 mẫu phần cứng
        byte ledMode = 2;         // 2 = Red + IR (đo cả nhịp tim và SpO2)
        int sampleRate = 200;     // 200 mẫu/giây (sau chia 4 còn 50 mẫu FIFO/giây)
        int pulseWidth = 411;     // Độ rộng xung 411us (18-bit ADC)
        int adcRange = 4096;      // Dải đo ADC 4096 nA

        particleSensor.setup(ledBrightness, sampleAverage, ledMode, sampleRate, pulseWidth, adcRange);
        max30102_ready = true;
        Serial.println("✅ 2. MAX30102 (0x57) đã kết nối thành công! Hãy đặt ngón tay/cổ tay lên mắt đọc.");
    }

    Serial.printf("Kích thước cửa sổ TinyML: %d phần tử (Tần số: %d Hz)\n", 
                  EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE, FREQUENCY_HZ);
    Serial.println("========================================================\n");
}

void loop() {
    float max_smv = 0.0; // Lưu đỉnh gia tốc va chạm lớn nhất (Signal Magnitude Vector) trong 2 giây

    // 1. Thu thập đủ 1 cửa sổ 2 giây dữ liệu 6 trục từ MPU6050 (50Hz = 20ms/mẫu)
    //    Đồng thời đọc liên tục bộ đệm FIFO của MAX30102 trong thời gian rảnh mỗi chu kỳ 20ms
    for (size_t ix = 0; ix < EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE; ix += 6) {
        uint64_t next_tick = millis() + INTERVAL_MS;

        sensors_event_t a, g, temp;
        mpu.getEvent(&a, &g, &temp);

        float ax = a.acceleration.x;
        float ay = a.acceleration.y;
        float az = a.acceleration.z;

        // Tính tổng hợp lực gia tốc SMV = sqrt(ax^2 + ay^2 + az^2)
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

        // Đọc dữ liệu nhịp tim & SpO2 từ FIFO của MAX30102
        updateHeartRateAndSpO2();

        // Giữ nhịp lấy mẫu chuẩn 50Hz (trong lúc đợi cũng tranh thủ quét FIFO MAX30102)
        while (millis() < next_tick) {
            updateHeartRateAndSpO2();
            delay(1);
        }
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

    // 5. THUẬT TOÁN KẾT HỢP (AI + XUNG LỰC VA CHẠM > 16.0 m/s^2)
    bool is_real_fall = (fall_score >= 0.75) && (max_smv > 16.0);

    // 6. Hiển thị đồng thời Trạng thái Té Ngã + Nhịp Tim + SpO2 lên Serial Monitor
    if (!max30102_ready) {
        Serial.printf("[AI] ADL: %5.1f%% | FALL: %5.1f%% | Lực: %4.1f m/s2 | [MAX30102: Chưa kết nối] -> %s\n",
                      adl_score * 100.0, fall_score * 100.0, max_smv,
                      is_real_fall ? "🚨 TÉ NGÃ THẬT!" : "Bình thường");
    } else if (!fingerDetected) {
        Serial.printf("[AI] ADL: %5.1f%% | FALL: %5.1f%% | Lực: %4.1f m/s2 | ❤️ Chưa đặt tay lên cảm biến -> %s\n",
                      adl_score * 100.0, fall_score * 100.0, max_smv,
                      is_real_fall ? "🚨 TÉ NGÃ THẬT!" : "Bình thường");
    } else {
        Serial.printf("[AI] ADL: %5.1f%% | FALL: %5.1f%% | Lực: %4.1f m/s2 | ❤️ HR: %3d BPM | 🩸 SpO2: %2d%% -> %s\n",
                      adl_score * 100.0, fall_score * 100.0, max_smv,
                      beatAvg, spo2Value,
                      is_real_fall ? "🚨 TÉ NGÃ THẬT!" : "Bình thường");
    }

    // 7. CẢNH BÁO CÒI: Chỉ hú còi khi thỏa mãn cả AI + Lực va chạm thực tế
    if (is_real_fall) {
        Serial.println("\n***************************************************");
        Serial.println(">>> CẢNH BÁO: PHÁT HIỆN VA CHẠM & TÉ NGÃ ĐEO TAY! <<<");
        if (fingerDetected) {
            Serial.printf(">>> CHỈ SỐ SINH TỒN LÚC NGÃ: Nhịp tim = %d BPM | SpO2 = %d%% <<<\n", beatAvg, spo2Value);
        }
        Serial.println("***************************************************\n");
        
        for (int i = 0; i < 3; i++) {
            buzzerOn();
            delay(150);
            buzzerOff();
            delay(100);
        }
    }
}
