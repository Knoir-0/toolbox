package com.example.toolbox.ui.tools

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

const val MIN_VIDEO_TRIM_DURATION_MS = 200L
const val DEFAULT_VIDEO_EXPORT_SUBFOLDER = "Movies/Toolbox"

/**
 * Supported editing workspace tabs in the Video Editor.
 */
enum class VideoEditorMode(val label: String) {
  TRIM("Trim"),
  CROP("Crop"),
  TRANSFORM("Rotate"),
  SPEED("Speed"),
  ADJUST("Filters"),
}

/**
 * Curated visual filter presets mapped to both real-time GPU [ColorMatrix] preview
 * and LGPL-compatible FFmpeg video filters.
 */
enum class VideoFilterPreset(val label: String) {
  NONE("Normal"),
  BW("B&W"),
  WARM("Warm"),
  COOL("Cool"),
  SEPIA("Sepia"),
  VINTAGE("Vintage"),
  VIVID("Vivid"),
  INVERT("Invert"),
}

/**
 * Target container/media formats supported by Video Editor export.
 */
enum class VideoExportFormat(
  val displayName: String,
  val shortLabel: String,
  val extension: String,
  val mimeType: String,
  val description: String,
) {
  MP4(
    displayName = "MP4 Video",
    shortLabel = "MP4",
    extension = "mp4",
    mimeType = "video/mp4",
    description = "Universal MPEG-4 video with fast-start playback",
  ),
  GIF(
    displayName = "Animated GIF",
    shortLabel = "GIF",
    extension = "gif",
    mimeType = "image/gif",
    description = "Looping Lanczos-scaled animated GIF",
  ),
  M4A(
    displayName = "Audio Only (M4A)",
    shortLabel = "M4A",
    extension = "m4a",
    mimeType = "audio/mp4",
    description = "Extract edited audio track as AAC M4A",
  ),
}

/**
 * Output resolution scaling presets. Never upscales smaller source clips.
 */
enum class VideoResolutionPreset(
  val label: String,
  val maxDimension: Int?,
  val baseBitrateBps: Int,
  val mpeg4QScale: Int,
) {
  ORIGINAL("Original", null, 8_000_000, 3),
  P1080("1080p", 1920, 6_000_000, 4),
  P720("720p", 1280, 3_500_000, 5),
  P480("480p", 854, 1_500_000, 6),
}

/**
 * Encoding bitrate / quantization presets for balancing file size and visual fidelity.
 */
enum class VideoQualityPreset(
  val label: String,
  val bitrateMultiplier: Float,
  val qScaleOffset: Int,
) {
  HIGH("High", 1.35f, -1),
  BALANCED("Balanced", 1.0f, 0),
  COMPACT("Compact", 0.55f, 3),
}

/**
 * Underlying FFmpeg execution tier selected for maximum performance.
 */
enum class VideoProcessingStrategy(
  val badgeLabel: String,
  val description: String,
) {
  STREAM_COPY(
    badgeLabel = "Instant Stream Copy",
    description = "Lossless zero-reencode FFmpeg packet copy",
  ),
  HARDWARE_FFMPEG(
    badgeLabel = "Hardware H.264 + FFmpeg",
    description = "Hardware MediaCodec H.264 with FFmpeg filtergraph",
  ),
  SOFTWARE_FFMPEG(
    badgeLabel = "FFmpeg Multi-Codec",
    description = "FFmpeg software multi-codec pipeline",
  ),
}

/**
 * Probed metadata of an opened video file.
 */
data class VideoMetadata(
  val file: File,
  val durationMs: Long,
  val width: Int,
  val height: Int,
  val rotationDegrees: Int = 0,
  val frameRate: Float = 30f,
  val bitrateBps: Long = 0L,
  val hasAudio: Boolean = true,
  val videoCodec: String? = null,
  val sizeBytes: Long = file.length(),
) {
  val displayWidth: Int
    get() = if (rotationDegrees == 90 || rotationDegrees == 270) height else width

  val displayHeight: Int
    get() = if (rotationDegrees == 90 || rotationDegrees == 270) width else height

  val resolutionLabel: String
    get() = if (displayWidth > 0 && displayHeight > 0) "${displayWidth}×${displayHeight}" else "Unknown"

  val durationFormatted: String
    get() = VideoEditorEngine.formatTimecode(durationMs, includeTenths = false)

  val sizeFormatted: String
    get() = FileManagerEngine.formatFileSize(sizeBytes)
}

/**
 * Immutable non-destructive editing state for the Video Editor, supporting lightweight
 * undo/redo snapshots without duplicating video buffers in memory.
 */
data class VideoEditState(
  val durationMs: Long = 0L,
  val trimStartMs: Long = 0L,
  val trimEndMs: Long = 0L,
  val cropRect: NormalizedRect = NormalizedRect(0f, 0f, 1f, 1f),
  val aspectPreset: AspectRatioPreset = AspectRatioPreset.FREE,
  val rotationDegrees: Int = 0,
  val flipHorizontal: Boolean = false,
  val flipVertical: Boolean = false,
  val playbackSpeed: Float = 1.0f,
  val isMuted: Boolean = false,
  val volume: Float = 1.0f,
  val fadeInOut: Boolean = false,
  val brightness: Float = 0f, // -100f..100f
  val contrast: Float = 1f, // 0.5f..1.5f
  val saturation: Float = 1f, // 0f..2f
  val filterPreset: VideoFilterPreset = VideoFilterPreset.NONE,
) {
  val normalizedRotation: Int
    get() = ((rotationDegrees % 360) + 360) % 360

  val effectiveTrimEndMs: Long
    get() = if (durationMs > 0L && trimEndMs <= 0L) durationMs else trimEndMs

  val trimmedSourceDurationMs: Long
    get() = (effectiveTrimEndMs - trimStartMs).coerceAtLeast(1L)

  val estimatedOutputDurationMs: Long
    get() {
      val safeSpeed = playbackSpeed.coerceIn(0.25f, 4.0f)
      return (trimmedSourceDurationMs / safeSpeed).roundToLong().coerceAtLeast(1L)
    }

  val hasTrimChanges: Boolean
    get() = durationMs > 0L && (trimStartMs > 20L || (durationMs - effectiveTrimEndMs) > 20L)

  val hasCropChanges: Boolean
    get() = cropRect.left > 0.005f ||
      cropRect.top > 0.005f ||
      cropRect.right < 0.995f ||
      cropRect.bottom < 0.995f

  val hasTransformChanges: Boolean
    get() = normalizedRotation != 0 || flipHorizontal || flipVertical

  val hasSpeedChanges: Boolean
    get() = abs(playbackSpeed - 1.0f) > 0.01f

  val hasAudioFilterChanges: Boolean
    get() = !isMuted && (hasSpeedChanges || abs(volume - 1.0f) > 0.01f || fadeInOut)

  val hasColorChanges: Boolean
    get() = filterPreset != VideoFilterPreset.NONE ||
      abs(brightness) > 0.5f ||
      abs(contrast - 1.0f) > 0.01f ||
      abs(saturation - 1.0f) > 0.01f

  val hasVisualReencodeChanges: Boolean
    get() = hasCropChanges ||
      hasTransformChanges ||
      hasSpeedChanges ||
      fadeInOut ||
      hasColorChanges

  val hasAnyChanges: Boolean
    get() = hasTrimChanges ||
      hasCropChanges ||
      hasTransformChanges ||
      hasSpeedChanges ||
      isMuted ||
      abs(volume - 1.0f) > 0.01f ||
      fadeInOut ||
      hasColorChanges

  fun coerceForDuration(totalDurationMs: Long): VideoEditState {
    if (totalDurationMs <= 0L) return copy(durationMs = 0L, trimStartMs = 0L, trimEndMs = 0L)
    val minClip = min(MIN_VIDEO_TRIM_DURATION_MS, totalDurationMs)
    val rawEnd = if (trimEndMs <= 0L) totalDurationMs else trimEndMs
    val safeStart = trimStartMs.coerceIn(0L, (totalDurationMs - minClip).coerceAtLeast(0L))
    val safeEnd = rawEnd.coerceIn(safeStart + minClip, totalDurationMs)
    return copy(
      durationMs = totalDurationMs,
      trimStartMs = safeStart,
      trimEndMs = safeEnd,
      cropRect = cropRect.coerceNormalized(),
      rotationDegrees = normalizedRotation,
      playbackSpeed = playbackSpeed.coerceIn(0.25f, 4.0f),
      volume = volume.coerceIn(0f, 2.0f),
      brightness = brightness.coerceIn(-100f, 100f),
      contrast = contrast.coerceIn(0.5f, 1.5f),
      saturation = saturation.coerceIn(0f, 2.0f),
    )
  }

  companion object {
    fun initialForDuration(durationMs: Long): VideoEditState {
      val safeDuration = durationMs.coerceAtLeast(0L)
      return VideoEditState(
        durationMs = safeDuration,
        trimStartMs = 0L,
        trimEndMs = safeDuration,
      )
    }
  }
}

/**
 * User-selected export settings.
 */
data class VideoExportOptions(
  val format: VideoExportFormat = VideoExportFormat.MP4,
  val resolution: VideoResolutionPreset = VideoResolutionPreset.ORIGINAL,
  val quality: VideoQualityPreset = VideoQualityPreset.BALANCED,
  val preferFastStreamCopy: Boolean = true,
)

/**
 * Result of a completed, cancelled, or failed video export operation.
 */
data class VideoExportResult(
  val success: Boolean,
  val cancelled: Boolean = false,
  val outputFile: File?,
  val galleryUri: Uri? = null,
  val strategyUsed: VideoProcessingStrategy,
  val usedFallback: Boolean,
  val elapsedMs: Long,
  val outputSizeBytes: Long,
  val message: String,
)

/**
 * Core processing and command-generation engine for the Video Editor tool.
 *
 * Performance architecture:
 * 1. **Zero-Reencode Stream Copy (`-c copy`)**: Used automatically for pure Trim and/or Mute
 *    operations on MP4/MOV/M4V containers when resolution and visual/audio filters are untouched,
 *    completing exports in milliseconds with zero quality loss.
 * 2. **Hardware-Accelerated FFmpeg (`h264_mediacodec`)**: Used as primary video encoder when
 *    filtergraphs (crop, rotate/flip, speed, color filters, scaling, fades) are active.
 * 3. **Automatic Software Fallback (`mpeg4`)**: Seamlessly retries with FFmpeg's universal
 *    software encoder if hardware codecs are busy or unsupported on an emulator/device.
 */
object VideoEditorEngine {

  val SUPPORTED_VIDEO_EXTENSIONS: Set<String> = setOf(
    "mp4", "m4v", "mkv", "mov", "qt", "webm", "avi", "3gp", "3gpp", "flv", "mpeg", "mpg", "ts", "mts", "m2ts",
  )

  private val STREAM_COPY_VIDEO_EXTENSIONS: Set<String> = setOf("mp4", "m4v", "mov")

  /**
   * Returns `true` if [file] has a recognized video extension.
   */
  fun isVideoFile(file: File): Boolean {
    if (file.isDirectory) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in SUPPORTED_VIDEO_EXTENSIONS || FileManagerEngine.isVideoFile(file)
  }

  /**
   * Formats a millisecond timestamp as `MM:SS.t` (or `HH:MM:SS.t` for clips >= 1 hour).
   */
  fun formatTimecode(ms: Long, includeTenths: Boolean = true): String {
    val clamped = ms.coerceAtLeast(0L)
    val totalSeconds = clamped / 1000L
    val tenths = (clamped % 1000L) / 100L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0L) {
      if (includeTenths) {
        String.format(Locale.US, "%d:%02d:%02d.%d", hours, minutes, seconds, tenths)
      } else {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
      }
    } else {
      if (includeTenths) {
        String.format(Locale.US, "%02d:%02d.%d", minutes, seconds, tenths)
      } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
      }
    }
  }

  /**
   * Formats a millisecond value as decimal seconds (`1.250`) for FFmpeg `-ss` / `-t` flags.
   */
  fun formatFfmpegSeconds(ms: Long): String {
    val sec = ms.coerceAtLeast(0L) / 1000.0
    return String.format(Locale.US, "%.3f", sec)
  }

  /**
   * Extracts the filename stem without its extension.
   */
  fun extractFileStem(fileName: String): String {
    val trimmed = fileName.trim()
    val dotIdx = trimmed.lastIndexOf('.')
    val raw = if (dotIdx > 0) trimmed.substring(0, dotIdx) else trimmed
    return raw.ifBlank { "edited_video" }
  }

  /**
   * Resolves a non-colliding output file inside [outputDir].
   */
  fun resolveOutputFile(
    outputDir: File,
    desiredStem: String,
    extension: String,
    sourceFile: File? = null,
  ): File {
    val safeStem = desiredStem
      .replace(Regex("[\\\\/:*?\"<>|]"), "_")
      .trim()
      .removeSuffix(".$extension")
      .ifBlank { "edited_video" }
    val cleanExt = extension.trim().removePrefix(".").lowercase(Locale.ROOT).ifBlank { "mp4" }
    var candidate = File(outputDir, "$safeStem.$cleanExt")
    var index = 1
    while (candidate.exists() || (sourceFile != null && candidate.absolutePath == sourceFile.absolutePath)) {
      candidate = File(outputDir, "${safeStem}_$index.$cleanExt")
      index++
    }
    return candidate
  }

  /**
   * Selects the optimal [VideoProcessingStrategy] for the requested edits and export options.
   */
  fun determineStrategy(
    metadata: VideoMetadata?,
    editState: VideoEditState,
    options: VideoExportOptions,
  ): VideoProcessingStrategy {
    val ext = metadata?.file?.extension?.lowercase(Locale.ROOT).orEmpty()

    if (options.format == VideoExportFormat.GIF) {
      return VideoProcessingStrategy.SOFTWARE_FFMPEG
    }

    if (options.format == VideoExportFormat.M4A) {
      val canCopyAudio = options.preferFastStreamCopy &&
        !editState.isMuted &&
        !editState.hasAudioFilterChanges &&
        ext in STREAM_COPY_VIDEO_EXTENSIONS
      return if (canCopyAudio) {
        VideoProcessingStrategy.STREAM_COPY
      } else {
        VideoProcessingStrategy.SOFTWARE_FFMPEG
      }
    }

    // MP4 output
    val canStreamCopyMp4 = options.preferFastStreamCopy &&
      options.resolution == VideoResolutionPreset.ORIGINAL &&
      !editState.hasVisualReencodeChanges &&
      !editState.hasAudioFilterChanges &&
      (metadata == null || ext in STREAM_COPY_VIDEO_EXTENSIONS)

    return if (canStreamCopyMp4) {
      VideoProcessingStrategy.STREAM_COPY
    } else {
      VideoProcessingStrategy.HARDWARE_FFMPEG
    }
  }

  /**
   * Computes post-rotation dimensions (`width` to `height`) before cropping/scaling.
   */
  fun computePostRotationDimensions(
    sourceDisplayWidth: Int,
    sourceDisplayHeight: Int,
    userRotationDegrees: Int,
  ): Pair<Int, Int> {
    val norm = ((userRotationDegrees % 360) + 360) % 360
    return if (norm == 90 || norm == 270) {
      sourceDisplayHeight to sourceDisplayWidth
    } else {
      sourceDisplayWidth to sourceDisplayHeight
    }
  }

  /**
   * Computes even-aligned pixel crop coordinates `(x, y, width, height)` required by YUV 4:2:0 encoders.
   */
  fun computeEvenCropPixels(
    cropRect: NormalizedRect,
    frameWidth: Int,
    frameHeight: Int,
  ): ImagePixelRect {
    val safeFrameW = max(2, frameWidth - (frameWidth % 2))
    val safeFrameH = max(2, frameHeight - (frameHeight % 2))
    val clamped = cropRect.coerceNormalized()

    var x = ((clamped.left * safeFrameW).roundToInt() / 2) * 2
    var y = ((clamped.top * safeFrameH).roundToInt() / 2) * 2
    x = x.coerceIn(0, max(0, safeFrameW - 2))
    y = y.coerceIn(0, max(0, safeFrameH - 2))

    var w = (((clamped.width * safeFrameW).roundToInt().coerceAtLeast(2)) / 2) * 2
    var h = (((clamped.height * safeFrameH).roundToInt().coerceAtLeast(2)) / 2) * 2
    w = w.coerceIn(2, safeFrameW - x)
    h = h.coerceIn(2, safeFrameH - y)
    if (w % 2 != 0) w = max(2, w - 1)
    if (h % 2 != 0) h = max(2, h - 1)

    return ImagePixelRect(x = x, y = y, width = w, height = h)
  }

  /**
   * Constructs an Android [ColorMatrix] for real-time 60fps GPU preview on `TextureView`
   * and frame snapshot export.
   */
  fun buildPreviewColorMatrix(editState: VideoEditState): ColorMatrix {
    val matrix = ColorMatrix()

    when (editState.filterPreset) {
      VideoFilterPreset.NONE -> {}
      VideoFilterPreset.BW -> {
        matrix.setSaturation(0f)
      }
      VideoFilterPreset.SEPIA -> {
        matrix.postConcat(
          ColorMatrix(
            floatArrayOf(
              0.393f, 0.769f, 0.189f, 0f, 0f,
              0.349f, 0.686f, 0.168f, 0f, 0f,
              0.272f, 0.534f, 0.131f, 0f, 0f,
              0f, 0f, 0f, 1f, 0f,
            )
          )
        )
      }
      VideoFilterPreset.WARM -> {
        matrix.postConcat(
          ColorMatrix(
            floatArrayOf(
              1.15f, 0f, 0f, 0f, 12f,
              0f, 1.04f, 0f, 0f, 4f,
              0f, 0f, 0.85f, 0f, -12f,
              0f, 0f, 0f, 1f, 0f,
            )
          )
        )
      }
      VideoFilterPreset.COOL -> {
        matrix.postConcat(
          ColorMatrix(
            floatArrayOf(
              0.86f, 0f, 0f, 0f, -10f,
              0f, 1.0f, 0f, 0f, 0f,
              0f, 0f, 1.18f, 0f, 14f,
              0f, 0f, 0f, 1f, 0f,
            )
          )
        )
      }
      VideoFilterPreset.VINTAGE -> {
        matrix.postConcat(
          ColorMatrix(
            floatArrayOf(
              0.9f, 0.08f, 0.04f, 0f, 16f,
              0.04f, 0.84f, 0.06f, 0f, 10f,
              0.03f, 0.06f, 0.72f, 0f, 6f,
              0f, 0f, 0f, 1f, 0f,
            )
          )
        )
      }
      VideoFilterPreset.VIVID -> {
        val vividSat = ColorMatrix().apply { setSaturation(1.38f) }
        matrix.postConcat(vividSat)
      }
      VideoFilterPreset.INVERT -> {
        matrix.postConcat(
          ColorMatrix(
            floatArrayOf(
              -1f, 0f, 0f, 0f, 255f,
              0f, -1f, 0f, 0f, 255f,
              0f, 0f, -1f, 0f, 255f,
              0f, 0f, 0f, 1f, 0f,
            )
          )
        )
      }
    }

    if (abs(editState.saturation - 1f) > 0.01f && editState.filterPreset != VideoFilterPreset.BW) {
      val satMatrix = ColorMatrix().apply { setSaturation(editState.saturation.coerceIn(0f, 2f)) }
      matrix.postConcat(satMatrix)
    }

    if (abs(editState.contrast - 1f) > 0.01f || abs(editState.brightness) > 0.5f) {
      val c = editState.contrast.coerceIn(0.5f, 1.5f)
      val b = editState.brightness.coerceIn(-100f, 100f)
      val translate = b + (1f - c) * 128f
      matrix.postConcat(
        ColorMatrix(
          floatArrayOf(
            c, 0f, 0f, 0f, translate,
            0f, c, 0f, 0f, translate,
            0f, 0f, c, 0f, translate,
            0f, 0f, 0f, 1f, 0f,
          )
        )
      )
    }

    return matrix
  }

  /**
   * Builds the LGPL-verified FFmpeg `-vf` filter stages for [editState] and [options].
   */
  fun buildVideoFilterStages(
    metadata: VideoMetadata?,
    editState: VideoEditState,
    options: VideoExportOptions,
  ): List<String> {
    val filters = mutableListOf<String>()

    // 1. User Rotation
    when (editState.normalizedRotation) {
      90 -> filters.add("transpose=1")
      180 -> {
        filters.add("hflip")
        filters.add("vflip")
      }
      270 -> filters.add("transpose=2")
    }

    // 2. Post-rotation Flips
    if (editState.flipHorizontal) {
      filters.add("hflip")
    }
    if (editState.flipVertical) {
      filters.add("vflip")
    }

    // 3. Crop
    if (editState.hasCropChanges) {
      val srcW = metadata?.displayWidth ?: 0
      val srcH = metadata?.displayHeight ?: 0
      val (rotW, rotH) = computePostRotationDimensions(srcW, srcH, editState.normalizedRotation)
      if (rotW >= 4 && rotH >= 4) {
        val px = computeEvenCropPixels(editState.cropRect, rotW, rotH)
        filters.add("crop=${px.width}:${px.height}:${px.x}:${px.y}")
      } else {
        val c = editState.cropRect.coerceNormalized()
        val wExpr = String.format(Locale.US, "max(2,trunc(iw*%.4f/2)*2)", c.width)
        val hExpr = String.format(Locale.US, "max(2,trunc(ih*%.4f/2)*2)", c.height)
        val xExpr = String.format(Locale.US, "trunc(iw*%.4f/2)*2", c.left)
        val yExpr = String.format(Locale.US, "trunc(ih*%.4f/2)*2", c.top)
        filters.add("crop=$wExpr:$hExpr:$xExpr:$yExpr")
      }
    }

    // 4. Resolution Scaling (and GIF frame rate / width optimization)
    if (options.format == VideoExportFormat.GIF) {
      val gifMaxDim = options.resolution.maxDimension?.coerceAtMost(640) ?: 480
      filters.add("fps=12")
      filters.add(
        "scale='max(2,trunc(min(1,min($gifMaxDim/iw,$gifMaxDim/ih))*iw/2)*2)':'max(2,trunc(min(1,min($gifMaxDim/iw,$gifMaxDim/ih))*ih/2)*2)':flags=lanczos"
      )
    } else {
      val maxDim = options.resolution.maxDimension
      if (maxDim != null) {
        filters.add(
          "scale='max(2,trunc(min(1,min($maxDim/iw,$maxDim/ih))*iw/2)*2)':'max(2,trunc(min(1,min($maxDim/iw,$maxDim/ih))*ih/2)*2)'"
        )
      } else if (filters.isNotEmpty() || editState.hasVisualReencodeChanges) {
        // Guarantee even dimensions for hardware H.264 / YUV420p encoders
        filters.add("scale=trunc(iw/2)*2:trunc(ih/2)*2")
      }
    }

    // 5. Color Presets & Adjustments (using LGPL-verified filters in libavfilter.so)
    when (editState.filterPreset) {
      VideoFilterPreset.NONE -> {}
      VideoFilterPreset.BW -> filters.add("hue=s=0")
      VideoFilterPreset.SEPIA -> {
        filters.add("colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131")
      }
      VideoFilterPreset.WARM -> {
        filters.add("colorbalance=rs=.15:gs=.03:bs=-.15:rm=.10:gm=.02:bm=-.10")
      }
      VideoFilterPreset.COOL -> {
        filters.add("colorbalance=rs=-.12:gs=.00:bs=.16:rm=-.08:gm=.02:bm=.12")
      }
      VideoFilterPreset.VINTAGE -> {
        filters.add("colorchannelmixer=.90:.08:.04:0:.04:.84:.06:0:.03:.06:.72")
        filters.add("vignette=PI/5")
      }
      VideoFilterPreset.VIVID -> {
        filters.add("hue=s=1.38")
      }
      VideoFilterPreset.INVERT -> {
        filters.add("negate")
      }
    }

    if (abs(editState.saturation - 1f) > 0.01f && editState.filterPreset != VideoFilterPreset.BW) {
      filters.add(String.format(Locale.US, "hue=s=%.2f", editState.saturation.coerceIn(0f, 2f)))
    }

    if (abs(editState.contrast - 1f) > 0.01f || abs(editState.brightness) > 0.5f) {
      val c = String.format(Locale.US, "%.2f", editState.contrast.coerceIn(0.5f, 1.5f))
      val b = String.format(Locale.US, "%.1f", editState.brightness.coerceIn(-100f, 100f))
      filters.add("lutyuv=y='clip((val-128)*$c+128+($b),0,255)'")
    }

    // 6. Playback Speed (shift to 0-origin first so trim offsets never skew timestamps)
    if (editState.hasSpeedChanges) {
      val ptsFactor = 1.0 / editState.playbackSpeed.coerceIn(0.25f, 4.0f)
      filters.add(String.format(Locale.US, "setpts=%.4f*(PTS-STARTPTS)", ptsFactor))
    }

    // 7. Smooth Video Fade In / Fade Out on output timeline
    if (editState.fadeInOut) {
      val outSec = editState.estimatedOutputDurationMs / 1000.0
      val fadeDur = min(0.5, outSec / 3.0)
      if (fadeDur >= 0.1) {
        val fadeStr = String.format(Locale.US, "%.2f", fadeDur)
        val outStartStr = String.format(Locale.US, "%.2f", (outSec - fadeDur).coerceAtLeast(0.0))
        filters.add("fade=t=in:st=0:d=$fadeStr")
        filters.add("fade=t=out:st=$outStartStr:d=$fadeStr")
      }
    }

    return filters
  }

  /**
   * Builds the FFmpeg `-af` audio filter stages, decomposing speeds outside `[0.5, 2.0]`
   * into chained `atempo` filters.
   */
  fun buildAudioFilterStages(editState: VideoEditState): List<String> {
    if (editState.isMuted) return emptyList()
    val filters = mutableListOf<String>()

    if (editState.hasSpeedChanges) {
      var remaining = editState.playbackSpeed.coerceIn(0.25f, 4.0f).toDouble()
      while (remaining < 0.5 - 1e-4) {
        filters.add("atempo=0.5")
        remaining /= 0.5
      }
      while (remaining > 2.0 + 1e-4) {
        filters.add("atempo=2.0")
        remaining /= 2.0
      }
      if (abs(remaining - 1.0) > 0.01) {
        filters.add(String.format(Locale.US, "atempo=%.4f", remaining.coerceIn(0.5, 2.0)))
      }
    }

    if (abs(editState.volume - 1.0f) > 0.01f) {
      filters.add(String.format(Locale.US, "volume=%.2f", editState.volume.coerceIn(0f, 2f)))
    }

    if (editState.fadeInOut) {
      val outSec = editState.estimatedOutputDurationMs / 1000.0
      val fadeDur = min(0.5, outSec / 3.0)
      if (fadeDur >= 0.1) {
        val fadeStr = String.format(Locale.US, "%.2f", fadeDur)
        val outStartStr = String.format(Locale.US, "%.2f", (outSec - fadeDur).coerceAtLeast(0.0))
        filters.add("afade=t=in:st=0:d=$fadeStr")
        filters.add("afade=t=out:st=$outStartStr:d=$fadeStr")
      }
    }

    return filters
  }

  /**
   * Builds the complete FFmpeg CLI argument list for [strategy].
   */
  fun buildFfmpegArguments(
    inputPath: String,
    outputPath: String,
    metadata: VideoMetadata?,
    editState: VideoEditState,
    options: VideoExportOptions,
    strategy: VideoProcessingStrategy = determineStrategy(metadata, editState, options),
  ): List<String> {
    val args = mutableListOf("-y")

    // Fast input-level demuxer seeking & windowing
    if (editState.hasTrimChanges) {
      if (editState.trimStartMs > 0L) {
        args.addAll(listOf("-ss", formatFfmpegSeconds(editState.trimStartMs)))
      }
      args.addAll(listOf("-t", formatFfmpegSeconds(editState.trimmedSourceDurationMs)))
    }

    args.addAll(listOf("-i", inputPath))

    val hasSourceAudio = metadata?.hasAudio ?: true
    val muteAudio = editState.isMuted || editState.volume <= 0.001f || !hasSourceAudio

    when (options.format) {
      VideoExportFormat.GIF -> {
        val vf = buildVideoFilterStages(metadata, editState, options).joinToString(",")
        if (vf.isNotEmpty()) {
          args.addAll(listOf("-vf", vf))
        }
        args.addAll(listOf("-an", "-loop", "0", outputPath))
        return args
      }

      VideoExportFormat.M4A -> {
        args.add("-vn")
        if (strategy == VideoProcessingStrategy.STREAM_COPY && !muteAudio) {
          args.addAll(listOf("-c:a", "copy", "-movflags", "+faststart", outputPath))
        } else {
          val af = buildAudioFilterStages(editState).joinToString(",")
          if (af.isNotEmpty()) {
            args.addAll(listOf("-af", af))
          }
          args.addAll(listOf("-c:a", "aac", "-b:a", "192k", "-movflags", "+faststart", outputPath))
        }
        return args
      }

      VideoExportFormat.MP4 -> {
        if (strategy == VideoProcessingStrategy.STREAM_COPY) {
          args.addAll(listOf("-c:v", "copy"))
          if (muteAudio) {
            args.add("-an")
          } else {
            args.addAll(listOf("-c:a", "copy"))
          }
          args.addAll(listOf("-movflags", "+faststart", outputPath))
          return args
        }

        val vf = buildVideoFilterStages(metadata, editState, options).joinToString(",")
        if (vf.isNotEmpty()) {
          args.addAll(listOf("-vf", vf))
        }

        val targetBitrate = (options.resolution.baseBitrateBps * options.quality.bitrateMultiplier)
          .roundToInt()
          .coerceIn(500_000, 20_000_000)
        val targetQScale = (options.resolution.mpeg4QScale + options.quality.qScaleOffset)
          .coerceIn(2, 12)

        if (strategy == VideoProcessingStrategy.HARDWARE_FFMPEG) {
          args.addAll(
            listOf(
              "-c:v", "h264_mediacodec",
              "-b:v", targetBitrate.toString(),
              "-pix_fmt", "yuv420p",
            )
          )
        } else {
          args.addAll(
            listOf(
              "-c:v", "mpeg4",
              "-q:v", targetQScale.toString(),
              "-pix_fmt", "yuv420p",
            )
          )
        }

        if (muteAudio) {
          args.add("-an")
        } else {
          val af = buildAudioFilterStages(editState).joinToString(",")
          val ext = metadata?.file?.extension?.lowercase(Locale.ROOT).orEmpty()
          if (af.isEmpty() && ext in STREAM_COPY_VIDEO_EXTENSIONS && strategy == VideoProcessingStrategy.HARDWARE_FFMPEG) {
            args.addAll(listOf("-c:a", "copy"))
          } else {
            if (af.isNotEmpty()) {
              args.addAll(listOf("-af", af))
            }
            args.addAll(listOf("-c:a", "aac", "-b:a", "128k"))
          }
        }

        args.addAll(listOf("-movflags", "+faststart", outputPath))
        return args
      }
    }
  }

  /**
   * Returns the ordered fallback sequence of strategies so exports always try the fastest
   * lossless or hardware path first and seamlessly degrade to universal software FFmpeg if needed.
   */
  fun buildStrategyFallbackChain(
    metadata: VideoMetadata?,
    editState: VideoEditState,
    options: VideoExportOptions,
  ): List<VideoProcessingStrategy> {
    val primary = determineStrategy(metadata, editState, options)
    return when (options.format) {
      VideoExportFormat.GIF -> listOf(VideoProcessingStrategy.SOFTWARE_FFMPEG)
      VideoExportFormat.M4A -> {
        if (primary == VideoProcessingStrategy.STREAM_COPY) {
          listOf(VideoProcessingStrategy.STREAM_COPY, VideoProcessingStrategy.SOFTWARE_FFMPEG)
        } else {
          listOf(VideoProcessingStrategy.SOFTWARE_FFMPEG)
        }
      }
      VideoExportFormat.MP4 -> {
        when (primary) {
          VideoProcessingStrategy.STREAM_COPY -> listOf(
            VideoProcessingStrategy.STREAM_COPY,
            VideoProcessingStrategy.HARDWARE_FFMPEG,
            VideoProcessingStrategy.SOFTWARE_FFMPEG,
          )
          VideoProcessingStrategy.HARDWARE_FFMPEG -> listOf(
            VideoProcessingStrategy.HARDWARE_FFMPEG,
            VideoProcessingStrategy.SOFTWARE_FFMPEG,
          )
          VideoProcessingStrategy.SOFTWARE_FFMPEG -> listOf(
            VideoProcessingStrategy.SOFTWARE_FFMPEG,
          )
        }
      }
    }
  }

  /**
   * Probes video duration, dimensions, rotation, framerate, and audio stream presence.
   * Uses fast native [MediaMetadataRetriever] first with [FFprobeKit] fallback for MKV/AVI/FLV.
   */
  suspend fun probeMetadata(file: File): VideoMetadata? = withContext(Dispatchers.IO) {
    if (!file.exists() || file.length() <= 0L) return@withContext null

    var durationMs = 0L
    var width = 0
    var height = 0
    var rotation = 0
    var frameRate = 30f
    var bitrate = 0L
    var hasAudio = true
    var videoCodec: String? = null

    val retriever = MediaMetadataRetriever()
    try {
      retriever.setDataSource(file.absolutePath)
      durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
      width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
      height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
      rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
      bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
      val hasAudioMeta = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
      if (hasAudioMeta != null) {
        hasAudio = hasAudioMeta.equals("yes", ignoreCase = true) || hasAudioMeta == "1"
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val fpsMeta = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull()
        if (fpsMeta != null && fpsMeta > 0f) frameRate = fpsMeta
      }
    } catch (_: Throwable) {
      // Fall through to FFprobe
    } finally {
      try {
        retriever.release()
      } catch (_: Throwable) {}
    }

    if (durationMs <= 0L || (width <= 0 && height <= 0)) {
      try {
        val infoSession = FFprobeKit.getMediaInformation(file.absolutePath)
        val info = infoSession.mediaInformation
        if (info != null) {
          val durSec = info.duration?.toDoubleOrNull() ?: 0.0
          if (durSec > 0.0) durationMs = (durSec * 1000.0).roundToLong()
          bitrate = info.bitrate?.toLongOrNull() ?: bitrate
          val streams = info.streams.orEmpty()
          val vStream = streams.firstOrNull { it.type.equals("video", ignoreCase = true) }
          val aStream = streams.firstOrNull { it.type.equals("audio", ignoreCase = true) }
          hasAudio = aStream != null
          if (vStream != null) {
            width = vStream.width?.toInt() ?: width
            height = vStream.height?.toInt() ?: height
            videoCodec = vStream.codec
            val fpsRaw = vStream.averageFrameRate ?: vStream.realFrameRate
            if (!fpsRaw.isNullOrBlank()) {
              val parts = fpsRaw.split("/")
              val parsedFps = if (parts.size == 2) {
                val num = parts[0].toFloatOrNull() ?: 0f
                val den = parts[1].toFloatOrNull() ?: 1f
                if (den > 0f) num / den else 0f
              } else {
                fpsRaw.toFloatOrNull() ?: 0f
              }
              if (parsedFps in 1f..240f) frameRate = parsedFps
            }
          }
        }
      } catch (_: Throwable) {}
    }

    if (durationMs <= 0L && width <= 0 && height <= 0) {
      return@withContext null
    }

    VideoMetadata(
      file = file,
      durationMs = durationMs.coerceAtLeast(500L),
      width = width.coerceAtLeast(2),
      height = height.coerceAtLeast(2),
      rotationDegrees = ((rotation % 360) + 360) % 360,
      frameRate = frameRate,
      bitrateBps = bitrate,
      hasAudio = hasAudio,
      videoCodec = videoCodec,
      sizeBytes = file.length(),
    )
  }

  /**
   * Extracts a row of downscaled timeline filmstrip thumbnails for fast visual scrubbing.
   */
  suspend fun extractFilmstripThumbnails(
    file: File,
    durationMs: Long,
    count: Int = 8,
    targetHeightPx: Int = 96,
  ): List<Bitmap> = withContext(Dispatchers.IO) {
    if (!file.exists() || durationMs <= 0L || count <= 0) return@withContext emptyList()
    val frames = mutableListOf<Bitmap>()
    val retriever = MediaMetadataRetriever()
    try {
      retriever.setDataSource(file.absolutePath)
      val rawW = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 160
      val rawH = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 90
      val rot = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
      val dispW = if (rot == 90 || rot == 270) rawH else rawW
      val dispH = if (rot == 90 || rot == 270) rawW else rawH
      val aspect = if (dispH > 0) dispW.toFloat() / dispH.toFloat() else 16f / 9f
      val targetWidthPx = (targetHeightPx * aspect).roundToInt().coerceIn(48, 320)

      for (i in 0 until count) {
        val fraction = if (count == 1) 0.5f else i.toFloat() / (count - 1).toFloat()
        val timeUs = (durationMs * fraction * 1000f).roundToLong().coerceAtLeast(0L)
        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
          retriever.getScaledFrameAtTime(
            timeUs,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
            targetWidthPx,
            targetHeightPx,
          )
        } else {
          retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.let { full ->
            val scaled = Bitmap.createScaledBitmap(full, targetWidthPx, targetHeightPx, true)
            if (scaled !== full) full.recycle()
            scaled
          }
        }
        if (bmp != null) {
          frames.add(bmp)
        }
      }
    } catch (_: Throwable) {
      // Ignore partial thumbnail failures
    } finally {
      try {
        retriever.release()
      } catch (_: Throwable) {}
    }
    frames
  }

  /**
   * Captures a high-resolution still frame at [timeMs] and applies active [editState] visual edits
   * (rotation, flip, crop, and color adjustments/preset).
   */
  suspend fun captureEditedFrame(
    file: File,
    timeMs: Long,
    editState: VideoEditState? = null,
  ): Bitmap? = withContext(Dispatchers.IO) {
    if (!file.exists()) return@withContext null
    val retriever = MediaMetadataRetriever()
    val rawFrame = try {
      retriever.setDataSource(file.absolutePath)
      val timeUs = (timeMs.coerceAtLeast(0L)) * 1000L
      retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
        ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        ?: retriever.frameAtTime
    } catch (_: Throwable) {
      null
    } finally {
      try {
        retriever.release()
      } catch (_: Throwable) {}
    } ?: return@withContext null

    if (editState == null) return@withContext rawFrame

    var working = rawFrame

    // 1. Rotate & Flip (apply rotation first, then screen-space flip to match preview & FFmpeg)
    if (editState.normalizedRotation != 0) {
      val rotated = BitmapOperations.rotate(working, degrees = editState.normalizedRotation.toFloat())
      if (rotated !== working) working.recycle()
      working = rotated
    }
    if (editState.flipHorizontal || editState.flipVertical) {
      val flipped = BitmapOperations.rotate(
        source = working,
        degrees = 0f,
        flipHorizontal = editState.flipHorizontal,
        flipVertical = editState.flipVertical,
      )
      if (flipped !== working) working.recycle()
      working = flipped
    }

    // 2. Crop
    if (editState.hasCropChanges) {
      val cropped = BitmapOperations.crop(working, editState.cropRect)
      if (cropped !== working) working.recycle()
      working = cropped
    }

    // 3. Color Matrix
    if (editState.hasColorChanges) {
      val out = Bitmap.createBitmap(working.width, working.height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(out)
      val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(buildPreviewColorMatrix(editState))
      }
      canvas.drawBitmap(working, 0f, 0f, paint)
      working.recycle()
      working = out
    }

    working
  }

  /**
   * Executes a video edit & export operation using FFmpeg with real-time progress reporting,
   * cancellation support, and automatic multi-tier fallback.
   */
  suspend fun exportVideo(
    context: Context?,
    sourceFile: File,
    outputDir: File,
    desiredStem: String,
    metadata: VideoMetadata?,
    editState: VideoEditState,
    options: VideoExportOptions,
    onProgress: (Float) -> Unit = {},
    ffmpegRunner: (suspend (List<String>, File, VideoProcessingStrategy) -> Boolean)? = null,
  ): VideoExportResult = withContext(Dispatchers.IO) {
    val startTimeMs = System.currentTimeMillis()

    if (!sourceFile.exists() || !sourceFile.canRead()) {
      return@withContext VideoExportResult(
        success = false,
        outputFile = null,
        strategyUsed = VideoProcessingStrategy.SOFTWARE_FFMPEG,
        usedFallback = false,
        elapsedMs = 0L,
        outputSizeBytes = 0L,
        message = "Source video file could not be read.",
      )
    }

    if (editState.isMuted && options.format == VideoExportFormat.M4A) {
      return@withContext VideoExportResult(
        success = false,
        outputFile = null,
        strategyUsed = VideoProcessingStrategy.SOFTWARE_FFMPEG,
        usedFallback = false,
        elapsedMs = 0L,
        outputSizeBytes = 0L,
        message = "Cannot export Audio Only (M4A) while Mute Audio is enabled.",
      )
    }

    // Determine writable target directory (prefer user Movies/Toolbox when writable, else staging dir + MediaStore publish)
    val directDirWritable = (outputDir.exists() || outputDir.mkdirs()) && outputDir.canWrite()
    val workingDir = if (directDirWritable) {
      outputDir
    } else {
      val fallbackBase = context?.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        ?: context?.cacheDir
        ?: outputDir
      File(fallbackBase, "ToolboxExports").apply { mkdirs() }
    }

    val outputFile = resolveOutputFile(
      outputDir = workingDir,
      desiredStem = desiredStem,
      extension = options.format.extension,
      sourceFile = sourceFile,
    )

    val strategies = buildStrategyFallbackChain(metadata, editState, options)
    val expectedOutDurationMs = editState.estimatedOutputDurationMs.coerceAtLeast(250L)
    var wasCancelled = false
    var lastErrorMessage = "Encoding failed"

    for ((index, strategy) in strategies.withIndex()) {
      if (outputFile.exists()) {
        outputFile.delete()
      }

      val args = buildFfmpegArguments(
        inputPath = sourceFile.absolutePath,
        outputPath = outputFile.absolutePath,
        metadata = metadata,
        editState = editState,
        options = options,
        strategy = strategy,
      )

      val succeeded = if (ffmpegRunner != null) {
        try {
          ffmpegRunner(args, outputFile, strategy)
        } catch (t: Throwable) {
          lastErrorMessage = t.message ?: t::class.java.simpleName
          false
        }
      } else {
        val runOutcome = runFfmpegSessionSuspend(
          args = args,
          expectedDurationMs = expectedOutDurationMs,
          onProgress = onProgress,
        )
        wasCancelled = runOutcome.cancelled
        if (!runOutcome.success && !runOutcome.errorSummary.isNullOrBlank()) {
          lastErrorMessage = runOutcome.errorSummary
        }
        runOutcome.success
      }

      if (wasCancelled) {
        if (outputFile.exists()) outputFile.delete()
        val elapsed = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(1L)
        return@withContext VideoExportResult(
          success = false,
          cancelled = true,
          outputFile = null,
          strategyUsed = strategy,
          usedFallback = index > 0,
          elapsedMs = elapsed,
          outputSizeBytes = 0L,
          message = "Export cancelled.",
        )
      }

      if (succeeded && outputFile.exists() && outputFile.length() > 0L) {
        onProgress(1f)
        val galleryUri = if (context != null) {
          publishOutputToMediaStoreIfNeeded(
            context = context,
            file = outputFile,
            format = options.format,
            alreadyInPublicStorage = directDirWritable,
          )
        } else {
          null
        }
        val elapsed = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(1L)
        return@withContext VideoExportResult(
          success = true,
          cancelled = false,
          outputFile = outputFile,
          galleryUri = galleryUri,
          strategyUsed = strategy,
          usedFallback = index > 0,
          elapsedMs = elapsed,
          outputSizeBytes = outputFile.length(),
          message = "Exported ${outputFile.name} via ${strategy.badgeLabel}",
        )
      }
    }

    if (outputFile.exists()) {
      outputFile.delete()
    }
    val elapsed = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(1L)
    VideoExportResult(
      success = false,
      cancelled = false,
      outputFile = null,
      strategyUsed = strategies.last(),
      usedFallback = strategies.size > 1,
      elapsedMs = elapsed,
      outputSizeBytes = 0L,
      message = "Export failed: $lastErrorMessage",
    )
  }

  private data class FfmpegRunOutcome(
    val success: Boolean,
    val cancelled: Boolean,
    val errorSummary: String? = null,
  )

  private suspend fun runFfmpegSessionSuspend(
    args: List<String>,
    expectedDurationMs: Long,
    onProgress: (Float) -> Unit,
  ): FfmpegRunOutcome = suspendCancellableCoroutine { continuation ->
    val session: FFmpegSession = FFmpegKit.executeWithArgumentsAsync(
      args.toTypedArray(),
      { completedSession ->
        if (continuation.isActive) {
          val rc = completedSession.returnCode
          val cancelled = ReturnCode.isCancel(rc)
          val ok = ReturnCode.isSuccess(rc)
          val err = if (!ok && !cancelled) {
            completedSession.failStackTrace
              ?: completedSession.allLogsAsString?.lines()?.lastOrNull { it.isNotBlank() }
          } else {
            null
          }
          continuation.resume(FfmpegRunOutcome(success = ok, cancelled = cancelled, errorSummary = err))
        }
      },
      { /* log callback */ },
      { stats ->
        val encodedMs = stats.time.toLong()
        if (encodedMs > 0L && expectedDurationMs > 0L) {
          val pct = (encodedMs.toFloat() / expectedDurationMs.toFloat()).coerceIn(0.01f, 0.99f)
          onProgress(pct)
        }
      },
    )

    continuation.invokeOnCancellation {
      try {
        FFmpegKit.cancel(session.sessionId)
      } catch (_: Throwable) {}
    }
  }

  /**
   * Returns the default `/Movies/Toolbox` directory on primary storage when accessible,
   * or the app's external Movies directory as fallback.
   */
  fun getDefaultExportDirectory(context: Context): File {
    val root = FileManagerEngine.getUserRootDirectory()
    val moviesDir = File(root, DEFAULT_VIDEO_EXPORT_SUBFOLDER)
    if (moviesDir.exists() || moviesDir.mkdirs() || root.canWrite()) {
      return moviesDir
    }
    val extMovies = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
    return File(extMovies, "Toolbox").apply { mkdirs() }
  }

  /**
   * Publishes an exported media file to Android's `MediaStore` or triggers `MediaScannerConnection`
   * so it immediately appears in system Gallery / Photos / Music apps.
   */
  private fun publishOutputToMediaStoreIfNeeded(
    context: Context,
    file: File,
    format: VideoExportFormat,
    alreadyInPublicStorage: Boolean,
  ): Uri? {
    if (alreadyInPublicStorage) {
      try {
        MediaScannerConnection.scanFile(
          context,
          arrayOf(file.absolutePath),
          arrayOf(format.mimeType),
          null,
        )
      } catch (_: Throwable) {}
      return Uri.fromFile(file)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return try {
        val resolver = context.contentResolver
        val (collection, relPath) = when (format) {
          VideoExportFormat.MP4 -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_MOVIES}/Toolbox"
          VideoExportFormat.GIF -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_PICTURES}/Toolbox"
          VideoExportFormat.M4A -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_MUSIC}/Toolbox"
        }
        val values = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
          put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
          put(MediaStore.MediaColumns.RELATIVE_PATH, relPath)
          put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val itemUri = resolver.insert(collection, values) ?: return null
        resolver.openOutputStream(itemUri)?.use { out ->
          FileInputStream(file).use { input ->
            input.copyTo(out)
          }
        }
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(itemUri, values, null, null)
        itemUri
      } catch (_: Throwable) {
        null
      }
    }
    return null
  }

  /**
   * Saves a captured video frame [Bitmap] as a high-resolution PNG in Photos/Gallery.
   */
  suspend fun saveCapturedFrameToGallery(
    context: Context,
    bitmap: Bitmap,
    baseName: String = "video_frame",
  ): File? = withContext(Dispatchers.IO) {
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "${baseName}_$stamp.png"

    val root = FileManagerEngine.getUserRootDirectory()
    val picturesDir = File(root, "Pictures/Toolbox")
    val directWritable = (picturesDir.exists() || picturesDir.mkdirs()) && picturesDir.canWrite()

    val targetDir = if (directWritable) {
      picturesDir
    } else {
      File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir, "Frames").apply { mkdirs() }
    }

    val outFile = File(targetDir, fileName)
    val saved = try {
      FileOutputStream(outFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
      }
    } catch (_: Throwable) {
      false
    }
    if (!saved || !outFile.exists()) return@withContext null

    if (directWritable) {
      try {
        MediaScannerConnection.scanFile(context, arrayOf(outFile.absolutePath), arrayOf("image/png"), null)
      } catch (_: Throwable) {}
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
          put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
          put(MediaStore.Images.Media.MIME_TYPE, "image/png")
          put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Toolbox")
          put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
          resolver.openOutputStream(uri)?.use { out ->
            FileInputStream(outFile).use { it.copyTo(out) }
          }
          values.clear()
          values.put(MediaStore.Images.Media.IS_PENDING, 0)
          resolver.update(uri, values, null, null)
        }
      } catch (_: Throwable) {}
    }
    outFile
  }

  /**
   * Generates a real, playable 4-second 640×360 30fps sample MP4 video with animated test patterns
   * and a gentle audio tone using FFmpeg's built-in `lavfi` `testsrc2` + `sine` generators.
   */
  suspend fun generateSampleVideo(context: Context): File? = withContext(Dispatchers.IO) {
    val sampleDir = File(context.cacheDir, "video_editor_samples").apply { mkdirs() }
    val sampleFile = File(sampleDir, "sample_clip.mp4")
    if (sampleFile.exists() && sampleFile.length() > 1024L) {
      return@withContext sampleFile
    }

    val encoders = listOf(
      listOf("-c:v", "h264_mediacodec", "-b:v", "2000000"),
      listOf("-c:v", "mpeg4", "-q:v", "4"),
    )

    for (videoEncoderArgs in encoders) {
      if (sampleFile.exists()) sampleFile.delete()
      val args = buildList {
        add("-y")
        addAll(listOf("-f", "lavfi", "-i", "testsrc2=size=640x360:rate=30:duration=4"))
        addAll(listOf("-f", "lavfi", "-i", "sine=frequency=440:sample_rate=44100:duration=4"))
        addAll(videoEncoderArgs)
        addAll(listOf("-pix_fmt", "yuv420p"))
        addAll(listOf("-af", "volume=0.25,afade=t=in:st=0:d=0.3,afade=t=out:st=3.5:d=0.5"))
        addAll(listOf("-c:a", "aac", "-b:a", "128k"))
        addAll(listOf("-movflags", "+faststart"))
        add(sampleFile.absolutePath)
      }
      try {
        val session = FFmpegKit.executeWithArguments(args.toTypedArray())
        if (ReturnCode.isSuccess(session.returnCode) && sampleFile.exists() && sampleFile.length() > 1024L) {
          return@withContext sampleFile
        }
      } catch (_: Throwable) {}
    }

    null
  }
}
