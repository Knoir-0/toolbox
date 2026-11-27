package com.example.toolbox.ui.tools

import com.example.toolbox.ui.theme.toolboxScreenBackground

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import com.example.toolbox.ui.theme.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NewLabel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import com.example.toolbox.ui.theme.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.toolbox.ui.theme.FilledIconButton
import com.example.toolbox.ui.theme.FilterChip
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.toolbox.ui.theme.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Resolves the adaptive background [Color] for a [NoteColor] card or editor surface by blending
 * semantic HSV hues with `MaterialTheme.colorScheme` surface tokens so both light and dark modes
 * maintain high text contrast.
 */
@Composable
fun resolveNoteCardColor(
  noteColor: NoteColor,
  isElevatedSurface: Boolean = false,
): Color {
  val scheme = MaterialTheme.colorScheme
  val baseSurface = if (isElevatedSurface) {
    scheme.surfaceContainerHigh
  } else {
    scheme.surfaceVariant.copy(alpha = 0.45f)
  }
  if (noteColor == NoteColor.DEFAULT) {
    return baseSurface
  }

  val isDark = scheme.surface.luminance() < 0.5f
  val hue = noteColorHue(noteColor)
  val saturation = if (noteColor == NoteColor.CLAY) {
    if (isDark) 0.22f else 0.28f
  } else {
    if (isDark) 0.55f else 0.65f
  }
  val value = if (isDark) 0.52f else 0.96f
  val tint = Color.hsv(hue = hue, saturation = saturation, value = value)
  val solidBase = if (isElevatedSurface) scheme.surfaceContainerHigh else scheme.surfaceVariant
  val blendFraction = if (isDark) 0.36f else 0.42f
  return lerp(solidBase, tint, blendFraction)
}

/**
 * Resolves a slightly more saturated swatch [Color] for color picker circles.
 */
@Composable
fun resolveNoteSwatchColor(noteColor: NoteColor): Color {
  val scheme = MaterialTheme.colorScheme
  if (noteColor == NoteColor.DEFAULT) {
    return scheme.surfaceVariant
  }
  val isDark = scheme.surface.luminance() < 0.5f
  val hue = noteColorHue(noteColor)
  val saturation = if (noteColor == NoteColor.CLAY) 0.32f else 0.72f
  val value = if (isDark) 0.68f else 0.92f
  val tint = Color.hsv(hue = hue, saturation = saturation, value = value)
  return lerp(scheme.surfaceVariant, tint, 0.62f)
}

internal fun noteColorHue(noteColor: NoteColor): Float = when (noteColor) {
  NoteColor.DEFAULT -> 0f
  NoteColor.CORAL -> 8f
  NoteColor.PEACH -> 28f
  NoteColor.SAND -> 48f
  NoteColor.MINT -> 142f
  NoteColor.SAGE -> 168f
  NoteColor.FOG -> 198f
  NoteColor.STORM -> 222f
  NoteColor.DUSK -> 268f
  NoteColor.BLOSSOM -> 325f
  NoteColor.CLAY -> 22f
}

internal class NoteCardActionDispatcher(
  val onEdit: (NoteItem) -> Unit = {},
  val onTogglePin: (NoteItem) -> Unit = {},
  val onChangeColor: (NoteItem) -> Unit = {},
  val onDuplicate: (NoteItem) -> Unit = {},
  val onCopyText: (NoteItem) -> Unit = {},
  val onSendToTodo: (NoteItem) -> Unit = {},
  val onExportAsFile: (NoteItem) -> Unit = {},
  val onShare: (NoteItem) -> Unit = {},
  val onToggleArchive: (NoteItem) -> Unit = {},
  val onDeleteOrTrash: (NoteItem) -> Unit = {},
  val onRestoreFromTrash: (NoteItem) -> Unit = {},
)

internal val LocalNoteCardActions = compositionLocalOf { NoteCardActionDispatcher() }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NotesScreen(
  modifier: Modifier = Modifier,
  repository: NotesRepository? = null,
  initialOpenAddNote: Boolean = false,
  initialAddChecklistMode: Boolean = false,
  initialGrantedFolderPaths: Set<String>? = null,
  onRequestFolderSystemAccess: ((NotesFolder, (Boolean) -> Unit) -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current

  val repo = remember(repository) {
    repository ?: SharedPreferencesNotesRepository(context)
  }

  var grantedFolderPaths by remember(repo, initialGrantedFolderPaths, isInspection) {
    mutableStateOf(
      when {
        initialGrantedFolderPaths != null -> initialGrantedFolderPaths
        isInspection -> setOf(DEFAULT_NOTES_SCOPED_FOLDER.displayPath)
        else -> repo.getGrantedFolderPaths()
      }
    )
  }
  val isNotesFolderGranted = remember(grantedFolderPaths) {
    NotesEngine.isNotesFolderAccessGranted(grantedFolderPaths)
  }
  var pendingPermissionFolderPath by rememberSaveable {
    mutableStateOf<String?>(null)
  }
  var folderAwaitingSystemResult by remember {
    mutableStateOf<NotesFolder?>(null)
  }

  var notes by remember { mutableStateOf(repo.loadNotes()) }
  var savedLabels by remember { mutableStateOf(repo.loadLabels()) }
  var isGridView by remember { mutableStateOf(repo.isGridView()) }
  var gridColumnsPerRow by remember { mutableIntStateOf(repo.getGridColumnsPerRow()) }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner, repo) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        val latestNotes = repo.loadNotes()
        val latestLabels = repo.loadLabels()
        if (latestNotes != notes || latestLabels != savedLabels) {
          notes = latestNotes
          savedLabels = latestLabels
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  fun grantNotesFolderAccess(folder: NotesFolder = DEFAULT_NOTES_SCOPED_FOLDER) {
    val updated = buildSet {
      addAll(grantedFolderPaths)
      add(folder.displayPath)
      add(DEFAULT_NOTES_SCOPED_FOLDER.displayPath)
    }
    grantedFolderPaths = updated
    repo.setGrantedFolderPaths(updated)
    pendingPermissionFolderPath = null
    notes = repo.loadNotes()
    savedLabels = repo.loadLabels()
  }

  val folderAccessLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    val targetFolder = folderAwaitingSystemResult ?: DEFAULT_NOTES_SCOPED_FOLDER
    folderAwaitingSystemResult = null
    if (result.resultCode == Activity.RESULT_OK) {
      val treeUri = result.data?.data
      if (treeUri != null) {
        try {
          val takeFlags = (result.data?.flags ?: 0) and
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
          context.contentResolver.takePersistableUriPermission(
            treeUri,
            if (takeFlags != 0) takeFlags else (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
          )
        } catch (_: Throwable) {}
      }
      grantNotesFolderAccess(targetFolder)
    }
  }

  fun requestSystemFolderAccess(folder: NotesFolder = DEFAULT_NOTES_SCOPED_FOLDER) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    if (onRequestFolderSystemAccess != null) {
      onRequestFolderSystemAccess(folder) { granted ->
        if (granted) {
          grantNotesFolderAccess(folder)
        }
      }
      return
    }
    try {
      folderAwaitingSystemResult = folder
      folderAccessLauncher.launch(
        NotesEngine.createFolderAccessIntent(
          context = context,
          folder = folder,
        )
      )
    } catch (_: Throwable) {
      folderAwaitingSystemResult = null
      grantNotesFolderAccess(folder)
    }
  }

  fun requestNotesFolderPermissionPrompt() {
    pendingPermissionFolderPath = DEFAULT_NOTES_SCOPED_FOLDER.displayPath
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
  }

  fun updateGridColumnsPerRow(columns: Int) {
    val coerced = columns.coerceIn(MIN_NOTES_GRID_COLUMNS, MAX_NOTES_GRID_COLUMNS)
    gridColumnsPerRow = coerced
    repo.setGridColumnsPerRow(coerced)
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
  }

  var undoStack by remember { mutableStateOf(listOf<NotesHistorySnapshot>()) }
  var redoStack by remember { mutableStateOf(listOf<NotesHistorySnapshot>()) }

  val canUndo = undoStack.isNotEmpty()
  val canRedo = redoStack.isNotEmpty()

  var activeFilter by remember { mutableStateOf<NotesFilter>(NotesFilter.All) }
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var selectedColorFilter by remember { mutableStateOf<NoteColor?>(null) }

  // Multi-selection state
  var selectedNoteIds by remember { mutableStateOf(setOf<String>()) }
  val isSelectionMode = selectedNoteIds.isNotEmpty()

  // Slide-up Add Note state (from the bottom "Take a note..." bar)
  var isAddNoteOpen by remember { mutableStateOf(initialOpenAddNote) }
  var addingNoteDraft by remember {
    mutableStateOf(
      NoteItem(
        isChecklist = initialOpenAddNote && initialAddChecklistMode,
        checklistItems = emptyList(),
      ),
    )
  }
  var addingNoteNewLabels by remember { mutableStateOf(listOf<String>()) }

  // Single Note View state (when tapping an existing note on the board)
  var editingNote by remember { mutableStateOf<NoteItem?>(null) }

  LaunchedEffect(initialOpenAddNote, initialAddChecklistMode, NotesEngine.pendingQuickCaptureRequest) {
    val pending = NotesEngine.consumePendingQuickCaptureRequest()
    if (pending != null || initialOpenAddNote) {
      val openChecklist = pending?.isChecklist ?: initialAddChecklistMode
      val initialPinned = pending?.initialPinned ?: false
      val initialLabel = pending?.initialLabel?.trim()?.takeIf { it.isNotEmpty() }
      editingNote = null
      selectedNoteIds = emptySet()
      when {
        initialLabel != null -> activeFilter = NotesFilter.Label(initialLabel)
        initialPinned -> activeFilter = NotesFilter.Pinned
        activeFilter == NotesFilter.Trash -> activeFilter = NotesFilter.All
      }
      addingNoteNewLabels = emptyList()
      addingNoteDraft = NoteItem(
        isChecklist = openChecklist,
        checklistItems = emptyList(),
        isPinned = initialPinned || activeFilter == NotesFilter.Pinned,
        labels = if (initialLabel != null) listOf(initialLabel) else emptyList(),
      )
      isAddNoteOpen = true
    }
  }

  LaunchedEffect(NotesEngine.pendingWidgetNavigationRequest) {
    val navRequest = NotesEngine.consumePendingWidgetNavigationRequest() ?: return@LaunchedEffect
    val latestNotes = repo.loadNotes()
    val latestLabels = repo.loadLabels()
    if (latestNotes != notes) notes = latestNotes
    if (latestLabels != savedLabels) savedLabels = latestLabels

    selectedNoteIds = emptySet()
    isAddNoteOpen = false
    if (navRequest.filter != null) {
      activeFilter = navRequest.filter
    }
    val targetNoteId = navRequest.openNoteId
    if (!targetNoteId.isNullOrBlank()) {
      val targetNote = latestNotes.firstOrNull { it.id == targetNoteId }
        ?: notes.firstOrNull { it.id == targetNoteId }
      if (targetNote != null) {
        editingNote = targetNote
      }
    } else {
      editingNote = null
    }
  }

  // Dialog states
  var showManageLabelsDialog by remember { mutableStateOf(false) }
  var showBulkColorDialog by remember { mutableStateOf(false) }
  var showEmptyTrashDialog by remember { mutableStateOf(false) }
  var labelPendingDelete by remember { mutableStateOf<String?>(null) }

  // Drag-and-drop and long-press selection state in both grid and list views
  val density = LocalDensity.current
  val gridState = rememberLazyStaggeredGridState()
  val verticalSpacingPx = with(density) { 10.dp.toPx() }
  val autoScrollEdgeThresholdPx = with(density) { 56.dp.toPx() }
  val maxAutoScrollStepPx = with(density) { 14.dp.toPx() }

  var gridCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  val cardCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }
  val cardBounds = remember { mutableStateMapOf<String, Rect>() }
  var activeLongPressNoteId by remember { mutableStateOf<String?>(null) }
  var draggedNoteId by remember { mutableStateOf<String?>(null) }
  var dragPointerOffset by remember { mutableStateOf(Offset.Zero) }
  var grabOffsetFromCenter by remember { mutableStateOf(Offset.Zero) }
  var draggedCardSize by remember { mutableStateOf<IntSize?>(null) }
  var hasDragged by remember { mutableStateOf(false) }
  var initialDragSnapshot by remember { mutableStateOf<NotesHistorySnapshot?>(null) }
  var autoScrollSpeedPx by remember { mutableFloatStateOf(0f) }

  fun syncCardBounds() {
    val gridCoords = gridCoordinates ?: return
    if (!gridCoords.isAttached) return
    val iterator = cardCoordinates.entries.iterator()
    while (iterator.hasNext()) {
      val (id, coords) = iterator.next()
      if (coords.isAttached) {
        cardBounds[id] = gridCoords.localBoundingBoxOf(coords, clipBounds = false)
      } else {
        iterator.remove()
        cardBounds.remove(id)
      }
    }
  }

  fun performDraggedNoteReorderAtCurrentPointer() {
    val activeNoteId = draggedNoteId ?: return
    val desiredCenter = dragPointerOffset - grabOffsetFromCenter
    val liveFiltered = NotesEngine.filterNotes(
      notes = notes,
      filter = activeFilter,
      searchQuery = if (isSearchActive) searchQuery else "",
      colorFilter = if (isSearchActive) selectedColorFilter else null,
    )
    val sectionNotes = if (liveFiltered.pinned.any { it.id == activeNoteId }) {
      liveFiltered.pinned
    } else {
      liveFiltered.others
    }

    val targetNoteId = findTargetNoteId(
      draggedNoteId = activeNoteId,
      point = desiredCenter,
      sectionNotes = sectionNotes,
      cardBounds = cardBounds,
      isGridView = isGridView,
    )

    if (targetNoteId != null && targetNoteId != activeNoteId) {
      val firstVisibleIndex = gridState.firstVisibleItemIndex
      val firstVisibleOffset = gridState.firstVisibleItemScrollOffset
      val reordered = NotesEngine.reorderNotes(notes, activeNoteId, targetNoteId)
      if (reordered != notes) {
        val nextFiltered = NotesEngine.filterNotes(
          notes = reordered,
          filter = activeFilter,
          searchQuery = if (isSearchActive) searchQuery else "",
          colorFilter = if (isSearchActive) selectedColorFilter else null,
        )
        val newSectionNotes = if (nextFiltered.pinned.any { it.id == activeNoteId }) {
          nextFiltered.pinned
        } else {
          nextFiltered.others
        }
        val activeColumns = if (isGridView) gridColumnsPerRow else 1
        updateSectionCardBoundsOptimistically(
          oldSectionNotes = sectionNotes,
          newSectionNotes = newSectionNotes,
          cardBounds = cardBounds,
          isGridView = isGridView,
          verticalSpacingPx = verticalSpacingPx,
          columns = activeColumns,
        )
        notes = reordered
        gridState.requestScrollToItem(firstVisibleIndex, firstVisibleOffset)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      }
    }
  }

  LaunchedEffect(draggedNoteId, autoScrollSpeedPx) {
    if (draggedNoteId == null || autoScrollSpeedPx == 0f) return@LaunchedEffect
    while (isActive && draggedNoteId != null && autoScrollSpeedPx != 0f) {
      val consumed = gridState.scrollBy(autoScrollSpeedPx)
      if (consumed != 0f) {
        syncCardBounds()
        performDraggedNoteReorderAtCurrentPointer()
      }
      delay(16L)
    }
  }

  fun applyStateChange(
    updatedNotes: List<NoteItem>,
    updatedLabels: List<String> = savedLabels,
  ) {
    val currentSnapshot = NotesHistorySnapshot(notes, savedLabels)
    val nextSnapshot = NotesHistorySnapshot(updatedNotes, updatedLabels)
    if (currentSnapshot == nextSnapshot) return

    undoStack = listOf(currentSnapshot) + undoStack.take(49)
    redoStack = emptyList()

    notes = updatedNotes
    savedLabels = updatedLabels
    repo.saveNotes(updatedNotes)
    repo.saveLabels(updatedLabels)
    NotesWidgets.updateAllWidgets(context)
  }

  fun undo() {
    if (undoStack.isEmpty()) return
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val previous = undoStack.first()
    undoStack = undoStack.drop(1)
    redoStack = listOf(NotesHistorySnapshot(notes, savedLabels)) + redoStack

    notes = previous.notes
    savedLabels = previous.labels
    repo.saveNotes(previous.notes)
    repo.saveLabels(previous.labels)
    NotesWidgets.updateAllWidgets(context)
  }

  fun redo() {
    if (redoStack.isEmpty()) return
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val next = redoStack.first()
    redoStack = redoStack.drop(1)
    undoStack = listOf(NotesHistorySnapshot(notes, savedLabels)) + undoStack

    notes = next.notes
    savedLabels = next.labels
    repo.saveNotes(next.notes)
    repo.saveLabels(next.labels)
    NotesWidgets.updateAllWidgets(context)
  }

  val allLabels = remember(notes, savedLabels) {
    NotesEngine.extractAllLabels(notes, savedLabels)
  }

  fun deleteLabelGloballyNow(labelToDelete: String) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val (updatedNotes, updatedLabels) = NotesEngine.deleteLabelGlobally(
      notes = notes,
      savedLabels = savedLabels,
      labelToRemove = labelToDelete,
    )
    applyStateChange(updatedNotes, updatedLabels)
    if (addingNoteDraft.labels.any { it.equals(labelToDelete, ignoreCase = true) }) {
      addingNoteDraft = addingNoteDraft.copy(
        labels = addingNoteDraft.labels.filterNot { it.equals(labelToDelete, ignoreCase = true) },
      )
    }
    if (addingNoteNewLabels.any { it.equals(labelToDelete, ignoreCase = true) }) {
      addingNoteNewLabels = addingNoteNewLabels.filterNot { it.equals(labelToDelete, ignoreCase = true) }
    }
    val currentFilter = activeFilter
    if (currentFilter is NotesFilter.Label && currentFilter.name.equals(labelToDelete, ignoreCase = true)) {
      activeFilter = NotesFilter.All
    }
  }

  fun requestDeleteLabel(labelToDelete: String) {
    val notesUsingLabel = NotesEngine.getNotesUsingLabel(notes, labelToDelete)
    if (notesUsingLabel.isNotEmpty()) {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      labelPendingDelete = labelToDelete
    } else {
      deleteLabelGloballyNow(labelToDelete)
    }
  }

  // If the currently selected label filter was deleted, reset to All
  LaunchedEffect(allLabels, activeFilter) {
    val current = activeFilter
    if (current is NotesFilter.Label && allLabels.none { it.equals(current.name, ignoreCase = true) }) {
      activeFilter = NotesFilter.All
    }
  }

  val filteredResult = remember(notes, activeFilter, isSearchActive, searchQuery, selectedColorFilter) {
    NotesEngine.filterNotes(
      notes = notes,
      filter = activeFilter,
      searchQuery = if (isSearchActive) searchQuery else "",
      colorFilter = if (isSearchActive) selectedColorFilter else null,
    )
  }

  val activeNotesCount = remember(notes) { notes.count { !it.isArchived && !it.isTrashed } }
  val pinnedNotesCount = remember(notes) { notes.count { it.isPinned && !it.isArchived && !it.isTrashed } }
  val archivedNotesCount = remember(notes) { notes.count { it.isArchived && !it.isTrashed } }
  val trashedNotesCount = remember(notes) { notes.count { it.isTrashed } }

  val headerSubtext = remember(
    isNotesFolderGranted,
    activeFilter,
    activeNotesCount,
    pinnedNotesCount,
    archivedNotesCount,
    trashedNotesCount,
    filteredResult.totalCount,
  ) {
    if (!isNotesFolderGranted) {
      "Storage access locked • Grant access to /Notes"
    } else {
      when (val filter = activeFilter) {
        NotesFilter.All -> {
          val noteWord = if (activeNotesCount == 1) "note" else "notes"
          if (pinnedNotesCount > 0) {
            "$activeNotesCount $noteWord • $pinnedNotesCount pinned"
          } else {
            "$activeNotesCount $noteWord"
          }
        }
        NotesFilter.Pinned -> "$pinnedNotesCount pinned"
        NotesFilter.Checklists -> "${filteredResult.totalCount} checklists"
        is NotesFilter.Label -> "${filter.name} • ${filteredResult.totalCount}"
        NotesFilter.Archived -> "Archive • $archivedNotesCount"
        NotesFilter.Trash -> "Trash • $trashedNotesCount"
      }
    }
  }

  fun saveAndCloseAddingNote() {
    if (!isAddNoteOpen) return
    val updatedNotes = NotesEngine.saveOrDiscardNote(notes, addingNoteDraft)
    val mergedLabels = (savedLabels + addingNoteNewLabels)
      .map { NotesEngine.normalizeLabelName(it) }
      .filter { it.isNotEmpty() }
      .distinct()
    applyStateChange(updatedNotes, mergedLabels)
    isAddNoteOpen = false
    addingNoteNewLabels = emptyList()
    focusManager.clearFocus()
  }

  fun openSingleNoteView(note: NoteItem) {
    if (isAddNoteOpen) {
      saveAndCloseAddingNote()
    }
    editingNote = note
  }

  BackHandler(enabled = isAddNoteOpen || isSelectionMode) {
    when {
      isAddNoteOpen -> saveAndCloseAddingNote()
      isSelectionMode -> selectedNoteIds = emptySet()
    }
  }

  var rootBottomInsetPx by remember { mutableIntStateOf(0) }
  val imeBottomPx = WindowInsets.ime.getBottom(density)
  val extraImeBottomPadding = with(density) {
    (imeBottomPx - rootBottomInsetPx).coerceAtLeast(0).toDp()
  }

  val noteCardActionDispatcher = remember(notes, savedLabels, context, isAddNoteOpen, addingNoteDraft, addingNoteNewLabels) {
    NoteCardActionDispatcher(
      onEdit = { note -> openSingleNoteView(note) },
      onTogglePin = { note ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        applyStateChange(NotesEngine.togglePin(notes, note.id))
      },
      onChangeColor = { note ->
        selectedNoteIds = setOf(note.id)
        showBulkColorDialog = true
      },
      onDuplicate = { note ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val now = System.currentTimeMillis()
        val copy = note.copy(
          id = java.util.UUID.randomUUID().toString(),
          title = if (note.title.isNotBlank()) "${note.title} (Copy)" else "",
          createdAt = now,
          updatedAt = now,
        )
        applyStateChange(listOf(copy) + notes)
        Toast.makeText(context, "Duplicated note", Toast.LENGTH_SHORT).show()
      },
      onCopyText = { note ->
        copyNoteToClipboard(context, note)
      },
      onSendToTodo = { note ->
        if (note.isChecklist && note.checklistItems.isNotEmpty()) {
          val subtasks = note.checklistItems.map { it.text }
          CrossToolActions.saveTaskToTodoList(
            context = context,
            title = note.title.ifBlank { "Checklist Note" },
            subtasks = subtasks,
          )
        } else {
          CrossToolActions.saveTaskToTodoList(
            context = context,
            title = note.title.ifBlank { note.content.lineSequence().firstOrNull()?.take(60) ?: "Note Task" },
            notes = if (note.title.isNotBlank()) note.content else "",
          )
        }
      },
      onExportAsFile = { note ->
        val baseName = note.title.ifBlank { "Note_${note.id.take(6)}" }
        CrossToolActions.exportTextAsFile(
          context = context,
          suggestedFileName = "$baseName$NOTES_FILE_DOT_EXTENSION",
          content = note.toText(),
          targetDir = repo.getNotesDirectory(),
        )
      },
      onShare = { note ->
        CrossToolActions.shareText(
          context = context,
          title = note.title.ifBlank { "Note" },
          text = note.toPlainText(),
        )
      },
      onToggleArchive = { note ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        applyStateChange(NotesEngine.setArchived(notes, setOf(note.id), !note.isArchived))
      },
      onDeleteOrTrash = { note ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (note.isTrashed) {
          applyStateChange(NotesEngine.deletePermanently(notes, setOf(note.id)))
        } else {
          applyStateChange(NotesEngine.moveToTrash(notes, setOf(note.id)))
        }
      },
      onRestoreFromTrash = { note ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        applyStateChange(NotesEngine.restoreFromTrash(notes, setOf(note.id)))
      },
    )
  }

  CompositionLocalProvider(LocalNoteCardActions provides noteCardActionDispatcher) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .onGloballyPositioned { coords ->
        val rootHeight = coords.findRootCoordinates().size.height
        val bottomInRoot = (coords.positionInRoot().y + coords.size.height).roundToInt()
        rootBottomInsetPx = (rootHeight - bottomInRoot).coerceAtLeast(0)
      }
      .testTag("notes_screen"),
  ) {
    val activeSingleNote = editingNote
    if (activeSingleNote != null) {
      val persistSingleNoteDraft: (NoteItem, List<String>, Boolean) -> Unit =
        { updatedDraft, newlyCreatedLabels, dismissAfterSave ->
          val existing = notes.firstOrNull { it.id == updatedDraft.id }
          val mergedLabels = (savedLabels + newlyCreatedLabels)
            .map { NotesEngine.normalizeLabelName(it) }
            .filter { it.isNotEmpty() }
            .distinct()
          val hasNoteContentChange = existing == null ||
            existing.copy(updatedAt = 0L) != updatedDraft.copy(
              title = updatedDraft.title.trim(),
              content = if (updatedDraft.isChecklist) "" else updatedDraft.content.trimEnd(),
              checklistItems = if (updatedDraft.isChecklist) {
                updatedDraft.checklistItems
                  .map { it.copy(text = it.text.trim()) }
                  .filter { it.text.isNotEmpty() }
              } else {
                emptyList()
              },
              labels = updatedDraft.labels.map { NotesEngine.normalizeLabelName(it) }.filter { it.isNotEmpty() }.distinct(),
              updatedAt = 0L,
            )
          if (hasNoteContentChange || mergedLabels != savedLabels) {
            val updatedNotes = NotesEngine.saveOrDiscardNote(notes, updatedDraft)
            applyStateChange(updatedNotes, mergedLabels)
            if (!dismissAfterSave) {
              editingNote = updatedNotes.firstOrNull { it.id == updatedDraft.id } ?: updatedDraft
            }
          }
          if (dismissAfterSave) {
            editingNote = null
          }
        }
      SingleNoteView(
        initialNote = activeSingleNote,
        availableLabels = allLabels,
        imeBottomPadding = extraImeBottomPadding,
        onAutoSave = { updatedDraft, newlyCreatedLabels ->
          persistSingleNoteDraft(updatedDraft, newlyCreatedLabels, false)
        },
        onSaveAndDismiss = { updatedDraft, newlyCreatedLabels ->
          persistSingleNoteDraft(updatedDraft, newlyCreatedLabels, true)
        },
        onMoveToTrash = { noteId ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          if (notes.any { it.id == noteId }) {
            applyStateChange(NotesEngine.moveToTrash(notes, setOf(noteId)))
          }
          editingNote = null
        },
        onDeletePermanently = { noteId ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          if (notes.any { it.id == noteId }) {
            applyStateChange(NotesEngine.deletePermanently(notes, setOf(noteId)))
          }
          editingNote = null
        },
        onRestoreFromTrash = { noteId ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          if (notes.any { it.id == noteId }) {
            applyStateChange(NotesEngine.restoreFromTrash(notes, setOf(noteId)))
          }
          editingNote = null
        },
        onCopyToClipboard = { draftToCopy ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          copyNoteToClipboard(context, draftToCopy)
        },
      )
    } else {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = extraImeBottomPadding),
    ) {
      // 1. Standard Tool Header or Multi-Selection Action Toolbar
      if (isSelectionMode) {
        NotesSelectionTopBar(
          selectedCount = selectedNoteIds.size,
          totalVisibleCount = filteredResult.totalCount,
          isTrashView = activeFilter == NotesFilter.Trash,
          isArchiveView = activeFilter == NotesFilter.Archived,
          onClearSelection = { selectedNoteIds = emptySet() },
          onToggleSelectAll = {
            val allVisibleIds = filteredResult.allNotes.map { it.id }.toSet()
            selectedNoteIds = if (selectedNoteIds.containsAll(allVisibleIds)) {
              emptySet()
            } else {
              allVisibleIds
            }
          },
          onTogglePinSelected = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val selectedNotes = notes.filter { it.id in selectedNoteIds }
            val shouldPin = selectedNotes.any { !it.isPinned }
            applyStateChange(NotesEngine.setPinned(notes, selectedNoteIds, shouldPin))
            selectedNoteIds = emptySet()
          },
          onOpenColorPicker = { showBulkColorDialog = true },
          onToggleArchiveSelected = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val shouldArchive = activeFilter != NotesFilter.Archived
            applyStateChange(NotesEngine.setArchived(notes, selectedNoteIds, shouldArchive))
            selectedNoteIds = emptySet()
          },
          onRestoreSelected = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            applyStateChange(NotesEngine.restoreFromTrash(notes, selectedNoteIds))
            selectedNoteIds = emptySet()
          },
          onDeleteSelected = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (activeFilter == NotesFilter.Trash) {
              applyStateChange(NotesEngine.deletePermanently(notes, selectedNoteIds))
            } else {
              applyStateChange(NotesEngine.moveToTrash(notes, selectedNoteIds))
            }
            selectedNoteIds = emptySet()
          },
        )
      } else {
        NotesHeaderTopBar(
          subtext = headerSubtext,
          isSearchActive = isSearchActive,
          searchQuery = searchQuery,
          onSearchQueryChange = { searchQuery = it },
          isGridView = isGridView,
          gridColumnsPerRow = gridColumnsPerRow,
          canUndo = canUndo,
          canRedo = canRedo,
          onToggleSearch = {
            isSearchActive = !isSearchActive
            if (!isSearchActive) {
              searchQuery = ""
              selectedColorFilter = null
            }
          },
          onCloseSearch = {
            isSearchActive = false
            searchQuery = ""
            selectedColorFilter = null
          },
          onToggleViewMode = {
            if (!isGridView) {
              isGridView = true
              repo.setGridView(true)
              updateGridColumnsPerRow(MIN_NOTES_GRID_COLUMNS)
            } else if (gridColumnsPerRow < MAX_NOTES_GRID_COLUMNS) {
              updateGridColumnsPerRow(gridColumnsPerRow + 1)
            } else {
              isGridView = false
              repo.setGridView(false)
            }
          },
          onSelectListView = {
            isGridView = false
            repo.setGridView(false)
          },
          onSelectGridColumns = { cols ->
            isGridView = true
            repo.setGridView(true)
            updateGridColumnsPerRow(cols)
          },
          onUndo = ::undo,
          onRedo = ::redo,
        )
      }

      // 2. Color Filter Bar (shown while searching)
      AnimatedVisibility(
        visible = isSearchActive && !isSelectionMode,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
      ) {
        NotesColorFilterBar(
          selectedColor = selectedColorFilter,
          onSelectColor = { color ->
            selectedColorFilter = if (selectedColorFilter == color) null else color
          },
        )
      }

      // 3. Category & Label Filter Chips Row + Pinned Floating [# per row] + [Manage Labels] Buttons
      NotesFilterChipsRow(
        activeFilter = activeFilter,
        labels = allLabels,
        archivedCount = archivedNotesCount,
        trashedCount = trashedNotesCount,
        isNotesFolderGranted = isNotesFolderGranted,
        pendingPermissionFolderPath = pendingPermissionFolderPath,
        isGridView = isGridView,
        gridColumnsPerRow = gridColumnsPerRow,
        onGridColumnsPerRowChange = { updateGridColumnsPerRow(it) },
        onSelectFilter = { filter ->
          if (!isNotesFolderGranted) {
            requestNotesFolderPermissionPrompt()
          } else {
            activeFilter = filter
            selectedNoteIds = emptySet()
          }
        },
        onRequestNotesFolderPermission = {
          requestNotesFolderPermissionPrompt()
        },
        onDeleteLabel = { labelToDelete ->
          if (!isNotesFolderGranted) {
            requestNotesFolderPermissionPrompt()
          } else {
            requestDeleteLabel(labelToDelete)
          }
        },
        onManageLabels = {
          if (!isNotesFolderGranted) {
            requestNotesFolderPermissionPrompt()
          } else {
            showManageLabelsDialog = true
          }
        },
      )

      // Trash Banner when inside Trash filter
      if (isNotesFolderGranted && activeFilter == NotesFilter.Trash && trashedNotesCount > 0) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Notes in Trash can be restored or deleted forever",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.weight(1f),
            )
            TextButton(
              onClick = { showEmptyTrashDialog = true },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("empty_trash_button"),
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Empty Trash",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }

      val pillFilters = remember(allLabels) {
        buildList {
          add(NotesFilter.All)
          allLabels.forEach { add(NotesFilter.Label(it)) }
          add(NotesFilter.Pinned)
          add(NotesFilter.Checklists)
          add(NotesFilter.Archived)
          add(NotesFilter.Trash)
        }
      }
      val normalizedActiveFilter = remember(pillFilters, activeFilter) {
        pillFilters.firstOrNull { candidate ->
          candidate == activeFilter ||
            (candidate is NotesFilter.Label &&
              activeFilter is NotesFilter.Label &&
              candidate.name.equals((activeFilter as NotesFilter.Label).name, ignoreCase = true))
        } ?: activeFilter
      }

      // 4. Scoped Storage Locked/Permission View or Horizontally Paginated Staggered Grid / List of Notes
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .pinchToChangeColumns(
            enabled = isNotesFolderGranted,
            isListView = !isGridView,
            columnsPerRow = gridColumnsPerRow,
            minColumns = MIN_NOTES_GRID_COLUMNS,
            maxColumns = MAX_NOTES_GRID_COLUMNS,
            onColumnsChange = { cols ->
              if (!isGridView) {
                isGridView = true
                repo.setGridView(true)
              }
              updateGridColumnsPerRow(cols)
            },
            onSelectListView = {
              if (isGridView) {
                isGridView = false
                repo.setGridView(false)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              }
            },
          ),
      ) {
        if (!isNotesFolderGranted) {
          if (pendingPermissionFolderPath != null) {
            NotesFolderPermissionRequestView(
              folder = DEFAULT_NOTES_SCOPED_FOLDER,
              onAllowAccess = {
                requestSystemFolderAccess(DEFAULT_NOTES_SCOPED_FOLDER)
              },
            )
          } else {
            NotesLockedStorageStateView(
              onRequestPermission = {
                requestNotesFolderPermissionPrompt()
              },
            )
          }
        } else {
          ToolboxPillHorizontalPager(
            items = pillFilters,
            selectedItem = normalizedActiveFilter,
            enabled = !isSelectionMode && activeLongPressNoteId == null && draggedNoteId == null,
            key = { _, filter ->
              when (filter) {
                is NotesFilter.All -> "ALL"
                is NotesFilter.Pinned -> "PINNED"
                is NotesFilter.Checklists -> "CHECKLISTS"
                is NotesFilter.Archived -> "ARCHIVED"
                is NotesFilter.Trash -> "TRASH"
                is NotesFilter.Label -> "LABEL:${filter.name.lowercase()}"
              }
            },
            onSelectItem = { filter ->
              isAddNoteOpen = false
              selectedNoteIds = emptySet()
              activeFilter = filter
            },
            modifier = Modifier.fillMaxSize(),
          ) { pageFilter ->
            val isCurrentPage = pageFilter == normalizedActiveFilter
            val pageFilteredResult = remember(notes, pageFilter, isSearchActive, searchQuery, selectedColorFilter) {
              NotesEngine.filterNotes(
                notes = notes,
                filter = pageFilter,
                searchQuery = if (isSearchActive) searchQuery else "",
                colorFilter = if (isSearchActive) selectedColorFilter else null,
              )
            }

            if (pageFilteredResult.isEmpty) {
              NotesEmptyState(
                activeFilter = pageFilter,
                hasSearchQuery = isSearchActive && (searchQuery.isNotBlank() || selectedColorFilter != null),
              )
            } else {
              val columns = if (isGridView) gridColumnsPerRow else 1
              val showSectionHeaders = pageFilter !is NotesFilter.Pinned &&
                pageFilter !is NotesFilter.Archived &&
                pageFilter !is NotesFilter.Trash &&
                pageFilteredResult.pinned.isNotEmpty() &&
                pageFilteredResult.others.isNotEmpty()

              val currentFilteredResult by rememberUpdatedState(pageFilteredResult)
              val currentActiveFilter by rememberUpdatedState(pageFilter)
              val currentIsSearchActive by rememberUpdatedState(isSearchActive)
              val currentSearchQuery by rememberUpdatedState(searchQuery)
              val currentSelectedColorFilter by rememberUpdatedState(selectedColorFilter)
              val currentIsGridView by rememberUpdatedState(isGridView)
              val currentColumns by rememberUpdatedState(columns)
              val pageGridState = if (isCurrentPage) gridState else rememberLazyStaggeredGridState()

              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .then(
                    if (isCurrentPage) {
                      Modifier
                        .onGloballyPositioned { coords ->
                          gridCoordinates = coords
                          syncCardBounds()
                        }
                        .pointerInput(isGridView, pageFilter) {
                          awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            syncCardBounds()
                            val hitNote = findInitialHitNote(
                              point = down.position,
                              visibleNotes = currentFilteredResult.allNotes,
                              cardBounds = cardBounds,
                            ) ?: return@awaitEachGesture

                            val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                            syncCardBounds()
                            val startOffset = longPress.position
                            val rect = cardBounds[hitNote.id]
                            val wasInSelectionMode = selectedNoteIds.isNotEmpty()
                            val selectionBeforePress = selectedNoteIds
                            val isSelectingOnHold = hitNote.id !in selectedNoteIds

                            // Immediately select/deselect the note when held down
                            activeLongPressNoteId = hitNote.id
                            selectedNoteIds = if (isSelectingOnHold) {
                              selectedNoteIds + hitNote.id
                            } else {
                              selectedNoteIds - hitNote.id
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                            val dragSlopPx = maxOf(
                              viewConfiguration.touchSlop,
                              with(density) { 14.dp.toPx() },
                            )
                            var totalDragOffset = Offset.Zero
                            var isReorderDragging = false

                            try {
                              while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == longPress.id } ?: break
                                if (change.changedToUpIgnoreConsumed()) {
                                  change.consume()
                                  break
                                }
                                val dragAmount = change.positionChangeIgnoreConsumed()
                                change.consume()
                                if (dragAmount == Offset.Zero) continue

                                totalDragOffset += dragAmount
                                val currentPointerPos = change.position

                                if (wasInSelectionMode || currentActiveFilter == NotesFilter.Trash) {
                                  syncCardBounds()
                                  val hoveredNote = findInitialHitNote(
                                    point = currentPointerPos,
                                    visibleNotes = currentFilteredResult.allNotes,
                                    cardBounds = cardBounds,
                                  )
                                  if (hoveredNote != null) {
                                    val updatedSelection = if (isSelectingOnHold) {
                                      selectedNoteIds + hoveredNote.id
                                    } else {
                                      selectedNoteIds - hoveredNote.id
                                    }
                                    if (updatedSelection != selectedNoteIds) {
                                      selectedNoteIds = updatedSelection
                                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                  }
                                  continue
                                }

                                if (!isReorderDragging) {
                                  if (totalDragOffset.getDistance() <= dragSlopPx) {
                                    continue
                                  }
                                  isReorderDragging = true
                                  selectedNoteIds = selectionBeforePress
                                  draggedNoteId = hitNote.id
                                  dragPointerOffset = currentPointerPos
                                  grabOffsetFromCenter = if (rect != null) {
                                    startOffset - rect.center
                                  } else {
                                    Offset.Zero
                                  }
                                  draggedCardSize = rect?.let {
                                    IntSize(it.width.roundToInt(), it.height.roundToInt())
                                  }
                                  hasDragged = true
                                  autoScrollSpeedPx = 0f
                                  initialDragSnapshot = NotesHistorySnapshot(notes, savedLabels)
                                } else {
                                  dragPointerOffset += dragAmount
                                }

                                val activeNoteId = draggedNoteId ?: continue
                                val desiredCenter = dragPointerOffset - grabOffsetFromCenter

                                val viewportHeight = (gridCoordinates?.size?.height ?: 0).toFloat()
                                autoScrollSpeedPx = when {
                                  viewportHeight <= 0f -> 0f
                                  dragPointerOffset.y < autoScrollEdgeThresholdPx && gridState.canScrollBackward -> {
                                    val fraction = ((autoScrollEdgeThresholdPx - dragPointerOffset.y) / autoScrollEdgeThresholdPx)
                                      .coerceIn(0f, 1f)
                                    -maxAutoScrollStepPx * fraction
                                  }
                                  dragPointerOffset.y > viewportHeight - autoScrollEdgeThresholdPx && gridState.canScrollForward -> {
                                    val fraction = ((dragPointerOffset.y - (viewportHeight - autoScrollEdgeThresholdPx)) / autoScrollEdgeThresholdPx)
                                      .coerceIn(0f, 1f)
                                    maxAutoScrollStepPx * fraction
                                  }
                                  else -> 0f
                                }

                                val liveFiltered = NotesEngine.filterNotes(
                                  notes = notes,
                                  filter = currentActiveFilter,
                                  searchQuery = if (currentIsSearchActive) currentSearchQuery else "",
                                  colorFilter = if (currentIsSearchActive) currentSelectedColorFilter else null,
                                )
                                val sectionNotes = if (liveFiltered.pinned.any { it.id == activeNoteId }) {
                                  liveFiltered.pinned
                                } else {
                                  liveFiltered.others
                                }

                                val targetNoteId = findTargetNoteId(
                                  draggedNoteId = activeNoteId,
                                  point = desiredCenter,
                                  sectionNotes = sectionNotes,
                                  cardBounds = cardBounds,
                                  isGridView = currentIsGridView,
                                )

                                if (targetNoteId != null && targetNoteId != activeNoteId) {
                                  val firstVisibleIndex = gridState.firstVisibleItemIndex
                                  val firstVisibleOffset = gridState.firstVisibleItemScrollOffset
                                  val reordered = NotesEngine.reorderNotes(notes, activeNoteId, targetNoteId)
                                  if (reordered != notes) {
                                    val nextFiltered = NotesEngine.filterNotes(
                                      notes = reordered,
                                      filter = currentActiveFilter,
                                      searchQuery = if (currentIsSearchActive) currentSearchQuery else "",
                                      colorFilter = if (currentIsSearchActive) currentSelectedColorFilter else null,
                                    )
                                    val newSectionNotes = if (nextFiltered.pinned.any { it.id == activeNoteId }) {
                                      nextFiltered.pinned
                                    } else {
                                      nextFiltered.others
                                    }
                                    updateSectionCardBoundsOptimistically(
                                      oldSectionNotes = sectionNotes,
                                      newSectionNotes = newSectionNotes,
                                      cardBounds = cardBounds,
                                      isGridView = currentIsGridView,
                                      verticalSpacingPx = verticalSpacingPx,
                                      columns = currentColumns,
                                    )
                                    notes = reordered
                                    gridState.requestScrollToItem(firstVisibleIndex, firstVisibleOffset)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                  }
                                }
                              }
                            } finally {
                              val before = initialDragSnapshot
                              val didReorder = isReorderDragging
                              activeLongPressNoteId = null
                              draggedNoteId = null
                              dragPointerOffset = Offset.Zero
                              grabOffsetFromCenter = Offset.Zero
                              draggedCardSize = null
                              hasDragged = false
                              autoScrollSpeedPx = 0f
                              initialDragSnapshot = null

                              if (didReorder && before != null && before.notes != notes) {
                                undoStack = listOf(before) + undoStack.take(49)
                                redoStack = emptyList()
                                repo.saveNotes(notes)
                                NotesWidgets.updateAllWidgets(context)
                              }
                            }
                          }
                        }
                    } else {
                      Modifier
                    }
                  ),
              ) {
                LazyVerticalStaggeredGrid(
                  state = pageGridState,
                  userScrollEnabled = draggedNoteId == null,
                  columns = StaggeredGridCells.Fixed(columns),
                  contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 6.dp,
                    bottom = 88.dp,
                  ),
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  verticalItemSpacing = 10.dp,
                  modifier = Modifier
                    .fillMaxSize()
                    .then(if (isCurrentPage) Modifier.testTag("notes_staggered_grid") else Modifier),
                ) {
                  if (pageFilteredResult.pinned.isNotEmpty()) {
                    if (showSectionHeaders || pageFilter == NotesFilter.All) {
                      item(
                        key = "section_header_pinned",
                        span = StaggeredGridItemSpan.FullLine,
                      ) {
                        NotesSectionHeader(
                          title = "PINNED",
                          modifier = if (isCurrentPage) Modifier.testTag("notes_section_pinned") else Modifier,
                        )
                      }
                    }

                    items(
                      items = pageFilteredResult.pinned,
                      key = { "pinned_${it.id}" },
                    ) { note ->
                      val isDraggingThis = isCurrentPage && draggedNoteId == note.id
                      if (isCurrentPage) {
                        DisposableEffect(note.id) {
                          onDispose {
                            cardCoordinates.remove(note.id)
                            cardBounds.remove(note.id)
                          }
                        }
                      }
                      NoteCard(
                        note = note,
                        isSelected = note.id in selectedNoteIds,
                        isSelectionMode = isSelectionMode,
                        isDragging = false,
                        testTagOverride = if (isCurrentPage) null else "pager_preview_note_${note.id}",
                        onClick = {
                          if (draggedNoteId != null || activeLongPressNoteId != null) return@NoteCard
                          if (isSelectionMode) {
                            selectedNoteIds = if (note.id in selectedNoteIds) {
                              selectedNoteIds - note.id
                            } else {
                              selectedNoteIds + note.id
                            }
                          } else {
                            openSingleNoteView(note)
                          }
                        },
                        onLongClick = null,
                        onTogglePin = {
                          if (draggedNoteId != null || activeLongPressNoteId != null) return@NoteCard
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          applyStateChange(NotesEngine.togglePin(notes, note.id))
                        },
                        onToggleChecklistItem = { itemId ->
                          if (draggedNoteId != null || activeLongPressNoteId != null) return@NoteCard
                          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                          applyStateChange(NotesEngine.toggleChecklistItem(notes, note.id, itemId))
                        },
                        modifier = Modifier
                          .then(
                            if (isCurrentPage) {
                              Modifier.onGloballyPositioned { coords ->
                                cardCoordinates[note.id] = coords
                                val gridCoords = gridCoordinates
                                if (gridCoords != null && gridCoords.isAttached && coords.isAttached) {
                                  cardBounds[note.id] = gridCoords.localBoundingBoxOf(coords, clipBounds = false)
                                }
                              }
                            } else {
                              Modifier
                            }
                          )
                          .graphicsLayer {
                            if (isDraggingThis) {
                              alpha = 0.3f
                            }
                          },
                      )
                    }
                  }

                  if (pageFilteredResult.others.isNotEmpty()) {
                    if (showSectionHeaders) {
                      item(
                        key = "section_header_others",
                        span = StaggeredGridItemSpan.FullLine,
                      ) {
                        NotesSectionHeader(
                          title = "OTHERS",
                          modifier = Modifier
                            .padding(top = 6.dp)
                            .then(if (isCurrentPage) Modifier.testTag("notes_section_others") else Modifier),
                        )
                      }
                    }

                    items(
                      items = pageFilteredResult.others,
                      key = { "other_${it.id}" },
                    ) { note ->
                      val isDraggingThis = isCurrentPage && draggedNoteId == note.id
                      if (isCurrentPage) {
                        DisposableEffect(note.id) {
                          onDispose {
                            cardCoordinates.remove(note.id)
                            cardBounds.remove(note.id)
                          }
                        }
                      }
                      NoteCard(
                        note = note,
                        isSelected = note.id in selectedNoteIds,
                        isSelectionMode = isSelectionMode,
                        isDragging = false,
                        testTagOverride = if (isCurrentPage) null else "pager_preview_note_${note.id}",
                        onClick = {
                          if (draggedNoteId != null || activeLongPressNoteId != null) return@NoteCard
                          if (isSelectionMode) {
                            selectedNoteIds = if (note.id in selectedNoteIds) {
                              selectedNoteIds - note.id
                            } else {
                              selectedNoteIds + note.id
                            }
                          } else {
                            openSingleNoteView(note)
                          }
                        },
                        onLongClick = null,
                        onTogglePin = {
                          if (draggedNoteId != null || activeLongPressNoteId != null) return@NoteCard
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          applyStateChange(NotesEngine.togglePin(notes, note.id))
                        },
                        onToggleChecklistItem = { itemId ->
                          if (draggedNoteId != null || activeLongPressNoteId != null) return@NoteCard
                          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                          applyStateChange(NotesEngine.toggleChecklistItem(notes, note.id, itemId))
                        },
                        modifier = Modifier
                          .then(
                            if (isCurrentPage) {
                              Modifier.onGloballyPositioned { coords ->
                                cardCoordinates[note.id] = coords
                                val gridCoords = gridCoordinates
                                if (gridCoords != null && gridCoords.isAttached && coords.isAttached) {
                                  cardBounds[note.id] = gridCoords.localBoundingBoxOf(coords, clipBounds = false)
                                }
                              }
                            } else {
                              Modifier
                            }
                          )
                          .graphicsLayer {
                            if (isDraggingThis) {
                              alpha = 0.3f
                            }
                          },
                      )
                    }
                  }
                }

                // Floating lifted note card overlay while dragging
                val activeDraggedNote = if (isCurrentPage) draggedNoteId?.let { id -> notes.firstOrNull { it.id == id } } else null
                if (activeDraggedNote != null && gridCoordinates != null) {
                  val desiredCenter = dragPointerOffset - grabOffsetFromCenter
                  val fallbackWidthPx = ((gridCoordinates?.size?.width ?: 1) / columns.toFloat())
                  val cardWidthPx = draggedCardSize?.width?.toFloat()
                    ?: cardBounds[activeDraggedNote.id]?.width
                    ?: fallbackWidthPx
                  val cardHeightPx = draggedCardSize?.height?.toFloat()
                    ?: cardBounds[activeDraggedNote.id]?.height
                    ?: with(density) { 100.dp.toPx() }

                  Box(
                    modifier = Modifier
                      .width(with(density) { cardWidthPx.toDp() })
                      .graphicsLayer {
                        translationX = desiredCenter.x - cardWidthPx / 2f
                        translationY = desiredCenter.y - cardHeightPx / 2f
                        scaleX = 1.04f
                        scaleY = 1.04f
                      }
                      .zIndex(100f),
                  ) {
                    NoteCard(
                      note = activeDraggedNote,
                      isSelected = activeDraggedNote.id in selectedNoteIds,
                      isSelectionMode = isSelectionMode,
                      isDragging = true,
                      testTagOverride = "dragged_note_overlay",
                      onClick = {},
                      onLongClick = null,
                      onTogglePin = {},
                      onToggleChecklistItem = {},
                    )
                  }
                }
              }
            }
          }
        }

        // 5. Floating 28.dp Quick-Capture Bar ("Take a note..." + Checklist shortcut) with Slide-Up Add Note View
        if (isNotesFolderGranted && !isSelectionMode && activeFilter != NotesFilter.Trash) {
          val preselectedLabels = when (val filter = activeFilter) {
            is NotesFilter.Label -> listOf(filter.name)
            else -> emptyList()
          }
          val defaultPinned = activeFilter == NotesFilter.Pinned
          val containerColor = if (isAddNoteOpen) {
            resolveNoteCardColor(addingNoteDraft.color, isElevatedSurface = true)
          } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
          }
          var addNoteDragOffsetY by remember(isAddNoteOpen) { mutableFloatStateOf(0f) }
          val dismissDragThresholdPx = with(density) { 48.dp.toPx() }
          val handleDragDownDelta: (Float) -> Unit = { deltaY ->
            addNoteDragOffsetY = (addNoteDragOffsetY + deltaY).coerceAtLeast(0f)
          }
          val handleDragDownEnd: () -> Unit = {
            if (addNoteDragOffsetY >= dismissDragThresholdPx) {
              addNoteDragOffsetY = 0f
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              saveAndCloseAddingNote()
            } else {
              addNoteDragOffsetY = 0f
            }
          }
          val handleDragDownCancel: () -> Unit = {
            addNoteDragOffsetY = 0f
          }

          val hexagonEnabled = currentHexagonIconButtons()
          Box(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
          ) {
            Surface(
              shape = if (hexagonEnabled && !isAddNoteOpen) {
                HorizontalHexagonShape
              } else {
                RoundedCornerShape(28.dp)
              },
              color = containerColor,
              tonalElevation = 6.dp,
              shadowElevation = 4.dp,
              border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              ),
              modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                  translationY = if (isAddNoteOpen) addNoteDragOffsetY else 0f
                }
                .testTag("notes_quick_capture_card"),
            ) {
              Column(modifier = Modifier.fillMaxWidth()) {
                NotesQuickCaptureBar(
                  isAddNoteOpen = isAddNoteOpen,
                  isChecklistMode = isAddNoteOpen && addingNoteDraft.isChecklist,
                  isPinned = if (isAddNoteOpen) addingNoteDraft.isPinned else defaultPinned,
                  onTakeTextNote = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (!isAddNoteOpen) {
                      addingNoteNewLabels = emptyList()
                      addingNoteDraft = NoteItem(
                        isChecklist = false,
                        isPinned = defaultPinned,
                        labels = preselectedLabels,
                      )
                      isAddNoteOpen = true
                    } else if (addingNoteDraft.isChecklist) {
                      addingNoteDraft = NotesEngine.toggleChecklistMode(addingNoteDraft)
                    } else {
                      saveAndCloseAddingNote()
                    }
                  },
                  onTakePinnedNote = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (!isAddNoteOpen) {
                      addingNoteNewLabels = emptyList()
                      addingNoteDraft = NoteItem(
                        isChecklist = false,
                        isPinned = true,
                        labels = preselectedLabels,
                      )
                      isAddNoteOpen = true
                    } else {
                      val newPinned = !addingNoteDraft.isPinned
                      addingNoteDraft = addingNoteDraft.copy(
                        isPinned = newPinned,
                        isArchived = if (newPinned) false else addingNoteDraft.isArchived,
                      )
                    }
                  },
                  onTakeChecklistNote = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (!isAddNoteOpen) {
                      addingNoteNewLabels = emptyList()
                      addingNoteDraft = NoteItem(
                        isChecklist = true,
                        checklistItems = emptyList(),
                        isPinned = defaultPinned,
                        labels = preselectedLabels,
                      )
                      isAddNoteOpen = true
                    } else {
                      addingNoteDraft = NotesEngine.toggleChecklistMode(addingNoteDraft)
                    }
                  },
                  onDragDownDelta = handleDragDownDelta,
                  onDragDownEnd = handleDragDownEnd,
                  onDragDownCancel = handleDragDownCancel,
                )

                // Add Note view slides up inside the floating 28.dp pill bar (similar to Todo schedule panel)
                AnimatedVisibility(
                  visible = isAddNoteOpen,
                  enter = slideInVertically(initialOffsetY = { it }) +
                    expandVertically(expandFrom = Alignment.Top) +
                    fadeIn(),
                  exit = slideOutVertically(targetOffsetY = { it }) +
                    shrinkVertically(shrinkTowards = Alignment.Top) +
                    fadeOut(),
                ) {
                  AddNoteSlideUpPanel(
                    draft = addingNoteDraft,
                    onDraftChange = { addingNoteDraft = it },
                    availableLabels = allLabels,
                    newlyCreatedLabels = addingNoteNewLabels,
                    onNewlyCreatedLabelsChange = { addingNoteNewLabels = it },
                    autoFocusTitle = isAddNoteOpen,
                    onSaveAndDismiss = { saveAndCloseAddingNote() },
                    onDiscard = {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      isAddNoteOpen = false
                      addingNoteNewLabels = emptyList()
                      focusManager.clearFocus()
                    },
                    onCopyToClipboard = { draftToCopy ->
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      copyNoteToClipboard(context, draftToCopy)
                    },
                    onDragDownDelta = handleDragDownDelta,
                    onDragDownEnd = handleDragDownEnd,
                    onDragDownCancel = handleDragDownCancel,
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
  }

  // Manage Labels Dialog
  if (showManageLabelsDialog) {
    ManageLabelsDialog(
      labels = allLabels,
      notesCountByLabel = allLabels.associateWith { label ->
        notes.count { !it.isTrashed && it.labels.any { l -> l.equals(label, ignoreCase = true) } }
      },
      onAddLabel = { newLabel ->
        val trimmed = newLabel.trim()
        if (trimmed.isNotEmpty() && allLabels.none { it.equals(trimmed, ignoreCase = true) }) {
          applyStateChange(notes, savedLabels + trimmed)
        }
      },
      onDeleteLabel = { labelToDelete ->
        requestDeleteLabel(labelToDelete)
      },
      onDismiss = { showManageLabelsDialog = false },
    )
  }

  // Delete Custom Label Confirmation Dialog (shown when notes are using the label)
  labelPendingDelete?.let { pendingLabel ->
    val notesUsingLabel = NotesEngine.getNotesUsingLabel(notes, pendingLabel)
    DeleteLabelConfirmDialog(
      label = pendingLabel,
      matchingNotes = notesUsingLabel,
      onConfirmDelete = {
        deleteLabelGloballyNow(pendingLabel)
        labelPendingDelete = null
      },
      onDismiss = { labelPendingDelete = null },
    )
  }

  // Bulk Color Picker Dialog
  if (showBulkColorDialog) {
    BulkNoteColorDialog(
      selectedCount = selectedNoteIds.size,
      onSelectColor = { chosenColor ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        applyStateChange(NotesEngine.setColor(notes, selectedNoteIds, chosenColor))
        showBulkColorDialog = false
        selectedNoteIds = emptySet()
      },
      onDismiss = { showBulkColorDialog = false },
    )
  }

  // Empty Trash Confirmation Dialog
  if (showEmptyTrashDialog) {
    EmptyTrashConfirmDialog(
      trashedCount = trashedNotesCount,
      onConfirmEmpty = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        applyStateChange(NotesEngine.emptyTrash(notes))
        showEmptyTrashDialog = false
      },
      onDismiss = { showEmptyTrashDialog = false },
    )
  }
}

private fun copyNoteToClipboard(context: Context, note: NoteItem) {
  val text = note.toPlainText()
  if (text.isBlank()) return
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
  clipboard?.setPrimaryClip(ClipData.newPlainText(note.title.ifBlank { "Note" }, text))
  Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesHeaderTopBar(
  subtext: String,
  isSearchActive: Boolean,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  isGridView: Boolean,
  gridColumnsPerRow: Int = DEFAULT_NOTES_GRID_COLUMNS,
  canUndo: Boolean,
  canRedo: Boolean,
  onToggleSearch: () -> Unit,
  onCloseSearch: () -> Unit,
  onToggleViewMode: () -> Unit,
  onSelectListView: () -> Unit = onToggleViewMode,
  onSelectGridColumns: (Int) -> Unit = { onToggleViewMode() },
  onUndo: () -> Unit,
  onRedo: () -> Unit,
) {
  val focusManager = LocalFocusManager.current

  BackHandler(enabled = isSearchActive) {
    focusManager.clearFocus()
    onCloseSearch()
  }

  ToolboxTopBar(
    title = "Notes",
    subtext = subtext,
    icon = Icons.AutoMirrored.Filled.StickyNote2,
    subtextTestTag = "notes_header_subtext",
    searchState = ToolboxTopBarSearchState(
      isActive = isSearchActive,
      query = searchQuery,
      onQueryChange = onSearchQueryChange,
      onOpenSearch = onToggleSearch,
      onCloseSearch = {
        focusManager.clearFocus()
        onCloseSearch()
      },
      placeholder = "Search titles, notes, checklists, or labels",
      searchContentDescription = "Search notes",
      closeContentDescription = "Close search",
      clearContentDescription = "Clear search text",
      toggleButtonTestTag = "notes_search_toggle",
      searchBarTestTag = "notes_toolbar_search_bar",
      closeButtonTestTag = "notes_search_close",
      inputTestTag = "notes_search_input",
      clearButtonTestTag = "notes_search_clear",
      onSearchImeAction = { focusManager.clearFocus() },
    ),
    actions = {
      if (canUndo || canRedo) {
        IconButton(
          onClick = onUndo,
          enabled = canUndo,
          modifier = Modifier.testTag("notes_undo_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Undo,
            contentDescription = "Undo",
            tint = if (canUndo) {
              MaterialTheme.colorScheme.onSurface
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            },
          )
        }
        IconButton(
          onClick = onRedo,
          enabled = canRedo,
          modifier = Modifier.testTag("notes_redo_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Redo,
            contentDescription = "Redo",
            tint = if (canRedo) {
              MaterialTheme.colorScheme.onSurface
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            },
          )
        }
      }

      ToolboxViewModeCycleButton(
        isListView = !isGridView,
        columnsPerRow = gridColumnsPerRow,
        minColumns = MIN_NOTES_GRID_COLUMNS,
        maxColumns = MAX_NOTES_GRID_COLUMNS,
        onSelectListView = onSelectListView,
        onSelectGridColumns = onSelectGridColumns,
        buttonTestTag = "notes_view_mode_toggle",
        countTestTag = "notes_columns_per_row_count",
        gridContentDescription = "$gridColumnsPerRow notes per row",
      )
    },
  )
}

@Composable
private fun NotesSelectionTopBar(
  selectedCount: Int,
  totalVisibleCount: Int,
  isTrashView: Boolean,
  isArchiveView: Boolean,
  onClearSelection: () -> Unit,
  onToggleSelectAll: () -> Unit,
  onTogglePinSelected: () -> Unit,
  onOpenColorPicker: () -> Unit,
  onToggleArchiveSelected: () -> Unit,
  onRestoreSelected: () -> Unit,
  onDeleteSelected: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("notes_selection_top_bar"),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(68.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      ToolboxMultiSelectBarRow(
        selectionState = ToolboxTopBarSelectionState(
          selectedCount = selectedCount,
          totalCount = totalVisibleCount,
          onToggleSelectAll = onToggleSelectAll,
          onCloseSelection = onClearSelection,
          closeButtonTestTag = "notes_clear_selection",
          selectAllRowTestTag = "notes_select_all_toggle",
          actions = {
            var showOverflowMenu by remember { mutableStateOf(false) }
            val hasSelection = selectedCount > 0

            if (isTrashView) {
              IconButton(
                onClick = onRestoreSelected,
                enabled = hasSelection,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("notes_bulk_restore"),
              ) {
                Icon(
                  imageVector = Icons.Default.RestoreFromTrash,
                  contentDescription = "Restore selected notes",
                  tint = if (hasSelection) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                  },
                  modifier = Modifier.size(20.dp),
                )
              }
            } else {
              IconButton(
                onClick = onTogglePinSelected,
                enabled = hasSelection,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("notes_bulk_pin"),
              ) {
                Icon(
                  imageVector = Icons.Default.PushPin,
                  contentDescription = "Pin or unpin selected",
                  tint = if (hasSelection) {
                    MaterialTheme.colorScheme.onSurface
                  } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                  },
                  modifier = Modifier.size(20.dp),
                )
              }
              IconButton(
                onClick = onOpenColorPicker,
                enabled = hasSelection,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("notes_bulk_color"),
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = "Change color",
                  tint = if (hasSelection) {
                    MaterialTheme.colorScheme.onSurface
                  } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                  },
                  modifier = Modifier.size(20.dp),
                )
              }
              IconButton(
                onClick = onToggleArchiveSelected,
                enabled = hasSelection,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("notes_bulk_archive"),
              ) {
                Icon(
                  imageVector = if (isArchiveView) Icons.Default.Unarchive else Icons.Default.Archive,
                  contentDescription = if (isArchiveView) "Unarchive selected" else "Archive selected",
                  tint = if (hasSelection) {
                    MaterialTheme.colorScheme.onSurface
                  } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                  },
                  modifier = Modifier.size(20.dp),
                )
              }
            }

            IconButton(
              onClick = onDeleteSelected,
              enabled = hasSelection,
              modifier = Modifier
                .size(36.dp)
                .testTag("notes_bulk_delete"),
            ) {
              Icon(
                imageVector = if (isTrashView) Icons.Default.DeleteForever else Icons.Default.Delete,
                contentDescription = if (isTrashView) "Delete forever" else "Move to trash",
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
                if (isTrashView) {
                  DropdownMenuItem(
                    text = { Text("Restore") },
                    leadingIcon = { Icon(Icons.Default.RestoreFromTrash, contentDescription = null) },
                    enabled = hasSelection,
                    onClick = {
                      showOverflowMenu = false
                      onRestoreSelected()
                    },
                  )
                } else {
                  DropdownMenuItem(
                    text = { Text("Pin / Unpin") },
                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                    enabled = hasSelection,
                    onClick = {
                      showOverflowMenu = false
                      onTogglePinSelected()
                    },
                  )
                  DropdownMenuItem(
                    text = { Text("Change color") },
                    leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    enabled = hasSelection,
                    onClick = {
                      showOverflowMenu = false
                      onOpenColorPicker()
                    },
                  )
                  DropdownMenuItem(
                    text = { Text(if (isArchiveView) "Unarchive" else "Archive") },
                    leadingIcon = {
                      Icon(
                        if (isArchiveView) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = null,
                      )
                    },
                    enabled = hasSelection,
                    onClick = {
                      showOverflowMenu = false
                      onToggleArchiveSelected()
                    },
                  )
                }
                DropdownMenuItem(
                  text = {
                    Text(
                      if (isTrashView) "Delete forever" else "Move to trash",
                      color = MaterialTheme.colorScheme.error,
                    )
                  },
                  leadingIcon = {
                    Icon(
                      if (isTrashView) Icons.Default.DeleteForever else Icons.Default.Delete,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.error,
                    )
                  },
                  enabled = hasSelection,
                  onClick = {
                    showOverflowMenu = false
                    onDeleteSelected()
                  },
                )
                HorizontalDivider()
                DropdownMenuItem(
                  text = {
                    Text(if (selectedCount == totalVisibleCount && totalVisibleCount > 0) "Deselect all" else "Select all")
                  },
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
                    onClearSelection()
                  },
                )
              }
            }
          },
        ),
      )
    }
  }
}

@Composable
private fun NotesColorFilterBar(
  selectedColor: NoteColor?,
  onSelectColor: (NoteColor) -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("notes_color_filter_bar"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Color:",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(end = 2.dp),
      )
      NoteColor.entries.forEach { color ->
        val isSelected = selectedColor == color
        val swatchColor = resolveNoteSwatchColor(color)
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(swatchColor)
            .border(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.outlineVariant
              },
              shape = CircleShape,
            )
            .clickable { onSelectColor(color) }
            .testTag("notes_color_filter_${color.name}"),
          contentAlignment = Alignment.Center,
        ) {
          if (isSelected) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "${color.displayName} active",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(15.dp),
            )
          } else if (color == NoteColor.DEFAULT) {
            Icon(
              imageVector = Icons.Default.FormatColorReset,
              contentDescription = color.displayName,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NotesFilterChipsRow(
  activeFilter: NotesFilter,
  labels: List<String>,
  archivedCount: Int,
  trashedCount: Int,
  isNotesFolderGranted: Boolean = true,
  pendingPermissionFolderPath: String? = null,
  isGridView: Boolean = true,
  gridColumnsPerRow: Int = DEFAULT_NOTES_GRID_COLUMNS,
  onGridColumnsPerRowChange: (Int) -> Unit = {},
  onSelectFilter: (NotesFilter) -> Unit,
  onRequestNotesFolderPermission: () -> Unit = {},
  onDeleteLabel: (String) -> Unit = {},
  onManageLabels: () -> Unit,
) {
  ToolboxPillNavigationBar(
    rowTestTag = "notes_filter_chips_row",
    floatingButtonsCount = 1,
    pills = {
      ToolboxPillNavigationItem(
        selected = if (!isNotesFolderGranted) {
          pendingPermissionFolderPath != null
        } else {
          activeFilter == NotesFilter.All
        },
        onClick = {
          if (!isNotesFolderGranted) {
            onRequestNotesFolderPermission()
          } else {
            onSelectFilter(NotesFilter.All)
          }
        },
        leadingIconContent = if (!isNotesFolderGranted) {
          {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .semantics(mergeDescendants = true) {
                  onClick {
                    onRequestNotesFolderPermission()
                    true
                  }
                }
                .testTag("notes_filter_lock_icon_ALL"),
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Unlock Notes folder",
                modifier = Modifier
                  .size(16.dp)
                  .testTag("folder_lock_icon_Notes"),
              )
            }
          }
        } else {
          null
        },
        label = "All",
        testTag = "notes_filter_all",
        modifier = Modifier.graphicsLayer {
          alpha = if (!isNotesFolderGranted) 0.72f else 1f
        },
      )

      labels.forEach { label ->
        val isSelected = isNotesFolderGranted &&
          activeFilter is NotesFilter.Label &&
          activeFilter.name.equals(label, ignoreCase = true)
        ToolboxPillNavigationItem(
          selected = isSelected,
          onClick = {
            if (!isNotesFolderGranted) {
              onRequestNotesFolderPermission()
            } else {
              onSelectFilter(NotesFilter.Label(label))
            }
          },
          leadingIcon = if (!isNotesFolderGranted) Icons.Default.Lock else null,
          label = label,
          trailingIcon = if (isNotesFolderGranted) {
            {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable { onDeleteLabel(label) }
                  .testTag("notes_filter_label_delete_$label"),
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Delete label $label",
                  modifier = Modifier
                    .size(16.dp)
                    .testTag("notes_filter_label_delete_icon_$label"),
                )
              }
            }
          } else {
            null
          },
          testTag = "notes_filter_label_$label",
          modifier = Modifier.graphicsLayer {
            alpha = if (!isNotesFolderGranted) 0.72f else 1f
          },
        )
      }

      ToolboxPillNavigationItem(
        selected = isNotesFolderGranted && activeFilter == NotesFilter.Pinned,
        onClick = {
          if (!isNotesFolderGranted) {
            onRequestNotesFolderPermission()
          } else {
            onSelectFilter(NotesFilter.Pinned)
          }
        },
        leadingIconContent = {
          Icon(
            imageVector = if (isNotesFolderGranted) Icons.Default.PushPin else Icons.Default.Lock,
            contentDescription = if (isNotesFolderGranted) "Pinned" else "Unlock Notes folder for Pinned",
            modifier = Modifier
              .size(16.dp)
              .testTag(
                if (isNotesFolderGranted) {
                  "notes_filter_unlocked_icon_PINNED"
                } else {
                  "notes_filter_lock_icon_PINNED"
                }
              ),
          )
        },
        label = "Pinned",
        testTag = "notes_filter_pinned",
        modifier = Modifier.graphicsLayer {
          alpha = if (!isNotesFolderGranted) 0.72f else 1f
        },
      )

      ToolboxPillNavigationItem(
        selected = isNotesFolderGranted && activeFilter == NotesFilter.Checklists,
        onClick = {
          if (!isNotesFolderGranted) {
            onRequestNotesFolderPermission()
          } else {
            onSelectFilter(NotesFilter.Checklists)
          }
        },
        leadingIconContent = {
          Icon(
            imageVector = if (isNotesFolderGranted) Icons.Default.CheckBox else Icons.Default.Lock,
            contentDescription = if (isNotesFolderGranted) "Checklists" else "Unlock Notes folder for Checklists",
            modifier = Modifier
              .size(16.dp)
              .testTag(
                if (isNotesFolderGranted) {
                  "notes_filter_unlocked_icon_CHECKLISTS"
                } else {
                  "notes_filter_lock_icon_CHECKLISTS"
                }
              ),
          )
        },
        label = "Checklists",
        testTag = "notes_filter_checklists",
        modifier = Modifier.graphicsLayer {
          alpha = if (!isNotesFolderGranted) 0.72f else 1f
        },
      )

      ToolboxPillNavigationItem(
        selected = isNotesFolderGranted && activeFilter == NotesFilter.Archived,
        onClick = {
          if (!isNotesFolderGranted) {
            onRequestNotesFolderPermission()
          } else {
            onSelectFilter(NotesFilter.Archived)
          }
        },
        leadingIconContent = {
          Icon(
            imageVector = if (isNotesFolderGranted) Icons.Default.Archive else Icons.Default.Lock,
            contentDescription = if (isNotesFolderGranted) "Archive" else "Unlock Notes folder for Archive",
            modifier = Modifier
              .size(16.dp)
              .testTag(
                if (isNotesFolderGranted) {
                  "notes_filter_unlocked_icon_ARCHIVED"
                } else {
                  "notes_filter_lock_icon_ARCHIVED"
                }
              ),
          )
        },
        label = if (isNotesFolderGranted && archivedCount > 0) "Archive ($archivedCount)" else "Archive",
        testTag = "notes_filter_archived",
        modifier = Modifier.graphicsLayer {
          alpha = if (!isNotesFolderGranted) 0.72f else 1f
        },
      )

      ToolboxPillNavigationItem(
        selected = isNotesFolderGranted && activeFilter == NotesFilter.Trash,
        onClick = {
          if (!isNotesFolderGranted) {
            onRequestNotesFolderPermission()
          } else {
            onSelectFilter(NotesFilter.Trash)
          }
        },
        leadingIconContent = {
          Icon(
            imageVector = if (isNotesFolderGranted) Icons.Default.Delete else Icons.Default.Lock,
            contentDescription = if (isNotesFolderGranted) "Trash" else "Unlock Notes folder for Trash",
            modifier = Modifier
              .size(16.dp)
              .testTag(
                if (isNotesFolderGranted) {
                  "notes_filter_unlocked_icon_TRASH"
                } else {
                  "notes_filter_lock_icon_TRASH"
                }
              ),
          )
        },
        label = if (isNotesFolderGranted && trashedCount > 0) "Trash ($trashedCount)" else "Trash",
        testTag = "notes_filter_trash",
        modifier = Modifier.graphicsLayer {
          alpha = if (!isNotesFolderGranted) 0.72f else 1f
        },
      )
    },
    floatingControls = {
      Surface(
        onClick = onManageLabels,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
          .size(34.dp)
          .testTag("notes_manage_labels_button"),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Label,
            contentDescription = "Manage labels",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    },
  )
}

/**
 * View displayed in the Notes tool when scoped storage access is locked (permission to `/Notes`
 * has not been granted yet), styled to match the Gallery, Audio Recorder, and Music Player tools.
 */
@Composable
private fun NotesLockedStorageStateView(
  onRequestPermission: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onRequestPermission)
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
        text = "Tap the lock icon on a navigation pill above to grant permission to access .text files in /Notes.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
  }
}

/**
 * View displayed when the user taps a locked navigation pill or its lock icon in the Notes tool,
 * asking for permission to access `.text` files in `/Notes` and launching the Android system
 * directory access prompt when the user taps "Allow".
 */
@Composable
private fun NotesFolderPermissionRequestView(
  folder: NotesFolder,
  onAllowAccess: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("folder_permission_view"),
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
        text = "Allow access to files in ${folder.folderPath}?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("folder_permission_title"),
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Notes needs permission to access files in ${folder.folderPath} (${folder.folderName}) to read and save .text notes in this location.",
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

@Composable
private fun NotesSectionHeader(
  title: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelMedium,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
  )
}

internal fun findInitialHitNote(
  point: Offset,
  visibleNotes: List<NoteItem>,
  cardBounds: Map<String, Rect>,
): NoteItem? {
  for (note in visibleNotes) {
    val rect = cardBounds[note.id] ?: continue
    if (rect.contains(point)) {
      return note
    }
  }
  return null
}

internal fun findTargetNoteId(
  draggedNoteId: String,
  point: Offset,
  sectionNotes: List<NoteItem>,
  cardBounds: Map<String, Rect>,
  isGridView: Boolean,
): String? {
  val currentIndex = sectionNotes.indexOfFirst { it.id == draggedNoteId }
  if (currentIndex == -1 || sectionNotes.size <= 1) return null
  val draggedRect = cardBounds[draggedNoteId] ?: return null

  var candidateIndex: Int? = null
  for (index in sectionNotes.indices) {
    val note = sectionNotes[index]
    val rect = cardBounds[note.id] ?: continue
    if (rect.contains(point)) {
      candidateIndex = index
      break
    }
  }

  if (candidateIndex == null) {
    var minDistanceSq = Float.MAX_VALUE
    for (index in sectionNotes.indices) {
      val note = sectionNotes[index]
      val rect = cardBounds[note.id] ?: continue
      val dx = point.x - rect.center.x
      val dy = point.y - rect.center.y
      val distSq = dx * dx + dy * dy
      if (distSq < minDistanceSq) {
        minDistanceSq = distSq
        candidateIndex = index
      }
    }
  }

  val targetIndex = candidateIndex ?: return null
  if (targetIndex == currentIndex) return null

  val targetNote = sectionNotes[targetIndex]
  val targetRect = cardBounds[targetNote.id] ?: return null

  val isDifferentColumn = isGridView &&
    abs(targetRect.center.x - draggedRect.center.x) >= draggedRect.width * 0.45f

  if (isDifferentColumn) {
    val passedHorizontally = if (targetRect.center.x > draggedRect.center.x) {
      point.x >= targetRect.left + targetRect.width * 0.25f
    } else {
      point.x <= targetRect.right - targetRect.width * 0.25f
    }
    if (!passedHorizontally) return null

    val verticallySeparated = targetRect.top >= draggedRect.bottom || targetRect.bottom <= draggedRect.top
    if (verticallySeparated) {
      val passedVertically = if (targetRect.center.y > draggedRect.center.y) {
        point.y >= targetRect.top + targetRect.height * 0.25f
      } else {
        point.y <= targetRect.bottom - targetRect.height * 0.25f
      }
      if (!passedVertically) return null
    }
    return targetNote.id
  } else {
    val passedVertically = if (targetIndex > currentIndex) {
      point.y >= targetRect.center.y - 2f
    } else {
      point.y <= targetRect.center.y + 2f
    }
    return if (passedVertically) targetNote.id else null
  }
}

internal fun updateSectionCardBoundsOptimistically(
  oldSectionNotes: List<NoteItem>,
  newSectionNotes: List<NoteItem>,
  cardBounds: MutableMap<String, Rect>,
  isGridView: Boolean,
  verticalSpacingPx: Float,
  columns: Int = 2,
) {
  val existingRects = oldSectionNotes.mapNotNull { cardBounds[it.id] }
  if (existingRects.size != oldSectionNotes.size || existingRects.isEmpty()) return

  val sectionTop = existingRects.minOf { it.top }
  val colCount = if (isGridView) columns.coerceAtLeast(2) else 1
  if (colCount <= 1 || existingRects.size == 1) {
    val left = existingRects.first().left
    val width = existingRects.first().width
    var currentTop = sectionTop
    for (note in newSectionNotes) {
      val h = cardBounds[note.id]?.height ?: continue
      cardBounds[note.id] = Rect(
        left = left,
        top = currentTop,
        right = left + width,
        bottom = currentTop + h,
      )
      currentTop += h + verticalSpacingPx
    }
  } else {
    val minLeft = existingRects.minOf { it.left }
    val colWidth = existingRects.first().width
    val colStep = colWidth + verticalSpacingPx
    val distinctLefts = existingRects.map { it.left }.sorted().fold(mutableListOf<Float>()) { acc, l ->
      if (acc.isEmpty() || l - acc.last() > 2f) acc.add(l)
      acc
    }
    val colLefts = FloatArray(colCount) { idx ->
      distinctLefts.getOrElse(idx) { minLeft + idx * colStep }
    }
    val colBottoms = FloatArray(colCount) { sectionTop }
    for (note in newSectionNotes) {
      val h = cardBounds[note.id]?.height ?: continue
      var minCol = 0
      for (c in 1 until colCount) {
        if (colBottoms[c] < colBottoms[minCol]) {
          minCol = c
        }
      }
      val top = colBottoms[minCol]
      val left = colLefts[minCol]
      cardBounds[note.id] = Rect(
        left = left,
        top = top,
        right = left + colWidth,
        bottom = top + h,
      )
      colBottoms[minCol] = top + h + verticalSpacingPx
    }
  }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
  note: NoteItem,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  onTogglePin: () -> Unit,
  onToggleChecklistItem: (String) -> Unit,
  isDragging: Boolean = false,
  testTagOverride: String? = null,
  modifier: Modifier = Modifier,
) {
  val cardColor = resolveNoteCardColor(note.color, isElevatedSurface = isDragging)
  val borderColor = if (isSelected || isDragging) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  }
  val borderWidth = if (isSelected || isDragging) 2.dp else 1.dp
  val clickModifier = if (isDragging) {
    Modifier
  } else if (onLongClick != null) {
    Modifier.combinedClickable(
      onClick = onClick,
      onLongClick = onLongClick,
    )
  } else {
    Modifier.clickable(onClick = onClick)
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = cardColor),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 0.dp),
    border = BorderStroke(borderWidth, borderColor),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .then(clickModifier)
      .testTag(testTagOverride ?: "note_card_${note.id}"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Header row: Title + Pin/More actions (or Selection checkbox at top right)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (note.title.isNotBlank()) {
            Text(
              text = note.title,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }
        }

        if (isSelectionMode) {
          Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() },
            modifier = Modifier
              .size(22.dp)
              .testTag("note_card_checkbox_${note.id}"),
          )
        } else {
          val noteActions = LocalNoteCardActions.current
          var noteMenuExpanded by remember { mutableStateOf(false) }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            if (!note.isTrashed) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .clickable(onClick = onTogglePin)
                  .testTag("note_card_pin_${note.id}"),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                  contentDescription = if (note.isPinned) "Unpin note" else "Pin note",
                  tint = if (note.isPinned) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                  },
                  modifier = Modifier.size(16.dp),
                )
              }
            }

            Box {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .clickable { noteMenuExpanded = true }
                  .testTag("note_more_${note.id}"),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "More options for ${note.title.ifBlank { "note" }}",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  modifier = Modifier.size(16.dp),
                )
              }

              DropdownMenu(
                expanded = noteMenuExpanded,
                onDismissRequest = { noteMenuExpanded = false },
                modifier = Modifier.testTag("note_menu_${note.id}"),
              ) {
                if (note.isTrashed) {
                  DropdownMenuItem(
                    text = { Text("Restore") },
                    leadingIcon = { Icon(Icons.Default.RestoreFromTrash, contentDescription = null) },
                    onClick = {
                      noteMenuExpanded = false
                      noteActions.onRestoreFromTrash(note)
                    },
                  )
                  DropdownMenuItem(
                    text = { Text("Copy") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    onClick = {
                      noteMenuExpanded = false
                      noteActions.onCopyText(note)
                    },
                  )
                  HorizontalDivider()
                  DropdownMenuItem(
                    text = { Text("Delete forever", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                      Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                      )
                    },
                    onClick = {
                      noteMenuExpanded = false
                      noteActions.onDeleteOrTrash(note)
                    },
                  )
                } else {
                  DropdownMenuItem(
                    text = { Text("Copy") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    onClick = {
                      noteMenuExpanded = false
                      noteActions.onCopyText(note)
                    },
                  )
                  DropdownMenuItem(
                    text = { Text("Share") },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                    onClick = {
                      noteMenuExpanded = false
                      noteActions.onShare(note)
                    },
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
                      noteMenuExpanded = false
                      noteActions.onDeleteOrTrash(note)
                    },
                  )
                }
              }
            }
          }
        }
      }

      // Body preview: either checklist items or text content
      val remainingUncheckedCount = if (note.isChecklist) {
        val uncheckedCount = note.uncheckedItems.size
        if (uncheckedCount > 6) uncheckedCount - 5 else 0
      } else {
        0
      }

      if (note.isChecklist) {
        val unchecked = note.uncheckedItems
        val checked = note.checkedItems
        val previewLimit = if (unchecked.size > 6) 5 else 6
        val previewItems = unchecked.take(previewLimit)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          previewItems.forEach { item ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = (item.indentLevel.coerceAtLeast(0) * 14).dp, top = 1.dp, bottom = 1.dp)
                .testTag("note_card_check_row_${item.id}"),
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .clickable(enabled = !isSelectionMode && !note.isTrashed) {
                    onToggleChecklistItem(item.id)
                  }
                  .testTag("note_card_check_item_${item.id}"),
              ) {
                Icon(
                  imageVector = Icons.Default.CheckBoxOutlineBlank,
                  contentDescription = "Check ${item.text}",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp),
                )
              }
              Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          if (checked.isNotEmpty()) {
            val itemWord = if (checked.size == 1) "checked item" else "checked items"
            Text(
              text = "+ ${checked.size} $itemWord",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      } else if (note.content.isNotBlank()) {
        Text(
          text = note.content,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 8,
          overflow = TextOverflow.Ellipsis,
        )
      }

      // Labels & Timestamp metadata row
      if (note.labels.isNotEmpty() || note.isArchived) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          if (note.isArchived) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            ) {
              Text(
                text = "Archived",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
          note.labels.forEach { label ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
              border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
              ),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (remainingUncheckedCount > 0) {
          val moreWord = if (remainingUncheckedCount == 1) "more item" else "more items"
          Text(
            text = "$remainingUncheckedCount $moreWord",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f, fill = false)
              .padding(end = 8.dp)
              .testTag("note_card_more_items_${note.id}"),
          )
        } else {
          Spacer(modifier = Modifier.width(0.dp))
        }

        Text(
          text = NotesEngine.formatTimestamp(note.updatedAt),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
          modifier = Modifier.testTag("note_card_timestamp_${note.id}"),
        )
      }
    }
  }
}

@Composable
private fun NotesQuickCaptureBar(
  isAddNoteOpen: Boolean = false,
  isChecklistMode: Boolean = false,
  isPinned: Boolean = false,
  onTakeTextNote: () -> Unit,
  onTakePinnedNote: () -> Unit = {},
  onTakeChecklistNote: () -> Unit,
  onDragDownDelta: (Float) -> Unit = {},
  onDragDownEnd: () -> Unit = {},
  onDragDownCancel: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val currentOnDragDownDelta by rememberUpdatedState(onDragDownDelta)
  val currentOnDragDownEnd by rememberUpdatedState(onDragDownEnd)
  val currentOnDragDownCancel by rememberUpdatedState(onDragDownCancel)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .pointerInput(isAddNoteOpen) {
        if (!isAddNoteOpen) return@pointerInput
        detectVerticalDragGestures(
          onVerticalDrag = { change, dragAmount ->
            if (dragAmount > 0f) {
              change.consume()
              currentOnDragDownDelta(dragAmount)
            }
          },
          onDragEnd = { currentOnDragDownEnd() },
          onDragCancel = { currentOnDragDownCancel() },
        )
      },
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    AnimatedVisibility(visible = isAddNoteOpen) {
      Box(
        modifier = Modifier
          .padding(top = 8.dp)
          .width(36.dp)
          .height(4.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
          .testTag("add_note_drag_handle"),
      )
    }

    val hexagonEnabled = currentHexagonIconButtons()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier
          .weight(1f)
          .clip(if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(20.dp))
          .clickable(onClick = onTakeTextNote)
          .padding(vertical = 4.dp)
          .testTag("take_a_note_button"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
              if (isAddNoteOpen) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.primaryContainer
              },
            ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = if (isAddNoteOpen) {
              MaterialTheme.colorScheme.onPrimary
            } else {
              MaterialTheme.colorScheme.onPrimaryContainer
            },
            modifier = Modifier.size(18.dp),
          )
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .padding(vertical = 6.dp),
          contentAlignment = Alignment.CenterStart,
        ) {
          Text(
            text = "Take a note...",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
              if (isPinned) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                Color.Transparent
              },
            )
            .clickable(onClick = onTakePinnedNote)
            .testTag("new_pin_note_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
            contentDescription = if (isPinned) "Unpin note" else "Pin note",
            tint = if (isPinned) {
              MaterialTheme.colorScheme.onPrimaryContainer
            } else {
              MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(18.dp),
          )
        }

        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
              if (isChecklistMode) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                Color.Transparent
              },
            )
            .clickable(onClick = onTakeChecklistNote)
            .testTag("new_checklist_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.CheckBox,
            contentDescription = "New checklist note",
            tint = if (isChecklistMode) {
              MaterialTheme.colorScheme.onPrimaryContainer
            } else {
              MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun NotesEmptyState(
  activeFilter: NotesFilter,
  hasSearchQuery: Boolean,
) {
  val (icon, title, subtitle) = when {
    hasSearchQuery -> Triple(
      Icons.Default.Search,
      "No matching notes",
      "Try a different keyword or clear the color filter",
    )
    activeFilter == NotesFilter.Pinned -> Triple(
      Icons.Default.PushPin,
      "No pinned notes yet",
      "Pin important notes to keep them at the top of your board",
    )
    activeFilter == NotesFilter.Checklists -> Triple(
      Icons.Default.CheckBox,
      "No checklists yet",
      "Tap the checkbox icon below to start an interactive list",
    )
    activeFilter == NotesFilter.Archived -> Triple(
      Icons.Default.Archive,
      "Your archive is empty",
      "Archived notes are tucked away here while staying searchable",
    )
    activeFilter == NotesFilter.Trash -> Triple(
      Icons.Default.Delete,
      "No notes in Trash",
      "Deleted notes appear here before being permanently removed",
    )
    activeFilter is NotesFilter.Label -> Triple(
      Icons.AutoMirrored.Filled.Label,
      "No notes with ${activeFilter.name}",
      "Tap 'Take a note...' below to create a note with this label",
    )
    else -> Triple(
      Icons.Default.Lightbulb,
      "Notes you add appear here",
      "Capture quick thoughts, color-coded cards, and checklists offline",
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp)
      .testTag("notes_empty_state"),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp),
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/**
 * Panel that slides up inside the floating 28.dp bottom pill bar for creating a new note,
 * similar to the Todo tool's schedule panel, and autofocuses the title input.
 */
@Composable
private fun AddNoteSlideUpPanel(
  draft: NoteItem,
  onDraftChange: (NoteItem) -> Unit,
  availableLabels: List<String>,
  newlyCreatedLabels: List<String>,
  onNewlyCreatedLabelsChange: (List<String>) -> Unit,
  autoFocusTitle: Boolean,
  onSaveAndDismiss: () -> Unit,
  onDiscard: () -> Unit,
  onCopyToClipboard: (NoteItem) -> Unit,
  onDragDownDelta: (Float) -> Unit = {},
  onDragDownEnd: () -> Unit = {},
  onDragDownCancel: () -> Unit = {},
) {
  val scrollState = rememberScrollState()
  var isChecklistDragActive by remember { mutableStateOf(false) }
  val currentOnDragDownDelta by rememberUpdatedState(onDragDownDelta)
  val currentOnDragDownEnd by rememberUpdatedState(onDragDownEnd)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .pointerInput(Unit) {
        val touchSlop = viewConfiguration.touchSlop
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
          var totalDx = 0f
          var totalDy = 0f
          var isPanelDragging = false

          while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (change.changedToUpIgnoreConsumed()) {
              if (isPanelDragging) {
                change.consume()
              }
              break
            }
            val delta = change.positionChangeIgnoreConsumed()
            if (delta == Offset.Zero) continue

            if (isChecklistDragActive) {
              break
            }

            if (!isPanelDragging) {
              totalDx += delta.x
              totalDy += delta.y
              if (abs(totalDx) > touchSlop && abs(totalDx) > abs(totalDy)) {
                break
              }
              if (totalDy > touchSlop && totalDy > abs(totalDx) && scrollState.value == 0) {
                isPanelDragging = true
                change.consume()
                currentOnDragDownDelta(totalDy)
              }
            } else {
              change.consume()
              currentOnDragDownDelta(delta.y)
            }
          }

          if (isPanelDragging) {
            currentOnDragDownEnd()
          }
        }
      }
      .testTag("add_note_panel"),
  ) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    var addNoteViewportCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f, fill = false)
        .heightIn(max = 440.dp)
        .onGloballyPositioned { addNoteViewportCoordinates = it }
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      NoteEditorContent(
        draft = draft,
        onDraftChange = onDraftChange,
        autoFocusTitle = autoFocusTitle,
        minContentLines = 3,
        showInlineChecklistInput = true,
        scrollState = scrollState,
        viewportCoordinates = addNoteViewportCoordinates,
        onChecklistDragActiveChange = { isChecklistDragActive = it },
      )

      NoteColorPickerSection(
        selectedColor = draft.color,
        onSelectColor = { onDraftChange(draft.copy(color = it)) },
      )

      NoteLabelsSection(
        draft = draft,
        onDraftChange = onDraftChange,
        availableLabels = availableLabels,
        newlyCreatedLabels = newlyCreatedLabels,
        onNewlyCreatedLabelsChange = onNewlyCreatedLabelsChange,
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = onDiscard,
        modifier = Modifier
          .requiredSize(28.dp)
          .testTag("add_note_cancel_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Discard note",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.requiredSize(18.dp),
        )
      }

      FilledIconButton(
        onClick = onSaveAndDismiss,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
          .requiredSize(28.dp)
          .testTag("add_note_confirm_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Add note",
          modifier = Modifier.requiredSize(18.dp),
        )
      }
    }
  }
}

/**
 * Full-screen single note view for viewing and editing an existing note when tapped on the board,
 * containing all of the content of the old add a note panel.
 */
@Composable
private fun SingleNoteView(
  initialNote: NoteItem,
  availableLabels: List<String>,
  imeBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
  onAutoSave: (NoteItem, List<String>) -> Unit = { _, _ -> },
  onSaveAndDismiss: (NoteItem, List<String>) -> Unit,
  onMoveToTrash: (String) -> Unit,
  onDeletePermanently: (String) -> Unit,
  onRestoreFromTrash: (String) -> Unit,
  onCopyToClipboard: (NoteItem) -> Unit,
) {
  var draft by remember(initialNote.id) { mutableStateOf(initialNote) }
  var newlyCreatedLabels by remember(initialNote.id) { mutableStateOf(listOf<String>()) }
  var newChecklistItemText by remember(initialNote.id) { mutableStateOf("") }
  var focusContentAtEndAction by remember(initialNote.id) { mutableStateOf<(() -> Unit)?>(null) }
  var isColorSelectorOpen by remember(initialNote.id) { mutableStateOf(false) }
  var isLabelControlsOpen by remember(initialNote.id) { mutableStateOf(false) }
  val haptic = LocalHapticFeedback.current
  val checklistInputFocusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val currentDraft by rememberUpdatedState(draft)
  val currentNewlyCreatedLabels by rememberUpdatedState(newlyCreatedLabels)
  val currentOnAutoSave by rememberUpdatedState(onAutoSave)
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner, initialNote.id) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_PAUSE) {
        currentOnAutoSave(currentDraft, currentNewlyCreatedLabels)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
  val handleEmptySpaceTap: () -> Unit = {
    if (draft.isChecklist) {
      if (!draft.isTrashed) {
        runCatching { checklistInputFocusRequester.requestFocus() }
        keyboardController?.show()
      }
    } else {
      focusContentAtEndAction?.invoke()
    }
  }

  BackHandler {
    onSaveAndDismiss(draft, newlyCreatedLabels)
  }

  val backgroundColor = resolveNoteCardColor(draft.color, isElevatedSurface = false)
  val topBarColor = resolveNoteCardColor(draft.color, isElevatedSurface = true)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(backgroundColor)
      .testTag("single_note_view"),
  ) {
    ToolboxTopBar(
      title = "",
      subtext = null,
      icon = Icons.AutoMirrored.Filled.StickyNote2,
      containerColor = topBarColor,
      onNavigateBack = { onSaveAndDismiss(draft, newlyCreatedLabels) },
      backContentDescription = "Back to notes",
      backButtonTestTag = "single_note_back_button",
      actions = {
        if (draft.isTrashed) {
          IconButton(
            onClick = { onRestoreFromTrash(draft.id) },
            modifier = Modifier
              .size(36.dp)
              .testTag("editor_restore_button"),
          ) {
            Icon(
              imageVector = Icons.Default.RestoreFromTrash,
              contentDescription = "Restore note",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
          }
          IconButton(
            onClick = { onDeletePermanently(draft.id) },
            modifier = Modifier
              .size(36.dp)
              .testTag("editor_delete_forever_button"),
          ) {
            Icon(
              imageVector = Icons.Default.DeleteForever,
              contentDescription = "Delete forever",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(20.dp),
            )
          }
        } else {
          IconButton(
            onClick = {
              val newPinned = !draft.isPinned
              draft = draft.copy(
                isPinned = newPinned,
                isArchived = if (newPinned) false else draft.isArchived,
              )
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("editor_pin_button"),
          ) {
            Icon(
              imageVector = if (draft.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
              contentDescription = if (draft.isPinned) "Unpin note" else "Pin note",
              tint = if (draft.isPinned) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = { draft = NotesEngine.toggleChecklistMode(draft) },
            modifier = Modifier
              .size(36.dp)
              .testTag("editor_checklist_toggle"),
          ) {
            Icon(
              imageVector = Icons.Default.CheckBox,
              contentDescription = if (draft.isChecklist) {
                "Hide checkboxes"
              } else {
                "Show checkboxes"
              },
              tint = if (draft.isChecklist) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = {
              val newArchived = !draft.isArchived
              draft = draft.copy(
                isArchived = newArchived,
                isPinned = if (newArchived) false else draft.isPinned,
              )
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("editor_archive_button"),
          ) {
            Icon(
              imageVector = if (draft.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
              contentDescription = if (draft.isArchived) "Unarchive" else "Archive",
              tint = if (draft.isArchived) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = { onCopyToClipboard(draft) },
            modifier = Modifier
              .size(36.dp)
              .testTag("editor_copy_button"),
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy note text",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = { onMoveToTrash(draft.id) },
            modifier = Modifier
              .size(36.dp)
              .testTag("editor_delete_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Move to trash",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      },
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    val editorScrollState = rememberScrollState()
    var editorViewportCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val currentHandleEmptySpaceTap by rememberUpdatedState(handleEmptySpaceTap)
    var editorContentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .onGloballyPositioned { editorViewportCoordinates = it }
        .pointerInput(Unit) {
          val touchSlop = viewConfiguration.touchSlop
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = true)
            val contentCoords = editorContentCoordinates?.takeIf { it.isAttached }
            val viewportCoords = editorViewportCoordinates?.takeIf { it.isAttached }
            if (contentCoords != null && viewportCoords != null) {
              val contentBottomInViewport = viewportCoords.localBoundingBoxOf(contentCoords, clipBounds = false).bottom
              if (down.position.y <= contentBottomInViewport) {
                return@awaitEachGesture
              }
            }
            var isTap = true
            while (true) {
              val event = awaitPointerEvent(PointerEventPass.Main)
              val change = event.changes.firstOrNull { it.id == down.id } ?: break
              if (change.isConsumed || (change.position - down.position).getDistance() > touchSlop) {
                isTap = false
                break
              }
              if (change.changedToUpIgnoreConsumed()) {
                change.consume()
                break
              }
            }
            if (isTap) {
              currentHandleEmptySpaceTap()
            }
          }
        }
        .testTag("single_note_editor_empty_space"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(editorScrollState)
          .onGloballyPositioned { editorContentCoordinates = it }
          .padding(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 12.dp + imeBottomPadding,
          ),
      ) {
        NoteEditorContent(
          draft = draft,
          onDraftChange = { draft = it },
          autoFocusTitle = false,
          minContentLines = 1,
          showInlineChecklistInput = false,
          scrollState = editorScrollState,
          viewportCoordinates = editorViewportCoordinates,
          imeBottomPadding = imeBottomPadding,
          onProvideFocusContentAtEnd = { focusContentAtEndAction = it },
        )
      }
    }

    if (draft.isChecklist && !draft.isTrashed) {
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Surface(
        color = backgroundColor,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("single_note_bottom_checklist_input_bar"),
      ) {
        ChecklistItemInputRow(
          text = newChecklistItemText,
          onTextChange = { newChecklistItemText = it },
          onAddItem = { itemText ->
            val parsedItems = NotesEngine.parseChecklistItems(itemText)
            if (parsedItems.isNotEmpty()) {
              draft = draft.copy(
                checklistItems = draft.checklistItems + parsedItems,
              )
            }
            newChecklistItemText = ""
          },
          checkedSuggestions = NotesEngine.getCheckedItemSuggestions(draft, newChecklistItemText),
          onSelectCheckedSuggestion = { suggestedItem ->
            draft = NotesEngine.applyCheckedItemSuggestion(draft, suggestedItem.id)
            newChecklistItemText = ""
          },
          suggestionsAbove = true,
          inputFocusRequester = checklistInputFocusRequester,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        )
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    Surface(
      color = topBarColor,
      tonalElevation = 2.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("single_note_bottom_color_bar"),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(
          visible = isColorSelectorOpen,
          enter = slideInVertically(initialOffsetY = { it }) +
            expandVertically(expandFrom = Alignment.Bottom) +
            fadeIn(),
          exit = slideOutVertically(targetOffsetY = { it }) +
            shrinkVertically(shrinkTowards = Alignment.Bottom) +
            fadeOut(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("single_note_color_selector_panel"),
          ) {
            NoteColorPickerSection(
              selectedColor = draft.color,
              onSelectColor = { newColor ->
                val updated = draft.copy(color = newColor)
                draft = updated
                onAutoSave(updated, newlyCreatedLabels)
              },
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          }
        }

        AnimatedVisibility(
          visible = isLabelControlsOpen,
          enter = slideInVertically(initialOffsetY = { it }) +
            expandVertically(expandFrom = Alignment.Bottom) +
            fadeIn(),
          exit = slideOutVertically(targetOffsetY = { it }) +
            shrinkVertically(shrinkTowards = Alignment.Bottom) +
            fadeOut(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("single_note_label_controls_panel"),
          ) {
            NoteLabelsSection(
              draft = draft,
              onDraftChange = { draft = it },
              availableLabels = availableLabels,
              newlyCreatedLabels = newlyCreatedLabels,
              onNewlyCreatedLabelsChange = { newlyCreatedLabels = it },
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              val nextState = !isColorSelectorOpen
              isColorSelectorOpen = nextState
              if (nextState) {
                isLabelControlsOpen = false
              }
            },
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(
                if (isColorSelectorOpen) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  Color.Transparent
                },
              )
              .testTag("single_note_palette_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = if (isColorSelectorOpen) "Hide color selector" else "Show color selector",
              tint = if (isColorSelectorOpen) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(20.dp),
            )
          }

          Text(
            text = "Edited ${NotesEngine.formatTimestamp(draft.updatedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("single_note_timestamp"),
          )

          IconButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              val nextState = !isLabelControlsOpen
              isLabelControlsOpen = nextState
              if (nextState) {
                isColorSelectorOpen = false
              }
            },
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(
                if (isLabelControlsOpen) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  Color.Transparent
                },
              )
              .testTag("single_note_label_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Label,
              contentDescription = if (isLabelControlsOpen) "Hide label controls" else "Show label controls",
              tint = if (isLabelControlsOpen) {
                MaterialTheme.colorScheme.primary
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

@Composable
private fun ChecklistItemInputRow(
  text: String,
  onTextChange: (String) -> Unit,
  onAddItem: (String) -> Unit,
  checkedSuggestions: List<NoteChecklistItem> = emptyList(),
  onSelectCheckedSuggestion: (NoteChecklistItem) -> Unit = {},
  suggestionsAbove: Boolean = false,
  inputFocusRequester: FocusRequester = remember { FocusRequester() },
  modifier: Modifier = Modifier,
) {
  val trimmed = text.trim()
  val canAdd = trimmed.isNotEmpty()
  val submit = {
    if (canAdd) {
      onAddItem(text)
    }
  }
  val handleInputTextChange: (String) -> Unit = { updatedText ->
    if (updatedText.contains('\n') || updatedText.contains('\r')) {
      val parsed = NotesEngine.parseChecklistItems(updatedText)
      if (parsed.isNotEmpty()) {
        onAddItem(updatedText)
      } else {
        onTextChange("")
      }
    } else {
      onTextChange(updatedText)
    }
  }
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    if (suggestionsAbove && checkedSuggestions.isNotEmpty()) {
      CheckedChecklistSuggestionsList(
        suggestions = checkedSuggestions,
        onSelectSuggestion = onSelectCheckedSuggestion,
      )
    }

    val hexagonEnabled = currentHexagonIconButtons()
    Surface(
      shape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("editor_checklist_input_card"),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier
            .weight(1f)
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(
                if (canAdd) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.primaryContainer
                },
              )
              .clickable {
                if (canAdd) {
                  submit()
                } else {
                  inputFocusRequester.requestFocus()
                }
              }
              .testTag("editor_add_checklist_item"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add list item",
              tint = if (canAdd) {
                MaterialTheme.colorScheme.onPrimary
              } else {
                MaterialTheme.colorScheme.onPrimaryContainer
              },
              modifier = Modifier.size(18.dp),
            )
          }

          BasicTextField(
            value = text,
            onValueChange = handleInputTextChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            decorationBox = { innerTextField ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                contentAlignment = Alignment.CenterStart,
              ) {
                if (text.isEmpty()) {
                  Text(
                    text = "List item...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                innerTextField()
              }
            },
            modifier = Modifier
              .weight(1f)
              .focusRequester(inputFocusRequester)
              .testTag("editor_new_checklist_input"),
          )
        }

        if (text.isNotEmpty()) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .clickable { onTextChange("") },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Clear list item input",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    }

    if (!suggestionsAbove && checkedSuggestions.isNotEmpty()) {
      CheckedChecklistSuggestionsList(
        suggestions = checkedSuggestions,
        onSelectSuggestion = onSelectCheckedSuggestion,
      )
    }
  }
}

@Composable
private fun CheckedChecklistSuggestionsList(
  suggestions: List<NoteChecklistItem>,
  onSelectSuggestion: (NoteChecklistItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (suggestions.isEmpty()) return
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
    tonalElevation = 3.dp,
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("editor_checked_suggestions"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    ) {
      suggestions.forEach { item ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectSuggestion(item) }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("editor_checked_suggestion_${item.id}"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector = Icons.Default.CheckBox,
            contentDescription = "Restore checked item ${item.text}",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun NoteColorPickerSection(
  selectedColor: NoteColor,
  onSelectColor: (NoteColor) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(
      text = "Card Color (${selectedColor.displayName})",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      NoteColor.entries.forEach { colorOption ->
        val isSelected = selectedColor == colorOption
        val swatch = resolveNoteSwatchColor(colorOption)
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(swatch)
            .border(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.outlineVariant
              },
              shape = CircleShape,
            )
            .clickable { onSelectColor(colorOption) }
            .testTag("editor_color_${colorOption.name}"),
          contentAlignment = Alignment.Center,
        ) {
          if (isSelected) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "${colorOption.displayName} selected",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(15.dp),
            )
          } else if (colorOption == NoteColor.DEFAULT) {
            Icon(
              imageVector = Icons.Default.FormatColorReset,
              contentDescription = colorOption.displayName,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NoteEditorContent(
  draft: NoteItem,
  onDraftChange: (NoteItem) -> Unit,
  autoFocusTitle: Boolean,
  minContentLines: Int = 5,
  showInlineChecklistInput: Boolean = true,
  scrollState: ScrollState? = null,
  viewportCoordinates: LayoutCoordinates? = null,
  imeBottomPadding: Dp = 0.dp,
  onChecklistDragActiveChange: (Boolean) -> Unit = {},
  onProvideFocusContentAtEnd: ((() -> Unit) -> Unit)? = null,
) {
  var newChecklistItemText by remember(draft.id) { mutableStateOf("") }
  var isCheckedSectionExpanded by remember(draft.id) { mutableStateOf(true) }

  // Drag-and-drop reorder state for checklist items
  val density = LocalDensity.current
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current
  val itemSpacingPx = with(density) { 2.dp.toPx() }
  val defaultItemHeightPx = with(density) { 36.dp.toPx() }
  val maxOverscrollPx = with(density) { 4.dp.toPx() }
  val autoScrollEdgeThresholdPx = with(density) { 56.dp.toPx() }
  val maxAutoScrollStepPx = with(density) { 14.dp.toPx() }
  val imeBottomPaddingPx = with(density) { imeBottomPadding.toPx() }
  var editorCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  var draggedChecklistItemId by remember(draft.id) { mutableStateOf<String?>(null) }
  var isChecklistHandlePressed by remember(draft.id) { mutableStateOf(false) }
  var checklistDragOffsetY by remember(draft.id) { mutableFloatStateOf(0f) }
  var checklistDragPointerRootY by remember(draft.id) { mutableStateOf<Float?>(null) }
  var checklistTopEdgeTriggerY by remember(draft.id) { mutableStateOf<Float?>(null) }
  var checklistBottomEdgeTriggerY by remember(draft.id) { mutableStateOf<Float?>(null) }
  var checklistAutoScrollSpeedPx by remember(draft.id) { mutableFloatStateOf(0f) }
  val checklistItemHeightsPx = remember(draft.id) { mutableStateMapOf<String, Float>() }
  val currentDraft by rememberUpdatedState(draft)
  val currentOnDraftChange by rememberUpdatedState(onDraftChange)
  val currentOnChecklistDragActiveChange by rememberUpdatedState(onChecklistDragActiveChange)

  LaunchedEffect(draggedChecklistItemId, isChecklistHandlePressed) {
    currentOnChecklistDragActiveChange(draggedChecklistItemId != null || isChecklistHandlePressed)
  }

  fun resolveVisibleChecklistViewportBounds(): Pair<Float, Float>? {
    val activeCoords = viewportCoordinates?.takeIf { it.isAttached }
      ?: editorCoordinates?.takeIf { it.isAttached }
      ?: return null
    val bounds = activeCoords.boundsInRoot()
    val rootHeight = activeCoords.findRootCoordinates().size.height.toFloat()
    val imeCutoffY = if (imeBottomPaddingPx > 0f && rootHeight > 0f) {
      rootHeight - imeBottomPaddingPx
    } else {
      Float.MAX_VALUE
    }
    val visibleTop = bounds.top
    val visibleBottom = minOf(bounds.bottom, imeCutoffY)
    return if (visibleBottom > visibleTop) visibleTop to visibleBottom else null
  }

  fun updateChecklistAutoScrollSpeed(pointerRootY: Float?, isPointerMove: Boolean) {
    val activeScrollState = scrollState
    val activeItemId = draggedChecklistItemId
    if (activeScrollState == null || activeItemId == null || pointerRootY == null) {
      checklistAutoScrollSpeedPx = 0f
      return
    }
    val (visibleTop, visibleBottom) = resolveVisibleChecklistViewportBounds() ?: run {
      checklistAutoScrollSpeedPx = 0f
      return
    }
    val liveDraft = currentDraft
    val item = liveDraft.checklistItems.firstOrNull { it.id == activeItemId } ?: run {
      checklistAutoScrollSpeedPx = 0f
      return
    }
    val sectionItems = if (item.isChecked) liveDraft.checkedItems else liveDraft.uncheckedItems
    val sectionIndex = sectionItems.indexOfFirst { it.id == activeItemId }
    val canMoveBackward = sectionIndex > 0
    val canMoveForward = sectionIndex in 0 until sectionItems.lastIndex

    val defaultTopTrigger = visibleTop + autoScrollEdgeThresholdPx
    val defaultBottomTrigger = visibleBottom - autoScrollEdgeThresholdPx
    val topTrigger = checklistTopEdgeTriggerY ?: defaultTopTrigger
    val bottomTrigger = checklistBottomEdgeTriggerY ?: defaultBottomTrigger

    checklistAutoScrollSpeedPx = TodoListEngine.calculateDragAutoScrollSpeed(
      pointerY = pointerRootY,
      viewportTop = visibleTop,
      viewportBottom = visibleBottom,
      edgeThresholdPx = autoScrollEdgeThresholdPx,
      maxScrollStepPx = maxAutoScrollStepPx,
      canScrollBackward = activeScrollState.canScrollBackward,
      canScrollForward = activeScrollState.canScrollForward,
      canMoveItemBackward = canMoveBackward,
      canMoveItemForward = canMoveForward,
      topEdgeTriggerY = topTrigger,
      bottomEdgeTriggerY = bottomTrigger,
    )

    if (isPointerMove) {
      checklistTopEdgeTriggerY = minOf(defaultTopTrigger, maxOf(topTrigger, pointerRootY))
      checklistBottomEdgeTriggerY = maxOf(defaultBottomTrigger, minOf(bottomTrigger, pointerRootY))
    }
  }

  fun startDraggingChecklistItem(itemId: String, startRootY: Float? = null) {
    focusManager.clearFocus()
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    draggedChecklistItemId = itemId
    checklistDragOffsetY = 0f
    checklistDragPointerRootY = startRootY
    checklistAutoScrollSpeedPx = 0f
    val viewportBounds = resolveVisibleChecklistViewportBounds()
    if (viewportBounds != null && startRootY != null) {
      val (visibleTop, visibleBottom) = viewportBounds
      checklistTopEdgeTriggerY = minOf(startRootY, visibleTop + autoScrollEdgeThresholdPx)
      checklistBottomEdgeTriggerY = maxOf(startRootY, visibleBottom - autoScrollEdgeThresholdPx)
    } else {
      checklistTopEdgeTriggerY = null
      checklistBottomEdgeTriggerY = null
    }
  }

  fun onDragChecklistItemDelta(itemId: String, deltaY: Float, pointerRootY: Float? = null) {
    if (draggedChecklistItemId != itemId) return
    if (pointerRootY != null) {
      checklistDragPointerRootY = pointerRootY
    }
    val liveDraft = currentDraft
    val item = liveDraft.checklistItems.firstOrNull { it.id == itemId } ?: return
    val sectionItems = if (item.isChecked) liveDraft.checkedItems else liveDraft.uncheckedItems

    val stepResult = TodoListEngine.calculateDragReorder(
      items = sectionItems,
      draggedId = itemId,
      idSelector = { it.id },
      currentOffsetY = checklistDragOffsetY + deltaY,
      itemHeightsPx = checklistItemHeightsPx,
      defaultItemHeightPx = defaultItemHeightPx,
      itemSpacingPx = itemSpacingPx,
      maxOverscrollPx = maxOverscrollPx,
    )

    checklistDragOffsetY = stepResult.remainingOffsetY
    if (stepResult.moved) {
      val updatedList = if (item.isChecked) {
        liveDraft.uncheckedItems + stepResult.items
      } else {
        stepResult.items + liveDraft.checkedItems
      }
      currentOnDraftChange(liveDraft.copy(checklistItems = updatedList))
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    updateChecklistAutoScrollSpeed(
      pointerRootY = pointerRootY ?: checklistDragPointerRootY,
      isPointerMove = pointerRootY != null,
    )
  }

  fun endDraggingChecklistItem() {
    draggedChecklistItemId = null
    checklistDragOffsetY = 0f
    checklistDragPointerRootY = null
    checklistTopEdgeTriggerY = null
    checklistBottomEdgeTriggerY = null
    checklistAutoScrollSpeedPx = 0f
  }

  LaunchedEffect(draggedChecklistItemId, checklistAutoScrollSpeedPx, scrollState) {
    val activeScrollState = scrollState ?: return@LaunchedEffect
    val activeItemId = draggedChecklistItemId ?: return@LaunchedEffect
    if (checklistAutoScrollSpeedPx == 0f) return@LaunchedEffect
    while (isActive && draggedChecklistItemId == activeItemId && checklistAutoScrollSpeedPx != 0f) {
      val canScroll = if (checklistAutoScrollSpeedPx < 0f) {
        activeScrollState.canScrollBackward
      } else {
        activeScrollState.canScrollForward
      }
      if (!canScroll) {
        checklistAutoScrollSpeedPx = 0f
        break
      }
      val consumed = activeScrollState.scrollBy(checklistAutoScrollSpeedPx)
      if (consumed != 0f) {
        onDragChecklistItemDelta(activeItemId, consumed, pointerRootY = null)
      } else {
        checklistAutoScrollSpeedPx = 0f
        break
      }
      delay(16L)
    }
  }

  val titleFocusRequester = remember { FocusRequester() }
  val contentFocusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  var contentFieldValue by remember(draft.id) {
    mutableStateOf(
      TextFieldValue(
        text = draft.content,
        selection = TextRange.Zero,
      ),
    )
  }
  if (contentFieldValue.text != draft.content) {
    val start = contentFieldValue.selection.start.coerceIn(0, draft.content.length)
    val end = contentFieldValue.selection.end.coerceIn(0, draft.content.length)
    contentFieldValue = contentFieldValue.copy(
      text = draft.content,
      selection = TextRange(start, end),
    )
  }
  val focusContentAtEnd = remember(draft.id, contentFocusRequester, keyboardController) {
    {
      val liveDraft = currentDraft
      if (!liveDraft.isChecklist) {
        contentFieldValue = TextFieldValue(
          text = liveDraft.content,
          selection = TextRange(liveDraft.content.length),
        )
        runCatching { contentFocusRequester.requestFocus() }
        keyboardController?.show()
      }
    }
  }
  SideEffect {
    onProvideFocusContentAtEnd?.invoke(focusContentAtEnd)
  }

  LaunchedEffect(autoFocusTitle, draft.id) {
    if (autoFocusTitle) {
      runCatching { titleFocusRequester.requestFocus() }
      delay(80)
      runCatching { titleFocusRequester.requestFocus() }
      keyboardController?.show()
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .onGloballyPositioned { editorCoordinates = it },
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    // Title Field (borderless / no outline)
    BasicTextField(
      value = draft.title,
      onValueChange = { onDraftChange(draft.copy(title = it)) },
      textStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      ),
      singleLine = true,
      cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
      decorationBox = { innerTextField ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        ) {
          if (draft.title.isEmpty()) {
            Text(
              text = "Title",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
          }
          innerTextField()
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(titleFocusRequester)
        .testTag("editor_title_input"),
    )

    // Body Content or Checklist Items
    if (draft.isChecklist) {
      val uncheckedItems = draft.uncheckedItems
      val checkedItems = draft.checkedItems

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        uncheckedItems.forEach { item ->
          key(item.id) {
            val isDraggingThis = draggedChecklistItemId == item.id
            ChecklistEditorRow(
              item = item,
              isBeingDragged = isDraggingThis,
              dragOffsetY = if (isDraggingThis) checklistDragOffsetY else 0f,
              onDragHandlePressedChange = { isChecklistHandlePressed = it },
              onStartDrag = { startRootY -> startDraggingChecklistItem(item.id, startRootY) },
              onDrag = { deltaY, pointerRootY -> onDragChecklistItemDelta(item.id, deltaY, pointerRootY) },
              onEndDrag = { endDraggingChecklistItem() },
              onHeightMeasured = { heightPx ->
                if (heightPx > 0f) {
                  checklistItemHeightsPx[item.id] = heightPx
                }
              },
              onToggleChecked = {
                onDraftChange(
                  draft.copy(
                    checklistItems = draft.checklistItems.map {
                      if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it
                    },
                  ),
                )
              },
              onTextChange = { updatedText ->
                onDraftChange(NotesEngine.updateChecklistItemText(draft, item.id, updatedText))
              },
              onDelete = {
                onDraftChange(
                  draft.copy(
                    checklistItems = draft.checklistItems.filterNot { it.id == item.id },
                  ),
                )
              },
              checkedSuggestions = NotesEngine.getCheckedItemSuggestions(draft, item.text),
              onSelectCheckedSuggestion = { suggestedItem ->
                onDraftChange(
                  NotesEngine.applyCheckedItemSuggestion(
                    note = draft,
                    checkedItemId = suggestedItem.id,
                    replacingUncheckedItemId = item.id,
                  ),
                )
              },
            )
          }
        }

        // Add new checklist item row (shown inline in add-note view; pinned at bottom in full edit view)
        if (showInlineChecklistInput) {
          ChecklistItemInputRow(
            text = newChecklistItemText,
            onTextChange = { newChecklistItemText = it },
            onAddItem = { itemText ->
              val parsedItems = NotesEngine.parseChecklistItems(itemText)
              if (parsedItems.isNotEmpty()) {
                onDraftChange(
                  draft.copy(
                    checklistItems = draft.checklistItems + parsedItems,
                  ),
                )
              }
              newChecklistItemText = ""
            },
            checkedSuggestions = NotesEngine.getCheckedItemSuggestions(draft, newChecklistItemText),
            onSelectCheckedSuggestion = { suggestedItem ->
              onDraftChange(NotesEngine.applyCheckedItemSuggestion(draft, suggestedItem.id))
              newChecklistItemText = ""
            },
          )
        }

        // Checked items section
        if (checkedItems.isNotEmpty()) {
          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 2.dp),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { isCheckedSectionExpanded = !isCheckedSectionExpanded }
                .padding(vertical = 2.dp),
            ) {
              Icon(
                imageVector = if (isCheckedSectionExpanded) {
                  Icons.Default.ExpandLess
                } else {
                  Icons.Default.ExpandMore
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${checkedItems.size} checked",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              TextButton(
                onClick = { onDraftChange(NotesEngine.uncheckAllItems(draft)) },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier
                  .height(28.dp)
                  .testTag("editor_uncheck_all_button"),
              ) {
                Text("Uncheck all", style = MaterialTheme.typography.labelMedium)
              }
              TextButton(
                onClick = { onDraftChange(NotesEngine.deleteCheckedItems(draft)) },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier
                  .height(28.dp)
                  .testTag("editor_delete_checked_button"),
              ) {
                Text(
                  text = "Delete checked",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.error,
                )
              }
            }
          }

          if (isCheckedSectionExpanded) {
            checkedItems.forEach { item ->
              key(item.id) {
                val isDraggingThis = draggedChecklistItemId == item.id
                ChecklistEditorRow(
                  item = item,
                  isBeingDragged = isDraggingThis,
                  dragOffsetY = if (isDraggingThis) checklistDragOffsetY else 0f,
                  onDragHandlePressedChange = { isChecklistHandlePressed = it },
                  onStartDrag = { startRootY -> startDraggingChecklistItem(item.id, startRootY) },
                  onDrag = { deltaY, pointerRootY -> onDragChecklistItemDelta(item.id, deltaY, pointerRootY) },
                  onEndDrag = { endDraggingChecklistItem() },
                  onHeightMeasured = { heightPx ->
                    if (heightPx > 0f) {
                      checklistItemHeightsPx[item.id] = heightPx
                    }
                  },
                  onToggleChecked = {
                    onDraftChange(
                      draft.copy(
                        checklistItems = draft.checklistItems.map {
                          if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it
                        },
                      ),
                    )
                  },
                  onTextChange = { updatedText ->
                    onDraftChange(NotesEngine.updateChecklistItemText(draft, item.id, updatedText))
                  },
                  onDelete = {
                    onDraftChange(
                      draft.copy(
                        checklistItems = draft.checklistItems.filterNot { it.id == item.id },
                      ),
                    )
                  },
                )
              }
            }
          }
        }
      }
    } else {
      BasicTextField(
        value = contentFieldValue,
        onValueChange = { updatedValue ->
          contentFieldValue = updatedValue
          if (updatedValue.text != draft.content) {
            onDraftChange(draft.copy(content = updatedValue.text))
          }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
          color = MaterialTheme.colorScheme.onSurface,
        ),
        minLines = minContentLines,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
          ) {
            if (contentFieldValue.text.isEmpty()) {
              Text(
                text = "Take a note...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              )
            }
            innerTextField()
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(contentFocusRequester)
          .testTag("editor_content_input"),
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteLabelsSection(
  draft: NoteItem,
  onDraftChange: (NoteItem) -> Unit,
  availableLabels: List<String>,
  newlyCreatedLabels: List<String>,
  onNewlyCreatedLabelsChange: (List<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  var newLabelInput by remember(draft.id) { mutableStateOf("") }
  var showLabelPicker by remember(draft.id) { mutableStateOf(false) }
  val keyboardController = LocalSoftwareKeyboardController.current

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Labels",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      TextButton(
        onClick = { showLabelPicker = !showLabelPicker },
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
        modifier = Modifier
          .height(28.dp)
          .testTag("editor_toggle_label_picker"),
      ) {
        Icon(
          imageVector = Icons.Default.NewLabel,
          contentDescription = null,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (showLabelPicker) "Hide label input" else "Add label",
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }

    val combinedLabels = (availableLabels + newlyCreatedLabels + draft.labels)
      .map { NotesEngine.normalizeLabelName(it) }
      .filter { it.isNotEmpty() }
      .distinct()

    if (combinedLabels.isNotEmpty()) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        combinedLabels.forEach { label ->
          val isAttached = draft.labels.any { it.equals(label, ignoreCase = true) }
          FilterChip(
            selected = isAttached,
            onClick = {
              onDraftChange(
                if (isAttached) {
                  draft.copy(labels = draft.labels.filterNot { it.equals(label, ignoreCase = true) })
                } else {
                  draft.copy(labels = draft.labels + label)
                },
              )
            },
            label = { Text(label) },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("editor_label_chip_$label"),
          )
        }
      }
    }

    AnimatedVisibility(visible = showLabelPicker) {
      val newLabelFocusRequester = remember { FocusRequester() }
      val trimmedInlineLabel = NotesEngine.normalizeLabelName(newLabelInput)
      val canAttachInlineLabel = trimmedInlineLabel.isNotEmpty()
      val attachInlineLabel = {
        if (canAttachInlineLabel) {
          if (draft.labels.none { it.equals(trimmedInlineLabel, ignoreCase = true) }) {
            onDraftChange(draft.copy(labels = draft.labels + trimmedInlineLabel))
          }
          if (newlyCreatedLabels.none { it.equals(trimmedInlineLabel, ignoreCase = true) }) {
            onNewlyCreatedLabelsChange(newlyCreatedLabels + trimmedInlineLabel)
          }
          newLabelInput = ""
        }
      }
      val hexagonEnabled = currentHexagonIconButtons()
      Surface(
        shape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        border = BorderStroke(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("editor_new_label_input_card"),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier
              .weight(1f)
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                  if (canAttachInlineLabel) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.primaryContainer
                  },
                )
                .clickable {
                  if (canAttachInlineLabel) {
                    attachInlineLabel()
                  } else {
                    newLabelFocusRequester.requestFocus()
                  }
                }
                .testTag("editor_add_label_confirm"),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Attach label",
                tint = if (canAttachInlineLabel) {
                  MaterialTheme.colorScheme.onPrimary
                } else {
                  MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.size(18.dp),
              )
            }

            BasicTextField(
              value = newLabelInput,
              onValueChange = { newLabelInput = it },
              textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
              ),
              cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
              singleLine = true,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(onDone = { attachInlineLabel() }),
              decorationBox = { innerTextField ->
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                  contentAlignment = Alignment.CenterStart,
                ) {
                  if (newLabelInput.isEmpty()) {
                    Text(
                      text = "New label name...",
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.Medium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  innerTextField()
                }
              },
              modifier = Modifier
                .weight(1f)
                .focusRequester(newLabelFocusRequester)
                .testTag("editor_new_label_input"),
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            if (newLabelInput.isNotEmpty()) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .clickable { newLabelInput = "" },
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear label input",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
            Box(
              modifier = Modifier.size(28.dp),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                  .size(18.dp)
                  .testTag("editor_new_label_icon"),
              )
            }
          }
        }
      }
      LaunchedEffect(Unit) {
        try {
          newLabelFocusRequester.requestFocus()
          keyboardController?.show()
        } catch (_: Exception) {}
      }
    }
  }
}

@Composable
private fun ChecklistEditorRow(
  item: NoteChecklistItem,
  isBeingDragged: Boolean = false,
  dragOffsetY: Float = 0f,
  onDragHandlePressedChange: (Boolean) -> Unit = {},
  onStartDrag: (Float?) -> Unit = {},
  onDrag: (Float, Float?) -> Unit = { _, _ -> },
  onEndDrag: () -> Unit = {},
  onHeightMeasured: (Float) -> Unit = {},
  onToggleChecked: () -> Unit,
  onTextChange: (String) -> Unit,
  onDelete: () -> Unit,
  checkedSuggestions: List<NoteChecklistItem> = emptyList(),
  onSelectCheckedSuggestion: (NoteChecklistItem) -> Unit = {},
) {
  val currentOnDragHandlePressedChange by rememberUpdatedState(onDragHandlePressedChange)
  val currentOnStartDrag by rememberUpdatedState(onStartDrag)
  val currentOnDrag by rememberUpdatedState(onDrag)
  val currentOnEndDrag by rememberUpdatedState(onEndDrag)
  var isInputFocused by remember(item.id) { mutableStateOf(false) }
  var hasTypedWhileFocused by remember(item.id) { mutableStateOf(false) }
  var dragHandleCoords by remember(item.id) { mutableStateOf<LayoutCoordinates?>(null) }
  var lastDownLocalOffset by remember(item.id) { mutableStateOf<Offset?>(null) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Surface(
      shape = CircleShape,
      color = if (isBeingDragged) {
        MaterialTheme.colorScheme.surfaceContainerHigh
      } else {
        Color.Transparent
      },
      tonalElevation = if (isBeingDragged) 4.dp else 0.dp,
      border = if (isBeingDragged) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
      } else {
        null
      },
      modifier = Modifier
        .fillMaxWidth()
        .zIndex(if (isBeingDragged) 2f else 0f)
        .graphicsLayer {
          translationY = if (isBeingDragged) dragOffsetY else 0f
        }
        .onGloballyPositioned { coords ->
          val height = coords.size.height.toFloat()
          if (height > 0f) {
            onHeightMeasured(height)
          }
        }
        .testTag("editor_checklist_row_${item.id}"),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            start = (item.indentLevel.coerceAtLeast(0) * 16).dp,
            end = 0.dp,
            top = 0.dp,
            bottom = 0.dp,
          ),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(width = 20.dp, height = 28.dp)
            .clip(CircleShape)
            .onGloballyPositioned { dragHandleCoords = it }
            .pointerInput(item.id) {
              awaitPointerEventScope {
                while (true) {
                  val event = awaitPointerEvent(PointerEventPass.Initial)
                  val firstNewDown = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                  if (firstNewDown != null) {
                    lastDownLocalOffset = firstNewDown.position
                  }
                  currentOnDragHandlePressedChange(event.changes.any { it.pressed })
                }
              }
            }
            .pointerInput(item.id) {
              var draggingThis = false
              try {
                detectDragGestures(
                  onDragStart = { startOffset ->
                    draggingThis = true
                    val initialLocalOffset = lastDownLocalOffset ?: startOffset
                    val startRootY = dragHandleCoords
                      ?.takeIf { it.isAttached }
                      ?.localToRoot(initialLocalOffset)
                      ?.y
                    currentOnStartDrag(startRootY)
                  },
                  onDrag = { change, dragAmount ->
                    change.consume()
                    val pointerRootY = dragHandleCoords
                      ?.takeIf { it.isAttached }
                      ?.localToRoot(change.position)
                      ?.y
                    currentOnDrag(dragAmount.y, pointerRootY)
                  },
                  onDragEnd = {
                    draggingThis = false
                    lastDownLocalOffset = null
                    currentOnDragHandlePressedChange(false)
                    currentOnEndDrag()
                  },
                  onDragCancel = {
                    draggingThis = false
                    lastDownLocalOffset = null
                    currentOnDragHandlePressedChange(false)
                    currentOnEndDrag()
                  },
                )
              } finally {
                if (draggingThis) {
                  lastDownLocalOffset = null
                  currentOnDragHandlePressedChange(false)
                  currentOnEndDrag()
                }
              }
            }
            .testTag("editor_checklist_drag_${item.id}"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Drag to reorder item",
            tint = if (isBeingDragged) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            },
            modifier = Modifier.size(16.dp),
          )
        }

        Checkbox(
          checked = item.isChecked,
          onCheckedChange = { onToggleChecked() },
          modifier = Modifier
            .size(28.dp)
            .testTag("editor_checkbox_${item.id}"),
        )
        BasicTextField(
          value = item.text,
          onValueChange = { updated ->
            hasTypedWhileFocused = true
            onTextChange(updated)
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = if (item.isChecked) {
              MaterialTheme.colorScheme.onSurfaceVariant
            } else {
              MaterialTheme.colorScheme.onSurface
            },
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
          ),
          cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
          modifier = Modifier
            .weight(1f)
            .onFocusChanged { focusState ->
              isInputFocused = focusState.isFocused
              if (!focusState.isFocused) {
                hasTypedWhileFocused = false
              }
            }
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .testTag("editor_checklist_text_${item.id}"),
        )
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(24.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove item",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }

    if (!item.isChecked && isInputFocused && hasTypedWhileFocused && checkedSuggestions.isNotEmpty()) {
      CheckedChecklistSuggestionsList(
        suggestions = checkedSuggestions,
        onSelectSuggestion = { suggestedItem ->
          hasTypedWhileFocused = false
          onSelectCheckedSuggestion(suggestedItem)
        },
        modifier = Modifier.padding(start = 28.dp, end = 0.dp, top = 2.dp, bottom = 4.dp),
      )
    }
  }
}

@Composable
private fun ManageLabelsDialog(
  labels: List<String>,
  notesCountByLabel: Map<String, Int>,
  onAddLabel: (String) -> Unit,
  onDeleteLabel: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var labelText by remember { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }
  val trimmed = NotesEngine.normalizeLabelName(labelText)
  val isDuplicate = labels.any { it.equals(trimmed, ignoreCase = true) }
  val canAdd = trimmed.isNotEmpty() && !isDuplicate
  val totalTaggedNotes = notesCountByLabel.values.sum()

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Exception) {}
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("manage_labels_dialog"),
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
            imageVector = Icons.Default.NewLabel,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Add & Manage Labels",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Create or remove tags to organize your notes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Context & Live Tag Preview Card
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                  if (isDuplicate) {
                    MaterialTheme.colorScheme.errorContainer
                  } else {
                    MaterialTheme.colorScheme.primaryContainer
                  }
                ),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = null,
                tint = if (isDuplicate) {
                  MaterialTheme.colorScheme.error
                } else {
                  MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(18.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (trimmed.isNotEmpty()) {
                  trimmed
                } else {
                  "${labels.size} Active ${if (labels.size == 1) "Label" else "Labels"}"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = when {
                  isDuplicate -> "Label already exists on your board"
                  canAdd -> "New tag preview • Ready to add"
                  else -> "$totalTaggedNotes tagged note ${if (totalTaggedNotes == 1) "assignment" else "assignments"} across your board"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isDuplicate) {
                  MaterialTheme.colorScheme.error
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Full-width Input Field with integrated Clear & Quick-Add actions
        OutlinedTextField(
          value = labelText,
          onValueChange = { labelText = it },
          label = { Text("Label name") },
          placeholder = { Text("e.g. ideas, work, recipes") },
          singleLine = true,
          isError = isDuplicate,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Label,
              contentDescription = null,
              tint = if (isDuplicate) {
                MaterialTheme.colorScheme.error
              } else {
                MaterialTheme.colorScheme.primary
              },
            )
          },
          trailingIcon = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp),
              modifier = Modifier.padding(end = 6.dp),
            ) {
              if (labelText.isNotEmpty()) {
                IconButton(
                  onClick = { labelText = "" },
                  modifier = Modifier.size(32.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
              IconButton(
                onClick = {
                  if (canAdd) {
                    onAddLabel(trimmed)
                    labelText = ""
                  }
                },
                enabled = canAdd,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("manage_labels_add_button"),
              ) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                      if (canAdd) {
                        MaterialTheme.colorScheme.primaryContainer
                      } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                      }
                    ),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add label",
                    tint = if (canAdd) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    },
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
            }
          },
          supportingText = if (isDuplicate) {
            { Text("A label with this name already exists") }
          } else {
            null
          },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              if (canAdd) {
                onAddLabel(trimmed)
                labelText = ""
              }
            },
          ),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("manage_labels_input"),
        )

        // Existing Board Labels Section
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "BOARD LABELS (${labels.size})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )

          if (labels.isEmpty()) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
              border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
              ),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = "No labels yet. Type a name above to create your first note tag.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
              )
            }
          } else {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              labels.forEach { label ->
                val count = notesCountByLabel[label] ?: 0
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                  border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                  ),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                      Text(
                        text = "$count ${if (count == 1) "note" else "notes"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    IconButton(
                      onClick = { onDeleteLabel(label) },
                      modifier = Modifier
                        .size(34.dp)
                        .testTag("delete_label_$label"),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete label $label",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                      )
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
          if (canAdd) {
            onAddLabel(trimmed)
          }
          onDismiss()
        },
        enabled = trimmed.isEmpty() || canAdd,
        shape = CircleShape,
        modifier = Modifier.testTag("manage_labels_done_button"),
      ) {
        Icon(
          imageVector = if (canAdd) Icons.Default.Add else Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (canAdd) "Add & Done" else "Done")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier.testTag("manage_labels_cancel_button"),
      ) {
        Text("Cancel")
      }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BulkNoteColorDialog(
  selectedCount: Int,
  onSelectColor: (NoteColor) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("bulk_color_dialog"),
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
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Choose Card Color",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Apply color to $selectedCount selected ${if (selectedCount == 1) "note" else "notes"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        NoteColor.entries.forEach { colorOption ->
          val swatch = resolveNoteSwatchColor(colorOption)
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onSelectColor(colorOption) }
              .padding(6.dp)
              .testTag("bulk_color_option_${colorOption.name}"),
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(swatch)
                .border(
                  width = 1.dp,
                  color = MaterialTheme.colorScheme.outlineVariant,
                  shape = CircleShape,
                ),
              contentAlignment = Alignment.Center,
            ) {
              if (colorOption == NoteColor.DEFAULT) {
                Icon(
                  imageVector = Icons.Default.FormatColorReset,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
            Text(
              text = colorOption.displayName,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    confirmButton = {},
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
private fun EmptyTrashConfirmDialog(
  trashedCount: Int,
  onConfirmEmpty: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("empty_trash_dialog"),
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
            imageVector = Icons.Default.DeleteForever,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Empty Trash?",
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
        Text(
          text = "Permanently delete $trashedCount ${if (trashedCount == 1) "note" else "notes"} from Trash?",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(14.dp),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirmEmpty,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("confirm_empty_trash_button"),
      ) {
        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Delete Forever")
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
private fun DeleteLabelConfirmDialog(
  label: String,
  matchingNotes: List<NoteItem>,
  onConfirmDelete: () -> Unit,
  onDismiss: () -> Unit,
) {
  val noteCount = matchingNotes.size
  val noteWord = if (noteCount == 1) "note" else "notes"

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("delete_label_confirm_dialog"),
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
            text = "Delete Label?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "\"$label\" • Used by $noteCount $noteWord",
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
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "Remove the label \"$label\" from $noteCount $noteWord? Your notes themselves will not be deleted.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )

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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "$noteCount tagged $noteWord",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirmDelete,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("confirm_delete_label_button"),
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
        modifier = Modifier.testTag("cancel_delete_label_button"),
      ) {
        Text("Cancel")
      }
    },
  )
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
  val sampleNotes = listOf(
    NoteItem(
      id = "1",
      title = "Weekend Camping Gear",
      isChecklist = true,
      checklistItems = listOf(
        NoteChecklistItem(id = "c1", text = "2-person tent & footprint", isChecked = false),
        NoteChecklistItem(id = "c2", text = "Headlamp + spare AAA batteries", isChecked = false),
        NoteChecklistItem(id = "c3", text = "Cast iron skillet", isChecked = true),
      ),
      color = NoteColor.SAGE,
      isPinned = true,
      labels = listOf("outdoors", "packing"),
    ),
    NoteItem(
      id = "2",
      title = "Book Recommendations",
      content = "• Project Hail Mary\n• The Pragmatic Programmer\n• Designing Data-Intensive Applications",
      color = NoteColor.SAND,
      isPinned = true,
      labels = listOf("reading"),
    ),
    NoteItem(
      id = "3",
      title = "Sourdough Starter Ratio",
      content = "1:2:2 feeding before overnight levain:\n25g starter, 50g warm water, 50g unbleached bread flour.",
      color = NoteColor.PEACH,
      isPinned = false,
      labels = listOf("recipes"),
    ),
    NoteItem(
      id = "4",
      title = "Synth Patch Ideas",
      content = "Detuned sawtooth lead with slow LFO on low-pass filter cutoff.",
      color = NoteColor.DUSK,
      isPinned = false,
      labels = listOf("music"),
    ),
  )
  ToolboxTheme {
    NotesScreen(
      repository = InMemoryNotesRepository(
        initialNotes = sampleNotes,
        initialLabels = listOf("outdoors", "packing", "reading", "recipes", "music"),
      ),
    )
  }
}
