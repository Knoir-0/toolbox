package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun searchScreen_displaysDiscoveryHomeAndLaunchesQuickTool() {
    var selectedTool: Tool? = null
    val searchPrefs = InMemorySearchPreferencesRepository(
      initialRecentSearches = listOf("P0420"),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        SearchScreen(
          onSelectTool = { selectedTool = it },
          searchPreferencesRepository = searchPrefs,
          customDataSources = SearchDataSources(),
        )
      }
    }

    composeTestRule.onNodeWithTag("search_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("search_query_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("search_pinned_tools_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("search_recent_searches_card").assertIsDisplayed()

    composeTestRule.onNodeWithTag("search_quick_launch_CALCULATOR").performClick()
    composeTestRule.waitForIdle()
    assertEquals(Tool.CALCULATOR, selectedTool)
  }

  @Test
  fun searchScreen_typingMathAndCatalogQuery_showsResultsAndOpensDetailDialog() {
    var selectedTool: Tool? = null

    composeTestRule.setContent {
      ToolboxTheme {
        SearchScreen(
          onSelectTool = { selectedTool = it },
          searchPreferencesRepository = InMemorySearchPreferencesRepository(),
          customDataSources = SearchDataSources(),
        )
      }
    }

    composeTestRule.onNodeWithTag("search_query_input").performTextReplacement("24 * 15 + 40")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("24 * 15 + 40 = 400").assertIsDisplayed()

    // Tap the live calculation card to open the inspection dialog
    composeTestRule.onNodeWithText("24 * 15 + 40 = 400").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("search_result_detail_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("search_dialog_open_tool_button").performClick()
    composeTestRule.waitForIdle()

    assertEquals(Tool.CALCULATOR, selectedTool)
  }
}
