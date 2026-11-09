package com.example.toolbox.ui.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect as AndroidRect
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Normalized rectangle within [0f, 1f] relative to image dimensions.
 */
data class NormalizedRect(
  val left: Float = 0f,
  val top: Float = 0f,
  val right: Float = 1f,
  val bottom: Float = 1f,
) {
  val width: Float get() = max(0f, right - left)
  val height: Float get() = max(0f, bottom - top)
  val aspectRatio: Float get() = if (height > 0f) width / height else 1f

  fun coerceNormalized(minSize: Float = 0.05f): NormalizedRect {
    val l = left.coerceIn(0f, 1f - minSize)
    val t = top.coerceIn(0f, 1f - minSize)
    val r = right.coerceIn(l + minSize, 1f)
    val b = bottom.coerceIn(t + minSize, 1f)
    return NormalizedRect(l, t, r, b)
  }

  fun toPixelRect(imageWidth: Int, imageHeight: Int): ImagePixelRect {
    val x = (left * imageWidth).roundToInt().coerceIn(0, max(0, imageWidth - 1))
    val y = (top * imageHeight).roundToInt().coerceIn(0, max(0, imageHeight - 1))
    val w = ((right - left) * imageWidth).roundToInt().coerceIn(1, max(1, imageWidth - x))
    val h = ((bottom - top) * imageHeight).roundToInt().coerceIn(1, max(1, imageHeight - y))
    return ImagePixelRect(x, y, w, h)
  }
}

data class ImagePixelRect(val x: Int, val y: Int, val width: Int, val height: Int)

enum class CropHandle {
  TOP_LEFT,
  TOP_RIGHT,
  BOTTOM_LEFT,
  BOTTOM_RIGHT,
  TOP,
  BOTTOM,
  LEFT,
  RIGHT,
  INSIDE,
}

enum class AspectRatioPreset(val label: String, val ratio: Float?) {
  FREE("Free", null),
  SQUARE("1:1", 1f),
  RATIO_4_3("4:3", 4f / 3f),
  RATIO_3_4("3:4", 3f / 4f),
  RATIO_16_9("16:9", 16f / 9f),
  RATIO_9_16("9:16", 9f / 16f),
}

/**
 * Calculates a crop rect centered in the image conforming to the specified aspect ratio.
 */
fun computeCropForAspectRatio(
  targetRatio: Float,
  imageWidth: Float,
  imageHeight: Float,
): NormalizedRect {
  if (imageWidth <= 0f || imageHeight <= 0f || targetRatio <= 0f) {
    return NormalizedRect(0f, 0f, 1f, 1f)
  }
  val imageAspect = imageWidth / imageHeight
  return if (targetRatio > imageAspect) {
    // Crop is wider than image: constrained by image width (wNorm = 1), scale hNorm down
    val hNorm = (imageAspect / targetRatio).coerceIn(0.05f, 1f)
    val top = (1f - hNorm) / 2f
    NormalizedRect(0f, top, 1f, top + hNorm)
  } else {
    // Crop is taller than image: constrained by image height (hNorm = 1), scale wNorm down
    val wNorm = (targetRatio / imageAspect).coerceIn(0.05f, 1f)
    val left = (1f - wNorm) / 2f
    NormalizedRect(left, 0f, left + wNorm, 1f)
  }
}

/**
 * Determines which crop handle (corner, edge, or inside) was touched.
 * Coordinates are passed in pixel values for accurate distance thresholding.
 */
fun hitTestCropHandle(
  touchX: Float,
  touchY: Float,
  cropRect: NormalizedRect,
  containerWidth: Float,
  containerHeight: Float,
  cornerThresholdPx: Float = 120f,
  edgeThresholdPx: Float = 90f,
): CropHandle? {
  if (containerWidth <= 0f || containerHeight <= 0f) return null

  val leftPx = cropRect.left * containerWidth
  val topPx = cropRect.top * containerHeight
  val rightPx = cropRect.right * containerWidth
  val bottomPx = cropRect.bottom * containerHeight

  fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return kotlin.math.sqrt(dx * dx + dy * dy)
  }

  // 1. Check 4 corners first (corners have highest priority)
  val distTl = distance(touchX, touchY, leftPx, topPx)
  val distTr = distance(touchX, touchY, rightPx, topPx)
  val distBl = distance(touchX, touchY, leftPx, bottomPx)
  val distBr = distance(touchX, touchY, rightPx, bottomPx)

  val minCornerDist = minOf(distTl, distTr, distBl, distBr)
  if (minCornerDist <= cornerThresholdPx) {
    return when (minCornerDist) {
      distTl -> CropHandle.TOP_LEFT
      distTr -> CropHandle.TOP_RIGHT
      distBl -> CropHandle.BOTTOM_LEFT
      else -> CropHandle.BOTTOM_RIGHT
    }
  }

  // 2. Check 4 edges
  val edgeExtension = cornerThresholdPx * 0.5f
  val withinX = touchX in (leftPx - edgeExtension)..(rightPx + edgeExtension)
  val withinY = touchY in (topPx - edgeExtension)..(bottomPx + edgeExtension)

  val distTop = if (withinX) kotlin.math.abs(touchY - topPx) else Float.MAX_VALUE
  val distBottom = if (withinX) kotlin.math.abs(touchY - bottomPx) else Float.MAX_VALUE
  val distLeft = if (withinY) kotlin.math.abs(touchX - leftPx) else Float.MAX_VALUE
  val distRight = if (withinY) kotlin.math.abs(touchX - rightPx) else Float.MAX_VALUE

  val minEdgeDist = minOf(distTop, distBottom, distLeft, distRight)
  if (minEdgeDist <= edgeThresholdPx) {
    return when (minEdgeDist) {
      distTop -> CropHandle.TOP
      distBottom -> CropHandle.BOTTOM
      distLeft -> CropHandle.LEFT
      else -> CropHandle.RIGHT
    }
  }

  // 3. Check inside the crop rect
  if (touchX in leftPx..rightPx && touchY in topPx..bottomPx) {
    return CropHandle.INSIDE
  }

  return null
}

/**
 * Updates a normalized crop rect given a drag delta in normalized coordinates,
 * optionally maintaining a locked aspect ratio.
 */
fun updateCropWithHandle(
  rect: NormalizedRect,
  handle: CropHandle,
  deltaX: Float,
  deltaY: Float,
  minSize: Float = 0.05f,
  lockedAspectRatio: Float? = null,
  imageWidth: Float = 1f,
  imageHeight: Float = 1f,
): NormalizedRect {
  var l = rect.left
  var t = rect.top
  var r = rect.right
  var b = rect.bottom

  if (handle == CropHandle.INSIDE) {
    val w = r - l
    val h = b - t
    val newL = (l + deltaX).coerceIn(0f, 1f - w)
    val newT = (t + deltaY).coerceIn(0f, 1f - h)
    return NormalizedRect(newL, newT, newL + w, newT + h)
  }

  // If aspect ratio is locked, preserve it during corner or edge drags
  if (lockedAspectRatio != null && lockedAspectRatio > 0f && imageWidth > 0f && imageHeight > 0f) {
    val imageAspect = imageWidth / imageHeight
    val targetNormRatio = (lockedAspectRatio / imageAspect).coerceAtLeast(0.01f)

    when (handle) {
      CropHandle.BOTTOM_RIGHT -> {
        var newW = max(minSize, (rect.right - rect.left) + deltaX).coerceAtMost(1f - l)
        var newH = newW / targetNormRatio
        if (t + newH > 1f) {
          newH = 1f - t
          newW = (newH * targetNormRatio).coerceAtMost(1f - l)
        }
        r = l + max(minSize, newW)
        b = t + max(minSize, newH)
      }
      CropHandle.BOTTOM_LEFT -> {
        var newW = max(minSize, (rect.right - rect.left) - deltaX).coerceAtMost(r)
        var newH = newW / targetNormRatio
        if (t + newH > 1f) {
          newH = 1f - t
          newW = (newH * targetNormRatio).coerceAtMost(r)
        }
        l = r - max(minSize, newW)
        b = t + max(minSize, newH)
      }
      CropHandle.TOP_RIGHT -> {
        var newW = max(minSize, (rect.right - rect.left) + deltaX).coerceAtMost(1f - l)
        var newH = newW / targetNormRatio
        if (b - newH < 0f) {
          newH = b
          newW = (newH * targetNormRatio).coerceAtMost(1f - l)
        }
        r = l + max(minSize, newW)
        t = b - max(minSize, newH)
      }
      CropHandle.TOP_LEFT -> {
        var newW = max(minSize, (rect.right - rect.left) - deltaX).coerceAtMost(r)
        var newH = newW / targetNormRatio
        if (b - newH < 0f) {
          newH = b
          newW = (newH * targetNormRatio).coerceAtMost(r)
        }
        l = r - max(minSize, newW)
        t = b - max(minSize, newH)
      }
      CropHandle.TOP -> {
        var newH = max(minSize, (rect.bottom - rect.top) - deltaY).coerceAtMost(b)
        var newW = (newH * targetNormRatio).coerceIn(minSize, 1f)
        val center = (l + r) / 2f
        var newL = center - newW / 2f
        var newR = center + newW / 2f
        if (newL < 0f) { newR -= newL; newL = 0f }
        if (newR > 1f) { newL -= (newR - 1f); newR = 1f }
        t = b - newH
        l = newL.coerceAtLeast(0f)
        r = newR.coerceAtMost(1f)
      }
      CropHandle.BOTTOM -> {
        var newH = max(minSize, (rect.bottom - rect.top) + deltaY).coerceAtMost(1f - t)
        var newW = (newH * targetNormRatio).coerceIn(minSize, 1f)
        val center = (l + r) / 2f
        var newL = center - newW / 2f
        var newR = center + newW / 2f
        if (newL < 0f) { newR -= newL; newL = 0f }
        if (newR > 1f) { newL -= (newR - 1f); newR = 1f }
        b = t + newH
        l = newL.coerceAtLeast(0f)
        r = newR.coerceAtMost(1f)
      }
      CropHandle.LEFT -> {
        var newW = max(minSize, (rect.right - rect.left) - deltaX).coerceAtMost(r)
        var newH = (newW / targetNormRatio).coerceIn(minSize, 1f)
        val center = (t + b) / 2f
        var newT = center - newH / 2f
        var newB = center + newH / 2f
        if (newT < 0f) { newB -= newT; newT = 0f }
        if (newB > 1f) { newT -= (newB - 1f); newB = 1f }
        l = r - newW
        t = newT.coerceAtLeast(0f)
        b = newB.coerceAtMost(1f)
      }
      CropHandle.RIGHT -> {
        var newW = max(minSize, (rect.right - rect.left) + deltaX).coerceAtMost(1f - l)
        var newH = (newW / targetNormRatio).coerceIn(minSize, 1f)
        val center = (t + b) / 2f
        var newT = center - newH / 2f
        var newB = center + newH / 2f
        if (newT < 0f) { newB -= newT; newT = 0f }
        if (newB > 1f) { newT -= (newB - 1f); newB = 1f }
        r = l + newW
        t = newT.coerceAtLeast(0f)
        b = newB.coerceAtMost(1f)
      }
      CropHandle.INSIDE -> {}
    }

    return NormalizedRect(l, t, r, b).coerceNormalized(minSize)
  }

  // Freeform unconstrained drag
  when (handle) {
    CropHandle.TOP_LEFT -> {
      l = min(l + deltaX, r - minSize).coerceAtLeast(0f)
      t = min(t + deltaY, b - minSize).coerceAtLeast(0f)
    }
    CropHandle.TOP_RIGHT -> {
      r = max(r + deltaX, l + minSize).coerceAtMost(1f)
      t = min(t + deltaY, b - minSize).coerceAtLeast(0f)
    }
    CropHandle.BOTTOM_LEFT -> {
      l = min(l + deltaX, r - minSize).coerceAtLeast(0f)
      b = max(b + deltaY, t + minSize).coerceAtMost(1f)
    }
    CropHandle.BOTTOM_RIGHT -> {
      r = max(r + deltaX, l + minSize).coerceAtMost(1f)
      b = max(b + deltaY, t + minSize).coerceAtMost(1f)
    }
    CropHandle.TOP -> {
      t = min(t + deltaY, b - minSize).coerceAtLeast(0f)
    }
    CropHandle.BOTTOM -> {
      b = max(b + deltaY, t + minSize).coerceAtMost(1f)
    }
    CropHandle.LEFT -> {
      l = min(l + deltaX, r - minSize).coerceAtLeast(0f)
    }
    CropHandle.RIGHT -> {
      r = max(r + deltaX, l + minSize).coerceAtMost(1f)
    }
    CropHandle.INSIDE -> {}
  }

  return NormalizedRect(l, t, r, b).coerceNormalized(minSize)
}

/**
 * Calculates inSampleSize for safe downsampling of large images to prevent OOM.
 */
fun calculateInSampleSize(
  origWidth: Int,
  origHeight: Int,
  maxDimension: Int = 2048,
): Int {
  var inSampleSize = 1
  if (origWidth > maxDimension || origHeight > maxDimension) {
    while ((origWidth / inSampleSize) > maxDimension || (origHeight / inSampleSize) > maxDimension) {
      inSampleSize *= 2
    }
  }
  return max(1, inSampleSize)
}

/**
 * Filter presets for one-touch image styling.
 */
enum class FilterPreset(val label: String) {
  NONE("Normal"),
  BW("B&W"),
  WARM("Warm"),
  COOL("Cool"),
  SEPIA("Sepia"),
  VINTAGE("Vintage"),
}

/**
 * Color and lighting adjustments data model.
 */
data class ColorAdjustments(
  val brightness: Float = 0f,      // -100f..100f
  val contrast: Float = 1f,         // 0.5f..1.5f
  val saturation: Float = 1f,       // 0f..2f
  val filterPreset: FilterPreset = FilterPreset.NONE,
)

/**
 * Constructs an Android ColorMatrix from the given [adjustments].
 */
fun buildColorMatrix(adjustments: ColorAdjustments): ColorMatrix {
  val matrix = ColorMatrix()

  // 1. Preset filter matrix
  when (adjustments.filterPreset) {
    FilterPreset.NONE -> {}
    FilterPreset.BW -> {
      matrix.setSaturation(0f)
    }
    FilterPreset.SEPIA -> {
      val sepia = ColorMatrix(
        floatArrayOf(
          0.393f, 0.769f, 0.189f, 0f, 0f,
          0.349f, 0.686f, 0.168f, 0f, 0f,
          0.272f, 0.534f, 0.131f, 0f, 0f,
          0f, 0f, 0f, 1f, 0f,
        ),
      )
      matrix.postConcat(sepia)
    }
    FilterPreset.WARM -> {
      val warm = ColorMatrix(
        floatArrayOf(
          1.15f, 0f, 0f, 0f, 12f,
          0f, 1.05f, 0f, 0f, 4f,
          0f, 0f, 0.85f, 0f, -12f,
          0f, 0f, 0f, 1f, 0f,
        ),
      )
      matrix.postConcat(warm)
    }
    FilterPreset.COOL -> {
      val cool = ColorMatrix(
        floatArrayOf(
          0.85f, 0f, 0f, 0f, -10f,
          0f, 1.0f, 0f, 0f, 0f,
          0f, 0f, 1.2f, 0f, 16f,
          0f, 0f, 0f, 1f, 0f,
        ),
      )
      matrix.postConcat(cool)
    }
    FilterPreset.VINTAGE -> {
      val vintage = ColorMatrix(
        floatArrayOf(
          0.9f, 0f, 0f, 0f, 18f,
          0f, 0.82f, 0f, 0f, 10f,
          0f, 0f, 0.72f, 0f, 6f,
          0f, 0f, 0f, 1f, 0f,
        ),
      )
      matrix.postConcat(vintage)
    }
  }

  // 2. Saturation (if not B&W)
  if (adjustments.saturation != 1f && adjustments.filterPreset != FilterPreset.BW) {
    val satMatrix = ColorMatrix().apply { setSaturation(adjustments.saturation) }
    matrix.postConcat(satMatrix)
  }

  // 3. Contrast & Brightness
  if (adjustments.contrast != 1f || adjustments.brightness != 0f) {
    val c = adjustments.contrast
    val b = adjustments.brightness
    val translate = b + (1f - c) * 128f
    val cbMatrix = ColorMatrix(
      floatArrayOf(
        c, 0f, 0f, 0f, translate,
        0f, c, 0f, 0f, translate,
        0f, 0f, c, 0f, translate,
        0f, 0f, 0f, 1f, 0f,
      ),
    )
    matrix.postConcat(cbMatrix)
  }

  return matrix
}

/**
 * Text annotation on top of the image.
 */
data class ImageTextAnnotation(
  val id: Long = System.currentTimeMillis(),
  val text: String,
  val xRatio: Float = 0.5f,
  val yRatio: Float = 0.5f,
  val color: Color = Color.White,
  val fontSizeSp: Float = 28f,
  val hasBackground: Boolean = true,
)

/**
 * Data structure representing a drawn stroke.
 */
data class ImageDrawingStroke(
  val points: List<Offset>,
  val color: Color,
  val strokeWidth: Float,
  val isEraser: Boolean = false,
)

/**
 * Generic history stack for undo and redo operations.
 */
class ImageEditorHistory<T>(private val maxDepth: Int = 15) {
  val undoStack = mutableListOf<T>()
  val redoStack = mutableListOf<T>()

  val canUndo: Boolean get() = undoStack.isNotEmpty()
  val canRedo: Boolean get() = redoStack.isNotEmpty()

  fun push(state: T) {
    if (undoStack.size >= maxDepth) {
      undoStack.removeAt(0)
    }
    undoStack.add(state)
    redoStack.clear()
  }

  fun undo(currentState: T): T? {
    if (undoStack.isEmpty()) return null
    val prev = undoStack.removeAt(undoStack.lastIndex)
    redoStack.add(currentState)
    return prev
  }

  fun redo(currentState: T): T? {
    if (redoStack.isEmpty()) return null
    val next = redoStack.removeAt(redoStack.lastIndex)
    undoStack.add(currentState)
    return next
  }

  fun clear() {
    undoStack.clear()
    redoStack.clear()
  }
}

/**
 * Calculates the scale factor required to cover the canvas during rotation and perspective tilt,
 * avoiding black or transparent border wedges.
 */
fun calculateTiltScaleFactor(
  tiltAngle: Float,
  horizontalTilt: Float = 0f,
  verticalTilt: Float = 0f,
  aspectRatio: Float = 1f,
): Float {
  val safeAspect = if (aspectRatio > 0f) aspectRatio else 1f
  val absAngleRad = Math.toRadians(Math.abs(tiltAngle.toDouble()))
  val sinA = Math.sin(absAngleRad).toFloat()
  val cosA = Math.cos(absAngleRad).toFloat()
  val rotScale = (cosA + sinA * maxOf(safeAspect, 1f / safeAspect)).coerceAtLeast(1f)

  val maxPerspAngle = maxOf(Math.abs(horizontalTilt), Math.abs(verticalTilt)).toDouble()
  val cosPersp = Math.cos(Math.toRadians(maxPerspAngle)).toFloat()
  val perspScale = if (cosPersp > 0.1f) (1f / cosPersp).coerceIn(1f, 3f) else 1f

  return (rotScale * perspScale).coerceIn(1f, 5f)
}

/**
 * Bitmap image manipulation operations: Cropping, Rotating, Tilting, Drawing, and Filters.
 */
object BitmapOperations {

  /**
   * Crops the [source] bitmap using the provided [cropRect].
   */
  fun crop(source: Bitmap, cropRect: NormalizedRect): Bitmap {
    val pixelRect = cropRect.toPixelRect(source.width, source.height)
    return Bitmap.createBitmap(
      source,
      pixelRect.x,
      pixelRect.y,
      pixelRect.width,
      pixelRect.height,
    )
  }

  /**
   * Rotates and optionally flips the [source] bitmap.
   */
  fun rotate(
    source: Bitmap,
    degrees: Float,
    flipHorizontal: Boolean = false,
    flipVertical: Boolean = false,
  ): Bitmap {
    val matrix = Matrix()
    if (flipHorizontal || flipVertical) {
      matrix.postScale(
        if (flipHorizontal) -1f else 1f,
        if (flipVertical) -1f else 1f,
        source.width / 2f,
        source.height / 2f,
      )
    }
    if (degrees != 0f) {
      matrix.postRotate(degrees)
    }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
  }

  /**
   * Applies perspective tilting and angle straightening to the [source] bitmap.
   *
   * @param source The source bitmap to transform.
   * @param tiltAngle Angle in degrees for 2D horizon straightening (-45°..45°).
   * @param horizontalTilt Angle in degrees for horizontal perspective yaw (-30°..30°).
   * @param verticalTilt Angle in degrees for vertical perspective pitch (-30°..30°).
   * @param cropBorders When true, scales up to eliminate empty border triangles.
   */
  fun tilt(
    source: Bitmap,
    tiltAngle: Float,
    horizontalTilt: Float = 0f,
    verticalTilt: Float = 0f,
    cropBorders: Boolean = true,
  ): Bitmap {
    if (tiltAngle == 0f && horizontalTilt == 0f && verticalTilt == 0f) {
      return source
    }

    val w = source.width.toFloat()
    val h = source.height.toFloat()
    val cx = w / 2f
    val cy = h / 2f

    val matrix = Matrix()

    // 3D perspective tilt
    if (horizontalTilt != 0f || verticalTilt != 0f) {
      val camera = android.graphics.Camera()
      camera.save()
      val maxDim = maxOf(w, h)
      val cameraDist = -(maxDim / 72f) * 8f
      camera.setLocation(0f, 0f, cameraDist)
      camera.rotateX(-verticalTilt)
      camera.rotateY(horizontalTilt)
      if (tiltAngle != 0f) {
        camera.rotateZ(-tiltAngle)
      }
      camera.getMatrix(matrix)
      camera.restore()
      matrix.preTranslate(-cx, -cy)
      matrix.postTranslate(cx, cy)
    } else {
      // Pure 2D straightening
      matrix.postRotate(tiltAngle, cx, cy)
    }

    if (cropBorders) {
      val aspect = if (h > 0f) w / h else 1f
      val scale = calculateTiltScaleFactor(
        tiltAngle = tiltAngle,
        horizontalTilt = horizontalTilt,
        verticalTilt = verticalTilt,
        aspectRatio = aspect,
      )
      if (scale > 1f) {
        matrix.postScale(scale, scale, cx, cy)
      }
    }

    val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint().apply {
      isAntiAlias = true
      isFilterBitmap = true
      isDither = true
    }
    canvas.drawBitmap(source, matrix, paint)
    return output
  }

  /**
   * Applies color adjustments (brightness, contrast, saturation, filter preset) to [source].
   */
  fun applyColorAdjustments(source: Bitmap, adjustments: ColorAdjustments): Bitmap {
    val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint().apply {
      isAntiAlias = true
      isDither = true
      colorFilter = ColorMatrixColorFilter(buildColorMatrix(adjustments))
    }
    canvas.drawBitmap(source, 0f, 0f, paint)
    return output
  }

  /**
   * Renders text annotations onto the bitmap.
   */
  fun renderAnnotations(source: Bitmap, annotations: List<ImageTextAnnotation>): Bitmap {
    if (annotations.isEmpty()) return source
    val output = source.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(output)
    val scale = max(source.width, source.height) / 1000f

    annotations.forEach { ann ->
      val textPaint = Paint().apply {
        isAntiAlias = true
        color = ann.color.toArgb()
        textSize = ann.fontSizeSp * scale * 1.8f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
      }

      val posX = ann.xRatio * source.width
      val posY = ann.yRatio * source.height

      if (ann.hasBackground) {
        val bounds = AndroidRect()
        textPaint.getTextBounds(ann.text, 0, ann.text.length, bounds)
        val pad = 14f * scale
        val bgPaint = Paint().apply {
          isAntiAlias = true
          color = AndroidColor.argb(160, 0, 0, 0)
        }
        val bgRect = RectF(
          posX - bounds.width() / 2f - pad,
          posY - bounds.height() - pad,
          posX + bounds.width() / 2f + pad,
          posY + pad,
        )
        canvas.drawRoundRect(bgRect, pad, pad, bgPaint)
      }

      canvas.drawText(ann.text, posX, posY, textPaint)
    }

    return output
  }

  /**
   * Draws a list of strokes directly onto a mutable copy of [source].
   */
  fun renderStrokes(
    source: Bitmap,
    strokes: List<ImageDrawingStroke>,
  ): Bitmap {
    if (strokes.isEmpty()) return source
    val output = source.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(output)

    strokes.forEach { stroke ->
      if (stroke.points.size < 2) {
        if (stroke.points.isNotEmpty()) {
          val p = stroke.points[0]
          val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            if (stroke.isEraser) {
              xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            } else {
              color = stroke.color.toArgb()
            }
          }
          canvas.drawCircle(p.x, p.y, stroke.strokeWidth / 2f, paint)
        }
        return@forEach
      }

      val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = stroke.strokeWidth
        if (stroke.isEraser) {
          xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
          color = stroke.color.toArgb()
        }
      }

      val path = AndroidPath()
      path.moveTo(stroke.points[0].x, stroke.points[0].y)
      for (i in 1 until stroke.points.size) {
        val prev = stroke.points[i - 1]
        val curr = stroke.points[i]
        val midX = (prev.x + curr.x) / 2f
        val midY = (prev.y + curr.y) / 2f
        path.quadTo(prev.x, prev.y, midX, midY)
      }
      val last = stroke.points.last()
      path.lineTo(last.x, last.y)

      canvas.drawPath(path, paint)
    }

    return output
  }

  /**
   * Generates a pleasant sample bitmap for preview and instant testing.
   */
  fun createSampleBitmap(width: Int = 800, height: Int = 800): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Sky gradient
    val skyPaint = Paint().apply {
      shader = LinearGradient(
        0f, 0f, 0f, height * 0.7f,
        AndroidColor.rgb(56, 142, 240),
        AndroidColor.rgb(255, 178, 102),
        Shader.TileMode.CLAMP,
      )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)

    // Sun
    val sunPaint = Paint().apply {
      isAntiAlias = true
      color = AndroidColor.rgb(255, 235, 59)
    }
    canvas.drawCircle(width * 0.25f, height * 0.35f, width * 0.12f, sunPaint)

    // Mountains
    val mountainPaint1 = Paint().apply {
      isAntiAlias = true
      color = AndroidColor.rgb(76, 92, 104)
      style = Paint.Style.FILL
    }
    val mountainPath1 = AndroidPath().apply {
      moveTo(0f, height.toFloat())
      lineTo(width * 0.35f, height * 0.45f)
      lineTo(width * 0.7f, height.toFloat())
      close()
    }
    canvas.drawPath(mountainPath1, mountainPaint1)

    val mountainPaint2 = Paint().apply {
      isAntiAlias = true
      color = AndroidColor.rgb(52, 73, 94)
      style = Paint.Style.FILL
    }
    val mountainPath2 = AndroidPath().apply {
      moveTo(width * 0.3f, height.toFloat())
      lineTo(width * 0.65f, height * 0.4f)
      lineTo(width.toFloat(), height.toFloat())
      close()
    }
    canvas.drawPath(mountainPath2, mountainPaint2)

    // Foreground grass hills
    val hillPaint = Paint().apply {
      isAntiAlias = true
      color = AndroidColor.rgb(46, 125, 50)
      style = Paint.Style.FILL
    }
    val hillRect = RectF(0f, height * 0.75f, width.toFloat(), height * 1.25f)
    canvas.drawOval(hillRect, hillPaint)

    // Text label
    val textPaint = Paint().apply {
      isAntiAlias = true
      color = AndroidColor.WHITE
      textSize = width * 0.045f
      textAlign = Paint.Align.CENTER
      isFakeBoldText = true
    }
    canvas.drawText("Toolbox Image Editor", width / 2f, height * 0.9f, textPaint)

    return bitmap
  }
}
