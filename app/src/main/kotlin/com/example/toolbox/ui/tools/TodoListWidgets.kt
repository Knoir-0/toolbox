package com.example.toolbox.ui.tools

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.Toast
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.toolbox.R
import com.example.toolbox.ui.main.BackgroundTexture
import com.example.toolbox.ui.main.DEFAULT_ACCENT_COLOR_AUTO
import com.example.toolbox.ui.main.DEFAULT_MUSIC_PLAYER_COLOR_OPTION
import com.example.toolbox.ui.main.DEFAULT_MUSIC_PLAYER_THEME_ENABLED
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.NAVIGATION_PREFERENCES_NAME
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import com.example.toolbox.ui.main.PREF_KEY_ACCENT_COLOR
import com.example.toolbox.ui.main.PREF_KEY_MUSIC_PLAYER_COLOR_OPTION
import com.example.toolbox.ui.main.PREF_KEY_MUSIC_PLAYER_THEME_ENABLED
import com.example.toolbox.ui.main.PREF_KEY_THEME_COLOR
import com.example.toolbox.ui.main.PREF_KEY_THEME_MODE
import com.example.toolbox.ui.main.ThemeMode
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.createStandaloneToolIntent
import com.example.toolbox.ui.main.createTodoQuickActionIntent
import com.example.toolbox.ui.theme.drawWidgetBackgroundTextureOverlay
import com.example.toolbox.ui.theme.isMusicPlayerAccentColorSetting
import com.example.toolbox.ui.theme.isMusicPlayerThemeColorSetting
import com.example.toolbox.ui.theme.isStarsSignThemeColorSetting
import com.example.toolbox.ui.theme.resolveToolboxColorScheme
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Snapshot of the task list, undo toast state, and theme palette rendered by the To-Do Home Screen
 * AppWidget ([TodoListWidgetProvider]).
 */
@Immutable
data class TodoWidgetState(
  val visibleTasks: List<TodoItem>,
  val totalVisibleCount: Int,
  val completedTodayCount: Int,
  val headerSubtitle: String,
  val pendingUndoRecord: TodoWidgetUndoRecord?,
  val isUndoToastVisible: Boolean,
  val undoToastMessage: String,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
)

/**
 * Snapshot recorded when a task is completed so the user can tap "Undo" on the widget toast banner
 * to restore the exact pre-completion state.
 */
@Immutable
data class TodoWidgetUndoRecord(
  val completedTaskId: String,
  val completedTaskTitle: String,
  val completedAtMillis: Long,
  val preCompletionSnapshot: TodoHistorySnapshot,
) {
  fun isActive(
    nowMillis: Long = System.currentTimeMillis(),
    timeoutMillis: Long = TodoListWidgets.UNDO_TOAST_DURATION_MS,
  ): Boolean = (nowMillis - completedAtMillis) in 0L..timeoutMillis

  fun toJson(): JSONObject = JSONObject().apply {
    put("completedTaskId", completedTaskId)
    put("completedTaskTitle", completedTaskTitle)
    put("completedAtMillis", completedAtMillis)
    put("completedToday", preCompletionSnapshot.completedToday)
    val tasksArray = org.json.JSONArray()
    preCompletionSnapshot.tasks.forEach { tasksArray.put(it.toJson()) }
    put("tasks", tasksArray)
    val completedArray = org.json.JSONArray()
    preCompletionSnapshot.completedTasks.forEach { completedArray.put(it.toJson()) }
    put("completedTasks", completedArray)
  }

  companion object {
    fun fromJson(json: JSONObject?): TodoWidgetUndoRecord? {
      if (json == null) return null
      val taskId = json.optString("completedTaskId", "").takeIf { it.isNotBlank() } ?: return null
      val title = json.optString("completedTaskTitle", "Task")
      val completedAt = json.optLong("completedAtMillis", 0L).takeIf { it > 0L } ?: return null
      val completedToday = json.optInt("completedToday", 0).coerceAtLeast(0)
      val tasks = mutableListOf<TodoItem>()
      val tasksArray = json.optJSONArray("tasks")
      if (tasksArray != null) {
        for (i in 0 until tasksArray.length()) {
          tasksArray.optJSONObject(i)?.let { tasks.add(TodoItem.fromJson(it)) }
        }
      }
      val completedTasks = mutableListOf<TodoItem>()
      val completedArray = json.optJSONArray("completedTasks")
      if (completedArray != null) {
        for (i in 0 until completedArray.length()) {
          completedArray.optJSONObject(i)?.let { completedTasks.add(TodoItem.fromJson(it)) }
        }
      }
      return TodoWidgetUndoRecord(
        completedTaskId = taskId,
        completedTaskTitle = title,
        completedAtMillis = completedAt,
        preCompletionSnapshot = TodoHistorySnapshot(
          tasks = tasks,
          completedToday = completedToday,
          completedTasks = completedTasks,
        ),
      )
    }
  }
}

/**
 * Resolved ARGB color tokens used to style the To-Do Home Screen AppWidget according to the user's
 * active Material 3 theme and dark/light mode in Settings.
 */
@Immutable
data class TodoWidgetPalette(
  val surfaceArgb: Int,
  val itemCardArgb: Int,
  val borderArgb: Int,
  val primaryArgb: Int,
  val onPrimaryArgb: Int,
  val primaryContainerArgb: Int,
  val onPrimaryContainerArgb: Int,
  val secondaryContainerArgb: Int,
  val onSecondaryContainerArgb: Int,
  val tertiaryContainerArgb: Int,
  val onTertiaryContainerArgb: Int,
  val onSurfaceArgb: Int,
  val onSurfaceVariantArgb: Int,
  val outlineArgb: Int,
  val toastSurfaceArgb: Int,
  val toastBorderArgb: Int,
  val hexagonIconButtons: Boolean = false,
  val iconButtonBackgroundRes: Int = R.drawable.bg_music_widget_circle,
  val taskCheckUncheckedIconRes: Int = R.drawable.ic_todo_widget_circle_unchecked,
  val taskCheckCheckedIconRes: Int = R.drawable.ic_todo_widget_circle_checked,
  val backgroundTexture: BackgroundTexture = BackgroundTexture.NONE,
  val secondaryArgb: Int = primaryArgb,
  val tertiaryArgb: Int = primaryArgb,
  val isDarkTheme: Boolean = true,
) {
  val itemSurfaceArgb: Int get() = itemCardArgb
}

/**
 * Central state resolver, RemoteViews builder, and action handler for the To-Do List Home Screen
 * AppWidget ([TodoListWidgetProvider]):
 * - Displays the scrollable To-Do task list (main view tasks within the 24-hour schedule horizon)
 * - Resizable horizontally and vertically within reasonable bounds
 * - No drag handle or reordering controls in the widget
 * - Keeps the check circle on each task item to complete tasks directly from the home screen
 * - Upon completion, pops up an undo toast (both an interactive in-widget Undo toast pill and a
 *   system Toast) allowing the user to undo the completion
 * - Features two floating circular FABs at the bottom-right: one for Plus (`+`) and one for
 *   Schedule, opening the To-Do tool directly with the "Add a task..." input focused or the
 *   schedule view opened
 */
object TodoListWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.todo.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_ITEM_CLICK = "com.example.toolbox.todo.ACTION_WIDGET_ITEM_CLICK"
  const val ACTION_WIDGET_COMPLETE_TASK = "com.example.toolbox.todo.ACTION_WIDGET_COMPLETE_TASK"
  const val ACTION_WIDGET_UNDO_COMPLETE = "com.example.toolbox.todo.ACTION_WIDGET_UNDO_COMPLETE"
  const val ACTION_WIDGET_DISMISS_UNDO = "com.example.toolbox.todo.ACTION_WIDGET_DISMISS_UNDO"
  const val ACTION_WIDGET_OPEN_TODO_ADD_TASK = "com.example.toolbox.todo.ACTION_WIDGET_OPEN_TODO_ADD_TASK"
  const val ACTION_WIDGET_OPEN_TODO_SCHEDULE = "com.example.toolbox.todo.ACTION_WIDGET_OPEN_TODO_SCHEDULE"

  const val EXTRA_TASK_ID = "extra_todo_widget_task_id"
  const val EXTRA_ITEM_CLICK_ACTION = "extra_todo_widget_item_click_action"
  const val ITEM_CLICK_ACTION_COMPLETE = "complete"
  const val ITEM_CLICK_ACTION_OPEN = "open"

  const val KEY_WIDGET_LAST_UNDO_JSON = "todo_widget_last_undo_json"
  const val UNDO_TOAST_DURATION_MS = 8_000L

  const val WIDGET_CORNER_RADIUS_DP = 16f

  private const val REQUEST_CODE_OPEN_TODO = 3100
  private const val REQUEST_CODE_ITEM_TEMPLATE = 3101
  private const val REQUEST_CODE_UNDO_COMPLETE = 3102
  private const val REQUEST_CODE_DISMISS_UNDO = 3103
  private const val REQUEST_CODE_FAB_ADD_TASK = 3104
  private const val REQUEST_CODE_FAB_SCHEDULE = 3105

  @Volatile
  private var inMemoryUndoRecord: TodoWidgetUndoRecord? = null

  /**
   * Refreshes all pinned Home Screen AppWidgets across every Toolbox tool whenever theme colors,
   * accent colors, dynamic media colors, Star Sign colors, background textures, or Hexagonify change.
   */
  fun refreshAllToolboxWidgets(context: Context?) {
    NavigationPreferencesBridge.refreshAllWidgets(context)
  }

  /**
   * Resolves the current Material 3 [TodoWidgetPalette] matching the user's theme settings
   * (using [resolveToolboxColorScheme] so widget colors and in-app [com.example.toolbox.ui.theme.ToolboxTheme]
   * colors stay 100% synchronized).
   */
  fun resolveWidgetPalette(context: Context? = null): TodoWidgetPalette {
    val isSystemDark = try {
      val uiMode = context?.resources?.configuration?.uiMode ?: Configuration.UI_MODE_NIGHT_YES
      (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    } catch (_: Throwable) {
      true
    }

    val prefs = try {
      context?.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
    } catch (_: Throwable) {
      null
    }
    val activeRepo = NavigationPreferencesBridge.activeRepository
    val useInMemoryRepo = activeRepo is InMemoryNavigationPreferencesRepository

    val themeMode = if (!useInMemoryRepo && prefs != null && prefs.contains(PREF_KEY_THEME_MODE)) {
      val themeModeName = prefs.getString(PREF_KEY_THEME_MODE, ThemeMode.SYSTEM.name)
      ThemeMode.entries.find { it.name.equals(themeModeName, ignoreCase = true) } ?: ThemeMode.SYSTEM
    } else {
      NavigationPreferencesBridge.themeMode
    }
    val darkTheme = when (themeMode) {
      ThemeMode.SYSTEM -> isSystemDark
      ThemeMode.LIGHT -> false
      ThemeMode.DARK -> true
    }

    val themeColor = if (!useInMemoryRepo && prefs != null && prefs.contains(PREF_KEY_THEME_COLOR)) {
      prefs.getString(PREF_KEY_THEME_COLOR, DEFAULT_THEME_COLOR_SYSTEM) ?: DEFAULT_THEME_COLOR_SYSTEM
    } else {
      NavigationPreferencesBridge.themeColor
    }
    val accentColor = if (!useInMemoryRepo && prefs != null && prefs.contains(PREF_KEY_ACCENT_COLOR)) {
      prefs.getString(PREF_KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR_AUTO) ?: DEFAULT_ACCENT_COLOR_AUTO
    } else {
      NavigationPreferencesBridge.accentColor
    }
    val musicThemeEnabled = if (
      !useInMemoryRepo &&
      prefs != null &&
      prefs.contains(PREF_KEY_MUSIC_PLAYER_THEME_ENABLED)
    ) {
      prefs.getBoolean(PREF_KEY_MUSIC_PLAYER_THEME_ENABLED, DEFAULT_MUSIC_PLAYER_THEME_ENABLED)
    } else {
      NavigationPreferencesBridge.musicPlayerThemeEnabled
    }
    val musicColorOption = if (
      !useInMemoryRepo &&
      prefs != null &&
      prefs.contains(PREF_KEY_MUSIC_PLAYER_COLOR_OPTION)
    ) {
      prefs.getString(PREF_KEY_MUSIC_PLAYER_COLOR_OPTION, DEFAULT_MUSIC_PLAYER_COLOR_OPTION)
        ?: DEFAULT_MUSIC_PLAYER_COLOR_OPTION
    } else {
      NavigationPreferencesBridge.musicPlayerColorOption
    }

    val usesMusicColors = musicThemeEnabled ||
      isMusicPlayerThemeColorSetting(themeColor) ||
      isMusicPlayerAccentColorSetting(accentColor)
    val session = MusicPlayerEngine.getActivePlaybackSession()?.takeIf { !it.isReleased }
    val internalActive = session?.let { it.isPlaying && it.currentTrack != null } == true
    if (context != null && usesMusicColors) {
      try {
        MusicPlayerEngine.ensureSystemMediaMonitoring(context)
      } catch (_: Throwable) {}
      if (
        !internalActive &&
        MusicPlayerEngine.externalSystemMediaInfo == null &&
        MusicPlayerEngine.isMusicPlayingOverride != true
      ) {
        try {
          MusicPlayerEngine.syncExternalSystemMediaSessions(context)
        } catch (_: Throwable) {}
      }
    }
    val externalMedia = MusicPlayerEngine.externalSystemMediaInfo
      ?.takeUnless { MusicPlayerEngine.isIgnoredPhoneOrCallSystemMediaInfo(it, context) }
    val isMusicPlaying = MusicPlayerEngine.isMusicPlayingOverride
      ?: (internalActive || (externalMedia?.isPlaying == true))
    val musicPalette = when {
      !internalActive && externalMedia != null && MusicPlayerEngine.isMusicPlayingOverride != true ->
        externalMedia.palette
      session != null && session.currentTrack != null ->
        session.activePalette
      else ->
        MusicPlayerEngine.activeTrackPalette
    }
    val activeStarsSign = if (isStarsSignThemeColorSetting(themeColor)) {
      StarsEngine.resolveCurrentSign(context = context)
    } else {
      StarsEngine.activeSignState
    }

    val colorScheme: ColorScheme = resolveToolboxColorScheme(
      context = context,
      darkTheme = darkTheme,
      themeColor = themeColor,
      accentColor = accentColor,
      musicPlayerThemeEnabled = musicThemeEnabled,
      musicPlayerColorOption = musicColorOption,
      isMusicPlaying = isMusicPlaying,
      musicPalette = musicPalette,
      activeStarsSign = activeStarsSign,
    )

    val surfaceColor = if (darkTheme) {
      lerp(colorScheme.surface, colorScheme.primary, 0.08f)
    } else {
      colorScheme.surface
    }
    val itemCardColor = if (darkTheme) {
      lerp(surfaceColor, Color.White, 0.07f)
    } else {
      lerp(colorScheme.surfaceVariant, Color.White, 0.45f)
    }
    val toastSurfaceColor = if (darkTheme) {
      lerp(surfaceColor, colorScheme.primaryContainer, 0.38f)
    } else {
      lerp(colorScheme.surface, colorScheme.primaryContainer, 0.45f)
    }
    val hexagonIconButtons = MusicPlayerWidgets.resolveHexagonIconButtonsEnabled(context)
    val backgroundTexture = MusicPlayerWidgets.resolveBackgroundTexture(context)

    return TodoWidgetPalette(
      surfaceArgb = MusicPlayerWidgets.colorToArgb(surfaceColor),
      itemCardArgb = MusicPlayerWidgets.colorToArgb(itemCardColor),
      borderArgb = MusicPlayerWidgets.colorToArgb(colorScheme.outlineVariant, alphaOverride = 0.5f),
      primaryArgb = MusicPlayerWidgets.colorToArgb(colorScheme.primary),
      onPrimaryArgb = MusicPlayerWidgets.colorToArgb(colorScheme.onPrimary),
      primaryContainerArgb = MusicPlayerWidgets.colorToArgb(colorScheme.primaryContainer),
      onPrimaryContainerArgb = MusicPlayerWidgets.colorToArgb(colorScheme.onPrimaryContainer),
      secondaryContainerArgb = MusicPlayerWidgets.colorToArgb(colorScheme.secondaryContainer),
      onSecondaryContainerArgb = MusicPlayerWidgets.colorToArgb(colorScheme.onSecondaryContainer),
      tertiaryContainerArgb = MusicPlayerWidgets.colorToArgb(colorScheme.tertiaryContainer),
      onTertiaryContainerArgb = MusicPlayerWidgets.colorToArgb(colorScheme.onTertiaryContainer),
      onSurfaceArgb = MusicPlayerWidgets.colorToArgb(colorScheme.onSurface),
      onSurfaceVariantArgb = MusicPlayerWidgets.colorToArgb(colorScheme.onSurfaceVariant),
      outlineArgb = MusicPlayerWidgets.colorToArgb(colorScheme.outline),
      toastSurfaceArgb = MusicPlayerWidgets.colorToArgb(toastSurfaceColor),
      toastBorderArgb = MusicPlayerWidgets.colorToArgb(colorScheme.primary, alphaOverride = 0.55f),
      hexagonIconButtons = hexagonIconButtons,
      iconButtonBackgroundRes = MusicPlayerWidgets.resolveIconButtonBackgroundRes(hexagonIconButtons),
      taskCheckUncheckedIconRes = if (hexagonIconButtons) {
        R.drawable.ic_todo_widget_hexagon_unchecked
      } else {
        R.drawable.ic_todo_widget_circle_unchecked
      },
      taskCheckCheckedIconRes = if (hexagonIconButtons) {
        R.drawable.ic_todo_widget_hexagon_checked
      } else {
        R.drawable.ic_todo_widget_circle_checked
      },
      backgroundTexture = backgroundTexture,
      secondaryArgb = MusicPlayerWidgets.colorToArgb(colorScheme.secondary),
      tertiaryArgb = MusicPlayerWidgets.colorToArgb(colorScheme.tertiary),
      isDarkTheme = darkTheme,
    )
  }

  /**
   * Saves the latest [TodoWidgetUndoRecord] so the user can undo a task completion from the widget
   * toast banner.
   */
  fun saveUndoRecord(
    context: Context?,
    record: TodoWidgetUndoRecord?,
  ) {
    inMemoryUndoRecord = record
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(
        SharedPreferencesTodoListRepository.PREFS_NAME,
        Context.MODE_PRIVATE,
      )
      if (record == null) {
        prefs.edit().remove(KEY_WIDGET_LAST_UNDO_JSON).apply()
      } else {
        prefs.edit().putString(KEY_WIDGET_LAST_UNDO_JSON, record.toJson().toString()).apply()
      }
    } catch (_: Throwable) {}
  }

  /**
   * Loads the active [TodoWidgetUndoRecord] if it has not expired.
   */
  fun loadActiveUndoRecord(
    context: Context?,
    nowMillis: Long = System.currentTimeMillis(),
  ): TodoWidgetUndoRecord? {
    val persisted = if (context != null) {
      try {
        val prefs = context.getSharedPreferences(
          SharedPreferencesTodoListRepository.PREFS_NAME,
          Context.MODE_PRIVATE,
        )
        val raw = prefs.getString(KEY_WIDGET_LAST_UNDO_JSON, null)
        if (!raw.isNullOrBlank()) {
          TodoWidgetUndoRecord.fromJson(JSONObject(raw))
        } else {
          null
        }
      } catch (_: Throwable) {
        null
      }
    } else {
      inMemoryUndoRecord
    }
    val candidate = persisted ?: inMemoryUndoRecord ?: return null
    return if (candidate.isActive(nowMillis)) {
      candidate
    } else {
      if (context != null) {
        saveUndoRecord(context, null)
      } else {
        inMemoryUndoRecord = null
      }
      null
    }
  }

  /**
   * Clears any in-memory and persisted widget undo state.
   */
  fun clearUndoRecord(context: Context? = null) {
    saveUndoRecord(context, null)
  }

  /**
   * Resolves the current [TodoWidgetState] from [repository] (or [SharedPreferencesTodoListRepository]).
   */
  fun resolveWidgetState(
    context: Context? = null,
    repository: TodoListRepository? = context?.let { SharedPreferencesTodoListRepository(it) },
    nowMillis: Long = System.currentTimeMillis(),
  ): TodoWidgetState {
    val allTasks = repository?.loadTasks().orEmpty()
    val visibleTasks = TodoListEngine.getMainViewTasks(allTasks, nowMillis)
    val todayKey = getTodayDateKey(nowMillis)
    val completedTodayCount = repository?.getCompletedTodayCount(todayKey) ?: 0
    val taskWord = if (visibleTasks.size == 1) "task" else "tasks"
    val headerSubtitle = "${visibleTasks.size} $taskWord • $completedTodayCount completed today"

    val activeUndo = loadActiveUndoRecord(context, nowMillis)
    val isUndoVisible = activeUndo != null
    val undoMessage = if (activeUndo != null) {
      "Completed \"${activeUndo.completedTaskTitle}\""
    } else {
      ""
    }

    return TodoWidgetState(
      visibleTasks = visibleTasks,
      totalVisibleCount = visibleTasks.size,
      completedTodayCount = completedTodayCount,
      headerSubtitle = headerSubtitle,
      pendingUndoRecord = activeUndo,
      isUndoToastVisible = isUndoVisible,
      undoToastMessage = undoMessage,
      palette = resolveWidgetPalette(context),
    )
  }

  /**
   * Builds the secondary subtitle line for a single [TodoItem] row in the widget (schedule info,
   * notes preview, or subtask preview).
   */
  fun buildTaskItemSubtitle(
    task: TodoItem,
    nowMillis: Long = System.currentTimeMillis(),
  ): String {
    val parts = mutableListOf<String>()
    if (task.scheduledAtMillis != null) {
      val label = TodoListEngine.formatScheduleLabel(
        scheduledAtMillis = task.scheduledAtMillis,
        scheduledEndMillis = task.scheduledEndMillis,
        nowMillis = nowMillis,
      )
      val repeatSuffix = task.recurrenceRule?.let { " (${it.formatShortLabel()})" }.orEmpty()
      val prefix = if (task.isDue(nowMillis)) "Due • " else ""
      parts.add("$prefix$label$repeatSuffix")
    }
    if (task.notes.isNotBlank()) {
      parts.add(task.notes.trim().replace("\n", " "))
    } else if (task.subtasks.isNotEmpty()) {
      val preview = task.subtasks.joinToString(" • ") { sub ->
        val mark = if (sub.isCompleted) "✓" else "○"
        "$mark ${sub.title}"
      }
      parts.add(preview)
    }
    return parts.joinToString(" • ")
  }

  /**
   * Formats the subtask progress badge (e.g. `"1/3"`) for a [TodoItem], or `null` when the task
   * has no subtasks.
   */
  fun formatSubtaskBadgeText(task: TodoItem): String? {
    if (task.subtasks.isEmpty()) return null
    val completed = task.subtasks.count { it.isCompleted }
    return "$completed/${task.subtasks.size}"
  }

  /**
   * Completes [taskId] from the widget, records the pre-completion snapshot so the user can undo it,
   * schedules an auto-dismiss refresh for the widget undo toast banner, and shows a system Toast.
   */
  fun completeTaskFromWidget(
    context: Context?,
    taskId: String,
    repository: TodoListRepository? = context?.let { SharedPreferencesTodoListRepository(it) },
    nowMillis: Long = System.currentTimeMillis(),
    showSystemToast: Boolean = true,
  ): TodoHistorySnapshot? {
    val repo = repository ?: return null
    val todayKey = getTodayDateKey(nowMillis)
    val currentTasks = repo.loadTasks()
    val targetTask = currentTasks.firstOrNull { it.id == taskId } ?: return null
    val currentCompletedToday = repo.getCompletedTodayCount(todayKey)
    val currentCompletedTasks = repo.loadCompletedTasks(todayKey)

    val beforeSnapshot = TodoHistorySnapshot(
      tasks = currentTasks,
      completedToday = currentCompletedToday,
      completedTasks = currentCompletedTasks,
    )
    val nextSnapshot = TodoListEngine.completeTask(beforeSnapshot, taskId, nowMillis)
    if (nextSnapshot == beforeSnapshot) return null

    repo.saveTasks(nextSnapshot.tasks)
    repo.setCompletedTodayCount(nextSnapshot.completedToday, todayKey)
    repo.saveCompletedTasks(nextSnapshot.completedTasks, todayKey)

    val undoRecord = TodoWidgetUndoRecord(
      completedTaskId = targetTask.id,
      completedTaskTitle = targetTask.title,
      completedAtMillis = nowMillis,
      preCompletionSnapshot = beforeSnapshot,
    )
    saveUndoRecord(context, undoRecord)

    if (context != null) {
      TodoListEngine.getTaskIdsToCancelAlarms(currentTasks, nextSnapshot.tasks).forEach { id ->
        TodoNotificationHelper.cancelTaskAlarm(context, id)
      }
      TodoNotificationHelper.syncTaskAlarms(context, nextSnapshot.tasks)
      scheduleUndoToastExpiryRefresh(context, nowMillis + UNDO_TOAST_DURATION_MS)
      updateAllWidgets(context)
      if (showSystemToast) {
        try {
          Toast.makeText(
            context,
            "Completed \"${targetTask.title}\" • Tap Undo on widget to restore",
            Toast.LENGTH_SHORT,
          ).show()
        } catch (_: Throwable) {}
      }
    }

    return nextSnapshot
  }

  /**
   * Undoes the most recent widget task completion using the saved [TodoWidgetUndoRecord].
   */
  fun undoLastCompletedTaskFromWidget(
    context: Context?,
    repository: TodoListRepository? = context?.let { SharedPreferencesTodoListRepository(it) },
    nowMillis: Long = System.currentTimeMillis(),
    showSystemToast: Boolean = true,
  ): TodoHistorySnapshot? {
    val repo = repository ?: return null
    val undoRecord = loadActiveUndoRecord(context, nowMillis) ?: return null
    val restored = undoRecord.preCompletionSnapshot
    val todayKey = getTodayDateKey(nowMillis)
    val previousTasks = repo.loadTasks()

    repo.saveTasks(restored.tasks)
    repo.setCompletedTodayCount(restored.completedToday, todayKey)
    repo.saveCompletedTasks(restored.completedTasks, todayKey)
    saveUndoRecord(context, null)

    if (context != null) {
      TodoListEngine.getTaskIdsToCancelAlarms(previousTasks, restored.tasks).forEach { id ->
        TodoNotificationHelper.cancelTaskAlarm(context, id)
      }
      TodoNotificationHelper.syncTaskAlarms(context, restored.tasks)
      updateAllWidgets(context)
      if (showSystemToast) {
        try {
          Toast.makeText(
            context,
            "Undid completion of \"${undoRecord.completedTaskTitle}\"",
            Toast.LENGTH_SHORT,
          ).show()
        } catch (_: Throwable) {}
      }
    }

    return restored
  }

  private fun scheduleUndoToastExpiryRefresh(
    context: Context,
    triggerAtMillis: Long,
  ) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, TodoListWidgetProvider::class.java).apply {
        action = ACTION_WIDGET_DISMISS_UNDO
        setPackage(context.packageName)
      }
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE_DISMISS_UNDO,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
      alarmManager.set(AlarmManager.RTC, triggerAtMillis, pendingIntent)
    } catch (_: Throwable) {}
  }

  /**
   * Renders the rounded background [Bitmap] for the To-Do List widget card (`16dp` rounded corners,
   * matching `MusicPlayerWidgets.ALBUM_WIDGET_CORNER_RADIUS_DP` and `MINI_BAR_CORNER_RADIUS_DP`,
   * with the border inset by `borderWidthPx` so it never appears clipped at the corners).
   */
  fun renderWidgetBackgroundBitmap(
    palette: TodoWidgetPalette,
    widthPx: Int = 520,
    heightPx: Int = 520,
    cornerRadiusPx: Float = 32f,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(120, 1400)
    val safeH = heightPx.coerceIn(100, 1600)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()
      val fillRect = RectF(0f, 0f, w, h)
      val fillRadius = min(cornerRadiusPx, min(fillRect.width(), fillRect.height()) * 0.5f)

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = palette.surfaceArgb
      }
      canvas.drawRoundRect(fillRect, fillRadius, fillRadius, fillPaint)

      drawWidgetBackgroundTextureOverlay(
        bitmap = bitmap,
        cornerRadiusPx = fillRadius,
        texture = palette.backgroundTexture,
        primaryColor = Color(palette.primaryArgb),
        tertiaryColor = Color(palette.tertiaryArgb),
        secondaryColor = Color(palette.secondaryArgb),
        onSurfaceColor = Color(palette.onSurfaceArgb),
        isDarkTheme = palette.isDarkTheme,
      )

      if (borderWidthPx > 0f) {
        val strokeInset = max(borderWidthPx * 1.25f, 1.5f)
        val borderRect = RectF(strokeInset, strokeInset, w - strokeInset, h - strokeInset)
        // Use a slightly larger corner radius (+borderWidthPx) so that even if fitXY stretches the
        // bitmap non-uniformly when the launcher's actual padded cell dimensions differ slightly
        // from AppWidgetManager.getAppWidgetOptions(), the border's corner arc never crosses outside
        // the root @android:id/background view's 16dp clipToOutline curve.
        val borderRadius = max(
          0f,
          min(cornerRadiusPx + borderWidthPx, min(borderRect.width(), borderRect.height()) * 0.5f),
        )
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = borderWidthPx
          color = palette.borderArgb
        }
        canvas.drawRoundRect(borderRect, borderRadius, borderRadius, borderPaint)
      }
      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders the rounded pill background [Bitmap] for the widget's Undo completion toast banner.
   */
  fun renderUndoToastBackgroundBitmap(
    palette: TodoWidgetPalette,
    widthPx: Int = 360,
    heightPx: Int = 80,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(160, 1000)
    val safeH = heightPx.coerceIn(48, 200)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()
      val fillRect = RectF(0f, 0f, w, h)
      val fillRadius = fillRect.height() * 0.5f

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = palette.toastSurfaceArgb
      }
      canvas.drawRoundRect(fillRect, fillRadius, fillRadius, fillPaint)

      if (borderWidthPx > 0f) {
        val strokeInset = max(borderWidthPx, 1f)
        val borderRect = RectF(strokeInset, strokeInset, w - strokeInset, h - strokeInset)
        val borderRadius = borderRect.height() * 0.5f
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = borderWidthPx
          color = palette.toastBorderArgb
        }
        canvas.drawRoundRect(borderRect, borderRadius, borderRadius, borderPaint)
      }
      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  fun buildOpenTodoListPendingIntent(
    context: Context,
    requestCode: Int = REQUEST_CODE_OPEN_TODO,
  ): PendingIntent {
    val intent = createStandaloneToolIntent(context, Tool.TODO_LIST)
    return PendingIntent.getActivity(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildOpenTodoQuickActionPendingIntent(
    context: Context,
    openSchedule: Boolean,
  ): PendingIntent {
    val intent = createTodoQuickActionIntent(context, openSchedule = openSchedule)
    val requestCode = if (openSchedule) REQUEST_CODE_FAB_SCHEDULE else REQUEST_CODE_FAB_ADD_TASK
    return PendingIntent.getActivity(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildWidgetBroadcastPendingIntent(
    context: Context,
    action: String,
    requestCode: Int,
    mutable: Boolean = false,
  ): PendingIntent {
    val intent = Intent(context, TodoListWidgetProvider::class.java).apply {
      this.action = action
      setPackage(context.packageName)
    }
    val mutabilityFlag = if (mutable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.FLAG_MUTABLE
    } else if (mutable) {
      0
    } else {
      PendingIntent.FLAG_IMMUTABLE
    }
    return PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or mutabilityFlag,
    )
  }

  /**
   * Builds the [RemoteViews] for a single task item inside the widget's scrollable [ListView].
   * Notice that there is no drag handle or reorder control, while the check circle is preserved
   * to complete the task.
   */
  fun buildTaskItemRemoteViews(
    context: Context,
    task: TodoItem,
    palette: TodoWidgetPalette = resolveWidgetPalette(context),
    nowMillis: Long = System.currentTimeMillis(),
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_todo_list_item)
    val cardBorderArgb = MusicPlayerWidgets.colorToArgb(
      lerp(
        Color(palette.itemCardArgb),
        Color(palette.borderArgb or 0xFF000000.toInt()),
        0.5f,
      )
    )
    views.setInt(R.id.widget_todo_item_bg, "setColorFilter", palette.itemCardArgb)
    views.setInt(R.id.widget_todo_item_border, "setColorFilter", cardBorderArgb)

    views.setTextViewText(R.id.widget_todo_item_title, task.title)
    views.setTextColor(R.id.widget_todo_item_title, palette.onSurfaceArgb)

    val subtitle = buildTaskItemSubtitle(task, nowMillis)
    if (subtitle.isNotBlank()) {
      views.setViewVisibility(R.id.widget_todo_item_subtitle, View.VISIBLE)
      views.setTextViewText(R.id.widget_todo_item_subtitle, subtitle)
      views.setTextColor(
        R.id.widget_todo_item_subtitle,
        if (task.isDue(nowMillis)) palette.primaryArgb else palette.onSurfaceVariantArgb,
      )
    } else {
      views.setViewVisibility(R.id.widget_todo_item_subtitle, View.GONE)
    }

    val badgeText = formatSubtaskBadgeText(task)
    if (badgeText != null) {
      views.setViewVisibility(R.id.widget_todo_item_subtask_badge_container, View.VISIBLE)
      views.setViewVisibility(R.id.widget_todo_item_subtask_badge, View.VISIBLE)
      views.setInt(
        R.id.widget_todo_item_subtask_badge_bg,
        "setColorFilter",
        palette.primaryContainerArgb,
      )
      views.setTextViewText(R.id.widget_todo_item_subtask_badge, badgeText)
      views.setTextColor(R.id.widget_todo_item_subtask_badge, palette.onPrimaryContainerArgb)
    } else {
      views.setViewVisibility(R.id.widget_todo_item_subtask_badge_container, View.GONE)
      views.setViewVisibility(R.id.widget_todo_item_subtask_badge, View.GONE)
    }

    views.setImageViewResource(
      R.id.widget_todo_item_check_circle,
      palette.taskCheckUncheckedIconRes,
    )
    views.setInt(R.id.widget_todo_item_check_circle, "setColorFilter", palette.primaryArgb)
    views.setContentDescription(
      R.id.widget_todo_item_check_container,
      "Complete ${task.title}",
    )

    // Fill-in intent for completing the task when tapping the check circle
    val completeFillIn = Intent().apply {
      putExtra(EXTRA_TASK_ID, task.id)
      putExtra(EXTRA_ITEM_CLICK_ACTION, ITEM_CLICK_ACTION_COMPLETE)
    }
    views.setOnClickFillInIntent(R.id.widget_todo_item_check_container, completeFillIn)
    views.setOnClickFillInIntent(R.id.widget_todo_item_check_circle, completeFillIn)

    // Fill-in intent for opening the To-Do tool when tapping the task text
    val openFillIn = Intent().apply {
      putExtra(EXTRA_TASK_ID, task.id)
      putExtra(EXTRA_ITEM_CLICK_ACTION, ITEM_CLICK_ACTION_OPEN)
    }
    views.setOnClickFillInIntent(R.id.widget_todo_item_text_container, openFillIn)
    views.setOnClickFillInIntent(R.id.widget_todo_item_root, openFillIn)
    views.setOnClickFillInIntent(R.id.widget_todo_item_bg, openFillIn)
    views.setOnClickFillInIntent(R.id.widget_todo_item_border, openFillIn)

    return views
  }

  /**
   * Builds the full [RemoteViews] for a [TodoListWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: TodoWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_todo_list)
    val palette = state.palette
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val (widthDp, heightDp) = MusicPlayerWidgets.resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 260f,
      defaultHeightDp = 260f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(120f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(100f)
    val maxDimPx = 1100f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(120)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(100)

    renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_todo_background, bgBmp)
    }

    // Header styling
    views.setImageViewResource(R.id.widget_todo_header_badge_bg, palette.iconButtonBackgroundRes)
    views.setInt(R.id.widget_todo_header_badge_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_todo_header_badge_icon, "setColorFilter", palette.primaryArgb)
    views.setTextViewText(R.id.widget_todo_title, "To Do")
    views.setTextColor(R.id.widget_todo_title, palette.onSurfaceArgb)
    views.setTextViewText(R.id.widget_todo_subtext, state.headerSubtitle)
    views.setTextColor(R.id.widget_todo_subtext, palette.onSurfaceVariantArgb)

    val openTodoIntent = buildOpenTodoListPendingIntent(context)
    views.setOnClickPendingIntent(R.id.widget_todo_header, openTodoIntent)
    views.setOnClickPendingIntent(R.id.widget_todo_empty_view, openTodoIntent)

    // Empty state styling
    views.setImageViewResource(R.id.widget_todo_empty_badge_bg, palette.iconButtonBackgroundRes)
    views.setInt(R.id.widget_todo_empty_badge_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setImageViewResource(R.id.widget_todo_empty_badge_icon, palette.taskCheckCheckedIconRes)
    views.setInt(R.id.widget_todo_empty_badge_icon, "setColorFilter", palette.primaryArgb)
    views.setTextColor(R.id.widget_todo_empty_title, palette.onSurfaceArgb)
    views.setTextColor(R.id.widget_todo_empty_subtitle, palette.onSurfaceVariantArgb)

    // Scrollable ListView adapter & empty view
    val serviceIntent = Intent(context, TodoWidgetRemoteViewsService::class.java).apply {
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }
    @Suppress("DEPRECATION")
    views.setRemoteAdapter(R.id.widget_todo_list_view, serviceIntent)
    views.setEmptyView(R.id.widget_todo_list_view, R.id.widget_todo_empty_view)

    val itemTemplatePendingIntent = buildWidgetBroadcastPendingIntent(
      context = context,
      action = ACTION_WIDGET_ITEM_CLICK,
      requestCode = REQUEST_CODE_ITEM_TEMPLATE,
      mutable = true,
    )
    views.setPendingIntentTemplate(R.id.widget_todo_list_view, itemTemplatePendingIntent)

    // Undo Toast Banner upon task completion
    if (state.isUndoToastVisible) {
      views.setViewVisibility(R.id.widget_todo_undo_toast_container, View.VISIBLE)
      views.setViewVisibility(R.id.widget_todo_bottom_spacer, View.GONE)
      renderUndoToastBackgroundBitmap(
        palette = palette,
        widthPx = (180f * density).roundToInt().coerceAtLeast(200),
        heightPx = (40f * density).roundToInt().coerceAtLeast(56),
        borderWidthPx = max(1f, 1f * density),
      )?.let { toastBmp ->
        views.setImageViewBitmap(R.id.widget_todo_undo_toast_bg, toastBmp)
      }
      views.setTextViewText(R.id.widget_todo_undo_toast_text, state.undoToastMessage)
      views.setTextColor(R.id.widget_todo_undo_toast_text, palette.onSurfaceArgb)
      views.setInt(R.id.widget_todo_undo_button_bg, "setColorFilter", palette.primaryContainerArgb)
      views.setInt(R.id.widget_todo_undo_icon, "setColorFilter", palette.onPrimaryContainerArgb)
      views.setTextColor(R.id.widget_todo_undo_button_text, palette.onPrimaryContainerArgb)

      val undoPendingIntent = buildWidgetBroadcastPendingIntent(
        context = context,
        action = ACTION_WIDGET_UNDO_COMPLETE,
        requestCode = REQUEST_CODE_UNDO_COMPLETE,
      )
      views.setOnClickPendingIntent(R.id.widget_todo_undo_button, undoPendingIntent)
      views.setOnClickPendingIntent(R.id.widget_todo_undo_toast_container, undoPendingIntent)
    } else {
      views.setViewVisibility(R.id.widget_todo_undo_toast_container, View.GONE)
      views.setViewVisibility(R.id.widget_todo_bottom_spacer, View.VISIBLE)
    }

    // Two Floating FABs at Bottom Right:
    // 1. Schedule FAB -> opens To Do tool with Schedule view opened
    views.setImageViewResource(
      R.id.widget_todo_fab_schedule_bg,
      palette.iconButtonBackgroundRes,
    )
    views.setInt(
      R.id.widget_todo_fab_schedule_bg,
      "setColorFilter",
      palette.secondaryContainerArgb,
    )
    views.setInt(
      R.id.widget_todo_fab_schedule,
      "setColorFilter",
      palette.onSecondaryContainerArgb,
    )
    views.setOnClickPendingIntent(
      R.id.widget_todo_fab_schedule,
      buildOpenTodoQuickActionPendingIntent(context, openSchedule = true),
    )

    // 2. Plus (+) FAB -> opens To Do tool with Add a task focused
    views.setImageViewResource(
      R.id.widget_todo_fab_plus_bg,
      palette.iconButtonBackgroundRes,
    )
    views.setInt(
      R.id.widget_todo_fab_plus_bg,
      "setColorFilter",
      palette.primaryContainerArgb,
    )
    views.setInt(
      R.id.widget_todo_fab_plus,
      "setColorFilter",
      palette.onPrimaryContainerArgb,
    )
    views.setOnClickPendingIntent(
      R.id.widget_todo_fab_plus,
      buildOpenTodoQuickActionPendingIntent(context, openSchedule = false),
    )

    return views
  }

  /**
   * Handles a widget broadcast action from [TodoListWidgetProvider].
   */
  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
    repository: TodoListRepository? = context?.let { SharedPreferencesTodoListRepository(it) },
  ): Boolean {
    val action = intent?.action ?: return false
    when (action) {
      ACTION_WIDGET_REFRESH -> {
        if (context != null) updateAllWidgets(context)
        return true
      }
      ACTION_WIDGET_DISMISS_UNDO -> {
        saveUndoRecord(context, null)
        if (context != null) updateAllWidgets(context)
        return true
      }
      ACTION_WIDGET_UNDO_COMPLETE -> {
        return undoLastCompletedTaskFromWidget(context = context, repository = repository) != null
      }
      ACTION_WIDGET_COMPLETE_TASK -> {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return false
        return completeTaskFromWidget(
          context = context,
          taskId = taskId,
          repository = repository,
        ) != null
      }
      ACTION_WIDGET_ITEM_CLICK -> {
        val clickAction = intent.getStringExtra(EXTRA_ITEM_CLICK_ACTION) ?: ITEM_CLICK_ACTION_OPEN
        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
        return if (clickAction == ITEM_CLICK_ACTION_COMPLETE && !taskId.isNullOrBlank()) {
          completeTaskFromWidget(
            context = context,
            taskId = taskId,
            repository = repository,
          ) != null
        } else {
          if (context != null) {
            try {
              context.startActivity(createStandaloneToolIntent(context, Tool.TODO_LIST))
            } catch (_: Throwable) {}
          }
          true
        }
      }
      ACTION_WIDGET_OPEN_TODO_ADD_TASK -> {
        TodoListEngine.requestQuickAction(openSchedule = false)
        if (context != null) {
          try {
            context.startActivity(createTodoQuickActionIntent(context, openSchedule = false))
          } catch (_: Throwable) {}
        }
        return true
      }
      ACTION_WIDGET_OPEN_TODO_SCHEDULE -> {
        TodoListEngine.requestQuickAction(openSchedule = true)
        if (context != null) {
          try {
            context.startActivity(createTodoQuickActionIntent(context, openSchedule = true))
          } catch (_: Throwable) {}
        }
        return true
      }
      else -> return false
    }
  }

  /**
   * Updates all pinned instances of [TodoListWidgetProvider] on the user's home screen and
   * notifies their scrollable [ListView] adapters that task data changed.
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, TodoListWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateTodoWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  /**
   * Updates the specified [appWidgetIds] for [TodoListWidgetProvider].
   */
  fun updateTodoWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: TodoWidgetState = resolveWidgetState(context),
  ) {
    for (appWidgetId in appWidgetIds) {
      try {
        val options = try {
          appWidgetManager.getAppWidgetOptions(appWidgetId)
        } catch (_: Throwable) {
          null
        }
        val views = buildWidgetRemoteViews(
          context = context,
          appWidgetId = appWidgetId,
          state = state,
          options = options,
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
      } catch (_: Throwable) {}
    }
    try {
      @Suppress("DEPRECATION")
      appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_todo_list_view)
    } catch (_: Throwable) {}
  }
}

/**
 * To-Do List Home Screen AppWidget:
 * - Displays the user's scrollable To-Do task list
 * - Resizable horizontally and vertically within reasonable bounds
 * - No drag-handle or reorder controls; keeps check circles to complete tasks directly
 * - Shows a toast with Undo support upon task completion
 * - Features two floating FABs at the bottom-right (`+` and Schedule) that launch the To-Do tool
 *   with the "Add a task..." input focused or the schedule view opened
 */
class TodoListWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    TodoListWidgets.updateTodoWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    TodoListWidgets.updateTodoWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.todo.ACTION_")) {
      TodoListWidgets.handleWidgetAction(context, intent)
    }
  }
}

/**
 * [RemoteViewsService] that powers the scrollable [ListView] inside [TodoListWidgetProvider].
 */
class TodoWidgetRemoteViewsService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return TodoWidgetRemoteViewsFactory(applicationContext)
  }
}

internal class TodoWidgetRemoteViewsFactory(
  private val context: Context,
) : RemoteViewsService.RemoteViewsFactory {

  private var tasks: List<TodoItem> = emptyList()
  private var palette: TodoWidgetPalette = TodoListWidgets.resolveWidgetPalette(context)

  override fun onCreate() {
    reloadTasks()
  }

  override fun onDataSetChanged() {
    reloadTasks()
  }

  private fun reloadTasks() {
    try {
      val repo = SharedPreferencesTodoListRepository(context)
      val now = System.currentTimeMillis()
      tasks = TodoListEngine.getMainViewTasks(repo.loadTasks(), now)
      palette = TodoListWidgets.resolveWidgetPalette(context)
    } catch (_: Throwable) {
      tasks = emptyList()
    }
  }

  override fun onDestroy() {
    tasks = emptyList()
  }

  override fun getCount(): Int = tasks.size

  override fun getViewAt(position: Int): RemoteViews? {
    val item = tasks.getOrNull(position) ?: return null
    return TodoListWidgets.buildTaskItemRemoteViews(
      context = context,
      task = item,
      palette = palette,
    )
  }

  override fun getLoadingView(): RemoteViews? = null

  override fun getViewTypeCount(): Int = 1

  override fun getItemId(position: Int): Long {
    val itemHash = tasks.getOrNull(position)?.id?.hashCode()?.toLong() ?: position.toLong()
    return (itemHash shl 32) xor (palette.hashCode().toLong() and 0xFFFFFFFFL)
  }

  override fun hasStableIds(): Boolean = true
}
