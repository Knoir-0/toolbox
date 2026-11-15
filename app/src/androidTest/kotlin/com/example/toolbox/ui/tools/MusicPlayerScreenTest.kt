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
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
    composeTestRule.onNodeWithText("Full Plays").assertIsDisplayed()
    composeTestRule.onNodeWithText("Skips").assertIsDisplayed()
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
        calculateAccentHarmonyColor(firstPalette.dominantColor, AccentHarmonyOption.SHADE_MID),
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
    composeTestRule.onNodeWithTag("song_list_view")
      .performScrollToNode(hasTestTag("song_item_sample_autumn_coffee"))
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
    composeTestRule.onNodeWithTag("music_view_mode_card").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("song_item_sample_starlight_drive")
      .assertIsDisplayed()
      .assertIsSelected()

    // Switch back to List view
    composeTestRule.onNodeWithTag("music_view_mode_list").performClick()
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
}
