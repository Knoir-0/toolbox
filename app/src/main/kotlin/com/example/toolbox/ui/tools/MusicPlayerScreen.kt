package com.example.toolbox.ui.tools

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
  modifier: Modifier = Modifier,
  initialDirectory: File? = null,
  initialTracks: List<MusicTrack>? = null,
  initialTab: MusicLibraryTab? = null,
  initialViewMode: MusicViewMode? = null,
  initialPlayerExpanded: Boolean = false,
  initialQueueExpanded: Boolean = false,
  onOpenColorPicker: ((String) -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val activity = remember(context) { context.findActivity() }

  val session = remember(
    context,
    activity,
    initialDirectory,
    initialTracks,
    initialTab,
    initialViewMode,
    initialPlayerExpanded,
    initialQueueExpanded,
    isInspection,
  ) {
    MusicPlayerEngine.getOrCreatePlaybackSession(
      context = context,
      activity = activity,
      initialDirectory = initialDirectory,
      initialTracks = initialTracks,
      initialTab = initialTab,
      initialViewMode = initialViewMode,
      initialPlayerExpanded = initialPlayerExpanded,
      initialQueueExpanded = initialQueueExpanded,
      isInspection = isInspection,
    )
  }

  // Library Tab: Artists, Albums, Songs, Genres, Playlists
  val selectedTab = session.selectedTab

  fun updateTab(tab: MusicLibraryTab) {
    session.updateTab(tab)
  }

  // View Mode: List vs Card
  val viewMode = session.viewMode

  fun updateViewMode(mode: MusicViewMode) {
    session.updateViewMode(mode)
  }

  // Sort Order
  val sortOrder = session.sortOrder

  fun updateSortOrder(order: MusicSortOrder) {
    session.updateSortOrder(order)
  }

  // Shuffle & Repeat
  val isShuffleEnabled = session.isShuffleEnabled

  fun toggleShuffle() {
    session.toggleShuffle()
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  val repeatMode = session.repeatMode

  fun cycleRepeatMode() {
    session.cycleRepeatMode()
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  // Favorites
  val favoriteIds = session.favoriteIds

  fun toggleFavorite(trackId: String) {
    session.toggleFavorite(trackId)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  var showFavoritesOnly by session::showFavoritesOnly

  // Play Statistics (persisted in SharedPreferences: play count + total playtime per track)
  val playStats = session.playStats

  // Playlists (persisted in SharedPreferences; Favorites & Most Played are built-in dynamic playlists)
  val savedPlaylistEntries = session.savedPlaylistEntries

  fun saveQueueAsPlaylist(name: String, tracksToSave: List<MusicTrack>) {
    session.saveQueueAsPlaylist(name, tracksToSave)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun deletePlaylist(playlistId: String) {
    if (playlistId in MusicPlayerEngine.BUILT_IN_PLAYLIST_IDS) return
    session.deletePlaylist(playlistId)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  // Search state
  var isSearchActive by session::isSearchActive
  var searchQuery by session::searchQuery

  // Drill-down selection state (Artist detail, Album detail, Genre detail, Playlist detail)
  var selectedArtistName by session::selectedArtistName
  var selectedAlbumKey by session::selectedAlbumKey
  var selectedGenreName by session::selectedGenreName
  var selectedPlaylistId by session::selectedPlaylistId

  fun clearDrillDown() {
    session.clearDrillDown()
  }

  val allTracks = session.allTracks

  val sortedTracks = remember(allTracks, sortOrder, playStats, showFavoritesOnly, favoriteIds, searchQuery, selectedTab, selectedArtistName, selectedAlbumKey, selectedGenreName, selectedPlaylistId) {
    val base = if (showFavoritesOnly) {
      allTracks.filter { it.id in favoriteIds }
    } else {
      allTracks
    }
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.SONGS) searchQuery else ""
    val filtered = MusicPlayerEngine.filterTracks(base, activeQuery)
    MusicPlayerEngine.sortTracks(filtered, sortOrder, playStats)
  }

  val baseArtists = remember(allTracks) {
    MusicPlayerEngine.buildArtists(allTracks)
  }
  val allArtists = remember(baseArtists, searchQuery, selectedTab, selectedArtistName, selectedAlbumKey, selectedGenreName, selectedPlaylistId) {
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.ARTISTS) searchQuery else ""
    MusicPlayerEngine.filterArtists(baseArtists, activeQuery)
  }

  val baseAlbums = remember(allTracks) {
    MusicPlayerEngine.buildAlbums(allTracks)
  }
  val allAlbums = remember(baseAlbums, searchQuery, selectedTab, selectedArtistName, selectedAlbumKey, selectedGenreName, selectedPlaylistId) {
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.ALBUMS) searchQuery else ""
    MusicPlayerEngine.filterAlbums(baseAlbums, activeQuery)
  }

  val baseGenres = remember(allTracks) {
    MusicPlayerEngine.buildGenres(allTracks)
  }
  val allGenres = remember(baseGenres, searchQuery, selectedTab, selectedArtistName, selectedAlbumKey, selectedGenreName, selectedPlaylistId) {
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.GENRES) searchQuery else ""
    MusicPlayerEngine.filterGenres(baseGenres, activeQuery)
  }

  val basePlaylists = remember(savedPlaylistEntries, allTracks, favoriteIds, playStats) {
    listOf(
      MusicPlayerEngine.buildFavoritesPlaylist(favoriteIds, allTracks),
      MusicPlayerEngine.buildMostPlayedPlaylist(playStats, allTracks),
    ) + MusicPlayerEngine.buildPlaylists(savedPlaylistEntries, allTracks)
  }
  val allPlaylists = remember(basePlaylists, searchQuery, selectedTab, selectedArtistName, selectedAlbumKey, selectedGenreName, selectedPlaylistId) {
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.PLAYLISTS) searchQuery else ""
    MusicPlayerEngine.filterPlaylists(basePlaylists, activeQuery)
  }

  // Playback Queue & Active Track State (managed by MusicPlaybackSession so playback continues across tool switches)
  val playbackQueue = session.playbackQueue
  val currentQueueIndex = session.currentQueueIndex
  val currentTrack: MusicTrack? = session.currentTrack

  val isPlaying = session.isPlaying
  var isPlayerExpanded by session::isPlayerExpanded
  val currentPositionMs = session.currentPositionMs
  val trackDurationMs = session.trackDurationMs
  val isDraggingSeek = session.isDraggingSeek
  val dragSeekFraction = session.dragSeekFraction

  // Speed & Sleep Timer & Modals (volume is left to the system media volume)
  val playbackSpeed = session.playbackSpeed
  val sleepTimerPreset = session.sleepTimerPreset
  val sleepRemainingMs = session.sleepRemainingMs
  var showQueueSheet by session::showQueueSheet
  var showSavePlaylistDialog by rememberSaveable { mutableStateOf(false) }
  var showTrackInfoDialog by rememberSaveable { mutableStateOf(false) }
  var inlineColorPickerHex by rememberSaveable { mutableStateOf<String?>(null) }
  var showSortMenu by remember { mutableStateOf(false) }
  var showSleepTimerMenu by remember { mutableStateOf(false) }

  fun openColorInColorsTool(hex: String) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    if (onOpenColorPicker != null) {
      onOpenColorPicker(hex)
    } else {
      inlineColorPickerHex = hex
    }
  }

  fun stepToTrack(
    newIndex: Int,
    autoPlay: Boolean = isPlaying,
  ) {
    session.stepToTrack(newIndex, autoPlay = autoPlay)
  }

  fun playTrackInContext(
    track: MusicTrack,
    contextTracks: List<MusicTrack>,
  ) {
    session.playTrackInContext(track, contextTracks)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun skipToNext(userInitiated: Boolean = true) {
    session.skipToNext(userInitiated = userInitiated)
  }

  fun skipToPrevious() {
    session.skipToPrevious()
  }

  fun moveQueueItem(fromIndex: Int, toIndex: Int) {
    if (session.moveQueueItem(fromIndex, toIndex)) {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  // Hardware volume keys control the system media volume while the Music tool is open or playing in the background
  DisposableEffect(context, session) {
    val previousStream = activity?.volumeControlStream
    activity?.volumeControlStream = AudioManager.STREAM_MUSIC
    onDispose {
      session.flushPlayStats()
      if (activity != null && previousStream != null && !session.isPlaying) {
        activity.volumeControlStream = previousStream
      }
    }
  }

  // Dynamic Album Art Palette for the active track
  var activePalette by remember(currentTrack?.id, currentTrack?.artworkPrimaryRgb, session.activePalette) {
    mutableStateOf(
      if (session.currentTrack?.id == currentTrack?.id) session.activePalette
      else MusicPlayerEngine.resolveTrackPalette(currentTrack)
    )
  }

  LaunchedEffect(currentTrack?.id, currentTrack?.album, currentTrack?.artist, isInspection) {
    val track = currentTrack ?: return@LaunchedEffect
    val resolved = if (isInspection || track.isSampleTrack) {
      MusicPlayerEngine.resolveTrackPalette(track)
    } else {
      MusicPlayerEngine.loadTrackPaletteAsync(track, sizePx = 360)
    }
    activePalette = resolved
    session.activePalette = resolved
    MusicPlayerEngine.updateActiveTrackPalette(track, resolved)
  }

  val animatedDominant by animateColorAsState(
    targetValue = activePalette.dominantColor,
    animationSpec = tween(durationMillis = 450),
    label = "dominantAlbumColor",
  )
  val animatedSecondary by animateColorAsState(
    targetValue = activePalette.secondaryColor,
    animationSpec = tween(durationMillis = 450),
    label = "secondaryAlbumColor",
  )
  val animatedAccent by animateColorAsState(
    targetValue = activePalette.accentColor,
    animationSpec = tween(durationMillis = 450),
    label = "accentAlbumColor",
  )
  val animatedDarkSurface by animateColorAsState(
    targetValue = activePalette.darkSurfaceColor,
    animationSpec = tween(durationMillis = 450),
    label = "darkSurfaceAlbumColor",
  )
  val animatedDeepBg by animateColorAsState(
    targetValue = activePalette.deepBackgroundColor,
    animationSpec = tween(durationMillis = 450),
    label = "deepBgAlbumColor",
  )

  val hasDrillDown = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null

  val activeInlineColorHex = inlineColorPickerHex
  if (activeInlineColorHex != null) {
    ColorPickerScreen(
      modifier = modifier,
      initialHexColor = activeInlineColorHex,
      onNavigateBack = {
        inlineColorPickerHex = null
      },
    )
    return
  }

  BackHandler(enabled = isPlayerExpanded || showQueueSheet || showSavePlaylistDialog || isSearchActive || hasDrillDown) {
    when {
      showSavePlaylistDialog -> showSavePlaylistDialog = false
      showQueueSheet -> showQueueSheet = false
      isPlayerExpanded -> isPlayerExpanded = false
      isSearchActive -> {
        isSearchActive = false
        searchQuery = ""
      }
      hasDrillDown -> clearDrillDown()
    }
  }

  val baseBg = MaterialTheme.colorScheme.background
  val ambientTopColor = lerp(baseBg, animatedDominant, 0.22f)
  val ambientMidColor = lerp(baseBg, animatedSecondary, 0.10f)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(ambientTopColor, ambientMidColor, baseBg),
        )
      )
      .testTag("music_player_screen")
      .semantics {
        contentDescription = "Music Player background ${activePalette.dominantHex}"
      },
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
    ) {
      // 1. Top Tool Header Bar
      val activeArtist = selectedArtistName?.let { name ->
        baseArtists.find { it.name.equals(name, ignoreCase = true) }?.let { artist ->
          if (searchQuery.isBlank()) {
            artist
          } else {
            artist.copy(
              albums = MusicPlayerEngine.filterAlbums(artist.albums, searchQuery),
              tracks = MusicPlayerEngine.filterTracks(artist.tracks, searchQuery),
            )
          }
        }
      }
      val activeAlbum = selectedAlbumKey?.let { key ->
        baseAlbums.find { "${it.name}|${it.artist}".equals(key, ignoreCase = true) }?.let { album ->
          if (searchQuery.isBlank()) {
            album
          } else {
            album.copy(
              tracks = MusicPlayerEngine.filterTracks(album.tracks, searchQuery),
            )
          }
        }
      }
      val activeGenre = selectedGenreName?.let { name ->
        baseGenres.find { it.name.equals(name, ignoreCase = true) }?.let { genre ->
          if (searchQuery.isBlank()) {
            genre
          } else {
            genre.copy(
              tracks = MusicPlayerEngine.filterTracks(genre.tracks, searchQuery),
            )
          }
        }
      }
      val activePlaylist = selectedPlaylistId?.let { id ->
        basePlaylists.find { it.id == id }?.let { playlist ->
          if (searchQuery.isBlank()) {
            playlist
          } else {
            playlist.copy(
              tracks = MusicPlayerEngine.filterTracks(playlist.tracks, searchQuery),
            )
          }
        }
      }

      val headerTitle = when {
        activeAlbum != null -> activeAlbum.name
        activeArtist != null -> activeArtist.name
        activeGenre != null -> activeGenre.name
        activePlaylist != null -> activePlaylist.name
        else -> "Music"
      }
      val headerSubtext = when {
        activeAlbum != null -> "${activeAlbum.artist} • ${activeAlbum.songCount} songs • ${activeAlbum.totalDurationFormatted}"
        activeArtist != null -> "${activeArtist.albumCount} albums • ${activeArtist.songCount} songs"
        activeGenre != null -> "${activeGenre.songCount} songs • ${activeGenre.artistCount} artists"
        activePlaylist != null -> activePlaylist.summaryText
        else -> "${baseArtists.size} artists • ${baseAlbums.size} albums • ${allTracks.size} songs"
      }
      val searchPlaceholder = when {
        activeAlbum != null -> "Search in ${activeAlbum.name}..."
        activeArtist != null -> "Search in ${activeArtist.name}..."
        activeGenre != null -> "Search in ${activeGenre.name}..."
        activePlaylist != null -> "Search in ${activePlaylist.name}..."
        else -> "Search ${selectedTab.displayName.lowercase()}..."
      }

      MusicPlayerTopBar(
        title = headerTitle,
        subtext = headerSubtext,
        searchPlaceholder = searchPlaceholder,
        hasDrillDown = hasDrillDown,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        viewMode = viewMode,
        sortOrder = sortOrder,
        showSortMenu = showSortMenu,
        accentTint = animatedDominant,
        onBackClick = { clearDrillDown() },
        onSearchToggle = {
          isSearchActive = !isSearchActive
          if (!isSearchActive) searchQuery = ""
        },
        onCloseSearch = {
          isSearchActive = false
          searchQuery = ""
        },
        onSearchQueryChange = { searchQuery = it },
        onToggleViewMode = {
          updateViewMode(viewMode.toggle())
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        onSelectViewMode = { mode ->
          updateViewMode(mode)
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        onOpenSortMenu = { showSortMenu = true },
        onDismissSortMenu = { showSortMenu = false },
        onSelectSortOrder = { order ->
          updateSortOrder(order)
          showSortMenu = false
        },
      )

      // 2. Category Tabs (Artists, Albums, Songs, Genres, Playlists)
      MusicCategoryAndViewBar(
        selectedTab = selectedTab,
        onSelectTab = { tab ->
          clearDrillDown()
          updateTab(tab)
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
      )

      // 3. Main Content Area (Drill-down or Selected Tab in List/Card View)
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      ) {
        when {
          activeAlbum != null -> {
            AlbumDetailContent(
              album = activeAlbum,
              viewMode = viewMode,
              currentTrackId = currentTrack?.id,
              isPlaying = isPlaying,
              favoriteIds = favoriteIds,
              onPlayAlbum = { shuffle ->
                if (shuffle && !isShuffleEnabled) toggleShuffle()
                val tracks = if (shuffle) activeAlbum.tracks.shuffled() else activeAlbum.tracks
                if (tracks.isNotEmpty()) {
                  playTrackInContext(tracks.first(), tracks)
                }
              },
              onSelectTrack = { track ->
                playTrackInContext(track, activeAlbum.tracks)
              },
              onToggleFavorite = { toggleFavorite(it.id) },
            )
          }

          activeArtist != null -> {
            ArtistDetailContent(
              artist = activeArtist,
              viewMode = viewMode,
              currentTrackId = currentTrack?.id,
              isPlaying = isPlaying,
              favoriteIds = favoriteIds,
              onPlayArtist = { shuffle ->
                if (shuffle && !isShuffleEnabled) toggleShuffle()
                val tracks = if (shuffle) activeArtist.tracks.shuffled() else activeArtist.tracks
                if (tracks.isNotEmpty()) {
                  playTrackInContext(tracks.first(), tracks)
                }
              },
              onOpenAlbum = { album ->
                selectedAlbumKey = "${album.name}|${album.artist}"
              },
              onSelectTrack = { track ->
                playTrackInContext(track, activeArtist.tracks)
              },
              onToggleFavorite = { toggleFavorite(it.id) },
            )
          }

          activeGenre != null -> {
            GenreDetailContent(
              genre = activeGenre,
              viewMode = viewMode,
              currentTrackId = currentTrack?.id,
              isPlaying = isPlaying,
              favoriteIds = favoriteIds,
              onPlayGenre = { shuffle ->
                if (shuffle && !isShuffleEnabled) toggleShuffle()
                val tracks = if (shuffle) activeGenre.tracks.shuffled() else activeGenre.tracks
                if (tracks.isNotEmpty()) {
                  playTrackInContext(tracks.first(), tracks)
                }
              },
              onSelectTrack = { track ->
                playTrackInContext(track, activeGenre.tracks)
              },
              onToggleFavorite = { toggleFavorite(it.id) },
            )
          }

          activePlaylist != null -> {
            PlaylistDetailContent(
              playlist = activePlaylist,
              viewMode = viewMode,
              currentTrackId = currentTrack?.id,
              isPlaying = isPlaying,
              favoriteIds = favoriteIds,
              playStats = playStats,
              onPlayPlaylist = { shuffle ->
                if (shuffle && !isShuffleEnabled) toggleShuffle()
                val tracks = if (shuffle) activePlaylist.tracks.shuffled() else activePlaylist.tracks
                if (tracks.isNotEmpty()) {
                  playTrackInContext(tracks.first(), tracks)
                }
              },
              onDeletePlaylist = {
                deletePlaylist(activePlaylist.id)
                selectedPlaylistId = null
              },
              onSelectTrack = { track ->
                playTrackInContext(track, activePlaylist.tracks)
              },
              onToggleFavorite = { toggleFavorite(it.id) },
            )
          }

          else -> {
            when (selectedTab) {
              MusicLibraryTab.ARTISTS -> {
                ArtistsTabContent(
                  artists = allArtists,
                  viewMode = viewMode,
                  currentArtistName = currentTrack?.artist,
                  isPlaying = isPlaying,
                  onPlayAll = {
                    val tracks = allArtists.flatMap { it.tracks }.ifEmpty { sortedTracks }
                    if (tracks.isNotEmpty()) {
                      playTrackInContext(tracks.first(), tracks)
                    }
                  },
                  onShuffleAll = {
                    if (allArtists.isNotEmpty()) {
                      val candidates = if (allArtists.size > 1 && currentTrack != null) {
                        allArtists.filter { !it.name.equals(currentTrack.artist, ignoreCase = true) }.ifEmpty { allArtists }
                      } else {
                        allArtists
                      }
                      val randomArtist = candidates.random()
                      if (randomArtist.tracks.isNotEmpty()) {
                        playTrackInContext(randomArtist.tracks.first(), randomArtist.tracks)
                      }
                    }
                  },
                  onSelectArtist = { artist ->
                    selectedArtistName = artist.name
                  },
                  onPlayArtist = { artist ->
                    if (artist.tracks.isNotEmpty()) {
                      playTrackInContext(artist.tracks.first(), artist.tracks)
                    }
                  },
                )
              }

              MusicLibraryTab.ALBUMS -> {
                AlbumsTabContent(
                  albums = allAlbums,
                  viewMode = viewMode,
                  currentAlbumName = currentTrack?.album,
                  isPlaying = isPlaying,
                  onPlayAll = {
                    val tracks = allAlbums.flatMap { it.tracks }.ifEmpty { sortedTracks }
                    if (tracks.isNotEmpty()) {
                      playTrackInContext(tracks.first(), tracks)
                    }
                  },
                  onShuffleAll = {
                    if (allAlbums.isNotEmpty()) {
                      val candidates = if (allAlbums.size > 1 && currentTrack != null) {
                        allAlbums.filter { !it.name.equals(currentTrack.album, ignoreCase = true) }.ifEmpty { allAlbums }
                      } else {
                        allAlbums
                      }
                      val randomAlbum = candidates.random()
                      if (randomAlbum.tracks.isNotEmpty()) {
                        playTrackInContext(randomAlbum.tracks.first(), randomAlbum.tracks)
                      }
                    }
                  },
                  onSelectAlbum = { album ->
                    selectedAlbumKey = "${album.name}|${album.artist}"
                  },
                  onPlayAlbum = { album ->
                    if (album.tracks.isNotEmpty()) {
                      playTrackInContext(album.tracks.first(), album.tracks)
                    }
                  },
                )
              }

              MusicLibraryTab.SONGS -> {
                SongsTabContent(
                  tracks = sortedTracks,
                  viewMode = viewMode,
                  currentTrackId = currentTrack?.id,
                  isPlaying = isPlaying,
                  favoriteIds = favoriteIds,
                  showFavoritesOnly = showFavoritesOnly,
                  onToggleFavoritesOnly = { showFavoritesOnly = !showFavoritesOnly },
                  onPlayAll = {
                    if (sortedTracks.isNotEmpty()) {
                      playTrackInContext(sortedTracks.first(), sortedTracks)
                    }
                  },
                  onShuffleAll = {
                    if (!isShuffleEnabled) toggleShuffle()
                    if (sortedTracks.isNotEmpty()) {
                      val shuffled = sortedTracks.shuffled()
                      playTrackInContext(shuffled.first(), shuffled)
                    }
                  },
                  onSelectTrack = { track ->
                    playTrackInContext(track, sortedTracks)
                  },
                  onToggleFavorite = { toggleFavorite(it.id) },
                )
              }

              MusicLibraryTab.GENRES -> {
                GenresTabContent(
                  genres = allGenres,
                  viewMode = viewMode,
                  currentGenreName = currentTrack?.genre,
                  isPlaying = isPlaying,
                  onPlayAll = {
                    val tracks = allGenres.flatMap { it.tracks }.ifEmpty { sortedTracks }
                    if (tracks.isNotEmpty()) {
                      playTrackInContext(tracks.first(), tracks)
                    }
                  },
                  onShuffleAll = {
                    if (allGenres.isNotEmpty()) {
                      val candidates = if (allGenres.size > 1 && currentTrack != null) {
                        allGenres.filter { !it.name.equals(currentTrack.genre, ignoreCase = true) }.ifEmpty { allGenres }
                      } else {
                        allGenres
                      }
                      val randomGenre = candidates.random()
                      if (randomGenre.tracks.isNotEmpty()) {
                        playTrackInContext(randomGenre.tracks.first(), randomGenre.tracks)
                      }
                    }
                  },
                  onSelectGenre = { genre ->
                    selectedGenreName = genre.name
                  },
                  onPlayGenre = { genre ->
                    if (genre.tracks.isNotEmpty()) {
                      playTrackInContext(genre.tracks.first(), genre.tracks)
                    }
                  },
                )
              }

              MusicLibraryTab.PLAYLISTS -> {
                PlaylistsTabContent(
                  playlists = allPlaylists,
                  viewMode = viewMode,
                  currentTrackId = currentTrack?.id,
                  isPlaying = isPlaying,
                  onPlayAll = {
                    val tracks = allPlaylists.flatMap { it.tracks }.distinctBy { it.id }.ifEmpty { sortedTracks }
                    if (tracks.isNotEmpty()) {
                      playTrackInContext(tracks.first(), tracks)
                    }
                  },
                  onShuffleAll = {
                    if (allPlaylists.isNotEmpty()) {
                      val randomPlaylist = allPlaylists.random()
                      if (randomPlaylist.tracks.isNotEmpty()) {
                        playTrackInContext(randomPlaylist.tracks.first(), randomPlaylist.tracks)
                      }
                    }
                  },
                  onSaveQueueAsPlaylist = {
                    showSavePlaylistDialog = true
                  },
                  onSelectPlaylist = { playlist ->
                    selectedPlaylistId = playlist.id
                  },
                  onPlayPlaylist = { playlist ->
                    if (playlist.tracks.isNotEmpty()) {
                      playTrackInContext(playlist.tracks.first(), playlist.tracks)
                    }
                  },
                  onDeletePlaylist = { playlist ->
                    deletePlaylist(playlist.id)
                  },
                )
              }
            }
          }
        }
      }

      // 4. Docked Collapsible Mini-Player Bar (follows current track's album art colors)
      if (currentTrack != null) {
        val progressFraction = if (trackDurationMs > 0L) {
          (currentPositionMs.toFloat() / trackDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
          0f
        }
        CollapsedMiniMusicPlayer(
          track = currentTrack,
          isPlaying = isPlaying,
          isFavorite = currentTrack.id in favoriteIds,
          progressFraction = progressFraction,
          dominantColor = animatedDominant,
          darkSurfaceColor = animatedDarkSurface,
          accentColor = animatedAccent,
          dominantHex = activePalette.dominantHex,
          onExpand = { isPlayerExpanded = true },
          onToggleFavorite = { toggleFavorite(currentTrack.id) },
          onPrevious = { skipToPrevious() },
          onPlayPause = {
            session.togglePlayPause()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          },
          onNext = { skipToNext(userInitiated = true) },
        )
      }
    }

    // 5. Full Expanded Collapsible Music Player Sheet (background follows the album art)
    AnimatedVisibility(
      visible = isPlayerExpanded && currentTrack != null,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
      if (currentTrack != null) {
        val seekFraction = if (isDraggingSeek) {
          dragSeekFraction
        } else if (trackDurationMs > 0L) {
          (currentPositionMs.toFloat() / trackDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
          0f
        }

        ExpandedMusicPlayerOverlay(
          track = currentTrack,
          isPlaying = isPlaying,
          isFavorite = currentTrack.id in favoriteIds,
          isShuffleEnabled = isShuffleEnabled,
          repeatMode = repeatMode,
          seekFraction = seekFraction,
          currentPositionMs = if (isDraggingSeek) (dragSeekFraction * trackDurationMs).toLong() else currentPositionMs,
          durationMs = trackDurationMs,
          playbackSpeed = playbackSpeed,
          sleepTimerPreset = sleepTimerPreset,
          sleepRemainingMs = sleepRemainingMs,
          queue = playbackQueue,
          currentQueueIndex = currentQueueIndex,
          initialQueueSheetValue = if (session.initialQueueExpandedState) SheetValue.Expanded else SheetValue.PartiallyExpanded,
          dominantColor = animatedDominant,
          secondaryColor = animatedSecondary,
          accentColor = animatedAccent,
          deepBackgroundColor = animatedDeepBg,
          dominantHex = activePalette.dominantHex,
          accentHex = activePalette.accentHex,
          showSleepTimerMenu = showSleepTimerMenu,
          onCollapse = { isPlayerExpanded = false },
          onToggleFavorite = { toggleFavorite(currentTrack.id) },
          onSeekChange = { fraction ->
            session.updateSeekDrag(fraction)
          },
          onSeekFinished = {
            session.finishSeekDrag()
          },
          onToggleShuffle = { toggleShuffle() },
          onPrevious = { skipToPrevious() },
          onPlayPause = {
            session.togglePlayPause()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          },
          onNext = { skipToNext(userInitiated = true) },
          onCycleRepeat = { cycleRepeatMode() },
          onCycleSpeed = {
            session.cyclePlaybackSpeed()
          },
          onSelectQueueIndex = { index ->
            stepToTrack(index, autoPlay = true)
          },
          onMoveQueueItem = { fromIndex, toIndex ->
            moveQueueItem(fromIndex, toIndex)
          },
          onSaveQueueAsPlaylist = {
            showSavePlaylistDialog = true
          },
          onOpenTrackInfo = { showTrackInfoDialog = true },
          onOpenSleepTimerMenu = { showSleepTimerMenu = true },
          onDismissSleepTimerMenu = { showSleepTimerMenu = false },
          onSelectSleepTimer = { preset ->
            session.selectSleepTimer(preset)
            showSleepTimerMenu = false
          },
          onNavigateToArtist = {
            isPlayerExpanded = false
            clearDrillDown()
            updateTab(MusicLibraryTab.ARTISTS)
            selectedArtistName = currentTrack.artist
          },
          onNavigateToAlbum = {
            isPlayerExpanded = false
            clearDrillDown()
            updateTab(MusicLibraryTab.ALBUMS)
            selectedAlbumKey = "${currentTrack.album}|${currentTrack.artist}"
          },
        )
      }
    }

    // 6. Queue Dialog (for Library View when Full Player is collapsed)
    if (showQueueSheet && !isPlayerExpanded) {
      PlaybackQueueDialog(
        queue = playbackQueue,
        currentQueueIndex = currentQueueIndex,
        isPlaying = isPlaying,
        accentColor = animatedDominant,
        onSelectQueueIndex = { index ->
          stepToTrack(index, autoPlay = true)
        },
        onMoveQueueItem = { fromIndex, toIndex ->
          moveQueueItem(fromIndex, toIndex)
        },
        onSaveQueueAsPlaylist = {
          showSavePlaylistDialog = true
        },
        onDismiss = { showQueueSheet = false },
      )
    }

    // 7. Save Queue as Playlist Dialog
    if (showSavePlaylistDialog) {
      SaveQueueAsPlaylistDialog(
        defaultPlaylistName = "Queue Playlist ${savedPlaylistEntries.size + 1}",
        queueTrackCount = playbackQueue.size,
        accentColor = animatedDominant,
        onConfirm = { name ->
          saveQueueAsPlaylist(name, playbackQueue.ifEmpty { allTracks })
          showSavePlaylistDialog = false
        },
        onDismiss = { showSavePlaylistDialog = false },
      )
    }

    // 8. Track Details Dialog
    if (showTrackInfoDialog && currentTrack != null) {
      TrackDetailsDialog(
        track = currentTrack,
        palette = activePalette,
        playStat = playStats[currentTrack.id] ?: TrackPlayStats(),
        onSelectColor = { hex ->
          showTrackInfoDialog = false
          openColorInColorsTool(hex)
        },
        onDismiss = { showTrackInfoDialog = false },
      )
    }
  }
}

@Composable
private fun MusicPlayerTopBar(
  title: String,
  subtext: String,
  searchPlaceholder: String,
  hasDrillDown: Boolean,
  isSearchActive: Boolean,
  searchQuery: String,
  viewMode: MusicViewMode,
  sortOrder: MusicSortOrder,
  showSortMenu: Boolean,
  accentTint: Color,
  onBackClick: () -> Unit,
  onSearchToggle: () -> Unit,
  onCloseSearch: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onToggleViewMode: () -> Unit,
  onSelectViewMode: (MusicViewMode) -> Unit,
  onOpenSortMenu: () -> Unit,
  onDismissSortMenu: () -> Unit,
  onSelectSortOrder: (MusicSortOrder) -> Unit,
) {
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  LaunchedEffect(isSearchActive) {
    if (isSearchActive) {
      try {
        focusRequester.requestFocus()
      } catch (_: Throwable) {}
    }
  }

  Surface(
    color = lerp(MaterialTheme.colorScheme.surface, accentTint, 0.12f),
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 68.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (isSearchActive) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = {
                focusManager.clearFocus()
                onCloseSearch()
              },
              modifier = Modifier
                .size(36.dp)
                .testTag("music_search_toggle"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Close search",
                tint = MaterialTheme.colorScheme.onSurface,
              )
            }

            TextField(
              value = searchQuery,
              onValueChange = onSearchQueryChange,
              placeholder = {
                Text(
                  text = searchPlaceholder,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              },
              singleLine = true,
              textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
              ),
              colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
              ),
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.testTag("music_search_clear"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Clear search",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              },
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
              keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
              modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .testTag("music_search_input"),
            )
          }
        } else {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            if (hasDrillDown) {
              IconButton(
                onClick = onBackClick,
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer)
                  .testTag("music_back_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
              }
            } else {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.Headphones,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          if (!isSearchActive) {
            IconButton(
              onClick = onSearchToggle,
              modifier = Modifier.testTag("music_search_toggle"),
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search music",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          ViewModeSegmentedToggle(
            isListView = viewMode == MusicViewMode.LIST,
            onSelectListView = { onSelectViewMode(MusicViewMode.LIST) },
            onSelectGridView = { onSelectViewMode(MusicViewMode.CARD) },
            onToggle = onToggleViewMode,
            accentColor = accentTint,
            toggleTestTag = "music_view_mode_toggle",
            listTestTag = "music_view_mode_list",
            gridTestTag = "music_view_mode_card",
            listContentDescription = "List view",
            gridContentDescription = "Card view",
          )

          Box {
            IconButton(
              onClick = onOpenSortMenu,
              modifier = Modifier.testTag("music_sort_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort tracks",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            DropdownMenu(
              expanded = showSortMenu,
              onDismissRequest = onDismissSortMenu,
            ) {
              MusicSortOrder.entries.forEach { option ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = option.displayName,
                      fontWeight = if (option == sortOrder) FontWeight.Bold else FontWeight.Normal,
                    )
                  },
                  leadingIcon = {
                    if (option == sortOrder) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                      )
                    }
                  },
                  onClick = { onSelectSortOrder(option) },
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Shared icon-only List / Card (or Grid / Tile) segmented view mode toggle used across tools.
 */
@Composable
internal fun ViewModeSegmentedToggle(
  isListView: Boolean,
  onSelectListView: () -> Unit,
  onSelectGridView: () -> Unit,
  onToggle: () -> Unit = {
    if (isListView) onSelectGridView() else onSelectListView()
  },
  accentColor: Color = MaterialTheme.colorScheme.primary,
  toggleTestTag: String? = null,
  listTestTag: String? = null,
  gridTestTag: String? = null,
  listContentDescription: String = "List view",
  gridContentDescription: String = "Card view",
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = CircleShape,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier
      .clip(CircleShape)
      .clickable(onClick = onToggle)
      .then(if (toggleTestTag != null) Modifier.testTag(toggleTestTag) else Modifier),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(2.dp),
    ) {
      val activeContainerColor = lerp(MaterialTheme.colorScheme.primaryContainer, accentColor, 0.25f)

      Surface(
        shape = CircleShape,
        color = if (isListView) activeContainerColor else Color.Transparent,
        modifier = Modifier
          .clip(CircleShape)
          .clickable {
            if (isListView) onToggle() else onSelectListView()
          }
          .then(if (listTestTag != null) Modifier.testTag(listTestTag) else Modifier),
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ViewList,
            contentDescription = listContentDescription,
            tint = if (isListView) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
          )
        }
      }

      Surface(
        shape = CircleShape,
        color = if (!isListView) activeContainerColor else Color.Transparent,
        modifier = Modifier
          .clip(CircleShape)
          .clickable {
            if (!isListView) onToggle() else onSelectGridView()
          }
          .then(if (gridTestTag != null) Modifier.testTag(gridTestTag) else Modifier),
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = gridContentDescription,
            tint = if (!isListView) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun MusicCategoryAndViewBar(
  selectedTab: MusicLibraryTab,
  onSelectTab: (MusicLibraryTab) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 8.dp)
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    MusicLibraryTab.entries.forEach { tab ->
      val isSelected = tab == selectedTab
      val tabIcon = when (tab) {
        MusicLibraryTab.ARTISTS -> Icons.Default.Person
        MusicLibraryTab.ALBUMS -> Icons.Default.Album
        MusicLibraryTab.SONGS -> Icons.Default.MusicNote
        MusicLibraryTab.GENRES -> Icons.Default.Category
        MusicLibraryTab.PLAYLISTS -> Icons.AutoMirrored.Filled.PlaylistPlay
      }
      FilterChip(
        selected = isSelected,
        onClick = { onSelectTab(tab) },
        label = {
          Text(
            text = tab.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          )
        },
        leadingIcon = {
          Icon(
            imageVector = tabIcon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        },
        shape = CircleShape,
        modifier = Modifier.testTag("music_tab_${tab.name}"),
      )
    }
  }
}

@Composable
private fun ArtistsTabContent(
  artists: List<MusicArtist>,
  viewMode: MusicViewMode,
  currentArtistName: String?,
  isPlaying: Boolean,
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectArtist: (MusicArtist) -> Unit,
  onPlayArtist: (MusicArtist) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Quick Action Row: Play All, Shuffle
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalButton(
        onClick = onPlayAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_play_all_button"),
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Play All", style = MaterialTheme.typography.labelMedium)
      }

      OutlinedButton(
        onClick = onShuffleAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_shuffle_all_button"),
      ) {
        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Shuffle", style = MaterialTheme.typography.labelMedium)
      }
    }

    if (artists.isEmpty()) {
      EmptyMusicState(message = "No artists match your search")
      return
    }

    if (viewMode == MusicViewMode.LIST) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("artist_list_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(artists, key = { it.name }) { artist ->
          val isActive = currentArtistName != null && currentArtistName.equals(artist.name, ignoreCase = true)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectArtist(artist) }
              .testTag("artist_item_${artist.name}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              TrackArtworkThumbnail(
                track = artist.representativeTrack,
                size = 52.dp,
                shape = CircleShape,
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = artist.name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${artist.summaryText} • ${artist.genres.joinToString(", ")}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              FilledIconButton(
                onClick = { onPlayArtist(artist) },
                shape = CircleShape,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("play_artist_${artist.name}"),
                colors = IconButtonDefaults.filledIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.primary,
                ),
              ) {
                Icon(
                  imageVector = if (isActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                  contentDescription = "Play ${artist.name}",
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxSize()
          .testTag("artist_card_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(artists, key = { it.name }) { artist ->
          val isActive = currentArtistName != null && currentArtistName.equals(artist.name, ignoreCase = true)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectArtist(artist) }
              .testTag("artist_item_${artist.name}"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(contentAlignment = Alignment.BottomEnd) {
                TrackArtworkThumbnail(
                  track = artist.representativeTrack,
                  size = 96.dp,
                  shape = CircleShape,
                )
                FilledIconButton(
                  onClick = { onPlayArtist(artist) },
                  shape = CircleShape,
                  modifier = Modifier.size(30.dp),
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                  ),
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play ${artist.name}",
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
              Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
              )
              Text(
                text = artist.summaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AlbumsTabContent(
  albums: List<MusicAlbum>,
  viewMode: MusicViewMode,
  currentAlbumName: String?,
  isPlaying: Boolean,
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectAlbum: (MusicAlbum) -> Unit,
  onPlayAlbum: (MusicAlbum) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Quick Action Row: Play All, Shuffle
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalButton(
        onClick = onPlayAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_play_all_button"),
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Play All", style = MaterialTheme.typography.labelMedium)
      }

      OutlinedButton(
        onClick = onShuffleAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_shuffle_all_button"),
      ) {
        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Shuffle", style = MaterialTheme.typography.labelMedium)
      }
    }

    if (albums.isEmpty()) {
      EmptyMusicState(message = "No albums match your search")
      return
    }

    if (viewMode == MusicViewMode.LIST) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("album_list_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(albums, key = { "${it.name}|${it.artist}" }) { album ->
          val isActive = currentAlbumName != null && currentAlbumName.equals(album.name, ignoreCase = true)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectAlbum(album) }
              .testTag("album_item_${album.name}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              TrackArtworkThumbnail(
                track = album.representativeTrack,
                size = 56.dp,
                shape = RoundedCornerShape(12.dp),
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = album.name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${album.summaryText} • ${album.year}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              FilledIconButton(
                onClick = { onPlayAlbum(album) },
                shape = CircleShape,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("play_album_${album.name}"),
                colors = IconButtonDefaults.filledIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.primary,
                ),
              ) {
                Icon(
                  imageVector = if (isActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                  contentDescription = "Play ${album.name}",
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxSize()
          .testTag("album_card_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(albums, key = { "${it.name}|${it.artist}" }) { album ->
          val isActive = currentAlbumName != null && currentAlbumName.equals(album.name, ignoreCase = true)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectAlbum(album) }
              .testTag("album_item_${album.name}"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .aspectRatio(1f),
                contentAlignment = Alignment.BottomEnd,
              ) {
                TrackArtworkBanner(
                  track = album.representativeTrack,
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                )
                FilledIconButton(
                  onClick = { onPlayAlbum(album) },
                  shape = CircleShape,
                  modifier = Modifier
                    .padding(8.dp)
                    .size(34.dp),
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                  ),
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play ${album.name}",
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
              Text(
                text = album.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = album.summaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SongsTabContent(
  tracks: List<MusicTrack>,
  viewMode: MusicViewMode,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  showFavoritesOnly: Boolean,
  onToggleFavoritesOnly: () -> Unit,
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Quick Action Row: Play All, Shuffle, Favorites Filter
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalButton(
        onClick = onPlayAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_play_all_button"),
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Play All", style = MaterialTheme.typography.labelMedium)
      }

      OutlinedButton(
        onClick = onShuffleAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_shuffle_all_button"),
      ) {
        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Shuffle", style = MaterialTheme.typography.labelMedium)
      }

      Spacer(modifier = Modifier.weight(1f))

      FilterChip(
        selected = showFavoritesOnly,
        onClick = onToggleFavoritesOnly,
        label = { Text("Favorites", style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
          Icon(
            imageVector = if (showFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
          )
        },
        shape = CircleShape,
        modifier = Modifier.testTag("music_favorites_filter_chip"),
      )
    }

    if (tracks.isEmpty()) {
      EmptyMusicState(
        message = if (showFavoritesOnly) "No favorite songs yet" else "No songs match your search",
      )
      return
    }

    if (viewMode == MusicViewMode.LIST) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("song_list_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(tracks, key = { it.id }) { track ->
          SongListItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxSize()
          .testTag("song_card_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(tracks, key = { it.id }) { track ->
          SongGridItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    }
  }
}

@Composable
private fun GenresTabContent(
  genres: List<MusicGenre>,
  viewMode: MusicViewMode,
  currentGenreName: String?,
  isPlaying: Boolean,
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectGenre: (MusicGenre) -> Unit,
  onPlayGenre: (MusicGenre) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Quick Action Row: Play All, Shuffle
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalButton(
        onClick = onPlayAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_play_all_button"),
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Play All", style = MaterialTheme.typography.labelMedium)
      }

      OutlinedButton(
        onClick = onShuffleAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_shuffle_all_button"),
      ) {
        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Shuffle", style = MaterialTheme.typography.labelMedium)
      }
    }

    if (genres.isEmpty()) {
      EmptyMusicState(message = "No genres match your search")
      return
    }

    if (viewMode == MusicViewMode.LIST) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("genre_list_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(genres, key = { it.name }) { genre ->
          val isActive = currentGenreName != null && currentGenreName.equals(genre.name, ignoreCase = true)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectGenre(genre) }
              .testTag("genre_item_${genre.name}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              TrackArtworkThumbnail(
                track = genre.representativeTrack,
                size = 52.dp,
                shape = RoundedCornerShape(12.dp),
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = genre.name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${genre.summaryText} • ${genre.totalDurationFormatted}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              FilledIconButton(
                onClick = { onPlayGenre(genre) },
                shape = CircleShape,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("play_genre_${genre.name}"),
                colors = IconButtonDefaults.filledIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.primary,
                ),
              ) {
                Icon(
                  imageVector = if (isActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                  contentDescription = "Play ${genre.name}",
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxSize()
          .testTag("genre_card_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(genres, key = { it.name }) { genre ->
          val isActive = currentGenreName != null && currentGenreName.equals(genre.name, ignoreCase = true)
          val genrePalette = remember(genre.representativeTrack?.id) {
            MusicPlayerEngine.resolveTrackPalette(genre.representativeTrack)
          }
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectGenre(genre) }
              .testTag("genre_item_${genre.name}"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(84.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                    Brush.linearGradient(
                      colors = listOf(genrePalette.dominantColor, genrePalette.secondaryColor),
                    )
                  )
                  .padding(10.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.LibraryMusic,
                  contentDescription = null,
                  tint = Color.White.copy(alpha = 0.85f),
                  modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopStart),
                )
                FilledIconButton(
                  onClick = { onPlayGenre(genre) },
                  shape = CircleShape,
                  modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomEnd),
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.22f),
                    contentColor = Color.White,
                  ),
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play ${genre.name}",
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
              Text(
                text = genre.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = genre.summaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PlaylistsTabContent(
  playlists: List<MusicPlaylist>,
  viewMode: MusicViewMode,
  currentTrackId: String?,
  isPlaying: Boolean,
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSaveQueueAsPlaylist: () -> Unit,
  onSelectPlaylist: (MusicPlaylist) -> Unit,
  onPlayPlaylist: (MusicPlaylist) -> Unit,
  onDeletePlaylist: (MusicPlaylist) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Quick Action Row: Play All, Shuffle, Save Queue
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalButton(
        onClick = onPlayAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_play_all_button"),
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Play All", style = MaterialTheme.typography.labelMedium)
      }

      OutlinedButton(
        onClick = onShuffleAll,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_shuffle_all_button"),
      ) {
        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Shuffle", style = MaterialTheme.typography.labelMedium)
      }

      Spacer(modifier = Modifier.weight(1f))

      OutlinedButton(
        onClick = onSaveQueueAsPlaylist,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("playlists_save_queue_button"),
      ) {
        Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Save Queue", style = MaterialTheme.typography.labelMedium)
      }
    }

    if (playlists.isEmpty()) {
      EmptyMusicState(message = "No playlists yet — save a queue as a playlist to get started")
      return
    }

    if (viewMode == MusicViewMode.LIST) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("playlist_list_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(playlists, key = { it.id }) { playlist ->
          val isActive = currentTrackId != null && playlist.tracks.any { it.id == currentTrackId }
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectPlaylist(playlist) }
              .testTag("playlist_item_${playlist.name}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              TrackArtworkThumbnail(
                track = playlist.representativeTrack,
                size = 52.dp,
                shape = RoundedCornerShape(12.dp),
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = playlist.name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = playlist.summaryText,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              if (!playlist.isBuiltIn) {
                IconButton(
                  onClick = { onDeletePlaylist(playlist) },
                  modifier = Modifier
                    .size(34.dp)
                    .testTag("delete_playlist_${playlist.name}"),
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${playlist.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
              FilledIconButton(
                onClick = { onPlayPlaylist(playlist) },
                shape = CircleShape,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("play_playlist_${playlist.name}"),
                colors = IconButtonDefaults.filledIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.primary,
                ),
              ) {
                Icon(
                  imageVector = if (isActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                  contentDescription = "Play ${playlist.name}",
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxSize()
          .testTag("playlist_card_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(playlists, key = { it.id }) { playlist ->
          val isActive = currentTrackId != null && playlist.tracks.any { it.id == currentTrackId }
          val playlistPalette = remember(playlist.representativeTrack?.id) {
            MusicPlayerEngine.resolveTrackPalette(playlist.representativeTrack)
          }
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectPlaylist(playlist) }
              .testTag("playlist_item_${playlist.name}"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(84.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                    Brush.linearGradient(
                      colors = listOf(playlistPalette.dominantColor, playlistPalette.secondaryColor),
                    )
                  )
                  .padding(10.dp),
              ) {
                Icon(
                  imageVector = when {
                    playlist.isFavorites -> Icons.Default.Star
                    playlist.isMostPlayed -> Icons.Default.GraphicEq
                    else -> Icons.AutoMirrored.Filled.PlaylistPlay
                  },
                  contentDescription = null,
                  tint = Color.White.copy(alpha = 0.85f),
                  modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopStart),
                )
                FilledIconButton(
                  onClick = { onPlayPlaylist(playlist) },
                  shape = CircleShape,
                  modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomEnd),
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.22f),
                    contentColor = Color.White,
                  ),
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play ${playlist.name}",
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
              Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = playlist.summaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ArtistDetailContent(
  artist: MusicArtist,
  viewMode: MusicViewMode,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  onPlayArtist: (shuffle: Boolean) -> Unit,
  onOpenAlbum: (MusicAlbum) -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("artist_detail_view"),
  ) {
    // Artist Header Card + Albums Carousel
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          TrackArtworkThumbnail(
            track = artist.representativeTrack,
            size = 64.dp,
            shape = CircleShape,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = artist.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "${artist.summaryText} • ${artist.totalDurationFormatted}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilledTonalButton(
                onClick = { onPlayArtist(false) },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play All", style = MaterialTheme.typography.labelMedium)
              }
              OutlinedButton(
                onClick = { onPlayArtist(true) },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
              ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Shuffle", style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }

        if (artist.albums.isNotEmpty()) {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(artist.albums, key = { it.name }) { album ->
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable { onOpenAlbum(album) }
                  .padding(2.dp),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                  )
                  Text(
                    text = "${album.name} (${album.songCount})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                  )
                }
              }
            }
          }
        }
      }
    }

    if (viewMode == MusicViewMode.LIST) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(artist.tracks, key = { it.id }) { track ->
          SongListItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(artist.tracks, key = { it.id }) { track ->
          SongGridItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    }
  }
}

@Composable
private fun AlbumDetailContent(
  album: MusicAlbum,
  viewMode: MusicViewMode,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  onPlayAlbum: (shuffle: Boolean) -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("album_detail_view"),
  ) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        TrackArtworkThumbnail(
          track = album.representativeTrack,
          size = 72.dp,
          shape = RoundedCornerShape(14.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = album.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${album.artist} • ${album.year} • ${album.genre}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { onPlayAlbum(false) },
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Play All", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
              onClick = { onPlayAlbum(true) },
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
              Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Shuffle", style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }
    }

    if (viewMode == MusicViewMode.LIST) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(album.tracks, key = { it.id }) { track ->
          SongListItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(album.tracks, key = { it.id }) { track ->
          SongGridItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    }
  }
}

@Composable
private fun GenreDetailContent(
  genre: MusicGenre,
  viewMode: MusicViewMode,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  onPlayGenre: (shuffle: Boolean) -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("genre_detail_view"),
  ) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        TrackArtworkThumbnail(
          track = genre.representativeTrack,
          size = 56.dp,
          shape = RoundedCornerShape(12.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = genre.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${genre.summaryText} • ${genre.totalDurationFormatted}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
              onClick = { onPlayGenre(false) },
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Play All", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
              onClick = { onPlayGenre(true) },
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
              Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Shuffle", style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }
    }

    if (viewMode == MusicViewMode.LIST) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(genre.tracks, key = { it.id }) { track ->
          SongListItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(genre.tracks, key = { it.id }) { track ->
          SongGridItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    }
  }
}

@Composable
private fun PlaylistDetailContent(
  playlist: MusicPlaylist,
  viewMode: MusicViewMode,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  playStats: Map<String, TrackPlayStats> = emptyMap(),
  onPlayPlaylist: (shuffle: Boolean) -> Unit,
  onDeletePlaylist: () -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("playlist_detail_view"),
  ) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        TrackArtworkThumbnail(
          track = playlist.representativeTrack,
          size = 56.dp,
          shape = RoundedCornerShape(12.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = playlist.summaryText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
              onClick = { onPlayPlaylist(false) },
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Play All", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
              onClick = { onPlayPlaylist(true) },
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
              Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Shuffle", style = MaterialTheme.typography.labelMedium)
            }
          }
        }
        if (!playlist.isBuiltIn) {
          IconButton(
            onClick = onDeletePlaylist,
            modifier = Modifier.size(36.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete playlist",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    if (playlist.isFavorites && playlist.tracks.isEmpty()) {
      EmptyMusicState(message = "No favorites yet — tap the star on any song to add it here")
      return@Column
    }

    if (playlist.isMostPlayed && playlist.tracks.isEmpty()) {
      EmptyMusicState(message = "No songs in library yet")
      return@Column
    }

    if (viewMode == MusicViewMode.LIST) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(playlist.tracks, key = { it.id }) { track ->
          val statBadge = if (playlist.isMostPlayed) {
            val s = playStats[track.id]
            if (s != null && s.hasBeenPlayed) {
              val playsLabel = if (s.playCount == 1) "1 play" else "${s.playCount} plays"
              "$playsLabel • ${s.totalPlayTimeFormatted}"
            } else {
              null
            }
          } else {
            null
          }
          SongListItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            playStatSummary = statBadge,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(playlist.tracks, key = { it.id }) { track ->
          val statBadge = if (playlist.isMostPlayed) {
            val s = playStats[track.id]
            if (s != null && s.hasBeenPlayed) {
              val playsLabel = if (s.playCount == 1) "1 play" else "${s.playCount} plays"
              "$playsLabel • ${s.totalPlayTimeFormatted}"
            } else {
              null
            }
          } else {
            null
          }
          SongGridItemCard(
            track = track,
            isCurrent = track.id == currentTrackId,
            isPlaying = isPlaying && track.id == currentTrackId,
            isFavorite = track.id in favoriteIds,
            playStatSummary = statBadge,
            onClick = { onSelectTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) },
          )
        }
      }
    }
  }
}

@Composable
private fun SongListItemCard(
  track: MusicTrack,
  isCurrent: Boolean,
  isPlaying: Boolean,
  isFavorite: Boolean,
  playStatSummary: String? = null,
  onClick: () -> Unit,
  onToggleFavorite: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
      else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("song_item_${track.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        TrackArtworkThumbnail(
          track = track,
          size = 48.dp,
          shape = RoundedCornerShape(10.dp),
        )
        if (isCurrent) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color.Black.copy(alpha = 0.42f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Now Playing" else "Selected",
              tint = Color.White,
              modifier = Modifier.size(22.dp),
            )
          }
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = track.title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = if (playStatSummary != null) {
            "${track.artist} • $playStatSummary"
          } else {
            "${track.artist} • ${track.album}"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Text(
        text = track.durationFormatted,
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier
          .size(34.dp)
          .testTag("favorite_song_${track.id}"),
      ) {
        Icon(
          imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
          tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
private fun SongGridItemCard(
  track: MusicTrack,
  isCurrent: Boolean,
  isPlaying: Boolean,
  isFavorite: Boolean,
  playStatSummary: String? = null,
  onClick: () -> Unit,
  onToggleFavorite: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
      else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("song_item_${track.id}"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.15f)
          .clip(RoundedCornerShape(12.dp)),
      ) {
        TrackArtworkBanner(
          track = track,
          modifier = Modifier.fillMaxSize(),
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color.Black.copy(alpha = 0.55f),
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(6.dp),
        ) {
          Text(
            text = track.durationFormatted,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          )
        }
        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(30.dp),
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) Color(0xFFFF80AB) else Color.White,
            modifier = Modifier.size(16.dp),
          )
        }
        if (isCurrent) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(6.dp)
              .size(28.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }

      Text(
        text = track.title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = if (playStatSummary != null) {
          "${track.artist} • $playStatSummary"
        } else {
          "${track.artist} • ${track.album}"
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/**
 * Collapsible Mini-Player docked at the bottom of the Music Player screen.
 * Its background dynamically follows the current song's album art colors.
 */
@Composable
private fun CollapsedMiniMusicPlayer(
  track: MusicTrack,
  isPlaying: Boolean,
  isFavorite: Boolean,
  progressFraction: Float,
  dominantColor: Color,
  darkSurfaceColor: Color,
  accentColor: Color,
  dominantHex: String,
  onExpand: () -> Unit,
  onToggleFavorite: () -> Unit,
  onPrevious: () -> Unit,
  onPlayPause: () -> Unit,
  onNext: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .clickable(onClick = onExpand)
      .testTag("music_player_collapsed")
      .semantics {
        contentDescription = "Mini music player background $dominantHex"
      },
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            colors = listOf(darkSurfaceColor, lerp(dominantColor, Color(0xFF14141F), 0.38f)),
          )
        ),
    ) {
      LinearProgressIndicator(
        progress = { progressFraction.coerceIn(0f, 1f) },
        color = accentColor,
        trackColor = Color.White.copy(alpha = 0.16f),
        modifier = Modifier
          .fillMaxWidth()
          .height(3.dp),
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        TrackArtworkThumbnail(
          track = track,
          size = 44.dp,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("mini_player_album_art"),
        )

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("mini_player_title"),
          )
          Text(
            text = "${track.artist} • ${track.album}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.78f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("mini_player_subtitle"),
          )
        }

        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier
            .size(32.dp)
            .testTag("mini_player_favorite_button"),
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) accentColor else Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp),
          )
        }

        IconButton(
          onClick = onPrevious,
          modifier = Modifier
            .size(34.dp)
            .testTag("mini_player_prev_button"),
        ) {
          Icon(
            imageVector = Icons.Default.SkipPrevious,
            contentDescription = "Previous track",
            tint = Color.White,
            modifier = Modifier.size(22.dp),
          )
        }

        FilledIconButton(
          onClick = onPlayPause,
          shape = CircleShape,
          modifier = Modifier
            .size(38.dp)
            .testTag("mini_player_play_pause_button"),
          colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = accentColor,
            contentColor = if (accentColor.red * 0.299f + accentColor.green * 0.587f + accentColor.blue * 0.114f > 0.55f) {
              Color(0xFF101018)
            } else {
              Color.White
            },
          ),
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(20.dp),
          )
        }

        IconButton(
          onClick = onNext,
          modifier = Modifier
            .size(34.dp)
            .testTag("mini_player_next_button"),
        ) {
          Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Next track",
            tint = Color.White,
            modifier = Modifier.size(22.dp),
          )
        }

        IconButton(
          onClick = onExpand,
          modifier = Modifier
            .size(32.dp)
            .testTag("music_player_expand_button"),
        ) {
          Icon(
            imageVector = Icons.Default.ExpandLess,
            contentDescription = "Expand player",
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}

/**
 * Full-Screen Collapsible Music Player Sheet.
 * Renders a multi-stop vertical gradient derived directly from the active song's album art colors
 * and a slide-up Queue bottom sheet panel styled similarly to the Roll History panel in the Dice Roller tool.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedMusicPlayerOverlay(
  track: MusicTrack,
  isPlaying: Boolean,
  isFavorite: Boolean,
  isShuffleEnabled: Boolean,
  repeatMode: MusicRepeatMode,
  seekFraction: Float,
  currentPositionMs: Long,
  durationMs: Long,
  playbackSpeed: Float,
  sleepTimerPreset: SleepTimerPreset,
  sleepRemainingMs: Long,
  queue: List<MusicTrack>,
  currentQueueIndex: Int,
  initialQueueSheetValue: SheetValue = SheetValue.PartiallyExpanded,
  dominantColor: Color,
  secondaryColor: Color,
  accentColor: Color,
  deepBackgroundColor: Color,
  dominantHex: String,
  accentHex: String,
  showSleepTimerMenu: Boolean,
  onCollapse: () -> Unit,
  onToggleFavorite: () -> Unit,
  onSeekChange: (Float) -> Unit,
  onSeekFinished: () -> Unit,
  onToggleShuffle: () -> Unit,
  onPrevious: () -> Unit,
  onPlayPause: () -> Unit,
  onNext: () -> Unit,
  onCycleRepeat: () -> Unit,
  onCycleSpeed: () -> Unit,
  onSelectQueueIndex: (Int) -> Unit,
  onMoveQueueItem: (fromIndex: Int, toIndex: Int) -> Unit,
  onSaveQueueAsPlaylist: () -> Unit,
  onOpenTrackInfo: () -> Unit,
  onOpenSleepTimerMenu: () -> Unit,
  onDismissSleepTimerMenu: () -> Unit,
  onSelectSleepTimer: (SleepTimerPreset) -> Unit,
  onNavigateToArtist: () -> Unit,
  onNavigateToAlbum: () -> Unit,
) {
  val queueSize = queue.size
  val artworkScale by animateFloatAsState(
    targetValue = if (isPlaying) 1.0f else 0.93f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "artworkScale",
  )

  val sheetState = rememberStandardBottomSheetState(
    initialValue = initialQueueSheetValue,
    skipHiddenState = true,
  )
  val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
  val coroutineScope = rememberCoroutineScope()

  val sheetPeekHeight = 32.dp
  val isQueueExpanded = sheetState.currentValue == SheetValue.Expanded ||
    sheetState.targetValue == SheetValue.Expanded

  BackHandler(enabled = isQueueExpanded) {
    coroutineScope.launch {
      sheetState.partialExpand()
    }
  }

  val topGradientColor = lerp(dominantColor, Color(0xFF12121A), 0.25f)
  val midGradientColor = lerp(secondaryColor, Color(0xFF0E0E16), 0.45f)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(topGradientColor, midGradientColor, deepBackgroundColor),
        )
      )
      .clickable(enabled = true, onClick = {}) // Consume touches behind overlay
      .testTag("music_player_expanded")
      .semantics {
        contentDescription = "Expanded player background $dominantHex accent $accentHex"
      },
  ) {
    BottomSheetScaffold(
      modifier = Modifier.fillMaxSize(),
      scaffoldState = scaffoldState,
      sheetPeekHeight = sheetPeekHeight,
      sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      sheetTonalElevation = 6.dp,
      sheetShadowElevation = 12.dp,
      sheetDragHandle = null,
      containerColor = Color.Transparent,
      sheetContent = {
        PlaybackQueueSheetContent(
          queue = queue,
          currentQueueIndex = currentQueueIndex,
          isPlaying = isPlaying,
          isExpanded = isQueueExpanded,
          peekHeight = sheetPeekHeight,
          onToggleSheet = {
            coroutineScope.launch {
              if (sheetState.currentValue == SheetValue.Expanded) {
                sheetState.partialExpand()
              } else {
                sheetState.expand()
              }
            }
          },
          onSelectQueueIndex = onSelectQueueIndex,
          onMoveQueueItem = onMoveQueueItem,
          onSaveQueueAsPlaylist = onSaveQueueAsPlaylist,
        )
      },
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(
              start = 20.dp,
              end = 20.dp,
              top = 12.dp,
              bottom = 12.dp + sheetPeekHeight,
            ),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          // 1. Top Header with Collapse Button, Now Playing Context, and Palette Swatch Pill
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = onCollapse,
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .testTag("music_player_collapse_button"),
            ) {
              Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Collapse player",
                tint = Color.White,
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.72f),
              )
              Text(
                text = "${track.album} (${currentQueueIndex + 1}/$queueSize)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }

            IconButton(
              onClick = onOpenTrackInfo,
              modifier = Modifier
                .size(40.dp)
                .testTag("expanded_player_info_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Track details",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp),
              )
            }
          }

          // 2. Large Album Art Card
          Box(
            modifier = Modifier
              .weight(1f, fill = false)
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
          ) {
            Card(
              shape = RoundedCornerShape(24.dp),
              elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
              border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f)),
              modifier = Modifier
                .size(220.dp)
                .scale(artworkScale)
                .testTag("expanded_player_album_art"),
            ) {
              TrackArtworkBanner(
                track = track,
                modifier = Modifier.fillMaxSize(),
              )
            }
          }

          // 3. Track Title, Artist, Album, Genre Badge & Favorite Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = track.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("expanded_player_title"),
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = track.artist,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = accentColor,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier
                    .clickable(onClick = onNavigateToArtist)
                    .testTag("expanded_player_artist"),
                )
                Text(
                  text = "•",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White.copy(alpha = 0.6f),
                )
                Text(
                  text = track.album,
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White.copy(alpha = 0.85f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier
                    .clickable(onClick = onNavigateToAlbum)
                    .testTag("expanded_player_album"),
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f),
              ) {
                Text(
                  text = "${track.genre} • ${track.year}",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.9f),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
            }

            IconButton(
              onClick = onToggleFavorite,
              modifier = Modifier
                .size(44.dp)
                .testTag("expanded_player_favorite_button"),
            ) {
              Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                tint = if (isFavorite) accentColor else Color.White,
                modifier = Modifier.size(26.dp),
              )
            }
          }

          // 4. Seek Slider (SlimDotSlider per DESIGN_LANGUAGE.md) + Elapsed / Total Time
          Column(modifier = Modifier.fillMaxWidth()) {
            MusicSlimDotSlider(
              value = seekFraction,
              onValueChange = onSeekChange,
              onValueChangeFinished = onSeekFinished,
              accentColor = accentColor,
              inactiveColor = Color.White.copy(alpha = 0.25f),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("expanded_player_seek_slider"),
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = AudioRecorderEngine.formatDurationMs(currentPositionMs),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.testTag("expanded_player_current_time"),
              )
              Text(
                text = AudioRecorderEngine.formatDurationMs(durationMs),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.testTag("expanded_player_total_time"),
              )
            }
          }

          // 5. Primary Transport Controls (Shuffle, Previous, Play/Pause, Next, Repeat)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = onToggleShuffle,
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isShuffleEnabled) accentColor.copy(alpha = 0.25f) else Color.Transparent)
                .testTag("expanded_player_shuffle_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = if (isShuffleEnabled) "Shuffle On" else "Shuffle Off",
                tint = if (isShuffleEnabled) accentColor else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
              )
            }

            IconButton(
              onClick = onPrevious,
              modifier = Modifier
                .size(44.dp)
                .testTag("expanded_player_prev_button"),
            ) {
              Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous track",
                tint = Color.White,
                modifier = Modifier.size(30.dp),
              )
            }

            FilledIconButton(
              onClick = onPlayPause,
              modifier = Modifier
                .size(64.dp)
                .testTag("expanded_player_play_pause_button"),
              shape = CircleShape,
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = accentColor,
                contentColor = if (accentColor.red * 0.299f + accentColor.green * 0.587f + accentColor.blue * 0.114f > 0.55f) {
                  Color(0xFF101018)
                } else {
                  Color.White
                },
              ),
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(34.dp),
              )
            }

            IconButton(
              onClick = onNext,
              modifier = Modifier
                .size(44.dp)
                .testTag("expanded_player_next_button"),
            ) {
              Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next track",
                tint = Color.White,
                modifier = Modifier.size(30.dp),
              )
            }

            IconButton(
              onClick = onCycleRepeat,
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (repeatMode != MusicRepeatMode.OFF) accentColor.copy(alpha = 0.25f) else Color.Transparent)
                .testTag("expanded_player_repeat_button"),
            ) {
              Icon(
                imageVector = if (repeatMode == MusicRepeatMode.REPEAT_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription = repeatMode.displayName,
                tint = if (repeatMode != MusicRepeatMode.OFF) accentColor else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
              )
            }
          }

          // 6. Secondary Utility Bar: Speed & Sleep Timer (Queue is a bottom slide-up panel right below)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = CircleShape,
              color = Color.White.copy(alpha = 0.14f),
              modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onCycleSpeed)
                .testTag("expanded_player_speed_button"),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Speed,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "${playbackSpeed}x",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White,
                )
              }
            }

            Box {
              Surface(
                shape = CircleShape,
                color = if (sleepTimerPreset != SleepTimerPreset.OFF) accentColor.copy(alpha = 0.28f)
                else Color.White.copy(alpha = 0.14f),
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable(onClick = onOpenSleepTimerMenu)
                  .testTag("expanded_player_sleep_button"),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                  )
                  Text(
                    text = if (sleepTimerPreset == SleepTimerPreset.OFF) {
                      "Sleep"
                    } else {
                      AudioRecorderEngine.formatDurationMs(sleepRemainingMs)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                  )
                }
              }

              DropdownMenu(
                expanded = showSleepTimerMenu,
                onDismissRequest = onDismissSleepTimerMenu,
              ) {
                SleepTimerPreset.entries.forEach { preset ->
                  DropdownMenuItem(
                    text = { Text(preset.label) },
                    leadingIcon = {
                      if (preset == sleepTimerPreset) {
                        Icon(Icons.Default.Check, contentDescription = null)
                      }
                    },
                    onClick = { onSelectSleepTimer(preset) },
                  )
                }
              }
            }
          }
        }

        // Scrim overlay behind queue sheet when expanded
        AnimatedVisibility(
          visible = isQueueExpanded,
          enter = fadeIn(tween(250)),
          exit = fadeOut(tween(200)),
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.5f))
              .pointerInput(Unit) {
                detectTapGestures {
                  coroutineScope.launch {
                    sheetState.partialExpand()
                  }
                }
              },
          )
        }
      }
    }
  }
}

/**
 * Slide-up Playback Queue Sheet Panel inside the Full Music Player,
 * styled similarly to `RollHistorySheetContent` in the Dice Roller tool.
 */
@Composable
private fun PlaybackQueueSheetContent(
  queue: List<MusicTrack>,
  currentQueueIndex: Int,
  isPlaying: Boolean,
  isExpanded: Boolean,
  peekHeight: Dp = 32.dp,
  onToggleSheet: () -> Unit,
  onSelectQueueIndex: (Int) -> Unit,
  onMoveQueueItem: (fromIndex: Int, toIndex: Int) -> Unit,
  onSaveQueueAsPlaylist: () -> Unit,
) {
  val arrowRotation by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
    label = "QueueChevronRotation",
  )

  val density = LocalDensity.current
  val defaultItemHeightPx = with(density) { 64.dp.toPx() }
  val itemSpacingPx = with(density) { 8.dp.toPx() }
  var draggedIndex by remember { mutableStateOf<Int?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  val itemHeightsPx = remember { mutableMapOf<Int, Float>() }
  val currentQueue by rememberUpdatedState(queue)
  val currentOnMoveQueueItem by rememberUpdatedState(onMoveQueueItem)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(0.60f)
      .testTag("music_queue_sheet"),
  ) {
    // Peek Header Bar (Drag or Tap to Pull Up Over the Player)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .height(peekHeight)
        .clickable(onClick = onToggleSheet)
        .testTag("music_queue_panel_toggle")
        .padding(horizontal = 20.dp, vertical = 3.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      // Drag handle pill
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier = Modifier
            .width(28.dp)
            .height(2.5.dp)
            .background(
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              shape = CircleShape,
            ),
        )
      }

      Spacer(modifier = Modifier.height(2.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("expanded_player_up_next_button"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp),
          )
          Text(
            text = "Queue",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
          if (queue.isNotEmpty()) {
            Badge(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
              Text(
                text = queue.size.toString(),
                modifier = Modifier.padding(horizontal = 4.dp),
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          if (!isExpanded && queue.isNotEmpty()) {
            val nextTrack = queue.getOrNull((currentQueueIndex + 1) % queue.size)
            val previewText = if (queue.size > 1 && nextTrack != null) {
              "Next: ${nextTrack.title}"
            } else {
              "Track ${currentQueueIndex + 1}/${queue.size}"
            }
            Text(
              text = previewText,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.widthIn(max = 150.dp),
            )
          }
          Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Toggle queue panel",
            modifier = Modifier
              .size(18.dp)
              .rotate(arrowRotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    // Top of Song Queue Action Bar: Save Queue as Playlist
    if (queue.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "${queue.size} songs in queue",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FilledTonalButton(
          onClick = onSaveQueueAsPlaylist,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("save_queue_as_playlist_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Save as Playlist",
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }

    // Expanded Queue Content
    if (queue.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "No songs in queue — select a track or album to start listening!",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
      }
    } else {
      LazyColumn(
        userScrollEnabled = draggedIndex == null,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("music_queue_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        itemsIndexed(queue, key = { idx, t -> "${idx}_${t.id}" }) { index, item ->
          val isCurrent = index == currentQueueIndex
          val isDragging = draggedIndex == index
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = if (isDragging) 8.dp else 0.dp,
            border = if (isCurrent || isDragging) {
              BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            } else {
              null
            },
            modifier = Modifier
              .fillMaxWidth()
              .zIndex(if (isDragging) 1f else 0f)
              .onSizeChanged { size ->
                if (size.height > 0) {
                  itemHeightsPx[index] = size.height.toFloat()
                }
              }
              .graphicsLayer {
                translationY = if (isDragging) dragOffsetY else 0f
                scaleX = if (isDragging) 1.02f else 1f
                scaleY = if (isDragging) 1.02f else 1f
              }
              .clip(RoundedCornerShape(14.dp))
              .clickable(enabled = draggedIndex == null) { onSelectQueueIndex(index) }
              .testTag("music_queue_item_$index"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .pointerInput(index, queue.size) {
                      detectDragGestures(
                        onDragStart = {
                          draggedIndex = index
                          dragOffsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                          change.consume()
                          val activeIdx = draggedIndex ?: return@detectDragGestures
                          var offset = dragOffsetY + dragAmount.y
                          var curIdx = activeIdx
                          val size = currentQueue.size
                          while (true) {
                            val stepPx = (itemHeightsPx[curIdx] ?: defaultItemHeightPx) + itemSpacingPx
                            val threshold = stepPx * 0.55f
                            if (offset > threshold && curIdx < size - 1) {
                              currentOnMoveQueueItem(curIdx, curIdx + 1)
                              curIdx += 1
                              draggedIndex = curIdx
                              offset -= stepPx
                            } else if (offset < -threshold && curIdx > 0) {
                              currentOnMoveQueueItem(curIdx, curIdx - 1)
                              curIdx -= 1
                              draggedIndex = curIdx
                              offset += stepPx
                            } else {
                              break
                            }
                          }
                          dragOffsetY = offset
                        },
                        onDragEnd = {
                          draggedIndex = null
                          dragOffsetY = 0f
                        },
                        onDragCancel = {
                          draggedIndex = null
                          dragOffsetY = 0f
                        },
                      )
                    }
                    .testTag("music_queue_drag_handle_$index"),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                  )
                }

                Box(contentAlignment = Alignment.Center) {
                  TrackArtworkThumbnail(
                    track = item,
                    size = 40.dp,
                    shape = RoundedCornerShape(10.dp),
                  )
                  if (isCurrent) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.42f)),
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                        imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Now Playing" else "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                      )
                    }
                  }
                }
                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Text(
                      text = "#${index + 1}",
                      style = MaterialTheme.typography.labelSmall,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold,
                      color = if (isCurrent) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      },
                    )
                    Text(
                      text = item.title,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.Bold,
                      color = if (isCurrent) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      },
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f, fill = false),
                    )
                  }
                  Text(
                    text = "${item.artist} • ${item.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = item.durationFormatted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = if (isCurrent) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
              )
            }
          }
        }
      }
    }
  }
}

/**
 * SlimDotSlider adhering to `DESIGN_LANGUAGE.md` Section 5.6:
 * `4.dp` slim track paired with a `16.dp` circular `CircleShape` dot drag handle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MusicSlimDotSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  onValueChangeFinished: (() -> Unit)? = null,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
  accentColor: Color = MaterialTheme.colorScheme.primary,
  inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
  val sliderColors = SliderDefaults.colors(
    thumbColor = accentColor,
    activeTrackColor = accentColor,
    inactiveTrackColor = inactiveColor,
  )
  Slider(
    value = value.coerceIn(valueRange.start, valueRange.endInclusive),
    onValueChange = onValueChange,
    onValueChangeFinished = onValueChangeFinished,
    valueRange = valueRange,
    colors = sliderColors,
    modifier = modifier.height(28.dp),
    thumb = {
      Box(
        modifier = Modifier
          .size(16.dp)
          .shadow(2.dp, CircleShape)
          .background(accentColor, CircleShape),
      )
    },
    track = { sliderState ->
      SliderDefaults.Track(
        sliderState = sliderState,
        modifier = Modifier.height(4.dp),
        colors = sliderColors,
      )
    },
  )
}

@Composable
internal fun TrackArtworkThumbnail(
  track: MusicTrack?,
  size: Dp,
  shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
  modifier: Modifier = Modifier,
) {
  val isInspection = LocalInspectionMode.current
  var bitmap by remember(track?.id, track?.album, track?.artist, track?.artworkPrimaryRgb) {
    mutableStateOf<Bitmap?>(
      if (track != null && !isInspection) MusicPlayerEngine.getCachedAlbumArtBitmap(track, 240)
      else null
    )
  }

  LaunchedEffect(track?.id, track?.album, track?.artist, isInspection) {
    if (track != null && !isInspection && bitmap == null) {
      bitmap = MusicPlayerEngine.loadAlbumArtBitmapAsync(track, sizePx = 240)
    }
  }

  val palette = remember(track?.id, track?.artworkPrimaryRgb, bitmap) {
    MusicPlayerEngine.resolveTrackPalette(track)
  }

  Box(
    modifier = modifier
      .size(size)
      .clip(shape)
      .background(
        Brush.linearGradient(
          colors = listOf(palette.dominantColor, palette.secondaryColor),
        )
      ),
    contentAlignment = Alignment.Center,
  ) {
    val activeBitmap = bitmap
    if (activeBitmap != null) {
      Image(
        bitmap = activeBitmap.asImageBitmap(),
        contentDescription = track?.album ?: "Album Art",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
    } else {
      Icon(
        imageVector = Icons.Default.MusicNote,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.9f),
        modifier = Modifier.size(size * 0.45f),
      )
    }
  }
}

@Composable
private fun TrackArtworkBanner(
  track: MusicTrack?,
  modifier: Modifier = Modifier,
) {
  val isInspection = LocalInspectionMode.current
  var bitmap by remember(track?.id, track?.album, track?.artist, track?.artworkPrimaryRgb) {
    mutableStateOf<Bitmap?>(
      if (track != null && !isInspection) MusicPlayerEngine.getCachedAlbumArtBitmap(track, 360)
      else null
    )
  }

  LaunchedEffect(track?.id, track?.album, track?.artist, isInspection) {
    if (track != null && !isInspection && bitmap == null) {
      bitmap = MusicPlayerEngine.loadAlbumArtBitmapAsync(track, sizePx = 360)
    }
  }

  val palette = remember(track?.id, track?.artworkPrimaryRgb, bitmap) {
    MusicPlayerEngine.resolveTrackPalette(track)
  }

  Box(
    modifier = modifier.background(
      Brush.linearGradient(
        colors = listOf(palette.dominantColor, palette.secondaryColor),
      )
    ),
    contentAlignment = Alignment.Center,
  ) {
    val activeBitmap = bitmap
    if (activeBitmap != null) {
      Image(
        bitmap = activeBitmap.asImageBitmap(),
        contentDescription = track?.album ?: "Album Art",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
    } else {
      Icon(
        imageVector = Icons.Default.Album,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.85f),
        modifier = Modifier.size(48.dp),
      )
    }
  }
}

@Composable
private fun PlaybackQueueDialog(
  queue: List<MusicTrack>,
  currentQueueIndex: Int,
  isPlaying: Boolean,
  accentColor: Color,
  onSelectQueueIndex: (Int) -> Unit,
  onMoveQueueItem: (fromIndex: Int, toIndex: Int) -> Unit,
  onSaveQueueAsPlaylist: () -> Unit,
  onDismiss: () -> Unit,
) {
  val density = LocalDensity.current
  val defaultItemHeightPx = with(density) { 56.dp.toPx() }
  val itemSpacingPx = with(density) { 6.dp.toPx() }
  var draggedIndex by remember { mutableStateOf<Int?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  val itemHeightsPx = remember { mutableMapOf<Int, Float>() }
  val currentQueue by rememberUpdatedState(queue)
  val currentOnMoveQueueItem by rememberUpdatedState(onMoveQueueItem)

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(lerp(MaterialTheme.colorScheme.primaryContainer, accentColor, 0.25f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Queue",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${queue.size} songs in queue",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (queue.isNotEmpty()) {
          FilledTonalButton(
            onClick = onSaveQueueAsPlaylist,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("save_queue_as_playlist_dialog_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save as Playlist", style = MaterialTheme.typography.labelLarge)
          }
        }

        LazyColumn(
          userScrollEnabled = draggedIndex == null,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .testTag("music_queue_list"),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          itemsIndexed(queue, key = { idx, t -> "${idx}_${t.id}" }) { index, item ->
            val isCurrent = index == currentQueueIndex
            val isDragging = draggedIndex == index
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isCurrent) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
              ),
              modifier = Modifier
                .fillMaxWidth()
                .zIndex(if (isDragging) 1f else 0f)
                .onSizeChanged { size ->
                  if (size.height > 0) {
                    itemHeightsPx[index] = size.height.toFloat()
                  }
                }
                .graphicsLayer {
                  translationY = if (isDragging) dragOffsetY else 0f
                  scaleX = if (isDragging) 1.02f else 1f
                  scaleY = if (isDragging) 1.02f else 1f
                }
                .clickable(enabled = draggedIndex == null) { onSelectQueueIndex(index) },
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .pointerInput(index, queue.size) {
                      detectDragGestures(
                        onDragStart = {
                          draggedIndex = index
                          dragOffsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                          change.consume()
                          val activeIdx = draggedIndex ?: return@detectDragGestures
                          var offset = dragOffsetY + dragAmount.y
                          var curIdx = activeIdx
                          val size = currentQueue.size
                          while (true) {
                            val stepPx = (itemHeightsPx[curIdx] ?: defaultItemHeightPx) + itemSpacingPx
                            val threshold = stepPx * 0.55f
                            if (offset > threshold && curIdx < size - 1) {
                              currentOnMoveQueueItem(curIdx, curIdx + 1)
                              curIdx += 1
                              draggedIndex = curIdx
                              offset -= stepPx
                            } else if (offset < -threshold && curIdx > 0) {
                              currentOnMoveQueueItem(curIdx, curIdx - 1)
                              curIdx -= 1
                              draggedIndex = curIdx
                              offset += stepPx
                            } else {
                              break
                            }
                          }
                          dragOffsetY = offset
                        },
                        onDragEnd = {
                          draggedIndex = null
                          dragOffsetY = 0f
                        },
                        onDragCancel = {
                          draggedIndex = null
                          dragOffsetY = 0f
                        },
                      )
                    }
                    .testTag("music_queue_dialog_drag_handle_$index"),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                  )
                }
                TrackArtworkThumbnail(
                  track = item,
                  size = 38.dp,
                  shape = RoundedCornerShape(10.dp),
                )
                Text(
                  text = "${index + 1}",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = "${item.artist} • ${item.durationFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                if (isCurrent) {
                  Icon(
                    imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Done")
      }
    },
  )
}

@Composable
private fun SaveQueueAsPlaylistDialog(
  defaultPlaylistName: String,
  queueTrackCount: Int,
  accentColor: Color,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var playlistName by rememberSaveable(defaultPlaylistName) {
    mutableStateOf(defaultPlaylistName)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("save_playlist_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(lerp(MaterialTheme.colorScheme.primaryContainer, accentColor, 0.25f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Save Queue as Playlist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "$queueTrackCount songs will be saved",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          value = playlistName,
          onValueChange = { playlistName = it },
          label = { Text("Playlist Name") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("playlist_name_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(playlistName) },
        shape = CircleShape,
        modifier = Modifier.testTag("confirm_save_playlist_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save")
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun TrackDetailsDialog(
  track: MusicTrack,
  palette: AlbumArtPalette,
  playStat: TrackPlayStats = TrackPlayStats(),
  onSelectColor: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val paletteEntries = listOf(
    palette.dominantColor to palette.dominantHex,
    palette.secondaryColor to palette.secondaryHex,
    palette.accentColor to palette.accentHex,
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("track_details_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Track Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Metadata, audio info, and colors",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            TrackArtworkThumbnail(
              track = track,
              size = 52.dp,
              shape = RoundedCornerShape(10.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "${track.artist} • ${track.album}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Interactive Colors row (links to Colors tool and selects the tapped color)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "Colors",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            paletteEntries.forEachIndexed { index, (swatchColor, hex) ->
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, swatchColor.copy(alpha = 0.55f)),
                modifier = Modifier
                  .weight(1f)
                  .clip(CircleShape)
                  .clickable { onSelectColor(hex) }
                  .testTag("track_details_color_$index"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                ) {
                  Box(
                    modifier = Modifier
                      .size(12.dp)
                      .clip(CircleShape)
                      .background(swatchColor)
                      .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = hex,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                  )
                }
              }
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        DetailRow(label = "Artist", value = track.artist)
        DetailRow(label = "Album", value = track.album)
        DetailRow(label = "Track #", value = track.trackNumberFormatted, isMono = true)
        DetailRow(label = "Genre", value = track.genre)
        DetailRow(label = "Year", value = track.year.toString(), isMono = true)
        DetailRow(label = "Duration", value = track.durationFormatted, isMono = true)
        DetailRow(label = "Plays", value = playStat.playCount.toString(), isMono = true)
        DetailRow(label = "Playtime", value = playStat.totalPlayTimeFormatted, isMono = true)
        DetailRow(label = "Format", value = track.formatLabel, isMono = true)
        DetailRow(label = "Bitrate", value = track.bitrateFormatted, isMono = true)
        DetailRow(label = "Sample Rate", value = track.sampleRateFormatted, isMono = true)
        DetailRow(label = "Channels", value = track.channelsLabel)
        DetailRow(label = "Size", value = track.sizeFormatted, isMono = true)
        DetailRow(label = "Source", value = track.sourceLabel, isMono = true)
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Close")
      }
    },
  )
}

@Composable
private fun DetailRow(
  label: String,
  value: String,
  isMono: Boolean = false,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      fontFamily = if (isMono) FontFamily.Monospace else null,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun EmptyMusicState(message: String) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        imageVector = Icons.Default.MusicNote,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(48.dp),
      )
      Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun MusicPlayerScreenPreview() {
  ToolboxTheme {
    MusicPlayerScreen()
  }
}

@Preview(showBackground = true, name = "Music Player - Full Player Expanded")
@Composable
private fun MusicPlayerExpandedPreview() {
  ToolboxTheme {
    MusicPlayerScreen(
      initialPlayerExpanded = true,
    )
  }
}

@Preview(showBackground = true, name = "Music Player - Full Player Queue Expanded")
@Composable
private fun MusicPlayerQueueExpandedPreview() {
  ToolboxTheme {
    MusicPlayerScreen(
      initialPlayerExpanded = true,
      initialQueueExpanded = true,
    )
  }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}
