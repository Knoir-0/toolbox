package com.example.toolbox.ui.tools

import com.example.toolbox.ui.theme.toolboxScreenBackground

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.example.toolbox.ui.theme.CircleShape
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
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetScaffold
import com.example.toolbox.ui.theme.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.toolbox.ui.theme.FilledIconButton
import com.example.toolbox.ui.theme.FilledTonalButton
import com.example.toolbox.ui.theme.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import com.example.toolbox.ui.theme.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.example.toolbox.ui.theme.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.toolbox.ui.theme.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import com.example.toolbox.ui.theme.resolveIconButtonShape
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.util.Locale
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

internal data class MusicSelectionController(
  val isSelectionMode: Boolean = false,
  val selectedKeys: Set<String> = emptySet(),
  val onToggleSelection: (String) -> Unit = {},
  val onLongPressSelect: (String) -> Unit = {},
) {
  fun isSelected(key: String): Boolean = key in selectedKeys
}

internal val LocalMusicSelectionController = compositionLocalOf { MusicSelectionController() }

private val ArtworkOverlayButtonBackground = Color.Black.copy(alpha = 0.45f)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
  initialGrantedFolderPaths: Set<String>? = null,
  onRequestFolderSystemAccess: ((MusicFolder, (Boolean) -> Unit) -> Unit)? = null,
  onRequestAddCustomFolder: (((MusicFolder?) -> Unit) -> Unit)? = null,
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
    initialGrantedFolderPaths,
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
      initialGrantedFolderPaths = initialGrantedFolderPaths,
      isInspection = isInspection,
    )
  }

  LaunchedEffect(initialPlayerExpanded) {
    if (initialPlayerExpanded) {
      session.expandFullPlayer()
    }
  }

  // Scoped Storage & Custom Folders State
  val isMusicFolderGranted = session.isMusicFolderGranted
  val pendingPermissionFolderPath = session.pendingPermissionFolderPath
  val customFolders = session.customFolders
  val selectedCustomFolderPath = session.selectedCustomFolderPath
  val selectedCustomFolder = session.selectedCustomFolder
  val isSortSavedForCurrentScope = session.isSortSavedForCurrentScope
  val currentSortSaveLabel = session.currentSortSaveLabel

  var folderAwaitingSystemResult by remember { mutableStateOf<MusicFolder?>(null) }

  val folderAccessLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    val targetFolder = folderAwaitingSystemResult
    folderAwaitingSystemResult = null
    if (targetFolder != null && result.resultCode == Activity.RESULT_OK) {
      val treeUri = result.data?.data
      if (treeUri != null) {
        try {
          val takeFlags = (result.data?.flags ?: 0) and
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
          context.contentResolver.takePersistableUriPermission(
            treeUri,
            if (takeFlags != 0) takeFlags else (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
          )
        } catch (_: Throwable) {}
      }
      session.grantFolderAccess(targetFolder)
    }
  }

  val addCustomFolderLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val treeUri = result.data?.data
      if (treeUri != null) {
        try {
          val takeFlags = (result.data?.flags ?: 0) and
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
          context.contentResolver.takePersistableUriPermission(
            treeUri,
            if (takeFlags != 0) takeFlags else (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
          )
        } catch (_: Throwable) {}
        val resolvedFolder = MusicPlayerEngine.resolveCustomFolderFromTreeUri(treeUri)
        if (resolvedFolder != null) {
          session.addCustomFolder(resolvedFolder)
        }
      }
    }
  }

  fun requestSystemFolderAccess(folder: MusicFolder = MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER) {
    if (onRequestFolderSystemAccess != null) {
      onRequestFolderSystemAccess(folder) { granted ->
        if (granted) {
          session.grantFolderAccess(folder)
        }
      }
      return
    }
    try {
      folderAwaitingSystemResult = folder
      folderAccessLauncher.launch(MusicPlayerEngine.createFolderAccessIntent(folder))
    } catch (_: Throwable) {
      folderAwaitingSystemResult = null
      session.grantFolderAccess(folder)
    }
  }

  fun requestAddCustomFolder() {
    if (onRequestAddCustomFolder != null) {
      onRequestAddCustomFolder { addedFolder ->
        if (addedFolder != null) {
          session.addCustomFolder(addedFolder)
        }
      }
      return
    }
    try {
      addCustomFolderLauncher.launch(MusicPlayerEngine.createAddCustomFolderIntent())
    } catch (_: Throwable) {}
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

  // Sort & Group Order
  val sortOrder = session.sortOrder
  val groupOption = session.groupOption
  val randomSortSeed = session.randomSortSeed

  fun updateSortOrder(order: MusicSortOrder) {
    session.updateSortOrder(order)
    sectionFocusVersion++
  }

  fun updateGroupOption(option: MusicGroupOption) {
    session.updateGroupOption(option)
    sectionFocusVersion++
  }

  fun toggleSaveSortForCurrentScope() {
    session.toggleSaveSortForCurrentScope()
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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

  // Frozen UI snapshots of play statistics so views and playlists do not reorder or update live
  // while music is playing and the user is viewing them.
  val viewPlayStats = session.viewPlayStats
  val homePlayStats = session.homePlayStats

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
  val sortedLibraryTracks = session.sortedLibraryTracks
  val hasDrillDown = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null || selectedPlaylistId != null

  val songsSearchQuery = if (!hasDrillDown && (selectedCustomFolder != null || selectedTab == MusicLibraryTab.SONGS)) searchQuery else ""
  val activeFavoriteSongIdsForFilter = if (showFavoriteSongsOnly) favoriteIds else emptySet()
  val sortedTracks = remember(
    sortedLibraryTracks,
    showFavoriteSongsOnly,
    activeFavoriteSongIdsForFilter,
    songsSearchQuery,
  ) {
    val base = if (showFavoriteSongsOnly) {
      sortedLibraryTracks.filter { it.id in activeFavoriteSongIdsForFilter }
    } else {
      sortedLibraryTracks
    }
    MusicPlayerEngine.filterTracks(base, songsSearchQuery)
  }

  val customFolderTracks = remember(
    sortedLibraryTracks,
    selectedCustomFolder,
    showFavoriteSongsOnly,
    activeFavoriteSongIdsForFilter,
    songsSearchQuery,
  ) {
    val folder = selectedCustomFolder
    if (folder == null) {
      emptyList()
    } else {
      val inFolder = sortedLibraryTracks.filter { track ->
        MusicPlayerEngine.isTrackInFolder(track, folder)
      }
      val base = if (showFavoriteSongsOnly) {
        inFolder.filter { it.id in activeFavoriteSongIdsForFilter }
      } else {
        inFolder
      }
      MusicPlayerEngine.filterTracks(base, songsSearchQuery)
    }
  }

  val baseArtists = session.libraryArtists
  val artistsSearchQuery = if (!hasDrillDown && selectedCustomFolder == null && selectedTab == MusicLibraryTab.ARTISTS) searchQuery else ""
  val activeFavoriteArtistsForFilter = if (showFavoriteArtistsOnly) favoriteArtists else emptySet()
  val allArtists = remember(
    baseArtists,
    showFavoriteArtistsOnly,
    activeFavoriteArtistsForFilter,
    artistsSearchQuery,
    sortOrder,
    viewPlayStats,
    randomSortSeed,
  ) {
    val base = if (showFavoriteArtistsOnly) {
      baseArtists.filter { MusicPlayerEngine.isArtistFavorite(it, activeFavoriteArtistsForFilter) }
    } else {
      baseArtists
    }
    val filtered = MusicPlayerEngine.filterArtists(base, artistsSearchQuery)
    MusicPlayerEngine.sortArtists(
      artists = filtered,
      sortOrder = sortOrder,
      playStats = viewPlayStats,
      randomSeed = randomSortSeed,
    )
  }

  val baseAlbums = session.libraryAlbums
  val albumsSearchQuery = if (!hasDrillDown && selectedCustomFolder == null && selectedTab == MusicLibraryTab.ALBUMS) searchQuery else ""
  val activeFavoriteAlbumsForFilter = if (showFavoriteAlbumsOnly) favoriteAlbums else emptySet()
  val allAlbums = remember(
    baseAlbums,
    showFavoriteAlbumsOnly,
    activeFavoriteAlbumsForFilter,
    albumsSearchQuery,
    sortOrder,
    viewPlayStats,
    randomSortSeed,
  ) {
    val base = if (showFavoriteAlbumsOnly) {
      baseAlbums.filter { MusicPlayerEngine.isAlbumFavorite(it, activeFavoriteAlbumsForFilter) }
    } else {
      baseAlbums
    }
    val filtered = MusicPlayerEngine.filterAlbums(base, albumsSearchQuery)
    MusicPlayerEngine.sortAlbums(
      albums = filtered,
      sortOrder = sortOrder,
      playStats = viewPlayStats,
      randomSeed = randomSortSeed,
    )
  }

  val baseGenres = session.libraryGenres
  val genresSearchQuery = if (!hasDrillDown && selectedCustomFolder == null && selectedTab == MusicLibraryTab.GENRES) searchQuery else ""
  val activeFavoriteGenresForFilter = if (showFavoriteGenresOnly) favoriteGenres else emptySet()
  val allGenres = remember(
    baseGenres,
    showFavoriteGenresOnly,
    activeFavoriteGenresForFilter,
    genresSearchQuery,
    sortOrder,
    viewPlayStats,
    randomSortSeed,
  ) {
    val base = if (showFavoriteGenresOnly) {
      baseGenres.filter { MusicPlayerEngine.isGenreFavorite(it, activeFavoriteGenresForFilter) }
    } else {
      baseGenres
    }
    val filtered = MusicPlayerEngine.filterGenres(base, genresSearchQuery)
    MusicPlayerEngine.sortGenres(
      genres = filtered,
      sortOrder = sortOrder,
      playStats = viewPlayStats,
      randomSeed = randomSortSeed,
    )
  }

  val basePlaylists = session.basePlaylists
  val playlistsSearchQuery = if (!hasDrillDown && selectedCustomFolder == null && selectedTab == MusicLibraryTab.PLAYLISTS) searchQuery else ""
  val allPlaylists = remember(
    basePlaylists,
    playlistsSearchQuery,
    sortOrder,
    viewPlayStats,
    randomSortSeed,
  ) {
    val filtered = MusicPlayerEngine.filterPlaylists(basePlaylists, playlistsSearchQuery)
    if (sortOrder == MusicSortOrder.TITLE_ASC) {
      // Preserve built-in playlists (Favorites, Most Played, Recently Played) at top on default TITLE_ASC sort
      filtered
    } else {
      MusicPlayerEngine.sortPlaylists(
        playlists = filtered,
        sortOrder = sortOrder,
        playStats = viewPlayStats,
        randomSeed = randomSortSeed,
      )
    }
  }

  val homeRefreshGeneration = session.homeRefreshGeneration
  val baseHomePlaylists = session.homePlaylists
  val homeSearchQuery = if (!hasDrillDown && selectedCustomFolder == null && selectedTab == MusicLibraryTab.HOME) searchQuery else ""
  val allHomePlaylists = remember(
    baseHomePlaylists,
    homeSearchQuery,
    sortOrder,
    homePlayStats,
    randomSortSeed,
  ) {
    val filtered = MusicPlayerEngine.filterPlaylists(baseHomePlaylists, homeSearchQuery)
    MusicPlayerEngine.sortPlaylists(
      playlists = filtered,
      sortOrder = sortOrder,
      playStats = homePlayStats,
      randomSeed = randomSortSeed,
      preserveOriginalOnRandom = true,
    )
  }

  // Playback Queue & Active Track State (managed by MusicPlaybackSession so playback continues across tool switches)
  val playbackQueue = session.playbackQueue
  val currentQueueIndex = session.currentQueueIndex
  val currentTrack: MusicTrack? = session.currentTrack

  val isPlaying = session.isPlaying
  var isPlayerExpanded by session::isPlayerExpanded

  // Speed & Sleep Timer & Modals (volume is left to the system media volume)
  val playbackSpeed = session.playbackSpeed
  val sleepTimerPreset = session.sleepTimerPreset
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
  var isSelectionMode by rememberSaveable { mutableStateOf(false) }
  var selectedItemKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
  var showMultiSelectDeleteDialog by remember { mutableStateOf(false) }
  var inlineColorPickerHex by rememberSaveable { mutableStateOf<String?>(null) }
  var showSortMenu by remember { mutableStateOf(false) }
  val crossToolHostState = rememberCrossToolFileHostState()

  fun clearSelection() {
    isSelectionMode = false
    selectedItemKeys = emptySet()
    showMultiSelectDeleteDialog = false
  }

  fun toggleItemSelection(key: String) {
    val updated = if (key in selectedItemKeys) {
      selectedItemKeys - key
    } else {
      selectedItemKeys + key
    }
    selectedItemKeys = updated
    if (updated.isEmpty()) {
      isSelectionMode = false
    }
  }

  fun enterSelectionWithItem(key: String) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    if (!isSelectionMode) {
      isSelectionMode = true
      selectedItemKeys = setOf(key)
    } else {
      toggleItemSelection(key)
    }
  }

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
    MusicPlayerEngine.pauseExternalSystemMediaIfPlaying(context)
    session.playTrackInContext(track, contextTracks)
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun playShuffledContext(
    contextTracks: List<MusicTrack>,
  ) {
    MusicPlayerEngine.pauseExternalSystemMediaIfPlaying(context)
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

  // Observe external Android system media player sessions when built-in player is idle
  val lifecycleOwner = LocalLifecycleOwner.current
  var isSystemMediaListenerEnabled by remember(context, isInspection) {
    mutableStateOf(
      if (isInspection) true else MusicPlayerEngine.isSystemMediaListenerEnabled(context)
    )
  }
  val storagePermissionSettingGranted = com.example.toolbox.ui.main.NavigationPreferencesBridge.isPermissionGranted(
    com.example.toolbox.ui.main.AppPermission.STORAGE
  )
  LaunchedEffect(context, isInspection, storagePermissionSettingGranted) {
    if (!isInspection) {
      session.syncFullFilesPermissionState()
      isSystemMediaListenerEnabled = MusicPlayerEngine.isSystemMediaListenerEnabled(context)
      MusicPlayerEngine.syncExternalSystemMediaSessions(context)
    }
  }
  DisposableEffect(lifecycleOwner, context, isInspection) {
    if (isInspection) {
      onDispose {}
    } else {
      val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          session.syncFullFilesPermissionState()
          isSystemMediaListenerEnabled = MusicPlayerEngine.isSystemMediaListenerEnabled(context)
          MusicPlayerEngine.syncExternalSystemMediaSessions(context)
        }
      }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
      }
    }
  }

  val externalSystemMedia = MusicPlayerEngine.externalSystemMediaInfo
    ?.takeUnless { MusicPlayerEngine.isIgnoredPhoneOrCallSystemMediaInfo(it, context) }
  var isControllingExternalMedia by remember {
    mutableStateOf(
      !isPlaying &&
        externalSystemMedia != null &&
        (externalSystemMedia.isPlaying || currentTrack == null)
    )
  }
  LaunchedEffect(isPlaying, externalSystemMedia?.isPlaying, externalSystemMedia == null, currentTrack == null) {
    if (isPlaying || externalSystemMedia == null) {
      isControllingExternalMedia = false
    } else if (externalSystemMedia.isPlaying || currentTrack == null) {
      isControllingExternalMedia = true
    }
  }
  val activeExternalMedia: SystemMediaPlayingInfo? =
    if (!isPlaying && externalSystemMedia != null && (externalSystemMedia.isPlaying || isControllingExternalMedia || currentTrack == null)) {
      externalSystemMedia
    } else {
      null
    }

  var externalLivePositionMs by remember(activeExternalMedia?.title, activeExternalMedia?.positionMs) {
    mutableLongStateOf(MusicPlayerEngine.currentExternalSystemMediaPositionMs())
  }
  var isDraggingExternalSeek by remember { mutableStateOf(false) }
  var dragExternalSeekFraction by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(
    activeExternalMedia?.isPlaying,
    activeExternalMedia?.positionMs,
    activeExternalMedia?.positionUpdateElapsedRealtimeMs,
  ) {
    val ext = activeExternalMedia ?: return@LaunchedEffect
    externalLivePositionMs = MusicPlayerEngine.currentExternalSystemMediaPositionMs()
    while (ext.isPlaying) {
      delay(250L)
      if (!isDraggingExternalSeek) {
        externalLivePositionMs = MusicPlayerEngine.currentExternalSystemMediaPositionMs()
      }
    }
  }

  // Dynamic Album Art Palette for the active track (or active external Android system media player)
  var activePalette by remember(currentTrack?.id, currentTrack?.artworkPrimaryRgb, session.activePalette) {
    mutableStateOf(
      if (session.currentTrack?.id == currentTrack?.id) session.activePalette
      else MusicPlayerEngine.resolveTrackPalette(currentTrack)
    )
  }

  LaunchedEffect(currentTrack?.id, currentTrack?.album, currentTrack?.artist, currentTrack?.artworkPrimaryRgb, isInspection) {
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

  val displayPalette = activeExternalMedia?.palette ?: activePalette

  val animatedDominant by animateColorAsState(
    targetValue = displayPalette.dominantColor,
    animationSpec = tween(durationMillis = 450),
    label = "dominantAlbumColor",
  )
  val animatedSecondary by animateColorAsState(
    targetValue = displayPalette.secondaryColor,
    animationSpec = tween(durationMillis = 450),
    label = "secondaryAlbumColor",
  )
  val animatedAccent by animateColorAsState(
    targetValue = displayPalette.accentColor,
    animationSpec = tween(durationMillis = 450),
    label = "accentAlbumColor",
  )
  val animatedDarkSurface by animateColorAsState(
    targetValue = displayPalette.darkSurfaceColor,
    animationSpec = tween(durationMillis = 450),
    label = "darkSurfaceAlbumColor",
  )
  val animatedDeepBg by animateColorAsState(
    targetValue = displayPalette.deepBackgroundColor,
    animationSpec = tween(durationMillis = 450),
    label = "deepBgAlbumColor",
  )

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
    enabled = isPlayerExpanded || showQueueSheet || showSavePlaylistDialog || isSelectionMode || isSearchActive || hasDrillDown || onNavigateBack != null
  ) {
    when {
      showSavePlaylistDialog -> showSavePlaylistDialog = false
      showQueueSheet -> showQueueSheet = false
      isPlayerExpanded -> {
        isPlayerExpanded = false
        sectionFocusVersion++
      }
      isSelectionMode -> {
        clearSelection()
      }
      isSearchActive -> {
        isSearchActive = false
        searchQuery = ""
        sectionFocusVersion++
      }
      hasDrillDown -> {
        clearSelection()
        clearDrillDown()
      }
      onNavigateBack != null -> onNavigateBack()
    }
  }

  val selectionController = remember(isSelectionMode, selectedItemKeys) {
    MusicSelectionController(
      isSelectionMode = isSelectionMode,
      selectedKeys = selectedItemKeys,
      onToggleSelection = ::toggleItemSelection,
      onLongPressSelect = ::enterSelectionWithItem,
    )
  }

  val baseBg = MaterialTheme.colorScheme.background
  val ambientTopColor = lerp(baseBg, animatedDominant, 0.22f)
  val ambientMidColor = lerp(baseBg, animatedSecondary, 0.10f)

  CompositionLocalProvider(
    LocalMusicItemActions provides musicItemActions,
    LocalMusicSelectionController provides selectionController,
  ) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(
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
        baseAlbums.find { MusicPlayerEngine.matchesAlbumKey(it, key) }?.let { album ->
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
        selectedCustomFolder != null -> selectedCustomFolder.pillLabel
        else -> "Music"
      }
      val headerSubtext = when {
        activeAlbum != null -> "${activeAlbum.artist} • ${activeAlbum.songCount} songs • ${activeAlbum.totalDurationFormatted}"
        activeArtist != null -> "${activeArtist.albumCount} albums • ${activeArtist.songCount} songs"
        activeGenre != null -> "${activeGenre.songCount} songs • ${activeGenre.artistCount} artists"
        activePlaylist != null -> activePlaylist.summaryText
        selectedCustomFolder != null -> "${selectedCustomFolder.folderPath} • ${customFolderTracks.size} songs"
        !isMusicFolderGranted -> "Storage access locked • Grant access to /Music"
        else -> "${baseArtists.size} artists • ${baseAlbums.size} albums • ${allTracks.size} songs"
      }
      val searchPlaceholder = when {
        activeAlbum != null -> "Search in ${activeAlbum.name}..."
        activeArtist != null -> "Search in ${activeArtist.name}..."
        activeGenre != null -> "Search in ${activeGenre.name}..."
        activePlaylist != null -> "Search in ${activePlaylist.name}..."
        selectedCustomFolder != null -> "Search in ${selectedCustomFolder.pillLabel}..."
        else -> "Search ${selectedTab.displayName.lowercase()}..."
      }

      val currentSelectableKeys: List<String> = remember(
        isSelectionMode,
        activeAlbum,
        activeArtist,
        activeGenre,
        activePlaylist,
        selectedCustomFolder,
        customFolderTracks,
        selectedTab,
        allHomePlaylists,
        allArtists,
        allAlbums,
        sortedTracks,
        allGenres,
        allPlaylists,
      ) {
        if (!isSelectionMode) {
          emptyList()
        } else {
          when {
            activeAlbum != null -> activeAlbum.tracks.map { "song:${it.id}" }
            activeArtist != null -> activeArtist.tracks.map { "song:${it.id}" }
            activeGenre != null -> activeGenre.tracks.map { "song:${it.id}" }
            activePlaylist != null -> activePlaylist.tracks.map { "song:${it.id}" }
            selectedCustomFolder != null -> customFolderTracks.map { "song:${it.id}" }
            else -> when (selectedTab) {
              MusicLibraryTab.HOME -> allHomePlaylists.map { "playlist:${it.id}" }
              MusicLibraryTab.ARTISTS -> allArtists.map { "artist:${it.name}" }
              MusicLibraryTab.ALBUMS -> allAlbums.map { "album:${it.name}|${it.artist}" }
              MusicLibraryTab.SONGS -> sortedTracks.map { "song:${it.id}" }
              MusicLibraryTab.GENRES -> allGenres.map { "genre:${it.name}" }
              MusicLibraryTab.PLAYLISTS -> allPlaylists.map { "playlist:${it.id}" }
            }
          }
        }
      }

      val selectedTracks: List<MusicTrack> = remember(
        selectedItemKeys,
        allTracks,
        baseArtists,
        baseAlbums,
        baseGenres,
        basePlaylists,
        baseHomePlaylists,
      ) {
        if (selectedItemKeys.isEmpty()) {
          emptyList()
        } else {
          val trackById = allTracks.associateBy { it.id }
          val combinedPlaylists = basePlaylists + baseHomePlaylists
          val collected = LinkedHashMap<String, MusicTrack>()
          for (key in selectedItemKeys) {
            when {
              key.startsWith("song:") -> {
                val id = key.removePrefix("song:")
                trackById[id]?.let { collected[it.id] = it }
              }
              key.startsWith("artist:") -> {
                val name = key.removePrefix("artist:")
                baseArtists.find { it.name.equals(name, ignoreCase = true) }?.tracks?.forEach {
                  collected[it.id] = it
                }
              }
              key.startsWith("album:") -> {
                val albumKey = key.removePrefix("album:")
                baseAlbums.find { MusicPlayerEngine.matchesAlbumKey(it, albumKey) }?.tracks?.forEach {
                  collected[it.id] = it
                }
              }
              key.startsWith("genre:") -> {
                val name = key.removePrefix("genre:")
                baseGenres.find { it.name.equals(name, ignoreCase = true) }?.tracks?.forEach {
                  collected[it.id] = it
                }
              }
              key.startsWith("playlist:") -> {
                val id = key.removePrefix("playlist:")
                combinedPlaylists.find { it.id == id }?.tracks?.forEach {
                  collected[it.id] = it
                }
              }
            }
          }
          collected.values.toList()
        }
      }

      val selectedDeletablePlaylistIds: List<String> = remember(selectedItemKeys) {
        selectedItemKeys.mapNotNull { key ->
          if (key.startsWith("playlist:")) {
            val id = key.removePrefix("playlist:")
            id.takeUnless { MusicPlayerEngine.isProtectedPlaylistId(it) }
          } else {
            null
          }
        }
      }

      val hasDeletableTrackOrCollectionSelection = remember(selectedItemKeys) {
        selectedItemKeys.any {
          it.startsWith("song:") || it.startsWith("artist:") || it.startsWith("album:") || it.startsWith("genre:")
        }
      }
      val canDeleteSelection = (hasDeletableTrackOrCollectionSelection && selectedTracks.isNotEmpty()) ||
        selectedDeletablePlaylistIds.isNotEmpty()

      MusicPlayerTopBar(
        title = headerTitle,
        subtext = headerSubtext,
        searchPlaceholder = searchPlaceholder,
        hasDrillDown = hasDrillDown,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        viewMode = viewMode,
        cardsPerRow = cardsPerRow,
        accentTint = animatedDominant,
        isSelectionMode = isSelectionMode,
        selectedCount = selectedItemKeys.size,
        totalSelectableCount = currentSelectableKeys.size,
        canDeleteSelection = canDeleteSelection,
        onToggleSelectAll = {
          if (currentSelectableKeys.isNotEmpty()) {
            selectedItemKeys = if (selectedItemKeys.size >= currentSelectableKeys.size &&
              currentSelectableKeys.all { it in selectedItemKeys }
            ) {
              emptySet()
            } else {
              currentSelectableKeys.toSet()
            }
          }
        },
        onCloseSelection = { clearSelection() },
        onBulkPlay = {
          if (selectedTracks.isNotEmpty()) {
            playTrackInContext(selectedTracks.first(), selectedTracks)
            clearSelection()
          }
        },
        onBulkPlayNext = {
          if (selectedTracks.isNotEmpty()) {
            musicItemActions.onPlayNext(selectedTracks)
            clearSelection()
          }
        },
        onBulkAddToQueue = {
          if (selectedTracks.isNotEmpty()) {
            musicItemActions.onAddToQueue(selectedTracks)
            clearSelection()
          }
        },
        onBulkAddToPlaylist = {
          if (selectedTracks.isNotEmpty()) {
            tracksForAddToPlaylistDialog = selectedTracks
            clearSelection()
          }
        },
        onBulkShare = {
          if (selectedTracks.isNotEmpty()) {
            musicItemActions.onShareTracks(selectedTracks)
            clearSelection()
          }
        },
        onBulkDelete = {
          if (!canDeleteSelection) return@MusicPlayerTopBar
          val singleKey = selectedItemKeys.singleOrNull()
          when {
            singleKey != null && singleKey.startsWith("artist:") -> {
              val name = singleKey.removePrefix("artist:")
              val artist = baseArtists.find { it.name.equals(name, ignoreCase = true) }
              if (artist != null) {
                clearSelection()
                musicItemActions.onDeleteArtist(artist)
              }
            }
            singleKey != null && singleKey.startsWith("album:") -> {
              val albumKey = singleKey.removePrefix("album:")
              val album = baseAlbums.find { MusicPlayerEngine.matchesAlbumKey(it, albumKey) }
              if (album != null) {
                clearSelection()
                musicItemActions.onDeleteAlbum(album)
              }
            }
            singleKey != null && singleKey.startsWith("genre:") -> {
              val name = singleKey.removePrefix("genre:")
              val genre = baseGenres.find { it.name.equals(name, ignoreCase = true) }
              if (genre != null) {
                clearSelection()
                musicItemActions.onDeleteGenre(genre)
              }
            }
            else -> {
              showMultiSelectDeleteDialog = true
            }
          }
        },
        onBackClick = {
          clearSelection()
          clearDrillDown()
        },
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
          if (viewMode == MusicViewMode.LIST) {
            updateViewMode(MusicViewMode.CARD)
            updateCardsPerRow(MIN_MUSIC_CARDS_PER_ROW)
          } else if (cardsPerRow < MAX_MUSIC_CARDS_PER_ROW) {
            updateCardsPerRow(cardsPerRow + 1)
          } else {
            updateViewMode(MusicViewMode.LIST)
          }
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        onSelectViewMode = { mode ->
          updateViewMode(mode)
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        onCardsPerRowChange = { cols ->
          updateCardsPerRow(cols)
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
      )

      val availableGroupOptions = if (hasDrillDown) emptyList() else session.availableGroupOptions

      // 2. Category Tabs (Home, Playlists, Songs, Albums, Artists, Genres + custom folders; reorderable on press and hold) + Pinned Floating [Add Folder] & [Sort/Group] Buttons
      MusicCategoryAndViewBar(
        tabs = tabOrder,
        selectedTab = selectedTab,
        isMusicFolderGranted = isMusicFolderGranted,
        pendingPermissionFolderPath = pendingPermissionFolderPath,
        customFolders = customFolders,
        selectedCustomFolderPath = selectedCustomFolderPath,
        viewMode = viewMode,
        cardsPerRow = cardsPerRow,
        sortOrder = sortOrder,
        groupOption = groupOption,
        availableGroupOptions = availableGroupOptions,
        showSortMenu = showSortMenu,
        isSortSavedForCurrentScope = isSortSavedForCurrentScope,
        sortSaveLabel = currentSortSaveLabel,
        onSelectTab = { tab ->
          if (!isMusicFolderGranted) {
            session.requestMusicFolderPermissionPrompt()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          } else {
            clearSelection()
            clearDrillDown()
            updateTab(tab)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          }
        },
        onRequestMusicFolderPermission = {
          session.requestMusicFolderPermissionPrompt()
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        onSelectCustomFolder = { folder ->
          if (!isMusicFolderGranted) {
            session.requestMusicFolderPermissionPrompt()
          } else {
            clearSelection()
            clearDrillDown()
            session.selectCustomFolder(folder)
            sectionFocusVersion++
          }
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        onRemoveCustomFolder = { folder ->
          clearSelection()
          session.removeCustomFolder(folder)
          sectionFocusVersion++
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        onAddCustomFolder = {
          requestAddCustomFolder()
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
        onSelectGroupOption = { option ->
          updateGroupOption(option)
          showSortMenu = false
        },
        onToggleSaveSortForCurrentScope = {
          toggleSaveSortForCurrentScope()
        },
      )

      // 3. Main Content Area (Scoped Storage Locked/Permission View, Drill-down, Custom Folder, or Selected Tab in List/Card View)
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .pinchToChangeColumns(
            enabled = isMusicFolderGranted,
            isListView = viewMode == MusicViewMode.LIST,
            columnsPerRow = cardsPerRow,
            minColumns = MIN_MUSIC_CARDS_PER_ROW,
            maxColumns = MAX_MUSIC_CARDS_PER_ROW,
            onColumnsChange = { cols ->
              if (viewMode == MusicViewMode.LIST) {
                updateViewMode(MusicViewMode.CARD)
              }
              updateCardsPerRow(cols)
            },
            onSelectListView = {
              if (viewMode != MusicViewMode.LIST) {
                updateViewMode(MusicViewMode.LIST)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              }
            },
          ),
      ) {
        when {
          !isMusicFolderGranted -> {
            if (pendingPermissionFolderPath != null) {
              MusicFolderPermissionRequestView(
                folder = MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER,
                onAllowAccess = {
                  requestSystemFolderAccess(MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER)
                },
              )
            } else {
              MusicLockedStorageStateView()
            }
          }

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
              playStats = viewPlayStats,
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

          selectedCustomFolder != null -> {
            val groupedCustomTracks = remember(customFolderTracks, groupOption) {
              MusicPlayerEngine.groupTracks(customFolderTracks, groupOption).flatMap { it.tracks }
            }
            SongsTabContent(
              tracks = customFolderTracks,
              groupOption = groupOption,
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
                if (groupedCustomTracks.isNotEmpty()) {
                  playTrackInContext(groupedCustomTracks.first(), groupedCustomTracks)
                }
              },
              onShuffleAll = {
                if (groupedCustomTracks.isNotEmpty()) {
                  playShuffledContext(groupedCustomTracks)
                }
              },
              onSelectTrack = { track ->
                playTrackInContext(track, groupedCustomTracks)
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
                  preferredMusicApps = session.preferredMusicApps,
                  onLaunchPreferredApp = { app ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    MusicPlayerEngine.launchPreferredMusicApp(context, app)
                  },
                  onTogglePreferredApp = { app ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    session.togglePreferredMusicApp(app)
                  },
                  onAddPreferredApp = { app ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    session.addPreferredMusicApp(app)
                  },
                  onRemovePreferredApp = { appId ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    session.removePreferredMusicApp(appId)
                  },
                  onRefresh = {
                    session.refreshHomePlaylistsAsync()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  },
                  onPlayAll = {
                    val tracks = if (selectedTab == MusicLibraryTab.HOME && sortOrder == MusicSortOrder.RANDOM && allTracks.isNotEmpty()) {
                      allTracks
                    } else {
                      sortedTracks.ifEmpty {
                        allHomePlaylists.flatMap { it.tracks }.distinctBy { it.id }
                      }
                    }
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
                val groupedAlbumsList = remember(allAlbums, groupOption) {
                  MusicPlayerEngine.groupAlbums(allAlbums, groupOption).flatMap { it.albums }
                }
                AlbumsTabContent(
                  albums = allAlbums,
                  groupOption = groupOption,
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
                    val tracks = groupedAlbumsList.flatMap { it.tracks }.ifEmpty {
                      if (showFavoriteAlbumsOnly) emptyList() else sortedTracks
                    }
                    if (tracks.isNotEmpty()) {
                      playTrackInContext(tracks.first(), tracks)
                    }
                  },
                  onShuffleAll = {
                    if (groupedAlbumsList.isNotEmpty()) {
                      val candidates = if (groupedAlbumsList.size > 1 && currentTrack != null) {
                        groupedAlbumsList.filter { !it.name.equals(currentTrack.album, ignoreCase = true) }.ifEmpty { groupedAlbumsList }
                      } else {
                        groupedAlbumsList
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
                val groupedSongsTracks = remember(sortedTracks, groupOption) {
                  MusicPlayerEngine.groupTracks(sortedTracks, groupOption).flatMap { it.tracks }
                }
                SongsTabContent(
                  tracks = sortedTracks,
                  groupOption = groupOption,
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
                    if (groupedSongsTracks.isNotEmpty()) {
                      playTrackInContext(groupedSongsTracks.first(), groupedSongsTracks)
                    }
                  },
                  onShuffleAll = {
                    if (groupedSongsTracks.isNotEmpty()) {
                      playShuffledContext(groupedSongsTracks)
                    }
                  },
                  onSelectTrack = { track ->
                    playTrackInContext(track, groupedSongsTracks)
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

      // 4. Docked Collapsible Mini-Player Bar (follows current track or external Android media player)
      val fallbackTrackForExternal = currentTrack ?: MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.first()
      if (currentTrack != null || activeExternalMedia != null) {
        val ext = activeExternalMedia
        val extProgress = if (ext != null && ext.durationMs > 0L) {
          (externalLivePositionMs.toFloat() / ext.durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
          0f
        }
        DockedMiniMusicPlayerSection(
          session = session,
          track = currentTrack ?: fallbackTrackForExternal,
          externalMedia = ext,
          externalProgressFraction = extProgress,
          isPlaying = ext?.isPlaying ?: isPlaying,
          isFavorite = currentTrack != null && currentTrack.id in favoriteIds,
          dominantColor = animatedDominant,
          darkSurfaceColor = animatedDarkSurface,
          accentColor = animatedAccent,
          dominantHex = displayPalette.dominantHex,
          onExpand = { isPlayerExpanded = true },
          onToggleFavorite = {
            if (ext == null && currentTrack != null) {
              toggleFavorite(currentTrack.id)
            }
          },
          onPrevious = {
            if (ext != null) {
              MusicPlayerEngine.skipExternalSystemMediaToPrevious(context)
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } else {
              skipToPrevious()
            }
          },
          onPlayPause = {
            if (ext != null) {
              isControllingExternalMedia = true
              MusicPlayerEngine.toggleExternalSystemMediaPlayPause(context)
            } else {
              session.togglePlayPause()
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          },
          onNext = {
            if (ext != null) {
              MusicPlayerEngine.skipExternalSystemMediaToNext(context)
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } else {
              skipToNext(userInitiated = true)
            }
          },
        )
      }
    }

    // 5. Full Expanded Collapsible Music Player Sheet (background follows the album art)
    AnimatedVisibility(
      visible = isPlayerExpanded && (currentTrack != null || activeExternalMedia != null),
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
      val ext = activeExternalMedia
      val effectiveTrack = currentTrack ?: MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.first()
      if (currentTrack != null || ext != null) {
        val currentPositionMs = if (ext != null) externalLivePositionMs else session.currentPositionMs
        val trackDurationMs = if (ext != null) ext.durationMs else session.trackDurationMs
        val isDraggingSeek = if (ext != null) isDraggingExternalSeek else session.isDraggingSeek
        val dragSeekFraction = if (ext != null) dragExternalSeekFraction else session.dragSeekFraction
        val sleepRemainingMs = session.sleepRemainingMs
        val seekFraction = if (isDraggingSeek) {
          dragSeekFraction
        } else if (trackDurationMs > 0L) {
          (currentPositionMs.toFloat() / trackDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
          0f
        }

        ExpandedMusicPlayerOverlay(
          track = effectiveTrack,
          externalMedia = ext,
          isPlaying = ext?.isPlaying ?: isPlaying,
          isFavorite = currentTrack != null && currentTrack.id in favoriteIds,
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
          dominantHex = displayPalette.dominantHex,
          accentHex = displayPalette.accentHex,
          onCollapse = {
            isPlayerExpanded = false
            sectionFocusVersion++
          },
          onToggleFavorite = {
            if (ext == null && currentTrack != null) {
              toggleFavorite(currentTrack.id)
            }
          },
          onSeekChange = { fraction ->
            if (ext != null) {
              isDraggingExternalSeek = true
              dragExternalSeekFraction = fraction.coerceIn(0f, 1f)
            } else {
              session.updateSeekDrag(fraction)
            }
          },
          onSeekFinished = {
            if (ext != null) {
              val targetMs = (dragExternalSeekFraction * ext.durationMs).toLong()
              isDraggingExternalSeek = false
              externalLivePositionMs = targetMs
              MusicPlayerEngine.seekExternalSystemMediaTo(targetMs, context)
            } else {
              session.finishSeekDrag()
            }
          },
          onToggleShuffle = { cycleShuffleMode() },
          onPrevious = {
            if (ext != null) {
              MusicPlayerEngine.skipExternalSystemMediaToPrevious(context)
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } else {
              skipToPrevious()
            }
          },
          onPlayPause = {
            if (ext != null) {
              isControllingExternalMedia = true
              MusicPlayerEngine.toggleExternalSystemMediaPlayPause(context)
            } else {
              session.togglePlayPause()
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          },
          onNext = {
            if (ext != null) {
              MusicPlayerEngine.skipExternalSystemMediaToNext(context)
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } else {
              skipToNext(userInitiated = true)
            }
          },
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
          isSystemMediaListenerEnabled = isSystemMediaListenerEnabled,
          onGrantSystemMediaAccess = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            MusicPlayerEngine.openSystemMediaListenerSettings(context)
          },
          onOpenExternalApp = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            MusicPlayerEngine.openExternalSystemMediaApp(context)
          },
          onTriggerExternalCustomAction = { customAction ->
            isControllingExternalMedia = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            MusicPlayerEngine.triggerExternalSystemMediaCustomAction(customAction, context)
          },
          onNavigateToArtist = {
            if (ext == null && currentTrack != null) {
              isPlayerExpanded = false
              clearDrillDown()
              updateTab(MusicLibraryTab.ARTISTS)
              selectedArtistName = currentTrack.artist
              sectionFocusVersion++
            }
          },
          onNavigateToAlbum = {
            if (ext == null && currentTrack != null) {
              isPlayerExpanded = false
              clearDrillDown()
              updateTab(MusicLibraryTab.ALBUMS)
              selectedAlbumKey = "${currentTrack.album}|${currentTrack.artist}"
              sectionFocusVersion++
            }
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
      var detailPalette by remember(detailTrack.id, detailTrack.artworkPrimaryRgb, activePalette, currentTrack?.id) {
        mutableStateOf(
          if (detailTrack.id == currentTrack?.id) activePalette
          else MusicPlayerEngine.resolveTrackPalette(detailTrack)
        )
      }
      LaunchedEffect(detailTrack.id, detailTrack.album, detailTrack.artist, detailTrack.artworkPrimaryRgb, isInspection) {
        if (!isInspection && !detailTrack.isSampleTrack) {
          detailPalette = MusicPlayerEngine.loadTrackPaletteAsync(detailTrack, sizePx = 360)
        }
      }
      TrackDetailsDialog(
        track = detailTrack,
        palette = detailPalette,
        playStat = session.playStats[detailTrack.id] ?: TrackPlayStats(),
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

    // 18. Multi-Select Bulk Delete Confirmation Dialog
    if (showMultiSelectDeleteDialog) {
      val playlistIdsToDelete = selectedItemKeys.mapNotNull { key ->
        if (key.startsWith("playlist:")) {
          key.removePrefix("playlist:").takeUnless { MusicPlayerEngine.isProtectedPlaylistId(it) }
        } else {
          null
        }
      }
      val tracksToDelete = remember(selectedItemKeys, allTracks, baseArtists, baseAlbums, baseGenres) {
        val trackById = allTracks.associateBy { it.id }
        val collected = LinkedHashMap<String, MusicTrack>()
        for (key in selectedItemKeys) {
          when {
            key.startsWith("song:") -> {
              trackById[key.removePrefix("song:")]?.let { collected[it.id] = it }
            }
            key.startsWith("artist:") -> {
              val name = key.removePrefix("artist:")
              baseArtists.find { it.name.equals(name, ignoreCase = true) }?.tracks?.forEach { collected[it.id] = it }
            }
            key.startsWith("album:") -> {
              val albumKey = key.removePrefix("album:")
              baseAlbums.find { "${it.name}|${it.artist}".equals(albumKey, ignoreCase = true) }?.tracks?.forEach { collected[it.id] = it }
            }
            key.startsWith("genre:") -> {
              val name = key.removePrefix("genre:")
              baseGenres.find { it.name.equals(name, ignoreCase = true) }?.tracks?.forEach { collected[it.id] = it }
            }
          }
        }
        collected.values.toList()
      }
      val selectedItemCount = selectedItemKeys.size
      AlertDialog(
        onDismissRequest = { showMultiSelectDeleteDialog = false },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("multi_select_delete_music_dialog"),
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
                text = if (playlistIdsToDelete.isNotEmpty() && tracksToDelete.isEmpty()) {
                  "Delete ${playlistIdsToDelete.size} Selected Playlist${if (playlistIdsToDelete.size == 1) "" else "s"}?"
                } else {
                  "Delete $selectedItemCount Selected Item${if (selectedItemCount == 1) "" else "s"}?"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        },
        text = {
          Text(
            text = if (playlistIdsToDelete.isNotEmpty() && tracksToDelete.isEmpty()) {
              "This will remove ${playlistIdsToDelete.size} user playlist${if (playlistIdsToDelete.size == 1) "" else "s"} (songs will remain in your library)."
            } else {
              "This will permanently delete ${tracksToDelete.size} song${if (tracksToDelete.size == 1) "" else "s"} from your library."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        },
        confirmButton = {
          Button(
            onClick = {
              playlistIdsToDelete.forEach { deletePlaylist(it) }
              if (tracksToDelete.isNotEmpty()) {
                session.deleteTracks(tracksToDelete)
              }
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              clearSelection()
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError,
            ),
            modifier = Modifier.testTag("confirm_multi_select_delete_music_button"),
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Delete")
          }
        },
        dismissButton = {
          TextButton(
            onClick = { showMultiSelectDeleteDialog = false },
            shape = CircleShape,
          ) {
            Text("Cancel")
          }
        },
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
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  accentTint: Color,
  isSelectionMode: Boolean = false,
  selectedCount: Int = 0,
  totalSelectableCount: Int = 0,
  canDeleteSelection: Boolean = false,
  onToggleSelectAll: () -> Unit = {},
  onCloseSelection: () -> Unit = {},
  onBulkPlay: () -> Unit = {},
  onBulkPlayNext: () -> Unit = {},
  onBulkAddToQueue: () -> Unit = {},
  onBulkAddToPlaylist: () -> Unit = {},
  onBulkShare: () -> Unit = {},
  onBulkDelete: () -> Unit = {},
  onBackClick: () -> Unit,
  onNavigateBack: (() -> Unit)? = null,
  onSearchToggle: () -> Unit,
  onCloseSearch: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onToggleViewMode: () -> Unit,
  onSelectViewMode: (MusicViewMode) -> Unit,
  onCardsPerRowChange: (Int) -> Unit = {},
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
    selectionState = if (isSelectionMode) {
      ToolboxTopBarSelectionState(
        selectedCount = selectedCount,
        totalCount = totalSelectableCount,
        onToggleSelectAll = onToggleSelectAll,
        onCloseSelection = onCloseSelection,
        checkboxTestTag = "action_toggle_select_all",
        actions = {
          var showOverflowMenu by remember { mutableStateOf(false) }
          val hasSelection = selectedCount > 0

          IconButton(
            onClick = onBulkPlay,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_play"),
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Play selected",
              tint = if (hasSelection) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = onBulkAddToPlaylist,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_add_to_playlist"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
              contentDescription = "Add selected to playlist",
              tint = if (hasSelection) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = onBulkShare,
            enabled = hasSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_share"),
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share selected",
              tint = if (hasSelection) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = onBulkDelete,
            enabled = canDeleteSelection,
            modifier = Modifier
              .size(36.dp)
              .testTag("action_bulk_delete"),
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete selected",
              tint = if (canDeleteSelection) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
              modifier = Modifier.size(20.dp),
            )
          }

          Box {
            IconButton(
              onClick = { showOverflowMenu = true },
              modifier = Modifier
                .size(36.dp)
                .testTag("action_selection_overflow"),
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More selection actions",
                modifier = Modifier.size(20.dp),
              )
            }

            DropdownMenu(
              expanded = showOverflowMenu,
              onDismissRequest = { showOverflowMenu = false },
            ) {
              DropdownMenuItem(
                text = { Text("Play") },
                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkPlay()
                },
                modifier = Modifier.testTag("action_selection_play"),
              )
              DropdownMenuItem(
                text = { Text("Play Next") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkPlayNext()
                },
                modifier = Modifier.testTag("action_selection_play_next"),
              )
              DropdownMenuItem(
                text = { Text("Add to Queue") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkAddToQueue()
                },
                modifier = Modifier.testTag("action_selection_add_to_queue"),
              )
              DropdownMenuItem(
                text = { Text("Add to Playlist…") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkAddToPlaylist()
                },
                modifier = Modifier.testTag("action_selection_add_to_playlist"),
              )
              DropdownMenuItem(
                text = { Text("Share") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                enabled = hasSelection,
                onClick = {
                  showOverflowMenu = false
                  onBulkShare()
                },
                modifier = Modifier.testTag("action_selection_share"),
              )
              if (canDeleteSelection) {
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
                    showOverflowMenu = false
                    onBulkDelete()
                  },
                  modifier = Modifier.testTag("action_selection_delete"),
                )
              }
            }
          }
        },
      )
    } else {
      null
    },
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
      ToolboxViewModeCycleButton(
        isListView = viewMode == MusicViewMode.LIST,
        columnsPerRow = cardsPerRow,
        minColumns = MIN_MUSIC_CARDS_PER_ROW,
        maxColumns = MAX_MUSIC_CARDS_PER_ROW,
        onSelectListView = { onSelectViewMode(MusicViewMode.LIST) },
        onSelectGridColumns = { cols ->
          onSelectViewMode(MusicViewMode.CARD)
          onCardsPerRowChange(cols)
        },
        buttonTestTag = "music_view_mode_toggle",
        countTestTag = "music_columns_per_row_count",
        gridContentDescription = "$cardsPerRow cards per row",
      )
    },
  )
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
  isMusicFolderGranted: Boolean = true,
  pendingPermissionFolderPath: String? = null,
  customFolders: List<MusicFolder> = emptyList(),
  selectedCustomFolderPath: String? = null,
  viewMode: MusicViewMode = MusicViewMode.LIST,
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  sortOrder: MusicSortOrder = MusicSortOrder.TITLE_ASC,
  groupOption: MusicGroupOption = MusicGroupOption.NONE,
  availableGroupOptions: List<MusicGroupOption> = emptyList(),
  showSortMenu: Boolean = false,
  isSortSavedForCurrentScope: Boolean = false,
  sortSaveLabel: String = "Save for home",
  onSelectTab: (MusicLibraryTab) -> Unit,
  onRequestMusicFolderPermission: () -> Unit = {},
  onSelectCustomFolder: (MusicFolder) -> Unit = {},
  onRemoveCustomFolder: (MusicFolder) -> Unit = {},
  onAddCustomFolder: () -> Unit = {},
  onMoveTab: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
  onCardsPerRowChange: (Int) -> Unit = {},
  onOpenSortMenu: () -> Unit = {},
  onDismissSortMenu: () -> Unit = {},
  onSelectSortOrder: (MusicSortOrder) -> Unit = {},
  onSelectGroupOption: (MusicGroupOption) -> Unit = {},
  onToggleSaveSortForCurrentScope: () -> Unit = {},
) {
  val density = LocalDensity.current
  val haptic = LocalHapticFeedback.current
  val scrollState = rememberScrollState()

  var prevCustomFolderCount by remember { mutableIntStateOf(customFolders.size) }
  LaunchedEffect(customFolders.size, scrollState.maxValue) {
    if (customFolders.size > prevCustomFolderCount && scrollState.maxValue > 0) {
      scrollState.scrollTo(scrollState.maxValue)
      prevCustomFolderCount = customFolders.size
    } else if (customFolders.size < prevCustomFolderCount) {
      prevCustomFolderCount = customFolders.size
    }
  }

  val currentTabs by rememberUpdatedState(tabs)
  val currentIsGranted by rememberUpdatedState(isMusicFolderGranted)
  val currentOnMoveTab by rememberUpdatedState(onMoveTab)
  val tabBounds = remember { mutableMapOf<MusicLibraryTab, Rect>() }
  var barCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

  var draggedTab by remember { mutableStateOf<MusicLibraryTab?>(null) }
  var dragPointerOffset by remember { mutableStateOf(Offset.Zero) }
  var grabOffsetFromCenter by remember { mutableStateOf(Offset.Zero) }
  var draggedTabSize by remember { mutableStateOf<IntSize?>(null) }

  val maxHitSlopPx = with(density) { 24.dp.toPx() }
  val leftEdgeScrollZonePx = with(density) { 36.dp.toPx() }
  val rightEdgeScrollZonePx = with(density) { 108.dp.toPx() }
  val sortButtonExcludeZonePx = with(density) { 88.dp.toPx() }
  val edgeScrollStepPx = with(density) { 10.dp.toPx() }

  ToolboxPillNavigationBar(
    barTestTag = "music_category_bar",
    scrollState = scrollState,
    scrollEnabled = draggedTab == null,
    floatingButtonsCount = 2,
    backgroundColor = Color.Unspecified,
    modifier = Modifier
      .onGloballyPositioned { coords ->
        barCoordinates = coords
      }
      .pointerInput(isMusicFolderGranted) {
        if (!isMusicFolderGranted) return@pointerInput
        detectDragGesturesAfterLongPress(
          onDragStart = { startOffset ->
            if (!currentIsGranted) return@detectDragGesturesAfterLongPress
            // The "Home" pill is fixed to the front of the list and cannot be dragged
            if (tabBounds[MusicLibraryTab.HOME]?.contains(startOffset) == true) {
              return@detectDragGesturesAfterLongPress
            }
            val reorderableTabs = currentTabs.filterNot { it == MusicLibraryTab.HOME }
            val directHit = reorderableTabs.firstOrNull { tab ->
              tabBounds[tab]?.contains(startOffset) == true
            }
            val barWidth = (barCoordinates?.size?.width ?: 0).toFloat()
            if (directHit == null && barWidth > 0f && startOffset.x >= barWidth - sortButtonExcludeZonePx) {
              return@detectDragGesturesAfterLongPress
            }
            val hitTab = directHit ?: reorderableTabs.minByOrNull { tab ->
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
              val homeRect = tabBounds[MusicLibraryTab.HOME]
              val closerToHome = homeRect != null &&
                abs(startOffset.x - homeRect.center.x) < abs(startOffset.x - rect.center.x)
              dx <= maxHitSlopPx && !closerToHome
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
            if (fromIndex > 0 && currentTabs.size > 2) {
              // Target index is constrained to indices >= 1 (after the fixed "Home" pill at the front)
              val targetIndex = (1..currentTabs.lastIndex).minByOrNull { idx ->
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
    pills = {
      tabs.forEachIndexed { index, tab ->
        val isSelected = if (!isMusicFolderGranted) {
          pendingPermissionFolderPath != null && index == 0
        } else {
          selectedCustomFolderPath == null && tab == selectedTab
        }
        val isBeingDragged = tab == draggedTab
        ToolboxPillNavigationItem(
          selected = isSelected,
          onClick = {
            if (draggedTab == null) {
              if (!isMusicFolderGranted) {
                onRequestMusicFolderPermission()
              } else {
                onSelectTab(tab)
              }
            }
          },
          label = tab.displayName,
          leadingIconContent = {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .semantics(mergeDescendants = true) {
                  if (draggedTab == null) {
                    onClick {
                      if (!isMusicFolderGranted) {
                        onRequestMusicFolderPermission()
                      } else {
                        onSelectTab(tab)
                      }
                      true
                    }
                  }
                }
                .testTag(
                  if (isMusicFolderGranted) {
                    "music_tab_unlocked_icon_${tab.name}"
                  } else {
                    "music_tab_lock_icon_${tab.name}"
                  }
                ),
            ) {
              Icon(
                imageVector = if (isMusicFolderGranted) tab.iconVector() else Icons.Default.Lock,
                contentDescription = if (isMusicFolderGranted) {
                  tab.displayName
                } else {
                  "Unlock Music folder for ${tab.displayName}"
                },
                modifier = Modifier
                  .size(16.dp)
                  .then(
                    if (!isMusicFolderGranted && index == 0) {
                      Modifier.testTag("folder_lock_icon_Music")
                    } else {
                      Modifier
                    }
                  ),
              )
            }
          },
          testTag = "music_tab_${tab.name}",
          modifier = Modifier
            .onGloballyPositioned { coords ->
              val barCoords = barCoordinates
              if (barCoords != null && barCoords.isAttached && coords.isAttached) {
                tabBounds[tab] = barCoords.localBoundingBoxOf(coords, clipBounds = false)
              }
            }
            .graphicsLayer {
              alpha = when {
                isBeingDragged -> 0.3f
                !isMusicFolderGranted -> 0.72f
                else -> 1f
              }
            },
        )
      }

      // User-added Custom Folder navigation pills with 'x' removal button on the right
      customFolders.forEach { folder ->
        val isSelectedFolder = isMusicFolderGranted &&
          selectedCustomFolderPath != null &&
          selectedCustomFolderPath.equals(folder.folderPath, ignoreCase = true)
        Box(
          modifier = Modifier.testTag("folder_pill_${folder.folderName}"),
        ) {
          ToolboxPillNavigationItem(
            selected = isSelectedFolder,
            onClick = {
              if (draggedTab == null) {
                onSelectCustomFolder(folder)
              }
            },
            leadingIconContent = {
              Icon(
                imageVector = if (isMusicFolderGranted) Icons.Default.Folder else Icons.Default.Lock,
                contentDescription = if (isMusicFolderGranted) {
                  "${folder.pillLabel} folder"
                } else {
                  "Unlock ${folder.pillLabel} folder"
                },
                modifier = Modifier
                  .size(16.dp)
                  .testTag("folder_pill_icon_${folder.folderName}"),
              )
            },
            label = folder.pillLabel,
            trailingIcon = {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable(enabled = draggedTab == null) {
                    onRemoveCustomFolder(folder)
                  }
                  .testTag("folder_delete_icon_${folder.folderName}"),
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Remove ${folder.pillLabel} folder",
                  modifier = Modifier
                    .size(16.dp)
                    .testTag("music_custom_folder_remove_${folder.folderName}"),
                )
              }
            },
            testTag = "music_folder_pill_${folder.folderName}",
            modifier = Modifier
              .graphicsLayer {
                alpha = if (!isMusicFolderGranted) 0.72f else 1f
              },
          )
        }
      }
    },
    floatingControls = {
      Surface(
        onClick = onAddCustomFolder,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
          .size(34.dp)
          .testTag("music_add_folder_button"),
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.testTag("add_folder_button"),
        ) {
          Icon(
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = "Add folder",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      val hasGroupOptions = availableGroupOptions.isNotEmpty()
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
              imageVector = if (hasGroupOptions) Icons.Default.Tune else Icons.AutoMirrored.Filled.Sort,
              contentDescription = if (hasGroupOptions) "Sort and group options" else "Sort tracks",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
        }

        DropdownMenu(
          expanded = showSortMenu,
          onDismissRequest = onDismissSortMenu,
          modifier = Modifier
            .then(if (hasGroupOptions) Modifier.widthIn(min = 320.dp, max = 390.dp) else Modifier)
            .testTag("music_sort_menu"),
        ) {
          if (hasGroupOptions) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 4.dp),
            ) {
              // Left panel: GROUP BY
              Column(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
              ) {
                Text(
                  text = "GROUP BY",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
                availableGroupOptions.forEach { option ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = option.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (option == groupOption) FontWeight.Bold else FontWeight.Normal,
                        color = if (option == groupOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                      )
                    },
                    trailingIcon = if (option == groupOption) {
                      {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(18.dp),
                        )
                      }
                    } else null,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    onClick = { onSelectGroupOption(option) },
                    modifier = Modifier.testTag("music_group_option_${option.name.lowercase(Locale.ROOT)}"),
                  )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(
                  color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                )

                // Bottom-left checkbox to save sort/group for this specific section or folder
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleSaveSortForCurrentScope() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("music_save_sort_for_section"),
                ) {
                  Checkbox(
                    checked = isSortSavedForCurrentScope,
                    onCheckedChange = { onToggleSaveSortForCurrentScope() },
                    colors = CheckboxDefaults.colors(
                      checkedColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                      .size(28.dp)
                      .testTag("music_save_sort_for_section_checkbox"),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = sortSaveLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSortSavedForCurrentScope) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSortSavedForCurrentScope) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }

              // Vertical divider between Group By and Sort By panels
              VerticalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 6.dp),
              )

              // Right panel: SORT BY
              Column(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
              ) {
                Text(
                  text = "SORT BY",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
                MusicSortOrder.entries.forEach { option ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = option.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (option == sortOrder) FontWeight.Bold else FontWeight.Normal,
                        color = if (option == sortOrder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                      )
                    },
                    trailingIcon = if (option == sortOrder) {
                      {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(18.dp),
                        )
                      }
                    } else null,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    onClick = { onSelectSortOrder(option) },
                    modifier = Modifier.testTag("music_sort_option_${option.name.lowercase(Locale.ROOT)}"),
                  )
                }
              }
            }
          } else {
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
                modifier = Modifier.testTag("music_sort_option_${option.name.lowercase(Locale.ROOT)}"),
              )
            }

            HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onToggleSaveSortForCurrentScope() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("music_save_sort_for_section"),
            ) {
              Checkbox(
                checked = isSortSavedForCurrentScope,
                onCheckedChange = { onToggleSaveSortForCurrentScope() },
                colors = CheckboxDefaults.colors(
                  checkedColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                  .size(28.dp)
                  .testTag("music_save_sort_for_section_checkbox"),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = sortSaveLabel,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSortSavedForCurrentScope) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSortSavedForCurrentScope) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
              )
            }
          }
        }
      }
    },
    overlayContent = {
      val activeDraggedTab = draggedTab
      if (activeDraggedTab != null && barCoordinates != null) {
        val desiredCenter = dragPointerOffset - grabOffsetFromCenter
        val fallbackWidthPx = with(density) { 96.dp.toPx() }
        val fallbackHeightPx = with(density) { 32.dp.toPx() }
        val widthPx = draggedTabSize?.width?.toFloat() ?: fallbackWidthPx
        val heightPx = draggedTabSize?.height?.toFloat() ?: fallbackHeightPx
        val isSelected = selectedCustomFolderPath == null && activeDraggedTab == selectedTab

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
          ToolboxPillNavigationItem(
            selected = isSelected,
            onClick = {},
            leadingIcon = activeDraggedTab.iconVector(),
            label = activeDraggedTab.displayName,
          )
        }
      }
    },
  )
}

/**
 * View displayed in the Music Player when scoped storage access is locked (permission to `/Music`
 * has not been granted yet), styled to match the Gallery and Audio Recorder tools.
 */
@Composable
private fun MusicLockedStorageStateView(
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("locked_storage_state"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp),
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Storage access is locked",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Tap the lock icon on a navigation pill above to grant permission to access files in /Music.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
  }
}

/**
 * View displayed when the user taps a locked navigation pill or its lock icon in the Music Player,
 * asking for permission to access files in `/Music` and launching the Android system directory
 * access prompt when the user taps "Allow", styled to match the Gallery and Audio Recorder tools.
 */
@Composable
private fun MusicFolderPermissionRequestView(
  folder: MusicFolder,
  onAllowAccess: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("folder_permission_view"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp),
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Allow access to files in ${folder.folderPath}?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("folder_permission_title"),
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Music Player needs permission to access files in ${folder.folderPath} (${folder.folderName}) to browse and play songs in this location.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("folder_permission_message"),
      )
      Spacer(modifier = Modifier.height(14.dp))
      Button(
        onClick = onAllowAccess,
        shape = CircleShape,
        modifier = Modifier.testTag("allow_folder_permission_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Allow")
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun HomeTabContent(
  playlists: List<MusicPlaylist>,
  viewMode: MusicViewMode,
  cardsPerRow: Int = DEFAULT_MUSIC_CARDS_PER_ROW,
  currentTrackId: String?,
  isPlaying: Boolean,
  refreshGeneration: Int = 0,
  preferredMusicApps: List<PreferredMusicAppLink> = emptyList(),
  onLaunchPreferredApp: (PreferredMusicAppLink) -> Unit = {},
  onTogglePreferredApp: (PreferredMusicAppLink) -> Unit = {},
  onAddPreferredApp: (PreferredMusicAppLink) -> Unit = {},
  onRemovePreferredApp: (String) -> Unit = {},
  onRefresh: () -> Unit,
  onPlayAll: () -> Unit,
  onShuffleAll: () -> Unit,
  onSelectPlaylist: (MusicPlaylist) -> Unit,
  onPlayPlaylist: (MusicPlaylist) -> Unit,
) {
  var showAddMusicAppDialog by rememberSaveable { mutableStateOf(false) }
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

  Box(
    modifier = Modifier
      .fillMaxSize()
      .testTag("music_home_tab_content"),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
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
          .weight(1f)
          .fillMaxWidth()
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
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 88.dp),
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
            val isActive = playlist.containsTrack(currentTrackId)
            val selection = LocalMusicSelectionController.current
            val selectionKey = "playlist:${playlist.id}"
            val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isItemSelected || isActive) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
              ),
              border = BorderStroke(
                width = if (isItemSelected) 1.5.dp else 1.dp,
                color = if (isItemSelected) MaterialTheme.colorScheme.primary
                else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              ),
              modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                  onClick = {
                    if (selection.isSelectionMode) {
                      selection.onToggleSelection(selectionKey)
                    } else {
                      onSelectPlaylist(playlist)
                    }
                  },
                  onLongClick = { selection.onLongPressSelect(selectionKey) },
                )
                .semantics { selected = isItemSelected || isActive }
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
                if (selection.isSelectionMode) {
                  Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center,
                  ) {
                    Checkbox(
                      checked = isItemSelected,
                      onCheckedChange = { selection.onToggleSelection(selectionKey) },
                      modifier = Modifier.testTag("select_checkbox_$selectionKey"),
                    )
                  }
                } else {
                  TrackArtworkThumbnail(
                    track = playlist.representativeTrack,
                    size = 56.dp,
                    shape = RoundedCornerShape(12.dp),
                  )
                }
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
          contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 88.dp),
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
            val isActive = playlist.containsTrack(currentTrackId)
            val selection = LocalMusicSelectionController.current
            val selectionKey = "playlist:${playlist.id}"
            val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isItemSelected || isActive) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
              ),
              border = BorderStroke(
                width = if (isItemSelected) 1.5.dp else 1.dp,
                color = if (isItemSelected) MaterialTheme.colorScheme.primary
                else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              ),
              modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                  onClick = {
                    if (selection.isSelectionMode) {
                      selection.onToggleSelection(selectionKey)
                    } else {
                      onSelectPlaylist(playlist)
                    }
                  },
                  onLongClick = { selection.onLongPressSelect(selectionKey) },
                )
                .semantics { selected = isItemSelected || isActive }
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
                  if (selection.isSelectionMode) {
                    Checkbox(
                      checked = isItemSelected,
                      onCheckedChange = { selection.onToggleSelection(selectionKey) },
                      modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .testTag("select_checkbox_$selectionKey"),
                    )
                  } else {
                    val timeBadgeShape = if (currentHexagonIconButtons()) HorizontalHexagonShape else RoundedCornerShape(8.dp)
                    Surface(
                      shape = timeBadgeShape,
                      color = Color.Black.copy(alpha = 0.55f),
                      modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .testTag("playlist_time_badge_${playlist.name}"),
                    ) {
                      Text(
                        text = playlist.totalDurationFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      )
                    }
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

    // Floating 28.dp Preferred Music Apps Bar fixed to the bottom of the Home view
    // above the navigation panel, matching the exact positioning and dimensions of Notes and To-Do
    HomePreferredMusicAppsFloatingBar(
      preferredApps = preferredMusicApps,
      onOpenAddAppDialog = { showAddMusicAppDialog = true },
      onLaunchApp = onLaunchPreferredApp,
      onRemoveApp = onRemovePreferredApp,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
    )

    if (showAddMusicAppDialog) {
      PreferredMusicAppPickerDialog(
        selectedApps = preferredMusicApps,
        onToggleApp = onTogglePreferredApp,
        onAddCustomApp = { customApp ->
          onAddPreferredApp(customApp)
        },
        onDismiss = { showAddMusicAppDialog = false },
      )
    }
  }
}

/**
 * Masks the left and right ends of a horizontally scrollable pill row so overflowing items
 * smoothly fade out at the ends when there are too many apps to fit inside the rounded pill bar.
 *
 * Place this modifier BEFORE `.horizontalScroll(scrollState)` so the fade masks remain anchored
 * to the viewport ends while the content scrolls.
 */
private fun Modifier.fadingHorizontalScrollEdges(
  scrollState: ScrollState,
  fadeWidth: Dp = 24.dp,
): Modifier = this
  .graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
  }
  .drawWithContent {
    drawContent()
    val maxScroll = scrollState.maxValue
    if (maxScroll > 0 && size.width > 0f) {
      val fadeWidthPx = fadeWidth.toPx().coerceAtMost(size.width * 0.35f)
      if (fadeWidthPx > 0f) {
        val leftFadeFraction = (scrollState.value.toFloat() / fadeWidthPx).coerceIn(0f, 1f)
        val rightFadeFraction = ((maxScroll - scrollState.value).toFloat() / fadeWidthPx).coerceIn(0f, 1f)
        val leftStop = ((fadeWidthPx * leftFadeFraction) / size.width).coerceIn(0f, 0.45f)
        val rightStop = (1f - ((fadeWidthPx * rightFadeFraction) / size.width)).coerceIn(0.55f, 1f)

        drawRect(
          brush = Brush.horizontalGradient(
            0f to if (leftFadeFraction > 0.01f) Color.Transparent else Color.Black,
            leftStop to Color.Black,
            rightStop to Color.Black,
            1f to if (rightFadeFraction > 0.01f) Color.Transparent else Color.Black,
          ),
          blendMode = BlendMode.DstIn,
        )
      }
    }
  }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomePreferredMusicAppsFloatingBar(
  preferredApps: List<PreferredMusicAppLink>,
  onOpenAddAppDialog: () -> Unit,
  onLaunchApp: (PreferredMusicAppLink) -> Unit,
  onRemoveApp: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  val hexagonEnabled = currentHexagonIconButtons()

  Box(modifier = modifier) {
    Surface(
      shape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      shadowElevation = 4.dp,
      border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("music_home_floating_app_bar"),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Small 28.dp circular plus icon button on the left, styled similar to the Notes app
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onOpenAddAppDialog)
            .testTag("music_home_add_app_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add preferred music app",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(18.dp),
          )
        }

        if (preferredApps.isEmpty()) {
          Row(
            modifier = Modifier
              .weight(1f)
              .clip(if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(20.dp))
              .clickable(onClick = onOpenAddAppDialog)
              .padding(vertical = 10.dp)
              .testTag("music_home_empty_apps_prompt"),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Add preferred music app...",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        } else {
          Row(
            modifier = Modifier
              .weight(1f)
              .padding(vertical = 4.dp)
              .fadingHorizontalScrollEdges(
                scrollState = scrollState,
                fadeWidth = 28.dp,
              )
              .horizontalScroll(scrollState)
              .testTag("music_home_apps_scroll_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            preferredApps.forEach { app ->
              val appAccent = Color(0xFF000000 or (app.accentRgb.toLong() and 0xFFFFFFL))
              val chipShape = if (hexagonEnabled) HorizontalHexagonShape else CircleShape
              Surface(
                shape = chipShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                border = BorderStroke(
                  width = 1.dp,
                  color = appAccent.copy(alpha = 0.45f),
                ),
                modifier = Modifier
                  .clip(chipShape)
                  .combinedClickable(
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onRemoveApp(app.id) },
                  )
                  .testTag("music_home_app_chip_${app.id}"),
              ) {
                Row(
                  modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(18.dp)
                      .clip(CircleShape)
                      .background(appAccent),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = app.label.trim().take(1).uppercase(),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (appAccent.red * 0.299f + appAccent.green * 0.587f + appAccent.blue * 0.114f > 0.6f) {
                        Color(0xFF101018)
                      } else {
                        Color.White
                      },
                    )
                  }
                  Text(
                    text = app.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PreferredMusicAppPickerDialog(
  selectedApps: List<PreferredMusicAppLink>,
  onToggleApp: (PreferredMusicAppLink) -> Unit,
  onAddCustomApp: (PreferredMusicAppLink) -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var isCustomPickerMode by rememberSaveable { mutableStateOf(false) }
  var isSystemMediaListenerEnabled by remember(context, isInspection) {
    mutableStateOf(
      if (isInspection) true else MusicPlayerEngine.isSystemMediaListenerEnabled(context)
    )
  }
  DisposableEffect(lifecycleOwner, context, isInspection) {
    if (isInspection) {
      onDispose {}
    } else {
      val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          isSystemMediaListenerEnabled = MusicPlayerEngine.isSystemMediaListenerEnabled(context)
          MusicPlayerEngine.syncExternalSystemMediaSessions(context)
        }
      }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
      }
    }
  }

  val popularApps = remember(context, isInspection) {
    MusicPlayerEngine.discoverPopularStreamingApps(
      context = if (isInspection) null else context,
    )
  }
  val customInstalledApps = remember(context, isInspection, isCustomPickerMode) {
    if (!isCustomPickerMode) {
      emptyList()
    } else {
      MusicPlayerEngine.discoverCustomPickerInstalledApps(
        context = if (isInspection) null else context,
      )
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("music_preferred_app_picker_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (isCustomPickerMode) {
          IconButton(
            onClick = {
              isCustomPickerMode = false
            },
            modifier = Modifier
              .size(32.dp)
              .testTag("music_custom_picker_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to streaming apps",
            )
          }
        } else {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Headphones,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(20.dp),
            )
          }
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isCustomPickerMode) "Custom App" else "Preferred Music Apps",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = if (isCustomPickerMode) {
              "Choose any installed app"
            } else {
              "Tap to add or remove apps from the Home pill bar"
            },
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
        if (!isSystemMediaListenerEnabled && !isCustomPickerMode) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("music_app_picker_listener_permission_banner"),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
            border = BorderStroke(
              width = 1.dp,
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "Grant Notification Access to show playing songs in the Music Player",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .weight(1f)
                  .padding(end = 8.dp),
              )
              OutlinedButton(
                onClick = {
                  MusicPlayerEngine.openSystemMediaListenerSettings(context)
                },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier
                  .height(26.dp)
                  .testTag("music_app_picker_grant_access_button"),
              ) {
                Text(
                  text = "Grant Access",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          }
        }

        if (!isCustomPickerMode) {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 320.dp)
              .testTag("music_app_picker_list"),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            items(popularApps, key = { it.id }) { app ->
              val isAdded = selectedApps.any {
                it.id == app.id || (app.packageName.isNotBlank() && it.packageName == app.packageName)
              }
              val appAccent = Color(0xFF000000 or (app.accentRgb.toLong() and 0xFFFFFFL))
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isAdded) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
                border = BorderStroke(
                  width = if (isAdded) 1.5.dp else 1.dp,
                  color = if (isAdded) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                  },
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable { onToggleApp(app) }
                  .testTag("music_app_picker_option_${app.id}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(appAccent),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = app.label.trim().take(1).uppercase(),
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color = if (appAccent.red * 0.299f + appAccent.green * 0.587f + appAccent.blue * 0.114f > 0.6f) {
                        Color(0xFF101018)
                      } else {
                        Color.White
                      },
                    )
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = app.label,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = app.packageName.ifBlank { app.webUrl },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                  Icon(
                    imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (isAdded) "Added" else "Add ${app.label}",
                    tint = if (isAdded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }

            // Custom App option at the end of the installed streaming apps list
            item(key = "custom_app_picker_option") {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(
                  width = 1.dp,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable {
                    isCustomPickerMode = true
                  }
                  .testTag("music_app_picker_custom_option"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Default.Apps,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Custom App",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                      text = "Choose any installed app",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Open custom app picker",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }
          }
        } else {
          // Custom Picker View: Choose any installed app on the device
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 320.dp)
              .testTag("music_custom_installed_apps_list"),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            items(customInstalledApps, key = { it.id }) { app ->
              val isAdded = selectedApps.any {
                it.id == app.id || (app.packageName.isNotBlank() && it.packageName == app.packageName)
              }
              val appAccent = Color(0xFF000000 or (app.accentRgb.toLong() and 0xFFFFFFL))
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isAdded) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
                border = BorderStroke(
                  width = if (isAdded) 1.5.dp else 1.dp,
                  color = if (isAdded) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                  },
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable {
                    onToggleApp(app)
                  }
                  .testTag("music_custom_installed_option_${app.id}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(appAccent),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = app.label.trim().take(1).uppercase(),
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color = if (appAccent.red * 0.299f + appAccent.green * 0.587f + appAccent.blue * 0.114f > 0.6f) {
                        Color(0xFF101018)
                      } else {
                        Color.White
                      },
                    )
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = app.label,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = app.packageName,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                  Icon(
                    imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (isAdded) "Added" else "Add ${app.label}",
                    tint = if (isAdded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier.testTag("music_app_picker_done_button"),
      ) {
        Text("Done")
      }
    },
  )
}

@OptIn(ExperimentalFoundationApi::class)
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
          val selection = LocalMusicSelectionController.current
          val selectionKey = "artist:${artist.name}"
          val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isItemSelected || isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = if (isItemSelected) 1.5.dp else 1.dp,
              color = if (isItemSelected) MaterialTheme.colorScheme.primary
              else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .combinedClickable(
                onClick = {
                  if (selection.isSelectionMode) {
                    selection.onToggleSelection(selectionKey)
                  } else {
                    onSelectArtist(artist)
                  }
                },
                onLongClick = { selection.onLongPressSelect(selectionKey) },
              )
              .semantics { selected = isItemSelected || isActive }
              .testTag("artist_item_${artist.name}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              if (selection.isSelectionMode) {
                Box(
                  modifier = Modifier.size(52.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Checkbox(
                    checked = isItemSelected,
                    onCheckedChange = { selection.onToggleSelection(selectionKey) },
                    modifier = Modifier.testTag("select_checkbox_$selectionKey"),
                  )
                }
              } else {
                TrackArtworkThumbnail(
                  track = artist.representativeTrack,
                  size = 52.dp,
                  shape = CircleShape,
                )
              }
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
          val selection = LocalMusicSelectionController.current
          val selectionKey = "artist:${artist.name}"
          val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isItemSelected || isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = if (isItemSelected) 1.5.dp else 1.dp,
              color = if (isItemSelected) MaterialTheme.colorScheme.primary
              else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .combinedClickable(
                onClick = {
                  if (selection.isSelectionMode) {
                    selection.onToggleSelection(selectionKey)
                  } else {
                    onSelectArtist(artist)
                  }
                },
                onLongClick = { selection.onLongPressSelect(selectionKey) },
              )
              .semantics { selected = isItemSelected || isActive }
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
                if (selection.isSelectionMode) {
                  Checkbox(
                    checked = isItemSelected,
                    onCheckedChange = { selection.onToggleSelection(selectionKey) },
                    modifier = Modifier
                      .align(Alignment.TopStart)
                      .testTag("select_checkbox_$selectionKey"),
                  )
                }
                IconButton(
                  onClick = { onToggleFavoriteArtist(artist) },
                  colors = IconButtonDefaults.iconButtonColors(
                    containerColor = ArtworkOverlayButtonBackground,
                  ),
                  modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopEnd)
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

/**
 * Collapsible section header displaying a group title (Album, Artist, Genre, or Year) with item count badge
 * and expand/collapse chevron in the Songs and Albums views, styled like the Files tool.
 */
@Composable
private fun MusicGroupHeader(
  title: String,
  count: Int,
  isCollapsed: Boolean = false,
  onToggleCollapse: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val rotationState by animateFloatAsState(
    targetValue = if (isCollapsed) -90f else 0f,
    label = "music_group_expand_rotation",
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("music_group_header_${title.lowercase(Locale.ROOT).replace(" ", "_")}"),
    shape = RoundedCornerShape(8.dp),
    color = Color.Transparent,
    onClick = onToggleCollapse,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        ) {
          Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
          )
        }
      }

      Icon(
        imageVector = Icons.Default.ExpandMore,
        contentDescription = if (isCollapsed) "Expand $title" else "Collapse $title",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .size(20.dp)
          .rotate(rotationState),
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumsTabContent(
  albums: List<MusicAlbum>,
  groupOption: MusicGroupOption = MusicGroupOption.NONE,
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
  val groupedAlbums = remember(albums, groupOption) {
    MusicPlayerEngine.groupAlbums(albums, groupOption)
  }
  val displayedAlbums = remember(groupedAlbums) {
    groupedAlbums.flatMap { it.albums }
  }
  var collapsedGroupTitles by remember(groupOption) {
    mutableStateOf(setOf<String>())
  }
  val focusedAlbumIndex = remember(displayedAlbums, currentTrack?.id, currentAlbumName) {
    MusicPlayerEngine.resolveFocusedAlbumIndex(
      albums = displayedAlbums,
      currentTrack = currentTrack,
      currentAlbumName = currentAlbumName,
    )
  }
  val focusedAlbumKey = displayedAlbums.getOrNull(focusedAlbumIndex)?.key
  val focusedLazyIndex = remember(groupedAlbums, focusedAlbumKey, collapsedGroupTitles) {
    MusicPlayerEngine.resolveFocusedGroupedAlbumLazyIndex(
      groups = groupedAlbums,
      focusedAlbumKey = focusedAlbumKey,
      collapsedGroupTitles = collapsedGroupTitles,
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
        initialFirstVisibleItemIndex = focusedLazyIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedLazyIndex, displayedAlbums.size, groupOption) {
        if (focusedLazyIndex >= 0 && !listState.isItemFullyVisible(focusedLazyIndex)) {
          listState.scrollToItem(focusedLazyIndex)
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
        groupedAlbums.forEach { group ->
          val hasHeader = group.title.isNotEmpty()
          val isCollapsed = hasHeader && group.title in collapsedGroupTitles
          if (hasHeader) {
            item(key = "header_${group.title}") {
              MusicGroupHeader(
                title = group.title,
                count = group.albums.size,
                isCollapsed = isCollapsed,
                onToggleCollapse = {
                  collapsedGroupTitles = if (isCollapsed) {
                    collapsedGroupTitles - group.title
                  } else {
                    collapsedGroupTitles + group.title
                  }
                },
              )
            }
          }
          if (!isCollapsed) {
            items(
              items = group.albums,
              key = { if (group.title.isEmpty()) "${it.name}|${it.artist}" else "${group.title}:${it.name}|${it.artist}" },
            ) { album ->
              val isActive = (focusedAlbumKey != null && album.key.equals(focusedAlbumKey, ignoreCase = true)) ||
                (currentAlbumName != null && currentAlbumName.equals(album.name, ignoreCase = true))
              val isFavorite = MusicPlayerEngine.isAlbumFavorite(album, favoriteAlbums)
              val selection = LocalMusicSelectionController.current
              val selectionKey = "album:${album.name}|${album.artist}"
              val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isItemSelected || isActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                  },
                ),
                border = BorderStroke(
                  width = if (isItemSelected) 1.5.dp else 1.dp,
                  color = if (isItemSelected) MaterialTheme.colorScheme.primary
                  else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                  else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .combinedClickable(
                    onClick = {
                      if (selection.isSelectionMode) {
                        selection.onToggleSelection(selectionKey)
                      } else {
                        onSelectAlbum(album)
                      }
                    },
                    onLongClick = { selection.onLongPressSelect(selectionKey) },
                  )
                  .semantics { selected = isItemSelected || isActive }
                  .testTag("album_item_${album.name}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  if (selection.isSelectionMode) {
                    Box(
                      modifier = Modifier.size(56.dp),
                      contentAlignment = Alignment.Center,
                    ) {
                      Checkbox(
                        checked = isItemSelected,
                        onCheckedChange = { selection.onToggleSelection(selectionKey) },
                        modifier = Modifier.testTag("select_checkbox_$selectionKey"),
                      )
                    }
                  } else {
                    TrackArtworkThumbnail(
                      track = album.representativeTrack,
                      size = 56.dp,
                      shape = RoundedCornerShape(12.dp),
                    )
                  }
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
        }
      }
    } else {
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedLazyIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedLazyIndex, displayedAlbums.size, groupOption) {
        if (focusedLazyIndex >= 0 && !gridState.isItemFullyVisible(focusedLazyIndex)) {
          gridState.scrollToItem(focusedLazyIndex)
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
        groupedAlbums.forEach { group ->
          val hasHeader = group.title.isNotEmpty()
          val isCollapsed = hasHeader && group.title in collapsedGroupTitles
          if (hasHeader) {
            item(
              key = "header_${group.title}",
              span = { GridItemSpan(maxLineSpan) },
            ) {
              MusicGroupHeader(
                title = group.title,
                count = group.albums.size,
                isCollapsed = isCollapsed,
                onToggleCollapse = {
                  collapsedGroupTitles = if (isCollapsed) {
                    collapsedGroupTitles - group.title
                  } else {
                    collapsedGroupTitles + group.title
                  }
                },
              )
            }
          }
          if (!isCollapsed) {
            items(
              items = group.albums,
              key = { if (group.title.isEmpty()) "${it.name}|${it.artist}" else "${group.title}:${it.name}|${it.artist}" },
            ) { album ->
              val isActive = (focusedAlbumKey != null && album.key.equals(focusedAlbumKey, ignoreCase = true)) ||
                (currentAlbumName != null && currentAlbumName.equals(album.name, ignoreCase = true))
              val isFavorite = MusicPlayerEngine.isAlbumFavorite(album, favoriteAlbums)
              val selection = LocalMusicSelectionController.current
              val selectionKey = "album:${album.name}|${album.artist}"
              val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isItemSelected || isActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                  },
                ),
                border = BorderStroke(
                  width = if (isItemSelected) 1.5.dp else 1.dp,
                  color = if (isItemSelected) MaterialTheme.colorScheme.primary
                  else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                  else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .combinedClickable(
                    onClick = {
                      if (selection.isSelectionMode) {
                        selection.onToggleSelection(selectionKey)
                      } else {
                        onSelectAlbum(album)
                      }
                    },
                    onLongClick = { selection.onLongPressSelect(selectionKey) },
                  )
                  .semantics { selected = isItemSelected || isActive }
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
                    if (selection.isSelectionMode) {
                      Checkbox(
                        checked = isItemSelected,
                        onCheckedChange = { selection.onToggleSelection(selectionKey) },
                        modifier = Modifier
                          .align(Alignment.TopStart)
                          .padding(4.dp)
                          .testTag("select_checkbox_$selectionKey"),
                      )
                    }
                    IconButton(
                      onClick = { onToggleFavoriteAlbum(album) },
                      colors = IconButtonDefaults.iconButtonColors(
                        containerColor = ArtworkOverlayButtonBackground,
                      ),
                      modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
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
  }
}

@Composable
private fun SongsTabContent(
  tracks: List<MusicTrack>,
  groupOption: MusicGroupOption = MusicGroupOption.NONE,
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
  val groupedTracks = remember(tracks, groupOption) {
    MusicPlayerEngine.groupTracks(tracks, groupOption)
  }
  var collapsedGroupTitles by remember(groupOption) {
    mutableStateOf(setOf<String>())
  }
  val focusedIndex = remember(groupedTracks, currentTrackId, collapsedGroupTitles) {
    MusicPlayerEngine.resolveFocusedGroupedTrackLazyIndex(
      groups = groupedTracks,
      currentTrackId = currentTrackId,
      collapsedGroupTitles = collapsedGroupTitles,
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
      LaunchedEffect(sectionFocusVersion, focusedIndex, tracks.size, groupOption) {
        if (focusedIndex >= 0 && !listState.isItemFullyVisible(focusedIndex)) {
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
        groupedTracks.forEach { group ->
          val hasHeader = group.title.isNotEmpty()
          val isCollapsed = hasHeader && group.title in collapsedGroupTitles
          if (hasHeader) {
            item(key = "header_${group.title}") {
              MusicGroupHeader(
                title = group.title,
                count = group.tracks.size,
                isCollapsed = isCollapsed,
                onToggleCollapse = {
                  collapsedGroupTitles = if (isCollapsed) {
                    collapsedGroupTitles - group.title
                  } else {
                    collapsedGroupTitles + group.title
                  }
                },
              )
            }
          }
          if (!isCollapsed) {
            items(
              items = group.tracks,
              key = { if (group.title.isEmpty()) it.id else "${group.title}:${it.id}" },
            ) { track ->
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
        }
      }
    } else {
      val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = focusedIndex.coerceAtLeast(0),
      )
      LaunchedEffect(sectionFocusVersion, focusedIndex, tracks.size, groupOption) {
        if (focusedIndex >= 0 && !gridState.isItemFullyVisible(focusedIndex)) {
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
        groupedTracks.forEach { group ->
          val hasHeader = group.title.isNotEmpty()
          val isCollapsed = hasHeader && group.title in collapsedGroupTitles
          if (hasHeader) {
            item(
              key = "header_${group.title}",
              span = { GridItemSpan(maxLineSpan) },
            ) {
              MusicGroupHeader(
                title = group.title,
                count = group.tracks.size,
                isCollapsed = isCollapsed,
                onToggleCollapse = {
                  collapsedGroupTitles = if (isCollapsed) {
                    collapsedGroupTitles - group.title
                  } else {
                    collapsedGroupTitles + group.title
                  }
                },
              )
            }
          }
          if (!isCollapsed) {
            items(
              items = group.tracks,
              key = { if (group.title.isEmpty()) it.id else "${group.title}:${it.id}" },
            ) { track ->
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
  }
}

@OptIn(ExperimentalFoundationApi::class)
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
          val selection = LocalMusicSelectionController.current
          val selectionKey = "genre:${genre.name}"
          val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isItemSelected || isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = if (isItemSelected) 1.5.dp else 1.dp,
              color = if (isItemSelected) MaterialTheme.colorScheme.primary
              else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .combinedClickable(
                onClick = {
                  if (selection.isSelectionMode) {
                    selection.onToggleSelection(selectionKey)
                  } else {
                    onSelectGenre(genre)
                  }
                },
                onLongClick = { selection.onLongPressSelect(selectionKey) },
              )
              .semantics { selected = isItemSelected || isActive }
              .testTag("genre_item_${genre.name}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              if (selection.isSelectionMode) {
                Box(
                  modifier = Modifier.size(52.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Checkbox(
                    checked = isItemSelected,
                    onCheckedChange = { selection.onToggleSelection(selectionKey) },
                    modifier = Modifier.testTag("select_checkbox_$selectionKey"),
                  )
                }
              } else {
                TrackArtworkThumbnail(
                  track = genre.representativeTrack,
                  size = 52.dp,
                  shape = RoundedCornerShape(12.dp),
                )
              }
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
          val selection = LocalMusicSelectionController.current
          val selectionKey = "genre:${genre.name}"
          val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
          val genrePalette = remember(genre.representativeTrack?.id, genre.representativeTrack?.artworkPrimaryRgb) {
            MusicPlayerEngine.resolveTrackPalette(genre.representativeTrack)
          }
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isItemSelected || isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = if (isItemSelected) 1.5.dp else 1.dp,
              color = if (isItemSelected) MaterialTheme.colorScheme.primary
              else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .combinedClickable(
                onClick = {
                  if (selection.isSelectionMode) {
                    selection.onToggleSelection(selectionKey)
                  } else {
                    onSelectGenre(genre)
                  }
                },
                onLongClick = { selection.onLongPressSelect(selectionKey) },
              )
              .semantics { selected = isItemSelected || isActive }
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
                if (selection.isSelectionMode) {
                  Checkbox(
                    checked = isItemSelected,
                    onCheckedChange = { selection.onToggleSelection(selectionKey) },
                    modifier = Modifier
                      .align(Alignment.TopStart)
                      .testTag("select_checkbox_$selectionKey"),
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                      .size(28.dp)
                      .align(Alignment.TopStart),
                  )
                }
                IconButton(
                  onClick = { onToggleFavoriteGenre(genre) },
                  colors = IconButtonDefaults.iconButtonColors(
                    containerColor = ArtworkOverlayButtonBackground,
                  ),
                  modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopEnd)
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

@OptIn(ExperimentalFoundationApi::class)
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
          val isActive = playlist.containsTrack(currentTrackId)
          val selection = LocalMusicSelectionController.current
          val selectionKey = "playlist:${playlist.id}"
          val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isItemSelected || isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = if (isItemSelected) 1.5.dp else 1.dp,
              color = if (isItemSelected) MaterialTheme.colorScheme.primary
              else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .combinedClickable(
                onClick = {
                  if (selection.isSelectionMode) {
                    selection.onToggleSelection(selectionKey)
                  } else {
                    onSelectPlaylist(playlist)
                  }
                },
                onLongClick = { selection.onLongPressSelect(selectionKey) },
              )
              .semantics { selected = isItemSelected || isActive }
              .testTag("playlist_item_${playlist.name}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              if (selection.isSelectionMode) {
                Box(
                  modifier = Modifier.size(52.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Checkbox(
                    checked = isItemSelected,
                    onCheckedChange = { selection.onToggleSelection(selectionKey) },
                    modifier = Modifier.testTag("select_checkbox_$selectionKey"),
                  )
                }
              } else {
                TrackArtworkThumbnail(
                  track = playlist.representativeTrack,
                  size = 52.dp,
                  shape = RoundedCornerShape(12.dp),
                )
              }
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
          val isActive = playlist.containsTrack(currentTrackId)
          val selection = LocalMusicSelectionController.current
          val selectionKey = "playlist:${playlist.id}"
          val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
          val playlistPalette = remember(playlist.representativeTrack?.id, playlist.representativeTrack?.artworkPrimaryRgb) {
            MusicPlayerEngine.resolveTrackPalette(playlist.representativeTrack)
          }
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isItemSelected || isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
            ),
            border = BorderStroke(
              width = if (isItemSelected) 1.5.dp else 1.dp,
              color = if (isItemSelected) MaterialTheme.colorScheme.primary
              else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .combinedClickable(
                onClick = {
                  if (selection.isSelectionMode) {
                    selection.onToggleSelection(selectionKey)
                  } else {
                    onSelectPlaylist(playlist)
                  }
                },
                onLongClick = { selection.onLongPressSelect(selectionKey) },
              )
              .semantics { selected = isItemSelected || isActive }
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
                if (selection.isSelectionMode) {
                  Checkbox(
                    checked = isItemSelected,
                    onCheckedChange = { selection.onToggleSelection(selectionKey) },
                    modifier = Modifier
                      .align(Alignment.TopStart)
                      .testTag("select_checkbox_$selectionKey"),
                  )
                } else {
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
                album.containsTrack(currentTrackId)
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

@OptIn(ExperimentalFoundationApi::class)
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
  val selection = LocalMusicSelectionController.current
  val selectionKey = "song:${track.id}"
  val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isItemSelected || isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = BorderStroke(
      width = if (isItemSelected) 1.5.dp else 1.dp,
      color = if (isItemSelected) MaterialTheme.colorScheme.primary
      else if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
      else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
    modifier = Modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = {
          if (selection.isSelectionMode) {
            selection.onToggleSelection(selectionKey)
          } else {
            onClick()
          }
        },
        onLongClick = { selection.onLongPressSelect(selectionKey) },
      )
      .semantics { selected = isItemSelected || isCurrent }
      .testTag("song_item_${track.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (selection.isSelectionMode) {
        Box(
          modifier = Modifier.size(48.dp),
          contentAlignment = Alignment.Center,
        ) {
          Checkbox(
            checked = isItemSelected,
            onCheckedChange = { selection.onToggleSelection(selectionKey) },
            modifier = Modifier.testTag("select_checkbox_$selectionKey"),
          )
        }
      } else {
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

@OptIn(ExperimentalFoundationApi::class)
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
  val selection = LocalMusicSelectionController.current
  val selectionKey = "song:${track.id}"
  val isItemSelected = selection.isSelectionMode && selection.isSelected(selectionKey)
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isItemSelected || isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = BorderStroke(
      width = if (isItemSelected) 1.5.dp else 1.dp,
      color = if (isItemSelected) MaterialTheme.colorScheme.primary
      else if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
      else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
    modifier = Modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = {
          if (selection.isSelectionMode) {
            selection.onToggleSelection(selectionKey)
          } else {
            onClick()
          }
        },
        onLongClick = { selection.onLongPressSelect(selectionKey) },
      )
      .semantics { selected = isItemSelected || isCurrent }
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
        if (selection.isSelectionMode) {
          Checkbox(
            checked = isItemSelected,
            onCheckedChange = { selection.onToggleSelection(selectionKey) },
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(4.dp)
              .testTag("select_checkbox_$selectionKey"),
          )
        } else {
          val timeBadgeShape = if (currentHexagonIconButtons()) HorizontalHexagonShape else RoundedCornerShape(8.dp)
          Surface(
            shape = timeBadgeShape,
            color = Color.Black.copy(alpha = 0.55f),
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(6.dp)
              .testTag("song_time_badge_${track.id}"),
          ) {
            Text(
              text = track.durationFormatted,
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
        }
        IconButton(
          onClick = onToggleFavorite,
          colors = IconButtonDefaults.iconButtonColors(
            containerColor = ArtworkOverlayButtonBackground,
          ),
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .size(30.dp)
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
          val indicatorShape = resolveIconButtonShape(CircleShape)
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(6.dp)
              .size(30.dp)
              .clip(indicatorShape)
              .background(ArtworkOverlayButtonBackground, indicatorShape),
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
      colors = IconButtonDefaults.iconButtonColors(
        containerColor = containerColor,
        contentColor = tint,
      ),
      modifier = Modifier
        .size(if (containerColor != Color.Transparent) 30.dp else 32.dp)
        .testTag("song_more_${track.id}"),
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "More options for ${track.title}",
        tint = tint,
        modifier = Modifier.size(if (containerColor != Color.Transparent) 16.dp else 18.dp),
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
      colors = IconButtonDefaults.iconButtonColors(
        containerColor = containerColor,
        contentColor = tint,
      ),
      modifier = Modifier
        .size(30.dp)
        .testTag(moreButtonTestTag),
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "More options for $collectionTitle",
        tint = tint,
        modifier = Modifier.size(if (containerColor != Color.Transparent) 16.dp else 18.dp),
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
  var textFieldValue by remember(initialValue) {
    mutableStateOf(
      TextFieldValue(
        text = initialValue,
        selection = TextRange(initialValue.length),
      )
    )
  }

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
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("rename_music_item_input"),
      )
    },
    confirmButton = {
      val trimmed = textFieldValue.text.trim()
      Button(
        onClick = { onConfirm(trimmed) },
        enabled = trimmed.isNotEmpty() && trimmed != initialValue,
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

@Composable
private fun DockedMiniMusicPlayerSection(
  session: MusicPlaybackSession,
  track: MusicTrack,
  externalMedia: SystemMediaPlayingInfo? = null,
  externalProgressFraction: Float = 0f,
  isPlaying: Boolean,
  isFavorite: Boolean,
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
  val currentPositionMs = session.currentPositionMs
  val trackDurationMs = session.trackDurationMs
  val progressFraction = if (externalMedia != null) {
    externalProgressFraction.coerceIn(0f, 1f)
  } else if (trackDurationMs > 0L) {
    (currentPositionMs.toFloat() / trackDurationMs.toFloat()).coerceIn(0f, 1f)
  } else {
    0f
  }
  CollapsedMiniMusicPlayer(
    track = track,
    externalMedia = externalMedia,
    isPlaying = isPlaying,
    isFavorite = isFavorite,
    progressFraction = progressFraction,
    dominantColor = dominantColor,
    darkSurfaceColor = darkSurfaceColor,
    accentColor = accentColor,
    dominantHex = dominantHex,
    onExpand = onExpand,
    onToggleFavorite = onToggleFavorite,
    onPrevious = onPrevious,
    onPlayPause = onPlayPause,
    onNext = onNext,
  )
}

/**
 * Collapsible Mini-Player docked at the bottom of the Music Player screen.
 * Its background dynamically follows the current song's album art colors (or active external
 * Android system media player session).
 */
@Composable
private fun CollapsedMiniMusicPlayer(
  track: MusicTrack,
  externalMedia: SystemMediaPlayingInfo? = null,
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
  val isExternal = externalMedia != null
  val displayTitle = externalMedia?.title ?: track.title
  val displaySubtitle = if (externalMedia != null) {
    listOf(externalMedia.artist, externalMedia.album, externalMedia.sourceAppName)
      .filter { it.isNotBlank() }
      .distinct()
      .joinToString(" • ")
      .ifBlank { "Android Media Player" }
  } else {
    "${track.artist} • ${track.album}"
  }

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
          overrideBitmap = externalMedia?.artworkBitmap,
          overridePalette = externalMedia?.palette,
          size = 44.dp,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .clickable(onClick = onExpand)
            .testTag("mini_player_album_art"),
        )

        Column(
          modifier = Modifier
            .weight(1f)
            .then(
              if (isExternal) {
                Modifier.semantics {
                  contentDescription = "$displayTitle • $displaySubtitle"
                }
              } else {
                Modifier.clearAndSetSemantics {
                  contentDescription = "$displayTitle • $displaySubtitle"
                }
              }
            ),
        ) {
          Text(
            text = displayTitle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("mini_player_title"),
          )
          Text(
            text = displaySubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.78f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("mini_player_subtitle"),
          )
        }

        if (!isExternal) {
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
 * with an integrated grab handle at the bottom of the player view. Dragging the handle up slides up
 * the queue view while pushing the album art out of the way, leaving the controls and title visible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedMusicPlayerOverlay(
  track: MusicTrack,
  externalMedia: SystemMediaPlayingInfo? = null,
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
  isSystemMediaListenerEnabled: Boolean = true,
  onGrantSystemMediaAccess: (() -> Unit)? = null,
  onOpenExternalApp: (() -> Unit)? = null,
  onTriggerExternalCustomAction: (SystemMediaCustomAction) -> Unit = {},
  onNavigateToArtist: () -> Unit,
  onNavigateToAlbum: () -> Unit,
) {
  val isExternal = externalMedia != null
  val queueSize = queue.size
  val artworkScale by animateFloatAsState(
    targetValue = if (isPlaying) 1.0f else 0.93f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "artworkScale",
  )

  val coroutineScope = rememberCoroutineScope()
  val initialExpandedFraction = if (!isExternal && initialQueueSheetValue == SheetValue.Expanded) 1f else 0f
  val queueExpandAnimatable = remember(isExternal) { Animatable(initialExpandedFraction) }
  var queueTargetExpanded by remember(isExternal, initialQueueSheetValue) {
    mutableStateOf(!isExternal && initialQueueSheetValue == SheetValue.Expanded)
  }
  val queueFraction = if (isExternal) 0f else queueExpandAnimatable.value.coerceIn(0f, 1f)
  val isQueueExpanded = !isExternal && (queueTargetExpanded || queueFraction > 0.5f)
  val isSheetSettledExpanded = !isExternal && queueTargetExpanded && queueFraction >= 0.98f
  var queueOpenCount by remember {
    mutableIntStateOf(if (initialQueueSheetValue == SheetValue.Expanded) 1 else 0)
  }

  fun animateQueueTo(expanded: Boolean) {
    if (isExternal) return
    if (expanded && !queueTargetExpanded) {
      queueOpenCount++
    }
    queueTargetExpanded = expanded
    coroutineScope.launch {
      queueExpandAnimatable.animateTo(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = Spring.StiffnessMediumLow,
        ),
      )
    }
  }

  fun toggleQueueSheet() {
    animateQueueTo(!queueTargetExpanded)
  }

  BackHandler(enabled = isQueueExpanded) {
    animateQueueTo(false)
  }

  val topGradientColor = lerp(dominantColor, Color(0xFF12121A), 0.25f)
  val midGradientColor = lerp(secondaryColor, Color(0xFF0E0E16), 0.45f)

  val density = LocalDensity.current
  val dismissThresholdPx = with(density) { 64.dp.toPx() }
  val defaultDragRangePx = with(density) { 300.dp.toPx() }
  val collapsedHandleBottomPaddingPx = with(density) { 6.dp.toPx() }
  var maxAlbumArtHeightPx by remember { mutableFloatStateOf(0f) }
  var settledQueueHeightPx by remember { mutableIntStateOf(0) }
  val queueDragRangePx = when {
    maxAlbumArtHeightPx > 80f -> maxAlbumArtHeightPx + collapsedHandleBottomPaddingPx
    settledQueueHeightPx > 80 -> settledQueueHeightPx.toFloat()
    else -> defaultDragRangePx
  }
  val currentQueueDragRangePx by rememberUpdatedState(queueDragRangePx)
  val targetQueueHeightPx = when {
    settledQueueHeightPx > 0 -> settledQueueHeightPx
    maxAlbumArtHeightPx > 0f -> (maxAlbumArtHeightPx + collapsedHandleBottomPaddingPx).roundToInt()
    else -> 0
  }
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
      .toolboxScreenBackground(
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
    val showAlbumArt = isExternal || queueFraction < 0.999f
    val showQueuePanel = !isExternal && queueFraction > 0.001f
    val artworkWeight = (1f - queueFraction).coerceAtLeast(0.0001f)
    val queueWeight = queueFraction.coerceAtLeast(0.0001f)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // 1. Top Header with Collapse Button, Now Playing Context, and Track Info Button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
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
            text = if (externalMedia != null) {
              val appName = externalMedia.sourceAppName.trim()
              if (appName.isNotEmpty()) "PLAYING FROM ${appName.uppercase()}" else "ANDROID MEDIA PLAYER"
            } else {
              "NOW PLAYING"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.72f),
          )
          Text(
            text = if (externalMedia != null) {
              externalMedia.album.ifBlank {
                externalMedia.sourceAppName.ifBlank { "Android Media Player" }
              }
            } else {
              "${track.album} (${currentQueueIndex + 1}/$queueSize)"
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        if (!isExternal) {
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
        } else {
          Spacer(modifier = Modifier.size(40.dp))
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Large Album Art Card — smoothly pushed out of the way as the queue slides up.
      // Shares flexible height directly with the slide-up queue panel so player controls never collapse or snap.
      if (showAlbumArt) {
        val artVisibilityFraction = (1f - queueFraction).coerceIn(0f, 1f)
        Box(
          modifier = Modifier
            .weight(artworkWeight)
            .fillMaxWidth()
            .padding(
              start = 20.dp,
              end = 20.dp,
              top = (2f * artVisibilityFraction).dp,
              bottom = (10f * artVisibilityFraction).dp,
            )
            .onSizeChanged { size ->
              if (queueFraction <= 0.001f && size.height > 0) {
                maxAlbumArtHeightPx = size.height.toFloat()
              }
            }
            .graphicsLayer {
              alpha = (1f - queueFraction * 1.15f).coerceIn(0f, 1f)
              val collapseScale = 0.72f + 0.28f * artVisibilityFraction
              scaleX = collapseScale
              scaleY = collapseScale
            },
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
                overrideBitmap = externalMedia?.artworkBitmap,
                overridePalette = externalMedia?.palette,
                modifier = Modifier.fillMaxSize(),
              )
              if (!isExternal) {
                IconButton(
                  onClick = onToggleFavorite,
                  colors = IconButtonDefaults.iconButtonColors(
                    containerColor = ArtworkOverlayButtonBackground,
                  ),
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(30.dp)
                    .testTag("expanded_player_favorite_button"),
                ) {
                  Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                    tint = if (isFavorite) accentColor else Color.White,
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
            }
          }
        }
      }

      // Fixed-height Player Controls Section (Title/Metadata, Seek, Transport, Speed/Sleep, Grab Handle)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // 3. Track Title, Artist, Album & Genre / Source Badge
        val displayTitle = externalMedia?.title ?: track.title
        val displayArtist = if (externalMedia != null) {
          externalMedia.artist.ifBlank {
            externalMedia.sourceAppName.ifBlank { "Android Media Player" }
          }
        } else {
          track.artist
        }
        val displayAlbum = if (externalMedia != null) {
          externalMedia.album
        } else {
          track.album
        }
        Column(
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = displayTitle,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .weight(1f)
                .testTag("expanded_player_title"),
            )
            if (!isExternal && !showAlbumArt) {
              IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                  .size(32.dp)
                  .testTag("expanded_player_favorite_button"),
              ) {
                Icon(
                  imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                  tint = if (isFavorite) accentColor else Color.White.copy(alpha = 0.85f),
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = displayArtist,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = accentColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .then(
                  if (!isExternal) Modifier.clickable(onClick = onNavigateToArtist)
                  else Modifier
                )
                .testTag("expanded_player_artist"),
            )
            if (displayAlbum.isNotBlank()) {
              Text(
                text = "•",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
              )
              Text(
                text = displayAlbum,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .then(
                    if (!isExternal) Modifier.clickable(onClick = onNavigateToAlbum)
                    else Modifier
                  )
                  .testTag("expanded_player_album"),
              )
            }
          }
          Spacer(modifier = Modifier.height(2.dp))
          val badgeText = if (externalMedia != null) {
            listOf(
              externalMedia.sourceAppName.ifBlank { "Android Media Player" },
              externalMedia.genre,
            ).filter { it.isNotBlank() }.distinct().joinToString(" • ")
          } else {
            "${track.genre} • ${track.year}"
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White.copy(alpha = 0.15f),
              modifier = if (isExternal) {
                Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .then(
                    if (onOpenExternalApp != null) {
                      Modifier.clickable(onClick = onOpenExternalApp)
                    } else {
                      Modifier
                    }
                  )
                  .semantics {
                    val appLabel = externalMedia.sourceAppName.takeIf { it.isNotBlank() } ?: "Android Media Player"
                    contentDescription = "Open $appLabel ($badgeText)"
                  }
                  .testTag("expanded_player_external_source_badge")
              } else {
                Modifier
              },
            ) {
              Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              )
            }
            if (isExternal && !isSystemMediaListenerEnabled && onGrantSystemMediaAccess != null) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable(onClick = onGrantSystemMediaAccess)
                  .testTag("expanded_player_grant_media_access_banner"),
              ) {
                Text(
                  text = "Grant Access",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = accentColor,
                  maxLines = 1,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
          }
        }

        // 4. Seek Slider (SlimDotSlider per DESIGN_LANGUAGE.md) + Elapsed / Total Time
        // Always reserve the row space even when hidden for external media with unknown duration.
        val isSeekBarVisible = !isExternal || durationMs > 0L
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expanded_player_seek_row"),
        ) {
          if (isSeekBarVisible) {
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
          } else {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .alpha(0f)
                .clearAndSetSemantics {},
            ) {
              Spacer(modifier = Modifier.height(28.dp))
              Text(
                text = "00:00",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
              )
            }
          }
        }

        // 5. Primary Transport Controls (Shuffle / External Action 1, Previous, Play/Pause, Next, Repeat / External Action 2)
        val externalLeftAction = externalMedia?.customActions?.getOrNull(0)
        val externalRightAction = externalMedia?.customActions?.getOrNull(1)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (!isExternal) {
            IconButton(
              onClick = onToggleShuffle,
              colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (shuffleMode.isEnabled) accentColor.copy(alpha = 0.25f) else Color.Transparent,
              ),
              modifier = Modifier
                .size(40.dp)
                .testTag("expanded_player_shuffle_button"),
            ) {
              Icon(
                imageVector = if (shuffleMode == MusicShuffleMode.SMART_SHUFFLE) Icons.Default.AutoAwesome else Icons.Default.Shuffle,
                contentDescription = shuffleMode.displayName,
                tint = if (shuffleMode.isEnabled) accentColor else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
              )
            }
          } else if (externalLeftAction != null) {
            ExternalCustomActionButton(
              customAction = externalLeftAction,
              accentColor = accentColor,
              testTag = "expanded_player_external_action_left",
              onClick = { onTriggerExternalCustomAction(externalLeftAction) },
            )
          } else {
            Spacer(modifier = Modifier.size(40.dp))
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

          if (!isExternal) {
            IconButton(
              onClick = onCycleRepeat,
              colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (repeatMode != MusicRepeatMode.OFF) accentColor.copy(alpha = 0.25f) else Color.Transparent,
              ),
              modifier = Modifier
                .size(40.dp)
                .testTag("expanded_player_repeat_button"),
            ) {
              Icon(
                imageVector = if (repeatMode == MusicRepeatMode.REPEAT_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription = repeatMode.displayName,
                tint = if (repeatMode != MusicRepeatMode.OFF) accentColor else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
              )
            }
          } else if (externalRightAction != null) {
            ExternalCustomActionButton(
              customAction = externalRightAction,
              accentColor = accentColor,
              testTag = "expanded_player_external_action_right",
              onClick = { onTriggerExternalCustomAction(externalRightAction) },
            )
          } else {
            Spacer(modifier = Modifier.size(40.dp))
          }
        }

        // 6. Secondary Utility Bar: Speed & Sleep Timer
        // Hidden when controlling external Android system media, while reserving the row space.
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expanded_player_speed_sleep_row"),
        ) {
          if (!isExternal) {
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
          } else {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .alpha(0f)
                .clearAndSetSemantics {},
            ) {
              Spacer(modifier = Modifier.size(16.dp))
              Text(
                text = "1.0x",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }

        // 7. Integrated Queue Grab Handle Bar at the bottom of the Player View
        if (!isExternal) {
          val arrowRotation by animateFloatAsState(
            targetValue = if (isQueueExpanded) 180f else 0f,
            label = "QueueChevronRotation",
          )
          val handlePrimaryTextColor = Color.White
          val handleSecondaryTextColor = Color.White.copy(alpha = 0.72f)
          val handlePillColor = Color.White.copy(alpha = 0.45f)
          var handleLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = ((1f - queueFraction) * 6f).dp)
              .onGloballyPositioned { coords ->
                handleLayoutCoordinates = coords
              }
              .pointerInput(Unit) {
                var dragStartFraction = 0f
                var dragStartRootY = 0f
                var totalRootDragY = 0f
                var lastStepRootDeltaY = 0f
                detectVerticalDragGestures(
                  onDragStart = { startOffset ->
                    dragStartFraction = queueExpandAnimatable.value
                    dragStartRootY = handleLayoutCoordinates?.localToRoot(startOffset)?.y ?: startOffset.y
                    totalRootDragY = 0f
                    lastStepRootDeltaY = 0f
                    coroutineScope.launch {
                      queueExpandAnimatable.stop()
                    }
                  },
                  onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    val currentRootY = handleLayoutCoordinates?.localToRoot(change.position)?.y
                    val newTotalRootDragY = if (currentRootY != null) {
                      currentRootY - dragStartRootY
                    } else {
                      totalRootDragY + dragAmount
                    }
                    val stepDelta = newTotalRootDragY - totalRootDragY
                    if (abs(stepDelta) > 0.5f) {
                      lastStepRootDeltaY = stepDelta
                    }
                    totalRootDragY = newTotalRootDragY
                    val rangePx = currentQueueDragRangePx.coerceAtLeast(1f)
                    val nextFraction = (dragStartFraction - (totalRootDragY / rangePx)).coerceIn(0f, 1f)
                    coroutineScope.launch {
                      queueExpandAnimatable.snapTo(nextFraction)
                    }
                  },
                  onDragEnd = {
                    val cur = queueExpandAnimatable.value
                    val shouldExpand = when {
                      lastStepRootDeltaY < -6f || (totalRootDragY < -24f && lastStepRootDeltaY <= 0f) -> true
                      lastStepRootDeltaY > 6f || (totalRootDragY > 24f && lastStepRootDeltaY >= 0f) -> false
                      else -> cur >= 0.5f
                    }
                    animateQueueTo(shouldExpand)
                  },
                  onDragCancel = {
                    animateQueueTo(queueExpandAnimatable.value >= 0.5f)
                  },
                )
              }
              .pointerInput(Unit) {
                detectTapGestures(
                  onTap = { toggleQueueSheet() },
                )
              }
              .semantics {
                onClick {
                  toggleQueueSheet()
                  true
                }
              }
              .testTag("music_queue_panel_toggle"),
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp)
                .heightIn(min = 22.dp)
                .testTag("expanded_player_up_next_button"),
              contentAlignment = Alignment.Center,
            ) {
              // Left: Queue icon, label, and song count badge
              Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "Queue",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = handlePrimaryTextColor,
                )
                if (queue.isNotEmpty()) {
                  Badge(
                    containerColor = accentColor.copy(alpha = 0.25f),
                    contentColor = handlePrimaryTextColor,
                  ) {
                    Text(
                      text = queue.size.toString(),
                      modifier = Modifier.padding(horizontal = 4.dp),
                    )
                  }
                }
              }

              // Center: Drag bar pill aligned vertically with the middle of the queue drag handle
              Box(
                modifier = Modifier
                  .align(Alignment.Center)
                  .width(36.dp)
                  .height(4.dp)
                  .background(
                    color = handlePillColor,
                    shape = CircleShape,
                  )
                  .testTag("music_queue_grab_handle"),
              )

              // Right: Next track preview & chevron
              Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                if (!isQueueExpanded && queue.isNotEmpty()) {
                  val nextTrack = queue.getOrNull((currentQueueIndex + 1) % queue.size)
                  val previewText = if (queue.size > 1 && nextTrack != null) {
                    "Next: ${nextTrack.title}"
                  } else {
                    "Track ${currentQueueIndex + 1}/${queue.size}"
                  }
                  Text(
                    text = previewText,
                    style = MaterialTheme.typography.labelSmall,
                    color = handleSecondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 120.dp),
                  )
                }
                Icon(
                  imageVector = Icons.Default.KeyboardArrowUp,
                  contentDescription = "Toggle queue panel",
                  modifier = Modifier
                    .size(18.dp)
                    .rotate(arrowRotation),
                  tint = handleSecondaryTextColor,
                )
              }
            }
          }
        } else {
          Spacer(modifier = Modifier.height(8.dp))
        }
      }

      // 8. Slide-Up Queue Panel (shares flexible height directly with Album Art while keeping controls intact)
      if (showQueuePanel) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(queueWeight)
            .onSizeChanged { size ->
              if (queueFraction >= 0.999f && size.height > 0) {
                settledQueueHeightPx = size.height
              }
            }
            .clipToBounds()
            .graphicsLayer {
              alpha = (queueFraction * 1.35f).coerceIn(0f, 1f)
            }
            .layout { measurable, constraints ->
              val hasBoundedHeight = constraints.hasBoundedHeight
              val boxHeight = if (hasBoundedHeight) constraints.maxHeight else 0
              val desiredHeight = if (hasBoundedHeight && targetQueueHeightPx > 0 && queueFraction < 0.999f) {
                targetQueueHeightPx.coerceAtLeast(boxHeight)
              } else {
                boxHeight
              }
              val childConstraints = if (hasBoundedHeight) {
                constraints.copy(
                  minHeight = desiredHeight.coerceAtLeast(0),
                  maxHeight = desiredHeight.coerceAtLeast(0),
                )
              } else {
                constraints
              }
              val placeable = measurable.measure(childConstraints)
              val layoutHeight = if (hasBoundedHeight) boxHeight else placeable.height
              layout(placeable.width, layoutHeight) {
                placeable.placeRelative(0, 0)
              }
            },
        ) {
          PlaybackQueueSheetContent(
            queue = queue,
            currentQueueIndex = currentQueueIndex,
            currentTrackId = track.id,
            isPlaying = isPlaying,
            shuffleMode = shuffleMode,
            isExpanded = isQueueExpanded,
            isSheetSettledExpanded = isSheetSettledExpanded,
            queueOpenCount = queueOpenCount,
            accentColor = accentColor,
            usePlayerBackground = true,
            onSelectQueueIndex = onSelectQueueIndex,
            onMoveQueueItem = onMoveQueueItem,
            onSaveQueueAsPlaylist = onSaveQueueAsPlaylist,
          )
        }
      }
    }
  }
}

@Composable
private fun ExternalCustomActionButton(
  customAction: SystemMediaCustomAction,
  accentColor: Color,
  testTag: String,
  onClick: () -> Unit,
) {
  val isHighlighted = MusicPlayerEngine.isExternalCustomActionHighlighted(customAction)
  val tintColor = if (isHighlighted) accentColor else Color.White.copy(alpha = 0.7f)
  val imageBitmap = remember(customAction.iconBitmap) {
    customAction.iconBitmap?.takeIf { !it.isRecycled }?.asImageBitmap()
  }
  IconButton(
    onClick = onClick,
    colors = IconButtonDefaults.iconButtonColors(
      containerColor = if (isHighlighted) accentColor.copy(alpha = 0.25f) else Color.Transparent,
    ),
    modifier = Modifier
      .size(40.dp)
      .testTag(testTag),
  ) {
    if (imageBitmap != null) {
      Image(
        bitmap = imageBitmap,
        contentDescription = customAction.label,
        colorFilter = ColorFilter.tint(tintColor),
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(22.dp),
      )
    } else {
      Icon(
        imageVector = resolveExternalCustomActionVector(customAction, isHighlighted),
        contentDescription = customAction.label,
        tint = tintColor,
        modifier = Modifier.size(22.dp),
      )
    }
  }
}

private fun resolveExternalCustomActionVector(
  customAction: SystemMediaCustomAction,
  isHighlighted: Boolean,
): androidx.compose.ui.graphics.vector.ImageVector {
  val combined = "${customAction.action} ${customAction.label}".lowercase(Locale.ROOT)
  return when {
    combined.contains("smart") && combined.contains("shuffle") -> Icons.Default.AutoAwesome
    combined.contains("shuffle") || combined.contains("random") -> Icons.Default.Shuffle
    combined.contains("repeat_one") || combined.contains("repeat one") || combined.contains("repeat 1") -> Icons.Default.RepeatOne
    combined.contains("repeat") || combined.contains("loop") -> Icons.Default.Repeat
    combined.contains("dislike") ||
      combined.contains("thumb_down") ||
      combined.contains("thumbs_down") ||
      combined.contains("thumbs down") ||
      combined.contains("ban") ||
      combined.contains("hide") -> Icons.Default.RemoveCircleOutline
    combined.contains("favorite") ||
      combined.contains("fav") ||
      combined.contains("star") ||
      combined.contains("heart") ||
      combined.contains("like") ||
      combined.contains("love") ||
      combined.contains("thumb_up") ||
      combined.contains("thumbs_up") ||
      combined.contains("thumbs up") -> if (isHighlighted) Icons.Default.Star else Icons.Default.StarBorder
    combined.contains("speed") || combined.contains("rate") -> Icons.Default.Speed
    combined.contains("sleep") || combined.contains("timer") -> Icons.Default.Timer
    combined.contains("queue") || combined.contains("playlist") -> Icons.AutoMirrored.Filled.QueueMusic
    combined.contains("add") || combined.contains("plus") -> Icons.Default.Add
    combined.contains("share") -> Icons.Default.Share
    else -> Icons.Default.Tune
  }
}

/**
 * Slide-up Playback Queue Sheet Panel inside the Full Music Player.
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
  accentColor: Color = MaterialTheme.colorScheme.primary,
  usePlayerBackground: Boolean = false,
  onSelectQueueIndex: (Int) -> Unit,
  onMoveQueueItem: (fromIndex: Int, toIndex: Int) -> Unit,
  onSaveQueueAsPlaylist: () -> Unit,
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
  val defaultItemHeightPx = with(density) { 64.dp.toPx() }
  val itemSpacingPx = with(density) { 8.dp.toPx() }
  var draggedIndex by remember { mutableStateOf<Int?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  val itemHeightsPx = remember { mutableMapOf<Int, Float>() }
  val currentQueue by rememberUpdatedState(queue)
  val currentOnMoveQueueItem by rememberUpdatedState(onMoveQueueItem)

  val primaryTextColor = if (usePlayerBackground) Color.White else MaterialTheme.colorScheme.onSurface
  val secondaryTextColor = if (usePlayerBackground) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant
  val activeHighlightColor = if (usePlayerBackground) accentColor else MaterialTheme.colorScheme.primary

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
      .fillMaxSize()
      .testTag("music_queue_sheet"),
  ) {
    // Top of Song Queue Action Bar: Save Queue as Playlist
    if (queue.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp),
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
          color = secondaryTextColor,
        )
        FilledTonalButton(
          onClick = onSaveQueueAsPlaylist,
          shape = CircleShape,
          colors = if (usePlayerBackground) {
            ButtonDefaults.filledTonalButtonColors(
              containerColor = Color.White.copy(alpha = 0.16f),
              contentColor = Color.White,
            )
          } else {
            ButtonDefaults.filledTonalButtonColors()
          },
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
          color = secondaryTextColor,
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        itemsIndexed(queue, key = { idx, t -> "${idx}_${t.id}" }) { index, item ->
          val isCurrent = index == focusedQueueIndex
          val isDragging = draggedIndex == index
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (usePlayerBackground) {
              if (isCurrent) accentColor.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.10f)
            } else {
              if (isCurrent) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              } else {
                MaterialTheme.colorScheme.surfaceContainer
              }
            },
            shadowElevation = if (isDragging) 8.dp else 0.dp,
            border = if (isCurrent || isDragging) {
              BorderStroke(1.dp, activeHighlightColor.copy(alpha = if (usePlayerBackground) 0.7f else 0.6f))
            } else if (usePlayerBackground) {
              BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
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
                    tint = secondaryTextColor,
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
                        activeHighlightColor
                      } else {
                        secondaryTextColor
                      },
                    )
                    Text(
                      text = item.title,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.Bold,
                      color = if (isCurrent) {
                        activeHighlightColor
                      } else {
                        primaryTextColor
                      },
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f, fill = false),
                    )
                  }
                  Text(
                    text = "${item.artist} • ${item.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
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
                  activeHighlightColor
                } else {
                  primaryTextColor
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
  overrideBitmap: Bitmap? = null,
  overridePalette: AlbumArtPalette? = null,
) {
  val isInspection = LocalInspectionMode.current
  var bitmap by remember(track?.id, track?.album, track?.artist, track?.artworkPrimaryRgb) {
    mutableStateOf<Bitmap?>(
      if (track != null && !isInspection && overrideBitmap == null) MusicPlayerEngine.getCachedAlbumArtBitmap(track, 240)
      else null
    )
  }

  LaunchedEffect(track?.id, track?.album, track?.artist, isInspection, overrideBitmap) {
    if (track != null && !isInspection && overrideBitmap == null && bitmap == null) {
      bitmap = MusicPlayerEngine.loadAlbumArtBitmapAsync(track, sizePx = 240)
    }
  }

  val palette = overridePalette ?: remember(track?.id, track?.artworkPrimaryRgb, bitmap) {
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
    val activeBitmap = overrideBitmap ?: bitmap
    val imageBitmap = remember(activeBitmap) { activeBitmap?.asImageBitmap() }
    if (imageBitmap != null) {
      Image(
        bitmap = imageBitmap,
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
  overrideBitmap: Bitmap? = null,
  overridePalette: AlbumArtPalette? = null,
) {
  val isInspection = LocalInspectionMode.current
  var bitmap by remember(track?.id, track?.album, track?.artist, track?.artworkPrimaryRgb) {
    mutableStateOf<Bitmap?>(
      if (track != null && !isInspection && overrideBitmap == null) MusicPlayerEngine.getCachedAlbumArtBitmap(track, 360)
      else null
    )
  }

  LaunchedEffect(track?.id, track?.album, track?.artist, isInspection, overrideBitmap) {
    if (track != null && !isInspection && overrideBitmap == null && bitmap == null) {
      bitmap = MusicPlayerEngine.loadAlbumArtBitmapAsync(track, sizePx = 360)
    }
  }

  val palette = overridePalette ?: remember(track?.id, track?.artworkPrimaryRgb, bitmap) {
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
    val activeBitmap = overrideBitmap ?: bitmap
    val imageBitmap = remember(activeBitmap) { activeBitmap?.asImageBitmap() }
    if (imageBitmap != null) {
      Image(
        bitmap = imageBitmap,
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
