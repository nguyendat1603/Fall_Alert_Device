#include <Arduino.h>
#include <Wire.h>

void setup() {
    Serial.begin(115200);
    delay(1000);
    
    Serial.println("\n=== KIEM TRA I2C ESP32 DEVKIT V1 ===");
    // Trên ESP32 DevKit V1, Wire.begin() tự động nhận diện SDA = 21, SCL = 22
    Wire.begin(21, 22);
}

void loop() {
    byte error, address;
    int nDevices = 0;

    Serial.println("Dang quet cac thiet bi I2C...");

    for (address = 1; address < 127; address++) {
        Wire.beginTransmission(address);
        error = Wire.endTransmission();

        if (error == 0) {
            Serial.print("-> Tim thay thiet bi tai dia chi: 0x");
            if (address < 16) Serial.print("0");
            Serial.print(address, HEX);

            if (address == 0x68 || address == 0x69) {
                Serial.println(" (MPU6050 OK!)");
            } else if (address == 0x57) {
                Serial.println(" (MAX30102 OK!)");
            } else {
                Serial.println();
            }
            nDevices++;
        }
    }

    if (nDevices == 0) {
        Serial.println("Khong tim thay thiet bi nao! Kiem tra lai day SDA(21), SCL(22), Nguon.");
    } else {
        Serial.printf("Ket thuc quet: Tim thay %d thiet bi.\n", nDevices);
    }

    Serial.println("----------------------------------------------");
    delay(4000);
}