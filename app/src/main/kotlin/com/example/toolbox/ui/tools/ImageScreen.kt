package com.example.toolbox.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.Dispatchers
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
 *   across sibling images in the folder, pinch-to-zoom (`100%..500%`), dismissible top/bottom
 *   translucent overlay bars, OCR/QR/Smart Tag recognition inspector, interactive camera-style
 *   color picker reticle with hex pill link to the Colors tool, one-tap Edit link to Image Editor,
 *   Share, Delete confirmation, and bottom thumbnail preview strip.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
        .background(MaterialTheme.colorScheme.background)
        .testTag("image_screen"),
    ) {
      ToolboxTopBar(
        title = "Image",
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
            ?.filter { it.isFile && !it.name.startsWith(".") && FileManagerEngine.isImageFile(it) }
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
            localPhotoIndexMap = localPhotoIndexMap + (entry.path to entry)
            if (!isInspection) {
              GalleryEngine.saveIndexCacheToDisk(indexCacheFile, localPhotoIndexMap)
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

@OptIn(ExperimentalLayoutApi::class)
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

@OptIn(ExperimentalLayoutApi::class)
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

  // Videos play automatically and muted by default in the viewer
  var isVideoPlaying by rememberSaveable(initialPhoto.path) { mutableStateOf(true) }
  var isVideoMuted by rememberSaveable(initialPhoto.path) { mutableStateOf(true) }

  // Current active page's zoom scale (1f..5f)
  var currentZoomScale by remember { mutableFloatStateOf(MIN_PHOTO_ZOOM) }

  // Reset zoom and auto-play muted when swiping to a different photo/video, keeping bottom preview scroll bar synced
  LaunchedEffect(pagerState.currentPage) {
    currentZoomScale = MIN_PHOTO_ZOOM
    isVideoPlaying = true
    isVideoMuted = true
    if (viewerPhotos.isNotEmpty() && pagerState.currentPage in viewerPhotos.indices) {
      if (viewerPhotos[pagerState.currentPage].isVideo) {
        isColorPickerActive = false
      }
      previewListState.animateScrollToItem(pagerState.currentPage)
    }
  }

  val currentPhoto = viewerPhotos.getOrElse(pagerState.currentPage) { initialPhoto }
  val currentIndexEntry = photoIndexMap[currentPhoto.path]

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
        isColorPickerActive = isCurrentPage && isColorPickerActive && !pagePhoto.isVideo,
        zoomScale = if (isCurrentPage) currentZoomScale else MIN_PHOTO_ZOOM,
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
            .clickable { currentZoomScale = MIN_PHOTO_ZOOM }
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

        Spacer(modifier = Modifier.width(4.dp))

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
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (currentPhoto.isVideo || currentPhoto.isAnimatedImage) {
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

            if (currentPhoto.isVideo) {
              Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable { isVideoMuted = !isVideoMuted }
                  .testTag("photo_viewer_mute_button"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Icon(
                    imageVector = if (isVideoMuted) {
                      Icons.AutoMirrored.Filled.VolumeOff
                    } else {
                      Icons.AutoMirrored.Filled.VolumeUp
                    },
                    contentDescription = if (isVideoMuted) "Unmute video" else "Mute video",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = if (isVideoMuted) "Muted" else "Sound on",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.testTag("photo_viewer_video_mute_state_label"),
                  )
                }
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

                currentIndexEntry.autoTags.forEach { tag ->
                  Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                    modifier = Modifier
                      .clip(CircleShape)
                      .clickable { showRecognitionInfoSheet = true }
                      .testTag("photo_viewer_tag_$tag"),
                  ) {
                    Text(
                      text = tag,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Medium,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
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

            LazyRow(
              state = previewListState,
              contentPadding = PaddingValues(
                start = effStartInset + 16.dp,
                end = effEndInset + 16.dp,
                top = 6.dp,
                bottom = effBottomInset + 12.dp,
              ),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
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

@OptIn(ExperimentalLayoutApi::class)
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
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Photo Info & Recognition",
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
        // 1. Auto-Tags Card
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "Automatic Tags",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            val tags = indexEntry?.autoTags.orEmpty()
            if (tags.isEmpty()) {
              Text(
                text = "No automatic tags detected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            } else {
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                tags.forEach { tag ->
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.testTag("photo_info_tag_$tag"),
                  ) {
                    Text(
                      text = tag,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                  }
                }
              }
            }
          }
        }

        // 2. Recognized Text (OCR) Card
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
internal fun PhotoViewerPageContent(
  photo: GalleryPhoto,
  isCurrentPage: Boolean,
  isVideoPlaying: Boolean = isCurrentPage,
  isVideoMuted: Boolean = true,
  isColorPickerActive: Boolean = false,
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
        try {
          BitmapFactory.decodeFile(photo.file.absolutePath)
        } catch (_: Throwable) {
          null
        }
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
  val currentIsColorPickerActive by rememberUpdatedState(isColorPickerActive)

  val currentBitmap = bitmap

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
        var isPlayerPrepared by remember(photo.path, photo.lastModifiedMs) { mutableStateOf(false) }
        var videoWidth by remember(photo.path, photo.lastModifiedMs) { mutableIntStateOf(0) }
        var videoHeight by remember(photo.path, photo.lastModifiedMs) { mutableIntStateOf(0) }

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
                mp.seekTo(0)
              }
            }
          } catch (_: Throwable) {}
        }

        DisposableEffect(photo.path, photo.lastModifiedMs) {
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

        Box(
          modifier = Modifier
            .fillMaxSize()
            .testTag("photo_viewer_image_${photo.name}"),
          contentAlignment = Alignment.Center,
        ) {
          BoxWithConstraints(
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
            val availWPx = with(density) { maxWidth.toPx() }
            val availHPx = with(density) { maxHeight.toPx() }
            val videoAspect = when {
              videoWidth > 0 && videoHeight > 0 -> videoWidth.toFloat() / videoHeight.toFloat()
              currentBitmap != null && currentBitmap.width > 0 && currentBitmap.height > 0 ->
                currentBitmap.width.toFloat() / currentBitmap.height.toFloat()
              else -> 16f / 9f
            }
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

            if (!isPlayerPrepared) {
              if (currentBitmap != null) {
                Image(
                  bitmap = currentBitmap.asImageBitmap(),
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

            if (!isInspectionMode && photo.file.exists()) {
              key(photo.path, photo.lastModifiedMs) {
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
                            mp.setDataSource(photo.file.absolutePath)
                            mp.setSurface(surface)
                            mp.isLooping = true
                            mp.setOnVideoSizeChangedListener { _, w, h ->
                              if (w > 0 && h > 0) {
                                videoWidth = w
                                videoHeight = h
                              }
                            }
                            mp.setOnPreparedListener { preparedMp ->
                              if (preparedMp.videoWidth > 0 && preparedMp.videoHeight > 0) {
                                videoWidth = preparedMp.videoWidth
                                videoHeight = preparedMp.videoHeight
                              }
                              preparedMp.isLooping = true
                              val vol = if (currentIsVideoMuted) 0f else 1f
                              try {
                                preparedMp.setVolume(vol, vol)
                              } catch (_: Throwable) {}
                              mediaPlayerRef = preparedMp
                              isPlayerPrepared = true
                              if (currentIsCurrentPage && currentIsVideoPlaying) {
                                try {
                                  preparedMp.start()
                                } catch (_: Throwable) {}
                              }
                            }
                            mp.setOnErrorListener { _, _, _ ->
                              isPlayerPrepared = false
                              true
                            }
                            mp.prepareAsync()
                          } catch (_: Throwable) {
                            isPlayerPrepared = false
                            try {
                              mp.release()
                            } catch (_: Throwable) {}
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
                    }
                  },
                  modifier = Modifier
                    .size(fitW, fitH)
                    .alpha(if (isPlayerPrepared) 1f else 0f),
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
      } else if (currentBitmap != null) {
        Image(
          bitmap = currentBitmap.asImageBitmap(),
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
  }
}

@Preview(showBackground = true)
@Composable
private fun ImageScreenPreview() {
  ToolboxTheme {
    ImageScreen()
  }
}
