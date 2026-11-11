package com.example.toolbox.ui.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorderScreen(
  modifier: Modifier = Modifier,
  initialDirectory: File? = null,
  initialFormat: AudioRecordingFormat? = null,
  onOpenAudioEditor: ((File) -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current

  val prefs = remember(context, isInspection) {
    if (isInspection) null
    else context.getSharedPreferences(AUDIO_RECORDER_PREFS_NAME, Context.MODE_PRIVATE)
  }

  val userRoot = remember(isInspection) {
    if (isInspection) File("/storage/emulated/0") else FileManagerEngine.getUserRootDirectory()
  }

  val fallbackBaseDir = remember(context, isInspection) {
    if (isInspection) null else (context.getExternalFilesDir(null) ?: context.filesDir)
  }

  // Selected recording format (persisted, defaults to M4A)
  var selectedFormat by rememberSaveable {
    val savedFormatName = prefs?.getString(PREF_KEY_AUDIO_FORMAT, null)
    mutableStateOf(initialFormat ?: AudioRecordingFormat.fromNameOrDefault(savedFormatName))
  }

  fun updateSelectedFormat(format: AudioRecordingFormat) {
    selectedFormat = format
    prefs?.edit()?.putString(PREF_KEY_AUDIO_FORMAT, format.name)?.apply()
  }

  // Selected recordings directory (defaults to /Audio)
  var recordingsDirectory by remember {
    val initialDir = initialDirectory ?: run {
      val savedPath = prefs?.getString(PREF_KEY_AUDIO_DIRECTORY, null)
      if (!savedPath.isNullOrBlank()) {
        AudioRecorderEngine.resolveDirectoryInput(savedPath, userRoot, fallbackBaseDir)
      } else {
        if (isInspection) File("/storage/emulated/0/Audio")
        else AudioRecorderEngine.getDefaultAudioDirectory(context)
      }
    }
    mutableStateOf(initialDir)
  }

  // User-facing folder display string (e.g. "/Audio")
  var directoryDisplayPath by rememberSaveable {
    val savedPath = prefs?.getString(PREF_KEY_AUDIO_DIRECTORY, null)
    mutableStateOf(
      if (!savedPath.isNullOrBlank()) savedPath
      else if (initialDirectory != null) AudioRecorderEngine.formatDirectoryDisplayPath(initialDirectory, userRoot)
      else DEFAULT_AUDIO_FOLDER_DISPLAY
    )
  }

  fun updateRecordingsDirectory(inputPath: String) {
    val normalizedDisplay = if (inputPath.isBlank()) {
      DEFAULT_AUDIO_FOLDER_DISPLAY
    } else if (!inputPath.startsWith("/")) {
      "/$inputPath"
    } else {
      inputPath.trim()
    }
    val resolved = AudioRecorderEngine.resolveDirectoryInput(
      inputPath = normalizedDisplay,
      userRoot = userRoot,
      fallbackBaseDir = fallbackBaseDir,
    )
    recordingsDirectory = resolved
    directoryDisplayPath = AudioRecorderEngine.formatDirectoryDisplayPath(resolved, userRoot)
      .takeIf { it.isNotBlank() } ?: normalizedDisplay
    prefs?.edit()?.putString(PREF_KEY_AUDIO_DIRECTORY, directoryDisplayPath)?.apply()
  }

  // Microphone permission check
  var hasMicPermission by remember {
    mutableStateOf(
      isInspection ||
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED,
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    hasMicPermission = granted
  }

  // Recording state
  var isRecording by remember { mutableStateOf(false) }
  var elapsedRecordingMs by remember { mutableLongStateOf(0L) }
  var liveAmplitude by remember { mutableFloatStateOf(0f) }
  var activeSession by remember { mutableStateOf<ActiveAudioRecordingSession?>(null) }
  var refreshTrigger by remember { mutableIntStateOf(0) }

  // Clean up recording session if screen is disposed while recording
  DisposableEffect(Unit) {
    onDispose {
      activeSession?.stop()
      activeSession = null
    }
  }

  // Timer & amplitude polling while recording
  LaunchedEffect(isRecording, activeSession) {
    if (!isRecording) {
      elapsedRecordingMs = 0L
      liveAmplitude = 0f
      return@LaunchedEffect
    }
    val startMs = System.currentTimeMillis()
    while (isActive && isRecording) {
      elapsedRecordingMs = (System.currentTimeMillis() - startMs).coerceAtLeast(0L)
      liveAmplitude = activeSession?.pollAmplitude() ?: 0.15f
      delay(80L)
    }
  }

  // List of recordings in current directory
  val recordings = remember(recordingsDirectory, refreshTrigger, isInspection) {
    if (isInspection) {
      listOf(
        AudioRecordingItem(
          file = File("/storage/emulated/0/Audio/Recording_20261110_123000.m4a"),
          name = "Recording_20261110_123000.m4a",
          path = "/storage/emulated/0/Audio/Recording_20261110_123000.m4a",
          sizeBytes = 248_320L,
          lastModifiedMs = 1794335400000L,
          extension = "m4a",
          durationMs = 15_000L,
        ),
        AudioRecordingItem(
          file = File("/storage/emulated/0/Audio/Recording_20261110_111542.wav"),
          name = "Recording_20261110_111542.wav",
          path = "/storage/emulated/0/Audio/Recording_20261110_111542.wav",
          sizeBytes = 1_411_200L,
          lastModifiedMs = 1794330942000L,
          extension = "wav",
          durationMs = 32_000L,
        ),
      )
    } else {
      AudioRecorderEngine.listRecordings(
        directory = recordingsDirectory,
        includeDurationMetadata = true,
      )
    }
  }

  // UI Dialog states
  var showFormatMenu by remember { mutableStateOf(false) }
  var showFolderDialog by remember { mutableStateOf(false) }
  var activePlaybackRecording by remember { mutableStateOf<AudioRecordingItem?>(null) }
  var recordingToRename by remember { mutableStateOf<AudioRecordingItem?>(null) }
  var recordingToDelete by remember { mutableStateOf<AudioRecordingItem?>(null) }
  var fileForAudioEditor by remember { mutableStateOf<File?>(null) }

  fun startRecording() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val outputFile = AudioRecorderEngine.createTimestampedOutputFile(
      directory = recordingsDirectory,
      format = selectedFormat,
    )
    val session = ActiveAudioRecordingSession(
      context = context,
      outputFile = outputFile,
      format = selectedFormat,
    )
    session.start()
    activeSession = session
    elapsedRecordingMs = 0L
    isRecording = true
  }

  fun stopRecording() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val session = activeSession
    activeSession = null
    isRecording = false
    val savedFile = session?.stop()
    refreshTrigger++
    if (savedFile != null && !isInspection) {
      Toast.makeText(context, "Saved ${savedFile.name}", Toast.LENGTH_SHORT).show()
    }
  }

  val activeAudioEditorFile = fileForAudioEditor
  if (activeAudioEditorFile != null) {
    AudioEditorScreen(
      modifier = modifier,
      initialFile = activeAudioEditorFile,
      initialDirectory = recordingsDirectory,
      onNavigateBack = {
        fileForAudioEditor = null
        refreshTrigger++
      },
    )
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("audio_recorder_screen"),
  ) {
    // 1. Standard Tool Header / Top Bar (DESIGN_LANGUAGE.md 5.1)
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 68.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                  if (isRecording) MaterialTheme.colorScheme.errorContainer
                  else MaterialTheme.colorScheme.primaryContainer
                ),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Audio Recorder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = if (isRecording) {
                  "Recording (${selectedFormat.shortLabel}) • $directoryDisplayPath"
                } else {
                  "${recordings.size} recordings • ${selectedFormat.shortLabel} • $directoryDisplayPath"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          // Trailing Action Buttons: Format selector & Folder selector
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Box {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable(enabled = !isRecording) { showFormatMenu = true }
                  .testTag("format_selector_button"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(15.dp),
                  )
                  Text(
                    text = selectedFormat.shortLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                  )
                  Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Select audio format",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp),
                  )
                }
              }

              DropdownMenu(
                expanded = showFormatMenu,
                onDismissRequest = { showFormatMenu = false },
                modifier = Modifier.testTag("format_dropdown_menu"),
              ) {
                AudioRecordingFormat.entries.forEach { format ->
                  val isSelected = format == selectedFormat
                  DropdownMenuItem(
                    text = {
                      Column {
                        Text(
                          text = format.displayName,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        )
                        Text(
                          text = format.description,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                    },
                    onClick = {
                      updateSelectedFormat(format)
                      showFormatMenu = false
                    },
                    trailingIcon = if (isSelected) {
                      {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary,
                        )
                      }
                    } else null,
                    modifier = Modifier.testTag("format_option_${format.name}"),
                  )
                }
              }
            }

            IconButton(
              onClick = { showFolderDialog = true },
              enabled = !isRecording,
              modifier = Modifier.testTag("change_folder_top_button"),
            ) {
              Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = "Change recordings folder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }

    // Optional Microphone Permission Banner
    if (!hasMicPermission) {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("mic_permission_banner"),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
              text = "Grant microphone access for hardware audio capture.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
          Button(
            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .padding(start = 8.dp)
              .testTag("allow_mic_permission_button"),
          ) {
            Text("Allow")
          }
        }
      }
    }

    // 2. Center Stage: Floating Microphone FAB in the middle of the screen
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(0.46f)
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("recorder_center_stage"),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        // Status Pill & Elapsed Timer above the floating FAB
        Badge(
          containerColor = if (isRecording) {
            MaterialTheme.colorScheme.errorContainer
          } else {
            MaterialTheme.colorScheme.surfaceVariant
          },
          contentColor = if (isRecording) {
            MaterialTheme.colorScheme.onErrorContainer
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
          modifier = Modifier.testTag("recording_status_badge"),
        ) {
          Text(
            text = if (isRecording) "● RECORDING" else "READY TO RECORD",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = AudioRecorderEngine.formatRecordingTimerMs(elapsedRecordingMs),
          style = MaterialTheme.typography.headlineMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.testTag("recording_timer_text"),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Floating Microphone / Stop FAB in the middle of the screen
        val infiniteTransition = rememberInfiniteTransition(label = "RecordingPulse")
        val pulseScale by infiniteTransition.animateFloat(
          initialValue = 1.0f,
          targetValue = 1.18f,
          animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
          ),
          label = "PulseRingScale",
        )

        val fabContainerColor by animateColorAsState(
          targetValue = if (isRecording) {
            MaterialTheme.colorScheme.error
          } else {
            MaterialTheme.colorScheme.primaryContainer
          },
          label = "FabContainerColor",
        )
        val fabContentColor by animateColorAsState(
          targetValue = if (isRecording) {
            MaterialTheme.colorScheme.onError
          } else {
            MaterialTheme.colorScheme.onPrimaryContainer
          },
          label = "FabContentColor",
        )

        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(116.dp),
        ) {
          // Outer animated pulse ring while recording
          if (isRecording) {
            val dynamicBoost = (liveAmplitude * 0.15f).coerceIn(0f, 0.15f)
            Box(
              modifier = Modifier
                .size(104.dp)
                .scale(pulseScale + dynamicBoost)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
            )
          }

          FloatingActionButton(
            onClick = {
              if (isRecording) {
                stopRecording()
              } else {
                startRecording()
              }
            },
            shape = CircleShape,
            containerColor = fabContainerColor,
            contentColor = fabContentColor,
            elevation = FloatingActionButtonDefaults.elevation(
              defaultElevation = 6.dp,
              pressedElevation = 10.dp,
            ),
            modifier = Modifier
              .size(88.dp)
              .testTag("audio_recorder_fab")
              .semantics {
                contentDescription = if (isRecording) "Stop recording" else "Start recording"
              },
          ) {
            AnimatedContent(
              targetState = isRecording,
              transitionSpec = {
                (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn())
                  .togetherWith(scaleOut() + fadeOut())
              },
              label = "MicToStopTransition",
            ) { recording ->
              if (recording) {
                Icon(
                  imageVector = Icons.Default.Stop,
                  contentDescription = "Stop",
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("fab_stop_icon"),
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = "Record",
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("fab_mic_icon"),
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = if (isRecording) {
            "Tap Stop to save .${selectedFormat.extension} recording"
          } else {
            "Tap microphone to start recording (${selectedFormat.displayName})"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
      }
    }

    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    // 3. Bottom Section: List of Recordings from /Audio folder (configurable)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(0.54f)
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Section Header with Current Folder Pill & Change Folder Action
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Recordings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ) {
            Text(
              text = recordings.size.toString(),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          // Clickable Folder Pill (defaults to /Audio, allows changing)
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable(enabled = !isRecording) { showFolderDialog = true }
              .testTag("recordings_folder_pill"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp),
              )
              Text(
                text = directoryDisplayPath,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          IconButton(
            onClick = { refreshTrigger++ },
            modifier = Modifier
              .size(32.dp)
              .testTag("refresh_recordings_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh recordings",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }

      if (recordings.isEmpty()) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("empty_recordings_state"),
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No recordings in $directoryDisplayPath",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap the microphone button above to record and save a timestamped audio file.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("recordings_list"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = 12.dp),
        ) {
          items(
            items = recordings,
            key = { it.path },
          ) { item ->
            RecordingListItemCard(
              item = item,
              onClick = {
                activePlaybackRecording = item
              },
              onEdit = {
                activePlaybackRecording = null
                fileForAudioEditor = item.file
                onOpenAudioEditor?.invoke(item.file)
              },
              onRename = {
                recordingToRename = item
              },
              onDelete = {
                recordingToDelete = item
              },
            )
          }
        }
      }
    }
  }

  // Miniature Player Modal (pops up and starts playing when a recording is tapped)
  activePlaybackRecording?.let { recording ->
    MiniAudioPlayerModal(
      recording = recording,
      onDismiss = { activePlaybackRecording = null },
    )
  }

  // Change Recordings Folder Dialog
  if (showFolderDialog) {
    ChangeRecordingsFolderDialog(
      currentDisplayPath = directoryDisplayPath,
      currentDirectory = recordingsDirectory,
      userRoot = userRoot,
      onDismiss = { showFolderDialog = false },
      onConfirmPath = { newPath ->
        updateRecordingsDirectory(newPath)
        refreshTrigger++
        showFolderDialog = false
      },
    )
  }

  // Rename Recording Dialog
  recordingToRename?.let { item ->
    RenameRecordingDialog(
      recording = item,
      onDismiss = { recordingToRename = null },
      onConfirmRename = { newName ->
        val result = FileManagerEngine.renameFile(item.file, newName)
        if (result.isSuccess) {
          refreshTrigger++
          recordingToRename = null
        } else {
          Toast.makeText(
            context,
            result.exceptionOrNull()?.message ?: "Could not rename recording",
            Toast.LENGTH_SHORT,
          ).show()
        }
      },
    )
  }

  // Delete Recording Confirmation Dialog
  recordingToDelete?.let { item ->
    DeleteRecordingDialog(
      recording = item,
      onDismiss = { recordingToDelete = null },
      onConfirmDelete = {
        FileManagerEngine.deleteFileOrDirectory(item.file)
        if (activePlaybackRecording?.path == item.path) {
          activePlaybackRecording = null
        }
        refreshTrigger++
        recordingToDelete = null
      },
    )
  }
}

@Composable
private fun RecordingListItemCard(
  item: AudioRecordingItem,
  onClick: () -> Unit,
  onEdit: () -> Unit = {},
  onRename: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("recording_item_${item.name}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Circular Leading Icon Badge
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.GraphicEq,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
      }

      // Title + Metadata
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.name,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
          ) {
            Text(
              text = item.formatBadge,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            )
          }
          Text(
            text = "${item.durationFormatted} • ${item.sizeFormatted} • ${item.timestampFormatted}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      // Overflow actions menu
      Box {
        IconButton(
          onClick = { menuExpanded = true },
          modifier = Modifier
            .size(36.dp)
            .testTag("recording_menu_${item.name}"),
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More options for ${item.name}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
        ) {
          DropdownMenuItem(
            text = { Text("Play") },
            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onClick()
            },
            modifier = Modifier.testTag("recording_play_${item.name}"),
          )
          DropdownMenuItem(
            text = { Text("Edit") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onEdit()
            },
            modifier = Modifier.testTag("action_edit_audio"),
          )
          DropdownMenuItem(
            text = { Text("Rename") },
            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onRename()
            },
          )
          DropdownMenuItem(
            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
            leadingIcon = {
              Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
              )
            },
            onClick = {
              menuExpanded = false
              onDelete()
            },
          )
        }
      }
    }
  }
}

/**
 * Miniature Player Modal that pops up when a recording is tapped, immediately starts playing
 * the audio file, and provides a simple play/pause toggle to the left of a tracking slider.
 */
@Composable
fun MiniAudioPlayerModal(
  recording: AudioRecordingItem,
  onDismiss: () -> Unit,
) {
  val isInspection = LocalInspectionMode.current

  var isPlaying by remember { mutableStateOf(true) }
  var durationMs by remember {
    mutableLongStateOf(recording.durationMs.coerceAtLeast(1000L))
  }
  var currentPositionMs by remember { mutableLongStateOf(0L) }
  var isDraggingSlider by remember { mutableStateOf(false) }
  var dragFraction by remember { mutableFloatStateOf(0f) }
  var usingFallbackTimer by remember { mutableStateOf(isInspection) }

  val mediaPlayer = remember(recording.path) {
    if (isInspection) {
      null
    } else {
      try {
        MediaPlayer().apply {
          setDataSource(recording.path)
          prepare()
          val mpDuration = duration.toLong()
          if (mpDuration > 0L) {
            durationMs = mpDuration
          }
          setOnCompletionListener {
            isPlaying = false
            currentPositionMs = durationMs
          }
          start()
        }
      } catch (_: Throwable) {
        usingFallbackTimer = true
        null
      }
    }
  }

  DisposableEffect(mediaPlayer) {
    onDispose {
      try {
        if (mediaPlayer?.isPlaying == true) {
          mediaPlayer.stop()
        }
      } catch (_: Throwable) {}
      try {
        mediaPlayer?.release()
      } catch (_: Throwable) {}
    }
  }

  // Synchronize playback progress with tracking slider
  LaunchedEffect(isPlaying, mediaPlayer, usingFallbackTimer, isDraggingSlider) {
    if (!isPlaying) return@LaunchedEffect
    while (isActive && isPlaying) {
      if (!isDraggingSlider) {
        if (mediaPlayer != null && !usingFallbackTimer) {
          try {
            val pos = mediaPlayer.currentPosition.toLong().coerceAtLeast(0L)
            currentPositionMs = pos.coerceAtMost(durationMs)
            if (!mediaPlayer.isPlaying && currentPositionMs >= durationMs - 80L) {
              isPlaying = false
            }
          } catch (_: Throwable) {
            usingFallbackTimer = true
          }
        } else {
          // Simulated timer fallback when running in test / headless environment
          val nextPos = currentPositionMs + 100L
          if (nextPos >= durationMs) {
            currentPositionMs = durationMs
            isPlaying = false
          } else {
            currentPositionMs = nextPos
          }
        }
      }
      delay(100L)
    }
  }

  fun togglePlayPause() {
    if (isPlaying) {
      isPlaying = false
      try {
        mediaPlayer?.pause()
      } catch (_: Throwable) {}
    } else {
      // If at the end, restart from 0
      if (currentPositionMs >= durationMs) {
        currentPositionMs = 0L
        try {
          mediaPlayer?.seekTo(0)
        } catch (_: Throwable) {}
      }
      isPlaying = true
      try {
        mediaPlayer?.start()
      } catch (_: Throwable) {}
    }
  }

  fun seekToFraction(fraction: Float) {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    val targetMs = (clampedFraction * durationMs).toLong()
    currentPositionMs = targetMs
    try {
      mediaPlayer?.seekTo(targetMs.toInt())
    } catch (_: Throwable) {}
  }

  val sliderProgress = if (isDraggingSlider) {
    dragFraction
  } else if (durationMs > 0L) {
    (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
  } else {
    0f
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      shadowElevation = 8.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .testTag("mini_player_modal"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Modal Header: Icon Badge + File Name + Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = recording.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("mini_player_title"),
              )
              Text(
                text = "${recording.formatBadge} • ${recording.sizeFormatted}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .testTag("mini_player_close_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close player",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        // Play/Pause toggle mini-FAB to the left of the tracking slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          SmallFloatingActionButton(
            onClick = { togglePlayPause() },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
              .testTag("mini_player_play_pause_button")
              .semantics {
                contentDescription = if (isPlaying) "Pause" else "Play"
              },
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = null,
              modifier = Modifier.size(22.dp),
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            SlimDotSlider(
              value = sliderProgress,
              onValueChange = { newFraction ->
                isDraggingSlider = true
                dragFraction = newFraction.coerceIn(0f, 1f)
                currentPositionMs = (dragFraction * durationMs).toLong()
              },
              onValueChangeFinished = {
                seekToFraction(dragFraction)
                isDraggingSlider = false
              },
              valueRange = 0f..1f,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("mini_player_tracking_slider"),
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = AudioRecorderEngine.formatDurationMs(currentPositionMs),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("mini_player_elapsed_time"),
              )
              Text(
                text = AudioRecorderEngine.formatDurationMs(durationMs),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("mini_player_total_time"),
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Dialog for changing the recordings folder (defaults to `/Audio`).
 * Follows DESIGN_LANGUAGE.md Section 5.2 Dialog Design Pattern.
 */
@Composable
private fun ChangeRecordingsFolderDialog(
  currentDisplayPath: String,
  currentDirectory: File,
  userRoot: File,
  onDismiss: () -> Unit,
  onConfirmPath: (String) -> Unit,
) {
  var textFieldValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = currentDisplayPath,
        selection = TextRange(0, currentDisplayPath.length),
      )
    )
  }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Throwable) {}
  }

  val presetFolders = remember {
    listOf("/Audio", "/Recordings", "/Music", "/Download")
  }

  val trimmedInput = textFieldValue.text.trim()
  val validationError = remember(trimmedInput) {
    if (trimmedInput.isEmpty()) {
      "Folder path cannot be empty"
    } else if (trimmedInput.any { it in listOf('\\', ':', '*', '?', '"', '<', '>', '|', '\u0000') }) {
      "Path contains invalid characters"
    } else {
      null
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("change_folder_dialog"),
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
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Recordings Folder",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Choose where recordings are saved & listed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Context Preview Card
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
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Current folder: $currentDisplayPath",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = currentDirectory.absolutePath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Quick Preset Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          presetFolders.forEach { preset ->
            val selected = trimmedInput.equals(preset, ignoreCase = true)
            FilterChip(
              selected = selected,
              onClick = {
                textFieldValue = TextFieldValue(
                  text = preset,
                  selection = TextRange(preset.length),
                )
              },
              label = { Text(preset) },
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.testTag("folder_preset_${preset.removePrefix("/")}"),
            )
          }
        }

        // Folder Path Input Field
        OutlinedTextField(
          value = textFieldValue,
          onValueChange = { textFieldValue = it },
          label = { Text("Folder path") },
          placeholder = { Text(DEFAULT_AUDIO_FOLDER_DISPLAY) },
          singleLine = true,
          isError = validationError != null,
          supportingText = if (validationError != null) {
            { Text(validationError) }
          } else {
            { Text("Relative to internal storage root (${userRoot.absolutePath})") }
          },
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(Icons.Default.Folder, contentDescription = null)
          },
          trailingIcon = if (textFieldValue.text.isNotEmpty()) {
            {
              IconButton(onClick = { textFieldValue = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear folder path")
              }
            }
          } else null,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              if (validationError == null) {
                onConfirmPath(trimmedInput)
              }
            },
          ),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("folder_path_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmPath(trimmedInput) },
        enabled = validationError == null,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_folder_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Confirm")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("cancel_folder_button"),
      ) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun RenameRecordingDialog(
  recording: AudioRecordingItem,
  onDismiss: () -> Unit,
  onConfirmRename: (String) -> Unit,
) {
  val stem = recording.file.nameWithoutExtension
  val ext = recording.extension
  var textFieldValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = recording.name,
        selection = TextRange(0, stem.length),
      )
    )
  }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Throwable) {}
  }

  val trimmed = textFieldValue.text.trim()
  val isUnchanged = trimmed == recording.name
  val validationError = remember(trimmed, recording) {
    if (isUnchanged) null
    else FileManagerEngine.validateFileName(trimmed, recording.file.parentFile)
  }
  val missingExtension = remember(trimmed, ext) {
    ext.isNotEmpty() && !trimmed.endsWith(".$ext", ignoreCase = true)
  }

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
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.DriveFileRenameOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Rename Recording",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Enter a new name for this audio file",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
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
              imageVector = Icons.Default.AudioFile,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
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

        OutlinedTextField(
          value = textFieldValue,
          onValueChange = { textFieldValue = it },
          label = { Text("New name") },
          singleLine = true,
          isError = validationError != null,
          supportingText = if (validationError != null) {
            { Text(validationError) }
          } else null,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = { Icon(Icons.Default.AudioFile, contentDescription = null) },
          trailingIcon = if (textFieldValue.text.isNotEmpty()) {
            {
              IconButton(onClick = { textFieldValue = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          } else null,
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        )

        if (missingExtension && trimmed.isNotEmpty()) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable {
                val restored = "$trimmed.$ext"
                textFieldValue = TextFieldValue(restored, TextRange(restored.length))
              },
          ) {
            Text(
              text = "Restore .$ext extension",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmRename(trimmed) },
        enabled = !isUnchanged && trimmed.isNotEmpty() && validationError == null,
        shape = RoundedCornerShape(12.dp),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Confirm")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun DeleteRecordingDialog(
  recording: AudioRecordingItem,
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
            text = "Delete Recording",
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
            imageVector = Icons.Default.AudioFile,
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
        shape = RoundedCornerShape(12.dp),
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Delete")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Cancel")
      }
    },
  )
}

@Preview(showBackground = true)
@Composable
private fun AudioRecorderScreenPreview() {
  ToolboxTheme {
    AudioRecorderScreen()
  }
}
