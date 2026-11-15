package com.example.toolbox.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AddAlbumTarget {
  ROOT_ALBUM,
  NESTED_ALBUM_HERE,
}

internal data class GalleryItemActionDispatcher(
  val onOpenAlbum: (GalleryAlbum) -> Unit = {},
  val onArchiveAlbum: (GalleryAlbum) -> Unit = {},
  val onOpenAlbumInFiles: (GalleryAlbum) -> Unit = {},
  val onShareAlbum: (GalleryAlbum) -> Unit = {},
  val onCutAlbum: (GalleryAlbum) -> Unit = {},
  val onCopyAlbum: (GalleryAlbum) -> Unit = {},
  val onRenameAlbum: (GalleryAlbum) -> Unit = {},
  val onAlbumInfo: (GalleryAlbum) -> Unit = {},
  val onDeleteAlbum: (GalleryAlbum) -> Unit = {},
  val onViewPhoto: (GalleryPhoto) -> Unit = {},
  val onEditPhoto: (GalleryPhoto) -> Unit = {},
  val onInspectPhotoColor: (GalleryPhoto) -> Unit = {},
  val onConvertPhoto: (GalleryPhoto) -> Unit = {},
  val onArchivePhoto: (GalleryPhoto) -> Unit = {},
  val onSavePhotoToNotes: (GalleryPhoto, GalleryPhotoIndexEntry?) -> Unit = { _, _ -> },
  val onOpenPhotoInFiles: (GalleryPhoto) -> Unit = {},
  val onOpenPhotoWith: (GalleryPhoto) -> Unit = {},
  val onSharePhoto: (GalleryPhoto) -> Unit = {},
  val onCutPhoto: (GalleryPhoto) -> Unit = {},
  val onCopyPhoto: (GalleryPhoto) -> Unit = {},
  val onDuplicatePhoto: (GalleryPhoto) -> Unit = {},
  val onRenamePhoto: (GalleryPhoto) -> Unit = {},
  val onPhotoInfo: (GalleryPhoto) -> Unit = {},
  val onDeletePhoto: (GalleryPhoto) -> Unit = {},
)

internal val LocalGalleryItemActions = compositionLocalOf { GalleryItemActionDispatcher() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
  modifier: Modifier = Modifier,
  rootDirectory: File? = null,
  initialFolders: List<File>? = null,
  initialAlbumDirectory: File? = null,
  initialPhotoFile: File? = null,
  initialAlbumsPerRow: Int? = null,
  initialViewMode: GalleryViewMode? = null,
  initialSortOrder: GallerySortOrder? = null,
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

  // Sort order: defaults to DATE_DESC (Newest first)
  var sortOrder by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_GALLERY_SORT_ORDER, null)
    mutableStateOf(initialSortOrder ?: GallerySortOrder.fromNameOrDefault(saved))
  }

  fun updateSortOrder(newOrder: GallerySortOrder) {
    sortOrder = newOrder
    prefs?.edit()?.putString(PREF_KEY_GALLERY_SORT_ORDER, newOrder.name)?.apply()
  }

  var showSortMenu by remember { mutableStateOf(false) }
  var isFabExpanded by rememberSaveable { mutableStateOf(false) }

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

  // Additional user-added folders beyond default Pictures and DCIM
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

  // Load un-nested root contents (Pictures and DCIM merged as if they were the same folder)
  val baseRootContents = remember(effectiveRootDir, initialFolders, additionalFolderPaths, refreshTrigger, isInspection) {
    if (isInspection && rootDirectory == null && initialFolders == null) {
      AlbumContents(
        directory = File("/storage/emulated/0/Pictures"),
        directories = listOf(File("/storage/emulated/0/Pictures"), File("/storage/emulated/0/DCIM")),
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
            directory = File("/storage/emulated/0/Pictures/Vacation"),
            name = "Vacation",
            path = "/storage/emulated/0/Pictures/Vacation",
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

  // Multi-selection state for albums and photos
  var isSelectionMode by rememberSaveable { mutableStateOf(false) }
  var selectedAlbumPaths by remember { mutableStateOf(setOf<String>()) }
  var selectedPhotoPaths by remember { mutableStateOf(setOf<String>()) }
  var showBulkInfoDialog by remember { mutableStateOf(false) }
  var showBulkDeleteDialog by remember { mutableStateOf(false) }
  var clipboardState by remember { mutableStateOf<GalleryClipboardState?>(null) }

  // Search & Smart Tag Filter state
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var selectedTagFilter by rememberSaveable { mutableStateOf<String?>(null) }

  // Background OCR, QR/Barcode & Automatic Tagging Index state
  val indexCacheFile = remember(context, isInspection) {
    if (isInspection) null else GalleryEngine.getDefaultIndexCacheFile(context)
  }
  var photoIndexMap by remember {
    mutableStateOf(GalleryEngine.loadIndexCacheFromDisk(indexCacheFile))
  }
  var isIndexingPhotos by remember { mutableStateOf(false) }
  var indexedProgressCount by remember { mutableIntStateOf(0) }
  var indexedTotalCount by remember { mutableIntStateOf(0) }

  fun exitSelectionMode() {
    isSelectionMode = false
    selectedAlbumPaths = emptySet()
    selectedPhotoPaths = emptySet()
  }

  fun closeSearch() {
    isSearchActive = false
    searchQuery = ""
    selectedTagFilter = null
  }

  fun toggleAlbumSelection(album: GalleryAlbum) {
    selectedAlbumPaths = if (album.path in selectedAlbumPaths) {
      selectedAlbumPaths - album.path
    } else {
      selectedAlbumPaths + album.path
    }
  }

  fun togglePhotoSelection(photo: GalleryPhoto) {
    selectedPhotoPaths = if (photo.path in selectedPhotoPaths) {
      selectedPhotoPaths - photo.path
    } else {
      selectedPhotoPaths + photo.path
    }
  }

  fun handleGalleryBack() {
    isFabExpanded = false
    exitSelectionMode()
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

  BackHandler(enabled = isFabExpanded || isSelectionMode || isSearchActive || albumStack.isNotEmpty() || onNavigateBack != null) {
    when {
      isFabExpanded -> {
        isFabExpanded = false
      }
      isSelectionMode -> {
        exitSelectionMode()
      }
      isSearchActive -> {
        closeSearch()
      }
      else -> {
        handleGalleryBack()
      }
    }
  }

  val currentAlbumContents = remember(currentAlbum, refreshTrigger) {
    currentAlbum?.let { album ->
      GalleryEngine.loadAlbumContents(album)
    }
  }

  val rawAlbums: List<GalleryAlbum> = if (currentAlbum == null) {
    rootContents.nestedAlbums
  } else {
    currentAlbumContents?.nestedAlbums.orEmpty()
  }

  val rawPhotos: List<GalleryPhoto> = if (currentAlbum == null) {
    rootContents.photos
  } else {
    currentAlbumContents?.photos.orEmpty()
  }

  // All photos in the gallery (for background indexing) and all photos in the current scope (root or current album subtree)
  val allGalleryPhotos: List<GalleryPhoto> = remember(rootContents, refreshTrigger) {
    GalleryEngine.collectAllGalleryPhotos(rootContents)
  }

  val allScopePhotos: List<GalleryPhoto> = remember(currentAlbum, rootContents, currentAlbumContents, allGalleryPhotos, refreshTrigger) {
    if (currentAlbum == null) {
      allGalleryPhotos
    } else {
      val subtreeFiles = GalleryEngine.collectMediaFiles(listOf(currentAlbum), emptyList())
      subtreeFiles.map { file ->
        GalleryPhoto(
          file = file,
          name = file.name,
          path = file.absolutePath,
          sizeBytes = file.length(),
          lastModifiedMs = file.lastModified(),
          extension = file.extension.lowercase(Locale.ROOT),
        )
      }
    }
  }

  // Background OCR, QR/barcode & ML Kit auto-tagging indexer
  LaunchedEffect(allGalleryPhotos, isInspection) {
    if (allGalleryPhotos.isEmpty()) {
      isIndexingPhotos = false
      indexedProgressCount = 0
      indexedTotalCount = 0
      return@LaunchedEffect
    }
    GalleryEngine.indexPhotosInBackground(
      photos = allGalleryPhotos,
      cacheFile = indexCacheFile,
      onProgress = { indexedCount, totalToIndex, snapshot ->
        photoIndexMap = snapshot
        if (totalToIndex > 0 && indexedCount < totalToIndex) {
          isIndexingPhotos = true
          indexedProgressCount = indexedCount
          indexedTotalCount = totalToIndex
        } else {
          isIndexingPhotos = false
          indexedProgressCount = totalToIndex
          indexedTotalCount = totalToIndex
        }
      },
    )
  }

  val availableAutoTags: List<Pair<String, Int>> = remember(allScopePhotos, photoIndexMap) {
    GalleryEngine.extractAvailableAutoTags(allScopePhotos, photoIndexMap)
  }

  val hasActiveSearchQueryOrTag = isSearchActive && (searchQuery.isNotBlank() || selectedTagFilter != null)

  val searchResult: GallerySearchResult = remember(
    rawAlbums,
    rawPhotos,
    allScopePhotos,
    photoIndexMap,
    isSearchActive,
    searchQuery,
    selectedTagFilter,
    sortOrder,
  ) {
    if (hasActiveSearchQueryOrTag) {
      GalleryEngine.searchGallery(
        albums = rawAlbums,
        directPhotos = rawPhotos,
        allScopePhotos = allScopePhotos,
        indexMap = photoIndexMap,
        searchQuery = searchQuery,
        selectedTag = selectedTagFilter,
        sortOrder = sortOrder,
      )
    } else {
      GallerySearchResult(
        matchingAlbums = GalleryEngine.sortAlbums(rawAlbums, sortOrder),
        matchingPhotos = GalleryEngine.sortPhotos(rawPhotos, sortOrder),
        matchSnippetsByPath = emptyMap(),
      )
    }
  }

  val activeAlbums: List<GalleryAlbum> = searchResult.matchingAlbums
  val activePhotos: List<GalleryPhoto> = searchResult.matchingPhotos
  val matchSnippetsByPath: Map<String, String> = searchResult.matchSnippetsByPath

  val selectedAlbums: List<GalleryAlbum> = remember(activeAlbums, selectedAlbumPaths) {
    activeAlbums.filter { it.path in selectedAlbumPaths }
  }

  val selectedPhotos: List<GalleryPhoto> = remember(activePhotos, selectedPhotoPaths) {
    activePhotos.filter { it.path in selectedPhotoPaths }
  }

  val totalSelectableCount = activeAlbums.size + activePhotos.size
  val selectedCount = selectedAlbums.size + selectedPhotos.size

  fun toggleSelectAll() {
    if (totalSelectableCount > 0 && selectedCount == totalSelectableCount) {
      selectedAlbumPaths = emptySet()
      selectedPhotoPaths = emptySet()
    } else {
      selectedAlbumPaths = activeAlbums.map { it.path }.toSet()
      selectedPhotoPaths = activePhotos.map { it.path }.toSet()
    }
  }

  // Defer album art pre-decoding to a background coroutine on Dispatchers.IO
  LaunchedEffect(activeAlbums, isInspection) {
    if (!isInspection && activeAlbums.isNotEmpty()) {
      GalleryEngine.prefetchAlbumArt(activeAlbums)
    }
  }

  var selectedPhotoForViewer by remember(initialPhotoFile?.absolutePath) {
    mutableStateOf(
      initialPhotoFile?.takeIf { it.exists() && it.isFile }?.let { file ->
        GalleryPhoto(
          file = file,
          name = file.name,
          path = file.absolutePath,
          sizeBytes = file.length(),
          lastModifiedMs = file.lastModified(),
          extension = file.extension.lowercase(Locale.ROOT),
        )
      }
    )
  }
  var addAlbumDialogTarget by remember { mutableStateOf<AddAlbumTarget?>(null) }
  var fileForImageEditor by remember { mutableStateOf<File?>(null) }
  var fileForVideoEditor by remember { mutableStateOf<File?>(null) }

  fun openMediaInEditor(photo: GalleryPhoto) {
    selectedPhotoForViewer = null
    exitSelectionMode()
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

  val crossToolHostState = rememberCrossToolFileHostState()
  if (crossToolHostState.hasActiveSubScreen) {
    crossToolHostState.RenderSubScreen(
      modifier = modifier,
      defaultDirectory = currentAlbum?.directory ?: effectiveRootDir,
      onReturnedFromSubScreen = { refreshTrigger++ },
    )
    return
  }

  val defaultNewAlbumDir = remember(effectiveRootDir, initialFolders) {
    initialFolders?.firstOrNull() ?: GalleryEngine.getDefaultNewAlbumFolder(effectiveRootDir)
  }

  val galleryItemActions = remember(context) {
    GalleryItemActionDispatcher(
      onOpenAlbum = { album ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        isFabExpanded = false
        albumStack.add(album)
      },
      onArchiveAlbum = { album ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        crossToolHostState.filesForArchiveCompression = listOf(album.directory)
      },
      onOpenAlbumInFiles = { album ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        crossToolHostState.openInFiles(album.directory)
      },
      onShareAlbum = { album ->
        val files = GalleryEngine.collectMediaFiles(listOf(album), emptyList())
        if (files.isNotEmpty()) {
          shareMediaFiles(context, files)
        } else {
          Toast.makeText(context, "No media files found in ${album.name}", Toast.LENGTH_SHORT).show()
        }
      },
      onCutAlbum = { album ->
        clipboardState = GalleryClipboardState(
          operation = GalleryClipboardOperation.CUT,
          albums = listOf(album),
          photos = emptyList(),
        )
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, "\"${album.name}\" cut", Toast.LENGTH_SHORT).show()
      },
      onCopyAlbum = { album ->
        clipboardState = GalleryClipboardState(
          operation = GalleryClipboardOperation.COPY,
          albums = listOf(album),
          photos = emptyList(),
        )
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, "\"${album.name}\" copied", Toast.LENGTH_SHORT).show()
      },
      onRenameAlbum = { album ->
        crossToolHostState.fileToRename = album.directory
      },
      onAlbumInfo = { album ->
        crossToolHostState.fileForDetails = album.directory
      },
      onDeleteAlbum = { album ->
        crossToolHostState.fileToDelete = album.directory
      },
      onViewPhoto = { photo ->
        selectedPhotoForViewer = photo
      },
      onEditPhoto = { photo ->
        openMediaInEditor(photo)
      },
      onInspectPhotoColor = { photo ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        crossToolHostState.inspectImageColor(photo.file)
      },
      onConvertPhoto = { photo ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        crossToolHostState.fileForConverter = photo.file
      },
      onArchivePhoto = { photo ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        crossToolHostState.filesForArchiveCompression = listOf(photo.file)
      },
      onSavePhotoToNotes = { photo, indexEntry ->
        val body = buildString {
          appendLine("File: ${photo.name}")
          appendLine("Size: ${photo.sizeFormatted} • ${photo.timestampFormatted}")
          if (indexEntry != null) {
            if (indexEntry.autoTags.isNotEmpty()) {
              appendLine("Tags: ${indexEntry.autoTags.joinToString(", ")}")
            }
            if (indexEntry.barcodeSummaries.isNotEmpty()) {
              appendLine("QR / Barcodes: ${indexEntry.barcodeSummaries.joinToString(", ")}")
            }
            if (indexEntry.ocrText.isNotBlank()) {
              appendLine()
              appendLine("Recognized Text:")
              append(indexEntry.ocrText.trim())
            }
          }
        }.trim()
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        CrossToolActions.saveTextToNotes(
          context = context,
          title = photo.name,
          content = body,
          label = "Gallery",
        )
      },
      onOpenPhotoInFiles = { photo ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        crossToolHostState.openInFiles(photo.file)
      },
      onOpenPhotoWith = { photo ->
        FileManagerEngine.openFile(context, photo.file, chooseApp = true)
      },
      onSharePhoto = { photo ->
        sharePhotoFile(context, photo.file)
      },
      onCutPhoto = { photo ->
        clipboardState = GalleryClipboardState(
          operation = GalleryClipboardOperation.CUT,
          albums = emptyList(),
          photos = listOf(photo),
        )
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, "\"${photo.name}\" cut", Toast.LENGTH_SHORT).show()
      },
      onCopyPhoto = { photo ->
        clipboardState = GalleryClipboardState(
          operation = GalleryClipboardOperation.COPY,
          albums = emptyList(),
          photos = listOf(photo),
        )
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, "\"${photo.name}\" copied", Toast.LENGTH_SHORT).show()
      },
      onDuplicatePhoto = { photo ->
        FileManagerEngine.duplicateFile(photo.file)
        refreshTrigger++
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, "Duplicated \"${photo.name}\"", Toast.LENGTH_SHORT).show()
      },
      onRenamePhoto = { photo ->
        crossToolHostState.fileToRename = photo.file
      },
      onPhotoInfo = { photo ->
        crossToolHostState.fileForDetails = photo.file
      },
      onDeletePhoto = { photo ->
        crossToolHostState.fileToDelete = photo.file
      },
    )
  }

  CompositionLocalProvider(LocalGalleryItemActions provides galleryItemActions) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("gallery_screen"),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
    ) {
      // Top Toolbar following DESIGN_LANGUAGE.md 5.1 & 5.5
      val indexingStatusSuffix = if (isIndexingPhotos && indexedTotalCount > 0) {
        " • Indexing $indexedProgressCount/$indexedTotalCount photos…"
      } else {
        ""
      }

      GalleryTopBar(
        title = currentAlbum?.name ?: "Gallery",
        subtext = (if (currentAlbum == null) {
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
        }) + indexingStatusSuffix,
        canNavigateBack = albumStack.isNotEmpty() || onNavigateBack != null,
        viewMode = viewMode,
        isSelectionMode = isSelectionMode,
        selectedCount = selectedCount,
        totalCount = totalSelectableCount,
        singleSelectedPhoto = if (selectedAlbums.isEmpty() && selectedPhotos.size == 1) selectedPhotos.first() else null,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        searchPlaceholder = if (currentAlbum != null) {
          "Search ${currentAlbum.name}, OCR text, QR, or tags..."
        } else {
          "Search photos, OCR text, QR codes, or tags..."
        },
        onSearchToggle = {
          isSearchActive = !isSearchActive
          if (!isSearchActive) {
            searchQuery = ""
            selectedTagFilter = null
          }
        },
        onCloseSearch = { closeSearch() },
        onSearchQueryChange = { searchQuery = it },
        onToggleSelectAll = { toggleSelectAll() },
        onCloseSelection = { exitSelectionMode() },
        onBulkShare = {
          val filesToShare = GalleryEngine.collectMediaFiles(selectedAlbums, selectedPhotos)
          if (filesToShare.isNotEmpty()) {
            shareMediaFiles(context, filesToShare)
          } else {
            Toast.makeText(context, "No media files found in selection", Toast.LENGTH_SHORT).show()
          }
        },
        onBulkInfo = {
          if (selectedCount > 0) {
            showBulkInfoDialog = true
          }
        },
        onBulkCut = {
          if (selectedCount > 0) {
            clipboardState = GalleryClipboardState(
              operation = GalleryClipboardOperation.CUT,
              albums = selectedAlbums,
              photos = selectedPhotos,
            )
            val count = selectedCount
            exitSelectionMode()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "$count ${if (count == 1) "item" else "items"} cut", Toast.LENGTH_SHORT).show()
          }
        },
        onBulkCopy = {
          if (selectedCount > 0) {
            clipboardState = GalleryClipboardState(
              operation = GalleryClipboardOperation.COPY,
              albums = selectedAlbums,
              photos = selectedPhotos,
            )
            val count = selectedCount
            exitSelectionMode()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "$count ${if (count == 1) "item" else "items"} copied", Toast.LENGTH_SHORT).show()
          }
        },
        onBulkDelete = {
          if (selectedCount > 0) {
            showBulkDeleteDialog = true
          }
        },
        onEditSinglePhoto = { photo ->
          openMediaInEditor(photo)
        },
        onBack = { handleGalleryBack() },
        onToggleViewMode = {
          val next = if (viewMode == GalleryViewMode.TILE) GalleryViewMode.LIST else GalleryViewMode.TILE
          updateViewMode(next)
        },
        onSelectViewMode = { updateViewMode(it) },
      )

      // Smart Tag Filter Chips Row (shown when searching)
      AnimatedVisibility(
        visible = isSearchActive && !isSelectionMode,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
      ) {
        GallerySearchTagFilterBar(
          availableTags = availableAutoTags,
          selectedTag = selectedTagFilter,
          isIndexing = isIndexingPhotos,
          indexedCount = indexedProgressCount,
          totalCount = indexedTotalCount,
          onSelectTag = { tag ->
            selectedTagFilter = if (selectedTagFilter.equals(tag, ignoreCase = true)) null else tag
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          },
        )
      }

      // Pill & Filter Controls Bar underneath the toolbar
      GalleryNavigationAndDensityBar(
        albumStack = albumStack.toList(),
        viewMode = viewMode,
        albumsPerRow = albumsPerRow,
        sortOrder = sortOrder,
        showSortMenu = showSortMenu,
        onNavigateToRoot = {
          isFabExpanded = false
          exitSelectionMode()
          navigatedToRootFromInitial = true
          albumStack.clear()
        },
        onNavigateToIndex = { index ->
          isFabExpanded = false
          exitSelectionMode()
          while (albumStack.size > index + 1) {
            albumStack.removeAt(albumStack.lastIndex)
          }
        },
        onAlbumsPerRowChange = { updateAlbumsPerRow(it) },
        onOpenSortMenu = { showSortMenu = true },
        onDismissSortMenu = { showSortMenu = false },
        onSelectSortOrder = {
          updateSortOrder(it)
          showSortMenu = false
        },
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
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .testTag("gallery_content_container")
          .pinchToChangeColumns(
            enabled = viewMode == GalleryViewMode.TILE,
            columnsPerRow = albumsPerRow,
            minColumns = MIN_ALBUMS_PER_ROW,
            maxColumns = MAX_ALBUMS_PER_ROW,
            onColumnsChange = { updateAlbumsPerRow(it) },
          ),
      ) {
        if (activeAlbums.isEmpty() && activePhotos.isEmpty()) {
          if (hasActiveSearchQueryOrTag) {
            GallerySearchEmptyStateView(
              searchQuery = searchQuery,
              selectedTag = selectedTagFilter,
              onClearSearch = {
                searchQuery = ""
                selectedTagFilter = null
              },
            )
          } else {
            EmptyAlbumStateView(
              folderName = currentAlbum?.name ?: "Gallery",
              onCreateSubfolder = {
                addAlbumDialogTarget = if (currentAlbum == null) {
                  AddAlbumTarget.ROOT_ALBUM
                } else {
                  AddAlbumTarget.NESTED_ALBUM_HERE
                }
              },
            )
          }
        } else if (viewMode == GalleryViewMode.TILE) {
          GalleryTileGridView(
            albums = activeAlbums,
            photos = activePhotos,
            albumsPerRow = albumsPerRow,
            isInsideAlbum = currentAlbum != null,
            isSelectionMode = isSelectionMode,
            selectedAlbumPaths = selectedAlbumPaths,
            selectedPhotoPaths = selectedPhotoPaths,
            photoIndexMap = photoIndexMap,
            matchSnippetsByPath = matchSnippetsByPath,
            onToggleAlbumSelect = { album ->
              toggleAlbumSelection(album)
            },
            onTogglePhotoSelect = { photo ->
              togglePhotoSelection(photo)
            },
            onAlbumLongClick = { album ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              isFabExpanded = false
              if (!isSelectionMode) {
                isSelectionMode = true
                selectedAlbumPaths = setOf(album.path)
                selectedPhotoPaths = emptySet()
              } else {
                toggleAlbumSelection(album)
              }
            },
            onPhotoLongClick = { photo ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              isFabExpanded = false
              if (!isSelectionMode) {
                isSelectionMode = true
                selectedPhotoPaths = setOf(photo.path)
                selectedAlbumPaths = emptySet()
              } else {
                togglePhotoSelection(photo)
              }
            },
            onAlbumClick = { album ->
              if (isSelectionMode) {
                toggleAlbumSelection(album)
              } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isFabExpanded = false
                albumStack.add(album)
              }
            },
            onPhotoClick = { photo ->
              if (isSelectionMode) {
                togglePhotoSelection(photo)
              } else {
                selectedPhotoForViewer = photo
              }
            },
            onPhotoEdit = { photo ->
              openMediaInEditor(photo)
            },
          )
        } else {
          GalleryListView(
            albums = activeAlbums,
            photos = activePhotos,
            isSelectionMode = isSelectionMode,
            selectedAlbumPaths = selectedAlbumPaths,
            selectedPhotoPaths = selectedPhotoPaths,
            photoIndexMap = photoIndexMap,
            matchSnippetsByPath = matchSnippetsByPath,
            onToggleAlbumSelect = { album ->
              toggleAlbumSelection(album)
            },
            onTogglePhotoSelect = { photo ->
              togglePhotoSelection(photo)
            },
            onAlbumLongClick = { album ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              isFabExpanded = false
              if (!isSelectionMode) {
                isSelectionMode = true
                selectedAlbumPaths = setOf(album.path)
                selectedPhotoPaths = emptySet()
              } else {
                toggleAlbumSelection(album)
              }
            },
            onPhotoLongClick = { photo ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              isFabExpanded = false
              if (!isSelectionMode) {
                isSelectionMode = true
                selectedPhotoPaths = setOf(photo.path)
                selectedAlbumPaths = emptySet()
              } else {
                togglePhotoSelection(photo)
              }
            },
            onAlbumClick = { album ->
              if (isSelectionMode) {
                toggleAlbumSelection(album)
              } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isFabExpanded = false
                albumStack.add(album)
              }
            },
            onPhotoClick = { photo ->
              if (isSelectionMode) {
                togglePhotoSelection(photo)
              } else {
                selectedPhotoForViewer = photo
              }
            },
            onPhotoEdit = { photo ->
              openMediaInEditor(photo)
            },
          )
        }
      }
    }

    // Scrim overlay when speed dial FAB is expanded
    AnimatedVisibility(
      visible = isFabExpanded && !isSelectionMode,
      enter = fadeIn(tween(150)),
      exit = fadeOut(tween(150)),
      modifier = Modifier.fillMaxSize(),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.32f))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
          ) {
            isFabExpanded = false
          },
      )
    }

    // Speed dial FAB menu (hidden during multi-select, matches PlantScheduleScreen and RecipeBookScreen)
    if (!isSelectionMode) {
      Column(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Mini FABs sliding up from underneath
        AnimatedVisibility(
          visible = isFabExpanded,
          enter = fadeIn(tween(160)) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMediumLow,
            ),
          ) + expandVertically(),
          exit = fadeOut(tween(120)) + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(140),
          ) + shrinkVertically(),
        ) {
          Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            GalleryMiniFab(
              label = "Select",
              icon = Icons.Default.Check,
              testTag = "gallery_select_fab_button",
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isFabExpanded = false
                isSelectionMode = true
                selectedAlbumPaths = emptySet()
                selectedPhotoPaths = emptySet()
              },
            )
            GalleryMiniFab(
              label = "New album",
              icon = Icons.Default.CreateNewFolder,
              testTag = "gallery_new_album_fab_button",
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isFabExpanded = false
                addAlbumDialogTarget = AddAlbumTarget.ROOT_ALBUM
              },
            )
            GalleryMiniFab(
              label = "New album here",
              icon = Icons.Default.FolderOpen,
              testTag = "gallery_new_album_here_fab_button",
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isFabExpanded = false
                addAlbumDialogTarget = AddAlbumTarget.NESTED_ALBUM_HERE
              },
            )
          }
        }

        val addIconRotation by animateFloatAsState(
          targetValue = if (isFabExpanded) 135f else 0f,
          label = "gallery_fab_rotation",
        )

        FloatingActionButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isFabExpanded = !isFabExpanded
          },
          shape = CircleShape,
          containerColor = if (isFabExpanded) {
            MaterialTheme.colorScheme.surfaceContainerHighest
          } else {
            MaterialTheme.colorScheme.primaryContainer
          },
          contentColor = if (isFabExpanded) {
            MaterialTheme.colorScheme.onSurfaceVariant
          } else {
            MaterialTheme.colorScheme.onPrimaryContainer
          },
          modifier = Modifier.testTag("gallery_fab_menu_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = if (isFabExpanded) "Close gallery actions" else "Gallery actions",
            modifier = Modifier.rotate(addIconRotation),
          )
        }
      }
    }

    // Floating Paste Bar (shown when albums/photos are in clipboard to copy/cut)
    clipboardState?.let { clip ->
      val pasteTargetDir = currentAlbum?.directory ?: defaultNewAlbumDir
      Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = if (isSelectionMode) 16.dp else 76.dp, start = 16.dp, end = 16.dp)
          .testTag("gallery_paste_bar"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector = if (clip.operation == GalleryClipboardOperation.CUT) {
              Icons.Default.ContentCut
            } else {
              Icons.Default.ContentCopy
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "${clip.totalCount} ${if (clip.operation == GalleryClipboardOperation.CUT) "cut" else "copied"}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
          )
          Button(
            onClick = {
              val count = if (clip.operation == GalleryClipboardOperation.CUT) {
                GalleryEngine.moveSelectedItems(clip.albums, clip.photos, pasteTargetDir)
              } else {
                GalleryEngine.copySelectedItems(clip.albums, clip.photos, pasteTargetDir)
              }
              clipboardState = null
              refreshTrigger++
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              Toast.makeText(context, "Pasted $count ${if (count == 1) "item" else "items"}", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("action_paste_here"),
          ) {
            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Paste here")
          }
          IconButton(
            onClick = { clipboardState = null },
            modifier = Modifier
              .size(32.dp)
              .testTag("action_cancel_paste"),
          ) {
            Icon(Icons.Default.Close, contentDescription = "Cancel paste")
          }
        }
      }
    }
  }

  // Full-screen photo preview dialog
  selectedPhotoForViewer?.let { photo ->
    GalleryPhotoViewerDialog(
      photos = activePhotos,
      initialPhoto = photo,
      photoIndexMap = photoIndexMap,
      onPhotoIndexed = { entry ->
        photoIndexMap = photoIndexMap + (entry.path to entry)
        GalleryEngine.saveIndexCacheToDisk(indexCacheFile, photoIndexMap)
      },
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
  addAlbumDialogTarget?.let { target ->
    val isRootTarget = target == AddAlbumTarget.ROOT_ALBUM || currentAlbum == null
    val targetParentDir = if (isRootTarget) {
      defaultNewAlbumDir
    } else {
      currentAlbum?.directory ?: defaultNewAlbumDir
    }
    AddGalleryFolderDialog(
      parentDirectory = targetParentDir,
      isRootLevel = isRootTarget,
      onDismiss = { addAlbumDialogTarget = null },
      onConfirm = { folderName ->
        val newDir = File(targetParentDir, folderName.trim())
        newDir.mkdirs()
        refreshTrigger++
        addAlbumDialogTarget = null
      },
    )
  }

  if (showBulkInfoDialog) {
    GalleryBulkInfoDialog(
      albums = selectedAlbums,
      photos = selectedPhotos,
      onDismiss = { showBulkInfoDialog = false },
    )
  }

  if (showBulkDeleteDialog) {
    GalleryBulkDeleteConfirmDialog(
      albums = selectedAlbums,
      photos = selectedPhotos,
      onDismiss = { showBulkDeleteDialog = false },
      onConfirm = {
        val deletedCount = GalleryEngine.deleteSelectedItems(selectedAlbums, selectedPhotos)
        showBulkDeleteDialog = false
        exitSelectionMode()
        refreshTrigger++
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(
          context,
          "Deleted $deletedCount ${if (deletedCount == 1) "item" else "items"}",
          Toast.LENGTH_SHORT,
        ).show()
      },
    )
  }

  crossToolHostState.RenderFileDialogs(
    onFileMutated = { refreshTrigger++ },
  )
  }
}

@Composable
private fun GalleryMiniFab(
  label: String,
  icon: ImageVector,
  testTag: String,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = null,
      onClick = onClick,
    ),
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 2.dp,
      shadowElevation = 2.dp,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      )
    }
    SmallFloatingActionButton(
      onClick = onClick,
      shape = CircleShape,
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
      modifier = Modifier.testTag(testTag),
    ) {
      Icon(icon, contentDescription = label)
    }
  }
}

@Composable
private fun GalleryTopBar(
  title: String,
  subtext: String,
  canNavigateBack: Boolean,
  viewMode: GalleryViewMode,
  isSelectionMode: Boolean = false,
  selectedCount: Int = 0,
  totalCount: Int = 0,
  singleSelectedPhoto: GalleryPhoto? = null,
  isSearchActive: Boolean = false,
  searchQuery: String = "",
  searchPlaceholder: String = "Search photos, OCR text, QR codes, or tags...",
  onSearchToggle: () -> Unit = {},
  onCloseSearch: () -> Unit = {},
  onSearchQueryChange: (String) -> Unit = {},
  onToggleSelectAll: () -> Unit = {},
  onCloseSelection: () -> Unit = {},
  onBulkShare: () -> Unit = {},
  onBulkInfo: () -> Unit = {},
  onBulkCut: () -> Unit = {},
  onBulkCopy: () -> Unit = {},
  onBulkDelete: () -> Unit = {},
  onEditSinglePhoto: (GalleryPhoto) -> Unit = {},
  onBack: () -> Unit,
  onToggleViewMode: () -> Unit,
  onSelectViewMode: (GalleryViewMode) -> Unit,
) {
  val focusManager = LocalFocusManager.current

  ToolboxTopBar(
    title = title,
    subtext = subtext,
    icon = Icons.Default.PhotoLibrary,
    onNavigateBack = if (canNavigateBack) onBack else null,
    backContentDescription = "Back",
    backButtonTestTag = "gallery_back_button",
    titleTestTag = "gallery_header_title",
    subtextTestTag = "gallery_header_subtitle",
    searchState = ToolboxTopBarSearchState(
      isActive = isSearchActive,
      query = searchQuery,
      onQueryChange = onSearchQueryChange,
      onOpenSearch = onSearchToggle,
      onCloseSearch = {
        focusManager.clearFocus()
        onCloseSearch()
      },
      placeholder = searchPlaceholder,
      searchContentDescription = "Search gallery, OCR text, QR codes, and tags",
      closeContentDescription = "Close search",
      clearContentDescription = "Clear search query",
      toggleButtonTestTag = "gallery_search_toggle",
      searchBarTestTag = "gallery_toolbar_search_bar",
      closeButtonTestTag = "gallery_search_close",
      inputTestTag = "gallery_search_input",
      clearButtonTestTag = "gallery_search_clear",
      onSearchImeAction = { focusManager.clearFocus() },
    ),
    selectionState = if (isSelectionMode) {
      ToolboxTopBarSelectionState(
        selectedCount = selectedCount,
        totalCount = totalCount,
        onToggleSelectAll = onToggleSelectAll,
        onCloseSelection = onCloseSelection,
        countTextTestTag = "gallery_selection_count_label",
        actions = {
          var showOverflowMenu by remember { mutableStateOf(false) }
          val hasSelection = selectedCount > 0

          IconButton(
            onClick = onBulkShare,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_share"),
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share",
              tint = if (hasSelection) {
                MaterialTheme.colorScheme.onSurface
              } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
              },
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = onBulkInfo,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_info"),
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Info",
              tint = if (hasSelection) {
                MaterialTheme.colorScheme.onSurface
              } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
              },
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = onBulkCut,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_cut"),
          ) {
            Icon(
              imageVector = Icons.Default.ContentCut,
              contentDescription = "Cut",
              tint = if (hasSelection) {
                MaterialTheme.colorScheme.onSurface
              } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
              },
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = onBulkCopy,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_copy"),
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy",
              tint = if (hasSelection) {
                MaterialTheme.colorScheme.onSurface
              } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
              },
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = onBulkDelete,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_delete"),
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = if (hasSelection) {
                MaterialTheme.colorScheme.error
              } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
              },
              modifier = Modifier.size(20.dp),
            )
          }

          Box {
            IconButton(
              onClick = { showOverflowMenu = true },
              modifier = Modifier
                .size(36.dp)
                .testTag("action_selection_overflow"),
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More actions",
                modifier = Modifier.size(20.dp),
              )
            }

            DropdownMenu(
              expanded = showOverflowMenu,
              onDismissRequest = { showOverflowMenu = false },
            ) {
              DropdownMenuItem(
                text = { Text("Share") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkShare()
                },
                modifier = Modifier.testTag("action_selection_share"),
              )
              DropdownMenuItem(
                text = { Text("Info") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkInfo()
                },
                modifier = Modifier.testTag("action_selection_info"),
              )
              DropdownMenuItem(
                text = { Text("Cut") },
                leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkCut()
                },
                modifier = Modifier.testTag("action_selection_cut"),
              )
              DropdownMenuItem(
                text = { Text("Copy") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkCopy()
                },
                modifier = Modifier.testTag("action_selection_copy"),
              )
              if (singleSelectedPhoto != null) {
                DropdownMenuItem(
                  text = { Text("Edit") },
                  leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onEditSinglePhoto(singleSelectedPhoto)
                  },
                  modifier = Modifier.testTag("action_selection_edit"),
                )
              }
              DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                  )
                },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkDelete()
                },
                modifier = Modifier.testTag("action_selection_delete"),
              )
              HorizontalDivider()
              DropdownMenuItem(
                text = {
                  Text(if (selectedCount == totalCount && totalCount > 0) "Deselect all" else "Select all")
                },
                leadingIcon = { Icon(Icons.Default.SelectAll, contentDescription = null) },
                onClick = {
                  showOverflowMenu = false
                  onToggleSelectAll()
                },
                modifier = Modifier.testTag("action_selection_select_all"),
              )
              DropdownMenuItem(
                text = { Text("Exit selection") },
                leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                onClick = {
                  showOverflowMenu = false
                  onCloseSelection()
                },
                modifier = Modifier.testTag("action_selection_exit"),
              )
            }
          }
        },
      )
    } else {
      null
    },
    actions = {
      ViewModeSegmentedToggle(
        isListView = viewMode == GalleryViewMode.LIST,
        onSelectListView = { onSelectViewMode(GalleryViewMode.LIST) },
        onSelectGridView = { onSelectViewMode(GalleryViewMode.TILE) },
        onToggle = onToggleViewMode,
        toggleTestTag = "gallery_view_mode_toggle",
        listTestTag = "gallery_list_view_button",
        gridTestTag = "gallery_tile_view_button",
        listContentDescription = "List view",
        gridContentDescription = "Tile view",
      )
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryNavigationAndDensityBar(
  albumStack: List<GalleryAlbum>,
  viewMode: GalleryViewMode,
  albumsPerRow: Int,
  sortOrder: GallerySortOrder,
  showSortMenu: Boolean,
  onNavigateToRoot: () -> Unit,
  onNavigateToIndex: (Int) -> Unit,
  onAlbumsPerRowChange: (Int) -> Unit,
  onOpenSortMenu: () -> Unit,
  onDismissSortMenu: () -> Unit,
  onSelectSortOrder: (GallerySortOrder) -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 46.dp)
        .testTag("gallery_pill_bar"),
      contentAlignment = Alignment.CenterStart,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .fadingPillRowUnderFloatingControls(hasColumnsButton = viewMode == GalleryViewMode.TILE)
          .horizontalScroll(rememberScrollState())
          .padding(
            start = 12.dp,
            end = if (viewMode == GalleryViewMode.TILE) 96.dp else 56.dp,
            top = 6.dp,
            bottom = 6.dp,
          ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Breadcrumb path pills (always shows root "Albums" pill, plus nested folders when inside an album)
        Row(
          modifier = Modifier.testTag("gallery_breadcrumbs"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          val isAtRoot = albumStack.isEmpty()
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isAtRoot) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
            },
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable(onClick = onNavigateToRoot)
              .testTag("gallery_breadcrumb_root"),
          ) {
            Text(
              text = "Albums",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isAtRoot) FontWeight.Bold else FontWeight.Medium,
              color = if (isAtRoot) {
                MaterialTheme.colorScheme.onPrimaryContainer
              } else {
                MaterialTheme.colorScheme.onSecondaryContainer
              },
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
              shape = RoundedCornerShape(20.dp),
              color = if (isCurrent) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              },
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
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
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
          }
        }
      }

      // Pinned floating [# per row] and [Sort/Filter] buttons on the right edge of the pill bar
      Row(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .pillBarFloatingControlsBackdrop(backgroundColor = MaterialTheme.colorScheme.surface)
          .padding(end = 12.dp)
          .zIndex(10f),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (viewMode == GalleryViewMode.TILE) {
          ToolboxFloatingColumnsButton(
            columnsPerRow = albumsPerRow,
            minColumns = MIN_ALBUMS_PER_ROW,
            maxColumns = MAX_ALBUMS_PER_ROW,
            onColumnsChange = onAlbumsPerRowChange,
            buttonTestTag = "gallery_density_indicator",
            countTestTag = "gallery_albums_per_row_count",
            contentDescription = "$albumsPerRow albums per row",
          )
        }

        Box {
          Surface(
            onClick = onOpenSortMenu,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
              .size(34.dp)
              .testTag("gallery_sort_button"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort gallery",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
            }
          }

          DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = onDismissSortMenu,
            modifier = Modifier.testTag("gallery_sort_menu"),
          ) {
            GallerySortOrder.entries.forEach { option ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = option.displayName,
                    fontWeight = if (option == sortOrder) FontWeight.Bold else FontWeight.Normal,
                  )
                },
                leadingIcon = {
                  if (option == sortOrder) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                    )
                  }
                },
                onClick = { onSelectSortOrder(option) },
                modifier = Modifier.testTag("gallery_sort_option_${option.name.lowercase()}"),
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
          text = "Grant access to view all photos in Pictures and DCIM",
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
private fun GallerySearchTagFilterBar(
  availableTags: List<Pair<String, Int>>,
  selectedTag: String?,
  isIndexing: Boolean,
  indexedCount: Int,
  totalCount: Int,
  onSelectTag: (String) -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("gallery_search_tag_filter_bar"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .testTag("gallery_search_tag_chips_row"),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = "Smart Tags:",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      if (availableTags.isEmpty()) {
        Text(
          text = if (isIndexing && totalCount > 0) {
            "Scanning photos ($indexedCount/$totalCount)…"
          } else {
            "No tags detected yet"
          },
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.testTag("gallery_search_tags_empty_label"),
        )
      } else {
        availableTags.forEach { (tag, count) ->
          val isSelected = selectedTag != null && selectedTag.equals(tag, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = { onSelectTag(tag) },
            label = {
              Text(
                text = "$tag ($count)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            },
            leadingIcon = {
              Icon(
                imageVector = when {
                  tag.equals("QR Code", ignoreCase = true) || tag.equals("Barcode", ignoreCase = true) ->
                    Icons.Default.QrCode2
                  tag.equals("Text", ignoreCase = true) ||
                    tag.equals("Document", ignoreCase = true) ||
                    tag.equals("Receipt", ignoreCase = true) ||
                    tag.equals("Notes", ignoreCase = true) ->
                    Icons.Default.DocumentScanner
                  else -> Icons.AutoMirrored.Filled.Label
                },
                contentDescription = null,
                modifier = Modifier.size(15.dp),
              )
            },
            shape = CircleShape,
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            modifier = Modifier.testTag("gallery_tag_filter_$tag"),
          )
        }
      }
    }
  }
}

@Composable
private fun GallerySearchEmptyStateView(
  searchQuery: String,
  selectedTag: String?,
  onClearSearch: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp)
      .testTag("gallery_search_empty_state"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(36.dp),
      )
    }
    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = "No matching photos or albums",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = buildString {
        append("Nothing matched ")
        if (searchQuery.isNotBlank()) {
          append("\"${searchQuery.trim()}\"")
        }
        if (selectedTag != null) {
          if (searchQuery.isNotBlank()) append(" with ")
          append("tag \"$selectedTag\"")
        }
        append(" across filenames, OCR text, QR codes, or smart tags.")
      },
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedButton(
      onClick = onClearSearch,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.testTag("gallery_clear_search_filter_button"),
    ) {
      Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Clear filter")
    }
  }
}

@Composable
private fun GalleryTileGridView(
  albums: List<GalleryAlbum>,
  photos: List<GalleryPhoto>,
  albumsPerRow: Int,
  isInsideAlbum: Boolean = false,
  isSelectionMode: Boolean = false,
  selectedAlbumPaths: Set<String> = emptySet(),
  selectedPhotoPaths: Set<String> = emptySet(),
  photoIndexMap: Map<String, GalleryPhotoIndexEntry> = emptyMap(),
  matchSnippetsByPath: Map<String, String> = emptyMap(),
  onToggleAlbumSelect: (GalleryAlbum) -> Unit = {},
  onTogglePhotoSelect: (GalleryPhoto) -> Unit = {},
  onAlbumLongClick: (GalleryAlbum) -> Unit = {},
  onPhotoLongClick: (GalleryPhoto) -> Unit = {},
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
    contentPadding = PaddingValues(if (isInsideAlbum) 2.dp else 10.dp),
    horizontalArrangement = Arrangement.spacedBy(
      if (isInsideAlbum) 2.dp else if (isCompactTile) 6.dp else 8.dp
    ),
    verticalArrangement = Arrangement.spacedBy(
      if (isInsideAlbum) 2.dp else if (isCompactTile) 8.dp else 10.dp
    ),
  ) {
    items(
      items = albums,
      key = { "album_${it.path}" },
    ) { album ->
      AlbumTileCard(
        album = album,
        isCompact = isCompactTile,
        isSelectionMode = isSelectionMode,
        isSelected = album.path in selectedAlbumPaths,
        onToggleSelect = { onToggleAlbumSelect(album) },
        onLongClick = { onAlbumLongClick(album) },
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
        isSelectionMode = isSelectionMode,
        isSelected = photo.path in selectedPhotoPaths,
        indexEntry = photoIndexMap[photo.path],
        matchSnippet = matchSnippetsByPath[photo.path],
        onToggleSelect = { onTogglePhotoSelect(photo) },
        onLongClick = { onPhotoLongClick(photo) },
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumTileCard(
  album: GalleryAlbum,
  isCompact: Boolean,
  isSelectionMode: Boolean = false,
  isSelected: Boolean = false,
  onToggleSelect: () -> Unit = {},
  onLongClick: () -> Unit = {},
  onClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      )
      .padding(2.dp)
      .testTag("album_tile_item_${album.name}"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Tile with most recent photo from the album
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (isSelected) {
          MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
      ),
      border = if (isSelected) {
        BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
      } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      },
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

        if (isSelected) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
          )
        }

        if (isSelectionMode) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(6.dp)
              .size(28.dp),
          ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
              Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                  checkedColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                  .size(24.dp)
                  .testTag("album_tile_checkbox_${album.name}"),
              )
            }
          }
        } else {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(4.dp)
              .size(26.dp),
          ) {
            GalleryAlbumContextMenuButton(
              album = album,
              onOpen = onClick,
            )
          }
        }

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
      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoTileCard(
  photo: GalleryPhoto,
  isCompact: Boolean,
  isSelectionMode: Boolean = false,
  isSelected: Boolean = false,
  indexEntry: GalleryPhotoIndexEntry? = null,
  matchSnippet: String? = null,
  onToggleSelect: () -> Unit = {},
  onLongClick: () -> Unit = {},
  onClick: () -> Unit,
  onEdit: () -> Unit = {},
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1f)
      .clip(RoundedCornerShape(4.dp))
      .then(
        if (isSelected) {
          Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
        } else {
          Modifier
        }
      )
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      )
      .testTag("photo_tile_item_${photo.name}"),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .testTag("photo_tile_${photo.name}"),
    ) {
      PhotoThumbnailBox(
        photoFile = photo.file,
        contentDescription = photo.name,
        iconSize = if (isCompact) 22.dp else 32.dp,
        modifier = Modifier.fillMaxSize(),
      )

      if (isSelected) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
        )
      }

      if (isSelectionMode) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(4.dp)
            .size(26.dp),
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Checkbox(
              checked = isSelected,
              onCheckedChange = { onToggleSelect() },
              colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
              ),
              modifier = Modifier
                .size(22.dp)
                .testTag("photo_tile_checkbox_${photo.name}"),
            )
          }
        }
      } else {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(4.dp)
            .size(26.dp),
        ) {
          GalleryPhotoContextMenuButton(
            photo = photo,
            indexEntry = indexEntry,
            onView = onClick,
            onEdit = onEdit,
          )
        }
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

      // Match snippet or OCR / QR badge on bottom of tile
      if (!matchSnippet.isNullOrBlank()) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(4.dp)
            .testTag("photo_tile_match_snippet_${photo.name}"),
        ) {
          Text(
            text = matchSnippet,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
          )
        }
      } else if (!isCompact && indexEntry != null && (indexEntry.hasOcrText || indexEntry.hasBarcodes)) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(4.dp)
            .testTag("photo_tile_recognition_badge_${photo.name}"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (indexEntry.hasBarcodes) {
              Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = "Contains QR or barcode",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(11.dp),
              )
            }
            if (indexEntry.hasOcrText) {
              Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = "Contains recognized text",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(11.dp),
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Secondary view: List View with the album photo as an icon on the left and the folder name label to the right.
 */
@Composable
private fun GalleryListView(
  albums: List<GalleryAlbum>,
  photos: List<GalleryPhoto>,
  isSelectionMode: Boolean = false,
  selectedAlbumPaths: Set<String> = emptySet(),
  selectedPhotoPaths: Set<String> = emptySet(),
  photoIndexMap: Map<String, GalleryPhotoIndexEntry> = emptyMap(),
  matchSnippetsByPath: Map<String, String> = emptyMap(),
  onToggleAlbumSelect: (GalleryAlbum) -> Unit = {},
  onTogglePhotoSelect: (GalleryPhoto) -> Unit = {},
  onAlbumLongClick: (GalleryAlbum) -> Unit = {},
  onPhotoLongClick: (GalleryPhoto) -> Unit = {},
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
        isSelectionMode = isSelectionMode,
        isSelected = album.path in selectedAlbumPaths,
        onToggleSelect = { onToggleAlbumSelect(album) },
        onLongClick = { onAlbumLongClick(album) },
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
        isSelectionMode = isSelectionMode,
        isSelected = photo.path in selectedPhotoPaths,
        indexEntry = photoIndexMap[photo.path],
        matchSnippet = matchSnippetsByPath[photo.path],
        onToggleSelect = { onTogglePhotoSelect(photo) },
        onLongClick = { onPhotoLongClick(photo) },
        onClick = { onPhotoClick(photo) },
        onEdit = { onPhotoEdit(photo) },
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumListRow(
  album: GalleryAlbum,
  isSelectionMode: Boolean = false,
  isSelected: Boolean = false,
  onToggleSelect: () -> Unit = {},
  onLongClick: () -> Unit = {},
  onClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = if (isSelected) {
      BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
      BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    },
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      )
      .testTag("album_list_item_${album.name}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (isSelectionMode) {
        Box(
          modifier = Modifier.size(36.dp),
          contentAlignment = Alignment.Center,
        ) {
          Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelect() },
            colors = CheckboxDefaults.colors(
              checkedColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.testTag("album_list_checkbox_${album.name}"),
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
      }

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

      if (!isSelectionMode) {
        GalleryAlbumContextMenuButton(
          album = album,
          onOpen = onClick,
        )
        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoListRow(
  photo: GalleryPhoto,
  isSelectionMode: Boolean = false,
  isSelected: Boolean = false,
  indexEntry: GalleryPhotoIndexEntry? = null,
  matchSnippet: String? = null,
  onToggleSelect: () -> Unit = {},
  onLongClick: () -> Unit = {},
  onClick: () -> Unit,
  onEdit: () -> Unit = {},
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = if (isSelected) {
      BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
      BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    },
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      )
      .testTag("photo_list_item_${photo.name}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (isSelectionMode) {
        Box(
          modifier = Modifier.size(36.dp),
          contentAlignment = Alignment.Center,
        ) {
          Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelect() },
            colors = CheckboxDefaults.colors(
              checkedColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.testTag("photo_list_checkbox_${photo.name}"),
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
      }

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
        if (!matchSnippet.isNullOrBlank()) {
          Text(
            text = matchSnippet,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("photo_list_match_snippet_${photo.name}"),
          )
        } else if (indexEntry != null && indexEntry.autoTags.isNotEmpty()) {
          Text(
            text = indexEntry.autoTags.joinToString(" • "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("photo_list_tags_${photo.name}"),
          )
        }
      }

      if (!isSelectionMode) {
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
        GalleryPhotoContextMenuButton(
          photo = photo,
          indexEntry = indexEntry,
          onView = onClick,
          onEdit = onEdit,
        )
      }
    }
  }
}

@Composable
private fun GalleryAlbumContextMenuButton(
  album: GalleryAlbum,
  onOpen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val actions = LocalGalleryItemActions.current
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    IconButton(
      onClick = { expanded = true },
      modifier = Modifier
        .size(32.dp)
        .testTag("album_more_${album.name}"),
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "More options for ${album.name}",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(18.dp),
      )
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.testTag("album_menu_${album.name}"),
    ) {
      DropdownMenuItem(
        text = { Text("Open Album") },
        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
        onClick = {
          expanded = false
          onOpen()
        },
        modifier = Modifier.testTag("album_menu_open_${album.name}"),
      )
      DropdownMenuItem(
        text = { Text("Compress to Archive") },
        leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onArchiveAlbum(album)
        },
        modifier = Modifier.testTag("album_menu_archive_${album.name}"),
      )
      DropdownMenuItem(
        text = { Text("Open in Files") },
        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onOpenAlbumInFiles(album)
        },
        modifier = Modifier.testTag("album_menu_open_in_files_${album.name}"),
      )
      HorizontalDivider()
      DropdownMenuItem(
        text = { Text("Share") },
        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onShareAlbum(album)
        },
        modifier = Modifier.testTag("album_menu_share_${album.name}"),
      )
      DropdownMenuItem(
        text = { Text("Cut") },
        leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onCutAlbum(album)
        },
        modifier = Modifier.testTag("album_menu_cut_${album.name}"),
      )
      DropdownMenuItem(
        text = { Text("Copy") },
        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onCopyAlbum(album)
        },
        modifier = Modifier.testTag("album_menu_copy_${album.name}"),
      )
      DropdownMenuItem(
        text = { Text("Rename") },
        leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onRenameAlbum(album)
        },
        modifier = Modifier.testTag("album_menu_rename_${album.name}"),
      )
      DropdownMenuItem(
        text = { Text("Info") },
        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onAlbumInfo(album)
        },
        modifier = Modifier.testTag("album_menu_info_${album.name}"),
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
          expanded = false
          actions.onDeleteAlbum(album)
        },
        modifier = Modifier.testTag("album_menu_delete_${album.name}"),
      )
    }
  }
}

@Composable
private fun GalleryPhotoContextMenuButton(
  photo: GalleryPhoto,
  indexEntry: GalleryPhotoIndexEntry? = null,
  onView: () -> Unit,
  onEdit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val actions = LocalGalleryItemActions.current
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    IconButton(
      onClick = { expanded = true },
      modifier = Modifier
        .size(32.dp)
        .testTag("photo_more_${photo.name}"),
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "More options for ${photo.name}",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(18.dp),
      )
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.testTag("photo_menu_${photo.name}"),
    ) {
      DropdownMenuItem(
        text = { Text("View") },
        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
        onClick = {
          expanded = false
          onView()
        },
        modifier = Modifier.testTag("photo_menu_view_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text(if (photo.isVideo) "Edit in Video Editor" else "Edit in Image Editor") },
        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
        onClick = {
          expanded = false
          onEdit()
        },
        modifier = Modifier.testTag("photo_menu_edit_${photo.name}"),
      )
      if (!photo.isVideo) {
        DropdownMenuItem(
          text = { Text("Inspect Color") },
          leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
          onClick = {
            expanded = false
            actions.onInspectPhotoColor(photo)
          },
          modifier = Modifier.testTag("photo_menu_inspect_color_${photo.name}"),
        )
      }
      DropdownMenuItem(
        text = { Text("Convert") },
        leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onConvertPhoto(photo)
        },
        modifier = Modifier.testTag("photo_menu_convert_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text("Compress to Archive") },
        leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onArchivePhoto(photo)
        },
        modifier = Modifier.testTag("photo_menu_archive_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text("Save to Notes") },
        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onSavePhotoToNotes(photo, indexEntry)
        },
        modifier = Modifier.testTag("photo_menu_save_to_notes_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text("Open in Files") },
        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onOpenPhotoInFiles(photo)
        },
        modifier = Modifier.testTag("photo_menu_open_in_files_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text("Open with…") },
        leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onOpenPhotoWith(photo)
        },
        modifier = Modifier.testTag("photo_menu_open_with_${photo.name}"),
      )
      HorizontalDivider()
      DropdownMenuItem(
        text = { Text("Share") },
        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onSharePhoto(photo)
        },
        modifier = Modifier.testTag("photo_menu_share_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text("Cut") },
        leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onCutPhoto(photo)
        },
        modifier = Modifier.testTag("photo_menu_cut_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text("Copy") },
        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onCopyPhoto(photo)
        },
        modifier = Modifier.testTag("photo_menu_copy_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text("Duplicate") },
        leadingIcon = { Icon(Icons.Default.FileCopy, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onDuplicatePhoto(photo)
        },
        modifier = Modifier.testTag("photo_menu_duplicate_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text("Rename") },
        leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onRenamePhoto(photo)
        },
        modifier = Modifier.testTag("photo_menu_rename_${photo.name}"),
      )
      DropdownMenuItem(
        text = { Text("Info") },
        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onPhotoInfo(photo)
        },
        modifier = Modifier.testTag("photo_menu_info_${photo.name}"),
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
          expanded = false
          actions.onDeletePhoto(photo)
        },
        modifier = Modifier.testTag("photo_menu_delete_${photo.name}"),
      )
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
) {
  val context = LocalContext.current
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
  var showRecognitionInfoSheet by rememberSaveable { mutableStateOf(false) }

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
  val currentIndexEntry = photoIndexMap[currentPhoto.path]

  // Ensure the active photo is indexed immediately if it hasn't been indexed yet
  LaunchedEffect(currentPhoto.path, currentPhoto.lastModifiedMs) {
    if (currentIndexEntry == null) {
      val entry = GalleryEngine.indexSinglePhoto(currentPhoto.file)
      onPhotoIndexed(entry)
    }
  }

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

      // Top overlay bar with Close, current photo title & index, zoom indicator, Info/OCR, Edit, and Share
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

      // Bottom preview scroll bar + Auto-Tag & OCR/QR quick bar, dismissible with a tap on the image
      AnimatedVisibility(
        visible = showPreviewBar,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = Modifier.align(Alignment.BottomCenter),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
        ) {
          if (currentIndexEntry != null &&
            (currentIndexEntry.autoTags.isNotEmpty() || currentIndexEntry.hasOcrText || currentIndexEntry.hasBarcodes)
          ) {
            Surface(
              color = Color.Black.copy(alpha = 0.68f),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("photo_viewer_recognition_bar"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState())
                  .padding(horizontal = 16.dp, vertical = 8.dp),
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
          }

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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GalleryPhotoRecognitionInfoDialog(
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
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("photo_info_copy_ocr_button"),
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Copy Text", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                  onClick = { onSaveOcrToNotes(indexEntry.ocrText) },
                  shape = RoundedCornerShape(10.dp),
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
                    shape = RoundedCornerShape(10.dp),
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
                      shape = RoundedCornerShape(10.dp),
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
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("photo_info_close_button"),
      ) {
        Text("Done")
      }
    },
  )
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

@Composable
private fun GalleryBulkInfoDialog(
  albums: List<GalleryAlbum>,
  photos: List<GalleryPhoto>,
  onDismiss: () -> Unit,
) {
  val info = remember(albums, photos) {
    GalleryEngine.getBulkSelectionInfo(albums, photos)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("gallery_bulk_info_dialog"),
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
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Selection Info",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${info.totalItems} ${if (info.totalItems == 1) "item" else "items"} selected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
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
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            GalleryDetailRow("Total Size", info.formattedSize)
            GalleryDetailRow("Exact Bytes", "${info.totalSizeBytes} bytes")
            GalleryDetailRow(
              "Breakdown",
              "${info.albumCount} ${if (info.albumCount == 1) "album" else "albums"}, " +
                "${info.photoCount} ${if (info.photoCount == 1) "photo" else "photos"}",
            )
            GalleryDetailRow(
              "Total Media Files",
              "${info.totalMediaFileCount} ${if (info.totalMediaFileCount == 1) "file" else "files"}",
            )
          }
        }

        if (albums.isNotEmpty()) {
          Text(
            text = "Selected Albums",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
          )
          albums.forEach { album ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Text(
                text = album.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
              )
              Text(
                text = album.summaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        if (photos.isNotEmpty()) {
          Text(
            text = "Selected Photos",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
          )
          photos.forEach { photo ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = if (photo.isVideo) Icons.Default.VideoFile else Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(22.dp),
              )
              Text(
                text = photo.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
              )
              Text(
                text = photo.sizeFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("gallery_close_bulk_info"),
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun GalleryBulkDeleteConfirmDialog(
  albums: List<GalleryAlbum>,
  photos: List<GalleryPhoto>,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  val info = remember(albums, photos) {
    GalleryEngine.getBulkSelectionInfo(albums, photos)
  }
  val itemNames = remember(albums, photos) {
    albums.map { it.name } + photos.map { it.name }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("gallery_bulk_delete_dialog"),
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
            text = "Delete ${info.totalItems} ${if (info.totalItems == 1) "item" else "items"}?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Total: ${info.formattedSize}",
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
          text = "Are you sure you want to permanently delete the selected albums and photos? This action cannot be undone.",
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
            itemNames.take(4).forEach { name ->
              Text(
                text = "• $name",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            if (itemNames.size > 4) {
              Text(
                text = "...and ${itemNames.size - 4} more",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("gallery_confirm_bulk_delete"),
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

@Composable
private fun GalleryDetailRow(label: String, value: String) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Medium,
    )
  }
}

private fun sharePhotoFile(context: Context, file: File) {
  shareMediaFiles(context, listOf(file))
}

private fun shareMediaFiles(context: Context, files: List<File>) {
  if (files.isEmpty()) return
  try {
    if (files.size == 1) {
      val file = files.first()
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
    } else {
      val uris = ArrayList<Uri>(files.size)
      for (file in files) {
        val uri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          file,
        )
        uris.add(uri)
      }
      val allVideos = files.all { VideoEditorEngine.isVideoFile(it) }
      val allImages = files.all { !VideoEditorEngine.isVideoFile(it) }
      val mimeType = when {
        allImages -> "image/*"
        allVideos -> "video/*"
        else -> "*/*"
      }
      val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = mimeType
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(intent, "Share ${files.size} items"))
    }
  } catch (_: Exception) {
    Toast.makeText(context, "Unable to share selected items", Toast.LENGTH_SHORT).show()
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
