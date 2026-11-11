package com.example.toolbox.ui.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

const val FILE_CONVERTER_PREFS_NAME = "file_converter_preferences"
const val PREF_KEY_CONVERTER_OUTPUT_DIR = "converter_output_directory"
const val PREF_KEY_CONVERTER_USE_SAME_FOLDER = "converter_use_same_folder"
const val DEFAULT_CONVERTED_FOLDER_NAME = "Converted"
const val DEFAULT_CONVERTED_FOLDER_DISPLAY = "/Converted"

/**
 * High-level media/file family used to group convertible formats in the UI.
 */
enum class ConversionMediaCategory(
  val displayName: String,
) {
  IMAGE("Image"),
  AUDIO("Audio"),
  VIDEO("Video"),
  DOCUMENT("Document & Data"),
  ARCHIVE("Archive"),
}

/**
 * Conversion processing engine used to perform a conversion.
 * [NATIVE] is always preferred where supported; [FFMPEG] acts as backup or primary for formats
 * not natively encoded by Android platform APIs.
 */
enum class ConverterBackend(
  val displayName: String,
  val description: String,
) {
  NATIVE(
    displayName = "Native",
    description = "Android Native APIs & Hardware Codec",
  ),
  FFMPEG(
    displayName = "FFmpeg",
    description = "FFmpeg Multi-Codec Engine",
  ),
}

/**
 * All file formats supported by the File Converter tool.
 */
enum class ConvertibleFormat(
  val displayName: String,
  val extension: String,
  val aliases: Set<String> = emptySet(),
  val mimeType: String,
  val category: ConversionMediaCategory,
  val description: String,
) {
  // --- IMAGE FORMATS ---
  PNG(
    displayName = "PNG",
    extension = "png",
    mimeType = "image/png",
    category = ConversionMediaCategory.IMAGE,
    description = "Lossless Portable Network Graphics",
  ),
  JPG(
    displayName = "JPG",
    extension = "jpg",
    aliases = setOf("jpeg", "jpe", "jfif"),
    mimeType = "image/jpeg",
    category = ConversionMediaCategory.IMAGE,
    description = "Compressed photographic image",
  ),
  WEBP(
    displayName = "WEBP",
    extension = "webp",
    mimeType = "image/webp",
    category = ConversionMediaCategory.IMAGE,
    description = "Modern high-efficiency web image",
  ),
  BMP(
    displayName = "BMP",
    extension = "bmp",
    aliases = setOf("dib"),
    mimeType = "image/bmp",
    category = ConversionMediaCategory.IMAGE,
    description = "Uncompressed Windows bitmap",
  ),
  GIF(
    displayName = "GIF",
    extension = "gif",
    mimeType = "image/gif",
    category = ConversionMediaCategory.IMAGE,
    description = "Graphics Interchange Format (static or animated)",
  ),
  HEIC(
    displayName = "HEIC",
    extension = "heic",
    aliases = setOf("heif"),
    mimeType = "image/heic",
    category = ConversionMediaCategory.IMAGE,
    description = "High Efficiency Image Container",
  ),
  ICO(
    displayName = "ICO",
    extension = "ico",
    mimeType = "image/x-icon",
    category = ConversionMediaCategory.IMAGE,
    description = "Icon image format",
  ),
  TIFF(
    displayName = "TIFF",
    extension = "tiff",
    aliases = setOf("tif"),
    mimeType = "image/tiff",
    category = ConversionMediaCategory.IMAGE,
    description = "Tagged Image File Format",
  ),

  // --- AUDIO FORMATS ---
  MP3(
    displayName = "MP3",
    extension = "mp3",
    mimeType = "audio/mpeg",
    category = ConversionMediaCategory.AUDIO,
    description = "Universal MPEG Layer-3 audio",
  ),
  WAV(
    displayName = "WAV",
    extension = "wav",
    aliases = setOf("wave"),
    mimeType = "audio/wav",
    category = ConversionMediaCategory.AUDIO,
    description = "Uncompressed 16-bit PCM audio",
  ),
  M4A(
    displayName = "M4A",
    extension = "m4a",
    mimeType = "audio/mp4",
    category = ConversionMediaCategory.AUDIO,
    description = "MPEG-4 AAC audio container",
  ),
  AAC(
    displayName = "AAC",
    extension = "aac",
    mimeType = "audio/aac",
    category = ConversionMediaCategory.AUDIO,
    description = "Advanced Audio Coding ADTS stream",
  ),
  FLAC(
    displayName = "FLAC",
    extension = "flac",
    mimeType = "audio/flac",
    category = ConversionMediaCategory.AUDIO,
    description = "Free Lossless Audio Codec",
  ),
  OGG(
    displayName = "OGG",
    extension = "ogg",
    aliases = setOf("oga"),
    mimeType = "audio/ogg",
    category = ConversionMediaCategory.AUDIO,
    description = "Ogg Vorbis / Opus audio container",
  ),
  OPUS(
    displayName = "OPUS",
    extension = "opus",
    mimeType = "audio/opus",
    category = ConversionMediaCategory.AUDIO,
    description = "Low-latency Opus audio codec",
  ),
  WMA(
    displayName = "WMA",
    extension = "wma",
    mimeType = "audio/x-ms-wma",
    category = ConversionMediaCategory.AUDIO,
    description = "Windows Media Audio",
  ),
  AIFF(
    displayName = "AIFF",
    extension = "aiff",
    aliases = setOf("aif", "aifc"),
    mimeType = "audio/aiff",
    category = ConversionMediaCategory.AUDIO,
    description = "Audio Interchange File Format",
  ),
  THREE_GPP(
    displayName = "3GP",
    extension = "3gp",
    aliases = setOf("3gpp", "amr"),
    mimeType = "audio/3gpp",
    category = ConversionMediaCategory.AUDIO,
    description = "3GPP mobile voice & media container",
  ),

  // --- VIDEO FORMATS ---
  MP4(
    displayName = "MP4",
    extension = "mp4",
    aliases = setOf("m4v"),
    mimeType = "video/mp4",
    category = ConversionMediaCategory.VIDEO,
    description = "Universal MPEG-4 video container",
  ),
  MKV(
    displayName = "MKV",
    extension = "mkv",
    mimeType = "video/x-matroska",
    category = ConversionMediaCategory.VIDEO,
    description = "Matroska multimedia container",
  ),
  WEBM(
    displayName = "WEBM",
    extension = "webm",
    mimeType = "video/webm",
    category = ConversionMediaCategory.VIDEO,
    description = "Open web video format (VP8/VP9/Opus)",
  ),
  MOV(
    displayName = "MOV",
    extension = "mov",
    aliases = setOf("qt"),
    mimeType = "video/quicktime",
    category = ConversionMediaCategory.VIDEO,
    description = "Apple QuickTime movie container",
  ),
  AVI(
    displayName = "AVI",
    extension = "avi",
    mimeType = "video/x-msvideo",
    category = ConversionMediaCategory.VIDEO,
    description = "Audio Video Interleave container",
  ),
  FLV(
    displayName = "FLV",
    extension = "flv",
    mimeType = "video/x-flv",
    category = ConversionMediaCategory.VIDEO,
    description = "Flash Video container",
  ),
  MPEG(
    displayName = "MPEG",
    extension = "mpeg",
    aliases = setOf("mpg", "m2v"),
    mimeType = "video/mpeg",
    category = ConversionMediaCategory.VIDEO,
    description = "MPEG-1 / MPEG-2 video stream",
  ),
  TS(
    displayName = "TS",
    extension = "ts",
    aliases = setOf("mts", "m2ts"),
    mimeType = "video/mp2t",
    category = ConversionMediaCategory.VIDEO,
    description = "MPEG Transport Stream",
  ),

  // --- DOCUMENT & DATA FORMATS ---
  PDF(
    displayName = "PDF",
    extension = "pdf",
    mimeType = "application/pdf",
    category = ConversionMediaCategory.DOCUMENT,
    description = "Portable Document Format",
  ),
  TXT(
    displayName = "TXT",
    extension = "txt",
    aliases = setOf("log", "text"),
    mimeType = "text/plain",
    category = ConversionMediaCategory.DOCUMENT,
    description = "Plain UTF-8 text document",
  ),
  MD(
    displayName = "MD",
    extension = "md",
    aliases = setOf("markdown"),
    mimeType = "text/markdown",
    category = ConversionMediaCategory.DOCUMENT,
    description = "Markdown formatted text",
  ),
  HTML(
    displayName = "HTML",
    extension = "html",
    aliases = setOf("htm"),
    mimeType = "text/html",
    category = ConversionMediaCategory.DOCUMENT,
    description = "HyperText Markup Language document",
  ),
  CSV(
    displayName = "CSV",
    extension = "csv",
    mimeType = "text/csv",
    category = ConversionMediaCategory.DOCUMENT,
    description = "Comma-Separated Values table",
  ),
  TSV(
    displayName = "TSV",
    extension = "tsv",
    aliases = setOf("tab"),
    mimeType = "text/tab-separated-values",
    category = ConversionMediaCategory.DOCUMENT,
    description = "Tab-Separated Values table",
  ),
  JSON(
    displayName = "JSON",
    extension = "json",
    mimeType = "application/json",
    category = ConversionMediaCategory.DOCUMENT,
    description = "JavaScript Object Notation data",
  ),
  XML(
    displayName = "XML",
    extension = "xml",
    mimeType = "application/xml",
    category = ConversionMediaCategory.DOCUMENT,
    description = "Extensible Markup Language data",
  ),

  // --- ARCHIVE FORMATS ---
  ZIP(
    displayName = "ZIP",
    extension = "zip",
    mimeType = "application/zip",
    category = ConversionMediaCategory.ARCHIVE,
    description = "Standard ZIP compressed archive",
  ),
  TAR(
    displayName = "TAR",
    extension = "tar",
    mimeType = "application/x-tar",
    category = ConversionMediaCategory.ARCHIVE,
    description = "POSIX TAR tape archive",
  ),
  TAR_GZ(
    displayName = "TAR.GZ",
    extension = "tar.gz",
    aliases = setOf("tgz"),
    mimeType = "application/gzip",
    category = ConversionMediaCategory.ARCHIVE,
    description = "GZIP-compressed TAR archive",
  ),
  ;

  val allExtensions: Set<String>
    get() = setOf(extension) + aliases

  companion object {
    fun fromExtension(ext: String?): ConvertibleFormat? {
      if (ext.isNullOrBlank()) return null
      val clean = ext.trim().removePrefix(".").lowercase(Locale.ROOT)
      if (clean.isEmpty()) return null
      return entries.find { clean == it.extension || clean in it.aliases }
    }

    fun fromFileName(fileName: String?): ConvertibleFormat? {
      if (fileName.isNullOrBlank()) return null
      val lower = fileName.trim().lowercase(Locale.ROOT)
      if (lower.endsWith(".tar.gz")) return TAR_GZ
      val dotIndex = lower.lastIndexOf('.')
      if (dotIndex < 0 || dotIndex == lower.lastIndex) return null
      val ext = lower.substring(dotIndex + 1)
      return fromExtension(ext)
    }
  }
}

/**
 * Defines a supported conversion path between [source] and [target], along with the
 * primary [preferredBackend] and optional [fallbackBackend].
 */
data class ConversionRoute(
  val source: ConvertibleFormat,
  val target: ConvertibleFormat,
  val preferredBackend: ConverterBackend,
  val fallbackBackend: ConverterBackend? = null,
  val description: String,
) {
  val isCrossCategory: Boolean
    get() = source.category != target.category

  fun supportsBackend(backend: ConverterBackend): Boolean {
    return preferredBackend == backend || fallbackBackend == backend
  }
}

/**
 * Result of a completed or failed conversion operation.
 */
data class ConversionResult(
  val success: Boolean,
  val sourceFile: File,
  val outputFile: File?,
  val sourceFormat: ConvertibleFormat,
  val targetFormat: ConvertibleFormat,
  val backendUsed: ConverterBackend,
  val usedFallback: Boolean,
  val durationMs: Long,
  val message: String,
) {
  val errorMessage: String?
    get() = if (success) null else message
}

/**
 * Record of a completed conversion displayed in the recent conversions list.
 */
data class ConvertedFileHistoryItem(
  val sourceName: String,
  val outputFile: File,
  val sourceFormat: ConvertibleFormat,
  val targetFormat: ConvertibleFormat,
  val backendUsed: ConverterBackend,
  val usedFallback: Boolean,
  val sizeBytes: Long,
  val durationMs: Long,
  val timestampMs: Long,
) {
  val sizeFormatted: String get() = FileManagerEngine.formatFileSize(sizeBytes)
}

/**
 * Core engine for the File Converter tool:
 * - Maintains the complete map of all supported file conversions across Native and FFmpeg backends
 * - Dynamically selects the optimal backend (Native first where possible, FFmpeg as backup/fallback)
 * - Executes image, audio, video, document/data, PDF, and archive conversions offline on-device
 */
object FileConverterEngine {

  private val IMAGE_FORMATS = ConvertibleFormat.entries.filter { it.category == ConversionMediaCategory.IMAGE }
  private val AUDIO_FORMATS = ConvertibleFormat.entries.filter { it.category == ConversionMediaCategory.AUDIO }
  private val VIDEO_FORMATS = ConvertibleFormat.entries.filter { it.category == ConversionMediaCategory.VIDEO }
  private val DOCUMENT_FORMATS = ConvertibleFormat.entries.filter { it.category == ConversionMediaCategory.DOCUMENT }
  private val ARCHIVE_FORMATS = ConvertibleFormat.entries.filter { it.category == ConversionMediaCategory.ARCHIVE }

  // Formats that Android's BitmapFactory can decode natively
  private val NATIVE_DECODABLE_IMAGES = setOf(
    ConvertibleFormat.PNG,
    ConvertibleFormat.JPG,
    ConvertibleFormat.WEBP,
    ConvertibleFormat.BMP,
    ConvertibleFormat.GIF,
    ConvertibleFormat.HEIC,
    ConvertibleFormat.ICO,
  )

  // Formats that our Native image writer can encode directly (Bitmap.compress + BMP/ICO encoders)
  private val NATIVE_ENCODABLE_IMAGES = setOf(
    ConvertibleFormat.PNG,
    ConvertibleFormat.JPG,
    ConvertibleFormat.WEBP,
    ConvertibleFormat.BMP,
    ConvertibleFormat.ICO,
  )

  // Audio formats that Android MediaExtractor + MediaCodec can decode on-device
  private val NATIVE_DECODABLE_AUDIO = setOf(
    ConvertibleFormat.MP3,
    ConvertibleFormat.WAV,
    ConvertibleFormat.M4A,
    ConvertibleFormat.AAC,
    ConvertibleFormat.FLAC,
    ConvertibleFormat.OGG,
    ConvertibleFormat.OPUS,
    ConvertibleFormat.THREE_GPP,
  )

  // Audio formats that our Native audio encoder can write directly (PCM WAV, AAC ADTS, M4A MPEG-4)
  private val NATIVE_ENCODABLE_AUDIO = setOf(
    ConvertibleFormat.WAV,
    ConvertibleFormat.M4A,
    ConvertibleFormat.AAC,
  )

  // Structured tabular/data formats that can be converted between each other natively
  private val STRUCTURED_DATA_FORMATS = setOf(
    ConvertibleFormat.CSV,
    ConvertibleFormat.TSV,
    ConvertibleFormat.JSON,
    ConvertibleFormat.XML,
  )

  // Text/markup formats
  private val TEXT_MARKUP_FORMATS = setOf(
    ConvertibleFormat.TXT,
    ConvertibleFormat.MD,
    ConvertibleFormat.HTML,
  )

  /**
   * Complete capability registry mapping each source [ConvertibleFormat] to all target
   * [ConvertibleFormat]s and their [ConversionRoute]s.
   */
  val CONVERSION_CAPABILITY_MAP: Map<ConvertibleFormat, Map<ConvertibleFormat, ConversionRoute>> by lazy {
    buildConversionCapabilityMap()
  }

  private fun buildConversionCapabilityMap(): Map<ConvertibleFormat, Map<ConvertibleFormat, ConversionRoute>> {
    val map = mutableMapOf<ConvertibleFormat, MutableMap<ConvertibleFormat, ConversionRoute>>()

    fun register(
      source: ConvertibleFormat,
      target: ConvertibleFormat,
      preferred: ConverterBackend,
      fallback: ConverterBackend? = null,
      description: String,
    ) {
      if (source == target) return
      val targetMap = map.getOrPut(source) { linkedMapOf() }
      targetMap[target] = ConversionRoute(
        source = source,
        target = target,
        preferredBackend = preferred,
        fallbackBackend = fallback,
        description = description,
      )
    }

    // 1. IMAGE <-> IMAGE & IMAGE -> PDF
    for (src in IMAGE_FORMATS) {
      for (dst in IMAGE_FORMATS) {
        if (src == dst) continue
        val canNative = src in NATIVE_DECODABLE_IMAGES && dst in NATIVE_ENCODABLE_IMAGES
        if (canNative) {
          register(
            source = src,
            target = dst,
            preferred = ConverterBackend.NATIVE,
            fallback = ConverterBackend.FFMPEG,
            description = "Native Android Bitmap transcoding with FFmpeg backup",
          )
        } else {
          register(
            source = src,
            target = dst,
            preferred = ConverterBackend.FFMPEG,
            fallback = if (src in NATIVE_DECODABLE_IMAGES && dst == ConvertibleFormat.GIF) ConverterBackend.NATIVE else null,
            description = "FFmpeg image codec conversion",
          )
        }
      }

      // Every image format can convert to PDF (Native PdfDocument for decodable images, FFmpeg+Native for TIFF)
      register(
        source = src,
        target = ConvertibleFormat.PDF,
        preferred = if (src in NATIVE_DECODABLE_IMAGES) ConverterBackend.NATIVE else ConverterBackend.FFMPEG,
        fallback = if (src in NATIVE_DECODABLE_IMAGES) ConverterBackend.FFMPEG else null,
        description = "Render image into a single-page PDF document",
      )
    }

    // 2. PDF -> IMAGE & PDF -> TXT/HTML/MD
    for (dst in listOf(ConvertibleFormat.PNG, ConvertibleFormat.JPG, ConvertibleFormat.WEBP, ConvertibleFormat.BMP)) {
      register(
        source = ConvertibleFormat.PDF,
        target = dst,
        preferred = ConverterBackend.NATIVE,
        fallback = null,
        description = "Native Android PdfRenderer page rasterization",
      )
    }

    // 3. AUDIO <-> AUDIO
    for (src in AUDIO_FORMATS) {
      for (dst in AUDIO_FORMATS) {
        if (src == dst) continue
        val canNative = src in NATIVE_DECODABLE_AUDIO && dst in NATIVE_ENCODABLE_AUDIO
        if (canNative) {
          register(
            source = src,
            target = dst,
            preferred = ConverterBackend.NATIVE,
            fallback = ConverterBackend.FFMPEG,
            description = "Native Android MediaCodec audio transcoding with FFmpeg backup",
          )
        } else {
          register(
            source = src,
            target = dst,
            preferred = ConverterBackend.FFMPEG,
            fallback = null,
            description = "FFmpeg audio codec transcoding",
          )
        }
      }
    }

    // 4. VIDEO <-> VIDEO, VIDEO -> AUDIO (Extract Audio), VIDEO -> IMAGE / GIF
    for (src in VIDEO_FORMATS) {
      for (dst in VIDEO_FORMATS) {
        if (src == dst) continue
        if (dst == ConvertibleFormat.MP4) {
          register(
            source = src,
            target = dst,
            preferred = ConverterBackend.NATIVE,
            fallback = ConverterBackend.FFMPEG,
            description = "Hardware-accelerated Media3 Transformer MP4 transcoding with FFmpeg backup",
          )
        } else {
          register(
            source = src,
            target = dst,
            preferred = ConverterBackend.FFMPEG,
            fallback = null,
            description = "FFmpeg video container & stream conversion",
          )
        }
      }

      // Video -> Audio extraction
      for (audioDst in AUDIO_FORMATS) {
        val canNativeExtract = audioDst in NATIVE_ENCODABLE_AUDIO
        register(
          source = src,
          target = audioDst,
          preferred = if (canNativeExtract) ConverterBackend.NATIVE else ConverterBackend.FFMPEG,
          fallback = if (canNativeExtract) ConverterBackend.FFMPEG else null,
          description = "Extract audio track from ${src.displayName} video to ${audioDst.displayName}",
        )
      }

      // Video -> Animated GIF (FFmpeg) & Video -> Still Frame Image (Native MediaMetadataRetriever + FFmpeg backup)
      register(
        source = src,
        target = ConvertibleFormat.GIF,
        preferred = ConverterBackend.FFMPEG,
        fallback = ConverterBackend.NATIVE,
        description = "Convert video clip into an animated GIF via FFmpeg",
      )
      for (imgDst in listOf(ConvertibleFormat.PNG, ConvertibleFormat.JPG, ConvertibleFormat.WEBP, ConvertibleFormat.BMP)) {
        register(
          source = src,
          target = imgDst,
          preferred = ConverterBackend.NATIVE,
          fallback = ConverterBackend.FFMPEG,
          description = "Extract high-resolution video frame to ${imgDst.displayName}",
        )
      }
    }

    // 5. DOCUMENT & STRUCTURED DATA CONVERSIONS (100% Native)
    val allTextAndData = TEXT_MARKUP_FORMATS + STRUCTURED_DATA_FORMATS
    for (src in allTextAndData) {
      // Any text/markup/data file can be rendered to PDF natively
      register(
        source = src,
        target = ConvertibleFormat.PDF,
        preferred = ConverterBackend.NATIVE,
        fallback = null,
        description = "Native Android PdfDocument typeset rendering",
      )
    }

    // Text/Markup <-> Text/Markup (TXT, MD, HTML)
    for (src in TEXT_MARKUP_FORMATS) {
      for (dst in TEXT_MARKUP_FORMATS) {
        if (src == dst) continue
        register(
          source = src,
          target = dst,
          preferred = ConverterBackend.NATIVE,
          fallback = null,
          description = "Native document markup transformation (${src.displayName} → ${dst.displayName})",
        )
      }
    }

    // Structured Data <-> Structured Data & Markup/Text (CSV, TSV, JSON, XML -> CSV, TSV, JSON, XML, HTML, MD, TXT)
    for (src in STRUCTURED_DATA_FORMATS) {
      for (dst in STRUCTURED_DATA_FORMATS + TEXT_MARKUP_FORMATS) {
        if (src == dst) continue
        register(
          source = src,
          target = dst,
          preferred = ConverterBackend.NATIVE,
          fallback = null,
          description = "Native structured data conversion (${src.displayName} → ${dst.displayName})",
        )
      }
    }

    // 6. ARCHIVE <-> ARCHIVE (ZIP, TAR, TAR.GZ)
    for (src in ARCHIVE_FORMATS) {
      for (dst in ARCHIVE_FORMATS) {
        if (src == dst) continue
        register(
          source = src,
          target = dst,
          preferred = ConverterBackend.NATIVE,
          fallback = null,
          description = "Native streaming archive repackaging (${src.displayName} → ${dst.displayName})",
        )
      }
    }

    return map.mapValues { it.value.toMap() }
  }

  /**
   * Total count of registered conversion routes across all formats.
   */
  val totalConversionRoutesCount: Int
    get() = CONVERSION_CAPABILITY_MAP.values.sumOf { it.size }

  /**
   * Detects the [ConvertibleFormat] of [file] from its filename/extension.
   */
  fun detectFormat(file: File?): ConvertibleFormat? {
    if (file == null) return null
    return ConvertibleFormat.fromFileName(file.name)
  }

  /**
   * Returns `true` if [file] is a non-directory file with a recognized [ConvertibleFormat]
   * that has at least one available conversion route.
   */
  fun isSupportedFile(file: File?): Boolean {
    if (file == null || file.isDirectory) return false
    val format = detectFormat(file) ?: return false
    return getAvailableRoutes(format).isNotEmpty()
  }

  /**
   * Returns all available [ConversionRoute]s from [source].
   */
  fun getAvailableRoutes(source: ConvertibleFormat): List<ConversionRoute> {
    return CONVERSION_CAPABILITY_MAP[source]?.values?.toList() ?: emptyList()
  }

  /**
   * Returns all target [ConvertibleFormat]s supported from [source].
   */
  fun getAvailableTargets(source: ConvertibleFormat): List<ConvertibleFormat> {
    return getAvailableRoutes(source).map { it.target }
  }

  /**
   * Returns the [ConversionRoute] for converting [source] to [target], or `null` if unsupported.
   */
  fun getRoute(source: ConvertibleFormat, target: ConvertibleFormat): ConversionRoute? {
    return CONVERSION_CAPABILITY_MAP[source]?.get(target)
  }

  /**
   * Dynamically selects which [ConverterBackend] to use for converting [source] to [target]
   * based on route preferences and runtime availability.
   * Always prefers [ConverterBackend.NATIVE] when the route supports it as primary.
   */
  fun selectBackend(
    source: ConvertibleFormat,
    target: ConvertibleFormat,
    nativeAvailable: Boolean = true,
    ffmpegAvailable: Boolean = true,
  ): ConverterBackend? {
    val route = getRoute(source, target) ?: return null
    val isPreferredAvailable = when (route.preferredBackend) {
      ConverterBackend.NATIVE -> nativeAvailable
      ConverterBackend.FFMPEG -> ffmpegAvailable
    }
    if (isPreferredAvailable) return route.preferredBackend

    val fallback = route.fallbackBackend ?: return null
    val isFallbackAvailable = when (fallback) {
      ConverterBackend.NATIVE -> nativeAvailable
      ConverterBackend.FFMPEG -> ffmpegAvailable
    }
    return if (isFallbackAvailable) fallback else null
  }

  /**
   * Extracts the filename stem without its format extension (handling `.tar.gz` double extensions).
   */
  fun extractFileStem(fileName: String): String {
    val trimmed = fileName.trim()
    if (trimmed.lowercase(Locale.ROOT).endsWith(".tar.gz") && trimmed.length > 7) {
      return trimmed.substring(0, trimmed.length - 7)
    }
    val dotIndex = trimmed.lastIndexOf('.')
    return if (dotIndex > 0) trimmed.substring(0, dotIndex) else trimmed.ifEmpty { "converted_file" }
  }

  /**
   * Builds a safe output file in [outputDir] with [desiredStem] and [targetFormat], avoiding
   * overwriting the source file or existing files by appending `(1)`, `(2)`, etc. when needed.
   */
  fun resolveOutputFile(
    outputDir: File,
    desiredStem: String,
    targetFormat: ConvertibleFormat,
    sourceFile: File? = null,
  ): File {
    val sanitizedStem = desiredStem
      .replace(Regex("[\\\\/:*?\"<>|]"), "_")
      .trim()
      .ifEmpty { "converted" }
    val ext = targetFormat.extension
    var candidate = File(outputDir, "$sanitizedStem.$ext")
    var counter = 1
    while (candidate.exists() || (sourceFile != null && candidate.absolutePath == sourceFile.absolutePath)) {
      candidate = File(outputDir, "${sanitizedStem}_$counter.$ext")
      counter++
    }
    return candidate
  }

  /**
   * Returns the default `/Converted` directory on the user's primary storage volume.
   */
  fun getDefaultConvertedDirectory(context: Context): File {
    val root = FileManagerEngine.getUserRootDirectory()
    val primaryDir = File(root, DEFAULT_CONVERTED_FOLDER_NAME)
    if (primaryDir.exists() || primaryDir.mkdirs() || root.canWrite()) {
      return primaryDir
    }
    val fallback = context.getExternalFilesDir(null) ?: context.filesDir
    return File(fallback, DEFAULT_CONVERTED_FOLDER_NAME).apply { mkdirs() }
  }

  /**
   * Builds the FFmpeg CLI argument list for converting [inputPath] ([source]) to [outputPath] ([target]).
   */
  fun buildFfmpegArguments(
    inputPath: String,
    outputPath: String,
    source: ConvertibleFormat,
    target: ConvertibleFormat,
  ): List<String> {
    val args = mutableListOf("-y", "-i", inputPath)

    when (target) {
      // Image targets
      ConvertibleFormat.PNG -> {
        if (source.category == ConversionMediaCategory.VIDEO) {
          args.addAll(listOf("-frames:v", "1"))
        }
      }
      ConvertibleFormat.JPG -> {
        if (source.category == ConversionMediaCategory.VIDEO) {
          args.addAll(listOf("-frames:v", "1"))
        }
        args.addAll(listOf("-q:v", "2"))
      }
      ConvertibleFormat.WEBP -> {
        if (source.category == ConversionMediaCategory.VIDEO) {
          args.addAll(listOf("-frames:v", "1"))
        }
        args.addAll(listOf("-quality", "90"))
      }
      ConvertibleFormat.BMP -> {
        if (source.category == ConversionMediaCategory.VIDEO) {
          args.addAll(listOf("-frames:v", "1"))
        }
      }
      ConvertibleFormat.GIF -> {
        if (source.category == ConversionMediaCategory.VIDEO) {
          args.addAll(listOf("-vf", "fps=10,scale=480:-1:flags=lanczos", "-loop", "0"))
        }
      }
      ConvertibleFormat.ICO -> {
        args.addAll(listOf("-vf", "scale='min(256,iw)':'min(256,ih)':force_original_aspect_ratio=decrease"))
      }
      ConvertibleFormat.TIFF -> {
        args.addAll(listOf("-compression_algo", "lzw"))
      }
      ConvertibleFormat.HEIC -> {
        args.addAll(listOf("-c:v", "libx265", "-tag:v", "hvc1"))
      }

      // Audio targets
      ConvertibleFormat.MP3 -> {
        args.addAll(listOf("-vn", "-b:a", "192k"))
      }
      ConvertibleFormat.WAV -> {
        args.addAll(listOf("-vn", "-c:a", "pcm_s16le", "-ar", "44100", "-ac", "2"))
      }
      ConvertibleFormat.M4A -> {
        args.addAll(listOf("-vn", "-c:a", "aac", "-b:a", "192k"))
      }
      ConvertibleFormat.AAC -> {
        args.addAll(listOf("-vn", "-c:a", "aac", "-b:a", "192k", "-f", "adts"))
      }
      ConvertibleFormat.FLAC -> {
        args.addAll(listOf("-vn", "-c:a", "flac"))
      }
      ConvertibleFormat.OGG -> {
        args.addAll(listOf("-vn", "-b:a", "160k"))
      }
      ConvertibleFormat.OPUS -> {
        args.addAll(listOf("-vn", "-c:a", "libopus", "-b:a", "128k"))
      }
      ConvertibleFormat.WMA -> {
        args.addAll(listOf("-vn", "-c:a", "wmav2", "-b:a", "160k"))
      }
      ConvertibleFormat.AIFF -> {
        args.addAll(listOf("-vn", "-c:a", "pcm_s16be"))
      }
      ConvertibleFormat.THREE_GPP -> {
        args.addAll(listOf("-vn", "-ar", "8000", "-ac", "1", "-b:a", "12.2k"))
      }

      // Video targets
      ConvertibleFormat.MP4 -> {
        args.addAll(listOf("-c:v", "mpeg4", "-q:v", "5", "-c:a", "aac", "-b:a", "128k", "-movflags", "+faststart"))
      }
      ConvertibleFormat.MKV -> {
        args.addAll(listOf("-c:v", "mpeg4", "-q:v", "5", "-c:a", "aac"))
      }
      ConvertibleFormat.WEBM -> {
        args.addAll(listOf("-b:v", "1M"))
      }
      ConvertibleFormat.MOV -> {
        args.addAll(listOf("-c:v", "mpeg4", "-q:v", "5", "-c:a", "aac"))
      }
      ConvertibleFormat.AVI -> {
        args.addAll(listOf("-c:v", "mpeg4", "-q:v", "5", "-c:a", "mp3"))
      }
      ConvertibleFormat.FLV -> {
        args.addAll(listOf("-c:v", "flv", "-q:v", "5", "-ar", "44100"))
      }
      ConvertibleFormat.MPEG -> {
        args.addAll(listOf("-c:v", "mpeg2video", "-q:v", "5", "-c:a", "mp2"))
      }
      ConvertibleFormat.TS -> {
        args.addAll(listOf("-c:v", "mpeg2video", "-q:v", "5", "-c:a", "mp2", "-f", "mpegts"))
      }

      else -> {
        // Default pass-through
      }
    }

    args.add(outputPath)
    return args
  }

  /**
   * Executes a file conversion from [sourceFile] to [targetFormat], writing the result into
   * [outputDir] with [desiredStem].
   *
   * Dynamically uses the route's [ConversionRoute.preferredBackend] (Native wherever possible)
   * and automatically falls back to [ConversionRoute.fallbackBackend] (FFmpeg or Native) if
   * the primary engine encounters an error.
   */
  suspend fun convertFile(
    context: Context?,
    sourceFile: File,
    outputFile: File,
    targetFormat: ConvertibleFormat,
  ): ConversionResult {
    val result = convertFile(
      context = context,
      sourceFile = sourceFile,
      targetFormat = targetFormat,
      outputDir = outputFile.parentFile ?: File("."),
      desiredStem = extractFileStem(outputFile.name),
    )
    if (result.success && result.outputFile != null && result.outputFile.absolutePath != outputFile.absolutePath) {
      result.outputFile.copyTo(outputFile, overwrite = true)
      result.outputFile.delete()
      return result.copy(outputFile = outputFile)
    }
    return result
  }

  suspend fun convertFile(
    context: Context?,
    sourceFile: File,
    targetFormat: ConvertibleFormat,
    outputDir: File,
    desiredStem: String = extractFileStem(sourceFile.name),
    sourceFormatOverride: ConvertibleFormat? = null,
    ffmpegRunner: (suspend (List<String>, File) -> Boolean)? = null,
  ): ConversionResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    val sourceFormat = sourceFormatOverride ?: detectFormat(sourceFile)
      ?: return@withContext ConversionResult(
        success = false,
        sourceFile = sourceFile,
        outputFile = null,
        sourceFormat = ConvertibleFormat.TXT,
        targetFormat = targetFormat,
        backendUsed = ConverterBackend.NATIVE,
        usedFallback = false,
        durationMs = 0L,
        message = "Unrecognized source file format: ${sourceFile.name}",
      )

    if (!sourceFile.exists() || !sourceFile.canRead()) {
      return@withContext ConversionResult(
        success = false,
        sourceFile = sourceFile,
        outputFile = null,
        sourceFormat = sourceFormat,
        targetFormat = targetFormat,
        backendUsed = ConverterBackend.NATIVE,
        usedFallback = false,
        durationMs = 0L,
        message = "Source file does not exist or cannot be read.",
      )
    }

    val route = getRoute(sourceFormat, targetFormat)
      ?: return@withContext ConversionResult(
        success = false,
        sourceFile = sourceFile,
        outputFile = null,
        sourceFormat = sourceFormat,
        targetFormat = targetFormat,
        backendUsed = ConverterBackend.NATIVE,
        usedFallback = false,
        durationMs = 0L,
        message = "Conversion from ${sourceFormat.displayName} to ${targetFormat.displayName} is not supported.",
      )

    if (!outputDir.exists()) {
      outputDir.mkdirs()
    }

    val outputFile = resolveOutputFile(
      outputDir = outputDir,
      desiredStem = desiredStem,
      targetFormat = targetFormat,
      sourceFile = sourceFile,
    )

    val primaryBackend = route.preferredBackend
    var primaryError: String? = null

    val primarySucceeded = try {
      executeWithBackend(
        backend = primaryBackend,
        context = context,
        sourceFile = sourceFile,
        outputFile = outputFile,
        sourceFormat = sourceFormat,
        targetFormat = targetFormat,
        ffmpegRunner = ffmpegRunner,
      )
    } catch (e: Throwable) {
      primaryError = e.message ?: e::class.java.simpleName
      false
    }

    if (primarySucceeded && outputFile.exists() && outputFile.length() > 0L) {
      val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
      return@withContext ConversionResult(
        success = true,
        sourceFile = sourceFile,
        outputFile = outputFile,
        sourceFormat = sourceFormat,
        targetFormat = targetFormat,
        backendUsed = primaryBackend,
        usedFallback = false,
        durationMs = elapsed,
        message = "Converted ${sourceFormat.displayName} → ${targetFormat.displayName} via ${primaryBackend.displayName}",
      )
    }

    // Clean up partial output before attempting fallback
    if (outputFile.exists()) {
      outputFile.delete()
    }

    val fallbackBackend = route.fallbackBackend
    if (fallbackBackend != null) {
      var fallbackError: String? = null
      val fallbackSucceeded = try {
        executeWithBackend(
          backend = fallbackBackend,
          context = context,
          sourceFile = sourceFile,
          outputFile = outputFile,
          sourceFormat = sourceFormat,
          targetFormat = targetFormat,
          ffmpegRunner = ffmpegRunner,
        )
      } catch (e: Throwable) {
        fallbackError = e.message ?: e::class.java.simpleName
        false
      }

      if (fallbackSucceeded && outputFile.exists() && outputFile.length() > 0L) {
        val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
        return@withContext ConversionResult(
          success = true,
          sourceFile = sourceFile,
          outputFile = outputFile,
          sourceFormat = sourceFormat,
          targetFormat = targetFormat,
          backendUsed = fallbackBackend,
          usedFallback = true,
          durationMs = elapsed,
          message = "Converted ${sourceFormat.displayName} → ${targetFormat.displayName} via ${fallbackBackend.displayName} (backup)",
        )
      }

      if (outputFile.exists()) {
        outputFile.delete()
      }

      val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
      return@withContext ConversionResult(
        success = false,
        sourceFile = sourceFile,
        outputFile = null,
        sourceFormat = sourceFormat,
        targetFormat = targetFormat,
        backendUsed = fallbackBackend,
        usedFallback = true,
        durationMs = elapsed,
        message = "Conversion failed (${primaryBackend.displayName}: ${primaryError ?: "failed"}; ${fallbackBackend.displayName}: ${fallbackError ?: "failed"})",
      )
    }

    val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
    ConversionResult(
      success = false,
      sourceFile = sourceFile,
      outputFile = null,
      sourceFormat = sourceFormat,
      targetFormat = targetFormat,
      backendUsed = primaryBackend,
      usedFallback = false,
      durationMs = elapsed,
      message = "Conversion failed via ${primaryBackend.displayName}${primaryError?.let { ": $it" } ?: ""}",
    )
  }

  private suspend fun executeWithBackend(
    backend: ConverterBackend,
    context: Context?,
    sourceFile: File,
    outputFile: File,
    sourceFormat: ConvertibleFormat,
    targetFormat: ConvertibleFormat,
    ffmpegRunner: (suspend (List<String>, File) -> Boolean)?,
  ): Boolean {
    return when (backend) {
      ConverterBackend.NATIVE -> executeNativeConversion(
        context = context,
        sourceFile = sourceFile,
        outputFile = outputFile,
        sourceFormat = sourceFormat,
        targetFormat = targetFormat,
      )
      ConverterBackend.FFMPEG -> executeFfmpegConversion(
        sourceFile = sourceFile,
        outputFile = outputFile,
        sourceFormat = sourceFormat,
        targetFormat = targetFormat,
        ffmpegRunner = ffmpegRunner,
      )
    }
  }

  // ============================================================================
  // NATIVE CONVERSION IMPLEMENTATIONS
  // ============================================================================

  private suspend fun executeNativeConversion(
    context: Context?,
    sourceFile: File,
    outputFile: File,
    sourceFormat: ConvertibleFormat,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    return when {
      // 1. Archive <-> Archive
      sourceFormat.category == ConversionMediaCategory.ARCHIVE &&
        targetFormat.category == ConversionMediaCategory.ARCHIVE -> {
        convertArchiveNative(sourceFile, outputFile, sourceFormat, targetFormat)
      }

      // 2. Document / Structured Data -> PDF
      (sourceFormat in TEXT_MARKUP_FORMATS || sourceFormat in STRUCTURED_DATA_FORMATS) &&
        targetFormat == ConvertibleFormat.PDF -> {
        convertTextOrDataToPdfNative(sourceFile, outputFile, sourceFormat)
      }

      // 3. Document / Structured Data <-> Document / Structured Data
      (sourceFormat in TEXT_MARKUP_FORMATS || sourceFormat in STRUCTURED_DATA_FORMATS) &&
        (targetFormat in TEXT_MARKUP_FORMATS || targetFormat in STRUCTURED_DATA_FORMATS) -> {
        convertDocumentOrDataNative(sourceFile, outputFile, sourceFormat, targetFormat)
      }

      // 4. PDF -> Image
      sourceFormat == ConvertibleFormat.PDF && targetFormat.category == ConversionMediaCategory.IMAGE -> {
        convertPdfToImageNative(sourceFile, outputFile, targetFormat)
      }

      // 5. Image -> PDF
      sourceFormat.category == ConversionMediaCategory.IMAGE && targetFormat == ConvertibleFormat.PDF -> {
        convertImageToPdfNative(sourceFile, outputFile)
      }

      // 6. Image -> Image
      sourceFormat.category == ConversionMediaCategory.IMAGE &&
        targetFormat.category == ConversionMediaCategory.IMAGE -> {
        convertImageToImageNative(sourceFile, outputFile, targetFormat)
      }

      // 7. Video -> Image (Frame extraction)
      sourceFormat.category == ConversionMediaCategory.VIDEO &&
        targetFormat.category == ConversionMediaCategory.IMAGE -> {
        convertVideoToFrameImageNative(sourceFile, outputFile, targetFormat)
      }

      // 8. Video -> MP4 (Media3 Transformer)
      sourceFormat.category == ConversionMediaCategory.VIDEO &&
        targetFormat == ConvertibleFormat.MP4 -> {
        if (context == null) return false
        convertVideoToMp4Media3(context, sourceFile, outputFile)
      }

      // 9. Audio / Video -> WAV (MediaExtractor + MediaCodec PCM decode -> RIFF WAV)
      (sourceFormat.category == ConversionMediaCategory.AUDIO || sourceFormat.category == ConversionMediaCategory.VIDEO) &&
        targetFormat == ConvertibleFormat.WAV -> {
        convertMediaToWavNative(sourceFile, outputFile)
      }

      // 10. Audio / Video -> M4A or AAC
      (sourceFormat.category == ConversionMediaCategory.AUDIO || sourceFormat.category == ConversionMediaCategory.VIDEO) &&
        (targetFormat == ConvertibleFormat.M4A || targetFormat == ConvertibleFormat.AAC) -> {
        convertMediaToAacOrM4aNative(sourceFile, outputFile, targetFormat)
      }

      else -> false
    }
  }

  /**
   * Native Image -> Image conversion using [BitmapFactory], [Bitmap.compress], and custom BMP/ICO/GIF encoders.
   */
  private fun convertImageToImageNative(
    sourceFile: File,
    outputFile: File,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return false
    return try {
      encodeBitmapToFile(bitmap, outputFile, targetFormat)
    } finally {
      bitmap.recycle()
    }
  }

  private fun encodeBitmapToFile(
    bitmap: Bitmap,
    outputFile: File,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    return when (targetFormat) {
      ConvertibleFormat.PNG -> {
        FileOutputStream(outputFile).use { out ->
          bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
      }
      ConvertibleFormat.JPG -> {
        // Composite onto white background if source has alpha channel
        val opaque = if (bitmap.hasAlpha()) {
          val bg = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
          val canvas = Canvas(bg)
          canvas.drawColor(Color.WHITE)
          canvas.drawBitmap(bitmap, 0f, 0f, null)
          bg
        } else {
          bitmap
        }
        try {
          FileOutputStream(outputFile).use { out ->
            opaque.compress(Bitmap.CompressFormat.JPEG, 92, out)
          }
        } finally {
          if (opaque !== bitmap) opaque.recycle()
        }
      }
      ConvertibleFormat.WEBP -> {
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          Bitmap.CompressFormat.WEBP_LOSSY
        } else {
          @Suppress("DEPRECATION")
          Bitmap.CompressFormat.WEBP
        }
        FileOutputStream(outputFile).use { out ->
          bitmap.compress(format, 90, out)
        }
      }
      ConvertibleFormat.BMP -> {
        writeBmpFile(bitmap, outputFile)
      }
      ConvertibleFormat.ICO -> {
        writeIcoFile(bitmap, outputFile)
      }
      ConvertibleFormat.GIF -> {
        writeSingleFrameGifFile(bitmap, outputFile)
      }
      else -> false
    }
  }

  /**
   * Writes an uncompressed 32-bit BGRA BMP file from [bitmap].
   */
  private fun writeBmpFile(bitmap: Bitmap, outputFile: File): Boolean {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) return false
    val rowBytes = width * 4
    val imageSize = rowBytes * height
    val fileSize = 54 + imageSize

    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    FileOutputStream(outputFile).use { fos ->
      val out = BufferedOutputStream(fos)
      val header = ByteBuffer.allocate(54).order(ByteOrder.LITTLE_ENDIAN)
      header.put('B'.code.toByte())
      header.put('M'.code.toByte())
      header.putInt(fileSize)
      header.putShort(0)
      header.putShort(0)
      header.putInt(54)
      // DIB Header (BITMAPINFOHEADER)
      header.putInt(40)
      header.putInt(width)
      header.putInt(height) // bottom-up
      header.putShort(1) // planes
      header.putShort(32) // 32 bpp
      header.putInt(0) // BI_RGB
      header.putInt(imageSize)
      header.putInt(2835) // 72 DPI
      header.putInt(2835)
      header.putInt(0)
      header.putInt(0)
      out.write(header.array())

      val rowBuf = ByteArray(rowBytes)
      for (y in height - 1 downTo 0) {
        val rowOffset = y * width
        var bIdx = 0
        for (x in 0 until width) {
          val argb = pixels[rowOffset + x]
          rowBuf[bIdx++] = (argb and 0xFF).toByte() // B
          rowBuf[bIdx++] = ((argb shr 8) and 0xFF).toByte() // G
          rowBuf[bIdx++] = ((argb shr 16) and 0xFF).toByte() // R
          rowBuf[bIdx++] = ((argb shr 24) and 0xFF).toByte() // A
        }
        out.write(rowBuf)
      }
      out.flush()
    }
    return outputFile.exists() && outputFile.length() >= 54L
  }

  /**
   * Writes a standard PNG-backed Windows `.ico` icon file (scaled up to 256x256).
   */
  private fun writeIcoFile(bitmap: Bitmap, outputFile: File): Boolean {
    val maxDim = 256
    val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
      val scale = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height)
      val w = (bitmap.width * scale).toInt().coerceIn(1, maxDim)
      val h = (bitmap.height * scale).toInt().coerceIn(1, maxDim)
      Bitmap.createScaledBitmap(bitmap, w, h, true)
    } else {
      bitmap
    }

    try {
      val pngBytes = ByteArrayOutputStream().use { baos ->
        if (!scaled.compress(Bitmap.CompressFormat.PNG, 100, baos)) return false
        baos.toByteArray()
      }
      FileOutputStream(outputFile).use { fos ->
        val header = ByteBuffer.allocate(22).order(ByteOrder.LITTLE_ENDIAN)
        header.putShort(0) // Reserved
        header.putShort(1) // Type 1 = ICO
        header.putShort(1) // 1 image
        header.put(if (scaled.width >= 256) 0 else scaled.width.toByte())
        header.put(if (scaled.height >= 256) 0 else scaled.height.toByte())
        header.put(0) // Palette
        header.put(0) // Reserved
        header.putShort(1) // Color planes
        header.putShort(32) // Bits per pixel
        header.putInt(pngBytes.size)
        header.putInt(22) // Offset to PNG data
        fos.write(header.array())
        fos.write(pngBytes)
      }
      return outputFile.exists() && outputFile.length() > 22L
    } finally {
      if (scaled !== bitmap) scaled.recycle()
    }
  }

  /**
   * Minimal native GIF89a encoder fallback (quantizes to 3-3-2 RGB 256-color palette with uncompressed LZW blocks).
   */
  private fun writeSingleFrameGifFile(bitmap: Bitmap, outputFile: File): Boolean {
    val width = bitmap.width.coerceAtMost(1024)
    val height = bitmap.height.coerceAtMost(1024)
    val working = if (bitmap.width != width || bitmap.height != height) {
      Bitmap.createScaledBitmap(bitmap, width, height, true)
    } else {
      bitmap
    }
    try {
      val pixels = IntArray(width * height)
      working.getPixels(pixels, 0, width, 0, 0, width, height)

      FileOutputStream(outputFile).use { fos ->
        val out = BufferedOutputStream(fos)
        // Header GIF89a
        out.write("GIF89a".toByteArray(Charsets.US_ASCII))
        val lsd = ByteBuffer.allocate(7).order(ByteOrder.LITTLE_ENDIAN)
        lsd.putShort(width.toShort())
        lsd.putShort(height.toShort())
        lsd.put(0xF7.toByte()) // Global color table flag (1), 8 bits color res, 256 entries
        lsd.put(0) // Background color
        lsd.put(0) // Pixel aspect ratio
        out.write(lsd.array())

        // 256-entry 3-3-2 RGB Global Color Table
        for (i in 0 until 256) {
          val r = ((i shr 5) and 0x07) * 255 / 7
          val g = ((i shr 2) and 0x07) * 255 / 7
          val b = (i and 0x03) * 255 / 3
          out.write(r)
          out.write(g)
          out.write(b)
        }

        // Image Descriptor
        out.write(0x2C)
        val imgDesc = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN)
        imgDesc.putShort(0)
        imgDesc.putShort(0)
        imgDesc.putShort(width.toShort())
        imgDesc.putShort(height.toShort())
        imgDesc.put(0)
        out.write(imgDesc.array())

        // LZW Minimum Code Size = 8
        out.write(8)
        // Emit uncompressed 9-bit LZW codes with CLEAR (256) every 254 pixels and EOI (257) at end
        val bitPacker = GifBitPacker(out)
        bitPacker.writeCode(256, 9) // Clear code
        var runCount = 0
        for (argb in pixels) {
          val r = ((argb shr 16) and 0xFF) * 7 / 255
          val g = ((argb shr 8) and 0xFF) * 7 / 255
          val b = (argb and 0xFF) * 3 / 255
          val idx = (r shl 5) or (g shl 2) or b
          bitPacker.writeCode(idx, 9)
          runCount++
          if (runCount == 250) {
            bitPacker.writeCode(256, 9)
            runCount = 0
          }
        }
        bitPacker.writeCode(257, 9) // EOI code
        bitPacker.flush()
        out.write(0) // Block terminator
        out.write(0x3B) // GIF Trailer
        out.flush()
      }
      return outputFile.exists() && outputFile.length() > 0L
    } finally {
      if (working !== bitmap) working.recycle()
    }
  }

  private class GifBitPacker(private val out: OutputStream) {
    private var bitBuffer = 0
    private var bitCount = 0
    private val block = ByteArray(255)
    private var blockLength = 0

    fun writeCode(code: Int, bits: Int) {
      bitBuffer = bitBuffer or (code shl bitCount)
      bitCount += bits
      while (bitCount >= 8) {
        block[blockLength++] = (bitBuffer and 0xFF).toByte()
        bitBuffer = bitBuffer ushr 8
        bitCount -= 8
        if (blockLength == 255) {
          out.write(255)
          out.write(block, 0, 255)
          blockLength = 0
        }
      }
    }

    fun flush() {
      if (bitCount > 0) {
        block[blockLength++] = (bitBuffer and 0xFF).toByte()
        bitBuffer = 0
        bitCount = 0
      }
      if (blockLength > 0) {
        out.write(blockLength)
        out.write(block, 0, blockLength)
        blockLength = 0
      }
    }
  }

  /**
   * Native Image -> PDF conversion using Android's [PdfDocument].
   */
  private fun convertImageToPdfNative(
    sourceFile: File,
    outputFile: File,
  ): Boolean {
    val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return false
    val pdf = PdfDocument()
    return try {
      val pageInfo = PdfDocument.PageInfo.Builder(
        bitmap.width.coerceAtLeast(1),
        bitmap.height.coerceAtLeast(1),
        1,
      ).create()
      val page = pdf.startPage(pageInfo)
      page.canvas.drawColor(Color.WHITE)
      page.canvas.drawBitmap(bitmap, 0f, 0f, null)
      pdf.finishPage(page)
      FileOutputStream(outputFile).use { out ->
        pdf.writeTo(out)
      }
      outputFile.exists() && outputFile.length() > 0L
    } finally {
      pdf.close()
      bitmap.recycle()
    }
  }

  /**
   * Native PDF -> Image conversion using Android's [PdfRenderer] (renders Page 1 at 2x resolution).
   */
  private fun convertPdfToImageNative(
    sourceFile: File,
    outputFile: File,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    val pfd = ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY) ?: return false
    return pfd.use { descriptor ->
      PdfRenderer(descriptor).use { renderer ->
        if (renderer.pageCount <= 0) return false
        renderer.openPage(0).use { page ->
          val width = (page.width * 2).coerceIn(1, 4096)
          val height = (page.height * 2).coerceIn(1, 4096)
          val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
          try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            encodeBitmapToFile(bitmap, outputFile, targetFormat)
          } finally {
            bitmap.recycle()
          }
        }
      }
    }
  }

  /**
   * Native Video -> Image frame extraction using [MediaMetadataRetriever].
   */
  private fun convertVideoToFrameImageNative(
    sourceFile: File,
    outputFile: File,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    val retriever = MediaMetadataRetriever()
    return try {
      retriever.setDataSource(sourceFile.absolutePath)
      val bitmap = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        ?: retriever.frameAtTime
        ?: return false
      try {
        encodeBitmapToFile(bitmap, outputFile, targetFormat)
      } finally {
        bitmap.recycle()
      }
    } finally {
      try {
        retriever.release()
      } catch (_: Exception) {}
    }
  }

  /**
   * Native Video -> MP4 transcoding using AndroidX Media3 [Transformer].
   */
  private suspend fun convertVideoToMp4Media3(
    context: Context,
    sourceFile: File,
    outputFile: File,
  ): Boolean = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine<Boolean> { continuation ->
      val transformer = Transformer.Builder(context)
        .setVideoMimeType(MimeTypes.VIDEO_H264)
        .setAudioMimeType(MimeTypes.AUDIO_AAC)
        .addListener(
          object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
              if (continuation.isActive) {
                continuation.resume(outputFile.exists() && outputFile.length() > 0L)
              }
            }

            override fun onError(
              composition: Composition,
              exportResult: ExportResult,
              exportException: ExportException,
            ) {
              if (continuation.isActive) {
                continuation.resume(false)
              }
            }
          }
        )
        .build()

      val mediaItem = MediaItem.fromUri(Uri.fromFile(sourceFile))
      val editedMediaItem = EditedMediaItem.Builder(mediaItem).build()
      transformer.start(editedMediaItem, outputFile.absolutePath)

      continuation.invokeOnCancellation {
        try {
          transformer.cancel()
        } catch (_: Exception) {}
      }
    }
  }

  /**
   * Native Audio/Video -> WAV conversion using Android [MediaExtractor] + [MediaCodec]
   * to decode the audio track into 16-bit PCM and write a RIFF WAV container.
   */
  internal fun convertMediaToWavNative(
    sourceFile: File,
    outputFile: File,
  ): Boolean {
    val extractor = MediaExtractor()
    try {
      extractor.setDataSource(sourceFile.absolutePath)
      var audioTrackIndex = -1
      var inputFormat: MediaFormat? = null
      for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
        if (mime.startsWith("audio/")) {
          audioTrackIndex = i
          inputFormat = format
          break
        }
      }
      if (audioTrackIndex < 0 || inputFormat == null) return false
      extractor.selectTrack(audioTrackIndex)

      val mime = inputFormat.getString(MediaFormat.KEY_MIME) ?: return false
      var sampleRate = if (inputFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
        inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
      } else {
        44100
      }
      var channelCount = if (inputFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
        inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
      } else {
        1
      }

      val codec = MediaCodec.createDecoderByType(mime)
      codec.configure(inputFormat, null, null, 0)
      codec.start()

      var totalPcmBytes = 0L
      RandomAccessFile(outputFile, "rw").use { raf ->
        raf.setLength(0L)
        // Reserve 44-byte WAV header
        raf.write(ByteArray(44))

        val bufferInfo = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false

        try {
          while (!outputDone) {
            if (!inputDone) {
              val inIndex = codec.dequeueInputBuffer(10_000L)
              if (inIndex >= 0) {
                val inputBuffer = codec.getInputBuffer(inIndex)!!
                val sampleSize = extractor.readSampleData(inputBuffer, 0)
                if (sampleSize < 0) {
                  codec.queueInputBuffer(
                    inIndex,
                    0,
                    0,
                    0L,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                  )
                  inputDone = true
                } else {
                  val presentationTimeUs = extractor.sampleTime
                  codec.queueInputBuffer(inIndex, 0, sampleSize, presentationTimeUs, 0)
                  extractor.advance()
                }
              }
            }

            val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000L)
            when {
              outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                val outFormat = codec.outputFormat
                if (outFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                  sampleRate = outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                }
                if (outFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                  channelCount = outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                }
              }
              outIndex >= 0 -> {
                val outputBuffer = codec.getOutputBuffer(outIndex)
                if (outputBuffer != null && bufferInfo.size > 0) {
                  outputBuffer.position(bufferInfo.offset)
                  outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                  val chunk = ByteArray(bufferInfo.size)
                  outputBuffer.get(chunk)
                  raf.write(chunk)
                  totalPcmBytes += chunk.size
                }
                codec.releaseOutputBuffer(outIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                  outputDone = true
                }
              }
            }
          }
        } finally {
          try {
            codec.stop()
          } catch (_: Exception) {}
          try {
            codec.release()
          } catch (_: Exception) {}
        }

        if (totalPcmBytes <= 0L) return false
        raf.seek(0L)
        raf.write(
          buildWavHeader(
            pcmDataLength = totalPcmBytes,
            sampleRate = sampleRate,
            channels = channelCount.coerceIn(1, 2),
            bitsPerSample = 16,
          )
        )
      }
      return outputFile.exists() && outputFile.length() > 44L
    } finally {
      try {
        extractor.release()
      } catch (_: Exception) {}
    }
  }

  internal fun buildWavHeader(
    pcmDataLength: Long,
    sampleRate: Int,
    channels: Int,
    bitsPerSample: Int,
  ): ByteArray {
    return AudioRecorderEngine.createWavHeader(
      pcmDataLength = pcmDataLength,
      sampleRate = sampleRate,
      channels = channels,
      bitsPerSample = bitsPerSample,
    )
  }

  /**
   * Native Audio/Video -> M4A or AAC conversion using Android's [MediaExtractor], [MediaCodec], and [MediaMuxer].
   * If the source audio track is already AAC, remuxes directly without lossy re-encoding!
   */
  private fun convertMediaToAacOrM4aNative(
    sourceFile: File,
    outputFile: File,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    val extractor = MediaExtractor()
    try {
      extractor.setDataSource(sourceFile.absolutePath)
      var audioTrackIndex = -1
      var inputFormat: MediaFormat? = null
      for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
        if (mime.startsWith("audio/")) {
          audioTrackIndex = i
          inputFormat = format
          break
        }
      }
      if (audioTrackIndex < 0 || inputFormat == null) return false
      extractor.selectTrack(audioTrackIndex)

      val inputMime = inputFormat.getString(MediaFormat.KEY_MIME) ?: return false

      // Fast lossless remux path if input is already AAC and target is M4A
      if (inputMime == MediaFormat.MIMETYPE_AUDIO_AAC && targetFormat == ConvertibleFormat.M4A) {
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        try {
          val dstTrack = muxer.addTrack(inputFormat)
          muxer.start()
          val buf = ByteBuffer.allocate(256 * 1024)
          val info = MediaCodec.BufferInfo()
          while (true) {
            val sampleSize = extractor.readSampleData(buf, 0)
            if (sampleSize < 0) break
            info.offset = 0
            info.size = sampleSize
            info.presentationTimeUs = extractor.sampleTime.coerceAtLeast(0L)
            info.flags = extractor.sampleFlags
            muxer.writeSampleData(dstTrack, buf, info)
            extractor.advance()
          }
          muxer.stop()
          return outputFile.exists() && outputFile.length() > 0L
        } finally {
          try {
            muxer.release()
          } catch (_: Exception) {}
        }
      }

      // Decode input to PCM in a temporary WAV file, then encode to AAC via MediaCodec
      val tempWav = File.createTempFile("conv_pcm_", ".wav", outputFile.parentFile)
      try {
        if (!convertMediaToWavNative(sourceFile, tempWav)) return false
        return encodeWavToAacOrM4aNative(tempWav, outputFile, targetFormat)
      } finally {
        tempWav.delete()
      }
    } finally {
      try {
        extractor.release()
      } catch (_: Exception) {}
    }
  }

  internal fun encodeWavToAacOrM4aNative(
    wavFile: File,
    outputFile: File,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    if (wavFile.length() <= 44L) return false
    RandomAccessFile(wavFile, "r").use { raf ->
      val header = ByteArray(44)
      raf.readFully(header)
      val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
      val channels = bb.getShort(22).toInt().coerceIn(1, 2)
      val sampleRate = bb.getInt(24).coerceIn(8000, 48000)

      val aacFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channels).apply {
        setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        setInteger(MediaFormat.KEY_BIT_RATE, 128_000)
        setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
      }

      val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
      encoder.configure(aacFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
      encoder.start()

      var muxer: MediaMuxer? = null
      var muxerTrackIndex = -1
      var muxerStarted = false
      var adtsOut: BufferedOutputStream? = null

      if (targetFormat == ConvertibleFormat.M4A) {
        muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
      } else {
        adtsOut = BufferedOutputStream(FileOutputStream(outputFile))
      }

      val bufferInfo = MediaCodec.BufferInfo()
      val readBuf = ByteArray(8192)
      val bytesPerSec = (sampleRate * channels * 2).toLong().coerceAtLeast(1L)
      var totalReadBytes = 0L
      var inputDone = false
      var outputDone = false

      try {
        while (!outputDone) {
          if (!inputDone) {
            val inIdx = encoder.dequeueInputBuffer(10_000L)
            if (inIdx >= 0) {
              val inBuf = encoder.getInputBuffer(inIdx)!!
              inBuf.clear()
              val maxToRead = minOf(readBuf.size, inBuf.remaining())
              val read = raf.read(readBuf, 0, maxToRead)
              if (read <= 0) {
                val ptsUs = (totalReadBytes * 1_000_000L) / bytesPerSec
                encoder.queueInputBuffer(inIdx, 0, 0, ptsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                inputDone = true
              } else {
                inBuf.put(readBuf, 0, read)
                val ptsUs = (totalReadBytes * 1_000_000L) / bytesPerSec
                totalReadBytes += read
                encoder.queueInputBuffer(inIdx, 0, read, ptsUs, 0)
              }
            }
          }

          val outIdx = encoder.dequeueOutputBuffer(bufferInfo, 10_000L)
          when {
            outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
              if (muxer != null && !muxerStarted) {
                muxerTrackIndex = muxer.addTrack(encoder.outputFormat)
                muxer.start()
                muxerStarted = true
              }
            }
            outIdx >= 0 -> {
              val outBuf = encoder.getOutputBuffer(outIdx)
              val isCodecConfig = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0
              if (outBuf != null && bufferInfo.size > 0 && !isCodecConfig) {
                outBuf.position(bufferInfo.offset)
                outBuf.limit(bufferInfo.offset + bufferInfo.size)
                if (muxer != null && muxerStarted) {
                  muxer.writeSampleData(muxerTrackIndex, outBuf, bufferInfo)
                } else if (adtsOut != null) {
                  val packetSize = bufferInfo.size + 7
                  val adtsHeader = buildAdtsHeader(packetSize, sampleRate, channels)
                  val aacBytes = ByteArray(bufferInfo.size)
                  outBuf.get(aacBytes)
                  adtsOut.write(adtsHeader)
                  adtsOut.write(aacBytes)
                }
              }
              encoder.releaseOutputBuffer(outIdx, false)
              if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                outputDone = true
              }
            }
          }
        }
      } finally {
        try {
          encoder.stop()
        } catch (_: Exception) {}
        try {
          encoder.release()
        } catch (_: Exception) {}
        try {
          if (muxerStarted) muxer?.stop()
        } catch (_: Exception) {}
        try {
          muxer?.release()
        } catch (_: Exception) {}
        try {
          adtsOut?.flush()
          adtsOut?.close()
        } catch (_: Exception) {}
      }
    }
    return outputFile.exists() && outputFile.length() > 0L
  }

  private fun buildAdtsHeader(packetLength: Int, sampleRate: Int, channelCount: Int): ByteArray {
    val freqIdx = when (sampleRate) {
      96000 -> 0
      88200 -> 1
      64000 -> 2
      48000 -> 3
      44100 -> 4
      32000 -> 5
      24000 -> 6
      22050 -> 7
      16000 -> 8
      12000 -> 9
      11025 -> 10
      8000 -> 11
      else -> 4
    }
    val profile = 2 // AAC LC
    val chanCfg = channelCount.coerceIn(1, 2)
    val header = ByteArray(7)
    header[0] = 0xFF.toByte()
    header[1] = 0xF9.toByte()
    header[2] = (((profile - 1) shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
    header[3] = (((chanCfg and 3) shl 6) + (packetLength shr 11)).toByte()
    header[4] = ((packetLength and 0x7FF) shr 3).toByte()
    header[5] = (((packetLength and 7) shl 5) + 0x1F).toByte()
    header[6] = 0xFC.toByte()
    return header
  }

  // ============================================================================
  // NATIVE DOCUMENT, MARKUP, PDF & STRUCTURED DATA CONVERSIONS
  // ============================================================================

  private fun convertTextOrDataToPdfNative(
    sourceFile: File,
    outputFile: File,
    sourceFormat: ConvertibleFormat,
  ): Boolean {
    val rawContent = sourceFile.readText(Charsets.UTF_8)
    val plainText = when (sourceFormat) {
      ConvertibleFormat.HTML -> stripHtmlToPlainText(rawContent)
      else -> rawContent
    }
    val pdf = PdfDocument()
    try {
      val pageWidth = 595 // A4 width in points
      val pageHeight = 842 // A4 height in points
      val margin = 40f
      val usableWidth = pageWidth - margin * 2
      val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 11f
      }
      val lineHeight = 16f
      val maxLinesPerPage = ((pageHeight - margin * 2) / lineHeight).toInt().coerceAtLeast(1)

      val wrappedLines = mutableListOf<String>()
      for (paragraph in plainText.replace("\r\n", "\n").split("\n")) {
        if (paragraph.isEmpty()) {
          wrappedLines.add("")
          continue
        }
        var start = 0
        while (start < paragraph.length) {
          val count = paint.breakText(paragraph, start, paragraph.length, true, usableWidth, null)
          if (count <= 0) break
          wrappedLines.add(paragraph.substring(start, start + count))
          start += count
        }
      }
      if (wrappedLines.isEmpty()) {
        wrappedLines.add(" ")
      }

      val chunks = wrappedLines.chunked(maxLinesPerPage)
      chunks.forEachIndexed { index, pageLines ->
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
        val page = pdf.startPage(pageInfo)
        page.canvas.drawColor(Color.WHITE)
        var y = margin + lineHeight
        for (line in pageLines) {
          page.canvas.drawText(line, margin, y, paint)
          y += lineHeight
        }
        pdf.finishPage(page)
      }

      FileOutputStream(outputFile).use { out ->
        pdf.writeTo(out)
      }
      return outputFile.exists() && outputFile.length() > 0L
    } finally {
      pdf.close()
    }
  }

  private fun convertDocumentOrDataNative(
    sourceFile: File,
    outputFile: File,
    sourceFormat: ConvertibleFormat,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    val content = sourceFile.readText(Charsets.UTF_8)

    // Structured data conversions (CSV, TSV, JSON, XML -> CSV, TSV, JSON, XML, HTML, MD, TXT)
    if (sourceFormat in STRUCTURED_DATA_FORMATS) {
      val rows = parseStructuredRows(content, sourceFormat)
      val formatted = formatStructuredRows(rows, targetFormat)
      outputFile.writeText(formatted, Charsets.UTF_8)
      return outputFile.exists() && outputFile.length() > 0L
    }

    // Text / Markup conversions (TXT, MD, HTML)
    val result = when (sourceFormat to targetFormat) {
      ConvertibleFormat.MD to ConvertibleFormat.HTML -> convertMarkdownToHtml(content)
      ConvertibleFormat.MD to ConvertibleFormat.TXT -> stripMarkdownToPlainText(content)
      ConvertibleFormat.TXT to ConvertibleFormat.HTML -> convertPlainTextToHtml(content, sourceFile.nameWithoutExtension)
      ConvertibleFormat.TXT to ConvertibleFormat.MD -> "# ${sourceFile.nameWithoutExtension}\n\n$content\n"
      ConvertibleFormat.HTML to ConvertibleFormat.TXT -> stripHtmlToPlainText(content)
      ConvertibleFormat.HTML to ConvertibleFormat.MD -> convertHtmlToMarkdown(content)
      else -> content
    }
    outputFile.writeText(result.ifEmpty { " " }, Charsets.UTF_8)
    return outputFile.exists() && outputFile.length() > 0L
  }

  private fun parseStructuredRows(
    content: String,
    sourceFormat: ConvertibleFormat,
  ): List<Map<String, String>> {
    return when (sourceFormat) {
      ConvertibleFormat.CSV -> parseDelimitedTable(content, ',')
      ConvertibleFormat.TSV -> parseDelimitedTable(content, '\t')
      ConvertibleFormat.JSON -> parseJsonRecords(content)
      ConvertibleFormat.XML -> parseXmlRecords(content)
      else -> emptyList()
    }
  }

  private fun parseDelimitedTable(content: String, delimiter: Char): List<Map<String, String>> {
    val lines = content.replace("\r\n", "\n").lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) return emptyList()
    val headers = splitDelimitedLine(lines.first(), delimiter).mapIndexed { i, h ->
      h.ifBlank { "col_${i + 1}" }
    }
    if (lines.size == 1) {
      return listOf(headers.associateWith { "" })
    }
    return lines.drop(1).map { line ->
      val values = splitDelimitedLine(line, delimiter)
      headers.mapIndexed { idx, header ->
        header to (values.getOrNull(idx) ?: "")
      }.toMap(linkedMapOf())
    }
  }

  private fun splitDelimitedLine(line: String, delimiter: Char): List<String> {
    val result = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
      val ch = line[i]
      when {
        ch == '"' -> {
          if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
            sb.append('"')
            i++
          } else {
            inQuotes = !inQuotes
          }
        }
        ch == delimiter && !inQuotes -> {
          result.add(sb.toString().trim())
          sb.setLength(0)
        }
        else -> sb.append(ch)
      }
      i++
    }
    result.add(sb.toString().trim())
    return result
  }

  private fun parseJsonRecords(content: String): List<Map<String, String>> {
    val trimmed = content.trim()
    val rows = mutableListOf<Map<String, String>>()
    if (trimmed.startsWith("[")) {
      val array = JSONArray(trimmed)
      for (i in 0 until array.length()) {
        val item = array.opt(i)
        if (item is JSONObject) {
          val map = linkedMapOf<String, String>()
          val keys = item.keys()
          while (keys.hasNext()) {
            val k = keys.next()
            map[k] = item.opt(k)?.toString() ?: ""
          }
          rows.add(map)
        } else {
          rows.add(mapOf("value" to (item?.toString() ?: "")))
        }
      }
    } else if (trimmed.startsWith("{")) {
      val obj = JSONObject(trimmed)
      val map = linkedMapOf<String, String>()
      val keys = obj.keys()
      while (keys.hasNext()) {
        val k = keys.next()
        map[k] = obj.opt(k)?.toString() ?: ""
      }
      rows.add(map)
    }
    return rows
  }

  private fun parseXmlRecords(content: String): List<Map<String, String>> {
    val rows = mutableListOf<Map<String, String>>()
    val rowRegex = Regex("<(?:row|item|record|entry)>([\\s\\S]*?)</(?:row|item|record|entry)>", RegexOption.IGNORE_CASE)
    val fieldRegex = Regex("<([a-zA-Z0-9_\\-]+)>([\\s\\S]*?)</\\1>")
    val matches = rowRegex.findAll(content).toList()
    if (matches.isNotEmpty()) {
      for (match in matches) {
        val inner = match.groupValues[1]
        val map = linkedMapOf<String, String>()
        for (field in fieldRegex.findAll(inner)) {
          map[field.groupValues[1]] = unescapeXml(field.groupValues[2].trim())
        }
        if (map.isNotEmpty()) rows.add(map)
      }
    } else {
      val map = linkedMapOf<String, String>()
      for (field in fieldRegex.findAll(content)) {
        map[field.groupValues[1]] = unescapeXml(field.groupValues[2].trim())
      }
      if (map.isNotEmpty()) rows.add(map)
    }
    return rows
  }

  private fun formatStructuredRows(
    rows: List<Map<String, String>>,
    targetFormat: ConvertibleFormat,
  ): String {
    val headers = rows.flatMap { it.keys }.distinct()
    return when (targetFormat) {
      ConvertibleFormat.CSV -> formatDelimitedTable(rows, headers, ',')
      ConvertibleFormat.TSV -> formatDelimitedTable(rows, headers, '\t')
      ConvertibleFormat.JSON -> {
        val array = JSONArray()
        for (row in rows) {
          val obj = JSONObject()
          for ((k, v) in row) {
            obj.put(k, v)
          }
          array.put(obj)
        }
        array.toString(2)
      }
      ConvertibleFormat.XML -> {
        buildString {
          appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
          appendLine("<records>")
          for (row in rows) {
            appendLine("  <record>")
            for ((k, v) in row) {
              val safeTag = k.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "field" }
              appendLine("    <$safeTag>${escapeXml(v)}</$safeTag>")
            }
            appendLine("  </record>")
          }
          appendLine("</records>")
        }
      }
      ConvertibleFormat.HTML -> {
        buildString {
          appendLine("<!DOCTYPE html>")
          appendLine("<html><head><meta charset=\"UTF-8\"><title>Converted Table</title></head><body>")
          appendLine("<table border=\"1\" cellspacing=\"0\" cellpadding=\"6\">")
          if (headers.isNotEmpty()) {
            append("  <thead><tr>")
            headers.forEach { h -> append("<th>${escapeXml(h)}</th>") }
            appendLine("</tr></thead>")
          }
          appendLine("  <tbody>")
          for (row in rows) {
            append("    <tr>")
            headers.forEach { h -> append("<td>${escapeXml(row[h] ?: "")}</td>") }
            appendLine("</tr>")
          }
          appendLine("  </tbody>")
          appendLine("</table></body></html>")
        }
      }
      ConvertibleFormat.MD -> {
        if (headers.isEmpty()) return ""
        buildString {
          appendLine("| " + headers.joinToString(" | ") + " |")
          appendLine("| " + headers.joinToString(" | ") { "---" } + " |")
          for (row in rows) {
            appendLine("| " + headers.joinToString(" | ") { (row[it] ?: "").replace("|", "\\|") } + " |")
          }
        }
      }
      else -> {
        rows.joinToString("\n") { row ->
          row.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        }.ifEmpty { " " }
      }
    }
  }

  private fun formatDelimitedTable(
    rows: List<Map<String, String>>,
    headers: List<String>,
    delimiter: Char,
  ): String {
    if (headers.isEmpty()) return "\n"
    fun escapeCell(cell: String): String {
      return if (cell.contains(delimiter) || cell.contains('"') || cell.contains('\n')) {
        "\"${cell.replace("\"", "\"\"")}\""
      } else {
        cell
      }
    }
    return buildString {
      appendLine(headers.joinToString(delimiter.toString()) { escapeCell(it) })
      for (row in rows) {
        appendLine(headers.joinToString(delimiter.toString()) { h -> escapeCell(row[h] ?: "") })
      }
    }
  }

  private fun convertMarkdownToHtml(md: String): String {
    val lines = md.replace("\r\n", "\n").lines()
    return buildString {
      appendLine("<!DOCTYPE html>")
      appendLine("<html><head><meta charset=\"UTF-8\"><title>Document</title></head><body>")
      for (line in lines) {
        val trimmed = line.trim()
        when {
          trimmed.startsWith("### ") -> appendLine("<h3>${escapeXml(trimmed.removePrefix("### "))}</h3>")
          trimmed.startsWith("## ") -> appendLine("<h2>${escapeXml(trimmed.removePrefix("## "))}</h2>")
          trimmed.startsWith("# ") -> appendLine("<h1>${escapeXml(trimmed.removePrefix("# "))}</h1>")
          trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
            appendLine("<li>${escapeXml(trimmed.substring(2))}</li>")
          }
          trimmed.isEmpty() -> appendLine("<br/>")
          else -> appendLine("<p>${escapeXml(line)}</p>")
        }
      }
      appendLine("</body></html>")
    }
  }

  private fun stripMarkdownToPlainText(md: String): String {
    return md
      .replace(Regex("^#{1,6}\\s+", RegexOption.MULTILINE), "")
      .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
      .replace(Regex("\\*(.*?)\\*"), "$1")
      .replace(Regex("`(.*?)`"), "$1")
      .replace(Regex("\\[(.*?)]\\((.*?)\\)"), "$1 ($2)")
  }

  private fun convertPlainTextToHtml(text: String, title: String): String {
    return buildString {
      appendLine("<!DOCTYPE html>")
      appendLine("<html><head><meta charset=\"UTF-8\"><title>${escapeXml(title)}</title></head><body>")
      for (line in text.replace("\r\n", "\n").lines()) {
        appendLine("<p>${escapeXml(line)}</p>")
      }
      appendLine("</body></html>")
    }
  }

  private fun stripHtmlToPlainText(html: String): String {
    return unescapeXml(
      html
        .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</(p|div|h[1-6]|li|tr)>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), "")
    ).trim()
  }

  private fun convertHtmlToMarkdown(html: String): String {
    return unescapeXml(
      html
        .replace(Regex("<h1[^>]*>([\\s\\S]*?)</h1>", RegexOption.IGNORE_CASE), "# $1\n")
        .replace(Regex("<h2[^>]*>([\\s\\S]*?)</h2>", RegexOption.IGNORE_CASE), "## $1\n")
        .replace(Regex("<h3[^>]*>([\\s\\S]*?)</h3>", RegexOption.IGNORE_CASE), "### $1\n")
        .replace(Regex("<li[^>]*>([\\s\\S]*?)</li>", RegexOption.IGNORE_CASE), "- $1\n")
        .replace(Regex("<strong[^>]*>([\\s\\S]*?)</strong>", RegexOption.IGNORE_CASE), "**$1**")
        .replace(Regex("<b[^>]*>([\\s\\S]*?)</b>", RegexOption.IGNORE_CASE), "**$1**")
        .replace(Regex("<em[^>]*>([\\s\\S]*?)</em>", RegexOption.IGNORE_CASE), "*$1*")
        .replace(Regex("<i[^>]*>([\\s\\S]*?)</i>", RegexOption.IGNORE_CASE), "*$1*")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
        .replace(Regex("<[^>]+>"), "")
    ).trim()
  }

  private fun escapeXml(s: String): String {
    return s
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
  }

  private fun unescapeXml(s: String): String {
    return s
      .replace("&quot;", "\"")
      .replace("&gt;", ">")
      .replace("&lt;", "<")
      .replace("&amp;", "&")
      .replace("&nbsp;", " ")
  }

  // ============================================================================
  // NATIVE ARCHIVE CONVERSIONS (ZIP <-> TAR <-> TAR.GZ)
  // ============================================================================

  private data class ArchiveEntryData(
    val name: String,
    val bytes: ByteArray,
    val lastModifiedMs: Long,
  )

  private fun convertArchiveNative(
    sourceFile: File,
    outputFile: File,
    sourceFormat: ConvertibleFormat,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    val entries = readArchiveEntries(sourceFile, sourceFormat)
    if (entries.isEmpty()) return false
    return writeArchiveEntries(entries, outputFile, targetFormat)
  }

  private fun readArchiveEntries(sourceFile: File, sourceFormat: ConvertibleFormat): List<ArchiveEntryData> {
    val entries = mutableListOf<ArchiveEntryData>()
    when (sourceFormat) {
      ConvertibleFormat.ZIP -> {
        ZipInputStream(BufferedInputStream(FileInputStream(sourceFile))).use { zis ->
          var entry = zis.nextEntry
          while (entry != null) {
            if (!entry.isDirectory) {
              val bytes = zis.readBytes()
              entries.add(
                ArchiveEntryData(
                  name = entry.name.removePrefix("/"),
                  bytes = bytes,
                  lastModifiedMs = entry.time.takeIf { it > 0 } ?: System.currentTimeMillis(),
                )
              )
            }
            zis.closeEntry()
            entry = zis.nextEntry
          }
        }
      }
      ConvertibleFormat.TAR -> {
        BufferedInputStream(FileInputStream(sourceFile)).use { bis ->
          entries.addAll(readTarStream(bis))
        }
      }
      ConvertibleFormat.TAR_GZ -> {
        GZIPInputStream(BufferedInputStream(FileInputStream(sourceFile))).use { gzis ->
          entries.addAll(readTarStream(gzis))
        }
      }
      else -> {}
    }
    return entries
  }

  private fun writeArchiveEntries(
    entries: List<ArchiveEntryData>,
    outputFile: File,
    targetFormat: ConvertibleFormat,
  ): Boolean {
    when (targetFormat) {
      ConvertibleFormat.ZIP -> {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
          for (entry in entries) {
            val zipEntry = ZipEntry(entry.name).apply {
              time = entry.lastModifiedMs
            }
            zos.putNextEntry(zipEntry)
            zos.write(entry.bytes)
            zos.closeEntry()
          }
        }
      }
      ConvertibleFormat.TAR -> {
        BufferedOutputStream(FileOutputStream(outputFile)).use { bos ->
          writeTarStream(entries, bos)
        }
      }
      ConvertibleFormat.TAR_GZ -> {
        GZIPOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { gzos ->
          writeTarStream(entries, gzos)
        }
      }
      else -> return false
    }
    return outputFile.exists() && outputFile.length() > 0L
  }

  private fun readTarStream(input: InputStream): List<ArchiveEntryData> {
    val entries = mutableListOf<ArchiveEntryData>()
    val header = ByteArray(512)
    while (true) {
      val read = readFullyOrEof(input, header)
      if (read < 512 || header.all { it == 0.toByte() }) break
      val name = extractNullTerminatedString(header, 0, 100)
      val sizeOctal = extractNullTerminatedString(header, 124, 12).trim()
      val mtimeOctal = extractNullTerminatedString(header, 136, 12).trim()
      val typeFlag = header[156].toInt().toChar()

      val size = sizeOctal.toLongOrNull(8) ?: 0L
      val mtimeSec = mtimeOctal.toLongOrNull(8) ?: (System.currentTimeMillis() / 1000L)

      val payload = ByteArray(size.toInt())
      if (size > 0) {
        readFullyOrEof(input, payload)
        val remainder = (size % 512L).toInt()
        if (remainder != 0) {
          val pad = ByteArray(512 - remainder)
          readFullyOrEof(input, pad)
        }
      }
      if (name.isNotEmpty() && typeFlag != '5' && !name.endsWith("/")) {
        entries.add(ArchiveEntryData(name = name, bytes = payload, lastModifiedMs = mtimeSec * 1000L))
      }
    }
    return entries
  }

  private fun writeTarStream(entries: List<ArchiveEntryData>, out: OutputStream) {
    for (entry in entries) {
      val header = ByteArray(512)
      val nameBytes = entry.name.take(99).toByteArray(Charsets.UTF_8)
      System.arraycopy(nameBytes, 0, header, 0, nameBytes.size)

      writeOctalBytes("0000644", header, 100, 8)
      writeOctalBytes("0000000", header, 108, 8)
      writeOctalBytes("0000000", header, 116, 8)
      writeOctalBytes(entry.bytes.size.toString(8).padStart(11, '0'), header, 124, 12)
      writeOctalBytes((entry.lastModifiedMs / 1000L).toString(8).padStart(11, '0'), header, 136, 12)

      // Fill checksum field with spaces (0x20) before computing header checksum
      for (i in 148 until 156) header[i] = ' '.code.toByte()
      header[156] = '0'.code.toByte() // Normal file

      var checksum = 0
      for (b in header) checksum += (b.toInt() and 0xFF)
      val chkStr = checksum.toString(8).padStart(6, '0')
      val chkBytes = chkStr.toByteArray(Charsets.US_ASCII)
      System.arraycopy(chkBytes, 0, header, 148, chkBytes.size.coerceAtMost(6))
      header[154] = 0
      header[155] = ' '.code.toByte()

      out.write(header)
      out.write(entry.bytes)
      val remainder = entry.bytes.size % 512
      if (remainder != 0) {
        out.write(ByteArray(512 - remainder))
      }
    }
    // End of archive: two 512-byte zero blocks
    out.write(ByteArray(1024))
    out.flush()
  }

  private fun readFullyOrEof(input: InputStream, buffer: ByteArray): Int {
    var total = 0
    while (total < buffer.size) {
      val r = input.read(buffer, total, buffer.size - total)
      if (r <= 0) break
      total += r
    }
    return total
  }

  private fun extractNullTerminatedString(buf: ByteArray, offset: Int, length: Int): String {
    var end = offset
    val limit = offset + length
    while (end < limit && buf[end] != 0.toByte()) {
      end++
    }
    return String(buf, offset, end - offset, Charsets.UTF_8)
  }

  private fun writeOctalBytes(octal: String, dest: ByteArray, offset: Int, length: Int) {
    val bytes = octal.toByteArray(Charsets.US_ASCII)
    val count = minOf(bytes.size, length - 1)
    System.arraycopy(bytes, 0, dest, offset, count)
  }

  // ============================================================================
  // FFMPEG CONVERSION IMPLEMENTATION
  // ============================================================================

  private suspend fun executeFfmpegConversion(
    sourceFile: File,
    outputFile: File,
    sourceFormat: ConvertibleFormat,
    targetFormat: ConvertibleFormat,
    ffmpegRunner: (suspend (List<String>, File) -> Boolean)?,
  ): Boolean {
    // Special case: Image -> PDF fallback via FFmpeg (decode to temporary PNG via FFmpeg, then wrap in PDF)
    if (targetFormat == ConvertibleFormat.PDF && sourceFormat.category == ConversionMediaCategory.IMAGE) {
      val tempPng = File.createTempFile("ffmpeg_img_", ".png", outputFile.parentFile)
      try {
        val decoded = executeFfmpegConversion(
          sourceFile = sourceFile,
          outputFile = tempPng,
          sourceFormat = sourceFormat,
          targetFormat = ConvertibleFormat.PNG,
          ffmpegRunner = ffmpegRunner,
        )
        if (!decoded || !tempPng.exists()) return false
        return convertImageToPdfNative(tempPng, outputFile)
      } finally {
        tempPng.delete()
      }
    }

    val args = buildFfmpegArguments(
      inputPath = sourceFile.absolutePath,
      outputPath = outputFile.absolutePath,
      source = sourceFormat,
      target = targetFormat,
    )

    if (ffmpegRunner != null) {
      return ffmpegRunner(args, outputFile)
    }

    return try {
      val session = FFmpegKit.executeWithArguments(args.toTypedArray())
      ReturnCode.isSuccess(session.returnCode) && outputFile.exists() && outputFile.length() > 0L
    } catch (_: Throwable) {
      false
    }
  }
}
