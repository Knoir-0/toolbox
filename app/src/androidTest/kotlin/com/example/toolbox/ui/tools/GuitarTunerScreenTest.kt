package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GuitarTunerScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun initialScreen_whenNoNoteDetected_doesNotDisplayNote() {
    val repo = InMemoryGuitarTunerRepository()
    composeTestRule.setContent {
      ToolboxTheme {
        GuitarTunerScreen(repository = repo)
      }
    }

    // When no note is detected, the detected note readout does not exist
    composeTestRule.onNodeWithTag("detected_note").assertDoesNotExist()

    // Header badge indicates no note is detected
    composeTestRule.onNodeWithText("No note detected").assertIsDisplayed()

    // Status indicates listening rather than in tune
    composeTestRule.onNodeWithText("LISTENING").assertIsDisplayed()
    composeTestRule.onNodeWithText("IN TUNE").assertDoesNotExist()
  }

  @Test
  fun quickTappingTuningBelowTuner_switchesActiveTuning() {
    val repo = InMemoryGuitarTunerRepository()
    composeTestRule.setContent {
      ToolboxTheme {
        GuitarTunerScreen(repository = repo)
      }
    }

    // Scroll to Drop D in the tunings list below the tuner and string display and tap it
    composeTestRule
      .onNodeWithTag("tuner_main_list")
      .performScrollToNode(hasTestTag("tuning_card_DROP_D"))
    composeTestRule.onNodeWithTag("tuning_card_DROP_D").performClick()

    // Toolbar subtitle and target frequency reflect Drop D (6th string D2 = 73.42 Hz)
    composeTestRule.onNodeWithText("Drop D • D A D G B E").assertIsDisplayed()
    composeTestRule
      .onNodeWithTag("tuner_main_list")
      .performScrollToNode(hasTestTag("tuner_gauge_card"))
    composeTestRule.onNodeWithText("73.42 Hz").assertIsDisplayed()
  }

  @Test
  fun favoritingTunings_persistsAndFiltersFavorites() {
    val repo = InMemoryGuitarTunerRepository()
    composeTestRule.setContent {
      ToolboxTheme {
        GuitarTunerScreen(repository = repo)
      }
    }

    // Scroll to Drop D and favorite it
    composeTestRule
      .onNodeWithTag("tuner_main_list")
      .performScrollToNode(hasTestTag("favorite_tuning_button_DROP_D"))
    composeTestRule.onNodeWithTag("favorite_tuning_button_DROP_D").performClick()

    assertTrue(GuitarTuningPreset.DROP_D in repo.loadFavoritePresets())

    // Filter by Favorites chip
    composeTestRule
      .onNodeWithTag("tuner_main_list")
      .performScrollToNode(hasTestTag("tuning_filter_row"))
    composeTestRule.onNodeWithTag("tuning_filter_favorites").performClick()

    composeTestRule.onNodeWithTag("tuning_card_DROP_D").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tuning_card_STANDARD").assertDoesNotExist()
  }

  @Test
  fun toolbarSearchButton_opensSearchOverlayAndSelectsTuning() {
    val repo = InMemoryGuitarTunerRepository()
    composeTestRule.setContent {
      ToolboxTheme {
        GuitarTunerScreen(repository = repo)
      }
    }

    // Tap the toolbar search button
    composeTestRule.onNodeWithTag("toggle_search_bar_button").performClick()

    // Search bar and catalog search overlay are displayed
    composeTestRule.onNodeWithTag("toolbar_search_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("catalog_search_overlay").assertIsDisplayed()

    // Type "DADGAD" into the search input
    composeTestRule.onNodeWithTag("tuning_search_input").performTextInput("DADGAD")
    composeTestRule.onNodeWithTag("catalog_tuning_card_DADGAD").assertIsDisplayed()

    // Tap DADGAD in the search results to switch to it and close search
    composeTestRule.onNodeWithTag("catalog_tuning_card_DADGAD").performClick()

    composeTestRule.onNodeWithTag("catalog_search_overlay").assertDoesNotExist()
    composeTestRule.onNodeWithText("DADGAD • D A D G A D").assertIsDisplayed()
  }
}
