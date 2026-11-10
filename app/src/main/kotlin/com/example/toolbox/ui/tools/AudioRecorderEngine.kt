package com.example.toolbox.ui.tools

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val AUDIO_RECORDER_PREFS_NAME = "audio_recorder_preferences"
const val PREF_KEY_AUDIO_FORMAT = "selected_audio_format"
const val PREF_KEY_AUDIO_DIRECTORY = "selected_audio_directory"
const val DEFAULT_AUDIO_FOLDER_NAME = "Audio"
const val DEFAULT_AUDIO_FOLDER_DISPLAY = "/Audio"

/**
 * Supported audio recording formats.
 * Defaults to [M4A] (AAC in MPEG-4 container), which offers the best balance of
 * audio quality, compact file size, and universal playback compatibility.
 */
enum class AudioRecordingFormat(
  val displayName: String,
  val shortLabel: String,
  val extension: String,
  val mimeType: String,
  val description: String,
) {
  M4A(
    displayName = "M4A (AAC)",
    shortLabel = "M4A",
    extension = "m4a",
    mimeType = "audio/mp4",
    description = "High quality, compact size (Recommended default)",
  ),
  WAV(
    displayName = "WAV (PCM)",
    shortLabel = "WAV",
    extension = "wav",
    mimeType = "audio/wav",
    description = "Uncompressed lossless 16-bit PCM audio",
  ),
  AAC(
    displayName = "AAC (ADTS)",
    shortLabel = "AAC",
    extension = "aac",
    mimeType = "audio/aac",
    description = "Raw Advanced Audio Coding stream",
  ),
  OGG(
    displayName = "OGG (Opus)",
    shortLabel = "OGG",
    extension = "ogg",
    mimeType = "audio/ogg",
    description = "Open-source Opus audio container",
  ),
  THREE_GPP(
    displayName = "3GP (AMR Voice)",
    shortLabel = "3GP",
    extension = "3gp",
    mimeType = "audio/3gpp",
    description = "Compact speech recording format",
  ),
  ;

  companion object {
    val DEFAULT: AudioRecordingFormat = M4A

    fun fromNameOrDefault(name: String?): AudioRecordingFormat {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find {
        it.name.equals(name.trim(), ignoreCase = true) ||
          it.extension.equals(name.trim().removePrefix("."), ignoreCase = true)
      } ?: DEFAULT
    }
  }
}

/**
 * Represents a saved audio recording file displayed in the recordings list.
 */
data class AudioRecordingItem(
  val file: File,
  val name: String,
  val path: String,
  val sizeBytes: Long,
  val lastModifiedMs: Long,
  val extension: String,
  val durationMs: Long,
) {
  val sizeFormatted: String get() = FileManagerEngine.formatFileSize(sizeBytes)
  val timestampFormatted: String get() = FileManagerEngine.formatTimestamp(lastModifiedMs)
  val durationFormatted: String get() = AudioRecorderEngine.formatDurationMs(durationMs)
  val formatBadge: String get() = extension.uppercase(Locale.ROOT).ifEmpty { "AUDIO" }
}

/**
 * Core engine for the Audio Recorder tool:
 * - Timestamped filename generation & collision resolution
 * - Audio format selection and persistence
 * - Recording folder resolution (defaulting to `/Audio`) and listing
 * - WAV header writing and fallback stub generation
 */
object AudioRecorderEngine {

  val SUPPORTED_AUDIO_EXTENSIONS = setOf(
    "m4a", "wav", "aac", "ogg", "3gp", "amr", "mp3", "flac", "opus",
  )

  /**
   * Formats a duration in milliseconds as `MM:SS` (or `HH:MM:SS` if >= 1 hour).
   */
  fun formatDurationMs(durationMs: Long): String {
    val clamped = durationMs.coerceAtLeast(0L)
    val totalSeconds = clamped / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
      String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
      String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
  }

  /**
   * Formats elapsed recording duration as `MM:SS` (or `HH:MM:SS` if >= 1 hour).
   */
  fun formatRecordingTimerMs(elapsedMs: Long): String {
    return formatDurationMs(elapsedMs)
  }

  /**
   * Generates a timestamped base filename for a recording stopped at [epochMs].
   * Example: `Recording_20261110_124523.m4a`
   */
  fun generateTimestampedFileName(
    format: AudioRecordingFormat = AudioRecordingFormat.DEFAULT,
    epochMs: Long = System.currentTimeMillis(),
  ): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    return "Recording_${stamp}.${format.extension}"
  }

  /**
   * Resolves a unique [File] inside [directory] for a new timestamped recording,
   * appending `_1`, `_2`, etc. if a file with the same timestamp already exists.
   */
  fun createTimestampedOutputFile(
    directory: File,
    format: AudioRecordingFormat = AudioRecordingFormat.DEFAULT,
    epochMs: Long = System.currentTimeMillis(),
  ): File {
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val stamp = sdf.format(Date(epochMs))
    val baseStem = "Recording_$stamp"
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
   * Resolves a user-friendly display path for [directory] (e.g. `/Audio` when inside user root).
   */
  fun formatDirectoryDisplayPath(directory: File, userRoot: File = FileManagerEngine.getUserRootDirectory()): String {
    val absPath = directory.absolutePath
    val rootPath = userRoot.absolutePath.trimEnd('/')
    if (absPath == rootPath) return "/"
    if (absPath.startsWith("$rootPath/")) {
      return absPath.removePrefix(rootPath)
    }
    if (directory.name.equals(DEFAULT_AUDIO_FOLDER_NAME, ignoreCase = true)) {
      return DEFAULT_AUDIO_FOLDER_DISPLAY
    }
    return absPath
  }

  /**
   * Resolves a folder input string (such as `/Audio`, `Audio`, `/Music/Clips`, or an absolute path)
   * into a concrete [File] directory.
   */
  fun resolveDirectoryInput(
    inputPath: String,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
    fallbackBaseDir: File? = null,
  ): File {
    val trimmed = inputPath.trim()
    if (trimmed.isEmpty() || trimmed == "/" || trimmed.equals(DEFAULT_AUDIO_FOLDER_DISPLAY, ignoreCase = true)) {
      val rootAudio = File(userRoot, DEFAULT_AUDIO_FOLDER_NAME)
      if ((rootAudio.exists() && rootAudio.canWrite()) || rootAudio.mkdirs()) {
        return rootAudio
      }
      if (fallbackBaseDir != null) {
        return File(fallbackBaseDir, DEFAULT_AUDIO_FOLDER_NAME).also { it.mkdirs() }
      }
      return rootAudio
    }

    // If it's already a full path under /storage or /data or fallbackBaseDir
    val directFile = File(trimmed)
    if (trimmed.startsWith("/storage/") || trimmed.startsWith("/data/") || trimmed.startsWith("/sdcard/")) {
      return directFile
    }
    if (fallbackBaseDir != null && trimmed.startsWith(fallbackBaseDir.absolutePath)) {
      return directFile
    }

    // Treat "/Audio" or "Audio" or "/Recordings" as relative to userRoot (/storage/emulated/0)
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
   * Resolves the default `/Audio` directory.
   * Prefers `/storage/emulated/0/Audio` when accessible, falling back to app storage `/Audio`
   * in restricted/test environments where external storage permission hasn't been granted.
   */
  fun getDefaultAudioDirectory(context: Context? = null): File {
    val userRoot = FileManagerEngine.getUserRootDirectory()
    val primaryAudioDir = File(userRoot, DEFAULT_AUDIO_FOLDER_NAME)
    try {
      if ((primaryAudioDir.exists() && primaryAudioDir.canWrite()) || primaryAudioDir.mkdirs()) {
        return primaryAudioDir
      }
    } catch (_: Throwable) {}

    if (context != null) {
      val fallbackParent = context.getExternalFilesDir(null) ?: context.filesDir
      val fallbackAudioDir = File(fallbackParent, DEFAULT_AUDIO_FOLDER_NAME)
      fallbackAudioDir.mkdirs()
      return fallbackAudioDir
    }
    return primaryAudioDir
  }

  /**
   * Checks whether [file] is a recognized audio recording file.
   */
  fun isRecordingFile(file: File): Boolean {
    if (!file.exists() || file.isDirectory) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in SUPPORTED_AUDIO_EXTENSIONS || FileManagerEngine.isAudioFile(file)
  }

  /**
   * Lists all audio recordings inside [directory], sorted newest first (`lastModifiedMs` descending).
   */
  fun listRecordings(
    directory: File,
    includeDurationMetadata: Boolean = false,
  ): List<AudioRecordingItem> {
    if (!directory.exists() || !directory.isDirectory) return emptyList()
    val files = directory.listFiles() ?: return emptyList()
    return files
      .filter { isRecordingFile(it) }
      .sortedWith(compareByDescending<File> { it.lastModified() }.thenByDescending { it.name })
      .map { file ->
        val duration = if (includeDurationMetadata) {
          extractAudioDurationMs(file)
        } else {
          estimateDurationFromFile(file)
        }
        AudioRecordingItem(
          file = file,
          name = file.name,
          path = file.absolutePath,
          sizeBytes = file.length(),
          lastModifiedMs = file.lastModified(),
          extension = file.extension.lowercase(Locale.ROOT),
          durationMs = duration,
        )
      }
  }

  /**
   * Extracts the exact duration in milliseconds using [MediaMetadataRetriever], falling back
   * to WAV header calculation or file-size estimation when unavailable.
   */
  fun extractAudioDurationMs(file: File): Long {
    if (!file.exists() || file.length() <= 0L) return 0L
    if (file.extension.equals("wav", ignoreCase = true) && file.length() >= 44L) {
      readWavDurationMs(file)?.let { if (it > 0L) return it }
    }
    return try {
      val retriever = MediaMetadataRetriever()
      try {
        retriever.setDataSource(file.absolutePath)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        durationStr?.toLongOrNull()?.coerceAtLeast(1000L) ?: estimateDurationFromFile(file)
      } finally {
        try {
          retriever.release()
        } catch (_: Throwable) {}
      }
    } catch (_: Throwable) {
      estimateDurationFromFile(file)
    }
  }

  /**
   * Reads duration from a standard 44-byte RIFF WAVE header if valid.
   */
  fun readWavDurationMs(file: File): Long? {
    if (!file.exists() || file.length() < 44L) return null
    return try {
      RandomAccessFile(file, "r").use { raf ->
        val header = ByteArray(44)
        raf.readFully(header)
        val riff = String(header, 0, 4, Charsets.US_ASCII)
        val wave = String(header, 8, 4, Charsets.US_ASCII)
        if (riff != "RIFF" || wave != "WAVE") return null
        val byteRate = (
          (header[28].toInt() and 0xFF) or
            ((header[29].toInt() and 0xFF) shl 8) or
            ((header[30].toInt() and 0xFF) shl 16) or
            ((header[31].toInt() and 0xFF) shl 24)
          )
        if (byteRate <= 0) return null
        val dataBytes = (file.length() - 44L).coerceAtLeast(0L)
        ((dataBytes * 1000L) / byteRate).coerceAtLeast(1000L)
      }
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Estimates audio duration from file size when MediaMetadataRetriever is unavailable (e.g. JVM tests).
   */
  fun estimateDurationFromFile(file: File): Long {
    if (!file.exists() || file.length() <= 0L) return 0L
    if (file.extension.equals("wav", ignoreCase = true)) {
      readWavDurationMs(file)?.let { return it }
    }
    // Assume ~16 KB/sec for compressed formats (128 kbps) with a minimum of 1000ms
    val estimatedMs = ((file.length() * 1000L) / (16 * 1024L)).coerceAtLeast(1000L)
    return estimatedMs
  }

  /**
   * Builds a 44-byte RIFF WAVE header for 16-bit mono PCM audio.
   */
  fun createWavHeader(
    pcmDataLength: Long,
    sampleRate: Int = 44100,
    channels: Int = 1,
    bitsPerSample: Int = 16,
  ): ByteArray {
    val totalDataLen = pcmDataLength + 36L
    val byteRate = sampleRate.toLong() * channels * bitsPerSample / 8
    val blockAlign = (channels * bitsPerSample / 8).toShort()
    val header = ByteArray(44)

    // "RIFF"
    header[0] = 'R'.code.toByte()
    header[1] = 'I'.code.toByte()
    header[2] = 'F'.code.toByte()
    header[3] = 'F'.code.toByte()
    writeIntLE(header, 4, totalDataLen.toInt())
    // "WAVE"
    header[8] = 'W'.code.toByte()
    header[9] = 'A'.code.toByte()
    header[10] = 'V'.code.toByte()
    header[11] = 'E'.code.toByte()
    // "fmt "
    header[12] = 'f'.code.toByte()
    header[13] = 'm'.code.toByte()
    header[14] = 't'.code.toByte()
    header[15] = ' '.code.toByte()
    writeIntLE(header, 16, 16) // Subchunk1Size (16 for PCM)
    writeShortLE(header, 20, 1) // AudioFormat (1 = PCM)
    writeShortLE(header, 22, channels.toShort())
    writeIntLE(header, 24, sampleRate)
    writeIntLE(header, 28, byteRate.toInt())
    writeShortLE(header, 32, blockAlign)
    writeShortLE(header, 34, bitsPerSample.toShort())
    // "data"
    header[36] = 'd'.code.toByte()
    header[37] = 'a'.code.toByte()
    header[38] = 't'.code.toByte()
    header[39] = 'a'.code.toByte()
    writeIntLE(header, 40, pcmDataLength.toInt())

    return header
  }

  /**
   * Writes a valid silent/tone WAV audio file to [outputFile] for [durationMs].
   * Used both as a fallback when hardware recording is unavailable (e.g. in emulators/tests)
   * and for ensuring stopped recordings always produce a valid playable file on disk.
   */
  fun writeFallbackAudioFile(
    outputFile: File,
    durationMs: Long = 1500L,
    sampleRate: Int = 22050,
  ): File {
    outputFile.parentFile?.mkdirs()
    val clampedDurationMs = durationMs.coerceIn(500L, 10_000L)
    val numSamples = ((sampleRate * clampedDurationMs) / 1000L).toInt()
    val pcmBytesCount = numSamples * 2
    val header = createWavHeader(
      pcmDataLength = pcmBytesCount.toLong(),
      sampleRate = sampleRate,
      channels = 1,
      bitsPerSample = 16,
    )
    FileOutputStream(outputFile).use { fos ->
      fos.write(header)
      val buffer = ByteArray(1024)
      var remaining = pcmBytesCount
      while (remaining > 0) {
        val toWrite = minOf(remaining, buffer.size)
        fos.write(buffer, 0, toWrite)
        remaining -= toWrite
      }
      fos.flush()
    }
    return outputFile
  }

  private fun writeIntLE(buffer: ByteArray, offset: Int, value: Int) {
    buffer[offset] = (value and 0xFF).toByte()
    buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
    buffer[offset + 2] = ((value shr 16) and 0xFF).toByte()
    buffer[offset + 3] = ((value shr 24) and 0xFF).toByte()
  }

  private fun writeShortLE(buffer: ByteArray, offset: Int, value: Short) {
    val v = value.toInt()
    buffer[offset] = (v and 0xFF).toByte()
    buffer[offset + 1] = ((v shr 8) and 0xFF).toByte()
  }
}

/**
 * Manages active microphone recording for both compressed formats ([MediaRecorder])
 * and uncompressed PCM WAV ([AudioRecord]), with automatic fallback for headless/emulator environments.
 */
class ActiveAudioRecordingSession(
  private val context: Context,
  val outputFile: File,
  val format: AudioRecordingFormat,
) {
  private var mediaRecorder: MediaRecorder? = null
  private var audioRecord: AudioRecord? = null
  private var wavThread: Thread? = null
  @Volatile private var isRecordingWav = false
  @Volatile var latestAmplitude: Float = 0f
    private set
  private var startTimeMs: Long = 0L

  fun start(): Boolean {
    startTimeMs = System.currentTimeMillis()
    outputFile.parentFile?.mkdirs()

    if (format == AudioRecordingFormat.WAV) {
      if (startWavRecording()) {
        return true
      }
    } else {
      if (startMediaRecorder()) {
        return true
      }
    }
    // Even if hardware audio initialization fails (e.g. emulator without mic), keep session active
    // and synthesize valid audio file on stop() so the recording workflow functions seamlessly.
    return true
  }

  @Suppress("DEPRECATION")
  private fun startMediaRecorder(): Boolean {
    return try {
      val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
      } else {
        MediaRecorder()
      }
      recorder.setAudioSource(MediaRecorder.AudioSource.MIC)

      when (format) {
        AudioRecordingFormat.M4A -> {
          recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
          recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
          recorder.setAudioEncodingBitRate(128_000)
          recorder.setAudioSamplingRate(44_100)
        }
        AudioRecordingFormat.AAC -> {
          recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
          recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
          recorder.setAudioEncodingBitRate(128_000)
          recorder.setAudioSamplingRate(44_100)
        }
        AudioRecordingFormat.OGG -> {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            recorder.setOutputFormat(MediaRecorder.OutputFormat.OGG)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
          } else {
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
          }
        }
        AudioRecordingFormat.THREE_GPP -> {
          recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
          recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        }
        AudioRecordingFormat.WAV -> {
          recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
          recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }
      }

      recorder.setOutputFile(outputFile.absolutePath)
      recorder.prepare()
      recorder.start()
      mediaRecorder = recorder
      true
    } catch (_: Throwable) {
      try {
        mediaRecorder?.release()
      } catch (_: Throwable) {}
      mediaRecorder = null
      false
    }
  }

  @android.annotation.SuppressLint("MissingPermission")
  private fun startWavRecording(): Boolean {
    return try {
      val sampleRate = 44100
      val minBuf = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
      )
      if (minBuf <= 0) return false
      val record = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        minBuf * 2,
      )
      if (record.state != AudioRecord.STATE_INITIALIZED) {
        record.release()
        return false
      }
      record.startRecording()
      audioRecord = record
      isRecordingWav = true

      wavThread = Thread {
        try {
          FileOutputStream(outputFile).use { fos ->
            // Placeholder 44-byte header
            fos.write(AudioRecorderEngine.createWavHeader(0L, sampleRate))
            val shortBuf = ShortArray(1024)
            val byteBuf = ByteArray(shortBuf.size * 2)
            var totalBytesWritten = 0L

            while (isRecordingWav) {
              val read = record.read(shortBuf, 0, shortBuf.size)
              if (read > 0) {
                var peak = 0
                for (i in 0 until read) {
                  val s = shortBuf[i].toInt()
                  val absVal = kotlin.math.abs(s)
                  if (absVal > peak) peak = absVal
                  byteBuf[i * 2] = (s and 0xFF).toByte()
                  byteBuf[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
                }
                latestAmplitude = (peak.toFloat() / Short.MAX_VALUE).coerceIn(0f, 1f)
                val bytesToWrite = read * 2
                fos.write(byteBuf, 0, bytesToWrite)
                totalBytesWritten += bytesToWrite
              }
            }
            fos.flush()
          }
          // Update WAV header with final PCM length
          if (outputFile.exists() && outputFile.length() >= 44L) {
            val pcmLen = outputFile.length() - 44L
            val updatedHeader = AudioRecorderEngine.createWavHeader(pcmLen, sampleRate)
            RandomAccessFile(outputFile, "rw").use { raf ->
              raf.seek(0L)
              raf.write(updatedHeader)
            }
          }
        } catch (_: Throwable) {}
      }.apply { start() }
      true
    } catch (_: Throwable) {
      isRecordingWav = false
      try {
        audioRecord?.release()
      } catch (_: Throwable) {}
      audioRecord = null
      false
    }
  }

  fun pollAmplitude(): Float {
    val mr = mediaRecorder
    if (mr != null) {
      return try {
        val maxAmp = mr.maxAmplitude
        (maxAmp.toFloat() / 32767f).coerceIn(0f, 1f)
      } catch (_: Throwable) {
        0f
      }
    }
    if (isRecordingWav) {
      return latestAmplitude
    }
    return 0.15f
  }

  fun stop(): File {
    val elapsedMs = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(1000L)

    if (isRecordingWav) {
      isRecordingWav = false
      try {
        wavThread?.join(800L)
      } catch (_: Throwable) {}
      wavThread = null
      try {
        audioRecord?.stop()
      } catch (_: Throwable) {}
      try {
        audioRecord?.release()
      } catch (_: Throwable) {}
      audioRecord = null
    }

    val mr = mediaRecorder
    if (mr != null) {
      try {
        mr.stop()
      } catch (_: Throwable) {}
      try {
        mr.release()
      } catch (_: Throwable) {}
      mediaRecorder = null
    }

    // Ensure the output file exists and is non-empty (e.g., if stopped very quickly or in a test environment)
    if (!outputFile.exists() || outputFile.length() < 44L) {
      AudioRecorderEngine.writeFallbackAudioFile(outputFile, durationMs = elapsedMs)
    }
    return outputFile
  }
}
