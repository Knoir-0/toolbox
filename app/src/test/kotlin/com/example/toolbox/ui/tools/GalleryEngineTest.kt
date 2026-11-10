package com.example.toolbox.ui.tools

import java.io.File
import java.nio.file.Files
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class GalleryEngineTest {

  private lateinit var tempRootDir: File

  @Before
  fun setUp() {
    tempRootDir = Files.createTempDirectory("gallery_engine_test").toFile()
  }

  @After
  fun tearDown() {
    if (::tempRootDir.isInitialized && tempRootDir.exists()) {
      tempRootDir.deleteRecursively()
    }
  }

  @Test
  fun defaultFolders_areDcimAndPhotos_andContentsAreUnNestedAsSameFolder() {
    assertEquals(listOf("DCIM", "Photos"), DEFAULT_GALLERY_FOLDER_NAMES)

    val folders = GalleryEngine.getDefaultGalleryFolders(tempRootDir)
    assertEquals(2, folders.size)
    assertEquals("DCIM", folders[0].name)
    assertEquals("Photos", folders[1].name)

    val dcimDir = File(tempRootDir, "DCIM").apply { mkdirs() }
    val photosDir = File(tempRootDir, "Photos").apply { mkdirs() }

    // Create sub-albums inside DCIM and Photos
    val cameraAlbumDir = File(dcimDir, "Camera").apply { mkdirs() }
    val screenshotsAlbumDir = File(dcimDir, "Screenshots").apply { mkdirs() }
    val vacationAlbumDir = File(photosDir, "Vacation").apply { mkdirs() }

    GalleryEngine.writeSolidColorPng(
      outputFile = File(cameraAlbumDir, "cam_01.png"),
      lastModifiedMs = 3_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(screenshotsAlbumDir, "screen_01.png"),
      lastModifiedMs = 4_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(vacationAlbumDir, "vac_01.png"),
      lastModifiedMs = 5_000_000L,
    )

    // Also add direct photos in DCIM and Photos
    GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimDir, "dcim_direct.png"),
      lastModifiedMs = 2_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(photosDir, "photos_direct.png"),
      lastModifiedMs = 6_000_000L,
    )

    // Root contents un-nest DCIM and Photos as if they were the same folder
    val rootContents = GalleryEngine.loadRootContents(rootDirectory = tempRootDir)
    assertEquals(
      listOf("Camera", "Screenshots", "Vacation"),
      rootContents.nestedAlbums.map { it.name },
    )
    assertEquals(
      listOf("photos_direct.png", "dcim_direct.png"),
      rootContents.photos.map { it.name },
    )

    val rootAlbums = GalleryEngine.loadRootAlbums(rootDirectory = tempRootDir)
    assertEquals(listOf("Camera", "Screenshots", "Vacation"), rootAlbums.map { it.name })
  }

  @Test
  fun unNestedDcimAndPhotos_mergesSameNamedSubfoldersAcrossBothRoots() {
    val dcimFavorites = File(tempRootDir, "DCIM/Favorites").apply { mkdirs() }
    val photosFavorites = File(tempRootDir, "Photos/Favorites").apply { mkdirs() }
    val dcimSub = File(dcimFavorites, "2025").apply { mkdirs() }
    val photosSub = File(photosFavorites, "2026").apply { mkdirs() }

    GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimFavorites, "older_fav.png"),
      lastModifiedMs = 1_000_000L,
    )
    val newestFav = GalleryEngine.writeSolidColorPng(
      outputFile = File(photosFavorites, "newer_fav.png"),
      lastModifiedMs = 9_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimSub, "sub_2025.png"),
      lastModifiedMs = 3_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(photosSub, "sub_2026.png"),
      lastModifiedMs = 4_000_000L,
    )

    val rootAlbums = GalleryEngine.loadRootAlbums(rootDirectory = tempRootDir)
    assertEquals(1, rootAlbums.size)

    val mergedFavorites = rootAlbums.single()
    assertEquals("Favorites", mergedFavorites.name)
    assertEquals(newestFav.name, mergedFavorites.mostRecentPhoto?.name)
    assertEquals(2, mergedFavorites.directPhotoCount)
    assertEquals(4, mergedFavorites.totalPhotoCount)
    assertEquals(2, mergedFavorites.subAlbumCount)

    // Opening the merged album combines nested albums and direct photos from both DCIM/Favorites and Photos/Favorites
    val mergedContents = GalleryEngine.loadAlbumContents(mergedFavorites)
    assertEquals(listOf("2025", "2026"), mergedContents.nestedAlbums.map { it.name })
    assertEquals(listOf("newer_fav.png", "older_fav.png"), mergedContents.photos.map { it.name })
  }

  @Test
  fun defaultViewModeAndAlbumsPerRow_matchSpecifications() {
    assertEquals(GalleryViewMode.TILE, GalleryViewMode.DEFAULT)
    assertEquals(GalleryViewMode.TILE, GalleryViewMode.fromNameOrDefault(null))
    assertEquals(GalleryViewMode.LIST, GalleryViewMode.fromNameOrDefault("LIST"))
    assertEquals(3, DEFAULT_ALBUMS_PER_ROW)
    assertEquals(3, MIN_ALBUMS_PER_ROW)
    assertEquals(6, MAX_ALBUMS_PER_ROW)
  }

  @Test
  fun coerceAlbumsPerRow_andPinchCalculation_adjustBetweenThreeAndSix() {
    assertEquals(3, GalleryEngine.coerceAlbumsPerRow(1))
    assertEquals(3, GalleryEngine.coerceAlbumsPerRow(3))
    assertEquals(4, GalleryEngine.coerceAlbumsPerRow(4))
    assertEquals(5, GalleryEngine.coerceAlbumsPerRow(5))
    assertEquals(6, GalleryEngine.coerceAlbumsPerRow(6))
    assertEquals(6, GalleryEngine.coerceAlbumsPerRow(9))

    // Pinching in (zoom < 1f) increases albums per row from 3 up to 6
    assertEquals(4, GalleryEngine.calculatePinchAlbumsPerRow(3, 0.75f))
    assertEquals(5, GalleryEngine.calculatePinchAlbumsPerRow(3, 0.58f))
    assertEquals(6, GalleryEngine.calculatePinchAlbumsPerRow(3, 0.40f))

    // Pinching out (zoom > 1f) decreases albums per row from 6 down to 3
    assertEquals(5, GalleryEngine.calculatePinchAlbumsPerRow(6, 1.30f))
    assertEquals(4, GalleryEngine.calculatePinchAlbumsPerRow(6, 1.60f))
    assertEquals(3, GalleryEngine.calculatePinchAlbumsPerRow(6, 2.00f))
  }

  @Test
  fun buildAlbum_selectsMostRecentPhotoAcrossDirectAndNestedFolders() {
    val dcimDir = File(tempRootDir, "DCIM").apply { mkdirs() }
    val photosDir = File(tempRootDir, "Photos").apply { mkdirs() }

    // Empty album has null mostRecentPhoto
    val emptyAlbum = GalleryEngine.buildAlbum(photosDir)
    assertNull(emptyAlbum.mostRecentPhoto)
    assertEquals(0, emptyAlbum.totalPhotoCount)

    // Create older and newer photos in DCIM
    val oldPhoto = GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimDir, "IMG_001.png"),
      red = 200,
      green = 50,
      blue = 50,
      lastModifiedMs = 1_000_000L,
    )
    val midPhoto = GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimDir, "IMG_002.png"),
      red = 50,
      green = 200,
      blue = 50,
      lastModifiedMs = 2_000_000L,
    )

    val albumWithDirect = GalleryEngine.buildAlbum(dcimDir)
    assertNotNull(albumWithDirect.mostRecentPhoto)
    assertEquals(midPhoto.name, albumWithDirect.mostRecentPhoto?.name)
    assertEquals(2, albumWithDirect.directPhotoCount)
    assertEquals(2, albumWithDirect.totalPhotoCount)

    // Add a nested subfolder with an even more recent photo
    val nestedCameraDir = File(dcimDir, "Camera").apply { mkdirs() }
    val newestNestedPhoto = GalleryEngine.writeSolidColorPng(
      outputFile = File(nestedCameraDir, "IMG_003_NEWEST.png"),
      red = 50,
      green = 50,
      blue = 220,
      lastModifiedMs = 5_000_000L,
    )

    val updatedAlbum = GalleryEngine.buildAlbum(dcimDir)
    assertEquals(newestNestedPhoto.name, updatedAlbum.mostRecentPhoto?.name)
    assertEquals(2, updatedAlbum.directPhotoCount)
    assertEquals(3, updatedAlbum.totalPhotoCount)
    assertEquals(1, updatedAlbum.subAlbumCount)
    assertTrue(oldPhoto.exists())
  }

  @Test
  fun loadAlbumContents_supportsMultiLevelNestedAlbumsAndPhotos() {
    val photosRoot = File(tempRootDir, "Photos").apply { mkdirs() }
    val vacationDir = File(photosRoot, "Vacation").apply { mkdirs() }
    val hawaiiDir = File(vacationDir, "Hawaii").apply { mkdirs() }

    GalleryEngine.writeSolidColorPng(
      outputFile = File(hawaiiDir, "beach_sunset.png"),
      lastModifiedMs = 9_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(vacationDir, "airport.png"),
      lastModifiedMs = 4_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(photosRoot, "root_portrait.png"),
      lastModifiedMs = 3_000_000L,
    )

    // Level 1: Photos contains nested album "Vacation" and direct photo "root_portrait.png"
    val photosContents = GalleryEngine.loadAlbumContents(photosRoot)
    assertEquals(1, photosContents.nestedAlbums.size)
    assertEquals("Vacation", photosContents.nestedAlbums[0].name)
    // "Vacation" cover photo should be the newest photo inside its tree ("beach_sunset.png")
    assertEquals("beach_sunset.png", photosContents.nestedAlbums[0].mostRecentPhoto?.name)
    assertEquals(1, photosContents.photos.size)
    assertEquals("root_portrait.png", photosContents.photos[0].name)

    // Level 2: Vacation contains nested album "Hawaii" and direct photo "airport.png"
    val vacationContents = GalleryEngine.loadAlbumContents(vacationDir)
    assertEquals(1, vacationContents.nestedAlbums.size)
    assertEquals("Hawaii", vacationContents.nestedAlbums[0].name)
    assertEquals("beach_sunset.png", vacationContents.nestedAlbums[0].mostRecentPhoto?.name)
    assertEquals(1, vacationContents.photos.size)
    assertEquals("airport.png", vacationContents.photos[0].name)

    // Level 3: Hawaii contains "beach_sunset.png"
    val hawaiiContents = GalleryEngine.loadAlbumContents(hawaiiDir)
    assertTrue(hawaiiContents.nestedAlbums.isEmpty())
    assertEquals(1, hawaiiContents.photos.size)
    assertEquals("beach_sunset.png", hawaiiContents.photos[0].name)
  }
}
