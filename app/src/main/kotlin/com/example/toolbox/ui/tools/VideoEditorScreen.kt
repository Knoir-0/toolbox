package com.example.toolbox.ui.tools

import android.graphics.Bitmap
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint as AndroidPaint
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Environment
import android.view.Surface as AndroidSurface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Simple Video Editor screen powered by real-time GPU preview and fast multi-tier FFmpeg export
 * (zero-reencode stream copy, hardware MediaCodec H.264, and universal software fallback).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
  initialFile: File? = null,
  initialDirectory: File? = null,
  initialMetadataForPreview: VideoMetadata? = null,
  modifier: Modifier = Modifier,
  onNavigateBack: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val isInspectionMode = LocalInspectionMode.current
  val coroutineScope = rememberCoroutineScope()

  var metadata by remember { mutableStateOf(initialMetadataForPreview) }
  var editState by remember {
    mutableStateOf(
      initialMetadataForPreview?.let { VideoEditState.initialForDuration(it.durationMs) }
        ?: VideoEditState()
    )
  }
  val history = remember { ImageEditorHistory<VideoEditState>() }
  // Revision counter forces recomposition when mutating ImageEditorHistory stacks
  var historyRevision by remember { mutableLongStateOf(0L) }

  var activeMode by rememberSaveable { mutableStateOf(VideoEditorMode.TRIM) }
  var filmstripFrames by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

  var isPlaying by remember { mutableStateOf(false) }
  var loopTrimPlayback by rememberSaveable { mutableStateOf(true) }
  var playheadMs by remember { mutableLongStateOf(0L) }
  var seekRequestToken by remember { mutableLongStateOf(0L) }
  var seekTargetMs by remember { mutableLongStateOf(0L) }

  var isLoadingVideo by remember { mutableStateOf(false) }
  var isGeneratingSample by remember { mutableStateOf(false) }
  var showFilePickerDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var exportOptions by remember { mutableStateOf(VideoExportOptions()) }

  var isExporting by remember { mutableStateOf(false) }
  var exportProgress by remember { mutableFloatStateOf(0f) }
  var exportJob by remember { mutableStateOf<Job?>(null) }
  var lastExportResult by remember { mutableStateOf<VideoExportResult?>(null) }

  var showDiscardDialog by remember { mutableStateOf(false) }
  var pendingDiscardAction by remember { mutableStateOf<(() -> Unit)?>(null) }

  fun pushUndoSnapshot(snapshot: VideoEditState = editState) {
    history.push(snapshot)
    historyRevision++
  }

  fun applyCommittedEdit(transform: (VideoEditState) -> VideoEditState) {
    val dur = metadata?.durationMs ?: editState.durationMs
    val next = transform(editState).coerceForDuration(dur)
    if (next != editState) {
      pushUndoSnapshot(editState)
      editState = next
    }
  }

  fun requestSeek(targetMs: Long) {
    val dur = metadata?.durationMs ?: editState.durationMs
    val clamped = targetMs.coerceIn(0L, max(0L, dur))
    playheadMs = clamped
    seekTargetMs = clamped
    seekRequestToken++
  }

  fun loadVideoFile(file: File) {
    coroutineScope.launch {
      isPlaying = false
      isLoadingVideo = true
      val probed = VideoEditorEngine.probeMetadata(file)
        ?: if (file.exists() && VideoEditorEngine.isVideoFile(file)) {
          VideoMetadata(
            file = file,
            durationMs = 5_000L,
            width = 1280,
            height = 720,
            sizeBytes = file.length(),
          )
        } else {
          null
        }
      if (probed == null) {
        isLoadingVideo = false
        Toast.makeText(context, "Could not open video file", Toast.LENGTH_SHORT).show()
        return@launch
      }
      metadata = probed
      editState = VideoEditState.initialForDuration(probed.durationMs)
      history.clear()
      historyRevision++
      playheadMs = 0L
      seekTargetMs = 0L
      seekRequestToken++
      lastExportResult = null
      filmstripFrames = emptyList()
      isLoadingVideo = false
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)

      // Extract lightweight filmstrip thumbnails asynchronously
      val thumbs = VideoEditorEngine.extractFilmstripThumbnails(
        file = probed.file,
        durationMs = probed.durationMs,
        count = 8,
      )
      if (metadata?.file?.absolutePath == probed.file.absolutePath) {
        filmstripFrames = thumbs
      }
    }
  }

  fun performWithDiscardCheck(action: () -> Unit) {
    if (metadata != null && editState.hasAnyChanges) {
      pendingDiscardAction = action
      showDiscardDialog = true
    } else {
      action()
    }
  }

  BackHandler(enabled = onNavigateBack != null) {
    performWithDiscardCheck {
      isPlaying = false
      onNavigateBack?.invoke()
    }
  }

  LaunchedEffect(initialFile?.absolutePath) {
    if (initialFile != null && initialFile.exists() && initialMetadataForPreview == null) {
      loadVideoFile(initialFile)
    }
  }

  val systemVideoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri: Uri? ->
    if (uri != null) {
      coroutineScope.launch {
        isLoadingVideo = true
        val copied = copyUriToTempSourceFile(context, uri)
        if (copied != null) {
          loadVideoFile(copied)
        } else {
          isLoadingVideo = false
          Toast.makeText(context, "Failed to import selected video", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  val canUndo = remember(historyRevision) { history.canUndo }
  val canRedo = remember(historyRevision) { history.canRedo }
  val currentMetadata = metadata

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("video_editor_screen"),
  ) {
    // Top Header Bar following DESIGN_LANGUAGE.md
    VideoEditorTopBar(
      metadata = currentMetadata,
      canUndo = canUndo,
      canRedo = canRedo,
      onUndo = {
        val prev = history.undo(editState)
        if (prev != null) {
          editState = prev
          historyRevision++
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
      },
      onRedo = {
        val next = history.redo(editState)
        if (next != null) {
          editState = next
          historyRevision++
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
      },
      onClose = {
        performWithDiscardCheck {
          isPlaying = false
          metadata = null
          editState = VideoEditState()
          filmstripFrames = emptyList()
          history.clear()
          historyRevision++
          lastExportResult = null
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
      },
      onExportClick = {
        isPlaying = false
        showExportDialog = true
      },
      onNavigateBack = onNavigateBack?.let { navBack ->
        {
          performWithDiscardCheck {
            isPlaying = false
            navBack()
          }
        }
      },
    )

    if (currentMetadata == null) {
      OpenFileInitialView(
        onOpenFile = { showFilePickerDialog = true },
        onUseSample = {
          if (!isGeneratingSample && !isLoadingVideo) {
            coroutineScope.launch {
              isGeneratingSample = true
              val sample = VideoEditorEngine.generateSampleVideo(context)
              isGeneratingSample = false
              if (sample != null) {
                loadVideoFile(sample)
              } else {
                Toast.makeText(context, "Could not generate sample clip", Toast.LENGTH_SHORT).show()
              }
            }
          }
        },
        sampleButtonText = if (isGeneratingSample) "generating sample video..." else "or try sample video",
        sampleButtonIcon = Icons.Default.Movie,
        sampleButtonTestTag = "open_sample_video_button",
        openButtonTestTag = "open_video_button",
        isLoading = isLoadingVideo || isGeneratingSample,
        modifier = Modifier.weight(1f),
      )
    } else {
      val activeStrategy = remember(currentMetadata, editState, exportOptions) {
        VideoEditorEngine.determineStrategy(currentMetadata, editState, exportOptions)
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
      ) {
        // Optional completed export banner
        val completedExport = lastExportResult
        AnimatedVisibility(visible = completedExport?.success == true && completedExport.outputFile != null) {
          if (completedExport?.outputFile != null) {
            RecentVideoExportBanner(
              result = completedExport,
              onShare = {
                val ext = completedExport.outputFile.extension.lowercase(Locale.ROOT)
                val mime = VideoExportFormat.entries.firstOrNull { it.extension == ext }?.mimeType ?: "video/mp4"
                shareConvertedFile(context, completedExport.outputFile, mime)
              },
              onOpenEdited = {
                if (VideoEditorEngine.isVideoFile(completedExport.outputFile)) {
                  loadVideoFile(completedExport.outputFile)
                }
              },
              onDismiss = { lastExportResult = null },
              modifier = Modifier.padding(bottom = 6.dp),
            )
          }
        }

        // 1. Live Video Preview Viewport
        VideoPreviewViewport(
          metadata = currentMetadata,
          editState = editState,
          activeMode = activeMode,
          strategy = activeStrategy,
          isPlaying = isPlaying,
          loopTrimPlayback = loopTrimPlayback,
          playheadMs = playheadMs,
          seekTargetMs = seekTargetMs,
          seekRequestToken = seekRequestToken,
          isInspectionMode = isInspectionMode,
          onTogglePlayPause = {
            if (!isPlaying && playheadMs >= editState.effectiveTrimEndMs - 60L) {
              requestSeek(editState.trimStartMs)
            }
            isPlaying = !isPlaying
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          },
          onPlayheadProgress = { posMs ->
            playheadMs = posMs
          },
          onPlaybackReachedEnd = {
            if (loopTrimPlayback) {
              requestSeek(editState.trimStartMs)
            } else {
              isPlaying = false
              playheadMs = editState.effectiveTrimEndMs
            }
          },
          onCropRectPreviewChange = { updatedRect ->
            editState = editState.copy(cropRect = updatedRect)
          },
          onCropDragStart = {
            pushUndoSnapshot(editState)
          },
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Interactive Filmstrip Timeline + Trim Handles
        VideoFilmstripTimeline(
          durationMs = currentMetadata.durationMs,
          trimStartMs = editState.trimStartMs,
          trimEndMs = editState.effectiveTrimEndMs,
          playheadMs = playheadMs,
          thumbnails = filmstripFrames,
          onScrubPlayhead = { newPosMs ->
            isPlaying = false
            requestSeek(newPosMs)
          },
          onTrimGestureStart = {
            isPlaying = false
            pushUndoSnapshot(editState)
          },
          onTrimRangeChange = { newStartMs, newEndMs ->
            editState = editState.copy(
              trimStartMs = newStartMs,
              trimEndMs = newEndMs,
            ).coerceForDuration(currentMetadata.durationMs)
          },
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Playback Transport & Quick Actions Row
        VideoTransportRow(
          isPlaying = isPlaying,
          loopTrimPlayback = loopTrimPlayback,
          playheadMs = playheadMs,
          editState = editState,
          hasAnyChanges = editState.hasAnyChanges,
          onTogglePlayPause = {
            if (!isPlaying && playheadMs >= editState.effectiveTrimEndMs - 60L) {
              requestSeek(editState.trimStartMs)
            }
            isPlaying = !isPlaying
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          },
          onToggleLoop = {
            loopTrimPlayback = !loopTrimPlayback
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          },
          onCaptureFrame = {
            coroutineScope.launch {
              isPlaying = false
              val frame = VideoEditorEngine.captureEditedFrame(
                file = currentMetadata.file,
                timeMs = playheadMs,
                editState = editState,
              )
              if (frame != null) {
                val saved = VideoEditorEngine.saveCapturedFrameToGallery(
                  context = context,
                  bitmap = frame,
                  baseName = VideoEditorEngine.extractFileStem(currentMetadata.file.name) + "_frame",
                )
                frame.recycle()
                if (saved != null) {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  Toast.makeText(context, "Saved frame: ${saved.name}", Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(context, "Could not save frame snapshot", Toast.LENGTH_SHORT).show()
                }
              } else {
                Toast.makeText(context, "Could not capture frame at current position", Toast.LENGTH_SHORT).show()
              }
            }
          },
          onShareVideo = {
            val fileToShare = lastExportResult?.outputFile?.takeIf { it.exists() } ?: currentMetadata.file
            val ext = fileToShare.extension.lowercase(Locale.ROOT)
            val mime = VideoExportFormat.entries.firstOrNull { it.extension == ext }?.mimeType ?: "video/mp4"
            shareConvertedFile(context, fileToShare, mime)
          },
          onOpenVideo = {
            performWithDiscardCheck {
              isPlaying = false
              showFilePickerDialog = true
            }
          },
          onResetAll = {
            applyCommittedEdit {
              VideoEditState.initialForDuration(currentMetadata.durationMs)
            }
            requestSeek(0L)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          },
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Mode-Specific Controls Card
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
              VideoEditorMode.TRIM -> {
                VideoTrimControls(
                  durationMs = currentMetadata.durationMs,
                  editState = editState,
                  playheadMs = playheadMs,
                  onSliderDragStart = {
                    isPlaying = false
                    pushUndoSnapshot(editState)
                  },
                  onTrimLiveChange = { newStart, newEnd ->
                    editState = editState.copy(
                      trimStartMs = newStart,
                      trimEndMs = newEnd,
                    ).coerceForDuration(currentMetadata.durationMs)
                  },
                  onSeekPreview = { pos -> requestSeek(pos) },
                  onSetStartAtPlayhead = {
                    applyCommittedEdit { state ->
                      val maxStart = (state.effectiveTrimEndMs - MIN_VIDEO_TRIM_DURATION_MS).coerceAtLeast(0L)
                      state.copy(trimStartMs = playheadMs.coerceIn(0L, maxStart))
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onSetEndAtPlayhead = {
                    applyCommittedEdit { state ->
                      val minEnd = min(currentMetadata.durationMs, state.trimStartMs + MIN_VIDEO_TRIM_DURATION_MS)
                      state.copy(trimEndMs = playheadMs.coerceIn(minEnd, currentMetadata.durationMs))
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onResetTrim = {
                    applyCommittedEdit { state ->
                      state.copy(trimStartMs = 0L, trimEndMs = currentMetadata.durationMs)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                )
              }

              VideoEditorMode.CROP -> {
                val (rotW, rotH) = VideoEditorEngine.computePostRotationDimensions(
                  currentMetadata.displayWidth,
                  currentMetadata.displayHeight,
                  editState.normalizedRotation,
                )
                val cropPixels = VideoEditorEngine.computeEvenCropPixels(editState.cropRect, rotW, rotH)
                val isVertical = isVerticalAspectRatio(
                  preset = editState.aspectPreset,
                  cropRect = editState.cropRect,
                  imageWidth = rotW.toFloat(),
                  imageHeight = rotH.toFloat(),
                )
                VideoCropControls(
                  selectedAspect = editState.aspectPreset,
                  isVertical = isVertical,
                  cropPixelSummary = "${cropPixels.width}×${cropPixels.height}",
                  hasCropChanges = editState.hasCropChanges,
                  onAspectSelected = { preset ->
                    applyCommittedEdit { state ->
                      val nextRect = if (preset.ratio != null) {
                        computeCropForAspectRatio(preset.ratio, rotW.toFloat(), rotH.toFloat())
                      } else {
                        state.cropRect
                      }
                      state.copy(aspectPreset = preset, cropRect = nextRect)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onSwapOrientation = {
                    applyCommittedEdit { state ->
                      val (swappedPreset, swappedRect) = swapCropAspectRatio(
                        preset = state.aspectPreset,
                        cropRect = state.cropRect,
                        imageWidth = rotW.toFloat(),
                        imageHeight = rotH.toFloat(),
                      )
                      state.copy(aspectPreset = swappedPreset, cropRect = swappedRect)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onResetCrop = {
                    applyCommittedEdit { state ->
                      state.copy(
                        aspectPreset = AspectRatioPreset.FREE,
                        cropRect = NormalizedRect(0f, 0f, 1f, 1f),
                      )
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                )
              }

              VideoEditorMode.TRANSFORM -> {
                VideoTransformControls(
                  editState = editState,
                  onRotateBy = { deltaDeg ->
                    applyCommittedEdit { state ->
                      state.copy(rotationDegrees = ((state.rotationDegrees + deltaDeg) % 360 + 360) % 360)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onToggleFlipH = {
                    applyCommittedEdit { state -> state.copy(flipHorizontal = !state.flipHorizontal) }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onToggleFlipV = {
                    applyCommittedEdit { state -> state.copy(flipVertical = !state.flipVertical) }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onResetTransform = {
                    applyCommittedEdit { state ->
                      state.copy(rotationDegrees = 0, flipHorizontal = false, flipVertical = false)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                )
              }

              VideoEditorMode.SPEED -> {
                VideoSpeedAndAudioControls(
                  editState = editState,
                  hasSourceAudio = currentMetadata.hasAudio,
                  onSpeedPresetSelected = { speed ->
                    applyCommittedEdit { state -> state.copy(playbackSpeed = speed) }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onSliderDragStart = { pushUndoSnapshot(editState) },
                  onSpeedLiveChange = { speed ->
                    editState = editState.copy(playbackSpeed = speed)
                  },
                  onToggleMute = {
                    applyCommittedEdit { state -> state.copy(isMuted = !state.isMuted) }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onToggleFade = {
                    applyCommittedEdit { state -> state.copy(fadeInOut = !state.fadeInOut) }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onVolumeLiveChange = { vol ->
                    editState = editState.copy(volume = vol)
                  },
                  onResetSpeedAudio = {
                    applyCommittedEdit { state ->
                      state.copy(
                        playbackSpeed = 1.0f,
                        isMuted = false,
                        volume = 1.0f,
                        fadeInOut = false,
                      )
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                )
              }

              VideoEditorMode.ADJUST -> {
                VideoAdjustControls(
                  editState = editState,
                  onPresetSelected = { preset ->
                    applyCommittedEdit { state -> state.copy(filterPreset = preset) }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  onSliderDragStart = { pushUndoSnapshot(editState) },
                  onBrightnessChange = { b -> editState = editState.copy(brightness = b) },
                  onContrastChange = { c -> editState = editState.copy(contrast = c) },
                  onSaturationChange = { s -> editState = editState.copy(saturation = s) },
                  onResetAdjust = {
                    applyCommittedEdit { state ->
                      state.copy(
                        filterPreset = VideoFilterPreset.NONE,
                        brightness = 0f,
                        contrast = 1f,
                        saturation = 1f,
                      )
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 5. Bottom Mode Selector Row
        VideoModeSelectorRow(
          activeMode = activeMode,
          editState = editState,
          onModeSelected = { mode ->
            activeMode = mode
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          },
        )
      }
    }
  }

  // In-app Video File Browser Dialog (shared StorageFilePickerDialog)
  if (showFilePickerDialog) {
    StorageFilePickerDialog(
      initialDirectory = initialDirectory
        ?: metadata?.file?.parentFile
        ?: File(FileManagerEngine.getUserRootDirectory(), Environment.DIRECTORY_MOVIES)
          .takeIf { it.exists() }
        ?: FileManagerEngine.getUserRootDirectory(),
      onDismiss = { showFilePickerDialog = false },
      onFileSelected = { selected ->
        showFilePickerDialog = false
        loadVideoFile(selected)
      },
      onLaunchSystemPicker = {
        showFilePickerDialog = false
        systemVideoPickerLauncher.launch("video/*")
      },
      title = "Select Video to Edit",
      fileFilter = VideoEditorEngine::isVideoFile,
    )
  }

  // Export Settings Dialog
  if (showExportDialog && currentMetadata != null) {
    ExportVideoDialog(
      metadata = currentMetadata,
      editState = editState,
      initialOptions = exportOptions,
      onDismiss = { showExportDialog = false },
      onConfirmExport = { chosenStem, chosenOptions ->
        exportOptions = chosenOptions
        showExportDialog = false
        isExporting = true
        exportProgress = 0.02f

        val job = coroutineScope.launch {
          val outDir = VideoEditorEngine.getDefaultExportDirectory(context)
          val result = VideoEditorEngine.exportVideo(
            context = context,
            sourceFile = currentMetadata.file,
            outputDir = outDir,
            desiredStem = chosenStem,
            metadata = currentMetadata,
            editState = editState,
            options = chosenOptions,
            onProgress = { pct ->
              exportProgress = pct
            },
          )
          isExporting = false
          exportJob = null

          if (result.success) {
            lastExportResult = result
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
          } else if (!result.cancelled) {
            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
          }
        }
        exportJob = job
      },
    )
  }

  // Live Export Progress Dialog
  if (isExporting) {
    val activeStrategy = VideoEditorEngine.determineStrategy(currentMetadata, editState, exportOptions)
    VideoExportProgressDialog(
      progress = exportProgress,
      strategy = activeStrategy,
      format = exportOptions.format,
      onCancel = {
        exportJob?.cancel()
        exportJob = null
        isExporting = false
        Toast.makeText(context, "Export cancelled", Toast.LENGTH_SHORT).show()
      },
    )
  }

  // Unsaved Changes Confirmation Dialog
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
          text = "Discard Video Edits?",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Text(
          text = "You have unexported edits on this clip. Are you sure you want to discard them?",
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
}

// ---------------------------------------------------------------------------
// Top Header Bar
// ---------------------------------------------------------------------------

@Composable
private fun VideoEditorTopBar(
  metadata: VideoMetadata?,
  canUndo: Boolean,
  canRedo: Boolean,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onClose: () -> Unit,
  onExportClick: () -> Unit,
  onNavigateBack: (() -> Unit)? = null,
) {
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          if (onNavigateBack != null) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier
                .size(36.dp)
                .testTag("action_navigate_back"),
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
          } else {
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
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Video Editor",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = if (metadata != null) {
                "${metadata.file.name} • ${metadata.resolutionLabel} • ${metadata.durationFormatted}"
              } else {
                "Fast FFmpeg video trimmer & editor"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        if (metadata != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            IconButton(
              onClick = onUndo,
              enabled = canUndo,
              modifier = Modifier
                .size(38.dp)
                .testTag("editor_undo_button"),
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
                .testTag("editor_redo_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                modifier = Modifier.size(20.dp),
              )
            }
            IconButton(
              onClick = onClose,
              modifier = Modifier
                .size(38.dp)
                .testTag("editor_close_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Video",
                modifier = Modifier.size(20.dp),
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            FilledIconButton(
              onClick = onExportClick,
              shape = CircleShape,
              modifier = Modifier
                .size(36.dp)
                .testTag("editor_export_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save Video",
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Live GPU Video Preview Viewport
// ---------------------------------------------------------------------------

@Composable
private fun VideoPreviewViewport(
  metadata: VideoMetadata,
  editState: VideoEditState,
  activeMode: VideoEditorMode,
  strategy: VideoProcessingStrategy,
  isPlaying: Boolean,
  loopTrimPlayback: Boolean,
  playheadMs: Long,
  seekTargetMs: Long,
  seekRequestToken: Long,
  isInspectionMode: Boolean,
  onTogglePlayPause: () -> Unit,
  onPlayheadProgress: (Long) -> Unit,
  onPlaybackReachedEnd: () -> Unit,
  onCropRectPreviewChange: (NormalizedRect) -> Unit,
  onCropDragStart: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current
  val currentEditState by rememberUpdatedState(editState)
  val currentIsPlaying by rememberUpdatedState(isPlaying)
  val currentOnPlayheadProgress by rememberUpdatedState(onPlayheadProgress)
  val currentOnPlaybackReachedEnd by rememberUpdatedState(onPlaybackReachedEnd)

  var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
  var isPlayerPrepared by remember(metadata.file.absolutePath) { mutableStateOf(false) }
  var textureViewRef by remember { mutableStateOf<TextureView?>(null) }

  // Apply real-time GPU ColorMatrix filter whenever color adjustments change
  val previewColorMatrix = remember(
    editState.filterPreset,
    editState.brightness,
    editState.contrast,
    editState.saturation,
  ) {
    VideoEditorEngine.buildPreviewColorMatrix(editState)
  }

  LaunchedEffect(textureViewRef, previewColorMatrix, editState.hasColorChanges) {
    val tv = textureViewRef ?: return@LaunchedEffect
    if (editState.hasColorChanges) {
      val paint = AndroidPaint().apply {
        colorFilter = ColorMatrixColorFilter(previewColorMatrix)
      }
      tv.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
    } else {
      tv.setLayerType(View.LAYER_TYPE_NONE, null)
    }
    tv.invalidate()
  }

  // Sync play/pause, speed, and audio mute/volume to MediaPlayer
  LaunchedEffect(
    mediaPlayerRef,
    isPlayerPrepared,
    isPlaying,
    editState.playbackSpeed,
    editState.isMuted,
    editState.volume,
  ) {
    val mp = mediaPlayerRef ?: return@LaunchedEffect
    if (!isPlayerPrepared) return@LaunchedEffect

    val vol = if (editState.isMuted) 0f else editState.volume.coerceIn(0f, 1.5f)
    try {
      mp.setVolume(vol, vol)
    } catch (_: Throwable) {}

    try {
      if (isPlaying) {
        val params = PlaybackParams().setSpeed(editState.playbackSpeed.coerceIn(0.25f, 3.0f))
        mp.playbackParams = params
        if (!mp.isPlaying) {
          mp.start()
        }
      } else if (mp.isPlaying) {
        mp.pause()
      }
    } catch (_: Throwable) {
      try {
        if (isPlaying && !mp.isPlaying) mp.start()
        else if (!isPlaying && mp.isPlaying) mp.pause()
      } catch (_: Throwable) {}
    }
  }

  // Handle explicit seeks from timeline / trim sliders
  LaunchedEffect(mediaPlayerRef, isPlayerPrepared, seekRequestToken) {
    val mp = mediaPlayerRef ?: return@LaunchedEffect
    if (!isPlayerPrepared) return@LaunchedEffect
    try {
      mp.seekTo(seekTargetMs.toInt())
    } catch (_: Throwable) {}
  }

  // High-frequency playhead ticker & trim-window enforcement while playing
  LaunchedEffect(mediaPlayerRef, isPlayerPrepared, isPlaying, loopTrimPlayback) {
    val mp = mediaPlayerRef ?: return@LaunchedEffect
    if (!isPlayerPrepared || !isPlaying) return@LaunchedEffect
    while (isActive && currentIsPlaying) {
      try {
        if (mp.isPlaying) {
          val pos = mp.currentPosition.toLong().coerceAtLeast(0L)
          val endMs = currentEditState.effectiveTrimEndMs
          val startMs = currentEditState.trimStartMs
          if (endMs > 0L && pos >= endMs) {
            if (loopTrimPlayback) {
              mp.seekTo(startMs.toInt())
              currentOnPlayheadProgress(startMs)
            } else {
              mp.pause()
              currentOnPlaybackReachedEnd()
            }
          } else {
            currentOnPlayheadProgress(pos)
          }
        }
      } catch (_: Throwable) {}
      delay(33L)
    }
  }

  DisposableEffect(metadata.file.absolutePath) {
    onDispose {
      isPlayerPrepared = false
      mediaPlayerRef?.let { mp ->
        try {
          mp.stop()
        } catch (_: Throwable) {}
        try {
          mp.release()
        } catch (_: Throwable) {}
      }
      mediaPlayerRef = null
    }
  }

  Surface(
    modifier = modifier.testTag("video_preview_viewport"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      contentAlignment = Alignment.Center,
    ) {
      val availW = maxWidth
      val availH = maxHeight
      val availWPx = with(density) { availW.toPx() }
      val availHPx = with(density) { availH.toPx() }

      val srcW = metadata.displayWidth.coerceAtLeast(2)
      val srcH = metadata.displayHeight.coerceAtLeast(2)
      val normRot = editState.normalizedRotation
      val (rotW, rotH) = VideoEditorEngine.computePostRotationDimensions(srcW, srcH, normRot)
      val rotatedAspect = rotW.toFloat() / rotH.toFloat()

      val (outerWPx, outerHPx) = if (availWPx > 0f && availHPx > 0f) {
        if (availWPx / availHPx > rotatedAspect) {
          (availHPx * rotatedAspect) to availHPx
        } else {
          availWPx to (availWPx / rotatedAspect)
        }
      } else {
        320f to 180f
      }

      val outerW = with(density) { outerWPx.toDp() }
      val outerH = with(density) { outerHPx.toDp() }
      // Before rotation, inner TextureView needs swapped dimensions when rotated 90/270 so it fills outerW x outerH cleanly after rotationZ
      val innerW = if (normRot == 90 || normRot == 270) outerH else outerW
      val innerH = if (normRot == 90 || normRot == 270) outerW else outerH

      Box(
        modifier = Modifier
          .size(outerW, outerH)
          .clip(RoundedCornerShape(8.dp))
          .clipToBounds()
          .let { base ->
            if (activeMode != VideoEditorMode.CROP) {
              base.pointerInput(Unit) {
                detectTapGestures(onTap = { onTogglePlayPause() })
              }
            } else {
              base
            }
          },
        contentAlignment = Alignment.Center,
      ) {
        // Post-rotation flip wrapper -> inner rotated video surface
        Box(
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
              scaleX = if (editState.flipHorizontal) -1f else 1f
              scaleY = if (editState.flipVertical) -1f else 1f
            },
          contentAlignment = Alignment.Center,
        ) {
          if (!isInspectionMode && metadata.file.exists()) {
            AndroidView(
              factory = { ctx ->
                TextureView(ctx).apply {
                  isOpaque = false
                  surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                      surfaceTexture: SurfaceTexture,
                      width: Int,
                      height: Int,
                    ) {
                      val surface = AndroidSurface(surfaceTexture)
                      val mp = MediaPlayer()
                      try {
                        mp.setDataSource(metadata.file.absolutePath)
                        mp.setSurface(surface)
                        mp.setOnPreparedListener { preparedMp ->
                          mediaPlayerRef = preparedMp
                          isPlayerPrepared = true
                          val initialSeek = playheadMs.coerceIn(
                            currentEditState.trimStartMs,
                            currentEditState.effectiveTrimEndMs,
                          )
                          try {
                            preparedMp.seekTo(initialSeek.toInt())
                          } catch (_: Throwable) {}
                        }
                        mp.setOnCompletionListener {
                          currentOnPlaybackReachedEnd()
                        }
                        mp.prepareAsync()
                      } catch (_: Throwable) {
                        surface.release()
                      }
                    }

                    override fun onSurfaceTextureSizeChanged(
                      surface: SurfaceTexture,
                      width: Int,
                      height: Int,
                    ) = Unit

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                      isPlayerPrepared = false
                      mediaPlayerRef?.let { mp ->
                        try {
                          mp.setSurface(null)
                          mp.release()
                        } catch (_: Throwable) {}
                      }
                      mediaPlayerRef = null
                      return true
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                  }
                  textureViewRef = this
                }
              },
              modifier = Modifier
                .size(innerW, innerH)
                .graphicsLayer {
                  rotationZ = normRot.toFloat()
                },
            )
          } else {
            // Clean preview placeholder for Compose @Preview and synthetic UI tests
            Box(
              modifier = Modifier
                .size(innerW, innerH)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Default.Movie,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${metadata.resolutionLabel} • ${metadata.durationFormatted}",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }

        // Interactive Crop Overlay in CROP mode, or subtle crop mask preview in other modes
        if (activeMode == VideoEditorMode.CROP) {
          var dragSessionActive by remember { mutableStateOf(false) }
          CropOverlay(
            cropRect = editState.cropRect,
            onCropRectChange = { updated ->
              if (!dragSessionActive) {
                dragSessionActive = true
                onCropDragStart()
              }
              onCropRectPreviewChange(updated)
            },
            containerSize = Size(outerWPx, outerHPx),
            lockedAspectRatio = editState.aspectPreset.ratio,
            imageWidth = rotW.toFloat(),
            imageHeight = rotH.toFloat(),
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(Unit) {
                awaitPointerEventScope {
                  while (true) {
                    val event = awaitPointerEvent()
                    if (event.changes.none { it.pressed }) {
                      dragSessionActive = false
                    }
                  }
                }
              }
              .testTag("video_crop_overlay"),
          )
        } else if (editState.hasCropChanges) {
          val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)
          val frameColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val l = editState.cropRect.left * w
            val t = editState.cropRect.top * h
            val r = editState.cropRect.right * w
            val b = editState.cropRect.bottom * h
            drawRect(scrimColor, Offset.Zero, Size(w, t))
            drawRect(scrimColor, Offset(0f, b), Size(w, h - b))
            drawRect(scrimColor, Offset(0f, t), Size(l, b - t))
            drawRect(scrimColor, Offset(r, t), Size(w - r, b - t))
            drawRect(
              color = frameColor,
              topLeft = Offset(l, t),
              size = Size(r - l, b - t),
              style = Stroke(width = 1.5.dp.toPx()),
            )
          }
        }
      }

      // Strategy & performance badge in top-left of viewport
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(4.dp)
          .testTag("strategy_badge"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = if (strategy == VideoProcessingStrategy.STREAM_COPY) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.secondary
            },
            modifier = Modifier.size(13.dp),
          )
          Text(
            text = strategy.badgeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Interactive Filmstrip Timeline & Transport Bar
// ---------------------------------------------------------------------------

private enum class TimelineDragTarget {
  START_HANDLE,
  END_HANDLE,
  PLAYHEAD,
}

@Composable
private fun VideoFilmstripTimeline(
  durationMs: Long,
  trimStartMs: Long,
  trimEndMs: Long,
  playheadMs: Long,
  thumbnails: List<Bitmap>,
  onScrubPlayhead: (Long) -> Unit,
  onTrimGestureStart: () -> Unit,
  onTrimRangeChange: (Long, Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val safeDuration = durationMs.coerceAtLeast(1L)
  val currentTrimStart by rememberUpdatedState(trimStartMs)
  val currentTrimEnd by rememberUpdatedState(trimEndMs)
  val currentOnScrub by rememberUpdatedState(onScrubPlayhead)
  val currentOnTrimChange by rememberUpdatedState(onTrimRangeChange)

  val primaryColor = MaterialTheme.colorScheme.primary
  val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
  val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.58f)
  val playheadColor = MaterialTheme.colorScheme.tertiary
  val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(50.dp)
      .testTag("video_filmstrip_timeline"),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(12.dp))
        .pointerInput(safeDuration) {
          detectTapGestures { offset ->
            if (size.width > 0) {
              val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
              val targetMs = (frac * safeDuration).roundToLong()
              currentOnScrub(targetMs)
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
          }
        }
        .pointerInput(safeDuration) {
          var dragTarget = TimelineDragTarget.PLAYHEAD
          val handleHitPx = 28.dp.toPx()

          detectHorizontalDragGestures(
            onDragStart = { startOffset ->
              if (size.width > 0) {
                val w = size.width.toFloat()
                val startX = (currentTrimStart.toFloat() / safeDuration) * w
                val endX = (currentTrimEnd.toFloat() / safeDuration) * w
                val distStart = abs(startOffset.x - startX)
                val distEnd = abs(startOffset.x - endX)

                dragTarget = when {
                  distStart <= handleHitPx && distStart <= distEnd -> {
                    onTrimGestureStart()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    TimelineDragTarget.START_HANDLE
                  }
                  distEnd <= handleHitPx -> {
                    onTrimGestureStart()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    TimelineDragTarget.END_HANDLE
                  }
                  else -> TimelineDragTarget.PLAYHEAD
                }
              }
            },
            onHorizontalDrag = { change, dragAmount ->
              change.consume()
              val w = size.width.toFloat()
              if (w <= 0f) return@detectHorizontalDragGestures
              val deltaMs = ((dragAmount / w) * safeDuration).roundToLong()
              val minClip = min(MIN_VIDEO_TRIM_DURATION_MS, safeDuration)

              when (dragTarget) {
                TimelineDragTarget.START_HANDLE -> {
                  val newStart = (currentTrimStart + deltaMs)
                    .coerceIn(0L, (currentTrimEnd - minClip).coerceAtLeast(0L))
                  currentOnTrimChange(newStart, currentTrimEnd)
                  currentOnScrub(newStart)
                }
                TimelineDragTarget.END_HANDLE -> {
                  val newEnd = (currentTrimEnd + deltaMs)
                    .coerceIn(min(safeDuration, currentTrimStart + minClip), safeDuration)
                  currentOnTrimChange(currentTrimStart, newEnd)
                  currentOnScrub(newEnd)
                }
                TimelineDragTarget.PLAYHEAD -> {
                  val frac = (change.position.x / w).coerceIn(0f, 1f)
                  currentOnScrub((frac * safeDuration).roundToLong())
                }
              }
            },
          )
        },
    ) {
      if (thumbnails.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxSize()) {
          thumbnails.forEach { bmp ->
            Image(
              bitmap = bmp.asImageBitmap(),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            )
          }
        }
      }

      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        if (thumbnails.isEmpty()) {
          val tickCount = 24
          val step = w / tickCount
          for (i in 1 until tickCount) {
            val tickH = if (i % 4 == 0) h * 0.55f else h * 0.3f
            drawLine(
              color = tickColor,
              start = Offset(i * step, (h - tickH) / 2f),
              end = Offset(i * step, (h + tickH) / 2f),
              strokeWidth = 1.5.dp.toPx(),
            )
          }
        }

        val startX = (trimStartMs.toFloat() / safeDuration).coerceIn(0f, 1f) * w
        val endX = (trimEndMs.toFloat() / safeDuration).coerceIn(0f, 1f) * w
        val playX = (playheadMs.toFloat() / safeDuration).coerceIn(0f, 1f) * w

        // Dim excluded timeline segments
        if (startX > 0f) {
          drawRect(scrimColor, Offset.Zero, Size(startX, h))
        }
        if (endX < w) {
          drawRect(scrimColor, Offset(endX, 0f), Size(w - endX, h))
        }

        // Trim window border & side handles
        val handleW = 10.dp.toPx()
        drawRoundRect(
          color = primaryColor,
          topLeft = Offset(startX, 1.dp.toPx()),
          size = Size((endX - startX).coerceAtLeast(4.dp.toPx()), h - 2.dp.toPx()),
          cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
          style = Stroke(width = 2.5.dp.toPx()),
        )

        // Left & Right solid grab handles
        drawRoundRect(
          color = primaryColor,
          topLeft = Offset(startX, 0f),
          size = Size(handleW, h),
          cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        )
        drawLine(
          color = onPrimaryColor,
          start = Offset(startX + handleW / 2f, h * 0.3f),
          end = Offset(startX + handleW / 2f, h * 0.7f),
          strokeWidth = 1.8.dp.toPx(),
        )

        val rightHandleLeft = (endX - handleW).coerceAtLeast(0f)
        drawRoundRect(
          color = primaryColor,
          topLeft = Offset(rightHandleLeft, 0f),
          size = Size(handleW, h),
          cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        )
        drawLine(
          color = onPrimaryColor,
          start = Offset(rightHandleLeft + handleW / 2f, h * 0.3f),
          end = Offset(rightHandleLeft + handleW / 2f, h * 0.7f),
          strokeWidth = 1.8.dp.toPx(),
        )

        // Playhead vertical line + top pill indicator
        drawLine(
          color = playheadColor,
          start = Offset(playX, 0f),
          end = Offset(playX, h),
          strokeWidth = 2.5.dp.toPx(),
        )
        drawCircle(
          color = playheadColor,
          radius = 5.dp.toPx(),
          center = Offset(playX, 6.dp.toPx()),
        )
      }
    }
  }
}

@Composable
private fun VideoTransportRow(
  isPlaying: Boolean,
  loopTrimPlayback: Boolean,
  playheadMs: Long,
  editState: VideoEditState,
  hasAnyChanges: Boolean,
  onTogglePlayPause: () -> Unit,
  onToggleLoop: () -> Unit,
  onCaptureFrame: () -> Unit,
  onShareVideo: () -> Unit,
  onOpenVideo: () -> Unit,
  onResetAll: () -> Unit,
) {
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
          .testTag("play_pause_button"),
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = if (isPlaying) "Pause" else "Play",
          modifier = Modifier.size(20.dp),
        )
      }

      FilledTonalIconToggleButton(
        checked = loopTrimPlayback,
        onCheckedChange = { onToggleLoop() },
        modifier = Modifier
          .size(34.dp)
          .testTag("loop_trim_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Repeat,
          contentDescription = "Loop trimmed range",
          modifier = Modifier.size(17.dp),
        )
      }

      Column {
        Text(
          text = "${VideoEditorEngine.formatTimecode(playheadMs)} / ${VideoEditorEngine.formatTimecode(editState.effectiveTrimEndMs)}",
          style = MaterialTheme.typography.labelMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Out ${VideoEditorEngine.formatTimecode(editState.estimatedOutputDurationMs)} • ${String.format(Locale.US, "%.2g×", editState.playbackSpeed)}",
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
        onClick = onCaptureFrame,
        modifier = Modifier
          .size(36.dp)
          .testTag("capture_frame_button"),
      ) {
        Icon(
          imageVector = Icons.Default.CameraAlt,
          contentDescription = "Save Frame Snapshot",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(19.dp),
        )
      }
      IconButton(
        onClick = onShareVideo,
        modifier = Modifier
          .size(36.dp)
          .testTag("editor_share_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = "Share Video",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(19.dp),
        )
      }
      IconButton(
        onClick = onOpenVideo,
        modifier = Modifier
          .size(36.dp)
          .testTag("editor_open_button"),
      ) {
        Icon(
          imageVector = Icons.Default.FolderOpen,
          contentDescription = "Open Another Video",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(19.dp),
        )
      }
      IconButton(
        onClick = onResetAll,
        enabled = hasAnyChanges,
        modifier = Modifier
          .size(36.dp)
          .testTag("editor_reset_button"),
      ) {
        Icon(
          imageVector = Icons.Default.RestartAlt,
          contentDescription = "Reset All Edits",
          modifier = Modifier.size(19.dp),
        )
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Mode Control Panels (Trim, Crop, Transform, Speed, Filters)
// ---------------------------------------------------------------------------

@Composable
private fun VideoTrimControls(
  durationMs: Long,
  editState: VideoEditState,
  playheadMs: Long,
  onSliderDragStart: () -> Unit,
  onTrimLiveChange: (Long, Long) -> Unit,
  onSeekPreview: (Long) -> Unit,
  onSetStartAtPlayhead: () -> Unit,
  onSetEndAtPlayhead: () -> Unit,
  onResetTrim: () -> Unit,
) {
  val safeDuration = durationMs.coerceAtLeast(MIN_VIDEO_TRIM_DURATION_MS)
  val minClip = min(MIN_VIDEO_TRIM_DURATION_MS, safeDuration)
  var startDragActive by remember { mutableStateOf(false) }
  var endDragActive by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Start ${VideoEditorEngine.formatTimecode(editState.trimStartMs)}",
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(98.dp),
      )
      SlimDotSlider(
        value = editState.trimStartMs.toFloat(),
        onValueChange = { raw ->
          if (!startDragActive) {
            startDragActive = true
            onSliderDragStart()
          }
          val clamped = raw.roundToLong().coerceIn(0L, (editState.effectiveTrimEndMs - minClip).coerceAtLeast(0L))
          onTrimLiveChange(clamped, editState.effectiveTrimEndMs)
          onSeekPreview(clamped)
        },
        onValueChangeFinished = { startDragActive = false },
        valueRange = 0f..safeDuration.toFloat(),
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
        text = "End   ${VideoEditorEngine.formatTimecode(editState.effectiveTrimEndMs)}",
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(98.dp),
      )
      SlimDotSlider(
        value = editState.effectiveTrimEndMs.toFloat(),
        onValueChange = { raw ->
          if (!endDragActive) {
            endDragActive = true
            onSliderDragStart()
          }
          val minEnd = min(safeDuration, editState.trimStartMs + minClip)
          val clamped = raw.roundToLong().coerceIn(minEnd, safeDuration)
          onTrimLiveChange(editState.trimStartMs, clamped)
          onSeekPreview(clamped)
        },
        onValueChangeFinished = { endDragActive = false },
        valueRange = 0f..safeDuration.toFloat(),
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
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalButton(
        onClick = onSetStartAtPlayhead,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier
          .height(34.dp)
          .testTag("trim_set_start_button"),
      ) {
        Text("Set Start (${VideoEditorEngine.formatTimecode(playheadMs)})", style = MaterialTheme.typography.labelSmall)
      }

      FilledTonalButton(
        onClick = onSetEndAtPlayhead,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier
          .height(34.dp)
          .testTag("trim_set_end_button"),
      ) {
        Text("Set End", style = MaterialTheme.typography.labelSmall)
      }

      OutlinedButton(
        onClick = onResetTrim,
        enabled = editState.hasTrimChanges,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier
          .height(34.dp)
          .testTag("trim_reset_button"),
      ) {
        Text("Reset Trim", style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Composable
private fun VideoCropControls(
  selectedAspect: AspectRatioPreset,
  isVertical: Boolean,
  cropPixelSummary: String,
  hasCropChanges: Boolean,
  onAspectSelected: (AspectRatioPreset) -> Unit,
  onSwapOrientation: () -> Unit,
  onResetCrop: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      AspectRatioPreset.entries.forEach { preset ->
        FilterChip(
          selected = selectedAspect == preset,
          onClick = { onAspectSelected(preset) },
          label = { Text(preset.label, style = MaterialTheme.typography.bodySmall) },
          shape = CircleShape,
          modifier = Modifier.testTag("crop_preset_${preset.name.lowercase(Locale.ROOT)}"),
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilledTonalIconToggleButton(
          checked = isVertical,
          onCheckedChange = { onSwapOrientation() },
          modifier = Modifier
            .size(36.dp)
            .testTag("crop_orientation_toggle"),
        ) {
          Icon(
            imageVector = if (isVertical) Icons.Default.CropPortrait else Icons.Default.CropLandscape,
            contentDescription = "Swap aspect orientation",
            modifier = Modifier.size(18.dp),
          )
        }
        Text(
          text = "Frame: $cropPixelSummary px",
          style = MaterialTheme.typography.labelMedium,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      OutlinedButton(
        onClick = onResetCrop,
        enabled = hasCropChanges || selectedAspect != AspectRatioPreset.FREE,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        modifier = Modifier
          .height(34.dp)
          .testTag("crop_reset_button"),
      ) {
        Text("Reset Crop", style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Composable
private fun VideoTransformControls(
  editState: VideoEditState,
  onRotateBy: (Int) -> Unit,
  onToggleFlipH: () -> Unit,
  onToggleFlipV: () -> Unit,
  onResetTransform: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalIconButton(
        onClick = { onRotateBy(-90) },
        modifier = Modifier.testTag("rotate_ccw_button"),
      ) {
        Icon(Icons.AutoMirrored.Filled.RotateLeft, contentDescription = "Rotate 90° Counter-Clockwise")
      }
      FilledTonalIconButton(
        onClick = { onRotateBy(90) },
        modifier = Modifier.testTag("rotate_cw_button"),
      ) {
        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate 90° Clockwise")
      }
      FilledTonalIconButton(
        onClick = { onRotateBy(180) },
        modifier = Modifier.testTag("rotate_180_button"),
      ) {
        Icon(Icons.Default.Refresh, contentDescription = "Rotate 180°")
      }
      FilledTonalIconToggleButton(
        checked = editState.flipHorizontal,
        onCheckedChange = { onToggleFlipH() },
        modifier = Modifier.testTag("flip_h_button"),
      ) {
        Icon(Icons.Default.Flip, contentDescription = "Flip Horizontally")
      }
      FilledTonalIconToggleButton(
        checked = editState.flipVertical,
        onCheckedChange = { onToggleFlipV() },
        modifier = Modifier.testTag("flip_v_button"),
      ) {
        Icon(Icons.Default.SwapVert, contentDescription = "Flip Vertically")
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val activeParts = buildList {
        add("${editState.normalizedRotation}°")
        if (editState.flipHorizontal) add("Flip H")
        if (editState.flipVertical) add("Flip V")
      }
      Text(
        text = "Orientation: ${activeParts.joinToString(" • ")}",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      OutlinedButton(
        onClick = onResetTransform,
        enabled = editState.hasTransformChanges,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        modifier = Modifier
          .height(34.dp)
          .testTag("transform_reset_button"),
      ) {
        Text("Reset", style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Composable
private fun VideoSpeedAndAudioControls(
  editState: VideoEditState,
  hasSourceAudio: Boolean,
  onSpeedPresetSelected: (Float) -> Unit,
  onSliderDragStart: () -> Unit,
  onSpeedLiveChange: (Float) -> Unit,
  onToggleMute: () -> Unit,
  onToggleFade: () -> Unit,
  onVolumeLiveChange: (Float) -> Unit,
  onResetSpeedAudio: () -> Unit,
) {
  val presets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
  var speedDragActive by remember { mutableStateOf(false) }
  var volDragActive by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      presets.forEach { preset ->
        val label = if (preset == 1.0f) "1×" else "${preset}×"
        FilterChip(
          selected = abs(editState.playbackSpeed - preset) < 0.02f,
          onClick = { onSpeedPresetSelected(preset) },
          label = { Text(label, style = MaterialTheme.typography.labelSmall) },
          shape = CircleShape,
          modifier = Modifier.testTag("speed_chip_${preset}"),
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Speed ${String.format(Locale.US, "%.2f×", editState.playbackSpeed)}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(92.dp),
      )
      SlimDotSlider(
        value = editState.playbackSpeed,
        onValueChange = { raw ->
          if (!speedDragActive) {
            speedDragActive = true
            onSliderDragStart()
          }
          onSpeedLiveChange((raw * 20f).roundToInt() / 20f)
        },
        onValueChangeFinished = { speedDragActive = false },
        valueRange = 0.25f..3.0f,
        modifier = Modifier
          .weight(1f)
          .testTag("speed_slider"),
      )
    }

    if (hasSourceAudio) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (editState.isMuted) "Muted" else "Vol ${(editState.volume * 100).roundToInt()}%",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.width(92.dp),
        )
        SlimDotSlider(
          value = if (editState.isMuted) 0f else editState.volume,
          onValueChange = { raw ->
            if (!volDragActive) {
              volDragActive = true
              onSliderDragStart()
            }
            onVolumeLiveChange((raw * 20f).roundToInt() / 20f)
          },
          onValueChangeFinished = { volDragActive = false },
          valueRange = 0f..2.0f,
          enabled = !editState.isMuted,
          modifier = Modifier
            .weight(1f)
            .testTag("volume_slider"),
        )
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilterChip(
        selected = editState.isMuted,
        onClick = onToggleMute,
        leadingIcon = {
          Icon(
            imageVector = if (editState.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        },
        label = { Text("Mute Audio", style = MaterialTheme.typography.labelSmall) },
        shape = CircleShape,
        modifier = Modifier.testTag("mute_audio_chip"),
      )

      FilterChip(
        selected = editState.fadeInOut,
        onClick = onToggleFade,
        label = { Text("Fade In/Out", style = MaterialTheme.typography.labelSmall) },
        shape = CircleShape,
        modifier = Modifier.testTag("fade_in_out_chip"),
      )

      OutlinedButton(
        onClick = onResetSpeedAudio,
        enabled = editState.hasSpeedChanges || editState.isMuted || abs(editState.volume - 1f) > 0.01f || editState.fadeInOut,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier
          .height(32.dp)
          .testTag("speed_reset_button"),
      ) {
        Text("Reset", style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Composable
private fun VideoAdjustControls(
  editState: VideoEditState,
  onPresetSelected: (VideoFilterPreset) -> Unit,
  onSliderDragStart: () -> Unit,
  onBrightnessChange: (Float) -> Unit,
  onContrastChange: (Float) -> Unit,
  onSaturationChange: (Float) -> Unit,
  onResetAdjust: () -> Unit,
) {
  var bDrag by remember { mutableStateOf(false) }
  var cDrag by remember { mutableStateOf(false) }
  var sDrag by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      VideoFilterPreset.entries.forEach { preset ->
        FilterChip(
          selected = editState.filterPreset == preset,
          onClick = { onPresetSelected(preset) },
          label = { Text(preset.label, style = MaterialTheme.typography.labelSmall) },
          shape = CircleShape,
          modifier = Modifier.testTag("filter_preset_${preset.name.lowercase(Locale.ROOT)}"),
        )
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "Bright ${editState.brightness.roundToInt()}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(96.dp),
      )
      SlimDotSlider(
        value = editState.brightness,
        onValueChange = {
          if (!bDrag) {
            bDrag = true
            onSliderDragStart()
          }
          onBrightnessChange(it)
        },
        onValueChangeFinished = { bDrag = false },
        valueRange = -100f..100f,
        modifier = Modifier
          .weight(1f)
          .testTag("brightness_slider"),
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "Contrast ${String.format(Locale.US, "%.2f", editState.contrast)}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(96.dp),
      )
      SlimDotSlider(
        value = editState.contrast,
        onValueChange = {
          if (!cDrag) {
            cDrag = true
            onSliderDragStart()
          }
          onContrastChange(it)
        },
        onValueChangeFinished = { cDrag = false },
        valueRange = 0.5f..1.5f,
        modifier = Modifier
          .weight(1f)
          .testTag("contrast_slider"),
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "Saturate ${String.format(Locale.US, "%.2f", editState.saturation)}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(96.dp),
      )
      SlimDotSlider(
        value = editState.saturation,
        onValueChange = {
          if (!sDrag) {
            sDrag = true
            onSliderDragStart()
          }
          onSaturationChange(it)
        },
        onValueChangeFinished = { sDrag = false },
        valueRange = 0f..2.0f,
        modifier = Modifier
          .weight(1f)
          .testTag("saturation_slider"),
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
    ) {
      OutlinedButton(
        onClick = onResetAdjust,
        enabled = editState.hasColorChanges,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        modifier = Modifier
          .height(32.dp)
          .testTag("adjust_reset_button"),
      ) {
        Text("Reset Filters", style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Composable
private fun VideoModeSelectorRow(
  activeMode: VideoEditorMode,
  editState: VideoEditState,
  onModeSelected: (VideoEditorMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  val modes = VideoEditorMode.entries
  SingleChoiceSegmentedButtonRow(
    modifier = modifier
      .fillMaxWidth()
      .padding(bottom = 4.dp)
      .testTag("mode_selector_row"),
  ) {
    modes.forEachIndexed { index, mode ->
      val hasActiveEdits = when (mode) {
        VideoEditorMode.TRIM -> editState.hasTrimChanges
        VideoEditorMode.CROP -> editState.hasCropChanges
        VideoEditorMode.TRANSFORM -> editState.hasTransformChanges
        VideoEditorMode.SPEED -> editState.hasSpeedChanges || editState.isMuted || abs(editState.volume - 1f) > 0.01f || editState.fadeInOut
        VideoEditorMode.ADJUST -> editState.hasColorChanges
      }
      SegmentedButton(
        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
        onClick = { onModeSelected(mode) },
        selected = activeMode == mode,
        icon = {},
        modifier = Modifier.testTag("mode_tab_${mode.name.lowercase(Locale.ROOT)}"),
        label = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
          ) {
            val icon = when (mode) {
              VideoEditorMode.TRIM -> Icons.Default.ContentCut
              VideoEditorMode.CROP -> Icons.Default.Crop
              VideoEditorMode.TRANSFORM -> Icons.AutoMirrored.Filled.RotateRight
              VideoEditorMode.SPEED -> Icons.Default.Speed
              VideoEditorMode.ADJUST -> Icons.Default.Tune
            }
            Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (hasActiveEdits) "${mode.label}•" else mode.label,
              style = MaterialTheme.typography.labelSmall,
              maxLines = 1,
            )
          }
        },
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Recent Export Banner, Export Dialog, Progress Dialog & File Browser Dialog
// ---------------------------------------------------------------------------

@Composable
private fun RecentVideoExportBanner(
  result: VideoExportResult,
  onShare: () -> Unit,
  onOpenEdited: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val outFile = result.outputFile ?: return
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("recent_export_banner"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f),
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = outFile.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${FileManagerEngine.formatFileSize(result.outputSizeBytes)} • ${result.strategyUsed.badgeLabel}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (VideoEditorEngine.isVideoFile(outFile)) {
          TextButton(onClick = onOpenEdited) {
            Text("Edit", style = MaterialTheme.typography.labelMedium)
          }
        }
        IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share exported file",
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(18.dp),
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss",
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun ExportVideoDialog(
  metadata: VideoMetadata,
  editState: VideoEditState,
  initialOptions: VideoExportOptions,
  onDismiss: () -> Unit,
  onConfirmExport: (String, VideoExportOptions) -> Unit,
) {
  val defaultStem = remember(metadata.file.name) {
    "${VideoEditorEngine.extractFileStem(metadata.file.name)}_edited"
  }
  var fileNameField by remember {
    mutableStateOf(
      TextFieldValue(
        text = defaultStem,
        selection = TextRange(0, defaultStem.length),
      )
    )
  }
  var options by remember { mutableStateOf(initialOptions) }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    delay(80)
    try {
      focusRequester.requestFocus()
    } catch (_: Throwable) {}
  }

  val strategy = VideoEditorEngine.determineStrategy(metadata, editState, options)
  val audioExportBlocked = options.format == VideoExportFormat.M4A && editState.isMuted

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    icon = {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(44.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.SaveAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(22.dp),
          )
        }
      }
    },
    title = {
      Text(
        text = "Export Edited Media",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Context & Performance Strategy Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = strategy.badgeLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            Text(
              text = "${strategy.description} • Output ~${VideoEditorEngine.formatTimecode(editState.estimatedOutputDurationMs)}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Output Filename Stem
        OutlinedTextField(
          value = fileNameField,
          onValueChange = { fileNameField = it },
          label = { Text("Output file name") },
          suffix = { Text(".${options.format.extension}") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("export_filename_input"),
        )

        // Format Selection
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Format",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            VideoExportFormat.entries.forEach { fmt ->
              FilterChip(
                selected = options.format == fmt,
                onClick = { options = options.copy(format = fmt) },
                label = { Text(fmt.displayName, style = MaterialTheme.typography.labelSmall) },
                shape = CircleShape,
                modifier = Modifier.testTag("export_format_${fmt.extension}"),
              )
            }
          }
        }

        // Resolution Selection (for MP4 / GIF)
        if (options.format != VideoExportFormat.M4A) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Resolution",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              VideoResolutionPreset.entries.forEach { res ->
                FilterChip(
                  selected = options.resolution == res,
                  onClick = { options = options.copy(resolution = res) },
                  label = { Text(res.label, style = MaterialTheme.typography.labelSmall) },
                  shape = CircleShape,
                  modifier = Modifier.testTag("export_res_${res.name.lowercase(Locale.ROOT)}"),
                )
              }
            }
          }
        }

        // Quality Selection (when re-encoding MP4)
        if (options.format == VideoExportFormat.MP4 && strategy != VideoProcessingStrategy.STREAM_COPY) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Encoding Quality",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              VideoQualityPreset.entries.forEach { q ->
                FilterChip(
                  selected = options.quality == q,
                  onClick = { options = options.copy(quality = q) },
                  label = { Text(q.label, style = MaterialTheme.typography.labelSmall) },
                  shape = CircleShape,
                  modifier = Modifier.testTag("export_quality_${q.name.lowercase(Locale.ROOT)}"),
                )
              }
            }
          }
        }

        if (audioExportBlocked) {
          Text(
            text = "Unmute audio in the Speed tab before exporting Audio Only (M4A).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirmExport(fileNameField.text.ifBlank { "edited_video" }, options)
        },
        enabled = !audioExportBlocked && fileNameField.text.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_export_button"),
      ) {
        Text("Export ${options.format.shortLabel}")
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
private fun VideoExportProgressDialog(
  progress: Float,
  strategy: VideoProcessingStrategy,
  format: VideoExportFormat,
  onCancel: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = {},
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    icon = {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(44.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Movie,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(22.dp),
          )
        }
      }
    },
    title = {
      Text(
        text = "Exporting ${format.shortLabel}...",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = strategy.badgeLabel,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
          progress = { progress.coerceIn(0f, 1f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape),
        )
        Text(
          text = "${(progress.coerceIn(0f, 1f) * 100).roundToInt()}%",
          style = MaterialTheme.typography.labelMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    },
    confirmButton = {
      OutlinedButton(
        onClick = onCancel,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("cancel_export_button"),
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
private fun VideoEditorScreenEmptyPreview() {
  ToolboxTheme {
    VideoEditorScreen()
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VideoEditorScreenLoadedPreview() {
  ToolboxTheme {
    VideoEditorScreen(
      initialMetadataForPreview = VideoMetadata(
        file = File("/storage/emulated/0/Movies/sunset_clip.mp4"),
        durationMs = 14_500L,
        width = 1920,
        height = 1080,
        frameRate = 30f,
        hasAudio = true,
        sizeBytes = 8_420_000L,
      )
    )
  }
}
