package com.example.toolbox.ui.tools

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ToolsUnitTest {

  @Test
  fun unitCatalog_convertsLengthCorrectly() {
    val meter = UnitCatalog.lengthUnits.first { it.id == "m" }
    val kilometer = UnitCatalog.lengthUnits.first { it.id == "km" }
    val inch = UnitCatalog.lengthUnits.first { it.id == "in" }

    assertEquals(1000.0, UnitCatalog.convert(1.0, kilometer, meter), 1e-6)
    assertEquals(0.0254, UnitCatalog.convert(1.0, inch, meter), 1e-6)
  }

  @Test
  fun unitCatalog_convertsTemperatureCorrectly() {
    val celsius = UnitCatalog.temperatureUnits.first { it.id == "c" }
    val fahrenheit = UnitCatalog.temperatureUnits.first { it.id == "f" }
    val kelvin = UnitCatalog.temperatureUnits.first { it.id == "k" }

    assertEquals(32.0, UnitCatalog.convert(0.0, celsius, fahrenheit), 1e-6)
    assertEquals(212.0, UnitCatalog.convert(100.0, celsius, fahrenheit), 1e-6)
    assertEquals(-40.0, UnitCatalog.convert(-40.0, celsius, fahrenheit), 1e-6)
    assertEquals(273.15, UnitCatalog.convert(0.0, celsius, kelvin), 1e-6)
  }

  @Test
  fun guitarTuningPresets_haveSixStringsFromLowToHigh() {
    GuitarTuningPreset.entries.forEach { preset ->
      assertEquals(6, preset.strings.size)
      assertEquals(6, preset.strings.first().stringNumber)
      assertEquals(1, preset.strings.last().stringNumber)
      assertTrue(preset.strings.first().frequencyHz < preset.strings.last().frequencyHz)
      preset.strings.forEach { spec ->
        assertEquals(spec.noteName, spec.fullLabel)
      }
    }
  }

  @Test
  fun guitarPitchDetector_matchesClosestStringAndDetectsFundamental() {
    val standardStrings = GuitarTuningPreset.STANDARD.strings
    // Slightly sharp A string (~112 Hz vs 110 Hz target)
    val matchA = GuitarPitchDetector.matchClosestString(112.0, standardStrings)
    assertEquals(1, matchA.stringIndex)
    assertEquals("A", matchA.stringSpec.noteName)
    assertTrue(matchA.centsOffset > 0f)

    // Synthesize a 196 Hz (G string) sine wave and verify autocorrelation pitch detection
    val sampleRate = 22050
    val targetFreq = 196.0
    val samples = ShortArray(2048) { i ->
      (kotlin.math.sin(2.0 * Math.PI * targetFreq * i / sampleRate) * 16000).toInt().toShort()
    }
    val detected = GuitarPitchDetector.detectFundamentalFrequency(samples, samples.size, sampleRate)
    assertTrue(detected != null && kotlin.math.abs(detected - targetFreq) < 1.5)
  }

  @Test
  fun colorMath_convertsHexAndRgbAndHsvCorrectly() {
    val parsed = ColorMath.parseHex("#23759B")
    assertEquals(Triple(35, 117, 155), parsed)
    assertEquals("#23759B", ColorMath.toHex(35, 117, 155))

    val hsv = ColorMath.rgbToHsv(255, 0, 0)
    assertEquals(0f, hsv.hue, 0.5f)
    assertEquals(1f, hsv.saturation, 0.01f)
    assertEquals(1f, hsv.value, 0.01f)

    val (r, g, b) = ColorMath.hsvToRgb(120f, 1f, 1f)
    assertEquals(Triple(0, 255, 0), Triple(r, g, b))
  }

  @Test
  fun colorMath_calculatesContrastAndHarmoniesCorrectly() {
    val whiteLum = ColorMath.relativeLuminance(255, 255, 255)
    val blackLum = ColorMath.relativeLuminance(0, 0, 0)
    val contrast = ColorMath.contrastRatio(whiteLum, blackLum)
    assertEquals(21.0, contrast, 0.05)
    assertEquals("AAA", ColorMath.wcagRating(contrast))

    val base = HsvColor(30f, 0.8f, 0.9f)
    val complementary = ColorMath.generateHarmony(base, HarmonyMode.COMPLEMENTARY)
    assertEquals(2, complementary.size)
    assertEquals(210f, complementary[1].hue, 0.01f)
  }

  @Test
  fun rulerMath_convertsMmInchesAndFormatsFractionsCorrectly() {
    assertEquals(1.0, RulerMath.mmToInches(25.4), 1e-6)
    assertEquals(25.4, RulerMath.inchesToMm(1.0), 1e-6)
    assertEquals(5.4, RulerMath.mmToCm(54.0), 1e-6)
    assertEquals(42.0f, RulerMath.caliperSpanMm(12.0f, 54.0f), 1e-4f)

    assertEquals("0\"", RulerMath.formatImperialFraction(0.0))
    assertEquals("2\"", RulerMath.formatImperialFraction(2.0))
    assertEquals("2 3/8\"", RulerMath.formatImperialFraction(2.375))
    assertEquals("5/16\"", RulerMath.formatImperialFraction(5.0 / 16.0))
    assertEquals("Credit Card Width", RulerMath.closestReferenceObject(54.0f).name)
  }

  @Test
  fun rulerMath_resolvesHardwareDpiAndCalculatesScreenMeasurements() {
    assertEquals(422.5f, RulerMath.resolveHardwareDpi(422.5f, 420), 1e-4f)
    assertEquals(420f, RulerMath.resolveHardwareDpi(10f, 420), 1e-4f)

    val pxPerMm = RulerMath.dpiToPxPerMm(254f, calibrationScale = 1.0f)
    assertEquals(10.0f, pxPerMm, 1e-4f)
    assertEquals(50.0f, RulerMath.mmToPx(5.0f, pxPerMm), 1e-4f)
    assertEquals(5.0f, RulerMath.pxToMm(50.0f, pxPerMm), 1e-4f)

    val measurements = DeviceScreenMeasurements(
      xdpi = 254f,
      ydpi = 254f,
      densityDpi = 240,
      widthPx = 1000,
      heightPx = 2000,
      calibrationScale = 1.0f,
    )
    assertEquals(100.0f, measurements.screenWidthMm, 1e-2f)
    assertEquals(200.0f, measurements.screenHeightMm, 1e-2f)
  }

  @Test
  fun rulerMath_resolveCaliperTarget_selectsClosestBarAndDisambiguatesOverlappingBars() {
    // 1. Clearly separated bars (A = 20mm, B = 70mm)
    // Touch at 22mm should select Bar A (0)
    val targetNearA = RulerMath.resolveCaliperTarget(
      touchCoordMm = 22f,
      startMm = 20f,
      endMm = 70f,
    )
    assertEquals(0, targetNearA)

    // Touch at 68mm should select Bar B (1)
    val targetNearB = RulerMath.resolveCaliperTarget(
      touchCoordMm = 68f,
      startMm = 20f,
      endMm = 70f,
    )
    assertEquals(1, targetNearB)

    // 2. Overlapping bars at the same position (A = 40mm, B = 40mm)
    // Touching on or before the minimum pulls the start jaw (Bar A = 0)
    val targetBeforeOverlap = RulerMath.resolveCaliperTarget(
      touchCoordMm = 35f,
      startMm = 40f,
      endMm = 40f,
    )
    assertEquals(0, targetBeforeOverlap)

    val targetAtOverlap = RulerMath.resolveCaliperTarget(
      touchCoordMm = 40f,
      startMm = 40f,
      endMm = 40f,
    )
    assertEquals(0, targetAtOverlap)

    // Touching to the right / after overlapping bars pulls Bar B (1)
    val targetAfterOverlap = RulerMath.resolveCaliperTarget(
      touchCoordMm = 45f,
      startMm = 40f,
      endMm = 40f,
    )
    assertEquals(1, targetAfterOverlap)

    // 3. Inverted order bars (A = 60mm, B = 20mm)
    // Touch at 22mm is closer to B (1)
    val targetNearBInverted = RulerMath.resolveCaliperTarget(
      touchCoordMm = 22f,
      startMm = 60f,
      endMm = 20f,
    )
    assertEquals(1, targetNearBInverted)

    // Touch at 58mm is closer to A (0)
    val targetNearAInverted = RulerMath.resolveCaliperTarget(
      touchCoordMm = 58f,
      startMm = 60f,
      endMm = 20f,
    )
    assertEquals(0, targetNearAInverted)
  }

  @Test
  fun beatSubdivision_labelsDoNotContainMultiplierText() {
    val expectedLabels = mapOf(
      BeatSubdivision.QUARTER to ("Quarter" to 1),
      BeatSubdivision.EIGHTH to ("Eighth" to 2),
      BeatSubdivision.TRIPLET to ("Triplet" to 3),
      BeatSubdivision.SIXTEENTH to ("Sixteenth" to 4),
    )

    for ((subdivision, expected) in expectedLabels) {
      val (label, ticks) = expected
      assertEquals(label, subdivision.label)
      assertEquals(ticks, subdivision.ticksPerBeat)
      assertTrue(!subdivision.label.contains(Regex("""\(\d+x\)""")))
    }
  }

  @Test
  fun timeSignature_defaultSignaturesDoNotExceedFiveEight() {
    val defaultLabels = TimeSignature.defaultSignatures.map { it.label }
    val expectedDefaults = listOf("4/4", "3/4", "2/4", "5/4", "6/8", "5/8")
    assertEquals(expectedDefaults, defaultLabels)

    // Ensure none of the extended meters above 5/8 are in defaults
    val aboveFiveEight = listOf("7/8", "7/4", "9/8", "10/8", "11/8", "12/8", "14/8", "23/16")
    for (meter in aboveFiveEight) {
      assertTrue("Default signatures should not contain $meter", !defaultLabels.contains(meter))
    }
  }

  @Test
  fun timeSignature_customSignaturesCanBeCreatedAndParsed() {
    // Custom Ganon themes (23/16, 7/4, 14/8) and Hyrule Castle (6/4, 10/8)
    val customGanon = TimeSignature.fromBeatsAndNote(23, 16, isCustom = true)
    assertEquals("23/16", customGanon.label)
    assertEquals(23, customGanon.beatsPerMeasure)
    assertEquals(16, customGanon.noteValue)
    assertTrue(customGanon.isCustom)

    val parsedGanon = TimeSignature.parse("23/16", isCustom = true)
    assertEquals(customGanon, parsedGanon)

    val customHyruleCastle = TimeSignature.fromBeatsAndNote(6, 4, isCustom = true)
    assertEquals("6/4", customHyruleCastle.label)
    assertEquals(6, customHyruleCastle.beatsPerMeasure)
    assertEquals(4, customHyruleCastle.noteValue)
    assertTrue(customHyruleCastle.isCustom)

    // Saver roundtrip
    val restored = TimeSignatureSaver.restore("23/16:true")
    assertEquals(customGanon, restored)
  }
}
