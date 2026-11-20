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

class MainScreenViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(FallGuardUiState())
  val uiState: StateFlow<FallGuardUiState> = _uiState.asStateFlow()

  private var countdownJob: Job? = null
  private var toastJob: Job? = null
  private var waveStep = 0

  private data class FallPreset(
    val title: String,
    val badge: String,
    val shortName: String,
    val smvRange: ClosedFloatingPointRange<Float>,
    val aiRange: ClosedFloatingPointRange<Float>,
    val hrRange: IntRange,
    val spo2Range: IntRange
  )

  private val fallPresets =
    listOf(
      FallPreset(
        title = "CẢNH BÁO: NGÃ NGỬA ĐẬP SÀN!",
        badge = "🚨 PHÁT HIỆN TÉ NGÃ: NGÃ NGỬA ĐẬP SÀN",
        shortName = "Té ngã ngửa",
        smvRange = 21.5f..26.2f,
        aiRange = 91.0f..98.8f,
        hrRange = 115..132,
        spo2Range = 93..96
      ),
      FallPreset(
        title = "CẢNH BÁO: NGÃ CHÚI CHỐNG CỔ TAY!",
        badge = "🚨 PHÁT HIỆN TÉ NGÃ: NGÃ CHÚI VỀ TRƯỚC",
        shortName = "Ngã chúi trước",
        smvRange = 19.5f..24.5f,
        aiRange = 88.0f..97.5f,
        hrRange = 110..126,
        spo2Range = 94..97
      ),
      FallPreset(
        title = "CẢNH BÁO: NGÃ NGHIÊNG ĐẬP HÔNG!",
        badge = "🚨 PHÁT HIỆN TÉ NGÃ: NGÃ NGHIÊNG",
        shortName = "Ngã nghiêng",
        smvRange = 18.2f..23.4f,
        aiRange = 85.0f..96.0f,
        hrRange = 106..122,
        spo2Range = 94..97
      ),
      FallPreset(
        title = "CẢNH BÁO: CHOÁNG NGẤT TỤT HUYẾT ÁP!",
        badge = "🚨 PHÁT HIỆN TÉ NGÃ: CHOÁNG NGẤT NGÃ QUỴ",
        shortName = "Ngất ngã quỵ",
        smvRange = 17.5f..21.8f,
        aiRange = 89.0f..97.0f,
        hrRange = 48..55,
        spo2Range = 89..92
      ),
      FallPreset(
        title = "CẢNH BÁO: TRƯỢT NGÃ KHỎI GHẾ!",
        badge = "🚨 PHÁT HIỆN TÉ NGÃ: TRƯỢT KHỎI GHẾ",
        shortName = "Trượt khỏi ghế",
        smvRange = 17.2f..20.8f,
        aiRange = 82.0f..94.5f,
        hrRange = 102..118,
        spo2Range = 95..97
      )
    )

  init {
    startClockAndWaveformLoop()
    startCountdownTimer()
  }

  private fun startClockAndWaveformLoop() {
    viewModelScope.launch {
      val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
      while (true) {
        waveStep++
        val state = _uiState.value
        val nextSmv =
          when (state.scenarioMode) {
            ScenarioMode.REAL_FALL -> 20.2f + (sin(waveStep * 0.45) * 4.8).toFloat()
            ScenarioMode.FALSE_ALARM -> 14.5f + (sin(waveStep * 0.35) * 2.6).toFloat()
            ScenarioMode.CARDIO -> 9.8f + (Random.nextFloat() - 0.5f) * 0.7f
            ScenarioMode.SAFE -> 9.8f + (Random.nextFloat() - 0.5f) * 0.8f
          }

        val freq = (state.heartRateBpm / 60.0) * 0.32
        val nextPpg =
          (50.0 + sin(waveStep * freq) * 30.0 + sin(waveStep * freq * 2.0) * 12.0).toFloat()

        _uiState.update { current ->
          current.copy(
            systemClock = timeFormat.format(Date()),
            smvHistory = (current.smvHistory.drop(1) + nextSmv),
            ppgHistory = (current.ppgHistory.drop(1) + nextPpg)
          )
        }
        delay(80L)
      }
    }
  }

  private fun startCountdownTimer() {
    countdownJob?.cancel()
    val total = _uiState.value.selectedCountdownOption
    _uiState.update { it.copy(countdownSeconds = total, totalCountdownSeconds = total) }

    countdownJob =
      viewModelScope.launch {
        while (_uiState.value.countdownSeconds > 0 &&
          _uiState.value.scenarioMode == ScenarioMode.REAL_FALL) {
          delay(1000L)
          val remaining = _uiState.value.countdownSeconds - 1
          _uiState.update { it.copy(countdownSeconds = remaining) }
          if (remaining == 0) {
            playBeep(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 450)
            showToast("🚨 ĐÃ TỰ ĐỘNG GỬI TIN NHẮN VÀ TỌA ĐỘ SOS TỚI 115!")
          }
        }
      }
  }

  fun switchTab(tab: AppTab) {
    _uiState.update { it.copy(currentTab = tab) }
  }

  fun switchWaveform(mode: WaveformMode) {
    _uiState.update { it.copy(waveformMode = mode) }
  }

  fun filterHistory(filter: HistoryFilter) {
    _uiState.update { it.copy(selectedHistoryFilter = filter) }
  }

  fun resetHistoryFilter() {
    _uiState.update { it.copy(selectedHistoryFilter = HistoryFilter.ALL) }
    showToast("🔄 Đã đặt lại bộ lọc hiển thị nhật ký")
  }

  fun simulateScenario(modeInput: String) {
    val targetMode =
      when (modeInput) {
        "safe" -> ScenarioMode.SAFE
        "false_alarm" -> ScenarioMode.FALSE_ALARM
        "real_fall" -> ScenarioMode.REAL_FALL
        "cardio" -> ScenarioMode.CARDIO
        else -> ScenarioMode.entries.random()
      }

    countdownJob?.cancel()
    val thresh = _uiState.value.smvThreshold
    val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    when (targetMode) {
      ScenarioMode.SAFE -> {
        val hr = Random.nextInt(70, 84)
        val spo2 = Random.nextInt(97, 100)
        val smv = (9.6f + Random.nextFloat() * 0.8f).round1()
        val ai = (1.2f + Random.nextFloat() * 3.0f).round1()

        _uiState.update {
          it.copy(
            scenarioMode = ScenarioMode.SAFE,
            heroBadge = "TRẠNG THÁI: AN TOÀN",
            heroTitle = "Người đeo đang hoạt động ổn định",
            heroSubtitle =
              "Hoạt động: Đi bộ sinh hoạt bình thường • Không có va chạm • AI TinyML tự tin ${(100f - ai).round1()}%.",
            heartRateBpm = hr,
            heartStatusBadge = "Bình thường (60-100)",
            spo2Pct = spo2,
            spo2StatusBadge = "Tốt (95-100%)",
            smvMs2 = smv,
            aiFallPercent = ai,
            aiClassBadge = "ADL: ${(100f - ai).round1()}%",
            layer1Text = "1. AI TinyML: $ai%",
            layer1Status = "Chưa",
            layer1Passed = false,
            layer2Text = "2. SMV: $smv < ${thresh.round1()}",
            layer2Status = "Chưa",
            layer2Warning = false,
            layer2Passed = false,
            buzzerHardwareState = "🔕 Buzzer C3: TẮT",
            isBuzzerAlerting = false
          )
        }
        showToast("✅ Đã chuyển sang trạng thái: An toàn / Sinh hoạt")
      }

      ScenarioMode.FALSE_ALARM -> {
        val isWristSwing = Random.nextBoolean()
        val actionTitle =
          if (isWristSwing) "Vung tay mạnh khi với đồ trên cao"
          else "Ngồi phịch mạnh xuống ghế sofa"
        val smv = (thresh + 1.2f + Random.nextFloat() * 2.2f).round1()
        val ai = (16.5f + Random.nextFloat() * 24.0f).round1()
        val hr = Random.nextInt(84, 96)
        val spo2 = Random.nextInt(96, 99)

        val newEvent =
          FallIncidentEvent(
            id = System.currentTimeMillis(),
            category = IncidentCategory.FILTERED,
            emoji = "🛡️",
            title = actionTitle,
            tagBadge = "LỌC BÁO GIẢ",
            timeAndLocation = "Hôm nay, $nowTime • Báo động đã tự lọc",
            rightStatusBadge = "CHẶN THÀNH CÔNG",
            smvText = "$smv m/s²",
            aiText = "$ai%",
            hrText = "$hr BPM",
            spo2Text = "$spo2%",
            actionSummary =
              "Xung lực $smv m/s² vượt ngưỡng nhưng AI TinyML nhận diện sinh hoạt ($ai% fall < 75%). Còi buzzer không hú."
          )

        _uiState.update {
          it.copy(
            scenarioMode = ScenarioMode.FALSE_ALARM,
            heroBadge = "🛡️ ĐÃ LỌC BÁO ĐỘNG GIẢ (ADL FILTERED)",
            heroTitle = actionTitle,
            heroSubtitle =
              "Xung lực SMV đạt $smv m/s² nhưng AI TinyML nhận diện hoạt động sinh hoạt (chỉ $ai% fall). Không kích hoạt còi hú.",
            heartRateBpm = hr,
            heartStatusBadge = "Vận động nhẹ",
            spo2Pct = spo2,
            spo2StatusBadge = "Bình thường",
            smvMs2 = smv,
            aiFallPercent = ai,
            aiClassBadge = "ADL Chặn ($ai%)",
            layer1Text = "1. AI TinyML: $ai%",
            layer1Status = "$ai% (Trượt)",
            layer1Passed = false,
            layer2Text = "2. SMV: $smv > ${thresh.round1()}",
            layer2Status = "$smv m/s² (Vượt)",
            layer2Warning = true,
            layer2Passed = false,
            buzzerHardwareState = "🛡️ Lọc an toàn: CÒI IM LẶNG",
            isBuzzerAlerting = false,
            historyEvents = listOf(newEvent) + it.historyEvents
          )
        }
        showToast("🛡️ Thuật toán kép đã chặn báo giả: $actionTitle")
      }

      ScenarioMode.REAL_FALL -> {
        val preset = fallPresets.random()
        val minSmv = maxOf(preset.smvRange.start, thresh + 1.5f)
        val maxSmv = maxOf(preset.smvRange.endInclusive, minSmv + 2.0f)
        val smv = (minSmv + Random.nextFloat() * (maxSmv - minSmv)).round1()
        val ai =
          (preset.aiRange.start +
              Random.nextFloat() * (preset.aiRange.endInclusive - preset.aiRange.start))
            .round1()
        val hr = Random.nextInt(preset.hrRange.first, preset.hrRange.last + 1)
        val spo2 = Random.nextInt(preset.spo2Range.first, preset.spo2Range.last + 1)

        val newEvent =
          FallIncidentEvent(
            id = System.currentTimeMillis(),
            category = IncidentCategory.SOS_SENT,
            emoji = "🚨",
            title = "Phát hiện sự cố: ${preset.shortName}",
            tagBadge = "KHẨN CẤP",
            timeAndLocation = "Hôm nay, $nowTime • Cổ tay ESP32-C3",
            rightStatusBadge = "ĐANG CẢNH BÁO",
            smvText = "$smv m/s²",
            aiText = "$ai%",
            hrText = "$hr BPM",
            spo2Text = "$spo2%",
            actionSummary =
              "Còi 85dB đã kích hoạt • Va đập $smv m/s² kết hợp AI xác nhận ${preset.shortName} ($ai%)."
          )

        _uiState.update {
          it.copy(
            scenarioMode = ScenarioMode.REAL_FALL,
            heroBadge = preset.badge,
            heroTitle = preset.title,
            heroSubtitle =
              "Va đập cực mạnh SMV $smv m/s² kết hợp mất phương hướng cổ tay. AI TinyML xác nhận té ngã $ai%.",
            heartRateBpm = hr,
            heartStatusBadge = if (hr < 60) "Nhịp chậm ($hr)" else "Tăng vọt (Hồi hộp)",
            spo2Pct = spo2,
            spo2StatusBadge = if (spo2 < 94) "Hơi thấp ($spo2%)" else "Tốt ($spo2%)",
            smvMs2 = smv,
            aiFallPercent = ai,
            aiClassBadge = "${preset.shortName} ($ai%)",
            layer1Text = "1. AI TinyML: $ai%",
            layer1Status = "$ai% (ĐẠT)",
            layer1Passed = true,
            layer2Text = "2. SMV: $smv > ${thresh.round1()}",
            layer2Status = "$smv m/s² (ĐẠT)",
            layer2Warning = false,
            layer2Passed = true,
            buzzerHardwareState = "🔊 CÒI C3: ĐANG HÚ BÁO ĐỘNG",
            isBuzzerAlerting = true,
            historyEvents = listOf(newEvent) + it.historyEvents
          )
        }
        playBeep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
        startCountdownTimer()
        showToast("🚨 BÁO ĐỘNG: ${preset.badge}!")
      }

      ScenarioMode.CARDIO -> {
        val hr = Random.nextInt(132, 146)
        val spo2 = Random.nextInt(88, 92)
        val smv = (9.7f + Random.nextFloat() * 0.5f).round1()
        val ai = (1.1f + Random.nextFloat() * 2.0f).round1()

        val newEvent =
          FallIncidentEvent(
            id = System.currentTimeMillis(),
            category = IncidentCategory.CARDIO,
            emoji = "❤️",
            title = "Nhịp tim nhanh ($hr BPM) & SpO2 giảm ($spo2%)",
            tagBadge = "SINH HIỆU",
            timeAndLocation = "Hôm nay, $nowTime • Cảnh báo MAX30102",
            rightStatusBadge = "$hr BPM",
            smvText = "$smv m/s²",
            aiText = "$ai%",
            hrText = "$hr BPM",
            spo2Text = "$spo2%",
            actionSummary =
              "Không có va chạm cơ học nhưng nhịp tim tăng $hr BPM và SpO2 giảm còn $spo2%. Cần nghỉ ngơi theo dõi."
          )

        _uiState.update {
          it.copy(
            scenarioMode = ScenarioMode.CARDIO,
            heroBadge = "❤️ BẤT THƯỜNG SINH HIỆU (MAX30102)",
            heroTitle = "Nhịp tim nhanh ($hr BPM) & SpO2 giảm ($spo2%)",
            heroSubtitle =
              "Không phát hiện va chạm cơ học. Đề xuất người đeo nghỉ ngơi tại chỗ và liên hệ bác sĩ gia đình.",
            heartRateBpm = hr,
            heartStatusBadge = "Nhịp tim nhanh (>120)",
            spo2Pct = spo2,
            spo2StatusBadge = "Thiếu oxy (<92%)",
            smvMs2 = smv,
            aiFallPercent = ai,
            aiClassBadge = "ADL Bình thường",
            layer1Text = "1. AI TinyML: $ai%",
            layer1Status = "Chưa",
            layer1Passed = false,
            layer2Text = "2. SMV: $smv < ${thresh.round1()}",
            layer2Status = "Chưa",
            layer2Warning = false,
            layer2Passed = false,
            buzzerHardwareState = "🔕 Buzzer: TẮT (Báo rung nhẹ)",
            isBuzzerAlerting = false,
            historyEvents = listOf(newEvent) + it.historyEvents
          )
        }
        playBeep(ToneGenerator.TONE_PROP_BEEP2, 250)
        showToast("❤️ Cảnh báo sinh hiệu: Nhịp tim $hr BPM & SpO2 $spo2%")
      }
    }
  }

  fun cancelFallAlarm() {
    countdownJob?.cancel()
    val elapsed = _uiState.value.totalCountdownSeconds - _uiState.value.countdownSeconds
    val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    val cur = _uiState.value

    val cancelledEvent =
      FallIncidentEvent(
        id = System.currentTimeMillis(),
        category = IncidentCategory.CANCELLED,
        emoji = "⚠️",
        title = "Người đeo xác nhận an toàn (${cur.aiClassBadge})",
        tagBadge = "ĐÃ TẮT CÒI",
        timeAndLocation = "Hôm nay, $nowTime • Phản hồi trên App",
        rightStatusBadge = "TỰ TẮT (${elapsed}s)",
        smvText = "${cur.smvMs2} m/s²",
        aiText = "${cur.aiFallPercent}%",
        hrText = "${cur.heartRateBpm} BPM",
        spo2Text = "${cur.spo2Pct}%",
        actionSummary =
          "Người dùng bấm \"Tôi ổn (Tắt còi)\" sau $elapsed giây. Đã gửi lệnh MQTT tắt còi Buzzer xuống ESP32-C3."
      )

    _uiState.update { it.copy(historyEvents = listOf(cancelledEvent) + it.historyEvents) }
    simulateScenario("safe")
    showToast("✔ Người dùng xác nhận an toàn: Đã tắt còi C3 và hủy gọi SOS")
  }

  fun callEmergencySos() {
    countdownJob?.cancel()
    playBeep(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 400)
    showToast("📞 Đang kết nối trực tiếp tổng đài 115 & gửi SMS định vị tới ${_uiState.value.emergencyPhone}!")
  }

  fun updateSmvThreshold(newVal: Float) {
    val rounded = ( kotlin.math.round(newVal * 2f) / 2f )
    _uiState.update { it.copy(smvThreshold = rounded) }
  }

  fun selectCountdownOption(seconds: Int) {
    _uiState.update {
      it.copy(
        selectedCountdownOption = seconds,
        countdownSeconds = seconds,
        totalCountdownSeconds = seconds
      )
    }
    showToast("⏱️ Đã chọn thời gian đếm ngược cảnh báo: ${seconds}s")
  }

  fun toggleDualLayer(enabled: Boolean) {
    _uiState.update { it.copy(isDualLayerEnabled = enabled) }
    if (enabled) {
      showToast("🛡️ Đã bật Thuật toán 2 lớp: Chống báo động giả tuyệt đối")
    } else {
      showToast("⚠️ Đã tắt kiểm duyệt TinyML: Chỉ kích hoạt theo ngưỡng SMV")
    }
  }

  fun toggleAutoCall115(enabled: Boolean) {
    _uiState.update { it.copy(autoCall115 = enabled) }
  }

  fun toggleAttachGps(enabled: Boolean) {
    _uiState.update { it.copy(attachGpsLink = enabled) }
  }

  fun triggerHardwareBuzzerTest() {
    if (_uiState.value.isTestingBuzzer) return
    viewModelScope.launch {
      _uiState.update { it.copy(isTestingBuzzer = true) }
      playBeep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
      showToast("📢 Đang phát lệnh test còi Buzzer 85dB tới ESP32-C3 qua BLE/MQTT")
      delay(1500L)
      _uiState.update { it.copy(isTestingBuzzer = false) }
    }
  }

  fun saveHardwareConfiguration() {
    if (_uiState.value.isSyncingConfig) return
    viewModelScope.launch {
      _uiState.update { it.copy(isSyncingConfig = true, configSyncedSuccess = false) }
      delay(1100L)
      _uiState.update { it.copy(isSyncingConfig = false, configSyncedSuccess = true) }
      playBeep(ToneGenerator.TONE_PROP_ACK, 200)
      showToast("💾 Cấu hình MPU6050 & Ngưỡng ${_uiState.value.smvThreshold} m/s² đã nạp vào Flash ESP32-C3!")
      delay(2000L)
      _uiState.update { it.copy(configSyncedSuccess = false) }
    }
  }

  fun showToast(message: String) {
    toastJob?.cancel()
    _uiState.update { it.copy(toastMessage = message) }
    toastJob =
      viewModelScope.launch {
        delay(3200L)
        _uiState.update { it.copy(toastMessage = null) }
      }
  }

  private fun playBeep(toneType: Int, durationMs: Int) {
    try {
      val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
      toneGen.startTone(toneType, durationMs)
    } catch (_: Exception) {}
  }

  private fun Float.round1(): Float = kotlin.math.round(this * 10f) / 10f
}
