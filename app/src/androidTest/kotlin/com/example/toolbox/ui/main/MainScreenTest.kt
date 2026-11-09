package com.example.toolbox.ui.main

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.toolbox.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.activity.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
    composeTestRule.setContent { MainScreen() }
  }

  @Test
  fun collapsedPanel_firstRowToolLabelsAreDisplayed() {
    composeTestRule.onNodeWithText("Dice Roller").assertIsDisplayed()
    composeTestRule.onNodeWithText("Unit Converter").assertIsDisplayed()
    composeTestRule.onNodeWithText("Guitar Tuner").assertIsDisplayed()
  }

  @Test
  fun pullUpPanel_switchesSelectedTool() {
    composeTestRule.onNodeWithText("Roll 1d20").assertExists()

    // Expand the pull-up panel so all rows of tools are visible
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Unit Converter").onFirst().performClick()
    composeTestRule.onNodeWithText("From Unit (Meter)").assertExists()

    composeTestRule.onAllNodesWithText("Guitar Tuner").onFirst().performClick()
    composeTestRule.onNodeWithText("TARGET").assertExists()

    composeTestRule.onAllNodesWithText("Metronome").onFirst().performClick()
    composeTestRule.onNodeWithText("Play").assertExists()

    composeTestRule.onAllNodesWithText("Color Picker").onFirst().performClick()
    composeTestRule.onNodeWithText("2D Color Canvas").assertExists()

    composeTestRule.onAllNodesWithText("Ruler").onFirst().performClick()
    composeTestRule.onNodeWithText("Caliper (A↔B)").assertExists()
    composeTestRule.onNodeWithTag("screen_edge_ruler_canvas").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ruler_unit_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ruler_unit_toggle").performClick()

    // Up to 2 rows per page: Calculator is in Row 2 on Page 1
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Calculator").onFirst().performClick()
    composeTestRule.onNodeWithTag("calculator_screen").assertExists()
  }

  @Test
  fun rowControls_hiddenUntilPulledUp_andSliderAdjustsFromThreeToSix_andHelpButtonShowsExplanation() {
    // Row controls are hidden when collapsed like a native navigation bar
    composeTestRule.onNodeWithTag("tools_per_row_control").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tools_per_row_slider").assertDoesNotExist()

    // Pull up the navigation box
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Row controls and slider with dots (3 - 6) are now visible
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_per_row_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_per_row_count").assertTextEquals("3")
    composeTestRule.onNodeWithTag("tools_grid_row_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_1").assertIsDisplayed()

    // Adjust slider to 4 per row
    composeTestRule.onNodeWithTag("tools_per_row_slider")
      .performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_count").assertTextEquals("4")

    // Dots underneath the slider are removed
    composeTestRule.onNodeWithTag("tools_per_row_dot_6").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tools_per_row_dot_3").assertDoesNotExist()

    // Adjust slider to 6 per row
    composeTestRule.onNodeWithTag("tools_per_row_slider")
      .performSemanticsAction(SemanticsActions.SetProgress) { it(6f) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_count").assertTextEquals("6")
    composeTestRule.onNodeWithTag("tools_grid_row_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_1").assertIsDisplayed()

    // Adjust slider to return to 3 per row
    composeTestRule.onNodeWithTag("tools_per_row_slider")
      .performSemanticsAction(SemanticsActions.SetProgress) { it(3f) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_count").assertTextEquals("3")

    // Tap the ? button on the right side of the slider to reveal drag-and-drop explanation text
    composeTestRule.onNodeWithTag("reorder_help_text").assertDoesNotExist()
    composeTestRule.onNodeWithTag("reorder_help_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("reorder_help_text").assertIsDisplayed()
  }

  @Test
  fun pullUpPanel_longPressAndDrag_reordersTools() {
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    val diceNode = composeTestRule.onNodeWithTag("tool_item_DICE_ROLLER")
    val converterNode = composeTestRule.onNodeWithTag("tool_item_UNIT_CONVERTER")

    val initialDiceLeft = diceNode.fetchSemanticsNode().boundsInRoot.left
    val initialConverterLeft = converterNode.fetchSemanticsNode().boundsInRoot.left
    assertTrue(initialDiceLeft < initialConverterLeft)

    val dragDeltaX = converterNode.fetchSemanticsNode().boundsInRoot.center.x -
      diceNode.fetchSemanticsNode().boundsInRoot.center.x

    composeTestRule.onNodeWithTag("tools_grid").performTouchInput {
      val start = Offset(width / 6f, height / 6f)
      down(start)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      moveBy(Offset(dragDeltaX, 0f))
      advanceEventTime(100L)
      up()
    }
    composeTestRule.waitForIdle()

    val newDiceLeft = diceNode.fetchSemanticsNode().boundsInRoot.left
    val newConverterLeft = converterNode.fetchSemanticsNode().boundsInRoot.left
    assertTrue(newConverterLeft < newDiceLeft)
  }

  @Test
  fun pullUpPanel_changingToolsPerRow_persistsPreference() {
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Change tools per row to 5 via slider
    composeTestRule.onNodeWithTag("tools_per_row_slider")
      .performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_count").assertTextEquals("5")

    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    assertEquals(5, repo.getToolsPerRow())
  }

  @Test
  fun pullUpPanel_reorderingTools_persistsPreference() {
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    val diceNode = composeTestRule.onNodeWithTag("tool_item_DICE_ROLLER")
    val converterNode = composeTestRule.onNodeWithTag("tool_item_UNIT_CONVERTER")

    val dragDeltaX = converterNode.fetchSemanticsNode().boundsInRoot.center.x -
      diceNode.fetchSemanticsNode().boundsInRoot.center.x

    composeTestRule.onNodeWithTag("tools_grid").performTouchInput {
      val start = Offset(width / 6f, height / 6f)
      down(start)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      moveBy(Offset(dragDeltaX, 0f))
      advanceEventTime(100L)
      up()
    }
    composeTestRule.waitForIdle()

    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    val savedOrder = repo.getToolOrder()
    val diceIndex = savedOrder.indexOf(Tool.DICE_ROLLER)
    val converterIndex = savedOrder.indexOf(Tool.UNIT_CONVERTER)
    assertTrue(converterIndex < diceIndex)
  }

  @Test
  fun collapsedPanel_swipeBetweenRows_allowsNavigatingAndSelectingTools() {
    // Initially on Row 0
    composeTestRule.onNodeWithText("Dice Roller").assertIsDisplayed()
    composeTestRule.onNodeWithText("Unit Converter").assertIsDisplayed()
    composeTestRule.onNodeWithText("Guitar Tuner").assertIsDisplayed()
    composeTestRule.onNodeWithText("Metronome").assertDoesNotExist()

    // Row indicator dots exist
    composeTestRule.onNodeWithTag("tools_panel_row_indicator").assertIsDisplayed()

    // Swipe left between rows on the collapsed navigation panel
    composeTestRule.onNodeWithTag("tools_grid").performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()

    // Now on Row 1: Metronome, Color Picker, Ruler are displayed
    composeTestRule.onNodeWithText("Metronome").assertIsDisplayed()
    composeTestRule.onNodeWithText("Color Picker").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ruler").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dice Roller").assertDoesNotExist()

    // Selecting a tool on Row 1 switches to that tool
    composeTestRule.onNodeWithText("Metronome").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Play").assertExists()

    // Tapping dot 0 in the row indicator jumps back to Row 0
    composeTestRule.onNodeWithTag("tools_panel_row_indicator_dot_0").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Dice Roller").assertIsDisplayed()
  }

  @Test
  fun openedPanel_paginatesAfterTwoRows_andAllowsSwipingAndPaging() {
    // Open the pull up panel
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // 10 tools with 3 per row is 4 rows; paginating after 2 rows gives 2 pages (2 rows each)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_1").assertIsDisplayed()
    // Row 2 and Row 3 are on Page 1, so they are not visible on Page 0
    composeTestRule.onNodeWithTag("tools_grid_row_2").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tools_grid_row_3").assertDoesNotExist()

    // Navigate to Page 1 via indicator dot
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_1").performClick()
    composeTestRule.waitForIdle()

    // Row 2 and Row 3 are now displayed on Page 1
    composeTestRule.onNodeWithTag("tools_grid_row_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_3").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").assertIsDisplayed()

    // Select Settings on Page 1
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Collapse the panel; since Settings (Row 3) was selected, collapsed panel shows Row 3
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").assertIsDisplayed()
  }
}
