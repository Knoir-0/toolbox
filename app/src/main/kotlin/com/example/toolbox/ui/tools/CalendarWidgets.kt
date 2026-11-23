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
import com.example.toolbox.ui.main.createCalendarWidgetIntent
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Snapshot of the day's schedule rendered by the Calendar Home Screen AppWidget ([CalendarWidgetProvider]).
 */
@Immutable
data class CalendarWidgetState(
  val focusedDay: CalendarDay,
  val today: CalendarDay,
  val isToday: Boolean,
  val occurrences: List<CalendarEventOccurrence>,
  val headerTitle: String,
  val headerSubtitle: String,
  val dayNumberText: String,
  val dayOfWeekText: String,
  val agendaSummaryText: String,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
  val iconButtonBackgroundRes: Int = palette.iconButtonBackgroundRes,
)

/**
 * Central state resolver, day navigation handler, and RemoteViews builder for the Calendar
 * Home Screen AppWidget ([CalendarWidgetProvider]):
 * - Surfaces the focused day's Google Calendar schedule (events, all-day holidays, and scheduled To-Do tasks)
 *   directly on the home screen.
 * - Includes `<` (Previous Day), `Today` reset, `>` (Next Day), and `+` (New Event) controls.
 * - Strictly follows `DESIGN_LANGUAGE.md` Section 5.8 (`16.dp` outer corner radius,
 *   `@android:id/background` + `clipToOutline="true"` launcher opt-out, inset `1.dp` border stroke,
 *   and automatic Hexagonify badge support).
 */
object CalendarWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.calendar.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_PREV_DAY = "com.example.toolbox.calendar.ACTION_WIDGET_PREV_DAY"
  const val ACTION_WIDGET_NEXT_DAY = "com.example.toolbox.calendar.ACTION_WIDGET_NEXT_DAY"
  const val ACTION_WIDGET_TODAY = "com.example.toolbox.calendar.ACTION_WIDGET_TODAY"

  const val KEY_WIDGET_FOCUSED_DATE_KEY = "calendar_widget_focused_date_key"
  const val WIDGET_CORNER_RADIUS_DP = 16f

  private const val REQUEST_CODE_OPEN_CALENDAR_BASE = 9600
  private const val REQUEST_CODE_ADD_EVENT_BASE = 9700
  private const val REQUEST_CODE_PREV_DAY_BASE = 9800
  private const val REQUEST_CODE_NEXT_DAY_BASE = 9900
  private const val REQUEST_CODE_TODAY_BASE = 9950

  @Volatile
  private var inMemoryFocusedDay: CalendarDay? = null

  fun clearInMemoryStateForTesting() {
    inMemoryFocusedDay = null
  }

  fun loadFocusedDay(
    context: Context? = null,
    defaultDay: CalendarDay = CalendarDay.today(),
  ): CalendarDay {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(CALENDAR_PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_WIDGET_FOCUSED_DATE_KEY, null)
        val parsed = CalendarDay.fromDateKeyOrNull(raw)
        if (parsed != null) {
          inMemoryFocusedDay = parsed
          return parsed
        }
      } catch (_: Throwable) {}
    }
    return inMemoryFocusedDay ?: defaultDay
  }

  fun saveFocusedDay(
    context: Context?,
    day: CalendarDay,
  ): CalendarDay {
    inMemoryFocusedDay = day
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(CALENDAR_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WIDGET_FOCUSED_DATE_KEY, day.dateKey).apply()
      } catch (_: Throwable) {}
    }
    return day
  }

  fun stepFocusedDay(
    context: Context?,
    deltaDays: Int,
    currentDayOverride: CalendarDay? = null,
  ): CalendarDay {
    val current = currentDayOverride ?: loadFocusedDay(context)
    val next = current.plusDays(deltaDays)
    return saveFocusedDay(context, next)
  }

  fun resetFocusedDayToToday(
    context: Context?,
    today: CalendarDay = CalendarDay.today(),
  ): CalendarDay {
    return saveFocusedDay(context, today)
  }

  fun formatAgendaLines(occurrences: List<CalendarEventOccurrence>): String {
    if (occurrences.isEmpty()) {
      return "No events scheduled • Tap + to create an event"
    }
    return occurrences.take(5).joinToString("\n") { occ ->
      val ev = occ.event
      val timePrefix = if (ev.isAllDay) {
        "All day"
      } else {
        CalendarEngine.formatTime(occ.instanceStartMillis)
      }
      val locSuffix = if (ev.location.isNotBlank() && !ev.isReadOnlyBuiltIn) " (${ev.location})" else ""
      "• $timePrefix — ${ev.title}$locSuffix"
    }.let { text ->
      if (occurrences.size > 5) {
        "$text\n+${occurrences.size - 5} more scheduled"
      } else {
        text
      }
    }
  }

  fun resolveWidgetState(
    context: Context? = null,
    calendarRepository: CalendarRepository? = context?.let { SharedPreferencesCalendarRepository(it) },
    todoRepository: TodoListRepository? = context?.let { SharedPreferencesTodoListRepository(it) },
    focusedDayOverride: CalendarDay? = null,
    todayOverride: CalendarDay = CalendarDay.today(),
  ): CalendarWidgetState {
    val today = todayOverride
    val focusedDay = focusedDayOverride ?: loadFocusedDay(context, defaultDay = today)
    val isToday = focusedDay == today

    val userEvents = calendarRepository?.loadEvents().orEmpty()
    val enabledCategories = calendarRepository?.getEnabledCategories() ?: CalendarCategory.entries.toSet()
    val showHolidays = calendarRepository?.getShowHolidays() ?: true
    val showTodos = calendarRepository?.getShowTodos() ?: true

    val holidayEvents = if (showHolidays) {
      CalendarEngine.builtInHolidaysForYear(focusedDay.year)
    } else {
      emptyList()
    }
    val todoEvents = if (showTodos && todoRepository != null) {
      CalendarEngine.projectTodoTasksToCalendarEvents(
        tasks = todoRepository.loadTasks(),
        completedTasks = todoRepository.loadCompletedTasks(getTodayDateKey()),
      )
    } else {
      emptyList()
    }

    val allEvents = userEvents + todoEvents + holidayEvents
    val occurrences = CalendarEngine.occurrencesForDay(
      events = allEvents,
      day = focusedDay,
      enabledCategories = enabledCategories,
    )

    val headerTitle = if (isToday) {
      "Today • ${focusedDay.monthShortName} ${focusedDay.dayOfMonth}"
    } else {
      "${focusedDay.dayOfWeekShortLabel}, ${focusedDay.monthShortName} ${focusedDay.dayOfMonth}, ${focusedDay.year}"
    }
    val countLabel = when (val c = occurrences.size) {
      0 -> "No events"
      1 -> "1 event scheduled"
      else -> "$c events scheduled"
    }
    val headerSubtitle = "${focusedDay.monthFullName} ${focusedDay.year} • $countLabel"

    return CalendarWidgetState(
      focusedDay = focusedDay,
      today = today,
      isToday = isToday,
      occurrences = occurrences,
      headerTitle = headerTitle,
      headerSubtitle = headerSubtitle,
      dayNumberText = focusedDay.dayOfMonth.toString(),
      dayOfWeekText = focusedDay.dayOfWeekShortLabel.uppercase(),
      agendaSummaryText = formatAgendaLines(occurrences),
      palette = TodoListWidgets.resolveWidgetPalette(context),
    )
  }

  fun renderWidgetBackgroundBitmap(
    palette: TodoWidgetPalette,
    widthPx: Int = 520,
    heightPx: Int = 260,
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

  fun buildOpenCalendarPendingIntent(
    context: Context,
    appWidgetId: Int,
    day: CalendarDay,
    openAddEvent: Boolean = false,
  ): PendingIntent {
    val intent = createCalendarWidgetIntent(
      context = context,
      dateKey = day.dateKey,
      openAddEvent = openAddEvent,
    )
    val base = if (openAddEvent) REQUEST_CODE_ADD_EVENT_BASE else REQUEST_CODE_OPEN_CALENDAR_BASE
    return PendingIntent.getActivity(
      context,
      base + (appWidgetId and 0xFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildDayActionBroadcastPendingIntent(
    context: Context,
    appWidgetId: Int,
    action: String,
    requestCodeBase: Int,
  ): PendingIntent {
    val intent = Intent(context, CalendarWidgetProvider::class.java).apply {
      this.action = action
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setPackage(context.packageName)
    }
    return PendingIntent.getBroadcast(
      context,
      requestCodeBase + (appWidgetId and 0xFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: CalendarWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_calendar)
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
      defaultHeightDp = 150f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(160f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(110f)
    val maxDimPx = 1000f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(160)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(110)

    renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_calendar_background, bgBmp)
    }

    // Header date badge (circle or vertical-sided hexagon when Hexagonify is enabled)
    views.setImageViewResource(R.id.widget_calendar_date_badge_bg, state.iconButtonBackgroundRes)
    views.setInt(
      R.id.widget_calendar_date_badge_bg,
      "setColorFilter",
      if (state.isToday) palette.primaryArgb else palette.primaryContainerArgb,
    )
    views.setTextViewText(R.id.widget_calendar_date_badge_number, state.dayNumberText)
    views.setTextColor(
      R.id.widget_calendar_date_badge_number,
      if (state.isToday) palette.onPrimaryArgb else palette.onPrimaryContainerArgb,
    )

    // Header title & subtitle
    views.setTextViewText(R.id.widget_calendar_title, state.headerTitle)
    views.setTextColor(R.id.widget_calendar_title, palette.onSurfaceArgb)
    views.setTextViewText(R.id.widget_calendar_subtitle, state.headerSubtitle)
    views.setTextColor(R.id.widget_calendar_subtitle, palette.onSurfaceVariantArgb)

    // Navigation & Add event buttons
    views.setInt(R.id.widget_calendar_prev_day, "setColorFilter", palette.onSurfaceVariantArgb)
    views.setInt(R.id.widget_calendar_next_day, "setColorFilter", palette.onSurfaceVariantArgb)
    views.setImageViewResource(R.id.widget_calendar_add_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_calendar_add_button_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_calendar_add_button_icon, "setColorFilter", palette.onPrimaryContainerArgb)

    // Agenda card content
    val cardWidthPx = (bgWidthPx - (24f * density * scaleFactor)).roundToInt().coerceAtLeast(120)
    val cardHeightPx = (bgHeightPx - (64f * density * scaleFactor)).roundToInt().coerceAtLeast(70)
    PlantScheduleWidgets.renderColumnCardBitmap(
      fillArgb = palette.itemCardArgb,
      borderArgb = palette.borderArgb,
      widthPx = cardWidthPx,
      heightPx = cardHeightPx,
      cornerRadiusPx = 14f * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { cardBmp ->
      views.setImageViewBitmap(R.id.widget_calendar_agenda_card_bg, cardBmp)
    }

    views.setTextViewText(R.id.widget_calendar_agenda_list, state.agendaSummaryText)
    views.setTextColor(
      R.id.widget_calendar_agenda_list,
      if (state.occurrences.isEmpty()) palette.onSurfaceVariantArgb else palette.onSurfaceArgb,
    )

    // Click PendingIntents
    val openDayIntent = buildOpenCalendarPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      day = state.focusedDay,
      openAddEvent = false,
    )
    val addEventIntent = buildOpenCalendarPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      day = state.focusedDay,
      openAddEvent = true,
    )
    val prevDayIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_PREV_DAY,
      requestCodeBase = REQUEST_CODE_PREV_DAY_BASE,
    )
    val nextDayIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_NEXT_DAY,
      requestCodeBase = REQUEST_CODE_NEXT_DAY_BASE,
    )
    val todayIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_TODAY,
      requestCodeBase = REQUEST_CODE_TODAY_BASE,
    )

    views.setOnClickPendingIntent(R.id.widget_calendar_date_badge, todayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_header_text_container, openDayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_agenda_card, openDayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_prev_day, prevDayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_next_day, nextDayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_add_button, addEventIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_add_button_icon, addEventIntent)

    return views
  }

  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
  ): Boolean = handleWidgetAction(
    context = context,
    action = intent?.action,
  )

  fun handleWidgetAction(
    context: Context?,
    action: String?,
  ): Boolean {
    if (action.isNullOrBlank()) return false
    return when (action) {
      ACTION_WIDGET_REFRESH -> {
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_PREV_DAY -> {
        stepFocusedDay(context, deltaDays = -1)
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_NEXT_DAY -> {
        stepFocusedDay(context, deltaDays = 1)
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_TODAY -> {
        resetFocusedDayToToday(context)
        if (context != null) updateAllWidgets(context)
        true
      }
      else -> false
    }
  }

  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, CalendarWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateCalendarWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updateCalendarWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: CalendarWidgetState = resolveWidgetState(context),
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
 * Calendar Home Screen AppWidget displaying the focused day's Google Calendar schedule,
 * `<` / `>` day stepper, Today badge reset, and `+` quick add event trigger.
 */
class CalendarWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    CalendarWidgets.updateCalendarWidgets(
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
    val state = CalendarWidgets.resolveWidgetState(context)
    val views = CalendarWidgets.buildWidgetRemoteViews(
      context = context,
      appWidgetId = appWidgetId,
      state = state,
      options = newOptions,
    )
    appWidgetManager.updateAppWidget(appWidgetId, views)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    CalendarWidgets.handleWidgetAction(context, intent)
  }
}
