package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.toolbox.R
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Snapshot of the playback, artwork, palette, and transport state rendered by the two
 * Music Player Home Screen AppWidgets:
 * 1. [MusicMiniBarWidgetProvider] — Single-bar mini player widget (similar to the collapsed mini player)
 * 2. [MusicAlbumArtWidgetProvider] — Album art widget with the favorite star overlaid at the bottom-right
 *    of the album art, track title & artist, and the full 5 playback controls below it
 */
@Immutable
data class MusicPlayerWidgetState(
  val track: MusicTrack?,
  val trackId: String?,
  val title: String,
  val artist: String,
  val album: String,
  val miniBarSubtitle: String,
  val isPlaying: Boolean,
  val isFavorite: Boolean,
  val isExternalMedia: Boolean,
  val sourceAppName: String,
  val progressFraction: Float,
  val progressPermille: Int,
  val shuffleMode: MusicShuffleMode,
  val repeatMode: MusicRepeatMode,
  val palette: AlbumArtPalette,
  val playPauseIconRes: Int,
  val playPauseContentDescription: String,
  val playPauseContainerArgb: Int,
  val playPauseContentArgb: Int,
  val favoriteIconRes: Int,
  val favoriteContentDescription: String,
  val miniFavoriteTintArgb: Int,
  val albumOverlayFavoriteTintArgb: Int,
  val albumOverlayFavoriteBgArgb: Int,
  val shuffleIconRes: Int,
  val shuffleContentDescription: String,
  val shuffleTintArgb: Int,
  val shuffleHighlightBgArgb: Int,
  val isShuffleHighlighted: Boolean,
  val repeatIconRes: Int,
  val repeatContentDescription: String,
  val repeatTintArgb: Int,
  val repeatHighlightBgArgb: Int,
  val isRepeatHighlighted: Boolean,
  val artworkBitmap: Bitmap?,
)

/**
 * Central renderer, state resolver, and update coordinator for the two Music Player Home Screen
 * AppWidgets ([MusicMiniBarWidgetProvider] and [MusicAlbumArtWidgetProvider]).
 */
object MusicPlayerWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.music.ACTION_WIDGET_REFRESH"

  private const val OVERLAY_FAVORITE_BG_ARGB = 0x73000000 // Color.Black.copy(alpha = 0.45f)
  private const val INACTIVE_CONTROL_TINT_ARGB = 0xB3FFFFFF.toInt() // Color.White.copy(alpha = 0.7f)
  private const val MINI_INACTIVE_FAVORITE_TINT_ARGB = 0xCCFFFFFF.toInt() // Color.White.copy(alpha = 0.8f)
  private const val DARK_PLAY_ICON_TINT_ARGB = 0xFF101018.toInt()
  private const val LIGHT_PLAY_ICON_TINT_ARGB = 0xFFFFFFFF.toInt()

  private const val REQUEST_CODE_MINI_OPEN = 2100
  private const val REQUEST_CODE_MINI_FAVORITE = 2101
  private const val REQUEST_CODE_MINI_PREV = 2102
  private const val REQUEST_CODE_MINI_PLAY_PAUSE = 2103
  private const val REQUEST_CODE_MINI_NEXT = 2104

  private const val REQUEST_CODE_ALBUM_OPEN = 2200
  private const val REQUEST_CODE_ALBUM_FAVORITE = 2201
  private const val REQUEST_CODE_ALBUM_SHUFFLE = 2202
  private const val REQUEST_CODE_ALBUM_PREV = 2203
  private const val REQUEST_CODE_ALBUM_PLAY_PAUSE = 2204
  private const val REQUEST_CODE_ALBUM_NEXT = 2205
  private const val REQUEST_CODE_ALBUM_REPEAT = 2206

  /**
   * Converts a Compose [Color] into a 32-bit `0xAARRGGBB` integer.
   */
  fun colorToArgb(color: Color, alphaOverride: Float? = null): Int {
    val a = ((alphaOverride ?: color.alpha) * 255f).roundToInt().coerceIn(0, 255)
    val r = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val g = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val b = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    return (a shl 24) or (r shl 16) or (g shl 8) or b
  }

  /**
   * Computes the contrasting icon color (`#101018` for bright accents, `#FFFFFF` for dark accents)
   * used inside the filled circular Play/Pause button, matching `CollapsedMiniMusicPlayer` and
   * `ExpandedMusicPlayerOverlay`.
   */
  fun resolvePlayPauseContentArgb(accentColor: Color): Int {
    val perceivedLuma = accentColor.red * 0.299f + accentColor.green * 0.587f + accentColor.blue * 0.114f
    return if (perceivedLuma > 0.55f) DARK_PLAY_ICON_TINT_ARGB else LIGHT_PLAY_ICON_TINT_ARGB
  }

  private fun loadPersistedFavoriteIds(context: Context?): Set<String> {
    if (context == null) return emptySet()
    return try {
      val prefs = context.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
      prefs.getString(PREF_KEY_MUSIC_FAVORITES, "")
        .orEmpty()
        .split("|")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()
    } catch (_: Throwable) {
      emptySet()
    }
  }

  private fun loadPersistedShuffleMode(context: Context?): MusicShuffleMode {
    if (context == null) return MusicShuffleMode.OFF
    return try {
      val prefs = context.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
      val savedMode = prefs.getString(PREF_KEY_MUSIC_SHUFFLE_MODE, null)
      if (!savedMode.isNullOrBlank()) {
        MusicShuffleMode.fromNameOrDefault(savedMode)
      } else if (prefs.getBoolean(PREF_KEY_MUSIC_SHUFFLE, false)) {
        MusicShuffleMode.SHUFFLE
      } else {
        MusicShuffleMode.OFF
      }
    } catch (_: Throwable) {
      MusicShuffleMode.OFF
    }
  }

  private fun loadPersistedRepeatMode(context: Context?): MusicRepeatMode {
    if (context == null) return MusicRepeatMode.OFF
    return try {
      val prefs = context.getSharedPreferences(MUSIC_PLAYER_PREFS_NAME, Context.MODE_PRIVATE)
      MusicRepeatMode.fromNameOrDefault(prefs.getString(PREF_KEY_MUSIC_REPEAT, null))
    } catch (_: Throwable) {
      MusicRepeatMode.OFF
    }
  }

  /**
   * Resolves the current [MusicPlayerWidgetState] from the active [MusicPlaybackSession],
   * active external Android system media session ([SystemMediaPlayingInfo]), or persisted
   * preferences and starter library fallback.
   */
  fun resolveWidgetState(
    context: Context? = null,
    session: MusicPlaybackSession? = MusicPlayerEngine.getActivePlaybackSession()?.takeIf { !it.isReleased },
    externalMedia: SystemMediaPlayingInfo? = MusicPlayerEngine.externalSystemMediaInfo
      ?.takeUnless { MusicPlayerEngine.isIgnoredPhoneOrCallSystemMediaInfo(it, context) },
    decodeArtwork: Boolean = true,
  ): MusicPlayerWidgetState {
    val internalPlaying = session?.let { it.isPlaying && it.currentTrack != null } == true

    if (!internalPlaying && externalMedia != null && MusicPlayerEngine.isMusicPlayingOverride != true) {
      val palette = externalMedia.palette
      val accentColor = palette.accentColor
      val accentArgb = colorToArgb(accentColor)
      val highlightBgArgb = colorToArgb(accentColor, alphaOverride = 0.25f)
      val isPlaying = externalMedia.isPlaying
      val posMs = MusicPlayerEngine.currentExternalSystemMediaPositionMs()
      val durMs = externalMedia.durationMs
      val progressFraction = if (durMs > 0L) {
        (posMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f)
      } else {
        0f
      }
      val progressPermille = (progressFraction * 1000f).roundToInt().coerceIn(0, 1000)
      val displayTitle = externalMedia.title.ifBlank { "Android Media Player" }
      val displayArtist = externalMedia.artist.ifBlank {
        externalMedia.sourceAppName.ifBlank { "Android Media Player" }
      }
      val displayAlbum = externalMedia.album
      val miniSubtitle = listOf(externalMedia.artist, externalMedia.album, externalMedia.sourceAppName)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(" • ")
        .ifBlank { "Android Media Player" }

      val leftAction = externalMedia.customActions.getOrNull(0)
      val rightAction = externalMedia.customActions.getOrNull(1)
      val leftHighlighted = leftAction != null && MusicPlayerEngine.isExternalCustomActionHighlighted(leftAction)
      val rightHighlighted = rightAction != null && MusicPlayerEngine.isExternalCustomActionHighlighted(rightAction)

      return MusicPlayerWidgetState(
        track = null,
        trackId = null,
        title = displayTitle,
        artist = displayArtist,
        album = displayAlbum,
        miniBarSubtitle = miniSubtitle,
        isPlaying = isPlaying,
        isFavorite = false,
        isExternalMedia = true,
        sourceAppName = externalMedia.sourceAppName,
        progressFraction = progressFraction,
        progressPermille = progressPermille,
        shuffleMode = MusicShuffleMode.OFF,
        repeatMode = MusicRepeatMode.OFF,
        palette = palette,
        playPauseIconRes = if (isPlaying) R.drawable.ic_media_pause else R.drawable.ic_media_play,
        playPauseContentDescription = if (isPlaying) "Pause" else "Play",
        playPauseContainerArgb = accentArgb,
        playPauseContentArgb = resolvePlayPauseContentArgb(accentColor),
        favoriteIconRes = R.drawable.ic_media_favorite_off,
        favoriteContentDescription = "Favorite",
        miniFavoriteTintArgb = MINI_INACTIVE_FAVORITE_TINT_ARGB,
        albumOverlayFavoriteTintArgb = LIGHT_PLAY_ICON_TINT_ARGB,
        albumOverlayFavoriteBgArgb = OVERLAY_FAVORITE_BG_ARGB,
        shuffleIconRes = R.drawable.ic_media_shuffle_off,
        shuffleContentDescription = leftAction?.label ?: MusicShuffleMode.OFF.displayName,
        shuffleTintArgb = if (leftHighlighted) accentArgb else INACTIVE_CONTROL_TINT_ARGB,
        shuffleHighlightBgArgb = highlightBgArgb,
        isShuffleHighlighted = leftHighlighted,
        repeatIconRes = R.drawable.ic_media_repeat_off,
        repeatContentDescription = rightAction?.label ?: MusicRepeatMode.OFF.displayName,
        repeatTintArgb = if (rightHighlighted) accentArgb else INACTIVE_CONTROL_TINT_ARGB,
        repeatHighlightBgArgb = highlightBgArgb,
        isRepeatHighlighted = rightHighlighted,
        artworkBitmap = externalMedia.artworkBitmap,
      )
    }

    val track = session?.currentTrack
      ?: session?.allTracks?.firstOrNull()
      ?: MusicPlayerEngine.activeTrack
      ?: MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.firstOrNull()

    val favoriteIds = session?.favoriteIds ?: loadPersistedFavoriteIds(context)
    val isFavorite = track != null && track.id in favoriteIds
    val shuffleMode = session?.shuffleMode ?: loadPersistedShuffleMode(context)
    val repeatMode = session?.repeatMode ?: loadPersistedRepeatMode(context)
    val isPlaying = MusicPlayerEngine.isMusicPlayingOverride
      ?: (session?.isPlaying == true && session.currentTrack != null)

    val posMs = if (session?.currentTrack != null) session.currentPositionMs.coerceAtLeast(0L) else 0L
    val durMs = (session?.trackDurationMs ?: track?.durationMs ?: 15_000L).coerceAtLeast(1L)
    val progressFraction = if (session?.currentTrack != null && durMs > 0L) {
      (posMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f)
    } else {
      0f
    }
    val progressPermille = (progressFraction * 1000f).roundToInt().coerceIn(0, 1000)

    val palette = if (session != null && session.currentTrack != null && session.currentTrack?.id == track?.id) {
      session.activePalette
    } else {
      MusicPlayerEngine.resolveTrackPalette(track)
    }
    val accentColor = palette.accentColor
    val accentArgb = colorToArgb(accentColor)
    val highlightBgArgb = colorToArgb(accentColor, alphaOverride = 0.25f)

    val title = track?.title ?: "No Track Selected"
    val artist = track?.artist ?: "Toolbox Music"
    val album = track?.album ?: ""
    val miniSubtitle = if (album.isNotBlank()) "$artist • $album" else artist

    val artworkBitmap = if (track != null && decodeArtwork) {
      MusicPlayerEngine.getCachedAlbumArtBitmap(track, 320)
        ?: try {
          MusicPlayerEngine.generateOrDecodeAlbumArtBitmap(track, 320)
        } catch (_: Throwable) {
          null
        }
    } else if (track != null) {
      MusicPlayerEngine.getCachedAlbumArtBitmap(track, 320)
    } else {
      null
    }

    val shuffleIconRes = MusicPlayerEngine.resolveSystemMediaShuffleIconRes(shuffleMode)
    val repeatIconRes = MusicPlayerEngine.resolveSystemMediaRepeatIconRes(repeatMode)
    val favoriteIconRes = MusicPlayerEngine.resolveSystemMediaFavoriteIconRes(isFavorite)
    val isShuffleHighlighted = shuffleMode.isEnabled
    val isRepeatHighlighted = repeatMode != MusicRepeatMode.OFF

    return MusicPlayerWidgetState(
      track = track,
      trackId = track?.id,
      title = title,
      artist = artist,
      album = album,
      miniBarSubtitle = miniSubtitle,
      isPlaying = isPlaying,
      isFavorite = isFavorite,
      isExternalMedia = false,
      sourceAppName = "Toolbox Music",
      progressFraction = progressFraction,
      progressPermille = progressPermille,
      shuffleMode = shuffleMode,
      repeatMode = repeatMode,
      palette = palette,
      playPauseIconRes = if (isPlaying) R.drawable.ic_media_pause else R.drawable.ic_media_play,
      playPauseContentDescription = if (isPlaying) "Pause" else "Play",
      playPauseContainerArgb = accentArgb,
      playPauseContentArgb = resolvePlayPauseContentArgb(accentColor),
      favoriteIconRes = favoriteIconRes,
      favoriteContentDescription = if (isFavorite) "Unfavorite" else "Favorite",
      miniFavoriteTintArgb = if (isFavorite) accentArgb else MINI_INACTIVE_FAVORITE_TINT_ARGB,
      albumOverlayFavoriteTintArgb = if (isFavorite) accentArgb else LIGHT_PLAY_ICON_TINT_ARGB,
      albumOverlayFavoriteBgArgb = OVERLAY_FAVORITE_BG_ARGB,
      shuffleIconRes = shuffleIconRes,
      shuffleContentDescription = shuffleMode.displayName,
      shuffleTintArgb = if (isShuffleHighlighted) accentArgb else INACTIVE_CONTROL_TINT_ARGB,
      shuffleHighlightBgArgb = highlightBgArgb,
      isShuffleHighlighted = isShuffleHighlighted,
      repeatIconRes = repeatIconRes,
      repeatContentDescription = repeatMode.displayName,
      repeatTintArgb = if (isRepeatHighlighted) accentArgb else INACTIVE_CONTROL_TINT_ARGB,
      repeatHighlightBgArgb = highlightBgArgb,
      isRepeatHighlighted = isRepeatHighlighted,
      artworkBitmap = artworkBitmap,
    )
  }

  /**
   * Builds the styled subtitle [CharSequence] for the Album Art widget (`"Artist • Album"`),
   * coloring the artist in [AlbumArtPalette.accentColor] (bold) and `" • Album"` in translucent white,
   * matching `ExpandedMusicPlayerOverlay`.
   */
  fun buildAlbumWidgetSubtitleText(state: MusicPlayerWidgetState): CharSequence {
    val artist = state.artist.trim()
    val album = state.album.trim()
    val fullText = when {
      artist.isNotEmpty() && album.isNotEmpty() -> "$artist • $album"
      artist.isNotEmpty() -> artist
      album.isNotEmpty() -> album
      else -> state.miniBarSubtitle
    }
    if (artist.isEmpty()) return fullText
    return try {
      val spannable = SpannableString(fullText)
      val accentArgb = colorToArgb(state.palette.accentColor)
      spannable.setSpan(
        ForegroundColorSpan(accentArgb),
        0,
        artist.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
      )
      spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        0,
        artist.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
      )
      if (fullText.length > artist.length) {
        spannable.setSpan(
          ForegroundColorSpan(0xD9FFFFFF.toInt()),
          artist.length,
          fullText.length,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
      }
      spannable
    } catch (_: Throwable) {
      fullText
    }
  }

  /**
   * Renders the rounded card background [Bitmap] for the single-bar Mini Player widget, matching
   * `CollapsedMiniMusicPlayer`:
   * - `18dp` rounded corners
   * - Horizontal gradient from `darkSurfaceColor` to `lerp(dominantColor, Color(0xFF14141F), 0.38f)`
   * - `1dp` border in `accentColor.copy(alpha = 0.4f)`
   */
  fun renderMiniBarBackgroundBitmap(
    palette: AlbumArtPalette,
    widthPx: Int = 640,
    heightPx: Int = 128,
    cornerRadiusPx: Float = 36f,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(120, 960)
    val safeH = heightPx.coerceIn(48, 320)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()

      val startColor = colorToArgb(palette.darkSurfaceColor)
      val endColor = colorToArgb(lerp(palette.dominantColor, Color(0xFF14141F), 0.38f))
      val borderColor = colorToArgb(palette.accentColor, alphaOverride = 0.42f)

      val halfStroke = borderWidthPx * 0.5f
      val rect = RectF(halfStroke, halfStroke, w - halfStroke, h - halfStroke)

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        shader = LinearGradient(
          0f,
          0f,
          w,
          0f,
          intArrayOf(startColor, endColor),
          floatArrayOf(0f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint)

      val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = borderWidthPx
        color = borderColor
      }
      canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, borderPaint)

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders the rounded card background [Bitmap] for the Album Art & 5 Playback Controls widget,
   * matching `ExpandedMusicPlayerOverlay`:
   * - `24dp` rounded corners
   * - 3-stop vertical gradient: `lerp(dominantColor, Color(0xFF12121A), 0.25f)`,
   *   `lerp(secondaryColor, Color(0xFF0E0E16), 0.45f)`, `deepBackgroundColor`
   * - `1dp` border in `accentColor.copy(alpha = 0.4f)`
   */
  fun renderAlbumArtWidgetBackgroundBitmap(
    palette: AlbumArtPalette,
    widthPx: Int = 480,
    heightPx: Int = 540,
    cornerRadiusPx: Float = 48f,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(120, 720)
    val safeH = heightPx.coerceIn(120, 720)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()

      val topColor = colorToArgb(lerp(palette.dominantColor, Color(0xFF12121A), 0.25f))
      val midColor = colorToArgb(lerp(palette.secondaryColor, Color(0xFF0E0E16), 0.45f))
      val bottomColor = colorToArgb(palette.deepBackgroundColor)
      val borderColor = colorToArgb(palette.accentColor, alphaOverride = 0.42f)

      val halfStroke = borderWidthPx * 0.5f
      val rect = RectF(halfStroke, halfStroke, w - halfStroke, h - halfStroke)

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        shader = LinearGradient(
          0f,
          0f,
          0f,
          h,
          intArrayOf(topColor, midColor, bottomColor),
          floatArrayOf(0f, 0.52f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint)

      val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = borderWidthPx
        color = borderColor
      }
      canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, borderPaint)

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders a center-cropped, rounded-corner album art [Bitmap] (with optional subtle white border)
   * suitable for `RemoteViews.setImageViewBitmap` on all Android versions.
   */
  fun renderRoundedAlbumArtBitmap(
    sourceBitmap: Bitmap?,
    palette: AlbumArtPalette,
    widthPx: Int,
    heightPx: Int,
    cornerRadiusPx: Float,
    borderWidthPx: Float = 0f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(48, 512)
    val safeH = heightPx.coerceIn(48, 512)
    return try {
      val output = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(output)
      val w = safeW.toFloat()
      val h = safeH.toFloat()
      val halfStroke = borderWidthPx * 0.5f
      val rect = RectF(halfStroke, halfStroke, w - halfStroke, h - halfStroke)

      val validSource = sourceBitmap?.takeIf { !it.isRecycled && it.width > 0 && it.height > 0 }
      if (validSource != null) {
        val shader = BitmapShader(validSource, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val scale = max(w / validSource.width.toFloat(), h / validSource.height.toFloat())
        val dx = (w - validSource.width * scale) * 0.5f
        val dy = (h - validSource.height * scale) * 0.5f
        val matrix = Matrix().apply {
          setScale(scale, scale)
          postTranslate(dx, dy)
        }
        shader.setLocalMatrix(matrix)
        val artPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
          style = Paint.Style.FILL
          this.shader = shader
        }
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, artPaint)
      } else {
        val domArgb = colorToArgb(palette.dominantColor)
        val secArgb = colorToArgb(palette.secondaryColor)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.FILL
          shader = LinearGradient(
            0f,
            0f,
            w,
            h,
            intArrayOf(domArgb, secArgb),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
          )
        }
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, bgPaint)
      }

      if (borderWidthPx > 0f) {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = borderWidthPx
          color = 0x40FFFFFF // Color.White.copy(alpha = 0.25f)
        }
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, borderPaint)
      }

      output
    } catch (_: Throwable) {
      sourceBitmap
    }
  }

  /**
   * Builds a broadcast [PendingIntent] targeting [MusicMediaControlReceiver] for a widget transport
   * or favorite/shuffle/repeat action.
   */
  fun buildWidgetActionPendingIntent(
    context: Context,
    action: String,
    requestCode: Int,
  ): PendingIntent {
    val intent = Intent(context, MusicMediaControlReceiver::class.java).apply {
      this.action = action
      setPackage(context.packageName)
    }
    return PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds an activity [PendingIntent] that launches the standalone Music Player with the full
   * player view expanded when the user taps the widget card, album art, or track title.
   */
  fun buildOpenMusicPlayerPendingIntent(
    context: Context,
    requestCode: Int,
  ): PendingIntent {
    val launchIntent = MusicPlayerEngine.createSystemMediaLaunchIntent(context)
    return PendingIntent.getActivity(
      context,
      requestCode,
      launchIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the [RemoteViews] for the single-bar Mini Player widget ([MusicMiniBarWidgetProvider]),
   * modeled after `CollapsedMiniMusicPlayer`.
   */
  fun buildMiniBarRemoteViews(
    context: Context,
    state: MusicPlayerWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_music_mini_bar)
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val minWidthDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 280)?.takeIf { it > 0 } ?: 280
    val minHeightDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 64)?.takeIf { it > 0 } ?: 64
    val bgWidthPx = (minWidthDp * density).roundToInt().coerceIn(240, 800)
    val bgHeightPx = (minHeightDp * density).roundToInt().coerceIn(72, 240)

    renderMiniBarBackgroundBitmap(
      palette = state.palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = 18f * density,
      borderWidthPx = max(1f, 1f * density),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_mini_background, bgBmp)
    }

    val thumbSizePx = (44f * density).roundToInt().coerceIn(64, 192)
    renderRoundedAlbumArtBitmap(
      sourceBitmap = state.artworkBitmap,
      palette = state.palette,
      widthPx = thumbSizePx,
      heightPx = thumbSizePx,
      cornerRadiusPx = 10f * density,
      borderWidthPx = 0f,
    )?.let { artBmp ->
      views.setImageViewBitmap(R.id.widget_mini_album_art, artBmp)
    }

    views.setProgressBar(R.id.widget_mini_progress_bar, 1000, state.progressPermille, false)
    views.setTextViewText(R.id.widget_mini_title, state.title)
    views.setTextViewText(R.id.widget_mini_subtitle, state.miniBarSubtitle)

    // Favorite Star button (hidden when controlling external Android system media)
    if (state.isExternalMedia) {
      views.setViewVisibility(R.id.widget_mini_favorite_button, View.GONE)
    } else {
      views.setViewVisibility(R.id.widget_mini_favorite_button, View.VISIBLE)
      views.setImageViewResource(R.id.widget_mini_favorite_button, state.favoriteIconRes)
      views.setInt(R.id.widget_mini_favorite_button, "setColorFilter", state.miniFavoriteTintArgb)
      views.setContentDescription(R.id.widget_mini_favorite_button, state.favoriteContentDescription)
      views.setOnClickPendingIntent(
        R.id.widget_mini_favorite_button,
        buildWidgetActionPendingIntent(
          context,
          MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE,
          REQUEST_CODE_MINI_FAVORITE,
        ),
      )
    }

    // Previous, Play/Pause, Next controls
    views.setImageViewResource(R.id.widget_mini_prev_button, R.drawable.ic_media_skip_previous)
    views.setInt(R.id.widget_mini_prev_button, "setColorFilter", LIGHT_PLAY_ICON_TINT_ARGB)
    views.setContentDescription(R.id.widget_mini_prev_button, "Previous track")
    views.setOnClickPendingIntent(
      R.id.widget_mini_prev_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_PREVIOUS,
        REQUEST_CODE_MINI_PREV,
      ),
    )

    views.setInt(R.id.widget_mini_play_pause_bg, "setColorFilter", state.playPauseContainerArgb)
    views.setImageViewResource(R.id.widget_mini_play_pause_button, state.playPauseIconRes)
    views.setInt(R.id.widget_mini_play_pause_button, "setColorFilter", state.playPauseContentArgb)
    views.setContentDescription(R.id.widget_mini_play_pause_button, state.playPauseContentDescription)
    views.setOnClickPendingIntent(
      R.id.widget_mini_play_pause_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_PLAY_PAUSE,
        REQUEST_CODE_MINI_PLAY_PAUSE,
      ),
    )

    views.setImageViewResource(R.id.widget_mini_next_button, R.drawable.ic_media_skip_next)
    views.setInt(R.id.widget_mini_next_button, "setColorFilter", LIGHT_PLAY_ICON_TINT_ARGB)
    views.setContentDescription(R.id.widget_mini_next_button, "Next track")
    views.setOnClickPendingIntent(
      R.id.widget_mini_next_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_NEXT,
        REQUEST_CODE_MINI_NEXT,
      ),
    )

    // Tapping the bar, album art, or song text opens the expanded Music Player
    val openIntent = buildOpenMusicPlayerPendingIntent(context, REQUEST_CODE_MINI_OPEN)
    views.setOnClickPendingIntent(R.id.widget_mini_root, openIntent)
    views.setOnClickPendingIntent(R.id.widget_mini_album_art, openIntent)
    views.setOnClickPendingIntent(R.id.widget_mini_text_container, openIntent)

    return views
  }

  /**
   * Builds the [RemoteViews] for the Album Art & Full 5 Playback Controls widget
   * ([MusicAlbumArtWidgetProvider]), modeled after `ExpandedMusicPlayerOverlay` with:
   * - Album art at top
   * - Favorite star overlaid at the bottom-right corner of the album art
   * - Track title and artist/album below the album art
   * - Full 5 playback controls (Shuffle, Previous, Play/Pause, Next, Repeat) below
   */
  fun buildAlbumArtRemoteViews(
    context: Context,
    state: MusicPlayerWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_music_album_art)
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val minWidthDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 240)?.takeIf { it > 0 } ?: 240
    val minHeightDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 260)?.takeIf { it > 0 } ?: 260
    val bgWidthPx = (minWidthDp * density).roundToInt().coerceIn(220, 640)
    val bgHeightPx = (minHeightDp * density).roundToInt().coerceIn(240, 680)

    renderAlbumArtWidgetBackgroundBitmap(
      palette = state.palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = 24f * density,
      borderWidthPx = max(1f, 1f * density),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_album_background, bgBmp)
    }

    // Estimate the album art container dimensions (widget size minus padding, text row, and 5-button row)
    val artWidthDp = (minWidthDp - 28).coerceAtLeast(120)
    val artHeightDp = (minHeightDp - 124).coerceAtLeast(100)
    val artWidthPx = (artWidthDp * density).roundToInt().coerceIn(140, 440)
    val artHeightPx = (artHeightDp * density).roundToInt().coerceIn(120, 440)

    renderRoundedAlbumArtBitmap(
      sourceBitmap = state.artworkBitmap,
      palette = state.palette,
      widthPx = artWidthPx,
      heightPx = artHeightPx,
      cornerRadiusPx = 20f * density,
      borderWidthPx = max(1.5f, 1.5f * density),
    )?.let { artBmp ->
      views.setImageViewBitmap(R.id.widget_album_art, artBmp)
    }

    // Favorite Star overlaid at the bottom-right of the album art
    if (state.isExternalMedia) {
      views.setViewVisibility(R.id.widget_album_favorite_container, View.GONE)
    } else {
      views.setViewVisibility(R.id.widget_album_favorite_container, View.VISIBLE)
      views.setInt(R.id.widget_album_favorite_bg, "setColorFilter", state.albumOverlayFavoriteBgArgb)
      views.setImageViewResource(R.id.widget_album_favorite_button, state.favoriteIconRes)
      views.setInt(R.id.widget_album_favorite_button, "setColorFilter", state.albumOverlayFavoriteTintArgb)
      views.setContentDescription(R.id.widget_album_favorite_button, state.favoriteContentDescription)
      views.setOnClickPendingIntent(
        R.id.widget_album_favorite_button,
        buildWidgetActionPendingIntent(
          context,
          MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE,
          REQUEST_CODE_ALBUM_FAVORITE,
        ),
      )
    }

    // Track Title & Artist / Album
    views.setTextViewText(R.id.widget_album_title, state.title)
    views.setTextViewText(R.id.widget_album_subtitle, buildAlbumWidgetSubtitleText(state))

    // 1. Shuffle Control
    views.setViewVisibility(
      R.id.widget_album_shuffle_bg,
      if (state.isShuffleHighlighted) View.VISIBLE else View.INVISIBLE,
    )
    views.setInt(R.id.widget_album_shuffle_bg, "setColorFilter", state.shuffleHighlightBgArgb)
    views.setImageViewResource(R.id.widget_album_shuffle_button, state.shuffleIconRes)
    views.setInt(R.id.widget_album_shuffle_button, "setColorFilter", state.shuffleTintArgb)
    views.setContentDescription(R.id.widget_album_shuffle_button, state.shuffleContentDescription)
    views.setOnClickPendingIntent(
      R.id.widget_album_shuffle_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE,
        REQUEST_CODE_ALBUM_SHUFFLE,
      ),
    )

    // 2. Previous Control
    views.setImageViewResource(R.id.widget_album_prev_button, R.drawable.ic_media_skip_previous)
    views.setInt(R.id.widget_album_prev_button, "setColorFilter", LIGHT_PLAY_ICON_TINT_ARGB)
    views.setContentDescription(R.id.widget_album_prev_button, "Previous track")
    views.setOnClickPendingIntent(
      R.id.widget_album_prev_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_PREVIOUS,
        REQUEST_CODE_ALBUM_PREV,
      ),
    )

    // 3. Play / Pause Control
    views.setInt(R.id.widget_album_play_pause_bg, "setColorFilter", state.playPauseContainerArgb)
    views.setImageViewResource(R.id.widget_album_play_pause_button, state.playPauseIconRes)
    views.setInt(R.id.widget_album_play_pause_button, "setColorFilter", state.playPauseContentArgb)
    views.setContentDescription(R.id.widget_album_play_pause_button, state.playPauseContentDescription)
    views.setOnClickPendingIntent(
      R.id.widget_album_play_pause_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_PLAY_PAUSE,
        REQUEST_CODE_ALBUM_PLAY_PAUSE,
      ),
    )

    // 4. Next Control
    views.setImageViewResource(R.id.widget_album_next_button, R.drawable.ic_media_skip_next)
    views.setInt(R.id.widget_album_next_button, "setColorFilter", LIGHT_PLAY_ICON_TINT_ARGB)
    views.setContentDescription(R.id.widget_album_next_button, "Next track")
    views.setOnClickPendingIntent(
      R.id.widget_album_next_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_NEXT,
        REQUEST_CODE_ALBUM_NEXT,
      ),
    )

    // 5. Repeat Control
    views.setViewVisibility(
      R.id.widget_album_repeat_bg,
      if (state.isRepeatHighlighted) View.VISIBLE else View.INVISIBLE,
    )
    views.setInt(R.id.widget_album_repeat_bg, "setColorFilter", state.repeatHighlightBgArgb)
    views.setImageViewResource(R.id.widget_album_repeat_button, state.repeatIconRes)
    views.setInt(R.id.widget_album_repeat_button, "setColorFilter", state.repeatTintArgb)
    views.setContentDescription(R.id.widget_album_repeat_button, state.repeatContentDescription)
    views.setOnClickPendingIntent(
      R.id.widget_album_repeat_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_CYCLE_REPEAT,
        REQUEST_CODE_ALBUM_REPEAT,
      ),
    )

    // Tapping the widget background, album art, or song text opens the expanded Music Player
    val openIntent = buildOpenMusicPlayerPendingIntent(context, REQUEST_CODE_ALBUM_OPEN)
    views.setOnClickPendingIntent(R.id.widget_album_root, openIntent)
    views.setOnClickPendingIntent(R.id.widget_album_art, openIntent)
    views.setOnClickPendingIntent(R.id.widget_album_text_container, openIntent)

    return views
  }

  /**
   * Handles a media control or widget broadcast action from either [MusicMediaControlReceiver],
   * [MusicMiniBarWidgetProvider], or [MusicAlbumArtWidgetProvider], routing to an active external
   * system media session or the built-in [MusicPlaybackSession] and refreshing all active widgets.
   */
  fun handleWidgetAction(
    context: Context?,
    action: String?,
  ): Boolean {
    if (action.isNullOrBlank()) return false
    if (action == ACTION_WIDGET_REFRESH) {
      if (context != null) updateAllWidgets(context)
      return true
    }

    val existingSession = MusicPlayerEngine.getActivePlaybackSession()?.takeIf { !it.isReleased }
    val internalPlaying = existingSession?.let { it.isPlaying && it.currentTrack != null } == true
    val external = MusicPlayerEngine.externalSystemMediaInfo
      ?.takeUnless { MusicPlayerEngine.isIgnoredPhoneOrCallSystemMediaInfo(it, context) }

    if (!internalPlaying && external != null && MusicPlayerEngine.isMusicPlayingOverride != true) {
      val handledExternal = when (action) {
        MusicPlayerEngine.ACTION_MEDIA_PLAY,
        MusicPlayerEngine.ACTION_MEDIA_PAUSE,
        MusicPlayerEngine.ACTION_MEDIA_PLAY_PAUSE -> MusicPlayerEngine.toggleExternalSystemMediaPlayPause(context)
        MusicPlayerEngine.ACTION_MEDIA_NEXT -> MusicPlayerEngine.skipExternalSystemMediaToNext(context)
        MusicPlayerEngine.ACTION_MEDIA_PREVIOUS -> MusicPlayerEngine.skipExternalSystemMediaToPrevious(context)
        MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE -> {
          val leftAction = external.customActions.getOrNull(0)
          if (leftAction != null) {
            MusicPlayerEngine.triggerExternalSystemMediaCustomAction(leftAction, context)
          } else {
            false
          }
        }
        MusicPlayerEngine.ACTION_MEDIA_CYCLE_REPEAT -> {
          val rightAction = external.customActions.getOrNull(1)
          if (rightAction != null) {
            MusicPlayerEngine.triggerExternalSystemMediaCustomAction(rightAction, context)
          } else {
            false
          }
        }
        else -> false
      }
      if (handledExternal) {
        if (context != null) updateAllWidgets(context)
        return true
      }
    }

    val session = existingSession
      ?: context?.let { ctx ->
        MusicPlayerEngine.getOrCreatePlaybackSession(context = ctx.applicationContext ?: ctx)
      }
      ?: return false

    if (session.allTracks.isEmpty() &&
      action in setOf(
        MusicPlayerEngine.ACTION_MEDIA_PLAY,
        MusicPlayerEngine.ACTION_MEDIA_PLAY_PAUSE,
        MusicPlayerEngine.ACTION_MEDIA_NEXT,
        MusicPlayerEngine.ACTION_MEDIA_PREVIOUS,
        MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE,
      )
    ) {
      session.allTracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    }

    val handled = session.handleMediaControlAction(action)
    if (context != null) {
      updateAllWidgets(context)
    }
    return handled
  }

  /**
   * Updates all pinned instances of [MusicMiniBarWidgetProvider] and [MusicAlbumArtWidgetProvider]
   * on the user's home screen.
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val miniComponent = ComponentName(appCtx, MusicMiniBarWidgetProvider::class.java)
      val albumComponent = ComponentName(appCtx, MusicAlbumArtWidgetProvider::class.java)
      val miniIds = try {
        manager.getAppWidgetIds(miniComponent) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      val albumIds = try {
        manager.getAppWidgetIds(albumComponent) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (miniIds.isEmpty() && albumIds.isEmpty()) return

      val state = resolveWidgetState(appCtx)
      if (miniIds.isNotEmpty()) {
        updateMiniBarWidgets(appCtx, manager, miniIds, state)
      }
      if (albumIds.isNotEmpty()) {
        updateAlbumArtWidgets(appCtx, manager, albumIds, state)
      }
    } catch (_: Throwable) {}
  }

  /**
   * Updates only the progress bar on any active [MusicMiniBarWidgetProvider] instances during
   * 1-second playback progress ticks without re-rendering full bitmaps.
   */
  fun updateMiniBarProgressOnly(
    context: Context?,
    progressPermille: Int,
  ) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val miniComponent = ComponentName(appCtx, MusicMiniBarWidgetProvider::class.java)
      val miniIds = manager.getAppWidgetIds(miniComponent) ?: return
      if (miniIds.isEmpty()) return
      val clamped = progressPermille.coerceIn(0, 1000)
      val partialViews = RemoteViews(appCtx.packageName, R.layout.widget_music_mini_bar).apply {
        setProgressBar(R.id.widget_mini_progress_bar, 1000, clamped, false)
      }
      for (id in miniIds) {
        manager.partiallyUpdateAppWidget(id, partialViews)
      }
    } catch (_: Throwable) {}
  }

  /**
   * Updates the specified [appWidgetIds] for [MusicMiniBarWidgetProvider].
   */
  fun updateMiniBarWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: MusicPlayerWidgetState = resolveWidgetState(context),
  ) {
    for (appWidgetId in appWidgetIds) {
      try {
        val options = try {
          appWidgetManager.getAppWidgetOptions(appWidgetId)
        } catch (_: Throwable) {
          null
        }
        val views = buildMiniBarRemoteViews(
          context = context,
          state = state,
          options = options,
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
      } catch (_: Throwable) {}
    }
  }

  /**
   * Updates the specified [appWidgetIds] for [MusicAlbumArtWidgetProvider].
   */
  fun updateAlbumArtWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: MusicPlayerWidgetState = resolveWidgetState(context),
  ) {
    for (appWidgetId in appWidgetIds) {
      try {
        val options = try {
          appWidgetManager.getAppWidgetOptions(appWidgetId)
        } catch (_: Throwable) {
          null
        }
        val views = buildAlbumArtRemoteViews(
          context = context,
          state = state,
          options = options,
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
      } catch (_: Throwable) {}
    }
  }
}

/**
 * First Music Player Home Screen AppWidget:
 * Single-bar mini player widget, styled after the collapsed mini player in the Music Player tool,
 * featuring a top progress bar, rounded album art thumbnail, song title & artist/album subtitle,
 * favorite star toggle, previous, circular accent play/pause button, and next controls.
 */
class MusicMiniBarWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    MusicPlayerWidgets.updateMiniBarWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    MusicPlayerWidgets.updateMiniBarWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.music.ACTION_")) {
      MusicPlayerWidgets.handleWidgetAction(context, action)
    }
  }
}

/**
 * Second Music Player Home Screen AppWidget:
 * Album art widget with the star for favoriting overlaid at the bottom-right corner of the album art,
 * track title & artist below the album art, and the full 5 playback controls (Shuffle, Previous,
 * Play/Pause, Next, Repeat) at the bottom, styled after the expanded Music Player tool.
 */
class MusicAlbumArtWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    MusicPlayerWidgets.updateAlbumArtWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    MusicPlayerWidgets.updateAlbumArtWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.music.ACTION_")) {
      MusicPlayerWidgets.handleWidgetAction(context, action)
    }
  }
}
