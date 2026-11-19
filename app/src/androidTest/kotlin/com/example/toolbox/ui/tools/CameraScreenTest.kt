package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.nio.file.Files
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
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
    composeTestRule.activity
      .getSharedPreferences(SharedPreferencesNotesRepository.PREFS_NAME, Context.MODE_PRIVATE)
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
    composeTestRule.activityRule.scenario.onActivity { activity ->
      assertTrue(activity.window.decorView.findViewById<android.view.View>(android.R.id.content).rootView.keepScreenOn ||
        activity.findViewById<android.view.View>(android.R.id.content).let { content ->
          fun hasKeepScreenOn(v: android.view.View): Boolean {
            if (v.keepScreenOn) return true
            if (v is android.view.ViewGroup) {
              for (i in 0 until v.childCount) {
                if (hasKeepScreenOn(v.getChildAt(i))) return true
              }
            }
            return false
          }
          hasKeepScreenOn(content)
        }
      )
    }
    composeTestRule.onNodeWithTag("camera_viewfinder_stage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_viewfinder_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_top_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_top_bar").assertIsDisplayed().assertHeightIsEqualTo(68.dp)
    composeTestRule.onNodeWithTag("camera_bottom_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_shutter_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_shutter_photo_icon", useUnmergedTree = true).assertIsDisplayed()

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

    // Take a photo in Standard mode and verify the Photo Mode slide-up navigation pill bar is displayed (without 2x2 Booth or Night)
    composeTestRule.onNodeWithTag("photo_controls_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_sub_mode_STANDARD").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_sub_mode_PANORAMA").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_sub_mode_HDR").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_sub_mode_BURST").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_sub_mode_COLLAGE").assertDoesNotExist()
    composeTestRule.onNodeWithTag("photo_sub_mode_NIGHT").assertDoesNotExist()

    // Verify floating Night Mode icon button is positioned to the left of the zoom options and cycles Auto -> On -> Off
    val nightButtonBounds = composeTestRule.onNodeWithTag("camera_night_mode_button").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
    val zoomPresetsBounds = composeTestRule.onNodeWithTag("zoom_presets_row").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
    assertTrue(nightButtonBounds.right <= zoomPresetsBounds.left + 1f)
    composeTestRule.onNodeWithTag("camera_night_mode_label", useUnmergedTree = true).assertTextEquals("Auto")
    composeTestRule.onNodeWithTag("camera_night_mode_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_night_mode_label", useUnmergedTree = true).assertTextEquals("On")
    composeTestRule.onNodeWithTag("camera_night_mode_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_night_mode_label", useUnmergedTree = true).assertTextEquals("Off")
    composeTestRule.onNodeWithTag("camera_night_mode_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_night_mode_label", useUnmergedTree = true).assertTextEquals("Auto")

    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()

    val mediaAfterPhoto = CameraEngine.listCapturedMedia(tempCameraDir)
    assertEquals(1, mediaAfterPhoto.size)
    assertTrue(mediaAfterPhoto.first().name.startsWith("IMG_"))

    // Select Panorama from the Photo navigation pill bar, start a panorama sweep, and finish/stitch the panorama
    composeTestRule.onNodeWithTag("photo_sub_mode_PANORAMA").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_panorama_reticle_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("panorama_sweep_status_badge").assertIsDisplayed()

    // Start panorama sweep
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_shutter_stop_icon", useUnmergedTree = true).assertIsDisplayed()

    // Finish panorama sweep and save stitched PANO_ file
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()

    val mediaAfterPanorama = CameraEngine.listCapturedMedia(tempCameraDir)
    assertEquals(2, mediaAfterPanorama.size)
    assertTrue(mediaAfterPanorama.any { it.name.startsWith("PANO_") })

    // Switch back to Standard photo sub-mode
    composeTestRule.onNodeWithTag("photo_sub_mode_STANDARD").performClick()
    composeTestRule.waitForIdle()

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
    composeTestRule.onNodeWithTag("camera_shutter_video_dot_icon", useUnmergedTree = true).assertIsDisplayed()

    // Start recording
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("video_recording_badge").assertIsDisplayed()

    // Stop recording
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()

    val mediaAfterVideo = CameraEngine.listCapturedMedia(tempCameraDir)
    assertEquals(3, mediaAfterVideo.size)
    assertTrue(mediaAfterVideo.any { it.isVideo && it.name.startsWith("VID_") })

    // Open Gallery tool directly at the Camera folder via the thumbnail link button
    composeTestRule.onNodeWithTag("recent_captures_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_header_title").assertIsDisplayed().assertTextEquals("Camera")
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

    // Open Create Code modal from the All / QR / Barcode selector row
    composeTestRule.onNodeWithTag("open_code_generator_chip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("create_code_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("generator_preview_canvas").assertIsDisplayed()
    composeTestRule.onNodeWithTag("close_create_code_button").performClick()
    composeTestRule.waitForIdle()

    // Verify Note option is in the Photo/Video/Scan/Note selector and icon buttons moved down to camera shutter row
    composeTestRule.onNodeWithTag("mode_tab_NOTE").assertIsDisplayed()
    val selectorBounds = composeTestRule.onNodeWithTag("capture_mode_selector").fetchSemanticsNode().boundsInRoot
    val shutterRowBounds = composeTestRule.onNodeWithTag("camera_shutter_row").fetchSemanticsNode().boundsInRoot
    val adjustmentsBounds = composeTestRule.onNodeWithTag("toggle_adjustments_button").fetchSemanticsNode().boundsInRoot
    val filtersBounds = composeTestRule.onNodeWithTag("toggle_filters_button").fetchSemanticsNode().boundsInRoot
    assertTrue(adjustmentsBounds.top >= selectorBounds.bottom - 1f)
    assertTrue(filtersBounds.top >= selectorBounds.bottom - 1f)
    assertTrue(adjustmentsBounds.center.y >= shutterRowBounds.top && adjustmentsBounds.center.y <= shutterRowBounds.bottom)
    assertTrue(filtersBounds.center.y >= shutterRowBounds.top && filtersBounds.center.y <= shutterRowBounds.bottom)

    // Select Note mode, scan a document via OCR, and verify it is shown for review first before adding to Notes
    composeTestRule.onNodeWithTag("mode_tab_NOTE").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_note_reticle_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_scanner_controls_bar").assertIsDisplayed()

    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("note_scanner_detected_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_scanner_review_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_scanner_detected_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_scanner_add_note_button").assertIsDisplayed()

    // Verify note has NOT been added before pressing Add Note
    val notesBeforeConfirm = SharedPreferencesNotesRepository(composeTestRule.activity).loadNotes()
    assertTrue(notesBeforeConfirm.none { it.title == "Product Roadmap Sync" })

    // Press Add Note after reviewing contents
    composeTestRule.onNodeWithTag("note_scanner_add_note_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("note_scanner_saved_badge").assertIsDisplayed()
    val savedNotes = SharedPreferencesNotesRepository(composeTestRule.activity).loadNotes()
    assertTrue(savedNotes.any { it.title == "Product Roadmap Sync" && it.labels.contains("Scanned") })

    // Select Vision mode (ML Kit live bounding-box tagging overlay)
    composeTestRule.onNodeWithTag("mode_tab_VISION").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_vision_reticle_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_shutter_vision_icon", useUnmergedTree = true).assertIsDisplayed()

    // Trigger ML Kit Vision analysis via the shutter button and verify dynamic colored bounding boxes with labels & percentages >= 65%
    composeTestRule.onNodeWithTag("camera_shutter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("vision_detected_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("vision_box_badge_Plant").assertIsDisplayed()
    composeTestRule.onNodeWithTag("vision_box_badge_Flowerpot").assertIsDisplayed()
    composeTestRule.onNodeWithTag("vision_box_badge_Leaf").assertIsDisplayed()
    // Houseplant (91.4%) has an identical duplicate box to Plant (96.2%) and is suppressed, while Leaf (75.8%) is allowed to overlap and its label does not overlap Plant's label
    composeTestRule.onNodeWithTag("vision_box_badge_Houseplant").assertDoesNotExist()
    val plantBadgeBounds = composeTestRule.onNodeWithTag("vision_box_badge_Plant").fetchSemanticsNode().boundsInRoot
    val leafBadgeBounds = composeTestRule.onNodeWithTag("vision_box_badge_Leaf").fetchSemanticsNode().boundsInRoot
    val flowerpotBadgeBounds = composeTestRule.onNodeWithTag("vision_box_badge_Flowerpot").fetchSemanticsNode().boundsInRoot
    assertFalse(plantBadgeBounds.overlaps(leafBadgeBounds))
    assertFalse(plantBadgeBounds.overlaps(flowerpotBadgeBounds))
    assertFalse(leafBadgeBounds.overlaps(flowerpotBadgeBounds))
    // Room (58.2%) and Furniture (39.4%) are below 65% confidence and must be hidden
    composeTestRule.onNodeWithTag("vision_box_badge_Room").assertDoesNotExist()
    composeTestRule.onNodeWithTag("vision_box_badge_Furniture").assertDoesNotExist()

    // Switch back to Note mode and open Notes directly from the scanned note card
    composeTestRule.onNodeWithTag("mode_tab_NOTE").performClick()
    composeTestRule.waitForIdle()

    // Tap the viewfinder to auto-focus and verify the focus indicator & hex color pill appear below the focus circle
    composeTestRule.onNodeWithTag("camera_viewfinder_box").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_focus_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_focus_color_pill").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_focus_color_hex_text", useUnmergedTree = true).assertIsDisplayed()

    // Tap the focus hex color pill to open the Color Picker tool with the color preselected, then navigate back
    composeTestRule.onNodeWithTag("camera_focus_color_pill").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("color_picker_screen").assertIsDisplayed()
    composeTestRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_screen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("note_scanner_open_notes_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("notes_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Product Roadmap Sync").assertIsDisplayed()
  }

  @Test
  fun cameraScreen_detectsAndSwitchesBetweenMultipleBackAndFrontCameras() {
    val multiCameraCatalog = CameraEngine.buildSystemCameraCatalog(
      listOf(
        RawCameraCharacteristicsInfo(
          cameraId = "0",
          openCameraId = "0",
          lensFacing = CameraLensFacing.BACK,
          focalLengthMm = 4.8f,
          sensorWidthMm = 6.4f,
          aperture = 1.8f,
          hasFlash = true,
        ),
        RawCameraCharacteristicsInfo(
          cameraId = "2",
          openCameraId = "0",
          physicalCameraId = "2",
          lensFacing = CameraLensFacing.BACK,
          focalLengthMm = 1.85f,
          sensorWidthMm = 4.8f,
          aperture = 2.2f,
          hasFlash = false,
        ),
        RawCameraCharacteristicsInfo(
          cameraId = "3",
          openCameraId = "3",
          lensFacing = CameraLensFacing.BACK,
          focalLengthMm = 10.8f,
          sensorWidthMm = 4.8f,
          aperture = 2.8f,
          hasFlash = false,
        ),
        RawCameraCharacteristicsInfo(
          cameraId = "1",
          openCameraId = "1",
          lensFacing = CameraLensFacing.FRONT,
          focalLengthMm = 3.2f,
          sensorWidthMm = 4.0f,
          aperture = 2.0f,
          hasFlash = false,
        ),
      )
    )

    composeTestRule.setContent {
      ToolboxTheme {
        CameraScreen(
          initialDirectory = tempCameraDir,
          systemCamerasOverride = multiCameraCatalog,
        )
      }
    }
    composeTestRule.waitForIdle()

    // Verify all 4 system camera pills (0.5x Ultra, 1x Wide, 3x Tele, Front) are displayed
    composeTestRule.onNodeWithTag("camera_lens_selector_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_device_pill_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_device_pill_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_device_pill_3").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_device_pill_1").assertIsDisplayed()

    // Default active camera is primary 1x Wide back camera (ID 0)
    val prefs = composeTestRule.activity.getSharedPreferences(CAMERA_PREFS_NAME, Context.MODE_PRIVATE)
    composeTestRule.onNodeWithTag("viewfinder_status_pill").assertTextEquals("Back • Wide 1x")

    // Switch directly to Ultra-Wide back camera (ID 2) via pill selector
    composeTestRule.onNodeWithTag("camera_device_pill_2").performClick()
    composeTestRule.waitForIdle()
    assertEquals("2", prefs.getString(PREF_KEY_CAMERA_ID, null))
    composeTestRule.onNodeWithTag("viewfinder_status_pill").assertTextEquals("Back • Ultra 0.5x")

    // Switch directly to Telephoto back camera (ID 3) via pill selector
    composeTestRule.onNodeWithTag("camera_device_pill_3").performClick()
    composeTestRule.waitForIdle()
    assertEquals("3", prefs.getString(PREF_KEY_CAMERA_ID, null))
    composeTestRule.onNodeWithTag("viewfinder_status_pill").assertTextEquals("Back • Tele 3x")

    // Cycle camera using the shutter-row camera switch button (3 -> 1 Front -> 2 Ultra-Wide)
    composeTestRule.onNodeWithTag("camera_flip_lens_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals("1", prefs.getString(PREF_KEY_CAMERA_ID, null))
    composeTestRule.onNodeWithTag("viewfinder_status_pill").assertTextEquals("Front")

    composeTestRule.onNodeWithTag("camera_flip_lens_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals("2", prefs.getString(PREF_KEY_CAMERA_ID, null))
    composeTestRule.onNodeWithTag("viewfinder_status_pill").assertTextEquals("Back • Ultra 0.5x")

    // Open Camera Settings dialog and verify all system camera lens chips and hardware specs are shown
    composeTestRule.onNodeWithTag("camera_settings_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_folder_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_settings_lens_chip_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_settings_lens_chip_3").assertIsDisplayed()
    composeTestRule.onNodeWithTag("camera_settings_lens_specs").assertIsDisplayed()

    // Select 1x Wide (ID 0) from settings dialog
    composeTestRule.onNodeWithTag("camera_settings_lens_chip_0").performClick()
    composeTestRule.waitForIdle()
    assertEquals("0", prefs.getString(PREF_KEY_CAMERA_ID, null))
  }
}
