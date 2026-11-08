package com.example.toolbox.ui.main

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
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.toolbox.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
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

    // Tap dot 6 to fit all 6 tools in a single row
    composeTestRule.onNodeWithTag("tools_per_row_dot_6").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_count").assertTextEquals("6")
    composeTestRule.onNodeWithTag("tools_grid_row_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_1").assertDoesNotExist()

    // Tap dot 3 to return to 3 per row
    composeTestRule.onNodeWithTag("tools_per_row_dot_3").performClick()
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
      val start = Offset(width / 6f, height / 4f)
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
}
