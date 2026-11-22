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

      // Device audio capture source generates Recording_Device_ filename and infers source accurately
      val deviceFileName = AudioRecorderEngine.generateTimestampedFileName(
        format = AudioRecordingFormat.M4A,
        epochMs = fixedEpoch,
        source = AudioCaptureSource.DEVICE_AUDIO,
      )
      assertTrue(deviceFileName.startsWith("Recording_Device_"))
      assertTrue(deviceFileName.endsWith(".m4a"))
      assertEquals(AudioCaptureSource.DEVICE_AUDIO, AudioRecorderEngine.inferCaptureSource(deviceFileName))
      assertEquals(AudioCaptureSource.MICROPHONE, AudioRecorderEngine.inferCaptureSource(fileName))

      val deviceFile = AudioRecorderEngine.createTimestampedOutputFile(
        directory = tempDir,
        format = AudioRecordingFormat.M4A,
        epochMs = fixedEpoch,
        source = AudioCaptureSource.DEVICE_AUDIO,
      )
      assertTrue(deviceFile.name.startsWith("Recording_Device_"))
      assertEquals(AudioCaptureSource.DEVICE_AUDIO, AudioRecorderEngine.inferCaptureSource(deviceFile.name))

      // Combined (Device + Microphone) audio capture source generates Recording_Both_ filename and infers source accurately
      val bothFileName = AudioRecorderEngine.generateTimestampedFileName(
        format = AudioRecordingFormat.M4A,
        epochMs = fixedEpoch,
        source = AudioCaptureSource.DEVICE_AND_MIC,
      )
      assertTrue(bothFileName.startsWith("Recording_Both_"))
      assertTrue(bothFileName.endsWith(".m4a"))
      assertEquals(AudioCaptureSource.DEVICE_AND_MIC, AudioRecorderEngine.inferCaptureSource(bothFileName))

      val bothFile = AudioRecorderEngine.createTimestampedOutputFile(
        directory = tempDir,
        format = AudioRecordingFormat.M4A,
        epochMs = fixedEpoch,
        source = AudioCaptureSource.DEVICE_AND_MIC,
      )
      assertTrue(bothFile.name.startsWith("Recording_Both_"))
      assertEquals(AudioCaptureSource.DEVICE_AND_MIC, AudioRecorderEngine.inferCaptureSource(bothFile.name))
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
  fun formatDirectoryDisplayPath_andResolveDirectoryInput_hideDownloadByDefault_andReappearWithFullFilesPermission() {
    assertEquals(listOf("Audio", "Recordings"), DEFAULT_AUDIO_RECORDER_FOLDER_NAMES)
    assertEquals(listOf("Audio", "Recordings", "Download"), FULL_FILES_DEFAULT_AUDIO_RECORDER_FOLDER_NAMES)
    assertEquals(listOf("/Audio", "/Recordings"), DEFAULT_AUDIO_RECORDER_FOLDER_DISPLAY_PATHS)
    assertEquals(listOf("/Audio", "/Recordings", "/Download"), FULL_FILES_DEFAULT_AUDIO_RECORDER_FOLDER_DISPLAY_PATHS)
    assertEquals(
      listOf(
        AudioRecorderDefaultFolder.AUDIO,
        AudioRecorderDefaultFolder.RECORDINGS,
      ),
      AudioRecorderDefaultFolder.defaultEntries(hasFullFilesPermission = false),
    )
    assertEquals(
      listOf(
        AudioRecorderDefaultFolder.AUDIO,
        AudioRecorderDefaultFolder.RECORDINGS,
        AudioRecorderDefaultFolder.DOWNLOADS,
      ),
      AudioRecorderDefaultFolder.defaultEntries(hasFullFilesPermission = true),
    )
    assertEquals(
      listOf(
        AudioRecorderDefaultFolder.AUDIO,
        AudioRecorderDefaultFolder.RECORDINGS,
        AudioRecorderDefaultFolder.DOWNLOADS,
      ),
      AudioRecorderDefaultFolder.entries,
    )

    val fakeRoot = File("/storage/emulated/0")
    val defaultAudio = File(fakeRoot, "Audio")
    assertEquals("/Audio", AudioRecorderEngine.formatDirectoryDisplayPath(defaultAudio, fakeRoot))
    assertEquals("/Recordings", AudioRecorderEngine.formatDirectoryDisplayPath(File(fakeRoot, "Recordings"), fakeRoot))
    assertEquals("/Download", AudioRecorderEngine.formatDirectoryDisplayPath(File(fakeRoot, "Download"), fakeRoot))

    val nested = File(fakeRoot, "Music/VoiceMemos")
    assertEquals("/Music/VoiceMemos", AudioRecorderEngine.formatDirectoryDisplayPath(nested, fakeRoot))

    val tempFallback = Files.createTempDirectory("fallback_root").toFile()
    try {
      val resolvedScopedDefaults = AudioRecorderEngine.resolveDefaultDirectories(
        userRoot = File("/non_existent_unwritable_root_path"),
        fallbackBaseDir = tempFallback,
        hasFullFilesPermission = false,
      )
      assertEquals(listOf("Audio", "Recordings"), resolvedScopedDefaults.map { it.name })
      assertTrue(resolvedScopedDefaults.all { it.exists() && it.isDirectory })

      val resolvedFullFilesDefaults = AudioRecorderEngine.resolveDefaultDirectories(
        userRoot = File("/non_existent_unwritable_root_path"),
        fallbackBaseDir = tempFallback,
        hasFullFilesPermission = true,
      )
      assertEquals(listOf("Audio", "Recordings", "Download"), resolvedFullFilesDefaults.map { it.name })
      assertTrue(resolvedFullFilesDefaults.all { it.exists() && it.isDirectory })

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

      val resolvedDownloads = AudioRecorderEngine.resolveDirectoryInput(
        inputPath = "/Download",
        userRoot = File("/non_existent_unwritable_root_path"),
        fallbackBaseDir = tempFallback,
      )
      assertEquals("Download", resolvedDownloads.name)
      assertTrue(resolvedDownloads.exists())
    } finally {
      tempFallback.deleteRecursively()
    }
  }

  @Test
  fun sortRecordings_andGroupRecordings_sortAndGroupAccurately() {
    val items = listOf(
      AudioRecordingItem(
        file = File("/storage/emulated/0/Audio/Recording_20261110_100000.m4a"),
        name = "Recording_20261110_100000.m4a",
        path = "/storage/emulated/0/Audio/Recording_20261110_100000.m4a",
        sizeBytes = 100_000L,
        lastModifiedMs = 1_000L,
        extension = "m4a",
        durationMs = 5_000L,
      ),
      AudioRecordingItem(
        file = File("/storage/emulated/0/Audio/Recording_Device_20261110_120000.wav"),
        name = "Recording_Device_20261110_120000.wav",
        path = "/storage/emulated/0/Audio/Recording_Device_20261110_120000.wav",
        sizeBytes = 500_000L,
        lastModifiedMs = 3_000L,
        extension = "wav",
        durationMs = 20_000L,
      ),
    )

    val byDateDesc = AudioRecorderEngine.sortRecordings(items, AudioRecorderSortOrder.DATE_DESC)
    assertEquals("Recording_Device_20261110_120000.wav", byDateDesc.first().name)

    val byDateAsc = AudioRecorderEngine.sortRecordings(items, AudioRecorderSortOrder.DATE_ASC)
    assertEquals("Recording_20261110_100000.m4a", byDateAsc.first().name)

    val byDurationDesc = AudioRecorderEngine.sortRecordings(items, AudioRecorderSortOrder.DURATION_DESC)
    assertEquals(20_000L, byDurationDesc.first().durationMs)

    val groupedBySource = AudioRecorderEngine.groupRecordings(items, AudioRecorderGroupOption.SOURCE)
    assertEquals(2, groupedBySource.size)
    assertEquals("Microphone", groupedBySource[0].title)
    assertEquals("Device Audio", groupedBySource[1].title)

    val groupedByFormat = AudioRecorderEngine.groupRecordings(items, AudioRecorderGroupOption.FORMAT)
    assertEquals(2, groupedByFormat.size)
    assertEquals("M4A", groupedByFormat[0].title)
    assertEquals("WAV", groupedByFormat[1].title)

    val filteredByWav = AudioRecorderEngine.filterRecordings(items, "wav")
    assertEquals(1, filteredByWav.size)
    assertEquals("Recording_Device_20261110_120000.wav", filteredByWav.first().name)

    val filteredByDevice = AudioRecorderEngine.filterRecordings(items, "Device Audio")
    assertEquals(1, filteredByDevice.size)
    assertEquals("Recording_Device_20261110_120000.wav", filteredByDevice.first().name)
  }

  @Test
  fun parseAndSerializeGrantedFolders_andFolderDocumentId_workAccurately() {
    assertTrue(AudioRecorderEngine.parseGrantedFolders(null).isEmpty())
    assertTrue(AudioRecorderEngine.parseGrantedFolders("").isEmpty())
    assertTrue(AudioRecorderEngine.parseGrantedFolders("   ").isEmpty())

    val parsed = AudioRecorderEngine.parseGrantedFolders("AUDIO, Download, /Recordings")
    assertEquals(
      setOf(
        AudioRecorderDefaultFolder.AUDIO,
        AudioRecorderDefaultFolder.RECORDINGS,
        AudioRecorderDefaultFolder.DOWNLOADS,
      ),
      parsed,
    )

    val serialized = AudioRecorderEngine.serializeGrantedFolders(
      setOf(AudioRecorderDefaultFolder.DOWNLOADS, AudioRecorderDefaultFolder.AUDIO)
    )
    assertEquals("AUDIO,DOWNLOADS", serialized)
    assertEquals(
      setOf(AudioRecorderDefaultFolder.AUDIO, AudioRecorderDefaultFolder.DOWNLOADS),
      AudioRecorderEngine.parseGrantedFolders(serialized),
    )

    assertEquals("primary:Audio", AudioRecorderEngine.buildFolderDocumentId(AudioRecorderDefaultFolder.AUDIO))
    assertEquals("primary:Recordings", AudioRecorderEngine.buildFolderDocumentId(AudioRecorderDefaultFolder.RECORDINGS))
    assertEquals("primary:Download", AudioRecorderEngine.buildFolderDocumentId(AudioRecorderDefaultFolder.DOWNLOADS))

    assertEquals(
      AudioRecorderDefaultFolder.AUDIO,
      AudioRecorderDefaultFolder.fromPathOrName("content://com.android.externalstorage.documents/tree/primary%3AAudio"),
    )
    assertEquals(
      AudioRecorderDefaultFolder.RECORDINGS,
      AudioRecorderDefaultFolder.fromPathOrName("primary:Recordings"),
    )
    assertEquals(
      AudioRecorderDefaultFolder.DOWNLOADS,
      AudioRecorderDefaultFolder.fromPathOrName("primary:Download"),
    )

    // Reordering and persisting custom + default folder pills (scoped storage vs full files permission)
    val customPodcasts = AudioRecorderFolder(
      folderName = "Podcasts",
      displayPath = "/Podcasts",
      customAbsolutePath = "/storage/emulated/0/Podcasts",
    )
    assertTrue(customPodcasts.isCustom)
    assertTrue(!customPodcasts.isDefault)
    assertTrue(DEFAULT_AUDIO_RECORDER_FOLDERS.all { it.isDefault && !it.isCustom })

    val withCustom = DEFAULT_AUDIO_RECORDER_FOLDERS + customPodcasts
    val reordered = AudioRecorderEngine.reorderFolders(withCustom, fromIndex = 2, toIndex = 0)
    assertEquals(listOf("Podcasts", "Audio", "Recordings"), reordered.map { it.folderName })

    val serializedPills = AudioRecorderEngine.serializeFolderPills(reordered, hasFullFilesPermission = false)
    val parsedPills = AudioRecorderEngine.parseFolderPills(serializedPills, hasFullFilesPermission = false)
    assertEquals(reordered, parsedPills)

    // When full files permission is granted, Download re-appears in the folder pills
    val parsedWithFullFiles = AudioRecorderEngine.parseFolderPills(serializedPills, hasFullFilesPermission = true)
    assertEquals(listOf("Podcasts", "Audio", "Recordings", "Download"), parsedWithFullFiles.map { it.folderName })
    assertEquals(
      listOf("Podcasts", "Audio", "Recordings"),
      AudioRecorderEngine.filterFolderPillsForPermission(parsedWithFullFiles, hasFullFilesPermission = false).map { it.folderName },
    )
    assertEquals(
      listOf("Podcasts", "Audio", "Recordings", "Download"),
      AudioRecorderEngine.filterFolderPillsForPermission(reordered, hasFullFilesPermission = true).map { it.folderName },
    )

    // Removing a custom folder removes it while preserving default folders
    val afterRemoveCustom = AudioRecorderEngine.removeCustomFolder(reordered, customPodcasts)
    assertEquals(listOf("Audio", "Recordings"), afterRemoveCustom.map { it.folderName })
    assertEquals(
      afterRemoveCustom,
      AudioRecorderEngine.removeCustomFolder(afterRemoveCustom, AudioRecorderDefaultFolder.AUDIO.toAudioRecorderFolder()),
    )

    // findRecordingListIndex accounts for flat and grouped lists
    val sampleItems = (1..3).map { idx ->
      AudioRecordingItem(
        file = File("/storage/emulated/0/Audio/Rec_$idx.m4a"),
        name = "Rec_$idx.m4a",
        path = "/storage/emulated/0/Audio/Rec_$idx.m4a",
        sizeBytes = 1000L * idx,
        lastModifiedMs = 1000L * idx,
        extension = "m4a",
        durationMs = 1000L,
      )
    }
    val flatGroups = AudioRecorderEngine.groupRecordings(sampleItems, AudioRecorderGroupOption.NONE)
    assertEquals(0, AudioRecorderEngine.findRecordingListIndex(flatGroups, "/storage/emulated/0/Audio/Rec_3.m4a"))
    assertEquals(2, AudioRecorderEngine.findRecordingListIndex(flatGroups, "/storage/emulated/0/Audio/Rec_1.m4a"))

    val formatGroups = AudioRecorderEngine.groupRecordings(sampleItems, AudioRecorderGroupOption.FORMAT)
    // Header at index 0, items at 1..3
    assertEquals(1, AudioRecorderEngine.findRecordingListIndex(formatGroups, "/storage/emulated/0/Audio/Rec_3.m4a"))
    assertEquals(3, AudioRecorderEngine.findRecordingListIndex(formatGroups, "/storage/emulated/0/Audio/Rec_1.m4a"))
  }

  @Test
  fun audioRecorderWidget_resolvesStateAndQuickRecordRequest() {
    val state = AudioRecorderWidgets.resolveWidgetState(context = null)
    assertEquals("Record", state.title)
    assertEquals(AudioRecordingFormat.M4A, state.selectedFormat)
    assertEquals("/Audio", state.directoryDisplayPath)
    assertEquals("M4A • /Audio", state.subtitle)
    assertEquals(16f, AudioRecorderWidgets.WIDGET_CORNER_RADIUS_DP, 0.001f)

    AudioRecorderEngine.requestQuickRecord(startRecording = true, source = AudioCaptureSource.MICROPHONE)
    val pending = AudioRecorderEngine.consumePendingQuickRecordRequest()
    assertNotNull(pending)
    assertTrue(pending!!.startRecording)
    assertEquals(AudioCaptureSource.MICROPHONE, pending.source)
    assertEquals(null, AudioRecorderEngine.consumePendingQuickRecordRequest())
  }
}
