package com.example.toolbox.ui.tools

import com.example.toolbox.ui.theme.toolboxScreenBackground

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Range
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import com.example.toolbox.ui.theme.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BurstMode
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HdrOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PanoramaHorizontal
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.example.toolbox.ui.theme.SmallFloatingActionButton
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
  modifier: Modifier = Modifier,
  initialDirectory: File? = null,
  initialCaptureMode: CameraCaptureMode? = null,
  initialFormat: CameraPhotoFormat? = null,
  systemCamerasOverride: List<CameraSystemDevice>? = null,
  notesRepository: NotesRepository? = null,
  onOpenGallery: ((File) -> Unit)? = null,
  onOpenNotes: (() -> Unit)? = null,
  onOpenColorPicker: ((String) -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()

  val effectiveNotesRepo = remember(context, notesRepository, isInspection) {
    notesRepository ?: if (isInspection) {
      InMemoryNotesRepository()
    } else {
      SharedPreferencesNotesRepository(context)
    }
  }

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

  // Detect all available system cameras (all back, front, and physical multi-camera lenses)
  val systemCameras = remember(context, isInspection, systemCamerasOverride) {
    systemCamerasOverride?.takeIf { it.isNotEmpty() } ?: if (isInspection) {
      CameraEngine.getDefaultFallbackSystemCameras()
    } else {
      CameraEngine.detectSystemCameras(context)
    }
  }

  // Persisted camera configuration states
  var captureMode by rememberSaveable {
    mutableStateOf(initialCaptureMode ?: CameraCaptureMode.DEFAULT)
  }

  LaunchedEffect(CameraEngine.pendingCaptureModeRequest) {
    val pending = CameraEngine.consumePendingCaptureModeRequest() ?: return@LaunchedEffect
    captureMode = pending.mode
  }

  LaunchedEffect(captureMode) {
    if (!isInspection) {
      CameraWidgets.saveActiveCaptureMode(context, captureMode)
    }
  }

  var lensFacing by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_LENS, null)
    mutableStateOf(CameraLensFacing.fromNameOrDefault(saved))
  }

  var selectedCameraId by rememberSaveable {
    val savedId = prefs?.getString(PREF_KEY_CAMERA_ID, null)
    val initialDevice = CameraEngine.resolveSelectedCamera(
      cameras = systemCameras,
      requestedCameraId = savedId,
      fallbackFacing = lensFacing,
    )
    mutableStateOf(initialDevice.cameraId)
  }

  val selectedCamera = remember(systemCameras, selectedCameraId, lensFacing) {
    CameraEngine.resolveSelectedCamera(
      cameras = systemCameras,
      requestedCameraId = selectedCameraId,
      fallbackFacing = lensFacing,
    )
  }

  val backSystemCameras = remember(systemCameras) {
    CameraEngine.getCamerasForFacing(systemCameras, CameraLensFacing.BACK)
  }

  val frontSystemCameras = remember(systemCameras) {
    CameraEngine.getCamerasForFacing(systemCameras, CameraLensFacing.FRONT)
  }

  LaunchedEffect(selectedCamera.cameraId, selectedCamera.lensFacing) {
    if (selectedCameraId != selectedCamera.cameraId) {
      selectedCameraId = selectedCamera.cameraId
    }
    if (lensFacing != selectedCamera.lensFacing) {
      lensFacing = selectedCamera.lensFacing
    }
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
    val saved = prefs?.getBoolean(PREF_KEY_CAMERA_MIRROR_FRONT, false) ?: false
    mutableStateOf(saved)
  }

  fun updateMirrorFrontCamera(mirror: Boolean) {
    mirrorFrontCamera = mirror
    prefs?.edit()?.putBoolean(PREF_KEY_CAMERA_MIRROR_FRONT, mirror)?.apply()
  }

  // Photo Sub-Mode / Tools Navigation Pill Bar state (Standard, Panorama, HDR, Burst)
  var photoSubMode by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_PHOTO_SUB_MODE, null)
    mutableStateOf(CameraPhotoSubMode.fromNameOrDefault(saved))
  }

  fun updatePhotoSubMode(newSubMode: CameraPhotoSubMode) {
    photoSubMode = newSubMode
    prefs?.edit()?.putString(PREF_KEY_CAMERA_PHOTO_SUB_MODE, newSubMode.name)?.apply()
  }

  // Floating Night Mode state (Auto, On, Off) positioned to the left of the zoom options
  var nightMode by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_NIGHT_MODE, null)
    mutableStateOf(CameraNightMode.fromNameOrDefault(saved))
  }

  fun updateNightMode(newNightMode: CameraNightMode) {
    nightMode = newNightMode
    prefs?.edit()?.putString(PREF_KEY_CAMERA_NIGHT_MODE, newNightMode.name)?.apply()
  }

  // QR Code Reader & Barcode Scanner states
  var scanFilter by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_SCAN_FILTER, null)
    mutableStateOf(CameraScanFilter.fromNameOrDefault(saved))
  }

  fun updateScanFilter(newFilter: CameraScanFilter) {
    scanFilter = newFilter
    prefs?.edit()?.putString(PREF_KEY_CAMERA_SCAN_FILTER, newFilter.name)?.apply()
  }

  var latestScannedCode by remember {
    mutableStateOf<CameraScannedCode?>(null)
  }

  fun recordScannedCode(code: CameraScannedCode, showToast: Boolean = false) {
    latestScannedCode = code
    if (showToast && !isInspection) {
      Toast.makeText(context, "Scanned ${code.format.shortBadge}: ${code.title}", Toast.LENGTH_SHORT).show()
    }
  }

  // Document OCR & Note Scanner states
  var notePreset by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_NOTE_PRESET, null)
    mutableStateOf(CameraNoteSamplePreset.fromNameOrDefault(saved))
  }

  fun updateNotePreset(preset: CameraNoteSamplePreset) {
    notePreset = preset
    prefs?.edit()?.putString(PREF_KEY_CAMERA_NOTE_PRESET, preset.name)?.apply()
  }

  var latestScannedDocument by remember {
    mutableStateOf<CameraScannedDocument?>(null)
  }

  var liveOcrDetectedDocument by remember {
    mutableStateOf<CameraScannedDocument?>(null)
  }

  var showInlineNotesScreen by remember {
    mutableStateOf(false)
  }

  // Live ML Kit Vision Tagging states (>= 50% confidence)
  var visionSamplePreset by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_VISION_PRESET, null)
    mutableStateOf(CameraVisionSamplePreset.fromNameOrDefault(saved))
  }

  var latestVisionResult by remember {
    mutableStateOf<CameraVisionAnalysisResult?>(
      if (isInspection && initialCaptureMode == CameraCaptureMode.VISION) {
        CameraEngine.buildVisionAnalysisResult(
          rawTags = CameraVisionSamplePreset.DEFAULT.sampleTags,
          confidenceThreshold = MIN_VISION_CONFIDENCE_THRESHOLD,
          inferenceLatencyMs = 15L,
          sourceDescription = CameraVisionSamplePreset.DEFAULT.sceneTitle,
        )
      } else {
        null
      }
    )
  }

  fun updateVisionSamplePreset(preset: CameraVisionSamplePreset) {
    visionSamplePreset = preset
    prefs?.edit()?.putString(PREF_KEY_CAMERA_VISION_PRESET, preset.name)?.apply()
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
  var showZoomSlider by rememberSaveable { mutableStateOf(false) }
  var showAdjustmentsPanel by rememberSaveable { mutableStateOf(false) }
  var showFiltersBar by rememberSaveable { mutableStateOf(false) }

  fun selectCameraDevice(device: CameraSystemDevice, resetZoom: Boolean = true) {
    val changedDevice = selectedCameraId != device.cameraId
    selectedCameraId = device.cameraId
    lensFacing = device.lensFacing
    if (resetZoom && changedDevice) {
      zoomLevel = MIN_CAMERA_ZOOM
    }
    prefs?.edit()
      ?.putString(PREF_KEY_CAMERA_ID, device.cameraId)
      ?.putString(PREF_KEY_CAMERA_LENS, device.lensFacing.name)
      ?.apply()
  }

  fun toggleFrontBackPrimaryCamera() {
    val nextDevice = CameraEngine.togglePrimaryFrontBackCamera(
      cameras = systemCameras,
      currentCameraId = selectedCamera.cameraId,
      currentFacing = selectedCamera.lensFacing,
    )
    selectCameraDevice(nextDevice)
  }

  // Smart ML Kit Object Detection Autofocus & 7-second Manual Focus Override state
  var smartAutoFocusTarget by remember {
    mutableStateOf(
      CameraEngine.selectSmartAutofocusTargetFromVisionTags(
        tags = visionSamplePreset.sampleTags,
      )
    )
  }
  var manualFocusTarget by remember { mutableStateOf<CameraSmartFocusTarget?>(null) }
  var focusPointNormalized by remember { mutableStateOf<Offset?>(null) }
  var focusSampledColorHex by remember { mutableStateOf<String?>(null) }
  var smartFocusSampledColorHex by remember { mutableStateOf<String?>(null) }
  var focusTriggerCounter by remember { mutableIntStateOf(0) }
  var inlineColorPickerHex by rememberSaveable { mutableStateOf<String?>(null) }

  val activeFocusTarget = remember(manualFocusTarget, smartAutoFocusTarget) {
    manualFocusTarget ?: smartAutoFocusTarget
  }

  fun openColorInColorsTool(hex: String) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    manualFocusTarget = null
    focusPointNormalized = null
    if (onOpenColorPicker != null) {
      onOpenColorPicker(hex)
    } else {
      inlineColorPickerHex = hex
    }
  }

  // Manual tap-to-focus overrides smart ML Kit autofocus for 7 seconds (MANUAL_FOCUS_OVERRIDE_DURATION_MS)
  // before automatically returning to smart object detection autofocus.
  LaunchedEffect(focusTriggerCounter) {
    if (manualFocusTarget != null || focusPointNormalized != null) {
      delay(MANUAL_FOCUS_OVERRIDE_DURATION_MS)
      manualFocusTarget = null
      focusPointNormalized = null
    }
  }

  // Self-timer countdown state
  var countdownSecondsRemaining by remember { mutableIntStateOf(0) }
  val isCountingDown = countdownSecondsRemaining > 0

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        hasCameraPermission = isInspection ||
          ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
          ) == PackageManager.PERMISSION_GRANTED
      } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
        countdownSecondsRemaining = 0
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

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

  // Interactive Panorama sweep state
  var isCapturingPanorama by remember { mutableStateOf(false) }
  var panoramaSweepProgress by remember { mutableFloatStateOf(0f) }
  var panoramaCapturedCount by remember { mutableIntStateOf(0) }
  var panoramaVerticalOffsetNorm by remember { mutableFloatStateOf(0f) }
  val panoramaCapturedFrames = remember { mutableListOf<Bitmap>() }

  // Optional live hardware TextureView reference for capturing real camera frames when available
  var activeTextureView by remember { mutableStateOf<TextureView?>(null) }

  DisposableEffect(Unit) {
    onDispose {
      activeVideoSession?.stop()
      activeVideoSession = null
      isCapturingPanorama = false
      panoramaCapturedFrames.clear()
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

  // Dialog & cross-tool navigation states
  var showFolderSettingsDialog by remember { mutableStateOf(false) }
  var showCreateCodeDialog by rememberSaveable { mutableStateOf(false) }
  var scannedCodeToInspect by remember { mutableStateOf<CameraScannedCode?>(null) }
  var directoryForGallery by remember { mutableStateOf<File?>(null) }

  // Live autofocus hex color sampling and transformed analysis frame capture from the active TextureView preview stream.
  // Keeps a lightweight raw preview bitmap cached and maps frames/coordinates through the TextureView
  // center-crop, zoom, and front-mirror transform.
  var latestRawPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var lastPreviewGrabUptimeMs by remember { mutableLongStateOf(0L) }

  fun captureTransformedAnalysisBitmap(maxEdgePx: Int = 640): Bitmap? {
    return try {
      val tv = activeTextureView ?: return null
      if (!tv.isAvailable) return null
      val vw = tv.width
      val vh = tv.height
      val raw = if (vw > 32 && vh > 32) {
        val scale = minOf(1f, maxEdgePx.toFloat() / maxOf(vw, vh).toFloat())
        val targetW = (vw * scale).roundToInt().coerceAtLeast(64)
        val targetH = (vh * scale).roundToInt().coerceAtLeast(64)
        tv.getBitmap(targetW, targetH)
      } else {
        tv.getBitmap(maxEdgePx, maxEdgePx)
      } ?: return null

      if (raw.width <= 16 || raw.height <= 16) return null
      latestRawPreviewBitmap = raw
      lastPreviewGrabUptimeMs = android.os.SystemClock.uptimeMillis()
      CameraEngine.transformCapturedPreviewBitmap(
        rawBitmap = raw,
        textureTransformMatrix = tv.getTransform(null),
        zoom = zoomLevel,
        lensFacing = lensFacing,
        mirrorFront = mirrorFrontCamera,
        viewWidth = if (vw > 32) vw else raw.width,
        viewHeight = if (vh > 32) vh else raw.height,
      )
    } catch (_: Throwable) {
      null
    }
  }

  // Continuous live frame QR & Barcode scanning when in SCANNER mode and hardware TextureView is rendering
  LaunchedEffect(captureMode, scanFilter, activeTextureView, zoomLevel, lensFacing, mirrorFrontCamera) {
    if (captureMode != CameraCaptureMode.SCANNER || isInspection) {
      return@LaunchedEffect
    }
    while (isActive && captureMode == CameraCaptureMode.SCANNER) {
      val alignedFrame = captureTransformedAnalysisBitmap(maxEdgePx = 640)
      if (alignedFrame != null && alignedFrame.width > 32 && alignedFrame.height > 32) {
        val decoded = withContext(Dispatchers.Default) {
          CameraEngine.decodeCodesFromBitmap(alignedFrame, filter = scanFilter)
        }
        if (decoded.isNotEmpty()) {
          val first = decoded.first()
          if (latestScannedCode?.rawValue != first.rawValue || latestScannedCode?.format != first.format) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            recordScannedCode(first, showToast = false)
          }
        }
      }
      delay(350L)
    }
  }

  fun performCodeScan() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    showShutterFlash = true

    // 1. Try decoding from live hardware TextureView frame first (aspect- and zoom-aligned)
    val hwDecoded = try {
      val aligned = captureTransformedAnalysisBitmap(maxEdgePx = 800)
      if (aligned != null && aligned.width > 32 && aligned.height > 32) {
        CameraEngine.decodeCodesFromBitmap(aligned, filter = scanFilter)
      } else {
        emptyList()
      }
    } catch (_: Throwable) {
      emptyList()
    }

    if (hwDecoded.isNotEmpty()) {
      recordScannedCode(hwDecoded.first(), showToast = true)
      return
    }

    // 2. Fallback to decoding a synthetic target code through ZXing's pixel decoder pipeline when hardware frame is unavailable
    val fallbackPreset = CameraScanSamplePreset.presetsForFilter(scanFilter).first()
    val targetFormat = fallbackPreset.format
    val targetContent = fallbackPreset.payload

    val synthW = if (targetFormat.is2D) 360 else 440
    val synthH = if (targetFormat.is2D) 360 else 200
    val pixels = CameraEngine.renderCodeToArgbPixels(
      content = targetContent,
      format = targetFormat,
      width = synthW,
      height = synthH,
    )
    val decodedFromPixels = if (pixels != null) {
      CameraEngine.decodeCodesFromArgbPixels(
        pixels = pixels,
        width = synthW,
        height = synthH,
        filter = scanFilter,
      )
    } else {
      emptyList()
    }

    val resultCode = decodedFromPixels.firstOrNull() ?: CameraEngine.analyzeScannedPayload(
      rawValue = CameraEngine.normalizeBarcodeInputForEncoding(targetContent, targetFormat),
      format = targetFormat,
    )
    recordScannedCode(resultCode, showToast = true)
  }

  // Continuous live frame OCR document scanning when in NOTE mode and hardware TextureView is rendering
  LaunchedEffect(captureMode, activeTextureView, zoomLevel, lensFacing, mirrorFrontCamera) {
    if (captureMode != CameraCaptureMode.NOTE || isInspection) {
      return@LaunchedEffect
    }
    while (isActive && captureMode == CameraCaptureMode.NOTE) {
      val alignedFrame = captureTransformedAnalysisBitmap(maxEdgePx = 960)
      if (alignedFrame != null && alignedFrame.width > 32 && alignedFrame.height > 32) {
        val recognized = withContext(Dispatchers.Default) {
          CameraEngine.recognizeTextFromBitmapWithMlKit(alignedFrame)
        }
        val stabilizedDoc = CameraEngine.selectStabilizedLiveOcrDocument(
          previousDoc = liveOcrDetectedDocument,
          candidateDoc = recognized,
        )
        if (stabilizedDoc != null) {
          liveOcrDetectedDocument = stabilizedDoc
        }
      }
      delay(420L)
    }
  }

  fun performNoteDocumentScan() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    showShutterFlash = true

    val initialDoc = (liveOcrDetectedDocument?.takeIf { it.rawText.isNotBlank() }
      ?: CameraEngine.analyzeScannedDocumentText(
        rawText = notePreset.sampleText,
        fallbackTitle = notePreset.defaultTitle,
      )).copy(savedNoteId = null)

    latestScannedDocument = initialDoc

    if (!isInspection) {
      Toast.makeText(context, "Scanned document: review and tap Add Note", Toast.LENGTH_SHORT).show()
    }

    // Also run on-device ML Kit OCR on the aligned high-res live hardware frame (if available) to refine the previewed document
    val alignedHardwareBitmap = captureTransformedAnalysisBitmap(maxEdgePx = 1080)
    if (alignedHardwareBitmap != null && alignedHardwareBitmap.width > 32 && alignedHardwareBitmap.height > 32) {
      coroutineScope.launch {
        val mlKitDoc = withContext(Dispatchers.Default) {
          CameraEngine.recognizeTextFromBitmapWithMlKit(alignedHardwareBitmap)
        }
        val stabilized = CameraEngine.selectStabilizedLiveOcrDocument(
          previousDoc = liveOcrDetectedDocument,
          candidateDoc = mlKitDoc,
        )
        if (stabilized != null && stabilized.rawText.isNotBlank()) {
          liveOcrDetectedDocument = stabilized
          val currentDoc = latestScannedDocument
          // Only update in-place if the user hasn't already dismissed or saved the note
          if (currentDoc != null && currentDoc.savedNoteId == null) {
            latestScannedDocument = stabilized.copy(savedNoteId = null)
          }
        }
      }
    }
  }

  fun addReviewedDocumentToNotes(document: CameraScannedDocument) {
    if (document.savedNoteId != null) return
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val (savedDoc, savedNote) = CameraEngine.saveScannedDocumentToNotes(
      repository = effectiveNotesRepo,
      document = document,
      color = notePreset.noteColor,
    )
    latestScannedDocument = savedDoc
    if (!isInspection) {
      Toast.makeText(context, "Added note: ${savedNote.title}", Toast.LENGTH_SHORT).show()
    }
  }

  fun sampleHexFromRawPreview(
    normX: Float,
    normY: Float,
    rawBitmap: Bitmap? = latestRawPreviewBitmap,
  ): String? {
    val bmp = rawBitmap ?: return null
    if (bmp.width <= 8 || bmp.height <= 8) return null
    val tv = activeTextureView
    val transformMatrix = try {
      tv?.getTransform(null)
    } catch (_: Throwable) {
      null
    }
    return CameraEngine.sampleLiveFocusColorHexFromRawTextureBitmap(
      rawBitmap = bmp,
      normX = normX,
      normY = normY,
      textureTransformMatrix = transformMatrix,
      zoom = zoomLevel,
      lensFacing = lensFacing,
      mirrorFront = mirrorFrontCamera,
      viewWidth = tv?.width?.takeIf { it > 32 } ?: bmp.width,
      viewHeight = tv?.height?.takeIf { it > 32 } ?: bmp.height,
      radiusPx = 5,
      filter = colorFilter,
      exposureEv = exposureEv,
    )
  }

  fun refreshLiveFocusColorSample(forceGrabBitmap: Boolean = false) {
    val tv = activeTextureView
    val nowUptimeMs = android.os.SystemClock.uptimeMillis()
    var rawBmp = latestRawPreviewBitmap
    if (tv != null &&
      tv.isAvailable &&
      (forceGrabBitmap || rawBmp == null || (nowUptimeMs - lastPreviewGrabUptimeMs) >= 48L)
    ) {
      val grabbed = try {
        val vw = tv.width
        val vh = tv.height
        if (vw > 32 && vh > 32) {
          val scale = minOf(1f, 220f / maxOf(vw, vh).toFloat())
          val targetW = (vw * scale).roundToInt().coerceAtLeast(64)
          val targetH = (vh * scale).roundToInt().coerceAtLeast(64)
          tv.getBitmap(targetW, targetH)
        } else {
          tv.getBitmap(192, 192)
        }
      } catch (_: Throwable) {
        null
      }
      if (grabbed != null && grabbed.width > 8 && grabbed.height > 8) {
        rawBmp = grabbed
        latestRawPreviewBitmap = grabbed
        lastPreviewGrabUptimeMs = nowUptimeMs
      }
    }

    val manualPoint = focusPointNormalized
    if (manualPoint != null) {
      focusSampledColorHex = sampleHexFromRawPreview(
        normX = manualPoint.x,
        normY = manualPoint.y,
        rawBitmap = rawBmp,
      )
    }
    val smartTarget = smartAutoFocusTarget
    if (smartTarget != null) {
      smartFocusSampledColorHex = sampleHexFromRawPreview(
        normX = smartTarget.clampedX,
        normY = smartTarget.clampedY,
        rawBitmap = rawBmp,
      )
    }
  }

  // Re-sample the live autofocus hex color immediately whenever the reticle coordinates or
  // camera visual parameters (filter, exposure, zoom, lens, mirror) change.
  LaunchedEffect(
    smartAutoFocusTarget?.clampedX,
    smartAutoFocusTarget?.clampedY,
    focusPointNormalized,
    colorFilter,
    exposureEv,
    zoomLevel,
    lensFacing,
    mirrorFrontCamera,
    selectedCamera.cameraId,
  ) {
    refreshLiveFocusColorSample(forceGrabBitmap = false)
  }

  // Continuous live autofocus hex color sampling loop across all capture modes and during both
  // Smart Autofocus and 7-second Manual Tap-to-Focus overrides.
  LaunchedEffect(
    activeTextureView,
    hasCameraPermission,
    selectedCamera.cameraId,
    colorFilter,
    exposureEv,
    zoomLevel,
    lensFacing,
    mirrorFrontCamera,
  ) {
    if (isInspection || !hasCameraPermission || activeTextureView == null) {
      return@LaunchedEffect
    }
    while (isActive && activeTextureView != null) {
      refreshLiveFocusColorSample(forceGrabBitmap = false)
      delay(80L)
    }
  }

  // Continuous live frame ML Kit Object Detection for Smart Autofocus across camera modes.
  // Pauses automatic focus target updates while a 7-second manual focus override is active,
  // and defers to the Vision mode detection pipeline when in VISION mode so two detectors do not fight.
  LaunchedEffect(
    activeTextureView,
    selectedCamera.cameraId,
    zoomLevel,
    lensFacing,
    mirrorFrontCamera,
    manualFocusTarget,
    captureMode,
    colorFilter,
    exposureEv,
  ) {
    if (isInspection || manualFocusTarget != null || captureMode == CameraCaptureMode.VISION) {
      return@LaunchedEffect
    }
    while (isActive && manualFocusTarget == null && captureMode != CameraCaptureMode.VISION) {
      val alignedFrame = captureTransformedAnalysisBitmap(maxEdgePx = 480)
      if (alignedFrame != null && alignedFrame.width > 32 && alignedFrame.height > 32) {
        val detectedTarget = withContext(Dispatchers.Default) {
          CameraEngine.detectSmartFocusObjectWithMlKit(
            bitmap = alignedFrame,
            previousTarget = smartAutoFocusTarget,
            fallbackToSalientRegion = true,
            streamMode = true,
          )
        }
        if (detectedTarget != null && manualFocusTarget == null) {
          smartAutoFocusTarget = detectedTarget
          smartFocusSampledColorHex = CameraEngine.sampleCenterRadiusColorHexFromBitmap(
            bitmap = alignedFrame,
            normX = detectedTarget.clampedX,
            normY = detectedTarget.clampedY,
            radiusPx = 6,
            filter = colorFilter,
            exposureEv = exposureEv,
          )
        }
      }
      delay(280L)
    }
  }

  // Keep smart autofocus target synced with detected Vision objects when not in a manual override
  LaunchedEffect(latestVisionResult, manualFocusTarget) {
    if (manualFocusTarget == null && latestVisionResult != null) {
      val fromVision = CameraEngine.selectSmartAutofocusTargetFromVisionResult(
        result = latestVisionResult,
        previousTarget = smartAutoFocusTarget,
      )
      if (fromVision != null) {
        smartAutoFocusTarget = fromVision
        refreshLiveFocusColorSample(forceGrabBitmap = false)
      }
    }
  }

  // Smooth velocity- and trajectory-aware autofocus glide between detection updates.
  // Traces a small elliptical cornering arc at the start of movement before moving in a straight
  // line toward the new target, with capped velocity gain and smooth deceleration at arrival.
  LaunchedEffect(
    smartAutoFocusTarget?.targetNormX,
    smartAutoFocusTarget?.targetNormY,
    manualFocusTarget,
  ) {
    if (isInspection || manualFocusTarget != null) {
      return@LaunchedEffect
    }
    var lastStepMs = System.currentTimeMillis()
    while (isActive && manualFocusTarget == null) {
      val current = smartAutoFocusTarget ?: break
      if (current.distanceToDesiredTarget <= 0.0012f && current.speedNormPerSec <= 0.004f) {
        break
      }
      delay(16L)
      val nowMs = System.currentTimeMillis()
      val dtSec = ((nowMs - lastStepMs).coerceIn(16L, 120L)) / 1000f
      lastStepMs = nowMs
      val latest = smartAutoFocusTarget ?: break
      if (manualFocusTarget == null) {
        val stepped = CameraEngine.stepStickyAutofocusTrajectory(
          currentTarget = latest,
          dtSeconds = dtSec,
          timestampMs = nowMs,
        )
        smartAutoFocusTarget = stepped
        refreshLiveFocusColorSample(forceGrabBitmap = false)
      }
    }
  }

  // Continuous live frame ML Kit Object Detection & Image Labeling when in VISION mode and hardware TextureView is rendering
  LaunchedEffect(captureMode, activeTextureView, zoomLevel, lensFacing, mirrorFrontCamera, colorFilter, exposureEv) {
    if (captureMode != CameraCaptureMode.VISION || isInspection) {
      return@LaunchedEffect
    }
    while (isActive && captureMode == CameraCaptureMode.VISION) {
      val alignedFrame = captureTransformedAnalysisBitmap(maxEdgePx = 640)
      if (alignedFrame != null && alignedFrame.width > 32 && alignedFrame.height > 32) {
        val currentSmart = smartAutoFocusTarget
        if (currentSmart != null && manualFocusTarget == null) {
          smartFocusSampledColorHex = CameraEngine.sampleCenterRadiusColorHexFromBitmap(
            bitmap = alignedFrame,
            normX = currentSmart.clampedX,
            normY = currentSmart.clampedY,
            radiusPx = 6,
            filter = colorFilter,
            exposureEv = exposureEv,
          )
        }
        val analyzed = withContext(Dispatchers.Default) {
          CameraEngine.recognizeVisionTagsFromBitmapWithMlKit(
            bitmap = alignedFrame,
            confidenceThreshold = MIN_VISION_CONFIDENCE_THRESHOLD,
            sourceDescription = "Live Camera Frame",
            previousResult = latestVisionResult,
            streamMode = true,
          )
        }
        if (analyzed != null) {
          if (analyzed.tags.isNotEmpty()) {
            latestVisionResult = analyzed
          } else if (latestVisionResult?.sourceDescription == "Live Camera Frame") {
            latestVisionResult = null
          }
        }
      }
      delay(260L)
    }
  }

  fun performVisionAnalysis() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    showShutterFlash = true

    val fallbackResult = CameraEngine.buildVisionAnalysisResult(
      rawTags = visionSamplePreset.sampleTags,
      confidenceThreshold = MIN_VISION_CONFIDENCE_THRESHOLD,
      inferenceLatencyMs = 16L,
      frameWidth = 640,
      frameHeight = 640,
      sourceDescription = visionSamplePreset.sceneTitle,
    )

    val currentLive = latestVisionResult?.takeIf {
      it.sourceDescription == "Live Camera Frame" && it.tags.isNotEmpty()
    }
    val initialResult = currentLive?.withThreshold(MIN_VISION_CONFIDENCE_THRESHOLD) ?: fallbackResult
    latestVisionResult = initialResult

    // Advance sample preset for subsequent fallback test triggers
    val nextPresetIndex = (visionSamplePreset.ordinal + 1) % CameraVisionSamplePreset.entries.size
    updateVisionSamplePreset(CameraVisionSamplePreset.entries[nextPresetIndex])

    // Also run on-device ML Kit Object Detection & Image Labeling asynchronously on the aligned hardware frame if available
    val alignedHardwareBitmap = captureTransformedAnalysisBitmap(maxEdgePx = 768)
    if (alignedHardwareBitmap != null && alignedHardwareBitmap.width > 32 && alignedHardwareBitmap.height > 32) {
      coroutineScope.launch {
        val hwResult = withContext(Dispatchers.Default) {
          CameraEngine.recognizeVisionTagsFromBitmapWithMlKit(
            bitmap = alignedHardwareBitmap,
            confidenceThreshold = MIN_VISION_CONFIDENCE_THRESHOLD,
            sourceDescription = "Live Camera Frame",
            previousResult = latestVisionResult,
            streamMode = false,
          )
        }
        if (hwResult != null && hwResult.tags.isNotEmpty()) {
          latestVisionResult = hwResult
        }
      }
    }
  }

  fun captureHardwarePreviewFrame(extractPanoramaBand: Boolean = false): Bitmap? {
    return try {
      val tv = activeTextureView
      val raw = if (tv != null && tv.isAvailable) tv.bitmap else null
      if (raw != null && tv != null && raw.width > 16 && raw.height > 16) {
        val transformed = CameraEngine.transformCapturedPreviewBitmap(
          rawBitmap = raw,
          textureTransformMatrix = tv.getTransform(null),
          zoom = zoomLevel,
          lensFacing = lensFacing,
          mirrorFront = mirrorFrontCamera,
        )
        if (extractPanoramaBand) {
          CameraEngine.extractPanoramaBandFromFrame(transformed)
        } else {
          transformed
        }
      } else {
        null
      }
    } catch (_: Throwable) {
      null
    }
  }

  fun finishPanoramaSweep() {
    if (!isCapturingPanorama) return
    isCapturingPanorama = false
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    showShutterFlash = true

    // Capture final frame at the end of the sweep if distinct from the last keyframe
    val finalSlice = captureHardwarePreviewFrame(extractPanoramaBand = true)
    if (finalSlice != null) {
      val lastSlice = panoramaCapturedFrames.lastOrNull()
      if (lastSlice == null) {
        panoramaCapturedFrames.add(finalSlice)
      } else {
        val (shiftFrac, _, zeroMad) = CameraEngine.estimatePanoramaPairAlignment(lastSlice, finalSlice)
        if (zeroMad >= 2.2f && abs(shiftFrac) >= 0.08f) {
          panoramaCapturedFrames.add(finalSlice)
        }
      }
    }

    val outputFile = CameraEngine.createTimestampedPhotoFile(
      directory = cameraDirectory,
      format = photoFormat,
      subMode = CameraPhotoSubMode.PANORAMA,
    )

    val stitchedHardware = if (panoramaCapturedFrames.isNotEmpty()) {
      val rawStitched = CameraEngine.stitchPanoramaBitmaps(panoramaCapturedFrames)
      if (rawStitched != null) {
        CameraEngine.applyPhotoEnhancementsToBitmap(
          bitmap = rawStitched,
          filter = colorFilter,
          exposureEv = exposureEv,
          flashMode = flashMode,
          subMode = CameraPhotoSubMode.PANORAMA,
          nightMode = nightMode,
        )
      } else {
        null
      }
    } else {
      null
    }

    var savedFromHardware = false
    if (stitchedHardware != null && stitchedHardware.width > 16 && stitchedHardware.height > 16) {
      savedFromHardware = try {
        val bos = ByteArrayOutputStream()
        val fmt = if (photoFormat == CameraPhotoFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val compressed = stitchedHardware.compress(fmt, 92, bos)
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
      CameraEngine.captureSyntheticPanoramaFile(
        outputFile = outputFile,
        lensFacing = lensFacing,
        filter = colorFilter,
        zoom = zoomLevel,
        exposureEv = exposureEv,
        mirrorFront = mirrorFrontCamera,
        flashMode = flashMode,
        nightMode = nightMode,
        segments = panoramaCapturedCount.coerceAtLeast(PANORAMA_TARGET_SEGMENTS),
      )
    }

    panoramaCapturedFrames.clear()
    panoramaSweepProgress = 0f
    panoramaCapturedCount = 0
    panoramaVerticalOffsetNorm = 0f
    refreshTrigger++
    if (!isInspection) {
      Toast.makeText(context, "Saved panorama ${outputFile.name}", Toast.LENGTH_SHORT).show()
    }
  }

  fun startPanoramaSweep() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    panoramaCapturedFrames.clear()
    val firstSlice = captureHardwarePreviewFrame(extractPanoramaBand = true)
    if (firstSlice != null) {
      panoramaCapturedFrames.add(firstSlice)
    }
    panoramaCapturedCount = 1
    panoramaSweepProgress = 1f / PANORAMA_TARGET_SEGMENTS.toFloat()
    panoramaVerticalOffsetNorm = 0f
    isCapturingPanorama = true
  }

  // Cancel active panorama sweep if user switches away from Photo / Panorama sub-mode
  LaunchedEffect(captureMode, photoSubMode) {
    if (isCapturingPanorama && (captureMode != CameraCaptureMode.PHOTO || photoSubMode != CameraPhotoSubMode.PANORAMA)) {
      isCapturingPanorama = false
      panoramaCapturedFrames.clear()
      panoramaSweepProgress = 0f
      panoramaCapturedCount = 0
      panoramaVerticalOffsetNorm = 0f
    }
  }

  // Live gyroscope + optical frame shift tracking while sweeping a panorama
  DisposableEffect(isCapturingPanorama) {
    if (!isCapturingPanorama || isInspection) {
      return@DisposableEffect onDispose {}
    }
    val sensorManager = try {
      context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    } catch (_: Throwable) {
      null
    }
    val gyroSensor = try {
      sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    } catch (_: Throwable) {
      null
    }

    var lastTimestampNs = 0L
    var accumulatedYawRad = 0f
    var accumulatedPitchRad = 0f
    val radiansPerSegment = 0.16f // ~9.2 degrees of horizontal rotation per panorama segment

    val sensorListener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent?) {
        val ev = event ?: return
        if (ev.sensor?.type != Sensor.TYPE_GYROSCOPE || ev.values.size < 3) return
        if (lastTimestampNs != 0L) {
          val dtSec = ((ev.timestamp - lastTimestampNs) * 1e-9f).coerceIn(0f, 0.25f)
          val pitchRate = ev.values[0]
          val yawRate = ev.values[1]
          accumulatedYawRad += abs(yawRate) * dtSec
          accumulatedPitchRad = (accumulatedPitchRad + pitchRate * dtSec).coerceIn(-0.35f, 0.35f)
          panoramaVerticalOffsetNorm = (accumulatedPitchRad / 0.35f).coerceIn(-1f, 1f)

          val targetCount = (1 + (accumulatedYawRad / radiansPerSegment).toInt())
            .coerceIn(1, PANORAMA_TARGET_SEGMENTS)
          val rawProgress = ((1f + (accumulatedYawRad / radiansPerSegment)) / PANORAMA_TARGET_SEGMENTS.toFloat())
            .coerceIn(1f / PANORAMA_TARGET_SEGMENTS.toFloat(), 1f)
          if (rawProgress > panoramaSweepProgress) {
            panoramaSweepProgress = rawProgress
          }
          if (targetCount > panoramaCapturedCount) {
            val nextSlice = captureHardwarePreviewFrame(extractPanoramaBand = true)
            if (nextSlice != null) {
              panoramaCapturedFrames.add(nextSlice)
            }
            panoramaCapturedCount = targetCount
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (panoramaCapturedCount >= PANORAMA_TARGET_SEGMENTS) {
              finishPanoramaSweep()
            }
          }
        }
        lastTimestampNs = ev.timestamp
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    if (sensorManager != null && gyroSensor != null) {
      try {
        sensorManager.registerListener(sensorListener, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
      } catch (_: Throwable) {}
    }

    onDispose {
      if (sensorManager != null) {
        try {
          sensorManager.unregisterListener(sensorListener)
        } catch (_: Throwable) {}
      }
    }
  }

  // Optical frame-to-frame translation fallback when panning without gyroscope trigger
  LaunchedEffect(isCapturingPanorama, activeTextureView) {
    if (!isCapturingPanorama || isInspection) {
      return@LaunchedEffect
    }
    while (isActive && isCapturingPanorama) {
      delay(130L)
      if (!isCapturingPanorama) break
      val candidate = captureHardwarePreviewFrame(extractPanoramaBand = true)
      if (candidate != null) {
        val lastKeyframe = panoramaCapturedFrames.lastOrNull()
        if (lastKeyframe == null) {
          panoramaCapturedFrames.add(candidate)
        } else {
          val (shiftFrac, dyFrac, zeroMad) = withContext(Dispatchers.Default) {
            CameraEngine.estimatePanoramaPairAlignment(lastKeyframe, candidate)
          }
          panoramaVerticalOffsetNorm = (dyFrac * 4f).coerceIn(-1f, 1f)
          val absShift = abs(shiftFrac)
          val stepThreshold = 0.22f
          val partialSegment = (absShift / stepThreshold).coerceIn(0f, 1f)
          val opticalProgress = ((panoramaCapturedCount.toFloat() + partialSegment * 0.85f) / PANORAMA_TARGET_SEGMENTS.toFloat())
            .coerceIn(panoramaSweepProgress, 1f)
          panoramaSweepProgress = opticalProgress

          if (zeroMad >= 2.8f && absShift >= stepThreshold) {
            panoramaCapturedFrames.add(candidate)
            panoramaCapturedCount = (panoramaCapturedCount + 1).coerceAtMost(PANORAMA_TARGET_SEGMENTS)
            panoramaSweepProgress = (panoramaCapturedCount.toFloat() / PANORAMA_TARGET_SEGMENTS.toFloat()).coerceIn(0f, 1f)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (panoramaCapturedCount >= PANORAMA_TARGET_SEGMENTS) {
              finishPanoramaSweep()
              break
            }
          }
        }
      }
    }
  }

  fun performPhotoCapture() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    showShutterFlash = true

    val activeSubMode = if (captureMode == CameraCaptureMode.PHOTO) {
      photoSubMode
    } else {
      CameraPhotoSubMode.STANDARD
    }

    // 1. Burst sub-mode: capture a rapid 3-frame continuous burst sequence
    if (activeSubMode == CameraPhotoSubMode.BURST) {
      val burstFiles = CameraEngine.createTimestampedBurstPhotoFiles(
        directory = cameraDirectory,
        format = photoFormat,
        burstCount = 3,
      )
      val evSteps = listOf(-0.2f, 0.0f, 0.25f)
      burstFiles.forEachIndexed { idx, file ->
        val frameEv = (exposureEv + evSteps.getOrElse(idx) { 0f })
          .coerceIn(MIN_CAMERA_EXPOSURE_EV, MAX_CAMERA_EXPOSURE_EV)
        CameraEngine.captureSyntheticPhotoFile(
          outputFile = file,
          lensFacing = lensFacing,
          aspectRatio = aspectRatio,
          filter = colorFilter,
          zoom = zoomLevel,
          exposureEv = frameEv,
          mirrorFront = mirrorFrontCamera,
          flashMode = flashMode,
          subMode = CameraPhotoSubMode.BURST,
          nightMode = nightMode,
        )
      }
      refreshTrigger++
      if (!isInspection && burstFiles.isNotEmpty()) {
        Toast.makeText(
          context,
          "Saved ${burstFiles.size}-shot burst (${burstFiles.first().name})",
          Toast.LENGTH_SHORT,
        ).show()
      }
      return
    }

    val outputFile = CameraEngine.createTimestampedPhotoFile(
      directory = cameraDirectory,
      format = photoFormat,
      subMode = activeSubMode,
    )

    // Try capturing from live hardware TextureView if available and rendering frames
    val hwBitmap = try {
      val baseFrame = captureHardwarePreviewFrame(
        extractPanoramaBand = activeSubMode == CameraPhotoSubMode.PANORAMA,
      )
      if (baseFrame != null) {
        val rawProcessed = when (activeSubMode) {
          CameraPhotoSubMode.PANORAMA -> {
            CameraEngine.stitchPanoramaBitmaps(listOf(baseFrame)) ?: baseFrame
          }
          else -> baseFrame
        }
        CameraEngine.applyPhotoEnhancementsToBitmap(
          bitmap = rawProcessed,
          filter = colorFilter,
          exposureEv = exposureEv,
          flashMode = flashMode,
          subMode = activeSubMode,
          nightMode = nightMode,
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
        subMode = activeSubMode,
        nightMode = nightMode,
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

    if (captureMode == CameraCaptureMode.SCANNER) {
      performCodeScan()
      return
    }

    if (captureMode == CameraCaptureMode.NOTE) {
      performNoteDocumentScan()
      return
    }

    if (captureMode == CameraCaptureMode.VISION) {
      performVisionAnalysis()
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
    } else if (captureMode == CameraCaptureMode.PHOTO && photoSubMode == CameraPhotoSubMode.PANORAMA) {
      if (isCapturingPanorama) {
        finishPanoramaSweep()
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
            startPanoramaSweep()
          }
        } else {
          startPanoramaSweep()
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

  val activeGalleryDir = directoryForGallery
  if (activeGalleryDir != null) {
    val galleryRootDir = remember(activeGalleryDir.absolutePath, userRoot.absolutePath) {
      if (FileManagerEngine.isSameOrDescendant(activeGalleryDir, userRoot)) {
        userRoot
      } else {
        activeGalleryDir.parentFile ?: activeGalleryDir
      }
    }
    GalleryScreen(
      modifier = modifier,
      rootDirectory = galleryRootDir,
      initialAlbumDirectory = activeGalleryDir,
      onNavigateBack = {
        directoryForGallery = null
        refreshTrigger++
      },
    )
    return
  }

  if (showInlineNotesScreen) {
    BackHandler {
      showInlineNotesScreen = false
    }
    NotesScreen(
      modifier = modifier,
      repository = effectiveNotesRepo,
    )
    return
  }

  val activeInlineColorHex = inlineColorPickerHex
  if (activeInlineColorHex != null) {
    BackHandler {
      inlineColorPickerHex = null
    }
    ColorPickerScreen(
      modifier = modifier,
      initialHexColor = activeInlineColorHex,
      onNavigateBack = {
        inlineColorPickerHex = null
      },
    )
    return
  }

  // Keep the display awake while the camera is in use.
  if (!isInspection) {
    val view = LocalView.current
    DisposableEffect(view) {
      view.keepScreenOn = true
      onDispose { view.keepScreenOn = false }
    }
  }

  // Measured height of the top controls overlay, used to pin the scanner box just beneath it.
  var topOverlayHeightPx by remember { mutableStateOf(0) }
  val density = LocalDensity.current

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(Color.Black)
      .testTag("camera_screen"),
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

    // 1. Full-Viewport Camera Stage (shrinks with black bars when aspect ratio does not match screen)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .toolboxScreenBackground(Color.Black)
        .testTag("camera_viewfinder_stage"),
      contentAlignment = Alignment.Center,
    ) {
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
          .pointerInput(colorFilter, exposureEv, lensFacing, zoomLevel, mirrorFrontCamera) {
            detectTapGestures { tapOffset ->
              val normX = (tapOffset.x / size.width.toFloat()).coerceIn(0.08f, 0.92f)
              val normY = (tapOffset.y / size.height.toFloat()).coerceIn(0.08f, 0.92f)
              val manualTarget = CameraEngine.createManualFocusTarget(normX = normX, normY = normY)
              manualFocusTarget = manualTarget
              focusPointNormalized = Offset(manualTarget.clampedX, manualTarget.clampedY)
              refreshLiveFocusColorSample(forceGrabBitmap = true)
              focusTriggerCounter++
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
          }
          .testTag("camera_viewfinder_box"),
        contentAlignment = Alignment.Center,
      ) {
        // Hardware Camera2 Live Preview when camera permission is granted
        if (hasCameraPermission && !isInspection) {
          HardwareCamera2Preview(
            selectedCamera = selectedCamera,
            lensFacing = lensFacing,
            aspectRatio = aspectRatio,
            zoomLevel = zoomLevel,
            exposureEv = exposureEv,
            flashMode = flashMode,
            mirrorFront = mirrorFrontCamera,
            captureMode = captureMode,
            focusTarget = activeFocusTarget,
            onTextureViewReady = { tv -> activeTextureView = tv },
            onPreviewFrameUpdated = { refreshLiveFocusColorSample(forceGrabBitmap = false) },
            modifier = Modifier.fillMaxSize(),
          )
        }

        // QR Code & Barcode Scanner Viewfinder Reticle Overlay
        if (captureMode == CameraCaptureMode.SCANNER) {
          // Pin the reticle near the top of the screen, just below the top controls overlay.
          val frameTopInScreen = (containerH - frameH) / 2
          val topOverlayHeight = with(density) { topOverlayHeightPx.toDp() }
          val reticleTopInset = (topOverlayHeight + 24.dp - frameTopInScreen).coerceAtLeast(24.dp)
          ScannerReticleOverlayCanvas(
            scanFilter = scanFilter,
            hasDetectedCode = latestScannedCode != null,
            topInset = reticleTopInset,
            modifier = Modifier
              .fillMaxSize()
              .testTag("camera_scanner_reticle_overlay"),
          )
        }

        // Document OCR Note Scanner Viewfinder Reticle Overlay
        if (captureMode == CameraCaptureMode.NOTE) {
          val frameTopInScreen = (containerH - frameH) / 2
          val topOverlayHeight = with(density) { topOverlayHeightPx.toDp() }
          val reticleTopInset = (topOverlayHeight + 24.dp - frameTopInScreen).coerceAtLeast(24.dp)
          DocumentNoteReticleOverlayCanvas(
            hasScannedDocument = latestScannedDocument != null,
            topInset = reticleTopInset,
            modifier = Modifier
              .fillMaxSize()
              .testTag("camera_note_reticle_overlay"),
          )
        }

        // Live ML Kit Vision Dynamic Bounding Boxes Overlay
        if (captureMode == CameraCaptureMode.VISION) {
          val frameTopInScreen = (containerH - frameH) / 2
          val topOverlayHeight = with(density) { topOverlayHeightPx.toDp() }
          val reticleTopInset = (topOverlayHeight + 16.dp - frameTopInScreen).coerceAtLeast(16.dp)
          VisionReticleOverlayCanvas(
            visionResult = latestVisionResult,
            topInset = reticleTopInset,
            modifier = Modifier
              .fillMaxSize()
              .testTag("camera_vision_reticle_overlay"),
          )
        }

        // Photo Mode Panorama Reticle Overlay
        if (captureMode == CameraCaptureMode.PHOTO && photoSubMode == CameraPhotoSubMode.PANORAMA) {
          val frameTopInScreen = (containerH - frameH) / 2
          val topOverlayHeight = with(density) { topOverlayHeightPx.toDp() }
          val reticleTopInset = (topOverlayHeight + 24.dp - frameTopInScreen).coerceAtLeast(24.dp)
          PanoramaReticleOverlayCanvas(
            isCapturingPanorama = isCapturingPanorama,
            sweepProgress = panoramaSweepProgress,
            capturedSegments = panoramaCapturedCount,
            targetSegments = PANORAMA_TARGET_SEGMENTS,
            verticalOffsetNorm = panoramaVerticalOffsetNorm,
            topInset = reticleTopInset,
            modifier = Modifier
              .fillMaxSize()
              .testTag("camera_panorama_reticle_overlay"),
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

    // Transparent Autofocus Reticle & Color Pill Overlay (rendered without elevating the opaque viewfinder stage above camera controls)
    Box(
      modifier = Modifier
        .size(width = frameW, height = frameH)
        .align(Alignment.Center)
        .zIndex(1f),
    ) {
      CameraFocusReticleOverlay(
        smartAutoFocusTarget = smartAutoFocusTarget,
        manualFocusTarget = manualFocusTarget,
        focusPointNormalized = focusPointNormalized,
        focusSampledColorHex = focusSampledColorHex,
        smartFocusSampledColorHex = smartFocusSampledColorHex,
        lensFacing = lensFacing,
        colorFilter = colorFilter,
        exposureEv = exposureEv,
        onOpenColorInColorsTool = { hex -> openColorInColorsTool(hex) },
      )
    }

    // 2. Top Controls Overlay (Translucent Header + Permission Banner + Status/Telemetry Pills)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .onSizeChanged { topOverlayHeightPx = it.height }
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
            .height(68.dp),
          contentAlignment = Alignment.Center,
        ) {
          // Top Bar Quick Camera Controls: Flash, Timer, Aspect Ratio, Grid, Settings
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
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
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable(enabled = !isCountingDown && systemCameras.size > 1) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                toggleFrontBackPrimaryCamera()
              }
              .testTag("viewfinder_status_pill"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = buildString {
                  append(selectedCamera.shortLabel)
                  if (captureMode == CameraCaptureMode.PHOTO && photoSubMode != CameraPhotoSubMode.STANDARD) {
                    append(" • ")
                    append(photoSubMode.shortLabel)
                  }
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

    // 3. Bottom Controls Overlay (Night Mode + Device Camera Selector + Zoom Toggle + Expandable Sliders/Filters + Translucent Control Deck)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .testTag("camera_bottom_overlay"),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Floating Night Mode Icon Button (left) + Detected System Cameras Pill Selector Bar (center) + Zoom Toggle Button (right)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .padding(bottom = 6.dp)
          .testTag("camera_lens_selector_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val nightContainerColor by animateColorAsState(
          targetValue = when (nightMode) {
            CameraNightMode.ON -> MaterialTheme.colorScheme.primaryContainer
            CameraNightMode.AUTO -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f)
            CameraNightMode.OFF -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
          },
          label = "NightModeButtonContainerColor",
        )
        val nightContentColor by animateColorAsState(
          targetValue = when (nightMode) {
            CameraNightMode.ON -> MaterialTheme.colorScheme.onPrimaryContainer
            CameraNightMode.AUTO -> MaterialTheme.colorScheme.onSecondaryContainer
            CameraNightMode.OFF -> MaterialTheme.colorScheme.onSurfaceVariant
          },
          label = "NightModeButtonContentColor",
        )

        Surface(
          shape = CircleShape,
          color = nightContainerColor,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(enabled = !isCountingDown) {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              updateNightMode(nightMode.next())
            }
            .testTag("camera_night_mode_button")
            .semantics {
              contentDescription = nightMode.displayName
            },
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.NightsStay,
              contentDescription = null,
              tint = nightContentColor,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        val activeFacingCameras = if (selectedCamera.lensFacing == CameraLensFacing.FRONT) {
          frontSystemCameras.ifEmpty { systemCameras }
        } else {
          backSystemCameras.ifEmpty { systemCameras }
        }
        val facingSelectorTestTag = if (selectedCamera.lensFacing == CameraLensFacing.FRONT) {
          "camera_front_lens_selector_row"
        } else {
          "camera_back_lens_selector_row"
        }
        Row(
          modifier = Modifier
            .weight(1f, fill = false)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(facingSelectorTestTag),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          activeFacingCameras.forEach { camera ->
            val isSelected = camera.cameraId == selectedCamera.cameraId
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                Color.Transparent
              },
              modifier = Modifier
                .height(28.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = !isCountingDown) {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  selectCameraDevice(camera)
                }
                .testTag("camera_device_pill_${camera.cameraId}")
                .semantics {
                  contentDescription = camera.displayName
                },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = if (camera.lensFacing == CameraLensFacing.FRONT) {
                    Icons.Default.CameraAlt
                  } else {
                    Icons.Default.PhotoCamera
                  },
                  contentDescription = null,
                  tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  modifier = Modifier.size(12.dp),
                )
                Text(
                  text = camera.chipLabel,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  maxLines = 1,
                )
              }
            }
          }
        }

        val zoomContainerColor by animateColorAsState(
          targetValue = if (showZoomSlider) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
          },
          label = "ZoomButtonContainerColor",
        )
        val zoomContentColor by animateColorAsState(
          targetValue = if (showZoomSlider) {
            MaterialTheme.colorScheme.onPrimaryContainer
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
          label = "ZoomButtonContentColor",
        )

        Surface(
          shape = CircleShape,
          color = zoomContainerColor,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(enabled = !isCountingDown) {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              showZoomSlider = !showZoomSlider
            }
            .testTag("camera_zoom_button")
            .semantics {
              contentDescription = if (showZoomSlider) "Hide zoom slider" else "Show zoom slider"
            },
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.ZoomIn,
              contentDescription = null,
              tint = zoomContentColor,
              modifier = Modifier.size(20.dp),
            )
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Expandable Pro Sliders Panel (Exposure Compensation with SlimDotSlider)
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

          // Live Scanned QR Code / Barcode Result Card
          val activeDetectedCode = latestScannedCode
          AnimatedVisibility(
            visible = activeDetectedCode != null && captureMode == CameraCaptureMode.SCANNER,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            if (activeDetectedCode != null) {
              DetectedScanResultCard(
                code = activeDetectedCode,
                onCopy = {
                  copyTextToClipboard(
                    context = context,
                    label = activeDetectedCode.format.displayName,
                    text = activeDetectedCode.primaryCopyValue,
                    isInspection = isInspection,
                  )
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onPrimaryAction = {
                  executeScannedCodePrimaryAction(
                    context = context,
                    code = activeDetectedCode,
                    isInspection = isInspection,
                  )
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onInspectDetails = {
                  scannedCodeToInspect = activeDetectedCode
                },
                onSaveImage = {
                  val outFile = CameraEngine.createTimestampedBarcodeFile(
                    directory = cameraDirectory,
                    format = activeDetectedCode.format,
                  )
                  CameraEngine.saveBarcodeImageFile(
                    outputFile = outFile,
                    content = activeDetectedCode.rawValue,
                    format = activeDetectedCode.format,
                  )
                  refreshTrigger++
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  if (!isInspection) {
                    Toast.makeText(context, "Saved ${outFile.name}", Toast.LENGTH_SHORT).show()
                  }
                },
                onDismiss = { latestScannedCode = null },
              )
            }
          }

          // QR Code Reader & Barcode Scanner Selector Row (All / QR / Barcode + Create Code)
          AnimatedVisibility(
            visible = !showZoomSlider && captureMode == CameraCaptureMode.SCANNER,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("scanner_controls_bar"),
              horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              CameraScanFilter.entries.forEach { filterOption ->
                val selected = filterOption == scanFilter
                FilterChip(
                  selected = selected,
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    updateScanFilter(filterOption)
                  },
                  label = {
                    Text(
                      text = filterOption.shortLabel,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = when (filterOption) {
                        CameraScanFilter.ALL -> Icons.Default.QrCodeScanner
                        CameraScanFilter.QR_CODE -> Icons.Default.QrCode2
                        CameraScanFilter.BARCODE -> Icons.Default.ViewWeek
                      },
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                  },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("scan_filter_${filterOption.name}"),
                )
              }

              FilterChip(
                selected = showCreateCodeDialog,
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  showCreateCodeDialog = true
                },
                label = {
                  Text(
                    text = "Create Code",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                  )
                },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("open_code_generator_chip"),
              )
            }
          }

          // Live Scanned Document OCR Note Result Card
          val activeScannedDocument = latestScannedDocument
          AnimatedVisibility(
            visible = activeScannedDocument != null && captureMode == CameraCaptureMode.NOTE,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            if (activeScannedDocument != null) {
              ScannedNoteResultCard(
                document = activeScannedDocument,
                onAddNote = {
                  addReviewedDocumentToNotes(activeScannedDocument)
                },
                onCopy = {
                  copyTextToClipboard(
                    context = context,
                    label = activeScannedDocument.title,
                    text = activeScannedDocument.rawText,
                    isInspection = isInspection,
                  )
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onOpenNotes = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  showInlineNotesScreen = true
                  onOpenNotes?.invoke()
                },
                onDismiss = { latestScannedDocument = null },
              )
            }
          }

          // Document OCR Note Scanner Preset & Quick Actions Row
          AnimatedVisibility(
            visible = !showZoomSlider && captureMode == CameraCaptureMode.NOTE,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("note_scanner_controls_bar"),
              horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              CameraNoteSamplePreset.entries.forEach { preset ->
                val selected = preset == notePreset
                FilterChip(
                  selected = selected,
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    updateNotePreset(preset)
                  },
                  label = {
                    Text(
                      text = preset.label,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.DocumentScanner,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                  },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("note_preset_${preset.name}"),
                )
              }

              FilterChip(
                selected = false,
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  showInlineNotesScreen = true
                  onOpenNotes?.invoke()
                },
                label = {
                  Text(
                    text = "Open Notes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                  )
                },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("open_notes_tool_chip"),
              )
            }
          }

          // Photo Mode Slide-Up Navigation Pill Bar (Standard / Panorama / HDR / Burst)
          AnimatedVisibility(
            visible = !showZoomSlider && captureMode == CameraCaptureMode.PHOTO,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + expandVertically() + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + shrinkVertically() + fadeOut(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("photo_controls_bar"),
              horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              CameraPhotoSubMode.entries.forEach { subModeOption ->
                val selected = subModeOption == photoSubMode
                FilterChip(
                  selected = selected,
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    updatePhotoSubMode(subModeOption)
                  },
                  label = {
                    Text(
                      text = subModeOption.shortLabel,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = when (subModeOption) {
                        CameraPhotoSubMode.STANDARD -> Icons.Default.PhotoCamera
                        CameraPhotoSubMode.PANORAMA -> Icons.Default.PanoramaHorizontal
                        CameraPhotoSubMode.HDR -> Icons.Default.HdrOn
                        CameraPhotoSubMode.BURST -> Icons.Default.BurstMode
                      },
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                  },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("photo_sub_mode_${subModeOption.name}"),
                )
              }
            }
          }

          // Smooth Zoom Scroll Bar (replaces the active camera mode tool selector bar when selected)
          AnimatedVisibility(
            visible = showZoomSlider,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + expandVertically() + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + shrinkVertically() + fadeOut(),
          ) {
            Surface(
              shape = RoundedCornerShape(24.dp),
              color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
              modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("camera_zoom_slider_container"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.ZoomIn,
                  contentDescription = "Zoom",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
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
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  textAlign = TextAlign.End,
                  modifier = Modifier
                    .width(38.dp)
                    .testTag("camera_zoom_value_text"),
                )
              }
            }
          }

          // Mode Selector Row: Photo / Video / Scan / Note / Vision Mode Switcher Pill
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("capture_mode_selector_row"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = RoundedCornerShape(24.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.testTag("capture_mode_selector"),
            ) {
              Row(
                modifier = Modifier
                  .horizontalScroll(rememberScrollState())
                  .padding(4.dp),
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
                      .clickable(enabled = !isRecordingVideo && !isCapturingPanorama && !isCountingDown) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showFiltersBar = false
                        showAdjustmentsPanel = false
                        showZoomSlider = false
                        captureMode = mode
                      }
                      .testTag("mode_tab_${mode.name}"),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      Icon(
                        imageVector = when (mode) {
                          CameraCaptureMode.VIDEO -> Icons.Default.Videocam
                          CameraCaptureMode.SCANNER -> Icons.Default.QrCodeScanner
                          CameraCaptureMode.NOTE -> Icons.AutoMirrored.Filled.StickyNote2
                          CameraCaptureMode.VISION -> Icons.Default.Visibility
                          CameraCaptureMode.PHOTO -> Icons.Default.PhotoCamera
                        },
                        contentDescription = null,
                        tint = if (selected) {
                          MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                          MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(15.dp),
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
          }

          // Primary Shutter Row: [Pro Adjustments] — [Recent Captures Well] — [Shutter FAB] — [Flip Camera / Video Snapshot] — [Color Filters]
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .testTag("camera_shutter_row"),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // Far Left: Pro Adjustments Toggle Button (moved down from selector row)
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
                .size(42.dp)
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
                modifier = Modifier.size(20.dp),
              )
            }

            // Inner Left: Open Camera Folder in Gallery Tool Button
            Box(
              contentAlignment = Alignment.TopEnd,
              modifier = Modifier.testTag("camera_gallery_button"),
            ) {
              val latestItem = capturedMedia.firstOrNull()
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                  .size(52.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .clickable(enabled = !isRecordingVideo) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    directoryForGallery = cameraDirectory
                    onOpenGallery?.invoke(cameraDirectory)
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
                      contentDescription = "Open Camera folder in Gallery (${latestItem?.name})",
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
                      contentDescription = "Open Camera folder in Gallery",
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

            // Center: Main Shutter / Record / Scan / Note OCR Button
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
                isRecordingVideo || isCapturingPanorama -> MaterialTheme.colorScheme.error
                captureMode == CameraCaptureMode.VIDEO -> MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                captureMode == CameraCaptureMode.SCANNER ||
                  captureMode == CameraCaptureMode.NOTE ||
                  captureMode == CameraCaptureMode.VISION ->
                  MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
              },
              label = "OuterShutterRingColor",
            )

            val innerCircleColor by animateColorAsState(
              targetValue = when {
                isRecordingVideo || isCapturingPanorama -> MaterialTheme.colorScheme.error
                captureMode == CameraCaptureMode.VIDEO -> MaterialTheme.colorScheme.error
                captureMode == CameraCaptureMode.SCANNER ||
                  captureMode == CameraCaptureMode.NOTE ||
                  captureMode == CameraCaptureMode.VISION ->
                  MaterialTheme.colorScheme.tertiaryContainer
                isCountingDown -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
              },
              label = "InnerShutterColor",
            )

            Box(
              modifier = Modifier
                .size(76.dp)
                .scale(if (isRecordingVideo || isCapturingPanorama) recordPulseScale else 1f)
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
                    captureMode == CameraCaptureMode.PHOTO && photoSubMode == CameraPhotoSubMode.PANORAMA && isCapturingPanorama ->
                      "Finish panorama capture"
                    captureMode == CameraCaptureMode.PHOTO && photoSubMode == CameraPhotoSubMode.PANORAMA ->
                      "Start panorama sweep"
                    captureMode == CameraCaptureMode.SCANNER -> "Scan QR code or barcode"
                    captureMode == CameraCaptureMode.NOTE -> "Scan document to note"
                    captureMode == CameraCaptureMode.VISION -> "Analyze frame with ML Kit Vision"
                    else -> "Take photo"
                  }
                },
              contentAlignment = Alignment.Center,
            ) {
              Surface(
                shape = if (isRecordingVideo || isCapturingPanorama) RoundedCornerShape(10.dp) else CircleShape,
                color = innerCircleColor,
                modifier = Modifier.size(if (isRecordingVideo || isCapturingPanorama) 32.dp else 60.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  AnimatedContent(
                    targetState = Pair(
                      Triple(captureMode, isRecordingVideo || isCapturingPanorama, isCountingDown),
                      photoSubMode,
                    ),
                    transitionSpec = { (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut()) },
                    label = "ShutterIconAnim",
                  ) { (stateTriple, activePhotoSubMode) ->
                    val (mode, recording, counting) = stateTriple
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
                        modifier = Modifier
                          .size(20.dp)
                          .testTag("camera_shutter_stop_icon"),
                      )
                      mode == CameraCaptureMode.VIDEO -> Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier
                          .size(26.dp)
                          .testTag("camera_shutter_video_dot_icon"),
                      )
                      mode == CameraCaptureMode.SCANNER -> Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(26.dp),
                      )
                      mode == CameraCaptureMode.NOTE -> Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(26.dp),
                      )
                      mode == CameraCaptureMode.VISION -> Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier
                          .size(26.dp)
                          .testTag("camera_shutter_vision_icon"),
                      )
                      else -> Icon(
                        imageVector = when (activePhotoSubMode) {
                          CameraPhotoSubMode.PANORAMA -> Icons.Default.PanoramaHorizontal
                          CameraPhotoSubMode.HDR -> Icons.Default.HdrOn
                          CameraPhotoSubMode.BURST -> Icons.Default.BurstMode
                          CameraPhotoSubMode.STANDARD -> Icons.Default.Camera
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                          .size(26.dp)
                          .testTag("camera_shutter_photo_icon"),
                      )
                    }
                  }
                }
              }
            }

            // Inner Right: Flip Camera (or Video Snapshot while recording video)
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
                  imageVector = Icons.Default.Camera,
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
                    toggleFrontBackPrimaryCamera()
                  }
                  .testTag("camera_flip_lens_button")
                  .semantics {
                    contentDescription = "Switch front and back camera (${selectedCamera.displayName})"
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

            // Far Right: Color Filters Toggle Button (moved down from selector row)
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
                .size(42.dp)
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
                modifier = Modifier.size(20.dp),
              )
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
      systemCameras = systemCameras,
      selectedCamera = selectedCamera,
      onSelectCamera = { device ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        selectCameraDevice(device)
      },
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

  // Dialog 2: Create QR / Barcode Modal
  if (showCreateCodeDialog) {
    CreateCodeDialog(
      onSaveGeneratedCodeToFolder = { content, format ->
        val normalized = CameraEngine.normalizeBarcodeInputForEncoding(content, format)
        val outFile = CameraEngine.createTimestampedBarcodeFile(
          directory = cameraDirectory,
          format = format,
        )
        CameraEngine.saveBarcodeImageFile(
          outputFile = outFile,
          content = normalized,
          format = format,
        )
        refreshTrigger++
        showCreateCodeDialog = false
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (!isInspection) {
          Toast.makeText(context, "Saved ${outFile.name}", Toast.LENGTH_SHORT).show()
        }
      },
      onDismiss = { showCreateCodeDialog = false },
    )
  }

  // Dialog 3: Scanned Code Structured Details & Inspector Dialog
  val activeInspectedCode = scannedCodeToInspect
  if (activeInspectedCode != null) {
    ScannedCodeDetailsDialog(
      code = activeInspectedCode,
      onCopyField = { label, value ->
        copyTextToClipboard(
          context = context,
          label = label,
          text = value,
          isInspection = isInspection,
        )
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      },
      onPrimaryAction = {
        executeScannedCodePrimaryAction(
          context = context,
          code = activeInspectedCode,
          isInspection = isInspection,
        )
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      },
      onShare = {
        shareScannedCodeText(context, activeInspectedCode)
      },
      onSaveBarcodeImage = {
        val outFile = CameraEngine.createTimestampedBarcodeFile(
          directory = cameraDirectory,
          format = activeInspectedCode.format,
        )
        CameraEngine.saveBarcodeImageFile(
          outputFile = outFile,
          content = activeInspectedCode.rawValue,
          format = activeInspectedCode.format,
        )
        refreshTrigger++
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (!isInspection) {
          Toast.makeText(context, "Saved ${outFile.name}", Toast.LENGTH_SHORT).show()
        }
      },
      onDismiss = { scannedCodeToInspect = null },
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
 * Safely initializes the requested [selectedCamera] (including top-level camera IDs and
 * physical sub-cameras of logical multi-camera groups) when available on the device/emulator,
 * and cleans up the session automatically when disposed or switched.
 */
@SuppressLint("MissingPermission")
@Composable
private fun HardwareCamera2Preview(
  selectedCamera: CameraSystemDevice,
  lensFacing: CameraLensFacing,
  aspectRatio: CameraAspectRatio,
  zoomLevel: Float,
  exposureEv: Float,
  flashMode: CameraFlashMode,
  mirrorFront: Boolean,
  captureMode: CameraCaptureMode,
  focusTarget: CameraSmartFocusTarget?,
  onTextureViewReady: (TextureView?) -> Unit,
  onPreviewFrameUpdated: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val currentOnTextureViewReady by rememberUpdatedState(onTextureViewReady)
  val currentOnPreviewFrameUpdated by rememberUpdatedState(onPreviewFrameUpdated)
  var cameraDevice by remember { mutableStateOf<CameraDevice?>(null) }
  var captureSession by remember { mutableStateOf<CameraCaptureSession?>(null) }
  var previewBuilder by remember { mutableStateOf<CaptureRequest.Builder?>(null) }
  var activeCharacteristics by remember { mutableStateOf<CameraCharacteristics?>(null) }
  var activePreviewSize by remember { mutableStateOf(Pair(1440, 1080)) }
  var activeSensorOrientation by remember { mutableIntStateOf(90) }
  var lastAppliedManualOverride by remember { mutableStateOf(false) }
  var surfaceReadyTrigger by remember { mutableIntStateOf(0) }
  var isLifecycleResumed by remember {
    mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
  }
  var lifecycleResumeTrigger by remember { mutableIntStateOf(0) }
  var cameraRetryTrigger by remember { mutableIntStateOf(0) }
  var textureViewInstance by remember { mutableStateOf<TextureView?>(null) }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> {
          isLifecycleResumed = true
          lifecycleResumeTrigger++
        }
        Lifecycle.Event.ON_PAUSE,
        Lifecycle.Event.ON_STOP -> {
          isLifecycleResumed = false
        }
        else -> {}
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

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

  // Update repeating request when zoom, exposure, flash, or smart/manual focus target changes
  LaunchedEffect(zoomLevel, exposureEv, flashMode, captureMode, focusTarget, captureSession, previewBuilder) {
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

      // Apply exposure compensation and smart ML Kit / manual autofocus metering regions if supported
      if (chars != null) {
        val evRange: Range<Int>? = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
        if (evRange != null && evRange.upper > evRange.lower) {
          val normalized = ((exposureEv - MIN_CAMERA_EXPOSURE_EV) / (MAX_CAMERA_EXPOSURE_EV - MIN_CAMERA_EXPOSURE_EV))
            .coerceIn(0f, 1f)
          val targetSteps = (evRange.lower + normalized * (evRange.upper - evRange.lower)).roundToInt()
            .coerceIn(evRange.lower, evRange.upper)
          builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, targetSteps)
        }

        val activeArray = chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        val maxAfRegions = chars.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) ?: 0
        val maxAeRegions = chars.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE) ?: 0
        val availableAfModes = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) ?: intArrayOf()

        if (activeArray != null && focusTarget != null) {
          val sensorRegion = CameraEngine.computeSensorMeteringRegion(
            sensorWidth = activeArray.width(),
            sensorHeight = activeArray.height(),
            target = focusTarget,
            sensorOrientationDegrees = activeSensorOrientation,
            lensFacing = lensFacing,
            mirrorFront = mirrorFront,
            zoom = zoomLevel,
            weight = MeteringRectangle.METERING_WEIGHT_MAX,
          )
          if (sensorRegion != null) {
            val meteringRect = MeteringRectangle(
              activeArray.left + sensorRegion.left,
              activeArray.top + sensorRegion.top,
              sensorRegion.width,
              sensorRegion.height,
              sensorRegion.weight,
            )
            if (maxAfRegions > 0) {
              builder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(meteringRect))
            }
            if (maxAeRegions > 0) {
              builder.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(meteringRect))
            }
          }
        }

        val isManual = focusTarget?.isManualOverride == true
        val continuousMode = if (
          captureMode == CameraCaptureMode.VIDEO &&
          CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO in availableAfModes
        ) {
          CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
        } else if (CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE in availableAfModes) {
          CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        } else if (CaptureRequest.CONTROL_AF_MODE_AUTO in availableAfModes) {
          CaptureRequest.CONTROL_AF_MODE_AUTO
        } else {
          null
        }

        if (isManual && CaptureRequest.CONTROL_AF_MODE_AUTO in availableAfModes) {
          builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
          builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
          session.capture(builder.build(), null, null)
          builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
          lastAppliedManualOverride = true
        } else {
          if (lastAppliedManualOverride) {
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL)
            session.capture(builder.build(), null, null)
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
            lastAppliedManualOverride = false
          }
          if (continuousMode != null) {
            builder.set(CaptureRequest.CONTROL_AF_MODE, continuousMode)
          }
        }
      }

      session.setRepeatingRequest(builder.build(), null, null)
    } catch (_: Throwable) {
      // Ignore if camera session was closed
    }
  }

  DisposableEffect(
    selectedCamera.cameraId,
    selectedCamera.openCameraId,
    selectedCamera.physicalCameraId,
    lensFacing,
    surfaceReadyTrigger,
    isLifecycleResumed,
    lifecycleResumeTrigger,
    cameraRetryTrigger,
  ) {
    val tv = textureViewInstance
    if (!isLifecycleResumed || tv == null || !tv.isAvailable) {
      return@DisposableEffect onDispose {}
    }

    val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
      ?: return@DisposableEffect onDispose {}

    val availableIds = try {
      manager.cameraIdList.toList()
    } catch (_: Throwable) {
      emptyList()
    }

    val desiredLensConst = if (lensFacing == CameraLensFacing.FRONT) {
      CameraCharacteristics.LENS_FACING_FRONT
    } else {
      CameraCharacteristics.LENS_FACING_BACK
    }

    val cameraId = when {
      selectedCamera.openCameraId in availableIds -> selectedCamera.openCameraId
      selectedCamera.cameraId in availableIds -> selectedCamera.cameraId
      else -> {
        try {
          availableIds.firstOrNull { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == desiredLensConst
          } ?: availableIds.firstOrNull()
        } catch (_: Throwable) {
          null
        }
      }
    }

    if (cameraId == null) {
      return@DisposableEffect onDispose {}
    }

    val requestedPhysicalCameraId = selectedCamera.physicalCameraId?.takeIf {
      it != cameraId && it !in availableIds
    }

    val mainHandler = Handler(Looper.getMainLooper())
    var isDisposed = false
    var isOpeningOrOpened = false
    var openedDevice: CameraDevice? = null
    var openedSession: CameraCaptureSession? = null
    var previewSurface: Surface? = null

    val retryRunnable = Runnable {
      if (!isDisposed && isLifecycleResumed && openedDevice == null) {
        cameraRetryTrigger++
      }
    }

    val availabilityCallback = object : CameraManager.AvailabilityCallback() {
      override fun onCameraAvailable(id: String) {
        if (!isDisposed && id == cameraId && isLifecycleResumed && openedDevice == null && !isOpeningOrOpened) {
          mainHandler.removeCallbacks(retryRunnable)
          mainHandler.post(retryRunnable)
        }
      }
    }

    try {
      manager.registerAvailabilityCallback(availabilityCallback, mainHandler)
    } catch (_: Throwable) {}

    try {
      val chars = try {
        manager.getCameraCharacteristics(selectedCamera.cameraId)
      } catch (_: Throwable) {
        manager.getCameraCharacteristics(cameraId)
      }
      activeCharacteristics = chars
      val sensorOrientation = chars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
      activeSensorOrientation = sensorOrientation
      val supportedSizes = chars
        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        ?.getOutputSizes(SurfaceTexture::class.java)
        ?.map { Pair(it.width, it.height) }
        ?.takeIf { it.isNotEmpty() }
        ?: try {
          manager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceTexture::class.java)
            ?.map { Pair(it.width, it.height) }
            .orEmpty()
        } catch (_: Throwable) {
          emptyList()
        }

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

      isOpeningOrOpened = true
      manager.openCamera(
        cameraId,
        object : CameraDevice.StateCallback() {
          override fun onOpened(device: CameraDevice) {
            if (isDisposed || !isLifecycleResumed) {
              try {
                device.close()
              } catch (_: Throwable) {}
              return
            }
            openedDevice = device
            cameraDevice = device
            onTextureViewReady(tv)
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
              fun createStandardSession() {
                try {
                  device.createCaptureSession(
                    listOf(surface),
                    object : CameraCaptureSession.StateCallback() {
                      override fun onConfigured(session: CameraCaptureSession) {
                        if (isDisposed || !isLifecycleResumed) {
                          try {
                            session.close()
                          } catch (_: Throwable) {}
                          return
                        }
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

              if (requestedPhysicalCameraId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                  val outputConfig = OutputConfiguration(surface).apply {
                    setPhysicalCameraId(requestedPhysicalCameraId)
                  }
                  val sessionConfig = SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    listOf(outputConfig),
                    ContextCompat.getMainExecutor(context),
                    object : CameraCaptureSession.StateCallback() {
                      override fun onConfigured(session: CameraCaptureSession) {
                        if (isDisposed || !isLifecycleResumed) {
                          try {
                            session.close()
                          } catch (_: Throwable) {}
                          return
                        }
                        openedSession = session
                        captureSession = session
                        try {
                          session.setRepeatingRequest(builder.build(), null, mainHandler)
                        } catch (_: Throwable) {}
                      }

                      override fun onConfigureFailed(session: CameraCaptureSession) {
                        if (!isDisposed && isLifecycleResumed) {
                          createStandardSession()
                        }
                      }
                    },
                  )
                  device.createCaptureSession(sessionConfig)
                } catch (_: Throwable) {
                  createStandardSession()
                }
              } else {
                createStandardSession()
              }
            } catch (_: Throwable) {}
          }

          override fun onDisconnected(device: CameraDevice) {
            isOpeningOrOpened = false
            try {
              device.close()
            } catch (_: Throwable) {}
            openedDevice = null
            cameraDevice = null
            captureSession = null
            if (!isDisposed && isLifecycleResumed) {
              mainHandler.removeCallbacks(retryRunnable)
              mainHandler.postDelayed(retryRunnable, 350L)
            }
          }

          override fun onError(device: CameraDevice, error: Int) {
            isOpeningOrOpened = false
            try {
              device.close()
            } catch (_: Throwable) {}
            openedDevice = null
            cameraDevice = null
            captureSession = null
            if (!isDisposed && isLifecycleResumed) {
              mainHandler.removeCallbacks(retryRunnable)
              mainHandler.postDelayed(retryRunnable, 350L)
            }
          }
        },
        mainHandler,
      )
    } catch (_: Throwable) {
      isOpeningOrOpened = false
      if (!isDisposed && isLifecycleResumed) {
        mainHandler.removeCallbacks(retryRunnable)
        mainHandler.postDelayed(retryRunnable, 350L)
      }
    }

    onDispose {
      isDisposed = true
      isOpeningOrOpened = false
      mainHandler.removeCallbacks(retryRunnable)
      try {
        manager.unregisterAvailabilityCallback(availabilityCallback)
      } catch (_: Throwable) {}
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
            currentOnTextureViewReady(this@apply)
            applyCenterCropTransform(this@apply, viewWidth = width, viewHeight = height)
            surfaceReadyTrigger++
          }

          override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            applyCenterCropTransform(this@apply, viewWidth = width, viewHeight = height)
          }

          override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            currentOnTextureViewReady(null)
            textureViewInstance = null
            return true
          }

          override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            currentOnPreviewFrameUpdated?.invoke()
          }
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
  systemCameras: List<CameraSystemDevice>,
  selectedCamera: CameraSystemDevice,
  onSelectCamera: (CameraSystemDevice) -> Unit,
  onDismiss: () -> Unit,
  onConfirm: (String, CameraPhotoFormat, Boolean) -> Unit,
) {
  val context = LocalContext.current
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

  val folderPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    if (result.resultCode == android.app.Activity.RESULT_OK) {
      val treeUri = result.data?.data
      if (treeUri != null) {
        try {
          val takeFlags = (result.data?.flags ?: 0) and
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
          val effectiveFlags = if (takeFlags != 0) {
            takeFlags
          } else {
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
          }
          context.contentResolver.takePersistableUriPermission(treeUri, effectiveFlags)
        } catch (_: Throwable) {}
        val resolvedPath = CameraEngine.resolveDisplayPathFromTreeUri(treeUri)
        if (!resolvedPath.isNullOrBlank()) {
          textFieldValue = TextFieldValue(
            text = resolvedPath,
            selection = TextRange(0, resolvedPath.length),
          )
        }
      }
    }
  }

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

        // Detected System Cameras Selector Chips & Hardware Specs (Separated into Back and Front bars)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          val backDialogCameras = remember(systemCameras) {
            CameraEngine.getCamerasForFacing(systemCameras, CameraLensFacing.BACK)
          }
          val frontDialogCameras = remember(systemCameras) {
            CameraEngine.getCamerasForFacing(systemCameras, CameraLensFacing.FRONT)
          }

          if (backDialogCameras.isNotEmpty()) {
            Text(
              text = "Back Cameras (${backDialogCameras.size})",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("camera_settings_back_lens_row"),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              backDialogCameras.forEach { camera ->
                val isSelected = camera.cameraId == selectedCamera.cameraId
                FilterChip(
                  selected = isSelected,
                  onClick = { onSelectCamera(camera) },
                  label = { Text(camera.chipLabel) },
                  leadingIcon = if (isSelected) {
                    {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  } else {
                    {
                      Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("camera_settings_lens_chip_${camera.cameraId}"),
                )
              }
            }
          }

          if (frontDialogCameras.isNotEmpty()) {
            Text(
              text = "Front Cameras (${frontDialogCameras.size})",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("camera_settings_front_lens_row"),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              frontDialogCameras.forEach { camera ->
                val isSelected = camera.cameraId == selectedCamera.cameraId
                FilterChip(
                  selected = isSelected,
                  onClick = { onSelectCamera(camera) },
                  label = { Text(camera.chipLabel) },
                  leadingIcon = if (isSelected) {
                    {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  } else {
                    {
                      Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("camera_settings_lens_chip_${camera.cameraId}"),
                )
              }
            }
          }

          Text(
            text = selectedCamera.specsSummary,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("camera_settings_lens_specs"),
          )
        }

        // Quick Folder Presets + Scoped Storage Folder Picker
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
          FilterChip(
            selected = false,
            onClick = {
              try {
                folderPickerLauncher.launch(
                  CameraEngine.createFolderAccessIntent(
                    context = context,
                    currentDisplayPath = textFieldValue.text.ifBlank { DEFAULT_CAMERA_FOLDER_DISPLAY },
                  )
                )
              } catch (_: Throwable) {}
            },
            label = { Text("Choose Folder") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.CreateNewFolder,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("camera_choose_scoped_folder_chip"),
          )
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
        shape = CircleShape,
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
        shape = CircleShape,
      ) {
        Text("Cancel")
      }
    },
  )
}

// =========================================================================
// QR Code Reader & Barcode Scanner UI Components & Dialogs
// =========================================================================

/**
 * Renders a ZXing boolean bit matrix for [content] in [format] onto a Compose [Canvas].
 */
@Composable
private fun BarcodeBitMatrixPreviewCanvas(
  content: String,
  format: CameraBarcodeFormat,
  modifier: Modifier = Modifier,
  darkColor: Color = Color(0xFF111114),
  lightColor: Color = Color.White,
) {
  val matrix = remember(content, format) {
    CameraEngine.encodeCodeBitMatrix(
      content = content,
      format = format,
      width = if (format.is2D) 140 else 220,
      height = if (format.is2D) 140 else 80,
    )
  }

  Canvas(modifier = modifier) {
    drawRoundRect(
      color = lightColor,
      cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
      size = size,
    )
    val mat = matrix ?: return@Canvas
    val rows = mat.size
    val cols = if (rows > 0) mat[0].size else return@Canvas
    if (cols <= 0) return@Canvas

    val cellW = size.width / cols.toFloat()
    val cellH = size.height / rows.toFloat()

    for (r in 0 until rows) {
      val row = mat[r]
      val top = r * cellH
      var c = 0
      while (c < cols) {
        if (row[c]) {
          var runEnd = c + 1
          while (runEnd < cols && row[runEnd]) {
            runEnd++
          }
          drawRect(
            color = darkColor,
            topLeft = Offset(c * cellW, top),
            size = Size((runEnd - c) * cellW + 0.4f, cellH + 0.4f),
          )
          c = runEnd
        } else {
          c++
        }
      }
    }
  }
}

/**
 * Animated viewfinder reticle overlay for the QR Code Reader and Barcode Scanner.
 * Adapts its framing aspect ratio to [scanFilter] (square for QR/All, wide horizontal rectangle for 1D Barcode)
 * and renders a smooth sweeping laser scan line.
 */
@Composable
private fun ScannerReticleOverlayCanvas(
  scanFilter: CameraScanFilter,
  hasDetectedCode: Boolean,
  modifier: Modifier = Modifier,
  topInset: Dp = 24.dp,
) {
  val bracketColor = if (hasDetectedCode) {
    MaterialTheme.colorScheme.tertiary
  } else {
    MaterialTheme.colorScheme.primary
  }

  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val minDim = size.minDimension
      val reticleW = when (scanFilter) {
        CameraScanFilter.BARCODE -> (minDim * 0.78f).coerceAtMost(size.width * 0.86f)
        else -> (minDim * 0.64f).coerceAtMost(260.dp.toPx())
      }
      val reticleH = when (scanFilter) {
        CameraScanFilter.BARCODE -> (reticleW * 0.48f).coerceAtLeast(110.dp.toPx())
        else -> reticleW
      }

      val left = (size.width - reticleW) / 2f
      val top = topInset.toPx().coerceAtMost((size.height - reticleH).coerceAtLeast(0f))
      val right = left + reticleW
      val bottom = top + reticleH

      // Subtle guide outline
      drawRoundRect(
        color = Color.White.copy(alpha = 0.24f),
        topLeft = Offset(left, top),
        size = Size(reticleW, reticleH),
        cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
        style = Stroke(width = 1.5.dp.toPx()),
      )

      // Corner bracket arms
      val armLen = minOf(reticleW, reticleH) * 0.22f
      val strokePx = 4.dp.toPx()

      // Top-Left bracket
      drawLine(
        color = bracketColor,
        start = Offset(left, top + armLen),
        end = Offset(left, top),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = bracketColor,
        start = Offset(left, top),
        end = Offset(left + armLen, top),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )

      // Top-Right bracket
      drawLine(
        color = bracketColor,
        start = Offset(right - armLen, top),
        end = Offset(right, top),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = bracketColor,
        start = Offset(right, top),
        end = Offset(right, top + armLen),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )

      // Bottom-Left bracket
      drawLine(
        color = bracketColor,
        start = Offset(left, bottom - armLen),
        end = Offset(left, bottom),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = bracketColor,
        start = Offset(left, bottom),
        end = Offset(left + armLen, bottom),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )

      // Bottom-Right bracket
      drawLine(
        color = bracketColor,
        start = Offset(right - armLen, bottom),
        end = Offset(right, bottom),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = bracketColor,
        start = Offset(right, bottom - armLen),
        end = Offset(right, bottom),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
    }
  }
}

/**
 * Viewfinder document reticle overlay rendered when [CameraCaptureMode.NOTE] is active.
 */
@Composable
private fun DocumentNoteReticleOverlayCanvas(
  hasScannedDocument: Boolean,
  modifier: Modifier = Modifier,
  topInset: Dp = 24.dp,
) {
  val bracketColor = if (hasScannedDocument) {
    MaterialTheme.colorScheme.tertiary
  } else {
    MaterialTheme.colorScheme.primary
  }
  val guideLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
  val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)

  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val minDim = size.minDimension
      val docW = (minDim * 0.64f).coerceAtMost(260.dp.toPx())
      val docH = (docW * 1.25f).coerceAtMost(size.height * 0.78f)
      val left = (size.width - docW) / 2f
      val top = topInset.toPx().coerceAtMost((size.height - docH).coerceAtLeast(0f))
      val right = left + docW
      val bottom = top + docH
      val armLen = minOf(docW, docH) * 0.14f
      val strokePx = 3.5.dp.toPx()

      // Subtle document frame border
      drawRoundRect(
        color = borderColor,
        topLeft = Offset(left, top),
        size = Size(docW, docH),
        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
        style = Stroke(width = 1.dp.toPx()),
      )

      // Subtle horizontal text line guides inside the document frame
      val lineStartX = left + docW * 0.12f
      val lineEndX = right - docW * 0.12f
      for (i in 1..4) {
        val lineY = top + (docH * (0.22f + i * 0.14f))
        val endX = if (i == 4) left + docW * 0.62f else lineEndX
        drawLine(
          color = guideLineColor,
          start = Offset(lineStartX, lineY),
          end = Offset(endX, lineY),
          strokeWidth = 1.5.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }

      // Corner brackets
      drawLine(
        color = bracketColor,
        start = Offset(left, top + armLen),
        end = Offset(left, top),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = bracketColor,
        start = Offset(left, top),
        end = Offset(left + armLen, top),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )

      drawLine(
        color = bracketColor,
        start = Offset(right - armLen, top),
        end = Offset(right, top),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = bracketColor,
        start = Offset(right, top),
        end = Offset(right, top + armLen),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )

      drawLine(
        color = bracketColor,
        start = Offset(left, bottom - armLen),
        end = Offset(left, bottom),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = bracketColor,
        start = Offset(left, bottom),
        end = Offset(left + armLen, bottom),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )

      drawLine(
        color = bracketColor,
        start = Offset(right - armLen, bottom),
        end = Offset(right, bottom),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = bracketColor,
        start = Offset(right, bottom - armLen),
        end = Offset(right, bottom),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
      )
    }
  }
}

/**
 * Viewfinder panoramic horizon track overlay rendered when [CameraCaptureMode.PHOTO] and
 * [CameraPhotoSubMode.PANORAMA] are active.
 * Displays real-time sweep progress, captured segment dots, horizon level indicator, and status prompt.
 */
@Composable
private fun PanoramaReticleOverlayCanvas(
  isCapturingPanorama: Boolean,
  sweepProgress: Float,
  capturedSegments: Int,
  targetSegments: Int = PANORAMA_TARGET_SEGMENTS,
  verticalOffsetNorm: Float = 0f,
  modifier: Modifier = Modifier,
  topInset: Dp = 24.dp,
) {
  val bracketColor = if (isCapturingPanorama) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.tertiary
  }
  val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
  val progressFillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)
  val guideLineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
  val activeDotColor = MaterialTheme.colorScheme.tertiary
  val clampedProgress = sweepProgress.coerceIn(0f, 1f)
  val totalSegs = targetSegments.coerceAtLeast(2)

  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val panoW = (size.width * 0.86f).coerceAtLeast(180.dp.toPx())
      val panoH = (panoW * 0.36f).coerceIn(88.dp.toPx(), 140.dp.toPx())
      val left = (size.width - panoW) / 2f
      val top = (topInset.toPx() + (size.height - topInset.toPx() - panoH) * 0.35f)
        .coerceAtMost((size.height - panoH).coerceAtLeast(0f))
      val right = left + panoW
      val bottom = top + panoH
      val cy = (top + bottom) / 2f
      val armLen = minOf(panoW, panoH) * 0.22f
      val strokePx = 3.5.dp.toPx()

      // Live sweep progress fill inside the panoramic track
      if (isCapturingPanorama && clampedProgress > 0f) {
        drawRoundRect(
          color = progressFillColor,
          topLeft = Offset(left, top),
          size = Size(panoW * clampedProgress, panoH),
          cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
        )
      }

      // Wide panoramic sweep track container
      drawRoundRect(
        color = trackColor,
        topLeft = Offset(left, top),
        size = Size(panoW, panoH),
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
        style = Stroke(width = 1.5.dp.toPx()),
      )

      // Dashed horizontal horizon alignment line
      drawLine(
        color = guideLineColor,
        start = Offset(left + 16.dp.toPx(), cy),
        end = Offset(right - 16.dp.toPx(), cy),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
      )

      // Segment vertical dividers & alignment target dots
      for (i in 1 until totalSegs) {
        val segX = left + (panoW * i) / totalSegs.toFloat()
        drawLine(
          color = trackColor,
          start = Offset(segX, top + 10.dp.toPx()),
          end = Offset(segX, bottom - 10.dp.toPx()),
          strokeWidth = 1.dp.toPx(),
        )
      }
      for (i in 0 until totalSegs) {
        val dotX = left + panoW * ((i + 0.5f) / totalSegs.toFloat())
        val isCaptured = isCapturingPanorama && i < capturedSegments
        drawCircle(
          color = if (isCaptured) activeDotColor else guideLineColor,
          radius = if (isCaptured) 5.5.dp.toPx() else 3.5.dp.toPx(),
          center = Offset(dotX, cy),
        )
      }

      // Live moving reticle ring tracking horizontal sweep progress and vertical horizon tilt
      if (isCapturingPanorama) {
        val cursorX = (left + 16.dp.toPx() + (panoW - 32.dp.toPx()) * clampedProgress)
          .coerceIn(left + 16.dp.toPx(), right - 16.dp.toPx())
        val cursorY = (cy + verticalOffsetNorm.coerceIn(-1f, 1f) * (panoH * 0.28f))
          .coerceIn(top + 14.dp.toPx(), bottom - 14.dp.toPx())
        drawCircle(
          color = activeDotColor,
          radius = 10.dp.toPx(),
          center = Offset(cursorX, cursorY),
          style = Stroke(width = 2.dp.toPx()),
        )
      }

      // Corner brackets
      drawLine(bracketColor, Offset(left, top + armLen), Offset(left, top), strokePx, StrokeCap.Round)
      drawLine(bracketColor, Offset(left, top), Offset(left + armLen, top), strokePx, StrokeCap.Round)
      drawLine(bracketColor, Offset(right - armLen, top), Offset(right, top), strokePx, StrokeCap.Round)
      drawLine(bracketColor, Offset(right, top), Offset(right, top + armLen), strokePx, StrokeCap.Round)
      drawLine(bracketColor, Offset(left, bottom - armLen), Offset(left, bottom), strokePx, StrokeCap.Round)
      drawLine(bracketColor, Offset(left, bottom), Offset(left + armLen, bottom), strokePx, StrokeCap.Round)
      drawLine(bracketColor, Offset(right - armLen, bottom), Offset(right, bottom), strokePx, StrokeCap.Round)
      drawLine(bracketColor, Offset(right, bottom - armLen), Offset(right, bottom), strokePx, StrokeCap.Round)
    }

    // Panorama guidance / active sweep status badge
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = if (isCapturingPanorama) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
      } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f)
      },
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = (topInset - 4.dp).coerceAtLeast(8.dp))
        .testTag("panorama_sweep_status_badge"),
    ) {
      Text(
        text = if (isCapturingPanorama) {
          "SWEEPING ${capturedSegments.coerceAtLeast(1)}/$totalSegs • Pan & tap shutter to finish"
        } else {
          "Panorama • Tap shutter and pan slowly"
        },
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = if (isCapturingPanorama) {
          MaterialTheme.colorScheme.onPrimaryContainer
        } else {
          MaterialTheme.colorScheme.onSurface
        },
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
      )
    }
  }
}

/**
 * Dynamically drawn colored bounding boxes over detected visual elements in [CameraCaptureMode.VISION] mode,
 * displaying each element's label and confidence percentage (only for tags >= 80% confidence).
 *
 * When a new detection result arrives, bounding boxes whose labels match boxes from the previous
 * detection smoothly move and resize from their previous bounds into the new ones.
 */
@Composable
private fun VisionReticleOverlayCanvas(
  visionResult: CameraVisionAnalysisResult?,
  modifier: Modifier = Modifier,
  topInset: Dp = 16.dp,
) {
  val targetTags = remember(visionResult) {
    CameraEngine.suppressOverlappingVisionTags(
      visionResult?.tags
        ?.filter { it.confidenceClamped >= MIN_VISION_CONFIDENCE_THRESHOLD }
        .orEmpty()
    ).take(5)
  }

  var transitionFromTags by remember { mutableStateOf(targetTags) }
  var transitionTargetTags by remember { mutableStateOf(targetTags) }
  val morphProgress = remember { Animatable(1f) }
  var colorIndexByLabel by remember {
    mutableStateOf(
      CameraEngine.resolveStableVisionTagColorIndices(
        previousTags = emptyList(),
        currentTags = targetTags,
      )
    )
  }

  val isNewTargetPending = transitionTargetTags != targetTags
  val snapshotFromTags = remember(isNewTargetPending, transitionFromTags, transitionTargetTags, morphProgress.value) {
    if (isNewTargetPending) {
      CameraEngine.interpolateVisionTags(
        previousTags = transitionFromTags,
        targetTags = transitionTargetTags,
        fraction = morphProgress.value,
      )
    } else {
      transitionFromTags
    }
  }
  val hasSharedLabelsWithPending = remember(snapshotFromTags, targetTags) {
    CameraEngine.hasMatchingVisionTagLabels(snapshotFromTags, targetTags)
  }
  val effectiveMorphFraction = if (isNewTargetPending && hasSharedLabelsWithPending) {
    0f
  } else if (isNewTargetPending) {
    1f
  } else {
    morphProgress.value
  }

  val visibleTags = remember(snapshotFromTags, targetTags, effectiveMorphFraction) {
    CameraEngine.interpolateVisionTags(
      previousTags = snapshotFromTags,
      targetTags = targetTags,
      fraction = effectiveMorphFraction,
    )
  }

  val activeColorIndexByLabel = remember(isNewTargetPending, snapshotFromTags, targetTags, colorIndexByLabel) {
    if (isNewTargetPending) {
      CameraEngine.resolveStableVisionTagColorIndices(
        previousTags = snapshotFromTags,
        currentTags = targetTags,
        previousColorIndices = colorIndexByLabel,
      )
    } else {
      colorIndexByLabel
    }
  }

  LaunchedEffect(targetTags) {
    if (transitionTargetTags != targetTags) {
      val fromSnapshot = CameraEngine.interpolateVisionTags(
        previousTags = transitionFromTags,
        targetTags = transitionTargetTags,
        fraction = morphProgress.value,
      )
      val shared = CameraEngine.hasMatchingVisionTagLabels(fromSnapshot, targetTags)
      transitionFromTags = fromSnapshot
      transitionTargetTags = targetTags
      colorIndexByLabel = CameraEngine.resolveStableVisionTagColorIndices(
        previousTags = fromSnapshot,
        currentTags = targetTags,
        previousColorIndices = colorIndexByLabel,
      )
      if (shared) {
        morphProgress.snapTo(0f)
        morphProgress.animateTo(
          targetValue = 1f,
          animationSpec = tween(
            durationMillis = VISION_BOX_MORPH_DURATION_MS,
            easing = FastOutSlowInEasing,
          ),
        )
      } else {
        morphProgress.snapTo(1f)
      }
    }
  }

  val boxStrokeColors = listOf(
    MaterialTheme.colorScheme.tertiary,
    MaterialTheme.colorScheme.primary,
    MaterialTheme.colorScheme.secondary,
    MaterialTheme.colorScheme.inversePrimary,
    MaterialTheme.colorScheme.error,
  )
  val badgeContainerColors = listOf(
    MaterialTheme.colorScheme.tertiaryContainer,
    MaterialTheme.colorScheme.primaryContainer,
    MaterialTheme.colorScheme.secondaryContainer,
    MaterialTheme.colorScheme.surfaceContainerHigh,
    MaterialTheme.colorScheme.errorContainer,
  )
  val badgeContentColors = listOf(
    MaterialTheme.colorScheme.onTertiaryContainer,
    MaterialTheme.colorScheme.onPrimaryContainer,
    MaterialTheme.colorScheme.onSecondaryContainer,
    MaterialTheme.colorScheme.onSurface,
    MaterialTheme.colorScheme.onErrorContainer,
  )

  val density = LocalDensity.current

  BoxWithConstraints(modifier = modifier) {
    val stageW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
    val stageH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
    val topInsetPx = with(density) { topInset.toPx() }
    val bottomMarginPx = with(density) { 170.dp.toPx() }.coerceAtMost(stageH * 0.35f)
    val usableTop = topInsetPx.coerceAtMost(stageH * 0.3f)
    val usableBottom = (stageH - bottomMarginPx).coerceAtLeast(usableTop + 80f)

    Canvas(modifier = Modifier.fillMaxSize()) {
      val cornerRadiusPx = 10.dp.toPx()
      val strokePx = 2.dp.toPx()
      val bracketStrokePx = 3.5.dp.toPx()

      visibleTags.forEachIndexed { idx, tag ->
        val normBox = (tag.boundingBox ?: CameraEngine.defaultVisionBoundingBoxForRank(idx)).coerceNormalized()
        val colorSlot = activeColorIndexByLabel[tag.label.trim().lowercase(java.util.Locale.US)] ?: idx
        val boxColor = boxStrokeColors[colorSlot % boxStrokeColors.size]

        val leftPx = (normBox.left * size.width).coerceIn(12.dp.toPx(), size.width - 64.dp.toPx())
        val rightPx = (normBox.right * size.width).coerceIn(leftPx + 52.dp.toPx(), size.width - 12.dp.toPx())
        val topPx = (normBox.top * size.height).coerceIn(usableTop, usableBottom - 44.dp.toPx())
        val bottomPx = (normBox.bottom * size.height).coerceIn(topPx + 40.dp.toPx(), usableBottom)
        val boxW = rightPx - leftPx
        val boxH = bottomPx - topPx
        val armLen = (minOf(boxW, boxH) * 0.20f).coerceIn(10.dp.toPx(), 26.dp.toPx())

        // Translucent colored region fill
        drawRoundRect(
          color = boxColor.copy(alpha = 0.12f),
          topLeft = Offset(leftPx, topPx),
          size = Size(boxW, boxH),
          cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        )

        // Colored bounding box outline
        drawRoundRect(
          color = boxColor.copy(alpha = 0.85f),
          topLeft = Offset(leftPx, topPx),
          size = Size(boxW, boxH),
          cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
          style = Stroke(width = strokePx),
        )

        // Corner accent brackets
        drawLine(
          color = boxColor,
          start = Offset(leftPx, topPx + armLen),
          end = Offset(leftPx, topPx),
          strokeWidth = bracketStrokePx,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = boxColor,
          start = Offset(leftPx, topPx),
          end = Offset(leftPx + armLen, topPx),
          strokeWidth = bracketStrokePx,
          cap = StrokeCap.Round,
        )

        drawLine(
          color = boxColor,
          start = Offset(rightPx - armLen, topPx),
          end = Offset(rightPx, topPx),
          strokeWidth = bracketStrokePx,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = boxColor,
          start = Offset(rightPx, topPx),
          end = Offset(rightPx, topPx + armLen),
          strokeWidth = bracketStrokePx,
          cap = StrokeCap.Round,
        )

        drawLine(
          color = boxColor,
          start = Offset(leftPx, bottomPx - armLen),
          end = Offset(leftPx, bottomPx),
          strokeWidth = bracketStrokePx,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = boxColor,
          start = Offset(leftPx, bottomPx),
          end = Offset(leftPx + armLen, bottomPx),
          strokeWidth = bracketStrokePx,
          cap = StrokeCap.Round,
        )

        drawLine(
          color = boxColor,
          start = Offset(rightPx - armLen, bottomPx),
          end = Offset(rightPx, bottomPx),
          strokeWidth = bracketStrokePx,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = boxColor,
          start = Offset(rightPx, bottomPx - armLen),
          end = Offset(rightPx, bottomPx),
          strokeWidth = bracketStrokePx,
          cap = StrokeCap.Round,
        )
      }
    }

    // Attached Label + Confidence Percentage Pills on each colored bounding box
    if (visibleTags.isNotEmpty()) {
      val labelPlacements = remember(visibleTags, stageW, stageH, usableTop, usableBottom, density) {
        CameraEngine.computeNonOverlappingVisionLabelPlacements(
          tags = visibleTags,
          stageWidthPx = stageW,
          usableTopPx = usableTop,
          usableBottomPx = usableBottom,
          charWidthPx = with(density) { 7.5.dp.toPx() },
          baseBadgePaddingPx = with(density) { 38.dp.toPx() },
          badgeHeightPx = with(density) { 26.dp.toPx() },
          insetPx = with(density) { 6.dp.toPx() },
          collisionPaddingPx = with(density) { 4.dp.toPx() },
          edgeMarginPx = with(density) { 12.dp.toPx() },
          stageHeightPx = stageH,
        )
      }
      Box(
        modifier = Modifier
          .fillMaxSize()
          .testTag("vision_detected_card"),
      ) {
        labelPlacements.forEachIndexed { idx, placement ->
          val tag = placement.tag
          val colorSlot = activeColorIndexByLabel[tag.label.trim().lowercase(java.util.Locale.US)] ?: idx
          val borderColor = boxStrokeColors[colorSlot % boxStrokeColors.size]
          val containerColor = badgeContainerColors[colorSlot % badgeContainerColors.size]
          val contentColor = badgeContentColors[colorSlot % badgeContentColors.size]

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = containerColor.copy(alpha = 0.92f),
            border = BorderStroke(1.5.dp, borderColor),
            tonalElevation = 3.dp,
            modifier = Modifier
              .offset {
                IntOffset(
                  x = placement.leftPx.roundToInt(),
                  y = placement.topPx.roundToInt(),
                )
              }
              .testTag("vision_box_badge_${tag.label}"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(7.dp)
                  .clip(CircleShape)
                  .background(borderColor),
              )
              Text(
                text = tag.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "${tag.confidencePercent}%",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Floating card displayed above the bottom controls when a document is scanned via OCR in Note mode,
 * allowing the user to review the extracted title and contents before pressing Add Note.
 */
@Composable
private fun ScannedNoteResultCard(
  document: CameraScannedDocument,
  onAddNote: () -> Unit,
  onCopy: () -> Unit,
  onOpenNotes: () -> Unit,
  onDismiss: () -> Unit,
) {
  val isSaved = document.savedNoteId != null

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("note_scanner_detected_card"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.DocumentScanner,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.size(20.dp),
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSaved) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.secondaryContainer
                },
              ) {
                Text(
                  text = if (isSaved) "SAVED TO NOTES" else "REVIEW NOTE",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isSaved) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                  },
                  modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag(if (isSaved) "note_scanner_saved_badge" else "note_scanner_review_badge"),
                )
              }
              Text(
                text = "${document.wordCount} words · ${document.lineCount} lines",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Text(
              text = document.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("note_scanner_detected_title"),
            )
            Text(
              text = document.bodyContent.ifBlank { document.rawText },
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("note_scanner_detected_content"),
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .testTag("note_scanner_dismiss_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss scanned note result",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
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
        Button(
          onClick = onAddNote,
          enabled = !isSaved,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("note_scanner_add_note_button"),
        ) {
          Icon(
            imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = if (isSaved) "Added" else "Add Note",
            style = MaterialTheme.typography.labelMedium,
          )
        }

        if (isSaved) {
          Button(
            onClick = onOpenNotes,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("note_scanner_open_notes_button"),
          ) {
            Icon(Icons.AutoMirrored.Filled.StickyNote2, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "Open Notes",
              style = MaterialTheme.typography.labelMedium,
            )
          }
        } else {
          TextButton(
            onClick = onOpenNotes,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("note_scanner_open_notes_button"),
          ) {
            Icon(Icons.AutoMirrored.Filled.StickyNote2, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Open Notes",
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }

        TextButton(
          onClick = onCopy,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("note_scanner_copy_button"),
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copy Text", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
  }
}

/**
 * Floating card displayed above the bottom controls when a QR code or barcode is decoded.
 */
@Composable
private fun DetectedScanResultCard(
  code: CameraScannedCode,
  onCopy: () -> Unit,
  onPrimaryAction: () -> Unit,
  onInspectDetails: () -> Unit,
  onSaveImage: () -> Unit,
  onDismiss: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("scanner_detected_card"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = when {
                code.payloadType == CameraScanPayloadType.WIFI -> Icons.Default.Wifi
                code.is2D -> Icons.Default.QrCode2
                else -> Icons.Default.ViewWeek
              },
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.size(20.dp),
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
              ) {
                Text(
                  text = code.format.shortBadge,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("scanner_detected_format_badge"),
                )
              }
              Text(
                text = code.payloadType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Text(
              text = code.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("scanner_detected_title"),
            )
            Text(
              text = code.rawValue,
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("scanner_detected_raw_value"),
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .testTag("scanner_result_dismiss_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss scan result",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      // Action Pills Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Button(
          onClick = onCopy,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("scanner_result_copy_button"),
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = if (code.payloadType == CameraScanPayloadType.WIFI) "Copy Password" else "Copy",
            style = MaterialTheme.typography.labelMedium,
          )
        }

        if (code.payloadType == CameraScanPayloadType.URL ||
          code.payloadType == CameraScanPayloadType.PHONE ||
          code.payloadType == CameraScanPayloadType.EMAIL ||
          code.payloadType == CameraScanPayloadType.GEO
        ) {
          TextButton(
            onClick = onPrimaryAction,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("scanner_result_action_button"),
          ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(code.payloadType.primaryActionLabel, style = MaterialTheme.typography.labelMedium)
          }
        }

        TextButton(
          onClick = onInspectDetails,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("scanner_result_details_button"),
        ) {
          Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Details", style = MaterialTheme.typography.labelMedium)
        }

        TextButton(
          onClick = onSaveImage,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("scanner_result_save_button"),
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save PNG", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
  }
}

/**
 * Dedicated modal dialog for generating and saving a 2D QR/Matrix code or 1D barcode image,
 * styled per `DESIGN_LANGUAGE.md` Section 5.2.
 */
@Composable
private fun CreateCodeDialog(
  onSaveGeneratedCodeToFolder: (String, CameraBarcodeFormat) -> Unit,
  onDismiss: () -> Unit,
) {
  var generatorFormat by rememberSaveable { mutableStateOf(CameraBarcodeFormat.QR_CODE) }
  var generatorInput by rememberSaveable { mutableStateOf("https://toolbox.example.com") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("create_code_dialog"),
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
            imageVector = Icons.Default.QrCode2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Create Code",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Generate a 2D QR code or 1D barcode",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Live Preview Card of Generated Code
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            BarcodeBitMatrixPreviewCanvas(
              content = generatorInput.ifBlank { generatorFormat.defaultSamplePayload },
              format = generatorFormat,
              modifier = Modifier
                .fillMaxWidth()
                .height(if (generatorFormat.is2D) 136.dp else 88.dp)
                .testTag("generator_preview_canvas"),
            )
            Text(
              text = "${generatorFormat.displayName} (${if (generatorFormat.is2D) "2D Matrix" else "1D Barcode"})",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Format Selector Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          CameraBarcodeFormat.entries.forEach { fmt ->
            val selected = fmt == generatorFormat
            FilterChip(
              selected = selected,
              onClick = {
                generatorFormat = fmt
                if (generatorInput.isBlank() ||
                  CameraBarcodeFormat.entries.any { it.defaultSamplePayload == generatorInput }
                ) {
                  generatorInput = fmt.defaultSamplePayload
                }
              },
              label = { Text(fmt.shortBadge) },
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.testTag("generator_format_chip_${fmt.name}"),
            )
          }
        }

        OutlinedTextField(
          value = generatorInput,
          onValueChange = { generatorInput = it },
          label = { Text("QR / Barcode content or digits") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          trailingIcon = {
            if (generatorInput.isNotEmpty()) {
              IconButton(onClick = { generatorInput = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear input")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("generator_content_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSaveGeneratedCodeToFolder(
            generatorInput.ifBlank { generatorFormat.defaultSamplePayload },
            generatorFormat,
          )
        },
        shape = CircleShape,
        modifier = Modifier.testTag("generator_save_png_button"),
      ) {
        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save PNG")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier.testTag("close_create_code_button"),
      ) {
        Text("Cancel")
      }
    },
  )
}

/**
 * Detailed inspector dialog for a decoded [CameraScannedCode], displaying its rendered code preview,
 * extracted structured fields, and one-tap Copy / Open / Share / Save actions.
 */
@Composable
private fun ScannedCodeDetailsDialog(
  code: CameraScannedCode,
  onCopyField: (String, String) -> Unit,
  onPrimaryAction: () -> Unit,
  onShare: () -> Unit,
  onSaveBarcodeImage: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("scanned_code_details_dialog"),
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
            .background(MaterialTheme.colorScheme.tertiaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (code.is2D) Icons.Default.QrCode2 else Icons.Default.ViewWeek,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = code.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${code.format.displayName} • ${code.payloadType.displayName}",
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
        // Rendered Code Context Preview Card
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            BarcodeBitMatrixPreviewCanvas(
              content = code.rawValue,
              format = code.format,
              modifier = Modifier
                .fillMaxWidth()
                .height(if (code.is2D) 132.dp else 84.dp),
            )
            Text(
              text = code.rawValue,
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("details_dialog_raw_value"),
            )
          }
        }

        // Structured Metadata Fields
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          code.fields.entries.forEach { (k, v) ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = k,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = v,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp),
              )
            }
          }
        }

        // Action Row: Save PNG & Share
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          TextButton(
            onClick = onSaveBarcodeImage,
            shape = CircleShape,
            modifier = Modifier
              .weight(1f)
              .testTag("details_save_barcode_button"),
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Save Image")
          }
          TextButton(
            onClick = onShare,
            shape = CircleShape,
            modifier = Modifier
              .weight(1f)
              .testTag("details_share_code_button"),
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Share")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onCopyField(code.format.displayName, code.primaryCopyValue) },
        shape = CircleShape,
        modifier = Modifier.testTag("details_copy_code_button"),
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (code.payloadType == CameraScanPayloadType.WIFI) "Copy Password" else "Copy")
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (code.payloadType == CameraScanPayloadType.URL ||
          code.payloadType == CameraScanPayloadType.PHONE ||
          code.payloadType == CameraScanPayloadType.EMAIL ||
          code.payloadType == CameraScanPayloadType.GEO
        ) {
          TextButton(
            onClick = onPrimaryAction,
            shape = CircleShape,
          ) {
            Text(code.payloadType.primaryActionLabel)
          }
        }
        TextButton(
          onClick = onDismiss,
          shape = CircleShape,
          modifier = Modifier.testTag("close_scanned_details_button"),
        ) {
          Text("Close")
        }
      }
    },
  )
}

private fun copyTextToClipboard(
  context: Context,
  label: String,
  text: String,
  isInspection: Boolean,
) {
  try {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    if (!isInspection) {
      Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
  } catch (_: Throwable) {}
}

private fun shareScannedCodeText(
  context: Context,
  code: CameraScannedCode,
) {
  try {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, "${code.format.displayName}: ${code.title}")
      putExtra(Intent.EXTRA_TEXT, code.rawValue)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share ${code.format.displayName}"))
  } catch (_: Throwable) {
    Toast.makeText(context, "Unable to share code", Toast.LENGTH_SHORT).show()
  }
}

private fun executeScannedCodePrimaryAction(
  context: Context,
  code: CameraScannedCode,
  isInspection: Boolean,
) {
  try {
    when (code.payloadType) {
      CameraScanPayloadType.URL -> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(code.rawValue.trim())).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      }
      CameraScanPayloadType.PHONE -> {
        val num = code.fields["Phone"] ?: code.rawValue.removePrefix("tel:")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num")).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      }
      CameraScanPayloadType.EMAIL -> {
        val email = code.fields["Email"] ?: code.rawValue.removePrefix("mailto:")
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      }
      CameraScanPayloadType.GEO -> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(code.rawValue.trim())).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      }
      else -> {
        copyTextToClipboard(
          context = context,
          label = code.format.displayName,
          text = code.primaryCopyValue,
          isInspection = isInspection,
        )
      }
    }
  } catch (_: Throwable) {
    copyTextToClipboard(
      context = context,
      label = code.format.displayName,
      text = code.primaryCopyValue,
      isInspection = isInspection,
    )
  }
}

/**
 * Renders the autofocus indicator (`camera_smart_af_indicator` for Smart ML Kit Object Detection
 * Autofocus, or `camera_focus_indicator` during a 7-second Manual Tap-to-Focus Override) with the
 * normal autofocus appearance: focus ring, center color dot, and center-radius sampled hex color
 * pill (`camera_focus_color_pill`), without a box or label badge.
 */
@Composable
private fun CameraFocusReticleOverlay(
  smartAutoFocusTarget: CameraSmartFocusTarget?,
  manualFocusTarget: CameraSmartFocusTarget?,
  focusPointNormalized: Offset?,
  focusSampledColorHex: String?,
  smartFocusSampledColorHex: String?,
  lensFacing: CameraLensFacing,
  colorFilter: CameraColorFilter,
  exposureEv: Float,
  onOpenColorInColorsTool: (String) -> Unit,
) {
  val density = LocalDensity.current
  val isManualFocus = focusPointNormalized != null
  val activeFocusPoint = focusPointNormalized
    ?: if (manualFocusTarget == null && smartAutoFocusTarget != null) {
      Offset(smartAutoFocusTarget.clampedX, smartAutoFocusTarget.clampedY)
    } else {
      null
    }

  if (activeFocusPoint != null) {
    val indicatorTag = if (isManualFocus) "camera_focus_indicator" else "camera_smart_af_indicator"
    val focusRingColor = MaterialTheme.colorScheme.tertiary
    val sampledHex = if (isManualFocus) focusSampledColorHex else smartFocusSampledColorHex
    val activeHex = sampledHex ?: CameraEngine.sampleSyntheticFocusColorHex(
      normX = activeFocusPoint.x,
      normY = activeFocusPoint.y,
      lensFacing = lensFacing,
      filter = colorFilter,
      exposureEv = exposureEv,
    )
    val swatchColor = remember(activeHex) {
      val rgb = ColorMath.parseHex(activeHex)
      if (rgb != null) {
        Color(red = rgb.first, green = rgb.second, blue = rgb.third)
      } else {
        focusRingColor
      }
    }
    var measuredPillWidthPx by remember { mutableIntStateOf(0) }

    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .testTag(indicatorTag),
    ) {
      val boxW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
      val boxH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
      val cx = boxW * activeFocusPoint.x
      val cy = boxH * activeFocusPoint.y
      val radiusPx = with(density) { 32.dp.toPx() }
      val gapPx = with(density) { 6.dp.toPx() }
      val edgeMarginPx = with(density) { 8.dp.toPx() }
      val fallbackPillWidthPx = with(density) { 86.dp.toPx() }
      val effectivePillW = if (measuredPillWidthPx > 0) measuredPillWidthPx.toFloat() else fallbackPillWidthPx
      val pillLeftPx = (cx - effectivePillW / 2f).coerceIn(
        edgeMarginPx,
        (boxW - effectivePillW - edgeMarginPx).coerceAtLeast(edgeMarginPx),
      )
      val pillTopPx = (cy + radiusPx + gapPx).coerceIn(
        edgeMarginPx,
        (boxH - with(density) { 36.dp.toPx() }).coerceAtLeast(edgeMarginPx),
      )

      Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
          color = focusRingColor,
          radius = radiusPx,
          center = Offset(cx, cy),
          style = Stroke(width = 2.dp.toPx()),
        )
        drawCircle(
          color = swatchColor,
          radius = 4.dp.toPx(),
          center = Offset(cx, cy),
        )
      }

      // Hex Color Pill below the auto-focus circle
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, swatchColor.copy(alpha = 0.65f)),
        modifier = Modifier
          .onSizeChanged { measuredPillWidthPx = it.width }
          .offset {
            IntOffset(
              x = pillLeftPx.roundToInt(),
              y = pillTopPx.roundToInt(),
            )
          }
          .clip(CircleShape)
          .clickable { onOpenColorInColorsTool(activeHex) }
          .testTag("camera_focus_color_pill"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Box(
            modifier = Modifier
              .size(12.dp)
              .clip(CircleShape)
              .background(swatchColor)
              .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), CircleShape)
              .testTag("camera_focus_color_swatch"),
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = activeHex,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.testTag("camera_focus_color_hex_text"),
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun CameraScreenPreview() {
  ToolboxTheme {
    CameraScreen()
  }
}

@Preview(showBackground = true)
@Composable
private fun CameraVisionScreenPreview() {
  ToolboxTheme {
    CameraScreen(initialCaptureMode = CameraCaptureMode.VISION)
  }
}
