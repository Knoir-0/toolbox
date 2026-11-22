package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import com.example.toolbox.R
import com.example.toolbox.ui.main.createCameraModeQuickActionIntent
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

const val PREF_KEY_CAMERA_CAPTURE_MODE = "camera_selected_capture_mode"

/**
 * Snapshot of the Camera mode selection and Material 3 palette rendered by the compact Camera
 * Pill Bar Home Screen AppWidget ([CameraWidgetProvider]).
 */
@Immutable
data class CameraWidgetState(
  val activeMode: CameraCaptureMode,
  val modes: List<CameraCaptureMode>,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
)

/**
 * Central state resolver, pill bitmap renderer, and RemoteViews builder for the compact Camera
 * Pill Bar Home Screen AppWidget ([CameraWidgetProvider]):
 * - Displays a compact `4x1` pill bar with all 5 Camera capture modes:
 *   1. **Photo** ([CameraCaptureMode.PHOTO])
 *   2. **Video** ([CameraCaptureMode.VIDEO])
 *   3. **Scan** ([CameraCaptureMode.SCANNER])
 *   4. **Note** ([CameraCaptureMode.NOTE])
 *   5. **Vision** ([CameraCaptureMode.VISION])
 * - Highlights the active / last-used mode in `primaryContainer` / `primary` and respects the
 *   **Hexagonify** setting (rendering sharp-edged horizontal hexagon pills when enabled).
 * - Tapping any pill opens the **Camera** tool directly in that capture mode.
 */
object CameraWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.camera.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_SELECT_MODE = "com.example.toolbox.camera.ACTION_WIDGET_SELECT_MODE"
  const val EXTRA_CAPTURE_MODE = "extra_camera_widget_capture_mode"

  const val WIDGET_CORNER_RADIUS_DP = 16f
  const val PILL_CORNER_RADIUS_DP = 14f

  private const val REQUEST_CODE_MODE_BASE = 7100

  @Volatile
  private var inMemoryActiveMode: CameraCaptureMode? = null

  /**
   * Loads the active / last-used [CameraCaptureMode], defaulting to [CameraCaptureMode.PHOTO].
   */
  fun loadActiveCaptureMode(context: Context? = null): CameraCaptureMode {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(CAMERA_PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(PREF_KEY_CAMERA_CAPTURE_MODE, null)
        if (!saved.isNullOrBlank()) {
          val parsed = CameraCaptureMode.fromNameOrDefault(saved)
          inMemoryActiveMode = parsed
          return parsed
        }
      } catch (_: Throwable) {}
    }
    return inMemoryActiveMode ?: CameraCaptureMode.DEFAULT
  }

  /**
   * Persists the active [CameraCaptureMode] and refreshes any pinned Camera widgets.
   */
  fun saveActiveCaptureMode(
    context: Context?,
    mode: CameraCaptureMode,
    refreshWidgets: Boolean = true,
  ) {
    inMemoryActiveMode = mode
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(CAMERA_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_CAMERA_CAPTURE_MODE, mode.name).apply()
      } catch (_: Throwable) {}
      if (refreshWidgets) {
        updateAllWidgets(context)
      }
    }
  }

  fun clearInMemoryModeForTesting() {
    inMemoryActiveMode = null
  }

  /**
   * Resolves the current [CameraWidgetState] from persisted Camera preferences and the user's
   * active Material 3 theme palette.
   */
  fun resolveWidgetState(
    context: Context? = null,
    activeModeOverride: CameraCaptureMode? = null,
  ): CameraWidgetState {
    val activeMode = activeModeOverride ?: loadActiveCaptureMode(context)
    return CameraWidgetState(
      activeMode = activeMode,
      modes = CameraCaptureMode.entries,
      palette = TodoListWidgets.resolveWidgetPalette(context),
    )
  }

  /**
   * Renders a mode pill background [Bitmap] for a single camera mode slot inside the compact pill bar.
   * When [hexagonShape] is `true`, renders a sharp-edged horizontal hexagon matching
   * `HorizontalHexagonShape` in `ToolboxPillNavigationItem`.
   */
  fun renderModePillBitmap(
    fillArgb: Int,
    borderArgb: Int,
    widthPx: Int = 140,
    heightPx: Int = 110,
    cornerRadiusPx: Float = 28f,
    borderWidthPx: Float = 2f,
    hexagonShape: Boolean = false,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(60, 600)
    val safeH = heightPx.coerceIn(48, 400)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = fillArgb
      }

      if (hexagonShape) {
        val inset = max(borderWidthPx, 1f)
        val taper = min(w * 0.20f, h * 0.36f)
        val path = Path().apply {
          moveTo(inset + taper, inset)
          lineTo(w - inset - taper, inset)
          lineTo(w - inset, h * 0.5f)
          lineTo(w - inset - taper, h - inset)
          lineTo(inset + taper, h - inset)
          lineTo(inset, h * 0.5f)
          close()
        }
        canvas.drawPath(path, fillPaint)
        if (borderWidthPx > 0f) {
          val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = borderWidthPx
            color = borderArgb
          }
          canvas.drawPath(path, borderPaint)
        }
      } else {
        val fillRect = RectF(0f, 0f, w, h)
        val radius = min(cornerRadiusPx, min(w, h) * 0.5f)
        canvas.drawRoundRect(fillRect, radius, radius, fillPaint)
        if (borderWidthPx > 0f) {
          val inset = max(borderWidthPx, 1f)
          val borderRect = RectF(inset, inset, w - inset, h - inset)
          val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = borderWidthPx
            color = borderArgb
          }
          canvas.drawRoundRect(
            borderRect,
            max(0f, radius - inset * 0.5f),
            max(0f, radius - inset * 0.5f),
            borderPaint,
          )
        }
      }
      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  fun buildOpenCameraModePendingIntent(
    context: Context,
    mode: CameraCaptureMode,
  ): PendingIntent {
    val intent = createCameraModeQuickActionIntent(context, mode.name)
    return PendingIntent.getActivity(
      context,
      REQUEST_CODE_MODE_BASE + mode.ordinal,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the full [RemoteViews] for a [CameraWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: CameraWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_camera_pill_bar)
    val palette = state.palette
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val (widthDp, heightDp) = MusicPlayerWidgets.resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 280f,
      defaultHeightDp = 72f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(180f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(100f)
    val maxDimPx = 1000f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(180)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(100)

    TodoListWidgets.renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_camera_background, bgBmp)
    }

    val pillWidthPx = ((bgWidthPx - (24f * density * scaleFactor)) / 5f).roundToInt().coerceAtLeast(64)
    val pillHeightPx = (bgHeightPx - (12f * density * scaleFactor)).roundToInt().coerceAtLeast(52)
    val pillCornerPx = PILL_CORNER_RADIUS_DP * density * scaleFactor
    val pillBorderPx = max(1f, 1f * density * scaleFactor)

    val selectedPillBmp = renderModePillBitmap(
      fillArgb = palette.primaryContainerArgb,
      borderArgb = palette.primaryArgb,
      widthPx = pillWidthPx,
      heightPx = pillHeightPx,
      cornerRadiusPx = pillCornerPx,
      borderWidthPx = pillBorderPx,
      hexagonShape = state.hexagonIconButtons,
    )
    val unselectedPillBmp = renderModePillBitmap(
      fillArgb = palette.itemCardArgb,
      borderArgb = palette.borderArgb,
      widthPx = pillWidthPx,
      heightPx = pillHeightPx,
      cornerRadiusPx = pillCornerPx,
      borderWidthPx = pillBorderPx,
      hexagonShape = state.hexagonIconButtons,
    )

    data class ModeSlotIds(
      val mode: CameraCaptureMode,
      val containerId: Int,
      val bgId: Int,
      val iconId: Int,
      val labelId: Int,
    )

    val slots = listOf(
      ModeSlotIds(
        mode = CameraCaptureMode.PHOTO,
        containerId = R.id.widget_camera_mode_photo,
        bgId = R.id.widget_camera_mode_photo_bg,
        iconId = R.id.widget_camera_mode_photo_icon,
        labelId = R.id.widget_camera_mode_photo_label,
      ),
      ModeSlotIds(
        mode = CameraCaptureMode.VIDEO,
        containerId = R.id.widget_camera_mode_video,
        bgId = R.id.widget_camera_mode_video_bg,
        iconId = R.id.widget_camera_mode_video_icon,
        labelId = R.id.widget_camera_mode_video_label,
      ),
      ModeSlotIds(
        mode = CameraCaptureMode.SCANNER,
        containerId = R.id.widget_camera_mode_scan,
        bgId = R.id.widget_camera_mode_scan_bg,
        iconId = R.id.widget_camera_mode_scan_icon,
        labelId = R.id.widget_camera_mode_scan_label,
      ),
      ModeSlotIds(
        mode = CameraCaptureMode.NOTE,
        containerId = R.id.widget_camera_mode_note,
        bgId = R.id.widget_camera_mode_note_bg,
        iconId = R.id.widget_camera_mode_note_icon,
        labelId = R.id.widget_camera_mode_note_label,
      ),
      ModeSlotIds(
        mode = CameraCaptureMode.VISION,
        containerId = R.id.widget_camera_mode_vision,
        bgId = R.id.widget_camera_mode_vision_bg,
        iconId = R.id.widget_camera_mode_vision_icon,
        labelId = R.id.widget_camera_mode_vision_label,
      ),
    )

    for (slot in slots) {
      val isSelected = slot.mode == state.activeMode
      val pillBmp = if (isSelected) selectedPillBmp else unselectedPillBmp
      if (pillBmp != null) {
        views.setImageViewBitmap(slot.bgId, pillBmp)
      }
      views.setInt(
        slot.iconId,
        "setColorFilter",
        if (isSelected) palette.primaryArgb else palette.onSurfaceVariantArgb,
      )
      views.setTextViewText(slot.labelId, slot.mode.displayName)
      views.setTextColor(
        slot.labelId,
        if (isSelected) palette.onPrimaryContainerArgb else palette.onSurfaceArgb,
      )
      views.setOnClickPendingIntent(
        slot.containerId,
        buildOpenCameraModePendingIntent(context, slot.mode),
      )
    }

    return views
  }

  /**
   * Handles a widget broadcast action for [CameraWidgetProvider].
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
      ACTION_WIDGET_SELECT_MODE -> {
        val rawMode = intent.getStringExtra(EXTRA_CAPTURE_MODE)
        val mode = CameraCaptureMode.fromNameOrDefault(rawMode)
        saveActiveCaptureMode(context, mode, refreshWidgets = true)
        CameraEngine.requestCaptureMode(mode)
        if (context != null) {
          try {
            context.startActivity(createCameraModeQuickActionIntent(context, mode.name))
          } catch (_: Throwable) {}
        }
        true
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [CameraWidgetProvider].
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, CameraWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateCameraWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updateCameraWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: CameraWidgetState = resolveWidgetState(context),
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
 * Compact Camera Pill Bar Home Screen AppWidget for launching Camera directly into Photo, Video,
 * Scan (QR/Barcode), Note (OCR), or Vision (ML Kit).
 */
class CameraWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    CameraWidgets.updateCameraWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    CameraWidgets.updateCameraWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.camera.ACTION_")) {
      CameraWidgets.handleWidgetAction(context, intent)
    }
  }
}
