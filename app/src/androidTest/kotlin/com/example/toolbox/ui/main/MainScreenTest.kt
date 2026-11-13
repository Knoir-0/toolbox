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
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
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
    composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()
    composeTestRule.onNodeWithText("Notes").assertIsDisplayed()
    composeTestRule.onNodeWithText("To Do").assertIsDisplayed()
  }

  @Test
  fun pullUpPanel_switchesSelectedTool() {
    composeTestRule.onNodeWithTag("calculator_screen").assertExists()

    // Expand the pull-up panel so Page 0 rows (Row 0 and Row 1) are visible
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Notes").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("notes_empty_state").assertExists()

    // Panel auto-collapsed back to one row; pull up and navigate to Page 1 for Units and Ruler
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Units").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("From Unit (Meter)").assertExists()

    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Ruler").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Caliper (A↔B)").assertExists()
    composeTestRule.onNodeWithTag("screen_edge_ruler_canvas").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ruler_unit_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ruler_unit_toggle").performClick()

    // Unhide Colors and Metronome to verify switching to them
    DefaultNavigationPreferencesRepository(composeTestRule.activity)
      .setHiddenTools(DEFAULT_HIDDEN_TOOLS - Tool.COLOR_PICKER - Tool.METRONOME)
    composeTestRule.waitForIdle()

    // Pull up and navigate to Page 2 for Guitar Tuner and Metronome
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Tuner").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("TARGET").assertExists()

    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Metronome").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Play").assertExists()

    // Pull up and navigate to Page 3 for Colors
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_3").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Colors").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("2D Color Canvas").assertExists()
  }

  @Test
  fun rowControls_hiddenUntilPulledUp_andSliderAdjustsFromThreeToSix_andHelpButtonShowsExplanation() {
    // Row controls are hidden when collapsed like a native navigation bar
    composeTestRule.onNodeWithTag("tools_per_row_control").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tools_per_row_slider").assertDoesNotExist()
    composeTestRule.onNodeWithTag("add_to_home_screen_button").assertDoesNotExist()

    // Pull up the navigation box
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Row controls, add-to-home-screen button, and slider with dots (3 - 6) are now visible
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()
    composeTestRule.onNodeWithTag("add_to_home_screen_button").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Tools per row").assertDoesNotExist()
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
  fun pullUpPanel_addToHomeScreenButton_isLeftOfSliderAndTargetsSelectedTool() {
    // Expand the pull-up panel
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    val addButton = composeTestRule.onNodeWithTag("add_to_home_screen_button")
    val slider = composeTestRule.onNodeWithTag("tools_per_row_slider")
    addButton.assertIsDisplayed()
    slider.assertIsDisplayed()

    // Old GridView icon in that position is removed
    composeTestRule.onNodeWithContentDescription("Tools per row").assertDoesNotExist()

    // Button is positioned to the left of the tools-per-row slider
    val addButtonBounds = addButton.fetchSemanticsNode().boundsInRoot
    val sliderBounds = slider.fetchSemanticsNode().boundsInRoot
    assertTrue(addButtonBounds.right <= sliderBounds.left)

    // Initially Calculate is selected
    composeTestRule.onNodeWithContentDescription("Add Calculate to Home screen").assertIsDisplayed()
    addButton.performClick()
    composeTestRule.waitForIdle()

    // Switch to Notes and re-expand the panel
    composeTestRule.onAllNodesWithText("Notes").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Button now targets the newly selected tool (Notes)
    composeTestRule.onNodeWithContentDescription("Add Notes to Home screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("add_to_home_screen_button").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun pullUpPanel_longPressAndDrag_reordersTools() {
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    val calcNode = composeTestRule.onNodeWithTag("tool_item_CALCULATOR")
    val notesNode = composeTestRule.onNodeWithTag("tool_item_NOTES")

    val initialCalcLeft = calcNode.fetchSemanticsNode().boundsInRoot.left
    val initialNotesLeft = notesNode.fetchSemanticsNode().boundsInRoot.left
    assertTrue(initialCalcLeft < initialNotesLeft)

    val dragDeltaX = notesNode.fetchSemanticsNode().boundsInRoot.center.x -
      calcNode.fetchSemanticsNode().boundsInRoot.center.x

    composeTestRule.onNodeWithTag("tools_grid").performTouchInput {
      val start = Offset(width / 6f, height / 6f)
      down(start)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      moveBy(Offset(dragDeltaX, 0f))
      advanceEventTime(100L)
      up()
    }
    composeTestRule.waitForIdle()

    val newCalcLeft = calcNode.fetchSemanticsNode().boundsInRoot.left
    val newNotesLeft = notesNode.fetchSemanticsNode().boundsInRoot.left
    assertTrue(newNotesLeft < newCalcLeft)
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

    val calcNode = composeTestRule.onNodeWithTag("tool_item_CALCULATOR")
    val notesNode = composeTestRule.onNodeWithTag("tool_item_NOTES")

    val dragDeltaX = notesNode.fetchSemanticsNode().boundsInRoot.center.x -
      calcNode.fetchSemanticsNode().boundsInRoot.center.x

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
    val calcIndex = savedOrder.indexOf(Tool.CALCULATOR)
    val notesIndex = savedOrder.indexOf(Tool.NOTES)
    assertTrue(notesIndex < calcIndex)
  }

  @Test
  fun pullUpPanel_dragToolToRightEdge_flipsToNextPageAndMovesTool() {
    // Open the pull up panel (expanded)
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Page 0 initially shows Calculator; Page 1 rows (row 2 and row 3) do not exist on Page 0
    composeTestRule.onNodeWithTag("tool_item_CALCULATOR").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_2").assertDoesNotExist()

    // Drag Calculator (col 0, row 0) to the far right edge of tools_grid and hold to trigger page switch
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

    // Calculator was moved to Page 1 and is displayed on Page 1!
    composeTestRule.onNodeWithTag("tool_item_CALCULATOR").assertIsDisplayed()

    // Preference was updated and persisted
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    val savedOrder = repo.getToolOrder()
    val calcIndex = savedOrder.indexOf(Tool.CALCULATOR)
    // On Page 1 with 3/row and 2 rows/page, Page 1 indices start at index 6
    assertTrue("Calculator should be on Page 1 (index >= 6), but was at $calcIndex", calcIndex >= 6)
  }

  @Test
  fun collapsedPanel_swipeBetweenRows_allowsNavigatingAndSelectingTools() {
    // Initially on Row 0
    composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()
    composeTestRule.onNodeWithText("Notes").assertIsDisplayed()
    composeTestRule.onNodeWithText("To Do").assertIsDisplayed()
    composeTestRule.onNodeWithText("Camera").assertDoesNotExist()

    // Row indicator dots exist
    composeTestRule.onNodeWithTag("tools_panel_row_indicator").assertIsDisplayed()

    // Swipe left between rows on the collapsed navigation panel
    composeTestRule.onNodeWithTag("tools_grid").performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()

    // Now on Row 1: Camera, Gallery, Files are displayed
    composeTestRule.onNodeWithText("Camera").assertIsDisplayed()
    composeTestRule.onNodeWithText("Gallery").assertIsDisplayed()
    composeTestRule.onNodeWithText("Files").assertIsDisplayed()
    composeTestRule.onNodeWithText("Calculate").assertDoesNotExist()

    // Selecting a tool on Row 1 switches to that tool
    composeTestRule.onNodeWithText("Camera").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_screen").assertExists()

    // Tapping dot 0 in the row indicator jumps back to Row 0
    composeTestRule.onNodeWithTag("tools_panel_row_indicator_dot_0").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()
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

    // Navigate to last page via indicator dot where Settings is located
    val lastPageIndex = (Tool.entries.size - DEFAULT_HIDDEN_TOOLS.size - 1) / (DEFAULT_TOOLS_PER_ROW * ROWS_PER_PAGE_EXPANDED)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$lastPageIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").assertIsDisplayed()

    // Select Settings on last page (automatically collapses back to one row)
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Since Settings was selected, collapsed panel shows Settings row
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").assertIsDisplayed()
  }

  @Test
  fun clickingTool_automaticallyCollapsesPanelBackToOneRow() {
    // Pull up the panel to expand it
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()

    // Click on a tool
    composeTestRule.onAllNodesWithText("Notes").onFirst().performClick()
    composeTestRule.waitForIdle()

    // Panel has automatically collapsed back to one row (row controls are hidden)
    composeTestRule.onNodeWithTag("tools_per_row_control").assertDoesNotExist()
    composeTestRule.onNodeWithTag("notes_empty_state").assertExists()
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
    // Page 1: Productivity (2 tools) + Files (1 tool)
    // Page 2: Photo & Video (2 tools)
    // Page 3: Audio & Sound (2 tools)
    // Page 4: Music & Instruments (2 tools)
    // Page 5: Remotes & Devices (2 tools)
    // Page 6: Miscellaneous (3 tools: Dice, Plants, Recipes)
    // Page 7: System (1 tool: Settings)
    val systemPageIndex = 7
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
    composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()
    composeTestRule.onNodeWithText("Units").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ruler").assertIsDisplayed()

    // Check Music & Instruments category
    val musicIndex = 4
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$musicIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Music & Instruments").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tuner").assertIsDisplayed()
    composeTestRule.onNodeWithText("Chords").assertIsDisplayed()
    composeTestRule.onNodeWithText("Metronome").assertDoesNotExist()
  }

  @Test
  fun categorySorting_dragUpOnDragHandle_revealsSecondRowOfCategories() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    repo.setToolsPerRow(3)
    composeTestRule.waitForIdle()

    // Initially collapsed: only Row 0 (Math & Measurement) is visible; Row 1 does not exist
    composeTestRule.onNodeWithTag("tools_grid_row_0").assertIsDisplayed()
    composeTestRule.onNodeWithText("Math & Measurement").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_1").assertDoesNotExist()
    composeTestRule.onNodeWithText("Productivity").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertDoesNotExist()

    // Drag up on the drag handle to reveal the second row of categories + controls
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performTouchInput {
      swipeUp()
    }
    composeTestRule.waitForIdle()

    // Both Row 0 and Row 1 of categories are now displayed simultaneously
    composeTestRule.onNodeWithTag("tools_grid_row_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Math & Measurement").assertIsDisplayed()
    composeTestRule.onNodeWithText("Productivity").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_label_files_and_storage").assertTextEquals("Files").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()

    // Navigate to Page 1 while expanded -> shows Row 2 (Photo & Video) and Row 3 (Audio & Sound)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_grid_row_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_3").assertIsDisplayed()
    composeTestRule.onNodeWithText("Photo & Video").assertIsDisplayed()
    composeTestRule.onNodeWithText("Audio & Sound").assertIsDisplayed()

    // Drag down on the drag handle to return to single-row category state
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performTouchInput {
      swipeDown()
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tools_grid_row_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_3").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertDoesNotExist()
  }

  @Test
  fun categorySorting_multipleCategoriesFitOnOnePageAsOneRow_whenToolsPerRowIncreases() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    repo.setToolsPerRow(6)
    composeTestRule.waitForIdle()

    // With 6 tools per row:
    // Row 0: Math & Measurement (3 tools) + Productivity (2 tools) + Files (1 tool) = 6 tools
    // Row 1: Photo & Video (2 tools) + Audio & Sound (2 tools) + Music & Instruments (2 tools) = 6 tools
    // Row 2: Remotes & Devices (2 tools) + Miscellaneous (3 tools) + System (1 tool: Settings) = 6 tools on 1 row!
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()

    // Remotes & Devices, Miscellaneous, and System fit on Page 2 as one row
    composeTestRule.onNodeWithText("Remotes & Devices").assertIsDisplayed()
    composeTestRule.onNodeWithText("Miscellaneous").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dice").assertIsDisplayed()
    composeTestRule.onNodeWithText("System").assertIsDisplayed()
    composeTestRule.onNodeWithText("Settings").assertIsDisplayed()

    // Pull up the panel to reveal the tools per row slider
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("System").assertIsDisplayed()
    composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_per_row_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_per_row_count").assertTextEquals("6")
  }

  @Test
  fun categorySorting_maxToolsPerRowSix_settingsToolIconDoesNotShrink() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    repo.setToolsPerRow(6)
    composeTestRule.waitForIdle()

    // With 6 tools per row:
    // Row 0: Math & Measurement (3 tools) + Productivity (2 tools) + Files (1 tool) = 6 tools
    // Row 1: Photo & Video (2 tools) + Audio & Sound (2 tools) + Music & Instruments (2 tools) = 6 tools
    // Row 2: Remotes & Devices (2 tools) + Miscellaneous (3 tools) + System (1 tool: Settings) = 6 tools
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
    repo.setHiddenTools(DEFAULT_HIDDEN_TOOLS - Tool.METRONOME)

    // Simulate usage: Calculator used 20 times, Metronome used 10 times, Ruler used 5 times
    repeat(20) { repo.incrementToolUsage(Tool.CALCULATOR) }
    repeat(10) { repo.incrementToolUsage(Tool.METRONOME) }
    repeat(5) { repo.incrementToolUsage(Tool.RULER) }

    // Switch to USAGE sorting
    repo.setToolSorting(ToolSortingOption.USAGE)
    composeTestRule.waitForIdle()

    // Row 0 of tools grid should now display Calculator, Metronome, Ruler as the top 3 most used
    composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()
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

    // Page 2 contains Dice Roller
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
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

    val lastPageIndex = (Tool.entries.size - DEFAULT_HIDDEN_TOOLS.size - 1) / (DEFAULT_TOOLS_PER_ROW * ROWS_PER_PAGE_EXPANDED)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$lastPageIndex").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Tool visibility card is displayed and expanded by default
    composeTestRule.onNodeWithTag("tool_visibility_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_visibility_toggle").assertIsDisplayed()

    // Initially expanded: category headers exist while individual tool rows are collapsed inside categories
    composeTestRule.onNodeWithTag("category_header_math_and_measurement").assertExists()
    composeTestRule.onNodeWithTag("tool_visibility_row_CALCULATOR").assertDoesNotExist()

    // Click toggle to collapse Tool Settings
    composeTestRule.onNodeWithTag("tool_visibility_toggle").performClick()
    composeTestRule.waitForIdle()

    // Now collapsed: category headers do not exist
    composeTestRule.onNodeWithTag("category_header_math_and_measurement").assertDoesNotExist()

    // Click toggle to expand again
    composeTestRule.onNodeWithTag("tool_visibility_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("category_header_math_and_measurement").assertExists()
  }

  @Test
  fun alphabeticalSorting_showsOrderPillSelectorAndSortsTools() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)

    // Expand panel and navigate to Settings
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    val lastPageIndex = (Tool.entries.size - DEFAULT_HIDDEN_TOOLS.size - 1) / (DEFAULT_TOOLS_PER_ROW * ROWS_PER_PAGE_EXPANDED)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$lastPageIndex").performClick()
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

    val lastPageIndex = (Tool.entries.size - DEFAULT_HIDDEN_TOOLS.size - 1) / (DEFAULT_TOOLS_PER_ROW * ROWS_PER_PAGE_EXPANDED)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$lastPageIndex").performClick()
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
    val lastPageIndex = (Tool.entries.size - DEFAULT_HIDDEN_TOOLS.size - 1) / (DEFAULT_TOOLS_PER_ROW * ROWS_PER_PAGE_EXPANDED)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$lastPageIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Tool settings card & toggle exist and are expanded by default
    composeTestRule.onNodeWithTag("tool_settings_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_settings_toggle").assertIsDisplayed()

    // Category headers and their tool/hidden count badges should exist by default without tapping toggle
    composeTestRule.onNodeWithTag("category_header_math_and_measurement").assertExists()
    composeTestRule.onNodeWithTag("category_tool_count_math_and_measurement").assertExists()
    composeTestRule.onNodeWithTag("category_hidden_count_math_and_measurement").assertExists()
    composeTestRule.onNodeWithTag("category_header_music_and_instruments").assertExists()
    composeTestRule.onNodeWithTag("category_tool_count_music_and_instruments").assertExists()
    composeTestRule.onNodeWithTag("category_hidden_count_music_and_instruments").assertExists()
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
  fun toolSettingsSection_tappingToolIconOrText_selectsToolRegardlessOfVisibility() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    // Metronome is hidden by default; Guitar Tuner is visible by default
    assertTrue(Tool.METRONOME in repo.getHiddenTools())
    assertTrue(Tool.GUITAR_TUNER !in repo.getHiddenTools())

    // Navigate to Settings
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    val lastPageIndex = (Tool.entries.size - DEFAULT_HIDDEN_TOOLS.size - 1) / (DEFAULT_TOOLS_PER_ROW * ROWS_PER_PAGE_EXPANDED)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$lastPageIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Expand Music & Instruments category in Tool Settings
    composeTestRule.onNodeWithTag("category_header_music_and_instruments").performClick()
    composeTestRule.waitForIdle()

    // Tap the icon of a visible tool (Guitar Tuner) -> selects Guitar Tuner without hiding it
    composeTestRule.onNodeWithTag("tool_settings_icon_GUITAR_TUNER").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("TARGET").assertExists()
    assertTrue(Tool.GUITAR_TUNER !in repo.getHiddenTools())

    // Navigate back to Settings and expand Music & Instruments category
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$lastPageIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_header_music_and_instruments").performClick()
    composeTestRule.waitForIdle()

    // Tap the text of a hidden tool (Metronome) -> selects Metronome while keeping it hidden
    composeTestRule.onNodeWithTag("tool_settings_text_METRONOME").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Play").assertExists()
    assertTrue(Tool.METRONOME in repo.getHiddenTools())
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

  @Test
  fun filesTool_supportedFileMenuShowsFileConverter_omitsOpen_retainsOpenWith_andOpensConverterDirectly() {
    val tempDir = java.io.File(composeTestRule.activity.cacheDir, "files_menu_test_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      java.io.File(tempDir, "sample.png").writeBytes(byteArrayOf(1, 2, 3, 4))
      java.io.File(tempDir, "unknown.unrecognizedext").writeBytes(byteArrayOf(5, 6, 7, 8))

      composeTestRule.setContent {
        com.example.toolbox.ui.tools.FilesScreen(initialDirectory = tempDir)
      }
      composeTestRule.waitForIdle()

      // Open options menu for supported file (sample.png)
      composeTestRule.onNodeWithTag("file_options_sample.png").performClick()
      composeTestRule.waitForIdle()

      // "Open" option is removed, "Open with…" is retained, and "File Converter" is displayed
      composeTestRule.onNodeWithTag("action_open_file").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_open_with").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_file_converter").assertIsDisplayed()

      // Click "File Converter" -> opens FileConverterScreen directly with sample.png preselected
      composeTestRule.onNodeWithTag("action_file_converter").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("file_converter_screen").assertIsDisplayed()
      composeTestRule.onNodeWithText("sample.png").assertIsDisplayed()

      // Navigate back to FilesScreen
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("files_screen").assertIsDisplayed()

      // Open options menu for unsupported file (unknown.unrecognizedext)
      composeTestRule.onNodeWithTag("file_options_unknown.unrecognizedext").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("action_open_file").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_open_with").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_file_converter").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_image").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_video").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_audio").assertDoesNotExist()
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun filesAndGalleryTools_editButtonOpensImageEditorAndVideoEditorDirectly() {
    val tempDir = java.io.File(composeTestRule.activity.cacheDir, "media_editor_test_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      val dcimDir = java.io.File(tempDir, "DCIM").apply { mkdirs() }
      com.example.toolbox.ui.tools.GalleryEngine.writeSolidColorPng(
        outputFile = java.io.File(dcimDir, "photo.png"),
      )
      java.io.File(dcimDir, "clip.mp4").writeBytes(byteArrayOf(0, 0, 0, 24))
      com.example.toolbox.ui.tools.AudioEditorEngine.writeWavFile(
        buffer = com.example.toolbox.ui.tools.AudioEditorEngine.createSampleAudioBuffer(durationMs = 1000L),
        outputFile = java.io.File(dcimDir, "track.wav"),
      )

      // 1. Test FilesScreen Edit button for Image, Video & Audio
      composeTestRule.setContent {
        com.example.toolbox.ui.tools.FilesScreen(initialDirectory = dcimDir)
      }
      composeTestRule.waitForIdle()

      // Image file menu shows Edit button (action_edit_image) and opens ImageEditorScreen
      composeTestRule.onNodeWithTag("file_options_photo.png").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_edit_image").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_edit_video").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_audio").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_image").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("image_editor_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("files_screen").assertIsDisplayed()

      // Video file menu shows Edit button (action_edit_video) and opens VideoEditorScreen
      composeTestRule.onNodeWithTag("file_options_clip.mp4").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_edit_video").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_edit_image").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_audio").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_video").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("video_editor_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("files_screen").assertIsDisplayed()

      // Audio file menu shows Edit button (action_edit_audio) and opens AudioEditorScreen
      composeTestRule.onNodeWithTag("file_options_track.wav").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_edit_audio").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_edit_image").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_video").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_open_archive").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_audio").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("audio_editor_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("files_screen").assertIsDisplayed()

      // Archive file menu shows Archive button (action_open_archive) and opens ArchiveScreen
      com.example.toolbox.ui.tools.ArchiveEngine.generateSampleArchive(dcimDir, "bundle.zip")
      composeTestRule.setContent {
        com.example.toolbox.ui.tools.FilesScreen(initialDirectory = dcimDir)
      }
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("file_options_bundle.zip").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_open_archive").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_edit_image").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_video").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_audio").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_open_archive").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("archive_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("archive_explorer_view").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("files_screen").assertIsDisplayed()

      // PDF file menu shows PDF Viewer button (action_open_pdf) and opens PdfViewerScreen
      com.example.toolbox.ui.tools.PdfViewerEngine.generateSamplePdfFile(java.io.File(dcimDir, "doc.pdf"))
      composeTestRule.setContent {
        com.example.toolbox.ui.tools.FilesScreen(initialDirectory = dcimDir)
      }
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("file_options_doc.pdf").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_open_pdf").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_edit_image").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_video").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_edit_audio").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_open_pdf").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("pdf_viewer_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("files_screen").assertIsDisplayed()

      // 2. Test GalleryScreen Edit button for Image & Video
      composeTestRule.setContent {
        com.example.toolbox.ui.tools.GalleryScreen(rootDirectory = tempDir)
      }
      composeTestRule.waitForIdle()

      // Open photo viewer and click Edit button for photo.png -> opens ImageEditorScreen
      composeTestRule.onNodeWithTag("photo_tile_item_photo.png").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("photo_viewer_edit_button").assertIsDisplayed()
      composeTestRule.onNodeWithTag("photo_viewer_edit_button").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("image_editor_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("gallery_screen").assertIsDisplayed()

      // Click tile Edit button for clip.mp4 -> opens VideoEditorScreen
      composeTestRule.onNodeWithTag("photo_tile_edit_clip.mp4").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("video_editor_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("gallery_screen").assertIsDisplayed()
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun filesTool_backPressAlwaysGoesUpAFolderUnlessAtRoot_andActsAsNormalAtRoot() {
    val rootDir = java.io.File(composeTestRule.activity.cacheDir, "files_back_test_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      val folderA = java.io.File(rootDir, "FolderA").apply { mkdirs() }
      val folderB = java.io.File(folderA, "FolderB").apply { mkdirs() }
      java.io.File(folderB, "deep_note.txt").writeText("deep")

      var outerBackTriggered = false

      composeTestRule.setContent {
        androidx.activity.compose.BackHandler {
          outerBackTriggered = true
        }
        com.example.toolbox.ui.tools.FilesScreen(
          initialDirectory = folderB,
          rootDirectory = rootDir,
        )
      }
      composeTestRule.waitForIdle()

      // Started at nested FolderB -> header shows FolderB and Up button is displayed
      composeTestRule.onNodeWithText("FolderB").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_up").assertIsDisplayed()

      // 1st Back press -> goes up a folder to FolderA
      composeTestRule.runOnUiThread {
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
      }
      composeTestRule.waitForIdle()
      org.junit.Assert.assertFalse(outerBackTriggered)
      composeTestRule.onNodeWithText("FolderA").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_up").assertIsDisplayed()

      // 2nd Back press -> goes up a folder to rootDir
      composeTestRule.runOnUiThread {
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
      }
      composeTestRule.waitForIdle()
      org.junit.Assert.assertFalse(outerBackTriggered)
      composeTestRule.onNodeWithTag("action_navigate_up").assertDoesNotExist()
      composeTestRule.onNodeWithTag("file_item_FolderA").assertIsDisplayed()

      // 3rd Back press (once at root) -> BackHandler in FilesScreen is disabled so back acts as normal
      composeTestRule.runOnUiThread {
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
      }
      composeTestRule.waitForIdle()
      org.junit.Assert.assertTrue(outerBackTriggered)
    } finally {
      rootDir.deleteRecursively()
    }
  }

  @Test
  fun pullUpPanel_dragToBottom_leavesOnlyDragHandle_andRestoresOnDragOrTap() {
    val handleNode = composeTestRule.onNodeWithTag("pull_up_panel_toggle")
    handleNode.assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid").assertIsDisplayed()
    composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()

    val initialHandleTop = handleNode.fetchSemanticsNode().boundsInRoot.top

    // Drag the tool panel down to the bottom of the screen
    composeTestRule.onNodeWithTag("tools_pull_up_panel").performTouchInput {
      swipeDown()
    }
    composeTestRule.waitForIdle()

    // Only the drag handle remains displayed at the bottom of the screen
    handleNode.assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid").assertDoesNotExist()
    composeTestRule.onNodeWithText("Calculate").assertDoesNotExist()
    val minimizedHandleTop = handleNode.fetchSemanticsNode().boundsInRoot.top
    assertTrue(minimizedHandleTop > initialHandleTop)

    // Dragging the handle back up restores the single-row tool panel
    handleNode.performTouchInput {
      swipeUp()
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tools_grid").assertIsDisplayed()
    composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()

    // Dragging the handle down again minimizes to only the drag handle, and tapping restores it
    handleNode.performTouchInput {
      swipeDown()
    }
    composeTestRule.waitForIdle()
    handleNode.assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid").assertDoesNotExist()

    handleNode.performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_grid").assertIsDisplayed()
    composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()
  }

  @Test
  fun categorySorting_manualToolsWithinCategory_longPressAndDragReordersWithinCategoryAndPersists() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    repo.setCategoryToolSorting(CategoryToolSortingOption.MANUAL)
    repo.setToolsPerRow(3)
    composeTestRule.waitForIdle()

    // On Page 0 (Math & Measurement): Calculate, Units, Ruler
    val calcNode = composeTestRule.onNodeWithTag("tool_item_CALCULATOR")
    val unitsNode = composeTestRule.onNodeWithTag("tool_item_UNIT_CONVERTER")
    calcNode.assertIsDisplayed()
    unitsNode.assertIsDisplayed()

    val initialCalcLeft = calcNode.fetchSemanticsNode().boundsInRoot.left
    val initialUnitsLeft = unitsNode.fetchSemanticsNode().boundsInRoot.left
    assertTrue(initialCalcLeft < initialUnitsLeft)

    val gridBounds = composeTestRule.onNodeWithTag("tools_grid").fetchSemanticsNode().boundsInRoot
    val calcCenterInGrid = calcNode.fetchSemanticsNode().boundsInRoot.center - gridBounds.topLeft
    val dragDeltaX = unitsNode.fetchSemanticsNode().boundsInRoot.center.x -
      calcNode.fetchSemanticsNode().boundsInRoot.center.x

    composeTestRule.onNodeWithTag("tools_grid").performTouchInput {
      down(calcCenterInGrid)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      moveBy(Offset(dragDeltaX, 0f))
      advanceEventTime(100L)
      up()
    }
    composeTestRule.waitForIdle()

    val newCalcLeft = calcNode.fetchSemanticsNode().boundsInRoot.left
    val newUnitsLeft = unitsNode.fetchSemanticsNode().boundsInRoot.left
    assertTrue(newUnitsLeft < newCalcLeft)

    val savedOrder = repo.getToolOrder()
    assertTrue(savedOrder.indexOf(Tool.UNIT_CONVERTER) < savedOrder.indexOf(Tool.CALCULATOR))
  }
}
