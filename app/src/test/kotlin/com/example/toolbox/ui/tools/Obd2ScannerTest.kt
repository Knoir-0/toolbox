package com.example.toolbox.ui.tools

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class Obd2ScannerTest {

  @Test
  fun parseLiveTelemetryPids_decodesStandardMode01Frames() {
    // 010C Engine RPM: 41 0C 1A F8 -> ((26 * 256) + 248) / 4 = 1726.0 RPM
    assertEquals(1726.0f, Obd2Parser.parseRpm("41 0C 1A F8 >")!!, 0.01f)
    assertEquals(850.0f, Obd2Parser.parseRpm("410C0D48")!!, 0.01f)
    assertNull(Obd2Parser.parseRpm("NO DATA >"))

    // 010D Vehicle Speed: 41 0D 55 -> 85 km/h
    assertEquals(85.0f, Obd2Parser.parseSpeedKmh("41 0D 55")!!, 0.01f)
    assertEquals("53" to "mph", Obd2Parser.formatSpeed(85.0f, useImperial = true))
    assertEquals("85" to "km/h", Obd2Parser.formatSpeed(85.0f, useImperial = false))

    // 0105 Coolant Temp: 41 05 82 -> 130 - 40 = 90°C (194°F)
    assertEquals(90.0f, Obd2Parser.parseCoolantTempC("41 05 82")!!, 0.01f)
    assertEquals("194" to "°F", Obd2Parser.formatTemperature(90.0f, useImperial = true))
    assertEquals("90" to "°C", Obd2Parser.formatTemperature(90.0f, useImperial = false))

    // 0104 Engine Load & 0111 Throttle Position: 0x80 (128) -> ~50.2%
    assertEquals(50.19f, Obd2Parser.parseEngineLoadPct("41 04 80")!!, 0.1f)
    assertEquals(100.0f, Obd2Parser.parseThrottlePct("41 11 FF")!!, 0.01f)

    // 0106 / 0107 Fuel Trim: 0x80 = 0.0%, 0x88 = +6.25%, 0x78 = -6.25%
    assertEquals(0.0f, Obd2Parser.parseFuelTrimPct("41 06 80", "06")!!, 0.01f)
    assertEquals(6.25f, Obd2Parser.parseFuelTrimPct("41 06 88", "06")!!, 0.01f)
    assertEquals(-6.25f, Obd2Parser.parseFuelTrimPct("41 07 78", "07")!!, 0.01f)

    // 0110 MAF Air Flow: 41 10 04 B0 -> 1200 / 100 = 12.0 g/s
    assertEquals(12.0f, Obd2Parser.parseMafGramsPerSec("41 10 04 B0")!!, 0.01f)

    // ATRV Battery Voltage
    assertEquals(14.2f, Obd2Parser.parseBatteryVoltage("14.2V\r>")!!, 0.01f)
    assertEquals(12.6f, Obd2Parser.parseBatteryVoltage("12.6 V")!!, 0.01f)
  }

  @Test
  fun decodeTwoByteDtc_andParseDtcResponse_handlesAllCategoriesAndCanFrames() {
    // P0420 -> 04 20
    assertEquals("P0420", Obd2Parser.decodeTwoByteDtc(0x04, 0x20))
    // C0035 -> 40 35
    assertEquals("C0035", Obd2Parser.decodeTwoByteDtc(0x40, 0x35))
    // B0001 -> 80 01
    assertEquals("B0001", Obd2Parser.decodeTwoByteDtc(0x80, 0x01))
    // U0100 -> C1 00
    assertEquals("U0100", Obd2Parser.decodeTwoByteDtc(0xC1, 0x00))
    // 00 00 padding -> null
    assertNull(Obd2Parser.decodeTwoByteDtc(0x00, 0x00))

    // CAN Mode 03 response with count byte (03 DTCs: P0420, P0171, U0100)
    val rawMode03 = "43 03 04 20 01 71 C1 00"
    val dtcs = Obd2Parser.parseDtcResponse(rawMode03, "43", DtcStatus.STORED)
    assertEquals(3, dtcs.size)
    assertEquals("P0420", dtcs[0].code)
    assertEquals("P0171", dtcs[1].code)
    assertEquals("U0100", dtcs[2].code)
    assertEquals(DtcStatus.STORED, dtcs[0].status)
  }

  @Test
  fun parseMonitorStatus_decodesMilDtcCountAndReadinessMonitors() {
    // 41 01 82 07 E5 04:
    // Byte A = 0x82 -> MIL ON (0x80), 2 stored DTCs
    // Byte B = 0x07 -> Spark ignition (bit 3 = 0), Misfire/Fuel/Comp supported & complete
    // Byte C = 0xE5 -> Catalyst (0x01), EVAP (0x04), O2 (0x20), O2 Heater (0x40), EGR (0x80) supported
    // Byte D = 0x04 -> EVAP (0x04) incomplete, others complete
    val summary = Obd2Parser.parseMonitorStatus("41 01 82 07 E5 04")
    assertNotNull(summary)
    assertTrue(summary!!.milOn)
    assertEquals(2, summary.storedDtcCount)
    assertFalse(summary.isCompressionIgnition)
    assertEquals(8, summary.supportedMonitors.size)
    assertEquals(7, summary.readyCount)
    assertEquals(1, summary.incompleteCount)

    val evap = summary.monitors.first { it.name.contains("EVAP") }
    assertEquals(MonitorStatus.INCOMPLETE, evap.status)
  }

  @Test
  fun parseVin_andDecodeMetadata_extractsSeventeenCharIsoVin() {
    // Multi-frame Mode 09 PID 02 response for "1HGCM82633A004352"
    val rawVin = """
      014
      0: 49 02 01 31 48 47
      1: 43 4D 38 32 36 33 33
      2: 41 30 30 34 33 35 32
    """.trimIndent()
    val vin = Obd2Parser.parseVin(rawVin)
    assertEquals("1HGCM82633A004352", vin)

    val (region, year) = Obd2Parser.decodeVinMetadata(vin!!)
    assertEquals("United States", region)
    assertEquals("2003", year)
  }

  @Test
  fun dtcCatalog_lookupAndSearch_handlesKnownAndDynamicManufacturerCodes() {
    val p0420 = Obd2DtcCatalog.lookup("P0420")
    assertTrue(p0420.description.contains("Catalyst", ignoreCase = true))

    // Unlisted manufacturer-specific code P1345 -> dynamically classified under Ignition & Misfire
    val p1345 = Obd2DtcCatalog.lookup("P1345")
    assertEquals("P1345", p1345.code)
    assertEquals("Ignition & Misfire", p1345.system)
    assertTrue(p1345.description.contains("Manufacturer-Specific"))

    val searchResults = Obd2DtcCatalog.search("P1345")
    assertTrue(searchResults.isNotEmpty())
    assertEquals("P1345", searchResults.first().code)

    // Category filter in top-toolbar search
    val emissionsOnly = Obd2DtcCatalog.search("", DtcSystemFilter.EMISSIONS)
    assertTrue(emissionsOnly.isNotEmpty())
    assertTrue(emissionsOnly.all { it.system.contains("Emission", ignoreCase = true) })
  }

  @Test
  fun obd2BleUuids_identifiesKnownAdapterNamesAndServices() {
    assertTrue(Obd2BleUuids.isLikelyObdAdapter("IOS-Vlink", emptyList()))
    assertTrue(Obd2BleUuids.isLikelyObdAdapter("VEEPEAK BLE+", emptyList()))
    assertTrue(Obd2BleUuids.isLikelyObdAdapter("Unknown Device", listOf(Obd2BleUuids.SERVICE_FFF0)))
    assertFalse(Obd2BleUuids.isLikelyObdAdapter("Smart Watch", emptyList()))
  }
}
