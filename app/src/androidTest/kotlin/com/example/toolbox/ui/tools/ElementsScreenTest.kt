package com.example.toolbox.ui.tools

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
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
import com.example.toolbox.R
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

  @Test
  fun periodicTableWidget_mainViewIsPeriodicTable_andTappingElementSwitchesToElementInfoWithSuborbitals() {
    val context = composeTestRule.activity
    ElementsWidgets.clearInMemoryStateForTesting()
    ElementsWidgets.showPeriodicTableInWidget(context, refreshWidgets = false)

    val options = Bundle().apply {
      putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 280)
      putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 160)
    }

    // 1. Default main view is the Periodic Table (widget_elements_table_view is VISIBLE, detail view is GONE)
    val tableState = ElementsWidgets.resolveWidgetState(
      context = context,
      viewModeOverride = ElementsWidgetViewMode.TABLE,
    )
    composeTestRule.runOnUiThread {
      val host = FrameLayout(context)
      val root = ElementsWidgets.buildWidgetRemoteViews(
        context = context,
        appWidgetId = 1,
        state = tableState,
        options = options,
      ).apply(context, host)

      val tableView = root.findViewById<View>(R.id.widget_elements_table_view)
      val detailView = root.findViewById<View>(R.id.widget_elements_detail_view)
      assertEquals(View.VISIBLE, tableView.visibility)
      assertEquals(View.GONE, detailView.visibility)

      // All 118 element cells exist in the Periodic Table hit grid and are clickable
      for (z in 1..118) {
        val cell = root.findViewById<View>(ElementsWidgets.cellViewIdForAtomicNumber(z))
        assertTrue("Element cell #$z must be clickable in the Periodic Table widget", cell.isClickable)
      }
    }

    // 2. Simulate tapping Iron (#26) on the Periodic Table widget -> transitions to ELEMENT_INFO view with suborbitals
    val selectIronIntent = Intent(context, ElementsWidgetProvider::class.java).apply {
      action = ElementsWidgets.ACTION_WIDGET_SELECT_ELEMENT
      putExtra(ElementsWidgets.EXTRA_ATOMIC_NUMBER, 26)
    }
    assertTrue(ElementsWidgets.handleWidgetAction(context, selectIronIntent))

    val ironState = ElementsWidgets.resolveWidgetState(context = context)
    assertEquals(ElementsWidgetViewMode.ELEMENT_INFO, ironState.viewMode)
    assertEquals(26, ironState.element.atomicNumber)

    composeTestRule.runOnUiThread {
      val host = FrameLayout(context)
      val root = ElementsWidgets.buildWidgetRemoteViews(
        context = context,
        appWidgetId = 1,
        state = ironState,
        options = options,
      ).apply(context, host)

      val tableView = root.findViewById<View>(R.id.widget_elements_table_view)
      val detailView = root.findViewById<View>(R.id.widget_elements_detail_view)
      assertEquals(View.GONE, tableView.visibility)
      assertEquals(View.VISIBLE, detailView.visibility)

      val titleView = root.findViewById<TextView>(R.id.widget_elements_title)
      val suborbitalsView = root.findViewById<TextView>(R.id.widget_elements_suborbitals_text)
      val descriptionCard = root.findViewById<View>(R.id.widget_elements_description_card)
      val descriptionView = root.findViewById<TextView>(R.id.widget_elements_description_text)
      val infoMetaView = root.findViewById<TextView>(R.id.widget_elements_info_meta_text)
      assertTrue(titleView.text.toString().contains("Iron (#26)"))
      assertEquals("Suborbitals: 3d⁶ [⇅][↑][↑][↑][↑] • 4s² [⇅]", suborbitalsView.text.toString())
      assertEquals(View.VISIBLE, descriptionCard.visibility)
      assertEquals(ironState.element.summary, descriptionView.text.toString())
      assertTrue(infoMetaView.text.toString().contains("Oxidation: +2, +3, +6"))
    }

    // 3. Tapping Back to Periodic Table returns the widget to the Periodic Table main view
    val backToTableIntent = Intent(context, ElementsWidgetProvider::class.java).apply {
      action = ElementsWidgets.ACTION_WIDGET_SHOW_TABLE
    }
    assertTrue(ElementsWidgets.handleWidgetAction(context, backToTableIntent))
    assertEquals(ElementsWidgetViewMode.TABLE, ElementsWidgets.resolveWidgetState(context = context).viewMode)
  }
}
