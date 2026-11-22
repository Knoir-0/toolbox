package com.example.toolbox.ui.tools

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

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

  @Test
  fun engine_calculateDragReorder_handlesVariableHeightsAndHysteresis() {
    val t1 = TodoItem(id = "t1", title = "Tall task with subtasks")
    val t2 = TodoItem(id = "t2", title = "Compact task 1")
    val t3 = TodoItem(id = "t3", title = "Compact task 2")
    val initial = listOf(t1, t2, t3)

    // t1 is 450px tall, while t2 and t3 are 140px tall; spacing is 10px
    val heights = mapOf(
      "t1" to 450f,
      "t2" to 140f,
      "t3" to 140f,
    )

    // Dragging compact t2 down toward compact t3 should use t3's step (140 + 10 = 150px),
    // NOT t1's height (450px). Threshold = 150 * 0.55 = 82.5px.
    val beforeThreshold = TodoListEngine.calculateDragReorder(
      items = initial,
      draggedId = "t2",
      idSelector = { it.id },
      currentOffsetY = 80f,
      itemHeightsPx = heights,
      defaultItemHeightPx = 140f,
      itemSpacingPx = 10f,
    )
    assertFalse(beforeThreshold.moved)
    assertEquals(listOf("t1", "t2", "t3"), beforeThreshold.items.map { it.id })
    assertEquals(80f, beforeThreshold.remainingOffsetY, 0.01f)

    // Dragging past 82.5px swaps t2 and t3 and subtracts 150px from offset
    val swappedDown = TodoListEngine.calculateDragReorder(
      items = initial,
      draggedId = "t2",
      idSelector = { it.id },
      currentOffsetY = 90f,
      itemHeightsPx = heights,
      defaultItemHeightPx = 140f,
      itemSpacingPx = 10f,
    )
    assertTrue(swappedDown.moved)
    assertEquals(1, swappedDown.fromIndex)
    assertEquals(2, swappedDown.toIndex)
    assertEquals(listOf("t1", "t3", "t2"), swappedDown.items.map { it.id })
    assertEquals(-60f, swappedDown.remainingOffsetY, 0.01f)

    // Immediately holding steady or nudging slightly (-60f) does NOT oscillate back
    val steadyAfterSwap = TodoListEngine.calculateDragReorder(
      items = swappedDown.items,
      draggedId = "t2",
      idSelector = { it.id },
      currentOffsetY = swappedDown.remainingOffsetY,
      itemHeightsPx = heights,
      defaultItemHeightPx = 140f,
      itemSpacingPx = 10f,
    )
    assertFalse(steadyAfterSwap.moved)
    assertEquals(listOf("t1", "t3", "t2"), steadyAfterSwap.items.map { it.id })
  }

  @Test
  fun engine_calculateDragReorder_supportsMultiItemFastDragAndBoundaryClamping() {
    val s1 = TodoSubTask(id = "s1", title = "Step 1")
    val s2 = TodoSubTask(id = "s2", title = "Step 2")
    val s3 = TodoSubTask(id = "s3", title = "Step 3")
    val subtasks = listOf(s1, s2, s3)
    val heights = mapOf("s1" to 40f, "s2" to 40f, "s3" to 40f)

    // Each step is 40 + 6 = 46px. Dragging s1 down by +120px crosses both s2 and s3 in one step.
    val fastMove = TodoListEngine.calculateDragReorder(
      items = subtasks,
      draggedId = "s1",
      idSelector = { it.id },
      currentOffsetY = 120f,
      itemHeightsPx = heights,
      defaultItemHeightPx = 40f,
      itemSpacingPx = 6f,
      maxOverscrollPx = 4f,
    )
    assertTrue(fastMove.moved)
    assertEquals(0, fastMove.fromIndex)
    assertEquals(2, fastMove.toIndex)
    assertEquals(listOf("s2", "s3", "s1"), fastMove.items.map { it.id })
    // 120 - 46 - 46 = 28, then clamped to maxOverscrollPx (4f) because s1 is now at lastIndex
    assertEquals(4f, fastMove.remainingOffsetY, 0.01f)

    // Dragging above index 0 clamps to -maxOverscrollPx
    val topClamp = TodoListEngine.calculateDragReorder(
      items = subtasks,
      draggedId = "s1",
      idSelector = { it.id },
      currentOffsetY = -100f,
      itemHeightsPx = heights,
      defaultItemHeightPx = 40f,
      itemSpacingPx = 6f,
      maxOverscrollPx = 4f,
    )
    assertFalse(topClamp.moved)
    assertEquals(-4f, topClamp.remainingOffsetY, 0.01f)
  }

  @Test
  fun engine_removeCompletedTask_removesMatchingTask() {
    val c1 = TodoItem(id = "c1", title = "Completed 1")
    val c2 = TodoItem(id = "c2", title = "Completed 2")
    val completed = listOf(c1, c2)

    val afterRemove = TodoListEngine.removeCompletedTask(completed, "c1")
    assertEquals(1, afterRemove.size)
    assertEquals("c2", afterRemove[0].id)
  }

  @Test
  fun historyManager_tracksCompleteDeleteAndUncompleteTasksAcrossUndoRedo() {
    val t1 = TodoItem(id = "t1", title = "Task 1")
    val t2 = TodoItem(id = "t2", title = "Task 2")
    val history = TodoHistoryManager(
      initialTasks = listOf(t1, t2),
      initialCompletedToday = 0,
      initialCompletedTasks = emptyList(),
    )

    assertFalse(history.canUndo)
    assertFalse(history.canRedo)

    // 1. Complete Task 1
    history.completeTask("t1")
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertEquals(listOf("t2"), history.currentTasks.map { it.id })
    assertEquals(1, history.currentCompletedToday)
    assertEquals(listOf("t1"), history.currentCompletedTasks.map { it.id })

    // 2. Delete active Task 2
    history.deleteTask("t2")
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertTrue(history.currentTasks.isEmpty())
    assertEquals(1, history.currentCompletedToday)
    assertEquals(listOf("t1"), history.currentCompletedTasks.map { it.id })

    // 3. Undo delete Task 2 -> Task 2 returns, Task 1 stays completed
    val afterUndoDelete = history.undo()
    assertNotNull(afterUndoDelete)
    assertEquals(listOf("t2"), history.currentTasks.map { it.id })
    assertEquals(1, history.currentCompletedToday)
    assertEquals(listOf("t1"), history.currentCompletedTasks.map { it.id })
    assertTrue(history.canUndo)
    assertTrue(history.canRedo)

    // 4. Redo delete Task 2 -> Task 2 deleted again
    val afterRedoDelete = history.redo()
    assertNotNull(afterRedoDelete)
    assertTrue(history.currentTasks.isEmpty())
    assertEquals(1, history.currentCompletedToday)
    assertEquals(listOf("t1"), history.currentCompletedTasks.map { it.id })
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)

    // 5. Uncomplete Task 1 from completed tasks -> moves back to active tasks & decrements completedToday
    history.uncompleteTask("t1")
    assertEquals(listOf("t1"), history.currentTasks.map { it.id })
    assertEquals(0, history.currentCompletedToday)
    assertTrue(history.currentCompletedTasks.isEmpty())
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)

    // 6. Undo uncomplete Task 1 -> moves back to completedTasks & restores completedToday = 1
    history.undo()
    assertTrue(history.currentTasks.isEmpty())
    assertEquals(1, history.currentCompletedToday)
    assertEquals(listOf("t1"), history.currentCompletedTasks.map { it.id })
    assertTrue(history.canRedo)

    // 7. Redo uncomplete Task 1 -> back in active tasks
    history.redo()
    assertEquals(listOf("t1"), history.currentTasks.map { it.id })
    assertEquals(0, history.currentCompletedToday)
    assertTrue(history.currentCompletedTasks.isEmpty())
    assertFalse(history.canRedo)

    // 8. Complete Task 1 again, then delete it from completedTasks, then undo & redo
    history.completeTask("t1")
    assertEquals(listOf("t1"), history.currentCompletedTasks.map { it.id })
    history.deleteCompletedTask("t1")
    assertTrue(history.currentCompletedTasks.isEmpty())
    assertEquals(0, history.currentCompletedToday)

    history.undo()
    assertEquals(listOf("t1"), history.currentCompletedTasks.map { it.id })
    assertEquals(1, history.currentCompletedToday)
    assertTrue(history.canRedo)

    history.redo()
    assertTrue(history.currentCompletedTasks.isEmpty())
    assertEquals(0, history.currentCompletedToday)
    assertFalse(history.canRedo)
  }

  @Test
  fun engine_saveTaskAsTemplate_savesTaskWithResetSubtasksAndDeduplicatesByTitle() {
    val taskWithSubtasks = TodoItem(
      id = "task-1",
      title = "  Morning Routine  ",
      subtasks = listOf(
        TodoSubTask(id = "sub-1", title = "Make coffee", isCompleted = true),
        TodoSubTask(id = "sub-2", title = "Review schedule", isCompleted = false),
      ),
      isSubtasksCollapsed = true,
    )

    val saved1 = TodoListEngine.saveTaskAsTemplate(emptyList(), taskWithSubtasks)
    assertEquals(1, saved1.size)
    assertEquals("Morning Routine", saved1[0].title)
    assertFalse(saved1[0].isSubtasksCollapsed)
    assertEquals(2, saved1[0].subtasks.size)
    assertFalse(saved1[0].subtasks[0].isCompleted)
    assertFalse(saved1[0].subtasks[1].isCompleted)

    // Saving another task with the same trimmed title replaces the existing template at the top
    val updatedTask = taskWithSubtasks.copy(
      subtasks = listOf(TodoSubTask(title = "Make espresso", isCompleted = true)),
    )
    val saved2 = TodoListEngine.saveTaskAsTemplate(saved1, updatedTask)
    assertEquals(1, saved2.size)
    assertEquals("Morning Routine", saved2[0].title)
    assertEquals(1, saved2[0].subtasks.size)
    assertEquals("Make espresso", saved2[0].subtasks[0].title)
    assertFalse(saved2[0].subtasks[0].isCompleted)
  }

  @Test
  fun engine_schedulesReschedulesAndDetectsDueNotifications() {
    val baseTime = 1_760_000_000_000L
    val initial = listOf(
      TodoItem(
        id = "t1",
        title = "Check lab oscillator",
        subtasks = listOf(
          TodoSubTask(title = "Verify 1kHz sine wave", isCompleted = false),
          TodoSubTask(title = "Measure peak-to-peak voltage", isCompleted = true),
        ),
      ),
    )

    // Schedule for +30m to +90m with note contents
    val start1 = baseTime + 30 * 60_000L
    val end1 = baseTime + 90 * 60_000L
    val scheduled = TodoListEngine.scheduleTask(
      tasks = initial,
      taskId = "t1",
      scheduledAtMillis = start1,
      scheduledEndMillis = end1,
      notes = "Bring BNC probe adapters",
    )

    assertTrue(scheduled[0].isScheduled)
    assertEquals(start1, scheduled[0].scheduledAtMillis)
    assertEquals(end1, scheduled[0].scheduledEndMillis)
    assertEquals("Bring BNC probe adapters", scheduled[0].notes)
    assertFalse(scheduled[0].isDue(baseTime + 10 * 60_000L))
    assertTrue(TodoListEngine.getDueUnnotifiedTasks(scheduled, baseTime + 10 * 60_000L).isEmpty())

    // JSON round-trip preserves schedule range and notes
    val restored = TodoItem.fromJson(scheduled[0].toJson())
    assertEquals(start1, restored.scheduledAtMillis)
    assertEquals(end1, restored.scheduledEndMillis)
    assertEquals("Bring BNC probe adapters", restored.notes)

    // When scheduled time is reached, task is due and triggers notification with title & contents
    val dueNow = TodoListEngine.getDueUnnotifiedTasks(scheduled, start1)
    assertEquals(1, dueNow.size)
    assertEquals("Check lab oscillator", dueNow[0].title)
    val content = dueNow[0].buildNotificationContent()
    assertTrue(content.contains("Bring BNC probe adapters"))
    assertTrue(content.contains("• Verify 1kHz sine wave"))
    assertTrue(content.contains("✓ Measure peak-to-peak voltage"))

    // Marking task notified prevents duplicate firing for the same schedule
    val markedNotified = TodoListEngine.markTaskNotified(scheduled, "t1", start1)
    assertTrue(TodoListEngine.getDueUnnotifiedTasks(markedNotified, start1 + 5_000L).isEmpty())

    // Rescheduling to a new variable date/time range resets notifiedAtScheduleMillis so it will fire again
    val start2 = baseTime + 3 * 3_600_000L
    val end2 = baseTime + 5 * 3_600_000L
    val rescheduled = TodoListEngine.scheduleTask(
      tasks = markedNotified,
      taskId = "t1",
      scheduledAtMillis = start2,
      scheduledEndMillis = end2,
    )
    assertEquals(start2, rescheduled[0].scheduledAtMillis)
    assertEquals(end2, rescheduled[0].scheduledEndMillis)
    assertTrue(TodoListEngine.getDueUnnotifiedTasks(rescheduled, start1 + 60_000L).isEmpty())
    assertEquals(1, TodoListEngine.getDueUnnotifiedTasks(rescheduled, start2).size)

    // Unscheduling clears schedule fields
    val unscheduled = TodoListEngine.unscheduleTask(rescheduled, "t1")
    assertFalse(unscheduled[0].isScheduled)
  }

  @Test
  fun engine_deletingOrCompletingScheduledTask_cancelsAlarmAndSuppressesNotification() {
    val baseTime = 1_760_000_000_000L
    val scheduledTime = baseTime + 30 * 60_000L
    val task1 = TodoItem(
      id = "scheduled-to-delete",
      title = "Delete me before alarm",
      scheduledAtMillis = scheduledTime,
    )
    val task2 = TodoItem(
      id = "scheduled-to-keep",
      title = "Keep me scheduled",
      scheduledAtMillis = scheduledTime,
    )
    val repo = InMemoryTodoListRepository(initialTasks = listOf(task1, task2))

    // Delete task1 before scheduledTime is reached
    val initialSnapshot = TodoHistorySnapshot(tasks = repo.loadTasks(), completedToday = 0)
    val afterDeleteSnapshot = TodoListEngine.deleteTask(initialSnapshot, "scheduled-to-delete")
    val cancelledIds = TodoListEngine.getTaskIdsToCancelAlarms(initialSnapshot.tasks, afterDeleteSnapshot.tasks)
    assertEquals(setOf("scheduled-to-delete"), cancelledIds)

    repo.saveTasks(afterDeleteSnapshot.tasks)

    // When scheduledTime is reached, deleted task must NOT trigger in-app due notification or alarm receiver notification
    val dueTasks = TodoListEngine.getDueUnnotifiedTasks(repo.loadTasks(), scheduledTime)
    assertEquals(listOf("scheduled-to-keep"), dueTasks.map { it.id })

    val deletedAlarmResult = TodoListEngine.consumeScheduledAlarm(
      repository = repo,
      taskId = "scheduled-to-delete",
      scheduledAtMillis = scheduledTime,
    )
    assertNull(deletedAlarmResult)

    // Active scheduled task DOES trigger once and is marked notified
    val activeAlarmResult = TodoListEngine.consumeScheduledAlarm(
      repository = repo,
      taskId = "scheduled-to-keep",
      scheduledAtMillis = scheduledTime,
    )
    assertNotNull(activeAlarmResult)
    assertEquals("Keep me scheduled", activeAlarmResult?.title)

    // Duplicate alarm for the same schedule does not re-trigger
    assertNull(
      TodoListEngine.consumeScheduledAlarm(
        repository = repo,
        taskId = "scheduled-to-keep",
        scheduledAtMillis = scheduledTime,
      ),
    )
  }

  @Test
  fun engine_filterTasks_matchesTitleNotesSubtasksAndScheduleLabel() {
    val tasks = listOf(
      TodoItem(
        id = "t1",
        title = "Buy soldering flux",
        notes = "Rosin core for PCB work",
        subtasks = listOf(TodoSubTask(title = "Check 63/37 alloy")),
      ),
      TodoItem(
        id = "t2",
        title = "Water tomatoes",
        notes = "Garden bed B",
        scheduledAtMillis = System.currentTimeMillis(),
      ),
    )

    assertEquals(2, TodoListEngine.filterTasks(tasks, "   ").size)
    assertEquals(listOf("t1"), TodoListEngine.filterTasks(tasks, "flux").map { it.id })
    assertEquals(listOf("t1"), TodoListEngine.filterTasks(tasks, "pcb").map { it.id })
    assertEquals(listOf("t1"), TodoListEngine.filterTasks(tasks, "63/37").map { it.id })
    assertEquals(listOf("t2"), TodoListEngine.filterTasks(tasks, "today").map { it.id })
    assertTrue(TodoListEngine.filterTasks(tasks, "nonexistent").isEmpty())
  }

  @Test
  fun engine_bulkActions_completeUncompleteScheduleAndDeleteMultipleTasks() {
    val t1 = TodoItem(id = "t1", title = "Task 1", subtasks = listOf(TodoSubTask(title = "Sub 1")))
    val t2 = TodoItem(id = "t2", title = "Task 2")
    val t3 = TodoItem(id = "t3", title = "Task 3")
    val initial = TodoHistorySnapshot(tasks = listOf(t1, t2, t3), completedToday = 0)

    // Bulk schedule t1 & t2
    val scheduledList = TodoListEngine.scheduleTasks(initial.tasks, setOf("t1", "t2"), 1_800_000_000_000L)
    assertTrue(scheduledList.first { it.id == "t1" }.isScheduled)
    assertTrue(scheduledList.first { it.id == "t2" }.isScheduled)
    assertFalse(scheduledList.first { it.id == "t3" }.isScheduled)

    // Bulk unschedule t1 & t2
    val unscheduledList = TodoListEngine.unscheduleTasks(scheduledList, setOf("t1", "t2"))
    assertFalse(unscheduledList.first { it.id == "t1" }.isScheduled)
    assertFalse(unscheduledList.first { it.id == "t2" }.isScheduled)

    // Bulk complete t1 & t2
    val afterBulkComplete = TodoListEngine.completeTasks(initial, setOf("t1", "t2"))
    assertEquals(listOf("t3"), afterBulkComplete.tasks.map { it.id })
    assertEquals(2, afterBulkComplete.completedToday)
    assertEquals(listOf("t1", "t2"), afterBulkComplete.completedTasks.map { it.id })
    assertTrue(afterBulkComplete.completedTasks.first().subtasks.first().isCompleted)

    // Bulk uncomplete t1
    val afterBulkUncomplete = TodoListEngine.uncompleteTasks(afterBulkComplete, setOf("t1"))
    assertEquals(listOf("t1", "t3"), afterBulkUncomplete.tasks.map { it.id })
    assertEquals(1, afterBulkUncomplete.completedToday)
    assertEquals(listOf("t2"), afterBulkUncomplete.completedTasks.map { it.id })

    // Bulk delete active t1 & t3
    val afterBulkDeleteActive = TodoListEngine.deleteTasks(afterBulkUncomplete, setOf("t1", "t3"))
    assertTrue(afterBulkDeleteActive.tasks.isEmpty())

    // Bulk delete completed t2
    val afterBulkDeleteCompleted = TodoListEngine.deleteCompletedTasks(afterBulkDeleteActive, setOf("t2"))
    assertTrue(afterBulkDeleteCompleted.completedTasks.isEmpty())
    assertEquals(0, afterBulkDeleteCompleted.completedToday)
  }

  @Test
  fun engine_getMainViewTasks_hidesScheduledTasksMoreThan24HoursInFuture() {
    val now = 1_700_000_000_000L
    val unscheduled = TodoItem(id = "unsched", title = "Unscheduled task")
    val overdue = TodoItem(id = "overdue", title = "Overdue task", scheduledAtMillis = now - 3_600_000L)
    val within24h = TodoItem(
      id = "within_24h",
      title = "In 12 hours",
      scheduledAtMillis = now + 12L * 3_600_000L,
    )
    val exact24h = TodoItem(
      id = "exact_24h",
      title = "In exactly 24 hours",
      scheduledAtMillis = now + MAIN_TASKS_SCHEDULE_HORIZON_MILLIS,
    )
    val beyond24h = TodoItem(
      id = "beyond_24h",
      title = "In 24 hours + 1 ms",
      scheduledAtMillis = now + MAIN_TASKS_SCHEDULE_HORIZON_MILLIS + 1L,
    )
    val nextWeek = TodoItem(
      id = "next_week",
      title = "Next week",
      scheduledAtMillis = now + 7L * MAIN_TASKS_SCHEDULE_HORIZON_MILLIS,
    )

    val allTasks = listOf(unscheduled, overdue, within24h, exact24h, beyond24h, nextWeek)

    // Main tasks view only includes unscheduled and tasks scheduled <= 24 hours in the future
    val mainTasks = TodoListEngine.getMainViewTasks(allTasks, nowMillis = now)
    assertEquals(
      listOf("unsched", "overdue", "within_24h", "exact_24h"),
      mainTasks.map { it.id },
    )

    assertFalse(exact24h.isScheduledBeyondHorizon(now))
    assertTrue(exact24h.isVisibleInMainTasksView(now))
    assertTrue(beyond24h.isScheduledBeyondHorizon(now))
    assertFalse(beyond24h.isVisibleInMainTasksView(now))

    // Once time advances so beyond24h is within 24 hours, it becomes visible in main tasks view
    val mainTasksLater = TodoListEngine.getMainViewTasks(allTasks, nowMillis = now + 1_000L)
    assertEquals(
      listOf("unsched", "overdue", "within_24h", "exact_24h", "beyond_24h"),
      mainTasksLater.map { it.id },
    )
  }

  @Test
  fun recurringTask_serializesAndReschedulesToPresetUponCompletion() {
    val now = 1_700_000_000_000L
    val threeDaysRule = TodoRecurrenceRule.fromPreset("3 days", Calendar.DAY_OF_YEAR, 3)
    val recurringTask = TodoItem(
      id = "rec-1",
      title = "Water studio plants",
      subtasks = listOf(TodoSubTask(id = "sub-1", title = "Check soil", isCompleted = true)),
      scheduledAtMillis = now - 60_000L,
      notifiedAtScheduleMillis = now - 60_000L,
      recurrenceRule = threeDaysRule,
    )

    // Verify JSON round-trip preserves recurrenceRule
    val restored = TodoItem.fromJson(recurringTask.toJson())
    assertEquals(threeDaysRule, restored.recurrenceRule)
    assertTrue(restored.isRecurring)

    val initialSnapshot = TodoHistorySnapshot(tasks = listOf(recurringTask), completedToday = 0)
    val afterComplete = TodoListEngine.completeTask(initialSnapshot, "rec-1", nowMillis = now)

    // Recurring task stays in active tasks, rescheduled for 3 days from completion time
    assertEquals(1, afterComplete.tasks.size)
    val rescheduled = afterComplete.tasks.first()
    assertEquals("rec-1", rescheduled.id)
    assertEquals(threeDaysRule.nextScheduledAtMillis(now), rescheduled.scheduledAtMillis)
    assertEquals(null, rescheduled.notifiedAtScheduleMillis)
    assertFalse(rescheduled.subtasks.first().isCompleted)

    // Completed occurrence is logged in completedTasks and increments completedToday
    assertEquals(1, afterComplete.completedToday)
    assertEquals(1, afterComplete.completedTasks.size)
    assertTrue(afterComplete.completedTasks.first().subtasks.first().isCompleted)

    // Because 3 days > 24 hours, it is hidden from main tasks view and only in scheduled view
    assertTrue(TodoListEngine.getMainViewTasks(afterComplete.tasks, nowMillis = now).isEmpty())
    assertTrue(rescheduled.isScheduled)
  }

  @Test
  fun promoteSubTaskToMainTask_movesSubtaskIntoMainList_andSupportsUndoRedo() {
    val sub1 = TodoSubTask(id = "sub-1", title = "Replace soldering tip")
    val sub2 = TodoSubTask(id = "sub-2", title = "Clean flux residue")
    val parent = TodoItem(
      id = "task-1",
      title = "Workbench cleanup",
      subtasks = listOf(sub1, sub2),
    )
    val secondTask = TodoItem(id = "task-2", title = "Order heatshrink")
    val history = TodoHistoryManager(initialTasks = listOf(parent, secondTask))

    // Promote sub1 to main task list
    val afterPromote = history.promoteSubTaskToMainTask("task-1", "sub-1")
    assertEquals(listOf("task-1", "sub-1", "task-2"), afterPromote.tasks.map { it.id })
    assertEquals("Replace soldering tip", afterPromote.tasks[1].title)
    assertEquals(listOf("sub-2"), afterPromote.tasks[0].subtasks.map { it.id })
    assertTrue(history.canUndo)

    // Undo restores sub1 back into parent's subtasks and removes it from main task list
    val afterUndo = history.undo()
    assertNotNull(afterUndo)
    assertEquals(listOf("task-1", "task-2"), afterUndo!!.tasks.map { it.id })
    assertEquals(listOf("sub-1", "sub-2"), afterUndo.tasks[0].subtasks.map { it.id })
    assertTrue(history.canRedo)

    // Redo re-promotes sub1 back into the main task list
    val afterRedo = history.redo()
    assertNotNull(afterRedo)
    assertEquals(listOf("task-1", "sub-1", "task-2"), afterRedo!!.tasks.map { it.id })
    assertEquals(listOf("sub-2"), afterRedo.tasks[0].subtasks.map { it.id })
  }

  @Test
  fun todoListWidgets_resolvesStateCompletesTasksWithUndoToastAndOpensTodoQuickActions() {
    assertEquals(16f, TodoListWidgets.WIDGET_CORNER_RADIUS_DP, 0.001f)
    assertEquals(16f, MusicPlayerWidgets.MINI_BAR_CORNER_RADIUS_DP, 0.001f)
    assertEquals(72f, MusicPlayerWidgets.MINI_BAR_HEIGHT_DP, 0.001f)
    assertEquals(16f, MusicPlayerWidgets.MEDIUM_WIDGET_CORNER_RADIUS_DP, 0.001f)
    assertEquals(112f, MusicPlayerWidgets.MEDIUM_WIDGET_HEIGHT_DP, 0.001f)
    assertEquals(16f, MusicPlayerWidgets.ALBUM_WIDGET_CORNER_RADIUS_DP, 0.001f)
    assertEquals(
      MusicWidgetViewStyle.MINI_BAR,
      MusicPlayerWidgets.resolveWidgetViewStyleForSize(280f, 72f),
    )
    assertEquals(
      MusicWidgetViewStyle.MEDIUM,
      MusicPlayerWidgets.resolveWidgetViewStyleForSize(280f, 112f),
    )
    assertEquals(
      MusicWidgetViewStyle.MEDIUM,
      MusicPlayerWidgets.resolveWidgetViewStyleForSize(280f, 220f),
    )
    assertEquals(
      MusicWidgetViewStyle.ALBUM_ART,
      MusicPlayerWidgets.resolveWidgetViewStyleForSize(260f, 260f),
    )

    // Verify external system media left custom action replaces the star button in widget state
    val externalWithLeftAction = SystemMediaPlayingInfo(
      sourcePackage = "com.spotify.music",
      sourceAppName = "Spotify",
      title = "Starlight Drive",
      artist = "Nova Pulse",
      album = "Horizons",
      isExternalApp = true,
      isPlaying = true,
      durationMs = 210_000L,
      positionMs = 45_000L,
      positionUpdateElapsedRealtimeMs = 0L,
      playbackSpeed = 1f,
      artworkBitmap = null,
      palette = MusicPlayerEngine.resolveTrackPalette(null),
      customActions = listOf(
        SystemMediaCustomAction(
          action = "com.spotify.mobile.android.ui.widget.ACTION_TOGGLE_SHUFFLE_ON",
          label = "Shuffle On",
          iconResId = 0,
        ),
      ),
    )
    val externalStateWithLeft = MusicPlayerWidgets.resolveWidgetState(
      context = null,
      session = null,
      externalMedia = externalWithLeftAction,
      decodeArtwork = false,
    )
    assertTrue(externalStateWithLeft.isExternalMedia)
    assertTrue(externalStateWithLeft.isLeftControlVisible)
    assertTrue(externalStateWithLeft.isShuffleHighlighted)
    assertEquals("Shuffle On", externalStateWithLeft.shuffleContentDescription)

    val externalWithoutLeftAction = externalWithLeftAction.copy(customActions = emptyList())
    val externalStateWithoutLeft = MusicPlayerWidgets.resolveWidgetState(
      context = null,
      session = null,
      externalMedia = externalWithoutLeftAction,
      decodeArtwork = false,
    )
    assertTrue(externalStateWithoutLeft.isExternalMedia)
    assertFalse(externalStateWithoutLeft.isLeftControlVisible)
    TodoListWidgets.clearUndoRecord()
    val now = 1_760_000_000_000L
    val todayKey = getTodayDateKey(now)
    val task1 = TodoItem(
      id = "w-task-1",
      title = "Order bench power supply leads",
      notes = "Banana plug to alligator clip",
      subtasks = listOf(
        TodoSubTask(id = "sub-1", title = "Red 1m", isCompleted = true),
        TodoSubTask(id = "sub-2", title = "Black 1m", isCompleted = false),
      ),
    )
    val task2 = TodoItem(
      id = "w-task-2",
      title = "Calibrate oscilloscope probe",
      scheduledAtMillis = now + 2 * 3_600_000L, // Within 24h horizon -> visible in widget
    )
    val taskBeyondHorizon = TodoItem(
      id = "w-task-future",
      title = "Annual lab audit",
      scheduledAtMillis = now + 5 * MAIN_TASKS_SCHEDULE_HORIZON_MILLIS, // > 24h -> hidden from main widget list
    )
    val repo = InMemoryTodoListRepository(
      initialTasks = listOf(task1, task2, taskBeyondHorizon),
      completedDate = todayKey,
      completedCount = 1,
    )

    // 1. Initial widget state lists visible tasks, formats subtitles & subtask badges, and hides undo toast
    val initialState = TodoListWidgets.resolveWidgetState(
      context = null,
      repository = repo,
      nowMillis = now,
    )
    assertEquals(listOf("w-task-1", "w-task-2"), initialState.visibleTasks.map { it.id })
    assertEquals(2, initialState.totalVisibleCount)
    assertEquals(1, initialState.completedTodayCount)
    assertEquals("2 tasks • 1 completed today", initialState.headerSubtitle)
    assertFalse(initialState.isUndoToastVisible)
    assertNull(initialState.pendingUndoRecord)
    assertEquals("1/2", TodoListWidgets.formatSubtaskBadgeText(task1))
    assertNull(TodoListWidgets.formatSubtaskBadgeText(task2))
    assertTrue(TodoListWidgets.buildTaskItemSubtitle(task1, now).contains("Banana plug to alligator clip"))

    // 2. Completing a task via check circle moves it to completed, increments today count, and activates undo toast
    val afterComplete = TodoListWidgets.completeTaskFromWidget(
      context = null,
      taskId = "w-task-1",
      repository = repo,
      nowMillis = now,
      showSystemToast = false,
    )
    assertNotNull(afterComplete)
    val stateAfterComplete = TodoListWidgets.resolveWidgetState(
      context = null,
      repository = repo,
      nowMillis = now + 1_000L,
    )
    assertEquals(listOf("w-task-2"), stateAfterComplete.visibleTasks.map { it.id })
    assertEquals(2, stateAfterComplete.completedTodayCount)
    assertEquals("1 task • 2 completed today", stateAfterComplete.headerSubtitle)
    assertTrue(stateAfterComplete.isUndoToastVisible)
    assertEquals("Completed \"Order bench power supply leads\"", stateAfterComplete.undoToastMessage)
    assertNotNull(stateAfterComplete.pendingUndoRecord)

    // Verify TodoWidgetUndoRecord JSON round-trip
    val undoJson = stateAfterComplete.pendingUndoRecord!!.toJson()
    val restoredUndoRecord = TodoWidgetUndoRecord.fromJson(undoJson)
    assertNotNull(restoredUndoRecord)
    assertEquals("w-task-1", restoredUndoRecord!!.completedTaskId)
    assertEquals(3, restoredUndoRecord.preCompletionSnapshot.tasks.size)

    // 3. Undoing task completion from the widget toast restores the task & reverts the daily completion counter
    val afterUndo = TodoListWidgets.undoLastCompletedTaskFromWidget(
      context = null,
      repository = repo,
      nowMillis = now + 2_000L,
      showSystemToast = false,
    )
    assertNotNull(afterUndo)
    val stateAfterUndo = TodoListWidgets.resolveWidgetState(
      context = null,
      repository = repo,
      nowMillis = now + 2_500L,
    )
    assertEquals(listOf("w-task-1", "w-task-2"), stateAfterUndo.visibleTasks.map { it.id })
    assertEquals(1, stateAfterUndo.completedTodayCount)
    assertFalse(stateAfterUndo.isUndoToastVisible)
    assertNull(stateAfterUndo.pendingUndoRecord)

    // 4. Undo toast automatically expires after UNDO_TOAST_DURATION_MS
    TodoListWidgets.completeTaskFromWidget(
      context = null,
      taskId = "w-task-2",
      repository = repo,
      nowMillis = now,
      showSystemToast = false,
    )
    val expiredState = TodoListWidgets.resolveWidgetState(
      context = null,
      repository = repo,
      nowMillis = now + TodoListWidgets.UNDO_TOAST_DURATION_MS + 500L,
    )
    assertFalse(expiredState.isUndoToastVisible)
    assertNull(expiredState.pendingUndoRecord)

    // 5. Plus (+) and Schedule FAB actions request opening To-Do tool with Add a task focused or Schedule view opened
    TodoListEngine.consumePendingQuickActionRequest()
    assertNull(TodoListEngine.pendingQuickActionRequest)

    TodoListEngine.requestQuickAction(openSchedule = false)
    val addTaskRequest = TodoListEngine.consumePendingQuickActionRequest()
    assertNotNull(addTaskRequest)
    assertFalse(addTaskRequest!!.openSchedule)
    assertNull(TodoListEngine.pendingQuickActionRequest)

    TodoListEngine.requestQuickAction(openSchedule = true)
    val scheduleRequest = TodoListEngine.consumePendingQuickActionRequest()
    assertNotNull(scheduleRequest)
    assertTrue(scheduleRequest!!.openSchedule)
    assertNull(TodoListEngine.pendingQuickActionRequest)
  }

  @Test
  fun todoList_savesAndLoadsTtxtFormat_withYamlNestedSubtaskLists() {
    val activeTask1 = TodoItem(
      id = "t1",
      title = "Prepare electronics kit",
      subtasks = listOf(
        TodoSubTask(id = "s1", title = "Pack multimeter", isCompleted = true),
        TodoSubTask(
          id = "s2",
          title = "Charge soldering iron",
          isCompleted = false,
          subtasks = listOf(
            TodoSubTask(id = "s2-1", title = "Find USB-C PD cable", isCompleted = false),
            TodoSubTask(id = "s2-2", title = "Inspect chisel tip", isCompleted = true),
          ),
        ),
      ),
    )
    val activeTask2 = TodoItem(
      id = "t2",
      title = "Order replacement fuses",
    )
    val completedTask = TodoItem(
      id = "c1",
      title = "Clean workbench",
      subtasks = listOf(
        TodoSubTask(id = "cs1", title = "Wipe ESD mat", isCompleted = true),
      ),
    )

    // 1. Single task .text serialization uses tab indentation ('\t') for nested lists with '-' and '+'
    val expectedTask1Ttxt = listOf(
      "- Prepare electronics kit",
      "\t+ Pack multimeter",
      "\t- Charge soldering iron",
      "\t\t- Find USB-C PD cable",
      "\t\t+ Inspect chisel tip",
    ).joinToString("\n")
    assertEquals(expectedTask1Ttxt, activeTask1.toTtxt())

    // 2. Full To Do list .text document includes '# To Do', active '-' tasks, completed '+' tasks, and tab-nested lists
    val expectedFullTtxt = listOf(
      "# To Do",
      "",
      "- Prepare electronics kit",
      "\t+ Pack multimeter",
      "\t- Charge soldering iron",
      "\t\t- Find USB-C PD cable",
      "\t\t+ Inspect chisel tip",
      "- Order replacement fuses",
      "+ Clean workbench",
      "\t+ Wipe ESD mat",
    ).joinToString("\n")
    assertEquals(
      expectedFullTtxt,
      TodoListEngine.formatTodoListToTtxt(
        tasks = listOf(activeTask1, activeTask2),
        completedTasks = listOf(completedTask),
      ),
    )

    // 3. Repository saves and loads .text file on disk, preserving multi-level tab list nesting
    val tempDir = java.nio.file.Files.createTempDirectory("todo_text_test").toFile()
    val todoTtxtFile = java.io.File(tempDir, TODO_LIST_TTXT_FILE_NAME)
    try {
      val repo = InMemoryTodoListRepository(
        initialTasks = listOf(activeTask1, activeTask2),
        initialCompletedTasks = listOf(completedTask),
        completedCount = 1,
        todoFile = todoTtxtFile,
      )
      assertTrue(todoTtxtFile.exists())
      assertEquals(expectedFullTtxt, todoTtxtFile.readText())
      assertEquals(expectedFullTtxt, repo.toTtxt())

      // Toggle deeply nested subtask 's2-1' ("Find USB-C PD cable") -> '-' becomes '+' at indent level 2 (2 tabs)
      val afterNestedToggle = TodoListEngine.toggleSubTask(repo.loadTasks(), taskId = "t1", subtaskId = "s2-1")
      repo.saveTasks(afterNestedToggle)

      val expectedAfterToggle = listOf(
        "# To Do",
        "",
        "- Prepare electronics kit",
        "\t+ Pack multimeter",
        "\t- Charge soldering iron",
        "\t\t+ Find USB-C PD cable",
        "\t\t+ Inspect chisel tip",
        "- Order replacement fuses",
        "+ Clean workbench",
        "\t+ Wipe ESD mat",
      ).joinToString("\n")
      assertEquals(expectedAfterToggle, todoTtxtFile.readText())

      // Load into a fresh repository from the .text file on disk
      val freshRepo = InMemoryTodoListRepository(todoFile = todoTtxtFile)
      val loadedActive = freshRepo.loadTasks()
      val loadedCompleted = freshRepo.loadCompletedTasks()

      assertEquals(2, loadedActive.size)
      assertEquals("Prepare electronics kit", loadedActive[0].title)
      assertEquals(2, loadedActive[0].subtasks.size)
      assertTrue(loadedActive[0].subtasks[0].isCompleted)
      assertEquals("Pack multimeter", loadedActive[0].subtasks[0].title)
      assertFalse(loadedActive[0].subtasks[1].isCompleted)
      assertEquals("Charge soldering iron", loadedActive[0].subtasks[1].title)
      assertEquals(2, loadedActive[0].subtasks[1].subtasks.size)
      assertTrue(loadedActive[0].subtasks[1].subtasks[0].isCompleted)
      assertEquals("Find USB-C PD cable", loadedActive[0].subtasks[1].subtasks[0].title)
      assertTrue(loadedActive[0].subtasks[1].subtasks[1].isCompleted)
      assertEquals("Inspect chisel tip", loadedActive[0].subtasks[1].subtasks[1].title)

      assertEquals(1, loadedCompleted.size)
      assertEquals("Clean workbench", loadedCompleted[0].title)
      assertEquals(1, loadedCompleted[0].subtasks.size)
      assertTrue(loadedCompleted[0].subtasks[0].isCompleted)
      assertEquals("Wipe ESD mat", loadedCompleted[0].subtasks[0].title)
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun engine_calculateDragAutoScrollSpeed_scrollsNearVisibleEdgesAndHonorsBoundaries() {
    val viewportTop = 100f
    val viewportBottom = 700f
    val threshold = 60f
    val maxStep = 15f

    // 1. Pointer in middle of viewport -> 0f speed
    val centerSpeed = TodoListEngine.calculateDragAutoScrollSpeed(
      pointerY = 400f,
      viewportTop = viewportTop,
      viewportBottom = viewportBottom,
      edgeThresholdPx = threshold,
      maxScrollStepPx = maxStep,
      canScrollBackward = true,
      canScrollForward = true,
    )
    assertEquals(0f, centerSpeed, 0.001f)

    // 2. Pointer halfway into bottom edge zone (700 - 30 = 670) -> +0.5 * maxStep
    val bottomHalfSpeed = TodoListEngine.calculateDragAutoScrollSpeed(
      pointerY = 670f,
      viewportTop = viewportTop,
      viewportBottom = viewportBottom,
      edgeThresholdPx = threshold,
      maxScrollStepPx = maxStep,
      canScrollBackward = true,
      canScrollForward = true,
    )
    assertEquals(7.5f, bottomHalfSpeed, 0.001f)

    // 3. Pointer at or past bottom edge (710) -> clamped to +maxStep
    val bottomMaxSpeed = TodoListEngine.calculateDragAutoScrollSpeed(
      pointerY = 710f,
      viewportTop = viewportTop,
      viewportBottom = viewportBottom,
      edgeThresholdPx = threshold,
      maxScrollStepPx = maxStep,
      canScrollBackward = true,
      canScrollForward = true,
    )
    assertEquals(15f, bottomMaxSpeed, 0.001f)

    // 4. Pointer halfway into top edge zone (100 + 30 = 130) -> -0.5 * maxStep
    val topHalfSpeed = TodoListEngine.calculateDragAutoScrollSpeed(
      pointerY = 130f,
      viewportTop = viewportTop,
      viewportBottom = viewportBottom,
      edgeThresholdPx = threshold,
      maxScrollStepPx = maxStep,
      canScrollBackward = true,
      canScrollForward = true,
    )
    assertEquals(-7.5f, topHalfSpeed, 0.001f)

    // 5. No auto-scroll when container cannot scroll in that direction or item is already at end of section
    assertEquals(
      0f,
      TodoListEngine.calculateDragAutoScrollSpeed(
        pointerY = 685f,
        viewportTop = viewportTop,
        viewportBottom = viewportBottom,
        edgeThresholdPx = threshold,
        maxScrollStepPx = maxStep,
        canScrollBackward = true,
        canScrollForward = false,
      ),
      0.001f,
    )
    assertEquals(
      0f,
      TodoListEngine.calculateDragAutoScrollSpeed(
        pointerY = 685f,
        viewportTop = viewportTop,
        viewportBottom = viewportBottom,
        edgeThresholdPx = threshold,
        maxScrollStepPx = maxStep,
        canScrollBackward = true,
        canScrollForward = true,
        canMoveItemForward = false,
      ),
      0.001f,
    )
    assertEquals(
      0f,
      TodoListEngine.calculateDragAutoScrollSpeed(
        pointerY = 115f,
        viewportTop = viewportTop,
        viewportBottom = viewportBottom,
        edgeThresholdPx = threshold,
        maxScrollStepPx = maxStep,
        canScrollBackward = true,
        canScrollForward = true,
        canMoveItemBackward = false,
      ),
      0.001f,
    )

    // 6. Starting inside top edge zone (topEdgeTriggerY = 120) and dragging down to 135 does NOT scroll up,
    //    while dragging up to 110 DOES scroll up
    assertEquals(
      0f,
      TodoListEngine.calculateDragAutoScrollSpeed(
        pointerY = 135f,
        viewportTop = viewportTop,
        viewportBottom = viewportBottom,
        edgeThresholdPx = threshold,
        maxScrollStepPx = maxStep,
        canScrollBackward = true,
        canScrollForward = true,
        topEdgeTriggerY = 120f,
      ),
      0.001f,
    )
    assertEquals(
      -7.5f,
      TodoListEngine.calculateDragAutoScrollSpeed(
        pointerY = 110f,
        viewportTop = viewportTop,
        viewportBottom = viewportBottom,
        edgeThresholdPx = threshold,
        maxScrollStepPx = maxStep,
        canScrollBackward = true,
        canScrollForward = true,
        topEdgeTriggerY = 120f,
      ),
      0.001f,
    )
  }
}
