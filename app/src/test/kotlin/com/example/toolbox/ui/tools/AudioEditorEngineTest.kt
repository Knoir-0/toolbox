package com.example.toolbox.ui.tools

import java.io.File
import java.nio.file.Files
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AudioEditorEngineTest {

  @Test
  fun createSampleAudioBuffer_producesValidSamplesAndDuration() {
    val buffer = AudioEditorEngine.createSampleAudioBuffer(sampleRate = 16000, durationMs = 2000L)
    assertEquals(16000, buffer.sampleRate)
    assertEquals(1, buffer.channels)
    assertEquals(32000, buffer.frameCount)
    assertEquals(2000L, buffer.durationMs)
    val peak = AudioEditorEngine.calculatePeakAmplitude(buffer)
    assertTrue("Expected audible peak amplitude > 0.3, got $peak", peak > 0.3f)
  }

  @Test
  fun extractWaveformPeaks_returnsNormalizedBuckets() {
    val samples = ShortArray(1000) { idx ->
      if (idx < 500) 16384 else 0
    }
    val buffer = AudioSampleBuffer(samples = samples, sampleRate = 1000, channels = 1)
    val peaks = AudioEditorEngine.extractWaveformPeaks(buffer, bucketCount = 10)
    assertEquals(10, peaks.size)
    // First 5 buckets should be ~0.5, last 5 buckets should be 0.0
    for (i in 0 until 5) {
      assertEquals(0.5f, peaks[i], 0.01f)
    }
    for (i in 5 until 10) {
      assertEquals(0.0f, peaks[i], 0.001f)
    }
  }

  @Test
  fun trim_keepsSelectedRangeOnly() {
    val samples = ShortArray(100) { it.toShort() }
    val buffer = AudioSampleBuffer(samples = samples, sampleRate = 100, channels = 1)
    val trimmed = AudioEditorEngine.trim(buffer, startFraction = 0.2f, endFraction = 0.7f)
    assertEquals(50, trimmed.frameCount)
    assertEquals(20.toShort(), trimmed.samples.first())
    assertEquals(69.toShort(), trimmed.samples.last())
  }

  @Test
  fun cutSelection_removesSelectedRangeAndSplicesRemaining() {
    val samples = ShortArray(100) { it.toShort() }
    val buffer = AudioSampleBuffer(samples = samples, sampleRate = 100, channels = 1)
    val cut = AudioEditorEngine.cutSelection(buffer, startFraction = 0.25f, endFraction = 0.75f)
    assertEquals(50, cut.frameCount)
    assertEquals(0.toShort(), cut.samples[0])
    assertEquals(24.toShort(), cut.samples[24])
    assertEquals(75.toShort(), cut.samples[25])
    assertEquals(99.toShort(), cut.samples[49])
  }

  @Test
  fun adjustVolume_scalesAndClampsAmplitudeWithinSelection() {
    val samples = shortArrayOf(1000, 2000, 20000, -20000)
    val buffer = AudioSampleBuffer(samples = samples, sampleRate = 4000, channels = 1)
    val boosted = AudioEditorEngine.adjustVolume(buffer, gainMultiplier = 2.0f)
    assertEquals(2000.toShort(), boosted.samples[0])
    assertEquals(4000.toShort(), boosted.samples[1])
    assertEquals(Short.MAX_VALUE, boosted.samples[2])
    assertEquals(Short.MIN_VALUE, boosted.samples[3])

    // Partial selection mute
    val mutedHalf = AudioEditorEngine.adjustVolume(buffer, gainMultiplier = 0f, startFraction = 0f, endFraction = 0.5f)
    assertEquals(0.toShort(), mutedHalf.samples[0])
    assertEquals(0.toShort(), mutedHalf.samples[1])
    assertEquals(20000.toShort(), mutedHalf.samples[2])
    assertEquals((-20000).toShort(), mutedHalf.samples[3])
  }

  @Test
  fun normalize_scalesPeakToTargetFraction() {
    val samples = shortArrayOf(4000, -8000, 2000, -1000)
    val buffer = AudioSampleBuffer(samples = samples, sampleRate = 4000, channels = 1)
    val normalized = AudioEditorEngine.normalize(buffer, targetPeakFraction = 0.90f)
    val newPeak = AudioEditorEngine.calculatePeakAmplitude(normalized)
    assertEquals(0.90f, newPeak, 0.02f)
  }

  @Test
  fun applyFade_attenuatesStartAndEndOfTrack() {
    val samples = ShortArray(1000) { 10000 }
    val buffer = AudioSampleBuffer(samples = samples, sampleRate = 1000, channels = 1) // 1000ms total
    val faded = AudioEditorEngine.applyFade(buffer, fadeInMs = 200L, fadeOutMs = 200L)
    assertEquals(0.toShort(), faded.samples.first())
    assertTrue("Midpoint of fade-in should be ~5000", faded.samples[100] in 4800..5200)
    assertEquals(10000.toShort(), faded.samples[500])
    assertEquals(0.toShort(), faded.samples.last())
  }

  @Test
  fun changeSpeed_resamplesFrameCountProportionally() {
    val samples = ShortArray(1000) { (it * 10).toShort() }
    val buffer = AudioSampleBuffer(samples = samples, sampleRate = 1000, channels = 1)

    val doubleSpeed = AudioEditorEngine.changeSpeed(buffer, speedFactor = 2.0f)
    assertEquals(500, doubleSpeed.frameCount)
    assertEquals(500L, doubleSpeed.durationMs)

    val halfSpeed = AudioEditorEngine.changeSpeed(buffer, speedFactor = 0.5f)
    assertEquals(2000, halfSpeed.frameCount)
    assertEquals(2000L, halfSpeed.durationMs)
  }

  @Test
  fun reverse_reversesFramesWhilePreservingStereoChannelOrder() {
    // 3 stereo frames: (10, 11), (20, 21), (30, 31)
    val stereoSamples = shortArrayOf(10, 11, 20, 21, 30, 31)
    val buffer = AudioSampleBuffer(samples = stereoSamples, sampleRate = 1000, channels = 2)
    val reversed = AudioEditorEngine.reverse(buffer)
    val expected = shortArrayOf(30, 31, 20, 21, 10, 11)
    assertTrue(expected.contentEquals(reversed.samples))
  }

  @Test
  fun writeAndReadWavFile_roundTripsLosslessly_andSharesFormatWithAudioRecorder() = runBlocking {
    val tempDir = Files.createTempDirectory("audio_editor_test").toFile()
    try {
      val original = AudioEditorEngine.createSampleAudioBuffer(sampleRate = 22050, durationMs = 1000L)
      val wavFile = File(tempDir, "test_track.wav")

      assertTrue(AudioEditorEngine.writeWavFile(original, wavFile))
      assertTrue(AudioEditorEngine.isSupportedAudioFile(wavFile))

      // Verify AudioRecorderEngine can also read duration from the written WAV
      val recorderDuration = AudioRecorderEngine.readWavDurationMs(wavFile)
      assertEquals(1000L, recorderDuration)

      // Verify readWavFile & loadAudioFile round-trip the exact samples
      val readBack = AudioEditorEngine.readWavFile(wavFile)
      assertNotNull(readBack)
      assertEquals(original, readBack)

      val loadedResult = AudioEditorEngine.loadAudioFile(context = null, file = wavFile)
      assertTrue(loadedResult.isSuccess)
      assertEquals(original, loadedResult.getOrNull())
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun undoRedoHistory_sharesImageEditorHistoryWithAudioSnapshots() {
    val history = ImageEditorHistory<AudioEditSnapshot>(maxDepth = 10)
    val base = AudioSampleBuffer(shortArrayOf(100, 200, 300, 400), sampleRate = 1000, channels = 1)
    val trimmed = AudioEditorEngine.trim(base, 0f, 0.5f)

    assertFalse(history.canUndo)
    history.push(AudioEditSnapshot(buffer = base, actionLabel = "Trim"))
    assertTrue(history.canUndo)

    val undone = history.undo(AudioEditSnapshot(buffer = trimmed))
    assertNotNull(undone)
    assertEquals(base, undone!!.buffer)
    assertTrue(history.canRedo)

    val redone = history.redo(undone)
    assertNotNull(redone)
    assertEquals(trimmed, redone!!.buffer)
  }

  @Test
  fun resolveWaveformHandleTarget_and_updateSelectionFromHandles_supportsBothSequentialAndSimultaneousHandles() {
    // 1. Single-finger target resolution near start, end, and middle playhead
    assertEquals(
      WaveformDragTarget.START_HANDLE,
      AudioEditorEngine.resolveWaveformHandleTarget(touchFraction = 0.02f, startFraction = 0f, endFraction = 1f),
    )
    assertEquals(
      WaveformDragTarget.END_HANDLE,
      AudioEditorEngine.resolveWaveformHandleTarget(touchFraction = 0.97f, startFraction = 0f, endFraction = 1f),
    )
    assertEquals(
      WaveformDragTarget.PLAYHEAD,
      AudioEditorEngine.resolveWaveformHandleTarget(touchFraction = 0.50f, startFraction = 0f, endFraction = 1f),
    )

    // 2. Multi-finger target resolution: when one handle is already claimed, second finger claims the opposite handle
    assertEquals(
      WaveformDragTarget.END_HANDLE,
      AudioEditorEngine.resolveWaveformHandleTarget(
        touchFraction = 0.85f,
        startFraction = 0.1f,
        endFraction = 0.9f,
        alreadyClaimedTarget = WaveformDragTarget.START_HANDLE,
      ),
    )
    assertEquals(
      WaveformDragTarget.START_HANDLE,
      AudioEditorEngine.resolveWaveformHandleTarget(
        touchFraction = 0.15f,
        startFraction = 0.1f,
        endFraction = 0.9f,
        alreadyClaimedTarget = WaveformDragTarget.END_HANDLE,
      ),
    )

    // 3. Sequential dragging: dragging start handle then end handle preserves both handles without resetting
    val (afterStartDrag, endAfterStartDrag) = AudioEditorEngine.updateSelectionFromHandles(
      currentStart = 0f,
      currentEnd = 1f,
      draggedStart = 0.25f,
      draggedEnd = null,
    )
    assertEquals(0.25f, afterStartDrag, 1e-4f)
    assertEquals(1.0f, endAfterStartDrag, 1e-4f)

    val (startAfterEndDrag, afterEndDrag) = AudioEditorEngine.updateSelectionFromHandles(
      currentStart = afterStartDrag,
      currentEnd = endAfterStartDrag,
      draggedStart = null,
      draggedEnd = 0.75f,
    )
    assertEquals(0.25f, startAfterEndDrag, 1e-4f)
    assertEquals(0.75f, afterEndDrag, 1e-4f)

    // 4. Simultaneous dragging: dragging both start and end handles at the same time updates both
    val (simStart, simEnd) = AudioEditorEngine.updateSelectionFromHandles(
      currentStart = 0.25f,
      currentEnd = 0.75f,
      draggedStart = 0.15f,
      draggedEnd = 0.85f,
    )
    assertEquals(0.15f, simStart, 1e-4f)
    assertEquals(0.85f, simEnd, 1e-4f)
  }

  @Test
  fun `buildEffectiveOutputBuffer applies pending clip selection volume fade and speed`() {
    val samples = ShortArray(1000) { 1000 }
    val buffer = AudioSampleBuffer(samples = samples, sampleRate = 1000, channels = 1)

    // Pending clip selection (20% to 70% = 500 frames), 200% gain, and 2.0x speed (= 250 frames)
    val output = AudioEditorEngine.buildEffectiveOutputBuffer(
      buffer = buffer,
      trimStartFraction = 0.2f,
      trimEndFraction = 0.7f,
      volumeGainPercent = 200f,
      fadeInSeconds = 0f,
      fadeOutSeconds = 0f,
      speedMultiplier = 2.0f,
    )

    assertEquals(250, output.frameCount)
    assertTrue(output.samples.all { it.toInt() == 2000 })
  }

  @Test
  fun `AudioExportFormat fromFileName resolves matching format`() {
    assertEquals(AudioExportFormat.WAV, AudioExportFormat.fromFileName("clip.wav"))
    assertEquals(AudioExportFormat.M4A, AudioExportFormat.fromFileName("voice.m4a"))
    assertEquals(AudioExportFormat.AAC, AudioExportFormat.fromFileName("track.aac"))
    assertEquals(AudioExportFormat.MP3, AudioExportFormat.fromFileName("song.mp3"))
    assertEquals(AudioExportFormat.WAV, AudioExportFormat.fromFileName("unknown.xyz"))
  }
}
