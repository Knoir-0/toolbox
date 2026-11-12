package com.example.toolbox.ui.tools

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
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
import android.net.Uri
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
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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
  onOpenGallery: ((File) -> Unit)? = null,
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
    val saved = prefs?.getBoolean(PREF_KEY_CAMERA_MIRROR_FRONT, false) ?: false
    mutableStateOf(saved)
  }

  fun updateMirrorFrontCamera(mirror: Boolean) {
    mirrorFrontCamera = mirror
    prefs?.edit()?.putBoolean(PREF_KEY_CAMERA_MIRROR_FRONT, mirror)?.apply()
  }

  // QR Code Reader & Barcode Scanner states
  var scanFilter by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_CAMERA_SCAN_FILTER, null)
    mutableStateOf(CameraScanFilter.fromNameOrDefault(saved))
  }

  var activeSamplePreset by rememberSaveable {
    mutableStateOf(CameraScanSamplePreset.QR_URL)
  }

  var customTargetCodeContent by rememberSaveable {
    mutableStateOf<String?>(null)
  }

  var customTargetCodeFormat by rememberSaveable {
    mutableStateOf<CameraBarcodeFormat?>(null)
  }

  var showSampleTargetOnViewfinder by rememberSaveable {
    mutableStateOf(false)
  }

  fun updateScanFilter(newFilter: CameraScanFilter) {
    scanFilter = newFilter
    prefs?.edit()?.putString(PREF_KEY_CAMERA_SCAN_FILTER, newFilter.name)?.apply()
    val customFmt = customTargetCodeFormat
    if (customFmt != null && !customFmt.matchesFilter(newFilter)) {
      customTargetCodeContent = null
      customTargetCodeFormat = null
    }
    if (!activeSamplePreset.format.matchesFilter(newFilter)) {
      activeSamplePreset = CameraScanSamplePreset.presetsForFilter(newFilter).first()
    }
  }

  var autoScanEnabled by rememberSaveable {
    val saved = prefs?.getBoolean(PREF_KEY_CAMERA_AUTO_SCAN, true) ?: true
    mutableStateOf(saved)
  }

  fun updateAutoScanEnabled(enabled: Boolean) {
    autoScanEnabled = enabled
    prefs?.edit()?.putBoolean(PREF_KEY_CAMERA_AUTO_SCAN, enabled)?.apply()
  }

  var scanHistory by remember {
    val savedJson = prefs?.getString(PREF_KEY_CAMERA_SCAN_HISTORY, null)
    mutableStateOf(CameraEngine.deserializeScanHistory(savedJson))
  }

  var latestScannedCode by remember {
    mutableStateOf<CameraScannedCode?>(null)
  }

  fun recordScannedCode(code: CameraScannedCode, showToast: Boolean = false) {
    latestScannedCode = code
    val updated = buildList {
      add(code)
      scanHistory.forEach { existing ->
        if (existing.rawValue != code.rawValue || existing.format != code.format) {
          add(existing)
        }
      }
    }.take(50)
    scanHistory = updated
    prefs?.edit()?.putString(PREF_KEY_CAMERA_SCAN_HISTORY, CameraEngine.serializeScanHistory(updated))?.apply()
    if (showToast && !isInspection) {
      Toast.makeText(context, "Scanned ${code.format.shortBadge}: ${code.title}", Toast.LENGTH_SHORT).show()
    }
  }

  fun clearScanHistory() {
    scanHistory = emptyList()
    prefs?.edit()?.remove(PREF_KEY_CAMERA_SCAN_HISTORY)?.apply()
  }

  fun deleteScanHistoryItem(item: CameraScannedCode) {
    val updated = scanHistory.filterNot { it.id == item.id }
    scanHistory = updated
    prefs?.edit()?.putString(PREF_KEY_CAMERA_SCAN_HISTORY, CameraEngine.serializeScanHistory(updated))?.apply()
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

  // Dialog & cross-tool navigation states
  var showFolderSettingsDialog by remember { mutableStateOf(false) }
  var showScanHistoryDialog by remember { mutableStateOf(false) }
  var scannedCodeToInspect by remember { mutableStateOf<CameraScannedCode?>(null) }
  var directoryForGallery by remember { mutableStateOf<File?>(null) }

  // Active target code for procedural/sample viewfinder & on-demand scanning
  val activeTargetFormat = customTargetCodeFormat ?: activeSamplePreset.format
  val activeTargetPayload = customTargetCodeContent ?: activeSamplePreset.payload

  // Continuous live frame QR & Barcode scanning when in SCANNER mode and hardware TextureView is rendering
  LaunchedEffect(captureMode, autoScanEnabled, scanFilter, activeTextureView) {
    if (captureMode != CameraCaptureMode.SCANNER || !autoScanEnabled || isInspection) {
      return@LaunchedEffect
    }
    while (isActive && captureMode == CameraCaptureMode.SCANNER && autoScanEnabled) {
      val tv = activeTextureView
      if (tv != null && tv.isAvailable) {
        val rawFrame = try {
          tv.getBitmap(480, 480)
        } catch (_: Throwable) {
          null
        }
        if (rawFrame != null && rawFrame.width > 32 && rawFrame.height > 32) {
          val decoded = withContext(Dispatchers.Default) {
            CameraEngine.decodeCodesFromBitmap(rawFrame, filter = scanFilter)
          }
          if (decoded.isNotEmpty()) {
            val first = decoded.first()
            if (latestScannedCode?.rawValue != first.rawValue || latestScannedCode?.format != first.format) {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              recordScannedCode(first, showToast = false)
            }
          }
        }
      }
      delay(400L)
    }
  }

  fun performCodeScan() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    showShutterFlash = true

    // 1. Try decoding from live hardware TextureView frame first
    val hwDecoded = try {
      val tv = activeTextureView
      val raw = if (tv != null && tv.isAvailable) tv.bitmap else null
      if (raw != null && raw.width > 32 && raw.height > 32) {
        CameraEngine.decodeCodesFromBitmap(raw, filter = scanFilter)
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

    // 2. Decode the active viewfinder target code through ZXing's pixel decoder pipeline
    val targetFormat = if (activeTargetFormat.matchesFilter(scanFilter)) {
      activeTargetFormat
    } else {
      CameraScanSamplePreset.presetsForFilter(scanFilter).first().format
    }
    val targetContent = if (activeTargetFormat.matchesFilter(scanFilter)) {
      activeTargetPayload
    } else {
      CameraScanSamplePreset.presetsForFilter(scanFilter).first().payload
    }

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

    if (captureMode == CameraCaptureMode.SCANNER) {
      performCodeScan()
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

        // QR Code & Barcode Scanner Viewfinder Target & Reticle Overlay
        if (captureMode == CameraCaptureMode.SCANNER) {
          val effectiveTargetFormat = if (activeTargetFormat.matchesFilter(scanFilter)) {
            activeTargetFormat
          } else {
            CameraScanSamplePreset.presetsForFilter(scanFilter).first().format
          }
          val effectiveTargetPayload = if (activeTargetFormat.matchesFilter(scanFilter)) {
            activeTargetPayload
          } else {
            CameraScanSamplePreset.presetsForFilter(scanFilter).first().payload
          }

          if (showSampleTargetOnViewfinder) {
            ScannerSampleCodeOverlay(
              content = effectiveTargetPayload,
              format = effectiveTargetFormat,
              modifier = Modifier
                .align(Alignment.Center)
                .testTag("camera_scanner_sample_target"),
            )
          }

          ScannerReticleOverlayCanvas(
            scanFilter = scanFilter,
            hasDetectedCode = latestScannedCode != null,
            modifier = Modifier
              .fillMaxSize()
              .testTag("camera_scanner_reticle_overlay"),
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
                    when {
                      isRecordingVideo -> MaterialTheme.colorScheme.errorContainer
                      captureMode == CameraCaptureMode.SCANNER -> MaterialTheme.colorScheme.tertiaryContainer
                      else -> MaterialTheme.colorScheme.primaryContainer
                    }
                  ),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = when (captureMode) {
                    CameraCaptureMode.VIDEO -> Icons.Default.Videocam
                    CameraCaptureMode.SCANNER -> Icons.Default.QrCodeScanner
                    CameraCaptureMode.PHOTO -> Icons.Default.PhotoCamera
                  },
                  contentDescription = null,
                  tint = when {
                    isRecordingVideo -> MaterialTheme.colorScheme.error
                    captureMode == CameraCaptureMode.SCANNER -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.primary
                  },
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
                  text = when {
                    isRecordingVideo -> "Recording (${CameraEngine.formatTimerDurationMs(elapsedVideoMs)}) • $directoryDisplayPath"
                    captureMode == CameraCaptureMode.SCANNER -> {
                      val scanCountLabel = if (scanHistory.size == 1) "1 scan" else "${scanHistory.size} scans"
                      "${scanFilter.displayName} • $scanCountLabel • $directoryDisplayPath"
                    }
                    else -> {
                      val countLabel = if (capturedMedia.size == 1) "1 capture" else "${capturedMedia.size} captures"
                      "$countLabel • ${aspectRatio.displayName} • $directoryDisplayPath"
                    }
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isRecordingVideo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }

            // Top Bar Quick Camera Controls: QR/Barcode Scanner Toggle, Flash, Timer, Aspect Ratio, Grid, Settings
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              // Quick QR & Barcode Scanner Mode / History Button
              IconButton(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  if (captureMode == CameraCaptureMode.SCANNER) {
                    showScanHistoryDialog = true
                  } else {
                    captureMode = CameraCaptureMode.SCANNER
                  }
                },
                enabled = !isRecordingVideo && !isCountingDown,
                modifier = Modifier.testTag("camera_qr_scanner_toggle_button"),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = if (captureMode == CameraCaptureMode.SCANNER) {
                      "Open scan history and code generator"
                    } else {
                      "Switch to QR code and barcode scanner"
                    },
                    tint = if (captureMode == CameraCaptureMode.SCANNER) {
                      MaterialTheme.colorScheme.tertiary
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
                  )
                  if (scanHistory.isNotEmpty()) {
                    Text(
                      text = scanHistory.size.toString(),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.ExtraBold,
                      color = MaterialTheme.colorScheme.tertiary,
                      modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .testTag("scanner_history_count_badge"),
                    )
                  }
                }
              }

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

          // Live Scanned QR Code / Barcode Result Card
          val activeDetectedCode = latestScannedCode
          AnimatedVisibility(
            visible = activeDetectedCode != null,
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

          // QR Code Reader & Barcode Scanner Controls Bar (visible in SCANNER mode)
          AnimatedVisibility(
            visible = captureMode == CameraCaptureMode.SCANNER,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("scanner_controls_bar"),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              // Row 1: Scanner Symbology Filter (All Codes / QR Code / Barcode) + History & Generator Button
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                          text = filterOption.displayName,
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
                }

                // Scan History & Generator Dialog Button
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = MaterialTheme.colorScheme.secondaryContainer,
                  modifier = Modifier
                    .padding(start = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showScanHistoryDialog = true }
                    .testTag("scanner_history_button"),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Default.History,
                      contentDescription = "Scan history and code generator",
                      tint = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.size(16.dp),
                    )
                    Text(
                      text = if (scanHistory.isEmpty()) "Codes" else "${scanHistory.size}",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                  }
                }
              }

              // Row 2: Sample Target Presets & Custom Code Generator Trigger
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                val visiblePresets = CameraScanSamplePreset.presetsForFilter(scanFilter)
                visiblePresets.forEach { preset ->
                  val isSelected = customTargetCodeContent == null && activeSamplePreset == preset
                  FilterChip(
                    selected = isSelected,
                    onClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      customTargetCodeContent = null
                      customTargetCodeFormat = null
                      activeSamplePreset = preset
                      showSampleTargetOnViewfinder = true
                    },
                    label = {
                      Text(
                        text = preset.label,
                        style = MaterialTheme.typography.labelSmall,
                      )
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("scan_sample_preset_${preset.name}"),
                  )
                }

                FilterChip(
                  selected = customTargetCodeContent != null,
                  onClick = { showScanHistoryDialog = true },
                  label = {
                    Text(
                      text = if (customTargetCodeContent != null) "Custom Code" else "Create Code",
                      style = MaterialTheme.typography.labelSmall,
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = null,
                      modifier = Modifier.size(14.dp),
                    )
                  },
                  shape = RoundedCornerShape(18.dp),
                  modifier = Modifier.testTag("open_code_generator_chip"),
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

            // Center: Photo / Video / Scan Mode Switcher Pill
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
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                      Icon(
                        imageVector = when (mode) {
                          CameraCaptureMode.VIDEO -> Icons.Default.Videocam
                          CameraCaptureMode.SCANNER -> Icons.Default.QrCodeScanner
                          CameraCaptureMode.PHOTO -> Icons.Default.PhotoCamera
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
            // Left: Open Camera Folder in Gallery Tool Button
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

            // Center: Main Shutter / Record / Scan Button
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
                captureMode == CameraCaptureMode.SCANNER -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
              },
              label = "OuterShutterRingColor",
            )

            val innerCircleColor by animateColorAsState(
              targetValue = when {
                isRecordingVideo -> MaterialTheme.colorScheme.error
                captureMode == CameraCaptureMode.VIDEO -> MaterialTheme.colorScheme.error
                captureMode == CameraCaptureMode.SCANNER -> MaterialTheme.colorScheme.tertiaryContainer
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
                    captureMode == CameraCaptureMode.SCANNER -> "Scan QR code or barcode"
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
                      mode == CameraCaptureMode.SCANNER -> Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
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

  // Dialog 2: QR Code & Barcode Scan History + Code Generator Hub Dialog
  if (showScanHistoryDialog) {
    ScanHistoryAndGeneratorDialog(
      history = scanHistory,
      autoScanEnabled = autoScanEnabled,
      showSampleTarget = showSampleTargetOnViewfinder,
      onToggleAutoScan = { updateAutoScanEnabled(it) },
      onToggleSampleTarget = { showSampleTargetOnViewfinder = it },
      onSelectCode = { code ->
        showScanHistoryDialog = false
        scannedCodeToInspect = code
      },
      onCopyCode = { code ->
        copyTextToClipboard(
          context = context,
          label = code.format.displayName,
          text = code.primaryCopyValue,
          isInspection = isInspection,
        )
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      },
      onDeleteHistoryItem = { deleteScanHistoryItem(it) },
      onClearHistory = { clearScanHistory() },
      onLoadGeneratedCodeToScanner = { content, format ->
        val normalized = CameraEngine.normalizeBarcodeInputForEncoding(content, format)
        customTargetCodeContent = normalized
        customTargetCodeFormat = format
        showSampleTargetOnViewfinder = true
        if (!format.matchesFilter(scanFilter)) {
          updateScanFilter(CameraScanFilter.ALL)
        }
        captureMode = CameraCaptureMode.SCANNER
        showScanHistoryDialog = false
        val analyzed = CameraEngine.analyzeScannedPayload(normalized, format)
        recordScannedCode(analyzed, showToast = true)
      },
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
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (!isInspection) {
          Toast.makeText(context, "Saved ${outFile.name}", Toast.LENGTH_SHORT).show()
        }
      },
      onDismiss = { showScanHistoryDialog = false },
    )
  }

  // Dialog 7: Scanned Code Structured Details & Inspector Dialog
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

// =========================================================================
// QR Code Reader & Barcode Scanner UI Components & Dialogs
// =========================================================================

/**
 * Renders a crisp, ZXing-encoded 2D QR/Matrix code or 1D linear barcode card inside the viewfinder
 * so the scanner can be visually inspected and tested even without a physical barcode in front of the lens.
 */
@Composable
private fun ScannerSampleCodeOverlay(
  content: String,
  format: CameraBarcodeFormat,
  modifier: Modifier = Modifier,
) {
  val cardWidth = if (format.is2D) 186.dp else 248.dp
  val cardHeight = if (format.is2D) 186.dp else 118.dp

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = Color.White.copy(alpha = 0.94f),
    tonalElevation = 4.dp,
    modifier = modifier
      .size(width = cardWidth, height = cardHeight)
      .padding(4.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      BarcodeBitMatrixPreviewCanvas(
        content = content,
        format = format,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "${format.shortBadge} • ${content.lineSequence().firstOrNull()?.take(24).orEmpty()}",
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1B1B1F),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

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
) {
  val infiniteTransition = rememberInfiniteTransition(label = "ScannerLaserSweep")
  val laserProgress by infiniteTransition.animateFloat(
    initialValue = 0.08f,
    targetValue = 0.92f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1900, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "LaserSweepProgress",
  )

  val bracketColor = if (hasDetectedCode) {
    MaterialTheme.colorScheme.tertiary
  } else {
    MaterialTheme.colorScheme.primary
  }
  val laserColor = MaterialTheme.colorScheme.tertiary

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
      val top = (size.height - reticleH) / 2f
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

      // Sweeping laser line inside reticle
      val laserY = top + reticleH * laserProgress
      drawLine(
        brush = Brush.horizontalGradient(
          colors = listOf(
            laserColor.copy(alpha = 0f),
            laserColor.copy(alpha = 0.9f),
            laserColor.copy(alpha = 0f),
          ),
          startX = left + 8.dp.toPx(),
          endX = right - 8.dp.toPx(),
        ),
        start = Offset(left + 8.dp.toPx(), laserY),
        end = Offset(right - 8.dp.toPx(), laserY),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
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
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
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
 * Dialog combining Scan History, Scanner Options, and a QR Code / Barcode Generator
 * styled per `DESIGN_LANGUAGE.md` Section 5.2.
 */
@Composable
private fun ScanHistoryAndGeneratorDialog(
  history: List<CameraScannedCode>,
  autoScanEnabled: Boolean,
  showSampleTarget: Boolean,
  onToggleAutoScan: (Boolean) -> Unit,
  onToggleSampleTarget: (Boolean) -> Unit,
  onSelectCode: (CameraScannedCode) -> Unit,
  onCopyCode: (CameraScannedCode) -> Unit,
  onDeleteHistoryItem: (CameraScannedCode) -> Unit,
  onClearHistory: () -> Unit,
  onLoadGeneratedCodeToScanner: (String, CameraBarcodeFormat) -> Unit,
  onSaveGeneratedCodeToFolder: (String, CameraBarcodeFormat) -> Unit,
  onDismiss: () -> Unit,
) {
  var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0 = History, 1 = Create QR / Barcode
  var generatorFormat by rememberSaveable { mutableStateOf(CameraBarcodeFormat.QR_CODE) }
  var generatorInput by rememberSaveable { mutableStateOf("https://toolbox.example.com") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("scanner_history_dialog"),
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
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "QR & Barcode Hub",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${history.size} scanned codes • Reader & Generator",
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
        // Mode Switcher Pills: History vs Generator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FilterChip(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            label = { Text("Scan History (${history.size})") },
            leadingIcon = {
              Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("scanner_hub_tab_history"),
          )
          FilterChip(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            label = { Text("Create Code") },
            leadingIcon = {
              Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("scanner_hub_tab_generator"),
          )
        }

        if (selectedTab == 0) {
          // Live Auto-Scan & Sample Target Toggles
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Continuous Live Frame Scan",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "Automatically decode QR & barcodes in camera view",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Switch(
                  checked = autoScanEnabled,
                  onCheckedChange = onToggleAutoScan,
                  modifier = Modifier.testTag("auto_scan_switch"),
                )
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Show Sample Code in Viewfinder",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "Display interactive test QR/barcode target on screen",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Switch(
                  checked = showSampleTarget,
                  onCheckedChange = onToggleSampleTarget,
                  modifier = Modifier.testTag("sample_target_switch"),
                )
              }
            }
          }

          if (history.isEmpty()) {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("empty_scan_history_card"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.QrCodeScanner,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(32.dp),
                )
                Text(
                  text = "No codes scanned yet",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Scan any QR code or 1D barcode using the camera or create one in the generator tab.",
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
                .heightIn(max = 260.dp)
                .testTag("scan_history_list"),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(history, key = { it.id }) { item ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectCode(item) }
                    .testTag("scan_history_item_${item.format.name}"),
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                        imageVector = if (item.is2D) Icons.Default.QrCode2 else Icons.Default.ViewWeek,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                      )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                      Text(
                        text = "${item.format.shortBadge} • ${item.payloadType.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }

                    IconButton(
                      onClick = { onCopyCode(item) },
                      modifier = Modifier.size(34.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy ${item.title}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                      )
                    }

                    IconButton(
                      onClick = { onDeleteHistoryItem(item) },
                      modifier = Modifier.size(34.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete ${item.title}",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                      )
                    }
                  }
                }
              }
            }
          }
        } else {
          // Tab 1: QR Code & Barcode Generator
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

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Button(
                onClick = {
                  onLoadGeneratedCodeToScanner(
                    generatorInput.ifBlank { generatorFormat.defaultSamplePayload },
                    generatorFormat,
                  )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("generator_scan_now_button"),
              ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan in Camera")
              }

              TextButton(
                onClick = {
                  onSaveGeneratedCodeToFolder(
                    generatorInput.ifBlank { generatorFormat.defaultSamplePayload },
                    generatorFormat,
                  )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("generator_save_png_button"),
              ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save PNG")
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
        modifier = Modifier.testTag("close_scanner_hub_button"),
      ) {
        Text("Done")
      }
    },
    dismissButton = {
      if (selectedTab == 0 && history.isNotEmpty()) {
        TextButton(
          onClick = onClearHistory,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("clear_scan_history_button"),
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Clear All")
        }
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
            shape = RoundedCornerShape(12.dp),
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
            shape = RoundedCornerShape(12.dp),
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
        shape = RoundedCornerShape(12.dp),
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
            shape = RoundedCornerShape(12.dp),
          ) {
            Text(code.payloadType.primaryActionLabel)
          }
        }
        TextButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
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

@Preview(showBackground = true)
@Composable
private fun CameraScreenPreview() {
  ToolboxTheme {
    CameraScreen()
  }
}
