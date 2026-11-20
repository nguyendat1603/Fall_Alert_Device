package com.iot.fallalert.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iot.fallalert.ui.theme.GlacierColors
import java.util.Locale

@Composable
fun HomeTabScreen(
  state: FallGuardUiState,
  onCancelFallAlarm: () -> Unit,
  onCallEmergencySos: () -> Unit,
  onSwitchWaveform: (WaveformMode) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // 1. HERO DYNAMIC STATUS CARD (4 STATES)
    HeroDynamicStatusCard(
      state = state,
      onCancelFallAlarm = onCancelFallAlarm,
      onCallEmergencySos = onCallEmergencySos
    )

    // 2. REAL-TIME 2x2 BENTO SENSOR GRID
    BentoSensorGrid(state = state)

    // 3. DUAL-LAYER FUSION VERIFICATION STRIP
    DualLayerVerificationStrip(state = state)

    // 4. LIVE DUAL-WAVEFORM CHART CARD
    LiveWaveformChartCard(
      state = state,
      onSwitchWaveform = onSwitchWaveform
    )
  }
}

@Composable
private fun HeroDynamicStatusCard(
  state: FallGuardUiState,
  onCancelFallAlarm: () -> Unit,
  onCallEmergencySos: () -> Unit
) {
  val (bgTop, bgBottom, borderCol, accentCol, iconBgCol, iconEmoji) =
    when (state.scenarioMode) {
      ScenarioMode.REAL_FALL ->
        HeroStyle(
          GlacierColors.Rose50,
          GlacierColors.Red100,
          GlacierColors.Red300,
          GlacierColors.Red700,
          GlacierColors.Red500.copy(alpha = 0.16f),
          "🚨"
        )
      ScenarioMode.FALSE_ALARM ->
        HeroStyle(
          GlacierColors.Amber50,
          Color(0xFFFEF9C3),
          GlacierColors.Amber300,
          GlacierColors.Amber700,
          GlacierColors.Amber100,
          "🛡️"
        )
      ScenarioMode.CARDIO ->
        HeroStyle(
          GlacierColors.Rose50,
          Color(0xFFFCE7F3),
          GlacierColors.Rose300,
          GlacierColors.Rose700,
          GlacierColors.Rose100,
          "❤️"
        )
      ScenarioMode.SAFE ->
        HeroStyle(
          GlacierColors.Emerald50,
          Color(0xFFF0FDFA),
          GlacierColors.Emerald300,
          GlacierColors.Emerald700,
          GlacierColors.Emerald100,
          "✅"
        )
    }

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.5.dp, borderCol),
    shadowElevation = if (state.scenarioMode == ScenarioMode.REAL_FALL) 8.dp else 4.dp
  ) {
    Column(
      modifier =
        Modifier.background(Brush.verticalGradient(listOf(bgTop, bgBottom))).padding(14.dp)
    ) {
      // Top Row of Hero Card
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier =
              Modifier.size(40.dp)
                .background(iconBgCol, RoundedCornerShape(12.dp))
                .border(1.dp, borderCol, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(text = iconEmoji, fontSize = 18.sp)
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Box(modifier = Modifier.size(7.dp).background(accentCol, CircleShape))
              Text(
                text = state.heroBadge,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = accentCol,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = state.heroTitle,
              fontSize = 14.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color =
                if (state.scenarioMode == ScenarioMode.REAL_FALL) GlacierColors.Red700
                else GlacierColors.Slate900
            )
          }
        }

        // C3: 100Hz Pill
        Box(
          modifier =
            Modifier.background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
              .border(1.dp, GlacierColors.Slate200, RoundedCornerShape(6.dp))
              .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
          Text(
            text = "C3: 100Hz",
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Slate600
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Subtitle
      Text(
        text = state.heroSubtitle,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium,
        color = GlacierColors.Slate700,
        lineHeight = 16.sp
      )

      // EMERGENCY COUNTDOWN & CTA BOX (Visible on REAL_FALL)
      AnimatedVisibility(visible = state.scenarioMode == ScenarioMode.REAL_FALL) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
          // Circular Countdown Row
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(14.dp))
                .border(1.dp, GlacierColors.Red300, RoundedCornerShape(14.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              // Circular SVG/Canvas Countdown
              Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                val progress =
                  if (state.totalCountdownSeconds > 0) {
                    state.countdownSeconds.toFloat() / state.totalCountdownSeconds.toFloat()
                  } else 0f

                Canvas(modifier = Modifier.fillMaxSize()) {
                  drawCircle(
                    color = GlacierColors.Rose200,
                    style = Stroke(width = 4.dp.toPx())
                  )
                  drawArc(
                    color = GlacierColors.Rose600,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                  )
                }
                Text(
                  text = "${state.countdownSeconds}s",
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.ExtraBold,
                  color = GlacierColors.Red600
                )
              }

              Column {
                Text(
                  text = "TỰ ĐỘNG GỌI SOS & GỬI GPS SAU: ${state.countdownSeconds}S",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = GlacierColors.Slate900
                )
                Text(
                  text = "Đang kích hoạt còi hú 85dB trên cổ tay",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = GlacierColors.Red700
                )
              }
            }

            Box(
              modifier =
                Modifier.background(GlacierColors.Red600, RoundedCornerShape(5.dp))
                  .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = "CẤP CỨU",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Two prominent tactile CTAs
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            TactileBox(
              onClick = onCancelFallAlarm,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(13.dp),
              backgroundColor = Color.White,
              border = BorderStroke(1.dp, GlacierColors.Slate300),
              contentPadding = PaddingValues(vertical = 11.dp, horizontal = 8.dp)
            ) {
              Text(
                text = "✔ Tôi ổn (Tắt còi)",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = GlacierColors.Slate800
              )
            }

            TactileBox(
              onClick = onCallEmergencySos,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(13.dp),
              backgroundColor = GlacierColors.Red600,
              border = BorderStroke(1.dp, GlacierColors.Red700),
              contentPadding = PaddingValues(vertical = 11.dp, horizontal = 8.dp)
            ) {
              Text(
                text = "📞 Gọi Cấp cứu 115",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }
    }
  }
}

private data class HeroStyle(
  val bgTop: Color,
  val bgBottom: Color,
  val borderCol: Color,
  val accentCol: Color,
  val iconBgCol: Color,
  val iconEmoji: String
)

@Composable
private fun BentoSensorGrid(state: FallGuardUiState) {
  val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
  val heartScale by
    infiniteTransition.animateFloat(
      initialValue = 1.0f,
      targetValue = 1.22f,
      animationSpec = infiniteRepeatable(tween(480), repeatMode = RepeatMode.Reverse),
      label = "heartScale"
    )

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    // ROW 1: Heart Rate & SpO2
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Card 1: Nhịp tim (MAX30102 PPG)
      GlacierGlassCard(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Box(modifier = Modifier.size(7.dp).background(GlacierColors.Rose500, CircleShape))
            Text(
              text = "Nhịp tim (PPG)",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = GlacierColors.Slate700
            )
          }
          Text(
            text = "❤️",
            fontSize = 13.sp,
            modifier = Modifier.scale(heartScale)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = state.heartRateBpm.toString(),
            fontSize = 27.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = GlacierColors.Slate900
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "BPM",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = GlacierColors.Slate500,
            modifier = Modifier.padding(bottom = 3.dp)
          )
        }

        // Mini ECG Sparkline
        Box(
          modifier =
            Modifier.padding(vertical = 6.dp)
              .fillMaxWidth()
              .height(30.dp)
              .background(GlacierColors.Rose50, RoundedCornerShape(7.dp))
              .border(1.dp, GlacierColors.Rose100, RoundedCornerShape(7.dp))
              .padding(4.dp)
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val pts =
              listOf(
                0f to 0.5f,
                0.2f to 0.5f,
                0.25f to 0.42f,
                0.3f to 0.5f,
                0.38f to 0.15f,
                0.43f to 0.88f,
                0.48f to 0.25f,
                0.54f to 0.5f,
                0.75f to 0.5f,
                0.82f to 0.18f,
                0.87f to 0.82f,
                0.92f to 0.5f,
                1f to 0.5f
              )
            val path = Path()
            pts.forEachIndexed { idx, (px, py) ->
              val x = px * size.width
              val y = py * size.height
              if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
              path = path,
              color = GlacierColors.Rose500,
              style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
          }
        }

        val isHighHr = state.heartRateBpm > 105 || state.heartRateBpm < 58
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier =
              Modifier.background(
                  if (isHighHr) GlacierColors.Rose50 else GlacierColors.Emerald50,
                  RoundedCornerShape(5.dp)
                )
                .border(
                  1.dp,
                  if (isHighHr) GlacierColors.Rose200 else GlacierColors.Emerald200,
                  RoundedCornerShape(5.dp)
                )
                .padding(horizontal = 5.dp, vertical = 2.dp)
          ) {
            Text(
              text = state.heartStatusBadge,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (isHighHr) GlacierColors.Rose700 else GlacierColors.Emerald700,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          Text(
            text = "~ 76 avg",
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            color = GlacierColors.Slate500
          )
        }
      }

      // Card 2: Nồng độ Oxy trong máu (SpO2)
      GlacierGlassCard(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Box(modifier = Modifier.size(7.dp).background(GlacierColors.Glacier500, CircleShape))
            Text(
              text = "Nồng độ Oxy (SpO2)",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = GlacierColors.Slate700
            )
          }
          Text(text = "🩸", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = state.spo2Pct.toString(),
            fontSize = 27.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = GlacierColors.Slate900
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "%",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = GlacierColors.Slate500,
            modifier = Modifier.padding(bottom = 3.dp)
          )
        }

        // Mini SpO2 Wave
        Box(
          modifier =
            Modifier.padding(vertical = 6.dp)
              .fillMaxWidth()
              .height(30.dp)
              .background(GlacierColors.Glacier50, RoundedCornerShape(7.dp))
              .border(1.dp, GlacierColors.Glacier200, RoundedCornerShape(7.dp))
              .padding(4.dp)
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val pts =
              listOf(
                0f to 0.75f,
                0.2f to 0.45f,
                0.4f to 0.62f,
                0.6f to 0.35f,
                0.8f to 0.3f,
                1f to 0.45f
              )
            val path = Path()
            pts.forEachIndexed { idx, (px, py) ->
              val x = px * size.width
              val y = py * size.height
              if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
              path = path,
              color = GlacierColors.Glacier600,
              style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
          }
        }

        val isLowSpo2 = state.spo2Pct < 94
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier =
              Modifier.background(
                  if (isLowSpo2) GlacierColors.Red50 else GlacierColors.Glacier100,
                  RoundedCornerShape(5.dp)
                )
                .border(
                  1.dp,
                  if (isLowSpo2) GlacierColors.Red200 else GlacierColors.Glacier200,
                  RoundedCornerShape(5.dp)
                )
                .padding(horizontal = 5.dp, vertical = 2.dp)
          ) {
            Text(
              text = state.spo2StatusBadge,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (isLowSpo2) GlacierColors.Red700 else GlacierColors.Glacier700
            )
          }

          Box(
            modifier =
              Modifier.width(36.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(GlacierColors.Slate200)
          ) {
            Box(
              modifier =
                Modifier.fillMaxHeight()
                  .fillMaxWidth((state.spo2Pct / 100f).coerceIn(0.1f, 1f))
                  .background(
                    if (isLowSpo2) GlacierColors.Red500 else GlacierColors.Glacier500,
                    RoundedCornerShape(50)
                  )
            )
          }
        }
      }
    }

    // ROW 2: SMV Impact & TinyML Inference
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Card 3: Gia tốc SMV (MPU6050)
      GlacierGlassCard(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Box(modifier = Modifier.size(7.dp).background(GlacierColors.Amber500, CircleShape))
            Text(
              text = "Gia tốc SMV",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = GlacierColors.Slate700
            )
          }
          Box(
            modifier =
              Modifier.background(GlacierColors.Slate100, RoundedCornerShape(4.dp))
                .border(1.dp, GlacierColors.Slate200, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = "G-Force",
              fontSize = 8.5.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = GlacierColors.Slate600
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = String.format(Locale.US, "%.1f", state.smvMs2),
            fontSize = 27.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = GlacierColors.Slate900
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "m/s²",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = GlacierColors.Slate500,
            modifier = Modifier.padding(bottom = 3.dp)
          )
        }

        val gForce = String.format(Locale.US, "%.2fG", state.smvMs2 / 9.80665f)
        val smvProgress = (state.smvMs2 / 26f).coerceIn(0.08f, 1f)
        val isOverThresh = state.smvMs2 > state.smvThreshold

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Hiện tại: $gForce",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = GlacierColors.Slate700
          )
          Text(
            text = "Ngưỡng: ${state.smvThreshold}",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Red600
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(7.dp)
              .clip(RoundedCornerShape(50))
              .background(GlacierColors.Slate200)
        ) {
          Box(
            modifier =
              Modifier.fillMaxHeight()
                .fillMaxWidth(smvProgress)
                .background(
                  when {
                    state.scenarioMode == ScenarioMode.REAL_FALL -> GlacierColors.Red500
                    isOverThresh -> GlacierColors.Amber500
                    else -> GlacierColors.Emerald500
                  },
                  RoundedCornerShape(50)
                )
          )
        }
      }

      // Card 4: TinyML Inference
      GlacierGlassCard(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Box(modifier = Modifier.size(7.dp).background(GlacierColors.Purple500, CircleShape))
            Text(
              text = "TinyML Inference",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = GlacierColors.Slate700
            )
          }
          Text(text = "🤖", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = String.format(Locale.US, "%.1f", state.aiFallPercent),
            fontSize = 27.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = GlacierColors.Slate900
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "% Fall",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = GlacierColors.Slate500,
            modifier = Modifier.padding(bottom = 3.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val isAiFall = state.aiFallPercent >= 75f
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier =
              Modifier.background(
                  when {
                    isAiFall -> GlacierColors.Red50
                    state.scenarioMode == ScenarioMode.FALSE_ALARM -> GlacierColors.Amber50
                    else -> GlacierColors.Emerald50
                  },
                  RoundedCornerShape(5.dp)
                )
                .border(
                  1.dp,
                  when {
                    isAiFall -> GlacierColors.Red200
                    state.scenarioMode == ScenarioMode.FALSE_ALARM -> GlacierColors.Amber200
                    else -> GlacierColors.Emerald200
                  },
                  RoundedCornerShape(5.dp)
                )
                .padding(horizontal = 5.dp, vertical = 2.dp)
          ) {
            Text(
              text = state.aiClassBadge,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color =
                when {
                  isAiFall -> GlacierColors.Red700
                  state.scenarioMode == ScenarioMode.FALSE_ALARM -> GlacierColors.Amber700
                  else -> GlacierColors.Emerald700
                },
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          Text(
            text = "Edge INT8",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = GlacierColors.Slate500
          )
        }
      }
    }
  }
}

@Composable
private fun DualLayerVerificationStrip(state: FallGuardUiState) {
  GlacierGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "KIỂM CHỨNG XÁC THỰC 2 LỚP (DUAL-LAYER)",
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = GlacierColors.Slate700
      )

      val (buzzerBg, buzzerTextCol) =
        when {
          state.isBuzzerAlerting -> GlacierColors.Red600 to Color.White
          state.scenarioMode == ScenarioMode.FALSE_ALARM ->
            GlacierColors.Amber50 to GlacierColors.Amber800
          else -> GlacierColors.Slate100 to GlacierColors.Slate600
        }

      Box(
        modifier =
          Modifier.background(buzzerBg, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
      ) {
        Text(
          text = state.buzzerHardwareState,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = buzzerTextCol
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Layer 1 Box
      LayerStatusBox(
        title = state.layer1Text,
        status = state.layer1Status,
        isPassed = state.layer1Passed,
        isWarning = false,
        modifier = Modifier.weight(1f)
      )
      // Layer 2 Box
      LayerStatusBox(
        title = state.layer2Text,
        status = state.layer2Status,
        isPassed = state.layer2Passed,
        isWarning = state.layer2Warning,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun LayerStatusBox(
  title: String,
  status: String,
  isPassed: Boolean,
  isWarning: Boolean,
  modifier: Modifier = Modifier
) {
  val bg =
    when {
      isPassed -> GlacierColors.Red50
      isWarning -> GlacierColors.Amber50
      else -> GlacierColors.Slate50
    }
  val border =
    when {
      isPassed -> GlacierColors.Red300
      isWarning -> GlacierColors.Amber300
      else -> GlacierColors.Slate200
    }

  Row(
    modifier =
      modifier
        .background(bg, RoundedCornerShape(10.dp))
        .border(1.dp, border, RoundedCornerShape(10.dp))
        .padding(horizontal = 8.dp, vertical = 7.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.SemiBold,
      color = GlacierColors.Slate800,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Box(
      modifier =
        Modifier.background(
            if (isPassed) GlacierColors.Red600 else Color.Transparent,
            RoundedCornerShape(4.dp)
          )
          .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
      Text(
        text = status,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color =
          when {
            isPassed -> Color.White
            isWarning -> GlacierColors.Amber700
            else -> GlacierColors.Slate500
          }
      )
    }
  }
}

@Composable
private fun LiveWaveformChartCard(
  state: FallGuardUiState,
  onSwitchWaveform: (WaveformMode) -> Unit
) {
  GlacierGlassCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier =
              Modifier.size(8.dp)
                .background(
                  if (state.waveformMode == WaveformMode.SMV) GlacierColors.Glacier500
                  else GlacierColors.Rose500,
                  CircleShape
                )
          )
          Text(
            text =
              if (state.waveformMode == WaveformMode.SMV)
                "Dạng sóng thời gian thực: Gia tốc SMV (m/s²)"
              else "Dạng sóng thời gian thực: Xung quang học (PPG)",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Slate900
          )
        }
        Text(
          text = "Tần số lấy mẫu: 100 Hz • Cửa sổ trượt 3.0s",
          fontSize = 9.5.sp,
          fontFamily = FontFamily.Monospace,
          color = GlacierColors.Slate500
        )
      }

      // Segmented Toggle
      Row(
        modifier =
          Modifier.background(GlacierColors.Slate100, RoundedCornerShape(8.dp))
            .border(1.dp, GlacierColors.Slate200, RoundedCornerShape(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        WaveformTogglePill(
          label = "SMV",
          selected = state.waveformMode == WaveformMode.SMV,
          activeColor = GlacierColors.Glacier700,
          onClick = { onSwitchWaveform(WaveformMode.SMV) }
        )
        WaveformTogglePill(
          label = "PPG Tim",
          selected = state.waveformMode == WaveformMode.PPG,
          activeColor = GlacierColors.Rose600,
          onClick = { onSwitchWaveform(WaveformMode.PPG) }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Canvas Waveform Box
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .height(135.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color.White)
          .border(1.dp, GlacierColors.Glacier200, RoundedCornerShape(12.dp))
    ) {
      val series =
        if (state.waveformMode == WaveformMode.SMV) state.smvHistory else state.ppgHistory
      val strokeColor =
        when {
          state.waveformMode == WaveformMode.PPG -> GlacierColors.Rose600
          state.scenarioMode == ScenarioMode.REAL_FALL -> GlacierColors.Rose600
          state.scenarioMode == ScenarioMode.FALSE_ALARM -> GlacierColors.Amber600
          else -> GlacierColors.Glacier600
        }

      Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        val w = size.width
        val h = size.height

        // Draw subtle grid lines
        val rows = 4
        val cols = 6
        for (r in 1 until rows) {
          val y = h * r / rows
          drawLine(
            color = GlacierColors.Glacier100,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1.dp.toPx()
          )
        }
        for (c in 1 until cols) {
          val x = w * c / cols
          drawLine(
            color = GlacierColors.Glacier100,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1.dp.toPx()
          )
        }

        // Dashed Red Threshold Line for SMV mode
        if (state.waveformMode == WaveformMode.SMV) {
          val yThresh = (h - (state.smvThreshold / 26f) * h).coerceIn(8f, h - 8f)
          drawLine(
            color = GlacierColors.Red500,
            start = Offset(0f, yThresh),
            end = Offset(w, yThresh),
            strokeWidth = 1.3.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
          )
        }

        if (series.size >= 2) {
          val maxScale = if (state.waveformMode == WaveformMode.SMV) 26f else 100f
          val stepX = w / (series.size - 1).toFloat()
          val linePath = Path()
          val fillPath = Path()

          series.forEachIndexed { idx, value ->
            val x = idx * stepX
            val y = (h - (value / maxScale) * h).coerceIn(4f, h - 4f)
            if (idx == 0) {
              linePath.moveTo(x, y)
              fillPath.moveTo(x, h)
              fillPath.lineTo(x, y)
            } else {
              linePath.lineTo(x, y)
              fillPath.lineTo(x, y)
            }
          }
          fillPath.lineTo(w, h)
          fillPath.close()

          drawPath(
            path = fillPath,
            brush =
              Brush.verticalGradient(
                listOf(strokeColor.copy(alpha = 0.22f), strokeColor.copy(alpha = 0.0f))
              )
          )
          drawPath(
            path = linePath,
            color = strokeColor,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
          )
        }
      }

      // Threshold Label Pill inside chart
      if (state.waveformMode == WaveformMode.SMV) {
        Box(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .padding(top = 6.dp, end = 8.dp)
              .background(Color.White.copy(alpha = 0.94f), RoundedCornerShape(5.dp))
              .border(1.dp, GlacierColors.Red200, RoundedCornerShape(5.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "Ngưỡng té: ${state.smvThreshold} m/s²",
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Red600
          )
        }
      }

      // Peak impact pill when falling
      if (state.scenarioMode == ScenarioMode.REAL_FALL &&
        state.waveformMode == WaveformMode.SMV) {
        Box(
          modifier =
            Modifier.align(Alignment.TopStart)
              .padding(top = 6.dp, start = 8.dp)
              .background(GlacierColors.Red600, RoundedCornerShape(50))
              .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "● Va đập đỉnh: ${state.smvMs2} m/s²",
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text =
          if (state.waveformMode == WaveformMode.SMV) "Tín hiệu lọc Kalman (MPU6050)"
          else "Tín hiệu hồng ngoại MAX30102",
        fontSize = 9.5.sp,
        fontFamily = FontFamily.Monospace,
        color = GlacierColors.Slate500
      )
      val currentWaveVal = state.smvHistory.lastOrNull() ?: state.smvMs2
      Text(
        text =
          if (state.waveformMode == WaveformMode.SMV)
            String.format(Locale.US, "Đỉnh hiện thời: %.2f m/s²", currentWaveVal)
          else "Nhịp PPG: ${state.heartRateBpm} BPM",
        fontSize = 9.5.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color =
          if (state.scenarioMode == ScenarioMode.REAL_FALL) GlacierColors.Red600
          else GlacierColors.Glacier700
      )
    }
  }
}

@Composable
private fun WaveformTogglePill(
  label: String,
  selected: Boolean,
  activeColor: Color,
  onClick: () -> Unit
) {
  TactileBox(
    onClick = onClick,
    shape = RoundedCornerShape(6.dp),
    backgroundColor = if (selected) Color.White else Color.Transparent,
    border = if (selected) BorderStroke(1.dp, GlacierColors.Glacier200) else null,
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = label,
      fontSize = 9.5.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      color = if (selected) activeColor else GlacierColors.Slate500
    )
  }
}
