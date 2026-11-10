package com.example.toolbox.ui.tools

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ImageEditorEngineTest {

  @Test
  fun normalizedRect_computesWidthHeightAndAspectRatioCorrectly() {
    val rect = NormalizedRect(left = 0.1f, top = 0.2f, right = 0.7f, bottom = 0.6f)
    assertEquals(0.6f, rect.width, 1e-5f)
    assertEquals(0.4f, rect.height, 1e-5f)
    assertEquals(1.5f, rect.aspectRatio, 1e-5f)
  }

  @Test
  fun normalizedRect_coercesToBoundsAndEnforcesMinSize() {
    val negative = NormalizedRect(left = -0.2f, top = -0.1f, right = 1.5f, bottom = 1.2f)
    val coerced = negative.coerceNormalized(minSize = 0.1f)
    assertEquals(0.0f, coerced.left, 1e-5f)
    assertEquals(0.0f, coerced.top, 1e-5f)
    assertEquals(1.0f, coerced.right, 1e-5f)
    assertEquals(1.0f, coerced.bottom, 1e-5f)

    // Collapsed rect
    val collapsed = NormalizedRect(left = 0.5f, top = 0.5f, right = 0.51f, bottom = 0.51f)
    val coercedCollapsed = collapsed.coerceNormalized(minSize = 0.1f)
    assertTrue(coercedCollapsed.width >= 0.1f)
    assertTrue(coercedCollapsed.height >= 0.1f)
  }

  @Test
  fun normalizedRect_convertsToPixelRectAccurately() {
    val rect = NormalizedRect(left = 0.25f, top = 0.1f, right = 0.75f, bottom = 0.9f)
    val pixelRect = rect.toPixelRect(imageWidth = 1000, imageHeight = 800)
    assertEquals(250, pixelRect.x)
    assertEquals(80, pixelRect.y)
    assertEquals(500, pixelRect.width)
    assertEquals(640, pixelRect.height)
  }

  @Test
  fun computeCropForAspectRatio_centersAndConstrainsProperly() {
    // 16:9 target crop on 1000x1000 (1:1 aspect) image:
    // targetRatio (1.777) > imageAspect (1.0), so width is full (0..1) and height is shrunk
    val cropWide = computeCropForAspectRatio(
      targetRatio = 16f / 9f,
      imageWidth = 1000f,
      imageHeight = 1000f,
    )
    assertEquals(0f, cropWide.left, 1e-4f)
    assertEquals(1f, cropWide.right, 1e-4f)
    assertEquals(1f, cropWide.width, 1e-4f)
    val expectedHeightNorm = (9f / 16f)
    assertEquals(expectedHeightNorm, cropWide.height, 1e-3f)
    val expectedTop = (1f - expectedHeightNorm) / 2f
    assertEquals(expectedTop, cropWide.top, 1e-3f)

    // 9:16 target crop on 1000x1000 image:
    // targetRatio (0.5625) < imageAspect (1.0), so height is full (0..1) and width is shrunk
    val cropTall = computeCropForAspectRatio(
      targetRatio = 9f / 16f,
      imageWidth = 1000f,
      imageHeight = 1000f,
    )
    assertEquals(0f, cropTall.top, 1e-4f)
    assertEquals(1f, cropTall.bottom, 1e-4f)
    assertEquals(1f, cropTall.height, 1e-4f)
    val expectedWidthNorm = 9f / 16f
    assertEquals(expectedWidthNorm, cropTall.width, 1e-3f)
  }

  @Test
  fun updateCropWithHandle_movesInsideWithoutResizing() {
    val initial = NormalizedRect(left = 0.2f, top = 0.2f, right = 0.6f, bottom = 0.6f)
    val moved = updateCropWithHandle(
      rect = initial,
      handle = CropHandle.INSIDE,
      deltaX = 0.1f,
      deltaY = 0.15f,
    )
    assertEquals(0.3f, moved.left, 1e-5f)
    assertEquals(0.35f, moved.top, 1e-5f)
    assertEquals(0.7f, moved.right, 1e-5f)
    assertEquals(0.75f, moved.bottom, 1e-5f)
    assertEquals(0.4f, moved.width, 1e-5f)
    assertEquals(0.4f, moved.height, 1e-5f)
  }

  @Test
  fun updateCropWithHandle_resizesCornersCorrectly() {
    val initial = NormalizedRect(left = 0.2f, top = 0.2f, right = 0.8f, bottom = 0.8f)

    // Drag Top-Left outward (increase crop area)
    val resizedTl = updateCropWithHandle(
      rect = initial,
      handle = CropHandle.TOP_LEFT,
      deltaX = -0.1f,
      deltaY = -0.1f,
    )
    assertEquals(0.1f, resizedTl.left, 1e-5f)
    assertEquals(0.1f, resizedTl.top, 1e-5f)
    assertEquals(0.8f, resizedTl.right, 1e-5f)
    assertEquals(0.8f, resizedTl.bottom, 1e-5f)

    // Drag Bottom-Right inward (shrink crop area)
    val resizedBr = updateCropWithHandle(
      rect = initial,
      handle = CropHandle.BOTTOM_RIGHT,
      deltaX = -0.2f,
      deltaY = -0.2f,
    )
    assertEquals(0.2f, resizedBr.left, 1e-5f)
    assertEquals(0.2f, resizedBr.top, 1e-5f)
    assertEquals(0.6f, resizedBr.right, 1e-5f)
    assertEquals(0.6f, resizedBr.bottom, 1e-5f)
  }

  @Test
  fun imageEditorHistory_tracksUndoAndRedo() {
    val history = ImageEditorHistory<String>(maxDepth = 3)

    assertFalse(history.canUndo)
    assertFalse(history.canRedo)

    history.push("State1")
    history.push("State2")
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)

    val current = "State3"
    val undone = history.undo(current)
    assertEquals("State2", undone)
    assertTrue(history.canUndo)
    assertTrue(history.canRedo)

    val redone = history.redo(undone!!)
    assertEquals("State3", redone)
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
  }

  @Test
  fun imageEditorHistory_clearsRedoStackOnPushAndLimitsDepth() {
    val history = ImageEditorHistory<String>(maxDepth = 2)

    history.push("S1")
    history.push("S2")
    history.push("S3") // S1 evicted due to maxDepth = 2

    val prev = history.undo("S4")
    assertEquals("S3", prev)
    assertTrue(history.canRedo)

    // Push new state clears redo
    history.push("S5")
    assertFalse(history.canRedo)

    history.clear()
    assertFalse(history.canUndo)
    assertFalse(history.canRedo)
    assertNull(history.undo("S5"))
  }

  @Test
  fun hitTestCropHandle_detectsCornersEdgesAndInsideCorrectly() {
    val cropRect = NormalizedRect(left = 0.2f, top = 0.2f, right = 0.8f, bottom = 0.8f)
    val width = 1000f
    val height = 1000f
    // Crop area in px: [200, 200] to [800, 800]

    // Corners
    assertEquals(
      CropHandle.TOP_LEFT,
      hitTestCropHandle(210f, 205f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )
    assertEquals(
      CropHandle.TOP_RIGHT,
      hitTestCropHandle(795f, 210f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )
    assertEquals(
      CropHandle.BOTTOM_LEFT,
      hitTestCropHandle(205f, 790f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )
    assertEquals(
      CropHandle.BOTTOM_RIGHT,
      hitTestCropHandle(790f, 795f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )

    // Edges (both slightly inside and slightly outside)
    // Top edge at y=200, center x=500
    assertEquals(
      CropHandle.TOP,
      hitTestCropHandle(500f, 185f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f) // 15px outside
    )
    assertEquals(
      CropHandle.TOP,
      hitTestCropHandle(500f, 220f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f) // 20px inside
    )

    // Bottom edge at y=800, center x=500
    assertEquals(
      CropHandle.BOTTOM,
      hitTestCropHandle(500f, 815f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f) // 15px outside
    )
    assertEquals(
      CropHandle.BOTTOM,
      hitTestCropHandle(500f, 785f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f) // 15px inside
    )

    // Left edge at x=200, center y=500
    assertEquals(
      CropHandle.LEFT,
      hitTestCropHandle(185f, 500f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )
    assertEquals(
      CropHandle.LEFT,
      hitTestCropHandle(220f, 500f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )

    // Right edge at x=800, center y=500
    assertEquals(
      CropHandle.RIGHT,
      hitTestCropHandle(815f, 500f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )
    assertEquals(
      CropHandle.RIGHT,
      hitTestCropHandle(780f, 500f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )

    // Center/Inside
    assertEquals(
      CropHandle.INSIDE,
      hitTestCropHandle(500f, 500f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )

    // Far outside returns null
    assertNull(
      hitTestCropHandle(50f, 50f, cropRect, width, height, cornerThresholdPx = 60f, edgeThresholdPx = 40f)
    )
  }

  @Test
  fun hitTestCropHandle_withBiggerHitboxes_detectsTouchesGenerously() {
    val cropRect = NormalizedRect(left = 0.3f, top = 0.3f, right = 0.7f, bottom = 0.7f)
    val width = 1000f
    val height = 1000f
    // Top-left corner is at (300, 300). With cornerThresholdPx = 160f, touch at (170, 170) is ~183px away (outside 160),
    // but touch at (200, 200) is sqrt(100^2 + 100^2) = 141.4px <= 160f
    assertEquals(
      CropHandle.TOP_LEFT,
      hitTestCropHandle(200f, 200f, cropRect, width, height)
    )

    // Top edge at y=300. Touch at (500, 200) is 100px away <= 120f edgeThreshold
    assertEquals(
      CropHandle.TOP,
      hitTestCropHandle(500f, 200f, cropRect, width, height)
    )
  }

  @Test
  fun calculateInSampleSize_handlesDifferentResolutions() {
    // 1080p within 2048 -> sample size 1
    assertEquals(1, calculateInSampleSize(1920, 1080, maxDimension = 2048))

    // 4000x3000 -> sample size 2
    assertEquals(2, calculateInSampleSize(4000, 3000, maxDimension = 2048))

    // 8000x6000 (48MP) -> sample size 4
    assertEquals(4, calculateInSampleSize(8000, 6000, maxDimension = 2048))

    // 16000x12000 -> sample size 8
    assertEquals(8, calculateInSampleSize(16000, 12000, maxDimension = 2048))
  }

  @Test
  fun updateCropWithHandle_preservesLockedAspectRatioOnCornerDrag() {
    val initial = NormalizedRect(left = 0.2f, top = 0.2f, right = 0.6f, bottom = 0.6f)
    // 1:1 aspect ratio on a 1:1 image
    val resized = updateCropWithHandle(
      rect = initial,
      handle = CropHandle.BOTTOM_RIGHT,
      deltaX = 0.2f,
      deltaY = 0.2f,
      lockedAspectRatio = 1f,
      imageWidth = 1000f,
      imageHeight = 1000f,
    )

    assertEquals(resized.width, resized.height, 1e-4f)
    assertEquals(0.2f, resized.left, 1e-4f)
    assertEquals(0.2f, resized.top, 1e-4f)
    assertEquals(0.8f, resized.right, 1e-4f)
    assertEquals(0.8f, resized.bottom, 1e-4f)
  }

  @Test
  fun colorAdjustments_defaultsAndProperties() {
    val defaultAdj = ColorAdjustments()
    assertEquals(0f, defaultAdj.brightness)
    assertEquals(1f, defaultAdj.contrast)
    assertEquals(1f, defaultAdj.saturation)
    assertEquals(FilterPreset.NONE, defaultAdj.filterPreset)

    val custom = defaultAdj.copy(
      brightness = 25f,
      contrast = 1.2f,
      saturation = 1.4f,
      filterPreset = FilterPreset.SEPIA,
    )
    assertEquals(25f, custom.brightness)
    assertEquals(1.2f, custom.contrast)
    assertEquals(1.4f, custom.saturation)
    assertEquals(FilterPreset.SEPIA, custom.filterPreset)
  }

  @Test
  fun calculateTiltScaleFactor_returnsOneWhenZero() {
    val scale = calculateTiltScaleFactor(
      tiltAngle = 0f,
      horizontalTilt = 0f,
      verticalTilt = 0f,
      aspectRatio = 1f,
    )
    assertEquals(1.0f, scale, 1e-4f)
  }

  @Test
  fun calculateTiltScaleFactor_scalesUpWhenRotated() {
    val scale = calculateTiltScaleFactor(
      tiltAngle = 15f,
      horizontalTilt = 0f,
      verticalTilt = 0f,
      aspectRatio = 1f,
    )
    assertTrue("Scale must be greater than 1.0 to avoid black borders", scale > 1.0f)
    // For 15° rotation on 1:1, cos(15) + sin(15) = 0.9659 + 0.2588 = 1.2247
    assertEquals(1.2247f, scale, 0.01f)
  }

  @Test
  fun calculateTiltScaleFactor_scalesUpWhenTiltedInPerspective() {
    val scale = calculateTiltScaleFactor(
      tiltAngle = 0f,
      horizontalTilt = 20f,
      verticalTilt = 0f,
      aspectRatio = 1.5f,
    )
    assertTrue("Scale must be greater than 1.0 for 20° perspective tilt", scale > 1.0f)
    // 1 / cos(20°) ≈ 1.064
    assertEquals(1.064f, scale, 0.02f)
  }

  @Test
  fun calculateTiltScaleFactor_handlesDifferentAspectRatios() {
    val wideScale = calculateTiltScaleFactor(tiltAngle = 10f, aspectRatio = 16f / 9f)
    val tallScale = calculateTiltScaleFactor(tiltAngle = 10f, aspectRatio = 9f / 16f)
    val squareScale = calculateTiltScaleFactor(tiltAngle = 10f, aspectRatio = 1f)

    assertTrue(wideScale > 1.0f)
    assertTrue(tallScale > 1.0f)
    assertTrue(squareScale > 1.0f)
    // Wide and tall with reciprocal aspect ratios should require identical scale factor
    assertEquals(wideScale, tallScale, 1e-3f)
  }

  @Test
  fun aspectRatioPreset_swapOrientation_invertsStandardRatios() {
    assertEquals(AspectRatioPreset.RATIO_3_4, AspectRatioPreset.RATIO_4_3.swapOrientation())
    assertEquals(AspectRatioPreset.RATIO_4_3, AspectRatioPreset.RATIO_3_4.swapOrientation())
    assertEquals(AspectRatioPreset.RATIO_9_16, AspectRatioPreset.RATIO_16_9.swapOrientation())
    assertEquals(AspectRatioPreset.RATIO_16_9, AspectRatioPreset.RATIO_9_16.swapOrientation())
    assertEquals(AspectRatioPreset.SQUARE, AspectRatioPreset.SQUARE.swapOrientation())
    assertEquals(AspectRatioPreset.FREE, AspectRatioPreset.FREE.swapOrientation())
  }

  @Test
  fun isVerticalAspectRatio_detectsOrientationAccurately() {
    val fullRect = NormalizedRect(0f, 0f, 1f, 1f)

    // Presets
    assertTrue(isVerticalAspectRatio(AspectRatioPreset.RATIO_3_4, fullRect, 1000f, 1000f))
    assertTrue(isVerticalAspectRatio(AspectRatioPreset.RATIO_9_16, fullRect, 1000f, 1000f))
    assertFalse(isVerticalAspectRatio(AspectRatioPreset.RATIO_4_3, fullRect, 1000f, 1000f))
    assertFalse(isVerticalAspectRatio(AspectRatioPreset.RATIO_16_9, fullRect, 1000f, 1000f))

    // Square honors squareIsVertical toggle flag
    assertFalse(isVerticalAspectRatio(AspectRatioPreset.SQUARE, fullRect, 1000f, 1000f, squareIsVertical = false))
    assertTrue(isVerticalAspectRatio(AspectRatioPreset.SQUARE, fullRect, 1000f, 1000f, squareIsVertical = true))

    // Free mode: landscape image
    assertFalse(isVerticalAspectRatio(AspectRatioPreset.FREE, fullRect, 1920f, 1080f))
    // Free mode: portrait image
    assertTrue(isVerticalAspectRatio(AspectRatioPreset.FREE, fullRect, 1080f, 1920f))

    // Free mode: arbitrary crop dimensions
    val wideCrop = NormalizedRect(left = 0.1f, top = 0.2f, right = 0.9f, bottom = 0.4f) // w=0.8, h=0.2
    assertFalse(isVerticalAspectRatio(AspectRatioPreset.FREE, wideCrop, 1000f, 1000f))

    val tallCrop = NormalizedRect(left = 0.2f, top = 0.1f, right = 0.4f, bottom = 0.9f) // w=0.2, h=0.8
    assertTrue(isVerticalAspectRatio(AspectRatioPreset.FREE, tallCrop, 1000f, 1000f))
  }

  @Test
  fun swapCropAspectRatio_swapsPresetsAndComputesCorrectCropRect() {
    val initialRect = NormalizedRect(0f, 0f, 1f, 1f)
    val imgW = 1000f
    val imgH = 1000f

    // 4:3 -> 3:4
    val (swapped43, rect43) = swapCropAspectRatio(AspectRatioPreset.RATIO_4_3, initialRect, imgW, imgH)
    assertEquals(AspectRatioPreset.RATIO_3_4, swapped43)
    assertEquals(3f / 4f, (rect43.width * imgW) / (rect43.height * imgH), 1e-3f)

    // 3:4 -> 4:3
    val (swapped34, rect34) = swapCropAspectRatio(AspectRatioPreset.RATIO_3_4, rect43, imgW, imgH)
    assertEquals(AspectRatioPreset.RATIO_4_3, swapped34)
    assertEquals(4f / 3f, (rect34.width * imgW) / (rect34.height * imgH), 1e-3f)

    // 16:9 -> 9:16
    val (swapped169, rect169) = swapCropAspectRatio(AspectRatioPreset.RATIO_16_9, initialRect, imgW, imgH)
    assertEquals(AspectRatioPreset.RATIO_9_16, swapped169)
    assertEquals(9f / 16f, (rect169.width * imgW) / (rect169.height * imgH), 1e-3f)

    // 9:16 -> 16:9
    val (swapped916, rect916) = swapCropAspectRatio(AspectRatioPreset.RATIO_9_16, rect169, imgW, imgH)
    assertEquals(AspectRatioPreset.RATIO_16_9, swapped916)
    assertEquals(16f / 9f, (rect916.width * imgW) / (rect916.height * imgH), 1e-3f)

    // Free mode: on a 2000x1000 (aspect 2.0) image, full rect starts at 2.0. Swapping gives aspect 0.5.
    val (swappedFree, rectFree) = swapCropAspectRatio(AspectRatioPreset.FREE, initialRect, 2000f, 1000f)
    assertEquals(AspectRatioPreset.FREE, swappedFree)
    val freeAspect = (rectFree.width * 2000f) / (rectFree.height * 1000f)
    assertEquals(0.5f, freeAspect, 1e-3f)

    // Swapping free mode again returns to 2.0
    val (swappedBackFree, rectBackFree) = swapCropAspectRatio(AspectRatioPreset.FREE, rectFree, 2000f, 1000f)
    assertEquals(AspectRatioPreset.FREE, swappedBackFree)
    val backAspect = (rectBackFree.width * 2000f) / (rectBackFree.height * 1000f)
    assertEquals(2.0f, backAspect, 1e-3f)
  }
}
