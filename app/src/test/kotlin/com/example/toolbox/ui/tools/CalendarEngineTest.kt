package com.example.toolbox.ui.tools

import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolboxDataRepositories
import com.example.toolbox.ui.main.exportDataToJson
import com.example.toolbox.ui.main.importDataFromJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalendarEngineTest {

  @Test
  fun daysInMonth_andIsLeapYear_computesCorrectDayCounts() {
    assertTrue(CalendarEngine.isLeapYear(2024))
    assertFalse(CalendarEngine.isLeapYear(2025))
    assertFalse(CalendarEngine.isLeapYear(1900))
    assertTrue(CalendarEngine.isLeapYear(2000))

    assertEquals(29, CalendarEngine.daysInMonth(2024, 2))
    assertEquals(28, CalendarEngine.daysInMonth(2026, 2))
    assertEquals(30, CalendarEngine.daysInMonth(2026, 11))
    assertEquals(31, CalendarEngine.daysInMonth(2026, 12))
  }

  @Test
  fun buildMonthGridDays_returns42SundayAlignedCells() {
    // November 2026 starts on Sunday (Nov 1, 2026)
    val gridNov2026 = CalendarEngine.buildMonthGridDays(2026, 11)
    assertEquals(42, gridNov2026.size)
    assertEquals(CalendarDay(2026, 11, 1), gridNov2026.first())
    assertEquals(Calendar.SUNDAY, gridNov2026.first().dayOfWeek)
    assertEquals(Calendar.SATURDAY, gridNov2026.last().dayOfWeek)

    // December 2026 starts on Tuesday (Dec 1, 2026) -> grid starts on Sunday Nov 29, 2026
    val gridDec2026 = CalendarEngine.buildMonthGridDays(2026, 12)
    assertEquals(42, gridDec2026.size)
    assertEquals(CalendarDay(2026, 11, 29), gridDec2026.first())
    assertEquals(Calendar.SUNDAY, gridDec2026.first().dayOfWeek)
  }

  @Test
  fun calendarViewMode_orderedAsScheduleMonthWeekDay_andDefaultsToSchedule() {
    assertEquals(
      listOf(
        CalendarViewMode.SCHEDULE,
        CalendarViewMode.MONTH,
        CalendarViewMode.WEEK,
        CalendarViewMode.DAY,
      ),
      CalendarViewMode.entries,
    )
    assertEquals(CalendarViewMode.SCHEDULE, CalendarViewMode.fromNameOrDefault(null))
    assertEquals(CalendarViewMode.SCHEDULE, CalendarViewMode.fromNameOrDefault(""))
    assertEquals(CalendarViewMode.SCHEDULE, CalendarViewMode.fromNameOrDefault("THREE_DAY"))
    assertEquals(CalendarViewMode.MONTH, CalendarViewMode.fromNameOrDefault("month"))
    assertEquals(CalendarViewMode.WEEK, CalendarViewMode.fromNameOrDefault("WEEK"))
    assertEquals(CalendarViewMode.DAY, CalendarViewMode.fromNameOrDefault("Day"))
    assertEquals(CalendarViewMode.SCHEDULE, InMemoryCalendarRepository().getViewMode())
  }

  @Test
  fun buildWeekDays_andConsecutiveDays_returnsExpectedRanges() {
    val mondayNov23 = CalendarDay(2026, 11, 23)
    val week = CalendarEngine.buildWeekDays(mondayNov23)
    assertEquals(7, week.size)
    assertEquals(CalendarDay(2026, 11, 22), week.first()) // Sunday
    assertEquals(CalendarDay(2026, 11, 28), week.last()) // Saturday

    val threeDays = CalendarEngine.buildConsecutiveDays(mondayNov23, 3)
    assertEquals(
      listOf(
        CalendarDay(2026, 11, 23),
        CalendarDay(2026, 11, 24),
        CalendarDay(2026, 11, 25),
      ),
      threeDays,
    )
  }

  @Test
  fun recurrenceExpansion_dailyWeeklyWeekdaysMonthlyYearly() {
    val startDay = CalendarDay(2026, 11, 23) // Monday
    val baseEvent = CalendarEvent(
      id = "rec_test",
      title = "Standup",
      startMillis = startDay.withTime(9, 30),
      endMillis = startDay.withTime(10, 0),
    )

    // 1. NONE -> only occurs on startDay
    assertNotNull(CalendarEngine.occurrenceForDay(baseEvent, startDay))
    assertNull(CalendarEngine.occurrenceForDay(baseEvent, startDay.plusDays(1)))

    // 2. DAILY -> occurs every day on or after startDay, never before
    val dailyEvent = baseEvent.copy(recurrence = CalendarRecurrence.DAILY)
    assertNull(CalendarEngine.occurrenceForDay(dailyEvent, startDay.plusDays(-1)))
    assertNotNull(CalendarEngine.occurrenceForDay(dailyEvent, startDay.plusDays(1)))
    assertNotNull(CalendarEngine.occurrenceForDay(dailyEvent, startDay.plusDays(40)))

    // 3. WEEKLY -> occurs every +7 days
    val weeklyEvent = baseEvent.copy(recurrence = CalendarRecurrence.WEEKLY)
    assertNull(CalendarEngine.occurrenceForDay(weeklyEvent, startDay.plusDays(3)))
    assertNotNull(CalendarEngine.occurrenceForDay(weeklyEvent, startDay.plusDays(7)))
    assertNotNull(CalendarEngine.occurrenceForDay(weeklyEvent, startDay.plusDays(14)))

    // 4. WEEKDAYS -> Mon..Fri only
    val weekdaysEvent = baseEvent.copy(recurrence = CalendarRecurrence.WEEKDAYS)
    assertNotNull(CalendarEngine.occurrenceForDay(weekdaysEvent, CalendarDay(2026, 11, 24))) // Tue
    assertNull(CalendarEngine.occurrenceForDay(weekdaysEvent, CalendarDay(2026, 11, 28))) // Sat
    assertNull(CalendarEngine.occurrenceForDay(weekdaysEvent, CalendarDay(2026, 11, 29))) // Sun
    assertNotNull(CalendarEngine.occurrenceForDay(weekdaysEvent, CalendarDay(2026, 11, 30))) // Mon

    // 5. MONTHLY -> same dayOfMonth in future months
    val monthlyEvent = baseEvent.copy(recurrence = CalendarRecurrence.MONTHLY)
    assertNotNull(CalendarEngine.occurrenceForDay(monthlyEvent, CalendarDay(2026, 12, 23)))
    assertNotNull(CalendarEngine.occurrenceForDay(monthlyEvent, CalendarDay(2027, 1, 23)))
    assertNull(CalendarEngine.occurrenceForDay(monthlyEvent, CalendarDay(2026, 12, 24)))

    // 6. YEARLY -> same month and dayOfMonth in future years
    val yearlyEvent = baseEvent.copy(recurrence = CalendarRecurrence.YEARLY)
    assertNotNull(CalendarEngine.occurrenceForDay(yearlyEvent, CalendarDay(2027, 11, 23)))
    assertNull(CalendarEngine.occurrenceForDay(yearlyEvent, CalendarDay(2027, 10, 23)))
  }

  @Test
  fun builtInHolidaysForYear_includesFixedAndCalculatedHolidays_andDoesNotDuplicateAcrossYears() {
    val holidays2025 = CalendarEngine.builtInHolidaysForYear(2025)
    val holidays2026 = CalendarEngine.builtInHolidaysForYear(2026)
    val holidays2027 = CalendarEngine.builtInHolidaysForYear(2027)
    assertTrue(holidays2026.isNotEmpty())
    assertTrue(holidays2026.any { it.title == "New Year's Day" && it.startDay == CalendarDay(2026, 1, 1) })
    // Thanksgiving 2026 is the 4th Thursday of November -> Nov 26, 2026
    assertTrue(holidays2026.any { it.title == "Thanksgiving Day" && it.startDay == CalendarDay(2026, 11, 26) })
    assertTrue(
      holidays2026.all {
        it.isBuiltInHoliday &&
          it.isAllDay &&
          it.category == CalendarCategory.HOLIDAYS &&
          it.recurrence == CalendarRecurrence.NONE
      }
    )

    // Combining adjacent years must never produce duplicate holiday occurrences on the same date
    val combinedHolidays = holidays2025 + holidays2026 + holidays2027
    val newYears2026Occurrences = CalendarEngine.occurrencesForDay(
      events = combinedHolidays,
      day = CalendarDay(2026, 1, 1),
    )
    assertEquals(1, newYears2026Occurrences.size)
    assertEquals("New Year's Day", newYears2026Occurrences.first().event.title)

    val christmas2026Occurrences = CalendarEngine.occurrencesForDay(
      events = combinedHolidays,
      day = CalendarDay(2026, 12, 25),
    )
    assertEquals(1, christmas2026Occurrences.size)
    assertEquals("Christmas Day", christmas2026Occurrences.first().event.title)
  }

  @Test
  fun projectTodoTasksToCalendarEvents_mapsScheduledTasksToCalendarEvents() {
    val day = CalendarDay(2026, 11, 23)
    val task = TodoItem(
      id = "task_123",
      title = "Submit Q4 tax estimate",
      notes = "Include form 1040-ES",
      scheduledAtMillis = day.withTime(14, 0),
      recurrenceRule = TodoRecurrenceRule.WEEKLY,
    )
    val projected = CalendarEngine.projectTodoTasksToCalendarEvents(listOf(task))
    assertEquals(1, projected.size)
    val calEvent = projected.first()
    assertEquals("Submit Q4 tax estimate", calEvent.title)
    assertEquals(CalendarCategory.TASKS, calEvent.category)
    assertEquals(CalendarRecurrence.WEEKLY, calEvent.recurrence)
    assertEquals("task_123", calEvent.linkedTodoTaskId)
  }

  @Test
  fun exportAndImportIcs_roundTripsEventsWithColorsCategoriesAndRecurrence() {
    val day = CalendarDay(2026, 11, 24)
    val original = listOf(
      CalendarEvent(
        id = "ics_1",
        title = "Architecture Review",
        description = "Discuss offline calendar sync",
        location = "Building 4",
        startMillis = day.withTime(13, 30),
        endMillis = day.withTime(15, 0),
        isAllDay = false,
        category = CalendarCategory.WORK,
        color = CalendarEventColor.BLUEBERRY,
        recurrence = CalendarRecurrence.BIWEEKLY,
      ),
      CalendarEvent(
        id = "ics_2",
        title = "Company Offsite",
        description = "All-day retreat",
        location = "Austin, TX",
        startMillis = day.toStartOfDayMillis(),
        endMillis = day.toEndOfDayMillis(),
        isAllDay = true,
        category = CalendarCategory.TRAVEL,
        color = CalendarEventColor.TANGERINE,
        recurrence = CalendarRecurrence.YEARLY,
      ),
    )

    val icsText = CalendarEngine.exportEventsToIcs(original)
    assertTrue(icsText.contains("BEGIN:VCALENDAR"))
    assertTrue(icsText.contains("SUMMARY:Architecture Review"))
    assertTrue(icsText.contains("RRULE:FREQ=WEEKLY;INTERVAL=2"))

    val imported = CalendarEngine.importEventsFromIcs(icsText)
    assertEquals(2, imported.size)
    assertEquals("Architecture Review", imported[0].title)
    assertEquals("Building 4", imported[0].location)
    assertEquals(CalendarCategory.WORK, imported[0].category)
    assertEquals(CalendarEventColor.BLUEBERRY, imported[0].color)
    assertEquals(CalendarRecurrence.BIWEEKLY, imported[0].recurrence)
    assertFalse(imported[0].isAllDay)

    assertEquals("Company Offsite", imported[1].title)
    assertTrue(imported[1].isAllDay)
    assertEquals(CalendarCategory.TRAVEL, imported[1].category)
    assertEquals(CalendarEventColor.TANGERINE, imported[1].color)
    assertEquals(CalendarRecurrence.YEARLY, imported[1].recurrence)
  }

  @Test
  fun dataBackupAndSearchIntegration_persistsAndFindsCalendarEvents() {
    val day = CalendarDay(2026, 11, 23)
    val event = CalendarEvent(
      id = "ev_dentist",
      title = "Dental Cleaning Appointment",
      description = "Dr. Smith Family Dentistry",
      location = "Main Street Clinic",
      startMillis = day.withTime(11, 0),
      endMillis = day.withTime(12, 0),
      category = CalendarCategory.HEALTH,
      color = CalendarEventColor.SAGE,
      isFavorite = true,
    )
    val sourceRepo = InMemoryCalendarRepository(
      initialEvents = listOf(event),
      initialViewMode = CalendarViewMode.WEEK,
    )

    // Universal Search Engine integration
    val searchResults = SearchEngine.search(
      query = "Dental Cleaning",
      dataSources = SearchDataSources(calendarRepository = sourceRepo),
    )
    assertTrue(
      searchResults.any {
        it.tool == Tool.CALENDAR &&
          it.resultType == SearchResultType.CALENDAR_EVENT &&
          it.title == "Dental Cleaning Appointment"
      }
    )

    // Full app JSON data backup & restore round-trip
    val json = exportDataToJson(ToolboxDataRepositories(calendarRepository = sourceRepo))
    val targetRepo = InMemoryCalendarRepository()
    assertTrue(importDataFromJson(json, ToolboxDataRepositories(calendarRepository = targetRepo)))
    assertEquals(1, targetRepo.loadEvents().size)
    assertEquals("Dental Cleaning Appointment", targetRepo.loadEvents().first().title)
    assertEquals(CalendarViewMode.WEEK, targetRepo.getViewMode())
  }

  @Test
  fun calendarWidgets_resolvesDailyStateAndStepsDays() {
    CalendarWidgets.clearInMemoryStateForTesting()
    val today = CalendarDay(2026, 11, 23)
    val event = CalendarEvent(
      id = "w_1",
      title = "Morning Coffee",
      startMillis = today.withTime(8, 30),
      endMillis = today.withTime(9, 0),
    )
    val repo = InMemoryCalendarRepository(initialEvents = listOf(event), initialShowHolidays = false)

    val state = CalendarWidgets.resolveWidgetState(
      context = null,
      calendarRepository = repo,
      focusedDayOverride = today,
      todayOverride = today,
    )
    assertTrue(state.isToday)
    assertEquals("23", state.dayNumberText)
    assertTrue(state.headerTitle.contains("Today"))
    assertTrue(state.agendaSummaryText.contains("Morning Coffee"))

    // Step forward and reset to Today
    CalendarWidgets.saveFocusedDay(null, today)
    assertEquals(CalendarDay(2026, 11, 24), CalendarWidgets.stepFocusedDay(null, 1))
    assertEquals(CalendarDay(2026, 11, 23), CalendarWidgets.stepFocusedDay(null, -1))
    CalendarWidgets.clearInMemoryStateForTesting()
  }

  @Test
  fun calendarMonthWidgets_resolvesMonthlyGridAndStepsMonths() {
    CalendarWidgets.clearInMemoryStateForTesting()
    val today = CalendarDay(2026, 11, 23)
    val event1 = CalendarEvent(
      id = "m_1",
      title = "Design Review",
      startMillis = today.withTime(10, 0),
      endMillis = today.withTime(11, 0),
    )
    val event2 = CalendarEvent(
      id = "m_2",
      title = "Team Dinner",
      startMillis = CalendarDay(2026, 11, 27).withTime(18, 0),
      endMillis = CalendarDay(2026, 11, 27).withTime(20, 0),
    )
    val repo = InMemoryCalendarRepository(
      initialEvents = listOf(event1, event2),
      initialShowHolidays = false,
    )

    val monthState = CalendarWidgets.resolveMonthWidgetState(
      context = null,
      calendarRepository = repo,
      focusedMonthDayOverride = today,
      todayOverride = today,
    )
    assertTrue(monthState.isCurrentMonth)
    assertEquals(2026, monthState.year)
    assertEquals(11, monthState.month)
    assertEquals(42, monthState.gridDays.size)
    assertEquals("November 2026", monthState.headerTitle)
    assertEquals("23", monthState.badgeDayNumberText)
    assertEquals(2, monthState.totalMonthOccurrences)
    assertEquals(1, monthState.occurrencesByDay[today]?.size)
    assertEquals(1, monthState.occurrencesByDay[CalendarDay(2026, 11, 27)]?.size)
    assertTrue(monthState.headerSubtitle.contains("Today: 1 event"))
    assertTrue(monthState.headerSubtitle.contains("2 events this month"))

    // Step months forward/backward and reset to Today
    CalendarWidgets.saveFocusedMonthDay(null, today)
    val nextMonth = CalendarWidgets.stepFocusedMonth(null, 1)
    assertEquals(2026, nextMonth.year)
    assertEquals(12, nextMonth.month)

    val prevMonth = CalendarWidgets.stepFocusedMonth(null, -2)
    assertEquals(2026, prevMonth.year)
    assertEquals(10, prevMonth.month)

    val resetMonth = CalendarWidgets.resetFocusedMonthToToday(null, today)
    assertEquals(today, resetMonth)
    CalendarWidgets.clearInMemoryStateForTesting()
  }
}
