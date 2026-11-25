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
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import com.example.toolbox.R
import com.example.toolbox.ui.main.createCalendarWidgetIntent
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * A single row item inside the scrollable [ListView] of the Calendar Schedule Home Screen AppWidget
 * ([CalendarWidgetProvider]), matching the Google Calendar Schedule (Agenda) view in [CalendarScreen]:
 * - Left Date Column (weekday + circular/hexagonal date badge, shown on the first item of each day)
 * - Right 14.dp Event Card (or empty "No events scheduled for Today" card when [occurrence] is `null`)
 */
@Immutable
data class CalendarWidgetScheduleItem(
  val day: CalendarDay,
  val isToday: Boolean,
  val showDateBadge: Boolean,
  val occurrence: CalendarEventOccurrence?,
) {
  val itemId: String
    get() = occurrence?.instanceId ?: "empty_${day.dateKey}"
}

/**
 * Snapshot of the upcoming schedule rendered by the Calendar Schedule Home Screen AppWidget ([CalendarWidgetProvider]).
 */
@Immutable
data class CalendarWidgetState(
  val focusedDay: CalendarDay,
  val today: CalendarDay,
  val isToday: Boolean,
  val occurrences: List<CalendarEventOccurrence>,
  val scheduleItems: List<CalendarWidgetScheduleItem> = emptyList(),
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
 * Snapshot of the monthly 6×7 calendar grid rendered by the Calendar Month Home Screen AppWidget ([CalendarMonthWidgetProvider]).
 */
@Immutable
data class CalendarMonthWidgetState(
  val focusedMonthDay: CalendarDay,
  val year: Int,
  val month: Int,
  val today: CalendarDay,
  val isCurrentMonth: Boolean,
  val gridDays: List<CalendarDay>,
  val occurrencesByDay: Map<CalendarDay, List<CalendarEventOccurrence>>,
  val totalMonthOccurrences: Int,
  val headerTitle: String,
  val headerSubtitle: String,
  val badgeDayNumberText: String,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
  val iconButtonBackgroundRes: Int = palette.iconButtonBackgroundRes,
)

/**
 * Central state resolver, day & month navigation handler, and RemoteViews builder for the two
 * Calendar Home Screen AppWidgets:
 * 1. **Calendar Schedule** ([CalendarWidgetProvider]): Surfaces the focused day's Google Calendar schedule
 *    (events, all-day holidays, and scheduled To-Do tasks) with `<` (Previous Day), `Today` reset,
 *    `>` (Next Day), and `+` (New Event) controls. Opens Calendar in [CalendarViewMode.SCHEDULE].
 * 2. **Calendar Month** ([CalendarMonthWidgetProvider]): Surfaces the full 6×7 Sunday-aligned monthly
 *    grid with weekday headers, highlighted Today badge, and colored event dots per day cell, paired with
 *    `<` (Previous Month), `Today` reset, `>` (Next Month), and `+` (New Event) controls. Opens Calendar
 *    in [CalendarViewMode.MONTH].
 *
 * Strictly follows `DESIGN_LANGUAGE.md` Section 5.8 (`16.dp` outer corner radius,
 * `@android:id/background` + `clipToOutline="true"` launcher opt-out, inset `1.dp` border stroke,
 * and automatic Hexagonify badge support).
 */
object CalendarWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.calendar.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_PREV_DAY = "com.example.toolbox.calendar.ACTION_WIDGET_PREV_DAY"
  const val ACTION_WIDGET_NEXT_DAY = "com.example.toolbox.calendar.ACTION_WIDGET_NEXT_DAY"
  const val ACTION_WIDGET_TODAY = "com.example.toolbox.calendar.ACTION_WIDGET_TODAY"
  const val ACTION_WIDGET_ITEM_CLICK = "com.example.toolbox.calendar.ACTION_WIDGET_ITEM_CLICK"

  const val EXTRA_EVENT_ID = "extra_calendar_widget_event_id"
  const val EXTRA_DATE_KEY = "extra_calendar_widget_date_key"
  const val EXTRA_ITEM_CLICK_ACTION = "extra_calendar_widget_item_click_action"
  const val ITEM_CLICK_ACTION_OPEN_EVENT = "open_event"
  const val ITEM_CLICK_ACTION_OPEN_DAY = "open_day"
  const val ITEM_CLICK_ACTION_TOGGLE_FAVORITE = "toggle_favorite"

  const val ACTION_MONTH_WIDGET_REFRESH = "com.example.toolbox.calendar.ACTION_MONTH_WIDGET_REFRESH"
  const val ACTION_MONTH_WIDGET_PREV_MONTH = "com.example.toolbox.calendar.ACTION_MONTH_WIDGET_PREV_MONTH"
  const val ACTION_MONTH_WIDGET_NEXT_MONTH = "com.example.toolbox.calendar.ACTION_MONTH_WIDGET_NEXT_MONTH"
  const val ACTION_MONTH_WIDGET_TODAY = "com.example.toolbox.calendar.ACTION_MONTH_WIDGET_TODAY"

  const val KEY_WIDGET_FOCUSED_DATE_KEY = "calendar_widget_focused_date_key"
  const val KEY_MONTH_WIDGET_FOCUSED_DATE_KEY = "calendar_month_widget_focused_date_key"
  const val WIDGET_CORNER_RADIUS_DP = 16f

  private const val REQUEST_CODE_OPEN_CALENDAR_BASE = 9600
  private const val REQUEST_CODE_ADD_EVENT_BASE = 9700
  private const val REQUEST_CODE_PREV_DAY_BASE = 9800
  private const val REQUEST_CODE_NEXT_DAY_BASE = 9900
  private const val REQUEST_CODE_TODAY_BASE = 9950
  private const val REQUEST_CODE_ITEM_TEMPLATE_BASE = 10000

  private const val REQUEST_CODE_OPEN_MONTH_CALENDAR_BASE = 10100
  private const val REQUEST_CODE_MONTH_ADD_EVENT_BASE = 10200
  private const val REQUEST_CODE_PREV_MONTH_BASE = 10300
  private const val REQUEST_CODE_NEXT_MONTH_BASE = 10400
  private const val REQUEST_CODE_MONTH_TODAY_BASE = 10450

  @Volatile
  private var inMemoryFocusedDay: CalendarDay? = null

  @Volatile
  private var inMemoryFocusedMonthDay: CalendarDay? = null

  fun clearInMemoryStateForTesting() {
    inMemoryFocusedDay = null
    inMemoryFocusedMonthDay = null
  }

  // ---------------------------------------------------------------------------
  // 1. Schedule Widget Focused Day State
  // ---------------------------------------------------------------------------

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

  // ---------------------------------------------------------------------------
  // 2. Month Widget Focused Month State
  // ---------------------------------------------------------------------------

  fun loadFocusedMonthDay(
    context: Context? = null,
    defaultDay: CalendarDay = CalendarDay.today(),
  ): CalendarDay {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(CALENDAR_PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_MONTH_WIDGET_FOCUSED_DATE_KEY, null)
        val parsed = CalendarDay.fromDateKeyOrNull(raw)
        if (parsed != null) {
          inMemoryFocusedMonthDay = parsed
          return parsed
        }
      } catch (_: Throwable) {}
    }
    return inMemoryFocusedMonthDay ?: defaultDay
  }

  fun saveFocusedMonthDay(
    context: Context?,
    day: CalendarDay,
  ): CalendarDay {
    inMemoryFocusedMonthDay = day
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(CALENDAR_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MONTH_WIDGET_FOCUSED_DATE_KEY, day.dateKey).apply()
      } catch (_: Throwable) {}
    }
    return day
  }

  fun stepFocusedMonth(
    context: Context?,
    deltaMonths: Int,
    currentDayOverride: CalendarDay? = null,
  ): CalendarDay {
    val current = currentDayOverride ?: loadFocusedMonthDay(context)
    val next = current.plusMonths(deltaMonths)
    return saveFocusedMonthDay(context, next)
  }

  fun resetFocusedMonthToToday(
    context: Context?,
    today: CalendarDay = CalendarDay.today(),
  ): CalendarDay {
    return saveFocusedMonthDay(context, today)
  }

  // ---------------------------------------------------------------------------
  // 3. State Resolution (Schedule & Month)
  // ---------------------------------------------------------------------------

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

  private fun collectAllEventsForYears(
    calendarRepository: CalendarRepository?,
    todoRepository: TodoListRepository?,
    years: Set<Int>,
  ): Pair<List<CalendarEvent>, Set<CalendarCategory>> {
    val userEvents = calendarRepository?.loadEvents().orEmpty()
    val enabledCategories = calendarRepository?.getEnabledCategories() ?: CalendarCategory.entries.toSet()
    val showHolidays = calendarRepository?.getShowHolidays() ?: true
    val showTodos = calendarRepository?.getShowTodos() ?: true

    val holidayEvents = if (showHolidays) {
      years.flatMap { y -> CalendarEngine.builtInHolidaysForYear(y) }
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

    return (userEvents + todoEvents + holidayEvents) to enabledCategories
  }

  fun buildScheduleItemMetadata(event: CalendarEvent): String {
    return buildList {
      add(CalendarEngine.formatTimeRange(event.startMillis, event.endMillis, event.isAllDay))
      add(event.category.displayName)
      if (event.recurrence != CalendarRecurrence.NONE) {
        add(event.recurrence.shortLabel)
      }
      if (event.location.isNotBlank()) {
        add(event.location)
      }
    }.joinToString(" • ")
  }

  fun resolveWidgetState(
    context: Context? = null,
    calendarRepository: CalendarRepository? = context?.let { SharedPreferencesCalendarRepository(it) },
    todoRepository: TodoListRepository? = context?.let { SharedPreferencesTodoListRepository(it) },
    focusedDayOverride: CalendarDay? = null,
    todayOverride: CalendarDay = CalendarDay.today(),
    nowMillis: Long = 0L,
  ): CalendarWidgetState {
    val today = todayOverride
    val rawFocusedDay = focusedDayOverride ?: loadFocusedDay(context, defaultDay = today)
    // Never focus on a past day in the Schedule widget
    val focusedDay = maxOf(rawFocusedDay, today)
    val isToday = focusedDay == today

    val yearsToLoad = buildSet {
      add(focusedDay.year)
      if (focusedDay.month >= 11) add(focusedDay.year + 1)
    }
    val (allEvents, enabledCategories) = collectAllEventsForYears(
      calendarRepository = calendarRepository,
      todoRepository = todoRepository,
      years = yearsToLoad,
    )

    val upcomingDaysMap = CalendarEngine.buildUpcomingScheduleDays(
      events = allEvents,
      anchorDay = focusedDay,
      today = today,
      nowMillis = nowMillis,
      enabledCategories = enabledCategories,
    )

    val focusedDayOccurrences = upcomingDaysMap[focusedDay]
      ?: CalendarEngine.filterUpcomingOccurrences(
        occurrences = CalendarEngine.occurrencesForDay(
          events = allEvents,
          day = focusedDay,
          enabledCategories = enabledCategories,
        ),
        today = today,
        nowMillis = nowMillis,
      )
    val allUpcomingOccurrences = upcomingDaysMap.values.flatten()
    val occurrences = if (focusedDayOccurrences.isNotEmpty()) {
      focusedDayOccurrences
    } else {
      allUpcomingOccurrences
    }

    val scheduleItems = buildList {
      upcomingDaysMap.forEach { (day, dayOccs) ->
        val dayIsToday = day == today
        if (dayOccs.isEmpty()) {
          add(
            CalendarWidgetScheduleItem(
              day = day,
              isToday = dayIsToday,
              showDateBadge = true,
              occurrence = null,
            )
          )
        } else {
          dayOccs.forEachIndexed { idx, occ ->
            add(
              CalendarWidgetScheduleItem(
                day = day,
                isToday = dayIsToday,
                showDateBadge = idx == 0,
                occurrence = occ,
              )
            )
          }
        }
      }
    }

    val headerTitle = if (isToday) {
      "Today • ${focusedDay.monthShortName} ${focusedDay.dayOfMonth}"
    } else {
      "${focusedDay.dayOfWeekShortLabel}, ${focusedDay.monthShortName} ${focusedDay.dayOfMonth}, ${focusedDay.year}"
    }
    val countLabel = when (val c = allUpcomingOccurrences.size) {
      0 -> "No upcoming events"
      1 -> "1 event scheduled"
      else -> "$c events scheduled"
    }
    val headerSubtitle = "${focusedDay.monthFullName} ${focusedDay.year} • $countLabel"

    return CalendarWidgetState(
      focusedDay = focusedDay,
      today = today,
      isToday = isToday,
      occurrences = occurrences,
      scheduleItems = scheduleItems,
      headerTitle = headerTitle,
      headerSubtitle = headerSubtitle,
      dayNumberText = focusedDay.dayOfMonth.toString(),
      dayOfWeekText = focusedDay.dayOfWeekShortLabel.uppercase(),
      agendaSummaryText = formatAgendaLines(occurrences),
      palette = TodoListWidgets.resolveWidgetPalette(context),
    )
  }

  fun resolveMonthWidgetState(
    context: Context? = null,
    calendarRepository: CalendarRepository? = context?.let { SharedPreferencesCalendarRepository(it) },
    todoRepository: TodoListRepository? = context?.let { SharedPreferencesTodoListRepository(it) },
    focusedMonthDayOverride: CalendarDay? = null,
    todayOverride: CalendarDay = CalendarDay.today(),
  ): CalendarMonthWidgetState {
    val today = todayOverride
    val focusedDay = focusedMonthDayOverride ?: loadFocusedMonthDay(context, defaultDay = today)
    val year = focusedDay.year
    val month = focusedDay.month.coerceIn(1, 12)
    val isCurrentMonth = year == today.year && month == today.month

    val gridDays = CalendarEngine.buildMonthGridDays(year, month)
    val yearsInGrid = gridDays.map { it.year }.toSet()
    val (allEvents, enabledCategories) = collectAllEventsForYears(
      calendarRepository = calendarRepository,
      todoRepository = todoRepository,
      years = yearsInGrid,
    )

    val occurrencesByDay = CalendarEngine.occurrencesForDays(
      events = allEvents,
      days = gridDays,
      enabledCategories = enabledCategories,
    )

    val currentMonthDays = gridDays.filter { it.year == year && it.month == month }
    val totalMonthOccurrences = currentMonthDays.sumOf { d -> occurrencesByDay[d].orEmpty().size }
    val todayOccurrencesCount = occurrencesByDay[today].orEmpty().size

    val headerTitle = CalendarEngine.formatMonthYearTitle(year, month)
    val countText = when (totalMonthOccurrences) {
      0 -> "No events this month"
      1 -> "1 event this month"
      else -> "$totalMonthOccurrences events this month"
    }
    val headerSubtitle = if (isCurrentMonth) {
      val todayPart = when (todayOccurrencesCount) {
        0 -> "Today: Free"
        1 -> "Today: 1 event"
        else -> "Today: $todayOccurrencesCount events"
      }
      "$todayPart • $countText"
    } else {
      "Month View • $countText"
    }

    return CalendarMonthWidgetState(
      focusedMonthDay = if (isCurrentMonth) today else CalendarDay(year, month, 1),
      year = year,
      month = month,
      today = today,
      isCurrentMonth = isCurrentMonth,
      gridDays = gridDays,
      occurrencesByDay = occurrencesByDay,
      totalMonthOccurrences = totalMonthOccurrences,
      headerTitle = headerTitle,
      headerSubtitle = headerSubtitle,
      badgeDayNumberText = today.dayOfMonth.toString(),
      palette = TodoListWidgets.resolveWidgetPalette(context),
    )
  }

  // ---------------------------------------------------------------------------
  // 4. Bitmap Rendering (Background & Month 6×7 Grid)
  // ---------------------------------------------------------------------------

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

  private fun withAlpha(argb: Int, alphaFraction: Float): Int {
    val a = (alphaFraction.coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255)
    return (argb and 0x00FFFFFF) or (a shl 24)
  }

  private fun drawVerticalHexagon(
    canvas: Canvas,
    cx: Float,
    cy: Float,
    radius: Float,
    paint: Paint,
  ) {
    val path = Path()
    for (i in 0 until 6) {
      val angleRad = Math.toRadians((60.0 * i) - 90.0)
      val x = cx + (radius * cos(angleRad)).toFloat()
      val y = cy + (radius * sin(angleRad)).toFloat()
      if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    canvas.drawPath(path, paint)
  }

  /**
   * Renders the 6×7 Sunday-aligned monthly calendar grid bitmap inside a `14.dp` rounded card
   * with weekday headers (`S M T W T F S`), circular/hexagonal Today highlight, and colored
   * Google Calendar event dots underneath each day with scheduled occurrences.
   */
  fun renderMonthGridBitmap(
    state: CalendarMonthWidgetState,
    widthPx: Int = 520,
    heightPx: Int = 340,
    cornerRadiusPx: Float = 28f,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(180, 1100)
    val safeH = heightPx.coerceIn(120, 1100)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val palette = state.palette
      val w = safeW.toFloat()
      val h = safeH.toFloat()

      // 1. Inner 14.dp Card Fill & Border
      val cardRadius = min(cornerRadiusPx, min(w, h) * 0.5f)
      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = palette.itemCardArgb
      }
      canvas.drawRoundRect(RectF(0f, 0f, w, h), cardRadius, cardRadius, fillPaint)

      if (borderWidthPx > 0f) {
        val inset = max(borderWidthPx, 1f)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = borderWidthPx
          color = palette.borderArgb
        }
        val r = max(0f, cardRadius - inset * 0.5f)
        canvas.drawRoundRect(RectF(inset, inset, w - inset, h - inset), r, r, borderPaint)
      }

      val padX = max(8f, w * 0.025f)
      val padY = max(6f, h * 0.03f)
      val innerW = (w - padX * 2f).coerceAtLeast(70f)
      val innerH = (h - padY * 2f).coerceAtLeast(70f)
      val colW = innerW / 7f

      // Header row takes ~14% of inner height; 6 weeks share the remaining 86%
      val headerH = (innerH * 0.14f).coerceIn(16f, 42f)
      val rowH = ((innerH - headerH) / 6f).coerceAtLeast(14f)

      // 2. Weekday Headers (S M T W T F S)
      val weekdayLabels = arrayOf("S", "M", "T", "W", "T", "F", "S")
      val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = min(headerH * 0.65f, colW * 0.42f).coerceIn(10f, 28f)
      }
      val headerFontMetrics = headerTextPaint.fontMetrics
      val headerBaselineY = padY + (headerH * 0.5f) - (headerFontMetrics.ascent + headerFontMetrics.descent) * 0.5f

      for (col in 0 until 7) {
        val cx = padX + col * colW + colW * 0.5f
        val isWeekend = col == 0 || col == 6
        headerTextPaint.color = if (isWeekend) palette.primaryArgb else palette.onSurfaceVariantArgb
        canvas.drawText(weekdayLabels[col], cx, headerBaselineY, headerTextPaint)
      }

      // Subtle divider below weekday header
      val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = withAlpha(palette.borderArgb, 0.55f)
        strokeWidth = max(1f, borderWidthPx * 0.65f)
      }
      canvas.drawLine(padX, padY + headerH, w - padX, padY + headerH, dividerPaint)

      // 3. 6 Weeks × 7 Days Grid
      val dayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = min(rowH * 0.46f, colW * 0.42f).coerceIn(10f, 30f)
      }
      val badgeFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = palette.primaryArgb
      }
      val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
      }

      val gridDays = state.gridDays
      for (index in 0 until min(42, gridDays.size)) {
        val day = gridDays[index]
        val row = index / 7
        val col = index % 7
        val cellLeft = padX + col * colW
        val cellTop = padY + headerH + row * rowH
        val cx = cellLeft + colW * 0.5f
        val numberCy = cellTop + rowH * 0.43f

        val isInFocusedMonth = day.year == state.year && day.month == state.month
        val isToday = day == state.today
        val dayOccurrences = state.occurrencesByDay[day].orEmpty()

        if (isToday) {
          val badgeRadius = min(colW, rowH) * 0.34f
          if (state.hexagonIconButtons) {
            drawVerticalHexagon(canvas, cx, numberCy, badgeRadius, badgeFillPaint)
          } else {
            canvas.drawCircle(cx, numberCy, badgeRadius, badgeFillPaint)
          }
        }

        dayTextPaint.typeface = Typeface.create(
          Typeface.DEFAULT,
          if (isToday || dayOccurrences.isNotEmpty()) Typeface.BOLD else Typeface.NORMAL,
        )
        dayTextPaint.color = when {
          isToday -> palette.onPrimaryArgb
          isInFocusedMonth -> palette.onSurfaceArgb
          else -> withAlpha(palette.onSurfaceVariantArgb, 0.42f)
        }

        val fm = dayTextPaint.fontMetrics
        val textBaseline = numberCy - (fm.ascent + fm.descent) * 0.5f
        canvas.drawText(day.dayOfMonth.toString(), cx, textBaseline, dayTextPaint)

        // Up to 3 colored event dots below the day number
        if (dayOccurrences.isNotEmpty()) {
          val previewDots = dayOccurrences.take(3)
          val dotRadius = min(colW, rowH).coerceAtLeast(14f) * 0.075f
          val dotSpacing = dotRadius * 2.7f
          val totalDotsWidth = (previewDots.size - 1) * dotSpacing
          val startDotX = cx - totalDotsWidth * 0.5f
          val dotY = cellTop + rowH * 0.82f

          previewDots.forEachIndexed { dotIdx, occ ->
            dotPaint.color = occ.event.color.accentColor.toArgb()
            canvas.drawCircle(startDotX + dotIdx * dotSpacing, dotY, dotRadius, dotPaint)
          }
        }
      }

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  // ---------------------------------------------------------------------------
  // 5. PendingIntents & RemoteViews Builders
  // ---------------------------------------------------------------------------

  fun buildOpenCalendarPendingIntent(
    context: Context,
    appWidgetId: Int,
    day: CalendarDay,
    openAddEvent: Boolean = false,
    viewMode: CalendarViewMode = CalendarViewMode.SCHEDULE,
    requestCodeBase: Int = if (openAddEvent) REQUEST_CODE_ADD_EVENT_BASE else REQUEST_CODE_OPEN_CALENDAR_BASE,
  ): PendingIntent {
    val intent = createCalendarWidgetIntent(
      context = context,
      dateKey = day.dateKey,
      openAddEvent = openAddEvent,
      viewModeName = viewMode.name,
    )
    return PendingIntent.getActivity(
      context,
      requestCodeBase + (appWidgetId and 0xFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildDayActionBroadcastPendingIntent(
    context: Context,
    appWidgetId: Int,
    action: String,
    requestCodeBase: Int,
    providerClass: Class<*> = CalendarWidgetProvider::class.java,
    mutable: Boolean = false,
  ): PendingIntent {
    val intent = Intent(context, providerClass).apply {
      this.action = action
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setPackage(context.packageName)
    }
    val mutabilityFlag = if (mutable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.FLAG_MUTABLE
    } else if (mutable) {
      0
    } else {
      PendingIntent.FLAG_IMMUTABLE
    }
    return PendingIntent.getBroadcast(
      context,
      requestCodeBase + (appWidgetId and 0xFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or mutabilityFlag,
    )
  }

  /**
   * Builds the [RemoteViews] for a single row in the Calendar Schedule widget's scrollable [ListView],
   * matching [GoogleCalendarScheduleAgendaView] and [CalendarEventListCard] in [CalendarScreen].
   */
  fun buildScheduleItemRemoteViews(
    context: Context,
    item: CalendarWidgetScheduleItem,
    palette: TodoWidgetPalette = TodoListWidgets.resolveWidgetPalette(context),
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_calendar_schedule_item)
    val day = item.day
    val isToday = item.isToday

    // 1. Left Date Column (Weekday + Circular/Hexagonal Date Badge)
    if (item.showDateBadge) {
      views.setViewVisibility(R.id.widget_calendar_schedule_date_column, View.VISIBLE)
      views.setTextViewText(R.id.widget_calendar_schedule_day_of_week, day.dayOfWeekShortLabel.uppercase())
      views.setTextColor(
        R.id.widget_calendar_schedule_day_of_week,
        if (isToday) palette.primaryArgb else palette.onSurfaceVariantArgb,
      )
      views.setImageViewResource(
        R.id.widget_calendar_schedule_day_badge_bg,
        palette.iconButtonBackgroundRes,
      )
      views.setInt(
        R.id.widget_calendar_schedule_day_badge_bg,
        "setColorFilter",
        if (isToday) palette.primaryArgb else palette.itemCardArgb,
      )
      views.setTextViewText(R.id.widget_calendar_schedule_day_number, day.dayOfMonth.toString())
      views.setTextColor(
        R.id.widget_calendar_schedule_day_number,
        if (isToday) palette.onPrimaryArgb else palette.onSurfaceArgb,
      )
    } else {
      views.setViewVisibility(R.id.widget_calendar_schedule_date_column, View.INVISIBLE)
    }

    val openDayFillIn = Intent().apply {
      putExtra(EXTRA_DATE_KEY, day.dateKey)
      putExtra(EXTRA_ITEM_CLICK_ACTION, ITEM_CLICK_ACTION_OPEN_DAY)
    }
    views.setOnClickFillInIntent(R.id.widget_calendar_schedule_date_column, openDayFillIn)
    views.setOnClickFillInIntent(R.id.widget_calendar_schedule_day_badge, openDayFillIn)

    // 2. Right Event Card (or Empty Today Card)
    val occurrence = item.occurrence
    views.setInt(R.id.widget_calendar_schedule_card_bg, "setColorFilter", palette.itemCardArgb)

    if (occurrence == null) {
      val defaultBorderArgb = MusicPlayerWidgets.colorToArgb(
        lerp(
          Color(palette.itemCardArgb),
          Color(palette.borderArgb or 0xFF000000.toInt()),
          0.5f,
        )
      )
      views.setInt(R.id.widget_calendar_schedule_card_border, "setColorFilter", defaultBorderArgb)
      views.setViewVisibility(R.id.widget_calendar_schedule_event_badge, View.GONE)
      views.setViewVisibility(R.id.widget_calendar_schedule_event_meta, View.GONE)
      views.setViewVisibility(R.id.widget_calendar_schedule_event_desc, View.GONE)
      views.setViewVisibility(R.id.widget_calendar_schedule_event_star, View.GONE)

      val emptyText = if (isToday) "No events scheduled for Today" else "No events scheduled"
      views.setTextViewText(R.id.widget_calendar_schedule_event_title, emptyText)
      views.setTextColor(R.id.widget_calendar_schedule_event_title, palette.onSurfaceVariantArgb)
      views.setInt(R.id.widget_calendar_schedule_event_title, "setPaintFlags", Paint.ANTI_ALIAS_FLAG)

      views.setOnClickFillInIntent(R.id.widget_calendar_schedule_card_root, openDayFillIn)
      views.setOnClickFillInIntent(R.id.widget_calendar_schedule_card_content, openDayFillIn)
      views.setOnClickFillInIntent(R.id.widget_calendar_schedule_event_text_container, openDayFillIn)
      return views
    }

    val event = occurrence.event
    val cardBorderArgb = MusicPlayerWidgets.colorToArgb(
      lerp(
        Color(palette.itemCardArgb),
        event.color.accentColor,
        0.45f,
      )
    )
    views.setInt(R.id.widget_calendar_schedule_card_border, "setColorFilter", cardBorderArgb)

    // Event Color & Category Badge
    views.setViewVisibility(R.id.widget_calendar_schedule_event_badge, View.VISIBLE)
    views.setImageViewResource(
      R.id.widget_calendar_schedule_event_badge_bg,
      palette.iconButtonBackgroundRes,
    )
    views.setInt(
      R.id.widget_calendar_schedule_event_badge_bg,
      "setColorFilter",
      event.color.containerColor(palette.isDarkTheme).toArgb(),
    )
    views.setImageViewResource(
      R.id.widget_calendar_schedule_event_badge_border,
      palette.taskCheckUncheckedIconRes,
    )
    views.setInt(
      R.id.widget_calendar_schedule_event_badge_border,
      "setColorFilter",
      event.color.accentColor.toArgb(),
    )
    val categoryIconRes = when (event.category) {
      CalendarCategory.TASKS -> R.drawable.ic_todo_widget_checklist
      else -> R.drawable.ic_notification_calendar
    }
    views.setImageViewResource(R.id.widget_calendar_schedule_event_badge_icon, categoryIconRes)
    views.setInt(
      R.id.widget_calendar_schedule_event_badge_icon,
      "setColorFilter",
      event.color.contentColor(palette.isDarkTheme).toArgb(),
    )

    // Event Title (with strikethrough for completed To Do tasks)
    views.setTextViewText(R.id.widget_calendar_schedule_event_title, event.title)
    views.setTextColor(R.id.widget_calendar_schedule_event_title, palette.onSurfaceArgb)
    val paintFlags = if (event.isCompletedTask) {
      Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
      Paint.ANTI_ALIAS_FLAG
    }
    views.setInt(R.id.widget_calendar_schedule_event_title, "setPaintFlags", paintFlags)

    // Metadata Row (Time • Category • Recurrence • Location)
    val metaText = buildScheduleItemMetadata(event)
    views.setViewVisibility(R.id.widget_calendar_schedule_event_meta, View.VISIBLE)
    views.setTextViewText(R.id.widget_calendar_schedule_event_meta, metaText)
    views.setTextColor(R.id.widget_calendar_schedule_event_meta, palette.onSurfaceVariantArgb)

    // Optional Description Row
    if (event.description.isNotBlank()) {
      views.setViewVisibility(R.id.widget_calendar_schedule_event_desc, View.VISIBLE)
      views.setTextViewText(
        R.id.widget_calendar_schedule_event_desc,
        event.description.replace("\n", " "),
      )
      views.setTextColor(
        R.id.widget_calendar_schedule_event_desc,
        withAlpha(palette.onSurfaceVariantArgb, 0.8f),
      )
    } else {
      views.setViewVisibility(R.id.widget_calendar_schedule_event_desc, View.GONE)
    }

    // Star Favorite Toggle Button (for user-created events)
    if (!event.isReadOnlyBuiltIn) {
      views.setViewVisibility(R.id.widget_calendar_schedule_event_star, View.VISIBLE)
      views.setImageViewResource(
        R.id.widget_calendar_schedule_event_star,
        if (event.isFavorite) R.drawable.ic_media_favorite_on else R.drawable.ic_media_favorite_off,
      )
      views.setInt(
        R.id.widget_calendar_schedule_event_star,
        "setColorFilter",
        if (event.isFavorite) palette.primaryArgb else palette.onSurfaceVariantArgb,
      )
      val starFillIn = Intent().apply {
        putExtra(EXTRA_EVENT_ID, event.id)
        putExtra(EXTRA_DATE_KEY, day.dateKey)
        putExtra(EXTRA_ITEM_CLICK_ACTION, ITEM_CLICK_ACTION_TOGGLE_FAVORITE)
      }
      views.setOnClickFillInIntent(R.id.widget_calendar_schedule_event_star, starFillIn)
    } else {
      views.setViewVisibility(R.id.widget_calendar_schedule_event_star, View.GONE)
    }

    // Click Event Card -> Opens Calendar in Schedule view and inspects the event
    val openEventFillIn = Intent().apply {
      putExtra(EXTRA_EVENT_ID, event.id)
      putExtra(EXTRA_DATE_KEY, day.dateKey)
      putExtra(EXTRA_ITEM_CLICK_ACTION, ITEM_CLICK_ACTION_OPEN_EVENT)
    }
    views.setOnClickFillInIntent(R.id.widget_calendar_schedule_card_root, openEventFillIn)
    views.setOnClickFillInIntent(R.id.widget_calendar_schedule_card_content, openEventFillIn)
    views.setOnClickFillInIntent(R.id.widget_calendar_schedule_event_badge, openEventFillIn)
    views.setOnClickFillInIntent(R.id.widget_calendar_schedule_event_text_container, openEventFillIn)

    return views
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
      defaultHeightDp = 180f,
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

    // Add event button
    views.setImageViewResource(R.id.widget_calendar_add_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_calendar_add_button_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_calendar_add_button_icon, "setColorFilter", palette.onPrimaryContainerArgb)

    // Empty state view styling
    views.setImageViewResource(R.id.widget_calendar_empty_badge_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_calendar_empty_badge_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_calendar_empty_badge_icon, "setColorFilter", palette.primaryArgb)
    views.setTextColor(R.id.widget_calendar_empty_title, palette.onSurfaceArgb)
    views.setTextViewText(R.id.widget_calendar_agenda_list, state.agendaSummaryText)
    views.setTextColor(R.id.widget_calendar_agenda_list, palette.onSurfaceVariantArgb)

    // Scrollable Schedule ListView adapter & empty view
    val serviceIntent = Intent(context, CalendarScheduleWidgetRemoteViewsService::class.java).apply {
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }
    @Suppress("DEPRECATION")
    views.setRemoteAdapter(R.id.widget_calendar_schedule_list_view, serviceIntent)
    views.setEmptyView(R.id.widget_calendar_schedule_list_view, R.id.widget_calendar_empty_view)

    val itemTemplatePendingIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_ITEM_CLICK,
      requestCodeBase = REQUEST_CODE_ITEM_TEMPLATE_BASE,
      providerClass = CalendarWidgetProvider::class.java,
      mutable = true,
    )
    views.setPendingIntentTemplate(R.id.widget_calendar_schedule_list_view, itemTemplatePendingIntent)

    // Click PendingIntents (opens Calendar in SCHEDULE view)
    val openDayIntent = buildOpenCalendarPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      day = state.focusedDay,
      openAddEvent = false,
      viewMode = CalendarViewMode.SCHEDULE,
      requestCodeBase = REQUEST_CODE_OPEN_CALENDAR_BASE,
    )
    val addEventIntent = buildOpenCalendarPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      day = state.focusedDay,
      openAddEvent = true,
      viewMode = CalendarViewMode.SCHEDULE,
      requestCodeBase = REQUEST_CODE_ADD_EVENT_BASE,
    )
    val todayIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_TODAY,
      requestCodeBase = REQUEST_CODE_TODAY_BASE,
      providerClass = CalendarWidgetProvider::class.java,
    )

    views.setOnClickPendingIntent(R.id.widget_calendar_date_badge, todayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_header_text_container, openDayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_empty_view, openDayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_add_button, addEventIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_add_button_icon, addEventIntent)

    return views
  }

  fun buildMonthWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: CalendarMonthWidgetState = resolveMonthWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_calendar_month)
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
      defaultHeightDp = 230f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(200f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(160f)
    val maxDimPx = 1000f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(200)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(160)

    renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_calendar_month_background, bgBmp)
    }

    // Header date badge (shows Today's date number; primary when viewing current month)
    views.setImageViewResource(R.id.widget_calendar_month_date_badge_bg, state.iconButtonBackgroundRes)
    views.setInt(
      R.id.widget_calendar_month_date_badge_bg,
      "setColorFilter",
      if (state.isCurrentMonth) palette.primaryArgb else palette.primaryContainerArgb,
    )
    views.setTextViewText(R.id.widget_calendar_month_date_badge_number, state.badgeDayNumberText)
    views.setTextColor(
      R.id.widget_calendar_month_date_badge_number,
      if (state.isCurrentMonth) palette.onPrimaryArgb else palette.onPrimaryContainerArgb,
    )

    // Header title & subtitle
    views.setTextViewText(R.id.widget_calendar_month_title, state.headerTitle)
    views.setTextColor(R.id.widget_calendar_month_title, palette.onSurfaceArgb)
    views.setTextViewText(R.id.widget_calendar_month_subtitle, state.headerSubtitle)
    views.setTextColor(R.id.widget_calendar_month_subtitle, palette.onSurfaceVariantArgb)

    // Navigation & Add event buttons
    views.setInt(R.id.widget_calendar_month_prev_month, "setColorFilter", palette.onSurfaceVariantArgb)
    views.setInt(R.id.widget_calendar_month_next_month, "setColorFilter", palette.onSurfaceVariantArgb)
    views.setImageViewResource(R.id.widget_calendar_month_add_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_calendar_month_add_button_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_calendar_month_add_button_icon, "setColorFilter", palette.onPrimaryContainerArgb)

    // Render 6x7 Month Grid Card Bitmap
    val gridWidthPx = (bgWidthPx - (20f * density * scaleFactor)).roundToInt().coerceAtLeast(160)
    val gridHeightPx = (bgHeightPx - (64f * density * scaleFactor)).roundToInt().coerceAtLeast(110)
    renderMonthGridBitmap(
      state = state,
      widthPx = gridWidthPx,
      heightPx = gridHeightPx,
      cornerRadiusPx = 14f * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { gridBmp ->
      views.setImageViewBitmap(R.id.widget_calendar_month_grid_image, gridBmp)
    }

    // Click PendingIntents (opens Calendar in MONTH view)
    val openMonthIntent = buildOpenCalendarPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      day = state.focusedMonthDay,
      openAddEvent = false,
      viewMode = CalendarViewMode.MONTH,
      requestCodeBase = REQUEST_CODE_OPEN_MONTH_CALENDAR_BASE,
    )
    val addEventIntent = buildOpenCalendarPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      day = state.focusedMonthDay,
      openAddEvent = true,
      viewMode = CalendarViewMode.MONTH,
      requestCodeBase = REQUEST_CODE_MONTH_ADD_EVENT_BASE,
    )
    val prevMonthIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_MONTH_WIDGET_PREV_MONTH,
      requestCodeBase = REQUEST_CODE_PREV_MONTH_BASE,
      providerClass = CalendarMonthWidgetProvider::class.java,
    )
    val nextMonthIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_MONTH_WIDGET_NEXT_MONTH,
      requestCodeBase = REQUEST_CODE_NEXT_MONTH_BASE,
      providerClass = CalendarMonthWidgetProvider::class.java,
    )
    val todayMonthIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_MONTH_WIDGET_TODAY,
      requestCodeBase = REQUEST_CODE_MONTH_TODAY_BASE,
      providerClass = CalendarMonthWidgetProvider::class.java,
    )

    views.setOnClickPendingIntent(R.id.widget_calendar_month_date_badge, todayMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_month_header_text_container, openMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_month_grid_card, openMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_month_grid_image, openMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_month_prev_month, prevMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_month_next_month, nextMonthIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_month_add_button, addEventIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_month_add_button_icon, addEventIntent)

    return views
  }

  // ---------------------------------------------------------------------------
  // 6. Action Dispatch & Widget Updates
  // ---------------------------------------------------------------------------

  fun toggleEventFavoriteFromWidget(
    context: Context?,
    eventId: String?,
    calendarRepository: CalendarRepository? = context?.let { SharedPreferencesCalendarRepository(it) },
  ): Boolean {
    if (eventId.isNullOrBlank()) return false
    val repo = calendarRepository ?: return false
    val currentEvents = repo.loadEvents()
    val target = currentEvents.firstOrNull { it.id == eventId } ?: return false
    if (target.isReadOnlyBuiltIn) return false
    val updated = currentEvents.map { ev ->
      if (ev.id == eventId) ev.copy(isFavorite = !ev.isFavorite) else ev
    }
    repo.saveEvents(updated)
    if (context != null) {
      updateAllWidgets(context)
    }
    return true
  }

  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
    calendarRepository: CalendarRepository? = context?.let { SharedPreferencesCalendarRepository(it) },
  ): Boolean {
    val action = intent?.action
    if (action == ACTION_WIDGET_ITEM_CLICK) {
      val clickAction = intent.getStringExtra(EXTRA_ITEM_CLICK_ACTION) ?: ITEM_CLICK_ACTION_OPEN_DAY
      val eventId = intent.getStringExtra(EXTRA_EVENT_ID)?.takeIf { it.isNotBlank() }
      val dateKey = intent.getStringExtra(EXTRA_DATE_KEY)?.takeIf { it.isNotBlank() }
      val targetDay = CalendarDay.fromDateKeyOrNull(dateKey)
      return when (clickAction) {
        ITEM_CLICK_ACTION_TOGGLE_FAVORITE -> {
          toggleEventFavoriteFromWidget(
            context = context,
            eventId = eventId,
            calendarRepository = calendarRepository,
          )
        }
        ITEM_CLICK_ACTION_OPEN_EVENT -> {
          CalendarEngine.requestNavigation(
            targetDay = targetDay,
            openAddEvent = false,
            openEventId = eventId,
            viewMode = CalendarViewMode.SCHEDULE,
          )
          if (context != null) {
            try {
              context.startActivity(
                createCalendarWidgetIntent(
                  context = context,
                  dateKey = dateKey,
                  openAddEvent = false,
                  viewModeName = CalendarViewMode.SCHEDULE.name,
                  openEventId = eventId,
                )
              )
            } catch (_: Throwable) {}
          }
          true
        }
        else -> {
          CalendarEngine.requestNavigation(
            targetDay = targetDay,
            openAddEvent = false,
            viewMode = CalendarViewMode.SCHEDULE,
          )
          if (context != null) {
            try {
              context.startActivity(
                createCalendarWidgetIntent(
                  context = context,
                  dateKey = dateKey,
                  openAddEvent = false,
                  viewModeName = CalendarViewMode.SCHEDULE.name,
                )
              )
            } catch (_: Throwable) {}
          }
          true
        }
      }
    }
    return handleWidgetAction(
      context = context,
      action = action,
    )
  }

  fun handleWidgetAction(
    context: Context?,
    action: String?,
  ): Boolean {
    if (action.isNullOrBlank()) return false
    return when (action) {
      ACTION_WIDGET_REFRESH, ACTION_MONTH_WIDGET_REFRESH -> {
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
      ACTION_MONTH_WIDGET_PREV_MONTH -> {
        stepFocusedMonth(context, deltaMonths = -1)
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_MONTH_WIDGET_NEXT_MONTH -> {
        stepFocusedMonth(context, deltaMonths = 1)
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_MONTH_WIDGET_TODAY -> {
        resetFocusedMonthToToday(context)
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
      val scheduleComponent = ComponentName(appCtx, CalendarWidgetProvider::class.java)
      val scheduleIds = try {
        manager.getAppWidgetIds(scheduleComponent) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (scheduleIds.isNotEmpty()) {
        updateCalendarWidgets(appCtx, manager, scheduleIds)
      }

      val monthComponent = ComponentName(appCtx, CalendarMonthWidgetProvider::class.java)
      val monthIds = try {
        manager.getAppWidgetIds(monthComponent) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (monthIds.isNotEmpty()) {
        updateCalendarMonthWidgets(appCtx, manager, monthIds)
      }
    } catch (_: Throwable) {}
  }

  fun updateCalendarWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: CalendarWidgetState = resolveWidgetState(context, nowMillis = System.currentTimeMillis()),
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
    try {
      @Suppress("DEPRECATION")
      appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_calendar_schedule_list_view)
    } catch (_: Throwable) {}
  }

  fun updateCalendarMonthWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: CalendarMonthWidgetState = resolveMonthWidgetState(context),
  ) {
    for (appWidgetId in appWidgetIds) {
      try {
        val options = try {
          appWidgetManager.getAppWidgetOptions(appWidgetId)
        } catch (_: Throwable) {
          null
        }
        val views = buildMonthWidgetRemoteViews(
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
 * Calendar Schedule Home Screen AppWidget displaying the scrollable Google Calendar Schedule view
 * (left weekday + date badge column and right color-coded 14.dp event cards, excluding past events),
 * Today badge reset, and `+` quick add event trigger.
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
    val state = CalendarWidgets.resolveWidgetState(context, nowMillis = System.currentTimeMillis())
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

/**
 * [RemoteViewsService] that powers the scrollable Google Calendar Schedule [ListView] inside
 * [CalendarWidgetProvider].
 */
class CalendarScheduleWidgetRemoteViewsService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return CalendarScheduleWidgetRemoteViewsFactory(applicationContext)
  }
}

internal class CalendarScheduleWidgetRemoteViewsFactory(
  private val context: Context,
) : RemoteViewsService.RemoteViewsFactory {

  private var scheduleItems: List<CalendarWidgetScheduleItem> = emptyList()
  private var palette: TodoWidgetPalette = TodoListWidgets.resolveWidgetPalette(context)

  override fun onCreate() {
    reloadSchedule()
  }

  override fun onDataSetChanged() {
    reloadSchedule()
  }

  private fun reloadSchedule() {
    try {
      val state = CalendarWidgets.resolveWidgetState(
        context = context,
        nowMillis = System.currentTimeMillis(),
      )
      scheduleItems = state.scheduleItems
      palette = state.palette
    } catch (_: Throwable) {
      scheduleItems = emptyList()
    }
  }

  override fun onDestroy() {
    scheduleItems = emptyList()
  }

  override fun getCount(): Int = scheduleItems.size

  override fun getViewAt(position: Int): RemoteViews? {
    val item = scheduleItems.getOrNull(position) ?: return null
    return CalendarWidgets.buildScheduleItemRemoteViews(
      context = context,
      item = item,
      palette = palette,
    )
  }

  override fun getLoadingView(): RemoteViews? = null

  override fun getViewTypeCount(): Int = 1

  override fun getItemId(position: Int): Long {
    val itemHash = scheduleItems.getOrNull(position)?.itemId?.hashCode()?.toLong() ?: position.toLong()
    return (itemHash shl 32) xor (palette.hashCode().toLong() and 0xFFFFFFFFL)
  }

  override fun hasStableIds(): Boolean = true
}

/**
 * Calendar Month Home Screen AppWidget displaying the 6×7 Sunday-aligned monthly grid with weekday
 * headers, Today badge highlight, colored event indicator dots, `<` / `>` month stepper, Today reset,
 * and `+` quick add event trigger.
 */
class CalendarMonthWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    CalendarWidgets.updateCalendarMonthWidgets(
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
    val state = CalendarWidgets.resolveMonthWidgetState(context)
    val views = CalendarWidgets.buildMonthWidgetRemoteViews(
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
