package com.example.toolbox.ui.tools

import android.content.Context
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
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.abs
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
  RATIO_9_16("9:16", 9f / 16f);

  fun swapOrientation(): AspectRatioPreset = when (this) {
    RATIO_4_3 -> RATIO_3_4
    RATIO_3_4 -> RATIO_4_3
    RATIO_16_9 -> RATIO_9_16
    RATIO_9_16 -> RATIO_16_9
    SQUARE -> SQUARE
    FREE -> FREE
  }
}

/**
 * Checks whether the current crop or preset represents a vertical (portrait) aspect ratio.
 */
fun isVerticalAspectRatio(
  preset: AspectRatioPreset,
  cropRect: NormalizedRect,
  imageWidth: Float,
  imageHeight: Float,
  squareIsVertical: Boolean = false,
): Boolean {
  return when (preset) {
    AspectRatioPreset.RATIO_3_4, AspectRatioPreset.RATIO_9_16 -> true
    AspectRatioPreset.RATIO_4_3, AspectRatioPreset.RATIO_16_9 -> false
    AspectRatioPreset.SQUARE -> squareIsVertical
    AspectRatioPreset.FREE -> {
      val pxW = cropRect.width * imageWidth
      val pxH = cropRect.height * imageHeight
      pxW < pxH
    }
  }
}

/**
 * Swaps between horizontal and vertical aspect ratios.
 * If a preset with a counterpart is selected (e.g. 4:3 <-> 3:4 or 16:9 <-> 9:16), it toggles to that preset.
 * If in FREE mode or custom crop, the current pixel aspect ratio is inverted.
 */
fun swapCropAspectRatio(
  preset: AspectRatioPreset,
  cropRect: NormalizedRect,
  imageWidth: Float,
  imageHeight: Float,
): Pair<AspectRatioPreset, NormalizedRect> {
  val swappedPreset = preset.swapOrientation()
  val targetRatio = when {
    swappedPreset.ratio != null -> swappedPreset.ratio
    else -> {
      val pxW = cropRect.width * imageWidth
      val pxH = cropRect.height * imageHeight
      if (pxW > 0f && pxH > 0f) pxH / pxW else 1f
    }
  }
  val newCropRect = computeCropForAspectRatio(targetRatio, imageWidth, imageHeight)
  return Pair(swappedPreset, newCropRect)
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
  cornerThresholdPx: Float = 160f,
  edgeThresholdPx: Float = 120f,
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

/**
 * Editable image metadata fields supported by the Image Editor's `Data` tool.
 */
data class ImageMetadata(
  val title: String = "",
  val artist: String = "",
  val copyright: String = "",
  val dateTaken: String = "",
  val cameraMake: String = "",
  val cameraModel: String = "",
  val software: String = "",
  val location: String = "",
) {
  val isEmpty: Boolean
    get() = title.isBlank() &&
      artist.isBlank() &&
      copyright.isBlank() &&
      dateTaken.isBlank() &&
      cameraMake.isBlank() &&
      cameraModel.isBlank() &&
      software.isBlank() &&
      location.isBlank()

  val populatedFieldCount: Int
    get() = listOf(
      title,
      artist,
      copyright,
      dateTaken,
      cameraMake,
      cameraModel,
      software,
      location,
    ).count { it.isNotBlank() }

  fun sanitized(): ImageMetadata {
    fun clean(value: String, maxLen: Int = 256): String =
      value.replace("\u0000", "")
        .replace("\r", " ")
        .replace("\n", " ")
        .trim()
        .take(maxLen)

    return ImageMetadata(
      title = clean(title, 512),
      artist = clean(artist),
      copyright = clean(copyright),
      dateTaken = clean(dateTaken, 64),
      cameraMake = clean(cameraMake, 128),
      cameraModel = clean(cameraModel, 128),
      software = clean(software, 128),
      location = clean(location, 128),
    )
  }
}

/**
 * Snapshot stored in [ImageEditorHistory] to support undo/redo across both bitmap and metadata edits.
 */
data class ImageEditSnapshot(
  val bitmap: Bitmap,
  val metadata: ImageMetadata = ImageMetadata(),
)

/**
 * Reads and writes image metadata (EXIF attributes + lossless binary metadata trailer for PNG/WebP/JVM).
 */
object ImageMetadataOperations {

  private const val TRAILER_MAGIC = "TBXIMGMD"

  /**
   * Parses a GPS coordinate string such as `"37.7749, -122.4194"` or `"+37.7749-122.4194"`
   * into `(latitude, longitude)` in decimal degrees.
   */
  fun parseGpsCoordinates(location: String): Pair<Double, Double>? {
    val trimmed = location.trim()
    if (trimmed.isEmpty()) return null

    if (',' in trimmed) {
      val parts = trimmed.split(',').map { it.trim() }
      if (parts.size >= 2) {
        val lat = parts[0].toDoubleOrNull()
        val lon = parts[1].toDoubleOrNull()
        if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
          return lat to lon
        }
      }
    }

    val isoRegex = Regex("""([+-]?\d+(?:\.\d+)?)\s*([+-]\d+(?:\.\d+)?)""")
    val match = isoRegex.find(trimmed)
    if (match != null) {
      val lat = match.groupValues[1].toDoubleOrNull()
      val lon = match.groupValues[2].toDoubleOrNull()
      if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
        return lat to lon
      }
    }

    return null
  }

  /**
   * Formats a decimal degree coordinate into EXIF rational DMS format (`"deg/1,min/1,secMicro/10000"`).
   */
  fun formatDecimalToExifDms(coordinate: Double): String {
    val absCoord = abs(coordinate).coerceIn(0.0, 180.0)
    val degrees = absCoord.toInt()
    val minutesFull = (absCoord - degrees) * 60.0
    val minutes = minutesFull.toInt()
    val secondsMicro = ((minutesFull - minutes) * 60.0 * 10000.0).roundToInt().coerceAtLeast(0)
    return "$degrees/1,$minutes/1,$secondsMicro/10000"
  }

  /**
   * Parses an EXIF rational DMS string (`"deg/1,min/1,sec/10000"`) and reference (`"N"`, `"S"`, `"E"`, `"W"`)
   * back into signed decimal degrees.
   */
  fun parseExifDmsToDecimal(dms: String?, ref: String?): Double? {
    if (dms.isNullOrBlank()) return null
    val parts = dms.split(',')
    if (parts.size < 3) return null
    fun parseRational(token: String): Double? {
      val trimmed = token.trim()
      if ('/' in trimmed) {
        val numDen = trimmed.split('/')
        val num = numDen.getOrNull(0)?.trim()?.toDoubleOrNull() ?: return null
        val den = numDen.getOrNull(1)?.trim()?.toDoubleOrNull() ?: return null
        return if (den != 0.0) num / den else 0.0
      }
      return trimmed.toDoubleOrNull()
    }
    val deg = parseRational(parts[0]) ?: return null
    val min = parseRational(parts[1]) ?: return null
    val sec = parseRational(parts[2]) ?: return null
    val unsigned = deg + (min / 60.0) + (sec / 3600.0)
    val isNegative = ref.equals("S", ignoreCase = true) || ref.equals("W", ignoreCase = true)
    return if (isNegative) -unsigned else unsigned
  }

  /**
   * Reads [ImageMetadata] from [file] using both Android [ExifInterface] and the lossless
   * binary trailer chunk (supporting JPEG, PNG, WebP, and local JVM unit tests).
   */
  fun readImageMetadata(file: File): ImageMetadata {
    if (!file.exists() || !file.isFile || file.length() <= 0L) return ImageMetadata()

    var metadata = readTrailerMetadata(file) ?: ImageMetadata()

    try {
      val exif = ExifInterface(file.absolutePath)
      val exifTitle = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)?.trim().orEmpty()
      val exifArtist = exif.getAttribute(ExifInterface.TAG_ARTIST)?.trim().orEmpty()
      val exifCopyright = exif.getAttribute(ExifInterface.TAG_COPYRIGHT)?.trim().orEmpty()
      val exifDate = (
        exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
          ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
        )?.trim().orEmpty()
      val exifMake = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim().orEmpty()
      val exifModel = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim().orEmpty()
      val exifSoftware = exif.getAttribute(ExifInterface.TAG_SOFTWARE)?.trim().orEmpty()

      val latDms = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
      val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
      val lonDms = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
      val lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
      val latDec = parseExifDmsToDecimal(latDms, latRef)
      val lonDec = parseExifDmsToDecimal(lonDms, lonRef)
      val exifLocation = if (latDec != null && lonDec != null) {
        String.format(Locale.US, "%.4f, %.4f", latDec, lonDec)
      } else {
        exif.getAttribute(ExifInterface.TAG_USER_COMMENT)?.trim().orEmpty()
      }

      metadata = metadata.copy(
        title = metadata.title.ifBlank { exifTitle },
        artist = metadata.artist.ifBlank { exifArtist },
        copyright = metadata.copyright.ifBlank { exifCopyright },
        dateTaken = metadata.dateTaken.ifBlank { exifDate },
        cameraMake = metadata.cameraMake.ifBlank { exifMake },
        cameraModel = metadata.cameraModel.ifBlank { exifModel },
        software = metadata.software.ifBlank { exifSoftware },
        location = metadata.location.ifBlank { exifLocation },
      )
    } catch (_: Throwable) {
      // ExifInterface may be stubbed in local JVM unit tests
    }

    return metadata.sanitized()
  }

  /**
   * Reads [ImageMetadata] from a content [uri] via [ExifInterface].
   */
  fun readImageMetadataFromUri(context: Context, uri: Uri): ImageMetadata {
    return try {
      context.contentResolver.openInputStream(uri)?.use { stream ->
        val exif = ExifInterface(stream)
        val exifTitle = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)?.trim().orEmpty()
        val exifArtist = exif.getAttribute(ExifInterface.TAG_ARTIST)?.trim().orEmpty()
        val exifCopyright = exif.getAttribute(ExifInterface.TAG_COPYRIGHT)?.trim().orEmpty()
        val exifDate = (
          exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
          )?.trim().orEmpty()
        val exifMake = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim().orEmpty()
        val exifModel = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim().orEmpty()
        val exifSoftware = exif.getAttribute(ExifInterface.TAG_SOFTWARE)?.trim().orEmpty()
        val latDec = parseExifDmsToDecimal(
          exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
          exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF),
        )
        val lonDec = parseExifDmsToDecimal(
          exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
          exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF),
        )
        val exifLocation = if (latDec != null && lonDec != null) {
          String.format(Locale.US, "%.4f, %.4f", latDec, lonDec)
        } else {
          exif.getAttribute(ExifInterface.TAG_USER_COMMENT)?.trim().orEmpty()
        }
        ImageMetadata(
          title = exifTitle,
          artist = exifArtist,
          copyright = exifCopyright,
          dateTaken = exifDate,
          cameraMake = exifMake,
          cameraModel = exifModel,
          software = exifSoftware,
          location = exifLocation,
        ).sanitized()
      } ?: ImageMetadata()
    } catch (_: Throwable) {
      ImageMetadata()
    }
  }

  /**
   * Writes [metadata] into [file] using Android [ExifInterface] (when supported by the container)
   * and updates the lossless metadata trailer chunk.
   */
  fun writeImageMetadata(file: File, metadata: ImageMetadata): Boolean {
    if (!file.exists() || !file.isFile) return false
    val clean = metadata.sanitized()
    return try {
      stripTrailerMetadata(file)

      try {
        val exif = ExifInterface(file.absolutePath)
        exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, clean.title.ifEmpty { null })
        exif.setAttribute(ExifInterface.TAG_ARTIST, clean.artist.ifEmpty { null })
        exif.setAttribute(ExifInterface.TAG_COPYRIGHT, clean.copyright.ifEmpty { null })
        exif.setAttribute(ExifInterface.TAG_DATETIME, clean.dateTaken.ifEmpty { null })
        exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, clean.dateTaken.ifEmpty { null })
        exif.setAttribute(ExifInterface.TAG_MAKE, clean.cameraMake.ifEmpty { null })
        exif.setAttribute(ExifInterface.TAG_MODEL, clean.cameraModel.ifEmpty { null })
        exif.setAttribute(ExifInterface.TAG_SOFTWARE, clean.software.ifEmpty { null })

        val coords = parseGpsCoordinates(clean.location)
        if (coords != null) {
          val (lat, lon) = coords
          exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, formatDecimalToExifDms(lat))
          exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (lat >= 0.0) "N" else "S")
          exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, formatDecimalToExifDms(lon))
          exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if (lon >= 0.0) "E" else "W")
          exif.setAttribute(ExifInterface.TAG_USER_COMMENT, null)
        } else {
          exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
          exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
          exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
          exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null)
          exif.setAttribute(ExifInterface.TAG_USER_COMMENT, clean.location.ifEmpty { null })
        }
        exif.saveAttributes()
      } catch (_: Throwable) {
        // ExifInterface may throw on unsupported container formats or in JVM unit tests
      }

      if (!clean.isEmpty) {
        appendTrailerMetadata(file, clean)
      }
      true
    } catch (_: Throwable) {
      false
    }
  }

  private fun stripTrailerMetadata(file: File) {
    val magicBytes = TRAILER_MAGIC.toByteArray(Charsets.US_ASCII)
    val footerLen = 4 + magicBytes.size // 12 bytes
    if (file.length() < footerLen + magicBytes.size) return
    RandomAccessFile(file, "rw").use { raf ->
      val len = raf.length()
      raf.seek(len - footerLen)
      val sizeBytes = ByteArray(4)
      val tailMagic = ByteArray(magicBytes.size)
      raf.readFully(sizeBytes)
      raf.readFully(tailMagic)
      if (!tailMagic.contentEquals(magicBytes)) return
      val payloadSize = ByteBuffer.wrap(sizeBytes).order(ByteOrder.BIG_ENDIAN).int
      val blockStart = len - footerLen - payloadSize - magicBytes.size
      if (payloadSize in 0..65536 && blockStart >= 0L) {
        raf.seek(blockStart)
        val headMagic = ByteArray(magicBytes.size)
        raf.readFully(headMagic)
        if (headMagic.contentEquals(magicBytes)) {
          raf.setLength(blockStart)
        }
      }
    }
  }

  private fun appendTrailerMetadata(file: File, metadata: ImageMetadata) {
    val magicBytes = TRAILER_MAGIC.toByteArray(Charsets.US_ASCII)
    val payloadBos = ByteArrayOutputStream()
    DataOutputStream(payloadBos).use { dos ->
      dos.writeUTF(metadata.title)
      dos.writeUTF(metadata.artist)
      dos.writeUTF(metadata.copyright)
      dos.writeUTF(metadata.dateTaken)
      dos.writeUTF(metadata.cameraMake)
      dos.writeUTF(metadata.cameraModel)
      dos.writeUTF(metadata.software)
      dos.writeUTF(metadata.location)
    }
    val payload = payloadBos.toByteArray()
    RandomAccessFile(file, "rw").use { raf ->
      raf.seek(raf.length())
      raf.write(magicBytes)
      raf.write(payload)
      val sizeBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(payload.size).array()
      raf.write(sizeBytes)
      raf.write(magicBytes)
    }
  }

  private fun readTrailerMetadata(file: File): ImageMetadata? {
    val magicBytes = TRAILER_MAGIC.toByteArray(Charsets.US_ASCII)
    val footerLen = 4 + magicBytes.size
    if (file.length() < footerLen + magicBytes.size) return null
    return try {
      RandomAccessFile(file, "r").use { raf ->
        val len = raf.length()
        raf.seek(len - footerLen)
        val sizeBytes = ByteArray(4)
        val tailMagic = ByteArray(magicBytes.size)
        raf.readFully(sizeBytes)
        raf.readFully(tailMagic)
        if (!tailMagic.contentEquals(magicBytes)) return null
        val payloadSize = ByteBuffer.wrap(sizeBytes).order(ByteOrder.BIG_ENDIAN).int
        val blockStart = len - footerLen - payloadSize - magicBytes.size
        if (payloadSize !in 1..65536 || blockStart < 0L) return null
        raf.seek(blockStart)
        val headMagic = ByteArray(magicBytes.size)
        raf.readFully(headMagic)
        if (!headMagic.contentEquals(magicBytes)) return null
        val payload = ByteArray(payloadSize)
        raf.readFully(payload)
        DataInputStream(ByteArrayInputStream(payload)).use { dis ->
          ImageMetadata(
            title = dis.readUTF(),
            artist = dis.readUTF(),
            copyright = dis.readUTF(),
            dateTaken = dis.readUTF(),
            cameraMake = dis.readUTF(),
            cameraModel = dis.readUTF(),
            software = dis.readUTF(),
            location = dis.readUTF(),
          ).sanitized()
        }
      }
    } catch (_: Throwable) {
      null
    }
  }
}
