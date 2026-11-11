package com.example.toolbox.ui.tools

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class NotesTest {

  @Test
  fun saveOrDiscardNote_discardsEmptyNewNote_andSavesValidNote() {
    val initial = emptyList<NoteItem>()

    // Empty note is discarded
    val emptyDraft = NoteItem(title = "   ", content = "   ")
    val afterEmpty = NotesEngine.saveOrDiscardNote(initial, emptyDraft, nowMs = 1000L)
    assertTrue(afterEmpty.isEmpty())

    // Empty checklist is also discarded
    val emptyChecklist = NoteItem(
      title = "",
      isChecklist = true,
      checklistItems = listOf(NoteChecklistItem(text = "   ")),
    )
    val afterEmptyChecklist = NotesEngine.saveOrDiscardNote(initial, emptyChecklist, nowMs = 1000L)
    assertTrue(afterEmptyChecklist.isEmpty())

    // Valid text note is added at the top
    val validNote = NoteItem(
      id = "n1",
      title = " Grocery List ",
      content = "Apples\nBananas",
      color = NoteColor.MINT,
      labels = listOf(" shopping ", "shopping", ""),
    )
    val afterValid = NotesEngine.saveOrDiscardNote(initial, validNote, nowMs = 2000L)
    assertEquals(1, afterValid.size)
    assertEquals("Grocery List", afterValid[0].title)
    assertEquals("Apples\nBananas", afterValid[0].content)
    assertEquals(NoteColor.MINT, afterValid[0].color)
    assertEquals(listOf("shopping"), afterValid[0].labels)
    assertEquals(2000L, afterValid[0].updatedAt)

    // Updating an existing note to be completely empty removes it
    val clearedNote = afterValid[0].copy(title = "", content = "")
    val afterCleared = NotesEngine.saveOrDiscardNote(afterValid, clearedNote, nowMs = 3000L)
    assertTrue(afterCleared.isEmpty())
  }

  @Test
  fun toggleChecklistMode_convertsPlainTextLinesToChecklistAndBack() {
    val textNote = NoteItem(
      id = "n1",
      title = "Packing",
      content = "- Tent\n[x] Sleeping bag\n[ ] Lantern\nWater filter",
      isChecklist = false,
    )

    // Convert to checklist
    val checklistNote = NotesEngine.toggleChecklistMode(textNote)
    assertTrue(checklistNote.isChecklist)
    assertEquals("", checklistNote.content)
    assertEquals(4, checklistNote.checklistItems.size)
    assertEquals("Tent", checklistNote.checklistItems[0].text)
    assertFalse(checklistNote.checklistItems[0].isChecked)
    assertEquals("Sleeping bag", checklistNote.checklistItems[1].text)
    assertTrue(checklistNote.checklistItems[1].isChecked)
    assertEquals("Lantern", checklistNote.checklistItems[2].text)
    assertFalse(checklistNote.checklistItems[2].isChecked)
    assertEquals("Water filter", checklistNote.checklistItems[3].text)
    assertFalse(checklistNote.checklistItems[3].isChecked)

    // Convert back to plain text
    val backToText = NotesEngine.toggleChecklistMode(checklistNote)
    assertFalse(backToText.isChecklist)
    assertTrue(backToText.checklistItems.isEmpty())
    assertEquals("Tent\nSleeping bag\nLantern\nWater filter", backToText.content)
  }

  @Test
  fun checklistOperations_toggleUncheckAllAndDeleteChecked() {
    val note = NoteItem(
      id = "n1",
      title = "Chores",
      isChecklist = true,
      checklistItems = listOf(
        NoteChecklistItem(id = "c1", text = "Dishes", isChecked = false),
        NoteChecklistItem(id = "c2", text = "Laundry", isChecked = true),
        NoteChecklistItem(id = "c3", text = "Vacuum", isChecked = true),
      ),
    )

    // Toggle c1 to checked
    val afterToggle = NotesEngine.toggleChecklistItem(listOf(note), "n1", "c1", nowMs = 5000L)[0]
    assertTrue(afterToggle.checklistItems.first { it.id == "c1" }.isChecked)
    assertEquals(3, afterToggle.checkedItems.size)
    assertEquals(0, afterToggle.uncheckedItems.size)

    // Uncheck all
    val uncheckedAll = NotesEngine.uncheckAllItems(afterToggle)
    assertEquals(0, uncheckedAll.checkedItems.size)
    assertEquals(3, uncheckedAll.uncheckedItems.size)

    // Delete checked on original note (removes c2 and c3, keeps c1)
    val deletedChecked = NotesEngine.deleteCheckedItems(note)
    assertEquals(1, deletedChecked.checklistItems.size)
    assertEquals("Dishes", deletedChecked.checklistItems[0].text)
  }

  @Test
  fun pinAndArchive_mutuallyExclusiveTransitionsWorkCorrectly() {
    val note = NoteItem(id = "n1", title = "Important", isPinned = true, isArchived = false)
    val list = listOf(note)

    // Archiving a pinned note unpins it
    val archived = NotesEngine.toggleArchive(list, "n1", nowMs = 100L)
    assertTrue(archived[0].isArchived)
    assertFalse(archived[0].isPinned)

    // Pinning an archived note unarchives it
    val repinned = NotesEngine.togglePin(archived, "n1", nowMs = 200L)
    assertTrue(repinned[0].isPinned)
    assertFalse(repinned[0].isArchived)
  }

  @Test
  fun trashRestoreAndEmptyTrash_lifecycleWorksCorrectly() {
    val n1 = NoteItem(id = "n1", title = "Keep", isPinned = true)
    val n2 = NoteItem(id = "n2", title = "Trash Me", isPinned = true)
    val initial = listOf(n1, n2)

    // Move n2 to trash -> unpins it and marks isTrashed = true
    val trashed = NotesEngine.moveToTrash(initial, setOf("n2"), nowMs = 1000L)
    val trashedN2 = trashed.first { it.id == "n2" }
    assertTrue(trashedN2.isTrashed)
    assertFalse(trashedN2.isPinned)

    // Restore n2 from trash
    val restored = NotesEngine.restoreFromTrash(trashed, setOf("n2"), nowMs = 2000L)
    assertFalse(restored.first { it.id == "n2" }.isTrashed)

    // Move n2 to trash again and empty trash
    val trashedAgain = NotesEngine.moveToTrash(restored, setOf("n2"), nowMs = 3000L)
    val emptied = NotesEngine.emptyTrash(trashedAgain)
    assertEquals(1, emptied.size)
    assertEquals("n1", emptied[0].id)
  }

  @Test
  fun filterNotes_partitionsPinnedAndOthers_andSearchesByTitleBodyChecklistAndLabels() {
    val notes = listOf(
      NoteItem(
        id = "1",
        title = "Pinned Recipe",
        content = "Sourdough bread flour",
        color = NoteColor.SAND,
        isPinned = true,
        labels = listOf("cooking"),
        updatedAt = 100L,
      ),
      NoteItem(
        id = "2",
        title = "Hardware Store",
        isChecklist = true,
        checklistItems = listOf(NoteChecklistItem(text = "Wood screws")),
        color = NoteColor.SAGE,
        isPinned = false,
        labels = listOf("diy"),
        updatedAt = 300L,
      ),
      NoteItem(
        id = "3",
        title = "Old Receipt",
        content = "Archived tax info",
        isArchived = true,
        updatedAt = 200L,
      ),
      NoteItem(
        id = "4",
        title = "Deleted Draft",
        content = "Trashed content",
        isTrashed = true,
        updatedAt = 400L,
      ),
    )

    // 1. All filter excludes archived and trashed, separates pinned and others
    val allResult = NotesEngine.filterNotes(notes, filter = NotesFilter.All)
    assertEquals(1, allResult.pinned.size)
    assertEquals("1", allResult.pinned[0].id)
    assertEquals(1, allResult.others.size)
    assertEquals("2", allResult.others[0].id)

    // 2. Checklists filter
    val checklistResult = NotesEngine.filterNotes(notes, filter = NotesFilter.Checklists)
    assertEquals(1, checklistResult.totalCount)
    assertEquals("2", checklistResult.others[0].id)

    // 3. Label filter
    val labelResult = NotesEngine.filterNotes(notes, filter = NotesFilter.Label("COOKING"))
    assertEquals(1, labelResult.totalCount)
    assertEquals("1", labelResult.pinned[0].id)

    // 4. Search query matching checklist item text
    val searchScrew = NotesEngine.filterNotes(notes, filter = NotesFilter.All, searchQuery = "screws")
    assertEquals(1, searchScrew.totalCount)
    assertEquals("2", searchScrew.others[0].id)

    // 5. Color filter
    val sandOnly = NotesEngine.filterNotes(notes, filter = NotesFilter.All, colorFilter = NoteColor.SAND)
    assertEquals(1, sandOnly.totalCount)
    assertEquals("1", sandOnly.pinned[0].id)

    // 6. Archive & Trash filters
    val archivedResult = NotesEngine.filterNotes(notes, filter = NotesFilter.Archived)
    assertEquals(1, archivedResult.totalCount)
    assertEquals("3", archivedResult.others[0].id)

    val trashResult = NotesEngine.filterNotes(notes, filter = NotesFilter.Trash)
    assertEquals(1, trashResult.totalCount)
    assertEquals("4", trashResult.others[0].id)
  }

  @Test
  fun jsonSerialization_roundTripPreservesAllNoteFields() {
    val original = NoteItem(
      id = "note-42",
      title = "Project Ideas",
      content = "",
      isChecklist = true,
      checklistItems = listOf(
        NoteChecklistItem(id = "item-1", text = "Offline sync", isChecked = true),
        NoteChecklistItem(id = "item-2", text = "Color cards", isChecked = false),
      ),
      color = NoteColor.BLOSSOM,
      isPinned = true,
      isArchived = false,
      isTrashed = false,
      labels = listOf("ideas", "android"),
      createdAt = 123456789L,
      updatedAt = 987654321L,
    )

    val json = original.toJson()
    val restored = NoteItem.fromJson(json)
    assertEquals(original, restored)
  }

  @Test
  fun duplicateAndDeleteLabelGlobally_workCorrectly() {
    val original = NoteItem(
      id = "n1",
      title = "Template",
      content = "Body",
      labels = listOf("work", "urgent"),
    )
    val duplicated = NotesEngine.duplicateNote(listOf(original), "n1", nowMs = 5000L)
    assertEquals(2, duplicated.size)
    assertTrue(duplicated[0].id != original.id)
    assertEquals("Template", duplicated[0].title)

    val (afterLabelDelete, remainingLabels) = NotesEngine.deleteLabelGlobally(
      notes = duplicated,
      savedLabels = listOf("work", "personal"),
      labelToRemove = "WORK",
    )
    assertEquals(listOf("personal"), remainingLabels)
    assertTrue(afterLabelDelete.all { "work" !in it.labels })
    assertTrue(afterLabelDelete.all { "urgent" in it.labels })
  }
}
