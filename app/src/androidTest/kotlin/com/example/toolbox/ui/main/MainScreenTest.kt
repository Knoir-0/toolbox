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
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("From Unit (Meter)").assertExists()

    // Panel auto-collapsed back to one row; pull up to select next tool
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Guitar Tuner").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("TARGET").assertExists()

    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Metronome").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Play").assertExists()

    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Color Picker").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("2D Color Canvas").assertExists()

    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Ruler").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Caliper (A↔B)").assertExists()
    composeTestRule.onNodeWithTag("screen_edge_ruler_canvas").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ruler_unit_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ruler_unit_toggle").performClick()

    // Pull up and navigate to Page 1: Calculator is in Row 2 on Page 1
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Calculator").onFirst().performClick()
    composeTestRule.waitForIdle()
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
  fun pullUpPanel_dragToolToRightEdge_flipsToNextPageAndMovesTool() {
    // Open the pull up panel (expanded)
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Page 0 initially shows Dice Roller; Page 1 rows (row 2 and row 3) do not exist on Page 0
    composeTestRule.onNodeWithTag("tool_item_DICE_ROLLER").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_2").assertDoesNotExist()

    // Drag Dice Roller (col 0, row 0) to the far right edge of tools_grid and hold to trigger page switch
    composeTestRule.onNodeWithTag("tools_grid").performTouchInput {
      val start = Offset(width / 6f, height / 6f)
      down(start)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      // Drag near the right edge
      moveTo(Offset(width - 5f, height / 6f))
      // Hold in edge zone for dwell duration (400ms) + buffer
      advanceEventTime(600L)
      up()
    }
    composeTestRule.waitForIdle()

    // Pager has switched to Page 1! Row 2 and Row 3 are now displayed
    composeTestRule.onNodeWithTag("tools_grid_row_2").assertIsDisplayed()

    // Dice Roller was moved to Page 1 and is displayed on Page 1!
    composeTestRule.onNodeWithTag("tool_item_DICE_ROLLER").assertIsDisplayed()

    // Preference was updated and persisted
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    val savedOrder = repo.getToolOrder()
    val diceIndex = savedOrder.indexOf(Tool.DICE_ROLLER)
    // On Page 1 with 3/row and 2 rows/page, Page 1 indices start at index 6
    assertTrue("Dice Roller should be on Page 1 (index >= 6), but was at $diceIndex", diceIndex >= 6)
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
    composeTestRule.onNodeWithTag("tool_item_AUDIO_RECORDER").assertIsDisplayed()

    // Navigate to Page 2 via indicator dot where Settings is located
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").assertIsDisplayed()

    // Select Settings on Page 2 (automatically collapses back to one row)
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Since Settings (Row 4) was selected, collapsed panel shows Row 4
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").assertIsDisplayed()
  }

  @Test
  fun clickingTool_automaticallyCollapsesPanelBackToOneRow() {
    // Pull up the panel to expand it
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()

    // Click on a tool
    composeTestRule.onAllNodesWithText("Unit Converter").onFirst().performClick()
    composeTestRule.waitForIdle()

    // Panel has automatically collapsed back to one row (row controls are hidden)
    composeTestRule.onNodeWithTag("tools_per_row_control").assertDoesNotExist()
    composeTestRule.onNodeWithText("From Unit (Meter)").assertExists()
  }

  @Test
  fun categorySorting_paginatesByCategoryAndDisplaysLabel() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    repo.setToolsPerRow(3)
    composeTestRule.waitForIdle()

    // Verify category label is displayed
    composeTestRule.onNodeWithTag("category_label").assertIsDisplayed()

    // With 3 tools per row:
    // Page 0: Math & Measurement (3 tools)
    // Page 1: Audio & Music (2 tools)
    // Page 2: Media & Creative (2 tools)
    // Page 3: Productivity (2 tools)
    // Page 4: Entertainment & Devices (2 tools) + System (1 tool) = 3 tools fit on 1 row!
    val systemPageIndex = 4
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$systemPageIndex").performClick()
    composeTestRule.waitForIdle()

    // Verify System label and Settings tool
    composeTestRule.onNodeWithText("System").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Verify settings screen shows Default Sorting card with both options
    composeTestRule.onNodeWithTag("toolbox_sorting_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sorting_option_user_choice").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sorting_option_tool_categories").assertIsDisplayed()

    // Click Manual
    composeTestRule.onNodeWithTag("sorting_option_user_choice").performClick()
    composeTestRule.waitForIdle()
    assertEquals(ToolSortingOption.USER_CHOICE, repo.getToolSorting())

    // Click Tool categories
    composeTestRule.onNodeWithTag("sorting_option_tool_categories").performClick()
    composeTestRule.waitForIdle()
    assertEquals(ToolSortingOption.TOOL_CATEGORIES, repo.getToolSorting())

    // Check Math & Measurement category (Page 0)
    val mathIndex = 0
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$mathIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Math & Measurement").assertIsDisplayed()
    composeTestRule.onNodeWithText("Calculator").assertIsDisplayed()
    composeTestRule.onNodeWithText("Unit Converter").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ruler").assertIsDisplayed()

    // Check Audio & Music category (Page 1)
    val musicIndex = 1
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$musicIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Audio & Music").assertIsDisplayed()
    composeTestRule.onNodeWithText("Guitar Tuner").assertIsDisplayed()
    composeTestRule.onNodeWithText("Metronome").assertIsDisplayed()
  }

  @Test
  fun categorySorting_multipleCategoriesFitOnOnePageAsOneRow_whenToolsPerRowIncreases() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    repo.setToolsPerRow(4)
    composeTestRule.waitForIdle()

    // With 4 tools per row:
    // Page 0: Math & Measurement (3 tools)
    // Page 1: Audio & Music (3 tools)
    // Page 2: Media & Creative (3 tools)
    // Page 3: Productivity (2 tools) + Entertainment & Devices (2 tools) = 4 tools on 1 row!
    // Page 4: System (1 tool)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_3").performClick()
    composeTestRule.waitForIdle()

    // Both Productivity and Entertainment & Devices fit on Page 3 as one row
    composeTestRule.onNodeWithText("Productivity").assertIsDisplayed()
    composeTestRule.onNodeWithText("Entertainment & Devices").assertIsDisplayed()
    composeTestRule.onNodeWithText("To-Do").assertIsDisplayed()
    composeTestRule.onNodeWithText("Files").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dice Roller").assertIsDisplayed()
    composeTestRule.onNodeWithText("TV Remote").assertIsDisplayed()

    // Pull up the panel to reveal the tools per row slider
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_per_row_count").assertTextEquals("4")
  }

  @Test
  fun categorySorting_maxToolsPerRowSix_settingsToolIconDoesNotShrink() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    repo.setToolsPerRow(6)
    composeTestRule.waitForIdle()

    // With 6 tools per row:
    // Page 0: Math & Measurement (3 tools) + Audio & Music (2 tools) = 5 tools
    // Page 1: Media & Creative (2 tools) + Productivity (2 tools) + Entertainment & Devices (2 tools) = 6 tools
    // Page 2: System (1 tool: Settings)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()

    // Verify Settings tool icon is displayed and has sufficient width
    composeTestRule.onNodeWithText("System").assertIsDisplayed()
    val settingsNode = composeTestRule.onNodeWithTag("tool_item_SETTINGS")
    settingsNode.assertIsDisplayed()
    val bounds = settingsNode.fetchSemanticsNode().boundsInRoot
    // Item width should span a full column slot and not be shrunk by double side spacers
    assertTrue(bounds.width > 100f)
  }

  @Test
  fun usageSorting_ordersToolsByUsageCountAndUpdatesOnToolSelection() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)

    // Simulate usage: Calculator used 20 times, Metronome used 10 times, Ruler used 5 times
    repeat(20) { repo.incrementToolUsage(Tool.CALCULATOR) }
    repeat(10) { repo.incrementToolUsage(Tool.METRONOME) }
    repeat(5) { repo.incrementToolUsage(Tool.RULER) }

    // Switch to USAGE sorting
    repo.setToolSorting(ToolSortingOption.USAGE)
    composeTestRule.waitForIdle()

    // Row 0 of tools grid should now display Calculator, Metronome, Ruler as the top 3 most used
    composeTestRule.onNodeWithText("Calculator").assertIsDisplayed()
    composeTestRule.onNodeWithText("Metronome").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ruler").assertIsDisplayed()

    val calcNode = composeTestRule.onNodeWithTag("tool_item_CALCULATOR")
    val metronomeNode = composeTestRule.onNodeWithTag("tool_item_METRONOME")
    val rulerNode = composeTestRule.onNodeWithTag("tool_item_RULER")

    val calcLeft = calcNode.fetchSemanticsNode().boundsInRoot.left
    val metronomeLeft = metronomeNode.fetchSemanticsNode().boundsInRoot.left
    val rulerLeft = rulerNode.fetchSemanticsNode().boundsInRoot.left

    // Verify ordering from left to right: Calculator (20) < Metronome (10) < Ruler (5)
    assertTrue(calcLeft < metronomeLeft)
    assertTrue(metronomeLeft < rulerLeft)

    // Selecting a tool in the panel increments its usage
    val initialDiceUsage = repo.getToolUsageCounts()[Tool.DICE_ROLLER] ?: 0
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Page 1 contains less used tools
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tool_item_DICE_ROLLER").performClick()
    composeTestRule.waitForIdle()

    val updatedDiceUsage = repo.getToolUsageCounts()[Tool.DICE_ROLLER] ?: 0
    assertEquals(initialDiceUsage + 1, updatedDiceUsage)
  }

  @Test
  fun toolVisibility_collapsiblePanelTogglesExpansion() {
    // Expand panel and navigate to Settings
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Page 2 contains Settings in manual mode
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Tool visibility card is displayed
    composeTestRule.onNodeWithTag("tool_visibility_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_visibility_toggle").assertIsDisplayed()

    // Initially collapsed: rows are not visible
    composeTestRule.onNodeWithTag("tool_visibility_row_CALCULATOR").assertDoesNotExist()

    // Click toggle to expand
    composeTestRule.onNodeWithTag("tool_visibility_toggle").performClick()
    composeTestRule.waitForIdle()

    // Now expanded: row exists
    composeTestRule.onNodeWithTag("tool_visibility_row_CALCULATOR").assertExists()

    // Click toggle to collapse again
    composeTestRule.onNodeWithTag("tool_visibility_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tool_visibility_row_CALCULATOR").assertDoesNotExist()
  }

  @Test
  fun alphabeticalSorting_showsOrderPillSelectorAndSortsTools() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)

    // Expand panel and navigate to Settings
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Page 2 contains Settings in manual mode
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Alphabetical sort option button is displayed with "Alpha" text
    composeTestRule.onNodeWithTag("sorting_option_alphabetical").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sorting_option_alpha").assertIsDisplayed()

    // Ascending/Descending pill selector is not visible initially (when manual mode)
    composeTestRule.onNodeWithTag("sort_order_ascending").assertDoesNotExist()

    // Click Alphabetical
    composeTestRule.onNodeWithTag("sorting_option_alphabetical").performClick()
    composeTestRule.waitForIdle()

    assertEquals(ToolSortingOption.ALPHABETICAL, repo.getToolSorting())

    // Ascending and Descending pill buttons should now pop up
    composeTestRule.onNodeWithTag("sort_order_ascending").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sort_order_descending").assertIsDisplayed()

    // Click Descending
    composeTestRule.onNodeWithTag("sort_order_descending").performClick()
    composeTestRule.waitForIdle()

    assertEquals(ToolSortOrder.DESCENDING, repo.getToolSortOrder())

    // Click Ascending
    composeTestRule.onNodeWithTag("sort_order_ascending").performClick()
    composeTestRule.waitForIdle()

    assertEquals(ToolSortOrder.ASCENDING, repo.getToolSortOrder())
  }

  @Test
  fun categorySorting_showsCategorySortingPillSelectorAndSorts() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)

    // Expand panel and navigate to Settings
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Page 2 contains Settings in manual mode
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Category sorting pill selector is not visible initially in manual mode
    composeTestRule.onNodeWithTag("category_sorting_manual").assertDoesNotExist()

    // Click Categories
    composeTestRule.onNodeWithTag("sorting_option_tool_categories").performClick()
    composeTestRule.waitForIdle()

    assertEquals(ToolSortingOption.TOOL_CATEGORIES, repo.getToolSorting())

    // Category sorting pill selector buttons should now pop up
    composeTestRule.onNodeWithTag("category_sorting_manual").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_sorting_alpha").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_sorting_usage").assertIsDisplayed()

    // Click Alpha
    composeTestRule.onNodeWithTag("category_sorting_alpha").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CategorySortingOption.ALPHABETICAL, repo.getCategorySorting())

    // Click Usage
    composeTestRule.onNodeWithTag("category_sorting_usage").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CategorySortingOption.USAGE, repo.getCategorySorting())

    // Click Manual
    composeTestRule.onNodeWithTag("category_sorting_manual").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CategorySortingOption.MANUAL, repo.getCategorySorting())

    // Tools within categories pill selector buttons should also pop up
    composeTestRule.onNodeWithTag("category_tool_sorting_manual").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_tool_sorting_alpha").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_tool_sorting_usage").assertIsDisplayed()

    // Click Alpha for tools within categories
    composeTestRule.onNodeWithTag("category_tool_sorting_alpha").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CategoryToolSortingOption.ALPHABETICAL, repo.getCategoryToolSorting())

    // Click Usage for tools within categories
    composeTestRule.onNodeWithTag("category_tool_sorting_usage").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CategoryToolSortingOption.USAGE, repo.getCategoryToolSorting())

    // Click Manual for tools within categories
    composeTestRule.onNodeWithTag("category_tool_sorting_manual").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CategoryToolSortingOption.MANUAL, repo.getCategoryToolSorting())
  }

  @Test
  fun toolSettingsSection_collapsibleCategoriesAndAddToHomeScreen() {
    // Navigate to Settings
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Tool settings card & toggle exist
    composeTestRule.onNodeWithTag("tool_settings_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_settings_toggle").assertIsDisplayed()

    // Expand Tool Settings
    composeTestRule.onNodeWithTag("tool_settings_toggle").performClick()
    composeTestRule.waitForIdle()

    // Category headers should exist
    composeTestRule.onNodeWithTag("category_header_math_and_measurement").assertExists()
    composeTestRule.onNodeWithTag("category_header_audio_and_music").assertExists()
    composeTestRule.onNodeWithTag("category_header_system").assertExists()

    // Categories are collapsed by default, so Calculator tool row should not exist yet
    composeTestRule.onNodeWithTag("tool_settings_row_CALCULATOR").assertDoesNotExist()

    // Expand Math & Measurement category
    composeTestRule.onNodeWithTag("category_header_math_and_measurement").performClick()
    composeTestRule.waitForIdle()

    // Calculator tool row and add to homescreen button should now be visible
    composeTestRule.onNodeWithTag("tool_settings_row_CALCULATOR").assertExists()
    composeTestRule.onNodeWithTag("add_to_home_CALCULATOR").assertExists()

    // Re-collapse Math & Measurement category
    composeTestRule.onNodeWithTag("category_header_math_and_measurement").performClick()
    composeTestRule.waitForIdle()

    // Calculator row should now be collapsed/not visible again
    composeTestRule.onNodeWithTag("tool_settings_row_CALCULATOR").assertDoesNotExist()
  }

  @Test
  fun standaloneTool_launchesWithoutNavigationBar() {
    composeTestRule.setContent {
      MainScreen(standaloneTool = Tool.CALCULATOR)
    }
    composeTestRule.waitForIdle()

    // Standalone tool screen is displayed
    composeTestRule.onNodeWithTag("standalone_tool_screen_CALCULATOR").assertIsDisplayed()

    // Navigation pull-up panel and drawer are completely hidden
    composeTestRule.onNodeWithTag("tools_pull_up_panel").assertDoesNotExist()
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").assertDoesNotExist()
  }
}
