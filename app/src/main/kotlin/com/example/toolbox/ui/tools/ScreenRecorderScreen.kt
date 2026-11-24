package com.example.toolbox.ui.tools

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.Button
import com.example.toolbox.ui.theme.CircleShape
import com.example.toolbox.ui.theme.FilledTonalButton
import com.example.toolbox.ui.theme.FloatingActionButton
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.SmallFloatingActionButton
import com.example.toolbox.ui.theme.TextButton
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import com.example.toolbox.ui.theme.toolboxScreenBackground
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Simple Screen Recorder tool screen.
 *
 * - Presents a clean, editor-style initial screen (`OpenFileInitialView` aesthetic):
 *   3 recording buttons matching [AudioRecorderScreen] (smaller **Device + Mic** button on the left,
 *   larger primary **Microphone** button in the center, and smaller **Device Audio** button on the right),
 *   `"tap to start recording"` below them, and an option near the bottom to select a single app to record
 *   (`"record a single app"` / `"App: <App Name>"`).
 * - Tapping the main canvas or any of the 3 recording buttons starts screen recording with the corresponding
 *   audio capture source ([ScreenAudioSource.DEVICE_AND_MIC], [ScreenAudioSource.MICROPHONE], or
 *   [ScreenAudioSource.DEVICE_AUDIO]).
 * - While recording, displays a live timer and `"tap to stop recording"` with quick app-launch if a
 *   single app is selected.
 * - Completed recordings can be shared, opened directly in **Video Editor**, or browsed via a
 *   recordings history sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenRecorderScreen(
  modifier: Modifier = Modifier,
  initialDirectory: File? = null,
  bypassSystemProjectionPrompt: Boolean = false,
  onOpenVideoEditor: ((File) -> Unit)? = null,
  onNavigateBack: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val isInspection = LocalInspectionMode.current

  val prefs = remember(context, isInspection) {
    if (isInspection) null
    else context.getSharedPreferences(SCREEN_RECORDER_PREFS_NAME, Context.MODE_PRIVATE)
  }

  val outputDirectory = remember(initialDirectory, context, isInspection) {
    initialDirectory ?: if (isInspection) {
      File("/storage/emulated/0/Movies/ScreenRecordings")
    } else {
      ScreenRecorderEngine.getDefaultScreenRecordingsDirectory(context)
    }
  }

  var audioSource by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_SCREEN_RECORD_AUDIO_SOURCE, null)
    mutableStateOf(ScreenAudioSource.fromNameOrDefault(saved))
  }

  var resolution by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_SCREEN_RECORD_RESOLUTION, null)
    mutableStateOf(ScreenResolutionPreset.fromNameOrDefault(saved))
  }

  var selectedAppPackage by rememberSaveable {
    mutableStateOf(prefs?.getString(PREF_KEY_SCREEN_RECORD_SELECTED_APP_PKG, null)?.takeIf { it.isNotBlank() })
  }
  var selectedAppLabel by rememberSaveable {
    mutableStateOf(prefs?.getString(PREF_KEY_SCREEN_RECORD_SELECTED_APP_LABEL, null)?.takeIf { it.isNotBlank() })
  }

  val selectedApp: RecordableAppTarget? = remember(selectedAppPackage, selectedAppLabel) {
    val pkg = selectedAppPackage
    val lbl = selectedAppLabel
    if (!pkg.isNullOrBlank() && !lbl.isNullOrBlank()) {
      RecordableAppTarget(
        packageName = pkg,
        label = lbl,
        accentRgb = ScreenRecorderEngine.computeAppAccentColor(lbl, pkg),
      )
    } else {
      null
    }
  }

  fun updateAudioSource(source: ScreenAudioSource) {
    audioSource = source
    prefs?.edit()?.putString(PREF_KEY_SCREEN_RECORD_AUDIO_SOURCE, source.name)?.apply()
    if (!isInspection) {
      ScreenRecorderWidgets.updateAllWidgets(context)
    }
  }

  fun updateSelectedApp(app: RecordableAppTarget?) {
    selectedAppPackage = app?.packageName
    selectedAppLabel = app?.label
    prefs?.edit()?.apply {
      if (app != null) {
        putString(PREF_KEY_SCREEN_RECORD_SELECTED_APP_PKG, app.packageName)
        putString(PREF_KEY_SCREEN_RECORD_SELECTED_APP_LABEL, app.label)
      } else {
        remove(PREF_KEY_SCREEN_RECORD_SELECTED_APP_PKG)
        remove(PREF_KEY_SCREEN_RECORD_SELECTED_APP_LABEL)
      }
      apply()
    }
    if (!isInspection) {
      ScreenRecorderWidgets.updateAllWidgets(context)
    }
  }

  fun cycleAudioSource() {
    val entries = ScreenAudioSource.entries
    val next = entries[(entries.indexOf(audioSource) + 1) % entries.size]
    updateAudioSource(next)
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
  }

  fun cycleResolution() {
    val entries = ScreenResolutionPreset.entries
    val next = entries[(entries.indexOf(resolution) + 1) % entries.size]
    resolution = next
    prefs?.edit()?.putString(PREF_KEY_SCREEN_RECORD_RESOLUTION, next.name)?.apply()
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    if (!isInspection) {
      ScreenRecorderWidgets.updateAllWidgets(context)
    }
  }

  var showSingleAppPickerDialog by rememberSaveable { mutableStateOf(false) }
  var showRecordingsHistoryDialog by rememberSaveable { mutableStateOf(false) }
  var recordingToDelete by remember { mutableStateOf<ScreenRecordingItem?>(null) }
  var fileForVideoEditor by remember { mutableStateOf<File?>(null) }
  var refreshTrigger by remember { mutableIntStateOf(0) }
  var pendingRequestedSource by rememberSaveable { mutableStateOf(audioSource) }

  val isRecording = ScreenRecorderEngine.isRecording
  val activeRecordingAudioSource = ScreenRecorderEngine.activeAudioSource
  var elapsedRecordingMs by remember { mutableLongStateOf(0L) }
  var latestRecordingBannerItem by remember { mutableStateOf<ScreenRecordingItem?>(null) }

  LaunchedEffect(isRecording, ScreenRecorderEngine.recordingStartEpochMs) {
    if (!isRecording) {
      elapsedRecordingMs = 0L
      return@LaunchedEffect
    }
    val startMs = ScreenRecorderEngine.recordingStartEpochMs.takeIf { it > 0L } ?: System.currentTimeMillis()
    while (isActive && ScreenRecorderEngine.isRecording) {
      elapsedRecordingMs = (System.currentTimeMillis() - startMs).coerceAtLeast(0L)
      delay(100L)
    }
  }

  val savedRecordings = remember(outputDirectory, refreshTrigger, isRecording, isInspection) {
    if (isInspection) {
      emptyList()
    } else {
      ScreenRecorderEngine.listRecordings(outputDirectory)
    }
  }

  fun beginRecordingWithResult(
    source: ScreenAudioSource,
    resultCode: Int,
    data: android.content.Intent?,
  ) {
    latestRecordingBannerItem = null
    updateAudioSource(source)
    ScreenRecorderEngine.startRecordingSession(
      context = context,
      outputDirectory = outputDirectory,
      targetApp = selectedApp,
      resolution = resolution,
      audioSource = source,
      resultCode = resultCode,
      resultData = data,
    )
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    if (selectedApp != null && !isInspection && resultCode == Activity.RESULT_OK) {
      ScreenRecorderEngine.launchTargetAppIfInstalled(context, selectedApp)
    }
  }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { _ -> }

  val projectionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
      beginRecordingWithResult(pendingRequestedSource, result.resultCode, result.data)
    } else {
      Toast.makeText(context, "Screen recording permission declined", Toast.LENGTH_SHORT).show()
    }
  }

  fun stopActiveRecording() {
    val savedItem = ScreenRecorderEngine.stopRecordingSession(context)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    refreshTrigger++
    if (savedItem != null) {
      latestRecordingBannerItem = savedItem
      if (!isInspection) {
        Toast.makeText(context, "Saved ${savedItem.name}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun startRecordingWithSource(source: ScreenAudioSource = audioSource) {
    pendingRequestedSource = source
    updateAudioSource(source)
    if (
      !isInspection &&
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      try {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      } catch (_: Throwable) {}
    }
    if (bypassSystemProjectionPrompt || isInspection) {
      beginRecordingWithResult(source, Activity.RESULT_CANCELED, null)
    } else {
      val captureIntent = ScreenRecorderEngine.createScreenCaptureIntent(
        context = context,
        singleAppMode = selectedApp != null,
      )
      if (captureIntent != null) {
        try {
          projectionLauncher.launch(captureIntent)
        } catch (_: Throwable) {
          beginRecordingWithResult(source, Activity.RESULT_CANCELED, null)
        }
      } else {
        beginRecordingWithResult(source, Activity.RESULT_CANCELED, null)
      }
    }
  }

  fun handleStartOrStopTap(requestedSource: ScreenAudioSource = audioSource) {
    if (ScreenRecorderEngine.isRecording) {
      stopActiveRecording()
    } else {
      startRecordingWithSource(requestedSource)
    }
  }

  // Handle incoming widget quick-record request
  LaunchedEffect(ScreenRecorderEngine.pendingQuickRecordRequest) {
    val pending = ScreenRecorderEngine.pendingQuickRecordRequest ?: return@LaunchedEffect
    ScreenRecorderEngine.consumePendingQuickRecordRequest()
    if (pending.startRecording && !ScreenRecorderEngine.isRecording) {
      startRecordingWithSource(pending.source)
    } else {
      updateAudioSource(pending.source)
    }
  }

  val activeVideoEditorFile = fileForVideoEditor
  if (activeVideoEditorFile != null) {
    VideoEditorScreen(
      initialFile = activeVideoEditorFile,
      initialDirectory = outputDirectory,
      modifier = modifier,
      onNavigateBack = {
        fileForVideoEditor = null
        refreshTrigger++
      },
    )
    return
  }

  if (onNavigateBack != null) {
    BackHandler {
      onNavigateBack()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .testTag("screen_recorder_screen"),
  ) {
    // 1. Standard Tool Header / Top Bar (DESIGN_LANGUAGE.md Section 5.1)
    ToolboxTopBar(
      title = "Screen Recorder",
      subtext = when {
        isRecording && selectedApp != null ->
          "Recording ${selectedApp.label} (${activeRecordingAudioSource.shortLabel}) • ${AudioRecorderEngine.formatDurationMs(elapsedRecordingMs)}"
        isRecording ->
          "Recording screen (${activeRecordingAudioSource.shortLabel}) • ${AudioRecorderEngine.formatDurationMs(elapsedRecordingMs)}"
        selectedApp != null ->
          "Single app: ${selectedApp.label} • ${resolution.label} • ${audioSource.shortLabel}"
        else ->
          "Full screen • ${resolution.label} • ${audioSource.shortLabel}"
      },
      icon = when {
        isRecording && activeRecordingAudioSource == ScreenAudioSource.DEVICE_AUDIO -> Icons.Default.PhoneAndroid
        isRecording && activeRecordingAudioSource == ScreenAudioSource.DEVICE_AND_MIC -> DeviceAndMicIcon
        isRecording -> Icons.Default.FiberManualRecord
        else -> Icons.AutoMirrored.Filled.ScreenShare
      },
      onNavigateBack = onNavigateBack,
      backButtonTestTag = "action_navigate_back",
      badgeContainerColor = if (isRecording) {
        MaterialTheme.colorScheme.errorContainer
      } else {
        MaterialTheme.colorScheme.primaryContainer
      },
      badgeIconTint = if (isRecording) {
        MaterialTheme.colorScheme.error
      } else {
        MaterialTheme.colorScheme.primary
      },
      subtextColor = if (isRecording) {
        MaterialTheme.colorScheme.error
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant
      },
      actions = {
        // Audio source cycle button (Mic / Device / Both / Muted)
        IconButton(
          onClick = { if (!isRecording) cycleAudioSource() },
          enabled = !isRecording,
          modifier = Modifier
            .size(36.dp)
            .testTag("screen_recorder_audio_toggle"),
        ) {
          when (audioSource) {
            ScreenAudioSource.DEVICE_AND_MIC -> {
              DeviceAndMicOverlayIcon(
                size = 20.dp,
                tint = MaterialTheme.colorScheme.primary,
              )
            }
            ScreenAudioSource.DEVICE_AUDIO -> {
              Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = "Audio: ${audioSource.label}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }
            ScreenAudioSource.MICROPHONE -> {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Audio: ${audioSource.label}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }
            ScreenAudioSource.MUTE -> {
              Icon(
                imageVector = Icons.Default.MicOff,
                contentDescription = "Audio: ${audioSource.label}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }

        // Resolution cycle pill
        val resPillShape = if (currentHexagonIconButtons()) HorizontalHexagonShape else RoundedCornerShape(20.dp)
        Surface(
          shape = resPillShape,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
          modifier = Modifier
            .clip(resPillShape)
            .clickable(enabled = !isRecording) { cycleResolution() }
            .testTag("screen_recorder_resolution_button"),
        ) {
          Text(
            text = resolution.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          )
        }

        // Saved recordings history button
        if (savedRecordings.isNotEmpty()) {
          Spacer(modifier = Modifier.width(2.dp))
          IconButton(
            onClick = { showRecordingsHistoryDialog = true },
            modifier = Modifier
              .size(36.dp)
              .testTag("screen_recorder_history_button"),
          ) {
            Icon(
              imageVector = Icons.Default.VideoFile,
              contentDescription = "Saved recordings (${savedRecordings.size})",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      },
    )

    // Optional recent recording banner (after stopping a recording)
    val recentItem = latestRecordingBannerItem
    AnimatedVisibility(visible = !isRecording && recentItem != null && recentItem.file.exists()) {
      if (recentItem != null) {
        RecentScreenRecordingBanner(
          item = recentItem,
          onEditInVideoEditor = {
            onOpenVideoEditor?.invoke(recentItem.file)
            fileForVideoEditor = recentItem.file
          },
          onShare = {
            CrossToolActions.shareFile(context, recentItem.file)
          },
          onDismiss = {
            latestRecordingBannerItem = null
          },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }
    }

    // 2. Simple Editor-Style Initial / Active Recording Canvas
    //    3 recording buttons matching AudioRecorderScreen ([Device + Mic] [Primary Mic] [Device Audio])
    //    + "tap to start recording"
    //    + Bottom option to select a single app to record
    ScreenRecorderInitialCanvas(
      isRecording = isRecording,
      activeRecordingAudioSource = activeRecordingAudioSource,
      defaultAudioSource = audioSource,
      elapsedRecordingMs = elapsedRecordingMs,
      selectedApp = selectedApp,
      onTapCanvasAction = {
        val defaultStartSource = if (audioSource == ScreenAudioSource.MUTE) {
          ScreenAudioSource.MUTE
        } else {
          audioSource
        }
        handleStartOrStopTap(defaultStartSource)
      },
      onTapRecordWithSource = { chosenSource ->
        if (isRecording) {
          stopActiveRecording()
        } else {
          startRecordingWithSource(chosenSource)
        }
      },
      onOpenSingleAppSelector = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        showSingleAppPickerDialog = true
      },
      onClearSingleApp = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        updateSelectedApp(null)
      },
      onLaunchSelectedApp = {
        val target = selectedApp ?: return@ScreenRecorderInitialCanvas
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val launched = ScreenRecorderEngine.launchTargetAppIfInstalled(context, target)
        if (!launched && !isInspection) {
          Toast.makeText(context, "Opening ${target.label}", Toast.LENGTH_SHORT).show()
        }
      },
      modifier = Modifier.weight(1f),
    )
  }

  // Single App Picker Dialog
  if (showSingleAppPickerDialog) {
    SingleAppSelectorDialog(
      selectedApp = selectedApp,
      onSelectApp = { chosenApp ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        updateSelectedApp(chosenApp)
        showSingleAppPickerDialog = false
      },
      onDismiss = { showSingleAppPickerDialog = false },
    )
  }

  // Saved Recordings History Dialog
  if (showRecordingsHistoryDialog) {
    ScreenRecordingsHistoryDialog(
      recordings = savedRecordings,
      directoryDisplayPath = "/${outputDirectory.name}",
      onOpenInVideoEditor = { item ->
        showRecordingsHistoryDialog = false
        onOpenVideoEditor?.invoke(item.file)
        fileForVideoEditor = item.file
      },
      onShare = { item ->
        CrossToolActions.shareFile(context, item.file)
      },
      onDelete = { item ->
        recordingToDelete = item
      },
      onDismiss = { showRecordingsHistoryDialog = false },
    )
  }

  // Delete Recording Confirmation Dialog
  val pendingDelete = recordingToDelete
  if (pendingDelete != null) {
    DeleteScreenRecordingDialog(
      recording = pendingDelete,
      onDismiss = { recordingToDelete = null },
      onConfirmDelete = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        try {
          pendingDelete.file.delete()
        } catch (_: Throwable) {}
        if (latestRecordingBannerItem?.path == pendingDelete.path) {
          latestRecordingBannerItem = null
        }
        recordingToDelete = null
        refreshTrigger++
      },
    )
  }
}

/**
 * Simple editor-style initial screen modeled after [OpenFileInitialView] in the editors, enhanced
 * with the 3 audio capture buttons from [AudioRecorderScreen]:
 * - Left: Smaller **Device + Mic** (`56.dp` `SmallFloatingActionButton` with [DeviceAndMicOverlayIcon])
 * - Center: Primary **Microphone** (`88.dp` `FloatingActionButton` with `Icons.Default.Mic`)
 * - Right: Smaller **Device Audio** (`56.dp` `SmallFloatingActionButton` with `Icons.Default.PhoneAndroid`)
 * - Centered `"tap to start recording"` text below the buttons (transitioning cleanly to
 *   `"tap to stop recording"` with live timecode while recording).
 * - Option near the bottom to select a single app to record (or clear/change the selected app).
 */
@Composable
private fun ScreenRecorderInitialCanvas(
  isRecording: Boolean,
  activeRecordingAudioSource: ScreenAudioSource,
  defaultAudioSource: ScreenAudioSource,
  elapsedRecordingMs: Long,
  selectedApp: RecordableAppTarget?,
  onTapCanvasAction: () -> Unit,
  onTapRecordWithSource: (ScreenAudioSource) -> Unit,
  onOpenSingleAppSelector: () -> Unit,
  onClearSingleApp: () -> Unit,
  onLaunchSelectedApp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isRecordingMic = isRecording &&
    (activeRecordingAudioSource == ScreenAudioSource.MICROPHONE ||
      activeRecordingAudioSource == ScreenAudioSource.MUTE)
  val isRecordingDevice = isRecording && activeRecordingAudioSource == ScreenAudioSource.DEVICE_AUDIO
  val isRecordingBoth = isRecording && activeRecordingAudioSource == ScreenAudioSource.DEVICE_AND_MIC

  val infiniteTransition = rememberInfiniteTransition(label = "ScreenRecordPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = if (isRecording) 1.16f else 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "ScreenRecordPulseScale",
  )

  val micFabContainerColor by animateColorAsState(
    targetValue = if (isRecording) {
      MaterialTheme.colorScheme.error
    } else {
      MaterialTheme.colorScheme.primaryContainer
    },
    label = "ScreenMicFabContainerColor",
  )
  val micFabContentColor by animateColorAsState(
    targetValue = if (isRecording) {
      MaterialTheme.colorScheme.onError
    } else {
      MaterialTheme.colorScheme.onPrimaryContainer
    },
    label = "ScreenMicFabContentColor",
  )

  val bothFabContainerColor by animateColorAsState(
    targetValue = if (isRecordingBoth) {
      MaterialTheme.colorScheme.error
    } else {
      MaterialTheme.colorScheme.secondaryContainer
    },
    label = "ScreenBothFabContainerColor",
  )
  val bothFabContentColor by animateColorAsState(
    targetValue = if (isRecordingBoth) {
      MaterialTheme.colorScheme.onError
    } else {
      MaterialTheme.colorScheme.onSecondaryContainer
    },
    label = "ScreenBothFabContentColor",
  )

  val deviceFabContainerColor by animateColorAsState(
    targetValue = if (isRecordingDevice) {
      MaterialTheme.colorScheme.error
    } else {
      MaterialTheme.colorScheme.secondaryContainer
    },
    label = "ScreenDeviceFabContainerColor",
  )
  val deviceFabContentColor by animateColorAsState(
    targetValue = if (isRecordingDevice) {
      MaterialTheme.colorScheme.onError
    } else {
      MaterialTheme.colorScheme.onSecondaryContainer
    },
    label = "ScreenDeviceFabContentColor",
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .clickable(onClick = onTapCanvasAction)
      .testTag("start_screen_recording_button")
      .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      // 3 Recording Buttons matching AudioRecorderScreen: [Device + Mic] [Primary Mic] [Device Audio]
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        // 1. Left: Smaller Device + Microphone (Both) FAB
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(68.dp),
        ) {
          if (isRecordingBoth) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
            )
          }

          SmallFloatingActionButton(
            onClick = {
              onTapRecordWithSource(ScreenAudioSource.DEVICE_AND_MIC)
            },
            shape = CircleShape,
            containerColor = bothFabContainerColor,
            contentColor = bothFabContentColor,
            elevation = FloatingActionButtonDefaults.elevation(
              defaultElevation = 4.dp,
              pressedElevation = 8.dp,
              focusedElevation = 4.dp,
              hoveredElevation = 4.dp,
            ),
            modifier = Modifier
              .size(56.dp)
              .testTag("both_screen_recorder_fab")
              .semantics {
                contentDescription = if (isRecordingBoth) {
                  "Stop device and microphone screen recording"
                } else {
                  "Record screen with device and microphone audio"
                }
              },
          ) {
            AnimatedContent(
              targetState = isRecordingBoth,
              transitionSpec = {
                (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn())
                  .togetherWith(scaleOut() + fadeOut())
              },
              label = "ScreenBothToStopTransition",
            ) { recordingBoth ->
              if (recordingBoth) {
                Icon(
                  imageVector = Icons.Default.Stop,
                  contentDescription = null,
                  modifier = Modifier
                    .size(26.dp)
                    .testTag("screen_fab_both_stop_icon"),
                )
              } else {
                DeviceAndMicOverlayIcon(
                  size = 26.dp,
                  tint = bothFabContentColor,
                  modifier = Modifier.testTag("screen_fab_both_audio_icon"),
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Center: Primary Microphone / Stop FAB
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(96.dp),
        ) {
          if (isRecordingMic) {
            Box(
              modifier = Modifier
                .size(94.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
            )
          }

          FloatingActionButton(
            onClick = {
              onTapRecordWithSource(ScreenAudioSource.MICROPHONE)
            },
            shape = CircleShape,
            containerColor = micFabContainerColor,
            contentColor = micFabContentColor,
            elevation = FloatingActionButtonDefaults.elevation(
              defaultElevation = 6.dp,
              pressedElevation = 10.dp,
              focusedElevation = 6.dp,
              hoveredElevation = 6.dp,
            ),
            modifier = Modifier
              .size(88.dp)
              .testTag("mic_screen_recorder_fab")
              .semantics {
                contentDescription = if (isRecording) {
                  "Stop screen recording"
                } else {
                  "Record screen with microphone audio"
                }
              },
          ) {
            AnimatedContent(
              targetState = isRecording,
              transitionSpec = {
                (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn())
                  .togetherWith(scaleOut() + fadeOut())
              },
              label = "ScreenMicToStopTransition",
            ) { recording ->
              if (recording) {
                Icon(
                  imageVector = Icons.Default.Stop,
                  contentDescription = null,
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("screen_fab_stop_icon"),
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = null,
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("screen_fab_mic_icon"),
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 3. Right: Smaller Device Audio FAB
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(68.dp),
        ) {
          if (isRecordingDevice) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
            )
          }

          SmallFloatingActionButton(
            onClick = {
              onTapRecordWithSource(ScreenAudioSource.DEVICE_AUDIO)
            },
            shape = CircleShape,
            containerColor = deviceFabContainerColor,
            contentColor = deviceFabContentColor,
            elevation = FloatingActionButtonDefaults.elevation(
              defaultElevation = 4.dp,
              pressedElevation = 8.dp,
              focusedElevation = 4.dp,
              hoveredElevation = 4.dp,
            ),
            modifier = Modifier
              .size(56.dp)
              .testTag("device_screen_recorder_fab")
              .semantics {
                contentDescription = if (isRecordingDevice) {
                  "Stop device audio screen recording"
                } else {
                  "Record screen with device audio"
                }
              },
          ) {
            AnimatedContent(
              targetState = isRecordingDevice,
              transitionSpec = {
                (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn())
                  .togetherWith(scaleOut() + fadeOut())
              },
              label = "ScreenDeviceToStopTransition",
            ) { recordingDevice ->
              if (recordingDevice) {
                Icon(
                  imageVector = Icons.Default.Stop,
                  contentDescription = null,
                  modifier = Modifier
                    .size(26.dp)
                    .testTag("screen_fab_device_stop_icon"),
                )
              } else {
                Icon(
                  imageVector = Icons.Default.PhoneAndroid,
                  contentDescription = null,
                  modifier = Modifier
                    .size(26.dp)
                    .testTag("screen_fab_device_audio_icon"),
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = if (isRecording) "tap to stop recording" else "tap to start recording",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("tap_to_start_recording_text"),
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = when {
          isRecording && activeRecordingAudioSource == ScreenAudioSource.DEVICE_AND_MIC ->
            "Recording screen + device & mic audio"
          isRecording && activeRecordingAudioSource == ScreenAudioSource.DEVICE_AUDIO ->
            "Recording screen + device audio"
          isRecording && activeRecordingAudioSource == ScreenAudioSource.MICROPHONE ->
            "Recording screen + microphone audio"
          isRecording ->
            "Recording screen (muted)"
          else ->
            "Tap mic, phone, or combined button to record screen"
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("screen_recorder_instruction_text"),
      )

      if (isRecording) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = AudioRecorderEngine.formatDurationMs(elapsedRecordingMs),
          style = MaterialTheme.typography.headlineSmall,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.testTag("screen_recording_timer_text"),
        )
      } else if (selectedApp != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Will record ${selectedApp.label}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("selected_app_subtitle_text"),
        )
      }
    }

    // Bottom option to select a single app to record (matching the placement of the sample button in OpenFileInitialView)
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 16.dp),
    ) {
      if (isRecording && selectedApp != null) {
        FilledTonalButton(
          onClick = onLaunchSelectedApp,
          shape = CircleShape,
          modifier = Modifier.testTag("open_recorded_app_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Open ${selectedApp.label}")
        }
      } else if (selectedApp == null) {
        TextButton(
          onClick = onOpenSingleAppSelector,
          enabled = !isRecording,
          shape = CircleShape,
          modifier = Modifier.testTag("select_single_app_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("record a single app")
        }
      } else {
        val chipShape = if (currentHexagonIconButtons()) HorizontalHexagonShape else CircleShape
        Surface(
          shape = chipShape,
          color = MaterialTheme.colorScheme.secondaryContainer,
          contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
          modifier = Modifier
            .clip(chipShape)
            .clickable(enabled = !isRecording, onClick = onOpenSingleAppSelector)
            .testTag("select_single_app_button"),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Apps,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "App: ${selectedApp.label}",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("selected_single_app_label"),
            )
            IconButton(
              onClick = onClearSingleApp,
              enabled = !isRecording,
              modifier = Modifier
                .size(28.dp)
                .testTag("clear_single_app_button")
                .semantics {
                  contentDescription = "Switch back to full screen"
                },
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp),
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Compact banner shown at the top of Screen Recorder right after a recording finishes,
 * offering one-tap Edit in **Video Editor** or Share.
 */
@Composable
private fun RecentScreenRecordingBanner(
  item: ScreenRecordingItem,
  onEditInVideoEditor: () -> Unit,
  onShare: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
    modifier = modifier.testTag("recent_screen_recording_banner"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Movie,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.name,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        val targetInfo = item.targetAppLabel?.let { "$it • " }.orEmpty()
        Text(
          text = "${targetInfo}${item.audioSource.shortLabel} • ${item.durationFormatted} • ${item.sizeFormatted}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      FilledTonalButton(
        onClick = onEditInVideoEditor,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier
          .height(32.dp)
          .testTag("edit_recent_recording_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = null,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Edit",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
        )
      }

      IconButton(
        onClick = onShare,
        modifier = Modifier
          .size(32.dp)
          .testTag("share_recent_recording_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = "Share recording",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp),
        )
      }

      IconButton(
        onClick = onDismiss,
        modifier = Modifier
          .size(28.dp)
          .testTag("dismiss_recent_recording_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Dismiss banner",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

/**
 * Dialog following `DESIGN_LANGUAGE.md` Section 5.2 to select either **Entire Screen** or a
 * single installed application to record.
 */
@Composable
private fun SingleAppSelectorDialog(
  selectedApp: RecordableAppTarget?,
  onSelectApp: (RecordableAppTarget?) -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  var searchQuery by rememberSaveable { mutableStateOf("") }

  val apps = remember(context, isInspection, searchQuery) {
    ScreenRecorderEngine.discoverRecordableApps(
      context = if (isInspection) null else context,
      searchQuery = searchQuery,
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("single_app_selector_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Select App to Record",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Record a single app or the entire screen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Search input field
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search apps...") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
            )
          },
          trailingIcon = if (searchQuery.isNotEmpty()) {
            {
              IconButton(
                onClick = { searchQuery = "" },
                modifier = Modifier.testTag("clear_app_search_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear search",
                )
              }
            }
          } else null,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("single_app_search_input"),
        )

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .testTag("single_app_list"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // 1. Entire Screen (Full Screen) option at the top
          item(key = "_entire_screen_") {
            val isFullScreenSelected = selectedApp == null
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isFullScreenSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
              border = BorderStroke(
                width = if (isFullScreenSelected) 1.5.dp else 1.dp,
                color = if (isFullScreenSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                },
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSelectApp(null) }
                .testTag("app_option_entire_screen"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                  )
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Entire Screen",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "Record everything shown on your display",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                if (isFullScreenSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }
          }

          // 2. Installed / recordable single apps
          items(apps, key = { it.packageName }) { app ->
            val isSelected = selectedApp?.packageName == app.packageName
            val appColor = Color(0xFF000000 or (app.accentRgb.toLong() and 0xFFFFFFL))
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
              border = BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                },
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSelectApp(app) }
                .testTag("app_option_${app.packageName}"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(appColor),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = app.label.trim().take(1).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                  )
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }
          }

          if (apps.isEmpty()) {
            item(key = "_empty_apps_") {
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("single_app_empty_state"),
              ) {
                Text(
                  text = "No apps matched \"$searchQuery\"",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier.testTag("close_single_app_dialog_button"),
      ) {
        Text("Close")
      }
    },
  )
}

/**
 * Dialog listing saved screen recordings with quick actions to open in **Video Editor**, share, or delete.
 */
@Composable
private fun ScreenRecordingsHistoryDialog(
  recordings: List<ScreenRecordingItem>,
  directoryDisplayPath: String,
  onOpenInVideoEditor: (ScreenRecordingItem) -> Unit,
  onShare: (ScreenRecordingItem) -> Unit,
  onDelete: (ScreenRecordingItem) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("screen_recordings_history_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.VideoFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Screen Recordings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${recordings.size} clips in $directoryDisplayPath",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 340.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(recordings, key = { it.path }) { item ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable { onOpenInVideoEditor(item) }
              .testTag("history_recording_item_${item.name}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                val appBadge = item.targetAppLabel?.let { "$it • " }.orEmpty()
                Text(
                  text = "${appBadge}${item.audioSource.shortLabel} • ${item.durationFormatted} • ${item.sizeFormatted}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              IconButton(
                onClick = { onOpenInVideoEditor(item) },
                modifier = Modifier.size(32.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit in Video Editor",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
              }
              IconButton(
                onClick = { onShare(item) },
                modifier = Modifier.size(32.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = "Share",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
                )
              }
              IconButton(
                onClick = { onDelete(item) },
                modifier = Modifier.size(32.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun DeleteScreenRecordingDialog(
  recording: ScreenRecordingItem,
  onDismiss: () -> Unit,
  onConfirmDelete: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.errorContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Delete Screen Recording",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "This action cannot be undone",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector = Icons.Default.VideoFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = recording.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "${recording.sizeFormatted} • ${recording.timestampFormatted}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirmDelete,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        shape = CircleShape,
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Delete")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Cancel")
      }
    },
  )
}

@Preview(showBackground = true)
@Composable
private fun ScreenRecorderScreenPreview() {
  ToolboxTheme {
    ScreenRecorderScreen()
  }
}
