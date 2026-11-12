package com.example.toolbox.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Archive tool screen:
 * - Simple initial screen (similar to Audio / Image / Video Editor tools) with `+` to open an archive
 *   or zip files, plus sample archive & create-zip buttons.
 * - Full File Explorer-style viewing screen (similar to [FilesScreen]) with breadcrumb navigation,
 *   list/grid toggle, search bar, sort/group options, collapsible categories, multi-select toolbar,
 *   in-archive text/image preview, extraction, and zipping/modifying archives.
 */
@Composable
fun ArchiveScreen(
  modifier: Modifier = Modifier,
  initialFile: File? = null,
  initialDirectory: File? = null,
  onNavigateBack: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val isInspection = LocalInspectionMode.current
  val coroutineScope = rememberCoroutineScope()

  val defaultBrowseDir = remember(initialDirectory, initialFile, isInspection) {
    initialDirectory
      ?: initialFile?.parentFile
      ?: if (isInspection) {
        File("/storage/emulated/0/Download")
      } else {
        val root = FileManagerEngine.getUserRootDirectory()
        val download = File(root, "Download")
        if (download.exists() && download.isDirectory) download else root
      }
  }

  var activeArchiveFile by remember(initialFile?.absolutePath) {
    mutableStateOf(initialFile?.takeIf { ArchiveEngine.isSupportedArchive(it) })
  }
  var archiveRevision by remember { mutableIntStateOf(0) }
  var isBusy by remember { mutableStateOf(false) }

  val archiveInfo = remember(activeArchiveFile?.absolutePath, archiveRevision, isInspection) {
    val file = activeArchiveFile
    when {
      file == null -> null
      isInspection && !file.exists() -> getPreviewSampleArchiveInfo(file)
      else -> ArchiveEngine.readArchive(file).getOrNull()
    }
  }

  // Explorer navigation & view state inside the opened archive
  var currentVirtualDir by rememberSaveable(activeArchiveFile?.absolutePath) { mutableStateOf("") }
  var viewMode by rememberSaveable { mutableStateOf(FileViewMode.LIST) }
  var groupOption by rememberSaveable { mutableStateOf(FileGroupOption.TYPE) }
  var sortOrder by rememberSaveable { mutableStateOf(FileSortOrder.NAME_ASC) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var showSortMenu by remember { mutableStateOf(false) }
  var isFabExpanded by rememberSaveable { mutableStateOf(false) }

  var collapsedGroupTitles by remember(currentVirtualDir, groupOption) {
    mutableStateOf(setOf<String>())
  }

  // Multi-selection state
  var isSelectionMode by rememberSaveable { mutableStateOf(false) }
  var selectedArchivePaths by remember { mutableStateOf(setOf<String>()) }

  // Dialog states
  var showOpenArchivePicker by remember { mutableStateOf(false) }
  var showCreateZipDialog by remember { mutableStateOf(false) }
  var preselectedFilesForZip by remember { mutableStateOf<List<File>>(emptyList()) }
  var showAddFilesDialog by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showArchiveInfoDialog by remember { mutableStateOf(false) }
  var showBulkInfoDialog by remember { mutableStateOf(false) }
  var showBulkDeleteDialog by remember { mutableStateOf(false) }

  var extractPathsTarget by remember { mutableStateOf<Set<String>?>(null) } // emptySet() means extract all
  var entryForDetails by remember { mutableStateOf<ArchiveEntryItem?>(null) }
  var entryForTextPreview by remember { mutableStateOf<ArchiveEntryItem?>(null) }
  var entryForImagePreview by remember { mutableStateOf<ArchiveEntryItem?>(null) }
  var entryToRename by remember { mutableStateOf<ArchiveEntryItem?>(null) }
  var entryToDelete by remember { mutableStateOf<ArchiveEntryItem?>(null) }

  var lastOperationBanner by remember { mutableStateOf<ArchiveOperationResult?>(null) }

  // If initialFile was passed and is NOT an archive (e.g., user opened a regular file or folder to zip),
  // pre-open the Create Zip dialog with that file selected.
  LaunchedEffect(initialFile?.absolutePath) {
    if (initialFile != null && initialFile.exists() && !ArchiveEngine.isSupportedArchive(initialFile)) {
      preselectedFilesForZip = listOf(initialFile)
      showCreateZipDialog = true
    }
  }

  // System document picker fallback
  val systemFilePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri: Uri? ->
    if (uri != null) {
      coroutineScope.launch {
        isBusy = true
        val copied = withContext(Dispatchers.IO) {
          copyUriToTempSourceFile(context, uri)
        }
        isBusy = false
        if (copied != null) {
          if (ArchiveEngine.isSupportedArchive(copied)) {
            activeArchiveFile = copied
            currentVirtualDir = ""
            selectedArchivePaths = emptySet()
            isSelectionMode = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          } else {
            preselectedFilesForZip = listOf(copied)
            showCreateZipDialog = true
          }
        } else {
          Toast.makeText(context, "Could not load selected file", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  fun navigateToVirtualDir(dirPath: String) {
    currentVirtualDir = ArchiveEngine.normalizeArchivePath(dirPath)
    searchQuery = ""
    isSearchActive = false
    isSelectionMode = false
    selectedArchivePaths = emptySet()
    isFabExpanded = false
  }

  val canGoUpInArchive = ArchiveEngine.canNavigateUp(currentVirtualDir)

  fun navigateUpInArchive(): Boolean {
    val parent = ArchiveEngine.navigateUpVirtualDirectory(currentVirtualDir) ?: return false
    navigateToVirtualDir(parent)
    return true
  }

  fun closeCurrentArchive() {
    activeArchiveFile = null
    currentVirtualDir = ""
    searchQuery = ""
    isSearchActive = false
    isSelectionMode = false
    selectedArchivePaths = emptySet()
    isFabExpanded = false
  }

  fun openEntryExternally(info: ArchiveInfo, entry: ArchiveEntryItem, chooseApp: Boolean) {
    coroutineScope.launch {
      val extractedTemp = withContext(Dispatchers.IO) {
        ArchiveEngine.extractEntryToTempFile(context, info.file, entry)
      }
      if (extractedTemp != null) {
        FileManagerEngine.openFile(context, extractedTemp, chooseApp)
      } else {
        Toast.makeText(context, "Could not extract ${entry.name}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun shareArchiveEntry(info: ArchiveInfo, entry: ArchiveEntryItem) {
    coroutineScope.launch {
      val extractedTemp = withContext(Dispatchers.IO) {
        ArchiveEngine.extractEntryToTempFile(context, info.file, entry)
      }
      if (extractedTemp != null) {
        try {
          val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            extractedTemp,
          )
          val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = entry.mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          }
          context.startActivity(Intent.createChooser(shareIntent, "Share ${entry.name}"))
        } catch (e: Exception) {
          Toast.makeText(context, "Could not share ${entry.name}", Toast.LENGTH_SHORT).show()
        }
      } else {
        Toast.makeText(context, "Could not extract ${entry.name}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  BackHandler(
    enabled = isFabExpanded ||
      isSelectionMode ||
      isSearchActive ||
      canGoUpInArchive ||
      onNavigateBack != null,
  ) {
    when {
      isFabExpanded -> {
        isFabExpanded = false
      }
      isSelectionMode -> {
        isSelectionMode = false
        selectedArchivePaths = emptySet()
      }
      isSearchActive -> {
        isSearchActive = false
        searchQuery = ""
      }
      canGoUpInArchive -> {
        navigateUpInArchive()
      }
      onNavigateBack != null -> {
        onNavigateBack()
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("archive_screen"),
  ) {
    val currentInfo = archiveInfo
    if (currentInfo == null) {
      // ====================================================================
      // 1. SIMPLE INITIAL SCREEN (Similar to Audio / Image / Video Editors)
      // ====================================================================
      Column(
        modifier = Modifier.fillMaxSize(),
      ) {
        ArchiveInitialTopBar(
          onCreateZipClick = {
            preselectedFilesForZip = emptyList()
            showCreateZipDialog = true
          },
          onNavigateBack = onNavigateBack,
        )

        ArchiveInitialView(
          onOpenFile = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showOpenArchivePicker = true
          },
          onCreateZip = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            preselectedFilesForZip = emptyList()
            showCreateZipDialog = true
          },
          onUseSample = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            coroutineScope.launch {
              isBusy = true
              val sampleFile = withContext(Dispatchers.IO) {
                val sampleDir = File(context.cacheDir, "sample_archives").apply { mkdirs() }
                ArchiveEngine.generateSampleArchive(sampleDir)
              }
              isBusy = false
              activeArchiveFile = sampleFile
              currentVirtualDir = ""
              selectedArchivePaths = emptySet()
              isSelectionMode = false
              archiveRevision++
            }
          },
          isLoading = isBusy,
          modifier = Modifier.weight(1f),
        )
      }
    } else {
      // ====================================================================
      // 2. FILE EXPLORER-STYLE VIEWING SCREEN (Similar to FilesScreen)
      // ====================================================================
      val visibleEntries = remember(currentInfo, currentVirtualDir, searchQuery) {
        ArchiveEngine.resolveVisibleEntries(currentInfo, currentVirtualDir, searchQuery)
      }
      val groupedEntries = remember(visibleEntries, groupOption, sortOrder) {
        ArchiveEngine.groupAndSortEntries(visibleEntries, groupOption, sortOrder)
      }
      val processedEntries = remember(groupedEntries) {
        groupedEntries.flatMap { it.items }
      }
      val selectedEntries = remember(processedEntries, selectedArchivePaths) {
        processedEntries.filter { it.archivePath in selectedArchivePaths }
      }
      val dirSummary = remember(currentInfo, currentVirtualDir) {
        ArchiveEngine.getVirtualDirectorySummary(currentInfo, currentVirtualDir)
      }

      val headerTitle = if (canGoUpInArchive) {
        ArchiveEngine.leafNameOf(currentVirtualDir)
      } else {
        currentInfo.file.name
      }

      val headerSubtext = if (canGoUpInArchive) {
        val folderSize = FileManagerEngine.formatFileSize(dirSummary.first)
        "$folderSize • ${currentInfo.file.name}"
      } else {
        val compSize = FileManagerEngine.formatFileSize(currentInfo.archiveFileSizeBytes)
        val uncompSize = FileManagerEngine.formatFileSize(currentInfo.totalUncompressedBytes)
        "${currentInfo.format.displayName} • $compSize ($uncompSize uncompressed)"
      }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .testTag("archive_explorer_view"),
      ) {
        // 1. Top Header Bar (Matching FilesHeader)
        ArchiveExplorerHeader(
          title = headerTitle,
          subtext = headerSubtext,
          canNavigateUp = canGoUpInArchive,
          onNavigateUp = { navigateUpInArchive() },
          onNavigateBack = onNavigateBack,
          isSearchActive = isSearchActive,
          searchQuery = searchQuery,
          onToggleSearch = {
            isSearchActive = !isSearchActive
            if (!isSearchActive) searchQuery = ""
          },
          onSearchQueryChange = { searchQuery = it },
          viewMode = viewMode,
          onToggleViewMode = {
            viewMode = if (viewMode == FileViewMode.LIST) FileViewMode.GRID else FileViewMode.LIST
          },
          sortOrder = sortOrder,
          groupOption = groupOption,
          showSortMenu = showSortMenu,
          onOpenSortMenu = { showSortMenu = true },
          onDismissSortMenu = { showSortMenu = false },
          onSelectSortOrder = {
            sortOrder = it
            showSortMenu = false
          },
          onSelectGroupOption = {
            groupOption = it
            showSortMenu = false
          },
          onExtractAll = {
            extractPathsTarget = emptySet()
          },
          onCloseArchive = {
            closeCurrentArchive()
          },
          isSelectionMode = isSelectionMode,
          selectedCount = selectedArchivePaths.size,
          totalCount = processedEntries.size,
          onToggleSelectAll = {
            selectedArchivePaths = if (selectedArchivePaths.size == processedEntries.size && processedEntries.isNotEmpty()) {
              emptySet()
            } else {
              processedEntries.map { it.archivePath }.toSet()
            }
          },
          onCloseSelection = {
            isSelectionMode = false
            selectedArchivePaths = emptySet()
          },
          onBulkExtract = {
            if (selectedArchivePaths.isNotEmpty()) {
              extractPathsTarget = selectedArchivePaths
            }
          },
          onBulkInfo = {
            if (selectedEntries.isNotEmpty()) {
              showBulkInfoDialog = true
            }
          },
          onBulkDelete = {
            if (selectedArchivePaths.isNotEmpty()) {
              showBulkDeleteDialog = true
            }
          },
        )

        // 2. Quick Action Pill Row (Matching StorageDevicePillRow in FilesScreen)
        ArchiveActionPillRow(
          archiveName = currentInfo.file.name,
          isAtRoot = !canGoUpInArchive,
          supportsModify = currentInfo.format.supportsMultiEntryWrite,
          onNavigateRoot = { navigateToVirtualDir("") },
          onExtractAll = { extractPathsTarget = emptySet() },
          onAddFiles = { showAddFilesDialog = true },
          onNewZip = {
            preselectedFilesForZip = emptyList()
            showCreateZipDialog = true
          },
          onShowArchiveInfo = { showArchiveInfoDialog = true },
        )

        // Optional status banner after extract / zip / add
        val banner = lastOperationBanner
        AnimatedVisibility(visible = banner != null) {
          if (banner != null) {
            ArchiveOperationBanner(
              result = banner,
              onDismiss = { lastOperationBanner = null },
            )
          }
        }

        // 3. Breadcrumb Path Bar & Summary (Matching BreadcrumbBar in FilesScreen)
        ArchiveBreadcrumbBar(
          archiveFileName = currentInfo.file.name,
          currentVirtualDir = currentVirtualDir,
          directorySummary = dirSummary,
          onNavigateToVirtualDir = { navigateToVirtualDir(it) },
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // 4. File List / Grid or Empty State
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        ) {
          if (processedEntries.isEmpty()) {
            EmptyArchiveEntriesView(
              isSearching = searchQuery.isNotEmpty(),
              searchQuery = searchQuery,
              supportsModify = currentInfo.format.supportsMultiEntryWrite,
              onAddFiles = { showAddFilesDialog = true },
              onCreateFolder = { showCreateFolderDialog = true },
            )
          } else {
            when (viewMode) {
              FileViewMode.LIST -> {
                LazyColumn(
                  modifier = Modifier
                    .fillMaxSize()
                    .testTag("archive_list_view"),
                  contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  groupedEntries.forEach { group ->
                    val isCollapsed = group.title.isNotEmpty() && group.title in collapsedGroupTitles
                    if (group.title.isNotEmpty()) {
                      item(key = "header_${group.title}") {
                        ArchiveGroupHeader(
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
                        key = { it.archivePath },
                      ) { item ->
                        ArchiveEntryListItem(
                          item = item,
                          isSelectionMode = isSelectionMode,
                          isSelected = item.archivePath in selectedArchivePaths,
                          supportsModify = currentInfo.format.supportsMultiEntryWrite,
                          onToggleSelect = {
                            selectedArchivePaths = if (item.archivePath in selectedArchivePaths) {
                              selectedArchivePaths - item.archivePath
                            } else {
                              selectedArchivePaths + item.archivePath
                            }
                          },
                          onClick = {
                            when {
                              item.isDirectory -> navigateToVirtualDir(item.archivePath)
                              ArchiveEngine.isTextEntry(item) -> entryForTextPreview = item
                              ArchiveEngine.isImageEntry(item) -> entryForImagePreview = item
                              else -> entryForDetails = item
                            }
                          },
                          onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (!isSelectionMode) {
                              isSelectionMode = true
                              selectedArchivePaths = setOf(item.archivePath)
                            } else {
                              selectedArchivePaths = if (item.archivePath in selectedArchivePaths) {
                                selectedArchivePaths - item.archivePath
                              } else {
                                selectedArchivePaths + item.archivePath
                              }
                            }
                          },
                          onPreviewText = { entryForTextPreview = item },
                          onPreviewImage = { entryForImagePreview = item },
                          onExtractEntry = { extractPathsTarget = setOf(item.archivePath) },
                          onOpenWith = { openEntryExternally(currentInfo, item, chooseApp = true) },
                          onShare = { shareArchiveEntry(currentInfo, item) },
                          onRename = { entryToRename = item },
                          onDetails = { entryForDetails = item },
                          onDelete = { entryToDelete = item },
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
                    .testTag("archive_grid_view"),
                  contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  groupedEntries.forEach { group ->
                    val isCollapsed = group.title.isNotEmpty() && group.title in collapsedGroupTitles
                    if (group.title.isNotEmpty()) {
                      item(
                        key = "header_${group.title}",
                        span = { GridItemSpan(maxLineSpan) },
                      ) {
                        ArchiveGroupHeader(
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
                        key = { it.archivePath },
                      ) { item ->
                        ArchiveEntryGridItem(
                          item = item,
                          isSelectionMode = isSelectionMode,
                          isSelected = item.archivePath in selectedArchivePaths,
                          supportsModify = currentInfo.format.supportsMultiEntryWrite,
                          onToggleSelect = {
                            selectedArchivePaths = if (item.archivePath in selectedArchivePaths) {
                              selectedArchivePaths - item.archivePath
                            } else {
                              selectedArchivePaths + item.archivePath
                            }
                          },
                          onClick = {
                            when {
                              item.isDirectory -> navigateToVirtualDir(item.archivePath)
                              ArchiveEngine.isTextEntry(item) -> entryForTextPreview = item
                              ArchiveEngine.isImageEntry(item) -> entryForImagePreview = item
                              else -> entryForDetails = item
                            }
                          },
                          onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (!isSelectionMode) {
                              isSelectionMode = true
                              selectedArchivePaths = setOf(item.archivePath)
                            } else {
                              selectedArchivePaths = if (item.archivePath in selectedArchivePaths) {
                                selectedArchivePaths - item.archivePath
                              } else {
                                selectedArchivePaths + item.archivePath
                              }
                            }
                          },
                          onPreviewText = { entryForTextPreview = item },
                          onPreviewImage = { entryForImagePreview = item },
                          onExtractEntry = { extractPathsTarget = setOf(item.archivePath) },
                          onOpenWith = { openEntryExternally(currentInfo, item, chooseApp = true) },
                          onShare = { shareArchiveEntry(currentInfo, item) },
                          onRename = { entryToRename = item },
                          onDetails = { entryForDetails = item },
                          onDelete = { entryToDelete = item },
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

      // Scrim overlay when speed-dial FAB is expanded
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

      // Speed-dial FAB menu (matching FilesScreen bottom-end FAB)
      if (!isSelectionMode) {
        Column(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 16.dp),
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
              // Mini FAB: Extract All
              SpeedDialMiniFabRow(
                label = "Extract archive",
                icon = Icons.Default.Unarchive,
                testTag = "fab_extract_all",
                onClick = {
                  isFabExpanded = false
                  extractPathsTarget = emptySet()
                },
              )

              if (currentInfo.format.supportsMultiEntryWrite) {
                // Mini FAB: Add Files to Archive
                SpeedDialMiniFabRow(
                  label = "Add files to archive",
                  icon = Icons.AutoMirrored.Filled.NoteAdd,
                  testTag = "fab_add_files",
                  onClick = {
                    isFabExpanded = false
                    showAddFilesDialog = true
                  },
                )

                // Mini FAB: New Folder in Archive
                SpeedDialMiniFabRow(
                  label = "New folder",
                  icon = Icons.Default.CreateNewFolder,
                  testTag = "fab_new_folder",
                  onClick = {
                    isFabExpanded = false
                    showCreateFolderDialog = true
                  },
                )
              }

              // Mini FAB: Create New Zip Archive
              SpeedDialMiniFabRow(
                label = "Create new zip",
                icon = Icons.Default.FolderZip,
                testTag = "fab_create_new_zip",
                onClick = {
                  isFabExpanded = false
                  preselectedFilesForZip = emptyList()
                  showCreateZipDialog = true
                },
              )
            }
          }

          val addIconRotation by animateFloatAsState(
            targetValue = if (isFabExpanded) 135f else 0f,
            label = "archive_fab_rotation",
          )

          FloatingActionButton(
            onClick = { isFabExpanded = !isFabExpanded },
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
            modifier = Modifier.testTag("action_archive_fab"),
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = if (isFabExpanded) "Close actions" else "Archive actions",
              modifier = Modifier.rotate(addIconRotation),
            )
          }
        }
      }
    }
  }

  // ============================================================================
  // DIALOGS
  // ============================================================================

  // 1. Open Archive Picker Dialog
  if (showOpenArchivePicker) {
    StorageFilePickerDialog(
      initialDirectory = defaultBrowseDir,
      onDismiss = { showOpenArchivePicker = false },
      onLaunchSystemPicker = {
        showOpenArchivePicker = false
        systemFilePickerLauncher.launch("*/*")
      },
      onFileSelected = { selectedFile ->
        showOpenArchivePicker = false
        if (ArchiveEngine.isSupportedArchive(selectedFile)) {
          activeArchiveFile = selectedFile
          currentVirtualDir = ""
          selectedArchivePaths = emptySet()
          isSelectionMode = false
          archiveRevision++
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
          preselectedFilesForZip = listOf(selectedFile)
          showCreateZipDialog = true
        }
      },
      title = "Select Archive to Open",
      fileFilter = { file -> ArchiveEngine.isSupportedArchive(file) },
    )
  }

  // 2. Create / Zip Archive Dialog
  if (showCreateZipDialog) {
    CreateZipArchiveDialog(
      initialDirectory = activeArchiveFile?.parentFile ?: defaultBrowseDir,
      initialSelectedFiles = preselectedFilesForZip,
      onDismiss = {
        showCreateZipDialog = false
        preselectedFilesForZip = emptyList()
      },
      onCreateArchive = { sourceFiles, outputFile, format, compressionLevel ->
        showCreateZipDialog = false
        preselectedFilesForZip = emptyList()
        coroutineScope.launch {
          isBusy = true
          val result = withContext(Dispatchers.IO) {
            ArchiveEngine.createArchive(
              sourceFiles = sourceFiles,
              outputFile = outputFile,
              format = format,
              compressionLevel = compressionLevel,
            )
          }
          isBusy = false
          lastOperationBanner = result
          if (result.success && result.outputFile != null) {
            activeArchiveFile = result.outputFile
            currentVirtualDir = ""
            selectedArchivePaths = emptySet()
            isSelectionMode = false
            archiveRevision++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          } else {
            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
          }
        }
      },
    )
  }

  // 3. Extract Archive / Selected Entries Dialog
  val currentExtractTarget = extractPathsTarget
  if (currentExtractTarget != null && archiveInfo != null) {
    val defaultDestDir = archiveInfo.file.parentFile?.takeIf { it.exists() && it.canWrite() }
      ?: defaultBrowseDir
    val archiveStem = FileConverterEngine.extractFileStem(archiveInfo.file.name)
    ExtractArchiveDialog(
      archiveInfo = archiveInfo,
      selectedPaths = currentExtractTarget,
      initialDestinationDir = defaultDestDir,
      defaultSubfolderName = archiveStem,
      onDismiss = { extractPathsTarget = null },
      onConfirmExtract = { destinationDir, preserveStructure, overwriteExisting ->
        extractPathsTarget = null
        coroutineScope.launch {
          isBusy = true
          val result = withContext(Dispatchers.IO) {
            if (currentExtractTarget.isEmpty()) {
              ArchiveEngine.extractEntries(
                archiveFile = archiveInfo.file,
                selectedArchivePaths = emptySet(),
                destinationDir = destinationDir,
                preservePaths = preserveStructure,
                overwrite = overwriteExisting,
                extractEntireArchive = true,
              )
            } else {
              ArchiveEngine.extractEntries(
                archiveFile = archiveInfo.file,
                selectedArchivePaths = currentExtractTarget,
                destinationDir = destinationDir,
                preservePaths = preserveStructure,
                overwrite = overwriteExisting,
                extractEntireArchive = false,
              )
            }
          }
          isBusy = false
          lastOperationBanner = result
          isSelectionMode = false
          selectedArchivePaths = emptySet()
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        }
      },
    )
  }

  // 4. Add Files to Existing Archive Dialog
  if (showAddFilesDialog && archiveInfo != null) {
    AddFilesToArchiveDialog(
      archiveName = archiveInfo.file.name,
      targetVirtualDir = currentVirtualDir,
      initialDirectory = archiveInfo.file.parentFile ?: defaultBrowseDir,
      onDismiss = { showAddFilesDialog = false },
      onConfirmAdd = { filesToAdd, compressionLevel ->
        showAddFilesDialog = false
        coroutineScope.launch {
          isBusy = true
          val result = withContext(Dispatchers.IO) {
            ArchiveEngine.addFilesToArchive(
              archiveFile = archiveInfo.file,
              filesToAdd = filesToAdd,
              targetVirtualDir = currentVirtualDir,
              compressionLevel = compressionLevel,
            )
          }
          isBusy = false
          lastOperationBanner = result
          if (result.success) {
            archiveRevision++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          }
          Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        }
      },
    )
  }

  // 5. Create Folder in Archive Dialog
  if (showCreateFolderDialog && archiveInfo != null) {
    CreateArchiveFolderDialog(
      onDismiss = { showCreateFolderDialog = false },
      onCreate = { folderName ->
        showCreateFolderDialog = false
        coroutineScope.launch {
          val result = withContext(Dispatchers.IO) {
            ArchiveEngine.createFolderInArchive(
              archiveFile = archiveInfo.file,
              parentVirtualDir = currentVirtualDir,
              folderName = folderName,
            )
          }
          if (result.success) {
            archiveRevision++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          }
          Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        }
      },
    )
  }

  // 6. Rename Entry in Archive Dialog
  val activeRenameEntry = entryToRename
  if (activeRenameEntry != null && archiveInfo != null) {
    RenameArchiveEntryDialog(
      item = activeRenameEntry,
      onDismiss = { entryToRename = null },
      onRename = { newName ->
        entryToRename = null
        coroutineScope.launch {
          val result = withContext(Dispatchers.IO) {
            ArchiveEngine.renameEntryInArchive(
              archiveFile = archiveInfo.file,
              oldArchivePath = activeRenameEntry.archivePath,
              newLeafName = newName,
            )
          }
          if (result.success) {
            archiveRevision++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          }
          Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        }
      },
    )
  }

  // 7. Single Entry Delete Confirmation Dialog
  val activeDeleteEntry = entryToDelete
  if (activeDeleteEntry != null && archiveInfo != null) {
    DeleteArchiveEntriesDialog(
      itemNames = listOf(activeDeleteEntry.name),
      onDismiss = { entryToDelete = null },
      onConfirmDelete = {
        entryToDelete = null
        coroutineScope.launch {
          val result = withContext(Dispatchers.IO) {
            ArchiveEngine.deleteEntriesFromArchive(
              archiveFile = archiveInfo.file,
              pathsToRemove = setOf(activeDeleteEntry.archivePath),
            )
          }
          if (result.success) {
            archiveRevision++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          }
          Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        }
      },
    )
  }

  // 8. Bulk Delete Confirmation Dialog
  if (showBulkDeleteDialog && archiveInfo != null) {
    val names = archiveInfo.allEntries
      .filter { it.archivePath in selectedArchivePaths }
      .map { it.name }
    DeleteArchiveEntriesDialog(
      itemNames = names,
      onDismiss = { showBulkDeleteDialog = false },
      onConfirmDelete = {
        val targets = selectedArchivePaths
        showBulkDeleteDialog = false
        coroutineScope.launch {
          val result = withContext(Dispatchers.IO) {
            ArchiveEngine.deleteEntriesFromArchive(
              archiveFile = archiveInfo.file,
              pathsToRemove = targets,
            )
          }
          if (result.success) {
            isSelectionMode = false
            selectedArchivePaths = emptySet()
            archiveRevision++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          }
          Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        }
      },
    )
  }

  // 9. Entry Details Dialog
  val activeDetailsEntry = entryForDetails
  if (activeDetailsEntry != null && archiveInfo != null) {
    ArchiveEntryDetailsDialog(
      item = activeDetailsEntry,
      archiveFile = archiveInfo.file,
      onDismiss = { entryForDetails = null },
      onExtract = {
        entryForDetails = null
        extractPathsTarget = setOf(activeDetailsEntry.archivePath)
      },
      onPreviewText = if (ArchiveEngine.isTextEntry(activeDetailsEntry)) {
        {
          entryForDetails = null
          entryForTextPreview = activeDetailsEntry
        }
      } else null,
      onPreviewImage = if (ArchiveEngine.isImageEntry(activeDetailsEntry)) {
        {
          entryForDetails = null
          entryForImagePreview = activeDetailsEntry
        }
      } else null,
    )
  }

  // 10. Bulk Info Dialog
  if (showBulkInfoDialog && archiveInfo != null) {
    val selectedList = archiveInfo.allEntries.filter { it.archivePath in selectedArchivePaths }
    ArchiveBulkInfoDialog(
      items = selectedList,
      onDismiss = { showBulkInfoDialog = false },
    )
  }

  // 11. Archive Container Summary Info Dialog
  if (showArchiveInfoDialog && archiveInfo != null) {
    ArchiveContainerInfoDialog(
      archiveInfo = archiveInfo,
      onDismiss = { showArchiveInfoDialog = false },
      onShareArchive = {
        showArchiveInfoDialog = false
        shareConvertedFile(context, archiveInfo.file, archiveInfo.format.mimeType)
      },
    )
  }

  // 12. In-Archive Text Viewer Dialog
  val activeTextEntry = entryForTextPreview
  if (activeTextEntry != null && archiveInfo != null) {
    ArchiveTextPreviewDialog(
      archiveFile = archiveInfo.file,
      entry = activeTextEntry,
      onDismiss = { entryForTextPreview = null },
      onExtract = {
        entryForTextPreview = null
        extractPathsTarget = setOf(activeTextEntry.archivePath)
      },
    )
  }

  // 13. In-Archive Image Preview Dialog
  val activeImageEntry = entryForImagePreview
  if (activeImageEntry != null && archiveInfo != null) {
    ArchiveImagePreviewDialog(
      archiveFile = archiveInfo.file,
      entry = activeImageEntry,
      onDismiss = { entryForImagePreview = null },
      onExtract = {
        entryForImagePreview = null
        extractPathsTarget = setOf(activeImageEntry.archivePath)
      },
      onShare = {
        shareArchiveEntry(archiveInfo, activeImageEntry)
      },
    )
  }
}

// ============================================================================
// INITIAL SCREEN COMPONENTS (Simple editor-style initial screen)
// ============================================================================

@Composable
private fun ArchiveInitialTopBar(
  onCreateZipClick: () -> Unit,
  onNavigateBack: (() -> Unit)? = null,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 68.dp)
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (onNavigateBack != null) {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_navigate_back"),
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
              imageVector = Icons.Default.FolderZip,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Archives",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "Tap + to open or zip files",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      TextButton(
        onClick = onCreateZipClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("action_create_archive"),
      ) {
        Icon(
          imageVector = Icons.Default.CreateNewFolder,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("New Zip")
      }
    }
  }
}

@Composable
private fun ArchiveInitialView(
  onOpenFile: () -> Unit,
  onCreateZip: () -> Unit,
  onUseSample: () -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp)
      .testTag("empty_state_card"),
  ) {
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .clip(RoundedCornerShape(24.dp))
        .clickable(enabled = !isLoading, onClick = onOpenFile)
        .padding(32.dp)
        .testTag("open_archive_button"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(72.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(32.dp),
              strokeWidth = 3.dp,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          } else {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Open archive",
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(36.dp),
            )
          }
        }
      }

      Text(
        text = "tap to open file",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.testTag("tap_to_open_file_text"),
      )
    }

    Column(
      modifier = Modifier.align(Alignment.BottomCenter),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      OutlinedButton(
        onClick = onCreateZip,
        enabled = !isLoading,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("create_zip_initial_button"),
      ) {
        Icon(
          imageVector = Icons.Default.FolderZip,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "create zip from files",
          style = MaterialTheme.typography.labelLarge,
        )
      }

      TextButton(
        onClick = onUseSample,
        enabled = !isLoading,
        modifier = Modifier.testTag("open_sample_archive_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Inventory2,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "or try sample archive",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

// ============================================================================
// FILE EXPLORER-STYLE VIEWING SCREEN COMPONENTS
// ============================================================================

@Composable
private fun ArchiveExplorerHeader(
  title: String,
  subtext: String,
  canNavigateUp: Boolean,
  onNavigateUp: () -> Unit,
  onNavigateBack: (() -> Unit)?,
  isSearchActive: Boolean,
  searchQuery: String,
  onToggleSearch: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  viewMode: FileViewMode,
  onToggleViewMode: () -> Unit,
  sortOrder: FileSortOrder,
  groupOption: FileGroupOption,
  showSortMenu: Boolean,
  onOpenSortMenu: () -> Unit,
  onDismissSortMenu: () -> Unit,
  onSelectSortOrder: (FileSortOrder) -> Unit,
  onSelectGroupOption: (FileGroupOption) -> Unit,
  onExtractAll: () -> Unit,
  onCloseArchive: () -> Unit,
  isSelectionMode: Boolean,
  selectedCount: Int,
  totalCount: Int,
  onToggleSelectAll: () -> Unit,
  onCloseSelection: () -> Unit,
  onBulkExtract: () -> Unit,
  onBulkInfo: () -> Unit,
  onBulkDelete: () -> Unit,
) {
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(isSearchActive) {
    if (isSearchActive) {
      try {
        focusRequester.requestFocus()
      } catch (_: Exception) {}
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
        // Multi-Selection Header Toolbar (Matching FilesHeader)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
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

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            var showOverflowMenu by remember { mutableStateOf(false) }
            val hasSelection = selectedCount > 0

            IconButton(
              onClick = onBulkExtract,
              enabled = hasSelection,
              modifier = Modifier
                .size(36.dp)
                .testTag("action_bulk_extract"),
            ) {
              Icon(
                Icons.Default.Unarchive,
                contentDescription = "Extract selected",
                tint = if (hasSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
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
                Icons.Default.Info,
                contentDescription = "Info",
                tint = if (hasSelection) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
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
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = if (hasSelection) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
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
                  text = { Text("Extract selected…") },
                  leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = null) },
                  enabled = hasSelection,
                  onClick = {
                    showOverflowMenu = false
                    onBulkExtract()
                  },
                )
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
                  text = { Text("Delete from archive", color = MaterialTheme.colorScheme.error) },
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
        // Standard Explorer Header Toolbar
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
            when {
              canNavigateUp -> {
                IconButton(
                  onClick = onNavigateUp,
                  modifier = Modifier
                    .size(36.dp)
                    .testTag("action_navigate_up"),
                ) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up")
                }
              }
              onNavigateBack != null -> {
                IconButton(
                  onClick = onNavigateBack,
                  modifier = Modifier
                    .size(36.dp)
                    .testTag("action_navigate_back"),
                ) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
              }
              else -> {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    Icons.Default.FolderZip,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                  )
                }
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
              )
              Text(
                text = subtext,
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
              Icon(Icons.Default.Search, contentDescription = "Search")
            }

            ViewModeSegmentedToggle(
              isListView = viewMode == FileViewMode.LIST,
              onSelectListView = { if (viewMode != FileViewMode.LIST) onToggleViewMode() },
              onSelectGridView = { if (viewMode == FileViewMode.LIST) onToggleViewMode() },
              onToggle = onToggleViewMode,
              toggleTestTag = "action_toggle_view_mode",
              listTestTag = "archive_view_mode_list",
              gridTestTag = "archive_view_mode_grid",
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
                  }

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

            IconButton(
              onClick = onExtractAll,
              modifier = Modifier.testTag("action_extract_all"),
            ) {
              Icon(Icons.Default.Unarchive, contentDescription = "Extract All")
            }

            IconButton(
              onClick = onCloseArchive,
              modifier = Modifier.testTag("action_close_archive"),
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close Archive")
            }
          }
        }
      }

      // Animated Expanding Search Bar (Matching FilesHeader)
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
                  "Search inside archive...",
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
                .testTag("archive_search_input"),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ArchiveActionPillRow(
  archiveName: String,
  isAtRoot: Boolean,
  supportsModify: Boolean,
  onNavigateRoot: () -> Unit,
  onExtractAll: () -> Unit,
  onAddFiles: () -> Unit,
  onNewZip: () -> Unit,
  onShowArchiveInfo: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FilterChip(
      selected = isAtRoot,
      onClick = onNavigateRoot,
      shape = RoundedCornerShape(20.dp),
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.FolderZip,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      },
      label = {
        Text(
          text = archiveName,
          fontSize = 13.sp,
          fontWeight = if (isAtRoot) FontWeight.SemiBold else FontWeight.Normal,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      },
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
      ),
      modifier = Modifier.testTag("chip_archive_root"),
    )

    FilterChip(
      selected = false,
      onClick = onExtractAll,
      shape = RoundedCornerShape(20.dp),
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Unarchive,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      },
      label = { Text("Extract All", fontSize = 13.sp) },
      modifier = Modifier.testTag("chip_extract_all"),
    )

    if (supportsModify) {
      FilterChip(
        selected = false,
        onClick = onAddFiles,
        shape = RoundedCornerShape(20.dp),
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        },
        label = { Text("Add Files", fontSize = 13.sp) },
        modifier = Modifier.testTag("chip_add_to_archive"),
      )
    }

    FilterChip(
      selected = false,
      onClick = onNewZip,
      shape = RoundedCornerShape(20.dp),
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.CreateNewFolder,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      },
      label = { Text("New Zip", fontSize = 13.sp) },
      modifier = Modifier.testTag("chip_new_zip"),
    )

    FilterChip(
      selected = false,
      onClick = onShowArchiveInfo,
      shape = RoundedCornerShape(20.dp),
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      },
      label = { Text("Info", fontSize = 13.sp) },
      modifier = Modifier.testTag("chip_archive_info"),
    )
  }
}

@Composable
private fun ArchiveOperationBanner(
  result: ArchiveOperationResult,
  onDismiss: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 4.dp)
      .testTag("archive_operation_banner"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (result.success) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
      } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
      },
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Info,
        contentDescription = null,
        tint = if (result.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = result.message,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      IconButton(
        onClick = onDismiss,
        modifier = Modifier.size(28.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Dismiss",
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
private fun ArchiveBreadcrumbBar(
  archiveFileName: String,
  currentVirtualDir: String,
  directorySummary: Pair<Long, Int>,
  onNavigateToVirtualDir: (String) -> Unit,
) {
  val breadcrumbs = remember(archiveFileName, currentVirtualDir) {
    ArchiveEngine.getBreadcrumbAncestors(archiveFileName, currentVirtualDir)
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
      breadcrumbs.takeLast(4).forEachIndexed { idx, (label, path) ->
        if (idx > 0) {
          Text(
            text = " / ",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 12.sp,
          )
        }
        val isCurrent = path == ArchiveEngine.normalizeArchivePath(currentVirtualDir)
        Text(
          text = label,
          color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
          fontSize = 12.sp,
          modifier = Modifier
            .clickable { onNavigateToVirtualDir(path) }
            .testTag("archive_breadcrumb_${if (path.isEmpty()) "root" else label}"),
        )
      }
    }

    val (totalBytes, itemCount) = directorySummary
    Text(
      text = "$itemCount items • ${FileManagerEngine.formatFileSize(totalBytes)}",
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun ArchiveGroupHeader(
  title: String,
  count: Int,
  isCollapsed: Boolean = false,
  onToggleCollapse: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val rotationState by animateFloatAsState(
    targetValue = if (isCollapsed) -90f else 0f,
    label = "archive_group_expand_rotation",
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("archive_group_header_${title.lowercase(Locale.ROOT).replace(" ", "_")}"),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArchiveEntryListItem(
  item: ArchiveEntryItem,
  isSelectionMode: Boolean,
  isSelected: Boolean,
  supportsModify: Boolean,
  onToggleSelect: () -> Unit,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onPreviewText: () -> Unit,
  onPreviewImage: () -> Unit,
  onExtractEntry: () -> Unit,
  onOpenWith: () -> Unit,
  onShare: () -> Unit,
  onRename: () -> Unit,
  onDetails: () -> Unit,
  onDelete: () -> Unit,
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
      .testTag("archive_item_${item.name}"),
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
        ArchiveEntryIconBox(item = item, size = 40.dp)
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
          val sizeDescription = if (item.isDirectory) {
            val count = "${item.childCount ?: 0} items"
            val size = FileManagerEngine.formatFileSize(item.sizeBytes)
            "$count • $size"
          } else {
            val uncomp = FileManagerEngine.formatFileSize(item.sizeBytes)
            if (item.compressionRatioPercent > 0) {
              "$uncomp (${item.compressionRatioPercent}% saved)"
            } else {
              uncomp
            }
          }
          Text(
            text = sizeDescription,
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
              .testTag("archive_entry_options_${item.name}"),
          ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
          }

          ArchiveEntryActionMenu(
            item = item,
            expanded = showMenu,
            supportsModify = supportsModify,
            onDismiss = { showMenu = false },
            onPreviewText = {
              showMenu = false
              onPreviewText()
            },
            onPreviewImage = {
              showMenu = false
              onPreviewImage()
            },
            onExtractEntry = {
              showMenu = false
              onExtractEntry()
            },
            onOpenWith = {
              showMenu = false
              onOpenWith()
            },
            onShare = {
              showMenu = false
              onShare()
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
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArchiveEntryGridItem(
  item: ArchiveEntryItem,
  isSelectionMode: Boolean,
  isSelected: Boolean,
  supportsModify: Boolean,
  onToggleSelect: () -> Unit,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onPreviewText: () -> Unit,
  onPreviewImage: () -> Unit,
  onExtractEntry: () -> Unit,
  onOpenWith: () -> Unit,
  onShare: () -> Unit,
  onRename: () -> Unit,
  onDetails: () -> Unit,
  onDelete: () -> Unit,
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
      .testTag("archive_grid_item_${item.name}"),
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
          ArchiveEntryIconBox(item = item, size = 44.dp)
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
            .padding(2.dp),
        ) {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier
              .size(28.dp)
              .testTag("archive_grid_options_${item.name}"),
          ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(16.dp))
          }

          ArchiveEntryActionMenu(
            item = item,
            expanded = showMenu,
            supportsModify = supportsModify,
            onDismiss = { showMenu = false },
            onPreviewText = {
              showMenu = false
              onPreviewText()
            },
            onPreviewImage = {
              showMenu = false
              onPreviewImage()
            },
            onExtractEntry = {
              showMenu = false
              onExtractEntry()
            },
            onOpenWith = {
              showMenu = false
              onOpenWith()
            },
            onShare = {
              showMenu = false
              onShare()
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
          )
        }
      }
    }
  }
}

@Composable
private fun ArchiveEntryIconBox(item: ArchiveEntryItem, size: Dp) {
  val (icon, bg, fg) = when {
    item.isDirectory -> Triple(
      Icons.Default.Folder,
      MaterialTheme.colorScheme.primaryContainer,
      MaterialTheme.colorScheme.primary,
    )
    ArchiveEngine.isImageEntry(item) -> Triple(
      Icons.Default.Image,
      MaterialTheme.colorScheme.tertiaryContainer,
      MaterialTheme.colorScheme.tertiary,
    )
    ArchiveEngine.isCodeEntry(item) -> Triple(
      Icons.Default.Code,
      MaterialTheme.colorScheme.secondaryContainer,
      MaterialTheme.colorScheme.secondary,
    )
    ArchiveEngine.isAudioEntry(item) -> Triple(
      Icons.Default.AudioFile,
      MaterialTheme.colorScheme.errorContainer,
      MaterialTheme.colorScheme.error,
    )
    ArchiveEngine.isVideoEntry(item) -> Triple(
      Icons.Default.VideoFile,
      MaterialTheme.colorScheme.errorContainer,
      MaterialTheme.colorScheme.error,
    )
    ArchiveEngine.isArchiveEntry(item) -> Triple(
      Icons.Default.FolderZip,
      MaterialTheme.colorScheme.surfaceContainerHighest,
      MaterialTheme.colorScheme.onSurface,
    )
    ArchiveEngine.isTextEntry(item) -> Triple(
      Icons.Default.Description,
      MaterialTheme.colorScheme.primaryContainer,
      MaterialTheme.colorScheme.primary,
    )
    else -> Triple(
      Icons.AutoMirrored.Filled.InsertDriveFile,
      MaterialTheme.colorScheme.surfaceVariant,
      MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
private fun ArchiveEntryActionMenu(
  item: ArchiveEntryItem,
  expanded: Boolean,
  supportsModify: Boolean,
  onDismiss: () -> Unit,
  onPreviewText: () -> Unit,
  onPreviewImage: () -> Unit,
  onExtractEntry: () -> Unit,
  onOpenWith: () -> Unit,
  onShare: () -> Unit,
  onRename: () -> Unit,
  onDetails: () -> Unit,
  onDelete: () -> Unit,
) {
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismiss,
  ) {
    if (!item.isDirectory) {
      if (ArchiveEngine.isTextEntry(item)) {
        DropdownMenuItem(
          text = { Text("View text") },
          leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
          onClick = onPreviewText,
          modifier = Modifier.testTag("action_preview_text_entry"),
        )
      } else if (ArchiveEngine.isImageEntry(item)) {
        DropdownMenuItem(
          text = { Text("Preview image") },
          leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
          onClick = onPreviewImage,
          modifier = Modifier.testTag("action_preview_image_entry"),
        )
      }

      DropdownMenuItem(
        text = { Text("Open with…") },
        leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) },
        onClick = onOpenWith,
        modifier = Modifier.testTag("action_open_entry_with"),
      )

      DropdownMenuItem(
        text = { Text("Share") },
        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
        onClick = onShare,
        modifier = Modifier.testTag("action_share_entry"),
      )
    }

    DropdownMenuItem(
      text = { Text("Extract…") },
      leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = null) },
      onClick = onExtractEntry,
      modifier = Modifier.testTag("action_extract_entry"),
    )

    if (supportsModify) {
      DropdownMenuItem(
        text = { Text("Rename") },
        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
        onClick = onRename,
        modifier = Modifier.testTag("action_rename_entry"),
      )
    }

    DropdownMenuItem(
      text = { Text("Info") },
      leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
      onClick = onDetails,
      modifier = Modifier.testTag("action_entry_info"),
    )

    if (supportsModify) {
      HorizontalDivider()
      DropdownMenuItem(
        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
        leadingIcon = {
          Icon(
            Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
          )
        },
        onClick = onDelete,
        modifier = Modifier.testTag("action_delete_entry"),
      )
    }
  }
}

@Composable
private fun EmptyArchiveEntriesView(
  isSearching: Boolean,
  searchQuery: String,
  supportsModify: Boolean,
  onAddFiles: () -> Unit,
  onCreateFolder: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = if (isSearching) Icons.Default.Search else Icons.Default.FolderZip,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(32.dp),
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = if (isSearching) "No matching entries" else "Folder is empty",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = if (isSearching) {
        "No files or folders inside the archive matched \"$searchQuery\""
      } else {
        "Add files or create a folder inside this archive"
      },
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    if (!isSearching && supportsModify) {
      Spacer(modifier = Modifier.height(16.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
          onClick = onCreateFolder,
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("New Folder")
        }
        Button(
          onClick = onAddFiles,
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Files")
        }
      }
    }
  }
}

@Composable
private fun SpeedDialMiniFabRow(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
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

// ============================================================================
// DIALOGS (Extract, Create Zip, Add Files, Text/Image Preview, Info)
// ============================================================================

@Composable
private fun ExtractArchiveDialog(
  archiveInfo: ArchiveInfo,
  selectedPaths: Set<String>,
  initialDestinationDir: File,
  defaultSubfolderName: String,
  onDismiss: () -> Unit,
  onConfirmExtract: (destinationDir: File, preserveStructure: Boolean, overwriteExisting: Boolean) -> Unit,
) {
  val rootDir = remember { FileManagerEngine.getUserRootDirectory() }
  var selectedBaseDir by remember {
    mutableStateOf(
      if (initialDestinationDir.exists() && initialDestinationDir.isDirectory) {
        initialDestinationDir
      } else {
        rootDir
      }
    )
  }
  var extractIntoSubfolder by rememberSaveable { mutableStateOf(selectedPaths.isEmpty()) }
  var subfolderName by rememberSaveable { mutableStateOf(defaultSubfolderName) }
  var preserveStructure by rememberSaveable { mutableStateOf(true) }
  var overwriteExisting by rememberSaveable { mutableStateOf(true) }

  val quickFolders = remember(rootDir, initialDestinationDir) {
    buildList {
      add("Archive Folder" to initialDestinationDir)
      add("Download" to File(rootDir, "Download"))
      add("Documents" to File(rootDir, "Documents"))
      add("Storage" to rootDir)
    }.distinctBy { it.second.absolutePath }
  }

  val targetDirectory = remember(selectedBaseDir, extractIntoSubfolder, subfolderName) {
    if (extractIntoSubfolder && subfolderName.isNotBlank()) {
      File(selectedBaseDir, subfolderName.trim())
    } else {
      selectedBaseDir
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("extract_archive_dialog"),
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
            imageVector = Icons.Default.Unarchive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (selectedPaths.isEmpty()) "Extract Archive" else "Extract ${selectedPaths.size} Selected",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = archiveInfo.file.name,
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "Destination folder",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          for ((label, folder) in quickFolders) {
            FilterChip(
              selected = selectedBaseDir.absolutePath == folder.absolutePath,
              onClick = { selectedBaseDir = folder },
              label = { Text(label) },
              shape = RoundedCornerShape(20.dp),
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { extractIntoSubfolder = !extractIntoSubfolder }
            .padding(vertical = 2.dp),
        ) {
          Checkbox(
            checked = extractIntoSubfolder,
            onCheckedChange = { extractIntoSubfolder = it },
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Create subfolder for extracted files",
            style = MaterialTheme.typography.bodyMedium,
          )
        }

        if (extractIntoSubfolder) {
          OutlinedTextField(
            value = subfolderName,
            onValueChange = { subfolderName = it },
            label = { Text("Subfolder name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("extract_subfolder_input"),
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { preserveStructure = !preserveStructure }
            .padding(vertical = 2.dp),
        ) {
          Checkbox(
            checked = preserveStructure,
            onCheckedChange = { preserveStructure = it },
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Preserve folder hierarchy",
            style = MaterialTheme.typography.bodyMedium,
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { overwriteExisting = !overwriteExisting }
            .padding(vertical = 2.dp),
        ) {
          Checkbox(
            checked = overwriteExisting,
            onCheckedChange = { overwriteExisting = it },
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Overwrite existing files",
            style = MaterialTheme.typography.bodyMedium,
          )
        }

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            Text(
              text = "Extracts to:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = targetDirectory.absolutePath,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirmExtract(targetDirectory, preserveStructure, overwriteExisting)
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_extract_button"),
      ) {
        Icon(Icons.Default.Unarchive, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Extract")
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CreateZipArchiveDialog(
  initialDirectory: File,
  initialSelectedFiles: List<File> = emptyList(),
  onDismiss: () -> Unit,
  onCreateArchive: (
    sourceFiles: List<File>,
    outputFile: File,
    format: ArchiveFormat,
    compressionLevel: ArchiveCompressionLevel,
  ) -> Unit,
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
  var selectedFiles by remember {
    mutableStateOf(initialSelectedFiles.filter { it.exists() }.distinctBy { it.absolutePath })
  }
  var selectedFormat by rememberSaveable { mutableStateOf(ArchiveFormat.ZIP) }
  var compressionLevel by rememberSaveable { mutableStateOf(ArchiveCompressionLevel.BALANCED) }

  val defaultStem = remember(initialSelectedFiles) {
    if (initialSelectedFiles.size == 1) {
      FileConverterEngine.extractFileStem(initialSelectedFiles.first().name)
    } else {
      "archive_${System.currentTimeMillis() / 1000L % 10000}"
    }
  }
  var archiveNameInput by rememberSaveable { mutableStateOf(defaultStem) }

  val browserEntries = remember(currentDir) {
    val rawList = currentDir.listFiles()?.toList() ?: emptyList()
    rawList
      .filter { !it.name.startsWith(".") }
      .sortedWith(
        compareByDescending<File> { it.isDirectory }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      )
  }

  val writableFormats = remember {
    listOf(ArchiveFormat.ZIP, ArchiveFormat.TAR, ArchiveFormat.TAR_GZ)
  }

  val sanitizedStem = archiveNameInput.trim()
    .removeSuffix(".${selectedFormat.extension}")
    .replace(Regex("[\\\\/:*?\"<>|]"), "_")
    .ifEmpty { "archive" }
  val outputFile = File(currentDir, "$sanitizedStem.${selectedFormat.extension}")

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("create_zip_dialog"),
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
            imageVector = Icons.Default.FolderZip,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Create Archive",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${selectedFiles.size} ${if (selectedFiles.size == 1) "item" else "items"} selected",
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
          .heightIn(max = 460.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // 1. Archive filename input
        OutlinedTextField(
          value = archiveNameInput,
          onValueChange = { archiveNameInput = it },
          label = { Text("Archive name (.${selectedFormat.extension})") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("zip_name_input"),
        )

        // 2. Format & Compression Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          for (fmt in writableFormats) {
            FilterChip(
              selected = selectedFormat == fmt,
              onClick = { selectedFormat = fmt },
              label = { Text(fmt.displayName) },
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.testTag("zip_format_chip_${fmt.name}"),
            )
          }
        }

        if (selectedFormat == ArchiveFormat.ZIP) {
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            for (level in ArchiveCompressionLevel.entries) {
              FilterChip(
                selected = compressionLevel == level,
                onClick = { compressionLevel = level },
                label = { Text(level.shortLabel, fontSize = 12.sp) },
                shape = RoundedCornerShape(16.dp),
              )
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // 3. Folder navigation header to select files/folders to zip
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          val parent = currentDir.parentFile
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
          ) {
            IconButton(
              onClick = { if (parent != null && parent.canRead()) currentDir = parent },
              enabled = parent != null && parent.canRead(),
              modifier = Modifier.size(32.dp),
            ) {
              Icon(Icons.Default.ArrowUpward, contentDescription = "Parent folder", modifier = Modifier.size(18.dp))
            }
            Text(
              text = currentDir.name.ifEmpty { "/" },
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          TextButton(
            onClick = {
              selectedFiles = if (browserEntries.all { entry -> selectedFiles.any { it.absolutePath == entry.absolutePath } }) {
                val dirSet = browserEntries.map { it.absolutePath }.toSet()
                selectedFiles.filterNot { it.absolutePath in dirSet }
              } else {
                (selectedFiles + browserEntries).distinctBy { it.absolutePath }
              }
            },
          ) {
            Text("Select All", fontSize = 12.sp)
          }
        }

        if (browserEntries.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "No files in this folder",
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
            items(browserEntries, key = { it.absolutePath }) { item ->
              val isChecked = selectedFiles.any { it.absolutePath == item.absolutePath }
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isChecked) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                  },
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    selectedFiles = if (isChecked) {
                      selectedFiles.filterNot { it.absolutePath == item.absolutePath }
                    } else {
                      selectedFiles + item
                    }
                  }
                  .testTag("zip_pick_item_${item.name}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                      selectedFiles = if (checked) {
                        (selectedFiles + item).distinctBy { it.absolutePath }
                      } else {
                        selectedFiles.filterNot { it.absolutePath == item.absolutePath }
                      }
                    },
                    modifier = Modifier.size(28.dp),
                  )
                  Icon(
                    imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                  )
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.name,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = if (item.isDirectory) "Folder" else FileManagerEngine.formatFileSize(item.length()),
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  if (item.isDirectory) {
                    TextButton(
                      onClick = { currentDir = item },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                      Text("Open", fontSize = 12.sp)
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
      Button(
        onClick = {
          onCreateArchive(selectedFiles, outputFile, selectedFormat, compressionLevel)
        },
        enabled = selectedFiles.isNotEmpty() && sanitizedStem.isNotEmpty(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_create_zip_button"),
      ) {
        Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Zip (${selectedFiles.size})")
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
private fun AddFilesToArchiveDialog(
  archiveName: String,
  targetVirtualDir: String,
  initialDirectory: File,
  onDismiss: () -> Unit,
  onConfirmAdd: (filesToAdd: List<File>, compressionLevel: ArchiveCompressionLevel) -> Unit,
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
  var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }

  val entries = remember(currentDir) {
    val rawList = currentDir.listFiles()?.toList() ?: emptyList()
    rawList
      .filter { !it.name.startsWith(".") }
      .sortedWith(
        compareByDescending<File> { it.isDirectory }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      )
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
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Add to Archive",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = if (targetVirtualDir.isEmpty()) archiveName else "$archiveName / $targetVirtualDir",
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
          .heightIn(max = 380.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val parent = currentDir.parentFile
          IconButton(
            onClick = { if (parent != null && parent.canRead()) currentDir = parent },
            enabled = parent != null && parent.canRead(),
            modifier = Modifier.size(32.dp),
          ) {
            Icon(Icons.Default.ArrowUpward, contentDescription = "Parent folder", modifier = Modifier.size(18.dp))
          }
          Text(
            text = currentDir.absolutePath,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          items(entries, key = { it.absolutePath }) { item ->
            val isChecked = selectedFiles.any { it.absolutePath == item.absolutePath }
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isChecked) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  selectedFiles = if (isChecked) {
                    selectedFiles.filterNot { it.absolutePath == item.absolutePath }
                  } else {
                    selectedFiles + item
                  }
                },
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Checkbox(
                  checked = isChecked,
                  onCheckedChange = { checked ->
                    selectedFiles = if (checked) {
                      (selectedFiles + item).distinctBy { it.absolutePath }
                    } else {
                      selectedFiles.filterNot { it.absolutePath == item.absolutePath }
                    }
                  },
                  modifier = Modifier.size(28.dp),
                )
                Icon(
                  imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                  contentDescription = null,
                  tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = if (item.isDirectory) "Folder" else FileManagerEngine.formatFileSize(item.length()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                if (item.isDirectory) {
                  TextButton(onClick = { currentDir = item }) {
                    Text("Open", fontSize = 12.sp)
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
        onClick = { onConfirmAdd(selectedFiles, ArchiveCompressionLevel.BALANCED) },
        enabled = selectedFiles.isNotEmpty(),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Add (${selectedFiles.size})")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun CreateArchiveFolderDialog(
  onDismiss: () -> Unit,
  onCreate: (String) -> Unit,
) {
  var folderName by remember { mutableStateOf("") }
  val error = remember(folderName) {
    if (folderName.isEmpty()) null else FileManagerEngine.validateFileName(folderName)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    title = { Text("New Folder in Archive", fontWeight = FontWeight.Bold) },
    text = {
      OutlinedTextField(
        value = folderName,
        onValueChange = { folderName = it },
        label = { Text("Folder name") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("archive_new_folder_input"),
      )
    },
    confirmButton = {
      Button(
        onClick = { onCreate(folderName.trim()) },
        enabled = folderName.isNotBlank() && error == null,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun RenameArchiveEntryDialog(
  item: ArchiveEntryItem,
  onDismiss: () -> Unit,
  onRename: (String) -> Unit,
) {
  var nameInput by remember(item.name) { mutableStateOf(item.name) }
  val error = remember(nameInput) {
    if (nameInput.isEmpty()) null else FileManagerEngine.validateFileName(nameInput)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    title = { Text("Rename Entry", fontWeight = FontWeight.Bold) },
    text = {
      OutlinedTextField(
        value = nameInput,
        onValueChange = { nameInput = it },
        label = { Text("New name") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("archive_rename_input"),
      )
    },
    confirmButton = {
      Button(
        onClick = { onRename(nameInput.trim()) },
        enabled = nameInput.isNotBlank() && error == null,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Rename")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun DeleteArchiveEntriesDialog(
  itemNames: List<String>,
  onDismiss: () -> Unit,
  onConfirmDelete: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    title = {
      Text(
        text = if (itemNames.size == 1) "Delete Entry?" else "Delete ${itemNames.size} Entries?",
        fontWeight = FontWeight.Bold,
      )
    },
    text = {
      Text(
        text = if (itemNames.size == 1) {
          "Remove \"${itemNames.first()}\" from this archive?"
        } else {
          "Remove ${itemNames.size} selected items from this archive?"
        },
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirmDelete,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_delete_archive_entry"),
      ) {
        Text("Delete")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun ArchiveEntryDetailsDialog(
  item: ArchiveEntryItem,
  archiveFile: File,
  onDismiss: () -> Unit,
  onExtract: () -> Unit,
  onPreviewText: (() -> Unit)? = null,
  onPreviewImage: (() -> Unit)? = null,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("archive_entry_details_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        ArchiveEntryIconBox(item = item, size = 44.dp)
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = if (item.isDirectory) "Archive Folder" else item.mimeType,
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ArchiveDetailRow("Path in archive", item.archivePath)
        ArchiveDetailRow("Archive file", archiveFile.name)
        if (item.isDirectory) {
          ArchiveDetailRow("Direct items", "${item.childCount ?: 0}")
        }
        ArchiveDetailRow(
          "Uncompressed size",
          "${FileManagerEngine.formatFileSize(item.sizeBytes)} (${item.sizeBytes} B)",
        )
        ArchiveDetailRow(
          "Compressed size",
          "${FileManagerEngine.formatFileSize(item.compressedSizeBytes)} (${item.compressedSizeBytes} B)",
        )
        if (!item.isDirectory) {
          ArchiveDetailRow("Space saved", "${item.compressionRatioPercent}%")
          if (item.crc32 != 0L) {
            ArchiveDetailRow("CRC-32", item.crc32.toString(16).uppercase(Locale.ROOT).padStart(8, '0'))
          }
        }
        ArchiveDetailRow("Modified", FileManagerEngine.formatTimestamp(item.lastModifiedMs))

        if (onPreviewText != null || onPreviewImage != null) {
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedButton(
            onClick = { onPreviewText?.invoke() ?: onPreviewImage?.invoke() },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(
              imageVector = if (onPreviewText != null) Icons.Default.Description else Icons.Default.Image,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (onPreviewText != null) "View Text Content" else "Preview Image")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onExtract,
        shape = RoundedCornerShape(12.dp),
      ) {
        Icon(Icons.Default.Unarchive, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Extract…")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun ArchiveBulkInfoDialog(
  items: List<ArchiveEntryItem>,
  onDismiss: () -> Unit,
) {
  val info = remember(items) { ArchiveEngine.getBulkEntryInfo(items) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    title = { Text("Selection Info (${info.totalItems} items)", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ArchiveDetailRow("Files", "${info.fileCount}")
        ArchiveDetailRow("Folders", "${info.folderCount}")
        ArchiveDetailRow("Uncompressed size", info.formattedUncompressedSize)
        ArchiveDetailRow("Compressed size", info.formattedCompressedSize)
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Done")
      }
    },
  )
}

@Composable
private fun ArchiveContainerInfoDialog(
  archiveInfo: ArchiveInfo,
  onDismiss: () -> Unit,
  onShareArchive: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.FolderZip,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = archiveInfo.file.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${archiveInfo.format.displayName} Archive",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ArchiveDetailRow("Location", archiveInfo.file.absolutePath)
        ArchiveDetailRow("Format", "${archiveInfo.format.displayName} (.${archiveInfo.format.extension})")
        ArchiveDetailRow("Total files", "${archiveInfo.totalFileCount}")
        ArchiveDetailRow("Total folders", "${archiveInfo.totalFolderCount}")
        ArchiveDetailRow(
          "Archive file size",
          FileManagerEngine.formatFileSize(archiveInfo.archiveFileSizeBytes),
        )
        ArchiveDetailRow(
          "Uncompressed size",
          FileManagerEngine.formatFileSize(archiveInfo.totalUncompressedBytes),
        )
        ArchiveDetailRow("Overall compression", "${archiveInfo.overallCompressionPercent}% saved")
      }
    },
    confirmButton = {
      Button(onClick = onShareArchive, shape = RoundedCornerShape(12.dp)) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Share")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun ArchiveDetailRow(label: String, value: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        RoundedCornerShape(10.dp),
      )
      .padding(horizontal = 12.dp, vertical = 8.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun ArchiveTextPreviewDialog(
  archiveFile: File,
  entry: ArchiveEntryItem,
  onDismiss: () -> Unit,
  onExtract: () -> Unit,
) {
  val context = LocalContext.current
  val textContent = remember(archiveFile.absolutePath, entry.archivePath) {
    ArchiveEngine.readEntryText(archiveFile, entry.archivePath) ?: ""
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .testTag("archive_text_preview_dialog"),
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = entry.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "${entry.archivePath} • ${FileManagerEngine.formatFileSize(entry.sizeBytes)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
              clipboard?.setPrimaryClip(ClipData.newPlainText(entry.name, textContent))
              Toast.makeText(context, "Copied text to clipboard", Toast.LENGTH_SHORT).show()
            },
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
          }
          Button(
            onClick = onExtract,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(start = 4.dp),
          ) {
            Icon(Icons.Default.Unarchive, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Extract")
          }
        }

        HorizontalDivider()

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        ) {
          Text(
            text = textContent,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = FontFamily.Monospace,
            ),
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@Composable
private fun ArchiveImagePreviewDialog(
  archiveFile: File,
  entry: ArchiveEntryItem,
  onDismiss: () -> Unit,
  onExtract: () -> Unit,
  onShare: () -> Unit,
) {
  val bitmap = remember(archiveFile.absolutePath, entry.archivePath) {
    try {
      val bytes = ArchiveEngine.readEntryBytes(archiveFile, entry.archivePath)
      if (bytes != null && bytes.isNotEmpty()) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
      } else {
        null
      }
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
        .padding(16.dp)
        .testTag("archive_image_preview_dialog"),
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
          Text(
            text = entry.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          IconButton(onClick = onShare) {
            Icon(Icons.Default.Share, contentDescription = "Share")
          }
          Button(
            onClick = onExtract,
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Default.Unarchive, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Extract")
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
              contentDescription = entry.name,
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            Text("Unable to decode image from archive", color = MaterialTheme.colorScheme.error)
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
            text = FileManagerEngine.formatFileSize(entry.sizeBytes),
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }
  }
}

// ============================================================================
// PREVIEW SAMPLE HELPERS
// ============================================================================

private fun getPreviewSampleArchiveInfo(file: File): ArchiveInfo {
  val now = System.currentTimeMillis()
  val entries = listOf(
    ArchiveEntryItem(
      name = "Documents",
      archivePath = "Documents",
      parentArchivePath = "",
      isDirectory = true,
      sizeBytes = 28_400L,
      compressedSizeBytes = 11_200L,
      lastModifiedMs = now - 3600_000L,
      extension = "",
      mimeType = "inode/directory",
      childCount = 2,
    ),
    ArchiveEntryItem(
      name = "Images",
      archivePath = "Images",
      parentArchivePath = "",
      isDirectory = true,
      sizeBytes = 64_200L,
      compressedSizeBytes = 48_100L,
      lastModifiedMs = now - 7200_000L,
      extension = "",
      mimeType = "inode/directory",
      childCount = 1,
    ),
    ArchiveEntryItem(
      name = "README.md",
      archivePath = "README.md",
      parentArchivePath = "",
      isDirectory = false,
      sizeBytes = 2_450L,
      compressedSizeBytes = 1_120L,
      lastModifiedMs = now - 1800_000L,
      extension = "md",
      mimeType = "text/markdown",
    ),
    ArchiveEntryItem(
      name = "config.json",
      archivePath = "config.json",
      parentArchivePath = "",
      isDirectory = false,
      sizeBytes = 980L,
      compressedSizeBytes = 420L,
      lastModifiedMs = now - 900_000L,
      extension = "json",
      mimeType = "application/json",
    ),
  )
  return ArchiveInfo(
    file = file,
    format = ArchiveFormat.ZIP,
    allEntries = entries,
    totalFileCount = 2,
    totalFolderCount = 2,
    totalUncompressedBytes = 96_030L,
    totalCompressedBytes = 60_840L,
    archiveFileSizeBytes = 60_840L,
  )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ArchiveScreenInitialPreview() {
  ToolboxTheme {
    ArchiveScreen()
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ArchiveScreenExplorerPreview() {
  ToolboxTheme {
    ArchiveScreen(
      initialFile = File("/storage/emulated/0/Download/sample_project_bundle.zip"),
    )
  }
}
