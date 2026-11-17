package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
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

    // Verify Play, Archive, and Add Note options are removed, and Music is shown
    composeTestRule.onNodeWithTag("recording_play_${wavFile.name}").assertDoesNotExist()
    composeTestRule.onNodeWithTag("action_compress_to_archive").assertDoesNotExist()
    composeTestRule.onNodeWithTag("action_save_to_notes").assertDoesNotExist()
    composeTestRule.onNodeWithTag("action_play_in_music")
      .assertIsDisplayed()
      .assertTextEquals("Music")

    composeTestRule.onNodeWithTag("action_edit_audio").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("audio_editor_screen").assertIsDisplayed()
    composeTestRule.onAllNodesWithText(wavFile.name, substring = true).onFirst().assertIsDisplayed()

    composeTestRule.onNodeWithTag("action_navigate_back").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_screen").assertIsDisplayed()
  }

  @Test
  fun deviceAudioFab_isSmallerAndPositionedToRightOfMicFab_andRecordsDeviceAudio() {
    composeTestRule.setContent {
      ToolboxTheme {
        AudioRecorderScreen(initialDirectory = testAudioDir)
      }
    }
    composeTestRule.waitForIdle()

    val micFabNode = composeTestRule.onNodeWithTag("audio_recorder_fab")
    val deviceFabNode = composeTestRule.onNodeWithTag("device_audio_recorder_fab")

    micFabNode.assertIsDisplayed()
    deviceFabNode
      .assertIsDisplayed()
      .assertContentDescriptionEquals("Record device audio")
    composeTestRule.onNodeWithTag("fab_device_audio_icon", useUnmergedTree = true).assertIsDisplayed()

    val micBounds = micFabNode.fetchSemanticsNode().boundsInRoot
    val deviceBounds = deviceFabNode.fetchSemanticsNode().boundsInRoot

    // Verify device audio FAB is smaller than main microphone FAB and positioned to its right
    assertTrue("Device audio FAB should be smaller than mic FAB", deviceBounds.width < micBounds.width)
    assertTrue("Device audio FAB should be to the right of mic FAB", deviceBounds.left >= micBounds.right)

    // Tap the device audio FAB to start recording device audio
    deviceFabNode.performClick()
    composeTestRule.waitForIdle()

    deviceFabNode.assertContentDescriptionEquals("Stop device audio recording")
    composeTestRule.onNodeWithTag("fab_device_stop_icon", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("● RECORDING DEVICE AUDIO").assertIsDisplayed()

    // Tap the device audio FAB again to stop and save the device audio recording
    deviceFabNode.performClick()
    composeTestRule.waitForIdle()

    val savedFiles = testAudioDir.listFiles()?.filter { it.isFile }.orEmpty()
    assertTrue("Expected a saved device audio recording file", savedFiles.isNotEmpty())
    val savedFile = savedFiles.first()
    assertTrue(savedFile.name.startsWith("Recording_Device_"))
    composeTestRule.onNodeWithTag("recording_item_${savedFile.name}").assertIsDisplayed()
    composeTestRule.onNodeWithText("DEVICE").assertIsDisplayed()
  }

  @Test
  fun bothAudioFab_isSmallerAndPositionedToLeftOfMicFab_andRecordsDeviceAndMicAudio() {
    composeTestRule.setContent {
      ToolboxTheme {
        AudioRecorderScreen(initialDirectory = testAudioDir)
      }
    }
    composeTestRule.waitForIdle()

    val bothFabNode = composeTestRule.onNodeWithTag("both_audio_recorder_fab")
    val micFabNode = composeTestRule.onNodeWithTag("audio_recorder_fab")

    micFabNode.assertIsDisplayed()
    bothFabNode
      .assertIsDisplayed()
      .assertContentDescriptionEquals("Record device and microphone audio")
    composeTestRule.onNodeWithTag("fab_both_audio_icon", useUnmergedTree = true).assertIsDisplayed()

    val outerDeviceIcon = composeTestRule.onNodeWithTag("fab_both_device_icon", useUnmergedTree = true)
    val innerMicIcon = composeTestRule.onNodeWithTag("fab_both_mic_icon", useUnmergedTree = true)
    outerDeviceIcon.assertIsDisplayed()
    innerMicIcon.assertIsDisplayed()

    val outerDeviceBounds = outerDeviceIcon.fetchSemanticsNode().boundsInRoot
    val innerMicBounds = innerMicIcon.fetchSemanticsNode().boundsInRoot
    assertTrue("Microphone icon should be overlaid inside the device icon horizontally", innerMicBounds.left > outerDeviceBounds.left && innerMicBounds.right < outerDeviceBounds.right)
    assertTrue("Microphone icon should be overlaid inside the device icon vertically", innerMicBounds.top > outerDeviceBounds.top && innerMicBounds.bottom < outerDeviceBounds.bottom)

    val bothBounds = bothFabNode.fetchSemanticsNode().boundsInRoot
    val micBounds = micFabNode.fetchSemanticsNode().boundsInRoot

    // Verify combined (device + mic) audio FAB is smaller than main microphone FAB and positioned to its left
    assertTrue("Combined audio FAB should be smaller than mic FAB", bothBounds.width < micBounds.width)
    assertTrue("Combined audio FAB should be to the left of mic FAB", bothBounds.right <= micBounds.left)

    // Tap the combined audio FAB to start recording both device and microphone audio
    bothFabNode.performClick()
    composeTestRule.waitForIdle()

    bothFabNode.assertContentDescriptionEquals("Stop device and microphone audio recording")
    composeTestRule.onNodeWithTag("fab_both_stop_icon", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("● RECORDING DEVICE & MIC AUDIO").assertIsDisplayed()

    // Tap the combined audio FAB again to stop and save the recording
    bothFabNode.performClick()
    composeTestRule.waitForIdle()

    val savedFiles = testAudioDir.listFiles()?.filter { it.isFile }.orEmpty()
    assertTrue("Expected a saved combined audio recording file", savedFiles.isNotEmpty())
    val savedFile = savedFiles.first()
    assertTrue(savedFile.name.startsWith("Recording_Both_"))
    composeTestRule.onNodeWithTag("recording_item_${savedFile.name}").assertIsDisplayed()
    composeTestRule.onNodeWithText("DEVICE + MIC").assertIsDisplayed()
  }
}
