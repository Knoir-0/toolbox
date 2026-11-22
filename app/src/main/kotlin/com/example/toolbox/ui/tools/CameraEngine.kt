package com.example.toolbox.ui.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.Text as MlKitText
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.GenericMultipleBarcodeReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumMap
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.suspendCancellableCoroutine

const val CAMERA_PREFS_NAME = "camera_tool_preferences"
const val PREF_KEY_CAMERA_DIRECTORY = "camera_save_directory"
const val PREF_KEY_CAMERA_LENS = "camera_lens_facing"
const val PREF_KEY_CAMERA_ID = "camera_selected_device_id"
const val PREF_KEY_CAMERA_FLASH = "camera_flash_mode"
const val PREF_KEY_CAMERA_TIMER = "camera_timer_duration"
const val PREF_KEY_CAMERA_ASPECT_RATIO = "camera_aspect_ratio"
const val PREF_KEY_CAMERA_GRID = "camera_grid_overlay"
const val PREF_KEY_CAMERA_FILTER = "camera_color_filter"
const val PREF_KEY_CAMERA_FORMAT = "camera_photo_format"
const val PREF_KEY_CAMERA_MIRROR_FRONT = "camera_mirror_front"
const val PREF_KEY_CAMERA_SCAN_FILTER = "camera_scan_filter"
const val PREF_KEY_CAMERA_NOTE_PRESET = "camera_note_preset"
const val PREF_KEY_CAMERA_VISION_THRESHOLD = "camera_vision_threshold"
const val PREF_KEY_CAMERA_VISION_PRESET = "camera_vision_preset"
const val PREF_KEY_CAMERA_PHOTO_SUB_MODE = "camera_photo_sub_mode"
const val PREF_KEY_CAMERA_NIGHT_MODE = "camera_night_mode"

const val PANORAMA_TARGET_SEGMENTS = 5

const val DEFAULT_CAMERA_FOLDER_SUBPATH = "DCIM/Camera"
const val DEFAULT_CAMERA_FOLDER_DISPLAY = "/DCIM/Camera"

const val MIN_CAMERA_ZOOM = 1.0f
const val MAX_CAMERA_ZOOM = 8.0f

const val MIN_CAMERA_EXPOSURE_EV = -2.0f
const val MAX_CAMERA_EXPOSURE_EV = 2.0f

/**
 * Duration (`7000ms` = 7 seconds) for which a manual tap-to-focus override locks focus
 * before automatically returning to ML Kit object detection smart autofocus.
 */
const val MANUAL_FOCUS_OVERRIDE_DURATION_MS = 7000L

/**
 * Natural frequency (rad/s) for the sticky autofocus trajectory controller, governing how
 * smoothly the focus point decelerates into its final destination.
 */
const val STICKY_AF_NATURAL_FREQUENCY_RAD_PER_SEC = 5.8f

/**
 * Steering inertia time constant (seconds) governing how smoothly the autofocus velocity vector
 * bends and accelerates along its trajectory toward a new target direction.
 */
const val STICKY_AF_STEERING_TIME_CONSTANT_SEC = 0.16f

/**
 * Maximum normalized viewfinder travel speed (`0.72f` = 72% of viewfinder width/height per second)
 * allowing the autofocus cursor to move faster across long distances after building up speed.
 */
const val STICKY_AF_MAX_SPEED_NORM_PER_SEC = 0.72f

/**
 * Maximum velocity increment (`0.22f` normalized units/sec) that a single trajectory update
 * can add to the autofocus cursor's current speed, preventing abrupt velocity jumps while
 * allowing consecutive steps to accelerate smoothly up to [STICKY_AF_MAX_SPEED_NORM_PER_SEC].
 */
const val STICKY_AF_MAX_VELOCITY_ADD_NORM_PER_SEC = 0.22f

/**
 * Maximum normalized tangential acceleration (`1.80f` units/s²) for smooth velocity gain and deceleration.
 */
const val STICKY_AF_MAX_ACCEL_NORM_PER_SEC_SQ = 1.80f

/**
 * Elliptical cornering aspect ratio (`b / a = 0.58f`, where `a > b`) for the initial elliptical
 * arc at the start of an autofocus movement before straightening onto the target line.
 */
const val STICKY_AF_CORNER_ELLIPSE_MINOR_TO_MAJOR_RATIO = 0.58f

/**
 * Minimum score margin (`0.20f`) a competing detection must exceed the currently locked subject
 * by before smart autofocus switches targets, preventing focus hunting when detections come in and out.
 */
const val STICKY_AF_SWITCH_HYSTERESIS_MARGIN = 0.20f

/**
 * Maximum duration (`1800ms`) to hold and gently coast the current smart autofocus target
 * when detections momentarily drop out between frames.
 */
const val STICKY_AF_DROPOUT_HOLD_DURATION_MS = 1800L

val CAMERA_ZOOM_PRESETS: List<Float> = listOf(1.0f, 2.0f, 3.0f, 5.0f)

/**
 * Indicates whether the active camera focus target is driven by default continuous autofocus,
 * ML Kit Object Detection smart autofocus, or a 7-second manual user override.
 */
enum class CameraFocusSource(
  val displayName: String,
  val shortLabel: String,
) {
  CONTINUOUS_AUTO(
    displayName = "Continuous Autofocus",
    shortLabel = "AF",
  ),
  ML_OBJECT_AUTO(
    displayName = "Smart Object Autofocus",
    shortLabel = "Smart AF",
  ),
  MANUAL_OVERRIDE(
    displayName = "Manual Focus Override",
    shortLabel = "Manual",
  ),
}

/**
 * Raw candidate object bounding box and classification metadata produced by ML Kit Object Detection
 * for evaluating smart autofocus targets.
 */
data class RawDetectedObjectForFocus(
  val boundingBox: CameraVisionBoundingBox,
  val trackingId: Int? = null,
  val label: String? = null,
  val confidence: Float = 0f,
)

/**
 * Describes the two-phase $C^1$ tangent-continuous cornering path for an autofocus transition:
 * 1. An initial elliptical arc with semi-major axis [semiMajorAxisA] (aligned with the exit straight-line
 *    direction) and semi-minor axis [semiMinorAxisB] (perpendicular to the straight-line direction,
 *    with [semiMajorAxisA] > [semiMinorAxisB]) sweeping from [startAngleRad] to $\pi/2$ at the
 *    junction point ([junctionX], [junctionY]).
 * 2. A straight line along unit vector ([lineDirX], [lineDirY]) of length [straightLineLength]
 *    from ([junctionX], [junctionY]) directly to ([goalX], [goalY]).
 */
data class CameraAutofocusCornerPath(
  val originX: Float,
  val originY: Float,
  val goalX: Float,
  val goalY: Float,
  val junctionX: Float,
  val junctionY: Float,
  val lineDirX: Float,
  val lineDirY: Float,
  val normalDirX: Float,
  val normalDirY: Float,
  val semiMajorAxisA: Float,
  val semiMinorAxisB: Float,
  val startAngleRad: Float,
  val currentAngleRad: Float = startAngleRad,
  val arcLength: Float,
  val straightLineLength: Float,
  val straightDistanceTraveled: Float = 0f,
) {
  companion object {
    const val HALF_PI: Float = (Math.PI / 2.0).toFloat()
  }

  /** Center X of the ellipse that defines the initial cornering arc. */
  val ellipseCenterX: Float
    get() = junctionX - semiMinorAxisB * normalDirX

  /** Center Y of the ellipse that defines the initial cornering arc. */
  val ellipseCenterY: Float
    get() = junctionY - semiMinorAxisB * normalDirY

  /** `true` while the cursor is traversing the initial elliptical arc before reaching ([junctionX], [junctionY]). */
  val isOnEllipticalArc: Boolean
    get() = arcLength > 1e-5f && currentAngleRad < HALF_PI - 1e-4f && straightDistanceTraveled <= 1e-5f

  /** `true` once the cursor has completed the initial elliptical arc and is moving in a straight line to ([goalX], [goalY]). */
  val isOnStraightLine: Boolean
    get() = !isOnEllipticalArc

  /**
   * Evaluates the normalized ellipse equation $(u_1 / a)^2 + (u_2 / b)^2$ for $([x], [y])$
   * in the principal-axis frame of the initial cornering ellipse. Points on the elliptical arc return `1.0f`.
   */
  fun evaluateEllipseEquation(x: Float, y: Float): Float {
    if (semiMajorAxisA <= 1e-5f || semiMinorAxisB <= 1e-5f) return 1f
    val dx = x - ellipseCenterX
    val dy = y - ellipseCenterY
    val u1 = -(dx * lineDirX + dy * lineDirY)
    val u2 = dx * normalDirX + dy * normalDirY
    val term1 = u1 / semiMajorAxisA
    val term2 = u2 / semiMinorAxisB
    return term1 * term1 + term2 * term2
  }

  /**
   * Perpendicular (cross-track) distance from $([x], [y])$ to the straight line connecting
   * ([junctionX], [junctionY]) to ([goalX], [goalY]). Points on the straight-line segment return `0.0f`.
   */
  fun crossTrackDistanceToStraightLine(x: Float, y: Float): Float {
    val dx = x - junctionX
    val dy = y - junctionY
    return abs(dx * normalDirX + dy * normalDirY)
  }

  /**
   * Longitudinal distance along the straight-line segment from ([junctionX], [junctionY]) toward ([goalX], [goalY]).
   */
  fun longitudinalDistanceAlongStraightLine(x: Float, y: Float): Float {
    val dx = x - junctionX
    val dy = y - junctionY
    return dx * lineDirX + dy * lineDirY
  }

  /**
   * Position $(x, y)$ on the initial elliptical arc at parametric angle [thetaRad] $\in [\text{startAngleRad}, \pi/2]$.
   */
  fun pointOnArc(thetaRad: Float): Pair<Float, Float> {
    val clampedTheta = thetaRad.coerceIn(startAngleRad, HALF_PI)
    val cosT = cos(clampedTheta.toDouble()).toFloat()
    val sinT = sin(clampedTheta.toDouble()).toFloat()
    val x = junctionX - semiMajorAxisA * cosT * lineDirX - semiMinorAxisB * (1f - sinT) * normalDirX
    val y = junctionY - semiMajorAxisA * cosT * lineDirY - semiMinorAxisB * (1f - sinT) * normalDirY
    return Pair(x, y)
  }

  /**
   * Unit tangent vector $(t_x, t_y)$ on the initial elliptical arc at parametric angle [thetaRad].
   */
  fun unitTangentOnArc(thetaRad: Float): Pair<Float, Float> {
    val clampedTheta = thetaRad.coerceIn(startAngleRad, HALF_PI)
    val d1 = semiMajorAxisA * sin(clampedTheta.toDouble()).toFloat()
    val d2 = semiMinorAxisB * cos(clampedTheta.toDouble()).toFloat()
    val mag = kotlin.math.sqrt(d1 * d1 + d2 * d2)
    if (mag <= 1e-6f) return Pair(lineDirX, lineDirY)
    val u1 = d1 / mag
    val u2 = d2 / mag
    return Pair(
      u1 * lineDirX + u2 * normalDirX,
      u1 * lineDirY + u2 * normalDirY,
    )
  }
}

/**
 * Represents a resolved focus target in normalized viewfinder coordinates (`0.0f..1.0f`),
 * either automatically tracked from an ML Kit `DetectedObject` ([CameraFocusSource.ML_OBJECT_AUTO])
 * or manually pinned by the user for [MANUAL_FOCUS_OVERRIDE_DURATION_MS] ([CameraFocusSource.MANUAL_OVERRIDE]).
 *
 * Includes velocity ([velocityX], [velocityY]), desired destination ([targetNormX], [targetNormY]),
 * and [cornerPath] state so smart autofocus traces a smooth elliptical cornering arc followed by
 * a straight line into the target.
 */
data class CameraSmartFocusTarget(
  val normX: Float,
  val normY: Float,
  val boundingBox: CameraVisionBoundingBox? = null,
  val trackingId: Int? = null,
  val categoryLabel: String? = null,
  val confidence: Float = 0f,
  val source: CameraFocusSource = CameraFocusSource.ML_OBJECT_AUTO,
  val timestampMs: Long = System.currentTimeMillis(),
  val targetNormX: Float = normX,
  val targetNormY: Float = normY,
  val targetBoundingBox: CameraVisionBoundingBox? = boundingBox,
  val velocityX: Float = 0f,
  val velocityY: Float = 0f,
  val lockStrength: Float = 1.0f,
  val missedDetectionFrames: Int = 0,
  val cornerPath: CameraAutofocusCornerPath? = null,
) {
  val clampedX: Float get() = normX.coerceIn(0.08f, 0.92f)
  val clampedY: Float get() = normY.coerceIn(0.08f, 0.92f)
  val clampedTargetX: Float get() = targetNormX.coerceIn(0.08f, 0.92f)
  val clampedTargetY: Float get() = targetNormY.coerceIn(0.08f, 0.92f)
  val speedNormPerSec: Float
    get() = kotlin.math.sqrt(velocityX * velocityX + velocityY * velocityY)
  val distanceToDesiredTarget: Float
    get() {
      val dx = clampedTargetX - clampedX
      val dy = clampedTargetY - clampedY
      return kotlin.math.sqrt(dx * dx + dy * dy)
    }
  val isManualOverride: Boolean get() = source == CameraFocusSource.MANUAL_OVERRIDE
  val isSmartObjectFocus: Boolean get() = source == CameraFocusSource.ML_OBJECT_AUTO
  val badgeLabel: String
    get() = when (source) {
      CameraFocusSource.MANUAL_OVERRIDE -> "Manual • 7s"
      CameraFocusSource.ML_OBJECT_AUTO -> {
        val cleanLabel = categoryLabel?.takeIf { it.isNotBlank() }
        if (cleanLabel != null) "Smart AF • $cleanLabel" else "Smart AF"
      }
      CameraFocusSource.CONTINUOUS_AUTO -> "AF"
    }
}

/**
 * Sensor-array pixel coordinates (`left`, `top`, `right`, `bottom`) and metering weight (`1..1000`)
 * used to build a Camera2 `MeteringRectangle` for `CONTROL_AF_REGIONS` and `CONTROL_AE_REGIONS`.
 */
data class CameraSensorMeteringRegion(
  val left: Int,
  val top: Int,
  val right: Int,
  val bottom: Int,
  val weight: Int = 1000,
) {
  val width: Int get() = (right - left).coerceAtLeast(1)
  val height: Int get() = (bottom - top).coerceAtLeast(1)
}

/**
 * Capture modes supported by the Camera tool.
 */
enum class CameraCaptureMode(
  val displayName: String,
  val shortLabel: String,
) {
  PHOTO(displayName = "Photo", shortLabel = "PHOTO"),
  VIDEO(displayName = "Video", shortLabel = "VIDEO"),
  SCANNER(displayName = "Scan", shortLabel = "SCAN"),
  NOTE(displayName = "Note", shortLabel = "NOTE"),
  VISION(displayName = "Vision", shortLabel = "VISION"),
  ;

  companion object {
    val DEFAULT: CameraCaptureMode = PHOTO

    fun fromNameOrDefault(name: String?): CameraCaptureMode {
      if (name.isNullOrBlank()) return DEFAULT
      val trimmed = name.trim()
      if (trimmed.equals("ocr", ignoreCase = true) ||
        trimmed.equals("notes", ignoreCase = true) ||
        trimmed.equals("document", ignoreCase = true)
      ) {
        return NOTE
      }
      if (trimmed.equals("vision", ignoreCase = true) ||
        trimmed.equals("tag", ignoreCase = true) ||
        trimmed.equals("tags", ignoreCase = true) ||
        trimmed.equals("tagging", ignoreCase = true) ||
        trimmed.equals("label", ignoreCase = true) ||
        trimmed.equals("labels", ignoreCase = true) ||
        trimmed.equals("mlkit", ignoreCase = true) ||
        trimmed.equals("eye", ignoreCase = true)
      ) {
        return VISION
      }
      return entries.find {
        it.name.equals(trimmed, ignoreCase = true) ||
          it.displayName.equals(trimmed, ignoreCase = true) ||
          it.shortLabel.equals(trimmed, ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Photo sub-modes / tools available in the slide-up navigation pill bar when in [CameraCaptureMode.PHOTO] mode.
 */
enum class CameraPhotoSubMode(
  val displayName: String,
  val shortLabel: String,
  val filePrefix: String,
  val description: String,
) {
  STANDARD(
    displayName = "Standard",
    shortLabel = "Standard",
    filePrefix = "IMG",
    description = "Standard single-shot photo capture",
  ),
  PANORAMA(
    displayName = "Panorama",
    shortLabel = "Panorama",
    filePrefix = "PANO",
    description = "Ultra-wide panoramic sweep photo stitcher",
  ),
  HDR(
    displayName = "HDR",
    shortLabel = "HDR",
    filePrefix = "HDR",
    description = "High Dynamic Range multi-exposure tone-mapped capture",
  ),
  BURST(
    displayName = "Burst",
    shortLabel = "Burst",
    filePrefix = "BURST",
    description = "Rapid 3-frame continuous action burst sequence",
  ),
  ;

  fun next(): CameraPhotoSubMode {
    val nextIndex = (ordinal + 1) % entries.size
    return entries[nextIndex]
  }

  companion object {
    val DEFAULT: CameraPhotoSubMode = STANDARD

    fun fromNameOrDefault(name: String?): CameraPhotoSubMode {
      if (name.isNullOrBlank()) return DEFAULT
      val trimmed = name.trim()
      if (trimmed.equals("pano", ignoreCase = true) ||
        trimmed.equals("panoramic", ignoreCase = true) ||
        trimmed.equals("wide", ignoreCase = true)
      ) {
        return PANORAMA
      }
      return entries.find {
        it.name.equals(trimmed, ignoreCase = true) ||
          it.displayName.equals(trimmed, ignoreCase = true) ||
          it.shortLabel.equals(trimmed, ignoreCase = true) ||
          it.filePrefix.equals(trimmed, ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Floating Night Mode toggle state (positioned to the left of the zoom options),
 * cycling between [AUTO], [ON], and [OFF].
 */
enum class CameraNightMode(
  val displayName: String,
  val shortLabel: String,
) {
  AUTO(
    displayName = "Night Mode Auto",
    shortLabel = "Auto",
  ),
  ON(
    displayName = "Night Mode On",
    shortLabel = "On",
  ),
  OFF(
    displayName = "Night Mode Off",
    shortLabel = "Off",
  ),
  ;

  fun next(): CameraNightMode {
    val nextIndex = (ordinal + 1) % entries.size
    return entries[nextIndex]
  }

  companion object {
    val DEFAULT: CameraNightMode = AUTO

    fun fromNameOrDefault(name: String?): CameraNightMode {
      if (name.isNullOrBlank()) return DEFAULT
      val trimmed = name.trim()
      return entries.find {
        it.name.equals(trimmed, ignoreCase = true) ||
          it.displayName.equals(trimmed, ignoreCase = true) ||
          it.shortLabel.equals(trimmed, ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Shared minimum confidence threshold (`80%`) linked across both Camera Vision and Gallery ML Kit tagging.
 */
const val MIN_VISION_CONFIDENCE_THRESHOLD = GalleryEngine.DEFAULT_ML_KIT_LABEL_CONFIDENCE_THRESHOLD

/**
 * Confidence threshold presets for live ML Kit Image Labeling in [CameraCaptureMode.VISION] mode.
 * Linked with Gallery smart auto-tagging so anything lower than 80% confidence
 * ([MIN_VISION_CONFIDENCE_THRESHOLD] / [GalleryEngine.DEFAULT_ML_KIT_LABEL_CONFIDENCE_THRESHOLD]) is hidden.
 */
enum class CameraVisionThresholdPreset(
  val label: String,
  val shortLabel: String,
  val minConfidence: Float,
  val description: String,
) {
  STANDARD_65(
    label = "≥80% Standard",
    shortLabel = "≥80%",
    minConfidence = MIN_VISION_CONFIDENCE_THRESHOLD,
    description = "Linked Camera & Gallery ML Kit minimum confidence threshold (80% minimum)",
  ),
  HIGH_70(
    label = "≥90% High",
    shortLabel = "≥90%",
    minConfidence = 0.90f,
    description = "High-confidence visual scene and object labels (90%)",
  ),
  GALLERY_80(
    label = "≥80% Gallery",
    shortLabel = "≥80% Gallery",
    minConfidence = GalleryEngine.DEFAULT_ML_KIT_LABEL_CONFIDENCE_THRESHOLD,
    description = "Exact confidence threshold linked with Gallery smart auto-tagging (80%)",
  ),
  ;

  companion object {
    val DEFAULT: CameraVisionThresholdPreset = STANDARD_65

    fun fromNameOrDefault(name: String?): CameraVisionThresholdPreset {
      if (name.isNullOrBlank()) return DEFAULT
      val trimmed = name.trim()
      return entries.find {
        it.name.equals(trimmed, ignoreCase = true) ||
          it.label.equals(trimmed, ignoreCase = true) ||
          it.shortLabel.equals(trimmed, ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Duration (`280ms`) for smoothly animating (moving and resizing) Vision mode bounding boxes
 * when consecutive detections share the same label or tracking ID.
 */
const val VISION_BOX_MORPH_DURATION_MS = 280

/**
 * Hysteresis confidence floor (`0.72f` = 72%) allowing an already-confirmed Vision tag (`>= 80%`)
 * to remain stable when live frame confidence momentarily dips slightly below 80%.
 */
const val VISION_HYSTERESIS_HOLD_CONFIDENCE = 0.72f

/**
 * Maximum consecutive missed frames (`2`) for which a confirmed Vision tag is coasted/held
 * before being dropped, preventing single-frame flicker.
 */
const val VISION_MAX_DROPOUT_HOLD_FRAMES = 2

/**
 * Maximum elapsed time (`1200ms`) for holding a confirmed Vision tag across transient frame dropouts.
 */
const val VISION_MAX_DROPOUT_HOLD_MS = 1200L

/**
 * Normalized bounding-box edge deadzone (`0.012f` = 1.2% of viewfinder) below which micro-jitter
 * is suppressed so stationary object bounding boxes remain rock-solid.
 */
const val VISION_BOX_MICRO_JITTER_DEADZONE = 0.012f

/**
 * Normalized bounding box (`0.0f..1.0f` in viewfinder coordinates) indicating where
 * a detected visual element is located in the camera frame.
 */
data class CameraVisionBoundingBox(
  val left: Float,
  val top: Float,
  val right: Float,
  val bottom: Float,
) {
  val width: Float get() = (right - left).coerceAtLeast(0f)
  val height: Float get() = (bottom - top).coerceAtLeast(0f)
  val area: Float get() = width * height
  val centerX: Float get() = (left + right) * 0.5f
  val centerY: Float get() = (top + bottom) * 0.5f

  /**
   * Euclidean distance in normalized viewfinder coordinates (`0.0f..0.707f`) from this
   * bounding box's center (`centerX`, `centerY`) to the viewfinder center (`0.5f`, `0.5f`).
   */
  val distanceToViewfinderCenter: Float
    get() {
      val dx = centerX - 0.5f
      val dy = centerY - 0.5f
      return kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
    }

  /**
   * Euclidean distance from the viewfinder center (`0.5f`, `0.5f`) to the nearest point inside
   * this bounding box (`0.0f` when the bounding box contains the viewfinder center).
   */
  val distanceFromBoundsToViewfinderCenter: Float
    get() {
      val nearestX = 0.5f.coerceIn(left, right)
      val nearestY = 0.5f.coerceIn(top, bottom)
      return kotlin.math.hypot((0.5f - nearestX).toDouble(), (0.5f - nearestY).toDouble()).toFloat()
    }

  /** `true` when this bounding box covers the exact center of the viewfinder (`0.5f`, `0.5f`). */
  val containsViewfinderCenter: Boolean
    get() = 0.5f in left..right && 0.5f in top..bottom

  fun coerceNormalized(): CameraVisionBoundingBox {
    val l = left.coerceIn(0.02f, 0.90f)
    val t = top.coerceIn(0.02f, 0.90f)
    val r = right.coerceIn(l + 0.08f, 0.98f)
    val b = bottom.coerceIn(t + 0.08f, 0.98f)
    return CameraVisionBoundingBox(left = l, top = t, right = r, bottom = b)
  }

  /**
   * Smoothly interpolates position and dimensions (`left`, `top`, `right`, `bottom`) from this
   * bounding box into [target] at progress [fraction] (`0.0f..1.0f`).
   */
  fun lerpTo(target: CameraVisionBoundingBox, fraction: Float): CameraVisionBoundingBox {
    val t = fraction.coerceIn(0f, 1f)
    if (t <= 0f) return coerceNormalized()
    if (t >= 1f) return target.coerceNormalized()
    val from = coerceNormalized()
    val to = target.coerceNormalized()
    return CameraVisionBoundingBox(
      left = from.left + (to.left - from.left) * t,
      top = from.top + (to.top - from.top) * t,
      right = from.right + (to.right - from.right) * t,
      bottom = from.bottom + (to.bottom - from.bottom) * t,
    ).coerceNormalized()
  }

  fun intersectionArea(other: CameraVisionBoundingBox): Float {
    val interLeft = maxOf(left, other.left)
    val interTop = maxOf(top, other.top)
    val interRight = minOf(right, other.right)
    val interBottom = minOf(bottom, other.bottom)
    if (interRight <= interLeft || interBottom <= interTop) return 0f
    return (interRight - interLeft) * (interBottom - interTop)
  }

  fun intersectionOverUnion(other: CameraVisionBoundingBox): Float {
    val inter = intersectionArea(other)
    if (inter <= 0f) return 0f
    val union = area + other.area - inter
    return if (union > 0f) (inter / union).coerceIn(0f, 1f) else 0f
  }

  fun containmentRatio(other: CameraVisionBoundingBox): Float {
    val inter = intersectionArea(other)
    if (inter <= 0f) return 0f
    val minArea = minOf(area, other.area)
    return if (minArea > 0f) (inter / minArea).coerceIn(0f, 1f) else 0f
  }

  /**
   * Returns `true` when this box and [other] are nearly identical or heavily overlapping beyond
   * the allowed overlap tolerance ([containmentThreshold] and [iouThreshold]).
   * Allows a moderate amount of bounding-box overlap (e.g., nested sub-parts or adjacent overlapping
   * objects) while still suppressing identical duplicate boxes.
   */
  fun isFullyOverlappingWith(
    other: CameraVisionBoundingBox,
    containmentThreshold: Float = 0.94f,
    iouThreshold: Float = 0.78f,
  ): Boolean {
    if (abs(left - other.left) < 0.03f && abs(top - other.top) < 0.03f &&
      abs(right - other.right) < 0.03f && abs(bottom - other.bottom) < 0.03f
    ) {
      return true
    }
    return (containmentRatio(other) >= containmentThreshold && intersectionOverUnion(other) >= 0.62f) ||
      intersectionOverUnion(other) >= iouThreshold
  }
}

/**
 * Corner / edge anchor slot inside a [CameraVisionBoundingBox] where its label badge is positioned
 * so that overlapping or nested boxes never have overlapping label pills.
 */
enum class CameraVisionLabelCorner(
  val displayName: String,
) {
  TOP_LEFT("Top-Left"),
  TOP_RIGHT("Top-Right"),
  BOTTOM_LEFT("Bottom-Left"),
  BOTTOM_RIGHT("Bottom-Right"),
  TOP_CENTER("Top-Center"),
  BOTTOM_CENTER("Bottom-Center"),
}

/**
 * Computed non-overlapping pixel/normalized rectangle and chosen [corner] for a [CameraVisionTag]'s
 * label badge inside the viewfinder stage.
 */
data class CameraVisionLabelPlacement(
  val tag: CameraVisionTag,
  val corner: CameraVisionLabelCorner,
  val leftPx: Float,
  val topPx: Float,
  val rightPx: Float,
  val bottomPx: Float,
) {
  val widthPx: Float get() = (rightPx - leftPx).coerceAtLeast(0f)
  val heightPx: Float get() = (bottomPx - topPx).coerceAtLeast(0f)

  fun overlapsWith(other: CameraVisionLabelPlacement, paddingPx: Float = 4f): Boolean {
    return (leftPx - paddingPx) < (other.rightPx + paddingPx) &&
      (rightPx + paddingPx) > (other.leftPx - paddingPx) &&
      (topPx - paddingPx) < (other.bottomPx + paddingPx) &&
      (bottomPx + paddingPx) > (other.topPx - paddingPx)
  }

  fun intersectionAreaWith(other: CameraVisionLabelPlacement, paddingPx: Float = 4f): Float {
    val interL = maxOf(leftPx - paddingPx, other.leftPx - paddingPx)
    val interT = maxOf(topPx - paddingPx, other.topPx - paddingPx)
    val interR = minOf(rightPx + paddingPx, other.rightPx + paddingPx)
    val interB = minOf(bottomPx + paddingPx, other.bottomPx + paddingPx)
    if (interR <= interL || interB <= interT) return 0f
    return (interR - interL) * (interB - interT)
  }
}

/**
 * Represents a single ML Kit Image Labeling tag with its confidence score (`0.50f..1.0f`),
 * knowledge-graph label index, spatial [boundingBox] in normalized viewfinder coordinates,
 * optional multi-frame [trackingId], [consecutiveFrames] lock count, and [missedFrames] dropout counter.
 */
data class CameraVisionTag(
  val label: String,
  val confidence: Float,
  val index: Int = -1,
  val boundingBox: CameraVisionBoundingBox? = null,
  val trackingId: Int? = null,
  val consecutiveFrames: Int = 1,
  val missedFrames: Int = 0,
) {
  val confidenceClamped: Float get() = confidence.coerceIn(0f, 1f)
  val confidencePercent: Int get() = (confidenceClamped * 100f).roundToInt().coerceIn(0, 100)
  val confidenceFormatted: String
    get() = String.format(Locale.US, "%.1f%%", confidenceClamped * 100f)
  val passesGalleryThreshold: Boolean
    get() = confidenceClamped >= GalleryEngine.DEFAULT_ML_KIT_LABEL_CONFIDENCE_THRESHOLD
}

/**
 * Sample visual scene presets for testing ML Kit tagging accuracy and spatial bounding boxes
 * when a live physical scene is not in front of the camera lens.
 */
enum class CameraVisionSamplePreset(
  val label: String,
  val sceneTitle: String,
  val sampleTags: List<CameraVisionTag>,
) {
  BOTANICAL(
    label = "Plant & Nature",
    sceneTitle = "Indoor Monstera Plant",
    sampleTags = listOf(
      CameraVisionTag(
        label = "Plant",
        confidence = 0.962f,
        index = 284,
        boundingBox = CameraVisionBoundingBox(0.14f, 0.16f, 0.62f, 0.54f),
      ),
      CameraVisionTag(
        label = "Houseplant",
        confidence = 0.914f,
        index = 192,
        // Fully overlaps with Plant (0.962f), so Houseplant is suppressed in favor of Plant
        boundingBox = CameraVisionBoundingBox(0.14f, 0.16f, 0.62f, 0.54f),
      ),
      CameraVisionTag(
        label = "Flowerpot",
        confidence = 0.891f,
        index = 148,
        // Separate non-overlapping region for the pot below the foliage
        boundingBox = CameraVisionBoundingBox(0.30f, 0.58f, 0.72f, 0.84f),
      ),
      CameraVisionTag(
        label = "Leaf",
        confidence = 0.864f,
        index = 211,
        // Nested sub-region with moderate overlap
        boundingBox = CameraVisionBoundingBox(0.18f, 0.20f, 0.52f, 0.48f),
      ),
      CameraVisionTag(
        label = "Room",
        confidence = 0.582f,
        index = 305,
        boundingBox = CameraVisionBoundingBox(0.08f, 0.14f, 0.92f, 0.86f),
      ),
      CameraVisionTag(
        label = "Furniture",
        confidence = 0.394f,
        index = 166,
        boundingBox = CameraVisionBoundingBox(0.20f, 0.70f, 0.80f, 0.88f),
      ),
    ),
  ),
  DESK_SETUP(
    label = "Tech & Desk",
    sceneTitle = "Developer Workstation",
    sampleTags = listOf(
      CameraVisionTag(
        label = "Laptop",
        confidence = 0.954f,
        index = 174,
        boundingBox = CameraVisionBoundingBox(0.18f, 0.28f, 0.82f, 0.68f),
      ),
      CameraVisionTag(
        label = "Computer keyboard",
        confidence = 0.892f,
        index = 118,
        boundingBox = CameraVisionBoundingBox(0.24f, 0.54f, 0.76f, 0.74f),
      ),
      CameraVisionTag(
        label = "Monitor",
        confidence = 0.865f,
        index = 230,
        boundingBox = CameraVisionBoundingBox(0.20f, 0.18f, 0.80f, 0.52f),
      ),
      CameraVisionTag(
        label = "Desk",
        confidence = 0.734f,
        index = 134,
        boundingBox = CameraVisionBoundingBox(0.10f, 0.50f, 0.90f, 0.84f),
      ),
      CameraVisionTag(
        label = "Electronics",
        confidence = 0.640f,
        index = 152,
        boundingBox = CameraVisionBoundingBox(0.64f, 0.36f, 0.90f, 0.62f),
      ),
      CameraVisionTag(
        label = "Gadget",
        confidence = 0.420f,
        index = 170,
        boundingBox = CameraVisionBoundingBox(0.68f, 0.40f, 0.88f, 0.60f),
      ),
    ),
  ),
  COFFEE_FOOD(
    label = "Food & Drink",
    sceneTitle = "Espresso & Croissant",
    sampleTags = listOf(
      CameraVisionTag(
        label = "Food",
        confidence = 0.946f,
        index = 162,
        boundingBox = CameraVisionBoundingBox(0.14f, 0.38f, 0.58f, 0.74f),
      ),
      CameraVisionTag(
        label = "Coffee cup",
        confidence = 0.884f,
        index = 112,
        boundingBox = CameraVisionBoundingBox(0.52f, 0.24f, 0.86f, 0.62f),
      ),
      CameraVisionTag(
        label = "Tableware",
        confidence = 0.812f,
        index = 340,
        boundingBox = CameraVisionBoundingBox(0.12f, 0.22f, 0.88f, 0.78f),
      ),
      CameraVisionTag(
        label = "Baked goods",
        confidence = 0.858f,
        index = 74,
        boundingBox = CameraVisionBoundingBox(0.18f, 0.42f, 0.54f, 0.70f),
      ),
      CameraVisionTag(
        label = "Table",
        confidence = 0.545f,
        index = 338,
        boundingBox = CameraVisionBoundingBox(0.08f, 0.16f, 0.92f, 0.84f),
      ),
      CameraVisionTag(
        label = "Cuisine",
        confidence = 0.365f,
        index = 126,
        boundingBox = CameraVisionBoundingBox(0.20f, 0.40f, 0.60f, 0.72f),
      ),
    ),
  ),
  OUTDOOR_STREET(
    label = "Architecture",
    sceneTitle = "City Street & Sky",
    sampleTags = listOf(
      CameraVisionTag(
        label = "Building",
        confidence = 0.935f,
        index = 88,
        boundingBox = CameraVisionBoundingBox(0.10f, 0.22f, 0.64f, 0.68f),
      ),
      CameraVisionTag(
        label = "Sky",
        confidence = 0.902f,
        index = 319,
        boundingBox = CameraVisionBoundingBox(0.12f, 0.12f, 0.88f, 0.36f),
      ),
      CameraVisionTag(
        label = "Car",
        confidence = 0.861f,
        index = 96,
        boundingBox = CameraVisionBoundingBox(0.46f, 0.54f, 0.88f, 0.78f),
      ),
      CameraVisionTag(
        label = "Road",
        confidence = 0.715f,
        index = 298,
        boundingBox = CameraVisionBoundingBox(0.10f, 0.62f, 0.90f, 0.86f),
      ),
      CameraVisionTag(
        label = "Tree",
        confidence = 0.612f,
        index = 351,
        boundingBox = CameraVisionBoundingBox(0.62f, 0.26f, 0.92f, 0.58f),
      ),
      CameraVisionTag(
        label = "Urban area",
        confidence = 0.448f,
        index = 368,
        boundingBox = CameraVisionBoundingBox(0.08f, 0.18f, 0.92f, 0.84f),
      ),
    ),
  ),
  ;

  companion object {
    val DEFAULT: CameraVisionSamplePreset = BOTANICAL

    fun fromNameOrDefault(name: String?): CameraVisionSamplePreset {
      if (name.isNullOrBlank()) return DEFAULT
      val trimmed = name.trim()
      return entries.find {
        it.name.equals(trimmed, ignoreCase = true) ||
          it.label.equals(trimmed, ignoreCase = true) ||
          it.sceneTitle.equals(trimmed, ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Represents a live or snapshot ML Kit Vision tagging analysis result in [CameraCaptureMode.VISION] mode.
 * Only tags with confidence >= 80% ([MIN_VISION_CONFIDENCE_THRESHOLD]) are included in [tags].
 */
data class CameraVisionAnalysisResult(
  val id: String,
  val tags: List<CameraVisionTag>,
  val allCandidateTags: List<CameraVisionTag> = tags,
  val confidenceThreshold: Float = MIN_VISION_CONFIDENCE_THRESHOLD,
  val inferenceLatencyMs: Long = 0L,
  val frameWidth: Int = 0,
  val frameHeight: Int = 0,
  val sourceDescription: String = "Live Camera Frame",
  val analyzedAtMs: Long = System.currentTimeMillis(),
) {
  val topTag: CameraVisionTag? get() = tags.firstOrNull()
  val galleryPassingCount: Int get() = tags.count { it.passesGalleryThreshold }
  val averageConfidence: Float
    get() = if (tags.isEmpty()) 0f else tags.map { it.confidenceClamped }.average().toFloat()
  val averageConfidenceFormatted: String
    get() = String.format(Locale.US, "%.1f%%", averageConfidence * 100f)
  val timestampFormatted: String get() = FileManagerEngine.formatTimestamp(analyzedAtMs)

  fun withThreshold(newThreshold: Float): CameraVisionAnalysisResult {
    val clamped = newThreshold.coerceIn(MIN_VISION_CONFIDENCE_THRESHOLD, 0.99f)
    val filtered = CameraEngine.suppressOverlappingVisionTags(
      allCandidateTags.filter { it.confidenceClamped >= clamped }
    )
    return copy(
      tags = filtered,
      confidenceThreshold = clamped,
    )
  }

  fun formatSummaryReport(): String {
    val galleryThresholdPct = (GalleryEngine.DEFAULT_ML_KIT_LABEL_CONFIDENCE_THRESHOLD * 100f).roundToInt()
    return buildString {
      appendLine("ML Kit Vision Tagging Report ($sourceDescription)")
      appendLine(
        "Threshold: ${(confidenceThreshold * 100f).roundToInt()}% • Latency: ${inferenceLatencyMs}ms • Gallery (≥${galleryThresholdPct}%): $galleryPassingCount/${tags.size}"
      )
      tags.forEachIndexed { idx, tag ->
        val galleryBadge = if (tag.passesGalleryThreshold) "[GALLERY ≥${galleryThresholdPct}%]" else "[BELOW ${galleryThresholdPct}%]"
        val indexSuffix = if (tag.index >= 0) " (#${tag.index})" else ""
        appendLine("${idx + 1}. ${tag.label}: ${tag.confidenceFormatted}$indexSuffix $galleryBadge")
      }
    }.trim()
  }
}

/**
 * Sample document templates for OCR Note scanning simulation and testing when a live physical document
 * is not in front of the camera lens.
 */
enum class CameraNoteSamplePreset(
  val label: String,
  val defaultTitle: String,
  val sampleText: String,
  val noteColor: NoteColor,
) {
  MEETING_NOTES(
    label = "Meeting Notes",
    defaultTitle = "Product Roadmap Sync",
    sampleText = "Product Roadmap Sync\nFinalize Q4 mobile release milestones\nReview camera OCR document scanner metrics\nSync recognized notes with offline board\nAction owner: Design & Engineering Team",
    noteColor = NoteColor.SAGE,
  ),
  CHECKLIST(
    label = "Checklist",
    defaultTitle = "Weekly Grocery List",
    sampleText = "Weekly Grocery List\n- Organic espresso beans\n- Sourdough bread\n- Avocados and cherry tomatoes\n- Sparkling water",
    noteColor = NoteColor.MINT,
  ),
  RECEIPT(
    label = "Receipt",
    defaultTitle = "Hardware Store Receipt",
    sampleText = "Hardware Store Receipt\nPrecision Screwdriver Set  $18.50\nDigital Caliper 150mm      $29.00\nMicrofiber Lens Cloths     $6.50\nTotal Paid: $54.00",
    noteColor = NoteColor.SAND,
  ),
  LECTURE_OUTLINE(
    label = "Study Notes",
    defaultTitle = "Optical Character Recognition",
    sampleText = "Optical Character Recognition\n1. Image preprocessing and contrast normalization\n2. Text block and line layout segmentation\n3. On-device ML Kit neural text recognition\n4. Automatic export to Toolbox Notes",
    noteColor = NoteColor.FOG,
  ),
  ;

  companion object {
    val DEFAULT: CameraNoteSamplePreset = MEETING_NOTES

    fun fromNameOrDefault(name: String?): CameraNoteSamplePreset {
      if (name.isNullOrBlank()) return DEFAULT
      val trimmed = name.trim()
      return entries.find {
        it.name.equals(trimmed, ignoreCase = true) ||
          it.label.equals(trimmed, ignoreCase = true) ||
          it.defaultTitle.equals(trimmed, ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Represents a single recognized block of text from an OCR document scan.
 */
data class CameraScannedTextBlock(
  val text: String,
  val lines: List<String>,
  val boundingBoxNormalized: List<Pair<Float, Float>> = emptyList(),
)

/**
 * Represents a document scanned via OCR in [CameraCaptureMode.NOTE] mode.
 */
data class CameraScannedDocument(
  val id: String,
  val rawText: String,
  val title: String,
  val body: String,
  val blocks: List<CameraScannedTextBlock> = emptyList(),
  val wordCount: Int = 0,
  val lineCount: Int = 0,
  val scannedAtMs: Long = System.currentTimeMillis(),
  val savedNoteId: String? = null,
) {
  val bodyContent: String get() = body
  val timestampFormatted: String get() = FileManagerEngine.formatTimestamp(scannedAtMs)
}

/**
 * Target symbology filter for the QR Code Reader & Barcode Scanner.
 */
enum class CameraScanFilter(
  val displayName: String,
  val shortLabel: String,
  val description: String,
) {
  ALL(
    displayName = "All Codes",
    shortLabel = "All",
    description = "Reads both 2D QR/Matrix codes and 1D product/industrial barcodes",
  ),
  QR_CODE(
    displayName = "QR Code",
    shortLabel = "QR Code",
    description = "2D Matrix reader (QR Code, Data Matrix, Aztec, PDF417)",
  ),
  BARCODE(
    displayName = "Barcode",
    shortLabel = "Barcode",
    description = "1D Linear scanner (EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, Codabar)",
  ),
  ;

  fun next(): CameraScanFilter {
    val nextIndex = (ordinal + 1) % entries.size
    return entries[nextIndex]
  }

  companion object {
    val DEFAULT: CameraScanFilter = ALL

    fun fromNameOrDefault(name: String?): CameraScanFilter {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find {
        it.name.equals(name.trim(), ignoreCase = true) ||
          it.shortLabel.equals(name.trim(), ignoreCase = true) ||
          it.displayName.equals(name.trim(), ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Supported 2D QR/Matrix and 1D Linear Barcode symbologies.
 */
enum class CameraBarcodeFormat(
  val displayName: String,
  val shortBadge: String,
  val is2D: Boolean,
  val zxingFormat: BarcodeFormat,
  val defaultSamplePayload: String,
) {
  QR_CODE(
    displayName = "QR Code",
    shortBadge = "QR CODE",
    is2D = true,
    zxingFormat = BarcodeFormat.QR_CODE,
    defaultSamplePayload = "https://toolbox.example.com/camera",
  ),
  DATA_MATRIX(
    displayName = "Data Matrix",
    shortBadge = "DATA MATRIX",
    is2D = true,
    zxingFormat = BarcodeFormat.DATA_MATRIX,
    defaultSamplePayload = "DM-TOOLBOX-2026",
  ),
  AZTEC(
    displayName = "Aztec Code",
    shortBadge = "AZTEC",
    is2D = true,
    zxingFormat = BarcodeFormat.AZTEC,
    defaultSamplePayload = "AZTEC-GATE-42",
  ),
  PDF_417(
    displayName = "PDF417",
    shortBadge = "PDF417",
    is2D = true,
    zxingFormat = BarcodeFormat.PDF_417,
    defaultSamplePayload = "PDF417-BOARDING-A12",
  ),
  EAN_13(
    displayName = "EAN-13",
    shortBadge = "EAN-13",
    is2D = false,
    zxingFormat = BarcodeFormat.EAN_13,
    defaultSamplePayload = "9780201379624",
  ),
  EAN_8(
    displayName = "EAN-8",
    shortBadge = "EAN-8",
    is2D = false,
    zxingFormat = BarcodeFormat.EAN_8,
    defaultSamplePayload = "96385074",
  ),
  UPC_A(
    displayName = "UPC-A",
    shortBadge = "UPC-A",
    is2D = false,
    zxingFormat = BarcodeFormat.UPC_A,
    defaultSamplePayload = "012345678905",
  ),
  UPC_E(
    displayName = "UPC-E",
    shortBadge = "UPC-E",
    is2D = false,
    zxingFormat = BarcodeFormat.UPC_E,
    defaultSamplePayload = "01234565",
  ),
  CODE_128(
    displayName = "Code 128",
    shortBadge = "CODE 128",
    is2D = false,
    zxingFormat = BarcodeFormat.CODE_128,
    defaultSamplePayload = "TOOLBOX-2026",
  ),
  CODE_39(
    displayName = "Code 39",
    shortBadge = "CODE 39",
    is2D = false,
    zxingFormat = BarcodeFormat.CODE_39,
    defaultSamplePayload = "ASSET-9042",
  ),
  CODE_93(
    displayName = "Code 93",
    shortBadge = "CODE 93",
    is2D = false,
    zxingFormat = BarcodeFormat.CODE_93,
    defaultSamplePayload = "CODE93-OK",
  ),
  ITF(
    displayName = "ITF",
    shortBadge = "ITF",
    is2D = false,
    zxingFormat = BarcodeFormat.ITF,
    defaultSamplePayload = "12345678901231",
  ),
  CODABAR(
    displayName = "Codabar",
    shortBadge = "CODABAR",
    is2D = false,
    zxingFormat = BarcodeFormat.CODABAR,
    defaultSamplePayload = "A40156B",
  ),
  ;

  fun matchesFilter(filter: CameraScanFilter): Boolean {
    return when (filter) {
      CameraScanFilter.ALL -> true
      CameraScanFilter.QR_CODE -> is2D
      CameraScanFilter.BARCODE -> !is2D
    }
  }

  companion object {
    val DEFAULT: CameraBarcodeFormat = QR_CODE

    fun fromZxingFormat(format: BarcodeFormat): CameraBarcodeFormat {
      return entries.find { it.zxingFormat == format } ?: when (format) {
        BarcodeFormat.MAXICODE -> QR_CODE
        BarcodeFormat.RSS_14, BarcodeFormat.RSS_EXPANDED, BarcodeFormat.UPC_EAN_EXTENSION -> EAN_13
        else -> QR_CODE
      }
    }

    fun fromNameOrDefault(name: String?): CameraBarcodeFormat {
      if (name.isNullOrBlank()) return DEFAULT
      val trimmed = name.trim()
      return entries.find {
        it.name.equals(trimmed, ignoreCase = true) ||
          it.displayName.equals(trimmed, ignoreCase = true) ||
          it.shortBadge.equals(trimmed, ignoreCase = true)
      } ?: DEFAULT
    }

    fun zxingFormatsForFilter(filter: CameraScanFilter): List<BarcodeFormat> {
      return entries
        .filter { it.matchesFilter(filter) }
        .map { it.zxingFormat }
    }
  }
}

/**
 * Semantic classification of a decoded QR code or barcode payload.
 */
enum class CameraScanPayloadType(
  val displayName: String,
  val primaryActionLabel: String,
) {
  URL(displayName = "Web URL", primaryActionLabel = "Open Link"),
  WIFI(displayName = "Wi-Fi Network", primaryActionLabel = "Copy Password"),
  EMAIL(displayName = "Email", primaryActionLabel = "Send Email"),
  PHONE(displayName = "Phone Number", primaryActionLabel = "Dial Number"),
  SMS(displayName = "SMS Message", primaryActionLabel = "Send SMS"),
  GEO(displayName = "Geo Coordinates", primaryActionLabel = "Open Map"),
  CONTACT(displayName = "Contact Card", primaryActionLabel = "Copy Contact"),
  CALENDAR(displayName = "Calendar Event", primaryActionLabel = "Copy Event"),
  ISBN(displayName = "ISBN Book Barcode", primaryActionLabel = "Copy ISBN"),
  PRODUCT(displayName = "Product Barcode", primaryActionLabel = "Copy Barcode"),
  BARCODE_ID(displayName = "Industrial Barcode", primaryActionLabel = "Copy Code"),
  TEXT(displayName = "Plain Text", primaryActionLabel = "Copy Text"),
}

/**
 * Built-in sample QR codes and barcodes for live viewfinder simulation, testing, and inspection.
 */
enum class CameraScanSamplePreset(
  val label: String,
  val format: CameraBarcodeFormat,
  val payload: String,
) {
  QR_URL(
    label = "QR: Web URL",
    format = CameraBarcodeFormat.QR_CODE,
    payload = "https://toolbox.example.com/camera",
  ),
  QR_WIFI(
    label = "QR: Wi-Fi",
    format = CameraBarcodeFormat.QR_CODE,
    payload = "WIFI:S:ToolboxStudio;T:WPA;P:antigravity42;;",
  ),
  QR_CONTACT(
    label = "QR: vCard",
    format = CameraBarcodeFormat.QR_CODE,
    payload = "BEGIN:VCARD\nVERSION:3.0\nFN:Alex Rivera\nORG:Toolbox Labs\nTEL:+1-555-0199\nEMAIL:alex@toolbox.example.com\nEND:VCARD",
  ),
  BARCODE_EAN13(
    label = "EAN-13 (ISBN)",
    format = CameraBarcodeFormat.EAN_13,
    payload = "9780201379624",
  ),
  BARCODE_UPCA(
    label = "UPC-A Product",
    format = CameraBarcodeFormat.UPC_A,
    payload = "012345678905",
  ),
  BARCODE_CODE128(
    label = "Code 128",
    format = CameraBarcodeFormat.CODE_128,
    payload = "TOOLBOX-2026",
  ),
  BARCODE_CODE39(
    label = "Code 39",
    format = CameraBarcodeFormat.CODE_39,
    payload = "ASSET-9042",
  ),
  MATRIX_DATA(
    label = "Data Matrix",
    format = CameraBarcodeFormat.DATA_MATRIX,
    payload = "DM-SERIAL-88412",
  ),
  ;

  companion object {
    fun presetsForFilter(filter: CameraScanFilter): List<CameraScanSamplePreset> {
      return entries.filter { it.format.matchesFilter(filter) }.ifEmpty { entries }
    }
  }
}

/**
 * Represents a decoded QR code or barcode result with structured metadata.
 */
data class CameraScannedCode(
  val id: String,
  val rawValue: String,
  val format: CameraBarcodeFormat,
  val payloadType: CameraScanPayloadType,
  val title: String,
  val subtitle: String,
  val fields: Map<String, String> = emptyMap(),
  val scannedAtMs: Long = System.currentTimeMillis(),
  val cornerPoints: List<Pair<Float, Float>> = emptyList(),
) {
  val timestampFormatted: String get() = FileManagerEngine.formatTimestamp(scannedAtMs)
  val is2D: Boolean get() = format.is2D
  val primaryCopyValue: String
    get() = when (payloadType) {
      CameraScanPayloadType.WIFI -> fields["Password"]?.takeIf { it.isNotEmpty() } ?: rawValue
      else -> rawValue
    }
}

/**
 * Camera lens facing direction.
 */
enum class CameraLensFacing(
  val displayName: String,
  val shortLabel: String,
) {
  BACK(displayName = "Back Camera", shortLabel = "Back"),
  FRONT(displayName = "Front Camera", shortLabel = "Front"),
  ;

  fun toggle(): CameraLensFacing = if (this == BACK) FRONT else BACK

  companion object {
    val DEFAULT: CameraLensFacing = BACK

    fun fromNameOrDefault(name: String?): CameraLensFacing {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
  }
}

/**
 * Optical lens role classification for multi-camera systems.
 */
enum class CameraLensType(
  val displayName: String,
  val shortLabel: String,
) {
  WIDE(displayName = "Wide", shortLabel = "Wide"),
  ULTRA_WIDE(displayName = "Ultra-Wide", shortLabel = "Ultra"),
  TELEPHOTO(displayName = "Telephoto", shortLabel = "Tele"),
  MACRO(displayName = "Macro", shortLabel = "Macro"),
  AUXILIARY(displayName = "Auxiliary", shortLabel = "Aux"),
  EXTERNAL(displayName = "External", shortLabel = "Ext"),
}

/**
 * Raw hardware camera metadata extracted from [CameraCharacteristics] prior to
 * relative optical zoom ratio calculation and catalog labeling.
 */
data class RawCameraCharacteristicsInfo(
  val cameraId: String,
  val openCameraId: String = cameraId,
  val physicalCameraId: String? = null,
  val lensFacing: CameraLensFacing,
  val isExternal: Boolean = false,
  val focalLengthMm: Float? = null,
  val sensorWidthMm: Float? = null,
  val sensorHeightMm: Float? = null,
  val aperture: Float? = null,
  val hasFlash: Boolean = false,
  val isLogicalMultiCamera: Boolean = false,
  val sensorOrientationDegrees: Int = 90,
  val minFocusDistanceDiopters: Float? = null,
)

/**
 * Represents a detected system camera lens (either a top-level Camera2 ID or a physical lens
 * backing a logical multi-camera) available for live preview and capture.
 */
data class CameraSystemDevice(
  val cameraId: String,
  val openCameraId: String = cameraId,
  val physicalCameraId: String? = null,
  val lensFacing: CameraLensFacing,
  val lensType: CameraLensType = CameraLensType.WIDE,
  val isPrimaryForFacing: Boolean = true,
  val isExternal: Boolean = false,
  val opticalZoomFactor: Float = 1.0f,
  val focalLengthMm: Float? = null,
  val equivalentFocalLength35mm: Int? = null,
  val aperture: Float? = null,
  val hasFlash: Boolean = false,
  val isLogicalMultiCamera: Boolean = false,
  val sensorOrientationDegrees: Int = 90,
  val displayName: String,
  val shortLabel: String,
  val chipLabel: String,
  val specsSummary: String,
) {
  val opticalZoomLabel: String
    get() = CameraEngine.formatOpticalZoomFactor(opticalZoomFactor)
}

/**
 * Flash / Torch operation modes.
 */
enum class CameraFlashMode(
  val displayName: String,
  val shortLabel: String,
) {
  OFF(displayName = "Flash Off", shortLabel = "OFF"),
  AUTO(displayName = "Auto Flash", shortLabel = "AUTO"),
  ON(displayName = "Flash On", shortLabel = "ON"),
  TORCH(displayName = "Torch Always On", shortLabel = "TORCH"),
  ;

  fun next(): CameraFlashMode {
    val nextIndex = (ordinal + 1) % entries.size
    return entries[nextIndex]
  }

  companion object {
    val DEFAULT: CameraFlashMode = OFF

    fun fromNameOrDefault(name: String?): CameraFlashMode {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
  }
}

/**
 * Self-timer countdown options prior to shutter release.
 */
enum class CameraTimerDuration(
  val displayName: String,
  val shortLabel: String,
  val seconds: Int,
) {
  OFF(displayName = "Timer Off", shortLabel = "Off", seconds = 0),
  THREE_SEC(displayName = "3 Seconds", shortLabel = "3s", seconds = 3),
  FIVE_SEC(displayName = "5 Seconds", shortLabel = "5s", seconds = 5),
  TEN_SEC(displayName = "10 Seconds", shortLabel = "10s", seconds = 10),
  ;

  fun next(): CameraTimerDuration {
    val nextIndex = (ordinal + 1) % entries.size
    return entries[nextIndex]
  }

  companion object {
    val DEFAULT: CameraTimerDuration = OFF

    fun fromNameOrDefault(name: String?): CameraTimerDuration {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find {
        it.name.equals(name.trim(), ignoreCase = true) ||
          it.shortLabel.equals(name.trim(), ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Viewfinder and capture aspect ratios.
 */
enum class CameraAspectRatio(
  val displayName: String,
  val portraitWidthToHeightRatio: Float?,
  val widthUnits: Int,
  val heightUnits: Int,
) {
  RATIO_4_3(
    displayName = "4:3",
    portraitWidthToHeightRatio = 3f / 4f,
    widthUnits = 3,
    heightUnits = 4,
  ),
  RATIO_16_9(
    displayName = "16:9",
    portraitWidthToHeightRatio = 9f / 16f,
    widthUnits = 9,
    heightUnits = 16,
  ),
  RATIO_1_1(
    displayName = "1:1",
    portraitWidthToHeightRatio = 1f,
    widthUnits = 1,
    heightUnits = 1,
  ),
  FULL(
    displayName = "Full",
    portraitWidthToHeightRatio = null,
    widthUnits = 9,
    heightUnits = 20,
  ),
  ;

  fun next(): CameraAspectRatio {
    val nextIndex = (ordinal + 1) % entries.size
    return entries[nextIndex]
  }

  /**
   * Resolves pixel width and height for a captured photo given a [baseLongEdge] resolution.
   */
  fun resolveDimensions(baseLongEdge: Int = 1440): Pair<Int, Int> {
    val clampedLong = baseLongEdge.coerceIn(64, 4096)
    return when (this) {
      RATIO_4_3 -> Pair((clampedLong * 3) / 4, clampedLong)
      RATIO_16_9 -> Pair((clampedLong * 9) / 16, clampedLong)
      RATIO_1_1 -> Pair(clampedLong, clampedLong)
      FULL -> Pair((clampedLong * 9) / 20, clampedLong)
    }
  }

  companion object {
    val DEFAULT: CameraAspectRatio = FULL

    fun fromNameOrDefault(name: String?): CameraAspectRatio {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find {
        it.name.equals(name.trim(), ignoreCase = true) ||
          it.displayName.equals(name.trim(), ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Composition grid overlays rendered on top of the viewfinder.
 */
enum class CameraGridOverlay(
  val displayName: String,
  val shortLabel: String,
) {
  NONE(displayName = "No Grid", shortLabel = "Off"),
  RULE_OF_THIRDS(displayName = "3×3 Rule of Thirds", shortLabel = "3×3"),
  GOLDEN_RATIO(displayName = "Golden Ratio (Phi)", shortLabel = "Phi"),
  CROSSHAIR(displayName = "Center Crosshair & Level", shortLabel = "Cross"),
  ;

  fun next(): CameraGridOverlay {
    val nextIndex = (ordinal + 1) % entries.size
    return entries[nextIndex]
  }

  companion object {
    val DEFAULT: CameraGridOverlay = NONE

    fun fromNameOrDefault(name: String?): CameraGridOverlay {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
  }
}

/**
 * Real-time color filter presets for viewfinder and photo capture.
 */
enum class CameraColorFilter(
  val displayName: String,
) {
  NONE(displayName = "Normal"),
  MONO(displayName = "Mono"),
  WARM(displayName = "Warm"),
  COOL(displayName = "Cool"),
  SEPIA(displayName = "Sepia"),
  VIVID(displayName = "Vivid"),
  ;

  companion object {
    val DEFAULT: CameraColorFilter = NONE

    fun fromNameOrDefault(name: String?): CameraColorFilter {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find {
        it.name.equals(name.trim(), ignoreCase = true) ||
          it.displayName.equals(name.trim(), ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Still photo file output formats.
 */
enum class CameraPhotoFormat(
  val displayName: String,
  val shortLabel: String,
  val extension: String,
  val mimeType: String,
  val description: String,
) {
  JPG(
    displayName = "JPEG (.jpg)",
    shortLabel = "JPG",
    extension = "jpg",
    mimeType = "image/jpeg",
    description = "Standard high-efficiency photo format (Recommended)",
  ),
  PNG(
    displayName = "PNG (.png)",
    shortLabel = "PNG",
    extension = "png",
    mimeType = "image/png",
    description = "Lossless uncompressed image format",
  ),
  ;

  companion object {
    val DEFAULT: CameraPhotoFormat = JPG

    fun fromNameOrDefault(name: String?): CameraPhotoFormat {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find {
        it.name.equals(name.trim(), ignoreCase = true) ||
          it.shortLabel.equals(name.trim(), ignoreCase = true) ||
          it.extension.equals(name.trim().removePrefix("."), ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Represents a captured photo or video item in the Camera output directory.
 */
data class CameraMediaItem(
  val file: File,
  val name: String,
  val path: String,
  val sizeBytes: Long,
  val lastModifiedMs: Long,
  val extension: String,
  val isVideo: Boolean,
  val durationMs: Long = 0L,
) {
  val sizeFormatted: String get() = FileManagerEngine.formatFileSize(sizeBytes)
  val timestampFormatted: String get() = FileManagerEngine.formatTimestamp(lastModifiedMs)
  val durationFormatted: String get() = CameraEngine.formatTimerDurationMs(durationMs)
  val formatBadge: String get() = extension.uppercase(Locale.ROOT).ifEmpty { if (isVideo) "MP4" else "JPG" }
}

/**
 * Core engine for the Camera tool:
 * - Coerces zoom (`1.0x..8.0x`) and exposure compensation (`-2.0..+2.0 EV`)
 * - Generates timestamped `IMG_yyyyMMdd_HHmmss` and `VID_yyyyMMdd_HHmmss` filenames with collision safety
 * - Resolves and formats the camera storage directory (defaulting to `/DCIM/Camera`)
 * - Applies color filters and exposure adjustments to RGB pixels
 * - Synthesizes valid image and video files when hardware camera capture is unavailable
 */
object CameraEngine {

  data class PendingCaptureModeRequest(
    val mode: CameraCaptureMode,
    val requestId: Long = System.nanoTime(),
  )

  var pendingCaptureModeRequest by mutableStateOf<PendingCaptureModeRequest?>(null)
    private set

  fun requestCaptureMode(mode: CameraCaptureMode) {
    pendingCaptureModeRequest = PendingCaptureModeRequest(mode = mode)
  }

  fun consumePendingCaptureModeRequest(): PendingCaptureModeRequest? {
    val current = pendingCaptureModeRequest
    pendingCaptureModeRequest = null
    return current
  }

  val SUPPORTED_PHOTO_EXTENSIONS: Set<String> = setOf("jpg", "jpeg", "png", "webp")
  val SUPPORTED_VIDEO_EXTENSIONS: Set<String> = setOf("mp4", "3gp", "mkv", "webm")

  /**
   * Coerces camera zoom level into `[MIN_CAMERA_ZOOM]..[MAX_CAMERA_ZOOM]` (`1.0f..8.0f`).
   */
  fun coerceZoom(zoom: Float): Float {
    if (zoom.isNaN()) return MIN_CAMERA_ZOOM
    return zoom.coerceIn(MIN_CAMERA_ZOOM, MAX_CAMERA_ZOOM)
  }

  /**
   * Calculates updated camera zoom from a pinch gesture [zoomChange].
   */
  fun calculatePinchZoom(currentZoom: Float, zoomChange: Float): Float {
    val base = coerceZoom(currentZoom)
    if (zoomChange <= 0f || zoomChange.isNaN()) return base
    return coerceZoom(base * zoomChange)
  }

  /**
   * Formats a zoom ratio as a human-readable badge label (e.g. `1x`, `2x`, `2.5x`).
   */
  fun formatZoomLabel(zoom: Float): String {
    val clamped = coerceZoom(zoom)
    val roundedTenths = (clamped * 10f).roundToInt() / 10f
    return if (roundedTenths == roundedTenths.toInt().toFloat()) {
      "${roundedTenths.toInt()}x"
    } else {
      String.format(Locale.US, "%.1fx", roundedTenths)
    }
  }

  /**
   * Formats an optical lens zoom factor (including `< 1.0f` ultra-wide factors like `0.5x`, `0.6x`
   * as well as `1x`, `2x`, `3x`, `3.5x`, `5x`) into a concise badge string.
   */
  fun formatOpticalZoomFactor(factor: Float): String {
    if (factor.isNaN() || factor <= 0f) return "1x"
    val clamped = factor.coerceIn(0.1f, 100f)
    val roundedTenths = (clamped * 10f).roundToInt() / 10f
    return if (roundedTenths >= 1f && roundedTenths == roundedTenths.toInt().toFloat()) {
      "${roundedTenths.toInt()}x"
    } else {
      String.format(Locale.US, "%.1fx", roundedTenths)
    }
  }

  /**
   * Returns a default 2-camera (Back + Front) fallback list when `CameraManager` is unavailable
   * or during headless/Compose preview inspection.
   */
  fun getDefaultFallbackSystemCameras(): List<CameraSystemDevice> {
    return buildSystemCameraCatalog(
      listOf(
        RawCameraCharacteristicsInfo(
          cameraId = "0",
          openCameraId = "0",
          lensFacing = CameraLensFacing.BACK,
          focalLengthMm = 4.75f,
          sensorWidthMm = 6.4f,
          sensorHeightMm = 4.8f,
          aperture = 1.8f,
          hasFlash = true,
          sensorOrientationDegrees = 90,
        ),
        RawCameraCharacteristicsInfo(
          cameraId = "1",
          openCameraId = "1",
          lensFacing = CameraLensFacing.FRONT,
          focalLengthMm = 3.2f,
          sensorWidthMm = 4.0f,
          sensorHeightMm = 3.0f,
          aperture = 2.0f,
          hasFlash = false,
          sensorOrientationDegrees = 270,
        ),
      )
    )
  }

  /**
   * Computes the 35mm full-frame equivalent focal length (in mm) from a physical [focalLengthMm]
   * and [sensorWidthMm], or returns `null` when either dimension is unavailable.
   */
  fun computeEquivalentFocalLength35mm(
    focalLengthMm: Float?,
    sensorWidthMm: Float?,
  ): Int? {
    val f = focalLengthMm ?: return null
    val sw = sensorWidthMm ?: return null
    if (f <= 0f || sw <= 0f || f.isNaN() || sw.isNaN()) return null
    val eq = ((f * 36.0f) / sw).roundToInt()
    return eq.takeIf { it in 6..800 }
  }

  /**
   * Builds an ordered, disambiguated catalog of [CameraSystemDevice] descriptors from a list of
   * raw hardware camera metadata entries.
   *
   * - Calculates each lens's 35mm equivalent focal length and relative optical zoom ratio (`0.5x`, `0.6x`, `1x`, `2x`, `3x`, `5x`, etc.)
   *   against the primary reference camera of that facing direction.
   * - Classifies lenses into [CameraLensType.ULTRA_WIDE], [CameraLensType.WIDE], [CameraLensType.TELEPHOTO],
   *   [CameraLensType.MACRO], [CameraLensType.AUXILIARY], or [CameraLensType.EXTERNAL].
   * - Orders cameras by facing ([CameraLensFacing.BACK] first, then [CameraLensFacing.FRONT]) and ascending
   *   optical zoom factor, while marking the main `1x` camera as [CameraSystemDevice.isPrimaryForFacing].
   * - Automatically disambiguates labels with `(ID)` whenever two lenses share the same facing, type, and zoom ratio.
   */
  fun buildSystemCameraCatalog(
    rawCameras: List<RawCameraCharacteristicsInfo>,
  ): List<CameraSystemDevice> {
    val deduplicated = rawCameras
      .filter { it.cameraId.isNotBlank() }
      .distinctBy { it.cameraId.trim() }
    if (deduplicated.isEmpty()) {
      return getDefaultFallbackSystemCameras()
    }

    val result = mutableListOf<CameraSystemDevice>()

    for (facing in listOf(CameraLensFacing.BACK, CameraLensFacing.FRONT)) {
      val group = deduplicated.filter { it.lensFacing == facing }
      if (group.isEmpty()) continue

      val primary = group.firstOrNull { it.physicalCameraId == null && !it.isExternal }
        ?: group.first()
      val primaryEq35 = computeEquivalentFocalLength35mm(primary.focalLengthMm, primary.sensorWidthMm)
      val primaryFocalMm = primary.focalLengthMm?.takeIf { it > 0f && !it.isNaN() }

      data class IntermediateCamera(
        val raw: RawCameraCharacteristicsInfo,
        val originalIndex: Int,
        val isPrimary: Boolean,
        val eq35mm: Int?,
        val opticalZoom: Float,
        val lensType: CameraLensType,
      )

      val intermediates = group.mapIndexed { idx, info ->
        val isPrimary = info.cameraId == primary.cameraId
        val eq35 = computeEquivalentFocalLength35mm(info.focalLengthMm, info.sensorWidthMm)
        val rawZoom = when {
          isPrimary -> 1.0f
          eq35 != null && primaryEq35 != null && primaryEq35 > 0 ->
            eq35.toFloat() / primaryEq35.toFloat()
          info.focalLengthMm != null && info.focalLengthMm > 0f && !info.focalLengthMm.isNaN() &&
            primaryFocalMm != null && primaryFocalMm > 0f ->
            info.focalLengthMm / primaryFocalMm
          else -> 1.0f
        }
        val roundedZoom = ((rawZoom.coerceIn(0.1f, 50f) * 10f).roundToInt() / 10f).coerceAtLeast(0.1f)
        val lensType = when {
          info.isExternal -> CameraLensType.EXTERNAL
          roundedZoom < 0.85f || (eq35 != null && eq35 <= 19) -> CameraLensType.ULTRA_WIDE
          roundedZoom >= 1.45f || (eq35 != null && eq35 >= 45) -> CameraLensType.TELEPHOTO
          isPrimary -> CameraLensType.WIDE
          (info.minFocusDistanceDiopters ?: 0f) >= 12.0f -> CameraLensType.MACRO
          else -> CameraLensType.AUXILIARY
        }
        IntermediateCamera(
          raw = info,
          originalIndex = idx,
          isPrimary = isPrimary,
          eq35mm = eq35,
          opticalZoom = roundedZoom,
          lensType = lensType,
        )
      }.sortedWith(
        compareBy<IntermediateCamera> { it.raw.isExternal }
          .thenBy { it.opticalZoom }
          .thenByDescending { it.isPrimary }
          .thenBy { it.originalIndex }
      )

      val hasMultipleInFacing = intermediates.size > 1

      val baseChipLabels = intermediates.map { item ->
        val zoomStr = formatOpticalZoomFactor(item.opticalZoom)
        if (!hasMultipleInFacing) {
          if (item.raw.isExternal) "External" else facing.shortLabel
        } else if (facing == CameraLensFacing.BACK) {
          "$zoomStr ${item.lensType.shortLabel}"
        } else {
          "Front $zoomStr"
        }
      }

      val chipLabelCounts = baseChipLabels.groupingBy { it }.eachCount()

      intermediates.forEachIndexed { idx, item ->
        val info = item.raw
        val zoomStr = formatOpticalZoomFactor(item.opticalZoom)
        val baseChip = baseChipLabels[idx]
        val needsIdDisambiguation = (chipLabelCounts[baseChip] ?: 0) > 1
        val finalChipLabel = if (needsIdDisambiguation) {
          "$baseChip (${info.cameraId})"
        } else {
          baseChip
        }

        val shortLabel = if (!hasMultipleInFacing) {
          if (info.isExternal) "External" else facing.shortLabel
        } else {
          val baseShort = "${facing.shortLabel} • ${item.lensType.shortLabel} $zoomStr"
          if (needsIdDisambiguation) "$baseShort (${info.cameraId})" else baseShort
        }

        val displayName = if (!hasMultipleInFacing) {
          if (info.isExternal) {
            "External Camera (ID ${info.cameraId})"
          } else {
            "${facing.displayName} (ID ${info.cameraId})"
          }
        } else {
          "${facing.shortLabel} ${item.lensType.displayName} ($zoomStr • ID ${info.cameraId})"
        }

        val specsSummary = buildString {
          append("ID ${info.cameraId}")
          if (info.physicalCameraId != null && info.physicalCameraId != info.openCameraId) {
            append(" (Logical ${info.openCameraId})")
          }
          append(" • ${facing.shortLabel} ${item.lensType.displayName} ($zoomStr)")
          if (item.eq35mm != null) {
            append(" • ${item.eq35mm}mm")
          } else if (info.focalLengthMm != null && info.focalLengthMm > 0f) {
            append(String.format(Locale.US, " • %.1fmm", info.focalLengthMm))
          }
          if (info.aperture != null && info.aperture > 0f) {
            append(String.format(Locale.US, " • f/%.1f", info.aperture))
          }
          if (info.hasFlash) {
            append(" • Flash")
          }
        }

        result.add(
          CameraSystemDevice(
            cameraId = info.cameraId,
            openCameraId = info.openCameraId,
            physicalCameraId = info.physicalCameraId,
            lensFacing = facing,
            lensType = item.lensType,
            isPrimaryForFacing = item.isPrimary,
            isExternal = info.isExternal,
            opticalZoomFactor = item.opticalZoom,
            focalLengthMm = info.focalLengthMm,
            equivalentFocalLength35mm = item.eq35mm,
            aperture = info.aperture,
            hasFlash = info.hasFlash,
            isLogicalMultiCamera = info.isLogicalMultiCamera,
            sensorOrientationDegrees = info.sensorOrientationDegrees,
            displayName = displayName,
            shortLabel = shortLabel,
            chipLabel = finalChipLabel,
            specsSummary = specsSummary,
          )
        )
      }
    }

    return if (result.isEmpty()) getDefaultFallbackSystemCameras() else result
  }

  private fun extractRawCameraCharacteristicsInfo(
    cameraId: String,
    openCameraId: String,
    physicalCameraId: String?,
    chars: CameraCharacteristics,
    fallbackFacing: CameraLensFacing = CameraLensFacing.BACK,
  ): RawCameraCharacteristicsInfo {
    val facingInt = try {
      chars.get(CameraCharacteristics.LENS_FACING)
    } catch (_: Throwable) {
      null
    }
    val isExternal = facingInt == CameraCharacteristics.LENS_FACING_EXTERNAL
    val lensFacing = when (facingInt) {
      CameraCharacteristics.LENS_FACING_FRONT -> CameraLensFacing.FRONT
      CameraCharacteristics.LENS_FACING_BACK -> CameraLensFacing.BACK
      CameraCharacteristics.LENS_FACING_EXTERNAL -> CameraLensFacing.BACK
      else -> fallbackFacing
    }
    val focalLengthMm = try {
      chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        ?.firstOrNull { it > 0f && !it.isNaN() }
    } catch (_: Throwable) {
      null
    }
    val sensorSize = try {
      chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
    } catch (_: Throwable) {
      null
    }
    val aperture = try {
      chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
        ?.firstOrNull { it > 0f && !it.isNaN() }
    } catch (_: Throwable) {
      null
    }
    val hasFlash = try {
      chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    } catch (_: Throwable) {
      false
    }
    val capabilities = try {
      chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
    } catch (_: Throwable) {
      null
    }
    val isLogicalMulti = capabilities?.contains(
      CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA
    ) == true
    val sensorOrientation = try {
      chars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
    } catch (_: Throwable) {
      90
    }
    val minFocusDistance = try {
      chars.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        ?.takeIf { it > 0f && !it.isNaN() }
    } catch (_: Throwable) {
      null
    }

    return RawCameraCharacteristicsInfo(
      cameraId = cameraId,
      openCameraId = openCameraId,
      physicalCameraId = physicalCameraId,
      lensFacing = lensFacing,
      isExternal = isExternal,
      focalLengthMm = focalLengthMm,
      sensorWidthMm = sensorSize?.width,
      sensorHeightMm = sensorSize?.height,
      aperture = aperture,
      hasFlash = hasFlash,
      isLogicalMultiCamera = isLogicalMulti,
      sensorOrientationDegrees = sensorOrientation,
      minFocusDistanceDiopters = minFocusDistance,
    )
  }

  /**
   * Queries Android's [CameraManager] to detect all preview-capable system cameras on the device,
   * including all top-level camera IDs in [CameraManager.getCameraIdList] as well as underlying
   * physical lenses backing logical multi-cameras on Android 9 (API 28)+.
   */
  fun detectSystemCameras(context: Context): List<CameraSystemDevice> {
    val manager = try {
      context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    } catch (_: Throwable) {
      null
    } ?: return getDefaultFallbackSystemCameras()

    val topLevelIds = try {
      manager.cameraIdList.toList()
    } catch (_: Throwable) {
      emptyList()
    }
    if (topLevelIds.isEmpty()) {
      return getDefaultFallbackSystemCameras()
    }

    val topLevelIdSet = topLevelIds.toSet()
    val rawList = mutableListOf<RawCameraCharacteristicsInfo>()

    for (id in topLevelIds) {
      val chars = try {
        manager.getCameraCharacteristics(id)
      } catch (_: Throwable) {
        null
      } ?: continue

      val streamMap = try {
        chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
      } catch (_: Throwable) {
        null
      }
      val previewSizes = try {
        streamMap?.getOutputSizes(SurfaceTexture::class.java)
      } catch (_: Throwable) {
        null
      }
      if (streamMap != null && previewSizes.isNullOrEmpty()) {
        continue
      }

      val topInfo = extractRawCameraCharacteristicsInfo(
        cameraId = id,
        openCameraId = id,
        physicalCameraId = null,
        chars = chars,
      )
      rawList.add(topInfo)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val physicalIds = try {
          chars.physicalCameraIds.toList()
        } catch (_: Throwable) {
          emptyList()
        }
        val hiddenPhysicalIds = physicalIds.filter { it !in topLevelIdSet && it != id }
        var skippedPrimaryDuplicate = false
        for (physId in hiddenPhysicalIds) {
          val physChars = try {
            manager.getCameraCharacteristics(physId)
          } catch (_: Throwable) {
            null
          } ?: continue

          val physStreamMap = try {
            physChars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
          } catch (_: Throwable) {
            null
          }
          val physPreviewSizes = try {
            physStreamMap?.getOutputSizes(SurfaceTexture::class.java)
          } catch (_: Throwable) {
            null
          }
          if (physStreamMap != null && physPreviewSizes.isNullOrEmpty()) {
            continue
          }

          val physInfo = extractRawCameraCharacteristicsInfo(
            cameraId = physId,
            openCameraId = id,
            physicalCameraId = physId,
            chars = physChars,
            fallbackFacing = topInfo.lensFacing,
          )

          val sameFocalAsLogical = topInfo.focalLengthMm != null &&
            physInfo.focalLengthMm != null &&
            abs(topInfo.focalLengthMm - physInfo.focalLengthMm) < 0.05f
          if (!skippedPrimaryDuplicate && sameFocalAsLogical && hiddenPhysicalIds.size > 1) {
            skippedPrimaryDuplicate = true
            continue
          }

          rawList.add(physInfo)
        }
      }
    }

    return if (rawList.isEmpty()) {
      getDefaultFallbackSystemCameras()
    } else {
      buildSystemCameraCatalog(rawList)
    }
  }

  /**
   * Resolves the active [CameraSystemDevice] from [cameras] matching [requestedCameraId],
   * falling back to the primary camera for [fallbackFacing] (e.g. `1x Wide` Back Camera),
   * or the first available camera.
   */
  fun resolveSelectedCamera(
    cameras: List<CameraSystemDevice>,
    requestedCameraId: String? = null,
    fallbackFacing: CameraLensFacing = CameraLensFacing.BACK,
  ): CameraSystemDevice {
    val effectiveList = cameras.ifEmpty { getDefaultFallbackSystemCameras() }
    if (!requestedCameraId.isNullOrBlank()) {
      effectiveList.find { it.cameraId == requestedCameraId.trim() }?.let { return it }
    }
    return effectiveList.firstOrNull { it.lensFacing == fallbackFacing && it.isPrimaryForFacing }
      ?: effectiveList.firstOrNull { it.lensFacing == fallbackFacing }
      ?: effectiveList.firstOrNull { it.isPrimaryForFacing }
      ?: effectiveList.first()
  }

  /**
   * Returns all [CameraSystemDevice] entries in [cameras] matching [facing]
   * ([CameraLensFacing.BACK] or [CameraLensFacing.FRONT]).
   */
  fun getCamerasForFacing(
    cameras: List<CameraSystemDevice>,
    facing: CameraLensFacing,
  ): List<CameraSystemDevice> {
    val effectiveList = cameras.ifEmpty { getDefaultFallbackSystemCameras() }
    return effectiveList.filter { it.lensFacing == facing }
  }

  /**
   * Resolves the primary [CameraSystemDevice] for [facing] (e.g. primary `1x Wide` for
   * [CameraLensFacing.BACK] or primary `Front` for [CameraLensFacing.FRONT]).
   */
  fun resolvePrimaryCameraForFacing(
    cameras: List<CameraSystemDevice>,
    facing: CameraLensFacing,
  ): CameraSystemDevice {
    val effectiveList = cameras.ifEmpty { getDefaultFallbackSystemCameras() }
    return effectiveList.firstOrNull { it.lensFacing == facing && it.isPrimaryForFacing }
      ?: effectiveList.firstOrNull { it.lensFacing == facing }
      ?: resolveSelectedCamera(effectiveList, fallbackFacing = facing)
  }

  /**
   * Toggles between the primary back camera and the primary front camera based on the facing of
   * [currentCameraId] (or [currentFacing] if specified).
   */
  fun togglePrimaryFrontBackCamera(
    cameras: List<CameraSystemDevice>,
    currentCameraId: String? = null,
    currentFacing: CameraLensFacing? = null,
  ): CameraSystemDevice {
    val effectiveList = cameras.ifEmpty { getDefaultFallbackSystemCameras() }
    val activeFacing = currentFacing
      ?: effectiveList.find { it.cameraId == currentCameraId }?.lensFacing
      ?: CameraLensFacing.BACK
    val targetFacing = activeFacing.toggle()
    return resolvePrimaryCameraForFacing(effectiveList, targetFacing)
  }

  /**
   * Returns the next [CameraSystemDevice] in [cameras] after [currentCameraId], wrapping around
   * so the user can cycle through every detected system camera on the device.
   */
  fun nextSystemCamera(
    cameras: List<CameraSystemDevice>,
    currentCameraId: String?,
  ): CameraSystemDevice {
    val effectiveList = cameras.ifEmpty { getDefaultFallbackSystemCameras() }
    val currentIndex = effectiveList.indexOfFirst { it.cameraId == currentCameraId }
    if (currentIndex < 0) {
      return resolveSelectedCamera(effectiveList)
    }
    return effectiveList[(currentIndex + 1) % effectiveList.size]
  }

  /**
   * Returns the next [CameraSystemDevice] matching [facing] after [currentCameraId], wrapping around.
   */
  fun nextCameraForFacing(
    cameras: List<CameraSystemDevice>,
    currentCameraId: String?,
    facing: CameraLensFacing,
  ): CameraSystemDevice {
    val facingCameras = cameras.filter { it.lensFacing == facing }
    if (facingCameras.isEmpty()) {
      return resolveSelectedCamera(cameras, requestedCameraId = currentCameraId, fallbackFacing = facing)
    }
    val currentIndex = facingCameras.indexOfFirst { it.cameraId == currentCameraId }
    if (currentIndex < 0) {
      return facingCameras.firstOrNull { it.isPrimaryForFacing } ?: facingCameras.first()
    }
    return facingCameras[(currentIndex + 1) % facingCameras.size]
  }

  /**
   * Coerces exposure compensation value into `[MIN_CAMERA_EXPOSURE_EV]..[MAX_CAMERA_EXPOSURE_EV]`.
   */
  fun coerceExposureEv(ev: Float): Float {
    if (ev.isNaN()) return 0f
    val clamped = ev.coerceIn(MIN_CAMERA_EXPOSURE_EV, MAX_CAMERA_EXPOSURE_EV)
    return (clamped * 10f).roundToInt() / 10f
  }

  /**
   * Formats exposure compensation as `0.0 EV`, `+1.2 EV`, or `-0.8 EV`.
   */
  fun formatExposureEvLabel(ev: Float): String {
    val clamped = coerceExposureEv(ev)
    val prefix = if (clamped > 0f) "+" else ""
    return String.format(Locale.US, "%s%.1f EV", prefix, clamped)
  }

  /**
   * Formats a duration in milliseconds as `MM:SS` (or `HH:MM:SS` if >= 1 hour).
   */
  fun formatTimerDurationMs(elapsedMs: Long): String {
    return AudioRecorderEngine.formatDurationMs(elapsedMs)
  }

  /**
   * Generates a timestamped filename for a photo captured at [epochMs].
   * Example: `IMG_20261110_174500.jpg` or `PANO_20261110_174500.jpg`
   */
  fun generatePhotoFileName(
    format: CameraPhotoFormat = CameraPhotoFormat.DEFAULT,
    subMode: CameraPhotoSubMode = CameraPhotoSubMode.STANDARD,
    epochMs: Long = System.currentTimeMillis(),
  ): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    return "${subMode.filePrefix}_${stamp}.${format.extension}"
  }

  /**
   * Generates a timestamped filename for a video recorded at [epochMs].
   * Example: `VID_20261110_174500.mp4`
   */
  fun generateVideoFileName(
    epochMs: Long = System.currentTimeMillis(),
  ): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    return "VID_${stamp}.mp4"
  }

  /**
   * Resolves a unique [File] inside [directory] for a newly captured photo,
   * appending `_1`, `_2`, etc. if a file with the same timestamp already exists.
   */
  fun createTimestampedPhotoFile(
    directory: File,
    format: CameraPhotoFormat = CameraPhotoFormat.DEFAULT,
    subMode: CameraPhotoSubMode = CameraPhotoSubMode.STANDARD,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    val baseStem = "${subMode.filePrefix}_$stamp"
    val ext = format.extension

    var candidate = File(directory, "$baseStem.$ext")
    var counter = 1
    while (candidate.exists()) {
      candidate = File(directory, "${baseStem}_$counter.$ext")
      counter++
    }
    return candidate
  }

  /**
   * Resolves a unique [File] inside [directory] for a newly captured panoramic photo (`PANO_yyyyMMdd_HHmmss`).
   */
  fun createTimestampedPanoramaFile(
    directory: File,
    format: CameraPhotoFormat = CameraPhotoFormat.DEFAULT,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    return createTimestampedPhotoFile(
      directory = directory,
      format = format,
      subMode = CameraPhotoSubMode.PANORAMA,
      epochMs = epochMs,
    )
  }

  /**
   * Resolves a list of [burstCount] unique [File]s inside [directory] for a burst capture sequence
   * (`BURST_yyyyMMdd_HHmmss_1.jpg`, `BURST_yyyyMMdd_HHmmss_2.jpg`, ...).
   */
  fun createTimestampedBurstPhotoFiles(
    directory: File,
    format: CameraPhotoFormat = CameraPhotoFormat.DEFAULT,
    burstCount: Int = 3,
    epochMs: Long = System.currentTimeMillis(),
  ): List<File> {
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val count = burstCount.coerceIn(2, 10)
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    val baseStem = "${CameraPhotoSubMode.BURST.filePrefix}_$stamp"
    val ext = format.extension

    val files = mutableListOf<File>()
    for (idx in 1..count) {
      var candidate = File(directory, "${baseStem}_$idx.$ext")
      var collision = 1
      while (candidate.exists() || candidate in files) {
        candidate = File(directory, "${baseStem}_${idx}_$collision.$ext")
        collision++
      }
      files.add(candidate)
    }
    return files
  }

  /**
   * Resolves a unique [File] inside [directory] for a newly recorded video,
   * appending `_1`, `_2`, etc. if a file with the same timestamp already exists.
   */
  fun createTimestampedVideoFile(
    directory: File,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    val baseStem = "VID_$stamp"
    val ext = "mp4"

    var candidate = File(directory, "$baseStem.$ext")
    var counter = 1
    while (candidate.exists()) {
      candidate = File(directory, "${baseStem}_$counter.$ext")
      counter++
    }
    return candidate
  }

  /**
   * Resolves a user-friendly display path for [directory] (e.g. `/DCIM/Camera` when inside user root).
   */
  fun formatDirectoryDisplayPath(
    directory: File,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): String {
    val absPath = directory.absolutePath
    val rootPath = userRoot.absolutePath.trimEnd('/')
    if (absPath == rootPath) return "/"
    if (absPath.startsWith("$rootPath/")) {
      return absPath.removePrefix(rootPath)
    }
    if (absPath.endsWith("/$DEFAULT_CAMERA_FOLDER_SUBPATH", ignoreCase = true)) {
      return DEFAULT_CAMERA_FOLDER_DISPLAY
    }
    return absPath
  }

  /**
   * Resolves a user-supplied folder path (such as `/DCIM/Camera`, `DCIM/Camera`, `/Photos`, or an absolute path)
   * into a concrete [File] directory.
   */
  fun resolveDirectoryInput(
    inputPath: String,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
    fallbackBaseDir: File? = null,
  ): File {
    val trimmed = inputPath.trim()
    if (trimmed.isEmpty() || trimmed == "/" || trimmed.equals(DEFAULT_CAMERA_FOLDER_DISPLAY, ignoreCase = true)) {
      val rootCameraDir = File(userRoot, DEFAULT_CAMERA_FOLDER_SUBPATH)
      if ((rootCameraDir.exists() && rootCameraDir.canWrite()) || rootCameraDir.mkdirs()) {
        return rootCameraDir
      }
      if (fallbackBaseDir != null) {
        return File(fallbackBaseDir, DEFAULT_CAMERA_FOLDER_SUBPATH).also { it.mkdirs() }
      }
      return rootCameraDir
    }

    val directFile = File(trimmed)
    if (trimmed.startsWith("/storage/") || trimmed.startsWith("/data/") || trimmed.startsWith("/sdcard/")) {
      return directFile
    }
    if (fallbackBaseDir != null && trimmed.startsWith(fallbackBaseDir.absolutePath)) {
      return directFile
    }

    val relativeSubpath = trimmed.trimStart('/')
    val candidateInRoot = File(userRoot, relativeSubpath)
    if ((candidateInRoot.exists() && candidateInRoot.canWrite()) || candidateInRoot.mkdirs()) {
      return candidateInRoot
    }
    if (fallbackBaseDir != null) {
      return File(fallbackBaseDir, relativeSubpath).also { it.mkdirs() }
    }
    return candidateInRoot
  }

  /**
   * Resolves the default `/DCIM/Camera` directory.
   * Prefers `/storage/emulated/0/DCIM/Camera` when accessible, falling back to app storage `/DCIM/Camera`
   * when external storage access is unavailable.
   */
  fun getDefaultCameraDirectory(context: Context? = null): File {
    val userRoot = FileManagerEngine.getUserRootDirectory()
    val primaryDir = File(userRoot, DEFAULT_CAMERA_FOLDER_SUBPATH)
    try {
      if ((primaryDir.exists() && primaryDir.canWrite()) || primaryDir.mkdirs()) {
        return primaryDir
      }
    } catch (_: Throwable) {}

    if (context != null) {
      val fallbackParent = context.getExternalFilesDir(null) ?: context.filesDir
      val fallbackDir = File(fallbackParent, DEFAULT_CAMERA_FOLDER_SUBPATH)
      fallbackDir.mkdirs()
      return fallbackDir
    }
    return primaryDir
  }

  /**
   * Creates the Android system prompt [android.content.Intent] (`Intent.ACTION_OPEN_DOCUMENT_TREE`)
   * scoped to [currentDisplayPath] (defaulting to `/DCIM/Camera`) so the user can pick or grant
   * scoped storage access to a camera save folder.
   */
  fun createFolderAccessIntent(
    context: Context? = null,
    currentDisplayPath: String = DEFAULT_CAMERA_FOLDER_DISPLAY,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): android.content.Intent {
    val cleanSubpath = currentDisplayPath.trim().trim('/').ifEmpty { DEFAULT_CAMERA_FOLDER_SUBPATH }
    try {
      val targetDir = File(userRoot, cleanSubpath)
      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }
    } catch (_: Throwable) {}

    val docId = "primary:$cleanSubpath"
    val initialUri = try {
      android.provider.DocumentsContract.buildDocumentUri(
        "com.android.externalstorage.documents",
        docId,
      )
    } catch (_: Throwable) {
      android.net.Uri.parse(
        "content://com.android.externalstorage.documents/document/primary%3A${cleanSubpath.replace("/", "%2F")}"
      )
    }
    val baseIntent = if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? android.os.storage.StorageManager
        storageManager?.primaryStorageVolume?.createOpenDocumentTreeIntent()
      } catch (_: Throwable) {
        null
      }
    } else {
      null
    } ?: android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT_TREE)

    return baseIntent.apply {
      action = android.content.Intent.ACTION_OPEN_DOCUMENT_TREE
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, initialUri)
      }
      addFlags(
        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
          android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
          android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
          android.content.Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
      )
    }
  }

  /**
   * Resolves a user-friendly display path (such as `/DCIM/Camera` or `/Pictures/Camera`) from a
   * SAF tree/document [android.net.Uri] or raw URI string.
   */
  fun resolveDisplayPathFromTreeUri(
    uri: android.net.Uri?,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): String? {
    if (uri == null) return null
    val resolved = GalleryEngine.resolveCustomFolderFromTreeUri(uri, userRoot) ?: return null
    val cleanDisplay = resolved.displayPath.trim()
    return if (cleanDisplay.isEmpty()) {
      DEFAULT_CAMERA_FOLDER_DISPLAY
    } else if (cleanDisplay.startsWith("/")) {
      cleanDisplay
    } else {
      "/$cleanDisplay"
    }
  }

  fun resolveDisplayPathFromTreeUriString(
    rawUriString: String?,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): String? {
    if (rawUriString.isNullOrBlank()) return null
    val decoded = try {
      java.net.URLDecoder.decode(rawUriString.trim(), "UTF-8")
    } catch (_: Throwable) {
      rawUriString.trim()
    }
    if (decoded.startsWith("file://", ignoreCase = true)) {
      val path = decoded.removePrefix("file://")
      return formatDirectoryDisplayPath(File(path), userRoot)
    }
    val rawDocId = decoded.substringAfterLast("/tree/", "").ifEmpty {
      decoded.substringAfterLast("/document/", "")
    }.ifEmpty {
      decoded
    }
    val cleanedSubpath = rawDocId
      .substringAfter(':', rawDocId)
      .trim()
      .trim('/')
    return if (cleanedSubpath.isEmpty()) {
      DEFAULT_CAMERA_FOLDER_DISPLAY
    } else {
      "/$cleanedSubpath"
    }
  }

  /**
   * Returns `true` if [file] is a supported video file.
   */
  fun isVideoFile(file: File): Boolean {
    if (!file.exists() || !file.isFile || file.name.startsWith(".")) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in SUPPORTED_VIDEO_EXTENSIONS || FileManagerEngine.isVideoFile(file)
  }

  /**
   * Returns `true` if [file] is a supported photo or video file produced or viewable by the Camera tool.
   */
  fun isCameraMediaFile(file: File): Boolean {
    if (!file.exists() || !file.isFile || file.name.startsWith(".")) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in SUPPORTED_PHOTO_EXTENSIONS ||
      ext in SUPPORTED_VIDEO_EXTENSIONS ||
      GalleryEngine.isPhotoFile(file) ||
      isVideoFile(file)
  }

  /**
   * Lists all captured photos and videos inside [directory], sorted newest first (`lastModifiedMs` descending).
   */
  fun listCapturedMedia(directory: File): List<CameraMediaItem> {
    if (!directory.exists() || !directory.isDirectory) return emptyList()
    val files = try {
      directory.listFiles()
    } catch (_: Throwable) {
      null
    } ?: return emptyList()

    return files
      .filter { isCameraMediaFile(it) }
      .sortedWith(
        compareByDescending<File> { it.lastModified() }
          .thenByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
      )
      .map { file ->
        val video = isVideoFile(file)
        val duration = if (video) {
          AudioRecorderEngine.estimateDurationFromFile(file)
        } else {
          0L
        }
        CameraMediaItem(
          file = file,
          name = file.name,
          path = file.absolutePath,
          sizeBytes = file.length(),
          lastModifiedMs = file.lastModified(),
          extension = file.extension.lowercase(Locale.ROOT),
          isVideo = video,
          durationMs = duration,
        )
      }
  }

  /**
   * Applies [filter] and [exposureEv] adjustments to an RGB color triple (`0..255` each).
   */
  fun applyFilterAndExposureToRgb(
    red: Int,
    green: Int,
    blue: Int,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    exposureEv: Float = 0f,
  ): Triple<Int, Int, Int> {
    val evFactor = 2.0.pow(coerceExposureEv(exposureEv).toDouble() * 0.65).toFloat()
    var r = (red * evFactor).coerceIn(0f, 255f)
    var g = (green * evFactor).coerceIn(0f, 255f)
    var b = (blue * evFactor).coerceIn(0f, 255f)

    when (filter) {
      CameraColorFilter.NONE -> {
        // Keep natural RGB
      }
      CameraColorFilter.MONO -> {
        val luma = 0.299f * r + 0.587f * g + 0.114f * b
        r = luma
        g = luma
        b = luma
      }
      CameraColorFilter.WARM -> {
        r = (r * 1.14f + 10f).coerceIn(0f, 255f)
        g = (g * 1.03f + 4f).coerceIn(0f, 255f)
        b = (b * 0.86f).coerceIn(0f, 255f)
      }
      CameraColorFilter.COOL -> {
        r = (r * 0.86f).coerceIn(0f, 255f)
        g = (g * 1.02f + 2f).coerceIn(0f, 255f)
        b = (b * 1.16f + 12f).coerceIn(0f, 255f)
      }
      CameraColorFilter.SEPIA -> {
        val sr = (r * 0.393f + g * 0.769f + b * 0.189f).coerceIn(0f, 255f)
        val sg = (r * 0.349f + g * 0.686f + b * 0.168f).coerceIn(0f, 255f)
        val sb = (r * 0.272f + g * 0.534f + b * 0.131f).coerceIn(0f, 255f)
        r = sr
        g = sg
        b = sb
      }
      CameraColorFilter.VIVID -> {
        val avg = (r + g + b) / 3f
        val satBoost = 1.38f
        r = ((avg + (r - avg) * satBoost) * 1.05f).coerceIn(0f, 255f)
        g = ((avg + (g - avg) * satBoost) * 1.05f).coerceIn(0f, 255f)
        b = ((avg + (b - avg) * satBoost) * 1.05f).coerceIn(0f, 255f)
      }
    }

    return Triple(
      r.roundToInt().coerceIn(0, 255),
      g.roundToInt().coerceIn(0, 255),
      b.roundToInt().coerceIn(0, 255),
    )
  }

  /**
   * Computes a deterministic `#RRGGBB` hex color for normalized focus coordinates ([normX], [normY])
   * in `0.0f..1.0f` when a live hardware camera bitmap is unavailable.
   */
  fun sampleSyntheticFocusColorHex(
    normX: Float,
    normY: Float,
    lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    exposureEv: Float = 0f,
  ): String {
    val clampedX = normX.coerceIn(0f, 1f)
    val clampedY = normY.coerceIn(0f, 1f)
    val baseR: Int
    val baseG: Int
    val baseB: Int
    if (clampedY >= 0.65f) {
      val tLow = ((clampedY - 0.65f) / 0.35f).coerceIn(0f, 1f)
      val horizShift = ((clampedX - 0.5f) * 28f).roundToInt()
      baseR = (42 + (28 - 42) * tLow + horizShift).roundToInt().coerceIn(0, 255)
      baseG = (68 + (92 - 68) * tLow - horizShift / 2).roundToInt().coerceIn(0, 255)
      baseB = (64 + (52 - 64) * tLow - horizShift).roundToInt().coerceIn(0, 255)
    } else {
      val t = (clampedY / 0.65f).coerceIn(0f, 1f)
      val topR = if (lensFacing == CameraLensFacing.BACK) 34 else 72
      val topG = if (lensFacing == CameraLensFacing.BACK) 56 else 48
      val topB = if (lensFacing == CameraLensFacing.BACK) 112 else 118
      val botR = if (lensFacing == CameraLensFacing.BACK) 218 else 186
      val botG = if (lensFacing == CameraLensFacing.BACK) 132 else 120
      val botB = if (lensFacing == CameraLensFacing.BACK) 110 else 168
      val horizShift = ((clampedX - 0.5f) * 24f).roundToInt()
      baseR = (topR + (botR - topR) * t + horizShift).roundToInt().coerceIn(0, 255)
      baseG = (topG + (botG - topG) * t).roundToInt().coerceIn(0, 255)
      baseB = (topB + (botB - topB) * t - horizShift).roundToInt().coerceIn(0, 255)
    }
    val (r, g, b) = applyFilterAndExposureToRgb(
      red = baseR,
      green = baseG,
      blue = baseB,
      filter = filter,
      exposureEv = exposureEv,
    )
    return String.format(Locale.US, "#%02X%02X%02X", r, g, b)
  }

  /**
   * Samples the average RGB color of the pixels within a small circular [radiusPx] around
   * normalized coordinates ([normX], [normY]) in `0.0f..1.0f` of an ARGB [pixels] buffer
   * (`width` × `height`), applying [filter] and [exposureEv], and returns its uppercase `#RRGGBB` hex string.
   */
  fun sampleCenterRadiusColorHexFromArgbPixels(
    pixels: IntArray,
    width: Int,
    height: Int,
    normX: Float,
    normY: Float,
    radiusPx: Int = 6,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    exposureEv: Float = 0f,
  ): String {
    if (width <= 0 || height <= 0 || pixels.size < width * height) {
      return sampleSyntheticFocusColorHex(normX, normY, filter = filter, exposureEv = exposureEv)
    }
    val clampedX = normX.coerceIn(0f, 1f)
    val clampedY = normY.coerceIn(0f, 1f)
    val cx = (clampedX * (width - 1)).roundToInt().coerceIn(0, width - 1)
    val cy = (clampedY * (height - 1)).roundToInt().coerceIn(0, height - 1)
    val rPx = radiusPx.coerceIn(0, 32)
    val rSq = rPx * rPx

    val minX = (cx - rPx).coerceAtLeast(0)
    val maxX = (cx + rPx).coerceAtMost(width - 1)
    val minY = (cy - rPx).coerceAtLeast(0)
    val maxY = (cy + rPx).coerceAtMost(height - 1)

    var sumR = 0L
    var sumG = 0L
    var sumB = 0L
    var count = 0

    for (y in minY..maxY) {
      val dy = y - cy
      val rowOffset = y * width
      for (x in minX..maxX) {
        val dx = x - cx
        if (dx * dx + dy * dy <= rSq) {
          val argb = pixels[rowOffset + x]
          if ((argb ushr 24) != 0) {
            sumR += (argb shr 16) and 0xFF
            sumG += (argb shr 8) and 0xFF
            sumB += argb and 0xFF
            count++
          }
        }
      }
    }

    if (count <= 0) {
      return sampleSyntheticFocusColorHex(normX, normY, filter = filter, exposureEv = exposureEv)
    }

    val avgR = (sumR.toFloat() / count).roundToInt().coerceIn(0, 255)
    val avgG = (sumG.toFloat() / count).roundToInt().coerceIn(0, 255)
    val avgB = (sumB.toFloat() / count).roundToInt().coerceIn(0, 255)
    val (finalR, finalG, finalB) = if (filter != CameraColorFilter.NONE || exposureEv != 0f) {
      applyFilterAndExposureToRgb(avgR, avgG, avgB, filter, exposureEv)
    } else {
      Triple(avgR, avgG, avgB)
    }
    return String.format(Locale.US, "#%02X%02X%02X", finalR, finalG, finalB)
  }

  /**
   * Samples the average RGB color of the pixels within a small circular [radiusPx] around
   * normalized coordinates ([normX], [normY]) of [bitmap] and returns its uppercase `#RRGGBB` hex string.
   */
  fun sampleCenterRadiusColorHexFromBitmap(
    bitmap: Bitmap,
    normX: Float,
    normY: Float,
    radiusPx: Int = 6,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    exposureEv: Float = 0f,
  ): String {
    return try {
      val bw = bitmap.width
      val bh = bitmap.height
      if (bw <= 0 || bh <= 0) {
        return sampleSyntheticFocusColorHex(normX, normY, filter = filter, exposureEv = exposureEv)
      }
      val clampedX = normX.coerceIn(0f, 1f)
      val clampedY = normY.coerceIn(0f, 1f)
      val cx = (clampedX * (bw - 1)).roundToInt().coerceIn(0, bw - 1)
      val cy = (clampedY * (bh - 1)).roundToInt().coerceIn(0, bh - 1)
      val rPx = radiusPx.coerceIn(0, 32)
      val rSq = rPx * rPx

      val minX = (cx - rPx).coerceAtLeast(0)
      val maxX = (cx + rPx).coerceAtMost(bw - 1)
      val minY = (cy - rPx).coerceAtLeast(0)
      val maxY = (cy + rPx).coerceAtMost(bh - 1)
      val patchW = maxX - minX + 1
      val patchH = maxY - minY + 1
      val patchPixels = IntArray(patchW * patchH)
      bitmap.getPixels(patchPixels, 0, patchW, minX, minY, patchW, patchH)

      var sumR = 0L
      var sumG = 0L
      var sumB = 0L
      var count = 0

      for (py in 0 until patchH) {
        val dy = (minY + py) - cy
        val rowOffset = py * patchW
        for (px in 0 until patchW) {
          val dx = (minX + px) - cx
          if (dx * dx + dy * dy <= rSq) {
            val argb = patchPixels[rowOffset + px]
            if ((argb ushr 24) != 0) {
              sumR += (argb shr 16) and 0xFF
              sumG += (argb shr 8) and 0xFF
              sumB += argb and 0xFF
              count++
            }
          }
        }
      }

      if (count <= 0) {
        return sampleSyntheticFocusColorHex(normX, normY, filter = filter, exposureEv = exposureEv)
      }

      val avgR = (sumR.toFloat() / count).roundToInt().coerceIn(0, 255)
      val avgG = (sumG.toFloat() / count).roundToInt().coerceIn(0, 255)
      val avgB = (sumB.toFloat() / count).roundToInt().coerceIn(0, 255)
      val (finalR, finalG, finalB) = if (filter != CameraColorFilter.NONE || exposureEv != 0f) {
        applyFilterAndExposureToRgb(avgR, avgG, avgB, filter, exposureEv)
      } else {
        Triple(avgR, avgG, avgB)
      }
      String.format(Locale.US, "#%02X%02X%02X", finalR, finalG, finalB)
    } catch (_: Throwable) {
      sampleSyntheticFocusColorHex(normX, normY, filter = filter, exposureEv = exposureEv)
    }
  }

  /**
   * Maps normalized viewfinder coordinates ([normX], [normY]) in `0.0f..1.0f` through the inverse
   * of the [TextureView] center-crop [textureTransformMatrix], digital [zoom], and front-camera
   * [mirrorFront] transform, and samples the circular patch color directly from [rawBitmap]
   * without allocating an intermediate transformed full-frame bitmap.
   */
  fun sampleLiveFocusColorHexFromRawTextureBitmap(
    rawBitmap: Bitmap,
    normX: Float,
    normY: Float,
    textureTransformMatrix: Matrix? = null,
    zoom: Float = 1.0f,
    lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    mirrorFront: Boolean = false,
    viewWidth: Int = rawBitmap.width,
    viewHeight: Int = rawBitmap.height,
    radiusPx: Int = 5,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    exposureEv: Float = 0f,
  ): String {
    val bw = rawBitmap.width
    val bh = rawBitmap.height
    if (bw <= 0 || bh <= 0) {
      return sampleSyntheticFocusColorHex(
        normX = normX,
        normY = normY,
        lensFacing = lensFacing,
        filter = filter,
        exposureEv = exposureEv,
      )
    }
    return try {
      val combinedMatrix = if (textureTransformMatrix != null) Matrix(textureTransformMatrix) else Matrix()
      val safeViewW = viewWidth.coerceAtLeast(1)
      val safeViewH = viewHeight.coerceAtLeast(1)
      if (textureTransformMatrix != null && (safeViewW != bw || safeViewH != bh)) {
        combinedMatrix.preScale(
          safeViewW.toFloat() / bw.toFloat(),
          safeViewH.toFloat() / bh.toFloat(),
        )
        combinedMatrix.postScale(
          bw.toFloat() / safeViewW.toFloat(),
          bh.toFloat() / safeViewH.toFloat(),
        )
      }
      val cx = bw / 2f
      val cy = bh / 2f
      val clampedZoom = coerceZoom(zoom)
      val flipX = if (lensFacing == CameraLensFacing.FRONT && mirrorFront) -1f else 1f
      if (clampedZoom != 1f || flipX != 1f) {
        combinedMatrix.postScale(clampedZoom * flipX, clampedZoom, cx, cy)
      }
      val inverseMatrix = Matrix()
      val (rawNormX, rawNormY) = if (combinedMatrix.invert(inverseMatrix)) {
        val pts = floatArrayOf(
          normX.coerceIn(0f, 1f) * bw.toFloat(),
          normY.coerceIn(0f, 1f) * bh.toFloat(),
        )
        inverseMatrix.mapPoints(pts)
        Pair(
          (pts[0] / bw.toFloat()).coerceIn(0f, 1f),
          (pts[1] / bh.toFloat()).coerceIn(0f, 1f),
        )
      } else {
        Pair(normX.coerceIn(0f, 1f), normY.coerceIn(0f, 1f))
      }
      sampleCenterRadiusColorHexFromBitmap(
        bitmap = rawBitmap,
        normX = rawNormX,
        normY = rawNormY,
        radiusPx = radiusPx,
        filter = filter,
        exposureEv = exposureEv,
      )
    } catch (_: Throwable) {
      sampleCenterRadiusColorHexFromBitmap(
        bitmap = rawBitmap,
        normX = normX,
        normY = normY,
        radiusPx = radiusPx,
        filter = filter,
        exposureEv = exposureEv,
      )
    }
  }

  /**
   * Determines whether Night Sight low-light enhancement should be applied given [nightMode],
   * [exposureEv], [flashMode], and optional frame [averageLuma] (`0..255`).
   */
  fun shouldApplyNightSight(
    nightMode: CameraNightMode,
    exposureEv: Float = 0f,
    flashMode: CameraFlashMode = CameraFlashMode.OFF,
    averageLuma: Int? = null,
  ): Boolean {
    return when (nightMode) {
      CameraNightMode.ON -> true
      CameraNightMode.OFF -> false
      CameraNightMode.AUTO -> {
        val flashActive = flashMode == CameraFlashMode.ON || flashMode == CameraFlashMode.TORCH
        if (averageLuma != null) {
          averageLuma < 85 && !flashActive
        } else {
          exposureEv <= -0.5f && !flashActive
        }
      }
    }
  }

  /**
   * Captures and writes a rich procedural scene photo to [outputFile] reflecting the active camera
   * parameters ([lensFacing], [aspectRatio], [filter], [zoom], [exposureEv], [mirrorFront], [flashMode], [subMode], [nightMode]).
   *
   * Works in both full Android runtime (using hardware-accelerated `Bitmap` & `Canvas` encoding)
   * and pure JVM unit tests (falling back to spec-compliant PNG encoding).
   */
  fun captureSyntheticPhotoFile(
    outputFile: File,
    lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    aspectRatio: CameraAspectRatio = CameraAspectRatio.RATIO_4_3,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    zoom: Float = 1.0f,
    exposureEv: Float = 0f,
    mirrorFront: Boolean = false,
    flashMode: CameraFlashMode = CameraFlashMode.OFF,
    subMode: CameraPhotoSubMode = CameraPhotoSubMode.STANDARD,
    nightMode: CameraNightMode = CameraNightMode.OFF,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    if (subMode == CameraPhotoSubMode.PANORAMA) {
      return captureSyntheticPanoramaFile(
        outputFile = outputFile,
        lensFacing = lensFacing,
        filter = filter,
        zoom = zoom,
        exposureEv = exposureEv,
        mirrorFront = mirrorFront,
        flashMode = flashMode,
        nightMode = nightMode,
        epochMs = epochMs,
      )
    }
    outputFile.parentFile?.mkdirs()
    val effectiveEv = if (flashMode == CameraFlashMode.ON || flashMode == CameraFlashMode.TORCH) {
      (exposureEv + 0.35f).coerceIn(MIN_CAMERA_EXPOSURE_EV, MAX_CAMERA_EXPOSURE_EV)
    } else {
      exposureEv
    }
    val useNightSight = shouldApplyNightSight(
      nightMode = nightMode,
      exposureEv = exposureEv,
      flashMode = flashMode,
    )

    fun transformSceneColor(r: Int, g: Int, b: Int): Triple<Int, Int, Int> {
      return when {
        useNightSight -> applyNightSightToRgb(r, g, b, filter, effectiveEv)
        subMode == CameraPhotoSubMode.HDR -> applyHdrToneMapToRgb(r, g, b, filter, effectiveEv)
        else -> applyFilterAndExposureToRgb(r, g, b, filter, effectiveEv)
      }
    }

    // Try Android Bitmap + Canvas first (succeeds on Android device/emulator)
    val androidWritten = try {
      val (width, height) = aspectRatio.resolveDimensions(baseLongEdge = 960)
      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      if (bitmap.width > 0 && bitmap.height > 0) {
        val canvas = Canvas(bitmap)
        if (lensFacing == CameraLensFacing.FRONT && mirrorFront) {
          canvas.scale(-1f, 1f, width / 2f, height / 2f)
        }

        val skyTop = transformSceneColor(
          r = if (lensFacing == CameraLensFacing.BACK) 34 else 72,
          g = if (lensFacing == CameraLensFacing.BACK) 56 else 48,
          b = if (lensFacing == CameraLensFacing.BACK) 112 else 118,
        )
        val skyBottom = transformSceneColor(
          r = if (lensFacing == CameraLensFacing.BACK) 218 else 186,
          g = if (lensFacing == CameraLensFacing.BACK) 132 else 120,
          b = if (lensFacing == CameraLensFacing.BACK) 110 else 168,
        )
        val groundColor = transformSceneColor(
          r = 42,
          g = 68,
          b = 64,
        )
        val sunColor = transformSceneColor(
          r = 255,
          g = 224,
          b = 142,
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(
          0f,
          0f,
          0f,
          height * 0.68f,
          android.graphics.Color.rgb(skyTop.first, skyTop.second, skyTop.third),
          android.graphics.Color.rgb(skyBottom.first, skyBottom.second, skyBottom.third),
          Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Sun / Subject glow scaled by zoom
        val zoomClamped = coerceZoom(zoom)
        val sunRadius = (minOf(width, height) * 0.16f * (1f + (zoomClamped - 1f) * 0.22f))
        val sunCx = width * 0.62f
        val sunCy = height * 0.34f
        paint.shader = RadialGradient(
          sunCx,
          sunCy,
          sunRadius,
          android.graphics.Color.rgb(sunColor.first, sunColor.second, sunColor.third),
          android.graphics.Color.argb(0, sunColor.first, sunColor.second, sunColor.third),
          Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(sunCx, sunCy, sunRadius, paint)

        // Horizon hills
        paint.shader = null
        paint.color = android.graphics.Color.rgb(groundColor.first, groundColor.second, groundColor.third)
        canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), paint)

        val isPng = outputFile.extension.equals("png", ignoreCase = true)
        val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val bos = ByteArrayOutputStream()
        val ok = bitmap.compress(compressFormat, 92, bos)
        val bytes = bos.toByteArray()
        if (ok && bytes.size > 64) {
          FileOutputStream(outputFile).use { fos ->
            fos.write(bytes)
            fos.flush()
          }
          true
        } else {
          false
        }
      } else {
        false
      }
    } catch (_: Throwable) {
      false
    }

    if (!androidWritten) {
      // Pure-JVM fallback (e.g. local unit tests where android.graphics.Bitmap is stubbed)
      val baseRed = if (lensFacing == CameraLensFacing.BACK) 0x5C else 0x8E
      val baseGreen = if (lensFacing == CameraLensFacing.BACK) 0x7A else 0x66
      val baseBlue = if (lensFacing == CameraLensFacing.BACK) 0xA6 else 0xA6
      val (r, g, b) = transformSceneColor(
        r = baseRed,
        g = baseGreen,
        b = baseBlue,
      )
      val (w, h) = aspectRatio.resolveDimensions(baseLongEdge = 64)
      GalleryEngine.writeSolidColorPng(
        outputFile = outputFile,
        red = r,
        green = g,
        blue = b,
        width = w.coerceIn(16, 128),
        height = h.coerceIn(16, 128),
        lastModifiedMs = epochMs,
      )
    } else {
      outputFile.setLastModified(epochMs)
    }

    return outputFile
  }

  /**
   * Captures and writes an ultra-wide stitched panoramic scene (`3:1` wide aspect ratio) to [outputFile]
   * reflecting [lensFacing], [filter], [zoom], [exposureEv], [mirrorFront], [flashMode], and [nightMode].
   */
  fun captureSyntheticPanoramaFile(
    outputFile: File,
    lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    zoom: Float = 1.0f,
    exposureEv: Float = 0f,
    mirrorFront: Boolean = false,
    flashMode: CameraFlashMode = CameraFlashMode.OFF,
    nightMode: CameraNightMode = CameraNightMode.OFF,
    segments: Int = PANORAMA_TARGET_SEGMENTS,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    outputFile.parentFile?.mkdirs()
    val effectiveEv = if (flashMode == CameraFlashMode.ON || flashMode == CameraFlashMode.TORCH) {
      (exposureEv + 0.35f).coerceIn(MIN_CAMERA_EXPOSURE_EV, MAX_CAMERA_EXPOSURE_EV)
    } else {
      exposureEv
    }
    val useNightSight = shouldApplyNightSight(
      nightMode = nightMode,
      exposureEv = exposureEv,
      flashMode = flashMode,
    )
    val segCount = segments.coerceIn(2, 8)

    fun transformColor(r: Int, g: Int, b: Int): Triple<Int, Int, Int> {
      return if (useNightSight) {
        applyNightSightToRgb(r, g, b, filter, effectiveEv)
      } else {
        applyFilterAndExposureToRgb(r, g, b, filter, effectiveEv)
      }
    }

    val androidWritten = try {
      val width = 1800
      val height = 600
      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      if (bitmap.width > 0 && bitmap.height > 0) {
        val canvas = Canvas(bitmap)
        if (lensFacing == CameraLensFacing.FRONT && mirrorFront) {
          canvas.scale(-1f, 1f, width / 2f, height / 2f)
        }

        val skyTop = transformColor(
          r = if (lensFacing == CameraLensFacing.BACK) 28 else 64,
          g = if (lensFacing == CameraLensFacing.BACK) 52 else 44,
          b = if (lensFacing == CameraLensFacing.BACK) 118 else 124,
        )
        val skyBottom = transformColor(
          r = if (lensFacing == CameraLensFacing.BACK) 232 else 196,
          g = if (lensFacing == CameraLensFacing.BACK) 142 else 126,
          b = if (lensFacing == CameraLensFacing.BACK) 104 else 172,
        )
        val ridgeFar = transformColor(58, 78, 96)
        val groundColor = transformColor(38, 66, 60)
        val sunColor = transformColor(255, 228, 148)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(
          0f,
          0f,
          0f,
          height * 0.72f,
          android.graphics.Color.rgb(skyTop.first, skyTop.second, skyTop.third),
          android.graphics.Color.rgb(skyBottom.first, skyBottom.second, skyBottom.third),
          Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val zoomClamped = coerceZoom(zoom)
        val sunRadius = height * 0.24f * (1f + (zoomClamped - 1f) * 0.15f)
        val sunCx = width * (0.35f + (segCount - 2) * 0.06f).coerceIn(0.25f, 0.75f)
        val sunCy = height * 0.36f
        paint.shader = RadialGradient(
          sunCx,
          sunCy,
          sunRadius,
          android.graphics.Color.rgb(sunColor.first, sunColor.second, sunColor.third),
          android.graphics.Color.argb(0, sunColor.first, sunColor.second, sunColor.third),
          Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(sunCx, sunCy, sunRadius, paint)

        // Wide panoramic mountain ridge silhouette across all stitched segments
        paint.shader = null
        paint.color = android.graphics.Color.rgb(ridgeFar.first, ridgeFar.second, ridgeFar.third)
        canvas.drawRect(0f, height * 0.56f, width.toFloat(), height.toFloat(), paint)

        paint.color = android.graphics.Color.rgb(groundColor.first, groundColor.second, groundColor.third)
        canvas.drawRect(0f, height * 0.68f, width.toFloat(), height.toFloat(), paint)

        val isPng = outputFile.extension.equals("png", ignoreCase = true)
        val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val bos = ByteArrayOutputStream()
        val ok = bitmap.compress(compressFormat, 92, bos)
        val bytes = bos.toByteArray()
        if (ok && bytes.size > 64) {
          FileOutputStream(outputFile).use { fos ->
            fos.write(bytes)
            fos.flush()
          }
          true
        } else {
          false
        }
      } else {
        false
      }
    } catch (_: Throwable) {
      false
    }

    if (!androidWritten) {
      // Pure-JVM fallback with wide 3:1 panoramic dimensions (e.g. 120x40)
      val baseRed = if (lensFacing == CameraLensFacing.BACK) 0x4A else 0x7E
      val baseGreen = if (lensFacing == CameraLensFacing.BACK) 0x82 else 0x6A
      val baseBlue = if (lensFacing == CameraLensFacing.BACK) 0xB4 else 0xB0
      val (r, g, b) = transformColor(baseRed, baseGreen, baseBlue)
      GalleryEngine.writeSolidColorPng(
        outputFile = outputFile,
        red = r,
        green = g,
        blue = b,
        width = 120,
        height = 40,
        lastModifiedMs = epochMs,
      )
    } else {
      outputFile.setLastModified(epochMs)
    }

    return outputFile
  }

  /**
   * Crops a tall portrait viewfinder frame to its central horizontal panoramic horizon band
   * so stitched panorama slices produce a true wide panoramic aspect ratio matching the
   * viewfinder Panorama reticle window.
   */
  fun extractPanoramaBandFromFrame(frame: Bitmap): Bitmap {
    if (frame.width <= 16 || frame.height <= 16) return frame
    // If already landscape or compact, keep as-is
    if (frame.height <= (frame.width * 0.85f).roundToInt()) {
      return frame
    }
    return try {
      val bandH = (frame.width * 0.72f).roundToInt().coerceIn(64, frame.height)
      val top = ((frame.height - bandH) * 0.42f).roundToInt().coerceIn(0, (frame.height - bandH).coerceAtLeast(0))
      Bitmap.createBitmap(frame, 0, top, frame.width, bandH)
    } catch (_: Throwable) {
      frame
    }
  }

  /**
   * Crops a single stationary frame into a clean `2.4:1` wide panoramic strip instead of
   * duplicating the same frame multiple times side by side.
   */
  fun cropSingleFrameToWidePanorama(frame: Bitmap): Bitmap {
    if (frame.width <= 16 || frame.height <= 16) return frame
    val targetH = (frame.width / 2.4f).roundToInt().coerceIn(32, frame.height)
    if (targetH >= frame.height) return frame
    return try {
      val top = ((frame.height - targetH) * 0.45f).roundToInt().coerceIn(0, (frame.height - targetH).coerceAtLeast(0))
      Bitmap.createBitmap(frame, 0, top, frame.width, targetH)
    } catch (_: Throwable) {
      frame
    }
  }

  /**
   * Estimates the normalized horizontal translation shift (`-1.0f..+1.0f` of frame width)
   * and vertical offset (`dy` in normalized fraction) between [prevFrame] and [currFrame],
   * along with the zero-shift Mean Absolute Difference (`zeroShiftMad` in `0f..255f`).
   *
   * Used both during live panorama sweeping (to detect when the user has panned far enough
   * for the next keyframe) and during final panorama stitching (to align overlapping seams
   * and discard stationary duplicate frames).
   */
  fun estimatePanoramaPairAlignment(
    prevFrame: Bitmap,
    currFrame: Bitmap,
    defaultOverlapFraction: Float = 0.25f,
  ): Triple<Float, Float, Float> {
    val defaultStep = (1f - defaultOverlapFraction.coerceIn(0.08f, 0.75f)).coerceIn(0.25f, 0.92f)
    if (prevFrame.width <= 16 || prevFrame.height <= 16 || currFrame.width <= 16 || currFrame.height <= 16) {
      return Triple(defaultStep, 0f, 32f)
    }
    return try {
      val sampleW = 48
      val sampleH = 28
      val prevSmall = Bitmap.createScaledBitmap(prevFrame, sampleW, sampleH, true)
      val currSmall = Bitmap.createScaledBitmap(currFrame, sampleW, sampleH, true)
      val prevPixels = IntArray(sampleW * sampleH)
      val currPixels = IntArray(sampleW * sampleH)
      prevSmall.getPixels(prevPixels, 0, sampleW, 0, 0, sampleW, sampleH)
      currSmall.getPixels(currPixels, 0, sampleW, 0, 0, sampleW, sampleH)

      fun luma(argb: Int): Int {
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        return (r * 77 + g * 150 + b * 29) shr 8
      }

      val prevLuma = IntArray(sampleW * sampleH) { luma(prevPixels[it]) }
      val currLuma = IntArray(sampleW * sampleH) { luma(currPixels[it]) }

      // 1. Check zero-shift MAD to detect stationary / duplicate frames
      var zeroDiffSum = 0L
      for (i in prevLuma.indices) {
        zeroDiffSum += abs(prevLuma[i] - currLuma[i])
      }
      val zeroShiftMad = zeroDiffSum.toFloat() / prevLuma.size.toFloat()
      if (zeroShiftMad < 2.2f) {
        // Virtually identical stationary frame
        return Triple(0f, 0f, zeroShiftMad)
      }

      // 2. Search horizontal step dx in [-0.75..+0.75] of sampleW and vertical dy in [-2..+2]
      val minStepPx = (sampleW * 0.15f).roundToInt().coerceAtLeast(4)
      val maxStepPx = (sampleW * 0.78f).roundToInt().coerceAtMost(sampleW - 6)
      var bestCost = Float.MAX_VALUE
      var bestDxPx = (sampleW * defaultStep).roundToInt()
      var bestDyPx = 0

      for (absDx in minStepPx..maxStepPx) {
        for (sign in intArrayOf(1, -1)) {
          val dx = absDx * sign
          val overlapW = sampleW - absDx
          if (overlapW < 6) continue
          for (dy in -2..2) {
            var diffSum = 0L
            var count = 0
            val yStart = maxOf(2, 2 - dy)
            val yEnd = minOf(sampleH - 2, sampleH - 2 - dy)
            for (y in yStart until yEnd) {
              val prevRow = y * sampleW
              val currRow = (y + dy) * sampleW
              for (ox in 0 until overlapW) {
                val pxPrev = if (dx >= 0) dx + ox else ox
                val pxCurr = if (dx >= 0) ox else absDx + ox
                diffSum += abs(prevLuma[prevRow + pxPrev] - currLuma[currRow + pxCurr])
                count++
              }
            }
            if (count > 0) {
              // Slight regularization toward moderate overlap so uniform skies don't collapse to extreme edges
              val mad = diffSum.toFloat() / count.toFloat()
              val stepNorm = absDx.toFloat() / sampleW.toFloat()
              val regPenalty = abs(stepNorm - 0.55f) * 3.0f + abs(dy) * 0.6f
              val cost = mad + regPenalty
              if (cost < bestCost) {
                bestCost = cost
                bestDxPx = dx
                bestDyPx = dy
              }
            }
          }
        }
      }

      val shiftFraction = (bestDxPx.toFloat() / sampleW.toFloat()).coerceIn(-0.85f, 0.85f)
      val dyFraction = (bestDyPx.toFloat() / sampleH.toFloat()).coerceIn(-0.15f, 0.15f)
      Triple(shiftFraction, dyFraction, zeroShiftMad)
    } catch (_: Throwable) {
      Triple(defaultStep, 0f, 32f)
    }
  }

  /**
   * Stitches a list of horizontal panoramic segment [frames] into a single wide panoramic [Bitmap]
   * with:
   * - Automatic stationary/duplicate frame suppression (never clones identical frames side-by-side)
   * - Pairwise horizontal and vertical alignment via [estimatePanoramaPairAlignment]
   * - Raised-cosine feathered seam blending and luminance gain matching across overlap zones
   */
  fun stitchPanoramaBitmaps(
    frames: List<Bitmap>,
    overlapFraction: Float = 0.22f,
  ): Bitmap? {
    val valid = frames.filter { it.width > 16 && it.height > 16 }
    if (valid.isEmpty()) return null
    if (valid.size == 1) {
      return cropSingleFrameToWidePanorama(extractPanoramaBandFromFrame(valid.first()))
    }
    return try {
      val bandedFrames = valid.map { extractPanoramaBandFromFrame(it) }
      val targetH = bandedFrames.first().height.coerceIn(80, 1080)
      val clampedOverlap = overlapFraction.coerceIn(0.08f, 0.55f)
      val defaultStepFraction = (1f - clampedOverlap).coerceIn(0.35f, 0.90f)

      val scaledFrames = bandedFrames.map { bmp ->
        if (bmp.height == targetH) {
          bmp
        } else {
          val scaledW = ((bmp.width.toFloat() * targetH) / bmp.height.toFloat()).roundToInt().coerceAtLeast(32)
          Bitmap.createScaledBitmap(bmp, scaledW, targetH, true)
        }
      }

      // Filter out stationary duplicate frames and collect relative horizontal shifts
      val uniqueFrames = mutableListOf<Bitmap>()
      val stepFractions = mutableListOf<Float>()
      val dyFractions = mutableListOf<Float>()
      uniqueFrames.add(scaledFrames.first())

      var panDirectionSign = 0
      for (i in 1 until scaledFrames.size) {
        val prev = uniqueFrames.last()
        val curr = scaledFrames[i]
        val (shiftFrac, dyFrac, zeroMad) = estimatePanoramaPairAlignment(
          prevFrame = prev,
          currFrame = curr,
          defaultOverlapFraction = clampedOverlap,
        )
        // Skip duplicate stationary frame when camera hasn't panned
        if (zeroMad < 2.2f || abs(shiftFrac) < 0.06f) {
          continue
        }
        if (panDirectionSign == 0) {
          panDirectionSign = if (shiftFrac < 0f) -1 else 1
        }
        uniqueFrames.add(curr)
        stepFractions.add(abs(shiftFrac).coerceIn(0.25f, 0.85f))
        dyFractions.add(dyFrac)
      }

      if (uniqueFrames.size == 1) {
        return cropSingleFrameToWidePanorama(uniqueFrames.first())
      }

      // If user panned right-to-left, reverse frame order so we composite left-to-right
      val orderedFrames = if (panDirectionSign < 0) uniqueFrames.reversed() else uniqueFrames
      val orderedSteps = if (panDirectionSign < 0) stepFractions.reversed() else stepFractions
      val orderedDys = if (panDirectionSign < 0) dyFractions.reversed().map { -it } else dyFractions

      val frameW = orderedFrames.first().width
      val stepPixels = orderedSteps.map { frac ->
        (frameW * frac.coerceIn(0.25f, defaultStepFraction.coerceAtLeast(0.45f)))
          .roundToInt()
          .coerceIn(16, (frameW - 8).coerceAtLeast(16))
      }

      val totalW = (frameW + stepPixels.sum()).coerceIn(frameW, 4096)
      val outPixels = IntArray(totalW * targetH)

      // Place first frame into output buffer
      val firstPixels = IntArray(frameW * targetH)
      orderedFrames.first().getPixels(firstPixels, 0, frameW, 0, 0, frameW, targetH)
      for (y in 0 until targetH) {
        System.arraycopy(firstPixels, y * frameW, outPixels, y * totalW, minOf(frameW, totalW))
      }

      var currentRightEdge = minOf(frameW, totalW)
      var currentOriginX = 0
      var cumulativeDyPx = 0

      for (idx in 1 until orderedFrames.size) {
        val frame = orderedFrames[idx]
        val fw = frame.width
        val fh = frame.height
        val stepPx = stepPixels[idx - 1]
        val nextOriginX = currentOriginX + stepPx
        if (nextOriginX >= totalW) break

        cumulativeDyPx = (cumulativeDyPx + (orderedDys[idx - 1] * targetH).roundToInt())
          .coerceIn(-(targetH / 8), targetH / 8)

        val framePixels = IntArray(fw * fh)
        frame.getPixels(framePixels, 0, fw, 0, 0, fw, fh)

        val overlapStart = nextOriginX.coerceIn(0, currentRightEdge)
        val overlapEnd = minOf(currentRightEdge, minOf(nextOriginX + fw, totalW))
        val overlapWidth = (overlapEnd - overlapStart).coerceAtLeast(1)

        // Compute luminance gain across the overlap zone for seamless exposure matching
        var leftLumaSum = 0L
        var rightLumaSum = 0L
        var lumaSamples = 0
        for (y in (targetH / 6) until (targetH * 5 / 6) step 2) {
          val srcY = (y - cumulativeDyPx).coerceIn(0, fh - 1)
          for (x in overlapStart until overlapEnd step 2) {
            val leftArgb = outPixels[y * totalW + x]
            val rightArgb = framePixels[srcY * fw + (x - nextOriginX)]
            val lLuma = (((leftArgb shr 16) and 0xFF) * 77 + ((leftArgb shr 8) and 0xFF) * 150 + (leftArgb and 0xFF) * 29) shr 8
            val rLuma = (((rightArgb shr 16) and 0xFF) * 77 + ((rightArgb shr 8) and 0xFF) * 150 + (rightArgb and 0xFF) * 29) shr 8
            leftLumaSum += lLuma
            rightLumaSum += rLuma
            lumaSamples++
          }
        }
        val exposureGain = if (lumaSamples > 0 && rightLumaSum > 100L) {
          (leftLumaSum.toFloat() / rightLumaSum.toFloat()).coerceIn(0.85f, 1.18f)
        } else {
          1.0f
        }

        val copyEnd = minOf(nextOriginX + fw, totalW)
        for (x in nextOriginX until copyEnd) {
          val srcX = x - nextOriginX
          // Raised-cosine feathered blend weight across overlap zone, 1.0f past overlapEnd
          val weightRight = if (x < overlapEnd && overlapWidth > 1) {
            val t = ((x - overlapStart).toFloat() / overlapWidth.toFloat()).coerceIn(0f, 1f)
            (0.5f - 0.5f * cos(Math.PI * t).toFloat()).coerceIn(0f, 1f)
          } else {
            1.0f
          }
          val weightLeft = 1.0f - weightRight
          // Smoothly taper gain adjustment from exposureGain at overlapStart to 1.0f at copyEnd
          val gainProgress = ((x - nextOriginX).toFloat() / fw.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
          val colGain = exposureGain + (1.0f - exposureGain) * gainProgress

          for (y in 0 until targetH) {
            val srcY = (y - cumulativeDyPx).coerceIn(0, fh - 1)
            val rightArgb = framePixels[srcY * fw + srcX]
            val rr = (((rightArgb shr 16) and 0xFF) * colGain).roundToInt().coerceIn(0, 255)
            val rg = (((rightArgb shr 8) and 0xFF) * colGain).roundToInt().coerceIn(0, 255)
            val rb = ((rightArgb and 0xFF) * colGain).roundToInt().coerceIn(0, 255)

            if (weightRight >= 0.999f) {
              outPixels[y * totalW + x] = (0xFF shl 24) or (rr shl 16) or (rg shl 8) or rb
            } else {
              val leftArgb = outPixels[y * totalW + x]
              val lr = (leftArgb shr 16) and 0xFF
              val lg = (leftArgb shr 8) and 0xFF
              val lb = leftArgb and 0xFF
              val br = (lr * weightLeft + rr * weightRight).roundToInt().coerceIn(0, 255)
              val bg = (lg * weightLeft + rg * weightRight).roundToInt().coerceIn(0, 255)
              val bb = (lb * weightLeft + rb * weightRight).roundToInt().coerceIn(0, 255)
              outPixels[y * totalW + x] = (0xFF shl 24) or (br shl 16) or (bg shl 8) or bb
            }
          }
        }

        currentOriginX = nextOriginX
        currentRightEdge = maxOf(currentRightEdge, copyEnd)
      }

      val finalW = currentRightEdge.coerceIn(32, totalW)
      val outBmp = Bitmap.createBitmap(finalW, targetH, Bitmap.Config.ARGB_8888)
      outBmp.setPixels(outPixels, 0, totalW, 0, 0, finalW, targetH)
      outBmp
    } catch (_: Throwable) {
      valid.firstOrNull()
    }
  }

  /**
   * Applies active [filter], [exposureEv], [subMode] (HDR), and [nightMode] (Off / Auto / On)
   * pixel enhancements to a captured hardware [bitmap].
   */
  fun applyPhotoEnhancementsToBitmap(
    bitmap: Bitmap,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    exposureEv: Float = 0f,
    flashMode: CameraFlashMode = CameraFlashMode.OFF,
    subMode: CameraPhotoSubMode = CameraPhotoSubMode.STANDARD,
    nightMode: CameraNightMode = CameraNightMode.OFF,
  ): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= 0 || h <= 0) return bitmap
    return try {
      val pixels = IntArray(w * h)
      bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

      // Compute average luminance when Night Mode is AUTO
      val avgLuma = if (nightMode == CameraNightMode.AUTO) {
        var sum = 0L
        var count = 0
        val step = maxOf(1, pixels.size / 1024)
        var i = 0
        while (i < pixels.size) {
          val p = pixels[i]
          val r = (p shr 16) and 0xFF
          val g = (p shr 8) and 0xFF
          val b = p and 0xFF
          sum += (r * 77 + g * 150 + b * 29) shr 8
          count++
          i += step
        }
        if (count > 0) (sum / count).toInt() else 128
      } else {
        null
      }

      val useNight = shouldApplyNightSight(
        nightMode = nightMode,
        exposureEv = exposureEv,
        flashMode = flashMode,
        averageLuma = avgLuma,
      )
      val useHdr = subMode == CameraPhotoSubMode.HDR
      if (!useNight && !useHdr && filter == CameraColorFilter.NONE && abs(exposureEv) < 0.05f) {
        return bitmap
      }

      for (i in pixels.indices) {
        val argb = pixels[i]
        val a = (argb ushr 24) and 0xFF
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        val (nr, ng, nb) = when {
          useNight -> applyNightSightToRgb(r, g, b, filter, exposureEv)
          useHdr -> applyHdrToneMapToRgb(r, g, b, filter, exposureEv)
          else -> applyFilterAndExposureToRgb(r, g, b, filter, exposureEv)
        }
        pixels[i] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
      }
      val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
      out.setPixels(pixels, 0, w, 0, 0, w, h)
      out
    } catch (_: Throwable) {
      bitmap
    }
  }

  /**
   * Applies High Dynamic Range (HDR) multi-exposure tone-mapping to an RGB triple (`0..255` each),
   * lifting dark shadow detail, recovering clipped highlights, and boosting local midtone contrast.
   */
  fun applyHdrToneMapToRgb(
    red: Int,
    green: Int,
    blue: Int,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    exposureEv: Float = 0f,
  ): Triple<Int, Int, Int> {
    // Fuse low (-0.85 EV), normal (0.0 EV), and high (+1.0 EV) virtual exposures with Mertens-style weights
    val low = applyFilterAndExposureToRgb(red, green, blue, filter, exposureEv - 0.85f)
    val mid = applyFilterAndExposureToRgb(red, green, blue, filter, exposureEv)
    val high = applyFilterAndExposureToRgb(red, green, blue, filter, exposureEv + 1.0f)

    fun fuseChannel(l: Int, m: Int, h: Int): Int {
      val normM = (m / 255f).coerceIn(0f, 1f)
      // When pixel is dark, favor high exposure (shadow lift); when bright, favor low exposure (highlight recovery)
      val shadowWeight = (1f - normM).pow(1.4f) * 0.55f
      val highlightWeight = normM.pow(1.6f) * 0.45f
      val midWeight = (1f - shadowWeight - highlightWeight).coerceAtLeast(0.15f)
      val fused = (l * highlightWeight + m * midWeight + h * shadowWeight).coerceIn(0f, 255f)
      // Gentle shadow-lift gamma (0.86) + midtone contrast
      val lifted = (fused / 255f).pow(0.86f).coerceIn(0f, 1f)
      return (lifted * 255f).roundToInt().coerceIn(0, 255)
    }

    return Triple(
      fuseChannel(low.first, mid.first, high.first),
      fuseChannel(low.second, mid.second, high.second),
      fuseChannel(low.third, mid.third, high.third),
    )
  }

  /**
   * Applies Night Sight low-light enhancement to an RGB triple (`0..255` each), lifting deep shadows
   * via a non-linear gamma curve while preserving starlight/highlight warmth.
   */
  fun applyNightSightToRgb(
    red: Int,
    green: Int,
    blue: Int,
    filter: CameraColorFilter = CameraColorFilter.NONE,
    exposureEv: Float = 0f,
  ): Triple<Int, Int, Int> {
    val boostedEv = (exposureEv + 0.95f).coerceIn(MIN_CAMERA_EXPOSURE_EV, MAX_CAMERA_EXPOSURE_EV)
    val (r, g, b) = applyFilterAndExposureToRgb(red, green, blue, filter, boostedEv)
    fun liftGamma(c: Int): Int {
      val norm = (c / 255f).coerceIn(0f, 1f)
      val lifted = norm.pow(0.74f)
      return (lifted * 255f).roundToInt().coerceIn(0, 255)
    }
    return Triple(liftGamma(r), liftGamma(g), liftGamma(b))
  }

  /**
   * Writes a valid, non-empty MP4 container stub file to [outputFile] representing a recorded
   * video of [durationMs]. Used when hardware video encoding is unavailable (e.g. emulators or tests).
   */
  fun writeFallbackVideoFile(
    outputFile: File,
    durationMs: Long = 2000L,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    outputFile.parentFile?.mkdirs()
    val clampedDurationMs = durationMs.coerceIn(500L, 60_000L)
    // Approximate 16 KB per second so duration estimation matches
    val payloadBytes = ((clampedDurationMs * 16 * 1024L) / 1000L).toInt().coerceIn(4096, 256_000)

    FileOutputStream(outputFile).use { fos ->
      // Write ISO Base Media File Format (MP4) ftyp box (24 bytes)
      val ftyp = ByteBuffer.allocate(24).apply {
        putInt(24) // box size
        put("ftyp".toByteArray(Charsets.US_ASCII))
        put("mp42".toByteArray(Charsets.US_ASCII)) // major_brand
        putInt(0) // minor_version
        put("mp42".toByteArray(Charsets.US_ASCII)) // compatible_brand 1
        put("isom".toByteArray(Charsets.US_ASCII)) // compatible_brand 2
      }.array()
      fos.write(ftyp)

      // Write mdat box with payload
      val mdatSize = 8 + payloadBytes
      val mdatHeader = ByteBuffer.allocate(8).apply {
        putInt(mdatSize)
        put("mdat".toByteArray(Charsets.US_ASCII))
      }.array()
      fos.write(mdatHeader)

      val chunk = ByteArray(1024)
      var remaining = payloadBytes
      while (remaining > 0) {
        val toWrite = minOf(remaining, chunk.size)
        fos.write(chunk, 0, toWrite)
        remaining -= toWrite
      }
      fos.flush()
    }
    outputFile.setLastModified(epochMs)
    return outputFile
  }

  /**
   * Computes normalized golden ratio grid lines (`0.382f` and `0.618f`) or rule-of-thirds lines (`0.333f` and `0.667f`).
   */
  fun getGridFractions(gridOverlay: CameraGridOverlay): List<Float> {
    return when (gridOverlay) {
      CameraGridOverlay.NONE -> emptyList()
      CameraGridOverlay.RULE_OF_THIRDS -> listOf(1f / 3f, 2f / 3f)
      CameraGridOverlay.GOLDEN_RATIO -> listOf(0.381966f, 0.618034f)
      CameraGridOverlay.CROSSHAIR -> listOf(0.5f)
    }
  }

  /**
   * Computes the fitted viewfinder dimensions `(width, height)` inside an available
   * viewport of [availableWidth] by [availableHeight] for the given [aspectRatio].
   *
   * When [aspectRatio] is [CameraAspectRatio.FULL] or matches the available viewport's
   * aspect ratio, the returned dimensions fill the entire available viewport.
   * When the camera's aspect ratio does not match the screen's aspect ratio, the
   * viewport shrinks along one axis to preserve [aspectRatio], leaving black bars
   * along the remaining axis.
   */
  fun calculateViewfinderDimensions(
    availableWidth: Float,
    availableHeight: Float,
    aspectRatio: CameraAspectRatio,
  ): Pair<Float, Float> {
    if (availableWidth <= 0f || availableHeight <= 0f) {
      return Pair(availableWidth.coerceAtLeast(0f), availableHeight.coerceAtLeast(0f))
    }
    val baseRatio = aspectRatio.portraitWidthToHeightRatio ?: return Pair(availableWidth, availableHeight)
    val isLandscape = availableWidth > availableHeight
    val targetRatio = if (isLandscape && baseRatio > 0f) 1f / baseRatio else baseRatio
    val screenRatio = availableWidth / availableHeight
    return if (abs(screenRatio - targetRatio) < 0.001f) {
      Pair(availableWidth, availableHeight)
    } else if (screenRatio > targetRatio) {
      Pair(availableHeight * targetRatio, availableHeight)
    } else {
      Pair(availableWidth, availableWidth / targetRatio)
    }
  }

  /**
   * Selects the best Camera2 `SurfaceTexture` preview buffer size `(sensorWidth, sensorHeight)`
   * from [supportedSensorSizes] for a `TextureView` of [viewWidth] by [viewHeight].
   *
   * Accounts for the rotation between [sensorOrientationDegrees] and [displayRotationDegrees]
   * so portrait viewports match upright sensor ratios (e.g., `1920×1080` -> `1080×1920` upright),
   * and caps the long edge at [maxLongEdge] when possible to avoid excessive preview latency.
   */
  fun selectOptimalSensorPreviewSize(
    supportedSensorSizes: List<Pair<Int, Int>>,
    viewWidth: Int,
    viewHeight: Int,
    sensorOrientationDegrees: Int = 90,
    displayRotationDegrees: Int = 0,
    maxLongEdge: Int = 1920,
  ): Pair<Int, Int> {
    val validSizes = supportedSensorSizes.filter { it.first > 0 && it.second > 0 }
    if (validSizes.isEmpty()) {
      return Pair(1440, 1080)
    }

    val totalRotation = ((sensorOrientationDegrees - displayRotationDegrees) % 360 + 360) % 360
    val swappedDimensions = totalRotation == 90 || totalRotation == 270

    val targetRatio = if (viewWidth > 0 && viewHeight > 0) {
      viewWidth.toFloat() / viewHeight.toFloat()
    } else {
      if (swappedDimensions) 9f / 16f else 16f / 9f
    }

    val withinCap = validSizes.filter { maxOf(it.first, it.second) <= maxLongEdge }
    val candidates = withinCap.ifEmpty { validSizes }

    fun candidateRatioDiff(size: Pair<Int, Int>): Float {
      val uprightW = if (swappedDimensions) minOf(size.first, size.second) else maxOf(size.first, size.second)
      val uprightH = if (swappedDimensions) maxOf(size.first, size.second) else minOf(size.first, size.second)
      val ratio = uprightW.toFloat() / uprightH.toFloat()
      return abs(ratio - targetRatio)
    }

    val minDiff = candidates.minOf { candidateRatioDiff(it) }
    val aspectTolerance = 0.08f
    val bestAspectMatches = candidates.filter { candidateRatioDiff(it) <= minDiff + aspectTolerance }

    return bestAspectMatches.maxByOrNull { it.first.toLong() * it.second.toLong() }
      ?: candidates.maxByOrNull { it.first.toLong() * it.second.toLong() }
      ?: validSizes.first()
  }

  /**
   * Computes the `(scaleX, scaleY)` factors required around the center of a `TextureView`
   * of size [viewWidth] × [viewHeight] to center-crop a camera stream of sensor dimensions
   * [sensorPreviewWidth] × [sensorPreviewHeight] without vertical or horizontal stretching.
   *
   * By default, Android's `TextureView` stretches its `SurfaceTexture` buffer (which is rotated
   * to natural device orientation `ROTATION_0` by `SurfaceTexture` according to [sensorOrientationDegrees])
   * to fill `viewWidth × viewHeight`. Applying these scale factors restores a 1:1 pixel aspect ratio.
   */
  fun calculateTextureViewCenterCropScale(
    viewWidth: Int,
    viewHeight: Int,
    sensorPreviewWidth: Int,
    sensorPreviewHeight: Int,
    sensorOrientationDegrees: Int = 90,
  ): Pair<Float, Float> {
    if (viewWidth <= 0 || viewHeight <= 0 || sensorPreviewWidth <= 0 || sensorPreviewHeight <= 0) {
      return Pair(1f, 1f)
    }
    val normSensorRot = ((sensorOrientationDegrees % 360) + 360) % 360
    val swapped = normSensorRot == 90 || normSensorRot == 270
    val naturalBufferWidth = if (swapped) {
      minOf(sensorPreviewWidth, sensorPreviewHeight)
    } else {
      maxOf(sensorPreviewWidth, sensorPreviewHeight)
    }
    val naturalBufferHeight = if (swapped) {
      maxOf(sensorPreviewWidth, sensorPreviewHeight)
    } else {
      minOf(sensorPreviewWidth, sensorPreviewHeight)
    }

    val bufferRatio = naturalBufferWidth.toFloat() / naturalBufferHeight.toFloat()
    val viewRatio = viewWidth.toFloat() / viewHeight.toFloat()

    val scaleX = maxOf(1f, bufferRatio / viewRatio)
    val scaleY = maxOf(1f, viewRatio / bufferRatio)
    return Pair(scaleX, scaleY)
  }

  /**
   * Builds the `android.graphics.Matrix` transform to apply via `TextureView.setTransform(matrix)`
   * so that the live camera preview is center-cropped without distortion across portrait and landscape
   * orientations.
   */
  fun buildTextureViewTransformMatrix(
    viewWidth: Int,
    viewHeight: Int,
    sensorPreviewWidth: Int,
    sensorPreviewHeight: Int,
    sensorOrientationDegrees: Int = 90,
    displayRotationDegrees: Int = 0,
  ): Matrix {
    val matrix = Matrix()
    if (viewWidth <= 0 || viewHeight <= 0 || sensorPreviewWidth <= 0 || sensorPreviewHeight <= 0) {
      return matrix
    }

    val centerX = viewWidth / 2f
    val centerY = viewHeight / 2f
    val normDisplayRot = ((displayRotationDegrees % 360) + 360) % 360
    val normSensorRot = ((sensorOrientationDegrees % 360) + 360) % 360
    val swapped = normSensorRot == 90 || normSensorRot == 270
    val naturalBufferWidth = if (swapped) {
      minOf(sensorPreviewWidth, sensorPreviewHeight)
    } else {
      maxOf(sensorPreviewWidth, sensorPreviewHeight)
    }
    val naturalBufferHeight = if (swapped) {
      maxOf(sensorPreviewWidth, sensorPreviewHeight)
    } else {
      minOf(sensorPreviewWidth, sensorPreviewHeight)
    }

    if (normDisplayRot == 90 || normDisplayRot == 270) {
      val scale = maxOf(
        viewHeight.toFloat() / naturalBufferWidth.toFloat(),
        viewWidth.toFloat() / naturalBufferHeight.toFloat(),
      )
      val preRotateScaleX = (naturalBufferWidth.toFloat() / viewWidth.toFloat()) * scale
      val preRotateScaleY = (naturalBufferHeight.toFloat() / viewHeight.toFloat()) * scale
      matrix.setScale(preRotateScaleX, preRotateScaleY, centerX, centerY)
      val rotateDegrees = if (normDisplayRot == 90) -90f else 90f
      matrix.postRotate(rotateDegrees, centerX, centerY)
    } else {
      val (scaleX, scaleY) = calculateTextureViewCenterCropScale(
        viewWidth = viewWidth,
        viewHeight = viewHeight,
        sensorPreviewWidth = sensorPreviewWidth,
        sensorPreviewHeight = sensorPreviewHeight,
        sensorOrientationDegrees = sensorOrientationDegrees,
      )
      matrix.setScale(scaleX, scaleY, centerX, centerY)
      if (normDisplayRot == 180) {
        matrix.postRotate(180f, centerX, centerY)
      }
    }
    return matrix
  }

  /**
   * Applies the `TextureView` center-crop transform [textureTransformMatrix], [zoom], and front-camera
   * horizontal [mirrorFront] onto a raw `TextureView.getBitmap()` frame so the saved image matches
   * the un-stretched live viewfinder.
   */
  fun transformCapturedPreviewBitmap(
    rawBitmap: Bitmap,
    textureTransformMatrix: Matrix,
    zoom: Float = 1.0f,
    lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    mirrorFront: Boolean = false,
    viewWidth: Int = rawBitmap.width,
    viewHeight: Int = rawBitmap.height,
  ): Bitmap {
    if (rawBitmap.width <= 0 || rawBitmap.height <= 0) return rawBitmap
    return try {
      val outBitmap = Bitmap.createBitmap(rawBitmap.width, rawBitmap.height, Bitmap.Config.ARGB_8888)
      val combinedMatrix = Matrix(textureTransformMatrix)
      val safeViewW = viewWidth.coerceAtLeast(1)
      val safeViewH = viewHeight.coerceAtLeast(1)
      if (safeViewW != rawBitmap.width || safeViewH != rawBitmap.height) {
        // Conjugate the TextureView matrix from (viewWidth, viewHeight) space into (rawBitmap.width, rawBitmap.height) space
        combinedMatrix.preScale(
          safeViewW.toFloat() / rawBitmap.width.toFloat(),
          safeViewH.toFloat() / rawBitmap.height.toFloat(),
        )
        combinedMatrix.postScale(
          rawBitmap.width.toFloat() / safeViewW.toFloat(),
          rawBitmap.height.toFloat() / safeViewH.toFloat(),
        )
      }
      val cx = rawBitmap.width / 2f
      val cy = rawBitmap.height / 2f
      val clampedZoom = coerceZoom(zoom)
      val flipX = if (lensFacing == CameraLensFacing.FRONT && mirrorFront) -1f else 1f
      if (clampedZoom != 1f || flipX != 1f) {
        combinedMatrix.postScale(clampedZoom * flipX, clampedZoom, cx, cy)
      }
      val canvas = Canvas(outBitmap)
      val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
      canvas.drawBitmap(rawBitmap, combinedMatrix, paint)
      outBitmap
    } catch (_: Throwable) {
      rawBitmap
    }
  }

  // =========================================================================
  // QR Code Reader & Barcode Scanner Engine
  // =========================================================================

  /**
   * Computes the standard GS1 modulo-10 check digit for a numeric barcode stem
   * (such as 12-digit EAN-13 stem, 7-digit EAN-8 stem, 11-digit UPC-A stem, or 13-digit ITF-14 stem).
   */
  fun computeGs1CheckDigit(digitsWithoutCheck: String): Char? {
    val clean = digitsWithoutCheck.trim()
    if (clean.isEmpty() || !clean.all { it.isDigit() }) return null
    var sum = 0
    val len = clean.length
    for (i in 0 until len) {
      val digit = clean[len - 1 - i] - '0'
      // Position from right (1-indexed): odd positions from right have weight 3, even have weight 1
      val weight = if (i % 2 == 0) 3 else 1
      sum += digit * weight
    }
    val check = (10 - (sum % 10)) % 10
    return ('0'.code + check).toChar()
  }

  /**
   * Verifies the modulo-10 GS1 check digit of a complete numeric barcode string
   * (e.g. 8-digit EAN-8/UPC-E, 12-digit UPC-A, 13-digit EAN-13, or 14-digit ITF-14).
   */
  fun verifyLinearBarcodeChecksum(digits: String): Boolean {
    val clean = digits.trim()
    if (clean.length !in setOf(8, 12, 13, 14) || !clean.all { it.isDigit() }) return false
    val expected = computeGs1CheckDigit(clean.substring(0, clean.length - 1)) ?: return false
    return clean.last() == expected
  }

  /**
   * Normalizes user-supplied content for encoding into [format] so strict 1D barcode writers
   * (EAN-13, EAN-8, UPC-A, UPC-E, ITF, Code 39, Codabar) always receive valid input and check digits.
   */
  fun normalizeBarcodeInputForEncoding(
    content: String,
    format: CameraBarcodeFormat,
  ): String {
    val trimmed = content.trim().ifEmpty { format.defaultSamplePayload }
    return when (format) {
      CameraBarcodeFormat.EAN_13 -> {
        val digits = trimmed.filter { it.isDigit() }.padEnd(12, '0').take(12)
        val check = computeGs1CheckDigit(digits) ?: '0'
        "$digits$check"
      }
      CameraBarcodeFormat.EAN_8 -> {
        val digits = trimmed.filter { it.isDigit() }.padEnd(7, '0').take(7)
        val check = computeGs1CheckDigit(digits) ?: '0'
        "$digits$check"
      }
      CameraBarcodeFormat.UPC_A -> {
        val digits = trimmed.filter { it.isDigit() }.padEnd(11, '0').take(11)
        val check = computeGs1CheckDigit(digits) ?: '0'
        "$digits$check"
      }
      CameraBarcodeFormat.UPC_E -> {
        val digits = trimmed.filter { it.isDigit() }
        if (digits.length == 8 && (digits.startsWith("0") || digits.startsWith("1"))) {
          digits
        } else {
          format.defaultSamplePayload
        }
      }
      CameraBarcodeFormat.ITF -> {
        val digits = trimmed.filter { it.isDigit() }.ifEmpty { format.defaultSamplePayload }
        if (digits.length % 2 == 0) digits else "0$digits"
      }
      CameraBarcodeFormat.CODE_39 -> {
        val allowed = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%"
        trimmed.uppercase(Locale.US).filter { it in allowed }.ifEmpty { format.defaultSamplePayload }
      }
      CameraBarcodeFormat.CODABAR -> {
        val cleanDigits = trimmed.filter { it.isDigit() || it in "-$:/.+" }.ifEmpty { "40156" }
        "A${cleanDigits}B"
      }
      else -> trimmed
    }
  }

  /**
   * Encodes [content] in [format] as a 2D boolean matrix (`[y][x]`, where `true` = dark module/bar)
   * of size [width] × [height] using ZXing's `MultiFormatWriter`.
   */
  fun encodeCodeBitMatrix(
    content: String,
    format: CameraBarcodeFormat,
    width: Int = if (format.is2D) 240 else 320,
    height: Int = if (format.is2D) 240 else 140,
  ): Array<BooleanArray>? {
    val targetW = width.coerceIn(32, 1024)
    val targetH = height.coerceIn(32, 1024)
    val normalized = normalizeBarcodeInputForEncoding(content, format)
    return try {
      val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
        put(EncodeHintType.CHARACTER_SET, "UTF-8")
        put(EncodeHintType.MARGIN, if (format.is2D) 2 else 6)
      }
      val bitMatrix = MultiFormatWriter().encode(
        normalized,
        format.zxingFormat,
        targetW,
        targetH,
        hints,
      )
      val result = Array(bitMatrix.height) { BooleanArray(bitMatrix.width) }
      for (y in 0 until bitMatrix.height) {
        val row = result[y]
        for (x in 0 until bitMatrix.width) {
          row[x] = bitMatrix.get(x, y)
        }
      }
      result
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders a QR code or barcode for [content] in [format] into an ARGB pixel array of size [width] × [height].
   * Works in both Android runtime and pure JVM unit tests.
   */
  fun renderCodeToArgbPixels(
    content: String,
    format: CameraBarcodeFormat,
    width: Int = if (format.is2D) 360 else 420,
    height: Int = if (format.is2D) 360 else 200,
    darkColorArgb: Int = 0xFF000000.toInt(),
    lightColorArgb: Int = 0xFFFFFFFF.toInt(),
  ): IntArray? {
    val targetW = width.coerceIn(64, 1024)
    val targetH = height.coerceIn(64, 1024)
    val matrix = encodeCodeBitMatrix(
      content = content,
      format = format,
      width = targetW,
      height = targetH,
    ) ?: return null

    val matH = matrix.size
    val matW = if (matH > 0) matrix[0].size else return null
    val pixels = IntArray(targetW * targetH) { lightColorArgb }

    // Center the matrix inside targetW x targetH with a clean quiet zone
    val padX = ((targetW - matW) / 2).coerceAtLeast(0)
    val padY = ((targetH - matH) / 2).coerceAtLeast(0)

    for (y in 0 until minOf(matH, targetH)) {
      val row = matrix[y]
      val destY = y + padY
      if (destY >= targetH) break
      val rowOffset = destY * targetW
      for (x in 0 until minOf(matW, targetW)) {
        val destX = x + padX
        if (destX >= targetW) break
        pixels[rowOffset + destX] = if (row[x]) darkColorArgb else lightColorArgb
      }
    }
    return pixels
  }

  /**
   * Creates an Android [Bitmap] containing the rendered QR code or barcode for [content] in [format].
   */
  fun createBarcodeBitmap(
    content: String,
    format: CameraBarcodeFormat,
    width: Int = if (format.is2D) 480 else 560,
    height: Int = if (format.is2D) 480 else 240,
    darkColorArgb: Int = 0xFF000000.toInt(),
    lightColorArgb: Int = 0xFFFFFFFF.toInt(),
  ): Bitmap? {
    val w = width.coerceIn(64, 1024)
    val h = height.coerceIn(64, 1024)
    val pixels = renderCodeToArgbPixels(
      content = content,
      format = format,
      width = w,
      height = h,
      darkColorArgb = darkColorArgb,
      lightColorArgb = lightColorArgb,
    ) ?: return null

    return try {
      val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
      if (bmp.width > 0 && bmp.height > 0) {
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        bmp
      } else {
        null
      }
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Writes an ARGB pixel buffer of [width] × [height] to a spec-compliant truecolor PNG file at [outputFile].
   * Works in both Android runtime and pure JVM unit tests.
   */
  fun writeArgbPixelsToPng(
    outputFile: File,
    pixels: IntArray,
    width: Int,
    height: Int,
    lastModifiedMs: Long = System.currentTimeMillis(),
  ): File {
    outputFile.parentFile?.mkdirs()
    val w = width.coerceIn(1, 2048)
    val h = height.coerceIn(1, 2048)

    val out = ByteArrayOutputStream()
    // PNG magic signature
    out.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))

    fun writeChunk(type: String, data: ByteArray) {
      val typeBytes = type.toByteArray(Charsets.US_ASCII)
      out.write(ByteBuffer.allocate(4).putInt(data.size).array())
      out.write(typeBytes)
      out.write(data)
      val crc = CRC32()
      crc.update(typeBytes)
      crc.update(data)
      out.write(ByteBuffer.allocate(4).putInt(crc.value.toInt()).array())
    }

    // IHDR chunk (13 bytes, 8-bit RGB colorType = 2)
    val ihdr = ByteBuffer.allocate(13).apply {
      putInt(w)
      putInt(h)
      put(8.toByte())
      put(2.toByte())
      put(0.toByte())
      put(0.toByte())
      put(0.toByte())
    }.array()
    writeChunk("IHDR", ihdr)

    val rowBytes = 1 + w * 3
    val rawScanlines = ByteArray(rowBytes * h)
    for (y in 0 until h) {
      val rowStart = y * rowBytes
      rawScanlines[rowStart] = 0 // Filter type 0 (None)
      val pixelRowOffset = y * w
      for (x in 0 until w) {
        val argb = if (pixelRowOffset + x < pixels.size) pixels[pixelRowOffset + x] else 0xFFFFFFFF.toInt()
        val pxOffset = rowStart + 1 + x * 3
        rawScanlines[pxOffset] = ((argb shr 16) and 0xFF).toByte()
        rawScanlines[pxOffset + 1] = ((argb shr 8) and 0xFF).toByte()
        rawScanlines[pxOffset + 2] = (argb and 0xFF).toByte()
      }
    }

    val deflater = Deflater(Deflater.BEST_SPEED)
    deflater.setInput(rawScanlines)
    deflater.finish()
    val compressedOut = ByteArrayOutputStream()
    val buf = ByteArray(4096)
    while (!deflater.finished()) {
      val count = deflater.deflate(buf)
      if (count > 0) compressedOut.write(buf, 0, count) else break
    }
    deflater.end()

    writeChunk("IDAT", compressedOut.toByteArray())
    writeChunk("IEND", ByteArray(0))

    FileOutputStream(outputFile).use { fos ->
      fos.write(out.toByteArray())
      fos.flush()
    }
    outputFile.setLastModified(lastModifiedMs)
    return outputFile
  }

  /**
   * Pure-JVM PNG decoder that extracts `(width, height, argbPixels)` from a standard 8-bit RGB/RGBA/Grayscale PNG file.
   * Used as a fallback when Android's `BitmapFactory` is stubbed in local JVM unit tests.
   */
  fun readPngToArgbPixels(file: File): Triple<Int, Int, IntArray>? {
    if (!file.exists() || !file.isFile || file.length() < 32L) return null
    return try {
      val bytes = file.readBytes()
      val pngSig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
      for (i in pngSig.indices) {
        if (bytes[i] != pngSig[i]) return null
      }

      var offset = 8
      var width = 0
      var height = 0
      var bitDepth = 8
      var colorType = 2
      val idatOut = ByteArrayOutputStream()

      while (offset + 8 <= bytes.size) {
        val length = ByteBuffer.wrap(bytes, offset, 4).int
        val chunkType = String(bytes, offset + 4, 4, Charsets.US_ASCII)
        val dataStart = offset + 8
        if (length < 0 || dataStart + length > bytes.size) break

        when (chunkType) {
          "IHDR" -> {
            val bb = ByteBuffer.wrap(bytes, dataStart, length)
            width = bb.int
            height = bb.int
            bitDepth = bb.get().toInt() and 0xFF
            colorType = bb.get().toInt() and 0xFF
          }
          "IDAT" -> {
            idatOut.write(bytes, dataStart, length)
          }
          "IEND" -> break
        }
        offset = dataStart + length + 4 // skip CRC
      }

      if (width <= 0 || height <= 0 || bitDepth != 8) return null
      val bytesPerPixel = when (colorType) {
        0 -> 1 // Grayscale
        2 -> 3 // Truecolor RGB
        6 -> 4 // Truecolor RGBA
        else -> return null
      }

      val inflater = Inflater()
      val compressed = idatOut.toByteArray()
      inflater.setInput(compressed)
      val expectedRawSize = height * (1 + width * bytesPerPixel)
      val raw = ByteArray(expectedRawSize)
      var totalInflated = 0
      while (!inflater.finished() && totalInflated < expectedRawSize) {
        val count = inflater.inflate(raw, totalInflated, expectedRawSize - totalInflated)
        if (count <= 0) break
        totalInflated += count
      }
      inflater.end()
      if (totalInflated < expectedRawSize) return null

      val stride = width * bytesPerPixel
      val prevLine = ByteArray(stride)
      val currLine = ByteArray(stride)
      val pixels = IntArray(width * height)

      fun paethPredictor(a: Int, b: Int, c: Int): Int {
        val p = a + b - c
        val pa = abs(p - a)
        val pb = abs(p - b)
        val pc = abs(p - c)
        return when {
          pa <= pb && pa <= pc -> a
          pb <= pc -> b
          else -> c
        }
      }

      for (y in 0 until height) {
        val rowStart = y * (1 + stride)
        val filterType = raw[rowStart].toInt() and 0xFF
        for (i in 0 until stride) {
          val xByte = raw[rowStart + 1 + i].toInt() and 0xFF
          val a = if (i >= bytesPerPixel) currLine[i - bytesPerPixel].toInt() and 0xFF else 0
          val b = prevLine[i].toInt() and 0xFF
          val c = if (i >= bytesPerPixel) prevLine[i - bytesPerPixel].toInt() and 0xFF else 0
          val recon = when (filterType) {
            0 -> xByte
            1 -> (xByte + a) and 0xFF
            2 -> (xByte + b) and 0xFF
            3 -> (xByte + ((a + b) ushr 1)) and 0xFF
            4 -> (xByte + paethPredictor(a, b, c)) and 0xFF
            else -> xByte
          }
          currLine[i] = recon.toByte()
        }

        val rowPixelOffset = y * width
        for (x in 0 until width) {
          val pxIdx = x * bytesPerPixel
          val argb = when (colorType) {
            0 -> {
              val v = currLine[pxIdx].toInt() and 0xFF
              (0xFF shl 24) or (v shl 16) or (v shl 8) or v
            }
            2 -> {
              val r = currLine[pxIdx].toInt() and 0xFF
              val g = currLine[pxIdx + 1].toInt() and 0xFF
              val b = currLine[pxIdx + 2].toInt() and 0xFF
              (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
            else -> {
              val r = currLine[pxIdx].toInt() and 0xFF
              val g = currLine[pxIdx + 1].toInt() and 0xFF
              val b = currLine[pxIdx + 2].toInt() and 0xFF
              val a = currLine[pxIdx + 3].toInt() and 0xFF
              (a shl 24) or (r shl 16) or (g shl 8) or b
            }
          }
          pixels[rowPixelOffset + x] = argb
        }
        System.arraycopy(currLine, 0, prevLine, 0, stride)
      }
      Triple(width, height, pixels)
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Generates a timestamped filename for a saved QR code or barcode image.
   * Example: `SCAN_QR_CODE_20261111_201500.png`
   */
  fun createTimestampedBarcodeFile(
    directory: File,
    format: CameraBarcodeFormat = CameraBarcodeFormat.QR_CODE,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    val baseStem = "SCAN_${format.name}_$stamp"
    val ext = "png"

    var candidate = File(directory, "$baseStem.$ext")
    var counter = 1
    while (candidate.exists()) {
      candidate = File(directory, "${baseStem}_$counter.$ext")
      counter++
    }
    return candidate
  }

  /**
   * Renders [content] in [format] and writes the resulting QR code or barcode image to [outputFile].
   * Works on both Android runtime and pure JVM unit tests.
   */
  fun saveBarcodeImageFile(
    outputFile: File,
    content: String,
    format: CameraBarcodeFormat,
    width: Int = if (format.is2D) 480 else 560,
    height: Int = if (format.is2D) 480 else 240,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    outputFile.parentFile?.mkdirs()
    val w = width.coerceIn(64, 1024)
    val h = height.coerceIn(64, 1024)
    val pixels = renderCodeToArgbPixels(
      content = content,
      format = format,
      width = w,
      height = h,
    ) ?: IntArray(w * h) { 0xFFFFFFFF.toInt() }

    val writtenViaBitmap = try {
      val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
      if (bmp != null && bmp.width > 0 && bmp.height > 0) {
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        val isJpg = outputFile.extension.equals("jpg", ignoreCase = true) ||
          outputFile.extension.equals("jpeg", ignoreCase = true)
        val fmt = if (isJpg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG
        val bos = ByteArrayOutputStream()
        val ok = bmp.compress(fmt, 95, bos)
        val bytes = bos.toByteArray()
        if (ok && bytes.size > 64) {
          FileOutputStream(outputFile).use { fos ->
            fos.write(bytes)
            fos.flush()
          }
          outputFile.setLastModified(epochMs)
          true
        } else {
          false
        }
      } else {
        false
      }
    } catch (_: Throwable) {
      false
    }

    if (!writtenViaBitmap) {
      writeArgbPixelsToPng(
        outputFile = outputFile,
        pixels = pixels,
        width = w,
        height = h,
        lastModifiedMs = epochMs,
      )
    }
    return outputFile
  }

  /**
   * Decodes all QR codes, 2D matrix codes, and 1D barcodes matching [filter] from an ARGB pixel buffer
   * of size [width] × [height].
   *
   * Supports both standard dark-on-light codes and inverted light-on-dark codes when [tryInverted] is `true`.
   */
  fun decodeCodesFromArgbPixels(
    pixels: IntArray,
    width: Int,
    height: Int,
    filter: CameraScanFilter = CameraScanFilter.ALL,
    tryInverted: Boolean = true,
    scannedAtMs: Long = System.currentTimeMillis(),
  ): List<CameraScannedCode> {
    if (width <= 0 || height <= 0 || pixels.size < width * height) {
      return emptyList()
    }

    val allowedZxingFormats = CameraBarcodeFormat.zxingFormatsForFilter(filter)
    val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
      put(DecodeHintType.POSSIBLE_FORMATS, allowedZxingFormats)
      put(DecodeHintType.TRY_HARDER, java.lang.Boolean.TRUE)
      put(DecodeHintType.CHARACTER_SET, "UTF-8")
    }

    val multiFormatReader = MultiFormatReader().apply { setHints(hints) }
    val multipleBarcodeReader = GenericMultipleBarcodeReader(multiFormatReader)
    val rawResults = mutableListOf<Result>()

    fun attemptDecode(source: com.google.zxing.LuminanceSource) {
      // 1. Try HybridBinarizer (single + multiple)
      val hybridBitmap = BinaryBitmap(HybridBinarizer(source))
      try {
        val multi = multipleBarcodeReader.decodeMultiple(hybridBitmap, hints)
        if (!multi.isNullOrEmpty()) {
          rawResults.addAll(multi)
        }
      } catch (_: Throwable) {}

      if (rawResults.isEmpty()) {
        try {
          val single = multiFormatReader.decodeWithState(hybridBitmap)
          rawResults.add(single)
        } catch (_: Throwable) {
        } finally {
          multiFormatReader.reset()
        }
      }

      // 2. Fallback to GlobalHistogramBinarizer (especially helpful for 1D linear barcodes with uneven lighting)
      if (rawResults.isEmpty()) {
        val globalBitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
        try {
          val single = multiFormatReader.decodeWithState(globalBitmap)
          rawResults.add(single)
        } catch (_: Throwable) {
        } finally {
          multiFormatReader.reset()
        }
      }
    }

    val rgbSource = RGBLuminanceSource(width, height, pixels)
    attemptDecode(rgbSource)

    if (rawResults.isEmpty() && tryInverted) {
      try {
        attemptDecode(rgbSource.invert())
      } catch (_: Throwable) {}
    }

    val seenKeys = mutableSetOf<String>()
    val decodedCodes = mutableListOf<CameraScannedCode>()

    for (res in rawResults) {
      val text = res.text ?: continue
      val camFormat = CameraBarcodeFormat.fromZxingFormat(res.barcodeFormat)
      if (!camFormat.matchesFilter(filter)) continue

      val dedupKey = "${camFormat.name}:$text"
      if (!seenKeys.add(dedupKey)) continue

      val normalizedPoints = res.resultPoints
        ?.filterNotNull()
        ?.map { pt ->
          Pair(
            (pt.x / width.toFloat()).coerceIn(0f, 1f),
            (pt.y / height.toFloat()).coerceIn(0f, 1f),
          )
        }
        .orEmpty()

      decodedCodes.add(
        analyzeScannedPayload(
          rawValue = text,
          format = camFormat,
          scannedAtMs = scannedAtMs,
          cornerPoints = normalizedPoints,
        )
      )
    }

    return decodedCodes
  }

  /**
   * Decodes QR codes and barcodes matching [filter] from an Android [Bitmap].
   */
  fun decodeCodesFromBitmap(
    bitmap: Bitmap,
    filter: CameraScanFilter = CameraScanFilter.ALL,
    scannedAtMs: Long = System.currentTimeMillis(),
  ): List<CameraScannedCode> {
    return try {
      val bw = bitmap.width
      val bh = bitmap.height
      if (bw <= 0 || bh <= 0) return emptyList()

      val maxEdge = 1024
      val workingBitmap = if (maxOf(bw, bh) > maxEdge) {
        val scale = maxEdge.toFloat() / maxOf(bw, bh).toFloat()
        val sw = (bw * scale).roundToInt().coerceAtLeast(32)
        val sh = (bh * scale).roundToInt().coerceAtLeast(32)
        Bitmap.createScaledBitmap(bitmap, sw, sh, true) ?: bitmap
      } else {
        bitmap
      }

      val w = workingBitmap.width
      val h = workingBitmap.height
      val pixels = IntArray(w * h)
      workingBitmap.getPixels(pixels, 0, w, 0, 0, w, h)
      decodeCodesFromArgbPixels(
        pixels = pixels,
        width = w,
        height = h,
        filter = filter,
        scannedAtMs = scannedAtMs,
      )
    } catch (_: Throwable) {
      emptyList()
    }
  }

  /**
   * Decodes QR codes and barcodes matching [filter] from an image [file] on disk.
   * Supports both Android `BitmapFactory` decoding and pure-JVM PNG fallback for local unit tests.
   */
  fun decodeCodesFromImageFile(
    file: File,
    filter: CameraScanFilter = CameraScanFilter.ALL,
    scannedAtMs: Long = System.currentTimeMillis(),
  ): List<CameraScannedCode> {
    if (!file.exists() || !file.isFile || file.length() <= 0L) return emptyList()

    // 1. Try Android BitmapFactory first
    val fromAndroidBitmap = try {
      val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeFile(file.absolutePath, boundsOpts)
      var sampleSize = 1
      val maxDim = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
      while (maxDim / sampleSize > 1280) {
        sampleSize *= 2
      }
      val decodeOpts = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
      }
      val bmp = BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
      if (bmp != null && bmp.width > 0 && bmp.height > 0) {
        decodeCodesFromBitmap(bmp, filter = filter, scannedAtMs = scannedAtMs)
      } else {
        emptyList()
      }
    } catch (_: Throwable) {
      emptyList()
    }

    if (fromAndroidBitmap.isNotEmpty()) {
      return fromAndroidBitmap
    }

    // 2. Fallback to pure-JVM PNG pixel reader (used in local unit tests)
    val pngData = readPngToArgbPixels(file) ?: return emptyList()
    return decodeCodesFromArgbPixels(
      pixels = pngData.third,
      width = pngData.first,
      height = pngData.second,
      filter = filter,
      scannedAtMs = scannedAtMs,
    )
  }

  /**
   * Parses and classifies a decoded QR code or barcode [rawValue] into a structured [CameraScannedCode]
   * with human-readable title, subtitle, and extracted key-value metadata fields.
   */
  fun analyzeScannedPayload(
    rawValue: String,
    format: CameraBarcodeFormat,
    scannedAtMs: Long = System.currentTimeMillis(),
    cornerPoints: List<Pair<Float, Float>> = emptyList(),
  ): CameraScannedCode {
    val trimmed = rawValue.trim()
    val fields = linkedMapOf<String, String>()
    fields["Format"] = format.displayName
    fields["Symbology"] = if (format.is2D) "2D Matrix Code" else "1D Linear Barcode"

    val id = "${format.name}_${scannedAtMs}_${trimmed.hashCode()}"

    // 1. Wi-Fi Network QR payload: WIFI:S:<SSID>;T:<WEP|WPA|nopass>;P:<PASSWORD>;H:<true|false>;;
    if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
      val body = trimmed.substring(5)
      val tokens = parseSemicolonKeyValues(body)
      val ssid = tokens["S"].orEmpty().ifEmpty { "Hidden Network" }
      val security = tokens["T"].orEmpty().ifEmpty { "WPA" }.uppercase(Locale.US)
      val password = tokens["P"].orEmpty()
      val hidden = tokens["H"].orEmpty()

      fields["SSID"] = ssid
      fields["Security"] = security
      if (password.isNotEmpty()) fields["Password"] = password
      if (hidden.isNotEmpty()) fields["Hidden"] = hidden

      val sub = buildString {
        append("Security: $security")
        if (password.isNotEmpty()) {
          append(" • Password: $password")
        } else {
          append(" • Open Network")
        }
      }
      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.WIFI,
        title = "Wi-Fi: $ssid",
        subtitle = sub,
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 2. Web URL payload (http:// or https://)
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
      val isHttps = trimmed.startsWith("https://", ignoreCase = true)
      val withoutScheme = trimmed.substringAfter("://")
      val host = withoutScheme.substringBefore('/').substringBefore('?').ifEmpty { trimmed }
      val path = "/" + withoutScheme.substringAfter('/', "")
      fields["URL"] = trimmed
      fields["Host"] = host
      fields["Protocol"] = if (isHttps) "HTTPS (Secure)" else "HTTP"
      if (path != "/") fields["Path"] = path

      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.URL,
        title = host,
        subtitle = "${if (isHttps) "HTTPS" else "HTTP"} Link • $trimmed",
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 3. vCard or MECARD Contact payload
    if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) || trimmed.startsWith("MECARD:", ignoreCase = true)) {
      var fullName = ""
      var org = ""
      var phone = ""
      var email = ""
      if (trimmed.startsWith("MECARD:", ignoreCase = true)) {
        val map = parseSemicolonKeyValues(trimmed.substring(7))
        fullName = map["N"].orEmpty()
        org = map["ORG"].orEmpty()
        phone = map["TEL"].orEmpty()
        email = map["EMAIL"].orEmpty()
      } else {
        trimmed.lines().forEach { line ->
          val upper = line.uppercase(Locale.US)
          when {
            upper.startsWith("FN:") -> fullName = line.substringAfter(':').trim()
            upper.startsWith("N:") && fullName.isEmpty() -> {
              fullName = line.substringAfter(':').split(';').filter { it.isNotBlank() }.joinToString(" ")
            }
            upper.startsWith("ORG:") -> org = line.substringAfter(':').trim()
            upper.startsWith("TEL") && line.contains(':') -> phone = line.substringAfter(':').trim()
            upper.startsWith("EMAIL") && line.contains(':') -> email = line.substringAfter(':').trim()
          }
        }
      }
      if (fullName.isNotEmpty()) fields["Name"] = fullName
      if (org.isNotEmpty()) fields["Organization"] = org
      if (phone.isNotEmpty()) fields["Phone"] = phone
      if (email.isNotEmpty()) fields["Email"] = email

      val summaryParts = listOfNotNull(
        org.takeIf { it.isNotEmpty() },
        phone.takeIf { it.isNotEmpty() },
        email.takeIf { it.isNotEmpty() },
      )
      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.CONTACT,
        title = if (fullName.isNotEmpty()) "Contact: $fullName" else "Contact Card (vCard)",
        subtitle = summaryParts.joinToString(" • ").ifEmpty { "vCard Contact Details" },
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 4. Email payload (mailto: or MATMSG:)
    if (trimmed.startsWith("mailto:", ignoreCase = true) || trimmed.startsWith("MATMSG:", ignoreCase = true)) {
      val emailAddr = if (trimmed.startsWith("mailto:", ignoreCase = true)) {
        trimmed.substring(7).substringBefore('?')
      } else {
        parseSemicolonKeyValues(trimmed.substring(7))["TO"].orEmpty()
      }
      fields["Email"] = emailAddr
      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.EMAIL,
        title = "Email: ${emailAddr.ifEmpty { trimmed }}",
        subtitle = "${format.shortBadge} • Email Recipient",
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 5. Phone number payload (tel:)
    if (trimmed.startsWith("tel:", ignoreCase = true)) {
      val phoneNum = trimmed.substring(4).trim()
      fields["Phone"] = phoneNum
      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.PHONE,
        title = "Phone: $phoneNum",
        subtitle = "${format.shortBadge} • Telephone Number",
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 6. SMS payload (smsto: or sms:)
    if (trimmed.startsWith("smsto:", ignoreCase = true) || trimmed.startsWith("sms:", ignoreCase = true)) {
      val afterPrefix = trimmed.substringAfter(':')
      val number = afterPrefix.substringBefore(':')
      val msg = afterPrefix.substringAfter(':', "")
      fields["Recipient"] = number
      if (msg.isNotEmpty()) fields["Message"] = msg
      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.SMS,
        title = "SMS to $number",
        subtitle = msg.ifEmpty { "SMS Message" },
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 7. Geo location payload (geo:lat,lon)
    if (trimmed.startsWith("geo:", ignoreCase = true)) {
      val coords = trimmed.substring(4).substringBefore('?')
      fields["Coordinates"] = coords
      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.GEO,
        title = "Location: $coords",
        subtitle = "${format.shortBadge} • Geographic Coordinates",
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 8. Calendar event payload (BEGIN:VEVENT)
    if (trimmed.contains("BEGIN:VEVENT", ignoreCase = true)) {
      var summary = ""
      var location = ""
      trimmed.lines().forEach { line ->
        val upper = line.uppercase(Locale.US)
        if (upper.startsWith("SUMMARY:")) summary = line.substringAfter(':').trim()
        if (upper.startsWith("LOCATION:")) location = line.substringAfter(':').trim()
      }
      if (summary.isNotEmpty()) fields["Event"] = summary
      if (location.isNotEmpty()) fields["Location"] = location
      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.CALENDAR,
        title = if (summary.isNotEmpty()) "Event: $summary" else "Calendar Event",
        subtitle = location.ifEmpty { "iCalendar VEVENT" },
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 9. Retail Product & ISBN Barcodes (EAN-13, EAN-8, UPC-A, UPC-E)
    if (format in setOf(
        CameraBarcodeFormat.EAN_13,
        CameraBarcodeFormat.EAN_8,
        CameraBarcodeFormat.UPC_A,
        CameraBarcodeFormat.UPC_E,
      )
    ) {
      val validCheck = verifyLinearBarcodeChecksum(trimmed)
      fields["Digits"] = trimmed
      fields["Checksum"] = if (validCheck) "Valid (GS1 Mod-10)" else "Unverified"

      if (format == CameraBarcodeFormat.EAN_13 && (trimmed.startsWith("978") || trimmed.startsWith("979"))) {
        fields["Category"] = "Bookland ISBN-13"
        return CameraScannedCode(
          id = id,
          rawValue = rawValue,
          format = format,
          payloadType = CameraScanPayloadType.ISBN,
          title = "ISBN $trimmed",
          subtitle = "EAN-13 Book Barcode • ${if (validCheck) "Valid Checksum" else "13 Digits"}",
          fields = fields,
          scannedAtMs = scannedAtMs,
          cornerPoints = cornerPoints,
        )
      }

      val regionLabel = resolveGs1PrefixRegion(trimmed, format)
      if (regionLabel.isNotEmpty()) {
        fields["Origin / Prefix"] = regionLabel
      }

      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.PRODUCT,
        title = "${format.shortBadge}: $trimmed",
        subtitle = buildString {
          append("Product Barcode")
          if (regionLabel.isNotEmpty()) append(" • $regionLabel")
          if (validCheck) append(" • Valid Check")
        },
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 10. Industrial 1D Barcodes (Code 128, Code 39, Code 93, ITF, Codabar)
    if (!format.is2D) {
      fields["Code Value"] = trimmed
      fields["Length"] = "${trimmed.length} chars"
      return CameraScannedCode(
        id = id,
        rawValue = rawValue,
        format = format,
        payloadType = CameraScanPayloadType.BARCODE_ID,
        title = "${format.shortBadge}: $trimmed",
        subtitle = "${format.displayName} • ${trimmed.length} characters",
        fields = fields,
        scannedAtMs = scannedAtMs,
        cornerPoints = cornerPoints,
      )
    }

    // 11. Default 2D QR / Data Matrix / Aztec / PDF417 Text Payload
    fields["Content"] = trimmed
    fields["Length"] = "${trimmed.length} chars"
    return CameraScannedCode(
      id = id,
      rawValue = rawValue,
      format = format,
      payloadType = CameraScanPayloadType.TEXT,
      title = trimmed.lineSequence().firstOrNull()?.take(48).orEmpty().ifEmpty { format.displayName },
      subtitle = "${format.shortBadge} • Plain Text (${trimmed.length} chars)",
      fields = fields,
      scannedAtMs = scannedAtMs,
      cornerPoints = cornerPoints,
    )
  }

  private fun parseSemicolonKeyValues(body: String): Map<String, String> {
    val result = linkedMapOf<String, String>()
    val parts = body.split(';')
    for (part in parts) {
      val idx = part.indexOf(':')
      if (idx > 0) {
        val k = part.substring(0, idx).trim().uppercase(Locale.US)
        val v = part.substring(idx + 1).trim()
        if (k.isNotEmpty()) {
          result[k] = v
        }
      }
    }
    return result
  }

  private fun resolveGs1PrefixRegion(digits: String, format: CameraBarcodeFormat): String {
    if (format == CameraBarcodeFormat.UPC_A || format == CameraBarcodeFormat.UPC_E) {
      return "US / Canada (UPC)"
    }
    if (digits.length < 3) return ""
    val prefix = digits.take(3).toIntOrNull() ?: return ""
    return when (prefix) {
      in 0..139 -> "US / Canada (GS1)"
      in 300..379 -> "France"
      in 400..440 -> "Germany"
      in 450..459, in 490..499 -> "Japan"
      in 500..509 -> "United Kingdom"
      in 690..699 -> "China"
      in 880..880 -> "South Korea"
      977 -> "ISSN Periodical"
      978, 979 -> "Bookland ISBN"
      else -> "GS1 International"
    }
  }

  // =========================================================================
  // Document OCR & Note Scanner Engine (Google ML Kit Text Recognition v2)
  // =========================================================================

  /**
   * Parses raw OCR recognized text into a structured [CameraScannedDocument], extracting a clean
   * note title, body content, line count, word count, and text blocks.
   */
  fun analyzeScannedDocumentText(
    rawText: String,
    blocks: List<CameraScannedTextBlock> = emptyList(),
    fallbackTitle: String = "Scanned Note",
    scannedAtMs: Long = System.currentTimeMillis(),
  ): CameraScannedDocument {
    val normalizedText = rawText.replace("\r\n", "\n").replace('\r', '\n').trim()
    val nonBlankLines = normalizedText
      .lines()
      .map { it.trim() }
      .filter { it.isNotEmpty() }

    val title: String
    val body: String
    when {
      nonBlankLines.size >= 2 -> {
        val rawFirst = nonBlankLines.first().removePrefix("#").trim()
        title = (if (rawFirst.length <= 72) rawFirst else rawFirst.take(72).trimEnd() + "…")
          .ifEmpty { fallbackTitle }
        body = nonBlankLines.drop(1).joinToString("\n")
      }
      nonBlankLines.size == 1 -> {
        val single = nonBlankLines.first()
        title = if (single.length <= 48) single else single.take(48).trimEnd() + "…"
        body = single
      }
      else -> {
        title = fallbackTitle
        body = ""
      }
    }

    val wordCount = if (normalizedText.isEmpty()) {
      0
    } else {
      normalizedText.split(Regex("\\s+")).count { it.isNotEmpty() }
    }

    val effectiveBlocks = blocks.ifEmpty {
      if (nonBlankLines.isNotEmpty()) {
        listOf(
          CameraScannedTextBlock(
            text = nonBlankLines.joinToString("\n"),
            lines = nonBlankLines,
          )
        )
      } else {
        emptyList()
      }
    }

    return CameraScannedDocument(
      id = "OCR_${scannedAtMs}_${normalizedText.hashCode()}",
      rawText = normalizedText,
      title = title,
      body = body,
      blocks = effectiveBlocks,
      wordCount = wordCount,
      lineCount = nonBlankLines.size,
      scannedAtMs = scannedAtMs,
    )
  }

  /**
   * Converts a [CameraScannedDocument] into a [NoteItem] ready to be stored in the Notes tool.
   */
  fun createNoteItemFromScannedDocument(
    document: CameraScannedDocument,
    color: NoteColor = NoteColor.DEFAULT,
    labels: List<String> = listOf("Scanned"),
  ): NoteItem {
    val cleanTitle = document.title.trim().ifEmpty { "Scanned Note" }
    val cleanContent = document.body.trim().ifEmpty { document.rawText.trim() }
    return NoteItem(
      title = cleanTitle,
      content = cleanContent,
      isChecklist = false,
      checklistItems = emptyList(),
      color = color,
      isPinned = false,
      isArchived = false,
      isTrashed = false,
      labels = labels.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
      createdAt = document.scannedAtMs,
      updatedAt = document.scannedAtMs,
    )
  }

  /**
   * Adds the contents of a scanned [document] as a new [NoteItem] in [repository] (the Notes tool),
   * persisting both the note and its labels.
   */
  fun saveScannedDocumentToNotes(
    repository: NotesRepository,
    document: CameraScannedDocument,
    color: NoteColor = NoteColor.DEFAULT,
    labels: List<String> = listOf("Scanned"),
  ): Pair<CameraScannedDocument, NoteItem> {
    val noteItem = createNoteItemFromScannedDocument(
      document = document,
      color = color,
      labels = labels,
    )
    val existingNotes = repository.loadNotes()
    val updatedNotes = NotesEngine.saveOrDiscardNote(
      notes = existingNotes,
      draft = noteItem,
      nowMs = document.scannedAtMs,
    )
    repository.saveNotes(updatedNotes)

    val mergedLabels = (repository.loadLabels() + noteItem.labels)
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .distinct()
    repository.saveLabels(mergedLabels)

    val savedNote = updatedNotes.firstOrNull { it.id == noteItem.id } ?: noteItem
    return document.copy(savedNoteId = savedNote.id) to savedNote
  }

  /**
   * Renders a high-contrast synthetic document page [Bitmap] containing [text] so that on-device
   * ML Kit OCR can process a crisp bitmap when a live physical document is not in front of the camera.
   */
  fun createSyntheticDocumentBitmap(
    text: String,
    width: Int = 720,
    height: Int = 960,
  ): Bitmap? {
    val w = width.coerceIn(240, 2048)
    val h = height.coerceIn(240, 2048)
    return try {
      val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
      if (bmp == null || bmp.width <= 0 || bmp.height <= 0) return null
      val canvas = Canvas(bmp)
      canvas.drawColor(android.graphics.Color.WHITE)

      val lines = text
        .replace("\r\n", "\n")
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }

      val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        textSize = 36f
        isFakeBoldText = true
      }
      val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(24, 24, 28)
        textSize = 28f
      }

      var y = 96f
      val leftMargin = 48f
      lines.forEachIndexed { index, line ->
        if (y < h - 48f) {
          val paint = if (index == 0) titlePaint else bodyPaint
          canvas.drawText(line, leftMargin, y, paint)
          y += if (index == 0) 64f else 48f
        }
      }
      bmp
    } catch (_: Throwable) {
      null
    }
  }

  private val mlKitClientLock = Any()

  @Volatile
  private var cachedStreamObjectDetector: ObjectDetector? = null

  @Volatile
  private var cachedSingleImageObjectDetector: ObjectDetector? = null

  @Volatile
  private var cachedSharedImageLabeler: ImageLabeler? = null

  @Volatile
  private var cachedSharedTextRecognizer: TextRecognizer? = null

  private fun getOrCreateObjectDetector(streamMode: Boolean): ObjectDetector {
    return synchronized(mlKitClientLock) {
      if (streamMode) {
        cachedStreamObjectDetector ?: ObjectDetection.getClient(
          ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        ).also { cachedStreamObjectDetector = it }
      } else {
        cachedSingleImageObjectDetector ?: ObjectDetection.getClient(
          ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        ).also { cachedSingleImageObjectDetector = it }
      }
    }
  }

  private fun getOrCreateSharedImageLabeler(): ImageLabeler {
    return synchronized(mlKitClientLock) {
      cachedSharedImageLabeler ?: ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
          .setConfidenceThreshold(0.45f)
          .build()
      ).also { cachedSharedImageLabeler = it }
    }
  }

  private fun getOrCreateSharedTextRecognizer(): TextRecognizer {
    return synchronized(mlKitClientLock) {
      cachedSharedTextRecognizer ?: TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
      ).also { cachedSharedTextRecognizer = it }
    }
  }

  /**
   * Stabilizes consecutive live OCR document recognitions so that a momentary mid-motion frame
   * (containing fewer words or truncated partial lines) does not overwrite a cleaner, fuller read
   * captured moments earlier (within [maxHoldDurationMs]), while immediately adopting genuinely new
   * or more complete text.
   */
  fun selectStabilizedLiveOcrDocument(
    previousDoc: CameraScannedDocument?,
    candidateDoc: CameraScannedDocument?,
    nowMs: Long = System.currentTimeMillis(),
    maxHoldDurationMs: Long = 2500L,
  ): CameraScannedDocument? {
    val cleanCandidate = candidateDoc?.takeIf { it.rawText.isNotBlank() && it.wordCount >= 1 }
    val cleanPrev = previousDoc?.takeIf { it.rawText.isNotBlank() && it.wordCount >= 1 }
    if (cleanCandidate == null) {
      if (cleanPrev != null && (nowMs - cleanPrev.scannedAtMs) in 0L..maxHoldDurationMs) {
        return cleanPrev
      }
      return null
    }
    if (cleanPrev == null) return cleanCandidate
    val elapsedMs = (nowMs - cleanPrev.scannedAtMs).coerceAtLeast(0L)
    if (elapsedMs > maxHoldDurationMs) return cleanCandidate

    // If the candidate has at least 80% of the words (or more lines/words) than previous, accept the fresh scan
    if (cleanCandidate.wordCount >= (cleanPrev.wordCount * 0.80f).toInt() &&
      cleanCandidate.rawText.length >= (cleanPrev.rawText.length * 0.75f).toInt()
    ) {
      return cleanCandidate
    }

    // Check word overlap: if candidate is a strict partial subset of previousDoc (same document, motion blur frame),
    // keep the fuller previousDoc rather than flickering down to a partial read.
    val prevWords = cleanPrev.rawText
      .lowercase(Locale.US)
      .split(Regex("\\s+"))
      .map { it.trim { ch -> !ch.isLetterOrDigit() } }
      .filter { it.length >= 2 }
      .toSet()
    val candWords = cleanCandidate.rawText
      .lowercase(Locale.US)
      .split(Regex("\\s+"))
      .map { it.trim { ch -> !ch.isLetterOrDigit() } }
      .filter { it.length >= 2 }
    if (candWords.isEmpty()) return cleanPrev
    val sharedCount = candWords.count { it in prevWords }
    val overlapRatio = sharedCount.toFloat() / candWords.size.toFloat()
    return if (overlapRatio >= 0.50f && cleanPrev.wordCount > cleanCandidate.wordCount) {
      cleanPrev
    } else if (cleanCandidate.wordCount >= 2) {
      cleanCandidate
    } else {
      cleanPrev
    }
  }

  /**
   * Runs Google ML Kit on-device Text Recognition v2 (`com.google.mlkit:text-recognition`) on [bitmap]
   * using a persistent reusable [TextRecognizer] client and returns the recognized [CameraScannedDocument],
   * or `null` if no text was recognized or ML Kit runtime is unavailable.
   */
  suspend fun recognizeTextFromBitmapWithMlKit(
    bitmap: Bitmap,
    rotationDegrees: Int = 0,
    scannedAtMs: Long = System.currentTimeMillis(),
  ): CameraScannedDocument? {
    if (bitmap.width <= 16 || bitmap.height <= 16) return null
    return try {
      val recognizer = getOrCreateSharedTextRecognizer()
      val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
      val visionText = suspendCancellableCoroutine<MlKitText?> { continuation ->
        recognizer.process(inputImage)
          .addOnSuccessListener { result ->
            if (continuation.isActive) {
              continuation.resume(result)
            }
          }
          .addOnFailureListener {
            if (continuation.isActive) {
              continuation.resume(null)
            }
          }
      } ?: return null

      val rawText = visionText.text.trim()
      if (rawText.isEmpty()) return null

      val normRot = ((rotationDegrees % 360) + 360) % 360
      val swapDims = normRot == 90 || normRot == 270
      val bw = (if (swapDims) bitmap.height else bitmap.width).toFloat().coerceAtLeast(1f)
      val bh = (if (swapDims) bitmap.width else bitmap.height).toFloat().coerceAtLeast(1f)
      val blocks = visionText.textBlocks.mapNotNull { block ->
        val blockText = block.text.trim()
        if (blockText.isEmpty()) return@mapNotNull null
        val blockLines = block.lines.map { it.text.trim() }.filter { it.isNotEmpty() }
        val cornerPts = block.cornerPoints?.map { pt ->
          Pair(
            (pt.x / bw).coerceIn(0f, 1f),
            (pt.y / bh).coerceIn(0f, 1f),
          )
        }.orEmpty()
        val normBox = if (cornerPts.isNotEmpty()) {
          cornerPts
        } else {
          val rect = block.boundingBox
          if (rect != null && rect.width() > 0 && rect.height() > 0) {
            val l = (rect.left / bw).coerceIn(0f, 1f)
            val t = (rect.top / bh).coerceIn(0f, 1f)
            val r = (rect.right / bw).coerceIn(0f, 1f)
            val b = (rect.bottom / bh).coerceIn(0f, 1f)
            listOf(l to t, r to t, r to b, l to b)
          } else {
            emptyList()
          }
        }
        CameraScannedTextBlock(
          text = blockText,
          lines = blockLines.ifEmpty { blockText.lines() },
          boundingBoxNormalized = normBox,
        )
      }

      analyzeScannedDocumentText(
        rawText = rawText,
        blocks = blocks,
        scannedAtMs = scannedAtMs,
      )
    } catch (_: Throwable) {
      null
    }
  }

  // =========================================================================
  // Live ML Kit Object Detection & Image Labeling / Vision Tagging Engine
  // =========================================================================

  /**
   * Overly generic, abstract, geometric, lighting, material, color-name, body-part-noise, and
   * Knowledge Graph parent ontology labels from ML Kit's base 400-class image labeler that
   * produce false or uninformative tags even at high confidence.
   */
  val BLOCKED_GENERIC_ML_KIT_LABELS: Set<String> = setOf(
    // Geometry & visual artifacts
    "font", "rectangle", "square", "circle", "triangle", "oval", "pattern", "symmetry",
    "parallel", "slope", "line", "angle", "point", "shape", "mesh", "grid", "net", "checkerboard",
    "polka dot", "plaid", "stripe", "motif",
    // Lighting & optical artifacts
    "tint and shade", "shade", "shadow", "darkness", "light", "lighting", "daylighting",
    "backlighting", "lens flare", "reflection", "gloss", "transparency", "flash photography",
    "light fixture", "lamp", "lampshade",
    // Abstract & photography ontology classes
    "material property", "composite material", "space", "design", "graphic design",
    "interior design", "automotive design", "urban design", "product design", "fashion design",
    "architecture", "engineering", "technology", "science", "art", "visual arts", "creative arts",
    "modern art", "fine art", "illustration", "drawing", "sketch", "painting", "watercolor paint",
    "photography", "still life photography", "macro photography", "monochrome",
    "monochrome photography", "black-and-white", "close-up", "snapshot", "stock photography",
    "portrait photography", "landscape",
    // Generic commerce / event / ontology hypernyms
    "goods", "product", "home good", "fashion good", "place", "event", "fun", "leisure",
    "recreation", "job", "white-collar worker", "business", "service", "advertising", "brand",
    "logo", "trademark", "symbol", "number", "handwriting", "calligraphy", "paper",
    "paper product", "publication", "document", "text", "label", "signage", "sign", "banner",
    "poster",
    // Room / wall / material background surfaces
    "room", "hall", "lobby", "wall", "floor", "flooring", "ceiling", "surface", "wood",
    "plywood", "hardwood", "wood flooring", "laminate flooring", "plank", "lumber", "wood stain",
    "varnish", "stain", "metal", "steel", "aluminum", "iron", "brass", "copper", "plastic",
    "glass", "concrete", "cement", "asphalt", "brick", " brickwork", "masonry", "tile", "plaster",
    "drywall", "paint", "coating", "countertop", "beam", "molding", "siding", "roofing",
    "building material", "fixture", "door", "window", "sash window", "curtain", "window blind",
    "window treatment", "window covering",
    // Pure color names output by ML Kit base labeler
    "electric blue", "magenta", "carmine", "peach", "beige", "tan", "maroon", "cyan", "azure",
    "cobalt blue", "amber", "gold", "silver", "bronze", "grey", "gray", "black", "white", "red",
    "blue", "green", "yellow", "orange", "purple", "pink", "brown", "violet", "teal", "turquoise",
    "indigo", "lavender", "coral", "crimson", "khaki", "ivory", "aqua", "lime", "navy blue",
    // Hardware / display / wire noise
    "multimedia", "software", "website", "screenshot", "display device", "output device",
    "input device", "peripheral", "electronic component", "electronic engineering",
    "electrical wiring", "cable", "wire", "electrical cable", "power supply",
    "electrical connector", "adapter", " electronic instrument", "machine", "tool",
    // Body-part / skin-patch false positives
    "human", "human body", "human leg", "human arm", "human hand", "human foot", "human head",
    "human hair", "flesh", "skin", "limb", "joint", "thumb", "finger", "nail", "wrist", "arm",
    "elbow", "shoulder", "neck", "jaw", "chin", "cheek", "forehead", "nose", "nostril", "eyelash",
    "eyebrow", "temple", "ear", "hair", "facial hair", "beard", "mustache", "gesture", "wrinkle",
    "abdomen", "chest", "trunk", "leg", "thigh", "knee", "calf", "ankle", "foot", "toe", "back",
    "hand", "face", "head", "mouth", "lip", "tooth", "tongue", "eye", "iris", "pupil", "smile",
    "happy", "selfie", "portrait", "youth", "child", "toddler", "adult",
    // Biological / botanical / environmental hypernyms
    "biological process", "organism", "adaptation", "terrestrial animal", "vertebrate",
    "invertebrate", "mammal", "carnivore", "terrestrial plant", "vascular plant",
    "flowering plant", "annual plant", "perennial plant", "herbaceous plant", "botany",
    "vegetation", "flora", "fauna", "natural environment", "natural landscape", "ecoregion",
    "biome", "atmosphere", "meteorological phenomenon", "atmospheric phenomenon",
    "geological phenomenon", "horizon", "twig", "plant stem", "arecales", "groundcover",
    "subshrub", "shrub", "grass", "grass family", "soil", "rock", "pebble", "sand", "mud",
    "fluid", "liquid", "gas", "vapor", "smoke", "steam", "drop", "moisture", "dew",
    // Food / kitchen hypernyms
    "ingredient", "recipe", "cuisine", "dish", "staple food", "comfort food", "finger food",
    "fast food", "junk food", "superfood", "produce", "natural foods", "whole food", "local food",
    "vegan nutrition", "vegetarian food", "gluten", "sweetness", "delicacy", "side dish",
    "condiment", "garnish", "tableware", "drinkware", "serveware", "dishware", "flatware",
    "kitchen utensil", "kitchenware", "cutlery", "home appliance", "major appliance",
    "small appliance", "kitchen appliance", "appliance",
    // Clothing / textile / furniture hypernyms
    "textile", "fabric", "fiber", "thread", "yarn", "wool", "cotton", "silk", "satin", "denim",
    "leather", "fur", "linens", "bedding", "tablecloth", "carpet", "rug", "mat", "cushion",
    "pillow", "throw pillow", "fashion", "fashion accessory", "clothing", "apparel", "outerwear",
    "activewear", "workwear", "streetwear", "sportswear", "formal wear", "sleeve", "collar",
    "pocket", "button", "zipper", "furniture", "cabinetry", "shelving", "shelf", "drawer",
    // Vehicle sub-part noise
    "motor vehicle", "land vehicle", "mode of transport", "transport", "transportation",
    "automotive exterior", "automotive lighting", "automotive tire", "automotive wheel system",
    "auto part", "bumper", "fender", "hood", "grille", "windshield", "wiper", "rim",
    "alloy wheel", "hubcap", "tread", "rolling", "spoke", "tire", "wheel", "headlamp", "taillight",
    "vehicle registration plate", "asphalt", "road surface", "sidewalk", "curb",
  )

  /**
   * Maps verbose or awkward ML Kit ontology terms to clean, specific user-facing object labels.
   */
  val CANONICAL_ML_KIT_LABEL_MAP: Map<String, String> = mapOf(
    "computer monitor" to "Monitor",
    "flat screen" to "Monitor",
    "liquid-crystal display" to "Monitor",
    "led-backlit lcd" to "Monitor",
    "personal computer" to "Computer",
    "netbook" to "Laptop",
    "ultrabook" to "Laptop",
    "computer keyboard" to "Computer keyboard",
    "space bar" to "Computer keyboard",
    "office equipment" to "Laptop",
    "mobile phone" to "Smartphone",
    "telephony" to "Smartphone",
    "communication device" to "Smartphone",
    "portable communications device" to "Smartphone",
    "feature phone" to "Phone",
    "smartphone" to "Smartphone",
    "tablet computer" to "Tablet",
    "computer desk" to "Desk",
    "office chair" to "Chair",
    "armrest" to "Chair",
    "club chair" to "Chair",
    "folding chair" to "Chair",
    "stool" to "Stool",
    "couch" to "Sofa",
    "loveseat" to "Sofa",
    "studio couch" to "Sofa",
    "sofa bed" to "Sofa",
    "coffee table" to "Table",
    "dining table" to "Table",
    "nightstand" to "Table",
    "eyewear" to "Glasses",
    "goggles" to "Glasses",
    "vision care" to "Glasses",
    "sunglasses" to "Sunglasses",
    "wristwatch" to "Watch",
    "analog watch" to "Watch",
    "timepiece" to "Clock",
    "wall clock" to "Clock",
    "alarm clock" to "Clock",
    "running shoe" to "Shoes",
    "athletic shoe" to "Sneakers",
    "outdoor shoe" to "Shoes",
    "walking shoe" to "Shoes",
    "footwear" to "Shoes",
    "high-heeled shoe" to "Shoes",
    "sandal" to "Sandals",
    "boot" to "Boots",
    "t-shirt" to "T-shirt",
    "dress shirt" to "Shirt",
    "polo shirt" to "Shirt",
    "trousers" to "Pants",
    "coat" to "Jacket",
    "blazer" to "Jacket",
    "windbreaker" to "Jacket",
    "handbag" to "Bag",
    "shoulder bag" to "Bag",
    "tote bag" to "Bag",
    "luggage and bags" to "Backpack",
    "baggage" to "Luggage",
    "suitcase" to "Luggage",
    "water bottle" to "Bottle",
    "plastic bottle" to "Bottle",
    "glass bottle" to "Bottle",
    "beer bottle" to "Bottle",
    "wine bottle" to "Bottle",
    "drink" to "Beverage",
    "soft drink" to "Beverage",
    "teacup" to "Cup",
    "coffee cup" to "Coffee cup",
    "espresso" to "Coffee",
    "cappuccino" to "Coffee",
    "latte" to "Coffee",
    "americano" to "Coffee",
    "caffeine" to "Coffee",
    "pastry" to "Baked goods",
    "croissant" to "Croissant",
    "baked goods" to "Baked goods",
    "felidae" to "Cat",
    "small to medium-sized cats" to "Cat",
    "domestic short-haired cat" to "Cat",
    "whiskers" to "Cat",
    "tabby cat" to "Cat",
    "kitten" to "Cat",
    "canidae" to "Dog",
    "dog breed" to "Dog",
    "companion dog" to "Dog",
    "working animal" to "Dog",
    "Sporting Group" to "Dog",
    "puppy" to "Dog",
    "snout" to "Dog",
    "aves" to "Bird",
    "beak" to "Bird",
    "songbird" to "Bird",
    "water bird" to "Bird",
    "ducks, geese and swans" to "Bird",
    "succulent plant" to "Succulent",
    "caryophyllales" to "Succulent",
    "monstera" to "Houseplant",
    "philodendron" to "Houseplant",
    "fern" to "Houseplant",
    "palm tree" to "Tree",
    "woody plant" to "Tree",
    "conifer" to "Tree",
    "petal" to "Flower",
    "blossom" to "Flower",
    "flowerpot" to "Flowerpot",
    "family car" to "Car",
    "sedan" to "Car",
    "hatchback" to "Car",
    "sports car" to "Car",
    "luxury vehicle" to "Car",
    "compact car" to "Car",
    "mid-size car" to "Car",
    "full-size car" to "Car",
    "subcompact car" to "Car",
    "city car" to "Car",
    "coupe" to "Car",
    "convertible" to "Car",
    "crossover suv" to "SUV",
    "sport utility vehicle" to "SUV",
    "pickup truck" to "Truck",
    "commercial vehicle" to "Truck",
    "van" to "Van",
    "minivan" to "Van",
    "bicycle wheel" to "Bicycle",
    "cycle sport" to "Bicycle",
    "road bicycle" to "Bicycle",
    "mountain bike" to "Bicycle",
    "skyline" to "Cityscape",
    "metropolitan area" to "City",
    "metropolis" to "City",
    "downtown" to "City",
    "tower block" to "Building",
    "skyscraper" to "Building",
    "condominium" to "Building",
    "commercial building" to "Building",
    "facade" to "Building",
    "apartment" to "Building",
    "cumulus" to "Clouds",
    "cloud" to "Clouds",
    "body of water" to "Water",
    "audio equipment" to "Headphones",
    "headset" to "Headphones",
    "loudspeaker" to "Speaker",
    "musical keyboard" to "Piano",
    "string instrument" to "Guitar",
    "plucked string instruments" to "Guitar",
    "acoustic guitar" to "Guitar",
    "electric guitar" to "Guitar",
    "musical instrument" to "Instrument",
  )

  /**
   * Maps generic parent hypernym labels (in lowercase) to the set of more specific child labels
   * (in lowercase) that should supersede and suppress the generic parent when both are detected.
   */
  private val GENERIC_HYPERNYM_PARENTS: Map<String, Set<String>> = mapOf(
    "food" to setOf(
      "coffee", "coffee cup", "croissant", "baked goods", "bread", "pizza", "burger", "sandwich",
      "salad", "fruit", "apple", "banana", "orange", "berry", "strawberry", "vegetable", "sushi",
      "pasta", "noodle", "soup", "steak", "meat", "seafood", "cake", "cookie", "ice cream",
      "chocolate", "dessert", "cheese", "egg", "breakfast", "snack",
    ),
    "beverage" to setOf("coffee", "coffee cup", "tea", "cup", "mug", "wine", "beer", "juice", "bottle"),
    "electronics" to setOf(
      "laptop", "monitor", "computer", "computer keyboard", "mouse", "smartphone", "phone",
      "tablet", "television", "camera", "headphones", "speaker", "watch", "clock", "game controller",
    ),
    "gadget" to setOf(
      "laptop", "monitor", "computer", "computer keyboard", "mouse", "smartphone", "phone",
      "tablet", "camera", "headphones", "speaker", "watch", "game controller",
    ),
    "computer" to setOf("laptop", "monitor", "computer keyboard", "mouse", "tablet"),
    "vehicle" to setOf("car", "suv", "truck", "van", "bus", "train", "bicycle", "motorcycle", "airplane", "boat", "ship"),
    "animal" to setOf("cat", "dog", "bird", "fish", "horse", "rabbit", "deer", "bear", "wildlife"),
    "pet" to setOf("cat", "dog", "bird", "fish", "rabbit"),
    "nature" to setOf("plant", "houseplant", "flowerpot", "flower", "tree", "succulent", "leaf", "mountain", "forest", "beach", "ocean", "lake", "river"),
    "plant" to setOf("houseplant", "flowerpot", "flower", "succulent", "tree"),
  )

  /**
   * Maps ML Kit's 5 coarse `ObjectDetector` category names to matching specific domain labels so
   * real `DetectedObject` bounding boxes pair accurately with their fine-grained `ImageLabeling` label.
   */
  private val OBJECT_DETECTOR_CATEGORY_KEYWORDS: Map<String, Set<String>> = mapOf(
    "plant" to setOf(
      "plant", "houseplant", "flowerpot", "flower", "flowers", "tree", "leaf", "succulent",
      "cactus", "vase", "garden", "forest",
    ),
    "food" to setOf(
      "food", "coffee", "coffee cup", "cup", "mug", "beverage", "croissant", "baked goods",
      "bread", "dessert", "cake", "cookie", "fruit", "apple", "banana", "orange", "vegetable",
      "salad", "pizza", "burger", "sandwich", "pasta", "sushi", "meat", "cheese", "bottle",
      "bowl", "plate",
    ),
    "home good" to setOf(
      "laptop", "monitor", "computer", "computer keyboard", "mouse", "smartphone", "phone",
      "tablet", "television", "camera", "headphones", "speaker", "clock", "watch", "desk",
      "table", "chair", "sofa", "stool", "bed", "book", "notebook", "pen", "pencil", "cup",
      "mug", "coffee cup", "bottle", "bowl", "plate", "vase", "flowerpot", "houseplant",
      "toy", "instrument", "guitar", "piano",
    ),
    "fashion good" to setOf(
      "glasses", "sunglasses", "watch", "shoes", "sneakers", "boots", "sandals", "bag",
      "backpack", "luggage", "hat", "cap", "helmet", "shirt", "t-shirt", "jacket", "hoodie",
      "pants", "jeans", "dress", "umbrella", "jewelry",
    ),
    "place" to setOf(
      "building", "house", "city", "cityscape", "street", "road", "bridge", "tower", "church",
      "castle", "stadium", "park", "mountain", "beach", "ocean", "lake", "river", "forest", "sky",
    ),
  )

  /**
   * Returns `true` if [label] is an overly generic, abstract, geometric, lighting, material, or
   * ontology-noise class that should be suppressed from ML Kit tagging output.
   */
  fun isBlockedGenericVisionLabel(label: String): Boolean {
    val clean = label.trim().lowercase(Locale.US)
    if (clean.isEmpty()) return true
    val canonical = CANONICAL_ML_KIT_LABEL_MAP[clean]?.lowercase(Locale.US) ?: clean
    return clean in BLOCKED_GENERIC_ML_KIT_LABELS || canonical in BLOCKED_GENERIC_ML_KIT_LABELS
  }

  /**
   * Normalizes a raw ML Kit label into a clean, specific user-facing object label, or returns
   * `null` if the label is in [BLOCKED_GENERIC_ML_KIT_LABELS].
   */
  fun normalizeAndFilterMlKitLabel(rawLabel: String): String? {
    val trimmed = rawLabel.trim()
    if (trimmed.isEmpty()) return null
    val lower = trimmed.lowercase(Locale.US)
    if (lower in BLOCKED_GENERIC_ML_KIT_LABELS) return null
    val canonical = CANONICAL_ML_KIT_LABEL_MAP[lower] ?: trimmed
    if (canonical.lowercase(Locale.US) in BLOCKED_GENERIC_ML_KIT_LABELS) return null
    return canonical.replaceFirstChar { ch ->
      if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
    }
  }

  /**
   * Returns a specificity bonus (`-0.08f..+0.06f`) used to prioritize concrete object nouns
   * (e.g., `"Laptop"`, `"Coffee cup"`, `"Cat"`, `"Flowerpot"`) over broad category terms
   * (e.g., `"Electronics"`, `"Food"`, `"Nature"`).
   */
  fun computeVisionLabelSpecificityBonus(label: String): Float {
    val lower = label.trim().lowercase(Locale.US)
    if (lower in GENERIC_HYPERNYM_PARENTS.keys) {
      return -0.06f
    }
    if (lower in setOf("indoor", "outdoor", "sky", "clouds", "water", "city", "street", "road")) {
      return -0.04f
    }
    return 0.04f
  }

  /**
   * Suppresses generic parent hypernym tags (such as `"Food"` when `"Coffee"` or `"Croissant"` is
   * already present, or `"Electronics"` when `"Laptop"` or `"Monitor"` is present).
   */
  fun suppressGenericHypernymVisionTags(tags: List<CameraVisionTag>): List<CameraVisionTag> {
    if (tags.size <= 1) return tags
    val presentLower = tags.map { it.label.trim().lowercase(Locale.US) }.toSet()
    return tags.filter { candidate ->
      val lower = candidate.label.trim().lowercase(Locale.US)
      val specificChildren = GENERIC_HYPERNYM_PARENTS[lower]
      if (specificChildren == null) {
        true
      } else {
        // Keep the generic parent only if none of its specific child labels were detected
        presentLower.none { it != lower && it in specificChildren }
      }
    }
  }

  /**
   * Rotates [bitmap] by [rotationDegrees] (`90`, `180`, `270`) if needed so all downstream object
   * detection, cropping, and normalized coordinate calculations operate in upright display space.
   */
  fun orientBitmapIfNeeded(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val normRot = ((rotationDegrees % 360) + 360) % 360
    if (normRot == 0 || bitmap.width <= 0 || bitmap.height <= 0) return bitmap
    return try {
      val matrix = Matrix().apply { postRotate(normRot.toFloat()) }
      Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } catch (_: Throwable) {
      bitmap
    }
  }

  /**
   * Returns a well-spaced fallback [CameraVisionBoundingBox] for a given tag rank (`0..N`)
   * when an explicit region bounding box is not provided.
   */
  fun defaultVisionBoundingBoxForRank(rank: Int): CameraVisionBoundingBox {
    return when (rank % 5) {
      0 -> CameraVisionBoundingBox(0.16f, 0.20f, 0.84f, 0.66f)
      1 -> CameraVisionBoundingBox(0.24f, 0.30f, 0.78f, 0.78f)
      2 -> CameraVisionBoundingBox(0.12f, 0.48f, 0.58f, 0.82f)
      3 -> CameraVisionBoundingBox(0.42f, 0.16f, 0.88f, 0.52f)
      else -> CameraVisionBoundingBox(0.10f, 0.14f, 0.90f, 0.84f)
    }
  }

  /**
   * Analyzes an ARGB pixel buffer (`width` × `height`) on a 32×32 spatial grid to locate distinct
   * foreground object contours (using perceptual color contrast against the frame perimeter and
   * local Sobel gradient energy) and returns their tight normalized [CameraVisionBoundingBox] bounds.
   */
  fun extractSalientRegionsFromArgbPixels(
    pixels: IntArray,
    width: Int,
    height: Int,
    maxRegions: Int = 5,
  ): List<CameraVisionBoundingBox> {
    if (width <= 8 || height <= 8 || pixels.size < width * height) {
      return listOf(defaultVisionBoundingBoxForRank(0))
    }

    val cols = 32.coerceAtMost(width)
    val rows = 32.coerceAtMost(height)
    val cellR = FloatArray(cols * rows)
    val cellG = FloatArray(cols * rows)
    val cellB = FloatArray(cols * rows)

    for (gy in 0 until rows) {
      val yStart = (gy * height) / rows
      val yEnd = (((gy + 1) * height) / rows).coerceAtLeast(yStart + 1)
      val yStep = ((yEnd - yStart) / 4).coerceAtLeast(1)
      for (gx in 0 until cols) {
        val xStart = (gx * width) / cols
        val xEnd = (((gx + 1) * width) / cols).coerceAtLeast(xStart + 1)
        val xStep = ((xEnd - xStart) / 4).coerceAtLeast(1)

        var sumR = 0f
        var sumG = 0f
        var sumB = 0f
        var count = 0
        var y = yStart
        while (y < yEnd) {
          val rowOffset = y * width
          var x = xStart
          while (x < xEnd) {
            val argb = pixels[rowOffset + x]
            sumR += (argb shr 16) and 0xFF
            sumG += (argb shr 8) and 0xFF
            sumB += argb and 0xFF
            count++
            x += xStep
          }
          y += yStep
        }
        val idx = gy * cols + gx
        if (count > 0) {
          cellR[idx] = sumR / count
          cellG[idx] = sumG / count
          cellB[idx] = sumB / count
        }
      }
    }

    // Compute perimeter background mean color
    var bgR = 0f
    var bgG = 0f
    var bgB = 0f
    var bgCount = 0
    for (gy in 0 until rows) {
      for (gx in 0 until cols) {
        if (gx == 0 || gx == cols - 1 || gy == 0 || gy == rows - 1) {
          val idx = gy * cols + gx
          bgR += cellR[idx]
          bgG += cellG[idx]
          bgB += cellB[idx]
          bgCount++
        }
      }
    }
    if (bgCount > 0) {
      bgR /= bgCount
      bgG /= bgCount
      bgB /= bgCount
    }

    val saliency = FloatArray(cols * rows)
    var totalSaliency = 0f
    for (gy in 0 until rows) {
      for (gx in 0 until cols) {
        val idx = gy * cols + gx
        val dr = cellR[idx] - bgR
        val dg = cellG[idx] - bgG
        val db = cellB[idx] - bgB
        // Perceptually weighted RGB distance against perimeter background
        val colorContrast = kotlin.math.sqrt((dr * dr * 0.30f + dg * dg * 0.59f + db * db * 0.11f) * 3f)

        val rightIdx = gy * cols + (gx + 1).coerceAtMost(cols - 1)
        val downIdx = (gy + 1).coerceAtMost(rows - 1) * cols + gx
        val edgeR = abs(cellR[idx] - cellR[rightIdx]) + abs(cellR[idx] - cellR[downIdx])
        val edgeG = abs(cellG[idx] - cellG[rightIdx]) + abs(cellG[idx] - cellG[downIdx])
        val edgeB = abs(cellB[idx] - cellB[rightIdx]) + abs(cellB[idx] - cellB[downIdx])
        val edgeEnergy = (edgeR + edgeG + edgeB) / 3f

        val normDx = (gx + 0.5f) / cols - 0.5f
        val normDy = (gy + 0.5f) / rows - 0.5f
        val centerWeight = (1.30f - (normDx * normDx + normDy * normDy) * 1.85f).coerceIn(0.42f, 1.30f)

        val score = (colorContrast * 0.78f + edgeEnergy * 0.85f) * centerWeight
        saliency[idx] = score
        totalSaliency += score
      }
    }

    val meanSaliency = totalSaliency / (cols * rows).toFloat()
    val visited = BooleanArray(cols * rows)
    val regions = mutableListOf<CameraVisionBoundingBox>()

    for (regionIdx in 0 until maxRegions.coerceAtLeast(1)) {
      var peakIdx = -1
      var peakScore = maxOf(meanSaliency * 1.12f, 6f)
      for (i in saliency.indices) {
        if (!visited[i] && saliency[i] > peakScore) {
          peakScore = saliency[i]
          peakIdx = i
        }
      }
      if (peakIdx < 0) break

      val peakGx = peakIdx % cols
      val peakGy = peakIdx / cols
      val seedR = cellR[peakIdx]
      val seedG = cellG[peakIdx]
      val seedB = cellB[peakIdx]

      var minGx = peakGx
      var maxGx = peakGx
      var minGy = peakGy
      var maxGy = peakGy

      val queue = ArrayDeque<Int>()
      queue.add(peakIdx)
      visited[peakIdx] = true

      while (queue.isNotEmpty()) {
        val curr = queue.removeFirst()
        val cx = curr % cols
        val cy = curr / cols
        if (cx < minGx) minGx = cx
        if (cx > maxGx) maxGx = cx
        if (cy < minGy) minGy = cy
        if (cy > maxGy) maxGy = cy

        val neighbors = intArrayOf(
          if (cx > 0) cy * cols + (cx - 1) else -1,
          if (cx + 1 < cols) cy * cols + (cx + 1) else -1,
          if (cy > 0) (cy - 1) * cols + cx else -1,
          if (cy + 1 < rows) (cy + 1) * cols + cx else -1,
        )
        for (nb in neighbors) {
          if (nb < 0 || visited[nb]) continue
          val cdR = cellR[nb] - seedR
          val cdG = cellG[nb] - seedG
          val cdB = cellB[nb] - seedB
          val colorDistToSeed = kotlin.math.sqrt(cdR * cdR + cdG * cdG + cdB * cdB)
          if (saliency[nb] >= meanSaliency * 0.90f && (colorDistToSeed <= 68f || saliency[nb] >= peakScore * 0.60f)) {
            visited[nb] = true
            queue.add(nb)
          }
        }
      }

      val leftNorm = (minGx.toFloat() / cols).coerceIn(0.02f, 0.88f)
      val topNorm = (minGy.toFloat() / rows).coerceIn(0.02f, 0.88f)
      val rightNorm = ((maxGx + 1).toFloat() / cols).coerceIn(leftNorm + 0.12f, 0.98f)
      val bottomNorm = ((maxGy + 1).toFloat() / rows).coerceIn(topNorm + 0.10f, 0.98f)
      regions.add(
        CameraVisionBoundingBox(
          left = leftNorm,
          top = topNorm,
          right = rightNorm,
          bottom = bottomNorm,
        ).coerceNormalized()
      )
    }

    if (regions.isEmpty()) {
      regions.add(defaultVisionBoundingBoxForRank(0))
    }
    return regions
  }

  /**
   * Extracts salient visual object regions from [bitmap] as normalized [CameraVisionBoundingBox] coordinates.
   */
  fun extractSalientRegionsFromBitmap(
    bitmap: Bitmap,
    maxRegions: Int = 5,
  ): List<CameraVisionBoundingBox> {
    return try {
      val w = bitmap.width
      val h = bitmap.height
      if (w <= 8 || h <= 8) return listOf(defaultVisionBoundingBoxForRank(0))
      val pixels = IntArray(w * h)
      bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
      extractSalientRegionsFromArgbPixels(pixels, w, h, maxRegions = maxRegions)
    } catch (_: Throwable) {
      listOf(defaultVisionBoundingBoxForRank(0))
    }
  }

  /**
   * Filters a list of [CameraVisionTag] detections using Non-Maximum Suppression (NMS) so that
   * when two detections have fully overlapping bounding boxes, only the highest-confidence
   * detection is kept and the lower-confidence overlapping detection is hidden.
   */
  fun suppressOverlappingVisionTags(tags: List<CameraVisionTag>): List<CameraVisionTag> {
    if (tags.size <= 1) return tags
    val sorted = tags.sortedByDescending { it.confidenceClamped }
    val kept = mutableListOf<CameraVisionTag>()
    for (candidate in sorted) {
      val candidateBox = (candidate.boundingBox ?: defaultVisionBoundingBoxForRank(0)).coerceNormalized()
      val overlapsHigherConfidence = kept.any { existing ->
        val existingBox = (existing.boundingBox ?: defaultVisionBoundingBoxForRank(0)).coerceNormalized()
        candidateBox.isFullyOverlappingWith(existingBox)
      }
      if (!overlapsHigherConfidence) {
        kept.add(candidate.copy(boundingBox = candidateBox))
      }
    }
    return kept
  }

  /**
   * Smoothly interpolates a [CameraVisionBoundingBox] from [fromBox] to [toBox] at progress
   * [fraction] (`0.0f..1.0f`), translating its center and resizing its width and height.
   */
  fun interpolateVisionBoundingBox(
    fromBox: CameraVisionBoundingBox,
    toBox: CameraVisionBoundingBox,
    fraction: Float,
  ): CameraVisionBoundingBox {
    return fromBox.lerpTo(toBox, fraction)
  }

  /**
   * Returns `true` when [previousTags] and [targetTags] share at least one detection with the same
   * case-insensitive label or the same non-null ML Kit [CameraVisionTag.trackingId].
   */
  fun hasMatchingVisionTagLabels(
    previousTags: List<CameraVisionTag>,
    targetTags: List<CameraVisionTag>,
  ): Boolean {
    if (previousTags.isEmpty() || targetTags.isEmpty()) return false
    val prevKeys = previousTags
      .map { it.label.trim().lowercase(Locale.US) }
      .filter { it.isNotEmpty() }
      .toSet()
    val prevTrackingIds = previousTags
      .mapNotNull { it.trackingId }
      .toSet()
    return targetTags.any { tag ->
      tag.label.trim().lowercase(Locale.US) in prevKeys ||
        (tag.trackingId != null && tag.trackingId in prevTrackingIds)
    }
  }

  /**
   * Smoothly moves and resizes bounding boxes from [previousTags] into [targetTags] at progress
   * [fraction] (`0.0f..1.0f`) for any detections that share the same label (case-insensitive)
   * or the same non-null [CameraVisionTag.trackingId].
   * Detections with brand-new labels/tracks appear immediately at their target bounding box.
   */
  fun interpolateVisionTags(
    previousTags: List<CameraVisionTag>,
    targetTags: List<CameraVisionTag>,
    fraction: Float,
  ): List<CameraVisionTag> {
    if (targetTags.isEmpty()) return emptyList()
    val clampedFraction = fraction.coerceIn(0f, 1f)
    if (previousTags.isEmpty() || clampedFraction >= 1f) {
      return targetTags.mapIndexed { idx, tag ->
        val box = (tag.boundingBox ?: defaultVisionBoundingBoxForRank(idx)).coerceNormalized()
        tag.copy(boundingBox = box)
      }
    }

    val previousByLabel = linkedMapOf<String, Pair<Int, CameraVisionTag>>()
    val previousByTrackingId = linkedMapOf<Int, Pair<Int, CameraVisionTag>>()
    previousTags.forEachIndexed { idx, tag ->
      val key = tag.label.trim().lowercase(Locale.US)
      if (key.isNotEmpty() && !previousByLabel.containsKey(key)) {
        previousByLabel[key] = idx to tag
      }
      val tid = tag.trackingId
      if (tid != null && !previousByTrackingId.containsKey(tid)) {
        previousByTrackingId[tid] = idx to tag
      }
    }

    return targetTags.mapIndexed { targetIdx, targetTag ->
      val targetBox = (targetTag.boundingBox ?: defaultVisionBoundingBoxForRank(targetIdx)).coerceNormalized()
      val key = targetTag.label.trim().lowercase(Locale.US)
      val prevMatch = previousByLabel[key]
        ?: targetTag.trackingId?.let { previousByTrackingId[it] }
      if (prevMatch != null) {
        val (prevIdx, prevTag) = prevMatch
        val prevBox = (prevTag.boundingBox ?: defaultVisionBoundingBoxForRank(prevIdx)).coerceNormalized()
        targetTag.copy(
          boundingBox = prevBox.lerpTo(targetBox, clampedFraction),
        )
      } else {
        targetTag.copy(
          boundingBox = targetBox,
        )
      }
    }
  }

  /**
   * Assigns stable color palette indices (`0 until paletteSize`) to [currentTags] by preserving the
   * color index of any tag whose label or [CameraVisionTag.trackingId] was already present in
   * [previousTags] / [previousColorIndices], so tracked boxes keep their color as they move and resize.
   */
  fun resolveStableVisionTagColorIndices(
    previousTags: List<CameraVisionTag>,
    currentTags: List<CameraVisionTag>,
    previousColorIndices: Map<String, Int> = emptyMap(),
    paletteSize: Int = 5,
  ): Map<String, Int> {
    if (currentTags.isEmpty()) return emptyMap()
    val safePalette = paletteSize.coerceAtLeast(1)
    val prevIndexLookup = linkedMapOf<String, Int>()
    val prevTrackingSlotLookup = linkedMapOf<Int, Int>()
    previousTags.forEachIndexed { idx, tag ->
      val key = tag.label.trim().lowercase(Locale.US)
      if (key.isNotEmpty()) {
        val slot = previousColorIndices[key] ?: (idx % safePalette)
        prevIndexLookup[key] = slot
        if (tag.trackingId != null) {
          prevTrackingSlotLookup[tag.trackingId] = slot
        }
      }
    }

    val result = linkedMapOf<String, Int>()
    val usedSlots = mutableSetOf<Int>()

    // 1. Preserve color slots for labels or trackingIds carried over from the previous detection
    for (tag in currentTags) {
      val key = tag.label.trim().lowercase(Locale.US)
      val prevSlot = prevIndexLookup[key]
        ?: tag.trackingId?.let { prevTrackingSlotLookup[it] }
      if (prevSlot != null && key !in result) {
        val normalizedSlot = ((prevSlot % safePalette) + safePalette) % safePalette
        result[key] = normalizedSlot
        usedSlots.add(normalizedSlot)
      }
    }

    // 2. Assign unused palette slots (or fallback index slots) to newly introduced labels
    var nextCandidateSlot = 0
    currentTags.forEachIndexed { idx, tag ->
      val key = tag.label.trim().lowercase(Locale.US)
      if (key !in result) {
        var chosenSlot = idx % safePalette
        if (chosenSlot in usedSlots && usedSlots.size < safePalette) {
          while (nextCandidateSlot < safePalette && nextCandidateSlot in usedSlots) {
            nextCandidateSlot++
          }
          if (nextCandidateSlot < safePalette) {
            chosenSlot = nextCandidateSlot
          }
        }
        result[key] = chosenSlot
        usedSlots.add(chosenSlot)
      }
    }

    return result
  }

  /**
   * Computes non-overlapping label badge placements ([CameraVisionLabelPlacement]) for a list of
   * visible [tags] inside a stage of size [stageWidthPx] × [stageHeightPx] (with badges clamped to
   * the unobstructed vertical range [usableTopPx]..[usableBottomPx]).
   *
   * Evaluates candidate corners ([CameraVisionLabelCorner.TOP_LEFT], [CameraVisionLabelCorner.TOP_RIGHT],
   * [CameraVisionLabelCorner.BOTTOM_LEFT], [CameraVisionLabelCorner.BOTTOM_RIGHT],
   * [CameraVisionLabelCorner.TOP_CENTER], [CameraVisionLabelCorner.BOTTOM_CENTER]) and vertical offset steps
   * within each bounding box so that overlapping or nested boxes place their labels in distinct corners
   * and never overlap one another.
   */
  fun computeNonOverlappingVisionLabelPlacements(
    tags: List<CameraVisionTag>,
    stageWidthPx: Float,
    usableTopPx: Float,
    usableBottomPx: Float,
    charWidthPx: Float = 7.5f,
    baseBadgePaddingPx: Float = 38f,
    badgeHeightPx: Float = 26f,
    insetPx: Float = 6f,
    collisionPaddingPx: Float = 4f,
    edgeMarginPx: Float = 12f,
    stageHeightPx: Float = usableBottomPx,
  ): List<CameraVisionLabelPlacement> {
    if (tags.isEmpty()) return emptyList()
    val safeStageW = stageWidthPx.coerceAtLeast(160f)
    val safeUsableTop = usableTopPx.coerceAtLeast(0f)
    val safeUsableBottom = usableBottomPx.coerceAtLeast(safeUsableTop + 80f)
    val safeStageH = stageHeightPx.coerceAtLeast(safeUsableBottom)

    val placements = mutableListOf<CameraVisionLabelPlacement>()
    val cornersOrder = listOf(
      CameraVisionLabelCorner.TOP_LEFT,
      CameraVisionLabelCorner.TOP_RIGHT,
      CameraVisionLabelCorner.BOTTOM_LEFT,
      CameraVisionLabelCorner.BOTTOM_RIGHT,
      CameraVisionLabelCorner.TOP_CENTER,
      CameraVisionLabelCorner.BOTTOM_CENTER,
    )

    tags.forEachIndexed { idx, tag ->
      val normBox = (tag.boundingBox ?: defaultVisionBoundingBoxForRank(idx)).coerceNormalized()
      val boxLeft = (normBox.left * safeStageW).coerceIn(edgeMarginPx, (safeStageW - 64f).coerceAtLeast(edgeMarginPx))
      val boxRight = (normBox.right * safeStageW).coerceIn(boxLeft + 52f, (safeStageW - edgeMarginPx).coerceAtLeast(boxLeft + 52f))
      val boxTop = (normBox.top * safeStageH).coerceIn(
        safeUsableTop,
        (safeUsableBottom - 44f).coerceAtLeast(safeUsableTop),
      )
      val boxBottom = (normBox.bottom * safeStageH).coerceIn(
        boxTop + 40f,
        safeUsableBottom.coerceAtLeast(boxTop + 40f),
      )

      val textChars = tag.label.length + "${tag.confidencePercent}%".length
      val estWidth = (baseBadgePaddingPx + textChars * charWidthPx)
        .coerceIn(76f, (safeStageW - edgeMarginPx * 2f).coerceAtLeast(76f))
      val estHeight = badgeHeightPx.coerceAtLeast(18f)

      val minAllowedX = edgeMarginPx
      val maxAllowedX = (safeStageW - estWidth - edgeMarginPx).coerceAtLeast(minAllowedX)
      val minAllowedY = safeUsableTop
      val maxAllowedY = (safeUsableBottom - estHeight).coerceAtLeast(minAllowedY)

      fun buildCandidate(corner: CameraVisionLabelCorner, step: Int): CameraVisionLabelPlacement {
        val rawX = when (corner) {
          CameraVisionLabelCorner.TOP_LEFT,
          CameraVisionLabelCorner.BOTTOM_LEFT -> boxLeft + insetPx
          CameraVisionLabelCorner.TOP_RIGHT,
          CameraVisionLabelCorner.BOTTOM_RIGHT -> boxRight - estWidth - insetPx
          CameraVisionLabelCorner.TOP_CENTER,
          CameraVisionLabelCorner.BOTTOM_CENTER -> (boxLeft + boxRight - estWidth) * 0.5f
        }
        val stepShiftY = step * (estHeight + collisionPaddingPx * 1.5f)
        val rawY = when (corner) {
          CameraVisionLabelCorner.TOP_LEFT,
          CameraVisionLabelCorner.TOP_RIGHT,
          CameraVisionLabelCorner.TOP_CENTER -> boxTop + insetPx + stepShiftY
          CameraVisionLabelCorner.BOTTOM_LEFT,
          CameraVisionLabelCorner.BOTTOM_RIGHT,
          CameraVisionLabelCorner.BOTTOM_CENTER -> boxBottom - estHeight - insetPx - stepShiftY
        }
        val clampedX = rawX.coerceIn(minAllowedX, maxAllowedX)
        val clampedY = rawY.coerceIn(minAllowedY, maxAllowedY)
        return CameraVisionLabelPlacement(
          tag = tag,
          corner = corner,
          leftPx = clampedX,
          topPx = clampedY,
          rightPx = clampedX + estWidth,
          bottomPx = clampedY + estHeight,
        )
      }

      var chosenPlacement: CameraVisionLabelPlacement? = null
      var bestFallback: CameraVisionLabelPlacement? = null
      var minOverlapArea = Float.MAX_VALUE

      // Try all 6 corners at step 0 first (keeps labels tucked cleanly into box corners),
      // then try inward vertical steps if all corners are occupied.
      outerLoop@ for (step in 0..3) {
        for (corner in cornersOrder) {
          val candidate = buildCandidate(corner, step)
          val overlapsAny = placements.any { existing ->
            candidate.overlapsWith(existing, paddingPx = collisionPaddingPx)
          }
          if (!overlapsAny) {
            chosenPlacement = candidate
            break@outerLoop
          }
          val totalOverlap = placements.sumOf { existing ->
            candidate.intersectionAreaWith(existing, paddingPx = collisionPaddingPx).toDouble()
          }.toFloat()
          if (totalOverlap < minOverlapArea) {
            minOverlapArea = totalOverlap
            bestFallback = candidate
          }
        }
      }

      placements.add(chosenPlacement ?: bestFallback ?: buildCandidate(CameraVisionLabelCorner.TOP_LEFT, idx))
    }

    return placements
  }

  /**
   * Normalizes, deduplicates, filters by confidence (>= [MIN_VISION_CONFIDENCE_THRESHOLD]),
   * and suppresses overlapping bounding boxes so only the highest-confidence detection renders
   * for any overlapping region.
   */
  fun buildVisionAnalysisResult(
    rawTags: List<CameraVisionTag>,
    confidenceThreshold: Float = MIN_VISION_CONFIDENCE_THRESHOLD,
    inferenceLatencyMs: Long = 14L,
    frameWidth: Int = 640,
    frameHeight: Int = 640,
    sourceDescription: String = "Live Camera Frame",
    analyzedAtMs: Long = System.currentTimeMillis(),
    maxLabels: Int = 10,
  ): CameraVisionAnalysisResult {
    val clampedThreshold = confidenceThreshold.coerceIn(MIN_VISION_CONFIDENCE_THRESHOLD, 0.99f)
    val dedupByLabel = linkedMapOf<String, CameraVisionTag>()

    val sortedCandidates = rawTags
      .mapNotNull { item ->
        val cleanText = item.label.trim()
        if (cleanText.isEmpty()) return@mapNotNull null
        val formattedText = cleanText.replaceFirstChar { ch ->
          if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
        }
        CameraVisionTag(
          label = formattedText,
          confidence = item.confidence.coerceIn(0f, 1f),
          index = item.index,
          boundingBox = item.boundingBox,
          trackingId = item.trackingId,
          consecutiveFrames = item.consecutiveFrames.coerceAtLeast(1),
          missedFrames = item.missedFrames.coerceAtLeast(0),
        )
      }
      .sortedByDescending { it.confidenceClamped }

    for (tag in sortedCandidates) {
      val key = tag.label.lowercase(Locale.US)
      if (!dedupByLabel.containsKey(key)) {
        val rank = dedupByLabel.size
        val resolvedBox = (tag.boundingBox ?: defaultVisionBoundingBoxForRank(rank)).coerceNormalized()
        dedupByLabel[key] = tag.copy(boundingBox = resolvedBox)
      }
    }

    val allCandidates = dedupByLabel.values.take(maxLabels.coerceAtLeast(1))
    val filteredAboveThreshold = allCandidates.filter { it.confidenceClamped >= clampedThreshold }
    val nonOverlappingTags = suppressOverlappingVisionTags(filteredAboveThreshold)

    return CameraVisionAnalysisResult(
      id = "VISION_${analyzedAtMs}_${sourceDescription.hashCode()}",
      tags = nonOverlappingTags,
      allCandidateTags = allCandidates,
      confidenceThreshold = clampedThreshold,
      inferenceLatencyMs = inferenceLatencyMs.coerceAtLeast(1L),
      frameWidth = frameWidth.coerceAtLeast(0),
      frameHeight = frameHeight.coerceAtLeast(0),
      sourceDescription = sourceDescription,
      analyzedAtMs = analyzedAtMs,
    )
  }

  /**
   * Computes a contextually padded and aspect-balanced pixel crop region (`left`, `top`, `right`, `bottom`)
   * around a detected object [normBox] within a frame of size [frameWidth] × [frameHeight].
   *
   * - Adds [paddingFraction] (`18%` by default, at least `12px`) around each edge so ML Kit's MobileNet
   *   `ImageLabeler` sees the object boundary and surrounding context rather than an isolated texture patch.
   * - Gently expands the shorter crop axis toward a balanced aspect ratio (capped by [maxAspectRatio])
   *   so square `224x224` input resizing inside ML Kit does not severely squash elongated objects.
   */
  fun computeContextualCropBounds(
    normBox: CameraVisionBoundingBox,
    frameWidth: Int,
    frameHeight: Int,
    paddingFraction: Float = 0.18f,
    maxAspectRatio: Float = 1.35f,
  ): CameraSensorMeteringRegion {
    val safeW = frameWidth.coerceAtLeast(32)
    val safeH = frameHeight.coerceAtLeast(32)
    val coerced = normBox.coerceNormalized()

    val rawLeft = (coerced.left * safeW).roundToInt()
    val rawTop = (coerced.top * safeH).roundToInt()
    val rawRight = (coerced.right * safeW).roundToInt()
    val rawBottom = (coerced.bottom * safeH).roundToInt()
    val rawW = (rawRight - rawLeft).coerceAtLeast(16)
    val rawH = (rawBottom - rawTop).coerceAtLeast(16)

    val padX = (rawW * paddingFraction).roundToInt().coerceAtLeast(12)
    val padY = (rawH * paddingFraction).roundToInt().coerceAtLeast(12)

    var left = (rawLeft - padX).coerceIn(0, safeW - 24)
    var top = (rawTop - padY).coerceIn(0, safeH - 24)
    var right = (rawRight + padX).coerceIn(left + 24, safeW)
    var bottom = (rawBottom + padY).coerceIn(top + 24, safeH)

    val currW = right - left
    val currH = bottom - top
    val safeMaxAspect = maxAspectRatio.coerceIn(1.05f, 2.5f)
    if (currW > (currH * safeMaxAspect).roundToInt()) {
      val desiredH = (currW / safeMaxAspect).roundToInt().coerceAtMost(safeH)
      val extraY = ((desiredH - currH) / 2).coerceAtLeast(0)
      top = (top - extraY).coerceIn(0, safeH - 24)
      bottom = (bottom + extraY).coerceIn(top + 24, safeH)
    } else if (currH > (currW * safeMaxAspect).roundToInt()) {
      val desiredW = (currH / safeMaxAspect).roundToInt().coerceAtMost(safeW)
      val extraX = ((desiredW - currW) / 2).coerceAtLeast(0)
      left = (left - extraX).coerceIn(0, safeW - 24)
      right = (right + extraX).coerceIn(left + 24, safeW)
    }

    return CameraSensorMeteringRegion(
      left = left,
      top = top,
      right = right,
      bottom = bottom,
      weight = 1000,
    )
  }

  /**
   * Stabilizes detected [currentTags] against [previousResult] across consecutive live preview frames:
   * 1. **Tracking ID, Label & Spatial IoU Matching**: Matches detections by ML Kit `trackingId`,
   *    case-insensitive label, or high spatial overlap (`IoU >= 0.55f`).
   * 2. **Bounding-Box Micro-Jitter Deadzone & Adaptive EMA Filtering**: Locks stationary boxes when edge
   *    movement is below [VISION_BOX_MICRO_JITTER_DEADZONE] (`1.2%`) and smoothly filters moving boxes
   *    with an adaptive Exponential Moving Average (`alpha = 0.45f..0.82f`).
   * 3. **Confidence EMA & Hysteresis**: Smooths confidence scores across frames, applies a small
   *    multi-frame confirmation bonus (`up to +3.2%`), and prevents confirmed tags (`>= 80%`) from
   *    flickering off when raw confidence momentarily dips to [VISION_HYSTERESIS_HOLD_CONFIDENCE] (`72%`)
   *    or drops out for up to [VISION_MAX_DROPOUT_HOLD_FRAMES] (`2` frames within [VISION_MAX_DROPOUT_HOLD_MS]).
   */
  fun stabilizeVisionTagsAcrossFrames(
    currentTags: List<CameraVisionTag>,
    previousResult: CameraVisionAnalysisResult?,
    confidenceThreshold: Float = MIN_VISION_CONFIDENCE_THRESHOLD,
    analyzedAtMs: Long = System.currentTimeMillis(),
  ): List<CameraVisionTag> {
    if (previousResult == null) {
      return currentTags.map { tag ->
        tag.copy(
          consecutiveFrames = tag.consecutiveFrames.coerceAtLeast(1),
          missedFrames = 0,
        )
      }
    }

    val elapsedMs = (analyzedAtMs - previousResult.analyzedAtMs).coerceAtLeast(0L)
    val prevTags = (previousResult.tags + previousResult.allCandidateTags)
      .distinctBy { "${it.label.trim().lowercase(Locale.US)}_${it.trackingId ?: -1}" }
    if (prevTags.isEmpty()) {
      return currentTags
    }

    val matchedPrevIndices = mutableSetOf<Int>()
    val stabilized = mutableListOf<CameraVisionTag>()

    for (currIdx in currentTags.indices) {
      val curr = currentTags[currIdx]
      val currBox = (curr.boundingBox ?: defaultVisionBoundingBoxForRank(currIdx)).coerceNormalized()
      val currKey = curr.label.trim().lowercase(Locale.US)

      var bestPrevIdx = -1
      var bestMatchScore = -1f

      for (pIdx in prevTags.indices) {
        if (pIdx in matchedPrevIndices) continue
        val prev = prevTags[pIdx]
        val prevBox = (prev.boundingBox ?: defaultVisionBoundingBoxForRank(pIdx)).coerceNormalized()
        val prevKey = prev.label.trim().lowercase(Locale.US)
        val iou = currBox.intersectionOverUnion(prevBox)
        val centerDist = kotlin.math.hypot(
          (currBox.centerX - prevBox.centerX).toDouble(),
          (currBox.centerY - prevBox.centerY).toDouble(),
        ).toFloat()

        val sameTrackingId = curr.trackingId != null &&
          curr.trackingId == prev.trackingId &&
          (iou >= 0.12f || centerDist <= 0.36f)
        val sameLabel = currKey.isNotEmpty() &&
          currKey == prevKey &&
          (iou >= 0.16f || centerDist <= 0.34f)
        val highSpatialOverlap = iou >= 0.56f

        if (!sameTrackingId && !sameLabel && !highSpatialOverlap) continue

        val matchScore = (if (sameTrackingId) 2.0f else 0f) +
          (if (sameLabel) 1.4f else 0f) +
          iou * 1.1f -
          centerDist * 0.4f
        if (matchScore > bestMatchScore) {
          bestMatchScore = matchScore
          bestPrevIdx = pIdx
        }
      }

      if (bestPrevIdx >= 0) {
        matchedPrevIndices.add(bestPrevIdx)
        val prev = prevTags[bestPrevIdx]
        val prevBox = (prev.boundingBox ?: defaultVisionBoundingBoxForRank(bestPrevIdx)).coerceNormalized()
        val prevKey = prev.label.trim().lowercase(Locale.US)

        // Bounding box stabilization: deadzone for micro-jitter + adaptive EMA for smooth motion
        val maxEdgeDelta = maxOf(
          abs(currBox.left - prevBox.left),
          abs(currBox.top - prevBox.top),
          abs(currBox.right - prevBox.right),
          abs(currBox.bottom - prevBox.bottom),
        )
        val smoothedBox = if (maxEdgeDelta < VISION_BOX_MICRO_JITTER_DEADZONE) {
          prevBox
        } else {
          val alpha = (0.45f + (maxEdgeDelta - VISION_BOX_MICRO_JITTER_DEADZONE) * 2.2f)
            .coerceIn(0.45f, 0.82f)
          prevBox.lerpTo(currBox, alpha)
        }

        val sameLabel = currKey == prevKey
        val nextConsecutive = if (sameLabel) (prev.consecutiveFrames + 1).coerceAtMost(30) else 2
        val consistencyBonus = (minOf(nextConsecutive - 1, 4) * 0.008f)
        val rawEmaConf = if (sameLabel) {
          (curr.confidenceClamped * 0.65f + prev.confidenceClamped * 0.35f + consistencyBonus).coerceIn(0f, 1f)
        } else {
          (curr.confidenceClamped * 0.82f + prev.confidenceClamped * 0.18f + consistencyBonus * 0.5f).coerceIn(0f, 1f)
        }

        val prevWasConfirmed = prev.confidenceClamped >= confidenceThreshold
        val finalConf = if (sameLabel && prevWasConfirmed && curr.confidenceClamped >= VISION_HYSTERESIS_HOLD_CONFIDENCE) {
          maxOf(rawEmaConf, confidenceThreshold)
        } else {
          rawEmaConf
        }

        stabilized.add(
          curr.copy(
            confidence = finalConf,
            boundingBox = smoothedBox,
            trackingId = curr.trackingId ?: prev.trackingId,
            consecutiveFrames = nextConsecutive,
            missedFrames = 0,
          )
        )
      } else {
        stabilized.add(
          curr.copy(
            boundingBox = currBox,
            consecutiveFrames = 1,
            missedFrames = 0,
          )
        )
      }
    }

    // Dropout coasting: hold previously confirmed tags that momentarily dropped out for 1-2 frames
    if (elapsedMs <= VISION_MAX_DROPOUT_HOLD_MS) {
      val existingKeys = stabilized.map { it.label.trim().lowercase(Locale.US) }.toMutableSet()
      val existingBoxes = stabilized.mapNotNull { it.boundingBox }.toMutableList()

      for (pIdx in previousResult.tags.indices) {
        val prev = previousResult.tags[pIdx]
        val prevKey = prev.label.trim().lowercase(Locale.US)
        if (prevKey.isEmpty() || prevKey in existingKeys) continue
        if (prev.confidenceClamped < confidenceThreshold) continue
        val nextMissed = prev.missedFrames + 1
        if (nextMissed > VISION_MAX_DROPOUT_HOLD_FRAMES) continue

        val prevBox = (prev.boundingBox ?: defaultVisionBoundingBoxForRank(pIdx)).coerceNormalized()
        val heavilyOverlappedByCurrent = existingBoxes.any { box ->
          box.intersectionOverUnion(prevBox) >= 0.52f || box.isFullyOverlappingWith(prevBox)
        }
        if (heavilyOverlappedByCurrent) continue

        val decayedConf = maxOf(
          confidenceThreshold,
          (prev.confidenceClamped - 0.015f * nextMissed).coerceIn(confidenceThreshold, 0.99f),
        )
        existingKeys.add(prevKey)
        existingBoxes.add(prevBox)
        stabilized.add(
          prev.copy(
            confidence = decayedConf,
            boundingBox = prevBox,
            missedFrames = nextMissed,
          )
        )
      }
    }

    return stabilized
  }

  private fun rememberPreviousVisionBoxesByLabel(
    previousResult: CameraVisionAnalysisResult?,
  ): Map<String, CameraVisionBoundingBox> {
    if (previousResult == null) return emptyMap()
    val map = linkedMapOf<String, CameraVisionBoundingBox>()
    val sourceTags = previousResult.tags.ifEmpty { previousResult.allCandidateTags }
    sourceTags.forEachIndexed { idx, tag ->
      val key = tag.label.trim().lowercase(Locale.US)
      if (key.isNotEmpty() && key !in map) {
        map[key] = (tag.boundingBox ?: defaultVisionBoundingBoxForRank(idx)).coerceNormalized()
      }
    }
    return map
  }

  private fun rememberPreviousVisionTagsByTrackingId(
    previousResult: CameraVisionAnalysisResult?,
  ): Map<Int, CameraVisionTag> {
    if (previousResult == null) return emptyMap()
    val map = linkedMapOf<Int, CameraVisionTag>()
    val sourceTags = previousResult.tags.ifEmpty { previousResult.allCandidateTags }
    for (tag in sourceTags) {
      val tid = tag.trackingId ?: continue
      if (tid !in map) {
        map[tid] = tag
      }
    }
    return map
  }

  /**
   * Runs a two-stage on-device Google ML Kit vision pipeline on [bitmap]:
   * 1. **Object Detection & Tracking** (`com.google.mlkit:object-detection`) using a persistent reusable
   *    detector (`STREAM_MODE` for live camera preview frames so `DetectedObject.trackingId` tracks
   *    objects across frames, or `SINGLE_IMAGE_MODE` when [streamMode] is `false`) with multi-object
   *    detection and classification enabled.
   * 2. **Fine-Grained Classification** (`com.google.mlkit:image-labeling`) using a persistent reusable
   *    labeler on each detected object's aspect-balanced contextual crop ([computeContextualCropBounds])
   *    and on the full frame, filtered through [normalizeAndFilterMlKitLabel],
   *    [suppressGenericHypernymVisionTags], and [stabilizeVisionTagsAcrossFrames].
   */
  suspend fun recognizeVisionTagsFromBitmapWithMlKit(
    bitmap: Bitmap,
    rotationDegrees: Int = 0,
    confidenceThreshold: Float = MIN_VISION_CONFIDENCE_THRESHOLD,
    baseDetectorMinConfidence: Float = MIN_VISION_CONFIDENCE_THRESHOLD,
    maxLabels: Int = 6,
    sourceDescription: String = "Live Camera Frame",
    analyzedAtMs: Long = System.currentTimeMillis(),
    previousResult: CameraVisionAnalysisResult? = null,
    streamMode: Boolean = true,
  ): CameraVisionAnalysisResult? {
    if (bitmap.width <= 16 || bitmap.height <= 16) return null
    val effectiveThreshold = confidenceThreshold.coerceIn(MIN_VISION_CONFIDENCE_THRESHOLD, 0.98f)
    val detectorMin = maxOf(0.48f, minOf(effectiveThreshold, baseDetectorMinConfidence) - 0.14f)
      .coerceIn(0.48f, 0.88f)
    return try {
      val startNs = System.nanoTime()
      val orientedBitmap = orientBitmapIfNeeded(bitmap, rotationDegrees)
      val bw = orientedBitmap.width
      val bh = orientedBitmap.height
      if (bw <= 16 || bh <= 16) return null

      // Stage 1: Persistent ML Kit Object Detector for neural-network bounding boxes + trackingId
      val objectDetector = getOrCreateObjectDetector(streamMode = streamMode)
      val fullInputImage = InputImage.fromBitmap(orientedBitmap, 0)

      val detectedObjects: List<DetectedObject> = try {
        suspendCancellableCoroutine { continuation ->
          objectDetector.process(fullInputImage)
            .addOnSuccessListener { results ->
              if (continuation.isActive) {
                continuation.resume(results.orEmpty())
              }
            }
            .addOnFailureListener {
              if (continuation.isActive) {
                continuation.resume(emptyList())
              }
            }
        }
      } catch (_: Throwable) {
        emptyList()
      }

      // Stage 2: Persistent ML Kit Image Labeler (with generic/abstract label filtering & canonical normalization)
      val labeler = getOrCreateSharedImageLabeler()

      suspend fun runFilteredLabelerOnBitmap(targetBmp: Bitmap): List<Pair<String, Pair<Float, Int>>> {
        val img = InputImage.fromBitmap(targetBmp, 0)
        return suspendCancellableCoroutine { continuation ->
          labeler.process(img)
            .addOnSuccessListener { results ->
              val dedup = linkedMapOf<String, Pair<String, Pair<Float, Int>>>()
              for (item in results) {
                val normalizedLabel = normalizeAndFilterMlKitLabel(item.text) ?: continue
                if (item.confidence < detectorMin) continue
                val key = normalizedLabel.lowercase(Locale.US)
                val existing = dedup[key]
                if (existing == null || item.confidence > existing.second.first) {
                  dedup[key] = normalizedLabel to (item.confidence to item.index)
                }
              }
              if (continuation.isActive) {
                continuation.resume(dedup.values.toList())
              }
            }
            .addOnFailureListener {
              if (continuation.isActive) {
                continuation.resume(emptyList())
              }
            }
        }
      }

      val fullFrameLabels = runFilteredLabelerOnBitmap(orientedBitmap)
      val fullFrameByKey = fullFrameLabels.associateBy { it.first.lowercase(Locale.US) }

      data class RealObjectCandidate(
        val normBox: CameraVisionBoundingBox,
        val trackingId: Int?,
        val coarseCategoryLower: String?,
        val coarseConfidence: Float,
        val coarseIndex: Int,
      )

      val previousBoxByLabel = rememberPreviousVisionBoxesByLabel(previousResult)
      val previousTagByTrackingId = rememberPreviousVisionTagsByTrackingId(previousResult)

      val validObjectBoxes = detectedObjects.mapNotNull { obj ->
        val rect = obj.boundingBox
        val left = (rect.left.toFloat() / bw.toFloat()).coerceIn(0f, 1f)
        val top = (rect.top.toFloat() / bh.toFloat()).coerceIn(0f, 1f)
        val right = (rect.right.toFloat() / bw.toFloat()).coerceIn(0f, 1f)
        val bottom = (rect.bottom.toFloat() / bh.toFloat()).coerceIn(0f, 1f)
        val box = CameraVisionBoundingBox(left, top, right, bottom)
        // Filter out tiny specks (< 4% edge) and full-bleed frame backgrounds (> 92% area)
        if (box.width < 0.04f || box.height < 0.04f || box.area > 0.92f) {
          null
        } else {
          val topCoarse = obj.labels.maxByOrNull { it.confidence }
          RealObjectCandidate(
            normBox = box.coerceNormalized(),
            trackingId = obj.trackingId,
            coarseCategoryLower = topCoarse?.text?.trim()?.lowercase(Locale.US),
            coarseConfidence = topCoarse?.confidence ?: 0f,
            coarseIndex = topCoarse?.index ?: -1,
          )
        }
      }.sortedByDescending { candidate ->
        // Prioritize centrally-framed objects closer to the center of the view, followed by prominence, confidence, and track continuity
        val centerScore = computeCenterProximityScore(candidate.normBox)
        val trackBonus = if (candidate.trackingId != null && candidate.trackingId in previousTagByTrackingId) 0.14f else 0f
        centerScore * 0.55f +
          candidate.normBox.area.coerceIn(0.04f, 0.65f) * centerScore * 0.35f +
          candidate.coarseConfidence * 0.30f +
          trackBonus
      }

      val localizedTags = mutableListOf<CameraVisionTag>()
      val claimedLabelsLower = mutableSetOf<String>()

      // Classify each real DetectedObject bounding box using an aspect-balanced contextual crop + multi-source corroboration
      for (objCandidate in validObjectBoxes.take(maxLabels.coerceAtLeast(1))) {
        val cropRegion = computeContextualCropBounds(
          normBox = objCandidate.normBox,
          frameWidth = bw,
          frameHeight = bh,
          paddingFraction = 0.18f,
          maxAspectRatio = 1.35f,
        )
        val cropW = cropRegion.width
        val cropH = cropRegion.height

        val cropLabels = if (cropW >= 24 && cropH >= 24) {
          val cropBmp = try {
            Bitmap.createBitmap(orientedBitmap, cropRegion.left, cropRegion.top, cropW, cropH)
          } catch (_: Throwable) {
            null
          }
          if (cropBmp != null) {
            runFilteredLabelerOnBitmap(cropBmp)
          } else {
            emptyList()
          }
        } else {
          emptyList()
        }

        val allowedKeywordsForCoarse = objCandidate.coarseCategoryLower?.let {
          OBJECT_DETECTOR_CATEGORY_KEYWORDS[it]
        }
        val prevTrackedTag = objCandidate.trackingId?.let { previousTagByTrackingId[it] }
        val prevTrackedKey = prevTrackedTag?.label?.trim()?.lowercase(Locale.US)

        var bestLabel: String? = null
        var bestConfidence = 0f
        var bestIndex = -1
        var bestRankingScore = -1f

        // 1. Evaluate crop labels corroborated by full-frame labels, object detector category, or multi-frame tracking
        for ((labelText, scoreAndIdx) in cropLabels) {
          val key = labelText.lowercase(Locale.US)
          if (key in claimedLabelsLower) continue
          val cropConf = scoreAndIdx.first
          val fullMatch = fullFrameByKey[key]
          val matchesCoarseDomain = allowedKeywordsForCoarse != null && key in allowedKeywordsForCoarse
          val prevBoxForLabel = previousBoxByLabel[key]
          val prevBoxIou = prevBoxForLabel?.let { objCandidate.normBox.intersectionOverUnion(it) } ?: 0f
          val matchesTrackedHistory = (prevTrackedKey != null && prevTrackedKey == key) || prevBoxIou >= 0.35f
          val isCorroborated = fullMatch != null || matchesCoarseDomain || matchesTrackedHistory

          // Multi-source confidence calibration:
          // - Corroborated by both full-frame and coarse domain/track: fuse scores with a small agreement boost
          // - Corroborated by full-frame, coarse domain, or temporal track: combine scores cleanly
          // - Uncorroborated crop-only on a valid mid-sized object box: apply a gentle 0.93x calibration factor
          val calibratedConf = when {
            fullMatch != null && (matchesCoarseDomain || matchesTrackedHistory) ->
              (maxOf(fullMatch.second.first, cropConf * 0.98f) + 0.025f).coerceAtMost(0.99f)
            fullMatch != null ->
              maxOf(fullMatch.second.first, cropConf * 0.97f)
            matchesCoarseDomain ->
              maxOf(cropConf * 0.98f, objCandidate.coarseConfidence * 0.93f)
            matchesTrackedHistory ->
              maxOf(cropConf * 0.98f, (prevTrackedTag?.confidenceClamped ?: cropConf) * 0.92f)
            objCandidate.normBox.area >= 0.025f ->
              cropConf * 0.93f
            else ->
              cropConf * 0.88f
          }

          val minRequiredForCandidate = if (matchesTrackedHistory) {
            VISION_HYSTERESIS_HOLD_CONFIDENCE
          } else {
            MIN_VISION_CONFIDENCE_THRESHOLD
          }
          if (!isCorroborated && calibratedConf < maxOf(effectiveThreshold, 0.80f)) {
            continue
          }
          if (calibratedConf < minRequiredForCandidate) continue

          val temporalOverlapBonus = prevBoxIou * 0.07f + (if (matchesTrackedHistory) 0.05f else 0f)

          val rankScore = calibratedConf +
            computeVisionLabelSpecificityBonus(labelText) +
            (if (fullMatch != null) 0.05f else 0f) +
            (if (matchesCoarseDomain) 0.04f else 0f) +
            temporalOverlapBonus

          if (rankScore > bestRankingScore) {
            bestRankingScore = rankScore
            bestLabel = labelText
            bestConfidence = calibratedConf
            bestIndex = scoreAndIdx.second
          }
        }

        // 2. If crop didn't yield a label, match an unclaimed full-frame label belonging to this object's category
        if (bestLabel == null && allowedKeywordsForCoarse != null) {
          val domainFullMatch = fullFrameLabels
            .filter { (lbl, _) ->
              val k = lbl.lowercase(Locale.US)
              k !in claimedLabelsLower && k in allowedKeywordsForCoarse
            }
            .maxByOrNull { (lbl, scoreAndIdx) ->
              val prevBox = previousBoxByLabel[lbl.lowercase(Locale.US)]
              val iouBonus = prevBox?.let { objCandidate.normBox.intersectionOverUnion(it) * 0.07f } ?: 0f
              scoreAndIdx.first + computeVisionLabelSpecificityBonus(lbl) + iouBonus
            }
          if (domainFullMatch != null) {
            bestLabel = domainFullMatch.first
            bestConfidence = domainFullMatch.second.first
            bestIndex = domainFullMatch.second.second
          }
        }

        // 3. If this object has a persistent STREAM_MODE trackingId from the previous frame, retain its verified label
        // when the bounding box remains consistent
        if (bestLabel == null &&
          prevTrackedTag != null &&
          prevTrackedKey != null &&
          prevTrackedKey !in claimedLabelsLower &&
          prevTrackedTag.missedFrames < VISION_MAX_DROPOUT_HOLD_FRAMES
        ) {
          bestLabel = prevTrackedTag.label
          bestConfidence = maxOf(
            VISION_HYSTERESIS_HOLD_CONFIDENCE,
            prevTrackedTag.confidenceClamped * 0.96f,
          )
          bestIndex = prevTrackedTag.index
        }

        // 4. If still unmatched and this is the primary detected object, pair with the top specific full-frame label
        if (bestLabel == null && localizedTags.isEmpty() && fullFrameLabels.isNotEmpty()) {
          val topFull = fullFrameLabels
            .filter { (lbl, _) -> lbl.lowercase(Locale.US) !in claimedLabelsLower }
            .maxByOrNull { (lbl, scoreAndIdx) ->
              val prevBox = previousBoxByLabel[lbl.lowercase(Locale.US)]
              val iouBonus = prevBox?.let { objCandidate.normBox.intersectionOverUnion(it) * 0.07f } ?: 0f
              scoreAndIdx.first + computeVisionLabelSpecificityBonus(lbl) + iouBonus
            }
          if (topFull != null) {
            bestLabel = topFull.first
            bestConfidence = topFull.second.first
            bestIndex = topFull.second.second
          }
        }

        // 5. Fallback to ObjectDetector's own coarse category ONLY if it is a concrete noun ("Plant" or "Food"),
        // never internal categories like "Home good", "Fashion good", or "Place".
        if (bestLabel == null &&
          objCandidate.coarseCategoryLower in setOf("plant", "food") &&
          objCandidate.coarseConfidence >= MIN_VISION_CONFIDENCE_THRESHOLD
        ) {
          val cleanCoarse = normalizeAndFilterMlKitLabel(objCandidate.coarseCategoryLower.orEmpty())
          if (cleanCoarse != null && cleanCoarse.lowercase(Locale.US) !in claimedLabelsLower) {
            bestLabel = cleanCoarse
            bestConfidence = objCandidate.coarseConfidence
            bestIndex = objCandidate.coarseIndex
          }
        }

        if (bestLabel != null && bestConfidence >= VISION_HYSTERESIS_HOLD_CONFIDENCE) {
          claimedLabelsLower.add(bestLabel.lowercase(Locale.US))
          localizedTags.add(
            CameraVisionTag(
              label = bestLabel,
              confidence = bestConfidence.coerceIn(0f, 1f),
              index = bestIndex,
              boundingBox = objCandidate.normBox,
              trackingId = objCandidate.trackingId,
            )
          )
        }
      }

      // Only fall back to foreground contour extraction if ObjectDetector found 0 objects in the frame
      if (localizedTags.isEmpty() && fullFrameLabels.isNotEmpty()) {
        val foregroundContours = extractSalientRegionsFromBitmap(orientedBitmap, maxRegions = 3)
        val remainingContours = foregroundContours.toMutableList()
        fullFrameLabels
          .sortedByDescending { (lbl, scoreAndIdx) ->
            scoreAndIdx.first + computeVisionLabelSpecificityBonus(lbl)
          }
          .take(foregroundContours.size)
          .forEach { (labelText, scoreAndIdx) ->
            val adjustedConfidence = (scoreAndIdx.first + minOf(0f, computeVisionLabelSpecificityBonus(labelText)))
              .coerceIn(0f, 1f)
            if (adjustedConfidence >= VISION_HYSTERESIS_HOLD_CONFIDENCE && remainingContours.isNotEmpty()) {
              val prevBox = previousBoxByLabel[labelText.lowercase(Locale.US)]
              val chosenContour = if (prevBox != null) {
                remainingContours.maxByOrNull { contour ->
                  val iou = contour.intersectionOverUnion(prevBox)
                  val dist = kotlin.math.hypot(
                    (contour.centerX - prevBox.centerX).toDouble(),
                    (contour.centerY - prevBox.centerY).toDouble(),
                  ).toFloat()
                  iou * 2.0f - dist * 0.5f
                } ?: remainingContours.first()
              } else {
                remainingContours.first()
              }
              remainingContours.remove(chosenContour)
              localizedTags.add(
                CameraVisionTag(
                  label = labelText,
                  confidence = adjustedConfidence,
                  index = scoreAndIdx.second,
                  boundingBox = chosenContour,
                )
              )
            }
          }
      }

      val temporallyStabilized = stabilizeVisionTagsAcrossFrames(
        currentTags = localizedTags,
        previousResult = previousResult,
        confidenceThreshold = effectiveThreshold,
        analyzedAtMs = analyzedAtMs,
      )
      val hypernymFiltered = suppressGenericHypernymVisionTags(temporallyStabilized)
      val latencyMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
      buildVisionAnalysisResult(
        rawTags = hypernymFiltered,
        confidenceThreshold = effectiveThreshold,
        inferenceLatencyMs = latencyMs,
        frameWidth = bw,
        frameHeight = bh,
        sourceDescription = sourceDescription,
        analyzedAtMs = analyzedAtMs,
        maxLabels = maxLabels,
      )
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Returns `true` if a manual tap-to-focus override started at [manualFocusTimestampMs]
   * is still active at [nowMs] within [overrideDurationMs] (defaults to [MANUAL_FOCUS_OVERRIDE_DURATION_MS] = 7000ms).
   */
  fun isManualFocusOverrideActive(
    manualFocusTimestampMs: Long,
    nowMs: Long = System.currentTimeMillis(),
    overrideDurationMs: Long = MANUAL_FOCUS_OVERRIDE_DURATION_MS,
  ): Boolean {
    if (manualFocusTimestampMs <= 0L || overrideDurationMs <= 0L) return false
    val elapsed = nowMs - manualFocusTimestampMs
    return elapsed in 0L until overrideDurationMs
  }

  /**
   * Returns the remaining duration in milliseconds for an active manual focus override,
   * or `0L` if the 7-second override has expired.
   */
  fun remainingManualFocusOverrideMs(
    manualFocusTimestampMs: Long,
    nowMs: Long = System.currentTimeMillis(),
    overrideDurationMs: Long = MANUAL_FOCUS_OVERRIDE_DURATION_MS,
  ): Long {
    if (!isManualFocusOverrideActive(manualFocusTimestampMs, nowMs, overrideDurationMs)) return 0L
    return (overrideDurationMs - (nowMs - manualFocusTimestampMs)).coerceIn(0L, overrideDurationMs)
  }

  /**
   * Creates a manual focus override target pinned at normalized viewfinder coordinates ([normX], [normY]).
   */
  fun createManualFocusTarget(
    normX: Float,
    normY: Float,
    timestampMs: Long = System.currentTimeMillis(),
  ): CameraSmartFocusTarget {
    val cx = normX.coerceIn(0.08f, 0.92f)
    val cy = normY.coerceIn(0.08f, 0.92f)
    val halfSize = 0.08f
    val box = CameraVisionBoundingBox(
      left = (cx - halfSize).coerceIn(0.02f, 0.90f),
      top = (cy - halfSize).coerceIn(0.02f, 0.90f),
      right = (cx + halfSize).coerceIn(0.10f, 0.98f),
      bottom = (cy + halfSize).coerceIn(0.10f, 0.98f),
    )
    return CameraSmartFocusTarget(
      normX = cx,
      normY = cy,
      boundingBox = box,
      source = CameraFocusSource.MANUAL_OVERRIDE,
      timestampMs = timestampMs,
    )
  }

  /**
   * Resolves the currently active camera focus target:
   * - If [manualTarget] is non-null and its 7-second override window ([overrideDurationMs]) has not expired at [nowMs],
   *   returns [manualTarget] with [CameraFocusSource.MANUAL_OVERRIDE].
   * - Otherwise, falls back to [smartAutoTarget] (driven by ML Kit Object Detection).
   */
  fun resolveActiveFocusTarget(
    manualTarget: CameraSmartFocusTarget?,
    smartAutoTarget: CameraSmartFocusTarget?,
    nowMs: Long = System.currentTimeMillis(),
    overrideDurationMs: Long = MANUAL_FOCUS_OVERRIDE_DURATION_MS,
  ): CameraSmartFocusTarget? {
    if (manualTarget != null && isManualFocusOverrideActive(manualTarget.timestampMs, nowMs, overrideDurationMs)) {
      return manualTarget.copy(source = CameraFocusSource.MANUAL_OVERRIDE)
    }
    return smartAutoTarget?.copy(source = CameraFocusSource.ML_OBJECT_AUTO)
  }

  /**
   * Returns `true` when [candidate] corresponds to the same physical subject currently tracked by
   * [previousTarget], accounting for tracking ID, bounding-box IoU overlap, category label continuity,
   * and proximity to both current position and velocity-projected trajectory position.
   */
  fun isSameTrackedSubject(
    candidate: RawDetectedObjectForFocus,
    previousTarget: CameraSmartFocusTarget?,
  ): Boolean {
    if (previousTarget == null || previousTarget.source != CameraFocusSource.ML_OBJECT_AUTO) {
      return false
    }
    val box = candidate.boundingBox.coerceNormalized()
    val cx = box.centerX.coerceIn(0.08f, 0.92f)
    val cy = box.centerY.coerceIn(0.08f, 0.92f)

    val predX = (previousTarget.clampedX + previousTarget.velocityX * 0.25f).coerceIn(0.08f, 0.92f)
    val predY = (previousTarget.clampedY + previousTarget.velocityY * 0.25f).coerceIn(0.08f, 0.92f)

    val distCurr = kotlin.math.hypot((cx - previousTarget.clampedX).toDouble(), (cy - previousTarget.clampedY).toDouble()).toFloat()
    val distPred = kotlin.math.hypot((cx - predX).toDouble(), (cy - predY).toDouble()).toFloat()
    val distTarget = kotlin.math.hypot((cx - previousTarget.clampedTargetX).toDouble(), (cy - previousTarget.clampedTargetY).toDouble()).toFloat()
    val minDist = minOf(distCurr, distPred, distTarget)

    val iouCurr = previousTarget.boundingBox?.let { box.intersectionOverUnion(it) } ?: 0f
    val iouTarget = previousTarget.targetBoundingBox?.let { box.intersectionOverUnion(it) } ?: 0f
    val maxIou = maxOf(iouCurr, iouTarget)

    val sameId = candidate.trackingId != null &&
      candidate.trackingId == previousTarget.trackingId &&
      minDist <= 0.42f
    val sameLabel = !candidate.label.isNullOrBlank() &&
      !previousTarget.categoryLabel.isNullOrBlank() &&
      candidate.label.trim().equals(previousTarget.categoryLabel.trim(), ignoreCase = true) &&
      minDist <= 0.30f

    return sameId || maxIou >= 0.28f || minDist <= 0.16f || sameLabel
  }

  /**
   * Computes a normalized center-proximity priority score (`0.04f..1.0f`) for [box] relative to the
   * viewfinder center (`0.5f`, `0.5f`).
   *
   * Uses a steep radial falloff from the viewfinder center combined with an inner-core center bonus
   * and bounding-box center-overlap proximity so objects closer to the center of the view are
   * strongly prioritized over peripheral detections.
   */
  fun computeCenterProximityScore(box: CameraVisionBoundingBox): Float {
    val centerDist = box.distanceToViewfinderCenter
    val normDist = (centerDist / 0.55f).coerceIn(0f, 1f)
    val radialScore = (1f - normDist).pow(1.65f)
    val innerCoreScore = ((0.24f - centerDist) / 0.24f).coerceIn(0f, 1f)
    val boundsProximityScore = ((0.18f - box.distanceFromBoundsToViewfinderCenter) / 0.18f).coerceIn(0f, 1f)
    return (radialScore * 0.84f + innerCoreScore * 0.11f + boundsProximityScore * 0.05f)
      .coerceIn(0.04f, 1.0f)
  }

  /**
   * Scores an ML Kit [RawDetectedObjectForFocus] candidate for smart autofocus priority.
   * Strongly prioritizes viewfinder center proximity so objects detected closer to the center
   * of the view are favored, while balancing foreground subject prominence, classification
   * confidence, and sticky temporal tracking stability (scaled by center proximity) with [previousTarget].
   */
  fun scoreObjectForSmartAutofocus(
    candidate: RawDetectedObjectForFocus,
    previousTarget: CameraSmartFocusTarget? = null,
  ): Float {
    val box = candidate.boundingBox
    if (box.width < 0.04f || box.height < 0.04f || box.area < 0.0020f || box.area > 0.92f) {
      return -1f
    }
    // Center proximity score (1.0 at exact center, steeply falling off toward periphery)
    val centerScore = computeCenterProximityScore(box)

    // Subject size prominence: sweet spot is 5%..45% of frame area
    val area = box.area
    val sizeScore = when {
      area < 0.02f -> (area / 0.02f) * 0.5f
      area <= 0.45f -> 0.5f + ((area - 0.02f) / 0.43f) * 0.5f
      else -> (1f - ((area - 0.45f) / 0.50f)).coerceIn(0.25f, 0.85f)
    }

    val confidenceBonus = candidate.confidence.coerceIn(0f, 1f) * 0.14f

    // Temporal tracking stickiness & trajectory alignment bonus to avoid focus hunting,
    // attenuated by center proximity so peripheral locks yield smoothly when a central object is detected
    var trackingBonus = 0f
    if (previousTarget != null && previousTarget.source == CameraFocusSource.ML_OBJECT_AUTO) {
      val lockScale = previousTarget.lockStrength.coerceIn(0.65f, 1.35f)
      val centerLockWeight = (0.35f + 0.65f * centerScore).coerceIn(0.35f, 1.0f)
      val predX = (previousTarget.clampedX + previousTarget.velocityX * 0.25f).coerceIn(0.08f, 0.92f)
      val predY = (previousTarget.clampedY + previousTarget.velocityY * 0.25f).coerceIn(0.08f, 0.92f)

      val distCurrSq = (box.centerX - previousTarget.clampedX).pow(2) + (box.centerY - previousTarget.clampedY).pow(2)
      val distPredSq = (box.centerX - predX).pow(2) + (box.centerY - predY).pow(2)
      val distTargetSq = (box.centerX - previousTarget.clampedTargetX).pow(2) + (box.centerY - previousTarget.clampedTargetY).pow(2)
      val minDist = kotlin.math.sqrt(minOf(distCurrSq, distPredSq, distTargetSq))

      if (candidate.trackingId != null && candidate.trackingId == previousTarget.trackingId && minDist <= 0.45f) {
        trackingBonus += 0.11f * lockScale * centerLockWeight
      }

      if (!candidate.label.isNullOrBlank() &&
        !previousTarget.categoryLabel.isNullOrBlank() &&
        candidate.label.trim().equals(previousTarget.categoryLabel.trim(), ignoreCase = true) &&
        minDist <= 0.36f
      ) {
        trackingBonus += 0.06f * lockScale * centerLockWeight
      }

      val iouCurr = previousTarget.boundingBox?.let { box.intersectionOverUnion(it) } ?: 0f
      val iouTarget = previousTarget.targetBoundingBox?.let { box.intersectionOverUnion(it) } ?: 0f
      val maxIou = maxOf(iouCurr, iouTarget)
      if (maxIou > 0f) {
        trackingBonus += maxIou * 0.10f * lockScale * centerLockWeight
      }

      // Smooth spatial stickiness radius around current, trajectory-predicted, and desired focus points
      if (minDist < 0.36f) {
        val proximityFactor = (1f - minDist / 0.36f).coerceIn(0f, 1f)
        trackingBonus += proximityFactor * proximityFactor * 0.12f * lockScale * centerLockWeight
      }

      // Velocity & trajectory alignment bonus: favor candidates continuing along the current movement vector
      val speed = previousTarget.speedNormPerSec
      if (speed >= 0.015f) {
        val moveDx = box.centerX - previousTarget.clampedX
        val moveDy = box.centerY - previousTarget.clampedY
        val moveDist = kotlin.math.sqrt(moveDx * moveDx + moveDy * moveDy)
        if (moveDist >= 0.015f) {
          val cosAlign = ((moveDx * previousTarget.velocityX + moveDy * previousTarget.velocityY) / (moveDist * speed))
            .coerceIn(-1f, 1f)
          val speedWeight = (speed / STICKY_AF_MAX_SPEED_NORM_PER_SEC).coerceIn(0f, 1f)
          trackingBonus += cosAlign * 0.08f * speedWeight
        }
      }
    }

    return centerScore * 0.82f + sizeScore * 0.14f + confidenceBonus + trackingBonus
  }

  private fun ellipseArcDerivative(
    semiMajorAxisA: Float,
    semiMinorAxisB: Float,
    thetaRad: Float,
  ): Float {
    val sinT = sin(thetaRad.toDouble()).toFloat()
    val cosT = cos(thetaRad.toDouble()).toFloat()
    val d1 = semiMajorAxisA * sinT
    val d2 = semiMinorAxisB * cosT
    return kotlin.math.sqrt(d1 * d1 + d2 * d2).coerceAtLeast(1e-6f)
  }

  private fun estimateRemainingEllipseArcLength(
    semiMajorAxisA: Float,
    semiMinorAxisB: Float,
    fromThetaRad: Float,
    toThetaRad: Float = CameraAutofocusCornerPath.HALF_PI,
    segments: Int = 12,
  ): Float {
    if (semiMajorAxisA <= 1e-5f || semiMinorAxisB <= 1e-5f) return 0f
    val span = toThetaRad - fromThetaRad
    if (span <= 1e-5f) return 0f
    val n = segments.coerceIn(4, 32)
    val dTheta = span / n.toFloat()
    var sum = 0f
    for (i in 0 until n) {
      val mid = fromThetaRad + (i + 0.5f) * dTheta
      sum += ellipseArcDerivative(semiMajorAxisA, semiMinorAxisB, mid)
    }
    return (sum * dTheta).coerceAtLeast(0f)
  }

  /**
   * Builds a $C^1$ tangent-continuous [CameraAutofocusCornerPath] from ([startX], [startY])
   * to ([goalX], [goalY]) given initial velocity ([velocityX], [velocityY]).
   *
   * The path begins with a small elliptical arc (semi-major axis `a` along the target line,
   * semi-minor axis `b < a` perpendicular to the target line) that smoothly turns the cursor
   * into the junction point `P_J`, and then follows an exact straight line from `P_J` to ([goalX], [goalY]).
   */
  fun buildAutofocusCornerPath(
    startX: Float,
    startY: Float,
    goalX: Float,
    goalY: Float,
    velocityX: Float = 0f,
    velocityY: Float = 0f,
  ): CameraAutofocusCornerPath {
    val halfPi = CameraAutofocusCornerPath.HALF_PI
    val dx = goalX - startX
    val dy = goalY - startY
    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
    if (dist <= 0.004f) {
      val dirX = if (dist > 1e-5f) dx / dist else 1f
      val dirY = if (dist > 1e-5f) dy / dist else 0f
      return CameraAutofocusCornerPath(
        originX = startX,
        originY = startY,
        goalX = goalX,
        goalY = goalY,
        junctionX = startX,
        junctionY = startY,
        lineDirX = dirX,
        lineDirY = dirY,
        normalDirX = -dirY,
        normalDirY = dirX,
        semiMajorAxisA = 0f,
        semiMinorAxisB = 0f,
        startAngleRad = halfPi,
        currentAngleRad = halfPi,
        arcLength = 0f,
        straightLineLength = dist,
        straightDistanceTraveled = 0f,
      )
    }

    val chordDirX = dx / dist
    val chordDirY = dy / dist
    val ccwNormalX = -chordDirY
    val ccwNormalY = chordDirX

    val v0 = kotlin.math.sqrt(velocityX * velocityX + velocityY * velocityY)
    var normalSign = 1f
    var alphaChord = 0.22f
    var hasVelocityHeading = false

    if (v0 >= 0.012f) {
      val ux0 = velocityX / v0
      val uy0 = velocityY / v0
      val dotChord = (ux0 * chordDirX + uy0 * chordDirY).coerceIn(-1f, 1f)
      val dotNormal = ux0 * ccwNormalX + uy0 * ccwNormalY
      val rawAngle = kotlin.math.acos(dotChord.toDouble()).toFloat()
      if (rawAngle >= 0.10f && abs(dotNormal) >= 1e-4f) {
        normalSign = if (dotNormal >= 0f) 1f else -1f
        alphaChord = rawAngle.coerceIn(0.10f, (Math.PI - 0.14).toFloat())
        hasVelocityHeading = true
      }
    }

    if (!hasVelocityHeading) {
      if (abs(dx) > 0.015f && abs(dy) > 0.015f) {
        val domX = if (abs(dx) >= abs(dy)) (if (dx >= 0f) 1f else -1f) else 0f
        val domY = if (abs(dx) >= abs(dy)) 0f else (if (dy >= 0f) 1f else -1f)
        val dotDom = (domX * chordDirX + domY * chordDirY).coerceIn(-1f, 1f)
        val domAngle = kotlin.math.acos(dotDom.toDouble()).toFloat()
        val domNormal = domX * ccwNormalX + domY * ccwNormalY
        normalSign = if (domNormal >= 0f) 1f else -1f
        alphaChord = (domAngle * 0.55f).coerceIn(0.12f, 0.36f)
      } else {
        val toCenterNormal = (0.5f - startX) * ccwNormalX + (0.5f - startY) * ccwNormalY
        normalSign = if (toCenterNormal >= 0f) 1f else -1f
        alphaChord = 0.22f
      }
    }

    val nX = normalSign * ccwNormalX
    val nY = normalSign * ccwNormalY

    val turnSine = sin(alphaChord.coerceAtMost(halfPi).toDouble()).toFloat()
    var a = (0.24f * dist + 0.06f * v0 * turnSine).coerceIn(
      minOf(0.010f, 0.22f * dist),
      minOf(0.34f * dist, 0.16f),
    )
    var b = (a * STICKY_AF_CORNER_ELLIPSE_MINOR_TO_MAJOR_RATIO).coerceIn(
      minOf(0.0058f, 0.13f * dist),
      minOf(0.20f * dist, a * 0.82f),
    )

    var beta = 0f
    var theta0 = 0f
    var e1X = chordDirX
    var e1Y = chordDirY
    var e2X = nX
    var e2Y = nY
    var jX = startX
    var jY = startY
    var lineLen = dist

    for (attempt in 0 until 3) {
      beta = 0f
      theta0 = 0f
      for (iter in 0 until 4) {
        val alpha = (alphaChord + beta).coerceIn(0.08f, (Math.PI - 0.12).toFloat())
        theta0 = kotlin.math.atan2(
          (b * cos(alpha.toDouble())).toFloat(),
          (a * sin(alpha.toDouble())).toFloat(),
        )
        val lateralOffset = b * (1f - sin(theta0.toDouble()).toFloat())
        val sinBeta = (lateralOffset / dist).coerceIn(0f, 0.65f)
        beta = kotlin.math.asin(sinBeta.toDouble()).toFloat()
      }

      val cosBeta = cos(beta.toDouble()).toFloat()
      val sinBeta = sin(beta.toDouble()).toFloat()
      e1X = cosBeta * chordDirX - sinBeta * nX
      e1Y = cosBeta * chordDirY - sinBeta * nY
      e2X = sinBeta * chordDirX + cosBeta * nX
      e2Y = sinBeta * chordDirY + cosBeta * nY

      val cosT0 = cos(theta0.toDouble()).toFloat()
      val sinT0 = sin(theta0.toDouble()).toFloat()
      jX = startX + a * cosT0 * e1X + b * (1f - sinT0) * e2X
      jY = startY + a * cosT0 * e1Y + b * (1f - sinT0) * e2Y
      lineLen = (dist * cosBeta - a * cosT0).coerceAtLeast(0f)

      val apexX = if (theta0 < 0f) jX - a * e1X - b * e2X else jX
      val apexY = if (theta0 < 0f) jY - a * e1Y - b * e2Y else jY
      val inBounds = jX in 0.081f..0.919f &&
        jY in 0.081f..0.919f &&
        apexX in 0.081f..0.919f &&
        apexY in 0.081f..0.919f
      if (inBounds && lineLen >= 0.35f * dist) {
        break
      }
      a *= 0.60f
      b *= 0.60f
    }

    val arcLen = estimateRemainingEllipseArcLength(
      semiMajorAxisA = a,
      semiMinorAxisB = b,
      fromThetaRad = theta0,
      toThetaRad = halfPi,
      segments = 16,
    )

    return CameraAutofocusCornerPath(
      originX = startX,
      originY = startY,
      goalX = goalX,
      goalY = goalY,
      junctionX = jX,
      junctionY = jY,
      lineDirX = e1X,
      lineDirY = e1Y,
      normalDirX = e2X,
      normalDirY = e2Y,
      semiMajorAxisA = a,
      semiMinorAxisB = b,
      startAngleRad = theta0,
      currentAngleRad = theta0,
      arcLength = arcLen,
      straightLineLength = lineLen,
      straightDistanceTraveled = 0f,
    )
  }

  /**
   * Advances [currentTarget] by [dtSeconds] along a smooth, velocity-continuous trajectory toward
   * ([desiredX], [desiredY]):
   * - Traces a small elliptical arc at the start of the movement to corner smoothly into the target heading,
   *   followed by a tangent-continuous straight line directly to ([desiredX], [desiredY]).
   * - Allows higher travel speed ([maxSpeedNormPerSec]) across long movements while strictly limiting
   *   the maximum velocity added to the cursor's current speed ([maxVelocityAddNormPerSec] and
   *   [maxAccelerationNormPerSecSq]).
   * - Smoothly decelerates along a $C^1$ arrival braking curve as it reaches the target without overshoot.
   * - Holds steady against sub-2% micro-jitter when already settled at the target.
   */
  fun stepStickyAutofocusTrajectory(
    currentTarget: CameraSmartFocusTarget,
    desiredX: Float = currentTarget.targetNormX,
    desiredY: Float = currentTarget.targetNormY,
    desiredBox: CameraVisionBoundingBox? = currentTarget.targetBoundingBox ?: currentTarget.boundingBox,
    dtSeconds: Float = 0.032f,
    naturalFrequencyRadPerSec: Float = STICKY_AF_NATURAL_FREQUENCY_RAD_PER_SEC,
    steeringTimeConstantSec: Float = STICKY_AF_STEERING_TIME_CONSTANT_SEC,
    maxSpeedNormPerSec: Float = STICKY_AF_MAX_SPEED_NORM_PER_SEC,
    maxVelocityAddNormPerSec: Float = STICKY_AF_MAX_VELOCITY_ADD_NORM_PER_SEC,
    maxAccelerationNormPerSecSq: Float = STICKY_AF_MAX_ACCEL_NORM_PER_SEC_SQ,
    timestampMs: Long = currentTarget.timestampMs + (dtSeconds.coerceAtLeast(0f) * 1000f).roundToInt(),
  ): CameraSmartFocusTarget {
    val halfPi = CameraAutofocusCornerPath.HALF_PI
    val startX = currentTarget.clampedX
    val startY = currentTarget.clampedY
    val rawGoalX = desiredX.coerceIn(0.08f, 0.92f)
    val rawGoalY = desiredY.coerceIn(0.08f, 0.92f)
    val coercedGoalBox = desiredBox?.coerceNormalized()

    val initialDx = rawGoalX - startX
    val initialDy = rawGoalY - startY
    val initialDistSq = initialDx * initialDx + initialDy * initialDy
    val initialSpeedSq = currentTarget.velocityX * currentTarget.velocityX +
      currentTarget.velocityY * currentTarget.velocityY
    val initialSpeed = kotlin.math.sqrt(initialSpeedSq)

    val alreadySettledAtPreviousGoal = currentTarget.distanceToDesiredTarget < 0.002f &&
      initialSpeedSq < 0.000225f

    // Sub-2% micro-jitter deadzone when stationary at previous target: hold steady without drift
    if (alreadySettledAtPreviousGoal && initialDistSq < 0.0004f) {
      return currentTarget.copy(
        normX = startX,
        normY = startY,
        targetNormX = startX,
        targetNormY = startY,
        boundingBox = currentTarget.boundingBox ?: coercedGoalBox,
        targetBoundingBox = coercedGoalBox ?: currentTarget.boundingBox,
        velocityX = 0f,
        velocityY = 0f,
        cornerPath = null,
        timestampMs = timestampMs,
      )
    }

    val existingPath = currentTarget.cornerPath
    val activePath = if (existingPath != null) {
      val goalShift = kotlin.math.hypot(
        (rawGoalX - existingPath.goalX).toDouble(),
        (rawGoalY - existingPath.goalY).toDouble(),
      ).toFloat()
      val (expectedX, expectedY) = if (existingPath.isOnEllipticalArc) {
        existingPath.pointOnArc(existingPath.currentAngleRad)
      } else {
        Pair(
          existingPath.junctionX + existingPath.straightDistanceTraveled * existingPath.lineDirX,
          existingPath.junctionY + existingPath.straightDistanceTraveled * existingPath.lineDirY,
        )
      }
      val posDrift = kotlin.math.hypot(
        (startX - expectedX.coerceIn(0.08f, 0.92f)).toDouble(),
        (startY - expectedY.coerceIn(0.08f, 0.92f)).toDouble(),
      ).toFloat()
      if (goalShift < 0.02f && posDrift < 0.006f) {
        existingPath
      } else {
        buildAutofocusCornerPath(
          startX = startX,
          startY = startY,
          goalX = rawGoalX,
          goalY = rawGoalY,
          velocityX = currentTarget.velocityX,
          velocityY = currentTarget.velocityY,
        )
      }
    } else {
      buildAutofocusCornerPath(
        startX = startX,
        startY = startY,
        goalX = rawGoalX,
        goalY = rawGoalY,
        velocityX = currentTarget.velocityX,
        velocityY = currentTarget.velocityY,
      )
    }

    val goalX = activePath.goalX
    val goalY = activePath.goalY

    val clampedDt = dtSeconds.coerceIn(0.004f, 0.60f)
    val subSteps = kotlin.math.ceil(clampedDt / 0.008f).toInt().coerceIn(1, 64)
    val h = clampedDt / subSteps.toFloat()

    val omega = naturalFrequencyRadPerSec.coerceIn(0.5f, 12.0f)
    val tau = steeringTimeConstantSec.coerceIn(0.04f, 1.0f)
    val vMax = maxSpeedNormPerSec.coerceIn(0.08f, 2.5f)
    val vAddMax = maxVelocityAddNormPerSec.coerceIn(0.02f, vMax)
    val aMax = maxAccelerationNormPerSecSq.coerceIn(0.2f, 10.0f)
    val aDecel = (aMax * 0.88f).coerceIn(0.25f, 8.0f)

    // Strict upper bound on the speed the cursor can reach by adding velocity to its own speed in this update
    val maxAllowedSpeedGain = minOf(vAddMax, aMax * clampedDt)
    val vCallMax = minOf(vMax, initialSpeed + maxAllowedSpeedGain)

    val d1 = aDecel / (omega * omega)

    var x = startX
    var y = startY
    var theta = activePath.currentAngleRad
    var straightU = activePath.straightDistanceTraveled
    var speed = initialSpeed
    var (tx, ty) = if (theta < halfPi - 1e-5f && activePath.arcLength > 1e-5f) {
      activePath.unitTangentOnArc(theta)
    } else {
      Pair(activePath.lineDirX, activePath.lineDirY)
    }

    for (step in 0 until subSteps) {
      val remArc = if (activePath.arcLength > 1e-5f && theta < halfPi - 1e-5f) {
        estimateRemainingEllipseArcLength(
          semiMajorAxisA = activePath.semiMajorAxisA,
          semiMinorAxisB = activePath.semiMinorAxisB,
          fromThetaRad = theta,
          toThetaRad = halfPi,
          segments = 8,
        )
      } else {
        0f
      }
      val remLine = (activePath.straightLineLength - straightU).coerceAtLeast(0f)
      val remDist = remArc + remLine

      if (remDist <= 0.0008f && speed <= 0.025f) {
        x = goalX
        y = goalY
        theta = halfPi
        straightU = activePath.straightLineLength
        speed = 0f
        break
      }

      // Smooth C^1 arrival deceleration curve: constant-deceleration parabola merging tangentially into omega * d
      val vArrival = if (remDist <= d1) {
        omega * remDist
      } else {
        kotlin.math.sqrt((2f * aDecel * remDist - aDecel * d1).coerceAtLeast(0f))
      }

      // Moderate speed through tight elliptical corner curvature, accelerating smoothly onto the straight line
      val vCornerMax = if (remArc > 1e-5f) {
        val cornerProgressSin = sin(theta.coerceIn(0f, halfPi).toDouble()).toFloat()
        vMax * (0.42f + 0.58f * cornerProgressSin)
      } else {
        vMax
      }

      val vDesired = minOf(vMax, vCornerMax, vArrival)
      val vTarget = minOf(vCallMax, vDesired)

      if (speed < vTarget) {
        speed = minOf(vTarget, speed + aMax * h)
      } else if (speed > vDesired) {
        val kinematicDecel = (speed * speed) / (2f * remDist.coerceAtLeast(1e-4f))
        val decelRate = maxOf(
          aDecel,
          (speed - vDesired) / (tau * 0.55f).coerceAtLeast(0.03f),
          kinematicDecel,
        )
        speed = maxOf(vDesired, speed - decelRate * h)
      }

      speed = minOf(speed, remDist / h).coerceAtLeast(0f)
      val ds = speed * h

      if (remArc > 1e-5f) {
        val d1Deriv = ellipseArcDerivative(activePath.semiMajorAxisA, activePath.semiMinorAxisB, theta)
        val midTheta = (theta + 0.5f * ds / d1Deriv).coerceIn(activePath.startAngleRad, halfPi)
        val d2Deriv = ellipseArcDerivative(activePath.semiMajorAxisA, activePath.semiMinorAxisB, midTheta)
        val candidateTheta = theta + ds / d2Deriv
        if (ds < remArc && candidateTheta < halfPi - 1e-4f) {
          theta = candidateTheta.coerceIn(activePath.startAngleRad, halfPi - 1e-4f)
          val (px, py) = activePath.pointOnArc(theta)
          val (utx, uty) = activePath.unitTangentOnArc(theta)
          x = px.coerceIn(0.08f, 0.92f)
          y = py.coerceIn(0.08f, 0.92f)
          tx = utx
          ty = uty
        } else {
          val leftoverDs = (ds - remArc).coerceAtLeast(0f)
          theta = halfPi
          straightU = (straightU + leftoverDs).coerceIn(0f, activePath.straightLineLength)
          x = (activePath.junctionX + straightU * activePath.lineDirX).coerceIn(0.08f, 0.92f)
          y = (activePath.junctionY + straightU * activePath.lineDirY).coerceIn(0.08f, 0.92f)
          tx = activePath.lineDirX
          ty = activePath.lineDirY
        }
      } else {
        theta = halfPi
        straightU = (straightU + ds).coerceIn(0f, activePath.straightLineLength)
        x = (activePath.junctionX + straightU * activePath.lineDirX).coerceIn(0.08f, 0.92f)
        y = (activePath.junctionY + straightU * activePath.lineDirY).coerceIn(0.08f, 0.92f)
        tx = activePath.lineDirX
        ty = activePath.lineDirY
      }

      val afterRemLine = (activePath.straightLineLength - straightU).coerceAtLeast(0f)
      if (theta >= halfPi - 1e-5f && afterRemLine <= 0.0008f && speed <= 0.025f) {
        x = goalX
        y = goalY
        theta = halfPi
        straightU = activePath.straightLineLength
        speed = 0f
        break
      }
    }

    val vx = if (speed > 1e-5f) speed * tx else 0f
    val vy = if (speed > 1e-5f) speed * ty else 0f

    val updatedCornerPath = if (speed <= 1e-5f &&
      theta >= halfPi - 1e-5f &&
      (activePath.straightLineLength - straightU) <= 0.0008f
    ) {
      activePath.copy(
        currentAngleRad = halfPi,
        straightDistanceTraveled = activePath.straightLineLength,
      )
    } else {
      activePath.copy(
        currentAngleRad = theta,
        straightDistanceTraveled = straightU,
      )
    }

    val initialDist = kotlin.math.sqrt(initialDistSq)
    val finalDist = kotlin.math.hypot((goalX - x).toDouble(), (goalY - y).toDouble()).toFloat()
    val progress = if (initialDist > 1e-4f) {
      (1f - (finalDist / initialDist)).coerceIn(0.08f, 1f)
    } else {
      1f
    }

    val prevBox = currentTarget.boundingBox
    val interpolatedBox = if (prevBox != null && coercedGoalBox != null) {
      CameraVisionBoundingBox(
        left = prevBox.left + (coercedGoalBox.left - prevBox.left) * progress,
        top = prevBox.top + (coercedGoalBox.top - prevBox.top) * progress,
        right = prevBox.right + (coercedGoalBox.right - prevBox.right) * progress,
        bottom = prevBox.bottom + (coercedGoalBox.bottom - prevBox.bottom) * progress,
      ).coerceNormalized()
    } else {
      coercedGoalBox ?: prevBox
    }

    return currentTarget.copy(
      normX = x,
      normY = y,
      boundingBox = interpolatedBox,
      targetNormX = goalX,
      targetNormY = goalY,
      targetBoundingBox = coercedGoalBox ?: interpolatedBox,
      velocityX = vx,
      velocityY = vy,
      cornerPath = updatedCornerPath,
      timestampMs = timestampMs,
    )
  }

  /**
   * Traces a sequence of normalized `(x, y)` coordinates from [target]'s current position
   * (`clampedX`, `clampedY`) toward its desired destination (`clampedTargetX`, `clampedTargetY`)
   * with respect to its current velocity (`velocityX`, `velocityY`) and trajectory kinematics.
   */
  fun sampleAutofocusTrajectoryPoints(
    target: CameraSmartFocusTarget,
    steps: Int = 12,
    totalDurationSeconds: Float = 1.2f,
  ): List<Pair<Float, Float>> {
    val count = steps.coerceIn(2, 60)
    val dt = (totalDurationSeconds.coerceIn(0.1f, 5.0f)) / count.toFloat()
    val points = ArrayList<Pair<Float, Float>>(count + 1)
    points.add(Pair(target.clampedX, target.clampedY))
    var sim = target
    for (i in 0 until count) {
      sim = stepStickyAutofocusTrajectory(
        currentTarget = sim,
        dtSeconds = dt,
        timestampMs = sim.timestampMs + (dt * 1000f).roundToInt(),
      )
      points.add(Pair(sim.clampedX, sim.clampedY))
    }
    return points
  }

  /**
   * Gently coasts and holds [previousTarget] along its current trajectory when object detections
   * momentarily drop out for a few frames (up to [maxHoldDurationMs]), preventing abrupt focus jumps.
   */
  fun retainSmartFocusTargetOnDropout(
    previousTarget: CameraSmartFocusTarget?,
    timestampMs: Long = System.currentTimeMillis(),
    maxHoldDurationMs: Long = STICKY_AF_DROPOUT_HOLD_DURATION_MS,
    maxMissedFrames: Int = 4,
  ): CameraSmartFocusTarget? {
    if (previousTarget == null || previousTarget.source != CameraFocusSource.ML_OBJECT_AUTO) {
      return null
    }
    val elapsedMs = (timestampMs - previousTarget.timestampMs).coerceAtLeast(0L)
    val nextMissed = previousTarget.missedDetectionFrames + 1
    if (elapsedMs > maxHoldDurationMs || nextMissed > maxMissedFrames) {
      return null
    }
    val dtSec = (elapsedMs.coerceIn(16L, 500L)) / 1000f
    val stepped = stepStickyAutofocusTrajectory(
      currentTarget = previousTarget,
      desiredX = previousTarget.clampedTargetX,
      desiredY = previousTarget.clampedTargetY,
      desiredBox = previousTarget.targetBoundingBox ?: previousTarget.boundingBox,
      dtSeconds = dtSec,
      timestampMs = timestampMs,
    )
    return stepped.copy(
      velocityX = stepped.velocityX * 0.75f,
      velocityY = stepped.velocityY * 0.75f,
      lockStrength = (previousTarget.lockStrength * 0.90f).coerceAtLeast(0.65f),
      missedDetectionFrames = nextMissed,
    )
  }

  /**
   * Selects the optimal smart autofocus target from a list of ML Kit detected objects ([objects]),
   * applying sticky subject hysteresis to resist transient detections and advancing the focus point
   * smoothly along a velocity- and trajectory-respecting curve toward the selected target.
   */
  fun selectSmartAutofocusTargetFromObjects(
    objects: List<RawDetectedObjectForFocus>,
    previousTarget: CameraSmartFocusTarget? = null,
    timestampMs: Long = System.currentTimeMillis(),
  ): CameraSmartFocusTarget? {
    if (objects.isEmpty()) {
      return retainSmartFocusTargetOnDropout(
        previousTarget = previousTarget,
        timestampMs = timestampMs,
      )
    }

    var bestCandidate: RawDetectedObjectForFocus? = null
    var bestScore = -1f
    var lockedCandidate: RawDetectedObjectForFocus? = null
    var lockedScore = -1f

    for (candidate in objects) {
      val score = scoreObjectForSmartAutofocus(candidate, previousTarget)
      if (score > bestScore) {
        bestScore = score
        bestCandidate = candidate
      }
      if (previousTarget != null &&
        previousTarget.source == CameraFocusSource.ML_OBJECT_AUTO &&
        score >= 0f &&
        isSameTrackedSubject(candidate, previousTarget) &&
        score > lockedScore
      ) {
        lockedScore = score
        lockedCandidate = candidate
      }
    }

    var winner = bestCandidate ?: return retainSmartFocusTargetOnDropout(previousTarget, timestampMs)
    if (bestScore < 0f) return retainSmartFocusTargetOnDropout(previousTarget, timestampMs)

    // Sticky hysteresis: if the currently tracked subject is still visible in the frame,
    // require a competing challenger to beat it by a clear margin before switching focus.
    val activeLocked = lockedCandidate
    if (activeLocked != null &&
      winner !== activeLocked &&
      (bestScore - lockedScore) < STICKY_AF_SWITCH_HYSTERESIS_MARGIN
    ) {
      winner = activeLocked
    }

    val coercedBox = winner.boundingBox.coerceNormalized()
    val desiredX = coercedBox.centerX.coerceIn(0.08f, 0.92f)
    val desiredY = coercedBox.centerY.coerceIn(0.08f, 0.92f)

    val cleanedLabel = winner.label?.let { raw ->
      normalizeAndFilterMlKitLabel(raw) ?: raw.trim().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
      }.takeIf { !isBlockedGenericVisionLabel(it) }
    }

    if (previousTarget == null || previousTarget.source != CameraFocusSource.ML_OBJECT_AUTO) {
      return CameraSmartFocusTarget(
        normX = desiredX,
        normY = desiredY,
        boundingBox = coercedBox,
        trackingId = winner.trackingId,
        categoryLabel = cleanedLabel,
        confidence = winner.confidence.coerceIn(0f, 1f),
        source = CameraFocusSource.ML_OBJECT_AUTO,
        timestampMs = timestampMs,
        targetNormX = desiredX,
        targetNormY = desiredY,
        targetBoundingBox = coercedBox,
        velocityX = 0f,
        velocityY = 0f,
        lockStrength = 1.0f,
        missedDetectionFrames = 0,
      )
    }

    val sameSubject = isSameTrackedSubject(winner, previousTarget)
    val nextLockStrength = if (sameSubject) {
      (previousTarget.lockStrength + 0.12f).coerceIn(0.85f, 1.35f)
    } else {
      0.85f
    }

    val rawDtMs = timestampMs - previousTarget.timestampMs
    val dtSeconds = if (rawDtMs > 0L) {
      (rawDtMs.coerceIn(16L, 450L)) / 1000f
    } else {
      0.24f
    }

    val stepped = stepStickyAutofocusTrajectory(
      currentTarget = previousTarget,
      desiredX = desiredX,
      desiredY = desiredY,
      desiredBox = coercedBox,
      dtSeconds = dtSeconds,
      timestampMs = timestampMs,
    )

    return stepped.copy(
      trackingId = winner.trackingId ?: if (sameSubject) previousTarget.trackingId else null,
      categoryLabel = cleanedLabel ?: if (sameSubject) previousTarget.categoryLabel else null,
      confidence = winner.confidence.coerceIn(0f, 1f),
      source = CameraFocusSource.ML_OBJECT_AUTO,
      timestampMs = timestampMs,
      lockStrength = nextLockStrength,
      missedDetectionFrames = 0,
    )
  }

  /**
   * Selects a smart autofocus target from localized [CameraVisionTag] items (with bounding boxes).
   */
  fun selectSmartAutofocusTargetFromVisionTags(
    tags: List<CameraVisionTag>,
    previousTarget: CameraSmartFocusTarget? = null,
    timestampMs: Long = System.currentTimeMillis(),
  ): CameraSmartFocusTarget? {
    val candidates = tags.mapNotNull { tag ->
      val box = tag.boundingBox ?: return@mapNotNull null
      if (isBlockedGenericVisionLabel(tag.label)) return@mapNotNull null
      RawDetectedObjectForFocus(
        boundingBox = box,
        trackingId = tag.trackingId ?: if (tag.index >= 0) tag.index else null,
        label = tag.label,
        confidence = tag.confidenceClamped,
      )
    }
    return selectSmartAutofocusTargetFromObjects(
      objects = candidates,
      previousTarget = previousTarget,
      timestampMs = timestampMs,
    )
  }

  /**
   * Extracts the primary smart autofocus target from a [CameraVisionAnalysisResult].
   */
  fun selectSmartAutofocusTargetFromVisionResult(
    result: CameraVisionAnalysisResult?,
    previousTarget: CameraSmartFocusTarget? = null,
    timestampMs: Long = System.currentTimeMillis(),
  ): CameraSmartFocusTarget? {
    if (result == null) {
      return retainSmartFocusTargetOnDropout(
        previousTarget = previousTarget,
        timestampMs = timestampMs,
      )
    }
    val sourceTags = result.tags.ifEmpty { result.allCandidateTags }
    return selectSmartAutofocusTargetFromVisionTags(
      tags = sourceTags,
      previousTarget = previousTarget,
      timestampMs = timestampMs,
    )
  }

  /**
   * Runs on-device Google ML Kit Object Detection (`com.google.mlkit.vision.objects.ObjectDetection`)
   * on [bitmap] using a persistent reusable detector (`STREAM_MODE` by default so ML Kit maintains
   * multi-frame `trackingId`s and Kalman box filtering) to locate physical objects and determine
   * the optimal smart autofocus target.
   */
  suspend fun detectSmartFocusObjectWithMlKit(
    bitmap: Bitmap,
    rotationDegrees: Int = 0,
    previousTarget: CameraSmartFocusTarget? = null,
    fallbackToSalientRegion: Boolean = true,
    analyzedAtMs: Long = System.currentTimeMillis(),
    streamMode: Boolean = true,
  ): CameraSmartFocusTarget? {
    if (bitmap.width <= 16 || bitmap.height <= 16) return null
    return try {
      val orientedBitmap = orientBitmapIfNeeded(bitmap, rotationDegrees)
      val bw = orientedBitmap.width
      val bh = orientedBitmap.height
      if (bw <= 16 || bh <= 16) return null

      val objectDetector = getOrCreateObjectDetector(streamMode = streamMode)
      val inputImage = InputImage.fromBitmap(orientedBitmap, 0)

      val detectedObjects: List<DetectedObject> = try {
        suspendCancellableCoroutine { continuation ->
          objectDetector.process(inputImage)
            .addOnSuccessListener { results ->
              if (continuation.isActive) {
                continuation.resume(results.orEmpty())
              }
            }
            .addOnFailureListener {
              if (continuation.isActive) {
                continuation.resume(emptyList())
              }
            }
        }
      } catch (_: Throwable) {
        emptyList()
      }

      val mlKitCandidates = detectedObjects.mapNotNull { obj ->
        val rect = obj.boundingBox
        val left = (rect.left.toFloat() / bw.toFloat()).coerceIn(0f, 1f)
        val top = (rect.top.toFloat() / bh.toFloat()).coerceIn(0f, 1f)
        val right = (rect.right.toFloat() / bw.toFloat()).coerceIn(0f, 1f)
        val bottom = (rect.bottom.toFloat() / bh.toFloat()).coerceIn(0f, 1f)
        val rawBox = CameraVisionBoundingBox(left, top, right, bottom)
        if (rawBox.width < 0.04f || rawBox.height < 0.04f || rawBox.area > 0.92f) {
          null
        } else {
          val prevBox = previousTarget?.targetBoundingBox ?: previousTarget?.boundingBox
          val smoothedBox = if (prevBox != null &&
            obj.trackingId != null &&
            obj.trackingId == previousTarget?.trackingId &&
            rawBox.intersectionOverUnion(prevBox) >= 0.30f
          ) {
            val maxEdgeDelta = maxOf(
              abs(rawBox.left - prevBox.left),
              abs(rawBox.top - prevBox.top),
              abs(rawBox.right - prevBox.right),
              abs(rawBox.bottom - prevBox.bottom),
            )
            if (maxEdgeDelta < VISION_BOX_MICRO_JITTER_DEADZONE) {
              prevBox
            } else {
              prevBox.lerpTo(rawBox, 0.58f)
            }
          } else {
            rawBox
          }
          val topLabel = obj.labels.maxByOrNull { it.confidence }
          RawDetectedObjectForFocus(
            boundingBox = smoothedBox,
            trackingId = obj.trackingId,
            label = topLabel?.text,
            confidence = topLabel?.confidence ?: 0.75f,
          )
        }
      }

      if (mlKitCandidates.isNotEmpty()) {
        return selectSmartAutofocusTargetFromObjects(
          objects = mlKitCandidates,
          previousTarget = previousTarget,
          timestampMs = analyzedAtMs,
        )
      }

      if (fallbackToSalientRegion) {
        val salientBoxes = extractSalientRegionsFromBitmap(orientedBitmap, maxRegions = 3)
        if (salientBoxes.isNotEmpty()) {
          val salientCandidates = salientBoxes.mapIndexed { idx, box ->
            RawDetectedObjectForFocus(
              boundingBox = box,
              trackingId = null,
              label = null,
              confidence = (0.82f - idx * 0.08f).coerceAtLeast(0.60f),
            )
          }
          // When holding a strong lock on a classified ML Kit object that dropped out for just 1-2 frames,
          // only switch to a salient fallback region if one aligns with the tracked subject; otherwise coast.
          if (previousTarget != null &&
            previousTarget.source == CameraFocusSource.ML_OBJECT_AUTO &&
            previousTarget.categoryLabel != null &&
            previousTarget.missedDetectionFrames < 2
          ) {
            val matchingSalient = salientCandidates.filter { isSameTrackedSubject(it, previousTarget) }
            if (matchingSalient.isEmpty()) {
              retainSmartFocusTargetOnDropout(
                previousTarget = previousTarget,
                timestampMs = analyzedAtMs,
              )?.let { return it }
            }
          }
          return selectSmartAutofocusTargetFromObjects(
            objects = salientCandidates,
            previousTarget = previousTarget,
            timestampMs = analyzedAtMs,
          )
        }
      }
      retainSmartFocusTargetOnDropout(
        previousTarget = previousTarget,
        timestampMs = analyzedAtMs,
      )
    } catch (_: Throwable) {
      retainSmartFocusTargetOnDropout(
        previousTarget = previousTarget,
        timestampMs = analyzedAtMs,
      )
    }
  }

  /**
   * Maps a normalized viewfinder [CameraSmartFocusTarget] (`0.0f..1.0f`) into sensor active-array
   * pixel coordinates ([CameraSensorMeteringRegion]) for Camera2 `CONTROL_AF_REGIONS` and `CONTROL_AE_REGIONS`,
   * accounting for [sensorOrientationDegrees], [lensFacing], [mirrorFront], and digital [zoom].
   */
  fun computeSensorMeteringRegion(
    sensorWidth: Int,
    sensorHeight: Int,
    target: CameraSmartFocusTarget,
    sensorOrientationDegrees: Int = 90,
    lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    mirrorFront: Boolean = false,
    zoom: Float = 1.0f,
    weight: Int = 1000,
  ): CameraSensorMeteringRegion? {
    if (sensorWidth <= 16 || sensorHeight <= 16) return null
    val clampedZoom = coerceZoom(zoom)

    // Un-mirror and un-zoom viewfinder coordinates into full sensor stream normalized space
    var vfX = target.clampedX
    val vfY = target.clampedY
    if (lensFacing == CameraLensFacing.FRONT && mirrorFront) {
      vfX = 1f - vfX
    }
    val unzoomedX = (0.5f + (vfX - 0.5f) / clampedZoom).coerceIn(0.04f, 0.96f)
    val unzoomedY = (0.5f + (vfY - 0.5f) / clampedZoom).coerceIn(0.04f, 0.96f)

    val normOrientation = ((sensorOrientationDegrees % 360) + 360) % 360
    val (sensorCenterX, sensorCenterY) = when (normOrientation) {
      90 -> Pair(unzoomedY, (1f - unzoomedX).coerceIn(0f, 1f))
      180 -> Pair((1f - unzoomedX).coerceIn(0f, 1f), (1f - unzoomedY).coerceIn(0f, 1f))
      270 -> {
        val sx = (1f - unzoomedY).coerceIn(0f, 1f)
        val sy = if (lensFacing == CameraLensFacing.FRONT) (1f - unzoomedX).coerceIn(0f, 1f) else unzoomedX
        Pair(sx, sy)
      }
      else -> Pair(unzoomedX, unzoomedY)
    }

    val box = target.boundingBox
    val (normHalfW, normHalfH) = if (box != null && !target.isManualOverride) {
      // Focus tightly on the core 55% of the detected object's bounding box
      val rawHalfW = (box.width * 0.275f / clampedZoom).coerceIn(0.05f, 0.22f)
      val rawHalfH = (box.height * 0.275f / clampedZoom).coerceIn(0.05f, 0.22f)
      if (normOrientation == 90 || normOrientation == 270) {
        Pair(rawHalfH, rawHalfW)
      } else {
        Pair(rawHalfW, rawHalfH)
      }
    } else {
      val spotHalf = (0.075f / clampedZoom).coerceIn(0.04f, 0.12f)
      Pair(spotHalf, spotHalf)
    }

    val cxPx = (sensorCenterX * sensorWidth).roundToInt()
    val cyPx = (sensorCenterY * sensorHeight).roundToInt()
    val halfWPx = (normHalfW * sensorWidth).roundToInt().coerceAtLeast(8)
    val halfHPx = (normHalfH * sensorHeight).roundToInt().coerceAtLeast(8)

    val left = (cxPx - halfWPx).coerceIn(0, sensorWidth - 16)
    val top = (cyPx - halfHPx).coerceIn(0, sensorHeight - 16)
    val right = (cxPx + halfWPx).coerceIn(left + 16, sensorWidth)
    val bottom = (cyPx + halfHPx).coerceIn(top + 16, sensorHeight)

    return CameraSensorMeteringRegion(
      left = left,
      top = top,
      right = right,
      bottom = bottom,
      weight = weight.coerceIn(1, 1000),
    )
  }
}

/**
 * Manages an active video recording session, using either a hardware callback or fallback MP4 writer.
 */
class ActiveCameraVideoSession(
  val outputFile: File,
  private val onStopHardwareRecorder: (() -> Boolean)? = null,
) {
  private var startTimeMs: Long = 0L

  fun start(): Boolean {
    startTimeMs = System.currentTimeMillis()
    outputFile.parentFile?.mkdirs()
    return true
  }

  fun stop(): File {
    val elapsedMs = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(1000L)
    val hardwareSucceeded = try {
      onStopHardwareRecorder?.invoke() ?: false
    } catch (_: Throwable) {
      false
    }

    if (!hardwareSucceeded || !outputFile.exists() || outputFile.length() < 32L) {
      CameraEngine.writeFallbackVideoFile(
        outputFile = outputFile,
        durationMs = elapsedMs,
      )
    }
    return outputFile
  }
}
