package com.example.toolbox.ui.tools

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoListTest {

  @Test
  fun todoItem_serializesAndDeserializesCorrectly() {
    val item = TodoItem(
      id = "test-uuid-1234",
      title = "Buy fresh strings",
      subtasks = listOf(
        TodoSubTask(id = "sub-1", title = "Check gauge 10-46", isCompleted = true),
        TodoSubTask(id = "sub-2", title = "Get spare high E", isCompleted = false),
      ),
      createdAt = 1699900000000L,
    )

    val json = item.toJson()
    val restored = TodoItem.fromJson(json)

    assertEquals("test-uuid-1234", restored.id)
    assertEquals("Buy fresh strings", restored.title)
    assertEquals(1699900000000L, restored.createdAt)
    assertEquals(2, restored.subtasks.size)
    assertEquals("sub-1", restored.subtasks[0].id)
    assertEquals("Check gauge 10-46", restored.subtasks[0].title)
    assertTrue(restored.subtasks[0].isCompleted)
    assertEquals("sub-2", restored.subtasks[1].id)
    assertFalse(restored.subtasks[1].isCompleted)
  }

  @Test
  fun todoItem_fromJson_handlesMissingOptionalFields() {
    val json = JSONObject().apply {
      put("title", "Minimal task")
    }
    val item = TodoItem.fromJson(json)

    assertEquals("Minimal task", item.title)
    assertNotNull(item.id)
    assertTrue(item.subtasks.isEmpty())
  }

  @Test
  fun inMemoryRepository_loadsAndSavesTasks() {
    val repo = InMemoryTodoListRepository()
    assertTrue(repo.loadTasks().isEmpty())

    val tasks = listOf(
      TodoItem(title = "Task 1"),
      TodoItem(title = "Task 2"),
    )
    repo.saveTasks(tasks)

    val loaded = repo.loadTasks()
    assertEquals(2, loaded.size)
    assertEquals("Task 1", loaded[0].title)
    assertEquals("Task 2", loaded[1].title)

    // Completed tasks
    assertTrue(repo.loadCompletedTasks().isEmpty())
    val completed = listOf(
      TodoItem(title = "Finished Task", subtasks = listOf(TodoSubTask(title = "Sub 1", isCompleted = true))),
    )
    repo.saveCompletedTasks(completed)
    val loadedCompleted = repo.loadCompletedTasks()
    assertEquals(1, loadedCompleted.size)
    assertEquals("Finished Task", loadedCompleted[0].title)
    assertEquals(1, loadedCompleted[0].subtasks.size)

    // Saved tasks
    assertTrue(repo.loadSavedTasks().isEmpty())
    val templates = listOf(
      TodoItem(title = "Template 1", subtasks = listOf(TodoSubTask(title = "Sub A"))),
    )
    repo.saveSavedTasks(templates)
    val loadedSaved = repo.loadSavedTasks()
    assertEquals(1, loadedSaved.size)
    assertEquals("Template 1", loadedSaved[0].title)
    assertEquals("Sub A", loadedSaved[0].subtasks[0].title)
  }

  @Test
  fun engine_managesSubtasksCorrectly() {
    val task = TodoItem(id = "t1", title = "Parent Task")
    val list = listOf(task)

    // Add subtask 1
    val withSub1 = TodoListEngine.addSubTask(list, taskId = "t1", subtaskTitle = "Step 1")
    assertEquals(1, withSub1[0].subtasks.size)
    assertEquals("Step 1", withSub1[0].subtasks[0].title)
    assertFalse(withSub1[0].subtasks[0].isCompleted)

    // Add subtask 2
    val withSub2 = TodoListEngine.addSubTask(withSub1, taskId = "t1", subtaskTitle = "Step 2")
    assertEquals(2, withSub2[0].subtasks.size)
    val sub1Id = withSub2[0].subtasks[0].id
    val sub2Id = withSub2[0].subtasks[1].id

    // Toggle subtask 1
    val toggled = TodoListEngine.toggleSubTask(withSub2, taskId = "t1", subtaskId = sub1Id)
    assertTrue(toggled[0].subtasks.first { it.id == sub1Id }.isCompleted)
    assertFalse(toggled[0].subtasks.first { it.id == sub2Id }.isCompleted)

    // Toggle subtask 1 back
    val untoggled = TodoListEngine.toggleSubTask(toggled, taskId = "t1", subtaskId = sub1Id)
    assertFalse(untoggled[0].subtasks.first { it.id == sub1Id }.isCompleted)

    // Delete subtask 2
    val afterDelete = TodoListEngine.deleteSubTask(untoggled, taskId = "t1", subtaskId = sub2Id)
    assertEquals(1, afterDelete[0].subtasks.size)
    assertEquals(sub1Id, afterDelete[0].subtasks[0].id)

    // Update subtask 1 title inline
    val afterUpdate = TodoListEngine.updateSubTaskTitle(afterDelete, taskId = "t1", subtaskId = sub1Id, newTitle = "Updated step 1")
    assertEquals("Updated step 1", afterUpdate[0].subtasks[0].title)

    // Reorder subtasks
    val taskWithThree = TodoListEngine.addSubTask(
      TodoListEngine.addSubTask(afterUpdate, taskId = "t1", subtaskTitle = "Step 2"),
      taskId = "t1",
      subtaskTitle = "Step 3",
    )
    assertEquals(3, taskWithThree[0].subtasks.size)
    assertEquals("Updated step 1", taskWithThree[0].subtasks[0].title)
    assertEquals("Step 2", taskWithThree[0].subtasks[1].title)
    assertEquals("Step 3", taskWithThree[0].subtasks[2].title)

    // Move Step 3 from index 2 to index 0
    val reordered = TodoListEngine.reorderSubTasks(taskWithThree, taskId = "t1", fromIndex = 2, toIndex = 0)
    assertEquals("Step 3", reordered[0].subtasks[0].title)
    assertEquals("Updated step 1", reordered[0].subtasks[1].title)
    assertEquals("Step 2", reordered[0].subtasks[2].title)

    // Subtasks open by default
    assertFalse(reordered[0].isSubtasksCollapsed)

    // Toggle collapse
    val collapsed = TodoListEngine.toggleSubtasksCollapsed(reordered, taskId = "t1")
    assertTrue(collapsed[0].isSubtasksCollapsed)

    // Toggle expand
    val expanded = TodoListEngine.toggleSubtasksCollapsed(collapsed, taskId = "t1")
    assertFalse(expanded[0].isSubtasksCollapsed)
  }

  @Test
  fun historyManager_supportsUndoAndRedo() {
    val initial = listOf(TodoItem(title = "Task 1"))
    val history = TodoHistoryManager(initial)

    assertFalse(history.canUndo)
    assertFalse(history.canRedo)

    // Add a new task (Action 1)
    val state2 = listOf(TodoItem(title = "Task 2"), TodoItem(title = "Task 1"))
    history.applyAction(state2)

    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertEquals(state2, history.currentTasks)

    // Add another task (Action 2)
    val state3 = listOf(TodoItem(title = "Task 3"), TodoItem(title = "Task 2"), TodoItem(title = "Task 1"))
    history.applyAction(state3)

    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertEquals(state3, history.currentTasks)

    // Undo -> restores state2
    val undone = history.undo()
    assertEquals(state2, undone?.tasks)
    assertEquals(state2, history.currentTasks)
    assertTrue(history.canUndo)
    assertTrue(history.canRedo)

    // Undo again -> restores initial
    val undone2 = history.undo()
    assertEquals(initial, undone2?.tasks)
    assertEquals(initial, history.currentTasks)
    assertFalse(history.canUndo)
    assertTrue(history.canRedo)

    // Redo -> restores state2
    val redone = history.redo()
    assertEquals(state2, redone?.tasks)
    assertEquals(state2, history.currentTasks)
    assertTrue(history.canUndo)
    assertTrue(history.canRedo)

    // New action invalidates redo stack
    val state4 = listOf(TodoItem(title = "Task 4"))
    history.applyAction(state4)
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
  }

  @Test
  fun engine_reordersTasksCorrectly() {
    val t1 = TodoItem(title = "A")
    val t2 = TodoItem(title = "B")
    val t3 = TodoItem(title = "C")
    val list = listOf(t1, t2, t3)

    // Move A to index 2: [B, C, A]
    val reordered = TodoListEngine.reorderTasks(list, fromIndex = 0, toIndex = 2)
    assertEquals(listOf("B", "C", "A"), reordered.map { it.title })

    // Move A back to index 1: [B, A, C]
    val reorderedBack = TodoListEngine.reorderTasks(reordered, fromIndex = 2, toIndex = 1)
    assertEquals(listOf("B", "A", "C"), reorderedBack.map { it.title })

    // Invalid index does nothing
    val noOp = TodoListEngine.reorderTasks(list, fromIndex = -1, toIndex = 2)
    assertEquals(list, noOp)
  }

  @Test
  fun repository_completedToday_incrementsAndResetsAtMidnight() {
    val repo = InMemoryTodoListRepository()
    val day1 = "2026-11-09"
    val day2 = "2026-11-10" // After midnight

    // Initially 0
    assertEquals(0, repo.getCompletedTodayCount(day1))

    // Set/increment count on day 1
    repo.setCompletedTodayCount(5, day1)
    assertEquals(5, repo.getCompletedTodayCount(day1))

    // Once midnight passes (day 2), count automatically resets to 0!
    assertEquals(0, repo.getCompletedTodayCount(day2))

    // Setting count on day 2 stores count for day 2
    repo.setCompletedTodayCount(1, day2)
    assertEquals(1, repo.getCompletedTodayCount(day2))
  }

  @Test
  fun historyManager_tracksCompletedTodayAcrossUndoRedo() {
    val initial = listOf(TodoItem(title = "Task 1"))
    val history = TodoHistoryManager(initialTasks = initial, initialCompletedToday = 0)

    assertEquals(0, history.currentCompletedToday)

    // Complete task -> 0 active tasks, 1 completed today
    history.applyAction(emptyList(), newCompletedToday = 1)
    assertEquals(1, history.currentCompletedToday)
    assertEquals(0, history.currentTasks.size)

    // Undo -> restores task and reverts completed today counter back to 0!
    val undone = history.undo()
    assertNotNull(undone)
    assertEquals(0, history.currentCompletedToday)
    assertEquals(1, history.currentTasks.size)

    // Redo -> restores 1 completed today
    val redone = history.redo()
    assertNotNull(redone)
    assertEquals(1, history.currentCompletedToday)
    assertEquals(0, history.currentTasks.size)
  }
}
