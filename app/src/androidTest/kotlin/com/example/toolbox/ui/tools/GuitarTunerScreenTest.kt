package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Rule
import org.junit.Test

class GuitarTunerScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun initialScreen_whenNoNoteDetected_doesNotDisplayNote() {
    composeTestRule.setContent {
      ToolboxTheme {
        GuitarTunerScreen()
      }
    }

    // When no note is detected, the detected note readout does not exist
    composeTestRule.onNodeWithTag("detected_note").assertDoesNotExist()

    // Header badge indicates no note is detected
    composeTestRule.onNodeWithText("No note detected").assertIsDisplayed()

    // Status indicates listening rather than in tune
    composeTestRule.onNodeWithText("LISTENING").assertIsDisplayed()
    composeTestRule.onNodeWithText("IN TUNE").assertDoesNotExist()
  }
}
