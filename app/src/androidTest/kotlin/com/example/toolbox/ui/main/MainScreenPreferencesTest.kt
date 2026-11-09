package com.example.toolbox.ui.main

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI test verifying that [MainScreen] loads persisted user preferences on launch.
 */
class MainScreenPreferencesTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    val repo = DefaultNavigationPreferencesRepository(composeTestRule.activity)
    val customOrder = listOf(
      Tool.CALCULATOR,
      Tool.RULER,
      Tool.METRONOME,
      Tool.COLOR_PICKER,
      Tool.GUITAR_TUNER,
      Tool.UNIT_CONVERTER,
      Tool.DICE_ROLLER,
      Tool.BLUETOOTH_REMOTE,
    )
    repo.setToolOrder(customOrder)
    repo.setToolsPerRow(4)

    composeTestRule.setContent {
      MainScreen()
    }
  }

  @Test
  fun mainScreen_launchesWithPersistedPreferencesFromStorage() {
    composeTestRule.waitForIdle()

    // Expand panel and verify restored count is 4
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_count").assertTextEquals("4")

    // Verify calculator is positioned before ruler according to the saved preference
    val calcNode = composeTestRule.onNodeWithTag("tool_item_CALCULATOR")
    val rulerNode = composeTestRule.onNodeWithTag("tool_item_RULER")
    calcNode.assertIsDisplayed()
    rulerNode.assertIsDisplayed()

    val calcLeft = calcNode.fetchSemanticsNode().boundsInRoot.left
    val rulerLeft = rulerNode.fetchSemanticsNode().boundsInRoot.left
    assertTrue(calcLeft < rulerLeft)
  }
}
