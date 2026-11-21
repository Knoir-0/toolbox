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

    val resolvedFromTree = CameraEngine.resolveDisplayPathFromTreeUriString(
      rawUriString = "content://com.android.externalstorage.documents/tree/primary%3ADCIM%2FCamera",
      userRoot = fakeRoot,
    )
    assertEquals("/DCIM/Camera", resolvedFromTree)
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

    assertEquals(0.80f, MIN_VISION_CONFIDENCE_THRESHOLD)
    assertEquals(GalleryEngine.DEFAULT_ML_KIT_LABEL_CONFIDENCE_THRESHOLD, MIN_VISION_CONFIDENCE_THRESHOLD)

    val rawTags = CameraVisionSamplePreset.BOTANICAL.sampleTags
    val standardResult = CameraEngine.buildVisionAnalysisResult(
      rawTags = rawTags,
      confidenceThreshold = MIN_VISION_CONFIDENCE_THRESHOLD,
      inferenceLatencyMs = 18L,
      sourceDescription = "Indoor Monstera Plant",
    )
    // Plant (96.2%), Flowerpot (89.1%), and Leaf (86.4% nested sub-region with moderate overlap) are >= 80% and kept.
    // Houseplant (91.4%) has an identical duplicate box to Plant (96.2%) and is suppressed.
    // Room (58.2%) and Furniture (39.4%) are below 80% and are hidden.
    assertEquals(3, standardResult.tags.size)
    assertEquals(listOf("Plant", "Flowerpot", "Leaf"), standardResult.tags.map { it.label })
    assertTrue(standardResult.tags.all { it.confidence >= 0.80f && it.boundingBox != null })
    assertEquals(6, standardResult.allCandidateTags.size)
    assertEquals(3, standardResult.galleryPassingCount)
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

    // Filtering with linked 80% Gallery threshold keeps the 3 non-overlapping >= 80% tags
    val galleryStrictResult = standardResult.withThreshold(CameraVisionThresholdPreset.GALLERY_80.minConfidence)
    assertEquals(3, galleryStrictResult.tags.size)
    assertTrue(galleryStrictResult.tags.all { it.passesGalleryThreshold })

    // Filtering with 90% High preset keeps only Plant (96.2%)
    val high90Result = standardResult.withThreshold(CameraVisionThresholdPreset.HIGH_70.minConfidence)
    assertEquals(1, high90Result.tags.size)
    assertEquals(listOf("Plant"), high90Result.tags.map { it.label })

    val report = galleryStrictResult.formatSummaryReport()
    assertTrue(report.contains("ML Kit Vision Tagging Report (Indoor Monstera Plant)"))
    assertTrue(report.contains("Plant: 96.2%"))
    assertTrue(report.contains("[GALLERY ≥80%]"))

    // Verify generic/abstract ontology labels are blocked and verbose ML Kit classes map to canonical nouns
    assertTrue(CameraEngine.isBlockedGenericVisionLabel("Font"))
    assertTrue(CameraEngine.isBlockedGenericVisionLabel("Rectangle"))
    assertTrue(CameraEngine.isBlockedGenericVisionLabel("Material property"))
    assertTrue(CameraEngine.isBlockedGenericVisionLabel("Room"))
    assertTrue(CameraEngine.isBlockedGenericVisionLabel("Furniture"))
    assertTrue(CameraEngine.isBlockedGenericVisionLabel("Tableware"))
    assertTrue(CameraEngine.isBlockedGenericVisionLabel("Home good"))
    assertTrue(CameraEngine.isBlockedGenericVisionLabel("Fashion good"))
    assertEquals(null, CameraEngine.normalizeAndFilterMlKitLabel("Tint and shade"))
    assertEquals("Cat", CameraEngine.normalizeAndFilterMlKitLabel("Felidae"))
    assertEquals("Dog", CameraEngine.normalizeAndFilterMlKitLabel("Canidae"))
    assertEquals("Laptop", CameraEngine.normalizeAndFilterMlKitLabel("Netbook"))
    assertEquals("Smartphone", CameraEngine.normalizeAndFilterMlKitLabel("Mobile phone"))

    // Verify generic parent hypernyms ("Food", "Electronics") are suppressed when specific child labels exist
    val hypernymFiltered = CameraEngine.suppressGenericHypernymVisionTags(
      listOf(
        CameraVisionTag("Food", 0.94f),
        CameraVisionTag("Coffee cup", 0.89f),
        CameraVisionTag("Electronics", 0.91f),
        CameraVisionTag("Laptop", 0.88f),
      )
    )
    assertEquals(listOf("Coffee cup", "Laptop"), hypernymFiltered.map { it.label })
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

    // Back and front camera selector lists are separated by facing direction
    val backCameras = CameraEngine.getCamerasForFacing(catalog, CameraLensFacing.BACK)
    val frontCameras = CameraEngine.getCamerasForFacing(catalog, CameraLensFacing.FRONT)
    assertEquals(listOf("2", "0", "4", "3"), backCameras.map { it.cameraId })
    assertEquals(listOf("1"), frontCameras.map { it.cameraId })

    // Primary camera resolution for each facing direction
    assertEquals("0", CameraEngine.resolvePrimaryCameraForFacing(catalog, CameraLensFacing.BACK).cameraId)
    assertEquals("1", CameraEngine.resolvePrimaryCameraForFacing(catalog, CameraLensFacing.FRONT).cameraId)

    // Front/back switch button toggles between the primary front camera (ID 1) and primary back camera (ID 0)
    // regardless of which back lens (0.5x Ultra, 1x Wide, or 3x Tele) was active
    val toggledFromWideBack = CameraEngine.togglePrimaryFrontBackCamera(catalog, currentCameraId = "0")
    assertEquals("1", toggledFromWideBack.cameraId)
    val toggledFromTeleBack = CameraEngine.togglePrimaryFrontBackCamera(catalog, currentCameraId = "3")
    assertEquals("1", toggledFromTeleBack.cameraId)
    val toggledFromUltraBack = CameraEngine.togglePrimaryFrontBackCamera(catalog, currentCameraId = "2")
    assertEquals("1", toggledFromUltraBack.cameraId)
    val toggledBackToPrimaryBack = CameraEngine.togglePrimaryFrontBackCamera(catalog, currentCameraId = "1")
    assertEquals("0", toggledBackToPrimaryBack.cameraId)

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

  @Test
  fun smartObjectAutofocus_andManual5sFocusOverride_prioritizeObjectsAndRespect5SecondOverrideWindow() {
    assertEquals(5000L, MANUAL_FOCUS_OVERRIDE_DURATION_MS)

    // 1. ML Kit Object Detection candidate scoring prioritizes prominent central subjects over peripheral specks or background boxes
    val peripheralTinySpeck = RawDetectedObjectForFocus(
      boundingBox = CameraVisionBoundingBox(0.02f, 0.02f, 0.08f, 0.08f),
      trackingId = 10,
      label = "Label",
      confidence = 0.65f,
    )
    val centralSubject = RawDetectedObjectForFocus(
      boundingBox = CameraVisionBoundingBox(0.28f, 0.26f, 0.72f, 0.70f),
      trackingId = 42,
      label = "Plant",
      confidence = 0.94f,
    )
    val offCenterSecondary = RawDetectedObjectForFocus(
      boundingBox = CameraVisionBoundingBox(0.62f, 0.58f, 0.90f, 0.86f),
      trackingId = 77,
      label = "Flowerpot",
      confidence = 0.85f,
    )
    val hugeBackground = RawDetectedObjectForFocus(
      boundingBox = CameraVisionBoundingBox(0.0f, 0.0f, 0.99f, 0.99f),
      trackingId = 99,
      label = "Room",
      confidence = 0.95f,
    )

    // Huge background (> 92% area) is rejected with negative score
    assertTrue(CameraEngine.scoreObjectForSmartAutofocus(hugeBackground) < 0f)

    val selectedTarget = CameraEngine.selectSmartAutofocusTargetFromObjects(
      objects = listOf(peripheralTinySpeck, offCenterSecondary, centralSubject, hugeBackground),
      previousTarget = null,
      timestampMs = 1000L,
    )
    assertNotNull(selectedTarget)
    assertEquals(CameraFocusSource.ML_OBJECT_AUTO, selectedTarget!!.source)
    assertTrue(selectedTarget.isSmartObjectFocus)
    assertFalse(selectedTarget.isManualOverride)
    assertEquals(42, selectedTarget.trackingId)
    assertEquals("Plant", selectedTarget.categoryLabel)
    assertEquals("Smart AF • Plant", selectedTarget.badgeLabel)
    assertEquals(0.50f, selectedTarget.normX, 0.01f)
    assertEquals(0.48f, selectedTarget.normY, 0.01f)

    // 2. Temporal stability & jitter smoothing when tracking the same object across consecutive frames
    val microJitterFrame = RawDetectedObjectForFocus(
      boundingBox = CameraVisionBoundingBox(0.285f, 0.265f, 0.725f, 0.705f),
      trackingId = 42,
      label = "Plant",
      confidence = 0.93f,
    )
    val stabilizedTarget = CameraEngine.selectSmartAutofocusTargetFromObjects(
      objects = listOf(microJitterFrame),
      previousTarget = selectedTarget,
      timestampMs = 1350L,
    )
    assertNotNull(stabilizedTarget)
    // Micro-jitter (< 2% movement) holds steady at previous coordinates
    assertEquals(selectedTarget.normX, stabilizedTarget!!.normX, 0.001f)
    assertEquals(selectedTarget.normY, stabilizedTarget.normY, 0.001f)

    // 3. Manual tap-to-focus override for 5 seconds (5000ms) before returning to smart autofocus
    val tapTimeMs = 2000L
    val manualTarget = CameraEngine.createManualFocusTarget(
      normX = 0.78f,
      normY = 0.32f,
      timestampMs = tapTimeMs,
    )
    assertEquals(CameraFocusSource.MANUAL_OVERRIDE, manualTarget.source)
    assertTrue(manualTarget.isManualOverride)
    assertFalse(manualTarget.isSmartObjectFocus)
    assertEquals("Manual • 5s", manualTarget.badgeLabel)

    // At t = 2000ms (0s elapsed), manual override is active with 5000ms remaining
    assertTrue(CameraEngine.isManualFocusOverrideActive(tapTimeMs, nowMs = 2000L))
    assertEquals(5000L, CameraEngine.remainingManualFocusOverrideMs(tapTimeMs, nowMs = 2000L))
    val resolvedAt0s = CameraEngine.resolveActiveFocusTarget(
      manualTarget = manualTarget,
      smartAutoTarget = selectedTarget,
      nowMs = 2000L,
    )
    assertEquals(CameraFocusSource.MANUAL_OVERRIDE, resolvedAt0s?.source)
    assertEquals(0.78f, resolvedAt0s?.normX ?: 0f, 0.001f)
    assertEquals(0.32f, resolvedAt0s?.normY ?: 0f, 0.001f)

    // At t = 5500ms (3.5s elapsed — past the old 2.8s timeout, still within 5s window), manual override is STILL active
    assertTrue(CameraEngine.isManualFocusOverrideActive(tapTimeMs, nowMs = 5500L))
    assertEquals(1500L, CameraEngine.remainingManualFocusOverrideMs(tapTimeMs, nowMs = 5500L))
    val resolvedAt3_5s = CameraEngine.resolveActiveFocusTarget(
      manualTarget = manualTarget,
      smartAutoTarget = selectedTarget,
      nowMs = 5500L,
    )
    assertEquals(CameraFocusSource.MANUAL_OVERRIDE, resolvedAt3_5s?.source)
    assertEquals(0.78f, resolvedAt3_5s?.normX ?: 0f, 0.001f)

    // At t = 6999ms (4.999s elapsed), manual override is STILL active
    assertTrue(CameraEngine.isManualFocusOverrideActive(tapTimeMs, nowMs = 6999L))
    assertEquals(1L, CameraEngine.remainingManualFocusOverrideMs(tapTimeMs, nowMs = 6999L))

    // At t = 7000ms (5.0s elapsed), manual override expires and automatically returns to ML Kit smart object autofocus
    assertFalse(CameraEngine.isManualFocusOverrideActive(tapTimeMs, nowMs = 7000L))
    assertEquals(0L, CameraEngine.remainingManualFocusOverrideMs(tapTimeMs, nowMs = 7000L))
    val resolvedAfter5s = CameraEngine.resolveActiveFocusTarget(
      manualTarget = manualTarget,
      smartAutoTarget = selectedTarget,
      nowMs = 7000L,
    )
    assertNotNull(resolvedAfter5s)
    assertEquals(CameraFocusSource.ML_OBJECT_AUTO, resolvedAfter5s?.source)
    assertEquals(selectedTarget.normX, resolvedAfter5s?.normX ?: 0f, 0.001f)
    assertEquals(selectedTarget.normY, resolvedAfter5s?.normY ?: 0f, 0.001f)
    assertEquals("Plant", resolvedAfter5s?.categoryLabel)

    // 4. Camera2 sensor metering region calculation for smart object bounding boxes and manual focus points
    val smartMetering = CameraEngine.computeSensorMeteringRegion(
      sensorWidth = 4000,
      sensorHeight = 3000,
      target = selectedTarget,
      sensorOrientationDegrees = 90,
      lensFacing = CameraLensFacing.BACK,
      zoom = 1.0f,
    )
    assertNotNull(smartMetering)
    assertTrue(smartMetering!!.left >= 0 && smartMetering.right <= 4000)
    assertTrue(smartMetering.top >= 0 && smartMetering.bottom <= 3000)
    assertTrue(smartMetering.width >= 16 && smartMetering.height >= 16)
    assertEquals(1000, smartMetering.weight)

    val manualMetering = CameraEngine.computeSensorMeteringRegion(
      sensorWidth = 4000,
      sensorHeight = 3000,
      target = manualTarget,
      sensorOrientationDegrees = 90,
      lensFacing = CameraLensFacing.BACK,
      zoom = 2.0f,
    )
    assertNotNull(manualMetering)
    assertTrue(manualMetering!!.left >= 0 && manualMetering.right <= 4000)
    assertTrue(manualMetering.top >= 0 && manualMetering.bottom <= 3000)
  }

  @Test
  fun stickyAutofocus_resistsTransientDistractors_andTracesGentleVelocityAwareTrajectory() {
    // 1. Acquire initial focus on a subject on the left side of the frame (center ~ 0.32, 0.45)
    val subjectA = RawDetectedObjectForFocus(
      boundingBox = CameraVisionBoundingBox(0.16f, 0.29f, 0.48f, 0.61f),
      trackingId = 101,
      label = "Coffee cup",
      confidence = 0.88f,
    )
    val initialLock = CameraEngine.selectSmartAutofocusTargetFromObjects(
      objects = listOf(subjectA),
      previousTarget = null,
      timestampMs = 1000L,
    )
    assertNotNull(initialLock)
    assertEquals(0.32f, initialLock!!.normX, 0.01f)
    assertEquals(0.45f, initialLock.normY, 0.01f)

    // 2. Sticky hysteresis: a competing object pops in on the right side with slightly higher confidence
    // while subjectA is still present. Sticky autofocus stays locked on subjectA instead of jumping.
    val transientDistractor = RawDetectedObjectForFocus(
      boundingBox = CameraVisionBoundingBox(0.48f, 0.32f, 0.84f, 0.68f),
      trackingId = 202,
      label = "Laptop",
      confidence = 0.94f,
    )
    val afterDistractor = CameraEngine.selectSmartAutofocusTargetFromObjects(
      objects = listOf(subjectA, transientDistractor),
      previousTarget = initialLock,
      timestampMs = 1350L,
    )
    assertNotNull(afterDistractor)
    assertEquals(101, afterDistractor!!.trackingId)
    assertEquals("Coffee cup", afterDistractor.categoryLabel)
    assertEquals(0.32f, afterDistractor.normX, 0.015f)
    assertEquals(0.45f, afterDistractor.normY, 0.015f)

    // 3. Transient 1-frame detection dropout: when objects list is momentarily empty,
    // autofocus holds its position rather than resetting to null.
    val afterDropout = CameraEngine.selectSmartAutofocusTargetFromObjects(
      objects = emptyList(),
      previousTarget = afterDistractor,
      timestampMs = 1700L,
    )
    assertNotNull(afterDropout)
    assertEquals(1, afterDropout!!.missedDetectionFrames)
    assertEquals(afterDistractor.normX, afterDropout.normX, 0.01f)

    // 4. Gentle trajectory when transitioning to a new target: when subjectA leaves the scene and only
    // a new subject at (0.76, 0.72) remains, autofocus does NOT teleport there in one step.
    // Instead, it sets targetNormX/Y to (0.76, 0.72) and begins gliding gently along the line toward it.
    val newDestinationSubject = RawDetectedObjectForFocus(
      boundingBox = CameraVisionBoundingBox(0.60f, 0.56f, 0.92f, 0.88f),
      trackingId = 303,
      label = "Flowerpot",
      confidence = 0.92f,
    )
    val glideStart = CameraEngine.selectSmartAutofocusTargetFromObjects(
      objects = listOf(newDestinationSubject),
      previousTarget = afterDistractor,
      timestampMs = 1750L,
    )
    assertNotNull(glideStart)
    assertEquals(0.76f, glideStart!!.targetNormX, 0.01f)
    assertEquals(0.72f, glideStart.targetNormY, 0.01f)
    // Moved only a fraction of the way in the first frame (slower, gentle movement)
    assertTrue(glideStart.normX > 0.32f && glideStart.normX < 0.52f)
    assertTrue(glideStart.normY > 0.45f && glideStart.normY < 0.60f)
    assertTrue(glideStart.velocityX > 0f && glideStart.velocityY > 0f)
    assertTrue(glideStart.speedNormPerSec <= STICKY_AF_MAX_SPEED_NORM_PER_SEC + 0.001f)

    // Trace the full trajectory from glideStart to its destination and verify smooth monotonic convergence
    val trajectoryPoints = CameraEngine.sampleAutofocusTrajectoryPoints(
      target = glideStart,
      steps = 20,
      totalDurationSeconds = 1.6f,
    )
    assertTrue(trajectoryPoints.size == 21)
    for (i in 1 until trajectoryPoints.size) {
      val prev = trajectoryPoints[i - 1]
      val curr = trajectoryPoints[i]
      // Monotonically advances along the gentle line toward (0.76, 0.72) without overshoot
      assertTrue(curr.first >= prev.first - 1e-4f && curr.first <= 0.76f + 1e-3f)
      assertTrue(curr.second >= prev.second - 1e-4f && curr.second <= 0.72f + 1e-3f)
    }
    val finalPoint = trajectoryPoints.last()
    assertEquals(0.76f, finalPoint.first, 0.03f)
    assertEquals(0.72f, finalPoint.second, 0.03f)

    // 5. Velocity & trajectory continuity on mid-flight target change:
    // Start at (0.40, 0.50) moving right with velocity (+0.32, 0.0), and redirect desired target straight down to (0.40, 0.82).
    // Because of velocity inertia, the next step continues forward in +X while curving downward in +Y.
    val movingRightTarget = CameraSmartFocusTarget(
      normX = 0.40f,
      normY = 0.50f,
      targetNormX = 0.80f,
      targetNormY = 0.50f,
      velocityX = 0.32f,
      velocityY = 0.0f,
      timestampMs = 2000L,
    )
    val curvedStep = CameraEngine.stepStickyAutofocusTrajectory(
      currentTarget = movingRightTarget,
      desiredX = 0.40f,
      desiredY = 0.82f,
      dtSeconds = 0.08f,
      timestampMs = 2080L,
    )
    assertTrue(curvedStep.normX > 0.405f) // Carried rightward by existing velocity trajectory
    assertTrue(curvedStep.normY > 0.502f) // Smoothly steering downward toward new desired target
    assertTrue(curvedStep.velocityX > 0f && curvedStep.velocityX < 0.32f)
    assertTrue(curvedStep.velocityY > 0f)
  }

  @Test
  fun visionBoundingBoxes_withSameLabels_smoothlyMoveAndResizeIntoNewDetections() {
    val previousTags = listOf(
      CameraVisionTag(
        label = "Plant",
        confidence = 0.95f,
        index = 284,
        boundingBox = CameraVisionBoundingBox(0.10f, 0.12f, 0.50f, 0.52f),
      ),
      CameraVisionTag(
        label = "Flowerpot",
        confidence = 0.88f,
        index = 148,
        boundingBox = CameraVisionBoundingBox(0.20f, 0.56f, 0.60f, 0.86f),
      ),
    )

    // Next detection: "Plant" moves right/down and grows wider/taller; "Flowerpot" moves and shrinks;
    // "Leaf" is a brand-new label that was not in the previous detection.
    val nextTags = listOf(
      CameraVisionTag(
        label = "Flowerpot",
        confidence = 0.94f,
        index = 148,
        boundingBox = CameraVisionBoundingBox(0.36f, 0.60f, 0.68f, 0.84f),
      ),
      CameraVisionTag(
        label = "Plant",
        confidence = 0.91f,
        index = 284,
        boundingBox = CameraVisionBoundingBox(0.26f, 0.20f, 0.78f, 0.72f),
      ),
      CameraVisionTag(
        label = "Leaf",
        confidence = 0.86f,
        index = 211,
        boundingBox = CameraVisionBoundingBox(0.30f, 0.24f, 0.62f, 0.48f),
      ),
    )

    assertTrue(CameraEngine.hasMatchingVisionTagLabels(previousTags, nextTags))
    assertFalse(
      CameraEngine.hasMatchingVisionTagLabels(
        previousTags,
        listOf(CameraVisionTag("Laptop", 0.95f)),
      )
    )

    // At fraction = 0.0f, same-label boxes start at their previous detection's position and size,
    // while the new "Leaf" box starts immediately at its target position and size.
    val atStart = CameraEngine.interpolateVisionTags(
      previousTags = previousTags,
      targetTags = nextTags,
      fraction = 0.0f,
    )
    val startPlantBox = atStart.first { it.label == "Plant" }.boundingBox!!
    val startPotBox = atStart.first { it.label == "Flowerpot" }.boundingBox!!
    val startLeafBox = atStart.first { it.label == "Leaf" }.boundingBox!!
    assertEquals(0.10f, startPlantBox.left, 0.001f)
    assertEquals(0.12f, startPlantBox.top, 0.001f)
    assertEquals(0.50f, startPlantBox.right, 0.001f)
    assertEquals(0.52f, startPlantBox.bottom, 0.001f)
    assertEquals(0.20f, startPotBox.left, 0.001f)
    assertEquals(0.60f, startPotBox.right, 0.001f)
    assertEquals(0.30f, startLeafBox.left, 0.001f)
    assertEquals(0.62f, startLeafBox.right, 0.001f)

    // At fraction = 0.5f, "Plant" and "Flowerpot" have smoothly moved and resized halfway to their new boxes
    val atMid = CameraEngine.interpolateVisionTags(
      previousTags = previousTags,
      targetTags = nextTags,
      fraction = 0.5f,
    )
    val midPlantBox = atMid.first { it.label == "Plant" }.boundingBox!!
    assertEquals(0.18f, midPlantBox.left, 0.001f)
    assertEquals(0.16f, midPlantBox.top, 0.001f)
    assertEquals(0.64f, midPlantBox.right, 0.001f)
    assertEquals(0.62f, midPlantBox.bottom, 0.001f)
    assertEquals(0.46f, midPlantBox.width, 0.001f) // Resized halfway between width 0.40 and width 0.52
    assertEquals(0.46f, midPlantBox.height, 0.001f) // Resized halfway between height 0.40 and height 0.52

    // At fraction = 1.0f, all boxes have arrived at their new detection bounds
    val atEnd = CameraEngine.interpolateVisionTags(
      previousTags = previousTags,
      targetTags = nextTags,
      fraction = 1.0f,
    )
    val endPlantBox = atEnd.first { it.label == "Plant" }.boundingBox!!
    assertEquals(0.26f, endPlantBox.left, 0.001f)
    assertEquals(0.20f, endPlantBox.top, 0.001f)
    assertEquals(0.78f, endPlantBox.right, 0.001f)
    assertEquals(0.72f, endPlantBox.bottom, 0.001f)

    // Color indices remain stable for same-label boxes even when rank order swaps between detections
    val initialColors = CameraEngine.resolveStableVisionTagColorIndices(
      previousTags = emptyList(),
      currentTags = previousTags,
    )
    assertEquals(0, initialColors["plant"])
    assertEquals(1, initialColors["flowerpot"])

    val updatedColors = CameraEngine.resolveStableVisionTagColorIndices(
      previousTags = previousTags,
      currentTags = nextTags,
      previousColorIndices = initialColors,
    )
    assertEquals(0, updatedColors["plant"])
    assertEquals(1, updatedColors["flowerpot"])
    assertEquals(2, updatedColors["leaf"])
  }
}
