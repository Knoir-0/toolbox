package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Rule
import org.junit.Test

class DecibelMeterScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun decibelMeterScreen_displaysGaugeStatsAndAllowsPauseAndReset() {
    composeTestRule.setContent {
      ToolboxTheme {
        DecibelMeterScreen(initialDecibels = 52.0f)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("decibel_meter_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Decibel Meter").assertIsDisplayed()
    composeTestRule.onNodeWithTag("decibel_gauge_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("decibel_current_value").assertTextEquals("52.0")
    composeTestRule.onNodeWithTag("decibel_category_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("decibel_min_value").assertIsDisplayed()
    composeTestRule.onNodeWithTag("decibel_avg_value").assertIsDisplayed()
    composeTestRule.onNodeWithTag("decibel_max_value").assertIsDisplayed()

    // Pause and resume
    composeTestRule.onNodeWithTag("decibel_pause_resume_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("PAUSED").assertIsDisplayed()

    // Reset statistics resets current/min/max to 0.0
    composeTestRule.onNodeWithTag("decibel_reset_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("decibel_current_value").assertTextEquals("0.0")
    composeTestRule.onNodeWithTag("decibel_min_value").assertTextEquals("0.0 dB")
    composeTestRule.onNodeWithTag("decibel_max_value").assertTextEquals("0.0 dB")
  }
}
