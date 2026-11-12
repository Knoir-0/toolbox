package com.example.toolbox.ui.tools

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
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

const val MUSIC_PLAYER_PREFS_NAME = "music_player_preferences"
const val PREF_KEY_MUSIC_VIEW_MODE = "music_player_view_mode"
const val PREF_KEY_MUSIC_TAB = "music_player_selected_tab"
const val PREF_KEY_MUSIC_SORT_ORDER = "music_player_sort_order"
const val PREF_KEY_MUSIC_SHUFFLE = "music_player_shuffle_enabled"
const val PREF_KEY_MUSIC_REPEAT = "music_player_repeat_mode"
const val PREF_KEY_MUSIC_FAVORITES = "music_player_favorite_ids"
const val PREF_KEY_MUSIC_PLAYLISTS = "music_player_playlists"
const val PREF_KEY_MUSIC_PLAY_STATS = "music_player_play_stats"
const val PREF_KEY_MUSIC_INCLUDE_SAMPLES = "music_player_include_samples"

val DEFAULT_MUSIC_FOLDER_NAMES: List<String> = listOf("Music", "Audio")

/**
 * Primary library browse tabs in the Music Player tool.
 */
enum class MusicLibraryTab(
  val displayName: String,
) {
  ARTISTS("Artists"),
  ALBUMS("Albums"),
  SONGS("Songs"),
  GENRES("Genres"),
  PLAYLISTS("Playlists"),
  ;

  companion object {
    val DEFAULT: MusicLibraryTab = ARTISTS

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
  MOST_PLAYED("Most Played"),
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
 * Sleep timer presets for automatic playback pause.
 */
enum class SleepTimerPreset(
  val label: String,
  val durationMs: Long,
) {
  OFF("Off", 0L),
  FIVE_MIN("5 min", 5 * 60_000L),
  FIFTEEN_MIN("15 min", 15 * 60_000L),
  THIRTY_MIN("30 min", 30 * 60_000L),
  SIXTY_MIN("60 min", 60 * 60_000L),
}

/**
 * Color palette extracted from a track or album's artwork [Bitmap] so the Music Player
 * background, mini-player bar, and controls dynamically follow the album art.
 */
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
  val durationFormatted: String
    get() = AudioRecorderEngine.formatDurationMs(durationMs)

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
data class MusicAlbum(
  val name: String,
  val artist: String,
  val year: Int,
  val genre: String,
  val tracks: List<MusicTrack>,
) {
  val songCount: Int get() = tracks.size
  val totalDurationMs: Long get() = tracks.sumOf { it.durationMs }
  val totalDurationFormatted: String get() = AudioRecorderEngine.formatDurationMs(totalDurationMs)
  val representativeTrack: MusicTrack? get() = tracks.firstOrNull()
  val summaryText: String
    get() {
      val countLabel = if (songCount == 1) "1 song" else "$songCount songs"
      return "$artist • $countLabel"
    }
}

/**
 * Represents an Artist aggregated from tracks and albums in the library.
 */
data class MusicArtist(
  val name: String,
  val albums: List<MusicAlbum>,
  val tracks: List<MusicTrack>,
) {
  val albumCount: Int get() = albums.size
  val songCount: Int get() = tracks.size
  val totalDurationMs: Long get() = tracks.sumOf { it.durationMs }
  val totalDurationFormatted: String get() = AudioRecorderEngine.formatDurationMs(totalDurationMs)
  val representativeTrack: MusicTrack? get() = tracks.firstOrNull()
  val genres: List<String> get() = tracks.map { it.genre }.distinct()
  val summaryText: String
    get() {
      val albumLabel = if (albumCount == 1) "1 album" else "$albumCount albums"
      val songLabel = if (songCount == 1) "1 song" else "$songCount songs"
      return "$albumLabel • $songLabel"
    }
}

/**
 * Represents a musical Genre aggregated from tracks in the library.
 */
data class MusicGenre(
  val name: String,
  val tracks: List<MusicTrack>,
  val albums: List<MusicAlbum>,
  val artists: List<String>,
) {
  val songCount: Int get() = tracks.size
  val albumCount: Int get() = albums.size
  val artistCount: Int get() = artists.size
  val totalDurationMs: Long get() = tracks.sumOf { it.durationMs }
  val totalDurationFormatted: String get() = AudioRecorderEngine.formatDurationMs(totalDurationMs)
  val representativeTrack: MusicTrack? get() = tracks.firstOrNull()
  val summaryText: String
    get() {
      val songLabel = if (songCount == 1) "1 song" else "$songCount songs"
      val artistLabel = if (artistCount == 1) "1 artist" else "$artistCount artists"
      return "$songLabel • $artistLabel"
    }
}

/**
 * Persisted playback statistics for a single track.
 */
data class TrackPlayStats(
  val playCount: Int = 0,
  val totalPlayTimeMs: Long = 0L,
  val lastPlayedAtMs: Long = 0L,
) {
  /**
   * Combined engagement score blending play count and total listen time:
   * each play contributes 60,000 points (equivalent to 1 minute of listening) plus 1 point per
   * millisecond of actual listen time.
   */
  val engagementScore: Long
    get() = playCount.toLong() * 60_000L + totalPlayTimeMs

  val hasBeenPlayed: Boolean
    get() = playCount > 0 || totalPlayTimeMs > 0L

  val totalPlayTimeFormatted: String
    get() = AudioRecorderEngine.formatDurationMs(totalPlayTimeMs)
}

/**
 * Persisted metadata entry for a user-saved Playlist.
 */
data class SavedPlaylistEntry(
  val id: String,
  val name: String,
  val trackIds: List<String>,
  val createdAtMs: Long = 0L,
)

/**
 * Represents a resolved Playlist of tracks in the Music Player.
 */
data class MusicPlaylist(
  val id: String,
  val name: String,
  val tracks: List<MusicTrack>,
  val createdAtMs: Long = 0L,
) {
  val isFavorites: Boolean get() = id == MusicPlayerEngine.FAVORITES_PLAYLIST_ID
  val isMostPlayed: Boolean get() = id == MusicPlayerEngine.MOST_PLAYED_PLAYLIST_ID
  val isBuiltIn: Boolean get() = isFavorites || isMostPlayed
  val songCount: Int get() = tracks.size
  val totalDurationMs: Long get() = tracks.sumOf { it.durationMs }
  val totalDurationFormatted: String get() = AudioRecorderEngine.formatDurationMs(totalDurationMs)
  val representativeTrack: MusicTrack? get() = tracks.firstOrNull()
  val artists: List<String> get() = tracks.map { it.artist }.distinct()
  val summaryText: String
    get() {
      val songLabel = if (songCount == 1) "1 song" else "$songCount songs"
      return "$songLabel • $totalDurationFormatted"
    }
}

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

  private const val MAX_ARTWORK_CACHE_ENTRIES = 256
  private const val MAX_TRACK_METADATA_CACHE_ENTRIES = 2048
  private const val MAX_NO_ARTWORK_CACHE_ENTRIES = 1024

  private val artworkDispatcher = Dispatchers.IO.limitedParallelism(4)
  private val artworkDecodeLocks = Array(16) { Any() }
  private val artworkCacheLock = Any()
  private val metadataCacheLock = Any()

  @Volatile
  private var artworkDiskCacheDir: File? = null

  private val artworkBitmapCache = object : LinkedHashMap<String, Bitmap>(64, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
      return size > MAX_ARTWORK_CACHE_ENTRIES
    }
  }
  private val paletteCache = object : LinkedHashMap<String, AlbumArtPalette>(128, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AlbumArtPalette>?): Boolean {
      return size > MAX_ARTWORK_CACHE_ENTRIES
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
  private val libraryTracksCache = object : LinkedHashMap<String, List<MusicTrack>>(8, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<MusicTrack>>?): Boolean {
      return size > 8
    }
  }

  /**
   * Configures an optional disk cache directory for persisted album art thumbnails.
   */
  fun configureDiskCache(cacheDir: File?) {
    if (cacheDir == null) {
      artworkDiskCacheDir = null
      return
    }
    val dir = File(cacheDir, "music_player_artwork_cache")
    try {
      if (!dir.exists()) dir.mkdirs()
      artworkDiskCacheDir = dir
    } catch (_: Throwable) {
      artworkDiskCacheDir = null
    }
  }

  /**
   * Clears in-memory and disk album art and palette caches.
   */
  fun clearArtworkCache() {
    synchronized(artworkCacheLock) {
      artworkBitmapCache.clear()
      paletteCache.clear()
      noExternalArtworkCache.clear()
    }
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
      libraryTracksCache.clear()
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

  /**
   * Builds an [AlbumArtPalette] from primary, secondary, and accent RGB integers.
   */
  fun buildPaletteFromRgb(
    primaryRgb: Int,
    secondaryRgb: Int,
    accentRgb: Int,
  ): AlbumArtPalette {
    val domColor = Color(0xFF000000 or (primaryRgb.toLong() and 0xFFFFFFL))
    val secColor = Color(0xFF000000 or (secondaryRgb.toLong() and 0xFFFFFFL))
    val rawAccent = Color(0xFF000000 or (accentRgb.toLong() and 0xFFFFFFL))

    // Ensure accentColor has high visibility against dark player surfaces
    val effectiveAccent = if (rawAccent.luminance() < 0.28f) {
      lerp(rawAccent, Color.White, 0.45f)
    } else {
      rawAccent
    }

    val darkSurface = lerp(domColor, Color(0xFF101018), 0.52f)
    val deepBg = lerp(secColor, Color(0xFF08080E), 0.72f)

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

  /**
   * Extracts an [AlbumArtPalette] by sampling pixel colors across [bitmap].
   */
  fun extractPaletteFromBitmap(bitmap: Bitmap): AlbumArtPalette {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) {
      return buildPaletteFromRgb(0x5E35B1, 0x1E88E5, 0xFF80AB)
    }

    val sampleSteps = 12
    val stepX = max(1, width / sampleSteps)
    val stepY = max(1, height / sampleSteps)

    var topR = 0L
    var topG = 0L
    var topB = 0L
    var topWeight = 0L

    var bottomR = 0L
    var bottomG = 0L
    var bottomB = 0L
    var bottomWeight = 0L

    var bestVibrantRgb = 0x5E35B1
    var bestVibrancyScore = -1f

    for (y in 0 until height step stepY) {
      val isTopHalf = y < height / 2
      for (x in 0 until width step stepX) {
        val pixel = bitmap.getPixel(x, y)
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF

        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val chroma = maxC - minC
        val luma = (r * 299 + g * 587 + b * 114) / 1000
        // Weight colorful, non-black/non-white pixels higher
        val weight = 1L + (chroma / 16).toLong()

        if (isTopHalf) {
          topR += r * weight
          topG += g * weight
          topB += b * weight
          topWeight += weight
        } else {
          bottomR += r * weight
          bottomG += g * weight
          bottomB += b * weight
          bottomWeight += weight
        }

        // Score vibrancy for accent color
        val saturation = if (maxC > 0) chroma.toFloat() / maxC.toFloat() else 0f
        val brightnessBalance = 1f - abs(luma - 165) / 165f
        val vibrancyScore = saturation * 0.7f + brightnessBalance.coerceAtLeast(0f) * 0.3f
        if (vibrancyScore > bestVibrancyScore && luma in 45..245) {
          bestVibrancyScore = vibrancyScore
          bestVibrantRgb = (r shl 16) or (g shl 8) or b
        }
      }
    }

    val domR = if (topWeight > 0) (topR / topWeight).toInt().coerceIn(0, 255) else 0x5E
    val domG = if (topWeight > 0) (topG / topWeight).toInt().coerceIn(0, 255) else 0x35
    val domB = if (topWeight > 0) (topB / topWeight).toInt().coerceIn(0, 255) else 0xB1
    val primaryRgb = (domR shl 16) or (domG shl 8) or domB

    val secR = if (bottomWeight > 0) (bottomR / bottomWeight).toInt().coerceIn(0, 255) else domR
    val secG = if (bottomWeight > 0) (bottomG / bottomWeight).toInt().coerceIn(0, 255) else domG
    val secB = if (bottomWeight > 0) (bottomB / bottomWeight).toInt().coerceIn(0, 255) else domB
    val secondaryRgb = (secR shl 16) or (secG shl 8) or secB

    return buildPaletteFromRgb(
      primaryRgb = primaryRgb,
      secondaryRgb = secondaryRgb,
      accentRgb = bestVibrantRgb,
    )
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
    val isGenericAlbum = normalizedAlbum.isEmpty() ||
      normalizedAlbum == "local recordings" ||
      normalizedAlbum == "unknown album"
    val file = track.file
    return if (isGenericAlbum && file != null) {
      "file:${file.absolutePath}:${file.lastModified()}"
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
    val albumKey = albumPaletteKey(track)
    val key = trackPaletteKey(track)
    synchronized(artworkCacheLock) {
      if (!track.isSampleTrack) {
        paletteCache[albumKey]?.let { return it }
      }
      paletteCache[key]?.let { return it }
    }
    val palette = buildPaletteFromRgb(
      primaryRgb = track.artworkPrimaryRgb,
      secondaryRgb = track.artworkSecondaryRgb,
      accentRgb = track.artworkAccentRgb,
    )
    synchronized(artworkCacheLock) {
      paletteCache[key] = palette
    }
    return palette
  }

  /**
   * Returns a cached album art [Bitmap] for [track] if already rendered/decoded, or `null`.
   * Reuses a cached bitmap of equal or higher resolution for the same album when available.
   */
  fun getCachedAlbumArtBitmap(track: MusicTrack, sizePx: Int = 320): Bitmap? {
    val safeSize = sizePx.coerceIn(64, 1024)
    val identity = albumArtIdentityKey(track)
    val primaryKey = "$identity|$safeSize"
    val legacyKey = "${track.album}|${track.artist}|${track.artworkPrimaryRgb}|$safeSize"
    val file = track.file
    val fileKey = if (file != null) "file:${file.absolutePath}:${file.lastModified()}|$safeSize" else null

    synchronized(artworkCacheLock) {
      artworkBitmapCache[primaryKey]?.let { return it }
      artworkBitmapCache[legacyKey]?.let { return it }
      if (fileKey != null) {
        artworkBitmapCache[fileKey]?.let { return it }
      }
      // Reuse a higher-resolution cached bitmap for the same album if already decoded
      val candidatePrefix = "$identity|"
      for ((k, bmp) in artworkBitmapCache) {
        if (k.startsWith(candidatePrefix) && bmp.width >= safeSize) {
          return bmp
        }
      }
      return null
    }
  }

  private fun cacheAlbumArtBitmap(
    track: MusicTrack,
    sizePx: Int,
    bitmap: Bitmap,
    extractedPalette: AlbumArtPalette? = null,
  ) {
    val safeSize = sizePx.coerceIn(64, 1024)
    val identity = albumArtIdentityKey(track)
    val primaryKey = "$identity|$safeSize"
    val legacyKey = "${track.album}|${track.artist}|${track.artworkPrimaryRgb}|$safeSize"
    val file = track.file
    val fileKey = if (file != null) "file:${file.absolutePath}:${file.lastModified()}|$safeSize" else null

    synchronized(artworkCacheLock) {
      artworkBitmapCache[primaryKey] = bitmap
      artworkBitmapCache[legacyKey] = bitmap
      if (fileKey != null) {
        artworkBitmapCache[fileKey] = bitmap
      }
      if (extractedPalette != null) {
        paletteCache[trackPaletteKey(track)] = extractedPalette
        paletteCache[albumPaletteKey(track)] = extractedPalette
      }
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
      if (width <= 0 || height <= 0) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
      } else {
        var inSampleSize = 1
        if (height > targetSize || width > targetSize) {
          val halfHeight = height / 2
          val halfWidth = width / 2
          while ((halfHeight / inSampleSize) >= targetSize && (halfWidth / inSampleSize) >= targetSize) {
            inSampleSize *= 2
          }
        }
        val decodeOptions = BitmapFactory.Options().apply {
          this.inSampleSize = inSampleSize
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
      }
    } catch (_: Throwable) {
      null
    }
  }

  private fun diskCacheFileFor(identity: String, sizePx: Int): File? {
    val dir = artworkDiskCacheDir ?: return null
    val bucketSize = if (sizePx <= 256) 240 else 360
    val hash = identity.hashCode().toUInt().toString(16)
    val lengthTag = identity.length
    return File(dir, "art_${hash}_${lengthTag}_$bucketSize.png")
  }

  private fun readDiskCachedAlbumArtBitmap(identity: String, sizePx: Int): Bitmap? {
    val cacheFile = diskCacheFileFor(identity, sizePx) ?: return null
    if (!cacheFile.exists() || !cacheFile.isFile || cacheFile.length() <= 0L) return null
    return try {
      BitmapFactory.decodeFile(cacheFile.absolutePath)
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
    getCachedAlbumArtBitmap(track, sizePx)?.let { return@withContext it }
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
    val bmp = generateOrDecodeAlbumArtBitmap(track, sizePx)
    synchronized(artworkCacheLock) {
      paletteCache[albumPaletteKey(track)]?.let { return@withContext it }
      paletteCache[trackPaletteKey(track)]?.let { return@withContext it }
    }
    val extracted = extractPaletteFromBitmap(bmp)
    synchronized(artworkCacheLock) {
      paletteCache[trackPaletteKey(track)] = extracted
      paletteCache[albumPaletteKey(track)] = extracted
    }
    extracted
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
    val extractedPalette = synchronized(artworkCacheLock) {
      paletteCache[albumPaletteKey(track)]
    } ?: return@withContext track
    val (pRgb, sRgb, aRgb) = paletteToRgbTriple(extractedPalette)
    val updated = track.copy(
      artworkPrimaryRgb = pRgb,
      artworkSecondaryRgb = sRgb,
      artworkAccentRgb = aRgb,
    )
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
      if (!seenIdentities.add(identity)) continue
      if (getCachedAlbumArtBitmap(track, sizePx) == null) {
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
    getCachedAlbumArtBitmap(track, sizePx)?.let { return it }

    val safeSize = sizePx.coerceIn(64, 1024)
    val identity = albumArtIdentityKey(track)
    val decodeLock = artworkDecodeLocks[(identity.hashCode() and 0x7FFFFFFF) % artworkDecodeLocks.size]

    synchronized(decodeLock) {
      getCachedAlbumArtBitmap(track, safeSize)?.let { return it }

      val file = track.file
      if (file != null && file.exists() && !track.isSampleTrack) {
        // 0. Check disk cache if configured
        val diskCached = readDiskCachedAlbumArtBitmap(identity, safeSize)
        if (diskCached != null) {
          val extractedPalette = extractPaletteFromBitmap(diskCached)
          cacheAlbumArtBitmap(track, safeSize, diskCached, extractedPalette)
          return diskCached
        }

        val knownNoExternalArt = synchronized(artworkCacheLock) {
          noExternalArtworkCache[identity] == true
        }

        if (!knownNoExternalArt) {
          // 1. Check embedded ID3 / MP4 cover art (downsampled to safeSize)
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
                  cacheAlbumArtBitmap(track, safeSize, decoded, extractedPalette)
                  writeDiskCachedAlbumArtBitmap(identity, safeSize, decoded)
                  return decoded
                }
              }
            } catch (_: Throwable) {
              // Fall through to folder art or procedural cover
            } finally {
              try {
                retriever.release()
              } catch (_: Throwable) {}
            }
          }

          // 2. Check sibling cover.jpg / folder.jpg in parent folder
          val parent = file.parentFile
          if (parent != null && parent.isDirectory) {
            val coverNames = listOf("cover.jpg", "cover.png", "folder.jpg", "folder.png", "album.jpg", "album.png")
            for (coverName in coverNames) {
              val coverFile = File(parent, coverName)
              if (coverFile.exists() && coverFile.isFile) {
                val decoded = GalleryEngine.decodeThumbnailBitmap(coverFile, reqSizePx = safeSize)
                if (decoded != null) {
                  val extractedPalette = extractPaletteFromBitmap(decoded)
                  cacheAlbumArtBitmap(track, safeSize, decoded, extractedPalette)
                  writeDiskCachedAlbumArtBitmap(identity, safeSize, decoded)
                  return decoded
                }
              }
            }
          }

          synchronized(artworkCacheLock) {
            noExternalArtworkCache[identity] = true
          }
        }
      }

      // 3. Render procedural album cover Bitmap
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

      cacheAlbumArtBitmap(track, safeSize, bitmap)
      return bitmap
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
    synchronized(metadataCacheLock) {
      trackMetadataCache["$baseKey:true"]?.let { return it }
      if (!requireInspectedMetadata) {
        trackMetadataCache["$baseKey:false"]?.let { return it }
      }
      return null
    }
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
    synchronized(metadataCacheLock) {
      trackMetadataCache["$cacheKeyBase:true"]?.let { cached ->
        if (deferAlbumArt) return cached
      }
      if (!inspectMetadataRetriever) {
        trackMetadataCache["$cacheKeyBase:false"]?.let { return it }
      }
    }

    val stem = file.nameWithoutExtension.trim()
    val fallbackArtist: String
    val fallbackTitle: String
    if (stem.contains(" - ")) {
      val parts = stem.split(" - ", limit = 2)
      fallbackArtist = parts[0].trim().ifEmpty { "Unknown Artist" }
      fallbackTitle = parts[1].trim().ifEmpty { stem }
    } else {
      fallbackArtist = file.parentFile?.parentFile?.name
        ?.takeIf { it.isNotBlank() && !it.equals("Music", ignoreCase = true) && !it.equals("0", ignoreCase = true) }
        ?: "Local Artist"
      fallbackTitle = stem.replace('_', ' ').ifEmpty { file.name }
    }

    val parentName = file.parentFile?.name?.trim().orEmpty()
    val fallbackAlbum = if (parentName.isNotEmpty() &&
      !parentName.equals("Music", ignoreCase = true) &&
      !parentName.equals("Audio", ignoreCase = true) &&
      !parentName.equals("Download", ignoreCase = true) &&
      !parentName.equals("0", ignoreCase = true)
    ) {
      parentName
    } else {
      "Local Recordings"
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
    var extractedColors: Triple<Int, Int, Int>? = null

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
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            ?.trim()?.takeIf { it.isNotEmpty() }?.let { album = it }
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
                val pal = extractPaletteFromBitmap(bmp)
                extractedColors = paletteToRgbTriple(pal)
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

    val (primaryRgb, secondaryRgb, accentRgb) = extractedColors
      ?: computeDeterministicArtworkColors(album = album, artist = artist, genre = genre)

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

    // Reuse already-cached album palette colors if background artwork extraction already ran for this album
    val cachedAlbumPalette = synchronized(artworkCacheLock) {
      paletteCache[albumPaletteKey(track)]
    }
    val finalTrack = if (cachedAlbumPalette != null && extractedColors == null) {
      val (pRgb, sRgb, aRgb) = paletteToRgbTriple(cachedAlbumPalette)
      track.copy(
        artworkPrimaryRgb = pRgb,
        artworkSecondaryRgb = sRgb,
        artworkAccentRgb = aRgb,
      )
    } else {
      track
    }

    synchronized(metadataCacheLock) {
      trackMetadataCache["$cacheKeyBase:$inspectMetadataRetriever"] = finalTrack
    }
    return finalTrack
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
  ): String {
    val targetDirs = if (initialDirectory != null) {
      listOf(initialDirectory)
    } else {
      DEFAULT_MUSIC_FOLDER_NAMES.map { File(rootDirectory, it) }
    }
    val dirStamp = targetDirs.joinToString("|") { dir ->
      val mod = if (dir.exists()) dir.lastModified() else -1L
      "${dir.absolutePath}@$mod"
    }
    return "$dirStamp|samples=$includeSamples"
  }

  /**
   * Returns the previously loaded library tracks from the in-memory cache if available and
   * top-level music directories have not changed, or `null` if not yet cached.
   */
  fun getCachedLibraryTracks(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    initialDirectory: File? = null,
    includeSamples: Boolean = false,
  ): List<MusicTrack>? {
    val key = libraryCacheKey(rootDirectory, initialDirectory, includeSamples)
    synchronized(metadataCacheLock) {
      return libraryTracksCache[key]
    }
  }

  /**
   * Resolves the full library of tracks from [initialDirectory] (if provided) or the default
   * `/Music` and `/Audio` folders under [rootDirectory], combining or falling back to
   * [DEFAULT_SAMPLE_TRACKS] when [includeSamples] is enabled or when no local audio files exist.
   */
  fun loadLibraryTracks(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    initialDirectory: File? = null,
    includeSamples: Boolean = false,
    inspectMetadataRetriever: Boolean = true,
    deferAlbumArt: Boolean = true,
  ): List<MusicTrack> {
    val targetDirs = if (initialDirectory != null) {
      listOf(initialDirectory)
    } else {
      DEFAULT_MUSIC_FOLDER_NAMES.map { File(rootDirectory, it) }
    }
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
    if (inspectMetadataRetriever) {
      val key = libraryCacheKey(rootDirectory, initialDirectory, includeSamples)
      synchronized(metadataCacheLock) {
        libraryTracksCache[key] = result
      }
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
  ): List<MusicTrack> = withContext(Dispatchers.IO) {
    ensureActive()
    loadLibraryTracks(
      rootDirectory = rootDirectory,
      initialDirectory = initialDirectory,
      includeSamples = includeSamples,
      inspectMetadataRetriever = inspectMetadataRetriever,
      deferAlbumArt = deferAlbumArt,
    )
  }

  /**
   * Aggregates [tracks] into a sorted list of [MusicAlbum] items.
   */
  fun buildAlbums(tracks: List<MusicTrack>): List<MusicAlbum> {
    if (tracks.isEmpty()) return emptyList()
    val grouped = LinkedHashMap<String, MutableList<MusicTrack>>()
    for (track in tracks) {
      val key = "${track.album.trim().lowercase(Locale.ROOT)}|${track.artist.trim().lowercase(Locale.ROOT)}"
      grouped.getOrPut(key) { mutableListOf() }.add(track)
    }
    return grouped.values.map { albumTracks ->
      val sortedTracks = albumTracks.sortedWith(
        compareBy<MusicTrack> { it.trackNumber }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
      )
      val first = sortedTracks.first()
      MusicAlbum(
        name = first.album,
        artist = first.artist,
        year = first.year,
        genre = first.genre,
        tracks = sortedTracks,
      )
    }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
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

  /** IDs of built-in playlists that are dynamically generated rather than persisted as user entries. */
  val BUILT_IN_PLAYLIST_IDS: Set<String> = setOf(FAVORITES_PLAYLIST_ID, MOST_PLAYED_PLAYLIST_ID)

  /** IDs of sample playlists shipped by older versions; stripped from persisted playlists on load. */
  val LEGACY_SAMPLE_PLAYLIST_IDS: Set<String> = setOf("playlist_synth_night", "playlist_chill_focus")

  /**
   * Builds the built-in Favorites [MusicPlaylist] from [favoriteIds], preserving library order.
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
   * Sorts [tracks] by combined play statistics (# of plays and total listen time), falling back to
   * most-recently-played and alphabetical title order for ties.
   */
  fun sortTracksByPlayStats(
    tracks: List<MusicTrack>,
    playStats: Map<String, TrackPlayStats>,
  ): List<MusicTrack> {
    if (tracks.isEmpty()) return emptyList()
    return tracks.sortedWith(
      compareByDescending<MusicTrack> { playStats[it.id]?.engagementScore ?: 0L }
        .thenByDescending { playStats[it.id]?.playCount ?: 0 }
        .thenByDescending { playStats[it.id]?.totalPlayTimeMs ?: 0L }
        .thenByDescending { playStats[it.id]?.lastPlayedAtMs ?: 0L }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
    )
  }

  /**
   * Builds the built-in Most Played [MusicPlaylist], automatically sorting all songs in [allTracks]
   * by a combination of play count and total playtime. Always returned so it is available by default.
   */
  fun buildMostPlayedPlaylist(
    playStats: Map<String, TrackPlayStats>,
    allTracks: List<MusicTrack>,
  ): MusicPlaylist {
    return MusicPlaylist(
      id = MOST_PLAYED_PLAYLIST_ID,
      name = MOST_PLAYED_PLAYLIST_NAME,
      tracks = sortTracksByPlayStats(allTracks, playStats),
    )
  }

  /**
   * Records a play-count increment and/or additional listening duration for [trackId] inside [current].
   */
  fun recordPlayStat(
    current: Map<String, TrackPlayStats>,
    trackId: String,
    incrementPlayCount: Boolean = false,
    addedPlayTimeMs: Long = 0L,
    timestampMs: Long = System.currentTimeMillis(),
  ): Map<String, TrackPlayStats> {
    if (trackId.isBlank()) return current
    val deltaMs = addedPlayTimeMs.coerceAtLeast(0L)
    if (!incrementPlayCount && deltaMs == 0L) return current
    val prev = current[trackId] ?: TrackPlayStats()
    val updated = prev.copy(
      playCount = prev.playCount + if (incrementPlayCount) 1 else 0,
      totalPlayTimeMs = prev.totalPlayTimeMs + deltaMs,
      lastPlayedAtMs = max(prev.lastPlayedAtMs, timestampMs),
    )
    return current + (trackId to updated)
  }

  /**
   * Serializes a [TrackPlayStats] map into a compact string for `SharedPreferences`.
   */
  fun serializePlayStats(stats: Map<String, TrackPlayStats>): String {
    return stats.entries
      .filter { (id, s) -> id.isNotBlank() && s.hasBeenPlayed }
      .joinToString("\n") { (id, s) ->
        val safeId = id.replace("\n", " ").replace("\t", " ").trim()
        "$safeId\t${s.playCount}\t${s.totalPlayTimeMs}\t${s.lastPlayedAtMs}"
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
          if (id.isNotEmpty() && (count > 0 || timeMs > 0L)) {
            result[id] = TrackPlayStats(
              playCount = count,
              totalPlayTimeMs = timeMs,
              lastPlayedAtMs = lastAt,
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
  ): List<MusicTrack> {
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
      MusicSortOrder.MOST_PLAYED -> sortTracksByPlayStats(tracks, playStats)
    }
  }

  /**
   * Resolves the next track index in a queue of size [queueSize].
   *
   * @param userInitiated `true` when the user explicitly tapped the Next button; `false` when a track
   *   finished playing naturally.
   */
  fun resolveNextTrackIndex(
    currentIndex: Int,
    queueSize: Int,
    repeatMode: MusicRepeatMode,
    shuffleEnabled: Boolean,
    userInitiated: Boolean = true,
    randomIndexProvider: (Int) -> Int = { bound -> (0 until bound).random() },
  ): Int? {
    if (queueSize <= 0) return null
    val safeCurrent = currentIndex.coerceIn(0, queueSize - 1)

    if (!userInitiated && repeatMode == MusicRepeatMode.REPEAT_ONE) {
      return safeCurrent
    }

    if (shuffleEnabled && queueSize > 1) {
      var candidate = randomIndexProvider(queueSize).coerceIn(0, queueSize - 1)
      if (candidate == safeCurrent) {
        candidate = (safeCurrent + 1) % queueSize
      }
      return candidate
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
   * Currently active [MusicPlaybackSession], observable by Compose UI (such as the expanded
   * navigation pane mini-player in [com.example.toolbox.ui.main.MainScreen]).
   */
  var activeSessionState by mutableStateOf<MusicPlaybackSession?>(null)
    internal set

  /**
   * Currently active [MusicTrack] across the Music Player session, observable by Compose UI
   * (including [com.example.toolbox.ui.theme.ToolboxTheme] and Settings).
   */
  var activeTrack by mutableStateOf<MusicTrack?>(DEFAULT_SAMPLE_TRACKS.firstOrNull())
    internal set

  /**
   * Currently active [AlbumArtPalette] for the Music Player's active track, observable by Compose UI
   * so the app's accent color can dynamically follow the Music Player's colors.
   */
  var activeTrackPalette by mutableStateOf(resolveTrackPalette(DEFAULT_SAMPLE_TRACKS.firstOrNull()))
    internal set

  /**
   * Updates the observable [activeTrack] and [activeTrackPalette] for the Music Player.
   */
  fun updateActiveTrackPalette(
    track: MusicTrack?,
    palette: AlbumArtPalette = resolveTrackPalette(track),
  ) {
    activeTrack = track
    activeTrackPalette = palette
  }

  /**
   * Returns the currently active [MusicPlaybackSession], if any.
   */
  fun getActivePlaybackSession(): MusicPlaybackSession? {
    synchronized(sessionLock) {
      return activePlaybackSession
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
    }
    activeSessionState = null
    if (activity is LifecycleOwner && observer != null) {
      try {
        activity.lifecycle.removeObserver(observer)
      } catch (_: Throwable) {}
    }
    sessionToRelease?.release()
    val defaultTrack = DEFAULT_SAMPLE_TRACKS.firstOrNull()
    updateActiveTrackPalette(defaultTrack, resolveTrackPalette(defaultTrack))
  }

  /**
   * Acquires or creates the background-capable [MusicPlaybackSession] scoped to [activity] so that
   * music playback, queue progression, play statistics, and sleep timers continue seamlessly in the
   * background when the user switches from the Music tool to another tool.
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
        isInspection = true,
      )
      activeSessionState = createdInspection
      return createdInspection
    }

    var previousToRelease: MusicPlaybackSession? = null
    val session: MusicPlaybackSession

    synchronized(sessionLock) {
      val currentSession = activePlaybackSession
      val currentActivity = activeActivityRef?.get()
      if (currentSession != null && !currentSession.isReleased && (activity == null || currentActivity === activity)) {
        currentSession.syncConfiguration(
          initialDirectory = initialDirectory,
          initialTracks = initialTracks,
          initialTab = initialTab,
          initialViewMode = initialViewMode,
          initialPlayerExpanded = initialPlayerExpanded,
          initialQueueExpanded = initialQueueExpanded,
        )
        activeSessionState = currentSession
        return currentSession
      }

      previousToRelease = currentSession
      val created = MusicPlaybackSession(
        context = context.applicationContext ?: context,
        initialDirectory = initialDirectory,
        initialTracks = initialTracks,
        initialTab = initialTab,
        initialViewMode = initialViewMode,
        initialPlayerExpanded = initialPlayerExpanded,
        initialQueueExpanded = initialQueueExpanded,
        isInspection = false,
      )
      activePlaybackSession = created
      activeSessionState = created
      activeActivityRef = if (activity != null) WeakReference(activity) else null

      if (activity is LifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_DESTROY) {
            var shouldRelease = false
            synchronized(sessionLock) {
              if (activePlaybackSession === created) {
                activePlaybackSession = null
                activeActivityRef = null
                activeLifecycleObserver = null
                shouldRelease = true
              }
            }
            if (shouldRelease) {
              if (activeSessionState === created) {
                activeSessionState = null
              }
              created.release()
            }
          }
        }
        activeLifecycleObserver = observer
        try {
          activity.lifecycle.addObserver(observer)
        } catch (_: Throwable) {}
      }
      session = created
    }

    previousToRelease?.release()
    return session
  }
}

private fun defaultPlaybackDispatcher(): CoroutineDispatcher {
  return try {
    if (Looper.getMainLooper() != null) {
      Dispatchers.Main
    } else {
      Dispatchers.Default
    }
  } catch (_: Throwable) {
    Dispatchers.Default
  }
}

/**
 * Persistent playback session and state holder for the Music Player tool.
 *
 * Continues playing audio, advancing the playback queue, tracking listening statistics, and
 * counting down the sleep timer in the background when the user switches to another tool.
 */
class MusicPlaybackSession(
  context: Context? = null,
  initialDirectory: File? = null,
  initialTracks: List<MusicTrack>? = null,
  initialTab: MusicLibraryTab? = null,
  initialViewMode: MusicViewMode? = null,
  initialPlayerExpanded: Boolean = false,
  initialQueueExpanded: Boolean = false,
  val isInspection: Boolean = false,
  coroutineContext: CoroutineContext = defaultPlaybackDispatcher(),
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

  var isReleased: Boolean = false
    private set

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

  // Library Tab: Artists, Albums, Songs, Genres, Playlists
  var selectedTab by mutableStateOf(
    initialTab ?: MusicLibraryTab.fromNameOrDefault(prefs?.getString(PREF_KEY_MUSIC_TAB, null))
  )

  fun updateTab(tab: MusicLibraryTab) {
    selectedTab = tab
    prefs?.edit()?.putString(PREF_KEY_MUSIC_TAB, tab.name)?.apply()
  }

  // View Mode: List vs Card
  var viewMode by mutableStateOf(
    initialViewMode ?: MusicViewMode.fromNameOrDefault(prefs?.getString(PREF_KEY_MUSIC_VIEW_MODE, null))
  )

  fun updateViewMode(mode: MusicViewMode) {
    viewMode = mode
    prefs?.edit()?.putString(PREF_KEY_MUSIC_VIEW_MODE, mode.name)?.apply()
  }

  // Sort Order
  var sortOrder by mutableStateOf(
    MusicSortOrder.fromNameOrDefault(prefs?.getString(PREF_KEY_MUSIC_SORT_ORDER, null))
  )

  fun updateSortOrder(order: MusicSortOrder) {
    sortOrder = order
    prefs?.edit()?.putString(PREF_KEY_MUSIC_SORT_ORDER, order.name)?.apply()
  }

  // Shuffle & Repeat
  var isShuffleEnabled by mutableStateOf(
    prefs?.getBoolean(PREF_KEY_MUSIC_SHUFFLE, false) ?: false
  )

  fun toggleShuffle() {
    isShuffleEnabled = !isShuffleEnabled
    prefs?.edit()?.putBoolean(PREF_KEY_MUSIC_SHUFFLE, isShuffleEnabled)?.apply()
  }

  var repeatMode by mutableStateOf(
    MusicRepeatMode.fromNameOrDefault(prefs?.getString(PREF_KEY_MUSIC_REPEAT, null))
  )

  fun cycleRepeatMode() {
    repeatMode = repeatMode.next()
    prefs?.edit()?.putString(PREF_KEY_MUSIC_REPEAT, repeatMode.name)?.apply()
  }

  // Favorites
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
    prefs?.edit()?.putString(PREF_KEY_MUSIC_FAVORITES, next.joinToString("|"))?.apply()
  }

  var showFavoritesOnly by mutableStateOf(false)

  // Play Statistics (persisted in SharedPreferences: play count + total playtime per track)
  var playStats by mutableStateOf(
    MusicPlayerEngine.deserializePlayStats(prefs?.getString(PREF_KEY_MUSIC_PLAY_STATS, ""))
  )

  fun recordTrackPlayStats(
    trackId: String,
    incrementPlayCount: Boolean = false,
    addedPlayTimeMs: Long = 0L,
    persistImmediately: Boolean = incrementPlayCount,
  ) {
    val next = MusicPlayerEngine.recordPlayStat(
      current = playStats,
      trackId = trackId,
      incrementPlayCount = incrementPlayCount,
      addedPlayTimeMs = addedPlayTimeMs,
    )
    if (next !== playStats) {
      playStats = next
      if (persistImmediately) {
        prefs?.edit()?.putString(PREF_KEY_MUSIC_PLAY_STATS, MusicPlayerEngine.serializePlayStats(next))?.apply()
      }
    }
  }

  fun flushPlayStats() {
    prefs?.edit()?.putString(PREF_KEY_MUSIC_PLAY_STATS, MusicPlayerEngine.serializePlayStats(playStats))?.apply()
  }

  // Playlists
  var savedPlaylistEntries by mutableStateOf(
    MusicPlayerEngine.deserializePlaylists(prefs?.getString(PREF_KEY_MUSIC_PLAYLISTS, ""))
      .filterNot { it.id in MusicPlayerEngine.LEGACY_SAMPLE_PLAYLIST_IDS || it.id in MusicPlayerEngine.BUILT_IN_PLAYLIST_IDS }
  )

  fun updateSavedPlaylists(next: List<SavedPlaylistEntry>) {
    savedPlaylistEntries = next
    prefs?.edit()?.putString(PREF_KEY_MUSIC_PLAYLISTS, MusicPlayerEngine.serializePlaylists(next))?.apply()
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

  fun deletePlaylist(playlistId: String) {
    if (playlistId in MusicPlayerEngine.BUILT_IN_PLAYLIST_IDS) return
    updateSavedPlaylists(savedPlaylistEntries.filterNot { it.id == playlistId })
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
    selectedArtistName = null
    selectedAlbumKey = null
    selectedGenreName = null
    selectedPlaylistId = null
  }

  // Library Tracks & Playback Queue State
  var allTracks by mutableStateOf<List<MusicTrack>>(emptyList())
  var playbackQueue by mutableStateOf<List<MusicTrack>>(emptyList())
  var currentQueueIndex by mutableIntStateOf(0)

  val currentTrack: MusicTrack?
    get() = playbackQueue.getOrNull(
      currentQueueIndex.coerceIn(0, maxOf(0, playbackQueue.size - 1))
    ) ?: allTracks.firstOrNull()

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

  init {
    if (!isInspection && cacheDir != null) {
      MusicPlayerEngine.configureDiskCache(cacheDir)
    }
    val includeSamples = prefs?.getBoolean(PREF_KEY_MUSIC_INCLUDE_SAMPLES, false) ?: false
    val initialLoaded = when {
      initialTracks != null -> initialTracks
      isInspection -> MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
      else -> MusicPlayerEngine.getCachedLibraryTracks(
        initialDirectory = initialDirectory,
        includeSamples = includeSamples,
      ) ?: MusicPlayerEngine.loadLibraryTracks(
        initialDirectory = initialDirectory,
        includeSamples = includeSamples,
        inspectMetadataRetriever = false,
        deferAlbumArt = true,
      )
    }
    allTracks = initialLoaded
    playbackQueue = initialLoaded
    currentQueueIndex = 0
    trackDurationMs = currentTrack?.durationMs?.coerceAtLeast(1000L) ?: 15_000L
    MusicPlayerEngine.activeSessionState = this
    prepareAndSyncMediaPlayer(forceRestart = false)
    startAsyncLibraryLoadAndArtworkPrefetch()
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
    }
    if (initialViewMode != null && initialViewMode != configuredInitialViewMode) {
      configuredInitialViewMode = initialViewMode
      viewMode = initialViewMode
    }
    if (initialPlayerExpanded != configuredInitialPlayerExpanded) {
      configuredInitialPlayerExpanded = initialPlayerExpanded
      isPlayerExpanded = initialPlayerExpanded
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
      val reloaded = when {
        initialTracks != null -> initialTracks
        else -> MusicPlayerEngine.getCachedLibraryTracks(
          initialDirectory = initialDirectory,
          includeSamples = includeSamples,
        ) ?: MusicPlayerEngine.loadLibraryTracks(
          initialDirectory = initialDirectory,
          includeSamples = includeSamples,
          inspectMetadataRetriever = false,
          deferAlbumArt = true,
        )
      }
      val activeId = currentTrack?.id
      allTracks = reloaded
      if (playbackQueue.isEmpty() || !isPlaying) {
        playbackQueue = reloaded
        val idx = if (activeId != null) reloaded.indexOfFirst { it.id == activeId } else 0
        currentQueueIndex = idx.coerceAtLeast(0)
        prepareAndSyncMediaPlayer(forceRestart = false)
      }
      startAsyncLibraryLoadAndArtworkPrefetch()
    }
  }

  private fun startAsyncLibraryLoadAndArtworkPrefetch() {
    if (isInspection || appContext == null) return
    libraryLoadJob?.cancel()
    libraryLoadJob = scope.launch {
      val explicitTracks = configuredInitialTracks
      if (explicitTracks == null) {
        val includeSamples = prefs?.getBoolean(PREF_KEY_MUSIC_INCLUDE_SAMPLES, false) ?: false
        val loaded = MusicPlayerEngine.loadLibraryTracksAsync(
          initialDirectory = configuredInitialDirectory,
          includeSamples = includeSamples,
          inspectMetadataRetriever = true,
          deferAlbumArt = true,
        )
        val prevTrackId = currentTrack?.id
        val activeId = playbackQueue.getOrNull(currentQueueIndex)?.id
        allTracks = loaded
        val byId = loaded.associateBy { it.id }
        val updatedQueue = if (playbackQueue.isEmpty()) {
          loaded
        } else {
          playbackQueue.map { existing -> byId[existing.id] ?: existing }
        }
        playbackQueue = if (updatedQueue.size == loaded.size && updatedQueue.zip(loaded).all { (a, b) -> a.id == b.id }) {
          loaded
        } else {
          updatedQueue
        }
        if (activeId != null) {
          val newIdx = playbackQueue.indexOfFirst { it.id == activeId }
          if (newIdx >= 0) {
            currentQueueIndex = newIdx
          }
        }
        val newTrack = currentTrack
        if (newTrack != null && (newTrack.id != prevTrackId || mediaPlayer == null)) {
          trackDurationMs = newTrack.durationMs.coerceAtLeast(1000L)
          prepareAndSyncMediaPlayer(forceRestart = false)
        }
      }
      val tracksToPrefetch = allTracks
      if (tracksToPrefetch.isNotEmpty()) {
        try {
          MusicPlayerEngine.prefetchAlbumArt(tracksToPrefetch, sizePx = 240)
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
    if (track != null && !isInspection && !track.isSampleTrack && appContext != null) {
      paletteLoadJob = scope.launch {
        try {
          val loadedPalette = MusicPlayerEngine.loadTrackPaletteAsync(track, sizePx = 360)
          if (!isReleased && currentTrack?.id == track.id) {
            activePalette = loadedPalette
            MusicPlayerEngine.updateActiveTrackPalette(track, loadedPalette)
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
      releaseMediaPlayer()
      syncProgressTicker()
      return
    }

    if (isInspection) {
      trackDurationMs = track.durationMs.coerceAtLeast(1000L)
      syncProgressTicker()
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

  fun stepToTrack(
    newIndex: Int,
    autoPlay: Boolean = isPlaying,
  ) {
    if (isReleased || playbackQueue.isEmpty()) return
    activePlayInstanceId++
    val clamped = newIndex.coerceIn(0, playbackQueue.lastIndex)
    val prevId = playbackQueue.getOrNull(currentQueueIndex)?.id
    currentQueueIndex = clamped
    currentPositionMs = 0L
    val target = playbackQueue[clamped]
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
    activePlayInstanceId++
    val queueList = if (contextTracks.isNotEmpty()) contextTracks else listOf(track)
    playbackQueue = queueList
    val idx = queueList.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
    currentQueueIndex = idx
    currentPositionMs = 0L
    trackDurationMs = track.durationMs.coerceAtLeast(1000L)
    lastCountedPlayTrackId = track.id
    recordTrackPlayStats(trackId = track.id, incrementPlayCount = true, persistImmediately = true)
    isPlaying = true
    prepareAndSyncMediaPlayer(forceRestart = true)
  }

  fun skipToNext(userInitiated: Boolean = true) {
    if (isReleased) return
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
      stepToTrack(prevIdx, autoPlay = isPlaying)
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
      currentQueueIndex = updatedCurrentIndex
      return true
    }
    return false
  }

  fun updateSeekDrag(fraction: Float) {
    isDraggingSeek = true
    dragSeekFraction = fraction.coerceIn(0f, 1f)
  }

  fun finishSeekDrag() {
    val targetMs = (dragSeekFraction * trackDurationMs).toLong().coerceIn(0L, trackDurationMs)
    currentPositionMs = targetMs
    isDraggingSeek = false
    try {
      mediaPlayer?.seekTo(targetMs.toInt())
    } catch (_: Throwable) {}
  }

  fun cyclePlaybackSpeed() {
    val idx = MusicPlayerEngine.PLAYBACK_SPEED_OPTIONS.indexOfFirst { it == playbackSpeed }
    val nextIdx = if (idx >= 0) (idx + 1) % MusicPlayerEngine.PLAYBACK_SPEED_OPTIONS.size else 2
    playbackSpeed = MusicPlayerEngine.PLAYBACK_SPEED_OPTIONS[nextIdx]
    applyPlaybackSpeed(mediaPlayer)
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
    scope.cancel()
    releaseMediaPlayer()
  }
}

