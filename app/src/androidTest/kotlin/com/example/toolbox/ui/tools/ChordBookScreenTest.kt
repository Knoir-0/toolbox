package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Rule
import org.junit.Test

class ChordBookScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun reverseScaleSearch_switchesModeSelectsNotesAndJumpsToScale() {
    composeTestRule.setContent {
      ToolboxTheme {
        ChordBookScreen()
      }
    }

    // 1. Navigate to Reverse tab
    composeTestRule.onNodeWithTag("chord_book_tab_REVERSE").performClick()
    composeTestRule.waitForIdle()

    // 2. Switch to Reverse Scale Search mode
    composeTestRule.onNodeWithTag("reverse_mode_scales").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    // Verify Reverse Scale Search controls and pitch class selector are displayed
    composeTestRule.onNodeWithTag("reverse_scale_pitch_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reverse_scale_matches_header").assertIsDisplayed()

    // 3. Clear selections, then select C(0), D(2), E(4), G(7), A(9) (C Major Pentatonic)
    composeTestRule.onNodeWithTag("reverse_clear_button").performClick()
    composeTestRule.waitForIdle()

    for (pc in listOf(0, 2, 4, 7, 9)) {
      composeTestRule.onNodeWithTag("reverse_scale_pitch_$pc").performClick()
    }
    composeTestRule.waitForIdle()

    // Verify top match card is displayed and jump to Scale view
    composeTestRule.onNodeWithTag("reverse_scale_match_0").assertIsDisplayed()
    composeTestRule.onNodeWithText("C Major Pentatonic").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reverse_scale_view_button_0").performClick()
    composeTestRule.waitForIdle()

    // 4. Verify we landed on the Scales & Key tab with C Major Pentatonic active,
    // and can jump back to Reverse Scale Search via the shortcut button
    composeTestRule.onNodeWithTag("active_scale_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scales_reverse_search_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("reverse_scale_pitch_0").assertIsDisplayed()
  }
}
