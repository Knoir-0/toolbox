package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

const val TODO_LIST_TTXT_FILE_NAME = "todo_list.text"
const val TODO_COMPLETED_TTXT_FILE_NAME = "todo_completed.text"
const val TODO_SAVED_TTXT_FILE_NAME = "todo_saved.text"

/**
 * Generates a date key formatted as YYYY-MM-DD for midnight reset comparison.
 */
fun getTodayDateKey(timeMillis: Long = System.currentTimeMillis()): String {
  val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  return sdf.format(Date(timeMillis))
}

/**
 * Represents a single subtask (with optional nested child [subtasks]) within a [TodoItem].
 * In `.text` files, nested subtasks follow YAML 2-space indentation
 * (`"  ".repeat(indentLevel)`) with `- <title>` for uncompleted items and `+ <title>` for completed items.
 */
data class TodoSubTask(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val isCompleted: Boolean = false,
  val subtasks: List<TodoSubTask> = emptyList(),
) {
  /**
   * Internal `.text` list marker: `-` when uncompleted, `+` when completed.
   */
  val ttxtPrefix: Char
    get() = if (isCompleted) '+' else '-'

  /**
   * Formats this subtask and any nested [subtasks] into YAML-indented `.text` lines.
   * Each level of nesting uses 2 spaces (`"  ".repeat(indentLevel)`).
   */
  fun toTtxt(indentLevel: Int = 1): String {
    val indent = "  ".repeat(indentLevel.coerceAtLeast(0))
    val selfLine = "$indent$ttxtPrefix ${title.trim()}"
    val validChildren = subtasks.filter { it.title.isNotBlank() }
    if (validChildren.isEmpty()) return selfLine
    val childLines = validChildren.map { it.toTtxt(indentLevel + 1) }
    return (listOf(selfLine) + childLines).joinToString("\n")
  }

  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("isCompleted", isCompleted)
    if (subtasks.isNotEmpty()) {
      val childrenArray = JSONArray()
      subtasks.forEach { childrenArray.put(it.toJson()) }
      put("subtasks", childrenArray)
    }
    put("ttxt", toTtxt(indentLevel = 1))
  }

  companion object {
    fun fromJson(json: JSONObject): TodoSubTask {
      val childrenList = mutableListOf<TodoSubTask>()
      val childrenArray = json.optJSONArray("subtasks")
      if (childrenArray != null) {
        for (i in 0 until childrenArray.length()) {
          val childObj = childrenArray.optJSONObject(i)
          if (childObj != null) {
            childrenList.add(fromJson(childObj))
          }
        }
      }
      return TodoSubTask(
        id = json.optString("id", UUID.randomUUID().toString()),
        title = json.optString("title", ""),
        isCompleted = json.optBoolean("isCompleted", false),
        subtasks = childrenList,
      )
    }
  }
}

/**
 * Maximum future schedule horizon (24 hours in milliseconds) for a scheduled task to remain visible
 * in the main tasks view. Tasks scheduled further than 24 hours in the future are hidden from the
 * main tasks view and only visible in the scheduled view.
 */
const val MAIN_TASKS_SCHEDULE_HORIZON_MILLIS = 24L * 60L * 60L * 1000L

/**
 * Supported recurrence units for Google Calendar-style recurring tasks and relative schedule presets.
 */
enum class TodoRecurrenceUnit(
  val calendarField: Int,
  val singularLabel: String,
  val pluralLabel: String,
) {
  MINUTE(Calendar.MINUTE, "minute", "minutes"),
  HOUR(Calendar.HOUR_OF_DAY, "hour", "hours"),
  DAY(Calendar.DAY_OF_YEAR, "day", "days"),
  WEEK(Calendar.WEEK_OF_YEAR, "week", "weeks"),
  MONTH(Calendar.MONTH, "month", "months"),
  YEAR(Calendar.YEAR, "year", "years"),
  WEEKDAYS(Calendar.DAY_OF_YEAR, "weekday", "weekdays"),
  ;

  companion object {
    fun fromCalendarField(field: Int): TodoRecurrenceUnit = when (field) {
      Calendar.MINUTE -> MINUTE
      Calendar.HOUR_OF_DAY, Calendar.HOUR -> HOUR
      Calendar.DAY_OF_YEAR, Calendar.DAY_OF_MONTH, Calendar.DATE -> DAY
      Calendar.WEEK_OF_YEAR, Calendar.WEEK_OF_MONTH -> WEEK
      Calendar.MONTH -> MONTH
      Calendar.YEAR -> YEAR
      else -> DAY
    }

    fun fromNameOrNull(name: String?): TodoRecurrenceUnit? {
      if (name.isNullOrBlank()) return null
      return entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
  }
}

/**
 * Defines how a recurring task repeats upon task completion.
 */
data class TodoRecurrenceRule(
  val interval: Int = 1,
  val unit: TodoRecurrenceUnit = TodoRecurrenceUnit.DAY,
  val presetLabel: String? = null,
) {
  /**
   * Computes the next scheduled timestamp starting from [fromMillis] (typically completion time).
   */
  fun nextScheduledAtMillis(fromMillis: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = fromMillis }
    val safeInterval = interval.coerceAtLeast(1)
    when (unit) {
      TodoRecurrenceUnit.WEEKDAYS -> {
        var remaining = safeInterval
        while (remaining > 0) {
          cal.add(Calendar.DAY_OF_YEAR, 1)
          val dow = cal.get(Calendar.DAY_OF_WEEK)
          if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) {
            remaining--
          }
        }
      }
      else -> {
        cal.add(unit.calendarField, safeInterval)
      }
    }
    return cal.timeInMillis
  }

  fun formatLabel(): String {
    if (unit == TodoRecurrenceUnit.WEEKDAYS) {
      return "Every weekday (Mon–Fri)"
    }
    if (!presetLabel.isNullOrBlank()) {
      return "Every $presetLabel"
    }
    val safeInterval = interval.coerceAtLeast(1)
    return if (safeInterval == 1) {
      when (unit) {
        TodoRecurrenceUnit.MINUTE -> "Every minute"
        TodoRecurrenceUnit.HOUR -> "Hourly"
        TodoRecurrenceUnit.DAY -> "Daily"
        TodoRecurrenceUnit.WEEK -> "Weekly"
        TodoRecurrenceUnit.MONTH -> "Monthly"
        TodoRecurrenceUnit.YEAR -> "Yearly"
        TodoRecurrenceUnit.WEEKDAYS -> "Every weekday (Mon–Fri)"
      }
    } else {
      "Every $safeInterval ${unit.pluralLabel}"
    }
  }

  fun formatShortLabel(): String {
    if (unit == TodoRecurrenceUnit.WEEKDAYS) {
      return "Weekdays"
    }
    if (!presetLabel.isNullOrBlank()) {
      return "Every $presetLabel"
    }
    val safeInterval = interval.coerceAtLeast(1)
    return if (safeInterval == 1) {
      when (unit) {
        TodoRecurrenceUnit.MINUTE -> "Every 1 min"
        TodoRecurrenceUnit.HOUR -> "Hourly"
        TodoRecurrenceUnit.DAY -> "Daily"
        TodoRecurrenceUnit.WEEK -> "Weekly"
        TodoRecurrenceUnit.MONTH -> "Monthly"
        TodoRecurrenceUnit.YEAR -> "Yearly"
        TodoRecurrenceUnit.WEEKDAYS -> "Weekdays"
      }
    } else {
      "Every $safeInterval ${unit.pluralLabel}"
    }
  }

  fun toJson(): JSONObject = JSONObject().apply {
    put("interval", interval.coerceAtLeast(1))
    put("unit", unit.name)
    if (!presetLabel.isNullOrBlank()) {
      put("presetLabel", presetLabel)
    }
  }

  companion object {
    val DAILY = TodoRecurrenceRule(interval = 1, unit = TodoRecurrenceUnit.DAY, presetLabel = "1 day")
    val WEEKLY = TodoRecurrenceRule(interval = 1, unit = TodoRecurrenceUnit.WEEK, presetLabel = "1 wk")
    val MONTHLY = TodoRecurrenceRule(interval = 1, unit = TodoRecurrenceUnit.MONTH, presetLabel = "1 mo")
    val YEARLY = TodoRecurrenceRule(interval = 1, unit = TodoRecurrenceUnit.YEAR)
    val WEEKDAYS = TodoRecurrenceRule(interval = 1, unit = TodoRecurrenceUnit.WEEKDAYS)

    fun fromPreset(label: String, calendarField: Int, amount: Int): TodoRecurrenceRule {
      return TodoRecurrenceRule(
        interval = amount.coerceAtLeast(1),
        unit = TodoRecurrenceUnit.fromCalendarField(calendarField),
        presetLabel = label,
      )
    }

    fun fromJson(json: JSONObject?): TodoRecurrenceRule? {
      if (json == null) return null
      val unit = TodoRecurrenceUnit.fromNameOrNull(json.optString("unit", null)) ?: return null
      val interval = json.optInt("interval", 1).coerceAtLeast(1)
      val presetLabel = if (json.has("presetLabel") && !json.isNull("presetLabel")) {
        json.optString("presetLabel").takeIf { it.isNotBlank() }
      } else {
        null
      }
      return TodoRecurrenceRule(
        interval = interval,
        unit = unit,
        presetLabel = presetLabel,
      )
    }
  }
}

/**
 * Represents a single todo task, optionally containing subtasks, notes, and a scheduled date/time range.
 */
data class TodoItem(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val notes: String = "",
  val subtasks: List<TodoSubTask> = emptyList(),
  val isSubtasksCollapsed: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val scheduledAtMillis: Long? = null,
  val scheduledEndMillis: Long? = null,
  val notifiedAtScheduleMillis: Long? = null,
  val recurrenceRule: TodoRecurrenceRule? = null,
) {
  val isScheduled: Boolean
    get() = scheduledAtMillis != null

  val isRecurring: Boolean
    get() = recurrenceRule != null

  fun isScheduledBeyondHorizon(
    nowMillis: Long = System.currentTimeMillis(),
    horizonMillis: Long = MAIN_TASKS_SCHEDULE_HORIZON_MILLIS,
  ): Boolean = scheduledAtMillis != null && (scheduledAtMillis - nowMillis) > horizonMillis

  fun isVisibleInMainTasksView(
    nowMillis: Long = System.currentTimeMillis(),
    horizonMillis: Long = MAIN_TASKS_SCHEDULE_HORIZON_MILLIS,
  ): Boolean = !isScheduledBeyondHorizon(nowMillis, horizonMillis)

  fun isDue(nowMillis: Long = System.currentTimeMillis()): Boolean =
    scheduledAtMillis != null && nowMillis >= scheduledAtMillis

  fun shouldTriggerNotification(nowMillis: Long = System.currentTimeMillis()): Boolean =
    scheduledAtMillis != null &&
      nowMillis >= scheduledAtMillis &&
      notifiedAtScheduleMillis != scheduledAtMillis

  /**
   * Builds the notification body from the note's contents (notes text and/or subtasks).
   */
  fun buildNotificationContent(): String {
    val parts = mutableListOf<String>()
    val trimmedNotes = notes.trim()
    if (trimmedNotes.isNotEmpty()) {
      parts.add(trimmedNotes)
    }
    if (subtasks.isNotEmpty()) {
      val subtasksText = subtasks.joinToString("\n") { sub ->
        val prefix = if (sub.isCompleted) "✓" else "•"
        "$prefix ${sub.title}"
      }
      parts.add(subtasksText)
    }
    if (parts.isEmpty()) {
      val scheduleText = scheduledAtMillis?.let {
        TodoListEngine.formatScheduleLabel(it, scheduledEndMillis)
      }
      return if (scheduleText != null) {
        "$title ($scheduleText)"
      } else {
        title
      }
    }
    return parts.joinToString("\n")
  }

  /**
   * Formats this task and its nested [subtasks] into YAML-indented `.text` lines:
   * - `- <title>` at indent 0 when [isCompleted] is `false`, or `+ <title>` when [isCompleted] is `true`.
   * - Nested subtasks indented by 2 spaces per level (`"  ".repeat(level)`), using `-` for uncompleted
   *   and `+` for completed subtasks.
   */
  fun toTtxt(isCompleted: Boolean = false): String =
    TodoListEngine.formatTaskToTtxt(this, isCompleted = isCompleted)

  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("notes", notes)
    val subtasksArray = JSONArray()
    subtasks.forEach { subtasksArray.put(it.toJson()) }
    put("subtasks", subtasksArray)
    put("ttxt", toTtxt(isCompleted = false))
    put("format", NOTES_FILE_EXTENSION)
    put("isSubtasksCollapsed", isSubtasksCollapsed)
    put("createdAt", createdAt)
    if (scheduledAtMillis != null) {
      put("scheduledAtMillis", scheduledAtMillis)
    }
    if (scheduledEndMillis != null) {
      put("scheduledEndMillis", scheduledEndMillis)
    }
    if (notifiedAtScheduleMillis != null) {
      put("notifiedAtScheduleMillis", notifiedAtScheduleMillis)
    }
    if (recurrenceRule != null) {
      put("recurrenceRule", recurrenceRule.toJson())
    }
  }

  companion object {
    fun fromTtxt(
      rawTtxt: String,
      id: String = UUID.randomUUID().toString(),
      nowMs: Long = System.currentTimeMillis(),
    ): TodoItem = TodoListEngine.parseTaskFromTtxt(
      rawTtxt = rawTtxt,
      id = id,
      nowMs = nowMs,
    )

    fun fromJson(json: JSONObject): TodoItem {
      val subtasksList = mutableListOf<TodoSubTask>()
      val hasSubtasksKey = json.has("subtasks")
      if (hasSubtasksKey) {
        val array = json.optJSONArray("subtasks")
        if (array != null) {
          for (i in 0 until array.length()) {
            val subObj = array.optJSONObject(i)
            if (subObj != null) {
              subtasksList.add(TodoSubTask.fromJson(subObj))
            }
          }
        }
      }
      val scheduledAt = if (json.has("scheduledAtMillis") && !json.isNull("scheduledAtMillis")) {
        json.optLong("scheduledAtMillis")
      } else {
        null
      }
      val scheduledEnd = if (json.has("scheduledEndMillis") && !json.isNull("scheduledEndMillis")) {
        json.optLong("scheduledEndMillis")
      } else {
        null
      }
      val notifiedAt = if (json.has("notifiedAtScheduleMillis") && !json.isNull("notifiedAtScheduleMillis")) {
        json.optLong("notifiedAtScheduleMillis")
      } else {
        null
      }
      val recurrence = if (json.has("recurrenceRule") && !json.isNull("recurrenceRule")) {
        TodoRecurrenceRule.fromJson(json.optJSONObject("recurrenceRule"))
      } else {
        null
      }
      val id = json.optString("id", UUID.randomUUID().toString())
      val createdAt = json.optLong("createdAt", System.currentTimeMillis())
      val rawTtxt = json.optString("ttxt", "")
      if (!json.has("title") && !hasSubtasksKey && rawTtxt.isNotBlank()) {
        val parsed = fromTtxt(rawTtxt = rawTtxt, id = id, nowMs = createdAt)
        return parsed.copy(
          notes = json.optString("notes", parsed.notes),
          isSubtasksCollapsed = json.optBoolean("isSubtasksCollapsed", false),
          createdAt = createdAt,
          scheduledAtMillis = scheduledAt,
          scheduledEndMillis = scheduledEnd,
          notifiedAtScheduleMillis = notifiedAt,
          recurrenceRule = recurrence,
        )
      }
      return TodoItem(
        id = id,
        title = json.optString("title", ""),
        notes = json.optString("notes", ""),
        subtasks = subtasksList,
        isSubtasksCollapsed = json.optBoolean("isSubtasksCollapsed", false),
        createdAt = createdAt,
        scheduledAtMillis = scheduledAt,
        scheduledEndMillis = scheduledEnd,
        notifiedAtScheduleMillis = notifiedAt,
        recurrenceRule = recurrence,
      )
    }
  }
}

/**
 * Snapshot of tasks and daily completed counter for undo/redo history.
 */
data class TodoHistorySnapshot(
  val tasks: List<TodoItem>,
  val completedToday: Int,
  val completedTasks: List<TodoItem> = emptyList(),
)

/**
 * Parsed result of reading a `.text` To Do list document containing active tasks (`- `)
 * and completed tasks (`+ `) with nested YAML subtasks.
 */
data class ParsedTodoListTtxt(
  val title: String = "To Do",
  val tasks: List<TodoItem> = emptyList(),
  val completedTasks: List<TodoItem> = emptyList(),
)

/**
 * Repository interface for saving and retrieving tasks in `.text` format and the daily completed count.
 */
interface TodoListRepository {
  fun loadTasks(): List<TodoItem>
  fun saveTasks(tasks: List<TodoItem>)
  fun getCompletedTodayCount(currentDate: String = getTodayDateKey()): Int
  fun setCompletedTodayCount(count: Int, currentDate: String = getTodayDateKey())
  fun loadCompletedTasks(currentDate: String = getTodayDateKey()): List<TodoItem>
  fun saveCompletedTasks(tasks: List<TodoItem>, currentDate: String = getTodayDateKey())
  fun loadSavedTasks(): List<TodoItem>
  fun saveSavedTasks(tasks: List<TodoItem>)
  fun toTtxt(currentDate: String = getTodayDateKey()): String =
    TodoListEngine.formatTodoListToTtxt(
      tasks = loadTasks(),
      completedTasks = loadCompletedTasks(currentDate),
    )
  fun getTodoTtxtFile(): File? = null
}

/**
 * In-memory repository for unit tests, previews, and isolation.
 * When [todoFile] is provided, the To Do list is also persisted to and loaded from [todoFile] in `.text` format.
 */
class InMemoryTodoListRepository(
  initialTasks: List<TodoItem> = emptyList(),
  private var completedDate: String = getTodayDateKey(),
  private var completedCount: Int = 0,
  initialCompletedTasks: List<TodoItem> = emptyList(),
  initialSavedTasks: List<TodoItem> = emptyList(),
  private val todoFile: File? = null,
) : TodoListRepository {
  private val tasks = initialTasks.toMutableList()
  private val completedTasksList = initialCompletedTasks.toMutableList()
  private val savedTasksList = initialSavedTasks.toMutableList()

  init {
    if (todoFile != null) {
      if (initialTasks.isNotEmpty() || initialCompletedTasks.isNotEmpty()) {
        TodoListEngine.saveTodoListToTtxtFile(todoFile, tasks, completedTasksList)
      } else if (todoFile.exists()) {
        val parsed = TodoListEngine.loadTodoListFromTtxtFile(todoFile)
        tasks.addAll(parsed.tasks)
        completedTasksList.addAll(parsed.completedTasks)
        if (completedCount == 0 && parsed.completedTasks.isNotEmpty()) {
          completedCount = parsed.completedTasks.size
        }
      }
    }
  }

  override fun loadTasks(): List<TodoItem> {
    val file = todoFile
    if (file != null && file.exists()) {
      val parsed = TodoListEngine.loadTodoListFromTtxtFile(
        file = file,
        metadataTasks = tasks.toList(),
        metadataCompletedTasks = completedTasksList.toList(),
      )
      tasks.clear()
      tasks.addAll(parsed.tasks)
    }
    return tasks.toList()
  }

  override fun saveTasks(tasks: List<TodoItem>) {
    this.tasks.clear()
    this.tasks.addAll(tasks)
    val file = todoFile
    if (file != null) {
      TodoListEngine.saveTodoListToTtxtFile(file, this.tasks, this.completedTasksList)
    }
  }

  override fun getCompletedTodayCount(currentDate: String): Int {
    return if (completedDate == currentDate) completedCount else 0
  }

  override fun setCompletedTodayCount(count: Int, currentDate: String) {
    completedDate = currentDate
    completedCount = maxOf(0, count)
  }

  override fun loadCompletedTasks(currentDate: String): List<TodoItem> {
    if (completedDate != currentDate) return emptyList()
    val file = todoFile
    if (file != null && file.exists()) {
      val parsed = TodoListEngine.loadTodoListFromTtxtFile(
        file = file,
        metadataTasks = tasks.toList(),
        metadataCompletedTasks = completedTasksList.toList(),
      )
      completedTasksList.clear()
      completedTasksList.addAll(parsed.completedTasks)
    }
    return completedTasksList.toList()
  }

  override fun saveCompletedTasks(tasks: List<TodoItem>, currentDate: String) {
    completedDate = currentDate
    completedTasksList.clear()
    completedTasksList.addAll(tasks)
    val file = todoFile
    if (file != null) {
      TodoListEngine.saveTodoListToTtxtFile(file, this.tasks, this.completedTasksList)
    }
  }

  override fun loadSavedTasks(): List<TodoItem> {
    return savedTasksList.toList()
  }

  override fun saveSavedTasks(tasks: List<TodoItem>) {
    savedTasksList.clear()
    savedTasksList.addAll(tasks)
  }

  override fun getTodoTtxtFile(): File? = todoFile
}

/**
 * Android [SharedPreferences] and `.text` file-backed repository for the To Do List tool.
 */
class SharedPreferencesTodoListRepository(
  private val sharedPreferences: SharedPreferences,
  private val todoFile: File? = null,
  private val hasExplicitTodoFile: Boolean = todoFile != null,
) : TodoListRepository {

  companion object {
    const val PREFS_NAME = "todo_list_preferences"
    const val KEY_TASKS_JSON = "tasks_json"
    const val KEY_TASKS_TTXT = "tasks_ttxt"
    const val KEY_COMPLETED_TODAY_DATE = "completed_today_date"
    const val KEY_COMPLETED_TODAY_COUNT = "completed_today_count"
    const val KEY_COMPLETED_TASKS_JSON = "completed_tasks_json"
    const val KEY_COMPLETED_TASKS_TTXT = "completed_tasks_ttxt"
    const val KEY_SAVED_TASKS_JSON = "saved_tasks_json"
    const val KEY_SAVED_TASKS_TTXT = "saved_tasks_ttxt"
  }

  constructor(context: Context) : this(
    sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    todoFile = TodoListEngine.getDefaultTodoTtxtFile(context),
    hasExplicitTodoFile = false,
  )

  constructor(context: Context, todoFile: File) : this(
    sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    todoFile = todoFile,
    hasExplicitTodoFile = true,
  )

  private fun parseTasksJson(jsonString: String?): List<TodoItem> {
    if (jsonString.isNullOrBlank()) return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<TodoItem>()
      for (i in 0 until array.length()) {
        val obj = array.optJSONObject(i)
        if (obj != null) {
          list.add(TodoItem.fromJson(obj))
        }
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  override fun loadTasks(): List<TodoItem> {
    val jsonString = sharedPreferences.getString(KEY_TASKS_JSON, null)
    val ttxtString = sharedPreferences.getString(KEY_TASKS_TTXT, null)
    val file = todoFile

    if (jsonString == null && ttxtString == null) {
      if (file != null) {
        if (hasExplicitTodoFile && file.exists()) {
          return TodoListEngine.loadTodoListFromTtxtFile(file).tasks
        } else if (!hasExplicitTodoFile && file.exists()) {
          try {
            file.delete()
          } catch (_: Throwable) {}
        }
      }
      return emptyList()
    }

    val metadataTasks = parseTasksJson(jsonString)
    if (hasExplicitTodoFile && file != null && file.exists()) {
      return TodoListEngine.loadTodoListFromTtxtFile(
        file = file,
        metadataTasks = metadataTasks,
      ).tasks
    }

    if (!ttxtString.isNullOrBlank()) {
      val parsed = TodoListEngine.parseTodoListFromTtxt(
        rawTtxt = ttxtString,
        metadataTasks = metadataTasks,
      )
      if (parsed.tasks.isNotEmpty() || metadataTasks.isEmpty()) {
        if (file != null && !file.exists() && parsed.tasks.isNotEmpty()) {
          try {
            TodoListEngine.saveTodoListToTtxtFile(file, parsed.tasks, emptyList())
          } catch (_: Throwable) {}
        }
        return parsed.tasks
      }
    }

    if (file != null && !file.exists() && metadataTasks.isNotEmpty()) {
      try {
        TodoListEngine.saveTodoListToTtxtFile(file, metadataTasks, emptyList())
      } catch (_: Throwable) {}
    }
    return metadataTasks
  }

  override fun saveTasks(tasks: List<TodoItem>) {
    val array = JSONArray()
    tasks.forEach { array.put(it.toJson()) }
    val tasksTtxt = TodoListEngine.formatTasksSectionToTtxt(tasks, isCompleted = false)
    sharedPreferences.edit()
      .putString(KEY_TASKS_JSON, array.toString())
      .putString(KEY_TASKS_TTXT, tasksTtxt)
      .apply()
    val file = todoFile
    if (file != null) {
      try {
        val currentCompleted = parseTasksJson(sharedPreferences.getString(KEY_COMPLETED_TASKS_JSON, null))
        TodoListEngine.saveTodoListToTtxtFile(file, tasks, currentCompleted)
      } catch (_: Throwable) {}
    }
  }

  override fun getCompletedTodayCount(currentDate: String): Int {
    val savedDate = sharedPreferences.getString(KEY_COMPLETED_TODAY_DATE, null)
    return if (savedDate == currentDate) {
      sharedPreferences.getInt(KEY_COMPLETED_TODAY_COUNT, 0)
    } else {
      0
    }
  }

  override fun setCompletedTodayCount(count: Int, currentDate: String) {
    sharedPreferences.edit()
      .putString(KEY_COMPLETED_TODAY_DATE, currentDate)
      .putInt(KEY_COMPLETED_TODAY_COUNT, maxOf(0, count))
      .apply()
  }

  override fun loadCompletedTasks(currentDate: String): List<TodoItem> {
    val savedDate = sharedPreferences.getString(KEY_COMPLETED_TODAY_DATE, null)
    if (savedDate != currentDate) {
      if (hasExplicitTodoFile && todoFile != null && todoFile.exists() && savedDate == null) {
        return TodoListEngine.loadTodoListFromTtxtFile(todoFile).completedTasks
      }
      return emptyList()
    }
    val jsonString = sharedPreferences.getString(KEY_COMPLETED_TASKS_JSON, null)
    val ttxtString = sharedPreferences.getString(KEY_COMPLETED_TASKS_TTXT, null)
    val metadataCompleted = parseTasksJson(jsonString)

    if (hasExplicitTodoFile && todoFile != null && todoFile.exists()) {
      return TodoListEngine.loadTodoListFromTtxtFile(
        file = todoFile,
        metadataCompletedTasks = metadataCompleted,
      ).completedTasks
    }

    if (!ttxtString.isNullOrBlank()) {
      val parsed = TodoListEngine.parseTodoListFromTtxt(
        rawTtxt = ttxtString,
        metadataCompletedTasks = metadataCompleted,
      )
      if (parsed.completedTasks.isNotEmpty() || metadataCompleted.isEmpty()) {
        return parsed.completedTasks
      }
    }
    return metadataCompleted
  }

  override fun saveCompletedTasks(tasks: List<TodoItem>, currentDate: String) {
    val array = JSONArray()
    tasks.forEach { array.put(it.toJson()) }
    val completedTtxt = TodoListEngine.formatTasksSectionToTtxt(tasks, isCompleted = true)
    sharedPreferences.edit()
      .putString(KEY_COMPLETED_TODAY_DATE, currentDate)
      .putString(KEY_COMPLETED_TASKS_JSON, array.toString())
      .putString(KEY_COMPLETED_TASKS_TTXT, completedTtxt)
      .apply()
    val file = todoFile
    if (file != null) {
      try {
        val currentActive = parseTasksJson(sharedPreferences.getString(KEY_TASKS_JSON, null))
        TodoListEngine.saveTodoListToTtxtFile(file, currentActive, tasks)
      } catch (_: Throwable) {}
    }
  }

  override fun loadSavedTasks(): List<TodoItem> {
    val jsonString = sharedPreferences.getString(KEY_SAVED_TASKS_JSON, null)
    val ttxtString = sharedPreferences.getString(KEY_SAVED_TASKS_TTXT, null)
    val metadataSaved = parseTasksJson(jsonString)
    if (!ttxtString.isNullOrBlank()) {
      val parsed = TodoListEngine.parseTodoListFromTtxt(
        rawTtxt = ttxtString,
        metadataTasks = metadataSaved,
      )
      if (parsed.tasks.isNotEmpty() || metadataSaved.isEmpty()) {
        return parsed.tasks
      }
    }
    return metadataSaved
  }

  override fun saveSavedTasks(tasks: List<TodoItem>) {
    val array = JSONArray()
    tasks.forEach { array.put(it.toJson()) }
    val savedTtxt = TodoListEngine.formatTasksSectionToTtxt(tasks, isCompleted = false)
    sharedPreferences.edit()
      .putString(KEY_SAVED_TASKS_JSON, array.toString())
      .putString(KEY_SAVED_TASKS_TTXT, savedTtxt)
      .apply()
  }

  override fun getTodoTtxtFile(): File? = todoFile
}

/**
 * Manages an undo and redo history stack for task list states and daily completed count.
 */
class TodoHistoryManager(
  initialTasks: List<TodoItem> = emptyList(),
  initialCompletedToday: Int = 0,
  initialCompletedTasks: List<TodoItem> = emptyList(),
) {
  private val undoStack = mutableListOf<TodoHistorySnapshot>()
  private val redoStack = mutableListOf<TodoHistorySnapshot>()

  var currentSnapshot: TodoHistorySnapshot = TodoHistorySnapshot(initialTasks, initialCompletedToday, initialCompletedTasks)
    private set

  val currentTasks: List<TodoItem> get() = currentSnapshot.tasks
  val currentCompletedToday: Int get() = currentSnapshot.completedToday
  val currentCompletedTasks: List<TodoItem> get() = currentSnapshot.completedTasks

  val canUndo: Boolean get() = undoStack.isNotEmpty()
  val canRedo: Boolean get() = redoStack.isNotEmpty()

  fun applyAction(
    newTasks: List<TodoItem>,
    newCompletedToday: Int = currentSnapshot.completedToday,
    newCompletedTasks: List<TodoItem> = currentSnapshot.completedTasks,
  ) {
    val nextSnapshot = TodoHistorySnapshot(newTasks, newCompletedToday, newCompletedTasks)
    if (nextSnapshot == currentSnapshot) return
    undoStack.add(currentSnapshot)
    if (undoStack.size > 50) {
      undoStack.removeAt(0)
    }
    redoStack.clear()
    currentSnapshot = nextSnapshot
  }

  fun applySnapshot(nextSnapshot: TodoHistorySnapshot) {
    applyAction(
      newTasks = nextSnapshot.tasks,
      newCompletedToday = nextSnapshot.completedToday,
      newCompletedTasks = nextSnapshot.completedTasks,
    )
  }

  fun completeTask(taskId: String): TodoHistorySnapshot {
    val next = TodoListEngine.completeTask(currentSnapshot, taskId)
    applySnapshot(next)
    return currentSnapshot
  }

  fun deleteTask(taskId: String): TodoHistorySnapshot {
    val next = TodoListEngine.deleteTask(currentSnapshot, taskId)
    applySnapshot(next)
    return currentSnapshot
  }

  fun uncompleteTask(taskId: String): TodoHistorySnapshot {
    val next = TodoListEngine.uncompleteTask(currentSnapshot, taskId)
    applySnapshot(next)
    return currentSnapshot
  }

  fun deleteCompletedTask(taskId: String): TodoHistorySnapshot {
    val next = TodoListEngine.deleteCompletedTask(currentSnapshot, taskId)
    applySnapshot(next)
    return currentSnapshot
  }

  fun promoteSubTaskToMainTask(taskId: String, subtaskId: String): TodoHistorySnapshot {
    val updatedTasks = TodoListEngine.promoteSubTaskToMainTask(currentSnapshot.tasks, taskId, subtaskId)
    applyAction(newTasks = updatedTasks)
    return currentSnapshot
  }

  fun undo(): TodoHistorySnapshot? {
    if (undoStack.isEmpty()) return null
    redoStack.add(currentSnapshot)
    currentSnapshot = undoStack.removeAt(undoStack.lastIndex)
    return currentSnapshot
  }

  fun redo(): TodoHistorySnapshot? {
    if (redoStack.isEmpty()) return null
    undoStack.add(currentSnapshot)
    currentSnapshot = redoStack.removeAt(redoStack.lastIndex)
    return currentSnapshot
  }
}

/**
 * Helper functions for tasks.
 */
object TodoListEngine {

  data class PendingQuickActionRequest(
    val openSchedule: Boolean,
    val requestId: Long = System.nanoTime(),
  )

  var pendingQuickActionRequest by mutableStateOf<PendingQuickActionRequest?>(null)
    private set

  /**
   * Requests that [TodoListScreen] immediately focus the "Add a task..." input
   * ([openSchedule] = `false`) or open the schedule view ([openSchedule] = `true`).
   */
  fun requestQuickAction(openSchedule: Boolean) {
    pendingQuickActionRequest = PendingQuickActionRequest(openSchedule = openSchedule)
  }

  /**
   * Consumes any pending quick-action request after [TodoListScreen] handles it.
   */
  fun consumePendingQuickActionRequest(): PendingQuickActionRequest? {
    val current = pendingQuickActionRequest
    pendingQuickActionRequest = null
    return current
  }

  private val TTXT_NESTED_LIST_LINE_REGEX = Regex("""^([ \t]*)([-+])\s+(.+)$""")

  private class MutableTtxtListNode(
    val indentSpaces: Int,
    val title: String,
    val isCompleted: Boolean,
    val children: MutableList<MutableTtxtListNode> = mutableListOf(),
  )

  /**
   * Formats a single [TodoItem] and its nested [TodoItem.subtasks] into YAML-indented `.text`
   * lines:
   * - `- <task title>` at indent 0 when [isCompleted] is `false`, or `+ <task title>` when `true`.
   * - Each level of nested [TodoSubTask]s is indented by 2 spaces (`"  ".repeat(level)`),
   *   using `-` for uncompleted subtasks and `+` for completed subtasks.
   */
  fun formatTaskToTtxt(
    task: TodoItem,
    isCompleted: Boolean = false,
  ): String {
    val prefix = if (isCompleted) '+' else '-'
    val selfLine = "$prefix ${task.title.trim()}"
    val validSubtasks = task.subtasks.filter { it.title.isNotBlank() }
    if (validSubtasks.isEmpty()) return selfLine
    val childLines = validSubtasks.map { it.toTtxt(indentLevel = 1) }
    return (listOf(selfLine) + childLines).joinToString("\n")
  }

  /**
   * Formats a list of [TodoItem]s into `.text` nested YAML list lines.
   */
  fun formatTasksSectionToTtxt(
    tasks: List<TodoItem>,
    isCompleted: Boolean = false,
  ): String {
    return tasks
      .filter { it.title.isNotBlank() }
      .joinToString("\n") { formatTaskToTtxt(it, isCompleted = isCompleted) }
  }

  /**
   * Formats the full To Do list ([tasks] and [completedTasks]) into a `.text` document
   * with YAML-style nested subtasks.
   */
  fun formatTodoListToTtxt(
    tasks: List<TodoItem>,
    completedTasks: List<TodoItem> = emptyList(),
    title: String = "To Do",
    includeHeader: Boolean = true,
  ): String {
    val sb = StringBuilder()
    if (includeHeader && title.isNotBlank()) {
      sb.append("# ").append(title.trim())
    }
    val activeSection = formatTasksSectionToTtxt(tasks, isCompleted = false)
    val completedSection = formatTasksSectionToTtxt(completedTasks, isCompleted = true)
    val combinedBody = listOf(activeSection, completedSection)
      .filter { it.isNotBlank() }
      .joinToString("\n")
    if (combinedBody.isNotBlank()) {
      if (sb.isNotEmpty()) sb.append("\n\n")
      sb.append(combinedBody)
    }
    return sb.toString()
  }

  private fun parseTtxtLinesToNodeTree(lines: List<String>): List<MutableTtxtListNode> {
    val rootNodes = mutableListOf<MutableTtxtListNode>()
    val stack = mutableListOf<MutableTtxtListNode>()

    for (rawLine in lines) {
      if (rawLine.isBlank()) continue
      val match = TTXT_NESTED_LIST_LINE_REGEX.matchEntire(rawLine) ?: continue
      val indentStr = match.groupValues[1].replace("\t", "  ")
      val indentSpaces = indentStr.length
      val marker = match.groupValues[2]
      val rawText = match.groupValues[3].trim()
      if (rawText.isEmpty()) continue

      val (cleanTitle, isCompleted) = when {
        marker == "-" && rawText.startsWith("[x] ", ignoreCase = true) ->
          rawText.substring(4).trim() to true
        marker == "-" && rawText.startsWith("[ ] ") ->
          rawText.substring(4).trim() to false
        else ->
          rawText to (marker == "+")
      }
      if (cleanTitle.isEmpty()) continue

      while (stack.isNotEmpty() && stack.last().indentSpaces >= indentSpaces) {
        stack.removeAt(stack.lastIndex)
      }

      val node = MutableTtxtListNode(
        indentSpaces = indentSpaces,
        title = cleanTitle,
        isCompleted = isCompleted,
      )
      if (stack.isEmpty()) {
        rootNodes.add(node)
      } else {
        stack.last().children.add(node)
      }
      stack.add(node)
    }
    return rootNodes
  }

  private fun convertNodesToSubtasks(
    nodes: List<MutableTtxtListNode>,
    existingSubtasks: List<TodoSubTask> = emptyList(),
  ): List<TodoSubTask> {
    val remainingExisting = existingSubtasks.toMutableList()
    return nodes.map { node ->
      val matchIdx = remainingExisting.indexOfFirst {
        it.title == node.title && it.isCompleted == node.isCompleted
      }.takeIf { it >= 0 }
        ?: remainingExisting.indexOfFirst { it.title == node.title }.takeIf { it >= 0 }
      val matchedSub = if (matchIdx != null) {
        remainingExisting.removeAt(matchIdx)
      } else {
        null
      }
      TodoSubTask(
        id = matchedSub?.id ?: UUID.randomUUID().toString(),
        title = node.title,
        isCompleted = node.isCompleted,
        subtasks = convertNodesToSubtasks(
          nodes = node.children,
          existingSubtasks = matchedSub?.subtasks.orEmpty(),
        ),
      )
    }
  }

  /**
   * Parses a `.text` To Do list document (with YAML-style 2-space nested lists) into active
   * [ParsedTodoListTtxt.tasks] (`- ` top-level items) and [ParsedTodoListTtxt.completedTasks]
   * (`+ ` top-level items), merging with optional [metadataTasks] and [metadataCompletedTasks].
   */
  fun parseTodoListFromTtxt(
    rawTtxt: String,
    metadataTasks: List<TodoItem> = emptyList(),
    metadataCompletedTasks: List<TodoItem> = emptyList(),
    nowMs: Long = System.currentTimeMillis(),
  ): ParsedTodoListTtxt {
    val normalized = rawTtxt.replace("\r\n", "\n").replace('\r', '\n')
    val lines = normalized.split('\n')
    val firstNonBlankIdx = lines.indexOfFirst { it.isNotBlank() }
    if (firstNonBlankIdx == -1) {
      return ParsedTodoListTtxt()
    }

    val firstTrimmed = lines[firstNonBlankIdx].trim()
    val docTitle: String
    val bodyLines: List<String>
    if (firstTrimmed.startsWith("# ") || firstTrimmed == "#") {
      docTitle = firstTrimmed.removePrefix("#").trim().ifBlank { "To Do" }
      bodyLines = lines.drop(firstNonBlankIdx + 1)
    } else {
      docTitle = "To Do"
      bodyLines = lines.drop(firstNonBlankIdx)
    }

    val rootNodes = parseTtxtLinesToNodeTree(bodyLines)
    val remainingActiveMeta = metadataTasks.toMutableList()
    val remainingCompletedMeta = metadataCompletedTasks.toMutableList()
    val activeTasks = mutableListOf<TodoItem>()
    val completedTasks = mutableListOf<TodoItem>()

    for (rootNode in rootNodes) {
      val metaPool = if (rootNode.isCompleted) remainingCompletedMeta else remainingActiveMeta
      val fallbackPool = if (rootNode.isCompleted) remainingActiveMeta else remainingCompletedMeta
      val matchIdx = metaPool.indexOfFirst { it.title == rootNode.title }.takeIf { it >= 0 }
      val matchedMeta = if (matchIdx != null) {
        metaPool.removeAt(matchIdx)
      } else {
        val fbIdx = fallbackPool.indexOfFirst { it.title == rootNode.title }.takeIf { it >= 0 }
        if (fbIdx != null) fallbackPool.removeAt(fbIdx) else null
      }

      val subtasks = convertNodesToSubtasks(
        nodes = rootNode.children,
        existingSubtasks = matchedMeta?.subtasks.orEmpty(),
      )
      val item = if (matchedMeta != null) {
        matchedMeta.copy(
          title = rootNode.title,
          subtasks = subtasks,
        )
      } else {
        TodoItem(
          id = UUID.randomUUID().toString(),
          title = rootNode.title,
          subtasks = subtasks,
          createdAt = nowMs,
        )
      }
      if (rootNode.isCompleted) {
        completedTasks.add(item)
      } else {
        activeTasks.add(item)
      }
    }

    return ParsedTodoListTtxt(
      title = docTitle,
      tasks = activeTasks,
      completedTasks = completedTasks,
    )
  }

  /**
   * Parses a single [TodoItem] (and its nested YAML subtasks) from a `.text` string.
   */
  fun parseTaskFromTtxt(
    rawTtxt: String,
    id: String = UUID.randomUUID().toString(),
    nowMs: Long = System.currentTimeMillis(),
  ): TodoItem {
    val parsed = parseTodoListFromTtxt(rawTtxt = rawTtxt, nowMs = nowMs)
    val allTopLevel = parsed.tasks + parsed.completedTasks
    if (allTopLevel.size == 1) {
      return allTopLevel.first().copy(id = id, createdAt = nowMs)
    }
    if (allTopLevel.size > 1) {
      val normalized = rawTtxt.replace("\r\n", "\n").replace('\r', '\n')
      val firstNonBlank = normalized.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
      if (firstNonBlank.startsWith("# ")) {
        val headingTitle = firstNonBlank.removePrefix("# ").trim()
        val bodyNodes = parseTtxtLinesToNodeTree(
          normalized.lines().dropWhile { it.isBlank() || it.trim().startsWith("#") }
        )
        return TodoItem(
          id = id,
          title = headingTitle.ifBlank { "Imported Task" },
          subtasks = convertNodesToSubtasks(bodyNodes),
          createdAt = nowMs,
        )
      }
      val first = allTopLevel.first()
      return first.copy(id = id, createdAt = nowMs)
    }
    val fallbackTitle = rawTtxt.lineSequence()
      .map { it.trim().removePrefix("#").trim() }
      .firstOrNull { it.isNotEmpty() }
      .orEmpty()
    return TodoItem(
      id = id,
      title = fallbackTitle,
      createdAt = nowMs,
    )
  }

  /**
   * Writes the To Do list ([tasks] and [completedTasks]) to [file] in `.text` format.
   */
  fun saveTodoListToTtxtFile(
    file: File,
    tasks: List<TodoItem>,
    completedTasks: List<TodoItem> = emptyList(),
  ) {
    file.parentFile?.let { parent ->
      if (!parent.exists()) {
        parent.mkdirs()
      }
    }
    file.writeText(formatTodoListToTtxt(tasks = tasks, completedTasks = completedTasks))
  }

  /**
   * Reads and parses a `.text` To Do list from [file].
   */
  fun loadTodoListFromTtxtFile(
    file: File,
    metadataTasks: List<TodoItem> = emptyList(),
    metadataCompletedTasks: List<TodoItem> = emptyList(),
  ): ParsedTodoListTtxt {
    if (!file.exists() || !file.isFile) {
      return ParsedTodoListTtxt(tasks = metadataTasks, completedTasks = metadataCompletedTasks)
    }
    val rawTtxt = try {
      file.readText()
    } catch (_: Throwable) {
      return ParsedTodoListTtxt(tasks = metadataTasks, completedTasks = metadataCompletedTasks)
    }
    return parseTodoListFromTtxt(
      rawTtxt = rawTtxt,
      metadataTasks = metadataTasks,
      metadataCompletedTasks = metadataCompletedTasks,
      nowMs = file.lastModified().takeIf { it > 0L } ?: System.currentTimeMillis(),
    )
  }

  /**
   * Resolves the default `todo_list.text` file inside `/Notes` (or app files fallback).
   */
  fun getDefaultTodoTtxtFile(
    context: Context? = null,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): File {
    val notesDir = NotesEngine.getDefaultNotesDirectory(context = context, userRoot = userRoot)
    return File(notesDir, TODO_LIST_TTXT_FILE_NAME)
  }

  /**
   * Result of applying a vertical drag delta during task or subtask reordering.
   */
  data class DragReorderStepResult<T>(
    val items: List<T>,
    val remainingOffsetY: Float,
    val moved: Boolean,
    val fromIndex: Int,
    val toIndex: Int,
  )

  /**
   * Calculates item reordering and residual visual offset during a vertical drag gesture,
   * accounting for individual item heights, inter-item spacing, and list boundaries.
   */
  fun <T> calculateDragReorder(
    items: List<T>,
    draggedId: String,
    idSelector: (T) -> String,
    currentOffsetY: Float,
    itemHeightsPx: Map<String, Float>,
    defaultItemHeightPx: Float,
    itemSpacingPx: Float,
    thresholdFraction: Float = 0.55f,
    maxOverscrollPx: Float = 0f,
  ): DragReorderStepResult<T> {
    val initialIndex = items.indexOfFirst { idSelector(it) == draggedId }
    if (initialIndex == -1 || items.size <= 1) {
      val clamped = currentOffsetY.coerceIn(-maxOverscrollPx, maxOverscrollPx)
      return DragReorderStepResult(
        items = items,
        remainingOffsetY = clamped,
        moved = false,
        fromIndex = initialIndex,
        toIndex = initialIndex,
      )
    }

    var currentIndex = initialIndex
    var offsetY = currentOffsetY
    var currentList = items
    var moved = false

    while (true) {
      if (offsetY > 0f && currentIndex < currentList.size - 1) {
        val nextItem = currentList[currentIndex + 1]
        val nextHeight = (itemHeightsPx[idSelector(nextItem)] ?: defaultItemHeightPx).coerceAtLeast(1f)
        val stepPx = nextHeight + itemSpacingPx
        val threshold = stepPx * thresholdFraction
        if (offsetY > threshold) {
          val mutable = currentList.toMutableList()
          val item = mutable.removeAt(currentIndex)
          mutable.add(currentIndex + 1, item)
          currentList = mutable.toList()
          offsetY -= stepPx
          currentIndex += 1
          moved = true
          continue
        }
      } else if (offsetY < 0f && currentIndex > 0) {
        val prevItem = currentList[currentIndex - 1]
        val prevHeight = (itemHeightsPx[idSelector(prevItem)] ?: defaultItemHeightPx).coerceAtLeast(1f)
        val stepPx = prevHeight + itemSpacingPx
        val threshold = stepPx * thresholdFraction
        if (offsetY < -threshold) {
          val mutable = currentList.toMutableList()
          val item = mutable.removeAt(currentIndex)
          mutable.add(currentIndex - 1, item)
          currentList = mutable.toList()
          offsetY += stepPx
          currentIndex -= 1
          moved = true
          continue
        }
      }
      break
    }

    if (currentIndex == 0 && offsetY < -maxOverscrollPx) {
      offsetY = -maxOverscrollPx
    }
    if (currentIndex == currentList.lastIndex && offsetY > maxOverscrollPx) {
      offsetY = maxOverscrollPx
    }

    return DragReorderStepResult(
      items = currentList,
      remainingOffsetY = offsetY,
      moved = moved,
      fromIndex = initialIndex,
      toIndex = currentIndex,
    )
  }

  /**
   * Moves a task from [fromIndex] to [toIndex].
   */
  fun reorderTasks(tasks: List<TodoItem>, fromIndex: Int, toIndex: Int): List<TodoItem> {
    if (fromIndex !in tasks.indices || toIndex !in tasks.indices || fromIndex == toIndex) {
      return tasks
    }
    val mutable = tasks.toMutableList()
    val item = mutable.removeAt(fromIndex)
    mutable.add(toIndex, item)
    return mutable.toList()
  }

  /**
   * Moves a subtask from [fromIndex] to [toIndex] within task [taskId].
   */
  fun reorderSubTasks(
    tasks: List<TodoItem>,
    taskId: String,
    fromIndex: Int,
    toIndex: Int,
  ): List<TodoItem> {
    return tasks.map { item ->
      if (item.id == taskId) {
        if (fromIndex !in item.subtasks.indices || toIndex !in item.subtasks.indices || fromIndex == toIndex) {
          item
        } else {
          val mutable = item.subtasks.toMutableList()
          val sub = mutable.removeAt(fromIndex)
          mutable.add(toIndex, sub)
          item.copy(subtasks = mutable.toList())
        }
      } else item
    }
  }

  /**
   * Toggles the collapsed/expanded state of subtasks for task [taskId].
   */
  fun toggleSubtasksCollapsed(tasks: List<TodoItem>, taskId: String): List<TodoItem> {
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(isSubtasksCollapsed = !item.isSubtasksCollapsed)
      } else item
    }
  }

  private fun toggleSubTaskRecursive(
    subtasks: List<TodoSubTask>,
    subtaskId: String,
  ): List<TodoSubTask> {
    return subtasks.map { sub ->
      when {
        sub.id == subtaskId -> sub.copy(isCompleted = !sub.isCompleted)
        sub.subtasks.isNotEmpty() -> sub.copy(subtasks = toggleSubTaskRecursive(sub.subtasks, subtaskId))
        else -> sub
      }
    }
  }

  /**
   * Toggles completion of a subtask (or nested subtask) within a task.
   */
  fun toggleSubTask(tasks: List<TodoItem>, taskId: String, subtaskId: String): List<TodoItem> {
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(subtasks = toggleSubTaskRecursive(item.subtasks, subtaskId))
      } else item
    }
  }

  /**
   * Appends a subtask to a task. If [subtaskTitle] contains a multi-line YAML-indented `.text` list,
   * parses the nested subtask hierarchy in place.
   */
  fun addSubTask(tasks: List<TodoItem>, taskId: String, subtaskTitle: String): List<TodoItem> {
    val trimmed = subtaskTitle.trim()
    if (trimmed.isEmpty()) return tasks
    val hasMultipleLines = subtaskTitle.contains('\n') || subtaskTitle.contains('\r')
    val parsedSubtasks = if (hasMultipleLines) {
      val nodes = parseTtxtLinesToNodeTree(
        subtaskTitle.replace("\r\n", "\n").replace('\r', '\n').lines()
      )
      if (nodes.isNotEmpty()) {
        convertNodesToSubtasks(nodes)
      } else {
        listOf(TodoSubTask(title = trimmed))
      }
    } else {
      listOf(TodoSubTask(title = trimmed))
    }
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(subtasks = item.subtasks + parsedSubtasks)
      } else item
    }
  }

  private fun addNestedSubTaskRecursive(
    subtasks: List<TodoSubTask>,
    parentSubtaskId: String,
    newChild: TodoSubTask,
  ): List<TodoSubTask> {
    return subtasks.map { sub ->
      when {
        sub.id == parentSubtaskId -> sub.copy(subtasks = sub.subtasks + newChild)
        sub.subtasks.isNotEmpty() -> sub.copy(
          subtasks = addNestedSubTaskRecursive(sub.subtasks, parentSubtaskId, newChild)
        )
        else -> sub
      }
    }
  }

  /**
   * Appends a nested child subtask under [parentSubtaskId] within task [taskId].
   */
  fun addNestedSubTask(
    tasks: List<TodoItem>,
    taskId: String,
    parentSubtaskId: String,
    subtaskTitle: String,
  ): List<TodoItem> {
    val trimmed = subtaskTitle.trim()
    if (trimmed.isEmpty()) return tasks
    val newChild = TodoSubTask(title = trimmed)
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(subtasks = addNestedSubTaskRecursive(item.subtasks, parentSubtaskId, newChild))
      } else item
    }
  }

  private fun deleteSubTaskRecursive(
    subtasks: List<TodoSubTask>,
    subtaskId: String,
  ): List<TodoSubTask> {
    return subtasks
      .filter { it.id != subtaskId }
      .map { sub ->
        if (sub.subtasks.isNotEmpty()) {
          sub.copy(subtasks = deleteSubTaskRecursive(sub.subtasks, subtaskId))
        } else {
          sub
        }
      }
  }

  /**
   * Removes a subtask (or nested subtask) from a task.
   */
  fun deleteSubTask(tasks: List<TodoItem>, taskId: String, subtaskId: String): List<TodoItem> {
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(subtasks = deleteSubTaskRecursive(item.subtasks, subtaskId))
      } else item
    }
  }

  private fun findSubTaskRecursive(
    subtasks: List<TodoSubTask>,
    subtaskId: String,
  ): TodoSubTask? {
    for (sub in subtasks) {
      if (sub.id == subtaskId) return sub
      if (sub.subtasks.isNotEmpty()) {
        findSubTaskRecursive(sub.subtasks, subtaskId)?.let { return it }
      }
    }
    return null
  }

  /**
   * Removes a subtask from task [taskId] and promotes it into a standalone [TodoItem]
   * in the main task list (inserted immediately after the parent task), preserving any nested children
   * as its own subtasks.
   */
  fun promoteSubTaskToMainTask(
    tasks: List<TodoItem>,
    taskId: String,
    subtaskId: String,
  ): List<TodoItem> {
    val parentIndex = tasks.indexOfFirst { it.id == taskId }
    if (parentIndex == -1) return tasks
    val parentTask = tasks[parentIndex]
    val subtask = findSubTaskRecursive(parentTask.subtasks, subtaskId) ?: return tasks

    val updatedParent = parentTask.copy(
      subtasks = deleteSubTaskRecursive(parentTask.subtasks, subtaskId),
    )
    val promotedTask = TodoItem(
      id = subtask.id,
      title = subtask.title,
      subtasks = subtask.subtasks,
    )
    val mutable = tasks.toMutableList()
    mutable[parentIndex] = updatedParent
    mutable.add(parentIndex + 1, promotedTask)
    return mutable.toList()
  }

  private fun updateSubTaskTitleRecursive(
    subtasks: List<TodoSubTask>,
    subtaskId: String,
    newTitle: String,
  ): List<TodoSubTask> {
    return subtasks.map { sub ->
      when {
        sub.id == subtaskId -> sub.copy(title = newTitle)
        sub.subtasks.isNotEmpty() -> sub.copy(
          subtasks = updateSubTaskTitleRecursive(sub.subtasks, subtaskId, newTitle)
        )
        else -> sub
      }
    }
  }

  /**
   * Updates the title of an existing subtask (or nested subtask).
   */
  fun updateSubTaskTitle(tasks: List<TodoItem>, taskId: String, subtaskId: String, newTitle: String): List<TodoItem> {
    val trimmed = newTitle.trim()
    if (trimmed.isEmpty()) return tasks
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(subtasks = updateSubTaskTitleRecursive(item.subtasks, subtaskId, trimmed))
      } else item
    }
  }

  /**
   * Removes a completed task by [taskId] from the completed tasks list.
   */
  fun removeCompletedTask(completedTasks: List<TodoItem>, taskId: String): List<TodoItem> {
    return completedTasks.filter { it.id != taskId }
  }

  /**
   * Returns active tasks that should be shown in the main tasks view at [nowMillis],
   * excluding scheduled tasks that are more than [horizonMillis] (default 24 hours) in the future.
   */
  fun getMainViewTasks(
    tasks: List<TodoItem>,
    nowMillis: Long = System.currentTimeMillis(),
    horizonMillis: Long = MAIN_TASKS_SCHEDULE_HORIZON_MILLIS,
  ): List<TodoItem> {
    return tasks.filter { it.isVisibleInMainTasksView(nowMillis, horizonMillis) }
  }

  /**
   * Filters [tasks] by case-insensitive substring matching across title, notes, subtasks,
   * formatted schedule label, and recurrence rule label.
   */
  fun filterTasks(tasks: List<TodoItem>, query: String): List<TodoItem> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return tasks
    return tasks.filter { task ->
      task.title.contains(trimmed, ignoreCase = true) ||
        task.notes.contains(trimmed, ignoreCase = true) ||
        task.subtasks.any { it.title.contains(trimmed, ignoreCase = true) } ||
        (task.scheduledAtMillis != null &&
          formatScheduleLabel(task.scheduledAtMillis, task.scheduledEndMillis)
            .contains(trimmed, ignoreCase = true)) ||
        (task.recurrenceRule != null &&
          task.recurrenceRule.formatLabel().contains(trimmed, ignoreCase = true))
    }
  }

  /**
   * Completes an active task by [taskId].
   * - Non-recurring tasks move from [TodoHistorySnapshot.tasks] to [TodoHistorySnapshot.completedTasks].
   * - Recurring tasks ([TodoItem.recurrenceRule] != null) automatically reschedule themselves for
   *   their next occurrence based on the selected recurrence preset at [nowMillis] (with subtasks reset
   *   to uncompleted), while also recording the completed occurrence in [TodoHistorySnapshot.completedTasks]
   *   and incrementing [TodoHistorySnapshot.completedToday].
   */
  fun completeTask(
    snapshot: TodoHistorySnapshot,
    taskId: String,
    nowMillis: Long = System.currentTimeMillis(),
  ): TodoHistorySnapshot {
    val task = snapshot.tasks.firstOrNull { it.id == taskId } ?: return snapshot
    val rule = task.recurrenceRule
    if (rule != null) {
      val nextStart = rule.nextScheduledAtMillis(nowMillis)
      val duration = if (
        task.scheduledAtMillis != null &&
        task.scheduledEndMillis != null &&
        task.scheduledEndMillis > task.scheduledAtMillis
      ) {
        task.scheduledEndMillis - task.scheduledAtMillis
      } else {
        null
      }
      val rescheduledTask = task.copy(
        scheduledAtMillis = nextStart,
        scheduledEndMillis = duration?.let { nextStart + it },
        notifiedAtScheduleMillis = null,
        subtasks = task.subtasks.map { it.copy(isCompleted = false) },
      )
      val completedOccurrence = task.copy(
        id = "${task.id}_completed_$nowMillis",
        isSubtasksCollapsed = true,
        subtasks = task.subtasks.map { it.copy(isCompleted = true) },
      )
      return snapshot.copy(
        tasks = snapshot.tasks.map { if (it.id == taskId) rescheduledTask else it },
        completedToday = snapshot.completedToday + 1,
        completedTasks = listOf(completedOccurrence) + snapshot.completedTasks,
      )
    }

    val updatedTasks = snapshot.tasks.filter { it.id != taskId }
    val completedItem = task.copy(
      isSubtasksCollapsed = true,
      subtasks = task.subtasks.map { it.copy(isCompleted = true) },
    )
    return snapshot.copy(
      tasks = updatedTasks,
      completedToday = snapshot.completedToday + 1,
      completedTasks = listOf(completedItem) + snapshot.completedTasks,
    )
  }

  /**
   * Completes multiple active tasks by [taskIds].
   * Recurring tasks automatically reschedule to their next occurrence at [nowMillis].
   */
  fun completeTasks(
    snapshot: TodoHistorySnapshot,
    taskIds: Set<String>,
    nowMillis: Long = System.currentTimeMillis(),
  ): TodoHistorySnapshot {
    val toComplete = snapshot.tasks.filter { it.id in taskIds }
    if (toComplete.isEmpty()) return snapshot

    val updatedTasks = snapshot.tasks.mapNotNull { task ->
      if (task.id !in taskIds) {
        task
      } else {
        val rule = task.recurrenceRule
        if (rule != null) {
          val nextStart = rule.nextScheduledAtMillis(nowMillis)
          val duration = if (
            task.scheduledAtMillis != null &&
            task.scheduledEndMillis != null &&
            task.scheduledEndMillis > task.scheduledAtMillis
          ) {
            task.scheduledEndMillis - task.scheduledAtMillis
          } else {
            null
          }
          task.copy(
            scheduledAtMillis = nextStart,
            scheduledEndMillis = duration?.let { nextStart + it },
            notifiedAtScheduleMillis = null,
            subtasks = task.subtasks.map { it.copy(isCompleted = false) },
          )
        } else {
          null
        }
      }
    }

    val completedItems = toComplete.mapIndexed { idx, task ->
      val completedId = if (task.recurrenceRule != null) {
        "${task.id}_completed_${nowMillis}_$idx"
      } else {
        task.id
      }
      task.copy(
        id = completedId,
        isSubtasksCollapsed = true,
        subtasks = task.subtasks.map { it.copy(isCompleted = true) },
      )
    }
    return snapshot.copy(
      tasks = updatedTasks,
      completedToday = snapshot.completedToday + completedItems.size,
      completedTasks = completedItems + snapshot.completedTasks,
    )
  }

  /**
   * Deletes an active task by [taskId] from [TodoHistorySnapshot.tasks].
   */
  fun deleteTask(snapshot: TodoHistorySnapshot, taskId: String): TodoHistorySnapshot {
    if (snapshot.tasks.none { it.id == taskId }) return snapshot
    return snapshot.copy(
      tasks = snapshot.tasks.filter { it.id != taskId },
    )
  }

  /**
   * Deletes multiple active tasks by [taskIds].
   */
  fun deleteTasks(snapshot: TodoHistorySnapshot, taskIds: Set<String>): TodoHistorySnapshot {
    if (snapshot.tasks.none { it.id in taskIds }) return snapshot
    return snapshot.copy(
      tasks = snapshot.tasks.filterNot { it.id in taskIds },
    )
  }

  /**
   * Uncompletes a completed task by [taskId], restoring it from [TodoHistorySnapshot.completedTasks]
   * back to [TodoHistorySnapshot.tasks] and decrementing [TodoHistorySnapshot.completedToday].
   */
  fun uncompleteTask(snapshot: TodoHistorySnapshot, taskId: String): TodoHistorySnapshot {
    val completedTask = snapshot.completedTasks.firstOrNull { it.id == taskId } ?: return snapshot
    val updatedCompleted = snapshot.completedTasks.filter { it.id != taskId }
    val restoredTask = completedTask.copy(
      isSubtasksCollapsed = false,
      subtasks = completedTask.subtasks.map { it.copy(isCompleted = false) },
    )
    return snapshot.copy(
      tasks = listOf(restoredTask) + snapshot.tasks,
      completedToday = maxOf(0, snapshot.completedToday - 1),
      completedTasks = updatedCompleted,
    )
  }

  /**
   * Uncompletes multiple completed tasks by [taskIds].
   */
  fun uncompleteTasks(snapshot: TodoHistorySnapshot, taskIds: Set<String>): TodoHistorySnapshot {
    val toRestore = snapshot.completedTasks.filter { it.id in taskIds }
    if (toRestore.isEmpty()) return snapshot
    val updatedCompleted = snapshot.completedTasks.filterNot { it.id in taskIds }
    val restoredTasks = toRestore.map { completedTask ->
      completedTask.copy(
        isSubtasksCollapsed = false,
        subtasks = completedTask.subtasks.map { it.copy(isCompleted = false) },
      )
    }
    return snapshot.copy(
      tasks = restoredTasks + snapshot.tasks,
      completedToday = maxOf(0, snapshot.completedToday - restoredTasks.size),
      completedTasks = updatedCompleted,
    )
  }

  /**
   * Deletes a completed task by [taskId] from [TodoHistorySnapshot.completedTasks]
   * and decrements [TodoHistorySnapshot.completedToday].
   */
  fun deleteCompletedTask(snapshot: TodoHistorySnapshot, taskId: String): TodoHistorySnapshot {
    if (snapshot.completedTasks.none { it.id == taskId }) return snapshot
    return snapshot.copy(
      completedToday = maxOf(0, snapshot.completedToday - 1),
      completedTasks = snapshot.completedTasks.filter { it.id != taskId },
    )
  }

  /**
   * Deletes multiple completed tasks by [taskIds] and decrements [TodoHistorySnapshot.completedToday].
   */
  fun deleteCompletedTasks(snapshot: TodoHistorySnapshot, taskIds: Set<String>): TodoHistorySnapshot {
    val removedCount = snapshot.completedTasks.count { it.id in taskIds }
    if (removedCount == 0) return snapshot
    return snapshot.copy(
      completedToday = maxOf(0, snapshot.completedToday - removedCount),
      completedTasks = snapshot.completedTasks.filterNot { it.id in taskIds },
    )
  }

  /**
   * Saves [task] as a template in [savedTasks], preserving its title, notes, and subtasks
   * (with completion reset to false) and deduplicating by trimmed title.
   */
  fun saveTaskAsTemplate(savedTasks: List<TodoItem>, task: TodoItem): List<TodoItem> {
    val trimmed = task.title.trim()
    if (trimmed.isEmpty()) return savedTasks
    val template = TodoItem(
      title = trimmed,
      notes = task.notes.trim(),
      subtasks = task.subtasks.map { TodoSubTask(title = it.title, isCompleted = false) },
      isSubtasksCollapsed = false,
    )
    return listOf(template) + savedTasks.filter { it.title != trimmed }
  }

  /**
   * Schedules or reschedules a task for [scheduledAtMillis] and an optional [scheduledEndMillis]
   * date/time range and optional [recurrenceRule]. Resets [TodoItem.notifiedAtScheduleMillis]
   * so a notification fires when [scheduledAtMillis] is reached.
   */
  fun scheduleTask(
    tasks: List<TodoItem>,
    taskId: String,
    scheduledAtMillis: Long,
    scheduledEndMillis: Long? = null,
    notes: String? = null,
    recurrenceRule: TodoRecurrenceRule? = null,
  ): List<TodoItem> {
    val normalizedEnd = scheduledEndMillis?.takeIf { it > scheduledAtMillis }
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(
          scheduledAtMillis = scheduledAtMillis,
          scheduledEndMillis = normalizedEnd,
          notifiedAtScheduleMillis = null,
          notes = notes?.trim() ?: item.notes,
          recurrenceRule = recurrenceRule,
        )
      } else {
        item
      }
    }
  }

  /**
   * Schedules or reschedules multiple tasks by [taskIds].
   */
  fun scheduleTasks(
    tasks: List<TodoItem>,
    taskIds: Set<String>,
    scheduledAtMillis: Long,
    scheduledEndMillis: Long? = null,
    recurrenceRule: TodoRecurrenceRule? = null,
  ): List<TodoItem> {
    val normalizedEnd = scheduledEndMillis?.takeIf { it > scheduledAtMillis }
    return tasks.map { item ->
      if (item.id in taskIds) {
        item.copy(
          scheduledAtMillis = scheduledAtMillis,
          scheduledEndMillis = normalizedEnd,
          notifiedAtScheduleMillis = null,
          recurrenceRule = recurrenceRule,
        )
      } else {
        item
      }
    }
  }

  /**
   * Clears any schedule and recurrence rule from the specified task.
   */
  fun unscheduleTask(tasks: List<TodoItem>, taskId: String): List<TodoItem> {
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(
          scheduledAtMillis = null,
          scheduledEndMillis = null,
          notifiedAtScheduleMillis = null,
          recurrenceRule = null,
        )
      } else {
        item
      }
    }
  }

  /**
   * Clears any schedule and recurrence rule from multiple tasks by [taskIds].
   */
  fun unscheduleTasks(tasks: List<TodoItem>, taskIds: Set<String>): List<TodoItem> {
    return tasks.map { item ->
      if (item.id in taskIds) {
        item.copy(
          scheduledAtMillis = null,
          scheduledEndMillis = null,
          notifiedAtScheduleMillis = null,
          recurrenceRule = null,
        )
      } else {
        item
      }
    }
  }

  /**
   * Updates the note contents of an existing task.
   */
  fun updateTaskNotes(tasks: List<TodoItem>, taskId: String, notes: String): List<TodoItem> {
    val trimmed = notes.trim()
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(notes = trimmed)
      } else {
        item
      }
    }
  }

  /**
   * Marks that a notification has been shown for [scheduledAtMillis] on [taskId].
   */
  fun markTaskNotified(
    tasks: List<TodoItem>,
    taskId: String,
    scheduledAtMillis: Long,
  ): List<TodoItem> {
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(notifiedAtScheduleMillis = scheduledAtMillis)
      } else {
        item
      }
    }
  }

  /**
   * Returns all active tasks whose scheduled date/time has been reached and have not yet
   * triggered a notification for their current schedule.
   */
  fun getDueUnnotifiedTasks(
    tasks: List<TodoItem>,
    nowMillis: Long = System.currentTimeMillis(),
  ): List<TodoItem> {
    return tasks.filter { it.shouldTriggerNotification(nowMillis) }
  }

  /**
   * Returns the set of task IDs from [previousTasks] whose alarms should be cancelled because
   * they were deleted, completed, unscheduled, or already notified in [currentTasks].
   */
  fun getTaskIdsToCancelAlarms(
    previousTasks: List<TodoItem>,
    currentTasks: List<TodoItem>,
  ): Set<String> {
    val activePendingAlarmIds = currentTasks
      .filter { it.scheduledAtMillis != null && it.notifiedAtScheduleMillis != it.scheduledAtMillis }
      .map { it.id }
      .toSet()
    return previousTasks
      .map { it.id }
      .filter { it !in activePendingAlarmIds }
      .toSet()
  }

  /**
   * Evaluates a fired scheduled alarm against the active tasks in [repository].
   * If the task still exists in active tasks, is still scheduled for [scheduledAtMillis] (when provided),
   * and has not yet been notified for that schedule, marks it notified in [repository] and returns it.
   * Returns null if the task was deleted, completed, unscheduled, rescheduled, or already notified.
   */
  fun consumeScheduledAlarm(
    repository: TodoListRepository,
    taskId: String?,
    scheduledAtMillis: Long? = null,
  ): TodoItem? {
    if (taskId.isNullOrBlank()) return null
    val tasks = repository.loadTasks()
    val matchingTask = tasks.firstOrNull { it.id == taskId } ?: return null
    val currentScheduledAt = matchingTask.scheduledAtMillis ?: return null
    if (scheduledAtMillis != null && scheduledAtMillis > 0L && scheduledAtMillis != currentScheduledAt) {
      return null
    }
    if (matchingTask.notifiedAtScheduleMillis == currentScheduledAt) {
      return null
    }
    val updated = markTaskNotified(tasks, matchingTask.id, currentScheduledAt)
    repository.saveTasks(updated)
    return matchingTask
  }

  /**
   * Formats a scheduled timestamp or date/time range into a concise human-readable label.
   */
  fun formatScheduleLabel(
    scheduledAtMillis: Long,
    scheduledEndMillis: Long? = null,
    nowMillis: Long = System.currentTimeMillis(),
  ): String {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    val shortDateFormat = SimpleDateFormat("MMM d", Locale.US)
    val fullDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val startCal = Calendar.getInstance().apply { timeInMillis = scheduledAtMillis }

    fun formatDayPrefix(cal: Calendar): String {
      val sameYear = cal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
      val dayDiff = if (sameYear) {
        cal.get(Calendar.DAY_OF_YEAR) - nowCal.get(Calendar.DAY_OF_YEAR)
      } else {
        Int.MIN_VALUE
      }
      return when {
        sameYear && dayDiff == 0 -> "Today"
        sameYear && dayDiff == 1 -> "Tomorrow"
        sameYear && dayDiff == -1 -> "Yesterday"
        sameYear -> shortDateFormat.format(Date(cal.timeInMillis))
        else -> fullDateFormat.format(Date(cal.timeInMillis))
      }
    }

    val startDayLabel = formatDayPrefix(startCal)
    val startTimeLabel = timeFormat.format(Date(scheduledAtMillis))

    val validEnd = scheduledEndMillis?.takeIf { it > scheduledAtMillis }
      ?: return "$startDayLabel, $startTimeLabel"

    val endCal = Calendar.getInstance().apply { timeInMillis = validEnd }
    val endTimeLabel = timeFormat.format(Date(validEnd))
    val sameDay = startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
      startCal.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR)

    return if (sameDay) {
      "$startDayLabel, $startTimeLabel – $endTimeLabel"
    } else {
      val endDayLabel = formatDayPrefix(endCal)
      "$startDayLabel, $startTimeLabel – $endDayLabel, $endTimeLabel"
    }
  }
}
