package com.example.toolbox.ui.tools

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.toolbox.R
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.createStandaloneToolIntent
import com.example.toolbox.ui.main.createToolShortcutBitmap
import com.example.toolbox.ui.main.resolveShortcutBackgroundColor
import com.example.toolbox.ui.main.resolveShortcutForegroundColor
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.UUID

const val CALENDAR_PREFS_NAME = "calendar_tool_preferences"
const val KEY_CALENDAR_EVENTS_JSON = "calendar_events_json"
const val KEY_CALENDAR_VIEW_MODE = "calendar_view_mode"
const val KEY_CALENDAR_ENABLED_CATEGORIES = "calendar_enabled_categories"
const val KEY_CALENDAR_SHOW_TODOS = "calendar_show_todos"
const val KEY_CALENDAR_SHOW_HOLIDAYS = "calendar_show_holidays"
const val KEY_CALENDAR_FAVORITE_EVENT_IDS = "calendar_favorite_event_ids"

/**
 * Google Calendar-inspired view modes supported by the Calendar tool.
 */
enum class CalendarViewMode(
  val label: String,
  val daysInTimeline: Int,
) {
  SCHEDULE("Schedule", 0),
  MONTH("Month", 0),
  WEEK("Week", 7),
  DAY("Day", 1),
  ;

  companion object {
    fun fromNameOrDefault(name: String?): CalendarViewMode {
      if (name.isNullOrBlank()) return SCHEDULE
      return entries.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) } ?: SCHEDULE
    }
  }
}

/**
 * Built-in calendar categories with Google Calendar-inspired semantic purpose.
 */
enum class CalendarCategory(
  val displayName: String,
  val defaultColorSwatch: CalendarEventColor,
) {
  PERSONAL("Personal", CalendarEventColor.PEACOCK),
  WORK("Work", CalendarEventColor.BLUEBERRY),
  BIRTHDAYS("Birthdays", CalendarEventColor.FLAMINGO),
  HOLIDAYS("Holidays", CalendarEventColor.BASIL),
  HEALTH("Health & Fitness", CalendarEventColor.SAGE),
  TRAVEL("Travel", CalendarEventColor.TANGERINE),
  TASKS("To Do Tasks", CalendarEventColor.LAVENDER),
  ;

  companion object {
    fun fromNameOrDefault(name: String?): CalendarCategory {
      if (name.isNullOrBlank()) return PERSONAL
      return entries.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) } ?: PERSONAL
    }
  }
}

/**
 * Official 11 Google Calendar event color swatches with adaptive light and dark mode hex palettes.
 */
enum class CalendarEventColor(
  val displayName: String,
  val hex: String,
  val lightContainerHex: String,
  val lightTextHex: String,
  val darkContainerHex: String,
  val darkTextHex: String,
) {
  TOMATO(
    displayName = "Tomato",
    hex = "#D50000",
    lightContainerHex = "#FAD2CF",
    lightTextHex = "#750000",
    darkContainerHex = "#5C1010",
    darkTextHex = "#FFB4AB",
  ),
  FLAMINGO(
    displayName = "Flamingo",
    hex = "#E67C73",
    lightContainerHex = "#FCE8E6",
    lightTextHex = "#7A271F",
    darkContainerHex = "#5D2A27",
    darkTextHex = "#FFDAD5",
  ),
  TANGERINE(
    displayName = "Tangerine",
    hex = "#F4511E",
    lightContainerHex = "#FEDFC8",
    lightTextHex = "#7D2507",
    darkContainerHex = "#5F2410",
    darkTextHex = "#FFDBCF",
  ),
  BANANA(
    displayName = "Banana",
    hex = "#F6BF26",
    lightContainerHex = "#FEF3D6",
    lightTextHex = "#654800",
    darkContainerHex = "#574108",
    darkTextHex = "#FFE08B",
  ),
  SAGE(
    displayName = "Sage",
    hex = "#33B679",
    lightContainerHex = "#CEEAD6",
    lightTextHex = "#0D522C",
    darkContainerHex = "#164A32",
    darkTextHex = "#A6F2C6",
  ),
  BASIL(
    displayName = "Basil",
    hex = "#0B8043",
    lightContainerHex = "#C6E7D1",
    lightTextHex = "#064724",
    darkContainerHex = "#0E4226",
    darkTextHex = "#99F1B9",
  ),
  PEACOCK(
    displayName = "Peacock",
    hex = "#039BE5",
    lightContainerHex = "#D2E3FC",
    lightTextHex = "#01476B",
    darkContainerHex = "#0C3F5C",
    darkTextHex = "#B5E3FF",
  ),
  BLUEBERRY(
    displayName = "Blueberry",
    hex = "#3F51B5",
    lightContainerHex = "#D8DEF8",
    lightTextHex = "#1C276B",
    darkContainerHex = "#242D63",
    darkTextHex = "#DEE0FF",
  ),
  LAVENDER(
    displayName = "Lavender",
    hex = "#7986CB",
    lightContainerHex = "#E8EAFA",
    lightTextHex = "#2C356E",
    darkContainerHex = "#303865",
    darkTextHex = "#E0E0FF",
  ),
  GRAPE(
    displayName = "Grape",
    hex = "#8E24AA",
    lightContainerHex = "#F3D9FA",
    lightTextHex = "#540F66",
    darkContainerHex = "#4C195B",
    darkTextHex = "#F9D8FF",
  ),
  GRAPHITE(
    displayName = "Graphite",
    hex = "#616161",
    lightContainerHex = "#E3E3E3",
    lightTextHex = "#2B2B2B",
    darkContainerHex = "#383838",
    darkTextHex = "#E2E2E2",
  ),
  ;

  val accentColor: Color
    get() = parseCalendarColorHex(hex)

  fun containerColor(darkTheme: Boolean): Color =
    parseCalendarColorHex(if (darkTheme) darkContainerHex else lightContainerHex)

  fun contentColor(darkTheme: Boolean): Color =
    parseCalendarColorHex(if (darkTheme) darkTextHex else lightTextHex)

  companion object {
    fun fromNameOrDefault(
      name: String?,
      fallback: CalendarEventColor = PEACOCK,
    ): CalendarEventColor {
      if (name.isNullOrBlank()) return fallback
      return entries.firstOrNull {
        it.name.equals(name.trim(), ignoreCase = true) ||
          it.displayName.equals(name.trim(), ignoreCase = true)
      } ?: fallback
    }
  }
}

internal fun parseCalendarColorHex(hex: String): Color {
  val clean = hex.trim().removePrefix("#")
  val rgb = clean.toLongOrNull(16) ?: 0x039BE5L
  return Color(0xFF000000L or rgb)
}

/**
 * Recurrence rule options for Google Calendar-style repeating events.
 */
enum class CalendarRecurrence(
  val label: String,
  val shortLabel: String,
) {
  NONE("Does not repeat", "Once"),
  DAILY("Every day", "Daily"),
  WEEKLY("Every week", "Weekly"),
  BIWEEKLY("Every 2 weeks", "Biweekly"),
  WEEKDAYS("Every weekday (Mon–Fri)", "Weekdays"),
  MONTHLY("Every month", "Monthly"),
  YEARLY("Every year", "Yearly"),
  ;

  companion object {
    fun fromNameOrDefault(name: String?): CalendarRecurrence {
      if (name.isNullOrBlank()) return NONE
      return entries.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) } ?: NONE
    }
  }
}

/**
 * Reminder lead-time options before an event starts.
 */
enum class CalendarReminderOption(
  val minutesBefore: Int,
  val label: String,
) {
  NONE(-1, "No reminder"),
  AT_START(0, "At time of event"),
  TEN_MINUTES(10, "10 minutes before"),
  THIRTY_MINUTES(30, "30 minutes before"),
  ONE_HOUR(60, "1 hour before"),
  ONE_DAY(1440, "1 day before"),
  ;

  companion object {
    fun fromMinutes(minutes: Int): CalendarReminderOption {
      return entries.firstOrNull { it.minutesBefore == minutes } ?: NONE
    }
  }
}

/**
 * Immutable local calendar date representation (`year`, `month` 1..12, `dayOfMonth` 1..31).
 */
@Immutable
data class CalendarDay(
  val year: Int,
  val month: Int,
  val dayOfMonth: Int,
) : Comparable<CalendarDay> {

  val dateKey: String
    get() = String.format(Locale.US, "%04d-%02d-%02d", year, month, dayOfMonth)

  override fun compareTo(other: CalendarDay): Int {
    if (year != other.year) return year.compareTo(other.year)
    if (month != other.month) return month.compareTo(other.month)
    return dayOfMonth.compareTo(other.dayOfMonth)
  }

  fun toStartOfDayMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, (month - 1).coerceIn(0, 11))
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
  }

  fun toEndOfDayMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, (month - 1).coerceIn(0, 11))
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.timeInMillis
  }

  fun withTime(hourOfDay: Int, minute: Int): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, (month - 1).coerceIn(0, 11))
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    cal.set(Calendar.HOUR_OF_DAY, hourOfDay.coerceIn(0, 23))
    cal.set(Calendar.MINUTE, minute.coerceIn(0, 59))
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
  }

  fun plusDays(days: Int): CalendarDay {
    val cal = Calendar.getInstance()
    cal.timeInMillis = toStartOfDayMillis()
    cal.add(Calendar.DAY_OF_YEAR, days)
    return fromCalendar(cal)
  }

  fun plusMonths(months: Int): CalendarDay {
    val cal = Calendar.getInstance()
    cal.timeInMillis = toStartOfDayMillis()
    cal.add(Calendar.MONTH, months)
    return fromCalendar(cal)
  }

  /**
   * Returns day of week where Sunday = 1 .. Saturday = 7 (matching [Calendar.DAY_OF_WEEK]).
   */
  val dayOfWeek: Int
    get() {
      val cal = Calendar.getInstance()
      cal.timeInMillis = toStartOfDayMillis()
      return cal.get(Calendar.DAY_OF_WEEK)
    }

  val isWeekend: Boolean
    get() = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

  val dayOfWeekShortLabel: String
    get() = when (dayOfWeek) {
      Calendar.SUNDAY -> "Sun"
      Calendar.MONDAY -> "Mon"
      Calendar.TUESDAY -> "Tue"
      Calendar.WEDNESDAY -> "Wed"
      Calendar.THURSDAY -> "Thu"
      Calendar.FRIDAY -> "Fri"
      Calendar.SATURDAY -> "Sat"
      else -> "Day"
    }

  val dayOfWeekSingleLetter: String
    get() = when (dayOfWeek) {
      Calendar.SUNDAY -> "S"
      Calendar.MONDAY -> "M"
      Calendar.TUESDAY -> "T"
      Calendar.WEDNESDAY -> "W"
      Calendar.THURSDAY -> "T"
      Calendar.FRIDAY -> "F"
      Calendar.SATURDAY -> "S"
      else -> "D"
    }

  val monthFullName: String
    get() = CalendarMonth.fromNumber(month).fullName

  val monthShortName: String
    get() = CalendarMonth.fromNumber(month).shortName

  companion object {
    fun today(nowMillis: Long = System.currentTimeMillis()): CalendarDay =
      fromMillis(nowMillis)

    fun fromMillis(millis: Long): CalendarDay {
      val cal = Calendar.getInstance().apply { timeInMillis = millis }
      return fromCalendar(cal)
    }

    fun fromCalendar(cal: Calendar): CalendarDay {
      return CalendarDay(
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH) + 1,
        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
      )
    }

    fun fromDateKeyOrNull(dateKey: String?): CalendarDay? {
      if (dateKey.isNullOrBlank()) return null
      val parts = dateKey.trim().split("-")
      if (parts.size != 3) return null
      val y = parts[0].toIntOrNull() ?: return null
      val m = parts[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
      val maxDay = CalendarEngine.daysInMonth(y, m)
      val d = parts[2].toIntOrNull()?.takeIf { it in 1..maxDay } ?: return null
      return CalendarDay(y, m, d)
    }
  }
}

/**
 * Represents a calendar event in Toolbox Calendar.
 */
@Immutable
data class CalendarEvent(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val description: String = "",
  val location: String = "",
  val startMillis: Long,
  val endMillis: Long,
  val isAllDay: Boolean = false,
  val category: CalendarCategory = CalendarCategory.PERSONAL,
  val color: CalendarEventColor = category.defaultColorSwatch,
  val recurrence: CalendarRecurrence = CalendarRecurrence.NONE,
  val reminderMinutesBefore: Int = CalendarReminderOption.THIRTY_MINUTES.minutesBefore,
  val isFavorite: Boolean = false,
  val isBuiltInHoliday: Boolean = false,
  val linkedTodoTaskId: String? = null,
  val isCompletedTask: Boolean = false,
  val notifiedTriggerMillis: Long? = null,
) {
  val durationMillis: Long
    get() = (endMillis - startMillis).coerceAtLeast(if (isAllDay) 86_400_000L else 900_000L)

  val startDay: CalendarDay
    get() = CalendarDay.fromMillis(startMillis)

  val endDay: CalendarDay
    get() = CalendarDay.fromMillis(maxOf(startMillis, endMillis - if (isAllDay) 1L else 0L))

  val isReadOnlyBuiltIn: Boolean
    get() = isBuiltInHoliday || linkedTodoTaskId != null

  val reminderOption: CalendarReminderOption
    get() = CalendarReminderOption.fromMinutes(reminderMinutesBefore)

  fun triggerAlarmAtMillis(): Long? {
    if (reminderMinutesBefore < 0 || isBuiltInHoliday || linkedTodoTaskId != null) return null
    return startMillis - reminderMinutesBefore * 60_000L
  }

  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("description", description)
    put("location", location)
    put("startMillis", startMillis)
    put("endMillis", endMillis)
    put("isAllDay", isAllDay)
    put("category", category.name)
    put("color", color.name)
    put("recurrence", recurrence.name)
    put("reminderMinutesBefore", reminderMinutesBefore)
    put("isFavorite", isFavorite)
    if (notifiedTriggerMillis != null) {
      put("notifiedTriggerMillis", notifiedTriggerMillis)
    }
  }

  companion object {
    fun fromJson(json: JSONObject): CalendarEvent {
      val category = CalendarCategory.fromNameOrDefault(json.optString("category", CalendarCategory.PERSONAL.name))
      val color = CalendarEventColor.fromNameOrDefault(
        name = json.optString("color", category.defaultColorSwatch.name),
        fallback = category.defaultColorSwatch,
      )
      val start = json.optLong("startMillis", System.currentTimeMillis())
      val rawEnd = json.optLong("endMillis", start + 3_600_000L)
      val end = maxOf(start, rawEnd)
      val notified = if (json.has("notifiedTriggerMillis") && !json.isNull("notifiedTriggerMillis")) {
        json.optLong("notifiedTriggerMillis")
      } else {
        null
      }
      return CalendarEvent(
        id = json.optString("id", "").ifBlank { UUID.randomUUID().toString() },
        title = json.optString("title", "").trim(),
        description = json.optString("description", "").trim(),
        location = json.optString("location", "").trim(),
        startMillis = start,
        endMillis = end,
        isAllDay = json.optBoolean("isAllDay", false),
        category = category,
        color = color,
        recurrence = CalendarRecurrence.fromNameOrDefault(json.optString("recurrence", CalendarRecurrence.NONE.name)),
        reminderMinutesBefore = json.optInt(
          "reminderMinutesBefore",
          CalendarReminderOption.THIRTY_MINUTES.minutesBefore,
        ),
        isFavorite = json.optBoolean("isFavorite", false),
        notifiedTriggerMillis = notified,
      )
    }
  }
}

/**
 * A concrete occurrence of a [CalendarEvent] on a specific [occurrenceDay] (handling recurring or multi-day events).
 */
@Immutable
data class CalendarEventOccurrence(
  val event: CalendarEvent,
  val occurrenceDay: CalendarDay,
  val instanceStartMillis: Long,
  val instanceEndMillis: Long,
) {
  val instanceId: String
    get() = "${event.id}_${occurrenceDay.dateKey}"

  val startHourFraction: Float
    get() {
      val cal = Calendar.getInstance().apply { timeInMillis = instanceStartMillis }
      return cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60f)
    }

  val endHourFraction: Float
    get() {
      val durationHours = ((instanceEndMillis - instanceStartMillis) / 3_600_000f).coerceAtLeast(0.4f)
      return (startHourFraction + durationHours).coerceAtMost(24f)
    }
}

/**
 * Repository interface for persisting user calendar events, active view mode, and category filters.
 */
interface CalendarRepository {
  fun loadEvents(): List<CalendarEvent>
  fun saveEvents(events: List<CalendarEvent>)

  fun getViewMode(): CalendarViewMode = CalendarViewMode.SCHEDULE
  fun setViewMode(mode: CalendarViewMode) {}

  fun getEnabledCategories(): Set<CalendarCategory> = CalendarCategory.entries.toSet()
  fun setEnabledCategories(categories: Set<CalendarCategory>) {}

  fun getShowTodos(): Boolean = true
  fun setShowTodos(show: Boolean) {}

  fun getShowHolidays(): Boolean = true
  fun setShowHolidays(show: Boolean) {}
}

/**
 * In-memory implementation of [CalendarRepository] for previews and unit tests.
 */
class InMemoryCalendarRepository(
  initialEvents: List<CalendarEvent> = emptyList(),
  initialViewMode: CalendarViewMode = CalendarViewMode.SCHEDULE,
  initialEnabledCategories: Set<CalendarCategory> = CalendarCategory.entries.toSet(),
  initialShowTodos: Boolean = true,
  initialShowHolidays: Boolean = true,
) : CalendarRepository {
  private var events: List<CalendarEvent> = initialEvents
  private var viewMode: CalendarViewMode = initialViewMode
  private var enabledCategories: Set<CalendarCategory> = initialEnabledCategories
  private var showTodos: Boolean = initialShowTodos
  private var showHolidays: Boolean = initialShowHolidays

  override fun loadEvents(): List<CalendarEvent> = events

  override fun saveEvents(events: List<CalendarEvent>) {
    this.events = events
  }

  override fun getViewMode(): CalendarViewMode = viewMode

  override fun setViewMode(mode: CalendarViewMode) {
    this.viewMode = mode
  }

  override fun getEnabledCategories(): Set<CalendarCategory> = enabledCategories

  override fun setEnabledCategories(categories: Set<CalendarCategory>) {
    this.enabledCategories = categories
  }

  override fun getShowTodos(): Boolean = showTodos

  override fun setShowTodos(show: Boolean) {
    this.showTodos = show
  }

  override fun getShowHolidays(): Boolean = showHolidays

  override fun setShowHolidays(show: Boolean) {
    this.showHolidays = show
  }
}

/**
 * SharedPreferences-backed implementation of [CalendarRepository].
 */
class SharedPreferencesCalendarRepository(
  private val prefs: SharedPreferences,
) : CalendarRepository {

  constructor(context: Context) : this(
    context.getSharedPreferences(CALENDAR_PREFS_NAME, Context.MODE_PRIVATE)
  )

  override fun loadEvents(): List<CalendarEvent> {
    val raw = prefs.getString(KEY_CALENDAR_EVENTS_JSON, null)
    if (raw.isNullOrBlank()) return emptyList()
    return CalendarEngine.parseEventsJson(raw)
  }

  override fun saveEvents(events: List<CalendarEvent>) {
    val persistedOnly = events.filter { !it.isBuiltInHoliday && it.linkedTodoTaskId == null && it.title.isNotBlank() }
    val json = CalendarEngine.serializeEventsJson(persistedOnly)
    prefs.edit().putString(KEY_CALENDAR_EVENTS_JSON, json).apply()
  }

  override fun getViewMode(): CalendarViewMode {
    val raw = prefs.getString(KEY_CALENDAR_VIEW_MODE, CalendarViewMode.SCHEDULE.name)
    return CalendarViewMode.fromNameOrDefault(raw)
  }

  override fun setViewMode(mode: CalendarViewMode) {
    prefs.edit().putString(KEY_CALENDAR_VIEW_MODE, mode.name).apply()
  }

  override fun getEnabledCategories(): Set<CalendarCategory> {
    if (!prefs.contains(KEY_CALENDAR_ENABLED_CATEGORIES)) {
      return CalendarCategory.entries.toSet()
    }
    val raw = prefs.getString(KEY_CALENDAR_ENABLED_CATEGORIES, "") ?: ""
    if (raw.isBlank()) return emptySet()
    return raw.split(",")
      .mapNotNull { token ->
        val trimmed = token.trim()
        CalendarCategory.entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
      }
      .toSet()
  }

  override fun setEnabledCategories(categories: Set<CalendarCategory>) {
    val serialized = categories.joinToString(",") { it.name }
    prefs.edit().putString(KEY_CALENDAR_ENABLED_CATEGORIES, serialized).apply()
  }

  override fun getShowTodos(): Boolean =
    prefs.getBoolean(KEY_CALENDAR_SHOW_TODOS, true)

  override fun setShowTodos(show: Boolean) {
    prefs.edit().putBoolean(KEY_CALENDAR_SHOW_TODOS, show).apply()
  }

  override fun getShowHolidays(): Boolean =
    prefs.getBoolean(KEY_CALENDAR_SHOW_HOLIDAYS, true)

  override fun setShowHolidays(show: Boolean) {
    prefs.edit().putBoolean(KEY_CALENDAR_SHOW_HOLIDAYS, show).apply()
  }

  companion object {
    const val PREFS_NAME = CALENDAR_PREFS_NAME
  }
}

/**
 * Pending widget or cross-tool navigation request into [CalendarScreen].
 */
data class CalendarNavigationRequest(
  val targetDay: CalendarDay? = null,
  val openAddEvent: Boolean = false,
  val openEventId: String? = null,
  val viewMode: CalendarViewMode? = null,
)

/**
 * Core Calendar calculation engine: month grid layout, recurrence expansion, holiday generation,
 * `.ics` iCalendar import/export, To-Do task projection, and time formatting.
 */
object CalendarEngine {

  var pendingNavigationRequest by mutableStateOf<CalendarNavigationRequest?>(null)
    private set

  fun requestNavigation(
    targetDay: CalendarDay? = null,
    openAddEvent: Boolean = false,
    openEventId: String? = null,
    viewMode: CalendarViewMode? = null,
  ) {
    pendingNavigationRequest = CalendarNavigationRequest(
      targetDay = targetDay,
      openAddEvent = openAddEvent,
      openEventId = openEventId,
      viewMode = viewMode,
    )
  }

  fun consumePendingNavigationRequest(): CalendarNavigationRequest? {
    val req = pendingNavigationRequest
    pendingNavigationRequest = null
    return req
  }

  fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

  fun daysInMonth(year: Int, month: Int): Int {
    return when (month.coerceIn(1, 12)) {
      1, 3, 5, 7, 8, 10, 12 -> 31
      4, 6, 9, 11 -> 30
      2 -> if (isLeapYear(year)) 29 else 28
      else -> 30
    }
  }

  /**
   * Builds a 42-day (6-week × 7-column, Sunday-first) month grid for [year] and [month] (`1..12`),
   * matching Google Calendar's monthly grid layout.
   */
  fun buildMonthGridDays(year: Int, month: Int): List<CalendarDay> {
    val firstDay = CalendarDay(year, month.coerceIn(1, 12), 1)
    // Sunday = 1 -> leading offset 0; Saturday = 7 -> leading offset 6
    val leadingDays = (firstDay.dayOfWeek - Calendar.SUNDAY).coerceIn(0, 6)
    val gridStart = firstDay.plusDays(-leadingDays)
    return (0 until 42).map { offset -> gridStart.plusDays(offset) }
  }

  /**
   * Builds the 7 days (Sunday through Saturday) of the week containing [day].
   */
  fun buildWeekDays(day: CalendarDay): List<CalendarDay> {
    val offsetFromSunday = (day.dayOfWeek - Calendar.SUNDAY).coerceIn(0, 6)
    val sunday = day.plusDays(-offsetFromSunday)
    return (0 until 7).map { sunday.plusDays(it) }
  }

  /**
   * Builds a consecutive list of [count] days starting from [startDay].
   */
  fun buildConsecutiveDays(startDay: CalendarDay, count: Int): List<CalendarDay> {
    val safeCount = count.coerceIn(1, 14)
    return (0 until safeCount).map { startDay.plusDays(it) }
  }

  /**
   * Generates built-in annual holidays and seasonal observances for [year].
   */
  fun builtInHolidaysForYear(year: Int): List<CalendarEvent> {
    val holidays = mutableListOf<Triple<Int, Int, Pair<String, String>>>()

    // Fixed-date holidays & observances
    holidays.add(Triple(1, 1, "New Year's Day" to "First day of the calendar year"))
    holidays.add(Triple(2, 14, "Valentine's Day" to "Celebration of love and affection"))
    holidays.add(Triple(3, 17, "St. Patrick's Day" to "Cultural and heritage celebration"))
    holidays.add(Triple(4, 22, "Earth Day" to "Annual global environmental protection day"))
    holidays.add(Triple(6, 19, "Juneteenth" to "Freedom and emancipation commemoration"))
    holidays.add(Triple(6, 21, "June Solstice" to "Astronomical longest day / shortest night"))
    holidays.add(Triple(7, 4, "Independence Day" to "National Independence Day holiday"))
    holidays.add(Triple(10, 31, "Halloween" to "All Hallows' Eve celebration"))
    holidays.add(Triple(11, 11, "Veterans Day" to "Honoring military veterans"))
    holidays.add(Triple(12, 21, "December Solstice" to "Astronomical winter/summer solstice"))
    holidays.add(Triple(12, 24, "Christmas Eve" to "Evening before Christmas Day"))
    holidays.add(Triple(12, 25, "Christmas Day" to "Annual Christmas holiday celebration"))
    holidays.add(Triple(12, 31, "New Year's Eve" to "Final day of the calendar year"))

    // Nth weekday of month holidays
    // MLK Day: 3rd Monday in January
    nthDayOfWeekInMonth(year, 1, Calendar.MONDAY, 3)?.let { d ->
      holidays.add(Triple(1, d, "Martin Luther King Jr. Day" to "Third Monday of January"))
    }
    // Presidents' Day: 3rd Monday in February
    nthDayOfWeekInMonth(year, 2, Calendar.MONDAY, 3)?.let { d ->
      holidays.add(Triple(2, d, "Presidents' Day" to "Third Monday of February"))
    }
    // Mother's Day: 2nd Sunday in May
    nthDayOfWeekInMonth(year, 5, Calendar.SUNDAY, 2)?.let { d ->
      holidays.add(Triple(5, d, "Mother's Day" to "Second Sunday of May"))
    }
    // Memorial Day: Last Monday in May
    lastDayOfWeekInMonth(year, 5, Calendar.MONDAY)?.let { d ->
      holidays.add(Triple(5, d, "Memorial Day" to "Last Monday of May"))
    }
    // Father's Day: 3rd Sunday in June
    nthDayOfWeekInMonth(year, 6, Calendar.SUNDAY, 3)?.let { d ->
      holidays.add(Triple(6, d, "Father's Day" to "Third Sunday of June"))
    }
    // Labor Day: 1st Monday in September
    nthDayOfWeekInMonth(year, 9, Calendar.MONDAY, 1)?.let { d ->
      holidays.add(Triple(9, d, "Labor Day" to "First Monday of September"))
    }
    // Indigenous Peoples' Day: 2nd Monday in October
    nthDayOfWeekInMonth(year, 10, Calendar.MONDAY, 2)?.let { d ->
      holidays.add(Triple(10, d, "Indigenous Peoples' Day" to "Second Monday of October"))
    }
    // Thanksgiving: 4th Thursday in November
    nthDayOfWeekInMonth(year, 11, Calendar.THURSDAY, 4)?.let { d ->
      holidays.add(Triple(11, d, "Thanksgiving Day" to "Fourth Thursday of November"))
    }

    return holidays.map { (month, day, titleAndDesc) ->
      val calDay = CalendarDay(year, month, day)
      val idSlug = titleAndDesc.first.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")
      CalendarEvent(
        id = "holiday_${year}_${month}_${day}_$idSlug",
        title = titleAndDesc.first,
        description = titleAndDesc.second,
        location = "Observance",
        startMillis = calDay.toStartOfDayMillis(),
        endMillis = calDay.toEndOfDayMillis(),
        isAllDay = true,
        category = CalendarCategory.HOLIDAYS,
        color = CalendarEventColor.BASIL,
        recurrence = CalendarRecurrence.NONE,
        reminderMinutesBefore = CalendarReminderOption.NONE.minutesBefore,
        isBuiltInHoliday = true,
      )
    }
  }

  fun nthDayOfWeekInMonth(year: Int, month: Int, dayOfWeek: Int, nth: Int): Int? {
    if (nth < 1 || month !in 1..12) return null
    val maxDays = daysInMonth(year, month)
    var count = 0
    for (d in 1..maxDays) {
      if (CalendarDay(year, month, d).dayOfWeek == dayOfWeek) {
        count++
        if (count == nth) return d
      }
    }
    return null
  }

  fun lastDayOfWeekInMonth(year: Int, month: Int, dayOfWeek: Int): Int? {
    if (month !in 1..12) return null
    val maxDays = daysInMonth(year, month)
    for (d in maxDays downTo 1) {
      if (CalendarDay(year, month, d).dayOfWeek == dayOfWeek) {
        return d
      }
    }
    return null
  }

  /**
   * Projects scheduled [TodoItem]s from the To Do tool into [CalendarEvent]s so scheduled tasks
   * appear seamlessly on the Google Calendar views.
   */
  fun projectTodoTasksToCalendarEvents(
    tasks: List<TodoItem>,
    completedTasks: List<TodoItem> = emptyList(),
  ): List<CalendarEvent> {
    val activeProjected = tasks.mapNotNull { task ->
      val scheduledAt = task.scheduledAtMillis ?: return@mapNotNull null
      val endAt = task.scheduledEndMillis?.takeIf { it > scheduledAt } ?: (scheduledAt + 1_800_000L)
      val calRecurrence = when (task.recurrenceRule?.unit) {
        TodoRecurrenceUnit.DAY -> if (task.recurrenceRule.interval == 1) CalendarRecurrence.DAILY else CalendarRecurrence.NONE
        TodoRecurrenceUnit.WEEK -> when (task.recurrenceRule.interval) {
          1 -> CalendarRecurrence.WEEKLY
          2 -> CalendarRecurrence.BIWEEKLY
          else -> CalendarRecurrence.NONE
        }
        TodoRecurrenceUnit.WEEKDAYS -> CalendarRecurrence.WEEKDAYS
        TodoRecurrenceUnit.MONTH -> CalendarRecurrence.MONTHLY
        TodoRecurrenceUnit.YEAR -> CalendarRecurrence.YEARLY
        else -> CalendarRecurrence.NONE
      }
      CalendarEvent(
        id = "todo_task_${task.id}",
        title = task.title,
        description = task.buildNotificationContent(),
        location = "To Do Task",
        startMillis = scheduledAt,
        endMillis = endAt,
        isAllDay = false,
        category = CalendarCategory.TASKS,
        color = CalendarEventColor.LAVENDER,
        recurrence = calRecurrence,
        reminderMinutesBefore = CalendarReminderOption.NONE.minutesBefore,
        linkedTodoTaskId = task.id,
        isCompletedTask = false,
      )
    }

    val completedProjected = completedTasks.mapNotNull { task ->
      val scheduledAt = task.scheduledAtMillis ?: return@mapNotNull null
      val endAt = task.scheduledEndMillis?.takeIf { it > scheduledAt } ?: (scheduledAt + 1_800_000L)
      CalendarEvent(
        id = "todo_completed_${task.id}",
        title = "✓ ${task.title}",
        description = task.notes,
        location = "Completed To Do Task",
        startMillis = scheduledAt,
        endMillis = endAt,
        isAllDay = false,
        category = CalendarCategory.TASKS,
        color = CalendarEventColor.GRAPHITE,
        recurrence = CalendarRecurrence.NONE,
        reminderMinutesBefore = CalendarReminderOption.NONE.minutesBefore,
        linkedTodoTaskId = task.id,
        isCompletedTask = true,
      )
    }

    return activeProjected + completedProjected
  }

  /**
   * Determines whether [event] occurs on [day] (taking into account multi-day spans and [CalendarRecurrence]).
   * If it occurs, returns its [CalendarEventOccurrence] for [day]; otherwise `null`.
   */
  fun occurrenceForDay(event: CalendarEvent, day: CalendarDay): CalendarEventOccurrence? {
    val eventStartDay = event.startDay
    val eventEndDay = event.endDay
    if (day < eventStartDay) return null

    val startCal = Calendar.getInstance().apply { timeInMillis = event.startMillis }
    val startHour = startCal.get(Calendar.HOUR_OF_DAY)
    val startMinute = startCal.get(Calendar.MINUTE)
    val duration = (event.endMillis - event.startMillis).coerceAtLeast(if (event.isAllDay) 86_400_000L else 1_800_000L)

    // Direct match within initial [eventStartDay..eventEndDay]
    if (day in eventStartDay..eventEndDay) {
      val instStart = if (event.isAllDay || day > eventStartDay) {
        day.toStartOfDayMillis()
      } else {
        day.withTime(startHour, startMinute)
      }
      val instEnd = if (event.isAllDay) {
        day.toEndOfDayMillis()
      } else if (day < eventEndDay) {
        day.toEndOfDayMillis()
      } else {
        (instStart + duration).coerceAtMost(day.toEndOfDayMillis())
      }
      return CalendarEventOccurrence(
        event = event,
        occurrenceDay = day,
        instanceStartMillis = instStart,
        instanceEndMillis = instEnd,
      )
    }

    // Check recurrence rules for days after eventStartDay
    if (event.recurrence == CalendarRecurrence.NONE) return null

    val daysBetween = daysBetween(eventStartDay, day)
    if (daysBetween <= 0) return null

    val matchesRecurrence = when (event.recurrence) {
      CalendarRecurrence.NONE -> false
      CalendarRecurrence.DAILY -> true
      CalendarRecurrence.WEEKLY -> daysBetween % 7 == 0
      CalendarRecurrence.BIWEEKLY -> daysBetween % 14 == 0
      CalendarRecurrence.WEEKDAYS -> !day.isWeekend
      CalendarRecurrence.MONTHLY -> day.dayOfMonth == eventStartDay.dayOfMonth
      CalendarRecurrence.YEARLY -> day.month == eventStartDay.month && day.dayOfMonth == eventStartDay.dayOfMonth
    }

    if (!matchesRecurrence) return null

    val instStart = if (event.isAllDay) {
      day.toStartOfDayMillis()
    } else {
      day.withTime(startHour, startMinute)
    }
    val instEnd = if (event.isAllDay) {
      day.toEndOfDayMillis()
    } else {
      (instStart + duration).coerceAtMost(day.toEndOfDayMillis())
    }

    return CalendarEventOccurrence(
      event = event,
      occurrenceDay = day,
      instanceStartMillis = instStart,
      instanceEndMillis = instEnd,
    )
  }

  fun daysBetween(start: CalendarDay, end: CalendarDay): Int {
    val diffMs = end.toStartOfDayMillis() - start.toStartOfDayMillis()
    return kotlin.math.round(diffMs / 86_400_000.0).toInt()
  }

  /**
   * Returns all event occurrences on [day], sorted with all-day events first and then chronologically by start time.
   */
  fun occurrencesForDay(
    events: List<CalendarEvent>,
    day: CalendarDay,
    enabledCategories: Set<CalendarCategory> = CalendarCategory.entries.toSet(),
  ): List<CalendarEventOccurrence> {
    return events
      .asSequence()
      .filter { it.category in enabledCategories }
      .mapNotNull { event -> occurrenceForDay(event, day) }
      .sortedWith(
        compareByDescending<CalendarEventOccurrence> { it.event.isAllDay }
          .thenBy { it.instanceStartMillis }
          .thenBy { it.event.title.lowercase(Locale.ROOT) }
      )
      .toList()
  }

  /**
   * Returns a map of [CalendarDay] to its [CalendarEventOccurrence]s across [days].
   */
  fun occurrencesForDays(
    events: List<CalendarEvent>,
    days: List<CalendarDay>,
    enabledCategories: Set<CalendarCategory> = CalendarCategory.entries.toSet(),
  ): Map<CalendarDay, List<CalendarEventOccurrence>> {
    return days.associateWith { day ->
      occurrencesForDay(events, day, enabledCategories)
    }
  }

  /**
   * Searches events matching [query], optional [categoryFilter], and optional [favoritesOnly] flag.
   */
  fun searchEvents(
    events: List<CalendarEvent>,
    query: String,
    categoryFilter: CalendarCategory? = null,
    favoritesOnly: Boolean = false,
  ): List<CalendarEvent> {
    val trimmed = query.trim().lowercase(Locale.ROOT)
    val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    val seenHolidayTitles = mutableSetOf<String>()
    return events
      .filter { event ->
        if (favoritesOnly && !event.isFavorite) return@filter false
        if (categoryFilter != null && event.category != categoryFilter) return@filter false
        if (tokens.isEmpty()) return@filter true
        val searchable = buildString {
          append(event.title.lowercase(Locale.ROOT))
          append(' ')
          append(event.description.lowercase(Locale.ROOT))
          append(' ')
          append(event.location.lowercase(Locale.ROOT))
          append(' ')
          append(event.category.displayName.lowercase(Locale.ROOT))
          append(' ')
          append(event.color.displayName.lowercase(Locale.ROOT))
          append(' ')
          append(event.startDay.monthFullName.lowercase(Locale.ROOT))
          append(' ')
          append(event.startDay.dateKey)
        }
        tokens.all { searchable.contains(it) }
      }
      .sortedWith(
        compareByDescending<CalendarEvent> { it.isFavorite }
          .thenBy { it.startMillis }
          .thenBy { it.title.lowercase(Locale.ROOT) }
      )
      .filter { event ->
        if (!event.isBuiltInHoliday) {
          true
        } else {
          seenHolidayTitles.add(event.title.lowercase(Locale.ROOT))
        }
      }
  }

  fun parseEventsJson(rawJson: String?): List<CalendarEvent> {
    if (rawJson.isNullOrBlank()) return emptyList()
    return try {
      val arr = JSONArray(rawJson)
      val list = mutableListOf<CalendarEvent>()
      for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        val event = CalendarEvent.fromJson(obj)
        if (event.title.isNotBlank()) {
          list.add(event)
        }
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun serializeEventsJson(events: List<CalendarEvent>): String {
    val arr = JSONArray()
    events.forEach { event ->
      if (event.title.isNotBlank()) {
        arr.put(event.toJson())
      }
    }
    return arr.toString()
  }

  fun formatDayHeader(day: CalendarDay, today: CalendarDay = CalendarDay.today()): String {
    val prefix = when (day) {
      today -> "Today • "
      today.plusDays(1) -> "Tomorrow • "
      today.plusDays(-1) -> "Yesterday • "
      else -> ""
    }
    return "$prefix${day.dayOfWeekShortLabel}, ${day.monthShortName} ${day.dayOfMonth}, ${day.year}"
  }

  fun formatMonthYearTitle(year: Int, month: Int): String {
    val monthEnum = CalendarMonth.fromNumber(month)
    return "${monthEnum.fullName} $year"
  }

  fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.US)
    return sdf.format(Date(millis))
  }

  fun formatTimeRange(startMillis: Long, endMillis: Long, isAllDay: Boolean): String {
    if (isAllDay) return "All day"
    val startStr = formatTime(startMillis)
    val endStr = formatTime(maxOf(startMillis, endMillis))
    return if (startStr == endStr) startStr else "$startStr – $endStr"
  }

  fun formatHourSlotLabel(hour: Int): String {
    return when {
      hour == 0 || hour == 24 -> "12 AM"
      hour < 12 -> "$hour AM"
      hour == 12 -> "12 PM"
      else -> "${hour - 12} PM"
    }
  }

  /**
   * Formats a [CalendarEvent] into human-readable summary text for clipboard copy, sharing, or saving to Notes.
   */
  fun formatEventPlainText(event: CalendarEvent): String {
    return buildString {
      appendLine(event.title)
      appendLine("Date: ${formatDayHeader(event.startDay)}")
      appendLine("Time: ${formatTimeRange(event.startMillis, event.endMillis, event.isAllDay)}")
      appendLine("Calendar: ${event.category.displayName} (${event.color.displayName})")
      if (event.recurrence != CalendarRecurrence.NONE) {
        appendLine("Repeats: ${event.recurrence.label}")
      }
      if (event.location.isNotBlank()) {
        appendLine("Location: ${event.location}")
      }
      if (event.description.isNotBlank()) {
        appendLine()
        appendLine(event.description)
      }
    }.trim()
  }

  /**
   * Exports a list of [CalendarEvent]s into a standard RFC 5545 iCalendar (`.ics`) string.
   */
  fun exportEventsToIcs(events: List<CalendarEvent>): String {
    val utcFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
    val dateOnlyFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
    return buildString {
      appendLine("BEGIN:VCALENDAR")
      appendLine("VERSION:2.0")
      appendLine("PRODID:-//Toolbox//Offline Calendar 1.0//EN")
      appendLine("CALSCALE:GREGORIAN")
      for (event in events) {
        appendLine("BEGIN:VEVENT")
        appendLine("UID:${event.id}@toolbox.local")
        appendLine("SUMMARY:${escapeIcsText(event.title)}")
        if (event.isAllDay) {
          appendLine("DTSTART;VALUE=DATE:${dateOnlyFormat.format(Date(event.startMillis))}")
          val nextDayMillis = event.startDay.plusDays(1).toStartOfDayMillis()
          appendLine("DTEND;VALUE=DATE:${dateOnlyFormat.format(Date(nextDayMillis))}")
        } else {
          appendLine("DTSTART:${utcFormat.format(Date(event.startMillis))}")
          appendLine("DTEND:${utcFormat.format(Date(maxOf(event.startMillis + 900_000L, event.endMillis)))}")
        }
        if (event.description.isNotBlank()) {
          appendLine("DESCRIPTION:${escapeIcsText(event.description)}")
        }
        if (event.location.isNotBlank()) {
          appendLine("LOCATION:${escapeIcsText(event.location)}")
        }
        appendLine("CATEGORIES:${event.category.name}")
        appendLine("X-TOOLBOX-COLOR:${event.color.name}")
        if (event.recurrence != CalendarRecurrence.NONE) {
          val rrule = when (event.recurrence) {
            CalendarRecurrence.DAILY -> "FREQ=DAILY"
            CalendarRecurrence.WEEKLY -> "FREQ=WEEKLY"
            CalendarRecurrence.BIWEEKLY -> "FREQ=WEEKLY;INTERVAL=2"
            CalendarRecurrence.WEEKDAYS -> "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"
            CalendarRecurrence.MONTHLY -> "FREQ=MONTHLY"
            CalendarRecurrence.YEARLY -> "FREQ=YEARLY"
            CalendarRecurrence.NONE -> ""
          }
          if (rrule.isNotEmpty()) {
            appendLine("RRULE:$rrule")
          }
        }
        appendLine("END:VEVENT")
      }
      appendLine("END:VCALENDAR")
    }.trim()
  }

  /**
   * Parses RFC 5545 iCalendar (`.ics`) content into a list of [CalendarEvent]s.
   */
  fun importEventsFromIcs(icsContent: String): List<CalendarEvent> {
    if (icsContent.isBlank()) return emptyList()
    val results = mutableListOf<CalendarEvent>()
    val lines = icsContent.lines().map { it.trim() }

    var inEvent = false
    var uid = ""
    var summary = ""
    var description = ""
    var location = ""
    var startMillis: Long? = null
    var endMillis: Long? = null
    var isAllDay = false
    var category = CalendarCategory.PERSONAL
    var color: CalendarEventColor? = null
    var recurrence = CalendarRecurrence.NONE

    for (line in lines) {
      when {
        line.equals("BEGIN:VEVENT", ignoreCase = true) -> {
          inEvent = true
          uid = ""
          summary = ""
          description = ""
          location = ""
          startMillis = null
          endMillis = null
          isAllDay = false
          category = CalendarCategory.PERSONAL
          color = null
          recurrence = CalendarRecurrence.NONE
        }
        line.equals("END:VEVENT", ignoreCase = true) -> {
          if (inEvent && summary.isNotBlank() && startMillis != null) {
            val resolvedStart = startMillis!!
            val resolvedEnd = endMillis?.takeIf { it >= resolvedStart }
              ?: (resolvedStart + if (isAllDay) 86_400_000L else 3_600_000L)
            val resolvedColor = color ?: category.defaultColorSwatch
            results.add(
              CalendarEvent(
                id = uid.removeSuffix("@toolbox.local").ifBlank { UUID.randomUUID().toString() },
                title = summary,
                description = description,
                location = location,
                startMillis = resolvedStart,
                endMillis = resolvedEnd,
                isAllDay = isAllDay,
                category = category,
                color = resolvedColor,
                recurrence = recurrence,
              )
            )
          }
          inEvent = false
        }
        inEvent -> {
          val colonIndex = line.indexOf(':')
          if (colonIndex > 0) {
            val rawKey = line.substring(0, colonIndex).uppercase(Locale.ROOT)
            val rawVal = unescapeIcsText(line.substring(colonIndex + 1).trim())
            val baseKey = rawKey.substringBefore(';')
            when (baseKey) {
              "UID" -> uid = rawVal
              "SUMMARY" -> summary = rawVal
              "DESCRIPTION" -> description = rawVal
              "LOCATION" -> location = rawVal
              "CATEGORIES" -> category = CalendarCategory.fromNameOrDefault(rawVal.substringBefore(','))
              "X-TOOLBOX-COLOR" -> color = CalendarEventColor.fromNameOrDefault(rawVal)
              "DTSTART" -> {
                val parsed = parseIcsDateTime(rawVal, rawKey.contains("VALUE=DATE"))
                if (parsed != null) {
                  startMillis = parsed.first
                  isAllDay = parsed.second
                }
              }
              "DTEND" -> {
                val parsed = parseIcsDateTime(rawVal, rawKey.contains("VALUE=DATE"))
                if (parsed != null) {
                  endMillis = parsed.first
                }
              }
              "RRULE" -> {
                recurrence = when {
                  rawVal.contains("BYDAY=MO,TU,WE,TH,FR", ignoreCase = true) -> CalendarRecurrence.WEEKDAYS
                  rawVal.contains("FREQ=DAILY", ignoreCase = true) -> CalendarRecurrence.DAILY
                  rawVal.contains("FREQ=WEEKLY", ignoreCase = true) && rawVal.contains("INTERVAL=2", ignoreCase = true) -> CalendarRecurrence.BIWEEKLY
                  rawVal.contains("FREQ=WEEKLY", ignoreCase = true) -> CalendarRecurrence.WEEKLY
                  rawVal.contains("FREQ=MONTHLY", ignoreCase = true) -> CalendarRecurrence.MONTHLY
                  rawVal.contains("FREQ=YEARLY", ignoreCase = true) -> CalendarRecurrence.YEARLY
                  else -> CalendarRecurrence.NONE
                }
              }
            }
          }
        }
      }
    }
    return results
  }

  private fun parseIcsDateTime(value: String, forceDateOnly: Boolean): Pair<Long, Boolean>? {
    val clean = value.trim().removeSuffix("Z")
    return try {
      if (forceDateOnly || clean.length == 8) {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        val date = sdf.parse(clean.take(8)) ?: return null
        date.time to true
      } else {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
        val date = sdf.parse(clean.take(15)) ?: return null
        date.time to false
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun escapeIcsText(text: String): String {
    return text
      .replace("\\", "\\\\")
      .replace("\n", "\\n")
      .replace(",", "\\,")
      .replace(";", "\\;")
  }

  private fun unescapeIcsText(text: String): String {
    return text
      .replace("\\n", "\n")
      .replace("\\,", ",")
      .replace("\\;", ";")
      .replace("\\\\", "\\")
  }
}

/**
 * Notification and [AlarmManager] helper for scheduling exact event reminders and posting
 * Google Calendar-style reminder notifications when an event is about to begin.
 */
object CalendarNotificationHelper {

  const val CHANNEL_ID = "calendar_events_channel"
  const val CHANNEL_NAME = "Calendar Event Reminders"
  const val ACTION_CALENDAR_EVENT_ALARM = "com.example.toolbox.ACTION_CALENDAR_EVENT_ALARM"
  const val EXTRA_EVENT_ID = "extra_calendar_event_id"
  const val EXTRA_EVENT_TITLE = "extra_calendar_event_title"
  const val EXTRA_EVENT_SUMMARY = "extra_calendar_event_summary"
  const val EXTRA_TRIGGER_MILLIS = "extra_calendar_trigger_millis"

  private val scheduledAlarmEventIds: MutableSet<String> =
    Collections.synchronizedSet(mutableSetOf())

  fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        ?: return
      if (manager.getNotificationChannel(CHANNEL_ID) == null) {
        val channel = NotificationChannel(
          CHANNEL_ID,
          CHANNEL_NAME,
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Reminders for scheduled Calendar events"
          enableVibration(true)
        }
        manager.createNotificationChannel(channel)
      }
    }
  }

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun showEventNotification(context: Context, event: CalendarEvent) {
    ensureNotificationChannel(context)
    if (!hasNotificationPermission(context)) return

    val timeSummary = CalendarEngine.formatTimeRange(event.startMillis, event.endMillis, event.isAllDay)
    val dateSummary = CalendarEngine.formatDayHeader(event.startDay)
    val bodyParts = mutableListOf("$dateSummary • $timeSummary")
    if (event.location.isNotBlank()) {
      bodyParts.add("Location: ${event.location}")
    }
    if (event.description.isNotBlank()) {
      bodyParts.add(event.description)
    }
    val contentText = bodyParts.joinToString("\n")

    val launchIntent = createStandaloneToolIntent(context, Tool.CALENDAR)
    val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      event.id.hashCode(),
      launchIntent,
      pendingIntentFlags,
    )

    val backgroundColor = resolveShortcutBackgroundColor(context = context)
    val foregroundColor = resolveShortcutForegroundColor(backgroundColor)
    val iconBitmap = try {
      createToolShortcutBitmap(
        imageVector = Tool.CALENDAR.icon,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
      )
    } catch (_: Exception) {
      null
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_notification_todo_list)
      .setColor(event.color.accentColor.toArgb())
      .setContentTitle(event.title)
      .setContentText("$dateSummary • $timeSummary")
      .setStyle(
        NotificationCompat.BigTextStyle()
          .setBigContentTitle(event.title)
          .bigText(contentText)
          .setSummaryText(event.category.displayName)
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_EVENT)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(contentPendingIntent)

    if (iconBitmap != null) {
      builder.setLargeIcon(iconBitmap)
    }

    try {
      NotificationManagerCompat.from(context).notify(event.id.hashCode(), builder.build())
    } catch (_: SecurityException) {}
  }

  fun scheduleEventAlarm(context: Context, event: CalendarEvent) {
    val triggerAt = event.triggerAlarmAtMillis() ?: return
    if (event.notifiedTriggerMillis == triggerAt) return
    if (triggerAt < System.currentTimeMillis() - 60_000L) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, CalendarAlarmReceiver::class.java).apply {
      action = ACTION_CALENDAR_EVENT_ALARM
      putExtra(EXTRA_EVENT_ID, event.id)
      putExtra(EXTRA_EVENT_TITLE, event.title)
      putExtra(
        EXTRA_EVENT_SUMMARY,
        CalendarEngine.formatTimeRange(event.startMillis, event.endMillis, event.isAllDay),
      )
      putExtra(EXTRA_TRIGGER_MILLIS, triggerAt)
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      event.id.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val safeTrigger = maxOf(System.currentTimeMillis() + 500L, triggerAt)
    try {
      val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
      } else {
        true
      }
      if (canExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, safeTrigger, pendingIntent)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, safeTrigger, pendingIntent)
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, safeTrigger, pendingIntent)
      }
    } catch (_: SecurityException) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, safeTrigger, pendingIntent)
      } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, safeTrigger, pendingIntent)
      }
    }
    scheduledAlarmEventIds.add(event.id)
  }

  fun cancelEventAlarm(context: Context, eventId: String) {
    scheduledAlarmEventIds.remove(eventId)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, CalendarAlarmReceiver::class.java).apply {
      action = ACTION_CALENDAR_EVENT_ALARM
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      eventId.hashCode(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  fun syncEventAlarms(context: Context, events: List<CalendarEvent>) {
    ensureNotificationChannel(context)
    val activeIds = events.map { it.id }.toSet()
    val removed = synchronized(scheduledAlarmEventIds) {
      scheduledAlarmEventIds.toSet() - activeIds
    }
    removed.forEach { cancelEventAlarm(context, it) }
    events.forEach { event ->
      val trigger = event.triggerAlarmAtMillis()
      if (trigger != null && event.notifiedTriggerMillis != trigger && trigger >= System.currentTimeMillis() - 60_000L) {
        scheduleEventAlarm(context, event)
      } else {
        cancelEventAlarm(context, event.id)
      }
    }
  }
}

/**
 * BroadcastReceiver that fires when a scheduled [CalendarEvent] reminder reaches its trigger time,
 * or reschedules active calendar reminders on device boot.
 */
class CalendarAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action ?: return
    val repo = SharedPreferencesCalendarRepository(context)

    if (action == Intent.ACTION_BOOT_COMPLETED) {
      CalendarNotificationHelper.syncEventAlarms(context, repo.loadEvents())
      return
    }

    if (action == CalendarNotificationHelper.ACTION_CALENDAR_EVENT_ALARM) {
      val eventId = intent.getStringExtra(CalendarNotificationHelper.EXTRA_EVENT_ID) ?: return
      val triggerMillis = intent.getLongExtra(CalendarNotificationHelper.EXTRA_TRIGGER_MILLIS, -1L)
        .takeIf { it > 0L }
      val events = repo.loadEvents()
      val target = events.firstOrNull { it.id == eventId } ?: return
      val updated = events.map { ev ->
        if (ev.id == eventId) {
          ev.copy(notifiedTriggerMillis = triggerMillis ?: ev.triggerAlarmAtMillis())
        } else {
          ev
        }
      }
      repo.saveEvents(updated)
      CalendarNotificationHelper.showEventNotification(context, target)
    }
  }
}
