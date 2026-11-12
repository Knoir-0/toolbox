package com.example.toolbox.ui.tools

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
  modifier: Modifier = Modifier,
  rootDirectory: File? = null,
  initialFolders: List<File>? = null,
  initialAlbumDirectory: File? = null,
  initialAlbumsPerRow: Int? = null,
  initialViewMode: GalleryViewMode? = null,
  onNavigateBack: (() -> Unit)? = null,
  onOpenImageEditor: ((File) -> Unit)? = null,
  onOpenVideoEditor: ((File) -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current

  val prefs = remember(context, isInspection) {
    if (isInspection) null
    else context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
  }

  val effectiveRootDir = remember(rootDirectory, isInspection) {
    rootDirectory ?: if (isInspection) {
      File("/storage/emulated/0")
    } else {
      FileManagerEngine.getUserRootDirectory()
    }
  }

  // Storage permission state
  var hasPermission by remember {
    mutableStateOf(
      isInspection ||
        rootDirectory != null ||
        initialFolders != null ||
        initialAlbumDirectory != null ||
        FileManagerEngine.hasStoragePermission(context)
    )
  }

  var refreshTrigger by remember { mutableIntStateOf(0) }

  val manageStorageLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    hasPermission = FileManagerEngine.hasStoragePermission(context)
    refreshTrigger++
  }

  val legacyPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasPermission = isGranted
    refreshTrigger++
  }

  fun requestStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      try {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
          data = Uri.parse("package:${context.packageName}")
        }
        manageStorageLauncher.launch(intent)
      } catch (_: Exception) {
        try {
          manageStorageLauncher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } catch (_: Exception) {
          Toast.makeText(context, "Please grant storage access in System Settings", Toast.LENGTH_SHORT).show()
        }
      }
    } else {
      legacyPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }
  }

  // View mode: defaults to TILE view
  var viewMode by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_GALLERY_VIEW_MODE, null)
    mutableStateOf(initialViewMode ?: GalleryViewMode.fromNameOrDefault(saved))
  }

  fun updateViewMode(newMode: GalleryViewMode) {
    viewMode = newMode
    prefs?.edit()?.putString(PREF_KEY_GALLERY_VIEW_MODE, newMode.name)?.apply()
  }

  // Albums per row in Tile View: defaults to 3, pinchable from 3 up to 6
  var albumsPerRow by rememberSaveable {
    val saved = prefs?.getInt(PREF_KEY_GALLERY_ALBUMS_PER_ROW, DEFAULT_ALBUMS_PER_ROW)
      ?: DEFAULT_ALBUMS_PER_ROW
    mutableIntStateOf(GalleryEngine.coerceAlbumsPerRow(initialAlbumsPerRow ?: saved))
  }

  fun updateAlbumsPerRow(newCount: Int) {
    val coerced = GalleryEngine.coerceAlbumsPerRow(newCount)
    if (coerced != albumsPerRow) {
      albumsPerRow = coerced
      prefs?.edit()?.putInt(PREF_KEY_GALLERY_ALBUMS_PER_ROW, coerced)?.apply()
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  // Additional user-added folders beyond default DCIM and Photos
  var additionalFolderPaths by rememberSaveable {
    val raw = prefs?.getString(PREF_KEY_GALLERY_FOLDERS, null).orEmpty()
    val list = raw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    mutableStateOf(list)
  }

  fun addCustomFolder(folderPath: String) {
    val updated = (additionalFolderPaths + folderPath.trim()).distinct()
    additionalFolderPaths = updated
    prefs?.edit()?.putString(PREF_KEY_GALLERY_FOLDERS, updated.joinToString("|"))?.apply()
    refreshTrigger++
  }

  // Load un-nested root contents (DCIM and Photos merged as if they were the same folder)
  val baseRootContents = remember(effectiveRootDir, initialFolders, additionalFolderPaths, refreshTrigger, isInspection) {
    if (isInspection && rootDirectory == null && initialFolders == null) {
      AlbumContents(
        directory = File("/storage/emulated/0/DCIM"),
        directories = listOf(File("/storage/emulated/0/DCIM"), File("/storage/emulated/0/Photos")),
        nestedAlbums = listOf(
          GalleryAlbum(
            directory = File("/storage/emulated/0/DCIM/Camera"),
            name = "Camera",
            path = "/storage/emulated/0/DCIM/Camera",
            mostRecentPhoto = null,
            directPhotoCount = 12,
            totalPhotoCount = 24,
            subAlbumCount = 1,
            lastModifiedMs = System.currentTimeMillis() - 60_000L,
          ),
          GalleryAlbum(
            directory = File("/storage/emulated/0/DCIM/Screenshots"),
            name = "Screenshots",
            path = "/storage/emulated/0/DCIM/Screenshots",
            mostRecentPhoto = null,
            directPhotoCount = 6,
            totalPhotoCount = 6,
            subAlbumCount = 0,
            lastModifiedMs = System.currentTimeMillis() - 90_000L,
          ),
          GalleryAlbum(
            directory = File("/storage/emulated/0/Photos/Vacation"),
            name = "Vacation",
            path = "/storage/emulated/0/Photos/Vacation",
            mostRecentPhoto = null,
            directPhotoCount = 8,
            totalPhotoCount = 15,
            subAlbumCount = 1,
            lastModifiedMs = System.currentTimeMillis() - 120_000L,
          ),
        ),
        photos = emptyList(),
      )
    } else {
      GalleryEngine.loadRootContents(
        rootDirectory = effectiveRootDir,
        folders = initialFolders,
        additionalPaths = additionalFolderPaths,
      )
    }
  }

  val resolvedInitialAlbum: GalleryAlbum? = remember(
    initialAlbumDirectory?.absolutePath,
    baseRootContents,
    refreshTrigger,
    isInspection,
  ) {
    initialAlbumDirectory?.let { dir ->
      if (!isInspection && !dir.exists()) {
        try {
          dir.mkdirs()
        } catch (_: Throwable) {}
      }
      val built = GalleryEngine.buildAlbum(dir)
      val displayName = if (dir.name.startsWith("camera", ignoreCase = true) || dir.name.isBlank()) {
        "Camera"
      } else {
        built.name
      }
      val matchingRoot = baseRootContents.nestedAlbums.find {
        it.name.equals(displayName, ignoreCase = true) ||
          it.directories.any { d -> d.absolutePath.equals(dir.absolutePath, ignoreCase = true) }
      }
      if (matchingRoot != null && !isInspection) {
        val mergedDirs = (listOf(dir) + matchingRoot.directories)
          .distinctBy { it.absolutePath.lowercase() }
        GalleryEngine.buildMergedAlbum(mergedDirs).copy(name = displayName)
      } else {
        built.copy(name = displayName)
      }
    }
  }

  val rootContents = remember(baseRootContents, resolvedInitialAlbum) {
    if (resolvedInitialAlbum == null) {
      baseRootContents
    } else {
      val existingIndex = baseRootContents.nestedAlbums.indexOfFirst {
        it.name.equals(resolvedInitialAlbum.name, ignoreCase = true) ||
          it.directories.any { d -> d.absolutePath.equals(resolvedInitialAlbum.directory.absolutePath, ignoreCase = true) }
      }
      val updatedNested = if (existingIndex >= 0) {
        baseRootContents.nestedAlbums.toMutableList().apply {
          set(existingIndex, resolvedInitialAlbum)
        }
      } else {
        (baseRootContents.nestedAlbums + resolvedInitialAlbum)
          .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
      }
      baseRootContents.copy(nestedAlbums = updatedNested)
    }
  }

  // Navigation stack for nested albums/folders
  var navigatedToRootFromInitial by remember(initialAlbumDirectory?.absolutePath) {
    mutableStateOf(false)
  }
  val albumStack = remember(initialAlbumDirectory?.absolutePath) {
    mutableStateListOf<GalleryAlbum>().apply {
      if (resolvedInitialAlbum != null) {
        add(resolvedInitialAlbum)
      }
    }
  }
  LaunchedEffect(resolvedInitialAlbum) {
    if (resolvedInitialAlbum != null &&
      albumStack.size == 1 &&
      albumStack[0].name.equals(resolvedInitialAlbum.name, ignoreCase = true)
    ) {
      albumStack[0] = resolvedInitialAlbum
    }
  }
  val currentAlbum: GalleryAlbum? = albumStack.lastOrNull()

  fun handleGalleryBack() {
    when {
      albumStack.size > 1 -> {
        albumStack.removeAt(albumStack.lastIndex)
      }
      albumStack.size == 1 -> {
        if (!navigatedToRootFromInitial && initialAlbumDirectory != null && onNavigateBack != null) {
          onNavigateBack()
        } else {
          albumStack.removeAt(albumStack.lastIndex)
        }
      }
      else -> {
        onNavigateBack?.invoke()
      }
    }
  }

  BackHandler(enabled = albumStack.isNotEmpty() || onNavigateBack != null) {
    handleGalleryBack()
  }

  val currentAlbumContents = remember(currentAlbum, refreshTrigger) {
    currentAlbum?.let { album ->
      GalleryEngine.loadAlbumContents(album)
    }
  }

  val activeAlbums: List<GalleryAlbum> = if (currentAlbum == null) {
    rootContents.nestedAlbums
  } else {
    currentAlbumContents?.nestedAlbums.orEmpty()
  }

  val activePhotos: List<GalleryPhoto> = if (currentAlbum == null) {
    rootContents.photos
  } else {
    currentAlbumContents?.photos.orEmpty()
  }

  // Defer album art pre-decoding to a background coroutine on Dispatchers.IO
  LaunchedEffect(activeAlbums, isInspection) {
    if (!isInspection && activeAlbums.isNotEmpty()) {
      GalleryEngine.prefetchAlbumArt(activeAlbums)
    }
  }

  var selectedPhotoForViewer by remember { mutableStateOf<GalleryPhoto?>(null) }
  var showAddFolderDialog by remember { mutableStateOf(false) }
  var fileForImageEditor by remember { mutableStateOf<File?>(null) }
  var fileForVideoEditor by remember { mutableStateOf<File?>(null) }

  fun openMediaInEditor(photo: GalleryPhoto) {
    selectedPhotoForViewer = null
    if (photo.isVideo) {
      fileForVideoEditor = photo.file
      onOpenVideoEditor?.invoke(photo.file)
    } else {
      fileForImageEditor = photo.file
      onOpenImageEditor?.invoke(photo.file)
    }
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
      initialDirectory = currentAlbum?.directory ?: effectiveRootDir,
      modifier = modifier,
      onNavigateBack = {
        fileForVideoEditor = null
        refreshTrigger++
      },
    )
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("gallery_screen"),
  ) {
    // Top Toolbar following DESIGN_LANGUAGE.md 5.1
    GalleryTopBar(
      title = currentAlbum?.name ?: "Gallery",
      subtext = if (currentAlbum == null) {
        val albumWord = if (activeAlbums.size == 1) "1 album" else "${activeAlbums.size} albums"
        val modeInfo = if (viewMode == GalleryViewMode.TILE) "$albumsPerRow per row" else "List view"
        if (activePhotos.isNotEmpty()) {
          val photoWord = if (activePhotos.size == 1) "1 photo" else "${activePhotos.size} photos"
          "$albumWord • $photoWord • $modeInfo"
        } else {
          "$albumWord • $modeInfo"
        }
      } else {
        val nestedCount = activeAlbums.size
        val photoCount = activePhotos.size
        buildString {
          if (nestedCount > 0) {
            append(if (nestedCount == 1) "1 folder" else "$nestedCount folders")
            append(" • ")
          }
          append(if (photoCount == 1) "1 photo" else "$photoCount photos")
        }
      },
      canNavigateBack = albumStack.isNotEmpty() || onNavigateBack != null,
      viewMode = viewMode,
      onBack = { handleGalleryBack() },
      onToggleViewMode = {
        val next = if (viewMode == GalleryViewMode.TILE) GalleryViewMode.LIST else GalleryViewMode.TILE
        updateViewMode(next)
      },
      onSelectViewMode = { updateViewMode(it) },
      onOpenAddFolder = { showAddFolderDialog = true },
      onRefresh = { refreshTrigger++ },
    )

    // Breadcrumb & View/Density Controls Bar
    GalleryNavigationAndDensityBar(
      albumStack = albumStack.toList(),
      viewMode = viewMode,
      albumsPerRow = albumsPerRow,
      onNavigateToRoot = {
        navigatedToRootFromInitial = true
        albumStack.clear()
      },
      onNavigateToIndex = { index ->
        while (albumStack.size > index + 1) {
          albumStack.removeAt(albumStack.lastIndex)
        }
      },
      onSelectViewMode = { updateViewMode(it) },
      onAlbumsPerRowChange = { updateAlbumsPerRow(it) },
    )

    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
    )

    if (!hasPermission) {
      GalleryPermissionBanner(
        onGrantPermission = { requestStoragePermission() },
      )
    }

    // Main Content Area (Tile View with pinch-to-zoom 3..6 per row, or List View)
    val currentColumnsState by rememberUpdatedState(albumsPerRow)
    val currentOnColumnsChange by rememberUpdatedState<(Int) -> Unit>({ updateAlbumsPerRow(it) })

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .testTag("gallery_content_container")
        .pointerInput(viewMode) {
          if (viewMode != GalleryViewMode.TILE) return@pointerInput
          awaitEachGesture {
            var cumulativeZoom = 1f
            var liveColumns = currentColumnsState
            var multiTouchStarted = false

            do {
              val event = awaitPointerEvent(PointerEventPass.Initial)
              val pressedCount = event.changes.count { it.pressed }
              if (pressedCount >= 2) {
                if (!multiTouchStarted) {
                  multiTouchStarted = true
                  cumulativeZoom = 1f
                  liveColumns = currentColumnsState
                }
                val zoomChange = event.calculateZoom()
                if (zoomChange != 1f && zoomChange > 0f && !zoomChange.isNaN()) {
                  cumulativeZoom *= zoomChange

                  // Pinching in (fingers moving closer, zoom < 1f) -> increase albums per row up to 6
                  while (cumulativeZoom <= 0.82f) {
                    val next = GalleryEngine.coerceAlbumsPerRow(liveColumns + 1)
                    if (next != liveColumns) {
                      liveColumns = next
                      currentOnColumnsChange(next)
                    }
                    cumulativeZoom /= 0.82f
                  }

                  // Pinching out (fingers spreading apart, zoom > 1f) -> decrease albums per row down to 3
                  while (cumulativeZoom >= 1.22f) {
                    val next = GalleryEngine.coerceAlbumsPerRow(liveColumns - 1)
                    if (next != liveColumns) {
                      liveColumns = next
                      currentOnColumnsChange(next)
                    }
                    cumulativeZoom /= 1.22f
                  }

                  event.changes.forEach { it.consume() }
                }
              } else {
                multiTouchStarted = false
                cumulativeZoom = 1f
              }
            } while (event.changes.any { it.pressed })
          }
        },
    ) {
      if (activeAlbums.isEmpty() && activePhotos.isEmpty()) {
        EmptyAlbumStateView(
          folderName = currentAlbum?.name ?: "Gallery",
          onCreateSubfolder = { showAddFolderDialog = true },
        )
      } else if (viewMode == GalleryViewMode.TILE) {
        GalleryTileGridView(
          albums = activeAlbums,
          photos = activePhotos,
          albumsPerRow = albumsPerRow,
          onAlbumClick = { album ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            albumStack.add(album)
          },
          onPhotoClick = { photo ->
            selectedPhotoForViewer = photo
          },
          onPhotoEdit = { photo ->
            openMediaInEditor(photo)
          },
        )
      } else {
        GalleryListView(
          albums = activeAlbums,
          photos = activePhotos,
          onAlbumClick = { album ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            albumStack.add(album)
          },
          onPhotoClick = { photo ->
            selectedPhotoForViewer = photo
          },
          onPhotoEdit = { photo ->
            openMediaInEditor(photo)
          },
        )
      }
    }
  }

  // Full-screen photo preview dialog
  selectedPhotoForViewer?.let { photo ->
    GalleryPhotoViewerDialog(
      photos = activePhotos,
      initialPhoto = photo,
      onDismiss = { selectedPhotoForViewer = null },
      onEdit = { editedPhoto ->
        openMediaInEditor(editedPhoto)
      },
      onShare = { sharedPhoto ->
        sharePhotoFile(context, sharedPhoto.file)
      },
    )
  }

  // Add / Create Folder Dialog
  if (showAddFolderDialog) {
    val defaultPhotosDir = remember(effectiveRootDir, initialFolders) {
      initialFolders?.firstOrNull() ?: File(effectiveRootDir, "Photos")
    }
    AddGalleryFolderDialog(
      parentDirectory = currentAlbum?.directory ?: defaultPhotosDir,
      isRootLevel = currentAlbum == null,
      onDismiss = { showAddFolderDialog = false },
      onConfirm = { folderName ->
        val parent = currentAlbum?.directory ?: defaultPhotosDir
        val newDir = File(parent, folderName.trim())
        newDir.mkdirs()
        refreshTrigger++
        showAddFolderDialog = false
      },
    )
  }
}

@Composable
private fun GalleryTopBar(
  title: String,
  subtext: String,
  canNavigateBack: Boolean,
  viewMode: GalleryViewMode,
  onBack: () -> Unit,
  onToggleViewMode: () -> Unit,
  onSelectViewMode: (GalleryViewMode) -> Unit,
  onOpenAddFolder: () -> Unit,
  onRefresh: () -> Unit,
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          if (canNavigateBack) {
            IconButton(
              onClick = onBack,
              modifier = Modifier
                .size(36.dp)
                .testTag("gallery_back_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
              )
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
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("gallery_header_title"),
            )
            Text(
              text = subtext,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("gallery_header_subtitle"),
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          IconButton(
            onClick = onToggleViewMode,
            modifier = Modifier.testTag("gallery_view_mode_toggle"),
          ) {
            Icon(
              imageVector = if (viewMode == GalleryViewMode.TILE) {
                Icons.AutoMirrored.Filled.ViewList
              } else {
                Icons.Default.GridView
              },
              contentDescription = if (viewMode == GalleryViewMode.TILE) {
                "Switch to list view"
              } else {
                "Switch to tile view"
              },
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          IconButton(
            onClick = onOpenAddFolder,
            modifier = Modifier.testTag("gallery_add_folder_button"),
          ) {
            Icon(
              imageVector = Icons.Default.CreateNewFolder,
              contentDescription = "Add folder",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          IconButton(
            onClick = onRefresh,
            modifier = Modifier.testTag("gallery_refresh_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh gallery",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryNavigationAndDensityBar(
  albumStack: List<GalleryAlbum>,
  viewMode: GalleryViewMode,
  albumsPerRow: Int,
  onNavigateToRoot: () -> Unit,
  onNavigateToIndex: (Int) -> Unit,
  onSelectViewMode: (GalleryViewMode) -> Unit,
  onAlbumsPerRowChange: (Int) -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      // Breadcrumb path row when inside nested albums/folders
      if (albumStack.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag("gallery_breadcrumbs"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .clickable(onClick = onNavigateToRoot)
              .testTag("gallery_breadcrumb_root"),
          ) {
            Text(
              text = "Albums",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }

          albumStack.forEachIndexed { index, album ->
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp),
            )
            val isCurrent = index == albumStack.lastIndex
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isCurrent) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              },
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigateToIndex(index) }
                .testTag("gallery_breadcrumb_${album.name}"),
            ) {
              Text(
                text = album.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrent) {
                  MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }
      }

      // View mode pills (Tile / List) + Tile albums-per-row indicator (3..6)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FilterChip(
            selected = viewMode == GalleryViewMode.TILE,
            onClick = { onSelectViewMode(GalleryViewMode.TILE) },
            label = { Text("Tile") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("gallery_tile_view_button"),
          )

          FilterChip(
            selected = viewMode == GalleryViewMode.LIST,
            onClick = { onSelectViewMode(GalleryViewMode.LIST) },
            label = { Text("List") },
            leadingIcon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("gallery_list_view_button"),
          )
        }

        if (viewMode == GalleryViewMode.TILE) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.testTag("gallery_density_indicator"),
          ) {
            Text(
              text = "$albumsPerRow per row",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier
                .testTag("gallery_albums_per_row_label")
                .semantics {
                  contentDescription = "$albumsPerRow albums per row"
                },
            )
            // Compact badge displaying exact numeric count (3..6)
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier
                .clip(CircleShape)
                .clickable {
                  val next = if (albumsPerRow >= MAX_ALBUMS_PER_ROW) MIN_ALBUMS_PER_ROW else albumsPerRow + 1
                  onAlbumsPerRowChange(next)
                }
                .testTag("gallery_albums_per_row_count"),
            ) {
              Text(
                text = albumsPerRow.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun GalleryPermissionBanner(
  onGrantPermission: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
    ),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 8.dp)
      .testTag("gallery_permission_banner"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Storage access enables full photo scanning",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
          text = "Grant access to view all photos in DCIM and Photos",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = onGrantPermission,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Grant")
      }
    }
  }
}

@Composable
private fun GalleryTileGridView(
  albums: List<GalleryAlbum>,
  photos: List<GalleryPhoto>,
  albumsPerRow: Int,
  onAlbumClick: (GalleryAlbum) -> Unit,
  onPhotoClick: (GalleryPhoto) -> Unit,
  onPhotoEdit: (GalleryPhoto) -> Unit = {},
) {
  val coercedColumns = GalleryEngine.coerceAlbumsPerRow(albumsPerRow)
  val isCompactTile = coercedColumns >= 5

  LazyVerticalGrid(
    columns = GridCells.Fixed(coercedColumns),
    modifier = Modifier
      .fillMaxSize()
      .testTag("gallery_tile_grid")
      .semantics {
        contentDescription = "Tile view with $coercedColumns albums per row"
      },
    contentPadding = PaddingValues(12.dp),
    horizontalArrangement = Arrangement.spacedBy(if (isCompactTile) 8.dp else 12.dp),
    verticalArrangement = Arrangement.spacedBy(if (isCompactTile) 10.dp else 14.dp),
  ) {
    items(
      items = albums,
      key = { "album_${it.path}" },
    ) { album ->
      AlbumTileCard(
        album = album,
        isCompact = isCompactTile,
        onClick = { onAlbumClick(album) },
      )
    }

    if (albums.isNotEmpty() && photos.isNotEmpty()) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
          text = "Photos",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
            .testTag("gallery_photos_section_header"),
        )
      }
    }

    items(
      items = photos,
      key = { "photo_${it.path}" },
    ) { photo ->
      PhotoTileCard(
        photo = photo,
        isCompact = isCompactTile,
        onClick = { onPhotoClick(photo) },
        onEdit = { onPhotoEdit(photo) },
      )
    }
  }
}

/**
 * Renders a single album in Tile View:
 * - Square tile showing the most recent photo from the album
 * - Folder name label positioned directly underneath the tile
 */
@Composable
private fun AlbumTileCard(
  album: GalleryAlbum,
  isCompact: Boolean,
  onClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(2.dp)
      .testTag("album_tile_item_${album.name}"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Tile with most recent photo from the album
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .testTag("album_tile_${album.name}"),
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        AlbumCoverPhotoContent(
          albumName = album.name,
          photoFile = album.mostRecentPhoto,
          iconSize = if (isCompact) 24.dp else 36.dp,
          modifier = Modifier.fillMaxSize(),
        )

        // Nested folders indicator badge
        if (album.hasNestedAlbums && !isCompact) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(6.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp),
              )
              Text(
                text = album.subAlbumCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Label with the folder name underneath the tile
    Text(
      text = album.name,
      style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("album_label_${album.name}"),
    )

    if (!isCompact) {
      Text(
        text = album.summaryText,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun PhotoTileCard(
  photo: GalleryPhoto,
  isCompact: Boolean,
  onClick: () -> Unit,
  onEdit: () -> Unit = {},
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(2.dp)
      .testTag("photo_tile_item_${photo.name}"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .testTag("photo_tile_${photo.name}"),
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        PhotoThumbnailBox(
          photoFile = photo.file,
          contentDescription = photo.name,
          iconSize = if (isCompact) 22.dp else 32.dp,
          modifier = Modifier.fillMaxSize(),
        )
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(4.dp)
            .size(26.dp)
            .clip(CircleShape)
            .clickable(onClick = onEdit)
            .testTag("photo_tile_edit_${photo.name}"),
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit ${photo.name}",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = photo.name,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("photo_label_${photo.name}"),
    )
  }
}

/**
 * Secondary view: List View with the album photo as an icon on the left and the folder name label to the right.
 */
@Composable
private fun GalleryListView(
  albums: List<GalleryAlbum>,
  photos: List<GalleryPhoto>,
  onAlbumClick: (GalleryAlbum) -> Unit,
  onPhotoClick: (GalleryPhoto) -> Unit,
  onPhotoEdit: (GalleryPhoto) -> Unit = {},
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("gallery_list_view"),
    contentPadding = PaddingValues(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(
      items = albums,
      key = { "list_album_${it.path}" },
    ) { album ->
      AlbumListRow(
        album = album,
        onClick = { onAlbumClick(album) },
      )
    }

    if (albums.isNotEmpty() && photos.isNotEmpty()) {
      item {
        Text(
          text = "Photos",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
            .testTag("gallery_photos_section_header"),
        )
      }
    }

    items(
      items = photos,
      key = { "list_photo_${it.path}" },
    ) { photo ->
      PhotoListRow(
        photo = photo,
        onClick = { onPhotoClick(photo) },
        onEdit = { onPhotoEdit(photo) },
      )
    }
  }
}

@Composable
private fun AlbumListRow(
  album: GalleryAlbum,
  onClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("album_list_item_${album.name}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Album photo as an icon on the left
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .testTag("album_list_icon_${album.name}"),
        contentAlignment = Alignment.Center,
      ) {
        AlbumCoverPhotoContent(
          albumName = album.name,
          photoFile = album.mostRecentPhoto,
          iconSize = 26.dp,
          modifier = Modifier.fillMaxSize(),
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Label with the folder name to the right
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = album.name,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.testTag("album_label_${album.name}"),
        )
        Text(
          text = album.summaryText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun PhotoListRow(
  photo: GalleryPhoto,
  onClick: () -> Unit,
  onEdit: () -> Unit = {},
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("photo_list_item_${photo.name}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .testTag("photo_list_icon_${photo.name}"),
        contentAlignment = Alignment.Center,
      ) {
        PhotoThumbnailBox(
          photoFile = photo.file,
          contentDescription = photo.name,
          iconSize = 26.dp,
          modifier = Modifier.fillMaxSize(),
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = photo.name,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.testTag("photo_label_${photo.name}"),
        )
        Text(
          text = "${photo.sizeFormatted} • ${photo.timestampFormatted}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      IconButton(
        onClick = onEdit,
        modifier = Modifier
          .size(36.dp)
          .testTag("photo_list_edit_${photo.name}"),
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = "Edit ${photo.name}",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun AlbumCoverPhotoContent(
  albumName: String,
  photoFile: File?,
  iconSize: Dp,
  modifier: Modifier = Modifier,
) {
  val filePath = photoFile?.absolutePath
  val fileLastMod = photoFile?.lastModified()
  var bitmap by remember(filePath, fileLastMod) {
    mutableStateOf(photoFile?.let { GalleryEngine.getCachedThumbnailBitmap(it) })
  }

  // Defer decoding album art to a background coroutine on Dispatchers.IO when not yet cached
  LaunchedEffect(filePath, fileLastMod) {
    if (bitmap == null && photoFile != null) {
      bitmap = GalleryEngine.loadThumbnailBitmapAsync(photoFile)
    }
  }

  val coverDescription = if (photoFile != null) {
    "Most recent photo: ${photoFile.name}"
  } else {
    "Empty album: $albumName"
  }

  Box(
    modifier = modifier
      .testTag("album_cover_${albumName}")
      .semantics {
        contentDescription = coverDescription
      },
    contentAlignment = Alignment.Center,
  ) {
    if (bitmap != null) {
      Image(
        bitmap = bitmap!!.asImageBitmap(),
        contentDescription = coverDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxSize()
          .testTag("album_cover_photo_${albumName}"),
      )
    } else if (photoFile != null) {
      // Placeholder tile displayed while background process loads album art (or if file has no bitmap headers)
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.tertiaryContainer)
          .testTag("album_cover_photo_${albumName}"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Image,
          contentDescription = coverDescription,
          tint = MaterialTheme.colorScheme.onTertiaryContainer,
          modifier = Modifier.size(iconSize),
        )
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.PhotoLibrary,
          contentDescription = coverDescription,
          tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
          modifier = Modifier.size(iconSize),
        )
      }
    }
  }
}

@Composable
private fun PhotoThumbnailBox(
  photoFile: File,
  contentDescription: String,
  iconSize: Dp,
  modifier: Modifier = Modifier,
) {
  val filePath = photoFile.absolutePath
  val fileLastMod = photoFile.lastModified()
  var bitmap by remember(filePath, fileLastMod) {
    mutableStateOf(GalleryEngine.getCachedThumbnailBitmap(photoFile))
  }

  LaunchedEffect(filePath, fileLastMod) {
    if (bitmap == null) {
      bitmap = GalleryEngine.loadThumbnailBitmapAsync(photoFile)
    }
  }

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    if (bitmap != null) {
      Image(
        bitmap = bitmap!!.asImageBitmap(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.tertiaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (VideoEditorEngine.isVideoFile(photoFile)) {
            Icons.Default.VideoFile
          } else {
            Icons.Default.Image
          },
          contentDescription = contentDescription,
          tint = MaterialTheme.colorScheme.onTertiaryContainer,
          modifier = Modifier.size(iconSize),
        )
      }
    }
  }
}

@Composable
private fun EmptyAlbumStateView(
  folderName: String,
  onCreateSubfolder: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp)
      .testTag("gallery_empty_album_state"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier = Modifier
        .size(76.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Default.FolderOpen,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(40.dp),
      )
    }
    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = "$folderName is empty",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "No photos or albums found",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedButton(
      onClick = onCreateSubfolder,
      shape = RoundedCornerShape(12.dp),
    ) {
      Icon(
        imageVector = Icons.Default.CreateNewFolder,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text("Create Album")
    }
  }
}

@Composable
private fun GalleryPhotoViewerDialog(
  photos: List<GalleryPhoto>,
  initialPhoto: GalleryPhoto,
  onDismiss: () -> Unit,
  onEdit: (GalleryPhoto) -> Unit = {},
  onShare: (GalleryPhoto) -> Unit,
) {
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
  val coroutineScope = rememberCoroutineScope()
  val previewListState = rememberLazyListState()

  // Controls whether the bottom preview scroll bar is visible (dismissed/restored with a tap on the image)
  var showPreviewBar by rememberSaveable { mutableStateOf(true) }

  // Current active page's zoom scale (1f..5f)
  var currentZoomScale by remember { mutableFloatStateOf(MIN_PHOTO_ZOOM) }

  // Reset zoom when swiping to a different photo and keep the bottom preview scroll bar synced
  LaunchedEffect(pagerState.currentPage) {
    currentZoomScale = MIN_PHOTO_ZOOM
    if (viewerPhotos.isNotEmpty() && pagerState.currentPage in viewerPhotos.indices) {
      previewListState.animateScrollToItem(pagerState.currentPage)
    }
  }

  val currentPhoto = viewerPhotos.getOrElse(pagerState.currentPage) { initialPhoto }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false,
    ),
  ) {
    // Full black background with the image rendered directly on top
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .testTag("gallery_photo_viewer_dialog")
        .semantics {
          contentDescription = "Full black background photo viewer"
        },
    ) {
      // Swipeable horizontal pager over all photos in the album
      HorizontalPager(
        state = pagerState,
        userScrollEnabled = currentZoomScale <= 1.02f,
        modifier = Modifier
          .fillMaxSize()
          .testTag("photo_viewer_pager"),
      ) { pageIndex ->
        val pagePhoto = viewerPhotos[pageIndex]
        val isCurrentPage = pageIndex == pagerState.currentPage
        PhotoViewerPageContent(
          photo = pagePhoto,
          isCurrentPage = isCurrentPage,
          zoomScale = if (isCurrentPage) currentZoomScale else MIN_PHOTO_ZOOM,
          onZoomScaleChange = { updatedZoom ->
            if (isCurrentPage) {
              currentZoomScale = updatedZoom
            }
          },
          onTapImage = {
            showPreviewBar = !showPreviewBar
          },
        )
      }

      // Top overlay bar with Close, current photo title & index, zoom indicator, and Share
      Row(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .background(Color.Black.copy(alpha = 0.65f))
          .padding(horizontal = 12.dp, vertical = 10.dp)
          .testTag("photo_viewer_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
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

        TextButton(
          onClick = { onEdit(currentPhoto) },
          modifier = Modifier.testTag("photo_viewer_edit_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = Color.White,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Edit",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
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
      }

      // Bottom preview scroll bar with images in the same album, dismissible with a tap on the image
      AnimatedVisibility(
        visible = showPreviewBar,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = Modifier.align(Alignment.BottomCenter),
      ) {
        Surface(
          color = Color.Black.copy(alpha = 0.76f),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("photo_viewer_preview_bar"),
        ) {
          LazyRow(
            state = previewListState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("photo_viewer_preview_scroll_bar"),
          ) {
            itemsIndexed(
              items = viewerPhotos,
              key = { _, item -> "preview_${item.path}" },
            ) { index, itemPhoto ->
              val isSelected = index == pagerState.currentPage
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

@Composable
private fun PhotoViewerPageContent(
  photo: GalleryPhoto,
  isCurrentPage: Boolean,
  zoomScale: Float,
  onZoomScaleChange: (Float) -> Unit,
  onTapImage: () -> Unit,
) {
  var bitmap by remember(photo.path, photo.lastModifiedMs) {
    mutableStateOf(GalleryEngine.getCachedThumbnailBitmap(photo.file))
  }

  LaunchedEffect(photo.path, photo.lastModifiedMs) {
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

  var panOffsetX by remember(photo.path) { mutableFloatStateOf(0f) }
  var panOffsetY by remember(photo.path) { mutableFloatStateOf(0f) }

  LaunchedEffect(zoomScale, isCurrentPage) {
    if (zoomScale <= 1.02f || !isCurrentPage) {
      panOffsetX = 0f
      panOffsetY = 0f
    }
  }

  val currentZoomState by rememberUpdatedState(zoomScale)
  val currentOnZoomChange by rememberUpdatedState(onZoomScaleChange)
  val currentOnTapImage by rememberUpdatedState(onTapImage)

  val currentBitmap = bitmap

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
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

              if (liveZoom > 1.02f && maxPointers == 1) {
                val panChange = event.calculatePan()
                if (panChange != Offset.Zero) {
                  panOffsetX += panChange.x
                  panOffsetY += panChange.y
                  event.changes.forEach { it.consume() }
                }
              }
            }
          } while (event.changes.any { it.pressed })

          if (maxPointers == 1 && !movedBeyondSlop) {
            currentOnTapImage()
          }
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    if (currentBitmap != null) {
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
}

@Composable
private fun AddGalleryFolderDialog(
  parentDirectory: File,
  isRootLevel: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  var folderName by rememberSaveable { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }
  val validationError = remember(folderName, parentDirectory) {
    if (folderName.isBlank()) null
    else FileManagerEngine.validateFileName(folderName.trim(), if (isRootLevel) null else parentDirectory)
  }
  val canConfirm = folderName.trim().isNotEmpty() && validationError == null

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("gallery_add_folder_dialog"),
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
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = if (isRootLevel) "Add Album Folder" else "New Nested Album",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = if (isRootLevel) {
              "Load another folder into your gallery"
            } else {
              "Create a nested folder inside ${parentDirectory.name}"
            },
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
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp),
            )
            Column {
              Text(
                text = "Location",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = parentDirectory.name.ifBlank { parentDirectory.absolutePath },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }

        OutlinedTextField(
          value = folderName,
          onValueChange = { folderName = it },
          label = { Text("Folder name") },
          singleLine = true,
          isError = validationError != null,
          supportingText = validationError?.let { err -> { Text(err) } },
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
          },
          trailingIcon = {
            if (folderName.isNotEmpty()) {
              IconButton(onClick = { folderName = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              if (canConfirm) onConfirm(folderName.trim())
            }
          ),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("gallery_folder_name_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(folderName.trim()) },
        enabled = canConfirm,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("gallery_confirm_add_folder"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Create")
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

private fun sharePhotoFile(context: Context, file: File) {
  try {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = FileManagerEngine.getMimeType(file)
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
  } catch (_: Exception) {
    Toast.makeText(context, "Unable to share ${file.name}", Toast.LENGTH_SHORT).show()
  }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenTilePreview() {
  ToolboxTheme {
    GalleryScreen()
  }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenListPreview() {
  ToolboxTheme {
    GalleryScreen(initialViewMode = GalleryViewMode.LIST)
  }
}
