package com.example.toolbox.ui.tools

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
  var historyVersion by remember { mutableIntStateOf(0) }

  val canUndo = undoStack.isNotEmpty()
  val canRedo = redoStack.isNotEmpty()

  // Quick Add State (title only)
  var newTaskTitle by remember { mutableStateOf("") }

  // Inline edit state (task ID currently being edited inline)
  var inlineEditingTaskId by remember { mutableStateOf<String?>(null) }

  // Schedule / Reschedule dialog state
  var schedulingTaskId by remember { mutableStateOf<String?>(null) }

  // Active in-app due notification popups
  var duePopupTasks by remember { mutableStateOf<List<TodoItem>>(emptyList()) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { _ -> }

  // Drag-and-drop reorder state
  val density = LocalDensity.current
  val listState = rememberLazyListState()
  val taskSpacingPx = with(density) { 4.dp.toPx() }
  val defaultTaskHeightPx = with(density) { 60.dp.toPx() }
  val taskMaxOverscrollPx = with(density) { 12.dp.toPx() }
  val subtaskSpacingPx = with(density) { 6.dp.toPx() }
  val defaultSubtaskHeightPx = with(density) { 40.dp.toPx() }
  val subtaskMaxOverscrollPx = with(density) { 4.dp.toPx() }

  var draggedTaskId by remember { mutableStateOf<String?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  var initialDragSnapshot by remember { mutableStateOf<TodoHistorySnapshot?>(null) }
  val taskHeightsPx = remember { mutableStateMapOf<String, Float>() }
  val subtaskHeightsPx = remember { mutableStateMapOf<String, Float>() }

  // Check midnight reset periodically / on resume
  LaunchedEffect(Unit) {
    completedTodayCount = repo.getCompletedTodayCount(getTodayDateKey())
    completedTasks = repo.loadCompletedTasks(getTodayDateKey())
    TodoNotificationHelper.ensureNotificationChannel(context)
  }

  // Synchronize background alarms and poll for due tasks every second while screen is active
  LaunchedEffect(tasks) {
    TodoNotificationHelper.syncTaskAlarms(context, tasks)
    while (true) {
      val now = System.currentTimeMillis()
      val dueList = TodoListEngine.getDueUnnotifiedTasks(tasks, now)
      if (dueList.isNotEmpty()) {
        var updated = tasks
        dueList.forEach { dueTask ->
          val scheduledAt = dueTask.scheduledAtMillis ?: now
          TodoNotificationHelper.showTaskNotification(context, dueTask)
          updated = TodoListEngine.markTaskNotified(updated, dueTask.id, scheduledAt)
          if (duePopupTasks.none { it.id == dueTask.id }) {
            duePopupTasks = listOf(dueTask) + duePopupTasks
          }
        }
        tasks = updated
        repo.saveTasks(updated)
      }
      delay(1_000L)
    }
  }

  fun applyAction(
    updatedTasks: List<TodoItem>,
    updatedCompletedToday: Int = completedTodayCount,
    updatedCompletedTasks: List<TodoItem> = completedTasks,
  ) {
    val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    val nextSnapshot = TodoHistorySnapshot(updatedTasks, updatedCompletedToday, updatedCompletedTasks)
    if (currentSnapshot == nextSnapshot) return
    undoStack = listOf(currentSnapshot) + undoStack.take(49)
    redoStack = emptyList()
    historyVersion += 1

    tasks = updatedTasks
    completedTodayCount = updatedCompletedToday
    completedTasks = updatedCompletedTasks

    repo.saveTasks(updatedTasks)
    repo.setCompletedTodayCount(updatedCompletedToday, getTodayDateKey())
    repo.saveCompletedTasks(updatedCompletedTasks, getTodayDateKey())
  }

  fun applySnapshot(nextSnapshot: TodoHistorySnapshot) {
    applyAction(
      updatedTasks = nextSnapshot.tasks,
      updatedCompletedToday = nextSnapshot.completedToday,
      updatedCompletedTasks = nextSnapshot.completedTasks,
    )
  }

  fun undo() {
    if (undoStack.isEmpty()) return
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val previousState = undoStack.first()
    undoStack = undoStack.drop(1)
    redoStack = listOf(TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)) + redoStack
    historyVersion += 1

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
    historyVersion += 1

    tasks = nextState.tasks
    completedTodayCount = nextState.completedToday
    completedTasks = nextState.completedTasks

    repo.saveTasks(nextState.tasks)
    repo.setCompletedTodayCount(nextState.completedToday, getTodayDateKey())
    repo.saveCompletedTasks(nextState.completedTasks, getTodayDateKey())
  }

  fun completeTask(task: TodoItem) {
    val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    val nextSnapshot = TodoListEngine.completeTask(currentSnapshot, task.id)
    if (nextSnapshot == currentSnapshot) return
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    applySnapshot(nextSnapshot)
  }

  fun deleteTask(task: TodoItem) {
    val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    val nextSnapshot = TodoListEngine.deleteTask(currentSnapshot, task.id)
    if (nextSnapshot == currentSnapshot) return
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    applySnapshot(nextSnapshot)
  }

  fun deleteCompletedTask(task: TodoItem) {
    val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    val nextSnapshot = TodoListEngine.deleteCompletedTask(currentSnapshot, task.id)
    if (nextSnapshot == currentSnapshot) return
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    applySnapshot(nextSnapshot)
  }

  fun uncompleteTask(task: TodoItem) {
    val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    val nextSnapshot = TodoListEngine.uncompleteTask(currentSnapshot, task.id)
    if (nextSnapshot == currentSnapshot) return
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    applySnapshot(nextSnapshot)
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
    val updated = TodoListEngine.saveTaskAsTemplate(savedTasks, task)
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

  fun scheduleTask(
    taskId: String,
    scheduledAtMillis: Long,
    scheduledEndMillis: Long?,
    notes: String,
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !TodoNotificationHelper.hasNotificationPermission(context)) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val updated = TodoListEngine.scheduleTask(
      tasks = tasks,
      taskId = taskId,
      scheduledAtMillis = scheduledAtMillis,
      scheduledEndMillis = scheduledEndMillis,
      notes = notes,
    )
    duePopupTasks = duePopupTasks.filter { it.id != taskId }
    applyAction(updated)
    val label = TodoListEngine.formatScheduleLabel(scheduledAtMillis, scheduledEndMillis)
    Toast.makeText(context, "Scheduled for $label", Toast.LENGTH_SHORT).show()
  }

  fun unscheduleTask(taskId: String) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    TodoNotificationHelper.cancelTaskAlarm(context, taskId)
    val updated = TodoListEngine.unscheduleTask(tasks, taskId)
    duePopupTasks = duePopupTasks.filter { it.id != taskId }
    applyAction(updated)
  }

  fun updateTaskNotes(taskId: String, notes: String) {
    val updated = TodoListEngine.updateTaskNotes(tasks, taskId, notes)
    applyAction(updated)
  }

  var draggedSubtaskId by remember { mutableStateOf<String?>(null) }
  var subtaskDragOffsetY by remember { mutableFloatStateOf(0f) }
  var initialSubtaskDragSnapshot by remember { mutableStateOf<TodoHistorySnapshot?>(null) }

  fun endDraggingSubTask() {
    val before = initialSubtaskDragSnapshot
    draggedSubtaskId = null
    subtaskDragOffsetY = 0f
    initialSubtaskDragSnapshot = null

    if (before != null && before.tasks != tasks) {
      undoStack = listOf(before) + undoStack.take(49)
      redoStack = emptyList()
      historyVersion += 1
      repo.saveTasks(tasks)
    }
  }

  fun startDragging(taskId: String) {
    if (draggedSubtaskId != null) {
      endDraggingSubTask()
    }
    inlineEditingTaskId = null
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    draggedTaskId = taskId
    dragOffsetY = 0f
    initialDragSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
  }

  fun onDragDelta(taskId: String, deltaY: Float) {
    if (draggedTaskId != taskId) return
    val firstVisibleIndex = listState.firstVisibleItemIndex
    val firstVisibleOffset = listState.firstVisibleItemScrollOffset

    val stepResult = TodoListEngine.calculateDragReorder(
      items = tasks,
      draggedId = taskId,
      idSelector = { it.id },
      currentOffsetY = dragOffsetY + deltaY,
      itemHeightsPx = taskHeightsPx,
      defaultItemHeightPx = defaultTaskHeightPx,
      itemSpacingPx = taskSpacingPx,
      maxOverscrollPx = taskMaxOverscrollPx,
    )

    dragOffsetY = stepResult.remainingOffsetY
    if (stepResult.moved) {
      tasks = stepResult.items
      val minIdx = minOf(stepResult.fromIndex, stepResult.toIndex)
      val maxIdx = maxOf(stepResult.fromIndex, stepResult.toIndex)
      if (firstVisibleIndex in minIdx..maxIdx) {
        listState.requestScrollToItem(firstVisibleIndex, firstVisibleOffset)
      }
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
      historyVersion += 1
      repo.saveTasks(tasks)
    }
  }

  fun startDraggingSubTask(subtaskId: String) {
    if (draggedTaskId != null) {
      endDragging()
    }
    inlineEditingTaskId = null
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    draggedSubtaskId = subtaskId
    subtaskDragOffsetY = 0f
    initialSubtaskDragSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
  }

  fun onDragSubTaskDelta(taskId: String, subtaskId: String, deltaY: Float) {
    if (draggedSubtaskId != subtaskId) return
    val task = tasks.firstOrNull { it.id == taskId } ?: return

    val stepResult = TodoListEngine.calculateDragReorder(
      items = task.subtasks,
      draggedId = subtaskId,
      idSelector = { it.id },
      currentOffsetY = subtaskDragOffsetY + deltaY,
      itemHeightsPx = subtaskHeightsPx,
      defaultItemHeightPx = defaultSubtaskHeightPx,
      itemSpacingPx = subtaskSpacingPx,
      maxOverscrollPx = subtaskMaxOverscrollPx,
    )

    subtaskDragOffsetY = stepResult.remainingOffsetY
    if (stepResult.moved) {
      tasks = tasks.map { item ->
        if (item.id == taskId) item.copy(subtasks = stepResult.items) else item
      }
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
              val scheduledCount = tasks.count { it.isScheduled }
              Text(
                text = "To Do",
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
                  text = "$scheduledCount scheduled",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = if (scheduledCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                  color = if (scheduledCount > 0) {
                    MaterialTheme.colorScheme.tertiary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  modifier = Modifier.testTag("todo_scheduled_count_label"),
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

      // In-app Notification Popup Banner when a scheduled note's date/time is reached
      AnimatedVisibility(
        visible = duePopupTasks.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
      ) {
        val topDue = duePopupTasks.firstOrNull()
        if (topDue != null) {
          DueNotificationPopupCard(
            task = topDue,
            onReschedule = {
              duePopupTasks = duePopupTasks.filter { it.id != topDue.id }
              schedulingTaskId = topDue.id
            },
            onDismiss = {
              duePopupTasks = duePopupTasks.filter { it.id != topDue.id }
            },
          )
        }
      }

      // Scheduled Notes Label Bar (shown when there are scheduled notes)
      val scheduledTasksCount = tasks.count { it.isScheduled }
      AnimatedVisibility(
        visible = scheduledTasksCount > 0,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("todo_scheduled_notes_label"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = if (scheduledTasksCount == 1) {
                  "Scheduled Notes • 1 active"
                } else {
                  "Scheduled Notes • $scheduledTasksCount active"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }
      }

      // Main Task List with Drag Handles, Subtasks, and Swipe Gestures
      if (tasks.isEmpty()) {
        EmptyTasksState()
      } else {
        LazyColumn(
          state = listState,
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
              historyVersion = historyVersion,
              isBeingDragged = isBeingDragged,
              dragOffsetY = if (isBeingDragged) dragOffsetY else 0f,
              isEditingInline = isEditingInline,
              onScheduleClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                schedulingTaskId = task.id
              },
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
              subtaskDragOffsetY = subtaskDragOffsetY,
              onStartDragSubTask = { subtaskId -> startDraggingSubTask(subtaskId) },
              onDragSubTask = { subtaskId, deltaY -> onDragSubTaskDelta(task.id, subtaskId, deltaY) },
              onEndDragSubTask = { endDraggingSubTask() },
              onSubtaskHeightMeasured = { subtaskId, heightPx ->
                if (heightPx > 0f) {
                  subtaskHeightsPx[subtaskId] = heightPx
                }
              },
              onStartDrag = { startDragging(task.id) },
              onDrag = { deltaY -> onDragDelta(task.id, deltaY) },
              onEndDrag = { endDragging() },
              onItemHeightMeasured = { heightPx ->
                if (heightPx > 0f) {
                  taskHeightsPx[task.id] = heightPx
                }
              },
              modifier = Modifier.animateItem(
                placementSpec = if (isBeingDragged) {
                  null
                } else {
                  spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold,
                  )
                },
              ),
            )
          }
        }
      }
    }

    // Schedule / Reschedule Task Dialog
    val taskToSchedule = schedulingTaskId?.let { id -> tasks.firstOrNull { it.id == id } }
    if (taskToSchedule != null) {
      ScheduleTaskDialog(
        task = taskToSchedule,
        onConfirmSchedule = { startMillis, endMillis, notes ->
          scheduleTask(
            taskId = taskToSchedule.id,
            scheduledAtMillis = startMillis,
            scheduledEndMillis = endMillis,
            notes = notes,
          )
          schedulingTaskId = null
        },
        onUnschedule = {
          unscheduleTask(taskToSchedule.id)
          schedulingTaskId = null
        },
        onDismiss = { schedulingTaskId = null },
      )
    }

    // Completed Tasks Sheet
    if (showCompletedTasksSheet) {
      CompletedTasksSheet(
        completedTasks = completedTasks,
        completedCount = completedTodayCount,
        historyVersion = historyVersion,
        canUndo = canUndo,
        canRedo = canRedo,
        onUndo = { undo() },
        onRedo = { redo() },
        onUncompleteTask = { task -> uncompleteTask(task) },
        onRemoveCompletedTask = { task -> deleteCompletedTask(task) },
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
  historyVersion: Int = 0,
  isBeingDragged: Boolean,
  dragOffsetY: Float,
  isEditingInline: Boolean,
  onScheduleClick: () -> Unit = {},
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
  subtaskDragOffsetY: Float = 0f,
  onStartDragSubTask: (String) -> Unit = {},
  onDragSubTask: (String, Float) -> Unit = { _, _ -> },
  onEndDragSubTask: () -> Unit = {},
  onSubtaskHeightMeasured: (String, Float) -> Unit = { _, _ -> },
  onStartDrag: () -> Unit,
  onDrag: (Float) -> Unit,
  onEndDrag: () -> Unit,
  onItemHeightMeasured: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isSubtaskBeingDragged = draggedSubtaskId != null && task.subtasks.any { it.id == draggedSubtaskId }
  var isDragHandlePressed by remember { mutableStateOf(false) }
  val isSwipeEnabled = !isBeingDragged && !isSubtaskBeingDragged && !isEditingInline && !isDragHandlePressed

  val currentOnComplete by rememberUpdatedState(onComplete)
  val currentOnDelete by rememberUpdatedState(onDelete)

  val dismissState = remember(task.id, historyVersion) {
    SwipeToDismissBoxState(
      initialValue = SwipeToDismissBoxValue.Settled,
      positionalThreshold = { it * 0.35f },
    )
  }
  var hasDismissTriggered by remember(dismissState) { mutableStateOf(false) }

  LaunchedEffect(dismissState, dismissState.currentValue, isSwipeEnabled) {
    if (isSwipeEnabled && !hasDismissTriggered) {
      when (dismissState.currentValue) {
        SwipeToDismissBoxValue.StartToEnd -> {
          hasDismissTriggered = true
          currentOnComplete()
          dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
        SwipeToDismissBoxValue.EndToStart -> {
          hasDismissTriggered = true
          currentOnDelete()
          dismissState.snapTo(SwipeToDismissBoxValue.Settled)
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
    enableDismissFromStartToEnd = isSwipeEnabled,
    enableDismissFromEndToStart = isSwipeEnabled,
    gesturesEnabled = isSwipeEnabled,
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
        dragScale = dragScale,
        isEditingInline = isEditingInline,
        onScheduleClick = onScheduleClick,
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
        subtaskDragOffsetY = subtaskDragOffsetY,
        onStartDragSubTask = onStartDragSubTask,
        onDragSubTask = onDragSubTask,
        onEndDragSubTask = onEndDragSubTask,
        onSubtaskHeightMeasured = onSubtaskHeightMeasured,
        onStartDrag = onStartDrag,
        onDrag = onDrag,
        onEndDrag = onEndDrag,
        onDragHandlePressedChange = { isDragHandlePressed = it },
        onHeightMeasured = onItemHeightMeasured,
      )
    },
    modifier = modifier
      .zIndex(if (isBeingDragged) 2f else if (isSubtaskBeingDragged) 1.5f else 1f)
      .graphicsLayer {
        translationY = if (isBeingDragged) dragOffsetY else 0f
        scaleX = dragScale
        scaleY = dragScale
      }
      .testTag("todo_item_${task.id}"),
  )
}

/**
 * Task card content with circular schedule/reschedule icon button, clickable text to edit,
 * scheduled note label, collapsible subtasks list, and drag handle.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskCardContent(
  task: TodoItem,
  isBeingDragged: Boolean,
  dragScale: Float = 1f,
  isEditingInline: Boolean,
  onScheduleClick: () -> Unit = {},
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
  subtaskDragOffsetY: Float = 0f,
  onStartDragSubTask: (String) -> Unit = {},
  onDragSubTask: (String, Float) -> Unit = { _, _ -> },
  onEndDragSubTask: () -> Unit = {},
  onSubtaskHeightMeasured: (String, Float) -> Unit = { _, _ -> },
  onStartDrag: () -> Unit,
  onDrag: (Float) -> Unit,
  onEndDrag: () -> Unit,
  onDragHandlePressedChange: (Boolean) -> Unit = {},
  onHeightMeasured: (Float) -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val checkScale = remember { Animatable(1f) }
  var newSubtaskInput by remember { mutableStateOf("") }
  var showAddSubtaskInput by remember { mutableStateOf(false) }
  var editingSubtaskId by remember { mutableStateOf<String?>(null) }
  val newSubtaskFocusRequester = remember { FocusRequester() }

  val currentOnScheduleClick by rememberUpdatedState(onScheduleClick)
  val currentOnCheckboxClick by rememberUpdatedState(onCheckboxClick)
  val currentOnSaveAsTemplate by rememberUpdatedState(onSaveAsTemplate)
  val currentOnStartDrag by rememberUpdatedState(onStartDrag)
  val currentOnDrag by rememberUpdatedState(onDrag)
  val currentOnEndDrag by rememberUpdatedState(onEndDrag)
  val currentOnStartDragSubTask by rememberUpdatedState(onStartDragSubTask)
  val currentOnDragSubTask by rememberUpdatedState(onDragSubTask)
  val currentOnEndDragSubTask by rememberUpdatedState(onEndDragSubTask)
  val currentOnDragHandlePressedChange by rememberUpdatedState(onDragHandlePressedChange)
  val currentDragScale by rememberUpdatedState(dragScale)

  LaunchedEffect(isBeingDragged, draggedSubtaskId) {
    if (isBeingDragged || draggedSubtaskId != null) {
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
      .pointerInput(task.id) {
        detectTapGestures(
          onDoubleTap = { currentOnSaveAsTemplate() },
        )
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
          .height(IntrinsicSize.Min)
          .heightIn(min = 60.dp)
          .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Drag Handle to reorder tasks (on the left side) - Triple bar
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .pointerInput(task.id) {
              awaitPointerEventScope {
                while (true) {
                  val event = awaitPointerEvent(PointerEventPass.Initial)
                  currentOnDragHandlePressedChange(event.changes.any { it.pressed })
                }
              }
            }
            .pointerInput(task.id) {
              var draggingThis = false
              try {
                detectDragGestures(
                  onDragStart = {
                    draggingThis = true
                    currentOnStartDrag()
                  },
                  onDrag = { change, dragAmount ->
                    change.consume()
                    currentOnDrag(dragAmount.y * currentDragScale)
                  },
                  onDragEnd = {
                    draggingThis = false
                    currentOnDragHandlePressedChange(false)
                    currentOnEndDrag()
                  },
                  onDragCancel = {
                    draggingThis = false
                    currentOnDragHandlePressedChange(false)
                    currentOnEndDrag()
                  },
                )
              } finally {
                if (draggingThis) {
                  currentOnDragHandlePressedChange(false)
                  currentOnEndDrag()
                }
              }
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

        // Circular Schedule / Reschedule icon button (replaces the check circle)
        Box(
          modifier = Modifier
            .size(34.dp)
            .scale(checkScale.value)
            .clip(CircleShape)
            .background(
              if (task.isScheduled) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
              },
              CircleShape,
            )
            .border(
              BorderStroke(
                1.dp,
                if (task.isScheduled) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.outlineVariant
                },
              ),
              CircleShape,
            )
            .clickable {
              currentOnScheduleClick()
            }
            .testTag("todo_item_schedule_button_${task.id}"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (task.isScheduled) Icons.Default.Update else Icons.Default.Schedule,
            contentDescription = if (task.isScheduled) "Reschedule task" else "Schedule task",
            tint = if (task.isScheduled) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(19.dp),
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
            decorationBox = { innerTextField ->
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(vertical = 12.dp, horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart,
              ) {
                innerTextField()
              }
            },
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .heightIn(min = 60.dp)
              .focusRequester(focusRequester)
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  hasBeenFocused = true
                } else if (hasBeenFocused) {
                  onSaveInlineEdit(textFieldValue.text)
                }
              }
              .testTag("todo_edit_title"),
          )
        } else {
          var showContextMenu by remember { mutableStateOf(false) }

          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .heightIn(min = 60.dp)
              .clip(RoundedCornerShape(8.dp))
              .combinedClickable(
                onClick = onStartInlineEdit,
                onDoubleClick = { currentOnSaveAsTemplate() },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  showContextMenu = true
                },
              )
              .padding(vertical = 12.dp, horizontal = 4.dp)
              .testTag("todo_item_title_${task.id}"),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
          ) {
            Text(
              text = task.title,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )

            if (task.notes.isNotBlank()) {
              Text(
                text = task.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("todo_item_notes_${task.id}"),
              )
            }

            if (task.scheduledAtMillis != null) {
              val isDue = task.isDue()
              val formattedSchedule = TodoListEngine.formatScheduleLabel(
                scheduledAtMillis = task.scheduledAtMillis,
                scheduledEndMillis = task.scheduledEndMillis,
              )
              Surface(
                color = if (isDue) {
                  MaterialTheme.colorScheme.tertiaryContainer
                } else {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                },
                shape = CircleShape,
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable { currentOnScheduleClick() }
                  .testTag("todo_item_schedule_label_${task.id}"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = if (isDue) Icons.Default.NotificationsActive else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isDue) {
                      MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                      MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.size(13.dp),
                  )
                  Text(
                    text = if (isDue) "Scheduled (Due) • $formattedSchedule" else "Scheduled • $formattedSchedule",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDue) {
                      MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                      MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }

            DropdownMenu(
              expanded = showContextMenu,
              onDismissRequest = { showContextMenu = false },
            ) {
              DropdownMenuItem(
                text = { Text(if (task.isScheduled) "Reschedule" else "Schedule") },
                leadingIcon = {
                  Icon(
                    imageVector = if (task.isScheduled) Icons.Default.Update else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                  )
                },
                onClick = {
                  showContextMenu = false
                  currentOnScheduleClick()
                },
                modifier = Modifier.testTag("todo_menu_schedule_${task.id}"),
              )
              DropdownMenuItem(
                text = { Text("Complete Task") },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                  )
                },
                onClick = {
                  showContextMenu = false
                  currentOnCheckboxClick()
                },
                modifier = Modifier.testTag("todo_menu_complete_${task.id}"),
              )
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

      // Subtasks Section (hidden when collapsed)
      AnimatedVisibility(
        visible = !task.isSubtasksCollapsed && (task.subtasks.isNotEmpty() || showAddSubtaskInput),
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
            key(subtask.id) {
              val isSubtaskBeingDragged = draggedSubtaskId == subtask.id
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(IntrinsicSize.Min)
                  .heightIn(min = 40.dp)
                  .zIndex(if (isSubtaskBeingDragged) 2f else 0f)
                  .graphicsLayer {
                    translationY = if (isSubtaskBeingDragged) subtaskDragOffsetY else 0f
                  }
                  .onGloballyPositioned { coords ->
                    val height = coords.size.height.toFloat()
                    if (height > 0f) {
                      onSubtaskHeightMeasured(subtask.id, height)
                    }
                  }
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (isSubtaskBeingDragged) {
                      MaterialTheme.colorScheme.surfaceContainerHigh
                    } else {
                      Color.Transparent
                    },
                  ),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                // Smaller drag handle with double bar
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .pointerInput(subtask.id) {
                      awaitPointerEventScope {
                        while (true) {
                          val event = awaitPointerEvent(PointerEventPass.Initial)
                          currentOnDragHandlePressedChange(event.changes.any { it.pressed })
                        }
                      }
                    }
                    .pointerInput(subtask.id) {
                      var draggingThisSubtask = false
                      try {
                        detectDragGestures(
                          onDragStart = {
                            draggingThisSubtask = true
                            currentOnStartDragSubTask(subtask.id)
                          },
                          onDrag = { change, dragAmount ->
                            change.consume()
                            currentOnDragSubTask(subtask.id, dragAmount.y * currentDragScale)
                          },
                          onDragEnd = {
                            draggingThisSubtask = false
                            currentOnDragHandlePressedChange(false)
                            currentOnEndDragSubTask()
                          },
                          onDragCancel = {
                            draggingThisSubtask = false
                            currentOnDragHandlePressedChange(false)
                            currentOnEndDragSubTask()
                          },
                        )
                      } finally {
                        if (draggingThisSubtask) {
                          currentOnDragHandlePressedChange(false)
                          currentOnEndDragSubTask()
                        }
                      }
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
                    decorationBox = { innerTextField ->
                      Box(
                        modifier = Modifier
                          .fillMaxSize()
                          .padding(vertical = 6.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.CenterStart,
                      ) {
                        innerTextField()
                      }
                    },
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxHeight()
                      .heightIn(min = 40.dp)
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
                      .testTag("todo_subtask_edit_${subtask.id}"),
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxHeight()
                      .heightIn(min = 40.dp)
                      .clip(RoundedCornerShape(4.dp))
                      .clickable { editingSubtaskId = subtask.id }
                      .padding(vertical = 6.dp, horizontal = 2.dp)
                      .testTag("todo_subtask_title_${subtask.id}"),
                    contentAlignment = Alignment.CenterStart,
                  ) {
                    Text(
                      text = subtask.title,
                      style = MaterialTheme.typography.bodyMedium,
                      color = if (subtask.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      },
                      textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    )
                  }
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
          }

          // Subtask input (shown when user presses + button, automatically focused)
          AnimatedVisibility(
            visible = showAddSubtaskInput,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .heightIn(min = 40.dp),
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
                  .fillMaxHeight()
                  .heightIn(min = 40.dp)
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
                modifier = Modifier.size(40.dp),
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
        .height(IntrinsicSize.Min)
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
          .fillMaxHeight()
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
 * Modal sheet displaying tasks completed today, supporting swipe-to-remove.
 * Subtasks are collapsed by default, but can be uncollapsed by tapping the indicator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompletedTasksSheet(
  completedTasks: List<TodoItem>,
  completedCount: Int,
  historyVersion: Int = 0,
  canUndo: Boolean = false,
  canRedo: Boolean = false,
  onUndo: () -> Unit = {},
  onRedo: () -> Unit = {},
  onUncompleteTask: (TodoItem) -> Unit = {},
  onRemoveCompletedTask: (TodoItem) -> Unit = {},
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  // Subtasks are collapsed by default, but can be uncollapsed per task
  val uncollapsedTaskIds = remember { mutableStateMapOf<String, Boolean>() }
  val currentOnUncompleteTask by rememberUpdatedState(onUncompleteTask)
  val currentOnRemoveCompletedTask by rememberUpdatedState(onRemoveCompletedTask)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    dragHandle = {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        BottomSheetDefaults.DragHandle()
      }
    },
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
          onClick = onUndo,
          enabled = canUndo,
          modifier = Modifier
            .size(32.dp)
            .testTag("todo_completed_tasks_undo"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Undo,
            contentDescription = "Undo",
            tint = if (canUndo) {
              MaterialTheme.colorScheme.onSurface
            } else {
              MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier.size(20.dp),
          )
        }
        IconButton(
          onClick = onRedo,
          enabled = canRedo,
          modifier = Modifier
            .size(32.dp)
            .testTag("todo_completed_tasks_redo"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Redo,
            contentDescription = "Redo",
            tint = if (canRedo) {
              MaterialTheme.colorScheme.onSurface
            } else {
              MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier.size(20.dp),
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
            val dismissState = remember(task.id, historyVersion) {
              SwipeToDismissBoxState(
                initialValue = SwipeToDismissBoxValue.Settled,
                positionalThreshold = { it * 0.35f },
              )
            }
            var hasDismissTriggered by remember(dismissState) { mutableStateOf(false) }

            LaunchedEffect(dismissState, dismissState.currentValue) {
              if (!hasDismissTriggered) {
                when (dismissState.currentValue) {
                  SwipeToDismissBoxValue.StartToEnd,
                  SwipeToDismissBoxValue.EndToStart -> {
                    hasDismissTriggered = true
                    currentOnRemoveCompletedTask(task)
                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                  }
                  SwipeToDismissBoxValue.Settled -> {}
                }
              }
            }

            SwipeToDismissBox(
              state = dismissState,
              enableDismissFromStartToEnd = true,
              enableDismissFromEndToStart = true,
              backgroundContent = {
                val direction = dismissState.dismissDirection
                val isSwiping = direction == SwipeToDismissBoxValue.StartToEnd ||
                  direction == SwipeToDismissBoxValue.EndToStart

                val backgroundColor by animateColorAsState(
                  targetValue = if (isSwiping) MaterialTheme.colorScheme.error else Color.Transparent,
                  label = "completed_swipe_bg_${task.id}",
                )

                val iconScale by animateFloatAsState(
                  targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.25f else 0.85f,
                  animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                  label = "completed_swipe_icon_scale_${task.id}",
                )

                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp),
                  contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                  } else {
                    Alignment.CenterEnd
                  },
                ) {
                  if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      modifier = Modifier.scale(iconScale),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(22.dp),
                      )
                      Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                  } else if (direction == SwipeToDismissBoxValue.EndToStart) {
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
                        modifier = Modifier.size(22.dp),
                      )
                    }
                  }
                }
              },
              content = {
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("todo_completed_item_card_${task.id}"),
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                  ),
                  elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 10.dp),
                  ) {
                    // Task row
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      // Checked icon (tap to uncomplete task)
                      Box(
                        modifier = Modifier
                          .size(32.dp)
                          .clip(CircleShape)
                          .clickable { currentOnUncompleteTask(task) }
                          .testTag("todo_completed_item_checkbox_${task.id}"),
                        contentAlignment = Alignment.Center,
                      ) {
                        Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = "Uncomplete task",
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(22.dp),
                        )
                      }

                      Spacer(modifier = Modifier.width(6.dp))

                      // Task title with strikethrough
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
              },
              modifier = Modifier
                .fillMaxWidth()
                .animateItem()
                .testTag("todo_completed_item_${task.id}"),
            )
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
    dragHandle = {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        BottomSheetDefaults.DragHandle()
      }
    },
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
          .height(IntrinsicSize.Min)
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
            .fillMaxHeight()
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
                  modifier = Modifier
                    .size(28.dp)
                    .testTag("todo_saved_item_delete_${savedTask.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete template",
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
}

/**
 * In-app notification card that pops up when a scheduled note's date/time is reached,
 * displaying the note title and contents alongside quick Reschedule and Dismiss actions.
 */
@Composable
private fun DueNotificationPopupCard(
  task: TodoItem,
  onReschedule: () -> Unit,
  onDismiss: () -> Unit,
) {
  val contentBody = task.buildNotificationContent()
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag("todo_due_notification_banner"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.tertiaryContainer,
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiary),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.size(20.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Scheduled Note Reached",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
          )
          Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.testTag("todo_due_notification_title"),
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(28.dp)
            .testTag("todo_due_notification_dismiss"),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss notification",
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      Text(
        text = contentBody,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.testTag("todo_due_notification_content"),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(
          onClick = onReschedule,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("todo_due_notification_reschedule"),
        ) {
          Icon(
            imageVector = Icons.Default.Update,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Reschedule",
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

private fun localMillisToUtcDateMillis(localMillis: Long): Long {
  val localCal = Calendar.getInstance().apply { timeInMillis = localMillis }
  val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
    clear()
    set(
      localCal.get(Calendar.YEAR),
      localCal.get(Calendar.MONTH),
      localCal.get(Calendar.DAY_OF_MONTH),
      0,
      0,
      0,
    )
  }
  return utcCal.timeInMillis
}

private fun applyUtcDateToLocalMillis(utcDateMillis: Long, existingLocalMillis: Long): Long {
  val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
    timeInMillis = utcDateMillis
  }
  val localCal = Calendar.getInstance().apply {
    timeInMillis = existingLocalMillis
    set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
    set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
    set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }
  return localCal.timeInMillis
}

private fun applyHourMinuteToLocalMillis(hourOfDay: Int, minute: Int, existingLocalMillis: Long): Long {
  val localCal = Calendar.getInstance().apply {
    timeInMillis = existingLocalMillis
    set(Calendar.HOUR_OF_DAY, hourOfDay)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }
  return localCal.timeInMillis
}

/**
 * Dialog for scheduling or rescheduling a [TodoItem] across variable date and time ranges,
 * with quick presets, custom date/time pickers, optional end date/time range, and note contents.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTaskDialog(
  task: TodoItem,
  onConfirmSchedule: (startMillis: Long, endMillis: Long?, notes: String) -> Unit,
  onUnschedule: () -> Unit,
  onDismiss: () -> Unit,
) {
  val defaultStart = remember(task.id) {
    task.scheduledAtMillis ?: (System.currentTimeMillis() + 3_600_000L)
  }
  var startMillis by remember(task.id) { mutableLongStateOf(defaultStart) }
  var hasEndRange by remember(task.id) {
    mutableStateOf(task.scheduledEndMillis != null && task.scheduledEndMillis > defaultStart)
  }
  var endMillis by remember(task.id) {
    mutableLongStateOf(task.scheduledEndMillis ?: (defaultStart + 3_600_000L))
  }
  var notesText by remember(task.id) { mutableStateOf(task.notes) }

  var showStartDatePicker by remember { mutableStateOf(false) }
  var showStartTimePicker by remember { mutableStateOf(false) }
  var showEndDatePicker by remember { mutableStateOf(false) }
  var showEndTimePicker by remember { mutableStateOf(false) }

  val dateFormatter = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.US) }
  val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.US) }

  fun updateStart(newStart: Long) {
    val duration = if (hasEndRange) (endMillis - startMillis).coerceAtLeast(1_800_000L) else 3_600_000L
    startMillis = newStart
    if (hasEndRange && endMillis <= newStart) {
      endMillis = newStart + duration
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("todo_schedule_dialog"),
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
            imageVector = if (task.isScheduled) Icons.Default.Update else Icons.Default.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = if (task.isScheduled) "Reschedule Note" else "Schedule Note",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Set date, time range, and notification contents",
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Context Preview Card
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
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = task.title,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Scheduled: ${
                TodoListEngine.formatScheduleLabel(
                  scheduledAtMillis = startMillis,
                  scheduledEndMillis = if (hasEndRange) endMillis else null,
                )
              }",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.testTag("todo_schedule_preview_label"),
            )
          }
        }

        // Quick Relative Time Presets
        Text(
          text = "Quick Presets",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          val presets = listOf(
            "In 1 min" to 60_000L,
            "In 15 min" to 15 * 60_000L,
            "In 1 hour" to 3_600_000L,
            "In 3 hours" to 3 * 3_600_000L,
            "In 1 day" to 24 * 3_600_000L,
            "In 1 week" to 7 * 24 * 3_600_000L,
          )
          presets.forEach { (label, offsetMs) ->
            SuggestionChip(
              onClick = {
                updateStart(System.currentTimeMillis() + offsetMs)
              },
              label = { Text(label, style = MaterialTheme.typography.labelSmall) },
              shape = CircleShape,
            )
          }
        }

        // Variable Offset Adjusters
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          val adjustments = listOf(
            "-15m" to -15 * 60_000L,
            "+15m" to 15 * 60_000L,
            "+1h" to 3_600_000L,
            "+1d" to 24 * 3_600_000L,
          )
          adjustments.forEach { (label, deltaMs) ->
            OutlinedButton(
              onClick = { updateStart(startMillis + deltaMs) },
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
              Text(label, style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        // Start Date & Time Pickers
        Text(
          text = if (hasEndRange) "Start Date & Time" else "Date & Time",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = { showStartDatePicker = true },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("todo_schedule_start_date_button"),
          ) {
            Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = dateFormatter.format(Date(startMillis)),
              style = MaterialTheme.typography.labelMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          OutlinedButton(
            onClick = { showStartTimePicker = true },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .testTag("todo_schedule_start_time_button"),
          ) {
            Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = timeFormatter.format(Date(startMillis)),
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }

        // Toggle Variable Date & Time Range (End Date & Time)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          FilterChip(
            selected = hasEndRange,
            onClick = {
              hasEndRange = !hasEndRange
              if (hasEndRange && endMillis <= startMillis) {
                endMillis = startMillis + 3_600_000L
              }
            },
            label = { Text("Time Range (End Date & Time)") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            },
            shape = CircleShape,
            modifier = Modifier.testTag("todo_schedule_range_toggle"),
          )
        }

        AnimatedVisibility(visible = hasEndRange) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "End Date & Time Range",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Quick range durations
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              val durations = listOf(
                "30m" to 30 * 60_000L,
                "1h" to 3_600_000L,
                "2h" to 2 * 3_600_000L,
                "4h" to 4 * 3_600_000L,
                "1d" to 24 * 3_600_000L,
              )
              durations.forEach { (label, durationMs) ->
                SuggestionChip(
                  onClick = { endMillis = startMillis + durationMs },
                  label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                  shape = CircleShape,
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedButton(
                onClick = { showEndDatePicker = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("todo_schedule_end_date_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = dateFormatter.format(Date(endMillis)),
                  style = MaterialTheme.typography.labelMedium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              OutlinedButton(
                onClick = { showEndTimePicker = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .testTag("todo_schedule_end_time_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.AccessTime,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = timeFormatter.format(Date(endMillis)),
                  style = MaterialTheme.typography.labelMedium,
                )
              }
            }
          }
        }

        // Note Contents input for notification body
        OutlinedTextField(
          value = notesText,
          onValueChange = { notesText = it },
          label = { Text("Note contents") },
          placeholder = { Text("Add details to display in notification...") },
          shape = RoundedCornerShape(14.dp),
          minLines = 2,
          maxLines = 4,
          trailingIcon = {
            if (notesText.isNotEmpty()) {
              IconButton(onClick = { notesText = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear contents")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("todo_schedule_notes_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirmSchedule(
            startMillis,
            if (hasEndRange && endMillis > startMillis) endMillis else null,
            notesText,
          )
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("todo_schedule_confirm_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (task.isScheduled) "Reschedule" else "Schedule")
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (task.isScheduled) {
          TextButton(
            onClick = onUnschedule,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("todo_schedule_clear_button"),
          ) {
            Text("Unschedule", color = MaterialTheme.colorScheme.error)
          }
        }
        TextButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("todo_schedule_cancel_button"),
        ) {
          Text("Cancel")
        }
      }
    },
  )

  if (showStartDatePicker) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = localMillisToUtcDateMillis(startMillis),
    )
    DatePickerDialog(
      onDismissRequest = { showStartDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { utcMillis ->
              updateStart(applyUtcDateToLocalMillis(utcMillis, startMillis))
            }
            showStartDatePicker = false
          },
        ) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(onClick = { showStartDatePicker = false }) {
          Text("Cancel")
        }
      },
    ) {
      DatePicker(state = datePickerState)
    }
  }

  if (showStartTimePicker) {
    val cal = remember(startMillis) { Calendar.getInstance().apply { timeInMillis = startMillis } }
    val timePickerState = rememberTimePickerState(
      initialHour = cal.get(Calendar.HOUR_OF_DAY),
      initialMinute = cal.get(Calendar.MINUTE),
    )
    AlertDialog(
      onDismissRequest = { showStartTimePicker = false },
      shape = RoundedCornerShape(28.dp),
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      title = { Text("Select Start Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
      text = {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          TimePicker(state = timePickerState)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            updateStart(applyHourMinuteToLocalMillis(timePickerState.hour, timePickerState.minute, startMillis))
            showStartTimePicker = false
          },
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(onClick = { showStartTimePicker = false }) {
          Text("Cancel")
        }
      },
    )
  }

  if (showEndDatePicker) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = localMillisToUtcDateMillis(endMillis),
    )
    DatePickerDialog(
      onDismissRequest = { showEndDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { utcMillis ->
              val candidate = applyUtcDateToLocalMillis(utcMillis, endMillis)
              endMillis = maxOf(startMillis + 60_000L, candidate)
            }
            showEndDatePicker = false
          },
        ) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEndDatePicker = false }) {
          Text("Cancel")
        }
      },
    ) {
      DatePicker(state = datePickerState)
    }
  }

  if (showEndTimePicker) {
    val cal = remember(endMillis) { Calendar.getInstance().apply { timeInMillis = endMillis } }
    val timePickerState = rememberTimePickerState(
      initialHour = cal.get(Calendar.HOUR_OF_DAY),
      initialMinute = cal.get(Calendar.MINUTE),
    )
    AlertDialog(
      onDismissRequest = { showEndTimePicker = false },
      shape = RoundedCornerShape(28.dp),
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      title = { Text("Select End Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
      text = {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          TimePicker(state = timePickerState)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val candidate = applyHourMinuteToLocalMillis(timePickerState.hour, timePickerState.minute, endMillis)
            endMillis = if (candidate <= startMillis) candidate + 24 * 3_600_000L else candidate
            showEndTimePicker = false
          },
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEndTimePicker = false }) {
          Text("Cancel")
        }
      },
    )
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
