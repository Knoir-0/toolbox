package com.example.toolbox.ui.main

import com.example.toolbox.data.DataRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {
  @Test
  fun uiState_initiallyLoading() = runTest {
    val viewModel = MainScreenViewModel(FakeMyModelRepository())
    assertEquals(viewModel.uiState.first(), MainScreenUiState.Loading)
  }

  @Test
  fun uiState_onItemSaved_isDisplayed() = runTest {
    val viewModel = MainScreenViewModel(FakeMyModelRepository())
    assertEquals(viewModel.uiState.first(), MainScreenUiState.Loading)
  }

  @Test
  fun coerceToolsPerRow_enforcesRangeFromThreeToSix() {
    assertEquals(3, DEFAULT_TOOLS_PER_ROW)
    assertEquals(3, MIN_TOOLS_PER_ROW)
    assertEquals(6, MAX_TOOLS_PER_ROW)
    assertEquals(3, coerceToolsPerRow(1))
    assertEquals(3, coerceToolsPerRow(2))
    assertEquals(3, coerceToolsPerRow(3))
    assertEquals(4, coerceToolsPerRow(4))
    assertEquals(5, coerceToolsPerRow(5))
    assertEquals(6, coerceToolsPerRow(6))
    assertEquals(6, coerceToolsPerRow(8))
  }

  @Test
  fun reorderTools_movesToolToTargetIndex() {
    // Use a fixed list so adding new tools to the enum doesn't break this test.
    val initial = listOf(
      Tool.DICE_ROLLER,
      Tool.UNIT_CONVERTER,
      Tool.GUITAR_TUNER,
      Tool.METRONOME,
      Tool.COLOR_PICKER,
      Tool.RULER,
    )
    val reorderedForward = reorderTools(initial, fromIndex = 0, toIndex = 2)
    assertEquals(
      listOf(
        Tool.UNIT_CONVERTER,
        Tool.GUITAR_TUNER,
        Tool.DICE_ROLLER,
        Tool.METRONOME,
        Tool.COLOR_PICKER,
        Tool.RULER,
      ),
      reorderedForward,
    )

    val reorderedBackward = reorderTools(reorderedForward, fromIndex = 5, toIndex = 1)
    assertEquals(
      listOf(
        Tool.UNIT_CONVERTER,
        Tool.RULER,
        Tool.GUITAR_TUNER,
        Tool.DICE_ROLLER,
        Tool.METRONOME,
        Tool.COLOR_PICKER,
      ),
      reorderedBackward,
    )
  }

  @Test
  fun toolBackgroundStyle_rulerAndTodoListUseContainerStyles_othersUseBackground() {
    assertEquals(ToolBackgroundStyle.SURFACE_CONTAINER_LOWEST, toolBackgroundStyle(Tool.RULER))
    assertEquals(ToolBackgroundStyle.SURFACE_CONTAINER, toolBackgroundStyle(Tool.TODO_LIST))

    val otherTools = Tool.entries.filter { it != Tool.RULER && it != Tool.TODO_LIST }
    for (tool in otherTools) {
      assertEquals(
        "Expected BACKGROUND style for $tool",
        ToolBackgroundStyle.BACKGROUND,
        toolBackgroundStyle(tool),
      )
    }
  }

  @Test
  fun calculatePageTransitionTargetIndex_expandedMode_forwardAndBackward() {
    // 12 tools, 3 per row, 2 rows per page -> Page 0: 0..5, Page 1: 6..11
    val totalTools = 12
    val effectiveColumns = 3
    val rowsPerPage = ROWS_PER_PAGE_EXPANDED
    val settingsIndex = 11

    // Dragging forward from Page 0 into Page 1 enters at beginning of Page 1 (index 6)
    val forwardTarget = calculatePageTransitionTargetIndex(
      targetPage = 1,
      direction = 1,
      rowsPerPage = rowsPerPage,
      effectiveColumns = effectiveColumns,
      totalTools = totalTools,
      settingsIndex = settingsIndex,
    )
    assertEquals(6, forwardTarget)

    // Dragging backward from Page 1 into Page 0 enters at end of Page 0 (index 5)
    val backwardTarget = calculatePageTransitionTargetIndex(
      targetPage = 0,
      direction = -1,
      rowsPerPage = rowsPerPage,
      effectiveColumns = effectiveColumns,
      totalTools = totalTools,
      settingsIndex = settingsIndex,
    )
    assertEquals(5, backwardTarget)
  }

  @Test
  fun calculatePageTransitionTargetIndex_collapsedMode_movesBetweenRows() {
    // 12 tools, 3 per row, 1 row per page (collapsed)
    // Row 0: 0..2, Row 1: 3..5, Row 2: 6..8, Row 3: 9..11
    val totalTools = 12
    val effectiveColumns = 3
    val rowsPerPage = ROWS_PER_PAGE_COLLAPSED
    val settingsIndex = 11

    // Row 0 -> Row 1 (forward) enters at 3
    assertEquals(
      3,
      calculatePageTransitionTargetIndex(
        targetPage = 1,
        direction = 1,
        rowsPerPage = rowsPerPage,
        effectiveColumns = effectiveColumns,
        totalTools = totalTools,
        settingsIndex = settingsIndex,
      ),
    )

    // Row 1 -> Row 0 (backward) enters at 2
    assertEquals(
      2,
      calculatePageTransitionTargetIndex(
        targetPage = 0,
        direction = -1,
        rowsPerPage = rowsPerPage,
        effectiveColumns = effectiveColumns,
        totalTools = totalTools,
        settingsIndex = settingsIndex,
      ),
    )

    // Row 1 -> Row 2 (forward) enters at 6
    assertEquals(
      6,
      calculatePageTransitionTargetIndex(
        targetPage = 2,
        direction = 1,
        rowsPerPage = rowsPerPage,
        effectiveColumns = effectiveColumns,
        totalTools = totalTools,
        settingsIndex = settingsIndex,
      ),
    )

    // Row 2 -> Row 1 (backward) enters at 5
    assertEquals(
      5,
      calculatePageTransitionTargetIndex(
        targetPage = 1,
        direction = -1,
        rowsPerPage = rowsPerPage,
        effectiveColumns = effectiveColumns,
        totalTools = totalTools,
        settingsIndex = settingsIndex,
      ),
    )
  }

  @Test
  fun calculatePageTransitionTargetIndex_guaranteesSettingsIsNeverTargeted() {
    // 7 tools with Settings at index 6
    // Effective columns = 3, rowsPerPage = 2 -> Page 0: 0..5, Page 1: 6 (Settings)
    val totalTools = 7
    val effectiveColumns = 3
    val rowsPerPage = ROWS_PER_PAGE_EXPANDED
    val settingsIndex = 6

    val targetIndex = calculatePageTransitionTargetIndex(
      targetPage = 1,
      direction = 1,
      rowsPerPage = rowsPerPage,
      effectiveColumns = effectiveColumns,
      totalTools = totalTools,
      settingsIndex = settingsIndex,
    )
    // Cannot exceed settingsIndex - 1 (index 5)
    assertEquals(5, targetIndex)
  }

  @Test
  fun reorderTools_acrossPages_shiftsItemsAndMaintainsSettingsLast() {
    val initial = Tool.entries.toList()
    val dice = Tool.DICE_ROLLER
    val bluetooth = Tool.BLUETOOTH_REMOTE // Initially index 6 (first item on Page 1 with 3/row, 2 rows/page)

    assertEquals(0, initial.indexOf(dice))
    assertEquals(6, initial.indexOf(bluetooth))
    assertEquals(Tool.SETTINGS, initial.last())

    // Move Dice Roller from Page 0 (index 0) to Page 1 (index 6)
    val movedToPage1 = reorderTools(initial, fromIndex = 0, toIndex = 6)
    assertEquals(6, movedToPage1.indexOf(dice))
    assertEquals(5, movedToPage1.indexOf(bluetooth)) // Bluetooth Remote shifted to Page 0
    assertEquals(Tool.SETTINGS, movedToPage1.last())

    // Move Dice Roller back from Page 1 (index 6) to Page 0 (index 0)
    val movedBackToPage0 = reorderTools(movedToPage1, fromIndex = 6, toIndex = 0)
    assertEquals(0, movedBackToPage0.indexOf(dice))
    assertEquals(6, movedBackToPage0.indexOf(bluetooth))
    assertEquals(initial, movedBackToPage0)
  }
}

private class FakeMyModelRepository : DataRepository {
  override val data: Flow<List<String>> = flow { emit(listOf("Sample")) }
}
