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
import com.example.toolbox.ui.main.AUDIO_RECORDER_QUICK_ACTION_START_BOTH
import com.example.toolbox.ui.main.AUDIO_RECORDER_QUICK_ACTION_START_DEVICE
import com.example.toolbox.ui.main.AUDIO_RECORDER_QUICK_ACTION_START_MIC
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
 * - Displays a compact card with the active recording format (`M4A`, `WAV`, `AAC`, `OGG`, `3GP`)
 *   and destination folder (`/Audio`, `/Recordings`, etc.).
 * - Tapping the left badge or title opens the **Record** tool ([Tool.AUDIO_RECORDER]).
 * - Features all 3 recording buttons from [AudioRecorderScreen] on the right:
 *   1. **Device + Mic** ([AudioCaptureSource.DEVICE_AND_MIC]) — smaller left button (`secondaryContainer`)
 *   2. **Microphone** ([AudioCaptureSource.MICROPHONE]) — larger center primary button (`primary`)
 *   3. **Device Audio** ([AudioCaptureSource.DEVICE_AUDIO]) — smaller right button (`secondaryContainer`)
 */
object AudioRecorderWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.audiorecorder.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_OPEN_RECORDER = "com.example.toolbox.audiorecorder.ACTION_WIDGET_OPEN_RECORDER"
  const val ACTION_WIDGET_START_RECORDING = "com.example.toolbox.audiorecorder.ACTION_WIDGET_START_RECORDING"
  const val EXTRA_CAPTURE_SOURCE = "extra_audio_recorder_widget_capture_source"

  const val WIDGET_CORNER_RADIUS_DP = 16f

  private const val REQUEST_CODE_OPEN_RECORDER = 5100
  private const val REQUEST_CODE_START_MIC_RECORDING = 5101
  private const val REQUEST_CODE_START_BOTH_RECORDING = 5102
  private const val REQUEST_CODE_START_DEVICE_RECORDING = 5103

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
    widthPx: Int = 440,
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

  fun quickActionModeForSource(source: AudioCaptureSource): String = when (source) {
    AudioCaptureSource.MICROPHONE -> AUDIO_RECORDER_QUICK_ACTION_START_MIC
    AudioCaptureSource.DEVICE_AUDIO -> AUDIO_RECORDER_QUICK_ACTION_START_DEVICE
    AudioCaptureSource.DEVICE_AND_MIC -> AUDIO_RECORDER_QUICK_ACTION_START_BOTH
  }

  fun buildOpenRecorderPendingIntent(
    context: Context,
    startRecording: Boolean = false,
    source: AudioCaptureSource = AudioCaptureSource.MICROPHONE,
  ): PendingIntent {
    val intent = if (startRecording) {
      createAudioRecorderQuickActionIntent(
        context = context,
        startRecording = true,
        quickActionMode = quickActionModeForSource(source),
      )
    } else {
      createStandaloneToolIntent(context, Tool.AUDIO_RECORDER)
    }
    val requestCode = if (!startRecording) {
      REQUEST_CODE_OPEN_RECORDER
    } else {
      when (source) {
        AudioCaptureSource.MICROPHONE -> REQUEST_CODE_START_MIC_RECORDING
        AudioCaptureSource.DEVICE_AND_MIC -> REQUEST_CODE_START_BOTH_RECORDING
        AudioCaptureSource.DEVICE_AUDIO -> REQUEST_CODE_START_DEVICE_RECORDING
      }
    }
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
      defaultWidthDp = 240f,
      defaultHeightDp = 72f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(140f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(100f)
    val maxDimPx = 900f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(140)
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

    // 1. Left: Smaller Device + Mic (Both) button
    views.setImageViewResource(R.id.widget_audio_recorder_both_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_audio_recorder_both_button_bg, "setColorFilter", palette.secondaryContainerArgb)
    views.setInt(R.id.widget_audio_recorder_both_button_icon, "setColorFilter", palette.onSecondaryContainerArgb)

    // 2. Middle: Primary Larger Microphone button
    views.setImageViewResource(R.id.widget_audio_recorder_record_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_audio_recorder_record_button_bg, "setColorFilter", palette.primaryArgb)
    views.setInt(R.id.widget_audio_recorder_record_button_icon, "setColorFilter", palette.onPrimaryArgb)

    // 3. Right: Smaller Device Audio button
    views.setImageViewResource(R.id.widget_audio_recorder_device_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_audio_recorder_device_button_bg, "setColorFilter", palette.secondaryContainerArgb)
    views.setInt(R.id.widget_audio_recorder_device_button_icon, "setColorFilter", palette.onSecondaryContainerArgb)

    val openIntent = buildOpenRecorderPendingIntent(context, startRecording = false)
    val recordBothIntent = buildOpenRecorderPendingIntent(
      context = context,
      startRecording = true,
      source = AudioCaptureSource.DEVICE_AND_MIC,
    )
    val recordMicIntent = buildOpenRecorderPendingIntent(
      context = context,
      startRecording = true,
      source = AudioCaptureSource.MICROPHONE,
    )
    val recordDeviceIntent = buildOpenRecorderPendingIntent(
      context = context,
      startRecording = true,
      source = AudioCaptureSource.DEVICE_AUDIO,
    )

    views.setOnClickPendingIntent(R.id.widget_audio_recorder_root, openIntent)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_badge, openIntent)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_text_container, openIntent)

    views.setOnClickPendingIntent(R.id.widget_audio_recorder_both_button, recordBothIntent)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_both_button_icon, recordBothIntent)

    views.setOnClickPendingIntent(R.id.widget_audio_recorder_record_button, recordMicIntent)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_record_button_icon, recordMicIntent)

    views.setOnClickPendingIntent(R.id.widget_audio_recorder_device_button, recordDeviceIntent)
    views.setOnClickPendingIntent(R.id.widget_audio_recorder_device_button_icon, recordDeviceIntent)

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
        val rawSource = intent.getStringExtra(EXTRA_CAPTURE_SOURCE)
        val source = AudioCaptureSource.fromNameOrDefault(rawSource)
        AudioRecorderEngine.requestQuickRecord(startRecording = true, source = source)
        if (context != null) {
          try {
            context.startActivity(
              createAudioRecorderQuickActionIntent(
                context = context,
                startRecording = true,
                quickActionMode = quickActionModeForSource(source),
              )
            )
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
