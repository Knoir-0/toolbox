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

    // Verify main camera screen and viewfinder exist
    composeTestRule.onNodeWithTag("camera_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_viewfinder_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_shutter_button").assertIsDisplayed()

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

    // Open Recent Captures dialog
    composeTestRule.onNodeWithTag("recent_captures_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("recent_captures_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Recent Captures").assertIsDisplayed()
  }
}
