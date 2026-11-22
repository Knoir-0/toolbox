package com.example.toolbox.ui.tools

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_TRIADIC
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.THEME_COLOR_MUSIC_ACCENT
import com.example.toolbox.ui.main.THEME_COLOR_MUSIC_PLAYER
import com.example.toolbox.ui.main.THEME_COLOR_MUSIC_SECONDARY
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolboxScaffold
import com.example.toolbox.ui.theme.AccentHarmonyOption
import com.example.toolbox.ui.theme.MusicPlayerColorOption
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.theme.calculateAccentHarmonyColor
import com.example.toolbox.ui.theme.calculateMusicPlayerBackgroundColor
import com.example.toolbox.ui.theme.createCustomColorScheme
import com.example.toolbox.ui.theme.createTertiaryTokens
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // 0. Home Pill is at the front of the pill list, selected by default, displays 6 generated playlists, and refreshes on pull-down
    composeTestRule.onNodeWithTag("music_tab_HOME").assertIsDisplayed().assertIsSelected()
    composeTestRule.onNodeWithTag("home_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("home_playlist_item_0").assertIsDisplayed()
    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)
    assertEquals(
      listOf(
        MusicLibraryTab.HOME,
        MusicLibraryTab.PLAYLISTS,
        MusicLibraryTab.SONGS,
        MusicLibraryTab.ALBUMS,
        MusicLibraryTab.ARTISTS,
        MusicLibraryTab.GENRES,
      ),
      session!!.tabOrder,
    )
    assertEquals(MusicLibraryTab.HOME, session.tabOrder.first())
    assertEquals(6, session.homePlaylists.size)
    val initialFirstMixName = session.homePlaylists.first().name
    composeTestRule.onNodeWithText(initialFirstMixName).assertIsDisplayed()

    // Pull down to refresh on Home tab -> generates 6 fresh playlists
    composeTestRule.onNodeWithTag("home_pull_to_refresh").performTouchInput {
      swipeDown(startY = top + 60f, endY = bottom - 60f)
    }
    composeTestRule.waitForIdle()
    assertTrue(session.homeRefreshGeneration >= 1)
    assertEquals(6, session.homePlaylists.size)

    // 1. Artists List & Card View
    composeTestRule.onNodeWithTag("music_tab_ARTISTS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("artist_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Aurora Synth").assertIsDisplayed()
    composeTestRule.onNodeWithTag("artist_more_Aurora Synth").assertIsDisplayed()

    // Cycle toolbar view button: List -> 2-column Card
    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_columns_per_row_count").assertTextEquals("2")
    composeTestRule.onNodeWithTag("artist_card_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("artist_more_Aurora Synth").assertIsDisplayed()

    // 2. Albums Card & List View (cycle 2 -> 3 -> List)
    composeTestRule.onNodeWithTag("music_tab_ALBUMS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("album_card_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Midnight Odyssey").assertIsDisplayed()
    composeTestRule.onNodeWithTag("album_more_Midnight Odyssey").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_columns_per_row_count").assertTextEquals("3")
    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("album_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("album_more_Midnight Odyssey").assertIsDisplayed()

    // 3. Songs List & Card View (cycle List -> 2-column Card)
    composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("song_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Neon Horizon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("song_more_sample_neon_horizon").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_columns_per_row_count").assertTextEquals("2")
    composeTestRule.onNodeWithTag("song_card_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("song_more_sample_neon_horizon").assertIsDisplayed()

    // 4. Genres Card & List View (cycle 2 -> 3 -> List)
    composeTestRule.onNodeWithTag("music_tab_GENRES").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_card_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_more_Synthwave").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_columns_per_row_count").assertTextEquals("3")
    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("genre_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_more_Synthwave").assertIsDisplayed()

    // 5. Playlists List & Card View (cycle List -> 2-column Card)
    composeTestRule.onNodeWithTag("music_tab_PLAYLISTS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_play_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_shuffle_all_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("playlist_list_view").assertIsDisplayed()
    composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    composeTestRule.onNodeWithText("Most Played").assertIsDisplayed()
    composeTestRule.onNodeWithText("Recently Played").assertIsDisplayed()
    composeTestRule.onNodeWithTag("playlist_more_Favorites").assertIsDisplayed()

    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_columns_per_row_count").assertTextEquals("2")
    composeTestRule.onNodeWithTag("playlist_card_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("playlist_more_Favorites").assertIsDisplayed()

    // 6. Integrated Toolbar Search searches the currently selected view (Playlists) and hides toolbar icons when searching
    composeTestRule.onNodeWithTag("music_view_mode_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_search_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_search_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_view_mode_toggle").assertDoesNotExist()
    composeTestRule.onNodeWithTag("music_search_input").performTextInput("Fav")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_search_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_view_mode_toggle").assertIsDisplayed()

    // 7. Sort button is pinned on the right inside the pill bar, includes Random option, and press and hold reorders section tabs
    composeTestRule.onNodeWithTag("music_sort_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_sort_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_sort_option_random").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_sort_option_title_asc").performClick()
    composeTestRule.waitForIdle()
    val barBounds = composeTestRule.onNodeWithTag("music_category_bar").fetchSemanticsNode().boundsInRoot
    val sortBounds = composeTestRule.onNodeWithTag("music_sort_button").fetchSemanticsNode().boundsInRoot
    assertTrue(sortBounds.top >= barBounds.top - 1f && sortBounds.bottom <= barBounds.bottom + 1f)
    assertTrue(sortBounds.center.x > barBounds.center.x)
    val homeBoundsBefore = composeTestRule.onNodeWithTag("music_tab_HOME").fetchSemanticsNode().boundsInRoot
    val playlistsBoundsBefore = composeTestRule.onNodeWithTag("music_tab_PLAYLISTS").fetchSemanticsNode().boundsInRoot
    val songsBoundsBefore = composeTestRule.onNodeWithTag("music_tab_SONGS").fetchSemanticsNode().boundsInRoot
    assertTrue(homeBoundsBefore.left < playlistsBoundsBefore.left)
    assertTrue(playlistsBoundsBefore.left < songsBoundsBefore.left)

    val startLocal = Offset(
      x = songsBoundsBefore.center.x - barBounds.left,
      y = songsBoundsBefore.center.y - barBounds.top,
    )
    val endLocal = Offset(
      x = homeBoundsBefore.center.x - barBounds.left,
      y = homeBoundsBefore.center.y - barBounds.top,
    )
    composeTestRule.onNodeWithTag("music_category_bar").performTouchInput {
      down(startLocal)
      advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
      moveTo(endLocal)
      up()
    }
    composeTestRule.waitForIdle()

    assertEquals(MusicLibraryTab.HOME, session.tabOrder.first())
    assertEquals(MusicLibraryTab.SONGS, session.tabOrder[1])
    val homeBoundsAfter = composeTestRule.onNodeWithTag("music_tab_HOME").fetchSemanticsNode().boundsInRoot
    val playlistsBoundsAfter = composeTestRule.onNodeWithTag("music_tab_PLAYLISTS").fetchSemanticsNode().boundsInRoot
    val songsBoundsAfter = composeTestRule.onNodeWithTag("music_tab_SONGS").fetchSemanticsNode().boundsInRoot
    assertTrue(homeBoundsAfter.left < songsBoundsAfter.left)
    assertTrue(songsBoundsAfter.left < playlistsBoundsAfter.left)
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

    // When first loaded, no queue is loaded until the user chooses
    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)
    assertTrue(session!!.playbackQueue.isEmpty())
    composeTestRule.onNodeWithTag("music_player_collapsed").assertDoesNotExist()

    // User chooses to play via Play All -> queue loads and mini player appears with initial track ("Neon Horizon", #00B4D8) and no carat indicator
    composeTestRule.onNodeWithTag("music_play_all_button").performClick()
    composeTestRule.waitForIdle()
    assertFalse(session.playbackQueue.isEmpty())
    composeTestRule.onNodeWithTag("music_player_collapsed").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_player_expand_button").assertDoesNotExist()
    composeTestRule.onNodeWithContentDescription("Mini music player background #00B4D8").assertExists()

    // Expand full music player by tapping the mini player
    composeTestRule.onNodeWithTag("mini_player_album_art").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("music_player_expanded").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_album_art").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_favorite_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_seek_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_shuffle_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_prev_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_play_pause_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_next_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_repeat_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_speed_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_up_next_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_queue_grab_handle").assertIsDisplayed()
    composeTestRule.onNodeWithText("Queue").assertIsDisplayed()

    // Drag or toggle slide-up queue panel from the bottom of the full player:
    // album art is pushed out of the way while title and controls remain visible above the queue
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("expanded_player_album_art").assertDoesNotExist()
    composeTestRule.onNodeWithTag("expanded_player_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_play_pause_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_seek_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_queue_list").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_queue_drag_handle_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("save_queue_as_playlist_button").assertIsDisplayed()

    // Save queue as a playlist from the top of the song queue
    composeTestRule.onNodeWithTag("save_queue_as_playlist_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("save_playlist_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirm_save_playlist_button").performClick()
    composeTestRule.waitForIdle()

    // Collapse queue panel back down -> album art returns
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("expanded_player_album_art").assertIsDisplayed()

    // Collapse back down to mini player by swiping down on the expanded music player
    composeTestRule.onNodeWithTag("music_player_expanded").performTouchInput {
      swipeDown(startY = top + 120f, endY = centerY + 240f)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_player_collapsed").assertIsDisplayed()

    // Switch to Songs tab and select a Jazz track ("Autumn Leaves & Coffee", #D97706)
    composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_list_view")
      .performScrollToNode(hasTestTag("song_item_sample_autumn_coffee"))
    composeTestRule.onNodeWithTag("song_item_sample_autumn_coffee").performClick()
    composeTestRule.waitForIdle()

    // Verify player background dynamically updated to follow the new album art (#D97706)
    composeTestRule.onNodeWithContentDescription("Mini music player background #D97706").assertExists()

    // Expand full player and open Track Details modal
    composeTestRule.onNodeWithTag("mini_player_album_art").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("expanded_player_info_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("track_details_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Colors").assertIsDisplayed()

    // Page 1 (Song Info) is active by default
    composeTestRule.onNodeWithTag("track_details_tab_SONG_INFO").assertIsDisplayed().assertIsSelected()
    composeTestRule.onNodeWithText("Artist").assertIsDisplayed()
    composeTestRule.onNodeWithText("Track #").assertIsDisplayed()

    // Switch to Page 2 (Stats)
    composeTestRule.onNodeWithTag("track_details_tab_STATS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("track_details_tab_STATS").assertIsSelected()
    composeTestRule.onNodeWithText("Full Plays").assertIsDisplayed()
    composeTestRule.onNodeWithText("Skips").assertIsDisplayed()
    composeTestRule.onNodeWithText("Last Played").assertIsDisplayed()

    // Switch to Page 3 (File Info)
    composeTestRule.onNodeWithTag("track_details_tab_FILE_INFO").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("track_details_tab_FILE_INFO").assertIsSelected()
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
    composeTestRule.onNodeWithTag("song_list_view")
      .performScrollToNode(hasTestTag("song_item_sample_autumn_coffee"))
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
  fun settingsScreen_themeColorMusicPlayerOption_updatesMainColorBackgroundAndHarmonyAccentDynamicallyWithMusicTrack() {
    val prefsRepo = InMemoryNavigationPreferencesRepository()
    var observedPrimaryColor: Color? = null
    var observedTertiaryColor: Color? = null
    var observedBackgroundColor: Color? = null

    composeTestRule.setContent {
      val themeColor by prefsRepo.themeColorFlow.collectAsState()
      val musicPlayerThemeEnabled by prefsRepo.musicPlayerThemeEnabledFlow.collectAsState()
      val musicPlayerColorOption by prefsRepo.musicPlayerColorOptionFlow.collectAsState()
      val accentColor by prefsRepo.accentColorFlow.collectAsState()
      ToolboxTheme(
        darkTheme = false,
        dynamicColor = false,
        themeColor = themeColor,
        accentColor = accentColor,
        musicPlayerThemeEnabled = musicPlayerThemeEnabled,
        musicPlayerColorOption = musicPlayerColorOption,
      ) {
        observedPrimaryColor = MaterialTheme.colorScheme.primary
        observedTertiaryColor = MaterialTheme.colorScheme.tertiary
        observedBackgroundColor = MaterialTheme.colorScheme.background
        SettingsScreen(
          preferencesRepository = prefsRepo,
          initialVisibilityExpanded = false,
          initialThemeColorExpanded = true,
          initialAccentColorExpanded = true,
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Verify Music Player checkbox is checked by default, and since music is not playing,
    // the app falls back to the user's selected theme color (default #8E66A6 fallback when dynamicColor = false)
    assertTrue(prefsRepo.getMusicPlayerThemeEnabled())
    assertEquals(DEFAULT_THEME_COLOR_SYSTEM, prefsRepo.getThemeColor())
    composeTestRule.onNodeWithTag("color_preset_music_player")
      .performScrollTo()
      .assertIsOn()
    assertEquals(Color(0xFF8E66A6), observedPrimaryColor)

    // Select a user fallback theme color (#386A20 Forest Green) and verify it applies while music is not playing,
    // while keeping the Music Player checkbox checked
    composeTestRule.onNodeWithTag("color_swatch_#386A20")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()
    assertEquals("#386A20", prefsRepo.getThemeColor())
    assertTrue(prefsRepo.getMusicPlayerThemeEnabled())
    assertEquals(Color(0xFF386A20), observedPrimaryColor)

    // 2. Start playing music: since the Music Player checkbox is checked by default,
    // the app themes according to the music player's active track palette
    composeTestRule.runOnIdle {
      MusicPlayerEngine.setMusicPlayingForTest(true)
    }
    composeTestRule.waitForIdle()

    assertEquals(THEME_COLOR_MUSIC_PLAYER, prefsRepo.getMusicPlayerColorOption())
    val firstPalette = MusicPlayerEngine.activeTrackPalette
    val customBaseBg = createCustomColorScheme(firstPalette.dominantColor, darkTheme = false).background
    assertEquals(firstPalette.dominantColor, observedPrimaryColor)
    assertEquals(
      calculateMusicPlayerBackgroundColor(customBaseBg, firstPalette, MusicPlayerColorOption.DOMINANT),
      observedBackgroundColor,
    )
    assertEquals(
      createTertiaryTokens(
        calculateAccentHarmonyColor(firstPalette.dominantColor, AccentHarmonyOption.ANALOGOUS),
        darkTheme = false,
      ).tertiary,
      observedTertiaryColor,
    )

    // Switch harmony mode to Triadic in Accent Color settings while Music Player Colors is active on Theme Color:
    // Accent color is calculated off the Music Player main color using Triadic harmony while background stays music-tinted
    composeTestRule.onNodeWithTag("accent_harmony_mode_triadic")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertEquals(ACCENT_COLOR_AUTO_TRIADIC, prefsRepo.getAccentColor())
    assertEquals(
      calculateMusicPlayerBackgroundColor(customBaseBg, firstPalette, MusicPlayerColorOption.DOMINANT),
      observedBackgroundColor,
    )
    assertEquals(
      createTertiaryTokens(
        calculateAccentHarmonyColor(firstPalette.dominantColor, AccentHarmonyOption.TRIADIC),
        darkTheme = false,
      ).tertiary,
      observedTertiaryColor,
    )

    // Select the Secondary swatch from the Music Player Palette section in Theme Color
    composeTestRule.onNodeWithTag("theme_music_swatch_secondary")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertEquals(THEME_COLOR_MUSIC_SECONDARY, prefsRepo.getMusicPlayerColorOption())
    assertEquals("#386A20", prefsRepo.getThemeColor())
    assertEquals(firstPalette.secondaryColor, observedPrimaryColor)
    assertEquals(
      calculateMusicPlayerBackgroundColor(customBaseBg, firstPalette, MusicPlayerColorOption.SECONDARY),
      observedBackgroundColor,
    )
    assertEquals(
      createTertiaryTokens(
        calculateAccentHarmonyColor(firstPalette.secondaryColor, AccentHarmonyOption.TRIADIC),
        darkTheme = false,
      ).tertiary,
      observedTertiaryColor,
    )

    // Select the Accent swatch from the Music Player Palette section in Theme Color
    composeTestRule.onNodeWithTag("theme_music_swatch_accent")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertEquals(THEME_COLOR_MUSIC_ACCENT, prefsRepo.getMusicPlayerColorOption())
    assertEquals(firstPalette.accentColor, observedPrimaryColor)
    assertEquals(
      calculateMusicPlayerBackgroundColor(customBaseBg, firstPalette, MusicPlayerColorOption.ACCENT),
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

    assertEquals(nextPalette.accentColor, observedPrimaryColor)
    assertEquals(
      calculateMusicPlayerBackgroundColor(customBaseBg, nextPalette, MusicPlayerColorOption.ACCENT),
      observedBackgroundColor,
    )
    assertEquals(
      createTertiaryTokens(
        calculateAccentHarmonyColor(nextPalette.accentColor, AccentHarmonyOption.TRIADIC),
        darkTheme = false,
      ).tertiary,
      observedTertiaryColor,
    )

    // 3. Uncheck the Music Player checkbox while music is playing: app falls back to user's selected theme color (#386A20)
    composeTestRule.onNodeWithTag("color_preset_music_player")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertFalse(prefsRepo.getMusicPlayerThemeEnabled())
    composeTestRule.onNodeWithTag("color_preset_music_player").assertIsOff()
    assertEquals(Color(0xFF386A20), observedPrimaryColor)

    // Re-check the Music Player checkbox while music is playing: app themes according to the music player again
    composeTestRule.onNodeWithTag("color_preset_music_player")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    assertTrue(prefsRepo.getMusicPlayerThemeEnabled())
    composeTestRule.onNodeWithTag("color_preset_music_player").assertIsOn()
    assertEquals(nextPalette.accentColor, observedPrimaryColor)

    // 4. Stop/pause music playback while checkbox is checked: app falls back to user's selected theme color (#386A20)
    composeTestRule.runOnIdle {
      MusicPlayerEngine.setMusicPlayingForTest(false)
    }
    composeTestRule.waitForIdle()
    assertEquals(Color(0xFF386A20), observedPrimaryColor)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun navigationPaneExpanded_keepsToolsPerRowWhenMusicPlayingOutsideMusicTool() {
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
    composeTestRule.onNodeWithTag("song_list_view")
      .performScrollToNode(hasTestTag("song_item_sample_autumn_coffee"))
    composeTestRule.onNodeWithTag("song_item_sample_autumn_coffee").performClick()
    composeTestRule.waitForIdle()

    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)
    assertTrue(session!!.isPlaying)

    // While still inside the Music tool, pull up the navigation pane:
    // ToolsPerRowControl should be shown (no navigation mini player)
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()
    composeTestRule.onNodeWithTag("navigation_mini_player").assertDoesNotExist()

    // Switch to the Calculator tool (which collapses the navigation pane)
    composeTestRule.onNodeWithTag("tool_item_CALCULATOR").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("calculator_screen").assertIsDisplayed()

    // Pull the navigation pane all the way up while in the Calculator tool with music playing:
    // The tools-per-row toolbar remains displayed and no navigation mini player is shown
    composeTestRule.onNodeWithTag("pull_up_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tools_per_row_control").assertIsDisplayed()
    composeTestRule.onNodeWithTag("navigation_mini_player").assertDoesNotExist()
  }

  @Test
  fun musicPlayer_preFocusesCurrentlyPlayingSongWhenSwitchingSectionsAndOpeningQueue() {
    // Build a larger 20-track library so bottom items are guaranteed off-screen unless pre-focused
    val extraTracks = (1..10).map { idx ->
      MusicTrack(
        id = "extra_track_$idx",
        title = "AAA Prelude ${idx.toString().padStart(2, '0')}",
        artist = "AAA Ensemble ${idx.toString().padStart(2, '0')}",
        album = "AAA Opus ${idx.toString().padStart(2, '0')}",
        genre = "AAA Genre ${idx.toString().padStart(2, '0')}",
        durationMs = 180_000L,
        year = 2025,
        trackNumber = idx,
        artworkPrimaryRgb = 0x3B82F6,
        artworkSecondaryRgb = 0x1D4ED8,
        artworkAccentRgb = 0x93C5FD,
      )
    }
    val allTracks = extraTracks + MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val targetTrack = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.first { it.id == "sample_starlight_drive" }

    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = allTracks,
        )
      }
    }
    composeTestRule.waitForIdle()

    // Start playing "Starlight Drive" (by Aurora Synth, album "Midnight Odyssey", genre "Synthwave")
    // near the end of the 20-track queue
    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)
    composeTestRule.runOnIdle {
      session!!.playTrackInContext(targetTrack, allTracks)
    }
    composeTestRule.waitForIdle()

    val expectedQueueIndex = allTracks.indexOfFirst { it.id == targetTrack.id }
    assertTrue(expectedQueueIndex >= 10)

    // 1. Switch to Songs section -> pre-focused on "Starlight Drive" (alphabetically last in the 20-track list)
    composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_item_sample_starlight_drive")
      .assertIsDisplayed()
      .assertIsSelected()

    // Favorites button on Songs quick action row matches the height of Play All and Shuffle buttons
    val playAllBounds = composeTestRule.onNodeWithTag("music_play_all_button").fetchSemanticsNode().boundsInRoot
    val shuffleAllBounds = composeTestRule.onNodeWithTag("music_shuffle_all_button").fetchSemanticsNode().boundsInRoot
    val favoritesButtonBounds = composeTestRule.onNodeWithTag("music_favorites_filter_chip").fetchSemanticsNode().boundsInRoot
    assertEquals(playAllBounds.height, favoritesButtonBounds.height, 1f)
    assertEquals(shuffleAllBounds.height, favoritesButtonBounds.height, 1f)

    // Switch to Card view in Songs section -> still pre-focused on "Starlight Drive"
    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_item_sample_starlight_drive")
      .assertIsDisplayed()
      .assertIsSelected()

    // Switch back to List view (2 -> 3 -> List)
    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_view_mode_toggle").performClick()
    composeTestRule.waitForIdle()

    // 2. Switch to Albums section -> pre-focused on "Midnight Odyssey"
    composeTestRule.onNodeWithTag("music_tab_ALBUMS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("album_item_Midnight Odyssey")
      .assertIsDisplayed()
      .assertIsSelected()

    // 3. Switch to Genres section -> pre-focused on "Synthwave"
    composeTestRule.onNodeWithTag("music_tab_GENRES").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("genre_item_Synthwave")
      .assertIsDisplayed()
      .assertIsSelected()

    // 4. Switch to Artists section -> pre-focused on "Aurora Synth"
    composeTestRule.onNodeWithTag("music_tab_ARTISTS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("artist_item_Aurora Synth")
      .assertIsDisplayed()
      .assertIsSelected()

    // 5. Switch to Playlists section -> Most Played contains the active track and is selected
    composeTestRule.onNodeWithTag("music_tab_PLAYLISTS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("playlist_item_Most Played")
      .assertIsDisplayed()
      .assertIsSelected()

    // 6. Expand the player and open the Queue -> pre-focused on the currently playing song at expectedQueueIndex
    composeTestRule.onNodeWithTag("mini_player_album_art").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_queue_item_$expectedQueueIndex")
      .assertIsDisplayed()
      .assertIsSelected()

    // 7. Skipping the track increments its tracked skip count
    composeTestRule.runOnIdle {
      session!!.skipToNext(userInitiated = true)
    }
    composeTestRule.waitForIdle()
    assertEquals(1, session!!.playStats[targetTrack.id]?.skipCount)

    // 8. Cycling shuffle button in expanded player cycles OFF -> SHUFFLE -> SMART_SHUFFLE -> OFF
    // and actually shuffles/restores the currently playing queue with the active song at index 0
    val activeBeforeShuffle = session!!.currentTrack!!
    val originalQueueIds = session!!.playbackQueue.map { it.id }

    // Collapse queue sheet so shuffle button is visible and clickable
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").performClick()
    composeTestRule.waitForIdle()

    // Tap 1: Shuffle
    composeTestRule.onNodeWithTag("expanded_player_shuffle_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicShuffleMode.SHUFFLE, session!!.shuffleMode)
    assertEquals(0, session!!.currentQueueIndex)
    assertEquals(activeBeforeShuffle.id, session!!.playbackQueue.first().id)

    // Tap 2: Smart Shuffle
    composeTestRule.onNodeWithTag("expanded_player_shuffle_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicShuffleMode.SMART_SHUFFLE, session!!.shuffleMode)
    assertEquals(0, session!!.currentQueueIndex)
    assertEquals(activeBeforeShuffle.id, session!!.playbackQueue.first().id)

    // Tap 3: Shuffle Off (restores unshuffled queue and active track's position)
    composeTestRule.onNodeWithTag("expanded_player_shuffle_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicShuffleMode.OFF, session.shuffleMode)
    assertEquals(originalQueueIds, session.playbackQueue.map { it.id })
    assertEquals(activeBeforeShuffle.id, session.currentTrack?.id)

    // 9. Sleep pill toggles directly through Off -> 10m -> 15m -> 20m -> 30m -> 60m -> Off on click
    assertEquals(SleepTimerPreset.OFF, session.sleepTimerPreset)
    val expectedSleepPresets = listOf(
      SleepTimerPreset.TEN_MIN,
      SleepTimerPreset.FIFTEEN_MIN,
      SleepTimerPreset.TWENTY_MIN,
      SleepTimerPreset.THIRTY_MIN,
      SleepTimerPreset.SIXTY_MIN,
      SleepTimerPreset.OFF,
    )
    for (preset in expectedSleepPresets) {
      composeTestRule.onNodeWithTag("expanded_player_sleep_button").performClick()
      composeTestRule.waitForIdle()
      assertEquals(preset, session.sleepTimerPreset)
    }
  }

  @Test
  fun musicPlayer_supportsFavoritingArtistsAlbumsAndGenres_withoutAddingToDefaultFavoritesPlaylist() {
    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS,
        )
      }
    }
    composeTestRule.waitForIdle()

    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)

    // 1. Favorite an artist ("Aurora Synth") and filter Artists by Favorites
    composeTestRule.onNodeWithTag("music_tab_ARTISTS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("favorite_artist_Aurora Synth").performClick()
    composeTestRule.waitForIdle()
    assertTrue(session!!.isArtistFavorite("Aurora Synth"))

    composeTestRule.onNodeWithTag("music_favorites_filter_chip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Aurora Synth").assertIsDisplayed()
    composeTestRule.onNodeWithText("Velvet Trio").assertDoesNotExist()

    // 2. Favorite an album ("Blue Note Sessions") and filter Albums by Favorites
    composeTestRule.onNodeWithTag("music_tab_ALBUMS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("favorite_album_Blue Note Sessions").performClick()
    composeTestRule.waitForIdle()
    assertTrue(session.isAlbumFavorite("Blue Note Sessions", "Velvet Trio"))

    composeTestRule.onNodeWithTag("music_favorites_filter_chip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Blue Note Sessions").assertIsDisplayed()
    composeTestRule.onNodeWithText("Midnight Odyssey").assertDoesNotExist()

    // 3. Favorite a genre ("Synthwave") and filter Genres by Favorites
    composeTestRule.onNodeWithTag("music_tab_GENRES").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("favorite_genre_Synthwave").performClick()
    composeTestRule.waitForIdle()
    assertTrue(session.isGenreFavorite("Synthwave"))

    composeTestRule.onNodeWithTag("music_favorites_filter_chip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Synthwave").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jazz").assertDoesNotExist()

    // 4. Verify the default Favorites playlist in Playlists tab remains empty (0 songs)
    composeTestRule.onNodeWithTag("music_tab_PLAYLISTS").performClick()
    composeTestRule.waitForIdle()
    assertTrue(session.favoriteIds.isEmpty())
    composeTestRule.onNodeWithTag("playlist_item_Favorites").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("No favorites yet — tap the star on any song to add it here").assertIsDisplayed()
  }

  @Test
  fun musicPlayer_contextMenuSupportsRenameAndBulkDeleteWithTypedConfirmation_andProtectsDefaultAndHomePlaylists() {
    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS,
        )
      }
    }
    composeTestRule.waitForIdle()

    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)

    // 1. Home playlists do not have Delete in their context menu
    val firstHomeMix = session!!.homePlaylists.first()
    composeTestRule.onNodeWithTag("playlist_more_${firstHomeMix.name}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("playlist_menu_${firstHomeMix.name}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("playlist_menu_delete_${firstHomeMix.name}").assertDoesNotExist()
    composeTestRule.onNodeWithTag("playlist_more_${firstHomeMix.name}").performClick()
    composeTestRule.waitForIdle()

    // 2. Default playlists (Favorites, Most Played, Recently Played) do not have Delete
    composeTestRule.onNodeWithTag("music_tab_PLAYLISTS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("delete_playlist_Favorites").assertDoesNotExist()
    composeTestRule.onNodeWithTag("playlist_more_Favorites").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("playlist_menu_delete_Favorites").assertDoesNotExist()
    composeTestRule.onNodeWithTag("playlist_more_Favorites").performClick()
    composeTestRule.waitForIdle()

    // 3. Rename an Artist from the Artist context menu ("Aurora Synth" -> "Aurora Prime")
    composeTestRule.onNodeWithTag("music_tab_ARTISTS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("artist_more_Aurora Synth").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("artist_menu_rename_Aurora Synth").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("rename_music_item_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rename_music_item_input").performTextInput(" Prime")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("confirm_rename_music_item_button").performClick()
    composeTestRule.waitForIdle()
    assertTrue(session.allTracks.any { it.artist == "Aurora Synth Prime" })

    // 4. Bulk delete an Artist requires typing the artist name in the confirmation modal
    composeTestRule.onNodeWithTag("artist_more_Aurora Synth Prime").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("artist_menu_delete_Aurora Synth Prime").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("bulk_delete_music_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirm_bulk_delete_button").assertIsNotEnabled()

    // Typing wrong name keeps confirm button disabled
    composeTestRule.onNodeWithTag("bulk_delete_name_input").performTextInput("Wrong Name")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("confirm_bulk_delete_button").assertIsNotEnabled()
  }

  @Test
  fun musicPlayer_longPressEntersMultiSelectMode_andSupportsSelectAllPlayAndDelete() {
    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS,
          initialTab = MusicLibraryTab.SONGS,
        )
      }
    }
    composeTestRule.waitForIdle()

    val firstSong = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS[0]
    val secondSong = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS[1]

    // 1. Long-press on first song enters multi-select mode and shows ToolboxTopBarSelectionState
    composeTestRule.onNodeWithTag("song_item_${firstSong.id}")
      .performTouchInput { longClick() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("action_close_selection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("action_toggle_select_all").assertIsDisplayed()
    composeTestRule.onNodeWithText("1 selected").assertIsDisplayed()
    composeTestRule.onNodeWithTag("select_checkbox_song:${firstSong.id}").assertIsDisplayed()

    // 2. Tapping a second song while in selection mode selects it ("2 selected")
    composeTestRule.onNodeWithTag("song_item_${secondSong.id}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("2 selected").assertIsDisplayed()

    // 3. Tapping bulk Play plays the selected tracks and exits multi-select mode
    composeTestRule.onNodeWithTag("action_bulk_play").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("action_close_selection").assertDoesNotExist()
    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)
    assertTrue(session!!.isPlaying)
    assertEquals(2, session.playbackQueue.size)
  }

  @Test
  fun musicPlayer_controlsExternalAndroidSystemMediaPlayer_andHidesLocalOnlyControls() {
    MusicPlayerEngine.updateExternalSystemMediaFromMetadata(
      title = "Veridis Quo",
      artist = "Daft Punk",
      album = "Discovery",
      genre = "French House",
      sourcePackage = "com.spotify.music",
      sourceAppName = "Spotify",
      isPlaying = true,
      notificationColorRgb = 0x1DB954,
      durationMs = 345_000L,
      positionMs = 60_000L,
    )

    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS,
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Mini-player displays external track metadata and hides local favorite button
    composeTestRule.onNodeWithTag("music_player_collapsed").assertIsDisplayed()
    composeTestRule.onNodeWithText("Veridis Quo").assertIsDisplayed()
    composeTestRule.onNodeWithText("Daft Punk • Discovery • Spotify").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mini_player_favorite_button").assertDoesNotExist()

    // 2. Mini-player Play/Pause button toggles external media playback without hiding the player
    composeTestRule.onNodeWithTag("mini_player_play_pause_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals("PAUSE", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)
    assertFalse(MusicPlayerEngine.externalSystemMediaInfo?.isPlaying == true)
    composeTestRule.onNodeWithTag("music_player_collapsed").assertIsDisplayed()

    // 3. Expand full player: shows external media metadata, seek slider, and transport controls,
    // while hiding local-only favorite star, file info, shuffle, repeat, speed, sleep, and queue sheet
    composeTestRule.onNodeWithTag("music_player_collapsed").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("music_player_expanded").assertIsDisplayed()
    composeTestRule.onNodeWithText("PLAYING FROM SPOTIFY").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_external_source_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_seek_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_prev_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_play_pause_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expanded_player_next_button").assertIsDisplayed()

    composeTestRule.onNodeWithTag("expanded_player_favorite_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("expanded_player_info_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("expanded_player_shuffle_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("expanded_player_repeat_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("expanded_player_speed_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("expanded_player_sleep_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("music_queue_panel_toggle").assertDoesNotExist()

    // Verify space is reserved for the speed / sleep controls row and seek bar row
    val visibleSeekRowBounds = composeTestRule.onNodeWithTag("expanded_player_seek_row").fetchSemanticsNode().boundsInRoot
    val speedSleepRowBounds = composeTestRule.onNodeWithTag("expanded_player_speed_sleep_row").fetchSemanticsNode().boundsInRoot
    val playPauseBoundsWithSeek = composeTestRule.onNodeWithTag("expanded_player_play_pause_button").fetchSemanticsNode().boundsInRoot
    assertTrue(visibleSeekRowBounds.height > 0f)
    assertTrue(speedSleepRowBounds.height > 0f)

    // When external media has unknown duration (0ms) and exposes custom actions, seek slider is hidden
    // with its row space reserved, and custom actions appear in the Shuffle (left) and Repeat (right) slots
    composeTestRule.runOnIdle {
      MusicPlayerEngine.updateExternalSystemMediaFromMetadata(
        title = "Veridis Quo",
        artist = "Daft Punk",
        album = "Discovery",
        genre = "French House",
        sourcePackage = "com.spotify.music",
        sourceAppName = "Spotify",
        isPlaying = false,
        notificationColorRgb = 0x1DB954,
        durationMs = 0L,
        positionMs = 0L,
        customActions = listOf(
          SystemMediaCustomAction(action = "spotify_shuffle", label = "Shuffle On"),
          SystemMediaCustomAction(action = "spotify_like", label = "Add to Liked Songs"),
        ),
      )
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("expanded_player_seek_slider").assertDoesNotExist()
    val reservedSeekRowBounds = composeTestRule.onNodeWithTag("expanded_player_seek_row").fetchSemanticsNode().boundsInRoot
    val playPauseBoundsWithoutSeek = composeTestRule.onNodeWithTag("expanded_player_play_pause_button").fetchSemanticsNode().boundsInRoot
    assertEquals(visibleSeekRowBounds.height, reservedSeekRowBounds.height, 1f)
    assertEquals(playPauseBoundsWithSeek.top, playPauseBoundsWithoutSeek.top, 1f)

    composeTestRule.onNodeWithTag("expanded_player_external_action_left").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals("CUSTOM:spotify_shuffle", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)

    composeTestRule.onNodeWithTag("expanded_player_external_action_right").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals("CUSTOM:spotify_like", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)

    // 4. Transport buttons in expanded player control external Android system media session
    composeTestRule.onNodeWithTag("expanded_player_play_pause_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals("PLAY", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)
    assertTrue(MusicPlayerEngine.externalSystemMediaInfo?.isPlaying == true)

    composeTestRule.onNodeWithTag("expanded_player_next_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals("NEXT", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)

    composeTestRule.onNodeWithTag("expanded_player_prev_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals("PREVIOUS", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)

    // 5. Tapping the badge underneath the Artist information opens the external app currently playing media
    var interceptedOpenPkg: String? = null
    MusicPlayerEngine.openExternalMediaAppOverrideForTest = { pkg ->
      interceptedOpenPkg = pkg
      true
    }
    composeTestRule.onNodeWithTag("expanded_player_external_source_badge").performClick()
    composeTestRule.waitForIdle()
    assertEquals("com.spotify.music", MusicPlayerEngine.lastOpenedExternalMediaPackage)
    assertEquals("com.spotify.music", interceptedOpenPkg)

    // 6. Receiving a phone call from the Android system media player is ignored and never displays "Phone"
    composeTestRule.runOnIdle {
      MusicPlayerEngine.updateExternalSystemMediaSession(null)
      MusicPlayerEngine.updateExternalSystemMediaFromMetadata(
        title = "Phone",
        sourcePackage = "com.google.android.dialer",
        sourceAppName = "Phone",
        isPlaying = true,
      )
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_player_collapsed").assertDoesNotExist()
    composeTestRule.onNodeWithTag("music_player_expanded").assertDoesNotExist()
  }

  @Test
  fun musicPlayer_homeViewFloatingAppBar_addsPopularAndCustomMusicApps_andScrollsWhenOverflowing() {
    MusicPlayerEngine.installedStreamingAppsOverrideForTest =
      MusicPlayerEngine.POPULAR_STREAMING_MUSIC_APPS.filter { it.id in setOf("spotify", "youtube_music") }
    MusicPlayerEngine.customPickerInstalledAppsOverrideForTest = listOf(
      PreferredMusicAppLink(
        id = "custom_tv_plex_labs_plexamp",
        label = "Plexamp",
        packageName = "tv.plex.labs.plexamp",
        accentRgb = 0xE5A00D,
        isCustom = true,
        isInstalled = true,
      ),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS,
          initialTab = MusicLibraryTab.HOME,
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Floating rounded pill bar is visible at the bottom of the Home view with the plus button on the left
    composeTestRule.onNodeWithTag("music_home_floating_app_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_home_add_app_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_home_empty_apps_prompt").assertIsDisplayed()

    // 2. Tapping the plus button opens the preferred music app picker (installed streaming apps + Custom App at the end, no search bar)
    composeTestRule.onNodeWithTag("music_home_add_app_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_preferred_app_picker_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_app_picker_search_input").assertDoesNotExist()
    composeTestRule.onNodeWithTag("music_app_picker_option_spotify").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_app_picker_option_youtube_music").assertIsDisplayed()
    composeTestRule.onNodeWithText("Custom App").assertIsDisplayed()
    composeTestRule.onNodeWithText("Choose any installed app").assertIsDisplayed()

    // Add Spotify & YouTube Music
    composeTestRule.onNodeWithTag("music_app_picker_option_spotify").performClick()
    composeTestRule.onNodeWithTag("music_app_picker_option_youtube_music").performClick()
    composeTestRule.waitForIdle()

    // 3. Tap Custom App at the end of the picker list and select an installed app
    composeTestRule.onNodeWithTag("music_app_picker_list")
      .performScrollToNode(hasTestTag("music_app_picker_custom_option"))
    composeTestRule.onNodeWithTag("music_app_picker_custom_option").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("music_custom_installed_option_custom_tv_plex_labs_plexamp").performClick()
    composeTestRule.waitForIdle()

    // Close picker dialog
    composeTestRule.onNodeWithTag("music_app_picker_done_button").performClick()
    composeTestRule.waitForIdle()

    // 4. Added apps appear inside the rounded pill bar on the Home view
    composeTestRule.onNodeWithTag("music_home_apps_scroll_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_home_app_chip_spotify").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_home_app_chip_youtube_music").assertIsDisplayed()
    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)
    assertEquals(3, session!!.preferredMusicApps.size)
    assertTrue(session.preferredMusicApps.any { it.label == "Plexamp" && it.isCustom })
  }

  @Test
  fun musicPlayer_scopedStorageLocksPillsUntilGranted_supportsCustomFolders_andPerSectionSavedSort() {
    val customDir = java.io.File(composeTestRule.activity.cacheDir, "CustomBeats_${System.currentTimeMillis()}").apply {
      mkdirs()
    }
    val customFolder = MusicFolder(
      folderName = "CustomBeats",
      displayPath = "/CustomBeats",
      customAbsolutePath = customDir.absolutePath,
    )

    try {
      composeTestRule.setContent {
        ToolboxTheme {
          MusicPlayerScreen(
            initialTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS,
            initialTab = MusicLibraryTab.HOME,
            initialGrantedFolderPaths = emptySet(),
            onRequestFolderSystemAccess = { _, onResult ->
              onResult(true)
            },
            onRequestAddCustomFolder = { onFolderPicked ->
              onFolderPicked(customFolder)
            },
          )
        }
      }
      composeTestRule.waitForIdle()

      // 1. Before granting permission to /Music, navigation pills show lock icons and locked storage view is displayed
      composeTestRule.onNodeWithTag("locked_storage_state").assertIsDisplayed()
      composeTestRule.onNodeWithTag("music_tab_lock_icon_HOME", useUnmergedTree = true).assertExists()
      composeTestRule.onNodeWithTag("music_tab_lock_icon_SONGS", useUnmergedTree = true).assertExists()

      // Tapping a locked navigation pill opens the /Music folder permission request view
      composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("folder_permission_view").assertIsDisplayed()
      composeTestRule.onNodeWithText("Allow access to files in /Music?").assertIsDisplayed()

      // Tapping "Allow" grants scoped access to /Music and unlocks the navigation pills
      composeTestRule.onNodeWithTag("allow_folder_permission_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("locked_storage_state").assertDoesNotExist()
      composeTestRule.onNodeWithTag("folder_permission_view").assertDoesNotExist()
      composeTestRule.onNodeWithTag("music_tab_unlocked_icon_HOME", useUnmergedTree = true).assertExists()

      // 2. On the Home view, default sort is Random and "Save for home" is pre-checked
      val session = MusicPlayerEngine.getActivePlaybackSession()
      assertNotNull(session)
      assertEquals(MusicLibraryTab.HOME, session!!.selectedTab)
      assertEquals(MusicSortOrder.RANDOM, session.sortOrder)
      assertTrue(session.isSortSavedForCurrentScope)

      composeTestRule.onNodeWithTag("music_sort_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("music_sort_menu").assertIsDisplayed()
      composeTestRule.onNodeWithTag("music_save_sort_for_section").assertIsDisplayed()
      composeTestRule.onNodeWithText("Save for home").assertIsDisplayed()
      composeTestRule.onNodeWithTag("music_save_sort_for_section_checkbox").assertIsOn()

      // Dismiss sort menu and switch to Songs tab: label becomes "Save for songs"
      composeTestRule.onNodeWithTag("music_sort_option_random").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("music_sort_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Save for songs").assertIsDisplayed()
      composeTestRule.onNodeWithTag("music_save_sort_for_section").performClick()
      composeTestRule.waitForIdle()
      assertTrue(session.isSortSavedForCurrentScope)
      composeTestRule.onNodeWithTag("music_sort_option_duration_desc").performClick()
      composeTestRule.waitForIdle()
      assertEquals(MusicSortOrder.DURATION_DESC, session.sortOrder)

      // 3. Tapping the Add Folder button in the navigation bar adds a custom folder pill with an 'x' removal button
      composeTestRule.onNodeWithTag("music_add_folder_button").assertIsDisplayed()
      composeTestRule.onNodeWithTag("music_add_folder_button").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("music_folder_pill_CustomBeats").assertIsDisplayed()
      composeTestRule.onNodeWithTag("folder_delete_icon_CustomBeats", useUnmergedTree = true).assertExists()

      // Sort menu inside custom folder shows "Save for folder"
      composeTestRule.onNodeWithTag("music_sort_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Save for folder").assertIsDisplayed()
      composeTestRule.onNodeWithTag("music_sort_option_title_asc").performClick()
      composeTestRule.waitForIdle()

      // Tapping the 'x' button on the right of the custom folder pill removes the custom folder
      composeTestRule.onNodeWithTag("folder_delete_icon_CustomBeats", useUnmergedTree = true).performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("music_folder_pill_CustomBeats").assertDoesNotExist()
      assertTrue(session.customFolders.isEmpty())
    } finally {
      customDir.deleteRecursively()
    }
  }

  @Test
  fun musicPlayer_groupAndSortFilter_groupsSongsByAlbumArtistGenre_andAlbumsByArtistGenre() {
    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS,
          initialTab = MusicLibraryTab.HOME,
        )
      }
    }
    composeTestRule.waitForIdle()

    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)

    // 1. On Home tab (non-grouping section), the menu is a Sort-only menu (no GROUP BY header)
    composeTestRule.onNodeWithTag("music_sort_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_sort_menu").assertIsDisplayed()
    composeTestRule.onNodeWithText("GROUP BY").assertDoesNotExist()
    composeTestRule.onNodeWithTag("music_sort_option_random").performClick()
    composeTestRule.waitForIdle()

    // 2. Switch to Songs view: sort filter changes into Group & Sort filter with Album, Artist, Genre, Year, None
    composeTestRule.onNodeWithTag("music_tab_SONGS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_sort_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("GROUP BY").assertIsDisplayed()
    composeTestRule.onNodeWithText("SORT BY").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_group_option_album").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_group_option_artist").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_group_option_genre").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_group_option_year").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_group_option_none").assertIsDisplayed()

    // Select grouping by Album in Songs view
    composeTestRule.onNodeWithTag("music_group_option_album").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicGroupOption.ALBUM, session!!.groupOption)
    composeTestRule.onNodeWithTag("music_group_header_blue_note_sessions").assertIsDisplayed()

    // Select grouping by Artist in Songs view
    composeTestRule.onNodeWithTag("music_sort_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_group_option_artist").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicGroupOption.ARTIST, session.groupOption)
    composeTestRule.onNodeWithTag("music_group_header_aurora_synth").assertIsDisplayed()

    // Select grouping by Genre in Songs view
    composeTestRule.onNodeWithTag("music_sort_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_group_option_genre").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicGroupOption.GENRE, session.groupOption)
    composeTestRule.onNodeWithTag("music_group_header_jazz").assertIsDisplayed()

    // Select grouping by Year in Songs view
    composeTestRule.onNodeWithTag("music_sort_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_group_option_year").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicGroupOption.YEAR, session.groupOption)
    composeTestRule.onNodeWithTag("music_group_header_2026").assertIsDisplayed()

    // 3. Switch to Albums view: Group & Sort filter allows grouping by Artist, Genre, or Year (not Album)
    composeTestRule.onNodeWithTag("music_tab_ALBUMS").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_sort_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("GROUP BY").assertIsDisplayed()
    composeTestRule.onNodeWithText("SORT BY").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_group_option_album").assertDoesNotExist()
    composeTestRule.onNodeWithTag("music_group_option_artist").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_group_option_genre").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_group_option_year").assertIsDisplayed()
    composeTestRule.onNodeWithTag("music_group_option_none").assertIsDisplayed()

    // Select grouping by Artist in Albums view
    composeTestRule.onNodeWithTag("music_group_option_artist").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicGroupOption.ARTIST, session.groupOption)
    composeTestRule.onNodeWithTag("music_group_header_aurora_synth").assertIsDisplayed()

    // Select grouping by Genre in Albums view
    composeTestRule.onNodeWithTag("music_sort_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_group_option_genre").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicGroupOption.GENRE, session.groupOption)
    composeTestRule.onNodeWithTag("music_group_header_jazz").assertIsDisplayed()

    // Select grouping by Year in Albums view
    composeTestRule.onNodeWithTag("music_sort_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("music_group_option_year").performClick()
    composeTestRule.waitForIdle()
    assertEquals(MusicGroupOption.YEAR, session.groupOption)
    composeTestRule.onNodeWithTag("music_group_header_2026").assertIsDisplayed()
  }

  @Test
  fun musicPlayer_twoAppWidgets_miniBarAndAlbumArtWithFiveControlsAndBottomRightStar() {
    val context = composeTestRule.activity
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS

    composeTestRule.setContent {
      ToolboxTheme {
        MusicPlayerScreen(
          initialTracks = tracks,
          initialTab = MusicLibraryTab.SONGS,
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Unified responsive Music Player AppWidgetProvider is registered in Android's AppWidgetManager
    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
    val installedProviders = appWidgetManager.installedProviders
      .filter { it.provider.packageName == context.packageName }
    val providerClasses = installedProviders.map { it.provider.className }.toSet()
    assertTrue(
      "Expected MusicAlbumArtWidgetProvider to be registered in AppWidgetManager",
      MusicAlbumArtWidgetProvider::class.java.name in providerClasses,
    )
    assertFalse(
      "Expected separate MusicMiniBarWidgetProvider not to be registered in AppWidgetManager",
      MusicMiniBarWidgetProvider::class.java.name in providerClasses,
    )
    assertFalse(
      "Expected separate MusicMediumWidgetProvider not to be registered in AppWidgetManager",
      MusicMediumWidgetProvider::class.java.name in providerClasses,
    )

    // 2. Start playing the first track and verify responsive RemoteViews switches between Mini Bar,
    //    Medium, and Album Art views based on widget dimensions
    assertTrue(MusicPlayerWidgets.handleWidgetAction(context, MusicPlayerEngine.ACTION_MEDIA_PLAY))
    composeTestRule.waitForIdle()

    val session = MusicPlayerEngine.getActivePlaybackSession()
    assertNotNull(session)
    session!!.seekToPositionMs(tracks[0].durationMs / 2L)

    composeTestRule.runOnIdle {
      val state = MusicPlayerWidgets.resolveWidgetState(context, session)
      assertTrue(state.isPlaying)
      assertEquals(tracks[0].title, state.title)

      val miniOptions = android.os.Bundle().apply {
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 280)
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 280)
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 72)
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 72)
      }
      val mediumOptions = android.os.Bundle().apply {
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 280)
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 280)
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 112)
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 112)
      }
      val albumOptions = android.os.Bundle().apply {
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 260)
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 260)
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 260)
        putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 260)
      }

      assertEquals(
        MusicWidgetViewStyle.MINI_BAR,
        MusicPlayerWidgets.resolveWidgetViewStyle(context, miniOptions),
      )
      assertEquals(
        MusicWidgetViewStyle.MEDIUM,
        MusicPlayerWidgets.resolveWidgetViewStyle(context, mediumOptions),
      )
      assertEquals(
        MusicWidgetViewStyle.ALBUM_ART,
        MusicPlayerWidgets.resolveWidgetViewStyle(context, albumOptions),
      )

      // View 1 (Mini Bar size): Single bar widget, with no seek bar, full-height left-aligned album art, and larger playback controls
      val miniHost = android.widget.FrameLayout(context)
      val miniRemoteViews = MusicPlayerWidgets.buildResponsiveMusicWidgetRemoteViews(context, state, miniOptions)
      val miniRoot = miniRemoteViews.apply(context, miniHost)

      val miniRow = miniRoot.findViewById<android.widget.LinearLayout>(com.example.toolbox.R.id.widget_mini_row)
      val miniArt = miniRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_mini_album_art)
      val miniTitle = miniRoot.findViewById<android.widget.TextView>(com.example.toolbox.R.id.widget_mini_title)
      val miniSubtitle = miniRoot.findViewById<android.widget.TextView>(com.example.toolbox.R.id.widget_mini_subtitle)
      val miniFav = miniRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_mini_favorite_button)
      val miniPrev = miniRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_mini_prev_button)
      val miniPlayPause = miniRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_mini_play_pause_button)
      val miniNext = miniRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_mini_next_button)

      // Verify no ProgressBar exists inside the mini widget
      assertEquals(0, (0 until (miniRoot as android.view.ViewGroup).childCount).count {
        miniRoot.getChildAt(it) is android.widget.ProgressBar
      })
      assertNotNull(miniRow)
      assertEquals(0, miniRow.paddingStart)
      assertEquals(0, miniRow.paddingTop)
      assertEquals(0, miniRow.paddingBottom)
      assertNotNull(miniArt)
      assertEquals(android.view.ViewGroup.LayoutParams.MATCH_PARENT, miniArt.layoutParams.height)
      assertTrue(miniArt.adjustViewBounds)
      assertEquals(tracks[0].title, miniTitle.text.toString())
      assertEquals("${tracks[0].artist} • ${tracks[0].album}", miniSubtitle.text.toString())
      assertEquals(android.view.View.VISIBLE, miniFav.visibility)
      assertEquals("Favorite", miniFav.contentDescription.toString())
      assertEquals("Previous track", miniPrev.contentDescription.toString())
      assertEquals("Pause", miniPlayPause.contentDescription.toString())
      assertEquals("Next track", miniNext.contentDescription.toString())

      // View 2 (Medium size): Medium player view, similar to mini bar (full-height left-aligned album art),
      // with song info & favorite star on top row and the full 5 playback controls underneath song info
      val mediumHost = android.widget.FrameLayout(context)
      val mediumRemoteViews = MusicPlayerWidgets.buildResponsiveMusicWidgetRemoteViews(context, state, mediumOptions)
      val mediumRoot = mediumRemoteViews.apply(context, mediumHost)

      val mediumRow = mediumRoot.findViewById<android.widget.LinearLayout>(com.example.toolbox.R.id.widget_medium_row)
      val mediumArt = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_album_art)
      val mediumRightColumn = mediumRoot.findViewById<android.widget.LinearLayout>(com.example.toolbox.R.id.widget_medium_right_column)
      val mediumInfoRow = mediumRoot.findViewById<android.widget.LinearLayout>(com.example.toolbox.R.id.widget_medium_info_row)
      val mediumTitle = mediumRoot.findViewById<android.widget.TextView>(com.example.toolbox.R.id.widget_medium_title)
      val mediumSubtitle = mediumRoot.findViewById<android.widget.TextView>(com.example.toolbox.R.id.widget_medium_subtitle)
      val mediumFav = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_favorite_button)
      val mediumControlsRow = mediumRoot.findViewById<android.widget.LinearLayout>(com.example.toolbox.R.id.widget_medium_controls_row)
      val mediumShuffle = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_shuffle_button)
      val mediumPrev = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_prev_button)
      val mediumPlayPause = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_play_pause_button)
      val mediumNext = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_next_button)
      val mediumRepeat = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_repeat_button)

      assertNotNull(mediumRow)
      assertEquals(0, mediumRow.paddingStart)
      assertNotNull(mediumArt)
      assertEquals(android.view.ViewGroup.LayoutParams.MATCH_PARENT, mediumArt.layoutParams.height)
      assertTrue(mediumArt.adjustViewBounds)
      assertNotNull(mediumRightColumn)
      assertEquals(android.widget.LinearLayout.VERTICAL, mediumRightColumn.orientation)
      // Verify song info row is on top and playback controls row is underneath it inside mediumRightColumn
      assertEquals(mediumInfoRow, mediumRightColumn.getChildAt(0))
      assertEquals(mediumControlsRow, mediumRightColumn.getChildAt(1))
      assertEquals(tracks[0].title, mediumTitle.text.toString())
      assertEquals("${tracks[0].artist} • ${tracks[0].album}", mediumSubtitle.text.toString())
      assertEquals(android.view.View.VISIBLE, mediumFav.visibility)
      assertEquals("Favorite", mediumFav.contentDescription.toString())
      assertEquals(5, mediumControlsRow.childCount)
      assertEquals("Shuffle Off", mediumShuffle.contentDescription.toString())
      assertEquals("Previous track", mediumPrev.contentDescription.toString())
      assertEquals("Pause", mediumPlayPause.contentDescription.toString())
      assertEquals("Next track", mediumNext.contentDescription.toString())
      assertEquals("Repeat Off", mediumRepeat.contentDescription.toString())

      // View 3 (Album Art size): Album art with favorite star overlaid at bottom-right of album art,
      // track title & artist, and the full 5 playback controls below (no progress bar)
      val albumHost = android.widget.FrameLayout(context)
      val albumRemoteViews = MusicPlayerWidgets.buildResponsiveMusicWidgetRemoteViews(context, state, albumOptions)
      val albumRoot = albumRemoteViews.apply(context, albumHost)

      val albumArtContainer = albumRoot.findViewById<android.widget.FrameLayout>(com.example.toolbox.R.id.widget_album_art_container)
      val albumArt = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_art)
      val albumFavContainer = albumRoot.findViewById<android.widget.FrameLayout>(com.example.toolbox.R.id.widget_album_favorite_container)
      val albumFavButton = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_favorite_button)
      val albumTitle = albumRoot.findViewById<android.widget.TextView>(com.example.toolbox.R.id.widget_album_title)
      val albumSubtitle = albumRoot.findViewById<android.widget.TextView>(com.example.toolbox.R.id.widget_album_subtitle)
      val albumControlsRow = albumRoot.findViewById<android.widget.LinearLayout>(com.example.toolbox.R.id.widget_album_controls_row)
      val albumShuffle = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_shuffle_button)
      val albumPrev = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_prev_button)
      val albumPlayPause = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_play_pause_button)
      val albumNext = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_next_button)
      val albumRepeat = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_repeat_button)

      assertNotNull(albumArtContainer)
      assertNotNull(albumArt)
      assertNotNull(albumFavContainer)
      // Verify favorite star container is a direct child of albumArtContainer overlaid at bottom|end
      assertEquals(albumArtContainer, albumFavContainer.parent)
      val favLp = albumFavContainer.layoutParams as android.widget.FrameLayout.LayoutParams
      assertEquals(
        android.view.Gravity.BOTTOM or android.view.Gravity.END,
        favLp.gravity and (android.view.Gravity.VERTICAL_GRAVITY_MASK or android.view.Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK),
      )
      assertEquals("Favorite", albumFavButton.contentDescription.toString())

      // Verify title, artist/album, and full 5 playback controls
      assertEquals(tracks[0].title, albumTitle.text.toString())
      assertEquals("${tracks[0].artist} • ${tracks[0].album}", albumSubtitle.text.toString())
      assertEquals(5, albumControlsRow.childCount)
      assertEquals("Shuffle Off", albumShuffle.contentDescription.toString())
      assertEquals("Previous track", albumPrev.contentDescription.toString())
      assertEquals("Pause", albumPlayPause.contentDescription.toString())
      assertEquals("Next track", albumNext.contentDescription.toString())
      assertEquals("Repeat Off", albumRepeat.contentDescription.toString())
    }

    // 3. Exercise widget actions (Favorite, Shuffle, Repeat, Next, Play/Pause) and verify updated RemoteViews
    assertTrue(MusicPlayerWidgets.handleWidgetAction(context, MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE))
    assertTrue(MusicPlayerWidgets.handleWidgetAction(context, MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE))
    assertTrue(MusicPlayerWidgets.handleWidgetAction(context, MusicPlayerEngine.ACTION_MEDIA_CYCLE_REPEAT))
    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle {
      val updatedState = MusicPlayerWidgets.resolveWidgetState(context, session)
      assertTrue(updatedState.isFavorite)
      assertEquals(MusicShuffleMode.SHUFFLE, updatedState.shuffleMode)
      assertEquals(MusicRepeatMode.REPEAT_ALL, updatedState.repeatMode)

      val albumHost = android.widget.FrameLayout(context)
      val albumRoot = MusicPlayerWidgets.buildAlbumArtRemoteViews(context, updatedState).apply(context, albumHost)
      val albumFavButton = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_favorite_button)
      val albumShuffleBg = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_shuffle_bg)
      val albumShuffle = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_shuffle_button)
      val albumRepeatBg = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_repeat_bg)
      val albumRepeat = albumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_album_repeat_button)

      assertEquals("Unfavorite", albumFavButton.contentDescription.toString())
      assertEquals(android.view.View.VISIBLE, albumShuffleBg.visibility)
      assertEquals("Shuffle", albumShuffle.contentDescription.toString())
      assertEquals(android.view.View.VISIBLE, albumRepeatBg.visibility)
      assertEquals("Repeat All", albumRepeat.contentDescription.toString())

      val mediumHost = android.widget.FrameLayout(context)
      val mediumRoot = MusicPlayerWidgets.buildMediumRemoteViews(context, updatedState).apply(context, mediumHost)
      val mediumFavButton = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_favorite_button)
      val mediumShuffleBg = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_shuffle_bg)
      val mediumShuffle = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_shuffle_button)
      val mediumRepeatBg = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_repeat_bg)
      val mediumRepeat = mediumRoot.findViewById<android.widget.ImageView>(com.example.toolbox.R.id.widget_medium_repeat_button)

      assertEquals("Unfavorite", mediumFavButton.contentDescription.toString())
      assertEquals(android.view.View.VISIBLE, mediumShuffleBg.visibility)
      assertEquals("Shuffle", mediumShuffle.contentDescription.toString())
      assertEquals(android.view.View.VISIBLE, mediumRepeatBg.visibility)
      assertEquals("Repeat All", mediumRepeat.contentDescription.toString())
    }
  }
}
