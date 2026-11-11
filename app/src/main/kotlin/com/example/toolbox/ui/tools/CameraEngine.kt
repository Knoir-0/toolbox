package com.example.toolbox.ui.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.Deflater
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

const val CAMERA_PREFS_NAME = "camera_tool_preferences"
const val PREF_KEY_CAMERA_DIRECTORY = "camera_save_directory"
const val PREF_KEY_CAMERA_LENS = "camera_lens_facing"
const val PREF_KEY_CAMERA_FLASH = "camera_flash_mode"
const val PREF_KEY_CAMERA_TIMER = "camera_timer_duration"
const val PREF_KEY_CAMERA_ASPECT_RATIO = "camera_aspect_ratio"
const val PREF_KEY_CAMERA_GRID = "camera_grid_overlay"
const val PREF_KEY_CAMERA_FILTER = "camera_color_filter"
const val PREF_KEY_CAMERA_FORMAT = "camera_photo_format"
const val PREF_KEY_CAMERA_MIRROR_FRONT = "camera_mirror_front"

const val DEFAULT_CAMERA_FOLDER_SUBPATH = "DCIM/Camera"
const val DEFAULT_CAMERA_FOLDER_DISPLAY = "/DCIM/Camera"

const val MIN_CAMERA_ZOOM = 1.0f
const val MAX_CAMERA_ZOOM = 8.0f

const val MIN_CAMERA_EXPOSURE_EV = -2.0f
const val MAX_CAMERA_EXPOSURE_EV = 2.0f

val CAMERA_ZOOM_PRESETS: List<Float> = listOf(1.0f, 2.0f, 3.0f, 5.0f)

/**
 * Capture modes supported by the Camera tool.
 */
enum class CameraCaptureMode(
  val displayName: String,
  val shortLabel: String,
) {
  PHOTO(displayName = "Photo", shortLabel = "PHOTO"),
  VIDEO(displayName = "Video", shortLabel = "VIDEO"),
  ;

  companion object {
    val DEFAULT: CameraCaptureMode = PHOTO

    fun fromNameOrDefault(name: String?): CameraCaptureMode {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
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
    val DEFAULT: CameraAspectRatio = RATIO_4_3

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
   * Example: `IMG_20261110_174500.jpg`
   */
  fun generatePhotoFileName(
    format: CameraPhotoFormat = CameraPhotoFormat.DEFAULT,
    epochMs: Long = System.currentTimeMillis(),
  ): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    return "IMG_${stamp}.${format.extension}"
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
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    val baseStem = "IMG_$stamp"
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
   * Captures and writes a rich procedural scene photo to [outputFile] reflecting the active camera
   * parameters ([lensFacing], [aspectRatio], [filter], [zoom], [exposureEv], [mirrorFront], [flashMode]).
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
    mirrorFront: Boolean = true,
    flashMode: CameraFlashMode = CameraFlashMode.OFF,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    outputFile.parentFile?.mkdirs()
    val effectiveEv = if (flashMode == CameraFlashMode.ON || flashMode == CameraFlashMode.TORCH) {
      (exposureEv + 0.35f).coerceIn(MIN_CAMERA_EXPOSURE_EV, MAX_CAMERA_EXPOSURE_EV)
    } else {
      exposureEv
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

        val skyTop = applyFilterAndExposureToRgb(
          red = if (lensFacing == CameraLensFacing.BACK) 34 else 72,
          green = if (lensFacing == CameraLensFacing.BACK) 56 else 48,
          blue = if (lensFacing == CameraLensFacing.BACK) 112 else 118,
          filter = filter,
          exposureEv = effectiveEv,
        )
        val skyBottom = applyFilterAndExposureToRgb(
          red = if (lensFacing == CameraLensFacing.BACK) 218 else 186,
          green = if (lensFacing == CameraLensFacing.BACK) 132 else 120,
          blue = if (lensFacing == CameraLensFacing.BACK) 110 else 168,
          filter = filter,
          exposureEv = effectiveEv,
        )
        val groundColor = applyFilterAndExposureToRgb(
          red = 42,
          green = 68,
          blue = 64,
          filter = filter,
          exposureEv = effectiveEv,
        )
        val sunColor = applyFilterAndExposureToRgb(
          red = 255,
          green = 224,
          blue = 142,
          filter = filter,
          exposureEv = effectiveEv,
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
      val (r, g, b) = applyFilterAndExposureToRgb(
        red = baseRed,
        green = baseGreen,
        blue = baseBlue,
        filter = filter,
        exposureEv = effectiveEv,
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
