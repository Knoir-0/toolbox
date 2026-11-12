package com.example.toolbox.ui.tools

import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
  modifier: Modifier = Modifier,
  initialDirectory: File? = null,
  initialTracks: List<MusicTrack>? = null,
  initialTab: MusicLibraryTab? = null,
  initialViewMode: MusicViewMode? = null,
  initialPlayerExpanded: Boolean = false,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current

  val prefs = remember(context, isInspection) {
    if (isInspection) null
    else context.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
  }

  // Library Tab: Artists, Albums, Songs, Genres
  var selectedTab by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_MUSIC_TAB, null)
    mutableStateOf(initialTab ?: MusicLibraryTab.fromNameOrDefault(saved))
  }

  fun updateTab(tab: MusicLibraryTab) {
    selectedTab = tab
    prefs?.edit()?.putString(PREF_KEY_MUSIC_TAB, tab.name)?.apply()
  }

  // View Mode: List vs Card
  var viewMode by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_MUSIC_VIEW_MODE, null)
    mutableStateOf(initialViewMode ?: MusicViewMode.fromNameOrDefault(saved))
  }

  fun updateViewMode(mode: MusicViewMode) {
    viewMode = mode
    prefs?.edit()?.putString(PREF_KEY_MUSIC_VIEW_MODE, mode.name)?.apply()
  }

  // Sort Order
  var sortOrder by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_MUSIC_SORT_ORDER, null)
    mutableStateOf(MusicSortOrder.fromNameOrDefault(saved))
  }

  fun updateSortOrder(order: MusicSortOrder) {
    sortOrder = order
    prefs?.edit()?.putString(PREF_KEY_MUSIC_SORT_ORDER, order.name)?.apply()
  }

  // Shuffle & Repeat
  var isShuffleEnabled by rememberSaveable {
    val saved = prefs?.getBoolean(PREF_KEY_MUSIC_SHUFFLE, false) ?: false
    mutableStateOf(saved)
  }

  fun toggleShuffle() {
    isShuffleEnabled = !isShuffleEnabled
    prefs?.edit()?.putBoolean(PREF_KEY_MUSIC_SHUFFLE, isShuffleEnabled)?.apply()
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  var repeatMode by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_MUSIC_REPEAT, null)
    mutableStateOf(MusicRepeatMode.fromNameOrDefault(saved))
  }

  fun cycleRepeatMode() {
    repeatMode = repeatMode.next()
    prefs?.edit()?.putString(PREF_KEY_MUSIC_REPEAT, repeatMode.name)?.apply()
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  // Favorites
  var favoriteIds by rememberSaveable {
    val saved = prefs?.getString(PREF_KEY_MUSIC_FAVORITES, "").orEmpty()
    val set = saved.split("|").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    mutableStateOf(set)
  }

  fun toggleFavorite(trackId: String) {
    val next = if (trackId in favoriteIds) favoriteIds - trackId else favoriteIds + trackId
    favoriteIds = next
    prefs?.edit()?.putString(PREF_KEY_MUSIC_FAVORITES, next.joinToString("|"))?.apply()
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  var showFavoritesOnly by rememberSaveable { mutableStateOf(false) }

  // Search state
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }

  // Drill-down selection state (Artist detail, Album detail, Genre detail)
  var selectedArtistName by rememberSaveable { mutableStateOf<String?>(null) }
  var selectedAlbumKey by rememberSaveable { mutableStateOf<String?>(null) }
  var selectedGenreName by rememberSaveable { mutableStateOf<String?>(null) }

  fun clearDrillDown() {
    selectedArtistName = null
    selectedAlbumKey = null
    selectedGenreName = null
  }

  // Load library tracks
  val allTracks = remember(initialTracks, initialDirectory, isInspection) {
    when {
      initialTracks != null -> initialTracks
      isInspection -> MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
      else -> MusicPlayerEngine.loadLibraryTracks(
        initialDirectory = initialDirectory,
        includeSamples = true,
      )
    }
  }

  val sortedTracks = remember(allTracks, sortOrder, showFavoritesOnly, favoriteIds, searchQuery) {
    val base = if (showFavoritesOnly) {
      allTracks.filter { it.id in favoriteIds }
    } else {
      allTracks
    }
    val filtered = MusicPlayerEngine.filterTracks(base, searchQuery)
    MusicPlayerEngine.sortTracks(filtered, sortOrder)
  }

  val allArtists = remember(allTracks, searchQuery) {
    MusicPlayerEngine.filterArtists(MusicPlayerEngine.buildArtists(allTracks), searchQuery)
  }

  val allAlbums = remember(allTracks, searchQuery) {
    MusicPlayerEngine.filterAlbums(MusicPlayerEngine.buildAlbums(allTracks), searchQuery)
  }

  val allGenres = remember(allTracks, searchQuery) {
    MusicPlayerEngine.filterGenres(MusicPlayerEngine.buildGenres(allTracks), searchQuery)
  }

  // Playback Queue & Active Track State
  var playbackQueue by remember(allTracks) {
    mutableStateOf(allTracks)
  }
  var currentQueueIndex by rememberSaveable {
    mutableIntStateOf(0)
  }
  val currentTrack: MusicTrack? = playbackQueue.getOrNull(
    currentQueueIndex.coerceIn(0, maxOf(0, playbackQueue.size - 1))
  ) ?: allTracks.firstOrNull()

  var isPlaying by rememberSaveable { mutableStateOf(false) }
  var isPlayerExpanded by rememberSaveable { mutableStateOf(initialPlayerExpanded) }
  var currentPositionMs by rememberSaveable { mutableLongStateOf(0L) }
  var trackDurationMs by remember(currentTrack) {
    mutableLongStateOf(currentTrack?.durationMs?.coerceAtLeast(1000L) ?: 15_000L)
  }
  var isDraggingSeek by remember { mutableStateOf(false) }
  var dragSeekFraction by remember { mutableFloatStateOf(0f) }

  // Volume & Speed & Sleep Timer & Modals
  var volume by rememberSaveable { mutableFloatStateOf(0.85f) }
  var isMuted by rememberSaveable { mutableStateOf(false) }
  var playbackSpeed by rememberSaveable { mutableFloatStateOf(1.0f) }
  var sleepTimerPreset by rememberSaveable { mutableStateOf(SleepTimerPreset.OFF) }
  var sleepRemainingMs by rememberSaveable { mutableLongStateOf(0L) }
  var showQueueSheet by rememberSaveable { mutableStateOf(false) }
  var showTrackInfoDialog by rememberSaveable { mutableStateOf(false) }
  var showSortMenu by remember { mutableStateOf(false) }
  var showSleepTimerMenu by remember { mutableStateOf(false) }

  // MediaPlayer lifecycle management
  val mediaPlayerRef = remember { mutableStateOf<MediaPlayer?>(null) }

  fun releaseMediaPlayer() {
    val mp = mediaPlayerRef.value
    mediaPlayerRef.value = null
    if (mp != null) {
      try {
        if (mp.isPlaying) mp.stop()
      } catch (_: Throwable) {}
      try {
        mp.release()
      } catch (_: Throwable) {}
    }
  }

  fun applyPlayerVolumeAndSpeed(mp: MediaPlayer?) {
    if (mp == null) return
    val effectiveVol = if (isMuted) 0f else volume.coerceIn(0f, 1f)
    try {
      mp.setVolume(effectiveVol, effectiveVol)
    } catch (_: Throwable) {}
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPlaying) {
      try {
        mp.playbackParams = PlaybackParams().setSpeed(playbackSpeed.coerceIn(0.5f, 2.0f))
      } catch (_: Throwable) {}
    }
  }

  fun stepToTrack(
    newIndex: Int,
    autoPlay: Boolean = isPlaying,
  ) {
    if (playbackQueue.isEmpty()) return
    val clamped = newIndex.coerceIn(0, playbackQueue.lastIndex)
    currentQueueIndex = clamped
    currentPositionMs = 0L
    val target = playbackQueue[clamped]
    trackDurationMs = target.durationMs.coerceAtLeast(1000L)
    isPlaying = autoPlay
  }

  fun playTrackInContext(
    track: MusicTrack,
    contextTracks: List<MusicTrack>,
  ) {
    val queueList = if (contextTracks.isNotEmpty()) contextTracks else listOf(track)
    playbackQueue = queueList
    val idx = queueList.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
    currentQueueIndex = idx
    currentPositionMs = 0L
    trackDurationMs = track.durationMs.coerceAtLeast(1000L)
    isPlaying = true
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  fun skipToNext(userInitiated: Boolean = true) {
    val nextIdx = MusicPlayerEngine.resolveNextTrackIndex(
      currentIndex = currentQueueIndex,
      queueSize = playbackQueue.size,
      repeatMode = repeatMode,
      shuffleEnabled = isShuffleEnabled,
      userInitiated = userInitiated,
    )
    if (nextIdx != null) {
      stepToTrack(nextIdx, autoPlay = if (userInitiated) isPlaying else true)
    } else {
      isPlaying = false
      currentPositionMs = 0L
    }
  }

  fun skipToPrevious() {
    val prevIdx = MusicPlayerEngine.resolvePreviousTrackIndex(
      currentIndex = currentQueueIndex,
      queueSize = playbackQueue.size,
      currentPositionMs = currentPositionMs,
    ) ?: return
    if (prevIdx == currentQueueIndex) {
      currentPositionMs = 0L
      try {
        mediaPlayerRef.value?.seekTo(0)
      } catch (_: Throwable) {}
    } else {
      stepToTrack(prevIdx, autoPlay = isPlaying)
    }
  }

  fun seekRelativeMs(deltaMs: Long) {
    val targetMs = (currentPositionMs + deltaMs).coerceIn(0L, trackDurationMs)
    currentPositionMs = targetMs
    try {
      mediaPlayerRef.value?.seekTo(targetMs.toInt())
    } catch (_: Throwable) {}
  }

  // Prepare MediaPlayer when currentTrack changes
  LaunchedEffect(currentTrack?.id, isInspection) {
    releaseMediaPlayer()
    val track = currentTrack ?: return@LaunchedEffect
    trackDurationMs = track.durationMs.coerceAtLeast(1000L)
    if (isInspection) return@LaunchedEffect

    try {
      val playableFile = if (track.isSampleTrack || track.file == null || !track.file.exists()) {
        MusicPlayerEngine.ensurePlayableSampleWavFile(context.cacheDir, track)
      } else {
        track.file
      }
      if (playableFile.exists()) {
        val mp = MediaPlayer()
        mp.setDataSource(playableFile.absolutePath)
        mp.prepare()
        val actualDur = mp.duration.toLong()
        if (actualDur > 0L) {
          trackDurationMs = actualDur
        }
        mp.setOnCompletionListener {
          skipToNext(userInitiated = false)
        }
        mediaPlayerRef.value = mp
        applyPlayerVolumeAndSpeed(mp)
        if (currentPositionMs > 0L && currentPositionMs < trackDurationMs) {
          mp.seekTo(currentPositionMs.toInt())
        }
        if (isPlaying) {
          mp.start()
        }
      }
    } catch (_: Throwable) {
      // Fallback simulated timer loop below handles progression if hardware codec is unavailable
    }
  }

  // Sync play/pause state with MediaPlayer
  LaunchedEffect(isPlaying) {
    val mp = mediaPlayerRef.value ?: return@LaunchedEffect
    try {
      if (isPlaying && !mp.isPlaying) {
        mp.start()
        applyPlayerVolumeAndSpeed(mp)
      } else if (!isPlaying && mp.isPlaying) {
        mp.pause()
      }
    } catch (_: Throwable) {}
  }

  // Sync volume, mute, and speed
  LaunchedEffect(volume, isMuted, playbackSpeed) {
    applyPlayerVolumeAndSpeed(mediaPlayerRef.value)
  }

  // Progress ticker loop
  LaunchedEffect(isPlaying, currentTrack?.id, playbackSpeed) {
    if (!isPlaying) return@LaunchedEffect
    val tickMs = 200L
    while (isActive && isPlaying) {
      delay(tickMs)
      if (!isDraggingSeek) {
        val mp = mediaPlayerRef.value
        val hardwarePos = try {
          if (mp != null && mp.isPlaying) mp.currentPosition.toLong() else null
        } catch (_: Throwable) {
          null
        }
        if (hardwarePos != null && hardwarePos > 0L) {
          currentPositionMs = hardwarePos.coerceAtMost(trackDurationMs)
        } else {
          val delta = (tickMs * playbackSpeed).toLong().coerceAtLeast(50L)
          val nextPos = currentPositionMs + delta
          if (nextPos >= trackDurationMs) {
            skipToNext(userInitiated = false)
          } else {
            currentPositionMs = nextPos
          }
        }
      }
    }
  }

  // Sleep timer countdown
  LaunchedEffect(sleepTimerPreset, isPlaying) {
    if (sleepTimerPreset == SleepTimerPreset.OFF || sleepRemainingMs <= 0L) return@LaunchedEffect
    while (isActive && sleepRemainingMs > 0L) {
      delay(1000L)
      sleepRemainingMs = (sleepRemainingMs - 1000L).coerceAtLeast(0L)
      if (sleepRemainingMs == 0L) {
        isPlaying = false
        sleepTimerPreset = SleepTimerPreset.OFF
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      releaseMediaPlayer()
    }
  }

  // Dynamic Album Art Palette for the active track
  var activePalette by remember(currentTrack?.id) {
    mutableStateOf(MusicPlayerEngine.resolveTrackPalette(currentTrack))
  }

  LaunchedEffect(currentTrack) {
    val track = currentTrack ?: return@LaunchedEffect
    val bmp = MusicPlayerEngine.loadAlbumArtBitmapAsync(track, sizePx = 360)
    val extracted = MusicPlayerEngine.extractPaletteFromBitmap(bmp)
    activePalette = if (track.isSampleTrack) {
      MusicPlayerEngine.resolveTrackPalette(track)
    } else {
      extracted
    }
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

  val hasDrillDown = selectedArtistName != null || selectedAlbumKey != null || selectedGenreName != null

  BackHandler(enabled = isPlayerExpanded || showQueueSheet || isSearchActive || hasDrillDown) {
    when {
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
        allArtists.find { it.name.equals(name, ignoreCase = true) }
          ?: MusicPlayerEngine.buildArtists(allTracks).find { it.name.equals(name, ignoreCase = true) }
      }
      val activeAlbum = selectedAlbumKey?.let { key ->
        allAlbums.find { "${it.name}|${it.artist}".equals(key, ignoreCase = true) }
          ?: MusicPlayerEngine.buildAlbums(allTracks).find { "${it.name}|${it.artist}".equals(key, ignoreCase = true) }
      }
      val activeGenre = selectedGenreName?.let { name ->
        allGenres.find { it.name.equals(name, ignoreCase = true) }
          ?: MusicPlayerEngine.buildGenres(allTracks).find { it.name.equals(name, ignoreCase = true) }
      }

      val headerTitle = when {
        activeAlbum != null -> activeAlbum.name
        activeArtist != null -> activeArtist.name
        activeGenre != null -> activeGenre.name
        else -> "Music"
      }
      val headerSubtext = when {
        activeAlbum != null -> "${activeAlbum.artist} • ${activeAlbum.songCount} songs • ${activeAlbum.totalDurationFormatted}"
        activeArtist != null -> "${activeArtist.albumCount} albums • ${activeArtist.songCount} songs"
        activeGenre != null -> "${activeGenre.songCount} songs • ${activeGenre.artistCount} artists"
        else -> "${allArtists.size} artists • ${allAlbums.size} albums • ${allTracks.size} songs"
      }

      MusicPlayerTopBar(
        title = headerTitle,
        subtext = headerSubtext,
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
        onSearchQueryChange = { searchQuery = it },
        onToggleViewMode = {
          updateViewMode(viewMode.toggle())
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        onOpenSortMenu = { showSortMenu = true },
        onDismissSortMenu = { showSortMenu = false },
        onSelectSortOrder = { order ->
          updateSortOrder(order)
          showSortMenu = false
        },
        onOpenQueue = { showQueueSheet = true },
      )

      // 2. Category Tabs (Artists, Albums, Songs, Genres) + List/Card View Mode Selector
      MusicCategoryAndViewBar(
        selectedTab = selectedTab,
        viewMode = viewMode,
        accentColor = animatedDominant,
        onSelectTab = { tab ->
          clearDrillDown()
          updateTab(tab)
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        onSelectViewMode = { mode ->
          updateViewMode(mode)
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

          else -> {
            when (selectedTab) {
              MusicLibraryTab.ARTISTS -> {
                ArtistsTabContent(
                  artists = allArtists,
                  viewMode = viewMode,
                  currentArtistName = currentTrack?.artist,
                  isPlaying = isPlaying,
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
            isPlaying = !isPlaying
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
          volume = if (isMuted) 0f else volume,
          isMuted = isMuted,
          playbackSpeed = playbackSpeed,
          sleepTimerPreset = sleepTimerPreset,
          sleepRemainingMs = sleepRemainingMs,
          queueSize = playbackQueue.size,
          currentQueueIndex = currentQueueIndex,
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
            isDraggingSeek = true
            dragSeekFraction = fraction.coerceIn(0f, 1f)
          },
          onSeekFinished = {
            val targetMs = (dragSeekFraction * trackDurationMs).toLong().coerceIn(0L, trackDurationMs)
            currentPositionMs = targetMs
            isDraggingSeek = false
            try {
              mediaPlayerRef.value?.seekTo(targetMs.toInt())
            } catch (_: Throwable) {}
          },
          onToggleShuffle = { toggleShuffle() },
          onPrevious = { skipToPrevious() },
          onRewind10s = { seekRelativeMs(-10_000L) },
          onPlayPause = {
            isPlaying = !isPlaying
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          },
          onForward10s = { seekRelativeMs(10_000L) },
          onNext = { skipToNext(userInitiated = true) },
          onCycleRepeat = { cycleRepeatMode() },
          onVolumeChange = { newVol ->
            volume = newVol.coerceIn(0f, 1f)
            if (volume > 0f && isMuted) isMuted = false
          },
          onToggleMute = { isMuted = !isMuted },
          onCycleSpeed = {
            val idx = MusicPlayerEngine.PLAYBACK_SPEED_OPTIONS.indexOfFirst { it == playbackSpeed }
            val nextIdx = if (idx >= 0) (idx + 1) % MusicPlayerEngine.PLAYBACK_SPEED_OPTIONS.size else 2
            playbackSpeed = MusicPlayerEngine.PLAYBACK_SPEED_OPTIONS[nextIdx]
          },
          onOpenQueue = { showQueueSheet = true },
          onOpenTrackInfo = { showTrackInfoDialog = true },
          onOpenSleepTimerMenu = { showSleepTimerMenu = true },
          onDismissSleepTimerMenu = { showSleepTimerMenu = false },
          onSelectSleepTimer = { preset ->
            sleepTimerPreset = preset
            sleepRemainingMs = preset.durationMs
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

    // 6. Queue Dialog / Sheet
    if (showQueueSheet) {
      PlaybackQueueDialog(
        queue = playbackQueue,
        currentQueueIndex = currentQueueIndex,
        isPlaying = isPlaying,
        accentColor = animatedDominant,
        onSelectQueueIndex = { index ->
          stepToTrack(index, autoPlay = true)
        },
        onDismiss = { showQueueSheet = false },
      )
    }

    // 7. Track Details Dialog
    if (showTrackInfoDialog && currentTrack != null) {
      TrackDetailsDialog(
        track = currentTrack,
        palette = activePalette,
        onDismiss = { showTrackInfoDialog = false },
      )
    }
  }
}

@Composable
private fun MusicPlayerTopBar(
  title: String,
  subtext: String,
  hasDrillDown: Boolean,
  isSearchActive: Boolean,
  searchQuery: String,
  viewMode: MusicViewMode,
  sortOrder: MusicSortOrder,
  showSortMenu: Boolean,
  accentTint: Color,
  onBackClick: () -> Unit,
  onSearchToggle: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onToggleViewMode: () -> Unit,
  onOpenSortMenu: () -> Unit,
  onDismissSortMenu: () -> Unit,
  onSelectSortOrder: (MusicSortOrder) -> Unit,
  onOpenQueue: () -> Unit,
) {
  val focusRequester = remember { FocusRequester() }

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
    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 68.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
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

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            IconButton(
              onClick = onSearchToggle,
              modifier = Modifier.testTag("music_search_toggle"),
            ) {
              Icon(
                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                contentDescription = if (isSearchActive) "Close search" else "Search music",
                tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            IconButton(
              onClick = onToggleViewMode,
              modifier = Modifier.testTag("music_view_mode_toggle"),
            ) {
              Icon(
                imageVector = if (viewMode == MusicViewMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                contentDescription = if (viewMode == MusicViewMode.LIST) "Switch to Card view" else "Switch to List view",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

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

            IconButton(
              onClick = onOpenQueue,
              modifier = Modifier.testTag("music_queue_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Playback Queue",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      AnimatedVisibility(
        visible = isSearchActive,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text("Search artists, albums, songs, genres...") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions = KeyboardActions(onSearch = {}),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 10.dp)
            .focusRequester(focusRequester)
            .testTag("music_search_input"),
        )
      }
    }
  }
}

@Composable
private fun MusicCategoryAndViewBar(
  selectedTab: MusicLibraryTab,
  viewMode: MusicViewMode,
  accentColor: Color,
  onSelectTab: (MusicLibraryTab) -> Unit,
  onSelectViewMode: (MusicViewMode) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // 4 Library Category Tabs: Artists, Albums, Songs, Genres
    Row(
      modifier = Modifier
        .weight(1f)
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
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.testTag("music_tab_${tab.name}"),
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Explicit List / Card View Segmented Pill
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(2.dp),
      ) {
        MusicViewMode.entries.forEach { mode ->
          val active = viewMode == mode
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (active) lerp(MaterialTheme.colorScheme.primaryContainer, accentColor, 0.25f) else Color.Transparent,
            modifier = Modifier
              .clip(RoundedCornerShape(18.dp))
              .clickable { onSelectViewMode(mode) }
              .testTag("music_view_mode_${mode.name.lowercase()}"),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
              Icon(
                imageVector = if (mode == MusicViewMode.LIST) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                contentDescription = "${mode.displayName} view",
                tint = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = mode.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
              )
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
  currentArtistName: String?,
  isPlaying: Boolean,
  onSelectArtist: (MusicArtist) -> Unit,
  onPlayArtist: (MusicArtist) -> Unit,
) {
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

@Composable
private fun AlbumsTabContent(
  albums: List<MusicAlbum>,
  viewMode: MusicViewMode,
  currentAlbumName: String?,
  isPlaying: Boolean,
  onSelectAlbum: (MusicAlbum) -> Unit,
  onPlayAlbum: (MusicAlbum) -> Unit,
) {
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
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("music_play_all_button"),
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Play All", style = MaterialTheme.typography.labelMedium)
      }

      OutlinedButton(
        onClick = onShuffleAll,
        shape = RoundedCornerShape(12.dp),
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
            imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
          )
        },
        shape = RoundedCornerShape(20.dp),
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
  onSelectGenre: (MusicGenre) -> Unit,
  onPlayGenre: (MusicGenre) -> Unit,
) {
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
          }
          FilledTonalButton(
            onClick = { onPlayArtist(false) },
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Play")
          }
        }

        if (artist.albums.isNotEmpty()) {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(artist.albums, key = { it.name }) { album ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
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
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Play", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
              onClick = { onPlayAlbum(true) },
              shape = RoundedCornerShape(12.dp),
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
        }
        FilledTonalButton(
          onClick = { onPlayGenre(false) },
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Play All")
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
private fun SongListItemCard(
  track: MusicTrack,
  isCurrent: Boolean,
  isPlaying: Boolean,
  isFavorite: Boolean,
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
          text = "${track.artist} • ${track.album}",
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
          imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
        text = "${track.artist} • ${track.album}",
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
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
 * Renders a multi-stop vertical gradient derived directly from the active song's album art colors.
 */
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
  volume: Float,
  isMuted: Boolean,
  playbackSpeed: Float,
  sleepTimerPreset: SleepTimerPreset,
  sleepRemainingMs: Long,
  queueSize: Int,
  currentQueueIndex: Int,
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
  onRewind10s: () -> Unit,
  onPlayPause: () -> Unit,
  onForward10s: () -> Unit,
  onNext: () -> Unit,
  onCycleRepeat: () -> Unit,
  onVolumeChange: (Float) -> Unit,
  onToggleMute: () -> Unit,
  onCycleSpeed: () -> Unit,
  onOpenQueue: () -> Unit,
  onOpenTrackInfo: () -> Unit,
  onOpenSleepTimerMenu: () -> Unit,
  onDismissSleepTimerMenu: () -> Unit,
  onSelectSleepTimer: (SleepTimerPreset) -> Unit,
  onNavigateToArtist: () -> Unit,
  onNavigateToAlbum: () -> Unit,
) {
  val artworkScale by animateFloatAsState(
    targetValue = if (isPlaying) 1.0f else 0.93f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "artworkScale",
  )

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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 12.dp),
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

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          // Album art color swatch badge
          Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
            modifier = Modifier
              .clickable(onClick = onOpenTrackInfo)
              .testTag("expanded_player_palette_badge"),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(dominantColor),
              )
              Text(
                text = dominantHex,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
              )
            }
          }

          IconButton(
            onClick = onOpenTrackInfo,
            modifier = Modifier
              .size(36.dp)
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
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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

      // 5. Primary Transport Controls (Shuffle, Previous, Rewind 10s, Play/Pause, Forward 10s, Next, Repeat)
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

        IconButton(
          onClick = onRewind10s,
          modifier = Modifier
            .size(40.dp)
            .testTag("expanded_player_rewind_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Replay10,
            contentDescription = "Rewind 10 seconds",
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(24.dp),
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
          onClick = onForward10s,
          modifier = Modifier
            .size(40.dp)
            .testTag("expanded_player_forward_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Forward10,
            contentDescription = "Fast-forward 10 seconds",
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(24.dp),
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

      // 6. Volume Control Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        IconButton(
          onClick = onToggleMute,
          modifier = Modifier
            .size(32.dp)
            .testTag("expanded_player_mute_button"),
        ) {
          Icon(
            imageVector = if (isMuted || volume == 0f) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = if (isMuted) "Unmute" else "Mute",
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp),
          )
        }

        MusicSlimDotSlider(
          value = volume,
          onValueChange = onVolumeChange,
          accentColor = accentColor,
          inactiveColor = Color.White.copy(alpha = 0.22f),
          modifier = Modifier
            .weight(1f)
            .testTag("expanded_player_volume_slider"),
        )

        Text(
          text = "${(volume * 100).toInt()}%",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = Color.White.copy(alpha = 0.8f),
          modifier = Modifier.width(36.dp),
          textAlign = TextAlign.End,
        )
      }

      // 7. Secondary Utility Bar: Speed, Sleep Timer, Up Next Queue
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.White.copy(alpha = 0.14f),
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
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
            shape = RoundedCornerShape(16.dp),
            color = if (sleepTimerPreset != SleepTimerPreset.OFF) accentColor.copy(alpha = 0.28f)
            else Color.White.copy(alpha = 0.14f),
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
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

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.White.copy(alpha = 0.14f),
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenQueue)
            .testTag("expanded_player_up_next_button"),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.QueueMusic,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = "Queue ($queueSize)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
            )
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
private fun TrackArtworkThumbnail(
  track: MusicTrack?,
  size: Dp,
  shape: androidx.compose.ui.graphics.Shape,
  modifier: Modifier = Modifier,
) {
  val isInspection = LocalInspectionMode.current
  var bitmap by remember(track?.id) {
    mutableStateOf<Bitmap?>(
      if (track != null && !isInspection) MusicPlayerEngine.getCachedAlbumArtBitmap(track, 240)
      else null
    )
  }

  LaunchedEffect(track, isInspection) {
    if (track != null && !isInspection && bitmap == null) {
      bitmap = MusicPlayerEngine.loadAlbumArtBitmapAsync(track, sizePx = 240)
    }
  }

  val palette = remember(track?.id) {
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
  var bitmap by remember(track?.id) {
    mutableStateOf<Bitmap?>(
      if (track != null && !isInspection) MusicPlayerEngine.getCachedAlbumArtBitmap(track, 360)
      else null
    )
  }

  LaunchedEffect(track, isInspection) {
    if (track != null && !isInspection && bitmap == null) {
      bitmap = MusicPlayerEngine.loadAlbumArtBitmapAsync(track, sizePx = 360)
    }
  }

  val palette = remember(track?.id) {
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
  onDismiss: () -> Unit,
) {
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
        Column {
          Text(
            text = "Playback Queue",
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
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 320.dp)
          .testTag("music_queue_list"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        itemsIndexed(queue, key = { idx, t -> "${idx}_${t.id}" }) { index, item ->
          val isCurrent = index == currentQueueIndex
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
              .clickable { onSelectQueueIndex(index) },
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
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
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Done")
      }
    },
  )
}

@Composable
private fun TrackDetailsDialog(
  track: MusicTrack,
  palette: AlbumArtPalette,
  onDismiss: () -> Unit,
) {
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
            text = "Metadata and album art palette",
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

        DetailRow(label = "Genre", value = track.genre)
        DetailRow(label = "Year", value = track.year.toString())
        DetailRow(label = "Duration", value = track.durationFormatted, isMono = true)
        DetailRow(label = "Size", value = track.sizeFormatted, isMono = true)
        DetailRow(
          label = "Album Art Palette",
          value = "${palette.dominantHex} / ${palette.secondaryHex} / ${palette.accentHex}",
          isMono = true,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
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
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      fontFamily = if (isMono) FontFamily.Monospace else null,
      color = MaterialTheme.colorScheme.onSurface,
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
