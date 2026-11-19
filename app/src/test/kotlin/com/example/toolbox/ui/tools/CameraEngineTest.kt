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

    // Optimal sensor preview size selection & TextureView center-crop scale (prevents vertical stretching)
    val supportedSensorSizes = listOf(
      Pair(3840, 2160),
      Pair(1920, 1440),
      Pair(1920, 1080),
      Pair(1440, 1080),
      Pair(1280, 720),
      Pair(640, 480),
    )
    assertEquals(
      Pair(1920, 1080),
      CameraEngine.selectOptimalSensorPreviewSize(
        supportedSensorSizes = supportedSensorSizes,
        viewWidth = 1080,
        viewHeight = 2400,
        sensorOrientationDegrees = 90,
        displayRotationDegrees = 0,
      ),
    )
    assertEquals(
      Pair(1920, 1440),
      CameraEngine.selectOptimalSensorPreviewSize(
        supportedSensorSizes = supportedSensorSizes,
        viewWidth = 1080,
        viewHeight = 1440,
        sensorOrientationDegrees = 90,
        displayRotationDegrees = 0,
      ),
    )

    // Full-screen 9:20 portrait view (1080x2400) with 16:9 sensor stream (1920x1080 rotated 90 deg -> 1080x1920)
    // expands X by 1.25x and keeps Y at 1.0x so the camera feed is center-cropped instead of stretched vertically.
    val (fullScaleX, fullScaleY) = CameraEngine.calculateTextureViewCenterCropScale(
      viewWidth = 1080,
      viewHeight = 2400,
      sensorPreviewWidth = 1920,
      sensorPreviewHeight = 1080,
      sensorOrientationDegrees = 90,
    )
    assertEquals(1.25f, fullScaleX, 0.001f)
    assertEquals(1.0f, fullScaleY, 0.001f)

    // 1:1 square view (1080x1080) with 4:3 sensor stream (1440x1080 rotated 90 deg -> 1080x1440)
    // keeps X at 1.0x and expands Y by 4/3 (1.333x) to center-crop top and bottom.
    val (squareScaleX, squareScaleY) = CameraEngine.calculateTextureViewCenterCropScale(
      viewWidth = 1080,
      viewHeight = 1080,
      sensorPreviewWidth = 1440,
      sensorPreviewHeight = 1080,
      sensorOrientationDegrees = 90,
    )
    assertEquals(1.0f, squareScaleX, 0.001f)
    assertEquals(4f / 3f, squareScaleY, 0.001f)

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

  @Test
  fun qrCodeAndBarcodeEncodingAndDecoding_roundTrips2DAnd1DFormats() {
    // 1. QR Code (Web URL)
    val qrPayload = "https://toolbox.example.com/qr-scanner?mode=test"
    val qrPixels = CameraEngine.renderCodeToArgbPixels(
      content = qrPayload,
      format = CameraBarcodeFormat.QR_CODE,
      width = 320,
      height = 320,
    )
    assertNotNull(qrPixels)
    val qrResults = CameraEngine.decodeCodesFromArgbPixels(
      pixels = qrPixels!!,
      width = 320,
      height = 320,
      filter = CameraScanFilter.QR_CODE,
    )
    assertEquals(1, qrResults.size)
    assertEquals(qrPayload, qrResults[0].rawValue)
    assertEquals(CameraBarcodeFormat.QR_CODE, qrResults[0].format)
    assertEquals(CameraScanPayloadType.URL, qrResults[0].payloadType)
    assertEquals("toolbox.example.com", qrResults[0].title)

    // Filter BARCODE should exclude QR_CODE
    val excludedByBarcodeFilter = CameraEngine.decodeCodesFromArgbPixels(
      pixels = qrPixels,
      width = 320,
      height = 320,
      filter = CameraScanFilter.BARCODE,
    )
    assertTrue(excludedByBarcodeFilter.isEmpty())

    // 2. Inverted (light-on-dark) QR Code decoding
    val invertedQrPixels = CameraEngine.renderCodeToArgbPixels(
      content = "WIFI:S:ToolboxNet;T:WPA;P:secret99;;",
      format = CameraBarcodeFormat.QR_CODE,
      width = 320,
      height = 320,
      darkColorArgb = 0xFFFFFFFF.toInt(),
      lightColorArgb = 0xFF000000.toInt(),
    )
    assertNotNull(invertedQrPixels)
    val invertedDecoded = CameraEngine.decodeCodesFromArgbPixels(
      pixels = invertedQrPixels!!,
      width = 320,
      height = 320,
      filter = CameraScanFilter.ALL,
      tryInverted = true,
    )
    assertEquals(1, invertedDecoded.size)
    assertEquals(CameraScanPayloadType.WIFI, invertedDecoded[0].payloadType)
    assertEquals("Wi-Fi: ToolboxNet", invertedDecoded[0].title)
    assertEquals("secret99", invertedDecoded[0].primaryCopyValue)

    // 3. EAN-13 Barcode (ISBN-13)
    val ean13Payload = "9780201379624"
    assertTrue(CameraEngine.verifyLinearBarcodeChecksum(ean13Payload))
    val eanPixels = CameraEngine.renderCodeToArgbPixels(
      content = ean13Payload,
      format = CameraBarcodeFormat.EAN_13,
      width = 420,
      height = 180,
    )
    assertNotNull(eanPixels)
    val eanResults = CameraEngine.decodeCodesFromArgbPixels(
      pixels = eanPixels!!,
      width = 420,
      height = 180,
      filter = CameraScanFilter.BARCODE,
    )
    assertEquals(1, eanResults.size)
    assertEquals(ean13Payload, eanResults[0].rawValue)
    assertEquals(CameraBarcodeFormat.EAN_13, eanResults[0].format)
    assertEquals(CameraScanPayloadType.ISBN, eanResults[0].payloadType)

    // 4. UPC-A & Code 128 & Code 39 Barcodes
    val code128Payload = "TOOLBOX-SCAN-2026"
    val code128Pixels = CameraEngine.renderCodeToArgbPixels(
      content = code128Payload,
      format = CameraBarcodeFormat.CODE_128,
      width = 440,
      height = 180,
    )
    assertNotNull(code128Pixels)
    val code128Decoded = CameraEngine.decodeCodesFromArgbPixels(
      pixels = code128Pixels!!,
      width = 440,
      height = 180,
      filter = CameraScanFilter.BARCODE,
    )
    assertEquals(1, code128Decoded.size)
    assertEquals(code128Payload, code128Decoded[0].rawValue)
    assertEquals(CameraBarcodeFormat.CODE_128, code128Decoded[0].format)
    assertEquals(CameraScanPayloadType.BARCODE_ID, code128Decoded[0].payloadType)
  }

  @Test
  fun saveAndDecodeBarcodeImageFile_persistsAndReadsPngOnDisk() {
    val barcodeFile = CameraEngine.createTimestampedBarcodeFile(
      directory = tempDir,
      format = CameraBarcodeFormat.UPC_A,
      epochMs = 1794335400000L,
    )
    CameraEngine.saveBarcodeImageFile(
      outputFile = barcodeFile,
      content = "012345678905",
      format = CameraBarcodeFormat.UPC_A,
      width = 440,
      height = 200,
    )
    assertTrue(barcodeFile.exists())
    assertTrue(barcodeFile.length() > 128L)

    val decodedFromFile = CameraEngine.decodeCodesFromImageFile(
      file = barcodeFile,
      filter = CameraScanFilter.ALL,
    )
    assertEquals(1, decodedFromFile.size)
    assertEquals("012345678905", decodedFromFile[0].rawValue)
    assertEquals(CameraBarcodeFormat.UPC_A, decodedFromFile[0].format)
    assertEquals(CameraScanPayloadType.PRODUCT, decodedFromFile[0].payloadType)
  }

  @Test
  fun payloadClassification_preservesAllMetadata() {
    val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:Jane Doe\nORG:DeepMind\nTEL:+1-555-0100\nEMAIL:jane@example.com\nEND:VCARD"
    val analyzedContact = CameraEngine.analyzeScannedPayload(
      rawValue = vcard,
      format = CameraBarcodeFormat.QR_CODE,
      scannedAtMs = 5000L,
    )
    assertEquals(CameraScanPayloadType.CONTACT, analyzedContact.payloadType)
    assertEquals("Contact: Jane Doe", analyzedContact.title)
    assertEquals("DeepMind", analyzedContact.fields["Organization"])
    assertEquals("+1-555-0100", analyzedContact.fields["Phone"])

    val analyzedEan = CameraEngine.analyzeScannedPayload(
      rawValue = "4006381333931",
      format = CameraBarcodeFormat.EAN_13,
      scannedAtMs = 6000L,
    )
    assertEquals(CameraScanPayloadType.PRODUCT, analyzedEan.payloadType)
    assertEquals("Germany", analyzedEan.fields["Origin / Prefix"])
  }

  @Test
  fun captureMode_supportsNoteModeResolution() {
    assertEquals(CameraCaptureMode.NOTE, CameraCaptureMode.fromNameOrDefault("NOTE"))
    assertEquals(CameraCaptureMode.NOTE, CameraCaptureMode.fromNameOrDefault("Note"))
    assertEquals(CameraCaptureMode.NOTE, CameraCaptureMode.fromNameOrDefault("ocr"))
    assertEquals("Note", CameraCaptureMode.NOTE.displayName)
    assertEquals("NOTE", CameraCaptureMode.NOTE.shortLabel)
  }

  @Test
  fun analyzeScannedDocumentText_andSaveToNotes_addsScannedNoteToNotesRepository() {
    val rawOcrText = """
      Sprint Architecture Review
      - Finalize on-device ML Kit OCR pipeline
      - Move adjustment and filter buttons to shutter row
      - Persist scanned document into NotesRepository
    """.trimIndent()

    val scannedDoc = CameraEngine.analyzeScannedDocumentText(
      rawText = rawOcrText,
      scannedAtMs = 1794335400000L,
    )
    assertEquals("Sprint Architecture Review", scannedDoc.title)
    assertTrue(scannedDoc.bodyContent.contains("Finalize on-device ML Kit OCR pipeline"))
    assertEquals(4, scannedDoc.lineCount)
    assertTrue(scannedDoc.wordCount >= 18)

    val notesRepo = InMemoryNotesRepository(initialNotes = emptyList())
    val (savedDoc, createdNote) = CameraEngine.saveScannedDocumentToNotes(
      repository = notesRepo,
      document = scannedDoc,
    )

    assertEquals(createdNote.id, savedDoc.savedNoteId)
    assertEquals("Sprint Architecture Review", createdNote.title)
    assertTrue(createdNote.content.contains("Finalize on-device ML Kit OCR pipeline"))
    assertTrue(createdNote.labels.contains("Scanned"))

    val storedNotes = notesRepo.loadNotes()
    assertEquals(1, storedNotes.size)
    assertEquals(createdNote.id, storedNotes.first().id)
    assertEquals("Sprint Architecture Review", storedNotes.first().title)
    assertEquals(createdNote.content, storedNotes.first().content)
  }

  @Test
  fun captureMode_supportsVisionModeAndMlKitTagAccuracyAnalysis() {
    assertEquals(CameraCaptureMode.VISION, CameraCaptureMode.fromNameOrDefault("VISION"))
    assertEquals(CameraCaptureMode.VISION, CameraCaptureMode.fromNameOrDefault("Vision"))
    assertEquals(CameraCaptureMode.VISION, CameraCaptureMode.fromNameOrDefault("tags"))
    assertEquals(CameraCaptureMode.VISION, CameraCaptureMode.fromNameOrDefault("mlkit"))
    assertEquals("Vision", CameraCaptureMode.VISION.displayName)
    assertEquals("VISION", CameraCaptureMode.VISION.shortLabel)

    val rawTags = CameraVisionSamplePreset.BOTANICAL.sampleTags
    val standardResult = CameraEngine.buildVisionAnalysisResult(
      rawTags = rawTags,
      confidenceThreshold = MIN_VISION_CONFIDENCE_THRESHOLD,
      inferenceLatencyMs = 18L,
      sourceDescription = "Indoor Monstera Plant",
    )
    // Plant (96.2%), Flowerpot (84.1%), and Leaf (75.8% nested sub-region with moderate overlap) are >= 65% and kept.
    // Houseplant (91.4%) has an identical duplicate box to Plant (96.2%) and is suppressed.
    // Room (58.2%) and Furniture (39.4%) are below 65% and are hidden.
    assertEquals(3, standardResult.tags.size)
    assertEquals(listOf("Plant", "Flowerpot", "Leaf"), standardResult.tags.map { it.label })
    assertTrue(standardResult.tags.all { it.confidence >= 0.65f && it.boundingBox != null })
    assertEquals(6, standardResult.allCandidateTags.size)
    assertEquals(2, standardResult.galleryPassingCount)
    assertEquals("Plant", standardResult.topTag?.label)
    assertEquals("96.2%", standardResult.topTag?.confidenceFormatted)
    assertTrue(standardResult.topTag?.passesGalleryThreshold == true)

    // Verify overlapping boxes arrange their labels in different corners so labels never overlap
    val placements = CameraEngine.computeNonOverlappingVisionLabelPlacements(
      tags = standardResult.tags,
      stageWidthPx = 420f,
      usableTopPx = 80f,
      usableBottomPx = 640f,
    )
    assertEquals(3, placements.size)
    for (i in placements.indices) {
      for (j in i + 1 until placements.size) {
        assertFalse(
          "Labels for ${placements[i].tag.label} and ${placements[j].tag.label} must not overlap",
          placements[i].overlapsWith(placements[j], paddingPx = 2f),
        )
      }
    }

    // Even when multiple boxes share nearly identical top-left coordinates, their labels are placed in different corners
    val heavilyOverlappingTags = listOf(
      CameraVisionTag("Monitor", 0.92f, boundingBox = CameraVisionBoundingBox(0.18f, 0.20f, 0.80f, 0.62f)),
      CameraVisionTag("Screen", 0.86f, boundingBox = CameraVisionBoundingBox(0.20f, 0.22f, 0.72f, 0.54f)),
      CameraVisionTag("Keyboard", 0.81f, boundingBox = CameraVisionBoundingBox(0.20f, 0.22f, 0.68f, 0.50f)),
      CameraVisionTag("Laptop", 0.78f, boundingBox = CameraVisionBoundingBox(0.18f, 0.20f, 0.76f, 0.58f)),
    )
    val cornerPlacements = CameraEngine.computeNonOverlappingVisionLabelPlacements(
      tags = heavilyOverlappingTags,
      stageWidthPx = 420f,
      usableTopPx = 80f,
      usableBottomPx = 640f,
    )
    assertEquals(4, cornerPlacements.size)
    assertTrue("Overlapping boxes use distinct corners", cornerPlacements.map { it.corner }.distinct().size >= 3)
    for (i in cornerPlacements.indices) {
      for (j in i + 1 until cornerPlacements.size) {
        assertFalse(cornerPlacements[i].overlapsWith(cornerPlacements[j], paddingPx = 2f))
      }
    }

    // Verify salient region extraction locates foreground object bounding box in pixel buffer
    val w = 64
    val h = 64
    val pixels = IntArray(w * h) { 0xFF101014.toInt() }
    for (y in 16 until 48) {
      for (x in 20 until 52) {
        pixels[y * w + x] = 0xFF34D399.toInt()
      }
    }
    val salientBoxes = CameraEngine.extractSalientRegionsFromArgbPixels(pixels, w, h, maxRegions = 2)
    assertTrue(salientBoxes.isNotEmpty())
    val primaryBox = salientBoxes.first()
    assertTrue(primaryBox.left >= 0.04f && primaryBox.right <= 0.96f)
    assertTrue(primaryBox.width >= 0.12f && primaryBox.height >= 0.10f)

    // Filtering with 80% Gallery threshold keeps the 2 non-overlapping >= 80% tags
    val galleryStrictResult = standardResult.withThreshold(CameraVisionThresholdPreset.GALLERY_80.minConfidence)
    assertEquals(2, galleryStrictResult.tags.size)
    assertTrue(galleryStrictResult.tags.all { it.passesGalleryThreshold })

    val report = galleryStrictResult.formatSummaryReport()
    assertTrue(report.contains("ML Kit Vision Tagging Report (Indoor Monstera Plant)"))
    assertTrue(report.contains("Plant: 96.2%"))
  }

  @Test
  fun sampleCenterRadiusColorHex_samplesCircularRadiusAroundFocusPoint() {
    val w = 32
    val h = 32
    // Fill background with dark navy (#102030) and a 9x9 center patch around (16, 16) with coral (#E65A46)
    val pixels = IntArray(w * h) { 0xFF102030.toInt() }
    for (y in 12..20) {
      for (x in 12..20) {
        pixels[y * w + x] = 0xFFE65A46.toInt()
      }
    }
    val sampledCenterHex = CameraEngine.sampleCenterRadiusColorHexFromArgbPixels(
      pixels = pixels,
      width = w,
      height = h,
      normX = 16f / 31f,
      normY = 16f / 31f,
      radiusPx = 3,
    )
    assertEquals("#E65A46", sampledCenterHex)

    val sampledCornerHex = CameraEngine.sampleCenterRadiusColorHexFromArgbPixels(
      pixels = pixels,
      width = w,
      height = h,
      normX = 0.05f,
      normY = 0.05f,
      radiusPx = 2,
    )
    assertEquals("#102030", sampledCornerHex)
  }

  @Test
  fun photoSubModes_andNightMode_resolveAndCapturePanoramaHdrBurstAndNightSight() {
    assertEquals(CameraPhotoSubMode.STANDARD, CameraPhotoSubMode.DEFAULT)
    assertEquals(CameraPhotoSubMode.PANORAMA, CameraPhotoSubMode.STANDARD.next())
    assertEquals(CameraPhotoSubMode.HDR, CameraPhotoSubMode.PANORAMA.next())
    assertEquals(CameraPhotoSubMode.BURST, CameraPhotoSubMode.HDR.next())
    assertEquals(CameraPhotoSubMode.STANDARD, CameraPhotoSubMode.BURST.next())

    assertEquals(CameraPhotoSubMode.PANORAMA, CameraPhotoSubMode.fromNameOrDefault("Panorama"))
    assertEquals(CameraPhotoSubMode.PANORAMA, CameraPhotoSubMode.fromNameOrDefault("pano"))
    assertEquals(CameraPhotoSubMode.HDR, CameraPhotoSubMode.fromNameOrDefault("HDR"))
    assertEquals(CameraPhotoSubMode.BURST, CameraPhotoSubMode.fromNameOrDefault("Burst"))
    // Removed 2x2 Booth and Night sub-modes fall back cleanly to STANDARD
    assertEquals(listOf(CameraPhotoSubMode.STANDARD, CameraPhotoSubMode.PANORAMA, CameraPhotoSubMode.HDR, CameraPhotoSubMode.BURST), CameraPhotoSubMode.entries)

    // Floating Night Mode cycles Auto -> On -> Off -> Auto
    assertEquals(CameraNightMode.AUTO, CameraNightMode.DEFAULT)
    assertEquals(CameraNightMode.ON, CameraNightMode.AUTO.next())
    assertEquals(CameraNightMode.OFF, CameraNightMode.ON.next())
    assertEquals(CameraNightMode.AUTO, CameraNightMode.OFF.next())
    assertEquals(CameraNightMode.ON, CameraNightMode.fromNameOrDefault("On"))
    assertEquals(CameraNightMode.OFF, CameraNightMode.fromNameOrDefault("Off"))
    assertEquals(CameraNightMode.AUTO, CameraNightMode.fromNameOrDefault("Auto"))

    assertTrue(CameraEngine.shouldApplyNightSight(CameraNightMode.ON, exposureEv = 0f))
    assertFalse(CameraEngine.shouldApplyNightSight(CameraNightMode.OFF, exposureEv = -1.5f, averageLuma = 20))
    assertTrue(CameraEngine.shouldApplyNightSight(CameraNightMode.AUTO, exposureEv = 0f, averageLuma = 45))
    assertFalse(CameraEngine.shouldApplyNightSight(CameraNightMode.AUTO, exposureEv = 0f, averageLuma = 140))

    val fixedEpoch = 1794335400000L
    // Panorama file capture
    val panoFile = CameraEngine.createTimestampedPanoramaFile(
      directory = tempDir,
      format = CameraPhotoFormat.PNG,
      epochMs = fixedEpoch,
    )
    assertTrue(panoFile.name.startsWith("PANO_"))
    CameraEngine.captureSyntheticPhotoFile(
      outputFile = panoFile,
      subMode = CameraPhotoSubMode.PANORAMA,
      nightMode = CameraNightMode.ON,
      epochMs = fixedEpoch,
    )
    assertTrue(panoFile.exists() && panoFile.length() > 32L)

    // HDR and Night Sight tone-mapping
    val (baseR, _, _) = CameraEngine.applyFilterAndExposureToRgb(40, 40, 40, CameraColorFilter.NONE, 0f)
    val (hdrR, _, _) = CameraEngine.applyHdrToneMapToRgb(40, 40, 40, CameraColorFilter.NONE, 0f)
    val (nightR, _, _) = CameraEngine.applyNightSightToRgb(40, 40, 40, CameraColorFilter.NONE, 0f)
    assertTrue("HDR lifts dark shadows", hdrR > baseR)
    assertTrue("Night Sight significantly lifts low-light shadows", nightR > hdrR)

    // Burst file creation (3 files)
    val burstFiles = CameraEngine.createTimestampedBurstPhotoFiles(
      directory = tempDir,
      format = CameraPhotoFormat.JPG,
      burstCount = 3,
      epochMs = fixedEpoch,
    )
    assertEquals(3, burstFiles.size)
    assertTrue(burstFiles.all { it.name.startsWith("BURST_") && it.name.endsWith(".jpg") })
  }

  @Test
  fun systemCameraCatalog_detectsMultipleBackAndFrontLenses_andCyclesBetweenThem() {
    val rawCameras = listOf(
      // Main Wide Back Camera (ID 0, ~27mm 35mm-eq)
      RawCameraCharacteristicsInfo(
        cameraId = "0",
        openCameraId = "0",
        lensFacing = CameraLensFacing.BACK,
        focalLengthMm = 4.8f,
        sensorWidthMm = 6.4f,
        sensorHeightMm = 4.8f,
        aperture = 1.8f,
        hasFlash = true,
      ),
      // Front Selfie Camera (ID 1, ~29mm 35mm-eq)
      RawCameraCharacteristicsInfo(
        cameraId = "1",
        openCameraId = "1",
        lensFacing = CameraLensFacing.FRONT,
        focalLengthMm = 3.2f,
        sensorWidthMm = 4.0f,
        sensorHeightMm = 3.0f,
        aperture = 2.0f,
        hasFlash = false,
      ),
      // Ultra-Wide Back Camera (ID 2, ~14mm 35mm-eq -> 0.5x optical zoom)
      RawCameraCharacteristicsInfo(
        cameraId = "2",
        openCameraId = "0",
        physicalCameraId = "2",
        lensFacing = CameraLensFacing.BACK,
        focalLengthMm = 1.85f,
        sensorWidthMm = 4.8f,
        sensorHeightMm = 3.6f,
        aperture = 2.2f,
        hasFlash = false,
      ),
      // Telephoto Back Camera (ID 3, ~81mm 35mm-eq -> 3x optical zoom)
      RawCameraCharacteristicsInfo(
        cameraId = "3",
        openCameraId = "3",
        lensFacing = CameraLensFacing.BACK,
        focalLengthMm = 10.8f,
        sensorWidthMm = 4.8f,
        sensorHeightMm = 3.6f,
        aperture = 2.8f,
        hasFlash = false,
      ),
      // Macro Back Camera (ID 4, high diopter close focus)
      RawCameraCharacteristicsInfo(
        cameraId = "4",
        openCameraId = "4",
        lensFacing = CameraLensFacing.BACK,
        focalLengthMm = 4.8f,
        sensorWidthMm = 6.4f,
        sensorHeightMm = 4.8f,
        aperture = 2.4f,
        minFocusDistanceDiopters = 25.0f,
      ),
    )

    val catalog = CameraEngine.buildSystemCameraCatalog(rawCameras)
    assertEquals(5, catalog.size)

    // Back cameras are sorted by ascending optical zoom (0.5x Ultra -> 1x Wide -> 1x Macro -> 3x Tele), followed by Front
    assertEquals(listOf("2", "0", "4", "3", "1"), catalog.map { it.cameraId })
    assertEquals(CameraLensType.ULTRA_WIDE, catalog[0].lensType)
    assertEquals("0.5x Ultra", catalog[0].chipLabel)
    assertEquals("0", catalog[0].openCameraId)
    assertEquals("2", catalog[0].physicalCameraId)

    assertEquals(CameraLensType.WIDE, catalog[1].lensType)
    assertTrue(catalog[1].isPrimaryForFacing)
    assertEquals("1x Wide", catalog[1].chipLabel)
    assertEquals("Back • Wide 1x", catalog[1].shortLabel)

    assertEquals(CameraLensType.MACRO, catalog[2].lensType)
    assertEquals("1x Macro", catalog[2].chipLabel)

    assertEquals(CameraLensType.TELEPHOTO, catalog[3].lensType)
    assertEquals("3x Tele", catalog[3].chipLabel)
    assertEquals("Back • Tele 3x", catalog[3].shortLabel)

    assertEquals(CameraLensFacing.FRONT, catalog[4].lensFacing)
    assertEquals("Front", catalog[4].chipLabel)

    // Default selected camera resolves to the primary 1x Wide back camera (ID 0), not the 0.5x ultra-wide
    val defaultBack = CameraEngine.resolveSelectedCamera(catalog, requestedCameraId = null, fallbackFacing = CameraLensFacing.BACK)
    assertEquals("0", defaultBack.cameraId)

    // Cycling through all system cameras visits every back and front camera in order and wraps around
    val step1 = CameraEngine.nextSystemCamera(catalog, defaultBack.cameraId)
    assertEquals("4", step1.cameraId)
    val step2 = CameraEngine.nextSystemCamera(catalog, step1.cameraId)
    assertEquals("3", step2.cameraId)
    val step3 = CameraEngine.nextSystemCamera(catalog, step2.cameraId)
    assertEquals("1", step3.cameraId)
    val step4 = CameraEngine.nextSystemCamera(catalog, step3.cameraId)
    assertEquals("2", step4.cameraId)
    val step5 = CameraEngine.nextSystemCamera(catalog, step4.cameraId)
    assertEquals("0", step5.cameraId)
  }
}
