package com.example.toolbox.ui.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
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
  fun scopedStorageAccess_locksPillsDisablesRecorder_andPromptsPermissionFlowOnLockTap() {
    var systemPromptFolder: AudioRecorderDefaultFolder? = null
    var pendingSystemGrantCallback: ((Boolean) -> Unit)? = null

    composeTestRule.setContent {
      ToolboxTheme {
        AudioRecorderScreen(
          initialDirectory = testAudioDir,
          onRequestFolderSystemAccess = { folder, onResult ->
            systemPromptFolder = folder
            pendingSystemGrantCallback = onResult
          },
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Initially all navigation pills show a lock icon and no folder has been granted access
    composeTestRule.onNodeWithTag("folder_lock_icon_Audio").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folder_lock_icon_Recordings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folder_lock_icon_Download").assertIsDisplayed()

    // 2. Recorder is entirely disabled and bottom message reflects that while disabled
    composeTestRule.onNodeWithTag("audio_recorder_fab").assertIsDisplayed().assertIsNotEnabled()
    composeTestRule.onNodeWithTag("device_audio_recorder_fab").assertIsDisplayed().assertIsNotEnabled()
    composeTestRule.onNodeWithTag("both_audio_recorder_fab").assertIsDisplayed().assertIsNotEnabled()
    composeTestRule.onNodeWithTag("format_selector_button").assertIsDisplayed().assertIsNotEnabled()
    composeTestRule.onNodeWithText("RECORDER DISABLED").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recorder_instruction_text")
      .assertIsDisplayed()
      .assertTextEquals("Recorder disabled • Grant access to at least one folder above to record")

    // Tapping the disabled mic FAB does not start recording
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_fab").assertContentDescriptionEquals("Start recording")

    // 3. Tapping the lock icon on Audio displays the view asking for permission to access files in /Audio
    composeTestRule.onNodeWithTag("folder_lock_icon_Audio").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("folder_permission_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folder_permission_title")
      .assertIsDisplayed()
      .assertTextEquals("Allow access to files in /Audio?")
    composeTestRule.onNodeWithTag("allow_folder_permission_button").assertIsDisplayed()

    // 4. Tapping the lock icon on Recordings updates the permission view for /Recordings
    composeTestRule.onNodeWithTag("folder_lock_icon_Recordings").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("folder_permission_title")
      .assertIsDisplayed()
      .assertTextEquals("Allow access to files in /Recordings?")

    // Switch back to Audio lock icon and tap Allow -> system prompt for /Audio pops up
    composeTestRule.onNodeWithTag("folder_lock_icon_Audio").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("allow_folder_permission_button").performClick()
    composeTestRule.waitForIdle()

    assertEquals(AudioRecorderDefaultFolder.AUDIO, systemPromptFolder)
    assertNotNull(pendingSystemGrantCallback)

    // Verify system intent targets ACTION_OPEN_DOCUMENT_TREE with initial URI for Audio
    val systemIntent = AudioRecorderEngine.createFolderAccessIntent(
      context = composeTestRule.activity,
      folder = AudioRecorderDefaultFolder.AUDIO,
      userRoot = testAudioDir.parentFile ?: testAudioDir,
    )
    assertEquals(Intent.ACTION_OPEN_DOCUMENT_TREE, systemIntent.action)
    @Suppress("DEPRECATION")
    val initialUri = systemIntent.getParcelableExtra<Uri>(DocumentsContract.EXTRA_INITIAL_URI)
    assertNotNull(initialUri)
    assertTrue(initialUri.toString().contains("Audio"))

    // 5. Completing system prompt grant unlocks Audio, enables the recorder, and restores bottom message
    composeTestRule.runOnUiThread {
      pendingSystemGrantCallback?.invoke(true)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("folder_unlocked_icon_Audio").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folder_lock_icon_Recordings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folder_lock_icon_Download").assertIsDisplayed()

    composeTestRule.onNodeWithTag("audio_recorder_fab").assertIsEnabled()
    composeTestRule.onNodeWithTag("device_audio_recorder_fab").assertIsEnabled()
    composeTestRule.onNodeWithTag("both_audio_recorder_fab").assertIsEnabled()
    composeTestRule.onNodeWithTag("format_selector_button").assertIsEnabled()
    composeTestRule.onNodeWithText("READY TO RECORD").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recorder_instruction_text")
      .assertIsDisplayed()
      .assertTextEquals("Tap mic, phone, or combined button to record (M4A (AAC))")

    // 6. Selecting a locked folder (Recordings) disables the recorder and restores the disabled bottom message
    composeTestRule.onNodeWithTag("folder_pill_Recordings").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("folder_permission_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("audio_recorder_fab").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("device_audio_recorder_fab").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("both_audio_recorder_fab").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("format_selector_button").assertIsNotEnabled()
    composeTestRule.onNodeWithText("RECORDER DISABLED").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recorder_instruction_text")
      .assertIsDisplayed()
      .assertTextEquals("Recorder disabled • Grant access to at least one folder above to record")

    // Switching back to unlocked Audio re-enables the recorder
    composeTestRule.onNodeWithTag("folder_pill_Audio").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_fab").assertIsEnabled()
    composeTestRule.onNodeWithTag("recorder_instruction_text")
      .assertIsDisplayed()
      .assertTextEquals("Tap mic, phone, or combined button to record (M4A (AAC))")
  }

  @Test
  fun addingNewRecording_scrollsToNewlyAddedRecordingInList() {
    val baseTimeMs = 1_700_000_000_000L
    for (i in 1..20) {
      val file = File(testAudioDir, String.format("Existing_%02d.wav", i))
      AudioRecorderEngine.writeFallbackAudioFile(file, durationMs = 1000L)
      file.setLastModified(baseTimeMs + i * 1000L)
    }

    composeTestRule.setContent {
      ToolboxTheme {
        AudioRecorderScreen(
          initialDirectory = testAudioDir,
          initialGrantedFolders = setOf(AudioRecorderDefaultFolder.AUDIO),
        )
      }
    }
    composeTestRule.waitForIdle()

    // Switch sort order to Date (Oldest first) so new recordings are added at the very bottom of the 20+ item list
    composeTestRule.onNodeWithTag("audio_recorder_filter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_sort_option_date_asc").performClick()
    composeTestRule.waitForIdle()

    // Record a new audio file -> should automatically scroll the list down to the newly added recording at the bottom
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    val newRecording = testAudioDir.listFiles()
      ?.filter { it.isFile && it.name.startsWith("Recording_") }
      ?.maxByOrNull { it.lastModified() }
    assertNotNull("Expected a newly saved recording file", newRecording)
    composeTestRule.onNodeWithTag("recording_item_${newRecording!!.name}").assertIsDisplayed()
  }

  @Test
  fun addCustomFolderButton_andDragAndDropPillReordering_workCorrectly() {
    val customDir = File(testAudioDir.parentFile, "VoiceMemos")
    customDir.mkdirs()
    try {
      var addFolderCallback: ((AudioRecorderFolder?) -> Unit)? = null

      composeTestRule.setContent {
        ToolboxTheme {
          AudioRecorderScreen(
            initialDirectory = testAudioDir,
            onRequestAddCustomFolder = { onAdded ->
              addFolderCallback = onAdded
            },
          )
        }
      }
      composeTestRule.waitForIdle()

      // 1. Add folder icon button is displayed in the floating section to the left of the filter button
      val addButtonBounds = composeTestRule.onNodeWithTag("audio_recorder_add_folder_button")
        .assertIsDisplayed()
        .fetchSemanticsNode()
        .boundsInRoot
      val filterButtonBounds = composeTestRule.onNodeWithTag("audio_recorder_filter_button")
        .assertIsDisplayed()
        .fetchSemanticsNode()
        .boundsInRoot
      assertTrue(
        "Add folder button should be in the floating section to the left of the filter button",
        addButtonBounds.right <= filterButtonBounds.left + 4f,
      )

      // Verify Android scoped storage intent for adding custom folders
      val addCustomIntent = AudioRecorderEngine.createAddCustomFolderIntent(
        context = composeTestRule.activity,
        userRoot = testAudioDir.parentFile ?: testAudioDir,
      )
      assertEquals(Intent.ACTION_OPEN_DOCUMENT_TREE, addCustomIntent.action)

      // 2. Tap Add Folder button -> triggers scoped storage folder selection and adds unlocked custom folder pill
      composeTestRule.onNodeWithTag("audio_recorder_add_folder_button").performClick()
      composeTestRule.waitForIdle()
      assertNotNull(addFolderCallback)

      composeTestRule.runOnUiThread {
        addFolderCallback?.invoke(
          AudioRecorderFolder(
            folderName = "VoiceMemos",
            displayPath = "/VoiceMemos",
            customAbsolutePath = customDir.absolutePath,
          )
        )
      }
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("folder_pill_VoiceMemos").assertIsDisplayed()
      composeTestRule.onNodeWithTag("folder_unlocked_icon_VoiceMemos").assertIsDisplayed()
      composeTestRule.onNodeWithTag("folder_delete_icon_VoiceMemos").assertIsDisplayed()
      composeTestRule.onNodeWithTag("folder_delete_icon_Audio").assertDoesNotExist()
      composeTestRule.onNodeWithTag("folder_delete_icon_Recordings").assertDoesNotExist()
      composeTestRule.onNodeWithTag("folder_delete_icon_Download").assertDoesNotExist()
      composeTestRule.onNodeWithText("No recordings in /VoiceMemos").assertIsDisplayed()
      composeTestRule.onNodeWithTag("audio_recorder_fab").assertIsEnabled()

      val voiceMemosUnlockBounds = composeTestRule.onNodeWithTag("folder_unlocked_icon_VoiceMemos")
        .fetchSemanticsNode()
        .boundsInRoot
      val voiceMemosDeleteBounds = composeTestRule.onNodeWithTag("folder_delete_icon_VoiceMemos")
        .fetchSemanticsNode()
        .boundsInRoot
      assertTrue(
        "Custom folder delete x icon (${voiceMemosDeleteBounds.left}) should be on the right side of the pill (${voiceMemosUnlockBounds.right})",
        voiceMemosUnlockBounds.right < voiceMemosDeleteBounds.left,
      )

      // Tapping the x icon on the right side of the custom folder pill deletes the custom folder pill
      composeTestRule.onNodeWithTag("folder_delete_icon_VoiceMemos").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("folder_pill_VoiceMemos").assertDoesNotExist()
      composeTestRule.onNodeWithTag("locked_storage_state").assertIsDisplayed()

      // 3. Verify initial pill order (Audio is to the left of Recordings) and drag Recordings to the left of Audio
      val initialAudioBounds = composeTestRule.onNodeWithTag("folder_pill_Audio")
        .fetchSemanticsNode()
        .boundsInRoot
      val initialRecordingsBounds = composeTestRule.onNodeWithTag("folder_pill_Recordings")
        .fetchSemanticsNode()
        .boundsInRoot
      assertTrue(initialAudioBounds.left < initialRecordingsBounds.left)

      val barBounds = composeTestRule.onNodeWithTag("audio_recorder_pill_bar")
        .fetchSemanticsNode()
        .boundsInRoot
      val startX = initialRecordingsBounds.center.x - barBounds.left
      val startY = initialRecordingsBounds.center.y - barBounds.top
      val endX = (initialAudioBounds.left - barBounds.left).coerceAtLeast(10f)

      composeTestRule.onNodeWithTag("audio_recorder_pill_bar").performTouchInput {
        down(Offset(startX, startY))
        advanceEventTime(650L)
        moveTo(Offset(endX, startY))
        advanceEventTime(100L)
        up()
      }
      composeTestRule.waitForIdle()

      val reorderedRecordingsBounds = composeTestRule.onNodeWithTag("folder_pill_Recordings")
        .fetchSemanticsNode()
        .boundsInRoot
      val reorderedAudioBounds = composeTestRule.onNodeWithTag("folder_pill_Audio")
        .fetchSemanticsNode()
        .boundsInRoot
      assertTrue(
        "Recordings pill should now be positioned to the left of Audio pill after drag-and-drop reorder",
        reorderedRecordingsBounds.left < reorderedAudioBounds.left,
      )
    } finally {
      customDir.deleteRecursively()
    }
  }

  @Test
  fun micFab_startsAndStopsRecording_savesTimestampedFile_andOpensMiniPlayerWithLeftPlayPauseToggle() {
    composeTestRule.setContent {
      ToolboxTheme {
        AudioRecorderScreen(
          initialDirectory = testAudioDir,
          initialGrantedFolders = AudioRecorderDefaultFolder.entries.toSet(),
        )
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
  fun formatSelector_folderPills_filterButton_andTopListLayout_workCorrectly() {
    composeTestRule.setContent {
      ToolboxTheme {
        AudioRecorderScreen(
          initialDirectory = testAudioDir,
          initialGrantedFolders = AudioRecorderDefaultFolder.entries.toSet(),
        )
      }
    }

    // Verify recording list view is positioned at the top, above the recording buttons
    val recordingsSectionBounds = composeTestRule.onNodeWithTag("recordings_section")
      .assertIsDisplayed()
      .fetchSemanticsNode()
      .boundsInRoot
    val recorderButtonsBounds = composeTestRule.onNodeWithTag("recorder_center_stage")
      .assertIsDisplayed()
      .fetchSemanticsNode()
      .boundsInRoot
    assertTrue(
      "Recording list view should be above the recording buttons",
      recordingsSectionBounds.bottom <= recorderButtonsBounds.top + 4f,
    )

    // Verify toolbar folder selector button and modal do not exist
    composeTestRule.onNodeWithTag("change_folder_top_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("change_folder_dialog").assertDoesNotExist()

    // Verify format selector is positioned inside the recorder view above the "Tap ..." instruction text
    val micFabBounds = composeTestRule.onNodeWithTag("audio_recorder_fab")
      .assertIsDisplayed()
      .fetchSemanticsNode()
      .boundsInRoot
    val formatSelectorBounds = composeTestRule.onNodeWithTag("format_selector_button")
      .assertIsDisplayed()
      .fetchSemanticsNode()
      .boundsInRoot
    val instructionTextBounds = composeTestRule.onNodeWithTag("recorder_instruction_text")
      .assertIsDisplayed()
      .fetchSemanticsNode()
      .boundsInRoot
    assertTrue(
      "Format selector should be inside the recorder view below the recording FABs",
      formatSelectorBounds.top >= micFabBounds.bottom - 4f,
    )
    assertTrue(
      "Format selector should be above the Tap ... instruction text",
      formatSelectorBounds.bottom <= instructionTextBounds.top + 4f,
    )

    // Verify default folder pills (Audio, Recordings, Download) and filter button are displayed
    composeTestRule.onNodeWithTag("folder_pill_Audio").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folder_pill_Recordings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folder_pill_Download").assertIsDisplayed()
    composeTestRule.onNodeWithTag("audio_recorder_filter_button").assertIsDisplayed()

    // Default format is M4A; open format dropdown and swap to WAV
    composeTestRule.onNodeWithTag("format_selector_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("format_option_WAV").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    // Record a WAV file in the default Audio folder
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    val wavFiles = testAudioDir.listFiles()?.filter { it.extension == "wav" }.orEmpty()
    assertTrue("Expected a saved .wav recording file", wavFiles.isNotEmpty())

    // Switch to Recordings folder using the pill selector and verify new recordings save there by default
    composeTestRule.onNodeWithTag("folder_pill_Recordings").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("No recordings in /Recordings").assertIsDisplayed()

    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_fab").performClick()
    composeTestRule.waitForIdle()

    val recordingsFolderFiles = File(testAudioDir.parentFile, "Recordings").listFiles()?.filter { it.isFile }.orEmpty()
    assertTrue("Expected recording to be saved into the selected Recordings folder", recordingsFolderFiles.isNotEmpty())
    File(testAudioDir.parentFile, "Recordings").deleteRecursively()

    composeTestRule.onNodeWithTag("folder_pill_Download").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("No recordings in /Download").assertIsDisplayed()

    composeTestRule.onNodeWithTag("folder_pill_Audio").performClick()
    composeTestRule.waitForIdle()

    // Open filter menu via filter button
    composeTestRule.onNodeWithTag("audio_recorder_filter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_recorder_filter_menu").assertIsDisplayed()

    // Dismiss filter menu and test toolbar search in the currently selected folder
    composeTestRule.onNodeWithTag("action_toggle_search").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("toolbar_search_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("audio_recorder_search_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("action_close_search").performClick()
    composeTestRule.waitForIdle()
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
        AudioRecorderScreen(
          initialDirectory = testAudioDir,
          initialGrantedFolders = AudioRecorderDefaultFolder.entries.toSet(),
        )
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
        AudioRecorderScreen(
          initialDirectory = testAudioDir,
          initialGrantedFolders = AudioRecorderDefaultFolder.entries.toSet(),
        )
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
        AudioRecorderScreen(
          initialDirectory = testAudioDir,
          initialGrantedFolders = AudioRecorderDefaultFolder.entries.toSet(),
        )
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
