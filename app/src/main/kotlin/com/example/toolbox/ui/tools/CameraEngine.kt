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
import kotlin.math.abs
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
const val PREF_KEY_CAMERA_SCAN_FILTER = "camera_scan_filter"

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
  SCANNER(displayName = "Scan", shortLabel = "SCAN"),
  ;

  companion object {
    val DEFAULT: CameraCaptureMode = PHOTO

    fun fromNameOrDefault(name: String?): CameraCaptureMode {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find {
        it.name.equals(name.trim(), ignoreCase = true) ||
          it.displayName.equals(name.trim(), ignoreCase = true) ||
          it.shortLabel.equals(name.trim(), ignoreCase = true)
      } ?: DEFAULT
    }
  }
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
    mirrorFront: Boolean = false,
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
  ): Bitmap {
    if (rawBitmap.width <= 0 || rawBitmap.height <= 0) return rawBitmap
    return try {
      val outBitmap = Bitmap.createBitmap(rawBitmap.width, rawBitmap.height, Bitmap.Config.ARGB_8888)
      val combinedMatrix = Matrix(textureTransformMatrix)
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
