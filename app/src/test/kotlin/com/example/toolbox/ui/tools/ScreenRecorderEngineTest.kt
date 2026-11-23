package com.example.toolbox.ui.tools

import android.content.Intent
import com.example.toolbox.R
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import com.example.toolbox.ui.main.SCREEN_RECORDER_QUICK_ACTION_START_BOTH
import com.example.toolbox.ui.main.SCREEN_RECORDER_QUICK_ACTION_START_DEVICE
import com.example.toolbox.ui.main.SCREEN_RECORDER_QUICK_ACTION_START_MIC
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolCategory
import java.nio.file.Files
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Test

class ScreenRecorderEngineTest {

  @After
  fun tearDown() {
    ScreenRecorderEngine.installedAppsOverrideForTest = null
    ScreenRecorderEngine.consumePendingQuickRecordRequest()
    ScreenRecorderEngine.stopRecordingSession(null)
    NavigationPreferencesBridge.hexagonIconButtons = false
  }

  @Test
  fun screenRecorderTool_isRegisteredInPhotoAndVideoCategoryBeforeSettings() {
    assertEquals("Screen Recorder", Tool.SCREEN_RECORDER.title)
    assertEquals(ToolCategory.PHOTO_AND_VIDEO, Tool.SCREEN_RECORDER.category)
    assertEquals(Tool.SETTINGS, Tool.entries.last())
    assertTrue(Tool.entries.indexOf(Tool.SCREEN_RECORDER) < Tool.entries.indexOf(Tool.SETTINGS))
  }

  @Test
  fun generateTimestampedFileName_formatsFullScreenSingleAppAndAudioSourcePrefixes() {
    val fixedEpochMs = 1_700_000_000_000L

    // 1. Microphone full screen
    val fullScreenMicName = ScreenRecorderEngine.generateTimestampedFileName(
      targetApp = null,
      audioSource = ScreenAudioSource.MICROPHONE,
      epochMs = fixedEpochMs,
    )
    assertTrue(fullScreenMicName.startsWith("Screen_"))
    assertFalse(fullScreenMicName.startsWith("Screen_Device_"))
    assertFalse(fullScreenMicName.startsWith("Screen_Both_"))
    assertTrue(fullScreenMicName.endsWith(".mp4"))
    assertNull(ScreenRecorderEngine.inferTargetAppLabelFromFileName(fullScreenMicName))
    assertEquals(ScreenAudioSource.MICROPHONE, ScreenRecorderEngine.inferAudioSourceFromFileName(fullScreenMicName))

    // 2. Device Audio full screen
    val fullScreenDeviceName = ScreenRecorderEngine.generateTimestampedFileName(
      targetApp = null,
      audioSource = ScreenAudioSource.DEVICE_AUDIO,
      epochMs = fixedEpochMs,
    )
    assertTrue(fullScreenDeviceName.startsWith("Screen_Device_"))
    assertNull(ScreenRecorderEngine.inferTargetAppLabelFromFileName(fullScreenDeviceName))
    assertEquals(ScreenAudioSource.DEVICE_AUDIO, ScreenRecorderEngine.inferAudioSourceFromFileName(fullScreenDeviceName))

    // 3. Device + Mic (Both) full screen
    val fullScreenBothName = ScreenRecorderEngine.generateTimestampedFileName(
      targetApp = null,
      audioSource = ScreenAudioSource.DEVICE_AND_MIC,
      epochMs = fixedEpochMs,
    )
    assertTrue(fullScreenBothName.startsWith("Screen_Both_"))
    assertNull(ScreenRecorderEngine.inferTargetAppLabelFromFileName(fullScreenBothName))
    assertEquals(ScreenAudioSource.DEVICE_AND_MIC, ScreenRecorderEngine.inferAudioSourceFromFileName(fullScreenBothName))

    // 4. Single app ("Google Chrome") with Device + Mic
    val chromeApp = RecordableAppTarget(
      packageName = "com.android.chrome",
      label = "Google Chrome",
    )
    val singleAppBothName = ScreenRecorderEngine.generateTimestampedFileName(
      targetApp = chromeApp,
      audioSource = ScreenAudioSource.DEVICE_AND_MIC,
      epochMs = fixedEpochMs,
    )
    assertTrue(singleAppBothName.startsWith("Screen_Both_Google_Chrome_"))
    assertTrue(singleAppBothName.endsWith(".mp4"))
    assertEquals("Google Chrome", ScreenRecorderEngine.inferTargetAppLabelFromFileName(singleAppBothName))
    assertEquals(ScreenAudioSource.DEVICE_AND_MIC, ScreenRecorderEngine.inferAudioSourceFromFileName(singleAppBothName))
  }

  @Test
  fun createTimestampedOutputFile_avoidsCollisionsInDirectory() {
    val tempDir = Files.createTempDirectory("screen_rec_test").toFile()
    try {
      val epochMs = 1_700_000_000_000L
      val first = ScreenRecorderEngine.createTimestampedOutputFile(
        directory = tempDir,
        targetApp = null,
        audioSource = ScreenAudioSource.DEVICE_AUDIO,
        epochMs = epochMs,
      )
      first.writeText("clip1")

      val second = ScreenRecorderEngine.createTimestampedOutputFile(
        directory = tempDir,
        targetApp = null,
        audioSource = ScreenAudioSource.DEVICE_AUDIO,
        epochMs = epochMs,
      )
      assertFalse(first.name == second.name)
      assertTrue(second.name.endsWith("_2.mp4"))
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun discoverRecordableApps_filtersBySearchQueryAndSupportsTestOverrides() {
    val fakeApps = listOf(
      RecordableAppTarget(packageName = "org.mozilla.firefox", label = "Firefox"),
      RecordableAppTarget(packageName = "com.spotify.music", label = "Spotify"),
      RecordableAppTarget(packageName = "com.discord", label = "Discord"),
    )
    ScreenRecorderEngine.installedAppsOverrideForTest = fakeApps

    val allApps = ScreenRecorderEngine.discoverRecordableApps(context = null)
    assertEquals(3, allApps.size)

    val byName = ScreenRecorderEngine.discoverRecordableApps(context = null, searchQuery = "spot")
    assertEquals(1, byName.size)
    assertEquals("Spotify", byName.first().label)

    val byPackage = ScreenRecorderEngine.discoverRecordableApps(context = null, searchQuery = "mozilla")
    assertEquals(1, byPackage.size)
    assertEquals("Firefox", byPackage.first().label)
  }

  @Test
  fun writeFallbackMp4File_andListRecordings_produceValidMp4EntriesWithAudioSource() {
    val tempDir = Files.createTempDirectory("screen_rec_list_test").toFile()
    try {
      val appTarget = RecordableAppTarget("com.google.android.youtube", "YouTube")
      val outFile = ScreenRecorderEngine.createTimestampedOutputFile(
        directory = tempDir,
        targetApp = appTarget,
        audioSource = ScreenAudioSource.DEVICE_AUDIO,
        epochMs = 1_700_000_050_000L,
      )
      ScreenRecorderEngine.writeFallbackMp4File(
        outputFile = outFile,
        durationMs = 2_000L,
      )
      assertTrue(outFile.exists())
      assertTrue(outFile.length() >= 128L)

      val listed = ScreenRecorderEngine.listRecordings(tempDir)
      assertEquals(1, listed.size)
      val item = listed.first()
      assertEquals(outFile.name, item.name)
      assertEquals("YouTube", item.targetAppLabel)
      assertEquals(ScreenAudioSource.DEVICE_AUDIO, item.audioSource)
      assertNotNull(item.sizeFormatted)
      assertNotNull(item.durationFormatted)
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun screenRecorderWidgets_resolvesStateMapsQuickActionsAndSupportsHexagonify() {
    val state = ScreenRecorderWidgets.resolveWidgetState(context = null)
    assertEquals("Screen Recorder", state.title)
    assertEquals("Full screen • 1080p", state.subtitle)
    assertEquals(ScreenResolutionPreset.P1080, state.selectedResolution)
    assertEquals(ScreenAudioSource.MICROPHONE, state.selectedAudioSource)
    assertFalse(state.hexagonIconButtons)
    assertEquals(R.drawable.bg_music_widget_circle, state.iconButtonBackgroundRes)

    // Quick action mode mapping for Mic, Device Audio, and Device + Mic
    assertEquals(
      SCREEN_RECORDER_QUICK_ACTION_START_MIC,
      ScreenRecorderWidgets.quickActionModeForSource(ScreenAudioSource.MICROPHONE),
    )
    assertEquals(
      SCREEN_RECORDER_QUICK_ACTION_START_DEVICE,
      ScreenRecorderWidgets.quickActionModeForSource(ScreenAudioSource.DEVICE_AUDIO),
    )
    assertEquals(
      SCREEN_RECORDER_QUICK_ACTION_START_BOTH,
      ScreenRecorderWidgets.quickActionModeForSource(ScreenAudioSource.DEVICE_AND_MIC),
    )

    // Quick record request queue
    ScreenRecorderEngine.requestQuickRecord(
      startRecording = true,
      source = ScreenAudioSource.DEVICE_AND_MIC,
    )
    val pending = ScreenRecorderEngine.consumePendingQuickRecordRequest()
    assertNotNull(pending)
    assertTrue(pending!!.startRecording)
    assertEquals(ScreenAudioSource.DEVICE_AND_MIC, pending.source)
    assertNull(ScreenRecorderEngine.consumePendingQuickRecordRequest())

    // Hexagonify support
    NavigationPreferencesBridge.hexagonIconButtons = true
    val hexState = ScreenRecorderWidgets.resolveWidgetState(context = null)
    assertTrue(hexState.hexagonIconButtons)
    assertEquals(R.drawable.bg_music_widget_hexagon, hexState.iconButtonBackgroundRes)
  }
}
