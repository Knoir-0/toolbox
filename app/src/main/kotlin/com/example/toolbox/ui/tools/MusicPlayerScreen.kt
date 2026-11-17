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
import android.widget.Toast
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
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal data class MusicItemActionDispatcher(
  val onPlayTracks: (List<MusicTrack>) -> Unit = {},
  val onShuffleTracks: (List<MusicTrack>) -> Unit = {},
  val onPlayNext: (List<MusicTrack>) -> Unit = {},
  val onAddToQueue: (List<MusicTrack>) -> Unit = {},
  val onAddToPlaylist: (List<MusicTrack>) -> Unit = {},
  val onRemoveFromPlaylist: ((MusicTrack) -> Unit)? = null,
  val onGoToArtist: (MusicTrack) -> Unit = {},
  val onGoToAlbum: (MusicTrack) -> Unit = {},
  val onTrackDetails: (MusicTrack) -> Unit = {},
  val onEditAudio: (MusicTrack) -> Unit = {},
  val onConvertTrack: (MusicTrack) -> Unit = {},
  val onArchiveTracks: (List<MusicTrack>) -> Unit = {},
  val onSaveToNotes: (String, List<MusicTrack>) -> Unit = { _, _ -> },
  val onOpenInFiles: (MusicTrack) -> Unit = {},
  val onOpenWith: (MusicTrack) -> Unit = {},
  val onShareTracks: (List<MusicTrack>) -> Unit = {},
  val onDuplicateTrack: (MusicTrack) -> Unit = {},
  val onRenameTrack: (MusicTrack) -> Unit = {},
  val onDeleteTrack: (MusicTrack) -> Unit = {},
  val onRenameArtist: (MusicArtist) -> Unit = {},
  val onDeleteArtist: (MusicArtist) -> Unit = {},
  val onRenameAlbum: (MusicAlbum) -> Unit = {},
  val onDeleteAlbum: (MusicAlbum) -> Unit = {},
  val onRenameGenre: (MusicGenre) -> Unit = {},
  val onDeleteGenre: (MusicGenre) -> Unit = {},
  val onRenamePlaylist: (MusicPlaylist) -> Unit = {},
  val onDeletePlaylist: (MusicPlaylist) -> Unit = {},
)

private enum class BulkDeleteCollectionKind(val label: String) {
  ARTIST("Artist"),
  ALBUM("Album"),
  GENRE("Genre"),
}

private data class BulkDeleteMusicCollectionTarget(
  val kind: BulkDeleteCollectionKind,
  val name: String,
  val artistScope: String = "",
  val songCount: Int,
  val subtitle: String,
)

internal val LocalMusicItemActions = compositionLocalOf { MusicItemActionDispatcher() }

private val ArtworkOverlayButtonBackground = Color.Black.copy(alpha = 0.45f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
  modifier: Modifier = Modifier,
  initialFile: File? = null,
  initialDirectory: File? = null,
  initialTracks: List<MusicTrack>? = null,
  initialTab: MusicLibraryTab? = null,
  initialViewMode: MusicViewMode? = null,
  initialPlayerExpanded: Boolean = false,
  initialQueueExpanded: Boolean = false,
  onOpenColorPicker: ((String) -> Unit)? = null,
  onNavigateBack: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val activity = remember(context) { context.findActivity() }
  val effectiveInitialDir = initialDirectory ?: initialFile?.parentFile

  val session = remember(
    context,
    activity,
    effectiveInitialDir,
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
      initialDirectory = effectiveInitialDir,
      initialTracks = initialTracks,
      initialTab = initialTab,
      initialViewMode = initialViewMode,
      initialPlayerExpanded = initialPlayerExpanded,
      initialQueueExpanded = initialQueueExpanded,
      isInspection = isInspection,
    )
  }

  // Library Tab Order & Selected Tab: Artists, Albums, Songs, Genres, Playlists
  val tabOrder = session.tabOrder
  val selectedTab = session.selectedTab
  var sectionFocusVersion by remember { mutableIntStateOf(0) }

  fun updateTab(tab: MusicLibraryTab) {
    session.updateTab(tab)
    sectionFocusVersion++
  }

  fun moveSectionTab(fromIndex: Int, toIndex: Int) {
    if (session.reorderTabs(fromIndex, toIndex)) {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  // View Mode: List vs Card
  val viewMode = session.viewMode
  val cardsPerRow = session.cardsPerRow

  fun updateViewMode(mode: MusicViewMode) {
    session.updateViewMode(mode)
    sectionFocusVersion++
  }

  fun updateCardsPerRow(count: Int) {
    session.updateCardsPerRow(count)
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
  }

  // Sort Order
  val sortOrder = session.sortOrder

  fun updateSortOrder(order: MusicSortOrder) {
    session.updateSortOrder(order)
    sectionFocusVersion++
  }

  // Shuffle & Repeat
  val shuffleMode = session.shuffleMode
  val isShuffleEnabled = session.isShuffleEnabled

  fun cycleShuffleMode() {
    session.cycleShuffleMode()
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  val repeatMode = session.repeatMode

  fun cycleRepeatMode() {
    session.cycleRepeatMode()
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  // Favorites (Songs, Artists, Albums, and Genres — only song favoriteIds populate the default Favorites playlist)
  val favoriteIds = session.favoriteIds
  val favoriteArtists = session.favoriteArtists
  val favoriteAlbums = session.favoriteAlbums
  val favoriteGenres = session.favoriteGenres

  fun toggleFavorite(trackId: String) {
    session.toggleFavorite(trackId)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun toggleFavoriteArtist(artistName: String) {
    session.toggleFavoriteArtist(artistName)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun toggleFavoriteAlbum(album: MusicAlbum) {
    session.toggleFavoriteAlbum(album)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun toggleFavoriteGenre(genreName: String) {
    session.toggleFavoriteGenre(genreName)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  var showFavoriteArtistsOnly by session::showFavoriteArtistsOnly
  var showFavoriteAlbumsOnly by session::showFavoriteAlbumsOnly
  var showFavoriteSongsOnly by session::showFavoriteSongsOnly
  var showFavoriteGenresOnly by session::showFavoriteGenresOnly

  // Play Statistics (persisted in SharedPreferences: play count + total playtime per track)
  val playStats = session.playStats

  // Playlists (persisted in SharedPreferences; Favorites, Most Played & Recently Played are built-in dynamic playlists)
  val savedPlaylistEntries = session.savedPlaylistEntries

  fun saveQueueAsPlaylist(name: String, tracksToSave: List<MusicTrack>) {
    session.saveQueueAsPlaylist(name, tracksToSave)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun deletePlaylist(playlistId: String) {
    if (MusicPlayerEngine.isProtectedPlaylistId(playlistId)) return
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
    sectionFocusVersion++
  }

  val allTracks = session.allTracks

  val sortedTracks = remember(
    allTracks,
    sortOrder,
    playStats,
    showFavoriteSongsOnly,
    favoriteIds,
    favoriteArtists,
    favoriteAlbums,
    favoriteGenres,
    searchQuery,
    selectedTab,
    selectedArtistName,
    selectedAlbumKey,
    selectedGenreName,
    selectedPlaylistId,
  ) {
    val base = if (showFavoriteSongsOnly) {
      allTracks.filter { it.id in favoriteIds }
    } else {
      allTracks
    }
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.SONGS) searchQuery else ""
    val filtered = MusicPlayerEngine.filterTracks(base, activeQuery)
    MusicPlayerEngine.sortTracks(
      tracks = filtered,
      sortOrder = sortOrder,
      playStats = playStats,
      favoriteIds = favoriteIds,
      favoriteArtists = favoriteArtists,
      favoriteAlbums = favoriteAlbums,
      favoriteGenres = favoriteGenres,
    )
  }

  val baseArtists = remember(allTracks) {
    MusicPlayerEngine.buildArtists(allTracks)
  }
  val allArtists = remember(baseArtists, showFavoriteArtistsOnly, favoriteArtists, searchQuery, selectedTab, selectedArtistName, selectedAlbumKey, selectedGenreName, selectedPlaylistId) {
    val base = if (showFavoriteArtistsOnly) {
      baseArtists.filter { MusicPlayerEngine.isArtistFavorite(it, favoriteArtists) }
    } else {
      baseArtists
    }
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.ARTISTS) searchQuery else ""
    MusicPlayerEngine.filterArtists(base, activeQuery)
  }

  val baseAlbums = remember(allTracks) {
    MusicPlayerEngine.buildAlbums(allTracks)
  }
  val allAlbums = remember(baseAlbums, showFavoriteAlbumsOnly, favoriteAlbums, searchQuery, selectedTab, selectedArtistName, selectedAlbumKey, selectedGenreName, selectedPlaylistId) {
    val base = if (showFavoriteAlbumsOnly) {
      baseAlbums.filter { MusicPlayerEngine.isAlbumFavorite(it, favoriteAlbums) }
    } else {
      baseAlbums
    }
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.ALBUMS) searchQuery else ""
    MusicPlayerEngine.filterAlbums(base, activeQuery)
  }

  val baseGenres = remember(allTracks) {
    MusicPlayerEngine.buildGenres(allTracks)
  }
  val allGenres = remember(baseGenres, showFavoriteGenresOnly, favoriteGenres, searchQuery, selectedTab, selectedArtistName, selectedAlbumKey, selectedGenreName, selectedPlaylistId) {
    val base = if (showFavoriteGenresOnly) {
      baseGenres.filter { MusicPlayerEngine.isGenreFavorite(it, favoriteGenres) }
    } else {
      baseGenres
    }
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.GENRES) searchQuery else ""
    MusicPlayerEngine.filterGenres(base, activeQuery)
  }

  val basePlaylists = remember(
    savedPlaylistEntries,
    allTracks,
    favoriteIds,
    favoriteArtists,
    favoriteAlbums,
    favoriteGenres,
    playStats,
  ) {
    listOf(
      MusicPlayerEngine.buildFavoritesPlaylist(favoriteIds, allTracks),
      MusicPlayerEngine.buildMostPlayedPlaylist(
        playStats = playStats,
        allTracks = allTracks,
        favoriteIds = favoriteIds,
        favoriteArtists = favoriteArtists,
        favoriteAlbums = favoriteAlbums,
        favoriteGenres = favoriteGenres,
      ),
      MusicPlayerEngine.buildRecentlyPlayedPlaylist(playStats, allTracks),
    ) + MusicPlayerEngine.buildPlaylists(savedPlaylistEntries, allTracks)
  }
  val allPlaylists = remember(basePlaylists, searchQuery, selectedTab, selectedArtistName, selectedAlbumKey, selectedGenreName, selectedPlaylistId) {
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.PLAYLISTS) searchQuery else ""
    MusicPlayerEngine.filterPlaylists(basePlaylists, activeQuery)
  }

  val homeRefreshGeneration = session.homeRefreshGeneration
  val baseHomePlaylists = remember(
    allTracks,
    playStats,
    favoriteIds,
    favoriteArtists,
    favoriteAlbums,
    favoriteGenres,
    homeRefreshGeneration,
  ) {
    MusicPlayerEngine.generateHomePlaylists(
      allTracks = allTracks,
      playStats = playStats,
      favoriteIds = favoriteIds,
      favoriteArtists = favoriteArtists,
      favoriteAlbums = favoriteAlbums,
      favoriteGenres = favoriteGenres,
      refreshGeneration = homeRefreshGeneration,
    )
  }
  val allHomePlaylists = remember(
    baseHomePlaylists,
    searchQuery,
    selectedTab,
    selectedArtistName,
    selectedAlbumKey,
    selectedGenreName,
    selectedPlaylistId,
  ) {
    val hasDrill = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null
    val activeQuery = if (!hasDrill && selectedTab == MusicLibraryTab.HOME) searchQuery else ""
    MusicPlayerEngine.filterPlaylists(baseHomePlaylists, activeQuery)
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
  var trackForDetailsDialog by remember { mutableStateOf<MusicTrack?>(null) }
  var tracksForAddToPlaylistDialog by remember { mutableStateOf<List<MusicTrack>?>(null) }
  var trackForRenameDialog by remember { mutableStateOf<MusicTrack?>(null) }
  var trackForDeleteDialog by remember { mutableStateOf<MusicTrack?>(null) }
  var artistForRenameDialog by remember { mutableStateOf<MusicArtist?>(null) }
  var albumForRenameDialog by remember { mutableStateOf<MusicAlbum?>(null) }
  var genreForRenameDialog by remember { mutableStateOf<MusicGenre?>(null) }
  var bulkDeleteTarget by remember { mutableStateOf<BulkDeleteMusicCollectionTarget?>(null) }
  var playlistForRenameDialog by remember { mutableStateOf<MusicPlaylist?>(null) }
  var playlistForDeleteDialog by remember { mutableStateOf<MusicPlaylist?>(null) }
  var inlineColorPickerHex by rememberSaveable { mutableStateOf<String?>(null) }
  var showSortMenu by remember { mutableStateOf(false) }
  val crossToolHostState = rememberCrossToolFileHostState()

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

  fun playShuffledContext(
    contextTracks: List<MusicTrack>,
  ) {
    session.playShuffledContext(contextTracks)
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

  // If launched with a specific audio file from the Files tool, start playing it in its folder context
  LaunchedEffect(initialFile?.absolutePath) {
    val file = initialFile
    if (file != null && file.exists() && file.isFile) {
      val existing = session.allTracks.find { it.file?.absolutePath?.equals(file.absolutePath, ignoreCase = true) == true }
      val targetTrack = existing ?: MusicPlayerEngine.buildTrackFromFile(file, inspectMetadataRetriever = !isInspection)
      val contextList = if (existing != null) {
        session.allTracks
      } else {
        listOf(targetTrack) + session.allTracks
      }
      session.playTrackInContext(targetTrack, contextList)
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

  if (crossToolHostState.hasActiveSubScreen) {
    crossToolHostState.RenderSubScreen(
      modifier = modifier,
      defaultDirectory = effectiveInitialDir,
    )
    return
  }

  val musicItemActions = remember(session, context, selectedPlaylistId) {
    MusicItemActionDispatcher(
      onPlayTracks = { tracks ->
        if (tracks.isNotEmpty()) {
          playTrackInContext(tracks.first(), tracks)
        }
      },
      onShuffleTracks = { tracks ->
        if (tracks.isNotEmpty()) {
          playShuffledContext(tracks)
        }
      },
      onPlayNext = { tracks ->
        if (tracks.isNotEmpty()) {
          session.playNext(tracks)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          val label = if (tracks.size == 1) "\"${tracks.first().title}\"" else "${tracks.size} songs"
          Toast.makeText(context, "Playing $label next", Toast.LENGTH_SHORT).show()
        }
      },
      onAddToQueue = { tracks ->
        if (tracks.isNotEmpty()) {
          session.addToQueue(tracks)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          val label = if (tracks.size == 1) "\"${tracks.first().title}\"" else "${tracks.size} songs"
          Toast.makeText(context, "Added $label to queue", Toast.LENGTH_SHORT).show()
        }
      },
      onAddToPlaylist = { tracks ->
        if (tracks.isNotEmpty()) {
          tracksForAddToPlaylistDialog = tracks
        }
      },
      onRemoveFromPlaylist = selectedPlaylistId
        ?.takeIf { it !in MusicPlayerEngine.BUILT_IN_PLAYLIST_IDS }
        ?.let { playlistId ->
          { track: MusicTrack ->
            if (session.removeTrackFromPlaylist(playlistId, track.id)) {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              Toast.makeText(context, "Removed \"${track.title}\" from playlist", Toast.LENGTH_SHORT).show()
            }
          }
        },
      onGoToArtist = { track ->
        isPlayerExpanded = false
        session.navigateToArtist(track.artist)
        sectionFocusVersion++
      },
      onGoToAlbum = { track ->
        isPlayerExpanded = false
        session.navigateToAlbum("${track.album}|${track.artist}")
        sectionFocusVersion++
      },
      onTrackDetails = { track ->
        trackForDetailsDialog = track
      },
      onEditAudio = { track ->
        val file = session.resolveTrackFile(track, context)
        if (file != null && file.exists()) {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          crossToolHostState.fileForAudioEditor = file
        } else {
          Toast.makeText(context, "Audio file unavailable", Toast.LENGTH_SHORT).show()
        }
      },
      onConvertTrack = { track ->
        val file = session.resolveTrackFile(track, context)
        if (file != null && file.exists()) {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          crossToolHostState.fileForConverter = file
        } else {
          Toast.makeText(context, "Audio file unavailable", Toast.LENGTH_SHORT).show()
        }
      },
      onArchiveTracks = { tracks ->
        val files = session.resolveCollectionFiles(tracks, context)
        if (files.isNotEmpty()) {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          crossToolHostState.filesForArchiveCompression = files
        } else {
          Toast.makeText(context, "No audio files available to compress", Toast.LENGTH_SHORT).show()
        }
      },
      onSaveToNotes = { title, tracks ->
        if (tracks.isNotEmpty()) {
          val content = if (tracks.size == 1) {
            val t = tracks.first()
            buildString {
              appendLine("Title: ${t.title}")
              appendLine("Artist: ${t.artist}")
              appendLine("Album: ${t.album} (${t.year})")
              appendLine("Genre: ${t.genre}")
              appendLine("Duration: ${t.durationFormatted}")
              append("Format: ${t.formatLabel} • ${t.bitrateFormatted}")
            }
          } else {
            tracks.mapIndexed { idx, t ->
              "${idx + 1}. ${t.title} — ${t.artist} (${t.album}, ${t.durationFormatted})"
            }.joinToString("\n")
          }
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          CrossToolActions.saveTextToNotes(
            context = context,
            title = title,
            content = content,
            label = "Music",
          )
        }
      },
      onOpenInFiles = { track ->
        val file = session.resolveTrackFile(track, context)
        if (file != null) {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          crossToolHostState.openInFiles(file)
        }
      },
      onOpenWith = { track ->
        val file = session.resolveTrackFile(track, context)
        if (file != null && file.exists()) {
          FileManagerEngine.openFile(context, file, chooseApp = true)
        } else {
          Toast.makeText(context, "Audio file unavailable", Toast.LENGTH_SHORT).show()
        }
      },
      onShareTracks = { tracks ->
        val files = session.resolveCollectionFiles(tracks, context)
        if (files.isNotEmpty()) {
          CrossToolActions.shareMultipleFiles(context, files)
        } else if (tracks.isNotEmpty()) {
          val summary = tracks.joinToString("\n") { "${it.title} — ${it.artist} (${it.album})" }
          CrossToolActions.shareText(context, tracks.first().title, summary)
        }
      },
      onDuplicateTrack = { track ->
        val dup = session.duplicateTrack(track, context)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, "Duplicated \"${dup.title}\"", Toast.LENGTH_SHORT).show()
      },
      onRenameTrack = { track ->
        trackForRenameDialog = track
      },
      onDeleteTrack = { track ->
        trackForDeleteDialog = track
      },
      onRenameArtist = { artist ->
        artistForRenameDialog = artist
      },
      onDeleteArtist = { artist ->
        bulkDeleteTarget = BulkDeleteMusicCollectionTarget(
          kind = BulkDeleteCollectionKind.ARTIST,
          name = artist.name,
          songCount = artist.songCount,
          subtitle = "${artist.name} • ${artist.summaryText}",
        )
      },
      onRenameAlbum = { album ->
        albumForRenameDialog = album
      },
      onDeleteAlbum = { album ->
        bulkDeleteTarget = BulkDeleteMusicCollectionTarget(
          kind = BulkDeleteCollectionKind.ALBUM,
          name = album.name,
          artistScope = album.artist,
          songCount = album.songCount,
          subtitle = "${album.name} • ${album.summaryText}",
        )
      },
      onRenameGenre = { genre ->
        genreForRenameDialog = genre
      },
      onDeleteGenre = { genre ->
        bulkDeleteTarget = BulkDeleteMusicCollectionTarget(
          kind = BulkDeleteCollectionKind.GENRE,
          name = genre.name,
          songCount = genre.songCount,
          subtitle = "${genre.name} • ${genre.summaryText}",
        )
      },
      onRenamePlaylist = { playlist ->
        if (!playlist.isBuiltIn && !playlist.isGeneratedHomeMix && !MusicPlayerEngine.isProtectedPlaylistId(playlist.id)) {
          playlistForRenameDialog = playlist
        }
      },
      onDeletePlaylist = { playlist ->
        if (!playlist.isBuiltIn && !playlist.isGeneratedHomeMix && !MusicPlayerEngine.isProtectedPlaylistId(playlist.id)) {
          playlistForDeleteDialog = playlist
        }
      },
    )
  }

  BackHandler(
    enabled = isPlayerExpanded || showQueueSheet || showSavePlaylistDialog || isSearchActive || hasDrillDown || onNavigateBack != null
  ) {
    when {
      showSavePlaylistDialog -> showSavePlaylistDialog = false
      showQueueSheet -> showQueueSheet = false
      isPlayerExpanded -> {
        isPlayerExpanded = false
        sectionFocusVersion++
      }
      isSearchActive -> {
        isSearchActive = false
        searchQuery = ""
        sectionFocusVersion++
      }
      hasDrillDown -> clearDrillDown()
      onNavigateBack != null -> onNavigateBack()
    }
  }

  val baseBg = MaterialTheme.colorScheme.background
  val ambientTopColor = lerp(baseBg, animatedDominant, 0.22f)
  val ambientMidColor = lerp(baseBg, animatedSecondary, 0.10f)

  CompositionLocalProvider(LocalMusicItemActions provides musicItemActions) {
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
        (basePlaylists + baseHomePlaylists).find { it.id == id }?.let { playlist ->
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
        accentTint = animatedDominant,
        onBackClick = { clearDrillDown() },
        onNavigateBack = onNavigateBack,
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
      )

      // 2. Category Tabs (Artists, Albums, Songs, Genres, Playlists; reorderable on press and hold) + Pinned Floating Sort Button
      MusicCategoryAndViewBar(
        tabs = tabOrder,
        selectedTab = selectedTab,
        viewMode = viewMode,
        cardsPerRow = cardsPerRow,
        sortOrder = sortOrder,
        showSortMenu = showSortMenu,
        onSelectTab = { tab ->
          clearDrillDown()
          updateTab(tab)
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        onMoveTab = { fromIdx, toIdx ->
          moveSectionTab(fromIdx, toIdx)
        },
        onCardsPerRowChange = { updateCardsPerRow(it) },
        onOpenSortMenu = { showSortMenu = true },
        onDismissSortMenu = { showSortMenu = false },
        onSelectSortOrder = { order ->
          updateSortOrder(order)
          showSortMenu = false
        },
      )

      // 3. Main Content Area (Drill-down or Selected Tab in List/Card View)
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .pinchToChangeColumns(
            enabled = viewMode == MusicViewMode.CARD,
            columnsPerRow = cardsPerRow,
            minColumns = MIN_MUSIC_CARDS_PER_ROW,
            maxColumns = MAX_MUSIC_CARDS_PER_ROW,
            onColumnsChange = { updateCardsPerRow(it) },
          ),
      ) {
        when {
          activeAlbum != null -> {
            AlbumDetailContent(
              album = activeAlbum,
              viewMode = viewMode,
              cardsPerRow = cardsPerRow,
              currentTrackId = currentTrack?.id,
              isPlaying = isPlaying,
              favoriteIds = favoriteIds,
              isAlbumFavorite = MusicPlayerEngine.isAlbumFavorite(activeAlbum, favoriteAlbums),
              sectionFocusVersion = sectionFocusVersion,
              onPlayAlbum = { shuffle ->
                if (activeAlbum.tracks.isNotEmpty()) {
                  if (shuffle) {
                    playShuffledContext(activeAlbum.tracks)
                  } else {
                    playTrackInContext(activeAlbum.tracks.first(), activeAlbum.tracks)
                  }
                }
              },
              onSelectTrack = { track ->
                playTrackInContext(track, activeAlbum.tracks)
              },
              onToggleFavorite = { toggleFavorite(it.id) },
              onToggleFavoriteAlbum = { toggleFavoriteAlbum(activeAlbum) },
            )
          }

          activeArtist != null -> {
            ArtistDetailContent(
              artist = activeArtist,
              viewMode = viewMode,
              cardsPerRow = cardsPerRow,
              currentTrack = currentTrack,
              currentTrackId = currentTrack?.id,
              isPlaying = isPlaying,
              favoriteIds = favoriteIds,
              isArtistFavorite = MusicPlayerEngine.isArtistFavorite(activeArtist, favoriteArtists),
              sectionFocusVersion = sectionFocusVersion,
              onPlayArtist = { shuffle ->
                if (activeArtist.tracks.isNotEmpty()) {
                  if (shuffle) {
                    playShuffledContext(activeArtist.tracks)
                  } else {
                    playTrackInContext(activeArtist.tracks.first(), activeArtist.tracks)
                  }
                }
              },
              onOpenAlbum = { album ->
                selectedAlbumKey = "${album.name}|${album.artist}"
                sectionFocusVersion++
              },
              onSelectTrack = { track ->
                playTrackInContext(track, activeArtist.tracks)
              },
              onToggleFavorite = { toggleFavorite(it.id) },
              onToggleFavoriteArtist = { toggleFavoriteArtist(activeArtist.name) },
            )
          }

          activeGenre != null -> {
            GenreDetailContent(
              genre = activeGenre,
              viewMode = viewMode,
              cardsPerRow = cardsPerRow,
              currentTrackId = currentTrack?.id,
              isPlaying = isPlaying,
              favoriteIds = favoriteIds,
              isGenreFavorite = MusicPlayerEngine.isGenreFavorite(activeGenre, favoriteGenres),
              sectionFocusVersion = sectionFocusVersion,
              onPlayGenre = { shuffle ->
                if (activeGenre.tracks.isNotEmpty()) {
                  if (shuffle) {
                    playShuffledContext(activeGenre.tracks)
                  } else {
                    playTrackInContext(activeGenre.tracks.first(), activeGenre.tracks)
                  }
                }
              },
              onSelectTrack = { track ->
                playTrackInContext(track, activeGenre.tracks)
              },
              onToggleFavorite = { toggleFavorite(it.id) },
              onToggleFavoriteGenre = { toggleFavoriteGenre(activeGenre.name) },
            )
          }

          activePlaylist != null -> {
            PlaylistDetailContent(
              playlist = activePlaylist,
              viewMode = viewMode,
              cardsPerRow = cardsPerRow,
              currentTrackId = currentTrack?.id,
              isPlaying = isPlaying,
              favoriteIds = favoriteIds,
              playStats = playStats,
              sectionFocusVersion = sectionFocusVersion,
              onPlayPlaylist = { shuffle ->
                if (activePlaylist.tracks.isNotEmpty()) {
                  if (shuffle) {
                    playShuffledContext(activePlaylist.tracks)
                  } else {
                    playTrackInContext(activePlaylist.tracks.first(), activePlaylist.tracks)
                  }
                }
              },
              onDeletePlaylist = {
                if (!activePlaylist.isBuiltIn && !activePlaylist.isGeneratedHomeMix && !MusicPlayerEngine.isProtectedPlaylistId(activePlaylist.id)) {
                  playlistForDeleteDialog = activePlaylist
                }
              },
              onSelectTrack = { track ->
                playTrackInContext(track, activePlaylist.tracks)
              },
              onToggleFavorite = { toggleFavorite(it.id) },
            )
          }

          else -> {
            when (selectedTab) {
              MusicLibraryTab.HOME -> {
                HomeTabContent(
                  playlists = allHomePlaylists,
                  viewMode = viewMode,
                  cardsPerRow = cardsPerRow,
                  currentTrackId = currentTrack?.id,
                  isPlaying = isPlaying,
                  refreshGeneration = homeRefreshGeneration,
                  onRefresh = {
                    session.refreshHomePlaylists()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  },
                  onPlayAll = {
                    val tracks = allHomePlaylists.flatMap { it.tracks }.distinctBy { it.id }.ifEmpty { sortedTracks }
                    if (tracks.isNotEmpty()) {
                      playTrackInContext(tracks.first(), tracks)
                    }
                  },
                  onShuffleAll = {
                    val nonEmpty = allHomePlaylists.filter { it.tracks.isNotEmpty() }
                    if (nonEmpty.isNotEmpty()) {
                      val randomPlaylist = nonEmpty.random()
                      playShuffledContext(randomPlaylist.tracks)
                    } else if (sortedTracks.isNotEmpty()) {
                      playShuffledContext(sortedTracks)
                    }
                  },
                  onSelectPlaylist = { playlist ->
                    selectedPlaylistId = playlist.id
                    sectionFocusVersion++
                  },
                  onPlayPlaylist = { playlist ->
                    if (playlist.tracks.isNotEmpty()) {
                      playTrackInContext(playlist.tracks.first(), playlist.tracks)
                    }
                  },
                )
              }

              MusicLibraryTab.ARTISTS -> {
                ArtistsTabContent(
                  artists = allArtists,
                  viewMode = viewMode,
                  cardsPerRow = cardsPerRow,
                  currentTrack = currentTrack,
                  currentArtistName = currentTrack?.artist,
                  isPlaying = isPlaying,
                  favoriteArtists = favoriteArtists,
                  showFavoritesOnly = showFavoriteArtistsOnly,
                  sectionFocusVersion = sectionFocusVersion,
                  onToggleFavoritesOnly = {
                    showFavoriteArtistsOnly = !showFavoriteArtistsOnly
                    sectionFocusVersion++
                  },
                  onPlayAll = {
                    val tracks = allArtists.flatMap { it.tracks }.ifEmpty {
                      if (showFavoriteArtistsOnly) emptyList() else sortedTracks
                    }
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
                    sectionFocusVersion++
                  },
                  onPlayArtist = { artist ->
                    if (artist.tracks.isNotEmpty()) {
                      playTrackInContext(artist.tracks.first(), artist.tracks)
                    }
                  },
                  onToggleFavoriteArtist = { artist ->
                    toggleFavoriteArtist(artist.name)
                  },
                )
              }

              MusicLibraryTab.ALBUMS -> {
                AlbumsTabContent(
                  albums = allAlbums,
                  viewMode = viewMode,
                  cardsPerRow = cardsPerRow,
                  currentTrack = currentTrack,
                  currentAlbumName = currentTrack?.album,
                  isPlaying = isPlaying,
                  favoriteAlbums = favoriteAlbums,
                  showFavoritesOnly = showFavoriteAlbumsOnly,
                  sectionFocusVersion = sectionFocusVersion,
                  onToggleFavoritesOnly = {
                    showFavoriteAlbumsOnly = !showFavoriteAlbumsOnly
                    sectionFocusVersion++
                  },
                  onPlayAll = {
                    val tracks = allAlbums.flatMap { it.tracks }.ifEmpty {
                      if (showFavoriteAlbumsOnly) emptyList() else sortedTracks
                    }
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
                    sectionFocusVersion++
                  },
                  onPlayAlbum = { album ->
                    if (album.tracks.isNotEmpty()) {
                      playTrackInContext(album.tracks.first(), album.tracks)
                    }
                  },
                  onToggleFavoriteAlbum = { album ->
                    toggleFavoriteAlbum(album)
                  },
                )
              }

              MusicLibraryTab.SONGS -> {
                SongsTabContent(
                  tracks = sortedTracks,
                  viewMode = viewMode,
                  cardsPerRow = cardsPerRow,
                  currentTrackId = currentTrack?.id,
                  isPlaying = isPlaying,
                  favoriteIds = favoriteIds,
                  showFavoritesOnly = showFavoriteSongsOnly,
                  sectionFocusVersion = sectionFocusVersion,
                  onToggleFavoritesOnly = {
                    showFavoriteSongsOnly = !showFavoriteSongsOnly
                    sectionFocusVersion++
                  },
                  onPlayAll = {
                    if (sortedTracks.isNotEmpty()) {
                      playTrackInContext(sortedTracks.first(), sortedTracks)
                    }
                  },
                  onShuffleAll = {
                    if (sortedTracks.isNotEmpty()) {
                      playShuffledContext(sortedTracks)
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
                  cardsPerRow = cardsPerRow,
                  currentTrack = currentTrack,
                  currentGenreName = currentTrack?.genre,
                  isPlaying = isPlaying,
                  favoriteGenres = favoriteGenres,
                  showFavoritesOnly = showFavoriteGenresOnly,
                  sectionFocusVersion = sectionFocusVersion,
                  onToggleFavoritesOnly = {
                    showFavoriteGenresOnly = !showFavoriteGenresOnly
                    sectionFocusVersion++
                  },
                  onPlayAll = {
                    val tracks = allGenres.flatMap { it.tracks }.ifEmpty {
                      if (showFavoriteGenresOnly) emptyList() else sortedTracks
                    }
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
                    sectionFocusVersion++
                  },
                  onPlayGenre = { genre ->
                    if (genre.tracks.isNotEmpty()) {
                      playTrackInContext(genre.tracks.first(), genre.tracks)
                    }
                  },
                  onToggleFavoriteGenre = { genre ->
                    toggleFavoriteGenre(genre.name)
                  },
                )
              }

              MusicLibraryTab.PLAYLISTS -> {
                PlaylistsTabContent(
                  playlists = allPlaylists,
                  viewMode = viewMode,
                  cardsPerRow = cardsPerRow,
                  currentTrackId = currentTrack?.id,
                  isPlaying = isPlaying,
                  sectionFocusVersion = sectionFocusVersion,
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
                    sectionFocusVersion++
                  },
                  onPlayPlaylist = { playlist ->
                    if (playlist.tracks.isNotEmpty()) {
                      playTrackInContext(playlist.tracks.first(), playlist.tracks)
                    }
                  },
                  onDeletePlaylist = { playlist ->
                    if (!playlist.isBuiltIn && !playlist.isGeneratedHomeMix && !MusicPlayerEngine.isProtectedPlaylistId(playlist.id)) {
                      playlistForDeleteDialog = playlist
                    }
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
          shuffleMode = shuffleMode,
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
          onCollapse = {
            isPlayerExpanded = false
            sectionFocusVersion++
          },
          onToggleFavorite = { toggleFavorite(currentTrack.id) },
          onSeekChange = { fraction ->
            session.updateSeekDrag(fraction)
          },
          onSeekFinished = {
            session.finishSeekDrag()
          },
          onToggleShuffle = { cycleShuffleMode() },
          onPrevious = { skipToPrevious() },
          onPlayPause = {
            session.togglePlayPause()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          },
          onNext = { skipToNext(userInitiated = true) },
          onCycleRepeat = { cycleRepeatMode() },
          onCycleSpeed = {
            session.cyclePlaybackSpeed()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          },
          onCycleSleepTimer = {
            session.cycleSleepTimer()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
          onNavigateToArtist = {
            isPlayerExpanded = false
            clearDrillDown()
            updateTab(MusicLibraryTab.ARTISTS)
            selectedArtistName = currentTrack.artist
            sectionFocusVersion++
          },
          onNavigateToAlbum = {
            isPlayerExpanded = false
            clearDrillDown()
            updateTab(MusicLibraryTab.ALBUMS)
            selectedAlbumKey = "${currentTrack.album}|${currentTrack.artist}"
            sectionFocusVersion++
          },
        )
      }
    }

    // 6. Queue Dialog (for Library View when Full Player is collapsed)
    if (showQueueSheet && !isPlayerExpanded) {
      PlaybackQueueDialog(
        queue = playbackQueue,
        currentQueueIndex = currentQueueIndex,
        currentTrackId = currentTrack?.id,
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
    val detailTrack = trackForDetailsDialog ?: if (showTrackInfoDialog) currentTrack else null
    if (detailTrack != null) {
      val detailPalette = remember(detailTrack.id, activePalette, currentTrack?.id) {
        if (detailTrack.id == currentTrack?.id) activePalette
        else MusicPlayerEngine.resolveTrackPalette(detailTrack)
      }
      TrackDetailsDialog(
        track = detailTrack,
        palette = detailPalette,
        playStat = playStats[detailTrack.id] ?: TrackPlayStats(),
        onSelectColor = { hex ->
          showTrackInfoDialog = false
          trackForDetailsDialog = null
          openColorInColorsTool(hex)
        },
        onDismiss = {
          showTrackInfoDialog = false
          trackForDetailsDialog = null
        },
      )
    }

    // 9. Add to Playlist Dialog
    tracksForAddToPlaylistDialog?.let { tracksToAdd ->
      AddToPlaylistDialog(
        tracksToAdd = tracksToAdd,
        savedPlaylists = savedPlaylistEntries,
        accentColor = animatedDominant,
        onAddToExistingPlaylist = { playlistId, playlistName ->
          session.addTracksToPlaylist(playlistId, tracksToAdd)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          Toast.makeText(context, "Added to \"$playlistName\"", Toast.LENGTH_SHORT).show()
          tracksForAddToPlaylistDialog = null
        },
        onCreateNewPlaylist = { newName ->
          saveQueueAsPlaylist(newName, tracksToAdd)
          Toast.makeText(context, "Created playlist \"$newName\"", Toast.LENGTH_SHORT).show()
          tracksForAddToPlaylistDialog = null
        },
        onDismiss = { tracksForAddToPlaylistDialog = null },
      )
    }

    // 10. Rename Track Dialog
    trackForRenameDialog?.let { targetTrack ->
      RenameMusicItemDialog(
        title = "Rename Song",
        subtitle = "${targetTrack.artist} • ${targetTrack.album}",
        initialValue = targetTrack.title,
        label = "Song Title",
        onConfirm = { newName ->
          session.renameTrack(targetTrack, newName, context)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          trackForRenameDialog = null
        },
        onDismiss = { trackForRenameDialog = null },
      )
    }

    // 11. Delete Track Confirmation Dialog
    trackForDeleteDialog?.let { targetTrack ->
      DeleteTrackConfirmDialog(
        track = targetTrack,
        onConfirm = {
          session.deleteTrack(targetTrack)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          trackForDeleteDialog = null
        },
        onDismiss = { trackForDeleteDialog = null },
      )
    }

    // 12. Rename Playlist Dialog
    playlistForRenameDialog?.let { targetPlaylist ->
      RenameMusicItemDialog(
        title = "Rename Playlist",
        subtitle = targetPlaylist.summaryText,
        initialValue = targetPlaylist.name,
        label = "Playlist Name",
        onConfirm = { newName ->
          session.renamePlaylist(targetPlaylist.id, newName)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          playlistForRenameDialog = null
        },
        onDismiss = { playlistForRenameDialog = null },
      )
    }

    // 13. Rename Artist Dialog
    artistForRenameDialog?.let { targetArtist ->
      RenameMusicItemDialog(
        title = "Rename Artist",
        subtitle = targetArtist.summaryText,
        initialValue = targetArtist.name,
        label = "Artist Name",
        onConfirm = { newName ->
          session.renameArtist(targetArtist.name, newName)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          artistForRenameDialog = null
        },
        onDismiss = { artistForRenameDialog = null },
      )
    }

    // 14. Rename Album Dialog
    albumForRenameDialog?.let { targetAlbum ->
      RenameMusicItemDialog(
        title = "Rename Album",
        subtitle = targetAlbum.summaryText,
        initialValue = targetAlbum.name,
        label = "Album Name",
        onConfirm = { newName ->
          session.renameAlbum(targetAlbum.name, targetAlbum.artist, newName)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          albumForRenameDialog = null
        },
        onDismiss = { albumForRenameDialog = null },
      )
    }

    // 15. Rename Genre Dialog
    genreForRenameDialog?.let { targetGenre ->
      RenameMusicItemDialog(
        title = "Rename Genre",
        subtitle = targetGenre.summaryText,
        initialValue = targetGenre.name,
        label = "Genre Name",
        onConfirm = { newName ->
          session.renameGenre(targetGenre.name, newName)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          genreForRenameDialog = null
        },
        onDismiss = { genreForRenameDialog = null },
      )
    }

    // 16. Bulk Delete Artist / Album / Genre Confirmation Dialog (requires typing the name)
    bulkDeleteTarget?.let { target ->
      BulkDeleteMusicCollectionConfirmDialog(
        target = target,
        onConfirm = {
          when (target.kind) {
            BulkDeleteCollectionKind.ARTIST -> session.deleteArtist(target.name)
            BulkDeleteCollectionKind.ALBUM -> session.deleteAlbum(target.name, target.artistScope)
            BulkDeleteCollectionKind.GENRE -> session.deleteGenre(target.name)
          }
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          bulkDeleteTarget = null
        },
        onDismiss = { bulkDeleteTarget = null },
      )
    }

    // 17. Delete User-Created Playlist Confirmation Dialog
    playlistForDeleteDialog?.let { targetPlaylist ->
      DeletePlaylistConfirmDialog(
        playlist = targetPlaylist,
        onConfirm = {
          deletePlaylist(targetPlaylist.id)
          if (selectedPlaylistId == targetPlaylist.id) {
            selectedPlaylistId = null
            sectionFocusVersion++
          }
          playlistForDeleteDialog = null
        },
        onDismiss = { playlistForDeleteDialog = null },
      )
    }
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
  accentTint: Color,
  onBackClick: () -> Unit,
  onNavigateBack: (() -> Unit)? = null,
  onSearchToggle: () -> Unit,
  onCloseSearch: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onToggleViewMode: () -> Unit,
  onSelectViewMode: (MusicViewMode) -> Unit,
) {
  val focusManager = LocalFocusManager.current
  val topBarColor = lerp(MaterialTheme.colorScheme.surface, accentTint, 0.12f)

  ToolboxTopBar(
    title = title,
    subtext = subtext,
    icon = Icons.Default.Headphones,
    containerColor = topBarColor,
    onNavigateBack = if (hasDrillDown) onBackClick else onNavigateBack,
    backContentDescription = "Back",
    backButtonTestTag = if (hasDrillDown) "music_back_button" else "action_navigate_back",
    searchState = ToolboxTopBarSearchState(
      isActive = isSearchActive,
      query = searchQuery,
      onQueryChange = onSearchQueryChange,
      onOpenSearch = onSearchToggle,
      onCloseSearch = {
        focusManager.clearFocus()
        onCloseSearch()
      },
      placeholder = searchPlaceholder,
      searchContentDescription = "Search music",
      closeContentDescription = "Close search",
      clearContentDescription = "Clear search",
      toggleButtonTestTag = "music_search_toggle",
      closeButtonTestTag = "music_search_toggle",
      inputTestTag = "music_search_input",
      clearButtonTestTag = "music_search_clear",
      onSearchImeAction = { focusManager.clearFocus() },
    ),
    actions = {
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
    },
  )
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

private fun MusicLibraryTab.iconVector() = when (this) {
  MusicLibraryTab.HOME -> Icons.Default.Home
  MusicLibraryTab.ARTISTS -> Icons.Default.Person
  MusicLibraryTab.ALBUMS -> Icons.Default.Album
  MusicLibraryTab.SONGS -> Icons.Default.MusicNote
  MusicLibraryTab.GENRES -> Icons.Default.Category
  MusicLibraryTab.PLAYLISTS -> Icons.AutoMirrored.Filled.PlaylistPlay
}

@Composable
private fun MusicCategoryAndViewBar(
  tabs: List<MusicLibraryTab> = MusicLibraryTab.entries.toList(),
  selectedTab: MusicLibraryTab,
  viewMode: MusicViewMode = MusicViewMode.LIST,
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  sortOrder: MusicSortOrder = MusicSortOrder.TITLE_ASC,
  showSortMenu: Boolean = false,
  onSelectTab: (MusicLibraryTab) -> Unit,
  onMoveTab: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
  onCardsPerRowChange: (Int) -> Unit = {},
  onOpenSortMenu: () -> Unit = {},
  onDismissSortMenu: () -> Unit = {},
  onSelectSortOrder: (MusicSortOrder) -> Unit = {},
) {
  val density = LocalDensity.current
  val haptic = LocalHapticFeedback.current
  val scrollState = rememberScrollState()

  val currentTabs by rememberUpdatedState(tabs)
  val currentOnMoveTab by rememberUpdatedState(onMoveTab)
  val tabBounds = remember { mutableMapOf<MusicLibraryTab, Rect>() }
  var barCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

  var draggedTab by remember { mutableStateOf<MusicLibraryTab?>(null) }
  var dragPointerOffset by remember { mutableStateOf(Offset.Zero) }
  var grabOffsetFromCenter by remember { mutableStateOf(Offset.Zero) }
  var draggedTabSize by remember { mutableStateOf<IntSize?>(null) }

  val maxHitSlopPx = with(density) { 24.dp.toPx() }
  val leftEdgeScrollZonePx = with(density) { 36.dp.toPx() }
  val rightEdgeScrollZonePx = with(density) { (if (viewMode == MusicViewMode.CARD) 112.dp else 72.dp).toPx() }
  val sortButtonExcludeZonePx = with(density) { (if (viewMode == MusicViewMode.CARD) 92.dp else 52.dp).toPx() }
  val edgeScrollStepPx = with(density) { 10.dp.toPx() }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("music_category_bar")
      .onGloballyPositioned { coords ->
        barCoordinates = coords
      }
      .pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
          onDragStart = { startOffset ->
            val barWidth = (barCoordinates?.size?.width ?: 0).toFloat()
            if (barWidth > 0f && startOffset.x >= barWidth - sortButtonExcludeZonePx) {
              return@detectDragGesturesAfterLongPress
            }
            val directHit = currentTabs.firstOrNull { tab ->
              tabBounds[tab]?.contains(startOffset) == true
            }
            val hitTab = directHit ?: currentTabs.minByOrNull { tab ->
              val rect = tabBounds[tab] ?: return@minByOrNull Float.MAX_VALUE
              val dx = when {
                startOffset.x < rect.left -> rect.left - startOffset.x
                startOffset.x > rect.right -> startOffset.x - rect.right
                else -> 0f
              }
              val dy = when {
                startOffset.y < rect.top -> rect.top - startOffset.y
                startOffset.y > rect.bottom -> startOffset.y - rect.bottom
                else -> 0f
              }
              dx + dy
            }?.takeIf { tab ->
              val rect = tabBounds[tab] ?: return@takeIf false
              val dx = when {
                startOffset.x < rect.left -> rect.left - startOffset.x
                startOffset.x > rect.right -> startOffset.x - rect.right
                else -> 0f
              }
              dx <= maxHitSlopPx
            }

            if (hitTab != null) {
              val rect = tabBounds[hitTab]
              draggedTab = hitTab
              dragPointerOffset = startOffset
              grabOffsetFromCenter = if (rect != null) {
                startOffset - rect.center
              } else {
                Offset.Zero
              }
              draggedTabSize = rect?.let { IntSize(it.width.roundToInt(), it.height.roundToInt()) }
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
          },
          onDrag = { change, dragAmount ->
            val activeTab = draggedTab ?: return@detectDragGesturesAfterLongPress
            change.consume()
            dragPointerOffset += dragAmount
            val desiredCenter = dragPointerOffset - grabOffsetFromCenter

            val barWidth = (barCoordinates?.size?.width ?: 0).toFloat()
            if (barWidth > 0f && scrollState.maxValue > 0) {
              if (dragPointerOffset.x < leftEdgeScrollZonePx && scrollState.value > 0) {
                scrollState.dispatchRawDelta(-edgeScrollStepPx)
              } else if (dragPointerOffset.x > barWidth - rightEdgeScrollZonePx && scrollState.value < scrollState.maxValue) {
                scrollState.dispatchRawDelta(edgeScrollStepPx)
              }
            }

            val fromIndex = currentTabs.indexOf(activeTab)
            if (fromIndex >= 0 && currentTabs.size > 1) {
              val targetIndex = currentTabs.indices.minByOrNull { idx ->
                val rect = tabBounds[currentTabs[idx]]
                if (rect != null) abs(desiredCenter.x - rect.center.x) else Float.MAX_VALUE
              }
              if (targetIndex != null && targetIndex != fromIndex) {
                currentOnMoveTab(fromIndex, targetIndex)
              }
            }
          },
          onDragEnd = {
            draggedTab = null
            dragPointerOffset = Offset.Zero
            grabOffsetFromCenter = Offset.Zero
            draggedTabSize = null
          },
          onDragCancel = {
            draggedTab = null
            dragPointerOffset = Offset.Zero
            grabOffsetFromCenter = Offset.Zero
            draggedTabSize = null
          },
        )
      },
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .fadingPillRowUnderFloatingControls(hasColumnsButton = viewMode == MusicViewMode.CARD)
        .horizontalScroll(scrollState, enabled = draggedTab == null)
        .padding(start = 12.dp, end = if (viewMode == MusicViewMode.CARD) 96.dp else 56.dp, top = 8.dp, bottom = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      tabs.forEach { tab ->
        val isSelected = tab == selectedTab
        val isBeingDragged = tab == draggedTab
        FilterChip(
          selected = isSelected,
          onClick = {
            if (draggedTab == null) {
              onSelectTab(tab)
            }
          },
          label = {
            Text(
              text = tab.displayName,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
          },
          leadingIcon = {
            Icon(
              imageVector = tab.iconVector(),
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          },
          shape = CircleShape,
          modifier = Modifier
            .onGloballyPositioned { coords ->
              val barCoords = barCoordinates
              if (barCoords != null && barCoords.isAttached && coords.isAttached) {
                tabBounds[tab] = barCoords.localBoundingBoxOf(coords, clipBounds = false)
              }
            }
            .graphicsLayer {
              alpha = if (isBeingDragged) 0.3f else 1f
            }
            .testTag("music_tab_${tab.name}"),
        )
      }
    }

    // Pinned floating [# per row] + [Sort] buttons on the right edge of the pill bar
    Row(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 12.dp)
        .zIndex(10f),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (viewMode == MusicViewMode.CARD) {
        ToolboxFloatingColumnsButton(
          columnsPerRow = cardsPerRow,
          minColumns = MIN_MUSIC_CARDS_PER_ROW,
          maxColumns = MAX_MUSIC_CARDS_PER_ROW,
          onColumnsChange = onCardsPerRowChange,
          buttonTestTag = "music_columns_per_row_button",
          countTestTag = "music_columns_per_row_count",
          contentDescription = "$cardsPerRow cards per row",
        )
      }

      Box {
        Surface(
          onClick = onOpenSortMenu,
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceContainerHigh,
          tonalElevation = 3.dp,
          shadowElevation = 4.dp,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
          modifier = Modifier
            .size(34.dp)
            .testTag("music_sort_button"),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Sort,
              contentDescription = "Sort tracks",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
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

    val activeDraggedTab = draggedTab
    if (activeDraggedTab != null && barCoordinates != null) {
      val desiredCenter = dragPointerOffset - grabOffsetFromCenter
      val fallbackWidthPx = with(density) { 96.dp.toPx() }
      val fallbackHeightPx = with(density) { 32.dp.toPx() }
      val widthPx = draggedTabSize?.width?.toFloat() ?: fallbackWidthPx
      val heightPx = draggedTabSize?.height?.toFloat() ?: fallbackHeightPx
      val isSelected = activeDraggedTab == selectedTab

      Box(
        modifier = Modifier
          .graphicsLayer {
            translationX = desiredCenter.x - widthPx / 2f
            translationY = desiredCenter.y - heightPx / 2f
            scaleX = 1.06f
            scaleY = 1.06f
          }
          .zIndex(100f),
      ) {
        FilterChip(
          selected = isSelected,
          onClick = {},
          label = {
            Text(
              text = activeDraggedTab.displayName,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
          },
          leadingIcon = {
            Icon(
              imageVector = activeDraggedTab.iconVector(),
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          },
          shape = CircleShape,
        )
      }
    }
  }
}

private fun LazyListState.isItemFullyVisible(index: Int): Boolean {
  val info = layoutInfo
  val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return false
  return item.offset >= info.viewportStartOffset &&
    (item.offset + item.size) <= info.viewportEndOffset
}

private fun LazyGridState.isItemFullyVisible(index: Int): Boolean {
  val info = layoutInfo
  val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return false
  return item.offset.y >= info.viewportStartOffset &&
    (item.offset.y + item.size.height) <= info.viewportEndOffset
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTabContent(
  playlists: List<MusicPlaylist>,
  viewMode: MusicViewMode,
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrackId: String?,
  isPlaying: Boolean,
  refreshGeneration: Int = 0,
  onRefresh: () -> Unit,
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectPlaylist: (MusicPlaylist) -> Unit,
  onPlayPlaylist: (MusicPlaylist) -> Unit,
) {
  val density = LocalDensity.current
  val pullThresholdPx = with(density) { 44.dp.toPx() }
  var lastRefreshTimeMs by remember { mutableLongStateOf(0L) }
  var accumulatedPullPx by remember { mutableFloatStateOf(0f) }
  val currentOnRefresh by rememberUpdatedState(onRefresh)

  fun triggerPullRefresh() {
    val now = System.currentTimeMillis()
    if (now - lastRefreshTimeMs >= 120L) {
      lastRefreshTimeMs = now
      currentOnRefresh()
    }
  }

  val pullNestedScrollConnection = remember(pullThresholdPx) {
    object : NestedScrollConnection {
      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
      ): Offset {
        if (source == NestedScrollSource.UserInput && available.y > 0f) {
          accumulatedPullPx += available.y
        } else if (consumed.y < 0f) {
          accumulatedPullPx = 0f
        }
        return Offset.Zero
      }

      override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
        if (accumulatedPullPx >= pullThresholdPx) {
          accumulatedPullPx = 0f
          triggerPullRefresh()
        } else {
          accumulatedPullPx = 0f
        }
        return androidx.compose.ui.unit.Velocity.Zero
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("music_home_tab_content"),
  ) {
    // Quick Action Row: Play All, Shuffle, Refresh Mixes
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
        onClick = onRefresh,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_home_refresh_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Refresh mixes",
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Refresh", style = MaterialTheme.typography.labelMedium)
      }
    }

    PullToRefreshBox(
      isRefreshing = false,
      onRefresh = { triggerPullRefresh() },
      modifier = Modifier
        .fillMaxSize()
        .nestedScroll(pullNestedScrollConnection)
        .testTag("home_pull_to_refresh"),
    ) {
      if (playlists.isEmpty()) {
        EmptyMusicState(
          message = "No mixes match your search — pull down to refresh your 6 custom mixes",
        )
      } else if (viewMode == MusicViewMode.LIST) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("home_list_view"),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          item(key = "home_mixes_banner") {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("home_mixes_header"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "Made for You • 6 ~1 hr mixes • Pull down to refresh",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  fontWeight = FontWeight.Medium,
                )
              }
            }
          }

          itemsIndexed(playlists, key = { idx, it -> "${it.id}_${refreshGeneration}_$idx" }) { index, playlist ->
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
                .semantics { selected = isActive }
                .testTag("home_playlist_item_$index"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp)
                  .testTag("playlist_item_${playlist.name}"),
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = playlist.summaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
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
                MusicCollectionContextMenuButton(
                  collectionTitle = playlist.name,
                  tracks = playlist.tracks,
                  moreButtonTestTag = "playlist_more_${playlist.name}",
                  menuTestTag = "playlist_menu_${playlist.name}",
                  onPlay = { onPlayPlaylist(playlist) },
                )
              }
            }
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(cardsPerRow),
          modifier = Modifier
            .fillMaxSize()
            .testTag("home_card_view"),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          item(span = { GridItemSpan(maxLineSpan) }, key = "home_mixes_banner") {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("home_mixes_header"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "Made for You • 6 ~1 hr mixes • Pull down to refresh",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  fontWeight = FontWeight.Medium,
                )
              }
            }
          }

          gridItemsIndexed(playlists, key = { idx, it -> "${it.id}_${refreshGeneration}_$idx" }) { index, playlist ->
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
                .semantics { selected = isActive }
                .testTag("home_playlist_item_$index"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp)
                  .testTag("playlist_item_${playlist.name}"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .clip(RoundedCornerShape(12.dp)),
                ) {
                  TrackArtworkBanner(
                    track = playlist.representativeTrack,
                    modifier = Modifier.fillMaxSize(),
                  )
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                      .align(Alignment.TopStart)
                      .padding(6.dp),
                  ) {
                    Text(
                      text = playlist.totalDurationFormatted,
                      style = MaterialTheme.typography.labelSmall,
                      fontFamily = FontFamily.Monospace,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                  }
                  MusicCollectionContextMenuButton(
                    collectionTitle = playlist.name,
                    tracks = playlist.tracks,
                    moreButtonTestTag = "playlist_more_${playlist.name}",
                    menuTestTag = "playlist_menu_${playlist.name}",
                    onPlay = { onPlayPlaylist(playlist) },
                    tint = Color.White,
                    containerColor = ArtworkOverlayButtonBackground,
                    modifier = Modifier
                      .align(Alignment.BottomStart)
                      .padding(6.dp),
                  )
                  FilledIconButton(
                    onClick = { onPlayPlaylist(playlist) },
                    shape = CircleShape,
                    modifier = Modifier
                      .align(Alignment.BottomEnd)
                      .padding(6.dp)
                      .size(30.dp)
                      .testTag("play_playlist_${playlist.name}"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                      containerColor = ArtworkOverlayButtonBackground,
                      contentColor = Color.White,
                    ),
                  ) {
                    Icon(
                      imageVector = if (isActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                      contentDescription = "Play ${playlist.name}",
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }
                Text(
                  text = playlist.name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = playlist.summaryText,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ArtistsTabContent(
  artists: List<MusicArtist>,
  viewMode: MusicViewMode,
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrack: MusicTrack? = null,
  currentArtistName: String?,
  isPlaying: Boolean,
  favoriteArtists: Set<String> = emptySet(),
  showFavoritesOnly: Boolean = false,
  sectionFocusVersion: Int = 0,
  onToggleFavoritesOnly: () -> Unit = {},
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectArtist: (MusicArtist) -> Unit,
  onPlayArtist: (MusicArtist) -> Unit,
  onToggleFavoriteArtist: (MusicArtist) -> Unit = {},
) {
  val focusedIndex = remember(artists, currentTrack?.id, currentArtistName) {
    MusicPlayerEngine.resolveFocusedArtistIndex(
      artists = artists,
      currentTrack = currentTrack,
      currentArtistName = currentArtistName,
    )
  }

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

      OutlinedButton(
        onClick = onToggleFavoritesOnly,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = if (showFavoritesOnly) {
          ButtonDefaults.filledTonalButtonColors()
        } else {
          ButtonDefaults.outlinedButtonColors()
        },
        border = if (showFavoritesOnly) {
          null
        } else {
          ButtonDefaults.outlinedButtonBorder(enabled = true)
        },
        modifier = Modifier
          .semantics { selected = showFavoritesOnly }
          .testTag("music_favorites_filter_chip"),
      ) {
        Icon(
          imageVector = if (showFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Favorites", style = MaterialTheme.typography.labelMedium)
      }
    }

    if (artists.isEmpty()) {
      EmptyMusicState(
        message = if (showFavoritesOnly) "No favorite artists yet" else "No artists match your search",
      )
      return
    }

    if (viewMode == MusicViewMode.LIST) {
      val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = focusedIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedIndex, artists.size) {
        if (focusedIndex in artists.indices && !listState.isItemFullyVisible(focusedIndex)) {
          listState.scrollToItem(focusedIndex)
        }
      }

      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .testTag("artist_list_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        itemsIndexed(artists, key = { _, it -> it.name }) { index, artist ->
          val isActive = index == focusedIndex ||
            (currentArtistName != null && currentArtistName.equals(artist.name, ignoreCase = true))
          val isFavorite = MusicPlayerEngine.isArtistFavorite(artist, favoriteArtists)
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
              .semantics { selected = isActive }
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
                  color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
              IconButton(
                onClick = { onToggleFavoriteArtist(artist) },
                modifier = Modifier
                  .size(34.dp)
                  .testTag("favorite_artist_${artist.name}"),
              ) {
                Icon(
                  imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = if (isFavorite) "Remove ${artist.name} from favorites" else "Add ${artist.name} to favorites",
                  tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
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
              val itemActions = LocalMusicItemActions.current
              MusicCollectionContextMenuButton(
                collectionTitle = artist.name,
                tracks = artist.tracks,
                moreButtonTestTag = "artist_more_${artist.name}",
                menuTestTag = "artist_menu_${artist.name}",
                isFavorite = isFavorite,
                onToggleFavorite = { onToggleFavoriteArtist(artist) },
                onPlay = { onPlayArtist(artist) },
                onRenamePlaylist = { itemActions.onRenameArtist(artist) },
                onDeletePlaylist = { itemActions.onDeleteArtist(artist) },
              )
            }
          }
        }
      }
    } else {
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedIndex, artists.size) {
        if (focusedIndex in artists.indices && !gridState.isItemFullyVisible(focusedIndex)) {
          gridState.scrollToItem(focusedIndex)
        }
      }

      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(cardsPerRow),
        modifier = Modifier
          .fillMaxSize()
          .testTag("artist_card_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        gridItemsIndexed(artists, key = { _, it -> it.name }) { index, artist ->
          val isActive = index == focusedIndex ||
            (currentArtistName != null && currentArtistName.equals(artist.name, ignoreCase = true))
          val isFavorite = MusicPlayerEngine.isArtistFavorite(artist, favoriteArtists)
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
              .semantics { selected = isActive }
              .testTag("artist_item_${artist.name}"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(96.dp),
              ) {
                TrackArtworkThumbnail(
                  track = artist.representativeTrack,
                  size = 96.dp,
                  shape = CircleShape,
                  modifier = Modifier.align(Alignment.Center),
                )
                IconButton(
                  onClick = { onToggleFavoriteArtist(artist) },
                  modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(ArtworkOverlayButtonBackground, CircleShape)
                    .testTag("favorite_artist_${artist.name}"),
                ) {
                  Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) "Remove ${artist.name} from favorites" else "Add ${artist.name} to favorites",
                    tint = if (isFavorite) Color(0xFFFF80AB) else Color.White,
                    modifier = Modifier.size(16.dp),
                  )
                }
                val itemActions = LocalMusicItemActions.current
                MusicCollectionContextMenuButton(
                  collectionTitle = artist.name,
                  tracks = artist.tracks,
                  moreButtonTestTag = "artist_more_${artist.name}",
                  menuTestTag = "artist_menu_${artist.name}",
                  isFavorite = isFavorite,
                  onToggleFavorite = { onToggleFavoriteArtist(artist) },
                  onPlay = { onPlayArtist(artist) },
                  onRenamePlaylist = { itemActions.onRenameArtist(artist) },
                  onDeletePlaylist = { itemActions.onDeleteArtist(artist) },
                  tint = Color.White,
                  containerColor = ArtworkOverlayButtonBackground,
                  modifier = Modifier.align(Alignment.BottomStart),
                )
                FilledIconButton(
                  onClick = { onPlayArtist(artist) },
                  shape = CircleShape,
                  modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomEnd)
                    .testTag("play_artist_${artist.name}"),
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ArtworkOverlayButtonBackground,
                    contentColor = Color.White,
                  ),
                ) {
                  Icon(
                    imageVector = if (isActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = "Play ${artist.name}",
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
              Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrack: MusicTrack? = null,
  currentAlbumName: String?,
  isPlaying: Boolean,
  favoriteAlbums: Set<String> = emptySet(),
  showFavoritesOnly: Boolean = false,
  sectionFocusVersion: Int = 0,
  onToggleFavoritesOnly: () -> Unit = {},
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectAlbum: (MusicAlbum) -> Unit,
  onPlayAlbum: (MusicAlbum) -> Unit,
  onToggleFavoriteAlbum: (MusicAlbum) -> Unit = {},
) {
  val focusedIndex = remember(albums, currentTrack?.id, currentAlbumName) {
    MusicPlayerEngine.resolveFocusedAlbumIndex(
      albums = albums,
      currentTrack = currentTrack,
      currentAlbumName = currentAlbumName,
    )
  }

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

      OutlinedButton(
        onClick = onToggleFavoritesOnly,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = if (showFavoritesOnly) {
          ButtonDefaults.filledTonalButtonColors()
        } else {
          ButtonDefaults.outlinedButtonColors()
        },
        border = if (showFavoritesOnly) {
          null
        } else {
          ButtonDefaults.outlinedButtonBorder(enabled = true)
        },
        modifier = Modifier
          .semantics { selected = showFavoritesOnly }
          .testTag("music_favorites_filter_chip"),
      ) {
        Icon(
          imageVector = if (showFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Favorites", style = MaterialTheme.typography.labelMedium)
      }
    }

    if (albums.isEmpty()) {
      EmptyMusicState(
        message = if (showFavoritesOnly) "No favorite albums yet" else "No albums match your search",
      )
      return
    }

    if (viewMode == MusicViewMode.LIST) {
      val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = focusedIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedIndex, albums.size) {
        if (focusedIndex in albums.indices && !listState.isItemFullyVisible(focusedIndex)) {
          listState.scrollToItem(focusedIndex)
        }
      }

      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .testTag("album_list_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        itemsIndexed(albums, key = { _, it -> "${it.name}|${it.artist}" }) { index, album ->
          val isActive = index == focusedIndex ||
            (currentAlbumName != null && currentAlbumName.equals(album.name, ignoreCase = true))
          val isFavorite = MusicPlayerEngine.isAlbumFavorite(album, favoriteAlbums)
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
              .semantics { selected = isActive }
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
                  color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
              IconButton(
                onClick = { onToggleFavoriteAlbum(album) },
                modifier = Modifier
                  .size(34.dp)
                  .testTag("favorite_album_${album.name}"),
              ) {
                Icon(
                  imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = if (isFavorite) "Remove ${album.name} from favorites" else "Add ${album.name} to favorites",
                  tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
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
              val itemActions = LocalMusicItemActions.current
              MusicCollectionContextMenuButton(
                collectionTitle = album.name,
                tracks = album.tracks,
                moreButtonTestTag = "album_more_${album.name}",
                menuTestTag = "album_menu_${album.name}",
                isFavorite = isFavorite,
                onToggleFavorite = { onToggleFavoriteAlbum(album) },
                onPlay = { onPlayAlbum(album) },
                onRenamePlaylist = { itemActions.onRenameAlbum(album) },
                onDeletePlaylist = { itemActions.onDeleteAlbum(album) },
              )
            }
          }
        }
      }
    } else {
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedIndex, albums.size) {
        if (focusedIndex in albums.indices && !gridState.isItemFullyVisible(focusedIndex)) {
          gridState.scrollToItem(focusedIndex)
        }
      }

      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(cardsPerRow),
        modifier = Modifier
          .fillMaxSize()
          .testTag("album_card_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        gridItemsIndexed(albums, key = { _, it -> "${it.name}|${it.artist}" }) { index, album ->
          val isActive = index == focusedIndex ||
            (currentAlbumName != null && currentAlbumName.equals(album.name, ignoreCase = true))
          val isFavorite = MusicPlayerEngine.isAlbumFavorite(album, favoriteAlbums)
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
              .semantics { selected = isActive }
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
                  .aspectRatio(1f)
                  .clip(RoundedCornerShape(12.dp)),
              ) {
                TrackArtworkBanner(
                  track = album.representativeTrack,
                  modifier = Modifier.fillMaxSize(),
                )
                IconButton(
                  onClick = { onToggleFavoriteAlbum(album) },
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(ArtworkOverlayButtonBackground, CircleShape)
                    .testTag("favorite_album_${album.name}"),
                ) {
                  Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) "Remove ${album.name} from favorites" else "Add ${album.name} to favorites",
                    tint = if (isFavorite) Color(0xFFFF80AB) else Color.White,
                    modifier = Modifier.size(16.dp),
                  )
                }
                val itemActions = LocalMusicItemActions.current
                MusicCollectionContextMenuButton(
                  collectionTitle = album.name,
                  tracks = album.tracks,
                  moreButtonTestTag = "album_more_${album.name}",
                  menuTestTag = "album_menu_${album.name}",
                  isFavorite = isFavorite,
                  onToggleFavorite = { onToggleFavoriteAlbum(album) },
                  onPlay = { onPlayAlbum(album) },
                  onRenamePlaylist = { itemActions.onRenameAlbum(album) },
                  onDeletePlaylist = { itemActions.onDeleteAlbum(album) },
                  tint = Color.White,
                  containerColor = ArtworkOverlayButtonBackground,
                  modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
                )
                FilledIconButton(
                  onClick = { onPlayAlbum(album) },
                  shape = CircleShape,
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(30.dp)
                    .testTag("play_album_${album.name}"),
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ArtworkOverlayButtonBackground,
                    contentColor = Color.White,
                  ),
                ) {
                  Icon(
                    imageVector = if (isActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = "Play ${album.name}",
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
              Text(
                text = album.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  showFavoritesOnly: Boolean,
  sectionFocusVersion: Int = 0,
  onToggleFavoritesOnly: () -> Unit,
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
) {
  val focusedIndex = remember(tracks, currentTrackId) {
    MusicPlayerEngine.resolveFocusedTrackIndex(tracks, currentTrackId)
  }

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

      OutlinedButton(
        onClick = onToggleFavoritesOnly,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = if (showFavoritesOnly) {
          ButtonDefaults.filledTonalButtonColors()
        } else {
          ButtonDefaults.outlinedButtonColors()
        },
        border = if (showFavoritesOnly) {
          null
        } else {
          ButtonDefaults.outlinedButtonBorder(enabled = true)
        },
        modifier = Modifier
          .semantics { selected = showFavoritesOnly }
          .testTag("music_favorites_filter_chip"),
      ) {
        Icon(
          imageVector = if (showFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Favorites", style = MaterialTheme.typography.labelMedium)
      }
    }

    if (tracks.isEmpty()) {
      EmptyMusicState(
        message = if (showFavoritesOnly) "No favorite songs yet" else "No songs match your search",
      )
      return
    }

    if (viewMode == MusicViewMode.LIST) {
      val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = focusedIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedIndex, tracks.size) {
        if (focusedIndex in tracks.indices && !listState.isItemFullyVisible(focusedIndex)) {
          listState.scrollToItem(focusedIndex)
        }
      }

      LazyColumn(
        state = listState,
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
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedIndex, tracks.size) {
        if (focusedIndex in tracks.indices && !gridState.isItemFullyVisible(focusedIndex)) {
          gridState.scrollToItem(focusedIndex)
        }
      }

      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(cardsPerRow),
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
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrack: MusicTrack? = null,
  currentGenreName: String?,
  isPlaying: Boolean,
  favoriteGenres: Set<String> = emptySet(),
  showFavoritesOnly: Boolean = false,
  sectionFocusVersion: Int = 0,
  onToggleFavoritesOnly: () -> Unit = {},
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectGenre: (MusicGenre) -> Unit,
  onPlayGenre: (MusicGenre) -> Unit,
  onToggleFavoriteGenre: (MusicGenre) -> Unit = {},
) {
  val focusedIndex = remember(genres, currentTrack?.id, currentGenreName) {
    MusicPlayerEngine.resolveFocusedGenreIndex(
      genres = genres,
      currentTrack = currentTrack,
      currentGenreName = currentGenreName,
    )
  }

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

      OutlinedButton(
        onClick = onToggleFavoritesOnly,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = if (showFavoritesOnly) {
          ButtonDefaults.filledTonalButtonColors()
        } else {
          ButtonDefaults.outlinedButtonColors()
        },
        border = if (showFavoritesOnly) {
          null
        } else {
          ButtonDefaults.outlinedButtonBorder(enabled = true)
        },
        modifier = Modifier
          .semantics { selected = showFavoritesOnly }
          .testTag("music_favorites_filter_chip"),
      ) {
        Icon(
          imageVector = if (showFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Favorites", style = MaterialTheme.typography.labelMedium)
      }
    }

    if (genres.isEmpty()) {
      EmptyMusicState(
        message = if (showFavoritesOnly) "No favorite genres yet" else "No genres match your search",
      )
      return
    }

    if (viewMode == MusicViewMode.LIST) {
      val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = focusedIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedIndex, genres.size) {
        if (focusedIndex in genres.indices && !listState.isItemFullyVisible(focusedIndex)) {
          listState.scrollToItem(focusedIndex)
        }
      }

      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .testTag("genre_list_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        itemsIndexed(genres, key = { _, it -> it.name }) { index, genre ->
          val isActive = index == focusedIndex ||
            (currentGenreName != null && currentGenreName.equals(genre.name, ignoreCase = true))
          val isFavorite = MusicPlayerEngine.isGenreFavorite(genre, favoriteGenres)
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
              .semantics { selected = isActive }
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
                  color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
              IconButton(
                onClick = { onToggleFavoriteGenre(genre) },
                modifier = Modifier
                  .size(34.dp)
                  .testTag("favorite_genre_${genre.name}"),
              ) {
                Icon(
                  imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = if (isFavorite) "Remove ${genre.name} from favorites" else "Add ${genre.name} to favorites",
                  tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
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
              val itemActions = LocalMusicItemActions.current
              MusicCollectionContextMenuButton(
                collectionTitle = genre.name,
                tracks = genre.tracks,
                moreButtonTestTag = "genre_more_${genre.name}",
                menuTestTag = "genre_menu_${genre.name}",
                isFavorite = isFavorite,
                onToggleFavorite = { onToggleFavoriteGenre(genre) },
                onPlay = { onPlayGenre(genre) },
                onRenamePlaylist = { itemActions.onRenameGenre(genre) },
                onDeletePlaylist = { itemActions.onDeleteGenre(genre) },
              )
            }
          }
        }
      }
    } else {
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedIndex, genres.size) {
        if (focusedIndex in genres.indices && !gridState.isItemFullyVisible(focusedIndex)) {
          gridState.scrollToItem(focusedIndex)
        }
      }

      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(cardsPerRow),
        modifier = Modifier
          .fillMaxSize()
          .testTag("genre_card_view"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        gridItemsIndexed(genres, key = { _, it -> it.name }) { index, genre ->
          val isActive = index == focusedIndex ||
            (currentGenreName != null && currentGenreName.equals(genre.name, ignoreCase = true))
          val isFavorite = MusicPlayerEngine.isGenreFavorite(genre, favoriteGenres)
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
              .semantics { selected = isActive }
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
                IconButton(
                  onClick = { onToggleFavoriteGenre(genre) },
                  modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(ArtworkOverlayButtonBackground, CircleShape)
                    .testTag("favorite_genre_${genre.name}"),
                ) {
                  Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) "Remove ${genre.name} from favorites" else "Add ${genre.name} to favorites",
                    tint = if (isFavorite) Color(0xFFFF80AB) else Color.White,
                    modifier = Modifier.size(16.dp),
                  )
                }
                val itemActions = LocalMusicItemActions.current
                MusicCollectionContextMenuButton(
                  collectionTitle = genre.name,
                  tracks = genre.tracks,
                  moreButtonTestTag = "genre_more_${genre.name}",
                  menuTestTag = "genre_menu_${genre.name}",
                  isFavorite = isFavorite,
                  onToggleFavorite = { onToggleFavoriteGenre(genre) },
                  onPlay = { onPlayGenre(genre) },
                  onRenamePlaylist = { itemActions.onRenameGenre(genre) },
                  onDeletePlaylist = { itemActions.onDeleteGenre(genre) },
                  tint = Color.White,
                  containerColor = ArtworkOverlayButtonBackground,
                  modifier = Modifier.align(Alignment.BottomStart),
                )
                FilledIconButton(
                  onClick = { onPlayGenre(genre) },
                  shape = CircleShape,
                  modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomEnd)
                    .testTag("play_genre_${genre.name}"),
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ArtworkOverlayButtonBackground,
                    contentColor = Color.White,
                  ),
                ) {
                  Icon(
                    imageVector = if (isActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = "Play ${genre.name}",
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
              Text(
                text = genre.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrackId: String?,
  isPlaying: Boolean,
  sectionFocusVersion: Int = 0,
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSaveQueueAsPlaylist: () -> Unit,
  onSelectPlaylist: (MusicPlaylist) -> Unit,
  onPlayPlaylist: (MusicPlaylist) -> Unit,
  onDeletePlaylist: (MusicPlaylist) -> Unit,
) {
  val focusedIndex = remember(playlists, currentTrackId) {
    MusicPlayerEngine.resolveFocusedPlaylistIndex(playlists, currentTrackId)
  }
  val scrollTargetIndex = if (focusedIndex <= 1) 0 else focusedIndex

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
      val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollTargetIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, scrollTargetIndex, playlists.size) {
        if (scrollTargetIndex in playlists.indices && !listState.isItemFullyVisible(scrollTargetIndex)) {
          listState.scrollToItem(scrollTargetIndex)
        }
      }

      LazyColumn(
        state = listState,
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
              .semantics { selected = isActive }
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
                  color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
              val itemActions = LocalMusicItemActions.current
              MusicCollectionContextMenuButton(
                collectionTitle = playlist.name,
                tracks = playlist.tracks,
                moreButtonTestTag = "playlist_more_${playlist.name}",
                menuTestTag = "playlist_menu_${playlist.name}",
                onPlay = { onPlayPlaylist(playlist) },
                onRenamePlaylist = if (!playlist.isBuiltIn) {
                  { itemActions.onRenamePlaylist(playlist) }
                } else null,
                onDeletePlaylist = if (!playlist.isBuiltIn) {
                  { onDeletePlaylist(playlist) }
                } else null,
              )
            }
          }
        }
      }
    } else {
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = scrollTargetIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, scrollTargetIndex, playlists.size) {
        if (scrollTargetIndex in playlists.indices && !gridState.isItemFullyVisible(scrollTargetIndex)) {
          gridState.scrollToItem(scrollTargetIndex)
        }
      }

      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(cardsPerRow),
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
              .semantics { selected = isActive }
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
                    playlist.isRecentlyPlayed -> Icons.Default.Timer
                    else -> Icons.AutoMirrored.Filled.PlaylistPlay
                  },
                  contentDescription = null,
                  tint = Color.White.copy(alpha = 0.85f),
                  modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopStart),
                )
                val itemActions = LocalMusicItemActions.current
                MusicCollectionContextMenuButton(
                  collectionTitle = playlist.name,
                  tracks = playlist.tracks,
                  moreButtonTestTag = "playlist_more_${playlist.name}",
                  menuTestTag = "playlist_menu_${playlist.name}",
                  onPlay = { onPlayPlaylist(playlist) },
                  onRenamePlaylist = if (!playlist.isBuiltIn) {
                    { itemActions.onRenamePlaylist(playlist) }
                  } else null,
                  onDeletePlaylist = if (!playlist.isBuiltIn) {
                    { onDeletePlaylist(playlist) }
                  } else null,
                  tint = Color.White,
                  containerColor = ArtworkOverlayButtonBackground,
                  modifier = Modifier.align(Alignment.BottomStart),
                )
                FilledIconButton(
                  onClick = { onPlayPlaylist(playlist) },
                  shape = CircleShape,
                  modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomEnd)
                    .testTag("play_playlist_${playlist.name}"),
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ArtworkOverlayButtonBackground,
                    contentColor = Color.White,
                  ),
                ) {
                  Icon(
                    imageVector = if (isActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = "Play ${playlist.name}",
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
              Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrack: MusicTrack? = null,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  isArtistFavorite: Boolean = false,
  sectionFocusVersion: Int = 0,
  onPlayArtist: (shuffle: Boolean) -> Unit,
  onOpenAlbum: (MusicAlbum) -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
  onToggleFavoriteArtist: () -> Unit = {},
) {
  val focusedTrackIndex = remember(artist.tracks, currentTrackId) {
    MusicPlayerEngine.resolveFocusedTrackIndex(artist.tracks, currentTrackId)
  }
  val focusedAlbumIndex = remember(artist.albums, currentTrack?.id, currentTrack?.album) {
    MusicPlayerEngine.resolveFocusedAlbumIndex(
      albums = artist.albums,
      currentTrack = currentTrack,
      currentAlbumName = currentTrack?.album,
      currentArtistName = currentTrack?.artist,
    )
  }

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
          IconButton(
            onClick = onToggleFavoriteArtist,
            modifier = Modifier
              .size(36.dp)
              .testTag("favorite_artist_${artist.name}"),
          ) {
            Icon(
              imageVector = if (isArtistFavorite) Icons.Default.Star else Icons.Default.StarBorder,
              contentDescription = if (isArtistFavorite) "Remove ${artist.name} from favorites" else "Add ${artist.name} to favorites",
              tint = if (isArtistFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        if (artist.albums.isNotEmpty()) {
          val albumRowState = rememberLazyListState(
            initialFirstVisibleItemIndex = focusedAlbumIndex.coerceAtLeast(0),
          )
          LaunchedEffect(sectionFocusVersion, focusedAlbumIndex, artist.albums.size) {
            if (focusedAlbumIndex in artist.albums.indices && !albumRowState.isItemFullyVisible(focusedAlbumIndex)) {
              albumRowState.scrollToItem(focusedAlbumIndex)
            }
          }

          LazyRow(
            state = albumRowState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            itemsIndexed(artist.albums, key = { _, it -> it.name }) { index, album ->
              val isActiveAlbum = index == focusedAlbumIndex ||
                (currentTrackId != null && album.tracks.any { it.id == currentTrackId })
              Surface(
                shape = CircleShape,
                color = if (isActiveAlbum) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                } else {
                  MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                  1.dp,
                  if (isActiveAlbum) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                  } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                  },
                ),
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable { onOpenAlbum(album) }
                  .semantics { selected = isActiveAlbum }
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
                    color = if (isActiveAlbum) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }
        }
      }
    }

    if (viewMode == MusicViewMode.LIST) {
      val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = focusedTrackIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedTrackIndex, artist.tracks.size) {
        if (focusedTrackIndex in artist.tracks.indices && !listState.isItemFullyVisible(focusedTrackIndex)) {
          listState.scrollToItem(focusedTrackIndex)
        }
      }

      LazyColumn(
        state = listState,
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
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedTrackIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedTrackIndex, artist.tracks.size) {
        if (focusedTrackIndex in artist.tracks.indices && !gridState.isItemFullyVisible(focusedTrackIndex)) {
          gridState.scrollToItem(focusedTrackIndex)
        }
      }

      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(cardsPerRow),
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
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  isAlbumFavorite: Boolean = false,
  sectionFocusVersion: Int = 0,
  onPlayAlbum: (shuffle: Boolean) -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
  onToggleFavoriteAlbum: () -> Unit = {},
) {
  val focusedTrackIndex = remember(album.tracks, currentTrackId) {
    MusicPlayerEngine.resolveFocusedTrackIndex(album.tracks, currentTrackId)
  }

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
        IconButton(
          onClick = onToggleFavoriteAlbum,
          modifier = Modifier
            .size(36.dp)
            .testTag("favorite_album_${album.name}"),
        ) {
          Icon(
            imageVector = if (isAlbumFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = if (isAlbumFavorite) "Remove ${album.name} from favorites" else "Add ${album.name} to favorites",
            tint = if (isAlbumFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    if (viewMode == MusicViewMode.LIST) {
      val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = focusedTrackIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedTrackIndex, album.tracks.size) {
        if (focusedTrackIndex in album.tracks.indices && !listState.isItemFullyVisible(focusedTrackIndex)) {
          listState.scrollToItem(focusedTrackIndex)
        }
      }

      LazyColumn(
        state = listState,
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
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedTrackIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedTrackIndex, album.tracks.size) {
        if (focusedTrackIndex in album.tracks.indices && !gridState.isItemFullyVisible(focusedTrackIndex)) {
          gridState.scrollToItem(focusedTrackIndex)
        }
      }

      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(cardsPerRow),
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
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  isGenreFavorite: Boolean = false,
  sectionFocusVersion: Int = 0,
  onPlayGenre: (shuffle: Boolean) -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
  onToggleFavoriteGenre: () -> Unit = {},
) {
  val focusedTrackIndex = remember(genre.tracks, currentTrackId) {
    MusicPlayerEngine.resolveFocusedTrackIndex(genre.tracks, currentTrackId)
  }

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
        IconButton(
          onClick = onToggleFavoriteGenre,
          modifier = Modifier
            .size(36.dp)
            .testTag("favorite_genre_${genre.name}"),
        ) {
          Icon(
            imageVector = if (isGenreFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = if (isGenreFavorite) "Remove ${genre.name} from favorites" else "Add ${genre.name} to favorites",
            tint = if (isGenreFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    if (viewMode == MusicViewMode.LIST) {
      val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = focusedTrackIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedTrackIndex, genre.tracks.size) {
        if (focusedTrackIndex in genre.tracks.indices && !listState.isItemFullyVisible(focusedTrackIndex)) {
          listState.scrollToItem(focusedTrackIndex)
        }
      }

      LazyColumn(
        state = listState,
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
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedTrackIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedTrackIndex, genre.tracks.size) {
        if (focusedTrackIndex in genre.tracks.indices && !gridState.isItemFullyVisible(focusedTrackIndex)) {
          gridState.scrollToItem(focusedTrackIndex)
        }
      }

      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(cardsPerRow),
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
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrackId: String?,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  playStats: Map<String, TrackPlayStats> = emptyMap(),
  sectionFocusVersion: Int = 0,
  onPlayPlaylist: (shuffle: Boolean) -> Unit,
  onDeletePlaylist: () -> Unit,
  onSelectTrack: (MusicTrack) -> Unit,
  onToggleFavorite: (MusicTrack) -> Unit,
) {
  val focusedTrackIndex = remember(playlist.tracks, currentTrackId) {
    MusicPlayerEngine.resolveFocusedTrackIndex(playlist.tracks, currentTrackId)
  }

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

    if ((playlist.isMostPlayed || playlist.isRecentlyPlayed) && playlist.tracks.isEmpty()) {
      EmptyMusicState(message = "No songs in library yet")
      return@Column
    }

    if (viewMode == MusicViewMode.LIST) {
      val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = focusedTrackIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedTrackIndex, playlist.tracks.size) {
        if (focusedTrackIndex in playlist.tracks.indices && !listState.isItemFullyVisible(focusedTrackIndex)) {
          listState.scrollToItem(focusedTrackIndex)
        }
      }

      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(playlist.tracks, key = { it.id }) { track ->
          val statBadge = when {
            playlist.isMostPlayed -> {
              val s = playStats[track.id]
              if (s != null && s.hasRecordedActivity) {
                val playsLabel = if (s.playCount == 1) "1 play" else "${s.playCount} plays"
                val fullLabel = "${s.formattedFullPlays(track.durationMs)} full"
                val skipsSuffix = if (s.skipCount > 0) {
                  " • ${s.skipCount} ${if (s.skipCount == 1) "skip" else "skips"}"
                } else {
                  ""
                }
                val lastPlayedSuffix = if (s.lastPlayedAtMs > 0L) {
                  " • ${s.lastPlayedFormatted}"
                } else {
                  ""
                }
                "$playsLabel • $fullLabel • ${s.totalPlayTimeFormatted}$skipsSuffix$lastPlayedSuffix"
              } else {
                null
              }
            }
            playlist.isRecentlyPlayed -> {
              val s = playStats[track.id]
              if (s != null && s.lastPlayedAtMs > 0L) {
                val playsLabel = if (s.playCount == 1) "1 play" else "${s.playCount} plays"
                "${s.lastPlayedFormatted} • $playsLabel"
              } else {
                null
              }
            }
            else -> null
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
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedTrackIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedTrackIndex, playlist.tracks.size) {
        if (focusedTrackIndex in playlist.tracks.indices && !gridState.isItemFullyVisible(focusedTrackIndex)) {
          gridState.scrollToItem(focusedTrackIndex)
        }
      }

      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(cardsPerRow),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(playlist.tracks, key = { it.id }) { track ->
          val statBadge = when {
            playlist.isMostPlayed -> {
              val s = playStats[track.id]
              if (s != null && s.hasRecordedActivity) {
                val playsLabel = if (s.playCount == 1) "1 play" else "${s.playCount} plays"
                val fullLabel = "${s.formattedFullPlays(track.durationMs)} full"
                val skipsSuffix = if (s.skipCount > 0) {
                  " • ${s.skipCount} ${if (s.skipCount == 1) "skip" else "skips"}"
                } else {
                  ""
                }
                val lastPlayedSuffix = if (s.lastPlayedAtMs > 0L) {
                  " • ${s.lastPlayedFormatted}"
                } else {
                  ""
                }
                "$playsLabel • $fullLabel • ${s.totalPlayTimeFormatted}$skipsSuffix$lastPlayedSuffix"
              } else {
                null
              }
            }
            playlist.isRecentlyPlayed -> {
              val s = playStats[track.id]
              if (s != null && s.lastPlayedAtMs > 0L) {
                val playsLabel = if (s.playCount == 1) "1 play" else "${s.playCount} plays"
                "${s.lastPlayedFormatted} • $playsLabel"
              } else {
                null
              }
            }
            else -> null
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
      .semantics { selected = isCurrent }
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

      SongContextMenuButton(
        track = track,
        isFavorite = isFavorite,
        onPlay = onClick,
        onToggleFavorite = onToggleFavorite,
      )
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
      .semantics { selected = isCurrent }
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
            .align(Alignment.TopStart)
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
            .padding(6.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(ArtworkOverlayButtonBackground, CircleShape)
            .testTag("favorite_song_${track.id}"),
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) Color(0xFFFF80AB) else Color.White,
            modifier = Modifier.size(16.dp),
          )
        }
        SongContextMenuButton(
          track = track,
          isFavorite = isFavorite,
          onPlay = onClick,
          onToggleFavorite = onToggleFavorite,
          tint = Color.White,
          containerColor = ArtworkOverlayButtonBackground,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(6.dp),
        )
        if (isCurrent) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(6.dp)
              .size(30.dp)
              .clip(CircleShape)
              .background(ArtworkOverlayButtonBackground, CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }

      Text(
        text = track.title,
        style = MaterialTheme.typography.bodyMedium,
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
  }
}

@Composable
private fun SongContextMenuButton(
  track: MusicTrack,
  isFavorite: Boolean,
  onPlay: () -> Unit,
  onToggleFavorite: () -> Unit,
  tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  containerColor: Color = Color.Transparent,
  modifier: Modifier = Modifier,
) {
  val actions = LocalMusicItemActions.current
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    IconButton(
      onClick = { expanded = true },
      modifier = Modifier
        .size(if (containerColor != Color.Transparent) 30.dp else 32.dp)
        .then(
          if (containerColor != Color.Transparent) {
            Modifier
              .clip(CircleShape)
              .background(containerColor, CircleShape)
          } else {
            Modifier
          }
        )
        .testTag("song_more_${track.id}"),
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "More options for ${track.title}",
        tint = tint,
        modifier = Modifier.size(18.dp),
      )
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.testTag("song_menu_${track.id}"),
    ) {
      DropdownMenuItem(
        text = { Text("Play Next") },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onPlayNext(listOf(track))
        },
        modifier = Modifier.testTag("song_menu_play_next_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Add to Queue") },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onAddToQueue(listOf(track))
        },
        modifier = Modifier.testTag("song_menu_add_to_queue_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Add to Playlist…") },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onAddToPlaylist(listOf(track))
        },
        modifier = Modifier.testTag("song_menu_add_to_playlist_${track.id}"),
      )
      if (actions.onRemoveFromPlaylist != null) {
        DropdownMenuItem(
          text = { Text("Remove from Playlist") },
          leadingIcon = { Icon(Icons.Default.RemoveCircleOutline, contentDescription = null) },
          onClick = {
            expanded = false
            actions.onRemoveFromPlaylist.invoke(track)
          },
          modifier = Modifier.testTag("song_menu_remove_from_playlist_${track.id}"),
        )
      }
      DropdownMenuItem(
        text = { Text("Go to Artist") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onGoToArtist(track)
        },
        modifier = Modifier.testTag("song_menu_go_to_artist_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Go to Album") },
        leadingIcon = { Icon(Icons.Default.Album, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onGoToAlbum(track)
        },
        modifier = Modifier.testTag("song_menu_go_to_album_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Track Details") },
        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onTrackDetails(track)
        },
        modifier = Modifier.testTag("song_menu_details_${track.id}"),
      )
      HorizontalDivider()
      DropdownMenuItem(
        text = { Text("Edit in Audio Editor") },
        leadingIcon = { Icon(Icons.Default.GraphicEq, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onEditAudio(track)
        },
        modifier = Modifier.testTag("song_menu_edit_audio_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Convert") },
        leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onConvertTrack(track)
        },
        modifier = Modifier.testTag("song_menu_convert_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Folder") },
        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onOpenInFiles(track)
        },
        modifier = Modifier.testTag("song_menu_open_in_files_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Open with…") },
        leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onOpenWith(track)
        },
        modifier = Modifier.testTag("song_menu_open_with_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Share") },
        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onShareTracks(listOf(track))
        },
        modifier = Modifier.testTag("song_menu_share_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Rename") },
        leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onRenameTrack(track)
        },
        modifier = Modifier.testTag("song_menu_rename_${track.id}"),
      )
      DropdownMenuItem(
        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
        leadingIcon = {
          Icon(
            Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
          )
        },
        onClick = {
          expanded = false
          actions.onDeleteTrack(track)
        },
        modifier = Modifier.testTag("song_menu_delete_${track.id}"),
      )
    }
  }
}

@Composable
private fun MusicCollectionContextMenuButton(
  collectionTitle: String,
  tracks: List<MusicTrack>,
  moreButtonTestTag: String,
  menuTestTag: String,
  isFavorite: Boolean? = null,
  onToggleFavorite: (() -> Unit)? = null,
  onPlay: () -> Unit,
  onShuffle: (() -> Unit)? = null,
  onRenamePlaylist: (() -> Unit)? = null,
  onDeletePlaylist: (() -> Unit)? = null,
  tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  containerColor: Color = Color.Transparent,
  modifier: Modifier = Modifier,
) {
  val actions = LocalMusicItemActions.current
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    IconButton(
      onClick = { expanded = true },
      modifier = Modifier
        .size(30.dp)
        .then(
          if (containerColor != Color.Transparent) {
            Modifier
              .clip(CircleShape)
              .background(containerColor, CircleShape)
          } else {
            Modifier
          }
        )
        .testTag(moreButtonTestTag),
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "More options for $collectionTitle",
        tint = tint,
        modifier = Modifier.size(18.dp),
      )
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.testTag(menuTestTag),
    ) {
      DropdownMenuItem(
        text = { Text("Shuffle") },
        leadingIcon = { Icon(Icons.Default.Shuffle, contentDescription = null) },
        onClick = {
          expanded = false
          if (onShuffle != null) onShuffle() else actions.onShuffleTracks(tracks)
        },
      )
      DropdownMenuItem(
        text = { Text("Play Next") },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onPlayNext(tracks)
        },
      )
      DropdownMenuItem(
        text = { Text("Add to Queue") },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onAddToQueue(tracks)
        },
      )
      DropdownMenuItem(
        text = { Text("Add to Playlist…") },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onAddToPlaylist(tracks)
        },
      )
      HorizontalDivider()
      DropdownMenuItem(
        text = { Text("Share") },
        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
        onClick = {
          expanded = false
          actions.onShareTracks(tracks)
        },
      )
      if (onRenamePlaylist != null) {
        DropdownMenuItem(
          text = { Text("Rename") },
          leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
          onClick = {
            expanded = false
            onRenamePlaylist()
          },
          modifier = Modifier.testTag(menuTestTag.replace("_menu_", "_menu_rename_")),
        )
      }
      if (onDeletePlaylist != null) {
        DropdownMenuItem(
          text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
          leadingIcon = {
            Icon(
              Icons.Default.Delete,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
            )
          },
          onClick = {
            expanded = false
            onDeletePlaylist()
          },
          modifier = Modifier.testTag(menuTestTag.replace("_menu_", "_menu_delete_")),
        )
      }
    }
  }
}

@Composable
private fun AddToPlaylistDialog(
  tracksToAdd: List<MusicTrack>,
  savedPlaylists: List<SavedPlaylistEntry>,
  accentColor: Color,
  onAddToExistingPlaylist: (playlistId: String, playlistName: String) -> Unit,
  onCreateNewPlaylist: (newName: String) -> Unit,
  onDismiss: () -> Unit,
) {
  var newPlaylistName by rememberSaveable {
    mutableStateOf(
      if (tracksToAdd.size == 1) "${tracksToAdd.first().title} Mix"
      else "Playlist ${savedPlaylists.size + 1}"
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("add_to_playlist_dialog"),
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
            text = "Add to Playlist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = if (tracksToAdd.size == 1) tracksToAdd.first().title else "${tracksToAdd.size} songs selected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (savedPlaylists.isNotEmpty()) {
          Text(
            text = "Saved Playlists",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          savedPlaylists.forEach { entry ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddToExistingPlaylist(entry.id, entry.name) }
                .testTag("add_to_playlist_item_${entry.name}"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                  )
                  Text(
                    text = "${entry.trackIds.size} songs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        Text(
          text = "Or Create New Playlist",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
          value = newPlaylistName,
          onValueChange = { newPlaylistName = it },
          label = { Text("New Playlist Name") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_to_playlist_new_name_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onCreateNewPlaylist(newPlaylistName) },
        enabled = newPlaylistName.trim().isNotEmpty(),
        shape = CircleShape,
        modifier = Modifier.testTag("add_to_playlist_create_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Create & Add")
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
private fun RenameMusicItemDialog(
  title: String,
  subtitle: String,
  initialValue: String,
  label: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var name by rememberSaveable(initialValue) { mutableStateOf(initialValue) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("rename_music_item_dialog"),
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
            imageVector = Icons.Default.DriveFileRenameOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("rename_music_item_input"),
      )
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name.trim()) },
        enabled = name.trim().isNotEmpty() && name.trim() != initialValue,
        shape = CircleShape,
        modifier = Modifier.testTag("confirm_rename_music_item_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Rename")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = CircleShape) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun DeleteTrackConfirmDialog(
  track: MusicTrack,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("delete_track_dialog"),
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
            .background(MaterialTheme.colorScheme.errorContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Delete Song?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${track.title} • ${track.artist}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Text(
        text = "Are you sure you want to remove \"${track.title}\" from your library? If backed by a local file, it will be deleted.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("confirm_delete_track_button"),
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Delete")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = CircleShape) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun BulkDeleteMusicCollectionConfirmDialog(
  target: BulkDeleteMusicCollectionTarget,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  var typedConfirmation by rememberSaveable(target.kind, target.name) { mutableStateOf("") }
  val canConfirm = typedConfirmation.trim() == target.name.trim()

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("bulk_delete_music_dialog"),
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
            .background(MaterialTheme.colorScheme.errorContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Delete ${target.kind.label}?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = target.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "This will permanently remove all ${target.songCount} ${if (target.songCount == 1) "song" else "songs"} in ${target.kind.label.lowercase()} \"${target.name}\" from your library. Type \"${target.name}\" below to confirm.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
          value = typedConfirmation,
          onValueChange = { typedConfirmation = it },
          label = { Text("Type \"${target.name}\" to confirm") },
          placeholder = { Text(target.name) },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("bulk_delete_name_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        enabled = canConfirm,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("confirm_bulk_delete_button"),
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Delete")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = CircleShape) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun DeletePlaylistConfirmDialog(
  playlist: MusicPlaylist,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("delete_playlist_dialog"),
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
            .background(MaterialTheme.colorScheme.errorContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Delete Playlist?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${playlist.name} • ${playlist.summaryText}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Text(
        text = "Are you sure you want to delete the playlist \"${playlist.name}\"? Your songs will remain in your library.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("confirm_delete_playlist_button"),
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Delete")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = CircleShape) {
        Text("Cancel")
      }
    },
  )
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
          modifier = Modifier
            .clickable(onClick = onExpand)
            .testTag("mini_player_album_art"),
        )

        Column(
          modifier = Modifier
            .weight(1f)
            .clearAndSetSemantics {
              contentDescription = "${track.title} • ${track.artist} • ${track.album}"
            },
        ) {
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
  shuffleMode: MusicShuffleMode,
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
  onCycleSleepTimer: () -> Unit,
  onSelectQueueIndex: (Int) -> Unit,
  onMoveQueueItem: (fromIndex: Int, toIndex: Int) -> Unit,
  onSaveQueueAsPlaylist: () -> Unit,
  onOpenTrackInfo: () -> Unit,
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
  val isSheetSettledExpanded = sheetState.currentValue == SheetValue.Expanded
  var queueOpenCount by remember {
    mutableIntStateOf(if (initialQueueSheetValue == SheetValue.Expanded) 1 else 0)
  }

  LaunchedEffect(isQueueExpanded) {
    if (isQueueExpanded) {
      queueOpenCount++
    }
  }

  BackHandler(enabled = isQueueExpanded) {
    coroutineScope.launch {
      sheetState.partialExpand()
    }
  }

  val topGradientColor = lerp(dominantColor, Color(0xFF12121A), 0.25f)
  val midGradientColor = lerp(secondaryColor, Color(0xFF0E0E16), 0.45f)

  val density = LocalDensity.current
  val dismissThresholdPx = with(density) { 64.dp.toPx() }
  var swipeDismissOffsetPx by remember { mutableFloatStateOf(0f) }
  val animatedDismissOffsetPx by animateFloatAsState(
    targetValue = swipeDismissOffsetPx,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "expandedPlayerSwipeDismissOffset",
  )
  val currentOnCollapse by rememberUpdatedState(onCollapse)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        translationY = animatedDismissOffsetPx
      }
      .background(
        Brush.verticalGradient(
          colors = listOf(topGradientColor, midGradientColor, deepBackgroundColor),
        )
      )
      .pointerInput(isQueueExpanded, dismissThresholdPx) {
        if (!isQueueExpanded) {
          detectVerticalDragGestures(
            onDragStart = {
              swipeDismissOffsetPx = 0f
            },
            onVerticalDrag = { change, dragAmount ->
              val nextOffset = (swipeDismissOffsetPx + dragAmount).coerceAtLeast(0f)
              if (nextOffset > 0f || dragAmount > 0f) {
                change.consume()
              }
              swipeDismissOffsetPx = nextOffset
            },
            onDragEnd = {
              val shouldDismiss = swipeDismissOffsetPx >= dismissThresholdPx
              swipeDismissOffsetPx = 0f
              if (shouldDismiss) {
                currentOnCollapse()
              }
            },
            onDragCancel = {
              swipeDismissOffsetPx = 0f
            },
          )
        }
      }
      .pointerInput(Unit) {
        detectTapGestures {}
      } // Consume touches behind overlay
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
          currentTrackId = track.id,
          isPlaying = isPlaying,
          shuffleMode = shuffleMode,
          isExpanded = isQueueExpanded,
          isSheetSettledExpanded = isSheetSettledExpanded,
          queueOpenCount = queueOpenCount,
          peekHeight = sheetPeekHeight,
          onToggleSheet = {
            coroutineScope.launch {
              if (sheetState.currentValue == SheetValue.Expanded) {
                sheetState.partialExpand()
              } else {
                queueOpenCount++
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
              top = 6.dp,
              bottom = 8.dp + sheetPeekHeight,
            ),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
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

          // 2. Large Album Art Card with Favorite Star in Bottom-Right Corner
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center,
          ) {
            Card(
              shape = RoundedCornerShape(24.dp),
              elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
              border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f)),
              modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize()
                .scale(artworkScale)
                .testTag("expanded_player_album_art"),
            ) {
              Box(modifier = Modifier.fillMaxSize()) {
                TrackArtworkBanner(
                  track = track,
                  modifier = Modifier.fillMaxSize(),
                )
                IconButton(
                  onClick = onToggleFavorite,
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ArtworkOverlayButtonBackground, CircleShape)
                    .testTag("expanded_player_favorite_button"),
                ) {
                  Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                    tint = if (isFavorite) accentColor else Color.White,
                    modifier = Modifier.size(24.dp),
                  )
                }
              }
            }
          }

          // 3. Track Title, Artist, Album & Genre Badge
          Column(
            modifier = Modifier.fillMaxWidth(),
          ) {
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
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White.copy(alpha = 0.15f),
            ) {
              Text(
                text = "${track.genre} • ${track.year}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
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
                .background(if (shuffleMode.isEnabled) accentColor.copy(alpha = 0.25f) else Color.Transparent)
                .testTag("expanded_player_shuffle_button"),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Shuffle,
                  contentDescription = shuffleMode.displayName,
                  tint = if (shuffleMode.isEnabled) accentColor else Color.White.copy(alpha = 0.7f),
                  modifier = Modifier.size(22.dp),
                )
                if (shuffleMode == MusicShuffleMode.SMART_SHUFFLE) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                      .size(11.dp)
                      .align(Alignment.TopEnd),
                  )
                }
              }
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

            Surface(
              shape = CircleShape,
              color = if (sleepTimerPreset != SleepTimerPreset.OFF) accentColor.copy(alpha = 0.28f)
              else Color.White.copy(alpha = 0.14f),
              modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onCycleSleepTimer)
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
  currentTrackId: String? = null,
  isPlaying: Boolean,
  shuffleMode: MusicShuffleMode = MusicShuffleMode.OFF,
  isExpanded: Boolean,
  isSheetSettledExpanded: Boolean = isExpanded,
  queueOpenCount: Int = 0,
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

  val focusedQueueIndex = remember(queue, currentQueueIndex, currentTrackId) {
    MusicPlayerEngine.resolveFocusedQueueIndex(
      queue = queue,
      currentQueueIndex = currentQueueIndex,
      currentTrackId = currentTrackId,
    )
  }
  val queueListState = rememberLazyListState(
    initialFirstVisibleItemIndex = focusedQueueIndex.coerceAtLeast(0),
  )

  val density = LocalDensity.current
  val defaultItemHeightPx = with(density) { 64.dp.toPx() }
  val itemSpacingPx = with(density) { 8.dp.toPx() }
  var draggedIndex by remember { mutableStateOf<Int?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  val itemHeightsPx = remember { mutableMapOf<Int, Float>() }
  val currentQueue by rememberUpdatedState(queue)
  val currentOnMoveQueueItem by rememberUpdatedState(onMoveQueueItem)

  LaunchedEffect(isExpanded, isSheetSettledExpanded, queueOpenCount) {
    if ((isExpanded || isSheetSettledExpanded) && focusedQueueIndex in queue.indices && draggedIndex == null) {
      queueListState.scrollToItem(focusedQueueIndex)
    }
  }

  LaunchedEffect(focusedQueueIndex, queue.size) {
    if (focusedQueueIndex in queue.indices && draggedIndex == null) {
      if (!isExpanded || !queueListState.isItemFullyVisible(focusedQueueIndex)) {
        queueListState.scrollToItem(focusedQueueIndex)
      }
    }
  }

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
        .pointerInput(onToggleSheet) {
          detectTapGestures { onToggleSheet() }
        }
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
          .clickable(onClick = onToggleSheet)
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
          text = if (shuffleMode.isEnabled) {
            "${queue.size} songs in queue • ${shuffleMode.displayName}"
          } else {
            "${queue.size} songs in queue"
          },
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
        state = queueListState,
        userScrollEnabled = draggedIndex == null,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("music_queue_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        itemsIndexed(queue, key = { idx, t -> "${idx}_${t.id}" }) { index, item ->
          val isCurrent = index == focusedQueueIndex
          val isDragging = draggedIndex == index
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isCurrent) {
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
              MaterialTheme.colorScheme.surfaceContainer
            },
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
              .semantics { selected = isCurrent }
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
                    .semantics(mergeDescendants = true) {}
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
  currentTrackId: String? = null,
  isPlaying: Boolean,
  accentColor: Color,
  onSelectQueueIndex: (Int) -> Unit,
  onMoveQueueItem: (fromIndex: Int, toIndex: Int) -> Unit,
  onSaveQueueAsPlaylist: () -> Unit,
  onDismiss: () -> Unit,
) {
  val focusedQueueIndex = remember(queue, currentQueueIndex, currentTrackId) {
    MusicPlayerEngine.resolveFocusedQueueIndex(
      queue = queue,
      currentQueueIndex = currentQueueIndex,
      currentTrackId = currentTrackId,
    )
  }
  val queueListState = rememberLazyListState(
    initialFirstVisibleItemIndex = focusedQueueIndex.coerceAtLeast(0),
  )

  val density = LocalDensity.current
  val defaultItemHeightPx = with(density) { 56.dp.toPx() }
  val itemSpacingPx = with(density) { 6.dp.toPx() }
  var draggedIndex by remember { mutableStateOf<Int?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  val itemHeightsPx = remember { mutableMapOf<Int, Float>() }
  val currentQueue by rememberUpdatedState(queue)
  val currentOnMoveQueueItem by rememberUpdatedState(onMoveQueueItem)

  LaunchedEffect(Unit) {
    if (focusedQueueIndex in queue.indices) {
      queueListState.scrollToItem(focusedQueueIndex)
    }
  }

  LaunchedEffect(focusedQueueIndex, queue.size) {
    if (focusedQueueIndex in queue.indices && draggedIndex == null && !queueListState.isItemFullyVisible(focusedQueueIndex)) {
      queueListState.scrollToItem(focusedQueueIndex)
    }
  }

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
          state = queueListState,
          userScrollEnabled = draggedIndex == null,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .testTag("music_queue_list"),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          itemsIndexed(queue, key = { idx, t -> "${idx}_${t.id}" }) { index, item ->
            val isCurrent = index == focusedQueueIndex
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
                .clickable(enabled = draggedIndex == null) { onSelectQueueIndex(index) }
                .semantics { selected = isCurrent }
                .testTag("music_queue_item_$index"),
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
                    .semantics(mergeDescendants = true) {}
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

private enum class TrackDetailsSection(val title: String) {
  SONG_INFO("Song Info"),
  STATS("Stats"),
  FILE_INFO("File Info"),
}

@Composable
private fun TrackDetailsDialog(
  track: MusicTrack,
  palette: AlbumArtPalette,
  playStat: TrackPlayStats = TrackPlayStats(),
  onSelectColor: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()
  val sections = TrackDetailsSection.entries
  val pagerState = rememberPagerState(
    initialPage = 0,
    pageCount = { sections.size },
  )

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
        verticalArrangement = Arrangement.spacedBy(10.dp),
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

        // 3-Tab Segmented Selector (Song Info, Stats, File Info)
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("track_details_tabs"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            sections.forEachIndexed { index, section ->
              val isSelected = pagerState.currentPage == index
              Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                modifier = Modifier
                  .weight(1f)
                  .clip(CircleShape)
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    coroutineScope.launch {
                      pagerState.animateScrollToPage(index)
                    }
                  }
                  .semantics { selected = isSelected }
                  .testTag("track_details_tab_${section.name}"),
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                ) {
                  Text(
                    text = section.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                      MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }
        }

        // Swipeable Paginated Content
        HorizontalPager(
          state = pagerState,
          verticalAlignment = Alignment.Top,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("track_details_pager"),
        ) { pageIndex ->
          val section = sections[pageIndex]
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 172.dp)
              .testTag("track_details_page_${section.name}"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              when (section) {
                TrackDetailsSection.SONG_INFO -> {
                  DetailRow(label = "Artist", value = track.artist)
                  DetailRow(label = "Album", value = track.album)
                  DetailRow(label = "Track #", value = track.trackNumberFormatted, isMono = true)
                  DetailRow(label = "Genre", value = track.genre)
                  DetailRow(label = "Year", value = track.year.toString(), isMono = true)
                  DetailRow(label = "Duration", value = track.durationFormatted, isMono = true)
                }

                TrackDetailsSection.STATS -> {
                  DetailRow(label = "Plays", value = playStat.playCount.toString(), isMono = true)
                  DetailRow(label = "Full Plays", value = playStat.formattedFullPlays(track.durationMs), isMono = true)
                  DetailRow(label = "Playtime", value = playStat.totalPlayTimeFormatted, isMono = true)
                  DetailRow(label = "Skips", value = playStat.skipCount.toString(), isMono = true)
                  DetailRow(label = "Last Played", value = playStat.lastPlayedFormatted, isMono = true)
                }

                TrackDetailsSection.FILE_INFO -> {
                  DetailRow(label = "Format", value = track.formatLabel, isMono = true)
                  DetailRow(label = "Bitrate", value = track.bitrateFormatted, isMono = true)
                  DetailRow(label = "Sample Rate", value = track.sampleRateFormatted, isMono = true)
                  DetailRow(label = "Channels", value = track.channelsLabel)
                  DetailRow(label = "Size", value = track.sizeFormatted, isMono = true)
                  DetailRow(label = "Source", value = track.sourceLabel, isMono = true)
                }
              }
            }
          }
        }

        // Page Indicator Dots
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .testTag("track_details_page_indicator"),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          sections.forEachIndexed { index, section ->
            val isSelected = pagerState.currentPage == index
            Box(
              modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(width = if (isSelected) 18.dp else 6.dp, height = 6.dp)
                .clip(CircleShape)
                .background(
                  if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                  }
                )
                .clickable {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                  }
                }
                .semantics {
                  contentDescription = "Page ${index + 1}: ${section.title}"
                  selected = isSelected
                }
                .testTag("track_details_dot_${section.name}"),
            )
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
