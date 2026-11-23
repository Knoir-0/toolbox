package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.toolbox.R
import com.example.toolbox.ui.main.BackgroundTexture
import com.example.toolbox.ui.main.DEFAULT_HEXAGON_ICON_BUTTONS
import com.example.toolbox.ui.main.NAVIGATION_PREFERENCES_NAME
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import com.example.toolbox.ui.main.PREF_KEY_BACKGROUND_TEXTURE
import com.example.toolbox.ui.main.PREF_KEY_HEXAGON_ICON_BUTTONS
import com.example.toolbox.ui.theme.drawWidgetBackgroundTextureOverlay
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class MusicWidgetViewStyle {
  /** Compact 1-row horizontal bar (left album art, track info, favorite star, prev/play/next). */
  MINI_BAR,

  /** Medium 2-row horizontal card (left album art, top row track info & favorite star, bottom row 5 playback controls). */
  MEDIUM,

  /** Large vertical card (large top square album art with overlaid favorite star, track info, and bottom 5 playback controls). */
  ALBUM_ART,
}

/**
 * Snapshot of the playback, artwork, palette, and transport state rendered by the unified
 * responsive Music Player Home Screen AppWidget ([MusicAlbumArtWidgetProvider]), which automatically
 * switches between three size-adaptive views ([MusicWidgetViewStyle]) as the user resizes it:
 * 1. [MusicWidgetViewStyle.MINI_BAR] — Single-bar mini player view (full-height left-aligned rounded album art,
 *    track title & subtitle, favorite star, and large transport controls, without a seek bar)
 * 2. [MusicWidgetViewStyle.MEDIUM] — Medium 2-row player view (full-height left-aligned rounded album art,
 *    track title & subtitle with favorite star on the top row, and the full 5 playback controls
 *    from the large player underneath the song info)
 * 3. [MusicWidgetViewStyle.ALBUM_ART] — Large album art view with the favorite star overlaid at the bottom-right
 *    of the album art, track title & artist, and the full 5 playback controls below it (extracting the
 *    left and right playback buttons from the Android system media player when external media is active)
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
  val leftCustomAction: SystemMediaCustomAction? = null,
  val rightCustomAction: SystemMediaCustomAction? = null,
  val leftCustomActionIconBitmap: Bitmap? = null,
  val rightCustomActionIconBitmap: Bitmap? = null,
  val isLeftControlVisible: Boolean = true,
  val isRightControlVisible: Boolean = true,
  val hexagonIconButtons: Boolean = false,
  val iconButtonBackgroundRes: Int = R.drawable.bg_music_widget_circle,
  val backgroundTexture: BackgroundTexture = BackgroundTexture.NONE,
)

/**
 * Central renderer, state resolver, and update coordinator for the three Music Player Home Screen
 * AppWidgets ([MusicMiniBarWidgetProvider], [MusicMediumWidgetProvider], and [MusicAlbumArtWidgetProvider]).
 */
object MusicPlayerWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.music.ACTION_WIDGET_REFRESH"

  const val MINI_BAR_HEIGHT_DP = 72f
  const val MINI_BAR_CORNER_RADIUS_DP = 16f
  const val MEDIUM_WIDGET_HEIGHT_DP = 112f
  const val MEDIUM_WIDGET_CORNER_RADIUS_DP = 16f
  const val ALBUM_WIDGET_CORNER_RADIUS_DP = 16f
  const val ALBUM_ART_CORNER_RADIUS_DP = 16f

  /**
   * Height threshold in `dp` below which the responsive Music Player widget renders the compact
   * 1-row [MusicWidgetViewStyle.MINI_BAR] layout (`heightDp < 100f`).
   */
  const val MINI_TO_MEDIUM_HEIGHT_THRESHOLD_DP = 100f

  /**
   * Height threshold in `dp` at or above which the responsive Music Player widget renders the full
   * vertical [MusicWidgetViewStyle.ALBUM_ART] layout (`heightDp >= 260f`).
   */
  const val MEDIUM_TO_ALBUM_ART_HEIGHT_THRESHOLD_DP = 260f

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

  private const val REQUEST_CODE_MEDIUM_OPEN = 2300
  private const val REQUEST_CODE_MEDIUM_FAVORITE = 2301
  private const val REQUEST_CODE_MEDIUM_SHUFFLE = 2302
  private const val REQUEST_CODE_MEDIUM_PREV = 2303
  private const val REQUEST_CODE_MEDIUM_PLAY_PAUSE = 2304
  private const val REQUEST_CODE_MEDIUM_NEXT = 2305
  private const val REQUEST_CODE_MEDIUM_REPEAT = 2306

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

  /**
   * Resolves whether "Hexagonify" is enabled for Home Screen AppWidgets from
   * persisted navigation [Context.getSharedPreferences] or [NavigationPreferencesBridge.hexagonIconButtons].
   */
  fun resolveHexagonIconButtonsEnabled(context: Context? = null): Boolean {
    val activeRepo = NavigationPreferencesBridge.activeRepository
    val useInMemoryRepo = activeRepo is com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
    if (!useInMemoryRepo && context != null) {
      try {
        val prefs = context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (prefs != null && prefs.contains(PREF_KEY_HEXAGON_ICON_BUTTONS)) {
          return prefs.getBoolean(PREF_KEY_HEXAGON_ICON_BUTTONS, DEFAULT_HEXAGON_ICON_BUTTONS)
        }
      } catch (_: Throwable) {}
    }
    return NavigationPreferencesBridge.hexagonIconButtons
  }

  /**
   * Resolves the active [BackgroundTexture] for Home Screen AppWidgets from persisted navigation
   * [Context.getSharedPreferences] or [NavigationPreferencesBridge.backgroundTexture].
   */
  fun resolveBackgroundTexture(context: Context? = null): BackgroundTexture {
    val activeRepo = NavigationPreferencesBridge.activeRepository
    val useInMemoryRepo = activeRepo is com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
    if (!useInMemoryRepo && context != null) {
      try {
        val prefs = context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (prefs != null && prefs.contains(PREF_KEY_BACKGROUND_TEXTURE)) {
          return BackgroundTexture.fromPrefValue(prefs.getString(PREF_KEY_BACKGROUND_TEXTURE, null))
        }
      } catch (_: Throwable) {}
    }
    return NavigationPreferencesBridge.backgroundTexture
  }

  /**
   * Returns the background drawable resource (`R.drawable.bg_music_widget_hexagon` when
   * [hexagonIconButtons] is `true`, or `R.drawable.bg_music_widget_circle` when `false`) for
   * circular (`1:1`) widget action icon buttons, header/empty icon badges, transport buttons, and FABs.
   */
  fun resolveIconButtonBackgroundRes(
    hexagonIconButtons: Boolean = NavigationPreferencesBridge.hexagonIconButtons,
  ): Int = if (hexagonIconButtons) {
    R.drawable.bg_music_widget_hexagon
  } else {
    R.drawable.bg_music_widget_circle
  }

  /**
   * Maps an external Android system media [SystemMediaCustomAction] to a vector drawable resource,
   * matching `resolveExternalCustomActionVector` in `MusicPlayerScreen.kt`.
   */
  fun resolveExternalCustomActionIconRes(
    customAction: SystemMediaCustomAction,
    isHighlighted: Boolean,
  ): Int {
    val combined = "${customAction.action} ${customAction.label}".lowercase(Locale.ROOT)
    return when {
      combined.contains("smart") && combined.contains("shuffle") -> R.drawable.ic_media_shuffle_smart
      combined.contains("shuffle") || combined.contains("random") ->
        if (isHighlighted) R.drawable.ic_media_shuffle_on else R.drawable.ic_media_shuffle_off
      combined.contains("repeat_one") || combined.contains("repeat one") || combined.contains("repeat 1") ->
        R.drawable.ic_media_repeat_one
      combined.contains("repeat") || combined.contains("loop") ->
        if (isHighlighted) R.drawable.ic_media_repeat_all else R.drawable.ic_media_repeat_off
      combined.contains("dislike") ||
        combined.contains("thumb_down") ||
        combined.contains("thumbs_down") ||
        combined.contains("thumbs down") ||
        combined.contains("ban") ||
        combined.contains("hide") -> R.drawable.ic_media_dislike
      combined.contains("favorite") ||
        combined.contains("fav") ||
        combined.contains("star") ||
        combined.contains("heart") ||
        combined.contains("like") ||
        combined.contains("love") ||
        combined.contains("thumb_up") ||
        combined.contains("thumbs_up") ||
        combined.contains("thumbs up") ->
        if (isHighlighted) R.drawable.ic_media_favorite_on else R.drawable.ic_media_favorite_off
      combined.contains("speed") || combined.contains("rate") -> R.drawable.ic_media_speed
      combined.contains("sleep") || combined.contains("timer") -> R.drawable.ic_media_timer
      combined.contains("queue") || combined.contains("playlist") -> R.drawable.ic_media_queue
      combined.contains("add") || combined.contains("plus") -> R.drawable.ic_media_add
      combined.contains("share") -> R.drawable.ic_media_share
      else -> R.drawable.ic_media_tune
    }
  }

  /**
   * Renders a tinted copy of an external custom action icon [Bitmap] so it displays with the
   * active accent or inactive white tint inside a Home Screen AppWidget [RemoteViews].
   */
  fun renderTintedActionIconBitmap(
    sourceBitmap: Bitmap?,
    tintArgb: Int,
    targetSizePx: Int = 64,
  ): Bitmap? {
    val valid = sourceBitmap?.takeIf { !it.isRecycled && it.width > 0 && it.height > 0 } ?: return null
    val safeSize = targetSizePx.coerceIn(24, 192)
    return try {
      val out = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(out)
      val scale = min(safeSize / valid.width.toFloat(), safeSize / valid.height.toFloat())
      val dw = valid.width * scale
      val dh = valid.height * scale
      val left = (safeSize - dw) * 0.5f
      val top = (safeSize - dh) * 0.5f
      val dst = RectF(left, top, left + dw, top + dh)
      val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = PorterDuffColorFilter(tintArgb, PorterDuff.Mode.SRC_IN)
      }
      canvas.drawBitmap(valid, null, dst, paint)
      out
    } catch (_: Throwable) {
      valid
    }
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
    externalMedia: SystemMediaPlayingInfo? = run {
      val internalActive = session?.let { it.isPlaying && it.currentTrack != null } == true
      if (!internalActive && context != null && MusicPlayerEngine.externalSystemMediaInfo == null && MusicPlayerEngine.isMusicPlayingOverride != true) {
        try {
          MusicPlayerEngine.syncExternalSystemMediaSessions(context)
        } catch (_: Throwable) {}
      }
      MusicPlayerEngine.externalSystemMediaInfo
        ?.takeUnless { MusicPlayerEngine.isIgnoredPhoneOrCallSystemMediaInfo(it, context) }
    },
    decodeArtwork: Boolean = true,
  ): MusicPlayerWidgetState {
    val hexagonIconButtons = resolveHexagonIconButtonsEnabled(context)
    val iconButtonBackgroundRes = resolveIconButtonBackgroundRes(hexagonIconButtons)
    val backgroundTexture = resolveBackgroundTexture(context)
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

      val leftIconBitmap = leftAction?.let { action ->
        action.iconBitmap?.takeIf { !it.isRecycled }
          ?: if (context != null && action.iconResId != 0) {
            MusicPlayerEngine.loadExternalCustomActionIconBitmap(
              context,
              externalMedia.sourcePackage.ifBlank { context.packageName },
              action.iconResId,
            )
          } else {
            null
          }
      }
      val rightIconBitmap = rightAction?.let { action ->
        action.iconBitmap?.takeIf { !it.isRecycled }
          ?: if (context != null && action.iconResId != 0) {
            MusicPlayerEngine.loadExternalCustomActionIconBitmap(
              context,
              externalMedia.sourcePackage.ifBlank { context.packageName },
              action.iconResId,
            )
          } else {
            null
          }
      }

      val leftIconRes = if (leftAction != null) {
        resolveExternalCustomActionIconRes(leftAction, leftHighlighted)
      } else {
        R.drawable.ic_media_shuffle_off
      }
      val rightIconRes = if (rightAction != null) {
        resolveExternalCustomActionIconRes(rightAction, rightHighlighted)
      } else {
        R.drawable.ic_media_repeat_off
      }

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
        shuffleIconRes = leftIconRes,
        shuffleContentDescription = leftAction?.label ?: MusicShuffleMode.OFF.displayName,
        shuffleTintArgb = if (leftHighlighted) accentArgb else INACTIVE_CONTROL_TINT_ARGB,
        shuffleHighlightBgArgb = highlightBgArgb,
        isShuffleHighlighted = leftHighlighted,
        repeatIconRes = rightIconRes,
        repeatContentDescription = rightAction?.label ?: MusicRepeatMode.OFF.displayName,
        repeatTintArgb = if (rightHighlighted) accentArgb else INACTIVE_CONTROL_TINT_ARGB,
        repeatHighlightBgArgb = highlightBgArgb,
        isRepeatHighlighted = rightHighlighted,
        artworkBitmap = externalMedia.artworkBitmap,
        leftCustomAction = leftAction,
        rightCustomAction = rightAction,
        leftCustomActionIconBitmap = leftIconBitmap,
        rightCustomActionIconBitmap = rightIconBitmap,
        isLeftControlVisible = leftAction != null,
        isRightControlVisible = rightAction != null,
        hexagonIconButtons = hexagonIconButtons,
        iconButtonBackgroundRes = iconButtonBackgroundRes,
        backgroundTexture = backgroundTexture,
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
      leftCustomAction = null,
      rightCustomAction = null,
      leftCustomActionIconBitmap = null,
      rightCustomActionIconBitmap = null,
      isLeftControlVisible = true,
      isRightControlVisible = true,
      hexagonIconButtons = hexagonIconButtons,
      iconButtonBackgroundRes = iconButtonBackgroundRes,
      backgroundTexture = backgroundTexture,
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
   * Resolves the current widget dimensions in `dp` from [AppWidgetManager] options, accounting for
   * Android 12+ `OPTION_APPWIDGET_SIZES` and portrait (`MIN_WIDTH` x `MAX_HEIGHT`) vs landscape
   * (`MAX_WIDTH` x `MIN_HEIGHT`) bounds so background bitmaps match the view's aspect ratio and
   * corner radii remain evenly rounded.
   */
  internal fun resolveWidgetSizeDp(
    context: Context,
    options: Bundle?,
    defaultWidthDp: Float,
    defaultHeightDp: Float,
  ): Pair<Float, Float> {
    if (options == null) return defaultWidthDp to defaultHeightDp
    val isLandscape = try {
      context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    } catch (_: Throwable) {
      false
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val sizes = try {
        @Suppress("DEPRECATION")
        options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
      } catch (_: Throwable) {
        null
      }
      if (!sizes.isNullOrEmpty()) {
        val chosen = if (isLandscape) {
          sizes.maxByOrNull { it.width }
        } else {
          sizes.minByOrNull { it.width }
        }
        if (chosen != null && chosen.width > 0f && chosen.height > 0f) {
          return chosen.width to chosen.height
        }
      }
    }
    val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
    val maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
    val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
    val maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
    val widthDp = if (isLandscape) {
      (if (maxW > 0) maxW else minW).takeIf { it > 0 }?.toFloat() ?: defaultWidthDp
    } else {
      (if (minW > 0) minW else maxW).takeIf { it > 0 }?.toFloat() ?: defaultWidthDp
    }
    val heightDp = if (isLandscape) {
      (if (minH > 0) minH else maxH).takeIf { it > 0 }?.toFloat() ?: defaultHeightDp
    } else {
      (if (maxH > 0) maxH else minH).takeIf { it > 0 }?.toFloat() ?: defaultHeightDp
    }
    return widthDp to heightDp
  }

  /**
   * Resolves which [MusicWidgetViewStyle] ([MusicWidgetViewStyle.MINI_BAR], [MusicWidgetViewStyle.MEDIUM],
   * or [MusicWidgetViewStyle.ALBUM_ART]) should be displayed for a given widget `(widthDp, heightDp)` size.
   */
  fun resolveWidgetViewStyleForSize(
    @Suppress("UNUSED_PARAMETER") widthDp: Float,
    heightDp: Float,
  ): MusicWidgetViewStyle {
    return when {
      heightDp < MINI_TO_MEDIUM_HEIGHT_THRESHOLD_DP -> MusicWidgetViewStyle.MINI_BAR
      heightDp < MEDIUM_TO_ALBUM_ART_HEIGHT_THRESHOLD_DP -> MusicWidgetViewStyle.MEDIUM
      else -> MusicWidgetViewStyle.ALBUM_ART
    }
  }

  /**
   * Resolves the active [MusicWidgetViewStyle] from [AppWidgetManager] options, defaulting to
   * [MusicWidgetViewStyle.MEDIUM] when no resize options are present.
   */
  fun resolveWidgetViewStyle(
    context: Context,
    options: Bundle?,
    defaultWidthDp: Float = 280f,
    defaultHeightDp: Float = MEDIUM_WIDGET_HEIGHT_DP,
  ): MusicWidgetViewStyle {
    val (widthDp, heightDp) = resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = defaultWidthDp,
      defaultHeightDp = defaultHeightDp,
    )
    return resolveWidgetViewStyleForSize(widthDp, heightDp)
  }

  /**
   * Builds size-responsive [RemoteViews] for the unified Music Player widget, automatically
   * switching between [buildMiniBarRemoteViews], [buildMediumRemoteViews], and [buildAlbumArtRemoteViews]
   * based on the widget's current dimensions (and supplying an Android 12+ `RemoteViews(Map<SizeF, RemoteViews>)`
   * mapping when the launcher provides `OPTION_APPWIDGET_SIZES` so launcher size transitions are seamless).
   */
  fun buildResponsiveMusicWidgetRemoteViews(
    context: Context,
    state: MusicPlayerWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && options != null) {
      val sizes = try {
        @Suppress("DEPRECATION")
        options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
      } catch (_: Throwable) {
        null
      }
      val validSizes = sizes?.filter { it.width > 0f && it.height > 0f }.orEmpty()
      if (validSizes.size > 1) {
        try {
          val viewMap = LinkedHashMap<SizeF, RemoteViews>(validSizes.size)
          for (size in validSizes) {
            val sizeBundle = Bundle().apply {
              putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, size.width.roundToInt())
              putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, size.width.roundToInt())
              putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, size.height.roundToInt())
              putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, size.height.roundToInt())
            }
            val style = resolveWidgetViewStyleForSize(size.width, size.height)
            viewMap[size] = when (style) {
              MusicWidgetViewStyle.MINI_BAR -> buildMiniBarRemoteViews(context, state, sizeBundle)
              MusicWidgetViewStyle.MEDIUM -> buildMediumRemoteViews(context, state, sizeBundle)
              MusicWidgetViewStyle.ALBUM_ART -> buildAlbumArtRemoteViews(context, state, sizeBundle)
            }
          }
          if (viewMap.isNotEmpty()) {
            return RemoteViews(viewMap)
          }
        } catch (_: Throwable) {}
      }
    }

    return when (resolveWidgetViewStyle(context, options)) {
      MusicWidgetViewStyle.MINI_BAR -> buildMiniBarRemoteViews(context, state, options)
      MusicWidgetViewStyle.MEDIUM -> buildMediumRemoteViews(context, state, options)
      MusicWidgetViewStyle.ALBUM_ART -> buildAlbumArtRemoteViews(context, state, options)
    }
  }

  /**
   * Renders the rounded card background [Bitmap] for the single-bar Mini Player widget:
   * - Evenly rounded `16dp` corners
   * - Horizontal gradient fading between the exact same palette colors as the larger Album Art
   *   Music Player widget (`lerp(dominantColor, Color(0xFF12121A), 0.25f)`,
   *   `lerp(secondaryColor, Color(0xFF0E0E16), 0.45f)`, `deepBackgroundColor`), rendered
   *   horizontally instead of vertically
   * - `1dp` border in `accentColor.copy(alpha = 0.42f)` that smoothly fades out to transparent as it
   *   approaches the left-aligned square album art (`x <= h`), and is inset by `borderWidthPx` so it
   *   is never clipped at the corners
   */
  fun renderMiniBarBackgroundBitmap(
    palette: AlbumArtPalette,
    widthPx: Int = 640,
    heightPx: Int = 128,
    cornerRadiusPx: Float = 32f,
    borderWidthPx: Float = 2f,
    backgroundTexture: BackgroundTexture = NavigationPreferencesBridge.backgroundTexture,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(120, 1600)
    val safeH = heightPx.coerceIn(48, 600)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()

      val startColor = colorToArgb(lerp(palette.dominantColor, Color(0xFF12121A), 0.25f))
      val midColor = colorToArgb(lerp(palette.secondaryColor, Color(0xFF0E0E16), 0.45f))
      val endColor = colorToArgb(palette.deepBackgroundColor)

      val fillRect = RectF(0f, 0f, w, h)
      val fillCornerRadius = min(cornerRadiusPx, min(fillRect.width(), fillRect.height()) * 0.5f)

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        shader = LinearGradient(
          0f,
          0f,
          w,
          0f,
          intArrayOf(startColor, midColor, endColor),
          floatArrayOf(0f, 0.52f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawRoundRect(fillRect, fillCornerRadius, fillCornerRadius, fillPaint)

      drawWidgetBackgroundTextureOverlay(
        bitmap = bitmap,
        cornerRadiusPx = fillCornerRadius,
        texture = backgroundTexture,
        primaryColor = palette.accentColor,
        tertiaryColor = palette.dominantColor,
        secondaryColor = palette.secondaryColor,
        onSurfaceColor = Color.White,
        isDarkTheme = true,
      )

      if (borderWidthPx > 0f) {
        val strokeInset = max(borderWidthPx * 1.25f, 1.5f)
        val borderRect = RectF(strokeInset, strokeInset, w - strokeInset, h - strokeInset)
        // Use a slightly larger corner radius (+borderWidthPx) so that even if fitXY stretches the
        // bitmap non-uniformly when the launcher's actual padded cell dimensions differ slightly
        // from AppWidgetManager.getAppWidgetOptions(), the border's corner arc never crosses outside
        // the root @android:id/background view's 16dp clipToOutline curve.
        val borderCornerRadius = max(
          0f,
          min(cornerRadiusPx + borderWidthPx, min(borderRect.width(), borderRect.height()) * 0.5f),
        )
        val borderColorSolid = colorToArgb(palette.accentColor, alphaOverride = 0.42f)
        val borderColorMid = colorToArgb(palette.accentColor, alphaOverride = 0.18f)
        val borderColorTransparent = colorToArgb(palette.accentColor, alphaOverride = 0f)

        // The left-aligned album art occupies [0, h] horizontally. Fade the outline smoothly
        // to transparent as it approaches the album art edge at x = h.
        val artEdgeStop = (h / w).coerceIn(0.08f, 0.55f)
        val midFadeStop = (artEdgeStop + 0.08f).coerceAtMost(0.75f)
        val fullFadeStop = (artEdgeStop + 0.20f).coerceAtMost(0.92f)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = borderWidthPx
          shader = LinearGradient(
            0f,
            0f,
            w,
            0f,
            intArrayOf(
              borderColorTransparent,
              borderColorTransparent,
              borderColorMid,
              borderColorSolid,
              borderColorSolid,
            ),
            floatArrayOf(
              0f,
              artEdgeStop,
              midFadeStop,
              fullFadeStop,
              1f,
            ),
            Shader.TileMode.CLAMP,
          )
        }
        canvas.drawRoundRect(borderRect, borderCornerRadius, borderCornerRadius, borderPaint)
      }

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders the rounded card background [Bitmap] for the Album Art & 5 Playback Controls widget,
   * matching `ExpandedMusicPlayerOverlay`:
   * - `16dp` rounded corners
   * - 3-stop vertical gradient: `lerp(dominantColor, Color(0xFF12121A), 0.25f)`,
   *   `lerp(secondaryColor, Color(0xFF0E0E16), 0.45f)`, `deepBackgroundColor`
   * - `1dp` border in `accentColor.copy(alpha = 0.42f)` inset by `borderWidthPx` so it is never
   *   clipped at the corners
   */
  fun renderAlbumArtWidgetBackgroundBitmap(
    palette: AlbumArtPalette,
    widthPx: Int = 480,
    heightPx: Int = 540,
    cornerRadiusPx: Float = 32f,
    borderWidthPx: Float = 2f,
    backgroundTexture: BackgroundTexture = NavigationPreferencesBridge.backgroundTexture,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(120, 1600)
    val safeH = heightPx.coerceIn(120, 1600)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()

      val topColor = colorToArgb(lerp(palette.dominantColor, Color(0xFF12121A), 0.25f))
      val midColor = colorToArgb(lerp(palette.secondaryColor, Color(0xFF0E0E16), 0.45f))
      val bottomColor = colorToArgb(palette.deepBackgroundColor)

      val fillRect = RectF(0f, 0f, w, h)
      val fillCornerRadius = min(cornerRadiusPx, min(fillRect.width(), fillRect.height()) * 0.5f)

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
      canvas.drawRoundRect(fillRect, fillCornerRadius, fillCornerRadius, fillPaint)

      drawWidgetBackgroundTextureOverlay(
        bitmap = bitmap,
        cornerRadiusPx = fillCornerRadius,
        texture = backgroundTexture,
        primaryColor = palette.accentColor,
        tertiaryColor = palette.dominantColor,
        secondaryColor = palette.secondaryColor,
        onSurfaceColor = Color.White,
        isDarkTheme = true,
      )

      if (borderWidthPx > 0f) {
        val strokeInset = max(borderWidthPx * 1.25f, 1.5f)
        val borderRect = RectF(strokeInset, strokeInset, w - strokeInset, h - strokeInset)
        val borderCornerRadius = max(
          0f,
          min(cornerRadiusPx + borderWidthPx, min(borderRect.width(), borderRect.height()) * 0.5f),
        )
        val borderColor = colorToArgb(palette.accentColor, alphaOverride = 0.42f)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = borderWidthPx
          color = borderColor
        }
        canvas.drawRoundRect(borderRect, borderCornerRadius, borderCornerRadius, borderPaint)
      }

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders a center-cropped, evenly rounded-corner album art [Bitmap] (with optional subtle white border)
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
    val safeW = widthPx.coerceIn(48, 1024)
    val safeH = heightPx.coerceIn(48, 1024)
    return try {
      val output = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(output)
      val w = safeW.toFloat()
      val h = safeH.toFloat()
      val halfStroke = borderWidthPx * 0.5f
      val rect = RectF(halfStroke, halfStroke, w - halfStroke, h - halfStroke)
      val evenCornerRadius = min(cornerRadiusPx, min(rect.width(), rect.height()) * 0.5f)

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
        canvas.drawRoundRect(rect, evenCornerRadius, evenCornerRadius, artPaint)
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
        canvas.drawRoundRect(rect, evenCornerRadius, evenCornerRadius, bgPaint)
      }

      if (borderWidthPx > 0f) {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = borderWidthPx
          color = 0x40FFFFFF // Color.White.copy(alpha = 0.25f)
        }
        canvas.drawRoundRect(rect, evenCornerRadius, evenCornerRadius, borderPaint)
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
   * Builds the [RemoteViews] for the single-bar Mini Player widget ([MusicMiniBarWidgetProvider]):
   * - Evenly rounded corners (`18dp` radius)
   * - Album art taking the full height of the bar, aligned all the way to the left, with corners
   *   rounded using the exact same radius as the bar corners so it fits seamlessly
   * - No seek bar display
   * - Track title and artist/album subtitle
   * - Enlarged Favorite, Previous, Play/Pause, and Next controls
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

    val (widthDp, _) = resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 280f,
      defaultHeightDp = MINI_BAR_HEIGHT_DP,
    )
    val heightDp = MINI_BAR_HEIGHT_DP
    val rawWidthPx = (widthDp * density).coerceAtLeast(120f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(48f)
    val maxDimPx = 1200f
    val scaleFactor = if (rawWidthPx > maxDimPx) maxDimPx / rawWidthPx else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(120)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(48)
    val barCornerRadiusPx = MINI_BAR_CORNER_RADIUS_DP * density * scaleFactor

    renderMiniBarBackgroundBitmap(
      palette = state.palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = barCornerRadiusPx,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
      backgroundTexture = state.backgroundTexture,
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_mini_background, bgBmp)
    }

    // Album art takes the full height of the bar (1:1 square of bgHeightPx x bgHeightPx)
    // and its corners are rounded with the exact same radius as the bar corners.
    val thumbSizePx = bgHeightPx
    renderRoundedAlbumArtBitmap(
      sourceBitmap = state.artworkBitmap,
      palette = state.palette,
      widthPx = thumbSizePx,
      heightPx = thumbSizePx,
      cornerRadiusPx = barCornerRadiusPx,
      borderWidthPx = 0f,
    )?.let { artBmp ->
      views.setImageViewBitmap(R.id.widget_mini_album_art, artBmp)
    }

    views.setTextViewText(R.id.widget_mini_title, state.title)
    views.setTextViewText(R.id.widget_mini_subtitle, state.miniBarSubtitle)

    views.setImageViewResource(R.id.widget_mini_favorite_bg, state.iconButtonBackgroundRes)
    views.setImageViewResource(R.id.widget_mini_play_pause_bg, state.iconButtonBackgroundRes)

    // Favorite Star button (or extracted Left Custom Action when controlling external Android system media)
    if (state.isExternalMedia) {
      if (!state.isLeftControlVisible) {
        views.setViewVisibility(R.id.widget_mini_favorite_container, View.GONE)
        views.setViewVisibility(R.id.widget_mini_favorite_bg, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_mini_favorite_button, View.GONE)
      } else {
        val iconSizePx = (24f * density).roundToInt().coerceIn(32, 96)
        views.setViewVisibility(R.id.widget_mini_favorite_container, View.VISIBLE)
        views.setViewVisibility(R.id.widget_mini_favorite_button, View.VISIBLE)
        views.setViewVisibility(
          R.id.widget_mini_favorite_bg,
          if (state.isShuffleHighlighted) View.VISIBLE else View.INVISIBLE,
        )
        views.setInt(R.id.widget_mini_favorite_bg, "setColorFilter", state.shuffleHighlightBgArgb)
        val leftCustomBmp = state.leftCustomActionIconBitmap?.takeIf { !it.isRecycled }
        if (leftCustomBmp != null) {
          val tintedLeft = renderTintedActionIconBitmap(leftCustomBmp, state.shuffleTintArgb, iconSizePx)
          views.setImageViewBitmap(R.id.widget_mini_favorite_button, tintedLeft ?: leftCustomBmp)
        } else {
          views.setImageViewResource(R.id.widget_mini_favorite_button, state.shuffleIconRes)
        }
        views.setInt(R.id.widget_mini_favorite_button, "setColorFilter", state.shuffleTintArgb)
        views.setContentDescription(R.id.widget_mini_favorite_button, state.shuffleContentDescription)
        views.setOnClickPendingIntent(
          R.id.widget_mini_favorite_button,
          buildWidgetActionPendingIntent(
            context,
            MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE,
            REQUEST_CODE_MINI_FAVORITE,
          ),
        )
      }
    } else {
      views.setViewVisibility(R.id.widget_mini_favorite_container, View.VISIBLE)
      views.setViewVisibility(R.id.widget_mini_favorite_bg, View.INVISIBLE)
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
    views.setOnClickPendingIntent(android.R.id.background, openIntent)
    views.setOnClickPendingIntent(R.id.widget_mini_row, openIntent)
    views.setOnClickPendingIntent(R.id.widget_mini_album_art, openIntent)
    views.setOnClickPendingIntent(R.id.widget_mini_text_container, openIntent)

    return views
  }

  /**
   * Builds the [RemoteViews] for the Medium 2-row Player widget ([MusicMediumWidgetProvider]):
   * - Evenly rounded corners (`16dp` radius)
   * - Album art taking the full height of the widget (`112dp` square), aligned all the way to the left,
   *   with corners rounded using the exact same radius as the widget corners so it fits seamlessly
   * - Top row to the right of the album art: Track title, artist/album subtitle, and Favorite star toggle
   * - Bottom row underneath the song info: Full 5 playback controls from the large player
   *   (Shuffle / External Left Action, Previous, Play/Pause, Next, Repeat / External Right Action)
   */
  fun buildMediumRemoteViews(
    context: Context,
    state: MusicPlayerWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_music_medium)
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val (widthDp, _) = resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 280f,
      defaultHeightDp = MEDIUM_WIDGET_HEIGHT_DP,
    )
    val heightDp = MEDIUM_WIDGET_HEIGHT_DP
    val rawWidthPx = (widthDp * density).coerceAtLeast(160f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(72f)
    val maxDimPx = 1200f
    val scaleFactor = if (rawWidthPx > maxDimPx) maxDimPx / rawWidthPx else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(160)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(72)
    val cornerRadiusPx = MEDIUM_WIDGET_CORNER_RADIUS_DP * density * scaleFactor

    renderMiniBarBackgroundBitmap(
      palette = state.palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = cornerRadiusPx,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
      backgroundTexture = state.backgroundTexture,
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_medium_background, bgBmp)
    }

    // Album art takes the full height of the medium card (1:1 square of bgHeightPx x bgHeightPx)
    // and its corners are rounded with the exact same radius as the widget corners.
    val thumbSizePx = bgHeightPx
    renderRoundedAlbumArtBitmap(
      sourceBitmap = state.artworkBitmap,
      palette = state.palette,
      widthPx = thumbSizePx,
      heightPx = thumbSizePx,
      cornerRadiusPx = cornerRadiusPx,
      borderWidthPx = 0f,
    )?.let { artBmp ->
      views.setImageViewBitmap(R.id.widget_medium_album_art, artBmp)
    }

    views.setTextViewText(R.id.widget_medium_title, state.title)
    views.setTextViewText(R.id.widget_medium_subtitle, state.miniBarSubtitle)

    views.setImageViewResource(R.id.widget_medium_favorite_bg, state.iconButtonBackgroundRes)
    views.setImageViewResource(R.id.widget_medium_shuffle_bg, state.iconButtonBackgroundRes)
    views.setImageViewResource(R.id.widget_medium_play_pause_bg, state.iconButtonBackgroundRes)
    views.setImageViewResource(R.id.widget_medium_repeat_bg, state.iconButtonBackgroundRes)

    // Favorite Star button in top info row (hidden when controlling external media, since external
    // custom actions are displayed in the 5-button playback row below)
    if (state.isExternalMedia) {
      views.setViewVisibility(R.id.widget_medium_favorite_container, View.GONE)
    } else {
      views.setViewVisibility(R.id.widget_medium_favorite_container, View.VISIBLE)
      views.setViewVisibility(R.id.widget_medium_favorite_bg, View.INVISIBLE)
      views.setViewVisibility(R.id.widget_medium_favorite_button, View.VISIBLE)
      views.setImageViewResource(R.id.widget_medium_favorite_button, state.favoriteIconRes)
      views.setInt(R.id.widget_medium_favorite_button, "setColorFilter", state.miniFavoriteTintArgb)
      views.setContentDescription(R.id.widget_medium_favorite_button, state.favoriteContentDescription)
      views.setOnClickPendingIntent(
        R.id.widget_medium_favorite_button,
        buildWidgetActionPendingIntent(
          context,
          MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE,
          REQUEST_CODE_MEDIUM_FAVORITE,
        ),
      )
    }

    val iconSizePx = (24f * density).roundToInt().coerceIn(32, 96)

    // 1. Left Control: Shuffle (internal) or Extracted Left Custom Action (external Android system media player)
    if (!state.isLeftControlVisible) {
      views.setViewVisibility(R.id.widget_medium_shuffle_container, View.INVISIBLE)
      views.setViewVisibility(R.id.widget_medium_shuffle_bg, View.INVISIBLE)
    } else {
      views.setViewVisibility(R.id.widget_medium_shuffle_container, View.VISIBLE)
      views.setViewVisibility(
        R.id.widget_medium_shuffle_bg,
        if (state.isShuffleHighlighted) View.VISIBLE else View.INVISIBLE,
      )
      views.setInt(R.id.widget_medium_shuffle_bg, "setColorFilter", state.shuffleHighlightBgArgb)
      val leftCustomBmp = state.leftCustomActionIconBitmap?.takeIf { !it.isRecycled }
      if (leftCustomBmp != null) {
        val tintedLeft = renderTintedActionIconBitmap(leftCustomBmp, state.shuffleTintArgb, iconSizePx)
        views.setImageViewBitmap(R.id.widget_medium_shuffle_button, tintedLeft ?: leftCustomBmp)
      } else {
        views.setImageViewResource(R.id.widget_medium_shuffle_button, state.shuffleIconRes)
      }
      views.setInt(R.id.widget_medium_shuffle_button, "setColorFilter", state.shuffleTintArgb)
      views.setContentDescription(R.id.widget_medium_shuffle_button, state.shuffleContentDescription)
      views.setOnClickPendingIntent(
        R.id.widget_medium_shuffle_button,
        buildWidgetActionPendingIntent(
          context,
          MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE,
          REQUEST_CODE_MEDIUM_SHUFFLE,
        ),
      )
    }

    // 2. Previous Control
    views.setImageViewResource(R.id.widget_medium_prev_button, R.drawable.ic_media_skip_previous)
    views.setInt(R.id.widget_medium_prev_button, "setColorFilter", LIGHT_PLAY_ICON_TINT_ARGB)
    views.setContentDescription(R.id.widget_medium_prev_button, "Previous track")
    views.setOnClickPendingIntent(
      R.id.widget_medium_prev_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_PREVIOUS,
        REQUEST_CODE_MEDIUM_PREV,
      ),
    )

    // 3. Play / Pause Control
    views.setInt(R.id.widget_medium_play_pause_bg, "setColorFilter", state.playPauseContainerArgb)
    views.setImageViewResource(R.id.widget_medium_play_pause_button, state.playPauseIconRes)
    views.setInt(R.id.widget_medium_play_pause_button, "setColorFilter", state.playPauseContentArgb)
    views.setContentDescription(R.id.widget_medium_play_pause_button, state.playPauseContentDescription)
    views.setOnClickPendingIntent(
      R.id.widget_medium_play_pause_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_PLAY_PAUSE,
        REQUEST_CODE_MEDIUM_PLAY_PAUSE,
      ),
    )

    // 4. Next Control
    views.setImageViewResource(R.id.widget_medium_next_button, R.drawable.ic_media_skip_next)
    views.setInt(R.id.widget_medium_next_button, "setColorFilter", LIGHT_PLAY_ICON_TINT_ARGB)
    views.setContentDescription(R.id.widget_medium_next_button, "Next track")
    views.setOnClickPendingIntent(
      R.id.widget_medium_next_button,
      buildWidgetActionPendingIntent(
        context,
        MusicPlayerEngine.ACTION_MEDIA_NEXT,
        REQUEST_CODE_MEDIUM_NEXT,
      ),
    )

    // 5. Right Control: Repeat (internal) or Extracted Right Custom Action (external Android system media player)
    if (!state.isRightControlVisible) {
      views.setViewVisibility(R.id.widget_medium_repeat_container, View.INVISIBLE)
      views.setViewVisibility(R.id.widget_medium_repeat_bg, View.INVISIBLE)
    } else {
      views.setViewVisibility(R.id.widget_medium_repeat_container, View.VISIBLE)
      views.setViewVisibility(
        R.id.widget_medium_repeat_bg,
        if (state.isRepeatHighlighted) View.VISIBLE else View.INVISIBLE,
      )
      views.setInt(R.id.widget_medium_repeat_bg, "setColorFilter", state.repeatHighlightBgArgb)
      val rightCustomBmp = state.rightCustomActionIconBitmap?.takeIf { !it.isRecycled }
      if (rightCustomBmp != null) {
        val tintedRight = renderTintedActionIconBitmap(rightCustomBmp, state.repeatTintArgb, iconSizePx)
        views.setImageViewBitmap(R.id.widget_medium_repeat_button, tintedRight ?: rightCustomBmp)
      } else {
        views.setImageViewResource(R.id.widget_medium_repeat_button, state.repeatIconRes)
      }
      views.setInt(R.id.widget_medium_repeat_button, "setColorFilter", state.repeatTintArgb)
      views.setContentDescription(R.id.widget_medium_repeat_button, state.repeatContentDescription)
      views.setOnClickPendingIntent(
        R.id.widget_medium_repeat_button,
        buildWidgetActionPendingIntent(
          context,
          MusicPlayerEngine.ACTION_MEDIA_CYCLE_REPEAT,
          REQUEST_CODE_MEDIUM_REPEAT,
        ),
      )
    }

    // Tapping the card, album art, or song text opens the expanded Music Player
    val openIntent = buildOpenMusicPlayerPendingIntent(context, REQUEST_CODE_MEDIUM_OPEN)
    views.setOnClickPendingIntent(android.R.id.background, openIntent)
    views.setOnClickPendingIntent(R.id.widget_medium_row, openIntent)
    views.setOnClickPendingIntent(R.id.widget_medium_album_art, openIntent)
    views.setOnClickPendingIntent(R.id.widget_medium_text_container, openIntent)

    return views
  }

  /**
   * Builds the [RemoteViews] for the Album Art & Full 5 Playback Controls widget
   * ([MusicAlbumArtWidgetProvider]), modeled after `ExpandedMusicPlayerOverlay` with:
   * - Album art at top
   * - Favorite star overlaid at the bottom-right corner of the album art
   * - Track title and artist/album below the album art
   * - Full 5 playback controls below (Shuffle / External Left Action, Previous, Play/Pause, Next,
   *   Repeat / External Right Action)
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

    val (widthDp, heightDp) = resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 240f,
      defaultHeightDp = 260f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(120f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(120f)
    val maxDimPx = 1200f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(120)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(120)

    renderAlbumArtWidgetBackgroundBitmap(
      palette = state.palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = ALBUM_WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
      backgroundTexture = state.backgroundTexture,
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_album_background, bgBmp)
    }

    // Render the album art as a strict 1:1 square fixed to the top with 8dp spacing on all sides
    val displayedArtSizeDp = (widthDp - 16f).coerceAtLeast(100f)
    val artSizePx = max(bgWidthPx, bgHeightPx).coerceIn(480, 1440)
    val artPxPerDp = artSizePx.toFloat() / displayedArtSizeDp

    renderRoundedAlbumArtBitmap(
      sourceBitmap = state.artworkBitmap,
      palette = state.palette,
      widthPx = artSizePx,
      heightPx = artSizePx,
      cornerRadiusPx = ALBUM_ART_CORNER_RADIUS_DP * artPxPerDp,
      borderWidthPx = max(1.5f, 1.5f * artPxPerDp),
    )?.let { artBmp ->
      views.setImageViewBitmap(R.id.widget_album_art, artBmp)
    }

    views.setImageViewResource(R.id.widget_album_favorite_bg, state.iconButtonBackgroundRes)
    views.setImageViewResource(R.id.widget_album_shuffle_bg, state.iconButtonBackgroundRes)
    views.setImageViewResource(R.id.widget_album_play_pause_bg, state.iconButtonBackgroundRes)
    views.setImageViewResource(R.id.widget_album_repeat_bg, state.iconButtonBackgroundRes)

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

    val iconSizePx = (24f * density).roundToInt().coerceIn(32, 96)

    // 1. Left Control: Shuffle (internal) or Extracted Left Custom Action (external Android system media player)
    if (!state.isLeftControlVisible) {
      views.setViewVisibility(R.id.widget_album_shuffle_container, View.INVISIBLE)
      views.setViewVisibility(R.id.widget_album_shuffle_bg, View.INVISIBLE)
    } else {
      views.setViewVisibility(R.id.widget_album_shuffle_container, View.VISIBLE)
      views.setViewVisibility(
        R.id.widget_album_shuffle_bg,
        if (state.isShuffleHighlighted) View.VISIBLE else View.INVISIBLE,
      )
      views.setInt(R.id.widget_album_shuffle_bg, "setColorFilter", state.shuffleHighlightBgArgb)
      val leftCustomBmp = state.leftCustomActionIconBitmap?.takeIf { !it.isRecycled }
      if (leftCustomBmp != null) {
        val tintedLeft = renderTintedActionIconBitmap(leftCustomBmp, state.shuffleTintArgb, iconSizePx)
        views.setImageViewBitmap(R.id.widget_album_shuffle_button, tintedLeft ?: leftCustomBmp)
      } else {
        views.setImageViewResource(R.id.widget_album_shuffle_button, state.shuffleIconRes)
      }
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
    }

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

    // 5. Right Control: Repeat (internal) or Extracted Right Custom Action (external Android system media player)
    if (!state.isRightControlVisible) {
      views.setViewVisibility(R.id.widget_album_repeat_container, View.INVISIBLE)
      views.setViewVisibility(R.id.widget_album_repeat_bg, View.INVISIBLE)
    } else {
      views.setViewVisibility(R.id.widget_album_repeat_container, View.VISIBLE)
      views.setViewVisibility(
        R.id.widget_album_repeat_bg,
        if (state.isRepeatHighlighted) View.VISIBLE else View.INVISIBLE,
      )
      views.setInt(R.id.widget_album_repeat_bg, "setColorFilter", state.repeatHighlightBgArgb)
      val rightCustomBmp = state.rightCustomActionIconBitmap?.takeIf { !it.isRecycled }
      if (rightCustomBmp != null) {
        val tintedRight = renderTintedActionIconBitmap(rightCustomBmp, state.repeatTintArgb, iconSizePx)
        views.setImageViewBitmap(R.id.widget_album_repeat_button, tintedRight ?: rightCustomBmp)
      } else {
        views.setImageViewResource(R.id.widget_album_repeat_button, state.repeatIconRes)
      }
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
    }

    // Tapping the widget background, album art, or song text opens the expanded Music Player
    val openIntent = buildOpenMusicPlayerPendingIntent(context, REQUEST_CODE_ALBUM_OPEN)
    views.setOnClickPendingIntent(android.R.id.background, openIntent)
    views.setOnClickPendingIntent(R.id.widget_album_content, openIntent)
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
    if (!internalPlaying && context != null && MusicPlayerEngine.externalSystemMediaInfo == null && MusicPlayerEngine.isMusicPlayingOverride != true) {
      try {
        MusicPlayerEngine.syncExternalSystemMediaSessions(context)
      } catch (_: Throwable) {}
    }
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
        if (context != null) TodoListWidgets.refreshAllToolboxWidgets(context)
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
      TodoListWidgets.refreshAllToolboxWidgets(context)
    }
    return handled
  }

  /**
   * Updates all pinned instances of the unified responsive Music Player widget
   * ([MusicAlbumArtWidgetProvider]) — plus any legacy [MusicMiniBarWidgetProvider] or
   * [MusicMediumWidgetProvider] instances — on the user's home screen, automatically selecting the
   * appropriate view ([MusicWidgetViewStyle.MINI_BAR], [MusicWidgetViewStyle.MEDIUM], or
   * [MusicWidgetViewStyle.ALBUM_ART]) for each widget instance's current size.
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val miniComponent = ComponentName(appCtx, MusicMiniBarWidgetProvider::class.java)
      val mediumComponent = ComponentName(appCtx, MusicMediumWidgetProvider::class.java)
      val albumComponent = ComponentName(appCtx, MusicAlbumArtWidgetProvider::class.java)
      val miniIds = try {
        manager.getAppWidgetIds(miniComponent) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      val mediumIds = try {
        manager.getAppWidgetIds(mediumComponent) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      val albumIds = try {
        manager.getAppWidgetIds(albumComponent) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (miniIds.isEmpty() && mediumIds.isEmpty() && albumIds.isEmpty()) return

      val state = resolveWidgetState(appCtx)
      if (miniIds.isNotEmpty()) {
        updateResponsiveWidgets(appCtx, manager, miniIds, state)
      }
      if (mediumIds.isNotEmpty()) {
        updateResponsiveWidgets(appCtx, manager, mediumIds, state)
      }
      if (albumIds.isNotEmpty()) {
        updateResponsiveWidgets(appCtx, manager, albumIds, state)
      }
    } catch (_: Throwable) {}
  }

  /**
   * No-op because the Mini Player widget no longer displays a seek/progress bar.
   */
  @Suppress("UNUSED_PARAMETER")
  fun updateMiniBarProgressOnly(
    context: Context?,
    progressPermille: Int,
  ) {
    // Seek bar display was removed from the mini widget.
  }

  /**
   * Updates the specified [appWidgetIds] using the size-responsive Music Player widget builder
   * ([buildResponsiveMusicWidgetRemoteViews]), automatically switching between Mini Bar, Medium
   * Player, and Album Art views based on each widget instance's dimensions.
   */
  fun updateResponsiveWidgets(
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
        val views = buildResponsiveMusicWidgetRemoteViews(
          context = context,
          state = state,
          options = options,
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
      } catch (_: Throwable) {}
    }
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
    updateResponsiveWidgets(context, appWidgetManager, appWidgetIds, state)
  }

  /**
   * Updates the specified [appWidgetIds] for [MusicMediumWidgetProvider].
   */
  fun updateMediumWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: MusicPlayerWidgetState = resolveWidgetState(context),
  ) {
    updateResponsiveWidgets(context, appWidgetManager, appWidgetIds, state)
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
    updateResponsiveWidgets(context, appWidgetManager, appWidgetIds, state)
  }
}

/**
 * Legacy Mini Bar widget provider retained as a thin delegate to [MusicPlayerWidgets.updateResponsiveWidgets].
 * The unified responsive Music Player widget registered in `AndroidManifest.xml` is [MusicAlbumArtWidgetProvider].
 */
class MusicMiniBarWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    MusicPlayerWidgets.updateResponsiveWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    MusicPlayerWidgets.updateResponsiveWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
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
 * Legacy Medium Player widget provider retained as a thin delegate to [MusicPlayerWidgets.updateResponsiveWidgets].
 * The unified responsive Music Player widget registered in `AndroidManifest.xml` is [MusicAlbumArtWidgetProvider].
 */
class MusicMediumWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    MusicPlayerWidgets.updateResponsiveWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    MusicPlayerWidgets.updateResponsiveWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
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
 * Unified, size-responsive Music Player Home Screen AppWidget:
 * Automatically switches between three views based on widget dimensions as the user resizes it:
 * - **Mini Bar** (`heightDp < 100dp`): Single-row horizontal bar with full-height left-aligned album art,
 *   song title & subtitle, favorite star, and Previous / Play-Pause / Next controls.
 * - **Medium Player** (`100dp <= heightDp < 260dp`, default `4x2`): Two-row horizontal card with
 *   full-height left-aligned album art, song info & favorite star on the top row, and the full 5
 *   playback controls underneath the song info.
 * - **Album Art Player** (`heightDp >= 260dp`): Large vertical card with top square album art and
 *   overlaid bottom-right favorite star, song title & artist/album, and the full 5 playback controls
 *   at the bottom (extracting the left and right custom playback buttons from the Android system media
 *   player when controlling external media).
 */
class MusicAlbumArtWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    MusicPlayerWidgets.updateResponsiveWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    MusicPlayerWidgets.updateResponsiveWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.music.ACTION_")) {
      MusicPlayerWidgets.handleWidgetAction(context, action)
    }
  }
}
