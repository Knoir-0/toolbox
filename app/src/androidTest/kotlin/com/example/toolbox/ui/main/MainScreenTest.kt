package com.example.toolbox.ui.main

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
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

  private val testContentState =
    androidx.compose.runtime.mutableStateOf<@androidx.compose.runtime.Composable () -> Unit>({ MainScreen() })

  private fun setTestContent(content: @androidx.compose.runtime.Composable () -> Unit) {
    composeTestRule.runOnUiThread {
      testContentState.value = content
    }
    composeTestRule.waitForIdle()
  }

  @Before
  fun setup() {
    composeTestRule.activity.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
    testContentState.value = { MainScreen() }
    composeTestRule.setContent { testContentState.value.invoke() }
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

    // Panel auto-collapsed back to one row; pull up and navigate to Page 1 for Ruler
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

    // Unhide Units, Colors, and Metronome to verify switching to them
    DefaultNavigationPreferencesRepository(composeTestRule.activity)
      .setHiddenTools(DEFAULT_HIDDEN_TOOLS - Tool.UNIT_CONVERTER - Tool.COLOR_PICKER - Tool.METRONOME)
    composeTestRule.waitForIdle()

    // With Units, Colors, Metronome unhidden (22 visible tools):
    // Page 0 (0..5): Calculate, Notes, To Do, Calendar, Camera, Gallery
    // Page 1 (6..11): Files, Music, Record, Ruler, Recipes, Plants
    // Page 2 (12..17): Tuner, Chords, Remote, Dice, Good, Search
    // Page 3 (18..21): Units, Colors, Metronome, Settings
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_3").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Units").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("From Unit (Meter)").assertExists()

    // Pull up and navigate to Page 2 for Guitar Tuner
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Tuner").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("TARGET").assertExists()

    // Pull up and navigate to Page 3 for Metronome and Colors
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_3").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Metronome").onFirst().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Play").assertExists()

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

    // Now on Row 1: Calendar, Camera, Gallery are displayed
    composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Camera").assertIsDisplayed()
    composeTestRule.onNodeWithText("Gallery").assertIsDisplayed()
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
    // Row 0: Math & Measure (2 tools: Calculate, Ruler)
    // Row 1: Productivity (3 tools: Notes, To Do, Calendar)
    // Row 2: Files (1 tool) + Photo & Video (2 tools) = 3 tools
    // Row 3: Audio (2 tools: Music, Record)
    // Row 4: Instruments (2 tools: Tuner, Chords) + Devices (1 tool: Remote) = 3 tools
    // Row 5: Lifestyle part 1 (3 tools: Recipes, Plants, Dice)
    // Row 6: Lifestyle part 2 (1 tool: Good) + System (2 tools: Search, Settings) = 3 tools
    val systemPageIndex = 6
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$systemPageIndex").performClick()
    composeTestRule.waitForIdle()

    // Verify System label and Settings tool
    composeTestRule.onNodeWithText("System").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()

    // Verify settings screen shows Default Sorting card (collapsed by default) and expand it
    composeTestRule.onNodeWithTag("toolbox_sorting_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toolbox_sorting_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sorting_option_user_choice").assertDoesNotExist()
    composeTestRule.onNodeWithTag("toolbox_sorting_toggle").performClick()
    composeTestRule.waitForIdle()
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

    // Check Math category (Row 0)
    val mathIndex = 0
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$mathIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Math").assertIsDisplayed()
    composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ruler").assertIsDisplayed()
    composeTestRule.onNodeWithText("Units").assertDoesNotExist()

    // Check Instruments category (Row 4)
    val musicIndex = 4
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$musicIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Instruments").assertIsDisplayed()
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

    // Initially collapsed: only Row 0 (Math + Files) is visible; Row 1 does not exist
    composeTestRule.onNodeWithTag("tools_grid_row_0").assertIsDisplayed()
    composeTestRule.onNodeWithText("Math").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_label_files").assertTextEquals("Files").assertIsDisplayed()
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
    composeTestRule.onNodeWithText("Math").assertIsDisplayed()
    composeTestRule.onNodeWithText("Productivity").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()

    // Navigate to Page 1 while expanded -> shows Row 2 (Media) and Row 3 (Audio + Devices)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_grid_row_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_grid_row_3").assertIsDisplayed()
    composeTestRule.onNodeWithText("Media").assertIsDisplayed()
    composeTestRule.onNodeWithText("Audio").assertIsDisplayed()

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
    // Row 0: Math (2) + Files (1) + Productivity (3) = 6 tools
    // Row 1: Media (2) + Audio (2) + Devices (1) = 5 tools
    // Row 2: Instruments (2) + Lifestyle (2) + Miscellaneous (2) = 6 tools
    // Row 3: System (2 tools: Search, Settings)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_2").performClick()
    composeTestRule.waitForIdle()

    // Instruments, Lifestyle, and Miscellaneous fit on Page 2 as one row
    composeTestRule.onNodeWithText("Instruments").assertIsDisplayed()
    composeTestRule.onNodeWithText("Lifestyle").assertIsDisplayed()
    composeTestRule.onNodeWithText("Miscellaneous").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dice").assertIsDisplayed()

    // Pull up the panel to reveal the second row (System) and the tools per row slider
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("System").assertIsDisplayed()
    composeTestRule.onNodeWithText("Search").assertIsDisplayed()
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
    // Row 2: Remotes & Devices (2 tools) + Miscellaneous (3 tools) = 5 tools
    // Row 3: System (2 tools: Search, Settings)
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_3").performClick()
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

    // Default Sorting, Tool visibility, Permissions, and Backup & Restore cards are collapsed by default
    composeTestRule.onNodeWithTag("toolbox_sorting_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toolbox_sorting_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sorting_option_user_choice").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tool_visibility_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_visibility_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("permissions_card").assertExists()
    composeTestRule.onNodeWithTag("permissions_toggle").assertExists()
    composeTestRule.onNodeWithTag("settings_backup_restore_card").assertExists()
    composeTestRule.onNodeWithTag("settings_backup_restore_toggle").assertExists()
    composeTestRule.onNodeWithTag("backup_settings_button").assertDoesNotExist()

    // Initially collapsed: category headers and permission cards do not exist
    composeTestRule.onNodeWithTag("category_header_math").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tool_visibility_row_CALCULATOR").assertDoesNotExist()
    composeTestRule.onNodeWithTag("permission_card_camera").assertDoesNotExist()

    // Click toggle to expand Tool Settings
    composeTestRule.onNodeWithTag("tool_visibility_toggle").performClick()
    composeTestRule.waitForIdle()

    // Now expanded: category headers exist while individual tool rows are collapsed inside categories
    composeTestRule.onNodeWithTag("category_header_math").assertExists()
    composeTestRule.onNodeWithTag("tool_visibility_row_CALCULATOR").assertDoesNotExist()

    // Click toggle to collapse again
    composeTestRule.onNodeWithTag("tool_visibility_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("category_header_math").assertDoesNotExist()
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

    // Expand Default Sorting section (collapsed by default)
    composeTestRule.onNodeWithTag("toolbox_sorting_toggle").performClick()
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

    // Expand Default Sorting section (collapsed by default)
    composeTestRule.onNodeWithTag("toolbox_sorting_toggle").performClick()
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

    // Tool settings card & toggle exist and are collapsed by default
    composeTestRule.onNodeWithTag("tool_settings_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tool_settings_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_header_math").assertDoesNotExist()

    // Expand Tool Settings
    composeTestRule.onNodeWithTag("tool_settings_toggle").performClick()
    composeTestRule.waitForIdle()

    // Category headers and their tool/hidden count badges should exist after expanding Tool Settings
    composeTestRule.onNodeWithTag("category_header_math").assertExists()
    composeTestRule.onNodeWithTag("category_tool_count_math").assertExists()
    composeTestRule.onNodeWithTag("category_hidden_count_math").assertExists()
    composeTestRule.onNodeWithTag("category_header_instruments").assertExists()
    composeTestRule.onNodeWithTag("category_tool_count_instruments").assertExists()
    composeTestRule.onNodeWithTag("category_hidden_count_instruments").assertExists()
    composeTestRule.onNodeWithTag("category_header_system").assertExists()

    // Categories are collapsed by default, so Calculator tool row should not exist yet
    composeTestRule.onNodeWithTag("tool_settings_row_CALCULATOR").assertDoesNotExist()

    // Expand Math category
    composeTestRule.onNodeWithTag("category_header_math").performClick()
    composeTestRule.waitForIdle()

    // Calculator tool row and add to homescreen button should now be visible
    composeTestRule.onNodeWithTag("tool_settings_row_CALCULATOR").assertExists()
    composeTestRule.onNodeWithTag("add_to_home_CALCULATOR").assertExists()

    // Re-collapse Math category
    composeTestRule.onNodeWithTag("category_header_math").performClick()
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

    // Expand Tool Settings and then Instruments category in Tool Settings
    composeTestRule.onNodeWithTag("tool_settings_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_header_instruments").performClick()
    composeTestRule.waitForIdle()

    // Tap the icon of a visible tool (Guitar Tuner) -> selects Guitar Tuner without hiding it
    composeTestRule.onNodeWithTag("tool_settings_icon_GUITAR_TUNER").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("TARGET").assertExists()
    assertTrue(Tool.GUITAR_TUNER !in repo.getHiddenTools())

    // Navigate back to Settings and expand Tool Settings and Instruments category
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_$lastPageIndex").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tool_item_SETTINGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tool_settings_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_header_instruments").performClick()
    composeTestRule.waitForIdle()

    // Tap the text of a hidden tool (Metronome) -> selects Metronome while keeping it hidden
    composeTestRule.onNodeWithTag("tool_settings_text_METRONOME").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Play").assertExists()
    assertTrue(Tool.METRONOME in repo.getHiddenTools())
  }

  @Test
  fun standaloneTool_launchesWithoutNavigationBar() {
    setTestContent {
      MainScreen(standaloneTool = Tool.CALCULATOR)
    }
    composeTestRule.waitForIdle()

    // Standalone tool screen is displayed
    composeTestRule.onNodeWithTag("standalone_tool_screen_CALCULATOR").assertIsDisplayed()

    // Navigation pull-up panel and drawer are completely hidden
    composeTestRule.onNodeWithTag("tools_pull_up_panel").assertDoesNotExist()
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").assertDoesNotExist()

    // Verify standalone shortcut intents open each tool/folder as its own app instance and reuse existing instances
    for (tool in Tool.entries) {
      val toolIntent = createStandaloneToolIntent(composeTestRule.activity, tool)
      assertEquals(tool, parseStandaloneTool(toolIntent))
      assertEquals(resolveStandaloneDocumentUri(tool), toolIntent.data)
      assertTrue((toolIntent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
      assertTrue((toolIntent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)
      assertTrue((toolIntent.flags and android.content.Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS) != 0)
      assertEquals(0, toolIntent.flags and android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
      assertEquals(0, toolIntent.flags and android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }

    val sampleFolderA = java.io.File("/storage/emulated/0/Download")
    val sampleFolderB = java.io.File("/storage/emulated/0/Pictures")
    val folderIntentA = createFolderShortcutIntent(composeTestRule.activity, sampleFolderA)
    val folderIntentB = createFolderShortcutIntent(composeTestRule.activity, sampleFolderB)
    val filesToolIntent = createStandaloneToolIntent(composeTestRule.activity, Tool.FILES)

    assertEquals(Tool.FILES, parseStandaloneTool(folderIntentA))
    assertEquals(sampleFolderA.absolutePath, parseFolderShortcut(folderIntentA))
    assertEquals(resolveStandaloneDocumentUri(Tool.FILES, sampleFolderA.absolutePath), folderIntentA.data)
    assertEquals(0, folderIntentA.flags and android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
    assertEquals(0, folderIntentA.flags and android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

    // Distinct folder locations and standalone Files tool have distinct document URIs so each opens as its own app instance,
    // while identical folder locations match the same canonical document URI so existing instances are reused
    org.junit.Assert.assertNotEquals(folderIntentA.data, folderIntentB.data)
    org.junit.Assert.assertNotEquals(folderIntentA.data, filesToolIntent.data)
    assertEquals(
      folderIntentA.data,
      createFolderShortcutIntent(composeTestRule.activity, java.io.File("/storage/emulated/0/Download/")).data,
    )
  }

  @Test
  fun filesTool_supportedFileMenuShowsFileConverter_omitsOpen_retainsOpenWith_andOpensConverterDirectly() {
    val tempDir = java.io.File(composeTestRule.activity.cacheDir, "files_menu_test_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      java.io.File(tempDir, "sample.png").writeBytes(byteArrayOf(1, 2, 3, 4))
      java.io.File(tempDir, "unknown.unrecognizedext").writeBytes(byteArrayOf(5, 6, 7, 8))

      setTestContent {
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
      setTestContent {
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
      setTestContent {
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
      setTestContent {
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
      setTestContent {
        com.example.toolbox.ui.tools.GalleryScreen(
          rootDirectory = tempDir,
          initialGrantedFolders = com.example.toolbox.ui.tools.GalleryDefaultFolder.entries.toSet(),
        )
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

      // Open ... menu on clip.mp4 tile and click Edit -> opens VideoEditorScreen
      composeTestRule.onNodeWithTag("photo_more_clip.mp4").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("photo_menu_edit_clip.mp4").performClick()
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

      setTestContent {
        androidx.activity.compose.BackHandler {
          outerBackTriggered = true
        }
        MainScreen(
          standaloneTool = Tool.FILES,
          initialDirectory = folderA,
        )
      }
      composeTestRule.waitForIdle()

      // FolderA opened from a homescreen shortcut acts as the root folder:
      // Header and root pill show FolderA, no Up button exists at root, and subfolder FolderB is listed
      composeTestRule.onAllNodesWithText("FolderA").onFirst().assertIsDisplayed()
      composeTestRule.onNodeWithTag("storage_pill_primary_root").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_up").assertDoesNotExist()
      composeTestRule.onNodeWithTag("file_item_FolderB").assertIsDisplayed()

      // Navigate into subfolder FolderB -> Up button appears
      composeTestRule.onNodeWithTag("file_item_FolderB").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onAllNodesWithText("FolderB").onFirst().assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_up").assertIsDisplayed()

      // 1st Back press -> goes up a folder to shortcut root FolderA
      composeTestRule.runOnUiThread {
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
      }
      composeTestRule.waitForIdle()
      org.junit.Assert.assertFalse(outerBackTriggered)
      composeTestRule.onAllNodesWithText("FolderA").onFirst().assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_up").assertDoesNotExist()
      composeTestRule.onNodeWithTag("file_item_FolderB").assertIsDisplayed()

      // 2nd Back press (once at shortcut root FolderA) -> cannot navigate up to parent rootDir; Back acts as normal at root
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
    repo.setHiddenTools(DEFAULT_HIDDEN_TOOLS - Tool.UNIT_CONVERTER)
    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    repo.setCategoryToolSorting(CategoryToolSortingOption.MANUAL)
    repo.setToolsPerRow(3)
    composeTestRule.waitForIdle()

    // On Page 0 (Math & Measurement): Calculate, Ruler, Units
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

  @Test
  fun filesTool_contextMenuUnifierActions_openMusicGalleryColorsAndImportNotesTodoAndClipboard() {
    val tempDir = java.io.File(composeTestRule.activity.cacheDir, "unifier_menu_test_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      com.example.toolbox.ui.tools.GalleryEngine.writeSolidColorPng(
        outputFile = java.io.File(tempDir, "swatch.png"),
        red = 0x2E,
        green = 0x8B,
        blue = 0x57,
      )
      com.example.toolbox.ui.tools.AudioEditorEngine.writeWavFile(
        buffer = com.example.toolbox.ui.tools.AudioEditorEngine.createSampleAudioBuffer(durationMs = 800L),
        outputFile = java.io.File(tempDir, "song.wav"),
      )
      java.io.File(tempDir, "checklist.md").writeText("# Tasks\n- [x] First item\n- [ ] Second item")

      setTestContent {
        com.example.toolbox.ui.tools.FilesScreen(initialDirectory = tempDir)
      }
      composeTestRule.waitForIdle()

      // 1. Image file menu shows Image and Gallery (without Inspect Color) and opens ImageScreen and GalleryScreen
      composeTestRule.onNodeWithTag("file_options_swatch.png").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_open_image").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_open_gallery").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_inspect_color").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_cut").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_copy").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_duplicate").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_open_image").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("image_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("gallery_viewer_close").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("files_screen").assertIsDisplayed()

      composeTestRule.onNodeWithTag("file_options_swatch.png").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_open_gallery").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("gallery_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("files_screen").assertIsDisplayed()

      // 2. Audio file menu shows Play in Music and opens MusicPlayerScreen
      composeTestRule.onNodeWithTag("file_options_song.wav").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_play_music").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_play_music").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("music_player_screen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_navigate_back").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("files_screen").assertIsDisplayed()

      // 3. Text/Markdown file menu shows Add Note (without Edit / View or Import to To Do), and single-item Copy populates Paste bar
      composeTestRule.onNodeWithTag("file_options_checklist.md").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_edit_text").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_save_to_notes").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_import_to_todo").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_copy").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("files_paste_bar").assertIsDisplayed()

      // 4. Context menu Move action populates the floating move/paste pill bar (instead of a dialog)
      composeTestRule.onNodeWithTag("file_options_checklist.md").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_move").assertIsDisplayed()
      composeTestRule.onNodeWithTag("action_move").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("move_destination_dialog").assertDoesNotExist()
      composeTestRule.onNodeWithTag("files_paste_bar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("files_move_bar").assertIsDisplayed()
      composeTestRule.onNodeWithText("Move here").assertIsDisplayed()
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun rulerTool_calculatorAndUnitConverterButtons_areLeftAndRightOfMeasurementLabel_andPrefillValue() {
    // Navigate to Ruler tool
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_panel_page_indicator_dot_1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onAllNodesWithText("Ruler").onFirst().performClick()
    composeTestRule.waitForIdle()

    val calcButton = composeTestRule.onNodeWithTag("ruler_calculator_button")
    val labelNode = composeTestRule.onNodeWithTag("ruler_measurement_label")
    val converterButton = composeTestRule.onNodeWithTag("ruler_unit_converter_button")

    calcButton.assertIsDisplayed()
    labelNode.assertIsDisplayed()
    converterButton.assertIsDisplayed()
    labelNode.assertTextEquals("5.40 cm")

    val calcBounds = calcButton.fetchSemanticsNode().boundsInRoot
    val labelBounds = labelNode.fetchSemanticsNode().boundsInRoot
    val converterBounds = converterButton.fetchSemanticsNode().boundsInRoot
    assertTrue(calcBounds.right <= labelBounds.left)
    assertTrue(converterBounds.left >= labelBounds.right)

    // Tapping Calculator link opens Calculator prefilled with "5.40"
    calcButton.performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("calculator_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("5.40")

    // Return to Ruler and tap Unit Converter link -> opens Unit Converter prefilled with "5.40" and From Unit (Centimeter)
    composeTestRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ruler_unit_converter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("unit_converter_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("unit_converter_input").assertTextEquals("5.40")
    composeTestRule.onNodeWithText("From Unit (Centimeter)").assertIsDisplayed()

    // Return to Ruler, toggle to Imperial (in), and tap Unit Converter link -> opens Unit Converter with "2.13" and From Unit (Inch)
    composeTestRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ruler_unit_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ruler_measurement_label").assertTextEquals("2.13 in")
    composeTestRule.onNodeWithTag("ruler_unit_converter_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("unit_converter_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("unit_converter_input").assertTextEquals("2.13")
    composeTestRule.onNodeWithText("From Unit (Inch)").assertIsDisplayed()
  }

  @Test
  fun imageTool_scrollingPreviewBarOutOfViewOfSelectedPhoto_doesNotShiftActionButtons() {
    val tempDir = java.io.File(composeTestRule.activity.cacheDir, "image_preview_scroll_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      val files = (1..20).map { idx ->
        val file = java.io.File(tempDir, "photo_%02d.png".format(idx))
        com.example.toolbox.ui.tools.GalleryEngine.writeSolidColorPng(
          outputFile = file,
          red = (idx * 10) % 255,
          green = (idx * 20) % 255,
          blue = (idx * 30) % 255,
        )
        file
      }

      setTestContent {
        com.example.toolbox.ui.tools.ImageScreen(initialFile = files.first())
      }
      composeTestRule.waitForIdle()

      val actionsRow = composeTestRule.onNodeWithTag("photo_viewer_actions_row")
      val previewBar = composeTestRule.onNodeWithTag("photo_viewer_preview_scroll_bar")
      actionsRow.assertIsDisplayed()
      previewBar.assertIsDisplayed()

      val initialActionsTop = actionsRow.fetchSemanticsNode().boundsInRoot.top
      val initialActionsBottom = actionsRow.fetchSemanticsNode().boundsInRoot.bottom
      val initialPreviewHeight = previewBar.fetchSemanticsNode().boundsInRoot.height

      // Scroll the bottom preview strip to the far end so the selected first photo is scrolled out of view
      previewBar.performTouchInput {
        swipeLeft()
      }
      composeTestRule.waitForIdle()
      previewBar.performTouchInput {
        swipeLeft()
      }
      composeTestRule.waitForIdle()

      val afterScrollActionsTop = actionsRow.fetchSemanticsNode().boundsInRoot.top
      val afterScrollActionsBottom = actionsRow.fetchSemanticsNode().boundsInRoot.bottom
      val afterScrollPreviewHeight = previewBar.fetchSemanticsNode().boundsInRoot.height

      org.junit.Assert.assertEquals(initialActionsTop, afterScrollActionsTop, 0.5f)
      org.junit.Assert.assertEquals(initialActionsBottom, afterScrollActionsBottom, 0.5f)
      org.junit.Assert.assertEquals(initialPreviewHeight, afterScrollPreviewHeight, 0.5f)
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun filesTool_tappingApk_opensSystemSettingToAllowInstallingApps_thenInstallsApkWhenTurnedOn() {
    val tempDir = java.io.File(composeTestRule.activity.cacheDir, "apk_install_test_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      val apkFile = java.io.File(tempDir, "toolbox-update.apk").apply {
        writeBytes(byteArrayOf(0x50, 0x4B, 0x03, 0x04))
      }

      var settingsLaunchedCount = 0
      var lastSettingsIntent: android.content.Intent? = null
      var installerLaunchedCount = 0
      var lastInstalledApkFile: java.io.File? = null
      var lastInstallerIntent: android.content.Intent? = null

      com.example.toolbox.ui.tools.FileManagerEngine.canInstallApksOverrideForTest = false
      com.example.toolbox.ui.tools.FileManagerEngine.unknownAppSourcesSettingsLauncherForTest = { intent ->
        settingsLaunchedCount++
        lastSettingsIntent = intent
        true
      }
      com.example.toolbox.ui.tools.FileManagerEngine.apkInstallerLauncherForTest = { file, intent ->
        installerLaunchedCount++
        lastInstalledApkFile = file
        lastInstallerIntent = intent
        true
      }

      setTestContent {
        com.example.toolbox.ui.tools.FilesScreen(initialDirectory = tempDir)
      }
      composeTestRule.waitForIdle()

      // 1. When install unknown apps setting is off, tapping the APK brings the user to the system setting
      composeTestRule.onNodeWithTag("file_item_toolbox-update.apk").assertIsDisplayed()
      composeTestRule.onNodeWithTag("file_item_toolbox-update.apk").performClick()
      composeTestRule.waitForIdle()

      assertEquals(1, settingsLaunchedCount)
      assertEquals(0, installerLaunchedCount)
      assertEquals(
        android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
        lastSettingsIntent?.action,
      )
      assertEquals(
        android.net.Uri.parse("package:${composeTestRule.activity.packageName}"),
        lastSettingsIntent?.data,
      )

      // 2. Simulate the user turning on the ability to let Toolbox install applications in system settings
      com.example.toolbox.ui.tools.FileManagerEngine.canInstallApksOverrideForTest = true

      // 3. Tapping on the APK now launches the system package installer for that APK
      composeTestRule.onNodeWithTag("file_item_toolbox-update.apk").performClick()
      composeTestRule.waitForIdle()

      assertEquals(1, settingsLaunchedCount)
      assertEquals(1, installerLaunchedCount)
      assertEquals(apkFile.absolutePath, lastInstalledApkFile?.absolutePath)
      assertEquals(android.content.Intent.ACTION_VIEW, lastInstallerIntent?.action)
      assertEquals(
        com.example.toolbox.ui.tools.FileManagerEngine.APK_MIME_TYPE,
        lastInstallerIntent?.type,
      )
      assertTrue(
        ((lastInstallerIntent?.flags ?: 0) and android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0,
      )

      // 4. Also verify tapping the APK in Grid view works identically
      composeTestRule.onNodeWithTag("action_toggle_view_mode").performClick()
      composeTestRule.waitForIdle()

      com.example.toolbox.ui.tools.FileManagerEngine.canInstallApksOverrideForTest = false
      composeTestRule.onNodeWithTag("file_grid_item_toolbox-update.apk").performClick()
      composeTestRule.waitForIdle()
      assertEquals(2, settingsLaunchedCount)
      assertEquals(1, installerLaunchedCount)

      com.example.toolbox.ui.tools.FileManagerEngine.canInstallApksOverrideForTest = true
      composeTestRule.onNodeWithTag("file_grid_item_toolbox-update.apk").performClick()
      composeTestRule.waitForIdle()
      assertEquals(2, settingsLaunchedCount)
      assertEquals(2, installerLaunchedCount)
    } finally {
      com.example.toolbox.ui.tools.FileManagerEngine.resetApkInstallTestOverrides()
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun filesTool_tappingAudioFile_opensAudioRecorderMiniAudioPlayerModal() {
    val tempDir = java.io.File(composeTestRule.activity.cacheDir, "audio_modal_test_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      val wavFile = java.io.File(tempDir, "voice_memo.wav")
      com.example.toolbox.ui.tools.AudioRecorderEngine.writeFallbackAudioFile(
        outputFile = wavFile,
        durationMs = 2000L,
      )

      setTestContent {
        com.example.toolbox.ui.tools.FilesScreen(initialDirectory = tempDir)
      }
      composeTestRule.waitForIdle()

      // 1. Tap the audio file in List view -> Audio Recorder's MiniAudioPlayerModal pops up
      composeTestRule.onNodeWithTag("file_item_voice_memo.wav").assertIsDisplayed()
      composeTestRule.onNodeWithTag("file_item_voice_memo.wav").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("mini_player_modal").assertIsDisplayed()
      composeTestRule.onNodeWithTag("mini_player_title").assertTextEquals("voice_memo.wav")
      composeTestRule.onNodeWithTag("mini_player_play_pause_button").assertIsDisplayed()
      composeTestRule.onNodeWithTag("mini_player_tracking_slider").assertIsDisplayed()

      // Close the modal
      composeTestRule.onNodeWithTag("mini_player_close_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("mini_player_modal").assertDoesNotExist()

      // 2. Switch to Grid view and tap the audio file -> MiniAudioPlayerModal pops up
      composeTestRule.onNodeWithTag("action_toggle_view_mode").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("file_grid_item_voice_memo.wav").assertIsDisplayed()
      composeTestRule.onNodeWithTag("file_grid_item_voice_memo.wav").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("mini_player_modal").assertIsDisplayed()
      composeTestRule.onNodeWithTag("mini_player_title").assertTextEquals("voice_memo.wav")

      composeTestRule.onNodeWithTag("mini_player_close_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("mini_player_modal").assertDoesNotExist()
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun filesTool_tappingVideoFile_opensMiniVideoPlayerModal() {
    val tempDir = java.io.File(composeTestRule.activity.cacheDir, "video_modal_test_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      val mp4File = java.io.File(tempDir, "demo_clip.mp4").apply {
        writeBytes(byteArrayOf(0, 0, 0, 24, 0x66, 0x74, 0x79, 0x70))
      }
      assertTrue(com.example.toolbox.ui.tools.FileManagerEngine.isVideoFile(mp4File))

      setTestContent {
        com.example.toolbox.ui.tools.FilesScreen(initialDirectory = tempDir)
      }
      composeTestRule.waitForIdle()

      // 1. Tap the video file in List view -> MiniVideoPlayerModal pops up
      composeTestRule.onNodeWithTag("file_item_demo_clip.mp4").assertIsDisplayed()
      composeTestRule.onNodeWithTag("file_item_demo_clip.mp4").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("mini_video_player_modal").assertIsDisplayed()
      composeTestRule.onNodeWithTag("mini_video_player_title").assertTextEquals("demo_clip.mp4")
      composeTestRule.onNodeWithTag("mini_video_player_viewport").assertIsDisplayed()
      composeTestRule.onNodeWithTag("mini_video_player_play_pause_button").assertIsDisplayed()
      composeTestRule.onNodeWithTag("mini_video_player_tracking_slider").assertIsDisplayed()
      composeTestRule.onNodeWithTag("mini_video_player_elapsed_time").assertIsDisplayed()
      composeTestRule.onNodeWithTag("mini_video_player_total_time").assertIsDisplayed()

      // Close the modal
      composeTestRule.onNodeWithTag("mini_video_player_close_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("mini_video_player_modal").assertDoesNotExist()

      // 2. Switch to Grid view and tap the video file -> MiniVideoPlayerModal pops up
      composeTestRule.onNodeWithTag("action_toggle_view_mode").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("file_grid_item_demo_clip.mp4").assertIsDisplayed()
      composeTestRule.onNodeWithTag("file_grid_item_demo_clip.mp4").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("mini_video_player_modal").assertIsDisplayed()
      composeTestRule.onNodeWithTag("mini_video_player_title").assertTextEquals("demo_clip.mp4")

      composeTestRule.onNodeWithTag("mini_video_player_close_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("mini_video_player_modal").assertDoesNotExist()
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun settingsScreen_errorLogsButtonAtBottom_opensErrorLogsFullPageViewAndKeepsLast100Errors() {
    val repo = InMemoryNavigationPreferencesRepository()
    for (i in 1..105) {
      repo.logError(
        message = "Logged error #$i",
        source = "Settings",
        details = "Trace #$i",
        timestampMs = 1_763_400_000_000L + i * 1_000L,
      )
    }
    assertEquals(100, repo.getErrorLogs().size)

    setTestContent {
      com.example.toolbox.ui.tools.SettingsScreen(
        preferencesRepository = repo,
      )
    }
    composeTestRule.waitForIdle()

    // Scroll to the bottom of SettingsScreen and click the Error Logs button
    composeTestRule.onNodeWithTag("error_logs_button").performScrollTo()
    composeTestRule.onNodeWithTag("error_logs_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("error_logs_button").performClick()
    composeTestRule.waitForIdle()

    // Full-page Error Logs view opens with toolbar containing back button, title (Error Logs), and search button
    composeTestRule.onNodeWithTag("error_logs_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("action_navigate_back").assertIsDisplayed()
    composeTestRule.onNodeWithTag("error_logs_title").assertTextEquals("Error Logs")
    composeTestRule.onNodeWithTag("action_toggle_search").assertIsDisplayed()
    composeTestRule.onNodeWithTag("error_logs_count_text").assertTextEquals("100 of 100 errors recorded")

    // Errors are displayed on cards with selectable/highlightable text and a Copy button on each card
    composeTestRule.onNodeWithTag("error_log_card_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("copy_error_log_button_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("error_log_selectable_text_0").assertIsDisplayed()
    composeTestRule.onNodeWithText("Logged error #105").assertIsDisplayed()
    composeTestRule.onNodeWithText("Logged error #6").assertExists()
    composeTestRule.onNodeWithText("Logged error #1").assertDoesNotExist()
    composeTestRule.onNodeWithText("Logged error #5").assertDoesNotExist()

    // Click the copy button on the first error card
    composeTestRule.onNodeWithTag("copy_error_log_button_0").performClick()
    composeTestRule.waitForIdle()

    // Click the toolbar search button and filter for "#105"
    composeTestRule.onNodeWithTag("action_toggle_search").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("search_input").performTextInput("#105")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("error_logs_count_text").assertTextEquals("1 of 100 errors matching")
    composeTestRule.onNodeWithText("Logged error #105").assertIsDisplayed()
    composeTestRule.onNodeWithText("Logged error #104").assertDoesNotExist()

    // Close search
    composeTestRule.onNodeWithTag("action_close_search").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("error_logs_count_text").assertTextEquals("100 of 100 errors recorded")

    // Navigate back to the main Settings screen via the toolbar back button
    composeTestRule.onNodeWithTag("action_navigate_back").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("error_logs_screen").assertDoesNotExist()
    composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()

    // Verify License button is positioned side by side with Error Logs button and opens the License view
    composeTestRule.onNodeWithTag("error_logs_button").performScrollTo()
    composeTestRule.onNodeWithTag("license_button").performScrollTo()
    val errorLogsBounds = composeTestRule.onNodeWithTag("error_logs_button").fetchSemanticsNode().boundsInRoot
    val licenseBounds = composeTestRule.onNodeWithTag("license_button").fetchSemanticsNode().boundsInRoot
    assertTrue(licenseBounds.left >= errorLogsBounds.right)
    assertEquals(errorLogsBounds.top, licenseBounds.top, 2f)

    composeTestRule.onNodeWithTag("license_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("license_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("license_title").assertTextEquals("License")
    composeTestRule.onNodeWithTag("license_subtext").assertTextEquals("GNU General Public License v3.0")
    composeTestRule.onNodeWithTag("license_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("copy_license_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("license_text").assertIsDisplayed()

    // Navigate back from License view to Settings
    composeTestRule.onNodeWithTag("action_navigate_back").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("license_screen").assertDoesNotExist()
    composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()

    // Verify Donate button is positioned below License button at the very bottom and temporarily opens the 404 Not Found view with shrug emote
    composeTestRule.onNodeWithTag("license_button").performScrollTo()
    composeTestRule.onNodeWithTag("donate_button").performScrollTo()
    val licenseBoundsBeforeDonate = composeTestRule.onNodeWithTag("license_button").fetchSemanticsNode().boundsInRoot
    val donateBounds = composeTestRule.onNodeWithTag("donate_button").fetchSemanticsNode().boundsInRoot
    assertTrue(donateBounds.top >= licenseBoundsBeforeDonate.bottom)

    composeTestRule.onNodeWithTag("donate_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("donate_404_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("donate_404_shrug_emote").assertTextEquals("¯\\_(ツ)_/¯")
    composeTestRule.onNodeWithTag("donate_404_title").assertTextEquals("404 Not Found")

    composeTestRule.onNodeWithTag("action_navigate_back").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("donate_404_screen").assertDoesNotExist()
    composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
  }

  @Test
  fun filesTool_tappingTextFile_opensAlignedTextEditorModalWithBottomSaveActionBar_resizesToContent_andOmitsEditViewFromMenu() {
    val tempDir = java.io.File(composeTestRule.activity.cacheDir, "text_modal_test_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    try {
      val shortFile = java.io.File(tempDir, "short.txt").apply {
        writeText("Single line note")
      }
      val mediumFile = java.io.File(tempDir, "medium.md").apply {
        writeText((1..6).joinToString("\n") { "Medium line $it" })
      }
      val longFile = java.io.File(tempDir, "long.log").apply {
        writeText((1..40).joinToString("\n") { "Long log entry line #$it with details" })
      }

      setTestContent {
        com.example.toolbox.ui.tools.FilesScreen(initialDirectory = tempDir)
      }
      composeTestRule.waitForIdle()

      // 1. Context menu on text file does not contain "Edit / View" option
      composeTestRule.onNodeWithTag("file_options_short.txt").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("action_edit_text").assertDoesNotExist()
      composeTestRule.onNodeWithText("Edit / View").assertDoesNotExist()
      composeTestRule.onNodeWithTag("action_save_to_notes").assertIsDisplayed()
      // Dismiss context menu by tapping the file item or back
      composeTestRule.runOnUiThread {
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
      }
      composeTestRule.waitForIdle()

      // 2. Tap short text file -> opens TextEditorDialog modal by default
      composeTestRule.onNodeWithTag("file_item_short.txt").performClick()
      composeTestRule.waitForIdle()

      val modalNode = composeTestRule.onNodeWithTag("text_editor_modal")
      val inputNode = composeTestRule.onNodeWithTag("text_editor_input")
      val actionBarNode = composeTestRule.onNodeWithTag("text_editor_action_bar")
      val saveButtonNode = composeTestRule.onNodeWithTag("text_editor_save_button")

      modalNode.assertIsDisplayed()
      composeTestRule.onNodeWithTag("text_editor_modal_title").assertTextEquals("short.txt")
      inputNode.assertIsDisplayed()
      actionBarNode.assertIsDisplayed()
      saveButtonNode.assertIsDisplayed()
      saveButtonNode.assertIsNotEnabled()

      // Verify Save button is inside the bottom action bar below the text editor input
      val inputBoundsShort = inputNode.fetchSemanticsNode().boundsInRoot
      val saveBoundsShort = saveButtonNode.fetchSemanticsNode().boundsInRoot
      val shortModalHeight = modalNode.fetchSemanticsNode().boundsInRoot.height
      assertTrue(saveBoundsShort.top >= inputBoundsShort.bottom)

      // Edit text and save via the bottom Save button
      inputNode.performTextReplacement("Updated short line")
      composeTestRule.waitForIdle()
      saveButtonNode.assertIsEnabled()
      saveButtonNode.performClick()
      composeTestRule.waitForIdle()
      assertEquals("Updated short line", shortFile.readText())
      saveButtonNode.assertIsNotEnabled()

      // Close modal
      composeTestRule.onNodeWithTag("text_editor_close_button").performClick()
      composeTestRule.waitForIdle()
      modalNode.assertDoesNotExist()

      // 3. Tap medium text file (6 lines) -> modal resizes taller than 1-line modal
      composeTestRule.onNodeWithTag("file_item_medium.md").performClick()
      composeTestRule.waitForIdle()
      val mediumModalHeight = composeTestRule.onNodeWithTag("text_editor_modal").fetchSemanticsNode().boundsInRoot.height
      assertTrue(mediumModalHeight > shortModalHeight)

      composeTestRule.onNodeWithTag("text_editor_cancel_button").performClick()
      composeTestRule.waitForIdle()

      // 4. Tap long text file (40 lines) -> modal grows up to max cap (does not fill entire screen) and scrolls
      composeTestRule.onNodeWithTag("file_item_long.log").performClick()
      composeTestRule.waitForIdle()
      val longModalHeight = composeTestRule.onNodeWithTag("text_editor_modal").fetchSemanticsNode().boundsInRoot.height
      assertTrue(longModalHeight > mediumModalHeight)
      composeTestRule.onNodeWithTag("text_editor_save_button").assertIsDisplayed()

      composeTestRule.onNodeWithTag("text_editor_close_button").performClick()
      composeTestRule.waitForIdle()
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun navigationBar_remainsFixedToPhysicalBottomOfDevice_acrossAllRotations() {
    val rotationState = androidx.compose.runtime.mutableIntStateOf(android.view.Surface.ROTATION_0)
    val selectedToolState = androidx.compose.runtime.mutableStateOf(Tool.CALCULATOR)

    setTestContent {
      com.example.toolbox.ui.theme.ToolboxTheme {
        androidx.compose.foundation.layout.Box(
          modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .testTag("rotation_test_root"),
        ) {
          @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
          ToolboxScaffold(
            selectedTool = selectedToolState.value,
            onSelectTool = { selectedToolState.value = it },
            displayRotationOverride = rotationState.intValue,
          )
        }
      }
    }

    val rootBounds = composeTestRule.onNodeWithTag("rotation_test_root").fetchSemanticsNode().boundsInRoot

    // 1. Portrait (ROTATION_0): navigation bar is docked along the bottom edge
    val portraitNavBounds = composeTestRule.onNodeWithTag("toolbox_navigation_bar_surface").fetchSemanticsNode().boundsInRoot
    org.junit.Assert.assertEquals(rootBounds.bottom, portraitNavBounds.bottom, 2f)
    assertTrue(portraitNavBounds.top > rootBounds.top)

    // 2. Landscape (ROTATION_90, 90° CCW): physical bottom is on the right edge of the window
    composeTestRule.runOnUiThread {
      rotationState.intValue = android.view.Surface.ROTATION_90
    }
    composeTestRule.waitForIdle()
    val landscapeRightNavBounds = composeTestRule.onNodeWithTag("toolbox_navigation_bar_surface").fetchSemanticsNode().boundsInRoot
    org.junit.Assert.assertEquals(rootBounds.right, landscapeRightNavBounds.right, 2f)
    org.junit.Assert.assertEquals(rootBounds.height, landscapeRightNavBounds.height, 2f)
    assertTrue(landscapeRightNavBounds.left > rootBounds.left)

    // Tapping a tool item while docked on the physical bottom (right edge) still switches tools
    composeTestRule.onAllNodesWithText("Notes").onFirst().performClick()
    composeTestRule.waitForIdle()
    assertEquals(Tool.NOTES, selectedToolState.value)

    // 3. Reverse Landscape (ROTATION_270, 90° CW): physical bottom is on the left edge of the window
    composeTestRule.runOnUiThread {
      rotationState.intValue = android.view.Surface.ROTATION_270
    }
    composeTestRule.waitForIdle()
    val landscapeLeftNavBounds = composeTestRule.onNodeWithTag("toolbox_navigation_bar_surface").fetchSemanticsNode().boundsInRoot
    org.junit.Assert.assertEquals(rootBounds.left, landscapeLeftNavBounds.left, 2f)
    org.junit.Assert.assertEquals(rootBounds.height, landscapeLeftNavBounds.height, 2f)
    assertTrue(landscapeLeftNavBounds.right < rootBounds.right)

    // 4. Upside-Down Portrait (ROTATION_180): physical bottom is on the top edge of the window
    composeTestRule.runOnUiThread {
      rotationState.intValue = android.view.Surface.ROTATION_180
    }
    composeTestRule.waitForIdle()
    val upsideDownNavBounds = composeTestRule.onNodeWithTag("toolbox_navigation_bar_surface").fetchSemanticsNode().boundsInRoot
    org.junit.Assert.assertEquals(rootBounds.top, upsideDownNavBounds.top, 2f)
    assertTrue(upsideDownNavBounds.bottom < rootBounds.bottom)
  }

  @Test
  fun toolSelection_updatesActivityRequestedOrientationAccordingToToolPolicy() {
    val selectedToolState = androidx.compose.runtime.mutableStateOf(Tool.CALCULATOR)

    setTestContent {
      com.example.toolbox.ui.theme.ToolboxTheme {
        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        ToolboxScaffold(
          selectedTool = selectedToolState.value,
          onSelectTool = { selectedToolState.value = it },
        )
      }
    }

    assertEquals(
      android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
      composeTestRule.activity.requestedOrientation,
    )

    composeTestRule.runOnUiThread {
      selectedToolState.value = Tool.NOTES
    }
    composeTestRule.waitForIdle()
    assertEquals(
      android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
      composeTestRule.activity.requestedOrientation,
    )

    composeTestRule.runOnUiThread {
      selectedToolState.value = Tool.FILES
    }
    composeTestRule.waitForIdle()
    assertEquals(
      android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
      composeTestRule.activity.requestedOrientation,
    )
  }
}
