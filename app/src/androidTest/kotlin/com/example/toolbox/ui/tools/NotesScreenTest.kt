package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotesScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun notesScreen_emptyStateDisplaysAndCanCreateTextNote() {
    val repo = InMemoryNotesRepository()
    composeTestRule.setContent {
      ToolboxTheme {
        NotesScreen(repository = repo)
      }
    }

    composeTestRule.onNodeWithTag("notes_empty_state").assertIsDisplayed()
    val pinBounds = composeTestRule.onNodeWithTag("new_pin_note_button").fetchSemanticsNode().boundsInRoot
    val checklistBounds = composeTestRule.onNodeWithTag("new_checklist_button").fetchSemanticsNode().boundsInRoot
    assertTrue("Pin and checklist buttons should not overlap", pinBounds.right <= checklistBounds.left)
    assertEquals(pinBounds.width, checklistBounds.width, 1f)
    assertEquals(pinBounds.height, checklistBounds.height, 1f)

    composeTestRule.onNodeWithTag("take_a_note_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("add_note_panel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_save_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("editor_pin_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("editor_checklist_toggle").assertDoesNotExist()
    composeTestRule.onNodeWithTag("editor_archive_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("editor_copy_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("editor_delete_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("editor_title_input").assertIsFocused()
    composeTestRule.onNodeWithTag("editor_title_input").performTextInput("Trail Mix Recipe")
    composeTestRule.onNodeWithTag("editor_content_input").performTextInput("Almonds, cashews, dark chocolate")
    composeTestRule.onNodeWithTag("editor_timestamp").assertDoesNotExist()
    composeTestRule.onNodeWithTag("editor_color_PEACH").performClick()
    composeTestRule.onNodeWithTag("new_pin_note_button").performClick()
    composeTestRule.onNodeWithTag("editor_toggle_label_picker").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("editor_new_label_input").assertIsFocused()
    composeTestRule.onNodeWithTag("editor_new_label_input").performTextInput("snacks")
    composeTestRule.onNodeWithTag("editor_add_label_confirm").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("editor_label_chip_snacks").assertIsDisplayed()
    val cancelButtonNode = composeTestRule.onNodeWithTag("add_note_cancel_button")
    val confirmButtonNode = composeTestRule.onNodeWithTag("add_note_confirm_button")
    cancelButtonNode.assertIsDisplayed()
    confirmButtonNode.assertIsDisplayed()
    val cancelBounds = cancelButtonNode.fetchSemanticsNode().boundsInRoot
    val confirmBounds = confirmButtonNode.fetchSemanticsNode().boundsInRoot
    assertTrue("Cancel button should be to the left of confirm button", cancelBounds.right < confirmBounds.left)
    confirmButtonNode.performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Trail Mix Recipe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Almonds, cashews, dark chocolate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notes_section_pinned").assertIsDisplayed()

    // Tapping the bottom-left X button in the add note view discards the draft and closes the panel
    composeTestRule.onNodeWithTag("take_a_note_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("add_note_panel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_title_input").performTextInput("Discarded Draft")
    composeTestRule.onNodeWithTag("add_note_cancel_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("add_note_panel").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Discarded Draft").assertDoesNotExist()
  }

  @Test
  fun notesScreen_tappingNote_opensSingleNoteViewWithAllEditorControls() {
    val repo = InMemoryNotesRepository(
      initialNotes = listOf(
        NoteItem(
          id = "n1",
          title = "Sourdough Starter",
          content = "25g starter, 50g water, 50g flour",
          color = NoteColor.SAND,
          labels = listOf("baking"),
        ),
      ),
      initialLabels = listOf("baking"),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        NotesScreen(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("note_card_n1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("single_note_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_note_back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_save_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("single_note_title").assertDoesNotExist()
    composeTestRule.onNodeWithTag("single_note_subtext").assertDoesNotExist()
    composeTestRule.onNodeWithTag("editor_pin_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_checklist_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_archive_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_copy_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_delete_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_title_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_content_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_note_timestamp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_note_bottom_color_bar").assertIsDisplayed()
    val singleNoteBoundsBefore = composeTestRule.onNodeWithTag("single_note_view").fetchSemanticsNode().boundsInRoot
    val colorBarBoundsBefore = composeTestRule.onNodeWithTag("single_note_bottom_color_bar").fetchSemanticsNode().boundsInRoot
    assertEquals(singleNoteBoundsBefore.bottom, colorBarBoundsBefore.bottom, 1f)

    // Tapping in the empty space below the note content focuses the content field with cursor at the end
    composeTestRule.onNodeWithTag("single_note_editor_empty_space").performTouchInput {
      click(androidx.compose.ui.geometry.Offset(centerX, bottom - 20f))
    }
    composeTestRule.waitForIdle()
    val contentNode = composeTestRule.onNodeWithTag("editor_content_input")
    contentNode.assertIsFocused()
    val selection = contentNode.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.TextSelectionRange]
    assertEquals(androidx.compose.ui.text.TextRange("25g starter, 50g water, 50g flour".length), selection)

    composeTestRule.onNodeWithTag("editor_label_chip_baking").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_color_SAGE").performClick()
    composeTestRule.onNodeWithTag("editor_pin_button").performClick()
    composeTestRule.onNodeWithTag("single_note_back_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("single_note_view").assertIsNotDisplayed()
    val updated = repo.loadNotes().first()
    assertEquals(NoteColor.SAGE, updated.color)
    assertTrue(updated.isPinned)
  }

  @Test
  fun notesScreen_checklistCreationAndInlineToggleWork() {
    val repo = InMemoryNotesRepository(
      initialNotes = listOf(
        NoteItem(
          id = "chk-1",
          title = "Grocery Run",
          isChecklist = true,
          checklistItems = listOf(
            NoteChecklistItem(id = "item-1", text = "Oat milk", isChecked = false),
            NoteChecklistItem(id = "item-2", text = "Coffee beans", isChecked = false),
          ),
          color = NoteColor.MINT,
        ),
      ),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        NotesScreen(repository = repo)
      }
    }

    composeTestRule.onNodeWithText("Grocery Run").assertIsDisplayed()
    composeTestRule.onNodeWithText("Oat milk").assertIsDisplayed()

    // Tapping the checklist item row text opens the note instead of checking the item
    composeTestRule.onNodeWithText("Oat milk").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("single_note_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_note_back_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("single_note_view").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Oat milk").assertIsDisplayed()

    // Check off 'Oat milk' directly from the note card's checkbox
    composeTestRule.onNodeWithTag("note_card_check_item_item-1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("+ 1 checked item").assertIsDisplayed()

    // Open the checklist note, type in the checklist input, and verify checked item 'Oat milk' is suggested
    composeTestRule.onNodeWithTag("note_card_chk-1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("single_note_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_checked_suggestions").assertDoesNotExist()

    composeTestRule.onNodeWithTag("editor_new_checklist_input").performTextInput("Oat")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("editor_checked_suggestions").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_checked_suggestion_item-1").assertIsDisplayed()

    // Tapping the suggested checked item unchecks (restores) it and clears the input
    composeTestRule.onNodeWithTag("editor_checked_suggestion_item-1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("editor_checked_suggestions").assertDoesNotExist()

    // Pasting a list with return characters into the new item input and adding it parses into multiple checklist items
    composeTestRule.onNodeWithTag("editor_new_checklist_input").performTextInput("Apples\nBananas\nCherries")
    composeTestRule.onNodeWithTag("editor_add_checklist_item").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("single_note_back_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Oat milk").assertIsDisplayed()
    composeTestRule.onNodeWithText("Apples").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bananas").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cherries").assertIsDisplayed()
    composeTestRule.onNodeWithText("+ 1 checked item").assertDoesNotExist()
  }

  @Test
  fun notesScreen_checklistWithMoreThanSixItems_showsFiveAndMoreItemsAtBottomLeftWithTimestampAtBottomRight() {
    val repo = InMemoryNotesRepository(
      initialNotes = listOf(
        NoteItem(
          id = "chk-many",
          title = "Packing List",
          isChecklist = true,
          checklistItems = (1..7).map { idx ->
            NoteChecklistItem(id = "item-$idx", text = "Gear $idx", isChecked = false)
          },
          color = NoteColor.SAGE,
        ),
      ),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        NotesScreen(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    (1..5).forEach { idx ->
      composeTestRule.onNodeWithText("Gear $idx").assertIsDisplayed()
    }
    composeTestRule.onNodeWithText("Gear 6").assertDoesNotExist()
    composeTestRule.onNodeWithText("Gear 7").assertDoesNotExist()

    val moreNode = composeTestRule.onNodeWithTag("note_card_more_items_chk-many")
    val timestampNode = composeTestRule.onNodeWithTag("note_card_timestamp_chk-many")
    moreNode.assertIsDisplayed()
    composeTestRule.onNodeWithText("2 more items").assertIsDisplayed()
    timestampNode.assertIsDisplayed()

    val moreBounds = moreNode.fetchSemanticsNode().boundsInRoot
    val timestampBounds = timestampNode.fetchSemanticsNode().boundsInRoot
    assertTrue(moreBounds.left < timestampBounds.left)

    // Checking one item leaves 6 unchecked items -> all 6 are shown and "# more items" disappears
    composeTestRule.onNodeWithTag("note_card_check_item_item-1").performClick()
    composeTestRule.waitForIdle()

    (2..7).forEach { idx ->
      composeTestRule.onNodeWithText("Gear $idx").assertIsDisplayed()
    }
    composeTestRule.onNodeWithTag("note_card_more_items_chk-many").assertDoesNotExist()
  }

  @Test
  fun notesScreen_toolbarIntegratedSearchAndColorFilterWork() {
    val repo = InMemoryNotesRepository(
      initialNotes = listOf(
        NoteItem(
          id = "n1",
          title = "Camping Gear",
          content = "Sleeping bag",
          color = NoteColor.SAGE,
        ),
        NoteItem(
          id = "n2",
          title = "Coffee Recipe",
          content = "Pour over ratio",
          color = NoteColor.PEACH,
        ),
      ),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        NotesScreen(repository = repo)
      }
    }

    composeTestRule.onNodeWithText("Camping Gear").assertIsDisplayed()
    composeTestRule.onNodeWithText("Coffee Recipe").assertIsDisplayed()

    // Open toolbar-integrated search
    composeTestRule.onNodeWithTag("notes_search_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("notes_toolbar_search_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notes_color_filter_bar").assertIsDisplayed()

    // Filter by color PEACH while searching
    composeTestRule.onNodeWithTag("notes_color_filter_PEACH").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Coffee Recipe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Camping Gear").assertIsNotDisplayed()

    // Clear color filter and type query
    composeTestRule.onNodeWithTag("notes_color_filter_PEACH").performClick()
    composeTestRule.onNodeWithTag("notes_search_input").performTextInput("Camping")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Camping Gear").assertIsDisplayed()
    composeTestRule.onNodeWithText("Coffee Recipe").assertIsNotDisplayed()

    // Close search
    composeTestRule.onNodeWithTag("notes_search_close").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Camping Gear").assertIsDisplayed()
    composeTestRule.onNodeWithText("Coffee Recipe").assertIsDisplayed()
  }

  @Test
  fun notesScreen_longPressAndDrag_rearrangesNotesInBothGridAndListViews() {
    val repo = InMemoryNotesRepository(
      initialNotes = listOf(
        NoteItem(
          id = "n1",
          title = "Note One",
          content = "First note body",
          color = NoteColor.SAGE,
        ),
        NoteItem(
          id = "n2",
          title = "Note Two",
          content = "Second note body",
          color = NoteColor.PEACH,
        ),
        NoteItem(
          id = "n3",
          title = "Note Three",
          content = "Third note body",
          color = NoteColor.MINT,
        ),
      ),
      initialGridView = true,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        NotesScreen(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    // 1. Grid View (2 columns): n1 is on the left, n2 is on the right
    val n1Node = composeTestRule.onNodeWithTag("note_card_n1")
    val n2Node = composeTestRule.onNodeWithTag("note_card_n2")
    val initialN1Bounds = n1Node.fetchSemanticsNode().boundsInRoot
    val initialN2Bounds = n2Node.fetchSemanticsNode().boundsInRoot
    assertTrue(initialN1Bounds.left < initialN2Bounds.left)

    val gridDelta = initialN2Bounds.center - initialN1Bounds.center
    n1Node.performTouchInput {
      down(center)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      moveBy(gridDelta)
      advanceEventTime(100L)
      up()
    }
    composeTestRule.waitForIdle()

    assertEquals(listOf("n2", "n1", "n3"), repo.loadNotes().map { it.id })
    assertTrue(
      n2Node.fetchSemanticsNode().boundsInRoot.left <
        n1Node.fetchSemanticsNode().boundsInRoot.left
    )

    // 2. Cycle 2-column Grid -> 3-column Grid -> List View (1 column) and drag n2 down onto n3
    composeTestRule.onNodeWithTag("notes_columns_per_row_count").assertTextEquals("2")
    composeTestRule.onNodeWithTag("notes_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("notes_columns_per_row_count").assertTextEquals("3")
    composeTestRule.onNodeWithTag("notes_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()

    val n3Node = composeTestRule.onNodeWithTag("note_card_n3")
    val listN2Bounds = n2Node.fetchSemanticsNode().boundsInRoot
    val listN3Bounds = n3Node.fetchSemanticsNode().boundsInRoot
    assertTrue(listN2Bounds.top < listN3Bounds.top)

    val listDelta = listN3Bounds.center - listN2Bounds.center
    n2Node.performTouchInput {
      down(center)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      moveBy(listDelta)
      advanceEventTime(100L)
      up()
    }
    composeTestRule.waitForIdle()

    assertEquals(listOf("n1", "n3", "n2"), repo.loadNotes().map { it.id })
    assertTrue(
      n3Node.fetchSemanticsNode().boundsInRoot.top <
        n2Node.fetchSemanticsNode().boundsInRoot.top
    )
  }

  @Test
  fun notesScreen_manageLabelsDialog_addsValidatesAndDeletesLabels() {
    val repo = InMemoryNotesRepository(
      initialNotes = listOf(
        NoteItem(
          id = "n1",
          title = "Camping Gear",
          content = "Sleeping bag",
          labels = listOf("outdoors"),
        ),
      ),
      initialLabels = listOf("outdoors"),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        NotesScreen(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    // Open Add & Manage Labels dialog from the Manage Labels button
    composeTestRule.onNodeWithTag("notes_manage_labels_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("manage_labels_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add & Manage Labels").assertIsDisplayed()
    composeTestRule.onNodeWithText("1 Active Label").assertIsDisplayed()

    // Enter a new label and quick-add via the inline '+' button
    composeTestRule.onNodeWithTag("manage_labels_input").performTextInput("recipes")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("manage_labels_add_button").performClick()
    composeTestRule.waitForIdle()

    assertTrue(repo.loadLabels().contains("recipes"))
    composeTestRule.onNodeWithText("2 Active Labels").assertIsDisplayed()

    // Close dialog via Done button
    composeTestRule.onNodeWithTag("manage_labels_done_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("notes_filter_label_recipes").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notes_filter_label_delete_recipes").assertIsDisplayed()
    composeTestRule.onNodeWithText("recipes").assertIsDisplayed()
    composeTestRule.onNodeWithText("#recipes").assertDoesNotExist()

    // 1. Deleting an unused custom label ("recipes") via the x icon on the pill removes it immediately without confirmation modal
    composeTestRule.onNodeWithTag("notes_filter_label_delete_recipes").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("delete_label_confirm_dialog").assertDoesNotExist()
    composeTestRule.onNodeWithTag("notes_filter_label_recipes").assertDoesNotExist()
    assertTrue(!repo.loadLabels().contains("recipes"))

    // 2. Deleting a custom label in use by notes ("outdoors") via the x icon on the pill prompts confirmation modal
    composeTestRule.onNodeWithTag("notes_filter_label_delete_outdoors").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("delete_label_confirm_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Delete Label?").assertIsDisplayed()

    // Cancel keeps the label and note intact
    composeTestRule.onNodeWithTag("cancel_delete_label_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("delete_label_confirm_dialog").assertDoesNotExist()
    composeTestRule.onNodeWithTag("notes_filter_label_outdoors").assertIsDisplayed()
    assertEquals(listOf("outdoors"), repo.loadNotes().single().labels)

    // Confirm removes the label from all notes and from the pill bar while preserving the note itself
    composeTestRule.onNodeWithTag("notes_filter_label_delete_outdoors").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("confirm_delete_label_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("delete_label_confirm_dialog").assertDoesNotExist()
    composeTestRule.onNodeWithTag("notes_filter_label_outdoors").assertDoesNotExist()
    assertTrue(repo.loadLabels().isEmpty())
    assertTrue(repo.loadNotes().single().labels.isEmpty())
    composeTestRule.onNodeWithText("Camping Gear").assertIsDisplayed()
  }

  @Test
  fun notesScreen_holdingDownOnNotes_multiSelectsNotes() {
    val repo = InMemoryNotesRepository(
      initialNotes = listOf(
        NoteItem(
          id = "n1",
          title = "First Note",
          content = "Body one",
        ),
        NoteItem(
          id = "n2",
          title = "Second Note",
          content = "Body two",
        ),
        NoteItem(
          id = "n3",
          title = "Third Note",
          content = "Body three",
        ),
      ),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        NotesScreen(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    // Hold down on the first note (with slight finger movement) -> enters multi-select mode
    composeTestRule.onNodeWithTag("note_card_n1").performTouchInput {
      down(center)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      moveBy(androidx.compose.ui.geometry.Offset(3f, 2f))
      up()
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("notes_selection_top_bar").assertIsDisplayed()
    composeTestRule.onNodeWithText("1 selected").assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_card_checkbox_n1").assertIsDisplayed()

    // Hold down on the second note -> adds it to selection
    composeTestRule.onNodeWithTag("note_card_n2").performTouchInput {
      down(center)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      up()
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("2 selected").assertIsDisplayed()

    // Tapping the third note in selection mode also selects it
    composeTestRule.onNodeWithTag("note_card_n3").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("3 selected").assertIsDisplayed()

    // Clear selection via top bar close button
    composeTestRule.onNodeWithTag("notes_clear_selection").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("notes_selection_top_bar").assertIsNotDisplayed()
  }

  @Test
  fun notesScreen_quickCaptureBarMatchesTodoQuickAddBarSize() {
    val notesRepo = InMemoryNotesRepository()
    val todoRepo = InMemoryTodoListRepository()
    var showTodo = androidx.compose.runtime.mutableStateOf(false)

    composeTestRule.setContent {
      ToolboxTheme {
        if (showTodo.value) {
          TodoListScreen(repository = todoRepo)
        } else {
          NotesScreen(repository = notesRepo)
        }
      }
    }
    composeTestRule.waitForIdle()

    val notesBarBounds = composeTestRule.onNodeWithTag("notes_quick_capture_card").fetchSemanticsNode().boundsInRoot

    // Open checklist add-note view and verify list item input matches Take a note bar height
    // and places the 28.dp add button inside the input to the left of the text field
    composeTestRule.onNodeWithTag("new_checklist_button").performClick()
    composeTestRule.waitForIdle()

    val checklistInputCardBounds = composeTestRule.onNodeWithTag("editor_checklist_input_card").fetchSemanticsNode().boundsInRoot
    val checklistAddBtnBounds = composeTestRule.onNodeWithTag("editor_add_checklist_item").fetchSemanticsNode().boundsInRoot
    val checklistTextInputBounds = composeTestRule.onNodeWithTag("editor_new_checklist_input").fetchSemanticsNode().boundsInRoot

    assertEquals(notesBarBounds.height, checklistInputCardBounds.height, 1f)
    assertTrue(checklistAddBtnBounds.left >= checklistInputCardBounds.left)
    assertTrue(checklistAddBtnBounds.right <= checklistTextInputBounds.left)
    assertTrue(checklistAddBtnBounds.top >= checklistInputCardBounds.top)
    assertTrue(checklistAddBtnBounds.bottom <= checklistInputCardBounds.bottom)

    composeTestRule.runOnIdle {
      todoRepo.saveTasks(listOf(TodoItem(id = "task-1", title = "Match height test")))
      showTodo.value = true
    }
    composeTestRule.waitForIdle()

    val todoBarBounds = composeTestRule.onNodeWithTag("todo_quick_add_card").fetchSemanticsNode().boundsInRoot
    val todoItemBounds = composeTestRule.onNodeWithTag("todo_item_card_task-1").fetchSemanticsNode().boundsInRoot

    assertEquals(todoBarBounds.width, notesBarBounds.width, 1f)
    assertEquals(todoBarBounds.height, notesBarBounds.height, 1f)
    assertEquals(todoBarBounds.height, todoItemBounds.height, 1f)
  }

  @Test
  fun notesScreen_requiresScopedStorageAccessToNotesRoot_andSavesTtxtFiles() {
    val tempRootDir = java.nio.file.Files.createTempDirectory("notes_scoped_root").toFile()
    val notesDir = java.io.File(tempRootDir, "Notes")
    try {
      val repo = InMemoryNotesRepository(
        initialNotesFolderGranted = false,
        notesDirectory = notesDir,
      )
      var requestedFolder: NotesFolder? = null

      composeTestRule.setContent {
        ToolboxTheme {
          NotesScreen(
            repository = repo,
            initialGrantedFolderPaths = emptySet(),
            onRequestFolderSystemAccess = { folder, onResult ->
              requestedFolder = folder
              onResult(true)
            },
          )
        }
      }
      composeTestRule.waitForIdle()

      // 1. Before granting scoped storage access to /Notes, tool shows locked storage view and hides quick capture bar
      composeTestRule.onNodeWithTag("locked_storage_state").assertIsDisplayed()
      composeTestRule.onNodeWithTag("notes_quick_capture_card").assertDoesNotExist()
      composeTestRule.onNodeWithTag("folder_lock_icon_Notes", useUnmergedTree = true).assertExists()

      // 2. Tapping a locked navigation pill displays the /Notes folder permission request view
      composeTestRule.onNodeWithTag("notes_filter_all").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("folder_permission_view").assertIsDisplayed()
      composeTestRule.onNodeWithText("Allow access to files in /Notes?").assertIsDisplayed()

      val systemIntent = NotesEngine.createFolderAccessIntent(
        context = composeTestRule.activity,
        folder = DEFAULT_NOTES_SCOPED_FOLDER,
        userRoot = tempRootDir,
      )
      assertEquals(android.content.Intent.ACTION_OPEN_DOCUMENT_TREE, systemIntent.action)

      // 3. Tapping "Allow" grants scoped storage access to /Notes and unlocks the Notes tool
      composeTestRule.onNodeWithTag("allow_folder_permission_button").performClick()
      composeTestRule.waitForIdle()

      assertEquals(DEFAULT_NOTES_SCOPED_FOLDER, requestedFolder)
      composeTestRule.onNodeWithTag("locked_storage_state").assertDoesNotExist()
      composeTestRule.onNodeWithTag("folder_permission_view").assertDoesNotExist()
      composeTestRule.onNodeWithTag("notes_quick_capture_card").assertIsDisplayed()

      // 4. Create a checklist note and verify it is saved as a .ttxt file in /Notes with '- ' and '+ ' list syntax
      composeTestRule.onNodeWithTag("new_checklist_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("editor_title_input").performTextInput("Toolbox Supplies")
      composeTestRule.onNodeWithTag("editor_new_checklist_input").performTextInput("Wrench\nScrewdriver")
      composeTestRule.onNodeWithTag("editor_add_checklist_item").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("add_note_confirm_button").performClick()
      composeTestRule.waitForIdle()

      val savedTtxtFile = java.io.File(notesDir, "Toolbox Supplies.ttxt")
      assertTrue("Expected .ttxt file in /Notes", savedTtxtFile.exists())
      assertEquals(
        "# Toolbox Supplies\n\n- Wrench\n- Screwdriver",
        savedTtxtFile.readText(),
      )

      // Checking off the first item replaces '-' with '+' in the .ttxt file
      val firstItemId = repo.loadNotes().single().checklistItems.first().id
      composeTestRule.onNodeWithTag("note_card_check_item_$firstItemId").performClick()
      composeTestRule.waitForIdle()

      assertEquals(
        "# Toolbox Supplies\n\n+ Wrench\n- Screwdriver",
        savedTtxtFile.readText(),
      )
    } finally {
      tempRootDir.deleteRecursively()
    }
  }
}
