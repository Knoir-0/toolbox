package com.example.toolbox.ui.tools

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CalendarContract
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
import com.example.toolbox.ui.main.AppPermission
import com.example.toolbox.ui.main.EXTRA_CALENDAR_DATE_KEY
import com.example.toolbox.ui.main.NavigationPreferencesBridge
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
import java.util.TimeZone
import java.util.UUID

const val CALENDAR_PREFS_NAME = "calendar_tool_preferences"
const val KEY_CALENDAR_EVENTS_JSON = "calendar_events_json"
const val KEY_CALENDAR_VIEW_MODE = "calendar_view_mode"
const val KEY_CALENDAR_ENABLED_CATEGORIES = "calendar_enabled_categories"
const val KEY_CALENDAR_SHOW_TODOS = "calendar_show_todos"
const val KEY_CALENDAR_SHOW_HOLIDAYS = "calendar_show_holidays"
const val KEY_CALENDAR_FAVORITE_EVENT_IDS = "calendar_favorite_event_ids"
const val KEY_CALENDAR_SYNC_DEVICE_CALENDARS = "calendar_sync_device_calendars"
const val KEY_CALENDAR_ENABLED_DEVICE_CALENDAR_IDS = "calendar_enabled_device_calendar_ids"
const val KEY_CALENDAR_DEFAULT_DEVICE_CALENDAR_ID = "calendar_default_device_calendar_id"
const val KEY_CALENDAR_HAS_PROMPTED_PERMISSION = "calendar_has_prompted_permission"
const val TARGET_CALENDAR_LOCAL_ID = -1L

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

    /**
     * Maps any raw ARGB calendar or event color from Android's [CalendarContract] to the closest
     * Google Calendar [CalendarEventColor] swatch so light and dark theme container/content contrast
     * tokens remain legible.
     */
    fun closestToArgb(argb: Int): CalendarEventColor {
      val r = (argb shr 16) and 0xFF
      val g = (argb shr 8) and 0xFF
      val b = argb and 0xFF
      return entries.minByOrNull { swatch ->
        val swatchRgb = swatch.hex.removePrefix("#").toIntOrNull(16) ?: 0x039BE5
        val sr = (swatchRgb shr 16) and 0xFF
        val sg = (swatchRgb shr 8) and 0xFF
        val sb = swatchRgb and 0xFF
        val dr = r - sr
        val dg = g - sg
        val db = b - sb
        dr * dr + dg * dg + db * db
      } ?: PEACOCK
    }
  }
}

/**
 * Metadata for a synced or local calendar exposed by Android's [CalendarContract.Calendars] provider.
 */
@Immutable
data class DeviceCalendarInfo(
  val id: Long,
  val displayName: String,
  val accountName: String = "",
  val accountType: String = "",
  val ownerAccount: String = "",
  val colorArgb: Int = 0xFF039BE5.toInt(),
  val isVisible: Boolean = true,
  val isPrimary: Boolean = false,
  val accessLevel: Int = CalendarContract.Calendars.CAL_ACCESS_OWNER,
) {
  val isWritable: Boolean
    get() = accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR

  val accentColor: Color
    get() = Color(colorArgb or 0xFF000000.toInt())

  val hexColor: String
    get() = String.format(Locale.US, "#%06X", colorArgb and 0xFFFFFF)

  val closestSwatch: CalendarEventColor
    get() = CalendarEventColor.closestToArgb(colorArgb)

  val accountSubtitle: String
    get() = when {
      accountName.isNotBlank() && !accountName.equals(displayName, ignoreCase = true) -> accountName
      ownerAccount.isNotBlank() && !ownerAccount.equals(displayName, ignoreCase = true) -> ownerAccount
      else -> if (isWritable) "On-device calendar" else "Read-only calendar"
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
  val deviceCalendarId: Long? = null,
  val deviceEventId: Long? = null,
  val deviceCalendarName: String? = null,
  val deviceAccountName: String? = null,
  val deviceCalendarColorArgb: Int? = null,
  val isDeviceEventWritable: Boolean = true,
  val isPreExpandedInstance: Boolean = false,
) {
  val durationMillis: Long
    get() = (endMillis - startMillis).coerceAtLeast(if (isAllDay) 86_400_000L else 900_000L)

  val startDay: CalendarDay
    get() = CalendarDay.fromMillis(startMillis)

  val endDay: CalendarDay
    get() = CalendarDay.fromMillis(maxOf(startMillis, endMillis - if (isAllDay) 1L else 0L))

  val isDeviceCalendarEvent: Boolean
    get() = deviceCalendarId != null && deviceEventId != null

  val isReadOnlyBuiltIn: Boolean
    get() = isBuiltInHoliday || linkedTodoTaskId != null || (isDeviceCalendarEvent && !isDeviceEventWritable)

  val calendarSourceLabel: String
    get() = deviceCalendarName?.takeIf { it.isNotBlank() } ?: category.displayName

  val favoriteKey: String
    get() = if (isDeviceCalendarEvent) "device_ev_${deviceCalendarId}_$deviceEventId" else id

  val reminderOption: CalendarReminderOption
    get() = CalendarReminderOption.fromMinutes(reminderMinutesBefore)

  fun triggerAlarmAtMillis(nowMillis: Long = System.currentTimeMillis()): Long? {
    if (reminderMinutesBefore < 0 || isBuiltInHoliday || linkedTodoTaskId != null || isDeviceCalendarEvent) return null
    val leadMillis = reminderMinutesBefore * 60_000L
    if (recurrence == CalendarRecurrence.NONE) {
      return startMillis - leadMillis
    }
    val initialTrigger = startMillis - leadMillis
    if (notifiedTriggerMillis != initialTrigger && initialTrigger >= nowMillis - 60_000L) {
      return initialTrigger
    }
    val searchStartDay = maxOf(startDay, CalendarDay.fromMillis(nowMillis).plusDays(-1))
    for (offset in 0..400) {
      val candidateDay = searchStartDay.plusDays(offset)
      val occ = CalendarEngine.occurrenceForDay(this, candidateDay) ?: continue
      val candidateTrigger = occ.instanceStartMillis - leadMillis
      if (notifiedTriggerMillis != candidateTrigger && candidateTrigger >= nowMillis - 60_000L) {
        return candidateTrigger
      }
    }
    return initialTrigger
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

  /**
   * Returns `true` if this occurrence is completely in the past relative to [today] and [nowMillis].
   * - Any occurrence on a day prior to [today] (`occurrenceDay < today`) is in the past.
   * - For [today], a timed (non-all-day) occurrence whose [instanceEndMillis] has already elapsed
   *   (`instanceEndMillis <= nowMillis`, when `nowMillis > 0L`) is in the past; all-day occurrences on
   *   [today] remain active for the entire day.
   */
  fun isPast(
    today: CalendarDay = CalendarDay.today(),
    nowMillis: Long = System.currentTimeMillis(),
  ): Boolean {
    if (occurrenceDay < today) return true
    if (occurrenceDay > today) return false
    if (event.isAllDay) return false
    return nowMillis > 0L && instanceEndMillis <= nowMillis
  }

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

  fun getSyncDeviceCalendars(): Boolean = true
  fun setSyncDeviceCalendars(sync: Boolean) {}

  fun getEnabledDeviceCalendarIds(): Set<Long>? = null
  fun setEnabledDeviceCalendarIds(ids: Set<Long>?) {}

  fun getPreferredTargetCalendarId(): Long? = null
  fun setPreferredTargetCalendarId(calendarId: Long?) {}

  fun getFavoriteEventKeys(): Set<String> = emptySet()
  fun setFavoriteEventKeys(keys: Set<String>) {}

  fun hasPromptedDeviceCalendarPermission(): Boolean = false
  fun setHasPromptedDeviceCalendarPermission(prompted: Boolean) {}
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
  initialSyncDeviceCalendars: Boolean = true,
  initialEnabledDeviceCalendarIds: Set<Long>? = null,
  initialPreferredTargetCalendarId: Long? = null,
  initialFavoriteEventKeys: Set<String> = emptySet(),
) : CalendarRepository {
  private var events: List<CalendarEvent> = initialEvents
  private var viewMode: CalendarViewMode = initialViewMode
  private var enabledCategories: Set<CalendarCategory> = initialEnabledCategories
  private var showTodos: Boolean = initialShowTodos
  private var showHolidays: Boolean = initialShowHolidays
  private var syncDeviceCalendars: Boolean = initialSyncDeviceCalendars
  private var enabledDeviceCalendarIds: Set<Long>? = initialEnabledDeviceCalendarIds
  private var preferredTargetCalendarId: Long? = initialPreferredTargetCalendarId
  private var favoriteEventKeys: Set<String> = initialFavoriteEventKeys
  private var promptedDevicePermission: Boolean = false

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

  override fun getSyncDeviceCalendars(): Boolean = syncDeviceCalendars

  override fun setSyncDeviceCalendars(sync: Boolean) {
    this.syncDeviceCalendars = sync
  }

  override fun getEnabledDeviceCalendarIds(): Set<Long>? = enabledDeviceCalendarIds

  override fun setEnabledDeviceCalendarIds(ids: Set<Long>?) {
    this.enabledDeviceCalendarIds = ids
  }

  override fun getPreferredTargetCalendarId(): Long? = preferredTargetCalendarId

  override fun setPreferredTargetCalendarId(calendarId: Long?) {
    this.preferredTargetCalendarId = calendarId
  }

  override fun getFavoriteEventKeys(): Set<String> = favoriteEventKeys

  override fun setFavoriteEventKeys(keys: Set<String>) {
    this.favoriteEventKeys = keys
  }

  override fun hasPromptedDeviceCalendarPermission(): Boolean = promptedDevicePermission

  override fun setHasPromptedDeviceCalendarPermission(prompted: Boolean) {
    this.promptedDevicePermission = prompted
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
    val persistedOnly = events.filter {
      !it.isBuiltInHoliday &&
        it.linkedTodoTaskId == null &&
        !it.isDeviceCalendarEvent &&
        it.title.isNotBlank()
    }
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

  override fun getSyncDeviceCalendars(): Boolean =
    prefs.getBoolean(KEY_CALENDAR_SYNC_DEVICE_CALENDARS, true)

  override fun setSyncDeviceCalendars(sync: Boolean) {
    prefs.edit().putBoolean(KEY_CALENDAR_SYNC_DEVICE_CALENDARS, sync).apply()
  }

  override fun getEnabledDeviceCalendarIds(): Set<Long>? {
    if (!prefs.contains(KEY_CALENDAR_ENABLED_DEVICE_CALENDAR_IDS)) return null
    val raw = prefs.getString(KEY_CALENDAR_ENABLED_DEVICE_CALENDAR_IDS, "") ?: ""
    if (raw.isBlank()) return emptySet()
    return raw.split(",")
      .mapNotNull { it.trim().toLongOrNull() }
      .toSet()
  }

  override fun setEnabledDeviceCalendarIds(ids: Set<Long>?) {
    if (ids == null) {
      prefs.edit().remove(KEY_CALENDAR_ENABLED_DEVICE_CALENDAR_IDS).apply()
    } else {
      val serialized = ids.sorted().joinToString(",")
      prefs.edit().putString(KEY_CALENDAR_ENABLED_DEVICE_CALENDAR_IDS, serialized).apply()
    }
  }

  override fun getPreferredTargetCalendarId(): Long? {
    if (!prefs.contains(KEY_CALENDAR_DEFAULT_DEVICE_CALENDAR_ID)) return null
    return prefs.getLong(KEY_CALENDAR_DEFAULT_DEVICE_CALENDAR_ID, TARGET_CALENDAR_LOCAL_ID)
  }

  override fun setPreferredTargetCalendarId(calendarId: Long?) {
    if (calendarId == null) {
      prefs.edit().remove(KEY_CALENDAR_DEFAULT_DEVICE_CALENDAR_ID).apply()
    } else {
      prefs.edit().putLong(KEY_CALENDAR_DEFAULT_DEVICE_CALENDAR_ID, calendarId).apply()
    }
  }

  override fun getFavoriteEventKeys(): Set<String> {
    val raw = prefs.getString(KEY_CALENDAR_FAVORITE_EVENT_IDS, null) ?: return emptySet()
    if (raw.isBlank()) return emptySet()
    return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
  }

  override fun setFavoriteEventKeys(keys: Set<String>) {
    prefs.edit().putString(KEY_CALENDAR_FAVORITE_EVENT_IDS, keys.sorted().joinToString(",")).apply()
  }

  override fun hasPromptedDeviceCalendarPermission(): Boolean =
    prefs.getBoolean(KEY_CALENDAR_HAS_PROMPTED_PERMISSION, false)

  override fun setHasPromptedDeviceCalendarPermission(prompted: Boolean) {
    prefs.edit().putBoolean(KEY_CALENDAR_HAS_PROMPTED_PERMISSION, prompted).apply()
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

    // Check recurrence rules for days after eventStartDay (skipped when already expanded via CalendarContract.Instances)
    if (event.isPreExpandedInstance || event.recurrence == CalendarRecurrence.NONE) return null

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
   * Local Toolbox events/holidays/tasks are filtered by [enabledCategories], whereas synced device calendar events
   * are filtered independently by [enabledDeviceCalendarIds] (when non-null).
   */
  fun occurrencesForDay(
    events: List<CalendarEvent>,
    day: CalendarDay,
    enabledCategories: Set<CalendarCategory> = CalendarCategory.entries.toSet(),
    enabledDeviceCalendarIds: Set<Long>? = null,
  ): List<CalendarEventOccurrence> {
    return events
      .asSequence()
      .filter { event ->
        if (event.isDeviceCalendarEvent) {
          enabledDeviceCalendarIds == null || event.deviceCalendarId in enabledDeviceCalendarIds
        } else {
          event.category in enabledCategories
        }
      }
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
    enabledDeviceCalendarIds: Set<Long>? = null,
  ): Map<CalendarDay, List<CalendarEventOccurrence>> {
    return days.associateWith { day ->
      occurrencesForDay(events, day, enabledCategories, enabledDeviceCalendarIds)
    }
  }

  /**
   * Filters [occurrences] to exclude past events relative to [today] and [nowMillis].
   */
  fun filterUpcomingOccurrences(
    occurrences: List<CalendarEventOccurrence>,
    today: CalendarDay = CalendarDay.today(),
    nowMillis: Long = System.currentTimeMillis(),
  ): List<CalendarEventOccurrence> {
    return occurrences.filterNot { it.isPast(today = today, nowMillis = nowMillis) }
  }

  /**
   * Builds the chronological upcoming schedule days and their non-past [CalendarEventOccurrence]s
   * for the Google Calendar Schedule (Agenda) view and the Calendar Schedule Home Screen AppWidget.
   *
   * Never includes days before [today] or timed events on [today] whose end time has already passed.
   */
  fun buildUpcomingScheduleDays(
    events: List<CalendarEvent>,
    anchorDay: CalendarDay = CalendarDay.today(),
    today: CalendarDay = CalendarDay.today(),
    nowMillis: Long = System.currentTimeMillis(),
    enabledCategories: Set<CalendarCategory> = CalendarCategory.entries.toSet(),
    horizonDays: Int = 60,
    enabledDeviceCalendarIds: Set<Long>? = null,
  ): Map<CalendarDay, List<CalendarEventOccurrence>> {
    val effectiveStartDay = maxOf(anchorDay, today)
    val endOfAnchorMonth = CalendarDay(
      effectiveStartDay.year,
      effectiveStartDay.month,
      daysInMonth(effectiveStartDay.year, effectiveStartDay.month),
    )
    val horizonEndDay = maxOf(
      endOfAnchorMonth,
      effectiveStartDay.plusDays(horizonDays.coerceIn(1, 366) - 1),
    )
    val totalDays = (daysBetween(effectiveStartDay, horizonEndDay) + 1).coerceIn(1, 366)
    val candidateDays = (0 until totalDays).map { offset -> effectiveStartDay.plusDays(offset) }

    val result = LinkedHashMap<CalendarDay, List<CalendarEventOccurrence>>()
    for (day in candidateDays) {
      val upcomingForDay = filterUpcomingOccurrences(
        occurrences = occurrencesForDay(events, day, enabledCategories, enabledDeviceCalendarIds),
        today = today,
        nowMillis = nowMillis,
      )
      if (upcomingForDay.isNotEmpty() || day == today || day == effectiveStartDay) {
        result[day] = upcomingForDay
      }
    }
    return result
  }

  /**
   * Searches events matching [query], optional [categoryFilter], optional [deviceCalendarIdFilter],
   * and optional [favoritesOnly] flag.
   */
  fun searchEvents(
    events: List<CalendarEvent>,
    query: String,
    categoryFilter: CalendarCategory? = null,
    favoritesOnly: Boolean = false,
    deviceCalendarIdFilter: Long? = null,
  ): List<CalendarEvent> {
    val trimmed = query.trim().lowercase(Locale.ROOT)
    val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    val seenHolidayTitles = mutableSetOf<String>()
    val seenRecurringDeviceEvents = mutableSetOf<String>()
    return events
      .filter { event ->
        if (favoritesOnly && !event.isFavorite) return@filter false
        if (deviceCalendarIdFilter != null && event.deviceCalendarId != deviceCalendarIdFilter) return@filter false
        if (categoryFilter != null && (event.isDeviceCalendarEvent || event.category != categoryFilter)) {
          return@filter false
        }
        if (tokens.isEmpty()) return@filter true
        val searchable = buildString {
          append(event.title.lowercase(Locale.ROOT))
          append(' ')
          append(event.description.lowercase(Locale.ROOT))
          append(' ')
          append(event.location.lowercase(Locale.ROOT))
          append(' ')
          append(event.calendarSourceLabel.lowercase(Locale.ROOT))
          append(' ')
          append(event.deviceAccountName.orEmpty().lowercase(Locale.ROOT))
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
        when {
          event.isBuiltInHoliday -> seenHolidayTitles.add(event.title.lowercase(Locale.ROOT))
          event.isDeviceCalendarEvent && event.recurrence != CalendarRecurrence.NONE ->
            seenRecurringDeviceEvents.add("${event.deviceCalendarId}_${event.deviceEventId}")
          else -> true
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
      appendLine("Calendar: ${event.calendarSourceLabel} (${event.color.displayName})")
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
   * Resolves which [DeviceCalendarInfo] IDs are active given [storedEnabledIds] (`null` = not yet customized,
   * defaulting to calendars marked visible in the system provider, or all calendars if none are marked visible).
   */
  fun resolveActiveDeviceCalendarIds(
    availableCalendars: List<DeviceCalendarInfo>,
    storedEnabledIds: Set<Long>?,
  ): Set<Long> {
    if (availableCalendars.isEmpty()) return emptySet()
    val validIds = availableCalendars.map { it.id }.toSet()
    if (storedEnabledIds != null) {
      return storedEnabledIds.intersect(validIds)
    }
    val visibleIds = availableCalendars.filter { it.isVisible }.map { it.id }.toSet()
    return visibleIds.ifEmpty { validIds }
  }

  /**
   * Resolves the default target device calendar ID when creating a new event (`null` = Toolbox Local).
   */
  fun resolveDefaultTargetDeviceCalendarId(
    preferredId: Long?,
    writableCalendars: List<DeviceCalendarInfo>,
    enabledDeviceCalendarIds: Set<Long>,
    syncEnabled: Boolean,
  ): Long? {
    if (!syncEnabled || writableCalendars.isEmpty()) return null
    if (preferredId == TARGET_CALENDAR_LOCAL_ID) return null
    if (preferredId != null && preferredId > 0L) {
      val explicit = writableCalendars.firstOrNull { it.id == preferredId }
      if (explicit != null) return explicit.id
    }
    return writableCalendars.firstOrNull { it.id in enabledDeviceCalendarIds && it.isPrimary }?.id
      ?: writableCalendars.firstOrNull { it.id in enabledDeviceCalendarIds }?.id
      ?: writableCalendars.firstOrNull { it.isPrimary }?.id
      ?: writableCalendars.first().id
  }

  /**
   * Converts UTC midnight all-day `[beginUtcMillis, endUtcMillis)` timestamps from Android's
   * [CalendarContract] into local [CalendarDay] start/end-of-day timestamps so timezones west or east
   * of UTC never shift all-day events onto the wrong date.
   */
  fun utcAllDayRangeToLocalMillis(
    beginUtcMillis: Long,
    endUtcMillis: Long,
  ): Pair<Long, Long> {
    val utcZone = TimeZone.getTimeZone("UTC")
    val startCal = Calendar.getInstance(utcZone).apply { timeInMillis = beginUtcMillis }
    val startDay = CalendarDay(
      year = startCal.get(Calendar.YEAR),
      month = startCal.get(Calendar.MONTH) + 1,
      dayOfMonth = startCal.get(Calendar.DAY_OF_MONTH),
    )
    val inclusiveEndUtc = maxOf(beginUtcMillis, endUtcMillis - 1L)
    val endCal = Calendar.getInstance(utcZone).apply { timeInMillis = inclusiveEndUtc }
    val rawEndDay = CalendarDay(
      year = endCal.get(Calendar.YEAR),
      month = endCal.get(Calendar.MONTH) + 1,
      dayOfMonth = endCal.get(Calendar.DAY_OF_MONTH),
    )
    val endDay = maxOf(startDay, rawEndDay)
    return startDay.toStartOfDayMillis() to endDay.toEndOfDayMillis()
  }

  /**
   * Converts local [startDay]..[endDay] into UTC midnight `[dtStartUtc, dtEndExclusiveUtc)` timestamps
   * as required by Android's [CalendarContract.Events] when `ALL_DAY = 1`.
   */
  fun localAllDayToUtcRangeMillis(
    startDay: CalendarDay,
    endDay: CalendarDay = startDay,
  ): Pair<Long, Long> {
    val utcZone = TimeZone.getTimeZone("UTC")
    val safeEnd = maxOf(startDay, endDay)
    val nextDay = safeEnd.plusDays(1)
    val startUtc = Calendar.getInstance(utcZone).apply {
      clear()
      set(startDay.year, (startDay.month - 1).coerceIn(0, 11), startDay.dayOfMonth, 0, 0, 0)
    }.timeInMillis
    val endUtc = Calendar.getInstance(utcZone).apply {
      clear()
      set(nextDay.year, (nextDay.month - 1).coerceIn(0, 11), nextDay.dayOfMonth, 0, 0, 0)
    }.timeInMillis
    return startUtc to endUtc
  }

  fun toRruleString(recurrence: CalendarRecurrence): String? {
    return when (recurrence) {
      CalendarRecurrence.NONE -> null
      CalendarRecurrence.DAILY -> "FREQ=DAILY"
      CalendarRecurrence.WEEKLY -> "FREQ=WEEKLY"
      CalendarRecurrence.BIWEEKLY -> "FREQ=WEEKLY;INTERVAL=2"
      CalendarRecurrence.WEEKDAYS -> "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"
      CalendarRecurrence.MONTHLY -> "FREQ=MONTHLY"
      CalendarRecurrence.YEARLY -> "FREQ=YEARLY"
    }
  }

  fun fromRruleString(rrule: String?): CalendarRecurrence {
    if (rrule.isNullOrBlank()) return CalendarRecurrence.NONE
    return when {
      rrule.contains("BYDAY=MO,TU,WE,TH,FR", ignoreCase = true) -> CalendarRecurrence.WEEKDAYS
      rrule.contains("FREQ=DAILY", ignoreCase = true) -> CalendarRecurrence.DAILY
      rrule.contains("FREQ=WEEKLY", ignoreCase = true) && rrule.contains("INTERVAL=2", ignoreCase = true) ->
        CalendarRecurrence.BIWEEKLY
      rrule.contains("FREQ=WEEKLY", ignoreCase = true) -> CalendarRecurrence.WEEKLY
      rrule.contains("FREQ=MONTHLY", ignoreCase = true) -> CalendarRecurrence.MONTHLY
      rrule.contains("FREQ=YEARLY", ignoreCase = true) -> CalendarRecurrence.YEARLY
      else -> CalendarRecurrence.NONE
    }
  }

  /**
   * Formats an RFC 5545 `DURATION` value (`P<n>D` for all-day events or `PT<s>S` for timed events)
   * required by Android's [CalendarContract.Events] whenever `RRULE` is non-null.
   */
  fun formatRfc5545Duration(
    startMillis: Long,
    endMillis: Long,
    isAllDay: Boolean,
  ): String {
    return if (isAllDay) {
      val sDay = CalendarDay.fromMillis(startMillis)
      val eDay = CalendarDay.fromMillis(maxOf(startMillis, endMillis - 1L))
      val days = (daysBetween(sDay, eDay) + 1).coerceAtLeast(1)
      "P${days}D"
    } else {
      val seconds = ((maxOf(startMillis + 900_000L, endMillis) - startMillis) / 1000L).coerceAtLeast(900L)
      "PT${seconds}S"
    }
  }

  fun inferCategoryForDeviceCalendar(
    calendarName: String,
    title: String = "",
  ): CalendarCategory {
    val combined = "${calendarName.lowercase(Locale.ROOT)} ${title.lowercase(Locale.ROOT)}"
    return when {
      combined.contains("birthday") -> CalendarCategory.BIRTHDAYS
      combined.contains("holiday") || combined.contains("observance") -> CalendarCategory.HOLIDAYS
      combined.contains("work") || combined.contains("office") -> CalendarCategory.WORK
      combined.contains("health") || combined.contains("fitness") || combined.contains("workout") ->
        CalendarCategory.HEALTH
      combined.contains("travel") || combined.contains("flight") || combined.contains("trip") ->
        CalendarCategory.TRAVEL
      else -> CalendarCategory.PERSONAL
    }
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

  /**
   * Evaluates a fired reminder alarm against the active events in [repository].
   * If the event still exists, has reminders enabled, matches [triggerMillis] (when provided),
   * and has not yet been notified for that trigger timestamp, marks it notified in [repository]
   * and returns the updated [CalendarEvent]. Returns `null` if the event was deleted, had its
   * reminder disabled, was rescheduled to a different trigger time, or was already notified.
   */
  fun consumeScheduledEventAlarm(
    repository: CalendarRepository,
    eventId: String?,
    triggerMillis: Long? = null,
    nowMillis: Long = System.currentTimeMillis(),
  ): CalendarEvent? {
    if (eventId.isNullOrBlank()) return null
    val events = repository.loadEvents()
    val matching = events.firstOrNull { it.id == eventId } ?: return null
    val expectedTrigger = matching.triggerAlarmAtMillis(nowMillis) ?: return null
    if (triggerMillis != null && triggerMillis > 0L && triggerMillis != expectedTrigger) {
      return null
    }
    val resolvedTrigger = triggerMillis?.takeIf { it > 0L } ?: expectedTrigger
    if (matching.notifiedTriggerMillis == resolvedTrigger) {
      return null
    }
    val notifiedEvent = matching.copy(notifiedTriggerMillis = resolvedTrigger)
    val updated = events.map { ev ->
      if (ev.id == matching.id) notifiedEvent else ev
    }
    repository.saveEvents(updated)
    return notifiedEvent
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
    if (!NavigationPreferencesBridge.isPermissionGranted(AppPermission.POST_NOTIFICATIONS)) {
      return false
    }
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

    val activeTrigger = event.notifiedTriggerMillis ?: event.triggerAlarmAtMillis()
    val occurrenceDay = if (activeTrigger != null && event.reminderMinutesBefore >= 0) {
      CalendarDay.fromMillis(activeTrigger + event.reminderMinutesBefore * 60_000L)
    } else {
      event.startDay
    }

    val timeSummary = CalendarEngine.formatTimeRange(event.startMillis, event.endMillis, event.isAllDay)
    val dateSummary = CalendarEngine.formatDayHeader(occurrenceDay)
    val bodyParts = mutableListOf("$dateSummary • $timeSummary")
    if (event.location.isNotBlank()) {
      bodyParts.add("Location: ${event.location}")
    }
    if (event.description.isNotBlank()) {
      bodyParts.add(event.description)
    }
    val contentText = bodyParts.joinToString("\n")

    val launchIntent = createStandaloneToolIntent(context, Tool.CALENDAR).apply {
      putExtra(EXTRA_CALENDAR_DATE_KEY, occurrenceDay.dateKey)
    }
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
      .setSmallIcon(R.drawable.ic_notification_calendar)
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
      NavigationPreferencesBridge.activeRepository?.recordPermissionUsage(
        AppPermission.POST_NOTIFICATIONS,
        Tool.CALENDAR,
      )
    } catch (_: SecurityException) {}
  }

  fun scheduleEventAlarm(context: Context, event: CalendarEvent) {
    val now = System.currentTimeMillis()
    val triggerAt = event.triggerAlarmAtMillis(now) ?: return
    if (event.notifiedTriggerMillis == triggerAt) return
    if (triggerAt < now - 60_000L) return

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

    val safeTrigger = maxOf(now + 500L, triggerAt)
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

  fun cancelEventAlarm(context: Context, eventId: String, dismissActiveNotification: Boolean = true) {
    scheduledAlarmEventIds.remove(eventId)
    if (dismissActiveNotification) {
      try {
        NotificationManagerCompat.from(context).cancel(eventId.hashCode())
      } catch (_: Throwable) {}
    }
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
    removed.forEach { cancelEventAlarm(context, it, dismissActiveNotification = true) }
    val now = System.currentTimeMillis()
    events.forEach { event ->
      val trigger = event.triggerAlarmAtMillis(now)
      if (trigger != null && event.notifiedTriggerMillis != trigger && trigger >= now - 60_000L) {
        scheduleEventAlarm(context, event)
      } else {
        cancelEventAlarm(context, event.id, dismissActiveNotification = false)
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
      NavigationPreferencesBridge.activeRepository?.recordPermissionUsage(
        AppPermission.RECEIVE_BOOT_COMPLETED,
        Tool.CALENDAR,
      )
      return
    }

    if (action == CalendarNotificationHelper.ACTION_CALENDAR_EVENT_ALARM) {
      val eventId = intent.getStringExtra(CalendarNotificationHelper.EXTRA_EVENT_ID) ?: return
      val triggerMillis = intent.getLongExtra(CalendarNotificationHelper.EXTRA_TRIGGER_MILLIS, -1L)
        .takeIf { it > 0L }
      val eventToNotify = CalendarEngine.consumeScheduledEventAlarm(
        repository = repo,
        eventId = eventId,
        triggerMillis = triggerMillis,
      )
      if (eventToNotify != null) {
        CalendarNotificationHelper.showEventNotification(context, eventToNotify)
        // If recurring, immediately schedule the next occurrence's reminder alarm
        if (eventToNotify.recurrence != CalendarRecurrence.NONE) {
          CalendarNotificationHelper.scheduleEventAlarm(context, eventToNotify)
        }
      }
    }
  }
}

/**
 * Helper for querying, inserting, updating, and deleting events in Android's system
 * [CalendarContract] provider (`READ_CALENDAR` / `WRITE_CALENDAR`), bridged with [AppPermission.CALENDAR].
 */
object DeviceCalendarHelper {

  private val CALENDARS_PROJECTION = arrayOf(
    CalendarContract.Calendars._ID,
    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
    CalendarContract.Calendars.ACCOUNT_NAME,
    CalendarContract.Calendars.ACCOUNT_TYPE,
    CalendarContract.Calendars.OWNER_ACCOUNT,
    CalendarContract.Calendars.CALENDAR_COLOR,
    CalendarContract.Calendars.VISIBLE,
    CalendarContract.Calendars.IS_PRIMARY,
    CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
  )

  private val INSTANCES_PROJECTION = arrayOf(
    CalendarContract.Instances.EVENT_ID,
    CalendarContract.Instances.CALENDAR_ID,
    CalendarContract.Instances.TITLE,
    CalendarContract.Instances.DESCRIPTION,
    CalendarContract.Instances.EVENT_LOCATION,
    CalendarContract.Instances.BEGIN,
    CalendarContract.Instances.END,
    CalendarContract.Instances.ALL_DAY,
    CalendarContract.Instances.DISPLAY_COLOR,
    CalendarContract.Instances.CALENDAR_COLOR,
    CalendarContract.Instances.RRULE,
    CalendarContract.Instances.HAS_ALARM,
    CalendarContract.Instances.STATUS,
  )

  @Volatile
  private var lastRecordedReadUsageMs: Long = 0L

  fun hasOsReadPermission(context: Context): Boolean {
    return try {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CALENDAR,
      ) == PackageManager.PERMISSION_GRANTED
    } catch (_: Throwable) {
      false
    }
  }

  fun hasOsWritePermission(context: Context): Boolean {
    return try {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_CALENDAR,
      ) == PackageManager.PERMISSION_GRANTED
    } catch (_: Throwable) {
      false
    }
  }

  fun hasReadPermission(context: Context): Boolean {
    return NavigationPreferencesBridge.isPermissionGranted(AppPermission.CALENDAR) &&
      hasOsReadPermission(context)
  }

  fun hasWritePermission(context: Context): Boolean {
    return NavigationPreferencesBridge.isPermissionGranted(AppPermission.CALENDAR) &&
      hasOsWritePermission(context)
  }

  private fun recordUsageThrottled(
    tool: Tool = Tool.CALENDAR,
    force: Boolean = false,
  ) {
    val now = System.currentTimeMillis()
    if (!force && now - lastRecordedReadUsageMs < 15_000L) return
    lastRecordedReadUsageMs = now
    NavigationPreferencesBridge.activeRepository?.recordPermissionUsage(
      AppPermission.CALENDAR,
      tool,
    )
  }

  /**
   * Queries all non-deleted calendars registered on the device via [CalendarContract.Calendars].
   */
  fun queryDeviceCalendars(
    context: Context,
    recordUsage: Boolean = true,
    tool: Tool = Tool.CALENDAR,
  ): List<DeviceCalendarInfo> {
    if (!hasReadPermission(context)) return emptyList()
    return try {
      val results = mutableListOf<DeviceCalendarInfo>()
      val selection = "${CalendarContract.Calendars.DELETED} = 0"
      context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        CALENDARS_PROJECTION,
        selection,
        null,
        null,
      )?.use { cursor ->
        val idIdx = cursor.getColumnIndex(CalendarContract.Calendars._ID)
        val nameIdx = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
        val accNameIdx = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
        val accTypeIdx = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE)
        val ownerIdx = cursor.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)
        val colorIdx = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)
        val visIdx = cursor.getColumnIndex(CalendarContract.Calendars.VISIBLE)
        val primIdx = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
        val accessIdx = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)

        while (cursor.moveToNext()) {
          if (idIdx < 0) continue
          val id = cursor.getLong(idIdx)
          val rawName = if (nameIdx >= 0 && !cursor.isNull(nameIdx)) cursor.getString(nameIdx).orEmpty().trim() else ""
          val accName = if (accNameIdx >= 0 && !cursor.isNull(accNameIdx)) cursor.getString(accNameIdx).orEmpty().trim() else ""
          val accType = if (accTypeIdx >= 0 && !cursor.isNull(accTypeIdx)) cursor.getString(accTypeIdx).orEmpty().trim() else ""
          val owner = if (ownerIdx >= 0 && !cursor.isNull(ownerIdx)) cursor.getString(ownerIdx).orEmpty().trim() else ""
          val colorArgb = if (colorIdx >= 0 && !cursor.isNull(colorIdx)) {
            cursor.getInt(colorIdx) or 0xFF000000.toInt()
          } else {
            0xFF039BE5.toInt()
          }
          val isVisible = if (visIdx >= 0 && !cursor.isNull(visIdx)) cursor.getInt(visIdx) != 0 else true
          val isPrimary = if (primIdx >= 0 && !cursor.isNull(primIdx)) cursor.getInt(primIdx) != 0 else false
          val accessLevel = if (accessIdx >= 0 && !cursor.isNull(accessIdx)) {
            cursor.getInt(accessIdx)
          } else {
            CalendarContract.Calendars.CAL_ACCESS_READ
          }
          val displayName = rawName.ifBlank { accName.ifBlank { "Calendar $id" } }

          results.add(
            DeviceCalendarInfo(
              id = id,
              displayName = displayName,
              accountName = accName,
              accountType = accType,
              ownerAccount = owner,
              colorArgb = colorArgb,
              isVisible = isVisible,
              isPrimary = isPrimary,
              accessLevel = accessLevel,
            )
          )
        }
      }
      if (recordUsage) {
        recordUsageThrottled(tool = tool)
      }
      results.sortedWith(
        compareByDescending<DeviceCalendarInfo> { it.isPrimary }
          .thenByDescending { it.isWritable }
          .thenBy { it.displayName.lowercase(Locale.ROOT) }
      )
    } catch (_: Throwable) {
      emptyList()
    }
  }

  private fun queryReminderMinutesByEventId(
    context: Context,
    eventIds: Set<Long>,
  ): Map<Long, Int> {
    if (eventIds.isEmpty()) return emptyMap()
    val result = mutableMapOf<Long, Int>()
    try {
      // Chunk IN clauses safely under SQLite's parameter limit
      for (chunk in eventIds.chunked(250)) {
        val idList = chunk.joinToString(",")
        val selection = "${CalendarContract.Reminders.EVENT_ID} IN ($idList) AND " +
          "(${CalendarContract.Reminders.METHOD} = ${CalendarContract.Reminders.METHOD_ALERT} OR " +
          "${CalendarContract.Reminders.METHOD} = ${CalendarContract.Reminders.METHOD_DEFAULT})"
        context.contentResolver.query(
          CalendarContract.Reminders.CONTENT_URI,
          arrayOf(CalendarContract.Reminders.EVENT_ID, CalendarContract.Reminders.MINUTES),
          selection,
          null,
          null,
        )?.use { cursor ->
          val evIdx = cursor.getColumnIndex(CalendarContract.Reminders.EVENT_ID)
          val minIdx = cursor.getColumnIndex(CalendarContract.Reminders.MINUTES)
          while (cursor.moveToNext()) {
            if (evIdx < 0 || minIdx < 0 || cursor.isNull(minIdx)) continue
            val evId = cursor.getLong(evIdx)
            val minutes = cursor.getInt(minIdx).coerceAtLeast(0)
            val existing = result[evId]
            if (existing == null || minutes < existing) {
              result[evId] = minutes
            }
          }
        }
      }
    } catch (_: Throwable) {}
    return result
  }

  /**
   * Queries expanded event occurrences from [CalendarContract.Instances] across `[rangeStartMillis, rangeEndMillis]`
   * for the specified [enabledCalendarIds].
   */
  fun queryEventsInRange(
    context: Context,
    rangeStartMillis: Long,
    rangeEndMillis: Long,
    calendarsById: Map<Long, DeviceCalendarInfo>,
    enabledCalendarIds: Set<Long>,
    favoriteKeys: Set<String> = emptySet(),
    recordUsage: Boolean = false,
    tool: Tool = Tool.CALENDAR,
  ): List<CalendarEvent> {
    if (enabledCalendarIds.isEmpty() || rangeEndMillis < rangeStartMillis || !hasReadPermission(context)) {
      return emptyList()
    }
    return try {
      data class RawInstance(
        val eventId: Long,
        val calendarId: Long,
        val title: String,
        val description: String,
        val location: String,
        val startMillis: Long,
        val endMillis: Long,
        val isAllDay: Boolean,
        val displayColorArgb: Int,
        val rrule: String?,
        val hasAlarm: Boolean,
      )

      val rawInstances = mutableListOf<RawInstance>()
      val alarmEventIds = mutableSetOf<Long>()

      CalendarContract.Instances.query(
        context.contentResolver,
        INSTANCES_PROJECTION,
        rangeStartMillis,
        rangeEndMillis,
      )?.use { cursor ->
        val evIdIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
        val calIdIdx = cursor.getColumnIndex(CalendarContract.Instances.CALENDAR_ID)
        val titleIdx = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
        val descIdx = cursor.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
        val locIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)
        val beginIdx = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
        val endIdx = cursor.getColumnIndex(CalendarContract.Instances.END)
        val allDayIdx = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY)
        val dispColorIdx = cursor.getColumnIndex(CalendarContract.Instances.DISPLAY_COLOR)
        val calColorIdx = cursor.getColumnIndex(CalendarContract.Instances.CALENDAR_COLOR)
        val rruleIdx = cursor.getColumnIndex(CalendarContract.Instances.RRULE)
        val alarmIdx = cursor.getColumnIndex(CalendarContract.Instances.HAS_ALARM)
        val statusIdx = cursor.getColumnIndex(CalendarContract.Instances.STATUS)

        while (cursor.moveToNext()) {
          if (evIdIdx < 0 || calIdIdx < 0 || beginIdx < 0) continue
          val calendarId = cursor.getLong(calIdIdx)
          if (calendarId !in enabledCalendarIds) continue
          val status = if (statusIdx >= 0 && !cursor.isNull(statusIdx)) cursor.getInt(statusIdx) else 0
          if (status == CalendarContract.Events.STATUS_CANCELED) continue

          val eventId = cursor.getLong(evIdIdx)
          val rawTitle = if (titleIdx >= 0 && !cursor.isNull(titleIdx)) cursor.getString(titleIdx).orEmpty().trim() else ""
          val title = rawTitle.ifBlank { "(No title)" }
          val desc = if (descIdx >= 0 && !cursor.isNull(descIdx)) cursor.getString(descIdx).orEmpty().trim() else ""
          val loc = if (locIdx >= 0 && !cursor.isNull(locIdx)) cursor.getString(locIdx).orEmpty().trim() else ""
          val rawBegin = cursor.getLong(beginIdx)
          val isAllDay = allDayIdx >= 0 && !cursor.isNull(allDayIdx) && cursor.getInt(allDayIdx) != 0
          val rawEnd = if (endIdx >= 0 && !cursor.isNull(endIdx)) {
            cursor.getLong(endIdx)
          } else {
            rawBegin + if (isAllDay) 86_400_000L else 3_600_000L
          }
          val (localStart, localEnd) = if (isAllDay) {
            CalendarEngine.utcAllDayRangeToLocalMillis(rawBegin, maxOf(rawBegin + 1L, rawEnd))
          } else {
            rawBegin to maxOf(rawBegin, rawEnd)
          }

          val calInfo = calendarsById[calendarId]
          val dispColor = when {
            dispColorIdx >= 0 && !cursor.isNull(dispColorIdx) -> cursor.getInt(dispColorIdx) or 0xFF000000.toInt()
            calColorIdx >= 0 && !cursor.isNull(calColorIdx) -> cursor.getInt(calColorIdx) or 0xFF000000.toInt()
            calInfo != null -> calInfo.colorArgb
            else -> 0xFF039BE5.toInt()
          }
          val rrule = if (rruleIdx >= 0 && !cursor.isNull(rruleIdx)) cursor.getString(rruleIdx) else null
          val hasAlarm = alarmIdx >= 0 && !cursor.isNull(alarmIdx) && cursor.getInt(alarmIdx) != 0
          if (hasAlarm) {
            alarmEventIds.add(eventId)
          }

          rawInstances.add(
            RawInstance(
              eventId = eventId,
              calendarId = calendarId,
              title = title,
              description = desc,
              location = loc,
              startMillis = localStart,
              endMillis = localEnd,
              isAllDay = isAllDay,
              displayColorArgb = dispColor,
              rrule = rrule,
              hasAlarm = hasAlarm,
            )
          )
        }
      }

      val reminderMinutesMap = queryReminderMinutesByEventId(context, alarmEventIds)
      if (recordUsage) {
        recordUsageThrottled(tool = tool)
      }

      rawInstances.map { inst ->
        val calInfo = calendarsById[inst.calendarId]
        val calName = calInfo?.displayName ?: "Device Calendar"
        val category = CalendarEngine.inferCategoryForDeviceCalendar(calName, inst.title)
        val swatch = CalendarEventColor.closestToArgb(inst.displayColorArgb)
        val reminderMins = if (inst.hasAlarm) {
          reminderMinutesMap[inst.eventId] ?: CalendarReminderOption.THIRTY_MINUTES.minutesBefore
        } else {
          CalendarReminderOption.NONE.minutesBefore
        }
        val favKey = "device_ev_${inst.calendarId}_${inst.eventId}"
        CalendarEvent(
          id = "device_cal_${inst.calendarId}_ev_${inst.eventId}_${inst.startMillis}",
          title = inst.title,
          description = inst.description,
          location = inst.location,
          startMillis = inst.startMillis,
          endMillis = inst.endMillis,
          isAllDay = inst.isAllDay,
          category = category,
          color = swatch,
          recurrence = CalendarEngine.fromRruleString(inst.rrule),
          reminderMinutesBefore = reminderMins,
          isFavorite = favKey in favoriteKeys,
          deviceCalendarId = inst.calendarId,
          deviceEventId = inst.eventId,
          deviceCalendarName = calName,
          deviceAccountName = calInfo?.accountName.orEmpty(),
          deviceCalendarColorArgb = calInfo?.colorArgb ?: inst.displayColorArgb,
          isDeviceEventWritable = calInfo?.isWritable ?: false,
          isPreExpandedInstance = true,
        )
      }
    } catch (_: Throwable) {
      emptyList()
    }
  }

  /**
   * Convenience helper for [CalendarWidgets] and [SearchEngine] that loads all enabled device calendar
   * events across [years].
   */
  fun queryEventsForYears(
    context: Context,
    years: Set<Int>,
    repository: CalendarRepository?,
    tool: Tool = Tool.CALENDAR,
  ): List<CalendarEvent> {
    if (years.isEmpty()) return emptyList()
    if (repository?.getSyncDeviceCalendars() == false) return emptyList()
    if (!hasReadPermission(context)) return emptyList()

    val calendars = queryDeviceCalendars(context, recordUsage = false, tool = tool)
    if (calendars.isEmpty()) return emptyList()
    val enabledIds = CalendarEngine.resolveActiveDeviceCalendarIds(
      availableCalendars = calendars,
      storedEnabledIds = repository?.getEnabledDeviceCalendarIds(),
    )
    if (enabledIds.isEmpty()) return emptyList()

    val minYear = years.minOrNull() ?: return emptyList()
    val maxYear = years.maxOrNull() ?: minYear
    // Include a 7-day buffer on both ends so week/month grids and UTC all-day offsets are always covered
    val rangeStart = CalendarDay(minYear, 1, 1).plusDays(-7).toStartOfDayMillis()
    val rangeEnd = CalendarDay(maxYear, 12, 31).plusDays(7).toEndOfDayMillis()
    return queryEventsInRange(
      context = context,
      rangeStartMillis = rangeStart,
      rangeEndMillis = rangeEnd,
      calendarsById = calendars.associateBy { it.id },
      enabledCalendarIds = enabledIds,
      favoriteKeys = repository?.getFavoriteEventKeys().orEmpty(),
      recordUsage = true,
      tool = tool,
    )
  }

  private fun resolveMatchingAccountColorKey(
    context: Context,
    calendar: DeviceCalendarInfo,
    desiredSwatch: CalendarEventColor,
  ): String? {
    if (calendar.accountName.isBlank() || calendar.accountType.isBlank()) return null
    return try {
      val selection = "${CalendarContract.Colors.ACCOUNT_NAME} = ? AND " +
        "${CalendarContract.Colors.ACCOUNT_TYPE} = ? AND " +
        "${CalendarContract.Colors.COLOR_TYPE} = ${CalendarContract.Colors.TYPE_EVENT}"
      val args = arrayOf(calendar.accountName, calendar.accountType)
      var bestKey: String? = null
      var bestDistance = Int.MAX_VALUE
      val targetRgb = desiredSwatch.hex.removePrefix("#").toIntOrNull(16) ?: return null
      val tr = (targetRgb shr 16) and 0xFF
      val tg = (targetRgb shr 8) and 0xFF
      val tb = targetRgb and 0xFF

      context.contentResolver.query(
        CalendarContract.Colors.CONTENT_URI,
        arrayOf(CalendarContract.Colors.COLOR_KEY, CalendarContract.Colors.COLOR),
        selection,
        args,
        null,
      )?.use { cursor ->
        val keyIdx = cursor.getColumnIndex(CalendarContract.Colors.COLOR_KEY)
        val colIdx = cursor.getColumnIndex(CalendarContract.Colors.COLOR)
        while (cursor.moveToNext()) {
          if (keyIdx < 0 || colIdx < 0 || cursor.isNull(keyIdx) || cursor.isNull(colIdx)) continue
          val key = cursor.getString(keyIdx) ?: continue
          val c = cursor.getInt(colIdx)
          val dr = ((c shr 16) and 0xFF) - tr
          val dg = ((c shr 8) and 0xFF) - tg
          val db = (c and 0xFF) - tb
          val dist = dr * dr + dg * dg + db * db
          if (dist < bestDistance) {
            bestDistance = dist
            bestKey = key
          }
        }
      }
      bestKey
    } catch (_: Throwable) {
      null
    }
  }

  private fun buildEventContentValues(
    context: Context,
    event: CalendarEvent,
    targetCalendar: DeviceCalendarInfo,
    includeCustomColorKey: Boolean = true,
  ): ContentValues {
    val rrule = CalendarEngine.toRruleString(event.recurrence)
    return ContentValues().apply {
      put(CalendarContract.Events.CALENDAR_ID, targetCalendar.id)
      put(CalendarContract.Events.TITLE, event.title.trim())
      put(CalendarContract.Events.DESCRIPTION, event.description.trim())
      put(CalendarContract.Events.EVENT_LOCATION, event.location.trim())
      put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
      put(CalendarContract.Events.HAS_ALARM, if (event.reminderMinutesBefore >= 0) 1 else 0)

      if (event.isAllDay) {
        val (utcStart, utcEnd) = CalendarEngine.localAllDayToUtcRangeMillis(event.startDay, event.endDay)
        put(CalendarContract.Events.DTSTART, utcStart)
        put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
        if (rrule != null) {
          put(CalendarContract.Events.RRULE, rrule)
          put(
            CalendarContract.Events.DURATION,
            CalendarEngine.formatRfc5545Duration(event.startMillis, event.endMillis, isAllDay = true),
          )
          putNull(CalendarContract.Events.DTEND)
        } else {
          put(CalendarContract.Events.DTEND, utcEnd)
          putNull(CalendarContract.Events.DURATION)
          putNull(CalendarContract.Events.RRULE)
        }
      } else {
        val safeEnd = maxOf(event.startMillis + 900_000L, event.endMillis)
        put(CalendarContract.Events.DTSTART, event.startMillis)
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        if (rrule != null) {
          put(CalendarContract.Events.RRULE, rrule)
          put(
            CalendarContract.Events.DURATION,
            CalendarEngine.formatRfc5545Duration(event.startMillis, safeEnd, isAllDay = false),
          )
          putNull(CalendarContract.Events.DTEND)
        } else {
          put(CalendarContract.Events.DTEND, safeEnd)
          putNull(CalendarContract.Events.DURATION)
          putNull(CalendarContract.Events.RRULE)
        }
      }

      if (includeCustomColorKey && event.color != targetCalendar.closestSwatch) {
        val colorKey = resolveMatchingAccountColorKey(context, targetCalendar, event.color)
        if (!colorKey.isNullOrBlank()) {
          put(CalendarContract.Events.EVENT_COLOR_KEY, colorKey)
        }
      }
    }
  }

  private fun syncRemindersForDeviceEvent(
    context: Context,
    eventId: Long,
    reminderMinutesBefore: Int,
  ) {
    try {
      context.contentResolver.delete(
        CalendarContract.Reminders.CONTENT_URI,
        "${CalendarContract.Reminders.EVENT_ID} = ?",
        arrayOf(eventId.toString()),
      )
      if (reminderMinutesBefore >= 0) {
        val values = ContentValues().apply {
          put(CalendarContract.Reminders.EVENT_ID, eventId)
          put(CalendarContract.Reminders.MINUTES, reminderMinutesBefore)
          put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
      }
    } catch (_: Throwable) {}
  }

  /**
   * Inserts a new event into [targetCalendar] via [CalendarContract.Events] and returns its system `eventId`,
   * or `null` if permissions are missing or the provider rejected the insert.
   */
  fun insertDeviceEvent(
    context: Context,
    event: CalendarEvent,
    targetCalendar: DeviceCalendarInfo,
  ): Long? {
    if (!hasWritePermission(context) || !targetCalendar.isWritable) return null
    return try {
      val uri = try {
        context.contentResolver.insert(
          CalendarContract.Events.CONTENT_URI,
          buildEventContentValues(context, event, targetCalendar, includeCustomColorKey = true),
        )
      } catch (_: IllegalArgumentException) {
        // Fallback without EVENT_COLOR_KEY for providers that disallow custom event colors
        context.contentResolver.insert(
          CalendarContract.Events.CONTENT_URI,
          buildEventContentValues(context, event, targetCalendar, includeCustomColorKey = false),
        )
      } ?: return null
      val newId = ContentUris.parseId(uri).takeIf { it > 0L } ?: return null
      syncRemindersForDeviceEvent(context, newId, event.reminderMinutesBefore)
      recordUsageThrottled(force = true)
      newId
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Updates an existing device calendar event (`eventId`) in [targetCalendar].
   * If the user moved the event to a different device calendar, inserts into [targetCalendar] and
   * removes the original event so cross-account moves succeed reliably.
   */
  fun updateDeviceEvent(
    context: Context,
    existingEventId: Long,
    existingCalendarId: Long?,
    updatedEvent: CalendarEvent,
    targetCalendar: DeviceCalendarInfo,
  ): Long? {
    if (!hasWritePermission(context) || !targetCalendar.isWritable) return null
    if (existingCalendarId != null && existingCalendarId != targetCalendar.id) {
      val newId = insertDeviceEvent(context, updatedEvent, targetCalendar) ?: return null
      deleteDeviceEvent(context, existingEventId)
      return newId
    }
    return try {
      val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, existingEventId)
      val rows = try {
        context.contentResolver.update(
          eventUri,
          buildEventContentValues(context, updatedEvent, targetCalendar, includeCustomColorKey = true),
          null,
          null,
        )
      } catch (_: IllegalArgumentException) {
        context.contentResolver.update(
          eventUri,
          buildEventContentValues(context, updatedEvent, targetCalendar, includeCustomColorKey = false),
          null,
          null,
        )
      }
      if (rows >= 0) {
        syncRemindersForDeviceEvent(context, existingEventId, updatedEvent.reminderMinutesBefore)
        recordUsageThrottled(force = true)
        existingEventId
      } else {
        null
      }
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Deletes an event (`deviceEventId`) from Android's [CalendarContract.Events] provider.
   */
  fun deleteDeviceEvent(
    context: Context,
    deviceEventId: Long,
  ): Boolean {
    if (!hasWritePermission(context)) return false
    return try {
      val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, deviceEventId)
      val deleted = context.contentResolver.delete(eventUri, null, null)
      recordUsageThrottled(force = true)
      deleted > 0
    } catch (_: Throwable) {
      false
    }
  }
}
