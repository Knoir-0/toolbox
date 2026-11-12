package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChordBookScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun reverseScaleSearch_switchesModeSelectsNotesAndJumpsToScale() {
    composeTestRule.setContent {
      ToolboxTheme {
        ChordBookScreen(tuningRepository = InMemoryGuitarTunerRepository())
      }
    }

    // 1. Navigate to Reverse tab
    composeTestRule.onNodeWithTag("chord_book_tab_REVERSE").performClick()
    composeTestRule.waitForIdle()

    // 2. Switch to Reverse Scale Search mode
    composeTestRule.onNodeWithTag("reverse_mode_scales").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    // Verify Reverse Scale Search controls and pitch class selector are displayed
    composeTestRule.onNodeWithTag("reverse_scale_pitch_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reverse_scale_matches_header").assertIsDisplayed()

    // 3. Clear selections, then select C(0), D(2), E(4), G(7), A(9) (C Major Pentatonic)
    composeTestRule.onNodeWithTag("reverse_clear_button").performClick()
    composeTestRule.waitForIdle()

    for (pc in listOf(0, 2, 4, 7, 9)) {
      composeTestRule.onNodeWithTag("reverse_scale_pitch_$pc").performClick()
    }
    composeTestRule.waitForIdle()

    // Verify top match card is displayed and jump to Scale view
    composeTestRule.onNodeWithTag("reverse_scale_match_0").assertIsDisplayed()
    composeTestRule.onNodeWithText("C Major Pentatonic").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reverse_scale_view_button_0").performClick()
    composeTestRule.waitForIdle()

    // 4. Verify we landed on the Scales & Key tab with C Major Pentatonic active,
    // and can jump back to Reverse Scale Search via the shortcut button
    composeTestRule.onNodeWithTag("active_scale_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scales_reverse_search_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("reverse_scale_pitch_0").assertIsDisplayed()
  }

  @Test
  fun tuningSearch_searchesSelectsAndSharesFavoriteTuningsWithGuitarTuner() {
    val sharedRepo = InMemoryGuitarTunerRepository(
      initialFavorites = setOf(GuitarTuningPreset.DROP_D),
    )
    composeTestRule.setContent {
      ToolboxTheme {
        ChordBookScreen(tuningRepository = sharedRepo)
      }
    }

    // Open the toolbar tuning search
    composeTestRule.onNodeWithTag("toggle_search_bar_button").performClick()
    composeTestRule.onNodeWithTag("toolbar_search_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("catalog_search_overlay").assertIsDisplayed()

    // Search for DADGAD, favorite it, and select it
    composeTestRule.onNodeWithTag("tuning_search_input").performTextInput("DADGAD")
    composeTestRule.onNodeWithTag("catalog_favorite_button_DADGAD").performClick()
    assertTrue(GuitarTuningPreset.DADGAD in sharedRepo.loadFavoritePresets())
    assertTrue(GuitarTuningPreset.DROP_D in sharedRepo.loadFavoritePresets())

    composeTestRule.onNodeWithTag("catalog_tuning_card_DADGAD").performClick()
    composeTestRule.onNodeWithTag("catalog_search_overlay").assertDoesNotExist()
    composeTestRule.onNodeWithTag("chord_book_header_subtext").assertIsDisplayed()
  }
}
