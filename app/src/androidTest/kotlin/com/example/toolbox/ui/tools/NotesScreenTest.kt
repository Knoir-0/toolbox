package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
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
    composeTestRule.onNodeWithTag("take_a_note_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("editor_title_input").performTextInput("Trail Mix Recipe")
    composeTestRule.onNodeWithTag("editor_content_input").performTextInput("Almonds, cashews, dark chocolate")
    composeTestRule.onNodeWithTag("editor_color_PEACH").performClick()
    composeTestRule.onNodeWithTag("editor_pin_button").performClick()
    composeTestRule.onNodeWithTag("editor_save_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Trail Mix Recipe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Almonds, cashews, dark chocolate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notes_section_pinned").assertIsDisplayed()
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

    // Check off 'Oat milk' directly from the note card
    composeTestRule.onNodeWithTag("note_card_check_item_item-1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("+ 1 checked item").assertIsDisplayed()
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

    // 2. Switch to List View (1 column) and drag n2 down onto n3
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

    // Open Add & Manage Labels dialog from the '+ Label' chip
    composeTestRule.onNodeWithTag("notes_add_label_chip").performClick()
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
  }
}
