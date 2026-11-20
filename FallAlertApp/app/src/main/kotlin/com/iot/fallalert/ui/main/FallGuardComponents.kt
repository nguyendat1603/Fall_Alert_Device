package com.iot.fallalert.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iot.fallalert.ui.theme.GlacierColors

@Composable
fun TactileBox(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  shape: RoundedCornerShape = RoundedCornerShape(12.dp),
  backgroundColor: Color = Color.White,
  border: BorderStroke? = null,
  contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
  content: @Composable () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.94f else 1f,
      animationSpec =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
      label = "tactileScale"
    )

  Surface(
    modifier =
      modifier
        .scale(scale)
        .clip(shape)
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick
        ),
    shape = shape,
    color = backgroundColor,
    border = border
  ) {
    Box(modifier = Modifier.padding(contentPadding), contentAlignment = Alignment.Center) {
      content()
    }
  }
}

@Composable
fun GlacierGlassCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 18.dp,
  borderColor: Color = GlacierColors.Glacier200.copy(alpha = 0.75f),
  backgroundColor: Color = Color.White.copy(alpha = 0.88f),
  contentPadding: PaddingValues = PaddingValues(14.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    modifier =
      modifier.shadow(
        elevation = 4.dp,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = GlacierColors.Glacier300.copy(alpha = 0.35f),
        spotColor = GlacierColors.Glacier400.copy(alpha = 0.25f)
      ),
    shape = RoundedCornerShape(cornerRadius),
    color = backgroundColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Column(modifier = Modifier.padding(contentPadding), content = content)
  }
}

@Composable
fun FallGuardTopHeader(systemClock: String, modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "onlinePulse")
  val pulseAlpha by
    infiniteTransition.animateFloat(
      initialValue = 0.35f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(tween(900), repeatMode = RepeatMode.Reverse),
      label = "pulseAlpha"
    )

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = GlacierColors.Glacier50.copy(alpha = 0.94f),
    shadowElevation = 3.dp,
    border = BorderStroke(0.5.dp, Color.White)
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
      // Android Status Bar Emulation Row
      Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = systemClock,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = GlacierColors.Slate700
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier =
              Modifier.background(GlacierColors.Glacier100, RoundedCornerShape(4.dp))
                .border(1.dp, GlacierColors.Glacier300, RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
          ) {
            Text(
              text = "5G IoT",
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = GlacierColors.Glacier700
            )
          }
          Text(
            text = "📶 98%",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = GlacierColors.Slate700
          )
        }
      }

      // App Brand & ESP32-C3 Online Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Brand Icon Box with green online dot
          Box(contentAlignment = Alignment.TopEnd) {
            Box(
              modifier =
                Modifier.size(38.dp)
                  .background(
                    brush =
                      Brush.linearGradient(
                        listOf(GlacierColors.Glacier100, GlacierColors.Glacier200)
                      ),
                    shape = RoundedCornerShape(12.dp)
                  )
                  .border(1.dp, GlacierColors.Glacier300, RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Text(text = "⌚", fontSize = 18.sp)
            }
            Box(
              modifier =
                Modifier.offset(x = 2.dp, y = (-2).dp)
                  .size(10.dp)
                  .background(GlacierColors.Emerald500.copy(alpha = pulseAlpha), CircleShape)
                  .border(1.5.dp, Color.White, CircleShape)
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "FallGuard",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GlacierColors.Slate900
              )
              Box(
                modifier =
                  Modifier.background(GlacierColors.Glacier500, RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "C3",
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier.size(6.dp).background(GlacierColors.Emerald500, CircleShape)
              )
              Text(
                text = "ESP32-C3 SuperMini • BLE 5.0",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = GlacierColors.Slate500
              )
            }
          }
        }

        // Right Status Pill
        Column(horizontalAlignment = Alignment.End) {
          Row(
            modifier =
              Modifier.background(GlacierColors.Emerald50, RoundedCornerShape(50))
                .border(1.dp, GlacierColors.Emerald300, RoundedCornerShape(50))
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Box(
              modifier =
                Modifier.size(7.dp)
                  .background(GlacierColors.Emerald500.copy(alpha = pulseAlpha), CircleShape)
            )
            Text(
              text = "Trực tuyến",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = GlacierColors.Emerald700
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "⌚ Đang đeo tay • 42ms",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = GlacierColors.Slate500
          )
        }
      }
    }
  }
}

@Composable
fun SimulatorFloatingDock(
  currentScenario: ScenarioMode,
  onSelectScenario: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .shadow(
          elevation = 10.dp,
          shape = RoundedCornerShape(18.dp),
          ambientColor = GlacierColors.Glacier400.copy(alpha = 0.45f)
        ),
    shape = RoundedCornerShape(18.dp),
    color = Color.White.copy(alpha = 0.94f),
    border = BorderStroke(1.dp, GlacierColors.Glacier200)
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          Box(modifier = Modifier.size(7.dp).background(GlacierColors.Glacier500, CircleShape))
          Text(
            text = "ESP32-C3 IOT TEST SIMULATOR",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = GlacierColors.Slate700
          )
        }
        Text(
          text = "Bấm thử nghiệm tương tác",
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          color = GlacierColors.Slate500
        )
      }

      Spacer(modifier = Modifier.height(5.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        // 1. Safe
        SimulatorDockButton(
          label = "✅ An toàn",
          bgColor = GlacierColors.Emerald50,
          borderColor = GlacierColors.Emerald200,
          textColor = GlacierColors.Emerald700,
          isSelected = currentScenario == ScenarioMode.SAFE,
          onClick = { onSelectScenario("safe") },
          modifier = Modifier.weight(1f)
        )
        // 2. False Alarm
        SimulatorDockButton(
          label = "🛡️ Báo giả",
          bgColor = GlacierColors.Amber50,
          borderColor = GlacierColors.Amber200,
          textColor = GlacierColors.Amber700,
          isSelected = currentScenario == ScenarioMode.FALSE_ALARM,
          onClick = { onSelectScenario("false_alarm") },
          modifier = Modifier.weight(1f)
        )
        // 3. Real Fall
        SimulatorDockButton(
          label = "🚨 Ngã thật",
          bgColor = GlacierColors.Red600,
          borderColor = GlacierColors.Red700,
          textColor = Color.White,
          isSelected = currentScenario == ScenarioMode.REAL_FALL,
          onClick = { onSelectScenario("real_fall") },
          modifier = Modifier.weight(1.08f)
        )
        // 4. Cardio
        SimulatorDockButton(
          label = "❤️ Tim/SpO2",
          bgColor = GlacierColors.Rose50,
          borderColor = GlacierColors.Rose200,
          textColor = GlacierColors.Rose700,
          isSelected = currentScenario == ScenarioMode.CARDIO,
          onClick = { onSelectScenario("cardio") },
          modifier = Modifier.weight(1.05f)
        )
        // 5. Random
        SimulatorDockButton(
          label = "🎲 Random",
          bgColor = GlacierColors.Glacier100,
          borderColor = GlacierColors.Glacier300,
          textColor = GlacierColors.Glacier800,
          isSelected = false,
          onClick = { onSelectScenario("random") },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun SimulatorDockButton(
  label: String,
  bgColor: Color,
  borderColor: Color,
  textColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  TactileBox(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(9.dp),
    backgroundColor = bgColor,
    border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, borderColor),
    contentPadding = PaddingValues(horizontal = 3.dp, vertical = 7.dp)
  ) {
    Text(
      text = label,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.Bold,
      color = textColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
  }
}

