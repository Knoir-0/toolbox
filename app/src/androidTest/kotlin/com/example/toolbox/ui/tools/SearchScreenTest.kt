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
  fun searchScreen_autofocusesToolbarSearchAndDisplaysCategoryPillsAndDirectory() {
    var selectedTool: Tool? = null

    composeTestRule.setContent {
      ToolboxTheme {
        SearchScreen(
          onSelectTool = { selectedTool = it },
          customDataSources = SearchDataSources(
            hiddenTools = setOf(Tool.CALCULATOR),
          ),
        )
      }
    }

    composeTestRule.onNodeWithTag("search_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toolbar_search_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("search_query_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("search_category_pills_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("search_suggestions_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("search_tool_hidden_icon_CALCULATOR").assertIsDisplayed()

    composeTestRule.onNodeWithTag("search_directory_tool_CALCULATOR").performClick()
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
