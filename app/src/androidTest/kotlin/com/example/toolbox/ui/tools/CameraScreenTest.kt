package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.nio.file.Files
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CameraScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var tempCameraDir: File

  @Before
  fun setUp() {
    composeTestRule.activity
      .getSharedPreferences(CAMERA_PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
    tempCameraDir = Files.createTempDirectory("camera_ui_test").toFile()
  }

  @After
  fun tearDown() {
    tempCameraDir.deleteRecursively()
  }

  @Test
  fun cameraScreen_capturesPhotosAndVideos_andSupportsControls() {
    composeTestRule.setContent {
      ToolboxTheme {
        CameraScreen(initialDirectory = tempCameraDir)
      }
    }
    composeTestRule.waitForIdle()

    // Verify main camera screen, full-viewport viewfinder, and translucent overlay controls exist
    composeTestRule.onNodeWithTag("camera_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_viewfinder_stage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_viewfinder_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_top_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_bottom_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_shutter_button").assertIsDisplayed()

    // Verify viewfinder fills the entire stage viewport by default (Full aspect ratio)
    val stageBounds = composeTestRule.onNodeWithTag("camera_viewfinder_stage").fetchSemanticsNode().boundsInRoot
    val fullBoxBounds = composeTestRule.onNodeWithTag("camera_viewfinder_box").fetchSemanticsNode().boundsInRoot
    assertEquals(stageBounds.width, fullBoxBounds.width, 1f)
    assertEquals(stageBounds.height, fullBoxBounds.height, 1f)

    // Cycle aspect ratio (Full -> 4:3) and verify the viewport shrinks to create black bars
    composeTestRule.onNodeWithTag("camera_aspect_ratio_button").performClick()
    composeTestRule.waitForIdle()
    val shrunkBoxBounds = composeTestRule.onNodeWithTag("camera_viewfinder_box").fetchSemanticsNode().boundsInRoot
    assertTrue(
      shrunkBoxBounds.height < stageBounds.height - 1f ||
        shrunkBoxBounds.width < stageBounds.width - 1f
    )

    // Take a photo
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()

    val mediaAfterPhoto = CameraEngine.listCapturedMedia(tempCameraDir)
    assertEquals(1, mediaAfterPhoto.size)
    assertTrue(mediaAfterPhoto.first().name.startsWith("IMG_"))

    // Switch to 2x zoom preset
    composeTestRule.onNodeWithTag("zoom_preset_2x").performClick()
    composeTestRule.waitForIdle()

    // Toggle grid overlay (3x3)
    composeTestRule.onNodeWithTag("camera_grid_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_grid_overlay_canvas").assertIsDisplayed()

    // Toggle color filters bar and select Mono
    composeTestRule.onNodeWithTag("toggle_filters_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("filter_chip_MONO").performClick()
    composeTestRule.waitForIdle()

    // Flip camera lens (Back -> Front)
    composeTestRule.onNodeWithTag("camera_flip_lens_button").performClick()
    composeTestRule.waitForIdle()

    // Switch to Video mode and record a video clip
    composeTestRule.onNodeWithTag("mode_tab_VIDEO").performClick()
    composeTestRule.waitForIdle()

    // Start recording
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("video_recording_badge").assertIsDisplayed()

    // Stop recording
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()

    val mediaAfterVideo = CameraEngine.listCapturedMedia(tempCameraDir)
    assertEquals(2, mediaAfterVideo.size)
    assertTrue(mediaAfterVideo.any { it.isVideo && it.name.startsWith("VID_") })

    // Open Gallery tool directly at the Camera folder via the thumbnail link button
    composeTestRule.onNodeWithTag("recent_captures_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_header_title").assertIsDisplayed()
    composeTestRule.onNodeWithText("Camera").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_breadcrumb_Camera").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_tile_item_${mediaAfterPhoto.first().name}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_back_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_screen").assertIsDisplayed()

    // Switch to Scan mode (QR Code Reader & Barcode Scanner)
    composeTestRule.onNodeWithTag("mode_tab_SCANNER").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_scanner_reticle_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scanner_controls_bar").assertIsDisplayed()

    // Filter to QR Code and trigger scan
    composeTestRule.onNodeWithTag("scan_filter_QR_CODE").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("scanner_detected_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scanner_detected_format_badge").assertIsDisplayed()

    // Switch filter to 1D Barcode and scan an EAN-13 barcode
    composeTestRule.onNodeWithTag("scan_filter_BARCODE").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("scanner_detected_card").assertIsDisplayed()

    // Open QR & Barcode Hub dialog and verify scan history
    composeTestRule.onNodeWithTag("scanner_history_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("scanner_history_dialog").assertIsDisplayed()
  }
}
