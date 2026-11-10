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
}

private class FakeMyModelRepository : DataRepository {
  override val data: Flow<List<String>> = flow { emit(listOf("Sample")) }
}
