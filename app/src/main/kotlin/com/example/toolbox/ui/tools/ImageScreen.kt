package com.example.toolbox.ui.tools

import com.example.toolbox.ui.theme.toolboxScreenBackground

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.view.Surface as AndroidSurface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import com.example.toolbox.ui.theme.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropOriginal
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import com.example.toolbox.ui.theme.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.toolbox.ui.theme.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.toolbox.ui.theme.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Standalone Image Viewer tool screen extracted from the Gallery tool.
 *
 * - When opened without an [initialFile] or [initialPhoto], presents a standard [ToolboxTopBar]
 *   and [OpenFileInitialView] so the user can pick an image file from device storage or open the
 *   built-in sample image.
 * - When an image file is active (passed via [initialFile] / [initialPhoto] from Files or Gallery,
 *   or selected in-tool), renders the full black-background Image Viewer with horizontal paging
 *   across sibling images in the folder, pinch-to-zoom (`50%..500%`), dismissible top/bottom
 *   translucent overlay bars, OCR/QR/Smart Tag recognition inspector, interactive camera-style
 *   color picker reticle with hex pill link to the Colors tool, one-tap Edit link to Image Editor,
 *   Share, Delete confirmation, and bottom thumbnail preview strip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(
  modifier: Modifier = Modifier,
  initialFile: File? = null,
  initialFiles: List<File>? = null,
  initialDirectory: File? = null,
  initialPhoto: GalleryPhoto? = null,
  initialPhotos: List<GalleryPhoto>? = null,
  photoIndexMap: Map<String, GalleryPhotoIndexEntry>? = null,
  onPhotoIndexed: ((GalleryPhotoIndexEntry) -> Unit)? = null,
  onNavigateBack: (() -> Unit)? = null,
  onOpenImageEditor: ((File) -> Unit)? = null,
  onOpenVideoEditor: ((File) -> Unit)? = null,
  onOpenColorPicker: ((String) -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()

  val startingFile = remember(initialFile?.absolutePath, initialPhoto?.path) {
    initialPhoto?.file ?: initialFile
  }

  var currentFile by remember(startingFile?.absolutePath) {
    mutableStateOf(startingFile)
  }
  var refreshTrigger by remember { mutableIntStateOf(0) }

  var fileForImageEditor by remember { mutableStateOf<File?>(null) }
  var fileForVideoEditor by remember { mutableStateOf<File?>(null) }
  var inlineColorPickerHex by rememberSaveable { mutableStateOf<String?>(null) }

  val indexCacheFile = remember(context, isInspection) {
    if (isInspection) null else GalleryEngine.getDefaultIndexCacheFile(context)
  }
  var localPhotoIndexMap by remember {
    mutableStateOf(
      photoIndexMap ?: GalleryEngine.loadIndexCacheFromDisk(indexCacheFile)
    )
  }
  val effectivePhotoIndexMap = photoIndexMap ?: localPhotoIndexMap

  val systemFilePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch {
        val copied = withContext(Dispatchers.IO) {
          copyUriToCacheImageFile(context, uri)
        }
        if (copied != null) {
          currentFile = copied
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
          Toast.makeText(context, "Unable to open selected image", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  val activeInlineColorHex = inlineColorPickerHex
  if (activeInlineColorHex != null) {
    com.example.toolbox.ui.main.SyncToolScreenOrientation(
      tool = com.example.toolbox.ui.main.Tool.COLOR_PICKER,
      restorePreviousOnDispose = true,
    )
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

  val activeImageEditorFile = fileForImageEditor
  if (activeImageEditorFile != null) {
    com.example.toolbox.ui.main.SyncToolScreenOrientation(
      tool = com.example.toolbox.ui.main.Tool.IMAGE_EDITOR,
      restorePreviousOnDispose = true,
    )
    ImageEditorScreen(
      modifier = modifier,
      initialFile = activeImageEditorFile,
      onNavigateBack = {
        fileForImageEditor = null
        refreshTrigger++
      },
    )
    return
  }

  val activeVideoEditorFile = fileForVideoEditor
  if (activeVideoEditorFile != null) {
    com.example.toolbox.ui.main.SyncToolScreenOrientation(
      tool = com.example.toolbox.ui.main.Tool.VIDEO_EDITOR,
      restorePreviousOnDispose = true,
    )
    VideoEditorScreen(
      initialFile = activeVideoEditorFile,
      initialDirectory = activeVideoEditorFile.parentFile ?: initialDirectory,
      modifier = modifier,
      onNavigateBack = {
        fileForVideoEditor = null
        refreshTrigger++
      },
    )
    return
  }

  val activeFile = currentFile
  if (activeFile == null) {
    if (onNavigateBack != null) {
      BackHandler { onNavigateBack() }
    }
    Column(
      modifier = modifier
        .fillMaxSize()
        .toolboxScreenBackground(MaterialTheme.colorScheme.background)
        .testTag("image_screen"),
    ) {
      ToolboxTopBar(
        title = "Viewer",
        subtext = "View images, OCR text, QR codes & colors",
        icon = Icons.Default.CropOriginal,
        onNavigateBack = onNavigateBack,
        backButtonTestTag = "action_navigate_back",
        additionalBackButtonTestTag = "image_back_button",
      )

      OpenFileInitialView(
        onOpenFile = {
          try {
            systemFilePickerLauncher.launch("image/*")
          } catch (_: Throwable) {}
        },
        onUseSample = {
          val sampleFile = FileManagerEngine.getOrCreateSampleImageFile(context)
          currentFile = sampleFile
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        sampleButtonText = "or try sample image",
        sampleButtonIcon = Icons.Default.Image,
        sampleButtonTestTag = "open_sample_image_button",
        openButtonTestTag = "open_file_button",
        modifier = Modifier.weight(1f),
      )
    }
  } else {
    val resolvedInitialPhoto = remember(activeFile.absolutePath, refreshTrigger) {
      initialPhoto?.takeIf { it.path == activeFile.absolutePath }
        ?: fileToGalleryPhoto(activeFile)
    }

    val resolvedPhotos = remember(
      activeFile.absolutePath,
      initialPhotos,
      initialFiles,
      initialDirectory,
      refreshTrigger,
    ) {
      val baseList: List<GalleryPhoto> = when {
        !initialPhotos.isNullOrEmpty() -> initialPhotos.filter { it.file.exists() || it.path == activeFile.absolutePath }
        !initialFiles.isNullOrEmpty() -> initialFiles
          .filter { it.exists() || it.absolutePath == activeFile.absolutePath }
          .map { fileToGalleryPhoto(it) }
        else -> {
          val parentDir = activeFile.parentFile ?: initialDirectory
          val siblingFiles = parentDir
            ?.takeIf { it.exists() && it.isDirectory }
            ?.listFiles()
            ?.filter { it.isFile && !it.name.startsWith(".") && GalleryEngine.isPhotoFile(it) }
            ?.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            .orEmpty()
          siblingFiles.map { fileToGalleryPhoto(it) }
        }
      }
      if (baseList.any { it.path == resolvedInitialPhoto.path }) {
        baseList
      } else {
        listOf(resolvedInitialPhoto) + baseList
      }
    }

    val dismissViewer = {
      if (startingFile != null && onNavigateBack != null) {
        onNavigateBack()
      } else {
        currentFile = null
      }
    }

    BackHandler {
      dismissViewer()
    }

    Box(
      modifier = modifier
        .fillMaxSize()
        .background(Color.Black)
        .testTag("image_screen"),
    ) {
      ImageViewerContent(
        photos = resolvedPhotos,
        initialPhoto = resolvedInitialPhoto,
        photoIndexMap = effectivePhotoIndexMap,
        applySystemInsets = false,
        additionalCloseButtonTestTag = "action_navigate_back",
        onPhotoIndexed = { entry ->
          if (onPhotoIndexed != null) {
            onPhotoIndexed(entry)
          } else {
            val updatedMap = localPhotoIndexMap + (entry.path to entry)
            localPhotoIndexMap = updatedMap
            if (!isInspection) {
              scope.launch(Dispatchers.IO) {
                GalleryEngine.saveIndexCacheToDisk(indexCacheFile, updatedMap)
              }
            }
          }
        },
        onDismiss = dismissViewer,
        onEdit = { photoToEdit ->
          if (photoToEdit.isVideo) {
            fileForVideoEditor = photoToEdit.file
            onOpenVideoEditor?.invoke(photoToEdit.file)
          } else {
            fileForImageEditor = photoToEdit.file
            onOpenImageEditor?.invoke(photoToEdit.file)
          }
        },
        onShare = { photoToShare ->
          sharePhotoFile(context, photoToShare.file)
        },
        onDelete = { photoToDelete ->
          val currentIndex = resolvedPhotos.indexOfFirst { it.path == photoToDelete.path }
          val remainingPhotos = resolvedPhotos.filterNot { it.path == photoToDelete.path }
          val deletedCount = GalleryEngine.deleteSelectedItems(emptyList(), listOf(photoToDelete))
          if (deletedCount > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            refreshTrigger++
            Toast.makeText(
              context,
              "Deleted ${photoToDelete.name}",
              Toast.LENGTH_SHORT,
            ).show()
            if (remainingPhotos.isEmpty()) {
              dismissViewer()
            } else {
              val nextIndex = currentIndex.coerceIn(0, remainingPhotos.lastIndex)
              currentFile = remainingPhotos[nextIndex].file
            }
          }
        },
        onOpenColorPicker = { hex ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          if (onOpenColorPicker != null) {
            onOpenColorPicker(hex)
          } else {
            inlineColorPickerHex = hex
          }
        },
      )
    }
  }
}

private fun fileToGalleryPhoto(file: File): GalleryPhoto {
  return GalleryPhoto(
    file = file,
    name = file.name,
    path = file.absolutePath,
    sizeBytes = if (file.exists()) file.length() else 0L,
    lastModifiedMs = if (file.exists()) file.lastModified() else 0L,
    extension = file.extension.lowercase(Locale.ROOT),
  )
}

private fun copyUriToCacheImageFile(context: Context, uri: Uri): File? {
  return try {
    val fileName = "opened_image_${System.currentTimeMillis()}.png"
    val outDir = File(context.cacheDir, "image_viewer_opened").apply { mkdirs() }
    val outFile = File(outDir, fileName)
    context.contentResolver.openInputStream(uri)?.use { input ->
      FileOutputStream(outFile).use { output ->
        input.copyTo(output)
      }
    }
    outFile.takeIf { it.exists() && it.length() > 0L }
  } catch (_: Throwable) {
    null
  }
}

@Composable
internal fun GalleryPhotoViewerDialog(
  photos: List<GalleryPhoto>,
  initialPhoto: GalleryPhoto,
  photoIndexMap: Map<String, GalleryPhotoIndexEntry> = emptyMap(),
  onPhotoIndexed: (GalleryPhotoIndexEntry) -> Unit = {},
  onDismiss: () -> Unit,
  onEdit: (GalleryPhoto) -> Unit = {},
  onShare: (GalleryPhoto) -> Unit,
  onDelete: (GalleryPhoto) -> Unit = {},
  onOpenColorPicker: (String) -> Unit = {},
) {
  val activityView = LocalView.current
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current
  val activitySystemBars = WindowInsets.systemBars.asPaddingValues()
  val activityCutout = WindowInsets.displayCutout.asPaddingValues()
  val rootWindowInsets = ViewCompat.getRootWindowInsets(activityView)
    ?.getInsetsIgnoringVisibility(
      WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
    )
  val rootTopDp = with(density) { (rootWindowInsets?.top ?: 0).toDp() }
  val rootBottomDp = with(density) { (rootWindowInsets?.bottom ?: 0).toDp() }
  val rootLeftDp = with(density) { (rootWindowInsets?.left ?: 0).toDp() }
  val rootRightDp = with(density) { (rootWindowInsets?.right ?: 0).toDp() }
  val rootStartDp = if (layoutDirection == LayoutDirection.Ltr) rootLeftDp else rootRightDp
  val rootEndDp = if (layoutDirection == LayoutDirection.Ltr) rootRightDp else rootLeftDp

  val activityTopInset = maxOf(
    rootTopDp,
    activitySystemBars.calculateTopPadding(),
    activityCutout.calculateTopPadding(),
  )
  val activityBottomInset = maxOf(
    rootBottomDp,
    activitySystemBars.calculateBottomPadding(),
    activityCutout.calculateBottomPadding(),
  )
  val activityStartInset = maxOf(
    rootStartDp,
    activitySystemBars.calculateStartPadding(layoutDirection),
    activityCutout.calculateStartPadding(layoutDirection),
  )
  val activityEndInset = maxOf(
    rootEndDp,
    activitySystemBars.calculateEndPadding(layoutDirection),
    activityCutout.calculateEndPadding(layoutDirection),
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false,
    ),
  ) {
    val dialogView = LocalView.current
    SideEffect {
      val dialogWindow = (dialogView.parent as? DialogWindowProvider)?.window
      if (dialogWindow != null) {
        WindowCompat.getInsetsController(dialogWindow, dialogView).apply {
          isAppearanceLightStatusBars = false
          isAppearanceLightNavigationBars = false
        }
      }
    }

    val dialogSystemBars = WindowInsets.systemBars.asPaddingValues()
    val dialogCutout = WindowInsets.displayCutout.asPaddingValues()
    val topSystemInset = maxOf(
      activityTopInset,
      dialogSystemBars.calculateTopPadding(),
      dialogCutout.calculateTopPadding(),
    )
    val bottomSystemInset = maxOf(
      activityBottomInset,
      dialogSystemBars.calculateBottomPadding(),
      dialogCutout.calculateBottomPadding(),
    )
    val startSystemInset = maxOf(
      activityStartInset,
      dialogSystemBars.calculateStartPadding(layoutDirection),
      dialogCutout.calculateStartPadding(layoutDirection),
    )
    val endSystemInset = maxOf(
      activityEndInset,
      dialogSystemBars.calculateEndPadding(layoutDirection),
      dialogCutout.calculateEndPadding(layoutDirection),
    )

    ImageViewerContent(
      photos = photos,
      initialPhoto = initialPhoto,
      photoIndexMap = photoIndexMap,
      applySystemInsets = true,
      topSystemInset = topSystemInset,
      bottomSystemInset = bottomSystemInset,
      startSystemInset = startSystemInset,
      endSystemInset = endSystemInset,
      onPhotoIndexed = onPhotoIndexed,
      onDismiss = onDismiss,
      onEdit = onEdit,
      onShare = onShare,
      onDelete = onDelete,
      onOpenColorPicker = onOpenColorPicker,
    )
  }
}

@Composable
internal fun ImageViewerContent(
  photos: List<GalleryPhoto>,
  initialPhoto: GalleryPhoto,
  photoIndexMap: Map<String, GalleryPhotoIndexEntry> = emptyMap(),
  applySystemInsets: Boolean = false,
  topSystemInset: Dp = 0.dp,
  bottomSystemInset: Dp = 0.dp,
  startSystemInset: Dp = 0.dp,
  endSystemInset: Dp = 0.dp,
  additionalCloseButtonTestTag: String? = null,
  onPhotoIndexed: (GalleryPhotoIndexEntry) -> Unit = {},
  onDismiss: () -> Unit,
  onEdit: (GalleryPhoto) -> Unit = {},
  onShare: (GalleryPhoto) -> Unit,
  onDelete: (GalleryPhoto) -> Unit = {},
  onOpenColorPicker: (String) -> Unit = {},
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val haptic = LocalHapticFeedback.current

  val effTopInset = if (applySystemInsets) topSystemInset else 0.dp
  val effBottomInset = if (applySystemInsets) bottomSystemInset else 0.dp
  val effStartInset = if (applySystemInsets) startSystemInset else 0.dp
  val effEndInset = if (applySystemInsets) endSystemInset else 0.dp

  val viewerPhotos = remember(photos, initialPhoto) {
    if (photos.isNotEmpty() && photos.any { it.path == initialPhoto.path }) {
      photos
    } else if (photos.isNotEmpty()) {
      listOf(initialPhoto) + photos
    } else {
      listOf(initialPhoto)
    }
  }
  val initialPage = remember(viewerPhotos, initialPhoto.path) {
    viewerPhotos.indexOfFirst { it.path == initialPhoto.path }.coerceAtLeast(0)
  }
  val pagerState = rememberPagerState(
    initialPage = initialPage,
    pageCount = { viewerPhotos.size },
  )
  LaunchedEffect(initialPhoto.path, initialPage) {
    if (pagerState.currentPage != initialPage && initialPage in viewerPhotos.indices) {
      pagerState.scrollToPage(initialPage)
    }
  }
  val coroutineScope = rememberCoroutineScope()
  val previewListState = rememberLazyListState()

  // Controls whether the bottom preview scroll bar is visible (dismissed/restored with a tap on the image)
  var showPreviewBar by rememberSaveable { mutableStateOf(true) }
  var showRecognitionInfoSheet by rememberSaveable { mutableStateOf(false) }
  var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }
  var isColorPickerActive by rememberSaveable { mutableStateOf(false) }
  var selectedOverlayTag by rememberSaveable { mutableStateOf<String?>(null) }
  var topBarHeightPx by remember { mutableIntStateOf(0) }
  var bottomBarHeightPx by remember { mutableIntStateOf(0) }
  var previewBarWidthPx by remember { mutableIntStateOf(0) }

  // Videos play automatically and muted by default in the viewer
  var isVideoPlaying by rememberSaveable(initialPhoto.path) { mutableStateOf(true) }
  var isVideoMuted by rememberSaveable(initialPhoto.path) { mutableStateOf(true) }

  // Current active page's zoom scale (0.5f..5f)
  var currentZoomScale by remember { mutableFloatStateOf(DEFAULT_PHOTO_ZOOM) }

  val currentPhoto = viewerPhotos.getOrElse(pagerState.currentPage) { initialPhoto }
  val currentIndexEntry = photoIndexMap[currentPhoto.path]

  val videoDurationsByPath = remember { mutableStateMapOf<String, Long>() }
  var videoDurationMs by remember { mutableLongStateOf(0L) }
  var videoPositionMs by remember { mutableLongStateOf(0L) }
  var isDraggingVideoSeek by remember { mutableStateOf(false) }
  var videoSeekDragFraction by remember { mutableFloatStateOf(0f) }
  var videoSeekTargetMs by remember { mutableLongStateOf(0L) }
  var videoSeekRequestToken by remember { mutableLongStateOf(0L) }

  // Reset zoom, active tag box overlay, and auto-play muted when swiping to a different photo/video
  LaunchedEffect(pagerState.currentPage, currentPhoto.path, currentPhoto.lastModifiedMs) {
    currentZoomScale = DEFAULT_PHOTO_ZOOM
    selectedOverlayTag = null
    isVideoPlaying = true
    isVideoMuted = true
    videoPositionMs = 0L
    isDraggingVideoSeek = false
    videoSeekDragFraction = 0f
    videoSeekTargetMs = 0L
    val cachedDuration = videoDurationsByPath[currentPhoto.path]?.takeIf { it > 0L } ?: 0L
    videoDurationMs = cachedDuration
    if (currentPhoto.isVideo) {
      isColorPickerActive = false
      if (cachedDuration <= 0L) {
        val extracted = withContext(Dispatchers.IO) {
          GalleryEngine.extractVideoDurationMs(currentPhoto.file)
        }
        if (extracted > 0L) {
          videoDurationsByPath[currentPhoto.path] = extracted
          if (videoDurationMs <= 0L) {
            videoDurationMs = extracted
          }
        }
      }
    }
  }

  // Center the active photo's thumbnail in the bottom preview scroll bar
  LaunchedEffect(pagerState.currentPage, previewBarWidthPx) {
    if (viewerPhotos.isNotEmpty() && pagerState.currentPage in viewerPhotos.indices && previewBarWidthPx > 0) {
      previewListState.animateScrollToItem(
        index = pagerState.currentPage,
        scrollOffset = 0,
      )
    }
  }

  val tagBoxStrokeColors = listOf(
    MaterialTheme.colorScheme.tertiary,
    MaterialTheme.colorScheme.primary,
    MaterialTheme.colorScheme.secondary,
    MaterialTheme.colorScheme.inversePrimary,
    MaterialTheme.colorScheme.error,
  )
  val tagBadgeContainerColors = listOf(
    MaterialTheme.colorScheme.tertiaryContainer,
    MaterialTheme.colorScheme.primaryContainer,
    MaterialTheme.colorScheme.secondaryContainer,
    MaterialTheme.colorScheme.surfaceContainerHigh,
    MaterialTheme.colorScheme.errorContainer,
  )
  val tagBadgeContentColors = listOf(
    MaterialTheme.colorScheme.onTertiaryContainer,
    MaterialTheme.colorScheme.onPrimaryContainer,
    MaterialTheme.colorScheme.onSecondaryContainer,
    MaterialTheme.colorScheme.onSurface,
    MaterialTheme.colorScheme.onErrorContainer,
  )

  val selectedOverlayTagIndex = remember(selectedOverlayTag, currentIndexEntry) {
    val active = selectedOverlayTag
    if (active == null) {
      0
    } else {
      currentIndexEntry?.autoTags
        ?.indexOfFirst { it.equals(active, ignoreCase = true) }
        ?.takeIf { it >= 0 } ?: 0
    }
  }

  // Ensure the active photo is indexed immediately if it hasn't been indexed yet
  LaunchedEffect(currentPhoto.path, currentPhoto.lastModifiedMs) {
    if (currentIndexEntry == null) {
      val entry = GalleryEngine.indexSinglePhoto(currentPhoto.file)
      onPhotoIndexed(entry)
    }
  }

  // Full black background with the image/video rendered directly on top and edge-to-edge translucent top/bottom panels
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag("gallery_photo_viewer_dialog")
      .semantics {
        contentDescription = "Full black background photo viewer"
      },
  ) {
    // Swipeable horizontal pager over all photos/videos in the album
    HorizontalPager(
      state = pagerState,
      userScrollEnabled = currentZoomScale <= 1.02f && !isColorPickerActive,
      modifier = Modifier
        .fillMaxSize()
        .testTag("photo_viewer_pager"),
    ) { pageIndex ->
      val pagePhoto = viewerPhotos[pageIndex]
      val isCurrentPage = pageIndex == pagerState.currentPage
      PhotoViewerPageContent(
        photo = pagePhoto,
        isCurrentPage = isCurrentPage,
        isVideoPlaying = isCurrentPage && isVideoPlaying,
        isVideoMuted = isVideoMuted,
        isControlsVisible = showPreviewBar,
        isDraggingVideoSeek = isCurrentPage && isDraggingVideoSeek,
        videoSeekTargetMs = if (isCurrentPage) videoSeekTargetMs else 0L,
        videoSeekRequestToken = if (isCurrentPage) videoSeekRequestToken else 0L,
        onVideoDurationChange = { duration ->
          if (duration > 0L) {
            videoDurationsByPath[pagePhoto.path] = duration
            if (isCurrentPage) {
              videoDurationMs = duration
            }
          }
        },
        onVideoPositionChange = { position ->
          if (isCurrentPage && !isDraggingVideoSeek) {
            val nonNegativePos = position.coerceAtLeast(0L)
            if (nonNegativePos > videoDurationMs && nonNegativePos > 0L) {
              videoDurationMs = nonNegativePos
              videoDurationsByPath[pagePhoto.path] = nonNegativePos
            }
            val activeDuration = videoDurationMs
            videoPositionMs = if (activeDuration > 0L) {
              nonNegativePos.coerceIn(0L, activeDuration)
            } else {
              nonNegativePos
            }
          }
        },
        isColorPickerActive = isCurrentPage && isColorPickerActive && !pagePhoto.isVideo,
        activeOverlayTag = if (isCurrentPage) selectedOverlayTag else null,
        activeOverlayTagColorIndex = selectedOverlayTagIndex,
        indexEntry = if (isCurrentPage) currentIndexEntry else photoIndexMap[pagePhoto.path],
        safeTopInsetPx = if (showPreviewBar) topBarHeightPx else 0,
        safeBottomInsetPx = if (showPreviewBar) bottomBarHeightPx else 0,
        zoomScale = if (isCurrentPage) currentZoomScale else DEFAULT_PHOTO_ZOOM,
        onZoomScaleChange = { updatedZoom ->
          if (isCurrentPage) {
            currentZoomScale = updatedZoom
          }
        },
        onTapImage = {
          showPreviewBar = !showPreviewBar
        },
        onOpenColorPicker = onOpenColorPicker,
      )
    }

    // Top overlay bar with Close, current photo title & index, zoom indicator, Info/OCR, Edit, and Share
    // Extends all the way to the top edge of the screen and dismisses/restores together with the bottom bar on tap
    AnimatedVisibility(
      visible = showPreviewBar,
      enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
      exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
      modifier = Modifier.align(Alignment.TopCenter),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .onSizeChanged { topBarHeightPx = it.height }
          .background(Color.Black.copy(alpha = 0.65f))
          .testTag("photo_viewer_top_bar")
          .padding(
            start = effStartInset + 12.dp,
            top = effTopInset + 10.dp,
            end = effEndInset + 12.dp,
            bottom = 10.dp,
          ),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = if (additionalCloseButtonTestTag != null) {
            Modifier.testTag(additionalCloseButtonTestTag)
          } else {
            Modifier
          },
        ) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("photo_viewer_close_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.White,
            )
          }
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp),
        ) {
          Text(
            text = currentPhoto.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("photo_viewer_current_title"),
          )
          Text(
            text = "${pagerState.currentPage + 1} / ${viewerPhotos.size} • ${currentPhoto.sizeFormatted}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.78f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("photo_viewer_page_indicator"),
          )
        }

        // Zoom scale badge (tap to reset zoom to 100%)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color.White.copy(alpha = 0.16f),
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { currentZoomScale = DEFAULT_PHOTO_ZOOM }
            .testTag("photo_viewer_zoom_badge"),
        ) {
          Text(
            text = "${(currentZoomScale * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("photo_viewer_zoom_label"),
          )
        }
      }
    }

    // Bottom preview scroll bar + Video playback controls + Auto-Tag & OCR/QR quick bar, dismissible with a tap
    AnimatedVisibility(
      visible = showPreviewBar,
      enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
      exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
      modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .onSizeChanged { bottomBarHeightPx = it.height },
      ) {
        if (currentPhoto.isAnimatedImage && !currentPhoto.isVideo) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                start = effStartInset + 16.dp,
                end = effEndInset + 16.dp,
                top = 8.dp,
                bottom = 8.dp,
              )
              .testTag("photo_viewer_video_controls"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = CircleShape,
              color = Color.Black.copy(alpha = 0.72f),
              border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
              modifier = Modifier
                .clip(CircleShape)
                .clickable { isVideoPlaying = !isVideoPlaying }
                .testTag("photo_viewer_play_pause_button"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = if (isVideoPlaying) "Pause" else "Play",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = if (isVideoPlaying) "Playing" else "Paused",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White,
                  modifier = Modifier.testTag("photo_viewer_video_play_state_label"),
                )
              }
            }
          }
        }

        Surface(
          color = Color.Black.copy(alpha = 0.76f),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("photo_viewer_preview_bar"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
          ) {
            if (currentPhoto.isVideo) {
              PhotoViewerVideoControlsRow(
                isVideoPlaying = isVideoPlaying,
                isVideoMuted = isVideoMuted,
                isDraggingVideoSeek = isDraggingVideoSeek,
                videoSeekDragFraction = videoSeekDragFraction,
                videoPositionMsProvider = { videoPositionMs },
                videoDurationMsProvider = {
                  videoDurationMs.takeIf { it > 0L }
                    ?: (videoDurationsByPath[currentPhoto.path]?.takeIf { it > 0L } ?: 0L)
                },
                startInset = effStartInset,
                endInset = effEndInset,
                onTogglePlayPause = { isVideoPlaying = !isVideoPlaying },
                onToggleMute = { isVideoMuted = !isVideoMuted },
                onSeekFractionChange = { newFraction ->
                  val clamped = newFraction.coerceIn(0f, 1f)
                  isDraggingVideoSeek = true
                  videoSeekDragFraction = clamped
                  val effectiveDurationMs = videoDurationMs.takeIf { it > 0L }
                    ?: (videoDurationsByPath[currentPhoto.path]?.takeIf { it > 0L } ?: 5_000L)
                  val targetMs = (clamped * effectiveDurationMs).roundToLong()
                  videoPositionMs = targetMs
                  videoSeekTargetMs = targetMs
                  videoSeekRequestToken++
                },
                onSeekFinished = {
                  val clamped = videoSeekDragFraction.coerceIn(0f, 1f)
                  val effectiveDurationMs = videoDurationMs.takeIf { it > 0L }
                    ?: (videoDurationsByPath[currentPhoto.path]?.takeIf { it > 0L } ?: 5_000L)
                  val targetMs = (clamped * effectiveDurationMs).roundToLong()
                  videoPositionMs = targetMs
                  videoSeekTargetMs = targetMs
                  isDraggingVideoSeek = false
                  videoSeekRequestToken++
                },
              )
            }
            if (currentIndexEntry != null &&
              (currentIndexEntry.autoTags.isNotEmpty() || currentIndexEntry.hasOcrText || currentIndexEntry.hasBarcodes)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState())
                  .padding(
                    start = effStartInset + 16.dp,
                    end = effEndInset + 16.dp,
                    top = 10.dp,
                    bottom = 4.dp,
                  )
                  .testTag("photo_viewer_recognition_bar"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                if (currentIndexEntry.hasOcrText) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                      .clip(CircleShape)
                      .clickable { showRecognitionInfoSheet = true }
                      .testTag("photo_viewer_ocr_pill"),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp),
                      )
                      Text(
                        text = "OCR Text",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                      )
                    }
                  }
                }

                if (currentIndexEntry.hasBarcodes) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                      .clip(CircleShape)
                      .clickable { showRecognitionInfoSheet = true }
                      .testTag("photo_viewer_barcode_pill"),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(14.dp),
                      )
                      Text(
                        text = currentIndexEntry.barcodePayloads.first().take(28),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }
                  }
                }

                currentIndexEntry.autoTags.forEachIndexed { tagIdx, tag ->
                  val isTagSelected = selectedOverlayTag?.equals(tag, ignoreCase = true) == true
                  val accentBorder = tagBoxStrokeColors[tagIdx % tagBoxStrokeColors.size]
                  val accentContainer = tagBadgeContainerColors[tagIdx % tagBadgeContainerColors.size]
                  val accentContent = tagBadgeContentColors[tagIdx % tagBadgeContentColors.size]
                  Surface(
                    shape = CircleShape,
                    color = if (isTagSelected) {
                      accentContainer.copy(alpha = 0.95f)
                    } else {
                      Color.White.copy(alpha = 0.16f)
                    },
                    border = BorderStroke(
                      width = if (isTagSelected) 1.5.dp else 1.dp,
                      color = if (isTagSelected) accentBorder else Color.White.copy(alpha = 0.28f),
                    ),
                    modifier = Modifier
                      .clip(CircleShape)
                      .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedOverlayTag = if (isTagSelected) null else tag
                      }
                      .testTag("photo_viewer_tag_$tag"),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                      if (isTagSelected) {
                        Box(
                          modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentBorder),
                        )
                      }
                      Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTagSelected) accentContent else Color.White,
                      )
                    }
                  }
                }
              }
            }

            // Action icon buttons row between smart tags and preview list, blending in with the translucent panel
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(
                  start = effStartInset + 16.dp,
                  end = effEndInset + 16.dp,
                  top = 4.dp,
                )
                .testTag("photo_viewer_actions_row"),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              IconButton(
                onClick = { showRecognitionInfoSheet = true },
                modifier = Modifier.testTag("photo_viewer_info_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = "Photo info, OCR text & smart tags",
                  tint = Color.White,
                )
              }

              IconButton(
                onClick = { onEdit(currentPhoto) },
                modifier = Modifier.testTag("photo_viewer_edit_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit",
                  tint = Color.White,
                )
              }

              IconButton(
                onClick = { onShare(currentPhoto) },
                modifier = Modifier.testTag("photo_viewer_share_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = "Share",
                  tint = Color.White,
                )
              }

              if (!currentPhoto.isVideo) {
                IconButton(
                  onClick = {
                    isColorPickerActive = !isColorPickerActive
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  },
                  modifier = Modifier.testTag("photo_viewer_color_picker_button"),
                ) {
                  Icon(
                    imageVector = Icons.Default.Colorize,
                    contentDescription = if (isColorPickerActive) "Close color picker" else "Color picker",
                    tint = if (isColorPickerActive) MaterialTheme.colorScheme.tertiary else Color.White,
                  )
                }
              }

              IconButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.testTag("photo_viewer_delete_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete",
                  tint = MaterialTheme.colorScheme.error,
                )
              }
            }

            val selectedThumbHalfDp = 32.dp
            val horizontalCenterPaddingDp = with(density) {
              if (previewBarWidthPx > 0) {
                (previewBarWidthPx.toDp() / 2f - selectedThumbHalfDp).coerceAtLeast(16.dp)
              } else {
                16.dp
              }
            }

            LazyRow(
              state = previewListState,
              contentPadding = PaddingValues(
                start = maxOf(effStartInset + 16.dp, horizontalCenterPaddingDp),
                end = maxOf(effEndInset + 16.dp, horizontalCenterPaddingDp),
                top = 6.dp,
                bottom = effBottomInset + 12.dp,
              ),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { previewBarWidthPx = it.width }
                .heightIn(min = 64.dp + 6.dp + effBottomInset + 12.dp)
                .testTag("photo_viewer_preview_scroll_bar"),
            ) {
              itemsIndexed(
                items = viewerPhotos,
                key = { _, item -> "preview_${item.path}" },
              ) { index, itemPhoto ->
                val isSelected = index == pagerState.currentPage
                Box(
                  modifier = Modifier.height(64.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Box(
                    modifier = Modifier
                      .size(if (isSelected) 64.dp else 54.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp),
                      )
                      .alpha(if (isSelected) 1f else 0.65f)
                      .clickable {
                        coroutineScope.launch {
                          pagerState.animateScrollToPage(index)
                        }
                      }
                      .testTag("photo_viewer_preview_thumb_${itemPhoto.name}"),
                    contentAlignment = Alignment.Center,
                  ) {
                    PhotoThumbnailBox(
                      photoFile = itemPhoto.file,
                      contentDescription = "Preview ${itemPhoto.name}",
                      iconSize = 22.dp,
                      lastModifiedMs = itemPhoto.lastModifiedMs,
                      modifier = Modifier.fillMaxSize(),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    if (showDeleteConfirmDialog) {
      GalleryViewerDeleteConfirmDialog(
        photo = currentPhoto,
        onDismiss = { showDeleteConfirmDialog = false },
        onConfirm = {
          showDeleteConfirmDialog = false
          onDelete(currentPhoto)
        },
      )
    }

    if (showRecognitionInfoSheet) {
      GalleryPhotoRecognitionInfoDialog(
        photo = currentPhoto,
        indexEntry = currentIndexEntry,
        onDismiss = { showRecognitionInfoSheet = false },
        onCopyText = { label, text ->
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
          clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
          Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
        },
        onSaveOcrToNotes = { ocrText ->
          val doc = CameraEngine.analyzeScannedDocumentText(
            rawText = ocrText,
            fallbackTitle = currentPhoto.name.substringBeforeLast('.').ifBlank { "Gallery Scan" },
          )
          val notesRepo = SharedPreferencesNotesRepository(context)
          CameraEngine.saveScannedDocumentToNotes(
            repository = notesRepo,
            document = doc,
            labels = (listOf("Gallery", "Scanned") + currentIndexEntry?.autoTags.orEmpty()).distinct(),
          )
          Toast.makeText(context, "Saved recognized text to Notes", Toast.LENGTH_SHORT).show()
        },
        onOpenUrl = { url ->
          try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
          } catch (_: Exception) {
            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
          }
        },
      )
    }
  }
}

@Composable
internal fun GalleryViewerDeleteConfirmDialog(
  photo: GalleryPhoto,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("photo_viewer_delete_confirm_dialog"),
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
            text = "Delete ${if (photo.isVideo) "video" else "photo"}?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Size: ${photo.sizeFormatted}",
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
        Text(
          text = "Are you sure you want to permanently delete this ${if (photo.isVideo) "video" else "photo"}? This action cannot be undone.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "• ${photo.name}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("photo_viewer_confirm_delete_button"),
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
        modifier = Modifier.testTag("photo_viewer_cancel_delete_button"),
      ) {
        Text("Cancel")
      }
    },
  )
}

@Composable
internal fun GalleryPhotoRecognitionInfoDialog(
  photo: GalleryPhoto,
  indexEntry: GalleryPhotoIndexEntry?,
  onDismiss: () -> Unit,
  onCopyText: (label: String, text: String) -> Unit,
  onSaveOcrToNotes: (String) -> Unit,
  onOpenUrl: (String) -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("photo_viewer_info_dialog"),
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
            imageVector = if (indexEntry?.hasOcrText == true) {
              Icons.Default.DocumentScanner
            } else {
              Icons.Default.AutoAwesome
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = if (indexEntry?.hasOcrText == true && indexEntry.hasBarcodes != true) {
              "Recognized Text (OCR)"
            } else {
              "Photo Info & Recognition"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${photo.name} • ${photo.sizeFormatted}",
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
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // 1. Recognized Text (OCR) Card
        if (indexEntry?.hasOcrText == true) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("photo_info_ocr_card"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.DocumentScanner,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "Recognized Text (OCR)",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
              }

              Text(
                text = indexEntry.ocrText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("photo_info_ocr_text"),
              )

              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                OutlinedButton(
                  onClick = { onCopyText("OCR text", indexEntry.ocrText) },
                  shape = CircleShape,
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("photo_info_copy_ocr_button"),
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Copy Text", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                  onClick = { onSaveOcrToNotes(indexEntry.ocrText) },
                  shape = CircleShape,
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("photo_info_save_to_notes_button"),
                ) {
                  Icon(Icons.AutoMirrored.Filled.StickyNote2, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Save to Notes", style = MaterialTheme.typography.labelMedium)
                }
              }
            }
          }
        }

        // 3. Decoded QR / Barcode Card
        if (indexEntry?.hasBarcodes == true) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("photo_info_barcode_card"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.QrCode2,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "QR & Barcodes",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
              }

              indexEntry.barcodePayloads.forEachIndexed { idx, payload ->
                val summary = indexEntry.barcodeSummaries.getOrNull(idx)
                if (!summary.isNullOrBlank()) {
                  Text(
                    text = summary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Text(
                  text = payload,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.testTag("photo_info_barcode_payload"),
                )
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  OutlinedButton(
                    onClick = { onCopyText("Barcode", payload) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("photo_info_copy_barcode_button"),
                  ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Code", style = MaterialTheme.typography.labelMedium)
                  }
                  if (payload.startsWith("http://", ignoreCase = true) ||
                    payload.startsWith("https://", ignoreCase = true)
                  ) {
                    Button(
                      onClick = { onOpenUrl(payload) },
                      shape = CircleShape,
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                      modifier = Modifier.testTag("photo_info_open_url_button"),
                    ) {
                      Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Open Link", style = MaterialTheme.typography.labelMedium)
                    }
                  }
                }
              }
            }
          }
        }

        // File Details Card (shown when opened via the info button on photos without OCR or barcodes)
        if (indexEntry?.hasOcrText != true && indexEntry?.hasBarcodes != true) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("photo_info_file_details_card"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = "File Details",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = "Name: ${photo.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Size: ${photo.sizeFormatted} • ${photo.timestampFormatted}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = photo.path,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier.testTag("photo_info_close_button"),
      ) {
        Text("Done")
      }
    },
  )
}

@Composable
private fun PhotoViewerVideoControlsRow(
  isVideoPlaying: Boolean,
  isVideoMuted: Boolean,
  isDraggingVideoSeek: Boolean,
  videoSeekDragFraction: Float,
  videoPositionMsProvider: () -> Long,
  videoDurationMsProvider: () -> Long,
  startInset: Dp,
  endInset: Dp,
  onTogglePlayPause: () -> Unit,
  onToggleMute: () -> Unit,
  onSeekFractionChange: (Float) -> Unit,
  onSeekFinished: () -> Unit,
) {
  val durationMs = videoDurationMsProvider()
  val positionMs = videoPositionMsProvider()
  val videoSeekProgress = if (isDraggingVideoSeek) {
    videoSeekDragFraction
  } else if (durationMs > 0L) {
    (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
  } else {
    0f
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(
        start = startInset + 12.dp,
        end = endInset + 12.dp,
        top = 6.dp,
        bottom = 2.dp,
      )
      .testTag("photo_viewer_video_controls"),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(
      onClick = onTogglePlayPause,
      modifier = Modifier.testTag("photo_viewer_play_pause_button"),
    ) {
      Icon(
        imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
        contentDescription = if (isVideoPlaying) "Pause" else "Play",
        tint = Color.White,
      )
    }

    SlimDotSlider(
      value = videoSeekProgress,
      onValueChange = onSeekFractionChange,
      onValueChangeFinished = onSeekFinished,
      valueRange = 0f..1f,
      modifier = Modifier
        .weight(1f)
        .testTag("photo_viewer_video_seek_bar")
        .semantics {
          contentDescription = "Video seek bar"
        },
    )

    IconButton(
      onClick = onToggleMute,
      modifier = Modifier.testTag("photo_viewer_mute_button"),
    ) {
      Icon(
        imageVector = if (isVideoMuted) {
          Icons.AutoMirrored.Filled.VolumeOff
        } else {
          Icons.AutoMirrored.Filled.VolumeUp
        },
        contentDescription = if (isVideoMuted) "Unmute video" else "Mute video",
        tint = Color.White,
      )
    }
  }
}

@Composable
internal fun PhotoViewerPageContent(
  photo: GalleryPhoto,
  isCurrentPage: Boolean,
  isVideoPlaying: Boolean = isCurrentPage,
  isVideoMuted: Boolean = true,
  isControlsVisible: Boolean = true,
  isDraggingVideoSeek: Boolean = false,
  videoSeekTargetMs: Long = 0L,
  videoSeekRequestToken: Long = 0L,
  onVideoDurationChange: (Long) -> Unit = {},
  onVideoPositionChange: (Long) -> Unit = {},
  isColorPickerActive: Boolean = false,
  activeOverlayTag: String? = null,
  activeOverlayTagColorIndex: Int = 0,
  indexEntry: GalleryPhotoIndexEntry? = null,
  safeTopInsetPx: Int = 0,
  safeBottomInsetPx: Int = 0,
  zoomScale: Float,
  onZoomScaleChange: (Float) -> Unit,
  onTapImage: () -> Unit,
  onOpenColorPicker: (String) -> Unit = {},
) {
  val isInspectionMode = LocalInspectionMode.current
  val density = LocalDensity.current
  val haptic = LocalHapticFeedback.current
  val isVideo = photo.isVideo
  val isAnimatedImage = remember(photo.path, photo.lastModifiedMs) {
    !isVideo && photo.isAnimatedImage
  }

  var bitmap by remember(photo.path, photo.lastModifiedMs) {
    mutableStateOf(GalleryEngine.getCachedThumbnailBitmap(photo.file))
  }

  LaunchedEffect(photo.path, photo.lastModifiedMs, isVideo) {
    if (isVideo) {
      if (bitmap == null) {
        val videoThumb = GalleryEngine.loadThumbnailBitmapAsync(photo.file)
        if (videoThumb != null) {
          bitmap = videoThumb
        }
      }
    } else {
      val fullBitmap = withContext(Dispatchers.IO) {
        GalleryEngine.decodeOrientedBitmapFromFile(photo.file)
      }
      if (fullBitmap != null) {
        bitmap = fullBitmap
      }
    }
  }

  var panOffsetX by remember(photo.path) { mutableFloatStateOf(0f) }
  var panOffsetY by remember(photo.path) { mutableFloatStateOf(0f) }
  var containerSizePx by remember { mutableStateOf(IntSize.Zero) }
  var colorFocusPointNormalized by remember(photo.path) { mutableStateOf(Offset(0.5f, 0.5f)) }

  LaunchedEffect(zoomScale, isCurrentPage) {
    if (zoomScale <= 1.02f || !isCurrentPage) {
      panOffsetX = 0f
      panOffsetY = 0f
    }
  }

  val currentZoomState by rememberUpdatedState(zoomScale)
  val currentOnZoomChange by rememberUpdatedState(onZoomScaleChange)
  val currentOnTapImage by rememberUpdatedState(onTapImage)
  val currentIsCurrentPage by rememberUpdatedState(isCurrentPage)
  val currentIsVideoPlaying by rememberUpdatedState(isVideoPlaying)
  val currentIsVideoMuted by rememberUpdatedState(isVideoMuted)
  val currentIsDraggingVideoSeek by rememberUpdatedState(isDraggingVideoSeek)
  val currentIsColorPickerActive by rememberUpdatedState(isColorPickerActive)
  val currentOnVideoDurationChange by rememberUpdatedState(onVideoDurationChange)
  val currentOnVideoPositionChange by rememberUpdatedState(onVideoPositionChange)

  val currentBitmap = bitmap
  val currentImageBitmap = remember(currentBitmap) {
    currentBitmap?.asImageBitmap()
  }

  val sampledColorHex = remember(
    photo.path,
    photo.lastModifiedMs,
    currentBitmap,
    colorFocusPointNormalized,
    zoomScale,
    panOffsetX,
    panOffsetY,
    containerSizePx,
    isColorPickerActive,
  ) {
    if (!isColorPickerActive || isVideo) {
      null
    } else {
      val cw = containerSizePx.width.toFloat().coerceAtLeast(1f)
      val ch = containerSizePx.height.toFloat().coerceAtLeast(1f)
      val viewportX = colorFocusPointNormalized.x * cw
      val viewportY = colorFocusPointNormalized.y * ch
      val (imgNormX, imgNormY) = GalleryEngine.mapViewportPointToImageNormalized(
        viewportX = viewportX,
        viewportY = viewportY,
        containerWidth = cw,
        containerHeight = ch,
        imageWidth = currentBitmap?.width ?: 0,
        imageHeight = currentBitmap?.height ?: 0,
        zoomScale = zoomScale,
        panOffsetX = panOffsetX,
        panOffsetY = panOffsetY,
      )
      GalleryEngine.samplePhotoColorHex(
        file = photo.file,
        bitmap = currentBitmap,
        normX = imgNormX,
        normY = imgNormY,
      )
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
      .onSizeChanged { containerSizePx = it },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .then(
          if (isCurrentPage) {
            Modifier.testTag("photo_viewer_active_page")
          } else {
            Modifier
          }
        )
        .testTag("photo_viewer_page_${photo.name}")
        .pointerInput(photo.path) {
          awaitEachGesture {
            var downPosition: Offset? = null
            var maxPointers = 0
            var movedBeyondSlop = false
            var liveZoom = currentZoomState
            var handledByColorPicker = false

            do {
              val event = awaitPointerEvent(PointerEventPass.Initial)
              val pressedChanges = event.changes.filter { it.pressed }
              val pressedCount = pressedChanges.size
              if (pressedCount > maxPointers) {
                maxPointers = pressedCount
              }

              if (pressedCount >= 2) {
                movedBeyondSlop = true
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                if (zoomChange != 1f && zoomChange > 0f && !zoomChange.isNaN()) {
                  liveZoom = GalleryEngine.calculatePhotoZoom(liveZoom, zoomChange)
                  currentOnZoomChange(liveZoom)
                }
                if (liveZoom > 1.02f) {
                  panOffsetX += panChange.x
                  panOffsetY += panChange.y
                } else {
                  panOffsetX = 0f
                  panOffsetY = 0f
                }
                event.changes.forEach { it.consume() }
              } else if (pressedCount == 1) {
                val change = pressedChanges.first()
                if (downPosition == null) {
                  downPosition = change.position
                } else if ((change.position - downPosition).getDistance() > viewConfiguration.touchSlop) {
                  movedBeyondSlop = true
                }

                if (currentIsColorPickerActive && maxPointers == 1) {
                  handledByColorPicker = true
                  val w = size.width.toFloat().coerceAtLeast(1f)
                  val h = size.height.toFloat().coerceAtLeast(1f)
                  val normX = (change.position.x / w).coerceIn(0.08f, 0.92f)
                  val normY = (change.position.y / h).coerceIn(0.08f, 0.92f)
                  val updatedFocus = Offset(normX, normY)
                  if (colorFocusPointNormalized != updatedFocus) {
                    colorFocusPointNormalized = updatedFocus
                  }
                  event.changes.forEach { it.consume() }
                } else if (liveZoom > 1.02f && maxPointers == 1) {
                  val panChange = event.calculatePan()
                  if (panChange != Offset.Zero) {
                    panOffsetX += panChange.x
                    panOffsetY += panChange.y
                    event.changes.forEach { it.consume() }
                  }
                }
              }
            } while (event.changes.any { it.pressed })

            if (handledByColorPicker) {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } else if (maxPointers == 1 && !movedBeyondSlop) {
              currentOnTapImage()
            }
          }
        },
      contentAlignment = Alignment.Center,
    ) {
      if (isVideo) {
        var mediaPlayerRef by remember(photo.path, photo.lastModifiedMs) { mutableStateOf<MediaPlayer?>(null) }
        var surfaceRef by remember(photo.path, photo.lastModifiedMs) { mutableStateOf<AndroidSurface?>(null) }
        var isPlayerPrepared by remember(photo.path, photo.lastModifiedMs) { mutableStateOf(false) }
        var hasRenderedFirstFrame by remember(photo.path, photo.lastModifiedMs) { mutableStateOf(false) }
        var preparedDurationMs by remember(photo.path, photo.lastModifiedMs) { mutableLongStateOf(0L) }
        var maxObservedPositionMs by remember(photo.path, photo.lastModifiedMs) { mutableLongStateOf(0L) }
        var isSeekingMp by remember(photo.path, photo.lastModifiedMs) { mutableStateOf(false) }
        var seekStartedAtUptimeMs by remember(photo.path, photo.lastModifiedMs) { mutableLongStateOf(0L) }
        var pendingSeekTargetMs by remember(photo.path, photo.lastModifiedMs) { mutableLongStateOf(-1L) }
        var lastDispatchedSeekTargetMs by remember(photo.path, photo.lastModifiedMs) { mutableLongStateOf(-1L) }
        var lastSeekCompletedAtUptimeMs by remember(photo.path, photo.lastModifiedMs) { mutableLongStateOf(0L) }
        var lastAppliedSeekToken by remember(photo.path, photo.lastModifiedMs) { mutableLongStateOf(0L) }
        var videoWidth by remember(photo.path, photo.lastModifiedMs) { mutableIntStateOf(0) }
        var videoHeight by remember(photo.path, photo.lastModifiedMs) { mutableIntStateOf(0) }

        LaunchedEffect(photo.path, photo.lastModifiedMs) {
          val metadataDuration = withContext(Dispatchers.IO) {
            GalleryEngine.extractVideoDurationMs(photo.file)
          }
          if (metadataDuration > 0L && preparedDurationMs <= 0L) {
            preparedDurationMs = metadataDuration
            currentOnVideoDurationChange(metadataDuration)
          }
        }

        fun dispatchSeekToMediaPlayer(mp: MediaPlayer, targetMs: Long, isDragging: Boolean) {
          val upperBound = preparedDurationMs.takeIf { it > 0L } ?: Long.MAX_VALUE
          val clampedTarget = targetMs.coerceIn(0L, upperBound)
          isSeekingMp = true
          seekStartedAtUptimeMs = SystemClock.uptimeMillis()
          lastDispatchedSeekTargetMs = clampedTarget
          if (clampedTarget > maxObservedPositionMs) {
            maxObservedPositionMs = clampedTarget
          }
          try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              // Always use frame-accurate SEEK_CLOSEST so the playback clock matches the user's seek bar position
              // instead of snapping back to a distant keyframe (such as 0ms on short clips with a single I-frame).
              val mode = if (isDragging && preparedDurationMs > 30_000L) {
                MediaPlayer.SEEK_CLOSEST_SYNC
              } else {
                MediaPlayer.SEEK_CLOSEST
              }
              mp.seekTo(clampedTarget, mode)
            } else {
              mp.seekTo(clampedTarget.toInt())
            }
          } catch (_: Throwable) {
            try {
              mp.seekTo(clampedTarget.toInt())
            } catch (_: Throwable) {
              isSeekingMp = false
            }
          }
        }

        fun releaseActiveVideoPlayer() {
          isPlayerPrepared = false
          hasRenderedFirstFrame = false
          isSeekingMp = false
          pendingSeekTargetMs = -1L
          lastDispatchedSeekTargetMs = -1L
          mediaPlayerRef?.let { mp ->
            try {
              mp.setOnPreparedListener(null)
              mp.setOnVideoSizeChangedListener(null)
              mp.setOnSeekCompleteListener(null)
              mp.setOnCompletionListener(null)
              mp.setOnInfoListener(null)
              mp.setOnErrorListener(null)
            } catch (_: Throwable) {}
            try {
              mp.setSurface(null)
            } catch (_: Throwable) {}
            try {
              mp.stop()
            } catch (_: Throwable) {}
            try {
              mp.release()
            } catch (_: Throwable) {}
          }
          mediaPlayerRef = null
          surfaceRef?.let { s ->
            try {
              s.release()
            } catch (_: Throwable) {}
          }
          surfaceRef = null
        }

        LaunchedEffect(isCurrentPage, preparedDurationMs) {
          if (preparedDurationMs > 0L) {
            currentOnVideoDurationChange(preparedDurationMs)
          }
        }

        DisposableEffect(isCurrentPage, isVideoPlaying) {
          if (isCurrentPage && isVideoPlaying) {
            MusicPlayerEngine.isInAppVideoPlaybackActive = true
          }
          onDispose {
            if (isCurrentPage) {
              MusicPlayerEngine.isInAppVideoPlaybackActive = false
            }
          }
        }

        LaunchedEffect(mediaPlayerRef, isPlayerPrepared, isCurrentPage, isVideoPlaying, isVideoMuted) {
          val mp = mediaPlayerRef ?: return@LaunchedEffect
          if (!isPlayerPrepared) return@LaunchedEffect
          try {
            val vol = if (isVideoMuted) 0f else 1f
            mp.setVolume(vol, vol)
          } catch (_: Throwable) {}

          try {
            if (isCurrentPage && isVideoPlaying) {
              if (!mp.isPlaying) {
                mp.start()
              }
            } else {
              if (mp.isPlaying) {
                mp.pause()
              }
              if (!isCurrentPage) {
                maxObservedPositionMs = 0L
                lastDispatchedSeekTargetMs = -1L
                mp.seekTo(0)
                currentOnVideoPositionChange(0L)
              }
            }
          } catch (_: Throwable) {}
        }

        LaunchedEffect(videoSeekRequestToken, mediaPlayerRef, isPlayerPrepared) {
          if (videoSeekRequestToken <= 0L || videoSeekRequestToken == lastAppliedSeekToken) {
            return@LaunchedEffect
          }
          val mp = mediaPlayerRef ?: return@LaunchedEffect
          if (!isPlayerPrepared) return@LaunchedEffect
          lastAppliedSeekToken = videoSeekRequestToken
          val targetMs = videoSeekTargetMs.coerceAtLeast(0L)
          val nowUptime = SystemClock.uptimeMillis()
          if (isSeekingMp && currentIsDraggingVideoSeek && (nowUptime - seekStartedAtUptimeMs) < 280L) {
            pendingSeekTargetMs = targetMs
          } else {
            pendingSeekTargetMs = -1L
            dispatchSeekToMediaPlayer(mp, targetMs, isDragging = currentIsDraggingVideoSeek)
          }
        }

        LaunchedEffect(
          mediaPlayerRef,
          isPlayerPrepared,
          isCurrentPage,
          isVideoPlaying,
          isDraggingVideoSeek,
        ) {
          val mp = mediaPlayerRef ?: return@LaunchedEffect
          if (!isPlayerPrepared || !isCurrentPage || isDraggingVideoSeek) {
            return@LaunchedEffect
          }
          fun syncPlayerClockAndDuration() {
            val now = SystemClock.uptimeMillis()
            if (isSeekingMp && (now - seekStartedAtUptimeMs) > 350L) {
              isSeekingMp = false
            }
            if (isSeekingMp) return
            try {
              val reportedDuration = mp.duration.toLong()
              if (reportedDuration > 0L && preparedDurationMs <= 0L) {
                preparedDurationMs = reportedDuration
                currentOnVideoDurationChange(reportedDuration)
              }
              val rawPos = mp.currentPosition.toLong().coerceAtLeast(0L)
              // Avoid immediate post-seek snap-back while NuPlayer's audio/video clock settles on the seek target
              val heldTarget = lastDispatchedSeekTargetMs
              val pos = if (
                heldTarget >= 0L &&
                (now - lastSeekCompletedAtUptimeMs) < 180L &&
                kotlin.math.abs(rawPos - heldTarget) > 400L
              ) {
                heldTarget
              } else {
                if (heldTarget >= 0L && (now - lastSeekCompletedAtUptimeMs) >= 180L) {
                  lastDispatchedSeekTargetMs = -1L
                }
                rawPos
              }
              if (pos > maxObservedPositionMs) {
                maxObservedPositionMs = pos
              }
              if (pos > preparedDurationMs && pos > 0L) {
                preparedDurationMs = pos
                currentOnVideoDurationChange(pos)
              }
              currentOnVideoPositionChange(pos)
            } catch (_: Throwable) {}
          }

          if (!isVideoPlaying) {
            syncPlayerClockAndDuration()
            return@LaunchedEffect
          }
          while (
            isActive &&
            isPlayerPrepared &&
            isCurrentPage &&
            isVideoPlaying &&
            !isDraggingVideoSeek
          ) {
            syncPlayerClockAndDuration()
            delay(40L)
          }
        }

        DisposableEffect(photo.path, photo.lastModifiedMs, isCurrentPage) {
          onDispose {
            releaseActiveVideoPlayer()
          }
        }

        val (displayVideoWidth, displayVideoHeight) = GalleryEngine.resolveVideoDisplayDimensions(
          rawVideoWidth = videoWidth,
          rawVideoHeight = videoHeight,
          thumbnailWidth = currentBitmap?.width ?: 0,
          thumbnailHeight = currentBitmap?.height ?: 0,
        )
        val videoAspect = if (displayVideoWidth > 0 && displayVideoHeight > 0) {
          displayVideoWidth.toFloat() / displayVideoHeight.toFloat()
        } else {
          16f / 9f
        }
        val availWPx = containerSizePx.width.toFloat()
        val availHPx = containerSizePx.height.toFloat()
        val (fitWPx, fitHPx) = if (availWPx > 0f && availHPx > 0f) {
          if (availWPx / availHPx > videoAspect) {
            (availHPx * videoAspect) to availHPx
          } else {
            availWPx to (availWPx / videoAspect)
          }
        } else {
          320f to 180f
        }
        val fitW = with(density) { fitWPx.toDp() }
        val fitH = with(density) { fitHPx.toDp() }

        Box(
          modifier = Modifier
            .fillMaxSize()
            .testTag("photo_viewer_image_${photo.name}"),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer {
                scaleX = zoomScale
                scaleY = zoomScale
                translationX = panOffsetX
                translationY = panOffsetY
              }
              .testTag("photo_viewer_video_${photo.name}"),
            contentAlignment = Alignment.Center,
          ) {
            if (!isPlayerPrepared || !hasRenderedFirstFrame || !isCurrentPage) {
              if (currentImageBitmap != null) {
                Image(
                  bitmap = currentImageBitmap,
                  contentDescription = photo.name,
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.fillMaxSize(),
                )
              } else {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = photo.name,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp),
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = photo.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                  )
                }
              }
            }

            if (isCurrentPage && !isInspectionMode && photo.file.exists()) {
              key(photo.path, photo.lastModifiedMs) {
                AndroidView(
                  factory = { ctx ->
                    TextureView(ctx).apply {
                      surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                          surfaceTexture: SurfaceTexture,
                          width: Int,
                          height: Int,
                        ) {
                          releaseActiveVideoPlayer()
                          val surface = AndroidSurface(surfaceTexture)
                          val mp = MediaPlayer()
                          surfaceRef = surface
                          mediaPlayerRef = mp
                          try {
                            mp.setDataSource(photo.file.absolutePath)
                            mp.setSurface(surface)
                            // Manage looping manually via OnCompletionListener so that:
                            // 1) OnCompletionListener fires at true stream EOS, calibrating the seek bar
                            //    duration if the container/audio header duration differs from actual playable video length.
                            // 2) NuPlayer's playback clock always resets cleanly to 0ms on each loop.
                            mp.isLooping = false
                            mp.setOnVideoSizeChangedListener { _, w, h ->
                              if (w > 0 && h > 0) {
                                videoWidth = w
                                videoHeight = h
                              }
                            }
                            mp.setOnInfoListener { _, what, _ ->
                              if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                                hasRenderedFirstFrame = true
                              }
                              false
                            }
                            mp.setOnSeekCompleteListener { seekMp ->
                              val nextTarget = pendingSeekTargetMs
                              if (nextTarget >= 0L) {
                                pendingSeekTargetMs = -1L
                                dispatchSeekToMediaPlayer(
                                  mp = seekMp,
                                  targetMs = nextTarget,
                                  isDragging = currentIsDraggingVideoSeek,
                                )
                              } else {
                                lastSeekCompletedAtUptimeMs = SystemClock.uptimeMillis()
                                isSeekingMp = false
                              }
                            }
                            mp.setOnCompletionListener { completedMp ->
                              if (mediaPlayerRef !== completedMp) return@setOnCompletionListener
                              val finalPos = try {
                                completedMp.currentPosition.toLong().coerceAtLeast(0L)
                              } catch (_: Throwable) {
                                0L
                              }
                              val observedEndMs = maxOf(maxObservedPositionMs, finalPos)
                              if (
                                !currentIsDraggingVideoSeek &&
                                observedEndMs > 250L &&
                                (preparedDurationMs <= 0L || kotlin.math.abs(preparedDurationMs - observedEndMs) > 120L)
                              ) {
                                preparedDurationMs = observedEndMs
                                currentOnVideoDurationChange(observedEndMs)
                              }
                              maxObservedPositionMs = 0L
                              lastDispatchedSeekTargetMs = -1L
                              isSeekingMp = false
                              pendingSeekTargetMs = -1L
                              try {
                                completedMp.seekTo(0)
                                currentOnVideoPositionChange(0L)
                                if (currentIsCurrentPage && currentIsVideoPlaying && !currentIsDraggingVideoSeek) {
                                  completedMp.start()
                                }
                              } catch (_: Throwable) {}
                            }
                            mp.setOnPreparedListener { preparedMp ->
                              if (mediaPlayerRef !== preparedMp) {
                                try {
                                  preparedMp.release()
                                } catch (_: Throwable) {}
                                return@setOnPreparedListener
                              }
                              if (preparedMp.videoWidth > 0 && preparedMp.videoHeight > 0) {
                                videoWidth = preparedMp.videoWidth
                                videoHeight = preparedMp.videoHeight
                              }
                              val duration = preparedMp.duration.toLong()
                              if (duration > 0L) {
                                preparedDurationMs = duration
                                currentOnVideoDurationChange(duration)
                              }
                              preparedMp.isLooping = false
                              val vol = if (currentIsVideoMuted) 0f else 1f
                              try {
                                preparedMp.setVolume(vol, vol)
                              } catch (_: Throwable) {}
                              isPlayerPrepared = true
                              if (currentIsCurrentPage && currentIsVideoPlaying) {
                                try {
                                  preparedMp.start()
                                } catch (_: Throwable) {}
                              }
                            }
                            mp.setOnErrorListener { _, _, _ ->
                              isPlayerPrepared = false
                              hasRenderedFirstFrame = false
                              true
                            }
                            mp.prepareAsync()
                          } catch (_: Throwable) {
                            releaseActiveVideoPlayer()
                          }
                        }

                        override fun onSurfaceTextureSizeChanged(
                          surface: SurfaceTexture,
                          width: Int,
                          height: Int,
                        ) = Unit

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                          releaseActiveVideoPlayer()
                          return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                          if (isPlayerPrepared && !hasRenderedFirstFrame) {
                            hasRenderedFirstFrame = true
                          }
                        }
                      }
                    }
                  },
                  modifier = if (containerSizePx.width > 0 && containerSizePx.height > 0) {
                    Modifier.size(fitW, fitH)
                  } else {
                    Modifier.fillMaxSize()
                  },
                )
              }
            }
          }
        }
      } else if (isAnimatedImage) {
        AnimatedImagePlayerSurface(
          file = photo.file,
          fallbackBitmap = currentBitmap,
          contentDescription = photo.name,
          isPlaying = isCurrentPage && isVideoPlaying,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
              scaleX = zoomScale
              scaleY = zoomScale
              translationX = panOffsetX
              translationY = panOffsetY
            }
            .testTag("photo_viewer_image_${photo.name}")
            .testTag("photo_viewer_animated_${photo.name}"),
        )
      } else if (currentImageBitmap != null) {
        Image(
          bitmap = currentImageBitmap,
          contentDescription = photo.name,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
              scaleX = zoomScale
              scaleY = zoomScale
              translationX = panOffsetX
              translationY = panOffsetY
            }
            .testTag("photo_viewer_image_${photo.name}"),
        )
      } else {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .graphicsLayer {
              scaleX = zoomScale
              scaleY = zoomScale
              translationX = panOffsetX
              translationY = panOffsetY
            }
            .testTag("photo_viewer_image_${photo.name}"),
        ) {
          Icon(
            imageVector = Icons.Default.Image,
            contentDescription = photo.name,
            tint = Color.White,
            modifier = Modifier.size(64.dp),
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = photo.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
          )
        }
      }
    }

    // Camera auto-focus style color picker reticle & Music Player-styled hex color pill
    if (isColorPickerActive && !isVideo) {
      val focusRingColor = MaterialTheme.colorScheme.tertiary
      val activeHex = sampledColorHex ?: GalleryEngine.samplePhotoColorHex(
        file = photo.file,
        bitmap = currentBitmap,
        normX = colorFocusPointNormalized.x,
        normY = colorFocusPointNormalized.y,
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
          .testTag("photo_viewer_color_focus_indicator"),
      ) {
        val boxW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val boxH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val cx = boxW * colorFocusPointNormalized.x
        val cy = boxH * colorFocusPointNormalized.y
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

        // Hex Color Pill below the auto-focus circle (styled like Music Player color pills)
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
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
            .clickable { onOpenColorPicker(activeHex) }
            .testTag("photo_viewer_color_pill"),
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
                .testTag("photo_viewer_color_swatch"),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = activeHex,
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              modifier = Modifier.testTag("photo_viewer_color_hex_text"),
            )
          }
        }
      }
    }

    // Camera-style recognized element bounding box & tag overlay when a tag pill is pressed
    if (isCurrentPage && activeOverlayTag != null) {
      GalleryTagBoundingBoxOverlay(
        tag = activeOverlayTag,
        colorIndex = activeOverlayTagColorIndex,
        indexEntry = indexEntry,
        photoFile = photo.file,
        bitmap = currentBitmap,
        zoomScale = zoomScale,
        panOffsetX = panOffsetX,
        panOffsetY = panOffsetY,
        safeTopInsetPx = safeTopInsetPx,
        safeBottomInsetPx = safeBottomInsetPx,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}

/**
 * Draws the localized bounding box, corner accent brackets, and attached tag badge over a Gallery
 * image when a smart tag pill is tapped, matching [CameraScreen]'s Vision reticle overlay style.
 */
@Composable
private fun GalleryTagBoundingBoxOverlay(
  tag: String,
  colorIndex: Int,
  indexEntry: GalleryPhotoIndexEntry?,
  photoFile: File,
  bitmap: android.graphics.Bitmap?,
  zoomScale: Float,
  panOffsetX: Float,
  panOffsetY: Float,
  safeTopInsetPx: Int = 0,
  safeBottomInsetPx: Int = 0,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current

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

  val safeIdx = colorIndex.coerceAtLeast(0)
  val boxColor = boxStrokeColors[safeIdx % boxStrokeColors.size]
  val containerColor = badgeContainerColors[safeIdx % badgeContainerColors.size]
  val contentColor = badgeContentColors[safeIdx % badgeContentColors.size]

  val normBox = remember(tag, indexEntry, bitmap, photoFile.absolutePath) {
    GalleryEngine.resolveTagBoundingBox(
      tag = tag,
      indexEntry = indexEntry,
      bitmap = bitmap,
      file = photoFile,
    )
  }
  val confidence = remember(tag, indexEntry) {
    GalleryEngine.resolveTagConfidence(tag = tag, indexEntry = indexEntry)
  }

  var measuredBadgeSize by remember(tag) { mutableStateOf(IntSize.Zero) }

  BoxWithConstraints(
    modifier = modifier.testTag("photo_viewer_tag_bounding_box_overlay"),
  ) {
    val stageW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
    val stageH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
    val edgePadPx = with(density) { 12.dp.toPx() }
    val insetPx = with(density) { 6.dp.toPx() }

    val viewportBox = remember(
      normBox,
      stageW,
      stageH,
      bitmap?.width,
      bitmap?.height,
      zoomScale,
      panOffsetX,
      panOffsetY,
      edgePadPx,
    ) {
      GalleryEngine.mapImageBoundingBoxToViewport(
        normBox = normBox,
        containerWidth = stageW,
        containerHeight = stageH,
        imageWidth = bitmap?.width ?: 0,
        imageHeight = bitmap?.height ?: 0,
        zoomScale = zoomScale,
        panOffsetX = panOffsetX,
        panOffsetY = panOffsetY,
        edgePaddingPx = edgePadPx,
      )
    }

    val leftPx = viewportBox.left
    val topPx = viewportBox.top
    val rightPx = viewportBox.right
    val bottomPx = viewportBox.bottom
    val boxW = (rightPx - leftPx).coerceAtLeast(1f)
    val boxH = (bottomPx - topPx).coerceAtLeast(1f)

    Canvas(modifier = Modifier.fillMaxSize()) {
      val cornerRadiusPx = 10.dp.toPx()
      val strokePx = 2.dp.toPx()
      val bracketStrokePx = 3.5.dp.toPx()
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

    // Semantics / layout node matching the exact bounding box region on screen
    Box(
      modifier = Modifier
        .offset {
          IntOffset(
            x = leftPx.roundToInt(),
            y = topPx.roundToInt(),
          )
        }
        .size(
          width = with(density) { boxW.toDp() },
          height = with(density) { boxH.toDp() },
        )
        .testTag("photo_viewer_tag_box_$tag")
        .semantics {
          contentDescription = "Recognized $tag bounding box"
        },
    )

    val estBadgeW = if (measuredBadgeSize.width > 0) {
      measuredBadgeSize.width.toFloat()
    } else {
      with(density) { (38.dp + (tag.length * 7.5f).dp).toPx() }
    }
    val estBadgeH = if (measuredBadgeSize.height > 0) {
      measuredBadgeSize.height.toFloat()
    } else {
      with(density) { 26.dp.toPx() }
    }

    val safeTop = (safeTopInsetPx.toFloat() + insetPx).coerceAtMost(stageH * 0.35f)
    val safeBottom = (stageH - safeBottomInsetPx.toFloat() - estBadgeH - insetPx)
      .coerceAtLeast(safeTop)

    val badgeLeftPx = (leftPx + insetPx).coerceIn(
      edgePadPx,
      (stageW - estBadgeW - edgePadPx).coerceAtLeast(edgePadPx),
    )
    val rawBadgeTopPx = topPx + insetPx
    val badgeTopPx = rawBadgeTopPx.coerceIn(safeTop, safeBottom)

    Surface(
      shape = RoundedCornerShape(8.dp),
      color = containerColor.copy(alpha = 0.92f),
      border = BorderStroke(1.5.dp, boxColor),
      tonalElevation = 3.dp,
      modifier = Modifier
        .onSizeChanged { measuredBadgeSize = it }
        .offset {
          IntOffset(
            x = badgeLeftPx.roundToInt(),
            y = badgeTopPx.roundToInt(),
          )
        }
        .testTag("photo_viewer_tag_box_badge_$tag"),
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
            .background(boxColor),
        )
        Text(
          text = tag,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = contentColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (confidence != null) {
          Text(
            text = "${(confidence * 100f).roundToInt().coerceIn(0, 100)}%",
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

@Preview(showBackground = true)
@Composable
private fun ImageScreenPreview() {
  ToolboxTheme {
    ImageScreen()
  }
}
