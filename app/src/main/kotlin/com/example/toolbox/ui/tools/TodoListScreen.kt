package com.example.toolbox.ui.tools

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
  modifier: Modifier = Modifier,
  repository: TodoListRepository? = null,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  val repo = remember(repository) {
    repository ?: SharedPreferencesTodoListRepository(context)
  }

  var tasks by remember { mutableStateOf(repo.loadTasks()) }
  var completedTodayCount by remember { mutableIntStateOf(repo.getCompletedTodayCount(getTodayDateKey())) }
  var completedTasks by remember { mutableStateOf(repo.loadCompletedTasks(getTodayDateKey())) }
  var showCompletedTasksSheet by remember { mutableStateOf(false) }

  var savedTasks by remember { mutableStateOf(repo.loadSavedTasks()) }
  var showSavedTasksSheet by remember { mutableStateOf(false) }

  var undoStack by remember { mutableStateOf(listOf<TodoHistorySnapshot>()) }
  var redoStack by remember { mutableStateOf(listOf<TodoHistorySnapshot>()) }

  val canUndo = undoStack.isNotEmpty()
  val canRedo = redoStack.isNotEmpty()

  // Quick Add State (title only)
  var newTaskTitle by remember { mutableStateOf("") }

  // Inline edit state (task ID currently being edited inline)
  var inlineEditingTaskId by remember { mutableStateOf<String?>(null) }

  // Drag-and-drop reorder state
  var draggedTaskId by remember { mutableStateOf<String?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  var initialDragSnapshot by remember { mutableStateOf<TodoHistorySnapshot?>(null) }
  var measuredItemHeightPx by remember { mutableFloatStateOf(0f) }

  // Check midnight reset periodically / on resume
  LaunchedEffect(Unit) {
    completedTodayCount = repo.getCompletedTodayCount(getTodayDateKey())
    completedTasks = repo.loadCompletedTasks(getTodayDateKey())
  }

  fun applyAction(
    updatedTasks: List<TodoItem>,
    updatedCompletedToday: Int = completedTodayCount,
    updatedCompletedTasks: List<TodoItem> = completedTasks,
  ) {
    val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    undoStack = listOf(currentSnapshot) + undoStack.take(49)
    redoStack = emptyList()

    tasks = updatedTasks
    completedTodayCount = updatedCompletedToday
    completedTasks = updatedCompletedTasks

    repo.saveTasks(updatedTasks)
    repo.setCompletedTodayCount(updatedCompletedToday, getTodayDateKey())
    repo.saveCompletedTasks(updatedCompletedTasks, getTodayDateKey())
  }

  fun undo() {
    if (undoStack.isEmpty()) return
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val previousState = undoStack.first()
    undoStack = undoStack.drop(1)
    redoStack = listOf(TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)) + redoStack

    tasks = previousState.tasks
    completedTodayCount = previousState.completedToday
    completedTasks = previousState.completedTasks

    repo.saveTasks(previousState.tasks)
    repo.setCompletedTodayCount(previousState.completedToday, getTodayDateKey())
    repo.saveCompletedTasks(previousState.completedTasks, getTodayDateKey())
  }

  fun redo() {
    if (redoStack.isEmpty()) return
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val nextState = redoStack.first()
    redoStack = redoStack.drop(1)
    undoStack = listOf(TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)) + undoStack

    tasks = nextState.tasks
    completedTodayCount = nextState.completedToday
    completedTasks = nextState.completedTasks

    repo.saveTasks(nextState.tasks)
    repo.setCompletedTodayCount(nextState.completedToday, getTodayDateKey())
    repo.saveCompletedTasks(nextState.completedTasks, getTodayDateKey())
  }

  fun completeTask(task: TodoItem) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = tasks.filter { it.id != task.id }
    val completedItem = task.copy(
      isSubtasksCollapsed = true,
      subtasks = task.subtasks.map { it.copy(isCompleted = true) },
    )
    applyAction(
      updated,
      updatedCompletedToday = completedTodayCount + 1,
      updatedCompletedTasks = listOf(completedItem) + completedTasks,
    )
  }

  fun deleteTask(task: TodoItem) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = tasks.filter { it.id != task.id }
    applyAction(updated)
  }

  fun addTask() {
    val trimmed = newTaskTitle.trim()
    if (trimmed.isEmpty()) return

    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val newItem = TodoItem(title = trimmed)
    applyAction(listOf(newItem) + tasks)
    newTaskTitle = ""
  }

  fun addSavedTask(savedTask: TodoItem) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val freshItem = TodoItem(
      title = savedTask.title,
      subtasks = savedTask.subtasks.map { TodoSubTask(title = it.title, isCompleted = false) },
      isSubtasksCollapsed = false,
    )
    applyAction(listOf(freshItem) + tasks)
  }

  fun saveTaskTemplate(task: TodoItem) {
    val trimmed = task.title.trim()
    if (trimmed.isEmpty()) return
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val template = TodoItem(
      title = trimmed,
      subtasks = task.subtasks.map { TodoSubTask(title = it.title, isCompleted = false) },
      isSubtasksCollapsed = false,
    )
    val updated = listOf(template) + savedTasks.filter { it.title != trimmed }
    savedTasks = updated
    repo.saveSavedTasks(updated)
    Toast.makeText(context, "Saved \"$trimmed\" as template", Toast.LENGTH_SHORT).show()
  }

  fun saveTaskTemplate(title: String, subtasks: List<TodoSubTask> = emptyList()) {
    saveTaskTemplate(TodoItem(title = title, subtasks = subtasks))
  }

  fun deleteSavedTask(savedTaskId: String) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = savedTasks.filter { it.id != savedTaskId }
    savedTasks = updated
    repo.saveSavedTasks(updated)
  }

  fun toggleSubTask(taskId: String, subtaskId: String) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val updated = TodoListEngine.toggleSubTask(tasks, taskId, subtaskId)
    applyAction(updated)
  }

  fun addSubTask(taskId: String, subtaskTitle: String) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val updated = TodoListEngine.addSubTask(tasks, taskId, subtaskTitle)
    applyAction(updated)
  }

  fun deleteSubTask(taskId: String, subtaskId: String) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = TodoListEngine.deleteSubTask(tasks, taskId, subtaskId)
    applyAction(updated)
  }

  fun updateSubTask(taskId: String, subtaskId: String, newTitle: String) {
    val updated = TodoListEngine.updateSubTaskTitle(tasks, taskId, subtaskId, newTitle)
    applyAction(updated)
  }

  fun toggleCollapseSubtasks(taskId: String) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val updated = TodoListEngine.toggleSubtasksCollapsed(tasks, taskId)
    applyAction(updated)
  }

  fun startDragging(taskId: String) {
    inlineEditingTaskId = null
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    draggedTaskId = taskId
    dragOffsetY = 0f
    initialDragSnapshot = TodoHistorySnapshot(tasks, completedTodayCount)
  }

  fun onDragDelta(taskId: String, deltaY: Float) {
    if (draggedTaskId != taskId) return
    dragOffsetY += deltaY

    val itemHeight = if (measuredItemHeightPx > 0f) measuredItemHeightPx else 160f
    val currentIndex = tasks.indexOfFirst { it.id == taskId }
    if (currentIndex == -1) return

    val threshold = itemHeight * 0.75f
    if (dragOffsetY > threshold && currentIndex < tasks.size - 1) {
      val newTasks = TodoListEngine.reorderTasks(tasks, currentIndex, currentIndex + 1)
      tasks = newTasks
      dragOffsetY -= itemHeight
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    } else if (dragOffsetY < -threshold && currentIndex > 0) {
      val newTasks = TodoListEngine.reorderTasks(tasks, currentIndex, currentIndex - 1)
      tasks = newTasks
      dragOffsetY += itemHeight
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  fun endDragging() {
    val before = initialDragSnapshot
    draggedTaskId = null
    dragOffsetY = 0f
    initialDragSnapshot = null

    if (before != null && before.tasks != tasks) {
      undoStack = listOf(before) + undoStack.take(49)
      redoStack = emptyList()
      repo.saveTasks(tasks)
    }
  }

  var draggedSubtaskId by remember { mutableStateOf<String?>(null) }
  var subtaskDragOffsetY by remember { mutableFloatStateOf(0f) }
  var initialSubtaskDragSnapshot by remember { mutableStateOf<TodoHistorySnapshot?>(null) }

  fun startDraggingSubTask(subtaskId: String) {
    inlineEditingTaskId = null
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    draggedSubtaskId = subtaskId
    subtaskDragOffsetY = 0f
    initialSubtaskDragSnapshot = TodoHistorySnapshot(tasks, completedTodayCount)
  }

  fun onDragSubTaskDelta(taskId: String, subtaskId: String, deltaY: Float) {
    if (draggedSubtaskId != subtaskId) return
    subtaskDragOffsetY += deltaY

    val itemHeight = 100f
    val task = tasks.firstOrNull { it.id == taskId } ?: return
    val currentIndex = task.subtasks.indexOfFirst { it.id == subtaskId }
    if (currentIndex == -1) return

    val threshold = itemHeight * 0.75f
    if (subtaskDragOffsetY > threshold && currentIndex < task.subtasks.size - 1) {
      val newTasks = TodoListEngine.reorderSubTasks(tasks, taskId, currentIndex, currentIndex + 1)
      tasks = newTasks
      subtaskDragOffsetY -= itemHeight
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    } else if (subtaskDragOffsetY < -threshold && currentIndex > 0) {
      val newTasks = TodoListEngine.reorderSubTasks(tasks, taskId, currentIndex, currentIndex - 1)
      tasks = newTasks
      subtaskDragOffsetY += itemHeight
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  fun endDraggingSubTask() {
    val before = initialSubtaskDragSnapshot
    draggedSubtaskId = null
    subtaskDragOffsetY = 0f
    initialSubtaskDragSnapshot = null

    if (before != null && before.tasks != tasks) {
      undoStack = listOf(before) + undoStack.take(49)
      redoStack = emptyList()
      repo.saveTasks(tasks)
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("todo_list_screen"),
    containerColor = MaterialTheme.colorScheme.background,
    bottomBar = {
      // Streamlined Rounded Quick Add Dock
      RoundedQuickAddBar(
        title = newTaskTitle,
        onTitleChange = { newTaskTitle = it },
        onAdd = { addTask() },
        onOpenSavedTasks = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          showSavedTasksSheet = true
        },
      )
    },
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MaterialTheme.colorScheme.background),
    ) {
      // Top Header Bar
      Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
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
                imageVector = Icons.Default.TaskAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
              )
            }
            Column {
              Text(
                text = "To-Do",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("todo_screen_title"),
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = if (tasks.size == 1) "1 task" else "${tasks.size} tasks",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.testTag("todo_task_count_label"),
                )
                Text(
                  text = "•",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Text(
                  text = "$completedTodayCount completed",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.SemiBold,
                  color = if (completedTodayCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      showCompletedTasksSheet = true
                    }
                    .padding(horizontal = 2.dp, vertical = 2.dp)
                    .testTag("todo_completed_today_counter"),
                )
              }
            }
          }

          // Header Actions: Undo and Redo
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Undo Button
            IconButton(
              onClick = { undo() },
              enabled = canUndo,
              modifier = Modifier.testTag("todo_undo_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) {
                  MaterialTheme.colorScheme.onSurface
                } else {
                  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
              )
            }

            // Redo Button
            IconButton(
              onClick = { redo() },
              enabled = canRedo,
              modifier = Modifier.testTag("todo_redo_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                tint = if (canRedo) {
                  MaterialTheme.colorScheme.onSurface
                } else {
                  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
              )
            }
          }
        }
      }
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Main Task List with Drag Handles, Subtasks, and Swipe Gestures
      if (tasks.isEmpty()) {
        EmptyTasksState()
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("todo_tasks_list"),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          items(
            items = tasks,
            key = { it.id },
          ) { task ->
            val isBeingDragged = draggedTaskId == task.id
            val isEditingInline = inlineEditingTaskId == task.id
            SwipeableTaskItem(
              task = task,
              isBeingDragged = isBeingDragged,
              dragOffsetY = if (isBeingDragged) dragOffsetY else 0f,
              isEditingInline = isEditingInline,
              onComplete = { completeTask(task) },
              onDelete = { deleteTask(task) },
              onStartInlineEdit = { inlineEditingTaskId = task.id },
              onSaveInlineEdit = { newTitle ->
                val trimmed = newTitle.trim()
                if (trimmed.isNotEmpty() && trimmed != task.title) {
                  val updated = tasks.map { if (it.id == task.id) it.copy(title = trimmed) else it }
                  applyAction(updated)
                }
                inlineEditingTaskId = null
              },
              onToggleSubTask = { subtaskId -> toggleSubTask(task.id, subtaskId) },
              onToggleCollapseSubtasks = { toggleCollapseSubtasks(task.id) },
              onSaveAsTemplate = { saveTaskTemplate(task) },
              onAddSubTask = { subtaskTitle -> addSubTask(task.id, subtaskTitle) },
              onDeleteSubTask = { subtaskId -> deleteSubTask(task.id, subtaskId) },
              onUpdateSubTask = { subtaskId, newTitle -> updateSubTask(task.id, subtaskId, newTitle) },
              draggedSubtaskId = draggedSubtaskId,
              onStartDragSubTask = { subtaskId -> startDraggingSubTask(subtaskId) },
              onDragSubTask = { subtaskId, deltaY -> onDragSubTaskDelta(task.id, subtaskId, deltaY) },
              onEndDragSubTask = { endDraggingSubTask() },
              onStartDrag = { startDragging(task.id) },
              onDrag = { deltaY -> onDragDelta(task.id, deltaY) },
              onEndDrag = { endDragging() },
              onItemHeightMeasured = { heightPx ->
                if (!isBeingDragged && heightPx > measuredItemHeightPx) {
                  measuredItemHeightPx = heightPx
                }
              },
              modifier = Modifier.animateItem(),
            )
          }
        }
      }
    }

    // Read-only Completed Tasks Sheet
    if (showCompletedTasksSheet) {
      CompletedTasksSheet(
        completedTasks = completedTasks,
        completedCount = completedTodayCount,
        onDismiss = { showCompletedTasksSheet = false },
      )
    }

    // Saved Tasks Sheet
    if (showSavedTasksSheet) {
      SavedTasksSheet(
        savedTasks = savedTasks,
        currentInputTitle = newTaskTitle,
        onAddSavedTask = { savedTask ->
          addSavedTask(savedTask)
        },
        onSaveTemplate = { title ->
          saveTaskTemplate(title)
        },
        onDeleteSavedTask = { savedTaskId ->
          deleteSavedTask(savedTaskId)
        },
        onDismiss = { showSavedTasksSheet = false },
      )
    }
  }
}

/**
 * Modern swipeable task card supporting swipe right to complete, swipe left to delete,
 * click-text-to-edit, vertical drag handle, and collapsible subtasks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskItem(
  task: TodoItem,
  isBeingDragged: Boolean,
  dragOffsetY: Float,
  isEditingInline: Boolean,
  onComplete: () -> Unit,
  onDelete: () -> Unit,
  onStartInlineEdit: () -> Unit,
  onSaveInlineEdit: (String) -> Unit,
  onToggleSubTask: (String) -> Unit,
  onToggleCollapseSubtasks: () -> Unit = {},
  onSaveAsTemplate: () -> Unit = {},
  onAddSubTask: (String) -> Unit,
  onDeleteSubTask: (String) -> Unit,
  onUpdateSubTask: (String, String) -> Unit,
  draggedSubtaskId: String? = null,
  onStartDragSubTask: (String) -> Unit = {},
  onDragSubTask: (String, Float) -> Unit = { _, _ -> },
  onEndDragSubTask: () -> Unit = {},
  onStartDrag: () -> Unit,
  onDrag: (Float) -> Unit,
  onEndDrag: () -> Unit,
  onItemHeightMeasured: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val dismissState = rememberSwipeToDismissBoxState(
    positionalThreshold = { it * 0.35f },
  )

  LaunchedEffect(dismissState.currentValue) {
    if (!isBeingDragged) {
      when (dismissState.currentValue) {
        SwipeToDismissBoxValue.StartToEnd -> {
          onComplete()
        }
        SwipeToDismissBoxValue.EndToStart -> {
          onDelete()
        }
        SwipeToDismissBoxValue.Settled -> {}
      }
    }
  }

  val dragScale by animateFloatAsState(
    targetValue = if (isBeingDragged) 1.03f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "drag_scale",
  )

  SwipeToDismissBox(
    state = dismissState,
    enableDismissFromStartToEnd = !isBeingDragged && !isEditingInline,
    enableDismissFromEndToStart = !isBeingDragged && !isEditingInline,
    backgroundContent = {
      val direction = dismissState.dismissDirection
      val isCompleteSwipe = direction == SwipeToDismissBoxValue.StartToEnd
      val isDeleteSwipe = direction == SwipeToDismissBoxValue.EndToStart

      val backgroundColor by animateColorAsState(
        targetValue = when {
          isCompleteSwipe -> MaterialTheme.colorScheme.primary
          isDeleteSwipe -> MaterialTheme.colorScheme.error
          else -> Color.Transparent
        },
        label = "swipe_bg",
      )

      val iconScale by animateFloatAsState(
        targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.25f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe_icon_scale",
      )

      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(14.dp))
          .background(backgroundColor)
          .padding(horizontal = 20.dp),
        contentAlignment = if (isCompleteSwipe) Alignment.CenterStart else Alignment.CenterEnd,
      ) {
        if (isCompleteSwipe) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.scale(iconScale),
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Complete",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(26.dp),
            )
            Text(
              text = "Complete",
              color = MaterialTheme.colorScheme.onPrimary,
              fontWeight = FontWeight.Bold,
            )
          }
        } else if (isDeleteSwipe) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.scale(iconScale),
          ) {
            Text(
              text = "Delete",
              color = MaterialTheme.colorScheme.onError,
              fontWeight = FontWeight.Bold,
            )
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.onError,
              modifier = Modifier.size(26.dp),
            )
          }
        }
      }
    },
    content = {
      TaskCardContent(
        task = task,
        isBeingDragged = isBeingDragged,
        isEditingInline = isEditingInline,
        onCheckboxClick = onComplete,
        onStartInlineEdit = onStartInlineEdit,
        onSaveInlineEdit = onSaveInlineEdit,
        onToggleSubTask = onToggleSubTask,
        onToggleCollapseSubtasks = onToggleCollapseSubtasks,
        onSaveAsTemplate = onSaveAsTemplate,
        onAddSubTask = onAddSubTask,
        onDeleteSubTask = onDeleteSubTask,
        onUpdateSubTask = onUpdateSubTask,
        draggedSubtaskId = draggedSubtaskId,
        onStartDragSubTask = onStartDragSubTask,
        onDragSubTask = onDragSubTask,
        onEndDragSubTask = onEndDragSubTask,
        onStartDrag = onStartDrag,
        onDrag = onDrag,
        onEndDrag = onEndDrag,
        onHeightMeasured = onItemHeightMeasured,
      )
    },
    modifier = modifier
      .zIndex(if (isBeingDragged) 2f else 1f)
      .graphicsLayer {
        translationY = dragOffsetY
        scaleX = dragScale
        scaleY = dragScale
      }
      .testTag("todo_item_${task.id}"),
  )
}

/**
 * Task card content with checkbox, clickable text to edit, collapsible subtasks list, and drag handle.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskCardContent(
  task: TodoItem,
  isBeingDragged: Boolean,
  isEditingInline: Boolean,
  onCheckboxClick: () -> Unit,
  onStartInlineEdit: () -> Unit,
  onSaveInlineEdit: (String) -> Unit,
  onToggleSubTask: (String) -> Unit,
  onToggleCollapseSubtasks: () -> Unit = {},
  onSaveAsTemplate: () -> Unit = {},
  onAddSubTask: (String) -> Unit,
  onDeleteSubTask: (String) -> Unit,
  onUpdateSubTask: (String, String) -> Unit,
  draggedSubtaskId: String? = null,
  onStartDragSubTask: (String) -> Unit = {},
  onDragSubTask: (String, Float) -> Unit = { _, _ -> },
  onEndDragSubTask: () -> Unit = {},
  onStartDrag: () -> Unit,
  onDrag: (Float) -> Unit,
  onEndDrag: () -> Unit,
  onHeightMeasured: (Float) -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val checkScale = remember { Animatable(1f) }
  var newSubtaskInput by remember { mutableStateOf("") }
  var showAddSubtaskInput by remember { mutableStateOf(false) }
  var editingSubtaskId by remember { mutableStateOf<String?>(null) }
  val newSubtaskFocusRequester = remember { FocusRequester() }

  LaunchedEffect(isBeingDragged) {
    if (isBeingDragged) {
      editingSubtaskId = null
      showAddSubtaskInput = false
    }
  }

  LaunchedEffect(task.isSubtasksCollapsed) {
    if (task.isSubtasksCollapsed) {
      editingSubtaskId = null
      showAddSubtaskInput = false
    }
  }

  LaunchedEffect(showAddSubtaskInput) {
    if (showAddSubtaskInput) {
      newSubtaskFocusRequester.requestFocus()
    }
  }

  val subtasksCount = task.subtasks.size
  val completedSubtasksCount = task.subtasks.count { it.isCompleted }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .onGloballyPositioned { coords ->
        onHeightMeasured(coords.size.height.toFloat())
      }
      .testTag("todo_item_card_${task.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isBeingDragged) {
        MaterialTheme.colorScheme.surfaceContainerHigh
      } else {
        MaterialTheme.colorScheme.surfaceContainerLow
      },
    ),
    border = if (isBeingDragged) {
      BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
      BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    },
    elevation = CardDefaults.cardElevation(
      defaultElevation = if (isBeingDragged) 6.dp else 0.dp,
    ),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Main Task Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Drag Handle to reorder tasks (on the left side) - Triple bar
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .pointerInput(task.id) {
              detectDragGestures(
                onDragStart = { onStartDrag() },
                onDrag = { change, dragAmount ->
                  change.consume()
                  onDrag(dragAmount.y)
                },
                onDragEnd = { onEndDrag() },
                onDragCancel = { onEndDrag() },
              )
            }
            .testTag("todo_item_drag_${task.id}"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Drag to reorder",
            tint = if (isBeingDragged) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(20.dp),
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Checkbox with spring bounce animation
        Box(
          modifier = Modifier
            .size(32.dp)
            .scale(checkScale.value)
            .clip(CircleShape)
            .clickable {
              scope.launch {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                checkScale.animateTo(
                  targetValue = 1.3f,
                  animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                )
                checkScale.animateTo(1f, tween(100))
                onCheckboxClick()
              }
            }
            .testTag("todo_item_checkbox_${task.id}"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.RadioButtonUnchecked,
            contentDescription = "Complete task",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp),
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Title or Inline Editor (blends seamlessly into background, cursor at end)
        if (isEditingInline) {
          var textFieldValue by remember(task.id, task.title) {
            mutableStateOf(
              TextFieldValue(
                text = task.title,
                selection = TextRange(task.title.length),
              )
            )
          }
          val focusRequester = remember { FocusRequester() }
          var hasBeenFocused by remember { mutableStateOf(false) }

          LaunchedEffect(Unit) {
            focusRequester.requestFocus()
          }

          BasicTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
              onDone = {
                onSaveInlineEdit(textFieldValue.text)
              },
            ),
            modifier = Modifier
              .weight(1f)
              .focusRequester(focusRequester)
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  hasBeenFocused = true
                } else if (hasBeenFocused) {
                  onSaveInlineEdit(textFieldValue.text)
                }
              }
              .padding(vertical = 4.dp, horizontal = 4.dp)
              .testTag("todo_edit_title"),
          )
        } else {
          var showContextMenu by remember { mutableStateOf(false) }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .combinedClickable(
                onClick = onStartInlineEdit,
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  showContextMenu = true
                },
              )
              .padding(vertical = 4.dp, horizontal = 4.dp)
              .testTag("todo_item_title_${task.id}"),
          ) {
            Text(
              text = task.title,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )

            DropdownMenu(
              expanded = showContextMenu,
              onDismissRequest = { showContextMenu = false },
            ) {
              DropdownMenuItem(
                text = { Text("Save as Template") },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                  )
                },
                onClick = {
                  showContextMenu = false
                  onSaveAsTemplate()
                },
                modifier = Modifier.testTag("todo_menu_save_template_${task.id}"),
              )
            }
          }
        }

        // Subtasks progress counter & Add subtask (+) button & Save as template button (when editing)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          if (isEditingInline) {
            IconButton(
              onClick = onSaveAsTemplate,
              modifier = Modifier
                .size(32.dp)
                .testTag("todo_save_template_inline_${task.id}"),
            ) {
              Icon(
                imageVector = Icons.Default.BookmarkAdd,
                contentDescription = "Save as template",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }
          }
          if (subtasksCount > 0) {
            val caretRotation by animateFloatAsState(
              targetValue = if (task.isSubtasksCollapsed) 0f else 180f,
              animationSpec = tween(durationMillis = 250),
              label = "caret_rotation_${task.id}",
            )

            Box(
              modifier = Modifier
                .clip(CircleShape)
                .clickable {
                  if (!task.isSubtasksCollapsed) {
                    showAddSubtaskInput = false
                    editingSubtaskId = null
                  }
                  onToggleCollapseSubtasks()
                }
                .background(
                  if (completedSubtasksCount == subtasksCount) {
                    MaterialTheme.colorScheme.primaryContainer
                  } else {
                    MaterialTheme.colorScheme.secondaryContainer
                  },
                  CircleShape,
                )
                .padding(horizontal = 7.dp, vertical = 3.dp)
                .testTag("todo_subtask_indicator_${task.id}"),
              contentAlignment = Alignment.Center,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text(
                  text = "$completedSubtasksCount/$subtasksCount",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (completedSubtasksCount == subtasksCount) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                  },
                )
                Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = if (task.isSubtasksCollapsed) "Expand subtasks" else "Collapse subtasks",
                  tint = if (completedSubtasksCount == subtasksCount) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                  },
                  modifier = Modifier
                    .size(15.dp)
                    .rotate(caretRotation),
                )
              }
            }
          }

          IconButton(
            onClick = {
              if (task.isSubtasksCollapsed) {
                onToggleCollapseSubtasks()
                showAddSubtaskInput = true
              } else {
                showAddSubtaskInput = !showAddSubtaskInput
              }
            },
            modifier = Modifier
              .size(32.dp)
              .testTag("todo_add_subtask_toggle_${task.id}"),
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add subtask",
              tint = if (showAddSubtaskInput) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
              },
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }

      // Subtasks Section (hidden when re-ordering tasks or collapsed)
      AnimatedVisibility(
        visible = !isBeingDragged && !task.isSubtasksCollapsed && (task.subtasks.isNotEmpty() || showAddSubtaskInput),
        enter = fadeIn(animationSpec = tween(250)) +
          slideInVertically(animationSpec = tween(250), initialOffsetY = { -it / 3 }) +
          expandVertically(animationSpec = tween(250)),
        exit = fadeOut(animationSpec = tween(200)) +
          slideOutVertically(animationSpec = tween(200), targetOffsetY = { -it / 3 }) +
          shrinkVertically(animationSpec = tween(200)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, bottom = 10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          // Subtask items (always shown directly, not collapsible, blends into background)
          task.subtasks.forEach { subtask ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              // Smaller drag handle with double bar
              val isSubtaskBeingDragged = draggedSubtaskId == subtask.id
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .pointerInput(subtask.id) {
                    detectDragGestures(
                      onDragStart = { onStartDragSubTask(subtask.id) },
                      onDrag = { change, dragAmount ->
                        change.consume()
                        onDragSubTask(subtask.id, dragAmount.y)
                      },
                      onDragEnd = { onEndDragSubTask() },
                      onDragCancel = { onEndDragSubTask() },
                    )
                  }
                  .testTag("todo_subtask_drag_${subtask.id}"),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.DragHandle,
                  contentDescription = "Drag to reorder subtask",
                  tint = if (isSubtaskBeingDragged) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                  },
                  modifier = Modifier.size(16.dp),
                )
              }

              Spacer(modifier = Modifier.width(2.dp))

              // Subtask checkbox
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .clickable { onToggleSubTask(subtask.id) }
                  .testTag("todo_subtask_checkbox_${subtask.id}"),
                contentAlignment = Alignment.Center,
              ) {
                if (subtask.isCompleted) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Complete subtask",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Incomplete subtask",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(22.dp),
                  )
                }
              }

              Spacer(modifier = Modifier.width(6.dp))

              // Subtask Title or Inline Editor (blends seamlessly into background, cursor at end)
              if (editingSubtaskId == subtask.id) {
                var subtaskTextFieldValue by remember(subtask.id, subtask.title) {
                  mutableStateOf(
                    TextFieldValue(
                      text = subtask.title,
                      selection = TextRange(subtask.title.length),
                    )
                  )
                }
                val subtaskFocusRequester = remember { FocusRequester() }
                var subtaskHasBeenFocused by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                  subtaskFocusRequester.requestFocus()
                }

                BasicTextField(
                  value = subtaskTextFieldValue,
                  onValueChange = { subtaskTextFieldValue = it },
                  textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                  ),
                  cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                  keyboardActions = KeyboardActions(
                    onDone = {
                      val trimmed = subtaskTextFieldValue.text.trim()
                      if (trimmed.isNotEmpty() && trimmed != subtask.title) {
                        onUpdateSubTask(subtask.id, trimmed)
                      }
                      editingSubtaskId = null
                    },
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .focusRequester(subtaskFocusRequester)
                    .onFocusChanged { focusState ->
                      if (focusState.isFocused) {
                        subtaskHasBeenFocused = true
                      } else if (subtaskHasBeenFocused) {
                        val trimmed = subtaskTextFieldValue.text.trim()
                        if (trimmed.isNotEmpty() && trimmed != subtask.title) {
                          onUpdateSubTask(subtask.id, trimmed)
                        }
                        editingSubtaskId = null
                      }
                    }
                    .padding(vertical = 2.dp, horizontal = 2.dp)
                    .testTag("todo_subtask_edit_${subtask.id}"),
                )
              } else {
                Text(
                  text = subtask.title,
                  style = MaterialTheme.typography.bodyMedium,
                  color = if (subtask.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                  textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { editingSubtaskId = subtask.id }
                    .padding(vertical = 2.dp, horizontal = 2.dp)
                    .testTag("todo_subtask_title_${subtask.id}"),
                )
              }

              // Delete subtask button (x icon, aligned with the + icon on main task row)
              IconButton(
                onClick = { onDeleteSubTask(subtask.id) },
                modifier = Modifier
                  .size(32.dp)
                  .testTag("todo_subtask_delete_${subtask.id}"),
              ) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Delete subtask",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                  modifier = Modifier.size(15.dp),
                )
              }
            }
          }

          // Subtask input (shown when user presses + button, automatically focused)
          AnimatedVisibility(
            visible = showAddSubtaskInput,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              // Align input with checkboxes (36.dp drag handle + 2.dp spacer)
              Spacer(modifier = Modifier.width(38.dp))

              BasicTextField(
                value = newSubtaskInput,
                onValueChange = { newSubtaskInput = it },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                  color = MaterialTheme.colorScheme.onSurface,
                  fontSize = 13.sp,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                  onDone = {
                    if (newSubtaskInput.trim().isNotEmpty()) {
                      onAddSubTask(newSubtaskInput)
                      newSubtaskInput = ""
                    }
                  },
                ),
                decorationBox = { innerTextField ->
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                  ) {
                    if (newSubtaskInput.isEmpty()) {
                      Text(
                        text = "Add a subtask...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                      )
                    }
                    innerTextField()
                  }
                },
                modifier = Modifier
                  .weight(1f)
                  .height(32.dp)
                  .focusRequester(newSubtaskFocusRequester)
                  .clip(CircleShape)
                  .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    CircleShape,
                  )
                  .testTag("todo_subtask_input_${task.id}"),
              )

              Spacer(modifier = Modifier.width(3.dp))

              FilledIconButton(
                onClick = {
                  if (newSubtaskInput.trim().isNotEmpty()) {
                    onAddSubTask(newSubtaskInput)
                    newSubtaskInput = ""
                  }
                },
                enabled = newSubtaskInput.trim().isNotEmpty(),
                shape = CircleShape,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary,
                  disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                  disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                ),
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add subtask",
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

/**
 * Streamlined docked bottom quick-add bar with rounded input and rounded plus button.
 */
@Composable
private fun RoundedQuickAddBar(
  title: String,
  onTitleChange: (String) -> Unit,
  onAdd: () -> Unit,
  onOpenSavedTasks: () -> Unit,
) {
  val isEnabled = title.trim().isNotEmpty()
  val addScale by animateFloatAsState(
    targetValue = if (isEnabled) 1f else 0.88f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "add_btn_scale",
  )

  Surface(
    color = MaterialTheme.colorScheme.surfaceContainer,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("todo_quick_add_card"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Button to the left of the task input to access saved tasks
      IconButton(
        onClick = onOpenSavedTasks,
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceContainerHigh)
          .testTag("todo_saved_tasks_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Bookmarks,
          contentDescription = "Saved tasks",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp),
        )
      }

      // Rounded text input field (pill / CircleShape)
      OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        placeholder = {
          Text(
            "Add a task...",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        },
        modifier = Modifier
          .weight(1f)
          .testTag("todo_input_title"),
        singleLine = true,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onAdd() }),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
          focusedTextColor = MaterialTheme.colorScheme.onSurface,
          unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
          cursorColor = MaterialTheme.colorScheme.primary,
        ),
      )

      // Fully rounded plus button (CircleShape)
      FilledIconButton(
        onClick = onAdd,
        enabled = isEnabled,
        shape = CircleShape,
        modifier = Modifier
          .size(52.dp)
          .scale(addScale)
          .testTag("todo_add_button"),
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
          disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        ),
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Task",
          modifier = Modifier.size(26.dp),
        )
      }
    }
  }
}

/**
 * Empty tasks illustration and encouragement.
 */
@Composable
private fun EmptyTasksState() {
  val scale by animateFloatAsState(
    targetValue = 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "empty_icon_scale",
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier = Modifier
          .size(80.dp)
          .scale(scale)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.TaskAlt,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(44.dp),
        )
      }

      Text(
        text = "All caught up!",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Text(
        text = "Swipe tasks right to complete, left to delete.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}


/**
 * Read-only modal sheet displaying tasks completed today.
 * Subtasks are collapsed by default, but can be uncollapsed by tapping the indicator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompletedTasksSheet(
  completedTasks: List<TodoItem>,
  completedCount: Int,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  // Subtasks are collapsed by default, but can be uncollapsed per task
  val uncollapsedTaskIds = remember { mutableStateMapOf<String, Boolean>() }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("todo_completed_tasks_sheet"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = 32.dp),
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Completed Today",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = if (completedCount == 1) "1 task completed" else "$completedCount tasks completed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .testTag("todo_completed_tasks_close"),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      if (completedTasks.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.TaskAlt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
              modifier = Modifier.size(44.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = if (completedCount > 0) "$completedCount task(s) completed today" else "No completed tasks yet today",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(completedTasks, key = { it.id }) { task ->
            val isUncollapsed = uncollapsedTaskIds[task.id] == true
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("todo_completed_item_${task.id}"),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              ),
              elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
              ) {
                // Task row (read-only)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  // Checked icon
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                  )

                  Spacer(modifier = Modifier.width(10.dp))

                  // Task title with strikethrough (read-only)
                  Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                  )

                  // Subtasks indicator (if task has subtasks)
                  if (task.subtasks.isNotEmpty()) {
                    val caretRotation by animateFloatAsState(
                      targetValue = if (isUncollapsed) 180f else 0f,
                      animationSpec = tween(durationMillis = 250),
                      label = "completed_caret_${task.id}",
                    )

                    Box(
                      modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                          uncollapsedTaskIds[task.id] = !isUncollapsed
                        }
                        .background(
                          MaterialTheme.colorScheme.primaryContainer,
                          CircleShape,
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                        .testTag("todo_completed_subtask_indicator_${task.id}"),
                      contentAlignment = Alignment.Center,
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                      ) {
                        Text(
                          text = "${task.subtasks.count { it.isCompleted }}/${task.subtasks.size}",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Icon(
                          imageVector = Icons.Default.KeyboardArrowDown,
                          contentDescription = if (isUncollapsed) "Collapse subtasks" else "Expand subtasks",
                          tint = MaterialTheme.colorScheme.onPrimaryContainer,
                          modifier = Modifier
                            .size(15.dp)
                            .rotate(caretRotation),
                        )
                      }
                    }
                  }
                }

                // Subtasks section (collapsed by default, but can be uncollapsed)
                AnimatedVisibility(
                  visible = isUncollapsed && task.subtasks.isNotEmpty(),
                  enter = fadeIn(animationSpec = tween(250)) +
                    slideInVertically(animationSpec = tween(250), initialOffsetY = { -it / 3 }) +
                    expandVertically(animationSpec = tween(250)),
                  exit = fadeOut(animationSpec = tween(200)) +
                    slideOutVertically(animationSpec = tween(200), targetOffsetY = { -it / 3 }) +
                    shrinkVertically(animationSpec = tween(200)),
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(start = 32.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    task.subtasks.forEach { subtask ->
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                          modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = subtask.title,
                          style = MaterialTheme.typography.bodySmall,
                          textDecoration = TextDecoration.LineThrough,
                          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
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
  }
}

/**
 * Modal bottom sheet displaying saved task templates for quick re-adding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedTasksSheet(
  savedTasks: List<TodoItem>,
  currentInputTitle: String,
  onAddSavedTask: (TodoItem) -> Unit,
  onSaveTemplate: (String) -> Unit,
  onDeleteSavedTask: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var newTemplateText by remember { mutableStateOf(currentInputTitle.trim()) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("todo_saved_tasks_sheet"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = 32.dp),
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Saved Tasks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Tap to quickly re-add to your to-do list",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .testTag("todo_saved_tasks_close"),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Quick save input inside sheet
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = newTemplateText,
          onValueChange = { newTemplateText = it },
          placeholder = {
            Text("Save a new template...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
          },
          singleLine = true,
          shape = CircleShape,
          modifier = Modifier
            .weight(1f)
            .testTag("todo_save_template_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
          ),
        )
        FilledIconButton(
          onClick = {
            if (newTemplateText.trim().isNotEmpty()) {
              onSaveTemplate(newTemplateText.trim())
              newTemplateText = ""
            }
          },
          enabled = newTemplateText.trim().isNotEmpty(),
          shape = CircleShape,
          modifier = Modifier.size(48.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Save template",
            modifier = Modifier.size(24.dp),
          )
        }
      }

      if (savedTasks.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Bookmarks,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
              modifier = Modifier.size(44.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No saved tasks yet",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Save repetitive or routine tasks to re-add them with one tap.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(savedTasks, key = { it.id }) { savedTask ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAddSavedTask(savedTask) }
                .testTag("todo_saved_item_${savedTask.id}"),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              ),
              elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                // Add icon
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(22.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = savedTask.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  if (savedTask.subtasks.isNotEmpty()) {
                    Text(
                      text = "${savedTask.subtasks.size} subtasks",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                IconButton(
                  onClick = { onDeleteSavedTask(savedTask.id) },
                  modifier = Modifier.size(28.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete template",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
}

@Preview(showBackground = true)
@Composable
fun TodoListScreenPreview() {
  val fakeRepo = InMemoryTodoListRepository(
    initialTasks = listOf(
      TodoItem(
        title = "Pick up new multimeter fuses",
        subtasks = listOf(
          TodoSubTask(title = "Check size 5x20mm"),
          TodoSubTask(title = "Get 10A pack"),
        ),
      ),
      TodoItem(title = "Calibrate tuner pitch reference"),
    ),
    completedCount = 2,
    initialCompletedTasks = listOf(
      TodoItem(
        title = "Inspect audio interface capacitors",
        subtasks = listOf(
          TodoSubTask(title = "Discharge rail caps", isCompleted = true),
          TodoSubTask(title = "Check ESR values", isCompleted = true),
        ),
      ),
      TodoItem(title = "Clean soldering iron tip"),
    ),
    initialSavedTasks = listOf(
      TodoItem(
        title = "Weekly gear maintenance",
        subtasks = listOf(
          TodoSubTask(title = "Wipe fretboards"),
          TodoSubTask(title = "Test cables"),
        ),
      ),
    ),
  )
  ToolboxTheme {
    TodoListScreen(repository = fakeRepo)
  }
}
