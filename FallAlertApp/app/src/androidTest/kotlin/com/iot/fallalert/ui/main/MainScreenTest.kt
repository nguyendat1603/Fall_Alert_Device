package com.iot.fallalert.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.iot.fallalert.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
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

  @Test
  fun appTitle_exists() {
    composeTestRule.onNodeWithText("FallGuard").assertExists()
  }
}
