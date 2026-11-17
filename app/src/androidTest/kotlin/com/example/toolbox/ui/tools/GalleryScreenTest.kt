package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
  fun defaultGallery_unNestsPicturesAndDcimAsSameFolder_defaultsToTileViewThreePerRow_withMostRecentPhotoAndLabelUnderneath() {
    val dcimDir = File(testRootDir, "DCIM").apply { mkdirs() }
    val picturesDir = File(testRootDir, "Pictures").apply { mkdirs() }

    // Create albums inside DCIM ("Camera") and Pictures ("Vacation"), plus a shared album name ("Favorites") in both
    val cameraAlbum = File(dcimDir, "Camera").apply { mkdirs() }
    val vacationAlbum = File(picturesDir, "Vacation").apply { mkdirs() }
    val dcimFavorites = File(dcimDir, "Favorites").apply { mkdirs() }
    val picturesFavorites = File(picturesDir, "Favorites").apply { mkdirs() }

    // Populate older and newer photos in DCIM/Camera and Pictures/Vacation
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

    // Populate shared "Favorites" folder across DCIM and Pictures
    GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimFavorites, "fav_dcim_older.png"),
      lastModifiedMs = 3_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(picturesFavorites, "fav_pictures_newest.png"),
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
    composeTestRule.onNodeWithTag("gallery_density_indicator").assertIsDisplayed()

    // 2. Contents of Pictures and DCIM are un-nested at the root level as if they were the same folder
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

    // 3. Each tile shows the most recent photo from that album (including merged Favorites across Pictures & DCIM)
    composeTestRule.onNodeWithTag("album_cover_Camera", useUnmergedTree = true)
      .assertContentDescriptionEquals("Most recent photo: camera_most_recent.png")
    composeTestRule.onNodeWithTag("album_cover_Favorites", useUnmergedTree = true)
      .assertContentDescriptionEquals("Most recent photo: fav_pictures_newest.png")
    composeTestRule.onNodeWithTag("album_cover_Vacation", useUnmergedTree = true)
      .assertContentDescriptionEquals("Most recent photo: vacation_most_recent.png")

    // 4. Folder name label is positioned underneath the tile, and album ... menu icon is at the bottom left
    val cameraTileBounds = cameraTile.fetchSemanticsNode().boundsInRoot
    val cameraLabelBounds = cameraLabel.fetchSemanticsNode().boundsInRoot
    val cameraMoreBounds = composeTestRule.onNodeWithTag("album_more_Camera", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Expected album ... menu to be in the bottom-left quadrant of the tile",
      cameraMoreBounds.center.x < cameraTileBounds.center.x && cameraMoreBounds.center.y > cameraTileBounds.center.y,
    )
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
    val picturesDir = File(testRootDir, "Pictures").apply { mkdirs() }

    // Create 3 albums in DCIM and 3 albums in Pictures -> un-nested together as 6 albums at root
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
      File(picturesDir, "Album_$index").apply {
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
    composeTestRule.onNodeWithTag("gallery_density_indicator").assertIsDisplayed()

    // Verify all 6 un-nested albums from Pictures and DCIM are aligned on the same first row
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
    val picturesDir = File(testRootDir, "Pictures").apply { mkdirs() }

    val cameraAlbum = File(dcimDir, "Camera").apply { mkdirs() }
    val vacationAlbum = File(picturesDir, "Vacation").apply { mkdirs() }

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
    val picturesDir = File(testRootDir, "Pictures").apply { mkdirs() }

    // Create Camera inside DCIM, and nested albums inside Pictures: Pictures -> Vacation -> Hawaii -> Beaches
    val cameraDir = File(dcimDir, "Camera").apply { mkdirs() }
    val vacationDir = File(picturesDir, "Vacation").apply { mkdirs() }
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

    // Root un-nests Pictures and DCIM, showing Camera and Vacation directly; Vacation tile uses newest photo from nested Hawaii folder
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
  fun emptyState_displaysNoPhotosOrAlbumsFoundAndCreateAlbumButton_andDefaultsNewAlbumLocationToPictures() {
    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_empty_album_state").assertIsDisplayed()
    composeTestRule.onNodeWithText("No photos or albums found").assertIsDisplayed()
    composeTestRule.onNodeWithText("Create Album").assertIsDisplayed()

    // Open Add Album dialog via FAB menu on default root screen: only Select and New album are shown (no New album here)
    composeTestRule.onNodeWithTag("gallery_add_folder_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("gallery_fab_menu_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Select").assertIsDisplayed()
    composeTestRule.onNodeWithText("New album").assertIsDisplayed()
    composeTestRule.onNodeWithText("New album here").assertDoesNotExist()
    composeTestRule.onNodeWithTag("gallery_new_album_here_fab_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("gallery_new_album_fab_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_add_folder_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pictures").assertIsDisplayed()
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

  @Test
  fun editButton_opensImageEditorAndVideoEditorDirectlyFromGallery() {
    val dcimDir = File(testRootDir, "DCIM").apply { mkdirs() }
    GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimDir, "photo.png"),
      lastModifiedMs = 2_000_000L,
    )
    File(dcimDir, "video.mp4").apply {
      writeBytes(byteArrayOf(0, 0, 0, 24))
      setLastModified(3_000_000L)
    }

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Edit icon is removed from image/video tiles and can be accessed via the ... menu at the bottom left
    composeTestRule.onNodeWithTag("photo_tile_edit_photo.png").assertDoesNotExist()
    composeTestRule.onNodeWithTag("photo_tile_edit_video.mp4").assertDoesNotExist()

    // Verify ... menu icon for images is at the bottom left of the tile
    val photoTileBounds = composeTestRule.onNodeWithTag("photo_tile_photo.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot
    val photoMoreBounds = composeTestRule.onNodeWithTag("photo_more_photo.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Expected photo ... menu to be in the bottom-left quadrant of the tile",
      photoMoreBounds.center.x < photoTileBounds.center.x && photoMoreBounds.center.y > photoTileBounds.center.y,
    )

    // Open photo.png in viewer and click Edit button -> opens ImageEditorScreen
    composeTestRule.onNodeWithTag("photo_tile_item_photo.png").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_viewer_edit_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_viewer_edit_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("image_editor_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("action_navigate_back").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_screen").assertIsDisplayed()

    // Open video.mp4 in viewer -> verify video plays automatically and starts muted, with toggleable controls
    composeTestRule.onNodeWithTag("photo_tile_item_video.mp4").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_viewer_video_video.mp4", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_viewer_video_controls").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_viewer_video_play_state_label", useUnmergedTree = true)
      .assertTextEquals("Playing")
    composeTestRule.onNodeWithTag("photo_viewer_video_mute_state_label", useUnmergedTree = true)
      .assertTextEquals("Muted")

    composeTestRule.onNodeWithTag("photo_viewer_mute_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_viewer_video_mute_state_label", useUnmergedTree = true)
      .assertTextEquals("Sound on")

    composeTestRule.onNodeWithTag("photo_viewer_play_pause_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_viewer_video_play_state_label", useUnmergedTree = true)
      .assertTextEquals("Paused")

    composeTestRule.onNodeWithTag("photo_viewer_close_button").performClick()
    composeTestRule.waitForIdle()

    // Open ... menu on video.mp4 tile and click Edit -> opens VideoEditorScreen
    composeTestRule.onNodeWithTag("photo_more_video.mp4").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("photo_menu_edit_video.mp4").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("video_editor_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("action_navigate_back").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_screen").assertIsDisplayed()
  }

  @Test
  fun insideAlbum_picturesHaveNoTextUnderneath_areSquareWithTightSpacing_andPinchAdjustsFromThreeUpToSixPerRow() {
    val vacationDir = File(testRootDir, "Pictures/Vacation").apply { mkdirs() }
    (1..6).forEach { index ->
      GalleryEngine.writeSolidColorPng(
        outputFile = File(vacationDir, "pic_$index.png"),
        lastModifiedMs = 1_000_000L * index,
      )
    }

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Navigate into Vacation album
    composeTestRule.onNodeWithTag("album_tile_item_Vacation").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_header_title").assertTextEquals("Vacation")
    composeTestRule.onNodeWithTag("gallery_albums_per_row_count").assertTextEquals("3")

    // Verify no text is shown underneath pictures
    composeTestRule.onNodeWithTag("photo_label_pic_6.png").assertDoesNotExist()
    composeTestRule.onNodeWithText("pic_6.png").assertDoesNotExist()

    // Verify pictures are square and have very little whitespace between them
    val firstPhotoBounds = composeTestRule.onNodeWithTag("photo_tile_pic_6.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot
    val secondPhotoBounds = composeTestRule.onNodeWithTag("photo_tile_pic_5.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Expected square picture tile (width=${firstPhotoBounds.width}, height=${firstPhotoBounds.height})",
      kotlin.math.abs(firstPhotoBounds.width - firstPhotoBounds.height) < 3f,
    )
    val horizontalGapPx = secondPhotoBounds.left - firstPhotoBounds.right
    assertTrue(
      "Expected minimal whitespace between picture tiles, got $horizontalGapPx px",
      horizontalGapPx <= 16f,
    )

    // Pinch to zoom out from 3 pictures per row up to 6 pictures per row
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
    val rowFirstTop = composeTestRule.onNodeWithTag("photo_tile_pic_6.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot.top
    val rowSixthTop = composeTestRule.onNodeWithTag("photo_tile_pic_1.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot.top
    assertTrue(
      "Expected all 6 pictures on the same row when zoomed out to 6 per row",
      kotlin.math.abs(rowFirstTop - rowSixthTop) < 5f,
    )
  }

  @Test
  fun toolbar_hasSortOption_hasNoManualRefreshButton_andSortsPhotosAndAlbums() {
    val cameraDir = File(testRootDir, "DCIM/Camera").apply { mkdirs() }
    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraDir, "alpha.png"),
      lastModifiedMs = 1_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraDir, "zeta.png"),
      lastModifiedMs = 5_000_000L,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Manual refresh option is removed from the toolbar, and sort option + per-row pill button are in the bar underneath
    composeTestRule.onNodeWithTag("gallery_refresh_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("gallery_pill_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_density_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_sort_button").assertIsDisplayed()

    val headerBottom = composeTestRule.onNodeWithTag("gallery_header_title").fetchSemanticsNode().boundsInRoot.bottom
    val densityBounds = composeTestRule.onNodeWithTag("gallery_density_indicator").fetchSemanticsNode().boundsInRoot
    val sortBounds = composeTestRule.onNodeWithTag("gallery_sort_button").fetchSemanticsNode().boundsInRoot

    assertTrue(
      "Expected sort button (${sortBounds.top}) to be in the bar underneath the toolbar ($headerBottom)",
      sortBounds.top >= headerBottom,
    )
    assertTrue(
      "Expected per-row pill button (${densityBounds.right}) to be on the left of the sort button (${sortBounds.left})",
      densityBounds.right <= sortBounds.left,
    )

    // Navigate into Camera album
    composeTestRule.onNodeWithTag("album_tile_item_Camera").performClick()
    composeTestRule.waitForIdle()

    // Default sort is Date (Newest first): zeta.png is before alpha.png
    val initialZetaLeft = composeTestRule.onNodeWithTag("photo_tile_zeta.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot.left
    val initialAlphaLeft = composeTestRule.onNodeWithTag("photo_tile_alpha.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot.left
    assertTrue(
      "Expected zeta.png ($initialZetaLeft) before alpha.png ($initialAlphaLeft) when sorted newest first",
      initialZetaLeft < initialAlphaLeft,
    )

    // Open sort & group menu: verify GROUP BY and SORT BY columns and select Name (A to Z)
    composeTestRule.onNodeWithTag("gallery_sort_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("GROUP BY").assertIsDisplayed()
    composeTestRule.onNodeWithText("SORT BY").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_group_option_folders").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_group_option_date").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_group_option_media_type").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_group_option_smart_tags").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_group_option_none").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_sort_option_name_asc").performClick()
    composeTestRule.waitForIdle()

    val sortedAlphaLeft = composeTestRule.onNodeWithTag("photo_tile_alpha.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot.left
    val sortedZetaLeft = composeTestRule.onNodeWithTag("photo_tile_zeta.png", useUnmergedTree = true)
      .fetchSemanticsNode().boundsInRoot.left
    assertTrue(
      "Expected alpha.png ($sortedAlphaLeft) before zeta.png ($sortedZetaLeft) when sorted by Name (A to Z)",
      sortedAlphaLeft < sortedZetaLeft,
    )
  }

  @Test
  fun fabMenu_insideNestedAlbum_providesNewAlbumAtRootAndNewAlbumHere() {
    val vacationDir = File(testRootDir, "Pictures/Vacation").apply { mkdirs() }
    GalleryEngine.writeSolidColorPng(
      outputFile = File(vacationDir, "beach.png"),
      lastModifiedMs = 2_000_000L,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Navigate into Vacation album
    composeTestRule.onNodeWithTag("album_tile_item_Vacation").performClick()
    composeTestRule.waitForIdle()

    // Open FAB menu -> "New album here" targets Vacation
    composeTestRule.onNodeWithTag("gallery_fab_menu_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("New album").assertIsDisplayed()
    composeTestRule.onNodeWithText("New album here").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_new_album_here_fab_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_add_folder_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("New Nested Album").assertIsDisplayed()
    composeTestRule.onNodeWithText("Create a nested folder inside Vacation").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.waitForIdle()

    // Open FAB menu -> "New album" targets root Pictures even when inside Vacation
    composeTestRule.onNodeWithTag("gallery_fab_menu_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_new_album_fab_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_add_folder_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Album Folder").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pictures").assertIsDisplayed()
  }

  @Test
  fun multiSelection_allowsSelectingMultipleAlbumsAndPhotos_supportsSelectAllInfoAndDelete() {
    val dcimDir = File(testRootDir, "DCIM").apply { mkdirs() }
    val picturesDir = File(testRootDir, "Pictures").apply { mkdirs() }

    val cameraAlbum = File(dcimDir, "Camera").apply { mkdirs() }
    val vacationAlbum = File(picturesDir, "Vacation").apply { mkdirs() }

    GalleryEngine.writeSolidColorPng(File(cameraAlbum, "cam_shot.png"), lastModifiedMs = 1_000_000L)
    GalleryEngine.writeSolidColorPng(File(vacationAlbum, "vac_shot.png"), lastModifiedMs = 2_000_000L)
    GalleryEngine.writeSolidColorPng(File(picturesDir, "root_photo_1.png"), lastModifiedMs = 3_000_000L)
    GalleryEngine.writeSolidColorPng(File(dcimDir, "root_photo_2.png"), lastModifiedMs = 4_000_000L)

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Long-press on Camera album tile to enter multi-selection mode
    composeTestRule.onNodeWithTag("album_tile_item_Camera").performTouchInput {
      longClick()
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_selection_count_label", useUnmergedTree = true)
      .assertTextEquals("1 selected")

    // Tap Vacation album and both root photos to select multiple albums and photos simultaneously
    composeTestRule.onNodeWithTag("album_tile_item_Vacation").performClick()
    composeTestRule.onNodeWithTag("photo_tile_item_root_photo_2.png").performClick()
    composeTestRule.onNodeWithTag("photo_tile_item_root_photo_1.png").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("gallery_selection_count_label", useUnmergedTree = true)
      .assertTextEquals("4 selected")

    // Open Selection Info dialog and verify breakdown of 2 albums and 2 photos
    composeTestRule.onNodeWithTag("action_bulk_info").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_bulk_info_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("2 albums, 2 photos").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_close_bulk_info").performClick()
    composeTestRule.waitForIdle()

    // Deselect Camera and root_photo_1.png, leaving Vacation and root_photo_2.png selected
    composeTestRule.onNodeWithTag("album_tile_item_Camera").performClick()
    composeTestRule.onNodeWithTag("photo_tile_item_root_photo_1.png").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_selection_count_label", useUnmergedTree = true)
      .assertTextEquals("2 selected")

    // Delete selected album (Vacation) and photo (root_photo_2.png)
    composeTestRule.onNodeWithTag("action_bulk_delete").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_bulk_delete_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_confirm_bulk_delete").performClick()
    composeTestRule.waitForIdle()

    // Selection mode exits and only Camera and root_photo_1.png remain
    composeTestRule.onNodeWithTag("album_tile_item_Vacation").assertDoesNotExist()
    composeTestRule.onNodeWithTag("photo_tile_item_root_photo_2.png").assertDoesNotExist()
    composeTestRule.onNodeWithTag("album_tile_item_Camera").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_tile_item_root_photo_1.png").assertIsDisplayed()
  }

  @Test
  fun smartSearch_searchesByOcrTextAndAutoTagChips_andPhotoViewerDisplaysRecognitionDetails() {
    GalleryEngine.clearPhotoIndexCache()
    val picturesDir = File(testRootDir, "Pictures/Receipts").apply { mkdirs() }
    val cameraDir = File(testRootDir, "DCIM/Camera").apply { mkdirs() }

    GalleryEngine.writeTextDocumentPng(
      outputFile = File(picturesDir, "bakery_receipt.png"),
      ocrText = "SUNRISE BAKERY\nSourdough Loaf $8.50\nTotal $8.50",
      imageLabels = listOf("Food"),
      lastModifiedMs = 5_000_000L,
    )
    GalleryEngine.writeTextDocumentPng(
      outputFile = File(cameraDir, "trail_map.png"),
      ocrText = "YOSEMITE VALLEY TRAILHEAD\nElevation 4000ft",
      imageLabels = listOf("Nature", "Mountain"),
      lastModifiedMs = 6_000_000L,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(rootDirectory = testRootDir)
      }
    }
    composeTestRule.waitForIdle()

    // Before opening search, navigation breadcrumbs are shown
    composeTestRule.onNodeWithTag("gallery_breadcrumbs").assertIsDisplayed()

    // Open toolbar search bar -> smart tags replace the navigation breadcrumbs in the pill toolbar, keeping the icon without 'Smart Tags:' text
    composeTestRule.onNodeWithTag("gallery_search_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_toolbar_search_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_search_tag_filter_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gallery_search_tags_icon").assertIsDisplayed()
    composeTestRule.onNodeWithText("Smart Tags:").assertDoesNotExist()
    composeTestRule.onNodeWithTag("gallery_breadcrumbs").assertDoesNotExist()

    // Filter by OCR text inside bakery_receipt.png ("Sourdough")
    composeTestRule.onNodeWithTag("gallery_search_input").performTextInput("Sourdough")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("photo_tile_item_bakery_receipt.png").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_tile_item_trail_map.png").assertDoesNotExist()

    // Clear text query and filter by automatic tag chip "Receipt"
    composeTestRule.onNodeWithTag("gallery_search_clear").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gallery_tag_filter_Receipt").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("photo_tile_item_bakery_receipt.png").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_tile_item_trail_map.png").assertDoesNotExist()

    // Open bakery_receipt.png in Photo Viewer and inspect recognition info dialog
    composeTestRule.onNodeWithTag("photo_tile_item_bakery_receipt.png").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("photo_viewer_recognition_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_viewer_ocr_pill").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("photo_viewer_info_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_info_ocr_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_info_copy_ocr_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photo_info_save_to_notes_button").assertIsDisplayed()
  }

  @Test
  fun inlineMutedVideoPreviews_playSequentiallyInView_pauseAfterAllPlayed_andRestartAfterCyclePause() {
    val cameraDir = File(testRootDir, "DCIM/Camera").apply { mkdirs() }
    File(cameraDir, "clip_1.mp4").apply {
      writeBytes(byteArrayOf(0, 0, 0, 24))
      setLastModified(3_000_000L)
    }
    File(cameraDir, "clip_2.mp4").apply {
      writeBytes(byteArrayOf(0, 0, 0, 24))
      setLastModified(2_000_000L)
    }

    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      ToolboxTheme {
        GalleryScreen(
          rootDirectory = testRootDir,
          initialAlbumDirectory = cameraDir,
          videoPreviewPlayDurationMs = 1_000L,
          videoPreviewCyclePauseDurationMs = 2_000L,
          videoPreviewScrollCooldownMs = 1_000L,
        )
      }
    }
    composeTestRule.mainClock.advanceTimeBy(100L)

    // 1. First video in view (clip_1.mp4, newest first) starts playing muted preview
    composeTestRule.onNodeWithTag("video_preview_playing_clip_1.mp4", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("video_preview_playing_clip_2.mp4", useUnmergedTree = true).assertDoesNotExist()

    // 2. After clip_1.mp4 plays for its preview duration, moves on to clip_2.mp4
    composeTestRule.mainClock.advanceTimeBy(1_050L)
    composeTestRule.onNodeWithTag("video_preview_playing_clip_1.mp4", useUnmergedTree = true).assertDoesNotExist()
    composeTestRule.onNodeWithTag("video_preview_playing_clip_2.mp4", useUnmergedTree = true).assertIsDisplayed()

    // 3. After all videos in view have played, pauses with no video playing
    composeTestRule.mainClock.advanceTimeBy(1_050L)
    composeTestRule.onNodeWithTag("video_preview_playing_clip_1.mp4", useUnmergedTree = true).assertDoesNotExist()
    composeTestRule.onNodeWithTag("video_preview_playing_clip_2.mp4", useUnmergedTree = true).assertDoesNotExist()

    // 4. After the cycle pause completes, begins again with the first video in view (clip_1.mp4)
    composeTestRule.mainClock.advanceTimeBy(2_050L)
    composeTestRule.onNodeWithTag("video_preview_playing_clip_1.mp4", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.mainClock.autoAdvance = true
  }
}
