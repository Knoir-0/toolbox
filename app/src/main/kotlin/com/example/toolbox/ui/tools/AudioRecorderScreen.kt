package com.example.toolbox.ui.tools

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import com.example.toolbox.ui.theme.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import com.example.toolbox.ui.theme.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.toolbox.ui.theme.FilledIconButton
import com.example.toolbox.ui.theme.FilterChip
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import androidx.compose.material3.FilterChipDefaults
import com.example.toolbox.ui.theme.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.example.toolbox.ui.theme.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.toolbox.ui.theme.TextButton
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

import com.example.toolbox.ui.theme.toolboxScreenBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorderScreen(
  modifier: Modifier = Modifier,
  initialDirectory: File? = null,
  initialFormat: AudioRecordingFormat? = null,
  initialGrantedFolders: Set<AudioRecorderDefaultFolder>? = null,
  initialHasFullFilesPermission: Boolean? = null,
  onRequestFolderSystemAccess: ((AudioRecorderDefaultFolder, (Boolean) -> Unit) -> Unit)? = null,
  onRequestFolderPathSystemAccess: ((AudioRecorderFolder, (Boolean) -> Unit) -> Unit)? = null,
  onRequestAddCustomFolder: (((AudioRecorderFolder?) -> Unit) -> Unit)? = null,
  onOpenAudioEditor: ((File) -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

  val prefs = remember(context, isInspection) {
    if (isInspection) null
    else context.getSharedPreferences(AUDIO_RECORDER_PREFS_NAME, Context.MODE_PRIVATE)
  }

  val userRoot = remember(isInspection) {
    if (isInspection) File("/storage/emulated/0") else FileManagerEngine.getUserRootDirectory()
  }

  val fallbackBaseDir = remember(context, isInspection) {
    if (isInspection) null else (context.getExternalFilesDir(null) ?: context.filesDir)
  }

  // Selected recording format (persisted, defaults to M4A)
  var selectedFormat by rememberSaveable {
    val savedFormatName = prefs?.getString(PREF_KEY_AUDIO_FORMAT, null)
    mutableStateOf(initialFormat ?: AudioRecordingFormat.fromNameOrDefault(savedFormatName))
  }

  fun updateSelectedFormat(format: AudioRecordingFormat) {
    selectedFormat = format
    prefs?.edit()?.putString(PREF_KEY_AUDIO_FORMAT, format.name)?.apply()
    if (!isInspection) {
      AudioRecorderWidgets.updateAllWidgets(context)
    }
  }

  val effectiveFallbackDir = remember(initialDirectory, userRoot, fallbackBaseDir) {
    if (initialDirectory != null && !initialDirectory.absolutePath.startsWith(userRoot.absolutePath)) {
      initialDirectory.parentFile ?: fallbackBaseDir
    } else {
      fallbackBaseDir
    }
  }

  val storagePermissionSettingGranted = com.example.toolbox.ui.main.NavigationPreferencesBridge.isPermissionGranted(
    com.example.toolbox.ui.main.AppPermission.STORAGE
  )
  var systemHasFullFilesPermission by remember(context, isInspection) {
    mutableStateOf(if (isInspection) false else FileManagerEngine.hasStoragePermission(context))
  }
  DisposableEffect(lifecycleOwner, context, isInspection) {
    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
      if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && !isInspection) {
        systemHasFullFilesPermission = FileManagerEngine.hasStoragePermission(context)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
  val hasFullFilesPermission: Boolean = initialHasFullFilesPermission
    ?: (!isInspection && storagePermissionSettingGranted && systemHasFullFilesPermission)

  // Ordered list of navigation folder pills (defaults + any custom folders added via scoped storage)
  // Downloads is hidden under scoped storage and re-appears when full files permission is granted.
  var rawFolderPills by remember(isInspection) {
    mutableStateOf(
      if (isInspection) FULL_FILES_DEFAULT_AUDIO_RECORDER_FOLDERS
      else AudioRecorderEngine.getFolderPills(context, hasFullFilesPermission = true)
    )
  }
  val folderPills: List<AudioRecorderFolder> = remember(rawFolderPills, hasFullFilesPermission) {
    AudioRecorderEngine.filterFolderPillsForPermission(
      folders = rawFolderPills,
      hasFullFilesPermission = hasFullFilesPermission,
    )
  }

  // Scoped folder access states (initially empty until at least one folder is granted access, or unlocked for Downloads when full files permission is granted)
  var grantedFolderPaths by remember(initialGrantedFolders, isInspection) {
    mutableStateOf(
      when {
        initialGrantedFolders != null -> initialGrantedFolders.map { it.displayPath }.toSet()
        isInspection -> emptySet()
        else -> AudioRecorderEngine.getGrantedFolderPaths(context)
      }
    )
  }

  val effectiveGrantedFolderPaths: Set<String> = remember(
    grantedFolderPaths,
    hasFullFilesPermission,
    initialGrantedFolders,
  ) {
    if (hasFullFilesPermission && initialGrantedFolders == null) {
      grantedFolderPaths + AudioRecorderDefaultFolder.DOWNLOADS.displayPath
    } else {
      grantedFolderPaths
    }
  }

  fun isFolderGranted(folder: AudioRecorderFolder): Boolean {
    return effectiveGrantedFolderPaths.any { it.equals(folder.displayPath, ignoreCase = true) }
  }

  // Folder display path for which the in-app permission view is currently displayed after tapping a lock icon
  var pendingPermissionFolderPath by rememberSaveable {
    mutableStateOf<String?>(null)
  }
  val pendingPermissionFolder: AudioRecorderFolder? = remember(pendingPermissionFolderPath, folderPills) {
    pendingPermissionFolderPath?.let { path ->
      folderPills.find { it.displayPath.equals(path, ignoreCase = true) }
    }
  }

  // Folder display path currently awaiting the Android system directory access prompt result
  var pendingSystemPromptFolderPath by rememberSaveable {
    mutableStateOf<String?>(null)
  }

  // Currently selected folder display path (defaults to /Audio)
  var selectedFolderPath by rememberSaveable {
    val savedPath = prefs?.getString(PREF_KEY_AUDIO_DIRECTORY, null)
    val initialPath = when {
      !savedPath.isNullOrBlank() ->
        if (savedPath.startsWith("/")) savedPath else "/$savedPath"
      initialDirectory != null ->
        AudioRecorderDefaultFolder.fromPathOrName(initialDirectory.name)?.displayPath
          ?: AudioRecorderDefaultFolder.DEFAULT.displayPath
      else -> AudioRecorderDefaultFolder.DEFAULT.displayPath
    }
    mutableStateOf(initialPath)
  }

  val selectedFolder: AudioRecorderFolder = remember(selectedFolderPath, folderPills) {
    folderPills.find { it.displayPath.equals(selectedFolderPath, ignoreCase = true) }
      ?: folderPills.firstOrNull { isFolderGranted(it) }
      ?: folderPills.firstOrNull()
      ?: AudioRecorderDefaultFolder.DEFAULT.toAudioRecorderFolder()
  }

  val isSelectedFolderGranted = isFolderGranted(selectedFolder)
  val isRecorderEnabled = isSelectedFolderGranted

  // User-facing folder display string (e.g. "/Audio", "/Recordings", "/Download", or custom "/Podcasts")
  val directoryDisplayPath = selectedFolder.displayPath

  fun resolveDirectoryForFolder(folder: AudioRecorderFolder): File {
    return when {
      isInspection -> File(userRoot, folder.displayPath.trimStart('/'))
      !folder.customAbsolutePath.isNullOrBlank() ->
        File(folder.customAbsolutePath).also { it.mkdirs() }
      initialDirectory != null &&
        folder.defaultFolder == AudioRecorderDefaultFolder.AUDIO &&
        (initialDirectory.name.startsWith("Audio", ignoreCase = true) ||
          AudioRecorderDefaultFolder.fromPathOrName(initialDirectory.name) == null) -> {
        initialDirectory
      }
      initialDirectory != null &&
        folder.defaultFolder != null &&
        AudioRecorderDefaultFolder.fromPathOrName(initialDirectory.name) == folder.defaultFolder -> {
        initialDirectory
      }
      initialDirectory != null &&
        !initialDirectory.absolutePath.startsWith(userRoot.absolutePath) -> {
        val baseDir = initialDirectory.parentFile ?: effectiveFallbackDir ?: userRoot
        File(baseDir, folder.displayPath.trimStart('/')).also { it.mkdirs() }
      }
      else -> {
        AudioRecorderEngine.resolveDirectoryInput(
          inputPath = folder.displayPath,
          userRoot = userRoot,
          fallbackBaseDir = effectiveFallbackDir,
        )
      }
    }
  }

  // Save & list location always defaults to the currently selected folder
  val recordingsDirectory = remember(selectedFolder, initialDirectory, userRoot, effectiveFallbackDir, isInspection) {
    resolveDirectoryForFolder(selectedFolder)
  }

  // Sort & group configuration (persisted globally or per-folder)
  var defaultSortGroupConfig by remember {
    mutableStateOf(
      if (isInspection) AudioRecorderSortGroupConfig()
      else AudioRecorderEngine.getDefaultSortGroupConfig(context)
    )
  }

  val initialResolvedSortGroup = remember(directoryDisplayPath) {
    if (isInspection) {
      defaultSortGroupConfig to false
    } else {
      AudioRecorderEngine.resolveSortGroupConfig(context, directoryDisplayPath)
    }
  }

  var groupOption by rememberSaveable {
    mutableStateOf(initialResolvedSortGroup.first.groupOption)
  }
  var sortOrder by rememberSaveable {
    mutableStateOf(initialResolvedSortGroup.first.sortOrder)
  }
  var isSortGroupSavedForFolder by rememberSaveable(directoryDisplayPath) {
    mutableStateOf(initialResolvedSortGroup.second)
  }
  var showFilterMenu by remember { mutableStateOf(false) }
  var collapsedGroupTitles by remember(directoryDisplayPath, groupOption) {
    mutableStateOf(setOf<String>())
  }

  fun syncSortGroupForFolder(folderKey: String) {
    if (isInspection) return
    val (resolved, saved) = AudioRecorderEngine.resolveSortGroupConfig(context, folderKey)
    groupOption = resolved.groupOption
    sortOrder = resolved.sortOrder
    isSortGroupSavedForFolder = saved
  }

  fun updateGroupOption(newGroup: AudioRecorderGroupOption) {
    groupOption = newGroup
    val updated = AudioRecorderSortGroupConfig(groupOption = newGroup, sortOrder = sortOrder)
    if (!isInspection) {
      if (isSortGroupSavedForFolder) {
        AudioRecorderEngine.setFolderSortGroupConfig(context, directoryDisplayPath, updated)
      } else {
        defaultSortGroupConfig = updated
        AudioRecorderEngine.setDefaultSortGroupConfig(context, updated)
      }
    }
  }

  fun updateSortOrder(newSort: AudioRecorderSortOrder) {
    sortOrder = newSort
    val updated = AudioRecorderSortGroupConfig(groupOption = groupOption, sortOrder = newSort)
    if (!isInspection) {
      if (isSortGroupSavedForFolder) {
        AudioRecorderEngine.setFolderSortGroupConfig(context, directoryDisplayPath, updated)
      } else {
        defaultSortGroupConfig = updated
        AudioRecorderEngine.setDefaultSortGroupConfig(context, updated)
      }
    }
  }

  fun toggleSaveSortGroupForFolder() {
    val newSaved = !isSortGroupSavedForFolder
    isSortGroupSavedForFolder = newSaved
    if (!isInspection) {
      if (newSaved) {
        val config = AudioRecorderSortGroupConfig(groupOption = groupOption, sortOrder = sortOrder)
        AudioRecorderEngine.setFolderSortGroupConfig(context, directoryDisplayPath, config)
      } else {
        AudioRecorderEngine.removeFolderSortGroupConfig(context, directoryDisplayPath)
        groupOption = defaultSortGroupConfig.groupOption
        sortOrder = defaultSortGroupConfig.sortOrder
      }
    }
  }

  fun selectRecordingsFolder(folder: AudioRecorderFolder) {
    selectedFolderPath = folder.displayPath
    prefs?.edit()?.putString(PREF_KEY_AUDIO_DIRECTORY, folder.displayPath)?.apply()
    syncSortGroupForFolder(folder.displayPath)
  }

  // Recording state
  var isRecording by remember { mutableStateOf(false) }
  var activeCaptureSource by rememberSaveable { mutableStateOf(AudioCaptureSource.MICROPHONE) }
  var elapsedRecordingMs by remember { mutableLongStateOf(0L) }
  var liveAmplitude by remember { mutableFloatStateOf(0f) }
  var activeSession by remember { mutableStateOf<ActiveAudioRecordingSession?>(null) }
  var refreshTrigger by remember { mutableIntStateOf(0) }

  fun grantFolderAccess(folder: AudioRecorderFolder) {
    if (rawFolderPills.none { it.displayPath.equals(folder.displayPath, ignoreCase = true) }) {
      val updatedPills = if (folder.isCustom) {
        listOf(folder) + rawFolderPills
      } else {
        rawFolderPills + folder
      }
      rawFolderPills = updatedPills
      if (!isInspection) {
        AudioRecorderEngine.setFolderPills(context, updatedPills, hasFullFilesPermission = true)
      }
    }
    grantedFolderPaths = if (isInspection) {
      grantedFolderPaths + folder.displayPath
    } else {
      grantedFolderPaths + AudioRecorderEngine.setFolderPathAccessGranted(context, folder.displayPath, true)
    }
    pendingPermissionFolderPath = null
    selectRecordingsFolder(folder)
    refreshTrigger++
  }

  fun moveFolderPill(fromIndex: Int, toIndex: Int) {
    val updatedVisible = AudioRecorderEngine.reorderFolders(folderPills, fromIndex, toIndex)
    if (updatedVisible != folderPills) {
      val hiddenPills = rawFolderPills.filter { raw ->
        updatedVisible.none { it.displayPath.equals(raw.displayPath, ignoreCase = true) }
      }
      val updatedRaw = updatedVisible + hiddenPills
      rawFolderPills = updatedRaw
      if (!isInspection) {
        AudioRecorderEngine.setFolderPills(context, updatedRaw, hasFullFilesPermission = true)
      }
    }
  }

  fun deleteCustomFolder(folder: AudioRecorderFolder) {
    if (folder.isDefault || isRecording) return
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updatedPills = AudioRecorderEngine.removeCustomFolder(rawFolderPills, folder)
    rawFolderPills = updatedPills
    if (!isInspection) {
      AudioRecorderEngine.removeCustomFolder(context, folder)
    }
    grantedFolderPaths = grantedFolderPaths.filterNot { it.equals(folder.displayPath, ignoreCase = true) }.toSet()
    if (pendingPermissionFolderPath?.equals(folder.displayPath, ignoreCase = true) == true) {
      pendingPermissionFolderPath = null
    }
    if (selectedFolderPath.equals(folder.displayPath, ignoreCase = true) ||
      selectedFolder.folderName.equals(folder.folderName, ignoreCase = true)
    ) {
      val visibleUpdated = AudioRecorderEngine.filterFolderPillsForPermission(updatedPills, hasFullFilesPermission)
      val fallbackFolder = visibleUpdated.firstOrNull { isFolderGranted(it) }
        ?: visibleUpdated.firstOrNull()
        ?: AudioRecorderDefaultFolder.DEFAULT.toAudioRecorderFolder()
      selectRecordingsFolder(fallbackFolder)
    }
    refreshTrigger++
  }

  val folderAccessLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    val targetPath = pendingSystemPromptFolderPath ?: pendingPermissionFolderPath ?: selectedFolder.displayPath
    val targetFolder = folderPills.find { it.displayPath.equals(targetPath, ignoreCase = true) } ?: selectedFolder
    if (result.resultCode == Activity.RESULT_OK) {
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
      }
      val resolvedFromUri = treeUri?.let { AudioRecorderEngine.resolveFolderFromTreeUri(it)?.toAudioRecorderFolder() }
      grantFolderAccess(targetFolder)
      if (resolvedFromUri != null && !resolvedFromUri.displayPath.equals(targetFolder.displayPath, ignoreCase = true)) {
        grantFolderAccess(resolvedFromUri)
      }
    }
    pendingSystemPromptFolderPath = null
  }

  val addCustomFolderLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
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
        val resolvedFolder = AudioRecorderEngine.resolveCustomFolderFromTreeUri(treeUri, userRoot)
        if (resolvedFolder != null) {
          grantFolderAccess(resolvedFolder)
        }
      }
    }
  }

  fun requestAddCustomFolder() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val customAddHandler = onRequestAddCustomFolder
    if (customAddHandler != null) {
      customAddHandler { addedFolder ->
        if (addedFolder != null) {
          grantFolderAccess(addedFolder)
        }
      }
    } else {
      try {
        val intent = AudioRecorderEngine.createAddCustomFolderIntent(
          context = context,
          userRoot = userRoot,
        )
        addCustomFolderLauncher.launch(intent)
      } catch (_: Throwable) {}
    }
  }

  fun requestSystemFolderAccess(folder: AudioRecorderFolder) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    pendingSystemPromptFolderPath = folder.displayPath
    val customPathHandler = onRequestFolderPathSystemAccess
    val customDefaultHandler = onRequestFolderSystemAccess
    if (customPathHandler != null) {
      customPathHandler(folder) { granted ->
        if (granted) {
          grantFolderAccess(folder)
        }
        pendingSystemPromptFolderPath = null
      }
    } else if (customDefaultHandler != null && folder.defaultFolder != null) {
      customDefaultHandler(folder.defaultFolder!!) { granted ->
        if (granted) {
          grantFolderAccess(folder)
        }
        pendingSystemPromptFolderPath = null
      }
    } else {
      try {
        val intent = AudioRecorderEngine.createFolderAccessIntent(
          context = context,
          folder = folder,
          userRoot = userRoot,
        )
        folderAccessLauncher.launch(intent)
      } catch (_: Throwable) {
        grantFolderAccess(folder)
        pendingSystemPromptFolderPath = null
      }
    }
  }

  fun handleFolderPillTap(folder: AudioRecorderFolder) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    if (isFolderGranted(folder)) {
      pendingPermissionFolderPath = null
      selectRecordingsFolder(folder)
      refreshTrigger++
    } else {
      selectRecordingsFolder(folder)
      pendingPermissionFolderPath = folder.displayPath
    }
  }

  // Microphone permission check
  var hasMicPermission by remember {
    mutableStateOf(
      isInspection ||
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED,
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    hasMicPermission = granted
  }

  // Clean up recording session if screen is disposed while recording
  DisposableEffect(Unit) {
    onDispose {
      activeSession?.stop()
      activeSession = null
    }
  }

  // Timer & amplitude polling while recording
  LaunchedEffect(isRecording, activeSession) {
    if (!isRecording) {
      elapsedRecordingMs = 0L
      liveAmplitude = 0f
      return@LaunchedEffect
    }
    val startMs = System.currentTimeMillis()
    while (isActive && isRecording) {
      elapsedRecordingMs = (System.currentTimeMillis() - startMs).coerceAtLeast(0L)
      liveAmplitude = activeSession?.pollAmplitude() ?: 0.15f
      delay(80L)
    }
  }

  // List of recordings in current directory (scoped strictly to folders granted access)
  val baseRecordings = remember(recordingsDirectory, refreshTrigger, isInspection, isSelectedFolderGranted) {
    if (!isSelectedFolderGranted) {
      emptyList()
    } else if (isInspection) {
      listOf(
        AudioRecordingItem(
          file = File("/storage/emulated/0/Audio/Recording_20261110_123000.m4a"),
          name = "Recording_20261110_123000.m4a",
          path = "/storage/emulated/0/Audio/Recording_20261110_123000.m4a",
          sizeBytes = 248_320L,
          lastModifiedMs = 1794335400000L,
          extension = "m4a",
          durationMs = 15_000L,
        ),
        AudioRecordingItem(
          file = File("/storage/emulated/0/Audio/Recording_20261110_111542.wav"),
          name = "Recording_20261110_111542.wav",
          path = "/storage/emulated/0/Audio/Recording_20261110_111542.wav",
          sizeBytes = 1_411_200L,
          lastModifiedMs = 1794330942000L,
          extension = "wav",
          durationMs = 32_000L,
        ),
      )
    } else {
      AudioRecorderEngine.listRecordings(
        directory = recordingsDirectory,
        includeDurationMetadata = true,
      )
    }
  }

  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }

  BackHandler(enabled = isSearchActive) {
    isSearchActive = false
    searchQuery = ""
  }

  val filteredRecordings = remember(baseRecordings, isSearchActive, searchQuery) {
    if (isSearchActive && searchQuery.isNotBlank()) {
      AudioRecorderEngine.filterRecordings(baseRecordings, searchQuery)
    } else {
      baseRecordings
    }
  }

  val recordings = remember(filteredRecordings, sortOrder) {
    AudioRecorderEngine.sortRecordings(filteredRecordings, sortOrder)
  }

  val groupedRecordings = remember(recordings, groupOption, sortOrder) {
    AudioRecorderEngine.groupRecordings(
      items = recordings,
      groupOption = groupOption,
      sortOrder = sortOrder,
    )
  }

  val recordingsListState = rememberLazyListState()
  var pendingScrollToRecordingPath by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(pendingScrollToRecordingPath, groupedRecordings, collapsedGroupTitles) {
    val targetPath = pendingScrollToRecordingPath ?: return@LaunchedEffect
    val groupContainingItem = groupedRecordings.firstOrNull { group ->
      group.title.isNotEmpty() && group.items.any { it.path == targetPath }
    }
    if (groupContainingItem != null && groupContainingItem.title in collapsedGroupTitles) {
      collapsedGroupTitles = collapsedGroupTitles - groupContainingItem.title
      return@LaunchedEffect
    }
    val targetIndex = AudioRecorderEngine.findRecordingListIndex(
      groups = groupedRecordings,
      targetPath = targetPath,
      collapsedGroupTitles = collapsedGroupTitles,
    )
    if (targetIndex >= 0) {
      try {
        recordingsListState.animateScrollToItem(targetIndex)
      } catch (_: Throwable) {
        recordingsListState.scrollToItem(targetIndex)
      }
      pendingScrollToRecordingPath = null
    }
  }

  // UI Dialog states
  var showFormatMenu by remember { mutableStateOf(false) }
  var activePlaybackRecording by remember { mutableStateOf<AudioRecordingItem?>(null) }
  var recordingToRename by remember { mutableStateOf<AudioRecordingItem?>(null) }
  var recordingToDelete by remember { mutableStateOf<AudioRecordingItem?>(null) }
  var fileForAudioEditor by remember { mutableStateOf<File?>(null) }
  val crossToolHostState = rememberCrossToolFileHostState()

  fun startRecording(source: AudioCaptureSource = AudioCaptureSource.MICROPHONE) {
    if (!isRecorderEnabled) return
    pendingPermissionFolderPath = null
    val targetDirectory = resolveDirectoryForFolder(selectedFolder)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    activeSession?.stop()
    activeSession = null
    activeCaptureSource = source
    val outputFile = AudioRecorderEngine.createTimestampedOutputFile(
      directory = targetDirectory,
      format = selectedFormat,
      source = source,
    )
    val session = ActiveAudioRecordingSession(
      context = context,
      outputFile = outputFile,
      format = selectedFormat,
      source = source,
    )
    session.start()
    activeSession = session
    elapsedRecordingMs = 0L
    isRecording = true
  }

  LaunchedEffect(AudioRecorderEngine.pendingQuickRecordRequest, isRecorderEnabled) {
    val pending = AudioRecorderEngine.pendingQuickRecordRequest ?: return@LaunchedEffect
    if (pending.startRecording && !isRecording && isRecorderEnabled) {
      AudioRecorderEngine.consumePendingQuickRecordRequest()
      startRecording(pending.source)
    } else if (!pending.startRecording) {
      AudioRecorderEngine.consumePendingQuickRecordRequest()
    }
  }

  fun stopRecording() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val session = activeSession
    activeSession = null
    isRecording = false
    val savedFile = session?.stop()
    if (savedFile != null) {
      if (isSearchActive && searchQuery.isNotBlank() && !savedFile.name.contains(searchQuery.trim(), ignoreCase = true)) {
        isSearchActive = false
        searchQuery = ""
      }
      pendingScrollToRecordingPath = savedFile.absolutePath
    }
    refreshTrigger++
    if (savedFile != null && !isInspection) {
      Toast.makeText(context, "Saved ${savedFile.name}", Toast.LENGTH_SHORT).show()
    }
  }

  val activeAudioEditorFile = fileForAudioEditor
  if (activeAudioEditorFile != null) {
    AudioEditorScreen(
      modifier = modifier,
      initialFile = activeAudioEditorFile,
      initialDirectory = recordingsDirectory,
      onNavigateBack = {
        fileForAudioEditor = null
        refreshTrigger++
      },
    )
    return
  }

  if (crossToolHostState.RenderSubScreen(
      modifier = modifier,
      onRefreshNeeded = { refreshTrigger++ },
    )
  ) {
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .testTag("audio_recorder_screen"),
  ) {
    // 1. Standard Tool Header / Top Bar (DESIGN_LANGUAGE.md 5.1 & 5.7)
    ToolboxTopBar(
      title = "Audio Recorder",
      subtext = if (isRecording) {
        val srcLabel = when (activeCaptureSource) {
          AudioCaptureSource.DEVICE_AUDIO -> "Recording device audio"
          AudioCaptureSource.DEVICE_AND_MIC -> "Recording device & mic audio"
          AudioCaptureSource.MICROPHONE -> "Recording"
        }
        "$srcLabel (${selectedFormat.shortLabel}) • $directoryDisplayPath"
      } else {
        "${recordings.size} recordings • ${selectedFormat.shortLabel} • $directoryDisplayPath"
      },
      icon = when {
        isRecording && activeCaptureSource == AudioCaptureSource.DEVICE_AUDIO -> Icons.Default.PhoneAndroid
        isRecording && activeCaptureSource == AudioCaptureSource.DEVICE_AND_MIC -> DeviceAndMicIcon
        else -> Icons.Default.Mic
      },
      badgeContainerColor = if (isRecording) {
        MaterialTheme.colorScheme.errorContainer
      } else {
        MaterialTheme.colorScheme.primaryContainer
      },
      badgeIconTint = if (isRecording) {
        MaterialTheme.colorScheme.error
      } else {
        MaterialTheme.colorScheme.primary
      },
      subtextColor = if (isRecording) {
        MaterialTheme.colorScheme.error
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant
      },
      searchState = ToolboxTopBarSearchState(
        isActive = isSearchActive,
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        onOpenSearch = { isSearchActive = true },
        onCloseSearch = {
          isSearchActive = false
          searchQuery = ""
        },
        placeholder = "Search recordings in $directoryDisplayPath...",
        searchContentDescription = "Search recordings",
        closeContentDescription = "Close search",
        clearContentDescription = "Clear search",
        toggleButtonTestTag = "action_toggle_search",
        searchBarTestTag = "toolbar_search_bar",
        closeButtonTestTag = "action_close_search",
        inputTestTag = "audio_recorder_search_input",
        clearButtonTestTag = "action_clear_search",
      ),
    )

    // 2. Folder Pill Selectors (Audio, Recordings, Download + Custom Folders; reorderable on press & hold)
    //    + Floating Add Folder & Filter Buttons (DESIGN_LANGUAGE.md 5.4)
    AudioRecorderFolderPillBar(
      folders = folderPills,
      selectedFolder = selectedFolder,
      grantedFolderPaths = effectiveGrantedFolderPaths,
      pendingPermissionFolder = pendingPermissionFolder,
      enabled = !isRecording,
      groupOption = groupOption,
      sortOrder = sortOrder,
      showFilterMenu = showFilterMenu,
      isSortGroupSavedForFolder = isSortGroupSavedForFolder,
      onSelectFolder = { folder ->
        handleFolderPillTap(folder)
      },
      onTapFolderIcon = { folder ->
        handleFolderPillTap(folder)
      },
      onMoveFolder = { fromIndex, toIndex ->
        moveFolderPill(fromIndex, toIndex)
      },
      onDeleteCustomFolder = { folder ->
        deleteCustomFolder(folder)
      },
      onAddCustomFolder = {
        if (!isRecording) {
          requestAddCustomFolder()
        }
      },
      onOpenFilterMenu = { showFilterMenu = true },
      onDismissFilterMenu = { showFilterMenu = false },
      onSelectGroupOption = {
        updateGroupOption(it)
        showFilterMenu = false
      },
      onSelectSortOrder = {
        updateSortOrder(it)
        showFilterMenu = false
      },
      onToggleSaveSortGroupForFolder = {
        toggleSaveSortGroupForFolder()
      },
    )

    // Optional Microphone Permission Banner (shown once folder storage access is granted)
    if (isRecorderEnabled && !hasMicPermission) {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("mic_permission_banner"),
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
              imageVector = Icons.Default.Mic,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
              text = "Grant microphone access for hardware audio capture.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
          Button(
            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            shape = CircleShape,
            modifier = Modifier
              .padding(start = 8.dp)
              .testTag("allow_mic_permission_button"),
          ) {
            Text("Allow")
          }
        }
      }
    }

    // 3. Top Section: Recording List View or Scoped Folder Permission Request View (positioned above the recording buttons)
    val folderAwaitingPermission = pendingPermissionFolder?.takeIf { !isFolderGranted(it) }
    val activeRecorderFolderPill = remember(folderPills, selectedFolder, pendingPermissionFolder) {
      val target = pendingPermissionFolder ?: selectedFolder
      folderPills.firstOrNull { it.displayPath.equals(target.displayPath, ignoreCase = true) } ?: selectedFolder
    }
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(0.54f)
        .swipeToSwitchPillView(
          items = folderPills,
          selectedItem = activeRecorderFolderPill,
          enabled = !isRecording && folderPills.size > 1,
          onSelectItem = { folder ->
            handleFolderPillTap(folder)
          },
        )
        .padding(horizontal = 16.dp, vertical = 10.dp)
        .testTag("recordings_section"),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (folderAwaitingPermission != null) {
        FolderPermissionRequestView(
          folder = folderAwaitingPermission,
          onAllowAccess = {
            requestSystemFolderAccess(folderAwaitingPermission)
          },
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        )
      } else if (!isSelectedFolderGranted) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("locked_storage_state"),
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
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Storage access is locked",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap the lock icon on a folder pill above to grant permission to access files in that location.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      } else if (recordings.isEmpty()) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("empty_recordings_state"),
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
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = if (isSearchActive && searchQuery.isNotBlank()) {
                "No recordings matching \"${searchQuery.trim()}\" in $directoryDisplayPath"
              } else {
                "No recordings in $directoryDisplayPath"
              },
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isSearchActive && searchQuery.isNotBlank()) {
                "Try a different search query or switch folders above."
              } else {
                "Tap the microphone button below to record and save a timestamped audio file."
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      } else {
        LazyColumn(
          state = recordingsListState,
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("recordings_list"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = 12.dp),
        ) {
          groupedRecordings.forEach { group ->
            val isCollapsed = group.title.isNotEmpty() && group.title in collapsedGroupTitles
            if (group.title.isNotEmpty()) {
              item(key = "header_${group.title}") {
                AudioRecordingGroupHeader(
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
                RecordingListItemCard(
                  item = item,
                  onClick = {
                    activePlaybackRecording = item
                  },
                  onPlayInMusic = {
                    activePlaybackRecording = null
                    crossToolHostState.fileForMusicPlayer = item.file
                  },
                  onEdit = {
                    activePlaybackRecording = null
                    fileForAudioEditor = item.file
                    onOpenAudioEditor?.invoke(item.file)
                  },
                  onConvert = {
                    crossToolHostState.fileForConverter = item.file
                  },
                  onOpenInFiles = {
                    crossToolHostState.directoryForFiles = item.file.parentFile ?: recordingsDirectory
                  },
                  onOpenWith = {
                    FileManagerEngine.openFile(context, item.file, chooseApp = true)
                  },
                  onShare = {
                    CrossToolActions.shareFile(context, item.file)
                  },
                  onRename = {
                    recordingToRename = item
                  },
                  onInfo = {
                    crossToolHostState.fileForDetails = item.file
                  },
                  onDelete = {
                    recordingToDelete = item
                  },
                )
              }
            }
          }
        }
      }
    }

    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    // 4. Bottom Section: Recording Buttons (positioned below the recording list view)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(0.46f)
        .padding(horizontal = 16.dp, vertical = 2.dp)
        .testTag("recorder_center_stage"),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        // Status Pill & Elapsed Timer above the floating FABs
        val isRecordingMic = isRecording && activeCaptureSource == AudioCaptureSource.MICROPHONE
        val isRecordingDevice = isRecording && activeCaptureSource == AudioCaptureSource.DEVICE_AUDIO
        val isRecordingBoth = isRecording && activeCaptureSource == AudioCaptureSource.DEVICE_AND_MIC

        Badge(
          containerColor = if (isRecording) {
            MaterialTheme.colorScheme.errorContainer
          } else {
            MaterialTheme.colorScheme.surfaceVariant
          },
          contentColor = when {
            !isRecorderEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            isRecording -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
          },
          modifier = Modifier.testTag("recording_status_badge"),
        ) {
          Text(
            text = when {
              !isRecorderEnabled -> "RECORDER DISABLED"
              isRecordingBoth -> "● RECORDING DEVICE & MIC AUDIO"
              isRecordingDevice -> "● RECORDING DEVICE AUDIO"
              isRecording -> "● RECORDING"
              else -> "READY TO RECORD"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = AudioRecorderEngine.formatRecordingTimerMs(elapsedRecordingMs),
          style = MaterialTheme.typography.headlineMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = when {
            !isRecorderEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isRecording -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
          },
          modifier = Modifier.testTag("recording_timer_text"),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Smaller Combined (Device + Mic) FAB on the left + Primary Microphone FAB in the middle + smaller Device Audio FAB on the right
        val infiniteTransition = rememberInfiniteTransition(label = "RecordingPulse")
        val pulseScale by infiniteTransition.animateFloat(
          initialValue = 1.0f,
          targetValue = 1.18f,
          animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
          ),
          label = "PulseRingScale",
        )

        val disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

        val fabContainerColor by animateColorAsState(
          targetValue = when {
            !isRecorderEnabled -> disabledContainerColor
            isRecording -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.primaryContainer
          },
          label = "FabContainerColor",
        )
        val fabContentColor by animateColorAsState(
          targetValue = when {
            !isRecorderEnabled -> disabledContentColor
            isRecording -> MaterialTheme.colorScheme.onError
            else -> MaterialTheme.colorScheme.onPrimaryContainer
          },
          label = "FabContentColor",
        )

        val bothFabContainerColor by animateColorAsState(
          targetValue = when {
            !isRecorderEnabled -> disabledContainerColor
            isRecordingBoth -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.secondaryContainer
          },
          label = "BothFabContainerColor",
        )
        val bothFabContentColor by animateColorAsState(
          targetValue = when {
            !isRecorderEnabled -> disabledContentColor
            isRecordingBoth -> MaterialTheme.colorScheme.onError
            else -> MaterialTheme.colorScheme.onSecondaryContainer
          },
          label = "BothFabContentColor",
        )

        val deviceFabContainerColor by animateColorAsState(
          targetValue = when {
            !isRecorderEnabled -> disabledContainerColor
            isRecordingDevice -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.secondaryContainer
          },
          label = "DeviceFabContainerColor",
        )
        val deviceFabContentColor by animateColorAsState(
          targetValue = when {
            !isRecorderEnabled -> disabledContentColor
            isRecordingDevice -> MaterialTheme.colorScheme.onError
            else -> MaterialTheme.colorScheme.onSecondaryContainer
          },
          label = "DeviceFabContentColor",
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          // Smaller Device + Microphone Audio FAB to the left of the primary Microphone FAB
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(68.dp),
          ) {
            if (isRecordingBoth) {
              val dynamicBoost = (liveAmplitude * 0.15f).coerceIn(0f, 0.15f)
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .scale(pulseScale + dynamicBoost)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
              )
            }

            SmallFloatingActionButton(
              onClick = {
                if (!isRecorderEnabled) return@SmallFloatingActionButton
                if (isRecordingBoth) {
                  stopRecording()
                } else {
                  startRecording(AudioCaptureSource.DEVICE_AND_MIC)
                }
              },
              shape = CircleShape,
              containerColor = bothFabContainerColor,
              contentColor = bothFabContentColor,
              elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = if (isRecorderEnabled) 4.dp else 0.dp,
                pressedElevation = if (isRecorderEnabled) 8.dp else 0.dp,
                focusedElevation = if (isRecorderEnabled) 4.dp else 0.dp,
                hoveredElevation = if (isRecorderEnabled) 4.dp else 0.dp,
              ),
              modifier = Modifier
                .size(56.dp)
                .testTag("both_audio_recorder_fab")
                .semantics {
                  if (!isRecorderEnabled) disabled()
                  contentDescription = if (isRecordingBoth) {
                    "Stop device and microphone audio recording"
                  } else {
                    "Record device and microphone audio"
                  }
                },
            ) {
              AnimatedContent(
                targetState = isRecordingBoth,
                transitionSpec = {
                  (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn())
                    .togetherWith(scaleOut() + fadeOut())
                },
                label = "BothToStopTransition",
              ) { recordingBoth ->
                if (recordingBoth) {
                  Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier
                      .size(26.dp)
                      .testTag("fab_both_stop_icon"),
                  )
                } else {
                  DeviceAndMicOverlayIcon(
                    size = 26.dp,
                    tint = bothFabContentColor,
                    modifier = Modifier.testTag("fab_both_audio_icon"),
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          // Primary Microphone / Stop FAB
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(96.dp),
          ) {
            // Outer animated pulse ring while recording microphone audio
            if (isRecordingMic) {
              val dynamicBoost = (liveAmplitude * 0.15f).coerceIn(0f, 0.15f)
              Box(
                modifier = Modifier
                  .size(94.dp)
                  .scale(pulseScale + dynamicBoost)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
              )
            }

            FloatingActionButton(
              onClick = {
                if (!isRecorderEnabled) return@FloatingActionButton
                if (isRecording) {
                  stopRecording()
                } else {
                  startRecording(AudioCaptureSource.MICROPHONE)
                }
              },
              shape = CircleShape,
              containerColor = fabContainerColor,
              contentColor = fabContentColor,
              elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = if (isRecorderEnabled) 6.dp else 0.dp,
                pressedElevation = if (isRecorderEnabled) 10.dp else 0.dp,
                focusedElevation = if (isRecorderEnabled) 6.dp else 0.dp,
                hoveredElevation = if (isRecorderEnabled) 6.dp else 0.dp,
              ),
              modifier = Modifier
                .size(88.dp)
                .testTag("audio_recorder_fab")
                .semantics {
                  if (!isRecorderEnabled) disabled()
                  contentDescription = if (isRecording) "Stop recording" else "Start recording"
                },
            ) {
              AnimatedContent(
                targetState = isRecording,
                transitionSpec = {
                  (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn())
                    .togetherWith(scaleOut() + fadeOut())
                },
                label = "MicToStopTransition",
              ) { recording ->
                if (recording) {
                  Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier
                      .size(40.dp)
                      .testTag("fab_stop_icon"),
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier
                      .size(40.dp)
                      .testTag("fab_mic_icon"),
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          // Smaller Device Audio FAB to the right with a phone screen icon
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(68.dp),
          ) {
            if (isRecordingDevice) {
              val dynamicBoost = (liveAmplitude * 0.15f).coerceIn(0f, 0.15f)
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .scale(pulseScale + dynamicBoost)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
              )
            }

            SmallFloatingActionButton(
              onClick = {
                if (!isRecorderEnabled) return@SmallFloatingActionButton
                if (isRecordingDevice) {
                  stopRecording()
                } else {
                  startRecording(AudioCaptureSource.DEVICE_AUDIO)
                }
              },
              shape = CircleShape,
              containerColor = deviceFabContainerColor,
              contentColor = deviceFabContentColor,
              elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = if (isRecorderEnabled) 4.dp else 0.dp,
                pressedElevation = if (isRecorderEnabled) 8.dp else 0.dp,
                focusedElevation = if (isRecorderEnabled) 4.dp else 0.dp,
                hoveredElevation = if (isRecorderEnabled) 4.dp else 0.dp,
              ),
              modifier = Modifier
                .size(56.dp)
                .testTag("device_audio_recorder_fab")
                .semantics {
                  if (!isRecorderEnabled) disabled()
                  contentDescription = if (isRecordingDevice) {
                    "Stop device audio recording"
                  } else {
                    "Record device audio"
                  }
                },
            ) {
              AnimatedContent(
                targetState = isRecordingDevice,
                transitionSpec = {
                  (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn())
                    .togetherWith(scaleOut() + fadeOut())
                },
                label = "DeviceToStopTransition",
              ) { recordingDevice ->
                if (recordingDevice) {
                  Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier
                      .size(26.dp)
                      .testTag("fab_device_stop_icon"),
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier
                      .size(26.dp)
                      .testTag("fab_device_audio_icon"),
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Saved Audio Format Selector positioned above the instruction text
        val formatChipContentColor = if (isRecorderEnabled) {
          MaterialTheme.colorScheme.onSecondaryContainer
        } else {
          disabledContentColor
        }
        val formatPillShape = if (currentHexagonIconButtons()) {
          HorizontalHexagonShape
        } else {
          RoundedCornerShape(20.dp)
        }
        Box {
          Surface(
            shape = formatPillShape,
            color = if (isRecorderEnabled) {
              MaterialTheme.colorScheme.secondaryContainer
            } else {
              disabledContainerColor
            },
            modifier = Modifier
              .clip(formatPillShape)
              .clickable(enabled = !isRecording && isRecorderEnabled) { showFormatMenu = true }
              .testTag("format_selector_button"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = formatChipContentColor,
                modifier = Modifier.size(15.dp),
              )
              Text(
                text = selectedFormat.shortLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = formatChipContentColor,
              )
              Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Select audio format",
                tint = formatChipContentColor,
                modifier = Modifier.size(16.dp),
              )
            }
          }

          DropdownMenu(
            expanded = showFormatMenu,
            onDismissRequest = { showFormatMenu = false },
            modifier = Modifier.testTag("format_dropdown_menu"),
          ) {
            AudioRecordingFormat.entries.forEach { format ->
              val isSelected = format == selectedFormat
              DropdownMenuItem(
                text = {
                  Column {
                    Text(
                      text = format.displayName,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                    Text(
                      text = format.description,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                },
                onClick = {
                  updateSelectedFormat(format)
                  showFormatMenu = false
                },
                trailingIcon = if (isSelected) {
                  {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                    )
                  }
                } else null,
                modifier = Modifier.testTag("format_option_${format.name}"),
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = when {
            !isRecorderEnabled -> "Recorder disabled • Grant access to at least one folder above to record"
            isRecordingBoth -> "Recording device & mic audio • Tap Stop to save .${selectedFormat.extension} recording"
            isRecordingDevice -> "Recording device audio • Tap Stop to save .${selectedFormat.extension} recording"
            isRecording -> "Tap Stop to save .${selectedFormat.extension} recording"
            else -> "Tap mic, phone, or combined button to record (${selectedFormat.displayName})"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("recorder_instruction_text"),
        )
      }
    }
  }

  crossToolHostState.RenderFileDialogs(
    onRefreshNeeded = { refreshTrigger++ },
  )

  // Miniature Player Modal (pops up and starts playing when a recording is tapped)
  activePlaybackRecording?.let { recording ->
    MiniAudioPlayerModal(
      recording = recording,
      onDismiss = { activePlaybackRecording = null },
    )
  }

  // Rename Recording Dialog
  recordingToRename?.let { item ->
    RenameRecordingDialog(
      recording = item,
      onDismiss = { recordingToRename = null },
      onConfirmRename = { newName ->
        val result = FileManagerEngine.renameFile(item.file, newName)
        if (result.isSuccess) {
          refreshTrigger++
          recordingToRename = null
        } else {
          Toast.makeText(
            context,
            result.exceptionOrNull()?.message ?: "Could not rename recording",
            Toast.LENGTH_SHORT,
          ).show()
        }
      },
    )
  }

  // Delete Recording Confirmation Dialog
  recordingToDelete?.let { item ->
    DeleteRecordingDialog(
      recording = item,
      onDismiss = { recordingToDelete = null },
      onConfirmDelete = {
        FileManagerEngine.deleteFileOrDirectory(item.file)
        if (activePlaybackRecording?.path == item.path) {
          activePlaybackRecording = null
        }
        refreshTrigger++
        recordingToDelete = null
      },
    )
  }
}

/**
 * View displayed when the user taps a locked folder navigation pill or its lock icon,
 * asking for permission to access files in that location and launching the Android system
 * directory access prompt when the user taps "Allow".
 */
@Composable
private fun FolderPermissionRequestView(
  folder: AudioRecorderFolder,
  onAllowAccess: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier.testTag("folder_permission_view"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp),
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Allow access to files in ${folder.displayPath}?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("folder_permission_title"),
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Audio Recorder needs permission to access files in ${folder.displayPath} (${folder.folderName}) to view and save recordings in this location.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("folder_permission_message"),
      )
      Spacer(modifier = Modifier.height(14.dp))
      Button(
        onClick = onAllowAccess,
        shape = CircleShape,
        modifier = Modifier.testTag("allow_folder_permission_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Allow")
      }
    }
  }
}

/**
 * Horizontal folder pill selector bar (`Audio`, `Recordings`, `Download` + custom folders,
 * reorderable via long-press drag-and-drop like Music Player) with pinned floating
 * [Add Folder] + [Filter / Sort] buttons on the right edge (DESIGN_LANGUAGE.md Section 5.4).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioRecorderFolderPillBar(
  folders: List<AudioRecorderFolder>,
  selectedFolder: AudioRecorderFolder,
  grantedFolderPaths: Set<String>,
  pendingPermissionFolder: AudioRecorderFolder?,
  enabled: Boolean,
  groupOption: AudioRecorderGroupOption,
  sortOrder: AudioRecorderSortOrder,
  showFilterMenu: Boolean,
  isSortGroupSavedForFolder: Boolean,
  onSelectFolder: (AudioRecorderFolder) -> Unit,
  onTapFolderIcon: (AudioRecorderFolder) -> Unit,
  onMoveFolder: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
  onDeleteCustomFolder: (AudioRecorderFolder) -> Unit = {},
  onAddCustomFolder: () -> Unit = {},
  onOpenFilterMenu: () -> Unit,
  onDismissFilterMenu: () -> Unit,
  onSelectGroupOption: (AudioRecorderGroupOption) -> Unit,
  onSelectSortOrder: (AudioRecorderSortOrder) -> Unit,
  onToggleSaveSortGroupForFolder: () -> Unit,
) {
  val density = LocalDensity.current
  val haptic = LocalHapticFeedback.current
  val scrollState = rememberScrollState()

  val currentFolders by rememberUpdatedState(folders)
  val currentOnMoveFolder by rememberUpdatedState(onMoveFolder)
  val pillBounds = remember { mutableMapOf<String, Rect>() }
  var barCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

  var draggedFolder by remember { mutableStateOf<AudioRecorderFolder?>(null) }
  var dragPointerOffset by remember { mutableStateOf(Offset.Zero) }
  var grabOffsetFromCenter by remember { mutableStateOf(Offset.Zero) }
  var draggedPillSize by remember { mutableStateOf<IntSize?>(null) }

  val maxHitSlopPx = with(density) { 24.dp.toPx() }
  val leftEdgeScrollZonePx = with(density) { 36.dp.toPx() }
  val rightEdgeScrollZonePx = with(density) { 112.dp.toPx() }
  val floatingButtonsExcludeZonePx = with(density) { 92.dp.toPx() }
  val edgeScrollStepPx = with(density) { 10.dp.toPx() }

  fun isGrantedFolder(folder: AudioRecorderFolder): Boolean {
    return grantedFolderPaths.any { it.equals(folder.displayPath, ignoreCase = true) }
  }

  LaunchedEffect(folders) {
    val activePaths = folders.map { it.displayPath }
    pillBounds.keys.retainAll { path ->
      activePaths.any { it.equals(path, ignoreCase = true) }
    }
  }

  ToolboxPillNavigationBar(
    barTestTag = "audio_recorder_pill_bar",
    scrollState = scrollState,
    scrollEnabled = draggedFolder == null,
    floatingButtonsCount = 2,
    backgroundColor = MaterialTheme.colorScheme.background,
    modifier = Modifier
      .onGloballyPositioned { coords ->
        barCoordinates = coords
      }
      .pointerInput(enabled) {
        if (!enabled) return@pointerInput
        detectDragGesturesAfterLongPress(
          onDragStart = { startOffset ->
            val barWidth = (barCoordinates?.size?.width ?: 0).toFloat()
            if (barWidth > 0f && startOffset.x >= barWidth - floatingButtonsExcludeZonePx) {
              return@detectDragGesturesAfterLongPress
            }
            val directHit = currentFolders.firstOrNull { folder ->
              pillBounds[folder.displayPath]?.contains(startOffset) == true
            }
            val hitFolder = directHit ?: currentFolders.minByOrNull { folder ->
              val rect = pillBounds[folder.displayPath] ?: return@minByOrNull Float.MAX_VALUE
              val dx = when {
                startOffset.x < rect.left -> rect.left - startOffset.x
                startOffset.x > rect.right -> startOffset.x - rect.right
                else -> 0f
              }
              val dy = when {
                startOffset.y < rect.top -> rect.top - startOffset.y
                startOffset.y > rect.bottom -> startOffset.y - rect.bottom
                else -> 0f
              }
              dx + dy
            }?.takeIf { folder ->
              val rect = pillBounds[folder.displayPath] ?: return@takeIf false
              val dx = when {
                startOffset.x < rect.left -> rect.left - startOffset.x
                startOffset.x > rect.right -> startOffset.x - rect.right
                else -> 0f
              }
              dx <= maxHitSlopPx
            }

            if (hitFolder != null) {
              val rect = pillBounds[hitFolder.displayPath]
              draggedFolder = hitFolder
              dragPointerOffset = startOffset
              grabOffsetFromCenter = if (rect != null) {
                startOffset - rect.center
              } else {
                Offset.Zero
              }
              draggedPillSize = rect?.let { IntSize(it.width.roundToInt(), it.height.roundToInt()) }
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
          },
          onDrag = { change, dragAmount ->
            val activeFolder = draggedFolder ?: return@detectDragGesturesAfterLongPress
            change.consume()
            dragPointerOffset += dragAmount
            val desiredCenter = dragPointerOffset - grabOffsetFromCenter

            val barWidth = (barCoordinates?.size?.width ?: 0).toFloat()
            if (barWidth > 0f && scrollState.maxValue > 0) {
              if (dragPointerOffset.x < leftEdgeScrollZonePx && scrollState.value > 0) {
                scrollState.dispatchRawDelta(-edgeScrollStepPx)
              } else if (dragPointerOffset.x > barWidth - rightEdgeScrollZonePx && scrollState.value < scrollState.maxValue) {
                scrollState.dispatchRawDelta(edgeScrollStepPx)
              }
            }

            val fromIndex = currentFolders.indexOfFirst {
              it.displayPath.equals(activeFolder.displayPath, ignoreCase = true)
            }
            if (fromIndex >= 0 && currentFolders.size > 1) {
              val targetIndex = currentFolders.indices.minByOrNull { idx ->
                val rect = pillBounds[currentFolders[idx].displayPath]
                if (rect != null) abs(desiredCenter.x - rect.center.x) else Float.MAX_VALUE
              }
              if (targetIndex != null && targetIndex != fromIndex) {
                currentOnMoveFolder(fromIndex, targetIndex)
              }
            }
          },
          onDragEnd = {
            draggedFolder = null
            dragPointerOffset = Offset.Zero
            grabOffsetFromCenter = Offset.Zero
            draggedPillSize = null
          },
          onDragCancel = {
            draggedFolder = null
            dragPointerOffset = Offset.Zero
            grabOffsetFromCenter = Offset.Zero
            draggedPillSize = null
          },
        )
      },
    pills = {
      folders.forEach { folder ->
        val isGranted = isGrantedFolder(folder)
        val isSelected = if (pendingPermissionFolder != null) {
          pendingPermissionFolder.displayPath.equals(folder.displayPath, ignoreCase = true)
        } else {
          isGranted && selectedFolder.displayPath.equals(folder.displayPath, ignoreCase = true)
        }
        val isBeingDragged = draggedFolder?.displayPath?.equals(folder.displayPath, ignoreCase = true) == true
        ToolboxPillNavigationItem(
          selected = isSelected,
          onClick = {
            if (enabled && draggedFolder == null) {
              onSelectFolder(folder)
            }
          },
          enabled = enabled,
          leadingIconContent = {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .semantics(mergeDescendants = true) {
                  if (enabled && draggedFolder == null) {
                    onClick {
                      onTapFolderIcon(folder)
                      true
                    }
                  }
                }
                .testTag(
                  if (isGranted) {
                    "folder_unlocked_icon_${folder.folderName}"
                  } else {
                    "folder_lock_icon_${folder.folderName}"
                  }
                ),
            ) {
              Icon(
                imageVector = if (isGranted) Icons.Default.Folder else Icons.Default.Lock,
                contentDescription = if (isGranted) {
                  "${folder.folderName} folder unlocked"
                } else {
                  "Unlock ${folder.folderName} folder"
                },
                modifier = Modifier
                  .size(16.dp)
                  .testTag("folder_pill_icon_${folder.folderName}"),
              )
            }
          },
          label = folder.folderName,
          trailingIcon = if (folder.isCustom) {
            {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable(enabled = enabled && draggedFolder == null) {
                    onDeleteCustomFolder(folder)
                  }
                  .testTag("folder_delete_icon_${folder.folderName}"),
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Delete ${folder.folderName} folder",
                  modifier = Modifier
                    .size(16.dp)
                    .testTag("folder_pill_delete_icon_${folder.folderName}"),
                )
              }
            }
          } else {
            null
          },
          testTag = "folder_pill_${folder.folderName}",
          modifier = Modifier
            .onGloballyPositioned { coords ->
              val barCoords = barCoordinates
              if (barCoords != null && barCoords.isAttached && coords.isAttached) {
                pillBounds[folder.displayPath] = barCoords.localBoundingBoxOf(coords, clipBounds = false)
              }
            }
            .graphicsLayer {
              alpha = if (isBeingDragged) 0.3f else 1f
            },
        )
      }
    },
    floatingControls = {
      Surface(
        onClick = onAddCustomFolder,
          enabled = enabled,
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceContainerHigh,
          tonalElevation = 3.dp,
          shadowElevation = 4.dp,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
          modifier = Modifier
            .size(34.dp)
            .testTag("audio_recorder_add_folder_button"),
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.testTag("add_folder_button"),
          ) {
            Icon(
              imageVector = Icons.Default.CreateNewFolder,
              contentDescription = "Add folder",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
        }

        Box {
          Surface(
            onClick = onOpenFilterMenu,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
              .size(34.dp)
              .testTag("audio_recorder_filter_button"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter and sort options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
            }
          }

          DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = onDismissFilterMenu,
            modifier = Modifier
              .widthIn(min = 320.dp, max = 390.dp)
              .testTag("audio_recorder_filter_menu"),
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
                AudioRecorderGroupOption.entries.forEach { option ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = option.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (option == groupOption) FontWeight.Bold else FontWeight.Normal,
                        color = if (option == groupOption) {
                          MaterialTheme.colorScheme.primary
                        } else {
                          MaterialTheme.colorScheme.onSurface
                        },
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
                    modifier = Modifier.testTag("audio_recorder_group_option_${option.name.lowercase()}"),
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
                    .testTag("audio_recorder_save_sort_group_for_folder"),
                ) {
                  Checkbox(
                    checked = isSortGroupSavedForFolder,
                    onCheckedChange = { onToggleSaveSortGroupForFolder() },
                    colors = CheckboxDefaults.colors(
                      checkedColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                      .size(28.dp)
                      .testTag("audio_recorder_save_sort_group_for_folder_checkbox"),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Save for folder",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSortGroupSavedForFolder) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSortGroupSavedForFolder) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.onSurface
                    },
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
                AudioRecorderSortOrder.entries.forEach { option ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = option.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (option == sortOrder) FontWeight.Bold else FontWeight.Normal,
                        color = if (option == sortOrder) {
                          MaterialTheme.colorScheme.primary
                        } else {
                          MaterialTheme.colorScheme.onSurface
                        },
                      )
                    },
                    trailingIcon = if (option == sortOrder) {
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
                    onClick = { onSelectSortOrder(option) },
                    modifier = Modifier.testTag("audio_recorder_sort_option_${option.name.lowercase()}"),
                  )
                }
              }
            }
          }
        }
      },
      overlayContent = {
        val activeDraggedFolder = draggedFolder
        if (activeDraggedFolder != null && barCoordinates != null) {
          val desiredCenter = dragPointerOffset - grabOffsetFromCenter
          val fallbackWidthPx = with(density) { 96.dp.toPx() }
          val fallbackHeightPx = with(density) { 32.dp.toPx() }
          val widthPx = draggedPillSize?.width?.toFloat() ?: fallbackWidthPx
          val heightPx = draggedPillSize?.height?.toFloat() ?: fallbackHeightPx
          val isGranted = isGrantedFolder(activeDraggedFolder)
          val isSelected = if (pendingPermissionFolder != null) {
            pendingPermissionFolder.displayPath.equals(activeDraggedFolder.displayPath, ignoreCase = true)
          } else {
            isGranted && selectedFolder.displayPath.equals(activeDraggedFolder.displayPath, ignoreCase = true)
          }

          Box(
            modifier = Modifier
              .graphicsLayer {
                translationX = desiredCenter.x - widthPx / 2f
                translationY = desiredCenter.y - heightPx / 2f
                scaleX = 1.06f
                scaleY = 1.06f
              }
              .zIndex(100f),
          ) {
            ToolboxPillNavigationItem(
              selected = isSelected,
              onClick = {},
              leadingIcon = if (isGranted) Icons.Default.Folder else Icons.Default.Lock,
              label = activeDraggedFolder.folderName,
              trailingIcon = if (activeDraggedFolder.isCustom) {
                {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                }
              } else {
                null
              },
            )
          }
        }
      },
    )
}

@Composable
private fun AudioRecordingGroupHeader(
  title: String,
  count: Int,
  isCollapsed: Boolean = false,
  onToggleCollapse: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val rotationState by animateFloatAsState(
    targetValue = if (isCollapsed) -90f else 0f,
    label = "audio_group_expand_rotation",
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("recording_group_header_${title.lowercase().replace(" ", "_")}"),
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
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
          )
        }
      }
      Icon(
        imageVector = Icons.Default.ExpandMore,
        contentDescription = if (isCollapsed) "Expand $title" else "Collapse $title",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .size(18.dp)
          .rotate(rotationState),
      )
    }
  }
}

@Composable
private fun RecordingListItemCard(
  item: AudioRecordingItem,
  onClick: () -> Unit,
  onPlayInMusic: () -> Unit = {},
  onEdit: () -> Unit = {},
  onConvert: () -> Unit = {},
  onOpenInFiles: () -> Unit = {},
  onOpenWith: () -> Unit = {},
  onShare: () -> Unit = {},
  onRename: () -> Unit,
  onInfo: () -> Unit = {},
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("recording_item_${item.name}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Circular Leading Icon Badge
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        if (item.captureSource == AudioCaptureSource.DEVICE_AND_MIC) {
          DeviceAndMicOverlayIcon(
            size = 20.dp,
            tint = MaterialTheme.colorScheme.primary,
          )
        } else {
          Icon(
            imageVector = when (item.captureSource) {
              AudioCaptureSource.DEVICE_AUDIO -> Icons.Default.PhoneAndroid
              AudioCaptureSource.DEVICE_AND_MIC -> DeviceAndMicIcon
              AudioCaptureSource.MICROPHONE -> Icons.Default.GraphicEq
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      // Title + Metadata
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.name,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
          ) {
            Text(
              text = item.formatBadge,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            )
          }
          if (item.captureSource == AudioCaptureSource.DEVICE_AUDIO) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
              Text(
                text = "DEVICE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
              )
            }
          } else if (item.captureSource == AudioCaptureSource.DEVICE_AND_MIC) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
              Text(
                text = "DEVICE + MIC",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
              )
            }
          }
          Text(
            text = "${item.durationFormatted} • ${item.sizeFormatted} • ${item.timestampFormatted}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      // Overflow actions menu
      Box {
        IconButton(
          onClick = { menuExpanded = true },
          modifier = Modifier
            .size(36.dp)
            .testTag("recording_menu_${item.name}"),
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More options for ${item.name}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
        ) {
          DropdownMenuItem(
            text = { Text("Music") },
            leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onPlayInMusic()
            },
            modifier = Modifier.testTag("action_play_in_music"),
          )
          DropdownMenuItem(
            text = { Text("Edit") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onEdit()
            },
            modifier = Modifier.testTag("action_edit_audio"),
          )
          DropdownMenuItem(
            text = { Text("Convert") },
            leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onConvert()
            },
            modifier = Modifier.testTag("action_convert_file"),
          )
          HorizontalDivider()
          DropdownMenuItem(
            text = { Text("Folder") },
            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onOpenInFiles()
            },
            modifier = Modifier.testTag("action_open_in_files"),
          )
          DropdownMenuItem(
            text = { Text("Open with…") },
            leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onOpenWith()
            },
            modifier = Modifier.testTag("action_open_with"),
          )
          DropdownMenuItem(
            text = { Text("Share") },
            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onShare()
            },
            modifier = Modifier.testTag("action_share"),
          )
          HorizontalDivider()
          DropdownMenuItem(
            text = { Text("Rename") },
            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onRename()
            },
          )
          DropdownMenuItem(
            text = { Text("Info") },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onInfo()
            },
            modifier = Modifier.testTag("action_info"),
          )
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
            onClick = {
              menuExpanded = false
              onDelete()
            },
          )
        }
      }
    }
  }
}

/**
 * Miniature Player Modal that pops up when a recording is tapped, immediately starts playing
 * the audio file, and provides a simple play/pause toggle to the left of a tracking slider.
 */
@Composable
fun MiniAudioPlayerModal(
  recording: AudioRecordingItem,
  onDismiss: () -> Unit,
) {
  val isInspection = LocalInspectionMode.current

  var isPlaying by remember { mutableStateOf(true) }
  var durationMs by remember {
    mutableLongStateOf(recording.durationMs.coerceAtLeast(1000L))
  }
  var currentPositionMs by remember { mutableLongStateOf(0L) }
  var isDraggingSlider by remember { mutableStateOf(false) }
  var dragFraction by remember { mutableFloatStateOf(0f) }
  var usingFallbackTimer by remember { mutableStateOf(isInspection) }

  val mediaPlayer = remember(recording.path) {
    if (isInspection) {
      null
    } else {
      try {
        MediaPlayer().apply {
          setDataSource(recording.path)
          prepare()
          val mpDuration = duration.toLong()
          if (mpDuration > 0L) {
            durationMs = mpDuration
          }
          setOnCompletionListener {
            isPlaying = false
            currentPositionMs = durationMs
          }
          start()
        }
      } catch (_: Throwable) {
        usingFallbackTimer = true
        null
      }
    }
  }

  DisposableEffect(mediaPlayer) {
    onDispose {
      try {
        if (mediaPlayer?.isPlaying == true) {
          mediaPlayer.stop()
        }
      } catch (_: Throwable) {}
      try {
        mediaPlayer?.release()
      } catch (_: Throwable) {}
    }
  }

  // Synchronize playback progress with tracking slider
  LaunchedEffect(isPlaying, mediaPlayer, usingFallbackTimer, isDraggingSlider) {
    if (!isPlaying) return@LaunchedEffect
    while (isActive && isPlaying) {
      if (!isDraggingSlider) {
        if (mediaPlayer != null && !usingFallbackTimer) {
          try {
            val pos = mediaPlayer.currentPosition.toLong().coerceAtLeast(0L)
            currentPositionMs = pos.coerceAtMost(durationMs)
            if (!mediaPlayer.isPlaying && currentPositionMs >= durationMs - 80L) {
              isPlaying = false
            }
          } catch (_: Throwable) {
            usingFallbackTimer = true
          }
        } else {
          // Simulated timer fallback when running in test / headless environment
          val nextPos = currentPositionMs + 100L
          if (nextPos >= durationMs) {
            currentPositionMs = durationMs
            isPlaying = false
          } else {
            currentPositionMs = nextPos
          }
        }
      }
      delay(100L)
    }
  }

  fun togglePlayPause() {
    if (isPlaying) {
      isPlaying = false
      try {
        mediaPlayer?.pause()
      } catch (_: Throwable) {}
    } else {
      // If at the end, restart from 0
      if (currentPositionMs >= durationMs) {
        currentPositionMs = 0L
        try {
          mediaPlayer?.seekTo(0)
        } catch (_: Throwable) {}
      }
      isPlaying = true
      try {
        mediaPlayer?.start()
      } catch (_: Throwable) {}
    }
  }

  fun seekToFraction(fraction: Float) {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    val targetMs = (clampedFraction * durationMs).toLong()
    currentPositionMs = targetMs
    try {
      mediaPlayer?.seekTo(targetMs.toInt())
    } catch (_: Throwable) {}
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
        .testTag("mini_player_modal"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Modal Header: Icon Badge + File Name + Close Button
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
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = recording.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("mini_player_title"),
              )
              Text(
                text = "${recording.formatBadge} • ${recording.sizeFormatted}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .testTag("mini_player_close_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close player",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        // Play/Pause toggle mini-FAB to the left of the tracking slider
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
              .testTag("mini_player_play_pause_button")
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
                .testTag("mini_player_tracking_slider"),
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
                modifier = Modifier.testTag("mini_player_elapsed_time"),
              )
              Text(
                text = AudioRecorderEngine.formatDurationMs(durationMs),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("mini_player_total_time"),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RenameRecordingDialog(
  recording: AudioRecordingItem,
  onDismiss: () -> Unit,
  onConfirmRename: (String) -> Unit,
) {
  val stem = recording.file.nameWithoutExtension
  val ext = recording.extension
  var textFieldValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = recording.name,
        selection = TextRange(0, stem.length),
      )
    )
  }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Throwable) {}
  }

  val trimmed = textFieldValue.text.trim()
  val isUnchanged = trimmed == recording.name
  val validationError = remember(trimmed, recording) {
    if (isUnchanged) null
    else FileManagerEngine.validateFileName(trimmed, recording.file.parentFile)
  }
  val missingExtension = remember(trimmed, ext) {
    ext.isNotEmpty() && !trimmed.endsWith(".$ext", ignoreCase = true)
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
            text = "Rename Recording",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Enter a new name for this audio file",
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
              imageVector = Icons.Default.AudioFile,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = recording.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "${recording.sizeFormatted} • ${recording.timestampFormatted}",
                style = MaterialTheme.typography.bodySmall,
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
          isError = validationError != null,
          supportingText = if (validationError != null) {
            { Text(validationError) }
          } else null,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = { Icon(Icons.Default.AudioFile, contentDescription = null) },
          trailingIcon = if (textFieldValue.text.isNotEmpty()) {
            {
              IconButton(onClick = { textFieldValue = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          } else null,
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        )

        if (missingExtension && trimmed.isNotEmpty()) {
          val extPillShape = if (currentHexagonIconButtons()) {
            HorizontalHexagonShape
          } else {
            RoundedCornerShape(20.dp)
          }
          Surface(
            shape = extPillShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
              .clip(extPillShape)
              .clickable {
                val restored = "$trimmed.$ext"
                textFieldValue = TextFieldValue(restored, TextRange(restored.length))
              },
          ) {
            Text(
              text = "Restore .$ext extension",
              style = MaterialTheme.typography.labelMedium,
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
        enabled = !isUnchanged && trimmed.isNotEmpty() && validationError == null,
        shape = CircleShape,
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Confirm")
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
private fun DeleteRecordingDialog(
  recording: AudioRecordingItem,
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
            text = "Delete Recording",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "This action cannot be undone",
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
            imageVector = Icons.Default.AudioFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = recording.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "${recording.sizeFormatted} • ${recording.timestampFormatted}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirmDelete,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        shape = CircleShape,
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

/**
 * Combined Device + Microphone icon with the microphone icon (`Icons.Default.Mic`)
 * overlaid on the inside of the device icon (`Icons.Default.PhoneAndroid`).
 */
@Composable
internal fun DeviceAndMicOverlayIcon(
  modifier: Modifier = Modifier,
  size: Dp = 26.dp,
  tint: Color = LocalContentColor.current,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier.size(size),
  ) {
    Icon(
      imageVector = Icons.Default.PhoneAndroid,
      contentDescription = null,
      tint = tint,
      modifier = Modifier
        .size(size)
        .testTag("fab_both_device_icon"),
    )
    Icon(
      imageVector = Icons.Default.Mic,
      contentDescription = null,
      tint = tint,
      modifier = Modifier
        .size(size * 0.46f)
        .offset(y = -(size * 0.042f))
        .testTag("fab_both_mic_icon"),
    )
  }
}

internal val DeviceAndMicIcon: ImageVector by lazy {
  val phone = Icons.Default.PhoneAndroid
  val mic = Icons.Default.Mic
  ImageVector.Builder(
    name = "DeviceAndMic",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    addVectorNodes(phone.root)
    addGroup(
      name = "mic_overlay",
      pivotX = 12f,
      pivotY = 12f,
      scaleX = 0.46f,
      scaleY = 0.46f,
      translationY = -1f,
    )
    addVectorNodes(mic.root)
    clearGroup()
  }.build()
}

private fun ImageVector.Builder.addVectorNodes(group: VectorGroup) {
  for (node in group) {
    when (node) {
      is VectorPath -> addPath(
        pathData = node.pathData,
        pathFillType = node.pathFillType,
        name = node.name,
        fill = node.fill,
        fillAlpha = node.fillAlpha,
        stroke = node.stroke,
        strokeAlpha = node.strokeAlpha,
        strokeLineWidth = node.strokeLineWidth,
        strokeLineCap = node.strokeLineCap,
        strokeLineJoin = node.strokeLineJoin,
        strokeLineMiter = node.strokeLineMiter,
        trimPathStart = node.trimPathStart,
        trimPathEnd = node.trimPathEnd,
        trimPathOffset = node.trimPathOffset,
      )
      is VectorGroup -> {
        addGroup(
          name = node.name,
          rotate = node.rotation,
          pivotX = node.pivotX,
          pivotY = node.pivotY,
          scaleX = node.scaleX,
          scaleY = node.scaleY,
          translationX = node.translationX,
          translationY = node.translationY,
          clipPathData = node.clipPathData,
        )
        addVectorNodes(node)
        clearGroup()
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun AudioRecorderScreenPreview() {
  ToolboxTheme {
    AudioRecorderScreen()
  }
}
