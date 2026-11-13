package com.example.toolbox.ui.tools

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import java.util.Locale
import java.util.UUID
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Standard BLE GATT Service and Characteristic UUIDs used by common ELM327 BLE OBD-II adapters
 * (Vgate iCar Pro, Veepeak BLE+, Carista, Viecar, HM-10, OBDLink CX / Nordic UART).
 */
object Obd2BleUuids {
  val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

  // Vgate iCar Pro / Veepeak BLE+ / IOS-Vlink (Service FFF0, Notify FFF1, Write FFF2)
  val SERVICE_FFF0: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
  val CHAR_FFF1_NOTIFY: UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
  val CHAR_FFF2_WRITE: UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")

  // HM-10 / Viecar / Generic BLE UART (Service FFE0, Notify+Write FFE1)
  val SERVICE_FFE0: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
  val CHAR_FFE1_RDWR: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

  // custom 18F0 service used by some ELM327 BLE clones (Notify 2AF0, Write 2AF1)
  val SERVICE_18F0: UUID = UUID.fromString("000018f0-0000-1000-8000-00805f9b34fb")
  val CHAR_2AF0_NOTIFY: UUID = UUID.fromString("00002af0-0000-1000-8000-00805f9b34fb")
  val CHAR_2AF1_WRITE: UUID = UUID.fromString("00002af1-0000-1000-8000-00805f9b34fb")

  // Nordic UART Service (NUS) used by OBDLink CX and modern BLE bridges
  val SERVICE_NUS: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
  val CHAR_NUS_WRITE: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
  val CHAR_NUS_NOTIFY: UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")

  val KNOWN_SERVICE_UUIDS: Set<UUID> = setOf(
    SERVICE_FFF0,
    SERVICE_FFE0,
    SERVICE_18F0,
    SERVICE_NUS,
  )

  private val KNOWN_ADAPTER_NAME_TOKENS = listOf(
    "obd",
    "elm",
    "vgate",
    "icar",
    "vlink",
    "veepeak",
    "carista",
    "viecar",
    "konnwei",
    "bafx",
    "lelink",
    "nexas",
    "ancd",
    "bluedriver",
  )

  fun isLikelyObdAdapter(deviceName: String?, advertisedServices: List<UUID> = emptyList()): Boolean {
    if (advertisedServices.any { it in KNOWN_SERVICE_UUIDS }) return true
    if (deviceName.isNullOrBlank()) return false
    val lower = deviceName.lowercase(Locale.US)
    return KNOWN_ADAPTER_NAME_TOKENS.any { lower.contains(it) }
  }
}

/**
 * Discovered or paired Bluetooth device entry for the OBD-II scanner device list.
 */
data class Obd2BleDevice(
  val name: String,
  val address: String,
  val rssi: Int? = null,
  val isBonded: Boolean = false,
  val isLikelyObd: Boolean = false,
)

/**
 * Category filter pills for the top-toolbar DTC catalog search overlay (matching Plants & Recipes).
 */
enum class DtcSystemFilter(
  val label: String,
  val systemPrefixOrKeyword: String,
) {
  FUEL_AND_AIR("Fuel & Air", "Fuel"),
  IGNITION_MISFIRE("Ignition & Misfire", "Ignition"),
  EMISSIONS("Emissions", "Emission"),
  SPEED_AND_IDLE("Speed & Idle", "Speed"),
  TRANSMISSION("Transmission", "Transmission"),
  CHASSIS_BODY_NET("Chassis, Body & CAN", "Chassis|Body|Network|Computer|Forced|Injector"),
}

/**
 * Connection lifecycle state for the OBD-II scanner.
 */
enum class Obd2ConnectionState {
  DISCONNECTED,
  SCANNING,
  CONNECTING,
  INITIALIZING,
  CONNECTED_BLE,
  CONNECTED_SIMULATOR,
  ERROR,
}

/**
 * Severity classification for Diagnostic Trouble Codes (DTCs).
 */
enum class DtcSeverity(val label: String) {
  CRITICAL("Critical"),
  WARNING("Warning"),
  INFO("Info"),
}

/**
 * Fault code status (Active/Stored Mode 03 vs Pending Mode 07 vs Reference Lookup).
 */
enum class DtcStatus(val label: String) {
  STORED("Stored (Mode 03)"),
  PENDING("Pending (Mode 07)"),
  LOOKUP("Catalog Reference"),
}

/**
 * Represents a decoded OBD-II Diagnostic Trouble Code (SAE J2012).
 */
data class Obd2DtcEntry(
  val code: String,
  val system: String,
  val description: String,
  val commonCauses: String,
  val severity: DtcSeverity,
  val status: DtcStatus = DtcStatus.STORED,
)

/**
 * Readiness monitor completion state (Mode 01 PID 01).
 */
enum class MonitorStatus(val label: String) {
  READY("Ready"),
  INCOMPLETE("Incomplete"),
  NOT_SUPPORTED("Not Supported"),
}

/**
 * Single emissions readiness monitor item decoded from Mode 01 PID 01.
 */
data class ReadinessMonitorItem(
  val name: String,
  val isContinuous: Boolean,
  val status: MonitorStatus,
)

/**
 * Decoded Mode 01 PID 01 MIL and emissions readiness summary.
 */
data class Obd2MonitorSummary(
  val milOn: Boolean = false,
  val storedDtcCount: Int = 0,
  val isCompressionIgnition: Boolean = false,
  val monitors: List<ReadinessMonitorItem> = emptyList(),
) {
  val supportedMonitors: List<ReadinessMonitorItem>
    get() = monitors.filter { it.status != MonitorStatus.NOT_SUPPORTED }

  val readyCount: Int
    get() = supportedMonitors.count { it.status == MonitorStatus.READY }

  val incompleteCount: Int
    get() = supportedMonitors.count { it.status == MonitorStatus.INCOMPLETE }
}

/**
 * Vehicle identification and protocol metadata (Mode 09 & ELM327 status).
 */
data class Obd2VehicleInfo(
  val vin: String = "",
  val calibrationId: String = "",
  val ecuName: String = "",
  val protocolName: String = "ISO 15765-4 CAN (11 bit ID, 500 kbaud)",
  val elmVersion: String = "ELM327 v2.1",
  val modelYearHint: String = "",
  val regionHint: String = "",
)

/**
 * Snapshot of live Mode 01 telemetry and battery voltage.
 */
data class Obd2LiveTelemetry(
  val rpm: Float = 0f,
  val maxRpm: Float = 0f,
  val speedKmh: Float = 0f,
  val maxSpeedKmh: Float = 0f,
  val coolantTempC: Float = 20f,
  val minCoolantTempC: Float = 20f,
  val maxCoolantTempC: Float = 20f,
  val engineLoadPct: Float = 0f,
  val throttlePct: Float = 0f,
  val batteryVoltage: Float = 12.6f,
  val shortTermFuelTrimPct: Float = 0f,
  val longTermFuelTrimPct: Float = 0f,
  val intakeAirTempC: Float = 22f,
  val mafGramsPerSec: Float = 0f,
  val rpmHistory: List<Float> = emptyList(),
  val speedHistory: List<Float> = emptyList(),
)

/**
 * Pure ELM327 / SAE J1979 / SAE J2012 protocol parser and math utilities.
 */
object Obd2Parser {

  /**
   * Converts kilometers per hour to miles per hour.
   */
  fun kmhToMph(kmh: Float): Float = kmh * 0.621371f

  /**
   * Converts Celsius to Fahrenheit.
   */
  fun celsiusToFahrenheit(celsius: Float): Float = (celsius * 9f / 5f) + 32f

  /**
   * Formats speed according to [useImperial] (`km/h` vs `mph`).
   */
  fun formatSpeed(kmh: Float, useImperial: Boolean): Pair<String, String> {
    val value = if (useImperial) kmhToMph(kmh) else kmh
    val unit = if (useImperial) "mph" else "km/h"
    return String.format(Locale.US, "%d", value.roundToInt()) to unit
  }

  /**
   * Formats temperature according to [useImperial] (`°C` vs `°F`).
   */
  fun formatTemperature(celsius: Float, useImperial: Boolean): Pair<String, String> {
    val value = if (useImperial) celsiusToFahrenheit(celsius) else celsius
    val unit = if (useImperial) "°F" else "°C"
    return String.format(Locale.US, "%d", value.roundToInt()) to unit
  }

  /**
   * Normalizes raw ELM327 output by removing prompt characters (`>`), echo lines,
   * status banners (`SEARCHING...`), and blank lines.
   */
  fun cleanElmResponse(raw: String): String {
    return raw
      .replace(">", "")
      .replace("\r", "\n")
      .lineSequence()
      .map { it.trim() }
      .filter { line ->
        line.isNotEmpty() &&
          !line.equals("SEARCHING...", ignoreCase = true) &&
          !line.equals("OK", ignoreCase = true)
      }
      .joinToString("\n")
  }

  /**
   * Extracts hex byte values (`0x00..0xFF`) following an expected Mode + PID response prefix
   * (for example, expectedPrefix `"410C"` for Mode 01 PID 0C).
   */
  fun extractPidDataBytes(raw: String, expectedModeHex: String, expectedPidHex: String): List<Int>? {
    val cleaned = cleanElmResponse(raw)
    if (cleaned.isEmpty() ||
      cleaned.contains("NO DATA", ignoreCase = true) ||
      cleaned.contains("ERROR", ignoreCase = true) ||
      cleaned.contains("UNABLE TO CONNECT", ignoreCase = true)
    ) {
      return null
    }

    val targetPrefix = (expectedModeHex + expectedPidHex).uppercase(Locale.US)
    for (line in cleaned.lines()) {
      val hexOnly = line
        .replace(Regex("^[0-9A-Fa-f]:"), "") // strip ISO-TP frame index like "0:"
        .replace(" ", "")
        .uppercase(Locale.US)
      val prefixIndex = hexOnly.indexOf(targetPrefix)
      if (prefixIndex >= 0) {
        val payloadHex = hexOnly.substring(prefixIndex + targetPrefix.length)
        val bytes = mutableListOf<Int>()
        var i = 0
        while (i + 1 < payloadHex.length) {
          val byteVal = payloadHex.substring(i, i + 2).toIntOrNull(16) ?: break
          bytes.add(byteVal)
          i += 2
        }
        if (bytes.isNotEmpty()) return bytes
      }
    }
    return null
  }

  /**
   * Parses Engine RPM (`010C`, response `41 0C AA BB`): `((A * 256) + B) / 4.0`.
   */
  fun parseRpm(raw: String): Float? {
    val bytes = extractPidDataBytes(raw, "41", "0C") ?: return null
    if (bytes.size < 2) return null
    return (((bytes[0] * 256) + bytes[1]) / 4f).coerceIn(0f, 16383.75f)
  }

  /**
   * Parses Vehicle Speed (`010D`, response `41 0D AA`): `A` km/h.
   */
  fun parseSpeedKmh(raw: String): Float? {
    val bytes = extractPidDataBytes(raw, "41", "0D") ?: return null
    return bytes.firstOrNull()?.toFloat()?.coerceIn(0f, 255f)
  }

  /**
   * Parses Engine Coolant Temperature (`0105`, response `41 05 AA`): `A - 40` °C.
   */
  fun parseCoolantTempC(raw: String): Float? {
    val bytes = extractPidDataBytes(raw, "41", "05") ?: return null
    return (bytes.firstOrNull()?.minus(40))?.toFloat()?.coerceIn(-40f, 215f)
  }

  /**
   * Parses Intake Air Temperature (`010F`, response `41 0F AA`): `A - 40` °C.
   */
  fun parseIntakeAirTempC(raw: String): Float? {
    val bytes = extractPidDataBytes(raw, "41", "0F") ?: return null
    return (bytes.firstOrNull()?.minus(40))?.toFloat()?.coerceIn(-40f, 215f)
  }

  /**
   * Parses Calculated Engine Load (`0104`, response `41 04 AA`): `A * 100 / 255` %.
   */
  fun parseEngineLoadPct(raw: String): Float? {
    val bytes = extractPidDataBytes(raw, "41", "04") ?: return null
    val a = bytes.firstOrNull() ?: return null
    return (a * 100f / 255f).coerceIn(0f, 100f)
  }

  /**
   * Parses Throttle Position (`0111`, response `41 11 AA`): `A * 100 / 255` %.
   */
  fun parseThrottlePct(raw: String): Float? {
    val bytes = extractPidDataBytes(raw, "41", "11") ?: return null
    val a = bytes.firstOrNull() ?: return null
    return (a * 100f / 255f).coerceIn(0f, 100f)
  }

  /**
   * Parses Short-Term (`0106`) or Long-Term (`0107`) Fuel Trim Bank 1: `(A - 128) * 100 / 128` %.
   */
  fun parseFuelTrimPct(raw: String, pidHex: String): Float? {
    val bytes = extractPidDataBytes(raw, "41", pidHex) ?: return null
    val a = bytes.firstOrNull() ?: return null
    return ((a - 128) * 100f / 128f).coerceIn(-100f, 99.22f)
  }

  /**
   * Parses Mass Air Flow rate (`0110`, response `41 10 AA BB`): `((A * 256) + B) / 100.0` g/s.
   */
  fun parseMafGramsPerSec(raw: String): Float? {
    val bytes = extractPidDataBytes(raw, "41", "10") ?: return null
    if (bytes.size < 2) return null
    return (((bytes[0] * 256) + bytes[1]) / 100f).coerceIn(0f, 655.35f)
  }

  /**
   * Parses ELM327 battery voltage reading from `AT RV` (e.g. `"14.2V"`, `"12.6 V"`).
   */
  fun parseBatteryVoltage(raw: String): Float? {
    val match = Regex("(\\d{1,2}\\.\\d{1,2})\\s*[Vv]?").find(raw) ?: return null
    return match.groupValues[1].toFloatOrNull()?.coerceIn(0f, 36f)
  }

  /**
   * Decodes a 2-byte SAE J2012 trouble code (`byteA`, `byteB`) into a 5-character DTC string
   * such as `"P0420"`, `"C0121"`, `"B0001"`, or `"U0100"`. Returns `null` for `00 00`.
   */
  fun decodeTwoByteDtc(byteA: Int, byteB: Int): String? {
    val a = byteA and 0xFF
    val b = byteB and 0xFF
    if (a == 0 && b == 0) return null

    val prefix = when ((a shr 6) and 0x03) {
      0 -> 'P'
      1 -> 'C'
      2 -> 'B'
      else -> 'U'
    }
    val digit1 = (a shr 4) and 0x03
    val digit2 = a and 0x0F
    val digit3 = (b shr 4) and 0x0F
    val digit4 = b and 0x0F
    return String.format(Locale.US, "%c%d%X%X%X", prefix, digit1, digit2, digit3, digit4)
  }

  /**
   * Parses Mode 03 (Stored, response `43 ...`) or Mode 07 (Pending, response `47 ...`) hex responses
   * into a distinct list of [Obd2DtcEntry] items enriched with the offline DTC catalog.
   */
  fun parseDtcResponse(
    raw: String,
    expectedModeResponseHex: String = "43",
    status: DtcStatus = DtcStatus.STORED,
  ): List<Obd2DtcEntry> {
    val cleaned = cleanElmResponse(raw)
    if (cleaned.isEmpty() ||
      cleaned.contains("NO DATA", ignoreCase = true) ||
      cleaned.contains("ERROR", ignoreCase = true)
    ) {
      return emptyList()
    }

    val modePrefix = expectedModeResponseHex.uppercase(Locale.US)
    val hexStream = StringBuilder()

    for (line in cleaned.lines()) {
      var normalized = line.trim()
      // Ignore ISO-TP byte count header line like "00A" or "014"
      if (normalized.matches(Regex("^[0-9A-Fa-f]{3}$"))) continue
      // Strip multi-frame index prefix like "0:" or "1:"
      normalized = normalized.replace(Regex("^[0-9A-Fa-f]:\\s*"), "")
      val compact = normalized.replace(" ", "").uppercase(Locale.US)
      if (!compact.matches(Regex("^[0-9A-F]+$"))) continue

      if (compact.startsWith(modePrefix)) {
        val afterMode = compact.substring(modePrefix.length)
        // In CAN (ISO 15765-4), the byte immediately after 43/47 is the DTC count if length % 4 == 2
        val payload = if (afterMode.length % 4 == 2) {
          afterMode.substring(2)
        } else {
          afterMode
        }
        hexStream.append(payload)
      } else {
        hexStream.append(compact)
      }
    }

    val codes = mutableSetOf<String>()
    val allHex = hexStream.toString()
    var i = 0
    while (i + 3 < allHex.length) {
      val byteA = allHex.substring(i, i + 2).toIntOrNull(16)
      val byteB = allHex.substring(i + 2, i + 4).toIntOrNull(16)
      if (byteA != null && byteB != null) {
        decodeTwoByteDtc(byteA, byteB)?.let { codes.add(it) }
      }
      i += 4
    }

    return codes.map { code -> Obd2DtcCatalog.lookup(code, status) }
  }

  /**
   * Parses Mode 01 PID 01 (`0101`, response `41 01 AA BB CC DD`) into an [Obd2MonitorSummary]
   * containing MIL (Check Engine Light) status, stored DTC count, ignition type, and readiness monitors.
   */
  fun parseMonitorStatus(raw: String): Obd2MonitorSummary? {
    val bytes = extractPidDataBytes(raw, "41", "01") ?: return null
    if (bytes.size < 4) return null

    val a = bytes[0]
    val b = bytes[1]
    val c = bytes[2]
    val d = bytes[3]

    val milOn = (a and 0x80) != 0
    val dtcCount = a and 0x7F
    val isCompression = (b and 0x08) != 0

    fun decodeMonitor(
      name: String,
      isContinuous: Boolean,
      supported: Boolean,
      incomplete: Boolean,
    ): ReadinessMonitorItem {
      val status = when {
        !supported -> MonitorStatus.NOT_SUPPORTED
        incomplete -> MonitorStatus.INCOMPLETE
        else -> MonitorStatus.READY
      }
      return ReadinessMonitorItem(name = name, isContinuous = isContinuous, status = status)
    }

    val monitors = mutableListOf(
      decodeMonitor(
        name = "Misfire Monitor",
        isContinuous = true,
        supported = (b and 0x01) != 0,
        incomplete = (b and 0x10) != 0,
      ),
      decodeMonitor(
        name = "Fuel System Monitor",
        isContinuous = true,
        supported = (b and 0x02) != 0,
        incomplete = (b and 0x20) != 0,
      ),
      decodeMonitor(
        name = "Comprehensive Components",
        isContinuous = true,
        supported = (b and 0x04) != 0,
        incomplete = (b and 0x40) != 0,
      ),
    )

    if (!isCompression) {
      // Spark ignition monitors
      monitors.addAll(
        listOf(
          decodeMonitor("Catalyst", false, (c and 0x01) != 0, (d and 0x01) != 0),
          decodeMonitor("Heated Catalyst", false, (c and 0x02) != 0, (d and 0x02) != 0),
          decodeMonitor("Evaporative System (EVAP)", false, (c and 0x04) != 0, (d and 0x04) != 0),
          decodeMonitor("Secondary Air System", false, (c and 0x08) != 0, (d and 0x08) != 0),
          decodeMonitor("Oxygen (O2) Sensor", false, (c and 0x20) != 0, (d and 0x20) != 0),
          decodeMonitor("O2 Sensor Heater", false, (c and 0x40) != 0, (d and 0x40) != 0),
          decodeMonitor("EGR / VVT System", false, (c and 0x80) != 0, (d and 0x80) != 0),
        )
      )
    } else {
      // Compression (Diesel) ignition monitors
      monitors.addAll(
        listOf(
          decodeMonitor("NMHC Catalyst", false, (c and 0x01) != 0, (d and 0x01) != 0),
          decodeMonitor("NOx / SCR Aftertreatment", false, (c and 0x02) != 0, (d and 0x02) != 0),
          decodeMonitor("Boost Pressure", false, (c and 0x08) != 0, (d and 0x08) != 0),
          decodeMonitor("Exhaust Gas Sensor", false, (c and 0x20) != 0, (d and 0x20) != 0),
          decodeMonitor("PM Filter Monitor", false, (c and 0x40) != 0, (d and 0x40) != 0),
          decodeMonitor("EGR / VVT System", false, (c and 0x80) != 0, (d and 0x80) != 0),
        )
      )
    }

    return Obd2MonitorSummary(
      milOn = milOn,
      storedDtcCount = dtcCount,
      isCompressionIgnition = isCompression,
      monitors = monitors,
    )
  }

  /**
   * Parses Mode 09 PID 02 (`0902`, response `49 02 ...`) multi-frame hex into a 17-character VIN.
   */
  fun parseVin(raw: String): String? {
    val cleaned = cleanElmResponse(raw)
    if (cleaned.isEmpty() || cleaned.contains("NO DATA", ignoreCase = true)) return null

    val hexChars = StringBuilder()
    for (line in cleaned.lines()) {
      var trimmed = line.trim()
      if (trimmed.matches(Regex("^[0-9A-Fa-f]{3}$"))) continue
      trimmed = trimmed.replace(Regex("^[0-9A-Fa-f]:\\s*"), "")
      val compact = trimmed.replace(" ", "").uppercase(Locale.US)
      if (!compact.matches(Regex("^[0-9A-F]+$"))) continue

      val withoutHeader = if (compact.startsWith("4902")) {
        // Skip "4902" and optional 1-byte message count (e.g. "01")
        if (compact.length >= 6) compact.substring(6) else compact.substring(4)
      } else {
        compact
      }
      hexChars.append(withoutHeader)
    }

    val ascii = StringBuilder()
    val hex = hexChars.toString()
    var i = 0
    while (i + 1 < hex.length) {
      val b = hex.substring(i, i + 2).toIntOrNull(16) ?: 0
      if (b in 0x30..0x39 || b in 0x41..0x5A) {
        ascii.append(b.toChar())
      }
      i += 2
    }

    val result = ascii.toString()
    return if (result.length >= 17) result.substring(0, 17) else result.takeIf { it.isNotEmpty() }
  }

  /**
   * Infers the manufacturing region and model year from a standard 17-character ISO 3779 VIN.
   */
  fun decodeVinMetadata(vin: String): Pair<String, String> {
    if (vin.length < 10) return "" to ""
    val firstChar = vin[0].uppercaseChar()
    val region = when (firstChar) {
      '1', '4', '5' -> "United States"
      '2' -> "Canada"
      '3' -> "Mexico"
      'J' -> "Japan"
      'K' -> "South Korea"
      'L' -> "China"
      'S' -> "United Kingdom"
      'V' -> "France / Spain"
      'W' -> "Germany"
      'Y' -> "Sweden / Finland"
      'Z' -> "Italy"
      else -> "International"
    }

    val yearChar = vin[9].uppercaseChar()
    val yearMap = mapOf(
      'A' to "2010", 'B' to "2011", 'C' to "2012", 'D' to "2013", 'E' to "2014",
      'F' to "2015", 'G' to "2016", 'H' to "2017", 'J' to "2018", 'K' to "2019",
      'L' to "2020", 'M' to "2021", 'N' to "2022", 'P' to "2023", 'R' to "2024",
      'S' to "2025", 'T' to "2026", 'V' to "2027",
      '1' to "2001", '2' to "2002", '3' to "2003", '4' to "2004", '5' to "2005",
      '6' to "2006", '7' to "2007", '8' to "2008", '9' to "2009",
    )
    val modelYear = yearMap[yearChar] ?: "Unknown"
    return region to modelYear
  }
}

/**
 * Built-in offline SAE J2012 Diagnostic Trouble Code (DTC) catalog and intelligent fallback decoder.
 */
object Obd2DtcCatalog {

  val ALL_ENTRIES: List<Obd2DtcEntry> = listOf(
    Obd2DtcEntry(
      code = "P0101",
      system = "Fuel & Air Metering",
      description = "Mass or Volume Air Flow (MAF) 'A' Circuit Range / Performance",
      commonCauses = "Dirty or faulty MAF sensor, intake air leak, clogged air filter, or frayed MAF wiring harness.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0102",
      system = "Fuel & Air Metering",
      description = "Mass or Volume Air Flow (MAF) 'A' Circuit Low Input",
      commonCauses = "Disconnected MAF sensor plug, open/shorted MAF signal wire, or failed MAF sensor.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0106",
      system = "Fuel & Air Metering",
      description = "Manifold Absolute Pressure (MAP) / Barometric Pressure Circuit Range / Performance",
      commonCauses = "Vacuum leak in intake manifold, faulty MAP sensor, or carbon buildup in sensor port.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0113",
      system = "Fuel & Air Metering",
      description = "Intake Air Temperature (IAT) Sensor 1 Circuit High Input",
      commonCauses = "Open circuit in IAT sensor connector, unplugged MAF/IAT assembly, or failed thermistor.",
      severity = DtcSeverity.INFO,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0118",
      system = "Fuel & Air Metering",
      description = "Engine Coolant Temperature (ECT) Sensor 1 Circuit High Input",
      commonCauses = "Faulty coolant temperature sensor, corroded ECT connector, or low coolant level.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0122",
      system = "Fuel & Air Metering",
      description = "Throttle / Pedal Position Sensor 'A' Circuit Low Input",
      commonCauses = "Faulty TPS sensor, loose throttle body connector, or short to ground on TPS signal wire.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0128",
      system = "Fuel & Air Metering",
      description = "Coolant Thermostat (Coolant Temperature Below Thermostat Regulating Temperature)",
      commonCauses = "Thermostat stuck open, low engine coolant, or inaccurate ECT sensor.",
      severity = DtcSeverity.INFO,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0131",
      system = "Fuel & Air Metering",
      description = "O2 Sensor Circuit Low Voltage (Bank 1, Sensor 1)",
      commonCauses = "Upstream oxygen sensor aging, exhaust leak before O2 sensor, or lean fuel condition.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0133",
      system = "Fuel & Air Metering",
      description = "O2 Sensor Circuit Slow Response (Bank 1, Sensor 1)",
      commonCauses = "Sluggish upstream O2 sensor, exhaust manifold leak, or contaminated sensor element.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0135",
      system = "Fuel & Air Metering",
      description = "O2 Sensor Heater Circuit (Bank 1, Sensor 1)",
      commonCauses = "Blown O2 heater fuse, open internal heater element in upstream O2 sensor, or damaged wiring.",
      severity = DtcSeverity.INFO,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0171",
      system = "Fuel & Air Metering",
      description = "System Too Lean (Bank 1)",
      commonCauses = "Vacuum leak (intake hose/gasket/PCV), dirty MAF sensor, weak fuel pump, or clogged fuel filter.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0172",
      system = "Fuel & Air Metering",
      description = "System Too Rich (Bank 1)",
      commonCauses = "Leaking fuel injector, stuck-open EVAP purge valve, faulty fuel pressure regulator, or dirty MAF.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0174",
      system = "Fuel & Air Metering",
      description = "System Too Lean (Bank 2)",
      commonCauses = "Intake vacuum leak, faulty PCV valve, dirty MAF sensor, or low fuel rail pressure.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0201",
      system = "Injector Circuit",
      description = "Injector Circuit / Open - Cylinder 1",
      commonCauses = "Failed Cylinder 1 fuel injector solenoid, unplugged injector harness, or ECM driver fault.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0299",
      system = "Forced Induction",
      description = "Turbocharger / Supercharger 'A' Underboost Condition",
      commonCauses = "Boost leak in charge pipe/intercooler, stuck wastegate actuator, or faulty diverter valve.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0300",
      system = "Ignition & Misfire",
      description = "Random / Multiple Cylinder Misfire Detected",
      commonCauses = "Worn spark plugs, failing ignition coils, vacuum leak, low fuel pressure, or uneven compression.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0301",
      system = "Ignition & Misfire",
      description = "Cylinder 1 Misfire Detected",
      commonCauses = "Faulty Cylinder 1 spark plug or ignition coil pack, clogged fuel injector, or low cylinder compression.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0302",
      system = "Ignition & Misfire",
      description = "Cylinder 2 Misfire Detected",
      commonCauses = "Faulty Cylinder 2 spark plug or ignition coil pack, clogged fuel injector, or low cylinder compression.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0303",
      system = "Ignition & Misfire",
      description = "Cylinder 3 Misfire Detected",
      commonCauses = "Faulty Cylinder 3 spark plug or ignition coil pack, clogged fuel injector, or low cylinder compression.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0304",
      system = "Ignition & Misfire",
      description = "Cylinder 4 Misfire Detected",
      commonCauses = "Faulty Cylinder 4 spark plug or ignition coil pack, clogged fuel injector, or low cylinder compression.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0325",
      system = "Ignition & Misfire",
      description = "Knock Sensor 1 Circuit (Bank 1 or Single Sensor)",
      commonCauses = "Failed piezo knock sensor, brittle knock sensor sub-harness, or loose sensor mounting bolt.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0335",
      system = "Ignition & Misfire",
      description = "Crankshaft Position Sensor 'A' Circuit",
      commonCauses = "Failed crankshaft position sensor, damaged reluctor ring, or oil-contaminated connector.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0340",
      system = "Ignition & Misfire",
      description = "Camshaft Position Sensor 'A' Circuit (Bank 1 or Single Sensor)",
      commonCauses = "Failed camshaft position sensor, stretched timing chain/belt, or open sensor wiring.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0401",
      system = "Emission Controls",
      description = "Exhaust Gas Recirculation (EGR) Flow Insufficient Detected",
      commonCauses = "Carbon-clogged EGR passages, stuck EGR valve, or faulty DPFE/EGR temperature sensor.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0420",
      system = "Emission Controls",
      description = "Catalyst System Efficiency Below Threshold (Bank 1)",
      commonCauses = "Aging catalytic converter, exhaust leak near rear O2 sensor, or faulty downstream O2 sensor.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0430",
      system = "Emission Controls",
      description = "Catalyst System Efficiency Below Threshold (Bank 2)",
      commonCauses = "Worn Bank 2 catalytic converter, exhaust flange leak, or degraded downstream O2 sensor.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0440",
      system = "Emission Controls",
      description = "Evaporative Emission (EVAP) Control System Malfunction",
      commonCauses = "Loose or worn gas cap seal, cracked EVAP vapor hose, or faulty purge/vent valve.",
      severity = DtcSeverity.INFO,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0442",
      system = "Emission Controls",
      description = "Evaporative Emission (EVAP) System Leak Detected (Small Leak)",
      commonCauses = "Loose fuel filler cap, hairline crack in charcoal canister hose, or leaking purge solenoid.",
      severity = DtcSeverity.INFO,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0455",
      system = "Emission Controls",
      description = "Evaporative Emission (EVAP) System Leak Detected (Large / Gross Leak)",
      commonCauses = "Missing or unseated gas cap, disconnected EVAP line, or stuck-open EVAP vent valve.",
      severity = DtcSeverity.INFO,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0456",
      system = "Emission Controls",
      description = "Evaporative Emission (EVAP) System Leak Detected (Very Small Leak)",
      commonCauses = "Worn fuel cap O-ring, aging EVAP vapor line, or slight seepage at fuel tank seal.",
      severity = DtcSeverity.INFO,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0500",
      system = "Speed & Idle Control",
      description = "Vehicle Speed Sensor (VSS) 'A'",
      commonCauses = "Faulty wheel speed / transmission output speed sensor, damaged tone ring, or ABS module fault.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0505",
      system = "Speed & Idle Control",
      description = "Idle Air Control (IAC) System",
      commonCauses = "Carbon buildup in throttle body / IAC valve, vacuum leak, or failed IAC stepper motor.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0507",
      system = "Speed & Idle Control",
      description = "Idle Air Control System RPM Higher Than Expected",
      commonCauses = "Intake manifold or PCV vacuum leak, dirty throttle plate, or uncalibrated electronic throttle body.",
      severity = DtcSeverity.INFO,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0562",
      system = "Computer & Electrical",
      description = "System Voltage Low",
      commonCauses = "Weak 12V battery, failing alternator voltage regulator, or corroded battery terminals/grounds.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0700",
      system = "Transmission",
      description = "Transmission Control System (MIL Request)",
      commonCauses = "Transmission Control Module (TCM) has stored a specific shift solenoid or speed sensor fault.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "P0741",
      system = "Transmission",
      description = "Torque Converter Clutch (TCC) Circuit Performance or Stuck Off",
      commonCauses = "Low/dirty transmission fluid, worn TCC solenoid, or internal torque converter clutch wear.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "C0035",
      system = "Chassis (ABS / Traction)",
      description = "Left Front Wheel Speed Sensor Supply / Circuit",
      commonCauses = "Damaged front-left ABS wheel speed sensor, metallic debris on tone ring, or broken sensor lead.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "B0001",
      system = "Body (Restraints / SRS)",
      description = "Driver Frontal Stage 1 Deployment Control",
      commonCauses = "Worn steering column clockspring ribbon cable or loose airbag squib connector.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "U0100",
      system = "Network (CAN Bus)",
      description = "Lost Communication With ECM / PCM 'A'",
      commonCauses = "CAN bus high/low wiring fault, blown ECM power fuse, or loose ground strap.",
      severity = DtcSeverity.CRITICAL,
      status = DtcStatus.LOOKUP,
    ),
    Obd2DtcEntry(
      code = "U0101",
      system = "Network (CAN Bus)",
      description = "Lost Communication With TCM (Transmission Control Module)",
      commonCauses = "Corroded TCM connector, low battery voltage during cranking, or CAN bus termination issue.",
      severity = DtcSeverity.WARNING,
      status = DtcStatus.LOOKUP,
    ),
  )

  private val BY_CODE: Map<String, Obd2DtcEntry> = ALL_ENTRIES.associateBy { it.code.uppercase(Locale.US) }

  /**
   * Looks up a 5-character DTC code in the offline dictionary, or synthesizes a structured
   * SAE J2012 subsystem classification if the specific code is manufacturer-specific or unlisted.
   */
  fun lookup(rawCode: String, status: DtcStatus = DtcStatus.LOOKUP): Obd2DtcEntry {
    val normalized = rawCode.trim().uppercase(Locale.US)
    BY_CODE[normalized]?.let { return it.copy(status = status) }

    val prefix = normalized.firstOrNull() ?: 'P'
    val secondChar = normalized.getOrNull(1) ?: '0'
    val thirdChar = normalized.getOrNull(2) ?: '0'
    val isManufacturerSpecific = secondChar in listOf('1', '3')

    val (system, subDesc, severity) = when (prefix) {
      'P' -> when (thirdChar) {
        '0', '1', '2' -> Triple(
          "Fuel & Air Metering",
          "Fuel and air metering or injector circuit fault",
          DtcSeverity.WARNING,
        )
        '3' -> Triple(
          "Ignition & Misfire",
          "Ignition system or cylinder misfire condition",
          DtcSeverity.CRITICAL,
        )
        '4' -> Triple(
          "Emission Controls",
          "Auxiliary emission control system fault",
          DtcSeverity.INFO,
        )
        '5' -> Triple(
          "Speed & Idle Control",
          "Vehicle speed, idle control, or auxiliary input fault",
          DtcSeverity.WARNING,
        )
        '6' -> Triple(
          "Computer & Output Circuit",
          "ECU / PCM processor or actuator output circuit fault",
          DtcSeverity.WARNING,
        )
        '7', '8', '9' -> Triple(
          "Transmission",
          "Transmission control or shift solenoid circuit fault",
          DtcSeverity.WARNING,
        )
        else -> Triple(
          "Powertrain",
          "Powertrain diagnostic trouble code",
          DtcSeverity.WARNING,
        )
      }
      'C' -> Triple(
        "Chassis (ABS / Stability)",
        "Chassis, braking, steering, or suspension system fault",
        DtcSeverity.WARNING,
      )
      'B' -> Triple(
        "Body Systems",
        "Body electronics, HVAC, lighting, or restraint fault",
        DtcSeverity.INFO,
      )
      'U' -> Triple(
        "Network (CAN / LIN Bus)",
        "Inter-module CAN bus communication fault",
        DtcSeverity.WARNING,
      )
      else -> Triple(
        "OBD-II Diagnostic",
        "Unclassified diagnostic trouble code",
        DtcSeverity.INFO,
      )
    }

    val scopeLabel = if (isManufacturerSpecific) "Manufacturer-Specific" else "SAE Standard"
    return Obd2DtcEntry(
      code = normalized,
      system = system,
      description = "$scopeLabel $subDesc ($normalized)",
      commonCauses = "Inspect wiring harness, sensor connector pins, and manufacturer service bulletin for $normalized.",
      severity = severity,
      status = status,
    )
  }

  /**
   * Searches the offline catalog by code, system, or description keyword, optionally filtered by
   * [categoryFilter]. If [query] matches the format of a valid 5-character OBD-II code
   * (`[PCBU][0-3][0-9A-F]{3}`) not yet in the static list, includes a dynamically decoded entry.
   */
  fun search(
    query: String,
    categoryFilter: DtcSystemFilter? = null,
  ): List<Obd2DtcEntry> {
    val trimmed = query.trim()
    val filteredByCategory = if (categoryFilter == null) {
      ALL_ENTRIES
    } else {
      val regex = Regex(categoryFilter.systemPrefixOrKeyword, RegexOption.IGNORE_CASE)
      ALL_ENTRIES.filter { regex.containsMatchIn(it.system) }
    }

    if (trimmed.isEmpty()) return filteredByCategory

    val matches = filteredByCategory.filter { entry ->
      entry.code.contains(trimmed, ignoreCase = true) ||
        entry.system.contains(trimmed, ignoreCase = true) ||
        entry.description.contains(trimmed, ignoreCase = true) ||
        entry.commonCauses.contains(trimmed, ignoreCase = true)
    }

    val upper = trimmed.uppercase(Locale.US)
    if (matches.none { it.code == upper } && upper.matches(Regex("^[PCBU][0-3][0-9A-F]{3}$"))) {
      val dynamicEntry = lookup(upper, DtcStatus.LOOKUP)
      val matchesCat = categoryFilter == null ||
        Regex(categoryFilter.systemPrefixOrKeyword, RegexOption.IGNORE_CASE).containsMatchIn(dynamicEntry.system)
      if (matchesCat) {
        return listOf(dynamicEntry) + matches
      }
    }
    return matches
  }
}

/**
 * High-level controller managing BLE GATT scanning, ELM327 connection & command queuing,
 * and the built-in Demo Vehicle Simulator.
 */
class Obd2ScannerController(
  private val context: Context,
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
  private val _connectionState = MutableStateFlow(Obd2ConnectionState.DISCONNECTED)
  val connectionState: StateFlow<Obd2ConnectionState> = _connectionState.asStateFlow()

  private val _connectedDeviceName = MutableStateFlow<String?>(null)
  val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

  private val _statusMessage = MutableStateFlow("Not connected • Tap Connect for BLE or Demo Vehicle")
  val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

  private val _discoveredDevices = MutableStateFlow<List<Obd2BleDevice>>(emptyList())
  val discoveredDevices: StateFlow<List<Obd2BleDevice>> = _discoveredDevices.asStateFlow()

  private val _pairedDevices = MutableStateFlow<List<Obd2BleDevice>>(emptyList())
  val pairedDevices: StateFlow<List<Obd2BleDevice>> = _pairedDevices.asStateFlow()

  private val _connectingDevice = MutableStateFlow<Obd2BleDevice?>(null)
  val connectingDevice: StateFlow<Obd2BleDevice?> = _connectingDevice.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _liveTelemetry = MutableStateFlow(Obd2LiveTelemetry())
  val liveTelemetry: StateFlow<Obd2LiveTelemetry> = _liveTelemetry.asStateFlow()

  private val _activeDtcs = MutableStateFlow<List<Obd2DtcEntry>>(emptyList())
  val activeDtcs: StateFlow<List<Obd2DtcEntry>> = _activeDtcs.asStateFlow()

  private val _monitorSummary = MutableStateFlow(Obd2MonitorSummary())
  val monitorSummary: StateFlow<Obd2MonitorSummary> = _monitorSummary.asStateFlow()

  private val _vehicleInfo = MutableStateFlow(Obd2VehicleInfo())
  val vehicleInfo: StateFlow<Obd2VehicleInfo> = _vehicleInfo.asStateFlow()

  private val _isPollingLive = MutableStateFlow(true)
  val isPollingLive: StateFlow<Boolean> = _isPollingLive.asStateFlow()

  private var bluetoothGatt: BluetoothGatt? = null
  private var writeCharacteristic: BluetoothGattCharacteristic? = null
  private var notifyCharacteristic: BluetoothGattCharacteristic? = null

  private val commandMutex = Mutex()
  private val responseBuffer = StringBuilder()
  private var pendingResponse: CompletableDeferred<String>? = null
  private var pollingJob: Job? = null
  private var scanJob: Job? = null
  private var simTick: Int = 0

  private val bluetoothAdapter: BluetoothAdapter?
    get() = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

  fun hasBluetoothAdapter(): Boolean = bluetoothAdapter != null

  fun isBluetoothEnabled(): Boolean = try {
    bluetoothAdapter?.isEnabled == true
  } catch (_: SecurityException) {
    false
  }

  @SuppressLint("MissingPermission")
  fun refreshPairedDevices() {
    val adapter = bluetoothAdapter ?: run {
      _pairedDevices.value = emptyList()
      return
    }
    try {
      val bonded = adapter.bondedDevices?.map { dev ->
        val devName = dev.name?.takeIf { it.isNotBlank() } ?: "Bluetooth Device (${dev.address})"
        Obd2BleDevice(
          name = devName,
          address = dev.address,
          rssi = null,
          isBonded = true,
          isLikelyObd = Obd2BleUuids.isLikelyObdAdapter(devName, emptyList()),
        )
      }?.sortedWith(
        compareByDescending<Obd2BleDevice> { it.isLikelyObd }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      ) ?: emptyList()
      _pairedDevices.value = bonded
    } catch (_: SecurityException) {
      _pairedDevices.value = emptyList()
    }
  }

  fun dismissError() {
    _errorMessage.value = null
  }

  private val leScanCallback = object : ScanCallback() {
    @SuppressLint("MissingPermission")
    override fun onScanResult(callbackType: Int, result: ScanResult) {
      val device = result.device ?: return
      val address = device.address ?: return
      val name = try {
        device.name?.takeIf { it.isNotBlank() }
          ?: result.scanRecord?.deviceName?.takeIf { it.isNotBlank() }
          ?: "BLE Device ($address)"
      } catch (_: SecurityException) {
        "BLE Device ($address)"
      }
      val serviceUuids = result.scanRecord?.serviceUuids?.map { it.uuid } ?: emptyList()
      val isLikelyObd = Obd2BleUuids.isLikelyObdAdapter(name, serviceUuids)
      val isBonded = try {
        device.bondState == BluetoothDevice.BOND_BONDED
      } catch (_: SecurityException) {
        false
      }
      val entry = Obd2BleDevice(
        name = name,
        address = address,
        rssi = result.rssi,
        isBonded = isBonded,
        isLikelyObd = isLikelyObd,
      )
      _discoveredDevices.update { current ->
        val filtered = current.filterNot { it.address == address }
        (filtered + entry).sortedWith(
          compareByDescending<Obd2BleDevice> { it.isLikelyObd }
            .thenByDescending { it.rssi ?: -100 }
        )
      }
    }

    override fun onScanFailed(errorCode: Int) {
      _connectionState.value = Obd2ConnectionState.ERROR
      _statusMessage.value = "BLE scan failed (code $errorCode)"
    }
  }

  /**
   * Starts a 10-second Bluetooth Low Energy scan for nearby OBD-II dongles.
   */
  @SuppressLint("MissingPermission")
  fun startBleScan() {
    stopBleScan()
    val adapter = bluetoothAdapter
    if (adapter == null || !adapter.isEnabled) {
      _statusMessage.value = "Bluetooth is turned off"
      return
    }
    val scanner = adapter.bluetoothLeScanner
    if (scanner == null) {
      _statusMessage.value = "BLE scanner unavailable on this device"
      return
    }

    _discoveredDevices.value = emptyList()
    _connectionState.value = Obd2ConnectionState.SCANNING
    _statusMessage.value = "Scanning for BLE OBD-II adapters..."

    try {
      val settings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
      scanner.startScan(null, settings, leScanCallback)
      scanJob = scope.launch {
        delay(10_000L)
        if (_connectionState.value == Obd2ConnectionState.SCANNING) {
          stopBleScan()
          _connectionState.value = Obd2ConnectionState.DISCONNECTED
          val count = _discoveredDevices.value.size
          _statusMessage.value = if (count > 0) {
            "Found $count BLE device(s) • Tap to connect"
          } else {
            "No BLE adapters found • Check ignition & adapter power"
          }
        }
      }
    } catch (e: Exception) {
      _connectionState.value = Obd2ConnectionState.ERROR
      _statusMessage.value = "Could not start BLE scan: ${e.message ?: "error"}"
    }
  }

  @SuppressLint("MissingPermission")
  fun stopBleScan() {
    scanJob?.cancel()
    scanJob = null
    try {
      bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
    } catch (_: Exception) {
    }
    if (_connectionState.value == Obd2ConnectionState.SCANNING) {
      _connectionState.value = Obd2ConnectionState.DISCONNECTED
      _statusMessage.value = "Select a paired adapter or tap + to scan"
    }
  }

  /**
   * Connects to a real BLE OBD-II dongle by its MAC [address] and initializes ELM327 over GATT.
   */
  @SuppressLint("MissingPermission")
  fun connectToBleDevice(deviceInfo: Obd2BleDevice) {
    stopBleScan()
    disconnectInternal(resetMessage = false)
    _errorMessage.value = null

    val adapter = bluetoothAdapter ?: run {
      _connectionState.value = Obd2ConnectionState.ERROR
      _statusMessage.value = "Bluetooth unavailable"
      _errorMessage.value = "Bluetooth unavailable on this device"
      return
    }

    val remoteDevice: BluetoothDevice = try {
      adapter.getRemoteDevice(deviceInfo.address)
    } catch (e: Exception) {
      _connectionState.value = Obd2ConnectionState.ERROR
      _statusMessage.value = "Invalid BLE address: ${deviceInfo.address}"
      _errorMessage.value = "Invalid BLE address: ${deviceInfo.address}"
      return
    }

    _connectingDevice.value = deviceInfo
    _connectionState.value = Obd2ConnectionState.CONNECTING
    _connectedDeviceName.value = deviceInfo.name
    _statusMessage.value = "Connecting to ${deviceInfo.name}..."

    bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      remoteDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    } else {
      remoteDevice.connectGatt(context, false, gattCallback)
    }
  }

  private val gattCallback = object : BluetoothGattCallback() {
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
      when (newState) {
        BluetoothProfile.STATE_CONNECTED -> {
          _connectionState.value = Obd2ConnectionState.INITIALIZING
          _statusMessage.value = "Discovering GATT services..."
          gatt.discoverServices()
        }
        BluetoothProfile.STATE_DISCONNECTED -> {
          pollingJob?.cancel()
          pendingResponse?.completeExceptionally(IllegalStateException("Disconnected"))
          _connectingDevice.value = null
          _connectionState.value = Obd2ConnectionState.DISCONNECTED
          _connectedDeviceName.value = null
          _statusMessage.value = "Disconnected from BLE adapter"
          try {
            gatt.close()
          } catch (_: Exception) {
          }
          if (bluetoothGatt === gatt) {
            bluetoothGatt = null
          }
        }
      }
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
      if (status != BluetoothGatt.GATT_SUCCESS) {
        _connectingDevice.value = null
        _connectionState.value = Obd2ConnectionState.ERROR
        _statusMessage.value = "Service discovery failed (status $status)"
        _errorMessage.value = "Service discovery failed (status $status)"
        return
      }

      val (notifyChar, writeChar) = resolveObdGattCharacteristics(gatt)
      if (notifyChar == null || writeChar == null) {
        _connectingDevice.value = null
        _connectionState.value = Obd2ConnectionState.ERROR
        _statusMessage.value = "Unsupported BLE device (no UART GATT characteristics)"
        _errorMessage.value = "Unsupported BLE device (no UART GATT characteristics)"
        return
      }

      notifyCharacteristic = notifyChar
      writeCharacteristic = writeChar

      gatt.setCharacteristicNotification(notifyChar, true)
      val cccd = notifyChar.getDescriptor(Obd2BleUuids.CCCD_UUID)
      if (cccd != null) {
        val enableValue = if ((notifyChar.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
          BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        } else {
          BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          gatt.writeDescriptor(cccd, enableValue)
        } else {
          @Suppress("DEPRECATION")
          cccd.value = enableValue
          @Suppress("DEPRECATION")
          gatt.writeDescriptor(cccd)
        }
      } else {
        startElmInitialization()
      }
    }

    override fun onDescriptorWrite(
      gatt: BluetoothGatt,
      descriptor: BluetoothGattDescriptor,
      status: Int,
    ) {
      if (descriptor.uuid == Obd2BleUuids.CCCD_UUID) {
        startElmInitialization()
      }
    }

    override fun onCharacteristicChanged(
      gatt: BluetoothGatt,
      characteristic: BluetoothGattCharacteristic,
      value: ByteArray,
    ) {
      onIncomingBleBytes(value)
    }

    @Deprecated("Deprecated in API 33")
    @Suppress("DEPRECATION")
    override fun onCharacteristicChanged(
      gatt: BluetoothGatt,
      characteristic: BluetoothGattCharacteristic,
    ) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        characteristic.value?.let { onIncomingBleBytes(it) }
      }
    }
  }

  private fun onIncomingBleBytes(bytes: ByteArray) {
    val chunk = bytes.toString(Charsets.US_ASCII)
    synchronized(responseBuffer) {
      responseBuffer.append(chunk)
      if (responseBuffer.contains(">")) {
        val full = responseBuffer.toString()
        responseBuffer.clear()
        pendingResponse?.complete(full)
      }
    }
  }

  private fun resolveObdGattCharacteristics(
    gatt: BluetoothGatt,
  ): Pair<BluetoothGattCharacteristic?, BluetoothGattCharacteristic?> {
    // 1. Check Vgate / Veepeak FFF0 service
    gatt.getService(Obd2BleUuids.SERVICE_FFF0)?.let { svc ->
      val notify = svc.getCharacteristic(Obd2BleUuids.CHAR_FFF1_NOTIFY)
      val write = svc.getCharacteristic(Obd2BleUuids.CHAR_FFF2_WRITE) ?: notify
      if (notify != null && write != null) return notify to write
    }
    // 2. Check HM-10 / Viecar FFE0 service
    gatt.getService(Obd2BleUuids.SERVICE_FFE0)?.let { svc ->
      val char = svc.getCharacteristic(Obd2BleUuids.CHAR_FFE1_RDWR)
      if (char != null) return char to char
    }
    // 3. Check 18F0 service
    gatt.getService(Obd2BleUuids.SERVICE_18F0)?.let { svc ->
      val notify = svc.getCharacteristic(Obd2BleUuids.CHAR_2AF0_NOTIFY)
      val write = svc.getCharacteristic(Obd2BleUuids.CHAR_2AF1_WRITE) ?: notify
      if (notify != null && write != null) return notify to write
    }
    // 4. Check Nordic UART Service (NUS)
    gatt.getService(Obd2BleUuids.SERVICE_NUS)?.let { svc ->
      val notify = svc.getCharacteristic(Obd2BleUuids.CHAR_NUS_NOTIFY)
      val write = svc.getCharacteristic(Obd2BleUuids.CHAR_NUS_WRITE)
      if (notify != null && write != null) return notify to write
    }
    // 5. Fallback: find first custom service with notify + write characteristics
    for (svc in gatt.services) {
      val uuidStr = svc.uuid.toString().lowercase(Locale.US)
      if (uuidStr.startsWith("00001800") || uuidStr.startsWith("00001801")) continue
      var notifyCandidate: BluetoothGattCharacteristic? = null
      var writeCandidate: BluetoothGattCharacteristic? = null
      for (ch in svc.characteristics) {
        val props = ch.properties
        if (notifyCandidate == null &&
          ((props and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 ||
            (props and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0)
        ) {
          notifyCandidate = ch
        }
        if (writeCandidate == null &&
          ((props and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
            (props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0)
        ) {
          writeCandidate = ch
        }
      }
      if (notifyCandidate != null && writeCandidate != null) {
        return notifyCandidate to writeCandidate
      }
    }
    return null to null
  }

  private fun startElmInitialization() {
    scope.launch {
      _connectionState.value = Obd2ConnectionState.INITIALIZING
      _statusMessage.value = "Initializing ELM327 protocol..."

      val elmVersionResp = sendElmCommand("ATZ", timeoutMs = 2500L)
      sendElmCommand("ATE0") // Echo off
      sendElmCommand("ATL0") // Linefeeds off
      sendElmCommand("ATS0") // Spaces off
      sendElmCommand("ATH0") // Headers off
      sendElmCommand("ATSP0") // Auto protocol detection

      val protocolName = Obd2Parser.cleanElmResponse(sendElmCommand("ATDP")).ifBlank {
        "AUTO (ISO 15765-4 CAN)"
      }
      val versionClean = Obd2Parser.cleanElmResponse(elmVersionResp)
        .lines()
        .firstOrNull { it.contains("ELM", ignoreCase = true) || it.contains("OBD", ignoreCase = true) }
        ?: "ELM327 BLE"

      _vehicleInfo.update {
        it.copy(
          elmVersion = versionClean,
          protocolName = protocolName,
        )
      }

      _connectingDevice.value = null
      _connectionState.value = Obd2ConnectionState.CONNECTED_BLE
      _statusMessage.value = "Connected to ${_connectedDeviceName.value ?: "BLE OBD-II"} • $protocolName"

      refreshVehicleInfoAndMonitors()
      scanTroubleCodes()
      startLivePollingLoop()
    }
  }

  @SuppressLint("MissingPermission")
  private suspend fun sendElmCommand(command: String, timeoutMs: Long = 1500L): String {
    return commandMutex.withLock {
      val gatt = bluetoothGatt ?: return@withLock ""
      val writeChar = writeCharacteristic ?: return@withLock ""

      val deferred = CompletableDeferred<String>()
      synchronized(responseBuffer) {
        responseBuffer.clear()
        pendingResponse = deferred
      }

      val payload = "$command\r".toByteArray(Charsets.US_ASCII)
      val writeType = if ((writeChar.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
        BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
      } else {
        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
      }

      val wrote = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          gatt.writeCharacteristic(writeChar, payload, writeType) == BluetoothGatt.GATT_SUCCESS
        } else {
          @Suppress("DEPRECATION")
          writeChar.writeType = writeType
          @Suppress("DEPRECATION")
          writeChar.value = payload
          @Suppress("DEPRECATION")
          gatt.writeCharacteristic(writeChar)
        }
      } catch (_: Exception) {
        false
      }

      if (!wrote) {
        return@withLock ""
      }

      withTimeoutOrNull(timeoutMs) {
        deferred.await()
      } ?: ""
    }
  }

  /**
   * Connects to the built-in Demo Vehicle Simulator so the user can test live telemetry,
   * trouble code scanning/clearing, and readiness monitors without a physical car.
   */
  fun startSimulatorMode() {
    stopBleScan()
    disconnectInternal(resetMessage = false)

    _connectionState.value = Obd2ConnectionState.CONNECTED_SIMULATOR
    _connectedDeviceName.value = "Demo Vehicle (BLE Simulator)"
    _statusMessage.value = "Connected • Demo Vehicle (ISO 15765-4 CAN 11b/500k)"

    val demoVin = "1HGCM82633A004352"
    val (region, year) = Obd2Parser.decodeVinMetadata(demoVin)
    _vehicleInfo.value = Obd2VehicleInfo(
      vin = demoVin,
      calibrationId = "37805-RAA-A530",
      ecuName = "ECM-EngineControl",
      protocolName = "ISO 15765-4 CAN (11 bit ID, 500 kbaud)",
      elmVersion = "ELM327 v2.2 BLE (Sim)",
      modelYearHint = year,
      regionHint = region,
    )

    // Populate realistic initial trouble codes & monitor status
    _activeDtcs.value = listOf(
      Obd2DtcCatalog.lookup("P0420", DtcStatus.STORED),
      Obd2DtcCatalog.lookup("P0171", DtcStatus.STORED),
      Obd2DtcCatalog.lookup("P0301", DtcStatus.PENDING),
    )
    _monitorSummary.value = Obd2Parser.parseMonitorStatus("41 01 82 07 E5 04") ?: Obd2MonitorSummary()

    startLivePollingLoop()
  }

  fun toggleLivePolling() {
    _isPollingLive.update { !it }
  }

  fun resetTelemetryPeaks() {
    _liveTelemetry.update { current ->
      current.copy(
        maxRpm = current.rpm,
        maxSpeedKmh = current.speedKmh,
        minCoolantTempC = current.coolantTempC,
        maxCoolantTempC = current.coolantTempC,
        rpmHistory = listOf(current.rpm),
        speedHistory = listOf(current.speedKmh),
      )
    }
  }

  private fun startLivePollingLoop() {
    pollingJob?.cancel()
    pollingJob = scope.launch {
      while (isActive) {
        if (!_isPollingLive.value) {
          delay(250L)
          continue
        }
        when (_connectionState.value) {
          Obd2ConnectionState.CONNECTED_SIMULATOR -> {
            stepSimulatorTelemetry()
            delay(350L)
          }
          Obd2ConnectionState.CONNECTED_BLE -> {
            pollBleLiveTelemetry()
            delay(200L)
          }
          else -> break
        }
      }
    }
  }

  private fun stepSimulatorTelemetry() {
    simTick++
    val phase = simTick * 0.14
    val drivingWave = (sin(phase) + 1.0) * 0.5 // 0.0 .. 1.0
    val secondaryWave = cos(phase * 0.6)

    val rpm = (780f + (drivingWave * 2850f) + (secondaryWave * 180f)).toFloat().coerceIn(720f, 6500f)
    val speedKmh = (drivingWave * 108f + secondaryWave * 6f).toFloat().coerceIn(0f, 220f)
    val coolantC = (88f + sin(phase * 0.15) * 4f).toFloat()
    val loadPct = (18f + drivingWave * 64f).toFloat().coerceIn(10f, 96f)
    val throttlePct = (12f + drivingWave * 52f).toFloat().coerceIn(8f, 92f)
    val voltage = (14.1f + (sin(phase * 0.5) * 0.15f)).toFloat()
    val stft = (sin(phase * 0.9) * 3.8).toFloat()
    val ltft = 4.7f
    val intakeC = (29f + cos(phase * 0.2) * 3f).toFloat()
    val maf = (2.8f + drivingWave * 38.5f).toFloat()

    _liveTelemetry.update { prev ->
      val maxRpm = maxOf(prev.maxRpm, rpm)
      val maxSpeed = maxOf(prev.maxSpeedKmh, speedKmh)
      val minCoolant = if (prev.rpmHistory.isEmpty()) coolantC else minOf(prev.minCoolantTempC, coolantC)
      val maxCoolant = if (prev.rpmHistory.isEmpty()) coolantC else maxOf(prev.maxCoolantTempC, coolantC)
      Obd2LiveTelemetry(
        rpm = rpm,
        maxRpm = maxRpm,
        speedKmh = speedKmh,
        maxSpeedKmh = maxSpeed,
        coolantTempC = coolantC,
        minCoolantTempC = minCoolant,
        maxCoolantTempC = maxCoolant,
        engineLoadPct = loadPct,
        throttlePct = throttlePct,
        batteryVoltage = voltage,
        shortTermFuelTrimPct = stft,
        longTermFuelTrimPct = ltft,
        intakeAirTempC = intakeC,
        mafGramsPerSec = maf,
        rpmHistory = (prev.rpmHistory + rpm).takeLast(32),
        speedHistory = (prev.speedHistory + speedKmh).takeLast(32),
      )
    }
  }

  private suspend fun pollBleLiveTelemetry() {
    val rpm = Obd2Parser.parseRpm(sendElmCommand("010C"))
    val speed = Obd2Parser.parseSpeedKmh(sendElmCommand("010D"))
    val coolant = Obd2Parser.parseCoolantTempC(sendElmCommand("0105"))
    val load = Obd2Parser.parseEngineLoadPct(sendElmCommand("0104"))
    val throttle = Obd2Parser.parseThrottlePct(sendElmCommand("0111"))
    val voltage = Obd2Parser.parseBatteryVoltage(sendElmCommand("ATRV"))
    val stft = Obd2Parser.parseFuelTrimPct(sendElmCommand("0106"), "06")
    val ltft = Obd2Parser.parseFuelTrimPct(sendElmCommand("0107"), "07")
    val iat = Obd2Parser.parseIntakeAirTempC(sendElmCommand("010F"))
    val maf = Obd2Parser.parseMafGramsPerSec(sendElmCommand("0110"))

    _liveTelemetry.update { prev ->
      val nextRpm = rpm ?: prev.rpm
      val nextSpeed = speed ?: prev.speedKmh
      val nextCoolant = coolant ?: prev.coolantTempC
      prev.copy(
        rpm = nextRpm,
        maxRpm = maxOf(prev.maxRpm, nextRpm),
        speedKmh = nextSpeed,
        maxSpeedKmh = maxOf(prev.maxSpeedKmh, nextSpeed),
        coolantTempC = nextCoolant,
        minCoolantTempC = minOf(prev.minCoolantTempC, nextCoolant),
        maxCoolantTempC = maxOf(prev.maxCoolantTempC, nextCoolant),
        engineLoadPct = load ?: prev.engineLoadPct,
        throttlePct = throttle ?: prev.throttlePct,
        batteryVoltage = voltage ?: prev.batteryVoltage,
        shortTermFuelTrimPct = stft ?: prev.shortTermFuelTrimPct,
        longTermFuelTrimPct = ltft ?: prev.longTermFuelTrimPct,
        intakeAirTempC = iat ?: prev.intakeAirTempC,
        mafGramsPerSec = maf ?: prev.mafGramsPerSec,
        rpmHistory = (prev.rpmHistory + nextRpm).takeLast(32),
        speedHistory = (prev.speedHistory + nextSpeed).takeLast(32),
      )
    }
  }

  /**
   * Scans Stored (`Mode 03`) and Pending (`Mode 07`) Diagnostic Trouble Codes.
   */
  fun scanTroubleCodes() {
    scope.launch {
      when (_connectionState.value) {
        Obd2ConnectionState.CONNECTED_SIMULATOR -> {
          if (_activeDtcs.value.isEmpty()) {
            _activeDtcs.value = listOf(
              Obd2DtcCatalog.lookup("P0420", DtcStatus.STORED),
              Obd2DtcCatalog.lookup("P0171", DtcStatus.STORED),
              Obd2DtcCatalog.lookup("P0301", DtcStatus.PENDING),
            )
            _monitorSummary.value = Obd2Parser.parseMonitorStatus("41 01 82 07 E5 04") ?: Obd2MonitorSummary()
          }
          _statusMessage.value = "DTC scan complete • ${_activeDtcs.value.size} trouble code(s) found"
        }
        Obd2ConnectionState.CONNECTED_BLE -> {
          _statusMessage.value = "Reading Stored (Mode 03) & Pending (Mode 07) DTCs..."
          val storedRaw = sendElmCommand("03", timeoutMs = 2500L)
          val pendingRaw = sendElmCommand("07", timeoutMs = 2500L)
          val stored = Obd2Parser.parseDtcResponse(storedRaw, "43", DtcStatus.STORED)
          val pending = Obd2Parser.parseDtcResponse(pendingRaw, "47", DtcStatus.PENDING)
            .filter { p -> stored.none { it.code == p.code } }
          val combined = stored + pending
          _activeDtcs.value = combined
          _statusMessage.value = "DTC scan complete • ${combined.size} trouble code(s) found"
          refreshVehicleInfoAndMonitors()
        }
        else -> {
          _statusMessage.value = "Connect to a BLE OBD-II adapter or Demo Vehicle first"
        }
      }
    }
  }

  /**
   * Sends Mode 04 (`04`) to clear Diagnostic Trouble Codes and turn off the Check Engine Light (MIL).
   */
  fun clearTroubleCodes() {
    scope.launch {
      when (_connectionState.value) {
        Obd2ConnectionState.CONNECTED_SIMULATOR -> {
          _activeDtcs.value = emptyList()
          // After Mode 04, MIL turns off (0x00) and non-continuous monitors reset
          _monitorSummary.value = Obd2Parser.parseMonitorStatus("41 01 00 07 E5 00") ?: Obd2MonitorSummary()
          _statusMessage.value = "Cleared trouble codes & reset MIL (Mode 04)"
        }
        Obd2ConnectionState.CONNECTED_BLE -> {
          sendElmCommand("04", timeoutMs = 2500L)
          _activeDtcs.value = emptyList()
          _statusMessage.value = "Cleared trouble codes & reset MIL (Mode 04)"
          refreshVehicleInfoAndMonitors()
        }
        else -> {
          _activeDtcs.value = emptyList()
        }
      }
    }
  }

  /**
   * Queries Mode 01 PID 01 (MIL & Readiness Monitors) and Mode 09 PID 02 (VIN).
   */
  fun refreshVehicleInfoAndMonitors() {
    scope.launch {
      if (_connectionState.value == Obd2ConnectionState.CONNECTED_BLE) {
        val monRaw = sendElmCommand("0101", timeoutMs = 2000L)
        Obd2Parser.parseMonitorStatus(monRaw)?.let { _monitorSummary.value = it }

        val vinRaw = sendElmCommand("0902", timeoutMs = 3000L)
        Obd2Parser.parseVin(vinRaw)?.let { vin ->
          val (region, year) = Obd2Parser.decodeVinMetadata(vin)
          _vehicleInfo.update {
            it.copy(
              vin = vin,
              regionHint = region,
              modelYearHint = year,
            )
          }
        }
      } else if (_connectionState.value == Obd2ConnectionState.CONNECTED_SIMULATOR) {
        _statusMessage.value = "Refreshed vehicle info & readiness monitors"
      }
    }
  }

  fun disconnect() {
    stopBleScan()
    disconnectInternal(resetMessage = true)
  }

  @SuppressLint("MissingPermission")
  private fun disconnectInternal(resetMessage: Boolean) {
    pollingJob?.cancel()
    pollingJob = null
    try {
      bluetoothGatt?.disconnect()
      bluetoothGatt?.close()
    } catch (_: Exception) {
    }
    bluetoothGatt = null
    writeCharacteristic = null
    notifyCharacteristic = null
    _connectingDevice.value = null
    _connectedDeviceName.value = null
    _connectionState.value = Obd2ConnectionState.DISCONNECTED
    if (resetMessage) {
      _statusMessage.value = "Select a paired adapter or tap + to scan"
    }
  }
}
