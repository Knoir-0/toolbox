package com.example.toolbox.ui.tools

import java.io.File
import java.nio.file.Files
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AudioRecorderEngineTest {

  @Test
  fun defaultFormat_isM4a_andResolvesByNameOrExtension() {
    assertEquals(AudioRecordingFormat.M4A, AudioRecordingFormat.DEFAULT)
    assertEquals("m4a", AudioRecordingFormat.DEFAULT.extension)
    assertEquals("M4A", AudioRecordingFormat.DEFAULT.shortLabel)

    assertEquals(AudioRecordingFormat.M4A, AudioRecordingFormat.fromNameOrDefault(null))
    assertEquals(AudioRecordingFormat.M4A, AudioRecordingFormat.fromNameOrDefault(""))
    assertEquals(AudioRecordingFormat.M4A, AudioRecordingFormat.fromNameOrDefault("UNKNOWN"))
    assertEquals(AudioRecordingFormat.WAV, AudioRecordingFormat.fromNameOrDefault("WAV"))
    assertEquals(AudioRecordingFormat.WAV, AudioRecordingFormat.fromNameOrDefault(".wav"))
    assertEquals(AudioRecordingFormat.AAC, AudioRecordingFormat.fromNameOrDefault("aac"))
    assertEquals(AudioRecordingFormat.OGG, AudioRecordingFormat.fromNameOrDefault("OGG"))
    assertEquals(AudioRecordingFormat.THREE_GPP, AudioRecordingFormat.fromNameOrDefault("3gp"))
  }

  @Test
  fun formatDurationMs_andFormatRecordingTimerMs_formatAccurately() {
    assertEquals("00:00", AudioRecorderEngine.formatDurationMs(0L))
    assertEquals("00:05", AudioRecorderEngine.formatDurationMs(5_400L))
    assertEquals("01:05", AudioRecorderEngine.formatDurationMs(65_000L))
    assertEquals("1:01:05", AudioRecorderEngine.formatDurationMs(3_665_000L))

    assertEquals("00:00", AudioRecorderEngine.formatRecordingTimerMs(0L))
    assertEquals("00:03", AudioRecorderEngine.formatRecordingTimerMs(3_450L))
    assertEquals("01:12", AudioRecorderEngine.formatRecordingTimerMs(72_890L))
  }

  @Test
  fun generateTimestampedFileName_andCreateTimestampedOutputFile_handleCollisions() {
    val tempDir = Files.createTempDirectory("audio_test").toFile()
    try {
      val fixedEpoch = 1794336323000L
      val fileName = AudioRecorderEngine.generateTimestampedFileName(AudioRecordingFormat.M4A, fixedEpoch)
      assertTrue(fileName.startsWith("Recording_"))
      assertTrue(fileName.endsWith(".m4a"))

      val firstFile = AudioRecorderEngine.createTimestampedOutputFile(
        directory = tempDir,
        format = AudioRecordingFormat.WAV,
        epochMs = fixedEpoch,
      )
      assertEquals(tempDir, firstFile.parentFile)
      assertTrue(firstFile.name.endsWith(".wav"))
      firstFile.writeText("stub")

      // Second call at same timestamp should append _1
      val secondFile = AudioRecorderEngine.createTimestampedOutputFile(
        directory = tempDir,
        format = AudioRecordingFormat.WAV,
        epochMs = fixedEpoch,
      )
      assertTrue(secondFile.name.endsWith("_1.wav"))
      assertFalse(firstFile.name == secondFile.name)
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun writeFallbackAudioFile_writesValidWavHeaderAndListsInRecordings() {
    val tempDir = Files.createTempDirectory("audio_recordings_test").toFile()
    try {
      val wavFile = File(tempDir, "Recording_20261110_120000.wav")
      AudioRecorderEngine.writeFallbackAudioFile(wavFile, durationMs = 2000L, sampleRate = 22050)
      assertTrue(wavFile.exists())
      assertTrue(wavFile.length() > 44L)

      val wavDuration = AudioRecorderEngine.readWavDurationMs(wavFile)
      assertNotNull(wavDuration)
      assertEquals(2000L, wavDuration!!)

      val m4aFile = File(tempDir, "Recording_20261110_120500.m4a")
      AudioRecorderEngine.writeFallbackAudioFile(m4aFile, durationMs = 1500L)
      m4aFile.setLastModified(wavFile.lastModified() + 5000L)

      // Non-audio file should be ignored
      File(tempDir, "notes.txt").writeText("ignore me")

      val listed = AudioRecorderEngine.listRecordings(tempDir, includeDurationMetadata = false)
      assertEquals(2, listed.size)
      // Sorted newest first
      assertEquals("Recording_20261110_120500.m4a", listed[0].name)
      assertEquals("M4A", listed[0].formatBadge)
      assertEquals("Recording_20261110_120000.wav", listed[1].name)
      assertEquals("WAV", listed[1].formatBadge)
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun formatDirectoryDisplayPath_andResolveDirectoryInput_defaultToAudioFolder() {
    val fakeRoot = File("/storage/emulated/0")
    val defaultAudio = File(fakeRoot, "Audio")
    assertEquals("/Audio", AudioRecorderEngine.formatDirectoryDisplayPath(defaultAudio, fakeRoot))

    val nested = File(fakeRoot, "Music/VoiceMemos")
    assertEquals("/Music/VoiceMemos", AudioRecorderEngine.formatDirectoryDisplayPath(nested, fakeRoot))

    val tempFallback = Files.createTempDirectory("fallback_root").toFile()
    try {
      val resolvedDefault = AudioRecorderEngine.resolveDirectoryInput(
        inputPath = "/Audio",
        userRoot = File("/non_existent_unwritable_root_path"),
        fallbackBaseDir = tempFallback,
      )
      assertEquals("Audio", resolvedDefault.name)
      assertTrue(resolvedDefault.exists())

      val resolvedCustom = AudioRecorderEngine.resolveDirectoryInput(
        inputPath = "/Recordings",
        userRoot = File("/non_existent_unwritable_root_path"),
        fallbackBaseDir = tempFallback,
      )
      assertEquals("Recordings", resolvedCustom.name)
      assertTrue(resolvedCustom.exists())
    } finally {
      tempFallback.deleteRecursively()
    }
  }
}
