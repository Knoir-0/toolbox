package com.example.toolbox.ui.tools

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.media.MediaMetadata
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.KeyEvent
import android.widget.Toast
import java.util.concurrent.ConcurrentHashMap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.toolbox.MainActivity
import com.example.toolbox.R
import com.example.toolbox.ui.main.EXTRA_EXPAND_MUSIC_PLAYER
import com.example.toolbox.ui.main.EXTRA_STANDALONE_TOOL
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.resolveStandaloneDocumentUri
import com.example.toolbox.ui.main.resolveToolShortcutAliasClassName
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

const val MUSIC_PLAYER_PREFS_NAME = "music_player_preferences"
const val PREF_KEY_MUSIC_VIEW_MODE = "music_player_view_mode"
const val PREF_KEY_MUSIC_CARDS_PER_ROW = "music_player_cards_per_row"
const val MIN_MUSIC_CARDS_PER_ROW = 2
const val MAX_MUSIC_CARDS_PER_ROW = 3
const val DEFAULT_MUSIC_CARDS_PER_ROW = 2
const val HOME_PLAYLIST_COUNT = 6
const val TARGET_HOME_PLAYLIST_DURATION_MS = 60L * 60_000L // ~1 hour target per generated Home playlist
const val PREF_KEY_MUSIC_TAB = "music_player_selected_tab"
const val PREF_KEY_MUSIC_TAB_ORDER = "music_player_tab_order"
const val PREF_KEY_MUSIC_SORT_ORDER = "music_player_sort_order"
const val PREF_KEY_MUSIC_GROUP_OPTION = "music_player_group_option"
const val PREF_KEY_MUSIC_SHUFFLE = "music_player_shuffle_enabled"
const val PREF_KEY_MUSIC_SHUFFLE_MODE = "music_player_shuffle_mode"
const val PREF_KEY_MUSIC_REPEAT = "music_player_repeat_mode"
const val PREF_KEY_MUSIC_FAVORITES = "music_player_favorite_ids"
const val PREF_KEY_MUSIC_FAVORITE_ARTISTS = "music_player_favorite_artists"
const val PREF_KEY_MUSIC_FAVORITE_ALBUMS = "music_player_favorite_albums"
const val PREF_KEY_MUSIC_FAVORITE_GENRES = "music_player_favorite_genres"
const val PREF_KEY_MUSIC_PLAYLISTS = "music_player_playlists"
const val PREF_KEY_MUSIC_PLAY_STATS = "music_player_play_stats"
const val PREF_KEY_MUSIC_INCLUDE_SAMPLES = "music_player_include_samples"
const val PREF_KEY_MUSIC_PREFERRED_APPS = "music_player_preferred_apps"
const val PREF_KEY_MUSIC_GRANTED_FOLDER_PATHS = "music_player_granted_folder_paths"
const val PREF_KEY_MUSIC_CUSTOM_FOLDERS = "music_player_custom_folders"
const val PREF_KEY_MUSIC_SAVED_SECTION_SORTS = "music_player_saved_section_sorts"

/**
 * Represents a storage folder in the Music Player tool (either the default `/Music` folder or a
 * user-added custom folder via Android's scoped storage access).
 */
@Immutable
data class MusicFolder(
  val folderName: String,
  val displayPath: String,
  val treeUri: String? = null,
  val customAbsolutePath: String? = null,
  val isCustom: Boolean = customAbsolutePath != null ||
    !(displayPath.equals("/Music", ignoreCase = true) || folderName.equals("Music", ignoreCase = true)),
) {
  val isDefaultMusicFolder: Boolean
    get() = !isCustom &&
      customAbsolutePath == null &&
      (displayPath.equals("/Music", ignoreCase = true) || folderName.equals("Music", ignoreCase = true))

  val pillLabel: String
    get() = folderName.ifBlank { displayPath.trim('/').substringAfterLast('/').ifBlank { "Music" } }

  val folderPath: String
    get() = displayPath

  val absolutePath: String?
    get() = customAbsolutePath
}

val DEFAULT_MUSIC_SCOPED_FOLDER = MusicFolder(
  folderName = "Music",
  displayPath = "/Music",
)

/**
 * Link to a preferred music or audio streaming app displayed in the floating bottom pill bar
 * on the Music Player Home view.
 */
@Immutable
data class PreferredMusicAppLink(
  val id: String,
  val label: String,
  val packageName: String,
  val webUrl: String = "",
  val accentRgb: Int = 0x1DB954,
  val isCustom: Boolean = false,
  val isInstalled: Boolean = false,
)

val DEFAULT_SCOPED_MUSIC_FOLDER_NAMES: List<String> = listOf("Music", "Audio", "Recordings")
val FULL_FILES_DEFAULT_MUSIC_FOLDER_NAMES: List<String> = listOf("Music", "Audio", "Recordings", "Download")
val DEFAULT_MUSIC_FOLDER_NAMES: List<String> = FULL_FILES_DEFAULT_MUSIC_FOLDER_NAMES
val SINGLE_ALBUM_FOLDER_NAMES: Set<String> = setOf("Audio", "Recordings")

/**
 * Primary library browse tabs in the Music Player tool.
 */
enum class MusicLibraryTab(
  val displayName: String,
) {
  HOME("Home"),
  PLAYLISTS("Playlists"),
  SONGS("Songs"),
  ALBUMS("Albums"),
  ARTISTS("Artists"),
  GENRES("Genres"),
  ;

  companion object {
    val DEFAULT: MusicLibraryTab = HOME

    fun fromNameOrDefault(name: String?): MusicLibraryTab {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
  }
}

/**
 * View presentation modes for Artist, Album, Song, and Genre tabs.
 */
enum class MusicViewMode(
  val displayName: String,
) {
  LIST("List"),
  CARD("Card"),
  ;

  fun toggle(): MusicViewMode = if (this == LIST) CARD else LIST

  companion object {
    val DEFAULT: MusicViewMode = LIST

    fun fromNameOrDefault(name: String?): MusicViewMode {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
  }
}

/**
 * Shuffle modes for playback queue ordering.
 */
enum class MusicShuffleMode(
  val displayName: String,
) {
  OFF("Shuffle Off"),
  SHUFFLE("Shuffle"),
  SMART_SHUFFLE("Smart Shuffle"),
  ;

  val isEnabled: Boolean
    get() = this != OFF

  fun next(): MusicShuffleMode = when (this) {
    OFF -> SHUFFLE
    SHUFFLE -> SMART_SHUFFLE
    SMART_SHUFFLE -> OFF
  }

  companion object {
    fun fromNameOrDefault(name: String?): MusicShuffleMode {
      if (name.isNullOrBlank()) return OFF
      if (name.equals("true", ignoreCase = true)) return SHUFFLE
      if (name.equals("false", ignoreCase = true)) return OFF
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: OFF
    }
  }
}

/**
 * Repeat modes for playback queue progression.
 */
enum class MusicRepeatMode(
  val displayName: String,
) {
  OFF("Repeat Off"),
  REPEAT_ALL("Repeat All"),
  REPEAT_ONE("Repeat One"),
  ;

  fun next(): MusicRepeatMode = when (this) {
    OFF -> REPEAT_ALL
    REPEAT_ALL -> REPEAT_ONE
    REPEAT_ONE -> OFF
  }

  companion object {
    fun fromNameOrDefault(name: String?): MusicRepeatMode {
      if (name.isNullOrBlank()) return OFF
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: OFF
    }
  }
}

/**
 * Sorting options for tracks in the Music Player.
 */
enum class MusicSortOrder(
  val displayName: String,
) {
  TITLE_ASC("Title (A to Z)"),
  TITLE_DESC("Title (Z to A)"),
  ARTIST_ASC("Artist (A to Z)"),
  ALBUM_ASC("Album (A to Z)"),
  DURATION_DESC("Duration (Longest)"),
  DURATION_ASC("Duration (Shortest)"),
  YEAR_DESC("Year (Newest)"),
  MOST_PLAYED("Most Played"),
  RECENTLY_PLAYED("Recently Played"),
  RANDOM("Random"),
  ;

  companion object {
    val DEFAULT: MusicSortOrder = TITLE_ASC

    fun fromNameOrDefault(name: String?): MusicSortOrder {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
  }
}

/**
 * Grouping options for organizing items in the Music Player Songs and Albums views.
 */
enum class MusicGroupOption(
  val displayName: String,
) {
  ALBUM("Album"),
  ARTIST("Artist"),
  GENRE("Genre"),
  YEAR("Year"),
  NONE("None"),
  ;

  companion object {
    val DEFAULT: MusicGroupOption = NONE

    val SONGS_OPTIONS: List<MusicGroupOption> = listOf(ALBUM, ARTIST, GENRE, YEAR, NONE)
    val ALBUMS_OPTIONS: List<MusicGroupOption> = listOf(ARTIST, GENRE, YEAR, NONE)

    fun fromNameOrDefault(name: String?): MusicGroupOption {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
  }
}

/**
 * Holds a combined group-by and sort-by configuration for a Music Player section or folder.
 */
@Immutable
data class MusicSortGroupConfig(
  val groupOption: MusicGroupOption = MusicGroupOption.DEFAULT,
  val sortOrder: MusicSortOrder = MusicSortOrder.DEFAULT,
)

/**
 * Sleep timer presets for automatic playback pause.
 */
enum class SleepTimerPreset(
  val label: String,
  val durationMs: Long,
) {
  OFF("Off", 0L),
  TEN_MIN("10 min", 10 * 60_000L),
  FIFTEEN_MIN("15 min", 15 * 60_000L),
  TWENTY_MIN("20 min", 20 * 60_000L),
  THIRTY_MIN("30 min", 30 * 60_000L),
  SIXTY_MIN("60 min", 60 * 60_000L),
  ;

  fun next(): SleepTimerPreset {
    val values = entries
    return values[(ordinal + 1) % values.size]
  }
}

/**
 * Color palette extracted from a track or album's artwork [Bitmap] so the Music Player
 * background, mini-player bar, and controls dynamically follow the album art.
 */
@Immutable
data class AlbumArtPalette(
  val dominantColor: Color,
  val secondaryColor: Color,
  val accentColor: Color,
  val darkSurfaceColor: Color,
  val deepBackgroundColor: Color,
  val onBackgroundColor: Color,
  val onSecondaryBackgroundColor: Color,
  val dominantHex: String,
  val secondaryHex: String,
  val accentHex: String,
)

/**
 * Represents a single music track in the library.
 */
@Immutable
data class MusicTrack(
  val id: String,
  val title: String,
  val artist: String,
  val album: String,
  val genre: String,
  val durationMs: Long,
  val trackNumber: Int = 1,
  val year: Int = 2026,
  val file: File? = null,
  val sizeBytes: Long = 0L,
  val lastModifiedMs: Long = 0L,
  val artworkPrimaryRgb: Int = 0x5E35B1,
  val artworkSecondaryRgb: Int = 0x1E88E5,
  val artworkAccentRgb: Int = 0xFF80AB,
  val isSampleTrack: Boolean = false,
  val sampleBaseFreqHz: Double = 261.63,
  val bitrateKbps: Int = 320,
  val sampleRateHz: Int = 44_100,
  val channels: Int = 2,
) {
  val durationFormatted: String = AudioRecorderEngine.formatDurationMs(durationMs)

  val sizeFormatted: String
    get() = if (sizeBytes > 0L) FileManagerEngine.formatFileSize(sizeBytes) else "2.4 MB"

  val trackNumberFormatted: String
    get() = "#$trackNumber"

  val formatLabel: String
    get() {
      val ext = file?.extension?.uppercase(Locale.US)?.takeIf { it.isNotEmpty() }
      return when {
        ext != null -> "$ext Audio"
        isSampleTrack -> "WAV (16-bit PCM)"
        else -> "MP3 Audio"
      }
    }

  val bitrateFormatted: String
    get() = "${bitrateKbps.coerceAtLeast(64)} kbps"

  val sampleRateFormatted: String
    get() = String.format(Locale.US, "%.1f kHz", sampleRateHz.coerceAtLeast(8_000) / 1000.0)

  val channelsLabel: String
    get() = if (channels <= 1) "Mono (1 ch)" else "Stereo ($channels ch)"

  val sourceLabel: String
    get() = file?.name ?: if (isSampleTrack) "${id}.wav (Sample Library)" else "${title}.mp3"
}

/**
 * Represents an Album aggregated from tracks in the library.
 */
@Immutable
data class MusicAlbum(
  val name: String,
  val artist: String,
  val year: Int,
  val genre: String,
  val tracks: List<MusicTrack>,
) {
  val key: String = "$name|$artist"
  val songCount: Int = tracks.size
  val totalDurationMs: Long = tracks.sumOf { it.durationMs }
  val totalDurationFormatted: String = AudioRecorderEngine.formatDurationMs(totalDurationMs)
  val representativeTrack: MusicTrack? = tracks.firstOrNull()
  val summaryText: String = run {
    val countLabel = if (songCount == 1) "1 song" else "$songCount songs"
    "$artist • $countLabel"
  }
  private val trackIdSet: Set<String> =
    if (tracks.isEmpty()) emptySet() else tracks.mapTo(HashSet(tracks.size)) { it.id }

  fun containsTrack(trackId: String?): Boolean = !trackId.isNullOrBlank() && trackId in trackIdSet
}

/**
 * Represents an Artist aggregated from tracks and albums in the library.
 */
@Immutable
data class MusicArtist(
  val name: String,
  val albums: List<MusicAlbum>,
  val tracks: List<MusicTrack>,
) {
  val albumCount: Int = albums.size
  val songCount: Int = tracks.size
  val totalDurationMs: Long = tracks.sumOf { it.durationMs }
  val totalDurationFormatted: String = AudioRecorderEngine.formatDurationMs(totalDurationMs)
  val representativeTrack: MusicTrack? = tracks.firstOrNull()
  val genres: List<String> = if (tracks.isEmpty()) emptyList() else tracks.map { it.genre }.distinct()
  val summaryText: String = run {
    val albumLabel = if (albumCount == 1) "1 album" else "$albumCount albums"
    val songLabel = if (songCount == 1) "1 song" else "$songCount songs"
    "$albumLabel • $songLabel"
  }
  private val trackIdSet: Set<String> =
    if (tracks.isEmpty()) emptySet() else tracks.mapTo(HashSet(tracks.size)) { it.id }

  fun containsTrack(trackId: String?): Boolean = !trackId.isNullOrBlank() && trackId in trackIdSet
}

/**
 * Represents a musical Genre aggregated from tracks in the library.
 */
@Immutable
data class MusicGenre(
  val name: String,
  val tracks: List<MusicTrack>,
  val albums: List<MusicAlbum>,
  val artists: List<String>,
) {
  val songCount: Int = tracks.size
  val albumCount: Int = albums.size
  val artistCount: Int = artists.size
  val totalDurationMs: Long = tracks.sumOf { it.durationMs }
  val totalDurationFormatted: String = AudioRecorderEngine.formatDurationMs(totalDurationMs)
  val representativeTrack: MusicTrack? = tracks.firstOrNull()
  val summaryText: String = run {
    val songLabel = if (songCount == 1) "1 song" else "$songCount songs"
    val artistLabel = if (artistCount == 1) "1 artist" else "$artistCount artists"
    "$songLabel • $artistLabel"
  }
  private val trackIdSet: Set<String> =
    if (tracks.isEmpty()) emptySet() else tracks.mapTo(HashSet(tracks.size)) { it.id }

  fun containsTrack(trackId: String?): Boolean = !trackId.isNullOrBlank() && trackId in trackIdSet
}

/**
 * Persisted playback statistics for a single track.
 */
@Immutable
data class TrackPlayStats(
  val playCount: Int = 0,
  val totalPlayTimeMs: Long = 0L,
  val lastPlayedAtMs: Long = 0L,
  val skipCount: Int = 0,
) {
  companion object {
    /**
     * Maximum points contributed by recency (1/4 of a single play's 60,000 points),
     * weighting recent plays into Most Played without overpowering play count or full plays.
     */
    const val MAX_RECENCY_BONUS_POINTS = 15_000L
  }

  /**
   * Computes a bounded recency bonus (`0..MAX_RECENCY_BONUS_POINTS`) from [lastPlayedAtMs]
   * relative to [referenceTimeMs] so recently played tracks receive a light boost in Most Played
   * without overpowering play count, calculated full plays, or total listen time.
   */
  fun recencyBonusPoints(referenceTimeMs: Long = 0L): Long {
    if (lastPlayedAtMs <= 0L || !hasBeenPlayed) return 0L
    return if (referenceTimeMs > 0L) {
      val ref = max(referenceTimeMs, lastPlayedAtMs)
      val ageMs = (ref - lastPlayedAtMs).coerceAtLeast(0L)
      val decay = 1.0 / (1.0 + kotlin.math.ln(1.0 + ageMs.toDouble()) / 10.0)
      (MAX_RECENCY_BONUS_POINTS.toDouble() * decay).roundToLong()
    } else {
      val scale = kotlin.math.ln(1.0 + lastPlayedAtMs.toDouble()) /
        kotlin.math.ln(1.0 + 2_000_000_000_000.0)
      (MAX_RECENCY_BONUS_POINTS.toDouble() * scale.coerceIn(0.0, 1.0)).roundToLong()
    }
  }

  /**
   * Combined engagement score blending play count and total listen time:
   * each play contributes 60,000 points (equivalent to 1 minute of listening) plus 1 point per
   * millisecond of actual listen time, a light recency bonus, and a tie-breaker penalty per skip.
   */
  val engagementScore: Long
    get() = playCount.toLong() * 60_000L + totalPlayTimeMs + recencyBonusPoints() - skipCount.toLong() * 25L

  /**
   * Number of equivalent full track completions based on [totalPlayTimeMs] divided by [trackDurationMs].
   */
  fun calculatedFullPlays(trackDurationMs: Long): Double {
    if (totalPlayTimeMs <= 0L) return 0.0
    val safeDurationMs = trackDurationMs.coerceAtLeast(1_000L).toDouble()
    return totalPlayTimeMs.toDouble() / safeDurationMs
  }

  /**
   * Human-friendly formatted value for [calculatedFullPlays] (e.g. "0", "2", or "1.5").
   */
  fun formattedFullPlays(trackDurationMs: Long): String {
    val fullPlays = calculatedFullPlays(trackDurationMs)
    if (fullPlays < 0.05) return "0"
    val roundedTenths = (fullPlays * 10.0).roundToLong()
    return if (roundedTenths % 10L == 0L) {
      (roundedTenths / 10L).toString()
    } else {
      String.format(Locale.US, "%.1f", roundedTenths / 10.0)
    }
  }

  /**
   * Track-aware engagement score used to sort the Most Played playlist:
   * combines play count, calculated full plays (60,000 points per full play), and total listen time,
   * with a light recency bonus from [lastPlayedAtMs] and skips weighted very lightly (-25 points per skip,
   * or `0` when [ignoreSkips] is `true`).
   */
  fun engagementScoreForTrack(
    trackDurationMs: Long,
    referenceTimeMs: Long = 0L,
    ignoreSkips: Boolean = false,
  ): Long {
    val fullPlaysScore = (calculatedFullPlays(trackDurationMs) * 60_000.0).roundToLong()
    val skipPenalty = if (ignoreSkips) 0L else skipCount.toLong() * 25L
    val recencyBonus = recencyBonusPoints(referenceTimeMs)
    return playCount.toLong() * 60_000L + fullPlaysScore + totalPlayTimeMs + recencyBonus - skipPenalty
  }

  val hasBeenPlayed: Boolean
    get() = playCount > 0 || totalPlayTimeMs > 0L || lastPlayedAtMs > 0L

  val hasRecordedActivity: Boolean
    get() = hasBeenPlayed || skipCount > 0

  val totalPlayTimeFormatted: String
    get() = AudioRecorderEngine.formatDurationMs(totalPlayTimeMs)

  val lastPlayedFormatted: String
    get() = if (lastPlayedAtMs > 0L) FileManagerEngine.formatTimestamp(lastPlayedAtMs) else "Never"
}

/**
 * Persisted metadata entry for a user-saved Playlist.
 */
@Immutable
data class SavedPlaylistEntry(
  val id: String,
  val name: String,
  val trackIds: List<String>,
  val createdAtMs: Long = 0L,
)

/**
 * Represents a resolved Playlist of tracks in the Music Player.
 */
@Immutable
data class MusicPlaylist(
  val id: String,
  val name: String,
  val tracks: List<MusicTrack>,
  val createdAtMs: Long = 0L,
  val subtitle: String = "",
  val isGeneratedHomeMix: Boolean = false,
) {
  val isFavorites: Boolean = id == MusicPlayerEngine.FAVORITES_PLAYLIST_ID
  val isMostPlayed: Boolean = id == MusicPlayerEngine.MOST_PLAYED_PLAYLIST_ID
  val isRecentlyPlayed: Boolean = id == MusicPlayerEngine.RECENTLY_PLAYED_PLAYLIST_ID
  val isBuiltIn: Boolean = isFavorites || isMostPlayed || isRecentlyPlayed || isGeneratedHomeMix
  val songCount: Int = tracks.size
  val totalDurationMs: Long = tracks.sumOf { it.durationMs }
  val totalDurationFormatted: String = AudioRecorderEngine.formatDurationMs(totalDurationMs)
  val representativeTrack: MusicTrack? = tracks.firstOrNull()
  val artists: List<String> = if (tracks.isEmpty()) emptyList() else tracks.map { it.artist }.distinct()
  val genres: List<String> =
    if (tracks.isEmpty()) emptyList() else tracks.map { it.genre }.filter { it.isNotBlank() }.distinct()
  val summaryText: String = run {
    val songLabel = if (songCount == 1) "1 song" else "$songCount songs"
    if (subtitle.isNotBlank()) {
      "$subtitle • $songLabel • $totalDurationFormatted"
    } else {
      "$songLabel • $totalDurationFormatted"
    }
  }
  private val trackIdSet: Set<String> =
    if (tracks.isEmpty()) emptySet() else tracks.mapTo(HashSet(tracks.size)) { it.id }

  fun containsTrack(trackId: String?): Boolean = !trackId.isNullOrBlank() && trackId in trackIdSet
}

/**
 * Represents a grouped section of [MusicTrack] items under a common [title] header.
 */
@Immutable
data class MusicTrackGroup(
  val title: String,
  val tracks: List<MusicTrack>,
)

/**
 * Represents a grouped section of [MusicAlbum] items under a common [title] header.
 */
@Immutable
data class MusicAlbumGroup(
  val title: String,
  val albums: List<MusicAlbum>,
)

/**
 * Core engine for the Music Player tool:
 * - Scans local music directories and extracts ID3 / audio metadata
 * - Provides a rich built-in multi-genre sample library with synthesized harmonic WAV audio
 * - Generates and caches album art [Bitmap] covers
 * - Extracts dynamic [AlbumArtPalette] colors from album art bitmaps so the player background follows the album art
 * - Groups tracks into Artists, Albums, Songs, and Genres
 * - Manages queue navigation, shuffle, repeat modes, sorting, and filtering
 */
object MusicPlayerEngine {

  val SUPPORTED_MUSIC_EXTENSIONS: Set<String> = setOf(
    "mp3", "m4a", "wav", "flac", "ogg", "aac", "opus",
  )

  val PLAYBACK_SPEED_OPTIONS: List<Float> = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

  const val MUSIC_INDEX_CACHE_FILE_NAME = "music_library_index_v2.json"

  const val MEDIA_NOTIFICATION_CHANNEL_ID = "music_player_media_channel"
  const val MEDIA_NOTIFICATION_CHANNEL_NAME = "Music Playback Controls"
  const val MEDIA_NOTIFICATION_ID = 4102

  const val ACTION_MEDIA_PLAY = "com.example.toolbox.music.ACTION_PLAY"
  const val ACTION_MEDIA_PAUSE = "com.example.toolbox.music.ACTION_PAUSE"
  const val ACTION_MEDIA_PLAY_PAUSE = "com.example.toolbox.music.ACTION_PLAY_PAUSE"
  const val ACTION_MEDIA_NEXT = "com.example.toolbox.music.ACTION_NEXT"
  const val ACTION_MEDIA_PREVIOUS = "com.example.toolbox.music.ACTION_PREVIOUS"
  const val ACTION_MEDIA_TOGGLE_FAVORITE = "com.example.toolbox.music.ACTION_TOGGLE_FAVORITE"
  const val ACTION_MEDIA_CYCLE_REPEAT = "com.example.toolbox.music.ACTION_CYCLE_REPEAT"
  const val ACTION_MEDIA_CYCLE_SHUFFLE = "com.example.toolbox.music.ACTION_CYCLE_SHUFFLE"
  const val ACTION_MEDIA_STOP = "com.example.toolbox.music.ACTION_STOP"

  val SYSTEM_MEDIA_PLAYBACK_ACTIONS: Long =
    PlaybackState.ACTION_PLAY or
      PlaybackState.ACTION_PAUSE or
      PlaybackState.ACTION_PLAY_PAUSE or
      PlaybackState.ACTION_STOP or
      PlaybackState.ACTION_SKIP_TO_NEXT or
      PlaybackState.ACTION_SKIP_TO_PREVIOUS or
      PlaybackState.ACTION_SEEK_TO or
      PlaybackState.ACTION_SET_PLAYBACK_SPEED

  /**
   * Resolves the state-aware vector drawable resource ID for the Shuffle button in the Android
   * system media player controls.
   */
  fun resolveSystemMediaShuffleIconRes(mode: MusicShuffleMode): Int = when (mode) {
    MusicShuffleMode.OFF -> R.drawable.ic_media_shuffle_off
    MusicShuffleMode.SHUFFLE -> R.drawable.ic_media_shuffle_on
    MusicShuffleMode.SMART_SHUFFLE -> R.drawable.ic_media_shuffle_smart
  }

  /**
   * Resolves the accessibility / action label for the Shuffle button in the Android system media
   * player controls.
   */
  fun resolveSystemMediaShuffleLabel(mode: MusicShuffleMode): String = when (mode) {
    MusicShuffleMode.OFF -> "Shuffle: Off"
    MusicShuffleMode.SHUFFLE -> "Shuffle: On"
    MusicShuffleMode.SMART_SHUFFLE -> "Smart Shuffle: On"
  }

  /**
   * Resolves the state-aware vector drawable resource ID for the Repeat / Replay button in the
   * Android system media player controls.
   */
  fun resolveSystemMediaRepeatIconRes(mode: MusicRepeatMode): Int = when (mode) {
    MusicRepeatMode.OFF -> R.drawable.ic_media_repeat_off
    MusicRepeatMode.REPEAT_ALL -> R.drawable.ic_media_repeat_all
    MusicRepeatMode.REPEAT_ONE -> R.drawable.ic_media_repeat_one
  }

  /**
   * Resolves the accessibility / action label for the Repeat / Replay button in the Android system
   * media player controls.
   */
  fun resolveSystemMediaRepeatLabel(mode: MusicRepeatMode): String = when (mode) {
    MusicRepeatMode.OFF -> "Repeat: Off"
    MusicRepeatMode.REPEAT_ALL -> "Repeat: All"
    MusicRepeatMode.REPEAT_ONE -> "Repeat: One"
  }

  /**
   * Resolves the state-aware vector drawable resource ID for the individual song's Favorite button
   * in the Android system media player controls.
   */
  fun resolveSystemMediaFavoriteIconRes(isSongFavorite: Boolean): Int =
    if (isSongFavorite) R.drawable.ic_media_favorite_on else R.drawable.ic_media_favorite_off

  /**
   * Resolves the accessibility / action label for the individual song's Favorite button in the
   * Android system media player controls.
   */
  fun resolveSystemMediaFavoriteLabel(isSongFavorite: Boolean): String =
    if (isSongFavorite) "Remove song from Favorites" else "Add song to Favorites"

  /**
   * Builds the [Intent] used when clicking on the Android system media player controls so that the
   * app opens directly into the standalone Music Player tool with the full player view expanded,
   * reusing the existing activity/session without stopping playback.
   */
  fun createSystemMediaLaunchIntent(context: Context): Intent {
    return Intent(context, MainActivity::class.java).apply {
      component = ComponentName(context.packageName, resolveToolShortcutAliasClassName(Tool.MUSIC))
      action = Intent.ACTION_VIEW
      data = resolveStandaloneDocumentUri(Tool.MUSIC)
      putExtra(EXTRA_STANDALONE_TOOL, Tool.MUSIC.name)
      putExtra(EXTRA_EXPAND_MUSIC_PLAYER, true)
      addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK or
          Intent.FLAG_ACTIVITY_SINGLE_TOP or
          Intent.FLAG_ACTIVITY_CLEAR_TOP
      )
    }
  }

  /**
   * Returns `true` if [deviceType] is a Bluetooth audio output device (A2DP, SCO, BLE Headset/Speaker/Broadcast, or Hearing Aid).
   */
  fun isBluetoothAudioDeviceType(deviceType: Int): Boolean {
    return deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
      deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
      deviceType == AudioDeviceInfo.TYPE_HEARING_AID ||
      deviceType == 26 || // AudioDeviceInfo.TYPE_BLE_HEADSET (API 31+)
      deviceType == 27 || // AudioDeviceInfo.TYPE_BLE_SPEAKER (API 31+)
      deviceType == 30    // AudioDeviceInfo.TYPE_BLE_BROADCAST (API 33+)
  }

  /**
   * Returns `true` if [deviceType] is a Bluetooth or wired/USB headphone audio output device.
   */
  fun isHeadphoneOrBluetoothAudioDeviceType(deviceType: Int): Boolean {
    return isBluetoothAudioDeviceType(deviceType) ||
      deviceType == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
      deviceType == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
      deviceType == AudioDeviceInfo.TYPE_USB_HEADSET
  }

  /**
   * Evaluates whether an incoming system audio/Bluetooth broadcast action and state transition
   * indicates that Bluetooth headphones (or active external headphones) have disconnected and
   * playback should pause.
   */
  fun shouldPauseOnAudioDisconnectBroadcast(
    action: String?,
    bluetoothState: Int = BluetoothProfile.STATE_DISCONNECTED,
    previousBluetoothState: Int = BluetoothProfile.STATE_CONNECTED,
    scoAudioState: Int = AudioManager.SCO_AUDIO_STATE_DISCONNECTED,
  ): Boolean {
    if (action.isNullOrBlank()) return false
    return when (action) {
      AudioManager.ACTION_AUDIO_BECOMING_NOISY -> true
      BluetoothDevice.ACTION_ACL_DISCONNECTED -> true
      BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
      BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
        (bluetoothState == BluetoothProfile.STATE_DISCONNECTED ||
          bluetoothState == BluetoothProfile.STATE_DISCONNECTING) &&
          previousBluetoothState != BluetoothProfile.STATE_DISCONNECTED
      }
      AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
        scoAudioState == AudioManager.SCO_AUDIO_STATE_DISCONNECTED &&
          previousBluetoothState == AudioManager.SCO_AUDIO_STATE_CONNECTED
      }
      else -> false
    }
  }

  private const val MAX_ARTWORK_CACHE_ENTRIES = 256
  private const val MAX_PALETTE_CACHE_ENTRIES = 512
  private const val MAX_TRACK_METADATA_CACHE_ENTRIES = 2048
  private const val MAX_NO_ARTWORK_CACHE_ENTRIES = 1024

  val DEFAULT_MUSIC_SCOPED_FOLDER: MusicFolder = com.example.toolbox.ui.tools.DEFAULT_MUSIC_SCOPED_FOLDER

  private val artworkDispatcher = Dispatchers.IO.limitedParallelism(4)
  private val artworkDecodeLocks = Array(16) { Any() }
  private val artworkCacheLock = Any()
  private val metadataCacheLock = Any()

  @Volatile
  private var artworkDiskCacheDir: File? = null

  @Volatile
  private var trackIndexDiskCacheFile: File? = null

  @Volatile
  private var diskIndexLoadedForPath: String? = null

  private val artworkBitmapCache = object : LinkedHashMap<String, Bitmap>(64, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
      return size > MAX_ARTWORK_CACHE_ENTRIES
    }
  }
  private val proceduralArtworkKeys = HashSet<String>()
  private val paletteCache = object : LinkedHashMap<String, AlbumArtPalette>(128, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AlbumArtPalette>?): Boolean {
      return size > MAX_PALETTE_CACHE_ENTRIES
    }
  }
  private val fallbackPaletteCache = object : LinkedHashMap<String, AlbumArtPalette>(128, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AlbumArtPalette>?): Boolean {
      return size > MAX_PALETTE_CACHE_ENTRIES
    }
  }
  private val noExternalArtworkCache = object : LinkedHashMap<String, Boolean>(128, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
      return size > MAX_NO_ARTWORK_CACHE_ENTRIES
    }
  }
  private val trackMetadataCache = object : LinkedHashMap<String, MusicTrack>(256, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, MusicTrack>?): Boolean {
      return size > MAX_TRACK_METADATA_CACHE_ENTRIES
    }
  }
  private val trackIndexByPath = object : LinkedHashMap<String, MusicTrack>(256, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, MusicTrack>?): Boolean {
      return size > MAX_TRACK_METADATA_CACHE_ENTRIES
    }
  }
  private val libraryTracksCache = object : LinkedHashMap<String, List<MusicTrack>>(8, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<MusicTrack>>?): Boolean {
      return size > 8
    }
  }

  /**
   * Returns the default persistent JSON cache file for Music library track metadata indexing.
   */
  fun getDefaultIndexCacheFile(context: Context): File {
    return File(context.filesDir, MUSIC_INDEX_CACHE_FILE_NAME)
  }

  /**
   * Configures an optional disk cache directory for persisted album art thumbnails and track metadata index.
   */
  fun configureDiskCache(cacheDir: File?, indexCacheFile: File? = null) {
    if (cacheDir == null) {
      artworkDiskCacheDir = null
      if (indexCacheFile == null) {
        trackIndexDiskCacheFile = null
      }
      return
    }
    val dir = File(cacheDir, "music_player_artwork_cache")
    try {
      if (!dir.exists()) dir.mkdirs()
      artworkDiskCacheDir = dir
    } catch (_: Throwable) {
      artworkDiskCacheDir = null
    }
    try {
      trackIndexDiskCacheFile = indexCacheFile ?: File(cacheDir, MUSIC_INDEX_CACHE_FILE_NAME)
    } catch (_: Throwable) {
      trackIndexDiskCacheFile = null
    }
  }

  /**
   * Clears in-memory and disk album art and palette caches.
   */
  fun clearArtworkCache() {
    synchronized(artworkCacheLock) {
      artworkBitmapCache.clear()
      proceduralArtworkKeys.clear()
      paletteCache.clear()
      fallbackPaletteCache.clear()
      noExternalArtworkCache.clear()
    }
    clearExternalMediaArtworkCaches()
    try {
      artworkDiskCacheDir?.listFiles()?.forEach { it.delete() }
    } catch (_: Throwable) {}
  }

  /**
   * Clears cached track metadata and library scan results.
   */
  fun clearTrackMetadataCache() {
    synchronized(metadataCacheLock) {
      trackMetadataCache.clear()
      trackIndexByPath.clear()
      libraryTracksCache.clear()
      diskIndexLoadedForPath = null
    }
  }

  /**
   * Returns a cached [MusicTrack] for [file] from the in-memory index if its `lastModifiedMs` and
   * `sizeBytes` signature still matches [file].
   */
  fun getCachedTrackIndex(file: File): MusicTrack? {
    val path = file.absolutePath
    val cached = synchronized(metadataCacheLock) {
      trackIndexByPath[path]
    } ?: return null
    return if (file.lastModified() == cached.lastModifiedMs && file.length() == cached.sizeBytes) {
      cached
    } else {
      null
    }
  }

  /**
   * Stores or updates [track] in the in-memory metadata and path index caches.
   */
  fun putCachedTrackIndex(track: MusicTrack) {
    val file = track.file ?: return
    val path = file.absolutePath
    val baseKey = "$path:${track.lastModifiedMs}:${track.sizeBytes}"
    synchronized(metadataCacheLock) {
      trackIndexByPath[path] = track
      trackMetadataCache["$baseKey:true"] = track
      trackMetadataCache["$baseKey:false"] = track
    }
  }

  /**
   * Updates all in-memory cached library track lists and path index entries after an in-place
   * rename, duplicate, or delete operation without invalidating the entire library cache.
   */
  internal fun updateCachedLibrarySnapshot(updatedTracks: List<MusicTrack>) {
    synchronized(metadataCacheLock) {
      trackIndexByPath.clear()
      for (track in updatedTracks) {
        val file = track.file ?: continue
        val path = file.absolutePath
        val baseKey = "$path:${track.lastModifiedMs}:${track.sizeBytes}"
        trackIndexByPath[path] = track
        trackMetadataCache["$baseKey:true"] = track
        trackMetadataCache["$baseKey:false"] = track
      }
      val existingKeys = libraryTracksCache.keys.toList()
      for (key in existingKeys) {
        libraryTracksCache[key] = updatedTracks
      }
    }
  }

  private fun trackToJson(track: MusicTrack): JSONObject? {
    val file = track.file ?: return null
    if (track.isSampleTrack) return null
    return JSONObject().apply {
      put("id", track.id)
      put("path", file.absolutePath)
      put("lastModifiedMs", track.lastModifiedMs)
      put("sizeBytes", track.sizeBytes)
      put("title", track.title)
      put("artist", track.artist)
      put("album", track.album)
      put("genre", track.genre)
      put("durationMs", track.durationMs)
      put("trackNumber", track.trackNumber)
      put("year", track.year)
      put("artworkPrimaryRgb", track.artworkPrimaryRgb)
      put("artworkSecondaryRgb", track.artworkSecondaryRgb)
      put("artworkAccentRgb", track.artworkAccentRgb)
      put("bitrateKbps", track.bitrateKbps)
      put("sampleRateHz", track.sampleRateHz)
    }
  }

  private fun trackFromJson(obj: JSONObject): MusicTrack? {
    val path = obj.optString("path", "").trim()
    if (path.isEmpty()) return null
    val file = File(path)
    val lastMod = obj.optLong("lastModifiedMs", 0L)
    val sizeBytes = obj.optLong("sizeBytes", 0L)
    val title = obj.optString("title", "").trim().ifEmpty { file.nameWithoutExtension }
    val artist = obj.optString("artist", "Local Artist").trim().ifEmpty { "Local Artist" }
    val album = obj.optString("album", "Local Recordings").trim().ifEmpty { "Local Recordings" }
    val genre = obj.optString("genre", "Local Audio").trim().ifEmpty { "Local Audio" }
    val durationMs = obj.optLong("durationMs", 180_000L).coerceAtLeast(1_000L)
    val trackNumber = obj.optInt("trackNumber", 1).coerceAtLeast(1)
    val year = obj.optInt("year", 2026)
    val (fallbackPrimary, fallbackSecondary, fallbackAccent) = computeDeterministicArtworkColors(
      album = album,
      artist = artist,
      genre = genre,
    )
    val primaryRgb = obj.optInt("artworkPrimaryRgb", fallbackPrimary)
    val secondaryRgb = obj.optInt("artworkSecondaryRgb", fallbackSecondary)
    val accentRgb = obj.optInt("artworkAccentRgb", fallbackAccent)
    val bitrateKbps = obj.optInt("bitrateKbps", 320)
    val sampleRateHz = obj.optInt("sampleRateHz", 44_100)
    val id = obj.optString("id", "file_$path").ifBlank { "file_$path" }
    return MusicTrack(
      id = id,
      title = title,
      artist = artist,
      album = album,
      genre = genre,
      durationMs = durationMs,
      trackNumber = trackNumber,
      year = year,
      file = file,
      sizeBytes = sizeBytes,
      lastModifiedMs = lastMod,
      artworkPrimaryRgb = primaryRgb,
      artworkSecondaryRgb = secondaryRgb,
      artworkAccentRgb = accentRgb,
      isSampleTrack = false,
      bitrateKbps = bitrateKbps,
      sampleRateHz = sampleRateHz,
    )
  }

  /**
   * Loads persisted [MusicTrack] metadata entries from [cacheFile] on disk and merges them into
   * the in-memory metadata cache, pruning entries whose files no longer exist or have modified signatures.
   */
  fun loadTrackIndexCacheFromDisk(
    cacheFile: File? = trackIndexDiskCacheFile,
  ): Map<String, MusicTrack> {
    if (cacheFile == null || !cacheFile.exists() || !cacheFile.isFile) {
      synchronized(metadataCacheLock) {
        return LinkedHashMap(trackIndexByPath)
      }
    }
    val absPath = cacheFile.absolutePath
    synchronized(metadataCacheLock) {
      if (diskIndexLoadedForPath == absPath && trackIndexByPath.isNotEmpty()) {
        return LinkedHashMap(trackIndexByPath)
      }
    }
    try {
      val raw = cacheFile.readText(Charsets.UTF_8)
      if (raw.isNotBlank()) {
        val root = JSONObject(raw)
        val entriesArray = root.optJSONArray("entries")
        if (entriesArray != null) {
          val validated = ArrayList<MusicTrack>(entriesArray.length())
          for (i in 0 until entriesArray.length()) {
            val obj = entriesArray.optJSONObject(i) ?: continue
            val parsed = trackFromJson(obj) ?: continue
            val file = parsed.file ?: continue
            if (file.exists() && file.isFile &&
              file.lastModified() == parsed.lastModifiedMs &&
              file.length() == parsed.sizeBytes
            ) {
              validated.add(parsed)
            }
          }
          synchronized(metadataCacheLock) {
            for (track in validated) {
              val path = track.file!!.absolutePath
              val baseKey = "$path:${track.lastModifiedMs}:${track.sizeBytes}"
              trackIndexByPath[path] = track
              trackMetadataCache["$baseKey:true"] = track
              trackMetadataCache["$baseKey:false"] = track
            }
            diskIndexLoadedForPath = absPath
          }
        }
      }
    } catch (_: Throwable) {
      // Ignore corrupt cache file and rebuild on next pass
    }
    synchronized(metadataCacheLock) {
      return LinkedHashMap(trackIndexByPath)
    }
  }

  /**
   * Persists the provided [tracks] (or current in-memory indexed tracks) to [cacheFile] on disk.
   */
  fun saveTrackIndexCacheToDisk(
    cacheFile: File? = trackIndexDiskCacheFile,
    tracks: Collection<MusicTrack>? = null,
  ) {
    val targetFile = cacheFile ?: return
    val snapshot = synchronized(metadataCacheLock) {
      if (tracks != null) {
        for (t in tracks) {
          val f = t.file ?: continue
          if (!t.isSampleTrack) {
            trackIndexByPath[f.absolutePath] = t
          }
        }
      }
      trackIndexByPath.values.toList()
    }
    try {
      targetFile.parentFile?.mkdirs()
      val arr = JSONArray()
      for (track in snapshot) {
        val json = trackToJson(track) ?: continue
        arr.put(json)
      }
      val root = JSONObject().apply {
        put("version", 1)
        put("entries", arr)
      }
      val tmpFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
      tmpFile.writeText(root.toString(), Charsets.UTF_8)
      if (tmpFile.exists() && tmpFile.length() > 0L) {
        tmpFile.renameTo(targetFile)
      } else {
        tmpFile.delete()
      }
    } catch (_: Throwable) {
      // Best-effort persistence
    }
  }

  /**
   * Curated starter library with distinct artists, albums, songs, genres, and vivid album art palettes.
   */
  val DEFAULT_SAMPLE_TRACKS: List<MusicTrack> = listOf(
    MusicTrack(
      id = "sample_neon_horizon",
      title = "Neon Horizon",
      artist = "Aurora Synth",
      album = "Midnight Odyssey",
      genre = "Synthwave",
      durationMs = 14_000L,
      trackNumber = 1,
      year = 2026,
      artworkPrimaryRgb = 0x00B4D8,
      artworkSecondaryRgb = 0x7209B7,
      artworkAccentRgb = 0x4CC9F0,
      isSampleTrack = true,
      sampleBaseFreqHz = 261.63, // C4
    ),
    MusicTrack(
      id = "sample_starlight_drive",
      title = "Starlight Drive",
      artist = "Aurora Synth",
      album = "Midnight Odyssey",
      genre = "Synthwave",
      durationMs = 16_000L,
      trackNumber = 2,
      year = 2026,
      artworkPrimaryRgb = 0x00B4D8,
      artworkSecondaryRgb = 0x7209B7,
      artworkAccentRgb = 0x4CC9F0,
      isSampleTrack = true,
      sampleBaseFreqHz = 293.66, // D4
    ),
    MusicTrack(
      id = "sample_cybernetic_pulse",
      title = "Cybernetic Pulse",
      artist = "Aurora Synth",
      album = "Solaris Grid",
      genre = "Electronic",
      durationMs = 15_000L,
      trackNumber = 1,
      year = 2025,
      artworkPrimaryRgb = 0xEC4899,
      artworkSecondaryRgb = 0x6366F1,
      artworkAccentRgb = 0xF472B6,
      isSampleTrack = true,
      sampleBaseFreqHz = 329.63, // E4
    ),
    MusicTrack(
      id = "sample_autumn_coffee",
      title = "Autumn Leaves & Coffee",
      artist = "Velvet Trio",
      album = "Late Night Sessions",
      genre = "Jazz",
      durationMs = 18_000L,
      trackNumber = 1,
      year = 2024,
      artworkPrimaryRgb = 0xD97706,
      artworkSecondaryRgb = 0x78350F,
      artworkAccentRgb = 0xFBBF24,
      isSampleTrack = true,
      sampleBaseFreqHz = 349.23, // F4
    ),
    MusicTrack(
      id = "sample_blue_note_stroll",
      title = "Blue Note Stroll",
      artist = "Velvet Trio",
      album = "Late Night Sessions",
      genre = "Jazz",
      durationMs = 15_000L,
      trackNumber = 2,
      year = 2024,
      artworkPrimaryRgb = 0xD97706,
      artworkSecondaryRgb = 0x78350F,
      artworkAccentRgb = 0xFBBF24,
      isSampleTrack = true,
      sampleBaseFreqHz = 392.00, // G4
    ),
    MusicTrack(
      id = "sample_canyon_breeze",
      title = "Canyon Breeze",
      artist = "Sierra Pines",
      album = "Wildflower Trail",
      genre = "Acoustic",
      durationMs = 14_000L,
      trackNumber = 1,
      year = 2025,
      artworkPrimaryRgb = 0x16A34A,
      artworkSecondaryRgb = 0x065F46,
      artworkAccentRgb = 0x4ADE80,
      isSampleTrack = true,
      sampleBaseFreqHz = 440.00, // A4
    ),
    MusicTrack(
      id = "sample_mountain_morning",
      title = "Mountain Morning",
      artist = "Sierra Pines",
      album = "Wildflower Trail",
      genre = "Acoustic",
      durationMs = 16_000L,
      trackNumber = 2,
      year = 2025,
      artworkPrimaryRgb = 0x16A34A,
      artworkSecondaryRgb = 0x065F46,
      artworkAccentRgb = 0x4ADE80,
      isSampleTrack = true,
      sampleBaseFreqHz = 493.88, // B4
    ),
    MusicTrack(
      id = "sample_crimson_velocity",
      title = "Crimson Velocity",
      artist = "The Echoes",
      album = "Static & Sparks",
      genre = "Rock",
      durationMs = 13_000L,
      trackNumber = 1,
      year = 2023,
      artworkPrimaryRgb = 0xDC2626,
      artworkSecondaryRgb = 0x7F1D1D,
      artworkAccentRgb = 0xFB923C,
      isSampleTrack = true,
      sampleBaseFreqHz = 220.00, // A3
    ),
    MusicTrack(
      id = "sample_electric_echo",
      title = "Electric Echo",
      artist = "The Echoes",
      album = "Static & Sparks",
      genre = "Rock",
      durationMs = 15_000L,
      trackNumber = 2,
      year = 2023,
      artworkPrimaryRgb = 0xDC2626,
      artworkSecondaryRgb = 0x7F1D1D,
      artworkAccentRgb = 0xFB923C,
      isSampleTrack = true,
      sampleBaseFreqHz = 246.94, // B3
    ),
    MusicTrack(
      id = "sample_moonlight_prelude",
      title = "Moonlight Prelude",
      artist = "Clara Vance",
      album = "Nocturnes in Glass",
      genre = "Classical",
      durationMs = 17_000L,
      trackNumber = 1,
      year = 2026,
      artworkPrimaryRgb = 0x4F46E5,
      artworkSecondaryRgb = 0x312E81,
      artworkAccentRgb = 0xA78BFA,
      isSampleTrack = true,
      sampleBaseFreqHz = 523.25, // C5
    ),
  )

  /**
   * Checks whether [file] is a supported audio music file.
   */
  fun isMusicFile(file: File): Boolean {
    if (!file.exists() || !file.isFile) return false
    if (file.name.startsWith(".")) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in SUPPORTED_MUSIC_EXTENSIONS
  }

  /**
   * Deterministically generates a vivid 3-color artwork seed triplet `(primaryRgb, secondaryRgb, accentRgb)`
   * from an album and artist name when no embedded picture is present.
   */
  fun computeDeterministicArtworkColors(
    album: String,
    artist: String,
    genre: String = "",
  ): Triple<Int, Int, Int> {
    val palettePresets = listOf(
      Triple(0x00B4D8, 0x7209B7, 0x4CC9F0), // Cyan / Violet
      Triple(0xEC4899, 0x6366F1, 0xF472B6), // Pink / Indigo
      Triple(0xD97706, 0x78350F, 0xFBBF24), // Amber / Espresso
      Triple(0x16A34A, 0x065F46, 0x4ADE80), // Emerald / Forest
      Triple(0xDC2626, 0x7F1D1D, 0xFB923C), // Crimson / Orange
      Triple(0x4F46E5, 0x312E81, 0xA78BFA), // Indigo / Lavender
      Triple(0x0D9488, 0x134E4A, 0x2DD4BF), // Teal / Mint
      Triple(0x9333EA, 0x4C1D95, 0xC084FC), // Purple / Orchid
    )
    val key = "${album.trim().lowercase(Locale.ROOT)}|${artist.trim().lowercase(Locale.ROOT)}|${genre.trim().lowercase(Locale.ROOT)}"
    var hash = 0
    for (ch in key) {
      hash = (hash * 31 + ch.code) and 0x7FFFFFFF
    }
    return palettePresets[hash % palettePresets.size]
  }

  /**
   * Formats an RGB integer (`0xRRGGBB`) into a `#RRGGBB` hex string.
   */
  fun formatRgbHex(rgb: Int): String {
    return String.format(Locale.US, "#%06X", rgb and 0xFFFFFF)
  }

  /**
   * Converts a Compose [Color] into a `#RRGGBB` hex string.
   */
  fun colorToHex(color: Color): String {
    val r = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val g = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val b = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    return String.format(Locale.US, "#%02X%02X%02X", r, g, b)
  }

  private const val MAX_PALETTE_SAMPLE_DIM = 48
  private const val MAX_PALETTE_CLUSTERS = 12
  private const val PALETTE_KMEANS_ITERATIONS = 6
  private const val PALETTE_MERGE_OKLAB_DISTANCE = 0.055f
  private const val MIN_ACCENT_LUMINANCE = 0.11f
  private const val TARGET_LIFTED_ACCENT_OKLAB_L = 0.56f
  private const val NEUTRAL_EMPTY_DOMINANT_RGB = 0x2A2A2E
  private const val NEUTRAL_EMPTY_SECONDARY_RGB = 0x1A1A1E
  private const val NEUTRAL_EMPTY_ACCENT_RGB = 0x9E9EA8

  private data class OkLab(
    val l: Float,
    val a: Float,
    val b: Float,
  ) {
    val chroma: Float
      get() = Math.sqrt((a * a + b * b).toDouble()).toFloat()
  }

  private class HistogramBin(
    val key: Int,
    var sumR: Long = 0L,
    var sumG: Long = 0L,
    var sumB: Long = 0L,
    var count: Int = 0,
  ) {
    var r: Int = 0
    var g: Int = 0
    var b: Int = 0
    var okL: Float = 0f
    var okA: Float = 0f
    var okB: Float = 0f

    fun finalizeColor() {
      val safeCount = count.coerceAtLeast(1)
      r = ((sumR + safeCount / 2L) / safeCount).toInt().coerceIn(0, 255)
      g = ((sumG + safeCount / 2L) / safeCount).toInt().coerceIn(0, 255)
      b = ((sumB + safeCount / 2L) / safeCount).toInt().coerceIn(0, 255)
      val lab = rgbToOkLab(r, g, b)
      okL = lab.l
      okA = lab.a
      okB = lab.b
    }
  }

  private data class ColorCluster(
    val rgb: Int,
    val r: Int,
    val g: Int,
    val b: Int,
    val count: Int,
    val population: Float,
    val okL: Float,
    val okA: Float,
    val okB: Float,
  ) {
    val okC: Float = Math.sqrt((okA * okA + okB * okB).toDouble()).toFloat()
  }

  private fun srgbChannelToLinear(channel: Int): Double {
    val c = channel.coerceIn(0, 255) / 255.0
    return if (c <= 0.04045) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
  }

  private fun linearToSrgbChannel(linear: Double): Int {
    val clamped = linear.coerceIn(0.0, 1.0)
    val srgb = if (clamped <= 0.0031308) {
      12.92 * clamped
    } else {
      1.055 * Math.pow(clamped, 1.0 / 2.4) - 0.055
    }
    return (srgb * 255.0).roundToInt().coerceIn(0, 255)
  }

  private fun rgbToOkLab(r: Int, g: Int, b: Int): OkLab {
    val lr = srgbChannelToLinear(r)
    val lg = srgbChannelToLinear(g)
    val lb = srgbChannelToLinear(b)
    val l = 0.4122214708 * lr + 0.5363325363 * lg + 0.0514459929 * lb
    val m = 0.2119034982 * lr + 0.6806995451 * lg + 0.1073969566 * lb
    val s = 0.0883024619 * lr + 0.2817188376 * lg + 0.6299787005 * lb
    val lRoot = Math.cbrt(l.coerceAtLeast(0.0))
    val mRoot = Math.cbrt(m.coerceAtLeast(0.0))
    val sRoot = Math.cbrt(s.coerceAtLeast(0.0))
    val okL = (0.2104542553 * lRoot + 0.7936177850 * mRoot - 0.0040720468 * sRoot).toFloat()
    val okA = (1.9779984951 * lRoot - 2.4285922050 * mRoot + 0.4505937099 * sRoot).toFloat()
    val okB = (0.0259040371 * lRoot + 0.7827717662 * mRoot - 0.8086757660 * sRoot).toFloat()
    return OkLab(okL, okA, okB)
  }

  /**
   * Converts an OKLab coordinate back to sRGB `0xRRGGBB`, bisection-scaling chroma into the sRGB
   * gamut if needed so the hue angle is strictly preserved with zero channel-clipping hue shift.
   */
  private fun okLabToGamutSafeRgb(okL: Float, okA: Float, okB: Float): Int {
    val clampedL = okL.coerceIn(0f, 1f).toDouble()
    val baseA = okA.toDouble()
    val baseB = okB.toDouble()

    fun linearRgbForScale(scale: Double): Triple<Double, Double, Double> {
      val a = baseA * scale
      val b = baseB * scale
      val lRoot = clampedL + 0.3963377774 * a + 0.2158037573 * b
      val mRoot = clampedL - 0.1055613458 * a - 0.0638541728 * b
      val sRoot = clampedL - 0.0894841775 * a - 1.2914855480 * b
      val l = lRoot * lRoot * lRoot
      val m = mRoot * mRoot * mRoot
      val s = sRoot * sRoot * sRoot
      val lr = +4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s
      val lg = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s
      val lb = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
      return Triple(lr, lg, lb)
    }

    fun inGamut(rgb: Triple<Double, Double, Double>): Boolean {
      return rgb.first in -1e-4..1.0001 &&
        rgb.second in -1e-4..1.0001 &&
        rgb.third in -1e-4..1.0001
    }

    val full = linearRgbForScale(1.0)
    val chosen = if (inGamut(full)) {
      full
    } else {
      var lo = 0.0
      var hi = 1.0
      repeat(14) {
        val mid = (lo + hi) * 0.5
        if (inGamut(linearRgbForScale(mid))) {
          lo = mid
        } else {
          hi = mid
        }
      }
      linearRgbForScale(lo)
    }
    val r = linearToSrgbChannel(chosen.first)
    val g = linearToSrgbChannel(chosen.second)
    val b = linearToSrgbChannel(chosen.third)
    return (r shl 16) or (g shl 8) or b
  }

  private fun okLabDistance(l1: Float, a1: Float, b1: Float, l2: Float, a2: Float, b2: Float): Float {
    val dl = l1 - l2
    val da = a1 - a2
    val db = b1 - b2
    return Math.sqrt((dl * dl + da * da + db * db).toDouble()).toFloat()
  }

  private fun okLabDistance(c1: ColorCluster, c2: ColorCluster): Float {
    return okLabDistance(c1.okL, c1.okA, c1.okB, c2.okL, c2.okA, c2.okB)
  }

  /**
   * Derives a tonal shade of [rgb] in OKLab while strictly preserving its hue angle (used only
   * when an album cover consists of a single uniform color cluster).
   */
  private fun deriveTonalShadeRgb(rgb: Int): Int {
    val r = (rgb shr 16) and 0xFF
    val g = (rgb shr 8) and 0xFF
    val b = rgb and 0xFF
    val lab = rgbToOkLab(r, g, b)
    val targetL = if (lab.l > 0.36f) {
      (lab.l - 0.16f).coerceAtLeast(0.14f)
    } else {
      (lab.l + 0.12f).coerceAtMost(0.45f)
    }
    return okLabToGamutSafeRgb(targetL, lab.a, lab.b)
  }

  /**
   * Lifts an excessively dark accent color (`luminance < 0.11`) in OKLab lightness while
   * preserving its exact hue angle (and keeping neutral grays strictly neutral).
   * Idempotent: colors already meeting the contrast floor are returned unmodified.
   */
  private fun liftDarkAccentRgbPreservingHue(rgb: Int): Int {
    val r = (rgb shr 16) and 0xFF
    val g = (rgb shr 8) and 0xFF
    val b = rgb and 0xFF
    val lab = rgbToOkLab(r, g, b)
    val targetL = max(lab.l, TARGET_LIFTED_ACCENT_OKLAB_L)
    val chromaScale = if (lab.chroma > 0.02f && lab.l > 0.05f) {
      (targetL / lab.l).coerceIn(1.0f, 1.85f)
    } else {
      1.0f
    }
    return okLabToGamutSafeRgb(targetL, lab.a * chromaScale, lab.b * chromaScale)
  }

  /**
   * Builds an [AlbumArtPalette] from primary, secondary, and accent RGB integers.
   *
   * Preserves [primaryRgb] and [secondaryRgb] verbatim and preserves [accentRgb] verbatim whenever
   * its relative luminance is at least [MIN_ACCENT_LUMINANCE] (so vivid reds, blues, greens, and
   * yellows are never washed out into pastels). Very dark accent colors are lifted in OKLab
   * lightness while strictly preserving their hue angle, making repeated round-trips idempotent.
   */
  fun buildPaletteFromRgb(
    primaryRgb: Int,
    secondaryRgb: Int,
    accentRgb: Int,
  ): AlbumArtPalette {
    val domColor = Color(0xFF000000 or (primaryRgb.toLong() and 0xFFFFFFL))
    val secColor = Color(0xFF000000 or (secondaryRgb.toLong() and 0xFFFFFFL))
    val rawAccent = Color(0xFF000000 or (accentRgb.toLong() and 0xFFFFFFL))

    // Preserve vivid artwork hues verbatim; only lift genuinely very dark accents in OKLab
    // lightness (preserving exact hue angle) so controls remain legible on dark player surfaces.
    val effectiveAccent = if (rawAccent.luminance() < MIN_ACCENT_LUMINANCE) {
      val liftedRgb = liftDarkAccentRgbPreservingHue(accentRgb and 0xFFFFFF)
      Color(0xFF000000 or (liftedRgb.toLong() and 0xFFFFFFL))
    } else {
      rawAccent
    }

    val darkSurface = lerp(domColor, Color(0xFF101012), 0.52f)
    val deepBg = lerp(secColor, Color(0xFF08080A), 0.72f)

    return AlbumArtPalette(
      dominantColor = domColor,
      secondaryColor = secColor,
      accentColor = effectiveAccent,
      darkSurfaceColor = darkSurface,
      deepBackgroundColor = deepBg,
      onBackgroundColor = Color(0xFFF8F9FA),
      onSecondaryBackgroundColor = Color(0xFFDDE1E8),
      dominantHex = colorToHex(domColor),
      secondaryHex = colorToHex(secColor),
      accentHex = colorToHex(effectiveAccent),
    )
  }

  private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
    return try {
      val isHardware = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        bitmap.config == Bitmap.Config.HARDWARE
      if (isHardware) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
      } else {
        bitmap
      }
    } catch (_: Throwable) {
      bitmap
    }
  }

  /**
   * Computes a power-of-two `BitmapFactory.Options.inSampleSize` that bounds both width and height
   * (including wide or tall rectangular covers) safely near [targetSize] so decoded bitmaps never
   * blow past Android's 1 MB Binder IPC transaction limit.
   */
  internal fun computeBoundedInSampleSize(width: Int, height: Int, targetSize: Int): Int {
    if (width <= 0 || height <= 0 || targetSize <= 0) return 1
    val safeTarget = targetSize.coerceAtLeast(64)
    var inSampleSize = 1
    if (height > safeTarget || width > safeTarget) {
      val halfHeight = height / 2
      val halfWidth = width / 2
      while ((halfHeight / inSampleSize) >= safeTarget && (halfWidth / inSampleSize) >= safeTarget) {
        inSampleSize *= 2
      }
      val maxAllowedDecodedEdge = safeTarget * 2
      while ((width / inSampleSize) > maxAllowedDecodedEdge || (height / inSampleSize) > maxAllowedDecodedEdge) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }

  /**
   * Converts [bitmap] to a software `ARGB_8888` [Bitmap] (if backed by `Bitmap.Config.HARDWARE`)
   * and scales it down proportionally if its longest edge exceeds [maxDimPx].
   */
  internal fun scaleAndNormalizeSoftwareBitmap(bitmap: Bitmap?, maxDimPx: Int = 360): Bitmap? {
    if (bitmap == null) return null
    return try {
      if (bitmap.isRecycled) return null
      val sw = ensureSoftwareBitmap(bitmap)
      val w = sw.width
      val h = sw.height
      if (w <= 0 || h <= 0) return sw
      val safeMax = maxDimPx.coerceAtLeast(64)
      val longest = max(w, h)
      if (longest <= safeMax) {
        sw
      } else {
        val scale = safeMax.toFloat() / longest.toFloat()
        val targetW = (w * scale).roundToInt().coerceAtLeast(1)
        val targetH = (h * scale).roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(sw, targetW, targetH, true)
        if (sw !== bitmap && scaled !== sw) {
          try {
            sw.recycle()
          } catch (_: Throwable) {}
        }
        scaled
      }
    } catch (_: Throwable) {
      bitmap
    }
  }

  /**
   * Prepares an artwork [Bitmap] that is safe for Android `MediaMetadata` and `Notification`
   * Binder IPC parcels (software-backed and bounded to at most [maxDimPx] on its longest edge,
   * keeping parcel payload well below the 1 MB Binder limit).
   */
  internal fun prepareSystemMediaArtworkBitmap(bitmap: Bitmap?, maxDimPx: Int = 256): Bitmap? {
    return scaleAndNormalizeSoftwareBitmap(bitmap, maxDimPx = maxDimPx.coerceIn(96, 320))
  }

  /**
   * Extracts an [AlbumArtPalette] from a raw ARGB_8888 [pixels] grid (`width * height`) using
   * perceptual OKLab color quantization (5-bit RGB histogram -> deterministic Median-Cut ->
   * weighted OKLab K-Means -> close-cluster merge -> population-guarded role selection).
   *
   * Guarantees:
   * - Never linearly averages opposing colors into non-existent muddy/inverted hues.
   * - Never injects a hardcoded purple fallback on dark, light, or monochrome covers.
   * - Filters transparent pixels (`alpha < 128`) and ignores tiny single-pixel speckles/artifacts.
   * - Pure-Kotlin & deterministic so it runs identically on Android runtime and JVM unit tests.
   */
  fun extractPaletteFromPixels(
    pixels: IntArray,
    width: Int,
    height: Int,
  ): AlbumArtPalette {
    if (width <= 0 || height <= 0 || pixels.size < width * height) {
      return buildPaletteFromRgb(
        primaryRgb = NEUTRAL_EMPTY_DOMINANT_RGB,
        secondaryRgb = NEUTRAL_EMPTY_SECONDARY_RGB,
        accentRgb = NEUTRAL_EMPTY_ACCENT_RGB,
      )
    }

    val sampleCols = min(width, MAX_PALETTE_SAMPLE_DIM)
    val sampleRows = min(height, MAX_PALETTE_SAMPLE_DIM)

    fun buildHistogram(minAlpha: Int): Pair<List<HistogramBin>, Int> {
      val binsByKey = HashMap<Int, HistogramBin>(256)
      var validCount = 0
      for (sy in 0 until sampleRows) {
        val y = if (sampleRows == height) {
          sy
        } else {
          (((sy + 0.5f) * height) / sampleRows).toInt().coerceIn(0, height - 1)
        }
        val rowOffset = y * width
        for (sx in 0 until sampleCols) {
          val x = if (sampleCols == width) {
            sx
          } else {
            (((sx + 0.5f) * width) / sampleCols).toInt().coerceIn(0, width - 1)
          }
          val pixel = pixels[rowOffset + x]
          val alpha = (pixel ushr 24) and 0xFF
          if (alpha < minAlpha) continue
          val r = (pixel shr 16) and 0xFF
          val g = (pixel shr 8) and 0xFF
          val b = pixel and 0xFF
          val key = ((r shr 3) shl 10) or ((g shr 3) shl 5) or (b shr 3)
          val bin = binsByKey.getOrPut(key) { HistogramBin(key) }
          bin.sumR += r
          bin.sumG += g
          bin.sumB += b
          bin.count += 1
          validCount += 1
        }
      }
      val list = ArrayList<HistogramBin>(binsByKey.size)
      for (bin in binsByKey.values) {
        bin.finalizeColor()
        list.add(bin)
      }
      // Deterministic ordering by quantized key
      list.sortBy { it.key }
      return list to validCount
    }

    var (bins, totalValidPixels) = buildHistogram(minAlpha = 128)
    if (totalValidPixels == 0) {
      val fallback = buildHistogram(minAlpha = 1)
      bins = fallback.first
      totalValidPixels = fallback.second
    }
    if (totalValidPixels == 0 || bins.isEmpty()) {
      return buildPaletteFromRgb(
        primaryRgb = NEUTRAL_EMPTY_DOMINANT_RGB,
        secondaryRgb = NEUTRAL_EMPTY_SECONDARY_RGB,
        accentRgb = NEUTRAL_EMPTY_ACCENT_RGB,
      )
    }

    val clusters = quantizeBinsToClusters(bins, totalValidPixels)
    if (clusters.isEmpty()) {
      val first = bins.first()
      val rgb = (first.r shl 16) or (first.g shl 8) or first.b
      return buildPaletteFromRgb(rgb, deriveTonalShadeRgb(rgb), rgb)
    }

    fun dominantWeightFactor(c: ColorCluster): Float {
      val isNearBlackOrWhite = c.okC < 0.030f && (c.okL < 0.20f || c.okL > 0.92f)
      val isFlatGray = c.okC < 0.025f
      if (c.population < 0.05f) {
        // Guard against tiny speckles stealing dominant from a solid background
        return if (isNearBlackOrWhite) 0.30f else if (isFlatGray) 0.50f else 1.0f
      }
      return when {
        c.okC >= 0.045f -> 1.0f + min(0.45f, c.okC * 2.0f)
        c.okC >= 0.025f -> 0.85f
        isNearBlackOrWhite -> 0.30f
        else -> 0.50f
      }
    }

    // 1. Dominant color: largest perceptually prominent color region
    val dominant = clusters.maxWith(
      compareBy<ColorCluster> { it.population * dominantWeightFactor(it) }
        .thenBy { it.count }
        .thenBy { it.rgb },
    )

    // 2. Secondary color: distinct major supporting color region (>= 3% population, deltaE >= 0.10)
    val secondaryCandidates = clusters.filter { candidate ->
      candidate !== dominant &&
        candidate.population >= 0.03f &&
        okLabDistance(candidate, dominant) >= 0.10f
    }
    val secondary = secondaryCandidates.maxWithOrNull(
      compareBy<ColorCluster> { c ->
        val dist = okLabDistance(c, dominant)
        val distinctBonus = min(1f, dist / 0.25f)
        c.population * dominantWeightFactor(c) * (0.40f + 0.60f * distinctBonus)
      }.thenBy { it.count }.thenBy { it.rgb },
    )
    val secondaryRgb = secondary?.rgb ?: deriveTonalShadeRgb(dominant.rgb)

    // 3. Accent color: most vibrant highlight actually present in a real region (>= 1.5% population)
    val populationGuarded = clusters.filter { it.population >= 0.015f }.ifEmpty { clusters }
    val chromaticAccents = populationGuarded.filter { it.okC >= 0.035f }
    val accent = if (chromaticAccents.isNotEmpty()) {
      chromaticAccents.maxWith(
        compareBy<ColorCluster> { c ->
          val chromaScore = (c.okC / 0.22f).coerceIn(0f, 1.2f)
          val lightnessFitness = (1f - abs(c.okL - 0.65f) / 0.65f).coerceIn(0.15f, 1f)
          val popSupport = (Math.sqrt(c.population.toDouble()).toFloat() * 1.5f).coerceIn(0.30f, 1.0f)
          val distinctBonus = if (okLabDistance(c, dominant) >= 0.12f) 1.15f else 1.0f
          (chromaScore * 0.65f + lightnessFitness * 0.35f) * popSupport * distinctBonus
        }.thenBy { it.count }.thenBy { it.rgb },
      )
    } else {
      // Strictly monochrome / grayscale artwork: choose the most legible neutral tone in the image
      populationGuarded.maxWith(
        compareBy<ColorCluster> { c ->
          val lightnessFitness = (1f - abs(c.okL - 0.68f) / 0.68f).coerceIn(0.10f, 1f)
          val popSupport = (Math.sqrt(c.population.toDouble()).toFloat() * 1.5f).coerceIn(0.35f, 1.0f)
          lightnessFitness * (0.40f + 0.60f * popSupport)
        }.thenBy { it.count }.thenBy { it.rgb },
      )
    }

    return buildPaletteFromRgb(
      primaryRgb = dominant.rgb,
      secondaryRgb = secondaryRgb,
      accentRgb = accent.rgb,
    )
  }

  private fun quantizeBinsToClusters(
    bins: List<HistogramBin>,
    totalValidPixels: Int,
  ): List<ColorCluster> {
    if (bins.size == 1) {
      val b = bins.first()
      val rgb = (b.r shl 16) or (b.g shl 8) or b.b
      return listOf(
        ColorCluster(
          rgb = rgb,
          r = b.r,
          g = b.g,
          b = b.b,
          count = b.count,
          population = b.count.toFloat() / totalValidPixels.toFloat(),
          okL = b.okL,
          okA = b.okA,
          okB = b.okB,
        ),
      )
    }

    // Stage 1: Deterministic Median-Cut in OKLab space to seed up to MAX_PALETTE_CLUSTERS boxes
    class OkLabBox(val items: MutableList<HistogramBin>) {
      var totalCount: Int = 0
      var minL = Float.POSITIVE_INFINITY
      var maxL = Float.NEGATIVE_INFINITY
      var minA = Float.POSITIVE_INFINITY
      var maxA = Float.NEGATIVE_INFINITY
      var minB = Float.POSITIVE_INFINITY
      var maxB = Float.NEGATIVE_INFINITY
      var maxSpan: Float = 0f
      var longestAxis: Int = 0 // 0=L, 1=a, 2=b
      var splitPriority: Double = 0.0

      init {
        recomputeBounds()
      }

      fun recomputeBounds() {
        totalCount = 0
        minL = Float.POSITIVE_INFINITY; maxL = Float.NEGATIVE_INFINITY
        minA = Float.POSITIVE_INFINITY; maxA = Float.NEGATIVE_INFINITY
        minB = Float.POSITIVE_INFINITY; maxB = Float.NEGATIVE_INFINITY
        for (bin in items) {
          totalCount += bin.count
          if (bin.okL < minL) minL = bin.okL
          if (bin.okL > maxL) maxL = bin.okL
          if (bin.okA < minA) minA = bin.okA
          if (bin.okA > maxA) maxA = bin.okA
          if (bin.okB < minB) minB = bin.okB
          if (bin.okB > maxB) maxB = bin.okB
        }
        val spanL = maxL - minL
        val spanA = maxA - minA
        val spanB = maxB - minB
        if (spanL >= spanA && spanL >= spanB) {
          maxSpan = spanL; longestAxis = 0
        } else if (spanA >= spanB) {
          maxSpan = spanA; longestAxis = 1
        } else {
          maxSpan = spanB; longestAxis = 2
        }
        splitPriority = if (items.size > 1 && maxSpan > 0.03f) {
          maxSpan.toDouble() * Math.sqrt(totalCount.toDouble())
        } else {
          0.0
        }
      }
    }

    val boxes = mutableListOf(OkLabBox(bins.toMutableList()))
    while (boxes.size < MAX_PALETTE_CLUSTERS) {
      val bestBox = boxes.maxByOrNull { it.splitPriority } ?: break
      if (bestBox.splitPriority <= 0.0) break
      val sorted = when (bestBox.longestAxis) {
        0 -> bestBox.items.sortedWith(compareBy<HistogramBin> { it.okL }.thenBy { it.key })
        1 -> bestBox.items.sortedWith(compareBy<HistogramBin> { it.okA }.thenBy { it.key })
        else -> bestBox.items.sortedWith(compareBy<HistogramBin> { it.okB }.thenBy { it.key })
      }
      val halfWeight = bestBox.totalCount / 2
      var accum = 0
      var splitIdx = 1
      for (i in 0 until sorted.lastIndex) {
        accum += sorted[i].count
        splitIdx = i + 1
        if (accum >= halfWeight) break
      }
      val left = OkLabBox(sorted.subList(0, splitIdx).toMutableList())
      val right = OkLabBox(sorted.subList(splitIdx, sorted.size).toMutableList())
      boxes.remove(bestBox)
      boxes.add(left)
      boxes.add(right)
    }

    // Stage 2: Weighted Lloyd K-Means refinement in OKLab space
    val k = boxes.size
    val centL = FloatArray(k)
    val centA = FloatArray(k)
    val centB = FloatArray(k)
    for (ci in 0 until k) {
      var wSum = 0.0
      var sL = 0.0; var sA = 0.0; var sB = 0.0
      for (bin in boxes[ci].items) {
        val w = bin.count.toDouble()
        wSum += w
        sL += bin.okL * w
        sA += bin.okA * w
        sB += bin.okB * w
      }
      if (wSum > 0.0) {
        centL[ci] = (sL / wSum).toFloat()
        centA[ci] = (sA / wSum).toFloat()
        centB[ci] = (sB / wSum).toFloat()
      }
    }

    val assignments = IntArray(bins.size) { -1 }
    repeat(PALETTE_KMEANS_ITERATIONS) {
      var changed = false
      for (bi in bins.indices) {
        val bin = bins[bi]
        var bestCi = 0
        var bestDistSq = Float.POSITIVE_INFINITY
        for (ci in 0 until k) {
          val dl = bin.okL - centL[ci]
          val da = bin.okA - centA[ci]
          val db = bin.okB - centB[ci]
          val distSq = dl * dl + da * da + db * db
          if (distSq < bestDistSq) {
            bestDistSq = distSq
            bestCi = ci
          }
        }
        if (assignments[bi] != bestCi) {
          assignments[bi] = bestCi
          changed = true
        }
      }
      if (!changed) return@repeat

      val sumL = DoubleArray(k)
      val sumA = DoubleArray(k)
      val sumB = DoubleArray(k)
      val sumW = DoubleArray(k)
      for (bi in bins.indices) {
        val ci = assignments[bi]
        val bin = bins[bi]
        val w = bin.count.toDouble()
        sumW[ci] += w
        sumL[ci] += bin.okL * w
        sumA[ci] += bin.okA * w
        sumB[ci] += bin.okB * w
      }
      for (ci in 0 until k) {
        if (sumW[ci] > 0.0) {
          centL[ci] = (sumL[ci] / sumW[ci]).toFloat()
          centA[ci] = (sumA[ci] / sumW[ci]).toFloat()
          centB[ci] = (sumB[ci] / sumW[ci]).toFloat()
        }
      }
    }

    // Group member bins into MutableClusters
    class MutableCluster(
      val memberBins: MutableList<HistogramBin> = mutableListOf(),
      var sumR: Long = 0L,
      var sumG: Long = 0L,
      var sumB: Long = 0L,
      var count: Int = 0,
      var okL: Float = 0f,
      var okA: Float = 0f,
      var okB: Float = 0f,
    ) {
      fun recompute() {
        val safe = count.coerceAtLeast(1)
        val r = ((sumR + safe / 2L) / safe).toInt().coerceIn(0, 255)
        val g = ((sumG + safe / 2L) / safe).toInt().coerceIn(0, 255)
        val b = ((sumB + safe / 2L) / safe).toInt().coerceIn(0, 255)
        val lab = rgbToOkLab(r, g, b)
        okL = lab.l
        okA = lab.a
        okB = lab.b
      }

      fun toFinalCluster(totalPixels: Int): ColorCluster {
        val safe = count.coerceAtLeast(1)
        var meanR = ((sumR + safe / 2L) / safe).toInt().coerceIn(0, 255)
        var meanG = ((sumG + safe / 2L) / safe).toInt().coerceIn(0, 255)
        var meanB = ((sumB + safe / 2L) / safe).toInt().coerceIn(0, 255)
        var lab = rgbToOkLab(meanR, meanG, meanB)

        // Grounding guard: ensure the returned cluster color is close to an actual pixel bin in
        // this cluster (never an interpolated color lying in an empty gap between two hues).
        val hasCloseRealBin = memberBins.any { bin ->
          okLabDistance(lab.l, lab.a, lab.b, bin.okL, bin.okA, bin.okB) <= 0.06f
        }
        if (!hasCloseRealBin && memberBins.isNotEmpty()) {
          val anchor = memberBins.maxWith(
            compareBy<HistogramBin> { bin ->
              val dist = okLabDistance(lab.l, lab.a, lab.b, bin.okL, bin.okA, bin.okB)
              bin.count.toFloat() / (0.04f + dist)
            }.thenBy { bin -> bin.count },
          )
          meanR = anchor.r
          meanG = anchor.g
          meanB = anchor.b
          lab = OkLab(anchor.okL, anchor.okA, anchor.okB)
        }
        val rgb = (meanR shl 16) or (meanG shl 8) or meanB
        return ColorCluster(
          rgb = rgb,
          r = meanR,
          g = meanG,
          b = meanB,
          count = count,
          population = count.toFloat() / totalPixels.toFloat(),
          okL = lab.l,
          okA = lab.a,
          okB = lab.b,
        )
      }
    }

    val rawClusters = Array(k) { MutableCluster() }
    for (bi in bins.indices) {
      val ci = assignments[bi].coerceIn(0, k - 1)
      val bin = bins[bi]
      val mc = rawClusters[ci]
      mc.memberBins.add(bin)
      mc.sumR += bin.sumR
      mc.sumG += bin.sumG
      mc.sumB += bin.sumB
      mc.count += bin.count
    }

    val merged = rawClusters.filter { it.count > 0 }.toMutableList()
    merged.forEach { it.recompute() }

    // Stage 3: Merge perceptually near-identical clusters so smooth gradients unify cleanly
    while (merged.size > 1) {
      var bestI = -1
      var bestJ = -1
      var bestDist = PALETTE_MERGE_OKLAB_DISTANCE
      for (i in 0 until merged.size) {
        val ci = merged[i]
        for (j in i + 1 until merged.size) {
          val cj = merged[j]
          val d = okLabDistance(ci.okL, ci.okA, ci.okB, cj.okL, cj.okA, cj.okB)
          if (d < bestDist) {
            bestDist = d
            bestI = i
            bestJ = j
          }
        }
      }
      if (bestI < 0 || bestJ < 0) break
      val target = merged[bestI]
      val donor = merged.removeAt(bestJ)
      target.memberBins.addAll(donor.memberBins)
      target.sumR += donor.sumR
      target.sumG += donor.sumG
      target.sumB += donor.sumB
      target.count += donor.count
      target.recompute()
    }

    return merged.map { it.toFinalCluster(totalValidPixels) }
  }

  /**
   * Extracts an [AlbumArtPalette] from [bitmap] by cell-centered sampling up to a 48×48 grid and
   * running perceptual OKLab color clustering via [extractPaletteFromPixels].
   */
  fun extractPaletteFromBitmap(bitmap: Bitmap): AlbumArtPalette {
    val readableBitmap = ensureSoftwareBitmap(bitmap)
    val shouldRecycleCopy = readableBitmap !== bitmap
    try {
      if (readableBitmap.isRecycled) {
        return buildPaletteFromRgb(
          primaryRgb = NEUTRAL_EMPTY_DOMINANT_RGB,
          secondaryRgb = NEUTRAL_EMPTY_SECONDARY_RGB,
          accentRgb = NEUTRAL_EMPTY_ACCENT_RGB,
        )
      }
      val width = readableBitmap.width
      val height = readableBitmap.height
      if (width <= 0 || height <= 0) {
        return buildPaletteFromRgb(
          primaryRgb = NEUTRAL_EMPTY_DOMINANT_RGB,
          secondaryRgb = NEUTRAL_EMPTY_SECONDARY_RGB,
          accentRgb = NEUTRAL_EMPTY_ACCENT_RGB,
        )
      }

      val sampleCols = min(width, MAX_PALETTE_SAMPLE_DIM)
      val sampleRows = min(height, MAX_PALETTE_SAMPLE_DIM)
      val sampledPixels = IntArray(sampleCols * sampleRows)
      val rowBuffer = IntArray(width)
      var outIdx = 0

      for (sy in 0 until sampleRows) {
        val y = if (sampleRows == height) {
          sy
        } else {
          (((sy + 0.5f) * height) / sampleRows).toInt().coerceIn(0, height - 1)
        }
        val rowReadOk = try {
          readableBitmap.getPixels(rowBuffer, 0, width, 0, y, width, 1)
          true
        } catch (_: Throwable) {
          false
        }
        for (sx in 0 until sampleCols) {
          val x = if (sampleCols == width) {
            sx
          } else {
            (((sx + 0.5f) * width) / sampleCols).toInt().coerceIn(0, width - 1)
          }
          sampledPixels[outIdx++] = if (rowReadOk) {
            rowBuffer[x]
          } else {
            try {
              readableBitmap.getPixel(x, y)
            } catch (_: Throwable) {
              0
            }
          }
        }
      }

      return extractPaletteFromPixels(sampledPixels, sampleCols, sampleRows)
    } finally {
      if (shouldRecycleCopy) {
        try {
          readableBitmap.recycle()
        } catch (_: Throwable) {}
      }
    }
  }

  /**
   * Returns the canonical single-album name (`"Audio"` or `"Recordings"`) when [file] resides
   * inside the `Audio` or `Recordings` directory (including any subdirectories), or `null` otherwise.
   */
  fun resolveSingleFolderAlbumName(file: File?): String? {
    if (file == null) return null
    var current = file.parentFile
    var matchedTopFolder: String? = null
    while (current != null) {
      val name = current.name.trim()
      when {
        name.equals("Audio", ignoreCase = true) -> matchedTopFolder = "Audio"
        name.equals("Recordings", ignoreCase = true) -> matchedTopFolder = "Recordings"
        name.equals("Music", ignoreCase = true) -> matchedTopFolder = "Music"
        name.equals("Downloads", ignoreCase = true) || name.equals("Download", ignoreCase = true) ->
          matchedTopFolder = "Download"
      }
      current = current.parentFile
    }
    return if (matchedTopFolder == "Audio" || matchedTopFolder == "Recordings") {
      matchedTopFolder
    } else {
      null
    }
  }

  /**
   * Returns `true` if [albumName] corresponds to one of the folders treated as a single album
   * (`"Audio"` or `"Recordings"`, case-insensitive).
   */
  fun isSingleFolderAlbumName(albumName: String?): Boolean {
    val trimmed = albumName?.trim() ?: return false
    return SINGLE_ALBUM_FOLDER_NAMES.any { it.equals(trimmed, ignoreCase = true) }
  }

  /**
   * Resolves the concrete [File] directory on disk for a [MusicFolder] pill.
   */
  fun resolveDirectoryForFolder(
    folder: MusicFolder,
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
  ): File {
    if (!folder.customAbsolutePath.isNullOrBlank()) {
      return File(folder.customAbsolutePath)
    }
    if (folder.isDefaultMusicFolder) {
      return File(rootDirectory, "Music")
    }
    val subPath = folder.displayPath.trim().trimStart('/')
    return if (subPath.isEmpty()) rootDirectory else File(rootDirectory, subPath)
  }

  fun parseGrantedFolderPaths(rawString: String?): Set<String> {
    if (rawString.isNullOrBlank()) return emptySet()
    return rawString
      .split(",", "|", "\n")
      .mapNotNull { token ->
        val trimmed = token.trim()
        if (trimmed.isEmpty()) {
          null
        } else if (trimmed.equals("Music", ignoreCase = true) || trimmed.equals("/Music", ignoreCase = true)) {
          "/Music"
        } else {
          "/" + trimmed.trim('/')
        }
      }
      .toSet()
  }

  fun serializeGrantedFolderPaths(paths: Set<String>): String {
    return paths
      .mapNotNull { path ->
        val trimmed = path.trim()
        if (trimmed.isEmpty()) {
          null
        } else if (trimmed.equals("Music", ignoreCase = true) || trimmed.equals("/Music", ignoreCase = true)) {
          "/Music"
        } else {
          "/" + trimmed.trim('/')
        }
      }
      .distinct()
      .joinToString("|")
  }

  fun getGrantedFolderPaths(context: Context): Set<String> {
    val prefs = context.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
    return parseGrantedFolderPaths(prefs.getString(PREF_KEY_MUSIC_GRANTED_FOLDER_PATHS, null))
  }

  fun setGrantedFolderPaths(context: Context, paths: Set<String>) {
    val prefs = context.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putString(PREF_KEY_MUSIC_GRANTED_FOLDER_PATHS, serializeGrantedFolderPaths(paths))
      .apply()
  }

  fun setFolderPathAccessGranted(
    context: Context,
    displayPath: String,
    granted: Boolean = true,
  ): Set<String> {
    val normalized = if (displayPath.trim().trim('/').equals("Music", ignoreCase = true)) {
      "/Music"
    } else {
      "/" + displayPath.trim().trim('/')
    }
    val current = getGrantedFolderPaths(context).toMutableSet()
    if (granted) {
      current.add(normalized)
    } else {
      current.removeAll { it.equals(normalized, ignoreCase = true) }
    }
    setGrantedFolderPaths(context, current)
    return current.toSet()
  }

  fun isMusicFolderAccessGranted(grantedPaths: Set<String>): Boolean {
    return grantedPaths.any {
      it.equals("/Music", ignoreCase = true) || it.equals("Music", ignoreCase = true)
    }
  }

  fun serializeCustomFolders(folders: List<MusicFolder>): String {
    val seenPaths = mutableSetOf<String>()
    val deduplicated = mutableListOf<MusicFolder>()
    for (folder in folders) {
      if (folder.isDefaultMusicFolder) continue
      val normKey = folder.displayPath.trim().lowercase(Locale.ROOT)
      if (normKey.isNotEmpty() && seenPaths.add(normKey)) {
        deduplicated.add(folder)
      }
    }
    return deduplicated.joinToString("\n") { folder ->
      listOf(
        folder.folderName.replace("\t", " ").replace("\n", " "),
        folder.displayPath.replace("\t", " ").replace("\n", " "),
        (folder.treeUri ?: "").replace("\t", " ").replace("\n", " "),
        (folder.customAbsolutePath ?: "").replace("\t", " ").replace("\n", " "),
      ).joinToString("\t")
    }
  }

  fun parseCustomFolders(rawString: String?): List<MusicFolder> {
    if (rawString.isNullOrBlank()) return emptyList()
    val seenPaths = mutableSetOf<String>()
    val parsed = mutableListOf<MusicFolder>()
    for (line in rawString.lines()) {
      val trimmed = line.trim()
      if (trimmed.isEmpty()) continue
      val folder = if (trimmed.contains("\t")) {
        val parts = trimmed.split("\t")
        val name = parts.getOrNull(0)?.trim().orEmpty()
        val display = parts.getOrNull(1)?.trim().orEmpty()
        val uri = parts.getOrNull(2)?.trim()?.takeIf { it.isNotEmpty() }
        val absPath = parts.getOrNull(3)?.trim()?.takeIf { it.isNotEmpty() }
        if (name.isNotEmpty() && display.isNotEmpty()) {
          MusicFolder(
            folderName = name,
            displayPath = if (display.startsWith("/")) display else "/$display",
            treeUri = uri,
            customAbsolutePath = absPath,
          )
        } else {
          null
        }
      } else {
        val cleanPath = "/" + trimmed.trim('/')
        val name = cleanPath.substringAfterLast('/')
        if (name.isNotEmpty()) MusicFolder(folderName = name, displayPath = cleanPath) else null
      }
      if (folder != null && !folder.isDefaultMusicFolder) {
        val normKey = folder.displayPath.lowercase(Locale.ROOT)
        if (seenPaths.add(normKey)) {
          parsed.add(folder)
        }
      }
    }
    return parsed
  }

  fun getCustomFolders(context: Context): List<MusicFolder> {
    val prefs = context.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
    return parseCustomFolders(prefs.getString(PREF_KEY_MUSIC_CUSTOM_FOLDERS, null))
  }

  fun setCustomFolders(context: Context, folders: List<MusicFolder>) {
    val prefs = context.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(PREF_KEY_MUSIC_CUSTOM_FOLDERS, serializeCustomFolders(folders)).apply()
  }

  fun removeCustomFolder(
    folders: List<MusicFolder>,
    folderToRemove: MusicFolder,
  ): List<MusicFolder> {
    return folders.filterNot { folder ->
      folder.displayPath.equals(folderToRemove.displayPath, ignoreCase = true) ||
        folder.folderName.equals(folderToRemove.folderName, ignoreCase = true) ||
        (!folderToRemove.customAbsolutePath.isNullOrBlank() &&
          folder.customAbsolutePath.equals(folderToRemove.customAbsolutePath, ignoreCase = true))
    }
  }

  fun removeCustomFolder(
    context: Context,
    folderToRemove: MusicFolder,
  ): List<MusicFolder> {
    val updated = removeCustomFolder(getCustomFolders(context), folderToRemove)
    setCustomFolders(context, updated)
    setFolderPathAccessGranted(context, folderToRemove.displayPath, false)
    return updated
  }

  fun createFolderAccessIntent(
    folder: MusicFolder,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): Intent = createFolderAccessIntent(context = null, folder = folder, userRoot = userRoot)

  fun createFolderAccessIntent(
    context: Context? = null,
    folder: MusicFolder = DEFAULT_MUSIC_SCOPED_FOLDER,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): Intent {
    val relativePath = folder.displayPath.trim().trim('/').ifEmpty { "Music" }
    try {
      val targetDir = if (!folder.customAbsolutePath.isNullOrBlank()) {
        File(folder.customAbsolutePath)
      } else {
        File(userRoot, relativePath)
      }
      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }
    } catch (_: Throwable) {}

    val docId = "primary:$relativePath"
    val initialUri = try {
      android.provider.DocumentsContract.buildDocumentUri(
        "com.android.externalstorage.documents",
        docId,
      )
    } catch (_: Throwable) {
      Uri.parse("content://com.android.externalstorage.documents/document/primary%3A$relativePath")
    }
    val baseIntent = if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? android.os.storage.StorageManager
        storageManager?.primaryStorageVolume?.createOpenDocumentTreeIntent()
      } catch (_: Throwable) {
        null
      }
    } else {
      null
    } ?: Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

    return baseIntent.apply {
      action = Intent.ACTION_OPEN_DOCUMENT_TREE
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, initialUri)
      }
      addFlags(
        Intent.FLAG_GRANT_READ_URI_PERMISSION or
          Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
          Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
          Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
      )
    }
  }

  fun createAddCustomFolderIntent(
    context: Context? = null,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): Intent {
    return AudioRecorderEngine.createAddCustomFolderIntent(context = context, userRoot = userRoot)
  }

  fun resolveCustomFolderFromTreeUri(
    uri: Uri?,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): MusicFolder? {
    if (uri == null) return null
    val resolved = GalleryEngine.resolveCustomFolderFromTreeUri(uri, userRoot) ?: return null
    val cleanDisplay = if (resolved.displayPath.startsWith("/")) resolved.displayPath else "/${resolved.displayPath}"
    if (cleanDisplay.equals("/Music", ignoreCase = true) && resolved.customAbsolutePath == null) {
      return DEFAULT_MUSIC_SCOPED_FOLDER.copy(treeUri = uri.toString())
    }
    return MusicFolder(
      folderName = resolved.folderName,
      displayPath = cleanDisplay,
      treeUri = resolved.treeUri ?: uri.toString(),
      customAbsolutePath = resolved.customAbsolutePath,
    )
  }

  /**
   * Scope key for saving a section-specific sort order (`"tab:HOME"`, `"tab:PLAYLISTS"`, etc.)
   * or a custom folder-specific sort order (`"folder:/path"`).
   */
  fun sortScopeKeyForTab(tab: MusicLibraryTab): String = "tab:${tab.name}"

  fun sortScopeKeyForFolder(displayPath: String): String =
    "folder:/${displayPath.trim().trim('/').lowercase(Locale.ROOT)}"

  fun resolveSortScopeKey(
    selectedTab: MusicLibraryTab,
    selectedCustomFolderPath: String? = null,
  ): String {
    return if (!selectedCustomFolderPath.isNullOrBlank()) {
      sortScopeKeyForFolder(selectedCustomFolderPath)
    } else {
      sortScopeKeyForTab(selectedTab)
    }
  }

  /**
   * Returns the "Save for ..." checkbox label in the Sort menu for the active view
   * (`"Save for home"`, `"Save for playlists"`, `"Save for songs"`, `"Save for albums"`,
   * `"Save for artists"`, `"Save for genre"`, or `"Save for folder"`).
   */
  fun resolveSortSaveLabel(
    selectedTab: MusicLibraryTab,
    selectedCustomFolderPath: String? = null,
  ): String {
    if (!selectedCustomFolderPath.isNullOrBlank()) {
      return "Save for folder"
    }
    return when (selectedTab) {
      MusicLibraryTab.HOME -> "Save for home"
      MusicLibraryTab.PLAYLISTS -> "Save for playlists"
      MusicLibraryTab.SONGS -> "Save for songs"
      MusicLibraryTab.ALBUMS -> "Save for albums"
      MusicLibraryTab.ARTISTS -> "Save for artists"
      MusicLibraryTab.GENRES -> "Save for genre"
    }
  }

  val DEFAULT_SAVED_SECTION_SORTS: Map<String, MusicSortOrder> = mapOf(
    sortScopeKeyForTab(MusicLibraryTab.HOME) to MusicSortOrder.RANDOM,
  )

  val DEFAULT_SAVED_SECTION_SORT_GROUPS: Map<String, MusicSortGroupConfig> = mapOf(
    sortScopeKeyForTab(MusicLibraryTab.HOME) to MusicSortGroupConfig(
      groupOption = MusicGroupOption.NONE,
      sortOrder = MusicSortOrder.RANDOM,
    ),
  )

  /**
   * Returns the available [MusicGroupOption]s for the given [selectedTab] or [selectedCustomFolderPath].
   * - Songs view (including custom folder views): [MusicGroupOption.ALBUM], [MusicGroupOption.ARTIST], [MusicGroupOption.GENRE], [MusicGroupOption.YEAR], [MusicGroupOption.NONE].
   * - Albums view: [MusicGroupOption.ARTIST], [MusicGroupOption.GENRE], [MusicGroupOption.YEAR], [MusicGroupOption.NONE].
   * - Other views (Home, Playlists, Artists, Genres): empty list (sort only).
   */
  fun availableGroupOptions(
    selectedTab: MusicLibraryTab,
    selectedCustomFolderPath: String? = null,
  ): List<MusicGroupOption> {
    if (!selectedCustomFolderPath.isNullOrBlank()) {
      return MusicGroupOption.SONGS_OPTIONS
    }
    return when (selectedTab) {
      MusicLibraryTab.SONGS -> MusicGroupOption.SONGS_OPTIONS
      MusicLibraryTab.ALBUMS -> MusicGroupOption.ALBUMS_OPTIONS
      else -> emptyList()
    }
  }

  /**
   * Coerces [groupOption] so that it is valid for the active [selectedTab] / [selectedCustomFolderPath],
   * falling back to [MusicGroupOption.NONE] if not supported in that section.
   */
  fun coerceGroupOptionForScope(
    groupOption: MusicGroupOption,
    selectedTab: MusicLibraryTab,
    selectedCustomFolderPath: String? = null,
  ): MusicGroupOption {
    val available = availableGroupOptions(selectedTab, selectedCustomFolderPath)
    return if (groupOption in available) groupOption else MusicGroupOption.NONE
  }

  fun parseSavedSectionSortGroups(rawString: String?): Map<String, MusicSortGroupConfig> {
    // By default (when never customized), Home has "Save for home" pre-checked with RANDOM sort
    if (rawString == null) return DEFAULT_SAVED_SECTION_SORT_GROUPS
    val trimmed = rawString.trim()
    if (trimmed.isEmpty() || trimmed == "__none__") return emptyMap()
    val result = LinkedHashMap<String, MusicSortGroupConfig>()
    for (line in trimmed.lines()) {
      val entry = line.trim()
      if (entry.isEmpty() || entry == "__none__") continue
      val sep = entry.lastIndexOf('=')
      if (sep <= 0) continue
      val key = entry.substring(0, sep).trim()
      val valuePart = entry.substring(sep + 1).trim()
      if (key.isNotEmpty() && valuePart.isNotEmpty()) {
        if (valuePart.contains(':')) {
          val parts = valuePart.split(':', limit = 2)
          result[key] = MusicSortGroupConfig(
            groupOption = MusicGroupOption.fromNameOrDefault(parts[0]),
            sortOrder = MusicSortOrder.fromNameOrDefault(parts[1]),
          )
        } else {
          result[key] = MusicSortGroupConfig(
            groupOption = MusicGroupOption.NONE,
            sortOrder = MusicSortOrder.fromNameOrDefault(valuePart),
          )
        }
      }
    }
    return result
  }

  fun serializeSavedSectionSortGroups(saved: Map<String, MusicSortGroupConfig>): String {
    if (saved.isEmpty()) return "__none__"
    return saved.entries.joinToString("\n") { (key, config) ->
      "$key=${config.groupOption.name}:${config.sortOrder.name}"
    }
  }

  fun parseSavedSectionSorts(rawString: String?): Map<String, MusicSortOrder> {
    return parseSavedSectionSortGroups(rawString).mapValues { it.value.sortOrder }
  }

  fun serializeSavedSectionSorts(saved: Map<String, MusicSortOrder>): String {
    if (saved.isEmpty()) return "__none__"
    return saved.entries.joinToString("\n") { (key, order) -> "$key=${order.name}" }
  }

  /**
   * Sorts a list of [MusicPlaylist]s according to [sortOrder].
   * When [preserveOriginalOnRandom] is `true` (such as on the Home view where mixes are already
   * randomly generated), `MusicSortOrder.RANDOM` preserves the generated mix order.
   */
  fun sortPlaylists(
    playlists: List<MusicPlaylist>,
    sortOrder: MusicSortOrder,
    playStats: Map<String, TrackPlayStats> = emptyMap(),
    randomSeed: Long = 0L,
    preserveOriginalOnRandom: Boolean = false,
  ): List<MusicPlaylist> {
    if (playlists.size <= 1) return playlists
    return when (sortOrder) {
      MusicSortOrder.RANDOM -> {
        if (preserveOriginalOnRandom) {
          playlists
        } else {
          val rng = kotlin.random.Random(if (randomSeed != 0L) randomSeed else 1L)
          playlists.shuffled(rng)
        }
      }
      MusicSortOrder.TITLE_ASC, MusicSortOrder.ARTIST_ASC, MusicSortOrder.ALBUM_ASC ->
        playlists.sortedBy { it.name.lowercase(Locale.ROOT) }
      MusicSortOrder.TITLE_DESC ->
        playlists.sortedByDescending { it.name.lowercase(Locale.ROOT) }
      MusicSortOrder.DURATION_DESC ->
        playlists.sortedWith(
          compareByDescending<MusicPlaylist> { it.totalDurationMs }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.DURATION_ASC ->
        playlists.sortedWith(
          compareBy<MusicPlaylist> { it.totalDurationMs }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.YEAR_DESC ->
        playlists.sortedWith(
          compareByDescending<MusicPlaylist> { pl -> pl.tracks.maxOfOrNull { it.year } ?: 0 }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.MOST_PLAYED ->
        playlists.sortedWith(
          compareByDescending<MusicPlaylist> { pl ->
            pl.tracks.sumOf { t -> playStats[t.id]?.engagementScoreForTrack(t.durationMs) ?: 0L }
          }.thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.RECENTLY_PLAYED ->
        playlists.sortedWith(
          compareByDescending<MusicPlaylist> { pl ->
            pl.tracks.maxOfOrNull { t -> playStats[t.id]?.lastPlayedAtMs ?: 0L } ?: 0L
          }.thenBy { it.name.lowercase(Locale.ROOT) }
        )
    }
  }

  /**
   * Sorts a list of [MusicArtist]s according to [sortOrder].
   */
  fun sortArtists(
    artists: List<MusicArtist>,
    sortOrder: MusicSortOrder,
    playStats: Map<String, TrackPlayStats> = emptyMap(),
    randomSeed: Long = 0L,
  ): List<MusicArtist> {
    if (artists.size <= 1) return artists
    return when (sortOrder) {
      MusicSortOrder.RANDOM -> {
        val rng = kotlin.random.Random(if (randomSeed != 0L) randomSeed else 1L)
        artists.shuffled(rng)
      }
      MusicSortOrder.TITLE_ASC, MusicSortOrder.ARTIST_ASC, MusicSortOrder.ALBUM_ASC ->
        artists.sortedBy { it.name.lowercase(Locale.ROOT) }
      MusicSortOrder.TITLE_DESC ->
        artists.sortedByDescending { it.name.lowercase(Locale.ROOT) }
      MusicSortOrder.DURATION_DESC ->
        artists.sortedWith(
          compareByDescending<MusicArtist> { it.totalDurationMs }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.DURATION_ASC ->
        artists.sortedWith(
          compareBy<MusicArtist> { it.totalDurationMs }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.YEAR_DESC ->
        artists.sortedWith(
          compareByDescending<MusicArtist> { a -> a.tracks.maxOfOrNull { it.year } ?: 0 }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.MOST_PLAYED ->
        artists.sortedWith(
          compareByDescending<MusicArtist> { a ->
            a.tracks.sumOf { t -> playStats[t.id]?.engagementScoreForTrack(t.durationMs) ?: 0L }
          }.thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.RECENTLY_PLAYED ->
        artists.sortedWith(
          compareByDescending<MusicArtist> { a ->
            a.tracks.maxOfOrNull { t -> playStats[t.id]?.lastPlayedAtMs ?: 0L } ?: 0L
          }.thenBy { it.name.lowercase(Locale.ROOT) }
        )
    }
  }

  /**
   * Sorts a list of [MusicAlbum]s according to [sortOrder].
   */
  fun sortAlbums(
    albums: List<MusicAlbum>,
    sortOrder: MusicSortOrder,
    playStats: Map<String, TrackPlayStats> = emptyMap(),
    randomSeed: Long = 0L,
  ): List<MusicAlbum> {
    if (albums.size <= 1) return albums
    return when (sortOrder) {
      MusicSortOrder.RANDOM -> {
        val rng = kotlin.random.Random(if (randomSeed != 0L) randomSeed else 1L)
        albums.shuffled(rng)
      }
      MusicSortOrder.TITLE_ASC, MusicSortOrder.ALBUM_ASC ->
        albums.sortedBy { it.name.lowercase(Locale.ROOT) }
      MusicSortOrder.TITLE_DESC ->
        albums.sortedByDescending { it.name.lowercase(Locale.ROOT) }
      MusicSortOrder.ARTIST_ASC ->
        albums.sortedWith(
          compareBy<MusicAlbum> { it.artist.lowercase(Locale.ROOT) }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.DURATION_DESC ->
        albums.sortedWith(
          compareByDescending<MusicAlbum> { it.totalDurationMs }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.DURATION_ASC ->
        albums.sortedWith(
          compareBy<MusicAlbum> { it.totalDurationMs }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.YEAR_DESC ->
        albums.sortedWith(
          compareByDescending<MusicAlbum> { it.year }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.MOST_PLAYED ->
        albums.sortedWith(
          compareByDescending<MusicAlbum> { al ->
            al.tracks.sumOf { t -> playStats[t.id]?.engagementScoreForTrack(t.durationMs) ?: 0L }
          }.thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.RECENTLY_PLAYED ->
        albums.sortedWith(
          compareByDescending<MusicAlbum> { al ->
            al.tracks.maxOfOrNull { t -> playStats[t.id]?.lastPlayedAtMs ?: 0L } ?: 0L
          }.thenBy { it.name.lowercase(Locale.ROOT) }
        )
    }
  }

  /**
   * Sorts a list of [MusicGenre]s according to [sortOrder].
   */
  fun sortGenres(
    genres: List<MusicGenre>,
    sortOrder: MusicSortOrder,
    playStats: Map<String, TrackPlayStats> = emptyMap(),
    randomSeed: Long = 0L,
  ): List<MusicGenre> {
    if (genres.size <= 1) return genres
    return when (sortOrder) {
      MusicSortOrder.RANDOM -> {
        val rng = kotlin.random.Random(if (randomSeed != 0L) randomSeed else 1L)
        genres.shuffled(rng)
      }
      MusicSortOrder.TITLE_ASC, MusicSortOrder.ARTIST_ASC, MusicSortOrder.ALBUM_ASC ->
        genres.sortedBy { it.name.lowercase(Locale.ROOT) }
      MusicSortOrder.TITLE_DESC ->
        genres.sortedByDescending { it.name.lowercase(Locale.ROOT) }
      MusicSortOrder.DURATION_DESC ->
        genres.sortedWith(
          compareByDescending<MusicGenre> { it.totalDurationMs }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.DURATION_ASC ->
        genres.sortedWith(
          compareBy<MusicGenre> { it.totalDurationMs }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.YEAR_DESC ->
        genres.sortedWith(
          compareByDescending<MusicGenre> { g -> g.tracks.maxOfOrNull { it.year } ?: 0 }
            .thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.MOST_PLAYED ->
        genres.sortedWith(
          compareByDescending<MusicGenre> { g ->
            g.tracks.sumOf { t -> playStats[t.id]?.engagementScoreForTrack(t.durationMs) ?: 0L }
          }.thenBy { it.name.lowercase(Locale.ROOT) }
        )
      MusicSortOrder.RECENTLY_PLAYED ->
        genres.sortedWith(
          compareByDescending<MusicGenre> { g ->
            g.tracks.maxOfOrNull { t -> playStats[t.id]?.lastPlayedAtMs ?: 0L } ?: 0L
          }.thenBy { it.name.lowercase(Locale.ROOT) }
        )
    }
  }

  /**
   * Groups an already-sorted list of [tracks] according to [groupOption] (Album, Artist, Genre, Year, or None).
   * When [groupOption] is [MusicGroupOption.NONE], returns a single group with an empty title.
   */
  fun groupTracks(
    tracks: List<MusicTrack>,
    groupOption: MusicGroupOption,
  ): List<MusicTrackGroup> {
    if (tracks.isEmpty()) return emptyList()
    return when (groupOption) {
      MusicGroupOption.NONE -> listOf(MusicTrackGroup(title = "", tracks = tracks))
      MusicGroupOption.ALBUM,
      MusicGroupOption.ARTIST,
      MusicGroupOption.GENRE,
      MusicGroupOption.YEAR -> {
        val grouped = LinkedHashMap<String, MutableList<MusicTrack>>()
        val displayTitles = LinkedHashMap<String, String>()
        for (track in tracks) {
          val rawLabel = when (groupOption) {
            MusicGroupOption.ALBUM -> track.album.trim().ifBlank { "Unknown Album" }
            MusicGroupOption.ARTIST -> track.artist.trim().ifBlank { "Unknown Artist" }
            MusicGroupOption.GENRE -> track.genre.trim().ifBlank { "Unknown Genre" }
            MusicGroupOption.YEAR -> if (track.year > 0) track.year.toString() else "Unknown Year"
            MusicGroupOption.NONE -> ""
          }
          val normKey = rawLabel.lowercase(Locale.ROOT)
          if (normKey !in displayTitles) {
            displayTitles[normKey] = rawLabel
          }
          grouped.getOrPut(normKey) { mutableListOf() }.add(track)
        }
        val groupList = grouped.entries
          .map { (normKey, groupTracks) ->
            MusicTrackGroup(
              title = displayTitles[normKey] ?: normKey,
              tracks = groupTracks,
            )
          }
        if (groupOption == MusicGroupOption.YEAR) {
          groupList.sortedWith(
            compareByDescending<MusicTrackGroup> { it.title.toIntOrNull() ?: 0 }
              .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title },
          )
        } else {
          groupList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        }
      }
    }
  }

  /**
   * Sorts [tracks] by [sortOrder] and groups them according to [groupOption].
   */
  fun groupAndSortTracks(
    tracks: List<MusicTrack>,
    groupOption: MusicGroupOption,
    sortOrder: MusicSortOrder,
    playStats: Map<String, TrackPlayStats> = emptyMap(),
    randomSeed: Long = 0L,
  ): List<MusicTrackGroup> {
    val sorted = sortTracks(
      tracks = tracks,
      sortOrder = sortOrder,
      playStats = playStats,
      randomSeed = randomSeed,
    )
    return groupTracks(sorted, groupOption)
  }

  /**
   * Groups an already-sorted list of [albums] according to [groupOption] (Artist, Genre, Year, or None).
   * When [groupOption] is [MusicGroupOption.NONE] (or unsupported), returns a single group with an empty title.
   */
  fun groupAlbums(
    albums: List<MusicAlbum>,
    groupOption: MusicGroupOption,
  ): List<MusicAlbumGroup> {
    if (albums.isEmpty()) return emptyList()
    return when (groupOption) {
      MusicGroupOption.NONE,
      MusicGroupOption.ALBUM -> listOf(MusicAlbumGroup(title = "", albums = albums))
      MusicGroupOption.ARTIST,
      MusicGroupOption.GENRE,
      MusicGroupOption.YEAR -> {
        val grouped = LinkedHashMap<String, MutableList<MusicAlbum>>()
        val displayTitles = LinkedHashMap<String, String>()
        for (album in albums) {
          val rawLabel = when (groupOption) {
            MusicGroupOption.ARTIST -> album.artist.trim().ifBlank { "Unknown Artist" }
            MusicGroupOption.GENRE -> album.genre.trim().ifBlank { "Unknown Genre" }
            MusicGroupOption.YEAR -> if (album.year > 0) album.year.toString() else "Unknown Year"
            else -> ""
          }
          val normKey = rawLabel.lowercase(Locale.ROOT)
          if (normKey !in displayTitles) {
            displayTitles[normKey] = rawLabel
          }
          grouped.getOrPut(normKey) { mutableListOf() }.add(album)
        }
        val groupList = grouped.entries
          .map { (normKey, groupAlbums) ->
            MusicAlbumGroup(
              title = displayTitles[normKey] ?: normKey,
              albums = groupAlbums,
            )
          }
        if (groupOption == MusicGroupOption.YEAR) {
          groupList.sortedWith(
            compareByDescending<MusicAlbumGroup> { it.title.toIntOrNull() ?: 0 }
              .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title },
          )
        } else {
          groupList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        }
      }
    }
  }

  /**
   * Sorts [albums] by [sortOrder] and groups them according to [groupOption].
   */
  fun groupAndSortAlbums(
    albums: List<MusicAlbum>,
    groupOption: MusicGroupOption,
    sortOrder: MusicSortOrder,
    playStats: Map<String, TrackPlayStats> = emptyMap(),
    randomSeed: Long = 0L,
  ): List<MusicAlbumGroup> {
    val sorted = sortAlbums(
      albums = albums,
      sortOrder = sortOrder,
      playStats = playStats,
      randomSeed = randomSeed,
    )
    return groupAlbums(sorted, groupOption)
  }

  /**
   * Resolves the flat `LazyColumn` / `LazyVerticalGrid` item index for [currentTrackId] inside
   * [groups], accounting for group headers and [collapsedGroupTitles]. Returns `-1` if not found.
   */
  fun resolveFocusedGroupedTrackLazyIndex(
    groups: List<MusicTrackGroup>,
    currentTrackId: String?,
    collapsedGroupTitles: Set<String> = emptySet(),
  ): Int {
    if (currentTrackId.isNullOrBlank()) return -1
    var lazyIndex = 0
    for (group in groups) {
      val hasHeader = group.title.isNotEmpty()
      if (hasHeader) {
        lazyIndex++
      }
      val isCollapsed = hasHeader && group.title in collapsedGroupTitles
      if (!isCollapsed) {
        for (track in group.tracks) {
          if (track.id == currentTrackId) {
            return lazyIndex
          }
          lazyIndex++
        }
      }
    }
    return -1
  }

  /**
   * Resolves the flat `LazyColumn` / `LazyVerticalGrid` item index for [focusedAlbumKey] inside
   * [groups], accounting for group headers and [collapsedGroupTitles]. Returns `-1` if not found.
   */
  fun resolveFocusedGroupedAlbumLazyIndex(
    groups: List<MusicAlbumGroup>,
    focusedAlbumKey: String?,
    collapsedGroupTitles: Set<String> = emptySet(),
  ): Int {
    if (focusedAlbumKey.isNullOrBlank()) return -1
    var lazyIndex = 0
    for (group in groups) {
      val hasHeader = group.title.isNotEmpty()
      if (hasHeader) {
        lazyIndex++
      }
      val isCollapsed = hasHeader && group.title in collapsedGroupTitles
      if (!isCollapsed) {
        for (album in group.albums) {
          if (album.key.equals(focusedAlbumKey, ignoreCase = true)) {
            return lazyIndex
          }
          lazyIndex++
        }
      }
    }
    return -1
  }

  /**
   * Checks whether the app currently has the full files access permission (`MANAGE_EXTERNAL_STORAGE`
   * on Android 11+ / `READ_EXTERNAL_STORAGE` on older versions) and [AppPermission.STORAGE] is enabled.
   */
  fun hasFullFilesPermission(context: Context): Boolean {
    return com.example.toolbox.ui.main.NavigationPreferencesBridge.isPermissionGranted(
      com.example.toolbox.ui.main.AppPermission.STORAGE
    ) && FileManagerEngine.hasStoragePermission(context)
  }

  /**
   * Checks whether [track] belongs to [folder] (matching either its [MusicFolder.customAbsolutePath]
   * or its relative [MusicFolder.displayPath] under [rootDirectory]).
   */
  fun isTrackInFolder(
    track: MusicTrack,
    folder: MusicFolder,
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
  ): Boolean {
    val file = track.file ?: return false
    val folderDir = resolveDirectoryForFolder(folder, rootDirectory)
    val trackPath = file.absolutePath
    val folderPath = folderDir.absolutePath.trimEnd('/')
    return trackPath.equals(folderPath, ignoreCase = true) ||
      trackPath.startsWith("$folderPath/", ignoreCase = true) ||
      file.parentFile?.name?.equals(folder.folderName, ignoreCase = true) == true
  }

  fun resolveDefaultMusicDirectories(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    hasFullFilesPermission: Boolean = true,
  ): List<File> {
    val folderNames = if (hasFullFilesPermission) {
      FULL_FILES_DEFAULT_MUSIC_FOLDER_NAMES
    } else {
      DEFAULT_SCOPED_MUSIC_FOLDER_NAMES
    }
    val defaults = folderNames.map { File(rootDirectory, it) }.toMutableList()
    if (hasFullFilesPermission) {
      val downloadsDir = File(rootDirectory, "Downloads")
      if (downloadsDir.exists() && downloadsDir.isDirectory) {
        defaults.add(downloadsDir)
      }
    }
    return defaults
  }

  /**
   * Computes a shared identity key for [track]'s album artwork so all tracks belonging to the same
   * album share a single decoded [Bitmap] and [AlbumArtPalette] in cache, while untagged single
   * files remain isolated by file path.
   */
  internal fun albumArtIdentityKey(track: MusicTrack): String {
    if (track.isSampleTrack) {
      return "sample:${track.album.trim().lowercase(Locale.ROOT)}|${track.artist.trim().lowercase(Locale.ROOT)}|${track.artworkPrimaryRgb}"
    }
    val normalizedAlbum = track.album.trim().lowercase(Locale.ROOT)
    val normalizedArtist = track.artist.trim().lowercase(Locale.ROOT)
    val isGenericOrSingleFolderBucket = isSingleFolderAlbumName(track.album) ||
      normalizedAlbum.isEmpty() ||
      normalizedAlbum == "local recordings" ||
      normalizedAlbum == "unknown album" ||
      normalizedAlbum == "download" ||
      normalizedAlbum == "downloads" ||
      normalizedAlbum == "music"
    val file = track.file
    return if (isGenericOrSingleFolderBucket && file != null) {
      "file:${file.absolutePath}:${track.lastModifiedMs}"
    } else {
      val parentPath = file?.parentFile?.absolutePath.orEmpty()
      "album:$parentPath|$normalizedAlbum|$normalizedArtist"
    }
  }

  private fun trackPaletteKey(track: MusicTrack): String {
    return "${track.id}:${track.artworkPrimaryRgb}:${track.artworkSecondaryRgb}:${track.artworkAccentRgb}"
  }

  private fun albumPaletteKey(track: MusicTrack): String {
    return "extracted_palette:${albumArtIdentityKey(track)}"
  }

  private fun filePaletteKey(file: File): String {
    return "extracted_file_palette:${file.absolutePath}:${file.lastModified()}:${file.length()}"
  }

  private fun filePaletteKey(track: MusicTrack): String? {
    val file = track.file ?: return null
    if (track.isSampleTrack) return null
    val mod = if (track.lastModifiedMs > 0L) track.lastModifiedMs else file.lastModified()
    val len = if (track.sizeBytes > 0L) track.sizeBytes else file.length()
    return "extracted_file_palette:${file.absolutePath}:$mod:$len"
  }

  private fun lookupCachedExtractedPalette(track: MusicTrack, fileOverride: File? = track.file): AlbumArtPalette? {
    if (track.isSampleTrack) return null
    val albumKey = albumPaletteKey(track)
    val fKey = fileOverride?.let { f ->
      val mod = if (track.lastModifiedMs > 0L) track.lastModifiedMs else f.lastModified()
      val len = if (track.sizeBytes > 0L) track.sizeBytes else f.length()
      "extracted_file_palette:${f.absolutePath}:$mod:$len"
    }
    synchronized(artworkCacheLock) {
      paletteCache[albumKey]?.let { return it }
      if (fKey != null) {
        paletteCache[fKey]?.let { found ->
          paletteCache[albumKey] = found
          return found
        }
      }
      return null
    }
  }

  private fun applyPaletteToTrack(track: MusicTrack, palette: AlbumArtPalette): MusicTrack {
    val (pRgb, sRgb, aRgb) = paletteToRgbTriple(palette)
    if (track.artworkPrimaryRgb == pRgb &&
      track.artworkSecondaryRgb == sRgb &&
      track.artworkAccentRgb == aRgb
    ) {
      return track
    }
    return track.copy(
      artworkPrimaryRgb = pRgb,
      artworkSecondaryRgb = sRgb,
      artworkAccentRgb = aRgb,
    )
  }

  private fun runComposeStateUpdate(block: () -> Unit) {
    val mainLooper = try {
      Looper.getMainLooper()
    } catch (_: Throwable) {
      null
    }
    val currentLooper = try {
      Looper.myLooper()
    } catch (_: Throwable) {
      null
    }
    if (mainLooper == null || currentLooper === mainLooper) {
      Snapshot.withMutableSnapshot {
        block()
      }
    } else {
      val posted = try {
        Handler(mainLooper).post {
          Snapshot.withMutableSnapshot {
            block()
          }
        }
      } catch (_: Throwable) {
        false
      }
      if (!posted) {
        Snapshot.withMutableSnapshot {
          block()
        }
      }
    }
  }

  private fun recordExtractedPaletteForTrack(
    track: MusicTrack,
    extractedPalette: AlbumArtPalette,
  ) {
    if (track.isSampleTrack) return
    val albumKey = albumPaletteKey(track)
    val fKey = filePaletteKey(track)
    synchronized(artworkCacheLock) {
      paletteCache[albumKey] = extractedPalette
      if (fKey != null) {
        paletteCache[fKey] = extractedPalette
      }
    }
    val updatedTrack = applyPaletteToTrack(track, extractedPalette)
    val file = track.file
    if (file != null) {
      val mod = if (track.lastModifiedMs > 0L) track.lastModifiedMs else file.lastModified()
      val len = if (track.sizeBytes > 0L) track.sizeBytes else file.length()
      val baseKey = "${file.absolutePath}:$mod:$len"
      synchronized(metadataCacheLock) {
        trackMetadataCache["$baseKey:true"]?.let { existing ->
          trackMetadataCache["$baseKey:true"] = applyPaletteToTrack(existing, extractedPalette)
        } ?: run {
          trackMetadataCache["$baseKey:true"] = updatedTrack
        }
        trackMetadataCache["$baseKey:false"]?.let { existing ->
          trackMetadataCache["$baseKey:false"] = applyPaletteToTrack(existing, extractedPalette)
        }
        trackIndexByPath[file.absolutePath]?.let { existing ->
          trackIndexByPath[file.absolutePath] = applyPaletteToTrack(existing, extractedPalette)
        }
      }
    }
    runComposeStateUpdate {
      val activeUpdated = activeTrack?.id == track.id
      if (activeUpdated) {
        activeTrack = updatedTrack
        internalTrackPalette = extractedPalette
      }
      val session = activeSessionState
      if (session != null && !session.isReleased && session.currentTrack?.id == track.id) {
        session.activePalette = extractedPalette
        session.syncSystemMediaControls(
          forceMetadataUpdate = false,
          notifyPlatformInBackground = true,
        )
      } else if (activeUpdated) {
        refreshAllWidgetsForMediaStateChange(monitoredAppContext)
      }
    }
  }

  internal fun syncTracksWithExtractedPalettes(tracks: List<MusicTrack>): List<MusicTrack> {
    if (tracks.isEmpty()) return tracks
    var changed = false
    val updated = tracks.map { track ->
      val extracted = lookupCachedExtractedPalette(track)
      if (extracted != null) {
        val withPal = applyPaletteToTrack(track, extracted)
        if (withPal !== track) {
          changed = true
        }
        withPal
      } else {
        track
      }
    }
    return if (changed) updated else tracks
  }

  private fun paletteToRgbTriple(palette: AlbumArtPalette): Triple<Int, Int, Int> {
    val pRgb = ((palette.dominantColor.red * 255f).roundToInt().coerceIn(0, 255) shl 16) or
      ((palette.dominantColor.green * 255f).roundToInt().coerceIn(0, 255) shl 8) or
      (palette.dominantColor.blue * 255f).roundToInt().coerceIn(0, 255)
    val sRgb = ((palette.secondaryColor.red * 255f).roundToInt().coerceIn(0, 255) shl 16) or
      ((palette.secondaryColor.green * 255f).roundToInt().coerceIn(0, 255) shl 8) or
      (palette.secondaryColor.blue * 255f).roundToInt().coerceIn(0, 255)
    val aRgb = ((palette.accentColor.red * 255f).roundToInt().coerceIn(0, 255) shl 16) or
      ((palette.accentColor.green * 255f).roundToInt().coerceIn(0, 255) shl 8) or
      (palette.accentColor.blue * 255f).roundToInt().coerceIn(0, 255)
    return Triple(pRgb, sRgb, aRgb)
  }

  /**
   * Resolves the [AlbumArtPalette] for [track], using cached bitmap extraction if available,
   * or computing deterministically from the track's album art colors.
   */
  fun resolveTrackPalette(track: MusicTrack?): AlbumArtPalette {
    if (track == null) {
      return buildPaletteFromRgb(0x6750A4, 0x311B92, 0xD0BCFF)
    }
    lookupCachedExtractedPalette(track)?.let { return it }
    val key = trackPaletteKey(track)
    synchronized(artworkCacheLock) {
      fallbackPaletteCache[key]?.let { return it }
    }
    val palette = buildPaletteFromRgb(
      primaryRgb = track.artworkPrimaryRgb,
      secondaryRgb = track.artworkSecondaryRgb,
      accentRgb = track.artworkAccentRgb,
    )
    synchronized(artworkCacheLock) {
      fallbackPaletteCache[key] = palette
    }
    return palette
  }

  /**
   * Returns a cached album art [Bitmap] for [track] if already rendered/decoded, or `null`.
   * Reuses a cached bitmap of equal or higher resolution for the same album when available.
   */
  fun getCachedAlbumArtBitmap(track: MusicTrack, sizePx: Int = 320): Bitmap? {
    val safeSize = sizePx.coerceIn(64, 1024)
    val bucketSize = if (safeSize <= 256) 240 else 360
    val altBucketSize = if (bucketSize == 240) 360 else 240
    val identity = albumArtIdentityKey(track)
    val primaryKey = "$identity|$bucketSize"
    val exactKey = "$identity|$safeSize"
    val altKey = "$identity|$altBucketSize"
    val procPrimaryKey = "procedural:${track.id}:$identity|$bucketSize"
    val procExactKey = "procedural:${track.id}:$identity|$safeSize"
    val procAltKey = "procedural:${track.id}:$identity|$altBucketSize"

    synchronized(artworkCacheLock) {
      artworkBitmapCache[primaryKey]?.let { return it }
      artworkBitmapCache[exactKey]?.let { return it }
      artworkBitmapCache[altKey]?.let { return it }
      artworkBitmapCache[procPrimaryKey]?.let { return it }
      artworkBitmapCache[procExactKey]?.let { return it }
      artworkBitmapCache[procAltKey]?.let { return it }
      return null
    }
  }

  private fun isCachedBitmapProcedural(track: MusicTrack, bitmap: Bitmap, sizePx: Int): Boolean {
    val safeSize = sizePx.coerceIn(64, 1024)
    val bucketSize = if (safeSize <= 256) 240 else 360
    val altBucketSize = if (bucketSize == 240) 360 else 240
    val identity = albumArtIdentityKey(track)
    val primaryKey = "$identity|$bucketSize"
    val exactKey = "$identity|$safeSize"
    val altKey = "$identity|$altBucketSize"
    synchronized(artworkCacheLock) {
      if (artworkBitmapCache[primaryKey] === bitmap && primaryKey !in proceduralArtworkKeys) return false
      if (artworkBitmapCache[exactKey] === bitmap && exactKey !in proceduralArtworkKeys) return false
      if (artworkBitmapCache[altKey] === bitmap && altKey !in proceduralArtworkKeys) return false
      return true
    }
  }

  private fun cacheAlbumArtBitmap(
    track: MusicTrack,
    sizePx: Int,
    bitmap: Bitmap,
    extractedPalette: AlbumArtPalette? = null,
    isProcedural: Boolean = false,
  ) {
    val safeSize = sizePx.coerceIn(64, 1024)
    val bucketSize = if (safeSize <= 256) 240 else 360
    val identity = albumArtIdentityKey(track)
    val primaryKey = if (isProcedural && !track.isSampleTrack) {
      "procedural:${track.id}:$identity|$bucketSize"
    } else {
      "$identity|$bucketSize"
    }
    val exactKey = if (isProcedural && !track.isSampleTrack) {
      "procedural:${track.id}:$identity|$safeSize"
    } else {
      "$identity|$safeSize"
    }

    synchronized(artworkCacheLock) {
      artworkBitmapCache[primaryKey] = bitmap
      if (isProcedural) {
        proceduralArtworkKeys.add(primaryKey)
      } else {
        proceduralArtworkKeys.remove(primaryKey)
      }
      if (safeSize != bucketSize) {
        artworkBitmapCache[exactKey] = bitmap
        if (isProcedural) {
          proceduralArtworkKeys.add(exactKey)
        } else {
          proceduralArtworkKeys.remove(exactKey)
        }
      }
    }
    if (extractedPalette != null && !isProcedural) {
      recordExtractedPaletteForTrack(track, extractedPalette)
    }
  }

  /**
   * Decodes a compressed image [bytes] array with power-of-two subsampling (`inSampleSize`)
   * so embedded high-resolution cover art is scaled efficiently to [reqSizePx].
   */
  fun decodeDownsampledBitmap(bytes: ByteArray, reqSizePx: Int = 320): Bitmap? {
    if (bytes.isEmpty()) return null
    val targetSize = reqSizePx.coerceIn(64, 1024)
    return try {
      val boundsOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
      val width = boundsOptions.outWidth
      val height = boundsOptions.outHeight
      val decoded = if (width <= 0 || height <= 0) {
        val opts = BitmapFactory.Options().apply {
          inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
      } else {
        val inSampleSize = computeBoundedInSampleSize(width, height, targetSize)
        val decodeOptions = BitmapFactory.Options().apply {
          this.inSampleSize = inSampleSize
          inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
      }
      scaleAndNormalizeSoftwareBitmap(decoded, maxDimPx = targetSize)
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Pure-Kotlin fallback reader that extracts embedded cover art bytes from:
   * - ID3v2 (`ID3v2.3` / `ID3v2.4` `APIC` frames and `ID3v2.2` `PIC` frames, either at file offset 0
   *   or inside a RIFF WAVE `"id3 "` / `"ID3 "` chunk)
   * - FLAC `METADATA_BLOCK_PICTURE` (block type 6)
   * - MP4 / M4A `covr` atom (`moov -> udta -> meta -> ilst -> covr -> data`)
   */
  internal fun extractEmbeddedArtworkBytes(file: File): ByteArray? {
    if (!file.exists() || !file.isFile || file.length() < 16L) return null
    return try {
      RandomAccessFile(file, "r").use { raf ->
        val fileLen = raf.length()
        val magic = ByteArray(12)
        raf.readFully(magic)

        // 1. Direct ID3v2 header at offset 0
        if (magic[0] == 'I'.code.toByte() &&
          magic[1] == 'D'.code.toByte() &&
          magic[2] == '3'.code.toByte()
        ) {
          extractId3v2PictureFromOffset(raf, 0L, fileLen)?.let { return it }
        }

        // 2. RIFF WAVE container with embedded "id3 " or "ID3 " chunk
        if (magic[0] == 'R'.code.toByte() &&
          magic[1] == 'I'.code.toByte() &&
          magic[2] == 'F'.code.toByte() &&
          magic[3] == 'F'.code.toByte() &&
          magic[8] == 'W'.code.toByte() &&
          magic[9] == 'A'.code.toByte() &&
          magic[10] == 'V'.code.toByte() &&
          magic[11] == 'E'.code.toByte()
        ) {
          var pos = 12L
          while (pos + 8L <= fileLen) {
            raf.seek(pos)
            val chunkHdr = ByteArray(8)
            raf.readFully(chunkHdr)
            val chunkId = String(chunkHdr, 0, 4, Charsets.US_ASCII)
            val chunkSize = ByteBuffer.wrap(chunkHdr, 4, 4)
              .order(ByteOrder.LITTLE_ENDIAN)
              .int
              .toLong() and 0xFFFFFFFFL
            val dataOffset = pos + 8L
            if (chunkId.equals("id3 ", ignoreCase = true) && dataOffset + 10L <= fileLen) {
              extractId3v2PictureFromOffset(raf, dataOffset, min(fileLen, dataOffset + chunkSize))?.let { return it }
            }
            val padded = chunkSize + (if (chunkSize % 2L == 1L) 1L else 0L)
            val next = dataOffset + padded
            if (next <= pos) break
            pos = next
          }
        }

        // 3. FLAC container ("fLaC" magic at offset 0)
        if (magic[0] == 'f'.code.toByte() &&
          magic[1] == 'L'.code.toByte() &&
          magic[2] == 'a'.code.toByte() &&
          magic[3] == 'C'.code.toByte()
        ) {
          var pos = 4L
          while (pos + 4L <= fileLen) {
            raf.seek(pos)
            val bh = ByteArray(4)
            raf.readFully(bh)
            val isLast = (bh[0].toInt() and 0x80) != 0
            val blockType = bh[0].toInt() and 0x7F
            val blockLen = ((bh[1].toInt() and 0xFF) shl 16) or
              ((bh[2].toInt() and 0xFF) shl 8) or
              (bh[3].toInt() and 0xFF)
            val blockStart = pos + 4L
            if (blockLen < 0 || blockStart + blockLen > fileLen) break
            if (blockType == 6 && blockLen in 32..(16 * 1024 * 1024)) {
              val payload = ByteArray(blockLen)
              raf.readFully(payload)
              parseFlacPictureBlock(payload)?.let { return it }
            }
            if (isLast) break
            pos = blockStart + blockLen
          }
        }

        // 4. MP4 / M4A ISO Base Media File Format ("ftyp" or top-level boxes)
        if (magic[4] == 'f'.code.toByte() &&
          magic[5] == 't'.code.toByte() &&
          magic[6] == 'y'.code.toByte() &&
          magic[7] == 'p'.code.toByte()
        ) {
          extractMp4CovrAtom(raf, 0L, fileLen, 0)?.let { return it }
        }

        null
      }
    } catch (_: Throwable) {
      null
    }
  }

  private fun extractId3v2PictureFromOffset(
    raf: RandomAccessFile,
    tagOffset: Long,
    maxEndOffset: Long,
  ): ByteArray? {
    if (tagOffset + 10L > maxEndOffset) return null
    raf.seek(tagOffset)
    val header = ByteArray(10)
    raf.readFully(header)
    if (header[0] != 'I'.code.toByte() ||
      header[1] != 'D'.code.toByte() ||
      header[2] != '3'.code.toByte()
    ) {
      return null
    }
    val majorVersion = header[3].toInt() and 0xFF
    val tagFlags = header[5].toInt() and 0xFF
    val tagSize = ((header[6].toInt() and 0x7F) shl 21) or
      ((header[7].toInt() and 0x7F) shl 14) or
      ((header[8].toInt() and 0x7F) shl 7) or
      (header[9].toInt() and 0x7F)
    if (tagSize <= 0) return null
    val tagEnd = (tagOffset + 10L + tagSize).coerceAtMost(maxEndOffset)

    if (majorVersion in 3..4 && (tagFlags and 0x40) != 0 && raf.filePointer + 4L <= tagEnd) {
      val extBytes = ByteArray(4)
      raf.readFully(extBytes)
      val extSize = if (majorVersion == 4) {
        ((extBytes[0].toInt() and 0x7F) shl 21) or
          ((extBytes[1].toInt() and 0x7F) shl 14) or
          ((extBytes[2].toInt() and 0x7F) shl 7) or
          (extBytes[3].toInt() and 0x7F)
      } else {
        ByteBuffer.wrap(extBytes).order(ByteOrder.BIG_ENDIAN).int + 4
      }
      if (extSize >= 4 && tagOffset + 10L + extSize <= tagEnd) {
        raf.seek(tagOffset + 10L + extSize)
      }
    }

    var fallbackPicture: ByteArray? = null
    val headerSize = if (majorVersion == 2) 6 else 10
    while (raf.filePointer + headerSize <= tagEnd) {
      val frameHeader = ByteArray(headerSize)
      raf.readFully(frameHeader)
      if (frameHeader[0].toInt() == 0) break

      val frameId: String
      val frameSize: Int
      val frameFlags2: Int
      if (majorVersion == 2) {
        frameId = String(frameHeader, 0, 3, Charsets.US_ASCII)
        frameSize = ((frameHeader[3].toInt() and 0xFF) shl 16) or
          ((frameHeader[4].toInt() and 0xFF) shl 8) or
          (frameHeader[5].toInt() and 0xFF)
        frameFlags2 = 0
      } else {
        frameId = String(frameHeader, 0, 4, Charsets.US_ASCII)
        val rawBeSize = ByteBuffer.wrap(frameHeader, 4, 4).order(ByteOrder.BIG_ENDIAN).int
        val hasHighBit = (frameHeader[4].toInt() and 0x80) != 0 ||
          (frameHeader[5].toInt() and 0x80) != 0 ||
          (frameHeader[6].toInt() and 0x80) != 0 ||
          (frameHeader[7].toInt() and 0x80) != 0
        frameSize = if (majorVersion == 4 && !hasHighBit) {
          val synchSize = ((frameHeader[4].toInt() and 0x7F) shl 21) or
            ((frameHeader[5].toInt() and 0x7F) shl 14) or
            ((frameHeader[6].toInt() and 0x7F) shl 7) or
            (frameHeader[7].toInt() and 0x7F)
          if (raf.filePointer + synchSize <= tagEnd) synchSize else rawBeSize
        } else {
          rawBeSize
        }
        frameFlags2 = frameHeader[9].toInt() and 0xFF
      }

      val frameStart = raf.filePointer
      if (frameSize <= 0 || frameStart + frameSize > tagEnd) break

      if ((frameId == "APIC" || frameId == "PIC") && frameSize in 16..(16 * 1024 * 1024)) {
        var payload = ByteArray(frameSize)
        raf.readFully(payload)
        var offset = 0
        if (majorVersion == 4 && (frameFlags2 and 0x01) != 0 && payload.size > 4) {
          offset = 4
        }
        val unsync = (tagFlags and 0x80) != 0 || (majorVersion == 4 && (frameFlags2 and 0x02) != 0)
        if (unsync) {
          val out = java.io.ByteArrayOutputStream(payload.size)
          var i = offset
          while (i < payload.size) {
            val b = payload[i].toInt() and 0xFF
            out.write(b)
            if (b == 0xFF && i + 1 < payload.size && payload[i + 1].toInt() == 0x00) {
              i += 2
            } else {
              i += 1
            }
          }
          payload = out.toByteArray()
          offset = 0
        }
        val parsed = parseId3PictureFramePayload(payload, offset, isV22 = (majorVersion == 2))
        if (parsed != null) {
          val (picType, imgBytes) = parsed
          if (picType == 3) {
            return imgBytes
          }
          if (fallbackPicture == null) {
            fallbackPicture = imgBytes
          }
        }
      }
      raf.seek(frameStart + frameSize)
    }
    return fallbackPicture
  }

  private fun parseId3PictureFramePayload(
    payload: ByteArray,
    startOffset: Int,
    isV22: Boolean,
  ): Pair<Int, ByteArray>? {
    if (startOffset + 6 >= payload.size) return null
    val encoding = payload[startOffset].toInt() and 0xFF
    var idx = startOffset + 1
    if (isV22) {
      idx += 3 // 3-byte image format ("JPG", "PNG")
    } else {
      while (idx < payload.size && payload[idx].toInt() != 0) {
        idx++
      }
      idx++ // skip null terminator of MIME type
    }
    if (idx >= payload.size) return null
    val picType = payload[idx].toInt() and 0xFF
    idx++

    // Skip null-terminated description string according to text encoding
    if (encoding == 1 || encoding == 2) {
      while (idx + 1 < payload.size) {
        if (payload[idx].toInt() == 0 && payload[idx + 1].toInt() == 0) {
          idx += 2
          break
        }
        idx += 2
      }
    } else {
      while (idx < payload.size && payload[idx].toInt() != 0) {
        idx++
      }
      idx++
    }

    // Align to image header if extra padding zeros exist
    while (idx < payload.size && payload[idx].toInt() == 0) {
      idx++
    }
    if (idx + 4 <= payload.size) {
      return picType to payload.copyOfRange(idx, payload.size)
    }
    return null
  }

  private fun parseFlacPictureBlock(payload: ByteArray): ByteArray? {
    if (payload.size < 32) return null
    val bb = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN)
    bb.int // picture type
    val mimeLen = bb.int
    if (mimeLen < 0 || bb.position() + mimeLen + 4 > payload.size) return null
    bb.position(bb.position() + mimeLen)
    val descLen = bb.int
    if (descLen < 0 || bb.position() + descLen + 20 > payload.size) return null
    bb.position(bb.position() + descLen + 16) // skip desc + width/height/depth/colors
    val dataLen = bb.int
    val pos = bb.position()
    if (dataLen <= 0 || pos + dataLen > payload.size) return null
    return payload.copyOfRange(pos, pos + dataLen)
  }

  private fun extractMp4CovrAtom(
    raf: RandomAccessFile,
    start: Long,
    end: Long,
    depth: Int,
  ): ByteArray? {
    if (depth > 8) return null
    val containerTypes = setOf("moov", "udta", "meta", "ilst", "covr")
    var pos = start
    while (pos + 8L <= end) {
      raf.seek(pos)
      val hdr = ByteArray(8)
      raf.readFully(hdr)
      var boxSize = ByteBuffer.wrap(hdr, 0, 4).order(ByteOrder.BIG_ENDIAN).int.toLong() and 0xFFFFFFFFL
      val boxType = String(hdr, 4, 4, Charsets.US_ASCII)
      var headerLen = 8L
      if (boxSize == 1L && pos + 16L <= end) {
        val ext = ByteArray(8)
        raf.readFully(ext)
        boxSize = ByteBuffer.wrap(ext).order(ByteOrder.BIG_ENDIAN).long
        headerLen = 16L
      } else if (boxSize == 0L) {
        boxSize = end - pos
      }
      if (boxSize < headerLen || pos + boxSize > end) break
      val boxEnd = pos + boxSize
      if (boxType == "data") {
        val payloadStart = pos + headerLen + 8L // skip 4-byte type/indicator + 4-byte locale
        val payloadLen = (boxEnd - payloadStart).toInt()
        if (payloadLen in 16..(16 * 1024 * 1024)) {
          raf.seek(payloadStart)
          val bytes = ByteArray(payloadLen)
          raf.readFully(bytes)
          return bytes
        }
      } else if (boxType in containerTypes) {
        val childStart = pos + headerLen + (if (boxType == "meta") 4L else 0L)
        if (childStart < boxEnd) {
          extractMp4CovrAtom(raf, childStart, boxEnd, depth + 1)?.let { return it }
        }
      }
      pos = boxEnd
    }
    return null
  }

  private fun findSiblingFolderCoverFile(parent: File): File? {
    if (!parent.exists() || !parent.isDirectory) return null
    val preferredStems = listOf(
      "cover",
      "folder",
      "album",
      "front",
      "art",
      "artwork",
      "albumart",
      "albumartsmall",
      "thumb",
    )
    val imageExtensions = setOf("jpg", "jpeg", "png", "webp")
    val children = try {
      parent.listFiles()
    } catch (_: Throwable) {
      null
    } ?: return null

    val imageFiles = children.filter { child ->
      child.isFile &&
        !child.name.startsWith(".") &&
        child.extension.lowercase(Locale.ROOT) in imageExtensions
    }
    if (imageFiles.isEmpty()) return null

    for (stem in preferredStems) {
      imageFiles.firstOrNull { it.nameWithoutExtension.equals(stem, ignoreCase = true) }?.let { return it }
    }
    for (stem in preferredStems) {
      imageFiles.firstOrNull { it.nameWithoutExtension.lowercase(Locale.ROOT).startsWith(stem) }?.let { return it }
    }
    val parentName = parent.name.trim()
    val isGenericParent = DEFAULT_MUSIC_FOLDER_NAMES.any { it.equals(parentName, ignoreCase = true) } ||
      parentName.equals("Downloads", ignoreCase = true) ||
      parentName.equals("Download", ignoreCase = true)
    if (!isGenericParent && imageFiles.size == 1) {
      return imageFiles.first()
    }
    return null
  }

  private fun diskCacheFileFor(identity: String, sizePx: Int): File? {
    val dir = artworkDiskCacheDir ?: return null
    val bucketSize = if (sizePx <= 256) 240 else 360
    val hash = identity.hashCode().toUInt().toString(16)
    val lengthTag = identity.length
    return File(dir, "art_v2_${hash}_${lengthTag}_$bucketSize.png")
  }

  private fun readDiskCachedAlbumArtBitmap(identity: String, sizePx: Int): Bitmap? {
    val cacheFile = diskCacheFileFor(identity, sizePx) ?: return null
    if (!cacheFile.exists() || !cacheFile.isFile || cacheFile.length() <= 0L) return null
    return try {
      val opts = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.ARGB_8888
      }
      BitmapFactory.decodeFile(cacheFile.absolutePath, opts)
    } catch (_: Throwable) {
      null
    }
  }

  private fun writeDiskCachedAlbumArtBitmap(identity: String, sizePx: Int, bitmap: Bitmap) {
    val cacheFile = diskCacheFileFor(identity, sizePx) ?: return
    if (cacheFile.exists() && cacheFile.length() > 0L) return
    try {
      cacheFile.parentFile?.mkdirs()
      val tmpFile = File(cacheFile.parentFile, "${cacheFile.name}.tmp")
      FileOutputStream(tmpFile).use { fos ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
        fos.flush()
      }
      if (tmpFile.exists() && tmpFile.length() > 0L) {
        tmpFile.renameTo(cacheFile)
      } else {
        tmpFile.delete()
      }
    } catch (_: Throwable) {}
  }

  /**
   * Loads or generates the album art [Bitmap] for [track] on a background thread,
   * also updating the cached [AlbumArtPalette] from the decoded bitmap if embedded art was found.
   */
  suspend fun loadAlbumArtBitmapAsync(
    track: MusicTrack,
    sizePx: Int = 320,
  ): Bitmap = withContext(artworkDispatcher) {
    ensureActive()
    getCachedAlbumArtBitmap(track, sizePx)?.let { cached ->
      if (!track.isSampleTrack &&
        lookupCachedExtractedPalette(track) == null &&
        !isCachedBitmapProcedural(track, cached, sizePx)
      ) {
        val extracted = extractPaletteFromBitmap(cached)
        recordExtractedPaletteForTrack(track, extracted)
      }
      return@withContext cached
    }
    generateOrDecodeAlbumArtBitmap(track, sizePx)
  }

  /**
   * Loads the [AlbumArtPalette] for [track] on a background thread after ensuring its album art
   * bitmap has been decoded or generated.
   */
  suspend fun loadTrackPaletteAsync(
    track: MusicTrack,
    sizePx: Int = 360,
  ): AlbumArtPalette = withContext(artworkDispatcher) {
    ensureActive()
    if (track.isSampleTrack) {
      return@withContext resolveTrackPalette(track)
    }
    lookupCachedExtractedPalette(track)?.let { cachedPalette ->
      if (getCachedAlbumArtBitmap(track, sizePx) != null) {
        return@withContext cachedPalette
      }
    }
    val bmp = generateOrDecodeAlbumArtBitmap(track, sizePx)
    lookupCachedExtractedPalette(track)?.let { return@withContext it }
    if (!isCachedBitmapProcedural(track, bmp, sizePx)) {
      val extracted = extractPaletteFromBitmap(bmp)
      recordExtractedPaletteForTrack(track, extracted)
      return@withContext extracted
    }
    resolveTrackPalette(track)
  }

  /**
   * Resolves a [track]'s album art and dynamic palette in the background on [Dispatchers.IO],
   * returning an updated [MusicTrack] with extracted RGB colors when embedded or folder cover art is present.
   */
  suspend fun resolveTrackAlbumArtAsync(
    track: MusicTrack,
    sizePx: Int = 320,
  ): MusicTrack = withContext(artworkDispatcher) {
    ensureActive()
    generateOrDecodeAlbumArtBitmap(track, sizePx)
    val extractedPalette = lookupCachedExtractedPalette(track) ?: return@withContext track
    val updated = applyPaletteToTrack(track, extractedPalette)
    val file = track.file
    if (file != null) {
      val metaKeyTrue = "${file.absolutePath}:${file.lastModified()}:${file.length()}:true"
      synchronized(metadataCacheLock) {
        trackMetadataCache[metaKeyTrue] = updated
      }
    }
    updated
  }

  /**
   * Prefetches and caches album art bitmaps in the background on [Dispatchers.IO] for unique
   * albums across [tracks] so scrolling through the library hits the in-memory cache immediately.
   */
  suspend fun prefetchAlbumArt(
    tracks: List<MusicTrack>,
    sizePx: Int = 240,
  ) = withContext(artworkDispatcher) {
    val seenIdentities = HashSet<String>()
    for (track in tracks) {
      ensureActive()
      val identity = albumArtIdentityKey(track)
      if (!seenIdentities.add(identity)) {
        lookupCachedExtractedPalette(track)?.let { palette ->
          recordExtractedPaletteForTrack(track, palette)
        }
        continue
      }
      if (getCachedAlbumArtBitmap(track, sizePx) == null || lookupCachedExtractedPalette(track) == null) {
        generateOrDecodeAlbumArtBitmap(track, sizePx)
      }
    }
  }

  /**
   * Decodes embedded or folder album art for [track], or procedurally renders a stylized
   * high-contrast album cover [Bitmap] using [track]'s album art colors.
   */
  fun generateOrDecodeAlbumArtBitmap(
    track: MusicTrack,
    sizePx: Int = 320,
  ): Bitmap {
    getCachedAlbumArtBitmap(track, sizePx)?.let { cached ->
      if (!track.isSampleTrack &&
        lookupCachedExtractedPalette(track) == null &&
        !isCachedBitmapProcedural(track, cached, sizePx)
      ) {
        val extracted = extractPaletteFromBitmap(cached)
        recordExtractedPaletteForTrack(track, extracted)
      }
      return cached
    }

    val safeSize = sizePx.coerceIn(64, 1024)
    val identity = albumArtIdentityKey(track)
    val decodeLock = artworkDecodeLocks[(identity.hashCode() and 0x7FFFFFFF) % artworkDecodeLocks.size]

    synchronized(decodeLock) {
      getCachedAlbumArtBitmap(track, safeSize)?.let { cached ->
        if (!track.isSampleTrack &&
          lookupCachedExtractedPalette(track) == null &&
          !isCachedBitmapProcedural(track, cached, safeSize)
        ) {
          val extracted = extractPaletteFromBitmap(cached)
          recordExtractedPaletteForTrack(track, extracted)
        }
        return cached
      }

      val file = track.file
      if (file != null && file.exists() && !track.isSampleTrack) {
        // 0. Check disk cache if configured
        val diskCached = readDiskCachedAlbumArtBitmap(identity, safeSize)
        if (diskCached != null) {
          val extractedPalette = extractPaletteFromBitmap(diskCached)
          cacheAlbumArtBitmap(track, safeSize, diskCached, extractedPalette, isProcedural = false)
          return diskCached
        }

        val noArtFileKey = "${file.absolutePath}:${file.lastModified()}:${file.length()}"
        val knownNoExternalArt = synchronized(artworkCacheLock) {
          noExternalArtworkCache[noArtFileKey] == true
        }

        if (!knownNoExternalArt) {
          // 1a. Check embedded ID3 / MP4 / FLAC cover art via MediaMetadataRetriever
          val retriever = try {
            MediaMetadataRetriever()
          } catch (_: Throwable) {
            null
          }
          if (retriever != null) {
            try {
              retriever.setDataSource(file.absolutePath)
              val embeddedBytes = retriever.embeddedPicture
              if (embeddedBytes != null && embeddedBytes.isNotEmpty()) {
                val decoded = decodeDownsampledBitmap(embeddedBytes, reqSizePx = safeSize)
                if (decoded != null) {
                  val extractedPalette = extractPaletteFromBitmap(decoded)
                  cacheAlbumArtBitmap(track, safeSize, decoded, extractedPalette, isProcedural = false)
                  writeDiskCachedAlbumArtBitmap(identity, safeSize, decoded)
                  return decoded
                }
              }
            } catch (_: Throwable) {
              // Fall through to pure-Kotlin embedded parser, folder art, or MediaStore
            } finally {
              try {
                retriever.release()
              } catch (_: Throwable) {}
            }
          }

          // 1b. Pure-Kotlin embedded artwork extraction fallback (ID3v2 APIC/PIC, WAV ID3, FLAC Picture, MP4 covr)
          val rawEmbeddedBytes = extractEmbeddedArtworkBytes(file)
          if (rawEmbeddedBytes != null && rawEmbeddedBytes.isNotEmpty()) {
            val decoded = decodeDownsampledBitmap(rawEmbeddedBytes, reqSizePx = safeSize)
            if (decoded != null) {
              val extractedPalette = extractPaletteFromBitmap(decoded)
              cacheAlbumArtBitmap(track, safeSize, decoded, extractedPalette, isProcedural = false)
              writeDiskCachedAlbumArtBitmap(identity, safeSize, decoded)
              return decoded
            }
          }

          // 2. Check sibling cover / folder / album art in parent folder (case-insensitive)
          val parent = file.parentFile
          if (parent != null && parent.isDirectory) {
            val coverFile = findSiblingFolderCoverFile(parent)
            if (coverFile != null && coverFile.exists() && coverFile.isFile) {
              val decoded = GalleryEngine.decodeThumbnailBitmap(coverFile, reqSizePx = safeSize)
              if (decoded != null) {
                val extractedPalette = extractPaletteFromBitmap(decoded)
                cacheAlbumArtBitmap(track, safeSize, decoded, extractedPalette, isProcedural = false)
                writeDiskCachedAlbumArtBitmap(identity, safeSize, decoded)
                return decoded
              }
            }
          }

          // 3. Check Android MediaStore album art if runtime context is available
          val runtimeCtx = monitoredAppContext
          if (runtimeCtx != null) {
            val mediaStoreBmp = queryMediaStoreArtworkForFile(
              context = runtimeCtx,
              file = file,
              title = track.title,
              album = track.album,
              reqSizePx = safeSize,
            )
            if (mediaStoreBmp != null) {
              val extractedPalette = extractPaletteFromBitmap(mediaStoreBmp)
              cacheAlbumArtBitmap(track, safeSize, mediaStoreBmp, extractedPalette, isProcedural = false)
              writeDiskCachedAlbumArtBitmap(identity, safeSize, mediaStoreBmp)
              return mediaStoreBmp
            }
          }

          synchronized(artworkCacheLock) {
            noExternalArtworkCache[noArtFileKey] = true
          }
        }
      }

      // 4. Render procedural album cover Bitmap (only when no embedded/folder/MediaStore art exists)
      val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val sizeF = safeSize.toFloat()

      val primaryArgb = (0xFF000000 or (track.artworkPrimaryRgb.toLong() and 0xFFFFFFL)).toInt()
      val secondaryArgb = (0xFF000000 or (track.artworkSecondaryRgb.toLong() and 0xFFFFFFL)).toInt()
      val accentArgb = (0xFF000000 or (track.artworkAccentRgb.toLong() and 0xFFFFFFL)).toInt()

      // Diagonal gradient background
      val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
          0f,
          0f,
          sizeF,
          sizeF,
          intArrayOf(primaryArgb, secondaryArgb),
          floatArrayOf(0f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawRect(0f, 0f, sizeF, sizeF, bgPaint)

      // Ambient accent radial glow in upper-right
      val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = RadialGradient(
          sizeF * 0.78f,
          sizeF * 0.24f,
          sizeF * 0.55f,
          intArrayOf(
            (accentArgb and 0x00FFFFFF) or 0xAA000000.toInt(),
            (accentArgb and 0x00FFFFFF),
          ),
          floatArrayOf(0f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawRect(0f, 0f, sizeF, sizeF, glowPaint)

      // Concentric vinyl / sonic rings
      val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = max(2f, sizeF * 0.012f)
        color = 0x38FFFFFF
      }
      val cx = sizeF * 0.5f
      val cy = sizeF * 0.5f
      canvas.drawCircle(cx, cy, sizeF * 0.36f, ringPaint)
      canvas.drawCircle(cx, cy, sizeF * 0.27f, ringPaint)
      canvas.drawCircle(cx, cy, sizeF * 0.18f, ringPaint)

      // Center record label disc
      val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = accentArgb
      }
      canvas.drawCircle(cx, cy, sizeF * 0.11f, discPaint)

      val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = secondaryArgb
      }
      canvas.drawCircle(cx, cy, sizeF * 0.035f, holePaint)

      // Decorative waveform bars along bottom
      val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x66FFFFFF
      }
      val barCount = 14
      val totalBarWidth = sizeF * 0.64f
      val startX = (sizeF - totalBarWidth) / 2f
      val slotW = totalBarWidth / barCount
      val barW = slotW * 0.52f
      val seed = track.album.hashCode() xor track.artist.hashCode()
      for (i in 0 until barCount) {
        val wave = abs(sin((i * 0.65) + (seed and 0xFF))).toFloat()
        val barH = sizeF * (0.04f + wave * 0.10f)
        val left = startX + i * slotW
        val top = sizeF * 0.86f - barH
        canvas.drawRoundRect(
          RectF(left, top, left + barW, sizeF * 0.86f),
          barW / 2f,
          barW / 2f,
          barPaint,
        )
      }

      cacheAlbumArtBitmap(track, safeSize, bitmap, extractedPalette = null, isProcedural = true)
      return bitmap
    }
  }

  @Suppress("DEPRECATION")
  private fun queryMediaStoreArtworkForFile(
    context: Context,
    file: File,
    title: String,
    album: String,
    reqSizePx: Int = 360,
  ): Bitmap? {
    return try {
      val resolver = context.contentResolver
      val projection = arrayOf(MediaStore.Audio.Media.ALBUM_ID)
      // 1. Match by exact file path (_DATA)
      resolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Audio.Media.DATA} = ?",
        arrayOf(file.absolutePath),
        null,
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          val foundAlbumId = cursor.getLong(0)
          if (foundAlbumId > 0L) {
            val artUri = Uri.parse("content://media/external/audio/albumart/$foundAlbumId")
            loadArtworkBitmapFromUri(context, artUri.toString(), reqSizePx = reqSizePx)?.let { return it }
          }
        }
      }
      // 2. Fallback to title / non-generic album match
      val isGenericAlbum = album.isBlank() ||
        album.equals("Local Recordings", ignoreCase = true) ||
        album.equals("Unknown Album", ignoreCase = true) ||
        album.equals("Download", ignoreCase = true) ||
        album.equals("Downloads", ignoreCase = true) ||
        isSingleFolderAlbumName(album)
      if (title.isNotBlank() && !isGenericAlbum) {
        resolver.query(
          MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
          projection,
          "${MediaStore.Audio.Media.TITLE} = ? AND ${MediaStore.Audio.Media.ALBUM} = ?",
          arrayOf(title, album),
          null,
        )?.use { cursor ->
          if (cursor.moveToFirst()) {
            val foundAlbumId = cursor.getLong(0)
            if (foundAlbumId > 0L) {
              val artUri = Uri.parse("content://media/external/audio/albumart/$foundAlbumId")
              loadArtworkBitmapFromUri(context, artUri.toString(), reqSizePx = reqSizePx)?.let { return it }
            }
          }
        }
      }
      null
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Returns a cached [MusicTrack] for [file] if its path, timestamp, and byte length match.
   */
  fun getCachedTrackMetadata(
    file: File,
    requireInspectedMetadata: Boolean = false,
  ): MusicTrack? {
    val baseKey = "${file.absolutePath}:${file.lastModified()}:${file.length()}"
    val cached = synchronized(metadataCacheLock) {
      trackMetadataCache["$baseKey:true"]
        ?: if (!requireInspectedMetadata) trackMetadataCache["$baseKey:false"] else null
    } ?: return null
    val extracted = lookupCachedExtractedPalette(cached, file)
    return if (extracted != null) applyPaletteToTrack(cached, extracted) else cached
  }

  /**
   * Parses track metadata from a local audio [file].
   * Uses `MediaMetadataRetriever` when available on Android runtime, falling back to intelligent
   * `Artist - Title` filename and parent folder parsing in unit tests or untagged files.
   *
   * When [deferAlbumArt] is `true` (the default), embedded picture decoding is skipped during
   * track scanning so large music collections load quickly and decode artwork asynchronously in
   * the background via [loadAlbumArtBitmapAsync] / [prefetchAlbumArt].
   */
  fun buildTrackFromFile(
    file: File,
    inspectMetadataRetriever: Boolean = true,
    deferAlbumArt: Boolean = true,
  ): MusicTrack {
    val fileLastMod = file.lastModified()
    val fileLength = file.length()
    val cacheKeyBase = "${file.absolutePath}:$fileLastMod:$fileLength"
    val singleFolderAlbum = resolveSingleFolderAlbumName(file)

    fun withResolvedAlbumAndCachedPalette(candidate: MusicTrack): MusicTrack {
      val albumAdjusted = if (singleFolderAlbum != null && candidate.album != singleFolderAlbum) {
        candidate.copy(album = singleFolderAlbum)
      } else {
        candidate
      }
      val cachedPalette = lookupCachedExtractedPalette(albumAdjusted, file)
      return if (cachedPalette != null) {
        applyPaletteToTrack(albumAdjusted, cachedPalette)
      } else {
        albumAdjusted
      }
    }

    synchronized(metadataCacheLock) {
      trackMetadataCache["$cacheKeyBase:true"]?.let { cached ->
        if (deferAlbumArt) {
          return withResolvedAlbumAndCachedPalette(cached)
        }
      }
      trackIndexByPath[file.absolutePath]?.let { indexed ->
        if (indexed.lastModifiedMs == fileLastMod && indexed.sizeBytes == fileLength && deferAlbumArt) {
          return withResolvedAlbumAndCachedPalette(indexed)
        }
      }
      if (!inspectMetadataRetriever) {
        trackMetadataCache["$cacheKeyBase:false"]?.let { cached ->
          return withResolvedAlbumAndCachedPalette(cached)
        }
      }
    }

    val parentName = file.parentFile?.name?.trim().orEmpty()
    val isParentTopDefaultFolder = DEFAULT_MUSIC_FOLDER_NAMES.any { it.equals(parentName, ignoreCase = true) } ||
      parentName.equals("Downloads", ignoreCase = true)
    val stem = file.nameWithoutExtension.trim()
    val fallbackArtist: String
    val fallbackTitle: String
    if (stem.contains(" - ")) {
      val parts = stem.split(" - ", limit = 2)
      fallbackArtist = parts[0].trim().ifEmpty { "Unknown Artist" }
      fallbackTitle = parts[1].trim().ifEmpty { stem }
    } else if (singleFolderAlbum != null || isParentTopDefaultFolder) {
      fallbackArtist = "Local Artist"
      fallbackTitle = stem.replace('_', ' ').ifEmpty { file.name }
    } else {
      fallbackArtist = file.parentFile?.parentFile?.name
        ?.takeIf {
          it.isNotBlank() &&
            !it.equals("Music", ignoreCase = true) &&
            !it.equals("Downloads", ignoreCase = true) &&
            !it.equals("Download", ignoreCase = true) &&
            !it.equals("0", ignoreCase = true)
        }
        ?: "Local Artist"
      fallbackTitle = stem.replace('_', ' ').ifEmpty { file.name }
    }

    val fallbackAlbum = when {
      singleFolderAlbum != null -> singleFolderAlbum
      parentName.equals("Downloads", ignoreCase = true) || parentName.equals("Download", ignoreCase = true) -> "Download"
      parentName.isNotEmpty() &&
        !parentName.equals("Music", ignoreCase = true) &&
        !parentName.equals("0", ignoreCase = true) -> parentName
      else -> "Local Recordings"
    }

    var title = fallbackTitle
    var artist = fallbackArtist
    var album = fallbackAlbum
    var genre = "Local Audio"
    var durationMs = AudioRecorderEngine.readWavDurationMs(file)?.takeIf { it > 0L } ?: 180_000L
    var trackNumber = 1
    var year = 2026
    var bitrateKbps = 320
    var sampleRateHz = 44_100
    var extractedPaletteFromFile: AlbumArtPalette? = null

    val embeddedTags = AudioEditorEngine.readEmbeddedFileMetadata(file)
    if (embeddedTags.title.isNotBlank()) title = embeddedTags.title
    if (embeddedTags.artist.isNotBlank()) artist = embeddedTags.artist
    if (singleFolderAlbum == null && embeddedTags.album.isNotBlank()) album = embeddedTags.album
    if (embeddedTags.genre.isNotBlank()) genre = embeddedTags.genre
    embeddedTags.trackNumber.substringBefore('/').trim().toIntOrNull()?.takeIf { it > 0 }?.let { trackNumber = it }
    embeddedTags.year.trim().take(4).toIntOrNull()?.takeIf { it in 1900..2100 }?.let { year = it }

    if (inspectMetadataRetriever) {
      val retriever = try {
        MediaMetadataRetriever()
      } catch (_: Throwable) {
        null
      }
      if (retriever != null) {
        try {
          retriever.setDataSource(file.absolutePath)
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            ?.trim()?.takeIf { it.isNotEmpty() }?.let { title = it }
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            ?.trim()?.takeIf { it.isNotEmpty() }?.let { artist = it }
          if (singleFolderAlbum == null) {
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
              ?.trim()?.takeIf { it.isNotEmpty() }?.let { album = it }
          }
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            ?.trim()?.takeIf { it.isNotEmpty() }?.let { genre = it }
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.trim()?.toLongOrNull()?.takeIf { it > 0L }?.let { durationMs = it }
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
            ?.substringBefore('/')?.trim()?.toIntOrNull()?.takeIf { it > 0 }?.let { trackNumber = it }
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            ?.trim()?.take(4)?.toIntOrNull()?.takeIf { it in 1900..2100 }?.let { year = it }
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            ?.trim()?.toLongOrNull()?.takeIf { it > 0L }?.let { bitrateKbps = (it / 1000L).toInt().coerceAtLeast(32) }
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
            ?.trim()?.toIntOrNull()?.takeIf { it > 0 }?.let { sampleRateHz = it }

          if (!deferAlbumArt) {
            val pic = retriever.embeddedPicture
            if (pic != null && pic.isNotEmpty()) {
              val bmp = decodeDownsampledBitmap(pic, reqSizePx = 320)
              if (bmp != null) {
                extractedPaletteFromFile = extractPaletteFromBitmap(bmp)
              }
            }
          }
        } catch (_: Throwable) {
          // Ignore and keep fallback metadata
        } finally {
          try {
            retriever.release()
          } catch (_: Throwable) {}
        }
      }
    }

    if (!deferAlbumArt && extractedPaletteFromFile == null) {
      val rawBytes = extractEmbeddedArtworkBytes(file)
      if (rawBytes != null && rawBytes.isNotEmpty()) {
        val bmp = decodeDownsampledBitmap(rawBytes, reqSizePx = 320)
        if (bmp != null) {
          extractedPaletteFromFile = extractPaletteFromBitmap(bmp)
        }
      }
      if (extractedPaletteFromFile == null) {
        val parent = file.parentFile
        val coverFile = if (parent != null) findSiblingFolderCoverFile(parent) else null
        if (coverFile != null) {
          val bmp = GalleryEngine.decodeThumbnailBitmap(coverFile, reqSizePx = 320)
          if (bmp != null) {
            extractedPaletteFromFile = extractPaletteFromBitmap(bmp)
          }
        }
      }
    }

    if (singleFolderAlbum != null) {
      album = singleFolderAlbum
    }

    val extractedColors = extractedPaletteFromFile?.let { paletteToRgbTriple(it) }
    val (primaryRgb, secondaryRgb, accentRgb) = extractedColors
      ?: computeDeterministicArtworkColors(
        album = album,
        artist = singleFolderAlbum ?: artist,
        genre = genre,
      )

    val track = MusicTrack(
      id = "file_${file.absolutePath}",
      title = title,
      artist = artist,
      album = album,
      genre = genre,
      durationMs = durationMs,
      trackNumber = trackNumber,
      year = year,
      file = file,
      sizeBytes = fileLength,
      lastModifiedMs = fileLastMod,
      artworkPrimaryRgb = primaryRgb,
      artworkSecondaryRgb = secondaryRgb,
      artworkAccentRgb = accentRgb,
      isSampleTrack = false,
      bitrateKbps = bitrateKbps,
      sampleRateHz = sampleRateHz,
    )

    if (extractedPaletteFromFile != null) {
      recordExtractedPaletteForTrack(track, extractedPaletteFromFile)
    }

    // Reuse already-cached album or file palette colors if background artwork extraction already ran
    val cachedAlbumPalette = lookupCachedExtractedPalette(track, file)
    val finalTrack = if (cachedAlbumPalette != null && extractedColors == null) {
      applyPaletteToTrack(track, cachedAlbumPalette)
    } else {
      track
    }

    synchronized(metadataCacheLock) {
      trackMetadataCache["$cacheKeyBase:$inspectMetadataRetriever"] = finalTrack
      if (inspectMetadataRetriever) {
        trackIndexByPath[file.absolutePath] = finalTrack
        trackMetadataCache["$cacheKeyBase:false"] = finalTrack
      }
    }
    return finalTrack
  }

  /**
   * Adjusts the embedded audio metadata tags (`title`, `artist`, `album`, `genre`, `year`,
   * `trackNumber`) on [file] to match [track], preserving any existing comment or cover art.
   * Returns `true` if the file exists and its metadata tags were updated on disk.
   */
  fun updateTrackFileMetadata(
    track: MusicTrack,
    file: File? = track.file,
  ): Boolean {
    val targetFile = file ?: return false
    if (!targetFile.exists() || !targetFile.isFile) return false
    val existingTags = AudioEditorEngine.readAudioMetadata(targetFile)
    val updatedTags = existingTags.copy(
      title = track.title.ifBlank { existingTags.title },
      artist = track.artist.ifBlank { existingTags.artist },
      album = track.album.ifBlank { existingTags.album },
      genre = track.genre.ifBlank { existingTags.genre },
      year = if (track.year > 0) track.year.toString() else existingTags.year,
      trackNumber = if (track.trackNumber > 0) track.trackNumber.toString() else existingTags.trackNumber,
    ).sanitized()
    val written = AudioEditorEngine.writeAudioMetadata(targetFile, updatedTags)
    if (written) {
      val refreshedTrack = track.copy(
        file = targetFile,
        lastModifiedMs = targetFile.lastModified(),
        sizeBytes = targetFile.length(),
      )
      putCachedTrackIndex(refreshedTrack)
    }
    return written
  }

  /**
   * Updates embedded audio metadata tags across all [tracks] that have local files on a
   * background thread ([Dispatchers.IO]), returning the number of files updated on disk.
   */
  suspend fun updateTracksFileMetadataAsync(
    tracks: Collection<MusicTrack>,
  ): Int = withContext(Dispatchers.IO) {
    var updatedFiles = 0
    for (track in tracks) {
      ensureActive()
      val file = track.file ?: continue
      if (file.exists() && updateTrackFileMetadata(track, file)) {
        updatedFiles++
      }
    }
    updatedFiles
  }

  /**
   * Scans [directories] recursively up to [maxDepth] for supported music files.
   */
  fun scanMusicDirectories(
    directories: List<File>,
    maxDepth: Int = 5,
    inspectMetadataRetriever: Boolean = true,
    deferAlbumArt: Boolean = true,
  ): List<MusicTrack> {
    val foundFiles = LinkedHashMap<String, File>()

    fun walk(dir: File, depth: Int) {
      if (depth > maxDepth || !dir.exists() || !dir.isDirectory) return
      val children = try {
        dir.listFiles()
      } catch (_: Throwable) {
        null
      } ?: return

      for (child in children) {
        if (child.name.startsWith(".")) continue
        if (child.isFile && isMusicFile(child)) {
          foundFiles.putIfAbsent(child.absolutePath, child)
        } else if (child.isDirectory && depth < maxDepth) {
          walk(child, depth + 1)
        }
      }
    }

    for (dir in directories) {
      walk(dir, 0)
    }

    return foundFiles.values
      .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
      .map {
        buildTrackFromFile(
          file = it,
          inspectMetadataRetriever = inspectMetadataRetriever,
          deferAlbumArt = deferAlbumArt,
        )
      }
  }

  private fun libraryCacheKey(
    rootDirectory: File,
    initialDirectory: File?,
    includeSamples: Boolean,
    additionalDirectories: List<File> = emptyList(),
    hasFullFilesPermission: Boolean = true,
  ): String {
    val targetDirs = if (initialDirectory != null) {
      listOf(initialDirectory) + additionalDirectories
    } else {
      resolveDefaultMusicDirectories(rootDirectory, hasFullFilesPermission) + additionalDirectories
    }.distinctBy { it.absolutePath }
    val dirStamp = targetDirs.joinToString("|") { dir ->
      val mod = if (dir.exists()) dir.lastModified() else -1L
      "${dir.absolutePath}@$mod"
    }
    return "$dirStamp|samples=$includeSamples|fullFiles=$hasFullFilesPermission"
  }

  /**
   * Returns the previously loaded library tracks from the in-memory cache if available and
   * top-level music directories have not changed, or `null` if not yet cached.
   */
  fun getCachedLibraryTracks(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    initialDirectory: File? = null,
    includeSamples: Boolean = false,
    additionalDirectories: List<File> = emptyList(),
    hasFullFilesPermission: Boolean = true,
  ): List<MusicTrack>? {
    val key = libraryCacheKey(rootDirectory, initialDirectory, includeSamples, additionalDirectories, hasFullFilesPermission)
    synchronized(metadataCacheLock) {
      return libraryTracksCache[key]
    }
  }

  /**
   * Resolves the full library of tracks from [initialDirectory] (if provided) or the default
   * `/Music`, `/Audio`, `/Recordings` (and `/Download` when [hasFullFilesPermission] is `true`) folders
   * under [rootDirectory] plus any [additionalDirectories] (user-added custom folders), combining or
   * falling back to [DEFAULT_SAMPLE_TRACKS] when [includeSamples] is enabled.
   */
  fun loadLibraryTracks(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    initialDirectory: File? = null,
    includeSamples: Boolean = false,
    inspectMetadataRetriever: Boolean = true,
    deferAlbumArt: Boolean = true,
    additionalDirectories: List<File> = emptyList(),
    hasFullFilesPermission: Boolean = true,
  ): List<MusicTrack> {
    if (trackIndexDiskCacheFile != null) {
      loadTrackIndexCacheFromDisk(trackIndexDiskCacheFile)
    }
    val targetDirs = if (initialDirectory != null) {
      listOf(initialDirectory) + additionalDirectories
    } else {
      resolveDefaultMusicDirectories(rootDirectory, hasFullFilesPermission) + additionalDirectories
    }.distinctBy { it.absolutePath }
    val localTracks = scanMusicDirectories(
      directories = targetDirs,
      inspectMetadataRetriever = inspectMetadataRetriever,
      deferAlbumArt = deferAlbumArt,
    )
    val result = when {
      localTracks.isNotEmpty() && includeSamples && initialDirectory == null -> localTracks + DEFAULT_SAMPLE_TRACKS
      localTracks.isNotEmpty() -> localTracks
      includeSamples -> DEFAULT_SAMPLE_TRACKS
      else -> emptyList()
    }
    val key = libraryCacheKey(rootDirectory, initialDirectory, includeSamples, additionalDirectories, hasFullFilesPermission)
    synchronized(metadataCacheLock) {
      if (inspectMetadataRetriever || libraryTracksCache[key] == null) {
        libraryTracksCache[key] = result
      }
    }
    if (inspectMetadataRetriever && trackIndexDiskCacheFile != null && localTracks.isNotEmpty()) {
      saveTrackIndexCacheToDisk(trackIndexDiskCacheFile, localTracks)
    }
    return result
  }

  /**
   * Asynchronously scans and loads the music library on a background thread ([Dispatchers.IO]),
   * deferring album art decoding so large music collections load without blocking the UI thread.
   */
  suspend fun loadLibraryTracksAsync(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    initialDirectory: File? = null,
    includeSamples: Boolean = false,
    inspectMetadataRetriever: Boolean = true,
    deferAlbumArt: Boolean = true,
    additionalDirectories: List<File> = emptyList(),
    hasFullFilesPermission: Boolean = true,
  ): List<MusicTrack> = withContext(Dispatchers.IO) {
    ensureActive()
    loadLibraryTracks(
      rootDirectory = rootDirectory,
      initialDirectory = initialDirectory,
      includeSamples = includeSamples,
      inspectMetadataRetriever = inspectMetadataRetriever,
      deferAlbumArt = deferAlbumArt,
      additionalDirectories = additionalDirectories,
      hasFullFilesPermission = hasFullFilesPermission,
    )
  }

  /**
   * Aggregates [tracks] into a sorted list of [MusicAlbum] items.
   * Tracks from the `Audio` and `Recordings` folders ([isSingleFolderAlbumName]) are each grouped
   * into a single unified album regardless of individual track artist differences.
   */
  fun buildAlbums(tracks: List<MusicTrack>): List<MusicAlbum> {
    if (tracks.isEmpty()) return emptyList()
    val grouped = LinkedHashMap<String, MutableList<MusicTrack>>()
    for (track in tracks) {
      val albumLower = track.album.trim().lowercase(Locale.ROOT)
      val key = if (isSingleFolderAlbumName(track.album)) {
        albumLower
      } else {
        "$albumLower|${track.artist.trim().lowercase(Locale.ROOT)}"
      }
      grouped.getOrPut(key) { mutableListOf() }.add(track)
    }
    return grouped.values.map { albumTracks ->
      val sortedTracks = albumTracks.sortedWith(
        compareBy<MusicTrack> { it.trackNumber }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
      )
      val first = sortedTracks.first()
      val distinctArtists = sortedTracks
        .map { it.artist.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase(Locale.ROOT) }
      val albumArtist = if (isSingleFolderAlbumName(first.album) && distinctArtists.size > 1) {
        "Various Artists"
      } else {
        first.artist
      }
      MusicAlbum(
        name = first.album,
        artist = albumArtist,
        year = first.year,
        genre = first.genre,
        tracks = sortedTracks,
      )
    }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
  }

  /**
   * Returns `true` if [album] matches the composite `name|artist` selection [key] (matching by
   * album name alone for single-folder albums like `"Audio"` and `"Recordings"`).
   */
  fun matchesAlbumKey(album: MusicAlbum, key: String): Boolean {
    if ("${album.name}|${album.artist}".equals(key, ignoreCase = true)) return true
    val keyAlbum = key.substringBefore("|").trim()
    if (isSingleFolderAlbumName(album.name) && album.name.equals(keyAlbum, ignoreCase = true)) {
      return true
    }
    return false
  }

  /**
   * Aggregates [tracks] into a sorted list of [MusicArtist] items.
   */
  fun buildArtists(tracks: List<MusicTrack>): List<MusicArtist> {
    if (tracks.isEmpty()) return emptyList()
    val grouped = LinkedHashMap<String, MutableList<MusicTrack>>()
    for (track in tracks) {
      val key = track.artist.trim().lowercase(Locale.ROOT)
      grouped.getOrPut(key) { mutableListOf() }.add(track)
    }
    return grouped.values.map { artistTracks ->
      val first = artistTracks.first()
      val albums = buildAlbums(artistTracks)
      val sortedTracks = artistTracks.sortedWith(
        compareBy<MusicTrack, String>(String.CASE_INSENSITIVE_ORDER) { it.album }
          .thenBy { it.trackNumber }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
      )
      MusicArtist(
        name = first.artist,
        albums = albums,
        tracks = sortedTracks,
      )
    }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
  }

  /**
   * Aggregates [tracks] into a sorted list of [MusicGenre] items.
   */
  fun buildGenres(tracks: List<MusicTrack>): List<MusicGenre> {
    if (tracks.isEmpty()) return emptyList()
    val grouped = LinkedHashMap<String, MutableList<MusicTrack>>()
    for (track in tracks) {
      val key = track.genre.trim().ifEmpty { "Unknown Genre" }.lowercase(Locale.ROOT)
      grouped.getOrPut(key) { mutableListOf() }.add(track)
    }
    return grouped.values.map { genreTracks ->
      val first = genreTracks.first()
      val albums = buildAlbums(genreTracks)
      val artists = genreTracks.map { it.artist }.distinctBy { it.lowercase(Locale.ROOT) }.sorted()
      val sortedTracks = genreTracks.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
      MusicGenre(
        name = first.genre.ifBlank { "Unknown Genre" },
        tracks = sortedTracks,
        albums = albums,
        artists = artists,
      )
    }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
  }

  /**
   * Filters [tracks] by matching [query] against title, artist, album, or genre.
   */
  fun filterTracks(tracks: List<MusicTrack>, query: String): List<MusicTrack> {
    val q = query.trim()
    if (q.isEmpty()) return tracks
    return tracks.filter { track ->
      track.title.contains(q, ignoreCase = true) ||
        track.artist.contains(q, ignoreCase = true) ||
        track.album.contains(q, ignoreCase = true) ||
        track.genre.contains(q, ignoreCase = true)
    }
  }

  /**
   * Filters [artists] by matching [query] against artist name, album names, or track titles.
   */
  fun filterArtists(artists: List<MusicArtist>, query: String): List<MusicArtist> {
    val q = query.trim()
    if (q.isEmpty()) return artists
    return artists.filter { artist ->
      artist.name.contains(q, ignoreCase = true) ||
        artist.albums.any { it.name.contains(q, ignoreCase = true) } ||
        artist.tracks.any { it.title.contains(q, ignoreCase = true) }
    }
  }

  /**
   * Filters [albums] by matching [query] against album name, artist name, genre, or track titles.
   */
  fun filterAlbums(albums: List<MusicAlbum>, query: String): List<MusicAlbum> {
    val q = query.trim()
    if (q.isEmpty()) return albums
    return albums.filter { album ->
      album.name.contains(q, ignoreCase = true) ||
        album.artist.contains(q, ignoreCase = true) ||
        album.genre.contains(q, ignoreCase = true) ||
        album.tracks.any { it.title.contains(q, ignoreCase = true) }
    }
  }

  /**
   * Filters [genres] by matching [query] against genre name, artists, or track titles.
   */
  fun filterGenres(genres: List<MusicGenre>, query: String): List<MusicGenre> {
    val q = query.trim()
    if (q.isEmpty()) return genres
    return genres.filter { genre ->
      genre.name.contains(q, ignoreCase = true) ||
        genre.artists.any { it.contains(q, ignoreCase = true) } ||
        genre.tracks.any { it.title.contains(q, ignoreCase = true) }
    }
  }

  /** Identifier of the built-in, always-present Favorites playlist. */
  const val FAVORITES_PLAYLIST_ID = "playlist_favorites"
  const val FAVORITES_PLAYLIST_NAME = "Favorites"

  /** Identifier of the built-in, always-present Most Played playlist. */
  const val MOST_PLAYED_PLAYLIST_ID = "playlist_most_played"
  const val MOST_PLAYED_PLAYLIST_NAME = "Most Played"

  /** Identifier of the built-in, always-present Recently Played playlist. */
  const val RECENTLY_PLAYED_PLAYLIST_ID = "playlist_recently_played"
  const val RECENTLY_PLAYED_PLAYLIST_NAME = "Recently Played"

  /** IDs of built-in playlists that are dynamically generated rather than persisted as user entries. */
  val BUILT_IN_PLAYLIST_IDS: Set<String> = setOf(
    FAVORITES_PLAYLIST_ID,
    MOST_PLAYED_PLAYLIST_ID,
    RECENTLY_PLAYED_PLAYLIST_ID,
  )

  /** IDs of the 6 generated Home playlists. */
  val HOME_PLAYLIST_IDS: Set<String> = (1..HOME_PLAYLIST_COUNT).map { "home_mix_$it" }.toSet()

  /**
   * Returns `true` if [playlistId] belongs to a protected default playlist (Favorites, Most Played,
   * Recently Played) or a generated Home playlist (`home_mix_*`), which cannot be deleted.
   */
  fun isProtectedPlaylistId(playlistId: String): Boolean {
    val trimmed = playlistId.trim()
    return trimmed in BUILT_IN_PLAYLIST_IDS ||
      trimmed in HOME_PLAYLIST_IDS ||
      trimmed.startsWith("home_mix_")
  }

  /** IDs of sample playlists shipped by older versions; stripped from persisted playlists on load. */
  val LEGACY_SAMPLE_PLAYLIST_IDS: Set<String> = setOf("playlist_synth_night", "playlist_chill_focus")

  /**
   * Builds the built-in Favorites [MusicPlaylist] from [favoriteIds], preserving library order.
   * Only favorited song IDs ([favoriteIds]) are included; favorited artists, albums, and genres
   * are intentionally excluded from the default Favorites playlist.
   * Always returned (even when empty) so it is available by default.
   */
  fun buildFavoritesPlaylist(
    favoriteIds: Set<String>,
    allTracks: List<MusicTrack>,
  ): MusicPlaylist {
    return MusicPlaylist(
      id = FAVORITES_PLAYLIST_ID,
      name = FAVORITES_PLAYLIST_NAME,
      tracks = allTracks.filter { it.id in favoriteIds },
    )
  }

  /**
   * Serializes a set of favorite entity keys/names into a newline-delimited string for `SharedPreferences`.
   */
  fun serializeFavoriteSet(values: Set<String>): String {
    return values
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .joinToString("\n")
  }

  /**
   * Deserializes a newline-delimited `SharedPreferences` string produced by [serializeFavoriteSet].
   */
  fun deserializeFavoriteSet(raw: String?): Set<String> {
    if (raw.isNullOrBlank()) return emptySet()
    return raw.lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .toSet()
  }

  /**
   * Returns `true` if [artistName] is present in [favoriteArtists] (case-insensitive).
   */
  fun isArtistFavorite(
    artistName: String,
    favoriteArtists: Set<String>,
  ): Boolean {
    val target = artistName.trim()
    return target.isNotEmpty() && favoriteArtists.any { it.equals(target, ignoreCase = true) }
  }

  /**
   * Returns `true` if [artist] is present in [favoriteArtists] (case-insensitive).
   */
  fun isArtistFavorite(
    artist: MusicArtist,
    favoriteArtists: Set<String>,
  ): Boolean = isArtistFavorite(artist.name, favoriteArtists)

  /**
   * Toggles [artistName] in [current] favorite artists set (case-insensitive) and returns the updated set.
   */
  fun toggleFavoriteArtist(
    current: Set<String>,
    artistName: String,
  ): Set<String> {
    val trimmed = artistName.trim()
    if (trimmed.isEmpty()) return current
    val existing = current.filter { it.equals(trimmed, ignoreCase = true) }.toSet()
    return if (existing.isNotEmpty()) current - existing else current + trimmed
  }

  /**
   * Toggles [artist] in [current] favorite artists set and returns the updated set.
   */
  fun toggleFavoriteArtist(
    current: Set<String>,
    artist: MusicArtist,
  ): Set<String> = toggleFavoriteArtist(current, artist.name)

  /**
   * Builds the canonical favorite key for an album from [albumName] and optional [artistName].
   */
  fun albumFavoriteKey(
    albumName: String,
    artistName: String = "",
  ): String {
    val trimmedAlbum = albumName.trim()
    val trimmedArtist = artistName.trim()
    return if (trimmedArtist.isNotEmpty() && !trimmedAlbum.contains("|")) {
      "$trimmedAlbum|$trimmedArtist"
    } else {
      trimmedAlbum
    }
  }

  /**
   * Builds the canonical favorite key for [album].
   */
  fun albumFavoriteKey(album: MusicAlbum): String = albumFavoriteKey(album.name, album.artist)

  /**
   * Returns `true` if the album identified by [albumName] and optional [artistName] is in [favoriteAlbums]
   * (case-insensitive).
   */
  fun isAlbumFavorite(
    albumName: String,
    artistName: String = "",
    favoriteAlbums: Set<String>,
  ): Boolean {
    val trimmedAlbum = albumName.trim()
    if (trimmedAlbum.isEmpty()) return false
    val trimmedArtist = artistName.trim()
    val key = albumFavoriteKey(trimmedAlbum, trimmedArtist)
    return favoriteAlbums.any { entry ->
      entry.equals(key, ignoreCase = true) ||
        entry.equals(trimmedAlbum, ignoreCase = true) ||
        (trimmedArtist.isEmpty() && entry.substringBefore("|").equals(trimmedAlbum, ignoreCase = true))
    }
  }

  /**
   * Returns `true` if [album] is in [favoriteAlbums] (case-insensitive).
   */
  fun isAlbumFavorite(
    album: MusicAlbum,
    favoriteAlbums: Set<String>,
  ): Boolean = isAlbumFavorite(album.name, album.artist, favoriteAlbums)

  /**
   * Toggles the album identified by [albumName] and optional [artistName] in [current] favorite albums set
   * (case-insensitive) and returns the updated set.
   */
  fun toggleFavoriteAlbum(
    current: Set<String>,
    albumName: String,
    artistName: String = "",
  ): Set<String> {
    val trimmedAlbum = albumName.trim()
    if (trimmedAlbum.isEmpty()) return current
    val trimmedArtist = artistName.trim()
    val key = albumFavoriteKey(trimmedAlbum, trimmedArtist)
    val existing = current.filter { entry ->
      entry.equals(key, ignoreCase = true) ||
        entry.equals(trimmedAlbum, ignoreCase = true) ||
        (trimmedArtist.isEmpty() && entry.substringBefore("|").equals(trimmedAlbum, ignoreCase = true))
    }.toSet()
    return if (existing.isNotEmpty()) current - existing else current + key
  }

  /**
   * Toggles [album] in [current] favorite albums set and returns the updated set.
   */
  fun toggleFavoriteAlbum(
    current: Set<String>,
    album: MusicAlbum,
  ): Set<String> = toggleFavoriteAlbum(current, album.name, album.artist)

  /**
   * Returns `true` if [genreName] is present in [favoriteGenres] (case-insensitive).
   */
  fun isGenreFavorite(
    genreName: String,
    favoriteGenres: Set<String>,
  ): Boolean {
    val target = genreName.trim()
    return target.isNotEmpty() && favoriteGenres.any { it.equals(target, ignoreCase = true) }
  }

  /**
   * Returns `true` if [genre] is present in [favoriteGenres] (case-insensitive).
   */
  fun isGenreFavorite(
    genre: MusicGenre,
    favoriteGenres: Set<String>,
  ): Boolean = isGenreFavorite(genre.name, favoriteGenres)

  /**
   * Toggles [genreName] in [current] favorite genres set (case-insensitive) and returns the updated set.
   */
  fun toggleFavoriteGenre(
    current: Set<String>,
    genreName: String,
  ): Set<String> {
    val trimmed = genreName.trim()
    if (trimmed.isEmpty()) return current
    val existing = current.filter { it.equals(trimmed, ignoreCase = true) }.toSet()
    return if (existing.isNotEmpty()) current - existing else current + trimmed
  }

  /**
   * Toggles [genre] in [current] favorite genres set and returns the updated set.
   */
  fun toggleFavoriteGenre(
    current: Set<String>,
    genre: MusicGenre,
  ): Set<String> = toggleFavoriteGenre(current, genre.name)

  internal class NormalizedFavoriteLookup(
    val favoriteIds: Set<String>,
    favoriteArtists: Set<String>,
    favoriteAlbums: Set<String>,
    favoriteGenres: Set<String>,
  ) {
    val artistLowerSet: Set<String> = if (favoriteArtists.isEmpty()) {
      emptySet()
    } else {
      favoriteArtists.mapNotNullTo(HashSet(favoriteArtists.size)) {
        it.trim().lowercase(Locale.ROOT).takeIf { s -> s.isNotEmpty() }
      }
    }
    val genreLowerSet: Set<String> = if (favoriteGenres.isEmpty()) {
      emptySet()
    } else {
      favoriteGenres.mapNotNullTo(HashSet(favoriteGenres.size)) {
        it.trim().lowercase(Locale.ROOT).takeIf { s -> s.isNotEmpty() }
      }
    }
    val albumExactLowerSet: Set<String>
    val albumNameOnlyLowerSet: Set<String>

    init {
      if (favoriteAlbums.isEmpty()) {
        albumExactLowerSet = emptySet()
        albumNameOnlyLowerSet = emptySet()
      } else {
        val exact = HashSet<String>(favoriteAlbums.size * 2)
        val nameOnly = HashSet<String>(favoriteAlbums.size)
        for (entry in favoriteAlbums) {
          val trimmed = entry.trim().lowercase(Locale.ROOT)
          if (trimmed.isNotEmpty()) {
            exact.add(trimmed)
            nameOnly.add(trimmed.substringBefore("|").trim())
          }
        }
        albumExactLowerSet = exact
        albumNameOnlyLowerSet = nameOnly
      }
    }

    val isEmpty: Boolean
      get() = favoriteIds.isEmpty() && artistLowerSet.isEmpty() && albumExactLowerSet.isEmpty() && genreLowerSet.isEmpty()

    fun isArtistFav(artistName: String): Boolean {
      if (artistLowerSet.isEmpty()) return false
      val target = artistName.trim().lowercase(Locale.ROOT)
      return target.isNotEmpty() && target in artistLowerSet
    }

    fun isAlbumFav(albumName: String, artistName: String): Boolean {
      if (albumExactLowerSet.isEmpty()) return false
      val trimmedAlbum = albumName.trim().lowercase(Locale.ROOT)
      if (trimmedAlbum.isEmpty()) return false
      if (trimmedAlbum in albumExactLowerSet) return true
      val trimmedArtist = artistName.trim().lowercase(Locale.ROOT)
      if (trimmedArtist.isEmpty()) {
        return trimmedAlbum in albumNameOnlyLowerSet
      }
      val key = if (!trimmedAlbum.contains("|")) "$trimmedAlbum|$trimmedArtist" else trimmedAlbum
      return key in albumExactLowerSet
    }

    fun isGenreFav(genreName: String): Boolean {
      if (genreLowerSet.isEmpty()) return false
      val target = genreName.trim().lowercase(Locale.ROOT)
      return target.isNotEmpty() && target in genreLowerSet
    }

    fun isTrackOrCategoryFav(track: MusicTrack): Boolean {
      if (isEmpty) return false
      return track.id in favoriteIds ||
        isArtistFav(track.artist) ||
        isAlbumFav(track.album, track.artist) ||
        isGenreFav(track.genre)
    }
  }

  private class PlayStatSortEntry(
    val track: MusicTrack,
    val engagementScore: Long,
    val fullPlays: Double,
    val playCount: Int,
    val totalPlayTimeMs: Long,
    val effectiveSkipCount: Int,
    val lastPlayedAtMs: Long,
  ) : Comparable<PlayStatSortEntry> {
    override fun compareTo(other: PlayStatSortEntry): Int {
      var cmp = other.engagementScore.compareTo(this.engagementScore)
      if (cmp != 0) return cmp
      cmp = other.fullPlays.compareTo(this.fullPlays)
      if (cmp != 0) return cmp
      cmp = other.playCount.compareTo(this.playCount)
      if (cmp != 0) return cmp
      cmp = other.totalPlayTimeMs.compareTo(this.totalPlayTimeMs)
      if (cmp != 0) return cmp
      cmp = this.effectiveSkipCount.compareTo(other.effectiveSkipCount)
      if (cmp != 0) return cmp
      cmp = other.lastPlayedAtMs.compareTo(this.lastPlayedAtMs)
      if (cmp != 0) return cmp
      return String.CASE_INSENSITIVE_ORDER.compare(this.track.title, other.track.title)
    }
  }

  private class RecentlyPlayedSortEntry(
    val track: MusicTrack,
    val lastPlayedAtMs: Long,
    val engagementScore: Long,
    val playCount: Int,
    val totalPlayTimeMs: Long,
  ) : Comparable<RecentlyPlayedSortEntry> {
    override fun compareTo(other: RecentlyPlayedSortEntry): Int {
      var cmp = other.lastPlayedAtMs.compareTo(this.lastPlayedAtMs)
      if (cmp != 0) return cmp
      cmp = other.engagementScore.compareTo(this.engagementScore)
      if (cmp != 0) return cmp
      cmp = other.playCount.compareTo(this.playCount)
      if (cmp != 0) return cmp
      cmp = other.totalPlayTimeMs.compareTo(this.totalPlayTimeMs)
      if (cmp != 0) return cmp
      return String.CASE_INSENSITIVE_ORDER.compare(this.track.title, other.track.title)
    }
  }

  /**
   * Returns `true` if [track] is favorited directly (`track.id in favoriteIds`) or belongs to a
   * favorited artist, album, or genre.
   */
  fun isTrackOrCategoryFavorite(
    track: MusicTrack,
    favoriteIds: Set<String> = activeSessionState?.favoriteIds ?: emptySet(),
    favoriteArtists: Set<String> = activeSessionState?.favoriteArtists ?: emptySet(),
    favoriteAlbums: Set<String> = activeSessionState?.favoriteAlbums ?: emptySet(),
    favoriteGenres: Set<String> = activeSessionState?.favoriteGenres ?: emptySet(),
  ): Boolean {
    return track.id in favoriteIds ||
      isArtistFavorite(track.artist, favoriteArtists) ||
      isAlbumFavorite(track.album, track.artist, favoriteAlbums) ||
      isGenreFavorite(track.genre, favoriteGenres)
  }

  /**
   * Sorts [tracks] by combined play statistics (calculated full plays, # of plays, and total listen
   * time, with a light recency weight from [TrackPlayStats.lastPlayedAtMs] and skips as a light
   * tie-breaker unless the song or its artist/album/genre is favorited), falling back to
   * most-recently-played and alphabetical title order for ties.
   *
   * Precomputes per-track sort keys and normalized favorite lookup sets in `O(N)` so sorting does
   * not repeat logarithm calculations or linear set scans inside the comparator.
   */
  fun sortTracksByPlayStats(
    tracks: List<MusicTrack>,
    playStats: Map<String, TrackPlayStats>,
    favoriteIds: Set<String> = activeSessionState?.favoriteIds ?: emptySet(),
    favoriteArtists: Set<String> = activeSessionState?.favoriteArtists ?: emptySet(),
    favoriteAlbums: Set<String> = activeSessionState?.favoriteAlbums ?: emptySet(),
    favoriteGenres: Set<String> = activeSessionState?.favoriteGenres ?: emptySet(),
  ): List<MusicTrack> {
    if (tracks.isEmpty()) return emptyList()
    if (tracks.size == 1) return tracks.toList()
    if (playStats.isEmpty()) {
      return tracks.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    }
    val referenceTimeMs = playStats.values.maxOfOrNull { it.lastPlayedAtMs } ?: 0L
    val favLookup = NormalizedFavoriteLookup(
      favoriteIds = favoriteIds,
      favoriteArtists = favoriteArtists,
      favoriteAlbums = favoriteAlbums,
      favoriteGenres = favoriteGenres,
    )
    val entries = Array(tracks.size) { index ->
      val track = tracks[index]
      val stat = playStats[track.id]
      if (stat == null || !stat.hasRecordedActivity) {
        PlayStatSortEntry(
          track = track,
          engagementScore = 0L,
          fullPlays = 0.0,
          playCount = 0,
          totalPlayTimeMs = 0L,
          effectiveSkipCount = 0,
          lastPlayedAtMs = 0L,
        )
      } else {
        val ignoreSkips = stat.skipCount > 0 && favLookup.isTrackOrCategoryFav(track)
        PlayStatSortEntry(
          track = track,
          engagementScore = stat.engagementScoreForTrack(
            trackDurationMs = track.durationMs,
            referenceTimeMs = referenceTimeMs,
            ignoreSkips = ignoreSkips,
          ),
          fullPlays = stat.calculatedFullPlays(track.durationMs),
          playCount = stat.playCount,
          totalPlayTimeMs = stat.totalPlayTimeMs,
          effectiveSkipCount = if (ignoreSkips) 0 else stat.skipCount,
          lastPlayedAtMs = stat.lastPlayedAtMs,
        )
      }
    }
    entries.sort()
    return List(entries.size) { entries[it].track }
  }

  /**
   * Builds the built-in Most Played [MusicPlaylist], automatically sorting all songs in [allTracks]
   * by a combination of play count, calculated full plays, total playtime, and a light recency weight
   * from when each song was last played (with skips as a light tie-breaker, cancelled out when the
   * song or its artist/album/genre is favorited).
   * Always returned so it is available by default.
   */
  fun buildMostPlayedPlaylist(
    playStats: Map<String, TrackPlayStats>,
    allTracks: List<MusicTrack>,
    favoriteIds: Set<String> = activeSessionState?.favoriteIds ?: emptySet(),
    favoriteArtists: Set<String> = activeSessionState?.favoriteArtists ?: emptySet(),
    favoriteAlbums: Set<String> = activeSessionState?.favoriteAlbums ?: emptySet(),
    favoriteGenres: Set<String> = activeSessionState?.favoriteGenres ?: emptySet(),
  ): MusicPlaylist {
    return MusicPlaylist(
      id = MOST_PLAYED_PLAYLIST_ID,
      name = MOST_PLAYED_PLAYLIST_NAME,
      tracks = sortTracksByPlayStats(
        tracks = allTracks,
        playStats = playStats,
        favoriteIds = favoriteIds,
        favoriteArtists = favoriteArtists,
        favoriteAlbums = favoriteAlbums,
        favoriteGenres = favoriteGenres,
      ),
    )
  }

  /**
   * Sorts [tracks] by when each song was last played ([TrackPlayStats.lastPlayedAtMs] descending),
   * falling back to engagement score and alphabetical title order for ties or unplayed songs.
   */
  fun sortTracksByRecentlyPlayed(
    tracks: List<MusicTrack>,
    playStats: Map<String, TrackPlayStats>,
  ): List<MusicTrack> {
    if (tracks.isEmpty()) return emptyList()
    if (tracks.size == 1) return tracks.toList()
    if (playStats.isEmpty()) {
      return tracks.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    }
    val referenceTimeMs = playStats.values.maxOfOrNull { it.lastPlayedAtMs } ?: 0L
    val entries = Array(tracks.size) { index ->
      val track = tracks[index]
      val stat = playStats[track.id]
      if (stat == null || !stat.hasRecordedActivity) {
        RecentlyPlayedSortEntry(
          track = track,
          lastPlayedAtMs = 0L,
          engagementScore = 0L,
          playCount = 0,
          totalPlayTimeMs = 0L,
        )
      } else {
        RecentlyPlayedSortEntry(
          track = track,
          lastPlayedAtMs = stat.lastPlayedAtMs,
          engagementScore = stat.engagementScoreForTrack(track.durationMs, referenceTimeMs),
          playCount = stat.playCount,
          totalPlayTimeMs = stat.totalPlayTimeMs,
        )
      }
    }
    entries.sort()
    return List(entries.size) { entries[it].track }
  }

  /**
   * Builds the built-in Recently Played [MusicPlaylist], automatically sorting songs in [allTracks]
   * by when each song was last played ([TrackPlayStats.lastPlayedAtMs] descending).
   * Always returned so it is available by default.
   */
  fun buildRecentlyPlayedPlaylist(
    playStats: Map<String, TrackPlayStats>,
    allTracks: List<MusicTrack>,
  ): MusicPlaylist {
    return MusicPlaylist(
      id = RECENTLY_PLAYED_PLAYLIST_ID,
      name = RECENTLY_PLAYED_PLAYLIST_NAME,
      tracks = sortTracksByRecentlyPlayed(allTracks, playStats),
    )
  }

  /**
   * Records a play-count increment, additional listening duration, and/or skip-count increment
   * for [trackId] inside [current].
   */
  fun recordPlayStat(
    current: Map<String, TrackPlayStats>,
    trackId: String,
    incrementPlayCount: Boolean = false,
    addedPlayTimeMs: Long = 0L,
    incrementSkipCount: Boolean = false,
    timestampMs: Long = System.currentTimeMillis(),
  ): Map<String, TrackPlayStats> {
    if (trackId.isBlank()) return current
    val deltaMs = addedPlayTimeMs.coerceAtLeast(0L)
    if (!incrementPlayCount && deltaMs == 0L && !incrementSkipCount) return current
    val prev = current[trackId] ?: TrackPlayStats()
    val updated = prev.copy(
      playCount = prev.playCount + if (incrementPlayCount) 1 else 0,
      totalPlayTimeMs = prev.totalPlayTimeMs + deltaMs,
      lastPlayedAtMs = max(prev.lastPlayedAtMs, timestampMs),
      skipCount = prev.skipCount + if (incrementSkipCount) 1 else 0,
    )
    return current + (trackId to updated)
  }

  /**
   * Serializes a [TrackPlayStats] map into a compact string for `SharedPreferences`.
   */
  fun serializePlayStats(stats: Map<String, TrackPlayStats>): String {
    return stats.entries
      .filter { (id, s) -> id.isNotBlank() && s.hasRecordedActivity }
      .joinToString("\n") { (id, s) ->
        val safeId = id.replace("\n", " ").replace("\t", " ").trim()
        "$safeId\t${s.playCount}\t${s.totalPlayTimeMs}\t${s.lastPlayedAtMs}\t${s.skipCount}"
      }
  }

  /**
   * Deserializes a `SharedPreferences` string produced by [serializePlayStats].
   */
  fun deserializePlayStats(raw: String?): Map<String, TrackPlayStats> {
    if (raw.isNullOrBlank()) return emptyMap()
    val result = LinkedHashMap<String, TrackPlayStats>()
    raw.lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .forEach { line ->
        val parts = line.split("\t")
        if (parts.size >= 3) {
          val id = parts[0].trim()
          val count = parts[1].trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
          val timeMs = parts[2].trim().toLongOrNull()?.coerceAtLeast(0L) ?: 0L
          val lastAt = parts.getOrNull(3)?.trim()?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
          val skipCount = parts.getOrNull(4)?.trim()?.toIntOrNull()?.coerceAtLeast(0) ?: 0
          if (id.isNotEmpty() && (count > 0 || timeMs > 0L || lastAt > 0L || skipCount > 0)) {
            result[id] = TrackPlayStats(
              playCount = count,
              totalPlayTimeMs = timeMs,
              lastPlayedAtMs = lastAt,
              skipCount = skipCount,
            )
          }
        }
      }
    return result
  }

  /**
   * Resolves [SavedPlaylistEntry] items against [allTracks] to build [MusicPlaylist] models.
   */
  fun buildPlaylists(
    entries: List<SavedPlaylistEntry>,
    allTracks: List<MusicTrack>,
  ): List<MusicPlaylist> {
    if (entries.isEmpty()) return emptyList()
    val tracksById = allTracks.associateBy { it.id }
    return entries.mapNotNull { entry ->
      val resolvedTracks = entry.trackIds.mapNotNull { tracksById[it] }
      if (resolvedTracks.isEmpty() && allTracks.isNotEmpty()) {
        null
      } else {
        MusicPlaylist(
          id = entry.id,
          name = entry.name,
          tracks = resolvedTracks,
          createdAtMs = entry.createdAtMs,
        )
      }
    }
  }

  /**
   * Filters [playlists] by matching [query] against playlist name, artist names, or track titles.
   */
  fun filterPlaylists(playlists: List<MusicPlaylist>, query: String): List<MusicPlaylist> {
    val q = query.trim()
    if (q.isEmpty()) return playlists
    return playlists.filter { playlist ->
      playlist.name.contains(q, ignoreCase = true) ||
        playlist.artists.any { it.contains(q, ignoreCase = true) } ||
        playlist.tracks.any { it.title.contains(q, ignoreCase = true) }
    }
  }

  /**
   * Serializes [entries] into a compact string for `SharedPreferences`.
   */
  fun serializePlaylists(entries: List<SavedPlaylistEntry>): String {
    return entries.joinToString("\n") { entry ->
      val safeName = entry.name.replace("\n", " ").replace("\t", " ").trim()
      val idsJoined = entry.trackIds.joinToString(",")
      "${entry.id}\t$safeName\t${entry.createdAtMs}\t$idsJoined"
    }
  }

  /**
   * Deserializes a `SharedPreferences` string produced by [serializePlaylists].
   */
  fun deserializePlaylists(raw: String?): List<SavedPlaylistEntry> {
    if (raw.isNullOrBlank()) return emptyList()
    return raw.lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .mapNotNull { line ->
        val parts = line.split("\t")
        if (parts.size < 4) return@mapNotNull null
        val id = parts[0].trim()
        val name = parts[1].trim()
        val createdAt = parts[2].trim().toLongOrNull() ?: 0L
        val trackIds = parts[3].split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (id.isEmpty() || name.isEmpty() || trackIds.isEmpty()) null
        else SavedPlaylistEntry(id = id, name = name, trackIds = trackIds, createdAtMs = createdAt)
      }
      .toList()
  }

  /**
   * Sorts [tracks] according to [sortOrder].
   */
  fun sortTracks(
    tracks: List<MusicTrack>,
    sortOrder: MusicSortOrder,
    playStats: Map<String, TrackPlayStats> = emptyMap(),
    favoriteIds: Set<String> = activeSessionState?.favoriteIds ?: emptySet(),
    favoriteArtists: Set<String> = activeSessionState?.favoriteArtists ?: emptySet(),
    favoriteAlbums: Set<String> = activeSessionState?.favoriteAlbums ?: emptySet(),
    favoriteGenres: Set<String> = activeSessionState?.favoriteGenres ?: emptySet(),
    randomSeed: Long = 0L,
  ): List<MusicTrack> {
    if (tracks.size <= 1) return tracks.toList()
    return when (sortOrder) {
      MusicSortOrder.TITLE_ASC -> tracks.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
      MusicSortOrder.TITLE_DESC -> tracks.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.title })
      MusicSortOrder.ARTIST_ASC -> tracks.sortedWith(
        compareBy<MusicTrack, String>(String.CASE_INSENSITIVE_ORDER) { it.artist }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.album }
          .thenBy { it.trackNumber }
      )
      MusicSortOrder.ALBUM_ASC -> tracks.sortedWith(
        compareBy<MusicTrack, String>(String.CASE_INSENSITIVE_ORDER) { it.album }
          .thenBy { it.trackNumber }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
      )
      MusicSortOrder.DURATION_DESC -> tracks.sortedWith(
        compareByDescending<MusicTrack> { it.durationMs }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
      )
      MusicSortOrder.DURATION_ASC -> tracks.sortedWith(
        compareBy<MusicTrack> { it.durationMs }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
      )
      MusicSortOrder.YEAR_DESC -> tracks.sortedWith(
        compareByDescending<MusicTrack> { it.year }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
      )
      MusicSortOrder.MOST_PLAYED -> sortTracksByPlayStats(
        tracks = tracks,
        playStats = playStats,
        favoriteIds = favoriteIds,
        favoriteArtists = favoriteArtists,
        favoriteAlbums = favoriteAlbums,
        favoriteGenres = favoriteGenres,
      )
      MusicSortOrder.RECENTLY_PLAYED -> sortTracksByRecentlyPlayed(tracks, playStats)
      MusicSortOrder.RANDOM -> {
        val rng = if (randomSeed != 0L) kotlin.random.Random(randomSeed) else kotlin.random.Random.Default
        var shuffled = tracks.shuffled(rng)
        if (tracks.size > 1 && shuffled.map { it.id } == tracks.map { it.id }) {
          shuffled = tracks.drop(1) + tracks.first()
        }
        shuffled
      }
    }
  }

  /**
   * Computes a positive selection weight for [track] used by [MusicShuffleMode.SMART_SHUFFLE]
   * so more-played songs are weighted more heavily towards the top of a shuffle while every
   * song retains a non-zero chance of appearing anywhere.
   */
  fun smartShuffleWeight(
    track: MusicTrack,
    playStats: Map<String, TrackPlayStats>,
    referenceTimeMs: Long = playStats.values.maxOfOrNull { it.lastPlayedAtMs } ?: 0L,
  ): Double {
    val stat = playStats[track.id] ?: return 1.0
    val score = stat.engagementScoreForTrack(track.durationMs, referenceTimeMs).coerceAtLeast(0L)
    val fullPlays = stat.calculatedFullPlays(track.durationMs).coerceAtLeast(0.0)
    return (1.0 + (score.toDouble() / 30_000.0) + (fullPlays * 2.5) + (stat.playCount * 1.5))
      .coerceAtLeast(0.25)
  }

  /**
   * Performs a weighted random shuffle without replacement on [tracks], weighting tracks with
   * higher play stats more towards the top of the returned list.
   */
  fun smartShuffleTracks(
    tracks: List<MusicTrack>,
    playStats: Map<String, TrackPlayStats>,
    random: kotlin.random.Random = kotlin.random.Random.Default,
  ): List<MusicTrack> {
    if (tracks.size <= 1) return tracks.toList()
    val pool = tracks.toMutableList()
    val referenceTimeMs = if (playStats.isEmpty()) {
      0L
    } else {
      playStats.values.maxOfOrNull { it.lastPlayedAtMs } ?: 0L
    }
    val weights = pool.map { smartShuffleWeight(it, playStats, referenceTimeMs) }.toMutableList()
    val result = ArrayList<MusicTrack>(pool.size)
    while (pool.isNotEmpty()) {
      val totalWeight = weights.sum()
      val chosenIdx = if (totalWeight <= 0.0) {
        random.nextInt(pool.size)
      } else {
        var r = random.nextDouble() * totalWeight
        var idx = pool.lastIndex
        for (i in pool.indices) {
          r -= weights[i]
          if (r <= 0.0) {
            idx = i
            break
          }
        }
        idx
      }
      result.add(pool.removeAt(chosenIdx))
      weights.removeAt(chosenIdx)
    }
    return result
  }

  /**
   * Reorders [baseQueue] according to [mode] while keeping the track matching [anchorTrackId]
   * (if present) pinned at index `0` when shuffling so the currently playing track continues
   * uninterrupted and the remaining tracks in the queue are shuffled behind it.
   *
   * - [MusicShuffleMode.OFF]: restores [baseQueue] in its original order and returns the index of
   *   [anchorTrackId] (or `0` if not found).
   * - [MusicShuffleMode.SHUFFLE]: pins [anchorTrackId] at index `0` and uniformly shuffles the
   *   remaining tracks.
   * - [MusicShuffleMode.SMART_SHUFFLE]: pins [anchorTrackId] at index `0` and weighted-shuffles
   *   the remaining tracks using [smartShuffleTracks] so most-played songs are weighted towards
   *   the top of the shuffle.
   */
  fun applyShuffleModeToQueue(
    baseQueue: List<MusicTrack>,
    currentQueue: List<MusicTrack> = baseQueue,
    anchorTrackId: String? = null,
    mode: MusicShuffleMode,
    playStats: Map<String, TrackPlayStats> = emptyMap(),
    random: kotlin.random.Random = kotlin.random.Random.Default,
  ): Pair<List<MusicTrack>, Int> {
    val source = if (baseQueue.isNotEmpty()) baseQueue else currentQueue
    if (source.isEmpty()) return emptyList<MusicTrack>() to 0
    if (mode == MusicShuffleMode.OFF) {
      val restoredIndex = if (anchorTrackId != null) {
        source.indexOfFirst { it.id == anchorTrackId }.coerceAtLeast(0)
      } else {
        0
      }
      return source.toList() to restoredIndex
    }
    if (source.size == 1) return source.toList() to 0

    val anchorIdx = if (anchorTrackId != null) source.indexOfFirst { it.id == anchorTrackId } else -1
    val anchorTrack = if (anchorIdx >= 0) source[anchorIdx] else null
    val candidates = if (anchorIdx >= 0) {
      source.filterIndexed { idx, _ -> idx != anchorIdx }
    } else {
      source
    }

    val shuffledCandidates = when (mode) {
      MusicShuffleMode.OFF -> candidates
      MusicShuffleMode.SHUFFLE -> {
        var candidateOrder = candidates.shuffled(random)
        if (candidates.size > 1 && candidateOrder.map { it.id } == candidates.map { it.id }) {
          candidateOrder = candidates.drop(1) + candidates.first()
        }
        candidateOrder
      }
      MusicShuffleMode.SMART_SHUFFLE -> {
        var candidateOrder = smartShuffleTracks(candidates, playStats, random)
        if (candidates.size > 1 && candidateOrder.map { it.id } == candidates.map { it.id }) {
          // Ensure smart shuffle visibly reorders if the initial draw matched the exact input order
          // unless the input was already strictly sorted by smartShuffleWeight
          val altOrder = smartShuffleTracks(candidates, playStats, random)
          if (altOrder.map { it.id } != candidates.map { it.id }) {
            candidateOrder = altOrder
          }
        }
        candidateOrder
      }
    }

    val finalQueue = if (anchorTrack != null) {
      listOf(anchorTrack) + shuffledCandidates
    } else {
      shuffledCandidates
    }
    return finalQueue to 0
  }

  /**
   * Resolves the next track index in a queue of size [queueSize].
   * Because the playback queue itself is shuffled when [MusicShuffleMode] is enabled,
   * progression steps sequentially through the queue rather than jumping to a random index.
   *
   * @param userInitiated `true` when the user explicitly tapped the Next button; `false` when a track
   *   finished playing naturally.
   */
  fun resolveNextTrackIndex(
    currentIndex: Int,
    queueSize: Int,
    repeatMode: MusicRepeatMode,
    shuffleEnabled: Boolean = false,
    userInitiated: Boolean = true,
  ): Int? {
    if (queueSize <= 0) return null
    val safeCurrent = currentIndex.coerceIn(0, queueSize - 1)

    if (!userInitiated && repeatMode == MusicRepeatMode.REPEAT_ONE) {
      return safeCurrent
    }

    val nextIndex = safeCurrent + 1
    return if (nextIndex < queueSize) {
      nextIndex
    } else if (repeatMode == MusicRepeatMode.REPEAT_ALL || repeatMode == MusicRepeatMode.REPEAT_ONE || userInitiated) {
      0
    } else {
      null
    }
  }

  /**
   * Resolves the target track index when the user taps Previous.
   * If [currentPositionMs] > 3,000 ms, returns [currentIndex] (restarting the current song);
   * otherwise steps back to the previous track (wrapping to the end of the queue).
   */
  fun resolvePreviousTrackIndex(
    currentIndex: Int,
    queueSize: Int,
    currentPositionMs: Long = 0L,
  ): Int? {
    if (queueSize <= 0) return null
    val safeCurrent = currentIndex.coerceIn(0, queueSize - 1)
    if (currentPositionMs > 3_000L) {
      return safeCurrent
    }
    return if (safeCurrent > 0) {
      safeCurrent - 1
    } else {
      queueSize - 1
    }
  }

  /**
   * Moves the track at [fromIndex] to [toIndex] in [queue], returning the updated queue and the
   * adjusted [currentQueueIndex] so the currently active track stays pointed at the same song.
   */
  fun reorderQueue(
    queue: List<MusicTrack>,
    fromIndex: Int,
    toIndex: Int,
    currentQueueIndex: Int,
  ): Pair<List<MusicTrack>, Int> {
    if (queue.isEmpty() || fromIndex !in queue.indices || toIndex !in queue.indices || fromIndex == toIndex) {
      return queue to currentQueueIndex.coerceIn(0, maxOf(0, queue.lastIndex))
    }
    val mutable = queue.toMutableList()
    val moved = mutable.removeAt(fromIndex)
    mutable.add(toIndex, moved)
    val safeCur = currentQueueIndex.coerceIn(0, queue.lastIndex)
    val newCurrentIndex = when {
      safeCur == fromIndex -> toIndex
      fromIndex < safeCur && toIndex >= safeCur -> safeCur - 1
      fromIndex > safeCur && toIndex <= safeCur -> safeCur + 1
      else -> safeCur
    }
    return mutable.toList() to newCurrentIndex
  }

  /**
   * Parses a persisted `|`-separated list of [MusicLibraryTab] names, deduplicating entries and
   * appending any missing tabs in their default order so all library sections are always present.
   * [MusicLibraryTab.HOME] is always fixed at index `0` at the front of the pill list.
   */
  fun parseTabOrder(raw: String?): List<MusicLibraryTab> {
    if (raw.isNullOrBlank()) return MusicLibraryTab.entries.toList()
    val parsed = raw
      .split("|", ",")
      .mapNotNull { token ->
        val trimmed = token.trim()
        if (trimmed.isEmpty()) null
        else MusicLibraryTab.entries.find { it.name.equals(trimmed, ignoreCase = true) }
      }
      .distinct()
    val missing = MusicLibraryTab.entries.filterNot { it in parsed }
    val others = (parsed + missing).filterNot { it == MusicLibraryTab.HOME }
    return listOf(MusicLibraryTab.HOME) + others
  }

  private val GENRE_TOKEN_SPLIT_REGEX = Regex("[^\\p{L}\\p{N}]+")

  /**
   * Normalizes [genre] text into lowercase alphanumeric words/tokens for text-similarity comparison.
   */
  internal fun tokenizeGenreText(genre: String): List<String> {
    return genre
      .trim()
      .lowercase(Locale.ROOT)
      .split(GENRE_TOKEN_SPLIT_REGEX)
      .filter { it.isNotEmpty() }
  }

  /**
   * Extracts character n-grams (default 3-grams, plus 2-grams for short words) from normalized
   * genre text so sub-words and compound genre names (e.g., "Synthwave" vs "Synthpop" vs "Chillwave")
   * can be compared directly by their text content.
   */
  private fun extractGenreNGrams(normText: String): Set<String> {
    val compact = normText.replace(GENRE_TOKEN_SPLIT_REGEX, "")
    if (compact.isEmpty()) return emptySet()
    if (compact.length < 3) return setOf(compact)
    val grams = HashSet<String>(compact.length * 2)
    for (i in 0..compact.length - 3) {
      grams.add(compact.substring(i, i + 3))
    }
    for (i in 0..compact.length - 2) {
      grams.add(compact.substring(i, i + 2))
    }
    return grams
  }

  /**
   * Computes a normalized text similarity score in `0.0..1.0` between [genreA] and [genreB] by
   * comparing shared words, substring containment, common prefixes/suffixes, and character n-gram
   * Dice similarity.
   */
  fun computeGenreTextSimilarity(genreA: String, genreB: String): Double {
    val normA = genreA.trim().lowercase(Locale.ROOT)
    val normB = genreB.trim().lowercase(Locale.ROOT)
    if (normA.isEmpty() || normB.isEmpty()) return 0.0
    if (normA == normB) return 1.0

    val tokensA = tokenizeGenreText(normA)
    val tokensB = tokenizeGenreText(normB)
    val tokenSetA = tokensA.toSet()
    val tokenSetB = tokensB.toSet()

    // 1. Exact shared word tokens (e.g., "Indie Rock" & "Alternative Rock", "Indie Pop" & "Indie Folk")
    val sharedTokens = tokenSetA.intersect(tokenSetB).filter { it.length >= 2 }
    val tokenOverlapScore = if (sharedTokens.isNotEmpty()) {
      val unionSize = tokenSetA.union(tokenSetB).size.coerceAtLeast(1)
      (0.65 + 0.35 * (sharedTokens.size.toDouble() / unionSize.toDouble())).coerceAtMost(0.98)
    } else {
      0.0
    }

    // 2. Substring containment across full normalized text or individual tokens (>= 3 chars)
    val compactA = normA.replace(GENRE_TOKEN_SPLIT_REGEX, "")
    val compactB = normB.replace(GENRE_TOKEN_SPLIT_REGEX, "")
    var substringScore = 0.0
    if (compactA.length >= 3 && compactB.length >= 3 && (compactA.contains(compactB) || compactB.contains(compactA))) {
      val ratio = minOf(compactA.length, compactB.length).toDouble() / maxOf(compactA.length, compactB.length).toDouble()
      substringScore = (0.60 + 0.35 * ratio).coerceAtMost(0.95)
    } else {
      for (tA in tokensA) {
        if (tA.length < 3) continue
        for (tB in tokensB) {
          if (tB.length < 3) continue
          if (tA.contains(tB) || tB.contains(tA)) {
            val ratio = minOf(tA.length, tB.length).toDouble() / maxOf(tA.length, tB.length).toDouble()
            substringScore = maxOf(substringScore, 0.55 + 0.30 * ratio)
          }
        }
      }
    }

    // 3. Shared prefix or suffix stem across tokens (e.g., "Synthwave" & "Synthpop" share "synth",
    //    "Synthwave" & "Chillwave" share "wave", "Electronic" & "Electro" share "electro")
    var affixScore = 0.0
    for (tA in tokensA + listOf(compactA)) {
      if (tA.length < 4) continue
      for (tB in tokensB + listOf(compactB)) {
        if (tB.length < 4) continue
        val commonPrefix = tA.commonPrefixWith(tB).length
        val commonSuffix = tA.commonSuffixWith(tB).length
        val bestAffix = maxOf(commonPrefix, commonSuffix)
        if (bestAffix >= 4) {
          val ratio = bestAffix.toDouble() / minOf(tA.length, tB.length).toDouble()
          affixScore = maxOf(affixScore, (0.45 + 0.35 * ratio).coerceAtMost(0.85))
        }
      }
    }

    // 4. Character n-gram Dice coefficient for fuzzy text similarity
    val gramsA = extractGenreNGrams(normA)
    val gramsB = extractGenreNGrams(normB)
    val ngramScore = if (gramsA.isNotEmpty() && gramsB.isNotEmpty()) {
      val intersection = gramsA.count { it in gramsB }
      (2.0 * intersection.toDouble()) / (gramsA.size + gramsB.size).toDouble()
    } else {
      0.0
    }

    return maxOf(tokenOverlapScore, substringScore, affixScore, ngramScore).coerceIn(0.0, 1.0)
  }

  /**
   * Returns `true` if [genreA] and [genreB] have similar genre text (shared words, stems, prefixes,
   * suffixes, substrings, or high character n-gram overlap).
   */
  fun areGenresSimilar(genreA: String, genreB: String): Boolean {
    return computeGenreTextSimilarity(genreA, genreB) >= 0.45
  }

  /**
   * Groups [genres] into clusters of genres with similar text. Genres that share words, stems,
   * prefixes/suffixes, or high text similarity are grouped together (using connected components at
   * the similarity threshold, or grouping top text-similarity neighbors when all genre names are
   * distinct).
   */
  fun groupGenresByTextSimilarity(genres: List<MusicGenre>): List<List<MusicGenre>> {
    val validGenres = genres.filter {
      it.name.isNotBlank() && !it.name.equals("Unknown Genre", ignoreCase = true)
    }.ifEmpty { genres }
    if (validGenres.size <= 1) return if (validGenres.isEmpty()) emptyList() else listOf(validGenres)

    val n = validGenres.size
    val similarityMatrix = Array(n) { DoubleArray(n) }
    for (i in 0 until n) {
      similarityMatrix[i][i] = 1.0
      for (j in i + 1 until n) {
        val sim = computeGenreTextSimilarity(validGenres[i].name, validGenres[j].name)
        similarityMatrix[i][j] = sim
        similarityMatrix[j][i] = sim
      }
    }

    // 1. Build connected components of genres whose text similarity meets the threshold (>= 0.45)
    val visited = BooleanArray(n)
    val textClusters = mutableListOf<List<MusicGenre>>()
    for (i in 0 until n) {
      if (visited[i]) continue
      val componentIndices = mutableListOf<Int>()
      val queue = ArrayDeque<Int>()
      queue.add(i)
      visited[i] = true
      while (queue.isNotEmpty()) {
        val curr = queue.removeFirst()
        componentIndices.add(curr)
        for (next in 0 until n) {
          if (!visited[next] && similarityMatrix[curr][next] >= 0.45) {
            visited[next] = true
            queue.add(next)
          }
        }
      }
      if (componentIndices.size >= 2) {
        textClusters.add(componentIndices.map { validGenres[it] })
      }
    }

    // 2. Also build anchor-based neighborhood groups for each genre with any text-similar neighbors
    //    so large families or overlapping stems (e.g., "Indie Rock", "Indie Pop", "Alternative Rock")
    //    can be sampled cleanly
    for (i in 0 until n) {
      val neighbors = (0 until n)
        .filter { j -> j != i && similarityMatrix[i][j] >= 0.30 }
        .sortedByDescending { j -> similarityMatrix[i][j] }
      if (neighbors.isNotEmpty()) {
        val group = listOf(validGenres[i]) + neighbors.map { validGenres[it] }
        val groupNames = group.map { it.name }.toSet()
        if (textClusters.none { existing -> existing.map { it.name }.toSet() == groupNames }) {
          textClusters.add(group)
        }
      }
    }

    // 3. Fallback when no genres share >= 0.30 text similarity: group each genre with its closest
    //    textual neighbors by character/n-gram similarity
    if (textClusters.isEmpty()) {
      for (i in 0 until n) {
        val closestNeighbors = (0 until n)
          .filter { it != i }
          .sortedWith(
            compareByDescending<Int> { j -> similarityMatrix[i][j] }
              .thenBy { j -> validGenres[j].name.lowercase(Locale.ROOT) }
          )
          .take(minOf(3, n - 1))
        val group = listOf(validGenres[i]) + closestNeighbors.map { validGenres[it] }
        val groupNames = group.map { it.name }.toSet()
        if (textClusters.none { existing -> existing.map { it.name }.toSet() == groupNames }) {
          textClusters.add(group)
        }
      }
    }

    // Sort clusters so stronger/larger text-similar groups come first
    return textClusters.sortedWith(
      compareByDescending<List<MusicGenre>> { cluster ->
        var totalSim = 0.0
        var pairCount = 0
        for (a in cluster.indices) {
          for (b in a + 1 until cluster.size) {
            totalSim += computeGenreTextSimilarity(cluster[a].name, cluster[b].name)
            pairCount++
          }
        }
        val avgSim = if (pairCount > 0) totalSim / pairCount else 0.0
        (if (avgSim >= 0.45) 10.0 else 0.0) + avgSim
      }.thenByDescending { it.size }
    )
  }

  /**
   * Selects tracks from [primaryTracks] (and optionally [fallbackTracks]) targeting approximately
   * [targetDurationMs] (~1 hour by default, not strict) without duplicating tracks.
   */
  fun selectTracksTowardTargetDuration(
    primaryTracks: List<MusicTrack>,
    fallbackTracks: List<MusicTrack> = emptyList(),
    targetDurationMs: Long = TARGET_HOME_PLAYLIST_DURATION_MS,
    minTracks: Int = 4,
    fillWithFallbackTowardTarget: Boolean = false,
  ): List<MusicTrack> {
    if (primaryTracks.isEmpty() && fallbackTracks.isEmpty()) return emptyList()
    val selected = ArrayList<MusicTrack>()
    val seenIds = HashSet<String>()
    var accumulatedMs = 0L

    for (track in primaryTracks) {
      if (!seenIds.add(track.id)) continue
      val dur = track.durationMs.coerceAtLeast(1_000L)
      if (selected.size >= minTracks && accumulatedMs >= targetDurationMs) {
        break
      }
      if (
        selected.size >= minTracks &&
        accumulatedMs >= (targetDurationMs * 3L) / 4L &&
        accumulatedMs + dur > targetDurationMs &&
        (accumulatedMs + dur - targetDurationMs) > (targetDurationMs - accumulatedMs)
      ) {
        break
      }
      selected.add(track)
      accumulatedMs += dur
    }

    val shouldPullFallback = selected.size < minTracks ||
      (fillWithFallbackTowardTarget && accumulatedMs < (targetDurationMs * 85L) / 100L)
    if (shouldPullFallback && fallbackTracks.isNotEmpty()) {
      for (track in fallbackTracks) {
        if (!seenIds.add(track.id)) continue
        val dur = track.durationMs.coerceAtLeast(1_000L)
        val hasMinTracks = selected.size >= minTracks
        if (hasMinTracks && (!fillWithFallbackTowardTarget || accumulatedMs >= targetDurationMs)) {
          break
        }
        if (
          hasMinTracks &&
          fillWithFallbackTowardTarget &&
          accumulatedMs >= (targetDurationMs * 3L) / 4L &&
          accumulatedMs + dur > targetDurationMs &&
          (accumulatedMs + dur - targetDurationMs) > (targetDurationMs - accumulatedMs)
        ) {
          break
        }
        selected.add(track)
        accumulatedMs += dur
      }
    }

    return selected
  }

  private fun interleaveTrackLists(lists: List<List<MusicTrack>>): List<MusicTrack> {
    if (lists.isEmpty()) return emptyList()
    val maxLen = lists.maxOfOrNull { it.size } ?: 0
    val result = ArrayList<MusicTrack>()
    val seen = HashSet<String>()
    for (i in 0 until maxLen) {
      for (list in lists) {
        if (i < list.size) {
          val track = list[i]
          if (seen.add(track.id)) {
            result.add(track)
          }
        }
      }
    }
    return result
  }

  /**
   * Compares the text of [genres], groups genres with similar text together via
   * [groupGenresByTextSimilarity], and picks a few random genres (2 to 4) to mix out of a
   * similarly grouped text cluster.
   */
  internal fun buildGenreMashupClusters(
    genres: List<MusicGenre>,
    refreshGeneration: Int,
    random: kotlin.random.Random,
  ): Pair<List<MusicGenre>, List<MusicGenre>> {
    if (genres.isEmpty()) return emptyList<MusicGenre>() to emptyList()
    if (genres.size == 1) return genres to genres

    val textGroups = groupGenresByTextSimilarity(genres).ifEmpty { listOf(genres) }
    val safeGen = refreshGeneration.coerceAtLeast(0)

    fun pickRandomGenresFromGroup(group: List<MusicGenre>): List<MusicGenre> {
      if (group.size <= 2) return group.shuffled(random)
      val maxPick = minOf(4, group.size)
      val minPick = minOf(2, maxPick)
      val pickCount = if (maxPick > minPick) {
        random.nextInt(minPick, maxPick + 1)
      } else {
        minPick
      }
      return group.shuffled(random).take(pickCount)
    }

    val groupA = textGroups[safeGen % textGroups.size]
    val clusterA = pickRandomGenresFromGroup(groupA)
    val clusterANames = clusterA.map { it.name }.toSet()

    val nonOverlappingGroups = textGroups.filter { group ->
      group.none { it.name in clusterANames }
    }
    val groupBPool = nonOverlappingGroups.ifEmpty {
      textGroups.filter { group ->
        group.map { it.name }.toSet() != groupA.map { it.name }.toSet()
      }.ifEmpty { textGroups }
    }
    val groupB = groupBPool[(safeGen + 1) % groupBPool.size]
    val clusterB = pickRandomGenresFromGroup(groupB)
    return clusterA to clusterB
  }

  /**
   * Generates [HOME_PLAYLIST_COUNT] (6) curated Home playlists targeting approximately
   * [targetDurationMs] (~1 hour each, not strict):
   * 1. **Genre Mashup**: Compares the text of the genres, groups similar text together, and picks
   *    a few random genres out of a similarly grouped text cluster to mix.
   * 2. **Old Favorites**: Songs by favorite artists/albums/songs/genres that have been played a lot
   *    but not listened to in a good while.
   * 3. **Play Stats Powerhouse**: Uses play counts, full plays, total listen time, and low skips.
   * 4. **Favorites Spotlight**: Uses favorite songs, favorite artists, favorite albums, and favorite genres.
   * 5. **Stats & Favorites Rediscovery**: Mixes lesser-played hidden gems with favorite/top-played anchors.
   * 6. **The Everything Blender**: Mixes genres, play stats, and favorite artists/albums/songs all together.
   */
  fun generateHomePlaylists(
    allTracks: List<MusicTrack>,
    playStats: Map<String, TrackPlayStats> = emptyMap(),
    favoriteIds: Set<String> = emptySet(),
    favoriteArtists: Set<String> = emptySet(),
    favoriteAlbums: Set<String> = emptySet(),
    favoriteGenres: Set<String> = emptySet(),
    refreshGeneration: Int = 0,
    targetDurationMs: Long = TARGET_HOME_PLAYLIST_DURATION_MS,
    random: kotlin.random.Random = kotlin.random.Random(
      refreshGeneration.toLong() * 6364136223846793005L + 1442695040888963407L
    ),
    precomputedGenres: List<MusicGenre>? = null,
    precomputedSortedByStats: List<MusicTrack>? = null,
  ): List<MusicPlaylist> {
    val gen = refreshGeneration.coerceAtLeast(0)
    if (allTracks.isEmpty()) {
      return listOf(
        MusicPlaylist(id = "home_mix_1", name = "Genre Fusion #1", tracks = emptyList(), subtitle = "Genre Mashup • ~1 hr target", isGeneratedHomeMix = true),
        MusicPlaylist(id = "home_mix_2", name = "Old Favorites", tracks = emptyList(), subtitle = "Old Favorites • Played A Lot, Not Recently", isGeneratedHomeMix = true),
        MusicPlaylist(id = "home_mix_3", name = "Heavy Rotation Hour", tracks = emptyList(), subtitle = "Play Stats • Heavy Rotation", isGeneratedHomeMix = true),
        MusicPlaylist(id = "home_mix_4", name = "VIP Favorites Mix", tracks = emptyList(), subtitle = "Favorites • Artists, Albums & Songs", isGeneratedHomeMix = true),
        MusicPlaylist(id = "home_mix_5", name = "Rediscovery Radar", tracks = emptyList(), subtitle = "Play Stats & Favorites • Deep Cuts", isGeneratedHomeMix = true),
        MusicPlaylist(id = "home_mix_6", name = "Cosmic Everything Blender", tracks = emptyList(), subtitle = "All-In Mix • Genres, Stats & Favorites", isGeneratedHomeMix = true),
      )
    }

    val totalLibraryDurationMs = allTracks.sumOf { it.durationMs.coerceAtLeast(1_000L) }
    val fillTowardOneHour = totalLibraryDurationMs >= targetDurationMs / 2L
    val genres = precomputedGenres ?: buildGenres(allTracks)
    val (clusterA, _) = buildGenreMashupClusters(genres, gen, random)
    val favLookup = NormalizedFavoriteLookup(
      favoriteIds = favoriteIds,
      favoriteArtists = favoriteArtists,
      favoriteAlbums = favoriteAlbums,
      favoriteGenres = favoriteGenres,
    )

    // 1. Genre Mashup #1 (Compares genre text, groups similar text, and picks a few random genres from a text-similar group)
    val clusterANames = clusterA.map { it.name }
    val clusterALowerSet = clusterANames.mapTo(HashSet(clusterANames.size)) { it.lowercase(Locale.ROOT) }
    val mash1Primary = interleaveTrackLists(
      clusterA.map { g -> g.tracks.shuffled(random) }
    )
    val mash1Fallback = allTracks
      .filterNot { t -> t.genre.lowercase(Locale.ROOT) in clusterALowerSet }
      .shuffled(random)
    val mash1Tracks = selectTracksTowardTargetDuration(
      primaryTracks = mash1Primary,
      fallbackTracks = mash1Fallback,
      targetDurationMs = targetDurationMs,
      minTracks = minOf(4, allTracks.size),
      fillWithFallbackTowardTarget = fillTowardOneHour,
    )
    val mash1Label = when {
      clusterANames.size >= 2 -> clusterANames.take(3).joinToString(" × ")
      else -> clusterANames.firstOrNull() ?: "Multi-Genre"
    }
    val mash1Titles = listOf(
      "$mash1Label Fusion",
      "Wavelength Mashup: $mash1Label",
      "Neon & Pulse: $mash1Label",
      "$mash1Label Power Hour",
    )
    val playlist1 = MusicPlaylist(
      id = "home_mix_1",
      name = mash1Titles[gen % mash1Titles.size],
      tracks = mash1Tracks,
      subtitle = "Genre Mashup • ${clusterANames.joinToString(" & ").ifEmpty { "Mixed Genres" }}",
      isGeneratedHomeMix = true,
    )

    // 2. Old Favorites (Songs by favorite artists/albums/songs/genres played a lot but not listened to in a good while)
    val playedTimestamps = playStats.values.mapNotNull { stat ->
      stat.lastPlayedAtMs.takeIf { stat.hasBeenPlayed && it > 0L }
    }
    val maxLastPlayedAtMs = playedTimestamps.maxOrNull() ?: 0L
    val minLastPlayedAtMs = playedTimestamps.minOrNull() ?: 0L
    val timestampSpanMs = (maxLastPlayedAtMs - minLastPlayedAtMs).coerceAtLeast(1L)

    fun computeOldFavoriteScore(track: MusicTrack): Long {
      val stat = playStats[track.id] ?: return 0L
      if (!stat.hasBeenPlayed) return 0L
      val fullPlaysPoints = (stat.calculatedFullPlays(track.durationMs) * 60_000.0).roundToLong()
      val baseVolume = (stat.playCount.toLong() * 60_000L + fullPlaysPoints + stat.totalPlayTimeMs - stat.skipCount.toLong() * 25L)
        .coerceAtLeast(1_000L)
      val stalenessFraction = when {
        maxLastPlayedAtMs <= minLastPlayedAtMs -> 0.5
        stat.lastPlayedAtMs <= 0L -> 1.0
        else -> ((maxLastPlayedAtMs - stat.lastPlayedAtMs).coerceAtLeast(0L).toDouble() / timestampSpanMs.toDouble())
          .coerceIn(0.0, 1.0)
      }
      return (baseVolume.toDouble() * (0.35 + 1.65 * stalenessFraction)).roundToLong()
    }

    fun isNotListenedToRecently(track: MusicTrack): Boolean {
      val stat = playStats[track.id] ?: return false
      if (!stat.hasBeenPlayed) return false
      return maxLastPlayedAtMs <= minLastPlayedAtMs || stat.lastPlayedAtMs < maxLastPlayedAtMs
    }

    val shuffledForOldFavTieBreak = allTracks.shuffled(random)
    val trueOldFavorites = shuffledForOldFavTieBreak
      .filter { favLookup.isTrackOrCategoryFav(it) && playStats[it.id]?.hasBeenPlayed == true && isNotListenedToRecently(it) }
      .sortedByDescending { computeOldFavoriteScore(it) }
    val recentPlayedFavorites = shuffledForOldFavTieBreak
      .filter { favLookup.isTrackOrCategoryFav(it) && playStats[it.id]?.hasBeenPlayed == true && !isNotListenedToRecently(it) }
      .sortedByDescending { computeOldFavoriteScore(it) }
    val nonFavOldHeavyPlays = shuffledForOldFavTieBreak
      .filter { !favLookup.isTrackOrCategoryFav(it) && playStats[it.id]?.hasBeenPlayed == true && isNotListenedToRecently(it) }
      .sortedByDescending { computeOldFavoriteScore(it) }
    val unplayedFavorites = shuffledForOldFavTieBreak
      .filter { favLookup.isTrackOrCategoryFav(it) && playStats[it.id]?.hasBeenPlayed != true }

    val oldFavoritesPrimary = (trueOldFavorites + recentPlayedFavorites + nonFavOldHeavyPlays + unplayedFavorites)
      .distinctBy { it.id }
    val oldFavoritesFallback = shuffledForOldFavTieBreak
      .sortedByDescending { computeOldFavoriteScore(it) }
    val oldFavTracks = selectTracksTowardTargetDuration(
      primaryTracks = oldFavoritesPrimary.ifEmpty { oldFavoritesFallback.take(minOf(6, allTracks.size)) },
      fallbackTracks = oldFavoritesFallback,
      targetDurationMs = targetDurationMs,
      minTracks = minOf(4, allTracks.size),
      fillWithFallbackTowardTarget = fillTowardOneHour,
    )
    val oldFavTitles = listOf(
      "Old Favorites",
      "Time Capsule: Old Favorites",
      "Forgotten Favorites & Classics",
      "Long-Lost Favorites Replay",
    )
    val playlist2 = MusicPlaylist(
      id = "home_mix_2",
      name = oldFavTitles[gen % oldFavTitles.size],
      tracks = oldFavTracks,
      subtitle = "Old Favorites • Played A Lot, Not Recently",
      isGeneratedHomeMix = true,
    )

    // 3. Play Stats Powerhouse (Most played, high completion, low skips, smart-shuffled)
    val sortedByStats = precomputedSortedByStats ?: sortTracksByPlayStats(
      tracks = allTracks,
      playStats = playStats,
      favoriteIds = favoriteIds,
      favoriteArtists = favoriteArtists,
      favoriteAlbums = favoriteAlbums,
      favoriteGenres = favoriteGenres,
    )
    val playedTracks = sortedByStats.filter { playStats[it.id]?.hasBeenPlayed == true }
    val statsSeedPool = if (playedTracks.isNotEmpty()) {
      smartShuffleTracks(playedTracks, playStats, random) +
        smartShuffleTracks(sortedByStats.filterNot { playStats[it.id]?.hasBeenPlayed == true }, playStats, random)
    } else {
      smartShuffleTracks(sortedByStats, playStats, random)
    }
    val statsTargetCount = if (fillTowardOneHour) allTracks.size else minOf(maxOf(5, (allTracks.size * 2) / 3), allTracks.size)
    val statsTracks = selectTracksTowardTargetDuration(
      primaryTracks = statsSeedPool.take(statsTargetCount),
      fallbackTracks = statsSeedPool.drop(statsTargetCount),
      targetDurationMs = targetDurationMs,
      minTracks = minOf(5, allTracks.size),
      fillWithFallbackTowardTarget = fillTowardOneHour,
    )
    val statsTitles = listOf(
      "Heavy Rotation Hour",
      "On Repeat: Stats Powerhouse",
      "Zero-Skip Obsessions",
      "Top Replay Sessions",
    )
    val playlist3 = MusicPlaylist(
      id = "home_mix_3",
      name = statsTitles[gen % statsTitles.size],
      tracks = statsTracks,
      subtitle = "Play Stats • Heavy Rotation & Top Plays",
      isGeneratedHomeMix = true,
    )

    // 4. Favorites Spotlight (Favorite songs, favorite artists, favorite albums, and favorite genres)
    val directFavSongs = if (favLookup.favoriteIds.isEmpty()) {
      emptyList()
    } else {
      allTracks.filter { it.id in favLookup.favoriteIds }.shuffled(random)
    }
    val favArtistSongs = if (favLookup.artistLowerSet.isEmpty()) {
      emptyList()
    } else {
      allTracks.filter { favLookup.isArtistFav(it.artist) }.shuffled(random)
    }
    val favAlbumSongs = if (favLookup.albumExactLowerSet.isEmpty()) {
      emptyList()
    } else {
      allTracks.filter { favLookup.isAlbumFav(it.album, it.artist) }.shuffled(random)
    }
    val favGenreSongs = if (favLookup.genreLowerSet.isEmpty()) {
      emptyList()
    } else {
      allTracks.filter { favLookup.isGenreFav(it.genre) }.shuffled(random)
    }
    val interleavedFavorites = interleaveTrackLists(
      listOf(directFavSongs, favArtistSongs, favAlbumSongs, favGenreSongs).filter { it.isNotEmpty() }
    )
    // Also include album-mates and artist-mates of favorited songs
    val favoriteArtistNamesFromSongs = directFavSongs.mapTo(HashSet(directFavSongs.size)) { it.artist.lowercase(Locale.ROOT) }
    val favoriteAlbumKeysFromSongs = directFavSongs.mapTo(HashSet(directFavSongs.size)) {
      "${it.album.lowercase(Locale.ROOT)}|${it.artist.lowercase(Locale.ROOT)}"
    }
    val relatedToFavorites = if (favoriteArtistNamesFromSongs.isEmpty() && favoriteAlbumKeysFromSongs.isEmpty()) {
      emptyList()
    } else {
      allTracks.filter { track ->
        val artistLower = track.artist.lowercase(Locale.ROOT)
        artistLower in favoriteArtistNamesFromSongs ||
          "${track.album.lowercase(Locale.ROOT)}|$artistLower" in favoriteAlbumKeysFromSongs
      }.shuffled(random)
    }
    val favoritesPrimary = (interleavedFavorites + relatedToFavorites).distinctBy { it.id }
    val favoritesFallback = allTracks.shuffled(random)
    val favTracks = selectTracksTowardTargetDuration(
      primaryTracks = favoritesPrimary.ifEmpty { favoritesFallback.take(minOf(6, allTracks.size)) },
      fallbackTracks = favoritesFallback,
      targetDurationMs = targetDurationMs,
      minTracks = minOf(5, allTracks.size),
      fillWithFallbackTowardTarget = fillTowardOneHour,
    )
    val favTitles = listOf(
      "Starlight Vault: All Favorites",
      "VIP Pass: Favorite Artists & Albums",
      "Crown Jewels & Starred Cuts",
      "Hall of Fame: Favorites Mix",
    )
    val playlist4 = MusicPlaylist(
      id = "home_mix_4",
      name = favTitles[gen % favTitles.size],
      tracks = favTracks,
      subtitle = "Favorites • Artists, Albums & Songs",
      isGeneratedHomeMix = true,
    )

    // 5. Play Stats + Favorites Rediscovery Mix (Hidden gems interleaved with familiar favorites & recent plays)
    val lesserPlayed = allTracks
      .sortedWith(
        compareBy<MusicTrack> { playStats[it.id]?.playCount ?: 0 }
          .thenBy { playStats[it.id]?.totalPlayTimeMs ?: 0L }
          .thenBy { playStats[it.id]?.skipCount ?: 0 }
      )
      .take(maxOf(3, (allTracks.size + 1) / 2))
      .shuffled(random)
    val anchors = (interleavedFavorites + sortTracksByRecentlyPlayed(allTracks, playStats).take(maxOf(3, allTracks.size / 2)))
      .distinctBy { it.id }
      .shuffled(random)
    val rediscoveryPrimary = interleaveTrackLists(listOf(lesserPlayed, anchors))
    val rediscoveryTargetCount = if (fillTowardOneHour) allTracks.size else minOf(maxOf(5, (allTracks.size * 2) / 3), allTracks.size)
    val rediscoveryTracks = selectTracksTowardTargetDuration(
      primaryTracks = rediscoveryPrimary.take(rediscoveryTargetCount),
      fallbackTracks = allTracks.shuffled(random),
      targetDurationMs = targetDurationMs,
      minTracks = minOf(5, allTracks.size),
      fillWithFallbackTowardTarget = fillTowardOneHour,
    )
    val rediscoveryTitles = listOf(
      "Hidden Gems & Heavy Hitters",
      "Rediscovery Radar",
      "Deep Cuts & Familiar Favorites",
      "Crate Digger's Replay Mix",
    )
    val playlist5 = MusicPlaylist(
      id = "home_mix_5",
      name = rediscoveryTitles[gen % rediscoveryTitles.size],
      tracks = rediscoveryTracks,
      subtitle = "Stats & Favorites • Deep Cuts + Replay",
      isGeneratedHomeMix = true,
    )

    // 6. The Everything Mega-Blender (Mixes genres, play stats, and favorites all together!)
    val genreShuffledStreams = genres.shuffled(random).map { it.tracks.shuffled(random) }
    val everythingPrimary = interleaveTrackLists(
      listOf(
        interleavedFavorites.shuffled(random),
        smartShuffleTracks(allTracks, playStats, random),
        interleaveTrackLists(genreShuffledStreams),
      ).filter { it.isNotEmpty() }
    )
    val everythingTracks = selectTracksTowardTargetDuration(
      primaryTracks = everythingPrimary,
      fallbackTracks = allTracks.shuffled(random),
      targetDurationMs = targetDurationMs,
      minTracks = minOf(6, allTracks.size),
      fillWithFallbackTowardTarget = true,
    )
    val everythingTitles = listOf(
      "Cosmic Blender: The Everything Mix",
      "Sonic Kaleidoscope: All-In Mix",
      "Chaos & Harmony: Full Mashup",
      "Hyperdrive Jukebox: Everything Mix",
    )
    val playlist6 = MusicPlaylist(
      id = "home_mix_6",
      name = everythingTitles[gen % everythingTitles.size],
      tracks = everythingTracks,
      subtitle = "Everything Mix • Genres, Stats & Favorites",
      isGeneratedHomeMix = true,
    )

    return listOf(playlist1, playlist2, playlist3, playlist4, playlist5, playlist6)
  }

  /**
   * Serializes an ordered list of [MusicLibraryTab]s for `SharedPreferences`, keeping
   * [MusicLibraryTab.HOME] fixed at the front.
   */
  fun serializeTabOrder(tabs: List<MusicLibraryTab>): String {
    val others = (tabs.distinct() + MusicLibraryTab.entries.filterNot { it in tabs })
      .filterNot { it == MusicLibraryTab.HOME }
    val normalized = listOf(MusicLibraryTab.HOME) + others
    return normalized.joinToString("|") { it.name }
  }

  /**
   * Moves the library section tab at [fromIndex] to [toIndex] in [tabs], returning the new order.
   * [MusicLibraryTab.HOME] is always fixed at index `0` at the front of the list: attempting to
   * move index `0` returns the list unchanged, and moving any other tab to index `<= 0` places it
   * at index `1` right after [MusicLibraryTab.HOME].
   */
  fun reorderTabs(
    tabs: List<MusicLibraryTab>,
    fromIndex: Int,
    toIndex: Int,
  ): List<MusicLibraryTab> {
    val others = (if (tabs.isEmpty()) MusicLibraryTab.entries.toList() else tabs)
      .distinct()
      .filterNot { it == MusicLibraryTab.HOME }
    val missingOthers = MusicLibraryTab.entries.filterNot { it == MusicLibraryTab.HOME || it in others }
    val normalized = listOf(MusicLibraryTab.HOME) + others + missingOthers
    if (fromIndex <= 0 || fromIndex !in normalized.indices) {
      return normalized
    }
    val clampedTarget = toIndex.coerceIn(1, normalized.lastIndex)
    if (fromIndex == clampedTarget) {
      return normalized
    }
    val mutable = normalized.toMutableList()
    val moved = mutable.removeAt(fromIndex)
    mutable.add(clampedTarget, moved)
    return mutable.toList()
  }

  /**
   * Resolves the index of the artist corresponding to [currentTrack] in [artists], or `-1` if none.
   */
  fun resolveFocusedArtistIndex(
    artists: List<MusicArtist>,
    currentTrack: MusicTrack? = null,
    currentArtistName: String? = currentTrack?.artist,
  ): Int {
    if (artists.isEmpty()) return -1
    val trackId = currentTrack?.id
    if (!trackId.isNullOrBlank()) {
      val byTrackId = artists.indexOfFirst { artist ->
        artist.containsTrack(trackId)
      }
      if (byTrackId >= 0) return byTrackId
    }
    if (!currentArtistName.isNullOrBlank()) {
      return artists.indexOfFirst { artist ->
        artist.name.equals(currentArtistName, ignoreCase = true)
      }
    }
    return -1
  }

  /**
   * Resolves the index of the album corresponding to [currentTrack] in [albums], or `-1` if none.
   */
  fun resolveFocusedAlbumIndex(
    albums: List<MusicAlbum>,
    currentTrack: MusicTrack? = null,
    currentAlbumName: String? = currentTrack?.album,
    currentArtistName: String? = currentTrack?.artist,
  ): Int {
    if (albums.isEmpty()) return -1
    val trackId = currentTrack?.id
    if (!trackId.isNullOrBlank()) {
      val byTrackId = albums.indexOfFirst { album ->
        album.containsTrack(trackId)
      }
      if (byTrackId >= 0) return byTrackId
    }
    if (!currentAlbumName.isNullOrBlank()) {
      if (!currentArtistName.isNullOrBlank()) {
        val byAlbumAndArtist = albums.indexOfFirst { album ->
          album.name.equals(currentAlbumName, ignoreCase = true) &&
            album.artist.equals(currentArtistName, ignoreCase = true)
        }
        if (byAlbumAndArtist >= 0) return byAlbumAndArtist
      }
      return albums.indexOfFirst { album ->
        album.name.equals(currentAlbumName, ignoreCase = true)
      }
    }
    return -1
  }

  /**
   * Resolves the index of the genre corresponding to [currentTrack] in [genres], or `-1` if none.
   */
  fun resolveFocusedGenreIndex(
    genres: List<MusicGenre>,
    currentTrack: MusicTrack? = null,
    currentGenreName: String? = currentTrack?.genre,
  ): Int {
    if (genres.isEmpty()) return -1
    val trackId = currentTrack?.id
    if (!trackId.isNullOrBlank()) {
      val byTrackId = genres.indexOfFirst { genre ->
        genre.containsTrack(trackId)
      }
      if (byTrackId >= 0) return byTrackId
    }
    if (!currentGenreName.isNullOrBlank()) {
      return genres.indexOfFirst { genre ->
        genre.name.equals(currentGenreName, ignoreCase = true)
      }
    }
    return -1
  }

  /**
   * Resolves the index of the track matching [currentTrackId] in [tracks], or `-1` if none.
   */
  fun resolveFocusedTrackIndex(
    tracks: List<MusicTrack>,
    currentTrackId: String?,
  ): Int {
    if (currentTrackId.isNullOrBlank() || tracks.isEmpty()) return -1
    return tracks.indexOfFirst { it.id == currentTrackId }
  }

  /**
   * Resolves the index of the playlist containing [currentTrackId] in [playlists], prioritizing
   * user-saved playlists and Favorites over the catch-all Most Played playlist, or `-1` if none.
   */
  fun resolveFocusedPlaylistIndex(
    playlists: List<MusicPlaylist>,
    currentTrackId: String?,
  ): Int {
    if (currentTrackId.isNullOrBlank() || playlists.isEmpty()) return -1
    val savedMatch = playlists.indexOfFirst { playlist ->
      !playlist.isBuiltIn && playlist.containsTrack(currentTrackId)
    }
    if (savedMatch >= 0) return savedMatch
    val favoritesMatch = playlists.indexOfFirst { playlist ->
      playlist.isFavorites && playlist.containsTrack(currentTrackId)
    }
    if (favoritesMatch >= 0) return favoritesMatch
    return playlists.indexOfFirst { playlist ->
      playlist.containsTrack(currentTrackId)
    }
  }

  /**
   * Resolves the index of the currently playing track in [queue], or `-1` if [queue] is empty.
   */
  fun resolveFocusedQueueIndex(
    queue: List<MusicTrack>,
    currentQueueIndex: Int,
    currentTrackId: String? = null,
  ): Int {
    if (queue.isEmpty()) return -1
    if (currentQueueIndex in queue.indices) {
      if (currentTrackId.isNullOrBlank() || queue[currentQueueIndex].id == currentTrackId) {
        return currentQueueIndex
      }
    }
    if (!currentTrackId.isNullOrBlank()) {
      val byId = queue.indexOfFirst { it.id == currentTrackId }
      if (byId >= 0) return byId
    }
    return currentQueueIndex.coerceIn(0, queue.lastIndex)
  }

  /**
   * Synthesizes a valid, melodic 16-bit PCM WAV file for a built-in sample [track] so `MediaPlayer`
   * can play real musical chords and progressions offline.
   */
  fun ensurePlayableSampleWavFile(
    cacheDir: File,
    track: MusicTrack,
    targetDir: File? = null,
    fileName: String = "${track.id}.wav",
  ): File {
    val sampleDir = (targetDir ?: File(cacheDir, "music_player_samples")).apply { mkdirs() }
    val targetFile = File(sampleDir, fileName)
    if (targetFile.exists() && targetFile.length() > 1024L) {
      return targetFile
    }

    val sampleRate = 22050
    val durationSeconds = (track.durationMs / 1000L).toInt().coerceIn(4, 25)
    val totalSamples = sampleRate * durationSeconds
    val pcmBytes = ByteArray(totalSamples * 2)

    val baseFreq = track.sampleBaseFreqHz
    // Major/minor triad ratios for a pleasant ambient chord progression
    val chordRatios = listOf(
      doubleArrayOf(1.0, 1.25, 1.5),       // I chord
      doubleArrayOf(0.8409, 1.0595, 1.25), // VI / IV chord
      doubleArrayOf(1.1225, 1.3348, 1.6818), // V / ii chord
      doubleArrayOf(1.0, 1.25, 1.5),       // I chord
    )

    val samplesPerChord = max(1, totalSamples / chordRatios.size)
    for (i in 0 until totalSamples) {
      val chordIndex = (i / samplesPerChord).coerceIn(0, chordRatios.lastIndex)
      val ratios = chordRatios[chordIndex]
      val t = i.toDouble() / sampleRate.toDouble()

      // Smooth envelope per chord
      val posInChord = (i % samplesPerChord).toDouble() / samplesPerChord.toDouble()
      val env = when {
        posInChord < 0.08 -> posInChord / 0.08
        posInChord > 0.88 -> (1.0 - posInChord) / 0.12
        else -> 1.0
      }

      val wave = (
        sin(2.0 * PI * baseFreq * ratios[0] * t) * 0.45 +
          sin(2.0 * PI * baseFreq * ratios[1] * t) * 0.30 +
          sin(2.0 * PI * baseFreq * ratios[2] * t) * 0.25
        ) * env

      val sampleShort = (wave * 11000.0).roundToInt().coerceIn(-32767, 32767).toShort()
      pcmBytes[i * 2] = (sampleShort.toInt() and 0xFF).toByte()
      pcmBytes[i * 2 + 1] = ((sampleShort.toInt() shr 8) and 0xFF).toByte()
    }

    FileOutputStream(targetFile).use { fos ->
      val header = AudioRecorderEngine.createWavHeader(
        pcmDataLength = pcmBytes.size.toLong(),
        sampleRate = sampleRate,
        channels = 1,
        bitsPerSample = 16,
      )
      fos.write(header)
      fos.write(pcmBytes)
      fos.flush()
    }
    return targetFile
  }

  private val sessionLock = Any()
  private var activePlaybackSession: MusicPlaybackSession? = null
  private var activeActivityRef: WeakReference<Activity>? = null
  private var activeLifecycleObserver: LifecycleEventObserver? = null

  /**
   * Currently active [MusicPlaybackSession], observable by Compose UI.
   */
  var activeSessionState by mutableStateOf<MusicPlaybackSession?>(null)
    internal set

  /**
   * Currently active [MusicTrack] across the Music Player session, observable by Compose UI
   * (including [com.example.toolbox.ui.theme.ToolboxTheme] and Settings).
   */
  var activeTrack by mutableStateOf<MusicTrack?>(DEFAULT_SAMPLE_TRACKS.firstOrNull())
    internal set

  private var internalTrackPalette by mutableStateOf(resolveTrackPalette(DEFAULT_SAMPLE_TRACKS.firstOrNull()))

  /**
   * Currently active external app media session reported by the Android system media player
   * (via [MediaSessionManager] or [ToolboxMediaListenerService]), if any.
   */
  var externalSystemMediaInfo by mutableStateOf<SystemMediaPlayingInfo?>(null)
    internal set

  /**
   * Currently active [AlbumArtPalette] for the Android system media player (either the built-in
   * Music Player or an external media app controlling the Android system media player), observable
   * by Compose UI so the app's theme/accent colors dynamically follow whatever media is playing.
   */
  var activeTrackPalette: AlbumArtPalette
    get() {
      val internalPlaying = activeSessionState?.let { !it.isReleased && it.isPlaying && it.currentTrack != null } == true
      val external = externalSystemMediaInfo?.takeUnless { isIgnoredPhoneOrCallSystemMediaInfo(it) }
      val internalPalette = internalTrackPalette
      if (internalPlaying || isMusicPlayingOverride == true) return internalPalette
      if (external != null && external.isPlaying) return external.palette
      if (external != null) return external.palette
      return internalPalette
    }
    internal set(value) {
      internalTrackPalette = value
    }

  /**
   * Optional test override for [isMusicPlaying]. When `null`, [isMusicPlaying] reflects the active
   * [MusicPlaybackSession] or external Android system media player playback state.
   */
  var isMusicPlayingOverride by mutableStateOf<Boolean?>(null)
    internal set

  /**
   * Whether media is currently actively playing in either the built-in Music Player or an external
   * app via the Android system media player, observable by Compose UI
   * (such as [com.example.toolbox.ui.theme.ToolboxTheme] and Settings).
   */
  val isMusicPlaying: Boolean
    get() {
      val override = isMusicPlayingOverride
      val internalPlaying = activeSessionState?.let { !it.isReleased && it.isPlaying && it.currentTrack != null } == true
      val externalPlaying = externalSystemMediaInfo
        ?.takeUnless { isIgnoredPhoneOrCallSystemMediaInfo(it) }
        ?.isPlaying == true
      return override ?: (internalPlaying || externalPlaying)
    }

  /**
   * Returns a [SystemMediaPlayingInfo] describing whatever media is currently active in the
   * Android system media player (built-in Music Player or external media app).
   */
  val activeSystemMediaInfo: SystemMediaPlayingInfo?
    get() {
      val internalSession = activeSessionState
      val internalTrack = internalSession?.currentTrack
      val external = externalSystemMediaInfo?.takeUnless { isIgnoredPhoneOrCallSystemMediaInfo(it) }
      val internalPalette = internalTrackPalette
      if (internalSession != null && !internalSession.isReleased && internalSession.isPlaying && internalTrack != null) {
        return SystemMediaPlayingInfo(
          title = internalTrack.title,
          artist = internalTrack.artist,
          album = internalTrack.album,
          genre = internalTrack.genre,
          sourcePackage = "com.example.toolbox",
          sourceAppName = "Toolbox Music",
          isExternalApp = false,
          isPlaying = true,
          palette = internalPalette,
        )
      }
      if (external != null && external.isPlaying && isMusicPlayingOverride != true) {
        return external
      }
      if (external != null && isMusicPlayingOverride != true) {
        return external
      }
      if (internalTrack != null) {
        return SystemMediaPlayingInfo(
          title = internalTrack.title,
          artist = internalTrack.artist,
          album = internalTrack.album,
          genre = internalTrack.genre,
          sourcePackage = "com.example.toolbox",
          sourceAppName = "Toolbox Music",
          isExternalApp = false,
          isPlaying = internalSession.isPlaying || isMusicPlayingOverride == true,
          palette = internalPalette,
        )
      }
      if (external != null) {
        return external
      }
      val fallbackTrack = activeTrack ?: return null
      return SystemMediaPlayingInfo(
        title = fallbackTrack.title,
        artist = fallbackTrack.artist,
        album = fallbackTrack.album,
        genre = fallbackTrack.genre,
        sourcePackage = "com.example.toolbox",
        sourceAppName = "Toolbox Music",
        isExternalApp = false,
        isPlaying = isMusicPlayingOverride == true,
        palette = internalPalette,
      )
    }

  @Volatile
  internal var lastSyncedMediaWidgetPalette: TodoWidgetPalette? = null

  @Volatile
  internal var mediaMusicWidgetRefreshCount: Int = 0

  @Volatile
  internal var mediaThemeWidgetsRefreshCount: Int = 0

  /**
   * Refreshes pinned Toolbox Home Screen AppWidgets whenever active media playback state, track,
   * or artwork palette changes:
   * - Always refreshes [MusicPlayerWidgets].
   * - Only refreshes the non-music theme-synchronized tool widgets ([TodoListWidgets],
   *   [NotesWidgets], [BackgroundTextureWidgets], etc.) when [forceThemeWidgetsRefresh] is `true`
   *   (e.g. system configuration/wallpaper change) or when the effective [TodoWidgetPalette] has
   *   actually changed (such as on a track change or playback start/stop when Music Player theme
   *   colors are active).
   */
  internal fun refreshAllWidgetsForMediaStateChange(
    context: Context? = monitoredAppContext,
    forceThemeWidgetsRefresh: Boolean = false,
  ) {
    mediaMusicWidgetRefreshCount += 1
    val currentPalette = try {
      TodoListWidgets.resolveWidgetPalette(context)
    } catch (_: Throwable) {
      null
    }
    val shouldRefreshThemeWidgets = forceThemeWidgetsRefresh ||
      currentPalette == null ||
      lastSyncedMediaWidgetPalette != currentPalette
    if (currentPalette != null) {
      lastSyncedMediaWidgetPalette = currentPalette
    }
    if (shouldRefreshThemeWidgets) {
      mediaThemeWidgetsRefreshCount += 1
    }
    val ctx = context?.applicationContext ?: context ?: monitoredAppContext ?: return
    if (shouldRefreshThemeWidgets) {
      TodoListWidgets.refreshAllToolboxWidgets(ctx)
    } else {
      MusicPlayerWidgets.updateAllWidgets(ctx)
    }
  }

  /**
   * Sets or clears a test override for [isMusicPlaying].
   */
  fun setMusicPlayingForTest(isPlaying: Boolean?) {
    val changed = isMusicPlayingOverride != isPlaying
    isMusicPlayingOverride = isPlaying
    if (changed) {
      refreshAllWidgetsForMediaStateChange(monitoredAppContext)
    }
  }

  /**
   * Updates the observable [activeTrack] and [activeTrackPalette] for the Music Player.
   */
  fun updateActiveTrackPalette(
    track: MusicTrack?,
    palette: AlbumArtPalette = resolveTrackPalette(track),
  ) {
    val changed = activeTrack?.id != track?.id || internalTrackPalette != palette
    activeTrack = track
    internalTrackPalette = palette
    if (changed) {
      refreshAllWidgetsForMediaStateChange(monitoredAppContext)
    }
  }

  private val externalMediaCacheLock = Any()
  private val externalPaletteCache = object : LinkedHashMap<String, AlbumArtPalette>(32, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AlbumArtPalette>?): Boolean = size > 32
  }
  private val externalNormalizedBitmapCache = object : LinkedHashMap<String, Bitmap>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean = size > 16
  }
  private val externalFallbackArtworkCache = object : LinkedHashMap<String, Bitmap>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean = size > 16
  }
  private val externalMissingArtworkCache = object : LinkedHashMap<String, Boolean>(32, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean = size > 32
  }

  @Volatile
  private var externalMediaSetFromSystemSync = false

  @Volatile
  private var lastBroadcastReceivedElapsedMs = 0L

  private fun clearExternalMediaArtworkCaches() {
    synchronized(externalMediaCacheLock) {
      externalPaletteCache.clear()
      externalNormalizedBitmapCache.clear()
      externalFallbackArtworkCache.clear()
      externalMissingArtworkCache.clear()
    }
  }

  private fun externalBitmapCacheSignature(bitmap: Bitmap): String {
    val id = System.identityHashCode(bitmap)
    val genId = try {
      bitmap.generationId
    } catch (_: Throwable) {
      0
    }
    return "$id:$genId:${bitmap.width}x${bitmap.height}"
  }

  private fun normalizeExternalArtworkBitmap(bitmap: Bitmap?): Bitmap? {
    if (bitmap == null) return null
    return try {
      if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
      val sig = externalBitmapCacheSignature(bitmap)
      synchronized(externalMediaCacheLock) {
        externalNormalizedBitmapCache[sig]?.takeIf { !it.isRecycled }?.let { return it }
      }
      val normalized = scaleAndNormalizeSoftwareBitmap(bitmap, maxDimPx = 360) ?: return null
      synchronized(externalMediaCacheLock) {
        externalNormalizedBitmapCache[sig] = normalized
        val normSig = externalBitmapCacheSignature(normalized)
        externalNormalizedBitmapCache[normSig] = normalized
      }
      normalized
    } catch (_: Throwable) {
      bitmap
    }
  }

  internal fun areBitmapsVisuallyEquivalent(a: Bitmap?, b: Bitmap?): Boolean {
    if (a === b) return true
    if (a == null || b == null) return false
    return try {
      if (a.isRecycled || b.isRecycled) return false
      if (a.generationId == b.generationId) return true
      val w = a.width
      val h = a.height
      if (w <= 0 || h <= 0 || w != b.width || h != b.height) return false
      val sampleSteps = 4
      for (iy in 0..sampleSteps) {
        val y = ((h - 1) * iy / sampleSteps).coerceIn(0, h - 1)
        for (ix in 0..sampleSteps) {
          val x = ((w - 1) * ix / sampleSteps).coerceIn(0, w - 1)
          if (a.getPixel(x, y) != b.getPixel(x, y)) return false
        }
      }
      true
    } catch (_: Throwable) {
      false
    }
  }

  /**
   * Extracts an [AlbumArtPalette] for an external app playing in the Android system media player,
   * using its artwork [Bitmap] when available, or notification color, or deterministic metadata seed.
   */
  fun resolveSystemMediaPalette(
    artworkBitmap: Bitmap? = null,
    notificationColorRgb: Int? = null,
    title: String = "",
    artist: String = "",
    album: String = "",
    genre: String = "",
  ): AlbumArtPalette {
    if (artworkBitmap != null) {
      val validDims = try {
        !artworkBitmap.isRecycled && artworkBitmap.width > 0 && artworkBitmap.height > 0
      } catch (_: Throwable) {
        false
      }
      if (validDims) {
        val cacheKey = "bmp:${externalBitmapCacheSignature(artworkBitmap)}"
        synchronized(externalMediaCacheLock) {
          externalPaletteCache[cacheKey]?.let { return it }
        }
        val safeBitmap = normalizeExternalArtworkBitmap(artworkBitmap) ?: artworkBitmap
        val extracted = extractPaletteFromBitmap(safeBitmap)
        synchronized(externalMediaCacheLock) {
          externalPaletteCache[cacheKey] = extracted
          if (safeBitmap !== artworkBitmap) {
            externalPaletteCache["bmp:${externalBitmapCacheSignature(safeBitmap)}"] = extracted
          }
        }
        return extracted
      }
    }
    if (notificationColorRgb != null && (notificationColorRgb and 0xFFFFFF) != 0) {
      val primary = notificationColorRgb and 0xFFFFFF
      val r = (primary shr 16) and 0xFF
      val g = (primary shr 8) and 0xFF
      val b = primary and 0xFF
      val secondary = ((r * 65 / 100) shl 16) or ((g * 65 / 100) shl 8) or (b * 65 / 100)
      val accent = (min(255, r + 55) shl 16) or (min(255, g + 55) shl 8) or min(255, b + 55)
      return buildPaletteFromRgb(primary, secondary, accent)
    }
    val (pRgb, sRgb, aRgb) = computeDeterministicArtworkColors(
      album = album.ifBlank { title.ifBlank { "System Media" } },
      artist = artist.ifBlank { "External Player" },
      genre = genre,
    )
    return buildPaletteFromRgb(pRgb, sRgb, aRgb)
  }

  internal fun safeElapsedRealtimeMs(): Long {
    return try {
      SystemClock.elapsedRealtime()
    } catch (_: Throwable) {
      0L
    }
  }

  /**
   * Package names of system Phone, Dialer, Telecom, and In-Call UI components whose media sessions,
   * ringtones, call audio streams, and call notifications must be ignored by the Music Player.
   */
  val IGNORED_PHONE_AND_CALL_PACKAGES: Set<String> = setOf(
    "com.google.android.dialer",
    "com.android.dialer",
    "com.android.phone",
    "com.android.server.telecom",
    "com.android.incallui",
    "com.android.contacts",
    "com.android.providers.telephony",
    "com.android.stk",
    "com.android.cellbroadcastreceiver",
    "com.google.android.cellbroadcastreceiver",
    "com.google.android.ims",
    "com.sec.imsservice",
    "com.samsung.android.dialer",
    "com.samsung.android.incallui",
    "com.samsung.android.app.telephonyui",
    "com.oneplus.dialer",
    "com.oplus.dialer",
    "com.coloros.phonemanager",
    "com.miui.incallui",
    "com.xiaomi.incallui",
    "com.huawei.contacts",
    "com.motorola.dialer",
    "com.asus.dialer",
    "com.lge.ltecall",
    "com.sonyericsson.android.socialphonebook",
    "com.qualcomm.qti.telephonyservice",
    "com.sh.smart.caller",
    "com.truecaller",
  )

  /**
   * Application labels of Phone, Dialer, Telecom, and Call management components that should never
   * be displayed as an external media player in the Music Player.
   */
  val IGNORED_PHONE_AND_CALL_APP_NAMES: Set<String> = setOf(
    "phone",
    "phone app",
    "phone services",
    "phone service",
    "phone call",
    "phone calls",
    "google phone",
    "samsung phone",
    "dialer",
    "phone & messaging storage",
    "call",
    "calls",
    "call management",
    "call services",
    "call screen",
    "call screening",
    "incallui",
    "in-call ui",
    "telecom",
    "telephony",
    "telephony ui",
    "carrier services",
    "incoming call",
    "incoming phone call",
    "ongoing call",
    "ongoing phone call",
    "active call",
    "voice call",
    "wi-fi calling",
  )

  /**
   * Call status titles and subtitles emitted by phone/telecom notifications or call sessions.
   */
  val IGNORED_PHONE_AND_CALL_STATUS_TEXTS: Set<String> = setOf(
    "incoming call",
    "incoming phone call",
    "incoming voice call",
    "ongoing call",
    "ongoing phone call",
    "ongoing voice call",
    "active call",
    "call in progress",
    "phone call",
    "voice call",
    "ringing",
    "ringing...",
    "calling...",
    "dialing",
    "dialing...",
    "on hold",
    "call on hold",
  )

  /**
   * Returns `true` if [usage] is an [AudioAttributes] usage for phone calls, ringtones, alarms,
   * or system notifications rather than media playback.
   */
  fun isNonMediaCallOrRingtoneUsage(usage: Int): Boolean {
    return usage == AudioAttributes.USAGE_VOICE_COMMUNICATION ||
      usage == AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING ||
      usage == AudioAttributes.USAGE_NOTIFICATION_RINGTONE ||
      usage == AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST ||
      usage == AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT ||
      usage == AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_DELAYED ||
      usage == AudioAttributes.USAGE_NOTIFICATION ||
      usage == AudioAttributes.USAGE_NOTIFICATION_EVENT ||
      usage == AudioAttributes.USAGE_ALARM ||
      usage == AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
  }

  /**
   * Returns `true` if [mode] is an [AudioManager] mode corresponding to an incoming or active
   * phone/communication call.
   */
  fun isPhoneCallAudioMode(mode: Int): Boolean {
    return mode == AudioManager.MODE_RINGTONE ||
      mode == AudioManager.MODE_IN_CALL ||
      mode == AudioManager.MODE_IN_COMMUNICATION ||
      mode == AudioManager.MODE_CALL_SCREENING ||
      mode == AudioManager.MODE_CALL_REDIRECT ||
      mode == AudioManager.MODE_COMMUNICATION_REDIRECT
  }

  /**
   * Returns `true` if the device is currently ringing for an incoming phone call or in an active
   * phone call.
   */
  fun isPhoneCallActiveOrRinging(context: Context? = monitoredAppContext): Boolean {
    if (context == null) return false
    try {
      val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
      if (audioManager != null && isPhoneCallAudioMode(audioManager.mode)) {
        return true
      }
    } catch (_: Throwable) {}
    try {
      val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
      @Suppress("DEPRECATION")
      val callState = telephonyManager?.callState ?: android.telephony.TelephonyManager.CALL_STATE_IDLE
      if (callState != android.telephony.TelephonyManager.CALL_STATE_IDLE) {
        return true
      }
    } catch (_: Throwable) {}
    return false
  }

  /**
   * Returns `true` if [packageName] belongs to a Phone, Dialer, Telecom, or In-Call UI app that
   * must be ignored by the Music Player's external Android system media player integration.
   */
  fun isIgnoredPhoneOrCallPackage(
    context: Context? = monitoredAppContext,
    packageName: String?,
  ): Boolean {
    val cleanPkg = packageName?.trim()?.lowercase(Locale.ROOT).orEmpty()
    if (cleanPkg.isEmpty()) return false
    if (cleanPkg in IGNORED_PHONE_AND_CALL_PACKAGES) return true
    if (
      cleanPkg.contains("dialer") ||
      cleanPkg.contains("telecom") ||
      cleanPkg.contains("incallui") ||
      cleanPkg.contains("incall") ||
      cleanPkg.contains("telephony") ||
      cleanPkg.contains("phonecall") ||
      cleanPkg.contains("callscreen") ||
      cleanPkg.contains("callmanagement") ||
      cleanPkg == "phone" ||
      cleanPkg.endsWith(".phone") ||
      cleanPkg.endsWith(".caller") ||
      cleanPkg.endsWith(".call") ||
      cleanPkg.endsWith(".calls")
    ) {
      return true
    }
    if (context != null) {
      try {
        val telecom = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
        if (telecom != null) {
          val defaultDialer = try { telecom.defaultDialerPackage?.trim()?.lowercase(Locale.ROOT) } catch (_: Throwable) { null }
          if (!defaultDialer.isNullOrEmpty() && cleanPkg == defaultDialer) return true
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val systemDialer = try { telecom.systemDialerPackage?.trim()?.lowercase(Locale.ROOT) } catch (_: Throwable) { null }
            if (!systemDialer.isNullOrEmpty() && cleanPkg == systemDialer) return true
          }
        }
      } catch (_: Throwable) {}
      try {
        val pm = context.packageManager
        if (pm != null) {
          val appInfo = pm.getApplicationInfo(packageName!!.trim(), 0)
          val label = pm.getApplicationLabel(appInfo)?.toString()?.trim().orEmpty()
          if (label.isNotEmpty() && isIgnoredPhoneOrCallAppName(label)) {
            return true
          }
        }
      } catch (_: Throwable) {}
    }
    return false
  }

  /**
   * Returns `true` if [appName] is a Phone, Dialer, Telecom, or Call management label.
   */
  fun isIgnoredPhoneOrCallAppName(appName: String?): Boolean {
    val clean = appName?.trim()?.lowercase(Locale.ROOT).orEmpty()
    if (clean.isEmpty()) return false
    return clean in IGNORED_PHONE_AND_CALL_APP_NAMES
  }

  /**
   * Returns `true` if the given external media metadata represents a phone call or the Phone/Dialer
   * portion of the Android system media player.
   */
  fun isIgnoredPhoneOrCallMediaMetadata(
    title: String?,
    artist: String? = "",
    album: String? = "",
    sourcePackage: String? = "",
    sourceAppName: String? = "",
    context: Context? = monitoredAppContext,
  ): Boolean {
    if (isIgnoredPhoneOrCallPackage(context, sourcePackage)) return true
    if (isIgnoredPhoneOrCallAppName(sourceAppName)) return true
    val cleanTitle = title?.trim()?.lowercase(Locale.ROOT).orEmpty()
    val cleanArtist = artist?.trim()?.lowercase(Locale.ROOT).orEmpty()
    val cleanAlbum = album?.trim()?.lowercase(Locale.ROOT).orEmpty()
    val cleanPkg = sourcePackage?.trim().orEmpty()
    val cleanApp = sourceAppName?.trim().orEmpty()

    if (cleanTitle in IGNORED_PHONE_AND_CALL_STATUS_TEXTS) return true
    if (cleanArtist in IGNORED_PHONE_AND_CALL_STATUS_TEXTS) return true
    if (cleanAlbum in IGNORED_PHONE_AND_CALL_STATUS_TEXTS) return true

    if (isIgnoredPhoneOrCallAppName(cleanTitle)) {
      val hasKnownMusicSource = cleanPkg.isNotEmpty() && !isIgnoredPhoneOrCallPackage(context, cleanPkg) &&
        cleanApp.isNotEmpty() && !isIgnoredPhoneOrCallAppName(cleanApp)
      val hasSongMetadata = cleanArtist.isNotEmpty() || cleanAlbum.isNotEmpty()
      if (!hasKnownMusicSource || !hasSongMetadata) {
        return true
      }
    }
    return false
  }

  /**
   * Returns `true` if [info] represents a phone call or the Phone/Dialer portion of the Android
   * system media player that should be ignored by the Music Player.
   */
  fun isIgnoredPhoneOrCallSystemMediaInfo(
    info: SystemMediaPlayingInfo?,
    context: Context? = monitoredAppContext,
  ): Boolean {
    if (info == null || !info.isExternalApp) return false
    return isIgnoredPhoneOrCallMediaMetadata(
      title = info.title,
      artist = info.artist,
      album = info.album,
      sourcePackage = info.sourcePackage,
      sourceAppName = info.sourceAppName,
      context = context,
    )
  }

  /**
   * Returns `true` if [controller] is a [MediaController] belonging to the Phone, Dialer, Telecom
   * headset button handler, or a voice call / ringtone stream.
   */
  internal fun isIgnoredPhoneOrCallMediaController(
    context: Context? = monitoredAppContext,
    controller: MediaController,
  ): Boolean {
    val pkg = try { controller.packageName } catch (_: Throwable) { null }
    if (isIgnoredPhoneOrCallPackage(context, pkg)) return true

    val tag = try { controller.tag } catch (_: Throwable) { null }?.trim()?.lowercase(Locale.ROOT).orEmpty()
    if (
      tag == "headsetmediabutton" ||
      tag.contains("telecom") ||
      tag.contains("incall") ||
      tag.contains("phonecall") ||
      tag.contains("call_session")
    ) {
      return true
    }

    try {
      val audioAttrs = controller.playbackInfo?.audioAttributes
      if (audioAttrs != null) {
        if (isNonMediaCallOrRingtoneUsage(audioAttrs.usage)) return true
        if (audioAttrs.contentType == AudioAttributes.CONTENT_TYPE_SONIFICATION) return true
        val stream = try { audioAttrs.volumeControlStream } catch (_: Throwable) { AudioManager.USE_DEFAULT_STREAM_TYPE }
        if (stream == AudioManager.STREAM_VOICE_CALL || stream == AudioManager.STREAM_RING) return true
      }
    } catch (_: Throwable) {}

    val meta = try { controller.metadata } catch (_: Throwable) { null }
    val desc = try { meta?.description } catch (_: Throwable) { null }
    val title = try {
      meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        ?: desc?.title?.toString()
        ?: ""
    } catch (_: Throwable) { "" }
    val artist = try {
      meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
        ?: desc?.subtitle?.toString()
        ?: ""
    } catch (_: Throwable) { "" }
    val album = try {
      meta?.getString(MediaMetadata.METADATA_KEY_ALBUM)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
        ?: desc?.description?.toString()
        ?: ""
    } catch (_: Throwable) { "" }
    val appLabel = if (context != null && !pkg.isNullOrBlank()) resolveAppLabel(context, pkg) else ""
    return isIgnoredPhoneOrCallMediaMetadata(
      title = title,
      artist = artist,
      album = album,
      sourcePackage = pkg,
      sourceAppName = appLabel,
      context = context,
    )
  }

  /**
   * Returns `true` if [notification] from [packageName] is a phone call, ringtone, or non-media
   * call notification that must be ignored by the Music Player.
   */
  internal fun isIgnoredPhoneOrCallNotification(
    context: Context? = monitoredAppContext,
    packageName: String?,
    notification: Notification,
  ): Boolean {
    if (isIgnoredPhoneOrCallPackage(context, packageName)) return true
    val category = notification.category
    if (
      category == Notification.CATEGORY_CALL ||
      category == Notification.CATEGORY_ALARM ||
      category == Notification.CATEGORY_MESSAGE ||
      category == Notification.CATEGORY_SYSTEM ||
      category == Notification.CATEGORY_STATUS
    ) {
      return true
    }
    try {
      @Suppress("DEPRECATION")
      val audioAttrs = notification.audioAttributes
      if (audioAttrs != null && isNonMediaCallOrRingtoneUsage(audioAttrs.usage)) {
        return true
      }
    } catch (_: Throwable) {}
    val extras = notification.extras
    if (extras != null) {
      val template = try { extras.getString(Notification.EXTRA_TEMPLATE).orEmpty() } catch (_: Throwable) { "" }
      if (template.contains("CallStyle", ignoreCase = true)) return true
      val hasCallExtras = try {
        extras.containsKey("android.callType") ||
          extras.containsKey("android.callPerson") ||
          extras.containsKey("android.callIsVideo")
      } catch (_: Throwable) { false }
      if (hasCallExtras) return true

      val title = try {
        (extras.getCharSequence(Notification.EXTRA_TITLE)
          ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG))?.toString().orEmpty()
      } catch (_: Throwable) { "" }
      val artist = try {
        extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
      } catch (_: Throwable) { "" }
      val album = try {
        (extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
          ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))?.toString().orEmpty()
      } catch (_: Throwable) { "" }
      val appLabel = if (context != null && !packageName.isNullOrBlank()) resolveAppLabel(context, packageName) else ""
      if (
        isIgnoredPhoneOrCallMediaMetadata(
          title = title,
          artist = artist,
          album = album,
          sourcePackage = packageName,
          sourceAppName = appLabel,
          context = context,
        )
      ) {
        return true
      }
    }
    return false
  }

  /**
   * Updates the observable external system media player state (from another app's [MediaSession]
   * or media notification), ignoring phone call / dialer sessions.
   */
  fun updateExternalSystemMediaSession(info: SystemMediaPlayingInfo?) {
    externalMediaSetFromSystemSync = false
    if (info != null && isIgnoredPhoneOrCallSystemMediaInfo(info)) {
      if (isIgnoredPhoneOrCallSystemMediaInfo(externalSystemMediaInfo)) {
        externalSystemMediaInfo = null
        refreshAllWidgetsForMediaStateChange(monitoredAppContext)
      }
      return
    }
    externalSystemMediaInfo = info
    refreshAllWidgetsForMediaStateChange(monitoredAppContext)
  }

  /**
   * Updates the external system media player state from raw metadata/artwork fields.
   */
  fun updateExternalSystemMediaFromMetadata(
    title: String,
    artist: String = "",
    album: String = "",
    genre: String = "",
    sourcePackage: String = "",
    sourceAppName: String = "",
    isPlaying: Boolean = true,
    artworkBitmap: Bitmap? = null,
    notificationColorRgb: Int? = null,
    durationMs: Long = 0L,
    positionMs: Long = 0L,
    positionUpdateElapsedRealtimeMs: Long = safeElapsedRealtimeMs(),
    playbackSpeed: Float = 1.0f,
    canSeek: Boolean = durationMs > 0L,
    canSkipNext: Boolean = true,
    canSkipPrevious: Boolean = true,
    customActions: List<SystemMediaCustomAction> = emptyList(),
  ): SystemMediaPlayingInfo {
    return applyExternalSystemMediaMetadata(
      title = title,
      artist = artist,
      album = album,
      genre = genre,
      sourcePackage = sourcePackage,
      sourceAppName = sourceAppName,
      isPlaying = isPlaying,
      artworkBitmap = artworkBitmap,
      notificationColorRgb = notificationColorRgb,
      durationMs = durationMs,
      positionMs = positionMs,
      positionUpdateElapsedRealtimeMs = positionUpdateElapsedRealtimeMs,
      playbackSpeed = playbackSpeed,
      canSeek = canSeek,
      canSkipNext = canSkipNext,
      canSkipPrevious = canSkipPrevious,
      customActions = customActions,
      fromSystemSync = false,
    )
  }

  internal fun applyExternalSystemMediaMetadata(
    title: String,
    artist: String = "",
    album: String = "",
    genre: String = "",
    sourcePackage: String = "",
    sourceAppName: String = "",
    isPlaying: Boolean = true,
    artworkBitmap: Bitmap? = null,
    notificationColorRgb: Int? = null,
    durationMs: Long = 0L,
    positionMs: Long = 0L,
    positionUpdateElapsedRealtimeMs: Long = safeElapsedRealtimeMs(),
    playbackSpeed: Float = 1.0f,
    canSeek: Boolean = durationMs > 0L,
    canSkipNext: Boolean = true,
    canSkipPrevious: Boolean = true,
    customActions: List<SystemMediaCustomAction> = emptyList(),
    fromSystemSync: Boolean = false,
  ): SystemMediaPlayingInfo {
    val cleanTitle = title.ifBlank { "External Media" }
    val safeDuration = durationMs.coerceAtLeast(0L)
    val safePosition = if (safeDuration > 0L) {
      positionMs.coerceIn(0L, safeDuration)
    } else {
      positionMs.coerceAtLeast(0L)
    }
    val safeSpeed = playbackSpeed.takeIf { it > 0f } ?: 1.0f
    val safeCanSeek = canSeek && safeDuration > 0L

    val existing = externalSystemMediaInfo
    val sameTrackIdentity = existing != null &&
      existing.isExternalApp &&
      existing.title == cleanTitle &&
      existing.artist == artist &&
      existing.album == album &&
      existing.genre == genre &&
      existing.sourcePackage == sourcePackage &&
      existing.sourceAppName == sourceAppName

    val normalizedInputArtwork = when {
      artworkBitmap == null -> null
      sameTrackIdentity &&
        existing.artworkBitmap != null &&
        (artworkBitmap === existing.artworkBitmap ||
          artworkBitmap.generationId == existing.artworkBitmap.generationId) -> existing.artworkBitmap
      else -> normalizeExternalArtworkBitmap(artworkBitmap)
    }

    val canReuseExistingArtworkAndPalette = sameTrackIdentity &&
      areBitmapsVisuallyEquivalent(normalizedInputArtwork, existing.artworkBitmap)

    val safeArtwork = if (canReuseExistingArtworkAndPalette) {
      existing.artworkBitmap
    } else {
      normalizedInputArtwork
    }
    val palette = if (canReuseExistingArtworkAndPalette && notificationColorRgb == null) {
      existing.palette
    } else {
      resolveSystemMediaPalette(
        artworkBitmap = safeArtwork,
        notificationColorRgb = notificationColorRgb,
        title = title,
        artist = artist,
        album = album,
        genre = genre,
      )
    }
    val stabilizedCustomActions = if (sameTrackIdentity && customActions.size == existing.customActions.size) {
      customActions.mapIndexed { idx, act ->
        val prevAct = existing.customActions[idx]
        if (
          act.action == prevAct.action &&
          act.label == prevAct.label &&
          act.iconResId == prevAct.iconResId &&
          areBitmapsVisuallyEquivalent(act.iconBitmap, prevAct.iconBitmap)
        ) {
          act.copy(iconBitmap = prevAct.iconBitmap)
        } else {
          act
        }
      }
    } else {
      customActions
    }
    val effectivePosUpdateElapsedMs = if (
      fromSystemSync &&
      sameTrackIdentity &&
      existing.isPlaying == isPlaying &&
      safePosition == 0L &&
      existing.positionMs == 0L &&
      existing.positionUpdateElapsedRealtimeMs > 0L
    ) {
      existing.positionUpdateElapsedRealtimeMs
    } else {
      positionUpdateElapsedRealtimeMs
    }
    val info = SystemMediaPlayingInfo(
      title = cleanTitle,
      artist = artist,
      album = album,
      genre = genre,
      sourcePackage = sourcePackage,
      sourceAppName = sourceAppName,
      isExternalApp = true,
      isPlaying = isPlaying,
      palette = palette,
      artworkBitmap = safeArtwork,
      durationMs = safeDuration,
      positionMs = safePosition,
      positionUpdateElapsedRealtimeMs = effectivePosUpdateElapsedMs,
      playbackSpeed = safeSpeed,
      canSeek = safeCanSeek,
      canSkipNext = canSkipNext,
      canSkipPrevious = canSkipPrevious,
      customActions = stabilizedCustomActions,
    )
    if (isIgnoredPhoneOrCallSystemMediaInfo(info)) {
      if (isIgnoredPhoneOrCallSystemMediaInfo(externalSystemMediaInfo)) {
        externalSystemMediaInfo = null
        refreshAllWidgetsForMediaStateChange(monitoredAppContext)
      }
      return info
    }
    externalMediaSetFromSystemSync = fromSystemSync
    val isWidgetRelevantStateSame = existing != null &&
      existing.title == info.title &&
      existing.artist == info.artist &&
      existing.album == info.album &&
      existing.genre == info.genre &&
      existing.sourcePackage == info.sourcePackage &&
      existing.sourceAppName == info.sourceAppName &&
      existing.isExternalApp == info.isExternalApp &&
      existing.isPlaying == info.isPlaying &&
      existing.palette == info.palette &&
      existing.artworkBitmap === info.artworkBitmap &&
      existing.canSkipNext == info.canSkipNext &&
      existing.canSkipPrevious == info.canSkipPrevious &&
      existing.customActions.size == info.customActions.size &&
      existing.customActions.indices.all { idx ->
        val a = existing.customActions[idx]
        val b = info.customActions[idx]
        a.action == b.action && a.label == b.label && a.iconResId == b.iconResId
      }

    val isPositionUnchangedFromPoll = fromSystemSync &&
      existing != null &&
      existing.durationMs == info.durationMs &&
      existing.playbackSpeed == info.playbackSpeed &&
      existing.canSeek == info.canSeek &&
      (
        (safePosition == 0L && existing.positionMs == 0L) ||
          kotlin.math.abs(currentExternalSystemMediaPositionMs(effectivePosUpdateElapsedMs) - safePosition) <= 1_500L
        )

    val isSemanticallyUnchangedFromPoll = isWidgetRelevantStateSame && isPositionUnchangedFromPoll

    if (!isSemanticallyUnchangedFromPoll && externalSystemMediaInfo != info) {
      externalSystemMediaInfo = info
      if (!isWidgetRelevantStateSame) {
        refreshAllWidgetsForMediaStateChange(monitoredAppContext)
      }
    }
    return externalSystemMediaInfo ?: info
  }

  internal data class CachedExternalMediaNotification(
    val packageName: String,
    val title: String,
    val artist: String,
    val album: String,
    val isOngoing: Boolean,
    val colorRgb: Int?,
    val artworkBitmap: Bitmap?,
    val sessionToken: MediaSession.Token?,
    val contentIntent: PendingIntent? = null,
    val customActions: List<SystemMediaCustomAction> = emptyList(),
    val postedAtMs: Long = System.currentTimeMillis(),
  )

  internal val activeMediaNotifications = ConcurrentHashMap<String, CachedExternalMediaNotification>()

  @Volatile
  private var lastBroadcastMediaInfo: SystemMediaPlayingInfo? = null

  private val systemMediaMonitorLock = Any()

  @Volatile
  private var systemMediaMonitoringRegistered = false

  @Volatile
  private var systemMediaSessionListenerRegistered = false

  private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

  private val engineTrackedControllers = mutableMapOf<MediaController, MediaController.Callback>()

  @Volatile
  private var activeExternalMediaController: MediaController? = null

  /**
   * Last transport command dispatched to an external Android system media session
   * (`"PLAY"`, `"PAUSE"`, `"NEXT"`, `"PREVIOUS"`, `"SEEK:<ms>"`), useful for verification and diagnostics.
   */
  var externalSystemMediaLastDispatchedAction by mutableStateOf<String?>(null)
    internal set

  private val engineActiveSessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
    val ctx = monitoredAppContext ?: return@OnActiveSessionsChangedListener
    bindEngineExternalControllers(ctx, controllers)
    syncFromExternalMediaControllers(ctx, controllers)
  }

  @Volatile
  private var monitoredAppContext: Context? = null

  /**
   * Returns `true` if [state] represents an actively playing or transitioning [PlaybackState].
   */
  fun isPlaybackStateActive(state: Int?): Boolean {
    return state == PlaybackState.STATE_PLAYING ||
      state == PlaybackState.STATE_BUFFERING ||
      state == PlaybackState.STATE_FAST_FORWARDING ||
      state == PlaybackState.STATE_REWINDING ||
      state == PlaybackState.STATE_SKIPPING_TO_NEXT ||
      state == PlaybackState.STATE_SKIPPING_TO_PREVIOUS ||
      state == PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM ||
      state == PlaybackState.STATE_CONNECTING
  }

  /**
   * Returns `true` if Notification Listener access is enabled for Toolbox so external apps'
   * active [MediaSession]s can be queried from [MediaSessionManager].
   */
  fun isSystemMediaListenerEnabled(context: Context): Boolean {
    if (ToolboxMediaListenerService.activeInstance != null) return true
    return try {
      val component = ComponentName(context, ToolboxMediaListenerService::class.java)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (nm?.isNotificationListenerAccessGranted(component) == true) {
          return true
        }
      }
      val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
      ).orEmpty()
      val pkg = context.packageName
      enabledListeners.contains(pkg) || enabledListeners.contains(component.flattenToString())
    } catch (_: Throwable) {
      false
    }
  }

  /**
   * Opens the Android system Notification Listener settings page so the user can grant Toolbox
   * access to read external media sessions and artwork from the Android system media player.
   */
  fun openSystemMediaListenerSettings(context: Context) {
    val component = ComponentName(context, ToolboxMediaListenerService::class.java)
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val detailIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
          putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, component.flattenToString())
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(detailIntent)
        return
      }
    } catch (_: Throwable) {}
    try {
      val listIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(listIntent)
    } catch (_: Throwable) {
      Toast.makeText(
        context,
        "Unable to open Notification Access settings",
        Toast.LENGTH_SHORT,
      ).show()
    }
  }

  /**
   * Converts a [Drawable] (such as a notification large icon or app launcher icon) into a
   * software [Bitmap] suitable for preview rendering and palette extraction.
   */
  internal fun drawableToBitmap(drawable: Drawable?, targetSizePx: Int = 360): Bitmap? {
    if (drawable == null) return null
    return try {
      if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return ensureSoftwareBitmap(drawable.bitmap)
      }
      val w = drawable.intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(targetSizePx) ?: targetSizePx
      val h = drawable.intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(targetSizePx) ?: targetSizePx
      val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bmp)
      drawable.setBounds(0, 0, canvas.width, canvas.height)
      drawable.draw(canvas)
      bmp
    } catch (_: Throwable) {
      null
    }
  }

  @Suppress("DEPRECATION")
  internal fun extractNotificationArtworkBitmap(
    context: Context,
    notification: Notification,
  ): Bitmap? {
    val extras = notification.extras
    try {
      notification.getLargeIcon()?.loadDrawable(context)?.let { drawable ->
        drawableToBitmap(drawable)?.let { return it }
      }
    } catch (_: Throwable) {}

    if (extras != null) {
      val keys = listOf(
        Notification.EXTRA_LARGE_ICON_BIG,
        Notification.EXTRA_LARGE_ICON,
        Notification.EXTRA_PICTURE,
      )
      for (key in keys) {
        try {
          val raw = extras.get(key)
          when (raw) {
            is Bitmap -> return ensureSoftwareBitmap(raw)
            is Icon -> {
              raw.loadDrawable(context)?.let { drawable ->
                drawableToBitmap(drawable)?.let { return it }
              }
            }
          }
        } catch (_: Throwable) {}
      }
    }
    return null
  }

  @Suppress("DEPRECATION")
  internal fun extractNotificationMediaSessionToken(notification: Notification): MediaSession.Token? {
    val extras = notification.extras ?: return null
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
      } else {
        extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token
      }
    } catch (_: Throwable) {
      null
    }
  }

  private val STANDARD_TRANSPORT_NOTIFICATION_ACTION_TITLES = setOf(
    "play",
    "pause",
    "play/pause",
    "play / pause",
    "next",
    "skip next",
    "next track",
    "skip to next",
    "previous",
    "prev",
    "skip previous",
    "previous track",
    "skip to previous",
    "stop",
    "fast forward",
    "rewind",
  )

  internal fun extractNotificationCustomActions(
    context: Context,
    notification: Notification,
  ): List<SystemMediaCustomAction> {
    val rawActions = notification.actions ?: return emptyList()
    if (rawActions.isEmpty()) return emptyList()
    val result = mutableListOf<SystemMediaCustomAction>()
    for ((index, notifAction) in rawActions.withIndex()) {
      if (notifAction == null) continue
      val title = try { notifAction.title?.toString()?.trim().orEmpty() } catch (_: Throwable) { "" }
      val normalized = title.lowercase(Locale.ROOT)
      if (normalized in STANDARD_TRANSPORT_NOTIFICATION_ACTION_TITLES) continue
      // In a standard 5-button MediaStyle notification where compact slots are [1, 2, 3],
      // skip indices 1..3 even if localized titles differ.
      val compactIndices = try {
        notification.extras?.getIntArray(Notification.EXTRA_COMPACT_ACTIONS)
      } catch (_: Throwable) {
        null
      }
      if (rawActions.size >= 5 && compactIndices != null && compactIndices.size == 3 && index in compactIndices) {
        continue
      }
      if (rawActions.size == 5 && title.isBlank() && index in 1..3) {
        continue
      }
      val iconBmp = try {
        notifAction.getIcon()?.loadDrawable(context)?.let { drawableToBitmap(it, targetSizePx = 64) }
      } catch (_: Throwable) {
        null
      }
      val slug = normalized.replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "$index" }
      result.add(
        SystemMediaCustomAction(
          action = "notif_action_${index}_$slug",
          label = title.ifBlank { "Action ${index + 1}" },
          iconBitmap = iconBmp,
          pendingIntent = notifAction.actionIntent,
        )
      )
    }
    return result
  }

  internal fun loadExternalCustomActionIconBitmap(
    context: Context,
    packageName: String,
    iconResId: Int,
  ): Bitmap? {
    if (packageName.isBlank() || iconResId == 0) return null
    val cacheKey = "action_icon:$packageName|$iconResId"
    synchronized(externalMediaCacheLock) {
      externalFallbackArtworkCache[cacheKey]?.takeIf { !it.isRecycled }?.let { return it }
    }
    val loaded = try {
      val fromIcon = try {
        Icon.createWithResource(packageName, iconResId).loadDrawable(context)
      } catch (_: Throwable) {
        null
      }
      val drawable = fromIcon ?: try {
        context.packageManager.getResourcesForApplication(packageName).getDrawable(iconResId, null)
      } catch (_: Throwable) {
        null
      }
      drawable?.let { drawableToBitmap(it, targetSizePx = 64) }
    } catch (_: Throwable) {
      null
    }
    if (loaded != null) {
      synchronized(externalMediaCacheLock) {
        externalFallbackArtworkCache[cacheKey] = loaded
      }
    }
    return loaded
  }

  internal fun loadArtworkBitmapFromUri(
    context: Context,
    uriString: String?,
    reqSizePx: Int = 360,
  ): Bitmap? {
    if (uriString.isNullOrBlank()) return null
    val trimmed = uriString.trim()
    val safeTarget = reqSizePx.coerceIn(64, 1024)
    val cacheKey = "uri:$trimmed|$safeTarget"
    synchronized(externalMediaCacheLock) {
      externalFallbackArtworkCache[cacheKey]?.takeIf { !it.isRecycled }?.let { return it }
      if (externalMissingArtworkCache[cacheKey] == true) return null
    }
    val loaded = try {
      val uri = Uri.parse(trimmed)
      val scheme = uri.scheme?.lowercase(Locale.ROOT)
      when (scheme) {
        "content", "android.resource" -> {
          val rawBytes = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes()
          }
          if (rawBytes != null && rawBytes.isNotEmpty()) {
            decodeDownsampledBitmap(rawBytes, reqSizePx = safeTarget)
          } else {
            null
          }
        }
        "file" -> {
          val path = uri.path
          if (!path.isNullOrBlank()) {
            val file = File(path)
            if (file.exists() && file.isFile) {
              GalleryEngine.decodeThumbnailBitmap(file, reqSizePx = safeTarget)
                ?.let { scaleAndNormalizeSoftwareBitmap(it, maxDimPx = safeTarget) }
            } else {
              null
            }
          } else {
            null
          }
        }
        else -> null
      }
    } catch (_: Throwable) {
      null
    }
    synchronized(externalMediaCacheLock) {
      if (loaded != null) {
        externalFallbackArtworkCache[cacheKey] = loaded
      } else {
        externalMissingArtworkCache[cacheKey] = true
      }
    }
    return loaded
  }

  internal fun findLocalOrMediaStoreArtworkBitmap(
    context: Context,
    title: String,
    artist: String,
    album: String,
    albumId: Long? = null,
  ): Bitmap? {
    if (albumId != null && albumId > 0L) {
      try {
        val artUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        loadArtworkBitmapFromUri(context, artUri.toString(), reqSizePx = 360)?.let { return it }
      } catch (_: Throwable) {}
    }
    if (title.isBlank() && album.isBlank()) return null
    val lookupKey = "local_lookup:${title.trim().lowercase(Locale.ROOT)}|${artist.trim().lowercase(Locale.ROOT)}|${album.trim().lowercase(Locale.ROOT)}"
    synchronized(externalMediaCacheLock) {
      externalFallbackArtworkCache[lookupKey]?.takeIf { !it.isRecycled }?.let { return it }
      if (externalMissingArtworkCache[lookupKey] == true) return null
    }
    // Check in-memory indexed tracks first
    val matchingTrack = synchronized(metadataCacheLock) {
      trackIndexByPath.values.firstOrNull { track ->
        (!track.isSampleTrack) && (
          (title.isNotBlank() && track.title.equals(title, ignoreCase = true)) ||
            (album.isNotBlank() && track.album.equals(album, ignoreCase = true))
          )
      }
    }
    if (matchingTrack != null) {
      getCachedAlbumArtBitmap(matchingTrack, 360)?.let { cached ->
        if (!isCachedBitmapProcedural(matchingTrack, cached, 360)) {
          synchronized(externalMediaCacheLock) {
            externalFallbackArtworkCache[lookupKey] = cached
          }
          return cached
        }
      }
    }
    if (matchingTrack?.file != null && matchingTrack.file.exists()) {
      try {
        val retriever = MediaMetadataRetriever()
        try {
          retriever.setDataSource(matchingTrack.file.absolutePath)
          val bytes = retriever.embeddedPicture
          if (bytes != null && bytes.isNotEmpty()) {
            decodeDownsampledBitmap(bytes, reqSizePx = 360)?.let { decoded ->
              synchronized(externalMediaCacheLock) {
                externalFallbackArtworkCache[lookupKey] = decoded
              }
              return decoded
            }
          }
        } finally {
          try {
            retriever.release()
          } catch (_: Throwable) {}
        }
      } catch (_: Throwable) {}
    }
    // Try MediaStore albumId lookup if permission is granted
    try {
      val resolver = context.contentResolver
      val projection = arrayOf(MediaStore.Audio.Media.ALBUM_ID)
      val selection = if (title.isNotBlank()) {
        "${MediaStore.Audio.Media.TITLE} = ?"
      } else {
        "${MediaStore.Audio.Media.ALBUM} = ?"
      }
      val args = arrayOf(if (title.isNotBlank()) title else album)
      resolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        args,
        null,
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          val foundAlbumId = cursor.getLong(0)
          if (foundAlbumId > 0L) {
            val artUri = Uri.parse("content://media/external/audio/albumart/$foundAlbumId")
            loadArtworkBitmapFromUri(context, artUri.toString(), reqSizePx = 360)?.let { loaded ->
              synchronized(externalMediaCacheLock) {
                externalFallbackArtworkCache[lookupKey] = loaded
              }
              return loaded
            }
          }
        }
      }
    } catch (_: Throwable) {}
    synchronized(externalMediaCacheLock) {
      externalMissingArtworkCache[lookupKey] = true
    }
    return null
  }

  internal fun loadAppIconBitmap(context: Context, packageName: String): Bitmap? {
    if (packageName.isBlank() || packageName == context.packageName) return null
    val cacheKey = "app_icon:${packageName.trim()}"
    synchronized(externalMediaCacheLock) {
      externalFallbackArtworkCache[cacheKey]?.takeIf { !it.isRecycled }?.let { return it }
    }
    val loaded = try {
      val drawable = context.packageManager.getApplicationIcon(packageName)
      drawableToBitmap(drawable)
    } catch (_: Throwable) {
      null
    }
    if (loaded != null) {
      synchronized(externalMediaCacheLock) {
        externalFallbackArtworkCache[cacheKey] = loaded
      }
    }
    return loaded
  }

  internal fun resolveAppLabel(context: Context, packageName: String): String {
    if (packageName.isBlank()) return ""
    return try {
      val pm = context.packageManager
      val appInfo = pm.getApplicationInfo(packageName, 0)
      pm.getApplicationLabel(appInfo).toString()
    } catch (_: Throwable) {
      packageName.substringAfterLast('.').replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
      }
    }
  }

  /**
   * Registers background listeners ([AudioManager.AudioPlaybackCallback], standard Android music
   * broadcast receivers, and [MediaSessionManager.OnActiveSessionsChangedListener]) so external
   * system media playback and artwork changes are detected in real time.
   */
  fun ensureSystemMediaMonitoring(context: Context) {
    val appCtx = context.applicationContext ?: context
    monitoredAppContext = appCtx
    synchronized(systemMediaMonitorLock) {
      if (!systemMediaMonitoringRegistered) {
        systemMediaMonitoringRegistered = true
        // 1. Register AudioPlaybackCallback (requires no special permissions)
        try {
          val audioManager = appCtx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
          audioManager?.registerAudioPlaybackCallback(
            object : AudioManager.AudioPlaybackCallback() {
              override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
                super.onPlaybackConfigChanged(configs)
                syncExternalSystemMediaSessions(appCtx)
              }
            },
            mainHandler,
          )
        } catch (_: Throwable) {}

        // 2. Register standard Android music BroadcastReceiver for apps that broadcast track metadata
        try {
          val filter = IntentFilter().apply {
            addAction("com.android.music.metachanged")
            addAction("com.android.music.playstatechanged")
            addAction("com.android.music.playbackcomplete")
            addAction("com.android.music.queuechanged")
            addAction("com.spotify.music.metadatachanged")
            addAction("com.spotify.music.playbackstatechanged")
            addAction("com.htc.music.metachanged")
            addAction("com.htc.music.playstatechanged")
            addAction("com.sec.android.app.music.metachanged")
            addAction("com.sec.android.app.music.playstatechanged")
            addAction("com.miui.player.metachanged")
            addAction("com.miui.player.playstatechanged")
          }
          val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
              if (intent == null) return
              handleExternalMusicBroadcast(appCtx, intent)
            }
          }
          ContextCompat.registerReceiver(
            appCtx,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED,
          )
        } catch (_: Throwable) {}

        // 2b. Register system configuration / theme / wallpaper change receiver so dark/light mode
        // toggles and Android 12+ Material You dynamic color changes immediately refresh widgets
        try {
          val configFilter = IntentFilter().apply {
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
            @Suppress("DEPRECATION")
            addAction(Intent.ACTION_WALLPAPER_CHANGED)
          }
          val configReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
              refreshAllWidgetsForMediaStateChange(
                context = appCtx,
                forceThemeWidgetsRefresh = true,
              )
            }
          }
          ContextCompat.registerReceiver(
            appCtx,
            configReceiver,
            configFilter,
            ContextCompat.RECEIVER_EXPORTED,
          )
        } catch (_: Throwable) {}
      }

      // 3. If Notification Listener is enabled, request rebind and register session listener
      if (isSystemMediaListenerEnabled(appCtx)) {
        try {
          if (ToolboxMediaListenerService.activeInstance == null) {
            NotificationListenerService.requestRebind(
              ComponentName(appCtx, ToolboxMediaListenerService::class.java),
            )
          }
        } catch (_: Throwable) {}

        if (!systemMediaSessionListenerRegistered) {
          try {
            val msm = appCtx.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            if (msm != null) {
              val component = ComponentName(appCtx, ToolboxMediaListenerService::class.java)
              msm.addOnActiveSessionsChangedListener(
                engineActiveSessionsListener,
                component,
                mainHandler,
              )
              systemMediaSessionListenerRegistered = true
            }
          } catch (_: Throwable) {}
        }
      }
    }
  }

  private fun handleExternalMusicBroadcast(context: Context, intent: Intent) {
    try {
      val extras = intent.extras ?: return
      val action = intent.action.orEmpty()
      val senderPkg = (
        intent.`package`
          ?: extras.getString("package")
          ?: extras.getString("scrobbling_source")
          ?: extras.getString("app_package")
          ?: extras.getString("player")
          ?: when {
            action.startsWith("com.spotify.music") -> "com.spotify.music"
            action.startsWith("com.sec.android") -> "com.sec.android.app.music"
            action.startsWith("com.htc.music") -> "com.htc.music"
            action.startsWith("com.miui.player") -> "com.miui.player"
            else -> findActiveExternalPlaybackPackage(context).orEmpty()
          }
        ).trim()
      if (senderPkg == context.packageName || isIgnoredPhoneOrCallPackage(context, senderPkg)) return

      val previous = (lastBroadcastMediaInfo ?: externalSystemMediaInfo)
        ?.takeUnless { isIgnoredPhoneOrCallSystemMediaInfo(it, context) }
      val title = extras.getString("track")
        ?: extras.getString("title")
        ?: extras.getString("trackName")
        ?: previous?.title.orEmpty()
      val artist = extras.getString("artist")
        ?: extras.getString("artistName")
        ?: previous?.artist.orEmpty()
      val album = extras.getString("album")
        ?: extras.getString("albumName")
        ?: previous?.album.orEmpty()
      val appLabel = resolveAppLabel(context, senderPkg).ifBlank { "External Player" }
      if (
        isIgnoredPhoneOrCallMediaMetadata(
          title = title.ifBlank { appLabel },
          artist = artist,
          album = album,
          sourcePackage = senderPkg,
          sourceAppName = appLabel,
          context = context,
        )
      ) {
        return
      }
      val playing = when {
        action.endsWith("playbackcomplete") -> false
        extras.containsKey("playing") -> extras.getBoolean("playing", true)
        extras.containsKey("isPlaying") -> extras.getBoolean("isPlaying", true)
        extras.containsKey("playstate") -> extras.getBoolean("playstate", true)
        else -> true
      }
      val albumId = when (val rawAlbumId = extras.get("albumId") ?: extras.get("id")) {
        is Long -> rawAlbumId
        is Int -> rawAlbumId.toLong()
        else -> null
      }
      val artBitmap = findLocalOrMediaStoreArtworkBitmap(context, title, artist, album, albumId)
        ?: loadAppIconBitmap(context, senderPkg)
      val nowElapsed = safeElapsedRealtimeMs()
      val preservedCustomActions = activeMediaNotifications[senderPkg]?.customActions?.takeIf { it.isNotEmpty() }
        ?: previous?.customActions.orEmpty()
      val info = applyExternalSystemMediaMetadata(
        title = title.ifBlank { appLabel },
        artist = artist,
        album = album,
        sourcePackage = senderPkg,
        sourceAppName = appLabel,
        isPlaying = playing,
        artworkBitmap = artBitmap,
        positionUpdateElapsedRealtimeMs = nowElapsed,
        customActions = preservedCustomActions,
        fromSystemSync = true,
      )
      lastBroadcastReceivedElapsedMs = nowElapsed
      lastBroadcastMediaInfo = info
    } catch (_: Throwable) {}
  }

  private fun bindEngineExternalControllers(
    context: Context,
    controllers: List<MediaController>?,
  ) {
    val ownPkg = context.packageName
    synchronized(systemMediaMonitorLock) {
      val validControllers = controllers.orEmpty().filter {
        try {
          it.packageName != ownPkg && !isIgnoredPhoneOrCallMediaController(context, it)
        } catch (_: Throwable) {
          false
        }
      }
      val currentTokens = engineTrackedControllers.keys.mapNotNull {
        try { it.sessionToken } catch (_: Throwable) { null }
      }.toSet()
      val desiredTokens = validControllers.mapNotNull {
        try { it.sessionToken } catch (_: Throwable) { null }
      }.toSet()
      if (currentTokens == desiredTokens && engineTrackedControllers.size == validControllers.size) {
        return
      }
      for ((ctrl, cb) in engineTrackedControllers) {
        try {
          ctrl.unregisterCallback(cb)
        } catch (_: Throwable) {}
      }
      engineTrackedControllers.clear()
      for (controller in validControllers) {
        val callback = object : MediaController.Callback() {
          override fun onPlaybackStateChanged(state: PlaybackState?) {
            syncExternalSystemMediaSessions(context)
          }

          override fun onMetadataChanged(metadata: MediaMetadata?) {
            syncExternalSystemMediaSessions(context)
          }

          override fun onSessionDestroyed() {
            syncExternalSystemMediaSessions(context)
          }
        }
        try {
          controller.registerCallback(callback, mainHandler)
          engineTrackedControllers[controller] = callback
        } catch (_: Throwable) {}
      }
    }
  }

  /**
   * Tracks whether an in-app video player (such as the Image Viewer or Video Editor) is currently
   * playing so [isExternalAudioPlaybackActive] does not mistake Toolbox's own `STREAM_MUSIC`
   * video playback for an external music player.
   */
  @Volatile
  internal var isInAppVideoPlaybackActive: Boolean = false

  /**
   * Returns `true` if another application is currently outputting media audio on the device
   * (either via [AudioManager.isMusicActive] or an active media [AudioPlaybackConfiguration]),
   * excluding Toolbox's own built-in player and phone call / ringtone audio streams.
   */
  internal fun isExternalAudioPlaybackActive(context: Context): Boolean {
    val internalPlaying = activeSessionState?.let { !it.isReleased && it.isPlaying } == true
    if (internalPlaying) return false
    if (isPhoneCallActiveOrRinging(context)) return false
    return try {
      val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
      if (isPhoneCallAudioMode(audioManager.mode)) return false
      val configs = audioManager.activePlaybackConfigurations.orEmpty()
      val myUid = Process.myUid()
      val uidRegex = Regex("""u/pid:(\d+)/""")
      var sawOwnActiveConfig = false
      for (cfg in configs) {
        val attrs = cfg.audioAttributes
        val usage = attrs?.usage ?: AudioAttributes.USAGE_UNKNOWN
        if (isNonMediaCallOrRingtoneUsage(usage)) continue
        if (attrs?.contentType == AudioAttributes.CONTENT_TYPE_SONIFICATION) continue
        val isMediaUsage = usage == AudioAttributes.USAGE_MEDIA ||
          usage == AudioAttributes.USAGE_GAME ||
          usage == AudioAttributes.USAGE_UNKNOWN
        if (!isMediaUsage) continue
        val dump = cfg.toString()
        if (dump.contains("state:idle") || dump.contains("state:stopped") || dump.contains("state:released") || dump.contains("state:paused")) {
          continue
        }
        val uidMatch = uidRegex.find(dump)?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (uidMatch != null && uidMatch == myUid) {
          sawOwnActiveConfig = true
          continue
        }
        if (uidMatch != null && uidMatch > 1000) {
          val pkgs = try { context.packageManager.getPackagesForUid(uidMatch) } catch (_: Throwable) { null }
          if (pkgs != null && pkgs.any { isIgnoredPhoneOrCallPackage(context, it) }) {
            continue
          }
        }
        if (dump.contains("state:started") || (uidMatch != null && uidMatch != myUid && uidMatch > 1000)) {
          return true
        }
      }
      if (isInAppVideoPlaybackActive || sawOwnActiveConfig) {
        false
      } else {
        audioManager.isMusicActive
      }
    } catch (_: Throwable) {
      false
    }
  }

  private fun findActiveExternalPlaybackPackage(context: Context): String? {
    val internalPlaying = activeSessionState?.let { !it.isReleased && it.isPlaying } == true
    if (internalPlaying) return null
    if (isPhoneCallActiveOrRinging(context)) return null
    return try {
      val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return null
      if (isPhoneCallAudioMode(audioManager.mode)) return null
      val configs = audioManager.activePlaybackConfigurations.orEmpty()
      val myUid = Process.myUid()
      val uidRegex = Regex("""u/pid:(\d+)/""")
      for (cfg in configs) {
        val attrs = cfg.audioAttributes
        val usage = attrs?.usage ?: AudioAttributes.USAGE_UNKNOWN
        if (isNonMediaCallOrRingtoneUsage(usage)) continue
        if (attrs?.contentType == AudioAttributes.CONTENT_TYPE_SONIFICATION) continue
        val isMediaUsage = usage == AudioAttributes.USAGE_MEDIA ||
          usage == AudioAttributes.USAGE_GAME ||
          usage == AudioAttributes.USAGE_UNKNOWN
        if (!isMediaUsage) continue
        val dump = cfg.toString()
        if (dump.contains("state:idle") || dump.contains("state:stopped") || dump.contains("state:released") || dump.contains("state:paused")) {
          continue
        }
        val uidMatch = uidRegex.find(dump)?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (uidMatch != null && uidMatch != myUid && uidMatch > 1000) {
          val pkgs = context.packageManager.getPackagesForUid(uidMatch)
          if (pkgs != null && pkgs.any { isIgnoredPhoneOrCallPackage(context, it) }) {
            continue
          }
          val pkg = pkgs?.firstOrNull { it != context.packageName && !isIgnoredPhoneOrCallPackage(context, it) }
          if (!pkg.isNullOrBlank()) {
            val label = resolveAppLabel(context, pkg)
            if (!isIgnoredPhoneOrCallAppName(label)) {
              return pkg
            }
          }
        }
      }
      null
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Queries [MediaSessionManager], active media notifications in [ToolboxMediaListenerService],
   * and [AudioManager] playback state, updating [externalSystemMediaInfo] with the currently
   * playing (or most recent) external media session's artwork palette and metadata.
   */
  fun syncExternalSystemMediaSessions(context: Context) {
    try {
      val appCtx = context.applicationContext ?: context
      ensureSystemMediaMonitoring(appCtx)

      // Refresh active media notifications from ToolboxMediaListenerService if connected
      ToolboxMediaListenerService.activeInstance?.refreshCachedMediaNotifications()

      val collectedControllers = mutableListOf<MediaController>()
      try {
        val msm = appCtx.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        if (msm != null) {
          val component = ComponentName(appCtx, ToolboxMediaListenerService::class.java)
          val active = try {
            msm.getActiveSessions(component)
          } catch (_: SecurityException) {
            try {
              msm.getActiveSessions(null)
            } catch (_: Throwable) {
              emptyList()
            }
          }
          collectedControllers.addAll(active.filterNot { isIgnoredPhoneOrCallMediaController(appCtx, it) })
        }
      } catch (_: Throwable) {}

      // Also build MediaControllers from any active media notification tokens not already in list
      for (cachedNotif in activeMediaNotifications.values) {
        if (isIgnoredPhoneOrCallPackage(appCtx, cachedNotif.packageName)) continue
        val token = cachedNotif.sessionToken ?: continue
        try {
          val ctrl = MediaController(appCtx, token)
          if (
            !isIgnoredPhoneOrCallMediaController(appCtx, ctrl) &&
            collectedControllers.none { it.packageName == ctrl.packageName }
          ) {
            collectedControllers.add(ctrl)
          }
        } catch (_: Throwable) {}
      }

      bindEngineExternalControllers(appCtx, collectedControllers)
      syncFromExternalMediaControllers(appCtx, collectedControllers)
    } catch (_: Throwable) {}
  }

  internal fun syncFromExternalMediaControllers(
    context: Context,
    controllers: List<MediaController>?,
  ) {
    try {
      val ownPkg = context.packageName
      // Purge any phone/call session that may have been cached previously
      if (isIgnoredPhoneOrCallSystemMediaInfo(externalSystemMediaInfo, context)) {
        externalSystemMediaInfo = null
        refreshAllWidgetsForMediaStateChange(context)
      }
      if (isIgnoredPhoneOrCallSystemMediaInfo(lastBroadcastMediaInfo, context)) {
        lastBroadcastMediaInfo = null
      }
      val ignoredNotifPkgs = activeMediaNotifications.values
        .filter { notif ->
          isIgnoredPhoneOrCallPackage(context, notif.packageName) ||
            isIgnoredPhoneOrCallMediaMetadata(
              title = notif.title,
              artist = notif.artist,
              album = notif.album,
              sourcePackage = notif.packageName,
              sourceAppName = resolveAppLabel(context, notif.packageName),
              context = context,
            )
        }
        .map { it.packageName }
      for (pkg in ignoredNotifPkgs) {
        activeMediaNotifications.remove(pkg)
      }

      val externalControllers = controllers.orEmpty().filter {
        try {
          it.packageName != ownPkg && !isIgnoredPhoneOrCallMediaController(context, it)
        } catch (_: Throwable) {
          false
        }
      }
      val systemMusicStreamActive = isExternalAudioPlaybackActive(context)

      if (externalControllers.isEmpty()) {
        // Check cached media notifications from ToolboxMediaListenerService
        val latestNotif = activeMediaNotifications.values
          .filter { notif ->
            notif.packageName != ownPkg &&
              !isIgnoredPhoneOrCallPackage(context, notif.packageName) &&
              !isIgnoredPhoneOrCallMediaMetadata(
                title = notif.title,
                artist = notif.artist,
                album = notif.album,
                sourcePackage = notif.packageName,
                sourceAppName = resolveAppLabel(context, notif.packageName),
                context = context,
              )
          }
          .maxByOrNull { (if (it.isOngoing) 1_000_000_000_000L else 0L) + it.postedAtMs }
        if (latestNotif != null) {
          val appLabel = resolveAppLabel(context, latestNotif.packageName)
          val artBmp = latestNotif.artworkBitmap
            ?: findLocalOrMediaStoreArtworkBitmap(context, latestNotif.title, latestNotif.artist, latestNotif.album)
            ?: loadAppIconBitmap(context, latestNotif.packageName)
          applyExternalSystemMediaMetadata(
            title = latestNotif.title.ifBlank { appLabel },
            artist = latestNotif.artist,
            album = latestNotif.album,
            sourcePackage = latestNotif.packageName,
            sourceAppName = appLabel,
            isPlaying = latestNotif.isOngoing || systemMusicStreamActive,
            artworkBitmap = artBmp,
            notificationColorRgb = latestNotif.colorRgb,
            customActions = latestNotif.customActions,
            fromSystemSync = true,
          )
          return
        }

        // Check standard Android music broadcast info
        val broadcastInfo = lastBroadcastMediaInfo?.takeUnless { isIgnoredPhoneOrCallSystemMediaInfo(it, context) }
        if (broadcastInfo != null) {
          val nowElapsed = safeElapsedRealtimeMs()
          val recentlyBroadcast = lastBroadcastReceivedElapsedMs > 0L &&
            nowElapsed >= lastBroadcastReceivedElapsedMs &&
            (nowElapsed - lastBroadcastReceivedElapsedMs) <= 3_500L
          val effectivePlaying = broadcastInfo.isPlaying && (systemMusicStreamActive || recentlyBroadcast)
          val updatedBroadcast = if (broadcastInfo.isPlaying != effectivePlaying) {
            broadcastInfo.copy(isPlaying = effectivePlaying)
          } else {
            broadcastInfo
          }
          externalMediaSetFromSystemSync = true
          if (externalSystemMediaInfo != updatedBroadcast) {
            externalSystemMediaInfo = updatedBroadcast
            refreshAllWidgetsForMediaStateChange(context)
          }
          return
        }

        // Check active external AudioPlaybackConfiguration package
        val activeExternalPkg = findActiveExternalPlaybackPackage(context)
        if (activeExternalPkg != null && !isIgnoredPhoneOrCallPackage(context, activeExternalPkg)) {
          val appLabel = resolveAppLabel(context, activeExternalPkg)
          if (!isIgnoredPhoneOrCallAppName(appLabel)) {
            val existing = externalSystemMediaInfo
            if (existing != null && existing.sourcePackage == activeExternalPkg) {
              externalMediaSetFromSystemSync = true
              if (!existing.isPlaying) {
                externalSystemMediaInfo = existing.copy(isPlaying = true)
                refreshAllWidgetsForMediaStateChange(context)
              }
            } else {
              val iconBmp = loadAppIconBitmap(context, activeExternalPkg)
              applyExternalSystemMediaMetadata(
                title = appLabel.ifBlank { "System Media" },
                sourcePackage = activeExternalPkg,
                sourceAppName = appLabel,
                isPlaying = true,
                artworkBitmap = iconBmp,
                fromSystemSync = true,
              )
            }
            return
          }
        }

        // Fallback when the user explicitly launched a preferred external music app from Toolbox
        // and system audio playback is active (e.g., before Notification Access is granted)
        val launchedApp = lastLaunchedPreferredMusicApp
        if (
          systemMusicStreamActive &&
          launchedApp != null &&
          !isIgnoredPhoneOrCallPackage(context, launchedApp.packageName) &&
          !isIgnoredPhoneOrCallAppName(launchedApp.label)
        ) {
          val fallbackPkg = launchedApp.packageName.trim()
          val fallbackLabel = launchedApp.label.ifBlank {
            resolveAppLabel(context, fallbackPkg).ifBlank { "Android Media Player" }
          }
          val existing = externalSystemMediaInfo
          if (existing != null && existing.isExternalApp && (fallbackPkg.isBlank() || existing.sourcePackage == fallbackPkg)) {
            externalMediaSetFromSystemSync = true
            if (!existing.isPlaying) {
              externalSystemMediaInfo = existing.copy(
                isPlaying = true,
                positionUpdateElapsedRealtimeMs = safeElapsedRealtimeMs(),
              )
              refreshAllWidgetsForMediaStateChange(context)
            }
          } else {
            val iconBmp = loadAppIconBitmap(context, fallbackPkg)
            val accentRgb = launchedApp.accentRgb.takeIf { it != 0 }
            applyExternalSystemMediaMetadata(
              title = fallbackLabel,
              sourcePackage = fallbackPkg,
              sourceAppName = fallbackLabel,
              isPlaying = true,
              artworkBitmap = iconBmp,
              notificationColorRgb = accentRgb,
              fromSystemSync = true,
            )
          }
          return
        }

        if ((activeExternalMediaController != null || externalMediaSetFromSystemSync) && externalSystemMediaInfo != null) {
          activeExternalMediaController = null
          if (externalSystemMediaInfo?.isPlaying == true) {
            externalSystemMediaInfo = externalSystemMediaInfo?.copy(isPlaying = false)
            refreshAllWidgetsForMediaStateChange(context)
          }
        }
        return
      }

      fun isControllerPlaying(ctrl: MediaController): Boolean {
        return try {
          val st = ctrl.playbackState?.state
          if (isPlaybackStateActive(st)) return true
          val notif = activeMediaNotifications[ctrl.packageName]
          val explicitlyStoppedOrPaused =
            st == PlaybackState.STATE_PAUSED ||
              st == PlaybackState.STATE_STOPPED ||
              st == PlaybackState.STATE_ERROR
          !explicitlyStoppedOrPaused && (notif?.isOngoing == true || systemMusicStreamActive)
        } catch (_: Throwable) {
          false
        }
      }

      fun controllerSelectionScore(ctrl: MediaController): Long {
        return try {
          val st = ctrl.playbackState?.state
          val notif = activeMediaNotifications[ctrl.packageName]
          val lastUpdate = try { ctrl.playbackState?.lastPositionUpdateTime ?: 0L } catch (_: Throwable) { 0L }
          val notifTime = notif?.postedAtMs ?: 0L
          val stateScore = when {
            isPlaybackStateActive(st) -> 4_000_000_000_000L
            notif?.isOngoing == true -> 3_000_000_000_000L
            st == PlaybackState.STATE_PAUSED -> 2_000_000_000_000L
            st != PlaybackState.STATE_STOPPED && st != PlaybackState.STATE_NONE && st != PlaybackState.STATE_ERROR -> 1_000_000_000_000L
            else -> 0L
          }
          stateScore + max(notifTime, lastUpdate)
        } catch (_: Throwable) {
          0L
        }
      }

      val chosen = externalControllers.firstOrNull { isControllerPlaying(it) }
        ?: externalControllers.maxByOrNull { controllerSelectionScore(it) }
        ?: externalControllers.first()
      activeExternalMediaController = chosen

      val playing = isControllerPlaying(chosen) ||
        (
          systemMusicStreamActive &&
            externalControllers.size == 1 &&
            chosen.playbackState?.state != PlaybackState.STATE_STOPPED &&
            chosen.playbackState?.state != PlaybackState.STATE_PAUSED
          )
      val meta = try { chosen.metadata } catch (_: Throwable) { null }
      val desc = try { meta?.description } catch (_: Throwable) { null }
      val pbState = try { chosen.playbackState } catch (_: Throwable) { null }
      val cachedNotif = activeMediaNotifications[chosen.packageName]

      val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        ?: desc?.title?.toString()
        ?: cachedNotif?.title
        ?: ""
      val artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
        ?: desc?.subtitle?.toString()
        ?: cachedNotif?.artist
        ?: ""
      val album = meta?.getString(MediaMetadata.METADATA_KEY_ALBUM)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
        ?: desc?.description?.toString()
        ?: cachedNotif?.album
        ?: ""
      val genre = meta?.getString(MediaMetadata.METADATA_KEY_GENRE) ?: ""
      val durationMs = try {
        meta?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0L) ?: 0L
      } catch (_: Throwable) {
        0L
      }
      val positionMs = try {
        pbState?.position?.takeIf { it >= 0L } ?: 0L
      } catch (_: Throwable) {
        0L
      }
      val posUpdateTime = try {
        pbState?.lastPositionUpdateTime?.takeIf { it > 0L } ?: safeElapsedRealtimeMs()
      } catch (_: Throwable) {
        safeElapsedRealtimeMs()
      }
      val speed = try {
        pbState?.playbackSpeed?.takeIf { it > 0f } ?: 1.0f
      } catch (_: Throwable) {
        1.0f
      }
      val actions = try {
        pbState?.actions ?: 0L
      } catch (_: Throwable) {
        0L
      }
      val canSeek = durationMs > 0L && (actions == 0L || (actions and PlaybackState.ACTION_SEEK_TO) != 0L)
      val canSkipNext = actions == 0L || (actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L
      val canSkipPrevious = actions == 0L || (actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L

      val artUri = meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_ART_URI)
        ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)
        ?: desc?.iconUri?.toString()

      val artBitmap = try { meta?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) } catch (_: Throwable) { null }
        ?: try { meta?.getBitmap(MediaMetadata.METADATA_KEY_ART) } catch (_: Throwable) { null }
        ?: try { meta?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON) } catch (_: Throwable) { null }
        ?: try { desc?.iconBitmap } catch (_: Throwable) { null }
        ?: loadArtworkBitmapFromUri(context, artUri, reqSizePx = 360)
        ?: cachedNotif?.artworkBitmap
        ?: findLocalOrMediaStoreArtworkBitmap(context, title, artist, album)
        ?: (if (cachedNotif?.colorRgb == null) loadAppIconBitmap(context, chosen.packageName) else null)

      val appLabel = resolveAppLabel(context, chosen.packageName)
      val notifCustomActions = cachedNotif?.customActions.orEmpty()
      val customActionsFromPlaybackState = try {
        pbState?.customActions.orEmpty().mapIndexedNotNull { idx, ca ->
          val actId = ca?.action?.toString()?.trim().orEmpty()
          val actName = ca?.name?.toString()?.trim().orEmpty().ifBlank { actId }
          if (actId.isBlank() && actName.isBlank()) {
            null
          } else {
            val iconRes = ca.icon
            val iconBmp = loadExternalCustomActionIconBitmap(context, chosen.packageName, iconRes)
              ?: notifCustomActions.getOrNull(idx)?.iconBitmap
            SystemMediaCustomAction(
              action = actId.ifBlank { actName },
              label = actName.ifBlank { actId },
              iconResId = iconRes,
              iconBitmap = iconBmp,
              extras = ca.extras,
              pendingIntent = notifCustomActions.getOrNull(idx)?.pendingIntent,
            )
          }
        }
      } catch (_: Throwable) {
        emptyList()
      }
      val resolvedCustomActions = when {
        customActionsFromPlaybackState.size >= 2 || notifCustomActions.isEmpty() ->
          customActionsFromPlaybackState.ifEmpty { notifCustomActions }
        customActionsFromPlaybackState.isEmpty() -> notifCustomActions
        else -> {
          val first = customActionsFromPlaybackState.first()
          val extraNotifActions = notifCustomActions.filterNot {
            it.label.equals(first.label, ignoreCase = true) || it.action.equals(first.action, ignoreCase = true)
          }
          customActionsFromPlaybackState + extraNotifActions
        }
      }

      applyExternalSystemMediaMetadata(
        title = title.ifBlank { appLabel },
        artist = artist,
        album = album,
        genre = genre,
        sourcePackage = chosen.packageName,
        sourceAppName = appLabel,
        isPlaying = playing,
        artworkBitmap = artBitmap,
        notificationColorRgb = cachedNotif?.colorRgb,
        durationMs = durationMs,
        positionMs = positionMs,
        positionUpdateElapsedRealtimeMs = posUpdateTime,
        playbackSpeed = speed,
        canSeek = canSeek,
        canSkipNext = canSkipNext,
        canSkipPrevious = canSkipPrevious,
        customActions = resolvedCustomActions,
        fromSystemSync = true,
      )
    } catch (_: Throwable) {}
  }

  private fun resolveActiveExternalController(context: Context? = monitoredAppContext): MediaController? {
    val current = activeExternalMediaController
    val targetPkg = externalSystemMediaInfo?.sourcePackage.orEmpty()
    if (current != null && !isIgnoredPhoneOrCallMediaController(context, current)) {
      val matches = try {
        targetPkg.isBlank() || current.packageName == targetPkg
      } catch (_: Throwable) {
        false
      }
      if (matches) return current
    }
    synchronized(systemMediaMonitorLock) {
      val validControllers = engineTrackedControllers.keys.filterNot {
        isIgnoredPhoneOrCallMediaController(context, it)
      }
      val tracked = validControllers.firstOrNull { ctrl ->
        try {
          targetPkg.isBlank() || ctrl.packageName == targetPkg
        } catch (_: Throwable) {
          false
        }
      } ?: validControllers.firstOrNull()
      if (tracked != null) {
        activeExternalMediaController = tracked
        return tracked
      }
    }
    val appCtx = context?.applicationContext ?: context ?: monitoredAppContext
    if (appCtx != null) {
      val notifToken = (
        if (targetPkg.isNotBlank() && !isIgnoredPhoneOrCallPackage(appCtx, targetPkg)) {
          activeMediaNotifications[targetPkg]?.sessionToken
        } else {
          null
        }
        ) ?: activeMediaNotifications.values
          .filterNot { isIgnoredPhoneOrCallPackage(appCtx, it.packageName) }
          .firstNotNullOfOrNull { it.sessionToken }
      if (notifToken != null) {
        return try {
          MediaController(appCtx, notifToken)
            .takeUnless { isIgnoredPhoneOrCallMediaController(appCtx, it) }
            ?.also { activeExternalMediaController = it }
        } catch (_: Throwable) {
          null
        }
      }
    }
    return null
  }

  private fun dispatchExternalMediaKeyEvent(context: Context?, keyCode: Int): Boolean {
    val appCtx = context?.applicationContext ?: context ?: monitoredAppContext ?: return false
    return try {
      val audioManager = appCtx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
      val now = safeElapsedRealtimeMs()
      val downEvent = KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0)
      val upEvent = KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0)
      audioManager.dispatchMediaKeyEvent(downEvent)
      audioManager.dispatchMediaKeyEvent(upEvent)
      true
    } catch (_: Throwable) {
      false
    }
  }

  /**
   * Computes the live interpolated playback position (in milliseconds) of the currently active
   * external Android system media session.
   */
  fun currentExternalSystemMediaPositionMs(
    nowElapsedRealtimeMs: Long = safeElapsedRealtimeMs(),
  ): Long {
    val info = externalSystemMediaInfo ?: return 0L
    val basePos = info.positionMs.coerceAtLeast(0L)
    val rawPos = if (info.isPlaying && info.positionUpdateElapsedRealtimeMs > 0L && nowElapsedRealtimeMs > info.positionUpdateElapsedRealtimeMs) {
      val deltaMs = ((nowElapsedRealtimeMs - info.positionUpdateElapsedRealtimeMs) * info.playbackSpeed.coerceAtLeast(0f)).toLong()
      basePos + deltaMs
    } else {
      basePos
    }
    return if (info.durationMs > 0L) {
      rawPos.coerceIn(0L, info.durationMs)
    } else {
      rawPos.coerceAtLeast(0L)
    }
  }

  /**
   * Toggles Play / Pause on the active external Android system media player session.
   */
  fun toggleExternalSystemMediaPlayPause(context: Context? = monitoredAppContext): Boolean {
    val current = externalSystemMediaInfo
    val targetPlaying = !(current?.isPlaying ?: false)
    externalSystemMediaLastDispatchedAction = if (targetPlaying) "PLAY" else "PAUSE"
    val nowElapsed = safeElapsedRealtimeMs()
    val frozenPos = currentExternalSystemMediaPositionMs(nowElapsed)

    val controller = resolveActiveExternalController(context)
    var dispatched = false
    if (controller != null) {
      try {
        if (targetPlaying) {
          controller.transportControls.play()
        } else {
          controller.transportControls.pause()
        }
        dispatched = true
      } catch (_: Throwable) {}
    }
    if (!dispatched) {
      dispatched = dispatchExternalMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    if (current != null) {
      externalSystemMediaInfo = current.copy(
        isPlaying = targetPlaying,
        positionMs = frozenPos,
        positionUpdateElapsedRealtimeMs = nowElapsed,
      )
      refreshAllWidgetsForMediaStateChange(context ?: monitoredAppContext)
      return true
    }
    refreshAllWidgetsForMediaStateChange(context ?: monitoredAppContext)
    return dispatched
  }

  /**
   * Pauses the active external Android system media session if it is currently playing.
   */
  fun pauseExternalSystemMediaIfPlaying(context: Context? = monitoredAppContext): Boolean {
    val current = externalSystemMediaInfo ?: return false
    if (!current.isPlaying) return false
    return toggleExternalSystemMediaPlayPause(context)
  }

  /**
   * Skips to the next track in the active external Android system media player session.
   */
  fun skipExternalSystemMediaToNext(context: Context? = monitoredAppContext): Boolean {
    val current = externalSystemMediaInfo
    externalSystemMediaLastDispatchedAction = "NEXT"
    val nowElapsed = safeElapsedRealtimeMs()

    val controller = resolveActiveExternalController(context)
    var dispatched = false
    if (controller != null) {
      try {
        controller.transportControls.skipToNext()
        dispatched = true
      } catch (_: Throwable) {}
    }
    if (!dispatched) {
      dispatched = dispatchExternalMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    if (current != null) {
      externalSystemMediaInfo = current.copy(
        positionMs = 0L,
        positionUpdateElapsedRealtimeMs = nowElapsed,
      )
    }
    val appCtx = context?.applicationContext ?: context ?: monitoredAppContext
    if (appCtx != null) {
      try {
        mainHandler.postDelayed({ syncExternalSystemMediaSessions(appCtx) }, 180L)
      } catch (_: Throwable) {}
    }
    return current != null || dispatched
  }

  /**
   * Skips to the previous track in the active external Android system media player session.
   */
  fun skipExternalSystemMediaToPrevious(context: Context? = monitoredAppContext): Boolean {
    val current = externalSystemMediaInfo
    externalSystemMediaLastDispatchedAction = "PREVIOUS"
    val nowElapsed = safeElapsedRealtimeMs()

    val controller = resolveActiveExternalController(context)
    var dispatched = false
    if (controller != null) {
      try {
        controller.transportControls.skipToPrevious()
        dispatched = true
      } catch (_: Throwable) {}
    }
    if (!dispatched) {
      dispatched = dispatchExternalMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    if (current != null) {
      externalSystemMediaInfo = current.copy(
        positionMs = 0L,
        positionUpdateElapsedRealtimeMs = nowElapsed,
      )
    }
    val appCtx = context?.applicationContext ?: context ?: monitoredAppContext
    if (appCtx != null) {
      try {
        mainHandler.postDelayed({ syncExternalSystemMediaSessions(appCtx) }, 180L)
      } catch (_: Throwable) {}
    }
    return current != null || dispatched
  }

  /**
   * Seeks to [positionMs] in the active external Android system media player session.
   */
  fun seekExternalSystemMediaTo(
    positionMs: Long,
    context: Context? = monitoredAppContext,
  ): Boolean {
    val current = externalSystemMediaInfo
    val clamped = if (current != null && current.durationMs > 0L) {
      positionMs.coerceIn(0L, current.durationMs)
    } else {
      positionMs.coerceAtLeast(0L)
    }
    externalSystemMediaLastDispatchedAction = "SEEK:$clamped"
    val nowElapsed = safeElapsedRealtimeMs()

    val controller = resolveActiveExternalController(context)
    var dispatched = false
    if (controller != null) {
      try {
        controller.transportControls.seekTo(clamped)
        dispatched = true
      } catch (_: Throwable) {}
    }

    if (current != null) {
      externalSystemMediaInfo = current.copy(
        positionMs = clamped,
        positionUpdateElapsedRealtimeMs = nowElapsed,
      )
      return true
    }
    return dispatched
  }

  /**
   * Returns `true` if [customAction] represents an active/toggled-on state (such as `"Shuffle On"`,
   * `"Smart Shuffle"`, `"Repeat All"`, `"Repeat One"`, `"Unfavorite"`, `"Liked"`, etc.).
   */
  fun isExternalCustomActionHighlighted(customAction: SystemMediaCustomAction): Boolean {
    val combined = "${customAction.action} ${customAction.label}".lowercase(Locale.ROOT)
    if (
      combined.contains("off") ||
      combined.contains("disable") ||
      combined.contains("add to fav") ||
      combined.contains("unliked")
    ) {
      return false
    }
    return combined.contains("shuffle on") ||
      combined.contains("smart shuffle") ||
      combined.contains("repeat all") ||
      combined.contains("repeat one") ||
      combined.contains("repeat 1") ||
      combined.contains("unfavorite") ||
      combined.contains("remove from fav") ||
      combined.contains("favorited") ||
      combined.contains("liked")
  }

  /**
   * Triggers an extra [SystemMediaCustomAction] on the active external Android system media player
   * session (via [MediaController.TransportControls.sendCustomAction] or the notification action's
   * [PendingIntent]).
   */
  fun triggerExternalSystemMediaCustomAction(
    customAction: SystemMediaCustomAction,
    context: Context? = monitoredAppContext,
  ): Boolean {
    externalSystemMediaLastDispatchedAction = "CUSTOM:${customAction.action}"
    var dispatched = false
    val controller = resolveActiveExternalController(context)
    if (controller != null && customAction.action.isNotBlank() && !customAction.action.startsWith("notif_action_")) {
      try {
        controller.transportControls.sendCustomAction(customAction.action, customAction.extras)
        dispatched = true
      } catch (_: Throwable) {}
    }
    if (!dispatched && customAction.pendingIntent != null) {
      try {
        customAction.pendingIntent.send()
        dispatched = true
      } catch (_: Throwable) {}
    }
    val appCtx = context?.applicationContext ?: context ?: monitoredAppContext
    if (appCtx != null) {
      try {
        mainHandler.postDelayed({ syncExternalSystemMediaSessions(appCtx) }, 180L)
      } catch (_: Throwable) {}
    }
    return dispatched || externalSystemMediaInfo != null
  }

  /**
   * Curated catalog of popular music & audio streaming apps shown in the Home view's
   * preferred music app picker before the custom picker option at the end.
   */
  val POPULAR_STREAMING_MUSIC_APPS: List<PreferredMusicAppLink> = listOf(
    PreferredMusicAppLink(
      id = "spotify",
      label = "Spotify",
      packageName = "com.spotify.music",
      webUrl = "https://open.spotify.com",
      accentRgb = 0x1DB954,
    ),
    PreferredMusicAppLink(
      id = "youtube_music",
      label = "YouTube Music",
      packageName = "com.google.android.apps.youtube.music",
      webUrl = "https://music.youtube.com",
      accentRgb = 0xFF0033,
    ),
    PreferredMusicAppLink(
      id = "apple_music",
      label = "Apple Music",
      packageName = "com.apple.android.music",
      webUrl = "https://music.apple.com",
      accentRgb = 0xFA243C,
    ),
    PreferredMusicAppLink(
      id = "soundcloud",
      label = "SoundCloud",
      packageName = "com.soundcloud.android",
      webUrl = "https://soundcloud.com",
      accentRgb = 0xFF5500,
    ),
    PreferredMusicAppLink(
      id = "amazon_music",
      label = "Amazon Music",
      packageName = "com.amazon.mp3",
      webUrl = "https://music.amazon.com",
      accentRgb = 0x00A8E1,
    ),
    PreferredMusicAppLink(
      id = "tidal",
      label = "TIDAL",
      packageName = "com.aspiro.tidal",
      webUrl = "https://listen.tidal.com",
      accentRgb = 0x00E5FF,
    ),
    PreferredMusicAppLink(
      id = "pandora",
      label = "Pandora",
      packageName = "com.pandora.android",
      webUrl = "https://www.pandora.com",
      accentRgb = 0x224099,
    ),
    PreferredMusicAppLink(
      id = "deezer",
      label = "Deezer",
      packageName = "deezer.android.app",
      webUrl = "https://www.deezer.com",
      accentRgb = 0xA238FF,
    ),
    PreferredMusicAppLink(
      id = "bandcamp",
      label = "Bandcamp",
      packageName = "com.bandcamp.android",
      webUrl = "https://bandcamp.com",
      accentRgb = 0x629AA9,
    ),
    PreferredMusicAppLink(
      id = "pocket_casts",
      label = "Pocket Casts",
      packageName = "au.com.shiftyjelly.pocketcasts",
      webUrl = "https://play.pocketcasts.com",
      accentRgb = 0xF43E37,
    ),
    PreferredMusicAppLink(
      id = "audible",
      label = "Audible",
      packageName = "com.audible.application",
      webUrl = "https://www.audible.com",
      accentRgb = 0xF8991D,
    ),
    PreferredMusicAppLink(
      id = "iheartradio",
      label = "iHeartRadio",
      packageName = "com.clearchannel.iheartradio.controller",
      webUrl = "https://www.iheart.com",
      accentRgb = 0xC6002B,
    ),
    PreferredMusicAppLink(
      id = "tunein",
      label = "TuneIn Radio",
      packageName = "tunein.player",
      webUrl = "https://tunein.com",
      accentRgb = 0x14D8CC,
    ),
    PreferredMusicAppLink(
      id = "qobuz",
      label = "Qobuz",
      packageName = "com.qobuz.music",
      webUrl = "https://play.qobuz.com",
      accentRgb = 0x2196F3,
    ),
  )

  /**
   * Last preferred music app link launched from the Home view's floating pill bar.
   */
  var lastLaunchedPreferredMusicApp by mutableStateOf<PreferredMusicAppLink?>(null)
    internal set

  /**
   * Optional test override for installed streaming apps returned by [discoverPopularStreamingApps].
   */
  var installedStreamingAppsOverrideForTest: List<PreferredMusicAppLink>? = null

  /**
   * Optional test override for installed device apps returned by [discoverCustomPickerInstalledApps].
   */
  var customPickerInstalledAppsOverrideForTest: List<PreferredMusicAppLink>? = null

  fun serializePreferredMusicApps(apps: List<PreferredMusicAppLink>): String {
    return apps.joinToString("\n") { app ->
      val safeId = app.id.replace("\t", " ").replace("\n", " ").trim()
      val safeLabel = app.label.replace("\t", " ").replace("\n", " ").trim()
      val safePkg = app.packageName.replace("\t", " ").replace("\n", " ").trim()
      val safeUrl = app.webUrl.replace("\t", " ").replace("\n", " ").trim()
      val hexRgb = (app.accentRgb and 0xFFFFFF).toString(16)
      val customFlag = if (app.isCustom) "1" else "0"
      "$safeId\t$safeLabel\t$safePkg\t$safeUrl\t$hexRgb\t$customFlag"
    }
  }

  fun deserializePreferredMusicApps(raw: String?): List<PreferredMusicAppLink> {
    if (raw.isNullOrBlank()) return emptyList()
    val result = mutableListOf<PreferredMusicAppLink>()
    val seenIds = mutableSetOf<String>()
    for (line in raw.lines()) {
      val trimmed = line.trim()
      if (trimmed.isEmpty()) continue
      val parts = trimmed.split("\t")
      if (parts.size < 3) continue
      val id = parts[0].trim().ifEmpty { continue }
      if (!seenIds.add(id)) continue
      val label = parts[1].trim().ifEmpty { id }
      val pkg = parts[2].trim()
      val webUrl = parts.getOrNull(3)?.trim().orEmpty()
      val rgb = parts.getOrNull(4)?.trim()?.toIntOrNull(16)
        ?: POPULAR_STREAMING_MUSIC_APPS.firstOrNull { it.id == id || it.packageName == pkg }?.accentRgb
        ?: 0x1DB954
      val isCustom = parts.getOrNull(5)?.trim() == "1"
      result.add(
        PreferredMusicAppLink(
          id = id,
          label = label,
          packageName = pkg,
          webUrl = webUrl,
          accentRgb = rgb and 0xFFFFFF,
          isCustom = isCustom,
        )
      )
    }
    return result
  }

  fun createCustomPreferredMusicAppLink(
    label: String,
    packageOrUrl: String,
  ): PreferredMusicAppLink {
    val cleanLabel = label.trim().ifEmpty {
      packageOrUrl.trim().substringAfterLast('.').substringAfterLast('/').ifEmpty { "Custom Music App" }
    }
    val cleanTarget = packageOrUrl.trim()
    val isUrl = cleanTarget.startsWith("http://", ignoreCase = true) ||
      cleanTarget.startsWith("https://", ignoreCase = true)
    val slug = cleanLabel.lowercase(Locale.ROOT)
      .replace(Regex("[^a-z0-9]+"), "_")
      .trim('_')
      .ifEmpty { "custom_app" }
    val (primaryRgb, _, _) = computeDeterministicArtworkColors(
      album = cleanLabel,
      artist = cleanTarget,
      genre = "Streaming",
    )
    return PreferredMusicAppLink(
      id = "custom_${slug}_${(cleanTarget.hashCode() and 0x7FFFFFFF) % 10000}",
      label = cleanLabel,
      packageName = if (isUrl) "" else cleanTarget,
      webUrl = if (isUrl) cleanTarget else "",
      accentRgb = primaryRgb,
      isCustom = true,
    )
  }

  /**
   * Returns only the installed streaming music apps on [context] (filtering [POPULAR_STREAMING_MUSIC_APPS]
   * to installed packages and including any additional installed audio/media apps on the device).
   */
  fun discoverPopularStreamingApps(
    context: Context?,
    searchQuery: String = "",
  ): List<PreferredMusicAppLink> {
    val overrideList = installedStreamingAppsOverrideForTest
    val pm = context?.packageManager
    val ownPkg = context?.packageName.orEmpty()
    val installedPresets = mutableListOf<PreferredMusicAppLink>()
    val extraAudioApps = mutableListOf<PreferredMusicAppLink>()

    if (overrideList != null) {
      installedPresets.addAll(overrideList.map { it.copy(isInstalled = true) })
    } else if (pm != null) {
      for (preset in POPULAR_STREAMING_MUSIC_APPS) {
        try {
          if (pm.getLaunchIntentForPackage(preset.packageName) != null) {
            installedPresets.add(preset.copy(isInstalled = true))
          }
        } catch (_: Throwable) {}
      }

      // Also check installed audio category / media browser apps not already in POPULAR_STREAMING_MUSIC_APPS
      try {
        val knownPkgs = POPULAR_STREAMING_MUSIC_APPS.map { it.packageName }.toSet()
        val mediaActions = listOf(
          "android.media.browse.MediaBrowserService",
          "androidx.media3.session.MediaSessionService",
          "androidx.media3.session.MediaLibraryService",
        )
        val discoveredPkgs = mutableSetOf<String>()
        for (action in mediaActions) {
          val services = try {
            pm.queryIntentServices(Intent(action), 0)
          } catch (_: Throwable) {
            emptyList()
          }
          for (ri in services) {
            val pkg = ri.serviceInfo?.packageName ?: continue
            if (pkg != ownPkg && pkg !in knownPkgs) {
              discoveredPkgs.add(pkg)
            }
          }
        }
        for (pkg in discoveredPkgs) {
          if (isIgnoredPhoneOrCallPackage(context, pkg)) continue
          if (pm.getLaunchIntentForPackage(pkg) == null) continue
          val appLabel = resolveAppLabel(context, pkg).ifBlank { continue }
          if (isIgnoredPhoneOrCallAppName(appLabel)) continue
          val (primaryRgb, _, _) = computeDeterministicArtworkColors(appLabel, pkg, "Audio")
          extraAudioApps.add(
            PreferredMusicAppLink(
              id = "audio_${pkg.replace('.', '_')}",
              label = appLabel,
              packageName = pkg,
              accentRgb = primaryRgb,
              isInstalled = true,
            )
          )
        }
      } catch (_: Throwable) {}
    }

    val combined = installedPresets + extraAudioApps
    val q = searchQuery.trim()
    val filtered = if (q.isEmpty()) {
      combined
    } else {
      combined.filter { app ->
        app.label.contains(q, ignoreCase = true) ||
          app.packageName.contains(q, ignoreCase = true)
      }
    }
    return filtered.sortedWith(
      compareBy<PreferredMusicAppLink> {
        POPULAR_STREAMING_MUSIC_APPS.indexOfFirst { p -> p.id == it.id }.let { idx -> if (idx >= 0) idx else Int.MAX_VALUE }
      }.thenBy { it.label.lowercase(Locale.ROOT) }
    )
  }

  /**
   * Returns all launchable apps installed on the device for the Custom App Picker option at the
   * end of the preferred music app dialog.
   */
  fun discoverCustomPickerInstalledApps(
    context: Context?,
    searchQuery: String = "",
  ): List<PreferredMusicAppLink> {
    val overrideList = customPickerInstalledAppsOverrideForTest
    if (overrideList != null) {
      val q = searchQuery.trim()
      return if (q.isEmpty()) {
        overrideList
      } else {
        overrideList.filter { it.label.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true) }
      }
    }
    val pm = context?.packageManager ?: return emptyList()
    val ownPkg = context.packageName
    val results = mutableListOf<PreferredMusicAppLink>()
    val seenPkgs = mutableSetOf<String>()
    try {
      val mainIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
      }
      val resolved = pm.queryIntentActivities(mainIntent, 0)
      for (ri in resolved) {
        val pkg = ri.activityInfo?.packageName ?: continue
        if (pkg == ownPkg || !seenPkgs.add(pkg)) continue
        val label = try {
          ri.loadLabel(pm)?.toString()?.trim().orEmpty()
        } catch (_: Throwable) {
          ""
        }.ifEmpty { resolveAppLabel(context, pkg) }
        if (label.isBlank()) continue
        val presetMatch = POPULAR_STREAMING_MUSIC_APPS.firstOrNull { it.packageName == pkg }
        val rgb = presetMatch?.accentRgb ?: computeDeterministicArtworkColors(label, pkg, "Custom").first
        results.add(
          PreferredMusicAppLink(
            id = presetMatch?.id ?: "custom_${pkg.replace('.', '_')}",
            label = label,
            packageName = pkg,
            webUrl = presetMatch?.webUrl.orEmpty(),
            accentRgb = rgb,
            isCustom = presetMatch == null,
            isInstalled = true,
          )
        )
      }
    } catch (_: Throwable) {}

    val q = searchQuery.trim()
    val filtered = if (q.isEmpty()) {
      results
    } else {
      results.filter {
        it.label.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true)
      }
    }
    return filtered.sortedBy { it.label.lowercase(Locale.ROOT) }
  }

  /**
   * Launches [app] from the Home view's floating preferred music app bar, falling back to its web
   * player URL when the native Android package is not installed on the device.
   */
  fun launchPreferredMusicApp(
    context: Context,
    app: PreferredMusicAppLink,
  ): Boolean {
    lastLaunchedPreferredMusicApp = app
    val pkg = app.packageName.trim()
    if (pkg.isNotEmpty() && !pkg.startsWith("http://", ignoreCase = true) && !pkg.startsWith("https://", ignoreCase = true)) {
      try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)?.apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (launchIntent != null) {
          context.startActivity(launchIntent)
          return true
        }
      } catch (_: Throwable) {}
    }

    val targetUrl = when {
      app.webUrl.isNotBlank() -> app.webUrl.trim()
      pkg.startsWith("http://", ignoreCase = true) || pkg.startsWith("https://", ignoreCase = true) -> pkg
      pkg.isNotBlank() -> "https://play.google.com/store/apps/details?id=$pkg"
      else -> ""
    }
    if (targetUrl.isNotEmpty()) {
      try {
        val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(viewIntent)
        return true
      } catch (_: Throwable) {}
    }
    return false
  }
  /**
   * Package name (or app identifier) of the last external media app opened via
   * [openExternalSystemMediaApp], useful for verification and diagnostics.
   */
  var lastOpenedExternalMediaPackage by mutableStateOf<String?>(null)
    internal set

  /**
   * Optional test override for [openExternalSystemMediaApp].
   */
  var openExternalMediaAppOverrideForTest: ((String) -> Boolean)? = null

  private fun unwrapActivityContext(context: Context): Context {
    var current: Context? = context
    while (current is android.content.ContextWrapper) {
      if (current is Activity) return current
      current = current.baseContext
    }
    return context
  }

  private fun createPendingIntentActivityStartOptionsBundle(): Bundle? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return null
    return try {
      val options = android.app.ActivityOptions.makeBasic()
      if (Build.VERSION.SDK_INT >= 36) {
        options.setPendingIntentBackgroundActivityStartMode(
          android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS
        )
      } else {
        @Suppress("DEPRECATION")
        options.setPendingIntentBackgroundActivityStartMode(
          android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
        )
      }
      options.toBundle()
    } catch (_: Throwable) {
      null
    }
  }

  private fun sendPendingIntentAllowingActivityStart(
    context: Context,
    pendingIntent: PendingIntent,
  ): Boolean {
    return try {
      val optionsBundle = createPendingIntentActivityStartOptionsBundle()
      if (optionsBundle != null) {
        pendingIntent.send(context, 0, null, null, null, null, optionsBundle)
      } else {
        pendingIntent.send()
      }
      true
    } catch (_: Throwable) {
      try {
        pendingIntent.send()
        true
      } catch (_: Throwable) {
        false
      }
    }
  }

  /**
   * Resolves the package name of the external application currently playing media in the
   * Android system media player.
   */
  fun resolveExternalSystemMediaPackage(context: Context? = monitoredAppContext): String {
    val ownPkg = context?.packageName.orEmpty()
    val info = externalSystemMediaInfo?.takeUnless { isIgnoredPhoneOrCallSystemMediaInfo(it, context) }
    val fromInfoPkg = info?.sourcePackage?.trim().orEmpty()
    if (fromInfoPkg.isNotEmpty() && fromInfoPkg != ownPkg && !isIgnoredPhoneOrCallPackage(context, fromInfoPkg)) {
      return fromInfoPkg
    }

    val controllerPkg = try {
      resolveActiveExternalController(context)?.packageName?.trim().orEmpty()
    } catch (_: Throwable) {
      ""
    }
    if (controllerPkg.isNotEmpty() && controllerPkg != ownPkg && !isIgnoredPhoneOrCallPackage(context, controllerPkg)) {
      return controllerPkg
    }

    val notifPkg = activeMediaNotifications.values
      .filter {
        it.packageName.isNotBlank() &&
          it.packageName != ownPkg &&
          !isIgnoredPhoneOrCallPackage(context, it.packageName)
      }
      .maxByOrNull { (if (it.isOngoing) 1_000_000_000_000L else 0L) + it.postedAtMs }
      ?.packageName
      ?.trim()
      .orEmpty()
    if (notifPkg.isNotEmpty()) {
      return notifPkg
    }

    if (context != null) {
      val activeAudioPkg = findActiveExternalPlaybackPackage(context)?.trim().orEmpty()
      if (activeAudioPkg.isNotEmpty() && activeAudioPkg != ownPkg && !isIgnoredPhoneOrCallPackage(context, activeAudioPkg)) {
        return activeAudioPkg
      }
    }

    val sourceAppName = info?.sourceAppName?.trim().orEmpty()
    if (sourceAppName.isNotEmpty() && !isIgnoredPhoneOrCallAppName(sourceAppName)) {
      val presetMatch = POPULAR_STREAMING_MUSIC_APPS.firstOrNull {
        it.label.equals(sourceAppName, ignoreCase = true) ||
          it.id.equals(sourceAppName, ignoreCase = true)
      }
      if (presetMatch != null && presetMatch.packageName.isNotBlank()) {
        return presetMatch.packageName
      }
      if (context != null) {
        val installedMatch = discoverCustomPickerInstalledApps(context, sourceAppName).firstOrNull {
          it.label.equals(sourceAppName, ignoreCase = true) &&
            !isIgnoredPhoneOrCallPackage(context, it.packageName)
        }
        if (installedMatch != null && installedMatch.packageName.isNotBlank()) {
          return installedMatch.packageName
        }
      }
    }

    return ""
  }

  /**
   * Opens the external application that is currently playing media in the Android system media player.
   */
  fun openExternalSystemMediaApp(context: Context): Boolean {
    val pkg = resolveExternalSystemMediaPackage(context)
    val info = externalSystemMediaInfo
    lastOpenedExternalMediaPackage = pkg.ifBlank {
      info?.sourceAppName?.trim().orEmpty()
    }.takeIf { it.isNotEmpty() }

    val override = openExternalMediaAppOverrideForTest
    if (override != null) {
      return override(pkg)
    }

    val launchContext = unwrapActivityContext(context)
    val controller = resolveActiveExternalController(context)
    val cachedNotif = if (pkg.isNotBlank()) {
      activeMediaNotifications[pkg]
    } else {
      activeMediaNotifications.values
        .filter { it.packageName != context.packageName }
        .maxByOrNull { (if (it.isOngoing) 1_000_000_000_000L else 0L) + it.postedAtMs }
    }

    // 1. Prefer launching the target package's activity directly from our visible foreground context
    // so Android 14+ PendingIntent background activity launch (BAL) restrictions cannot silently block it.
    if (pkg.isNotBlank() && pkg != context.packageName) {
      try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)?.apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        if (launchIntent != null) {
          launchContext.startActivity(launchIntent)
          return true
        }
      } catch (_: Throwable) {}

      try {
        val pm = context.packageManager
        val candidateIntents = listOf(
          Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC).setPackage(pkg),
          Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg),
          Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_DEFAULT).setPackage(pkg),
          Intent(Intent.ACTION_MAIN).setPackage(pkg),
        )
        for (queryIntent in candidateIntents) {
          val resolvedList = try {
            pm.queryIntentActivities(queryIntent, 0)
          } catch (_: Throwable) {
            emptyList()
          }
          val resolved = resolvedList.firstOrNull { it.activityInfo?.exported == true }
            ?: resolvedList.firstOrNull()
          val actInfo = resolved?.activityInfo
          if (actInfo != null) {
            val directIntent = Intent(Intent.ACTION_MAIN).apply {
              component = ComponentName(actInfo.packageName, actInfo.name)
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            launchContext.startActivity(directIntent)
            return true
          }
        }
      } catch (_: Throwable) {}
    }

    // 2. Fallback to MediaController.sessionActivity or Notification.contentIntent with explicit
    // Android 14+ background activity start ActivityOptions.
    val sessionPendingIntent = try {
      controller?.sessionActivity
    } catch (_: Throwable) {
      null
    }
    if (sessionPendingIntent != null && sendPendingIntentAllowingActivityStart(launchContext, sessionPendingIntent)) {
      return true
    }

    val notifPendingIntent = cachedNotif?.contentIntent
    if (notifPendingIntent != null && sendPendingIntentAllowingActivityStart(launchContext, notifPendingIntent)) {
      return true
    }

    return false
  }

  /**
   * Returns the currently active [MusicPlaybackSession], if any.
   */
  fun getActivePlaybackSession(): MusicPlaybackSession? {
    synchronized(sessionLock) {
      return activePlaybackSession
    }
  }

  private var pendingExpandFullPlayer: Boolean = false

  /**
   * Expands the active [MusicPlaybackSession] into the full music player view (or marks a pending
   * expansion request for the next session configuration sync) when launched from the Android
   * system media player.
   */
  fun requestExpandFullPlayer() {
    synchronized(sessionLock) {
      val current = activePlaybackSession
      if (current != null && !current.isReleased) {
        current.expandFullPlayer()
        pendingExpandFullPlayer = false
      } else {
        pendingExpandFullPlayer = true
      }
    }
  }

  /**
   * Stops and releases the active [MusicPlaybackSession], if any.
   */
  fun resetPlaybackSession() {
    val sessionToRelease: MusicPlaybackSession?
    val activity: Activity?
    val observer: LifecycleEventObserver?
    synchronized(sessionLock) {
      sessionToRelease = activePlaybackSession
      activity = activeActivityRef?.get()
      observer = activeLifecycleObserver
      activePlaybackSession = null
      activeActivityRef = null
      activeLifecycleObserver = null
      pendingExpandFullPlayer = false
    }
    activeSessionState = null
    isMusicPlayingOverride = null
    externalMediaSetFromSystemSync = false
    lastBroadcastReceivedElapsedMs = 0L
    externalSystemMediaInfo = null
    activeExternalMediaController = null
    externalSystemMediaLastDispatchedAction = null
    lastOpenedExternalMediaPackage = null
    openExternalMediaAppOverrideForTest = null
    lastLaunchedPreferredMusicApp = null
    installedStreamingAppsOverrideForTest = null
    customPickerInstalledAppsOverrideForTest = null
    lastBroadcastMediaInfo = null
    activeMediaNotifications.clear()
    clearExternalMediaArtworkCaches()
    lastSyncedMediaWidgetPalette = null
    if (activity is LifecycleOwner && observer != null) {
      try {
        activity.lifecycle.removeObserver(observer)
      } catch (_: Throwable) {}
    }
    sessionToRelease?.release()
    val defaultTrack = DEFAULT_SAMPLE_TRACKS.firstOrNull()
    updateActiveTrackPalette(defaultTrack, resolveTrackPalette(defaultTrack))
    mediaMusicWidgetRefreshCount = 0
    mediaThemeWidgetsRefreshCount = 0
  }

  /**
   * Acquires or creates the background-capable [MusicPlaybackSession] scoped to [activity] so that
   * music playback, queue progression, play statistics, and sleep timers continue seamlessly in the
   * background when the user switches from the Music tool to another tool or opens the standalone
   * Music Player from the Android system media player controls.
   */
  fun getOrCreatePlaybackSession(
    context: Context,
    activity: Activity? = null,
    initialDirectory: File? = null,
    initialTracks: List<MusicTrack>? = null,
    initialTab: MusicLibraryTab? = null,
    initialViewMode: MusicViewMode? = null,
    initialPlayerExpanded: Boolean = false,
    initialQueueExpanded: Boolean = false,
    initialGrantedFolderPaths: Set<String>? = null,
    isInspection: Boolean = false,
  ): MusicPlaybackSession {
    if (isInspection) {
      val createdInspection = MusicPlaybackSession(
        context = context,
        initialDirectory = initialDirectory,
        initialTracks = initialTracks,
        initialTab = initialTab,
        initialViewMode = initialViewMode,
        initialPlayerExpanded = initialPlayerExpanded,
        initialQueueExpanded = initialQueueExpanded,
        initialGrantedFolderPaths = initialGrantedFolderPaths,
        isInspection = true,
      )
      activeSessionState = createdInspection
      return createdInspection
    }

    var previousToRelease: MusicPlaybackSession? = null
    val session: MusicPlaybackSession

    synchronized(sessionLock) {
      val shouldExpand = initialPlayerExpanded || pendingExpandFullPlayer
      pendingExpandFullPlayer = false
      val currentSession = activePlaybackSession
      val currentActivity = activeActivityRef?.get()
      val canReuseAcrossActivities = currentSession != null &&
        !currentSession.isReleased &&
        initialGrantedFolderPaths == null &&
        (
          activity == null ||
            currentActivity === activity ||
            initialTracks == null ||
            currentSession.isPlaying ||
            currentSession.currentTrack != null
          )
      if (canReuseAcrossActivities && currentSession != null) {
        if (activity != null && currentActivity !== activity) {
          val oldObserver = activeLifecycleObserver
          if (currentActivity is LifecycleOwner && oldObserver != null) {
            try {
              currentActivity.lifecycle.removeObserver(oldObserver)
            } catch (_: Throwable) {}
          }
          activeActivityRef = WeakReference(activity)
          attachSessionLifecycleObserverLocked(activity, currentSession)
        }
        currentSession.syncConfiguration(
          initialDirectory = initialDirectory,
          initialTracks = initialTracks,
          initialTab = initialTab,
          initialViewMode = initialViewMode,
          initialPlayerExpanded = shouldExpand,
          initialQueueExpanded = initialQueueExpanded,
        )
        if (shouldExpand) {
          currentSession.expandFullPlayer()
        }
        activeSessionState = currentSession
        return currentSession
      }

      val oldObserver = activeLifecycleObserver
      if (currentActivity is LifecycleOwner && oldObserver != null) {
        try {
          currentActivity.lifecycle.removeObserver(oldObserver)
        } catch (_: Throwable) {}
      }

      previousToRelease = currentSession
      val created = MusicPlaybackSession(
        context = context.applicationContext ?: context,
        initialDirectory = initialDirectory,
        initialTracks = initialTracks,
        initialTab = initialTab,
        initialViewMode = initialViewMode,
        initialPlayerExpanded = shouldExpand,
        initialQueueExpanded = initialQueueExpanded,
        initialGrantedFolderPaths = initialGrantedFolderPaths,
        isInspection = false,
      )
      if (shouldExpand) {
        created.expandFullPlayer()
      }
      activePlaybackSession = created
      activeSessionState = created
      activeActivityRef = if (activity != null) WeakReference(activity) else null

      if (activity != null) {
        attachSessionLifecycleObserverLocked(activity, created)
      }
      session = created
    }

    previousToRelease?.release()
    return session
  }

  private fun attachSessionLifecycleObserverLocked(
    activity: Activity,
    session: MusicPlaybackSession,
  ) {
    if (activity !is LifecycleOwner) {
      activeLifecycleObserver = null
      return
    }
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_DESTROY) {
        var shouldRelease = false
        synchronized(sessionLock) {
          val isSameActivity = activeActivityRef?.get() === activity
          val isChangingConfig = try {
            activity.isChangingConfigurations
          } catch (_: Throwable) {
            false
          }
          val keepAliveInBackground = session.configuredInitialTracks == null &&
            (session.isPlaying || session.currentTrack != null)
          if (activePlaybackSession === session && isSameActivity && !isChangingConfig && !keepAliveInBackground) {
            activePlaybackSession = null
            activeActivityRef = null
            activeLifecycleObserver = null
            shouldRelease = true
          } else if (activePlaybackSession === session && isSameActivity) {
            activeActivityRef = null
            activeLifecycleObserver = null
          }
        }
        if (shouldRelease) {
          if (activeSessionState === session) {
            activeSessionState = null
          }
          session.release()
        }
      }
    }
    activeLifecycleObserver = observer
    try {
      activity.lifecycle.addObserver(observer)
    } catch (_: Throwable) {}
  }
}

private val DEFAULT_PLAYBACK_DISPATCHER: CoroutineDispatcher by lazy {
  try {
    if (Looper.getMainLooper() != null) {
      Dispatchers.Main
    } else {
      Dispatchers.Default
    }
  } catch (_: Throwable) {
    Dispatchers.Default
  }
}

private fun defaultPlaybackDispatcher(): CoroutineDispatcher = DEFAULT_PLAYBACK_DISPATCHER

private fun defaultBackgroundDispatcher(playbackContext: CoroutineContext): CoroutineContext =
  if (playbackContext === DEFAULT_PLAYBACK_DISPATCHER) Dispatchers.Default else playbackContext

private fun defaultIoDispatcher(playbackContext: CoroutineContext): CoroutineContext =
  if (playbackContext === DEFAULT_PLAYBACK_DISPATCHER) Dispatchers.IO else playbackContext

/**
 * Persistent playback session and state holder for the Music Player tool.
 *
 * Continues playing audio, advancing the playback queue, tracking listening statistics, and
 * counting down the sleep timer in the background when the user switches to another tool.
 * Heavy play-stats sorting, playlist rebuilding, and Home mix generation are delegated to
 * background coroutines so foreground interactions (such as starring a song) remain instantaneous.
 */
class MusicPlaybackSession(
  context: Context? = null,
  initialDirectory: File? = null,
  initialTracks: List<MusicTrack>? = null,
  initialTab: MusicLibraryTab? = null,
  initialViewMode: MusicViewMode? = null,
  initialPlayerExpanded: Boolean = false,
  initialQueueExpanded: Boolean = false,
  initialGrantedFolderPaths: Set<String>? = null,
  val isInspection: Boolean = false,
  coroutineContext: CoroutineContext = defaultPlaybackDispatcher(),
  private val backgroundCoroutineContext: CoroutineContext = defaultBackgroundDispatcher(coroutineContext),
  private val ioCoroutineContext: CoroutineContext = defaultIoDispatcher(coroutineContext),
) {
  private val appContext: Context? = context?.applicationContext ?: context
  private val cacheDir: File? = if (isInspection) null else appContext?.cacheDir
  private val prefs: SharedPreferences? = if (isInspection || appContext == null) {
    null
  } else {
    appContext.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
  }

  private val scope = CoroutineScope(SupervisorJob() + coroutineContext)
  private var mediaPlayer: MediaPlayer? = null
  private var preparedTrackId: String? = null
  private var activePlayInstanceId: Long = 0L
  private var progressJob: Job? = null
  private var sleepTimerJob: Job? = null
  private var libraryLoadJob: Job? = null
  private var paletteLoadJob: Job? = null
  private var mediaArtworkLoadJob: Job? = null
  internal var favoritesRecalcJob: Job? = null
  internal var statsAndSortRecalcJob: Job? = null
  internal var libraryRebuildJob: Job? = null
  internal var prefsPersistJob: Job? = null
  internal var playStatsPersistJob: Job? = null
  internal var mediaControlSyncJob: Job? = null

  private val audioManager: AudioManager? = if (isInspection || appContext == null) {
    null
  } else {
    try {
      appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    } catch (_: Throwable) {
      null
    }
  }
  private var mediaSession: MediaSession? = null
  private var lastSyncedMediaMetadataTrackId: String? = null
  private var lastSyncedMediaMetadataDurationMs: Long = -1L
  private var lastSyncedMediaArtworkBitmap: Bitmap? = null
  private var lastNotifiedTrackId: String? = null
  private var lastNotifiedIsPlaying: Boolean? = null
  private var lastNotifiedShuffleIconRes: Int = 0
  private var lastNotifiedRepeatIconRes: Int = 0
  private var lastNotifiedFavoriteIconRes: Int = 0
  private var lastNotifiedArtworkBitmap: Bitmap? = null
  private var lastNotifiedColorRgb: Int = 0
  private var audioDisconnectReceiver: BroadcastReceiver? = null
  private var mediaControlReceiver: BroadcastReceiver? = null
  private var audioDeviceCallback: AudioDeviceCallback? = null
  private var audioFocusRequest: Any? = null
  private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
    when (focusChange) {
      AudioManager.AUDIOFOCUS_LOSS,
      AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
        if (isPlaying) {
          setPlayingState(false)
        }
      }
    }
  }

  var bluetoothDisconnectPauseCount: Int = 0
    private set

  var systemMediaControlState: SystemMediaControlState = SystemMediaControlState()
    private set

  var isReleased: Boolean = false
    private set

  /**
   * Indicates whether a background playlist / play-statistics recalculation task is currently running.
   */
  var isRecalculatingInBackground by mutableStateOf(false)
    internal set

  /**
   * Count of completed background favorite/stats/playlist recalculation tasks.
   */
  var backgroundRecalculationCount by mutableIntStateOf(0)
    internal set

  internal var configuredInitialDirectory: File? = initialDirectory
    private set
  internal var configuredInitialTracks: List<MusicTrack>? = initialTracks
    private set
  internal var configuredInitialTab: MusicLibraryTab? = initialTab
    private set
  internal var configuredInitialViewMode: MusicViewMode? = initialViewMode
    private set
  internal var configuredInitialPlayerExpanded: Boolean = initialPlayerExpanded
    private set
  internal var configuredInitialQueueExpanded: Boolean = initialQueueExpanded
    private set

  // Library Tab Order & Selected Tab: Home, Playlists, Songs, Albums, Artists, Genres
  var tabOrder by mutableStateOf(
    MusicPlayerEngine.parseTabOrder(prefs?.getString(PREF_KEY_MUSIC_TAB_ORDER, null))
  )

  fun reorderTabs(fromIndex: Int, toIndex: Int): Boolean {
    val updated = MusicPlayerEngine.reorderTabs(tabOrder, fromIndex, toIndex)
    if (updated != tabOrder) {
      tabOrder = updated
      persistStringPrefInBackground(PREF_KEY_MUSIC_TAB_ORDER, MusicPlayerEngine.serializeTabOrder(updated))
      return true
    }
    return false
  }

  fun updateTabOrder(newOrder: List<MusicLibraryTab>) {
    val normalized = MusicPlayerEngine.parseTabOrder(MusicPlayerEngine.serializeTabOrder(newOrder))
    if (normalized != tabOrder) {
      tabOrder = normalized
      persistStringPrefInBackground(PREF_KEY_MUSIC_TAB_ORDER, MusicPlayerEngine.serializeTabOrder(normalized))
    }
  }

  var selectedTab by mutableStateOf(
    initialTab ?: run {
      val savedTab = prefs?.getString(PREF_KEY_MUSIC_TAB, null)
      if (!savedTab.isNullOrBlank()) {
        MusicLibraryTab.fromNameOrDefault(savedTab)
      } else {
        tabOrder.firstOrNull() ?: MusicLibraryTab.DEFAULT
      }
    }
  )

  // Scoped Storage & Custom Folders State
  var grantedFolderPaths by mutableStateOf<Set<String>>(
    when {
      initialGrantedFolderPaths != null -> initialGrantedFolderPaths
      initialTracks != null ||
        initialDirectory != null ||
        isInspection ||
        prefs?.getBoolean(PREF_KEY_MUSIC_INCLUDE_SAMPLES, false) == true ->
        setOf(MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER.folderPath)
      else -> MusicPlayerEngine.parseGrantedFolderPaths(
        prefs?.getString(PREF_KEY_MUSIC_GRANTED_FOLDER_PATHS, null)
      )
    }
  )
    internal set

  val isMusicFolderGranted: Boolean
    get() = MusicPlayerEngine.isMusicFolderAccessGranted(grantedFolderPaths)

  var pendingPermissionFolderPath by mutableStateOf<String?>(null)

  var customFolders by mutableStateOf<List<MusicFolder>>(
    MusicPlayerEngine.parseCustomFolders(prefs?.getString(PREF_KEY_MUSIC_CUSTOM_FOLDERS, null))
  )
    internal set

  var selectedCustomFolderPath by mutableStateOf<String?>(null)
    internal set

  val selectedCustomFolder: MusicFolder?
    get() = customFolders.firstOrNull { it.folderPath == selectedCustomFolderPath }

  fun requestMusicFolderPermissionPrompt(
    folderPath: String = MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER.folderPath,
  ) {
    pendingPermissionFolderPath = folderPath
  }

  fun grantFolderAccess(
    folder: MusicFolder = MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER,
  ) {
    val updated = buildSet {
      addAll(grantedFolderPaths)
      add(folder.folderPath)
      if (
        folder.folderPath.equals(MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER.folderPath, ignoreCase = true) ||
          folder.folderName.equals("Music", ignoreCase = true)
      ) {
        add(MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER.folderPath)
      }
    }
    val wasGranted = isMusicFolderGranted
    grantedFolderPaths = updated
    if (pendingPermissionFolderPath == folder.folderPath || isMusicFolderGranted) {
      pendingPermissionFolderPath = null
    }
    persistStringPrefInBackground(
      PREF_KEY_MUSIC_GRANTED_FOLDER_PATHS,
      MusicPlayerEngine.serializeGrantedFolderPaths(updated),
    )
    if (!wasGranted && isMusicFolderGranted) {
      reloadLibraryTracksForGrantedFolders()
    }
  }

  fun addCustomFolder(folder: MusicFolder) {
    val existing = customFolders.firstOrNull {
      it.folderPath.equals(folder.folderPath, ignoreCase = true) ||
        (it.absolutePath != null && folder.absolutePath != null && it.absolutePath == folder.absolutePath)
    }
    val targetFolder = existing ?: folder
    if (existing == null) {
      val nextFolders = customFolders + targetFolder
      customFolders = nextFolders
      persistStringPrefInBackground(
        PREF_KEY_MUSIC_CUSTOM_FOLDERS,
        MusicPlayerEngine.serializeCustomFolders(nextFolders),
      )
    }
    val nextGranted = grantedFolderPaths + targetFolder.folderPath + MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER.folderPath
    grantedFolderPaths = nextGranted
    pendingPermissionFolderPath = null
    persistStringPrefInBackground(
      PREF_KEY_MUSIC_GRANTED_FOLDER_PATHS,
      MusicPlayerEngine.serializeGrantedFolderPaths(nextGranted),
    )
    selectCustomFolder(targetFolder)
    reloadLibraryTracksForGrantedFolders()
  }

  fun removeCustomFolder(folder: MusicFolder) {
    val nextFolders = customFolders.filterNot {
      it.folderPath.equals(folder.folderPath, ignoreCase = true) ||
        it.folderName.equals(folder.folderName, ignoreCase = true)
    }
    if (nextFolders.size != customFolders.size) {
      customFolders = nextFolders
      persistStringPrefInBackground(
        PREF_KEY_MUSIC_CUSTOM_FOLDERS,
        MusicPlayerEngine.serializeCustomFolders(nextFolders),
      )
    }
    if (
      selectedCustomFolderPath != null &&
        (
          selectedCustomFolderPath.equals(folder.folderPath, ignoreCase = true) ||
            nextFolders.none { it.folderPath == selectedCustomFolderPath }
          )
    ) {
      selectedCustomFolderPath = null
      syncSortOrderWithCurrentScope()
    }
  }

  fun selectCustomFolder(folder: MusicFolder?) {
    if (!isMusicFolderGranted) {
      pendingPermissionFolderPath = MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER.folderPath
      return
    }
    val nextPath = folder?.folderPath
    if (selectedCustomFolderPath != nextPath) {
      clearDrillDown()
      selectedCustomFolderPath = nextPath
      syncSortOrderWithCurrentScope()
    }
  }

  fun updateTab(tab: MusicLibraryTab) {
    if (!isMusicFolderGranted) {
      pendingPermissionFolderPath = MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER.folderPath
      return
    }
    val tabChanged = selectedTab != tab || selectedCustomFolderPath != null
    if (tabChanged) {
      syncViewPlayStatsSnapshot()
    }
    selectedCustomFolderPath = null
    selectedTab = tab
    persistStringPrefInBackground(PREF_KEY_MUSIC_TAB, tab.name)
    if (tabChanged) {
      syncSortOrderWithCurrentScope()
    }
  }

  // View Mode: List vs Card
  var viewMode by mutableStateOf(
    initialViewMode ?: MusicViewMode.fromNameOrDefault(prefs?.getString(PREF_KEY_MUSIC_VIEW_MODE, null))
  )

  fun updateViewMode(mode: MusicViewMode) {
    viewMode = mode
    persistStringPrefInBackground(PREF_KEY_MUSIC_VIEW_MODE, mode.name)
  }

  // Card View Columns per Row (2..3)
  var cardsPerRow by mutableIntStateOf(
    (prefs?.getInt(PREF_KEY_MUSIC_CARDS_PER_ROW, DEFAULT_MUSIC_CARDS_PER_ROW) ?: DEFAULT_MUSIC_CARDS_PER_ROW)
      .coerceIn(MIN_MUSIC_CARDS_PER_ROW, MAX_MUSIC_CARDS_PER_ROW)
  )

  fun updateCardsPerRow(count: Int) {
    val coerced = count.coerceIn(MIN_MUSIC_CARDS_PER_ROW, MAX_MUSIC_CARDS_PER_ROW)
    cardsPerRow = coerced
    if (!isReleased) {
      prefs?.edit()?.putInt(PREF_KEY_MUSIC_CARDS_PER_ROW, coerced)?.apply()
    }
  }

  // Per-Section / Per-Folder Sort & Group Configuration and Global Defaults
  var defaultSortOrder by mutableStateOf(
    MusicSortOrder.fromNameOrDefault(prefs?.getString(PREF_KEY_MUSIC_SORT_ORDER, null))
  )
    internal set

  var defaultGroupOption by mutableStateOf(
    MusicGroupOption.fromNameOrDefault(prefs?.getString(PREF_KEY_MUSIC_GROUP_OPTION, null))
  )
    internal set

  var savedSectionSortGroups by mutableStateOf<Map<String, MusicSortGroupConfig>>(
    MusicPlayerEngine.parseSavedSectionSortGroups(prefs?.getString(PREF_KEY_MUSIC_SAVED_SECTION_SORTS, null))
  )
    internal set

  val savedSectionSortOrders: Map<String, MusicSortOrder>
    get() = savedSectionSortGroups.mapValues { it.value.sortOrder }

  val currentSortScopeKey: String
    get() = MusicPlayerEngine.resolveSortScopeKey(selectedTab, selectedCustomFolderPath)

  val isSortSavedForCurrentScope: Boolean
    get() = currentSortScopeKey in savedSectionSortGroups

  val currentSortSaveLabel: String
    get() = MusicPlayerEngine.resolveSortSaveLabel(selectedTab, selectedCustomFolderPath)

  val availableGroupOptions: List<MusicGroupOption>
    get() = MusicPlayerEngine.availableGroupOptions(selectedTab, selectedCustomFolderPath)

  // Active Group Option for current section/folder
  var groupOption by mutableStateOf(
    MusicPlayerEngine.coerceGroupOptionForScope(
      groupOption = savedSectionSortGroups[
        MusicPlayerEngine.resolveSortScopeKey(selectedTab, selectedCustomFolderPath)
      ]?.groupOption ?: defaultGroupOption,
      selectedTab = selectedTab,
      selectedCustomFolderPath = selectedCustomFolderPath,
    )
  )

  // Active Sort Order for current section/folder
  var sortOrder by mutableStateOf(
    savedSectionSortGroups[MusicPlayerEngine.resolveSortScopeKey(selectedTab, selectedCustomFolderPath)]?.sortOrder
      ?: defaultSortOrder
  )

  var randomSortSeed by mutableLongStateOf(
    if (sortOrder == MusicSortOrder.RANDOM) {
      System.currentTimeMillis().takeIf { it != 0L } ?: 1L
    } else {
      0L
    }
  )
    internal set

  private fun syncSortOrderWithCurrentScope() {
    val savedConfig = savedSectionSortGroups[currentSortScopeKey]
    val targetOrder = savedConfig?.sortOrder ?: defaultSortOrder
    val targetGroup = MusicPlayerEngine.coerceGroupOptionForScope(
      groupOption = savedConfig?.groupOption ?: defaultGroupOption,
      selectedTab = selectedTab,
      selectedCustomFolderPath = selectedCustomFolderPath,
    )
    groupOption = targetGroup
    if (sortOrder != targetOrder) {
      if (targetOrder == MusicSortOrder.RANDOM && randomSortSeed == 0L) {
        randomSortSeed = System.currentTimeMillis().takeIf { it != 0L } ?: 1L
      }
      sortOrder = targetOrder
      applySortOrderToLibraryTracks(targetOrder, statsChanged = false)
    }
  }

  private fun applySortOrderToLibraryTracks(order: MusicSortOrder, statsChanged: Boolean) {
    if (!statsChanged && allTracks.size <= 100) {
      statsAndSortRecalcJob?.cancel()
      sortedLibraryTracks = when (order) {
        MusicSortOrder.MOST_PLAYED -> {
          if (mostPlayedPlaylist.tracks.size == allTracks.size) {
            mostPlayedPlaylist.tracks
          } else {
            MusicPlayerEngine.sortTracks(
              tracks = allTracks,
              sortOrder = order,
              playStats = viewPlayStats,
              favoriteIds = favoriteIds,
              favoriteArtists = favoriteArtists,
              favoriteAlbums = favoriteAlbums,
              favoriteGenres = favoriteGenres,
            )
          }
        }
        MusicSortOrder.RECENTLY_PLAYED -> {
          if (recentlyPlayedPlaylist.tracks.size == allTracks.size) {
            recentlyPlayedPlaylist.tracks
          } else {
            MusicPlayerEngine.sortTracksByRecentlyPlayed(allTracks, viewPlayStats)
          }
        }
        else -> MusicPlayerEngine.sortTracks(
          tracks = allTracks,
          sortOrder = order,
          playStats = viewPlayStats,
          favoriteIds = favoriteIds,
          favoriteArtists = favoriteArtists,
          favoriteAlbums = favoriteAlbums,
          favoriteGenres = favoriteGenres,
          randomSeed = randomSortSeed,
        )
      }
      return
    }
    if (statsChanged) {
      scheduleStatsAndSortRecalculation(statsChanged = true)
    } else {
      when (order) {
        MusicSortOrder.MOST_PLAYED -> {
          if (mostPlayedPlaylist.tracks.size == allTracks.size) {
            sortedLibraryTracks = mostPlayedPlaylist.tracks
          } else {
            scheduleStatsAndSortRecalculation(statsChanged = false)
          }
        }
        MusicSortOrder.RECENTLY_PLAYED -> {
          if (recentlyPlayedPlaylist.tracks.size == allTracks.size) {
            sortedLibraryTracks = recentlyPlayedPlaylist.tracks
          } else {
            scheduleStatsAndSortRecalculation(statsChanged = false)
          }
        }
        else -> scheduleStatsAndSortRecalculation(statsChanged = false)
      }
    }
  }

  fun updateGroupOption(option: MusicGroupOption) {
    val coerced = MusicPlayerEngine.coerceGroupOptionForScope(
      groupOption = option,
      selectedTab = selectedTab,
      selectedCustomFolderPath = selectedCustomFolderPath,
    )
    groupOption = coerced
    val scopeKey = currentSortScopeKey
    if (scopeKey in savedSectionSortGroups) {
      val nextSaved = savedSectionSortGroups.toMutableMap().apply {
        put(scopeKey, MusicSortGroupConfig(groupOption = coerced, sortOrder = sortOrder))
      }
      savedSectionSortGroups = nextSaved
      persistStringPrefInBackground(
        PREF_KEY_MUSIC_SAVED_SECTION_SORTS,
        MusicPlayerEngine.serializeSavedSectionSortGroups(nextSaved),
      )
    } else {
      defaultGroupOption = coerced
      persistStringPrefInBackground(PREF_KEY_MUSIC_GROUP_OPTION, coerced.name)
    }
  }

  fun updateSortOrder(order: MusicSortOrder) {
    val statsChanged = viewPlayStats !== playStats
    if (statsChanged) {
      viewPlayStats = playStats
    }
    if (order == MusicSortOrder.RANDOM) {
      val nextSeed = System.currentTimeMillis() + randomSortSeed + 1L
      randomSortSeed = if (nextSeed != 0L) nextSeed else 1L
    }
    val orderChanged = sortOrder != order || order == MusicSortOrder.RANDOM
    sortOrder = order
    val scopeKey = currentSortScopeKey
    if (scopeKey in savedSectionSortGroups) {
      val nextSaved = savedSectionSortGroups.toMutableMap().apply {
        put(scopeKey, MusicSortGroupConfig(groupOption = groupOption, sortOrder = order))
      }
      savedSectionSortGroups = nextSaved
      persistStringPrefInBackground(
        PREF_KEY_MUSIC_SAVED_SECTION_SORTS,
        MusicPlayerEngine.serializeSavedSectionSortGroups(nextSaved),
      )
    } else {
      defaultSortOrder = order
      persistStringPrefInBackground(PREF_KEY_MUSIC_SORT_ORDER, order.name)
    }
    if (statsChanged || orderChanged) {
      applySortOrderToLibraryTracks(order, statsChanged = statsChanged)
    }
  }

  fun setSaveSortForCurrentScope(saved: Boolean) {
    val scopeKey = currentSortScopeKey
    if (saved) {
      val nextSaved = savedSectionSortGroups.toMutableMap().apply {
        put(scopeKey, MusicSortGroupConfig(groupOption = groupOption, sortOrder = sortOrder))
      }
      savedSectionSortGroups = nextSaved
      persistStringPrefInBackground(
        PREF_KEY_MUSIC_SAVED_SECTION_SORTS,
        MusicPlayerEngine.serializeSavedSectionSortGroups(nextSaved),
      )
    } else {
      if (scopeKey in savedSectionSortGroups) {
        val nextSaved = savedSectionSortGroups.toMutableMap().apply {
          remove(scopeKey)
        }
        savedSectionSortGroups = nextSaved
        persistStringPrefInBackground(
          PREF_KEY_MUSIC_SAVED_SECTION_SORTS,
          MusicPlayerEngine.serializeSavedSectionSortGroups(nextSaved),
        )
      }
      groupOption = MusicPlayerEngine.coerceGroupOptionForScope(
        groupOption = defaultGroupOption,
        selectedTab = selectedTab,
        selectedCustomFolderPath = selectedCustomFolderPath,
      )
      if (sortOrder != defaultSortOrder) {
        sortOrder = defaultSortOrder
        applySortOrderToLibraryTracks(defaultSortOrder, statsChanged = false)
      }
    }
  }

  fun toggleSaveSortForCurrentScope(): Boolean {
    val nextSaved = !isSortSavedForCurrentScope
    setSaveSortForCurrentScope(nextSaved)
    return nextSaved
  }

  // Shuffle & Repeat
  var shuffleMode by mutableStateOf(
    run {
      val savedMode = prefs?.getString(PREF_KEY_MUSIC_SHUFFLE_MODE, null)
      if (!savedMode.isNullOrBlank()) {
        MusicShuffleMode.fromNameOrDefault(savedMode)
      } else if (prefs?.getBoolean(PREF_KEY_MUSIC_SHUFFLE, false) == true) {
        MusicShuffleMode.SHUFFLE
      } else {
        MusicShuffleMode.OFF
      }
    }
  )

  val isShuffleEnabled: Boolean
    get() = shuffleMode.isEnabled

  fun setShuffleMode(
    mode: MusicShuffleMode,
    random: kotlin.random.Random = kotlin.random.Random.Default,
  ) {
    shuffleMode = mode
    if (!isReleased) {
      prefs?.edit()
        ?.putString(PREF_KEY_MUSIC_SHUFFLE_MODE, mode.name)
        ?.putBoolean(PREF_KEY_MUSIC_SHUFFLE, mode.isEnabled)
        ?.apply()
    }
    if (playbackQueue.isNotEmpty()) {
      val anchorId = currentTrack?.id
      val base = if (unshuffledQueue.isNotEmpty()) unshuffledQueue else playbackQueue
      val (reordered, newIdx) = MusicPlayerEngine.applyShuffleModeToQueue(
        baseQueue = base,
        currentQueue = playbackQueue,
        anchorTrackId = anchorId,
        mode = mode,
        playStats = playStats,
        random = random,
      )
      playbackQueue = reordered
      currentQueueIndex = newIdx
    }
    syncSystemMediaControls(forceMetadataUpdate = false)
  }

  fun cycleShuffleMode(
    random: kotlin.random.Random = kotlin.random.Random.Default,
  ) {
    setShuffleMode(shuffleMode.next(), random = random)
  }

  fun toggleShuffle() {
    cycleShuffleMode()
  }

  var repeatMode by mutableStateOf(
    MusicRepeatMode.fromNameOrDefault(prefs?.getString(PREF_KEY_MUSIC_REPEAT, null))
  )

  fun cycleRepeatMode() {
    repeatMode = repeatMode.next()
    persistStringPrefInBackground(PREF_KEY_MUSIC_REPEAT, repeatMode.name)
    syncSystemMediaControls(forceMetadataUpdate = false)
  }

  // Favorites (Songs, Artists, Albums, and Genres — only song favoriteIds populate the default Favorites playlist)
  var favoriteIds by mutableStateOf(
    prefs?.getString(PREF_KEY_MUSIC_FAVORITES, "")
      .orEmpty()
      .split("|")
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .toSet()
  )

  fun toggleFavorite(trackId: String) {
    val next = if (trackId in favoriteIds) favoriteIds - trackId else favoriteIds + trackId
    favoriteIds = next
    persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITES, next, separator = "|")
    if (currentTrack?.id == trackId) {
      syncSystemMediaControls(forceMetadataUpdate = false)
    } else if (!isInspection && appContext != null) {
      MusicPlayerWidgets.updateAllWidgets(appContext)
    }
    val trackSkipCount = max(
      viewPlayStats[trackId]?.skipCount ?: 0,
      playStats[trackId]?.skipCount ?: 0,
    )
    scheduleFavoritesAndStatsRecalculation(
      favoritesChanged = true,
      mostPlayedAffected = trackSkipCount > 0,
    )
  }

  var favoriteArtists by mutableStateOf(
    MusicPlayerEngine.deserializeFavoriteSet(prefs?.getString(PREF_KEY_MUSIC_FAVORITE_ARTISTS, ""))
  )

  fun isArtistFavorite(artistName: String): Boolean =
    MusicPlayerEngine.isArtistFavorite(artistName, favoriteArtists)

  fun isArtistFavorite(artist: MusicArtist): Boolean =
    MusicPlayerEngine.isArtistFavorite(artist, favoriteArtists)

  fun toggleFavoriteArtist(artistName: String) {
    val next = MusicPlayerEngine.toggleFavoriteArtist(favoriteArtists, artistName)
    favoriteArtists = next
    persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_ARTISTS, next, separator = "\n")
    scheduleFavoritesAndStatsRecalculation(
      favoritesChanged = false,
      mostPlayedAffected = hasAnySkippedTracksInStats(),
    )
  }

  fun toggleFavoriteArtist(artist: MusicArtist) {
    toggleFavoriteArtist(artist.name)
  }

  var favoriteAlbums by mutableStateOf(
    MusicPlayerEngine.deserializeFavoriteSet(prefs?.getString(PREF_KEY_MUSIC_FAVORITE_ALBUMS, ""))
  )

  fun isAlbumFavorite(albumName: String, artistName: String = ""): Boolean =
    MusicPlayerEngine.isAlbumFavorite(albumName, artistName, favoriteAlbums)

  fun isAlbumFavorite(album: MusicAlbum): Boolean =
    MusicPlayerEngine.isAlbumFavorite(album, favoriteAlbums)

  fun toggleFavoriteAlbum(albumName: String, artistName: String = "") {
    val next = MusicPlayerEngine.toggleFavoriteAlbum(favoriteAlbums, albumName, artistName)
    favoriteAlbums = next
    persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_ALBUMS, next, separator = "\n")
    scheduleFavoritesAndStatsRecalculation(
      favoritesChanged = false,
      mostPlayedAffected = hasAnySkippedTracksInStats(),
    )
  }

  fun toggleFavoriteAlbum(album: MusicAlbum) {
    toggleFavoriteAlbum(album.name, album.artist)
  }

  var favoriteGenres by mutableStateOf(
    MusicPlayerEngine.deserializeFavoriteSet(prefs?.getString(PREF_KEY_MUSIC_FAVORITE_GENRES, ""))
  )

  fun isGenreFavorite(genreName: String): Boolean =
    MusicPlayerEngine.isGenreFavorite(genreName, favoriteGenres)

  fun isGenreFavorite(genre: MusicGenre): Boolean =
    MusicPlayerEngine.isGenreFavorite(genre, favoriteGenres)

  fun toggleFavoriteGenre(genreName: String) {
    val next = MusicPlayerEngine.toggleFavoriteGenre(favoriteGenres, genreName)
    favoriteGenres = next
    persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_GENRES, next, separator = "\n")
    scheduleFavoritesAndStatsRecalculation(
      favoritesChanged = false,
      mostPlayedAffected = hasAnySkippedTracksInStats(),
    )
  }

  fun toggleFavoriteGenre(genre: MusicGenre) {
    toggleFavoriteGenre(genre.name)
  }

  var favoriteFilterTabs by mutableStateOf<Set<MusicLibraryTab>>(emptySet())

  fun isFavoritesOnlyEnabled(tab: MusicLibraryTab): Boolean = tab in favoriteFilterTabs

  fun setFavoritesOnly(tab: MusicLibraryTab, enabled: Boolean) {
    favoriteFilterTabs = if (enabled) {
      favoriteFilterTabs + tab
    } else {
      favoriteFilterTabs - tab
    }
  }

  fun toggleFavoritesOnly(tab: MusicLibraryTab = selectedTab) {
    setFavoritesOnly(tab, !isFavoritesOnlyEnabled(tab))
  }

  var showFavoriteArtistsOnly: Boolean
    get() = isFavoritesOnlyEnabled(MusicLibraryTab.ARTISTS)
    set(value) = setFavoritesOnly(MusicLibraryTab.ARTISTS, value)

  var showFavoriteAlbumsOnly: Boolean
    get() = isFavoritesOnlyEnabled(MusicLibraryTab.ALBUMS)
    set(value) = setFavoritesOnly(MusicLibraryTab.ALBUMS, value)

  var showFavoriteSongsOnly: Boolean
    get() = isFavoritesOnlyEnabled(MusicLibraryTab.SONGS)
    set(value) = setFavoritesOnly(MusicLibraryTab.SONGS, value)

  var showFavoriteGenresOnly: Boolean
    get() = isFavoritesOnlyEnabled(MusicLibraryTab.GENRES)
    set(value) = setFavoritesOnly(MusicLibraryTab.GENRES, value)

  // Preferred Music Apps displayed in the Home view's bottom floating pill bar
  var preferredMusicApps by mutableStateOf(
    MusicPlayerEngine.deserializePreferredMusicApps(prefs?.getString(PREF_KEY_MUSIC_PREFERRED_APPS, ""))
  )
    internal set

  fun addPreferredMusicApp(app: PreferredMusicAppLink) {
    if (preferredMusicApps.any { it.id == app.id || (app.packageName.isNotBlank() && it.packageName == app.packageName) }) {
      return
    }
    val updated = preferredMusicApps + app
    preferredMusicApps = updated
    persistStringPrefInBackground(
      PREF_KEY_MUSIC_PREFERRED_APPS,
      MusicPlayerEngine.serializePreferredMusicApps(updated),
    )
  }

  fun removePreferredMusicApp(appIdOrPackage: String) {
    val updated = preferredMusicApps.filterNot {
      it.id == appIdOrPackage || (it.packageName.isNotBlank() && it.packageName == appIdOrPackage)
    }
    if (updated.size != preferredMusicApps.size) {
      preferredMusicApps = updated
      persistStringPrefInBackground(
        PREF_KEY_MUSIC_PREFERRED_APPS,
        MusicPlayerEngine.serializePreferredMusicApps(updated),
      )
    }
  }

  fun togglePreferredMusicApp(app: PreferredMusicAppLink): Boolean {
    val exists = preferredMusicApps.any {
      it.id == app.id || (app.packageName.isNotBlank() && it.packageName == app.packageName)
    }
    return if (exists) {
      removePreferredMusicApp(app.id)
      false
    } else {
      addPreferredMusicApp(app)
      true
    }
  }

  var showFavoritesOnly: Boolean
    get() = isFavoritesOnlyEnabled(selectedTab)
    set(value) = setFavoritesOnly(selectedTab, value)

  // Play Statistics (persisted in SharedPreferences: play count + total playtime per track).
  // Kept as a plain property so 200ms playback progress ticks and background track transitions
  // do not cause live recompositions or list/playlist jumping while the user is viewing them.
  var playStats: Map<String, TrackPlayStats> =
    MusicPlayerEngine.deserializePlayStats(prefs?.getString(PREF_KEY_MUSIC_PLAY_STATS, ""))

  // Frozen UI snapshot of play statistics for views and playlists; only refreshed when navigating
  // between tabs/views or changing sort order (or when not actively playing).
  var viewPlayStats by mutableStateOf(playStats)
    internal set

  // Frozen UI snapshot of play statistics for Home playlists; only refreshed on initial load
  // and when the user explicitly pulls down to refresh (or taps Refresh).
  var homePlayStats by mutableStateOf(playStats)
    internal set

  private fun hasAnySkippedTracksInStats(): Boolean {
    return viewPlayStats.values.any { it.skipCount > 0 } ||
      playStats.values.any { it.skipCount > 0 }
  }

  fun syncViewPlayStatsSnapshot() {
    if (viewPlayStats !== playStats) {
      viewPlayStats = playStats
      scheduleStatsAndSortRecalculation(statsChanged = true)
    }
  }

  fun recordTrackPlayStats(
    trackId: String,
    incrementPlayCount: Boolean = false,
    addedPlayTimeMs: Long = 0L,
    incrementSkipCount: Boolean = false,
    persistImmediately: Boolean = incrementPlayCount || incrementSkipCount,
  ) {
    val next = MusicPlayerEngine.recordPlayStat(
      current = playStats,
      trackId = trackId,
      incrementPlayCount = incrementPlayCount,
      addedPlayTimeMs = addedPlayTimeMs,
      incrementSkipCount = incrementSkipCount,
    )
    if (next !== playStats) {
      playStats = next
      if (persistImmediately) {
        persistPlayStatsInBackground(next)
      }
    }
  }

  private fun persistPlayStatsInBackground(statsSnapshot: Map<String, TrackPlayStats>) {
    if (prefs == null || isReleased) return
    playStatsPersistJob?.cancel()
    prefs.edit().putString(PREF_KEY_MUSIC_PLAY_STATS, MusicPlayerEngine.serializePlayStats(statsSnapshot)).apply()
  }

  private fun persistFavoritesInBackground(
    prefKey: String,
    values: Set<String>,
    separator: String,
  ) {
    if (prefs == null || isReleased) return
    val serialized = if (separator == "\n") {
      MusicPlayerEngine.serializeFavoriteSet(values)
    } else {
      values.joinToString(separator)
    }
    prefs.edit().putString(prefKey, serialized).apply()
  }

  private fun persistStringPrefInBackground(prefKey: String, value: String) {
    if (prefs == null || isReleased) return
    prefs.edit().putString(prefKey, value).apply()
  }

  fun flushPlayStats() {
    playStatsPersistJob?.cancel()
    if (!isReleased && playStats.isNotEmpty()) {
      prefs?.edit()?.putString(PREF_KEY_MUSIC_PLAY_STATS, MusicPlayerEngine.serializePlayStats(playStats))?.apply()
    }
  }

  /**
   * Reloads persisted preferences (favorites, playlists, play statistics, custom folders,
   * preferred music apps, tab order, view mode, and sort configuration) from [prefs] after
   * a data restore in Settings.
   */
  fun reloadPersistedDataFromPrefs() {
    val p = prefs ?: return
    playStatsPersistJob?.cancel()
    prefsPersistJob?.cancel()
    tabOrder = MusicPlayerEngine.parseTabOrder(p.getString(PREF_KEY_MUSIC_TAB_ORDER, null))
    viewMode = MusicViewMode.fromNameOrDefault(p.getString(PREF_KEY_MUSIC_VIEW_MODE, null))
    cardsPerRow = p.getInt(PREF_KEY_MUSIC_CARDS_PER_ROW, DEFAULT_MUSIC_CARDS_PER_ROW)
      .coerceIn(MIN_MUSIC_CARDS_PER_ROW, MAX_MUSIC_CARDS_PER_ROW)
    defaultSortOrder = MusicSortOrder.fromNameOrDefault(p.getString(PREF_KEY_MUSIC_SORT_ORDER, null))
    defaultGroupOption = MusicGroupOption.fromNameOrDefault(p.getString(PREF_KEY_MUSIC_GROUP_OPTION, null))
    savedSectionSortGroups = MusicPlayerEngine.parseSavedSectionSortGroups(p.getString(PREF_KEY_MUSIC_SAVED_SECTION_SORTS, null))
    favoriteIds = p.getString(PREF_KEY_MUSIC_FAVORITES, "")
      .orEmpty()
      .split("|")
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .toSet()
    favoriteArtists = MusicPlayerEngine.deserializeFavoriteSet(p.getString(PREF_KEY_MUSIC_FAVORITE_ARTISTS, ""))
    favoriteAlbums = MusicPlayerEngine.deserializeFavoriteSet(p.getString(PREF_KEY_MUSIC_FAVORITE_ALBUMS, ""))
    favoriteGenres = MusicPlayerEngine.deserializeFavoriteSet(p.getString(PREF_KEY_MUSIC_FAVORITE_GENRES, ""))
    preferredMusicApps = MusicPlayerEngine.deserializePreferredMusicApps(p.getString(PREF_KEY_MUSIC_PREFERRED_APPS, ""))
    playStats = MusicPlayerEngine.deserializePlayStats(p.getString(PREF_KEY_MUSIC_PLAY_STATS, ""))
    viewPlayStats = playStats
    homePlayStats = playStats
    savedPlaylistEntries = MusicPlayerEngine.deserializePlaylists(p.getString(PREF_KEY_MUSIC_PLAYLISTS, ""))
      .filterNot { it.id in MusicPlayerEngine.LEGACY_SAMPLE_PLAYLIST_IDS || it.id in MusicPlayerEngine.BUILT_IN_PLAYLIST_IDS }
    customFolders = MusicPlayerEngine.parseCustomFolders(p.getString(PREF_KEY_MUSIC_CUSTOM_FOLDERS, null))
    syncSortOrderWithCurrentScope()
    refreshLibraryDerivedStateAfterMutation(allTracks)
  }

  //Playlists & Background-Derived Library Collections
  var savedPlaylistEntries by mutableStateOf(
    MusicPlayerEngine.deserializePlaylists(prefs?.getString(PREF_KEY_MUSIC_PLAYLISTS, ""))
      .filterNot { it.id in MusicPlayerEngine.LEGACY_SAMPLE_PLAYLIST_IDS || it.id in MusicPlayerEngine.BUILT_IN_PLAYLIST_IDS }
  )

  var libraryArtists by mutableStateOf<List<MusicArtist>>(emptyList())
    internal set
  var libraryAlbums by mutableStateOf<List<MusicAlbum>>(emptyList())
    internal set
  var libraryGenres by mutableStateOf<List<MusicGenre>>(emptyList())
    internal set
  var sortedLibraryTracks by mutableStateOf<List<MusicTrack>>(emptyList())
    internal set

  var favoritesPlaylist by mutableStateOf(
    MusicPlayerEngine.buildFavoritesPlaylist(emptySet(), emptyList())
  )
    internal set
  var mostPlayedPlaylist by mutableStateOf(
    MusicPlayerEngine.buildMostPlayedPlaylist(emptyMap(), emptyList())
  )
    internal set
  var recentlyPlayedPlaylist by mutableStateOf(
    MusicPlayerEngine.buildRecentlyPlayedPlaylist(emptyMap(), emptyList())
  )
    internal set
  var userPlaylists by mutableStateOf<List<MusicPlaylist>>(emptyList())
    internal set
  var basePlaylists by mutableStateOf<List<MusicPlaylist>>(
    listOf(favoritesPlaylist, mostPlayedPlaylist, recentlyPlayedPlaylist)
  )
    internal set

  // Generated Home Playlists (6 dynamic ~1 hr mixes refreshed on pull-to-refresh or background favorite updates)
  var homeRefreshGeneration by mutableIntStateOf(0)
    private set

  var homePlaylistsState by mutableStateOf<List<MusicPlaylist>>(
    MusicPlayerEngine.generateHomePlaylists(emptyList())
  )
    internal set

  val homePlaylists: List<MusicPlaylist>
    get() = homePlaylistsState

  /**
   * Synchronously refreshes the 6 Home playlists and returns the updated list.
   */
  fun refreshHomePlaylists(): List<MusicPlaylist> {
    homePlayStats = playStats
    viewPlayStats = playStats
    homeRefreshGeneration += 1
    val nextMostPlayed = MusicPlayerEngine.buildMostPlayedPlaylist(
      playStats = viewPlayStats,
      allTracks = allTracks,
      favoriteIds = favoriteIds,
      favoriteArtists = favoriteArtists,
      favoriteAlbums = favoriteAlbums,
      favoriteGenres = favoriteGenres,
    )
    val nextRecentlyPlayed = MusicPlayerEngine.buildRecentlyPlayedPlaylist(viewPlayStats, allTracks)
    val nextHome = MusicPlayerEngine.generateHomePlaylists(
      allTracks = allTracks,
      playStats = homePlayStats,
      favoriteIds = favoriteIds,
      favoriteArtists = favoriteArtists,
      favoriteAlbums = favoriteAlbums,
      favoriteGenres = favoriteGenres,
      refreshGeneration = homeRefreshGeneration,
      precomputedGenres = libraryGenres.takeIf { it.isNotEmpty() || allTracks.isEmpty() },
      precomputedSortedByStats = nextMostPlayed.tracks,
    )
    Snapshot.withMutableSnapshot {
      mostPlayedPlaylist = nextMostPlayed
      recentlyPlayedPlaylist = nextRecentlyPlayed
      basePlaylists = listOf(favoritesPlaylist, nextMostPlayed, nextRecentlyPlayed) + userPlaylists
      homePlaylistsState = nextHome
      if (sortOrder == MusicSortOrder.MOST_PLAYED) {
        sortedLibraryTracks = nextMostPlayed.tracks
      } else if (sortOrder == MusicSortOrder.RECENTLY_PLAYED) {
        sortedLibraryTracks = nextRecentlyPlayed.tracks
      }
    }
    return nextHome
  }

  /**
   * Refreshes the 6 Home playlists and view play stats on a background task (`Dispatchers.Default`)
   * so pull-to-refresh and Refresh button taps never block the UI thread.
   */
  fun refreshHomePlaylistsAsync() {
    homePlayStats = playStats
    viewPlayStats = playStats
    homeRefreshGeneration += 1
    val tracksSnapshot = allTracks
    val homeStatsSnapshot = homePlayStats
    val viewStatsSnapshot = viewPlayStats
    val favIdsSnapshot = favoriteIds
    val favArtistsSnapshot = favoriteArtists
    val favAlbumsSnapshot = favoriteAlbums
    val favGenresSnapshot = favoriteGenres
    val genSnapshot = homeRefreshGeneration
    val genresSnapshot = libraryGenres.takeIf { it.isNotEmpty() || tracksSnapshot.isEmpty() }
    val sortOrderSnapshot = sortOrder

    favoritesRecalcJob?.cancel()
    isRecalculatingInBackground = true
    favoritesRecalcJob = scope.launch(backgroundCoroutineContext) {
      try {
        val nextMostPlayed = MusicPlayerEngine.buildMostPlayedPlaylist(
          playStats = viewStatsSnapshot,
          allTracks = tracksSnapshot,
          favoriteIds = favIdsSnapshot,
          favoriteArtists = favArtistsSnapshot,
          favoriteAlbums = favAlbumsSnapshot,
          favoriteGenres = favGenresSnapshot,
        )
        ensureActive()
        val nextRecentlyPlayed = MusicPlayerEngine.buildRecentlyPlayedPlaylist(
          playStats = viewStatsSnapshot,
          allTracks = tracksSnapshot,
        )
        ensureActive()
        val nextHome = MusicPlayerEngine.generateHomePlaylists(
          allTracks = tracksSnapshot,
          playStats = homeStatsSnapshot,
          favoriteIds = favIdsSnapshot,
          favoriteArtists = favArtistsSnapshot,
          favoriteAlbums = favAlbumsSnapshot,
          favoriteGenres = favGenresSnapshot,
          refreshGeneration = genSnapshot,
          precomputedGenres = genresSnapshot,
          precomputedSortedByStats = nextMostPlayed.tracks,
        )
        ensureActive()
        if (!isReleased) {
          Snapshot.withMutableSnapshot {
            if (allTracks === tracksSnapshot) {
              mostPlayedPlaylist = nextMostPlayed
              recentlyPlayedPlaylist = nextRecentlyPlayed
              basePlaylists = listOf(favoritesPlaylist, nextMostPlayed, nextRecentlyPlayed) + userPlaylists
              if (homeRefreshGeneration == genSnapshot) {
                homePlaylistsState = nextHome
              }
              if (sortOrder == sortOrderSnapshot) {
                when (sortOrderSnapshot) {
                  MusicSortOrder.MOST_PLAYED -> sortedLibraryTracks = nextMostPlayed.tracks
                  MusicSortOrder.RECENTLY_PLAYED -> sortedLibraryTracks = nextRecentlyPlayed.tracks
                  else -> {}
                }
              }
            }
            backgroundRecalculationCount += 1
            isRecalculatingInBackground = false
          }
        }
      } finally {
        if (!isReleased) {
          isRecalculatingInBackground = false
        }
      }
    }
  }

  /**
   * Delegates Favorites playlist updates, Most Played play-stats recalculation, and Home mix
   * regeneration to a background task ([backgroundCoroutineContext]) so starring a song, artist,
   * album, or genre never blocks the foreground UI thread.
   */
  private fun scheduleFavoritesAndStatsRecalculation(
    favoritesChanged: Boolean,
    mostPlayedAffected: Boolean,
  ) {
    val tracksSnapshot = allTracks
    val favIdsSnapshot = favoriteIds
    val favArtistsSnapshot = favoriteArtists
    val favAlbumsSnapshot = favoriteAlbums
    val favGenresSnapshot = favoriteGenres
    val viewStatsSnapshot = viewPlayStats
    val homeStatsSnapshot = homePlayStats
    val homeGenSnapshot = homeRefreshGeneration
    val sortOrderSnapshot = sortOrder
    val genresSnapshot = libraryGenres.takeIf { it.isNotEmpty() || tracksSnapshot.isEmpty() }

    favoritesRecalcJob?.cancel()
    isRecalculatingInBackground = true
    favoritesRecalcJob = scope.launch(backgroundCoroutineContext) {
      try {
        val nextFavPlaylist = if (favoritesChanged) {
          MusicPlayerEngine.buildFavoritesPlaylist(favIdsSnapshot, tracksSnapshot)
        } else {
          null
        }
        ensureActive()
        if (nextFavPlaylist != null && !isReleased) {
          Snapshot.withMutableSnapshot {
            if (favoriteIds == favIdsSnapshot && allTracks === tracksSnapshot) {
              favoritesPlaylist = nextFavPlaylist
              basePlaylists = listOf(nextFavPlaylist, mostPlayedPlaylist, recentlyPlayedPlaylist) + userPlaylists
            }
          }
        }

        val nextMostPlayed = if (mostPlayedAffected) {
          MusicPlayerEngine.buildMostPlayedPlaylist(
            playStats = viewStatsSnapshot,
            allTracks = tracksSnapshot,
            favoriteIds = favIdsSnapshot,
            favoriteArtists = favArtistsSnapshot,
            favoriteAlbums = favAlbumsSnapshot,
            favoriteGenres = favGenresSnapshot,
          )
        } else {
          null
        }
        ensureActive()
        if (nextMostPlayed != null && !isReleased) {
          Snapshot.withMutableSnapshot {
            if (allTracks === tracksSnapshot) {
              mostPlayedPlaylist = nextMostPlayed
              basePlaylists = listOf(favoritesPlaylist, nextMostPlayed, recentlyPlayedPlaylist) + userPlaylists
              if (sortOrder == sortOrderSnapshot && sortOrderSnapshot == MusicSortOrder.MOST_PLAYED) {
                sortedLibraryTracks = nextMostPlayed.tracks
              }
            }
          }
        }

        val effectiveSortedByStats = if (homeStatsSnapshot === viewStatsSnapshot) {
          (nextMostPlayed ?: mostPlayedPlaylist).tracks.takeIf { it.size == tracksSnapshot.size }
        } else {
          null
        }
        val nextHomePlaylists = MusicPlayerEngine.generateHomePlaylists(
          allTracks = tracksSnapshot,
          playStats = homeStatsSnapshot,
          favoriteIds = favIdsSnapshot,
          favoriteArtists = favArtistsSnapshot,
          favoriteAlbums = favAlbumsSnapshot,
          favoriteGenres = favGenresSnapshot,
          refreshGeneration = homeGenSnapshot,
          precomputedGenres = genresSnapshot,
          precomputedSortedByStats = effectiveSortedByStats,
        )
        ensureActive()
        if (!isReleased) {
          Snapshot.withMutableSnapshot {
            if (homeRefreshGeneration == homeGenSnapshot && allTracks === tracksSnapshot) {
              homePlaylistsState = nextHomePlaylists
            }
            backgroundRecalculationCount += 1
            isRecalculatingInBackground = false
          }
        }
      } finally {
        if (!isReleased) {
          isRecalculatingInBackground = false
        }
      }
    }
  }

  /**
   * Delegates play-statistics playlist rebuilding (`Most Played`, `Recently Played`) and track
   * sorting (`sortedLibraryTracks`) to a background task ([backgroundCoroutineContext]).
   */
  private fun scheduleStatsAndSortRecalculation(statsChanged: Boolean) {
    val tracksSnapshot = allTracks
    val viewStatsSnapshot = viewPlayStats
    val sortOrderSnapshot = sortOrder
    val randomSeedSnapshot = randomSortSeed
    val favIdsSnapshot = favoriteIds
    val favArtistsSnapshot = favoriteArtists
    val favAlbumsSnapshot = favoriteAlbums
    val favGenresSnapshot = favoriteGenres

    statsAndSortRecalcJob?.cancel()
    isRecalculatingInBackground = true
    statsAndSortRecalcJob = scope.launch(backgroundCoroutineContext) {
      try {
        val nextMostPlayed = if (statsChanged) {
          MusicPlayerEngine.buildMostPlayedPlaylist(
            playStats = viewStatsSnapshot,
            allTracks = tracksSnapshot,
            favoriteIds = favIdsSnapshot,
            favoriteArtists = favArtistsSnapshot,
            favoriteAlbums = favAlbumsSnapshot,
            favoriteGenres = favGenresSnapshot,
          )
        } else {
          null
        }
        ensureActive()
        val nextRecentlyPlayed = if (statsChanged) {
          MusicPlayerEngine.buildRecentlyPlayedPlaylist(
            playStats = viewStatsSnapshot,
            allTracks = tracksSnapshot,
          )
        } else {
          null
        }
        ensureActive()
        val effectiveMostPlayed = nextMostPlayed ?: mostPlayedPlaylist
        val effectiveRecentlyPlayed = nextRecentlyPlayed ?: recentlyPlayedPlaylist
        val nextSorted = when (sortOrderSnapshot) {
          MusicSortOrder.MOST_PLAYED -> {
            if (effectiveMostPlayed.tracks.size == tracksSnapshot.size) {
              effectiveMostPlayed.tracks
            } else {
              MusicPlayerEngine.sortTracks(
                tracks = tracksSnapshot,
                sortOrder = sortOrderSnapshot,
                playStats = viewStatsSnapshot,
                favoriteIds = favIdsSnapshot,
                favoriteArtists = favArtistsSnapshot,
                favoriteAlbums = favAlbumsSnapshot,
                favoriteGenres = favGenresSnapshot,
              )
            }
          }
          MusicSortOrder.RECENTLY_PLAYED -> {
            if (effectiveRecentlyPlayed.tracks.size == tracksSnapshot.size) {
              effectiveRecentlyPlayed.tracks
            } else {
              MusicPlayerEngine.sortTracksByRecentlyPlayed(tracksSnapshot, viewStatsSnapshot)
            }
          }
          else -> MusicPlayerEngine.sortTracks(
            tracks = tracksSnapshot,
            sortOrder = sortOrderSnapshot,
            playStats = viewStatsSnapshot,
            favoriteIds = favIdsSnapshot,
            favoriteArtists = favArtistsSnapshot,
            favoriteAlbums = favAlbumsSnapshot,
            favoriteGenres = favGenresSnapshot,
            randomSeed = randomSeedSnapshot,
          )
        }
        ensureActive()
        if (!isReleased) {
          Snapshot.withMutableSnapshot {
            if (allTracks === tracksSnapshot) {
              if (nextMostPlayed != null) mostPlayedPlaylist = nextMostPlayed
              if (nextRecentlyPlayed != null) recentlyPlayedPlaylist = nextRecentlyPlayed
              if (nextMostPlayed != null || nextRecentlyPlayed != null) {
                basePlaylists = listOf(favoritesPlaylist, effectiveMostPlayed, effectiveRecentlyPlayed) + userPlaylists
              }
              if (sortOrder == sortOrderSnapshot) {
                sortedLibraryTracks = nextSorted
              }
            }
            backgroundRecalculationCount += 1
            isRecalculatingInBackground = false
          }
        }
      } finally {
        if (!isReleased) {
          isRecalculatingInBackground = false
        }
      }
    }
  }

  private data class ComputedLibraryState(
    val tracks: List<MusicTrack>,
    val artists: List<MusicArtist>,
    val albums: List<MusicAlbum>,
    val genres: List<MusicGenre>,
    val favorites: MusicPlaylist,
    val mostPlayed: MusicPlaylist,
    val recentlyPlayed: MusicPlaylist,
    val savedPlaylists: List<MusicPlaylist>,
    val sortedTracks: List<MusicTrack>,
    val homeMixes: List<MusicPlaylist>,
  )

  private fun computeFullLibraryState(
    tracks: List<MusicTrack>,
    viewStats: Map<String, TrackPlayStats> = viewPlayStats,
    homeStats: Map<String, TrackPlayStats> = homePlayStats,
    favIds: Set<String> = favoriteIds,
    favArtists: Set<String> = favoriteArtists,
    favAlbums: Set<String> = favoriteAlbums,
    favGenres: Set<String> = favoriteGenres,
    savedEntries: List<SavedPlaylistEntry> = savedPlaylistEntries,
    order: MusicSortOrder = sortOrder,
    homeGen: Int = homeRefreshGeneration,
    randomSeed: Long = randomSortSeed,
  ): ComputedLibraryState {
    val artists = MusicPlayerEngine.buildArtists(tracks)
    val albums = MusicPlayerEngine.buildAlbums(tracks)
    val genres = MusicPlayerEngine.buildGenres(tracks)
    val favPlaylist = MusicPlayerEngine.buildFavoritesPlaylist(favIds, tracks)
    val mostPlayed = MusicPlayerEngine.buildMostPlayedPlaylist(
      playStats = viewStats,
      allTracks = tracks,
      favoriteIds = favIds,
      favoriteArtists = favArtists,
      favoriteAlbums = favAlbums,
      favoriteGenres = favGenres,
    )
    val recentlyPlayed = MusicPlayerEngine.buildRecentlyPlayedPlaylist(viewStats, tracks)
    val resolvedUserPlaylists = MusicPlayerEngine.buildPlaylists(savedEntries, tracks)
    val sorted = when (order) {
      MusicSortOrder.MOST_PLAYED -> mostPlayed.tracks
      MusicSortOrder.RECENTLY_PLAYED -> recentlyPlayed.tracks
      else -> MusicPlayerEngine.sortTracks(
        tracks = tracks,
        sortOrder = order,
        playStats = viewStats,
        favoriteIds = favIds,
        favoriteArtists = favArtists,
        favoriteAlbums = favAlbums,
        favoriteGenres = favGenres,
        randomSeed = randomSeed,
      )
    }
    val homeMixes = MusicPlayerEngine.generateHomePlaylists(
      allTracks = tracks,
      playStats = homeStats,
      favoriteIds = favIds,
      favoriteArtists = favArtists,
      favoriteAlbums = favAlbums,
      favoriteGenres = favGenres,
      refreshGeneration = homeGen,
      precomputedGenres = genres,
      precomputedSortedByStats = if (homeStats === viewStats) mostPlayed.tracks else null,
    )
    return ComputedLibraryState(
      tracks = tracks,
      artists = artists,
      albums = albums,
      genres = genres,
      favorites = favPlaylist,
      mostPlayed = mostPlayed,
      recentlyPlayed = recentlyPlayed,
      savedPlaylists = resolvedUserPlaylists,
      sortedTracks = sorted,
      homeMixes = homeMixes,
    )
  }

  private fun applyComputedLibraryState(computed: ComputedLibraryState) {
    Snapshot.withMutableSnapshot {
      libraryArtists = computed.artists
      libraryAlbums = computed.albums
      libraryGenres = computed.genres
      favoritesPlaylist = computed.favorites
      mostPlayedPlaylist = computed.mostPlayed
      recentlyPlayedPlaylist = computed.recentlyPlayed
      userPlaylists = computed.savedPlaylists
      basePlaylists = listOf(computed.favorites, computed.mostPlayed, computed.recentlyPlayed) + computed.savedPlaylists
      sortedLibraryTracks = computed.sortedTracks
      homePlaylistsState = computed.homeMixes
    }
  }

  private fun refreshLibraryDerivedStateAfterMutation(tracks: List<MusicTrack>) {
    if (tracks.size <= 100) {
      applyComputedLibraryState(computeFullLibraryState(tracks))
    } else {
      val tracksSnapshot = tracks
      val viewStatsSnapshot = viewPlayStats
      val homeStatsSnapshot = homePlayStats
      val favIdsSnapshot = favoriteIds
      val favArtistsSnapshot = favoriteArtists
      val favAlbumsSnapshot = favoriteAlbums
      val favGenresSnapshot = favoriteGenres
      val savedSnapshot = savedPlaylistEntries
      val orderSnapshot = sortOrder
      val homeGenSnapshot = homeRefreshGeneration
      val randomSeedSnapshot = randomSortSeed
      libraryRebuildJob?.cancel()
      isRecalculatingInBackground = true
      libraryRebuildJob = scope.launch(backgroundCoroutineContext) {
        try {
          val computed = computeFullLibraryState(
            tracks = tracksSnapshot,
            viewStats = viewStatsSnapshot,
            homeStats = homeStatsSnapshot,
            favIds = favIdsSnapshot,
            favArtists = favArtistsSnapshot,
            favAlbums = favAlbumsSnapshot,
            favGenres = favGenresSnapshot,
            savedEntries = savedSnapshot,
            order = orderSnapshot,
            homeGen = homeGenSnapshot,
            randomSeed = randomSeedSnapshot,
          )
          ensureActive()
          if (!isReleased && allTracks === tracksSnapshot) {
            applyComputedLibraryState(computed)
            backgroundRecalculationCount += 1
          }
        } finally {
          if (!isReleased) {
            isRecalculatingInBackground = false
          }
        }
      }
    }
  }

  /**
   * Suspends until all pending background favorite, play-stats, playlist, and persistence tasks complete.
   */
  suspend fun awaitBackgroundRecalculations() {
    favoritesRecalcJob?.join()
    statsAndSortRecalcJob?.join()
    libraryRebuildJob?.join()
    prefsPersistJob?.join()
    playStatsPersistJob?.join()
    mediaControlSyncJob?.join()
  }

  fun updateSavedPlaylists(next: List<SavedPlaylistEntry>) {
    savedPlaylistEntries = next
    if (!isReleased) {
      prefs?.edit()?.putString(PREF_KEY_MUSIC_PLAYLISTS, MusicPlayerEngine.serializePlaylists(next))?.apply()
    }
    val tracksSnapshot = allTracks
    if (tracksSnapshot.size <= 100) {
      val resolved = MusicPlayerEngine.buildPlaylists(next, tracksSnapshot)
      Snapshot.withMutableSnapshot {
        userPlaylists = resolved
        basePlaylists = listOf(favoritesPlaylist, mostPlayedPlaylist, recentlyPlayedPlaylist) + resolved
      }
    } else {
      scope.launch(backgroundCoroutineContext) {
        val resolved = MusicPlayerEngine.buildPlaylists(next, tracksSnapshot)
        if (!isReleased && savedPlaylistEntries === next && allTracks === tracksSnapshot) {
          Snapshot.withMutableSnapshot {
            userPlaylists = resolved
            basePlaylists = listOf(favoritesPlaylist, mostPlayedPlaylist, recentlyPlayedPlaylist) + resolved
          }
        }
      }
    }
  }

  fun saveQueueAsPlaylist(name: String, tracksToSave: List<MusicTrack>) {
    val trimmedName = name.trim().ifEmpty { "Playlist ${savedPlaylistEntries.size + 1}" }
    val trackIds = tracksToSave.map { it.id }
    if (trackIds.isEmpty()) return
    val newEntry = SavedPlaylistEntry(
      id = "playlist_${System.currentTimeMillis()}_${savedPlaylistEntries.size + 1}",
      name = trimmedName,
      trackIds = trackIds,
      createdAtMs = System.currentTimeMillis(),
    )
    updateSavedPlaylists(listOf(newEntry) + savedPlaylistEntries)
  }

  fun deletePlaylist(playlistId: String): Boolean {
    if (MusicPlayerEngine.isProtectedPlaylistId(playlistId)) return false
    val updated = savedPlaylistEntries.filterNot { it.id == playlistId }
    if (updated.size == savedPlaylistEntries.size) return false
    updateSavedPlaylists(updated)
    return true
  }

  fun renamePlaylist(playlistId: String, newName: String): Boolean {
    if (MusicPlayerEngine.isProtectedPlaylistId(playlistId)) return false
    val trimmed = newName.trim()
    if (trimmed.isEmpty()) return false
    var changed = false
    val updated = savedPlaylistEntries.map { entry ->
      if (entry.id == playlistId) {
        changed = true
        entry.copy(name = trimmed)
      } else {
        entry
      }
    }
    if (changed) {
      updateSavedPlaylists(updated)
    }
    return changed
  }

  fun addTracksToPlaylist(playlistId: String, tracks: List<MusicTrack>): Boolean {
    if (playlistId in MusicPlayerEngine.BUILT_IN_PLAYLIST_IDS || tracks.isEmpty()) return false
    val idsToAdd = tracks.map { it.id }
    var changed = false
    val updated = savedPlaylistEntries.map { entry ->
      if (entry.id == playlistId) {
        changed = true
        entry.copy(trackIds = (entry.trackIds + idsToAdd).distinct())
      } else {
        entry
      }
    }
    if (changed) {
      updateSavedPlaylists(updated)
    }
    return changed
  }

  fun removeTrackFromPlaylist(playlistId: String, trackId: String): Boolean {
    if (playlistId in MusicPlayerEngine.BUILT_IN_PLAYLIST_IDS) return false
    var changed = false
    val updated = savedPlaylistEntries.map { entry ->
      if (entry.id == playlistId && trackId in entry.trackIds) {
        changed = true
        entry.copy(trackIds = entry.trackIds.filterNot { it == trackId })
      } else {
        entry
      }
    }
    if (changed) {
      updateSavedPlaylists(updated)
    }
    return changed
  }

  // Search state
  var isSearchActive by mutableStateOf(false)
  var searchQuery by mutableStateOf("")

  // Drill-down selection state
  var selectedArtistName by mutableStateOf<String?>(null)
  var selectedAlbumKey by mutableStateOf<String?>(null)
  var selectedGenreName by mutableStateOf<String?>(null)
  var selectedPlaylistId by mutableStateOf<String?>(null)

  fun clearDrillDown() {
    syncViewPlayStatsSnapshot()
    selectedArtistName = null
    selectedAlbumKey = null
    selectedGenreName = null
    selectedPlaylistId = null
  }

  fun navigateToArtist(artistName: String) {
    if (artistName.isBlank()) return
    clearDrillDown()
    selectedTab = MusicLibraryTab.ARTISTS
    selectedArtistName = artistName
  }

  fun navigateToAlbum(albumKey: String) {
    if (albumKey.isBlank()) return
    clearDrillDown()
    selectedTab = MusicLibraryTab.ALBUMS
    selectedAlbumKey = albumKey
  }

  // Library Tracks & Playback Queue State
  var allTracks by mutableStateOf<List<MusicTrack>>(emptyList())
  var unshuffledQueue by mutableStateOf<List<MusicTrack>>(emptyList())
    internal set
  var playbackQueue by mutableStateOf<List<MusicTrack>>(emptyList())
  var currentQueueIndex by mutableIntStateOf(0)

  val currentTrack: MusicTrack?
    get() = playbackQueue.getOrNull(
      currentQueueIndex.coerceIn(0, maxOf(0, playbackQueue.size - 1))
    )

  var activePalette by mutableStateOf(MusicPlayerEngine.resolveTrackPalette(null))
    internal set
  private var lastSyncedPaletteTrackId: String? = null

  var isPlaying by mutableStateOf(false)
  var isPlayerExpanded by mutableStateOf(initialPlayerExpanded)
  var initialQueueExpandedState by mutableStateOf(initialQueueExpanded)
  var currentPositionMs by mutableLongStateOf(0L)
  var trackDurationMs by mutableLongStateOf(15_000L)
  var isDraggingSeek by mutableStateOf(false)
  var dragSeekFraction by mutableFloatStateOf(0f)

  // Speed & Sleep Timer & Sheet state
  var playbackSpeed by mutableFloatStateOf(1.0f)
  var sleepTimerPreset by mutableStateOf(SleepTimerPreset.OFF)
  var sleepRemainingMs by mutableLongStateOf(0L)
  var showQueueSheet by mutableStateOf(false)

  private var lastCountedPlayTrackId: String? = null

  /**
   * Returns `true` if a hardware [MediaPlayer] instance is currently playing audio.
   */
  val isHardwarePlayerActive: Boolean
    get() = try {
      mediaPlayer?.isPlaying == true
    } catch (_: Throwable) {
      false
    }

  var isLoadingLibrary by mutableStateOf(false)
    internal set

  var hasFullFilesPermission by mutableStateOf(
    !isInspection && appContext != null && MusicPlayerEngine.hasFullFilesPermission(appContext)
  )
    internal set

  fun syncFullFilesPermissionState() {
    if (isInspection || appContext == null) return
    val current = MusicPlayerEngine.hasFullFilesPermission(appContext)
    if (current != hasFullFilesPermission) {
      hasFullFilesPermission = current
      if (isMusicFolderGranted) {
        reloadLibraryTracksForGrantedFolders()
      }
    }
  }

  private fun resolveAdditionalCustomFolderDirectories(): List<File> =
    customFolders.map { MusicPlayerEngine.resolveDirectoryForFolder(it) }

  init {
    if (!isInspection && cacheDir != null) {
      val indexFile = appContext?.let { MusicPlayerEngine.getDefaultIndexCacheFile(it) }
      MusicPlayerEngine.configureDiskCache(cacheDir, indexFile)
    }
    val includeSamples = prefs?.getBoolean(PREF_KEY_MUSIC_INCLUDE_SAMPLES, false) ?: false
    val additionalDirs = resolveAdditionalCustomFolderDirectories()
    val initialLoaded = when {
      initialTracks != null -> initialTracks
      isInspection -> MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
      !isMusicFolderGranted -> emptyList()
      appContext != null -> MusicPlayerEngine.getCachedLibraryTracks(
        initialDirectory = initialDirectory,
        includeSamples = includeSamples,
        additionalDirectories = additionalDirs,
        hasFullFilesPermission = hasFullFilesPermission,
      ) ?: emptyList()
      else -> MusicPlayerEngine.getCachedLibraryTracks(
        initialDirectory = initialDirectory,
        includeSamples = includeSamples,
        additionalDirectories = additionalDirs,
        hasFullFilesPermission = hasFullFilesPermission,
      ) ?: MusicPlayerEngine.loadLibraryTracks(
        initialDirectory = initialDirectory,
        includeSamples = includeSamples,
        inspectMetadataRetriever = false,
        deferAlbumArt = true,
        additionalDirectories = additionalDirs,
        hasFullFilesPermission = hasFullFilesPermission,
      )
    }
    allTracks = initialLoaded
    refreshLibraryDerivedStateAfterMutation(initialLoaded)
    if ((initialPlayerExpanded || initialQueueExpanded) && initialLoaded.isNotEmpty() && MusicPlayerEngine.externalSystemMediaInfo == null) {
      unshuffledQueue = initialLoaded
      playbackQueue = initialLoaded
      currentQueueIndex = 0
    } else {
      unshuffledQueue = emptyList()
      playbackQueue = emptyList()
      currentQueueIndex = 0
    }
    trackDurationMs = currentTrack?.durationMs?.coerceAtLeast(1000L) ?: 15_000L
    MusicPlayerEngine.activeSessionState = this
    initSystemAudioAndMediaControls()
    prepareAndSyncMediaPlayer(forceRestart = false)
    if (isMusicFolderGranted) {
      startAsyncLibraryLoadAndArtworkPrefetch()
    }
  }

  private fun reloadLibraryTracksForGrantedFolders() {
    if (!isMusicFolderGranted) return
    val includeSamples = prefs?.getBoolean(PREF_KEY_MUSIC_INCLUDE_SAMPLES, false) ?: false
    val additionalDirs = resolveAdditionalCustomFolderDirectories()
    val explicitTracks = configuredInitialTracks
    if (explicitTracks != null) {
      val extraTracks = if (additionalDirs.isNotEmpty()) {
        MusicPlayerEngine.scanMusicDirectories(
          directories = additionalDirs,
          inspectMetadataRetriever = false,
          deferAlbumArt = true,
        )
      } else {
        emptyList()
      }
      val combined = (explicitTracks + extraTracks).distinctBy { it.id }
      applyLoadedLibraryTracks(combined)
      return
    }
    if (appContext == null) {
      val loaded = MusicPlayerEngine.loadLibraryTracks(
        initialDirectory = configuredInitialDirectory,
        includeSamples = includeSamples,
        inspectMetadataRetriever = false,
        deferAlbumArt = true,
        additionalDirectories = additionalDirs,
        hasFullFilesPermission = hasFullFilesPermission,
      )
      applyLoadedLibraryTracks(loaded)
    } else {
      startAsyncLibraryLoadAndArtworkPrefetch()
    }
  }

  /**
   * Opens the full expanded music player sheet, ensuring a track is queued if the library has tracks.
   */
  fun expandFullPlayer() {
    showQueueSheet = false
    isPlayerExpanded = true
    if (currentTrack == null && allTracks.isNotEmpty() && MusicPlayerEngine.externalSystemMediaInfo == null) {
      unshuffledQueue = allTracks
      playbackQueue = allTracks
      currentQueueIndex = 0
      trackDurationMs = currentTrack?.durationMs?.coerceAtLeast(1000L) ?: 15_000L
      prepareAndSyncMediaPlayer(forceRestart = false)
    }
  }

  internal fun syncConfiguration(
    initialDirectory: File?,
    initialTracks: List<MusicTrack>?,
    initialTab: MusicLibraryTab?,
    initialViewMode: MusicViewMode?,
    initialPlayerExpanded: Boolean,
    initialQueueExpanded: Boolean,
  ) {
    if (initialTab != null && initialTab != configuredInitialTab) {
      configuredInitialTab = initialTab
      selectedTab = initialTab
      syncSortOrderWithCurrentScope()
    }
    if (initialViewMode != null && initialViewMode != configuredInitialViewMode) {
      configuredInitialViewMode = initialViewMode
      viewMode = initialViewMode
    }
    if (initialPlayerExpanded) {
      configuredInitialPlayerExpanded = true
      expandFullPlayer()
    } else if (initialPlayerExpanded != configuredInitialPlayerExpanded) {
      configuredInitialPlayerExpanded = initialPlayerExpanded
    }
    if (initialQueueExpanded != configuredInitialQueueExpanded) {
      configuredInitialQueueExpanded = initialQueueExpanded
      initialQueueExpandedState = initialQueueExpanded
    }

    val tracksChanged = initialTracks != null && initialTracks != configuredInitialTracks
    val dirChanged = initialDirectory != configuredInitialDirectory
    if (tracksChanged || dirChanged) {
      configuredInitialTracks = initialTracks ?: configuredInitialTracks
      configuredInitialDirectory = initialDirectory
      val includeSamples = prefs?.getBoolean(PREF_KEY_MUSIC_INCLUDE_SAMPLES, false) ?: false
      val additionalDirs = resolveAdditionalCustomFolderDirectories()
      val reloaded = when {
        initialTracks != null -> initialTracks
        !isMusicFolderGranted -> emptyList()
        appContext != null -> MusicPlayerEngine.getCachedLibraryTracks(
          initialDirectory = initialDirectory,
          includeSamples = includeSamples,
          additionalDirectories = additionalDirs,
          hasFullFilesPermission = hasFullFilesPermission,
        ) ?: allTracks
        else -> MusicPlayerEngine.getCachedLibraryTracks(
          initialDirectory = initialDirectory,
          includeSamples = includeSamples,
          additionalDirectories = additionalDirs,
          hasFullFilesPermission = hasFullFilesPermission,
        ) ?: MusicPlayerEngine.loadLibraryTracks(
          initialDirectory = initialDirectory,
          includeSamples = includeSamples,
          inspectMetadataRetriever = false,
          deferAlbumArt = true,
          additionalDirectories = additionalDirs,
          hasFullFilesPermission = hasFullFilesPermission,
        )
      }
      val activeId = currentTrack?.id
      allTracks = reloaded
      refreshLibraryDerivedStateAfterMutation(reloaded)
      val byId = reloaded.associateBy { it.id }
      if (unshuffledQueue.isNotEmpty()) {
        unshuffledQueue = unshuffledQueue.mapNotNull { existing -> byId[existing.id] ?: existing }
      }
      if (playbackQueue.isNotEmpty()) {
        playbackQueue = playbackQueue.mapNotNull { existing -> byId[existing.id] ?: existing }
        val idx = if (activeId != null) playbackQueue.indexOfFirst { it.id == activeId } else 0
        currentQueueIndex = idx.coerceAtLeast(0)
        prepareAndSyncMediaPlayer(forceRestart = false)
      }
      if (isMusicFolderGranted) {
        startAsyncLibraryLoadAndArtworkPrefetch()
      }
    }
  }

  private fun applyLoadedLibraryTracks(
    loaded: List<MusicTrack>,
    precomputedState: ComputedLibraryState? = null,
  ) {
    val prevTrack = currentTrack
    val prevTrackId = prevTrack?.id
    val activeId = playbackQueue.getOrNull(currentQueueIndex)?.id
    if (allTracks != loaded) {
      allTracks = loaded
    }
    if (precomputedState != null) {
      applyComputedLibraryState(precomputedState)
    } else {
      refreshLibraryDerivedStateAfterMutation(loaded)
    }
    val byId = loaded.associateBy { it.id }
    if (unshuffledQueue.isNotEmpty()) {
      unshuffledQueue = unshuffledQueue.map { existing -> byId[existing.id] ?: existing }
    }
    if (playbackQueue.isNotEmpty()) {
      playbackQueue = playbackQueue.map { existing -> byId[existing.id] ?: existing }
    }
    if (activeId != null) {
      val newIdx = playbackQueue.indexOfFirst { it.id == activeId }
      if (newIdx >= 0) {
        currentQueueIndex = newIdx
      }
    } else if ((isPlayerExpanded || configuredInitialPlayerExpanded) && playbackQueue.isEmpty() && loaded.isNotEmpty() && MusicPlayerEngine.externalSystemMediaInfo == null) {
      unshuffledQueue = loaded
      playbackQueue = loaded
      currentQueueIndex = 0
    }
    val newTrack = currentTrack
    if (newTrack != null && (newTrack.id != prevTrackId || mediaPlayer == null)) {
      trackDurationMs = newTrack.durationMs.coerceAtLeast(1000L)
      prepareAndSyncMediaPlayer(forceRestart = false)
    } else if (newTrack != null && prevTrack != null && (
        newTrack.album != prevTrack.album ||
          newTrack.artist != prevTrack.artist ||
          newTrack.artworkPrimaryRgb != prevTrack.artworkPrimaryRgb
        )
    ) {
      syncActiveTrackPalette(force = true)
    }
  }

  private fun startAsyncLibraryLoadAndArtworkPrefetch() {
    if (isInspection || appContext == null || !isMusicFolderGranted) return
    val fullFilesGranted = MusicPlayerEngine.hasFullFilesPermission(appContext)
    hasFullFilesPermission = fullFilesGranted
    libraryLoadJob?.cancel()
    libraryLoadJob = scope.launch {
      val explicitTracks = configuredInitialTracks
      val additionalDirs = resolveAdditionalCustomFolderDirectories()
      if (explicitTracks == null) {
        isLoadingLibrary = allTracks.isEmpty()
        try {
          val includeSamples = prefs?.getBoolean(PREF_KEY_MUSIC_INCLUDE_SAMPLES, false) ?: false
          val indexFile = MusicPlayerEngine.getDefaultIndexCacheFile(appContext)
          withContext(Dispatchers.IO) {
            MusicPlayerEngine.loadTrackIndexCacheFromDisk(indexFile)
          }
          // Fast pass: uses persisted disk index cache entries for unchanged files without blocking on MediaMetadataRetriever
          if (allTracks.isEmpty()) {
            val fastLoaded = MusicPlayerEngine.loadLibraryTracksAsync(
              initialDirectory = configuredInitialDirectory,
              includeSamples = includeSamples,
              inspectMetadataRetriever = false,
              deferAlbumArt = true,
              additionalDirectories = additionalDirs,
              hasFullFilesPermission = fullFilesGranted,
            )
            if (fastLoaded.isNotEmpty()) {
              val fastComputed = withContext(backgroundCoroutineContext) {
                computeFullLibraryState(fastLoaded)
              }
              applyLoadedLibraryTracks(fastLoaded, fastComputed)
              isLoadingLibrary = false
            }
          }
          // Full background pass: inspects only new/modified files and saves updated index to disk
          val loaded = MusicPlayerEngine.loadLibraryTracksAsync(
            initialDirectory = configuredInitialDirectory,
            includeSamples = includeSamples,
            inspectMetadataRetriever = true,
            deferAlbumArt = true,
            additionalDirectories = additionalDirs,
            hasFullFilesPermission = fullFilesGranted,
          )
          val fullComputed = withContext(backgroundCoroutineContext) {
            computeFullLibraryState(loaded)
          }
          applyLoadedLibraryTracks(loaded, fullComputed)
        } finally {
          isLoadingLibrary = false
        }
      }
      val tracksToPrefetch = allTracks
      if (tracksToPrefetch.isNotEmpty()) {
        try {
          MusicPlayerEngine.prefetchAlbumArt(tracksToPrefetch, sizePx = 240)
          val syncedTracks = MusicPlayerEngine.syncTracksWithExtractedPalettes(allTracks)
          if (syncedTracks !== allTracks) {
            val syncedComputed = withContext(backgroundCoroutineContext) {
              computeFullLibraryState(syncedTracks)
            }
            applyLoadedLibraryTracks(syncedTracks, syncedComputed)
            withContext(Dispatchers.IO) {
              val indexFile = MusicPlayerEngine.getDefaultIndexCacheFile(appContext)
              MusicPlayerEngine.saveTrackIndexCacheToDisk(
                cacheFile = indexFile,
                tracks = syncedTracks.filter { !it.isSampleTrack && it.file != null },
              )
            }
          }
          if (currentTrack != null) {
            syncActiveTrackPalette(force = true)
          }
        } catch (_: kotlinx.coroutines.CancellationException) {
          throw kotlinx.coroutines.CancellationException()
        } catch (_: Throwable) {}
      }
    }
  }

  private fun releaseMediaPlayer() {
    val mp = mediaPlayer
    mediaPlayer = null
    preparedTrackId = null
    if (mp != null) {
      try {
        mp.setOnCompletionListener(null)
      } catch (_: Throwable) {}
      try {
        if (mp.isPlaying) mp.stop()
      } catch (_: Throwable) {}
      try {
        mp.release()
      } catch (_: Throwable) {}
    }
  }

  private fun applyPlaybackSpeed(mp: MediaPlayer?) {
    if (mp == null) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPlaying) {
      try {
        mp.playbackParams = PlaybackParams().setSpeed(playbackSpeed.coerceIn(0.5f, 2.0f))
      } catch (_: Throwable) {}
    }
  }

  internal fun syncActiveTrackPalette(force: Boolean = false) {
    if (isReleased) return
    val track = currentTrack
    val trackId = track?.id
    if (!force && trackId == lastSyncedPaletteTrackId && track != null) {
      return
    }
    lastSyncedPaletteTrackId = trackId
    val immediatePalette = MusicPlayerEngine.resolveTrackPalette(track)
    activePalette = immediatePalette
    MusicPlayerEngine.updateActiveTrackPalette(track, immediatePalette)
    paletteLoadJob?.cancel()
    if (track != null && !isInspection && !track.isSampleTrack && (appContext != null || track.file != null)) {
      paletteLoadJob = scope.launch {
        try {
          val loadedPalette = MusicPlayerEngine.loadTrackPaletteAsync(track, sizePx = 360)
          if (!isReleased && currentTrack?.id == track.id) {
            activePalette = loadedPalette
            MusicPlayerEngine.updateActiveTrackPalette(track, loadedPalette)
            syncSystemMediaControls(
              forceMetadataUpdate = false,
              notifyPlatformInBackground = true,
            )
          }
        } catch (_: kotlinx.coroutines.CancellationException) {
          throw kotlinx.coroutines.CancellationException()
        } catch (_: Throwable) {}
      }
    }
  }

  fun prepareAndSyncMediaPlayer(forceRestart: Boolean = false) {
    if (isReleased) return
    syncActiveTrackPalette(force = forceRestart)
    val track = currentTrack
    if (track == null) {
      abandonSystemAudioFocus()
      releaseMediaPlayer()
      syncProgressTicker()
      syncSystemMediaControls(forceMetadataUpdate = true)
      return
    }

    if (isPlaying) {
      requestSystemAudioFocus()
    } else {
      abandonSystemAudioFocus()
    }

    if (isInspection) {
      trackDurationMs = track.durationMs.coerceAtLeast(1000L)
      syncProgressTicker()
      syncSystemMediaControls(forceMetadataUpdate = forceRestart)
      return
    }

    val existingMp = mediaPlayer
    if (existingMp != null && preparedTrackId == track.id) {
      try {
        if (forceRestart) {
          existingMp.seekTo(currentPositionMs.toInt().coerceAtLeast(0))
        }
        if (isPlaying && !existingMp.isPlaying) {
          existingMp.start()
          applyPlaybackSpeed(existingMp)
        } else if (!isPlaying && existingMp.isPlaying) {
          existingMp.pause()
        } else if (isPlaying) {
          applyPlaybackSpeed(existingMp)
        }
        syncProgressTicker()
        syncSystemMediaControls(forceMetadataUpdate = forceRestart)
        return
      } catch (_: Throwable) {
        releaseMediaPlayer()
      }
    }

    releaseMediaPlayer()
    trackDurationMs = track.durationMs.coerceAtLeast(1000L)

    val dir = cacheDir
    val instanceId = activePlayInstanceId
    if (dir != null) {
      try {
        val playableFile = if (track.isSampleTrack || track.file == null || !track.file.exists()) {
          if (appContext != null) {
            FileManagerEngine.getOrCreateSampleMusicFile(track, appContext)
          } else {
            MusicPlayerEngine.ensurePlayableSampleWavFile(dir, track)
          }
        } else {
          track.file
        }
        if (playableFile.exists()) {
          val mp = MediaPlayer()
          mp.setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
          )
          mp.setDataSource(playableFile.absolutePath)
          mp.prepare()
          val actualDur = mp.duration.toLong()
          if (actualDur > 0L) {
            trackDurationMs = actualDur
          }
          mp.setOnCompletionListener {
            scope.launch {
              autoAdvanceFromInstance(instanceId)
            }
          }
          mediaPlayer = mp
          preparedTrackId = track.id
          if (currentPositionMs > 0L && currentPositionMs < trackDurationMs) {
            mp.seekTo(currentPositionMs.toInt())
          }
          if (isPlaying) {
            mp.start()
            applyPlaybackSpeed(mp)
          }
        }
      } catch (_: Throwable) {
        // Fallback simulated timer loop in syncProgressTicker handles progression if hardware codec is unavailable
      }
    }

    syncProgressTicker()
    syncSystemMediaControls(forceMetadataUpdate = true)
  }

  private fun autoAdvanceFromInstance(instanceId: Long) {
    if (isReleased || instanceId != activePlayInstanceId) return
    skipToNext(userInitiated = false)
  }

  private fun syncProgressTicker() {
    if (isReleased || !isPlaying || currentTrack == null) {
      progressJob?.cancel()
      progressJob = null
      return
    }
    if (progressJob?.isActive == true) return

    progressJob = scope.launch {
      val tickMs = 200L
      var ticksSinceFlush = 0
      while (isActive && isPlaying && !isReleased) {
        val tickInstanceId = activePlayInstanceId
        delay(tickMs)
        val activeTrack = currentTrack ?: break
        if (!isPlaying || isReleased) break
        recordTrackPlayStats(
          trackId = activeTrack.id,
          incrementPlayCount = false,
          addedPlayTimeMs = tickMs,
          persistImmediately = ++ticksSinceFlush % 10 == 0,
        )
        if (!isDraggingSeek) {
          val mp = mediaPlayer
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
              autoAdvanceFromInstance(tickInstanceId)
            } else {
              currentPositionMs = nextPos
            }
          }
          if (ticksSinceFlush % 5 == 0 && appContext != null && !isInspection) {
            val safeDur = trackDurationMs.coerceAtLeast(1L)
            val permille = ((currentPositionMs.toFloat() / safeDur.toFloat()) * 1000f).roundToInt().coerceIn(0, 1000)
            MusicPlayerWidgets.updateMiniBarProgressOnly(appContext, permille)
          }
        }
      }
    }
  }

  fun setPlayingState(playing: Boolean) {
    if (isReleased) return
    isPlaying = playing
    val activeId = currentTrack?.id
    if (playing && activeId != null && lastCountedPlayTrackId != activeId) {
      lastCountedPlayTrackId = activeId
      recordTrackPlayStats(trackId = activeId, incrementPlayCount = true, persistImmediately = true)
    } else if (!playing) {
      flushPlayStats()
    }
    prepareAndSyncMediaPlayer(forceRestart = false)
  }

  fun togglePlayPause() {
    setPlayingState(!isPlaying)
  }

  private fun recordSkipIfEligible(requireActivePlayback: Boolean = false) {
    val outgoingTrack = currentTrack ?: return
    val duration = trackDurationMs.coerceAtLeast(1_000L)
    if (currentPositionMs >= (duration * 95L) / 100L) return
    if (requireActivePlayback && (!isPlaying || currentPositionMs <= 0L)) return
    recordTrackPlayStats(
      trackId = outgoingTrack.id,
      incrementSkipCount = true,
      persistImmediately = true,
    )
  }

  fun stepToTrack(
    newIndex: Int,
    autoPlay: Boolean = isPlaying,
    recordSkipOnSwitch: Boolean = true,
  ) {
    if (isReleased || playbackQueue.isEmpty()) return
    val clamped = newIndex.coerceIn(0, playbackQueue.lastIndex)
    val prevId = playbackQueue.getOrNull(currentQueueIndex)?.id
    val target = playbackQueue[clamped]
    if (recordSkipOnSwitch && prevId != null && target.id != prevId) {
      recordSkipIfEligible(requireActivePlayback = true)
    }
    activePlayInstanceId++
    currentQueueIndex = clamped
    currentPositionMs = 0L
    trackDurationMs = target.durationMs.coerceAtLeast(1000L)
    if (autoPlay) {
      if (target.id == prevId) {
        lastCountedPlayTrackId = target.id
      } else {
        lastCountedPlayTrackId = target.id
      }
      recordTrackPlayStats(trackId = target.id, incrementPlayCount = true, persistImmediately = true)
    }
    isPlaying = autoPlay
    prepareAndSyncMediaPlayer(forceRestart = true)
  }

  fun playTrackInContext(
    track: MusicTrack,
    contextTracks: List<MusicTrack>,
  ) {
    if (isReleased) return
    if (currentTrack?.id != null && currentTrack?.id != track.id) {
      recordSkipIfEligible(requireActivePlayback = true)
    }
    activePlayInstanceId++
    val queueList = if (contextTracks.isNotEmpty()) contextTracks else listOf(track)
    unshuffledQueue = queueList
    if (shuffleMode.isEnabled) {
      val (shuffled, idx) = MusicPlayerEngine.applyShuffleModeToQueue(
        baseQueue = queueList,
        currentQueue = queueList,
        anchorTrackId = track.id,
        mode = shuffleMode,
        playStats = playStats,
      )
      playbackQueue = shuffled
      currentQueueIndex = idx
    } else {
      playbackQueue = queueList
      val idx = queueList.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
      currentQueueIndex = idx
    }
    currentPositionMs = 0L
    trackDurationMs = track.durationMs.coerceAtLeast(1000L)
    lastCountedPlayTrackId = track.id
    recordTrackPlayStats(trackId = track.id, incrementPlayCount = true, persistImmediately = true)
    isPlaying = true
    prepareAndSyncMediaPlayer(forceRestart = true)
  }

  /**
   * Starts shuffled playback of [contextTracks] while preserving [contextTracks] as [unshuffledQueue]
   * so turning shuffle off restores the original context order. If [shuffleMode] is currently
   * [MusicShuffleMode.OFF], activates [MusicShuffleMode.SHUFFLE]; if already in
   * [MusicShuffleMode.SMART_SHUFFLE], preserves smart shuffle.
   */
  fun playShuffledContext(
    contextTracks: List<MusicTrack>,
    random: kotlin.random.Random = kotlin.random.Random.Default,
  ) {
    if (isReleased || contextTracks.isEmpty()) return
    val activeMode = if (shuffleMode == MusicShuffleMode.OFF) {
      shuffleMode = MusicShuffleMode.SHUFFLE
      prefs?.edit()
        ?.putString(PREF_KEY_MUSIC_SHUFFLE_MODE, MusicShuffleMode.SHUFFLE.name)
        ?.putBoolean(PREF_KEY_MUSIC_SHUFFLE, true)
        ?.apply()
      MusicShuffleMode.SHUFFLE
    } else {
      shuffleMode
    }
    val (shuffled, _) = MusicPlayerEngine.applyShuffleModeToQueue(
      baseQueue = contextTracks,
      currentQueue = contextTracks,
      anchorTrackId = null,
      mode = activeMode,
      playStats = playStats,
      random = random,
    )
    val startTrack = shuffled.first()
    if (currentTrack?.id != null && currentTrack?.id != startTrack.id) {
      recordSkipIfEligible(requireActivePlayback = true)
    }
    activePlayInstanceId++
    unshuffledQueue = contextTracks
    playbackQueue = shuffled
    currentQueueIndex = 0
    currentPositionMs = 0L
    trackDurationMs = startTrack.durationMs.coerceAtLeast(1000L)
    lastCountedPlayTrackId = startTrack.id
    recordTrackPlayStats(trackId = startTrack.id, incrementPlayCount = true, persistImmediately = true)
    isPlaying = true
    prepareAndSyncMediaPlayer(forceRestart = true)
  }

  fun skipToNext(userInitiated: Boolean = true) {
    if (isReleased) return
    if (userInitiated) {
      recordSkipIfEligible(requireActivePlayback = false)
    }
    val nextIdx = MusicPlayerEngine.resolveNextTrackIndex(
      currentIndex = currentQueueIndex,
      queueSize = playbackQueue.size,
      repeatMode = repeatMode,
      shuffleEnabled = isShuffleEnabled,
      userInitiated = userInitiated,
    )
    if (nextIdx != null) {
      stepToTrack(nextIdx, autoPlay = if (userInitiated) isPlaying else true, recordSkipOnSwitch = false)
    } else {
      activePlayInstanceId++
      isPlaying = false
      currentPositionMs = 0L
      try {
        val mp = mediaPlayer
        if (mp != null) {
          if (mp.isPlaying) mp.pause()
          mp.seekTo(0)
        }
      } catch (_: Throwable) {}
      flushPlayStats()
      syncProgressTicker()
      syncSystemMediaControls(forceMetadataUpdate = false)
    }
  }

  fun skipToPrevious() {
    if (isReleased) return
    val prevIdx = MusicPlayerEngine.resolvePreviousTrackIndex(
      currentIndex = currentQueueIndex,
      queueSize = playbackQueue.size,
      currentPositionMs = currentPositionMs,
    ) ?: return
    if (prevIdx == currentQueueIndex) {
      activePlayInstanceId++
      currentPositionMs = 0L
      if (isPlaying) {
        playbackQueue.getOrNull(currentQueueIndex)?.id?.let { id ->
          recordTrackPlayStats(trackId = id, incrementPlayCount = true, persistImmediately = true)
        }
      }
      prepareAndSyncMediaPlayer(forceRestart = true)
    } else {
      recordSkipIfEligible(requireActivePlayback = false)
      stepToTrack(prevIdx, autoPlay = isPlaying, recordSkipOnSwitch = false)
    }
  }

  fun moveQueueItem(fromIndex: Int, toIndex: Int): Boolean {
    val (updatedQueue, updatedCurrentIndex) = MusicPlayerEngine.reorderQueue(
      queue = playbackQueue,
      fromIndex = fromIndex,
      toIndex = toIndex,
      currentQueueIndex = currentQueueIndex,
    )
    if (updatedQueue !== playbackQueue) {
      playbackQueue = updatedQueue
      if (!shuffleMode.isEnabled) {
        unshuffledQueue = updatedQueue
      }
      currentQueueIndex = updatedCurrentIndex
      return true
    }
    return false
  }

  fun playNext(tracks: List<MusicTrack>) {
    if (isReleased || tracks.isEmpty()) return
    if (playbackQueue.isEmpty()) {
      playTrackInContext(tracks.first(), tracks)
      return
    }
    val insertIdx = (currentQueueIndex + 1).coerceIn(0, playbackQueue.size)
    val mutableQueue = playbackQueue.toMutableList()
    mutableQueue.addAll(insertIdx, tracks)
    playbackQueue = mutableQueue
    val mutableUnshuffled = unshuffledQueue.toMutableList()
    val unshuffledInsertIdx = (unshuffledQueue.indexOfFirst { it.id == currentTrack?.id } + 1)
      .coerceIn(0, mutableUnshuffled.size)
    mutableUnshuffled.addAll(unshuffledInsertIdx, tracks)
    unshuffledQueue = mutableUnshuffled
  }

  fun addToQueue(tracks: List<MusicTrack>) {
    if (isReleased || tracks.isEmpty()) return
    if (playbackQueue.isEmpty()) {
      playTrackInContext(tracks.first(), tracks)
      return
    }
    playbackQueue = playbackQueue + tracks
    unshuffledQueue = unshuffledQueue + tracks
  }

  fun resolveTrackFile(track: MusicTrack, context: Context? = appContext): File? {
    val existing = track.file
    if (existing != null && existing.exists()) {
      return existing
    }
    return try {
      when {
        context != null -> FileManagerEngine.getOrCreateSampleMusicFile(track, context)
        cacheDir != null -> MusicPlayerEngine.ensurePlayableSampleWavFile(cacheDir, track)
        else -> existing
      }
    } catch (_: Throwable) {
      existing
    }
  }

  fun resolveCollectionFiles(tracks: List<MusicTrack>, context: Context? = appContext): List<File> {
    return tracks.mapNotNull { resolveTrackFile(it, context) }.filter { it.exists() }
  }

  fun duplicateTrack(track: MusicTrack, context: Context? = appContext): MusicTrack {
    val resolvedFile = resolveTrackFile(track, context)
    val dupFile = if (resolvedFile != null && resolvedFile.exists()) {
      FileManagerEngine.duplicateFile(resolvedFile).getOrNull()
    } else {
      null
    }
    val newTitle = if (dupFile != null) {
      dupFile.nameWithoutExtension
    } else {
      "${track.title} (Copy)"
    }
    val duplicatedTrack = track.copy(
      id = dupFile?.absolutePath ?: "${track.id}_copy_${System.currentTimeMillis()}",
      title = newTitle,
      file = dupFile ?: track.file,
    )
    val idx = allTracks.indexOfFirst { it.id == track.id }
    val updatedAll = allTracks.toMutableList()
    if (idx >= 0) {
      updatedAll.add(idx + 1, duplicatedTrack)
    } else {
      updatedAll.add(0, duplicatedTrack)
    }
    allTracks = updatedAll
    MusicPlayerEngine.updateCachedLibrarySnapshot(updatedAll)
    refreshLibraryDerivedStateAfterMutation(updatedAll)
    scope.launch(ioCoroutineContext) {
      MusicPlayerEngine.saveTrackIndexCacheToDisk(tracks = updatedAll)
    }
    return duplicatedTrack
  }

  var isUpdatingFileMetadataInBackground by mutableStateOf(false)
    internal set

  var backgroundFileMetadataUpdateCount by mutableIntStateOf(0)
    internal set

  private var metadataUpdateJob: Job? = null

  suspend fun awaitPendingMetadataUpdates() {
    metadataUpdateJob?.join()
  }

  /**
   * Delegates writing updated audio metadata tags (`title`, `artist`, `album`, `genre`) onto the
   * underlying audio files of [affectedTracks] to a background task on [ioCoroutineContext],
   * refreshing file signatures (`lastModifiedMs`, `sizeBytes`) and persisting the disk index cache.
   */
  private fun scheduleBackgroundFileMetadataUpdate(
    affectedTracks: List<MusicTrack>,
  ) {
    val tracksWithFiles = affectedTracks.filter { it.file != null && it.file.exists() }
    val previousJob = metadataUpdateJob
    if (tracksWithFiles.isNotEmpty()) {
      isUpdatingFileMetadataInBackground = true
    }
    metadataUpdateJob = scope.launch(ioCoroutineContext) {
      try {
        previousJob?.join()
        val updatedById = LinkedHashMap<String, Pair<Long, Long>>()
        for (track in tracksWithFiles) {
          ensureActive()
          val file = track.file ?: continue
          if (file.exists() && MusicPlayerEngine.updateTrackFileMetadata(track, file)) {
            updatedById[track.id] = file.lastModified() to file.length()
            backgroundFileMetadataUpdateCount += 1
          }
        }
        if (updatedById.isNotEmpty() && !isReleased) {
          Snapshot.withMutableSnapshot {
            allTracks = allTracks.map { t ->
              val sig = updatedById[t.id]
              if (sig != null) t.copy(lastModifiedMs = sig.first, sizeBytes = sig.second) else t
            }
            unshuffledQueue = unshuffledQueue.map { t ->
              val sig = updatedById[t.id]
              if (sig != null) t.copy(lastModifiedMs = sig.first, sizeBytes = sig.second) else t
            }
            playbackQueue = playbackQueue.map { t ->
              val sig = updatedById[t.id]
              if (sig != null) t.copy(lastModifiedMs = sig.first, sizeBytes = sig.second) else t
            }
            sortedLibraryTracks = sortedLibraryTracks.map { t ->
              val sig = updatedById[t.id]
              if (sig != null) t.copy(lastModifiedMs = sig.first, sizeBytes = sig.second) else t
            }
          }
          MusicPlayerEngine.updateCachedLibrarySnapshot(allTracks)
        }
        MusicPlayerEngine.saveTrackIndexCacheToDisk(tracks = allTracks)
      } finally {
        if (!isReleased) {
          Snapshot.withMutableSnapshot {
            isUpdatingFileMetadataInBackground = false
          }
        }
      }
    }
  }

  fun renameTrack(track: MusicTrack, newNameInput: String, context: Context? = appContext): MusicTrack? {
    val trimmed = newNameInput.trim()
    if (trimmed.isEmpty()) return null
    val existingFile = track.file
    var renamedFile: File? = existingFile
    if (existingFile != null && existingFile.exists()) {
      val ext = existingFile.extension
      val targetFileName = if (ext.isNotEmpty() && !trimmed.contains(".")) {
        "$trimmed.$ext"
      } else {
        trimmed
      }
      val renameResult = FileManagerEngine.renameFile(existingFile, targetFileName)
      if (renameResult.isSuccess) {
        renamedFile = renameResult.getOrNull()
      }
    }
    val newTitle = if (renamedFile != null && trimmed.contains(".")) {
      renamedFile.nameWithoutExtension
    } else {
      trimmed.substringBeforeLast(".")
    }.ifEmpty { trimmed }
    val updatedTrack = track.copy(
      id = renamedFile?.absolutePath ?: track.id,
      title = newTitle,
      file = renamedFile,
    )
    allTracks = allTracks.map { if (it.id == track.id) updatedTrack else it }
    unshuffledQueue = unshuffledQueue.map { if (it.id == track.id) updatedTrack else it }
    playbackQueue = playbackQueue.map { if (it.id == track.id) updatedTrack else it }
    val snapshot = allTracks
    MusicPlayerEngine.updateCachedLibrarySnapshot(snapshot)
    refreshLibraryDerivedStateAfterMutation(snapshot)
    scheduleBackgroundFileMetadataUpdate(listOf(updatedTrack))
    return updatedTrack
  }

  /**
   * Renames an entire artist across all matching tracks in the library and active playback queues,
   * updating favorite artist/album references, active drill-down state, and adjusting the `artist`
   * metadata on affected audio files in a background task.
   */
  fun renameArtist(oldArtistName: String, newArtistName: String): Int {
    val oldTrimmed = oldArtistName.trim()
    val newTrimmed = newArtistName.trim()
    if (oldTrimmed.isEmpty() || newTrimmed.isEmpty() || oldTrimmed == newTrimmed) return 0
    val affectedTracks = ArrayList<MusicTrack>()
    allTracks = allTracks.map { track ->
      if (track.artist.trim().equals(oldTrimmed, ignoreCase = true)) {
        val updated = track.copy(artist = newTrimmed)
        affectedTracks.add(updated)
        updated
      } else {
        track
      }
    }
    if (affectedTracks.isEmpty()) return 0
    unshuffledQueue = unshuffledQueue.map { track ->
      if (track.artist.trim().equals(oldTrimmed, ignoreCase = true)) track.copy(artist = newTrimmed) else track
    }
    playbackQueue = playbackQueue.map { track ->
      if (track.artist.trim().equals(oldTrimmed, ignoreCase = true)) track.copy(artist = newTrimmed) else track
    }
    if (favoriteArtists.any { it.equals(oldTrimmed, ignoreCase = true) }) {
      val updatedFavArtists = favoriteArtists
        .filterNot { it.equals(oldTrimmed, ignoreCase = true) }
        .toSet() + newTrimmed
      favoriteArtists = updatedFavArtists
      persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_ARTISTS, updatedFavArtists, separator = "\n")
    }
    val updatedFavAlbums = favoriteAlbums.map { entry ->
      if (entry.contains("|")) {
        val albumPart = entry.substringBefore("|")
        val artistPart = entry.substringAfter("|")
        if (artistPart.trim().equals(oldTrimmed, ignoreCase = true)) {
          "$albumPart|$newTrimmed"
        } else {
          entry
        }
      } else {
        entry
      }
    }.toSet()
    if (updatedFavAlbums != favoriteAlbums) {
      favoriteAlbums = updatedFavAlbums
      persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_ALBUMS, updatedFavAlbums, separator = "\n")
    }
    if (selectedArtistName?.trim()?.equals(oldTrimmed, ignoreCase = true) == true) {
      selectedArtistName = newTrimmed
    }
    selectedAlbumKey?.let { key ->
      if (key.contains("|")) {
        val albumPart = key.substringBefore("|")
        val artistPart = key.substringAfter("|")
        if (artistPart.trim().equals(oldTrimmed, ignoreCase = true)) {
          selectedAlbumKey = "$albumPart|$newTrimmed"
        }
      }
    }
    val snapshot = allTracks
    MusicPlayerEngine.updateCachedLibrarySnapshot(snapshot)
    refreshLibraryDerivedStateAfterMutation(snapshot)
    scheduleBackgroundFileMetadataUpdate(affectedTracks)
    return affectedTracks.size
  }

  /**
   * Renames an entire album across all matching tracks in the library and active playback queues,
   * updating favorite album references, active drill-down state, and adjusting the `album`
   * metadata on affected audio files in a background task.
   */
  fun renameAlbum(oldAlbumName: String, artistName: String = "", newAlbumName: String): Int {
    val oldTrimmed = oldAlbumName.trim()
    val artistTrimmed = artistName.trim()
    val newTrimmed = newAlbumName.trim()
    if (oldTrimmed.isEmpty() || newTrimmed.isEmpty() || oldTrimmed == newTrimmed) return 0
    val ignoreArtistFilter = artistTrimmed.isEmpty() ||
      artistTrimmed.equals("Various Artists", ignoreCase = true) ||
      MusicPlayerEngine.isSingleFolderAlbumName(oldTrimmed)
    fun matches(track: MusicTrack): Boolean {
      return track.album.trim().equals(oldTrimmed, ignoreCase = true) &&
        (ignoreArtistFilter || track.artist.trim().equals(artistTrimmed, ignoreCase = true))
    }
    val affectedTracks = ArrayList<MusicTrack>()
    var matchedArtist = artistTrimmed
    allTracks = allTracks.map { track ->
      if (matches(track)) {
        if (matchedArtist.isEmpty()) matchedArtist = track.artist
        val updated = track.copy(album = newTrimmed)
        affectedTracks.add(updated)
        updated
      } else {
        track
      }
    }
    if (affectedTracks.isEmpty()) return 0
    unshuffledQueue = unshuffledQueue.map { track ->
      if (matches(track)) track.copy(album = newTrimmed) else track
    }
    playbackQueue = playbackQueue.map { track ->
      if (matches(track)) track.copy(album = newTrimmed) else track
    }
    if (MusicPlayerEngine.isAlbumFavorite(oldTrimmed, matchedArtist, favoriteAlbums)) {
      val withoutOld = MusicPlayerEngine.toggleFavoriteAlbum(favoriteAlbums, oldTrimmed, matchedArtist)
      val withNew = MusicPlayerEngine.toggleFavoriteAlbum(withoutOld, newTrimmed, matchedArtist)
      favoriteAlbums = withNew
      persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_ALBUMS, withNew, separator = "\n")
    }
    selectedAlbumKey?.let { key ->
      val keyAlbum = key.substringBefore("|").trim()
      val keyArtist = key.substringAfter("|", "").trim()
      if (
        keyAlbum.equals(oldTrimmed, ignoreCase = true) &&
        (artistTrimmed.isEmpty() || keyArtist.isEmpty() || keyArtist.equals(artistTrimmed, ignoreCase = true))
      ) {
        val targetArtist = keyArtist.ifEmpty { matchedArtist }
        selectedAlbumKey = if (targetArtist.isNotEmpty()) "$newTrimmed|$targetArtist" else newTrimmed
      }
    }
    val snapshot = allTracks
    MusicPlayerEngine.updateCachedLibrarySnapshot(snapshot)
    refreshLibraryDerivedStateAfterMutation(snapshot)
    scheduleBackgroundFileMetadataUpdate(affectedTracks)
    return affectedTracks.size
  }

  /**
   * Renames an entire genre across all matching tracks in the library and active playback queues,
   * updating favorite genre references, active drill-down state, and adjusting the `genre`
   * metadata on affected audio files in a background task.
   */
  fun renameGenre(oldGenreName: String, newGenreName: String): Int {
    val oldTrimmed = oldGenreName.trim()
    val newTrimmed = newGenreName.trim()
    if (oldTrimmed.isEmpty() || newTrimmed.isEmpty() || oldTrimmed == newTrimmed) return 0
    val affectedTracks = ArrayList<MusicTrack>()
    allTracks = allTracks.map { track ->
      if (track.genre.trim().equals(oldTrimmed, ignoreCase = true)) {
        val updated = track.copy(genre = newTrimmed)
        affectedTracks.add(updated)
        updated
      } else {
        track
      }
    }
    if (affectedTracks.isEmpty()) return 0
    unshuffledQueue = unshuffledQueue.map { track ->
      if (track.genre.trim().equals(oldTrimmed, ignoreCase = true)) track.copy(genre = newTrimmed) else track
    }
    playbackQueue = playbackQueue.map { track ->
      if (track.genre.trim().equals(oldTrimmed, ignoreCase = true)) track.copy(genre = newTrimmed) else track
    }
    if (favoriteGenres.any { it.equals(oldTrimmed, ignoreCase = true) }) {
      val updatedFavGenres = favoriteGenres
        .filterNot { it.equals(oldTrimmed, ignoreCase = true) }
        .toSet() + newTrimmed
      favoriteGenres = updatedFavGenres
      persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_GENRES, updatedFavGenres, separator = "\n")
    }
    if (selectedGenreName?.trim()?.equals(oldTrimmed, ignoreCase = true) == true) {
      selectedGenreName = newTrimmed
    }
    val snapshot = allTracks
    MusicPlayerEngine.updateCachedLibrarySnapshot(snapshot)
    refreshLibraryDerivedStateAfterMutation(snapshot)
    scheduleBackgroundFileMetadataUpdate(affectedTracks)
    return affectedTracks.size
  }

  /**
   * Deletes all [tracksToDelete] from disk (when backed by local files), the library, and the
   * playback queue, updating playback state cleanly.
   */
  fun deleteTracks(tracksToDelete: Collection<MusicTrack>): Int {
    if (tracksToDelete.isEmpty()) return 0
    val idsToDelete = tracksToDelete.map { it.id }.toSet()
    tracksToDelete.forEach { track ->
      track.file?.let { file ->
        if (file.exists()) {
          FileManagerEngine.deleteFileOrDirectory(file)
        }
      }
    }
    val wasCurrentDeleted = currentTrack?.id in idsToDelete
    val oldQueueIdx = currentQueueIndex
    val removedBeforeCurrent = playbackQueue.take(oldQueueIdx).count { it.id in idsToDelete }
    val beforeCount = allTracks.size
    allTracks = allTracks.filterNot { it.id in idsToDelete }
    val deletedCount = beforeCount - allTracks.size
    unshuffledQueue = unshuffledQueue.filterNot { it.id in idsToDelete }
    val newPlaybackQueue = playbackQueue.filterNot { it.id in idsToDelete }
    playbackQueue = newPlaybackQueue
    if (favoriteIds.any { it in idsToDelete }) {
      val nextFavs = favoriteIds - idsToDelete
      favoriteIds = nextFavs
      persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITES, nextFavs, separator = "|")
    }
    if (newPlaybackQueue.isEmpty()) {
      currentQueueIndex = 0
      isPlaying = false
      currentPositionMs = 0L
      abandonSystemAudioFocus()
      releaseMediaPlayer()
      syncSystemMediaControls(forceMetadataUpdate = true)
    } else {
      currentQueueIndex = (oldQueueIdx - removedBeforeCurrent).coerceIn(0, newPlaybackQueue.lastIndex)
      if (wasCurrentDeleted) {
        currentPositionMs = 0L
        prepareAndSyncMediaPlayer(forceRestart = true)
      }
    }
    val snapshot = allTracks
    MusicPlayerEngine.updateCachedLibrarySnapshot(snapshot)
    refreshLibraryDerivedStateAfterMutation(snapshot)
    scope.launch(ioCoroutineContext) {
      MusicPlayerEngine.saveTrackIndexCacheToDisk(tracks = snapshot)
    }
    return deletedCount
  }

  fun deleteTrack(track: MusicTrack) {
    deleteTracks(listOf(track))
  }

  /**
   * Bulk-deletes an entire artist and all of their songs from the library.
   */
  fun deleteArtist(artistName: String): Int {
    val trimmed = artistName.trim()
    if (trimmed.isEmpty()) return 0
    val matching = allTracks.filter { it.artist.trim().equals(trimmed, ignoreCase = true) }
    val deleted = deleteTracks(matching)
    if (favoriteArtists.any { it.equals(trimmed, ignoreCase = true) }) {
      val next = favoriteArtists.filterNot { it.equals(trimmed, ignoreCase = true) }.toSet()
      favoriteArtists = next
      persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_ARTISTS, next, separator = "\n")
    }
    if (selectedArtistName?.trim()?.equals(trimmed, ignoreCase = true) == true) {
      selectedArtistName = null
    }
    return deleted
  }

  /**
   * Bulk-deletes an entire album and all of its songs from the library.
   */
  fun deleteAlbum(albumName: String, artistName: String = ""): Int {
    val trimmedAlbum = albumName.trim()
    val trimmedArtist = artistName.trim()
    if (trimmedAlbum.isEmpty()) return 0
    val ignoreArtistFilter = trimmedArtist.isEmpty() ||
      trimmedArtist.equals("Various Artists", ignoreCase = true) ||
      MusicPlayerEngine.isSingleFolderAlbumName(trimmedAlbum)
    val matching = allTracks.filter { track ->
      track.album.trim().equals(trimmedAlbum, ignoreCase = true) &&
        (ignoreArtistFilter || track.artist.trim().equals(trimmedArtist, ignoreCase = true))
    }
    val deleted = deleteTracks(matching)
    if (MusicPlayerEngine.isAlbumFavorite(trimmedAlbum, trimmedArtist, favoriteAlbums)) {
      val next = MusicPlayerEngine.toggleFavoriteAlbum(favoriteAlbums, trimmedAlbum, trimmedArtist)
      favoriteAlbums = next
      persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_ALBUMS, next, separator = "\n")
    }
    selectedAlbumKey?.let { key ->
      val keyAlbum = key.substringBefore("|").trim()
      val keyArtist = key.substringAfter("|", "").trim()
      if (
        keyAlbum.equals(trimmedAlbum, ignoreCase = true) &&
        (trimmedArtist.isEmpty() || keyArtist.isEmpty() || keyArtist.equals(trimmedArtist, ignoreCase = true))
      ) {
        selectedAlbumKey = null
      }
    }
    return deleted
  }

  /**
   * Bulk-deletes an entire genre and all of its songs from the library.
   */
  fun deleteGenre(genreName: String): Int {
    val trimmed = genreName.trim()
    if (trimmed.isEmpty()) return 0
    val matching = allTracks.filter { it.genre.trim().equals(trimmed, ignoreCase = true) }
    val deleted = deleteTracks(matching)
    if (favoriteGenres.any { it.equals(trimmed, ignoreCase = true) }) {
      val next = favoriteGenres.filterNot { it.equals(trimmed, ignoreCase = true) }.toSet()
      favoriteGenres = next
      persistFavoritesInBackground(PREF_KEY_MUSIC_FAVORITE_GENRES, next, separator = "\n")
    }
    if (selectedGenreName?.trim()?.equals(trimmed, ignoreCase = true) == true) {
      selectedGenreName = null
    }
    return deleted
  }

  fun updateSeekDrag(fraction: Float) {
    isDraggingSeek = true
    dragSeekFraction = fraction.coerceIn(0f, 1f)
  }

  fun finishSeekDrag() {
    val targetMs = (dragSeekFraction * trackDurationMs).toLong().coerceIn(0L, trackDurationMs)
    isDraggingSeek = false
    seekToPositionMs(targetMs)
  }

  fun seekToPositionMs(positionMs: Long) {
    val targetMs = positionMs.coerceIn(0L, trackDurationMs)
    currentPositionMs = targetMs
    try {
      mediaPlayer?.seekTo(targetMs.toInt())
    } catch (_: Throwable) {}
    syncSystemMediaControls(forceMetadataUpdate = false)
  }

  fun setPlaybackSpeedValue(speed: Float) {
    val clamped = speed.coerceIn(0.5f, 2.0f)
    playbackSpeed = clamped
    applyPlaybackSpeed(mediaPlayer)
    syncSystemMediaControls(forceMetadataUpdate = false)
  }

  fun cyclePlaybackSpeed() {
    val idx = MusicPlayerEngine.PLAYBACK_SPEED_OPTIONS.indexOfFirst { it == playbackSpeed }
    val nextIdx = if (idx >= 0) (idx + 1) % MusicPlayerEngine.PLAYBACK_SPEED_OPTIONS.size else 2
    setPlaybackSpeedValue(MusicPlayerEngine.PLAYBACK_SPEED_OPTIONS[nextIdx])
  }

  /**
   * Pauses playback when Bluetooth headphones (or active external headphones) disconnect.
   * Returns `true` if playback was active and has now been paused.
   */
  fun onBluetoothHeadphonesDisconnected(): Boolean {
    if (isReleased || !isPlaying) return false
    bluetoothDisconnectPauseCount += 1
    setPlayingState(false)
    return true
  }

  /**
   * Evaluates an incoming audio/Bluetooth disconnect broadcast [Intent] (or action string) and pauses
   * playback if currently playing.
   */
  fun handleAudioDisconnectIntent(intent: Intent?): Boolean {
    if (intent == null) return false
    val action = intent.action
    val btState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED)
    val prevBtState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, BluetoothProfile.STATE_CONNECTED)
    val scoState = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED)
    val prevScoState = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE, AudioManager.SCO_AUDIO_STATE_CONNECTED)
    val effectivePrev = if (action == AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED) prevScoState else prevBtState
    if (MusicPlayerEngine.shouldPauseOnAudioDisconnectBroadcast(
        action = action,
        bluetoothState = btState,
        previousBluetoothState = effectivePrev,
        scoAudioState = scoState,
      )
    ) {
      return onBluetoothHeadphonesDisconnected()
    }
    return false
  }

  /**
   * Handles an action from Android system media player controls or media notification buttons.
   */
  fun handleMediaControlAction(action: String?): Boolean {
    if (isReleased || action.isNullOrBlank()) return false
    return when (action) {
      MusicPlayerEngine.ACTION_MEDIA_PLAY -> {
        if (currentTrack == null && allTracks.isNotEmpty()) {
          playTrackInContext(allTracks.first(), allTracks)
        } else {
          setPlayingState(true)
        }
        true
      }
      MusicPlayerEngine.ACTION_MEDIA_PAUSE,
      MusicPlayerEngine.ACTION_MEDIA_STOP -> {
        setPlayingState(false)
        true
      }
      MusicPlayerEngine.ACTION_MEDIA_PLAY_PAUSE -> {
        if (currentTrack == null && allTracks.isNotEmpty()) {
          playTrackInContext(allTracks.first(), allTracks)
        } else {
          togglePlayPause()
        }
        true
      }
      MusicPlayerEngine.ACTION_MEDIA_NEXT -> {
        if (playbackQueue.isEmpty() && allTracks.isNotEmpty()) {
          unshuffledQueue = allTracks
          playbackQueue = allTracks
          currentQueueIndex = 0
        }
        skipToNext(userInitiated = true)
        true
      }
      MusicPlayerEngine.ACTION_MEDIA_PREVIOUS -> {
        if (playbackQueue.isEmpty() && allTracks.isNotEmpty()) {
          unshuffledQueue = allTracks
          playbackQueue = allTracks
          currentQueueIndex = 0
        }
        skipToPrevious()
        true
      }
      MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE -> {
        val active = currentTrack ?: allTracks.firstOrNull() ?: return false
        toggleFavorite(active.id)
        true
      }
      MusicPlayerEngine.ACTION_MEDIA_CYCLE_REPEAT -> {
        cycleRepeatMode()
        true
      }
      MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE -> {
        cycleShuffleMode()
        true
      }
      else -> false
    }
  }

  /**
   * Handles hardware or Bluetooth AVRCP media button key events.
   */
  fun handleMediaKeyEvent(keyCode: Int, keyAction: Int = KeyEvent.ACTION_DOWN): Boolean {
    if (isReleased || keyAction != KeyEvent.ACTION_DOWN) return false
    return when (keyCode) {
      KeyEvent.KEYCODE_MEDIA_PLAY -> handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_PLAY)
      KeyEvent.KEYCODE_MEDIA_PAUSE,
      KeyEvent.KEYCODE_MEDIA_STOP -> handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_PAUSE)
      KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
      KeyEvent.KEYCODE_HEADSETHOOK -> handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_PLAY_PAUSE)
      KeyEvent.KEYCODE_MEDIA_NEXT -> handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_NEXT)
      KeyEvent.KEYCODE_MEDIA_PREVIOUS -> handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_PREVIOUS)
      else -> false
    }
  }

  private fun requestSystemAudioFocus() {
    val am = audioManager ?: return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val req = (audioFocusRequest as? AudioFocusRequest) ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
          )
          .setOnAudioFocusChangeListener(audioFocusListener)
          .build()
          .also { audioFocusRequest = it }
        am.requestAudioFocus(req)
      } else {
        @Suppress("DEPRECATION")
        am.requestAudioFocus(
          audioFocusListener,
          AudioManager.STREAM_MUSIC,
          AudioManager.AUDIOFOCUS_GAIN,
        )
      }
    } catch (_: Throwable) {}
  }

  private fun abandonSystemAudioFocus() {
    val am = audioManager ?: return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        (audioFocusRequest as? AudioFocusRequest)?.let { am.abandonAudioFocusRequest(it) }
      } else {
        @Suppress("DEPRECATION")
        am.abandonAudioFocus(audioFocusListener)
      }
    } catch (_: Throwable) {}
  }

  private fun initSystemAudioAndMediaControls() {
    if (isInspection || appContext == null) return

    // 1. Register Bluetooth & Audio Becoming Noisy disconnect receiver
    try {
      val disconnectReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          handleAudioDisconnectIntent(intent)
        }
      }
      val disconnectFilter = IntentFilter().apply {
        addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
        addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
      }
      ContextCompat.registerReceiver(
        appContext,
        disconnectReceiver,
        disconnectFilter,
        ContextCompat.RECEIVER_EXPORTED,
      )
      audioDisconnectReceiver = disconnectReceiver
    } catch (_: Throwable) {}

    // 2. Register AudioDeviceCallback for immediate Bluetooth/headphone device removal detection
    try {
      val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
          if (removedDevices == null || !isPlaying) return
          val disconnectedHeadphones = removedDevices.any { device ->
            device.isSink && MusicPlayerEngine.isHeadphoneOrBluetoothAudioDeviceType(device.type)
          }
          if (disconnectedHeadphones) {
            onBluetoothHeadphonesDisconnected()
          }
        }
      }
      audioManager?.registerAudioDeviceCallback(callback, null)
      audioDeviceCallback = callback
    } catch (_: Throwable) {}

    // 3. Register Media Control BroadcastReceiver for notification action PendingIntents
    try {
      val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          handleMediaControlAction(intent?.action)
        }
      }
      val controlFilter = IntentFilter().apply {
        addAction(MusicPlayerEngine.ACTION_MEDIA_PLAY)
        addAction(MusicPlayerEngine.ACTION_MEDIA_PAUSE)
        addAction(MusicPlayerEngine.ACTION_MEDIA_PLAY_PAUSE)
        addAction(MusicPlayerEngine.ACTION_MEDIA_NEXT)
        addAction(MusicPlayerEngine.ACTION_MEDIA_PREVIOUS)
        addAction(MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE)
        addAction(MusicPlayerEngine.ACTION_MEDIA_CYCLE_REPEAT)
        addAction(MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE)
        addAction(MusicPlayerEngine.ACTION_MEDIA_STOP)
      }
      ContextCompat.registerReceiver(
        appContext,
        controlReceiver,
        controlFilter,
        ContextCompat.RECEIVER_NOT_EXPORTED,
      )
      mediaControlReceiver = controlReceiver
    } catch (_: Throwable) {}

    // 4. Create Android platform MediaSession for system media player controls (Lock screen, Quick Settings, Bluetooth AVRCP)
    try {
      val session = MediaSession(appContext, "ToolboxMusicPlayerSession")
      val launchIntent = MusicPlayerEngine.createSystemMediaLaunchIntent(appContext)
      val sessionActivityPi = PendingIntent.getActivity(
        appContext,
        MusicPlayerEngine.MEDIA_NOTIFICATION_ID,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
      session.setSessionActivity(sessionActivityPi)
      session.setCallback(
        object : MediaSession.Callback() {
          override fun onPlay() {
            handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_PLAY)
          }

          override fun onPause() {
            handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_PAUSE)
          }

          override fun onStop() {
            handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_STOP)
          }

          override fun onSkipToNext() {
            handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_NEXT)
          }

          override fun onSkipToPrevious() {
            handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_PREVIOUS)
          }

          override fun onSeekTo(pos: Long) {
            seekToPositionMs(pos)
          }

          override fun onSetPlaybackSpeed(speed: Float) {
            setPlaybackSpeedValue(speed)
          }

          override fun onCustomAction(action: String, extras: Bundle?) {
            handleMediaControlAction(action)
          }

          override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
            val keyEvent: KeyEvent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            } else {
              @Suppress("DEPRECATION")
              mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            }
            if (keyEvent != null && handleMediaKeyEvent(keyEvent.keyCode, keyEvent.action)) {
              return true
            }
            return super.onMediaButtonEvent(mediaButtonIntent)
          }
        }
      )
      mediaSession = session
      ensureMediaNotificationChannel()
    } catch (_: Throwable) {}
  }

  private fun ensureMediaNotificationChannel() {
    val ctx = appContext ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (nm.getNotificationChannel(MusicPlayerEngine.MEDIA_NOTIFICATION_CHANNEL_ID) == null) {
          val channel = NotificationChannel(
            MusicPlayerEngine.MEDIA_NOTIFICATION_CHANNEL_ID,
            MusicPlayerEngine.MEDIA_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW,
          ).apply {
            description = "System media player controls for the Toolbox Music Player"
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
          }
          nm.createNotificationChannel(channel)
        }
      } catch (_: Throwable) {}
    }
  }

  internal fun syncSystemMediaControls(
    forceMetadataUpdate: Boolean = false,
    notifyPlatformInBackground: Boolean = false,
  ) {
    val track = currentTrack
    // Strictly reflects the individual song's favorite state (not artist or album favorite status)
    val isFav = track != null && track.id in favoriteIds
    val shuffleIconRes = MusicPlayerEngine.resolveSystemMediaShuffleIconRes(shuffleMode)
    val repeatIconRes = MusicPlayerEngine.resolveSystemMediaRepeatIconRes(repeatMode)
    val favIconRes = MusicPlayerEngine.resolveSystemMediaFavoriteIconRes(isFav)
    val cachedArt = if (track != null && !isInspection) {
      MusicPlayerEngine.getCachedAlbumArtBitmap(track, 360)
    } else {
      null
    }

    val snapshotPositionMs = currentPositionMs
    val snapshotDurationMs = trackDurationMs
    val snapshotIsPlaying = isPlaying
    val snapshotSpeed = playbackSpeed
    val snapshotShuffleMode = shuffleMode
    val snapshotRepeatMode = repeatMode

    systemMediaControlState = if (track == null || isReleased) {
      SystemMediaControlState(isActive = false)
    } else {
      SystemMediaControlState(
        isActive = true,
        trackId = track.id,
        title = track.title,
        artist = track.artist,
        album = track.album,
        genre = track.genre,
        durationMs = snapshotDurationMs,
        positionMs = snapshotPositionMs,
        isPlaying = snapshotIsPlaying,
        playbackSpeed = snapshotSpeed,
        isFavorite = isFav,
        shuffleMode = snapshotShuffleMode,
        repeatMode = snapshotRepeatMode,
        shuffleIconRes = shuffleIconRes,
        repeatIconRes = repeatIconRes,
        favoriteIconRes = favIconRes,
        customActionIds = listOf(
          MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE,
          MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE,
        ),
        hasArtworkBitmap = cachedArt != null,
        actions = MusicPlayerEngine.SYSTEM_MEDIA_PLAYBACK_ACTIONS,
      )
    }

    if (isInspection || appContext == null) return
    val session = mediaSession ?: return

    var shouldRefreshWidgets = false
    val applyPlatformSync = {
      if (track == null || isReleased) {
        shouldRefreshWidgets = lastNotifiedTrackId != null || lastSyncedMediaMetadataTrackId != null
        try {
          session.isActive = false
          val stopState = PlaybackState.Builder()
            .setState(PlaybackState.STATE_STOPPED, 0L, 0f)
            .setActions(MusicPlayerEngine.SYSTEM_MEDIA_PLAYBACK_ACTIONS)
            .build()
          session.setPlaybackState(stopState)
        } catch (_: Throwable) {}
        try {
          val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
          nm?.cancel(MusicPlayerEngine.MEDIA_NOTIFICATION_ID)
        } catch (_: Throwable) {}
        lastSyncedMediaMetadataTrackId = null
        lastSyncedMediaMetadataDurationMs = -1L
        lastSyncedMediaArtworkBitmap = null
        lastNotifiedTrackId = null
        lastNotifiedIsPlaying = null
        lastNotifiedShuffleIconRes = 0
        lastNotifiedRepeatIconRes = 0
        lastNotifiedFavoriteIconRes = 0
        lastNotifiedArtworkBitmap = null
        lastNotifiedColorRgb = 0
      } else {
        // 1. Update PlaybackState (isolated try/catch so metadata/notification errors never block transport state):
        //    - CustomAction 0 (left of playback controls): Shuffle (cycles OFF -> SHUFFLE -> SMART_SHUFFLE)
        //    - Transport controls: Previous / Play-Pause / Next
        //    - CustomAction 1 (right of playback controls): Star / Favorite for the individual song (reflects track.id in favoriteIds)
        try {
          session.isActive = true
          val stateInt = if (snapshotIsPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
          val effectiveSpeed = if (snapshotIsPlaying) snapshotSpeed else 0f
          val shuffleLabel = MusicPlayerEngine.resolveSystemMediaShuffleLabel(snapshotShuffleMode)
          val favLabel = MusicPlayerEngine.resolveSystemMediaFavoriteLabel(isFav)
          val playbackState = PlaybackState.Builder()
            .setState(stateInt, snapshotPositionMs.coerceAtLeast(0L), effectiveSpeed)
            .setActions(MusicPlayerEngine.SYSTEM_MEDIA_PLAYBACK_ACTIONS)
            .addCustomAction(
              PlaybackState.CustomAction.Builder(
                MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE,
                shuffleLabel,
                shuffleIconRes,
              ).build()
            )
            .addCustomAction(
              PlaybackState.CustomAction.Builder(
                MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE,
                favLabel,
                favIconRes,
              ).build()
            )
            .build()
          session.setPlaybackState(playbackState)
        } catch (_: Throwable) {}

        // 2. Load artwork asynchronously if not yet in memory
        if (cachedArt == null && mediaArtworkLoadJob?.isActive != true) {
          val targetId = track.id
          mediaArtworkLoadJob = scope.launch {
            try {
              val loadedArt = MusicPlayerEngine.loadAlbumArtBitmapAsync(track, sizePx = 360)
              if (!isReleased && currentTrack?.id == targetId && loadedArt != lastSyncedMediaArtworkBitmap) {
                syncSystemMediaControls(forceMetadataUpdate = true)
              }
            } catch (_: kotlinx.coroutines.CancellationException) {
              throw kotlinx.coroutines.CancellationException()
            } catch (_: Throwable) {}
          }
        }

        val safeMediaArt = MusicPlayerEngine.prepareSystemMediaArtworkBitmap(cachedArt, maxDimPx = 240)

        // 3. Update MediaMetadata when track, duration, or artwork changes (bounded below Binder IPC limits)
        val metadataChanged = forceMetadataUpdate ||
          lastSyncedMediaMetadataTrackId != track.id ||
          lastSyncedMediaMetadataDurationMs != snapshotDurationMs ||
          lastSyncedMediaArtworkBitmap !== cachedArt
        if (metadataChanged) {
          lastSyncedMediaMetadataTrackId = track.id
          lastSyncedMediaMetadataDurationMs = snapshotDurationMs
          lastSyncedMediaArtworkBitmap = cachedArt

          fun buildBaseMetadataBuilder(): MediaMetadata.Builder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, track.id)
            .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
            .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, track.artist)
            .putString(MediaMetadata.METADATA_KEY_GENRE, track.genre)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, snapshotDurationMs)
            .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, track.trackNumber.toLong())
            .putLong(MediaMetadata.METADATA_KEY_YEAR, track.year.toLong())

          try {
            val metaBuilder = buildBaseMetadataBuilder()
            if (safeMediaArt != null) {
              metaBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, safeMediaArt)
              metaBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, safeMediaArt)
            }
            session.setMetadata(metaBuilder.build())
          } catch (_: Throwable) {
            // Fallback without duplicate bitmap keys if OEM Binder buffer is tight
            try {
              val fallbackBuilder = buildBaseMetadataBuilder()
              val compactArt = MusicPlayerEngine.prepareSystemMediaArtworkBitmap(cachedArt, maxDimPx = 144)
              if (compactArt != null) {
                fallbackBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, compactArt)
              }
              session.setMetadata(fallbackBuilder.build())
            } catch (_: Throwable) {
              try {
                session.setMetadata(buildBaseMetadataBuilder().build())
              } catch (_: Throwable) {}
            }
          }
        }

        // 4. Only post/update the MediaStyle Notification when notification-visible fields change
        val notifPalette = MusicPlayerEngine.resolveTrackPalette(track)
        val notifColorRgb = ((notifPalette.dominantColor.red * 255f).roundToInt().coerceIn(0, 255) shl 16) or
          ((notifPalette.dominantColor.green * 255f).roundToInt().coerceIn(0, 255) shl 8) or
          (notifPalette.dominantColor.blue * 255f).roundToInt().coerceIn(0, 255)

        val notificationNeedsUpdate = forceMetadataUpdate ||
          lastNotifiedTrackId != track.id ||
          lastNotifiedIsPlaying != snapshotIsPlaying ||
          lastNotifiedShuffleIconRes != shuffleIconRes ||
          lastNotifiedFavoriteIconRes != favIconRes ||
          lastNotifiedRepeatIconRes != repeatIconRes ||
          lastNotifiedArtworkBitmap !== cachedArt ||
          lastNotifiedColorRgb != notifColorRgb
        if (notificationNeedsUpdate) {
          shouldRefreshWidgets = true
          lastNotifiedTrackId = track.id
          lastNotifiedIsPlaying = snapshotIsPlaying
          lastNotifiedShuffleIconRes = shuffleIconRes
          lastNotifiedFavoriteIconRes = favIconRes
          lastNotifiedRepeatIconRes = repeatIconRes
          lastNotifiedArtworkBitmap = cachedArt
          lastNotifiedColorRgb = notifColorRgb
          try {
            postSystemMediaNotification(
              track = track,
              isPlayingSnapshot = snapshotIsPlaying,
              shuffleModeSnapshot = snapshotShuffleMode,
              isFavorite = isFav,
              shuffleIconRes = shuffleIconRes,
              favIconRes = favIconRes,
              notifColorRgb = notifColorRgb,
              artworkBitmap = safeMediaArt,
              session = session,
            )
          } catch (_: Throwable) {
            try {
              postSystemMediaNotification(
                track = track,
                isPlayingSnapshot = snapshotIsPlaying,
                shuffleModeSnapshot = snapshotShuffleMode,
                isFavorite = isFav,
                shuffleIconRes = shuffleIconRes,
                favIconRes = favIconRes,
                notifColorRgb = notifColorRgb,
                artworkBitmap = null,
                session = session,
              )
            } catch (_: Throwable) {}
          }
        }
      }
    }

    if (notifyPlatformInBackground) {
      mediaControlSyncJob?.cancel()
      mediaControlSyncJob = scope.launch(ioCoroutineContext) {
        applyPlatformSync()
        if (shouldRefreshWidgets) {
          MusicPlayerEngine.refreshAllWidgetsForMediaStateChange(appContext)
        }
      }
    } else {
      applyPlatformSync()
      if (shouldRefreshWidgets) {
        MusicPlayerEngine.refreshAllWidgetsForMediaStateChange(appContext)
      }
    }
  }

  private fun buildMediaActionPendingIntent(action: String, requestCode: Int): PendingIntent {
    val intent = Intent(action).apply {
      setPackage(appContext?.packageName)
    }
    return PendingIntent.getBroadcast(
      appContext,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun postSystemMediaNotification(
    track: MusicTrack,
    isPlayingSnapshot: Boolean,
    shuffleModeSnapshot: MusicShuffleMode,
    isFavorite: Boolean,
    shuffleIconRes: Int,
    favIconRes: Int,
    notifColorRgb: Int,
    artworkBitmap: Bitmap?,
    session: MediaSession,
  ) {
    val ctx = appContext ?: return
    ensureMediaNotificationChannel()
    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

    val launchIntent = MusicPlayerEngine.createSystemMediaLaunchIntent(ctx)
    val contentPi = PendingIntent.getActivity(
      ctx,
      MusicPlayerEngine.MEDIA_NOTIFICATION_ID,
      launchIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val shuffleAction = Notification.Action.Builder(
      Icon.createWithResource(ctx, shuffleIconRes),
      MusicPlayerEngine.resolveSystemMediaShuffleLabel(shuffleModeSnapshot),
      buildMediaActionPendingIntent(MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE, 100),
    ).build()

    val prevAction = Notification.Action.Builder(
      Icon.createWithResource(ctx, R.drawable.ic_media_skip_previous),
      "Previous",
      buildMediaActionPendingIntent(MusicPlayerEngine.ACTION_MEDIA_PREVIOUS, 101),
    ).build()

    val playPauseIcon = if (isPlayingSnapshot) {
      R.drawable.ic_media_pause
    } else {
      R.drawable.ic_media_play
    }
    val playPauseTitle = if (isPlayingSnapshot) "Pause" else "Play"
    val playPauseAction = Notification.Action.Builder(
      Icon.createWithResource(ctx, playPauseIcon),
      playPauseTitle,
      buildMediaActionPendingIntent(MusicPlayerEngine.ACTION_MEDIA_PLAY_PAUSE, 102),
    ).build()

    val nextAction = Notification.Action.Builder(
      Icon.createWithResource(ctx, R.drawable.ic_media_skip_next),
      "Next",
      buildMediaActionPendingIntent(MusicPlayerEngine.ACTION_MEDIA_NEXT, 103),
    ).build()

    val favoriteAction = Notification.Action.Builder(
      Icon.createWithResource(ctx, favIconRes),
      MusicPlayerEngine.resolveSystemMediaFavoriteLabel(isFavorite),
      buildMediaActionPendingIntent(MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE, 104),
    ).build()

    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Notification.Builder(ctx, MusicPlayerEngine.MEDIA_NOTIFICATION_CHANNEL_ID)
    } else {
      @Suppress("DEPRECATION")
      Notification.Builder(ctx)
    }

    val mediaStyle = Notification.MediaStyle()
      .setMediaSession(session.sessionToken)
      .setShowActionsInCompactView(1, 2, 3)

    builder
      .setSmallIcon(R.drawable.ic_tool_music_player_foreground)
      .setContentTitle(track.title)
      .setContentText("${track.artist} • ${track.album}")
      .setSubText(track.genre)
      .setContentIntent(contentPi)
      .setDeleteIntent(buildMediaActionPendingIntent(MusicPlayerEngine.ACTION_MEDIA_STOP, 105))
      .setVisibility(Notification.VISIBILITY_PUBLIC)
      .setCategory(Notification.CATEGORY_TRANSPORT)
      .setOngoing(isPlayingSnapshot)
      .setOnlyAlertOnce(true)
      .setColor((0xFF000000 or (notifColorRgb.toLong() and 0xFFFFFFL)).toInt())
      .setStyle(mediaStyle)
      .addAction(shuffleAction)
      .addAction(prevAction)
      .addAction(playPauseAction)
      .addAction(nextAction)
      .addAction(favoriteAction)

    if (artworkBitmap != null) {
      builder.setLargeIcon(artworkBitmap)
    }

    nm.notify(MusicPlayerEngine.MEDIA_NOTIFICATION_ID, builder.build())
  }

  private fun releaseSystemAudioAndMediaControls() {
    abandonSystemAudioFocus()
    val ctx = appContext
    if (ctx != null) {
      audioDisconnectReceiver?.let { receiver ->
        try {
          ctx.unregisterReceiver(receiver)
        } catch (_: Throwable) {}
      }
      mediaControlReceiver?.let { receiver ->
        try {
          ctx.unregisterReceiver(receiver)
        } catch (_: Throwable) {}
      }
      audioDeviceCallback?.let { cb ->
        try {
          audioManager?.unregisterAudioDeviceCallback(cb)
        } catch (_: Throwable) {}
      }
      try {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(MusicPlayerEngine.MEDIA_NOTIFICATION_ID)
      } catch (_: Throwable) {}
    }
    audioDisconnectReceiver = null
    mediaControlReceiver = null
    audioDeviceCallback = null
    mediaSession?.let { session ->
      try {
        session.isActive = false
        session.release()
      } catch (_: Throwable) {}
    }
    mediaSession = null
    systemMediaControlState = SystemMediaControlState(isActive = false)
  }

  fun cycleSleepTimer() {
    selectSleepTimer(sleepTimerPreset.next())
  }

  fun selectSleepTimer(preset: SleepTimerPreset) {
    sleepTimerPreset = preset
    sleepRemainingMs = preset.durationMs
    sleepTimerJob?.cancel()
    if (preset != SleepTimerPreset.OFF && sleepRemainingMs > 0L) {
      sleepTimerJob = scope.launch {
        while (isActive && !isReleased && sleepTimerPreset != SleepTimerPreset.OFF && sleepRemainingMs > 0L) {
          delay(1000L)
          sleepRemainingMs = (sleepRemainingMs - 1000L).coerceAtLeast(0L)
          if (sleepRemainingMs == 0L) {
            setPlayingState(false)
            sleepTimerPreset = SleepTimerPreset.OFF
          }
        }
      }
    }
  }

  /**
   * Releases all resources (MediaPlayer and background coroutines) when the host Activity is destroyed.
   */
  fun release() {
    if (isReleased) return
    isReleased = true
    if (MusicPlayerEngine.activeSessionState === this) {
      MusicPlayerEngine.activeSessionState = null
    }
    flushPlayStats()
    progressJob?.cancel()
    sleepTimerJob?.cancel()
    libraryLoadJob?.cancel()
    paletteLoadJob?.cancel()
    mediaArtworkLoadJob?.cancel()
    favoritesRecalcJob?.cancel()
    statsAndSortRecalcJob?.cancel()
    libraryRebuildJob?.cancel()
    prefsPersistJob?.cancel()
    playStatsPersistJob?.cancel()
    mediaControlSyncJob?.cancel()
    scope.cancel()
    releaseMediaPlayer()
    releaseSystemAudioAndMediaControls()
  }
}

/**
 * Snapshot of the Android system media player controls state published by [MusicPlaybackSession]
 * via [MediaSession] and [Notification.MediaStyle].
 */
data class SystemMediaControlState(
  val isActive: Boolean = false,
  val trackId: String? = null,
  val title: String = "",
  val artist: String = "",
  val album: String = "",
  val genre: String = "",
  val durationMs: Long = 0L,
  val positionMs: Long = 0L,
  val isPlaying: Boolean = false,
  val playbackSpeed: Float = 1.0f,
  val isFavorite: Boolean = false,
  val shuffleMode: MusicShuffleMode = MusicShuffleMode.OFF,
  val repeatMode: MusicRepeatMode = MusicRepeatMode.OFF,
  val shuffleIconRes: Int = R.drawable.ic_media_shuffle_off,
  val repeatIconRes: Int = R.drawable.ic_media_repeat_off,
  val favoriteIconRes: Int = R.drawable.ic_media_favorite_off,
  val customActionIds: List<String> = emptyList(),
  val hasArtworkBitmap: Boolean = false,
  val actions: Long = 0L,
)

/**
 * An extra action button exposed by an external Android media player session
 * (via [PlaybackState.CustomAction] on its [MediaSession] or via non-transport
 * [Notification.Action] items on its media notification).
 */
data class SystemMediaCustomAction(
  val action: String,
  val label: String,
  val iconResId: Int = 0,
  val iconBitmap: Bitmap? = null,
  val extras: Bundle? = null,
  val pendingIntent: PendingIntent? = null,
)

/**
 * Metadata and extracted [AlbumArtPalette] for whichever media session is currently active in the
 * Android system media player (either the built-in Toolbox Music Player or an external media app).
 */
data class SystemMediaPlayingInfo(
  val title: String,
  val artist: String = "",
  val album: String = "",
  val genre: String = "",
  val sourcePackage: String = "",
  val sourceAppName: String = "",
  val isExternalApp: Boolean = false,
  val isPlaying: Boolean = false,
  val palette: AlbumArtPalette,
  val artworkBitmap: Bitmap? = null,
  val durationMs: Long = 0L,
  val positionMs: Long = 0L,
  val positionUpdateElapsedRealtimeMs: Long = 0L,
  val playbackSpeed: Float = 1.0f,
  val canSeek: Boolean = false,
  val canSkipNext: Boolean = true,
  val canSkipPrevious: Boolean = true,
  val customActions: List<SystemMediaCustomAction> = emptyList(),
)

/**
 * BroadcastReceiver that routes system media notification and Home Screen AppWidget transport
 * actions to the active [MusicPlaybackSession] (or external Android system media session).
 */
class MusicMediaControlReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    val action = intent?.action ?: return
    MusicPlayerWidgets.handleWidgetAction(context, action)
  }
}

/**
 * NotificationListenerService that observes active [MediaSession] controllers and media
 * notifications across the Android system media player so the app's dynamic media theme colors can
 * follow external media players (e.g. Spotify, YouTube Music, Pocket Casts) as well as the
 * built-in Music Player.
 */
class ToolboxMediaListenerService : NotificationListenerService() {
  companion object {
    @Volatile
    internal var activeInstance: ToolboxMediaListenerService? = null
  }

  private var sessionManager: MediaSessionManager? = null
  private val trackedControllers = mutableMapOf<MediaController, MediaController.Callback>()
  private val serviceHandler by lazy { Handler(Looper.getMainLooper()) }

  private val activeSessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
    bindExternalControllers(controllers)
    MusicPlayerEngine.syncExternalSystemMediaSessions(applicationContext)
  }

  override fun onCreate() {
    super.onCreate()
    activeInstance = this
  }

  override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
    super.onConfigurationChanged(newConfig)
    MusicPlayerEngine.refreshAllWidgetsForMediaStateChange(
      context = applicationContext,
      forceThemeWidgetsRefresh = true,
    )
  }

  override fun onDestroy() {
    if (activeInstance === this) {
      activeInstance = null
    }
    super.onDestroy()
  }

  override fun onListenerConnected() {
    super.onListenerConnected()
    activeInstance = this
    refreshCachedMediaNotifications()
    try {
      val msm = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
      sessionManager = msm
      val component = ComponentName(this, ToolboxMediaListenerService::class.java)
      msm.addOnActiveSessionsChangedListener(activeSessionsListener, component, serviceHandler)
      val initial = msm.getActiveSessions(component)
      bindExternalControllers(initial)
      MusicPlayerEngine.syncExternalSystemMediaSessions(applicationContext)
    } catch (_: Throwable) {
      MusicPlayerEngine.syncExternalSystemMediaSessions(applicationContext)
    }
  }

  override fun onListenerDisconnected() {
    if (activeInstance === this) {
      activeInstance = null
    }
    clearTrackedControllers()
    try {
      sessionManager?.removeOnActiveSessionsChangedListener(activeSessionsListener)
    } catch (_: Throwable) {}
    sessionManager = null
    super.onListenerDisconnected()
  }

  internal fun refreshCachedMediaNotifications() {
    try {
      val list = activeNotifications ?: return
      val seenPackages = mutableSetOf<String>()
      for (sbn in list) {
        val pkg = sbn?.packageName ?: continue
        if (pkg == packageName) continue
        val parsed = parseMediaStatusBarNotification(sbn) ?: continue
        seenPackages.add(pkg)
        MusicPlayerEngine.activeMediaNotifications[pkg] = parsed
      }
      val toRemove = MusicPlayerEngine.activeMediaNotifications.keys.filter { it !in seenPackages }
      for (pkg in toRemove) {
        MusicPlayerEngine.activeMediaNotifications.remove(pkg)
      }
    } catch (_: Throwable) {}
  }

  private fun parseMediaStatusBarNotification(
    sbn: StatusBarNotification,
  ): MusicPlayerEngine.CachedExternalMediaNotification? {
    return try {
      val notification = sbn.notification ?: return null
      val pkg = sbn.packageName ?: return null
      if (pkg == packageName) return null
      if (MusicPlayerEngine.isIgnoredPhoneOrCallNotification(applicationContext, pkg, notification)) {
        return null
      }
      val extras = notification.extras ?: return null
      val template = try { extras.getString(Notification.EXTRA_TEMPLATE).orEmpty() } catch (_: Throwable) { "" }
      val hasMediaSession = try { extras.containsKey(Notification.EXTRA_MEDIA_SESSION) } catch (_: Throwable) { false }
      val isMediaNotification = notification.category == Notification.CATEGORY_TRANSPORT ||
        hasMediaSession ||
        template.contains("Media", ignoreCase = true)
      if (!isMediaNotification) return null

      val title = try {
        (extras.getCharSequence(Notification.EXTRA_TITLE)
          ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG))?.toString().orEmpty()
      } catch (_: Throwable) { "" }
      val artist = try {
        extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
      } catch (_: Throwable) { "" }
      val album = try {
        (extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
          ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))?.toString().orEmpty()
      } catch (_: Throwable) { "" }
      val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
      val colorRgb = notification.color.takeIf { it != 0 }
      val postedAtMs = sbn.postTime.takeIf { it > 0L } ?: System.currentTimeMillis()
      val sessionToken = MusicPlayerEngine.extractNotificationMediaSessionToken(notification)
      val existingCached = MusicPlayerEngine.activeMediaNotifications[pkg]
      val sameNotificationMetadata = existingCached != null &&
        existingCached.title == title &&
        existingCached.artist == artist &&
        existingCached.album == album &&
        existingCached.isOngoing == isOngoing &&
        existingCached.colorRgb == colorRgb &&
        existingCached.sessionToken == sessionToken
      if (sameNotificationMetadata && existingCached.postedAtMs == postedAtMs) {
        return existingCached
      }
      val rawArtworkBitmap = MusicPlayerEngine.extractNotificationArtworkBitmap(applicationContext, notification)
      val artworkBitmap = if (
        sameNotificationMetadata &&
        MusicPlayerEngine.areBitmapsVisuallyEquivalent(rawArtworkBitmap, existingCached.artworkBitmap)
      ) {
        existingCached.artworkBitmap
      } else {
        rawArtworkBitmap
      }
      val rawCustomActions = MusicPlayerEngine.extractNotificationCustomActions(applicationContext, notification)
      val customActions = if (
        sameNotificationMetadata &&
        rawCustomActions.size == existingCached.customActions.size &&
        rawCustomActions.indices.all { idx ->
          val a = rawCustomActions[idx]
          val b = existingCached.customActions[idx]
          a.action == b.action &&
            a.label == b.label &&
            a.iconResId == b.iconResId &&
            MusicPlayerEngine.areBitmapsVisuallyEquivalent(a.iconBitmap, b.iconBitmap)
        }
      ) {
        existingCached.customActions
      } else {
        rawCustomActions
      }
      if (
        sameNotificationMetadata &&
        artworkBitmap === existingCached.artworkBitmap &&
        customActions === existingCached.customActions
      ) {
        return existingCached
      }

      MusicPlayerEngine.CachedExternalMediaNotification(
        packageName = pkg,
        title = title,
        artist = artist,
        album = album,
        isOngoing = isOngoing,
        colorRgb = colorRgb,
        artworkBitmap = artworkBitmap,
        sessionToken = sessionToken,
        contentIntent = notification.contentIntent,
        customActions = customActions,
        postedAtMs = postedAtMs,
      )
    } catch (_: Throwable) {
      null
    }
  }

  override fun onNotificationPosted(sbn: StatusBarNotification?) {
    super.onNotificationPosted(sbn)
    if (sbn == null) return
    try {
      val parsed = parseMediaStatusBarNotification(sbn) ?: return
      MusicPlayerEngine.activeMediaNotifications[parsed.packageName] = parsed

      try {
        val msm = sessionManager ?: (getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager)
        if (msm != null) {
          val component = ComponentName(this, ToolboxMediaListenerService::class.java)
          val controllers = msm.getActiveSessions(component)
          bindExternalControllers(controllers)
        }
      } catch (_: Throwable) {}

      MusicPlayerEngine.syncExternalSystemMediaSessions(applicationContext)
    } catch (_: Throwable) {}
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    super.onNotificationRemoved(sbn)
    try {
      val pkg = sbn?.packageName ?: return
      if (pkg == packageName) return
      val removed = MusicPlayerEngine.activeMediaNotifications.remove(pkg)
      if (removed != null && !MusicPlayerEngine.isIgnoredPhoneOrCallPackage(applicationContext, pkg)) {
        MusicPlayerEngine.syncExternalSystemMediaSessions(applicationContext)
      }
    } catch (_: Throwable) {}
  }

  private fun bindExternalControllers(controllers: List<MediaController>?) {
    val validControllers = controllers.orEmpty().filter {
      try {
        it.packageName != packageName &&
          !MusicPlayerEngine.isIgnoredPhoneOrCallMediaController(applicationContext, it)
      } catch (_: Throwable) {
        false
      }
    }
    val currentTokens = trackedControllers.keys.mapNotNull {
      try { it.sessionToken } catch (_: Throwable) { null }
    }.toSet()
    val desiredTokens = validControllers.mapNotNull {
      try { it.sessionToken } catch (_: Throwable) { null }
    }.toSet()
    if (currentTokens == desiredTokens && trackedControllers.size == validControllers.size) {
      return
    }
    clearTrackedControllers()
    for (controller in validControllers) {
      val callback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
          MusicPlayerEngine.syncExternalSystemMediaSessions(applicationContext)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
          MusicPlayerEngine.syncExternalSystemMediaSessions(applicationContext)
        }

        override fun onSessionDestroyed() {
          MusicPlayerEngine.syncExternalSystemMediaSessions(applicationContext)
        }
      }
      try {
        controller.registerCallback(callback, serviceHandler)
        trackedControllers[controller] = callback
      } catch (_: Throwable) {}
    }
  }

  private fun clearTrackedControllers() {
    for ((ctrl, cb) in trackedControllers) {
      try {
        ctrl.unregisterCallback(cb)
      } catch (_: Throwable) {}
    }
    trackedControllers.clear()
  }
}

