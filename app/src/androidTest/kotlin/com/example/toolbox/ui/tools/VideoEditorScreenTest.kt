package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.toolbox.ui.main.MainScreen
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import org.junit.Rule
import org.junit.Test

/** Instrumented Compose UI tests for [VideoEditorScreen]. */
class VideoEditorScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun emptyState_displaysOpenActionsAndSampleVideoButton() {
    composeTestRule.setContent {
      ToolboxTheme {
        VideoEditorScreen()
      }
    }

    composeTestRule.onNodeWithTag("video_editor_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("open_video_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tap_to_open_file_text").assertIsDisplayed()
    composeTestRule.onNodeWithText("tap to open file").assertIsDisplayed()
    composeTestRule.onNodeWithTag("open_sample_video_button").assertIsDisplayed()
    composeTestRule.onNodeWithText("or try sample video").assertIsDisplayed()
  }

  @Test
  fun loadedState_switchesModesSupportsUndoRedoAndOpensExportDialog() {
    val fakeMeta = VideoMetadata(
      file = File("/storage/emulated/0/Movies/test_clip.mp4"),
      durationMs = 10_000L,
      width = 1920,
      height = 1080,
      frameRate = 30f,
      hasAudio = true,
      sizeBytes = 4_096_000L,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        VideoEditorScreen(initialMetadataForPreview = fakeMeta)
      }
    }

    // Header, preview viewport, strategy badge, timeline, and mode selector are visible
    composeTestRule.onNodeWithTag("video_preview_viewport").assertIsDisplayed()
    composeTestRule.onNodeWithTag("strategy_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("video_filmstrip_timeline").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mode_selector_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_undo_button").assertIsNotEnabled()

    // Switch to Rotate tab and rotate 90 CW -> Undo becomes enabled & strategy switches to Hardware H.264
    composeTestRule.onNodeWithTag("mode_tab_transform").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("rotate_cw_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("editor_undo_button").assertIsEnabled()
    composeTestRule.onNodeWithText("Orientation: 90°").assertIsDisplayed()

    // Undo restores 0°
    composeTestRule.onNodeWithTag("editor_undo_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Orientation: 0°").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_redo_button").assertIsEnabled()

    // Switch through Crop, Speed, and Filters tabs
    composeTestRule.onNodeWithTag("mode_tab_crop").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("crop_preset_ratio_16_9").assertIsDisplayed()

    composeTestRule.onNodeWithTag("mode_tab_speed").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("mute_audio_chip").assertIsDisplayed()

    composeTestRule.onNodeWithTag("mode_tab_adjust").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("filter_preset_warm").performClick()
    composeTestRule.waitForIdle()

    // Open Export Dialog
    composeTestRule.onNodeWithTag("editor_export_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Export Edited Media").assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_filename_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirm_export_button").assertIsDisplayed()
  }

  @Test
  fun standaloneMode_rendersVideoEditorTool() {
    composeTestRule.setContent {
      MainScreen(standaloneTool = Tool.VIDEO_EDITOR)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("standalone_tool_screen_VIDEO_EDITOR").assertIsDisplayed()
    composeTestRule.onNodeWithTag("video_editor_screen").assertIsDisplayed()
  }
}
