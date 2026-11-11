package com.example.toolbox.ui.tools

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.Looper
import android.util.Range
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.Switch
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
  modifier: Modifier = Modifier,
  initialDirectory: File? = null,
  initialCaptureMode: CameraCaptureMode? = null,
  initialFormat: CameraPhotoFormat? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()

  val prefs = remember(context, isInspection) {
    if (isInspection) null
    else context.getSharedPreferences(CAMERA_PREFS_NAME, Context.MODE_PRIVATE)
  }

  val userRoot = remember(isInspection) {
    if (isInspection) File("/storage/emulated/0") else FileManagerEngine.getUserRootDirectory()
  }

  val fallbackBaseDir = remember(context, isInspection) {
    if (isInspection) null else (context.getExternalFilesDir(null) ?: context.filesDir)
  }

  // Persisted camera configuration states
  var captureMode by rememberSaveable {
    mutableStateOf(initialCaptureMode ?: CameraCaptureMode.DEFAULT)
  }

  var lensFacing by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_LENS, null)
    mutableStateOf(CameraLensFacing.fromNameOrDefault(saved))
  }

  fun updateLensFacing(facing: CameraLensFacing) {
    lensFacing = facing
    prefs?.edit()?.putString(PREF_KEY_CAMERA_LENS, facing.name)?.apply()
  }

  var flashMode by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_FLASH, null)
    mutableStateOf(CameraFlashMode.fromNameOrDefault(saved))
  }

  fun updateFlashMode(mode: CameraFlashMode) {
    flashMode = mode
    prefs?.edit()?.putString(PREF_KEY_CAMERA_FLASH, mode.name)?.apply()
  }

  var timerDuration by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_TIMER, null)
    mutableStateOf(CameraTimerDuration.fromNameOrDefault(saved))
  }

  fun updateTimerDuration(timer: CameraTimerDuration) {
    timerDuration = timer
    prefs?.edit()?.putString(PREF_KEY_CAMERA_TIMER, timer.name)?.apply()
  }

  var aspectRatio by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_ASPECT_RATIO, null)
    mutableStateOf(CameraAspectRatio.fromNameOrDefault(saved))
  }

  fun updateAspectRatio(ratio: CameraAspectRatio) {
    aspectRatio = ratio
    prefs?.edit()?.putString(PREF_KEY_CAMERA_ASPECT_RATIO, ratio.name)?.apply()
  }

  var gridOverlay by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_GRID, null)
    mutableStateOf(CameraGridOverlay.fromNameOrDefault(saved))
  }

  fun updateGridOverlay(grid: CameraGridOverlay) {
    gridOverlay = grid
    prefs?.edit()?.putString(PREF_KEY_CAMERA_GRID, grid.name)?.apply()
  }

  var colorFilter by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_FILTER, null)
    mutableStateOf(CameraColorFilter.fromNameOrDefault(saved))
  }

  fun updateColorFilter(filter: CameraColorFilter) {
    colorFilter = filter
    prefs?.edit()?.putString(PREF_KEY_CAMERA_FILTER, filter.name)?.apply()
  }

  var photoFormat by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_FORMAT, null)
    mutableStateOf(initialFormat ?: CameraPhotoFormat.fromNameOrDefault(saved))
  }

  fun updatePhotoFormat(format: CameraPhotoFormat) {
    photoFormat = format
    prefs?.edit()?.putString(PREF_KEY_CAMERA_FORMAT, format.name)?.apply()
  }

  var mirrorFrontCamera by rememberSaveable {
    val saved = prefs?.getBoolean(PREF_KEY_CAMERA_MIRROR_FRONT, true) ?: true
    mutableStateOf(saved)
  }

  fun updateMirrorFrontCamera(mirror: Boolean) {
    mirrorFrontCamera = mirror
    prefs?.edit()?.putBoolean(PREF_KEY_CAMERA_MIRROR_FRONT, mirror)?.apply()
  }

  // Output directory (defaults to /DCIM/Camera)
  var cameraDirectory by remember {
    val initialDir = initialDirectory ?: run {
      val savedPath = prefs?.getString(PREF_KEY_CAMERA_DIRECTORY, null)
      if (!savedPath.isNullOrBlank()) {
        CameraEngine.resolveDirectoryInput(savedPath, userRoot, fallbackBaseDir)
      } else {
        if (isInspection) File("/storage/emulated/0/DCIM/Camera")
        else CameraEngine.getDefaultCameraDirectory(context)
      }
    }
    mutableStateOf(initialDir)
  }

  var directoryDisplayPath by rememberSaveable {
    val savedPath = prefs?.getString(PREF_KEY_CAMERA_DIRECTORY, null)
    mutableStateOf(
      if (!savedPath.isNullOrBlank()) savedPath
      else if (initialDirectory != null) CameraEngine.formatDirectoryDisplayPath(initialDirectory, userRoot)
      else DEFAULT_CAMERA_FOLDER_DISPLAY
    )
  }

  fun updateCameraDirectory(inputPath: String) {
    val normalized = if (inputPath.isBlank()) {
      DEFAULT_CAMERA_FOLDER_DISPLAY
    } else if (!inputPath.startsWith("/")) {
      "/$inputPath"
    } else {
      inputPath.trim()
    }
    val resolved = CameraEngine.resolveDirectoryInput(
      inputPath = normalized,
      userRoot = userRoot,
      fallbackBaseDir = fallbackBaseDir,
    )
    cameraDirectory = resolved
    directoryDisplayPath = CameraEngine.formatDirectoryDisplayPath(resolved, userRoot)
      .takeIf { it.isNotBlank() } ?: normalized
    prefs?.edit()?.putString(PREF_KEY_CAMERA_DIRECTORY, directoryDisplayPath)?.apply()
  }

  // Camera permission check
  var hasCameraPermission by remember {
    mutableStateOf(
      isInspection ||
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED,
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
  ) { permissions ->
    val camGranted = permissions[Manifest.permission.CAMERA] == true ||
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    hasCameraPermission = camGranted
  }

  // Live camera adjustments & transient UI states
  var zoomLevel by rememberSaveable { mutableFloatStateOf(MIN_CAMERA_ZOOM) }
  var exposureEv by rememberSaveable { mutableFloatStateOf(0f) }
  var showAdjustmentsPanel by rememberSaveable { mutableStateOf(false) }
  var showFiltersBar by rememberSaveable { mutableStateOf(false) }

  // Tap-to-focus indicator state (normalized 0f..1f coordinates)
  var focusPointNormalized by remember { mutableStateOf<Offset?>(null) }
  var focusTriggerCounter by remember { mutableIntStateOf(0) }

  LaunchedEffect(focusTriggerCounter) {
    if (focusPointNormalized != null) {
      delay(1400L)
      focusPointNormalized = null
    }
  }

  // Self-timer countdown state
  var countdownSecondsRemaining by remember { mutableIntStateOf(0) }
  val isCountingDown = countdownSecondsRemaining > 0

  // Shutter flash animation state
  var showShutterFlash by remember { mutableStateOf(false) }
  LaunchedEffect(showShutterFlash) {
    if (showShutterFlash) {
      delay(140L)
      showShutterFlash = false
    }
  }

  // Video recording state
  var isRecordingVideo by remember { mutableStateOf(false) }
  var elapsedVideoMs by remember { mutableLongStateOf(0L) }
  var activeVideoSession by remember { mutableStateOf<ActiveCameraVideoSession?>(null) }
  var refreshTrigger by remember { mutableIntStateOf(0) }

  // Optional live hardware TextureView reference for capturing real camera frames when available
  var activeTextureView by remember { mutableStateOf<TextureView?>(null) }

  DisposableEffect(Unit) {
    onDispose {
      activeVideoSession?.stop()
      activeVideoSession = null
    }
  }

  LaunchedEffect(isRecordingVideo, activeVideoSession) {
    if (!isRecordingVideo) {
      elapsedVideoMs = 0L
      return@LaunchedEffect
    }
    val startMs = System.currentTimeMillis()
    while (isActive && isRecordingVideo) {
      elapsedVideoMs = (System.currentTimeMillis() - startMs).coerceAtLeast(0L)
      delay(100L)
    }
  }

  // List of captured media in current directory
  val capturedMedia = remember(cameraDirectory, refreshTrigger, isInspection) {
    if (isInspection) {
      listOf(
        CameraMediaItem(
          file = File("/storage/emulated/0/DCIM/Camera/IMG_20261110_173000.jpg"),
          name = "IMG_20261110_173000.jpg",
          path = "/storage/emulated/0/DCIM/Camera/IMG_20261110_173000.jpg",
          sizeBytes = 1_420_000L,
          lastModifiedMs = 1794353400000L,
          extension = "jpg",
          isVideo = false,
        ),
        CameraMediaItem(
          file = File("/storage/emulated/0/DCIM/Camera/VID_20261110_171500.mp4"),
          name = "VID_20261110_171500.mp4",
          path = "/storage/emulated/0/DCIM/Camera/VID_20261110_171500.mp4",
          sizeBytes = 4_850_000L,
          lastModifiedMs = 1794352500000L,
          extension = "mp4",
          isVideo = true,
          durationMs = 12_000L,
        ),
      )
    } else {
      CameraEngine.listCapturedMedia(cameraDirectory)
    }
  }

  // Dialog states
  var showFolderSettingsDialog by remember { mutableStateOf(false) }
  var showRecentCapturesDialog by remember { mutableStateOf(false) }
  var mediaItemToPreview by remember { mutableStateOf<CameraMediaItem?>(null) }
  var mediaItemToRename by remember { mutableStateOf<CameraMediaItem?>(null) }
  var mediaItemToDelete by remember { mutableStateOf<CameraMediaItem?>(null) }

  fun performPhotoCapture() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    showShutterFlash = true

    val outputFile = CameraEngine.createTimestampedPhotoFile(
      directory = cameraDirectory,
      format = photoFormat,
    )

    // Try capturing from live hardware TextureView if available and rendering frames
    val hwBitmap = try {
      val tv = activeTextureView
      val raw = if (tv != null && tv.isAvailable) tv.bitmap else null
      if (raw != null && tv != null) {
        CameraEngine.transformCapturedPreviewBitmap(
          rawBitmap = raw,
          textureTransformMatrix = tv.getTransform(null),
          zoom = zoomLevel,
          lensFacing = lensFacing,
          mirrorFront = mirrorFrontCamera,
        )
      } else {
        null
      }
    } catch (_: Throwable) {
      null
    }

    var savedFromHardware = false
    if (hwBitmap != null && hwBitmap.width > 16 && hwBitmap.height > 16) {
      savedFromHardware = try {
        val bos = ByteArrayOutputStream()
        val fmt = if (photoFormat == CameraPhotoFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val compressed = hwBitmap.compress(fmt, 92, bos)
        val bytes = bos.toByteArray()
        if (compressed && bytes.size > 64) {
          outputFile.parentFile?.mkdirs()
          FileOutputStream(outputFile).use { fos ->
            fos.write(bytes)
            fos.flush()
          }
          true
        } else {
          false
        }
      } catch (_: Throwable) {
        false
      }
    }

    if (!savedFromHardware) {
      CameraEngine.captureSyntheticPhotoFile(
        outputFile = outputFile,
        lensFacing = lensFacing,
        aspectRatio = aspectRatio,
        filter = colorFilter,
        zoom = zoomLevel,
        exposureEv = exposureEv,
        mirrorFront = mirrorFrontCamera,
        flashMode = flashMode,
      )
    }

    refreshTrigger++
    if (!isInspection) {
      Toast.makeText(context, "Saved ${outputFile.name}", Toast.LENGTH_SHORT).show()
    }
  }

  fun startVideoRecording() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val outputFile = CameraEngine.createTimestampedVideoFile(
      directory = cameraDirectory,
    )
    val session = ActiveCameraVideoSession(outputFile = outputFile)
    session.start()
    activeVideoSession = session
    elapsedVideoMs = 0L
    isRecordingVideo = true
  }

  fun stopVideoRecording() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val session = activeVideoSession
    activeVideoSession = null
    isRecordingVideo = false
    val savedFile = session?.stop()
    refreshTrigger++
    if (savedFile != null && !isInspection) {
      Toast.makeText(context, "Saved ${savedFile.name}", Toast.LENGTH_SHORT).show()
    }
  }

  // Self-timer coroutine trigger
  fun triggerShutterAction() {
    if (isCountingDown) {
      // Cancel active countdown
      countdownSecondsRemaining = 0
      return
    }

    if (captureMode == CameraCaptureMode.VIDEO) {
      if (isRecordingVideo) {
        stopVideoRecording()
      } else {
        if (timerDuration.seconds > 0) {
          coroutineScope.launch {
            var remaining = timerDuration.seconds
            while (remaining > 0) {
              countdownSecondsRemaining = remaining
              delay(1000L)
              if (countdownSecondsRemaining == 0) return@launch
              remaining--
            }
            countdownSecondsRemaining = 0
            startVideoRecording()
          }
        } else {
          startVideoRecording()
        }
      }
    } else {
      if (timerDuration.seconds > 0) {
        coroutineScope.launch {
          var remaining = timerDuration.seconds
          while (remaining > 0) {
            countdownSecondsRemaining = remaining
            delay(1000L)
            if (countdownSecondsRemaining == 0) return@launch
            remaining--
          }
          countdownSecondsRemaining = 0
          performPhotoCapture()
        }
      } else {
        performPhotoCapture()
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag("camera_screen"),
  ) {
    // 1. Full-Viewport Camera Stage (shrinks with black bars when aspect ratio does not match screen)
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .testTag("camera_viewfinder_stage"),
      contentAlignment = Alignment.Center,
    ) {
      val containerW = maxWidth
      val containerH = maxHeight

      val (frameW, frameH) = remember(containerW, containerH, aspectRatio) {
        val (w, h) = CameraEngine.calculateViewfinderDimensions(
          availableWidth = containerW.value,
          availableHeight = containerH.value,
          aspectRatio = aspectRatio,
        )
        Pair(w.dp, h.dp)
      }

      Box(
        modifier = Modifier
          .size(width = frameW, height = frameH)
          .clipToBounds()
          .background(Color.Black)
          .pointerInput(Unit) {
            detectTransformGestures { _, _, zoomChange, _ ->
              if (zoomChange != 1f) {
                zoomLevel = CameraEngine.calculatePinchZoom(zoomLevel, zoomChange)
              }
            }
          }
          .pointerInput(Unit) {
            detectTapGestures { tapOffset ->
              val normX = (tapOffset.x / size.width.toFloat()).coerceIn(0.08f, 0.92f)
              val normY = (tapOffset.y / size.height.toFloat()).coerceIn(0.08f, 0.92f)
              focusPointNormalized = Offset(normX, normY)
              focusTriggerCounter++
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
          }
          .testTag("camera_viewfinder_box"),
        contentAlignment = Alignment.Center,
      ) {
        // Synthetic / Procedural Live Viewfinder Background (always rendered underneath so preview is never blank)
        ProceduralCameraViewfinderCanvas(
          lensFacing = lensFacing,
          colorFilter = colorFilter,
          zoomLevel = zoomLevel,
          exposureEv = exposureEv,
          mirrorFront = mirrorFrontCamera,
          flashMode = flashMode,
          modifier = Modifier.fillMaxSize(),
        )

        // Hardware Camera2 Live Preview when camera permission is granted
        if (hasCameraPermission && !isInspection) {
          HardwareCamera2Preview(
            lensFacing = lensFacing,
            aspectRatio = aspectRatio,
            zoomLevel = zoomLevel,
            exposureEv = exposureEv,
            flashMode = flashMode,
            mirrorFront = mirrorFrontCamera,
            onTextureViewReady = { tv -> activeTextureView = tv },
            modifier = Modifier.fillMaxSize(),
          )
        }

        // Composition Grid Overlay (3x3, Golden Ratio, or Crosshair)
        if (gridOverlay != CameraGridOverlay.NONE) {
          ViewfinderGridOverlayCanvas(
            gridOverlay = gridOverlay,
            modifier = Modifier
              .fillMaxSize()
              .testTag("camera_grid_overlay_canvas"),
          )
        }

        // Tap-to-Focus Reticle Indicator
        val activeFocusPoint = focusPointNormalized
        if (activeFocusPoint != null) {
          val focusRingColor = MaterialTheme.colorScheme.tertiary
          Box(
            modifier = Modifier
              .fillMaxSize()
              .testTag("camera_focus_indicator"),
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val cx = size.width * activeFocusPoint.x
              val cy = size.height * activeFocusPoint.y
              val radius = 32.dp.toPx()
              drawCircle(
                color = focusRingColor,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx()),
              )
              drawCircle(
                color = focusRingColor.copy(alpha = 0.7f),
                radius = 4.dp.toPx(),
                center = Offset(cx, cy),
              )
            }
          }
        }

        // Self-Timer Countdown Overlay
        if (isCountingDown) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
            tonalElevation = 6.dp,
            modifier = Modifier
              .size(108.dp)
              .testTag("camera_timer_countdown_overlay"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = countdownSecondsRemaining.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("camera_timer_countdown_text"),
              )
            }
          }
        }

        // Shutter Flash Overlay
        if (showShutterFlash) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)),
          )
        }
      }
    }

    // 2. Top Controls Overlay (Translucent Header + Permission Banner + Status/Telemetry Pills)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .testTag("camera_top_overlay"),
    ) {
      Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("camera_top_bar"),
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
                    if (isRecordingVideo) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                  ),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = if (captureMode == CameraCaptureMode.VIDEO) {
                    Icons.Default.Videocam
                  } else {
                    Icons.Default.PhotoCamera
                  },
                  contentDescription = null,
                  tint = if (isRecordingVideo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Camera",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = if (isRecordingVideo) {
                    "Recording (${CameraEngine.formatTimerDurationMs(elapsedVideoMs)}) • $directoryDisplayPath"
                  } else {
                    val countLabel = if (capturedMedia.size == 1) "1 capture" else "${capturedMedia.size} captures"
                    "$countLabel • ${aspectRatio.displayName} • $directoryDisplayPath"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isRecordingVideo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }

            // Top Bar Quick Camera Controls: Flash, Timer, Aspect Ratio, Grid, Settings
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              // Flash Mode Cycle Button
              IconButton(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  updateFlashMode(flashMode.next())
                },
                modifier = Modifier.testTag("camera_flash_button"),
              ) {
                Icon(
                  imageVector = when (flashMode) {
                    CameraFlashMode.OFF -> Icons.Default.FlashOff
                    CameraFlashMode.AUTO -> Icons.Default.FlashAuto
                    CameraFlashMode.ON -> Icons.Default.FlashOn
                    CameraFlashMode.TORCH -> Icons.Default.Highlight
                  },
                  contentDescription = flashMode.displayName,
                  tint = if (flashMode != CameraFlashMode.OFF) {
                    MaterialTheme.colorScheme.tertiary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                )
              }

              // Self-Timer Cycle Button
              IconButton(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  updateTimerDuration(timerDuration.next())
                },
                enabled = !isRecordingVideo && !isCountingDown,
                modifier = Modifier.testTag("camera_timer_button"),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = if (timerDuration == CameraTimerDuration.OFF) {
                      Icons.Default.TimerOff
                    } else {
                      Icons.Default.Timer
                    },
                    contentDescription = timerDuration.displayName,
                    tint = if (timerDuration != CameraTimerDuration.OFF) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
                  )
                  if (timerDuration != CameraTimerDuration.OFF) {
                    Text(
                      text = timerDuration.shortLabel,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.ExtraBold,
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .testTag("camera_timer_badge"),
                    )
                  }
                }
              }

              // Aspect Ratio Pill Button
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .clickable(enabled = !isRecordingVideo) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    updateAspectRatio(aspectRatio.next())
                  }
                  .testTag("camera_aspect_ratio_button"),
              ) {
                Text(
                  text = aspectRatio.displayName,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
              }

              // Grid Overlay Cycle Button
              IconButton(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  updateGridOverlay(gridOverlay.next())
                },
                modifier = Modifier.testTag("camera_grid_button"),
              ) {
                Icon(
                  imageVector = if (gridOverlay == CameraGridOverlay.NONE) {
                    Icons.Default.GridOff
                  } else {
                    Icons.Default.GridOn
                  },
                  contentDescription = gridOverlay.displayName,
                  tint = if (gridOverlay != CameraGridOverlay.NONE) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                )
              }

              // Camera Folder & Format Settings Button
              IconButton(
                onClick = { showFolderSettingsDialog = true },
                enabled = !isRecordingVideo,
                modifier = Modifier.testTag("camera_settings_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.FolderOpen,
                  contentDescription = "Camera storage and format settings",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }

      // Optional Camera Permission Banner
      if (!hasCameraPermission) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("camera_permission_banner"),
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
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
              )
              Text(
                text = "Grant camera access for live optical viewfinder capture.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
            }
            Button(
              onClick = {
                permissionLauncher.launch(
                  arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                )
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .padding(start = 8.dp)
                .testTag("allow_camera_permission_button"),
            ) {
              Text("Allow")
            }
          }
        }
      }

      // Top-Left & Top-Right Status Badges Overlay Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Left: Recording indicator OR Lens + Filter pill
        if (isRecordingVideo) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.88f),
            modifier = Modifier.testTag("video_recording_badge"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.error),
              )
              Text(
                text = "REC ${CameraEngine.formatTimerDurationMs(elapsedVideoMs)}",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.testTag("video_recording_timer_text"),
              )
            }
          }
        } else {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
            modifier = Modifier.testTag("viewfinder_status_pill"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = buildString {
                  append(lensFacing.shortLabel)
                  if (colorFilter != CameraColorFilter.NONE) {
                    append(" • ")
                    append(colorFilter.displayName)
                  }
                  if (gridOverlay != CameraGridOverlay.NONE) {
                    append(" • ")
                    append(gridOverlay.shortLabel)
                  }
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }

        // Right: Zoom & EV readout pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { showAdjustmentsPanel = !showAdjustmentsPanel }
            .testTag("viewfinder_telemetry_pill"),
        ) {
          Text(
            text = "${CameraEngine.formatZoomLabel(zoomLevel)} • ${CameraEngine.formatExposureEvLabel(exposureEv)}",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          )
        }
      }
    }

    // 3. Bottom Controls Overlay (Zoom Presets + Expandable Sliders/Filters + Translucent Control Deck)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .testTag("camera_bottom_overlay"),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Zoom Preset Pills (1x, 2x, 3x, 5x)
      Row(
        modifier = Modifier
          .padding(bottom = 8.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f))
          .padding(horizontal = 6.dp, vertical = 4.dp)
          .testTag("zoom_presets_row"),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        CAMERA_ZOOM_PRESETS.forEach { preset ->
          val isSelected = kotlin.math.abs(zoomLevel - preset) < 0.15f
          val presetLabel = "${preset.toInt()}x"
          Surface(
            shape = CircleShape,
            color = if (isSelected) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              Color.Transparent
            },
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                zoomLevel = preset
              }
              .testTag("zoom_preset_${preset.toInt()}x"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = presetLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
              )
            }
          }
        }
      }

      // Translucent Bottom Control Deck (Expandable Panels + Mode Tabs + Shutter Row)
      Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("camera_bottom_controls"),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          // Expandable Pro Sliders Panel (Fine Zoom & Exposure Compensation with SlimDotSlider)
          AnimatedVisibility(
            visible = showAdjustmentsPanel,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("camera_adjustments_panel"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                // Fine Zoom Slider
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = "Zoom",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(64.dp),
                  )
                  CameraSlimDotSlider(
                    value = zoomLevel,
                    onValueChange = { zoomLevel = CameraEngine.coerceZoom(it) },
                    valueRange = MIN_CAMERA_ZOOM..MAX_CAMERA_ZOOM,
                    modifier = Modifier
                      .weight(1f)
                      .testTag("camera_zoom_slider"),
                  )
                  Text(
                    text = CameraEngine.formatZoomLabel(zoomLevel),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                      .width(44.dp)
                      .testTag("camera_zoom_value_text"),
                  )
                }

                // Exposure Compensation Slider
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Exposure,
                    contentDescription = "Exposure",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = "Exposure",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(64.dp),
                  )
                  CameraSlimDotSlider(
                    value = exposureEv,
                    onValueChange = { exposureEv = CameraEngine.coerceExposureEv(it) },
                    valueRange = MIN_CAMERA_EXPOSURE_EV..MAX_CAMERA_EXPOSURE_EV,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                      .weight(1f)
                      .testTag("camera_exposure_slider"),
                  )
                  Text(
                    text = CameraEngine.formatExposureEvLabel(exposureEv),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                      .width(52.dp)
                      .testTag("camera_exposure_value_text"),
                  )
                }
              }
            }
          }

          // Expandable Color Filters Row
          AnimatedVisibility(
            visible = showFiltersBar,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 2.dp)
                .testTag("camera_filters_row"),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              CameraColorFilter.entries.forEach { filter ->
                val isSelected = filter == colorFilter
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    updateColorFilter(filter)
                  },
                  label = {
                    Text(
                      text = filter.displayName,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  leadingIcon = if (isSelected) {
                    {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  } else null,
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("filter_chip_${filter.name}"),
                )
              }
            }
          }

          // Mode Selector & Quick Tool Toggles Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // Left: Pro Adjustments Toggle Button
            IconButton(
              onClick = { showAdjustmentsPanel = !showAdjustmentsPanel },
              colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (showAdjustmentsPanel) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                },
              ),
              modifier = Modifier
                .size(38.dp)
                .testTag("toggle_adjustments_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Toggle zoom and exposure sliders",
                tint = if (showAdjustmentsPanel) {
                  MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(18.dp),
              )
            }

            // Center: Photo / Video Mode Switcher Pill
            Surface(
              shape = RoundedCornerShape(24.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.testTag("capture_mode_selector"),
            ) {
              Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                CameraCaptureMode.entries.forEach { mode ->
                  val selected = mode == captureMode
                  Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) {
                      MaterialTheme.colorScheme.primaryContainer
                    } else {
                      Color.Transparent
                    },
                    modifier = Modifier
                      .clip(RoundedCornerShape(20.dp))
                      .clickable(enabled = !isRecordingVideo && !isCountingDown) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        captureMode = mode
                      }
                      .testTag("mode_tab_${mode.name}"),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Icon(
                        imageVector = if (mode == CameraCaptureMode.VIDEO) {
                          Icons.Default.Videocam
                        } else {
                          Icons.Default.PhotoCamera
                        },
                        contentDescription = null,
                        tint = if (selected) {
                          MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                          MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp),
                      )
                      Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) {
                          MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                          MaterialTheme.colorScheme.onSurfaceVariant
                        },
                      )
                    }
                  }
                }
              }
            }

            // Right: Color Filters Toggle Button
            IconButton(
              onClick = { showFiltersBar = !showFiltersBar },
              colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (showFiltersBar || colorFilter != CameraColorFilter.NONE) {
                  MaterialTheme.colorScheme.tertiaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                },
              ),
              modifier = Modifier
                .size(38.dp)
                .testTag("toggle_filters_button"),
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Toggle color filters",
                tint = if (showFiltersBar || colorFilter != CameraColorFilter.NONE) {
                  MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(18.dp),
              )
            }
          }

          // Primary Shutter Row: [Recent Captures Well] — [Shutter FAB] — [Flip Camera / Video Snapshot]
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // Left: Recent Captures Thumbnail Button
            Box(contentAlignment = Alignment.TopEnd) {
              val latestItem = capturedMedia.firstOrNull()
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                  .size(52.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .clickable(enabled = !isRecordingVideo) {
                    showRecentCapturesDialog = true
                  }
                  .testTag("recent_captures_button"),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  val thumbBitmap = remember(latestItem?.path, latestItem?.lastModifiedMs, isInspection) {
                    if (isInspection || latestItem == null || latestItem.isVideo) {
                      null
                    } else {
                      GalleryEngine.decodeThumbnailBitmap(latestItem.file, reqSizePx = 160)
                    }
                  }
                  if (thumbBitmap != null) {
                    Image(
                      bitmap = thumbBitmap.asImageBitmap(),
                      contentDescription = "Recent capture ${latestItem?.name}",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize(),
                    )
                  } else {
                    Icon(
                      imageVector = if (latestItem?.isVideo == true) {
                        Icons.Default.Videocam
                      } else {
                        Icons.Default.PhotoLibrary
                      },
                      contentDescription = "Open recent captures",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(24.dp),
                    )
                  }
                }
              }

              if (capturedMedia.isNotEmpty()) {
                Badge(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier
                    .offset(x = 4.dp, y = (-4).dp)
                    .testTag("recent_captures_count_badge"),
                ) {
                  Text(
                    text = capturedMedia.size.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }

            // Center: Main Shutter / Record Button
            val infinitePulse = rememberInfiniteTransition(label = "CameraRecordPulse")
            val recordPulseScale by infinitePulse.animateFloat(
              initialValue = 1.0f,
              targetValue = 1.12f,
              animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
              ),
              label = "RecordPulseScale",
            )

            val outerRingColor by animateColorAsState(
              targetValue = when {
                isRecordingVideo -> MaterialTheme.colorScheme.error
                captureMode == CameraCaptureMode.VIDEO -> MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                else -> MaterialTheme.colorScheme.primary
              },
              label = "OuterShutterRingColor",
            )

            val innerCircleColor by animateColorAsState(
              targetValue = when {
                isRecordingVideo -> MaterialTheme.colorScheme.error
                captureMode == CameraCaptureMode.VIDEO -> MaterialTheme.colorScheme.error
                isCountingDown -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
              },
              label = "InnerShutterColor",
            )

            Box(
              modifier = Modifier
                .size(76.dp)
                .scale(if (isRecordingVideo) recordPulseScale else 1f)
                .clip(CircleShape)
                .border(3.dp, outerRingColor, CircleShape)
                .clickable { triggerShutterAction() }
                .padding(6.dp)
                .testTag("camera_shutter_button")
                .semantics {
                  contentDescription = when {
                    isCountingDown -> "Cancel timer"
                    captureMode == CameraCaptureMode.VIDEO && isRecordingVideo -> "Stop video recording"
                    captureMode == CameraCaptureMode.VIDEO -> "Start video recording"
                    else -> "Take photo"
                  }
                },
              contentAlignment = Alignment.Center,
            ) {
              Surface(
                shape = if (isRecordingVideo) RoundedCornerShape(10.dp) else CircleShape,
                color = innerCircleColor,
                modifier = Modifier.size(if (isRecordingVideo) 32.dp else 60.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  AnimatedContent(
                    targetState = Triple(captureMode, isRecordingVideo, isCountingDown),
                    transitionSpec = { (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut()) },
                    label = "ShutterIconAnim",
                  ) { (mode, recording, counting) ->
                    when {
                      counting -> Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(26.dp),
                      )
                      recording -> Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(20.dp),
                      )
                      mode == CameraCaptureMode.VIDEO -> Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(26.dp),
                      )
                      else -> Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp),
                      )
                    }
                  }
                }
              }
            }

            // Right: Flip Camera (or Video Snapshot while recording video)
            if (isRecordingVideo) {
              SmallFloatingActionButton(
                onClick = { performPhotoCapture() },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                  .size(52.dp)
                  .testTag("video_snapshot_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.PhotoCamera,
                  contentDescription = "Take snapshot while recording",
                  modifier = Modifier.size(22.dp),
                )
              }
            } else {
              val lensRotation by animateFloatAsState(
                targetValue = if (lensFacing == CameraLensFacing.FRONT) 180f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "LensFlipRotation",
              )
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                modifier = Modifier
                  .size(52.dp)
                  .clip(CircleShape)
                  .clickable(enabled = !isCountingDown) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    updateLensFacing(lensFacing.toggle())
                  }
                  .testTag("camera_flip_lens_button")
                  .semantics {
                    contentDescription = "Switch camera lens (${lensFacing.displayName})"
                  },
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                      .size(24.dp)
                      .graphicsLayer { rotationZ = lensRotation },
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Dialog 1: Camera Folder & Photo Format Settings Dialog (DESIGN_LANGUAGE.md 5.2)
  if (showFolderSettingsDialog) {
    CameraFolderAndFormatDialog(
      currentDisplayPath = directoryDisplayPath,
      currentDirectory = cameraDirectory,
      currentFormat = photoFormat,
      mirrorFront = mirrorFrontCamera,
      onDismiss = { showFolderSettingsDialog = false },
      onConfirm = { newPath, newFormat, newMirror ->
        updateCameraDirectory(newPath)
        updatePhotoFormat(newFormat)
        updateMirrorFrontCamera(newMirror)
        showFolderSettingsDialog = false
        refreshTrigger++
      },
    )
  }

  // Dialog 2: Recent Captures Browser & Inspector Sheet/Dialog
  if (showRecentCapturesDialog) {
    RecentCapturesDialog(
      captures = capturedMedia,
      directoryDisplayPath = directoryDisplayPath,
      onDismiss = { showRecentCapturesDialog = false },
      onSelectPreview = { mediaItemToPreview = it },
      onRequestRename = { mediaItemToRename = it },
      onRequestDelete = { mediaItemToDelete = it },
      onShare = { item -> shareCameraMediaFile(context, item.file, item.isVideo) },
    )
  }

  // Dialog 3: Full-Screen Media Preview Modal
  val activePreview = mediaItemToPreview
  if (activePreview != null) {
    CapturedMediaPreviewDialog(
      item = activePreview,
      onDismiss = { mediaItemToPreview = null },
      onShare = { shareCameraMediaFile(context, activePreview.file, activePreview.isVideo) },
      onDelete = {
        mediaItemToPreview = null
        mediaItemToDelete = activePreview
      },
    )
  }

  // Dialog 4: Rename Capture Dialog (DESIGN_LANGUAGE.md 5.2 with stem pre-selection)
  val itemToRename = mediaItemToRename
  if (itemToRename != null) {
    RenameCapturedMediaDialog(
      item = itemToRename,
      onDismiss = { mediaItemToRename = null },
      onConfirmRename = { newName ->
        val renamed = FileManagerEngine.renameFile(itemToRename.file, newName)
        mediaItemToRename = null
        if (renamed.isSuccess) {
          refreshTrigger++
        } else if (!isInspection) {
          Toast.makeText(context, "Could not rename file", Toast.LENGTH_SHORT).show()
        }
      },
    )
  }

  // Dialog 5: Delete Confirmation Dialog (DESIGN_LANGUAGE.md 5.2)
  val itemToDelete = mediaItemToDelete
  if (itemToDelete != null) {
    DeleteCapturedMediaDialog(
      item = itemToDelete,
      onDismiss = { mediaItemToDelete = null },
      onConfirmDelete = {
        itemToDelete.file.deleteRecursively()
        mediaItemToDelete = null
        refreshTrigger++
      },
    )
  }
}

/**
 * Slim track slider (`4.dp` height) with a circular dot thumb (`16.dp` `CircleShape`)
 * following `DESIGN_LANGUAGE.md` Section 5.6 (`SlimDotSlider`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraSlimDotSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  onValueChangeFinished: (() -> Unit)? = null,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
  steps: Int = 0,
  enabled: Boolean = true,
  accentColor: Color = MaterialTheme.colorScheme.primary,
) {
  val sliderColors = SliderDefaults.colors(
    thumbColor = accentColor,
    activeTrackColor = accentColor,
    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
  )
  Slider(
    value = value,
    onValueChange = onValueChange,
    onValueChangeFinished = onValueChangeFinished,
    modifier = modifier,
    valueRange = valueRange,
    steps = steps,
    enabled = enabled,
    colors = sliderColors,
    thumb = {
      Box(
        modifier = Modifier
          .size(16.dp)
          .shadow(2.dp, CircleShape)
          .background(accentColor, CircleShape),
      )
    },
    track = { sliderState ->
      SliderDefaults.Track(
        sliderState = sliderState,
        modifier = Modifier.height(4.dp),
        colors = sliderColors,
      )
    },
  )
}

/**
 * Renders an interactive procedural scene inside the viewfinder that responds in real time
 * to [lensFacing], [colorFilter], [zoomLevel], [exposureEv], [mirrorFront], and [flashMode].
 */
@Composable
private fun ProceduralCameraViewfinderCanvas(
  lensFacing: CameraLensFacing,
  colorFilter: CameraColorFilter,
  zoomLevel: Float,
  exposureEv: Float,
  mirrorFront: Boolean,
  flashMode: CameraFlashMode,
  modifier: Modifier = Modifier,
) {
  val effectiveEv = if (flashMode == CameraFlashMode.TORCH) {
    (exposureEv + 0.4f).coerceIn(MIN_CAMERA_EXPOSURE_EV, MAX_CAMERA_EXPOSURE_EV)
  } else {
    exposureEv
  }

  val skyTopRgb = CameraEngine.applyFilterAndExposureToRgb(
    red = if (lensFacing == CameraLensFacing.BACK) 36 else 74,
    green = if (lensFacing == CameraLensFacing.BACK) 58 else 52,
    blue = if (lensFacing == CameraLensFacing.BACK) 114 else 120,
    filter = colorFilter,
    exposureEv = effectiveEv,
  )
  val skyBottomRgb = CameraEngine.applyFilterAndExposureToRgb(
    red = if (lensFacing == CameraLensFacing.BACK) 216 else 188,
    green = if (lensFacing == CameraLensFacing.BACK) 134 else 122,
    blue = if (lensFacing == CameraLensFacing.BACK) 112 else 170,
    filter = colorFilter,
    exposureEv = effectiveEv,
  )
  val ridgeFarRgb = CameraEngine.applyFilterAndExposureToRgb(
    red = 68,
    green = 84,
    blue = 110,
    filter = colorFilter,
    exposureEv = effectiveEv,
  )
  val ridgeNearRgb = CameraEngine.applyFilterAndExposureToRgb(
    red = 40,
    green = 66,
    blue = 64,
    filter = colorFilter,
    exposureEv = effectiveEv,
  )
  val sunRgb = CameraEngine.applyFilterAndExposureToRgb(
    red = 255,
    green = 228,
    blue = 148,
    filter = colorFilter,
    exposureEv = effectiveEv,
  )

  val skyTopColor = Color(skyTopRgb.first, skyTopRgb.second, skyTopRgb.third)
  val skyBottomColor = Color(skyBottomRgb.first, skyBottomRgb.second, skyBottomRgb.third)
  val ridgeFarColor = Color(ridgeFarRgb.first, ridgeFarRgb.second, ridgeFarRgb.third)
  val ridgeNearColor = Color(ridgeNearRgb.first, ridgeNearRgb.second, ridgeNearRgb.third)
  val sunColor = Color(sunRgb.first, sunRgb.second, sunRgb.third)

  Canvas(
    modifier = modifier.graphicsLayer {
      val clampedZoom = CameraEngine.coerceZoom(zoomLevel)
      scaleX = clampedZoom * (if (lensFacing == CameraLensFacing.FRONT && mirrorFront) -1f else 1f)
      scaleY = clampedZoom
    },
  ) {
    // Sky gradient
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(skyTopColor, skyBottomColor),
        startY = 0f,
        endY = size.height * 0.72f,
      ),
      size = size,
    )

    // Sun / Ambient orb
    val sunCenter = Offset(
      x = size.width * (if (lensFacing == CameraLensFacing.BACK) 0.64f else 0.5f),
      y = size.height * 0.34f,
    )
    val sunRadius = size.minDimension * 0.16f
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(sunColor, sunColor.copy(alpha = 0f)),
        center = sunCenter,
        radius = sunRadius * 1.6f,
      ),
      radius = sunRadius * 1.6f,
      center = sunCenter,
    )
    drawCircle(
      color = sunColor.copy(alpha = 0.85f),
      radius = sunRadius * 0.55f,
      center = sunCenter,
    )

    // Distant horizon layer
    drawRect(
      color = ridgeFarColor,
      topLeft = Offset(0f, size.height * 0.60f),
      size = Size(size.width, size.height * 0.40f),
    )

    // Foreground horizon layer
    drawRect(
      color = ridgeNearColor,
      topLeft = Offset(0f, size.height * 0.72f),
      size = Size(size.width, size.height * 0.28f),
    )
  }
}

/**
 * Renders the selected composition grid overlay ([CameraGridOverlay]) over the viewfinder.
 */
@Composable
private fun ViewfinderGridOverlayCanvas(
  gridOverlay: CameraGridOverlay,
  modifier: Modifier = Modifier,
) {
  val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
  val accentColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f)
  val fractions = remember(gridOverlay) { CameraEngine.getGridFractions(gridOverlay) }

  Canvas(modifier = modifier) {
    val strokePx = 1.dp.toPx()
    for (f in fractions) {
      val x = size.width * f
      val y = size.height * f
      drawLine(
        color = lineColor,
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = strokePx,
      )
      drawLine(
        color = lineColor,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokePx,
      )
    }

    if (gridOverlay == CameraGridOverlay.CROSSHAIR) {
      val cx = size.width / 2f
      val cy = size.height / 2f
      val reticleRadius = 24.dp.toPx()
      drawCircle(
        color = accentColor,
        radius = reticleRadius,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5.dp.toPx()),
      )
      drawLine(
        color = accentColor,
        start = Offset(cx - reticleRadius * 1.4f, cy),
        end = Offset(cx + reticleRadius * 1.4f, cy),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }
  }
}

/**
 * Hardware Camera2 live preview backed by a [TextureView].
 * Safely initializes the requested [lensFacing] camera when available on the device/emulator,
 * and cleans up the session automatically when disposed or switched.
 */
@SuppressLint("MissingPermission")
@Composable
private fun HardwareCamera2Preview(
  lensFacing: CameraLensFacing,
  aspectRatio: CameraAspectRatio,
  zoomLevel: Float,
  exposureEv: Float,
  flashMode: CameraFlashMode,
  mirrorFront: Boolean,
  onTextureViewReady: (TextureView?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var cameraDevice by remember { mutableStateOf<CameraDevice?>(null) }
  var captureSession by remember { mutableStateOf<CameraCaptureSession?>(null) }
  var previewBuilder by remember { mutableStateOf<CaptureRequest.Builder?>(null) }
  var activeCharacteristics by remember { mutableStateOf<CameraCharacteristics?>(null) }
  var activePreviewSize by remember { mutableStateOf(Pair(1440, 1080)) }
  var activeSensorOrientation by remember { mutableIntStateOf(90) }
  var surfaceReadyTrigger by remember { mutableIntStateOf(0) }
  var textureViewInstance by remember { mutableStateOf<TextureView?>(null) }

  fun getDisplayRotationDegrees(tv: TextureView): Int {
    @Suppress("DEPRECATION")
    val rotation = try {
      tv.display?.rotation ?: Surface.ROTATION_0
    } catch (_: Throwable) {
      Surface.ROTATION_0
    }
    return when (rotation) {
      Surface.ROTATION_90 -> 90
      Surface.ROTATION_180 -> 180
      Surface.ROTATION_270 -> 270
      else -> 0
    }
  }

  fun applyCenterCropTransform(
    tv: TextureView,
    viewWidth: Int = tv.width,
    viewHeight: Int = tv.height,
    previewSize: Pair<Int, Int> = activePreviewSize,
    sensorOrientation: Int = activeSensorOrientation,
  ) {
    if (viewWidth <= 0 || viewHeight <= 0) return
    val matrix = CameraEngine.buildTextureViewTransformMatrix(
      viewWidth = viewWidth,
      viewHeight = viewHeight,
      sensorPreviewWidth = previewSize.first,
      sensorPreviewHeight = previewSize.second,
      sensorOrientationDegrees = sensorOrientation,
      displayRotationDegrees = getDisplayRotationDegrees(tv),
    )
    tv.setTransform(matrix)
  }

  // Re-apply center-crop transform whenever aspect ratio or active preview stream size changes
  LaunchedEffect(aspectRatio, activePreviewSize, activeSensorOrientation) {
    val tv = textureViewInstance ?: return@LaunchedEffect
    applyCenterCropTransform(tv)
  }

  // Update repeating request when zoom, exposure, or flash changes
  LaunchedEffect(zoomLevel, exposureEv, flashMode, captureSession, previewBuilder) {
    val session = captureSession ?: return@LaunchedEffect
    val builder = previewBuilder ?: return@LaunchedEffect
    val chars = activeCharacteristics
    try {
      // Apply flash / torch mode
      when (flashMode) {
        CameraFlashMode.TORCH -> {
          builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
          builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
        }
        CameraFlashMode.ON -> {
          builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH)
          builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
        }
        CameraFlashMode.AUTO -> {
          builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
          builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
        }
        CameraFlashMode.OFF -> {
          builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
          builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
        }
      }

      // Apply exposure compensation if supported
      if (chars != null) {
        val evRange: Range<Int>? = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
        if (evRange != null && evRange.upper > evRange.lower) {
          val normalized = ((exposureEv - MIN_CAMERA_EXPOSURE_EV) / (MAX_CAMERA_EXPOSURE_EV - MIN_CAMERA_EXPOSURE_EV))
            .coerceIn(0f, 1f)
          val targetSteps = (evRange.lower + normalized * (evRange.upper - evRange.lower)).roundToInt()
            .coerceIn(evRange.lower, evRange.upper)
          builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, targetSteps)
        }
      }

      session.setRepeatingRequest(builder.build(), null, null)
    } catch (_: Throwable) {
      // Ignore if camera session was closed
    }
  }

  DisposableEffect(lensFacing, surfaceReadyTrigger) {
    val tv = textureViewInstance
    if (tv == null || !tv.isAvailable) {
      return@DisposableEffect onDispose {}
    }

    val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
      ?: return@DisposableEffect onDispose {}

    val desiredLensConst = if (lensFacing == CameraLensFacing.FRONT) {
      CameraCharacteristics.LENS_FACING_FRONT
    } else {
      CameraCharacteristics.LENS_FACING_BACK
    }

    val cameraId = try {
      manager.cameraIdList.firstOrNull { id ->
        manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == desiredLensConst
      } ?: manager.cameraIdList.firstOrNull()
    } catch (_: Throwable) {
      null
    }

    if (cameraId == null) {
      return@DisposableEffect onDispose {}
    }

    val mainHandler = Handler(Looper.getMainLooper())
    var openedDevice: CameraDevice? = null
    var openedSession: CameraCaptureSession? = null
    var previewSurface: Surface? = null

    try {
      val chars = manager.getCameraCharacteristics(cameraId)
      activeCharacteristics = chars
      val sensorOrientation = chars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
      activeSensorOrientation = sensorOrientation
      val supportedSizes = chars
        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        ?.getOutputSizes(SurfaceTexture::class.java)
        ?.map { Pair(it.width, it.height) }
        .orEmpty()

      val initialPreviewSize = CameraEngine.selectOptimalSensorPreviewSize(
        supportedSensorSizes = supportedSizes,
        viewWidth = tv.width,
        viewHeight = tv.height,
        sensorOrientationDegrees = sensorOrientation,
        displayRotationDegrees = getDisplayRotationDegrees(tv),
      )
      activePreviewSize = initialPreviewSize
      applyCenterCropTransform(
        tv = tv,
        previewSize = initialPreviewSize,
        sensorOrientation = sensorOrientation,
      )

      manager.openCamera(
        cameraId,
        object : CameraDevice.StateCallback() {
          override fun onOpened(device: CameraDevice) {
            openedDevice = device
            cameraDevice = device
            try {
              val st = tv.surfaceTexture ?: return
              val chosenSize = CameraEngine.selectOptimalSensorPreviewSize(
                supportedSensorSizes = supportedSizes,
                viewWidth = tv.width,
                viewHeight = tv.height,
                sensorOrientationDegrees = sensorOrientation,
                displayRotationDegrees = getDisplayRotationDegrees(tv),
              )
              activePreviewSize = chosenSize
              st.setDefaultBufferSize(chosenSize.first, chosenSize.second)
              applyCenterCropTransform(
                tv = tv,
                previewSize = chosenSize,
                sensorOrientation = sensorOrientation,
              )

              val surface = Surface(st)
              previewSurface = surface
              val builder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
              builder.addTarget(surface)
              builder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE,
              )
              previewBuilder = builder

              @Suppress("DEPRECATION")
              device.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                  override fun onConfigured(session: CameraCaptureSession) {
                    openedSession = session
                    captureSession = session
                    try {
                      session.setRepeatingRequest(builder.build(), null, mainHandler)
                    } catch (_: Throwable) {}
                  }

                  override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                mainHandler,
              )
            } catch (_: Throwable) {}
          }

          override fun onDisconnected(device: CameraDevice) {
            try {
              device.close()
            } catch (_: Throwable) {}
            cameraDevice = null
          }

          override fun onError(device: CameraDevice, error: Int) {
            try {
              device.close()
            } catch (_: Throwable) {}
            cameraDevice = null
          }
        },
        mainHandler,
      )
    } catch (_: Throwable) {}

    onDispose {
      onTextureViewReady(null)
      try {
        openedSession?.close()
      } catch (_: Throwable) {}
      try {
        openedDevice?.close()
      } catch (_: Throwable) {}
      try {
        previewSurface?.release()
      } catch (_: Throwable) {}
      captureSession = null
      cameraDevice = null
      previewBuilder = null
    }
  }

  AndroidView(
    factory = { ctx ->
      TextureView(ctx).apply {
        isOpaque = false
        addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
          val w = right - left
          val h = bottom - top
          if (w > 0 && h > 0 && (w != oldRight - oldLeft || h != oldBottom - oldTop)) {
            applyCenterCropTransform(this@apply, viewWidth = w, viewHeight = h)
          }
        }
        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
          override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            textureViewInstance = this@apply
            onTextureViewReady(this@apply)
            applyCenterCropTransform(this@apply, viewWidth = width, viewHeight = height)
            surfaceReadyTrigger++
          }

          override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            applyCenterCropTransform(this@apply, viewWidth = width, viewHeight = height)
          }

          override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            onTextureViewReady(null)
            textureViewInstance = null
            return true
          }

          override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
      }
    },
    update = { tv ->
      applyCenterCropTransform(tv)
    },
    modifier = modifier.graphicsLayer {
      val clampedZoom = CameraEngine.coerceZoom(zoomLevel)
      scaleX = clampedZoom * (if (lensFacing == CameraLensFacing.FRONT && mirrorFront) -1f else 1f)
      scaleY = clampedZoom
    },
  )
}

/**
 * Dialog for configuring the Camera output folder, still photo format (JPG/PNG),
 * and front camera mirroring, styled per `DESIGN_LANGUAGE.md` 5.2.
 */
@Composable
private fun CameraFolderAndFormatDialog(
  currentDisplayPath: String,
  currentDirectory: File,
  currentFormat: CameraPhotoFormat,
  mirrorFront: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (String, CameraPhotoFormat, Boolean) -> Unit,
) {
  val focusRequester = remember { FocusRequester() }
  var textFieldValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = currentDisplayPath,
        selection = TextRange(0, currentDisplayPath.length),
      )
    )
  }
  var selectedFormat by remember { mutableStateOf(currentFormat) }
  var mirrorFrontEnabled by remember { mutableStateOf(mirrorFront) }

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Throwable) {}
  }

  val trimmedInput = textFieldValue.text.trim()
  val isValid = trimmedInput.isNotEmpty() && !trimmedInput.contains('\u0000')

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("camera_folder_dialog"),
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
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Camera Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Configure save folder, photo format, and lens options",
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
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = currentDisplayPath,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${currentDirectory.absolutePath} • ${selectedFormat.shortLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Photo Format Selector Chips (JPG / PNG)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Photo Format",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            CameraPhotoFormat.entries.forEach { format ->
              val isSelected = format == selectedFormat
              FilterChip(
                selected = isSelected,
                onClick = { selectedFormat = format },
                label = { Text(format.displayName) },
                leadingIcon = if (isSelected) {
                  {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                } else null,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("camera_format_chip_${format.name}"),
              )
            }
          }
        }

        // Quick Folder Presets
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          val presets = listOf("/DCIM/Camera", "/Photos", "/Pictures")
          presets.forEach { preset ->
            FilterChip(
              selected = textFieldValue.text.trim().equals(preset, ignoreCase = true),
              onClick = {
                textFieldValue = TextFieldValue(
                  text = preset,
                  selection = TextRange(0, preset.length),
                )
              },
              label = { Text(preset) },
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.testTag("camera_folder_preset_${preset.trimStart('/').replace('/', '_')}"),
            )
          }
        }

        // Rounded Folder Path Input Field
        OutlinedTextField(
          value = textFieldValue,
          onValueChange = { textFieldValue = it },
          label = { Text("Save folder path") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
          },
          trailingIcon = {
            if (textFieldValue.text.isNotEmpty()) {
              IconButton(
                onClick = { textFieldValue = TextFieldValue("") },
              ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear folder path")
              }
            }
          },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              if (isValid) {
                onConfirm(trimmedInput, selectedFormat, mirrorFrontEnabled)
              }
            }
          ),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("camera_folder_input"),
        )

        // Front Camera Mirror Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Default.Flip,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                text = "Mirror Front Camera",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Flip selfies horizontally to match preview",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Switch(
            checked = mirrorFrontEnabled,
            onCheckedChange = { mirrorFrontEnabled = it },
            modifier = Modifier.testTag("mirror_front_camera_switch"),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(trimmedInput, selectedFormat, mirrorFrontEnabled) },
        enabled = isValid,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_camera_settings_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save")
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

/**
 * Dialog listing recent photos and videos captured in the current camera folder.
 */
@Composable
private fun RecentCapturesDialog(
  captures: List<CameraMediaItem>,
  directoryDisplayPath: String,
  onDismiss: () -> Unit,
  onSelectPreview: (CameraMediaItem) -> Unit,
  onRequestRename: (CameraMediaItem) -> Unit,
  onRequestDelete: (CameraMediaItem) -> Unit,
  onShare: (CameraMediaItem) -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("recent_captures_dialog"),
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
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Recent Captures",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${captures.size} items in $directoryDisplayPath",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      if (captures.isEmpty()) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_captures_card"),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(36.dp),
            )
            Text(
              text = "No captures yet",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Photos and videos you capture will appear here.",
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
            .heightIn(max = 360.dp)
            .testTag("recent_captures_list"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(captures, key = { it.path }) { item ->
            var showMenu by remember { mutableStateOf(false) }
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSelectPreview(item) }
                .testTag("capture_item_${item.name}"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center,
                ) {
                  val thumb = remember(item.path, item.lastModifiedMs) {
                    if (item.isVideo) null else GalleryEngine.decodeThumbnailBitmap(item.file, reqSizePx = 120)
                  }
                  if (thumb != null) {
                    Image(
                      bitmap = thumb.asImageBitmap(),
                      contentDescription = null,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize(),
                    )
                  } else {
                    Icon(
                      imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Image,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.size(22.dp),
                    )
                  }
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
                  Text(
                    text = buildString {
                      append(item.formatBadge)
                      append(" • ")
                      append(item.sizeFormatted)
                      if (item.isVideo && item.durationMs > 0L) {
                        append(" • ")
                        append(item.durationFormatted)
                      }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }

                Box {
                  IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("capture_more_${item.name}"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.MoreVert,
                      contentDescription = "More actions for ${item.name}",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }

                  DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                  ) {
                    DropdownMenuItem(
                      text = { Text("Rename") },
                      leadingIcon = {
                        Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null)
                      },
                      onClick = {
                        showMenu = false
                        onRequestRename(item)
                      },
                    )
                    DropdownMenuItem(
                      text = { Text("Share") },
                      leadingIcon = {
                        Icon(Icons.Default.Share, contentDescription = null)
                      },
                      onClick = {
                        showMenu = false
                        onShare(item)
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
                        showMenu = false
                        onRequestDelete(item)
                      },
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("close_recent_captures_button"),
      ) {
        Text("Done")
      }
    },
  )
}

/**
 * Preview modal for inspecting a captured photo or video item.
 */
@Composable
private fun CapturedMediaPreviewDialog(
  item: CameraMediaItem,
  onDismiss: () -> Unit,
  onShare: () -> Unit,
  onDelete: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("capture_preview_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${item.formatBadge} • ${item.sizeFormatted} • ${item.timestampFormatted}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(240.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentAlignment = Alignment.Center,
      ) {
        val decoded = remember(item.path, item.lastModifiedMs) {
          if (item.isVideo) null else GalleryEngine.decodeThumbnailBitmap(item.file, reqSizePx = 720)
        }
        if (decoded != null) {
          Image(
            bitmap = decoded.asImageBitmap(),
            contentDescription = item.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
          )
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Image,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(48.dp),
            )
            Text(
              text = if (item.isVideo) "Video Clip (${item.durationFormatted})" else item.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(
          onClick = onDelete,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Delete")
        }
        TextButton(
          onClick = onShare,
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Share")
        }
        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("Close")
        }
      }
    },
  )
}

/**
 * Rename dialog following `DESIGN_LANGUAGE.md` 5.2 (pre-selecting filename stem excluding extension
 * and providing a one-tap extension restoration pill if the extension is removed).
 */
@Composable
private fun RenameCapturedMediaDialog(
  item: CameraMediaItem,
  onDismiss: () -> Unit,
  onConfirmRename: (String) -> Unit,
) {
  val originalName = item.name
  val originalExt = item.extension
  val stemLength = if (originalExt.isNotEmpty() && originalName.endsWith(".$originalExt", ignoreCase = true)) {
    (originalName.length - originalExt.length - 1).coerceAtLeast(0)
  } else {
    originalName.length
  }

  val focusRequester = remember { FocusRequester() }
  var textFieldValue by remember(originalName) {
    mutableStateOf(
      TextFieldValue(
        text = originalName,
        selection = TextRange(0, stemLength),
      )
    )
  }

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Throwable) {}
  }

  val trimmed = textFieldValue.text.trim()
  val isMissingExtension = originalExt.isNotEmpty() && !trimmed.endsWith(".$originalExt", ignoreCase = true)
  val isValid = trimmed.isNotEmpty() && trimmed != originalName && !trimmed.contains('/')

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
            text = "Rename Capture",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Enter a new filename for this capture",
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
              imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Image,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
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
              Text(
                text = "${item.formatBadge} • ${item.sizeFormatted}",
                style = MaterialTheme.typography.labelSmall,
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
          shape = RoundedCornerShape(14.dp),
          trailingIcon = {
            if (textFieldValue.text.isNotEmpty()) {
              IconButton(onClick = { textFieldValue = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear name")
              }
            }
          },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              if (isValid) onConfirmRename(trimmed)
            }
          ),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        )

        if (isMissingExtension) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable {
                val restored = "${trimmed.trimEnd('.')}.$originalExt"
                textFieldValue = TextFieldValue(
                  text = restored,
                  selection = TextRange(0, (restored.length - originalExt.length - 1).coerceAtLeast(0)),
                )
              },
          ) {
            Text(
              text = "Restore .$originalExt extension",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
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
        enabled = isValid,
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

/**
 * Delete confirmation dialog following `DESIGN_LANGUAGE.md` 5.2.
 */
@Composable
private fun DeleteCapturedMediaDialog(
  item: CameraMediaItem,
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
            text = "Delete Capture",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "This file will be permanently removed",
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
            imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = item.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "${item.formatBadge} • ${item.sizeFormatted}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirmDelete,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
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

private fun shareCameraMediaFile(context: Context, file: File, isVideo: Boolean) {
  try {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file,
    )
    val mimeType = if (isVideo) "video/*" else "image/*"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
  } catch (_: Exception) {
    Toast.makeText(context, "Unable to share ${file.name}", Toast.LENGTH_SHORT).show()
  }
}

@Preview(showBackground = true)
@Composable
private fun CameraScreenPreview() {
  ToolboxTheme {
    CameraScreen()
  }
}
