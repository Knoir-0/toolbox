package com.example.toolbox.ui.tools

import android.content.Context
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bottom tool panel modes in the Audio Editor.
 * Trimming and cutting are performed directly on the top waveform stage.
 */
enum class AudioEditorMode(val displayName: String) {
  VOLUME_FADE("Volume & Fade"),
  SPEED_EFFECTS("Speed & Reverse"),
}

/**
 * Interactive drag targets on the waveform clipping stage.
 */
enum class WaveformDragTarget {
  NONE,
  PLAYHEAD,
  START_HANDLE,
  END_HANDLE,
}

/**
 * Supported export formats for the Audio Editor, backed by [ConvertibleFormat] from [FileConverterEngine].
 */
enum class AudioExportFormat(
  val convertibleFormat: ConvertibleFormat,
  val label: String,
  val subtitle: String,
) {
  WAV(ConvertibleFormat.WAV, "WAV", "Lossless 16-bit PCM"),
  M4A(ConvertibleFormat.M4A, "M4A", "AAC in MPEG-4 container"),
  MP3(ConvertibleFormat.MP3, "MP3", "Universal MP3 audio"),
  AAC(ConvertibleFormat.AAC, "AAC", "AAC ADTS audio stream"),
  OGG(ConvertibleFormat.OGG, "OGG", "Ogg Vorbis audio"),
  FLAC(ConvertibleFormat.FLAC, "FLAC", "Lossless FLAC audio");

  val extension: String
    get() = convertibleFormat.extension

  val mimeType: String
    get() = convertibleFormat.mimeType

  companion object {
    fun fromFileName(fileName: String): AudioExportFormat {
      val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
      return entries.firstOrNull { it.extension.equals(ext, ignoreCase = true) } ?: WAV
    }
  }
}

/**
 * In-memory 16-bit PCM audio buffer used by [AudioEditorEngine] for waveform visualization,
 * non-destructive undo/redo editing, real-time preview playback, and export.
 */
data class AudioSampleBuffer(
  val samples: ShortArray,
  val sampleRate: Int = AudioEditorEngine.DEFAULT_SAMPLE_RATE,
  val channels: Int = 1,
) {
  val safeChannels: Int
    get() = channels.coerceIn(1, 2)

  val frameCount: Int
    get() = if (safeChannels > 0) samples.size / safeChannels else 0

  val durationMs: Long
    get() = if (sampleRate > 0) (frameCount * 1000L) / sampleRate else 0L

  val pcmByteSize: Long
    get() = samples.size * 2L

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AudioSampleBuffer) return false
    return sampleRate == other.sampleRate &&
      channels == other.channels &&
      samples.contentEquals(other.samples)
  }

  override fun hashCode(): Int {
    var result = samples.contentHashCode()
    result = 31 * result + sampleRate
    result = 31 * result + channels
    return result
  }
}

/**
 * Snapshot stored in [ImageEditorHistory] to support multi-step undo/redo in the Audio Editor.
 */
data class AudioEditSnapshot(
  val buffer: AudioSampleBuffer,
  val trimStartFraction: Float = 0f,
  val trimEndFraction: Float = 1f,
  val actionLabel: String = "Original",
)

/**
 * Core audio processing engine for the Audio Editor tool.
 *
 * Reuses shared audio and file infrastructure across the app:
 * - [AudioRecorderEngine] for WAV headers, supported extensions, duration formatting, and `/Audio` directory resolution.
 * - [FileConverterEngine] for decoding compressed formats (`.m4a`, `.mp3`, `.aac`, `.ogg`, `.flac`, `.opus`) to PCM WAV
 *   and encoding edited audio back into target formats via Native `MediaCodec`/`MediaMuxer` and `FFmpegKit`.
 * - [FileManagerEngine] for file size formatting and filename validation.
 */
object AudioEditorEngine {

  const val DEFAULT_SAMPLE_RATE = 44100
  const val MIN_TRIM_SPAN_FRACTION = 0.02f

  /** Maximum number of 16-bit samples loaded into memory (~15 minutes mono at 44.1kHz). */
  private const val MAX_IN_MEMORY_SAMPLES = 40_000_000

  /**
   * Resolves which waveform drag target ([WaveformDragTarget.START_HANDLE], [WaveformDragTarget.END_HANDLE],
   * or [WaveformDragTarget.PLAYHEAD]) a pointer at [touchFraction] should control.
   *
   * Supports both single-finger and simultaneous multi-finger (dual-handle) clipping:
   * - If one handle is already claimed by another active finger ([alreadyClaimedTarget]),
   *   a second finger near the other handle (or anywhere on the opposite side) claims the remaining handle.
   * - When both handles are close together, directional disambiguation selects the start handle when touching
   *   at or left of the midpoint and the end handle when touching right of the midpoint.
   */
  fun resolveWaveformHandleTarget(
    touchFraction: Float,
    startFraction: Float,
    endFraction: Float,
    handleHitRadiusFraction: Float = 0.07f,
    alreadyClaimedTarget: WaveformDragTarget = WaveformDragTarget.NONE,
  ): WaveformDragTarget {
    val clampedTouch = touchFraction.coerceIn(0f, 1f)
    val clampedStart = startFraction.coerceIn(0f, 1f)
    val clampedEnd = endFraction.coerceIn(clampedStart, 1f)

    when (alreadyClaimedTarget) {
      WaveformDragTarget.START_HANDLE -> return WaveformDragTarget.END_HANDLE
      WaveformDragTarget.END_HANDLE -> return WaveformDragTarget.START_HANDLE
      else -> {}
    }

    val distStart = abs(clampedTouch - clampedStart)
    val distEnd = abs(clampedTouch - clampedEnd)
    val hitRadius = handleHitRadiusFraction.coerceIn(0.02f, 0.25f)

    if (distStart > hitRadius && distEnd > hitRadius) {
      return WaveformDragTarget.PLAYHEAD
    }

    if (abs(distStart - distEnd) < 0.015f) {
      val mid = (clampedStart + clampedEnd) * 0.5f
      return if (clampedTouch <= mid) {
        WaveformDragTarget.START_HANDLE
      } else {
        WaveformDragTarget.END_HANDLE
      }
    }

    return if (distStart <= distEnd) {
      WaveformDragTarget.START_HANDLE
    } else {
      WaveformDragTarget.END_HANDLE
    }
  }

  /**
   * Computes the updated `(startFraction, endFraction)` clipping bounds when the user drags
   * the start handle ([draggedStart]), the end handle ([draggedEnd]), or both handles simultaneously.
   *
   * Preserves whichever handle is not being dragged and enforces [minSpan] between start and end.
   */
  fun updateSelectionFromHandles(
    currentStart: Float,
    currentEnd: Float,
    draggedStart: Float? = null,
    draggedEnd: Float? = null,
    minSpan: Float = MIN_TRIM_SPAN_FRACTION,
  ): Pair<Float, Float> {
    val safeMinSpan = minSpan.coerceIn(0.005f, 0.5f)
    val baseStart = currentStart.coerceIn(0f, 1f - safeMinSpan)
    val baseEnd = currentEnd.coerceIn(baseStart + safeMinSpan, 1f)

    return when {
      draggedStart != null && draggedEnd != null -> {
        val low = min(draggedStart, draggedEnd).coerceIn(0f, 1f - safeMinSpan)
        val high = max(draggedStart, draggedEnd).coerceIn(low + safeMinSpan, 1f)
        val adjustedLow = min(low, high - safeMinSpan).coerceAtLeast(0f)
        adjustedLow to high
      }
      draggedStart != null -> {
        val newStart = draggedStart.coerceIn(0f, (baseEnd - safeMinSpan).coerceAtLeast(0f))
        newStart to baseEnd
      }
      draggedEnd != null -> {
        val newEnd = draggedEnd.coerceIn((baseStart + safeMinSpan).coerceAtMost(1f), 1f)
        baseStart to newEnd
      }
      else -> baseStart to baseEnd
    }
  }

  /**
   * Returns `true` if [file] has a supported audio extension or is recognized as an audio format by [FileConverterEngine].
   */
  fun isSupportedAudioFile(file: File): Boolean {
    if (!file.exists() || !file.isFile) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    if (ext in AudioRecorderEngine.SUPPORTED_AUDIO_EXTENSIONS) return true
    return FileConverterEngine.detectFormat(file)?.category == ConversionMediaCategory.AUDIO
  }

  /**
   * Extracts normalized peak amplitude buckets in `0f..1f` for rendering the waveform canvas.
   */
  fun extractWaveformPeaks(
    buffer: AudioSampleBuffer,
    bucketCount: Int = 120,
  ): List<Float> {
    val count = bucketCount.coerceIn(1, 2000)
    val totalSamples = buffer.samples.size
    if (totalSamples == 0) return List(count) { 0f }

    val peaks = ArrayList<Float>(count)
    for (bucket in 0 until count) {
      val startIdx = ((bucket.toLong() * totalSamples) / count).toInt().coerceIn(0, totalSamples - 1)
      val endIdx = (((bucket + 1L) * totalSamples) / count).toInt().coerceIn(startIdx + 1, totalSamples)
      var maxAbs = 0
      for (i in startIdx until endIdx) {
        val sample = buffer.samples[i].toInt()
        val absVal = if (sample == Short.MIN_VALUE.toInt()) 32768 else abs(sample)
        if (absVal > maxAbs) maxAbs = absVal
      }
      peaks.add((maxAbs / 32768f).coerceIn(0f, 1f))
    }
    return peaks
  }

  /**
   * Computes the peak normalized amplitude (`0f..1f`) within `[startFraction, endFraction]`.
   */
  fun calculatePeakAmplitude(
    buffer: AudioSampleBuffer,
    startFraction: Float = 0f,
    endFraction: Float = 1f,
  ): Float {
    val (startSample, endSample) = resolveSampleRange(buffer, startFraction, endFraction)
    if (endSample <= startSample) return 0f
    var maxAbs = 0
    for (i in startSample until endSample) {
      val s = buffer.samples[i].toInt()
      val absVal = if (s == Short.MIN_VALUE.toInt()) 32768 else abs(s)
      if (absVal > maxAbs) maxAbs = absVal
    }
    return (maxAbs / 32768f).coerceIn(0f, 1f)
  }

  /**
   * Trims [buffer] to keep only the frames within `[startFraction, endFraction]`.
   */
  fun trim(
    buffer: AudioSampleBuffer,
    startFraction: Float,
    endFraction: Float,
  ): AudioSampleBuffer {
    if (buffer.samples.isEmpty()) return buffer
    val (startSample, endSample) = resolveSampleRange(buffer, startFraction, endFraction)
    if (endSample <= startSample) {
      return AudioSampleBuffer(
        samples = ShortArray(buffer.safeChannels),
        sampleRate = buffer.sampleRate,
        channels = buffer.safeChannels,
      )
    }
    val trimmed = buffer.samples.copyOfRange(startSample, endSample)
    return buffer.copy(samples = trimmed)
  }

  /**
   * Cuts (removes) the frames within `[startFraction, endFraction]` and splices the remaining parts together.
   */
  fun cutSelection(
    buffer: AudioSampleBuffer,
    startFraction: Float,
    endFraction: Float,
  ): AudioSampleBuffer {
    if (buffer.samples.isEmpty()) return buffer
    val (startSample, endSample) = resolveSampleRange(buffer, startFraction, endFraction)
    val prefixLen = startSample
    val suffixLen = (buffer.samples.size - endSample).coerceAtLeast(0)
    val remainingLen = prefixLen + suffixLen
    if (remainingLen <= 0) {
      return AudioSampleBuffer(
        samples = ShortArray(buffer.safeChannels),
        sampleRate = buffer.sampleRate,
        channels = buffer.safeChannels,
      )
    }
    val result = ShortArray(remainingLen)
    if (prefixLen > 0) {
      System.arraycopy(buffer.samples, 0, result, 0, prefixLen)
    }
    if (suffixLen > 0) {
      System.arraycopy(buffer.samples, endSample, result, prefixLen, suffixLen)
    }
    return buffer.copy(samples = result)
  }

  /**
   * Scales sample amplitudes within `[startFraction, endFraction]` by [gainMultiplier] (`0.0f` to `4.0f`).
   */
  fun adjustVolume(
    buffer: AudioSampleBuffer,
    gainMultiplier: Float,
    startFraction: Float = 0f,
    endFraction: Float = 1f,
  ): AudioSampleBuffer {
    if (buffer.samples.isEmpty()) return buffer
    val gain = gainMultiplier.coerceIn(0f, 4f)
    if (abs(gain - 1f) < 1e-4f) return buffer

    val (startSample, endSample) = resolveSampleRange(buffer, startFraction, endFraction)
    if (endSample <= startSample) return buffer

    val out = buffer.samples.copyOf()
    for (i in startSample until endSample) {
      val scaled = (out[i] * gain).roundToInt()
      out[i] = scaled.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return buffer.copy(samples = out)
  }

  /**
   * Normalizes the peak amplitude within `[startFraction, endFraction]` to [targetPeakFraction] (default `0.95f`).
   */
  fun normalize(
    buffer: AudioSampleBuffer,
    targetPeakFraction: Float = 0.95f,
    startFraction: Float = 0f,
    endFraction: Float = 1f,
  ): AudioSampleBuffer {
    val currentPeak = calculatePeakAmplitude(buffer, startFraction, endFraction)
    if (currentPeak <= 1e-5f) return buffer
    val target = targetPeakFraction.coerceIn(0.1f, 1.0f)
    val gain = (target / currentPeak).coerceIn(0.05f, 8.0f)
    val (startSample, endSample) = resolveSampleRange(buffer, startFraction, endFraction)
    val out = buffer.samples.copyOf()
    for (i in startSample until endSample) {
      val scaled = (out[i] * gain).roundToInt()
      out[i] = scaled.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return buffer.copy(samples = out)
  }

  /**
   * Applies linear fade-in and/or fade-out envelopes within `[startFraction, endFraction]`.
   */
  fun applyFade(
    buffer: AudioSampleBuffer,
    fadeInMs: Long,
    fadeOutMs: Long,
    startFraction: Float = 0f,
    endFraction: Float = 1f,
  ): AudioSampleBuffer {
    if (buffer.samples.isEmpty()) return buffer
    if (fadeInMs <= 0L && fadeOutMs <= 0L) return buffer

    val channels = buffer.safeChannels
    val (startSample, endSample) = resolveSampleRange(buffer, startFraction, endFraction)
    val startFrame = startSample / channels
    val endFrame = endSample / channels
    val regionFrames = endFrame - startFrame
    if (regionFrames <= 1) return buffer

    val fadeInFrames = ((fadeInMs.coerceAtLeast(0L) * buffer.sampleRate) / 1000L)
      .toInt()
      .coerceAtMost(regionFrames)
    val fadeOutFrames = ((fadeOutMs.coerceAtLeast(0L) * buffer.sampleRate) / 1000L)
      .toInt()
      .coerceAtMost(regionFrames)

    val out = buffer.samples.copyOf()
    for (frameOffset in 0 until regionFrames) {
      var envelope = 1f
      if (fadeInFrames > 0 && frameOffset < fadeInFrames) {
        envelope = min(envelope, frameOffset.toFloat() / fadeInFrames.toFloat())
      }
      val framesFromEnd = (regionFrames - 1) - frameOffset
      if (fadeOutFrames > 0 && framesFromEnd < fadeOutFrames) {
        envelope = min(envelope, framesFromEnd.toFloat() / fadeOutFrames.toFloat())
      }
      if (envelope < 1f) {
        val baseIdx = (startFrame + frameOffset) * channels
        for (ch in 0 until channels) {
          val s = out[baseIdx + ch]
          out[baseIdx + ch] = (s * envelope).roundToInt()
            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            .toShort()
        }
      }
    }
    return buffer.copy(samples = out)
  }

  /**
   * Changes playback speed (and pitch) by resampling [buffer] by [speedFactor] (`0.25f` to `4.0f`).
   */
  fun changeSpeed(
    buffer: AudioSampleBuffer,
    speedFactor: Float,
  ): AudioSampleBuffer {
    if (buffer.samples.isEmpty()) return buffer
    val speed = speedFactor.coerceIn(0.25f, 4.0f)
    if (abs(speed - 1f) < 1e-4f) return buffer

    val channels = buffer.safeChannels
    val inFrames = buffer.frameCount
    if (inFrames <= 1) return buffer

    val outFrames = max(1, (inFrames / speed).roundToInt())
      .coerceAtMost(MAX_IN_MEMORY_SAMPLES / channels)
    val outSamples = ShortArray(outFrames * channels)

    for (outFrame in 0 until outFrames) {
      val srcPos = outFrame * speed
      val frame0 = srcPos.toInt().coerceIn(0, inFrames - 1)
      val frame1 = (frame0 + 1).coerceAtMost(inFrames - 1)
      val frac = srcPos - frame0
      for (ch in 0 until channels) {
        val s0 = buffer.samples[frame0 * channels + ch]
        val s1 = buffer.samples[frame1 * channels + ch]
        val interpolated = s0 + (s1 - s0) * frac
        outSamples[outFrame * channels + ch] = interpolated.roundToInt()
          .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
          .toShort()
      }
    }
    return buffer.copy(samples = outSamples)
  }

  /**
   * Reverses the audio frames within `[startFraction, endFraction]` while preserving channel order.
   */
  fun reverse(
    buffer: AudioSampleBuffer,
    startFraction: Float = 0f,
    endFraction: Float = 1f,
  ): AudioSampleBuffer {
    if (buffer.samples.isEmpty()) return buffer
    val channels = buffer.safeChannels
    val (startSample, endSample) = resolveSampleRange(buffer, startFraction, endFraction)
    val startFrame = startSample / channels
    val endFrame = endSample / channels
    if (endFrame - startFrame <= 1) return buffer

    val out = buffer.samples.copyOf()
    var leftFrame = startFrame
    var rightFrame = endFrame - 1
    while (leftFrame < rightFrame) {
      val leftBase = leftFrame * channels
      val rightBase = rightFrame * channels
      for (ch in 0 until channels) {
        val tmp = out[leftBase + ch]
        out[leftBase + ch] = out[rightBase + ch]
        out[rightBase + ch] = tmp
      }
      leftFrame++
      rightFrame--
    }
    return buffer.copy(samples = out)
  }

  /**
   * Synthesizes a pleasant 4-note musical chord arpeggio (`C4 - E4 - G4 - C5`) so users can
   * explore the Audio Editor immediately without needing an existing file.
   */
  fun createSampleAudioBuffer(
    sampleRate: Int = 22050,
    durationMs: Long = 3200L,
  ): AudioSampleBuffer {
    val safeRate = sampleRate.coerceIn(8000, 48000)
    val totalFrames = ((durationMs.coerceIn(400L, 10_000L) * safeRate) / 1000L).toInt().coerceAtLeast(1)
    val samples = ShortArray(totalFrames)
    val noteFrequencies = doubleArrayOf(261.63, 329.63, 392.00, 523.25) // C4, E4, G4, C5
    val framesPerNote = (totalFrames / noteFrequencies.size).coerceAtLeast(1)

    for (frame in 0 until totalFrames) {
      val noteIdx = (frame / framesPerNote).coerceIn(0, noteFrequencies.lastIndex)
      val frameInNote = frame - (noteIdx * framesPerNote)
      val noteProgress = frameInNote.toDouble() / framesPerNote.toDouble()

      // Smooth attack & exponential-style decay envelope per note
      val attack = (noteProgress / 0.08).coerceIn(0.0, 1.0)
      val release = ((1.0 - noteProgress) / 0.25).coerceIn(0.0, 1.0)
      val envelope = attack * (0.35 + 0.65 * release)

      val freq = noteFrequencies[noteIdx]
      val t = frame.toDouble() / safeRate.toDouble()
      val fundamental = sin(2.0 * PI * freq * t)
      val harmonic2 = 0.35 * sin(2.0 * PI * (freq * 2.0) * t)
      val harmonic3 = 0.15 * sin(2.0 * PI * (freq * 3.0) * t)

      val normalized = ((fundamental + harmonic2 + harmonic3) / 1.5) * envelope * 0.75
      samples[frame] = (normalized * Short.MAX_VALUE).roundToInt()
        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        .toShort()
    }
    return AudioSampleBuffer(samples = samples, sampleRate = safeRate, channels = 1)
  }

  /**
   * Writes [buffer] as a standard 16-bit PCM RIFF WAVE file using [AudioRecorderEngine.createWavHeader].
   */
  fun writeWavFile(
    buffer: AudioSampleBuffer,
    outputFile: File,
  ): Boolean {
    return try {
      outputFile.parentFile?.mkdirs()
      val pcmBytesLength = buffer.samples.size * 2L
      val header = AudioRecorderEngine.createWavHeader(
        pcmDataLength = pcmBytesLength,
        sampleRate = buffer.sampleRate,
        channels = buffer.safeChannels,
        bitsPerSample = 16,
      )
      BufferedOutputStream(FileOutputStream(outputFile)).use { out ->
        out.write(header)
        val chunkFrames = 4096
        val byteChunk = ByteBuffer.allocate(chunkFrames * 2).order(ByteOrder.LITTLE_ENDIAN)
        var offset = 0
        while (offset < buffer.samples.size) {
          byteChunk.clear()
          val count = min(chunkFrames, buffer.samples.size - offset)
          for (i in 0 until count) {
            byteChunk.putShort(buffer.samples[offset + i])
          }
          out.write(byteChunk.array(), 0, count * 2)
          offset += count
        }
        out.flush()
      }
      outputFile.exists() && outputFile.length() >= 44L
    } catch (_: Exception) {
      false
    }
  }

  /**
   * Parses a RIFF WAVE file into a 16-bit PCM [AudioSampleBuffer].
   * Supports 8-bit unsigned PCM, 16-bit signed PCM, 24-bit signed PCM, 32-bit signed PCM, and 32-bit IEEE float WAVs.
   */
  fun readWavFile(file: File): AudioSampleBuffer? {
    if (!file.exists() || file.length() < 44L) return null
    return try {
      RandomAccessFile(file, "r").use { raf ->
        val riffHeader = ByteArray(12)
        raf.readFully(riffHeader)
        val riff = String(riffHeader, 0, 4, Charsets.US_ASCII)
        val wave = String(riffHeader, 8, 4, Charsets.US_ASCII)
        if (riff != "RIFF" || wave != "WAVE") return null

        var audioFormat = 1
        var channels = 1
        var sampleRate = DEFAULT_SAMPLE_RATE
        var bitsPerSample = 16
        var dataOffset = -1L
        var dataSize = 0L

        while (raf.filePointer + 8L <= raf.length()) {
          val chunkHeader = ByteArray(8)
          raf.readFully(chunkHeader)
          val chunkId = String(chunkHeader, 0, 4, Charsets.US_ASCII)
          val chunkSize = ByteBuffer.wrap(chunkHeader, 4, 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int
            .toLong() and 0xFFFFFFFFL
          val chunkStart = raf.filePointer

          if (chunkId == "fmt " && chunkSize >= 16L) {
            val fmtBytes = ByteArray(min(chunkSize, 40L).toInt())
            raf.readFully(fmtBytes)
            val bb = ByteBuffer.wrap(fmtBytes).order(ByteOrder.LITTLE_ENDIAN)
            audioFormat = bb.getShort(0).toInt() and 0xFFFF
            channels = (bb.getShort(2).toInt() and 0xFFFF).coerceIn(1, 8)
            sampleRate = bb.getInt(4).coerceIn(4000, 192000)
            bitsPerSample = (bb.getShort(14).toInt() and 0xFFFF)
          } else if (chunkId == "data") {
            dataOffset = chunkStart
            dataSize = min(chunkSize, raf.length() - chunkStart)
            break
          }

          val nextPos = chunkStart + chunkSize + (if (chunkSize % 2L == 1L) 1L else 0L)
          if (nextPos <= chunkStart || nextPos > raf.length()) break
          raf.seek(nextPos)
        }

        if (dataOffset < 0L || dataSize <= 0L) return null
        val bytesPerSample = (bitsPerSample / 8).coerceAtLeast(1)
        val totalInputSamples = (dataSize / bytesPerSample).toInt().coerceAtMost(MAX_IN_MEMORY_SAMPLES)
        if (totalInputSamples <= 0) return null

        raf.seek(dataOffset)
        val rawBytes = ByteArray(totalInputSamples * bytesPerSample)
        raf.readFully(rawBytes)
        val bb = ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN)

        // Downmix >2 channels to stereo, keep 1 or 2 channels directly
        val outChannels = channels.coerceIn(1, 2)
        val totalFrames = totalInputSamples / channels
        val outSamples = ShortArray(totalFrames * outChannels)

        for (frame in 0 until totalFrames) {
          for (ch in 0 until outChannels) {
            val sampleIdx = frame * channels + ch
            val bytePos = sampleIdx * bytesPerSample
            val pcm16: Short = when {
              bitsPerSample == 8 -> {
                val u8 = rawBytes[bytePos].toInt() and 0xFF
                ((u8 - 128) shl 8).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
              }
              bitsPerSample == 16 -> {
                bb.getShort(bytePos)
              }
              bitsPerSample == 24 -> {
                val b1 = rawBytes[bytePos + 1].toInt() and 0xFF
                val b2 = rawBytes[bytePos + 2].toInt()
                ((b2 shl 8) or b1).toShort()
              }
              bitsPerSample == 32 && audioFormat == 3 -> {
                val f = bb.getFloat(bytePos).coerceIn(-1f, 1f)
                (f * Short.MAX_VALUE).roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
              }
              bitsPerSample == 32 -> {
                (bb.getInt(bytePos) shr 16).toShort()
              }
              else -> 0
            }
            outSamples[frame * outChannels + ch] = pcm16
          }
        }

        AudioSampleBuffer(
          samples = outSamples,
          sampleRate = sampleRate,
          channels = outChannels,
        )
      }
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Loads any supported audio file into an [AudioSampleBuffer].
   * Uses direct RIFF WAV parsing when possible, or delegates to [FileConverterEngine]
   * (`convertMediaToWavNative` + `convertFile` FFmpeg backup) for compressed formats.
   */
  suspend fun loadAudioFile(
    context: Context?,
    file: File,
  ): Result<AudioSampleBuffer> = withContext(Dispatchers.IO) {
    if (!file.exists() || !file.isFile || file.length() <= 0L) {
      return@withContext Result.failure(IllegalArgumentException("Audio file does not exist or is empty."))
    }

    // 1. Direct WAV read fast-path
    readWavFile(file)?.let { wavBuffer ->
      if (wavBuffer.samples.isNotEmpty()) {
        return@withContext Result.success(wavBuffer)
      }
    }

    // 2. Decode compressed audio via shared FileConverterEngine
    val tempDir = context?.cacheDir ?: file.parentFile ?: File(System.getProperty("java.io.tmpdir") ?: "/tmp")
    val tempWav = File.createTempFile("audio_editor_decode_", ".wav", tempDir)
    var convertedFile: File? = null
    try {
      val nativeSuccess = try {
        FileConverterEngine.convertMediaToWavNative(file, tempWav)
      } catch (_: Throwable) {
        false
      }

      val wavToRead = if (nativeSuccess && tempWav.exists() && tempWav.length() > 44L) {
        tempWav
      } else if (context != null) {
        val convResult = FileConverterEngine.convertFile(
          sourceFile = file,
          targetFormat = ConvertibleFormat.WAV,
          outputDir = tempDir,
          desiredStem = "audio_editor_ffmpeg_${System.nanoTime()}",
          context = context,
        )
        convertedFile = convResult.outputFile
        convResult.outputFile ?: tempWav
      } else {
        tempWav
      }

      val decodedBuffer = readWavFile(wavToRead)
      if (decodedBuffer != null && decodedBuffer.samples.isNotEmpty()) {
        Result.success(decodedBuffer)
      } else {
        Result.failure(IllegalStateException("Could not decode ${file.name} into PCM audio."))
      }
    } catch (e: Exception) {
      Result.failure(e)
    } finally {
      tempWav.delete()
      convertedFile?.delete()
    }
  }

  /**
   * Produces the effective [AudioSampleBuffer] to save or share by applying any uncommitted
   * slider adjustments (volume gain, fade-in/out, speed factor) and active clip selection
   * (`[trimStartFraction, trimEndFraction]`) on top of [buffer].
   */
  fun buildEffectiveOutputBuffer(
    buffer: AudioSampleBuffer,
    trimStartFraction: Float = 0f,
    trimEndFraction: Float = 1f,
    volumeGainPercent: Float = 100f,
    fadeInSeconds: Float = 0f,
    fadeOutSeconds: Float = 0f,
    speedMultiplier: Float = 1.0f,
    applyTrimSelection: Boolean = true,
  ): AudioSampleBuffer {
    var working = buffer
    val gainMultiplier = volumeGainPercent / 100f
    if (abs(gainMultiplier - 1f) > 0.01f) {
      working = adjustVolume(
        buffer = working,
        gainMultiplier = gainMultiplier,
        startFraction = trimStartFraction,
        endFraction = trimEndFraction,
      )
    }
    val fadeInMs = (fadeInSeconds * 1000f).roundToInt().toLong()
    val fadeOutMs = (fadeOutSeconds * 1000f).roundToInt().toLong()
    if (fadeInMs > 0L || fadeOutMs > 0L) {
      working = applyFade(
        buffer = working,
        fadeInMs = fadeInMs,
        fadeOutMs = fadeOutMs,
        startFraction = trimStartFraction,
        endFraction = trimEndFraction,
      )
    }
    if (applyTrimSelection && (trimStartFraction > 0.005f || trimEndFraction < 0.995f)) {
      working = trim(working, trimStartFraction, trimEndFraction)
    }
    if (abs(speedMultiplier - 1f) >= 0.02f) {
      working = changeSpeed(working, speedMultiplier)
    }
    return working
  }

  /**
   * Exports [buffer] to [outputFile] in the specified [format], reusing [AudioRecorderEngine]
   * for WAV headers and [FileConverterEngine] for M4A, MP3, AAC, OGG, and FLAC encoding.
   * When [overwriteExisting] is `true`, writes directly to [outputFile] in place instead of
   * appending a collision counter suffix.
   */
  suspend fun exportAudioBuffer(
    context: Context,
    buffer: AudioSampleBuffer,
    outputFile: File,
    format: AudioExportFormat,
    overwriteExisting: Boolean = false,
  ): Result<File> = withContext(Dispatchers.IO) {
    try {
      val outDir = outputFile.parentFile ?: AudioRecorderEngine.getDefaultAudioDirectory(context)
      outDir.mkdirs()
      val desiredStem = FileConverterEngine.extractFileStem(outputFile.name)

      if (format == AudioExportFormat.WAV) {
        val targetWav = if (overwriteExisting) {
          File(outDir, "$desiredStem.${format.extension}")
        } else {
          FileConverterEngine.resolveOutputFile(
            outputDir = outDir,
            desiredStem = desiredStem,
            targetFormat = ConvertibleFormat.WAV,
          )
        }
        return@withContext if (writeWavFile(buffer, targetWav)) {
          Result.success(targetWav)
        } else {
          Result.failure(IllegalStateException("Failed to write WAV file."))
        }
      }

      val tempWav = File.createTempFile("audio_editor_export_", ".wav", context.cacheDir)
      try {
        if (!writeWavFile(buffer, tempWav)) {
          return@withContext Result.failure(IllegalStateException("Failed to stage temporary WAV for encoding."))
        }
        val convertDir = if (overwriteExisting) context.cacheDir else outDir
        val convResult = FileConverterEngine.convertFile(
          sourceFile = tempWav,
          targetFormat = format.convertibleFormat,
          outputDir = convertDir,
          desiredStem = if (overwriteExisting) "overwrite_stage_${System.nanoTime()}" else desiredStem,
          context = context,
        )
        val stagedFile = convResult.outputFile
        if (convResult.success && stagedFile != null && stagedFile.exists() && stagedFile.length() > 0L) {
          if (overwriteExisting) {
            val destination = File(outDir, "$desiredStem.${format.extension}")
            stagedFile.copyTo(destination, overwrite = true)
            stagedFile.delete()
            Result.success(destination)
          } else {
            Result.success(stagedFile)
          }
        } else {
          Result.failure(IllegalStateException(convResult.message.ifBlank { "Failed to encode ${format.label} audio." }))
        }
      } finally {
        tempWav.delete()
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun resolveSampleRange(
    buffer: AudioSampleBuffer,
    startFraction: Float,
    endFraction: Float,
  ): Pair<Int, Int> {
    val channels = buffer.safeChannels
    val totalFrames = buffer.frameCount
    if (totalFrames <= 0) return 0 to 0
    val clampedStart = startFraction.coerceIn(0f, 1f)
    val clampedEnd = endFraction.coerceIn(clampedStart, 1f)
    val startFrame = (clampedStart * totalFrames).roundToInt().coerceIn(0, totalFrames)
    val endFrame = (clampedEnd * totalFrames).roundToInt().coerceIn(startFrame, totalFrames)
    return (startFrame * channels) to (endFrame * channels)
  }
}
