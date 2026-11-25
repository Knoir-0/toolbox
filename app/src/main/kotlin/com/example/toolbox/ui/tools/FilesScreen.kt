package com.example.toolbox.ui.tools

import com.example.toolbox.ui.theme.toolboxScreenBackground

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.Surface as AndroidSurface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import com.example.toolbox.ui.theme.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.CropOriginal
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import com.example.toolbox.ui.theme.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.toolbox.ui.theme.FilterChip
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import androidx.compose.material3.FilterChipDefaults
import com.example.toolbox.ui.theme.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.toolbox.ui.theme.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.example.toolbox.ui.theme.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.toolbox.ui.theme.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.toolbox.ui.main.addFolderToHomeScreen
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
  modifier: Modifier = Modifier,
  initialDirectory: File? = null,
  rootDirectory: File? = null,
  initialSearchActive: Boolean = false,
  onOpenFile: ((File, Boolean) -> Unit)? = null,
  onOpenFileConverter: ((File) -> Unit)? = null,
  onOpenImage: ((File) -> Unit)? = null,
  onOpenImageEditor: ((File) -> Unit)? = null,
  onOpenVideoEditor: ((File) -> Unit)? = null,
  onOpenAudioEditor: ((File) -> Unit)? = null,
  onOpenArchive: ((File) -> Unit)? = null,
  onOpenPdfViewer: ((File) -> Unit)? = null,
  onOpenMusicPlayer: ((File) -> Unit)? = null,
  onOpenGallery: ((File) -> Unit)? = null,
  onOpenColorPicker: ((String) -> Unit)? = null,
  onOpenNotes: ((File) -> Unit)? = null,
  onOpenTodoList: ((File) -> Unit)? = null,
  onNavigateBack: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current

  val defaultUserRoot = remember(isInspection) {
    if (isInspection) File("/storage/emulated/0") else FileManagerEngine.getUserRootDirectory()
  }

  val hasCustomRoot = remember(rootDirectory, defaultUserRoot) {
    rootDirectory != null &&
      rootDirectory.absoluteFile.normalize().path != defaultUserRoot.absoluteFile.normalize().path
  }

  // User's default root folder: storage/emulated/0 (or custom rootDirectory if provided)
  val userRoot = remember(rootDirectory, initialDirectory, isInspection, defaultUserRoot) {
    when {
      rootDirectory != null -> rootDirectory.absoluteFile.normalize()
      isInspection -> defaultUserRoot
      else -> {
        val initDir = initialDirectory?.absoluteFile?.normalize()
        if (initDir != null && !FileManagerEngine.isSameOrDescendant(initDir, defaultUserRoot)) {
          val volumes = FileManagerEngine.getStorageVolumes(context)
          val matchingVolume = volumes.firstOrNull { vol ->
            FileManagerEngine.isSameOrDescendant(initDir, vol.file)
          }
          matchingVolume?.file ?: initDir
        } else {
          defaultUserRoot
        }
      }
    }
  }

  // Active directory defaults to storage/emulated/0 (or custom rootDirectory)
  var currentDirectory by rememberSaveable(
    initialDirectory?.absoluteFile?.normalize()?.path,
    rootDirectory?.absoluteFile?.normalize()?.path,
  ) {
    mutableStateOf(initialDirectory?.absoluteFile?.normalize() ?: userRoot)
  }

  LaunchedEffect(userRoot, hasCustomRoot) {
    if (hasCustomRoot && !FileManagerEngine.isSameOrDescendant(currentDirectory, userRoot)) {
      currentDirectory = userRoot
    }
  }

  // Hot-plug storage revision token
  var storageRevision by remember { mutableIntStateOf(0) }

  val samplesRoot = remember(context, isInspection) {
    if (isInspection) File("/storage/emulated/0/Samples") else FileManagerEngine.getSamplesDirectory(context)
  }

  // Discovered storage volumes (Primary root + Samples folder + any connected external storage devices,
  // or the scoped root folder when opened with a custom rootDirectory such as a home screen folder shortcut)
  val storageVolumes = remember(storageRevision, isInspection, userRoot, samplesRoot, hasCustomRoot) {
    if (hasCustomRoot) {
      listOf(
        StorageVolumeInfo(
          id = "primary_root",
          name = userRoot.name.ifBlank { "Home" },
          file = userRoot,
          isPrimary = true,
          isRemovable = false,
        ),
      )
    } else if (isInspection) {
      listOf(
        StorageVolumeInfo(
          id = "primary_root",
          name = "Home",
          file = userRoot,
          isPrimary = true,
          isRemovable = false,
        ),
        StorageVolumeInfo(
          id = FileManagerEngine.SAMPLES_VOLUME_ID,
          name = FileManagerEngine.SAMPLES_FOLDER_NAME,
          file = samplesRoot,
          isPrimary = false,
          isRemovable = false,
        ),
        StorageVolumeInfo(
          id = "sdcard_mock",
          name = "SD Card",
          file = File("/storage/1234-5678"),
          isPrimary = false,
          isRemovable = true,
        ),
      )
    } else {
      FileManagerEngine.getStorageVolumes(context).map { vol ->
        if (vol.isPrimary) vol.copy(file = userRoot) else vol
      }
    }
  }

  // Resolve the currently active volume based on currentDirectory
  val activeVolume = remember(currentDirectory, storageVolumes) {
    storageVolumes
      .filter { vol -> FileManagerEngine.isSameOrDescendant(currentDirectory, vol.file) }
      .maxByOrNull { vol -> vol.file.absolutePath.length }
      ?: storageVolumes.firstOrNull()
  }

  // Listen for storage device connection and disconnection events
  DisposableEffect(context, isInspection) {
    if (isInspection) {
      onDispose {}
    } else {
      val filter = IntentFilter().apply {
        addAction(Intent.ACTION_MEDIA_MOUNTED)
        addAction(Intent.ACTION_MEDIA_UNMOUNTED)
        addAction(Intent.ACTION_MEDIA_EJECT)
        addAction(Intent.ACTION_MEDIA_REMOVED)
        addDataScheme("file")
      }
      val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
          storageRevision++
        }
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
      } else {
        context.registerReceiver(receiver, filter)
      }
      onDispose {
        try {
          context.unregisterReceiver(receiver)
        } catch (_: Exception) {}
      }
    }
  }

  // Permission handling for reading storage/emulated/0
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  val storagePermissionSettingGranted = com.example.toolbox.ui.main.NavigationPreferencesBridge.isPermissionGranted(
    com.example.toolbox.ui.main.AppPermission.STORAGE
  )
  var systemHasPermission by remember(context, isInspection) {
    mutableStateOf(isInspection || FileManagerEngine.hasStoragePermission(context))
  }
  DisposableEffect(lifecycleOwner, context, isInspection) {
    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
      if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && !isInspection) {
        systemHasPermission = FileManagerEngine.hasStoragePermission(context)
        storageRevision++
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
  val hasPermission = isInspection || (storagePermissionSettingGranted && systemHasPermission)

  val manageStorageLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    systemHasPermission = FileManagerEngine.hasStoragePermission(context)
    storageRevision++
  }

  val legacyPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    systemHasPermission = isGranted
    storageRevision++
  }

  val manageUnknownSourcesLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    storageRevision++
  }

  fun requestStoragePermission() {
    if (!storagePermissionSettingGranted) {
      com.example.toolbox.ui.main.NavigationPreferencesBridge.activeRepository?.grantPermission(
        com.example.toolbox.ui.main.AppPermission.STORAGE
      ) ?: run {
        com.example.toolbox.ui.main.NavigationPreferencesBridge.permissionGrantStates =
          com.example.toolbox.ui.main.NavigationPreferencesBridge.permissionGrantStates +
            (com.example.toolbox.ui.main.AppPermission.STORAGE to true)
      }
      if (FileManagerEngine.hasStoragePermission(context)) {
        systemHasPermission = true
        storageRevision++
        return
      }
    }
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

  // Default sort & group configuration for folders without a saved folder-specific override
  var defaultSortGroupConfig by remember {
    mutableStateOf(
      if (isInspection) {
        FolderSortGroupConfig()
      } else {
        FileManagerEngine.getDefaultSortGroupConfig(context)
      }
    )
  }

  val initialResolvedSortGroup = remember {
    if (isInspection) {
      FolderSortGroupConfig() to false
    } else {
      FileManagerEngine.resolveSortGroupConfig(context, currentDirectory)
    }
  }

  var isSortGroupSavedForFolder by remember(currentDirectory) {
    mutableStateOf(
      if (isInspection) false else FileManagerEngine.hasFolderSortGroupConfig(context, currentDirectory)
    )
  }

  var folderEntryDefaultConfig by remember(currentDirectory) {
    mutableStateOf(defaultSortGroupConfig)
  }

  var groupOption by rememberSaveable { mutableStateOf(initialResolvedSortGroup.first.groupOption) }
  var sortOrder by rememberSaveable { mutableStateOf(initialResolvedSortGroup.first.sortOrder) }
  var viewMode by rememberSaveable { mutableStateOf(FileViewMode.LIST) }
  var gridColumnsPerRow by rememberSaveable {
    mutableIntStateOf(
      if (isInspection) DEFAULT_FILES_GRID_COLUMNS else FileManagerEngine.getGridColumnsPerRow(context)
    )
  }
  fun updateGridColumnsPerRow(newColumns: Int) {
    val coerced = FileManagerEngine.coerceGridColumnsPerRow(newColumns)
    gridColumnsPerRow = coerced
    if (!isInspection) {
      FileManagerEngine.setGridColumnsPerRow(context, coerced)
    }
  }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var isSearchActive by rememberSaveable { mutableStateOf(initialSearchActive) }
  var isFabExpanded by rememberSaveable { mutableStateOf(false) }

  // Collapsed group titles for collapsible categories (resets on folder or group option change)
  var collapsedGroupTitles by remember(currentDirectory, groupOption) {
    mutableStateOf(setOf<String>())
  }

  // Directory content revision token to trigger recomposition on modifications
  var refreshTrigger by remember { mutableIntStateOf(0) }

  // Ensure the Samples directory and sample video are populated in the background
  LaunchedEffect(samplesRoot, isInspection) {
    if (!isInspection) {
      kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        FileManagerEngine.populateDefaultSampleFiles(samplesRoot, context)
        VideoEditorEngine.generateSampleVideo(context, samplesRoot)
      }
      if (FileManagerEngine.isSameOrDescendant(currentDirectory, samplesRoot)) {
        refreshTrigger++
      }
    }
  }

  // Items in current directory
  val items = remember(currentDirectory, refreshTrigger, storageRevision, hasPermission, isInspection, samplesRoot) {
    if (isInspection) {
      getPreviewSampleFiles()
    } else {
      if (FileManagerEngine.isSameOrDescendant(currentDirectory, samplesRoot)) {
        FileManagerEngine.populateDefaultSampleFiles(samplesRoot, context)
      }
      FileManagerEngine.listDirectoryContents(currentDirectory)
    }
  }

  // Filtered, sorted & grouped items
  val groupedItems = remember(items, searchQuery, sortOrder, groupOption) {
    val filtered = FileManagerEngine.filterFiles(items, searchQuery)
    FileManagerEngine.groupAndSortFiles(filtered, groupOption, sortOrder)
  }

  // Flattened list for selection / counts / empty state
  val processedItems = remember(groupedItems) {
    groupedItems.flatMap { it.items }
  }

  // Storage summary info (computed asynchronously in background task, cancellable on folder navigation)
  var storageSummary by remember(currentDirectory, refreshTrigger) {
    mutableStateOf<Pair<Long, Int>?>(if (isInspection) 124000L to 4 else null)
  }

  LaunchedEffect(currentDirectory, refreshTrigger, isInspection) {
    if (isInspection) {
      storageSummary = 124000L to 4
    } else {
      storageSummary = null
      storageSummary = FileManagerEngine.getStorageSummary(currentDirectory)
    }
  }

  // Device storage space information (used/total) for the active volume
  val storageSpaceInfo = remember(activeVolume, storageRevision, isInspection) {
    if (isInspection) {
      StorageSpaceInfo(
        totalBytes = 128L * 1024L * 1024L * 1024L,
        freeBytes = 82L * 1024L * 1024L * 1024L,
        usedBytes = 46L * 1024L * 1024L * 1024L,
      )
    } else {
      FileManagerEngine.getDeviceStorageSpace(activeVolume?.file ?: userRoot)
    }
  }

  val storageInfoSubtext = remember(storageSpaceInfo) {
    storageSpaceInfo?.displayText ?: "Device Storage"
  }

  // Dialog and Sheet States
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showCreateFileDialog by remember { mutableStateOf(false) }
  var fileToRename by remember { mutableStateOf<FileItem?>(null) }
  var fileToDelete by remember { mutableStateOf<FileItem?>(null) }
  var fileForDetails by remember { mutableStateOf<FileItem?>(null) }
  var fileForTextEdit by remember { mutableStateOf<FileItem?>(null) }
  var activeAudioPlaybackItem by remember { mutableStateOf<AudioRecordingItem?>(null) }
  var activeVideoPlaybackFile by remember { mutableStateOf<File?>(null) }
  var fileForImage by remember { mutableStateOf<File?>(null) }
  var fileForConverter by remember { mutableStateOf<File?>(null) }
  var fileForImageEditor by remember { mutableStateOf<File?>(null) }
  var fileForVideoEditor by remember { mutableStateOf<File?>(null) }
  var fileForAudioEditor by remember { mutableStateOf<File?>(null) }
  var fileForArchive by remember { mutableStateOf<File?>(null) }
  var filesForArchiveCompression by remember { mutableStateOf<List<File>?>(null) }
  var fileForPdfViewer by remember { mutableStateOf<File?>(null) }
  var fileForMusicPlayer by remember { mutableStateOf<File?>(null) }
  var fileForGallery by remember { mutableStateOf<File?>(null) }
  var colorHexForColorPicker by remember { mutableStateOf<String?>(null) }
  var showSortMenu by remember { mutableStateOf(false) }

  val previewIndexCacheFile = remember(context, isInspection) {
    if (isInspection) null else GalleryEngine.getDefaultIndexCacheFile(context)
  }
  var previewPhotoIndexMap by remember {
    mutableStateOf(GalleryEngine.loadIndexCacheFromDisk(previewIndexCacheFile))
  }

  // Multi-selection state
  var isSelectionMode by rememberSaveable { mutableStateOf(false) }
  var selectedPaths by remember { mutableStateOf(setOf<String>()) }
  var showBulkInfoDialog by remember { mutableStateOf(false) }
  var showBulkDeleteDialog by remember { mutableStateOf(false) }
  var itemsToMove by remember { mutableStateOf<List<FileItem>?>(null) }
  var clipboardState by remember { mutableStateOf<ClipboardState?>(null) }

  val selectedItems = remember(processedItems, selectedPaths) {
    processedItems.filter { it.path in selectedPaths }
  }

  fun navigateTo(dir: File) {
    currentDirectory = dir
    val folderConfig = if (isInspection) null else FileManagerEngine.getFolderSortGroupConfig(context, dir)
    if (folderConfig != null) {
      isSortGroupSavedForFolder = true
      groupOption = folderConfig.groupOption
      sortOrder = folderConfig.sortOrder
    } else {
      isSortGroupSavedForFolder = false
      groupOption = defaultSortGroupConfig.groupOption
      sortOrder = defaultSortGroupConfig.sortOrder
    }
    searchQuery = ""
    isSearchActive = false
    isSelectionMode = false
    selectedPaths = emptySet()
    isFabExpanded = false
  }

  val activeRoot = activeVolume?.file ?: userRoot

  fun canNavigateUp(): Boolean {
    return FileManagerEngine.canNavigateUp(currentDirectory, activeRoot)
  }

  fun navigateUp(): Boolean {
    val parent = FileManagerEngine.navigateUpDirectory(currentDirectory, activeRoot)
    if (parent != null) {
      navigateTo(parent)
      return true
    }
    if (onNavigateBack != null) {
      onNavigateBack()
      return true
    }
    return false
  }

  fun openFile(file: File, chooseApp: Boolean = false) {
    if (!chooseApp && (FileManagerEngine.isVideoFile(file) || VideoEditorEngine.isVideoFile(file))) {
      activeVideoPlaybackFile = file
      return
    }
    if (!chooseApp && FileManagerEngine.isAudioFile(file) && !VideoEditorEngine.isVideoFile(file)) {
      activeAudioPlaybackItem = AudioRecorderEngine.toAudioRecordingItem(
        file = file,
        includeDurationMetadata = !isInspection,
      )
      return
    }
    if (!chooseApp && FileManagerEngine.isTextFile(file)) {
      fileForTextEdit = processedItems.firstOrNull { it.path == file.absolutePath }
        ?: FileManagerEngine.toFileItem(file)
      return
    }
    if (onOpenFile != null) {
      onOpenFile(file, chooseApp)
    } else {
      FileManagerEngine.openFile(
        context = context,
        file = file,
        chooseApp = chooseApp,
        unknownSourcesSettingsLauncher = { settingsIntent ->
          manageUnknownSourcesLauncher.launch(settingsIntent)
        },
      )
    }
  }

  fun shareFile(file: File) {
    CrossToolActions.shareFile(context, file)
  }

  fun shareMultipleItems(filesToShare: List<File>) {
    CrossToolActions.shareMultipleFiles(context, filesToShare)
  }

  fun saveFileToNotes(file: File) {
    val repo = SharedPreferencesNotesRepository(context)
    val imported = FileManagerEngine.importFileToNotes(file, repo)
    if (imported != null) {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      onOpenNotes?.invoke(file)
      Toast.makeText(context, "Saved \"${imported.title}\" to Notes", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Could not import ${file.name} to Notes", Toast.LENGTH_SHORT).show()
    }
  }

  fun importFileToTodo(file: File) {
    val repo = SharedPreferencesTodoListRepository(context)
    val imported = FileManagerEngine.importFileToTodoList(file, repo)
    if (imported != null) {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      onOpenTodoList?.invoke(file)
      val subCount = imported.subtasks.size
      val detail = if (subCount > 0) " ($subCount items)" else ""
      Toast.makeText(context, "Imported \"${imported.title}\"$detail to To Do", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Could not import ${file.name} to To Do", Toast.LENGTH_SHORT).show()
    }
  }

  fun inspectImageColor(file: File) {
    val hex = FileManagerEngine.extractDominantColorHex(file) ?: "#10B981"
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    colorHexForColorPicker = hex
    onOpenColorPicker?.invoke(hex)
  }

  val activeConverterFile = fileForConverter
  if (activeConverterFile != null) {
    FileConverterScreen(
      modifier = modifier,
      initialFile = activeConverterFile,
      initialDirectory = currentDirectory,
      onNavigateBack = {
        fileForConverter = null
        refreshTrigger++
      },
    )
    return
  }

  val activeImageFile = fileForImage
  if (activeImageFile != null) {
    val siblingPhotos = remember(processedItems, activeImageFile.absolutePath) {
      val mediaItems = processedItems.filter {
        !it.isDirectory && FileManagerEngine.isImageFile(it.file)
      }.map { mediaItem ->
        GalleryPhoto(
          file = mediaItem.file,
          name = mediaItem.name,
          path = mediaItem.path,
          sizeBytes = mediaItem.sizeBytes,
          lastModifiedMs = mediaItem.lastModifiedMs,
          extension = mediaItem.extension.lowercase(Locale.ROOT),
        )
      }
      val initialPhoto = GalleryPhoto(
        file = activeImageFile,
        name = activeImageFile.name,
        path = activeImageFile.absolutePath,
        sizeBytes = activeImageFile.length(),
        lastModifiedMs = activeImageFile.lastModified(),
        extension = activeImageFile.extension.lowercase(Locale.ROOT),
      )
      if (mediaItems.any { it.path == initialPhoto.path }) {
        mediaItems
      } else {
        listOf(initialPhoto) + mediaItems
      }
    }
    ImageScreen(
      modifier = modifier,
      initialFile = activeImageFile,
      initialPhotos = siblingPhotos,
      initialDirectory = currentDirectory,
      photoIndexMap = previewPhotoIndexMap,
      onPhotoIndexed = { entry ->
        previewPhotoIndexMap = previewPhotoIndexMap + (entry.path to entry)
        if (!isInspection) {
          GalleryEngine.saveIndexCacheToDisk(previewIndexCacheFile, previewPhotoIndexMap)
        }
      },
      onNavigateBack = {
        fileForImage = null
        refreshTrigger++
      },
      onOpenImageEditor = { editFile ->
        fileForImage = null
        fileForImageEditor = editFile
        onOpenImageEditor?.invoke(editFile)
      },
      onOpenVideoEditor = { editFile ->
        fileForImage = null
        fileForVideoEditor = editFile
        onOpenVideoEditor?.invoke(editFile)
      },
      onOpenColorPicker = { hex ->
        fileForImage = null
        colorHexForColorPicker = hex
        onOpenColorPicker?.invoke(hex)
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
      initialDirectory = currentDirectory,
      modifier = modifier,
      onNavigateBack = {
        fileForVideoEditor = null
        refreshTrigger++
      },
    )
    return
  }

  val activeAudioEditorFile = fileForAudioEditor
  if (activeAudioEditorFile != null) {
    AudioEditorScreen(
      modifier = modifier,
      initialFile = activeAudioEditorFile,
      initialDirectory = currentDirectory,
      onNavigateBack = {
        fileForAudioEditor = null
        refreshTrigger++
      },
    )
    return
  }

  val activeArchiveFile = fileForArchive
  if (activeArchiveFile != null) {
    ArchiveScreen(
      modifier = modifier,
      initialFile = activeArchiveFile,
      initialDirectory = currentDirectory,
      onNavigateBack = {
        fileForArchive = null
        refreshTrigger++
      },
    )
    return
  }

  val activeCompressionFiles = filesForArchiveCompression
  if (!activeCompressionFiles.isNullOrEmpty()) {
    ArchiveScreen(
      modifier = modifier,
      initialFilesToCompress = activeCompressionFiles,
      initialDirectory = currentDirectory,
      onNavigateBack = {
        filesForArchiveCompression = null
        refreshTrigger++
      },
    )
    return
  }

  val activePdfViewerFile = fileForPdfViewer
  if (activePdfViewerFile != null) {
    DocumentViewerScreen(
      modifier = modifier,
      initialFile = activePdfViewerFile,
      initialDirectory = currentDirectory,
      onNavigateBack = {
        fileForPdfViewer = null
        refreshTrigger++
      },
    )
    return
  }

  val activeMusicFile = fileForMusicPlayer
  if (activeMusicFile != null) {
    val isDir = activeMusicFile.isDirectory
    MusicPlayerScreen(
      modifier = modifier,
      initialFile = if (isDir) null else activeMusicFile,
      initialDirectory = if (isDir) activeMusicFile else (activeMusicFile.parentFile ?: currentDirectory),
      initialTab = MusicLibraryTab.SONGS,
      onNavigateBack = {
        fileForMusicPlayer = null
        refreshTrigger++
      },
    )
    return
  }

  val activeGalleryTarget = fileForGallery
  if (activeGalleryTarget != null) {
    val isDir = activeGalleryTarget.isDirectory
    val targetAlbumDir = if (isDir) activeGalleryTarget else (activeGalleryTarget.parentFile ?: currentDirectory)
    GalleryScreen(
      modifier = modifier,
      rootDirectory = targetAlbumDir,
      initialAlbumDirectory = targetAlbumDir,
      initialPhotoFile = if (isDir) null else activeGalleryTarget,
      onNavigateBack = {
        fileForGallery = null
        refreshTrigger++
      },
      onOpenImageEditor = onOpenImageEditor,
      onOpenVideoEditor = onOpenVideoEditor,
    )
    return
  }

  val activeColorHex = colorHexForColorPicker
  if (activeColorHex != null) {
    ColorPickerScreen(
      modifier = modifier,
      initialHexColor = activeColorHex,
      onNavigateBack = {
        colorHexForColorPicker = null
      },
    )
    return
  }

  val canGoUp = canNavigateUp() || onNavigateBack != null

  BackHandler(enabled = isFabExpanded || isSelectionMode || isSearchActive || canGoUp) {
    when {
      isFabExpanded -> {
        isFabExpanded = false
      }
      isSelectionMode -> {
        isSelectionMode = false
        selectedPaths = emptySet()
      }
      isSearchActive -> {
        isSearchActive = false
        searchQuery = ""
      }
      canGoUp -> {
        navigateUp()
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("files_screen"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .toolboxScreenBackground(MaterialTheme.colorScheme.background),
    ) {
      // 1. Top Bar / Header
      val headerSubtext = if (canNavigateUp() || hasCustomRoot) {
        val folderSize = storageSummary?.first
        if (folderSize != null) {
          val folderSizeFormatted = FileManagerEngine.formatFileSize(folderSize)
          "$folderSizeFormatted • $storageInfoSubtext"
        } else {
          storageInfoSubtext
        }
      } else {
        storageInfoSubtext
      }

      val rootHeaderTitle = if (hasCustomRoot) userRoot.name.ifBlank { "Files" } else "Files"

      FilesHeader(
        currentDirectory = currentDirectory,
        rootTitle = rootHeaderTitle,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        storageInfoSubtext = headerSubtext,
        viewMode = viewMode,
        gridColumnsPerRow = gridColumnsPerRow,
        isSelectionMode = isSelectionMode,
        selectedCount = selectedPaths.size,
        totalCount = processedItems.size,
        onToggleSelectAll = {
          selectedPaths = if (selectedPaths.size == processedItems.size && processedItems.isNotEmpty()) {
            emptySet()
          } else {
            processedItems.map { it.path }.toSet()
          }
        },
        onCloseSelection = {
          isSelectionMode = false
          selectedPaths = emptySet()
        },
        onBulkInfo = {
          if (selectedItems.isNotEmpty()) {
            showBulkInfoDialog = true
          }
        },
        onBulkCut = {
          if (selectedItems.isNotEmpty()) {
            clipboardState = ClipboardState(ClipboardOperation.CUT, selectedItems)
            isSelectionMode = false
            selectedPaths = emptySet()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "${selectedItems.size} items cut", Toast.LENGTH_SHORT).show()
          }
        },
        onBulkMove = {
          if (selectedItems.isNotEmpty()) {
            clipboardState = ClipboardState(ClipboardOperation.MOVE, selectedItems)
            isSelectionMode = false
            selectedPaths = emptySet()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "${selectedItems.size} items ready to move", Toast.LENGTH_SHORT).show()
          }
        },
        onBulkCopy = {
          if (selectedItems.isNotEmpty()) {
            clipboardState = ClipboardState(ClipboardOperation.COPY, selectedItems)
            isSelectionMode = false
            selectedPaths = emptySet()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "${selectedItems.size} items copied", Toast.LENGTH_SHORT).show()
          }
        },
        onBulkDelete = {
          if (selectedItems.isNotEmpty()) {
            showBulkDeleteDialog = true
          }
        },
        onToggleSearch = {
          isSearchActive = !isSearchActive
          if (!isSearchActive) searchQuery = ""
        },
        onSearchQueryChange = { searchQuery = it },
        onToggleViewMode = {
          if (viewMode == FileViewMode.LIST) {
            viewMode = FileViewMode.GRID
            updateGridColumnsPerRow(MIN_FILES_GRID_COLUMNS)
          } else if (gridColumnsPerRow < MAX_FILES_GRID_COLUMNS) {
            updateGridColumnsPerRow(gridColumnsPerRow + 1)
          } else {
            viewMode = FileViewMode.LIST
          }
        },
        onSelectListView = {
          viewMode = FileViewMode.LIST
        },
        onSelectGridColumns = { cols ->
          viewMode = FileViewMode.GRID
          updateGridColumnsPerRow(cols)
        },
        onNavigateUp = { navigateUp() },
        canNavigateUp = canNavigateUp() || onNavigateBack != null,
        singleSelectedFolder = if (selectedItems.size == 1 && selectedItems.first().isDirectory) selectedItems.first().file else null,
        singleSelectedFile = if (selectedItems.size == 1 && !selectedItems.first().isDirectory) selectedItems.first().file else null,
        onOpenSelectedFile = { file, chooseApp ->
          isSelectionMode = false
          selectedPaths = emptySet()
          openFile(file, chooseApp)
        },
        onConvertSelectedFile = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          fileForConverter = file
          onOpenFileConverter?.invoke(file)
        },
        onOpenSelectedImage = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          fileForImage = file
          onOpenImage?.invoke(file)
        },
        onEditSelectedImage = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          fileForImageEditor = file
          onOpenImageEditor?.invoke(file)
        },
        onEditSelectedVideo = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          fileForVideoEditor = file
          onOpenVideoEditor?.invoke(file)
        },
        onEditSelectedAudio = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          fileForAudioEditor = file
          onOpenAudioEditor?.invoke(file)
        },
        onOpenSelectedArchive = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          fileForArchive = file
          onOpenArchive?.invoke(file)
        },
        onOpenSelectedPdf = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          fileForPdfViewer = file
          onOpenPdfViewer?.invoke(file)
        },
        onPlaySelectedMusic = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          fileForMusicPlayer = file
          onOpenMusicPlayer?.invoke(file)
        },
        onOpenSelectedGallery = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          fileForGallery = file
          onOpenGallery?.invoke(file)
        },
        onInspectSelectedColor = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          inspectImageColor(file)
        },
        onSaveSelectedToNotes = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          saveFileToNotes(file)
        },
        onImportSelectedToTodo = { file ->
          isSelectionMode = false
          selectedPaths = emptySet()
          importFileToTodo(file)
        },
        onBulkShare = {
          if (selectedItems.isNotEmpty()) {
            val files = selectedItems.map { it.file }
            isSelectionMode = false
            selectedPaths = emptySet()
            shareMultipleItems(files)
          }
        },
        onCompressSelectedItems = {
          if (selectedItems.isNotEmpty()) {
            val files = selectedItems.map { it.file }
            isSelectionMode = false
            selectedPaths = emptySet()
            filesForArchiveCompression = files
            files.firstOrNull()?.let { onOpenArchive?.invoke(it) }
          }
        },
        onAddFolderToHomeScreen = { folder -> addFolderToHomeScreen(context, folder) },
      )

      // 2. Storage Pill Buttons Row + Pinned Floating [# per row] & Sort/Group Buttons:
      // Starts with the user's root folder (storage/emulated/0), and whenever external
      // storage devices are connected, new pills pop up with the storage device name.
      StorageDevicePillRow(
        storageVolumes = storageVolumes,
        activeVolume = activeVolume,
        currentDirectory = currentDirectory,
        hasCustomRoot = hasCustomRoot,
        viewMode = viewMode,
        gridColumnsPerRow = gridColumnsPerRow,
        onGridColumnsChange = { updateGridColumnsPerRow(it) },
        sortOrder = sortOrder,
        groupOption = groupOption,
        showSortMenu = showSortMenu,
        isSortGroupSavedForFolder = isSortGroupSavedForFolder,
        onSelectVolume = { volume ->
          navigateTo(volume.file)
        },
        onOpenSortMenu = { showSortMenu = true },
        onDismissSortMenu = { showSortMenu = false },
        onSelectSortOrder = {
          sortOrder = it
          val updated = FolderSortGroupConfig(groupOption = groupOption, sortOrder = it)
          if (isSortGroupSavedForFolder) {
            if (!isInspection) {
              FileManagerEngine.setFolderSortGroupConfig(context, currentDirectory, updated)
            }
          } else {
            defaultSortGroupConfig = updated
            if (!isInspection) {
              FileManagerEngine.setDefaultSortGroupConfig(context, updated)
            }
          }
          showSortMenu = false
        },
        onSelectGroupOption = {
          groupOption = it
          val updated = FolderSortGroupConfig(groupOption = it, sortOrder = sortOrder)
          if (isSortGroupSavedForFolder) {
            if (!isInspection) {
              FileManagerEngine.setFolderSortGroupConfig(context, currentDirectory, updated)
            }
          } else {
            defaultSortGroupConfig = updated
            if (!isInspection) {
              FileManagerEngine.setDefaultSortGroupConfig(context, updated)
            }
          }
          showSortMenu = false
        },
        onToggleSaveSortGroupForFolder = {
          val newSaved = !isSortGroupSavedForFolder
          isSortGroupSavedForFolder = newSaved
          if (newSaved) {
            val folderConfig = FolderSortGroupConfig(groupOption = groupOption, sortOrder = sortOrder)
            if (!isInspection) {
              FileManagerEngine.setFolderSortGroupConfig(context, currentDirectory, folderConfig)
            }
            if (defaultSortGroupConfig != folderEntryDefaultConfig) {
              defaultSortGroupConfig = folderEntryDefaultConfig
              if (!isInspection) {
                FileManagerEngine.setDefaultSortGroupConfig(context, folderEntryDefaultConfig)
              }
            }
          } else {
            if (!isInspection) {
              FileManagerEngine.removeFolderSortGroupConfig(context, currentDirectory)
            }
            groupOption = defaultSortGroupConfig.groupOption
            sortOrder = defaultSortGroupConfig.sortOrder
          }
        },
      )

      val isBrowsingSamples = activeVolume?.id == FileManagerEngine.SAMPLES_VOLUME_ID ||
        FileManagerEngine.isSameOrDescendant(currentDirectory, samplesRoot)

      // Permission Request Banner (if access to storage/emulated/0 is restricted)
      if (!hasPermission && !isBrowsingSamples && !isInspection) {
        StoragePermissionBanner(
          onRequestPermission = { requestStoragePermission() },
        )
      }

      // 3. Breadcrumb Path Bar & Storage Summary
      BreadcrumbBar(
        currentDirectory = currentDirectory,
        volumeRoot = activeVolume?.file ?: userRoot,
        storageSummary = storageSummary,
        onNavigateToDir = { navigateTo(it) },
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // 4. File List / Grid or Empty State
      val activeVolumeForSwipe = remember(storageVolumes, activeVolume, currentDirectory, hasCustomRoot) {
        if (hasCustomRoot) {
          storageVolumes.firstOrNull { FileManagerEngine.isAtRootDirectory(currentDirectory, it.file) } ?: activeVolume
        } else {
          storageVolumes.firstOrNull { it.id == activeVolume?.id } ?: activeVolume
        }
      }
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .swipeToSwitchPillView(
            items = storageVolumes,
            selectedItem = activeVolumeForSwipe,
            enabled = !isSelectionMode && storageVolumes.size > 1,
            onSelectItem = { volume ->
              if (volume != null) {
                navigateTo(volume.file)
              }
            },
          )
          .pinchToChangeColumns(
            enabled = true,
            isListView = viewMode == FileViewMode.LIST,
            columnsPerRow = gridColumnsPerRow,
            minColumns = MIN_FILES_GRID_COLUMNS,
            maxColumns = MAX_FILES_GRID_COLUMNS,
            onColumnsChange = { cols ->
              val wasListView = viewMode == FileViewMode.LIST
              val previousCols = gridColumnsPerRow
              if (wasListView) {
                viewMode = FileViewMode.GRID
              }
              updateGridColumnsPerRow(cols)
              if (wasListView || previousCols != gridColumnsPerRow) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              }
            },
            onSelectListView = {
              if (viewMode != FileViewMode.LIST) {
                viewMode = FileViewMode.LIST
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              }
            },
          ),
      ) {
        if (processedItems.isEmpty()) {
          EmptyFilesView(
            isSearching = searchQuery.isNotEmpty(),
            searchQuery = searchQuery,
            hasPermission = hasPermission || isBrowsingSamples,
            onRequestPermission = { requestStoragePermission() },
            onCreateFolder = { showCreateFolderDialog = true },
            onCreateFile = { showCreateFileDialog = true },
          )
        } else {
          when (viewMode) {
            FileViewMode.LIST -> {
              LazyColumn(
                modifier = Modifier
                  .fillMaxSize()
                  .testTag("files_list_view"),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                groupedItems.forEach { group ->
                  val isCollapsed = group.title.isNotEmpty() && group.title in collapsedGroupTitles
                  if (group.title.isNotEmpty()) {
                    item(key = "header_${group.title}") {
                      FileGroupHeader(
                        title = group.title,
                        count = group.items.size,
                        isCollapsed = isCollapsed,
                        onToggleCollapse = {
                          collapsedGroupTitles = if (isCollapsed) {
                            collapsedGroupTitles - group.title
                          } else {
                            collapsedGroupTitles + group.title
                          }
                        },
                      )
                    }
                  }
                  if (!isCollapsed) {
                    items(
                      items = group.items,
                      key = { if (group.title.isEmpty()) it.path else "${group.title}:${it.path}" },
                    ) { item ->
                      FileListItem(
                        item = item,
                        isSelectionMode = isSelectionMode,
                        isSelected = item.path in selectedPaths,
                        onToggleSelect = {
                          selectedPaths = if (item.path in selectedPaths) {
                            selectedPaths - item.path
                          } else {
                            selectedPaths + item.path
                          }
                        },
                        onClick = {
                          if (isSelectionMode) {
                            selectedPaths = if (item.path in selectedPaths) {
                              selectedPaths - item.path
                            } else {
                              selectedPaths + item.path
                            }
                          } else {
                            if (item.isDirectory) {
                              navigateTo(item.file)
                            } else {
                              openFile(item.file, chooseApp = false)
                            }
                          }
                        },
                        onLongClick = {
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          if (!isSelectionMode) {
                            isSelectionMode = true
                            selectedPaths = setOf(item.path)
                          } else {
                            selectedPaths = if (item.path in selectedPaths) {
                              selectedPaths - item.path
                            } else {
                              selectedPaths + item.path
                            }
                          }
                        },
                        onOpenWith = { openFile(item.file, chooseApp = true) },
                        onOpenConverter = {
                          fileForConverter = item.file
                          onOpenFileConverter?.invoke(item.file)
                        },
                        onEditImage = {
                          fileForImageEditor = item.file
                          onOpenImageEditor?.invoke(item.file)
                        },
                        onEditVideo = {
                          fileForVideoEditor = item.file
                          onOpenVideoEditor?.invoke(item.file)
                        },
                        onEditAudio = {
                          fileForAudioEditor = item.file
                          onOpenAudioEditor?.invoke(item.file)
                        },
                        onOpenArchive = {
                          fileForArchive = item.file
                          onOpenArchive?.invoke(item.file)
                        },
                        onOpenPdf = {
                          fileForPdfViewer = item.file
                          onOpenPdfViewer?.invoke(item.file)
                        },
                        onPlayMusic = {
                          fileForMusicPlayer = item.file
                          onOpenMusicPlayer?.invoke(item.file)
                        },
                        onOpenGallery = {
                          fileForGallery = item.file
                          onOpenGallery?.invoke(item.file)
                        },
                        onInspectColor = {
                          inspectImageColor(item.file)
                        },
                        onSaveToNotes = {
                          saveFileToNotes(item.file)
                        },
                        onImportToTodo = {
                          importFileToTodo(item.file)
                        },
                        onOpenText = { fileForTextEdit = item },
                        onOpenImage = {
                          fileForImage = item.file
                          onOpenImage?.invoke(item.file)
                        },
                        onShare = {
                          if (item.isDirectory) {
                            shareMultipleItems(listOf(item.file))
                          } else {
                            shareFile(item.file)
                          }
                        },
                        onCut = {
                          clipboardState = ClipboardState(ClipboardOperation.CUT, listOf(item))
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          Toast.makeText(context, "\"${item.name}\" cut", Toast.LENGTH_SHORT).show()
                        },
                        onMove = {
                          clipboardState = ClipboardState(ClipboardOperation.MOVE, listOf(item))
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          Toast.makeText(context, "\"${item.name}\" ready to move", Toast.LENGTH_SHORT).show()
                        },
                        onCopy = {
                          clipboardState = ClipboardState(ClipboardOperation.COPY, listOf(item))
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          Toast.makeText(context, "\"${item.name}\" copied", Toast.LENGTH_SHORT).show()
                        },
                        onDuplicate = {
                          FileManagerEngine.duplicateFile(item.file)
                          refreshTrigger++
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onRename = { fileToRename = item },
                        onDetails = { fileForDetails = item },
                        onDelete = { fileToDelete = item },
                        onAddToHomeScreen = { addFolderToHomeScreen(context, item.file) },
                      )
                    }
                  }
                }
              }
            }
            FileViewMode.GRID -> {
              val coercedColumns = FileManagerEngine.coerceGridColumnsPerRow(gridColumnsPerRow)
              val isCompactGrid = coercedColumns >= 5
              LazyVerticalGrid(
                columns = GridCells.Fixed(coercedColumns),
                modifier = Modifier
                  .fillMaxSize()
                  .testTag("files_grid_view"),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(if (isCompactGrid) 6.dp else 8.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactGrid) 6.dp else 8.dp),
              ) {
                groupedItems.forEach { group ->
                  val isCollapsed = group.title.isNotEmpty() && group.title in collapsedGroupTitles
                  if (group.title.isNotEmpty()) {
                    item(
                      key = "header_${group.title}",
                      span = { GridItemSpan(maxLineSpan) },
                    ) {
                      FileGroupHeader(
                        title = group.title,
                        count = group.items.size,
                        isCollapsed = isCollapsed,
                        onToggleCollapse = {
                          collapsedGroupTitles = if (isCollapsed) {
                            collapsedGroupTitles - group.title
                          } else {
                            collapsedGroupTitles + group.title
                          }
                        },
                      )
                    }
                  }
                  if (!isCollapsed) {
                    items(
                      items = group.items,
                      key = { if (group.title.isEmpty()) it.path else "${group.title}:${it.path}" },
                    ) { item ->
                    FileGridItem(
                      item = item,
                      isCompact = isCompactGrid,
                      isSelectionMode = isSelectionMode,
                      isSelected = item.path in selectedPaths,
                      onToggleSelect = {
                        selectedPaths = if (item.path in selectedPaths) {
                          selectedPaths - item.path
                        } else {
                          selectedPaths + item.path
                        }
                      },
                      onClick = {
                        if (isSelectionMode) {
                          selectedPaths = if (item.path in selectedPaths) {
                            selectedPaths - item.path
                          } else {
                            selectedPaths + item.path
                          }
                        } else {
                          if (item.isDirectory) {
                            navigateTo(item.file)
                          } else {
                            openFile(item.file, chooseApp = false)
                          }
                        }
                      },
                      onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!isSelectionMode) {
                          isSelectionMode = true
                          selectedPaths = setOf(item.path)
                        } else {
                          selectedPaths = if (item.path in selectedPaths) {
                            selectedPaths - item.path
                          } else {
                            selectedPaths + item.path
                          }
                        }
                      },
                      onOpenWith = { openFile(item.file, chooseApp = true) },
                      onOpenConverter = {
                        fileForConverter = item.file
                        onOpenFileConverter?.invoke(item.file)
                      },
                      onEditImage = {
                        fileForImageEditor = item.file
                        onOpenImageEditor?.invoke(item.file)
                      },
                      onEditVideo = {
                        fileForVideoEditor = item.file
                        onOpenVideoEditor?.invoke(item.file)
                      },
                      onEditAudio = {
                        fileForAudioEditor = item.file
                        onOpenAudioEditor?.invoke(item.file)
                      },
                      onOpenArchive = {
                        fileForArchive = item.file
                        onOpenArchive?.invoke(item.file)
                      },
                      onOpenPdf = {
                        fileForPdfViewer = item.file
                        onOpenPdfViewer?.invoke(item.file)
                      },
                      onPlayMusic = {
                        fileForMusicPlayer = item.file
                        onOpenMusicPlayer?.invoke(item.file)
                      },
                      onOpenGallery = {
                        fileForGallery = item.file
                        onOpenGallery?.invoke(item.file)
                      },
                      onInspectColor = {
                        inspectImageColor(item.file)
                      },
                      onSaveToNotes = {
                        saveFileToNotes(item.file)
                      },
                      onImportToTodo = {
                        importFileToTodo(item.file)
                      },
                      onOpenText = { fileForTextEdit = item },
                      onOpenImage = {
                        fileForImage = item.file
                        onOpenImage?.invoke(item.file)
                      },
                      onShare = {
                        if (item.isDirectory) {
                          shareMultipleItems(listOf(item.file))
                        } else {
                          shareFile(item.file)
                        }
                      },
                      onCut = {
                        clipboardState = ClipboardState(ClipboardOperation.CUT, listOf(item))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "\"${item.name}\" cut", Toast.LENGTH_SHORT).show()
                      },
                      onMove = {
                        clipboardState = ClipboardState(ClipboardOperation.MOVE, listOf(item))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "\"${item.name}\" ready to move", Toast.LENGTH_SHORT).show()
                      },
                      onCopy = {
                        clipboardState = ClipboardState(ClipboardOperation.COPY, listOf(item))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "\"${item.name}\" copied", Toast.LENGTH_SHORT).show()
                      },
                      onDuplicate = {
                        FileManagerEngine.duplicateFile(item.file)
                        refreshTrigger++
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      },
                      onRename = { fileToRename = item },
                      onDetails = { fileForDetails = item },
                      onDelete = { fileToDelete = item },
                      onAddToHomeScreen = { addFolderToHomeScreen(context, item.file) },
                    )
                  }
                }
              }
            }
          }
          }
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

    // Speed dial FAB menu for adding/creating files and folders (hidden during multi-select)
    if (!isSelectionMode) {
      Column(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Mini FABs sliding out from underneath
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
            // Mini FAB: New File
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
              ) {
                isFabExpanded = false
                showCreateFileDialog = true
              },
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
              ) {
                Text(
                  text = "New file",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
              SmallFloatingActionButton(
                onClick = {
                  isFabExpanded = false
                  showCreateFileDialog = true
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.testTag("action_new_file"),
              ) {
                Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "New File")
              }
            }

            // Mini FAB: New Folder
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
              ) {
                isFabExpanded = false
                showCreateFolderDialog = true
              },
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
              ) {
                Text(
                  text = "New folder",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
              SmallFloatingActionButton(
                onClick = {
                  isFabExpanded = false
                  showCreateFolderDialog = true
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.testTag("action_new_folder"),
              ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
              }
            }
          }
        }

        // Main Add Button (rotates from + to × when expanded)
        val addIconRotation by animateFloatAsState(
          targetValue = if (isFabExpanded) 135f else 0f,
          label = "add_fab_rotation",
        )

        FloatingActionButton(
          onClick = { isFabExpanded = !isFabExpanded },
          shape = CircleShape,
          containerColor = if (isFabExpanded) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primaryContainer,
          contentColor = if (isFabExpanded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier
            .testTag("action_add")
            .testTag("action_add_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = if (isFabExpanded) "Close add menu" else "Add",
            modifier = Modifier.rotate(addIconRotation),
          )
        }
      }
    }

    // Floating Paste / Move Bar (shown when items are in clipboard to copy/cut/move)
    clipboardState?.let { clip ->
      val isMove = clip.operation == ClipboardOperation.MOVE
      val isCut = clip.operation == ClipboardOperation.CUT
      Surface(
        shape = if (currentHexagonIconButtons()) HorizontalHexagonShape else CircleShape,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .padding(
            start = 16.dp,
            end = if (isSelectionMode) 16.dp else 84.dp,
            bottom = 16.dp,
          )
          .height(56.dp)
          .testTag("files_paste_bar"),
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 8.dp)
            .let { mod -> if (isMove) mod.testTag("files_move_bar") else mod },
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector = when (clip.operation) {
              ClipboardOperation.CUT -> Icons.Default.ContentCut
              ClipboardOperation.MOVE -> Icons.AutoMirrored.Filled.DriveFileMove
              ClipboardOperation.COPY -> Icons.Default.ContentCopy
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = when (clip.operation) {
              ClipboardOperation.CUT -> "${clip.items.size} cut"
              ClipboardOperation.MOVE -> "${clip.items.size} to move"
              ClipboardOperation.COPY -> "${clip.items.size} copied"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          Box(
            modifier = if (isMove) Modifier.testTag("action_move_here") else Modifier,
          ) {
            Button(
              onClick = {
                val count = if (isCut || isMove) {
                  FileManagerEngine.moveMultipleFiles(clip.items.map { it.file }, currentDirectory)
                } else {
                  FileManagerEngine.copyMultipleFiles(clip.items.map { it.file }, currentDirectory)
                }
                clipboardState = null
                refreshTrigger++
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val toastText = if (isMove) {
                  "Moved $count items"
                } else {
                  "Pasted $count items"
                }
                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
              },
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
              modifier = Modifier.testTag("action_paste_here"),
            ) {
              Icon(
                imageVector = if (isMove) Icons.AutoMirrored.Filled.DriveFileMove else Icons.Default.ContentPaste,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isMove) "Move here" else "Paste here")
            }
          }
          IconButton(
            onClick = { clipboardState = null },
            modifier = Modifier
              .size(32.dp)
              .testTag(if (isMove) "action_cancel_move" else "action_cancel_paste"),
          ) {
            Icon(
              Icons.Default.Close,
              contentDescription = if (isMove) "Cancel move" else "Cancel paste",
            )
          }
        }
      }
    }

    // Dialogs & Viewers
    if (showCreateFolderDialog) {
      CreateFolderDialog(
        parentDir = currentDirectory,
        onDismiss = { showCreateFolderDialog = false },
        onConfirm = { name ->
          val result = FileManagerEngine.createFolder(currentDirectory, name)
          if (result.isSuccess) {
            refreshTrigger++
            showCreateFolderDialog = false
          } else {
            Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
          }
        },
      )
    }

    if (showCreateFileDialog) {
      CreateFileDialog(
        parentDir = currentDirectory,
        onDismiss = { showCreateFileDialog = false },
        onConfirm = { name, content ->
          val result = FileManagerEngine.createFile(currentDirectory, name, content)
          if (result.isSuccess) {
            refreshTrigger++
            showCreateFileDialog = false
          } else {
            Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
          }
        },
      )
    }

    fileToRename?.let { item ->
      RenameFileDialog(
        item = item,
        onDismiss = { fileToRename = null },
        onConfirm = { newName ->
          val result = FileManagerEngine.renameFile(item.file, newName)
          if (result.isSuccess) {
            refreshTrigger++
            fileToRename = null
          } else {
            Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
          }
        },
      )
    }

    fileToDelete?.let { item ->
      DeleteConfirmDialog(
        item = item,
        onDismiss = { fileToDelete = null },
        onConfirm = {
          FileManagerEngine.deleteFileOrDirectory(item.file)
          if (activeAudioPlaybackItem?.path == item.path) {
            activeAudioPlaybackItem = null
          }
          if (activeVideoPlaybackFile?.absolutePath == item.path) {
            activeVideoPlaybackFile = null
          }
          if (fileForTextEdit?.path == item.path) {
            fileForTextEdit = null
          }
          refreshTrigger++
          fileToDelete = null
        },
      )
    }

    activeAudioPlaybackItem?.let { recording ->
      MiniAudioPlayerModal(
        recording = recording,
        onDismiss = { activeAudioPlaybackItem = null },
      )
    }

    activeVideoPlaybackFile?.let { videoFile ->
      MiniVideoPlayerModal(
        file = videoFile,
        onDismiss = { activeVideoPlaybackFile = null },
      )
    }

    fileForDetails?.let { item ->
      FileDetailsDialog(
        item = item,
        onDismiss = { fileForDetails = null },
        onOpen = { openFile(item.file, chooseApp = false) },
        onOpenWith = { openFile(item.file, chooseApp = true) },
        onShare = { shareFile(item.file) },
      )
    }

    fileForTextEdit?.let { item ->
      TextEditorDialog(
        file = item.file,
        onDismiss = { fileForTextEdit = null },
        onSaved = {
          refreshTrigger++
        },
      )
    }

    if (showBulkInfoDialog) {
      BulkInfoDialog(
        items = selectedItems,
        onDismiss = { showBulkInfoDialog = false },
      )
    }

    if (showBulkDeleteDialog) {
      BulkDeleteConfirmDialog(
        items = selectedItems,
        onDismiss = { showBulkDeleteDialog = false },
        onConfirm = {
          val count = FileManagerEngine.deleteMultipleFiles(selectedItems.map { it.file })
          showBulkDeleteDialog = false
          isSelectionMode = false
          selectedPaths = emptySet()
          refreshTrigger++
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          Toast.makeText(context, "Deleted $count items", Toast.LENGTH_SHORT).show()
        },
      )
    }

    itemsToMove?.let { moveList ->
      MoveDestinationDialog(
        itemsToMove = moveList,
        initialDirectory = currentDirectory,
        storageVolumes = storageVolumes,
        onDismiss = { itemsToMove = null },
        onConfirmMove = { destinationDir ->
          val count = FileManagerEngine.moveMultipleFiles(moveList.map { it.file }, destinationDir)
          itemsToMove = null
          isSelectionMode = false
          selectedPaths = emptySet()
          refreshTrigger++
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          val label = if (count == 1) "1 item" else "$count items"
          Toast.makeText(context, "Moved $label to ${destinationDir.name.ifEmpty { destinationDir.absolutePath }}", Toast.LENGTH_SHORT).show()
        },
      )
    }
  }
}

// ---------------------------------------------------------
// Sub-components: Header, Storage Pill Buttons, Breadcrumbs
// ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilesHeader(
  currentDirectory: File,
  rootTitle: String = "Files",
  isSearchActive: Boolean,
  searchQuery: String,
  storageInfoSubtext: String,
  viewMode: FileViewMode,
  gridColumnsPerRow: Int = DEFAULT_FILES_GRID_COLUMNS,
  isSelectionMode: Boolean = false,
  selectedCount: Int = 0,
  totalCount: Int = 0,
  onToggleSelectAll: () -> Unit = {},
  onCloseSelection: () -> Unit = {},
  onBulkInfo: () -> Unit = {},
  onBulkCut: () -> Unit = {},
  onBulkMove: () -> Unit = {},
  onBulkCopy: () -> Unit = {},
  onBulkDelete: () -> Unit = {},
  onToggleSearch: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onToggleViewMode: () -> Unit,
  onSelectListView: () -> Unit = onToggleViewMode,
  onSelectGridColumns: (Int) -> Unit = { onToggleViewMode() },
  onNavigateUp: () -> Unit,
  canNavigateUp: Boolean,
  singleSelectedFolder: File? = null,
  singleSelectedFile: File? = null,
  onOpenSelectedFile: ((File, Boolean) -> Unit)? = null,
  onConvertSelectedFile: ((File) -> Unit)? = null,
  onOpenSelectedImage: ((File) -> Unit)? = null,
  onEditSelectedImage: ((File) -> Unit)? = null,
  onEditSelectedVideo: ((File) -> Unit)? = null,
  onEditSelectedAudio: ((File) -> Unit)? = null,
  onOpenSelectedArchive: ((File) -> Unit)? = null,
  onOpenSelectedPdf: ((File) -> Unit)? = null,
  onPlaySelectedMusic: ((File) -> Unit)? = null,
  onOpenSelectedGallery: ((File) -> Unit)? = null,
  onInspectSelectedColor: ((File) -> Unit)? = null,
  onSaveSelectedToNotes: ((File) -> Unit)? = null,
  onImportSelectedToTodo: ((File) -> Unit)? = null,
  onBulkShare: () -> Unit = {},
  onCompressSelectedItems: () -> Unit = {},
  onAddFolderToHomeScreen: ((File) -> Unit)? = null,
) {
  ToolboxTopBar(
    title = if (canNavigateUp) currentDirectory.name else rootTitle,
    subtext = storageInfoSubtext,
    icon = Icons.Default.FolderOpen,
    onNavigateBack = if (canNavigateUp) onNavigateUp else null,
    backContentDescription = "Up",
    backButtonTestTag = "action_navigate_up",
    searchState = ToolboxTopBarSearchState(
      isActive = isSearchActive,
      query = searchQuery,
      onQueryChange = onSearchQueryChange,
      onOpenSearch = onToggleSearch,
      onCloseSearch = onToggleSearch,
      placeholder = "Search files...",
      closeContentDescription = "Close Search",
      toggleButtonTestTag = "action_toggle_search",
      closeButtonTestTag = "action_close_search",
      inputTestTag = "files_search_input",
    ),
    selectionState = if (isSelectionMode) {
      ToolboxTopBarSelectionState(
        selectedCount = selectedCount,
        totalCount = totalCount,
        onToggleSelectAll = onToggleSelectAll,
        onCloseSelection = onCloseSelection,
        actions = {
          var showOverflowMenu by remember { mutableStateOf(false) }
          val hasSelection = selectedCount > 0

          // Info Icon Button
          IconButton(
            onClick = onBulkInfo,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_info"),
          ) {
            Icon(
              Icons.Default.Info,
              contentDescription = "Info",
              tint = if (hasSelection) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
              modifier = Modifier.size(20.dp),
            )
          }

          // Cut Icon Button
          IconButton(
            onClick = onBulkCut,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_cut"),
          ) {
            Icon(
              Icons.Default.ContentCut,
              contentDescription = "Cut",
              tint = if (hasSelection) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
              modifier = Modifier.size(20.dp),
            )
          }

          // Copy Icon Button
          IconButton(
            onClick = onBulkCopy,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_copy"),
          ) {
            Icon(
              Icons.Default.ContentCopy,
              contentDescription = "Copy",
              tint = if (hasSelection) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
              modifier = Modifier.size(20.dp),
            )
          }

          // Delete Icon Button in RED (enabled when selectedCount > 0)
          IconButton(
            onClick = onBulkDelete,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_delete"),
          ) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Delete",
              tint = if (hasSelection) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
              modifier = Modifier.size(20.dp),
            )
          }

          // Overflow "..." Menu for when space is constrained or for full labeled actions
          Box {
            IconButton(
              onClick = { showOverflowMenu = true },
              modifier = Modifier
                .size(36.dp)
                .testTag("action_selection_overflow"),
            ) {
              Icon(
                Icons.Default.MoreVert,
                contentDescription = "More actions",
                modifier = Modifier.size(20.dp),
              )
            }

            DropdownMenu(
              expanded = showOverflowMenu,
              onDismissRequest = { showOverflowMenu = false },
            ) {
              DropdownMenuItem(
                text = { Text("Info") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkInfo()
                },
              )
              DropdownMenuItem(
                text = { Text("Cut") },
                leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkCut()
                },
              )
              DropdownMenuItem(
                text = { Text("Move") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkMove()
                },
                modifier = Modifier.testTag("action_selection_move"),
              )
              DropdownMenuItem(
                text = { Text("Copy") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkCopy()
                },
              )
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
              if (singleSelectedFile != null && onOpenSelectedFile != null) {
                DropdownMenuItem(
                  text = { Text("Open with…") },
                  leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onOpenSelectedFile(singleSelectedFile, true)
                  },
                  modifier = Modifier.testTag("action_selection_open_with"),
                )
              }
              if (singleSelectedFile != null && onConvertSelectedFile != null && FileConverterEngine.isSupportedFile(singleSelectedFile)) {
                DropdownMenuItem(
                  text = { Text("Convert") },
                  leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onConvertSelectedFile(singleSelectedFile)
                  },
                  modifier = Modifier.testTag("action_selection_file_converter"),
                )
              }
              val archiveTarget = when {
                singleSelectedFolder != null -> singleSelectedFolder
                singleSelectedFile != null && ArchiveEngine.isSupportedArchive(singleSelectedFile) -> singleSelectedFile
                else -> null
              }
              if (archiveTarget != null && onOpenSelectedArchive != null) {
                DropdownMenuItem(
                  text = { Text("Archive") },
                  leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onOpenSelectedArchive(archiveTarget)
                  },
                  modifier = Modifier.testTag("action_selection_open_archive"),
                )
              } else if (hasSelection) {
                DropdownMenuItem(
                  text = { Text("Archive") },
                  leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onCompressSelectedItems()
                  },
                  modifier = Modifier.testTag("action_selection_open_archive"),
                )
              }
              if (singleSelectedFile != null && onOpenSelectedPdf != null && FileManagerEngine.isDocumentViewerFile(singleSelectedFile)) {
                DropdownMenuItem(
                  text = { Text("Document Viewer") },
                  leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onOpenSelectedPdf(singleSelectedFile)
                  },
                  modifier = Modifier.testTag("action_selection_open_pdf"),
                )
              }
              if (singleSelectedFile != null && onPlaySelectedMusic != null && FileManagerEngine.isAudioFile(singleSelectedFile) && !VideoEditorEngine.isVideoFile(singleSelectedFile)) {
                DropdownMenuItem(
                  text = { Text("Music") },
                  leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onPlaySelectedMusic(singleSelectedFile)
                  },
                  modifier = Modifier.testTag("action_selection_play_music"),
                )
              }
              if (singleSelectedFile != null && onOpenSelectedImage != null && FileManagerEngine.isImageFile(singleSelectedFile)) {
                DropdownMenuItem(
                  text = { Text("Viewer") },
                  leadingIcon = { Icon(Icons.Default.CropOriginal, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onOpenSelectedImage(singleSelectedFile)
                  },
                  modifier = Modifier.testTag("action_selection_open_image"),
                )
              }
              if (singleSelectedFile != null && onOpenSelectedGallery != null && (FileManagerEngine.isImageFile(singleSelectedFile) || VideoEditorEngine.isVideoFile(singleSelectedFile))) {
                DropdownMenuItem(
                  text = { Text("Gallery") },
                  leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onOpenSelectedGallery(singleSelectedFile)
                  },
                  modifier = Modifier.testTag("action_selection_open_gallery"),
                )
              }
              if (singleSelectedFile != null && FileManagerEngine.isTextFile(singleSelectedFile)) {
                if (onSaveSelectedToNotes != null) {
                  DropdownMenuItem(
                    text = { Text("Add Note") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.StickyNote2, contentDescription = null) },
                    onClick = {
                      showOverflowMenu = false
                      onSaveSelectedToNotes(singleSelectedFile)
                    },
                    modifier = Modifier.testTag("action_selection_save_to_notes"),
                  )
                }
              }
              if (singleSelectedFile != null && onEditSelectedImage != null && FileManagerEngine.isImageFile(singleSelectedFile)) {
                DropdownMenuItem(
                  text = { Text("Edit") },
                  leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onEditSelectedImage(singleSelectedFile)
                  },
                  modifier = Modifier.testTag("action_selection_edit_image"),
                )
              } else if (singleSelectedFile != null && onEditSelectedVideo != null && VideoEditorEngine.isVideoFile(singleSelectedFile)) {
                DropdownMenuItem(
                  text = { Text("Edit") },
                  leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onEditSelectedVideo(singleSelectedFile)
                  },
                  modifier = Modifier.testTag("action_selection_edit_video"),
                )
              } else if (singleSelectedFile != null && onEditSelectedAudio != null && AudioEditorEngine.isSupportedAudioFile(singleSelectedFile)) {
                DropdownMenuItem(
                  text = { Text("Edit") },
                  leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onEditSelectedAudio(singleSelectedFile)
                  },
                  modifier = Modifier.testTag("action_selection_edit_audio"),
                )
              }
              if (singleSelectedFolder != null && onOpenSelectedGallery != null) {
                DropdownMenuItem(
                  text = { Text("Gallery") },
                  leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onOpenSelectedGallery(singleSelectedFolder)
                  },
                  modifier = Modifier.testTag("action_selection_open_gallery"),
                )
              }
              if (singleSelectedFolder != null && onPlaySelectedMusic != null) {
                DropdownMenuItem(
                  text = { Text("Music") },
                  leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onPlaySelectedMusic(singleSelectedFolder)
                  },
                  modifier = Modifier.testTag("action_selection_open_music"),
                )
              }
              if (singleSelectedFolder != null && onAddFolderToHomeScreen != null) {
                DropdownMenuItem(
                  text = { Text("Add to Home") },
                  leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                  onClick = {
                    showOverflowMenu = false
                    onAddFolderToHomeScreen(singleSelectedFolder)
                  },
                  modifier = Modifier.testTag("action_selection_add_to_home"),
                )
              }
              DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkDelete()
                },
              )
              HorizontalDivider()
              DropdownMenuItem(
                text = { Text(if (selectedCount == totalCount && totalCount > 0) "Deselect all" else "Select all") },
                leadingIcon = { Icon(Icons.Default.SelectAll, contentDescription = null) },
                onClick = {
                  showOverflowMenu = false
                  onToggleSelectAll()
                },
              )
              DropdownMenuItem(
                text = { Text("Exit selection") },
                leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                onClick = {
                  showOverflowMenu = false
                  onCloseSelection()
                },
              )
            }
          }
        },
      )
    } else {
      null
    },
    actions = {
      ToolboxViewModeCycleButton(
        isListView = viewMode == FileViewMode.LIST,
        columnsPerRow = gridColumnsPerRow,
        minColumns = MIN_FILES_GRID_COLUMNS,
        maxColumns = MAX_FILES_GRID_COLUMNS,
        onSelectListView = onSelectListView,
        onSelectGridColumns = onSelectGridColumns,
        buttonTestTag = "action_toggle_view_mode",
        countTestTag = "files_columns_per_row_count",
        gridContentDescription = "$gridColumnsPerRow files per row",
      )
    },
  )
}

/**
 * Storage device pill buttons row with a pinned floating sort/group icon on the right.
 * Displays a pill button for storage/emulated/0 (the user's root folder), and dynamically
 * displays new pill buttons with the name of each connected external storage device (SD card, USB OTG, etc.).
 */
@Composable
private fun StorageDevicePillRow(
  storageVolumes: List<StorageVolumeInfo>,
  activeVolume: StorageVolumeInfo?,
  currentDirectory: File? = null,
  hasCustomRoot: Boolean = false,
  viewMode: FileViewMode = FileViewMode.LIST,
  gridColumnsPerRow: Int = DEFAULT_FILES_GRID_COLUMNS,
  onGridColumnsChange: (Int) -> Unit = {},
  sortOrder: FileSortOrder = FileSortOrder.NAME_ASC,
  groupOption: FileGroupOption = FileGroupOption.TYPE,
  showSortMenu: Boolean = false,
  isSortGroupSavedForFolder: Boolean = false,
  onSelectVolume: (StorageVolumeInfo) -> Unit,
  onOpenSortMenu: () -> Unit = {},
  onDismissSortMenu: () -> Unit = {},
  onSelectSortOrder: (FileSortOrder) -> Unit = {},
  onSelectGroupOption: (FileGroupOption) -> Unit = {},
  onToggleSaveSortGroupForFolder: () -> Unit = {},
) {
  ToolboxPillNavigationBar(
    barTestTag = "files_pill_bar",
    floatingButtonsCount = 1,
    pills = {
      storageVolumes.forEach { volume ->
        val isSelected = if (hasCustomRoot && currentDirectory != null) {
          FileManagerEngine.isAtRootDirectory(currentDirectory, volume.file)
        } else {
          activeVolume?.id == volume.id
        }
        val icon = when {
          volume.isPrimary && hasCustomRoot -> Icons.Default.Folder
          volume.isPrimary -> Icons.Default.Home
          volume.id == FileManagerEngine.SAMPLES_VOLUME_ID ||
            volume.name.equals(FileManagerEngine.SAMPLES_FOLDER_NAME, ignoreCase = true) -> Icons.Default.Folder
          volume.name.contains("usb", ignoreCase = true) -> Icons.Default.Usb
          else -> Icons.Default.SdCard
        }

        ToolboxPillNavigationItem(
          selected = isSelected,
          onClick = { onSelectVolume(volume) },
          leadingIcon = icon,
          label = volume.name,
          testTag = "storage_pill_${volume.id}",
        )
      }
    },
    floatingControls = {
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
            .testTag("action_sort_menu"),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Sort and group options",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
        }

        DropdownMenu(
          expanded = showSortMenu,
          onDismissRequest = onDismissSortMenu,
          modifier = Modifier
            .widthIn(min = 320.dp, max = 390.dp)
            .testTag("sort_and_group_menu"),
        ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp),
        ) {
          // Left panel: GROUP BY
          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(),
          ) {
            Text(
              text = "GROUP BY",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
            FileGroupOption.entries.forEach { option ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = option.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (option == groupOption) FontWeight.Bold else FontWeight.Normal,
                    color = if (option == groupOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                  )
                },
                trailingIcon = if (option == groupOption) {
                  {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                } else null,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                onClick = { onSelectGroupOption(option) },
                modifier = Modifier.testTag("group_option_${option.name.lowercase()}"),
              )
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            )

            // Bottom-left checkbox to save sort/group for this specific folder
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onToggleSaveSortGroupForFolder() }
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("save_sort_group_for_folder"),
            ) {
              Checkbox(
                checked = isSortGroupSavedForFolder,
                onCheckedChange = { onToggleSaveSortGroupForFolder() },
                colors = CheckboxDefaults.colors(
                  checkedColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                  .size(28.dp)
                  .testTag("save_sort_group_for_folder_checkbox"),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Save for folder",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSortGroupSavedForFolder) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSortGroupSavedForFolder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          // Vertical divider between Group By and Sort By panels
          VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 6.dp),
          )

          // Right panel: SORT BY
          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(),
          ) {
            Text(
              text = "SORT BY",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
            FileSortOrder.entries.forEach { order ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = order.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal,
                    color = if (order == sortOrder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                  )
                },
                trailingIcon = if (order == sortOrder) {
                  {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                } else null,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                onClick = { onSelectSortOrder(order) },
                modifier = Modifier.testTag("sort_order_${order.name.lowercase()}"),
              )
            }
            }
          }
        }
      }
    },
  )
}

@Composable
private fun StoragePermissionBanner(
  onRequestPermission: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 4.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
    ),
    shape = RoundedCornerShape(12.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        Icons.Default.FolderShared,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(28.dp),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Storage Permission Required",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "Allow access to manage files in storage/emulated/0",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Button(
        onClick = onRequestPermission,
        modifier = Modifier.padding(start = 6.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
        ),
      ) {
        Text("Grant", fontSize = 12.sp)
      }
    }
  }
}

/**
 * Section header displaying group title (Folders, Images, Documents, etc.) with count badge
 * and collapsible toggle with animated chevron.
 */
@Composable
private fun FileGroupHeader(
  title: String,
  count: Int,
  isCollapsed: Boolean = false,
  onToggleCollapse: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val rotationState by animateFloatAsState(
    targetValue = if (isCollapsed) -90f else 0f,
    label = "group_expand_rotation",
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("file_group_header_${title.lowercase().replace(" ", "_")}"),
    shape = RoundedCornerShape(8.dp),
    color = Color.Transparent,
    onClick = onToggleCollapse,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        ) {
          Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
          )
        }
      }

      Icon(
        imageVector = Icons.Default.ExpandMore,
        contentDescription = if (isCollapsed) "Expand $title" else "Collapse $title",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .size(20.dp)
          .rotate(rotationState),
      )
    }
  }
}

@Composable
private fun BreadcrumbBar(
  currentDirectory: File,
  volumeRoot: File,
  storageSummary: Pair<Long, Int>?,
  onNavigateToDir: (File) -> Unit,
) {
  val pathAncestors = remember(currentDirectory, volumeRoot) {
    val list = mutableListOf<File>()
    var curr: File? = currentDirectory
    while (curr != null) {
      list.add(0, curr)
      if (curr.absolutePath == volumeRoot.absolutePath) break
      curr = curr.parentFile
    }
    list
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier
        .weight(1f)
        .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      pathAncestors.takeLast(4).forEachIndexed { idx, dir ->
        if (idx > 0) {
          Text(" / ", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        val isRoot = dir.absolutePath == volumeRoot.absolutePath
        val label = if (isRoot) {
          if (dir.absolutePath == "/storage/emulated/0" || volumeRoot.absolutePath.endsWith("emulated/0")) "Home" else dir.name.ifEmpty { "Home" }
        } else {
          dir.name
        }

        Text(
          text = label,
          color = if (dir == currentDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = if (dir == currentDirectory) FontWeight.Bold else FontWeight.Normal,
          fontSize = 12.sp,
          modifier = Modifier.clickable { onNavigateToDir(dir) },
        )
      }
    }

    if (storageSummary != null) {
      val (totalBytes, itemCount) = storageSummary
      Text(
        text = "$itemCount items • ${FileManagerEngine.formatFileSize(totalBytes)}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

// ---------------------------------------------------------
// File List & Grid Items
// ---------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
  item: FileItem,
  isSelectionMode: Boolean = false,
  isSelected: Boolean = false,
  onToggleSelect: () -> Unit = {},
  onClick: () -> Unit,
  onLongClick: () -> Unit = {},
  onOpenWith: () -> Unit = {},
  onOpenConverter: () -> Unit = {},
  onEditImage: () -> Unit = {},
  onEditVideo: () -> Unit = {},
  onEditAudio: () -> Unit = {},
  onOpenArchive: () -> Unit = {},
  onOpenPdf: () -> Unit = {},
  onPlayMusic: () -> Unit = {},
  onOpenGallery: () -> Unit = {},
  onInspectColor: () -> Unit = {},
  onSaveToNotes: () -> Unit = {},
  onImportToTodo: () -> Unit = {},
  onOpenText: () -> Unit,
  onOpenImage: () -> Unit,
  onShare: () -> Unit,
  onCut: () -> Unit = {},
  onMove: () -> Unit = {},
  onCopy: () -> Unit = {},
  onDuplicate: () -> Unit,
  onRename: () -> Unit,
  onDetails: () -> Unit,
  onDelete: () -> Unit,
  onAddToHomeScreen: () -> Unit = {},
) {
  var showMenu by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = {
          if (isSelectionMode) {
            onToggleSelect()
          } else {
            onClick()
          }
        },
        onLongClick = onLongClick,
      )
      .testTag("file_item_${item.name}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = if (isSelected) {
      BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else null,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (isSelectionMode) {
        Box(
          modifier = Modifier.size(40.dp),
          contentAlignment = Alignment.Center,
        ) {
          Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelect() },
            colors = CheckboxDefaults.colors(
              checkedColor = MaterialTheme.colorScheme.primary,
            ),
          )
        }
      } else {
        FileIconBox(item = item, size = 40.dp)
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.name,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (item.isDirectory) {
              val count = "${item.childCount ?: 0} items"
              val size = FileManagerEngine.formatFileSize(item.sizeBytes)
              "$count • $size"
            } else {
              FileManagerEngine.formatFileSize(item.sizeBytes)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
          Text(
            text = FileManagerEngine.formatTimestamp(item.lastModifiedMs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (!isSelectionMode) {
        Box {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier
              .size(36.dp)
              .testTag("file_options_${item.name}"),
          ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
          }

          FileActionMenu(
            item = item,
            expanded = showMenu,
            onDismiss = { showMenu = false },
            onOpenWith = {
              showMenu = false
              onOpenWith()
            },
            onOpenConverter = {
              showMenu = false
              onOpenConverter()
            },
            onEditImage = {
              showMenu = false
              onEditImage()
            },
            onEditVideo = {
              showMenu = false
              onEditVideo()
            },
            onEditAudio = {
              showMenu = false
              onEditAudio()
            },
            onOpenArchive = {
              showMenu = false
              onOpenArchive()
            },
            onOpenPdf = {
              showMenu = false
              onOpenPdf()
            },
            onPlayMusic = {
              showMenu = false
              onPlayMusic()
            },
            onOpenGallery = {
              showMenu = false
              onOpenGallery()
            },
            onInspectColor = {
              showMenu = false
              onInspectColor()
            },
            onSaveToNotes = {
              showMenu = false
              onSaveToNotes()
            },
            onImportToTodo = {
              showMenu = false
              onImportToTodo()
            },
            onOpenText = {
              showMenu = false
              onOpenText()
            },
            onOpenImage = {
              showMenu = false
              onOpenImage()
            },
            onShare = {
              showMenu = false
              onShare()
            },
            onCut = {
              showMenu = false
              onCut()
            },
            onMove = {
              showMenu = false
              onMove()
            },
            onCopy = {
              showMenu = false
              onCopy()
            },
            onDuplicate = {
              showMenu = false
              onDuplicate()
            },
            onRename = {
              showMenu = false
              onRename()
            },
            onDetails = {
              showMenu = false
              onDetails()
            },
            onDelete = {
              showMenu = false
              onDelete()
            },
            onAddToHomeScreen = {
              showMenu = false
              onAddToHomeScreen()
            },
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileGridItem(
  item: FileItem,
  isCompact: Boolean = false,
  isSelectionMode: Boolean = false,
  isSelected: Boolean = false,
  onToggleSelect: () -> Unit = {},
  onClick: () -> Unit,
  onLongClick: () -> Unit = {},
  onOpenWith: () -> Unit = {},
  onOpenConverter: () -> Unit = {},
  onEditImage: () -> Unit = {},
  onEditVideo: () -> Unit = {},
  onEditAudio: () -> Unit = {},
  onOpenArchive: () -> Unit = {},
  onOpenPdf: () -> Unit = {},
  onPlayMusic: () -> Unit = {},
  onOpenGallery: () -> Unit = {},
  onInspectColor: () -> Unit = {},
  onSaveToNotes: () -> Unit = {},
  onImportToTodo: () -> Unit = {},
  onOpenText: () -> Unit = {},
  onOpenImage: () -> Unit = {},
  onShare: () -> Unit,
  onCut: () -> Unit = {},
  onMove: () -> Unit = {},
  onCopy: () -> Unit = {},
  onDuplicate: () -> Unit,
  onRename: () -> Unit,
  onDetails: () -> Unit,
  onDelete: () -> Unit,
  onAddToHomeScreen: () -> Unit = {},
) {
  var showMenu by remember { mutableStateOf(false) }
  val iconBoxSize = if (isCompact) 32.dp else 44.dp

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(0.92f)
      .combinedClickable(
        onClick = {
          if (isSelectionMode) {
            onToggleSelect()
          } else {
            onClick()
          }
        },
        onLongClick = onLongClick,
      )
      .testTag("file_grid_item_${item.name}"),
    shape = RoundedCornerShape(if (isCompact) 10.dp else 14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = if (isSelected) {
      BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else null,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(if (isCompact) 4.dp else 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        if (isSelectionMode) {
          Box(
            modifier = Modifier.size(iconBoxSize),
            contentAlignment = Alignment.Center,
          ) {
            Checkbox(
              checked = isSelected,
              onCheckedChange = { onToggleSelect() },
              colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
              ),
            )
          }
        } else {
          FileIconBox(item = item, size = iconBoxSize)
        }

        Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

        Text(
          text = item.name,
          style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        if (!isCompact) {
          Text(
            text = if (item.isDirectory) {
              "${item.childCount ?: 0} items • ${FileManagerEngine.formatFileSize(item.sizeBytes)}"
            } else {
              FileManagerEngine.formatFileSize(item.sizeBytes)
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
          )
        }
      }

      if (!isSelectionMode) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(2.dp)
        ) {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier
              .size(28.dp)
              .testTag("file_grid_options_${item.name}"),
          ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(16.dp))
          }

          FileActionMenu(
            item = item,
            expanded = showMenu,
            onDismiss = { showMenu = false },
            onOpenWith = {
              showMenu = false
              onOpenWith()
            },
            onOpenConverter = {
              showMenu = false
              onOpenConverter()
            },
            onEditImage = {
              showMenu = false
              onEditImage()
            },
            onEditVideo = {
              showMenu = false
              onEditVideo()
            },
            onEditAudio = {
              showMenu = false
              onEditAudio()
            },
            onOpenArchive = {
              showMenu = false
              onOpenArchive()
            },
            onOpenPdf = {
              showMenu = false
              onOpenPdf()
            },
            onPlayMusic = {
              showMenu = false
              onPlayMusic()
            },
            onOpenGallery = {
              showMenu = false
              onOpenGallery()
            },
            onInspectColor = {
              showMenu = false
              onInspectColor()
            },
            onSaveToNotes = {
              showMenu = false
              onSaveToNotes()
            },
            onImportToTodo = {
              showMenu = false
              onImportToTodo()
            },
            onOpenText = {
              showMenu = false
              onOpenText()
            },
            onOpenImage = {
              showMenu = false
              onOpenImage()
            },
            onShare = {
              showMenu = false
              onShare()
            },
            onCut = {
              showMenu = false
              onCut()
            },
            onMove = {
              showMenu = false
              onMove()
            },
            onCopy = {
              showMenu = false
              onCopy()
            },
            onDuplicate = {
              showMenu = false
              onDuplicate()
            },
            onRename = {
              showMenu = false
              onRename()
            },
            onDetails = {
              showMenu = false
              onDetails()
            },
            onDelete = {
              showMenu = false
              onDelete()
            },
            onAddToHomeScreen = {
              showMenu = false
              onAddToHomeScreen()
            },
          )
        }
      }
    }
  }
}

@Composable
private fun FileIconBox(item: FileItem, size: androidx.compose.ui.unit.Dp) {
  val (icon, bg, fg) = when {
    item.isDirectory -> Triple(Icons.Default.Folder, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
    FileManagerEngine.isImageFile(item.file) -> Triple(Icons.Default.Image, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary)
    FileManagerEngine.isCodeFile(item.file) -> Triple(Icons.Default.Code, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary)
    FileManagerEngine.isAudioFile(item.file) -> Triple(Icons.Default.AudioFile, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
    FileManagerEngine.isVideoFile(item.file) -> Triple(Icons.Default.VideoFile, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
    FileManagerEngine.isArchiveFile(item.file) -> Triple(Icons.Default.FolderZip, MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurface)
    FileManagerEngine.isPdfFile(item.file) -> Triple(Icons.Default.PictureAsPdf, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
    FileManagerEngine.isTextFile(item.file) -> Triple(Icons.Default.Description, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
    else -> Triple(Icons.AutoMirrored.Filled.InsertDriveFile, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
  }

  Box(
    modifier = Modifier
      .size(size)
      .background(bg, RoundedCornerShape(10.dp)),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = fg,
      modifier = Modifier.size(size * 0.58f),
    )
  }
}

@Composable
private fun FileActionMenu(
  item: FileItem,
  expanded: Boolean,
  onDismiss: () -> Unit,
  onOpenWith: () -> Unit = {},
  onOpenConverter: () -> Unit = {},
  onEditImage: () -> Unit = {},
  onEditVideo: () -> Unit = {},
  onEditAudio: () -> Unit = {},
  onOpenArchive: () -> Unit = {},
  onOpenPdf: () -> Unit = {},
  onPlayMusic: () -> Unit = {},
  onOpenGallery: () -> Unit = {},
  onInspectColor: () -> Unit = {},
  onSaveToNotes: () -> Unit = {},
  onImportToTodo: () -> Unit = {},
  onOpenText: () -> Unit,
  onOpenImage: () -> Unit,
  onShare: () -> Unit,
  onCut: () -> Unit = {},
  onMove: () -> Unit = {},
  onCopy: () -> Unit = {},
  onDuplicate: () -> Unit,
  onRename: () -> Unit,
  onDetails: () -> Unit,
  onDelete: () -> Unit,
  onAddToHomeScreen: () -> Unit = {},
) {
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismiss,
  ) {
    if (!item.isDirectory) {
      DropdownMenuItem(
        text = { Text("Open with…") },
        leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) },
        onClick = onOpenWith,
        modifier = Modifier.testTag("action_open_with"),
      )
      if (FileConverterEngine.isSupportedFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Convert") },
          leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
          onClick = onOpenConverter,
          modifier = Modifier.testTag("action_file_converter"),
        )
      }
      if (ArchiveEngine.isSupportedArchive(item.file)) {
        DropdownMenuItem(
          text = { Text("Archive") },
          leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
          onClick = onOpenArchive,
          modifier = Modifier.testTag("action_open_archive"),
        )
      }
      if (FileManagerEngine.isDocumentViewerFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Document Viewer") },
          leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
          onClick = onOpenPdf,
          modifier = Modifier.testTag("action_open_pdf"),
        )
      }
      if (FileManagerEngine.isImageFile(item.file)) {
        Box(modifier = Modifier.testTag("action_preview_image")) {
          DropdownMenuItem(
            text = { Text("Viewer") },
            leadingIcon = { Icon(Icons.Default.CropOriginal, contentDescription = null) },
            onClick = onOpenImage,
            modifier = Modifier.testTag("action_open_image"),
          )
        }
        DropdownMenuItem(
          text = { Text("Edit") },
          leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
          onClick = onEditImage,
          modifier = Modifier.testTag("action_edit_image"),
        )
        DropdownMenuItem(
          text = { Text("Gallery") },
          leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
          onClick = onOpenGallery,
          modifier = Modifier.testTag("action_open_gallery"),
        )
      } else if (AudioEditorEngine.isSupportedAudioFile(item.file) && !VideoEditorEngine.isVideoFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Music") },
          leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null) },
          onClick = onPlayMusic,
          modifier = Modifier.testTag("action_play_music"),
        )
        DropdownMenuItem(
          text = { Text("Edit") },
          leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
          onClick = onEditAudio,
          modifier = Modifier.testTag("action_edit_audio"),
        )
      } else if (FileManagerEngine.isAudioFile(item.file) && !VideoEditorEngine.isVideoFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Music") },
          leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null) },
          onClick = onPlayMusic,
          modifier = Modifier.testTag("action_play_music"),
        )
      } else if (VideoEditorEngine.isVideoFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Edit") },
          leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
          onClick = onEditVideo,
          modifier = Modifier.testTag("action_edit_video"),
        )
        DropdownMenuItem(
          text = { Text("Gallery") },
          leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
          onClick = onOpenGallery,
          modifier = Modifier.testTag("action_open_gallery"),
        )
      } else if (FileManagerEngine.isTextFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Add Note") },
          leadingIcon = { Icon(Icons.AutoMirrored.Filled.StickyNote2, contentDescription = null) },
          onClick = onSaveToNotes,
          modifier = Modifier.testTag("action_save_to_notes"),
        )
      }
      DropdownMenuItem(
        text = { Text("Share") },
        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
        onClick = onShare,
        modifier = Modifier.testTag("action_share"),
      )
    }

    if (item.isDirectory) {
      DropdownMenuItem(
        text = { Text("Gallery") },
        leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
        onClick = onOpenGallery,
        modifier = Modifier.testTag("action_open_gallery"),
      )
      DropdownMenuItem(
        text = { Text("Music") },
        leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null) },
        onClick = onPlayMusic,
        modifier = Modifier.testTag("action_open_music"),
      )
      DropdownMenuItem(
        text = { Text("Archive") },
        leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
        onClick = onOpenArchive,
        modifier = Modifier.testTag("action_open_archive"),
      )
      DropdownMenuItem(
        text = { Text("Add to Home") },
        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
        onClick = onAddToHomeScreen,
        modifier = Modifier.testTag("action_add_to_home_screen"),
      )
      DropdownMenuItem(
        text = { Text("Share") },
        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
        onClick = onShare,
        modifier = Modifier.testTag("action_share"),
      )
    }

    HorizontalDivider()
    DropdownMenuItem(
      text = { Text("Cut") },
      leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
      onClick = onCut,
      modifier = Modifier.testTag("action_cut"),
    )
    DropdownMenuItem(
      text = { Text("Move") },
      leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null) },
      onClick = onMove,
      modifier = Modifier.testTag("action_move"),
    )
    DropdownMenuItem(
      text = { Text("Copy") },
      leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
      onClick = onCopy,
      modifier = Modifier.testTag("action_copy"),
    )
    DropdownMenuItem(
      text = { Text("Rename") },
      leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
      onClick = onRename,
      modifier = Modifier.testTag("action_rename"),
    )
    DropdownMenuItem(
      text = { Text("Info") },
      leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
      onClick = onDetails,
      modifier = Modifier.testTag("action_details"),
    )
    HorizontalDivider()
    DropdownMenuItem(
      text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
      leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
      onClick = onDelete,
      modifier = Modifier.testTag("action_delete"),
    )
  }
}

@Composable
private fun EmptyFilesView(
  isSearching: Boolean,
  searchQuery: String,
  hasPermission: Boolean,
  onRequestPermission: () -> Unit,
  onCreateFolder: () -> Unit,
  onCreateFile: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier = Modifier
        .size(80.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        Icons.Default.FolderOpen,
        contentDescription = null,
        modifier = Modifier.size(44.dp),
        tint = MaterialTheme.colorScheme.primary,
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = if (isSearching) "No matches for \"$searchQuery\"" else "This folder is empty",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = if (!hasPermission) {
        "Storage permission is needed to view all files"
      } else if (isSearching) {
        "Try another search term"
      } else {
        "Create a new folder or file to get started"
      },
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(20.dp))

    if (!hasPermission) {
      Button(onClick = onRequestPermission) {
        Icon(Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Grant Storage Access")
      }
    } else if (!isSearching) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(onClick = onCreateFile) {
          Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("File")
        }
        Button(onClick = onCreateFolder) {
          Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Folder")
        }
      }
    }
  }
}

// ---------------------------------------------------------
// Dialogs: Create Folder, Create File, Rename, Delete, Details
// ---------------------------------------------------------

@Composable
private fun CreateFolderDialog(
  parentDir: File,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  val validationError = remember(name) {
    if (name.isEmpty()) null else FileManagerEngine.validateFileName(name, parentDir)
  }

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
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "New Folder",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Create in ${parentDir.name.ifEmpty { parentDir.absolutePath }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Column {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Folder Name") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          isError = validationError != null,
          supportingText = {
            if (validationError != null) {
              Text(validationError, color = MaterialTheme.colorScheme.error)
            }
          },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name.trim()) },
        enabled = name.trim().isNotEmpty() && validationError == null,
        shape = CircleShape,
      ) {
        Text("Create")
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

@Composable
private fun CreateFileDialog(
  parentDir: File,
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var initialContent by remember { mutableStateOf("") }
  val validationError = remember(name) {
    if (name.isEmpty()) null else FileManagerEngine.validateFileName(name, parentDir)
  }

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
            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "New File",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Create in ${parentDir.name.ifEmpty { parentDir.absolutePath }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("File Name (e.g. notes.txt)") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          isError = validationError != null,
          supportingText = {
            if (validationError != null) {
              Text(validationError, color = MaterialTheme.colorScheme.error)
            }
          },
          modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick extension suggestions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf(".txt", ".text", ".md", ".json", ".log").forEach { ext ->
            FilterChip(
              selected = name.endsWith(ext),
              onClick = {
                val base = if (name.contains(".")) name.substringBeforeLast(".") else name.ifEmpty { "file" }
                name = "$base$ext"
              },
              label = { Text(ext, fontSize = 11.sp) },
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = initialContent,
          onValueChange = { initialContent = it },
          label = { Text("Initial Content (Optional)") },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
          maxLines = 4,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name.trim(), initialContent) },
        enabled = name.trim().isNotEmpty() && validationError == null,
        shape = CircleShape,
      ) {
        Text("Create")
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

@Composable
internal fun RenameFileDialog(
  item: FileItem,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  val isFolder = item.isDirectory
  val initialStem = if (!isFolder && item.name.contains(".")) {
    item.name.substringBeforeLast(".")
  } else {
    item.name
  }
  val extension = if (!isFolder && item.name.contains(".")) {
    ".${item.name.substringAfterLast(".")}"
  } else ""

  // Use TextFieldValue to pre-select just the filename stem, leaving extension unselected
  var textFieldValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = item.name,
        selection = if (!isFolder && initialStem.isNotEmpty()) {
          TextRange(0, initialStem.length)
        } else {
          TextRange(0, item.name.length)
        },
      )
    )
  }

  val focusRequester = remember { FocusRequester() }
  val currentText = textFieldValue.text
  val validationError = remember(currentText) {
    if (currentText == item.name) null else FileManagerEngine.validateFileName(currentText, item.file.parentFile)
  }

  val isChanged = currentText.trim() != item.name
  val isValid = currentText.trim().isNotEmpty() && validationError == null
  val canConfirm = isChanged && isValid

  // Show a helper if the file originally had an extension, but the user stripped it
  val missingExtensionWarning = remember(currentText) {
    if (!isFolder && extension.isNotEmpty() && !currentText.endsWith(extension)) {
      "Missing extension $extension"
    } else null
  }

  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(100)
    focusRequester.requestFocus()
  }

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
            text = if (isFolder) "Rename Folder" else "Rename File",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Enter a new name for this item",
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
        // Current File / Folder Preview Card
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
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            FileIconBox(item = item, size = 38.dp)
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
                text = if (isFolder) {
                  "${item.childCount ?: 0} items • ${FileManagerEngine.formatFileSize(item.sizeBytes)}"
                } else {
                  "${FileManagerEngine.formatFileSize(item.sizeBytes)} • ${item.extension.uppercase(Locale.ROOT).ifEmpty { "FILE" }}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        // Text Field Input
        OutlinedTextField(
          value = textFieldValue,
          onValueChange = { textFieldValue = it },
          label = { Text("New name") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          isError = validationError != null,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              if (canConfirm) onConfirm(currentText.trim())
            }
          ),
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = null,
              tint = if (validationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
          },
          trailingIcon = {
            if (currentText.isNotEmpty()) {
              IconButton(onClick = { textFieldValue = TextFieldValue("") }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear",
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          },
          supportingText = {
            if (validationError != null) {
              Text(
                text = validationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
              )
            } else if (missingExtensionWarning != null) {
              Text(
                text = missingExtensionWarning,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
              )
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        )

        // Quick extension restoration badge if extension was stripped
        if (missingExtensionWarning != null) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            modifier = Modifier
              .clickable {
                val restored = currentText.trim() + extension
                textFieldValue = TextFieldValue(
                  text = restored,
                  selection = TextRange(restored.length),
                )
              }
              .fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = "Tap to restore extension $extension",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(currentText.trim()) },
        enabled = canConfirm,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Rename")
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

@Composable
internal fun DeleteConfirmDialog(
  item: FileItem,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
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
            text = "Delete ${if (item.isDirectory) "Folder" else "File"}?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Text("Are you sure you want to delete \"${item.name}\"? This action cannot be undone.")
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
      ) {
        Text("Delete")
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

@Composable
internal fun BulkInfoDialog(
  items: List<FileItem>,
  onDismiss: () -> Unit,
) {
  val info = remember(items) { FileManagerEngine.getBulkStorageInfo(items) }

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
            text = "${items.size} ${if (items.size == 1) "item" else "items"} selected",
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
            DetailRow("Total Size", info.formattedSize)
            DetailRow("Exact Bytes", "${info.totalSizeBytes} bytes")
            DetailRow("Breakdown", "${info.folderCount} folders, ${info.fileCount} files")
          }
        }

        Text(
          text = "Selected Items",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp),
        )

        items.forEach { item ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FileIconBox(item = item, size = 28.dp)
            Text(
              text = item.name,
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )
            Text(
              text = if (item.isDirectory) "${item.childCount ?: 0} items" else FileManagerEngine.formatFileSize(item.sizeBytes),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
internal fun BulkDeleteConfirmDialog(
  items: List<FileItem>,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  val info = remember(items) { FileManagerEngine.getBulkStorageInfo(items) }

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
            text = "Delete ${items.size} ${if (items.size == 1) "item" else "items"}?",
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
          text = "Are you sure you want to permanently delete these items? This action cannot be undone.",
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
            items.take(4).forEach { item ->
              Text(
                text = "• ${item.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            if (items.size > 4) {
              Text(
                text = "...and ${items.size - 4} more",
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
        shape = CircleShape,
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
        shape = CircleShape,
      ) {
        Text("Cancel")
      }
    },
  )
}

@Composable
internal fun MoveDestinationDialog(
  itemsToMove: List<FileItem>,
  initialDirectory: File,
  storageVolumes: List<StorageVolumeInfo>,
  onDismiss: () -> Unit,
  onConfirmMove: (File) -> Unit,
) {
  val rootDir = remember { FileManagerEngine.getUserRootDirectory() }
  var currentDir by remember {
    mutableStateOf(
      if (initialDirectory.exists() && initialDirectory.isDirectory) {
        initialDirectory
      } else {
        rootDir
      }
    )
  }
  var searchQuery by remember { mutableStateOf("") }

  val movingDirectories = remember(itemsToMove) {
    itemsToMove.filter { it.isDirectory }.map { it.file }
  }

  val subfolders = remember(currentDir, searchQuery, movingDirectories) {
    val rawList = currentDir.listFiles()?.toList() ?: emptyList()
    rawList
      .filter { it.isDirectory && !it.name.startsWith(".") }
      .filter { dir ->
        movingDirectories.none { movingDir ->
          FileManagerEngine.isSameOrDescendant(dir, movingDir)
        }
      }
      .filter {
        searchQuery.isBlank() || it.name.contains(searchQuery.trim(), ignoreCase = true)
      }
      .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
  }

  val quickFolders = remember(rootDir, storageVolumes) {
    buildList {
      for (vol in storageVolumes) {
        add(vol.name to vol.file)
      }
      add("Download" to File(rootDir, "Download"))
      add("Documents" to File(rootDir, "Documents"))
      add("Pictures" to File(rootDir, "Pictures"))
    }.distinctBy { it.second.absolutePath }
  }

  val canMoveHere = remember(currentDir, movingDirectories) {
    currentDir.exists() &&
      currentDir.isDirectory &&
      movingDirectories.none { movingDir ->
        FileManagerEngine.isSameOrDescendant(currentDir, movingDir)
      }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("move_destination_dialog"),
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
            imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (itemsToMove.size == 1) "Move \"${itemsToMove.first().name}\"" else "Move ${itemsToMove.size} Items",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = currentDir.absolutePath,
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
          .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          for ((label, folder) in quickFolders) {
            FilterChip(
              selected = currentDir.absolutePath == folder.absolutePath,
              onClick = {
                if (folder.exists() || folder.mkdirs()) {
                  currentDir = folder
                }
              },
              label = { Text(label) },
              shape = RoundedCornerShape(20.dp),
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          val parent = currentDir.parentFile
          IconButton(
            onClick = { if (parent != null && parent.canRead()) currentDir = parent },
            enabled = parent != null && parent.canRead(),
            modifier = Modifier.testTag("move_dialog_navigate_up"),
          ) {
            Icon(Icons.Default.ArrowUpward, contentDescription = "Parent folder")
          }

          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter folders...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            trailingIcon = if (searchQuery.isNotEmpty()) {
              {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
              }
            } else null,
            modifier = Modifier.weight(1f),
          )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        if (subfolders.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "No subfolders in this directory",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            items(subfolders, key = { it.absolutePath }) { folder ->
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { currentDir = folder }
                  .testTag("move_folder_item_${folder.name}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                  )
                  Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmMove(currentDir) },
        enabled = canMoveHere,
        shape = CircleShape,
        modifier = Modifier.testTag("confirm_move_here_button"),
      ) {
        Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Move here")
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

@Composable
internal fun FileDetailsDialog(
  item: FileItem,
  onDismiss: () -> Unit,
  onOpen: () -> Unit = {},
  onOpenWith: () -> Unit = {},
  onShare: () -> Unit,
) {
  val context = LocalContext.current
  val details = remember(item) {
    FileManagerEngine.getFileDetails(item.file)
  }

  fun copyDetailToClipboard(label: String, value: String) {
    try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
      clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
      Toast.makeText(context, "Copied $label", Toast.LENGTH_SHORT).show()
    } catch (_: Throwable) {}
  }

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
        FileIconBox(item = item, size = 44.dp)
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = if (details.isDirectory) "Folder • ${details.sizeFormatted}" else "${details.mimeType} • ${details.sizeFormatted}",
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        DetailRow(
          label = "Location",
          value = details.parentPath,
          onCopy = { copyDetailToClipboard("Path", item.path) },
          copyTestTag = "details_copy_path",
        )
        DetailRow("Size", details.sizeFormatted)
        if (!details.isDirectory) {
          DetailRow("Exact bytes", "${details.sizeBytes} bytes")
        }
        DetailRow("Type", details.mimeType)
        DetailRow("Modified", details.lastModifiedFormatted)
        if (details.lineCount != null) {
          DetailRow("Line count", "${details.lineCount} lines")
        }
        if (details.md5Checksum != null) {
          DetailRow(
            label = "MD5 Hash",
            value = details.md5Checksum,
            onCopy = { copyDetailToClipboard("MD5 Hash", details.md5Checksum) },
            copyTestTag = "details_copy_md5",
          )
        }
        DetailRow("Permissions", "Read: ${details.canRead} • Write: ${details.canWrite}")
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Close")
      }
    },
    dismissButton = {
      if (!item.isDirectory) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = {
              onDismiss()
              onOpenWith()
            },
            shape = CircleShape,
            modifier = Modifier.testTag("details_action_open_with"),
          ) {
            Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open with")
          }
          OutlinedButton(
            onClick = onShare,
            shape = CircleShape,
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share")
          }
        }
      }
    },
  )
}

@Composable
internal fun DetailRow(
  label: String,
  value: String,
  onCopy: (() -> Unit)? = null,
  copyTestTag: String? = null,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .let { mod -> if (onCopy != null) mod.clickable { onCopy() } else mod },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
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
    if (onCopy != null) {
      IconButton(
        onClick = onCopy,
        modifier = Modifier
          .size(28.dp)
          .let { mod -> if (copyTestTag != null) mod.testTag(copyTestTag) else mod },
      ) {
        Icon(
          Icons.Default.ContentCopy,
          contentDescription = "Copy $label",
          modifier = Modifier.size(15.dp),
          tint = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

// ---------------------------------------------------------
// In-App Text Editor & Image Viewer
// ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TextEditorDialog(
  file: File,
  onDismiss: () -> Unit,
  onSaved: () -> Unit,
) {
  val context = LocalContext.current
  var textContent by remember(file.absolutePath) {
    mutableStateOf(FileManagerEngine.readTextContent(file) ?: "")
  }
  var originalContent by remember(file.absolutePath) {
    mutableStateOf(textContent)
  }
  var isSaving by remember(file.absolutePath) { mutableStateOf(false) }
  val hasChanges = textContent != originalContent
  val formatBadge = remember(file.name) {
    file.extension.uppercase(Locale.ROOT).ifEmpty { "TXT" }
  }
  val sizeFormatted = remember(file.absolutePath, textContent, hasChanges) {
    val byteCount = if (file.exists() && !hasChanges) {
      file.length()
    } else {
      textContent.toByteArray(Charsets.UTF_8).size.toLong()
    }
    FileManagerEngine.formatFileSize(byteCount)
  }
  val statusText = if (hasChanges) "Unsaved changes*" else "Saved"

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      shadowElevation = 8.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 24.dp)
        .testTag("text_editor_modal"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Modal Header: Icon Badge + File Name & Metadata/Status + Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = if (FileManagerEngine.isCodeFile(file)) Icons.Default.Code else Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = file.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("text_editor_modal_title"),
              )
              Text(
                text = "$formatBadge • $sizeFormatted • $statusText",
                style = MaterialTheme.typography.bodySmall,
                color = if (hasChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("text_editor_modal_subtitle"),
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .testTag("text_editor_close_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        // Editor Body (resizes to fit content, or scrolls if there is too much content)
        OutlinedTextField(
          value = textContent,
          onValueChange = { textContent = it },
          shape = RoundedCornerShape(14.dp),
          minLines = 1,
          maxLines = 14,
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .heightIn(max = 340.dp)
            .testTag("text_editor_input"),
          textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
          ),
          placeholder = { Text("Enter text here...") },
        )

        // Bottom Action Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("text_editor_action_bar"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          val lines = textContent.lines().size
          val chars = textContent.length
          Text(
            text = "$lines lines • $chars characters",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f)
              .padding(end = 8.dp)
              .testTag("text_editor_stats"),
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            TextButton(
              onClick = onDismiss,
              shape = CircleShape,
              modifier = Modifier.testTag("text_editor_cancel_button"),
            ) {
              Text("Cancel")
            }

            Button(
              onClick = {
                isSaving = true
                if (FileManagerEngine.writeTextContent(file, textContent)) {
                  originalContent = textContent
                  onSaved()
                  Toast.makeText(context, "Saved ${file.name}", Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(context, "Error saving file", Toast.LENGTH_SHORT).show()
                }
                isSaving = false
              },
              enabled = hasChanges && !isSaving,
              shape = CircleShape,
              modifier = Modifier.testTag("text_editor_save_button"),
            ) {
              Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Save")
            }
          }
        }
      }
    }
  }
}

/**
 * Simple Video Player Modal (styled matching [MiniAudioPlayerModal]) that pops up when a video file
 * is tapped in the File Manager tool, immediately starts playing the video, and provides a video
 * viewport along with a play/pause toggle to the left of a tracking slider.
 */
@Composable
fun MiniVideoPlayerModal(
  file: File,
  onDismiss: () -> Unit,
) {
  val isInspection = LocalInspectionMode.current
  val density = LocalDensity.current

  val initialDurationMs = remember(file.absolutePath, isInspection) {
    if (isInspection) {
      5000L
    } else {
      AudioRecorderEngine.extractAudioDurationMs(file).coerceAtLeast(1000L)
    }
  }

  val thumbnailBitmap = remember(file.absolutePath, isInspection) {
    if (isInspection || !file.exists() || file.length() <= 0L) {
      null
    } else {
      try {
        val retriever = MediaMetadataRetriever()
        try {
          retriever.setDataSource(file.absolutePath)
          retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } finally {
          try {
            retriever.release()
          } catch (_: Throwable) {}
        }
      } catch (_: Throwable) {
        null
      }
    }
  }

  var isPlaying by remember(file.absolutePath) { mutableStateOf(true) }
  var durationMs by remember(file.absolutePath) { mutableLongStateOf(initialDurationMs) }
  var currentPositionMs by remember(file.absolutePath) { mutableLongStateOf(0L) }
  var isDraggingSlider by remember(file.absolutePath) { mutableStateOf(false) }
  var dragFraction by remember(file.absolutePath) { mutableFloatStateOf(0f) }
  var usingFallbackTimer by remember(file.absolutePath) { mutableStateOf(isInspection) }

  var mediaPlayerRef by remember(file.absolutePath) { mutableStateOf<MediaPlayer?>(null) }
  var isPlayerPrepared by remember(file.absolutePath) { mutableStateOf(false) }
  var videoWidth by remember(file.absolutePath) { mutableIntStateOf(thumbnailBitmap?.width ?: 0) }
  var videoHeight by remember(file.absolutePath) { mutableIntStateOf(thumbnailBitmap?.height ?: 0) }

  val formatBadge = remember(file.name) {
    file.extension.uppercase(Locale.ROOT).ifEmpty { "VIDEO" }
  }
  val sizeFormatted = remember(file.absolutePath) {
    FileManagerEngine.formatFileSize(file.length())
  }

  DisposableEffect(file.absolutePath) {
    onDispose {
      isPlayerPrepared = false
      mediaPlayerRef?.let { mp ->
        try {
          if (mp.isPlaying) {
            mp.stop()
          }
        } catch (_: Throwable) {}
        try {
          mp.release()
        } catch (_: Throwable) {}
      }
      mediaPlayerRef = null
    }
  }

  // Synchronize playback progress with tracking slider
  LaunchedEffect(isPlaying, mediaPlayerRef, isPlayerPrepared, usingFallbackTimer, isDraggingSlider) {
    if (!isPlaying) return@LaunchedEffect
    while (kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]?.isActive != false && isPlaying) {
      if (!isDraggingSlider) {
        val mp = mediaPlayerRef
        if (mp != null && isPlayerPrepared && !usingFallbackTimer) {
          try {
            val pos = mp.currentPosition.toLong().coerceAtLeast(0L)
            currentPositionMs = pos.coerceAtMost(durationMs)
            if (!mp.isPlaying && currentPositionMs >= durationMs - 80L) {
              isPlaying = false
            }
          } catch (_: Throwable) {
            usingFallbackTimer = true
          }
        } else {
          // Simulated timer fallback when running in test / headless environment or while preparing
          val nextPos = currentPositionMs + 100L
          if (nextPos >= durationMs) {
            currentPositionMs = durationMs
            isPlaying = false
          } else {
            currentPositionMs = nextPos
          }
        }
      }
      kotlinx.coroutines.delay(100L)
    }
  }

  fun togglePlayPause() {
    val mp = mediaPlayerRef
    if (isPlaying) {
      isPlaying = false
      if (isPlayerPrepared) {
        try {
          mp?.pause()
        } catch (_: Throwable) {}
      }
    } else {
      if (currentPositionMs >= durationMs) {
        currentPositionMs = 0L
        if (isPlayerPrepared) {
          try {
            mp?.seekTo(0)
          } catch (_: Throwable) {}
        }
      }
      isPlaying = true
      if (isPlayerPrepared) {
        try {
          mp?.start()
        } catch (_: Throwable) {}
      }
    }
  }

  fun seekToFraction(fraction: Float) {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    val targetMs = (clampedFraction * durationMs).toLong()
    currentPositionMs = targetMs
    if (isPlayerPrepared) {
      try {
        mediaPlayerRef?.seekTo(targetMs.toInt())
      } catch (_: Throwable) {}
    }
  }

  val sliderProgress = if (isDraggingSlider) {
    dragFraction
  } else if (durationMs > 0L) {
    (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
  } else {
    0f
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      shadowElevation = 8.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .testTag("mini_video_player_modal"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Modal Header: Icon Badge + File Name + Close Button (matching MiniAudioPlayerModal)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.VideoFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = file.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("mini_video_player_title"),
              )
              Text(
                text = "$formatBadge • $sizeFormatted",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("mini_video_player_subtitle"),
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .testTag("mini_video_player_close_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close player",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        // Video Viewport Card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.Black,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { togglePlayPause() }
            .testTag("mini_video_player_viewport"),
        ) {
          BoxWithConstraints(
            modifier = Modifier
              .fillMaxSize()
              .clipToBounds(),
            contentAlignment = Alignment.Center,
          ) {
            val availWPx = with(density) { maxWidth.toPx() }
            val availHPx = with(density) { maxHeight.toPx() }
            val videoAspect = if (videoWidth > 0 && videoHeight > 0) {
              videoWidth.toFloat() / videoHeight.toFloat()
            } else {
              16f / 9f
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

            if (thumbnailBitmap != null && !isPlayerPrepared) {
              Image(
                bitmap = thumbnailBitmap.asImageBitmap(),
                contentDescription = file.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(fitW, fitH),
              )
            } else if (!isPlayerPrepared) {
              Icon(
                imageVector = Icons.Default.VideoFile,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp),
              )
            }

            if (!isInspection && file.exists()) {
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
                          mp.setDataSource(file.absolutePath)
                          mp.setSurface(surface)
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
                            val mpDuration = preparedMp.duration.toLong()
                            if (mpDuration > 0L) {
                              durationMs = mpDuration
                            }
                            mediaPlayerRef = preparedMp
                            isPlayerPrepared = true
                            usingFallbackTimer = false
                            if (currentPositionMs > 0L && currentPositionMs < durationMs) {
                              try {
                                preparedMp.seekTo(currentPositionMs.toInt())
                              } catch (_: Throwable) {}
                            }
                            if (isPlaying) {
                              try {
                                preparedMp.start()
                              } catch (_: Throwable) {}
                            }
                          }
                          mp.setOnCompletionListener {
                            isPlaying = false
                            currentPositionMs = durationMs
                          }
                          mp.setOnErrorListener { _, _, _ ->
                            isPlayerPrepared = false
                            usingFallbackTimer = true
                            true
                          }
                          mp.prepareAsync()
                        } catch (_: Throwable) {
                          isPlayerPrepared = false
                          usingFallbackTimer = true
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

        // Play/Pause toggle mini-FAB to the left of the tracking slider (matching MiniAudioPlayerModal)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          SmallFloatingActionButton(
            onClick = { togglePlayPause() },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
              .testTag("mini_video_player_play_pause_button")
              .semantics {
                contentDescription = if (isPlaying) "Pause" else "Play"
              },
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = null,
              modifier = Modifier.size(22.dp),
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            SlimDotSlider(
              value = sliderProgress,
              onValueChange = { newFraction ->
                isDraggingSlider = true
                dragFraction = newFraction.coerceIn(0f, 1f)
                currentPositionMs = (dragFraction * durationMs).toLong()
              },
              onValueChangeFinished = {
                seekToFraction(dragFraction)
                isDraggingSlider = false
              },
              valueRange = 0f..1f,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("mini_video_player_tracking_slider"),
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = AudioRecorderEngine.formatDurationMs(currentPositionMs),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("mini_video_player_elapsed_time"),
              )
              Text(
                text = AudioRecorderEngine.formatDurationMs(durationMs),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("mini_video_player_total_time"),
              )
            }
          }
        }
      }
    }
  }
}

// ---------------------------------------------------------
// Helpers
// ---------------------------------------------------------

private fun getPreviewSampleFiles(): List<FileItem> {
  return listOf(
    FileItem(
      file = File("/storage/emulated/0/Download"),
      name = "Download",
      path = "/storage/emulated/0/Download",
      isDirectory = true,
      sizeBytes = 42_500_000L,
      lastModifiedMs = System.currentTimeMillis() - 3600000,
      extension = "",
      mimeType = "inode/directory",
      childCount = 5,
    ),
    FileItem(
      file = File("/storage/emulated/0/Documents"),
      name = "Documents",
      path = "/storage/emulated/0/Documents",
      isDirectory = true,
      sizeBytes = 1_850_000L,
      lastModifiedMs = System.currentTimeMillis() - 7200000,
      extension = "",
      mimeType = "inode/directory",
      childCount = 3,
    ),
    FileItem(
      file = File("/storage/emulated/0/DCIM"),
      name = "DCIM",
      path = "/storage/emulated/0/DCIM",
      isDirectory = true,
      sizeBytes = 320_000_000L,
      lastModifiedMs = System.currentTimeMillis() - 10800000,
      extension = "",
      mimeType = "inode/directory",
      childCount = 12,
    ),
    FileItem(
      file = File("/storage/emulated/0/Welcome.txt"),
      name = "Welcome.txt",
      path = "/storage/emulated/0/Welcome.txt",
      isDirectory = false,
      sizeBytes = 1420L,
      lastModifiedMs = System.currentTimeMillis(),
      extension = "txt",
      mimeType = "text/plain",
    ),
  )
}

@Preview(showBackground = true)
@Composable
fun FilesScreenPreview() {
  ToolboxTheme {
    FilesScreen()
  }
}

@Preview(showBackground = true)
@Composable
fun FilesScreenSearchActivePreview() {
  ToolboxTheme {
    FilesScreen(initialSearchActive = true)
  }
}

@Preview(showBackground = true)
@Composable
fun FileGroupHeaderExpandedPreview() {
  ToolboxTheme {
    Column(modifier = Modifier.padding(16.dp)) {
      FileGroupHeader(
        title = "Folders",
        count = 4,
        isCollapsed = false,
        onToggleCollapse = {},
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun FileGroupHeaderCollapsedPreview() {
  ToolboxTheme {
    Column(modifier = Modifier.padding(16.dp)) {
      FileGroupHeader(
        title = "Folders",
        count = 4,
        isCollapsed = true,
        onToggleCollapse = {},
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun FilesHeaderSortMenuPreview() {
  ToolboxTheme {
    Column(modifier = Modifier.padding(16.dp)) {
      FilesHeader(
        currentDirectory = File("/storage/emulated/0/Documents"),
        isSearchActive = false,
        searchQuery = "",
        storageInfoSubtext = "Internal Storage",
        viewMode = FileViewMode.LIST,
        onToggleSearch = {},
        onSearchQueryChange = {},
        onToggleViewMode = {},
        onNavigateUp = {},
        canNavigateUp = true,
      )
      StorageDevicePillRow(
        storageVolumes = listOf(
          StorageVolumeInfo(
            id = "primary",
            name = "Internal Storage",
            file = File("/storage/emulated/0"),
            isPrimary = true,
            isRemovable = false,
          ),
        ),
        activeVolume = null,
        sortOrder = FileSortOrder.NAME_ASC,
        groupOption = FileGroupOption.TYPE,
        showSortMenu = true,
        onSelectVolume = {},
        onOpenSortMenu = {},
        onDismissSortMenu = {},
        onSelectSortOrder = {},
      )
    }
  }
}
