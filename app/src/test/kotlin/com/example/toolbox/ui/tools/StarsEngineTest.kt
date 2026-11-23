package com.example.toolbox.ui.tools

import com.example.toolbox.ui.main.THEME_COLOR_STARS_SIGN
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.theme.isStarsSignThemeColorSetting
import com.example.toolbox.ui.theme.parseHexColor
import com.example.toolbox.ui.theme.resolveStarsSignThemeColor
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StarsEngineTest {

  @Test
  fun zodiacSign_fromMonthDayAndLongitude_mapsAll12SignsAccurately() {
    assertEquals(ZodiacSign.ARIES, ZodiacSign.fromMonthDay(3, 25))
    assertEquals(ZodiacSign.TAURUS, ZodiacSign.fromMonthDay(5, 14))
    assertEquals(ZodiacSign.GEMINI, ZodiacSign.fromMonthDay(6, 1))
    assertEquals(ZodiacSign.CANCER, ZodiacSign.fromMonthDay(7, 4))
    assertEquals(ZodiacSign.LEO, ZodiacSign.fromMonthDay(8, 10))
    assertEquals(ZodiacSign.VIRGO, ZodiacSign.fromMonthDay(9, 5))
    assertEquals(ZodiacSign.LIBRA, ZodiacSign.fromMonthDay(10, 10))
    assertEquals(ZodiacSign.SCORPIO, ZodiacSign.fromMonthDay(10, 28))
    assertEquals(ZodiacSign.SAGITTARIUS, ZodiacSign.fromMonthDay(12, 5))
    assertEquals(ZodiacSign.CAPRICORN, ZodiacSign.fromMonthDay(1, 5))
    assertEquals(ZodiacSign.AQUARIUS, ZodiacSign.fromMonthDay(2, 5))
    assertEquals(ZodiacSign.PISCES, ZodiacSign.fromMonthDay(3, 5))

    assertEquals(ZodiacSign.ARIES, ZodiacSign.fromLongitude(15.0))
    assertEquals(ZodiacSign.TAURUS, ZodiacSign.fromLongitude(45.0))
    assertEquals(ZodiacSign.SCORPIO, ZodiacSign.fromLongitude(225.0))
    assertEquals(ZodiacSign.PISCES, ZodiacSign.fromLongitude(355.0))
  }

  @Test
  fun calculateNatalChart_computesBigThreeAll10PlanetsHousesAndAspects() {
    val profile = BirthProfile(
      id = "test_her",
      name = "Her Chart",
      relationshipTag = "Me",
      birthYear = 1999,
      birthMonth = 5,
      birthDay = 14,
      birthHour = 16,
      birthMinute = 20,
      hasExactBirthTime = true,
      cityName = "Chicago, IL, USA",
      latitude = 41.8781,
      longitude = -87.6298,
      timeZoneId = "America/Chicago",
    )

    val chart = StarsEngine.calculateNatalChart(profile)
    assertEquals(ZodiacSign.TAURUS, chart.sunSign)
    assertEquals(10, chart.placements.size)
    assertTrue(chart.placements.all { it.house in 1..12 })
    assertTrue(chart.placements.all { it.degreeInSign in 0..29 })
    assertTrue(chart.bigThreeSummary.contains("Taurus Sun"))
    assertNotNull(chart.dominantElement)
    assertNotNull(chart.dominantModality)
  }

  @Test
  fun defaultProfiles_areEmptyByDefault_andFallBackToCurrentBirthSign() {
    assertTrue(DEFAULT_BIRTH_PROFILES.isEmpty())
    val repo = InMemoryStarsRepository()
    assertTrue(repo.loadProfiles().isEmpty())
    assertEquals("", repo.getActiveProfileId())

    val novCal = Calendar.getInstance().apply {
      set(2026, Calendar.NOVEMBER, 23, 12, 0, 0)
    }
    assertEquals(ZodiacSign.SAGITTARIUS, StarsEngine.getCurrentZodiacSign(novCal))

    val mayCal = Calendar.getInstance().apply {
      set(2026, Calendar.MAY, 14, 12, 0, 0)
    }
    assertEquals(ZodiacSign.TAURUS, StarsEngine.getCurrentZodiacSign(mayCal))
  }

  @Test
  fun generateDailyHoroscope_producesDeterministicTransitBackedReadingAndMoonPhase() {
    val profile = BirthProfile(
      id = "test_her",
      name = "Her Chart",
      relationshipTag = "Me",
      birthYear = 1999,
      birthMonth = 5,
      birthDay = 14,
      birthHour = 16,
      birthMinute = 20,
      hasExactBirthTime = true,
      cityName = "Chicago, IL, USA",
      latitude = 41.8781,
      longitude = -87.6298,
      timeZoneId = "America/Chicago",
    )
    val chart = StarsEngine.calculateNatalChart(profile)
    val reading1 = StarsEngine.generateDailyHoroscope(
      year = 2026,
      month = 11,
      day = 22,
      targetSign = ZodiacSign.TAURUS,
      natalChart = chart,
    )
    val reading2 = StarsEngine.generateDailyHoroscope(
      year = 2026,
      month = 11,
      day = 22,
      targetSign = ZodiacSign.TAURUS,
      natalChart = chart,
    )

    assertEquals(reading1.headlineMantra, reading2.headlineMantra)
    assertEquals(reading1.luckyNumber, reading2.luckyNumber)
    assertEquals(reading1.moonSnapshot.phaseType, reading2.moonSnapshot.phaseType)
    assertTrue(reading1.moonSnapshot.illuminationPercent in 0..100)
    assertTrue(reading1.overviewReading.isNotBlank())
    assertTrue(reading1.loveReading.isNotBlank())
    assertTrue(reading1.careerReading.isNotBlank())
    assertEquals(3, reading1.dosList.size)
    assertEquals(3, reading1.dontsList.size)
  }

  @Test
  fun calculateSynastry_computesDimensionsCrossAspectsAndSummary() {
    val p1 = BirthProfile(
      id = "test_her",
      name = "My Chart",
      relationshipTag = "Me",
      birthYear = 1999,
      birthMonth = 5,
      birthDay = 14,
      birthHour = 16,
      birthMinute = 20,
      hasExactBirthTime = true,
      cityName = "Chicago, IL, USA",
      latitude = 41.8781,
      longitude = -87.6298,
      timeZoneId = "America/Chicago",
    )
    val p2 = BirthProfile(
      id = "test_partner",
      name = "Partner",
      relationshipTag = "Partner",
      birthYear = 1998,
      birthMonth = 10,
      birthDay = 28,
      birthHour = 9,
      birthMinute = 45,
      hasExactBirthTime = true,
      cityName = "Chicago, IL, USA",
      latitude = 41.8781,
      longitude = -87.6298,
      timeZoneId = "America/Chicago",
    )
    val c1 = StarsEngine.calculateNatalChart(p1)
    val c2 = StarsEngine.calculateNatalChart(p2)

    val report = StarsEngine.calculateSynastry(c1, c2)
    assertTrue(report.overallScore in 65..99)
    assertTrue(report.romanceScore in 65..99)
    assertTrue(report.emotionalScore in 65..99)
    assertTrue(report.communicationScore in 65..99)
    assertTrue(report.valuesScore in 65..99)
    assertTrue(report.summary.contains(p1.name))
    assertTrue(report.summary.contains(p2.name))
    assertTrue(report.strengths.isNotEmpty())
    assertTrue(report.growthTips.isNotEmpty())
  }

  @Test
  fun guideCatalog_andUniversalSearch_surfaceSignsPlanetsHousesAndAspects() {
    assertTrue(StarsEngine.GUIDE_CATALOG.size >= 51) // 12 signs + 10 planets + 12 houses + 5 aspects + 8 phases + 4 elements

    val searchResults = SearchEngine.search("Scorpio")
    assertTrue(searchResults.any { it.tool == Tool.STARS && it.resultType == SearchResultType.ASTROLOGY })

    val retroResults = SearchEngine.search("Mercury Retrograde")
    assertTrue(retroResults.any { it.tool == Tool.STARS })
  }

  @Test
  fun starsWidget_defaultsToCurrentBirthSignWhenNoProfiles_andCyclesSigns() {
    StarsWidgets.clearInMemoryStateForTesting()
    val repo = InMemoryStarsRepository()
    val cal = Calendar.getInstance().apply {
      set(2026, Calendar.NOVEMBER, 23, 12, 0, 0)
    }

    val initialState = StarsWidgets.resolveWidgetState(context = null, repository = repo, calendar = cal)
    assertEquals(ZodiacSign.SAGITTARIUS, initialState.activeSign)
    assertTrue(initialState.mantraText.isNotBlank())

    val nextSign = ZodiacSign.entries[(initialState.activeSign.ordinal + 1) % ZodiacSign.entries.size]
    StarsWidgets.saveOverrideSign(null, nextSign)

    val cycledState = StarsWidgets.resolveWidgetState(context = null, repository = repo, calendar = cal)
    assertEquals(ZodiacSign.CAPRICORN, cycledState.activeSign)
    StarsWidgets.clearInMemoryStateForTesting()
  }

  @Test
  fun starsSignThemeColor_followsCurrentCalendarStarSignRatherThanFocusedSign() {
    assertTrue(isStarsSignThemeColorSetting(THEME_COLOR_STARS_SIGN))
    assertFalse(isStarsSignThemeColorSetting("SYSTEM"))

    val scorpioCal = Calendar.getInstance().apply {
      set(2026, Calendar.NOVEMBER, 5, 12, 0, 0)
    }
    val resolvedScorpio = StarsEngine.resolveCurrentSign(calendar = scorpioCal)
    assertEquals(ZodiacSign.SCORPIO, resolvedScorpio)
    assertEquals(
      parseHexColor(ZodiacSign.SCORPIO.powerColorHex),
      resolveStarsSignThemeColor(),
    )

    // Focusing/overriding a different sign in the Stars tool or widget must not alter the current star sign theme color
    StarsWidgets.saveOverrideSign(null, ZodiacSign.ARIES)
    assertEquals(
      parseHexColor(ZodiacSign.SCORPIO.powerColorHex),
      resolveStarsSignThemeColor(),
    )
    StarsWidgets.clearInMemoryStateForTesting()

    val sagCal = Calendar.getInstance().apply {
      set(2026, Calendar.NOVEMBER, 23, 12, 0, 0)
    }
    val resolvedSag = StarsEngine.resolveCurrentSign(calendar = sagCal)
    assertEquals(ZodiacSign.SAGITTARIUS, resolvedSag)
    assertEquals(
      parseHexColor(ZodiacSign.SAGITTARIUS.powerColorHex),
      resolveStarsSignThemeColor(),
    )
    StarsEngine.resolveCurrentSign()
  }

  @Test
  fun skyAr_projectsConstellationsAndSnapsOrionsBeltFromCameraPixels() {
    assertEquals(16, SKY_CONSTELLATIONS.size)
    val orion = SKY_CONSTELLATIONS.first { it.id == "orion" }
    assertTrue(orion.stars.any { it.id == "alnitak" })
    assertTrue(orion.stars.any { it.id == "alnilam" })
    assertTrue(orion.stars.any { it.id == "mintaka" })

    val jd = StarsEngine.toJulianDay(2026, 11, 22, 23, 0, "America/Chicago")
    val horiz = StarsEngine.equatorialToHorizontal(
      raHours = orion.centerRaHours,
      decDegrees = orion.centerDecDegrees,
      jd = jd,
      latitude = 41.8781,
      longitude = -87.6298,
    )
    assertTrue(horiz.altitudeDegrees in -90.0..90.0)
    assertTrue(horiz.azimuthDegrees in 0.0..360.0)

    // Project Orion directly in front of the camera
    val proj = StarsEngine.projectSkyToCameraViewport(
      targetAltitudeDeg = horiz.altitudeDegrees,
      targetAzimuthDeg = horiz.azimuthDegrees,
      cameraAltitudeDeg = horiz.altitudeDegrees,
      cameraAzimuthDeg = horiz.azimuthDegrees,
    )
    assertTrue(proj.isInFrontOfCamera)
    assertEquals(0.5f, proj.normX, 0.02f)
    assertEquals(0.5f, proj.normY, 0.02f)

    // Synthesize a dark 200x200 night-sky camera frame with 3 collinear bright stars (Orion's Belt)
    val w = 200
    val h = 200
    val pixels = IntArray(w * h) { 0xFF060812.toInt() }
    fun plotStar(px: Int, py: Int) {
      for (dy in -1..1) {
        for (dx in -1..1) {
          val x = (px + dx).coerceIn(0, w - 1)
          val y = (py + dy).coerceIn(0, h - 1)
          pixels[y * w + x] = 0xFFFFFFFF.toInt()
        }
      }
    }
    // Three equally spaced diagonal belt stars: Alnitak (70, 110), Alnilam (100, 100), Mintaka (130, 90)
    plotStar(70, 110)
    plotStar(100, 100)
    plotStar(130, 90)

    val detected = StarsEngine.detectBrightStarPointsFromArgb(pixels, w, h)
    assertEquals(3, detected.size)

    val match = StarsEngine.matchConstellationToDetectedStars(
      detectedPoints = detected,
      aimedConstellation = orion,
    )
    assertNotNull(match)
    assertEquals("orion", match!!.constellationId)
    assertTrue(match.asterismLabel.contains("Orion's Belt"))
    assertTrue(match.snappedStarPositions.containsKey("alnitak"))
    assertTrue(match.snappedStarPositions.containsKey("alnilam"))
    assertTrue(match.snappedStarPositions.containsKey("mintaka"))
    assertTrue(match.snappedStarPositions.containsKey("betelgeuse"))
    assertTrue(match.snappedStarPositions.containsKey("rigel"))
  }
}
