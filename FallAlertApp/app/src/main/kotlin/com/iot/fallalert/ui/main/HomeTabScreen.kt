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

