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

