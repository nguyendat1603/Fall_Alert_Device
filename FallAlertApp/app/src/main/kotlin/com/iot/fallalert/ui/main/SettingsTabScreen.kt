package com.iot.fallalert.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iot.fallalert.ui.theme.GlacierColors
import java.util.Locale

@Composable
fun SettingsTabScreen(
  state: FallGuardUiState,
  onSmvThresholdChange: (Float) -> Unit,
  onSelectCountdownOption: (Int) -> Unit,
  onToggleDualLayer: (Boolean) -> Unit,
  onToggleAutoCall115: (Boolean) -> Unit,
  onToggleAttachGps: (Boolean) -> Unit,
  onTriggerBuzzerTest: () -> Unit,
  onSaveConfig: () -> Unit,
  onShowToast: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Top Hardware Diagnostic Header & 3 Snapshot Cards
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "Cài đặt & Phần cứng ⚙️",
        fontSize = 16.5.sp,
        fontWeight = FontWeight.ExtraBold,
        color = GlacierColors.Slate900
      )
      Text(
        text = "Hiệu chuẩn cảm biến MPU6050 & cấu hình khẩn cấp ESP32-C3 SuperMini",
        fontSize = 11.sp,
        color = GlacierColors.Slate500
      )

      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HardwareSnapshotMiniCard(
          label = "FIRMWARE",
          value = "v2.4.12",
          sub = "OTA Sẵn sàng",
          subColor = GlacierColors.Emerald600,
          modifier = Modifier.weight(1f)
        )
        HardwareSnapshotMiniCard(
          label = "MPU6050",
          value = "OK (100Hz)",
          sub = "Kalman 6-DOF",
          subColor = GlacierColors.Glacier600,
          modifier = Modifier.weight(1f)
        )
        HardwareSnapshotMiniCard(
          label = "MAX30102",
          value = "I2C 0x57",
          sub = "Chạm da: Tốt",
          subColor = GlacierColors.Emerald600,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 1. SMV Impact Threshold & Dual-Layer Algorithm Card
    GlacierGlassCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "⚡ Ngưỡng lực va chạm tiếp đất (SMV)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Slate900
          )
          Text(
            text = "Khuyên dùng: 16.0 m/s² để tránh báo động giả",
            fontSize = 10.sp,
            color = GlacierColors.Slate500
          )
        }
        Box(
          modifier =
            Modifier.background(GlacierColors.Glacier100, RoundedCornerShape(8.dp))
              .border(1.dp, GlacierColors.Glacier300, RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = String.format(Locale.US, "%.1f m/s²", state.smvThreshold),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Glacier700
          )
        }
      }

      Slider(
        value = state.smvThreshold,
        onValueChange = onSmvThresholdChange,
        valueRange = 12.0f..22.0f,
        steps = 19,
        colors =
          SliderDefaults.colors(
            thumbColor = GlacierColors.Glacier600,
            activeTrackColor = GlacierColors.Glacier500,
            inactiveTrackColor = GlacierColors.Slate200
          ),
        modifier = Modifier.fillMaxWidth()
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "12.0 m/s² (Rất nhạy)",
          fontSize = 9.5.sp,
          fontFamily = FontFamily.Monospace,
          color = GlacierColors.Slate500
        )
        Text(
          text = "16.0 m/s² (Chuẩn)",
          fontSize = 9.5.sp,
          fontFamily = FontFamily.Monospace,
          color = GlacierColors.Glacier700,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "22.0 m/s² (Chống rung)",
          fontSize = 9.5.sp,
          fontFamily = FontFamily.Monospace,
          color = GlacierColors.Slate500
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Countdown Timer Selector (10s / 15s / 30s)
      Text(
        text = "⏱️ Thời gian đếm ngược trước khi tự động gọi SOS:",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = GlacierColors.Slate800
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(10, 15, 30).forEach { sec ->
          val isSelected = state.selectedCountdownOption == sec
          TactileBox(
            onClick = { onSelectCountdownOption(sec) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            backgroundColor = if (isSelected) GlacierColors.Glacier50 else Color.White,
            border =
              BorderStroke(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) GlacierColors.Glacier500 else GlacierColors.Slate200
              ),
            contentPadding = PaddingValues(vertical = 8.dp)
          ) {
            Text(
              text = "${sec} giây",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) GlacierColors.Glacier700 else GlacierColors.Slate600
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Dual-Layer Toggle Switch
      SettingSwitchRow(
        icon = "🛡️",
        title = "Bật kiểm chứng kép (AI TinyML + Lực SMV)",
        subtitle = "Chống báo giả tuyệt đối khi vung tay hoặc ngồi mạnh",
        checked = state.isDualLayerEnabled,
        activeColor = GlacierColors.Glacier600,
        onCheckedChange = onToggleDualLayer
      )
    }

    // 2. SOS Emergency Protocol & Contacts Card
    GlacierGlassCard(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "🚨 LIÊN HỆ KHẨN CẤP (SOS)",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.ExtraBold,
        color = GlacierColors.Slate900
      )
      Text(
        text = "Chuỗi hành động cứu trợ khi xảy ra sự cố té ngã",
        fontSize = 10.sp,
        color = GlacierColors.Slate500
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Primary Caregiver Card
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(
              Brush.horizontalGradient(listOf(GlacierColors.Rose50, Color.White)),
              RoundedCornerShape(14.dp)
            )
            .border(1.dp, GlacierColors.Rose200, RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier =
              Modifier.size(36.dp)
                .background(GlacierColors.Rose100, CircleShape)
                .border(1.dp, GlacierColors.Rose200, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "H",
              fontSize = 14.sp,
              fontWeight = FontWeight.ExtraBold,
              color = GlacierColors.Rose600
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Text(
                text = state.emergencyContactName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GlacierColors.Slate900
              )
              Box(
                modifier =
                  Modifier.background(GlacierColors.Rose100, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "Ưu tiên 1",
                  fontSize = 8.5.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = GlacierColors.Rose700
                )
              }
            }
            Text(
              text = "📞 ${state.emergencyPhone}",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = GlacierColors.Slate600
            )
          }
        }

        TactileBox(
          onClick = {
            onShowToast("📞 Đang mở trình gọi khẩn tới: ${state.emergencyPhone}")
            try {
              val dialIntent =
                Intent(
                  Intent.ACTION_DIAL,
                  Uri.parse("tel:${state.emergencyPhone.replace(".", "")}")
                )
              context.startActivity(dialIntent)
            } catch (_: Exception) {}
          },
          shape = RoundedCornerShape(10.dp),
          backgroundColor = GlacierColors.Rose600,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
        ) {
          Text(
            text = "📞 Gọi thử",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      SettingSwitchRow(
        icon = "🚑",
        title = "Tự động gọi cấp cứu 115",
        subtitle = "Nếu người thân không bắt máy sau 30 giây",
        checked = state.autoCall115,
        activeColor = GlacierColors.Rose600,
        onCheckedChange = onToggleAutoCall115
      )

      Spacer(modifier = Modifier.height(6.dp))

      SettingSwitchRow(
        icon = "📍",
        title = "Đính kèm liên kết GPS Live Maps",
        subtitle = "Gửi SMS tọa độ chính xác bán kính 3m",
        checked = state.attachGpsLink,
        activeColor = GlacierColors.Glacier600,
        onCheckedChange = onToggleAttachGps
      )
    }

    // 3. Hardware Diagnostics & IoT Telemetry Card
    GlacierGlassCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "🔧 CHẨN ĐOÁN PHẦN CỨNG & IOT",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GlacierColors.Slate900
          )
          Text(
            text = "Kiểm thử còi báo động trên ESP32-C3 SuperMini & MQTT",
            fontSize = 10.sp,
            color = GlacierColors.Slate500
          )
        }
        Box(
          modifier =
            Modifier.background(GlacierColors.Emerald50, RoundedCornerShape(6.dp))
              .border(1.dp, GlacierColors.Emerald200, RoundedCornerShape(6.dp))
              .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
          Text(
            text = "Online",
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Emerald700
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Remote Buzzer Test Button
      TactileBox(
        onClick = onTriggerBuzzerTest,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        backgroundColor =
          if (state.isTestingBuzzer) GlacierColors.Amber100 else GlacierColors.Amber50,
        border =
          BorderStroke(
            1.dp,
            if (state.isTestingBuzzer) GlacierColors.Amber500 else GlacierColors.Amber300
          ),
        contentPadding = PaddingValues(vertical = 11.dp, horizontal = 12.dp)
      ) {
        Text(
          text =
            if (state.isTestingBuzzer) "🔊⚡ Đang phát tín hiệu test còi 85dB..."
            else "🔊 Bấm kêu thử còi Buzzer 85dB trên vòng tay",
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color =
            if (state.isTestingBuzzer) GlacierColors.Amber800 else GlacierColors.Slate800
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // MQTT Broker Info Box
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .background(GlacierColors.Slate50, RoundedCornerShape(12.dp))
            .border(1.dp, GlacierColors.Glacier200, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "🌐 MQTT Broker",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Slate700
          )
          Text(
            text = "● Độ trễ: 38ms (QoS 1)",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Emerald700
          )
        }
        MqttInfoRow("Server:", "broker.emqx.io:1883", GlacierColors.Slate800)
        MqttInfoRow("Pub Topic:", "fall_alert/c3_wrist/telemetry", GlacierColors.Glacier700)
        MqttInfoRow("Sub Command:", "fall_alert/c3_wrist/cmd", GlacierColors.Purple700)
      }
    }

    // 4. Primary Save Action Button
    TactileBox(
      onClick = onSaveConfig,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      backgroundColor =
        when {
          state.configSyncedSuccess -> GlacierColors.Emerald600
          else -> GlacierColors.Glacier600
        },
      contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp)
    ) {
      Text(
        text =
          when {
            state.isSyncingConfig -> "🔄 ĐANG ĐỒNG BỘ QUA BLE/MQTT..."
            state.configSyncedSuccess -> "✔ ĐÃ ĐỒNG BỘ THÀNH CÔNG!"
            else -> "💾 LƯU CẤU HÌNH XUỐNG ESP32-C3"
          },
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
      )
    }
  }
}

@Composable
private fun HardwareSnapshotMiniCard(
  label: String,
  value: String,
  sub: String,
  subColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = Color.White.copy(alpha = 0.88f),
    border = BorderStroke(1.dp, GlacierColors.Glacier200)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        color = GlacierColors.Slate400
      )
      Text(
        text = value,
        fontSize = 11.5.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
        color = GlacierColors.Slate800
      )
      Text(
        text = sub,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        color = subColor
      )
    }
  }
}

@Composable
private fun SettingSwitchRow(
  icon: String,
  title: String,
  subtitle: String,
  checked: Boolean,
  activeColor: Color,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .background(Color.White, RoundedCornerShape(12.dp))
        .border(1.dp, GlacierColors.Glacier100, RoundedCornerShape(12.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(text = icon, fontSize = 15.sp)
      Column {
        Text(
          text = title,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = GlacierColors.Slate800
        )
        Text(
          text = subtitle,
          fontSize = 9.5.sp,
          color = GlacierColors.Slate500
        )
      }
    }
    Spacer(modifier = Modifier.width(6.dp))
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = activeColor
        )
    )
  }
}

@Composable
private fun MqttInfoRow(label: String, value: String, valueCol: Color) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      fontSize = 9.5.sp,
      fontFamily = FontFamily.Monospace,
      color = GlacierColors.Slate500
    )
    Text(
      text = value,
      fontSize = 9.5.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      color = valueCol
    )
  }
}
