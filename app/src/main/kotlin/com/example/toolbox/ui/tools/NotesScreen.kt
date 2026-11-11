package com.example.toolbox.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NewLabel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme

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

private fun noteColorHue(noteColor: NoteColor): Float = when (noteColor) {
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NotesScreen(
  modifier: Modifier = Modifier,
  repository: NotesRepository? = null,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  val repo = remember(repository) {
    repository ?: SharedPreferencesNotesRepository(context)
  }

  var notes by remember { mutableStateOf(repo.loadNotes()) }
  var savedLabels by remember { mutableStateOf(repo.loadLabels()) }
  var isGridView by remember { mutableStateOf(repo.isGridView()) }

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

  // Editor state
  var editingNote by remember { mutableStateOf<NoteItem?>(null) }

  // Dialog states
  var showManageLabelsDialog by remember { mutableStateOf(false) }
  var showBulkColorDialog by remember { mutableStateOf(false) }
  var showEmptyTrashDialog by remember { mutableStateOf(false) }

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
  }

  val allLabels = remember(notes, savedLabels) {
    NotesEngine.extractAllLabels(notes, savedLabels)
  }

  // If the currently selected label filter was deleted, reset to All
  LaunchedEffect(allLabels, activeFilter) {
    val current = activeFilter
    if (current is NotesFilter.Label && allLabels.none { it.equals(current.name, ignoreCase = true) }) {
      activeFilter = NotesFilter.All
    }
  }

  val filteredResult = remember(notes, activeFilter, searchQuery, selectedColorFilter) {
    NotesEngine.filterNotes(
      notes = notes,
      filter = activeFilter,
      searchQuery = if (isSearchActive) searchQuery else "",
      colorFilter = selectedColorFilter,
    )
  }

  val activeNotesCount = remember(notes) { notes.count { !it.isArchived && !it.isTrashed } }
  val pinnedNotesCount = remember(notes) { notes.count { it.isPinned && !it.isArchived && !it.isTrashed } }
  val archivedNotesCount = remember(notes) { notes.count { it.isArchived && !it.isTrashed } }
  val trashedNotesCount = remember(notes) { notes.count { it.isTrashed } }

  val headerSubtext = remember(
    activeFilter,
    activeNotesCount,
    pinnedNotesCount,
    archivedNotesCount,
    trashedNotesCount,
    filteredResult.totalCount,
  ) {
    when (val filter = activeFilter) {
      NotesFilter.All -> {
        val noteWord = if (activeNotesCount == 1) "note" else "notes"
        if (pinnedNotesCount > 0) {
          "$activeNotesCount $noteWord • $pinnedNotesCount pinned"
        } else {
          "$activeNotesCount $noteWord • Offline board"
        }
      }
      NotesFilter.Pinned -> "$pinnedNotesCount pinned"
      NotesFilter.Checklists -> "${filteredResult.totalCount} checklists"
      is NotesFilter.Label -> "#${filter.name} • ${filteredResult.totalCount}"
      NotesFilter.Archived -> "Archive • $archivedNotesCount"
      NotesFilter.Trash -> "Trash • $trashedNotesCount"
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("notes_screen"),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
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
          isGridView = isGridView,
          canUndo = canUndo,
          canRedo = canRedo,
          onToggleSearch = {
            isSearchActive = !isSearchActive
            if (!isSearchActive) {
              searchQuery = ""
              selectedColorFilter = null
            }
          },
          onToggleViewMode = {
            val nextMode = !isGridView
            isGridView = nextMode
            repo.setGridView(nextMode)
          },
          onOpenLabelsDialog = { showManageLabelsDialog = true },
          onUndo = ::undo,
          onRedo = ::redo,
        )
      }

      // 2. Expandable Search & Color Filter Bar
      AnimatedVisibility(
        visible = isSearchActive && !isSelectionMode,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
      ) {
        NotesSearchAndColorFilterBar(
          searchQuery = searchQuery,
          onSearchQueryChange = { searchQuery = it },
          selectedColor = selectedColorFilter,
          onSelectColor = { color ->
            selectedColorFilter = if (selectedColorFilter == color) null else color
          },
          onCloseSearch = {
            isSearchActive = false
            searchQuery = ""
            selectedColorFilter = null
          },
        )
      }

      // 3. Category & Label Filter Chips Row
      NotesFilterChipsRow(
        activeFilter = activeFilter,
        labels = allLabels,
        archivedCount = archivedNotesCount,
        trashedCount = trashedNotesCount,
        onSelectFilter = { filter ->
          activeFilter = filter
          selectedNoteIds = emptySet()
        },
        onManageLabels = { showManageLabelsDialog = true },
      )

      // Trash Banner when inside Trash filter
      if (activeFilter == NotesFilter.Trash && trashedNotesCount > 0) {
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

      // 4. Staggered Grid / List of Notes
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      ) {
        if (filteredResult.isEmpty) {
          NotesEmptyState(
            activeFilter = activeFilter,
            hasSearchQuery = isSearchActive && (searchQuery.isNotBlank() || selectedColorFilter != null),
          )
        } else {
          val columns = if (isGridView) 2 else 1
          val showSectionHeaders = activeFilter !is NotesFilter.Pinned &&
            activeFilter !is NotesFilter.Archived &&
            activeFilter !is NotesFilter.Trash &&
            filteredResult.pinned.isNotEmpty() &&
            filteredResult.others.isNotEmpty()

          LazyVerticalStaggeredGrid(
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
              .testTag("notes_staggered_grid"),
          ) {
            if (filteredResult.pinned.isNotEmpty()) {
              if (showSectionHeaders || activeFilter == NotesFilter.All) {
                item(
                  key = "section_header_pinned",
                  span = StaggeredGridItemSpan.FullLine,
                ) {
                  NotesSectionHeader(
                    title = "PINNED",
                    modifier = Modifier.testTag("notes_section_pinned"),
                  )
                }
              }

              items(
                items = filteredResult.pinned,
                key = { "pinned_${it.id}" },
              ) { note ->
                NoteCard(
                  note = note,
                  isSelected = note.id in selectedNoteIds,
                  isSelectionMode = isSelectionMode,
                  onClick = {
                    if (isSelectionMode) {
                      selectedNoteIds = if (note.id in selectedNoteIds) {
                        selectedNoteIds - note.id
                      } else {
                        selectedNoteIds + note.id
                      }
                    } else {
                      editingNote = note
                    }
                  },
                  onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedNoteIds = if (note.id in selectedNoteIds) {
                      selectedNoteIds - note.id
                    } else {
                      selectedNoteIds + note.id
                    }
                  },
                  onTogglePin = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    applyStateChange(NotesEngine.togglePin(notes, note.id))
                  },
                  onToggleChecklistItem = { itemId ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    applyStateChange(NotesEngine.toggleChecklistItem(notes, note.id, itemId))
                  },
                )
              }
            }

            if (filteredResult.others.isNotEmpty()) {
              if (showSectionHeaders) {
                item(
                  key = "section_header_others",
                  span = StaggeredGridItemSpan.FullLine,
                ) {
                  NotesSectionHeader(
                    title = "OTHERS",
                    modifier = Modifier
                      .padding(top = 6.dp)
                      .testTag("notes_section_others"),
                  )
                }
              }

              items(
                items = filteredResult.others,
                key = { "other_${it.id}" },
              ) { note ->
                NoteCard(
                  note = note,
                  isSelected = note.id in selectedNoteIds,
                  isSelectionMode = isSelectionMode,
                  onClick = {
                    if (isSelectionMode) {
                      selectedNoteIds = if (note.id in selectedNoteIds) {
                        selectedNoteIds - note.id
                      } else {
                        selectedNoteIds + note.id
                      }
                    } else {
                      editingNote = note
                    }
                  },
                  onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedNoteIds = if (note.id in selectedNoteIds) {
                      selectedNoteIds - note.id
                    } else {
                      selectedNoteIds + note.id
                    }
                  },
                  onTogglePin = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    applyStateChange(NotesEngine.togglePin(notes, note.id))
                  },
                  onToggleChecklistItem = { itemId ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    applyStateChange(NotesEngine.toggleChecklistItem(notes, note.id, itemId))
                  },
                )
              }
            }
          }
        }

        // 5. Bottom Google Keep-inspired Quick-Capture Bar ("Take a note..." + Checklist shortcut)
        if (!isSelectionMode && activeFilter != NotesFilter.Trash) {
          val preselectedLabels = when (val filter = activeFilter) {
            is NotesFilter.Label -> listOf(filter.name)
            else -> emptyList()
          }
          val defaultPinned = activeFilter == NotesFilter.Pinned

          NotesQuickCaptureBar(
            onTakeTextNote = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              editingNote = NoteItem(
                isChecklist = false,
                isPinned = defaultPinned,
                labels = preselectedLabels,
              )
            },
            onTakeChecklistNote = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              editingNote = NoteItem(
                isChecklist = true,
                checklistItems = emptyList(),
                isPinned = defaultPinned,
                labels = preselectedLabels,
              )
            },
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(horizontal = 14.dp, vertical = 12.dp),
          )
        }
      }
    }
  }

  // Note Editor Sheet
  val currentDraft = editingNote
  if (currentDraft != null) {
    NoteEditorSheet(
      initialNote = currentDraft,
      availableLabels = allLabels,
      onSaveAndDismiss = { updatedDraft, newlyCreatedLabels ->
        val updatedNotes = NotesEngine.saveOrDiscardNote(notes, updatedDraft)
        val mergedLabels = (savedLabels + newlyCreatedLabels)
          .map { it.trim() }
          .filter { it.isNotEmpty() }
          .distinct()
        applyStateChange(updatedNotes, mergedLabels)
        editingNote = null
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
      onDuplicateNote = { updatedDraft ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val withSaved = NotesEngine.saveOrDiscardNote(notes, updatedDraft)
        val duplicated = NotesEngine.duplicateNote(withSaved, updatedDraft.id)
        applyStateChange(duplicated)
        editingNote = null
        Toast.makeText(context, "Note duplicated", Toast.LENGTH_SHORT).show()
      },
      onCopyToClipboard = { draftToCopy ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        copyNoteToClipboard(context, draftToCopy)
      },
    )
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
        val (updatedNotes, updatedLabels) = NotesEngine.deleteLabelGlobally(
          notes = notes,
          savedLabels = savedLabels,
          labelToRemove = labelToDelete,
        )
        applyStateChange(updatedNotes, updatedLabels)
      },
      onDismiss = { showManageLabelsDialog = false },
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

@Composable
private fun NotesHeaderTopBar(
  subtext: String,
  isSearchActive: Boolean,
  isGridView: Boolean,
  canUndo: Boolean,
  canRedo: Boolean,
  onToggleSearch: () -> Unit,
  onToggleViewMode: () -> Unit,
  onOpenLabelsDialog: () -> Unit,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
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
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.StickyNote2,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Notes",
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
              modifier = Modifier.testTag("notes_header_subtext"),
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
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

          IconButton(
            onClick = onToggleSearch,
            modifier = Modifier.testTag("notes_search_toggle"),
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search notes",
              tint = if (isSearchActive) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
            )
          }

          IconButton(
            onClick = onToggleViewMode,
            modifier = Modifier.testTag("notes_view_mode_toggle"),
          ) {
            Icon(
              imageVector = if (isGridView) {
                Icons.Default.ViewAgenda
              } else {
                Icons.Default.GridView
              },
              contentDescription = if (isGridView) "Switch to list view" else "Switch to grid view",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          IconButton(
            onClick = onOpenLabelsDialog,
            modifier = Modifier.testTag("notes_manage_labels_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Label,
              contentDescription = "Manage labels",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
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
        .heightIn(min = 68.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          IconButton(
            onClick = onClearSelection,
            modifier = Modifier.testTag("notes_clear_selection"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Exit selection",
            )
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable(onClick = onToggleSelectAll)
              .padding(end = 8.dp)
              .testTag("notes_select_all_toggle"),
          ) {
            Checkbox(
              checked = totalVisibleCount > 0 && selectedCount == totalVisibleCount,
              onCheckedChange = { onToggleSelectAll() },
            )
            Text(
              text = "$selectedCount selected",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isTrashView) {
            IconButton(
              onClick = onRestoreSelected,
              modifier = Modifier.testTag("notes_bulk_restore"),
            ) {
              Icon(
                imageVector = Icons.Default.RestoreFromTrash,
                contentDescription = "Restore selected notes",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          } else {
            IconButton(
              onClick = onTogglePinSelected,
              modifier = Modifier.testTag("notes_bulk_pin"),
            ) {
              Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pin or unpin selected",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            IconButton(
              onClick = onOpenColorPicker,
              modifier = Modifier.testTag("notes_bulk_color"),
            ) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Change color",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            IconButton(
              onClick = onToggleArchiveSelected,
              modifier = Modifier.testTag("notes_bulk_archive"),
            ) {
              Icon(
                imageVector = if (isArchiveView) Icons.Default.Unarchive else Icons.Default.Archive,
                contentDescription = if (isArchiveView) "Unarchive selected" else "Archive selected",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          IconButton(
            onClick = onDeleteSelected,
            enabled = selectedCount > 0,
            modifier = Modifier.testTag("notes_bulk_delete"),
          ) {
            Icon(
              imageVector = if (isTrashView) Icons.Default.DeleteForever else Icons.Default.Delete,
              contentDescription = if (isTrashView) "Delete forever" else "Move to trash",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NotesSearchAndColorFilterBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedColor: NoteColor?,
  onSelectColor: (NoteColor) -> Unit,
  onCloseSearch: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Search titles, notes, checklists, or #labels") },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        },
        trailingIcon = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { onSearchQueryChange("") },
                modifier = Modifier.testTag("notes_search_clear"),
              ) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear search text",
                )
              }
            }
            IconButton(
              onClick = onCloseSearch,
              modifier = Modifier.testTag("notes_search_close"),
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close search",
              )
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("notes_search_input"),
      )

      // Color filter strip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
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
}

@Composable
private fun NotesFilterChipsRow(
  activeFilter: NotesFilter,
  labels: List<String>,
  archivedCount: Int,
  trashedCount: Int,
  onSelectFilter: (NotesFilter) -> Unit,
  onManageLabels: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .testTag("notes_filter_chips_row"),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FilterChip(
      selected = activeFilter == NotesFilter.All,
      onClick = { onSelectFilter(NotesFilter.All) },
      label = { Text("All") },
      shape = RoundedCornerShape(20.dp),
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
      ),
      modifier = Modifier.testTag("notes_filter_all"),
    )

    FilterChip(
      selected = activeFilter == NotesFilter.Pinned,
      onClick = { onSelectFilter(NotesFilter.Pinned) },
      label = { Text("Pinned") },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.PushPin,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      },
      shape = RoundedCornerShape(20.dp),
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
      ),
      modifier = Modifier.testTag("notes_filter_pinned"),
    )

    FilterChip(
      selected = activeFilter == NotesFilter.Checklists,
      onClick = { onSelectFilter(NotesFilter.Checklists) },
      label = { Text("Checklists") },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.CheckBox,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      },
      shape = RoundedCornerShape(20.dp),
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
      ),
      modifier = Modifier.testTag("notes_filter_checklists"),
    )

    labels.forEach { label ->
      val isSelected = activeFilter is NotesFilter.Label &&
        activeFilter.name.equals(label, ignoreCase = true)
      FilterChip(
        selected = isSelected,
        onClick = { onSelectFilter(NotesFilter.Label(label)) },
        label = { Text("#$label") },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = Modifier.testTag("notes_filter_label_$label"),
      )
    }

    FilterChip(
      selected = activeFilter == NotesFilter.Archived,
      onClick = { onSelectFilter(NotesFilter.Archived) },
      label = {
        Text(if (archivedCount > 0) "Archive ($archivedCount)" else "Archive")
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Archive,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      },
      shape = RoundedCornerShape(20.dp),
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
      ),
      modifier = Modifier.testTag("notes_filter_archived"),
    )

    FilterChip(
      selected = activeFilter == NotesFilter.Trash,
      onClick = { onSelectFilter(NotesFilter.Trash) },
      label = {
        Text(if (trashedCount > 0) "Trash ($trashedCount)" else "Trash")
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      },
      shape = RoundedCornerShape(20.dp),
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
      ),
      modifier = Modifier.testTag("notes_filter_trash"),
    )

    FilterChip(
      selected = false,
      onClick = onManageLabels,
      label = { Text("+ Label") },
      shape = RoundedCornerShape(20.dp),
      modifier = Modifier.testTag("notes_add_label_chip"),
    )
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
  note: NoteItem,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onTogglePin: () -> Unit,
  onToggleChecklistItem: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val cardColor = resolveNoteCardColor(note.color)
  val borderColor = if (isSelected) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  }
  val borderWidth = if (isSelected) 2.dp else 1.dp

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = cardColor),
    border = BorderStroke(borderWidth, borderColor),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      )
      .testTag("note_card_${note.id}"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Header row: Selection checkbox + Title + Pin indicator/toggle
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
          if (isSelectionMode) {
            Checkbox(
              checked = isSelected,
              onCheckedChange = { onClick() },
              modifier = Modifier.size(22.dp),
            )
          }
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

        if (!note.isTrashed && !isSelectionMode) {
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
      }

      // Body preview: either checklist items or text content
      if (note.isChecklist) {
        val unchecked = note.uncheckedItems
        val checked = note.checkedItems
        val previewItems = unchecked.take(6)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          previewItems.forEach { item ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable(enabled = !isSelectionMode && !note.isTrashed) {
                  onToggleChecklistItem(item.id)
                }
                .padding(vertical = 1.dp)
                .testTag("note_card_check_item_${item.id}"),
            ) {
              Icon(
                imageVector = Icons.Default.CheckBoxOutlineBlank,
                contentDescription = "Check ${item.text}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          val remainingUnchecked = unchecked.size - previewItems.size
          if (remainingUnchecked > 0) {
            Text(
              text = "+ $remainingUnchecked more unchecked",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                text = "#$label",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
        }
      }

      Text(
        text = NotesEngine.formatTimestamp(note.updatedAt),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
      )
    }
  }
}

@Composable
private fun NotesQuickCaptureBar(
  onTakeTextNote: () -> Unit,
  onTakeChecklistNote: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
    modifier = modifier.fillMaxWidth(),
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
          .clip(RoundedCornerShape(20.dp))
          .clickable(onClick = onTakeTextNote)
          .padding(vertical = 10.dp)
          .testTag("take_a_note_button"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(18.dp),
          )
        }
        Text(
          text = "Take a note...",
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      IconButton(
        onClick = onTakeChecklistNote,
        modifier = Modifier.testTag("new_checklist_button"),
      ) {
        Icon(
          imageVector = Icons.Default.CheckBox,
          contentDescription = "New checklist note",
          tint = MaterialTheme.colorScheme.primary,
        )
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
      "No notes with #${activeFilter.name}",
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NoteEditorSheet(
  initialNote: NoteItem,
  availableLabels: List<String>,
  onSaveAndDismiss: (NoteItem, List<String>) -> Unit,
  onMoveToTrash: (String) -> Unit,
  onDeletePermanently: (String) -> Unit,
  onRestoreFromTrash: (String) -> Unit,
  onDuplicateNote: (NoteItem) -> Unit,
  onCopyToClipboard: (NoteItem) -> Unit,
) {
  var draft by remember(initialNote.id) { mutableStateOf(initialNote) }
  var newlyCreatedLabels by remember(initialNote.id) { mutableStateOf(listOf<String>()) }
  var newChecklistItemText by remember { mutableStateOf("") }
  var newLabelInput by remember { mutableStateOf("") }
  var showLabelPicker by remember { mutableStateOf(false) }
  var isCheckedSectionExpanded by remember { mutableStateOf(true) }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val sheetBackgroundColor = resolveNoteCardColor(draft.color, isElevatedSurface = true)

  ModalBottomSheet(
    onDismissRequest = { onSaveAndDismiss(draft, newlyCreatedLabels) },
    sheetState = sheetState,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor = sheetBackgroundColor,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("note_editor_sheet"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp)
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Editor Top Toolbar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Button(
          onClick = { onSaveAndDismiss(draft, newlyCreatedLabels) },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("editor_save_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Done")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (draft.isTrashed) {
            IconButton(
              onClick = { onRestoreFromTrash(draft.id) },
              modifier = Modifier.testTag("editor_restore_button"),
            ) {
              Icon(
                imageVector = Icons.Default.RestoreFromTrash,
                contentDescription = "Restore note",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
            IconButton(
              onClick = { onDeletePermanently(draft.id) },
              modifier = Modifier.testTag("editor_delete_forever_button"),
            ) {
              Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = "Delete forever",
                tint = MaterialTheme.colorScheme.error,
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
              modifier = Modifier.testTag("editor_pin_button"),
            ) {
              Icon(
                imageVector = if (draft.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (draft.isPinned) "Unpin note" else "Pin note",
                tint = if (draft.isPinned) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
              )
            }

            IconButton(
              onClick = { draft = NotesEngine.toggleChecklistMode(draft) },
              modifier = Modifier.testTag("editor_checklist_toggle"),
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
              modifier = Modifier.testTag("editor_archive_button"),
            ) {
              Icon(
                imageVector = if (draft.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                contentDescription = if (draft.isArchived) "Unarchive" else "Archive",
                tint = if (draft.isArchived) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
              )
            }

            IconButton(
              onClick = { onCopyToClipboard(draft) },
              modifier = Modifier.testTag("editor_copy_button"),
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy note text",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            IconButton(
              onClick = { onDuplicateNote(draft) },
              modifier = Modifier.testTag("editor_duplicate_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                contentDescription = "Duplicate note",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            IconButton(
              onClick = { onMoveToTrash(draft.id) },
              modifier = Modifier.testTag("editor_delete_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Move to trash",
                tint = MaterialTheme.colorScheme.error,
              )
            }
          }
        }
      }

      // Title Field
      OutlinedTextField(
        value = draft.title,
        onValueChange = { draft = draft.copy(title = it) },
        placeholder = {
          Text(
            text = "Title",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        textStyle = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        ),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("editor_title_input"),
      )

      // Body Content or Checklist Items
      if (draft.isChecklist) {
        val uncheckedItems = draft.uncheckedItems
        val checkedItems = draft.checkedItems

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          uncheckedItems.forEach { item ->
            ChecklistEditorRow(
              item = item,
              onToggleChecked = {
                draft = draft.copy(
                  checklistItems = draft.checklistItems.map {
                    if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it
                  },
                )
              },
              onTextChange = { updatedText ->
                draft = draft.copy(
                  checklistItems = draft.checklistItems.map {
                    if (it.id == item.id) it.copy(text = updatedText) else it
                  },
                )
              },
              onDelete = {
                draft = draft.copy(
                  checklistItems = draft.checklistItems.filterNot { it.id == item.id },
                )
              },
            )
          }

          // Add new checklist item row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = newChecklistItemText,
              onValueChange = { newChecklistItemText = it },
              placeholder = { Text("List item...") },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                )
              },
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(
                onDone = {
                  val trimmed = newChecklistItemText.trim()
                  if (trimmed.isNotEmpty()) {
                    draft = draft.copy(
                      checklistItems = draft.checklistItems + NoteChecklistItem(text = trimmed),
                    )
                    newChecklistItemText = ""
                  }
                },
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("editor_new_checklist_input"),
            )

            Button(
              onClick = {
                val trimmed = newChecklistItemText.trim()
                if (trimmed.isNotEmpty()) {
                  draft = draft.copy(
                    checklistItems = draft.checklistItems + NoteChecklistItem(text = trimmed),
                  )
                  newChecklistItemText = ""
                }
              },
              enabled = newChecklistItemText.isNotBlank(),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("editor_add_checklist_item"),
            ) {
              Text("Add")
            }
          }

          // Checked items section
          if (checkedItems.isNotEmpty()) {
            HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              modifier = Modifier.padding(vertical = 4.dp),
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { isCheckedSectionExpanded = !isCheckedSectionExpanded }
                  .padding(vertical = 4.dp, horizontal = 4.dp),
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
                  onClick = { draft = NotesEngine.uncheckAllItems(draft) },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("editor_uncheck_all_button"),
                ) {
                  Text("Uncheck all")
                }
                TextButton(
                  onClick = { draft = NotesEngine.deleteCheckedItems(draft) },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("editor_delete_checked_button"),
                ) {
                  Text(
                    text = "Delete checked",
                    color = MaterialTheme.colorScheme.error,
                  )
                }
              }
            }

            if (isCheckedSectionExpanded) {
              checkedItems.forEach { item ->
                ChecklistEditorRow(
                  item = item,
                  onToggleChecked = {
                    draft = draft.copy(
                      checklistItems = draft.checklistItems.map {
                        if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it
                      },
                    )
                  },
                  onTextChange = { updatedText ->
                    draft = draft.copy(
                      checklistItems = draft.checklistItems.map {
                        if (it.id == item.id) it.copy(text = updatedText) else it
                      },
                    )
                  },
                  onDelete = {
                    draft = draft.copy(
                      checklistItems = draft.checklistItems.filterNot { it.id == item.id },
                    )
                  },
                )
              }
            }
          }
        }
      } else {
        OutlinedTextField(
          value = draft.content,
          onValueChange = { draft = draft.copy(content = it) },
          placeholder = { Text("Take a note...") },
          minLines = 5,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_content_input"),
        )
      }

      // Color Picker Strip
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = "Card Color (${draft.color.displayName})",
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
            val isSelected = draft.color == colorOption
            val swatch = resolveNoteSwatchColor(colorOption)
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(swatch)
                .border(
                  width = if (isSelected) 2.5.dp else 1.dp,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.outlineVariant
                  },
                  shape = CircleShape,
                )
                .clickable { draft = draft.copy(color = colorOption) }
                .testTag("editor_color_${colorOption.name}"),
              contentAlignment = Alignment.Center,
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "${colorOption.displayName} selected",
                  tint = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.size(18.dp),
                )
              } else if (colorOption == NoteColor.DEFAULT) {
                Icon(
                  imageVector = Icons.Default.FormatColorReset,
                  contentDescription = colorOption.displayName,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }

      // Labels assignment section
      Column(
        modifier = Modifier.fillMaxWidth(),
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
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("editor_toggle_label_picker"),
          ) {
            Icon(
              imageVector = Icons.Default.NewLabel,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (showLabelPicker) "Hide label input" else "Add label")
          }
        }

        val combinedLabels = (availableLabels + newlyCreatedLabels + draft.labels)
          .map { it.trim() }
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
                  draft = if (isAttached) {
                    draft.copy(labels = draft.labels.filterNot { it.equals(label, ignoreCase = true) })
                  } else {
                    draft.copy(labels = draft.labels + label)
                  }
                },
                label = { Text("#$label") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("editor_label_chip_$label"),
              )
            }
          }
        }

        AnimatedVisibility(visible = showLabelPicker) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = newLabelInput,
              onValueChange = { newLabelInput = it },
              placeholder = { Text("New label name...") },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("editor_new_label_input"),
            )
            Button(
              onClick = {
                val trimmed = newLabelInput.trim().removePrefix("#").trim()
                if (trimmed.isNotEmpty()) {
                  if (draft.labels.none { it.equals(trimmed, ignoreCase = true) }) {
                    draft = draft.copy(labels = draft.labels + trimmed)
                  }
                  if (newlyCreatedLabels.none { it.equals(trimmed, ignoreCase = true) }) {
                    newlyCreatedLabels = newlyCreatedLabels + trimmed
                  }
                  newLabelInput = ""
                }
              },
              enabled = newLabelInput.trim().removePrefix("#").isNotBlank(),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("editor_add_label_confirm"),
            ) {
              Text("Attach")
            }
          }
        }
      }

      // Edited timestamp footer
      Text(
        text = "Edited ${NotesEngine.formatTimestamp(draft.updatedAt)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun ChecklistEditorRow(
  item: NoteChecklistItem,
  onToggleChecked: () -> Unit,
  onTextChange: (String) -> Unit,
  onDelete: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        checked = item.isChecked,
        onCheckedChange = { onToggleChecked() },
        modifier = Modifier.testTag("editor_checkbox_${item.id}"),
      )
      BasicTextField(
        value = item.text,
        onValueChange = onTextChange,
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
          .padding(vertical = 8.dp),
      )
      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(32.dp),
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
  val trimmed = labelText.trim().removePrefix("#").trim()
  val isDuplicate = labels.any { it.equals(trimmed, ignoreCase = true) }
  val canAdd = trimmed.isNotEmpty() && !isDuplicate

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
            imageVector = Icons.AutoMirrored.Filled.Label,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Manage Labels",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Create or remove offline note tags",
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
              imageVector = Icons.Default.NewLabel,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Text(
              text = "${labels.size} active ${if (labels.size == 1) "label" else "labels"} across your board",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = labelText,
            onValueChange = { labelText = it },
            label = { Text("New label") },
            singleLine = true,
            isError = isDuplicate,
            shape = RoundedCornerShape(14.dp),
            leadingIcon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = null,
              )
            },
            trailingIcon = {
              if (labelText.isNotEmpty()) {
                IconButton(onClick = { labelText = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
              }
            },
            supportingText = if (isDuplicate) {
              { Text("Label already exists") }
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
              .weight(1f)
              .focusRequester(focusRequester)
              .testTag("manage_labels_input"),
          )

          Button(
            onClick = {
              if (canAdd) {
                onAddLabel(trimmed)
                labelText = ""
              }
            },
            enabled = canAdd,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("manage_labels_add_button"),
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          }
        }

        if (labels.isNotEmpty()) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 200.dp)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            labels.forEach { label ->
              val count = notesCountByLabel[label] ?: 0
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "#$label",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                      text = "$count ${if (count == 1) "note" else "notes"}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  IconButton(
                    onClick = { onDeleteLabel(label) },
                    modifier = Modifier.testTag("delete_label_$label"),
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
    },
    confirmButton = {
      Button(
        onClick = {
          if (canAdd) {
            onAddLabel(trimmed)
          }
          onDismiss()
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("manage_labels_done_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Done")
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
        shape = RoundedCornerShape(12.dp),
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
        shape = RoundedCornerShape(12.dp),
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
        shape = RoundedCornerShape(12.dp),
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
