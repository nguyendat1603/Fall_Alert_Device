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
  val historyEvents: List<FallIncidentEvent> = emptyList(),

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

