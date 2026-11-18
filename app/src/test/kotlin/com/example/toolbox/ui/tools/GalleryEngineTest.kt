package com.example.toolbox.ui.tools

import androidx.compose.ui.graphics.Color
import com.example.toolbox.ui.main.ThemeMode
import java.io.File
import java.nio.file.Files
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
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
  fun defaultFolders_areDcimPicturesAndDownloads_andContentsAreUnNestedAsSameFolder_andDefaultNewAlbumIsPictures() {
    assertEquals(listOf("DCIM", "Pictures", "Download"), DEFAULT_GALLERY_FOLDER_NAMES)
    assertEquals("Pictures", DEFAULT_NEW_ALBUM_FOLDER_NAME)

    val folders = GalleryEngine.getDefaultGalleryFolders(tempRootDir)
    assertEquals(3, folders.size)
    assertEquals(listOf("DCIM", "Pictures", "Download"), folders.map { it.name })

    val defaultNewAlbumFolder = GalleryEngine.getDefaultNewAlbumFolder(tempRootDir)
    assertEquals(File(tempRootDir, "Pictures").absolutePath, defaultNewAlbumFolder.absolutePath)
    assertEquals("Pictures", defaultNewAlbumFolder.name)

    val dcimDir = File(tempRootDir, "DCIM").apply { mkdirs() }
    val picturesDir = File(tempRootDir, "Pictures").apply { mkdirs() }

    // Create sub-albums inside DCIM and Pictures
    val cameraAlbumDir = File(dcimDir, "Camera").apply { mkdirs() }
    val screenshotsAlbumDir = File(dcimDir, "Screenshots").apply { mkdirs() }
    val vacationAlbumDir = File(picturesDir, "Vacation").apply { mkdirs() }

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

    // Also add direct photos in DCIM and Pictures
    GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimDir, "dcim_direct.png"),
      lastModifiedMs = 2_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(picturesDir, "pictures_direct.png"),
      lastModifiedMs = 6_000_000L,
    )

    // Root contents un-nest Pictures and DCIM as if they were the same folder
    val rootContents = GalleryEngine.loadRootContents(rootDirectory = tempRootDir)
    assertEquals(
      listOf("Camera", "Screenshots", "Vacation"),
      rootContents.nestedAlbums.map { it.name },
    )
    assertEquals(
      listOf("pictures_direct.png", "dcim_direct.png"),
      rootContents.photos.map { it.name },
    )

    val rootAlbums = GalleryEngine.loadRootAlbums(rootDirectory = tempRootDir)
    assertEquals(listOf("Camera", "Screenshots", "Vacation"), rootAlbums.map { it.name })

    // When Download exists, it is treated as a single "Download" album rather than un-nested
    val downloadsDir = File(tempRootDir, "Download").apply { mkdirs() }
    val downloadsSubDir = File(downloadsDir, "ExtractedZip").apply { mkdirs() }
    GalleryEngine.writeSolidColorPng(
      outputFile = File(downloadsDir, "dl_direct.png"),
      lastModifiedMs = 7_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(downloadsSubDir, "dl_nested.png"),
      lastModifiedMs = 8_000_000L,
    )

    val rootWithDownloads = GalleryEngine.loadRootContents(rootDirectory = tempRootDir)
    assertEquals(
      listOf("Camera", "Download", "Screenshots", "Vacation"),
      rootWithDownloads.nestedAlbums.map { it.name },
    )
    // Direct photos in Download do not spill into root loose photos
    assertEquals(
      listOf("pictures_direct.png", "dcim_direct.png"),
      rootWithDownloads.photos.map { it.name },
    )

    val downloadsAlbum = rootWithDownloads.nestedAlbums.first { it.name == "Download" }
    assertEquals(2, downloadsAlbum.totalPhotoCount)
    assertEquals(2, downloadsAlbum.directPhotoCount)
    assertEquals(0, downloadsAlbum.subAlbumCount)
    assertEquals("dl_nested.png", downloadsAlbum.mostRecentPhoto?.name)

    val downloadsContents = GalleryEngine.loadAlbumContents(downloadsAlbum)
    assertTrue(downloadsContents.nestedAlbums.isEmpty())
    assertEquals(
      listOf("dl_nested.png", "dl_direct.png"),
      downloadsContents.photos.map { it.name },
    )
  }

  @Test
  fun unNestedPicturesAndDcim_mergesSameNamedSubfoldersAcrossBothRoots() {
    val dcimFavorites = File(tempRootDir, "DCIM/Favorites").apply { mkdirs() }
    val picturesFavorites = File(tempRootDir, "Pictures/Favorites").apply { mkdirs() }
    val dcimSub = File(dcimFavorites, "2025").apply { mkdirs() }
    val picturesSub = File(picturesFavorites, "2026").apply { mkdirs() }

    GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimFavorites, "older_fav.png"),
      lastModifiedMs = 1_000_000L,
    )
    val newestFav = GalleryEngine.writeSolidColorPng(
      outputFile = File(picturesFavorites, "newer_fav.png"),
      lastModifiedMs = 9_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimSub, "sub_2025.png"),
      lastModifiedMs = 3_000_000L,
    )
    GalleryEngine.writeSolidColorPng(
      outputFile = File(picturesSub, "sub_2026.png"),
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

    // Opening the merged album combines nested albums and direct photos from both DCIM/Favorites and Pictures/Favorites
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

    // Content background behind albums and photos is pure white in light mode and pure black in dark mode
    assertEquals(Color.White, GalleryEngine.resolveContentBackgroundColor(ThemeMode.LIGHT, systemInDarkTheme = false))
    assertEquals(Color.White, GalleryEngine.resolveContentBackgroundColor(ThemeMode.LIGHT, systemInDarkTheme = true))
    assertEquals(Color.Black, GalleryEngine.resolveContentBackgroundColor(ThemeMode.DARK, systemInDarkTheme = false))
    assertEquals(Color.Black, GalleryEngine.resolveContentBackgroundColor(ThemeMode.DARK, systemInDarkTheme = true))
    assertEquals(Color.White, GalleryEngine.resolveContentBackgroundColor(ThemeMode.SYSTEM, systemInDarkTheme = false))
    assertEquals(Color.Black, GalleryEngine.resolveContentBackgroundColor(ThemeMode.SYSTEM, systemInDarkTheme = true))
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

    // Individual photo viewer pinch-to-zoom clamps between 1f and 5f
    assertEquals(1f, GalleryEngine.calculatePhotoZoom(1f, 0.5f))
    assertEquals(2.5f, GalleryEngine.calculatePhotoZoom(1f, 2.5f))
    assertEquals(5f, GalleryEngine.calculatePhotoZoom(2f, 4f))
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

  @Test
  fun deferredAlbumArt_resolvesCoverPhotoInBackground() = runBlocking {
    GalleryEngine.clearThumbnailCache()
    val albumDir = File(tempRootDir, "DCIM/Trips").apply { mkdirs() }
    val nestedDir = File(albumDir, "Alps").apply { mkdirs() }
    val newestPhoto = GalleryEngine.writeSolidColorPng(
      outputFile = File(nestedDir, "alps_peak.png"),
      lastModifiedMs = 7_500_000L,
    )

    // Initial deferred build skips deep recursive cover scan
    val deferredAlbum = GalleryEngine.buildAlbum(albumDir, deferAlbumArt = true)
    assertNull(deferredAlbum.mostRecentPhoto)
    assertEquals(1, deferredAlbum.subAlbumCount)

    // Background resolution populates mostRecentPhoto and totalPhotoCount on Dispatchers.IO
    val resolvedAlbum = GalleryEngine.resolveAlbumArtAsync(deferredAlbum)
    assertNotNull(resolvedAlbum.mostRecentPhoto)
    assertEquals(newestPhoto.name, resolvedAlbum.mostRecentPhoto?.name)
    assertEquals(1, resolvedAlbum.totalPhotoCount)
  }

  @Test
  fun noMediaFile_hidesDirectoryAndSubtreeFromAlbumsCountsAndCovers() {
    val picturesDir = File(tempRootDir, "Pictures").apply { mkdirs() }
    val visibleDir = File(picturesDir, "Visible").apply { mkdirs() }
    val hiddenDir = File(picturesDir, "Hidden").apply { mkdirs() }
    val hiddenNestedDir = File(hiddenDir, "Nested").apply { mkdirs() }
    File(hiddenDir, ".nomedia").createNewFile()

    GalleryEngine.writeSolidColorPng(File(visibleDir, "visible.png"), lastModifiedMs = 1_000_000L)
    GalleryEngine.writeSolidColorPng(File(hiddenDir, "hidden.png"), lastModifiedMs = 9_000_000L)
    GalleryEngine.writeSolidColorPng(File(hiddenNestedDir, "hidden_nested.png"), lastModifiedMs = 9_500_000L)

    // Hidden album is excluded from the root listing
    val rootAlbums = GalleryEngine.loadRootAlbums(rootDirectory = tempRootDir)
    assertEquals(listOf("Visible"), rootAlbums.map { it.name })

    // Covers and recursive counts ignore the .nomedia subtree
    val picturesAlbum = GalleryEngine.buildAlbum(picturesDir)
    assertEquals(1, picturesAlbum.subAlbumCount)
    assertEquals(1, picturesAlbum.totalPhotoCount)
    assertEquals("visible.png", picturesAlbum.mostRecentPhoto?.name)

    // Opening a .nomedia folder directly yields nothing
    val hiddenContents = GalleryEngine.loadAlbumContents(hiddenDir)
    assertTrue(hiddenContents.photos.isEmpty())
    assertTrue(hiddenContents.nestedAlbums.isEmpty())

    // A whole root folder with .nomedia (or a .nomedia ancestor) is skipped entirely
    val dcimDir = File(tempRootDir, "DCIM").apply { mkdirs() }
    GalleryEngine.writeSolidColorPng(File(dcimDir, "dcim.png"), lastModifiedMs = 2_000_000L)
    File(dcimDir, ".nomedia").createNewFile()
    val rootContents = GalleryEngine.loadRootContents(
      rootDirectory = tempRootDir,
      additionalPaths = listOf("Pictures/Hidden/Nested"),
    )
    assertTrue(rootContents.photos.isEmpty())
    assertEquals(listOf("Visible"), rootContents.nestedAlbums.map { it.name })
    assertTrue(GalleryEngine.isHiddenByNoMedia(hiddenNestedDir))
    assertTrue(GalleryEngine.containsNoMediaFile(hiddenDir))

    // Custom folder pill bypasses .nomedia files when viewing the custom folder directly,
    // while remaining excluded from the root Album view
    val customHiddenFolder = GalleryFolder(
      folderName = "Hidden",
      displayPath = "/Pictures/Hidden",
      customAbsolutePath = hiddenDir.absolutePath,
    )
    val customFolderContents = GalleryEngine.loadFolderPillContents(
      folder = customHiddenFolder,
      rootDirectory = tempRootDir,
    )
    assertEquals(listOf("hidden.png"), customFolderContents.photos.map { it.name })
    assertEquals(listOf("Nested"), customFolderContents.nestedAlbums.map { it.name })
    val nestedContents = GalleryEngine.loadAlbumContents(customFolderContents.nestedAlbums.single())
    assertEquals(listOf("hidden_nested.png"), nestedContents.photos.map { it.name })
  }

  @Test
  fun loadAlbumContents_includesSupportedImageAndVideoFiles_andSetsIsVideoFlag() {
    val mediaDir = File(tempRootDir, "DCIM/Media").apply { mkdirs() }
    GalleryEngine.writeSolidColorPng(
      outputFile = File(mediaDir, "photo.png"),
      lastModifiedMs = 3_000_000L,
    )
    val videoFile = File(mediaDir, "clip.mp4").apply {
      writeBytes(byteArrayOf(0, 0, 0, 24))
      setLastModified(4_000_000L)
    }

    val contents = GalleryEngine.loadAlbumContents(mediaDir)
    assertEquals(2, contents.photos.size)
    assertEquals("clip.mp4", contents.photos[0].name)
    assertTrue(contents.photos[0].isVideo)
    assertEquals(videoFile.absolutePath, contents.photos[0].path)
    assertEquals("photo.png", contents.photos[1].name)
    assertTrue(!contents.photos[1].isVideo)
  }

  @Test
  fun sortAlbums_andSortPhotos_orderByDateNameAndSize() {
    assertEquals(GallerySortOrder.DATE_DESC, GallerySortOrder.DEFAULT)
    assertEquals(GallerySortOrder.DATE_DESC, GallerySortOrder.fromNameOrDefault(null))
    assertEquals(GallerySortOrder.NAME_ASC, GallerySortOrder.fromNameOrDefault("NAME_ASC"))

    val albums = listOf(
      GalleryAlbum(
        directory = File(tempRootDir, "Beta"),
        name = "Beta",
        path = "/Beta",
        mostRecentPhoto = null,
        directPhotoCount = 5,
        totalPhotoCount = 5,
        subAlbumCount = 0,
        lastModifiedMs = 2_000L,
      ),
      GalleryAlbum(
        directory = File(tempRootDir, "Alpha"),
        name = "Alpha",
        path = "/Alpha",
        mostRecentPhoto = null,
        directPhotoCount = 12,
        totalPhotoCount = 12,
        subAlbumCount = 0,
        lastModifiedMs = 1_000L,
      ),
      GalleryAlbum(
        directory = File(tempRootDir, "Gamma"),
        name = "Gamma",
        path = "/Gamma",
        mostRecentPhoto = null,
        directPhotoCount = 2,
        totalPhotoCount = 2,
        subAlbumCount = 0,
        lastModifiedMs = 3_000L,
      ),
    )

    assertEquals(
      listOf("Gamma", "Beta", "Alpha"),
      GalleryEngine.sortAlbums(albums, GallerySortOrder.DATE_DESC).map { it.name },
    )
    assertEquals(
      listOf("Alpha", "Beta", "Gamma"),
      GalleryEngine.sortAlbums(albums, GallerySortOrder.DATE_ASC).map { it.name },
    )
    assertEquals(
      listOf("Alpha", "Beta", "Gamma"),
      GalleryEngine.sortAlbums(albums, GallerySortOrder.NAME_ASC).map { it.name },
    )
    assertEquals(
      listOf("Gamma", "Beta", "Alpha"),
      GalleryEngine.sortAlbums(albums, GallerySortOrder.NAME_DESC).map { it.name },
    )
    assertEquals(
      listOf("Alpha", "Beta", "Gamma"),
      GalleryEngine.sortAlbums(albums, GallerySortOrder.SIZE_DESC).map { it.name },
    )
    assertEquals(
      listOf("Gamma", "Beta", "Alpha"),
      GalleryEngine.sortAlbums(albums, GallerySortOrder.SIZE_ASC).map { it.name },
    )

    val photos = listOf(
      GalleryPhoto(
        file = File(tempRootDir, "b.png"),
        name = "b.png",
        path = "/b.png",
        sizeBytes = 500L,
        lastModifiedMs = 2_000L,
        extension = "png",
      ),
      GalleryPhoto(
        file = File(tempRootDir, "a.png"),
        name = "a.png",
        path = "/a.png",
        sizeBytes = 1200L,
        lastModifiedMs = 1_000L,
        extension = "png",
      ),
      GalleryPhoto(
        file = File(tempRootDir, "c.png"),
        name = "c.png",
        path = "/c.png",
        sizeBytes = 200L,
        lastModifiedMs = 3_000L,
        extension = "png",
      ),
    )

    assertEquals(
      listOf("c.png", "b.png", "a.png"),
      GalleryEngine.sortPhotos(photos, GallerySortOrder.DATE_DESC).map { it.name },
    )
    assertEquals(
      listOf("a.png", "b.png", "c.png"),
      GalleryEngine.sortPhotos(photos, GallerySortOrder.DATE_ASC).map { it.name },
    )
    assertEquals(
      listOf("a.png", "b.png", "c.png"),
      GalleryEngine.sortPhotos(photos, GallerySortOrder.NAME_ASC).map { it.name },
    )
    assertEquals(
      listOf("c.png", "b.png", "a.png"),
      GalleryEngine.sortPhotos(photos, GallerySortOrder.NAME_DESC).map { it.name },
    )
    assertEquals(
      listOf("a.png", "b.png", "c.png"),
      GalleryEngine.sortPhotos(photos, GallerySortOrder.SIZE_DESC).map { it.name },
    )
    assertEquals(
      listOf("c.png", "b.png", "a.png"),
      GalleryEngine.sortPhotos(photos, GallerySortOrder.SIZE_ASC).map { it.name },
    )
  }

  @Test
  fun bulkSelectionOperations_infoCopyMoveAndDeleteMultipleAlbumsAndPhotos() {
    val picturesDir = File(tempRootDir, "Pictures").apply { mkdirs() }
    val dcimDir = File(tempRootDir, "DCIM").apply { mkdirs() }

    val tripAlbumDir = File(picturesDir, "Trip").apply { mkdirs() }
    val cameraAlbumDir = File(dcimDir, "Camera").apply { mkdirs() }

    val tripPhoto1 = GalleryEngine.writeSolidColorPng(File(tripAlbumDir, "trip1.png"), lastModifiedMs = 1_000_000L)
    val tripPhoto2 = GalleryEngine.writeSolidColorPng(File(tripAlbumDir, "trip2.png"), lastModifiedMs = 2_000_000L)
    val camPhoto1 = GalleryEngine.writeSolidColorPng(File(cameraAlbumDir, "cam1.png"), lastModifiedMs = 3_000_000L)
    val rootDirectPhoto1 = GalleryEngine.writeSolidColorPng(File(picturesDir, "direct1.png"), lastModifiedMs = 4_000_000L)
    val rootDirectPhoto2 = GalleryEngine.writeSolidColorPng(File(dcimDir, "direct2.png"), lastModifiedMs = 5_000_000L)

    val rootContents = GalleryEngine.loadRootContents(rootDirectory = tempRootDir)
    val selectedAlbums = rootContents.nestedAlbums // Camera, Trip
    val selectedPhotos = rootContents.photos // direct2.png, direct1.png

    assertEquals(2, selectedAlbums.size)
    assertEquals(2, selectedPhotos.size)

    // 1. Bulk selection info across multiple albums and photos
    val info = GalleryEngine.getBulkSelectionInfo(selectedAlbums, selectedPhotos)
    assertEquals(2, info.albumCount)
    assertEquals(2, info.photoCount)
    assertEquals(4, info.totalItems)
    assertEquals(5, info.totalMediaFileCount)
    val expectedBytes = listOf(tripPhoto1, tripPhoto2, camPhoto1, rootDirectPhoto1, rootDirectPhoto2).sumOf { it.length() }
    assertEquals(expectedBytes, info.totalSizeBytes)

    // 2. Copy selected albums and photos into a destination folder
    val backupDir = File(picturesDir, "Backup").apply { mkdirs() }
    val copiedCount = GalleryEngine.copySelectedItems(
      albums = listOf(selectedAlbums.first { it.name == "Trip" }),
      photos = selectedPhotos,
      destinationDir = backupDir,
    )
    assertEquals(3, copiedCount)
    assertTrue(File(backupDir, "Trip/trip1.png").exists())
    assertTrue(File(backupDir, "Trip/trip2.png").exists())
    assertTrue(File(backupDir, "direct1.png").exists())
    assertTrue(File(backupDir, "direct2.png").exists())

    // 3. Move selected album and photo into destination folder
    val archiveDir = File(picturesDir, "Archive").apply { mkdirs() }
    val movedCount = GalleryEngine.moveSelectedItems(
      albums = listOf(selectedAlbums.first { it.name == "Camera" }),
      photos = listOf(selectedPhotos.first { it.name == "direct1.png" }),
      destinationDir = archiveDir,
    )
    assertEquals(2, movedCount)
    assertTrue(File(archiveDir, "Camera/cam1.png").exists())
    assertTrue(!cameraAlbumDir.exists())
    assertTrue(File(archiveDir, "direct1.png").exists())
    assertTrue(!rootDirectPhoto1.exists())

    // 4. Delete selected albums and photos
    val deletedCount = GalleryEngine.deleteSelectedItems(
      albums = listOf(selectedAlbums.first { it.name == "Trip" }),
      photos = listOf(selectedPhotos.first { it.name == "direct2.png" }),
    )
    assertEquals(2, deletedCount)
    assertTrue(!tripAlbumDir.exists())
    assertTrue(!rootDirectPhoto2.exists())
  }

  @Test
  fun indexSinglePhoto_andDeriveAutoTags_extractsOcrTextVisualLabelsAndSmartTags() = runBlocking {
    assertEquals(0.80f, GalleryEngine.DEFAULT_ML_KIT_LABEL_CONFIDENCE_THRESHOLD)
    GalleryEngine.clearPhotoIndexCache()
    val screenshotsDir = File(tempRootDir, "Pictures/Screenshots").apply { mkdirs() }
    val receiptsDir = File(tempRootDir, "Pictures/Finance").apply { mkdirs() }

    val receiptFile = GalleryEngine.writeTextDocumentPng(
      outputFile = File(receiptsDir, "coffee_invoice.png"),
      ocrText = "BLUE BOTTLE COFFEE\nInvoice #1042\nSubtotal $12.00\nTax $1.08\nTotal $13.08",
      imageLabels = listOf("Food", "Coffee"),
      lastModifiedMs = 5_000_000L,
    )
    val screenshotNoteFile = GalleryEngine.writeTextDocumentPng(
      outputFile = File(screenshotsDir, "Screenshot_20261115.png"),
      ocrText = "Meeting Notes:\n- Ship gallery OCR search\n- Add automatic tagging\n- Cache index on disk",
      imageLabels = listOf("Screenshot"),
      lastModifiedMs = 6_000_000L,
    )

    val receiptEntry = GalleryEngine.indexSinglePhoto(receiptFile)
    assertTrue(receiptEntry.ocrText.contains("BLUE BOTTLE COFFEE"))
    assertTrue(receiptEntry.autoTags.contains("Receipt"))
    assertTrue(receiptEntry.autoTags.contains("Document"))
    assertTrue(receiptEntry.autoTags.contains("Text"))
    assertTrue(receiptEntry.autoTags.contains("Food"))
    assertTrue(receiptEntry.autoTags.contains("Coffee"))

    val screenshotEntry = GalleryEngine.indexSinglePhoto(screenshotNoteFile)
    assertTrue(screenshotEntry.ocrText.contains("Ship gallery OCR search"))
    assertTrue(screenshotEntry.autoTags.contains("Screenshot"))
    assertTrue(screenshotEntry.autoTags.contains("Notes"))
    assertTrue(screenshotEntry.autoTags.contains("Document"))
  }

  @Test
  fun indexCachePersistence_loadsValidEntriesAndInvalidatesModifiedFiles() = runBlocking {
    val cacheFile = File(tempRootDir, "gallery_smart_index_test.json")
    GalleryEngine.clearPhotoIndexCache()

    val picturesDir = File(tempRootDir, "Pictures").apply { mkdirs() }
    val docFile = GalleryEngine.writeTextDocumentPng(
      outputFile = File(picturesDir, "scan_01.png"),
      ocrText = "Original Contract Terms 2025",
      imageLabels = listOf("Document"),
      lastModifiedMs = 2_000_000L,
    )
    val photo = GalleryPhoto(
      file = docFile,
      name = docFile.name,
      path = docFile.absolutePath,
      sizeBytes = docFile.length(),
      lastModifiedMs = docFile.lastModified(),
      extension = "png",
    )

    val indexedMap = GalleryEngine.indexPhotosInBackground(
      photos = listOf(photo),
      cacheFile = cacheFile,
    )
    assertEquals(1, indexedMap.size)
    assertTrue(cacheFile.exists())

    // Clear in-memory cache and reload from disk
    GalleryEngine.clearPhotoIndexCache()
    val loadedFromDisk = GalleryEngine.loadIndexCacheFromDisk(cacheFile)
    assertEquals(1, loadedFromDisk.size)
    assertEquals("Original Contract Terms 2025", GalleryEngine.getCachedPhotoIndex(docFile)?.ocrText)

    // Modify the file content & timestamp -> cached signature should invalidate and re-index
    GalleryEngine.writeTextDocumentPng(
      outputFile = docFile,
      ocrText = "Updated Contract Addendum 2026",
      imageLabels = listOf("Document"),
      lastModifiedMs = 9_000_000L,
    )
    assertNull(GalleryEngine.getCachedPhotoIndex(docFile))

    val reindexed = GalleryEngine.indexSinglePhoto(docFile)
    assertEquals("Updated Contract Addendum 2026", reindexed.ocrText)
  }

  @Test
  fun searchGallery_matchesOcrTextQrPayloadsFilenamesAndAutoTagFilters() {
    GalleryEngine.clearPhotoIndexCache()
    val picturesDir = File(tempRootDir, "Pictures/Scans").apply { mkdirs() }
    val receiptFile = GalleryEngine.writeTextDocumentPng(
      outputFile = File(picturesDir, "dinner_receipt.png"),
      ocrText = "TRATTORIA ROMA\nTotal $48.50",
      imageLabels = listOf("Food"),
      lastModifiedMs = 4_000_000L,
    )
    val wifiQrFile = GalleryEngine.writeSolidColorPng(
      outputFile = File(picturesDir, "guest_wifi.png"),
      lastModifiedMs = 5_000_000L,
    )

    GalleryEngine.putCachedPhotoIndex(
      GalleryPhotoIndexEntry(
        path = receiptFile.absolutePath,
        lastModifiedMs = receiptFile.lastModified(),
        sizeBytes = receiptFile.length(),
        ocrText = "TRATTORIA ROMA\nTotal $48.50",
        barcodePayloads = emptyList(),
        barcodeSummaries = emptyList(),
        autoTags = listOf("Receipt", "Food", "Text"),
      )
    )
    GalleryEngine.putCachedPhotoIndex(
      GalleryPhotoIndexEntry(
        path = wifiQrFile.absolutePath,
        lastModifiedMs = wifiQrFile.lastModified(),
        sizeBytes = wifiQrFile.length(),
        ocrText = "",
        barcodePayloads = listOf("WIFI:S:CafeGuest;T:WPA;P:espresso123;;"),
        barcodeSummaries = listOf("Wi-Fi: CafeGuest (WPA)"),
        autoTags = listOf("QR Code", "Wi-Fi"),
      )
    )

    val rootContents = GalleryEngine.loadRootContents(rootDirectory = tempRootDir)
    val allPhotos = GalleryEngine.collectAllGalleryPhotos(rootContents = rootContents)
    val indexMap = GalleryEngine.getAllCachedPhotoIndices()

    // 1. Search by OCR text inside image
    val ocrSearch = GalleryEngine.searchGallery(
      albums = rootContents.nestedAlbums,
      directPhotos = rootContents.photos,
      allScopePhotos = allPhotos,
      indexMap = indexMap,
      searchQuery = "trattoria",
    )
    assertEquals(1, ocrSearch.matchingPhotos.size)
    assertEquals("dinner_receipt.png", ocrSearch.matchingPhotos.single().name)
    assertTrue(ocrSearch.matchSnippetsByPath[receiptFile.absolutePath].orEmpty().contains("OCR:"))

    // 2. Search by QR code payload
    val qrSearch = GalleryEngine.searchGallery(
      albums = rootContents.nestedAlbums,
      directPhotos = rootContents.photos,
      allScopePhotos = allPhotos,
      indexMap = indexMap,
      searchQuery = "CafeGuest",
    )
    assertEquals(1, qrSearch.matchingPhotos.size)
    assertEquals("guest_wifi.png", qrSearch.matchingPhotos.single().name)
    assertTrue(qrSearch.matchSnippetsByPath[wifiQrFile.absolutePath].orEmpty().contains("Code:"))

    // 3. Filter by automatic tag chip alone ("Receipt")
    val tagFilterSearch = GalleryEngine.searchGallery(
      albums = rootContents.nestedAlbums,
      directPhotos = rootContents.photos,
      allScopePhotos = allPhotos,
      indexMap = indexMap,
      searchQuery = "",
      selectedTag = "Receipt",
    )
    assertEquals(1, tagFilterSearch.matchingPhotos.size)
    assertEquals("dinner_receipt.png", tagFilterSearch.matchingPhotos.single().name)

    // 4. Available auto-tags list is ordered and contains both photos' tags
    val availableTagNames = GalleryEngine.extractAvailableAutoTags(
      photos = allPhotos,
      indexMap = indexMap,
    ).map { it.first }
    assertTrue(availableTagNames.containsAll(listOf("Receipt", "QR Code", "Wi-Fi", "Food", "Text")))
  }

  @Test
  fun inlineVideoPreviewScheduler_cyclesThroughVisibleVideos_pausesAfterCycle_andPausesWhenPlayingVideoScrolledOutOfView() {
    assertTrue(DEFAULT_VIDEO_PREVIEW_PLAY_DURATION_MS in 3_000L..6_000L)
    assertTrue(DEFAULT_VIDEO_PREVIEW_CYCLE_PAUSE_DURATION_MS in 6_000L..8_000L)
    assertTrue(DEFAULT_VIDEO_PREVIEW_SCROLL_OUT_COOLDOWN_MS in 2_000L..3_000L)

    val visibleVideos = listOf("/DCIM/v1.mp4", "/DCIM/v2.mp4", "/DCIM/v3.mp4")

    // 1. Starts playing the first video in view
    var state = GalleryEngine.onVisibleVideosChanged(
      state = GalleryVideoPreviewState(),
      visibleVideoPaths = visibleVideos,
    )
    assertEquals(GalleryVideoPreviewPhase.PLAYING, state.phase)
    assertEquals("/DCIM/v1.mp4", state.activeVideoPath)

    // 2. After v1 finishes (3-6s), moves on to v2, then v3
    state = GalleryEngine.onActiveVideoPreviewCompleted(state, visibleVideos)
    assertEquals(GalleryVideoPreviewPhase.PLAYING, state.phase)
    assertEquals("/DCIM/v2.mp4", state.activeVideoPath)

    state = GalleryEngine.onActiveVideoPreviewCompleted(state, visibleVideos)
    assertEquals(GalleryVideoPreviewPhase.PLAYING, state.phase)
    assertEquals("/DCIM/v3.mp4", state.activeVideoPath)

    // 3. Once all videos in view have been played, pauses in CYCLE_PAUSE (6-8s) with no active video
    state = GalleryEngine.onActiveVideoPreviewCompleted(state, visibleVideos)
    assertEquals(GalleryVideoPreviewPhase.CYCLE_PAUSE, state.phase)
    assertNull(state.activeVideoPath)

    // 4. After the cycle pause completes, begins again with the first video in view (v1)
    state = GalleryEngine.onPreviewPauseOrCooldownCompleted(state, visibleVideos)
    assertEquals(GalleryVideoPreviewPhase.PLAYING, state.phase)
    assertEquals("/DCIM/v1.mp4", state.activeVideoPath)

    // 5. If the user scrolls the playing video (v1) out of view, playback immediately pauses in SCROLL_COOLDOWN
    val scrolledVisibleVideos = listOf("/DCIM/v2.mp4", "/DCIM/v3.mp4", "/DCIM/v4.mp4")
    state = GalleryEngine.onVisibleVideosChanged(state, scrolledVisibleVideos)
    assertEquals(GalleryVideoPreviewPhase.SCROLL_COOLDOWN, state.phase)
    assertNull(state.activeVideoPath)

    // 6. After the 2-3s cooldown following scroll stop, starts again with the first video currently in view (v2)
    state = GalleryEngine.onPreviewPauseOrCooldownCompleted(state, scrolledVisibleVideos)
    assertEquals(GalleryVideoPreviewPhase.PLAYING, state.phase)
    assertEquals("/DCIM/v2.mp4", state.activeVideoPath)
  }

  @Test
  fun groupPhotos_supportsFoldersDateMediaTypeSmartTagsAndNone_andSavesSortGroupConfigs() {
    GalleryEngine.clearSortGroupConfigs()
    assertEquals(GalleryGroupOption.NONE, GalleryGroupOption.DEFAULT)
    assertEquals(GalleryGroupOption.NONE, GalleryGroupOption.fromNameOrDefault(null))
    assertEquals(GalleryGroupOption.FOLDERS, GalleryGroupOption.fromNameOrDefault("FOLDERS"))
    assertEquals(GalleryGroupOption.DATE, GalleryGroupOption.fromNameOrDefault("DATE"))
    assertEquals(GalleryGroupOption.MEDIA_TYPE, GalleryGroupOption.fromNameOrDefault("MEDIA_TYPE"))
    assertEquals(GalleryGroupOption.SMART_TAGS, GalleryGroupOption.fromNameOrDefault("SMART_TAGS"))
    assertEquals(GalleryGroupOption.NONE, GalleryGroupOption.fromNameOrDefault("NONE"))

    val nowMs = 1_763_400_000_000L
    val oneHourAgo = nowMs - 3_600_000L
    val yesterdayMs = nowMs - 26L * 3_600_000L
    val sixtyDaysAgoMs = nowMs - 60L * 24L * 3_600_000L

    val dcimDir = File(tempRootDir, "DCIM").apply { mkdirs() }
    val picturesDir = File(tempRootDir, "Pictures").apply { mkdirs() }
    val downloadsDir = File(tempRootDir, "Download").apply { mkdirs() }

    val photoFile = File(dcimDir, "Camera/portrait.jpg").apply {
      parentFile?.mkdirs()
      writeBytes(byteArrayOf(1, 2, 3))
    }
    val videoFile = File(dcimDir, "Camera/clip.mp4").apply {
      writeBytes(byteArrayOf(0, 0, 0, 24))
    }
    val gifFile = File(picturesDir, "meme.gif").apply {
      parentFile?.mkdirs()
      writeBytes(byteArrayOf(1, 2))
    }
    val screenshotFile = File(picturesDir, "Screenshots/Screenshot_01.png").apply {
      parentFile?.mkdirs()
      writeBytes(byteArrayOf(1, 2, 3, 4))
    }
    val downloadedPhoto = File(downloadsDir, "downloaded.png").apply {
      writeBytes(byteArrayOf(1, 2, 3))
    }

    // 0. FOLDERS groups by the default root folders ("DCIM", "Pictures", and "Download")
    val folderGroups = GalleryEngine.groupByDefaultFolders(
      rootDirectories = listOf(dcimDir, picturesDir, downloadsDir),
      sortOrder = GallerySortOrder.NAME_ASC,
    )
    assertEquals(listOf("DCIM", "Pictures", "Download"), folderGroups.map { it.title })
    assertEquals(listOf("Camera"), folderGroups[0].albums.map { it.name })
    assertEquals(listOf("Screenshots"), folderGroups[1].albums.map { it.name })
    assertEquals(listOf("meme.gif"), folderGroups[1].photos.map { it.name })
    assertEquals(listOf("Download"), folderGroups[2].albums.map { it.name })
    assertEquals(1, folderGroups[2].albums.single().totalPhotoCount)
    assertTrue(folderGroups[2].photos.isEmpty())

    // Verify saving default and folder-specific sort & group configurations
    val customDefault = GallerySortGroupConfig(GalleryGroupOption.DATE, GallerySortOrder.NAME_ASC)
    GalleryEngine.setDefaultSortGroupConfig(config = customDefault)
    assertEquals(customDefault, GalleryEngine.getDefaultSortGroupConfig())

    val cameraFolderConfig = GallerySortGroupConfig(GalleryGroupOption.MEDIA_TYPE, GallerySortOrder.SIZE_DESC)
    GalleryEngine.setFolderSortGroupConfig(directory = File(dcimDir, "Camera"), config = cameraFolderConfig)
    assertTrue(GalleryEngine.hasFolderSortGroupConfig(directory = File(dcimDir, "Camera")))
    assertEquals(cameraFolderConfig to true, GalleryEngine.resolveSortGroupConfig(folderKey = File(dcimDir, "Camera").absolutePath))
    assertEquals(customDefault to false, GalleryEngine.resolveSortGroupConfig(folderKey = picturesDir.absolutePath))

    GalleryEngine.removeFolderSortGroupConfig(directory = File(dcimDir, "Camera"))
    assertTrue(!GalleryEngine.hasFolderSortGroupConfig(directory = File(dcimDir, "Camera")))
    GalleryEngine.clearSortGroupConfigs()

    val photos = listOf(
      GalleryPhoto(photoFile, photoFile.name, photoFile.absolutePath, 300L, oneHourAgo, "jpg"),
      GalleryPhoto(videoFile, videoFile.name, videoFile.absolutePath, 900L, yesterdayMs, "mp4"),
      GalleryPhoto(gifFile, gifFile.name, gifFile.absolutePath, 200L, sixtyDaysAgoMs, "gif"),
      GalleryPhoto(screenshotFile, screenshotFile.name, screenshotFile.absolutePath, 400L, oneHourAgo - 1_000L, "png"),
    )

    val indexMap = mapOf(
      photoFile.absolutePath to GalleryPhotoIndexEntry(
        path = photoFile.absolutePath,
        lastModifiedMs = oneHourAgo,
        sizeBytes = 300L,
        autoTags = listOf("Nature"),
      ),
      screenshotFile.absolutePath to GalleryPhotoIndexEntry(
        path = screenshotFile.absolutePath,
        lastModifiedMs = oneHourAgo - 1_000L,
        sizeBytes = 400L,
        autoTags = listOf("Receipt", "Screenshot"),
      ),
    )

    // 1. NONE produces a single untitled flat group
    val noneGroups = GalleryEngine.groupPhotos(
      photos = photos,
      groupOption = GalleryGroupOption.NONE,
      sortOrder = GallerySortOrder.DATE_DESC,
    )
    assertEquals(1, noneGroups.size)
    assertEquals("", noneGroups.single().title)
    assertEquals(4, noneGroups.single().photos.size)

    // 2. DATE groups into Today, Yesterday, and Month Year
    val dateGroups = GalleryEngine.groupPhotos(
      photos = photos,
      groupOption = GalleryGroupOption.DATE,
      sortOrder = GallerySortOrder.DATE_DESC,
      nowMs = nowMs,
    )
    assertEquals("Today", dateGroups[0].title)
    assertEquals(2, dateGroups[0].photos.size)
    assertEquals("Yesterday", dateGroups[1].title)
    assertEquals(listOf("clip.mp4"), dateGroups[1].photos.map { it.name })
    assertEquals(3, dateGroups.size)

    // 3. MEDIA_TYPE groups into Photos, Videos, GIFs, and Screenshots
    val mediaTypeGroups = GalleryEngine.groupPhotos(
      photos = photos,
      groupOption = GalleryGroupOption.MEDIA_TYPE,
      sortOrder = GallerySortOrder.DATE_DESC,
      indexMap = indexMap,
    )
    assertEquals(
      listOf("Photos", "Videos", "GIFs", "Screenshots"),
      mediaTypeGroups.map { it.title },
    )

    // 4. SMART_TAGS groups by detected tags plus Untagged
    val smartTagGroups = GalleryEngine.groupPhotos(
      photos = photos,
      groupOption = GalleryGroupOption.SMART_TAGS,
      sortOrder = GallerySortOrder.DATE_DESC,
      indexMap = indexMap,
    )
    assertEquals(
      listOf("Receipt", "Screenshot", "Nature", "Untagged"),
      smartTagGroups.map { it.title },
    )
  }

  @Test
  fun samplePhotoColorHex_andMapViewportPointToImageNormalized_extractExactHexColorFromPhoto() {
    val dcimDir = File(tempRootDir, "DCIM").apply { mkdirs() }
    val coralPng = GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimDir, "coral.png"),
      red = 0xFF,
      green = 0x6B,
      blue = 0x57,
      width = 48,
      height = 48,
    )
    val tealPng = GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimDir, "teal.png"),
      red = 0x0D,
      green = 0x94,
      blue = 0x88,
      width = 48,
      height = 48,
    )

    assertEquals("#FF6B57", GalleryEngine.samplePhotoColorHex(file = coralPng, normX = 0.5f, normY = 0.5f))
    assertEquals("#0D9488", GalleryEngine.samplePhotoColorHex(file = tealPng, normX = 0.25f, normY = 0.75f))

    // Center of viewport maps to (0.5f, 0.5f) of fitted image
    val (normX, normY) = GalleryEngine.mapViewportPointToImageNormalized(
      viewportX = 200f,
      viewportY = 400f,
      containerWidth = 400f,
      containerHeight = 800f,
      imageWidth = 100,
      imageHeight = 100,
    )
    assertEquals(0.5f, normX, 0.001f)
    assertEquals(0.5f, normY, 0.001f)
  }

  @Test
  fun isAnimatedImageFile_andSupportsInlinePreview_detectAnimatedGifsAndVideos() {
    val dcimDir = File(tempRootDir, "DCIM").apply { mkdirs() }
    val gifFile = GalleryEngine.writeSampleAnimatedGif(
      outputFile = File(dcimDir, "dance.gif"),
      lastModifiedMs = 4_000_000L,
    )
    val pngFile = GalleryEngine.writeSolidColorPng(
      outputFile = File(dcimDir, "still.png"),
      lastModifiedMs = 3_000_000L,
    )
    val mp4File = File(dcimDir, "clip.mp4").apply {
      writeBytes(byteArrayOf(0, 0, 0, 24))
      setLastModified(5_000_000L)
    }

    assertTrue(GalleryEngine.isAnimatedImageFile(gifFile))
    assertTrue(!GalleryEngine.isAnimatedImageFile(pngFile))
    assertTrue(!GalleryEngine.isAnimatedImageFile(mp4File))
    assertTrue(GalleryEngine.isInlinePreviewMediaFile(gifFile))
    assertTrue(GalleryEngine.isInlinePreviewMediaFile(mp4File))
    assertTrue(!GalleryEngine.isInlinePreviewMediaFile(pngFile))

    val contents = GalleryEngine.loadAlbumContents(dcimDir)
    val photosByName = contents.photos.associateBy { it.name }
    assertTrue(photosByName.getValue("dance.gif").isAnimatedImage)
    assertTrue(photosByName.getValue("dance.gif").supportsInlinePreview)
    assertTrue(photosByName.getValue("clip.mp4").supportsInlinePreview)
    assertTrue(!photosByName.getValue("still.png").supportsInlinePreview)
  }

  @Test
  fun parseAndSerializeGrantedFolders_folderDocumentId_andReorderPillsWithAlbumsFixedAtFront() {
    assertEquals(listOf("DCIM", "Pictures", "Download"), DEFAULT_GALLERY_FOLDER_NAMES)
    assertEquals(listOf("/DCIM", "/Pictures", "/Download"), DEFAULT_GALLERY_FOLDER_DISPLAY_PATHS)
    assertEquals(listOf("Camera", "Pictures", "Download"), DEFAULT_GALLERY_FOLDERS.map { it.pillLabel })
    assertEquals(
      listOf(
        GalleryDefaultFolder.DCIM,
        GalleryDefaultFolder.PICTURES,
        GalleryDefaultFolder.DOWNLOADS,
      ),
      GalleryDefaultFolder.entries,
    )

    assertTrue(GalleryEngine.parseGrantedFolders(null).isEmpty())
    assertTrue(GalleryEngine.parseGrantedFolders("").isEmpty())
    assertTrue(GalleryEngine.parseGrantedFolders("   ").isEmpty())

    val parsed = GalleryEngine.parseGrantedFolders("DCIM, Download, /Pictures")
    assertEquals(
      setOf(
        GalleryDefaultFolder.DCIM,
        GalleryDefaultFolder.PICTURES,
        GalleryDefaultFolder.DOWNLOADS,
      ),
      parsed,
    )

    val serialized = GalleryEngine.serializeGrantedFolders(
      setOf(GalleryDefaultFolder.DOWNLOADS, GalleryDefaultFolder.DCIM)
    )
    assertEquals("DCIM,DOWNLOADS", serialized)
    assertEquals(
      setOf(GalleryDefaultFolder.DCIM, GalleryDefaultFolder.DOWNLOADS),
      GalleryEngine.parseGrantedFolders(serialized),
    )

    assertEquals("primary:DCIM", GalleryEngine.buildFolderDocumentId(GalleryDefaultFolder.DCIM))
    assertEquals("primary:Pictures", GalleryEngine.buildFolderDocumentId(GalleryDefaultFolder.PICTURES))
    assertEquals("primary:Download", GalleryEngine.buildFolderDocumentId(GalleryDefaultFolder.DOWNLOADS))

    assertEquals(
      GalleryDefaultFolder.DCIM,
      GalleryDefaultFolder.fromPathOrName("content://com.android.externalstorage.documents/tree/primary%3ADCIM"),
    )
    assertEquals(
      GalleryDefaultFolder.PICTURES,
      GalleryDefaultFolder.fromPathOrName("primary:Pictures"),
    )
    assertEquals(
      GalleryDefaultFolder.DOWNLOADS,
      GalleryDefaultFolder.fromPathOrName("primary:Download"),
    )

    // Reordering and persisting custom + default folder pills
    val customScreenshots = GalleryFolder(
      folderName = "Screenshots",
      displayPath = "/Screenshots",
      customAbsolutePath = "/storage/emulated/0/Screenshots",
    )
    assertTrue(customScreenshots.isCustom)
    assertTrue(!customScreenshots.isDefault)
    assertTrue(DEFAULT_GALLERY_FOLDERS.all { it.isDefault && !it.isCustom })

    val withCustom = DEFAULT_GALLERY_FOLDERS + customScreenshots
    val reordered = GalleryEngine.reorderFolders(withCustom, fromIndex = 3, toIndex = 0)
    assertEquals(listOf("Screenshots", "DCIM", "Pictures", "Download"), reordered.map { it.folderName })

    val serializedPills = GalleryEngine.serializeFolderPills(reordered)
    val parsedPills = GalleryEngine.parseFolderPills(serializedPills)
    assertEquals(reordered, parsedPills)

    // Removing a custom folder removes it while preserving default folders
    val afterRemoveCustom = GalleryEngine.removeCustomFolder(reordered, customScreenshots)
    assertEquals(listOf("DCIM", "Pictures", "Download"), afterRemoveCustom.map { it.folderName })
    assertEquals(
      afterRemoveCustom,
      GalleryEngine.removeCustomFolder(afterRemoveCustom, GalleryDefaultFolder.DCIM.toGalleryFolder()),
    )

    // Full navigation pill reordering keeps "Albums" fixed at the front of the list (index 0)
    val navPills = listOf("Albums", "DCIM", "Pictures", "Download")
    assertEquals(
      listOf("Albums", "DCIM", "Pictures", "Download"),
      GalleryEngine.reorderNavigationPills(navPills, fromIndex = 0, toIndex = 2),
    )
    assertEquals(
      listOf("Albums", "Pictures", "DCIM", "Download"),
      GalleryEngine.reorderNavigationPills(navPills, fromIndex = 2, toIndex = 0),
    )
    assertEquals(
      listOf("Albums", "Download", "DCIM", "Pictures"),
      GalleryEngine.reorderNavigationPills(navPills, fromIndex = 3, toIndex = 1),
    )
  }
}
