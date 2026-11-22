package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
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

    // Sheet should be dismissed and label displayed top center on the hero card
    composeTestRule.onNodeWithTag("saved_rolls_sheet").assertDoesNotExist()
    composeTestRule.onNodeWithTag("dice_roller_hero_label").assertIsDisplayed()
    composeTestRule.onNodeWithText("Fireball").assertIsDisplayed()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun testToolbarSpellSearch_rollsSpellDisplaysTopCenterLabelAndPrefillsSaveDialog() {
    val savedList = mutableListOf<SavedDiceRoll>()

    composeTestRule.setContent {
      ToolboxTheme {
        DiceRollerScreen(
          initialSheetValue = SheetValue.PartiallyExpanded,
          initialHistory = emptyList(),
          initialSavedRolls = savedList,
          onSaveRolls = { updated ->
            savedList.clear()
            savedList.addAll(updated)
          },
        )
      }
    }

    // 1. Verify top toolbar and search button are displayed
    composeTestRule.onNodeWithTag("dice_roller_top_bar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dice Roller").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_search_bar_button").assertIsDisplayed()

    // 2. Tap search button to switch to D&D spell search view
    composeTestRule.onNodeWithTag("toggle_search_bar_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("toolbar_search_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dice_roller_spell_search_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dice_roller_hero_card").assertDoesNotExist()

    // 3. Common spells are displayed; filter by "Magic" and tap on "Magic Missile"
    composeTestRule.onNodeWithText("Fireball").assertIsDisplayed()
    composeTestRule.onNodeWithText("Magic Missile").assertIsDisplayed()
    composeTestRule.onNodeWithTag("spell_search_input").performTextInput("Magic")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Magic Missile").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    // 4. Verify returned to roll view, rolled the spell (3d4 + 3), and label is displayed top center on hero card
    composeTestRule.onNodeWithTag("dice_roller_spell_search_view").assertDoesNotExist()
    val heroCard = composeTestRule.onNodeWithTag("dice_roller_hero_card")
    val heroLabel = composeTestRule.onNodeWithTag("dice_roller_hero_label")
    heroCard.assertIsDisplayed()
    heroLabel.assertIsDisplayed()
    composeTestRule.onNodeWithText("Magic Missile").assertIsDisplayed()
    composeTestRule.onNodeWithText("Roll 3d4 + 3").assertIsDisplayed()

    val heroBounds = heroCard.getUnclippedBoundsInRoot()
    val labelBounds = heroLabel.getUnclippedBoundsInRoot()
    val heroCenterX = (heroBounds.left + heroBounds.right) / 2
    val labelCenterX = (labelBounds.left + labelBounds.right) / 2
    val heroCenterY = (heroBounds.top + heroBounds.bottom) / 2
    val labelCenterY = (labelBounds.top + labelBounds.bottom) / 2

    // Label is horizontally centered and in the top portion of the hero card
    assertTrue(
      "Label should be horizontally centered on hero card",
      kotlin.math.abs(heroCenterX.value - labelCenterX.value) < 12f,
    )
    assertTrue(
      "Label should be in the top portion of hero card",
      labelCenterY < heroCenterY,
    )

    // 5. Hitting the Save button prefills the save dialog with the roll label
    composeTestRule.onNodeWithTag("dice_roller_save_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save Roll").assertIsDisplayed()
    composeTestRule.onNodeWithText("Magic Missile").assertIsDisplayed()

    // Confirming save immediately saves with the prefilled label
    composeTestRule.onNodeWithTag("save_roll_confirm_button").performClick()
    composeTestRule.waitForIdle()

    assertTrue(savedList.any { it.name == "Magic Missile" && it.notation == "3d4 + 3" })
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun testSearchPanel_weaponsFilterAndBottomRightAttributionFabModal() {
    composeTestRule.setContent {
      ToolboxTheme {
        DiceRollerScreen(
          initialSheetValue = SheetValue.PartiallyExpanded,
          initialHistory = emptyList(),
          initialSavedRolls = emptyList(),
        )
      }
    }

    // 1. Open search view
    composeTestRule.onNodeWithTag("toggle_search_bar_button").performClick()
    composeTestRule.waitForIdle()

    val searchView = composeTestRule.onNodeWithTag("dice_roller_spell_search_view")
    val infoFab = composeTestRule.onNodeWithTag("srd_attribution_fab")
    searchView.assertIsDisplayed()
    infoFab.assertIsDisplayed()

    // 2. Verify info FAB is positioned at the bottom right of the search panel
    val searchBounds = searchView.getUnclippedBoundsInRoot()
    val fabBounds = infoFab.getUnclippedBoundsInRoot()
    val searchCenterX = (searchBounds.left + searchBounds.right) / 2
    val searchCenterY = (searchBounds.top + searchBounds.bottom) / 2
    val fabCenterX = (fabBounds.left + fabBounds.right) / 2
    val fabCenterY = (fabBounds.top + fabBounds.bottom) / 2

    assertTrue("Info FAB should be on the right side of the search panel", fabCenterX > searchCenterX)
    assertTrue("Info FAB should be in the bottom portion of the search panel", fabCenterY > searchCenterY)

    // 3. Click the info FAB to open the SRD 5.2.1 CC-BY-4.0 attribution modal
    infoFab.performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("srd_attribution_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("SRD 5.2.1 Attribution").assertIsDisplayed()
    composeTestRule.onNodeWithTag("srd_attribution_text").assertIsDisplayed()
    composeTestRule.onNodeWithText(DndSpellCatalog.SRD_ATTRIBUTION_TEXT).assertIsDisplayed()

    // 4. Close attribution dialog
    composeTestRule.onNodeWithTag("srd_attribution_close_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("srd_attribution_dialog").assertDoesNotExist()

    // 5. Filter by Weapons category pill and roll Greatsword (2d6)
    composeTestRule.onNodeWithTag("spell_category_chip_WEAPONS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("spell_search_input").performTextInput("Greatsword")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Greatsword").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("dice_roller_hero_label").assertIsDisplayed()
    composeTestRule.onNodeWithText("Greatsword").assertIsDisplayed()
    composeTestRule.onNodeWithText("Roll 2d6").assertIsDisplayed()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun testHoldToRepeatAndModifierMax40() {
    composeTestRule.setContent {
      ToolboxTheme {
        DiceRollerScreen(
          initialSheetValue = SheetValue.PartiallyExpanded,
          initialHistory = emptyList(),
          initialSavedRolls = emptyList(),
        )
      }
    }

    // Press and hold "Increase Modifier" to reach the maximum of +40
    val increaseModButton = composeTestRule.onNodeWithContentDescription("Increase Modifier")
    increaseModButton.performTouchInput {
      down(center)
      advanceEventTime(4000L)
      up()
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("+40").assertIsDisplayed()

    // Press and hold "Increase Dice Count" to increase count via hold
    val increaseCountButton = composeTestRule.onNodeWithContentDescription("Increase Dice Count")
    increaseCountButton.performTouchInput {
      down(center)
      advanceEventTime(2000L)
      up()
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("20d20").assertIsDisplayed()
  }
}
