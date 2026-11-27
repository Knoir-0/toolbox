package com.example.toolbox.ui.tools

import com.example.toolbox.ui.theme.toolboxScreenBackground

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import com.example.toolbox.ui.theme.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import com.example.toolbox.ui.theme.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.ModalBottomSheet
import com.example.toolbox.ui.theme.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import com.example.toolbox.ui.theme.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

private enum class TodoFilterTab {
  TASKS,
  SCHEDULED,
  COMPLETED,
}

internal class TodoItemActionDispatcher(
  val onUnscheduleTask: (TodoItem) -> Unit = {},
  val onDeleteTask: (TodoItem) -> Unit = {},
  val onCopyTask: (TodoItem) -> Unit = {},
  val onShareTask: (TodoItem) -> Unit = {},
)

internal val LocalTodoItemActions = compositionLocalOf { TodoItemActionDispatcher() }

private fun formatTodoTaskPlainText(task: TodoItem): String {
  return buildString {
    appendLine(task.toText())
    if (task.notes.isNotBlank()) {
      appendLine(task.notes)
    }
    if (task.scheduledAtMillis != null) {
      val sched = TodoListEngine.formatScheduleLabel(task.scheduledAtMillis, task.scheduledEndMillis)
      val repeatSuffix = task.recurrenceRule?.let { " (${it.formatLabel()})" } ?: ""
      appendLine("Scheduled: $sched$repeatSuffix")
    }
  }.trim()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodoListScreen(
  modifier: Modifier = Modifier,
  repository: TodoListRepository? = null,
  initialFocusAddTask: Boolean = false,
  initialOpenSchedule: Boolean = false,
  initialGrantedFolderPaths: Set<String>? = null,
  onRequestFolderSystemAccess: ((TodoFolder, (Boolean) -> Unit) -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current

  val repo = remember(repository) {
    repository ?: SharedPreferencesTodoListRepository(context)
  }

  var grantedFolderPaths by remember(repo, initialGrantedFolderPaths, isInspection) {
    mutableStateOf(
      when {
        initialGrantedFolderPaths != null -> initialGrantedFolderPaths
        isInspection -> setOf(DEFAULT_TODO_SCOPED_FOLDER.displayPath)
        else -> repo.getGrantedFolderPaths()
      }
    )
  }
  val isTodoFolderGranted = remember(grantedFolderPaths) {
    TodoListEngine.isTodoFolderAccessGranted(grantedFolderPaths)
  }
  var pendingPermissionFolderPath by rememberSaveable {
    mutableStateOf<String?>(null)
  }
  var folderAwaitingSystemResult by remember {
    mutableStateOf<TodoFolder?>(null)
  }

  var tasks by remember { mutableStateOf(repo.loadTasks()) }
  var completedTodayCount by remember { mutableIntStateOf(repo.getCompletedTodayCount(getTodayDateKey())) }
  var completedTasks by remember { mutableStateOf(repo.loadCompletedTasks(getTodayDateKey())) }
  var selectedTab by remember {
    mutableStateOf(if (initialOpenSchedule) TodoFilterTab.SCHEDULED else TodoFilterTab.TASKS)
  }
  var showCompletedTasksSheet by remember { mutableStateOf(false) }

  var savedTasks by remember { mutableStateOf(repo.loadSavedTasks()) }
  var showSavedTasksSheet by remember { mutableStateOf(false) }

  fun grantTodoFolderAccess(folder: TodoFolder = DEFAULT_TODO_SCOPED_FOLDER) {
    val updated = buildSet {
      addAll(grantedFolderPaths)
      add(folder.displayPath)
      add(DEFAULT_TODO_SCOPED_FOLDER.displayPath)
    }
    grantedFolderPaths = updated
    repo.setGrantedFolderPaths(updated)
    pendingPermissionFolderPath = null
    val today = getTodayDateKey()
    tasks = repo.loadTasks()
    completedTodayCount = repo.getCompletedTodayCount(today)
    completedTasks = repo.loadCompletedTasks(today)
    savedTasks = repo.loadSavedTasks()
    if (tasks.isNotEmpty() || completedTasks.isNotEmpty()) {
      repo.saveTasks(tasks)
      repo.saveCompletedTasks(completedTasks, today)
    }
  }

  val folderAccessLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    val targetFolder = folderAwaitingSystemResult ?: DEFAULT_TODO_SCOPED_FOLDER
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
      grantTodoFolderAccess(targetFolder)
    }
  }

  fun requestSystemFolderAccess(folder: TodoFolder = DEFAULT_TODO_SCOPED_FOLDER) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    if (onRequestFolderSystemAccess != null) {
      onRequestFolderSystemAccess(folder) { granted ->
        if (granted) {
          grantTodoFolderAccess(folder)
        }
      }
      return
    }
    try {
      folderAwaitingSystemResult = folder
      folderAccessLauncher.launch(
        TodoListEngine.createFolderAccessIntent(
          context = context,
          folder = folder,
        )
      )
    } catch (_: Throwable) {
      folderAwaitingSystemResult = null
      grantTodoFolderAccess(folder)
    }
  }

  fun requestTodoFolderPermissionPrompt() {
    pendingPermissionFolderPath = DEFAULT_TODO_SCOPED_FOLDER.displayPath
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
  }

  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }

  // Multi-selection state (Section 5.5 of DESIGN_LANGUAGE.md)
  var selectedTaskIds by remember { mutableStateOf(setOf<String>()) }
  val isSelectionMode = selectedTaskIds.isNotEmpty()
  var isSchedulingBulk by remember { mutableStateOf(false) }

  var undoStack by remember { mutableStateOf(listOf<TodoHistorySnapshot>()) }
  var redoStack by remember { mutableStateOf(listOf<TodoHistorySnapshot>()) }
  var historyVersion by remember { mutableIntStateOf(0) }

  val canUndo = undoStack.isNotEmpty()
  val canRedo = redoStack.isNotEmpty()

  // Quick Add State (title only)
  var newTaskTitle by remember { mutableStateOf("") }
  val taskInputFocusRequester = remember { FocusRequester() }

  // Inline edit state (task ID currently being edited inline)
  var inlineEditingTaskId by remember { mutableStateOf<String?>(null) }

  // Schedule / Reschedule panel state
  var showSchedulePanel by remember { mutableStateOf(false) }
  var schedulingTaskId by remember { mutableStateOf<String?>(null) }
  var scheduleStartMillis by remember { mutableLongStateOf(System.currentTimeMillis() + 3_600_000L) }
  var scheduleRecurrenceRule by remember { mutableStateOf<TodoRecurrenceRule?>(null) }
  var selectedSchedulePreset by remember {
    mutableStateOf(TodoRecurrenceRule(interval = 1, unit = TodoRecurrenceUnit.HOUR, presetLabel = "1 hr"))
  }
  var hasPendingSchedule by remember { mutableStateOf(false) }

  // Active in-app due notification popups
  var duePopupTasks by remember { mutableStateOf<List<TodoItem>>(emptyList()) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { _ -> }

  // Drag-and-drop reorder state
  val density = LocalDensity.current
  val listState = rememberLazyListState()
  var scrollToTopTrigger by remember { mutableIntStateOf(0) }

  fun scrollTaskListToTop() {
    listState.requestScrollToItem(0)
    scrollToTopTrigger += 1
  }

  LaunchedEffect(scrollToTopTrigger) {
    if (scrollToTopTrigger > 0) {
      listState.scrollToItem(0)
    }
  }
  val taskSpacingPx = with(density) { 8.dp.toPx() }
  val defaultTaskHeightPx = with(density) { 52.dp.toPx() }
  val taskMaxOverscrollPx = with(density) { 12.dp.toPx() }
  val subtaskSpacingPx = with(density) { 6.dp.toPx() }
  val defaultSubtaskHeightPx = with(density) { 40.dp.toPx() }
  val subtaskMaxOverscrollPx = with(density) { 4.dp.toPx() }
  val autoScrollEdgeThresholdPx = with(density) { 56.dp.toPx() }
  val maxAutoScrollStepPx = with(density) { 14.dp.toPx() }

  var taskListCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  var quickAddBarCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  var draggedTaskId by remember { mutableStateOf<String?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  var initialDragSnapshot by remember { mutableStateOf<TodoHistorySnapshot?>(null) }
  var dragPointerRootY by remember { mutableStateOf<Float?>(null) }
  var dragTopEdgeTriggerY by remember { mutableStateOf<Float?>(null) }
  var dragBottomEdgeTriggerY by remember { mutableStateOf<Float?>(null) }
  var autoScrollSpeedPx by remember { mutableFloatStateOf(0f) }
  val taskHeightsPx = remember { mutableStateMapOf<String, Float>() }
  val subtaskHeightsPx = remember { mutableStateMapOf<String, Float>() }
  var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

  // Check midnight reset periodically / on resume
  val lifecycleOwner = LocalLifecycleOwner.current
  androidx.compose.runtime.DisposableEffect(lifecycleOwner, repo) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        val latestTasks = repo.loadTasks()
        val latestCompletedToday = repo.getCompletedTodayCount(getTodayDateKey())
        val latestCompletedTasks = repo.loadCompletedTasks(getTodayDateKey())
        if (latestTasks != tasks ||
          latestCompletedToday != completedTodayCount ||
          latestCompletedTasks != completedTasks
        ) {
          tasks = latestTasks
          completedTodayCount = latestCompletedToday
          completedTasks = latestCompletedTasks
          undoStack = TodoListWidgets.loadUndoStack(context)
          redoStack = TodoListWidgets.loadRedoStack(context)
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  LaunchedEffect(Unit) {
    completedTodayCount = repo.getCompletedTodayCount(getTodayDateKey())
    completedTasks = repo.loadCompletedTasks(getTodayDateKey())
    TodoNotificationHelper.ensureNotificationChannel(context)
  }

  LaunchedEffect(
    initialFocusAddTask,
    initialOpenSchedule,
    TodoListEngine.pendingQuickActionRequest,
    isTodoFolderGranted,
  ) {
    if (!isTodoFolderGranted) return@LaunchedEffect
    val pending = TodoListEngine.consumePendingQuickActionRequest()
    if (pending != null || initialFocusAddTask || initialOpenSchedule) {
      val openSchedule = pending?.openSchedule ?: initialOpenSchedule
      val focusAddTask = if (pending != null) !pending.openSchedule else initialFocusAddTask
      selectedTaskIds = emptySet()
      inlineEditingTaskId = null
      if (openSchedule) {
        selectedTab = TodoFilterTab.SCHEDULED
        schedulingTaskId = null
        isSchedulingBulk = false
        if (!hasPendingSchedule) {
          scheduleStartMillis = System.currentTimeMillis() + 3_600_000L
          scheduleRecurrenceRule = null
          selectedSchedulePreset = TodoRecurrenceRule(
            interval = 1,
            unit = TodoRecurrenceUnit.HOUR,
            presetLabel = "1 hr",
          )
        }
        showSchedulePanel = true
      }
      if (focusAddTask) {
        if (selectedTab == TodoFilterTab.COMPLETED) {
          selectedTab = TodoFilterTab.TASKS
        }
        try {
          taskInputFocusRequester.requestFocus()
        } catch (_: Throwable) {}
      }
    }
  }

  // Synchronize background alarms and poll for due tasks every second while screen is active
  LaunchedEffect(tasks) {
    TodoNotificationHelper.syncTaskAlarms(context, tasks)
    while (true) {
      val now = System.currentTimeMillis()
      if (TodoListEngine.getMainViewTasks(tasks, now) != TodoListEngine.getMainViewTasks(tasks, nowMillis)) {
        nowMillis = now
      }
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

  fun syncAlarmsForTaskUpdate(previousTasks: List<TodoItem>, nextTasks: List<TodoItem>) {
    TodoListEngine.getTaskIdsToCancelAlarms(previousTasks, nextTasks).forEach { id ->
      TodoNotificationHelper.cancelTaskAlarm(context, id)
    }
    val activeScheduledIds = nextTasks.filter { it.isScheduled }.map { it.id }.toSet()
    duePopupTasks = duePopupTasks.filter { it.id in activeScheduledIds }
    if (schedulingTaskId != null && nextTasks.none { it.id == schedulingTaskId }) {
      schedulingTaskId = null
      showSchedulePanel = false
    }
    TodoNotificationHelper.syncTaskAlarms(context, nextTasks)
  }

  fun applyAction(
    updatedTasks: List<TodoItem>,
    updatedCompletedToday: Int = completedTodayCount,
    updatedCompletedTasks: List<TodoItem> = completedTasks,
  ) {
    val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    val nextSnapshot = TodoHistorySnapshot(updatedTasks, updatedCompletedToday, updatedCompletedTasks)
    if (currentSnapshot == nextSnapshot) return
    val previousTasks = tasks
    undoStack = listOf(currentSnapshot) + undoStack.take(49)
    redoStack = emptyList()
    historyVersion += 1
    nowMillis = System.currentTimeMillis()

    tasks = updatedTasks
    completedTodayCount = updatedCompletedToday
    completedTasks = updatedCompletedTasks
    val validIds = (updatedTasks.map { it.id } + updatedCompletedTasks.map { it.id }).toSet()
    selectedTaskIds = selectedTaskIds.intersect(validIds)

    repo.saveTasks(updatedTasks)
    repo.setCompletedTodayCount(updatedCompletedToday, getTodayDateKey())
    repo.saveCompletedTasks(updatedCompletedTasks, getTodayDateKey())
    syncAlarmsForTaskUpdate(previousTasks, updatedTasks)
    TodoListWidgets.saveUndoRedoHistory(context, undoStack, redoStack)
    TodoListWidgets.updateAllWidgets(context)
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
    val previousTasks = tasks
    val previousState = undoStack.first()
    undoStack = undoStack.drop(1)
    redoStack = listOf(TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)) + redoStack
    historyVersion += 1
    nowMillis = System.currentTimeMillis()

    tasks = previousState.tasks
    completedTodayCount = previousState.completedToday
    completedTasks = previousState.completedTasks

    repo.saveTasks(previousState.tasks)
    repo.setCompletedTodayCount(previousState.completedToday, getTodayDateKey())
    repo.saveCompletedTasks(previousState.completedTasks, getTodayDateKey())
    syncAlarmsForTaskUpdate(previousTasks, previousState.tasks)
    TodoListWidgets.saveUndoRedoHistory(context, undoStack, redoStack)
    TodoListWidgets.updateAllWidgets(context)
  }

  fun redo() {
    if (redoStack.isEmpty()) return
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val previousTasks = tasks
    val nextState = redoStack.first()
    redoStack = redoStack.drop(1)
    undoStack = listOf(TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)) + undoStack
    historyVersion += 1
    nowMillis = System.currentTimeMillis()

    tasks = nextState.tasks
    completedTodayCount = nextState.completedToday
    completedTasks = nextState.completedTasks

    repo.saveTasks(nextState.tasks)
    repo.setCompletedTodayCount(nextState.completedToday, getTodayDateKey())
    repo.saveCompletedTasks(nextState.completedTasks, getTodayDateKey())
    syncAlarmsForTaskUpdate(previousTasks, nextState.tasks)
    TodoListWidgets.saveUndoRedoHistory(context, undoStack, redoStack)
    TodoListWidgets.updateAllWidgets(context)
  }

  fun completeTask(task: TodoItem) {
    val now = System.currentTimeMillis()
    val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    val nextSnapshot = TodoListEngine.completeTask(currentSnapshot, task.id, now)
    if (nextSnapshot == currentSnapshot) return
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    applySnapshot(nextSnapshot)
    val rescheduledTask = nextSnapshot.tasks.firstOrNull { it.id == task.id }
    if (task.isRecurring && rescheduledTask?.scheduledAtMillis != null) {
      val nextLabel = TodoListEngine.formatScheduleLabel(
        scheduledAtMillis = rescheduledTask.scheduledAtMillis,
        scheduledEndMillis = rescheduledTask.scheduledEndMillis,
        nowMillis = now,
      )
      Toast.makeText(context, "Completed! Rescheduled for $nextLabel", Toast.LENGTH_SHORT).show()
    }
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

  fun toggleTaskSelection(taskId: String) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    selectedTaskIds = if (taskId in selectedTaskIds) {
      selectedTaskIds - taskId
    } else {
      selectedTaskIds + taskId
    }
  }

  fun openScheduleForTask(task: TodoItem) {
    val defaultStart = task.scheduledAtMillis ?: (System.currentTimeMillis() + 3_600_000L)
    scheduleStartMillis = defaultStart
    scheduleRecurrenceRule = task.recurrenceRule
    selectedSchedulePreset = task.recurrenceRule
      ?: TodoRecurrenceRule(interval = 1, unit = TodoRecurrenceUnit.HOUR, presetLabel = "1 hr")
    schedulingTaskId = task.id
    isSchedulingBulk = false
    showSchedulePanel = true
  }

  fun addTask() {
    val trimmed = newTaskTitle.trim()
    if (trimmed.isEmpty()) return

    val shouldSchedule = hasPendingSchedule || (showSchedulePanel && schedulingTaskId == null && !isSchedulingBulk)
    if (shouldSchedule) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !TodoNotificationHelper.hasNotificationPermission(context)) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      val startMillis = scheduleStartMillis
      val recurrence = scheduleRecurrenceRule
      val newItem = TodoItem(
        title = trimmed,
        scheduledAtMillis = startMillis,
        scheduledEndMillis = null,
        recurrenceRule = recurrence,
      )
      applyAction(listOf(newItem) + tasks)
      scrollTaskListToTop()
      val label = TodoListEngine.formatScheduleLabel(startMillis, null)
      val repeatSuffix = recurrence?.let { " (${it.formatLabel()})" } ?: ""
      Toast.makeText(context, "Scheduled for $label$repeatSuffix", Toast.LENGTH_SHORT).show()
      newTaskTitle = ""
      scheduleRecurrenceRule = null
      hasPendingSchedule = false
      showSchedulePanel = false
    } else {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      val newItem = TodoItem(title = trimmed)
      applyAction(listOf(newItem) + tasks)
      scrollTaskListToTop()
      newTaskTitle = ""
    }
  }

  fun addSavedTask(savedTask: TodoItem) {
    val shouldSchedule = hasPendingSchedule || (showSchedulePanel && schedulingTaskId == null && !isSchedulingBulk)
    if (shouldSchedule) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !TodoNotificationHelper.hasNotificationPermission(context)) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      val startMillis = scheduleStartMillis
      val recurrence = scheduleRecurrenceRule
      val freshItem = TodoListEngine.instantiateSavedTask(
        savedTask = savedTask,
        scheduledAtMillis = startMillis,
        scheduledEndMillis = null,
        recurrenceRule = recurrence,
      )
      applyAction(listOf(freshItem) + tasks)
      scrollTaskListToTop()
      val label = TodoListEngine.formatScheduleLabel(startMillis, null)
      val repeatSuffix = recurrence?.let { " (${it.formatLabel()})" } ?: ""
      Toast.makeText(context, "Scheduled for $label$repeatSuffix", Toast.LENGTH_SHORT).show()
      scheduleRecurrenceRule = null
      hasPendingSchedule = false
      showSchedulePanel = false
    } else {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      val freshItem = TodoListEngine.instantiateSavedTask(savedTask)
      applyAction(listOf(freshItem) + tasks)
      scrollTaskListToTop()
    }
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

  fun promoteSubTask(taskId: String, subtaskId: String) {
    val parentTask = tasks.firstOrNull { it.id == taskId } ?: return
    val subtask = TodoListEngine.findSubTask(parentTask.subtasks, subtaskId) ?: return
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val updated = TodoListEngine.promoteSubTaskToMainTask(tasks, taskId, subtaskId)
    applyAction(updated)
    Toast.makeText(context, "Moved \"${subtask.title}\" to main tasks", Toast.LENGTH_SHORT).show()
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
    recurrenceRule: TodoRecurrenceRule? = null,
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
      recurrenceRule = recurrenceRule,
    )
    duePopupTasks = duePopupTasks.filter { it.id != taskId }
    applyAction(updated)
    val label = TodoListEngine.formatScheduleLabel(scheduledAtMillis, scheduledEndMillis)
    val repeatSuffix = recurrenceRule?.let { " (${it.formatLabel()})" } ?: ""
    Toast.makeText(context, "Scheduled for $label$repeatSuffix", Toast.LENGTH_SHORT).show()
  }

  fun scheduleSelectedTasks(
    taskIds: Set<String>,
    scheduledAtMillis: Long,
    scheduledEndMillis: Long?,
    recurrenceRule: TodoRecurrenceRule? = null,
  ) {
    if (taskIds.isEmpty()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !TodoNotificationHelper.hasNotificationPermission(context)) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = TodoListEngine.scheduleTasks(
      tasks = tasks,
      taskIds = taskIds,
      scheduledAtMillis = scheduledAtMillis,
      scheduledEndMillis = scheduledEndMillis,
      recurrenceRule = recurrenceRule,
    )
    duePopupTasks = duePopupTasks.filter { it.id !in taskIds }
    applyAction(updated)
    selectedTaskIds = emptySet()
    val label = TodoListEngine.formatScheduleLabel(scheduledAtMillis, scheduledEndMillis)
    val repeatSuffix = recurrenceRule?.let { " (${it.formatLabel()})" } ?: ""
    Toast.makeText(context, "Scheduled ${taskIds.size} task(s) for $label$repeatSuffix", Toast.LENGTH_SHORT).show()
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
  var draggedSubtaskParentTaskId by remember { mutableStateOf<String?>(null) }
  var subtaskDragOffsetY by remember { mutableFloatStateOf(0f) }
  var initialSubtaskDragSnapshot by remember { mutableStateOf<TodoHistorySnapshot?>(null) }

  fun resolveVisibleTaskListViewportBounds(): Pair<Float, Float>? {
    val listCoords = taskListCoordinates?.takeIf { it.isAttached } ?: return null
    val listBounds = listCoords.boundsInRoot()
    val visibleTop = listBounds.top
    val quickAddTop = quickAddBarCoordinates
      ?.takeIf { it.isAttached }
      ?.boundsInRoot()
      ?.top
    val visibleBottom = if (
      quickAddTop != null &&
      quickAddTop > visibleTop + autoScrollEdgeThresholdPx * 2f &&
      quickAddTop < listBounds.bottom
    ) {
      quickAddTop
    } else {
      listBounds.bottom
    }
    return if (visibleBottom > visibleTop) visibleTop to visibleBottom else null
  }

  val mainViewTasks = remember(tasks, nowMillis) {
    TodoListEngine.getMainViewTasks(tasks, nowMillis)
  }

  val activeFilteredTasks = remember(tasks, mainViewTasks, selectedTab, isSearchActive, searchQuery) {
    val base = if (selectedTab == TodoFilterTab.SCHEDULED) {
      tasks.filter { it.isScheduled }
    } else {
      mainViewTasks
    }
    if (isSearchActive && searchQuery.isNotBlank()) {
      TodoListEngine.filterTasks(base, searchQuery)
    } else {
      base
    }
  }

  fun updateTodoDragAutoScrollSpeed(pointerRootY: Float?, isPointerMove: Boolean) {
    val activeTaskId = draggedTaskId
    val activeSubtaskId = draggedSubtaskId
    if ((activeTaskId == null && activeSubtaskId == null) || pointerRootY == null) {
      autoScrollSpeedPx = 0f
      return
    }
    val (visibleTop, visibleBottom) = resolveVisibleTaskListViewportBounds() ?: run {
      autoScrollSpeedPx = 0f
      return
    }
    val canMoveBackward: Boolean
    val canMoveForward: Boolean
    if (activeTaskId != null) {
      val currentIndex = activeFilteredTasks.indexOfFirst { it.id == activeTaskId }
      canMoveBackward = currentIndex > 0
      canMoveForward = currentIndex in 0 until activeFilteredTasks.lastIndex
    } else {
      val parentTask = tasks.firstOrNull { it.id == draggedSubtaskParentTaskId }
        ?: tasks.firstOrNull { task -> task.subtasks.any { it.id == activeSubtaskId } }
      val subtasks = parentTask?.subtasks.orEmpty()
      val currentIndex = subtasks.indexOfFirst { it.id == activeSubtaskId }
      canMoveBackward = currentIndex > 0
      canMoveForward = currentIndex in 0 until subtasks.lastIndex
    }

    val defaultTopTrigger = visibleTop + autoScrollEdgeThresholdPx
    val defaultBottomTrigger = visibleBottom - autoScrollEdgeThresholdPx
    val topTrigger = dragTopEdgeTriggerY ?: defaultTopTrigger
    val bottomTrigger = dragBottomEdgeTriggerY ?: defaultBottomTrigger

    autoScrollSpeedPx = TodoListEngine.calculateDragAutoScrollSpeed(
      pointerY = pointerRootY,
      viewportTop = visibleTop,
      viewportBottom = visibleBottom,
      edgeThresholdPx = autoScrollEdgeThresholdPx,
      maxScrollStepPx = maxAutoScrollStepPx,
      canScrollBackward = listState.canScrollBackward,
      canScrollForward = listState.canScrollForward,
      canMoveItemBackward = canMoveBackward,
      canMoveItemForward = canMoveForward,
      topEdgeTriggerY = topTrigger,
      bottomEdgeTriggerY = bottomTrigger,
    )

    if (isPointerMove) {
      dragTopEdgeTriggerY = minOf(defaultTopTrigger, maxOf(topTrigger, pointerRootY))
      dragBottomEdgeTriggerY = maxOf(defaultBottomTrigger, minOf(bottomTrigger, pointerRootY))
    }
  }

  fun initializeDragEdgeTriggers(startRootY: Float?) {
    dragPointerRootY = startRootY
    autoScrollSpeedPx = 0f
    val viewportBounds = resolveVisibleTaskListViewportBounds()
    if (viewportBounds != null && startRootY != null) {
      val (visibleTop, visibleBottom) = viewportBounds
      dragTopEdgeTriggerY = minOf(startRootY, visibleTop + autoScrollEdgeThresholdPx)
      dragBottomEdgeTriggerY = maxOf(startRootY, visibleBottom - autoScrollEdgeThresholdPx)
    } else {
      dragTopEdgeTriggerY = null
      dragBottomEdgeTriggerY = null
    }
  }

  fun clearDragAutoScrollState() {
    dragPointerRootY = null
    dragTopEdgeTriggerY = null
    dragBottomEdgeTriggerY = null
    autoScrollSpeedPx = 0f
  }

  fun endDraggingSubTask() {
    val before = initialSubtaskDragSnapshot
    draggedSubtaskId = null
    draggedSubtaskParentTaskId = null
    subtaskDragOffsetY = 0f
    initialSubtaskDragSnapshot = null
    clearDragAutoScrollState()

    if (before != null && before.tasks != tasks) {
      undoStack = listOf(before) + undoStack.take(49)
      redoStack = emptyList()
      historyVersion += 1
      repo.saveTasks(tasks)
      TodoListWidgets.saveUndoRedoHistory(context, undoStack, redoStack)
      TodoListWidgets.updateAllWidgets(context)
    }
  }

  fun startDragging(taskId: String, startRootY: Float? = null) {
    if (draggedSubtaskId != null) {
      endDraggingSubTask()
    }
    inlineEditingTaskId = null
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    draggedTaskId = taskId
    dragOffsetY = 0f
    initialDragSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    initializeDragEdgeTriggers(startRootY)
  }

  fun onDragDelta(taskId: String, deltaY: Float, pointerRootY: Float? = null) {
    if (draggedTaskId != taskId) return
    if (pointerRootY != null) {
      dragPointerRootY = pointerRootY
    }
    val firstVisibleIndex = listState.firstVisibleItemIndex
    val firstVisibleOffset = listState.firstVisibleItemScrollOffset
    val visibleItems = activeFilteredTasks
    val visibleIds = visibleItems.map { it.id }.toSet()

    val stepResult = TodoListEngine.calculateDragReorder(
      items = visibleItems,
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
      val reorderedIterator = stepResult.items.iterator()
      tasks = tasks.map { item ->
        if (item.id in visibleIds && reorderedIterator.hasNext()) {
          reorderedIterator.next()
        } else {
          item
        }
      }
      val minIdx = minOf(stepResult.fromIndex, stepResult.toIndex)
      val maxIdx = maxOf(stepResult.fromIndex, stepResult.toIndex)
      if (firstVisibleIndex in minIdx..maxIdx) {
        listState.requestScrollToItem(firstVisibleIndex, firstVisibleOffset)
      }
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    updateTodoDragAutoScrollSpeed(
      pointerRootY = pointerRootY ?: dragPointerRootY,
      isPointerMove = pointerRootY != null,
    )
  }

  fun endDragging() {
    val before = initialDragSnapshot
    draggedTaskId = null
    dragOffsetY = 0f
    initialDragSnapshot = null
    clearDragAutoScrollState()

    if (before != null && before.tasks != tasks) {
      undoStack = listOf(before) + undoStack.take(49)
      redoStack = emptyList()
      historyVersion += 1
      repo.saveTasks(tasks)
      TodoListWidgets.saveUndoRedoHistory(context, undoStack, redoStack)
      TodoListWidgets.updateAllWidgets(context)
    }
  }

  fun startDraggingSubTask(taskId: String, subtaskId: String, startRootY: Float? = null) {
    if (draggedTaskId != null) {
      endDragging()
    }
    inlineEditingTaskId = null
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    draggedSubtaskParentTaskId = taskId
    draggedSubtaskId = subtaskId
    subtaskDragOffsetY = 0f
    initialSubtaskDragSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
    initializeDragEdgeTriggers(startRootY)
  }

  fun onDragSubTaskDelta(taskId: String, subtaskId: String, deltaY: Float, pointerRootY: Float? = null) {
    if (draggedSubtaskId != subtaskId) return
    if (pointerRootY != null) {
      dragPointerRootY = pointerRootY
    }
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
    updateTodoDragAutoScrollSpeed(
      pointerRootY = pointerRootY ?: dragPointerRootY,
      isPointerMove = pointerRootY != null,
    )
  }

  LaunchedEffect(draggedTaskId, draggedSubtaskId, autoScrollSpeedPx) {
    val activeTaskId = draggedTaskId
    val activeSubtaskId = draggedSubtaskId
    if ((activeTaskId == null && activeSubtaskId == null) || autoScrollSpeedPx == 0f) {
      return@LaunchedEffect
    }
    while (
      isActive &&
      draggedTaskId == activeTaskId &&
      draggedSubtaskId == activeSubtaskId &&
      autoScrollSpeedPx != 0f
    ) {
      val canScroll = if (autoScrollSpeedPx < 0f) {
        listState.canScrollBackward
      } else {
        listState.canScrollForward
      }
      if (!canScroll) {
        autoScrollSpeedPx = 0f
        break
      }
      val consumed = listState.scrollBy(autoScrollSpeedPx)
      if (consumed != 0f) {
        if (activeTaskId != null) {
          onDragDelta(activeTaskId, consumed, pointerRootY = null)
        } else if (activeSubtaskId != null) {
          val parentTaskId = draggedSubtaskParentTaskId
            ?: tasks.firstOrNull { task -> task.subtasks.any { it.id == activeSubtaskId } }?.id
          if (parentTaskId != null) {
            onDragSubTaskDelta(parentTaskId, activeSubtaskId, consumed, pointerRootY = null)
          } else {
            autoScrollSpeedPx = 0f
            break
          }
        }
      } else {
        autoScrollSpeedPx = 0f
        break
      }
      delay(16L)
    }
  }

  val taskToSchedule = schedulingTaskId?.let { id -> tasks.firstOrNull { it.id == id } }
  val isSchedulePanelOpen = showSchedulePanel || taskToSchedule != null || isSchedulingBulk

  val filteredCompletedTasks = remember(completedTasks, isSearchActive, searchQuery) {
    if (isSearchActive && searchQuery.isNotBlank()) {
      TodoListEngine.filterTasks(completedTasks, searchQuery)
    } else {
      completedTasks
    }
  }

  val visibleSelectableItems = if (selectedTab == TodoFilterTab.COMPLETED) {
    filteredCompletedTasks
  } else {
    activeFilteredTasks
  }

  BackHandler(enabled = isSchedulePanelOpen || isSelectionMode || isSearchActive) {
    when {
      isSchedulePanelOpen -> {
        if (taskToSchedule == null && !isSchedulingBulk) {
          hasPendingSchedule = false
        }
        schedulingTaskId = null
        isSchedulingBulk = false
        showSchedulePanel = false
      }
      isSelectionMode -> {
        selectedTaskIds = emptySet()
      }
      isSearchActive -> {
        isSearchActive = false
        searchQuery = ""
        focusManager.clearFocus()
      }
    }
  }

  val todoActionDispatcher = remember(tasks, completedTasks, completedTodayCount, context) {
    TodoItemActionDispatcher(
      onUnscheduleTask = { task ->
        unscheduleTask(task.id)
      },
      onDeleteTask = { task ->
        deleteTask(task)
      },
      onCopyTask = { task ->
        CrossToolActions.copyTextToClipboard(
          context = context,
          label = task.title,
          text = formatTodoTaskPlainText(task),
        )
      },
      onShareTask = { task ->
        CrossToolActions.shareText(
          context = context,
          title = task.title,
          text = formatTodoTaskPlainText(task),
        )
      },
    )
  }

  val scheduledTasksCount = remember(tasks) { tasks.count { it.isScheduled } }
  val headerSubtext = remember(
    isTodoFolderGranted,
    selectedTab,
    mainViewTasks.size,
    scheduledTasksCount,
    completedTodayCount,
  ) {
    if (!isTodoFolderGranted) {
      "Storage access locked • Grant access to /ToDo"
    } else {
      when (selectedTab) {
        TodoFilterTab.TASKS -> "${mainViewTasks.size} tasks • $completedTodayCount completed today"
        TodoFilterTab.SCHEDULED -> "$scheduledTasksCount scheduled • ${mainViewTasks.size} tasks"
        TodoFilterTab.COMPLETED -> "$completedTodayCount completed today • ${mainViewTasks.size} tasks"
      }
    }
  }

  var rootBottomInsetPx by remember { mutableIntStateOf(0) }
  val imeBottomPx = WindowInsets.ime.getBottom(density)
  val extraImeBottomPadding = with(density) {
    (imeBottomPx - rootBottomInsetPx).coerceAtLeast(0).toDp()
  }
  val quickAddBringIntoViewRequester = remember { BringIntoViewRequester() }
  val quickAddCoroutineScope = rememberCoroutineScope()
  var isQuickAddInputFocused by remember { mutableStateOf(false) }

  LaunchedEffect(isQuickAddInputFocused, imeBottomPx, newTaskTitle) {
    if (isQuickAddInputFocused) {
      quickAddBringIntoViewRequester.bringIntoView()
    }
  }

  CompositionLocalProvider(LocalTodoItemActions provides todoActionDispatcher) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .toolboxScreenBackground(MaterialTheme.colorScheme.background)
        .onGloballyPositioned { coords ->
          val rootHeight = coords.findRootCoordinates().size.height
          val bottomInRoot = (coords.positionInRoot().y + coords.size.height).roundToInt()
          rootBottomInsetPx = (rootHeight - bottomInRoot).coerceAtLeast(0)
        }
        .padding(bottom = extraImeBottomPadding)
        .testTag("todo_list_screen"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .toolboxScreenBackground(MaterialTheme.colorScheme.background),
      ) {
        // 1. Unified 68.dp Top Bar with Search, Multi-Selection, Undo/Redo, and View Mode Toggle
        ToolboxTopBar(
          title = "To Do",
          subtext = headerSubtext,
          icon = Icons.Default.TaskAlt,
          titleTestTag = "todo_screen_title",
          subtextTestTag = "todo_screen_subtext",
          selectionState = if (isSelectionMode) {
            ToolboxTopBarSelectionState(
              selectedCount = selectedTaskIds.size,
              totalCount = visibleSelectableItems.size,
              onToggleSelectAll = {
                val allVisibleIds = visibleSelectableItems.map { it.id }.toSet()
                selectedTaskIds = if (allVisibleIds.isNotEmpty() && selectedTaskIds.containsAll(allVisibleIds)) {
                  emptySet()
                } else {
                  allVisibleIds
                }
              },
              onCloseSelection = {
                selectedTaskIds = emptySet()
                isSchedulingBulk = false
              },
              closeButtonTestTag = "todo_clear_selection",
              selectAllRowTestTag = "todo_select_all_toggle",
              actions = {
                var showOverflowMenu by remember { mutableStateOf(false) }
                val hasSelection = selectedTaskIds.isNotEmpty()
                val isCompletedTab = selectedTab == TodoFilterTab.COMPLETED
                val selectedItemsList = visibleSelectableItems.filter { it.id in selectedTaskIds }

                if (isCompletedTab) {
                  IconButton(
                    onClick = {
                      val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
                      val nextSnapshot = TodoListEngine.uncompleteTasks(currentSnapshot, selectedTaskIds)
                      if (nextSnapshot != currentSnapshot) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        applySnapshot(nextSnapshot)
                      }
                      selectedTaskIds = emptySet()
                    },
                    enabled = hasSelection,
                    modifier = Modifier
                      .size(36.dp)
                      .testTag("todo_bulk_uncomplete"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.RadioButtonUnchecked,
                      contentDescription = "Uncomplete selected",
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
                    onClick = {
                      val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
                      val nextSnapshot = TodoListEngine.completeTasks(currentSnapshot, selectedTaskIds)
                      if (nextSnapshot != currentSnapshot) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        applySnapshot(nextSnapshot)
                      }
                      selectedTaskIds = emptySet()
                    },
                    enabled = hasSelection,
                    modifier = Modifier
                      .size(36.dp)
                      .testTag("todo_bulk_complete"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = "Complete selected",
                      tint = if (hasSelection) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                      },
                      modifier = Modifier.size(20.dp),
                    )
                  }

                  IconButton(
                    onClick = {
                      val firstScheduledTask = selectedItemsList.firstOrNull { it.scheduledAtMillis != null }
                      scheduleStartMillis = firstScheduledTask?.scheduledAtMillis ?: (System.currentTimeMillis() + 3_600_000L)
                      scheduleRecurrenceRule = firstScheduledTask?.recurrenceRule
                      selectedSchedulePreset = firstScheduledTask?.recurrenceRule
                        ?: TodoRecurrenceRule(interval = 1, unit = TodoRecurrenceUnit.HOUR, presetLabel = "1 hr")
                      schedulingTaskId = null
                      isSchedulingBulk = true
                      showSchedulePanel = true
                    },
                    enabled = hasSelection,
                    modifier = Modifier
                      .size(36.dp)
                      .testTag("todo_bulk_schedule"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.Schedule,
                      contentDescription = "Schedule selected",
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
                  onClick = {
                    if (selectedItemsList.isNotEmpty()) {
                      val combined = selectedItemsList.joinToString("\n\n") { formatTodoTaskPlainText(it) }
                      CrossToolActions.shareText(
                        context = context,
                        title = if (selectedItemsList.size == 1) selectedItemsList.first().title else "${selectedItemsList.size} Tasks",
                        text = combined,
                      )
                      selectedTaskIds = emptySet()
                    }
                  },
                  enabled = hasSelection,
                  modifier = Modifier
                    .size(36.dp)
                    .testTag("todo_bulk_share"),
                ) {
                  Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share selected",
                    tint = if (hasSelection) {
                      MaterialTheme.colorScheme.onSurface
                    } else {
                      MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(20.dp),
                  )
                }

                IconButton(
                  onClick = {
                    val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
                    val nextSnapshot = if (isCompletedTab) {
                      TodoListEngine.deleteCompletedTasks(currentSnapshot, selectedTaskIds)
                    } else {
                      TodoListEngine.deleteTasks(currentSnapshot, selectedTaskIds)
                    }
                    if (nextSnapshot != currentSnapshot) {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      applySnapshot(nextSnapshot)
                    }
                    selectedTaskIds = emptySet()
                  },
                  enabled = hasSelection,
                  modifier = Modifier
                    .size(36.dp)
                    .testTag("todo_bulk_delete"),
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete selected",
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
                    if (isCompletedTab) {
                      DropdownMenuItem(
                        text = { Text("Uncomplete") },
                        leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null) },
                        enabled = hasSelection,
                        onClick = {
                          showOverflowMenu = false
                          val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
                          val nextSnapshot = TodoListEngine.uncompleteTasks(currentSnapshot, selectedTaskIds)
                          if (nextSnapshot != currentSnapshot) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            applySnapshot(nextSnapshot)
                          }
                          selectedTaskIds = emptySet()
                        },
                      )
                    } else {
                      DropdownMenuItem(
                        text = { Text("Complete") },
                        leadingIcon = {
                          Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                          )
                        },
                        enabled = hasSelection,
                        onClick = {
                          showOverflowMenu = false
                          val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
                          val nextSnapshot = TodoListEngine.completeTasks(currentSnapshot, selectedTaskIds)
                          if (nextSnapshot != currentSnapshot) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            applySnapshot(nextSnapshot)
                          }
                          selectedTaskIds = emptySet()
                        },
                      )
                      DropdownMenuItem(
                        text = { Text("Schedule") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        enabled = hasSelection,
                        onClick = {
                          showOverflowMenu = false
                          val firstScheduledTask = selectedItemsList.firstOrNull { it.scheduledAtMillis != null }
                          scheduleStartMillis = firstScheduledTask?.scheduledAtMillis ?: (System.currentTimeMillis() + 3_600_000L)
                          scheduleRecurrenceRule = firstScheduledTask?.recurrenceRule
                          selectedSchedulePreset = firstScheduledTask?.recurrenceRule
                            ?: TodoRecurrenceRule(interval = 1, unit = TodoRecurrenceUnit.HOUR, presetLabel = "1 hr")
                          schedulingTaskId = null
                          isSchedulingBulk = true
                          showSchedulePanel = true
                        },
                      )
                      if (selectedItemsList.any { it.isScheduled }) {
                        DropdownMenuItem(
                          text = { Text("Unschedule") },
                          leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null) },
                          enabled = hasSelection,
                          onClick = {
                            showOverflowMenu = false
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTaskIds.forEach { id -> TodoNotificationHelper.cancelTaskAlarm(context, id) }
                            val updated = TodoListEngine.unscheduleTasks(tasks, selectedTaskIds)
                            duePopupTasks = duePopupTasks.filter { it.id !in selectedTaskIds }
                            applyAction(updated)
                            selectedTaskIds = emptySet()
                          },
                        )
                      }
                      DropdownMenuItem(
                        text = { Text("Save as Template") },
                        leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null) },
                        enabled = hasSelection,
                        onClick = {
                          showOverflowMenu = false
                          var updatedTemplates = savedTasks
                          selectedItemsList.forEach { item ->
                            updatedTemplates = TodoListEngine.saveTaskAsTemplate(updatedTemplates, item)
                          }
                          savedTasks = updatedTemplates
                          repo.saveSavedTasks(updatedTemplates)
                          Toast.makeText(context, "Saved ${selectedItemsList.size} template(s)", Toast.LENGTH_SHORT).show()
                          selectedTaskIds = emptySet()
                        },
                      )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                      text = { Text("Copy") },
                      leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                      enabled = hasSelection,
                      onClick = {
                        showOverflowMenu = false
                        if (selectedItemsList.isNotEmpty()) {
                          val combined = selectedItemsList.joinToString("\n\n") { formatTodoTaskPlainText(it) }
                          CrossToolActions.copyTextToClipboard(
                            context = context,
                            label = "Tasks",
                            text = combined,
                          )
                          selectedTaskIds = emptySet()
                        }
                      },
                    )
                    DropdownMenuItem(
                      text = { Text("Share") },
                      leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                      enabled = hasSelection,
                      onClick = {
                        showOverflowMenu = false
                        if (selectedItemsList.isNotEmpty()) {
                          val combined = selectedItemsList.joinToString("\n\n") { formatTodoTaskPlainText(it) }
                          CrossToolActions.shareText(
                            context = context,
                            title = if (selectedItemsList.size == 1) selectedItemsList.first().title else "${selectedItemsList.size} Tasks",
                            text = combined,
                          )
                          selectedTaskIds = emptySet()
                        }
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
                      enabled = hasSelection,
                      onClick = {
                        showOverflowMenu = false
                        val currentSnapshot = TodoHistorySnapshot(tasks, completedTodayCount, completedTasks)
                        val nextSnapshot = if (isCompletedTab) {
                          TodoListEngine.deleteCompletedTasks(currentSnapshot, selectedTaskIds)
                        } else {
                          TodoListEngine.deleteTasks(currentSnapshot, selectedTaskIds)
                        }
                        if (nextSnapshot != currentSnapshot) {
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          applySnapshot(nextSnapshot)
                        }
                        selectedTaskIds = emptySet()
                      },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                      text = {
                        Text(
                          if (selectedTaskIds.size == visibleSelectableItems.size && visibleSelectableItems.isNotEmpty()) {
                            "Deselect all"
                          } else {
                            "Select all"
                          },
                        )
                      },
                      leadingIcon = { Icon(Icons.Default.SelectAll, contentDescription = null) },
                      onClick = {
                        showOverflowMenu = false
                        val allVisibleIds = visibleSelectableItems.map { it.id }.toSet()
                        selectedTaskIds = if (allVisibleIds.isNotEmpty() && selectedTaskIds.containsAll(allVisibleIds)) {
                          emptySet()
                        } else {
                          allVisibleIds
                        }
                      },
                    )
                    DropdownMenuItem(
                      text = { Text("Exit selection") },
                      leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                      onClick = {
                        showOverflowMenu = false
                        selectedTaskIds = emptySet()
                        isSchedulingBulk = false
                      },
                    )
                  }
                }
              },
            )
          } else {
            null
          },
          searchState = ToolboxTopBarSearchState(
            isActive = isSearchActive,
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onOpenSearch = { isSearchActive = true },
            onCloseSearch = {
              focusManager.clearFocus()
              isSearchActive = false
              searchQuery = ""
            },
            placeholder = "Search tasks, subtasks, or notes...",
            searchContentDescription = "Search tasks",
            closeContentDescription = "Close search",
            clearContentDescription = "Clear search",
            toggleButtonTestTag = "todo_search_toggle",
            searchBarTestTag = "todo_toolbar_search_bar",
            closeButtonTestTag = "todo_search_close",
            inputTestTag = "todo_search_input",
            clearButtonTestTag = "todo_search_clear",
            onSearchImeAction = { focusManager.clearFocus() },
          ),
          actions = {
            // Undo Button
            IconButton(
              onClick = { undo() },
              enabled = canUndo,
              modifier = Modifier
                .size(36.dp)
                .testTag("todo_undo_button"),
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

            // Redo Button
            IconButton(
              onClick = { redo() },
              enabled = canRedo,
              modifier = Modifier
                .size(36.dp)
                .testTag("todo_redo_button"),
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
          },
        )
        // 2. Pill Filter Row (Tasks, Scheduled, Completed) + Pinned Floating [Saved Tasks] Control
        TodoFilterPillRow(
          selectedTab = selectedTab,
          tasksCount = mainViewTasks.size,
          scheduledCount = scheduledTasksCount,
          completedCount = completedTodayCount,
          isTodoFolderGranted = isTodoFolderGranted,
          pendingPermissionFolderPath = pendingPermissionFolderPath,
          onOpenSavedTasks = {
            if (!isTodoFolderGranted) {
              requestTodoFolderPermissionPrompt()
            } else {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              showSavedTasksSheet = true
            }
          },
          onRequestTodoFolderPermission = {
            requestTodoFolderPermissionPrompt()
          },
          onSelectTab = { tab ->
            if (!isTodoFolderGranted) {
              requestTodoFolderPermissionPrompt()
            } else {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              selectedTab = tab
              selectedTaskIds = emptySet()
            }
          },
        )

        // 3. In-app Notification Popup Banner when a scheduled note's date/time is reached
        AnimatedVisibility(
          visible = isTodoFolderGranted && duePopupTasks.isNotEmpty(),
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut(),
        ) {
          val topDue = duePopupTasks.firstOrNull()
          if (topDue != null) {
            DueNotificationPopupCard(
              task = topDue,
              onReschedule = {
                duePopupTasks = duePopupTasks.filter { it.id != topDue.id }
                openScheduleForTask(topDue)
              },
              onDismiss = {
                duePopupTasks = duePopupTasks.filter { it.id != topDue.id }
              },
            )
          }
        }

        // 4. Main Content Area (Scoped Storage Locked/Permission View or Horizontally Paginated Task List)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        ) {
          if (!isTodoFolderGranted) {
            if (pendingPermissionFolderPath != null) {
              TodoFolderPermissionRequestView(
                folder = DEFAULT_TODO_SCOPED_FOLDER,
                onAllowAccess = {
                  requestSystemFolderAccess(DEFAULT_TODO_SCOPED_FOLDER)
                },
              )
            } else {
              TodoLockedStorageStateView(
                onRequestPermission = {
                  requestTodoFolderPermissionPrompt()
                },
              )
            }
          } else {
            val hasActiveSearchQuery = isSearchActive && searchQuery.isNotBlank()

            ToolboxPillHorizontalPager(
              items = TodoFilterTab.entries,
              selectedItem = selectedTab,
              enabled = !isSelectionMode && draggedTaskId == null && draggedSubtaskId == null,
              key = { _, tab -> tab.name },
              onSelectItem = { tab ->
                selectedTab = tab
                selectedTaskIds = emptySet()
              },
              modifier = Modifier.fillMaxSize(),
            ) { pageTab ->
              val isActivePage = pageTab == selectedTab
              when (pageTab) {
                TodoFilterTab.COMPLETED -> {
                  CompletedTasksListContent(
                    completedTasks = filteredCompletedTasks,
                    completedCount = completedTodayCount,
                    hasSearchQuery = hasActiveSearchQuery,
                    isSelectionMode = isSelectionMode,
                    selectedTaskIds = selectedTaskIds,
                    onToggleSelectTask = { taskId -> toggleTaskSelection(taskId) },
                    historyVersion = historyVersion,
                    onUncompleteTask = { task -> uncompleteTask(task) },
                    onRemoveCompletedTask = { task -> deleteCompletedTask(task) },
                  )
                }
                TodoFilterTab.TASKS,
                TodoFilterTab.SCHEDULED -> {
                  val pageFilteredTasks = remember(tasks, mainViewTasks, pageTab, hasActiveSearchQuery, searchQuery) {
                    val base = if (pageTab == TodoFilterTab.SCHEDULED) {
                      tasks.filter { it.isScheduled }
                    } else {
                      mainViewTasks
                    }
                    if (hasActiveSearchQuery) {
                      TodoListEngine.filterTasks(base, searchQuery)
                    } else {
                      base
                    }
                  }
                  if (pageFilteredTasks.isEmpty()) {
                    when {
                      hasActiveSearchQuery -> EmptySearchTasksState()
                      pageTab == TodoFilterTab.SCHEDULED -> EmptyScheduledTasksState()
                      else -> EmptyTasksState()
                    }
                  } else {
                    val pageListState = if (isActivePage) listState else rememberLazyListState()
                    LazyColumn(
                      state = pageListState,
                      modifier = Modifier
                        .fillMaxSize()
                        .then(
                          if (isActivePage) {
                            Modifier
                              .onGloballyPositioned { taskListCoordinates = it }
                              .testTag("todo_tasks_list")
                          } else {
                            Modifier
                          }
                        ),
                      contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 104.dp,
                      ),
                      verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      items(
                        items = pageFilteredTasks,
                        key = { it.id },
                      ) { task ->
                      val isBeingDragged = draggedTaskId == task.id
                      val isEditingInline = inlineEditingTaskId == task.id
                      SwipeableTaskItem(
                        task = task,
                        historyVersion = historyVersion,
                        isBeingDragged = isBeingDragged,
                        dragOffsetY = if (isBeingDragged) dragOffsetY else 0f,
                        isSelected = task.id in selectedTaskIds,
                        isSelectionMode = isSelectionMode,
                        isEditingInline = isEditingInline,
                        onToggleSelection = { toggleTaskSelection(task.id) },
                        onScheduleClick = {
                          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                          openScheduleForTask(task)
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
                        onPromoteSubTask = { subtaskId -> promoteSubTask(task.id, subtaskId) },
                        onUpdateSubTask = { subtaskId, newTitle -> updateSubTask(task.id, subtaskId, newTitle) },
                        draggedSubtaskId = draggedSubtaskId,
                        subtaskDragOffsetY = subtaskDragOffsetY,
                        onStartDragSubTask = { subtaskId, startRootY ->
                          startDraggingSubTask(task.id, subtaskId, startRootY)
                        },
                        onDragSubTask = { subtaskId, deltaY, pointerRootY ->
                          onDragSubTaskDelta(task.id, subtaskId, deltaY, pointerRootY)
                        },
                        onEndDragSubTask = { endDraggingSubTask() },
                        onSubtaskHeightMeasured = { subtaskId, heightPx ->
                          if (heightPx > 0f) {
                            subtaskHeightsPx[subtaskId] = heightPx
                          }
                        },
                        onStartDrag = { startRootY -> startDragging(task.id, startRootY) },
                        onDrag = { deltaY, pointerRootY -> onDragDelta(task.id, deltaY, pointerRootY) },
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
              }
            }
          }
        }
      }

      // 5. Floating 28.dp Pill Quick-Add Bar (styled like NotesQuickCaptureBar)
      if (isTodoFolderGranted && (!isSelectionMode || isSchedulePanelOpen)) {
        val hexagonEnabled = currentHexagonIconButtons()
        val savedTaskSuggestions = remember(savedTasks, newTaskTitle, isSelectionMode) {
          if (isSelectionMode) {
            emptyList()
          } else {
            TodoListEngine.getSavedTaskSuggestions(savedTasks, newTaskTitle)
          }
        }
        Column(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .bringIntoViewRequester(quickAddBringIntoViewRequester)
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (savedTaskSuggestions.isNotEmpty()) {
            SavedTaskSuggestionsList(
              suggestions = savedTaskSuggestions,
              onSelectSuggestion = { suggestedTask ->
                addSavedTask(suggestedTask)
                newTaskTitle = ""
              },
            )
          }

          Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = if (hexagonEnabled && !isSchedulePanelOpen) {
              HorizontalHexagonShape
            } else {
              RoundedCornerShape(28.dp)
            },
            tonalElevation = 6.dp,
            shadowElevation = 4.dp,
            border = BorderStroke(
              width = 1.dp,
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .onGloballyPositioned { quickAddBarCoordinates = it }
              .testTag("todo_quick_add_card"),
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              if (!isSelectionMode) {
                RoundedQuickAddBar(
                  title = newTaskTitle,
                  onTitleChange = { newTaskTitle = it },
                  onAdd = { addTask() },
                  onOpenSavedTasks = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showSavedTasksSheet = true
                  },
                  isScheduleOpen = isSchedulePanelOpen,
                  hasPendingSchedule = hasPendingSchedule,
                  onToggleSchedule = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (isSchedulePanelOpen) {
                      showSchedulePanel = false
                      schedulingTaskId = null
                      isSchedulingBulk = false
                    } else {
                      schedulingTaskId = null
                      isSchedulingBulk = false
                      if (!hasPendingSchedule) {
                        val defaultStart = System.currentTimeMillis() + 3_600_000L
                        scheduleStartMillis = defaultStart
                        scheduleRecurrenceRule = null
                        selectedSchedulePreset = TodoRecurrenceRule(
                          interval = 1,
                          unit = TodoRecurrenceUnit.HOUR,
                          presetLabel = "1 hr",
                        )
                      }
                      showSchedulePanel = true
                    }
                  },
                  inputFocusRequester = taskInputFocusRequester,
                  onInputFocusChanged = { focused ->
                    isQuickAddInputFocused = focused
                    if (focused) {
                      quickAddCoroutineScope.launch {
                        quickAddBringIntoViewRequester.bringIntoView()
                      }
                    }
                  },
                )
              }

            // Schedule Task panel slides up inside the floating 28.dp pill bar
            AnimatedVisibility(
              visible = isSchedulePanelOpen,
              enter = slideInVertically(initialOffsetY = { it }) +
                expandVertically(expandFrom = Alignment.Top) +
                fadeIn(),
              exit = slideOutVertically(targetOffsetY = { it }) +
                shrinkVertically(shrinkTowards = Alignment.Top) +
                fadeOut(),
            ) {
              val previewTitle = when {
                isSchedulingBulk -> "${selectedTaskIds.size} selected task(s)"
                taskToSchedule != null -> taskToSchedule.title
                else -> newTaskTitle.trim().ifEmpty { "New Task" }
              }
              val isExistingSched = when {
                isSchedulingBulk -> tasks.any { it.id in selectedTaskIds && it.isScheduled }
                taskToSchedule != null -> taskToSchedule.isScheduled
                else -> hasPendingSchedule
              }
              ScheduleTaskPanel(
                taskTitle = previewTitle,
                isExistingScheduled = isExistingSched,
                startMillis = scheduleStartMillis,
                onStartMillisChange = { newStart ->
                  scheduleStartMillis = newStart
                },
                recurrenceRule = scheduleRecurrenceRule,
                onRecurrenceRuleChange = { newRule ->
                  scheduleRecurrenceRule = newRule
                },
                selectedPreset = selectedSchedulePreset,
                onSelectedPresetChange = { newPreset ->
                  selectedSchedulePreset = newPreset
                },
                onPresetSelected = {
                  if (!isSchedulingBulk) {
                    taskInputFocusRequester.requestFocus()
                  }
                },
                onConfirmSchedule = {
                  when {
                    isSchedulingBulk -> {
                      scheduleSelectedTasks(
                        taskIds = selectedTaskIds,
                        scheduledAtMillis = scheduleStartMillis,
                        scheduledEndMillis = null,
                        recurrenceRule = scheduleRecurrenceRule,
                      )
                      isSchedulingBulk = false
                      showSchedulePanel = false
                    }
                    taskToSchedule != null -> {
                      scheduleTask(
                        taskId = taskToSchedule.id,
                        scheduledAtMillis = scheduleStartMillis,
                        scheduledEndMillis = null,
                        recurrenceRule = scheduleRecurrenceRule,
                      )
                      schedulingTaskId = null
                      showSchedulePanel = false
                    }
                    else -> {
                      val trimmed = newTaskTitle.trim()
                      if (trimmed.isNotEmpty()) {
                        hasPendingSchedule = true
                        addTask()
                      } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !TodoNotificationHelper.hasNotificationPermission(context)) {
                          notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        hasPendingSchedule = true
                        showSchedulePanel = false
                        val label = TodoListEngine.formatScheduleLabel(scheduleStartMillis, null)
                        val repeatSuffix = scheduleRecurrenceRule?.let { " (${it.formatLabel()})" } ?: ""
                        Toast.makeText(context, "Scheduled for $label$repeatSuffix", Toast.LENGTH_SHORT).show()
                      }
                    }
                  }
                },
                onUnschedule = {
                  when {
                    isSchedulingBulk -> {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      selectedTaskIds.forEach { id -> TodoNotificationHelper.cancelTaskAlarm(context, id) }
                      val updated = TodoListEngine.unscheduleTasks(tasks, selectedTaskIds)
                      duePopupTasks = duePopupTasks.filter { it.id !in selectedTaskIds }
                      applyAction(updated)
                      selectedTaskIds = emptySet()
                      isSchedulingBulk = false
                    }
                    taskToSchedule != null -> {
                      unscheduleTask(taskToSchedule.id)
                      schedulingTaskId = null
                    }
                    else -> {
                      hasPendingSchedule = false
                      scheduleRecurrenceRule = null
                    }
                  }
                  showSchedulePanel = false
                },
                onDismiss = {
                  if (taskToSchedule == null && !isSchedulingBulk) {
                    hasPendingSchedule = false
                    scheduleRecurrenceRule = null
                  }
                  schedulingTaskId = null
                  isSchedulingBulk = false
                  showSchedulePanel = false
                },
              )
              }
            }
          }
        }
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
}

/**
 * Modern swipeable task card supporting swipe right to complete, swipe left to delete,
 * click-text-to-edit, vertical drag handle, multi-selection, and collapsible subtasks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskItem(
  task: TodoItem,
  historyVersion: Int = 0,
  isBeingDragged: Boolean,
  dragOffsetY: Float,
  isSelected: Boolean = false,
  isSelectionMode: Boolean = false,
  isEditingInline: Boolean,
  onToggleSelection: () -> Unit = {},
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
  onPromoteSubTask: (String) -> Unit = {},
  onUpdateSubTask: (String, String) -> Unit,
  draggedSubtaskId: String? = null,
  subtaskDragOffsetY: Float = 0f,
  onStartDragSubTask: (String, Float?) -> Unit = { _, _ -> },
  onDragSubTask: (String, Float, Float?) -> Unit = { _, _, _ -> },
  onEndDragSubTask: () -> Unit = {},
  onSubtaskHeightMeasured: (String, Float) -> Unit = { _, _ -> },
  onStartDrag: (Float?) -> Unit,
  onDrag: (Float, Float?) -> Unit,
  onEndDrag: () -> Unit,
  onItemHeightMeasured: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isSubtaskBeingDragged = draggedSubtaskId != null && task.subtasks.any { it.id == draggedSubtaskId }
  var isDragHandlePressed by remember { mutableStateOf(false) }
  val isSwipeEnabled = !isBeingDragged && !isSubtaskBeingDragged && !isEditingInline && !isDragHandlePressed && !isSelectionMode

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
        isSelected = isSelected,
        isSelectionMode = isSelectionMode,
        showDragHandle = true,
        isEditingInline = isEditingInline,
        onToggleSelection = onToggleSelection,
        onScheduleClick = onScheduleClick,
        onCheckboxClick = onComplete,
        onStartInlineEdit = onStartInlineEdit,
        onSaveInlineEdit = onSaveInlineEdit,
        onToggleSubTask = onToggleSubTask,
        onToggleCollapseSubtasks = onToggleCollapseSubtasks,
        onSaveAsTemplate = onSaveAsTemplate,
        onAddSubTask = onAddSubTask,
        onDeleteSubTask = onDeleteSubTask,
        onPromoteSubTask = onPromoteSubTask,
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
 * Task card content styled according to DESIGN_LANGUAGE.md Section 5.4 & 5.5:
 * - RoundedCornerShape(14.dp)
 * - surfaceVariant.copy(alpha = 0.45f) container color (primaryContainer.copy(alpha = 0.35f) when selected)
 * - 1.dp outlineVariant.copy(alpha = 0.5f) border (1.5.dp primary when selected or dragged)
 * - Leading Checkbox replacement in multi-selection mode.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskCardContent(
  task: TodoItem,
  isBeingDragged: Boolean,
  dragScale: Float = 1f,
  isSelected: Boolean = false,
  isSelectionMode: Boolean = false,
  showDragHandle: Boolean = true,
  isEditingInline: Boolean,
  onToggleSelection: () -> Unit = {},
  onScheduleClick: () -> Unit = {},
  onCheckboxClick: () -> Unit,
  onStartInlineEdit: () -> Unit,
  onSaveInlineEdit: (String) -> Unit,
  onToggleSubTask: (String) -> Unit,
  onToggleCollapseSubtasks: () -> Unit = {},
  onSaveAsTemplate: () -> Unit = {},
  onAddSubTask: (String) -> Unit,
  onDeleteSubTask: (String) -> Unit,
  onPromoteSubTask: (String) -> Unit = {},
  onUpdateSubTask: (String, String) -> Unit,
  draggedSubtaskId: String? = null,
  subtaskDragOffsetY: Float = 0f,
  onStartDragSubTask: (String, Float?) -> Unit = { _, _ -> },
  onDragSubTask: (String, Float, Float?) -> Unit = { _, _, _ -> },
  onEndDragSubTask: () -> Unit = {},
  onSubtaskHeightMeasured: (String, Float) -> Unit = { _, _ -> },
  onStartDrag: (Float?) -> Unit,
  onDrag: (Float, Float?) -> Unit,
  onEndDrag: () -> Unit,
  onDragHandlePressedChange: (Boolean) -> Unit = {},
  onHeightMeasured: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val checkScale = remember { Animatable(1f) }
  var newSubtaskInput by remember { mutableStateOf("") }
  var showAddSubtaskInput by remember { mutableStateOf(false) }
  var editingSubtaskId by remember { mutableStateOf<String?>(null) }
  var showContextMenu by remember { mutableStateOf(false) }
  val todoActions = LocalTodoItemActions.current
  val newSubtaskFocusRequester = remember { FocusRequester() }

  val currentOnToggleSelection by rememberUpdatedState(onToggleSelection)
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

  LaunchedEffect(isBeingDragged, draggedSubtaskId, isSelectionMode) {
    if (isBeingDragged || draggedSubtaskId != null || isSelectionMode) {
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
    modifier = modifier
      .fillMaxWidth()
      .onGloballyPositioned { coords ->
        onHeightMeasured(coords.size.height.toFloat())
      }
      .pointerInput(task.id, isSelectionMode) {
        if (!isSelectionMode) {
          detectTapGestures(
            onDoubleTap = { currentOnSaveAsTemplate() },
          )
        }
      }
      .testTag("todo_item_card_${task.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        isBeingDragged -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.background
      },
    ),
    border = when {
      isSelected || isBeingDragged -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
      else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
          .heightIn(min = 52.dp)
          .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (isSelectionMode) {
          Checkbox(
            checked = isSelected,
            onCheckedChange = { currentOnToggleSelection() },
            colors = CheckboxDefaults.colors(
              checkedColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier
              .size(36.dp)
              .testTag("todo_item_select_checkbox_${task.id}"),
          )
          Spacer(modifier = Modifier.width(8.dp))
        } else {
          if (showDragHandle) {
            var taskDragHandleCoords by remember(task.id) { mutableStateOf<LayoutCoordinates?>(null) }
            var lastTaskDownLocalOffset by remember(task.id) { mutableStateOf<Offset?>(null) }
            // Drag Handle to reorder tasks (on the left side) - Triple bar
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .onGloballyPositioned { taskDragHandleCoords = it }
                .pointerInput(task.id) {
                  awaitPointerEventScope {
                    while (true) {
                      val event = awaitPointerEvent(PointerEventPass.Initial)
                      val firstNewDown = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                      if (firstNewDown != null) {
                        lastTaskDownLocalOffset = firstNewDown.position
                      }
                      currentOnDragHandlePressedChange(event.changes.any { it.pressed })
                    }
                  }
                }
                .pointerInput(task.id) {
                  var draggingThis = false
                  try {
                    detectDragGestures(
                      onDragStart = { startOffset ->
                        draggingThis = true
                        val initialLocalOffset = lastTaskDownLocalOffset ?: startOffset
                        val startRootY = taskDragHandleCoords
                          ?.takeIf { it.isAttached }
                          ?.localToRoot(initialLocalOffset)
                          ?.y
                        currentOnStartDrag(startRootY)
                      },
                      onDrag = { change, dragAmount ->
                        change.consume()
                        val pointerRootY = taskDragHandleCoords
                          ?.takeIf { it.isAttached }
                          ?.localToRoot(change.position)
                          ?.y
                        currentOnDrag(dragAmount.y * currentDragScale, pointerRootY)
                      },
                      onDragEnd = {
                        draggingThis = false
                        lastTaskDownLocalOffset = null
                        currentOnDragHandlePressedChange(false)
                        currentOnEndDrag()
                      },
                      onDragCancel = {
                        draggingThis = false
                        lastTaskDownLocalOffset = null
                        currentOnDragHandlePressedChange(false)
                        currentOnEndDrag()
                      },
                    )
                  } finally {
                    if (draggingThis) {
                      lastTaskDownLocalOffset = null
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
          }

          // Checkbox to complete task
          Box(
            modifier = Modifier
              .size(32.dp)
              .scale(checkScale.value)
              .clip(CircleShape)
              .clickable {
                currentOnCheckboxClick()
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
        }

        // Title or Inline Editor (blends seamlessly into background, cursor at end)
        if (isEditingInline && !isSelectionMode) {
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
              fontWeight = FontWeight.SemiBold,
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
                  .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart,
              ) {
                innerTextField()
              }
            },
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .heightIn(min = 52.dp)
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
          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .heightIn(min = 52.dp)
              .clip(RoundedCornerShape(8.dp))
              .combinedClickable(
                onClick = {
                  if (isSelectionMode) {
                    currentOnToggleSelection()
                  } else {
                    onStartInlineEdit()
                  }
                },
                onDoubleClick = {
                  if (!isSelectionMode) {
                    currentOnSaveAsTemplate()
                  }
                },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  currentOnToggleSelection()
                },
              )
              .padding(vertical = 10.dp, horizontal = 4.dp)
              .testTag("todo_item_title_${task.id}"),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
          ) {
            Text(
              text = task.title,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold,
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
              val recurrenceSuffix = task.recurrenceRule?.let { " • ${it.formatShortLabel()}" } ?: ""
              Surface(
                color = if (isDue) {
                  MaterialTheme.colorScheme.tertiaryContainer
                } else {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                },
                shape = CircleShape,
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable(enabled = !isSelectionMode) { currentOnScheduleClick() }
                  .testTag("todo_item_schedule_label_${task.id}"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = when {
                      isDue -> Icons.Default.NotificationsActive
                      task.isRecurring -> Icons.Default.Repeat
                      else -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = if (isDue) {
                      MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                      MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.size(13.dp),
                  )
                  Text(
                    text = if (isDue) {
                      "Scheduled (Due) • $formattedSchedule$recurrenceSuffix"
                    } else {
                      "Scheduled • $formattedSchedule$recurrenceSuffix"
                    },
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
          }
        }

        // Subtasks progress counter, Add subtask (+) button & MoreVert (...) menu
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          if (subtasksCount > 0) {
            val caretRotation by animateFloatAsState(
              targetValue = if (task.isSubtasksCollapsed) 0f else 180f,
              animationSpec = tween(durationMillis = 250),
              label = "caret_rotation_${task.id}",
            )

            Box(
              modifier = Modifier
                .clip(CircleShape)
                .clickable(enabled = !isSelectionMode) {
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

          if (!isSelectionMode) {
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

            Box {
              IconButton(
                onClick = { showContextMenu = true },
                modifier = Modifier
                  .size(32.dp)
                  .testTag("todo_task_more_${task.id}"),
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "More options for ${task.title}",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                  modifier = Modifier.size(18.dp),
                )
              }

              DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                modifier = Modifier.testTag("todo_task_menu_${task.id}"),
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
                if (task.isScheduled) {
                  DropdownMenuItem(
                    text = { Text("Unschedule") },
                    leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null) },
                    onClick = {
                      showContextMenu = false
                      todoActions.onUnscheduleTask(task)
                    },
                  )
                }
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
                HorizontalDivider()
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
                DropdownMenuItem(
                  text = { Text("Copy") },
                  leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                  onClick = {
                    showContextMenu = false
                    todoActions.onCopyTask(task)
                  },
                )
                DropdownMenuItem(
                  text = { Text("Share") },
                  leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                  onClick = {
                    showContextMenu = false
                    todoActions.onShareTask(task)
                  },
                )
                HorizontalDivider()
                DropdownMenuItem(
                  text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.error,
                    )
                  },
                  onClick = {
                    showContextMenu = false
                    todoActions.onDeleteTask(task)
                  },
                )
              }
            }
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
            .padding(start = if (showDragHandle) 18.dp else 12.dp, end = 14.dp, bottom = 10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          // Subtask items (including nested subtasks indented by depth)
          val flattenedSubtasks = remember(task.subtasks) { flattenTodoSubTasks(task.subtasks) }
          flattenedSubtasks.forEach { (subtask, depth) ->
            key(subtask.id) {
              val isSubtaskBeingDragged = draggedSubtaskId == subtask.id
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(start = (depth * 18).dp)
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
                if (showDragHandle && !isSelectionMode) {
                  var subtaskDragHandleCoords by remember(subtask.id) { mutableStateOf<LayoutCoordinates?>(null) }
                  var lastSubtaskDownLocalOffset by remember(subtask.id) { mutableStateOf<Offset?>(null) }
                  // Smaller drag handle with double bar
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .onGloballyPositioned { subtaskDragHandleCoords = it }
                      .pointerInput(subtask.id) {
                        awaitPointerEventScope {
                          while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val firstNewDown = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                            if (firstNewDown != null) {
                              lastSubtaskDownLocalOffset = firstNewDown.position
                            }
                            currentOnDragHandlePressedChange(event.changes.any { it.pressed })
                          }
                        }
                      }
                      .pointerInput(subtask.id) {
                        var draggingThisSubtask = false
                        try {
                          detectDragGestures(
                            onDragStart = { startOffset ->
                              draggingThisSubtask = true
                              val initialLocalOffset = lastSubtaskDownLocalOffset ?: startOffset
                              val startRootY = subtaskDragHandleCoords
                                ?.takeIf { it.isAttached }
                                ?.localToRoot(initialLocalOffset)
                                ?.y
                              currentOnStartDragSubTask(subtask.id, startRootY)
                            },
                            onDrag = { change, dragAmount ->
                              change.consume()
                              val pointerRootY = subtaskDragHandleCoords
                                ?.takeIf { it.isAttached }
                                ?.localToRoot(change.position)
                                ?.y
                              currentOnDragSubTask(subtask.id, dragAmount.y * currentDragScale, pointerRootY)
                            },
                            onDragEnd = {
                              draggingThisSubtask = false
                              lastSubtaskDownLocalOffset = null
                              currentOnDragHandlePressedChange(false)
                              currentOnEndDragSubTask()
                            },
                            onDragCancel = {
                              draggingThisSubtask = false
                              lastSubtaskDownLocalOffset = null
                              currentOnDragHandlePressedChange(false)
                              currentOnEndDragSubTask()
                            },
                          )
                        } finally {
                          if (draggingThisSubtask) {
                            lastSubtaskDownLocalOffset = null
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
                }

                // Subtask checkbox
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !isSelectionMode) { onToggleSubTask(subtask.id) }
                    .testTag("todo_subtask_checkbox_${subtask.id}"),
                  contentAlignment = Alignment.Center,
                ) {
                  if (subtask.isCompleted) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = "Complete subtask",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp),
                    )
                  } else {
                    Icon(
                      imageVector = Icons.Default.RadioButtonUnchecked,
                      contentDescription = "Incomplete subtask",
                      tint = MaterialTheme.colorScheme.outline,
                      modifier = Modifier.size(20.dp),
                    )
                  }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Subtask Title or Inline Editor
                if (editingSubtaskId == subtask.id && !isSelectionMode) {
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
                      .clickable(enabled = !isSelectionMode) { editingSubtaskId = subtask.id }
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

                if (!isSelectionMode) {
                  IconButton(
                    onClick = { onPromoteSubTask(subtask.id) },
                    modifier = Modifier
                      .size(32.dp)
                      .testTag("todo_subtask_promote_${subtask.id}"),
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.CallMade,
                      contentDescription = "Move subtask to main task list",
                      tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                      modifier = Modifier.size(15.dp),
                    )
                  }

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
          }

          // Subtask input (shown when user presses + button, automatically focused)
          AnimatedVisibility(
            visible = showAddSubtaskInput && !isSelectionMode,
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
              if (showDragHandle) {
                Spacer(modifier = Modifier.width(38.dp))
              }

              val hexagonEnabled = currentHexagonIconButtons()
              Surface(
                shape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 4.dp,
                border = BorderStroke(
                  width = 1.dp,
                  color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("todo_subtask_input_card_${task.id}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(26.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer)
                      .clickable {
                        if (newSubtaskInput.trim().isNotEmpty()) {
                          onAddSubTask(newSubtaskInput)
                          newSubtaskInput = ""
                        } else {
                          newSubtaskFocusRequester.requestFocus()
                        }
                      },
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = "Add subtask",
                      tint = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.size(16.dp),
                    )
                  }

                  BasicTextField(
                    value = newSubtaskInput,
                    onValueChange = { newSubtaskInput = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Medium,
                      color = MaterialTheme.colorScheme.onSurface,
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
                          .fillMaxWidth()
                          .padding(vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart,
                      ) {
                        if (newSubtaskInput.isEmpty()) {
                          Text(
                            text = "Add a subtask...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                          )
                        }
                        innerTextField()
                      }
                    },
                    modifier = Modifier
                      .weight(1f)
                      .focusRequester(newSubtaskFocusRequester)
                      .testTag("todo_subtask_input_${task.id}"),
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
 * Floating 28.dp quick-add pill bar styled like NotesQuickCaptureBar,
 * with the circular Add badge, borderless inline task input, Schedule toggle, and Saved Tasks button
 * all integrated inside a single unified input surface.
 */
@Composable
private fun RoundedQuickAddBar(
  title: String,
  onTitleChange: (String) -> Unit,
  onAdd: () -> Unit,
  onOpenSavedTasks: () -> Unit,
  isScheduleOpen: Boolean = false,
  hasPendingSchedule: Boolean = false,
  onToggleSchedule: () -> Unit = {},
  inputFocusRequester: FocusRequester = remember { FocusRequester() },
  onInputFocusChanged: (Boolean) -> Unit = {},
) {
  val isEnabled = title.trim().isNotEmpty()
  val addScale by animateFloatAsState(
    targetValue = if (isEnabled) 1.06f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "add_btn_scale",
  )

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
      // Leading 28.dp circular Add icon badge inside the input bar (matching NotesQuickCaptureBar)
      Box(
        modifier = Modifier
          .size(28.dp)
          .scale(addScale)
          .clip(CircleShape)
          .background(
            if (isEnabled) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.primaryContainer
            },
          )
          .clickable {
            if (isEnabled) {
              onAdd()
            } else {
              inputFocusRequester.requestFocus()
            }
          }
          .testTag("todo_add_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Task",
          tint = if (isEnabled) {
            MaterialTheme.colorScheme.onPrimary
          } else {
            MaterialTheme.colorScheme.onPrimaryContainer
          },
          modifier = Modifier.size(18.dp),
        )
      }

      // Seamless borderless text input blending directly into the 28.dp pill bar
      BasicTextField(
        value = title,
        onValueChange = onTitleChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onAdd() }),
        decorationBox = { innerTextField ->
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.CenterStart,
          ) {
            if (title.isEmpty()) {
              Text(
                text = "Add a task...",
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
          .onFocusChanged { onInputFocusChanged(it.isFocused) }
          .testTag("todo_input_title"),
      )
    }

    // Trailing action icons inside the input pill (Clear when typing, Schedule task, Saved tasks)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (title.isNotEmpty()) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable { onTitleChange("") },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Clear input",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(
            if (isScheduleOpen || hasPendingSchedule) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              Color.Transparent
            },
          )
          .clickable(onClick = onToggleSchedule)
          .testTag("todo_schedule_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (isScheduleOpen) Icons.Default.KeyboardArrowDown else Icons.Default.Schedule,
          contentDescription = if (isScheduleOpen) "Dismiss schedule task" else "Schedule task",
          tint = if (isScheduleOpen || hasPendingSchedule) {
            MaterialTheme.colorScheme.onPrimaryContainer
          } else {
            MaterialTheme.colorScheme.primary
          },
          modifier = Modifier.size(18.dp),
        )
      }

      if (!isScheduleOpen) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onOpenSavedTasks)
            .testTag("todo_saved_tasks_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Bookmarks,
            contentDescription = "Saved tasks",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun SavedTaskSuggestionsList(
  suggestions: List<TodoItem>,
  onSelectSuggestion: (TodoItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (suggestions.isEmpty()) return
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
    tonalElevation = 3.dp,
    shadowElevation = 4.dp,
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("todo_saved_suggestions"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    ) {
      suggestions.forEach { savedTask ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectSuggestion(savedTask) }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("todo_saved_suggestion_${savedTask.id}"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Bookmarks,
            contentDescription = "Add saved task ${savedTask.title}",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = savedTask.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          if (savedTask.subtasks.isNotEmpty()) {
            Text(
              text = "${savedTask.subtasks.size} ${if (savedTask.subtasks.size == 1) "subtask" else "subtasks"}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
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

private data class FlattenedTodoSubTask(
  val subtask: TodoSubTask,
  val depth: Int,
)

private fun flattenTodoSubTasks(
  subtasks: List<TodoSubTask>,
  depth: Int = 0,
): List<FlattenedTodoSubTask> {
  val result = mutableListOf<FlattenedTodoSubTask>()
  for (sub in subtasks) {
    result.add(FlattenedTodoSubTask(sub, depth))
    if (sub.subtasks.isNotEmpty()) {
      result.addAll(flattenTodoSubTasks(sub.subtasks, depth + 1))
    }
  }
  return result
}

/**
 * Pill buttons row for switching between Tasks, Scheduled, and Completed views,
 * with a pinned floating [Saved Tasks] control on the right edge (Section 5.4 of DESIGN_LANGUAGE.md).
 */
@Composable
private fun TodoFilterPillRow(
  selectedTab: TodoFilterTab,
  tasksCount: Int,
  scheduledCount: Int,
  completedCount: Int,
  isTodoFolderGranted: Boolean = true,
  pendingPermissionFolderPath: String? = null,
  onOpenSavedTasks: () -> Unit = {},
  onRequestTodoFolderPermission: () -> Unit = {},
  onSelectTab: (TodoFilterTab) -> Unit,
) {
  ToolboxPillNavigationBar(
    rowTestTag = "todo_pill_bar",
    floatingButtonsCount = 1,
    pills = {
      ToolboxPillNavigationItem(
        selected = if (!isTodoFolderGranted) {
          pendingPermissionFolderPath != null
        } else {
          selectedTab == TodoFilterTab.TASKS
        },
        onClick = {
          if (!isTodoFolderGranted) {
            onRequestTodoFolderPermission()
          } else {
            onSelectTab(TodoFilterTab.TASKS)
          }
        },
        leadingIcon = if (isTodoFolderGranted) Icons.Default.TaskAlt else null,
        leadingIconContent = if (!isTodoFolderGranted) {
          {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .semantics(mergeDescendants = true) {
                  onClick {
                    onRequestTodoFolderPermission()
                    true
                  }
                }
                .testTag("todo_filter_lock_icon_TASKS"),
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Unlock ToDo folder",
                modifier = Modifier
                  .size(16.dp)
                  .testTag("folder_lock_icon_ToDo"),
              )
            }
          }
        } else {
          null
        },
        label = if (tasksCount == 1) "1 task" else "$tasksCount tasks",
        testTag = "todo_task_count_label",
        modifier = Modifier.graphicsLayer {
          alpha = if (!isTodoFolderGranted) 0.72f else 1f
        },
      )

      ToolboxPillNavigationItem(
        selected = isTodoFolderGranted && selectedTab == TodoFilterTab.SCHEDULED,
        onClick = {
          if (!isTodoFolderGranted) {
            onRequestTodoFolderPermission()
          } else {
            onSelectTab(TodoFilterTab.SCHEDULED)
          }
        },
        leadingIconContent = {
          Icon(
            imageVector = if (isTodoFolderGranted) Icons.Default.Schedule else Icons.Default.Lock,
            contentDescription = if (isTodoFolderGranted) "Scheduled" else "Unlock ToDo folder for Scheduled",
            modifier = Modifier
              .size(16.dp)
              .testTag(
                if (isTodoFolderGranted) {
                  "todo_filter_unlocked_icon_SCHEDULED"
                } else {
                  "todo_filter_lock_icon_SCHEDULED"
                }
              ),
          )
        },
        label = "$scheduledCount scheduled",
        testTag = "todo_scheduled_count_label",
        modifier = Modifier.graphicsLayer {
          alpha = if (!isTodoFolderGranted) 0.72f else 1f
        },
      )

      ToolboxPillNavigationItem(
        selected = isTodoFolderGranted && selectedTab == TodoFilterTab.COMPLETED,
        onClick = {
          if (!isTodoFolderGranted) {
            onRequestTodoFolderPermission()
          } else {
            onSelectTab(TodoFilterTab.COMPLETED)
          }
        },
        leadingIconContent = {
          Icon(
            imageVector = if (isTodoFolderGranted) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = if (isTodoFolderGranted) "Completed" else "Unlock ToDo folder for Completed",
            modifier = Modifier
              .size(16.dp)
              .testTag(
                if (isTodoFolderGranted) {
                  "todo_filter_unlocked_icon_COMPLETED"
                } else {
                  "todo_filter_lock_icon_COMPLETED"
                }
              ),
          )
        },
        label = "$completedCount completed",
        testTag = "todo_completed_today_counter",
        modifier = Modifier.graphicsLayer {
          alpha = if (!isTodoFolderGranted) 0.72f else 1f
        },
      )
    },
    floatingControls = {
      Surface(
        onClick = onOpenSavedTasks,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
          .size(34.dp)
          .testTag("todo_pill_saved_tasks_button"),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Bookmarks,
            contentDescription = "Saved task templates",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    },
  )
}

/**
 * View displayed in the To Do tool when scoped storage access is locked (permission to `/ToDo`
 * has not been granted yet), styled to match the Notes, Gallery, Audio Recorder, and Music Player tools.
 */
@Composable
private fun TodoLockedStorageStateView(
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
        text = "Tap the lock icon on a navigation pill above to grant permission to access $TODO_LIST_TEXT_FILE_NAME in /ToDo.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
  }
}

/**
 * View displayed when the user taps a locked navigation pill or its lock icon in the To Do tool,
 * asking for permission to access `to-do.text` in `/ToDo` and launching the Android system
 * directory access prompt when the user taps "Allow".
 */
@Composable
private fun TodoFolderPermissionRequestView(
  folder: TodoFolder,
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
        text = "To Do needs permission to access files in ${folder.folderPath} (${folder.folderName}) to read and save $TODO_LIST_TEXT_FILE_NAME in this location.",
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
        Text(
          text = "Allow",
          fontWeight = FontWeight.SemiBold,
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
          .size(64.dp)
          .scale(scale)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.TaskAlt,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp),
        )
      }

      Text(
        text = "All caught up!",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Text(
        text = "Swipe tasks right to complete, left to delete.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/**
 * Empty state when filtering by scheduled tasks.
 */
@Composable
private fun EmptyScheduledTasksState() {
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
          .size(64.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Schedule,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp),
        )
      }

      Text(
        text = "No scheduled tasks",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Text(
        text = "Tap the schedule icon below or long-press a task to set a reminder.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/**
 * Empty state when searching tasks yields no matches.
 */
@Composable
private fun EmptySearchTasksState() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp)
      .testTag("todo_search_empty_state"),
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
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp),
        )
      }

      Text(
        text = "No matching tasks",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Text(
        text = "Try a different keyword or filter tab",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/**
 * Inline view displaying tasks completed today, supporting multi-selection,
 * uncomplete, and swipe-to-remove.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun CompletedTasksListContent(
  completedTasks: List<TodoItem>,
  completedCount: Int,
  hasSearchQuery: Boolean = false,
  isSelectionMode: Boolean = false,
  selectedTaskIds: Set<String> = emptySet(),
  onToggleSelectTask: (String) -> Unit = {},
  historyVersion: Int = 0,
  onUncompleteTask: (TodoItem) -> Unit = {},
  onRemoveCompletedTask: (TodoItem) -> Unit = {},
) {
  val uncollapsedTaskIds = remember { mutableStateMapOf<String, Boolean>() }
  val currentOnUncompleteTask by rememberUpdatedState(onUncompleteTask)
  val currentOnRemoveCompletedTask by rememberUpdatedState(onRemoveCompletedTask)

  if (completedTasks.isEmpty()) {
    if (hasSearchQuery) {
      EmptySearchTasksState()
    } else {
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
              .size(64.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(32.dp),
            )
          }
          Text(
            text = if (completedCount > 0) "$completedCount task(s) completed today" else "No completed tasks yet today",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Completed tasks appear here and reset each day.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("todo_completed_tasks_list"),
      contentPadding = PaddingValues(
        start = 12.dp,
        end = 12.dp,
        top = 8.dp,
        bottom = 104.dp,
      ),
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

        LaunchedEffect(dismissState, dismissState.currentValue, isSelectionMode) {
          if (!isSelectionMode && !hasDismissTriggered) {
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
          enableDismissFromStartToEnd = !isSelectionMode,
          enableDismissFromEndToStart = !isSelectionMode,
          gesturesEnabled = !isSelectionMode,
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
                .clip(RoundedCornerShape(14.dp))
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
            CompletedTaskCard(
              task = task,
              isUncollapsed = isUncollapsed,
              isSelected = task.id in selectedTaskIds,
              isSelectionMode = isSelectionMode,
              onToggleUncollapsed = { uncollapsedTaskIds[task.id] = !isUncollapsed },
              onToggleSelect = { onToggleSelectTask(task.id) },
              onUncomplete = { currentOnUncompleteTask(task) },
              onDelete = { currentOnRemoveCompletedTask(task) },
            )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompletedTaskCard(
  task: TodoItem,
  isUncollapsed: Boolean,
  isSelected: Boolean = false,
  isSelectionMode: Boolean = false,
  onToggleUncollapsed: () -> Unit,
  onToggleSelect: () -> Unit = {},
  onUncomplete: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  var completedMenuExpanded by remember { mutableStateOf(false) }
  val completedActions = LocalTodoItemActions.current

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .combinedClickable(
        onClick = {
          if (isSelectionMode) {
            onToggleSelect()
          } else if (task.subtasks.isNotEmpty()) {
            onToggleUncollapsed()
          }
        },
        onLongClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onToggleSelect()
        },
      )
      .testTag("todo_completed_item_card_${task.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.background
      },
    ),
    border = if (isSelected) {
      BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
      BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    },
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 52.dp)
          .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (isSelectionMode) {
          Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelect() },
            colors = CheckboxDefaults.colors(
              checkedColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier
              .size(32.dp)
              .testTag("todo_completed_item_select_checkbox_${task.id}"),
          )
        } else {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .clickable { onUncomplete() }
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
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = task.title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          textDecoration = TextDecoration.LineThrough,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          modifier = Modifier.weight(1f),
        )

        if (task.subtasks.isNotEmpty()) {
          val caretRotation by animateFloatAsState(
            targetValue = if (isUncollapsed) 180f else 0f,
            animationSpec = tween(durationMillis = 250),
            label = "completed_caret_${task.id}",
          )

          Box(
            modifier = Modifier
              .clip(CircleShape)
              .clickable(enabled = !isSelectionMode) {
                onToggleUncollapsed()
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

        if (!isSelectionMode) {
          Box {
            IconButton(
              onClick = { completedMenuExpanded = true },
              modifier = Modifier
                .size(32.dp)
                .testTag("todo_completed_task_more_${task.id}"),
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options for ${task.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.size(18.dp),
              )
            }
            DropdownMenu(
              expanded = completedMenuExpanded,
              onDismissRequest = { completedMenuExpanded = false },
            ) {
              DropdownMenuItem(
                text = { Text("Uncomplete Task") },
                leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null) },
                onClick = {
                  completedMenuExpanded = false
                  onUncomplete()
                },
              )
              DropdownMenuItem(
                text = { Text("Copy") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                onClick = {
                  completedMenuExpanded = false
                  completedActions.onCopyTask(task)
                },
              )
              DropdownMenuItem(
                text = { Text("Share") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                onClick = {
                  completedMenuExpanded = false
                  completedActions.onShareTask(task)
                },
              )
              HorizontalDivider()
              DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                  )
                },
                onClick = {
                  completedMenuExpanded = false
                  onDelete()
                },
              )
            }
          }
        }
      }

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
            .padding(start = 32.dp, bottom = 10.dp),
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

/**
 * Modal sheet displaying tasks completed today, supporting swipe-to-remove.
 * Styled according to DESIGN_LANGUAGE.md Section 4 & 5.2 (28.dp top corners, surfaceContainerHigh, icon badge).
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
  val uncollapsedTaskIds = remember { mutableStateMapOf<String, Boolean>() }
  val currentOnUncompleteTask by rememberUpdatedState(onUncompleteTask)
  val currentOnRemoveCompletedTask by rememberUpdatedState(onRemoveCompletedTask)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
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
      // Header with 44.dp Circular Icon Badge
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
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
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Completed Today",
            style = MaterialTheme.typography.titleLarge,
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
            .size(36.dp)
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
            .size(36.dp)
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
            .size(36.dp)
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
                    .clip(RoundedCornerShape(14.dp))
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
                CompletedTaskCard(
                  task = task,
                  isUncollapsed = isUncollapsed,
                  onToggleUncollapsed = { uncollapsedTaskIds[task.id] = !isUncollapsed },
                  onUncomplete = { currentOnUncompleteTask(task) },
                  onDelete = { currentOnRemoveCompletedTask(task) },
                )
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
 * Styled according to DESIGN_LANGUAGE.md Section 4 & 5.2 (28.dp top corners, surfaceContainerHigh, icon badge).
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
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
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
      // Header with 44.dp Circular Icon Badge
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
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
            imageVector = Icons.Default.Bookmarks,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Saved Tasks",
            style = MaterialTheme.typography.titleLarge,
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
            .size(36.dp)
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

      // Quick save input inside sheet (styled like NotesQuickCaptureBar)
      val templateFocusRequester = remember { FocusRequester() }
      val isTemplateEnabled = newTemplateText.trim().isNotEmpty()
      val hexagonEnabled = currentHexagonIconButtons()
      Surface(
        shape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        border = BorderStroke(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp)
          .testTag("todo_save_template_input_card"),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(
                if (isTemplateEnabled) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.primaryContainer
                },
              )
              .clickable {
                if (isTemplateEnabled) {
                  onSaveTemplate(newTemplateText.trim())
                  newTemplateText = ""
                } else {
                  templateFocusRequester.requestFocus()
                }
              },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Save template",
              tint = if (isTemplateEnabled) {
                MaterialTheme.colorScheme.onPrimary
              } else {
                MaterialTheme.colorScheme.onPrimaryContainer
              },
              modifier = Modifier.size(18.dp),
            )
          }

          BasicTextField(
            value = newTemplateText,
            onValueChange = { newTemplateText = it },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
              onDone = {
                if (isTemplateEnabled) {
                  onSaveTemplate(newTemplateText.trim())
                  newTemplateText = ""
                }
              },
            ),
            decorationBox = { innerTextField ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                contentAlignment = Alignment.CenterStart,
              ) {
                if (newTemplateText.isEmpty()) {
                  Text(
                    text = "Save a new template...",
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
              .padding(vertical = 4.dp)
              .focusRequester(templateFocusRequester)
              .testTag("todo_save_template_input"),
          )

          if (newTemplateText.isNotEmpty()) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable { newTemplateText = "" },
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear template input",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
            }
          }
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
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Bookmarks,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No saved tasks yet",
              style = MaterialTheme.typography.titleMedium,
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
                .clip(RoundedCornerShape(14.dp))
                .clickable { onAddSavedTask(savedTask) }
                .testTag("todo_saved_item_${savedTask.id}"),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 52.dp)
                  .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = savedTask.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  if (savedTask.subtasks.isNotEmpty()) {
                    Text(
                      text = "${savedTask.subtasks.size} subtasks",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                IconButton(
                  onClick = { onDeleteSavedTask(savedTask.id) },
                  modifier = Modifier
                    .size(32.dp)
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
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .testTag("todo_due_notification_banner"),
    shape = RoundedCornerShape(14.dp),
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
            imageVector = Tool.TODO_LIST.icon,
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

private fun addCalendarOffset(baseMillis: Long, field: Int, amount: Int): Long {
  return Calendar.getInstance().apply {
    timeInMillis = baseMillis
    add(field, amount)
  }.timeInMillis
}

/**
 * Panel that slides up inside the floating 28.dp bottom pill bar for scheduling or rescheduling tasks,
 * with multi-row quick shortcuts and custom date/time pickers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTaskPanel(
  taskTitle: String,
  isExistingScheduled: Boolean,
  startMillis: Long,
  onStartMillisChange: (Long) -> Unit,
  recurrenceRule: TodoRecurrenceRule? = null,
  onRecurrenceRuleChange: (TodoRecurrenceRule?) -> Unit = {},
  selectedPreset: TodoRecurrenceRule = TodoRecurrenceRule(
    interval = 1,
    unit = TodoRecurrenceUnit.HOUR,
    presetLabel = "1 hr",
  ),
  onSelectedPresetChange: (TodoRecurrenceRule) -> Unit = {},
  onPresetSelected: () -> Unit = {},
  onConfirmSchedule: () -> Unit,
  onUnschedule: () -> Unit,
  onDismiss: () -> Unit,
) {
  var showStartDatePicker by remember { mutableStateOf(false) }
  var showStartTimePicker by remember { mutableStateOf(false) }
  var showCustomRecurrenceDialog by remember { mutableStateOf(false) }

  val dateFormatter = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.US) }
  val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.US) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("todo_schedule_dialog"),
  ) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f, fill = false)
        .heightIn(max = 500.dp)
        .verticalScroll(rememberScrollState())
        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Context Preview Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Text(
            text = taskTitle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          val scheduleLabelText = TodoListEngine.formatScheduleLabel(
            scheduledAtMillis = startMillis,
            scheduledEndMillis = null,
          )
          val recurrenceSummary = recurrenceRule?.let { " • ${it.formatLabel()}" } ?: ""
          Text(
            text = "Scheduled: $scheduleLabelText$recurrenceSummary",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("todo_schedule_preview_label"),
          )
        }
      }

      // Quick Relative Time Shortcuts in Separate Rows
      val shortcutRows = remember {
        listOf(
          listOf(
            Triple("5 min", Calendar.MINUTE, 5),
            Triple("10 min", Calendar.MINUTE, 10),
            Triple("15 min", Calendar.MINUTE, 15),
            Triple("20 min", Calendar.MINUTE, 20),
            Triple("30 min", Calendar.MINUTE, 30),
          ),
          listOf(
            Triple("1 hr", Calendar.HOUR_OF_DAY, 1),
            Triple("2 hr", Calendar.HOUR_OF_DAY, 2),
            Triple("4 hr", Calendar.HOUR_OF_DAY, 4),
            Triple("8 hr", Calendar.HOUR_OF_DAY, 8),
            Triple("12 hr", Calendar.HOUR_OF_DAY, 12),
          ),
          listOf(
            Triple("1 day", Calendar.DAY_OF_YEAR, 1),
            Triple("3 days", Calendar.DAY_OF_YEAR, 3),
            Triple("5 days", Calendar.DAY_OF_YEAR, 5),
            Triple("7 days", Calendar.DAY_OF_YEAR, 7),
          ),
          listOf(
            Triple("1 wk", Calendar.WEEK_OF_YEAR, 1),
            Triple("2 wks", Calendar.WEEK_OF_YEAR, 2),
            Triple("3 wks", Calendar.WEEK_OF_YEAR, 3),
            Triple("4 wks", Calendar.WEEK_OF_YEAR, 4),
          ),
          listOf(
            Triple("1 mo", Calendar.MONTH, 1),
            Triple("2 mo", Calendar.MONTH, 2),
            Triple("3 mo", Calendar.MONTH, 3),
            Triple("4 mo", Calendar.MONTH, 4),
            Triple("6 mo", Calendar.MONTH, 6),
          ),
        )
      }

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        shortcutRows.forEach { rowPresets ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            rowPresets.forEach { (label, calField, amount) ->
              val presetRule = TodoRecurrenceRule.fromPreset(label, calField, amount)
              val isPresetSelected = selectedPreset.unit == presetRule.unit &&
                selectedPreset.interval == presetRule.interval &&
                selectedPreset.presetLabel == presetRule.presetLabel
              val presetPillShape = if (currentHexagonIconButtons()) {
                HorizontalHexagonShape
              } else {
                RoundedCornerShape(20.dp)
              }
              Surface(
                color = if (isPresetSelected) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
                shape = presetPillShape,
                border = BorderStroke(
                  width = 1.dp,
                  color = if (isPresetSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                  },
                ),
                modifier = Modifier
                  .weight(1f)
                  .clip(presetPillShape)
                  .clickable {
                    onStartMillisChange(
                      addCalendarOffset(System.currentTimeMillis(), calField, amount),
                    )
                    onSelectedPresetChange(presetRule)
                    if (recurrenceRule != null) {
                      onRecurrenceRuleChange(presetRule)
                    }
                    onPresetSelected()
                  },
              ) {
                Box(
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isPresetSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isPresetSelected) {
                      MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                      MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                  )
                }
              }
            }
          }
        }
      }

      // Google Calendar-style Recurrence / Repeat Section
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Repeat,
            contentDescription = null,
            tint = if (recurrenceRule != null) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "Repeat",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          text = recurrenceRule?.formatLabel() ?: "Does not repeat",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = if (recurrenceRule != null) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
          modifier = Modifier.testTag("todo_schedule_recurrence_status"),
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val doesNotRepeatSelected = recurrenceRule == null
        FilterChip(
          selected = doesNotRepeatSelected,
          onClick = { onRecurrenceRuleChange(null) },
          shape = RoundedCornerShape(20.dp),
          label = {
            Text(
              text = "Does not repeat",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (doesNotRepeatSelected) FontWeight.Bold else FontWeight.Medium,
            )
          },
          modifier = Modifier.testTag("todo_recurrence_none"),
        )

        val repeatPresetSelected = recurrenceRule == selectedPreset
        FilterChip(
          selected = repeatPresetSelected,
          onClick = {
            onRecurrenceRuleChange(selectedPreset)
          },
          shape = RoundedCornerShape(20.dp),
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Repeat,
              contentDescription = null,
              modifier = Modifier.size(14.dp),
            )
          },
          label = {
            Text(
              text = selectedPreset.formatShortLabel(),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (repeatPresetSelected) FontWeight.Bold else FontWeight.Medium,
            )
          },
          modifier = Modifier.testTag("todo_recurrence_preset"),
        )

        val calendarPresets = listOf(
          "Daily" to TodoRecurrenceRule.DAILY,
          "Weekly" to TodoRecurrenceRule.WEEKLY,
          "Monthly" to TodoRecurrenceRule.MONTHLY,
          "Yearly" to TodoRecurrenceRule.YEARLY,
          "Weekdays (Mon–Fri)" to TodoRecurrenceRule.WEEKDAYS,
        )

        calendarPresets.forEach { (chipLabel, rule) ->
          val isSelected = recurrenceRule?.unit == rule.unit &&
            recurrenceRule.interval == rule.interval &&
            (rule.unit == TodoRecurrenceUnit.WEEKDAYS || recurrenceRule.presetLabel == rule.presetLabel)
          FilterChip(
            selected = isSelected,
            onClick = {
              onSelectedPresetChange(rule)
              onRecurrenceRuleChange(rule)
              onStartMillisChange(rule.nextScheduledAtMillis(System.currentTimeMillis()))
            },
            shape = RoundedCornerShape(20.dp),
            label = {
              Text(
                text = chipLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            },
            modifier = Modifier.testTag("todo_recurrence_${rule.unit.name.lowercase(Locale.US)}"),
          )
        }

        val isCustomSelected = recurrenceRule != null &&
          recurrenceRule != selectedPreset &&
          calendarPresets.none { (_, r) ->
            recurrenceRule.unit == r.unit &&
              recurrenceRule.interval == r.interval &&
              recurrenceRule.presetLabel == r.presetLabel
          }
        FilterChip(
          selected = isCustomSelected,
          onClick = { showCustomRecurrenceDialog = true },
          shape = RoundedCornerShape(20.dp),
          label = {
            Text(
              text = if (isCustomSelected) {
                "Custom (${recurrenceRule.formatShortLabel()})"
              } else {
                "Custom..."
              },
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium,
            )
          },
          modifier = Modifier.testTag("todo_recurrence_custom"),
        )
      }

      // Date & Time Pickers
      Text(
        text = "Date & Time",
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
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .weight(1f)
            .height(28.dp)
            .testTag("todo_schedule_start_date_button"),
        ) {
          Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
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
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier
            .height(28.dp)
            .testTag("todo_schedule_start_time_button"),
        ) {
          Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = timeFormatter.format(Date(startMillis)),
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }

    // Bottom Action Buttons (pinned below the scrollable content so they are never squished or resized)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .requiredSize(28.dp)
            .testTag("todo_schedule_cancel_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cancel",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.requiredSize(18.dp),
          )
        }
        if (isExistingScheduled) {
          TextButton(
            onClick = onUnschedule,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier
              .requiredHeight(28.dp)
              .testTag("todo_schedule_clear_button"),
          ) {
            Text(
              text = "Unschedule",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      FilledIconButton(
        onClick = onConfirmSchedule,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
          .requiredSize(28.dp)
          .testTag("todo_schedule_confirm_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = if (isExistingScheduled) "Reschedule" else "Schedule",
          modifier = Modifier.requiredSize(18.dp),
        )
      }
    }
  }

  if (showStartDatePicker) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = localMillisToUtcDateMillis(startMillis),
    )
    DatePickerDialog(
      onDismissRequest = { showStartDatePicker = false },
      shape = RoundedCornerShape(28.dp),
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { utcMillis ->
              onStartMillisChange(applyUtcDateToLocalMillis(utcMillis, startMillis))
            }
            showStartDatePicker = false
          },
          shape = CircleShape,
        ) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showStartDatePicker = false },
          shape = CircleShape,
        ) {
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
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp),
            )
          }
          Column {
            Text(
              text = "Select Start Time",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Choose when this task reminder should trigger",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      },
      text = {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          TimePicker(state = timePickerState)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onStartMillisChange(applyHourMinuteToLocalMillis(timePickerState.hour, timePickerState.minute, startMillis))
            showStartTimePicker = false
          },
          shape = CircleShape,
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showStartTimePicker = false },
          shape = CircleShape,
        ) {
          Text("Cancel")
        }
      },
    )
  }

  if (showCustomRecurrenceDialog) {
    var intervalInput by remember(recurrenceRule) {
      mutableStateOf((recurrenceRule?.interval ?: 1).toString())
    }
    var selectedUnit by remember(recurrenceRule) {
      val currentUnit = recurrenceRule?.unit ?: TodoRecurrenceUnit.DAY
      mutableStateOf(
        if (currentUnit == TodoRecurrenceUnit.WEEKDAYS) TodoRecurrenceUnit.DAY else currentUnit,
      )
    }
    val parsedInterval = intervalInput.trim().toIntOrNull()?.coerceIn(1, 365)
    val canConfirmCustom = parsedInterval != null && parsedInterval >= 1
    val selectableUnits = remember {
      listOf(
        TodoRecurrenceUnit.MINUTE,
        TodoRecurrenceUnit.HOUR,
        TodoRecurrenceUnit.DAY,
        TodoRecurrenceUnit.WEEK,
        TodoRecurrenceUnit.MONTH,
        TodoRecurrenceUnit.YEAR,
      )
    }

    AlertDialog(
      onDismissRequest = { showCustomRecurrenceDialog = false },
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
              imageVector = Icons.Default.Repeat,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp),
            )
          }
          Column {
            Text(
              text = "Custom Recurrence",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Choose how often this task repeats after completion",
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
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              val previewRule = TodoRecurrenceRule(
                interval = parsedInterval ?: 1,
                unit = selectedUnit,
              )
              Text(
                text = "$taskTitle • ${previewRule.formatLabel()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          OutlinedTextField(
            value = intervalInput,
            onValueChange = { input ->
              if (input.length <= 3 && input.all { it.isDigit() }) {
                intervalInput = input
              }
            },
            label = { Text("Repeats every") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Done,
            ),
            isError = !canConfirmCustom,
            supportingText = if (!canConfirmCustom) {
              { Text("Enter a number from 1 to 365") }
            } else {
              null
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("todo_custom_recurrence_interval_input"),
          )

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            selectableUnits.chunked(3).forEach { unitRow ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                unitRow.forEach { unitOption ->
                  val isUnitSelected = selectedUnit == unitOption
                  val unitText = if ((parsedInterval ?: 1) == 1) {
                    unitOption.singularLabel
                  } else {
                    unitOption.pluralLabel
                  }
                  FilterChip(
                    selected = isUnitSelected,
                    onClick = { selectedUnit = unitOption },
                    shape = RoundedCornerShape(20.dp),
                    label = {
                      Text(
                        text = unitText.replaceFirstChar { it.titlecase(Locale.US) },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isUnitSelected) FontWeight.Bold else FontWeight.Medium,
                      )
                    },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("todo_custom_recurrence_unit_${unitOption.name.lowercase(Locale.US)}"),
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (parsedInterval != null) {
              val customRule = TodoRecurrenceRule(
                interval = parsedInterval,
                unit = selectedUnit,
              )
              onSelectedPresetChange(customRule)
              onRecurrenceRuleChange(customRule)
              onStartMillisChange(customRule.nextScheduledAtMillis(System.currentTimeMillis()))
              showCustomRecurrenceDialog = false
            }
          },
          enabled = canConfirmCustom,
          shape = CircleShape,
          modifier = Modifier.testTag("todo_custom_recurrence_confirm"),
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Done")
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showCustomRecurrenceDialog = false },
          shape = CircleShape,
        ) {
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
