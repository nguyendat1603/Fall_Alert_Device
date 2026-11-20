package com.iot.fallalert.ui.main

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab {
  HOME,
  HISTORY,
  SETTINGS
}

enum class ScenarioMode {
  SAFE,
  FALSE_ALARM,
  REAL_FALL,
  CARDIO
}

enum class WaveformMode {
  SMV,
  PPG
}

enum class IncidentCategory {
  SOS_SENT,
  CANCELLED,
  FILTERED,
  CARDIO
}

enum class HistoryFilter {
  ALL,
  SOS_SENT,
  CANCELLED,
  FILTERED,
  CARDIO
}

data class FallIncidentEvent(
  val id: Long,
  val category: IncidentCategory,
  val emoji: String,
  val title: String,
  val tagBadge: String,
  val timeAndLocation: String,
  val rightStatusBadge: String,
  val smvText: String,
  val aiText: String,
  val hrText: String,
  val spo2Text: String,
  val actionSummary: String
)

data class FallGuardUiState(
  val currentTab: AppTab = AppTab.HOME,
  val scenarioMode: ScenarioMode = ScenarioMode.REAL_FALL,
  val waveformMode: WaveformMode = WaveformMode.SMV,
  val systemClock: String = "00:10",

  // Hero Card State
  val heroBadge: String = "🚨 PHÁT HIỆN TÉ NGÃ: NGÃ NGỬA ĐẬP SÀN",
  val heroTitle: String = "CẢNH BÁO TAI NẠN TÉ NGÃ KHẨN CẤP!",
  val heroSubtitle: String =
    "Lực va chạm vượt ngưỡng cực hạn: 24.8 m/s² • AI TinyML nhận diện ngã: 96.5% • Còi Buzzer ESP32-C3 đang kích hoạt!",
  val countdownSeconds: Int = 15,
  val totalCountdownSeconds: Int = 15,

  // 2x2 Bento Sensor Cards
  val heartRateBpm: Int = 118,
  val heartStatusBadge: String = "Tăng vọt (Hồi hộp)",
  val spo2Pct: Int = 95,
  val spo2StatusBadge: String = "Tốt (95-100%)",
  val smvMs2: Float = 24.8f,
  val aiFallPercent: Float = 96.5f,
  val aiClassBadge: String = "Ngã ngửa (96.5%)",

  // Dual-Layer Verification Strip
  val layer1Text: String = "1. AI TinyML: 96.5%",
  val layer1Status: String = "KÍCH HOẠT",
  val layer1Passed: Boolean = true,
  val layer2Text: String = "2. SMV: 24.8 > 16 m/s²",
  val layer2Status: String = "VƯỢT NGƯỠNG",
  val layer2Warning: Boolean = false,
  val layer2Passed: Boolean = true,
  val buzzerHardwareState: String = "🚨 BUZZER C3: ĐANG HÚ (85dB)",
  val isBuzzerAlerting: Boolean = true,

  // Live Chart Buffers
  val smvHistory: List<Float> = List(60) { 9.8f },
  val ppgHistory: List<Float> = List(60) { 50f },

  // History Timeline
  val selectedHistoryFilter: HistoryFilter = HistoryFilter.ALL,
  val historyEvents: List<FallIncidentEvent> = initialHistoryEvents(),

  // Settings & Hardware Calibration
  val smvThreshold: Float = 16.0f,
  val selectedCountdownOption: Int = 15,
  val isDualLayerEnabled: Boolean = true,
  val emergencyContactName: String = "Anh Hùng (Con trai)",
  val emergencyPhone: String = "0912.345.678",
  val autoCall115: Boolean = true,
  val attachGpsLink: Boolean = true,
  val isTestingBuzzer: Boolean = false,
  val isSyncingConfig: Boolean = false,
  val configSyncedSuccess: Boolean = false,

  // Toast overlay
  val toastMessage: String? = null
)

internal fun initialHistoryEvents(): List<FallIncidentEvent> =
  listOf(
    FallIncidentEvent(
      id = 1L,
      category = IncidentCategory.SOS_SENT,
      emoji = "🚨",
      title = "Phát hiện sự cố té ngã (Ngã ngửa đập sàn)",
      tagBadge = "KHẨN CẤP",
      timeAndLocation = "Hôm nay, 14:32:05 • Hành lang tầng 2",
      rightStatusBadge = "ĐÃ GỬI SOS",
      smvText = "24.8 m/s²",
      aiText = "96.5%",
      hrText = "118 BPM",
      spo2Text = "95%",
      actionSummary =
        "Còi 85dB đã kêu • Đã gửi SMS khẩn cấp & Tọa độ GPS tới 0912.345.678 • Người thân đã xác nhận."
    ),
    FallIncidentEvent(
      id = 2L,
      category = IncidentCategory.CANCELLED,
      emoji = "⚠️",
      title = "Phát hiện sự cố té ngã (Trượt chân)",
      tagBadge = "ĐÃ TẮT CÒI",
      timeAndLocation = "Hôm qua, 15:20:18 • Nhà tắm",
      rightStatusBadge = "TỰ TẮT (8s)",
      smvText = "21.5 m/s²",
      aiText = "91.2%",
      hrText = "110 BPM",
      spo2Text = "96%",
      actionSummary =
        "Người đeo bấm nút \"Tôi ổn (Tắt còi)\" sau 8 giây đếm ngược. Hệ thống không gửi tin báo khẩn SOS."
    ),
    FallIncidentEvent(
      id = 3L,
      category = IncidentCategory.FILTERED,
      emoji = "🛡️",
      title = "Ngồi phịch mạnh xuống ghế sofa",
      tagBadge = "LỌC BÁO GIẢ",
      timeAndLocation = "Hôm qua, 07:45:02 • Phòng khách",
      rightStatusBadge = "CHẶN THÀNH CÔNG",
      smvText = "17.2 m/s²",
      aiText = "18.2%",
      hrText = "88 BPM",
      spo2Text = "98%",
      actionSummary =
        "Lực va chạm 17.2 m/s² vượt ngưỡng nhưng AI TinyML nhận diện tư thế ngồi (18.2% fall). Còi buzzer không hú."
    ),
    FallIncidentEvent(
      id = 4L,
      category = IncidentCategory.CARDIO,
      emoji = "❤️",
      title = "Nhịp tim tăng nhanh lúc nghỉ ngơi",
      tagBadge = "SINH HIỆU",
      timeAndLocation = "20/11, 21:10:48 • Phòng ngủ",
      rightStatusBadge = "132 BPM",
      smvText = "9.8 m/s²",
      aiText = "1.4%",
      hrText = "132 BPM",
      spo2Text = "97%",
      actionSummary =
        "Phát hiện triệu chứng hồi hộp, SpO2 duy trì 97%. Người dùng đã ngồi nghỉ 15 phút và nhịp tim trở lại 82 BPM."
    ),
    FallIncidentEvent(
      id = 5L,
      category = IncidentCategory.SOS_SENT,
      emoji = "🚨",
      title = "Té ngã chúi về trước chống cổ tay",
      tagBadge = "KHẨN CẤP",
      timeAndLocation = "18/11, 06:40:11 • Sân trước",
      rightStatusBadge = "ĐÃ GỬI SOS",
      smvText = "26.4 m/s²",
      aiText = "98.1%",
      hrText = "130 BPM",
      spo2Text = "92%",
      actionSummary =
        "Còi hú 85dB tại chỗ. Đã gửi tọa độ GPS cấp cứu và người nhà tiếp cận hỗ trợ kịp thời sau 4 phút."
    )
  )
