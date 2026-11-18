---
name: kotlin-android-iot-health
description: Expert guidance for building Kotlin Android mobile applications (Jetpack Compose, MVVM, StateFlow, Foreground Service, Notification Channels, MQTT/Firebase/REST) for ESP32 IoT Fall Detection and Real-time Health Monitoring (Heart Rate, SpO2, Fall Alerts).
---

# Kotlin Android IoT Fall Detection & Health Monitoring Skill

Use this skill when designing, scaffolding, coding, or debugging the Android mobile application in **Kotlin** (with **Jetpack Compose**) that connects to the **ESP32-C3 SuperMini** wearable fall detection and health monitoring system.

## 1. End-to-End Architecture Options
1. **Option A: MQTT Broker (Recommended for HUST IT4735 IoT Course)**
   - **ESP32-C3 SuperMini** connects to Wi-Fi and publishes JSON payloads to an MQTT Broker (`broker.emqx.io`, `hivemq`, or local Mosquitto/Node.js server) on topics:
     - `fall_alert/device_01/telemetry` (periodic HR, SpO2, SMV, battery, status)
     - `fall_alert/device_01/alert` (instant high-priority QoS 1 message when `is_real_fall == true`)
   - **Kotlin Android App** uses an MQTT client (`HiveMQ MQTT Client` or `Eclipse Paho`) inside an Android **Foreground Service** to stay connected in the background and trigger a High-Priority **Full-Screen / Heads-Up Notification** + alarm sound + vibration immediately upon receiving a fall event.
2. **Option B: Firebase Realtime Database + FCM (Cloud Realtime)**
   - **ESP32-C3** sends HTTP PUT/POST or uses `Firebase-ESP-Client` to update `/devices/device_01/live` and `/alerts`.
   - **Kotlin Android App** listens to `FirebaseDatabase` `ValueEventListener` (in-app real-time charts) and `FirebaseMessagingService` (push notifications even when the app is closed).

## 2. Standard JSON Payload Contract (ESP32 <-> Kotlin App)
```json
{
  "device_id": "esp32_c3_wrist_01",
  "timestamp": 1731950000,
  "fall_detected": true,
  "fall_confidence": 0.92,
  "impact_smv_ms2": 21.4,
  "heart_rate_bpm": 86,
  "spo2_pct": 98,
  "status": "FALL_ALERT"
}
```

## 3. Kotlin Android Tech Stack & Best Practices
- **UI Layer:** Jetpack Compose (Material 3), `StateFlow` in `ViewModel` (`androidx.lifecycle:lifecycle-viewmodel-compose`).
- **Real-time Background Alerting:**
  - Create a `NotificationChannel` with `NotificationManager.IMPORTANCE_HIGH`, custom alarm sound, and vibration pattern.
  - Request `Manifest.permission.POST_NOTIFICATIONS` (Android 13 / API 33+) and `Manifest.permission.FOREGROUND_SERVICE`.
- **Health Dashboard UI:**
  - Status Banner: Safe (Green) vs. Fall Emergency (Flashing Red with "Đã xử lý / Gọi cấp cứu" action buttons).
  - Vital Cards: Heart Rate (BPM), SpO2 (%), Peak Impact Force ($m/s^2$), AI Confidence (%).
  - Alert History Log: List of past fall events with timestamps.
