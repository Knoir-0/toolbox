package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipeLeft
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GalleryScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var testRootDir: File

  @Before
  fun setUp() {
    composeTestRule.activity
      .getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()

    testRootDir = File(composeTestRule.activity.cacheDir, "GalleryTest_${System.currentTimeMillis()}")
    testRootDir.mkdirs()
  }

  @After
  fun tearDown() {
    if (::testRootDir.isInitialized && testRootDir.exists()) {
      testRootDir.deleteRecursively()
    }
  }

  @Test
  fun defaultGallery_unNestsDcimAndPhotosAsSameFolder_defaultsToTileViewThreePerRow_withMostRecentPhotoAndLabelUnderneath() {
    val dcimDir = File(testRootDir, "DCIM").apply { mkdirs() }
    val photosDir = File(testRootDir, "Photos").apply { mkdirs() }

    // Create albums inside DCIM ("Camera") and Photos ("Vacation"), plus a shared album name ("Favorites") in both
    val cameraAlbum = File(dcimDir, "Camera").apply { mkdirs() }
    val vacationAlbum = File(photosDir, "Vacation").apply { mkdirs() }
    val dcimFavorites = File(dcimDir, "Favorites").apply { mkdirs() }
    val photosFavorites = File(photosDir, "Favorites").apply { mkdirs() }

    // Populate older and newer photos in DCIM/Camera and Photos/Vacation
    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraAlbum, "camera_older.png"),
      red = 180,
      green = 40,
      blue = 40,
      lastModifiedMs = 1_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraAlbum, "camera_most_recent.png"),
      red = 40,
      green = 180,
      blue = 40,
      lastModifiedMs = 5_000_000L,
    )

    GalleryEngine.writeSolidColorPng(
      outputFile = File(vacationAlbum, "vacation_older.png"),
      red = 40,
      green = 40,
      blue = 180,
      lastModifiedMs = 2_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(vacationAlbum, "vacation_most_recent.png"),
      red = 200,
      green = 160,
      blue = 40,
      lastModifiedMs = 8_000_000L,
    )

    // Populate shared "Favorites" folder across DCIM and Photos
    GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimFavorites, "fav_dcim_older.png"),
      lastModifiedMs = 3_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(photosFavorites, "fav_photos_newest.png"),
      lastModifiedMs = 9_000_000L,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // 1. Default view is Tile View with 3 albums per row
    composeTestRule.onNodeWithTag("gallery_tile_grid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_albums_per_row_count").assertTextEquals("3")
    composeTestRule.onNodeWithTag("gallery_albums_per_row_label").assertTextEquals("3 per row")

    // 2. Contents of DCIM and Photos are un-nested at the root level as if they were the same folder
    val cameraTile = composeTestRule.onNodeWithTag("album_tile_Camera", useUnmergedTree = true)
    val cameraLabel = composeTestRule.onNodeWithTag("album_label_Camera", useUnmergedTree = true)
    val favoritesTile = composeTestRule.onNodeWithTag("album_tile_Favorites", useUnmergedTree = true)
    val favoritesLabel = composeTestRule.onNodeWithTag("album_label_Favorites", useUnmergedTree = true)
    val vacationTile = composeTestRule.onNodeWithTag("album_tile_Vacation", useUnmergedTree = true)
    val vacationLabel = composeTestRule.onNodeWithTag("album_label_Vacation", useUnmergedTree = true)

    cameraTile.assertIsDisplayed()
    cameraLabel.assertIsDisplayed().assertTextEquals("Camera")
    favoritesTile.assertIsDisplayed()
    favoritesLabel.assertIsDisplayed().assertTextEquals("Favorites")
    vacationTile.assertIsDisplayed()
    vacationLabel.assertIsDisplayed().assertTextEquals("Vacation")

    // 3. Each tile shows the most recent photo from that album (including merged Favorites across DCIM & Photos)
    composeTestRule.onNodeWithTag("album_cover_Camera", useUnmergedTree = true)
      .assertContentDescriptionEquals("Most recent photo: camera_most_recent.png")
    composeTestRule.onNodeWithTag("album_cover_Favorites", useUnmergedTree = true)
      .assertContentDescriptionEquals("Most recent photo: fav_photos_newest.png")
    composeTestRule.onNodeWithTag("album_cover_Vacation", useUnmergedTree = true)
      .assertContentDescriptionEquals("Most recent photo: vacation_most_recent.png")

    // 4. Folder name label is positioned underneath the tile
    val cameraTileBounds = cameraTile.fetchSemanticsNode().boundsInRoot
    val cameraLabelBounds = cameraLabel.fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Expected folder label 'Camera' (${cameraLabelBounds.top}) to be underneath its tile (${cameraTileBounds.bottom})",
      cameraLabelBounds.top >= cameraTileBounds.bottom - 2f,
    )

    val vacationTileBounds = vacationTile.fetchSemanticsNode().boundsInRoot
    val vacationLabelBounds = vacationLabel.fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Expected folder label 'Vacation' (${vacationLabelBounds.top}) to be underneath its tile (${vacationTileBounds.bottom})",
      vacationLabelBounds.top >= vacationTileBounds.bottom - 2f,
    )
  }

  @Test
  fun tileView_pinchInAndOut_adjustsAlbumsPerRowFromThreeUpToSix() {
    val dcimDir = File(testRootDir, "DCIM").apply { mkdirs() }
    val photosDir = File(testRootDir, "Photos").apply { mkdirs() }

    // Create 3 albums in DCIM and 3 albums in Photos -> un-nested together as 6 albums at root
    (1..3).forEach { index ->
      File(dcimDir, "Album_$index").apply {
        mkdirs()
        GalleryEngine.writeSolidColorPng(
          outputFile = File(this, "cover_$index.png"),
          lastModifiedMs = 1_000_000L * index,
        )
      }
    }
    (4..6).forEach { index ->
      File(photosDir, "Album_$index").apply {
        mkdirs()
        GalleryEngine.writeSolidColorPng(
          outputFile = File(this, "cover_$index.png"),
          lastModifiedMs = 1_000_000L * index,
        )
      }
    }

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Starts at 3 albums per row
    composeTestRule.onNodeWithTag("gallery_albums_per_row_count").assertTextEquals("3")

    // Pinch in (two fingers moving closer together) to adjust all the way up to 6 albums per row
    composeTestRule.onNodeWithTag("gallery_content_container").performTouchInput {
      val center = this.center
      pinch(
        start0 = Offset(center.x - 320f, center.y),
        end0 = Offset(center.x - 40f, center.y),
        start1 = Offset(center.x + 320f, center.y),
        end1 = Offset(center.x + 40f, center.y),
        durationMillis = 300L,
      )
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_albums_per_row_count").assertTextEquals("6")
    composeTestRule.onNodeWithTag("gallery_albums_per_row_label").assertTextEquals("6 per row")

    // Verify all 6 un-nested albums from DCIM and Photos are aligned on the same first row
    val firstTileTop = composeTestRule.onNodeWithTag("album_tile_Album_1", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot.top
    val sixthTileTop = composeTestRule.onNodeWithTag("album_tile_Album_6", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot.top
    assertTrue(
      "Expected Album_1 and Album_6 to be on the same row when set to 6 per row",
      kotlin.math.abs(firstTileTop - sixthTileTop) < 5f,
    )

    // Pinch out (two fingers spreading apart) to adjust back down to 3 albums per row
    composeTestRule.onNodeWithTag("gallery_content_container").performTouchInput {
      val center = this.center
      pinch(
        start0 = Offset(center.x - 40f, center.y),
        end0 = Offset(center.x - 320f, center.y),
        start1 = Offset(center.x + 40f, center.y),
        end1 = Offset(center.x + 320f, center.y),
        durationMillis = 300L,
      )
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_albums_per_row_count").assertTextEquals("3")
  }

  @Test
  fun listView_displaysAlbumPhotoIconOnLeftAndLabelOnRight() {
    val dcimDir = File(testRootDir, "DCIM").apply { mkdirs() }
    val photosDir = File(testRootDir, "Photos").apply { mkdirs() }

    val cameraAlbum = File(dcimDir, "Camera").apply { mkdirs() }
    val vacationAlbum = File(photosDir, "Vacation").apply { mkdirs() }

    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraAlbum, "camera_cover.png"),
      lastModifiedMs = 3_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(vacationAlbum, "vacation_cover.png"),
      lastModifiedMs = 4_000_000L,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Switch to secondary List View
    composeTestRule.onNodeWithTag("gallery_list_view_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_list_view").assertIsDisplayed()

    val cameraIcon = composeTestRule.onNodeWithTag("album_list_icon_Camera", useUnmergedTree = true)
    val cameraLabel = composeTestRule.onNodeWithTag("album_label_Camera", useUnmergedTree = true)
    cameraIcon.assertIsDisplayed()
    cameraLabel.assertIsDisplayed().assertTextEquals("Camera")

    val iconBounds = cameraIcon.fetchSemanticsNode().boundsInRoot
    val labelBounds = cameraLabel.fetchSemanticsNode().boundsInRoot

    // Verify the album photo icon is on the left and the folder name label is to the right
    assertTrue(
      "Expected album icon (${iconBounds.right}) to be on the left of folder label (${labelBounds.left})",
      iconBounds.right <= labelBounds.left + 2f,
    )
  }

  @Test
  fun nestedAlbums_allowsNavigatingIntoUnNestedSubfoldersAndBack() {
    val dcimDir = File(testRootDir, "DCIM").apply { mkdirs() }
    val photosDir = File(testRootDir, "Photos").apply { mkdirs() }

    // Create Camera inside DCIM, and nested albums inside Photos: Photos -> Vacation -> Hawaii -> Beaches
    val cameraDir = File(dcimDir, "Camera").apply { mkdirs() }
    val vacationDir = File(photosDir, "Vacation").apply { mkdirs() }
    val hawaiiDir = File(vacationDir, "Hawaii").apply { mkdirs() }

    GalleryEngine.writeSolidColorPng(
      outputFile = File(hawaiiDir, "sunset_hawaii.png"),
      lastModifiedMs = 9_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraDir, "camera_shot.png"),
      lastModifiedMs = 2_000_000L,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Root un-nests DCIM and Photos, showing Camera and Vacation directly; Vacation tile uses newest photo from nested Hawaii folder
    composeTestRule.onNodeWithTag("album_tile_item_Camera").assertIsDisplayed()
    composeTestRule.onNodeWithTag("album_tile_item_Vacation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("album_cover_Vacation", useUnmergedTree = true)
      .assertContentDescriptionEquals("Most recent photo: sunset_hawaii.png")

    // Tap Vacation album to open it
    composeTestRule.onNodeWithTag("album_tile_item_Vacation").performClick()
    composeTestRule.waitForIdle()

    // Inside Vacation: nested album "Hawaii" is displayed
    composeTestRule.onNodeWithTag("gallery_header_title").assertTextEquals("Vacation")
    composeTestRule.onNodeWithTag("album_tile_item_Hawaii").assertIsDisplayed()
    composeTestRule.onNodeWithTag("album_label_Hawaii", useUnmergedTree = true)
      .assertTextEquals("Hawaii")

    // Tap Hawaii nested album to view its photos
    composeTestRule.onNodeWithTag("album_tile_item_Hawaii").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_header_title").assertTextEquals("Hawaii")
    composeTestRule.onNodeWithTag("photo_tile_item_sunset_hawaii.png").assertIsDisplayed()

    // Navigate back using the top bar back button
    composeTestRule.onNodeWithTag("gallery_back_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_header_title").assertTextEquals("Vacation")

    // Jump back to root via breadcrumb
    composeTestRule.onNodeWithTag("gallery_breadcrumb_root").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_header_title").assertTextEquals("Gallery")
    composeTestRule.onNodeWithTag("album_tile_item_Camera").assertIsDisplayed()
    composeTestRule.onNodeWithTag("album_tile_item_Vacation").assertIsDisplayed()
  }

  @Test
  fun emptyState_displaysNoPhotosOrAlbumsFoundAndCreateAlbumButton() {
    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_empty_album_state").assertIsDisplayed()
    composeTestRule.onNodeWithText("No photos or albums found").assertIsDisplayed()
    composeTestRule.onNodeWithText("Create Album").assertIsDisplayed()
  }

  @Test
  fun individualPhotoViewer_hasBlackBackground_supportsSwiping_previewBarTapDismiss_andPinchToZoom() {
    val cameraDir = File(testRootDir, "DCIM/Camera").apply { mkdirs() }

    // Create 3 photos in the same album (sorted newest first: photo_3.png, photo_2.png, photo_1.png)
    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraDir, "photo_1.png"),
      red = 200,
      green = 60,
      blue = 60,
      lastModifiedMs = 1_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraDir, "photo_2.png"),
      red = 60,
      green = 200,
      blue = 60,
      lastModifiedMs = 2_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraDir, "photo_3.png"),
      red = 60,
      green = 60,
      blue = 200,
      lastModifiedMs = 3_000_000L,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Open Camera album and tap the first photo (photo_3.png)
    composeTestRule.onNodeWithTag("album_tile_item_Camera").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_tile_item_photo_3.png").performClick()
    composeTestRule.waitForIdle()

    // 1. Full black background viewer with the image on top
    composeTestRule.onNodeWithTag("gallery_photo_viewer_dialog")
      .assertIsDisplayed()
      .assertContentDescriptionEquals("Full black background photo viewer")
    composeTestRule.onNodeWithTag("photo_viewer_current_title").assertTextEquals("photo_3.png")
    composeTestRule.onNodeWithTag("photo_viewer_image_photo_3.png", useUnmergedTree = true).assertIsDisplayed()

    // 2. Preview scroll bar at the bottom shows all images in the same album
    composeTestRule.onNodeWithTag("photo_viewer_preview_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_viewer_preview_scroll_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_viewer_preview_thumb_photo_3.png").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_viewer_preview_thumb_photo_2.png").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_viewer_preview_thumb_photo_1.png").assertIsDisplayed()

    // 3. Swipe left to move to the next photo in the album (photo_2.png)
    composeTestRule.onNodeWithTag("photo_viewer_pager").performTouchInput {
      swipeLeft()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_viewer_current_title").assertTextEquals("photo_2.png")

    // Tap a thumbnail in the bottom preview scroll bar to jump to photo_1.png
    composeTestRule.onNodeWithTag("photo_viewer_preview_thumb_photo_1.png").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_viewer_current_title").assertTextEquals("photo_1.png")

    // 4. Tap on the image to dismiss the bottom preview scroll bar, and tap again to bring it back
    composeTestRule.onNodeWithTag("photo_viewer_active_page").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_viewer_preview_bar").assertDoesNotExist()

    composeTestRule.onNodeWithTag("photo_viewer_active_page").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_viewer_preview_bar").assertIsDisplayed()

    // 5. Pinch to zoom on the photo
    composeTestRule.onNodeWithTag("photo_viewer_zoom_label", useUnmergedTree = true).assertTextEquals("100%")
    composeTestRule.onNodeWithTag("photo_viewer_active_page").performTouchInput {
      val center = this.center
      pinch(
        start0 = Offset(center.x - 60f, center.y),
        end0 = Offset(center.x - 240f, center.y),
        start1 = Offset(center.x + 60f, center.y),
        end1 = Offset(center.x + 240f, center.y),
        durationMillis = 300L,
      )
    }
    composeTestRule.waitForIdle()

    val zoomLabelText = composeTestRule
      .onNodeWithTag("photo_viewer_zoom_label", useUnmergedTree = true)
      .fetchSemanticsNode()
      .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
      .first()
      .text
    val zoomPercent = zoomLabelText.removeSuffix("%").toInt()
    assertTrue("Expected zoom percent > 100% after pinch-out, got $zoomPercent%", zoomPercent > 100)
  }
}
