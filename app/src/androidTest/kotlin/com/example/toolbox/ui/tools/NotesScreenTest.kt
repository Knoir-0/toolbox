package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.toolbox.ui.theme.ToolboxTheme
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
}
