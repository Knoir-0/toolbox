package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Rule
import org.junit.Test

class ElementsScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun elementsScreen_displaysPeriodicTableSpotlightAndOpensInspectorAndMolarMass() {
    composeTestRule.setContent {
      ToolboxTheme {
        ElementsScreen(
          repository = InMemoryElementsRepository(),
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("elements_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("elements_toolbar_title").assertTextEquals("Elements")
    composeTestRule.onNodeWithTag("selected_element_spotlight_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("periodic_table_grid").assertIsDisplayed()

    // Tap Helium or Lithium tile to select it
    composeTestRule.onNodeWithTag("element_tile_Li").performClick()
    composeTestRule.waitForIdle()

    // Tap spotlight inspect button to open the Element Inspector dialog
    composeTestRule.onNodeWithTag("spotlight_inspect_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("element_inspector_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Lithium (#3)").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dialog_close_button").performClick()
    composeTestRule.waitForIdle()

    // Switch to Molar Mass calculator tab
    composeTestRule.onNodeWithTag("elements_mode_pill_MOLAR_MASS").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("elements_molar_mass_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("molar_mass_formula_input").performTextReplacement("C6H12O6")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("molar_mass_result_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("molar_mass_total_value").assertTextEquals("180.1560 g/mol")
  }
}
