package com.example.toolbox.ui.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
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
  themeColor: String? = null,
  onOpenFile: ((File, Boolean) -> Unit)? = null,
  onOpenFileConverter: ((File) -> Unit)? = null,
  onOpenImageEditor: ((File) -> Unit)? = null,
  onOpenVideoEditor: ((File) -> Unit)? = null,
  onOpenAudioEditor: ((File) -> Unit)? = null,
  onOpenArchive: ((File) -> Unit)? = null,
  onOpenPdfViewer: ((File) -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current

  // User's default root folder: storage/emulated/0 (or custom rootDirectory if provided)
  val userRoot = remember(rootDirectory, initialDirectory, isInspection) {
    when {
      rootDirectory != null -> rootDirectory
      isInspection -> File("/storage/emulated/0")
      else -> {
        val defaultRoot = FileManagerEngine.getUserRootDirectory()
        val initDir = initialDirectory
        if (initDir != null && !FileManagerEngine.isSameOrDescendant(initDir, defaultRoot)) {
          val volumes = FileManagerEngine.getStorageVolumes(context)
          val matchingVolume = volumes.firstOrNull { vol ->
            FileManagerEngine.isSameOrDescendant(initDir, vol.file)
          }
          matchingVolume?.file ?: initDir
        } else {
          defaultRoot
        }
      }
    }
  }

  // Active directory defaults to storage/emulated/0
  var currentDirectory by rememberSaveable(initialDirectory?.absolutePath, rootDirectory?.absolutePath) {
    mutableStateOf(initialDirectory ?: userRoot)
  }

  // Hot-plug storage revision token
  var storageRevision by remember { mutableIntStateOf(0) }

  val samplesRoot = remember(context, isInspection) {
    if (isInspection) File("/storage/emulated/0/Samples") else FileManagerEngine.getSamplesDirectory(context)
  }

  // Discovered storage volumes (Primary root + Samples folder + any connected external storage devices)
  val storageVolumes = remember(storageRevision, isInspection, userRoot, samplesRoot) {
    if (isInspection) {
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
  var hasPermission by remember {
    mutableStateOf(isInspection || FileManagerEngine.hasStoragePermission(context))
  }

  val manageStorageLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    hasPermission = FileManagerEngine.hasStoragePermission(context)
    storageRevision++
  }

  val legacyPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasPermission = isGranted
    storageRevision++
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
  var fileForImagePreview by remember { mutableStateOf<FileItem?>(null) }
  var fileForConverter by remember { mutableStateOf<File?>(null) }
  var fileForImageEditor by remember { mutableStateOf<File?>(null) }
  var fileForVideoEditor by remember { mutableStateOf<File?>(null) }
  var fileForAudioEditor by remember { mutableStateOf<File?>(null) }
  var fileForArchive by remember { mutableStateOf<File?>(null) }
  var fileForPdfViewer by remember { mutableStateOf<File?>(null) }
  var showSortMenu by remember { mutableStateOf(false) }

  // Multi-selection state
  var isSelectionMode by rememberSaveable { mutableStateOf(false) }
  var selectedPaths by remember { mutableStateOf(setOf<String>()) }
  var showBulkInfoDialog by remember { mutableStateOf(false) }
  var showBulkDeleteDialog by remember { mutableStateOf(false) }
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
    val parent = FileManagerEngine.navigateUpDirectory(currentDirectory, activeRoot) ?: return false
    navigateTo(parent)
    return true
  }

  fun openFile(file: File, chooseApp: Boolean = false) {
    if (onOpenFile != null) {
      onOpenFile(file, chooseApp)
    } else {
      FileManagerEngine.openFile(context, file, chooseApp)
    }
  }

  fun shareFile(file: File) {
    try {
      val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
      )
      val mime = FileManagerEngine.getMimeType(file)
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
    } catch (e: Exception) {
      Toast.makeText(context, "Could not share file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
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

  val activePdfViewerFile = fileForPdfViewer
  if (activePdfViewerFile != null) {
    PdfViewerScreen(
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

  val canGoUp = canNavigateUp()

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
        .background(MaterialTheme.colorScheme.background),
    ) {
      // 1. Top Bar / Header
      val headerSubtext = if (canNavigateUp()) {
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

      FilesHeader(
        currentDirectory = currentDirectory,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        storageInfoSubtext = headerSubtext,
        viewMode = viewMode,
        sortOrder = sortOrder,
        groupOption = groupOption,
        showSortMenu = showSortMenu,
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
          viewMode = if (viewMode == FileViewMode.LIST) FileViewMode.GRID else FileViewMode.LIST
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
        isSortGroupSavedForFolder = isSortGroupSavedForFolder,
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
        onNavigateUp = { navigateUp() },
        canNavigateUp = canNavigateUp(),
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
        onAddFolderToHomeScreen = { folder -> addFolderToHomeScreen(context, folder, themeColor) },
        onAddCurrentFolderToHomeScreen = { addFolderToHomeScreen(context, currentDirectory, themeColor) },
      )

      // 2. Storage Pill Buttons Row:
      // Starts with the user's root folder (storage/emulated/0), and whenever external
      // storage devices are connected, new pills pop up with the storage device name.
      StorageDevicePillRow(
        storageVolumes = storageVolumes,
        activeVolume = activeVolume,
        onSelectVolume = { volume ->
          navigateTo(volume.file)
        },
      )

      val isBrowsingSamples = activeVolume?.id == FileManagerEngine.SAMPLES_VOLUME_ID

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
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
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
                      key = { it.path },
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
                        onOpenText = { fileForTextEdit = item },
                        onOpenImage = { fileForImagePreview = item },
                        onShare = { shareFile(item.file) },
                        onDuplicate = {
                          FileManagerEngine.duplicateFile(item.file)
                          refreshTrigger++
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onRename = { fileToRename = item },
                        onDetails = { fileForDetails = item },
                        onDelete = { fileToDelete = item },
                        onAddToHomeScreen = { addFolderToHomeScreen(context, item.file, themeColor) },
                      )
                    }
                  }
                }
              }
            }
            FileViewMode.GRID -> {
              LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 104.dp),
                modifier = Modifier
                  .fillMaxSize()
                  .testTag("files_grid_view"),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                      key = { it.path },
                    ) { item ->
                    FileGridItem(
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
                      onOpenText = { fileForTextEdit = item },
                      onOpenImage = { fileForImagePreview = item },
                      onShare = { shareFile(item.file) },
                      onDuplicate = {
                        FileManagerEngine.duplicateFile(item.file)
                        refreshTrigger++
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      },
                      onRename = { fileToRename = item },
                      onDetails = { fileForDetails = item },
                      onDelete = { fileToDelete = item },
                      onAddToHomeScreen = { addFolderToHomeScreen(context, item.file, themeColor) },
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

    // Floating Paste Bar (shown when items are in clipboard to copy/cut)
    clipboardState?.let { clip ->
      Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = if (isSelectionMode) 16.dp else 76.dp, start = 16.dp, end = 16.dp)
          .testTag("files_paste_bar"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector = if (clip.operation == ClipboardOperation.CUT) Icons.Default.ContentCut else Icons.Default.ContentCopy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "${clip.items.size} ${if (clip.operation == ClipboardOperation.CUT) "cut" else "copied"}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
          )
          Button(
            onClick = {
              val count = if (clip.operation == ClipboardOperation.CUT) {
                FileManagerEngine.moveMultipleFiles(clip.items.map { it.file }, currentDirectory)
              } else {
                FileManagerEngine.copyMultipleFiles(clip.items.map { it.file }, currentDirectory)
              }
              clipboardState = null
              refreshTrigger++
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              Toast.makeText(context, "Pasted $count items", Toast.LENGTH_SHORT).show()
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
            modifier = Modifier.size(32.dp),
          ) {
            Icon(Icons.Default.Close, contentDescription = "Cancel paste")
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
          refreshTrigger++
          fileToDelete = null
        },
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

    fileForImagePreview?.let { item ->
      ImageViewerDialog(
        file = item.file,
        onDismiss = { fileForImagePreview = null },
        onShare = { shareFile(item.file) },
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
  }
}

// ---------------------------------------------------------
// Sub-components: Header, Storage Pill Buttons, Breadcrumbs
// ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilesHeader(
  currentDirectory: File,
  isSearchActive: Boolean,
  searchQuery: String,
  storageInfoSubtext: String,
  viewMode: FileViewMode,
  sortOrder: FileSortOrder,
  groupOption: FileGroupOption = FileGroupOption.TYPE,
  showSortMenu: Boolean,
  isSelectionMode: Boolean = false,
  selectedCount: Int = 0,
  totalCount: Int = 0,
  onToggleSelectAll: () -> Unit = {},
  onCloseSelection: () -> Unit = {},
  onBulkInfo: () -> Unit = {},
  onBulkCut: () -> Unit = {},
  onBulkCopy: () -> Unit = {},
  onBulkDelete: () -> Unit = {},
  onToggleSearch: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onToggleViewMode: () -> Unit,
  onOpenSortMenu: () -> Unit,
  onDismissSortMenu: () -> Unit,
  onSelectSortOrder: (FileSortOrder) -> Unit,
  onSelectGroupOption: (FileGroupOption) -> Unit = {},
  isSortGroupSavedForFolder: Boolean = false,
  onToggleSaveSortGroupForFolder: () -> Unit = {},
  onNavigateUp: () -> Unit,
  canNavigateUp: Boolean,
  singleSelectedFolder: File? = null,
  singleSelectedFile: File? = null,
  onOpenSelectedFile: ((File, Boolean) -> Unit)? = null,
  onConvertSelectedFile: ((File) -> Unit)? = null,
  onEditSelectedImage: ((File) -> Unit)? = null,
  onEditSelectedVideo: ((File) -> Unit)? = null,
  onEditSelectedAudio: ((File) -> Unit)? = null,
  onOpenSelectedArchive: ((File) -> Unit)? = null,
  onOpenSelectedPdf: ((File) -> Unit)? = null,
  onAddFolderToHomeScreen: ((File) -> Unit)? = null,
  onAddCurrentFolderToHomeScreen: (() -> Unit)? = null,
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(isSearchActive) {
    if (isSearchActive) {
      kotlinx.coroutines.delay(100)
      focusRequester.requestFocus()
      keyboardController?.show()
    } else {
      keyboardController?.hide()
    }
  }

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
      if (isSelectionMode) {
        // Multi-Selection Header Toolbar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Leading: Close button + Select All Checkbox + "# selected" Text
          Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            IconButton(
              onClick = onCloseSelection,
              modifier = Modifier
                .size(36.dp)
                .testTag("action_close_selection"),
            ) {
              Icon(Icons.Default.Close, contentDescription = "Exit selection")
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleSelectAll)
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .testTag("action_select_all_checkbox"),
            ) {
              Checkbox(
                checked = totalCount > 0 && selectedCount == totalCount,
                onCheckedChange = { onToggleSelectAll() },
                colors = CheckboxDefaults.colors(
                  checkedColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.size(32.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          // Trailing Actions: Info, Cut, Copy, Delete (red), and Overflow "..." Menu
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
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
                  text = { Text("Copy") },
                  leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                  enabled = hasSelection,
                  onClick = {
                    showOverflowMenu = false
                    onBulkCopy()
                  },
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
                if (singleSelectedFile != null && onOpenSelectedArchive != null && ArchiveEngine.isSupportedArchive(singleSelectedFile)) {
                  DropdownMenuItem(
                    text = { Text("Archives") },
                    leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
                    onClick = {
                      showOverflowMenu = false
                      onOpenSelectedArchive(singleSelectedFile)
                    },
                    modifier = Modifier.testTag("action_selection_open_archive"),
                  )
                }
                if (singleSelectedFile != null && onOpenSelectedPdf != null && FileManagerEngine.isPdfFile(singleSelectedFile)) {
                  DropdownMenuItem(
                    text = { Text("PDF Viewer") },
                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                    onClick = {
                      showOverflowMenu = false
                      onOpenSelectedPdf(singleSelectedFile)
                    },
                    modifier = Modifier.testTag("action_selection_open_pdf"),
                  )
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
                if (singleSelectedFolder != null && onAddFolderToHomeScreen != null) {
                  DropdownMenuItem(
                    text = { Text("Add to Home screen") },
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
          }
        }
      } else {
        // Standard Header Toolbar (matches height and layout of To-Do toolbar)
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
            if (canNavigateUp) {
              IconButton(
                onClick = onNavigateUp,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("action_navigate_up"),
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up")
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
                  Icons.Default.FolderOpen,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (canNavigateUp) currentDirectory.name else "Files",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = storageInfoSubtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            IconButton(onClick = onToggleSearch, modifier = Modifier.testTag("action_toggle_search")) {
              Icon(
                Icons.Default.Search,
                contentDescription = "Search",
              )
            }

            ViewModeSegmentedToggle(
              isListView = viewMode == FileViewMode.LIST,
              onSelectListView = { if (viewMode != FileViewMode.LIST) onToggleViewMode() },
              onSelectGridView = { if (viewMode == FileViewMode.LIST) onToggleViewMode() },
              onToggle = onToggleViewMode,
              toggleTestTag = "action_toggle_view_mode",
              listTestTag = "files_view_mode_list",
              gridTestTag = "files_view_mode_grid",
              listContentDescription = "List view",
              gridContentDescription = "Grid view",
            )

            Box {
              IconButton(onClick = onOpenSortMenu, modifier = Modifier.testTag("action_sort_menu")) {
                Icon(Icons.Default.Tune, contentDescription = "Sort and group options")
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

            if (canNavigateUp && onAddCurrentFolderToHomeScreen != null) {
              var showFolderMenu by remember { mutableStateOf(false) }
              Box {
                IconButton(
                  onClick = { showFolderMenu = true },
                  modifier = Modifier.testTag("action_current_folder_menu"),
                ) {
                  Icon(Icons.Default.MoreVert, contentDescription = "Folder options")
                }
                DropdownMenu(
                  expanded = showFolderMenu,
                  onDismissRequest = { showFolderMenu = false },
                ) {
                  DropdownMenuItem(
                    text = { Text("Add to Home screen") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    onClick = {
                      showFolderMenu = false
                      onAddCurrentFolderToHomeScreen()
                    },
                    modifier = Modifier.testTag("action_add_current_folder_to_home"),
                  )
                }
              }
            }
          }
        }
      }

      // 2. Animated Expanding Search Bar
      // Animates expanding from the search button on the right to fill the entire toolbar
      AnimatedVisibility(
        visible = isSearchActive && !isSelectionMode,
        enter = fadeIn(animationSpec = tween(220)) + expandHorizontally(
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
          ),
          expandFrom = Alignment.End,
        ),
        exit = fadeOut(animationSpec = tween(180)) + shrinkHorizontally(
          animationSpec = tween(200),
          shrinkTowards = Alignment.End,
        ),
        modifier = Modifier.matchParentSize(),
      ) {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = {},
            ),
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 2.dp,
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = onToggleSearch,
              modifier = Modifier.testTag("action_close_search"),
            ) {
              Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Close Search",
                tint = MaterialTheme.colorScheme.onSurface,
              )
            }

            TextField(
              value = searchQuery,
              onValueChange = onSearchQueryChange,
              placeholder = {
                Text(
                  "Search files...",
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              },
              singleLine = true,
              textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
              ),
              colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
              ),
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                      Icons.Default.Clear,
                      contentDescription = "Clear search",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("files_search_input"),
            )
          }
        }
      }
    }
  }
}

/**
 * Storage device pill buttons row.
 * Displays a pill button for storage/emulated/0 (the user's root folder), and dynamically
 * displays new pill buttons with the name of each connected external storage device (SD card, USB OTG, etc.).
 */
@Composable
private fun StorageDevicePillRow(
  storageVolumes: List<StorageVolumeInfo>,
  activeVolume: StorageVolumeInfo?,
  onSelectVolume: (StorageVolumeInfo) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    storageVolumes.forEach { volume ->
      val isSelected = activeVolume?.id == volume.id
      val icon = when {
        volume.isPrimary -> Icons.Default.Home
        volume.id == FileManagerEngine.SAMPLES_VOLUME_ID ||
          volume.name.equals(FileManagerEngine.SAMPLES_FOLDER_NAME, ignoreCase = true) -> Icons.Default.Folder
        volume.name.contains("usb", ignoreCase = true) -> Icons.Default.Usb
        else -> Icons.Default.SdCard
      }

      FilterChip(
        selected = isSelected,
        onClick = { onSelectVolume(volume) },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("storage_pill_${volume.id}"),
        leadingIcon = {
          Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        },
        label = {
          Text(
            text = volume.name,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
          selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
      )
    }
  }
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
  onOpenText: () -> Unit,
  onOpenImage: () -> Unit,
  onShare: () -> Unit,
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
  onOpenText: () -> Unit = {},
  onOpenImage: () -> Unit = {},
  onShare: () -> Unit,
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
    } else null,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        if (isSelectionMode) {
          Box(
            modifier = Modifier.size(44.dp),
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
          FileIconBox(item = item, size = 44.dp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = item.name,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

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
  onOpenText: () -> Unit,
  onOpenImage: () -> Unit,
  onShare: () -> Unit,
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
          text = { Text("Archives") },
          leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
          onClick = onOpenArchive,
          modifier = Modifier.testTag("action_open_archive"),
        )
      }
      if (FileManagerEngine.isPdfFile(item.file)) {
        DropdownMenuItem(
          text = { Text("PDF Viewer") },
          leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
          onClick = onOpenPdf,
          modifier = Modifier.testTag("action_open_pdf"),
        )
      }
      if (FileManagerEngine.isImageFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Edit") },
          leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
          onClick = onEditImage,
          modifier = Modifier.testTag("action_edit_image"),
        )
        DropdownMenuItem(
          text = { Text("Preview") },
          leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
          onClick = onOpenImage,
          modifier = Modifier.testTag("action_preview_image"),
        )
      } else if (AudioEditorEngine.isSupportedAudioFile(item.file) && !VideoEditorEngine.isVideoFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Edit") },
          leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
          onClick = onEditAudio,
          modifier = Modifier.testTag("action_edit_audio"),
        )
      } else if (VideoEditorEngine.isVideoFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Edit") },
          leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
          onClick = onEditVideo,
          modifier = Modifier.testTag("action_edit_video"),
        )
      } else if (FileManagerEngine.isTextFile(item.file)) {
        DropdownMenuItem(
          text = { Text("Edit / View") },
          leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
          onClick = onOpenText,
          modifier = Modifier.testTag("action_edit_text"),
        )
      }
      DropdownMenuItem(
        text = { Text("Share") },
        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
        onClick = onShare,
      )
    }

    DropdownMenuItem(
      text = { Text("Copy") },
      leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
      onClick = onDuplicate,
    )
    DropdownMenuItem(
      text = { Text("Rename") },
      leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
      onClick = onRename,
    )
    DropdownMenuItem(
      text = { Text("Info") },
      leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
      onClick = onDetails,
    )
    if (item.isDirectory) {
      DropdownMenuItem(
        text = { Text("Add to Home screen") },
        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
        onClick = onAddToHomeScreen,
        modifier = Modifier.testTag("action_add_to_home_screen"),
      )
    }
    HorizontalDivider()
    DropdownMenuItem(
      text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
      leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
      onClick = onDelete,
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
    title = { Text("New Folder") },
    text = {
      Column {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Folder Name") },
          singleLine = true,
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
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
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
    title = { Text("New File") },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("File Name (e.g. notes.txt)") },
          singleLine = true,
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
          listOf(".txt", ".md", ".json", ".log").forEach { ext ->
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
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun RenameFileDialog(
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
        shape = RoundedCornerShape(12.dp),
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
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun DeleteConfirmDialog(
  item: FileItem,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    title = { Text("Delete ${if (item.isDirectory) "Folder" else "File"}?") },
    text = {
      Text("Are you sure you want to delete \"${item.name}\"? This action cannot be undone.")
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        shape = RoundedCornerShape(12.dp),
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
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun BulkInfoDialog(
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
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun BulkDeleteConfirmDialog(
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

@Composable
private fun FileDetailsDialog(
  item: FileItem,
  onDismiss: () -> Unit,
  onOpen: () -> Unit = {},
  onOpenWith: () -> Unit = {},
  onShare: () -> Unit,
) {
  val details = remember(item) {
    FileManagerEngine.getFileDetails(item.file)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        FileIconBox(item = item, size = 32.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = item.name,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        DetailRow("Location", details.parentPath)
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
          DetailRow("MD5 Hash", details.md5Checksum)
        }
        DetailRow("Permissions", "Read: ${details.canRead} • Write: ${details.canWrite}")
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
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
            modifier = Modifier.testTag("details_action_open_with"),
          ) {
            Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open with")
          }
          OutlinedButton(onClick = onShare) {
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
private fun DetailRow(label: String, value: String) {
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

// ---------------------------------------------------------
// In-App Text Editor & Image Viewer
// ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextEditorDialog(
  file: File,
  onDismiss: () -> Unit,
  onSaved: () -> Unit,
) {
  val context = LocalContext.current
  var textContent by remember {
    mutableStateOf(FileManagerEngine.readTextContent(file) ?: "")
  }
  var originalContent by remember {
    mutableStateOf(textContent)
  }
  var isSaving by remember { mutableStateOf(false) }
  val hasChanges = textContent != originalContent

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Top Toolbar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = file.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = if (hasChanges) "Unsaved changes*" else "Saved",
              style = MaterialTheme.typography.labelSmall,
              color = if (hasChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save")
          }
        }

        HorizontalDivider()

        // Editor Body
        OutlinedTextField(
          value = textContent,
          onValueChange = { textContent = it },
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(12.dp),
          textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
          ),
          placeholder = { Text("Enter text here...") },
        )

        // Footer info bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          val lines = textContent.lines().size
          val chars = textContent.length
          Text("$lines lines • $chars characters", style = MaterialTheme.typography.labelSmall)
          Text(FileManagerEngine.formatFileSize(file.length()), style = MaterialTheme.typography.labelSmall)
        }
      }
    }
  }
}

@Composable
private fun ImageViewerDialog(
  file: File,
  onDismiss: () -> Unit,
  onShare: () -> Unit,
) {
  val bitmap = remember(file) {
    try {
      BitmapFactory.decodeFile(file.absolutePath)
    } catch (_: Exception) {
      null
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
          Text(
            text = file.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          IconButton(onClick = onShare) {
            Icon(Icons.Default.Share, contentDescription = "Share")
          }
        }

        HorizontalDivider()

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.05f))
            .padding(12.dp),
          contentAlignment = Alignment.Center,
        ) {
          if (bitmap != null) {
            Image(
              bitmap = bitmap.asImageBitmap(),
              contentDescription = file.name,
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            Text("Unable to load image", color = MaterialTheme.colorScheme.error)
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = if (bitmap != null) "${bitmap.width} × ${bitmap.height} px" else "",
            style = MaterialTheme.typography.labelMedium,
          )
          Text(
            text = FileManagerEngine.formatFileSize(file.length()),
            style = MaterialTheme.typography.labelMedium,
          )
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
    Box(modifier = Modifier.padding(16.dp)) {
      FilesHeader(
        currentDirectory = File("/storage/emulated/0/Documents"),
        isSearchActive = false,
        searchQuery = "",
        storageInfoSubtext = "Internal Storage",
        viewMode = FileViewMode.LIST,
        sortOrder = FileSortOrder.NAME_ASC,
        groupOption = FileGroupOption.TYPE,
        showSortMenu = true,
        onToggleSearch = {},
        onSearchQueryChange = {},
        onToggleViewMode = {},
        onOpenSortMenu = {},
        onDismissSortMenu = {},
        onSelectSortOrder = {},
        onNavigateUp = {},
        canNavigateUp = true,
      )
    }
  }
}
