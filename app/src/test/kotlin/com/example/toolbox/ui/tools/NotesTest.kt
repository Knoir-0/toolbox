package com.example.toolbox.ui.tools

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
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
      content = "- Tent\n\t- Rainfly\n[x] Sleeping bag\n[ ] Lantern\nWater filter",
      isChecklist = false,
    )

    // Convert to checklist (preserving tab indentation as nesting level)
    val checklistNote = NotesEngine.toggleChecklistMode(textNote)
    assertTrue(checklistNote.isChecklist)
    assertEquals("", checklistNote.content)
    assertEquals(5, checklistNote.checklistItems.size)
    assertEquals("Tent", checklistNote.checklistItems[0].text)
    assertFalse(checklistNote.checklistItems[0].isChecked)
    assertEquals(0, checklistNote.checklistItems[0].indentLevel)
    assertEquals("Rainfly", checklistNote.checklistItems[1].text)
    assertFalse(checklistNote.checklistItems[1].isChecked)
    assertEquals(1, checklistNote.checklistItems[1].indentLevel)
    assertEquals("Sleeping bag", checklistNote.checklistItems[2].text)
    assertTrue(checklistNote.checklistItems[2].isChecked)
    assertEquals("Lantern", checklistNote.checklistItems[3].text)
    assertFalse(checklistNote.checklistItems[3].isChecked)
    assertEquals("Water filter", checklistNote.checklistItems[4].text)
    assertFalse(checklistNote.checklistItems[4].isChecked)

    // Convert back to plain text (preserving tab indentation)
    val backToText = NotesEngine.toggleChecklistMode(checklistNote)
    assertFalse(backToText.isChecklist)
    assertTrue(backToText.checklistItems.isEmpty())
    assertEquals("Tent\n\tRainfly\nSleeping bag\nLantern\nWater filter", backToText.content)
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

    // Suggest checked items when typing in checklists
    val suggestions = NotesEngine.getCheckedItemSuggestions(note, "laun")
    assertEquals(listOf("Laundry"), suggestions.map { it.text })

    // Selecting a suggested checked item restores (unchecks) it
    val restoredFromNewInput = NotesEngine.applyCheckedItemSuggestion(note, "c2")
    assertEquals(2, restoredFromNewInput.uncheckedItems.size)
    assertEquals(listOf("Dishes", "Laundry"), restoredFromNewInput.uncheckedItems.map { it.text })
    assertEquals(listOf("Vacuum"), restoredFromNewInput.checkedItems.map { it.text })

    // Selecting a suggested checked item while editing an existing unchecked item replaces it
    val typingNote = note.copy(
      checklistItems = note.checklistItems + NoteChecklistItem(id = "c4", text = "Vac", isChecked = false),
    )
    val restoredInPlace = NotesEngine.applyCheckedItemSuggestion(
      note = typingNote,
      checkedItemId = "c3",
      replacingUncheckedItemId = "c4",
    )
    assertEquals(listOf("Dishes", "Vacuum"), restoredInPlace.uncheckedItems.map { it.text })
    assertEquals(listOf("Laundry"), restoredInPlace.checkedItems.map { it.text })

    // Pasting a list with return characters parses into multiple checklist items
    val parsedItems = NotesEngine.parseChecklistItems("Apples\nBananas\r\n\r\n- Cherries\n• Dates")
    assertEquals(listOf("Apples", "Bananas", "Cherries", "Dates"), parsedItems.map { it.text })
    assertTrue(parsedItems.all { !it.isChecked })

    // Pasting a multi-line list into an existing checklist item splits it in place
    val splitInPlace = NotesEngine.updateChecklistItemText(
      note = note,
      itemId = "c1",
      updatedText = "Dishes\nMop floor\nWipe counters",
    )
    assertEquals(
      listOf("Dishes", "Mop floor", "Wipe counters"),
      splitInPlace.uncheckedItems.map { it.text },
    )
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
    assertEquals(
      "# Project Ideas\n\n+ Offline sync\n- Color cards\n\n#ideas, #android",
      restored.toTtxt(),
    )
    assertEquals(
      "# Project Ideas\n\n+ Offline sync\n- Color cards\n\nideas, android",
      restored.toPlainText(),
    )
  }

  @Test
  fun textFormat_serializesAndParsesMarkdownNotesAndPlusMinusChecklists_andPersistsInNotesDirectory() {
    assertEquals("text", NOTES_FILE_EXTENSION)
    assertEquals(".text", NOTES_FILE_DOT_EXTENSION)
    assertEquals("Text", NOTES_FILE_FORMAT_NAME)
    assertEquals("/Notes", DEFAULT_NOTES_SCOPED_FOLDER.displayPath)

    // 1. Checklist note uses '- ' for unchecked items, '+ ' for checked items, tab indentation ('\t') for nested items,
    //    and '#Label 1, #Label 2' on the last line
    val checklistNote = NoteItem(
      id = "chk-42",
      title = "Camping Checklist",
      isChecklist = true,
      checklistItems = listOf(
        NoteChecklistItem(id = "c1", text = "Tent", isChecked = false, indentLevel = 0),
        NoteChecklistItem(id = "c1-sub", text = "Rainfly & stakes", isChecked = false, indentLevel = 1),
        NoteChecklistItem(id = "c1-deep", text = "Titanium pegs", isChecked = true, indentLevel = 2),
        NoteChecklistItem(id = "c2", text = "Sleeping bag", isChecked = true, indentLevel = 0),
        NoteChecklistItem(id = "c3", text = "Water filter", isChecked = false, indentLevel = 0),
      ),
      color = NoteColor.SAGE,
      isPinned = true,
      labels = listOf("outdoors", "weekend trip"),
    )

    val expectedChecklistTtxt = listOf(
      "# Camping Checklist",
      "",
      "- Tent",
      "\t- Rainfly & stakes",
      "\t\t+ Titanium pegs",
      "+ Sleeping bag",
      "- Water filter",
      "",
      "#outdoors, #weekend trip",
    ).joinToString("\n")
    assertEquals(expectedChecklistTtxt, checklistNote.toTtxt())

    val parsedChecklist = NoteItem.fromTtxt(expectedChecklistTtxt, id = "chk-42")
    assertEquals("Camping Checklist", parsedChecklist.title)
    assertTrue(parsedChecklist.isChecklist)
    assertEquals(5, parsedChecklist.checklistItems.size)
    assertEquals("Tent", parsedChecklist.checklistItems[0].text)
    assertFalse(parsedChecklist.checklistItems[0].isChecked)
    assertEquals(0, parsedChecklist.checklistItems[0].indentLevel)
    assertEquals('-', parsedChecklist.checklistItems[0].ttxtPrefix)
    assertEquals("Rainfly & stakes", parsedChecklist.checklistItems[1].text)
    assertFalse(parsedChecklist.checklistItems[1].isChecked)
    assertEquals(1, parsedChecklist.checklistItems[1].indentLevel)
    assertEquals("\t- Rainfly & stakes", parsedChecklist.checklistItems[1].toTtxtLine())
    assertEquals("Titanium pegs", parsedChecklist.checklistItems[2].text)
    assertTrue(parsedChecklist.checklistItems[2].isChecked)
    assertEquals(2, parsedChecklist.checklistItems[2].indentLevel)
    assertEquals("\t\t+ Titanium pegs", parsedChecklist.checklistItems[2].toTtxtLine())
    assertEquals("Sleeping bag", parsedChecklist.checklistItems[3].text)
    assertTrue(parsedChecklist.checklistItems[3].isChecked)
    assertEquals(0, parsedChecklist.checklistItems[3].indentLevel)
    assertEquals('+', parsedChecklist.checklistItems[3].ttxtPrefix)
    assertEquals("Water filter", parsedChecklist.checklistItems[4].text)
    assertFalse(parsedChecklist.checklistItems[4].isChecked)
    assertEquals(0, parsedChecklist.checklistItems[4].indentLevel)
    assertEquals(listOf("outdoors", "weekend trip"), parsedChecklist.labels)

    // 2. Markdown text note uses '# Title' and Markdown body
    val markdownNote = NoteItem(
      id = "md-1",
      title = "Architecture Notes",
      content = "Use **Material 3** tokens and `.text` files in `/Notes`.",
      isChecklist = false,
      color = NoteColor.SAND,
    )
    val expectedMarkdownTtxt = """
      # Architecture Notes

      Use **Material 3** tokens and `.text` files in `/Notes`.
    """.trimIndent()
    assertEquals(expectedMarkdownTtxt, markdownNote.toTtxt())

    val parsedMarkdown = NoteItem.fromTtxt(expectedMarkdownTtxt, id = "md-1")
    assertEquals("Architecture Notes", parsedMarkdown.title)
    assertFalse(parsedMarkdown.isChecklist)
    assertEquals("Use **Material 3** tokens and `.text` files in `/Notes`.", parsedMarkdown.content)
    assertTrue(parsedMarkdown.labels.isEmpty())

    // 3. Saving and loading .text files inside a /Notes directory
    val tempNotesDir = java.nio.file.Files.createTempDirectory("notes_text_test").toFile()
    try {
      val repo = InMemoryNotesRepository(notesDirectory = tempNotesDir)
      repo.saveNotes(listOf(checklistNote, markdownNote))

      val campingFile = java.io.File(tempNotesDir, "Camping Checklist.text")
      val archFile = java.io.File(tempNotesDir, "Architecture Notes.text")
      assertTrue(campingFile.exists())
      assertTrue(archFile.exists())
      assertEquals(expectedChecklistTtxt, campingFile.readText())
      assertEquals(expectedMarkdownTtxt, archFile.readText())

      // Checking 'Tent' replaces '-' with '+' in the .text file on disk
      val afterCheck = NotesEngine.toggleChecklistItem(repo.loadNotes(), "chk-42", "c1")
      repo.saveNotes(afterCheck)
      assertEquals(
        listOf(
          "# Camping Checklist",
          "",
          "+ Tent",
          "\t- Rainfly & stakes",
          "\t\t+ Titanium pegs",
          "+ Sleeping bag",
          "- Water filter",
          "",
          "#outdoors, #weekend trip",
        ).joinToString("\n"),
        campingFile.readText(),
      )

      // Dropping an external .text file into /Notes loads it automatically (including tab-nested checklist items and #Label CSV line)
      val externalFile = java.io.File(tempNotesDir, "Quick Groceries.text")
      externalFile.writeText("# Quick Groceries\n\n- Produce\n\t- Apples\n\t+ Spinach\n+ Coffee\n\n#Groceries, #Meal Prep")

      val reloaded = repo.loadNotes()
      assertEquals(3, reloaded.size)
      val imported = reloaded.first { it.title == "Quick Groceries" }
      assertTrue(imported.isChecklist)
      assertEquals(
        listOf(
          Triple("Produce", false, 0),
          Triple("Apples", false, 1),
          Triple("Spinach", true, 1),
          Triple("Coffee", true, 0),
        ),
        imported.checklistItems.map { Triple(it.text, it.isChecked, it.indentLevel) },
      )
      assertEquals(listOf("Groceries", "Meal Prep"), imported.labels)
    } finally {
      tempNotesDir.deleteRecursively()
    }
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

    assertEquals(2, NotesEngine.getNotesUsingLabel(duplicated, "work").size)
    assertEquals(0, NotesEngine.getNotesUsingLabel(duplicated, "personal").size)

    val (afterLabelDelete, remainingLabels) = NotesEngine.deleteLabelGlobally(
      notes = duplicated,
      savedLabels = listOf("work", "personal"),
      labelToRemove = "WORK",
    )
    assertEquals(listOf("personal"), remainingLabels)
    assertEquals(0, NotesEngine.getNotesUsingLabel(afterLabelDelete, "work").size)
    assertTrue(afterLabelDelete.all { "work" !in it.labels })
    assertTrue(afterLabelDelete.all { "urgent" in it.labels })
  }

  @Test
  fun reorderNotes_rearrangesNotesAndFilterPreservesManualArrangement() {
    val n1 = NoteItem(id = "n1", title = "First", updatedAt = 100L)
    val n2 = NoteItem(id = "n2", title = "Second", updatedAt = 300L)
    val n3 = NoteItem(id = "n3", title = "Third", updatedAt = 200L)
    val initial = listOf(n1, n2, n3)

    // Move n1 to n3's position
    val reordered = NotesEngine.reorderNotes(initial, fromNoteId = "n1", toNoteId = "n3")
    assertEquals(listOf("n2", "n3", "n1"), reordered.map { it.id })

    // filterNotes preserves manual arrangement rather than overriding with updatedAt
    val filtered = NotesEngine.filterNotes(reordered, filter = NotesFilter.All)
    assertEquals(listOf("n2", "n3", "n1"), filtered.others.map { it.id })

    // Move n1 back to the top
    val movedBack = NotesEngine.reorderNotes(reordered, fromNoteId = "n1", toNoteId = "n2")
    assertEquals(listOf("n1", "n2", "n3"), movedBack.map { it.id })
  }

  @Test
  fun findTargetNoteId_resolvesTargetsInBothGridAndListViewsWithoutOscillation() {
    val n1 = NoteItem(id = "n1", title = "Short Note")
    val n2 = NoteItem(id = "n2", title = "Tall Note")
    val section = listOf(n1, n2)

    // 1. List view (1 column) with variable heights (n1 = 100px, n2 = 300px)
    val listBounds = mutableMapOf(
      "n1" to Rect(0f, 0f, 400f, 100f),
      "n2" to Rect(0f, 110f, 400f, 410f),
    )

    // Dragging n1 down to n2's center (y = 260f) triggers swap
    val targetInList = findTargetNoteId(
      draggedNoteId = "n1",
      point = Offset(200f, 260f),
      sectionNotes = section,
      cardBounds = listBounds,
      isGridView = false,
    )
    assertEquals("n2", targetInList)

    val reorderedList = listOf(n2, n1)
    updateSectionCardBoundsOptimistically(
      oldSectionNotes = section,
      newSectionNotes = reorderedList,
      cardBounds = listBounds,
      isGridView = false,
      verticalSpacingPx = 10f,
    )

    // Holding finger at y = 260f after swap does NOT oscillate back
    val afterSwapInList = findTargetNoteId(
      draggedNoteId = "n1",
      point = Offset(200f, 260f),
      sectionNotes = reorderedList,
      cardBounds = listBounds,
      isGridView = false,
    )
    assertNull(afterSwapInList)

    // 2. Grid view (2 columns) horizontal drag
    val gridBounds = mutableMapOf(
      "n1" to Rect(0f, 0f, 195f, 120f),
      "n2" to Rect(205f, 0f, 400f, 120f),
    )
    val targetInGrid = findTargetNoteId(
      draggedNoteId = "n1",
      point = Offset(302.5f, 60f),
      sectionNotes = section,
      cardBounds = gridBounds,
      isGridView = true,
    )
    assertEquals("n2", targetInGrid)
  }

  @Test
  fun notesWidgets_filtersByAllPinnedOrLabel_persistsConfig_andSupportsQuickCaptureFabs() {
    assertEquals(16f, NotesWidgets.WIDGET_CORNER_RADIUS_DP, 0.001f)
    NotesWidgets.clearInMemoryConfigs()

    val pinnedWorkNote = NoteItem(
      id = "nw-1",
      title = "Q4 Roadmap",
      content = "Finalize widget specifications",
      isPinned = true,
      labels = listOf("work"),
      updatedAt = 1000L,
    )
    val unpinnedWorkChecklist = NoteItem(
      id = "nw-2",
      title = "Release Checklist",
      isChecklist = true,
      checklistItems = listOf(
        NoteChecklistItem(id = "c1", text = "Update manifest", isChecked = true),
        NoteChecklistItem(id = "c2", text = "Run unit tests", isChecked = false),
      ),
      isPinned = false,
      labels = listOf("work", "android"),
      updatedAt = 2000L,
    )
    val personalPinnedNote = NoteItem(
      id = "nw-3",
      title = "Grocery List",
      content = "Oat milk and espresso beans",
      isPinned = true,
      labels = listOf("personal"),
      updatedAt = 3000L,
    )
    val archivedNote = NoteItem(
      id = "nw-4",
      title = "Archived Note",
      content = "Should not appear in widget",
      isArchived = true,
      labels = listOf("work"),
    )
    val trashedNote = NoteItem(
      id = "nw-5",
      title = "Trashed Note",
      content = "In trash",
      isTrashed = true,
      labels = listOf("work"),
    )

    val repo = InMemoryNotesRepository(
      initialNotes = listOf(
        pinnedWorkNote,
        unpinnedWorkChecklist,
        personalPinnedNote,
        archivedNote,
        trashedNote,
      ),
      initialLabels = listOf("work", "personal", "ideas"),
    )

    // Available labels combines saved labels and active note labels alphabetically
    assertEquals(
      listOf("android", "ideas", "personal", "work"),
      NotesWidgets.resolveAvailableLabels(repository = repo),
    )

    // 1. All Notes mode shows all active (non-archived, non-trashed) notes with pinned notes first
    val allConfig = NotesWidgetConfig(filterMode = NotesWidgetFilterMode.ALL)
    NotesWidgets.saveWidgetConfig(context = null, appWidgetId = 101, config = allConfig)
    val allState = NotesWidgets.resolveWidgetState(
      context = null,
      appWidgetId = 101,
      repository = repo,
    )
    assertEquals("Notes", allState.headerTitle)
    assertEquals("3 notes • 2 pinned", allState.headerSubtitle)
    assertEquals("All", allState.filterPillText)
    assertEquals(listOf("nw-1", "nw-3", "nw-2"), allState.visibleNotes.map { it.id })
    assertFalse(allState.isGridView)
    assertEquals(1, allState.columnsPerRow)
    assertEquals(
      listOf(listOf("nw-1"), listOf("nw-3"), listOf("nw-2")),
      allState.noteRows.map { row -> row.map { it.id } },
    )

    // View mode selector at top right cycles List (1 column) -> 2-column Grid -> List (1 column)
    // and clamps grid columns to a maximum of 2
    assertEquals(2, NotesWidgets.MAX_WIDGET_GRID_COLUMNS)
    val clampedConfig = NotesWidgetConfig(
      filterMode = NotesWidgetFilterMode.ALL,
      isGridView = true,
      gridColumnsPerRow = 3,
    )
    assertEquals(2, clampedConfig.coercedGridColumnsPerRow)
    assertEquals(2, clampedConfig.effectiveColumnsPerRow)

    val cycledToGrid = NotesWidgets.cycleWidgetViewMode(context = null, appWidgetId = 101)
    assertTrue(cycledToGrid.isGridView)
    assertEquals(2, cycledToGrid.effectiveColumnsPerRow)
    val gridState = NotesWidgets.resolveWidgetState(
      context = null,
      appWidgetId = 101,
      repository = repo,
    )
    assertTrue(gridState.isGridView)
    assertEquals(2, gridState.columnsPerRow)
    assertEquals(
      listOf(listOf("nw-1", "nw-3"), listOf("nw-2")),
      gridState.noteRows.map { row -> row.map { it.id } },
    )

    // Cycling again from 2 columns wraps back to List view (1 column)
    val cycledBackToList = NotesWidgets.cycleWidgetViewMode(context = null, appWidgetId = 101)
    assertFalse(cycledBackToList.isGridView)
    assertEquals(1, cycledBackToList.effectiveColumnsPerRow)

    // 2. Pinned Notes mode shows only pinned active notes
    val pinnedConfig = NotesWidgetConfig(filterMode = NotesWidgetFilterMode.PINNED)
    NotesWidgets.saveWidgetConfig(context = null, appWidgetId = 102, config = pinnedConfig)
    val pinnedState = NotesWidgets.resolveWidgetState(
      context = null,
      appWidgetId = 102,
      repository = repo,
    )
    assertEquals("Pinned Notes", pinnedState.headerTitle)
    assertEquals("2 pinned notes", pinnedState.headerSubtitle)
    assertEquals("Pinned", pinnedState.filterPillText)
    assertEquals(listOf("nw-1", "nw-3"), pinnedState.visibleNotes.map { it.id })

    // 3. Specific Label mode shows only non-trashed notes tagged with that label
    val labelConfig = NotesWidgetConfig(
      filterMode = NotesWidgetFilterMode.LABEL,
      labelName = "work",
    )
    val restoredLabelConfig = NotesWidgetConfig.fromJson(labelConfig.toJson())
    assertEquals(NotesWidgetFilterMode.LABEL, restoredLabelConfig.effectiveFilterMode)
    assertEquals("work", restoredLabelConfig.normalizedLabelName)

    NotesWidgets.saveWidgetConfig(context = null, appWidgetId = 103, config = restoredLabelConfig)
    val labelState = NotesWidgets.resolveWidgetState(
      context = null,
      appWidgetId = 103,
      repository = repo,
    )
    assertEquals("work", labelState.headerTitle)
    assertEquals("2 notes • 1 pinned", labelState.headerSubtitle)
    assertEquals("work", labelState.filterPillText)
    assertEquals(listOf("nw-1", "nw-2"), labelState.visibleNotes.map { it.id })

    // 4. Item formatting helpers for titles, checklist previews, badges, and metadata
    assertEquals("1/2", NotesWidgets.formatChecklistBadgeText(unpinnedWorkChecklist))
    assertNull(NotesWidgets.formatChecklistBadgeText(pinnedWorkNote))
    assertEquals("☐ Run unit tests", NotesWidgets.buildNoteItemBodyPreview(unpinnedWorkChecklist))
    assertEquals("Finalize widget specifications", NotesWidgets.buildNoteItemBodyPreview(pinnedWorkNote))

    // 5. Plus (+) and Checklist FAB quick-capture requests carry the widget's filter context
    NotesEngine.consumePendingQuickCaptureRequest()
    NotesEngine.requestQuickCapture(
      isChecklist = false,
      initialPinned = true,
      initialLabel = null,
    )
    val pinnedNoteCapture = NotesEngine.consumePendingQuickCaptureRequest()
    assertEquals(false, pinnedNoteCapture?.isChecklist)
    assertEquals(true, pinnedNoteCapture?.initialPinned)
    assertNull(pinnedNoteCapture?.initialLabel)

    NotesEngine.requestQuickCapture(
      isChecklist = true,
      initialPinned = false,
      initialLabel = "work",
    )
    val labeledChecklistCapture = NotesEngine.consumePendingQuickCaptureRequest()
    assertEquals(true, labeledChecklistCapture?.isChecklist)
    assertEquals(false, labeledChecklistCapture?.initialPinned)
    assertEquals("work", labeledChecklistCapture?.initialLabel)

    // 6. Note card color in the widget follows the selected NoteColor
    val darkPalette = allState.palette.copy(isDarkTheme = true)
    val lightPalette = allState.palette.copy(isDarkTheme = false)
    assertEquals(
      darkPalette.itemCardArgb,
      NotesWidgets.resolveNoteCardArgb(NoteColor.DEFAULT, darkPalette),
    )
    assertEquals(
      lightPalette.itemCardArgb,
      NotesWidgets.resolveNoteCardArgb(NoteColor.DEFAULT, lightPalette),
    )

    val resolvedDarkColors = NoteColor.entries.map { color ->
      NotesWidgets.resolveNoteCardArgb(color, darkPalette)
    }
    val resolvedLightColors = NoteColor.entries.map { color ->
      NotesWidgets.resolveNoteCardArgb(color, lightPalette)
    }
    assertEquals(NoteColor.entries.size, resolvedDarkColors.toSet().size)
    assertEquals(NoteColor.entries.size, resolvedLightColors.toSet().size)
    NoteColor.entries.filter { it != NoteColor.DEFAULT }.forEach { color ->
      val fillDark = NotesWidgets.resolveNoteCardArgb(color, darkPalette)
      val borderDark = NotesWidgets.resolveNoteCardBorderArgb(color, darkPalette)
      assertTrue(fillDark != darkPalette.itemCardArgb)
      assertEquals(0xFF000000.toInt(), fillDark and 0xFF000000.toInt())
      assertEquals(0xFF000000.toInt(), borderDark and 0xFF000000.toInt())
    }
  }
}
