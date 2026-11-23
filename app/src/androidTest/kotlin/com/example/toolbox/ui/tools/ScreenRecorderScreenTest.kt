package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.toolbox.ui.main.MainScreen
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Instrumented Compose UI tests for [ScreenRecorderScreen]. */
class ScreenRecorderScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var testRecordingsDir: File

  @Before
  fun setup() {
    ScreenRecorderEngine.stopRecordingSession(composeTestRule.activity)
    ScreenRecorderEngine.consumePendingQuickRecordRequest()
    ScreenRecorderEngine.installedAppsOverrideForTest = listOf(
      RecordableAppTarget(packageName = "com.android.chrome", label = "Chrome", accentRgb = 0x4285F4),
      RecordableAppTarget(packageName = "com.google.android.youtube", label = "YouTube", accentRgb = 0xFF0000),
      RecordableAppTarget(packageName = "com.google.android.apps.maps", label = "Maps", accentRgb = 0x34A853),
    )
    composeTestRule.activity
      .getSharedPreferences(SCREEN_RECORDER_PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()

    testRecordingsDir = File(
      composeTestRule.activity.cacheDir,
      "ScreenRecordingsTest_${System.currentTimeMillis()}",
    ).apply { mkdirs() }
  }

  @After
  fun tearDown() {
    ScreenRecorderEngine.stopRecordingSession(composeTestRule.activity)
    ScreenRecorderEngine.consumePendingQuickRecordRequest()
    ScreenRecorderEngine.installedAppsOverrideForTest = null
    if (::testRecordingsDir.isInitialized && testRecordingsDir.exists()) {
      testRecordingsDir.deleteRecursively()
    }
  }

  @Test
  fun initialScreen_displaysThreeAudioSourceButtonsTapToStartAndBottomSelectSingleAppOption() {
    composeTestRule.setContent {
      ToolboxTheme {
        ScreenRecorderScreen(
          initialDirectory = testRecordingsDir,
          bypassSystemProjectionPrompt = true,
        )
      }
    }

    composeTestRule.onNodeWithTag("screen_recorder_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Screen Recorder").assertIsDisplayed()

    // 3 recording buttons matching AudioRecorderScreen: [Device + Mic] [Primary Mic] [Device Audio]
    val bothBounds = composeTestRule.onNodeWithTag("both_screen_recorder_fab")
      .assertIsDisplayed()
      .fetchSemanticsNode()
      .boundsInRoot
    val micBounds = composeTestRule.onNodeWithTag("mic_screen_recorder_fab")
      .assertIsDisplayed()
      .fetchSemanticsNode()
      .boundsInRoot
    val deviceBounds = composeTestRule.onNodeWithTag("device_screen_recorder_fab")
      .assertIsDisplayed()
      .fetchSemanticsNode()
      .boundsInRoot

    assertTrue("Device + Mic button should be to the left of primary Mic button", bothBounds.right < micBounds.left)
    assertTrue("Primary Mic button should be to the left of Device Audio button", micBounds.right < deviceBounds.left)

    composeTestRule.onNodeWithTag("tap_to_start_recording_text")
      .assertIsDisplayed()
      .assertTextEquals("tap to start recording")
    composeTestRule.onNodeWithTag("select_single_app_button").assertIsDisplayed()
    composeTestRule.onNodeWithText("record a single app").assertIsDisplayed()
  }

  @Test
  fun singleAppOption_opensPickerAllowsSelectingAndClearingSingleApp() {
    composeTestRule.setContent {
      ToolboxTheme {
        ScreenRecorderScreen(
          initialDirectory = testRecordingsDir,
          bypassSystemProjectionPrompt = true,
        )
      }
    }

    // Open single app selector near the bottom
    composeTestRule.onNodeWithTag("select_single_app_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("single_app_selector_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Select App to Record").assertIsDisplayed()
    composeTestRule.onNodeWithTag("app_option_entire_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("app_option_com.android.chrome").assertIsDisplayed()
    composeTestRule.onNodeWithTag("app_option_com.google.android.youtube").assertIsDisplayed()

    // Filter apps by search query
    composeTestRule.onNodeWithTag("single_app_search_input").performTextInput("YouTube")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("app_option_com.google.android.youtube").assertIsDisplayed()

    // Select YouTube
    composeTestRule.onNodeWithTag("app_option_com.google.android.youtube").performClick()
    composeTestRule.waitForIdle()

    // Bottom pill now displays the selected single app and subtitle reflects it
    composeTestRule.onNodeWithTag("selected_single_app_label")
      .assertIsDisplayed()
      .assertTextContains("App: YouTube")
    composeTestRule.onNodeWithTag("selected_app_subtitle_text")
      .assertIsDisplayed()
      .assertTextEquals("Will record YouTube")

    // Clear selected app via close button on the pill -> returns to "record a single app"
    composeTestRule.onNodeWithTag("clear_single_app_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("record a single app").assertIsDisplayed()
  }

  @Test
  fun micDeviceAndBothButtons_startAndStopScreenRecordingWithSelectedAudioSource() {
    composeTestRule.setContent {
      ToolboxTheme {
        ScreenRecorderScreen(
          initialDirectory = testRecordingsDir,
          bypassSystemProjectionPrompt = true,
        )
      }
    }

    // 1. Tap Device + Mic (Both) button on the left
    composeTestRule.onNodeWithTag("both_screen_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    assertEquals(ScreenAudioSource.DEVICE_AND_MIC, ScreenRecorderEngine.activeAudioSource)
    composeTestRule.onNodeWithTag("tap_to_start_recording_text")
      .assertIsDisplayed()
      .assertTextEquals("tap to stop recording")
    composeTestRule.onNodeWithTag("screen_recorder_instruction_text")
      .assertTextEquals("Recording screen + device & mic audio")

    // Tap again to stop recording
    composeTestRule.onNodeWithTag("both_screen_recorder_fab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("recent_screen_recording_banner").assertIsDisplayed()

    // 2. Tap Device Audio button on the right
    composeTestRule.onNodeWithTag("device_screen_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    assertEquals(ScreenAudioSource.DEVICE_AUDIO, ScreenRecorderEngine.activeAudioSource)
    composeTestRule.onNodeWithTag("screen_recorder_instruction_text")
      .assertTextEquals("Recording screen + device audio")

    // Stop via Device Audio button
    composeTestRule.onNodeWithTag("device_screen_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    // 3. Tap primary Mic button in the center
    composeTestRule.onNodeWithTag("mic_screen_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    assertEquals(ScreenAudioSource.MICROPHONE, ScreenRecorderEngine.activeAudioSource)
    composeTestRule.onNodeWithTag("screen_recorder_instruction_text")
      .assertTextEquals("Recording screen + microphone audio")

    // Stop via primary Mic button
    composeTestRule.onNodeWithTag("mic_screen_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tap_to_start_recording_text")
      .assertIsDisplayed()
      .assertTextEquals("tap to start recording")
    composeTestRule.onNodeWithTag("recent_screen_recording_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("edit_recent_recording_button").assertIsDisplayed()
  }

  @Test
  fun standaloneMode_rendersScreenRecorderTool() {
    composeTestRule.setContent {
      MainScreen(standaloneTool = Tool.SCREEN_RECORDER)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("standalone_tool_screen_SCREEN_RECORDER").assertIsDisplayed()
    composeTestRule.onNodeWithTag("screen_recorder_screen").assertIsDisplayed()
  }
}
