package com.example.toolbox.ui.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

const val MUSIC_PLAYER_PREFS_NAME = "music_player_preferences"
const val PREF_KEY_MUSIC_VIEW_MODE = "music_player_view_mode"
const val PREF_KEY_MUSIC_TAB = "music_player_selected_tab"
const val PREF_KEY_MUSIC_SORT_ORDER = "music_player_sort_order"
const val PREF_KEY_MUSIC_SHUFFLE = "music_player_shuffle_enabled"
const val PREF_KEY_MUSIC_REPEAT = "music_player_repeat_mode"
const val PREF_KEY_MUSIC_FAVORITES = "music_player_favorite_ids"
const val PREF_KEY_MUSIC_INCLUDE_SAMPLES = "music_player_include_samples"

val DEFAULT_MUSIC_FOLDER_NAMES: List<String> = listOf("Music", "Audio", "Download")

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
) {
  val durationFormatted: String
    get() = AudioRecorderEngine.formatDurationMs(durationMs)

  val sizeFormatted: String
    get() = if (sizeBytes > 0L) FileManagerEngine.formatFileSize(sizeBytes) else "2.4 MB"
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

  private const val MAX_ARTWORK_CACHE_ENTRIES = 64
  private val artworkCacheLock = Any()
  private val artworkBitmapCache = object : LinkedHashMap<String, Bitmap>(32, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
      return size > MAX_ARTWORK_CACHE_ENTRIES
    }
  }
  private val paletteCache = object : LinkedHashMap<String, AlbumArtPalette>(64, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AlbumArtPalette>?): Boolean {
      return size > MAX_ARTWORK_CACHE_ENTRIES
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
   * Resolves the [AlbumArtPalette] for [track], using cached bitmap extraction if available,
   * or computing deterministically from the track's album art colors.
   */
  fun resolveTrackPalette(track: MusicTrack?): AlbumArtPalette {
    if (track == null) {
      return buildPaletteFromRgb(0x6750A4, 0x311B92, 0xD0BCFF)
    }
    val key = "${track.id}:${track.artworkPrimaryRgb}:${track.artworkSecondaryRgb}:${track.artworkAccentRgb}"
    synchronized(artworkCacheLock) {
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
   */
  fun getCachedAlbumArtBitmap(track: MusicTrack, sizePx: Int = 320): Bitmap? {
    val key = "${track.album}|${track.artist}|${track.artworkPrimaryRgb}|$sizePx"
    synchronized(artworkCacheLock) {
      return artworkBitmapCache[key]
    }
  }

  /**
   * Loads or generates the album art [Bitmap] for [track] on a background thread,
   * also updating the cached [AlbumArtPalette] from the decoded bitmap if embedded art was found.
   */
  suspend fun loadAlbumArtBitmapAsync(
    track: MusicTrack,
    sizePx: Int = 320,
  ): Bitmap = withContext(Dispatchers.IO) {
    ensureActive()
    getCachedAlbumArtBitmap(track, sizePx)?.let { return@withContext it }
    generateOrDecodeAlbumArtBitmap(track, sizePx)
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

    val key = "${track.album}|${track.artist}|${track.artworkPrimaryRgb}|$sizePx"
    val file = track.file
    if (file != null && file.exists() && !track.isSampleTrack) {
      // 1. Check embedded ID3 / MP4 cover art
      val retriever = MediaMetadataRetriever()
      try {
        retriever.setDataSource(file.absolutePath)
        val embeddedBytes = retriever.embeddedPicture
        if (embeddedBytes != null && embeddedBytes.isNotEmpty()) {
          val decoded = BitmapFactory.decodeByteArray(embeddedBytes, 0, embeddedBytes.size)
          if (decoded != null) {
            val extractedPalette = extractPaletteFromBitmap(decoded)
            val paletteKey = "${track.id}:${track.artworkPrimaryRgb}:${track.artworkSecondaryRgb}:${track.artworkAccentRgb}"
            synchronized(artworkCacheLock) {
              artworkBitmapCache[key] = decoded
              paletteCache[paletteKey] = extractedPalette
            }
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

      // 2. Check sibling cover.jpg / folder.jpg in parent folder
      val parent = file.parentFile
      if (parent != null && parent.isDirectory) {
        val coverNames = listOf("cover.jpg", "cover.png", "folder.jpg", "folder.png", "album.jpg", "album.png")
        for (coverName in coverNames) {
          val coverFile = File(parent, coverName)
          if (coverFile.exists() && coverFile.isFile) {
            val decoded = GalleryEngine.decodeThumbnailBitmap(coverFile, reqSizePx = sizePx)
            if (decoded != null) {
              val extractedPalette = extractPaletteFromBitmap(decoded)
              val paletteKey = "${track.id}:${track.artworkPrimaryRgb}:${track.artworkSecondaryRgb}:${track.artworkAccentRgb}"
              synchronized(artworkCacheLock) {
                artworkBitmapCache[key] = decoded
                paletteCache[paletteKey] = extractedPalette
              }
              return decoded
            }
          }
        }
      }
    }

    // 3. Render procedural album cover Bitmap
    val safeSize = sizePx.coerceIn(64, 1024)
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

    synchronized(artworkCacheLock) {
      artworkBitmapCache[key] = bitmap
    }
    return bitmap
  }

  /**
   * Parses track metadata from a local audio [file].
   * Uses `MediaMetadataRetriever` when available on Android runtime, falling back to intelligent
   * `Artist - Title` filename and parent folder parsing in unit tests or untagged files.
   */
  fun buildTrackFromFile(
    file: File,
    inspectMetadataRetriever: Boolean = true,
  ): MusicTrack {
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

          val pic = retriever.embeddedPicture
          if (pic != null && pic.isNotEmpty()) {
            val bmp = BitmapFactory.decodeByteArray(pic, 0, pic.size)
            if (bmp != null) {
              val pal = extractPaletteFromBitmap(bmp)
              val pRgb = ((pal.dominantColor.red * 255).roundToInt() shl 16) or
                ((pal.dominantColor.green * 255).roundToInt() shl 8) or
                (pal.dominantColor.blue * 255).roundToInt()
              val sRgb = ((pal.secondaryColor.red * 255).roundToInt() shl 16) or
                ((pal.secondaryColor.green * 255).roundToInt() shl 8) or
                (pal.secondaryColor.blue * 255).roundToInt()
              val aRgb = ((pal.accentColor.red * 255).roundToInt() shl 16) or
                ((pal.accentColor.green * 255).roundToInt() shl 8) or
                (pal.accentColor.blue * 255).roundToInt()
              extractedColors = Triple(pRgb, sRgb, aRgb)
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

    return MusicTrack(
      id = "file_${file.absolutePath}",
      title = title,
      artist = artist,
      album = album,
      genre = genre,
      durationMs = durationMs,
      trackNumber = trackNumber,
      year = year,
      file = file,
      sizeBytes = file.length(),
      lastModifiedMs = file.lastModified(),
      artworkPrimaryRgb = primaryRgb,
      artworkSecondaryRgb = secondaryRgb,
      artworkAccentRgb = accentRgb,
      isSampleTrack = false,
    )
  }

  /**
   * Scans [directories] recursively up to [maxDepth] for supported music files.
   */
  fun scanMusicDirectories(
    directories: List<File>,
    maxDepth: Int = 5,
    inspectMetadataRetriever: Boolean = true,
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
      .map { buildTrackFromFile(it, inspectMetadataRetriever = inspectMetadataRetriever) }
  }

  /**
   * Resolves the full library of tracks from [initialDirectory] (if provided) or the default
   * `/Music`, `/Audio`, and `/Download` folders under [rootDirectory], combining or falling back to
   * [DEFAULT_SAMPLE_TRACKS] when [includeSamples] is enabled or when no local audio files exist.
   */
  fun loadLibraryTracks(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    initialDirectory: File? = null,
    includeSamples: Boolean = true,
    inspectMetadataRetriever: Boolean = true,
  ): List<MusicTrack> {
    val targetDirs = if (initialDirectory != null) {
      listOf(initialDirectory)
    } else {
      DEFAULT_MUSIC_FOLDER_NAMES.map { File(rootDirectory, it) }
    }
    val localTracks = scanMusicDirectories(
      directories = targetDirs,
      inspectMetadataRetriever = inspectMetadataRetriever,
    )
    return when {
      localTracks.isNotEmpty() && includeSamples && initialDirectory == null -> localTracks + DEFAULT_SAMPLE_TRACKS
      localTracks.isNotEmpty() -> localTracks
      includeSamples -> DEFAULT_SAMPLE_TRACKS
      else -> emptyList()
    }
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

  /**
   * Sorts [tracks] according to [sortOrder].
   */
  fun sortTracks(
    tracks: List<MusicTrack>,
    sortOrder: MusicSortOrder,
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
   * Synthesizes a valid, melodic 16-bit PCM WAV file for a built-in sample [track] so `MediaPlayer`
   * can play real musical chords and progressions offline.
   */
  fun ensurePlayableSampleWavFile(
    cacheDir: File,
    track: MusicTrack,
  ): File {
    val sampleDir = File(cacheDir, "music_player_samples").apply { mkdirs() }
    val targetFile = File(sampleDir, "${track.id}.wav")
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
}
