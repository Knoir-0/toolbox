package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Generates a date key formatted as YYYY-MM-DD for midnight reset comparison.
 */
fun getTodayDateKey(timeMillis: Long = System.currentTimeMillis()): String {
  val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  return sdf.format(Date(timeMillis))
}

/**
 * Represents a single subtask within a [TodoItem].
 */
data class TodoSubTask(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val isCompleted: Boolean = false,
) {
  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("isCompleted", isCompleted)
  }

  companion object {
    fun fromJson(json: JSONObject): TodoSubTask = TodoSubTask(
      id = json.optString("id", UUID.randomUUID().toString()),
      title = json.optString("title", ""),
      isCompleted = json.optBoolean("isCompleted", false),
    )
  }
}

/**
 * Represents a single todo task, optionally containing subtasks.
 */
data class TodoItem(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val subtasks: List<TodoSubTask> = emptyList(),
  val isSubtasksCollapsed: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
) {
  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    val subtasksArray = JSONArray()
    subtasks.forEach { subtasksArray.put(it.toJson()) }
    put("subtasks", subtasksArray)
    put("isSubtasksCollapsed", isSubtasksCollapsed)
    put("createdAt", createdAt)
  }

  companion object {
    fun fromJson(json: JSONObject): TodoItem {
      val subtasksList = mutableListOf<TodoSubTask>()
      if (json.has("subtasks")) {
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
      return TodoItem(
        id = json.optString("id", UUID.randomUUID().toString()),
        title = json.optString("title", ""),
        subtasks = subtasksList,
        isSubtasksCollapsed = json.optBoolean("isSubtasksCollapsed", false),
        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
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
 * Repository interface for saving and retrieving tasks and the daily completed count.
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
}

/**
 * In-memory repository for unit tests, previews, and isolation.
 */
class InMemoryTodoListRepository(
  initialTasks: List<TodoItem> = emptyList(),
  private var completedDate: String = getTodayDateKey(),
  private var completedCount: Int = 0,
  initialCompletedTasks: List<TodoItem> = emptyList(),
  initialSavedTasks: List<TodoItem> = emptyList(),
) : TodoListRepository {
  private val tasks = initialTasks.toMutableList()
  private val completedTasksList = initialCompletedTasks.toMutableList()
  private val savedTasksList = initialSavedTasks.toMutableList()

  override fun loadTasks(): List<TodoItem> = tasks.toList()

  override fun saveTasks(tasks: List<TodoItem>) {
    this.tasks.clear()
    this.tasks.addAll(tasks)
  }

  override fun getCompletedTodayCount(currentDate: String): Int {
    return if (completedDate == currentDate) completedCount else 0
  }

  override fun setCompletedTodayCount(count: Int, currentDate: String) {
    completedDate = currentDate
    completedCount = maxOf(0, count)
  }

  override fun loadCompletedTasks(currentDate: String): List<TodoItem> {
    return if (completedDate == currentDate) completedTasksList.toList() else emptyList()
  }

  override fun saveCompletedTasks(tasks: List<TodoItem>, currentDate: String) {
    completedDate = currentDate
    completedTasksList.clear()
    completedTasksList.addAll(tasks)
  }

  override fun loadSavedTasks(): List<TodoItem> {
    return savedTasksList.toList()
  }

  override fun saveSavedTasks(tasks: List<TodoItem>) {
    savedTasksList.clear()
    savedTasksList.addAll(tasks)
  }
}

/**
 * Android [SharedPreferences]-backed repository.
 */
class SharedPreferencesTodoListRepository(
  private val sharedPreferences: SharedPreferences,
) : TodoListRepository {

  companion object {
    const val PREFS_NAME = "todo_list_preferences"
    const val KEY_TASKS_JSON = "tasks_json"
    const val KEY_COMPLETED_TODAY_DATE = "completed_today_date"
    const val KEY_COMPLETED_TODAY_COUNT = "completed_today_count"
    const val KEY_COMPLETED_TASKS_JSON = "completed_tasks_json"
    const val KEY_SAVED_TASKS_JSON = "saved_tasks_json"
  }

  constructor(context: Context) : this(
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  )

  override fun loadTasks(): List<TodoItem> {
    val jsonString = sharedPreferences.getString(KEY_TASKS_JSON, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<TodoItem>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(TodoItem.fromJson(obj))
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  override fun saveTasks(tasks: List<TodoItem>) {
    val array = JSONArray()
    tasks.forEach { array.put(it.toJson()) }
    sharedPreferences.edit().putString(KEY_TASKS_JSON, array.toString()).apply()
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
    if (savedDate != currentDate) return emptyList()
    val jsonString = sharedPreferences.getString(KEY_COMPLETED_TASKS_JSON, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<TodoItem>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(TodoItem.fromJson(obj))
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  override fun saveCompletedTasks(tasks: List<TodoItem>, currentDate: String) {
    val array = JSONArray()
    tasks.forEach { array.put(it.toJson()) }
    sharedPreferences.edit()
      .putString(KEY_COMPLETED_TODAY_DATE, currentDate)
      .putString(KEY_COMPLETED_TASKS_JSON, array.toString())
      .apply()
  }

  override fun loadSavedTasks(): List<TodoItem> {
    val jsonString = sharedPreferences.getString(KEY_SAVED_TASKS_JSON, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<TodoItem>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(TodoItem.fromJson(obj))
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  override fun saveSavedTasks(tasks: List<TodoItem>) {
    val array = JSONArray()
    tasks.forEach { array.put(it.toJson()) }
    sharedPreferences.edit()
      .putString(KEY_SAVED_TASKS_JSON, array.toString())
      .apply()
  }
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

  /**
   * Toggles completion of a subtask within a task.
   */
  fun toggleSubTask(tasks: List<TodoItem>, taskId: String, subtaskId: String): List<TodoItem> {
    return tasks.map { item ->
      if (item.id == taskId) {
        val updatedSubtasks = item.subtasks.map { sub ->
          if (sub.id == subtaskId) sub.copy(isCompleted = !sub.isCompleted) else sub
        }
        item.copy(subtasks = updatedSubtasks)
      } else item
    }
  }

  /**
   * Appends a subtask to a task.
   */
  fun addSubTask(tasks: List<TodoItem>, taskId: String, subtaskTitle: String): List<TodoItem> {
    val trimmed = subtaskTitle.trim()
    if (trimmed.isEmpty()) return tasks
    return tasks.map { item ->
      if (item.id == taskId) {
        val newSubtask = TodoSubTask(title = trimmed)
        item.copy(subtasks = item.subtasks + newSubtask)
      } else item
    }
  }

  /**
   * Removes a subtask from a task.
   */
  fun deleteSubTask(tasks: List<TodoItem>, taskId: String, subtaskId: String): List<TodoItem> {
    return tasks.map { item ->
      if (item.id == taskId) {
        item.copy(subtasks = item.subtasks.filter { it.id != subtaskId })
      } else item
    }
  }

  /**
   * Updates the title of an existing subtask.
   */
  fun updateSubTaskTitle(tasks: List<TodoItem>, taskId: String, subtaskId: String, newTitle: String): List<TodoItem> {
    val trimmed = newTitle.trim()
    if (trimmed.isEmpty()) return tasks
    return tasks.map { item ->
      if (item.id == taskId) {
        val updatedSubtasks = item.subtasks.map { sub ->
          if (sub.id == subtaskId) sub.copy(title = trimmed) else sub
        }
        item.copy(subtasks = updatedSubtasks)
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
   * Completes an active task by [taskId], moving it from [TodoHistorySnapshot.tasks]
   * to [TodoHistorySnapshot.completedTasks] and incrementing [TodoHistorySnapshot.completedToday].
   */
  fun completeTask(snapshot: TodoHistorySnapshot, taskId: String): TodoHistorySnapshot {
    val task = snapshot.tasks.firstOrNull { it.id == taskId } ?: return snapshot
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
   * Deletes an active task by [taskId] from [TodoHistorySnapshot.tasks].
   */
  fun deleteTask(snapshot: TodoHistorySnapshot, taskId: String): TodoHistorySnapshot {
    if (snapshot.tasks.none { it.id == taskId }) return snapshot
    return snapshot.copy(
      tasks = snapshot.tasks.filter { it.id != taskId },
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
   * Saves [task] as a template in [savedTasks], preserving its title and subtasks
   * (with completion reset to false) and deduplicating by trimmed title.
   */
  fun saveTaskAsTemplate(savedTasks: List<TodoItem>, task: TodoItem): List<TodoItem> {
    val trimmed = task.title.trim()
    if (trimmed.isEmpty()) return savedTasks
    val template = TodoItem(
      title = trimmed,
      subtasks = task.subtasks.map { TodoSubTask(title = it.title, isCompleted = false) },
      isSubtasksCollapsed = false,
    )
    return listOf(template) + savedTasks.filter { it.title != trimmed }
  }
}
