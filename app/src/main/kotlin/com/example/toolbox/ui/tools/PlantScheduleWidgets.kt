package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import com.example.toolbox.R
import com.example.toolbox.ui.main.createPlantScheduleWidgetIntent
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Snapshot of the Monthly Planting & Harvest Schedule rendered by the Planting Schedule
 * Home Screen AppWidget ([PlantScheduleWidgetProvider]).
 */
@Immutable
data class PlantScheduleWidgetState(
  val month: CalendarMonth,
  val monthNumber: Int,
  val scheduledPlants: List<PlantItem>,
  val plantingThisMonth: List<PlantItem>,
  val harvestingThisMonth: List<PlantItem>,
  val headerTitle: String,
  val headerSubtitle: String,
  val plantingCountText: String,
  val harvestCountText: String,
  val plantingListText: String,
  val harvestListText: String,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
  val iconButtonBackgroundRes: Int = palette.iconButtonBackgroundRes,
)

/**
 * Central state resolver, month navigation handler, and RemoteViews builder for the Monthly Planting
 * & Harvest Schedule Home Screen AppWidget ([PlantScheduleWidgetProvider]):
 * - Surfaces the monthly planting and harvest schedule card from [PlantScheduleScreen] on the home screen.
 * - Highlights planting items in the active Theme color (`primary` / `primaryContainer`) and
 *   harvesting items in the active Accent color (`tertiary` / `tertiaryContainer`).
 * - Includes `<` (Previous Month) and `>` (Next Month) controls to browse any month of the year
 *   directly on the home screen, plus a `+` button that opens **Plants** directly into the plant
 *   catalog search overlay.
 */
object PlantScheduleWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.plants.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_PREV_MONTH = "com.example.toolbox.plants.ACTION_WIDGET_PREV_MONTH"
  const val ACTION_WIDGET_NEXT_MONTH = "com.example.toolbox.plants.ACTION_WIDGET_NEXT_MONTH"
  const val ACTION_WIDGET_SET_MONTH = "com.example.toolbox.plants.ACTION_WIDGET_SET_MONTH"

  const val EXTRA_MONTH_NUMBER = "extra_plant_widget_month_number"

  const val KEY_WIDGET_SELECTED_MONTH = "plant_widget_selected_month"
  const val WIDGET_CORNER_RADIUS_DP = 16f
  const val COLUMN_CORNER_RADIUS_DP = 12f

  private const val REQUEST_CODE_OPEN_MONTH_BASE = 6100
  private const val REQUEST_CODE_OPEN_SEARCH_BASE = 6200
  private const val REQUEST_CODE_PREV_MONTH_BASE = 6300
  private const val REQUEST_CODE_NEXT_MONTH_BASE = 6400

  @Volatile
  private var inMemorySelectedMonth: Int? = null

  /**
   * Loads the currently focused month (`1..12`) for the Planting Schedule widget, defaulting to
   * [CalendarMonth.currentMonthNumber].
   */
  fun loadSelectedMonth(
    context: Context? = null,
    defaultMonth: Int = CalendarMonth.currentMonthNumber(),
  ): Int {
    val fallback = defaultMonth.coerceIn(1, 12)
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(
          SharedPreferencesPlantScheduleRepository.PREFS_NAME,
          Context.MODE_PRIVATE,
        )
        if (prefs.contains(KEY_WIDGET_SELECTED_MONTH)) {
          val saved = prefs.getInt(KEY_WIDGET_SELECTED_MONTH, fallback)
          if (saved in 1..12) {
            inMemorySelectedMonth = saved
            return saved
          }
        }
      } catch (_: Throwable) {}
    }
    return inMemorySelectedMonth?.takeIf { it in 1..12 } ?: fallback
  }

  /**
   * Saves the focused month (`1..12`) for the Planting Schedule widget.
   */
  fun saveSelectedMonth(
    context: Context?,
    monthNumber: Int,
  ): Int {
    val coerced = monthNumber.coerceIn(1, 12)
    inMemorySelectedMonth = coerced
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(
          SharedPreferencesPlantScheduleRepository.PREFS_NAME,
          Context.MODE_PRIVATE,
        )
        prefs.edit().putInt(KEY_WIDGET_SELECTED_MONTH, coerced).apply()
      } catch (_: Throwable) {}
    }
    return coerced
  }

  /**
   * Steps the widget's focused month backward (`delta = -1`) or forward (`delta = +1`) with
   * wrap-around between January (`1`) and December (`12`).
   */
  fun stepSelectedMonth(
    context: Context?,
    delta: Int,
    currentMonthOverride: Int? = null,
  ): Int {
    val current = (currentMonthOverride ?: loadSelectedMonth(context)).coerceIn(1, 12)
    val zeroBased = ((current - 1 + delta) % 12 + 12) % 12
    val nextMonth = zeroBased + 1
    return saveSelectedMonth(context, nextMonth)
  }

  fun clearInMemoryMonthForTesting() {
    inMemorySelectedMonth = null
  }

  /**
   * Formats the bulleted plant list displayed inside the Plant or Harvest column of the widget.
   */
  fun formatColumnPlantList(
    plants: List<PlantItem>,
    emptyMessage: String,
  ): String {
    if (plants.isEmpty()) return emptyMessage
    return plants.joinToString("\n") { plant ->
      "• ${plant.name}"
    }
  }

  /**
   * Resolves the current [PlantScheduleWidgetState] from [repository] and the active Material 3 palette.
   */
  fun resolveWidgetState(
    context: Context? = null,
    repository: PlantScheduleRepository? = context?.let { SharedPreferencesPlantScheduleRepository(it) },
    monthNumberOverride: Int? = null,
  ): PlantScheduleWidgetState {
    val monthNum = (monthNumberOverride ?: loadSelectedMonth(context)).coerceIn(1, 12)
    val month = CalendarMonth.fromNumber(monthNum)
    val scheduledPlants = repository?.loadScheduledPlants().orEmpty()
    val plantingThisMonth = PlantScheduleEngine.plantsForPlantingInMonth(scheduledPlants, monthNum)
    val harvestingThisMonth = PlantScheduleEngine.plantsForHarvestInMonth(scheduledPlants, monthNum)

    val headerTitle = month.fullName
    val headerSubtitle = if (scheduledPlants.isEmpty()) {
      "No plants scheduled • Tap + to add"
    } else {
      "${plantingThisMonth.size} to plant • ${harvestingThisMonth.size} to harvest"
    }

    val plantingListText = formatColumnPlantList(
      plants = plantingThisMonth,
      emptyMessage = "No scheduled plantings",
    )
    val harvestListText = formatColumnPlantList(
      plants = harvestingThisMonth,
      emptyMessage = "No scheduled harvests",
    )

    return PlantScheduleWidgetState(
      month = month,
      monthNumber = monthNum,
      scheduledPlants = scheduledPlants,
      plantingThisMonth = plantingThisMonth,
      harvestingThisMonth = harvestingThisMonth,
      headerTitle = headerTitle,
      headerSubtitle = headerSubtitle,
      plantingCountText = plantingThisMonth.size.toString(),
      harvestCountText = harvestingThisMonth.size.toString(),
      plantingListText = plantingListText,
      harvestListText = harvestListText,
      palette = TodoListWidgets.resolveWidgetPalette(context),
    )
  }

  private fun blendArgb(baseArgb: Int, overlayArgb: Int, overlayFraction: Float): Int {
    val t = overlayFraction.coerceIn(0f, 1f)
    val inv = 1f - t
    val baseR = (baseArgb ushr 16) and 0xFF
    val baseG = (baseArgb ushr 8) and 0xFF
    val baseB = baseArgb and 0xFF
    val overR = (overlayArgb ushr 16) and 0xFF
    val overG = (overlayArgb ushr 8) and 0xFF
    val overB = overlayArgb and 0xFF
    val r = (baseR * inv + overR * t).roundToInt().coerceIn(0, 255)
    val g = (baseG * inv + overG * t).roundToInt().coerceIn(0, 255)
    val b = (baseB * inv + overB * t).roundToInt().coerceIn(0, 255)
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
  }

  private fun withAlpha(argb: Int, alphaFraction: Float): Int {
    val a = (alphaFraction.coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255)
    return (argb and 0x00FFFFFF) or (a shl 24)
  }

  /**
   * Renders a `12.dp` rounded inner column card bitmap (used for the theme-colored Planting column
   * and accent-colored Harvest column).
   */
  fun renderColumnCardBitmap(
    fillArgb: Int,
    borderArgb: Int,
    widthPx: Int = 240,
    heightPx: Int = 200,
    cornerRadiusPx: Float = 24f,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(80, 900)
    val safeH = heightPx.coerceIn(60, 900)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()
      val fillRect = RectF(0f, 0f, w, h)
      val radius = min(cornerRadiusPx, min(w, h) * 0.5f)

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = fillArgb
      }
      canvas.drawRoundRect(fillRect, radius, radius, fillPaint)

      if (borderWidthPx > 0f) {
        val inset = max(borderWidthPx, 1f)
        val borderRect = RectF(inset, inset, w - inset, h - inset)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = borderWidthPx
          color = borderArgb
        }
        canvas.drawRoundRect(borderRect, max(0f, radius - inset * 0.5f), max(0f, radius - inset * 0.5f), borderPaint)
      }
      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  fun buildOpenPlantSchedulePendingIntent(
    context: Context,
    appWidgetId: Int,
    monthNumber: Int,
    openCatalogSearch: Boolean = false,
  ): PendingIntent {
    val intent = createPlantScheduleWidgetIntent(
      context = context,
      monthNumber = monthNumber,
      openCatalogSearch = openCatalogSearch,
    )
    val base = if (openCatalogSearch) REQUEST_CODE_OPEN_SEARCH_BASE else REQUEST_CODE_OPEN_MONTH_BASE
    return PendingIntent.getActivity(
      context,
      base + (appWidgetId and 0xFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildMonthStepBroadcastPendingIntent(
    context: Context,
    appWidgetId: Int,
    isNext: Boolean,
  ): PendingIntent {
    val intent = Intent(context, PlantScheduleWidgetProvider::class.java).apply {
      action = if (isNext) ACTION_WIDGET_NEXT_MONTH else ACTION_WIDGET_PREV_MONTH
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setPackage(context.packageName)
    }
    val base = if (isNext) REQUEST_CODE_NEXT_MONTH_BASE else REQUEST_CODE_PREV_MONTH_BASE
    return PendingIntent.getBroadcast(
      context,
      base + (appWidgetId and 0xFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the full [RemoteViews] for a [PlantScheduleWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: PlantScheduleWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_plant_schedule)
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
      defaultHeightDp = 160f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(160f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(110f)
    val maxDimPx = 1000f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(160)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(110)

    TodoListWidgets.renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_plant_schedule_background, bgBmp)
    }

    // Header badge & title
    views.setImageViewResource(R.id.widget_plant_schedule_header_badge_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_plant_schedule_header_badge_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_plant_schedule_header_badge_icon, "setColorFilter", palette.primaryArgb)
    views.setTextViewText(R.id.widget_plant_schedule_title, state.headerTitle)
    views.setTextColor(R.id.widget_plant_schedule_title, palette.onSurfaceArgb)
    views.setTextViewText(R.id.widget_plant_schedule_subtitle, state.headerSubtitle)
    views.setTextColor(R.id.widget_plant_schedule_subtitle, palette.onSurfaceVariantArgb)

    // Month step buttons & Add plant button
    views.setInt(R.id.widget_plant_schedule_prev_month, "setColorFilter", palette.onSurfaceVariantArgb)
    views.setInt(R.id.widget_plant_schedule_next_month, "setColorFilter", palette.onSurfaceVariantArgb)
    views.setImageViewResource(R.id.widget_plant_schedule_add_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_plant_schedule_add_button_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_plant_schedule_add_button_icon, "setColorFilter", palette.onPrimaryContainerArgb)

    // Render Plant & Harvest column background bitmaps matching MonthActivitySummaryCard
    val colWidthPx = ((bgWidthPx - (30f * density * scaleFactor)) * 0.5f).roundToInt().coerceAtLeast(100)
    val colHeightPx = (bgHeightPx - (64f * density * scaleFactor)).roundToInt().coerceAtLeast(70)
    val colCornerPx = COLUMN_CORNER_RADIUS_DP * density * scaleFactor
    val colBorderPx = max(1f, 1f * density * scaleFactor)

    val plantingFillArgb = blendArgb(palette.surfaceArgb, palette.primaryContainerArgb, 0.48f)
    val plantingBorderArgb = withAlpha(palette.primaryArgb, 0.38f)
    renderColumnCardBitmap(
      fillArgb = plantingFillArgb,
      borderArgb = plantingBorderArgb,
      widthPx = colWidthPx,
      heightPx = colHeightPx,
      cornerRadiusPx = colCornerPx,
      borderWidthPx = colBorderPx,
    )?.let { bmp ->
      views.setImageViewBitmap(R.id.widget_plant_schedule_planting_bg, bmp)
    }

    val harvestFillArgb = blendArgb(palette.surfaceArgb, palette.tertiaryContainerArgb, 0.48f)
    val harvestBorderArgb = withAlpha(palette.onTertiaryContainerArgb, 0.35f)
    renderColumnCardBitmap(
      fillArgb = harvestFillArgb,
      borderArgb = harvestBorderArgb,
      widthPx = colWidthPx,
      heightPx = colHeightPx,
      cornerRadiusPx = colCornerPx,
      borderWidthPx = colBorderPx,
    )?.let { bmp ->
      views.setImageViewBitmap(R.id.widget_plant_schedule_harvest_bg, bmp)
    }

    // Plant column text & colors
    views.setInt(R.id.widget_plant_schedule_planting_dot, "setColorFilter", palette.primaryArgb)
    views.setTextColor(R.id.widget_plant_schedule_planting_header, palette.onPrimaryContainerArgb)
    views.setTextViewText(R.id.widget_plant_schedule_planting_count, state.plantingCountText)
    views.setTextColor(R.id.widget_plant_schedule_planting_count, palette.primaryArgb)
    views.setTextViewText(R.id.widget_plant_schedule_planting_list, state.plantingListText)
    views.setTextColor(
      R.id.widget_plant_schedule_planting_list,
      if (state.plantingThisMonth.isEmpty()) palette.onSurfaceVariantArgb else palette.onSurfaceArgb,
    )

    // Harvest column text & colors
    views.setInt(R.id.widget_plant_schedule_harvest_dot, "setColorFilter", palette.onTertiaryContainerArgb)
    views.setTextColor(R.id.widget_plant_schedule_harvest_header, palette.onTertiaryContainerArgb)
    views.setTextViewText(R.id.widget_plant_schedule_harvest_count, state.harvestCountText)
    views.setTextColor(R.id.widget_plant_schedule_harvest_count, palette.onTertiaryContainerArgb)
    views.setTextViewText(R.id.widget_plant_schedule_harvest_list, state.harvestListText)
    views.setTextColor(
      R.id.widget_plant_schedule_harvest_list,
      if (state.harvestingThisMonth.isEmpty()) palette.onSurfaceVariantArgb else palette.onSurfaceArgb,
    )

    // Click listeners
    val openMonthIntent = buildOpenPlantSchedulePendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      monthNumber = state.monthNumber,
      openCatalogSearch = false,
    )
    val openSearchIntent = buildOpenPlantSchedulePendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      monthNumber = state.monthNumber,
      openCatalogSearch = true,
    )
    views.setOnClickPendingIntent(R.id.widget_plant_schedule_header_badge, openMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_plant_schedule_header_text_container, openMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_plant_schedule_planting_column, openMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_plant_schedule_harvest_column, openMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_plant_schedule_add_button, openSearchIntent)
    views.setOnClickPendingIntent(R.id.widget_plant_schedule_add_button_icon, openSearchIntent)
    views.setOnClickPendingIntent(
      R.id.widget_plant_schedule_prev_month,
      buildMonthStepBroadcastPendingIntent(context, appWidgetId, isNext = false),
    )
    views.setOnClickPendingIntent(
      R.id.widget_plant_schedule_next_month,
      buildMonthStepBroadcastPendingIntent(context, appWidgetId, isNext = true),
    )

    return views
  }

  /**
   * Handles a widget broadcast action for [PlantScheduleWidgetProvider].
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
      ACTION_WIDGET_PREV_MONTH -> {
        stepSelectedMonth(context, delta = -1)
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_NEXT_MONTH -> {
        stepSelectedMonth(context, delta = 1)
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_SET_MONTH -> {
        val month = intent.getIntExtra(EXTRA_MONTH_NUMBER, -1)
        if (month in 1..12) {
          saveSelectedMonth(context, month)
          if (context != null) updateAllWidgets(context)
          true
        } else {
          false
        }
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [PlantScheduleWidgetProvider].
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, PlantScheduleWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updatePlantScheduleWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updatePlantScheduleWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: PlantScheduleWidgetState = resolveWidgetState(context),
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
 * Monthly Planting & Harvest Schedule Home Screen AppWidget for the Plants tool.
 */
class PlantScheduleWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    PlantScheduleWidgets.updatePlantScheduleWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    PlantScheduleWidgets.updatePlantScheduleWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.plants.ACTION_")) {
      PlantScheduleWidgets.handleWidgetAction(context, intent)
    }
  }
}
