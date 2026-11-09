package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CalculatorScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      ToolboxTheme {
        CalculatorScreen(bottomPadding = 80.dp)
      }
    }
  }

  @Test
  fun testInitialState_displaysZeroAndKeypad() {
    composeTestRule.onNodeWithTag("calculator_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("0")
    composeTestRule.onNodeWithTag("calculator_key_7").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calculator_key_=").assertIsDisplayed()
  }

  @Test
  fun testSimpleArithmeticOperation() {
    // 7 + 8 = 15
    composeTestRule.onNodeWithTag("calculator_key_7").performClick()
    composeTestRule.onNodeWithTag("calculator_key_+").performClick()
    composeTestRule.onNodeWithTag("calculator_key_8").performClick()
    composeTestRule.waitForIdle()

    // Live preview shows = 15
    composeTestRule.onNodeWithTag("calculator_display_preview").assertTextEquals("= 15")

    // Press equals
    composeTestRule.onNodeWithTag("calculator_key_=").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("15")
  }

  @Test
  fun testClearAndBackspace() {
    composeTestRule.onNodeWithTag("calculator_key_9").performClick()
    composeTestRule.onNodeWithTag("calculator_key_5").performClick()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("95")

    composeTestRule.onNodeWithTag("calculator_key_⌫").performClick()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("9")

    composeTestRule.onNodeWithTag("calculator_key_C").performClick()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("0")
  }

  @Test
  fun testScientificDrawerExpansionAndTrig() {
    composeTestRule.onNodeWithTag("calculator_scientific_keypad").assertDoesNotExist()

    // Expand scientific keypad
    composeTestRule.onNodeWithTag("calculator_scientific_toggle_chip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("calculator_scientific_keypad").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calculator_key_sin").assertIsDisplayed()

    // sin(90) in DEG mode
    composeTestRule.onNodeWithTag("calculator_key_sin").performClick()
    composeTestRule.onNodeWithTag("calculator_key_9").performClick()
    composeTestRule.onNodeWithTag("calculator_key_0").performClick()
    composeTestRule.onNodeWithTag("calculator_key_=").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("1")
  }

  @Test
  fun testSmartParenthesis() {
    // Initial state: empty expression (displays 0)
    // Press ( ) -> opens paren "("
    composeTestRule.onNodeWithTag("calculator_key_( )").performClick()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("(")

    // Press 5 -> "(5"
    composeTestRule.onNodeWithTag("calculator_key_5").performClick()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("(5")

    // Press ( ) -> automatically closes paren because unclosed > 0 and ends with digit -> "(5)"
    composeTestRule.onNodeWithTag("calculator_key_( )").performClick()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("(5)")

    // Press + 3 -> "(5)+3"
    composeTestRule.onNodeWithTag("calculator_key_+").performClick()
    composeTestRule.onNodeWithTag("calculator_key_3").performClick()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("(5)+3")

    // Evaluate
    composeTestRule.onNodeWithTag("calculator_key_=").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("8")
  }

  @Test
  fun testFunctionsKeypadPercentAndNoParens() {
    // Expand scientific keypad
    composeTestRule.onNodeWithTag("calculator_scientific_toggle_chip").performClick()
    composeTestRule.waitForIdle()

    // Percent key is present in functions
    composeTestRule.onNodeWithTag("calculator_key_%").assertIsDisplayed()

    // Separate parenthesis keys are removed from functions
    composeTestRule.onNodeWithTag("calculator_key_(").assertDoesNotExist()
    composeTestRule.onNodeWithTag("calculator_key_)").assertDoesNotExist()

    // Test percent calculation: 50% = 0.5
    composeTestRule.onNodeWithTag("calculator_key_5").performClick()
    composeTestRule.onNodeWithTag("calculator_key_0").performClick()
    composeTestRule.onNodeWithTag("calculator_key_%").performClick()
    composeTestRule.onNodeWithTag("calculator_key_=").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("calculator_display_expression").assertTextEquals("0.5")
  }

  @Test
  fun testHistorySheetOpenAndClose() {
    // Perform calculation 4 * 5 = 20
    composeTestRule.onNodeWithTag("calculator_key_4").performClick()
    composeTestRule.onNodeWithTag("calculator_key_×").performClick()
    composeTestRule.onNodeWithTag("calculator_key_5").performClick()
    composeTestRule.onNodeWithTag("calculator_key_=").performClick()
    composeTestRule.waitForIdle()

    // Open history
    composeTestRule.onNodeWithTag("calculator_history_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("calculator_history_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calculator_clear_history_button").assertIsDisplayed()

    // Close history
    composeTestRule.onNodeWithContentDescription("Close history").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("calculator_history_sheet").assertDoesNotExist()
  }
}
