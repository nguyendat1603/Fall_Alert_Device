package com.iot.fallalert.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.iot.fallalert.ui.theme.FallAlertAppTheme
import com.iot.fallalert.ui.theme.GlacierColors

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit = {},
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel() }
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  FallGuardGlacierContent(
    state = uiState,
    onSelectTab = viewModel::switchTab,
    onSimulateScenario = viewModel::simulateScenario,
    onCancelFallAlarm = viewModel::cancelFallAlarm,
    onCallEmergencySos = viewModel::callEmergencySos,
    onSwitchWaveform = viewModel::switchWaveform,
    onSelectHistoryFilter = viewModel::filterHistory,
    onResetHistoryFilter = viewModel::resetHistoryFilter,
    onSmvThresholdChange = viewModel::updateSmvThreshold,
    onSelectCountdownOption = viewModel::selectCountdownOption,
    onToggleDualLayer = viewModel::toggleDualLayer,
    onToggleAutoCall115 = viewModel::toggleAutoCall115,
    onToggleAttachGps = viewModel::toggleAttachGps,
    onTriggerBuzzerTest = viewModel::triggerHardwareBuzzerTest,
    onSaveConfig = viewModel::saveHardwareConfiguration,
    onShowToast = viewModel::showToast,
    modifier = modifier
  )
}

@Composable
fun FallGuardGlacierContent(
  state: FallGuardUiState,
  onSelectTab: (AppTab) -> Unit,
  onSimulateScenario: (String) -> Unit,
  onCancelFallAlarm: () -> Unit,
  onCallEmergencySos: () -> Unit,
  onSwitchWaveform: (WaveformMode) -> Unit,
  onSelectHistoryFilter: (HistoryFilter) -> Unit,
  onResetHistoryFilter: () -> Unit,
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
  val scrollState = rememberScrollState()

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors =
              listOf(
                GlacierColors.Glacier100,
                GlacierColors.Glacier50,
                GlacierColors.Slate50
              )
          )
        )
        .statusBarsPadding()
        .navigationBarsPadding()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Persistent Top App Header
      FallGuardTopHeader(systemClock = state.systemClock)

      // Scrollable Active Tab Content
      Column(
        modifier =
          Modifier.weight(1f)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp)
      ) {
        when (state.currentTab) {
          AppTab.HOME -> {
            HomeTabScreen(
              state = state,
              onCancelFallAlarm = onCancelFallAlarm,
              onCallEmergencySos = onCallEmergencySos,
              onSwitchWaveform = onSwitchWaveform
            )
          }
          AppTab.HISTORY -> {
            HistoryTabScreen(
              state = state,
              onSelectFilter = onSelectHistoryFilter,
              onResetFilter = onResetHistoryFilter,
              onExportPdf = { onShowToast("📄 Đang xuất file báo cáo lịch sử CSV/PDF...") }
            )
          }
          AppTab.SETTINGS -> {
            SettingsTabScreen(
              state = state,
              onSmvThresholdChange = onSmvThresholdChange,
              onSelectCountdownOption = onSelectCountdownOption,
              onToggleDualLayer = onToggleDualLayer,
              onToggleAutoCall115 = onToggleAutoCall115,
              onToggleAttachGps = onToggleAttachGps,
              onTriggerBuzzerTest = onTriggerBuzzerTest,
              onSaveConfig = onSaveConfig,
              onShowToast = onShowToast
            )
          }
        }

        // Extra bottom space so floating simulator dock & bottom nav don't cover content
        Spacer(modifier = Modifier.height(145.dp))
      }
    }

    // Bottom Docked Simulator + Navigation Bar
    Column(
      modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      SimulatorFloatingDock(
        currentScenario = state.scenarioMode,
        onSelectScenario = onSimulateScenario,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      GlacierBottomNavBar(
        currentTab = state.currentTab,
        historyCount = state.historyEvents.size,
        onSelectTab = onSelectTab
      )
    }

    // Top Animated Toast Notification Overlay
    GlacierToastOverlay(
      toastMessage = state.toastMessage,
      modifier = Modifier.align(Alignment.TopCenter)
    )
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
  FallAlertAppTheme {
    FallGuardGlacierContent(
      state = FallGuardUiState(),
      onSelectTab = {},
      onSimulateScenario = {},
      onCancelFallAlarm = {},
      onCallEmergencySos = {},
      onSwitchWaveform = {},
      onSelectHistoryFilter = {},
      onResetHistoryFilter = {},
      onSmvThresholdChange = {},
      onSelectCountdownOption = {},
      onToggleDualLayer = {},
      onToggleAutoCall115 = {},
      onToggleAttachGps = {},
      onTriggerBuzzerTest = {},
      onSaveConfig = {},
      onShowToast = {}
    )
  }
}
