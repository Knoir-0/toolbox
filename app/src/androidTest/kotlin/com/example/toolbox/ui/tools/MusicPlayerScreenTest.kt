package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("artist_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Aurora Synth").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_card").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("artist_card_view").assertIsDisplayed()

    // 2. Albums Card & List View
    composeTestRule.onNodeWithTag("music_tab_ALBUMS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("album_card_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Midnight Odyssey").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_list").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("album_list_view").assertIsDisplayed()

    // 3. Songs List & Card View
    composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("song_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Neon Horizon").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_card").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_card_view").assertIsDisplayed()

    // 4. Genres Card & List View
    composeTestRule.onNodeWithTag("music_tab_GENRES").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_card_view").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_list").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("genre_list_view").assertIsDisplayed()

    // 5. Playlists List & Card View
    composeTestRule.onNodeWithTag("music_tab_PLAYLISTS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("playlist_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_card").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("playlist_card_view").assertIsDisplayed()

    // 6. Integrated Toolbar Search searches the currently selected view (Playlists)
    composeTestRule.onNodeWithTag("music_search_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_search_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_search_input").performTextInput("Fav")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_search_toggle").performClick()
    composeTestRule.waitForIdle()
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
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithText("#00B4D8").assertIsDisplayed()

    // Toggle slide-up queue panel from the bottom of the full player
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_queue_list").assertIsDisplayed()
    composeTestRule.onNodeWithTag("save_queue_as_playlist_button").assertIsDisplayed()

    // Save queue as a playlist from the top of the song queue
    composeTestRule.onNodeWithTag("save_queue_as_playlist_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("save_playlist_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirm_save_playlist_button").performClick()
    composeTestRule.waitForIdle()

    // Collapse queue panel back down
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").performClick()
    composeTestRule.waitForIdle()

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

    // Expand full player and open Track Details modal
    composeTestRule.onNodeWithTag("music_player_expand_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("expanded_player_info_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("track_details_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Colors").assertIsDisplayed()
    composeTestRule.onNodeWithText("Format").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bitrate").assertIsDisplayed()

    // Click one of the colors in the Track Details modal to open the Colors tool with that color selected
    composeTestRule.onNodeWithTag("track_details_color_1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("color_picker_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("color_picker_hero_hex").assertIsDisplayed()
  }
}
