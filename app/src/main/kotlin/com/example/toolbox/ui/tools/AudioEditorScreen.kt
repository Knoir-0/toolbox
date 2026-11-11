package com.example.toolbox.ui.tools

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
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
 * Shares audio, file, and UI infrastructure across the application:
 * - [AudioEditorEngine] / [AudioRecorderEngine] / [FileConverterEngine] for decoding, editing, and multi-format export.
 * - [StorageFilePickerDialog], [copyUriToTempSourceFile], and [shareConvertedFile] from [FileConverterScreen].
 * - [ImageEditorHistory] from [ImageEditorEngine] for multi-step undo/redo state management.
 * - [SlimDotSlider] from [ImageEditorScreen] for tactile range and parameter adjustments.
 */
@Composable
fun AudioEditorScreen(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val isInspectionMode = LocalInspectionMode.current
  val coroutineScope = rememberCoroutineScope()

  val audioDirectory = remember {
    if (isInspectionMode) {
      File("/storage/emulated/0/Audio")
    } else {
      AudioRecorderEngine.getDefaultAudioDirectory(context)
    }
  }

  // Undo/redo history shared from ImageEditorEngine
  val history = remember { ImageEditorHistory<AudioEditSnapshot>(maxDepth = 15) }
  var historyVersion by remember { mutableIntStateOf(0) }

  var originalBuffer by remember { mutableStateOf<AudioSampleBuffer?>(null) }
  var currentBuffer by remember { mutableStateOf<AudioSampleBuffer?>(null) }
  var sourceFile by remember { mutableStateOf<File?>(null) }
  var sourceFileName by remember { mutableStateOf("sample_audio.wav") }
  var statusBannerText by remember { mutableStateOf<String?>(null) }
  var isBusy by remember { mutableStateOf(false) }

  // Selection & mode state
  var activeMode by remember { mutableStateOf(AudioEditorMode.VOLUME_FADE) }
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
  var loopPlayback by remember { mutableStateOf(false) }
  var playheadFraction by remember { mutableFloatStateOf(0f) }
  var playbackJob by remember { mutableStateOf<Job?>(null) }
  var activeAudioTrack by remember { mutableStateOf<AudioTrack?>(null) }

  // Dialog states
  var showFilePickerDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }

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

  fun commitEdit(newBuffer: AudioSampleBuffer, actionDescription: String, resetSelection: Boolean = true) {
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

  val canUndo = remember(historyVersion) { history.canUndo }
  val canRedo = remember(historyVersion) { history.canRedo }

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
          val sample = AudioEditorEngine.createSampleAudioBuffer()
          loadBufferIntoEditor(
            buffer = sample,
            name = "sample_chord_arpeggio.wav",
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
        // 1. Standard Tool Header / Top Bar (DESIGN_LANGUAGE.md Section 5.1)
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
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Audio Editor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  val chLabel = if (activeBuf.safeChannels == 2) "Stereo" else "Mono"
                  val subtitleText =
                    "$sourceFileName • ${AudioRecorderEngine.formatDurationMs(activeBuf.durationMs)} • ${activeBuf.sampleRate / 1000.0}kHz $chLabel"
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
              ) {
                IconButton(
                  onClick = {
                    val active = currentBuffer ?: return@IconButton
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
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                  },
                  enabled = canUndo && !isBusy,
                  modifier = Modifier.testTag("audio_editor_undo_button"),
                ) {
                  Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                }

                IconButton(
                  onClick = {
                    val active = currentBuffer ?: return@IconButton
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
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                  },
                  enabled = canRedo && !isBusy,
                  modifier = Modifier.testTag("audio_editor_redo_button"),
                ) {
                  Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                }

                IconButton(
                  onClick = {
                    val orig = originalBuffer ?: return@IconButton
                    volumeGainPercent = 100f
                    fadeInSeconds = 0f
                    fadeOutSeconds = 0f
                    speedMultiplier = 1.0f
                    commitEdit(orig, "Reset to original audio")
                  },
                  enabled = !isBusy && (
                    currentBuffer != originalBuffer ||
                      trimStartFraction > 0.005f ||
                      trimEndFraction < 0.995f ||
                      abs(volumeGainPercent - 100f) > 0.5f ||
                      fadeInSeconds > 0.05f ||
                      fadeOutSeconds > 0.05f ||
                      abs(speedMultiplier - 1f) >= 0.02f
                    ),
                  modifier = Modifier.testTag("audio_editor_reset_button"),
                ) {
                  Icon(Icons.Default.Restore, contentDescription = "Reset to original")
                }

                IconButton(
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    stopPlayback()
                    currentBuffer = null
                    originalBuffer = null
                    sourceFile = null
                    history.clear()
                    statusBannerText = null
                    historyVersion++
                  },
                  enabled = !isBusy,
                  modifier = Modifier.testTag("audio_editor_close_button"),
                ) {
                  Icon(Icons.Default.Close, contentDescription = "Close audio")
                }
              }
            }
          }
        }

        // 2. Active Editor Content
        Column(
          modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        // Waveform & Selection Stage Card (handles trimming & cutting directly at the top)
        WaveformStageCard(
          buffer = activeBuf,
          peaks = waveformPeaks,
          trimStartFraction = trimStartFraction,
          trimEndFraction = trimEndFraction,
          playheadFraction = playheadFraction,
          isPlaying = isPlaying,
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
            playheadFraction = fraction.coerceIn(trimStartFraction, trimEndFraction)
          },
          onResetSelection = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            trimStartFraction = 0f
            trimEndFraction = 1f
            playheadFraction = 0f
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

        // Transport & Scrubber Card
        PlaybackTransportCard(
          buffer = activeBuf,
          isPlaying = isPlaying,
          loopPlayback = loopPlayback,
          playheadFraction = playheadFraction,
          trimStartFraction = trimStartFraction,
          trimEndFraction = trimEndFraction,
          onTogglePlayPause = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (isPlaying) stopPlayback() else startPlayback()
          },
          onStopRewind = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            stopPlayback()
            playheadFraction = trimStartFraction
          },
          onToggleLoop = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            loopPlayback = !loopPlayback
          },
          onScrub = { frac ->
            if (isPlaying) stopPlayback()
            playheadFraction = frac.coerceIn(0f, 1f)
          },
        )

        // Mode Controls Panel (Volume & Fade, Speed & Reverse)
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            // Segmented Mode Selector
            AudioEditorModeSelector(
              activeMode = activeMode,
              onSelectMode = { mode ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                activeMode = mode
              },
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            when (activeMode) {
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
      }

      // 3. Dedicated Bottom Save & Share Action Bar
      AudioEditorSaveBar(
        buffer = activeBuf,
        hasHistoryEdits = canUndo || activeBuf != originalBuffer,
        trimStartFraction = trimStartFraction,
        trimEndFraction = trimEndFraction,
        volumeGainPercent = volumeGainPercent,
        fadeInSeconds = fadeInSeconds,
        fadeOutSeconds = fadeOutSeconds,
        speedMultiplier = speedMultiplier,
        isBusy = isBusy,
        onShare = {
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
            val tempShareFile = File(context.cacheDir, "${FileConverterEngine.extractFileStem(sourceFileName)}_edited.wav")
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
        onSaveEdits = {
          stopPlayback()
          showExportDialog = true
        },
      )
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

@Composable
private fun WaveformStageCard(
  buffer: AudioSampleBuffer,
  peaks: List<Float>,
  trimStartFraction: Float,
  trimEndFraction: Float,
  playheadFraction: Float,
  isPlaying: Boolean,
  statusBannerText: String?,
  onSelectionChange: (Float, Float) -> Unit,
  onScrub: (Float) -> Unit,
  onResetSelection: () -> Unit,
  onTrimKeepSelection: () -> Unit,
  onCutRemoveSelection: () -> Unit,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val inactiveBarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)
  val selectionTint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
  val dimMaskColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
  val outlineColor = MaterialTheme.colorScheme.outlineVariant

  val startMs = (trimStartFraction * buffer.durationMs).roundToInt().toLong()
  val endMs = (trimEndFraction * buffer.durationMs).roundToInt().toLong()
  val selectedDurationMs = (endMs - startMs).coerceAtLeast(0L)
  val hasSubSelection = trimStartFraction > 0.005f || trimEndFraction < 0.995f
  val peakLevel = remember(buffer, trimStartFraction, trimEndFraction) {
    (AudioEditorEngine.calculatePeakAmplitude(buffer, trimStartFraction, trimEndFraction) * 100f).roundToInt()
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Selection: ${AudioRecorderEngine.formatDurationMs(startMs)} – ${AudioRecorderEngine.formatDurationMs(endMs)}",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Length: ${AudioRecorderEngine.formatDurationMs(selectedDurationMs)} • Peak: $peakLevel%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        if (hasSubSelection) {
          FilterChip(
            selected = false,
            onClick = onResetSelection,
            label = {
              Text(
                text = "Select All",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
              )
            },
            shape = RoundedCornerShape(20.dp),
          )
        }
      }

      // Interactive Waveform Canvas supporting simultaneous multi-touch start & end handle dragging
      val currentTrimStart by rememberUpdatedState(trimStartFraction)
      val currentTrimEnd by rememberUpdatedState(trimEndFraction)
      val currentOnSelectionChange by rememberUpdatedState(onSelectionChange)
      val currentOnScrub by rememberUpdatedState(onScrub)

      var isDraggingStartHandle by remember { mutableStateOf(false) }
      var isDraggingEndHandle by remember { mutableStateOf(false) }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(148.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f))
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

                // Bind any newly pressed pointers (e.g. second finger placed on the other handle)
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

                    // If first finger started on playhead and a second finger touches down,
                    // promote the two fingers into simultaneous start (left) and end (right) handles.
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

          // Waveform amplitude bars
          if (peaks.isNotEmpty()) {
            val count = peaks.size
            val slotWidth = w / count
            val barWidth = (slotWidth * 0.62f).coerceAtLeast(2.dp.toPx())
            val maxHalfHeight = (h * 0.42f)

            for (i in 0 until count) {
              val barCenterX = (i + 0.5f) * slotWidth
              val barFrac = barCenterX / w
              val inSelection = barFrac in trimStartFraction..trimEndFraction
              val peak = peaks[i].coerceIn(0.04f, 1f)
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

          // Trim Start & End vertical marker lines + vertically centered bar handles
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

          // Start vertical boundary line & centered bar handle
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

          // End vertical boundary line & centered bar handle
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

          // Playhead cursor line
          drawLine(
            color = tertiaryColor,
            start = Offset(playheadX, 0f),
            end = Offset(playheadX, h),
            strokeWidth = 2.dp.toPx(),
          )
          drawCircle(
            color = tertiaryColor,
            radius = 5.dp.toPx(),
            center = Offset(playheadX, centerY),
            style = Stroke(width = 2.dp.toPx()),
          )
        }
      }

      // Direct inline Trim & Cut action bar right below the waveform canvas
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Button(
          onClick = onTrimKeepSelection,
          enabled = hasSubSelection,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("audio_editor_trim_button"),
        ) {
          Icon(
            imageVector = Icons.Default.ContentCut,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Trim (Keep)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        FilledTonalButton(
          onClick = onCutRemoveSelection,
          enabled = hasSubSelection,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("audio_editor_cut_button"),
        ) {
          Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Cut (Remove)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      if (!statusBannerText.isNullOrBlank()) {
        Text(
          text = statusBannerText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.testTag("audio_editor_status_banner"),
        )
      }
    }
  }
}

@Composable
private fun PlaybackTransportCard(
  buffer: AudioSampleBuffer,
  isPlaying: Boolean,
  loopPlayback: Boolean,
  playheadFraction: Float,
  trimStartFraction: Float,
  trimEndFraction: Float,
  onTogglePlayPause: () -> Unit,
  onStopRewind: () -> Unit,
  onToggleLoop: () -> Unit,
  onScrub: (Float) -> Unit,
) {
  val currentMs = (playheadFraction * buffer.durationMs).roundToInt().toLong()
  val totalMs = buffer.durationMs

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FilledIconButton(
            onClick = onTogglePlayPause,
            shape = CircleShape,
            modifier = Modifier
              .size(44.dp)
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
            shape = CircleShape,
            modifier = Modifier
              .size(44.dp)
              .testTag("audio_editor_stop_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Stop,
              contentDescription = "Stop and rewind to selection start",
              modifier = Modifier.size(20.dp),
            )
          }

          FilledTonalIconButton(
            onClick = onToggleLoop,
            shape = CircleShape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
              containerColor = if (loopPlayback) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceVariant
              },
            ),
            modifier = Modifier
              .size(44.dp)
              .testTag("audio_editor_loop_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Repeat,
              contentDescription = "Loop selection",
              tint = if (loopPlayback) {
                MaterialTheme.colorScheme.onPrimaryContainer
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(20.dp),
            )
          }
        }

        Text(
          text = "${AudioRecorderEngine.formatDurationMs(currentMs)} / ${AudioRecorderEngine.formatDurationMs(totalMs)}",
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      SlimDotSlider(
        value = playheadFraction,
        onValueChange = onScrub,
        valueRange = 0f..1f,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun AudioEditorModeSelector(
  activeMode: AudioEditorMode,
  onSelectMode: (AudioEditorMode) -> Unit,
) {
  val modes = AudioEditorMode.entries
  SingleChoiceSegmentedButtonRow(
    modifier = Modifier.fillMaxWidth(),
  ) {
    modes.forEachIndexed { index, mode ->
      val selected = mode == activeMode
      val icon = when (mode) {
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
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = mode.displayName,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        },
      )
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
  onApplyVolumeAndFade: () -> Unit,
  onNormalize: () -> Unit,
  onMuteSelection: () -> Unit,
) {
  val maxFadeSec = ((buffer.durationMs / 1000f) * (trimEndFraction - trimStartFraction)).coerceIn(0.5f, 5.0f)

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    // Gain slider
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "Volume Gain",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "${volumeGainPercent.roundToInt()}%",
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      SlimDotSlider(
        value = volumeGainPercent,
        onValueChange = onVolumeGainChange,
        valueRange = 0f..250f,
        modifier = Modifier.fillMaxWidth(),
      )
    }

    // Fade In slider
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "Fade In",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = String.format(Locale.US, "%.1fs", fadeInSeconds),
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      SlimDotSlider(
        value = fadeInSeconds.coerceIn(0f, maxFadeSec),
        onValueChange = onFadeInChange,
        valueRange = 0f..maxFadeSec,
        modifier = Modifier.fillMaxWidth(),
      )
    }

    // Fade Out slider
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "Fade Out",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = String.format(Locale.US, "%.1fs", fadeOutSeconds),
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      SlimDotSlider(
        value = fadeOutSeconds.coerceIn(0f, maxFadeSec),
        onValueChange = onFadeOutChange,
        valueRange = 0f..maxFadeSec,
        modifier = Modifier.fillMaxWidth(),
      )
    }

    // Uniform 44.dp aligned action row (Normalize, Mute, Apply)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalButton(
        onClick = onNormalize,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("audio_editor_normalize_button"),
      ) {
        Icon(
          imageVector = Icons.Default.AutoFixHigh,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Normalize",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      FilledTonalButton(
        onClick = onMuteSelection,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("audio_editor_mute_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.VolumeOff,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Mute",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Button(
        onClick = onApplyVolumeAndFade,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
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
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun SpeedAndReverseControls(
  speedMultiplier: Float,
  onSpeedChange: (Float) -> Unit,
  onApplySpeed: () -> Unit,
  onReverseSelection: () -> Unit,
) {
  val speedPresets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "Speed & Pitch Factor",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = String.format(Locale.US, "%.2fx", speedMultiplier),
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      SlimDotSlider(
        value = speedMultiplier,
        onValueChange = onSpeedChange,
        valueRange = 0.5f..2.0f,
        modifier = Modifier.fillMaxWidth(),
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      for (preset in speedPresets) {
        FilterChip(
          selected = abs(speedMultiplier - preset) < 0.02f,
          onClick = { onSpeedChange(preset) },
          label = { Text("${preset}x", style = MaterialTheme.typography.labelMedium) },
          shape = RoundedCornerShape(20.dp),
        )
      }
    }

    // Uniform 44.dp aligned action row (Reverse, Apply Speed)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalButton(
        onClick = onReverseSelection,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("audio_editor_reverse_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Replay,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Reverse",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Button(
        onClick = onApplySpeed,
        enabled = abs(speedMultiplier - 1.0f) >= 0.02f,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("audio_editor_apply_speed_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Apply Speed",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

/**
 * Dedicated bottom action bar for saving and sharing edits without hunting for small header icons.
 * Automatically summarizes applied or pending adjustments (clip handles, volume/fade, speed).
 */
@Composable
private fun AudioEditorSaveBar(
  buffer: AudioSampleBuffer,
  hasHistoryEdits: Boolean,
  trimStartFraction: Float,
  trimEndFraction: Float,
  volumeGainPercent: Float,
  fadeInSeconds: Float,
  fadeOutSeconds: Float,
  speedMultiplier: Float,
  isBusy: Boolean,
  onShare: () -> Unit,
  onSaveEdits: () -> Unit,
) {
  val pendingTags = remember(
    trimStartFraction,
    trimEndFraction,
    volumeGainPercent,
    fadeInSeconds,
    fadeOutSeconds,
    speedMultiplier,
  ) {
    buildList {
      if (trimStartFraction > 0.005f || trimEndFraction < 0.995f) {
        add("Clip range")
      }
      if (abs(volumeGainPercent - 100f) > 1f || fadeInSeconds > 0.05f || fadeOutSeconds > 0.05f) {
        add("Gain/Fade")
      }
      if (abs(speedMultiplier - 1.0f) >= 0.02f) {
        add(String.format(Locale.US, "%.2fx speed", speedMultiplier))
      }
    }
  }

  val effectiveDurationMs = remember(
    buffer.durationMs,
    trimStartFraction,
    trimEndFraction,
    speedMultiplier,
  ) {
    val selectedMs = ((trimEndFraction - trimStartFraction).coerceIn(0f, 1f) * buffer.durationMs)
    (selectedMs / speedMultiplier.coerceIn(0.25f, 4.0f)).roundToInt().toLong().coerceAtLeast(0L)
  }

  val summarySubtitle = when {
    pendingTags.isNotEmpty() -> "Includes: ${pendingTags.joinToString(" • ")}"
    hasHistoryEdits -> "Edits ready • ${AudioRecorderEngine.formatDurationMs(effectiveDurationMs)}"
    else -> "Output: ${AudioRecorderEngine.formatDurationMs(effectiveDurationMs)}"
  }

  Surface(
    color = MaterialTheme.colorScheme.surfaceContainer,
    tonalElevation = 3.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("audio_editor_save_bar"),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (hasHistoryEdits || pendingTags.isNotEmpty()) "Unsaved Edits" else "Ready to Save",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = summarySubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = if (pendingTags.isNotEmpty()) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        FilledTonalButton(
          onClick = onShare,
          enabled = !isBusy,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
          modifier = Modifier
            .height(44.dp)
            .testTag("audio_editor_share_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share edited audio",
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Share",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
          )
        }

        Button(
          onClick = onSaveEdits,
          enabled = !isBusy,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
          modifier = Modifier
            .height(44.dp)
            .testTag("audio_editor_export_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Save Edits",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
          )
        }
      }
    }
  }
}

/**
 * Save / Export Audio dialog adhering to DESIGN_LANGUAGE.md Section 5.2.
 * Supports both saving as a new copy and overwriting the original audio file directly.
 */
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
                label = { Text(fmt.label) },
                shape = RoundedCornerShape(20.dp),
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AudioEditorScreenEmptyPreview() {
  ToolboxTheme {
    AudioEditorScreen()
  }
}
