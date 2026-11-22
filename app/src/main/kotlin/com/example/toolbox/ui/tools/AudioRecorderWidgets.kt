package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import com.example.toolbox.R
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.createAudioRecorderQuickActionIntent
import com.example.toolbox.ui.main.createStandaloneToolIntent
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Snapshot of the Audio Recorder configuration and Material 3 palette rendered by the compact
 * Record Home Screen AppWidget ([AudioRecorderWidgetProvider]).
 */
@Immutable
data class AudioRecorderWidgetState(
  val selectedFormat: AudioRecordingFormat,
  val directoryDisplayPath: String,
  val title: String,
  val subtitle: String,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
  val iconButtonBackgroundRes: Int = palette.iconButtonBackgroundRes,
)

/**
 * Central state resolver, RemoteViews builder, and action handler for the compact Record Home Screen
 * AppWidget ([AudioRecorderWidgetProvider]):
 * - Displays a compact `2x1` card with the active recording format (`M4A`, `WAV`, `AAC`, `OGG`, `3GP`)
 *   and destination folder (`/Audio`, `/Recordings`, etc.).
 * - Tapping the card opens the **Record** tool ([Tool.AUDIO_RECORDER]).
 * - Tapping the trailing circular/hexagon **Mic** button opens **Record** and immediately begins
 *   capturing a timestamped voice memo.
 */
object AudioRecorderWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.audiorecorder.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_OPEN_RECORDER = "com.example.toolbox.audiorecorder.ACTION_WIDGET_OPEN_RECORDER"
  const val ACTION_WIDGET_START_RECORDING = "com.example.toolbox.audiorecorder.ACTION_WIDGET_START_RECORDING"

  const val WIDGET_CORNER_RADIUS_DP = 16f

  private const val REQUEST_CODE_OPEN_RECORDER = 5100
  private const val REQUEST_CODE_START_RECORDING = 5101

  /**
   * Resolves the current [AudioRecorderWidgetState] from persisted Audio Recorder preferences and
   * the user's active Material 3 theme palette.
   */
  fun resolveWidgetState(context: Context? = null): AudioRecorderWidgetState {
    val prefs = try {
      context?.getSharedPreferences(AUDIO_RECORDER_PREFS_NAME, Context.MODE_PRIVATE)
    } catch (_: Throwable) {
      null
    }
    val formatName = prefs?.getString(PREF_KEY_AUDIO_FORMAT, null)
    val format = AudioRecordingFormat.fromNameOrDefault(formatName)
    val savedDir = prefs?.getString(PREF_KEY_AUDIO_DIRECTORY, null)
      ?.trim()
      ?.takeIf { it.isNotEmpty() }
      ?: DEFAULT_AUDIO_FOLDER_DISPLAY
    val normalizedDir = if (savedDir.startsWith("/")) savedDir else "/$savedDir"
    val subtitle = "${format.shortLabel} • $normalizedDir"
    val palette = TodoListWidgets.resolveWidgetPalette(context)

    return AudioRecorderWidgetState(
      selectedFormat = format,
      directoryDisplayPath = normalizedDir,
      title = "Record",
      subtitle = subtitle,
      palette = palette,
    )
  }

  fun renderWidgetBackgroundBitmap(
    palette: TodoWidgetPalette,
    widthPx: Int = 360,
    heightPx: Int = 144,
    cornerRadiusPx: Float = 32f,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    return TodoListWidgets.renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = widthPx,
      heightPx = heightPx,
      cornerRadiusPx = cornerRadiusPx,
      borderWidthPx = borderWidthPx,
    )
  }

  fun buildOpenRecorderPendingIntent(
    context: Context,
    startRecording: Boolean = false,
  ): PendingIntent {
    val intent = if (startRecording) {
      createAudioRecorderQuickActionIntent(context, startRecording = true)
    } else {
      createStandaloneToolIntent(context, Tool.AUDIO_RECORDER)
    }
    val requestCode = if (startRecording) REQUEST_CODE_START_RECORDING else REQUEST_CODE_OPEN_RECORDER
    return PendingIntent.getActivity(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the [RemoteViews] for an [AudioRecorderWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: AudioRecorderWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_audio_recorder)
    val palette = state.palette
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val (widthDp, heightDp) = MusicPlayerWidgets.resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 180f,
      defaultHeightDp = 72f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(120f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(100f)
    val maxDimPx = 900f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(120)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(100)

    renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_audio_recorder_background, bgBmp)
    }

    // Leading icon badge
    views.setImageViewResource(R.id.widget_audio_recorder_badge_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_audio_recorder_badge_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_audio_recorder_badge_icon, "setColorFilter", palette.primaryArgb)

    // Title & subtitle
    views.setTextViewText(R.id.widget_audio_recorder_title, state.title)
    views.setTextColor(R.id.widget_audio_recorder_title, palette.onSurfaceArgb)
    views.setTextViewText(R.id.widget_audio_recorder_subtitle, state.subtitle)
    views.setTextColor(R.id.widget_audio_recorder_subtitle, palette.onSurfaceVariantArgb)

    // Trailing 1-tap Record button
    views.setImageViewResource(R.id.widget_audio_recorder_record_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_audio_recorder_record_button_bg, "setColorFilter", palette.primaryArgb)
    views.setInt(R.id.widget_audio_recorder_record_button_icon, "setColorFilter", palette.onPrimaryArgb)

    val openIntent = buildOpenRecorderPendingIntent(context, startRecording = false)
    val recordIntent = buildOpenRecorderPendingIntent(context, startRecording = true)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_root, openIntent)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_badge, openIntent)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_text_container, openIntent)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_record_button, recordIntent)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_record_button_icon, recordIntent)

    return views
  }

  /**
   * Handles a widget broadcast action for [AudioRecorderWidgetProvider].
   */
  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
  ): Boolean {
    val action = intent?.action ?: return false
    return when (action) {
      ACTION_WIDGET_REFRESH -> {
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_OPEN_RECORDER -> {
        if (context != null) {
          try {
            context.startActivity(createStandaloneToolIntent(context, Tool.AUDIO_RECORDER))
          } catch (_: Throwable) {}
        }
        true
      }
      ACTION_WIDGET_START_RECORDING -> {
        AudioRecorderEngine.requestQuickRecord(startRecording = true)
        if (context != null) {
          try {
            context.startActivity(createAudioRecorderQuickActionIntent(context, startRecording = true))
          } catch (_: Throwable) {}
        }
        true
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [AudioRecorderWidgetProvider].
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, AudioRecorderWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateAudioRecorderWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updateAudioRecorderWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: AudioRecorderWidgetState = resolveWidgetState(context),
  ) {
    for (appWidgetId in appWidgetIds) {
      try {
        val options = try {
          appWidgetManager.getAppWidgetOptions(appWidgetId)
        } catch (_: Throwable) {
          null
        }
        val views = buildWidgetRemoteViews(
          context = context,
          appWidgetId = appWidgetId,
          state = state,
          options = options,
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
      } catch (_: Throwable) {}
    }
  }
}

/**
 * Compact Record Home Screen AppWidget for the Audio Recorder tool.
 */
class AudioRecorderWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    AudioRecorderWidgets.updateAudioRecorderWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    AudioRecorderWidgets.updateAudioRecorderWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.audiorecorder.ACTION_")) {
      AudioRecorderWidgets.handleWidgetAction(context, intent)
    }
  }
}
