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
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.toArgb
import com.example.toolbox.R
import com.example.toolbox.ui.main.createCalendarWidgetIntent
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Snapshot of the day's schedule rendered by the Calendar Schedule Home Screen AppWidget ([CalendarWidgetProvider]).
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

    val (allEvents, enabledCategories) = collectAllEventsForYears(
      calendarRepository = calendarRepository,
      todoRepository = todoRepository,
      years = setOf(focusedDay.year),
    )
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
  ): PendingIntent {
    val intent = Intent(context, providerClass).apply {
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
    val prevDayIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_PREV_DAY,
      requestCodeBase = REQUEST_CODE_PREV_DAY_BASE,
      providerClass = CalendarWidgetProvider::class.java,
    )
    val nextDayIntent = buildDayActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_NEXT_DAY,
      requestCodeBase = REQUEST_CODE_NEXT_DAY_BASE,
      providerClass = CalendarWidgetProvider::class.java,
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
    views.setOnClickPendingIntent(R.id.widget_calendar_agenda_card, openDayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_prev_day, prevDayIntent)
    views.setOnClickPendingIntent(R.id.widget_calendar_next_day, nextDayIntent)
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
 * Calendar Schedule Home Screen AppWidget displaying the focused day's Google Calendar schedule,
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
