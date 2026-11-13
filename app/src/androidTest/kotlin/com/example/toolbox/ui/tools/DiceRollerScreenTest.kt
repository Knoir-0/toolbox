package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DiceRollerScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun testSaveAndLoadButtons_layoutAndFunctionality() {
    val initialSaved = mutableListOf<SavedDiceRoll>()

    composeTestRule.setContent {
      ToolboxTheme {
        DiceRollerScreen(
          initialSheetValue = SheetValue.PartiallyExpanded,
          initialHistory = emptyList(),
          initialSavedRolls = initialSaved,
          onSaveRolls = { updated ->
            initialSaved.clear()
            initialSaved.addAll(updated)
          },
        )
      }
    }

    // 1. Verify Save and Load buttons are displayed
    val saveButton = composeTestRule.onNodeWithTag("dice_roller_save_button")
    val loadButton = composeTestRule.onNodeWithTag("dice_roller_load_button")
    val rollButton = composeTestRule.onNodeWithTag("dice_roller_roll_button")

    saveButton.assertIsDisplayed()
    loadButton.assertIsDisplayed()
    rollButton.assertIsDisplayed()

    // 2. Verify Load button is to the left of the Roll button, and Roll is to the left of Save
    val rollBounds = rollButton.getUnclippedBoundsInRoot()
    val saveBounds = saveButton.getUnclippedBoundsInRoot()
    val loadBounds = loadButton.getUnclippedBoundsInRoot()

    assertTrue("Load button should be to the left of Roll button", loadBounds.left < rollBounds.left)
    assertTrue("Roll button should be to the left of Save button", rollBounds.left < saveBounds.left)

    // 3. Click Save button to open save roll dialog
    saveButton.performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save Roll").assertIsDisplayed()
    composeTestRule.onNodeWithTag("save_roll_name_input").assertIsDisplayed()

    // Type a name for the roll
    composeTestRule.onNodeWithTag("save_roll_name_input").performTextInput("Fireball")
    composeTestRule.waitForIdle()

    // Click confirm to save
    composeTestRule.onNodeWithTag("save_roll_confirm_button").performClick()
    composeTestRule.waitForIdle()

    // 4. Click Load button to open saved rolls sheet
    loadButton.performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("saved_rolls_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithText("Fireball").assertIsDisplayed()

    // 5. Tap on the saved roll to roll it and dismiss sheet
    composeTestRule.onNodeWithText("Fireball").performClick()
    composeTestRule.waitForIdle()

    // Sheet should be dismissed
    composeTestRule.onNodeWithTag("saved_rolls_sheet").assertDoesNotExist()
  }
}
