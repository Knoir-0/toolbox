package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MusicPlayerScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.activity.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }

  @Test
  fun musicPlayer_supportsArtistAlbumSongAndGenreListAndCardViews() {
    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS,
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Artists List & Card View
    composeTestRule.onNodeWithTag("music_tab_ARTISTS").assertIsDisplayed()
    composeTestRule.onNodeWithTag("artist_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Aurora Synth").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_card").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("artist_card_view").assertIsDisplayed()

    // 2. Albums Card & List View
    composeTestRule.onNodeWithTag("music_tab_ALBUMS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("album_card_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Midnight Odyssey").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_list").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("album_list_view").assertIsDisplayed()

    // 3. Songs List & Card View
    composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Neon Horizon").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_card").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_card_view").assertIsDisplayed()

    // 4. Genres Card & List View
    composeTestRule.onNodeWithTag("music_tab_GENRES").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("genre_card_view").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_list").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("genre_list_view").assertIsDisplayed()
  }

  @Test
  fun collapsibleMusicPlayer_expandsAndCollapses_andBackgroundFollowsAlbumArt() {
    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS,
        )
      }
    }
    composeTestRule.waitForIdle()

    // Mini player is visible at bottom with initial track ("Neon Horizon", #00B4D8)
    composeTestRule.onNodeWithTag("music_player_collapsed").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Mini music player background #00B4D8").assertExists()

    // Expand full music player
    composeTestRule.onNodeWithTag("music_player_expand_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("music_player_expanded").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_seek_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_shuffle_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_prev_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_play_pause_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_next_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_repeat_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_volume_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_speed_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_up_next_button").assertIsDisplayed()
    composeTestRule.onNodeWithText("#00B4D8").assertIsDisplayed()

    // Collapse back down to mini player
    composeTestRule.onNodeWithTag("music_player_collapse_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_player_collapsed").assertIsDisplayed()

    // Switch to Songs tab and select a Jazz track ("Autumn Leaves & Coffee", #D97706)
    composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_item_sample_autumn_coffee").performClick()
    composeTestRule.waitForIdle()

    // Verify player background dynamically updated to follow the new album art (#D97706)
    composeTestRule.onNodeWithContentDescription("Mini music player background #D97706").assertExists()
  }
}
