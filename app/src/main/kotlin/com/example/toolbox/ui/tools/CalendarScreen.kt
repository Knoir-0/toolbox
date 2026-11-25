package com.example.toolbox.ui.tools

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import com.example.toolbox.ui.main.ThemeMode
import com.example.toolbox.ui.theme.Button
import com.example.toolbox.ui.theme.CircleShape
import com.example.toolbox.ui.theme.FilterChip
import com.example.toolbox.ui.theme.FloatingActionButton
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.TextButton
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import com.example.toolbox.ui.theme.toolboxScreenBackground
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

@OptIn(
  ExperimentalMaterial3Api::class,
  ExperimentalFoundationApi::class,
  ExperimentalLayoutApi::class,
)
@Composable
fun CalendarScreen(
  modifier: Modifier = Modifier,
  repository: CalendarRepository? = null,
  todoRepository: TodoListRepository? = null,
  onOpenColorPicker: ((String) -> Unit)? = null,
  onOpenNotes: (() -> Unit)? = null,
  onOpenTodoList: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current

  val isDarkTheme = when (NavigationPreferencesBridge.themeMode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  val repo = remember(repository, context, isInspection) {
    repository ?: if (isInspection) {
      val today = CalendarDay.today()
      InMemoryCalendarRepository(
        initialEvents = listOf(
          CalendarEvent(
            id = "sample_standup",
            title = "Team Design Sync",
            description = "Review Q4 roadmap and Material 3 components",
            location = "Room 302 • Meet",
            startMillis = today.withTime(10, 0),
            endMillis = today.withTime(11, 0),
            category = CalendarCategory.WORK,
            color = CalendarEventColor.PEACOCK,
            recurrence = CalendarRecurrence.WEEKDAYS,
            isFavorite = true,
          ),
          CalendarEvent(
            id = "sample_workout",
            title = "Evening Run & Stretch",
            description = "5K neighborhood loop",
            location = "Lakeside Trail",
            startMillis = today.withTime(17, 30),
            endMillis = today.withTime(18, 30),
            category = CalendarCategory.HEALTH,
            color = CalendarEventColor.SAGE,
          ),
        ),
      )
    } else {
      SharedPreferencesCalendarRepository(context)
    }
  }

  val todoRepo = remember(todoRepository, context, isInspection) {
    todoRepository ?: if (isInspection) {
      InMemoryTodoListRepository()
    } else {
      SharedPreferencesTodoListRepository(context)
    }
  }

  var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { _ -> }

  LaunchedEffect(Unit) {
    if (!isInspection) {
      CalendarNotificationHelper.ensureNotificationChannel(context)
      CalendarNotificationHelper.syncEventAlarms(context, repo.loadEvents())
    }
    while (true) {
      delay(30_000L)
      nowMillis = System.currentTimeMillis()
    }
  }

  val today = remember(nowMillis) { CalendarDay.today(nowMillis) }
  var selectedYear by rememberSaveable { mutableIntStateOf(today.year) }
  var selectedMonth by rememberSaveable { mutableIntStateOf(today.month) }
  var selectedDayOfMonth by rememberSaveable { mutableIntStateOf(today.dayOfMonth) }

  val selectedDay = remember(selectedYear, selectedMonth, selectedDayOfMonth) {
    val maxDay = CalendarEngine.daysInMonth(selectedYear, selectedMonth)
    CalendarDay(selectedYear, selectedMonth.coerceIn(1, 12), selectedDayOfMonth.coerceIn(1, maxDay))
  }

  fun setSelectedDay(day: CalendarDay) {
    selectedYear = day.year
    selectedMonth = day.month
    selectedDayOfMonth = day.dayOfMonth
  }

  var userEvents by remember { mutableStateOf(repo.loadEvents()) }
  var viewMode by remember { mutableStateOf(repo.getViewMode()) }
  var enabledCategories by remember { mutableStateOf(repo.getEnabledCategories()) }
  var showTodos by remember { mutableStateOf(repo.getShowTodos()) }
  var showHolidays by remember { mutableStateOf(repo.getShowHolidays()) }

  val projectedTodoEvents = remember(showTodos, selectedYear, selectedMonth, userEvents) {
    if (!showTodos) {
      emptyList()
    } else {
      val activeTasks = todoRepo.loadTasks()
      val completedTasks = todoRepo.loadCompletedTasks(getTodayDateKey())
      CalendarEngine.projectTodoTasksToCalendarEvents(activeTasks, completedTasks)
    }
  }

  val builtInHolidays = remember(showHolidays, selectedYear, selectedMonth) {
    if (!showHolidays) {
      emptyList()
    } else {
      val years = buildSet {
        add(selectedYear)
        if (selectedMonth == 1) add(selectedYear - 1)
        if (selectedMonth >= 11) add(selectedYear + 1)
      }
      years.flatMap { y -> CalendarEngine.builtInHolidaysForYear(y) }
    }
  }

  val allEvents = remember(userEvents, projectedTodoEvents, builtInHolidays) {
    userEvents + projectedTodoEvents + builtInHolidays
  }

  // Search & Catalog Overlay State (Section 5.7 of DESIGN_LANGUAGE.md)
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var searchCategoryFilterName by rememberSaveable { mutableStateOf<String?>(null) }
  var searchFavoritesOnly by rememberSaveable { mutableStateOf(false) }

  val searchCategoryFilter = remember(searchCategoryFilterName) {
    searchCategoryFilterName?.let { name ->
      CalendarCategory.entries.firstOrNull { it.name == name }
    }
  }

  // Multi-selection State (Section 5.5 of DESIGN_LANGUAGE.md)
  var isSelectionMode by rememberSaveable { mutableStateOf(false) }
  var selectedEventIds by remember { mutableStateOf(setOf<String>()) }
  var showSelectionOverflowMenu by remember { mutableStateOf(false) }

  // Dialog States (Section 5.2 of DESIGN_LANGUAGE.md)
  var showEventEditorDialog by remember { mutableStateOf(false) }
  var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
  var prefillEvent by remember { mutableStateOf<CalendarEvent?>(null) }
  var editorInitialHour by remember { mutableIntStateOf(9) }
  var inspectingEvents by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
  var pendingDeleteEvents by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
  var showFilterCalendarsDialog by remember { mutableStateOf(false) }

  fun closeSearch() {
    isSearchActive = false
    searchQuery = ""
    searchCategoryFilterName = null
    searchFavoritesOnly = false
    focusManager.clearFocus()
  }

  fun exitSelectionMode() {
    isSelectionMode = false
    selectedEventIds = emptySet()
  }

  fun persistEvents(updated: List<CalendarEvent>) {
    val clean = updated.filter { !it.isBuiltInHoliday && it.linkedTodoTaskId == null }
    userEvents = clean
    repo.saveEvents(clean)
    if (!isInspection) {
      CalendarNotificationHelper.syncEventAlarms(context, clean)
      CalendarWidgets.updateAllWidgets(context)
    }
    val remainingIds = clean.map { it.id }.toSet()
    selectedEventIds = selectedEventIds.intersect(remainingIds)
    if (selectedEventIds.isEmpty()) {
      isSelectionMode = false
    }
  }

  fun toggleEventFavorite(event: CalendarEvent) {
    if (event.isReadOnlyBuiltIn) return
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = userEvents.map {
      if (it.id == event.id) it.copy(isFavorite = !it.isFavorite) else it
    }
    persistEvents(updated)
  }

  fun toggleEventSelection(eventId: String) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val next = if (eventId in selectedEventIds) {
      selectedEventIds - eventId
    } else {
      selectedEventIds + eventId
    }
    selectedEventIds = next
    if (next.isEmpty()) {
      isSelectionMode = false
    }
  }

  fun openNewEventDialog(day: CalendarDay = selectedDay, initialHour: Int = 9) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    setSelectedDay(day)
    editingEvent = null
    prefillEvent = null
    editorInitialHour = initialHour.coerceIn(0, 23)
    showEventEditorDialog = true
  }

  fun openEditEventDialog(event: CalendarEvent) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    if (event.isReadOnlyBuiltIn) {
      // Built-in holiday or To-Do task -> duplicate as custom editable event
      editingEvent = null
      prefillEvent = event.copy(
        id = UUID.randomUUID().toString(),
        isBuiltInHoliday = false,
        linkedTodoTaskId = null,
        category = if (event.category == CalendarCategory.HOLIDAYS || event.category == CalendarCategory.TASKS) {
          CalendarCategory.PERSONAL
        } else {
          event.category
        },
      )
    } else {
      editingEvent = event
      prefillEvent = null
    }
    showEventEditorDialog = true
  }

  fun duplicateEvents(eventsToCopy: List<CalendarEvent>) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val copies = eventsToCopy.map { orig ->
      orig.copy(
        id = UUID.randomUUID().toString(),
        title = "${orig.title} (Copy)",
        isBuiltInHoliday = false,
        linkedTodoTaskId = null,
        notifiedTriggerMillis = null,
      )
    }
    persistEvents(copies + userEvents)
    Toast.makeText(
      context,
      if (copies.size == 1) "Duplicated \"${copies.first().title}\"" else "Duplicated ${copies.size} events",
      Toast.LENGTH_SHORT,
    ).show()
  }

  // Handle incoming widget / cross-tool navigation requests
  LaunchedEffect(CalendarEngine.pendingNavigationRequest) {
    val req = CalendarEngine.consumePendingNavigationRequest() ?: return@LaunchedEffect
    req.targetDay?.let { setSelectedDay(it) }
    req.viewMode?.let {
      viewMode = it
      repo.setViewMode(it)
    }
    if (req.openAddEvent) {
      openNewEventDialog(req.targetDay ?: selectedDay)
    } else if (!req.openEventId.isNullOrBlank()) {
      val target = allEvents.firstOrNull { it.id == req.openEventId }
      if (target != null) {
        setSelectedDay(target.startDay)
        inspectingEvents = listOf(target)
      }
    }
  }

  BackHandler(enabled = isSelectionMode || isSearchActive) {
    when {
      isSelectionMode -> exitSelectionMode()
      isSearchActive -> closeSearch()
    }
  }

  val selectedDayOccurrences = remember(allEvents, selectedDay, enabledCategories) {
    CalendarEngine.occurrencesForDay(allEvents, selectedDay, enabledCategories)
  }

  val selectableUserEvents = remember(userEvents) { userEvents }

  val topBarTitle = remember(selectedDay, viewMode) {
    when (viewMode) {
      CalendarViewMode.MONTH, CalendarViewMode.SCHEDULE ->
        CalendarEngine.formatMonthYearTitle(selectedDay.year, selectedDay.month)
      CalendarViewMode.DAY ->
        "${selectedDay.monthShortName} ${selectedDay.dayOfMonth}, ${selectedDay.year}"
      CalendarViewMode.WEEK -> {
        val weekDays = CalendarEngine.buildWeekDays(selectedDay)
        val first = weekDays.first()
        val last = weekDays.last()
        if (first.month == last.month) {
          "${first.monthShortName} ${first.dayOfMonth}–${last.dayOfMonth}, ${first.year}"
        } else {
          "${first.monthShortName} ${first.dayOfMonth} – ${last.monthShortName} ${last.dayOfMonth}"
        }
      }
    }
  }

  val topBarSubtext = remember(selectedDay, selectedDayOccurrences, today) {
    val countText = when (val c = selectedDayOccurrences.size) {
      0 -> "No events"
      1 -> "1 event"
      else -> "$c events"
    }
    if (selectedDay == today) {
      "Today • ${selectedDay.dayOfWeekShortLabel}, ${selectedDay.monthShortName} ${selectedDay.dayOfMonth} • $countText"
    } else {
      "${selectedDay.dayOfWeekShortLabel}, ${selectedDay.monthShortName} ${selectedDay.dayOfMonth} • $countText"
    }
  }

  fun stepDatePeriod(delta: Int) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val nextDay = when (viewMode) {
      CalendarViewMode.MONTH, CalendarViewMode.SCHEDULE -> selectedDay.plusMonths(delta)
      CalendarViewMode.DAY -> selectedDay.plusDays(delta)
      CalendarViewMode.WEEK -> selectedDay.plusDays(delta * 7)
    }
    setSelectedDay(nextDay)
  }

  val hexagonEnabled = currentHexagonIconButtons()

  Column(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .testTag("calendar_screen"),
  ) {
    // 1. Unified 68.dp ToolboxTopBar (Section 5.1, 5.5, 5.7)
    ToolboxTopBar(
      title = topBarTitle,
      subtext = topBarSubtext,
      icon = Icons.Default.CalendarMonth,
      titleTestTag = "calendar_top_bar_title",
      subtextTestTag = "calendar_top_bar_subtext",
      selectionState = if (isSelectionMode) {
        val selectedItems = userEvents.filter { it.id in selectedEventIds }
        ToolboxTopBarSelectionState(
          selectedCount = selectedEventIds.size,
          totalCount = selectableUserEvents.size,
          onToggleSelectAll = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedEventIds = if (selectedEventIds.size == selectableUserEvents.size) {
              emptySet()
            } else {
              selectableUserEvents.map { it.id }.toSet()
            }
            if (selectedEventIds.isEmpty()) isSelectionMode = false
          },
          onCloseSelection = { exitSelectionMode() },
          actions = {
            // Inspect
            IconButton(
              onClick = {
                if (selectedItems.isNotEmpty()) {
                  inspectingEvents = selectedItems
                }
              },
              enabled = selectedItems.isNotEmpty(),
              modifier = Modifier
                .size(36.dp)
                .testTag("calendar_selection_inspect"),
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Inspect selected events",
                modifier = Modifier.size(20.dp),
              )
            }

            // Duplicate
            IconButton(
              onClick = {
                if (selectedItems.isNotEmpty()) {
                  duplicateEvents(selectedItems)
                  exitSelectionMode()
                }
              },
              enabled = selectedItems.isNotEmpty(),
              modifier = Modifier
                .size(36.dp)
                .testTag("calendar_selection_duplicate"),
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Duplicate selected events",
                modifier = Modifier.size(20.dp),
              )
            }

            // Share / Export .ics
            IconButton(
              onClick = {
                if (selectedItems.isNotEmpty()) {
                  val summary = selectedItems.joinToString("\n\n---\n\n") {
                    CalendarEngine.formatEventPlainText(it)
                  }
                  CrossToolActions.shareText(
                    context = context,
                    title = "Calendar Events (${selectedItems.size})",
                    text = summary,
                  )
                }
              },
              enabled = selectedItems.isNotEmpty(),
              modifier = Modifier
                .size(36.dp)
                .testTag("calendar_selection_share"),
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share selected events",
                modifier = Modifier.size(20.dp),
              )
            }

            // Delete (Red error tint)
            IconButton(
              onClick = {
                if (selectedItems.isNotEmpty()) {
                  pendingDeleteEvents = selectedItems
                }
              },
              enabled = selectedItems.isNotEmpty(),
              modifier = Modifier
                .size(36.dp)
                .testTag("calendar_selection_delete"),
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete selected events",
                tint = if (selectedItems.isNotEmpty()) {
                  MaterialTheme.colorScheme.error
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
                modifier = Modifier.size(20.dp),
              )
            }

            // Overflow Menu
            Box {
              IconButton(
                onClick = { showSelectionOverflowMenu = true },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("calendar_selection_more"),
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "More bulk actions",
                  modifier = Modifier.size(20.dp),
                )
              }
              DropdownMenu(
                expanded = showSelectionOverflowMenu,
                onDismissRequest = { showSelectionOverflowMenu = false },
              ) {
                DropdownMenuItem(
                  text = { Text("Export as .ics file") },
                  leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                  onClick = {
                    showSelectionOverflowMenu = false
                    if (selectedItems.isNotEmpty()) {
                      val ics = CalendarEngine.exportEventsToIcs(selectedItems)
                      CrossToolActions.exportTextAsFile(
                        context = context,
                        suggestedFileName = "toolbox_calendar.ics",
                        content = ics,
                      )
                      exitSelectionMode()
                    }
                  },
                )
                DropdownMenuItem(
                  text = { Text("Save to Notes") },
                  leadingIcon = { Icon(Icons.AutoMirrored.Filled.StickyNote2, contentDescription = null) },
                  onClick = {
                    showSelectionOverflowMenu = false
                    if (selectedItems.isNotEmpty()) {
                      val body = selectedItems.joinToString("\n\n") {
                        CalendarEngine.formatEventPlainText(it)
                      }
                      CrossToolActions.saveTextToNotes(
                        context = context,
                        title = if (selectedItems.size == 1) selectedItems.first().title else "Calendar Schedule (${selectedItems.size} events)",
                        content = body,
                        label = "calendar",
                      )
                      onOpenNotes?.invoke()
                      exitSelectionMode()
                    }
                  },
                )
                DropdownMenuItem(
                  text = { Text("Add to To Do") },
                  leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                  onClick = {
                    showSelectionOverflowMenu = false
                    selectedItems.forEach { ev ->
                      CrossToolActions.saveTaskToTodoList(
                        context = context,
                        title = ev.title,
                        notes = ev.description,
                        scheduledAtMillis = ev.startMillis,
                        showToast = false,
                      )
                    }
                    Toast.makeText(
                      context,
                      "Added ${selectedItems.size} event(s) to To Do",
                      Toast.LENGTH_SHORT,
                    ).show()
                    onOpenTodoList?.invoke()
                    exitSelectionMode()
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
        // Google Calendar "Today" badge button with current date number
        Surface(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            setSelectedDay(today)
          },
          shape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(18.dp),
          color = if (selectedDay == today) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
          },
          border = BorderStroke(
            width = 1.dp,
            color = if (selectedDay == today) {
              MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            } else {
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            },
          ),
          modifier = Modifier
            .height(32.dp)
            .testTag("calendar_action_today"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Today,
              contentDescription = "Jump to Today",
              tint = if (selectedDay == today) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(15.dp),
            )
            Text(
              text = today.dayOfMonth.toString(),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (selectedDay == today) {
                MaterialTheme.colorScheme.onPrimaryContainer
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
            )
          }
        }
      },
      searchState = ToolboxTopBarSearchState(
        isActive = isSearchActive,
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        onOpenSearch = { isSearchActive = true },
        onCloseSearch = { closeSearch() },
        placeholder = "Search events, holidays, locations...",
        toggleButtonTestTag = "calendar_action_search",
        inputTestTag = "calendar_search_input",
      ),
    )

    // 2. View Mode & Calendars Filter Pill Bar (Section 5.4 ToolboxPillNavigationBar)
    ToolboxPillNavigationBar(
      barTestTag = "calendar_view_mode_pill_bar",
      floatingButtonsCount = 1,
      backgroundColor = MaterialTheme.colorScheme.background,
      floatingControls = {
        Surface(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showFilterCalendarsDialog = true
          },
          shape = CircleShape,
          color = if (enabledCategories.size < CalendarCategory.entries.size) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
          },
          modifier = Modifier
            .size(34.dp)
            .testTag("calendar_filter_categories_button"),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.FilterList,
              contentDescription = "Filter calendars",
              tint = if (enabledCategories.size < CalendarCategory.entries.size) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(18.dp),
            )
          }
        }
      },
    ) {
      CalendarViewMode.entries.forEach { mode ->
        val modeIcon: ImageVector = when (mode) {
          CalendarViewMode.SCHEDULE -> Icons.AutoMirrored.Filled.ViewList
          CalendarViewMode.MONTH -> Icons.Default.CalendarMonth
          CalendarViewMode.WEEK -> Icons.Default.CalendarViewWeek
          CalendarViewMode.DAY -> Icons.Default.CalendarViewDay
        }
        ToolboxPillNavigationItem(
          selected = viewMode == mode,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewMode = mode
            repo.setViewMode(mode)
          },
          label = mode.label,
          leadingIcon = modeIcon,
          testTag = "calendar_view_pill_${mode.name.lowercase()}",
        )
      }
    }

    // 3. Main Content Box + Top-Expanding Search Catalog Overlay (Section 5.7)
    Box(modifier = Modifier.fillMaxSize()) {
      when (viewMode) {
        CalendarViewMode.MONTH -> {
          GoogleCalendarMonthView(
            year = selectedDay.year,
            month = selectedDay.month,
            selectedDay = selectedDay,
            today = today,
            allEvents = allEvents,
            enabledCategories = enabledCategories,
            isDarkTheme = isDarkTheme,
            isSelectionMode = isSelectionMode,
            selectedEventIds = selectedEventIds,
            onSelectDay = { day ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              setSelectedDay(day)
            },
            onOpenNewEvent = { day -> openNewEventDialog(day) },
            onEventClick = { event ->
              if (isSelectionMode) {
                if (!event.isReadOnlyBuiltIn) {
                  toggleEventSelection(event.id)
                }
              } else {
                inspectingEvents = listOf(event)
              }
            },
            onEventLongClick = { event ->
              if (!event.isReadOnlyBuiltIn) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isSelectionMode = true
                selectedEventIds = selectedEventIds + event.id
              } else {
                inspectingEvents = listOf(event)
              }
            },
            onToggleFavorite = { toggleEventFavorite(it) },
          )
        }

        CalendarViewMode.DAY, CalendarViewMode.WEEK -> {
          val timelineDays = remember(selectedDay, viewMode) {
            when (viewMode) {
              CalendarViewMode.DAY -> listOf(selectedDay)
              CalendarViewMode.WEEK -> CalendarEngine.buildWeekDays(selectedDay)
              else -> listOf(selectedDay)
            }
          }
          GoogleCalendarTimelineView(
            days = timelineDays,
            selectedDay = selectedDay,
            today = today,
            nowMillis = nowMillis,
            allEvents = allEvents,
            enabledCategories = enabledCategories,
            isDarkTheme = isDarkTheme,
            onSelectDay = { day ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              setSelectedDay(day)
            },
            onEmptySlotClick = { day, hour ->
              openNewEventDialog(day, initialHour = hour)
            },
            onEventClick = { event ->
              if (isSelectionMode && !event.isReadOnlyBuiltIn) {
                toggleEventSelection(event.id)
              } else {
                inspectingEvents = listOf(event)
              }
            },
          )
        }

        CalendarViewMode.SCHEDULE -> {
          GoogleCalendarScheduleAgendaView(
            anchorDay = selectedDay,
            today = today,
            nowMillis = nowMillis,
            allEvents = allEvents,
            enabledCategories = enabledCategories,
            isDarkTheme = isDarkTheme,
            isSelectionMode = isSelectionMode,
            selectedEventIds = selectedEventIds,
            onSelectDay = { setSelectedDay(it) },
            onEventClick = { event ->
              if (isSelectionMode && !event.isReadOnlyBuiltIn) {
                toggleEventSelection(event.id)
              } else {
                inspectingEvents = listOf(event)
              }
            },
            onEventLongClick = { event ->
              if (!event.isReadOnlyBuiltIn) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isSelectionMode = true
                selectedEventIds = selectedEventIds + event.id
              } else {
                inspectingEvents = listOf(event)
              }
            },
            onToggleFavorite = { toggleEventFavorite(it) },
          )
        }
      }

      // Integrated Expanding Search Catalog Overlay (Section 5.7)
      this@Column.AnimatedVisibility(
        visible = isSearchActive && !isSelectionMode,
        enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(220)) + fadeIn(tween(180)),
        exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(180)) + fadeOut(tween(150)),
        modifier = Modifier
          .fillMaxSize()
          .zIndex(20f),
      ) {
        val matchingEvents = remember(
          allEvents,
          searchQuery,
          searchCategoryFilter,
          searchFavoritesOnly,
        ) {
          CalendarEngine.searchEvents(
            events = allEvents,
            query = searchQuery,
            categoryFilter = searchCategoryFilter,
            favoritesOnly = searchFavoritesOnly,
          )
        }

        Surface(
          color = MaterialTheme.colorScheme.background,
          modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_search_overlay"),
        ) {
          Column(modifier = Modifier.fillMaxSize()) {
            ToolboxPillNavigationBar(
              barTestTag = "calendar_search_filter_bar",
            ) {
              ToolboxPillNavigationItem(
                selected = searchCategoryFilter == null && !searchFavoritesOnly,
                onClick = {
                  searchCategoryFilterName = null
                  searchFavoritesOnly = false
                },
                label = "All (${allEvents.size})",
                testTag = "calendar_search_filter_all",
              )
              ToolboxPillNavigationItem(
                selected = searchFavoritesOnly,
                onClick = { searchFavoritesOnly = !searchFavoritesOnly },
                label = "Starred",
                leadingIcon = Icons.Default.Star,
                testTag = "calendar_search_filter_starred",
              )
              CalendarCategory.entries.forEach { cat ->
                ToolboxPillNavigationItem(
                  selected = searchCategoryFilter == cat,
                  onClick = {
                    searchCategoryFilterName = if (searchCategoryFilter == cat) null else cat.name
                  },
                  label = cat.displayName,
                  leadingIconContent = {
                    Box(
                      modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(cat.defaultColorSwatch.accentColor),
                    )
                  },
                  testTag = "calendar_search_filter_${cat.name.lowercase()}",
                )
              }
            }

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              if (matchingEvents.isEmpty()) {
                item {
                  Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(top = 12.dp)
                      .testTag("calendar_search_empty_card"),
                  ) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                      )
                      Text(
                        text = "No matching calendar events",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                      )
                      Text(
                        text = "Try searching by event title, holiday name, category, color swatch, or month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                      )
                    }
                  }
                }
              } else {
                items(matchingEvents, key = { it.id }) { event ->
                  CalendarEventListCard(
                    event = event,
                    occurrenceDay = event.startDay,
                    isDarkTheme = isDarkTheme,
                    isSelected = false,
                    isSelectionMode = false,
                    showDateInSubtitle = true,
                    onClick = {
                      setSelectedDay(event.startDay)
                      closeSearch()
                      inspectingEvents = listOf(event)
                    },
                    onLongClick = {
                      setSelectedDay(event.startDay)
                      closeSearch()
                      inspectingEvents = listOf(event)
                    },
                    onToggleFavorite = { toggleEventFavorite(event) },
                  )
                }
              }
            }
          }
        }
      }

      // Floating Action Button (+) to create new event (Section 5.3)
      if (!isSearchActive && !isSelectionMode) {
        FloatingActionButton(
          onClick = { openNewEventDialog(selectedDay) },
          shape = CircleShape,
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .testTag("calendar_fab_add_event"),
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create event",
          )
        }
      }
    }
  }

  // 4. Dialogs (Section 5.2)
  if (showEventEditorDialog) {
    CalendarEventEditorDialog(
      editingEvent = editingEvent,
      prefillEvent = prefillEvent,
      defaultDay = selectedDay,
      defaultStartHour = editorInitialHour,
      isDarkTheme = isDarkTheme,
      onDismiss = {
        showEventEditorDialog = false
        editingEvent = null
        prefillEvent = null
      },
      onSave = { savedEvent ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (
          !isInspection &&
          savedEvent.reminderMinutesBefore >= 0 &&
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
          !CalendarNotificationHelper.hasNotificationPermission(context)
        ) {
          try {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          } catch (_: Throwable) {}
        }
        val updated = if (editingEvent != null) {
          userEvents.map { if (it.id == savedEvent.id) savedEvent else it }
        } else {
          listOf(savedEvent) + userEvents
        }
        persistEvents(updated)
        setSelectedDay(savedEvent.startDay)
        showEventEditorDialog = false
        editingEvent = null
        prefillEvent = null
      },
    )
  }

  if (inspectingEvents.isNotEmpty()) {
    CalendarEventInspectorDialog(
      events = inspectingEvents,
      isDarkTheme = isDarkTheme,
      onDismiss = { inspectingEvents = emptyList() },
      onEdit = { event ->
        inspectingEvents = emptyList()
        openEditEventDialog(event)
      },
      onDuplicate = { event ->
        inspectingEvents = emptyList()
        duplicateEvents(listOf(event))
      },
      onDelete = { event ->
        inspectingEvents = emptyList()
        if (!event.isReadOnlyBuiltIn) {
          pendingDeleteEvents = listOf(event)
        }
      },
      onOpenColorPicker = onOpenColorPicker,
      onSaveToNotes = { event ->
        CrossToolActions.saveTextToNotes(
          context = context,
          title = event.title,
          content = CalendarEngine.formatEventPlainText(event),
          label = "calendar",
        )
        onOpenNotes?.invoke()
      },
      onAddToTodo = { event ->
        CrossToolActions.saveTaskToTodoList(
          context = context,
          title = event.title,
          notes = event.description,
          scheduledAtMillis = event.startMillis,
        )
        onOpenTodoList?.invoke()
      },
      onExportIcs = { eventsToExport ->
        val ics = CalendarEngine.exportEventsToIcs(eventsToExport)
        val fileName = if (eventsToExport.size == 1) {
          "${eventsToExport.first().title.lowercase().replace(Regex("[^a-z0-9]+"), "_").ifBlank { "event" }}.ics"
        } else {
          "calendar_events.ics"
        }
        CrossToolActions.exportTextAsFile(
          context = context,
          suggestedFileName = fileName,
          content = ics,
        )
      },
    )
  }

  if (pendingDeleteEvents.isNotEmpty()) {
    CalendarDeleteConfirmDialog(
      events = pendingDeleteEvents,
      onDismiss = { pendingDeleteEvents = emptyList() },
      onConfirmDelete = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val idsToRemove = pendingDeleteEvents.map { it.id }.toSet()
        idsToRemove.forEach { CalendarNotificationHelper.cancelEventAlarm(context, it) }
        persistEvents(userEvents.filterNot { it.id in idsToRemove })
        pendingDeleteEvents = emptyList()
        exitSelectionMode()
      },
    )
  }

  if (showFilterCalendarsDialog) {
    CalendarFilterCategoriesDialog(
      enabledCategories = enabledCategories,
      showHolidays = showHolidays,
      showTodos = showTodos,
      onDismiss = { showFilterCalendarsDialog = false },
      onApply = { updatedCategories, updatedHolidays, updatedTodos ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        enabledCategories = updatedCategories
        showHolidays = updatedHolidays
        showTodos = updatedTodos
        repo.setEnabledCategories(updatedCategories)
        repo.setShowHolidays(updatedHolidays)
        repo.setShowTodos(updatedTodos)
        if (!isInspection) {
          CalendarWidgets.updateAllWidgets(context)
        }
        showFilterCalendarsDialog = false
      },
    )
  }
}

/**
 * Google Calendar-inspired Month View:
 * Top 6×7 month grid with weekday headers, circular today badge, and colored event chips/dots,
 * paired with the selected day's scrollable agenda list below.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GoogleCalendarMonthView(
  year: Int,
  month: Int,
  selectedDay: CalendarDay,
  today: CalendarDay,
  allEvents: List<CalendarEvent>,
  enabledCategories: Set<CalendarCategory>,
  isDarkTheme: Boolean,
  isSelectionMode: Boolean,
  selectedEventIds: Set<String>,
  onSelectDay: (CalendarDay) -> Unit,
  onOpenNewEvent: (CalendarDay) -> Unit,
  onEventClick: (CalendarEvent) -> Unit,
  onEventLongClick: (CalendarEvent) -> Unit,
  onToggleFavorite: (CalendarEvent) -> Unit,
) {
  val gridDays = remember(year, month) {
    CalendarEngine.buildMonthGridDays(year, month)
  }
  val occurrencesByDay = remember(allEvents, gridDays, enabledCategories) {
    CalendarEngine.occurrencesForDays(allEvents, gridDays, enabledCategories)
  }
  val selectedOccurrences = occurrencesByDay[selectedDay].orEmpty()
  val weekdayHeaders = remember { listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("calendar_month_view"),
    contentPadding = PaddingValues(bottom = 88.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // 1. Month Grid Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 4.dp)
          .testTag("calendar_month_grid_card"),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        ) {
          // Weekday Header Row (S M T W T F S)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 4.dp),
          ) {
            weekdayHeaders.forEachIndexed { idx, label ->
              val isWeekendCol = idx == 0 || idx == 6
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isWeekendCol) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
              )
            }
          }

          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 4.dp),
          )

          // 6 Weeks × 7 Days Grid
          val weeks = gridDays.chunked(7)
          weeks.forEach { weekDays ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
              horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              weekDays.forEach { day ->
                val isCurrentMonth = day.month == month && day.year == year
                val isToday = day == today
                val isSelected = day == selectedDay
                val dayOccurrences = occurrencesByDay[day].orEmpty()

                val cellBgColor = when {
                  isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                  isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                  else -> Color.Transparent
                }
                val cellBorderColor = when {
                  isSelected -> MaterialTheme.colorScheme.primary
                  isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                  else -> Color.Transparent
                }

                Column(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cellBgColor)
                    .then(
                      if (cellBorderColor != Color.Transparent) {
                        Modifier.border(1.dp, cellBorderColor, RoundedCornerShape(10.dp))
                      } else {
                        Modifier
                      }
                    )
                    .combinedClickable(
                      onClick = { onSelectDay(day) },
                      onLongClick = { onOpenNewEvent(day) },
                    )
                    .padding(horizontal = 2.dp, vertical = 3.dp)
                    .testTag("calendar_day_cell_${day.dateKey}"),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  // Day number badge (Google Calendar filled primary circle on Today)
                  Box(
                    modifier = Modifier
                      .size(22.dp)
                      .clip(CircleShape)
                      .background(
                        when {
                          isToday -> MaterialTheme.colorScheme.primary
                          isSelected -> MaterialTheme.colorScheme.primaryContainer
                          else -> Color.Transparent
                        }
                      ),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = day.dayOfMonth.toString(),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimary
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                      },
                    )
                  }

                  Spacer(modifier = Modifier.height(2.dp))

                  // Up to 2 miniature Google Calendar event chips inside the cell + overflow indicator
                  val previewOccurrences = dayOccurrences.take(2)
                  previewOccurrences.forEach { occ ->
                    val ev = occ.event
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ev.color.containerColor(isDarkTheme))
                        .padding(horizontal = 3.dp, vertical = 1.dp),
                    ) {
                      Text(
                        text = ev.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontSize = 8.5.sp,
                          lineHeight = 10.sp,
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = ev.color.contentColor(isDarkTheme),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }
                  }

                  if (dayOccurrences.size > 2) {
                    Text(
                      text = "+${dayOccurrences.size - 2} more",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                      ),
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                      maxLines = 1,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 2. Selected Day Header & Quick Add Row
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = CalendarEngine.formatDayHeader(selectedDay, today),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = if (selectedOccurrences.isEmpty()) {
              "No events scheduled • Tap + to add an event"
            } else {
              "${selectedOccurrences.size} scheduled ${if (selectedOccurrences.size == 1) "item" else "items"}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        TextButton(
          onClick = { onOpenNewEvent(selectedDay) },
          shape = CircleShape,
          modifier = Modifier.testTag("calendar_selected_day_quick_add"),
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add event")
        }
      }
    }

    // 3. Selected Day Events List
    if (selectedOccurrences.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("calendar_selected_day_empty_card"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onOpenNewEvent(selectedDay) }
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Nothing planned for ${selectedDay.monthShortName} ${selectedDay.dayOfMonth}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Tap to schedule an event, reminder, or birthday",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    } else {
      items(selectedOccurrences, key = { it.instanceId }) { occurrence ->
        val ev = occurrence.event
        Box(modifier = Modifier.padding(horizontal = 14.dp)) {
          CalendarEventListCard(
            event = ev,
            occurrenceDay = occurrence.occurrenceDay,
            isDarkTheme = isDarkTheme,
            isSelected = ev.id in selectedEventIds,
            isSelectionMode = isSelectionMode && !ev.isReadOnlyBuiltIn,
            showDateInSubtitle = false,
            onClick = { onEventClick(ev) },
            onLongClick = { onEventLongClick(ev) },
            onToggleFavorite = { onToggleFavorite(ev) },
          )
        }
      }
    }
  }
}

/**
 * Google Calendar-inspired 24-Hour Vertical Timeline Grid for Day (1 col), 3-Day (3 cols), and Week (7 cols):
 * - Sticky top header with day letters, circular day numbers, and all-day event banners.
 * - Scrollable 24-hour grid (`00:00` to `23:00`) with positioned color-coded event blocks and a live
 *   Google Calendar current-time indicator line + dot on Today.
 */
@Composable
private fun GoogleCalendarTimelineView(
  days: List<CalendarDay>,
  selectedDay: CalendarDay,
  today: CalendarDay,
  nowMillis: Long,
  allEvents: List<CalendarEvent>,
  enabledCategories: Set<CalendarCategory>,
  isDarkTheme: Boolean,
  onSelectDay: (CalendarDay) -> Unit,
  onEmptySlotClick: (CalendarDay, Int) -> Unit,
  onEventClick: (CalendarEvent) -> Unit,
) {
  val occurrencesByDay = remember(allEvents, days, enabledCategories) {
    CalendarEngine.occurrencesForDays(allEvents, days, enabledCategories)
  }
  val hourHeightDp = 56.dp
  val timeGutterWidth = 54.dp
  val scrollState = rememberScrollState()

  val currentHourFraction = remember(nowMillis) {
    val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60f)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("calendar_timeline_view"),
  ) {
    // 1. Sticky Day Headers & All-Day Event Strip
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp, end = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Spacer(modifier = Modifier.width(timeGutterWidth))
          days.forEach { day ->
            val isToday = day == today
            val isSelected = day == selectedDay
            Column(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onSelectDay(day) }
                .padding(vertical = 4.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                text = day.dayOfWeekShortLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isToday) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
              )
              Spacer(modifier = Modifier.height(2.dp))
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(CircleShape)
                  .background(
                    when {
                      isToday -> MaterialTheme.colorScheme.primary
                      isSelected -> MaterialTheme.colorScheme.primaryContainer
                      else -> Color.Transparent
                    }
                  ),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = day.dayOfMonth.toString(),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = when {
                    isToday -> MaterialTheme.colorScheme.onPrimary
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                  },
                )
              }
            }
          }
        }

        // All-Day events row (if any day has all-day events)
        val hasAnyAllDay = days.any { d -> occurrencesByDay[d].orEmpty().any { it.event.isAllDay } }
        if (hasAnyAllDay) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp, end = 8.dp),
            verticalAlignment = Alignment.Top,
          ) {
            Box(
              modifier = Modifier.width(timeGutterWidth),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "All day",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            days.forEach { day ->
              val allDayOccs = occurrencesByDay[day].orEmpty().filter { it.event.isAllDay }
              Column(
                modifier = Modifier
                  .weight(1f)
                  .padding(horizontal = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                allDayOccs.forEach { occ ->
                  val ev = occ.event
                  Surface(
                    onClick = { onEventClick(ev) },
                    shape = RoundedCornerShape(6.dp),
                    color = ev.color.containerColor(isDarkTheme),
                    border = BorderStroke(1.dp, ev.color.accentColor.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Text(
                      text = ev.title,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = ev.color.contentColor(isDarkTheme),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    )
                  }
                }
              }
            }
          }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      }
    }

    // 2. Scrollable 24-Hour Grid
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(bottom = 88.dp),
    ) {
      val totalGridHeight = hourHeightDp * 24
      val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
      val currentTimeLineColor = MaterialTheme.colorScheme.tertiary

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(totalGridHeight)
          .padding(end = 8.dp),
      ) {
        // Left Time Label Gutter (12 AM .. 11 PM)
        Column(
          modifier = Modifier
            .width(timeGutterWidth)
            .fillMaxHeight(),
        ) {
          for (hour in 0 until 24) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(hourHeightDp)
                .padding(end = 6.dp, top = 2.dp),
              contentAlignment = Alignment.TopEnd,
            ) {
              if (hour > 0) {
                Text(
                  text = CalendarEngine.formatHourSlotLabel(hour),
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }

        // Day Columns
        days.forEach { day ->
          val timedOccurrences = occurrencesByDay[day].orEmpty().filter { !it.event.isAllDay }
          val isToday = day == today

          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .border(
                width = 0.5.dp,
                color = dividerColor,
              ),
          ) {
            // Tappable Hour Slots
            Column(modifier = Modifier.fillMaxSize()) {
              for (hour in 0 until 24) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(hourHeightDp)
                    .clickable { onEmptySlotClick(day, hour) },
                ) {
                  HorizontalDivider(
                    color = dividerColor,
                    modifier = Modifier.align(Alignment.TopStart),
                  )
                }
              }
            }

            // Positioned Timed Event Blocks
            timedOccurrences.forEach { occ ->
              val ev = occ.event
              val topOffset = hourHeightDp * occ.startHourFraction
              val blockHeight = (hourHeightDp * (occ.endHourFraction - occ.startHourFraction))
                .coerceAtLeast(24.dp)

              Surface(
                onClick = { onEventClick(ev) },
                shape = RoundedCornerShape(8.dp),
                color = ev.color.containerColor(isDarkTheme),
                border = BorderStroke(1.dp, ev.color.accentColor),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 2.dp)
                  .offset(y = topOffset)
                  .height(blockHeight)
                  .testTag("calendar_timeline_event_${ev.id}"),
              ) {
                Row(modifier = Modifier.fillMaxSize()) {
                  // Left Google Calendar accent bar
                  Box(
                    modifier = Modifier
                      .width(4.dp)
                      .fillMaxHeight()
                      .background(ev.color.accentColor),
                  )
                  Column(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(horizontal = 6.dp, vertical = 2.dp),
                  ) {
                    Text(
                      text = ev.title,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = ev.color.contentColor(isDarkTheme),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    if (blockHeight >= 38.dp) {
                      Text(
                        text = CalendarEngine.formatTimeRange(
                          occ.instanceStartMillis,
                          occ.instanceEndMillis,
                          false,
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = ev.color.contentColor(isDarkTheme).copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }
                  }
                }
              }
            }

            // Google Calendar Current Time Indicator (Line + Leading Circle Dot) on Today
            if (isToday) {
              val nowOffset = hourHeightDp * currentHourFraction
              Canvas(
                modifier = Modifier
                  .fillMaxWidth()
                  .offset(y = nowOffset)
                  .height(2.dp),
              ) {
                drawCircle(
                  color = currentTimeLineColor,
                  radius = 4.5.dp.toPx(),
                  center = Offset(4.dp.toPx(), size.height / 2f),
                )
                drawLine(
                  color = currentTimeLineColor,
                  start = Offset(4.dp.toPx(), size.height / 2f),
                  end = Offset(size.width, size.height / 2f),
                  strokeWidth = 2.dp.toPx(),
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
 * Google Calendar-inspired Schedule (Agenda) View:
 * Chronological stream of upcoming days starting from [today] / [anchorDay] (never showing past days
 * or past events) with Google Calendar-style left date badge column (weekday + circular date number)
 * and right event cards.
 */
@Composable
private fun GoogleCalendarScheduleAgendaView(
  anchorDay: CalendarDay,
  today: CalendarDay,
  nowMillis: Long,
  allEvents: List<CalendarEvent>,
  enabledCategories: Set<CalendarCategory>,
  isDarkTheme: Boolean,
  isSelectionMode: Boolean,
  selectedEventIds: Set<String>,
  onSelectDay: (CalendarDay) -> Unit,
  onEventClick: (CalendarEvent) -> Unit,
  onEventLongClick: (CalendarEvent) -> Unit,
  onToggleFavorite: (CalendarEvent) -> Unit,
) {
  val occurrencesByDay = remember(allEvents, anchorDay, today, nowMillis, enabledCategories) {
    CalendarEngine.buildUpcomingScheduleDays(
      events = allEvents,
      anchorDay = anchorDay,
      today = today,
      nowMillis = nowMillis,
      enabledCategories = enabledCategories,
    )
  }
  val activeDaysWithEvents = remember(occurrencesByDay) {
    occurrencesByDay.keys.toList()
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("calendar_schedule_view"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items(activeDaysWithEvents, key = { it.dateKey }) { day ->
      val dayOccurrences = occurrencesByDay[day].orEmpty()
      val isToday = day == today

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("calendar_schedule_day_row_${day.dateKey}"),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Left Date Column (Google Calendar Schedule style)
        Column(
          modifier = Modifier
            .width(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelectDay(day) }
            .padding(vertical = 2.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = day.dayOfWeekShortLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isToday) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          )
          Spacer(modifier = Modifier.height(2.dp))
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(
                if (isToday) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                }
              ),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = day.dayOfMonth.toString(),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (isToday) {
                MaterialTheme.colorScheme.onPrimary
              } else {
                MaterialTheme.colorScheme.onSurface
              },
            )
          }
        }

        // Right Events Column
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (dayOccurrences.isEmpty()) {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              ),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = if (isToday) "No events scheduled for Today" else "No events scheduled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
              )
            }
          } else {
            dayOccurrences.forEach { occ ->
              val ev = occ.event
              CalendarEventListCard(
                event = ev,
                occurrenceDay = day,
                isDarkTheme = isDarkTheme,
                isSelected = ev.id in selectedEventIds,
                isSelectionMode = isSelectionMode && !ev.isReadOnlyBuiltIn,
                showDateInSubtitle = false,
                onClick = { onEventClick(ev) },
                onLongClick = { onEventLongClick(ev) },
                onToggleFavorite = { onToggleFavorite(ev) },
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(72.dp))
    }
  }
}

/**
 * Rounded 14.dp Google Calendar Event Card (Section 5.4, 5.5, 5.7):
 * - Color-coded left accent strip and circular badge (or Checkbox during multi-select mode).
 * - Title, time range, category, recurrence, and location metadata separated by `•`.
 * - Trailing Star favorite toggle button.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalendarEventListCard(
  event: CalendarEvent,
  occurrenceDay: CalendarDay,
  isDarkTheme: Boolean,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  showDateInSubtitle: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onToggleFavorite: () -> Unit,
) {
  val containerBg = if (isSelected) {
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
  } else {
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
  }

  val metadataParts = remember(event, occurrenceDay, showDateInSubtitle) {
    buildList {
      if (showDateInSubtitle) {
        add("${occurrenceDay.monthShortName} ${occurrenceDay.dayOfMonth}, ${occurrenceDay.year}")
      }
      add(CalendarEngine.formatTimeRange(event.startMillis, event.endMillis, event.isAllDay))
      add(event.category.displayName)
      if (event.recurrence != CalendarRecurrence.NONE) {
        add(event.recurrence.shortLabel)
      }
      if (event.location.isNotBlank()) {
        add(event.location)
      }
    }.joinToString(" • ")
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = containerBg),
    border = BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) {
        MaterialTheme.colorScheme.primary
      } else {
        event.color.accentColor.copy(alpha = 0.45f)
      },
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      )
      .testTag("calendar_event_card_${event.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (isSelectionMode) {
        Checkbox(
          checked = isSelected,
          onCheckedChange = { onClick() },
          modifier = Modifier.size(36.dp),
        )
      } else {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(event.color.containerColor(isDarkTheme))
            .border(1.5.dp, event.color.accentColor, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = when (event.category) {
              CalendarCategory.TASKS -> Icons.Default.Checklist
              CalendarCategory.HOLIDAYS -> Icons.Default.CalendarToday
              else -> Icons.Default.Event
            },
            contentDescription = null,
            tint = event.color.contentColor(isDarkTheme),
            modifier = Modifier.size(18.dp),
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = event.title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          textDecoration = if (event.isCompletedTask) TextDecoration.LineThrough else TextDecoration.None,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = metadataParts,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (event.description.isNotBlank()) {
          Text(
            text = event.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      if (!event.isReadOnlyBuiltIn) {
        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier
            .size(34.dp)
            .testTag("calendar_event_star_${event.id}"),
        ) {
          Icon(
            imageVector = if (event.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = if (event.isFavorite) "Remove from starred" else "Star event",
            tint = if (event.isFavorite) {
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

/**
 * Create / Edit Calendar Event Dialog adhering to DESIGN_LANGUAGE.md Section 5.2:
 * - 28.dp rounded corners, `surfaceContainerHigh`, `tonalElevation = 6.dp`
 * - 44.dp circular icon badge + title + subtitle
 * - Context preview card showing active date, time range, category, and color swatch
 * - Autofocus + pre-selected text in title field, inline validation, and `CircleShape` buttons
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CalendarEventEditorDialog(
  editingEvent: CalendarEvent?,
  prefillEvent: CalendarEvent?,
  defaultDay: CalendarDay,
  defaultStartHour: Int,
  isDarkTheme: Boolean,
  onDismiss: () -> Unit,
  onSave: (CalendarEvent) -> Unit,
) {
  val source = editingEvent ?: prefillEvent
  val initialTitle = source?.title.orEmpty()

  var titleField by remember {
    mutableStateOf(
      TextFieldValue(
        text = initialTitle,
        selection = TextRange(0, initialTitle.length),
      )
    )
  }
  var locationText by remember { mutableStateOf(source?.location.orEmpty()) }
  var descriptionText by remember { mutableStateOf(source?.description.orEmpty()) }
  var isAllDay by remember { mutableStateOf(source?.isAllDay ?: false) }
  var selectedCategory by remember {
    mutableStateOf(
      source?.category?.takeIf { it != CalendarCategory.HOLIDAYS && it != CalendarCategory.TASKS }
        ?: CalendarCategory.PERSONAL
    )
  }
  var selectedColor by remember {
    mutableStateOf(source?.color ?: selectedCategory.defaultColorSwatch)
  }
  var selectedRecurrence by remember {
    mutableStateOf(source?.recurrence ?: CalendarRecurrence.NONE)
  }
  var selectedReminder by remember {
    mutableStateOf(source?.reminderOption ?: CalendarReminderOption.THIRTY_MINUTES)
  }

  val initialDay = remember(source, defaultDay) {
    source?.startDay ?: defaultDay
  }
  var eventDay by remember { mutableStateOf(initialDay) }

  val initialStartCal = remember(source, defaultDay, defaultStartHour) {
    Calendar.getInstance().apply {
      timeInMillis = source?.startMillis ?: defaultDay.withTime(defaultStartHour, 0)
    }
  }
  val initialEndCal = remember(source, defaultDay, defaultStartHour) {
    Calendar.getInstance().apply {
      timeInMillis = source?.endMillis ?: defaultDay.withTime((defaultStartHour + 1).coerceAtMost(23), 0)
    }
  }

  var startHour by remember { mutableIntStateOf(initialStartCal.get(Calendar.HOUR_OF_DAY)) }
  var startMinute by remember { mutableIntStateOf(initialStartCal.get(Calendar.MINUTE)) }
  var endHour by remember { mutableIntStateOf(initialEndCal.get(Calendar.HOUR_OF_DAY)) }
  var endMinute by remember { mutableIntStateOf(initialEndCal.get(Calendar.MINUTE)) }

  var showDatePicker by remember { mutableStateOf(false) }
  var showStartTimePicker by remember { mutableStateOf(false) }
  var showEndTimePicker by remember { mutableStateOf(false) }

  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) {
    delay(80)
    runCatching { focusRequester.requestFocus() }
  }

  val startMillis = remember(eventDay, isAllDay, startHour, startMinute) {
    if (isAllDay) eventDay.toStartOfDayMillis() else eventDay.withTime(startHour, startMinute)
  }
  val endMillis = remember(eventDay, isAllDay, endHour, endMinute, startMillis) {
    if (isAllDay) {
      eventDay.toEndOfDayMillis()
    } else {
      eventDay.withTime(endHour, endMinute)
    }
  }

  val trimmedTitle = titleField.text.trim()
  val trimmedLocation = locationText.trim()
  val trimmedDescription = descriptionText.trim()
  val isTimeRangeValid = isAllDay || endMillis >= startMillis
  val isUnchanged = editingEvent != null &&
    editingEvent.title == trimmedTitle &&
    editingEvent.location == trimmedLocation &&
    editingEvent.description == trimmedDescription &&
    editingEvent.isAllDay == isAllDay &&
    editingEvent.category == selectedCategory &&
    editingEvent.color == selectedColor &&
    editingEvent.recurrence == selectedRecurrence &&
    editingEvent.reminderMinutesBefore == selectedReminder.minutesBefore &&
    editingEvent.startMillis == startMillis &&
    editingEvent.endMillis == maxOf(startMillis, endMillis)
  val canConfirm = trimmedTitle.isNotEmpty() && isTimeRangeValid && !isUnchanged

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
            imageVector = if (editingEvent != null) Icons.Default.Edit else Icons.Default.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = if (editingEvent != null) "Edit Event" else "New Calendar Event",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${eventDay.monthShortName} ${eventDay.dayOfMonth}, ${eventDay.year} • ${selectedCategory.displayName}",
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
          .heightIn(max = 440.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Context Preview Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          border = BorderStroke(1.dp, selectedColor.accentColor.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(selectedColor.containerColor(isDarkTheme))
                .border(1.5.dp, selectedColor.accentColor, CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = selectedColor.contentColor(isDarkTheme),
                modifier = Modifier.size(16.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = trimmedTitle.ifEmpty { "Untitled event" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "${CalendarEngine.formatDayHeader(eventDay)} • ${
                  CalendarEngine.formatTimeRange(startMillis, maxOf(startMillis, endMillis), isAllDay)
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Event Title Input
        OutlinedTextField(
          value = titleField,
          onValueChange = { titleField = it },
          label = { Text("Event title") },
          placeholder = { Text("Add title, meeting, or birthday...") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(Icons.Default.Event, contentDescription = null)
          },
          trailingIcon = {
            if (titleField.text.isNotEmpty()) {
              IconButton(onClick = { titleField = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear title")
              }
            }
          },
          isError = titleField.text.isNotEmpty() && trimmedTitle.isEmpty(),
          supportingText = if (titleField.text.isNotEmpty() && trimmedTitle.isEmpty()) {
            { Text("Event title cannot be blank") }
          } else if (!isTimeRangeValid) {
            { Text("End time must be at or after start time", color = MaterialTheme.colorScheme.error) }
          } else {
            null
          },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("calendar_editor_title_input"),
        )

        // All-Day Switch & Date Picker Chip
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "All-day",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
          Switch(
            checked = isAllDay,
            onCheckedChange = { isAllDay = it },
            modifier = Modifier.testTag("calendar_editor_all_day_switch"),
          )
        }

        // Date & Time Selector Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            onClick = { showDatePicker = true },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.weight(1.2f),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "${eventDay.monthShortName} ${eventDay.dayOfMonth}, ${eventDay.year}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
              )
            }
          }

          if (!isAllDay) {
            Surface(
              onClick = { showStartTimePicker = true },
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier.weight(0.9f),
            ) {
              Box(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = CalendarEngine.formatTime(startMillis),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }

            Surface(
              onClick = { showEndTimePicker = true },
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(
                1.dp,
                if (isTimeRangeValid) {
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                } else {
                  MaterialTheme.colorScheme.error
                },
              ),
              modifier = Modifier.weight(0.9f),
            ) {
              Box(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = CalendarEngine.formatTime(endMillis),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isTimeRangeValid) {
                    MaterialTheme.colorScheme.onSurface
                  } else {
                    MaterialTheme.colorScheme.error
                  },
                )
              }
            }
          }
        }

        // Category Pills
        Text(
          text = "Calendar Category",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          val editableCategories = remember {
            CalendarCategory.entries.filter {
              it != CalendarCategory.HOLIDAYS && it != CalendarCategory.TASKS
            }
          }
          editableCategories.forEach { cat ->
            ToolboxPillNavigationItem(
              selected = selectedCategory == cat,
              onClick = {
                selectedCategory = cat
                selectedColor = cat.defaultColorSwatch
              },
              label = cat.displayName,
              testTag = "calendar_editor_category_${cat.name.lowercase()}",
            )
          }
        }

        // Google Calendar 11 Color Swatches
        Text(
          text = "Event Color (${selectedColor.displayName})",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          CalendarEventColor.entries.forEach { swatch ->
            val isSwatchSelected = selectedColor == swatch
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(swatch.accentColor)
                .border(
                  width = if (isSwatchSelected) 2.5.dp else 1.dp,
                  color = if (isSwatchSelected) {
                    MaterialTheme.colorScheme.onSurface
                  } else {
                    Color.Transparent
                  },
                  shape = CircleShape,
                )
                .clickable { selectedColor = swatch }
                .testTag("calendar_editor_color_${swatch.name.lowercase()}"),
              contentAlignment = Alignment.Center,
            ) {
              if (isSwatchSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = swatch.displayName,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }

        // Recurrence Pills
        Text(
          text = "Recurrence",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          CalendarRecurrence.entries.forEach { rule ->
            ToolboxPillNavigationItem(
              selected = selectedRecurrence == rule,
              onClick = { selectedRecurrence = rule },
              label = rule.shortLabel,
              leadingIcon = if (rule != CalendarRecurrence.NONE) Icons.Default.Repeat else null,
              testTag = "calendar_editor_recurrence_${rule.name.lowercase()}",
            )
          }
        }

        // Reminder Notification Pills
        Text(
          text = "Notification Reminder",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          CalendarReminderOption.entries.forEach { rem ->
            ToolboxPillNavigationItem(
              selected = selectedReminder == rem,
              onClick = { selectedReminder = rem },
              label = rem.label,
              leadingIcon = if (rem != CalendarReminderOption.NONE) Icons.Default.NotificationsActive else null,
            )
          }
        }

        // Location Input
        OutlinedTextField(
          value = locationText,
          onValueChange = { locationText = it },
          label = { Text("Location") },
          placeholder = { Text("Add location or meeting link") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
          trailingIcon = {
            if (locationText.isNotEmpty()) {
              IconButton(onClick = { locationText = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear location")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_editor_location_input"),
        )

        // Notes / Description Input
        OutlinedTextField(
          value = descriptionText,
          onValueChange = { descriptionText = it },
          label = { Text("Description & notes") },
          placeholder = { Text("Add agenda, guests, or notes...") },
          minLines = 2,
          maxLines = 4,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_editor_description_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (canConfirm) {
            val result = CalendarEvent(
              id = editingEvent?.id ?: UUID.randomUUID().toString(),
              title = trimmedTitle,
              description = descriptionText.trim(),
              location = locationText.trim(),
              startMillis = startMillis,
              endMillis = maxOf(startMillis, endMillis),
              isAllDay = isAllDay,
              category = selectedCategory,
              color = selectedColor,
              recurrence = selectedRecurrence,
              reminderMinutesBefore = selectedReminder.minutesBefore,
              isFavorite = editingEvent?.isFavorite ?: false,
            )
            onSave(result)
          }
        },
        enabled = canConfirm,
        shape = CircleShape,
        modifier = Modifier.testTag("calendar_editor_save_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (editingEvent != null) "Save" else "Create")
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

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = eventDay.toStartOfDayMillis(),
    )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      shape = RoundedCornerShape(28.dp),
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { utcMillis ->
              val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = utcMillis
              }
              eventDay = CalendarDay(
                year = utcCal.get(Calendar.YEAR),
                month = utcCal.get(Calendar.MONTH) + 1,
                dayOfMonth = utcCal.get(Calendar.DAY_OF_MONTH),
              )
            }
            showDatePicker = false
          },
          shape = CircleShape,
        ) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showDatePicker = false },
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
    val timeState = rememberTimePickerState(
      initialHour = startHour,
      initialMinute = startMinute,
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
              text = "Choose when this event begins on ${eventDay.monthShortName} ${eventDay.dayOfMonth}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      },
      text = {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          TimePicker(state = timeState)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            startHour = timeState.hour
            startMinute = timeState.minute
            if (endHour < startHour || (endHour == startHour && endMinute < startMinute)) {
              endHour = (startHour + 1).coerceAtMost(23)
              endMinute = startMinute
            }
            showStartTimePicker = false
          },
          shape = CircleShape,
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Confirm")
        }
      },
      dismissButton = {
        TextButton(onClick = { showStartTimePicker = false }, shape = CircleShape) {
          Text("Cancel")
        }
      },
    )
  }

  if (showEndTimePicker) {
    val timeState = rememberTimePickerState(
      initialHour = endHour,
      initialMinute = endMinute,
    )
    AlertDialog(
      onDismissRequest = { showEndTimePicker = false },
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
              text = "Select End Time",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Choose when this event finishes on ${eventDay.monthShortName} ${eventDay.dayOfMonth}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      },
      text = {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          TimePicker(state = timeState)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            endHour = timeState.hour
            endMinute = timeState.minute
            showEndTimePicker = false
          },
          shape = CircleShape,
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Confirm")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEndTimePicker = false }, shape = CircleShape) {
          Text("Cancel")
        }
      },
    )
  }
}

/**
 * Event Inspector Dialog adhering to DESIGN_LANGUAGE.md Section 5.2, with cross-tool links to
 * Colors (swatch hex pill), Notes, To Do, and `.ics` iCalendar export.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarEventInspectorDialog(
  events: List<CalendarEvent>,
  isDarkTheme: Boolean,
  onDismiss: () -> Unit,
  onEdit: (CalendarEvent) -> Unit,
  onDuplicate: (CalendarEvent) -> Unit,
  onDelete: (CalendarEvent) -> Unit,
  onOpenColorPicker: ((String) -> Unit)?,
  onSaveToNotes: (CalendarEvent) -> Unit,
  onAddToTodo: (CalendarEvent) -> Unit,
  onExportIcs: (List<CalendarEvent>) -> Unit,
) {
  val context = LocalContext.current
  val single = events.singleOrNull()

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
            imageVector = if (single != null) Icons.Default.Event else Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = single?.title ?: "${events.size} Calendar Events",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = if (single != null) {
              "${single.category.displayName} • ${CalendarEngine.formatDayHeader(single.startDay)}"
            } else {
              "Selected events summary & actions"
            },
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
          .heightIn(max = 420.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (single != null) {
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
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Schedule,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = CalendarEngine.formatTimeRange(
                    single.startMillis,
                    single.endMillis,
                    single.isAllDay,
                  ),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }

              if (single.recurrence != CalendarRecurrence.NONE) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = single.recurrence.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              if (single.location.isNotBlank()) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = single.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }

              if (single.reminderOption != CalendarReminderOption.NONE) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = single.reminderOption.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              if (single.description.isNotBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text(
                  text = single.description,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }

          // Swatch Color Pill Linking to Colors Tool
          Surface(
            onClick = {
              onDismiss()
              onOpenColorPicker?.invoke(single.color.hex)
            },
            shape = RoundedCornerShape(20.dp),
            color = single.color.containerColor(isDarkTheme),
            border = BorderStroke(1.dp, single.color.accentColor),
            modifier = Modifier.testTag("calendar_inspector_color_pill"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(single.color.accentColor),
              )
              Text(
                text = "${single.color.displayName} • ${single.color.hex}",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = single.color.contentColor(isDarkTheme),
              )
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Open in Colors",
                tint = single.color.contentColor(isDarkTheme),
                modifier = Modifier.size(14.dp),
              )
            }
          }

          // Cross-tool & Quick Actions FlowRow
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            ToolboxPillNavigationItem(
              selected = false,
              onClick = { onDuplicate(single) },
              label = "Duplicate",
              leadingIcon = Icons.Default.ContentCopy,
            )
            ToolboxPillNavigationItem(
              selected = false,
              onClick = {
                CrossToolActions.copyTextToClipboard(
                  context = context,
                  label = single.title,
                  text = CalendarEngine.formatEventPlainText(single),
                )
              },
              label = "Copy Text",
              leadingIcon = Icons.Default.ContentCopy,
            )
            ToolboxPillNavigationItem(
              selected = false,
              onClick = {
                CrossToolActions.shareText(
                  context = context,
                  title = single.title,
                  text = CalendarEngine.formatEventPlainText(single),
                )
              },
              label = "Share",
              leadingIcon = Icons.Default.Share,
            )
            ToolboxPillNavigationItem(
              selected = false,
              onClick = {
                onDismiss()
                onSaveToNotes(single)
              },
              label = "Save to Notes",
              leadingIcon = Icons.AutoMirrored.Filled.StickyNote2,
            )
            if (single.linkedTodoTaskId == null) {
              ToolboxPillNavigationItem(
                selected = false,
                onClick = {
                  onDismiss()
                  onAddToTodo(single)
                },
                label = "Add to To Do",
                leadingIcon = Icons.Default.Checklist,
              )
            }
            ToolboxPillNavigationItem(
              selected = false,
              onClick = {
                onExportIcs(listOf(single))
              },
              label = "Export .ics",
              leadingIcon = Icons.Default.FileDownload,
            )
            if (!single.isReadOnlyBuiltIn) {
              ToolboxPillNavigationItem(
                selected = false,
                onClick = { onDelete(single) },
                label = "Delete",
                leadingIcon = Icons.Default.Delete,
                testTag = "calendar_inspector_delete_button",
              )
            }
          }
        } else {
          events.forEach { ev ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              ),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = ev.title,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "${CalendarEngine.formatDayHeader(ev.startDay)} • ${
                    CalendarEngine.formatTimeRange(ev.startMillis, ev.endMillis, ev.isAllDay)
                  }",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (single != null) {
        Button(
          onClick = { onEdit(single) },
          shape = CircleShape,
          modifier = Modifier.testTag("calendar_inspector_edit_button"),
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (single.isReadOnlyBuiltIn) "Customize Copy" else "Edit")
        }
      } else {
        Button(
          onClick = {
            onExportIcs(events)
            onDismiss()
          },
          shape = CircleShape,
        ) {
          Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Export .ics")
        }
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Close")
      }
    },
  )
}

/**
 * Destructive Delete Confirmation Dialog (Section 5.2).
 */
@Composable
private fun CalendarDeleteConfirmDialog(
  events: List<CalendarEvent>,
  onDismiss: () -> Unit,
  onConfirmDelete: () -> Unit,
) {
  val single = events.singleOrNull()
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
            text = if (single != null) "Delete Event?" else "Delete ${events.size} Events?",
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
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = if (single != null) {
            "Are you sure you want to delete \"${single.title}\"? It will be permanently removed from your calendar."
          } else {
            "Are you sure you want to permanently delete these ${events.size} events from your calendar?"
          },
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              if (single != null) {
                Text(
                  text = single.title,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "${CalendarEngine.formatDayHeader(single.startDay)} • ${
                    CalendarEngine.formatTimeRange(single.startMillis, single.endMillis, single.isAllDay)
                  }",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              } else {
                Text(
                  text = events.take(4).joinToString("\n") { "• ${it.title}" },
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                if (events.size > 4) {
                  Text(
                    text = "...and ${events.size - 4} more",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        onClick = onConfirmDelete,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("calendar_confirm_delete_button"),
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
 * Filter Calendars & Layers Dialog (Personal, Work, Birthdays, Health, Travel, Holidays, To-Do Tasks).
 */
@Composable
private fun CalendarFilterCategoriesDialog(
  enabledCategories: Set<CalendarCategory>,
  showHolidays: Boolean,
  showTodos: Boolean,
  onDismiss: () -> Unit,
  onApply: (Set<CalendarCategory>, Boolean, Boolean) -> Unit,
) {
  var selected by remember { mutableStateOf(enabledCategories) }
  var holidaysChecked by remember { mutableStateOf(showHolidays) }
  var todosChecked by remember { mutableStateOf(showTodos) }
  val isUnchanged = selected == enabledCategories &&
    holidaysChecked == showHolidays &&
    todosChecked == showTodos

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
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Visible Calendars",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Toggle calendar categories, holidays & tasks",
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
        // Context Preview Card
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
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${selected.size} of ${CalendarCategory.entries.size} calendars active",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = if (selected.isEmpty()) {
                  "No calendar layers visible"
                } else {
                  selected.joinToString(" • ") { it.displayName }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        CalendarCategory.entries.forEach { cat ->
          val isChecked = cat in selected
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                selected = if (isChecked) selected - cat else selected + cat
                if (cat == CalendarCategory.HOLIDAYS) holidaysChecked = !isChecked
                if (cat == CalendarCategory.TASKS) todosChecked = !isChecked
              },
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(cat.defaultColorSwatch.accentColor),
              )
              Text(
                text = cat.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
              )
              Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                  selected = if (checked) selected + cat else selected - cat
                  if (cat == CalendarCategory.HOLIDAYS) holidaysChecked = checked
                  if (cat == CalendarCategory.TASKS) todosChecked = checked
                },
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onApply(selected, holidaysChecked, todosChecked) },
        enabled = !isUnchanged,
        shape = CircleShape,
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Apply")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, shape = CircleShape) {
        Text("Cancel")
      }
    },
  )
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
  ToolboxTheme {
    CalendarScreen()
  }
}
