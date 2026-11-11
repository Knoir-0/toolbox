package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AudioRecorderScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var testAudioDir: File

  @Before
  fun setup() {
    composeTestRule.activity
      .getSharedPreferences(AUDIO_RECORDER_PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()

    testAudioDir = File(composeTestRule.activity.cacheDir, "AudioTest_${System.currentTimeMillis()}")
    testAudioDir.mkdirs()
  }

  @After
  fun tearDown() {
    if (::testAudioDir.isInitialized && testAudioDir.exists()) {
      testAudioDir.deleteRecursively()
    }
  }

  @Test
  fun micFab_startsAndStopsRecording_savesTimestampedFile_andOpensMiniPlayerWithLeftPlayPauseToggle() {
    composeTestRule.setContent {
      ToolboxTheme {
        AudioRecorderScreen(initialDirectory = testAudioDir)
      }
    }

    // 1. Microphone FAB is displayed in the middle of the screen in idle state
    composeTestRule.onNodeWithTag("audio_recorder_fab")
      .assertIsDisplayed()
      .assertContentDescriptionEquals("Start recording")
    composeTestRule.onNodeWithTag("fab_mic_icon", useUnmergedTree = true).assertIsDisplayed()

    // 2. Tap the FAB to start recording -> transitions into a Stop button
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("audio_recorder_fab")
      .assertContentDescriptionEquals("Stop recording")
    composeTestRule.onNodeWithTag("fab_stop_icon", useUnmergedTree = true).assertIsDisplayed()

    // 3. Tap the Stop button -> saves a timestamped audio file and displays it in the list below
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    val savedFiles = testAudioDir.listFiles()?.filter { it.isFile }.orEmpty()
    assertTrue("Expected a saved recording file", savedFiles.isNotEmpty())
    val savedFile = savedFiles.first()
    assertTrue(savedFile.name.startsWith("Recording_"))
    assertTrue(savedFile.name.endsWith(".m4a"))

    val recordingNode = composeTestRule.onNodeWithTag("recording_item_${savedFile.name}")
    recordingNode.assertIsDisplayed()

    // 4. Tap the recording -> pops up miniature player modal with play/pause toggle to the left of tracking slider
    recordingNode.performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("mini_player_modal").assertIsDisplayed()
    val playPauseNode = composeTestRule.onNodeWithTag("mini_player_play_pause_button")
    val sliderNode = composeTestRule.onNodeWithTag("mini_player_tracking_slider")

    playPauseNode.assertIsDisplayed()
    sliderNode.assertIsDisplayed()

    // Verify play/pause toggle is positioned to the left of the tracking slider
    val playPauseBounds = playPauseNode.fetchSemanticsNode().boundsInRoot
    val sliderBounds = sliderNode.fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Play/Pause toggle should be to the left of the tracking slider",
      playPauseBounds.right <= sliderBounds.left + 4f,
    )

    // Toggling play/pause switches between Pause and Play states
    playPauseNode.performClick()
    composeTestRule.waitForIdle()
    playPauseNode.assertContentDescriptionEquals("Play")
  }

  @Test
  fun formatSelector_andFolderSelector_allowSwappingFormatAndChangingFolder() {
    composeTestRule.setContent {
      ToolboxTheme {
        AudioRecorderScreen(initialDirectory = testAudioDir)
      }
    }

    // Default format is M4A; open format dropdown and swap to WAV
    composeTestRule.onNodeWithTag("format_selector_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("format_option_WAV").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    // Record a WAV file
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    val wavFiles = testAudioDir.listFiles()?.filter { it.extension == "wav" }.orEmpty()
    assertTrue("Expected a saved .wav recording file", wavFiles.isNotEmpty())

    // Open folder selector dialog via folder pill
    composeTestRule.onNodeWithTag("recordings_folder_pill").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("change_folder_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folder_preset_Recordings").performClick()
    composeTestRule.onNodeWithTag("confirm_folder_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("/Recordings").assertIsDisplayed()
  }

  @Test
  fun recordingContextMenu_editOptionOpensAudioEditorDirectly_andNavigatesBack() {
    val wavFile = File(testAudioDir, "Recording_Test.wav")
    AudioEditorEngine.writeWavFile(
      buffer = AudioEditorEngine.createSampleAudioBuffer(durationMs = 1500L),
      outputFile = wavFile,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        AudioRecorderScreen(initialDirectory = testAudioDir)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("recording_item_${wavFile.name}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recording_menu_${wavFile.name}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("action_edit_audio").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("audio_editor_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText(wavFile.name, substring = true).assertIsDisplayed()

    composeTestRule.onNodeWithTag("action_navigate_back").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_screen").assertIsDisplayed()
  }
}
