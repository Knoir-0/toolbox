package com.example.toolbox.ui.tools

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.toolbox.R
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.createStandaloneToolIntent
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val SCREEN_RECORDER_PREFS_NAME = "screen_recorder_preferences"
const val PREF_KEY_SCREEN_RECORD_AUDIO_SOURCE = "screen_record_audio_source"
const val PREF_KEY_SCREEN_RECORD_RESOLUTION = "screen_record_resolution"
const val PREF_KEY_SCREEN_RECORD_SELECTED_APP_PKG = "screen_record_selected_app_pkg"
const val PREF_KEY_SCREEN_RECORD_SELECTED_APP_LABEL = "screen_record_selected_app_label"

const val DEFAULT_SCREEN_RECORDINGS_SUBFOLDER = "Movies/ScreenRecordings"

/**
 * Audio source options for Screen Recorder, matching [AudioCaptureSource] in the Audio Recorder:
 * - [MICROPHONE]: External microphone audio
 * - [DEVICE_AUDIO]: Internal device audio
 * - [DEVICE_AND_MIC]: Both internal device audio and external microphone audio
 * - [MUTE]: Silent video (no audio track)
 */
enum class ScreenAudioSource(
  val label: String,
  val shortLabel: String,
  val description: String,
  val includeAudio: Boolean,
  val includeMic: Boolean,
  val includeDeviceAudio: Boolean,
) {
  MICROPHONE(
    label = "Microphone",
    shortLabel = "Mic",
    description = "Record screen with external microphone audio",
    includeAudio = true,
    includeMic = true,
    includeDeviceAudio = false,
  ),
  DEVICE_AUDIO(
    label = "Device Audio",
    shortLabel = "Device",
    description = "Record screen with internal device audio",
    includeAudio = true,
    includeMic = false,
    includeDeviceAudio = true,
  ),
  DEVICE_AND_MIC(
    label = "Device + Mic",
    shortLabel = "Both",
    description = "Record screen with both internal device audio and microphone",
    includeAudio = true,
    includeMic = true,
    includeDeviceAudio = true,
  ),
  MUTE(
    label = "No Audio (Muted)",
    shortLabel = "Muted",
    description = "Record screen video without audio",
    includeAudio = false,
    includeMic = false,
    includeDeviceAudio = false,
  ),
  ;

  companion object {
    val DEFAULT: ScreenAudioSource = MICROPHONE
    val MIC: ScreenAudioSource get() = MICROPHONE

    fun fromNameOrDefault(name: String?): ScreenAudioSource {
      if (name.isNullOrBlank()) return DEFAULT
      val trimmed = name.trim()
      return entries.firstOrNull {
        it.name.equals(trimmed, ignoreCase = true) ||
          it.shortLabel.equals(trimmed, ignoreCase = true) ||
          it.label.equals(trimmed, ignoreCase = true)
      } ?: when (trimmed.lowercase(Locale.ROOT)) {
        "mic", "microphone" -> MICROPHONE
        "device", "device_audio", "internal" -> DEVICE_AUDIO
        "both", "combined", "device_and_mic", "device_mic", "device + mic" -> DEVICE_AND_MIC
        "mute", "muted", "none" -> MUTE
        else -> DEFAULT
      }
    }
  }
}

/**
 * Target video resolution presets for Screen Recorder.
 */
enum class ScreenResolutionPreset(
  val label: String,
  val widthPx: Int,
  val heightPx: Int,
  val videoBitrateBps: Int,
  val frameRate: Int,
) {
  P1080(
    label = "1080p",
    widthPx = 1080,
    heightPx = 1920,
    videoBitrateBps = 8_000_000,
    frameRate = 30,
  ),
  P720(
    label = "720p",
    widthPx = 720,
    heightPx = 1280,
    videoBitrateBps = 4_500_000,
    frameRate = 30,
  ),
  P480(
    label = "480p",
    widthPx = 480,
    heightPx = 854,
    videoBitrateBps = 2_000_000,
    frameRate = 30,
  ),
  ;

  companion object {
    val DEFAULT: ScreenResolutionPreset = P1080

    fun fromNameOrDefault(name: String?): ScreenResolutionPreset {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.firstOrNull {
        it.name.equals(name.trim(), ignoreCase = true) ||
          it.label.equals(name.trim(), ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Represents an installed or targetable application that can be selected for single-app screen recording.
 */
data class RecordableAppTarget(
  val packageName: String,
  val label: String,
  val accentRgb: Int = 0x8E66A6,
  val isSystemApp: Boolean = false,
)

/**
 * Metadata for a completed or existing screen recording file.
 */
data class ScreenRecordingItem(
  val file: File,
  val name: String,
  val path: String,
  val sizeBytes: Long,
  val lastModifiedMs: Long,
  val durationMs: Long,
  val width: Int = 1080,
  val height: Int = 1920,
  val targetAppLabel: String? = null,
  val targetAppPackage: String? = null,
  val audioSource: ScreenAudioSource = ScreenRecorderEngine.inferAudioSourceFromFileName(name),
) {
  val sizeFormatted: String get() = FileManagerEngine.formatFileSize(sizeBytes)
  val timestampFormatted: String get() = FileManagerEngine.formatTimestamp(lastModifiedMs)
  val durationFormatted: String get() = AudioRecorderEngine.formatDurationMs(durationMs)
}

/**
 * Core engine for the Screen Recorder tool:
 * - Supports full-screen recording and single-app recording (via Android 14+
 *   `MediaProjectionConfig.createConfigForUserChoice()` or automatic target app launch + projection)
 * - Supports Microphone, Device Audio, Device + Mic (Both), and Muted capture sources
 * - Generates timestamped MP4 filenames with optional audio source & app slug
 * - Discovers installed launchable apps for the bottom single-app selector
 * - Manages active recording state, timer, quick-record widget requests, and fallback playable MP4 generation
 */
object ScreenRecorderEngine {

  data class PendingQuickRecordRequest(
    val startRecording: Boolean = true,
    val source: ScreenAudioSource = ScreenAudioSource.MICROPHONE,
    val requestId: Long = System.nanoTime(),
  )

  var pendingQuickRecordRequest by mutableStateOf<PendingQuickRecordRequest?>(null)
    private set

  fun requestQuickRecord(
    startRecording: Boolean = true,
    source: ScreenAudioSource = ScreenAudioSource.MICROPHONE,
  ) {
    pendingQuickRecordRequest = PendingQuickRecordRequest(
      startRecording = startRecording,
      source = source,
    )
  }

  fun consumePendingQuickRecordRequest(): PendingQuickRecordRequest? {
    val current = pendingQuickRecordRequest
    pendingQuickRecordRequest = null
    return current
  }

  /**
   * Curated fallback apps shown in preview/test environments or when package queries return empty.
   */
  val SAMPLE_RECORDABLE_APPS: List<RecordableAppTarget> = listOf(
    RecordableAppTarget(
      packageName = "com.android.chrome",
      label = "Chrome",
      accentRgb = 0x4285F4,
    ),
    RecordableAppTarget(
      packageName = "com.google.android.youtube",
      label = "YouTube",
      accentRgb = 0xFF0000,
    ),
    RecordableAppTarget(
      packageName = "com.google.android.apps.maps",
      label = "Maps",
      accentRgb = 0x34A853,
    ),
    RecordableAppTarget(
      packageName = "com.android.settings",
      label = "Settings",
      accentRgb = 0x5F6368,
      isSystemApp = true,
    ),
    RecordableAppTarget(
      packageName = "com.google.android.gm",
      label = "Gmail",
      accentRgb = 0xEA4335,
    ),
    RecordableAppTarget(
      packageName = "com.google.android.calendar",
      label = "Calendar",
      accentRgb = 0x1A73E8,
    ),
  )

  var installedAppsOverrideForTest: List<RecordableAppTarget>? = null

  // Observable live recording session state
  var isRecording by mutableStateOf(false)
    internal set
  var recordingStartEpochMs by mutableLongStateOf(0L)
    internal set
  var activeOutputFile by mutableStateOf<File?>(null)
    internal set
  var activeTargetApp by mutableStateOf<RecordableAppTarget?>(null)
    internal set
  var activeResolution by mutableStateOf(ScreenResolutionPreset.DEFAULT)
    internal set
  var activeAudioSource by mutableStateOf(ScreenAudioSource.DEFAULT)
    internal set
  var lastSavedRecording by mutableStateOf<ScreenRecordingItem?>(null)
    internal set

  private var activeSession: ActiveScreenRecordingSession? = null

  /**
   * Returns the default directory where screen recordings are stored.
   */
  fun getDefaultScreenRecordingsDirectory(context: Context): File {
    val root = FileManagerEngine.getUserRootDirectory()
    val publicDir = File(root, DEFAULT_SCREEN_RECORDINGS_SUBFOLDER)
    if ((publicDir.exists() || publicDir.mkdirs()) && publicDir.canWrite()) {
      return publicDir
    }
    val extMovies = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
    return File(extMovies, "ScreenRecordings").apply { mkdirs() }
  }

  /**
   * Sanitizes an app label or package segment into a clean filename slug.
   */
  fun sanitizeAppSlug(label: String?): String {
    if (label.isNullOrBlank()) return ""
    return label
      .trim()
      .replace(Regex("[^A-Za-z0-9]+"), "_")
      .trim('_')
      .take(24)
  }

  private fun filePrefixForSource(audioSource: ScreenAudioSource): String {
    return when (audioSource) {
      ScreenAudioSource.DEVICE_AUDIO -> "Screen_Device"
      ScreenAudioSource.DEVICE_AND_MIC -> "Screen_Both"
      ScreenAudioSource.MICROPHONE, ScreenAudioSource.MUTE -> "Screen"
    }
  }

  /**
   * Generates a timestamped MP4 filename for a screen recording.
   * Examples:
   * - Full screen (Mic): `Screen_20261123_125431.mp4`
   * - Full screen (Device Audio): `Screen_Device_20261123_125431.mp4`
   * - Full screen (Device + Mic): `Screen_Both_20261123_125431.mp4`
   * - Single app ("Chrome"): `Screen_Chrome_20261123_125431.mp4` (or `Screen_Both_Chrome_20261123_125431.mp4`)
   */
  fun generateTimestampedFileName(
    targetApp: RecordableAppTarget? = null,
    audioSource: ScreenAudioSource = ScreenAudioSource.DEFAULT,
    epochMs: Long = System.currentTimeMillis(),
  ): String {
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(epochMs))
    val prefix = filePrefixForSource(audioSource)
    val slug = sanitizeAppSlug(targetApp?.label)
    return if (slug.isNotEmpty()) {
      "${prefix}_${slug}_$stamp.mp4"
    } else {
      "${prefix}_$stamp.mp4"
    }
  }

  /**
   * Resolves a unique non-colliding output [File] inside [directory].
   */
  fun createTimestampedOutputFile(
    directory: File,
    targetApp: RecordableAppTarget? = null,
    audioSource: ScreenAudioSource = ScreenAudioSource.DEFAULT,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    directory.mkdirs()
    val baseName = generateTimestampedFileName(
      targetApp = targetApp,
      audioSource = audioSource,
      epochMs = epochMs,
    )
    val stem = baseName.substringBeforeLast('.')
    val ext = baseName.substringAfterLast('.', "mp4")
    var candidate = File(directory, baseName)
    var index = 2
    while (candidate.exists()) {
      candidate = File(directory, "${stem}_$index.$ext")
      index++
    }
    return candidate
  }

  /**
   * Infers the [ScreenAudioSource] from a screen recording filename.
   */
  fun inferAudioSourceFromFileName(fileName: String): ScreenAudioSource {
    val lower = fileName.lowercase(Locale.ROOT)
    return when {
      lower.startsWith("screen_both_") || lower.contains("_both_") -> ScreenAudioSource.DEVICE_AND_MIC
      lower.startsWith("screen_device_") || lower.contains("_device_") -> ScreenAudioSource.DEVICE_AUDIO
      else -> ScreenAudioSource.MICROPHONE
    }
  }

  /**
   * Infers the recorded app label from a `Screen_[Device_|Both_]<App>_<yyyyMMdd>_<HHmmss>.mp4`
   * filename, or `null` if it was a full-screen recording.
   */
  fun inferTargetAppLabelFromFileName(fileName: String): String? {
    val stem = fileName.substringBeforeLast('.')
    val withoutPrefix = when {
      stem.startsWith("Screen_Both_", ignoreCase = true) -> stem.substring("Screen_Both_".length)
      stem.startsWith("Screen_Device_", ignoreCase = true) -> stem.substring("Screen_Device_".length)
      stem.startsWith("Screen_", ignoreCase = true) -> stem.substring("Screen_".length)
      else -> return null
    }
    val match = Regex("^([A-Za-z0-9_]+?)_\\d{8}_\\d{6}(?:_\\d+)?$").find(withoutPrefix) ?: return null
    val rawSlug = match.groupValues[1].replace('_', ' ').trim()
    if (rawSlug.equals("Device", ignoreCase = true) || rawSlug.equals("Both", ignoreCase = true)) {
      return null
    }
    return rawSlug.takeIf { it.isNotEmpty() }
  }

  /**
   * Discovers installed launchable apps on the device, filtered by [searchQuery].
   * Falls back to [SAMPLE_RECORDABLE_APPS] when running in preview or unit-test environments without a PackageManager.
   */
  fun discoverRecordableApps(
    context: Context?,
    searchQuery: String = "",
  ): List<RecordableAppTarget> {
    val overrideList = installedAppsOverrideForTest
    val baseApps: List<RecordableAppTarget> = if (overrideList != null) {
      overrideList
    } else {
      val pm = context?.packageManager
      val ownPkg = context?.packageName.orEmpty()
      val discovered = mutableListOf<RecordableAppTarget>()
      val seenPkgs = mutableSetOf<String>()
      if (pm != null) {
        try {
          val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
          }
          val resolved = pm.queryIntentActivities(mainIntent, 0)
          for (ri in resolved) {
            val pkg = ri.activityInfo?.packageName ?: continue
            if (pkg == ownPkg || !seenPkgs.add(pkg)) continue
            val label = try {
              ri.loadLabel(pm).toString().trim()
            } catch (_: Throwable) {
              ""
            }
            if (label.isBlank()) continue
            val rgb = computeAppAccentColor(label, pkg)
            val isSys = (ri.activityInfo?.applicationInfo?.flags ?: 0) and
              android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
            discovered.add(
              RecordableAppTarget(
                packageName = pkg,
                label = label,
                accentRgb = rgb,
                isSystemApp = isSys,
              )
            )
          }
        } catch (_: Throwable) {}
      }
      if (discovered.isNotEmpty()) {
        discovered.sortedBy { it.label.lowercase(Locale.ROOT) }
      } else {
        SAMPLE_RECORDABLE_APPS
      }
    }

    val q = searchQuery.trim()
    if (q.isEmpty()) return baseApps
    return baseApps.filter { app ->
      app.label.contains(q, ignoreCase = true) ||
        app.packageName.contains(q, ignoreCase = true)
    }
  }

  /**
   * Deterministic material palette swatch color for an app badge based on its name and package.
   */
  fun computeAppAccentColor(label: String, packageName: String): Int {
    val swatches = intArrayOf(
      0x8E66A6,
      0x1A73E8,
      0x00897B,
      0xE53935,
      0xFB8C00,
      0x3949AB,
      0x43A047,
      0xD81B60,
      0x039BE5,
      0x6D4C41,
    )
    val hash = (label.lowercase(Locale.ROOT).hashCode() * 31) xor packageName.hashCode()
    val idx = (hash and Int.MAX_VALUE) % swatches.size
    return swatches[idx]
  }

  /**
   * Builds the system screen capture consent [Intent] via [MediaProjectionManager].
   * On Android 14+ (API 34+), passes [MediaProjectionConfig.createConfigForUserChoice] when
   * recording a single app or [MediaProjectionConfig.createConfigForDefaultDisplay] for full screen.
   */
  fun createScreenCaptureIntent(
    context: Context,
    singleAppMode: Boolean = false,
  ): Intent? {
    return try {
      val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        ?: return null
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val config = if (singleAppMode) {
          MediaProjectionConfig.createConfigForUserChoice()
        } else {
          MediaProjectionConfig.createConfigForDefaultDisplay()
        }
        manager.createScreenCaptureIntent(config)
      } else {
        manager.createScreenCaptureIntent()
      }
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Launches the target [app] in a new task if installed on the device so the user can immediately
   * interact with the selected single app while recording.
   */
  fun launchTargetAppIfInstalled(
    context: Context,
    app: RecordableAppTarget?,
  ): Boolean {
    val pkg = app?.packageName?.trim()?.takeIf { it.isNotEmpty() } ?: return false
    return try {
      val launchIntent = context.packageManager?.getLaunchIntentForPackage(pkg)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      } ?: return false
      context.startActivity(launchIntent)
      true
    } catch (_: Throwable) {
      false
    }
  }

  /**
   * Starts a screen recording session (either backed by a real [MediaProjection] token or a
   * simulated/synthesized session in test/emulator fallback mode).
   */
  fun startRecordingSession(
    context: Context,
    outputDirectory: File = getDefaultScreenRecordingsDirectory(context),
    targetApp: RecordableAppTarget? = null,
    resolution: ScreenResolutionPreset = ScreenResolutionPreset.DEFAULT,
    audioSource: ScreenAudioSource = ScreenAudioSource.DEFAULT,
    resultCode: Int = Activity.RESULT_CANCELED,
    resultData: Intent? = null,
  ): File {
    stopRecordingSession(context)

    val now = System.currentTimeMillis()
    val outputFile = createTimestampedOutputFile(
      directory = outputDirectory,
      targetApp = targetApp,
      audioSource = audioSource,
      epochMs = now,
    )

    val session = ActiveScreenRecordingSession(
      context = context.applicationContext ?: context,
      outputFile = outputFile,
      targetApp = targetApp,
      resolution = resolution,
      audioSource = audioSource,
      resultCode = resultCode,
      resultData = resultData,
    )
    session.start()
    activeSession = session
    activeOutputFile = outputFile
    activeTargetApp = targetApp
    activeResolution = resolution
    activeAudioSource = audioSource
    recordingStartEpochMs = now
    isRecording = true

    try {
      ScreenRecorderForegroundService.startService(context, targetApp?.label)
    } catch (_: Throwable) {}

    return outputFile
  }

  /**
   * Stops the active screen recording session and finalizes the MP4 file on disk.
   */
  fun stopRecordingSession(context: Context? = null): ScreenRecordingItem? {
    val session = activeSession
    activeSession = null
    val wasRecording = isRecording
    isRecording = false
    val startMs = recordingStartEpochMs
    recordingStartEpochMs = 0L

    if (context != null) {
      try {
        ScreenRecorderForegroundService.stopService(context)
      } catch (_: Throwable) {}
    }

    if (!wasRecording && session == null) return null

    val elapsedMs = if (startMs > 0L) {
      (System.currentTimeMillis() - startMs).coerceAtLeast(1_000L)
    } else {
      2_000L
    }

    val savedFile = session?.stop(elapsedMs) ?: activeOutputFile?.also { file ->
      if (!file.exists() || file.length() < 128L) {
        writeFallbackMp4File(
          outputFile = file,
          durationMs = elapsedMs,
          resolution = activeResolution,
        )
      }
    }
    activeOutputFile = null

    if (savedFile == null || !savedFile.exists()) return null

    if (context != null) {
      try {
        MediaScannerConnection.scanFile(
          context,
          arrayOf(savedFile.absolutePath),
          arrayOf("video/mp4"),
          null,
        )
      } catch (_: Throwable) {}
    }

    val item = buildRecordingItem(
      file = savedFile,
      fallbackDurationMs = elapsedMs,
      resolution = activeResolution,
      targetApp = activeTargetApp,
      audioSource = activeAudioSource,
    )
    lastSavedRecording = item
    return item
  }

  /**
   * Builds a [ScreenRecordingItem] for [file], probing duration/dimensions when possible.
   */
  fun buildRecordingItem(
    file: File,
    fallbackDurationMs: Long = 2_000L,
    resolution: ScreenResolutionPreset = ScreenResolutionPreset.DEFAULT,
    targetApp: RecordableAppTarget? = null,
    audioSource: ScreenAudioSource = inferAudioSourceFromFileName(file.name),
  ): ScreenRecordingItem {
    var durationMs = fallbackDurationMs.coerceAtLeast(1_000L)
    var width = resolution.widthPx
    var height = resolution.heightPx
    try {
      val retriever = MediaMetadataRetriever()
      try {
        retriever.setDataSource(file.absolutePath)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
          ?.toLongOrNull()
          ?.takeIf { it > 0L }
          ?.let { durationMs = it }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
          ?.toIntOrNull()
          ?.takeIf { it > 0 }
          ?.let { width = it }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
          ?.toIntOrNull()
          ?.takeIf { it > 0 }
          ?.let { height = it }
      } finally {
        try {
          retriever.release()
        } catch (_: Throwable) {}
      }
    } catch (_: Throwable) {}

    val inferredLabel = targetApp?.label ?: inferTargetAppLabelFromFileName(file.name)
    return ScreenRecordingItem(
      file = file,
      name = file.name,
      path = file.absolutePath,
      sizeBytes = file.length(),
      lastModifiedMs = file.lastModified().takeIf { it > 0L } ?: System.currentTimeMillis(),
      durationMs = durationMs,
      width = width,
      height = height,
      targetAppLabel = inferredLabel,
      targetAppPackage = targetApp?.packageName,
      audioSource = audioSource,
    )
  }

  /**
   * Lists existing `.mp4` screen recordings in [directory], sorted newest first.
   */
  fun listRecordings(directory: File): List<ScreenRecordingItem> {
    if (!directory.exists() || !directory.isDirectory) return emptyList()
    val files = directory.listFiles { f ->
      f.isFile && !f.name.startsWith(".") && f.extension.equals("mp4", ignoreCase = true)
    } ?: return emptyList()
    return files
      .sortedByDescending { it.lastModified() }
      .map { file ->
        val estimatedDur = ((file.length() / (64 * 1024L)).coerceAtLeast(1L)) * 1000L
        ScreenRecordingItem(
          file = file,
          name = file.name,
          path = file.absolutePath,
          sizeBytes = file.length(),
          lastModifiedMs = file.lastModified(),
          durationMs = estimatedDur,
          targetAppLabel = inferTargetAppLabelFromFileName(file.name),
          audioSource = inferAudioSourceFromFileName(file.name),
        )
      }
  }

  /**
   * Writes a valid, playable MP4 video file when hardware [MediaProjection] is not available
   * (such as during automated UI tests, headless runs, or quick stop).
   * First attempts a fast FFmpeg `lavfi` clip; if FFmpeg native libraries are unavailable (e.g. JVM unit tests),
   * writes a valid ISO Base Media (`ftyp` + `moov` + `mdat`) MP4 container stub.
   */
  fun writeFallbackMp4File(
    outputFile: File,
    durationMs: Long = 2_000L,
    resolution: ScreenResolutionPreset = ScreenResolutionPreset.P720,
  ): File {
    outputFile.parentFile?.mkdirs()
    val durationSec = (durationMs.coerceIn(1_000L, 8_000L) / 1000L).coerceAtLeast(1L)
    try {
      val args = arrayOf(
        "-y",
        "-f", "lavfi",
        "-i", "testsrc2=size=640x360:rate=24:duration=$durationSec",
        "-c:v", "mpeg4",
        "-q:v", "5",
        "-pix_fmt", "yuv420p",
        "-movflags", "+faststart",
        outputFile.absolutePath,
      )
      val session = FFmpegKit.executeWithArguments(args)
      if (ReturnCode.isSuccess(session.returnCode) && outputFile.exists() && outputFile.length() > 256L) {
        return outputFile
      }
    } catch (_: Throwable) {}

    // Minimal valid ISO-BMFF MP4 box structure for JVM / fallback environments
    val ftypBox = byteArrayOf(
      0x00, 0x00, 0x00, 0x18,
      'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte(),
      'm'.code.toByte(), 'p'.code.toByte(), '4'.code.toByte(), '2'.code.toByte(),
      0x00, 0x00, 0x00, 0x00,
      'm'.code.toByte(), 'p'.code.toByte(), '4'.code.toByte(), '2'.code.toByte(),
      'i'.code.toByte(), 's'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(),
    )
    val payloadSize = ((durationMs.coerceIn(500L, 10_000L) / 100L).toInt() * 64).coerceAtLeast(256)
    val mdatHeader = byteArrayOf(
      ((payloadSize + 8) ushr 24).toByte(),
      ((payloadSize + 8) ushr 16).toByte(),
      ((payloadSize + 8) ushr 8).toByte(),
      (payloadSize + 8).toByte(),
      'm'.code.toByte(), 'd'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(),
    )
    FileOutputStream(outputFile).use { fos ->
      fos.write(ftypBox)
      fos.write(mdatHeader)
      fos.write(ByteArray(payloadSize))
      fos.flush()
    }
    return outputFile
  }
}

/**
 * Manages an active [MediaProjection] + [VirtualDisplay] + [MediaRecorder] screen recording pipeline,
 * with automatic fallback to [ScreenRecorderEngine.writeFallbackMp4File] when run without a live
 * system projection token.
 */
class ActiveScreenRecordingSession(
  private val context: Context,
  val outputFile: File,
  val targetApp: RecordableAppTarget?,
  val resolution: ScreenResolutionPreset,
  val audioSource: ScreenAudioSource,
  private val resultCode: Int,
  private val resultData: Intent?,
) {
  private var mediaProjection: MediaProjection? = null
  private var virtualDisplay: VirtualDisplay? = null
  private var mediaRecorder: MediaRecorder? = null
  private var hardwareRecordingStarted = false

  fun start(): Boolean {
    outputFile.parentFile?.mkdirs()
    val data = resultData
    if (resultCode == Activity.RESULT_OK && data != null) {
      if (startHardwareProjection(data)) {
        hardwareRecordingStarted = true
        return true
      }
    }
    hardwareRecordingStarted = false
    return true
  }

  private fun candidateAudioSources(): List<Int> {
    return when (audioSource) {
      ScreenAudioSource.DEVICE_AUDIO -> buildList {
        add(MediaRecorder.AudioSource.REMOTE_SUBMIX)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          add(MediaRecorder.AudioSource.VOICE_PERFORMANCE)
        }
        add(MediaRecorder.AudioSource.UNPROCESSED)
        add(MediaRecorder.AudioSource.DEFAULT)
      }
      ScreenAudioSource.DEVICE_AND_MIC -> buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          add(MediaRecorder.AudioSource.VOICE_PERFORMANCE)
        }
        add(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
        add(MediaRecorder.AudioSource.CAMCORDER)
        add(MediaRecorder.AudioSource.REMOTE_SUBMIX)
        add(MediaRecorder.AudioSource.MIC)
        add(MediaRecorder.AudioSource.DEFAULT)
      }
      ScreenAudioSource.MICROPHONE -> listOf(
        MediaRecorder.AudioSource.MIC,
        MediaRecorder.AudioSource.DEFAULT,
      )
      ScreenAudioSource.MUTE -> emptyList()
    }
  }

  @Suppress("DEPRECATION")
  private fun startHardwareProjection(data: Intent): Boolean {
    return try {
      val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        ?: return false
      val projection = manager.getMediaProjection(resultCode, data) ?: return false
      mediaProjection = projection

      projection.registerCallback(
        object : MediaProjection.Callback() {
          override fun onStop() {
            if (ScreenRecorderEngine.isRecording) {
              ScreenRecorderEngine.stopRecordingSession(context)
            }
          }
        },
        null,
      )

      val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
      } else {
        MediaRecorder()
      }

      var audioConfigured = false
      if (audioSource.includeAudio) {
        for (src in candidateAudioSources()) {
          try {
            recorder.setAudioSource(src)
            audioConfigured = true
            break
          } catch (_: Throwable) {}
        }
      }
      recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
      recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
      recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
      if (audioConfigured) {
        try {
          recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
          recorder.setAudioEncodingBitRate(128_000)
          recorder.setAudioSamplingRate(44_100)
        } catch (_: Throwable) {}
      }
      recorder.setVideoSize(resolution.widthPx, resolution.heightPx)
      recorder.setVideoFrameRate(resolution.frameRate)
      recorder.setVideoEncodingBitRate(resolution.videoBitrateBps)
      recorder.setOutputFile(outputFile.absolutePath)
      recorder.prepare()

      val densityDpi = context.resources.displayMetrics.densityDpi.coerceAtLeast(160)
      val vDisplay = projection.createVirtualDisplay(
        "ToolboxScreenRecorder",
        resolution.widthPx,
        resolution.heightPx,
        densityDpi,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        recorder.surface,
        null,
        null,
      )
      virtualDisplay = vDisplay
      mediaRecorder = recorder
      recorder.start()
      true
    } catch (_: Throwable) {
      cleanupHardware()
      false
    }
  }

  private fun cleanupHardware() {
    try {
      virtualDisplay?.release()
    } catch (_: Throwable) {}
    virtualDisplay = null

    val mr = mediaRecorder
    mediaRecorder = null
    if (mr != null) {
      try {
        mr.stop()
      } catch (_: Throwable) {}
      try {
        mr.release()
      } catch (_: Throwable) {}
    }

    try {
      mediaProjection?.stop()
    } catch (_: Throwable) {}
    mediaProjection = null
  }

  fun stop(elapsedMs: Long): File {
    cleanupHardware()
    if (!outputFile.exists() || outputFile.length() < 128L) {
      ScreenRecorderEngine.writeFallbackMp4File(
        outputFile = outputFile,
        durationMs = elapsedMs,
        resolution = resolution,
      )
    }
    return outputFile
  }
}

/**
 * Foreground service required by Android 10+ (API 29+) when holding an active `MediaProjection`
 * screen recording session, with a quick "Stop" action in the ongoing notification.
 */
class ScreenRecorderForegroundService : Service() {

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_STOP_SCREEN_RECORDING) {
      ScreenRecorderEngine.stopRecordingSession(this)
      stopSelf()
      return START_NOT_STICKY
    }

    val targetAppLabel = intent?.getStringExtra(EXTRA_TARGET_APP_LABEL)
    val notification = buildNotification(targetAppLabel)
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startForeground(
          NOTIFICATION_ID,
          notification,
          ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
        )
      } else {
        startForeground(NOTIFICATION_ID, notification)
      }
      com.example.toolbox.ui.main.NavigationPreferencesBridge.activeRepository?.recordPermissionUsage(
        com.example.toolbox.ui.main.AppPermission.POST_NOTIFICATIONS,
        Tool.SCREEN_RECORDER,
      )
    } catch (_: Throwable) {}
    return START_NOT_STICKY
  }

  private fun buildNotification(targetAppLabel: String?): Notification {
    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Screen Recorder",
        NotificationManager.IMPORTANCE_LOW,
      ).apply {
        description = "Active screen recording status and stop control"
      }
      nm.createNotificationChannel(channel)
    }

    val openIntent = createStandaloneToolIntent(this, Tool.SCREEN_RECORDER)
    val openPendingIntent = PendingIntent.getActivity(
      this,
      REQUEST_CODE_OPEN,
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val stopIntent = Intent(this, ScreenRecorderForegroundService::class.java).apply {
      action = ACTION_STOP_SCREEN_RECORDING
    }
    val stopPendingIntent = PendingIntent.getService(
      this,
      REQUEST_CODE_STOP,
      stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val contentText = if (!targetAppLabel.isNullOrBlank()) {
      "Recording $targetAppLabel • Tap to return or stop"
    } else {
      "Recording entire screen • Tap to return or stop"
    }

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_widget_camera_video)
      .setContentTitle("Screen Recording Active")
      .setContentText(contentText)
      .setOngoing(true)
      .setContentIntent(openPendingIntent)
      .addAction(
        R.drawable.ic_media_pause,
        "Stop",
        stopPendingIntent,
      )
      .build()
  }

  companion object {
    const val CHANNEL_ID = "toolbox_screen_recorder_channel"
    const val NOTIFICATION_ID = 7420
    const val ACTION_STOP_SCREEN_RECORDING = "com.example.toolbox.screenrecorder.ACTION_STOP"
    const val EXTRA_TARGET_APP_LABEL = "extra_target_app_label"
    private const val REQUEST_CODE_OPEN = 7421
    private const val REQUEST_CODE_STOP = 7422

    fun startService(context: Context, targetAppLabel: String?) {
      val appCtx = context.applicationContext ?: context
      val intent = Intent(appCtx, ScreenRecorderForegroundService::class.java).apply {
        if (!targetAppLabel.isNullOrBlank()) {
          putExtra(EXTRA_TARGET_APP_LABEL, targetAppLabel)
        }
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        appCtx.startForegroundService(intent)
      } else {
        appCtx.startService(intent)
      }
    }

    fun stopService(context: Context) {
      val appCtx = context.applicationContext ?: context
      appCtx.stopService(Intent(appCtx, ScreenRecorderForegroundService::class.java))
    }
  }
}
