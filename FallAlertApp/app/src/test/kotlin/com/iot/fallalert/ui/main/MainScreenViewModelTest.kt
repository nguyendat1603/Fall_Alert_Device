package com.iot.fallalert.ui.main

import junit.framework.TestCase.assertEquals
import org.junit.Test

class MainScreenViewModelTest {
  @Test
  fun defaultUiState_hasGlacierFallGuardDefaults() {
    val state = FallGuardUiState()
    assertEquals(AppTab.HOME, state.currentTab)
    assertEquals(16.0f, state.smvThreshold)
  }
}
