package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    // Tap spotlight inspect button to open the full-screen Element Inspector view
    composeTestRule.onNodeWithTag("spotlight_inspect_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("element_inspector_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("element_inspector_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Lithium (#3)").assertIsDisplayed()

    val screenBounds = composeTestRule.onNodeWithTag("elements_screen")
      .fetchSemanticsNode().boundsInRoot
    val inspectorBounds = composeTestRule.onNodeWithTag("element_inspector_view")
      .fetchSemanticsNode().boundsInRoot
    assertEquals(screenBounds.width, inspectorBounds.width, 2f)
    assertEquals(screenBounds.height, inspectorBounds.height, 2f)

    composeTestRule.onNodeWithTag("inspector_back_button").performClick()
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

  @Test
  fun tableView_defaultZoomFitsFullDeviceWidth_andSupportsPinchToZoomInAndOut() {
    composeTestRule.setContent {
      ToolboxTheme {
        ElementsScreen(
          repository = InMemoryElementsRepository(),
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Default view zoom level (100%) shows the table taking up the full device width
    composeTestRule.onNodeWithTag("periodic_table_zoom_label", useUnmergedTree = true)
      .assertTextEquals("100%")

    val screenBounds = composeTestRule.onNodeWithTag("elements_screen")
      .fetchSemanticsNode().boundsInRoot
    val gridNode = composeTestRule.onNodeWithTag("periodic_table_grid")
      .fetchSemanticsNode()

    assertEquals(screenBounds.width, gridNode.boundsInRoot.width, 2f)

    // Both Group 1 (Hydrogen/Lithium) and Group 18 (Helium/Oganesson) are visible across the full device width
    composeTestRule.onNodeWithTag("element_tile_H").assertIsDisplayed()
    composeTestRule.onNodeWithTag("element_tile_He").assertIsDisplayed()
    composeTestRule.onNodeWithTag("element_tile_Og").assertIsDisplayed()

    val hBoundsDefault = composeTestRule.onNodeWithTag("element_tile_H")
      .fetchSemanticsNode().boundsInRoot
    val heBoundsDefault = composeTestRule.onNodeWithTag("element_tile_He")
      .fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Expected Group 18 tile (He right=${heBoundsDefault.right}) to reach near the right edge of the device (${screenBounds.right})",
      heBoundsDefault.right >= screenBounds.right - 32f,
    )

    // 2. Pinch out (two fingers spreading apart) to zoom in on the periodic table
    composeTestRule.onNodeWithTag("periodic_table_grid").performTouchInput {
      val center = this.center
      pinch(
        start0 = Offset(center.x - 60f, center.y),
        end0 = Offset(center.x - 240f, center.y),
        start1 = Offset(center.x + 60f, center.y),
        end1 = Offset(center.x + 240f, center.y),
        durationMillis = 300L,
      )
    }
    composeTestRule.waitForIdle()

    val zoomedInLabel = composeTestRule
      .onNodeWithTag("periodic_table_zoom_label", useUnmergedTree = true)
      .fetchSemanticsNode()
      .config[SemanticsProperties.Text]
      .first()
      .text
    val zoomedInPercent = zoomedInLabel.removeSuffix("%").toInt()
    assertTrue("Expected zoom > 100% after pinching to zoom in, got $zoomedInPercent%", zoomedInPercent > 100)

    val hBoundsZoomedIn = composeTestRule.onNodeWithTag("element_tile_H")
      .fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Expected tile width to increase when zoomed in (${hBoundsZoomedIn.width} > ${hBoundsDefault.width})",
      hBoundsZoomedIn.width > hBoundsDefault.width,
    )

    // 3. Pinch in (two fingers moving closer together) to zoom back out
    composeTestRule.onNodeWithTag("periodic_table_grid").performTouchInput {
      val center = this.center
      pinch(
        start0 = Offset(center.x - 240f, center.y),
        end0 = Offset(center.x - 60f, center.y),
        start1 = Offset(center.x + 240f, center.y),
        end1 = Offset(center.x + 60f, center.y),
        durationMillis = 300L,
      )
    }
    composeTestRule.waitForIdle()

    val zoomedOutLabel = composeTestRule
      .onNodeWithTag("periodic_table_zoom_label", useUnmergedTree = true)
      .fetchSemanticsNode()
      .config[SemanticsProperties.Text]
      .first()
      .text
    val zoomedOutPercent = zoomedOutLabel.removeSuffix("%").toInt()
    assertTrue(
      "Expected zoom to decrease after pinching to zoom out ($zoomedOutPercent% < $zoomedInPercent%)",
      zoomedOutPercent < zoomedInPercent,
    )

    val hBoundsZoomedOut = composeTestRule.onNodeWithTag("element_tile_H")
      .fetchSemanticsNode().boundsInRoot
    assertTrue(
      "Expected tile width to decrease when zoomed out (${hBoundsZoomedOut.width} < ${hBoundsZoomedIn.width})",
      hBoundsZoomedOut.width < hBoundsZoomedIn.width,
    )
  }
}
