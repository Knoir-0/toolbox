package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.toolbox.ui.main.ACCENT_COLOR_MUSIC_ACCENT
import com.example.toolbox.ui.main.ACCENT_COLOR_MUSIC_PLAYER
import com.example.toolbox.ui.main.ACCENT_COLOR_MUSIC_SECONDARY
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolboxScaffold
import com.example.toolbox.ui.theme.AccentHarmonyOption
import com.example.toolbox.ui.theme.MusicPlayerColorOption
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.theme.buildMusicPlayerAccentPrefValue
import com.example.toolbox.ui.theme.calculateAccentHarmonyColor
import com.example.toolbox.ui.theme.calculateMusicPlayerBackgroundColor
import com.example.toolbox.ui.theme.createTertiaryTokens
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MusicPlayerScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    MusicPlayerEngine.resetPlaybackSession()
    composeTestRule.activity.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }

  @After
  fun tearDown() {
    MusicPlayerEngine.resetPlaybackSession()
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
    composeTestRule.onNodeWithText("Most Played").assertIsDisplayed()

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
    composeTestRule.onNodeWithTag("expanded_player_speed_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_up_next_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithText("Queue").assertIsDisplayed()

    // Toggle slide-up queue panel from the bottom of the full player
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_queue_list").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_queue_drag_handle_0").assertIsDisplayed()
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

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun musicPlayer_continuesPlayingInBackgroundWhenSwitchedToAnotherTool() {
    composeTestRule.activity.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putBoolean(PREF_KEY_MUSIC_INCLUDE_SAMPLES, true)
      .commit()

    var selectedTool by mutableStateOf(Tool.MUSIC)

    composeTestRule.setContent {
      ToolboxTheme {
        ToolboxScaffold(
          selectedTool = selectedTool,
          onSelectTool = { selectedTool = it },
          initialTools = listOf(Tool.MUSIC, Tool.CALCULATOR, Tool.NOTES, Tool.SETTINGS),
          preferencesRepository = null,
        )
      }
    }
    composeTestRule.waitForIdle()

    // Select a song in the Music tool and start playing
    composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_item_sample_autumn_coffee").performClick()
    composeTestRule.waitForIdle()

    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)
    assertTrue(session!!.isPlaying)
    assertEquals("sample_autumn_coffee", session.currentTrack?.id)

    // Switch to the Calculator tool via the bottom tool navigation panel
    composeTestRule.onNodeWithTag("tool_item_CALCULATOR").performClick()
    composeTestRule.waitForIdle()

    // Calculator tool is now displayed; MusicPlayerScreen is no longer in composition
    composeTestRule.onNodeWithTag("calculator_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_player_screen").assertDoesNotExist()

    // Verify music continues playing in the background while Calculator is open
    assertTrue(session.isPlaying)
    assertEquals("sample_autumn_coffee", session.currentTrack?.id)
    composeTestRule.mainClock.advanceTimeBy(600L)
    composeTestRule.waitForIdle()
    assertTrue(session.isPlaying)

    // Switch back to the Music tool and verify playback state and track are still active
    composeTestRule.onNodeWithTag("tool_item_MUSIC").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_player_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_player_collapsed").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Mini music player background #D97706").assertExists()
    assertTrue(session.isPlaying)
    assertEquals("sample_autumn_coffee", session.currentTrack?.id)
  }

  @Test
  fun settingsScreen_accentColorMusicPlayerOption_updatesThemeBackgroundAndHarmonyAccentDynamicallyWithMusicTrack() {
    val prefsRepo = InMemoryNavigationPreferencesRepository()
    var observedTertiaryColor: Color? = null
    var observedBackgroundColor: Color? = null

    composeTestRule.setContent {
      val accentColor by prefsRepo.accentColorFlow.collectAsState()
      ToolboxTheme(
        darkTheme = false,
        dynamicColor = false,
        accentColor = accentColor,
      ) {
        observedTertiaryColor = MaterialTheme.colorScheme.tertiary
        observedBackgroundColor = MaterialTheme.colorScheme.background
        SettingsScreen(
          preferencesRepository = prefsRepo,
          initialVisibilityExpanded = false,
          initialAccentColorExpanded = true,
        )
      }
    }
    composeTestRule.waitForIdle()

    val baseBackgroundColor = observedBackgroundColor!!

    // Select the Music Player Colors option card in Accent Color settings
    composeTestRule.onNodeWithTag("accent_preset_music_player")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertEquals(ACCENT_COLOR_MUSIC_PLAYER, prefsRepo.getAccentColor())
    val firstPalette = MusicPlayerEngine.activeTrackPalette
    assertEquals(
      calculateMusicPlayerBackgroundColor(baseBackgroundColor, firstPalette, MusicPlayerColorOption.DOMINANT),
      observedBackgroundColor,
    )
    assertEquals(
      createTertiaryTokens(
        calculateAccentHarmonyColor(firstPalette.dominantColor, AccentHarmonyOption.COMPLEMENTARY),
        darkTheme = false,
      ).tertiary,
      observedTertiaryColor,
    )

    // Switch harmony mode to Triadic while Music Player Colors is active:
    // Accent color is calculated off the Music Player color using Triadic harmony while background stays music-tinted
    composeTestRule.onNodeWithTag("accent_harmony_mode_triadic")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertEquals(
      buildMusicPlayerAccentPrefValue(MusicPlayerColorOption.DOMINANT, AccentHarmonyOption.TRIADIC),
      prefsRepo.getAccentColor(),
    )
    assertEquals(
      calculateMusicPlayerBackgroundColor(baseBackgroundColor, firstPalette, MusicPlayerColorOption.DOMINANT),
      observedBackgroundColor,
    )
    assertEquals(
      createTertiaryTokens(
        calculateAccentHarmonyColor(firstPalette.dominantColor, AccentHarmonyOption.TRIADIC),
        darkTheme = false,
      ).tertiary,
      observedTertiaryColor,
    )

    // Select the Secondary swatch from the Music Player Palette section (keeps Triadic harmony)
    composeTestRule.onNodeWithTag("accent_music_swatch_secondary")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertEquals(
      buildMusicPlayerAccentPrefValue(MusicPlayerColorOption.SECONDARY, AccentHarmonyOption.TRIADIC),
      prefsRepo.getAccentColor(),
    )
    assertEquals(
      calculateMusicPlayerBackgroundColor(baseBackgroundColor, firstPalette, MusicPlayerColorOption.SECONDARY),
      observedBackgroundColor,
    )
    assertEquals(
      createTertiaryTokens(
        calculateAccentHarmonyColor(firstPalette.secondaryColor, AccentHarmonyOption.TRIADIC),
        darkTheme = false,
      ).tertiary,
      observedTertiaryColor,
    )

    // Select the Accent swatch from the Music Player Palette section
    composeTestRule.onNodeWithTag("accent_music_swatch_accent")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertEquals(
      buildMusicPlayerAccentPrefValue(MusicPlayerColorOption.ACCENT, AccentHarmonyOption.TRIADIC),
      prefsRepo.getAccentColor(),
    )
    assertEquals(
      calculateMusicPlayerBackgroundColor(baseBackgroundColor, firstPalette, MusicPlayerColorOption.ACCENT),
      observedBackgroundColor,
    )
    assertEquals(
      createTertiaryTokens(
        calculateAccentHarmonyColor(firstPalette.accentColor, AccentHarmonyOption.TRIADIC),
        darkTheme = false,
      ).tertiary,
      observedTertiaryColor,
    )

    // Simulate the Music Player changing tracks in the background and verify ToolboxTheme updates reactively
    val nextTrack = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS[1]
    val nextPalette = MusicPlayerEngine.resolveTrackPalette(nextTrack)
    composeTestRule.runOnIdle {
      MusicPlayerEngine.updateActiveTrackPalette(nextTrack, nextPalette)
    }
    composeTestRule.waitForIdle()

    assertEquals(
      calculateMusicPlayerBackgroundColor(baseBackgroundColor, nextPalette, MusicPlayerColorOption.ACCENT),
      observedBackgroundColor,
    )
    assertEquals(
      createTertiaryTokens(
        calculateAccentHarmonyColor(nextPalette.accentColor, AccentHarmonyOption.TRIADIC),
        darkTheme = false,
      ).tertiary,
      observedTertiaryColor,
    )
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun navigationPaneExpanded_replacesToolsPerRowWithMiniPlayerWhenMusicPlayingOutsideMusicTool() {
    composeTestRule.activity.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putBoolean(PREF_KEY_MUSIC_INCLUDE_SAMPLES, true)
      .commit()

    var selectedTool by mutableStateOf(Tool.MUSIC)

    composeTestRule.setContent {
      ToolboxTheme {
        ToolboxScaffold(
          selectedTool = selectedTool,
          onSelectTool = { selectedTool = it },
          initialTools = listOf(Tool.MUSIC, Tool.CALCULATOR, Tool.NOTES, Tool.SETTINGS),
          preferencesRepository = null,
        )
      }
    }
    composeTestRule.waitForIdle()

    // Start playing a song in the Music tool
    composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_item_sample_autumn_coffee").performClick()
    composeTestRule.waitForIdle()

    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)
    assertTrue(session!!.isPlaying)

    // While still inside the Music tool, pull up the navigation pane:
    // ToolsPerRowControl should be shown (not the navigation mini player)
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()
    composeTestRule.onNodeWithTag("navigation_mini_player").assertDoesNotExist()

    // Switch to the Calculator tool (which collapses the navigation pane)
    composeTestRule.onNodeWithTag("tool_item_CALCULATOR").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("calculator_screen").assertIsDisplayed()

    // Pull the navigation pane all the way up while in the Calculator tool with music playing:
    // The entire tools-per-row toolbar should be replaced by the navigation mini player
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("navigation_mini_player").assertIsDisplayed()
    composeTestRule.onNodeWithTag("navigation_mini_player_title", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertDoesNotExist()

    // Pausing playback from the navigation mini player restores the tools-per-row toolbar
    composeTestRule.onNodeWithTag("navigation_mini_player_play_pause_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()
    composeTestRule.onNodeWithTag("navigation_mini_player").assertDoesNotExist()
  }
}
