package com.example.toolbox.ui.tools

import java.io.File
import java.nio.file.Files
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class CameraEngineTest {

  private lateinit var tempDir: File

  @Before
  fun setUp() {
    tempDir = Files.createTempDirectory("camera_engine_test").toFile()
  }

  @After
  fun tearDown() {
    tempDir.deleteRecursively()
  }

  @Test
  fun zoomCoercionAndFormatting_clampsAndFormatsCorrectly() {
    assertEquals(1.0f, CameraEngine.coerceZoom(0.2f))
    assertEquals(1.0f, CameraEngine.coerceZoom(Float.NaN))
    assertEquals(2.5f, CameraEngine.coerceZoom(2.5f))
    assertEquals(8.0f, CameraEngine.coerceZoom(15.0f))

    assertEquals("1x", CameraEngine.formatZoomLabel(1.0f))
    assertEquals("2x", CameraEngine.formatZoomLabel(2.0f))
    assertEquals("2.5x", CameraEngine.formatZoomLabel(2.52f))
    assertEquals("8x", CameraEngine.formatZoomLabel(9.0f))

    // Pinch zoom
    assertEquals(2.0f, CameraEngine.calculatePinchZoom(1.0f, 2.0f))
    assertEquals(1.0f, CameraEngine.calculatePinchZoom(2.0f, 0.25f))
    assertEquals(8.0f, CameraEngine.calculatePinchZoom(4.0f, 3.0f))
  }

  @Test
  fun exposureEvCoercionAndFormatting_handlesNegativeZeroAndPositiveValues() {
    assertEquals(-2.0f, CameraEngine.coerceExposureEv(-5.0f))
    assertEquals(0.0f, CameraEngine.coerceExposureEv(Float.NaN))
    assertEquals(1.3f, CameraEngine.coerceExposureEv(1.34f))
    assertEquals(2.0f, CameraEngine.coerceExposureEv(4.0f))

    assertEquals("0.0 EV", CameraEngine.formatExposureEvLabel(0f))
    assertEquals("+1.5 EV", CameraEngine.formatExposureEvLabel(1.5f))
    assertEquals("-1.2 EV", CameraEngine.formatExposureEvLabel(-1.2f))
  }

  @Test
  fun enums_cycleAndResolveFromNameOrDefault() {
    // Lens facing
    assertEquals(CameraLensFacing.FRONT, CameraLensFacing.BACK.toggle())
    assertEquals(CameraLensFacing.BACK, CameraLensFacing.FRONT.toggle())
    assertEquals(CameraLensFacing.FRONT, CameraLensFacing.fromNameOrDefault("front"))
    assertEquals(CameraLensFacing.BACK, CameraLensFacing.fromNameOrDefault("invalid"))

    // Flash mode
    assertEquals(CameraFlashMode.AUTO, CameraFlashMode.OFF.next())
    assertEquals(CameraFlashMode.ON, CameraFlashMode.AUTO.next())
    assertEquals(CameraFlashMode.TORCH, CameraFlashMode.ON.next())
    assertEquals(CameraFlashMode.OFF, CameraFlashMode.TORCH.next())

    // Timer duration
    assertEquals(CameraTimerDuration.THREE_SEC, CameraTimerDuration.OFF.next())
    assertEquals(CameraTimerDuration.FIVE_SEC, CameraTimerDuration.THREE_SEC.next())
    assertEquals(CameraTimerDuration.TEN_SEC, CameraTimerDuration.FIVE_SEC.next())
    assertEquals(CameraTimerDuration.OFF, CameraTimerDuration.TEN_SEC.next())
    assertEquals(CameraTimerDuration.FIVE_SEC, CameraTimerDuration.fromNameOrDefault("5s"))

    // Aspect ratio
    assertEquals(CameraAspectRatio.FULL, CameraAspectRatio.DEFAULT)
    assertEquals(CameraAspectRatio.FULL, CameraAspectRatio.fromNameOrDefault(null))
    assertEquals(CameraAspectRatio.RATIO_16_9, CameraAspectRatio.RATIO_4_3.next())
    assertEquals(CameraAspectRatio.RATIO_1_1, CameraAspectRatio.RATIO_16_9.next())
    assertEquals(CameraAspectRatio.FULL, CameraAspectRatio.RATIO_1_1.next())
    assertEquals(CameraAspectRatio.RATIO_4_3, CameraAspectRatio.FULL.next())
    assertEquals(Pair(1080, 1440), CameraAspectRatio.RATIO_4_3.resolveDimensions(1440))
    assertEquals(Pair(810, 1440), CameraAspectRatio.RATIO_16_9.resolveDimensions(1440))
    assertEquals(Pair(1440, 1440), CameraAspectRatio.RATIO_1_1.resolveDimensions(1440))

    // Viewfinder dimensions (full viewport by default, shrinks when aspect ratio does not match screen)
    assertEquals(
      Pair(400f, 800f),
      CameraEngine.calculateViewfinderDimensions(400f, 800f, CameraAspectRatio.FULL),
    )
    assertEquals(
      Pair(300f, 400f),
      CameraEngine.calculateViewfinderDimensions(300f, 800f, CameraAspectRatio.RATIO_4_3),
    )
    assertEquals(
      Pair(400f, 400f),
      CameraEngine.calculateViewfinderDimensions(400f, 800f, CameraAspectRatio.RATIO_1_1),
    )
    assertEquals(
      Pair(600f, 800f),
      CameraEngine.calculateViewfinderDimensions(700f, 800f, CameraAspectRatio.RATIO_4_3),
    )

    // Grid overlay
    assertEquals(CameraGridOverlay.RULE_OF_THIRDS, CameraGridOverlay.NONE.next())
    assertEquals(CameraGridOverlay.GOLDEN_RATIO, CameraGridOverlay.RULE_OF_THIRDS.next())
    assertEquals(CameraGridOverlay.CROSSHAIR, CameraGridOverlay.GOLDEN_RATIO.next())
    assertEquals(CameraGridOverlay.NONE, CameraGridOverlay.CROSSHAIR.next())
    assertTrue(CameraEngine.getGridFractions(CameraGridOverlay.NONE).isEmpty())
    assertEquals(2, CameraEngine.getGridFractions(CameraGridOverlay.RULE_OF_THIRDS).size)
    assertEquals(2, CameraEngine.getGridFractions(CameraGridOverlay.GOLDEN_RATIO).size)
    assertEquals(listOf(0.5f), CameraEngine.getGridFractions(CameraGridOverlay.CROSSHAIR))
  }

  @Test
  fun createTimestampedPhotoAndVideoFiles_avoidsCollisions() {
    val fixedEpochMs = 1794335400000L
    val photo1 = CameraEngine.createTimestampedPhotoFile(
      directory = tempDir,
      format = CameraPhotoFormat.JPG,
      epochMs = fixedEpochMs,
    )
    assertTrue(photo1.name.startsWith("IMG_"))
    assertTrue(photo1.name.endsWith(".jpg"))
    photo1.writeText("stub")

    val photo2 = CameraEngine.createTimestampedPhotoFile(
      directory = tempDir,
      format = CameraPhotoFormat.JPG,
      epochMs = fixedEpochMs,
    )
    assertTrue(photo2.name.endsWith("_1.jpg"))

    val video1 = CameraEngine.createTimestampedVideoFile(
      directory = tempDir,
      epochMs = fixedEpochMs,
    )
    assertTrue(video1.name.startsWith("VID_"))
    assertTrue(video1.name.endsWith(".mp4"))
    video1.writeText("stub")

    val video2 = CameraEngine.createTimestampedVideoFile(
      directory = tempDir,
      epochMs = fixedEpochMs,
    )
    assertTrue(video2.name.endsWith("_1.mp4"))
  }

  @Test
  fun directoryResolutionAndDisplayFormatting_resolvesRelativeToUserRoot() {
    val fakeRoot = File(tempDir, "emulated_0").apply { mkdirs() }
    val resolvedDefault = CameraEngine.resolveDirectoryInput("", userRoot = fakeRoot)
    assertEquals(File(fakeRoot, "DCIM/Camera").absolutePath, resolvedDefault.absolutePath)
    assertEquals("/DCIM/Camera", CameraEngine.formatDirectoryDisplayPath(resolvedDefault, fakeRoot))

    val resolvedCustom = CameraEngine.resolveDirectoryInput("/Photos/Snaps", userRoot = fakeRoot)
    assertEquals(File(fakeRoot, "Photos/Snaps").absolutePath, resolvedCustom.absolutePath)
    assertEquals("/Photos/Snaps", CameraEngine.formatDirectoryDisplayPath(resolvedCustom, fakeRoot))
  }

  @Test
  fun colorFiltersAndExposure_transformRgbValuesPredictably() {
    // Mono converts RGB into equal grayscale components
    val (mr, mg, mb) = CameraEngine.applyFilterAndExposureToRgb(
      red = 200,
      green = 100,
      blue = 50,
      filter = CameraColorFilter.MONO,
      exposureEv = 0f,
    )
    assertEquals(mr, mg)
    assertEquals(mg, mb)

    // Warm boosts red relative to blue
    val (wr, _, wb) = CameraEngine.applyFilterAndExposureToRgb(
      red = 120,
      green = 120,
      blue = 120,
      filter = CameraColorFilter.WARM,
      exposureEv = 0f,
    )
    assertTrue(wr > wb)

    // Cool boosts blue relative to red
    val (cr, _, cb) = CameraEngine.applyFilterAndExposureToRgb(
      red = 120,
      green = 120,
      blue = 120,
      filter = CameraColorFilter.COOL,
      exposureEv = 0f,
    )
    assertTrue(cb > cr)

    // Positive exposure increases brightness
    val (baseR, _, _) = CameraEngine.applyFilterAndExposureToRgb(100, 100, 100, CameraColorFilter.NONE, 0f)
    val (brightR, _, _) = CameraEngine.applyFilterAndExposureToRgb(100, 100, 100, CameraColorFilter.NONE, 1.5f)
    val (darkR, _, _) = CameraEngine.applyFilterAndExposureToRgb(100, 100, 100, CameraColorFilter.NONE, -1.5f)
    assertTrue(brightR > baseR)
    assertTrue(darkR < baseR)
  }

  @Test
  fun captureSyntheticPhotoAndVideo_writesValidFilesAndListsNewestFirst() {
    val photoFile = File(tempDir, "IMG_20261110_100000.png")
    CameraEngine.captureSyntheticPhotoFile(
      outputFile = photoFile,
      lensFacing = CameraLensFacing.BACK,
      aspectRatio = CameraAspectRatio.RATIO_4_3,
      filter = CameraColorFilter.VIVID,
      zoom = 2.0f,
      exposureEv = 0.5f,
      epochMs = 1000L,
    )
    assertTrue(photoFile.exists())
    assertTrue(photoFile.length() > 32L)
    assertTrue(CameraEngine.isCameraMediaFile(photoFile))
    assertFalse(CameraEngine.isVideoFile(photoFile))

    val videoFile = File(tempDir, "VID_20261110_100500.mp4")
    val session = ActiveCameraVideoSession(outputFile = videoFile)
    assertTrue(session.start())
    val stoppedFile = session.stop()
    stoppedFile.setLastModified(2000L)
    assertTrue(stoppedFile.exists())
    assertTrue(stoppedFile.length() > 1024L)
    assertTrue(CameraEngine.isVideoFile(stoppedFile))

    val listed = CameraEngine.listCapturedMedia(tempDir)
    assertEquals(2, listed.size)
    // Video has newer timestamp (2000L vs 1000L), so it is first
    assertEquals("VID_20261110_100500.mp4", listed[0].name)
    assertTrue(listed[0].isVideo)
    assertEquals("IMG_20261110_100000.png", listed[1].name)
    assertFalse(listed[1].isVideo)
  }
}
