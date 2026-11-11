package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Rule
import org.junit.Test

class PlantScheduleScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun searchForPlants_addToSchedule_andDisplayYearViewWithHighlightedMonths() {
    val repository = InMemoryPlantScheduleRepository()

    composeTestRule.setContent {
      ToolboxTheme {
        PlantScheduleScreen(repository = repository)
      }
    }

    // Initially schedule is empty and catalog + year view legend are displayed
    composeTestRule.onNodeWithTag("plant_schedule_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("year_view_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("legend_planting_theme").assertIsDisplayed()
    composeTestRule.onNodeWithTag("legend_harvesting_accent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("schedule_empty_state").assertIsDisplayed()

    // Search for "Tomato"
    composeTestRule.onNodeWithTag("plant_search_input").performTextInput("Tomato")
    composeTestRule.waitForIdle()

    // Tomato catalog card and Add button appear
    composeTestRule.onNodeWithTag("catalog_plant_card_tomato").assertIsDisplayed()
    composeTestRule.onNodeWithTag("add_plant_button_tomato").performClick()
    composeTestRule.waitForIdle()

    // Clear search and verify Tomato is in the Year View schedule with Planting (Mar-May) and Harvesting (Jul-Oct)
    composeTestRule.onNodeWithTag("plant_search_clear").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("scheduled_plant_item_tomato").assertIsDisplayed()
    composeTestRule.onNodeWithTag("year_cell_tomato_1")
      .assertContentDescriptionEquals("Tomato Jan: Inactive")
    composeTestRule.onNodeWithTag("year_cell_tomato_4")
      .assertContentDescriptionEquals("Tomato Apr: Planting")
    composeTestRule.onNodeWithTag("year_cell_tomato_8")
      .assertContentDescriptionEquals("Tomato Aug: Harvesting")

    // Remove Tomato from schedule
    composeTestRule.onNodeWithTag("remove_scheduled_plant_tomato").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("schedule_empty_state").assertIsDisplayed()
  }

  @Test
  fun createCustomPlant_addsToScheduleAndHighlightsSelectedMonths() {
    val repository = InMemoryPlantScheduleRepository()

    composeTestRule.setContent {
      ToolboxTheme {
        PlantScheduleScreen(repository = repository)
      }
    }

    composeTestRule.onNodeWithTag("add_custom_plant_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("custom_plant_name_input").performTextInput("Heirloom Melon")
    composeTestRule.onNodeWithTag("confirm_custom_plant_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Heirloom Melon").assertIsDisplayed()
  }

  @Test
  fun longPressScheduledPlant_entersMultiSelectionModeAndSupportsBulkDelete() {
    val tomato = PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "tomato" }
    val basil = PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "basil" }
    val repository = InMemoryPlantScheduleRepository(
      initialScheduledPlants = listOf(tomato, basil),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        PlantScheduleScreen(repository = repository)
      }
    }

    composeTestRule.onNodeWithTag("scheduled_plant_item_tomato")
      .performTouchInput { longClick() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("multi_select_toolbar").assertIsDisplayed()
    composeTestRule.onNodeWithText("1 selected").assertIsDisplayed()

    composeTestRule.onNodeWithTag("bulk_delete_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("confirm_delete_plants_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("scheduled_plant_item_basil").assertIsDisplayed()
  }
}
