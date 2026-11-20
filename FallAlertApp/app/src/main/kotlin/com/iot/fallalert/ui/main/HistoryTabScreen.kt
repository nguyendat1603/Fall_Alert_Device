package com.iot.fallalert.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iot.fallalert.ui.theme.GlacierColors

@Composable
fun HistoryTabScreen(
  state: FallGuardUiState,
  onSelectFilter: (HistoryFilter) -> Unit,
  onResetFilter: () -> Unit,
  onExportPdf: () -> Unit,
  modifier: Modifier = Modifier
) {
  val totalFalls =
    state.historyEvents.count {
      it.category == IncidentCategory.SOS_SENT || it.category == IncidentCategory.CANCELLED
    }
  val totalSos = state.historyEvents.count { it.category == IncidentCategory.SOS_SENT }
  val totalCancelled = state.historyEvents.count { it.category == IncidentCategory.CANCELLED }

  val filteredEvents =
    when (state.selectedHistoryFilter) {
      HistoryFilter.ALL -> state.historyEvents
      HistoryFilter.SOS_SENT ->
        state.historyEvents.filter { it.category == IncidentCategory.SOS_SENT }
      HistoryFilter.CANCELLED ->
        state.historyEvents.filter { it.category == IncidentCategory.CANCELLED }
      HistoryFilter.FILTERED ->
        state.historyEvents.filter { it.category == IncidentCategory.FILTERED }
      HistoryFilter.CARDIO ->
        state.historyEvents.filter { it.category == IncidentCategory.CARDIO }
    }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // 1. Header & Actions
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
          Box(modifier = Modifier.size(8.dp).background(GlacierColors.Glacier500, CircleShape))
          Text(
            text = "Lịch sử sự kiện & Cảnh báo",
            fontSize = 15.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GlacierColors.Slate900
          )
        }
        Text(
          text = "Dữ liệu cảm biến MPU6050 & MAX30102 • Lưu Flash C3 & Cloud",
          fontSize = 10.5.sp,
          color = GlacierColors.Slate500
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TactileBox(
          onClick = onExportPdf,
          shape = RoundedCornerShape(8.dp),
          backgroundColor = Color.White,
          border = BorderStroke(1.dp, GlacierColors.Glacier300),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)
        ) {
          Text(
            text = "📄 Xuất PDF",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = GlacierColors.Glacier700
          )
        }
        TactileBox(
          onClick = onResetFilter,
          shape = RoundedCornerShape(8.dp),
          backgroundColor = Color.White,
          border = BorderStroke(1.dp, GlacierColors.Slate200),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)
        ) {
          Text(
            text = "Xóa lọc",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = GlacierColors.Slate600
          )
        }
      }
    }

    // 2. Quick Stat KPIs Strip (3 Cards)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      KpiStatCard(
        title = "🚨 TỔNG TÉ NGÃ",
        value = "$totalFalls vụ",
        bgColor = GlacierColors.Red50.copy(alpha = 0.7f),
        borderColor = GlacierColors.Red200,
        titleColor = GlacierColors.Red700,
        valueColor = GlacierColors.Red600,
        modifier = Modifier.weight(1f)
      )
      KpiStatCard(
        title = "📡 ĐÃ GỬI SOS",
        value = "$totalSos vụ",
        bgColor = Color.White.copy(alpha = 0.85f),
        borderColor = GlacierColors.Slate200,
        titleColor = GlacierColors.Slate600,
        valueColor = GlacierColors.Slate800,
        modifier = Modifier.weight(1f)
      )
      KpiStatCard(
        title = "✔ NGƯỜI ĐEO TỰ TẮT",
        value = "$totalCancelled vụ",
        bgColor = GlacierColors.Emerald50.copy(alpha = 0.7f),
        borderColor = GlacierColors.Emerald200,
        titleColor = GlacierColors.Emerald700,
        valueColor = GlacierColors.Emerald700,
        modifier = Modifier.weight(1f)
      )
    }

    // 3. Filter Chips Carousel
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      FilterChipButton(
        label = "Tất cả (${state.historyEvents.size})",
        selected = state.selectedHistoryFilter == HistoryFilter.ALL,
        activeBg = GlacierColors.Glacier600,
        onClick = { onSelectFilter(HistoryFilter.ALL) }
      )
      FilterChipButton(
        label = "📡 Đã gửi SOS ($totalSos)",
        selected = state.selectedHistoryFilter == HistoryFilter.SOS_SENT,
        activeBg = GlacierColors.Red600,
        onClick = { onSelectFilter(HistoryFilter.SOS_SENT) }
      )
      FilterChipButton(
        label = "✔ Tự tắt ($totalCancelled)",
        selected = state.selectedHistoryFilter == HistoryFilter.CANCELLED,
        activeBg = GlacierColors.Emerald600,
        onClick = { onSelectFilter(HistoryFilter.CANCELLED) }
      )
      val filteredCount =
        state.historyEvents.count { it.category == IncidentCategory.FILTERED }
      FilterChipButton(
        label = "🛡️ Báo giả đã lọc ($filteredCount)",
        selected = state.selectedHistoryFilter == HistoryFilter.FILTERED,
        activeBg = GlacierColors.Amber600,
        onClick = { onSelectFilter(HistoryFilter.FILTERED) }
      )
      val cardioCount = state.historyEvents.count { it.category == IncidentCategory.CARDIO }
      FilterChipButton(
        label = "❤️ Tim mạch/SpO2 ($cardioCount)",
        selected = state.selectedHistoryFilter == HistoryFilter.CARDIO,
        activeBg = GlacierColors.Rose600,
        onClick = { onSelectFilter(HistoryFilter.CARDIO) }
      )
    }

    // 4. Vertical Timeline Events List
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      
    }
  }
}

@Composable
private fun KpiStatCard(
  title: String,
  value: String,
  bgColor: Color,
  borderColor: Color,
  titleColor: Color,
  valueColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = bgColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 9.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = title,
        fontSize = 8.5.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = titleColor,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
        color = valueColor
      )
    }
  }
}

@Composable
private fun FilterChipButton(
  label: String,
  selected: Boolean,
  activeBg: Color,
  onClick: () -> Unit
) {
  TactileBox(
    onClick = onClick,
    shape = RoundedCornerShape(50),
    backgroundColor = if (selected) activeBg else Color.White.copy(alpha = 0.88f),
    border =
      BorderStroke(
        1.dp,
        if (selected) activeBg else GlacierColors.Slate200
      ),
    contentPadding = PaddingValues(horizontal = 11.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      fontSize = 10.5.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
      color = if (selected) Color.White else GlacierColors.Slate700
    )
  }
}

