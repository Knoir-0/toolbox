package com.example.toolbox.ui.tools

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Simple Audio Editor tool screen.
 *
 * Shares audio, file, and UI infrastructure across the application and aligns its control layout
 * with [ImageEditorScreen] and [VideoEditorScreen]:
 * - Top bar with Undo, Redo, Close, and Export action.
 * - Full-height interactive waveform viewport (`weight(1f)`) with floating metadata/selection pills.
 * - Compact playback transport & quick-actions row (Play/Pause, Stop, Loop, Timecode, Share, Open, Reset).
 * - Mode-specific control surface (`RoundedCornerShape(16.dp)`) with single-row inline [SlimDotSlider]s
 *   and compact `CircleShape` action buttons.
 * - Bottom [SingleChoiceSegmentedButtonRow] mode selector (`Trim`, `Volume`, `Speed`).
 */
@Composable
fun AudioEditorScreen(
  modifier: Modifier = Modifier,
  initialBuffer: AudioSampleBuffer? = null,
  initialFile: File? = null,
  initialDirectory: File? = null,
  initialFileName: String = "sample_audio.wav",
  onNavigateBack: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val isInspectionMode = LocalInspectionMode.current
  val coroutineScope = rememberCoroutineScope()

  val audioDirectory = remember(initialDirectory, initialFile, isInspectionMode) {
    initialDirectory
      ?: initialFile?.parentFile
      ?: if (isInspectionMode) {
        File("/storage/emulated/0/Audio")
      } else {
        AudioRecorderEngine.getDefaultAudioDirectory(context)
      }
  }

  val resolvedInitialBuffer = remember(initialBuffer, initialFile) {
    initialBuffer ?: initialFile?.let { file ->
      AudioEditorEngine.readWavFile(file)
        ?: if (file.exists()) {
          AudioEditorEngine.createSampleAudioBuffer(durationMs = 2000L)
        } else {
          null
        }
    }
  }

  // Undo/redo history shared from ImageEditorEngine
  val history = remember { ImageEditorHistory<AudioEditSnapshot>(maxDepth = 15) }
  var historyVersion by remember { mutableIntStateOf(0) }

  var originalBuffer by remember(resolvedInitialBuffer) { mutableStateOf<AudioSampleBuffer?>(resolvedInitialBuffer) }
  var currentBuffer by remember(resolvedInitialBuffer) { mutableStateOf<AudioSampleBuffer?>(resolvedInitialBuffer) }
  var sourceFile by remember(initialFile) { mutableStateOf<File?>(initialFile) }
  var sourceFileName by remember(initialFile, initialFileName) {
    mutableStateOf(initialFile?.name ?: initialFileName)
  }
  var statusBannerText by remember { mutableStateOf<String?>(null) }
  var isBusy by remember { mutableStateOf(false) }

  // Selection & mode state aligned with ImageEditorScreen / VideoEditorScreen
  var activeMode by rememberSaveable { mutableStateOf(AudioEditorMode.TRIM) }
  var trimStartFraction by remember { mutableFloatStateOf(0f) }
  var trimEndFraction by remember { mutableFloatStateOf(1f) }

  // Volume & Fade controls state
  var volumeGainPercent by remember { mutableFloatStateOf(100f) }
  var fadeInSeconds by remember { mutableFloatStateOf(0f) }
  var fadeOutSeconds by remember { mutableFloatStateOf(0f) }

  // Speed & Reverse controls state
  var speedMultiplier by remember { mutableFloatStateOf(1.0f) }

  // Playback state
  var isPlaying by remember { mutableStateOf(false) }
  var loopPlayback by rememberSaveable { mutableStateOf(false) }
  var playheadFraction by remember { mutableFloatStateOf(0f) }
  var playbackJob by remember { mutableStateOf<Job?>(null) }
  var activeAudioTrack by remember { mutableStateOf<AudioTrack?>(null) }

  // Dialog states
  var showFilePickerDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showDiscardDialog by remember { mutableStateOf(false) }
  var pendingDiscardAction by remember { mutableStateOf<(() -> Unit)?>(null) }

  fun stopPlayback() {
    playbackJob?.cancel()
    playbackJob = null
    isPlaying = false
    try {
      activeAudioTrack?.pause()
      activeAudioTrack?.flush()
      activeAudioTrack?.release()
    } catch (_: Throwable) {}
    activeAudioTrack = null
  }

  DisposableEffect(Unit) {
    onDispose {
      stopPlayback()
    }
  }

  val canUndo = remember(historyVersion) { history.canUndo }
  val canRedo = remember(historyVersion) { history.canRedo }

  val hasTrimChanges = trimStartFraction > 0.005f || trimEndFraction < 0.995f
  val hasVolumeFadeChanges =
    abs(volumeGainPercent - 100f) > 0.5f || fadeInSeconds > 0.05f || fadeOutSeconds > 0.05f
  val hasSpeedChanges = abs(speedMultiplier - 1f) >= 0.02f
  val hasAnyChanges =
    canUndo ||
      currentBuffer != originalBuffer ||
      hasTrimChanges ||
      hasVolumeFadeChanges ||
      hasSpeedChanges

  fun performWithDiscardCheck(action: () -> Unit) {
    if (currentBuffer != null && hasAnyChanges) {
      pendingDiscardAction = action
      showDiscardDialog = true
    } else {
      action()
    }
  }

  fun loadBufferIntoEditor(
    buffer: AudioSampleBuffer,
    name: String,
    file: File? = null,
    banner: String? = null,
  ) {
    stopPlayback()
    history.clear()
    originalBuffer = buffer
    currentBuffer = buffer
    sourceFile = file
    sourceFileName = name
    trimStartFraction = 0f
    trimEndFraction = 1f
    playheadFraction = 0f
    volumeGainPercent = 100f
    fadeInSeconds = 0f
    fadeOutSeconds = 0f
    speedMultiplier = 1.0f
    statusBannerText = banner
    historyVersion++
  }

  fun commitEdit(
    newBuffer: AudioSampleBuffer,
    actionDescription: String,
    resetSelection: Boolean = true,
  ) {
    val prev = currentBuffer ?: return
    stopPlayback()
    history.push(
      AudioEditSnapshot(
        buffer = prev,
        trimStartFraction = trimStartFraction,
        trimEndFraction = trimEndFraction,
        actionLabel = actionDescription,
      )
    )
    currentBuffer = newBuffer
    if (resetSelection) {
      trimStartFraction = 0f
      trimEndFraction = 1f
      playheadFraction = 0f
    } else {
      playheadFraction = trimStartFraction
    }
    statusBannerText = actionDescription
    historyVersion++
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun openAudioFile(file: File) {
    coroutineScope.launch {
      stopPlayback()
      isBusy = true
      val result = AudioEditorEngine.loadAudioFile(context, file)
      isBusy = false
      result.onSuccess { buffer ->
        loadBufferIntoEditor(
          buffer = buffer,
          name = file.name,
          file = file,
          banner = "Loaded ${file.name} (${AudioRecorderEngine.formatDurationMs(buffer.durationMs)})",
        )
      }.onFailure { err ->
        Toast.makeText(
          context,
          err.message ?: "Unable to decode audio file",
          Toast.LENGTH_SHORT,
        ).show()
      }
    }
  }

  LaunchedEffect(initialFile) {
    if (initialBuffer == null && initialFile != null && initialFile.exists()) {
      val loaded = AudioEditorEngine.loadAudioFile(context, initialFile).getOrNull()
      if (loaded != null) {
        loadBufferIntoEditor(
          buffer = loaded,
          name = initialFile.name,
          file = initialFile,
          banner = "Loaded ${initialFile.name} (${AudioRecorderEngine.formatDurationMs(loaded.durationMs)})",
        )
      }
    }
  }

  BackHandler(enabled = onNavigateBack != null) {
    performWithDiscardCheck {
      stopPlayback()
      onNavigateBack?.invoke()
    }
  }

  val systemPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri: Uri? ->
    if (uri != null) {
      coroutineScope.launch {
        isBusy = true
        val copied = withContext(Dispatchers.IO) {
          copyUriToTempSourceFile(context, uri)
        }
        isBusy = false
        if (copied != null) {
          openAudioFile(copied)
        } else {
          Toast.makeText(context, "Could not read selected audio file", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  fun startPlayback() {
    val buffer = currentBuffer ?: return
    if (buffer.samples.isEmpty() || buffer.frameCount <= 0) return
    stopPlayback()

    val startFrac = if (playheadFraction >= trimStartFraction && playheadFraction < trimEndFraction - 0.01f) {
      playheadFraction
    } else {
      trimStartFraction
    }
    val endFrac = trimEndFraction.coerceIn(startFrac + 0.01f, 1f)

    isPlaying = true
    playheadFraction = startFrac

    playbackJob = coroutineScope.launch {
      val channels = buffer.safeChannels
      val totalFrames = buffer.frameCount
      var currentStartFrac = startFrac

      do {
        val startFrame = (currentStartFrac * totalFrames).roundToInt().coerceIn(0, totalFrames - 1)
        val endFrame = (endFrac * totalFrames).roundToInt().coerceIn(startFrame + 1, totalFrames)

        withContext(Dispatchers.IO) {
          var track: AudioTrack? = null
          try {
            if (!isInspectionMode) {
              val channelMask = if (channels == 2) {
                AudioFormat.CHANNEL_OUT_STEREO
              } else {
                AudioFormat.CHANNEL_OUT_MONO
              }
              val minBufBytes = AudioTrack.getMinBufferSize(
                buffer.sampleRate,
                channelMask,
                AudioFormat.ENCODING_PCM_16BIT,
              ).coerceAtLeast(4096)

              track = AudioTrack.Builder()
                .setAudioAttributes(
                  AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                )
                .setAudioFormat(
                  AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(buffer.sampleRate)
                    .setChannelMask(channelMask)
                    .build()
                )
                .setBufferSizeInBytes(minBufBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
              activeAudioTrack = track
              track.play()
            }

            val chunkFrames = (buffer.sampleRate / 25).coerceAtLeast(256) // ~40ms chunks
            var frameCursor = startFrame
            while (isActive && frameCursor < endFrame) {
              val framesToWrite = min(chunkFrames, endFrame - frameCursor)
              val sampleOffset = frameCursor * channels
              val sampleCount = framesToWrite * channels
              val written = track?.write(buffer.samples, sampleOffset, sampleCount) ?: sampleCount
              if (written <= 0) {
                delay(20L)
              }
              frameCursor += framesToWrite
              val progress = (frameCursor.toFloat() / totalFrames.toFloat()).coerceIn(0f, 1f)
              withContext(Dispatchers.Main) {
                playheadFraction = progress
              }
              if (track == null) {
                val chunkDurationMs = ((framesToWrite * 1000L) / buffer.sampleRate).coerceAtLeast(16L)
                delay(chunkDurationMs)
              }
            }
          } catch (_: Throwable) {
            // Clean fallback when AudioTrack is unavailable in unit/headless environments
          } finally {
            try {
              track?.stop()
              track?.release()
            } catch (_: Throwable) {}
            if (activeAudioTrack === track) {
              activeAudioTrack = null
            }
          }
        }
        currentStartFrac = trimStartFraction
      } while (isActive && loopPlayback)

      if (isActive) {
        isPlaying = false
        playheadFraction = trimStartFraction
      }
    }
  }

  val waveformPeaks = remember(currentBuffer) {
    currentBuffer?.let { AudioEditorEngine.extractWaveformPeaks(it, bucketCount = 96) } ?: emptyList()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("audio_editor_screen"),
  ) {
    val activeBuf = currentBuffer
    if (activeBuf == null) {
      OpenFileInitialView(
        onOpenFile = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          showFilePickerDialog = true
        },
        onUseSample = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          val sampleFile = FileManagerEngine.getOrCreateSampleAudioFile(context)
          val sample = AudioEditorEngine.readWavFile(sampleFile) ?: AudioEditorEngine.createSampleAudioBuffer()
          loadBufferIntoEditor(
            buffer = sample,
            name = sampleFile.name,
            file = sampleFile,
            banner = "Loaded synthesized C-Major arpeggio sample",
          )
        },
        sampleButtonText = "or try sample audio",
        sampleButtonIcon = Icons.Default.AudioFile,
        sampleButtonTestTag = "audio_editor_sample_button",
        openButtonTestTag = "open_audio_button",
        isLoading = isBusy,
      )
    } else {
      Column(
        modifier = Modifier.fillMaxSize(),
      ) {
        // 1. Top Header Bar aligned with VideoEditorTopBar & DESIGN_LANGUAGE.md Section 5.1
        AudioEditorTopBar(
          buffer = activeBuf,
          sourceFileName = sourceFileName,
          canUndo = canUndo && !isBusy,
          canRedo = canRedo && !isBusy,
          isBusy = isBusy,
          onNavigateBack = onNavigateBack?.let { navigateBack ->
            {
              performWithDiscardCheck {
                stopPlayback()
                navigateBack()
              }
            }
          },
          onUndo = {
            val active = currentBuffer ?: return@AudioEditorTopBar
            stopPlayback()
            val prev = history.undo(
              AudioEditSnapshot(
                buffer = active,
                trimStartFraction = trimStartFraction,
                trimEndFraction = trimEndFraction,
              )
            )
            if (prev != null) {
              currentBuffer = prev.buffer
              trimStartFraction = prev.trimStartFraction
              trimEndFraction = prev.trimEndFraction
              playheadFraction = prev.trimStartFraction
              statusBannerText = "Undid edit"
              historyVersion++
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
          },
          onRedo = {
            val active = currentBuffer ?: return@AudioEditorTopBar
            stopPlayback()
            val next = history.redo(
              AudioEditSnapshot(
                buffer = active,
                trimStartFraction = trimStartFraction,
                trimEndFraction = trimEndFraction,
              )
            )
            if (next != null) {
              currentBuffer = next.buffer
              trimStartFraction = next.trimStartFraction
              trimEndFraction = next.trimEndFraction
              playheadFraction = next.trimStartFraction
              statusBannerText = "Redid edit"
              historyVersion++
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
          },
          onClose = {
            performWithDiscardCheck {
              stopPlayback()
              currentBuffer = null
              originalBuffer = null
              sourceFile = null
              history.clear()
              statusBannerText = null
              historyVersion++
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
          },
          onExportClick = {
            stopPlayback()
            showExportDialog = true
          },
        )

        // 2. Main Non-Scrolling Editor Workspace (aligned with ImageEditorScreen & VideoEditorScreen)
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
          // 2a. Interactive Waveform Preview Viewport (takes weight(1f) like VideoPreviewViewport & ImageCanvas)
          WaveformPreviewViewport(
            buffer = activeBuf,
            peaks = waveformPeaks,
            trimStartFraction = trimStartFraction,
            trimEndFraction = trimEndFraction,
            playheadFraction = playheadFraction,
            volumeGainPercent = volumeGainPercent,
            fadeInSeconds = fadeInSeconds,
            fadeOutSeconds = fadeOutSeconds,
            statusBannerText = statusBannerText,
            onSelectionChange = { newStart, newEnd ->
              val startMoved = abs(newStart - trimStartFraction) > 1e-4f
              val (updatedStart, updatedEnd) = AudioEditorEngine.updateSelectionFromHandles(
                currentStart = trimStartFraction,
                currentEnd = trimEndFraction,
                draggedStart = newStart,
                draggedEnd = newEnd,
              )
              trimStartFraction = updatedStart
              trimEndFraction = updatedEnd
              if (!isPlaying) {
                playheadFraction = if (startMoved) {
                  updatedStart
                } else {
                  playheadFraction.coerceIn(updatedStart, updatedEnd)
                }
              }
            },
            onScrub = { fraction ->
              if (isPlaying) stopPlayback()
              playheadFraction = fraction.coerceIn(0f, 1f)
            },
            onResetSelection = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              trimStartFraction = 0f
              trimEndFraction = 1f
              playheadFraction = 0f
            },
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
          )

          Spacer(modifier = Modifier.height(6.dp))

          // 2b. Compact Playback Transport & Quick Actions Row (aligned with VideoTransportRow)
          AudioTransportRow(
            buffer = activeBuf,
            isPlaying = isPlaying,
            loopPlayback = loopPlayback,
            playheadFraction = playheadFraction,
            trimStartFraction = trimStartFraction,
            trimEndFraction = trimEndFraction,
            speedMultiplier = speedMultiplier,
            hasAnyChanges = hasAnyChanges,
            isBusy = isBusy,
            onTogglePlayPause = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              if (isPlaying) stopPlayback() else startPlayback()
            },
            onStopRewind = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              stopPlayback()
              playheadFraction = trimStartFraction
            },
            onToggleLoop = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              loopPlayback = !loopPlayback
            },
            onShareAudio = {
              stopPlayback()
              coroutineScope.launch {
                isBusy = true
                val effectiveBuf = withContext(Dispatchers.Default) {
                  AudioEditorEngine.buildEffectiveOutputBuffer(
                    buffer = activeBuf,
                    trimStartFraction = trimStartFraction,
                    trimEndFraction = trimEndFraction,
                    volumeGainPercent = volumeGainPercent,
                    fadeInSeconds = fadeInSeconds,
                    fadeOutSeconds = fadeOutSeconds,
                    speedMultiplier = speedMultiplier,
                  )
                }
                val tempShareFile = File(
                  context.cacheDir,
                  "${FileConverterEngine.extractFileStem(sourceFileName)}_edited.wav",
                )
                val ok = withContext(Dispatchers.IO) {
                  AudioEditorEngine.writeWavFile(effectiveBuf, tempShareFile)
                }
                isBusy = false
                if (ok) {
                  shareConvertedFile(context, tempShareFile, "audio/wav")
                } else {
                  Toast.makeText(context, "Could not prepare audio for sharing", Toast.LENGTH_SHORT).show()
                }
              }
            },
            onOpenAudio = {
              performWithDiscardCheck {
                stopPlayback()
                showFilePickerDialog = true
              }
            },
            onResetAll = {
              val orig = originalBuffer ?: return@AudioTransportRow
              volumeGainPercent = 100f
              fadeInSeconds = 0f
              fadeOutSeconds = 0f
              speedMultiplier = 1.0f
              commitEdit(orig, "Reset to original audio")
            },
          )

          Spacer(modifier = Modifier.height(6.dp))

          // 2c. Mode-Specific Controls Card (aligned with ImageEditorScreen & VideoEditorScreen)
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
              when (activeMode) {
                AudioEditorMode.TRIM -> {
                  AudioTrimControls(
                    buffer = activeBuf,
                    trimStartFraction = trimStartFraction,
                    trimEndFraction = trimEndFraction,
                    playheadFraction = playheadFraction,
                    onTrimChange = { newStart, newEnd ->
                      if (isPlaying) stopPlayback()
                      val (updatedStart, updatedEnd) = AudioEditorEngine.updateSelectionFromHandles(
                        currentStart = trimStartFraction,
                        currentEnd = trimEndFraction,
                        draggedStart = newStart,
                        draggedEnd = newEnd,
                      )
                      trimStartFraction = updatedStart
                      trimEndFraction = updatedEnd
                      playheadFraction = playheadFraction.coerceIn(updatedStart, updatedEnd)
                    },
                    onSetStartAtPlayhead = {
                      val maxStart = (trimEndFraction - AudioEditorEngine.MIN_TRIM_SPAN_FRACTION).coerceAtLeast(0f)
                      trimStartFraction = playheadFraction.coerceIn(0f, maxStart)
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onSetEndAtPlayhead = {
                      val minEnd = (trimStartFraction + AudioEditorEngine.MIN_TRIM_SPAN_FRACTION).coerceAtMost(1f)
                      trimEndFraction = playheadFraction.coerceIn(minEnd, 1f)
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onResetSelection = {
                      trimStartFraction = 0f
                      trimEndFraction = 1f
                      playheadFraction = 0f
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onTrimKeepSelection = {
                      val trimmed = AudioEditorEngine.trim(activeBuf, trimStartFraction, trimEndFraction)
                      commitEdit(
                        newBuffer = trimmed,
                        actionDescription = "Trimmed to ${AudioRecorderEngine.formatDurationMs(trimmed.durationMs)}",
                      )
                    },
                    onCutRemoveSelection = {
                      val cut = AudioEditorEngine.cutSelection(activeBuf, trimStartFraction, trimEndFraction)
                      commitEdit(
                        newBuffer = cut,
                        actionDescription = "Cut selection (${AudioRecorderEngine.formatDurationMs(cut.durationMs)} remaining)",
                      )
                    },
                  )
                }

                AudioEditorMode.VOLUME_FADE -> {
                  VolumeAndFadeControls(
                    buffer = activeBuf,
                    trimStartFraction = trimStartFraction,
                    trimEndFraction = trimEndFraction,
                    volumeGainPercent = volumeGainPercent,
                    fadeInSeconds = fadeInSeconds,
                    fadeOutSeconds = fadeOutSeconds,
                    onVolumeGainChange = { volumeGainPercent = it },
                    onFadeInChange = { fadeInSeconds = it },
                    onFadeOutChange = { fadeOutSeconds = it },
                    onResetVolumeAndFade = {
                      volumeGainPercent = 100f
                      fadeInSeconds = 0f
                      fadeOutSeconds = 0f
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onApplyVolumeAndFade = {
                      var updated = activeBuf
                      val gainMultiplier = volumeGainPercent / 100f
                      if (abs(gainMultiplier - 1f) > 0.01f) {
                        updated = AudioEditorEngine.adjustVolume(
                          buffer = updated,
                          gainMultiplier = gainMultiplier,
                          startFraction = trimStartFraction,
                          endFraction = trimEndFraction,
                        )
                      }
                      val fadeInMs = (fadeInSeconds * 1000f).roundToInt().toLong()
                      val fadeOutMs = (fadeOutSeconds * 1000f).roundToInt().toLong()
                      if (fadeInMs > 0L || fadeOutMs > 0L) {
                        updated = AudioEditorEngine.applyFade(
                          buffer = updated,
                          fadeInMs = fadeInMs,
                          fadeOutMs = fadeOutMs,
                          startFraction = trimStartFraction,
                          endFraction = trimEndFraction,
                        )
                      }
                      volumeGainPercent = 100f
                      fadeInSeconds = 0f
                      fadeOutSeconds = 0f
                      commitEdit(
                        newBuffer = updated,
                        actionDescription = "Applied volume & fade adjustments",
                        resetSelection = false,
                      )
                    },
                    onNormalize = {
                      val normalized = AudioEditorEngine.normalize(
                        buffer = activeBuf,
                        targetPeakFraction = 0.95f,
                        startFraction = trimStartFraction,
                        endFraction = trimEndFraction,
                      )
                      commitEdit(
                        newBuffer = normalized,
                        actionDescription = "Normalized peak amplitude to 95%",
                        resetSelection = false,
                      )
                    },
                    onMuteSelection = {
                      val muted = AudioEditorEngine.adjustVolume(
                        buffer = activeBuf,
                        gainMultiplier = 0f,
                        startFraction = trimStartFraction,
                        endFraction = trimEndFraction,
                      )
                      commitEdit(
                        newBuffer = muted,
                        actionDescription = "Muted selected range",
                        resetSelection = false,
                      )
                    },
                  )
                }

                AudioEditorMode.SPEED_EFFECTS -> {
                  SpeedAndReverseControls(
                    speedMultiplier = speedMultiplier,
                    onSpeedChange = { speedMultiplier = it },
                    onResetSpeed = {
                      speedMultiplier = 1.0f
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onApplySpeed = {
                      val resampled = AudioEditorEngine.changeSpeed(activeBuf, speedMultiplier)
                      val formattedSpeed = String.format(Locale.US, "%.2fx", speedMultiplier)
                      commitEdit(
                        newBuffer = resampled,
                        actionDescription = "Changed speed to $formattedSpeed (${AudioRecorderEngine.formatDurationMs(resampled.durationMs)})",
                      )
                      speedMultiplier = 1.0f
                    },
                    onReverseSelection = {
                      val reversed = AudioEditorEngine.reverse(
                        buffer = activeBuf,
                        startFraction = trimStartFraction,
                        endFraction = trimEndFraction,
                      )
                      val scopeLabel = if (trimStartFraction <= 0.01f && trimEndFraction >= 0.99f) {
                        "Reversed full track"
                      } else {
                        "Reversed selected range"
                      }
                      commitEdit(
                        newBuffer = reversed,
                        actionDescription = scopeLabel,
                        resetSelection = false,
                      )
                    },
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // 2d. Bottom Mode Selector Row (aligned with ModeSelectorRow & VideoModeSelectorRow)
          AudioEditorModeSelector(
            activeMode = activeMode,
            hasTrimEdits = hasTrimChanges,
            hasVolumeFadeEdits = hasVolumeFadeChanges,
            hasSpeedEdits = hasSpeedChanges,
            onSelectMode = { mode ->
              activeMode = mode
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
          )
        }
      }
    }
  }

  // Shared StorageFilePickerDialog from FileConverterScreen
  if (showFilePickerDialog) {
    StorageFilePickerDialog(
      initialDirectory = audioDirectory,
      title = "Select Audio File",
      fileFilter = { file -> AudioEditorEngine.isSupportedAudioFile(file) },
      onDismiss = { showFilePickerDialog = false },
      onLaunchSystemPicker = {
        showFilePickerDialog = false
        systemPickerLauncher.launch("audio/*")
      },
      onFileSelected = { selectedFile ->
        showFilePickerDialog = false
        openAudioFile(selectedFile)
      },
    )
  }

  // Unsaved Changes Confirmation Dialog (aligned with ImageEditorScreen & VideoEditorScreen)
  if (showDiscardDialog) {
    AlertDialog(
      onDismissRequest = { showDiscardDialog = false },
      shape = RoundedCornerShape(28.dp),
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      icon = {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier.size(44.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(22.dp),
            )
          }
        }
      },
      title = {
        Text(
          text = "Discard Audio Edits?",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Text(
          text = "You have unsaved edits on this audio track. Are you sure you want to discard them?",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showDiscardDialog = false
            pendingDiscardAction?.invoke()
            pendingDiscardAction = null
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("confirm_discard_button"),
        ) {
          Text("Discard")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showDiscardDialog = false
            pendingDiscardAction = null
          },
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("Keep Editing")
        }
      },
    )
  }

  // Save / Export Audio Dialog following DESIGN_LANGUAGE.md Section 5.2
  val exportBuf = currentBuffer
  if (showExportDialog && exportBuf != null) {
    val previewEffectiveBuffer = remember(
      exportBuf,
      trimStartFraction,
      trimEndFraction,
      volumeGainPercent,
      fadeInSeconds,
      fadeOutSeconds,
      speedMultiplier,
    ) {
      AudioEditorEngine.buildEffectiveOutputBuffer(
        buffer = exportBuf,
        trimStartFraction = trimStartFraction,
        trimEndFraction = trimEndFraction,
        volumeGainPercent = volumeGainPercent,
        fadeInSeconds = fadeInSeconds,
        fadeOutSeconds = fadeOutSeconds,
        speedMultiplier = speedMultiplier,
      )
    }

    AudioExportDialog(
      buffer = previewEffectiveBuffer,
      initialFileName = sourceFileName,
      originalSourceFile = sourceFile,
      outputDirectory = audioDirectory,
      isExporting = isBusy,
      onDismiss = { if (!isBusy) showExportDialog = false },
      onConfirmExport = { targetFileNameStem, exportFormat, overwriteOriginal ->
        coroutineScope.launch {
          isBusy = true
          val targetFile = if (overwriteOriginal && sourceFile != null) {
            sourceFile!!
          } else {
            val sanitizedStem = targetFileNameStem.trim().ifEmpty { "edited_audio" }
            File(audioDirectory, "$sanitizedStem.${exportFormat.extension}")
          }
          val resolvedFormat = if (overwriteOriginal && sourceFile != null) {
            AudioExportFormat.fromFileName(sourceFile!!.name)
          } else {
            exportFormat
          }
          val result = AudioEditorEngine.exportAudioBuffer(
            context = context,
            buffer = previewEffectiveBuffer,
            outputFile = targetFile,
            format = resolvedFormat,
            overwriteExisting = overwriteOriginal && sourceFile != null,
          )
          isBusy = false
          result.onSuccess { savedFile ->
            showExportDialog = false
            // Commit any pending slider/handle adjustments into the editor state
            if (previewEffectiveBuffer != exportBuf) {
              history.push(
                AudioEditSnapshot(
                  buffer = exportBuf,
                  trimStartFraction = trimStartFraction,
                  trimEndFraction = trimEndFraction,
                  actionLabel = "Saved edits",
                )
              )
              currentBuffer = previewEffectiveBuffer
              trimStartFraction = 0f
              trimEndFraction = 1f
              playheadFraction = 0f
              volumeGainPercent = 100f
              fadeInSeconds = 0f
              fadeOutSeconds = 0f
              speedMultiplier = 1.0f
              historyVersion++
            }
            sourceFile = savedFile
            sourceFileName = savedFile.name
            statusBannerText = if (overwriteOriginal) {
              "Saved edits directly to ${savedFile.name}"
            } else {
              "Saved ${savedFile.name} to /Audio"
            }
            Toast.makeText(
              context,
              "Saved ${savedFile.name}",
              Toast.LENGTH_SHORT,
            ).show()
          }.onFailure { err ->
            Toast.makeText(
              context,
              err.message ?: "Save failed",
              Toast.LENGTH_SHORT,
            ).show()
          }
        }
      },
    )
  }
}

// ---------------------------------------------------------------------------
// Top Header Bar (aligned with VideoEditorTopBar)
// ---------------------------------------------------------------------------

@Composable
private fun AudioEditorTopBar(
  buffer: AudioSampleBuffer,
  sourceFileName: String,
  canUndo: Boolean,
  canRedo: Boolean,
  isBusy: Boolean,
  onNavigateBack: (() -> Unit)? = null,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onClose: () -> Unit,
  onExportClick: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 68.dp)
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f),
      ) {
        if (onNavigateBack != null) {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_navigate_back"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        } else {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Audio Editor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          val chLabel = if (buffer.safeChannels == 2) "Stereo" else "Mono"
          val subtitleText =
            "$sourceFileName • ${AudioRecorderEngine.formatDurationMs(buffer.durationMs)} • ${buffer.sampleRate / 1000.0}kHz $chLabel"
          Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        IconButton(
          onClick = onUndo,
          enabled = canUndo,
          modifier = Modifier
            .size(38.dp)
            .testTag("audio_editor_undo_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Undo,
            contentDescription = "Undo",
            modifier = Modifier.size(20.dp),
          )
        }
        IconButton(
          onClick = onRedo,
          enabled = canRedo,
          modifier = Modifier
            .size(38.dp)
            .testTag("audio_editor_redo_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Redo,
            contentDescription = "Redo",
            modifier = Modifier.size(20.dp),
          )
        }
        IconButton(
          onClick = onClose,
          enabled = !isBusy,
          modifier = Modifier
            .size(38.dp)
            .testTag("audio_editor_close_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close audio",
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        FilledIconButton(
          onClick = onExportClick,
          enabled = !isBusy,
          shape = CircleShape,
          modifier = Modifier
            .size(36.dp)
            .testTag("audio_editor_export_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Save,
            contentDescription = "Save audio",
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Interactive Waveform Stage Viewport (aligned with VideoPreviewViewport)
// ---------------------------------------------------------------------------

@Composable
private fun WaveformPreviewViewport(
  buffer: AudioSampleBuffer,
  peaks: List<Float>,
  trimStartFraction: Float,
  trimEndFraction: Float,
  playheadFraction: Float,
  volumeGainPercent: Float,
  fadeInSeconds: Float,
  fadeOutSeconds: Float,
  statusBannerText: String?,
  onSelectionChange: (Float, Float) -> Unit,
  onScrub: (Float) -> Unit,
  onResetSelection: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val inactiveBarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)
  val selectionTint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f)
  val dimMaskColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)
  val outlineColor = MaterialTheme.colorScheme.outlineVariant

  val startMs = (trimStartFraction * buffer.durationMs).roundToInt().toLong()
  val endMs = (trimEndFraction * buffer.durationMs).roundToInt().toLong()
  val selectedDurationMs = (endMs - startMs).coerceAtLeast(0L)
  val hasSubSelection = trimStartFraction > 0.005f || trimEndFraction < 0.995f
  val peakLevel = remember(buffer, trimStartFraction, trimEndFraction) {
    (AudioEditorEngine.calculatePeakAmplitude(buffer, trimStartFraction, trimEndFraction) * 100f).roundToInt()
  }

  val currentTrimStart by rememberUpdatedState(trimStartFraction)
  val currentTrimEnd by rememberUpdatedState(trimEndFraction)
  val currentOnSelectionChange by rememberUpdatedState(onSelectionChange)
  val currentOnScrub by rememberUpdatedState(onScrub)

  var isDraggingStartHandle by remember { mutableStateOf(false) }
  var isDraggingEndHandle by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier.testTag("audio_waveform_viewport"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
    ) {
      // Interactive Waveform Canvas supporting simultaneous multi-touch start & end handle dragging
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f))
          .testTag("audio_editor_waveform_canvas")
          .pointerInput(Unit) {
            awaitEachGesture {
              val pointerTargets = mutableMapOf<PointerId, WaveformDragTarget>()
              val firstDown = awaitFirstDown(requireUnconsumed = false)
              val initialWidth = size.width.toFloat().coerceAtLeast(1f)
              val initialHitRadius = (32.dp.toPx() / initialWidth).coerceIn(0.06f, 0.16f)
              val firstFrac = (firstDown.position.x / initialWidth).coerceIn(0f, 1f)

              val firstTarget = AudioEditorEngine.resolveWaveformHandleTarget(
                touchFraction = firstFrac,
                startFraction = currentTrimStart,
                endFraction = currentTrimEnd,
                handleHitRadiusFraction = initialHitRadius,
              )
              pointerTargets[firstDown.id] = firstTarget
              isDraggingStartHandle = pointerTargets.containsValue(WaveformDragTarget.START_HANDLE)
              isDraggingEndHandle = pointerTargets.containsValue(WaveformDragTarget.END_HANDLE)

              if (firstTarget == WaveformDragTarget.PLAYHEAD) {
                currentOnScrub(firstFrac)
              }

              do {
                val event = awaitPointerEvent()
                val w = size.width.toFloat().coerceAtLeast(1f)
                val hitRadius = (32.dp.toPx() / w).coerceIn(0.06f, 0.16f)
                val activeChanges = event.changes.filter { it.pressed }

                for (change in activeChanges) {
                  if (!pointerTargets.containsKey(change.id)) {
                    val touchFrac = (change.position.x / w).coerceIn(0f, 1f)
                    val alreadyClaimed = when {
                      pointerTargets.containsValue(WaveformDragTarget.START_HANDLE) &&
                        !pointerTargets.containsValue(WaveformDragTarget.END_HANDLE) ->
                        WaveformDragTarget.START_HANDLE
                      pointerTargets.containsValue(WaveformDragTarget.END_HANDLE) &&
                        !pointerTargets.containsValue(WaveformDragTarget.START_HANDLE) ->
                        WaveformDragTarget.END_HANDLE
                      else -> WaveformDragTarget.NONE
                    }

                    if (alreadyClaimed == WaveformDragTarget.NONE &&
                      pointerTargets.values.any { it == WaveformDragTarget.PLAYHEAD } &&
                      activeChanges.size >= 2
                    ) {
                      val sortedByX = activeChanges.sortedBy { it.position.x }
                      pointerTargets[sortedByX.first().id] = WaveformDragTarget.START_HANDLE
                      pointerTargets[sortedByX.last().id] = WaveformDragTarget.END_HANDLE
                    } else {
                      pointerTargets[change.id] = AudioEditorEngine.resolveWaveformHandleTarget(
                        touchFraction = touchFrac,
                        startFraction = currentTrimStart,
                        endFraction = currentTrimEnd,
                        handleHitRadiusFraction = hitRadius,
                        alreadyClaimedTarget = alreadyClaimed,
                      )
                    }
                  }
                }

                val pressedIds = activeChanges.map { it.id }.toSet()
                pointerTargets.keys.retainAll(pressedIds)

                isDraggingStartHandle = pointerTargets.containsValue(WaveformDragTarget.START_HANDLE)
                isDraggingEndHandle = pointerTargets.containsValue(WaveformDragTarget.END_HANDLE)

                var draggedStartFrac: Float? = null
                var draggedEndFrac: Float? = null
                var scrubbedPlayheadFrac: Float? = null

                for (change in activeChanges) {
                  val frac = (change.position.x / w).coerceIn(0f, 1f)
                  when (pointerTargets[change.id]) {
                    WaveformDragTarget.START_HANDLE -> {
                      draggedStartFrac = frac
                      change.consume()
                    }
                    WaveformDragTarget.END_HANDLE -> {
                      draggedEndFrac = frac
                      change.consume()
                    }
                    WaveformDragTarget.PLAYHEAD -> {
                      scrubbedPlayheadFrac = frac
                      change.consume()
                    }
                    WaveformDragTarget.NONE, null -> {}
                  }
                }

                if (draggedStartFrac != null || draggedEndFrac != null) {
                  val (newStart, newEnd) = AudioEditorEngine.updateSelectionFromHandles(
                    currentStart = currentTrimStart,
                    currentEnd = currentTrimEnd,
                    draggedStart = draggedStartFrac,
                    draggedEnd = draggedEndFrac,
                  )
                  currentOnSelectionChange(newStart, newEnd)
                } else if (scrubbedPlayheadFrac != null) {
                  currentOnScrub(scrubbedPlayheadFrac)
                }
              } while (event.changes.any { it.pressed })

              isDraggingStartHandle = false
              isDraggingEndHandle = false
            }
          },
      ) {
        val totalDurationSec = (buffer.durationMs / 1000f).coerceAtLeast(0.01f)
        val fadeInSpanFrac = (fadeInSeconds / totalDurationSec).coerceAtLeast(0f)
        val fadeOutSpanFrac = (fadeOutSeconds / totalDurationSec).coerceAtLeast(0f)
        val gainMultiplier = (volumeGainPercent / 100f).coerceIn(0f, 2.5f)

        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val centerY = h / 2f
          val startX = trimStartFraction.coerceIn(0f, 1f) * w
          val endX = trimEndFraction.coerceIn(0f, 1f) * w
          val playheadX = playheadFraction.coerceIn(0f, 1f) * w

          // Selection background highlight
          drawRect(
            color = selectionTint,
            topLeft = Offset(startX, 0f),
            size = Size((endX - startX).coerceAtLeast(0f), h),
          )

          // Center zero-crossing reference line
          drawLine(
            color = outlineColor.copy(alpha = 0.45f),
            start = Offset(0f, centerY),
            end = Offset(w, centerY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
          )

          // Waveform amplitude bars with live preview of gain & fade envelope
          if (peaks.isNotEmpty()) {
            val count = peaks.size
            val slotWidth = w / count
            val barWidth = (slotWidth * 0.62f).coerceAtLeast(2.dp.toPx())
            val maxHalfHeight = (h * 0.40f)

            for (i in 0 until count) {
              val barCenterX = (i + 0.5f) * slotWidth
              val barFrac = barCenterX / w
              val inSelection = barFrac in trimStartFraction..trimEndFraction

              var envelope = 1f
              if (inSelection) {
                envelope *= gainMultiplier
                if (fadeInSpanFrac > 0.001f && barFrac < trimStartFraction + fadeInSpanFrac) {
                  val f = ((barFrac - trimStartFraction) / fadeInSpanFrac).coerceIn(0f, 1f)
                  envelope *= f
                }
                if (fadeOutSpanFrac > 0.001f && barFrac > trimEndFraction - fadeOutSpanFrac) {
                  val f = ((trimEndFraction - barFrac) / fadeOutSpanFrac).coerceIn(0f, 1f)
                  envelope *= f
                }
              }

              val peak = (peaks[i] * envelope).coerceIn(0.03f, 1f)
              val halfBar = (peak * maxHalfHeight).coerceAtLeast(2.dp.toPx())

              drawRoundRect(
                color = if (inSelection) primaryColor else inactiveBarColor,
                topLeft = Offset(barCenterX - barWidth / 2f, centerY - halfBar),
                size = Size(barWidth, halfBar * 2f),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
              )
            }
          }

          // Dim outside selection
          if (startX > 0f) {
            drawRect(
              color = dimMaskColor,
              topLeft = Offset(0f, 0f),
              size = Size(startX, h),
            )
          }
          if (endX < w) {
            drawRect(
              color = dimMaskColor,
              topLeft = Offset(endX, 0f),
              size = Size(w - endX, h),
            )
          }

          // Trim window border & side handles
          val startStroke = if (isDraggingStartHandle) 3.5.dp.toPx() else 2.5.dp.toPx()
          val endStroke = if (isDraggingEndHandle) 3.5.dp.toPx() else 2.5.dp.toPx()
          val startBarWidth = if (isDraggingStartHandle) 10.dp.toPx() else 8.dp.toPx()
          val endBarWidth = if (isDraggingEndHandle) 10.dp.toPx() else 8.dp.toPx()
          val startBarHeight = if (isDraggingStartHandle) 44.dp.toPx() else 36.dp.toPx()
          val endBarHeight = if (isDraggingEndHandle) 44.dp.toPx() else 36.dp.toPx()
          val startHalfW = startBarWidth / 2f
          val endHalfW = endBarWidth / 2f
          val startHandleCenterX = startX.coerceIn(startHalfW, (w - startHalfW).coerceAtLeast(startHalfW))
          val endHandleCenterX = endX.coerceIn(endHalfW, (w - endHalfW).coerceAtLeast(endHalfW))
          val gripHalfHeight = 7.dp.toPx()
          val gripStroke = 1.5.dp.toPx()

          // Start boundary & grip handle
          drawLine(
            color = primaryColor,
            start = Offset(startX, 0f),
            end = Offset(startX, h),
            strokeWidth = startStroke,
          )
          if (isDraggingStartHandle) {
            val haloW = 16.dp.toPx()
            val haloH = 52.dp.toPx()
            drawRoundRect(
              color = primaryColor.copy(alpha = 0.25f),
              topLeft = Offset(startHandleCenterX - haloW / 2f, centerY - haloH / 2f),
              size = Size(haloW, haloH),
              cornerRadius = CornerRadius(haloW / 2f, haloW / 2f),
            )
          }
          drawRoundRect(
            color = primaryColor,
            topLeft = Offset(startHandleCenterX - startHalfW, centerY - startBarHeight / 2f),
            size = Size(startBarWidth, startBarHeight),
            cornerRadius = CornerRadius(startHalfW, startHalfW),
          )
          drawLine(
            color = onPrimaryColor.copy(alpha = 0.85f),
            start = Offset(startHandleCenterX, centerY - gripHalfHeight),
            end = Offset(startHandleCenterX, centerY + gripHalfHeight),
            strokeWidth = gripStroke,
          )

          // End boundary & grip handle
          drawLine(
            color = primaryColor,
            start = Offset(endX, 0f),
            end = Offset(endX, h),
            strokeWidth = endStroke,
          )
          if (isDraggingEndHandle) {
            val haloW = 16.dp.toPx()
            val haloH = 52.dp.toPx()
            drawRoundRect(
              color = primaryColor.copy(alpha = 0.25f),
              topLeft = Offset(endHandleCenterX - haloW / 2f, centerY - haloH / 2f),
              size = Size(haloW, haloH),
              cornerRadius = CornerRadius(haloW / 2f, haloW / 2f),
            )
          }
          drawRoundRect(
            color = primaryColor,
            topLeft = Offset(endHandleCenterX - endHalfW, centerY - endBarHeight / 2f),
            size = Size(endBarWidth, endBarHeight),
            cornerRadius = CornerRadius(endHalfW, endHalfW),
          )
          drawLine(
            color = onPrimaryColor.copy(alpha = 0.85f),
            start = Offset(endHandleCenterX, centerY - gripHalfHeight),
            end = Offset(endHandleCenterX, centerY + gripHalfHeight),
            strokeWidth = gripStroke,
          )

          // Playhead vertical line + top pill indicator (matching VideoFilmstripTimeline)
          drawLine(
            color = tertiaryColor,
            start = Offset(playheadX, 0f),
            end = Offset(playheadX, h),
            strokeWidth = 2.5.dp.toPx(),
          )
          drawCircle(
            color = tertiaryColor,
            radius = 5.dp.toPx(),
            center = Offset(playheadX, centerY),
            style = Stroke(width = 2.dp.toPx()),
          )
        }
      }

      // Top-left selection & peak badge (aligned with VideoPreviewViewport's strategy_badge)
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(6.dp),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
          Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(13.dp),
          )
          Text(
            text = "${AudioRecorderEngine.formatDurationMs(startMs)} – ${AudioRecorderEngine.formatDurationMs(endMs)} (${AudioRecorderEngine.formatDurationMs(selectedDurationMs)}) • Peak $peakLevel%",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      // Top-right Select All reset pill when a sub-range is selected (aligned with ImageEditorScreen's zoom reset pill)
      if (hasSubSelection) {
        Surface(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .clip(CircleShape)
            .clickable(onClick = onResetSelection),
          color = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          shadowElevation = 2.dp,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Select All",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      // Bottom status toast pill inside viewport when an edit is applied
      if (!statusBannerText.isNullOrBlank()) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.90f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(6.dp)
            .testTag("audio_editor_status_banner"),
        ) {
          Text(
            text = statusBannerText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Playback Transport & Quick Actions Row (aligned with VideoTransportRow)
// ---------------------------------------------------------------------------

@Composable
private fun AudioTransportRow(
  buffer: AudioSampleBuffer,
  isPlaying: Boolean,
  loopPlayback: Boolean,
  playheadFraction: Float,
  trimStartFraction: Float,
  trimEndFraction: Float,
  speedMultiplier: Float,
  hasAnyChanges: Boolean,
  isBusy: Boolean,
  onTogglePlayPause: () -> Unit,
  onStopRewind: () -> Unit,
  onToggleLoop: () -> Unit,
  onShareAudio: () -> Unit,
  onOpenAudio: () -> Unit,
  onResetAll: () -> Unit,
) {
  val currentMs = (playheadFraction * buffer.durationMs).roundToInt().toLong()
  val endMs = (trimEndFraction * buffer.durationMs).roundToInt().toLong()
  val effectiveDurationMs = remember(
    buffer.durationMs,
    trimStartFraction,
    trimEndFraction,
    speedMultiplier,
  ) {
    val selectedMs = ((trimEndFraction - trimStartFraction).coerceIn(0f, 1f) * buffer.durationMs)
    (selectedMs / speedMultiplier.coerceIn(0.25f, 4.0f)).roundToInt().toLong().coerceAtLeast(0L)
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      FilledIconButton(
        onClick = onTogglePlayPause,
        modifier = Modifier
          .size(38.dp)
          .testTag("audio_editor_play_pause_button"),
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = if (isPlaying) "Pause" else "Play",
          modifier = Modifier.size(20.dp),
        )
      }

      FilledTonalIconButton(
        onClick = onStopRewind,
        modifier = Modifier
          .size(34.dp)
          .testTag("audio_editor_stop_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Stop,
          contentDescription = "Stop and rewind to selection start",
          modifier = Modifier.size(17.dp),
        )
      }

      FilledTonalIconToggleButton(
        checked = loopPlayback,
        onCheckedChange = { onToggleLoop() },
        modifier = Modifier
          .size(34.dp)
          .testTag("audio_editor_loop_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Repeat,
          contentDescription = "Loop selection",
          modifier = Modifier.size(17.dp),
        )
      }

      Column {
        Text(
          text = "${AudioRecorderEngine.formatDurationMs(currentMs)} / ${AudioRecorderEngine.formatDurationMs(endMs)}",
          style = MaterialTheme.typography.labelMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Out ${AudioRecorderEngine.formatDurationMs(effectiveDurationMs)} • ${String.format(Locale.US, "%.2g×", speedMultiplier)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      IconButton(
        onClick = onShareAudio,
        enabled = !isBusy,
        modifier = Modifier
          .size(36.dp)
          .testTag("audio_editor_share_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = "Share edited audio",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(19.dp),
        )
      }
      IconButton(
        onClick = onOpenAudio,
        enabled = !isBusy,
        modifier = Modifier
          .size(36.dp)
          .testTag("audio_editor_open_button"),
      ) {
        Icon(
          imageVector = Icons.Default.FolderOpen,
          contentDescription = "Open another audio file",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(19.dp),
        )
      }
      IconButton(
        onClick = onResetAll,
        enabled = hasAnyChanges && !isBusy,
        modifier = Modifier
          .size(36.dp)
          .testTag("audio_editor_reset_button"),
      ) {
        Icon(
          imageVector = Icons.Default.RestartAlt,
          contentDescription = "Reset to original",
          modifier = Modifier.size(19.dp),
        )
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Mode Control Panels (Trim, Volume & Fade, Speed & Reverse)
// Aligned with ImageEditorScreen & VideoEditorScreen controls
// ---------------------------------------------------------------------------

@Composable
private fun AudioTrimControls(
  buffer: AudioSampleBuffer,
  trimStartFraction: Float,
  trimEndFraction: Float,
  playheadFraction: Float,
  onTrimChange: (Float?, Float?) -> Unit,
  onSetStartAtPlayhead: () -> Unit,
  onSetEndAtPlayhead: () -> Unit,
  onResetSelection: () -> Unit,
  onTrimKeepSelection: () -> Unit,
  onCutRemoveSelection: () -> Unit,
) {
  val startMs = (trimStartFraction * buffer.durationMs).roundToInt().toLong()
  val endMs = (trimEndFraction * buffer.durationMs).roundToInt().toLong()
  val playheadMs = (playheadFraction * buffer.durationMs).roundToInt().toLong()
  val hasSubSelection = trimStartFraction > 0.005f || trimEndFraction < 0.995f

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Start ${AudioRecorderEngine.formatDurationMs(startMs)}",
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(98.dp),
      )
      SlimDotSlider(
        value = trimStartFraction,
        onValueChange = { raw -> onTrimChange(raw, null) },
        valueRange = 0f..1f,
        modifier = Modifier
          .weight(1f)
          .testTag("trim_start_slider"),
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "End   ${AudioRecorderEngine.formatDurationMs(endMs)}",
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(98.dp),
      )
      SlimDotSlider(
        value = trimEndFraction,
        onValueChange = { raw -> onTrimChange(null, raw) },
        valueRange = 0f..1f,
        modifier = Modifier
          .weight(1f)
          .testTag("trim_end_slider"),
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        FilledTonalButton(
          onClick = onSetStartAtPlayhead,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("trim_set_start_button"),
        ) {
          Text(
            text = "Set Start (${AudioRecorderEngine.formatDurationMs(playheadMs)})",
            style = MaterialTheme.typography.labelSmall,
          )
        }

        FilledTonalButton(
          onClick = onSetEndAtPlayhead,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("trim_set_end_button"),
        ) {
          Text(
            text = "Set End",
            style = MaterialTheme.typography.labelSmall,
          )
        }

        OutlinedButton(
          onClick = onResetSelection,
          enabled = hasSubSelection,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("trim_reset_button"),
        ) {
          Text(
            text = "Reset",
            style = MaterialTheme.typography.labelSmall,
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        FilledTonalButton(
          onClick = onCutRemoveSelection,
          enabled = hasSubSelection,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("audio_editor_cut_button"),
        ) {
          Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Cut (Remove)",
            style = MaterialTheme.typography.labelSmall,
          )
        }

        Button(
          onClick = onTrimKeepSelection,
          enabled = hasSubSelection,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("audio_editor_trim_button"),
        ) {
          Icon(
            imageVector = Icons.Default.ContentCut,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Trim (Keep)",
            style = MaterialTheme.typography.labelSmall,
          )
        }
      }
    }
  }
}

@Composable
private fun VolumeAndFadeControls(
  buffer: AudioSampleBuffer,
  trimStartFraction: Float,
  trimEndFraction: Float,
  volumeGainPercent: Float,
  fadeInSeconds: Float,
  fadeOutSeconds: Float,
  onVolumeGainChange: (Float) -> Unit,
  onFadeInChange: (Float) -> Unit,
  onFadeOutChange: (Float) -> Unit,
  onResetVolumeAndFade: () -> Unit,
  onApplyVolumeAndFade: () -> Unit,
  onNormalize: () -> Unit,
  onMuteSelection: () -> Unit,
) {
  val maxFadeSec = ((buffer.durationMs / 1000f) * (trimEndFraction - trimStartFraction)).coerceIn(0.5f, 5.0f)
  val hasPendingVolumeOrFade =
    abs(volumeGainPercent - 100f) > 0.5f || fadeInSeconds > 0.05f || fadeOutSeconds > 0.05f

  Column(modifier = Modifier.fillMaxWidth()) {
    // 1. Volume Gain (inline label + SlimDotSlider matching ImageEditor / VideoEditor)
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Gain ${volumeGainPercent.roundToInt()}%",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(98.dp),
      )
      SlimDotSlider(
        value = volumeGainPercent,
        onValueChange = onVolumeGainChange,
        valueRange = 0f..250f,
        modifier = Modifier
          .weight(1f)
          .testTag("audio_editor_gain_slider"),
      )
    }

    // 2. Fade In
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Fade In ${String.format(Locale.US, "%.1fs", fadeInSeconds)}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(98.dp),
      )
      SlimDotSlider(
        value = fadeInSeconds.coerceIn(0f, maxFadeSec),
        onValueChange = onFadeInChange,
        valueRange = 0f..maxFadeSec,
        modifier = Modifier
          .weight(1f)
          .testTag("audio_editor_fade_in_slider"),
      )
    }

    // 3. Fade Out
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Fade Out ${String.format(Locale.US, "%.1fs", fadeOutSeconds)}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(98.dp),
      )
      SlimDotSlider(
        value = fadeOutSeconds.coerceIn(0f, maxFadeSec),
        onValueChange = onFadeOutChange,
        valueRange = 0f..maxFadeSec,
        modifier = Modifier
          .weight(1f)
          .testTag("audio_editor_fade_out_slider"),
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 4. Compact CircleShape action row (Normalize, Mute on left; Reset, Apply on right)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        FilledTonalButton(
          onClick = onNormalize,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("audio_editor_normalize_button"),
        ) {
          Icon(
            imageVector = Icons.Default.AutoFixHigh,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Normalize",
            style = MaterialTheme.typography.labelSmall,
          )
        }

        FilledTonalButton(
          onClick = onMuteSelection,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("audio_editor_mute_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeOff,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Mute",
            style = MaterialTheme.typography.labelSmall,
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedButton(
          onClick = onResetVolumeAndFade,
          enabled = hasPendingVolumeOrFade,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("volume_fade_reset_button"),
        ) {
          Text(
            text = "Reset",
            style = MaterialTheme.typography.labelSmall,
          )
        }

        Button(
          onClick = onApplyVolumeAndFade,
          enabled = hasPendingVolumeOrFade,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("audio_editor_apply_volume_fade_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Apply",
            style = MaterialTheme.typography.labelSmall,
          )
        }
      }
    }
  }
}

@Composable
private fun SpeedAndReverseControls(
  speedMultiplier: Float,
  onSpeedChange: (Float) -> Unit,
  onResetSpeed: () -> Unit,
  onApplySpeed: () -> Unit,
  onReverseSelection: () -> Unit,
) {
  val speedPresets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
  val hasSpeedDelta = abs(speedMultiplier - 1.0f) >= 0.02f

  Column(modifier = Modifier.fillMaxWidth()) {
    // 1. Speed Preset Chips Row at top (matching VideoSpeedAndAudioControls & AdjustControls)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      for (preset in speedPresets) {
        val label = if (preset == 1.0f) "1×" else "${preset}×"
        FilterChip(
          selected = abs(speedMultiplier - preset) < 0.02f,
          onClick = { onSpeedChange(preset) },
          label = { Text(label, style = MaterialTheme.typography.labelSmall) },
          shape = CircleShape,
          modifier = Modifier.testTag("speed_chip_${preset}"),
        )
      }
    }

    // 2. Inline Speed Slider Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Speed ${String.format(Locale.US, "%.2f×", speedMultiplier)}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(98.dp),
      )
      SlimDotSlider(
        value = speedMultiplier,
        onValueChange = onSpeedChange,
        valueRange = 0.5f..2.0f,
        modifier = Modifier
          .weight(1f)
          .testTag("audio_editor_speed_slider"),
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 3. Compact CircleShape Action Row (Reverse on left; Reset & Apply Speed on right)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalButton(
        onClick = onReverseSelection,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier
          .height(34.dp)
          .testTag("audio_editor_reverse_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Replay,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Reverse",
          style = MaterialTheme.typography.labelSmall,
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedButton(
          onClick = onResetSpeed,
          enabled = hasSpeedDelta,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("speed_reset_button"),
        ) {
          Text(
            text = "Reset",
            style = MaterialTheme.typography.labelSmall,
          )
        }

        Button(
          onClick = onApplySpeed,
          enabled = hasSpeedDelta,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("audio_editor_apply_speed_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Apply Speed",
            style = MaterialTheme.typography.labelSmall,
          )
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Bottom Mode Selector Row (aligned with ModeSelectorRow & VideoModeSelectorRow)
// ---------------------------------------------------------------------------

@Composable
private fun AudioEditorModeSelector(
  activeMode: AudioEditorMode,
  hasTrimEdits: Boolean,
  hasVolumeFadeEdits: Boolean,
  hasSpeedEdits: Boolean,
  onSelectMode: (AudioEditorMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  val modes = AudioEditorMode.entries
  SingleChoiceSegmentedButtonRow(
    modifier = modifier
      .fillMaxWidth()
      .padding(bottom = 4.dp)
      .testTag("mode_selector_row"),
  ) {
    modes.forEachIndexed { index, mode ->
      val selected = mode == activeMode
      val hasActiveEdits = when (mode) {
        AudioEditorMode.TRIM -> hasTrimEdits
        AudioEditorMode.VOLUME_FADE -> hasVolumeFadeEdits
        AudioEditorMode.SPEED_EFFECTS -> hasSpeedEdits
      }
      val icon = when (mode) {
        AudioEditorMode.TRIM -> Icons.Default.ContentCut
        AudioEditorMode.VOLUME_FADE -> Icons.AutoMirrored.Filled.VolumeUp
        AudioEditorMode.SPEED_EFFECTS -> Icons.Default.Speed
      }
      SegmentedButton(
        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
        onClick = { onSelectMode(mode) },
        selected = selected,
        icon = {},
        modifier = Modifier.testTag("audio_editor_mode_${mode.name.lowercase(Locale.ROOT)}"),
        label = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (hasActiveEdits) "${mode.displayName}•" else mode.displayName,
              style = MaterialTheme.typography.labelSmall,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        },
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Save / Export Audio Dialog (DESIGN_LANGUAGE.md Section 5.2)
// ---------------------------------------------------------------------------

@Composable
private fun AudioExportDialog(
  buffer: AudioSampleBuffer,
  initialFileName: String,
  originalSourceFile: File?,
  outputDirectory: File,
  isExporting: Boolean,
  onDismiss: () -> Unit,
  onConfirmExport: (String, AudioExportFormat, Boolean) -> Unit,
) {
  val canOverwriteOriginal = originalSourceFile != null
  var overwriteOriginal by remember(canOverwriteOriginal) { mutableStateOf(false) }

  val initialStem = remember(initialFileName) {
    val base = FileConverterEngine.extractFileStem(initialFileName)
    if (base.endsWith("_edited")) base else "${base}_edited"
  }
  val defaultFormat = remember(initialFileName) {
    AudioExportFormat.fromFileName(initialFileName)
  }
  var selectedFormat by remember(defaultFormat) { mutableStateOf(defaultFormat) }
  var textFieldValue by remember(initialStem) {
    mutableStateOf(
      TextFieldValue(
        text = initialStem,
        selection = TextRange(0, initialStem.length),
      )
    )
  }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(overwriteOriginal) {
    if (!overwriteOriginal) {
      try {
        focusRequester.requestFocus()
      } catch (_: Throwable) {}
    }
  }

  val activeFormat = if (overwriteOriginal && originalSourceFile != null) {
    AudioExportFormat.fromFileName(originalSourceFile.name)
  } else {
    selectedFormat
  }

  val fullCandidateName = "${textFieldValue.text.trim()}.${selectedFormat.extension}"
  val validationError = remember(textFieldValue.text, selectedFormat, overwriteOriginal) {
    if (overwriteOriginal) {
      null
    } else if (textFieldValue.text.isBlank()) {
      "File name cannot be empty"
    } else {
      FileManagerEngine.validateFileName(fullCandidateName)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("audio_editor_export_dialog"),
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
            imageVector = Icons.Default.SaveAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Save Audio Edits",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = if (overwriteOriginal && originalSourceFile?.parentFile != null) {
              AudioRecorderEngine.formatDirectoryDisplayPath(originalSourceFile.parentFile!!)
            } else {
              AudioRecorderEngine.formatDirectoryDisplayPath(outputDirectory)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Context Preview Card (DESIGN_LANGUAGE.md Section 5.2)
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
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
            }
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              Text(
                text = "${AudioRecorderEngine.formatDurationMs(buffer.durationMs)} • ${buffer.sampleRate} Hz • ${if (buffer.safeChannels == 2) "Stereo" else "Mono"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${activeFormat.label} • ${activeFormat.subtitle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        // Save Mode Selector (Save as Copy vs Overwrite Original)
        if (originalSourceFile != null) {
          val saveModes = listOf(
            false to "Save as Copy",
            true to "Overwrite Original",
          )
          SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
          ) {
            saveModes.forEachIndexed { index, (isOverwriteOption, label) ->
              val selected = overwriteOriginal == isOverwriteOption
              SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = saveModes.size),
                onClick = { overwriteOriginal = isOverwriteOption },
                selected = selected,
                icon = {},
                modifier = Modifier.testTag(
                  if (isOverwriteOption) {
                    "audio_editor_save_mode_overwrite"
                  } else {
                    "audio_editor_save_mode_copy"
                  }
                ),
                label = {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                },
              )
            }
          }
        }

        if (overwriteOriginal && originalSourceFile != null) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = originalSourceFile.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Replaces the original file in place with your current edits.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        } else {
          // Format Selector Chips
          Text(
            text = "Format",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            for (fmt in AudioExportFormat.entries) {
              FilterChip(
                selected = selectedFormat == fmt,
                onClick = { selectedFormat = fmt },
                label = { Text(fmt.label, style = MaterialTheme.typography.labelSmall) },
                shape = CircleShape,
              )
            }
          }

          // Rounded Input Field (DESIGN_LANGUAGE.md Section 5.2)
          OutlinedTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            label = { Text("File name") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            },
            trailingIcon = if (textFieldValue.text.isNotEmpty()) {
              {
                IconButton(onClick = { textFieldValue = TextFieldValue("") }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear file name",
                  )
                }
              }
            } else null,
            suffix = { Text(".${selectedFormat.extension}") },
            singleLine = true,
            isError = validationError != null,
            supportingText = validationError?.let { err -> { Text(err) } },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester)
              .testTag("audio_editor_export_filename_input"),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmExport(textFieldValue.text.trim(), activeFormat, overwriteOriginal) },
        enabled = validationError == null && !isExporting,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("audio_editor_export_confirm_button"),
      ) {
        if (isExporting) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary,
          )
          Spacer(modifier = Modifier.width(6.dp))
        } else {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
        }
        Text(if (overwriteOriginal) "Overwrite" else "Save Copy")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        enabled = !isExporting,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Cancel")
      }
    },
  )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AudioEditorScreenEmptyPreview() {
  ToolboxTheme {
    AudioEditorScreen()
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AudioEditorScreenLoadedPreview() {
  ToolboxTheme {
    AudioEditorScreen(
      initialBuffer = AudioEditorEngine.createSampleAudioBuffer(),
      initialFileName = "sample_chord_arpeggio.wav",
    )
  }
}
