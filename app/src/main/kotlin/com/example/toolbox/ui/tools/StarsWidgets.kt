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
import com.example.toolbox.ui.main.createStandaloneToolIntent
import java.util.Calendar
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Snapshot of the Stars daily cosmic weather and natal Big Three rendered by the
 * Stars Home Screen AppWidget ([StarsWidgetProvider]).
 */
@Immutable
data class StarsWidgetState(
  val profileName: String,
  val activeSign: ZodiacSign,
  val sunSign: ZodiacSign,
  val moonSign: ZodiacSign,
  val risingSign: ZodiacSign,
  val vibeWord: String,
  val mantraText: String,
  val moonBadgeText: String,
  val subtextLine: String,
  val isMercuryRetrograde: Boolean,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
  val iconButtonBackgroundRes: Int = palette.iconButtonBackgroundRes,
)

/**
 * Central state resolver, RemoteViews builder, and action handler for the Stars Home Screen
 * AppWidget ([StarsWidgetProvider]):
 * - Displays the active sign glyph badge on the left, daily cosmic vibe + moon phase + mantra in the center,
 *   Big Three & lucky number subtext below, and a cycle-sign button on the right.
 * - Adheres to `DESIGN_LANGUAGE.md` Section 5.8 (`16.dp` outer corner radius, `@android:id/background`
 *   with `clipToOutline="true"`, inset `1.dp` border stroke, and automatic Hexagonify badge support).
 */
object StarsWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.stars.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_CYCLE_SIGN = "com.example.toolbox.stars.ACTION_WIDGET_CYCLE_SIGN"

  const val KEY_WIDGET_SIGN_OVERRIDE = "stars_widget_sign_override"
  const val WIDGET_CORNER_RADIUS_DP = 16f

  private const val REQUEST_CODE_OPEN_STARS = 9400
  private const val REQUEST_CODE_CYCLE_SIGN = 9401

  private var inMemoryOverrideSign: ZodiacSign? = null

  fun clearInMemoryStateForTesting() {
    inMemoryOverrideSign = null
  }

  fun loadOverrideSign(context: Context? = null): ZodiacSign? {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(STARS_PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_WIDGET_SIGN_OVERRIDE, null)
        if (!raw.isNullOrBlank()) {
          val sign = ZodiacSign.entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
          if (sign != null) {
            inMemoryOverrideSign = sign
            return sign
          }
        }
      } catch (_: Throwable) {}
    }
    return inMemoryOverrideSign
  }

  fun saveOverrideSign(context: Context?, sign: ZodiacSign?) {
    inMemoryOverrideSign = sign
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(STARS_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (sign == null) {
          editor.remove(KEY_WIDGET_SIGN_OVERRIDE)
        } else {
          editor.putString(KEY_WIDGET_SIGN_OVERRIDE, sign.name)
        }
        editor.apply()
      } catch (_: Throwable) {}
    }
  }

  /**
   * Resolves the current [StarsWidgetState] from persisted birth profiles (or the current birth sign
   * when no profiles are filled out) and today's real sky transits.
   */
  fun resolveWidgetState(
    context: Context? = null,
    repository: StarsRepository? = null,
    calendar: Calendar = Calendar.getInstance(),
  ): StarsWidgetState {
    val repo = repository ?: context?.let {
      try {
        SharedPreferencesStarsRepository(it)
      } catch (_: Throwable) {
        null
      }
    } ?: InMemoryStarsRepository()

    val currentBirthSign = StarsEngine.getCurrentZodiacSign(calendar)
    val profiles = repo.loadProfiles()
    val activeId = repo.getActiveProfileId()
    val activeProfile = profiles.firstOrNull { it.id == activeId } ?: profiles.firstOrNull()
    val natalChart = activeProfile?.let { StarsEngine.calculateNatalChart(it) }

    val displaySign = loadOverrideSign(context) ?: natalChart?.sunSign ?: currentBirthSign
    val reading = StarsEngine.generateDailyHoroscope(
      year = calendar.get(Calendar.YEAR),
      month = calendar.get(Calendar.MONTH) + 1,
      day = calendar.get(Calendar.DAY_OF_MONTH),
      targetSign = displaySign,
      natalChart = natalChart,
    )

    val sunSign = natalChart?.sunSign ?: displaySign
    val moonSign = natalChart?.moonSign ?: reading.moonSnapshot.moonSign
    val risingSign = natalChart?.risingSign ?: displaySign

    val rxSuffix = if (reading.isMercuryRetrograde) " • ☿ Rx" else ""
    val moonBadge = "${reading.moonSnapshot.phaseType.emoji} ${reading.moonSnapshot.phaseType.displayName} (${reading.moonSnapshot.moonSign.glyph})$rxSuffix"
    val subtext = if (natalChart != null) {
      "☉ ${natalChart.sunSign.displayName} • ☽ ${natalChart.moonSign.displayName} • ↑ ${natalChart.risingSign.displayName} • Lucky #${reading.luckyNumber}"
    } else {
      "☉ ${displaySign.displayName} (${displaySign.dateRange}) • ☽ ${reading.moonSnapshot.moonSign.displayName} Moon • Lucky #${reading.luckyNumber}"
    }

    val palette = TodoListWidgets.resolveWidgetPalette(context)
    return StarsWidgetState(
      profileName = activeProfile?.name ?: displaySign.displayName,
      activeSign = displaySign,
      sunSign = sunSign,
      moonSign = moonSign,
      risingSign = risingSign,
      vibeWord = reading.cosmicVibeWord,
      mantraText = reading.headlineMantra,
      moonBadgeText = moonBadge,
      subtextLine = subtext,
      isMercuryRetrograde = reading.isMercuryRetrograde,
      palette = palette,
    )
  }

  /**
   * Cycles the displayed widget sign to the next zodiac sign and refreshes all Stars widgets.
   */
  fun cycleWidgetSign(
    context: Context? = null,
    repository: StarsRepository? = null,
    refreshWidgets: Boolean = true,
  ): ZodiacSign {
    val currentState = resolveWidgetState(context = context, repository = repository)
    val nextSign = ZodiacSign.entries[(currentState.activeSign.ordinal + 1) % ZodiacSign.entries.size]
    saveOverrideSign(context, nextSign)
    if (refreshWidgets && context != null) {
      updateAllWidgets(context)
    }
    return nextSign
  }

  fun renderWidgetBackgroundBitmap(
    palette: TodoWidgetPalette,
    widthPx: Int = 520,
    heightPx: Int = 160,
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

  fun buildOpenStarsToolPendingIntent(context: Context): PendingIntent {
    val intent = createStandaloneToolIntent(context, Tool.STARS)
    return PendingIntent.getActivity(
      context,
      REQUEST_CODE_OPEN_STARS,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildCycleSignPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, StarsWidgetProvider::class.java).apply {
      action = ACTION_WIDGET_CYCLE_SIGN
    }
    return PendingIntent.getBroadcast(
      context,
      REQUEST_CODE_CYCLE_SIGN,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the [RemoteViews] for a [StarsWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: StarsWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_stars)
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
      defaultHeightDp = 84f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(160f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(100f)
    val maxDimPx = 900f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(160)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(100)

    renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_stars_background, bgBmp)
    }

    // Left sign glyph badge (circle or hexagon when Hexagonify is enabled)
    views.setImageViewResource(R.id.widget_stars_sign_badge_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_stars_sign_badge_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setTextViewText(R.id.widget_stars_sign_glyph, state.activeSign.glyph)
    views.setTextColor(R.id.widget_stars_sign_glyph, palette.onPrimaryContainerArgb)

    // Center title, moon phase pill, mantra, and Big Three subtext
    views.setTextViewText(R.id.widget_stars_title, "${state.activeSign.displayName} • ${state.vibeWord}")
    views.setTextColor(R.id.widget_stars_title, palette.onSurfaceArgb)

    views.setTextViewText(R.id.widget_stars_moon_pill, state.moonBadgeText)
    views.setTextColor(R.id.widget_stars_moon_pill, palette.primaryArgb)

    views.setTextViewText(R.id.widget_stars_mantra, "“${state.mantraText}”")
    views.setTextColor(R.id.widget_stars_mantra, palette.onSurfaceArgb)

    views.setTextViewText(R.id.widget_stars_subtext, state.subtextLine)
    views.setTextColor(R.id.widget_stars_subtext, palette.onSurfaceVariantArgb)

    // Right cycle-sign button
    views.setImageViewResource(R.id.widget_stars_next_sign_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_stars_next_sign_button_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_stars_next_sign_button_icon, "setColorFilter", palette.primaryArgb)

    val openIntent = buildOpenStarsToolPendingIntent(context)
    val cycleIntent = buildCycleSignPendingIntent(context)

    views.setOnClickPendingIntent(R.id.widget_stars_root, openIntent)
    views.setOnClickPendingIntent(R.id.widget_stars_sign_badge, openIntent)
    views.setOnClickPendingIntent(R.id.widget_stars_text_container, openIntent)
    views.setOnClickPendingIntent(R.id.widget_stars_next_sign_button, cycleIntent)
    views.setOnClickPendingIntent(R.id.widget_stars_next_sign_button_icon, cycleIntent)

    return views
  }

  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
    repository: StarsRepository? = null,
  ): Boolean = handleWidgetAction(
    context = context,
    action = intent?.action,
    repository = repository,
  )

  fun handleWidgetAction(
    context: Context?,
    action: String?,
    repository: StarsRepository? = null,
  ): Boolean {
    if (action.isNullOrBlank()) return false
    return when (action) {
      ACTION_WIDGET_REFRESH -> {
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_CYCLE_SIGN -> {
        cycleWidgetSign(
          context = context,
          repository = repository,
          refreshWidgets = true,
        )
        true
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [StarsWidgetProvider].
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, StarsWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateStarsWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updateStarsWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: StarsWidgetState = resolveWidgetState(context),
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
 * Stars Home Screen AppWidget displaying daily horoscope mantra, Moon phase, Big Three summary,
 * and one-tap sign cycling.
 */
class StarsWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    StarsWidgets.updateStarsWidgets(
      context = context,
      appWidgetManager = appWidgetManager,
      appWidgetIds = appWidgetIds,
    )
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    val state = StarsWidgets.resolveWidgetState(context)
    val views = StarsWidgets.buildWidgetRemoteViews(
      context = context,
      appWidgetId = appWidgetId,
      state = state,
      options = newOptions,
    )
    appWidgetManager.updateAppWidget(appWidgetId, views)
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (StarsWidgets.handleWidgetAction(context, intent)) {
      return
    }
    super.onReceive(context, intent)
  }
}
