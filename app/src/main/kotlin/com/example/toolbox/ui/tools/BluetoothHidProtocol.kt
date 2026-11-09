package com.example.toolbox.ui.tools

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Pure (Android-free) HID protocol helpers used by the Bluetooth Remote tool.
 *
 * The phone registers itself as a composite HID device exposing three reports:
 *  - [REPORT_ID_KEYBOARD]: a standard 6-key-rollover boot keyboard
 *  - [REPORT_ID_CONSUMER]: a consumer-control (media / TV remote) usage
 *  - [REPORT_ID_MOUSE]: a 3-button relative mouse with a vertical wheel
 *
 * Android TV maps these to KeyEvents (D-pad, Home, Back, media keys) and shows a
 * pointer cursor when relative mouse reports arrive.
 */
object HidProtocol {
  const val REPORT_ID_KEYBOARD: Int = 1
  const val REPORT_ID_CONSUMER: Int = 2
  const val REPORT_ID_MOUSE: Int = 3

  const val KEYBOARD_REPORT_SIZE: Int = 8
  const val CONSUMER_REPORT_SIZE: Int = 2
  const val MOUSE_REPORT_SIZE: Int = 4

  const val MODIFIER_LEFT_SHIFT: Int = 0x02

  const val MOUSE_BUTTON_LEFT: Int = 0x01
  const val MOUSE_BUTTON_RIGHT: Int = 0x02
  const val MOUSE_BUTTON_MIDDLE: Int = 0x04

  /** Combined HID report descriptor: keyboard + consumer control + mouse. */
  val REPORT_DESCRIPTOR: ByteArray = intArrayOf(
    // ---- Keyboard (Report ID 1) ----
    0x05, 0x01, // Usage Page (Generic Desktop)
    0x09, 0x06, // Usage (Keyboard)
    0xA1, 0x01, // Collection (Application)
    0x85, REPORT_ID_KEYBOARD, //   Report ID (1)
    0x05, 0x07, //   Usage Page (Keyboard/Keypad)
    0x19, 0xE0, //   Usage Minimum (Left Control)
    0x29, 0xE7, //   Usage Maximum (Right GUI)
    0x15, 0x00, //   Logical Minimum (0)
    0x25, 0x01, //   Logical Maximum (1)
    0x75, 0x01, //   Report Size (1)
    0x95, 0x08, //   Report Count (8)
    0x81, 0x02, //   Input (Data, Variable, Absolute) -> modifier byte
    0x95, 0x01, //   Report Count (1)
    0x75, 0x08, //   Report Size (8)
    0x81, 0x01, //   Input (Constant) -> reserved byte
    0x95, 0x06, //   Report Count (6)
    0x75, 0x08, //   Report Size (8)
    0x15, 0x00, //   Logical Minimum (0)
    0x25, 0x65, //   Logical Maximum (101)
    0x05, 0x07, //   Usage Page (Keyboard/Keypad)
    0x19, 0x00, //   Usage Minimum (0)
    0x29, 0x65, //   Usage Maximum (101)
    0x81, 0x00, //   Input (Data, Array) -> 6 key slots
    0xC0, // End Collection

    // ---- Consumer Control (Report ID 2) ----
    0x05, 0x0C, // Usage Page (Consumer)
    0x09, 0x01, // Usage (Consumer Control)
    0xA1, 0x01, // Collection (Application)
    0x85, REPORT_ID_CONSUMER, //   Report ID (2)
    0x15, 0x00, //   Logical Minimum (0)
    0x26, 0xFF, 0x03, //   Logical Maximum (0x03FF)
    0x19, 0x00, //   Usage Minimum (0)
    0x2A, 0xFF, 0x03, //   Usage Maximum (0x03FF)
    0x75, 0x10, //   Report Size (16)
    0x95, 0x01, //   Report Count (1)
    0x81, 0x00, //   Input (Data, Array)
    0xC0, // End Collection

    // ---- Mouse (Report ID 3) ----
    0x05, 0x01, // Usage Page (Generic Desktop)
    0x09, 0x02, // Usage (Mouse)
    0xA1, 0x01, // Collection (Application)
    0x85, REPORT_ID_MOUSE, //   Report ID (3)
    0x09, 0x01, //   Usage (Pointer)
    0xA1, 0x00, //   Collection (Physical)
    0x05, 0x09, //     Usage Page (Buttons)
    0x19, 0x01, //     Usage Minimum (1)
    0x29, 0x03, //     Usage Maximum (3)
    0x15, 0x00, //     Logical Minimum (0)
    0x25, 0x01, //     Logical Maximum (1)
    0x95, 0x03, //     Report Count (3)
    0x75, 0x01, //     Report Size (1)
    0x81, 0x02, //     Input (Data, Variable, Absolute)
    0x95, 0x01, //     Report Count (1)
    0x75, 0x05, //     Report Size (5)
    0x81, 0x03, //     Input (Constant) -> padding
    0x05, 0x01, //     Usage Page (Generic Desktop)
    0x09, 0x30, //     Usage (X)
    0x09, 0x31, //     Usage (Y)
    0x09, 0x38, //     Usage (Wheel)
    0x15, 0x81, //     Logical Minimum (-127)
    0x25, 0x7F, //     Logical Maximum (127)
    0x75, 0x08, //     Report Size (8)
    0x95, 0x03, //     Report Count (3)
    0x81, 0x06, //     Input (Data, Variable, Relative)
    0xC0, //   End Collection
    0xC0, // End Collection
  ).map { it.toByte() }.toByteArray()

  fun keyboardReport(modifiers: Int, keyCode: Int): ByteArray =
    ByteArray(KEYBOARD_REPORT_SIZE).also {
      it[0] = modifiers.toByte()
      it[2] = keyCode.toByte()
    }

  fun emptyKeyboardReport(): ByteArray = ByteArray(KEYBOARD_REPORT_SIZE)

  fun consumerReport(usage: Int): ByteArray = byteArrayOf(
    (usage and 0xFF).toByte(),
    ((usage shr 8) and 0xFF).toByte(),
  )

  fun mouseReport(buttons: Int, dx: Int, dy: Int, wheel: Int = 0): ByteArray = byteArrayOf(
    (buttons and 0x07).toByte(),
    dx.coerceIn(-127, 127).toByte(),
    dy.coerceIn(-127, 127).toByte(),
    wheel.coerceIn(-127, 127).toByte(),
  )

  /**
   * Splits an arbitrarily large relative movement into a list of (dx, dy) steps that each
   * fit within the signed 8-bit range of a single mouse report.
   */
  fun splitMouseMovement(dx: Int, dy: Int): List<Pair<Int, Int>> {
    if (dx == 0 && dy == 0) return emptyList()
    val steps = maxOf((abs(dx) + 126) / 127, (abs(dy) + 126) / 127, 1)
    val result = ArrayList<Pair<Int, Int>>(steps)
    var sentX = 0
    var sentY = 0
    for (i in 1..steps) {
      val targetX = (dx.toDouble() * i / steps).roundToInt()
      val targetY = (dy.toDouble() * i / steps).roundToInt()
      result += (targetX - sentX) to (targetY - sentY)
      sentX = targetX
      sentY = targetY
    }
    return result
  }
}

/** USB HID keyboard usage IDs (Usage Page 0x07) used by the remote. */
object HidKey {
  const val ENTER: Int = 0x28
  const val ESCAPE: Int = 0x29
  const val BACKSPACE: Int = 0x2A
  const val TAB: Int = 0x2B
  const val SPACE: Int = 0x2C
  const val RIGHT: Int = 0x4F
  const val LEFT: Int = 0x50
  const val DOWN: Int = 0x51
  const val UP: Int = 0x52
}

/** HID Consumer page (0x0C) usages understood by Android TV. */
enum class ConsumerKey(val usage: Int) {
  POWER(0x0030),
  MENU(0x0040),
  CHANNEL_UP(0x009C),
  CHANNEL_DOWN(0x009D),
  FAST_FORWARD(0x00B3),
  REWIND(0x00B4),
  NEXT_TRACK(0x00B5),
  PREVIOUS_TRACK(0x00B6),
  STOP(0x00B7),
  PLAY_PAUSE(0x00CD),
  VOICE_ASSIST(0x00CF),
  MUTE(0x00E2),
  VOLUME_UP(0x00E9),
  VOLUME_DOWN(0x00EA),
  SEARCH(0x0221),
  HOME(0x0223),
  BACK(0x0224),
}

/** A keyboard key press: HID usage + modifier mask. */
data class HidKeyStroke(val keyCode: Int, val modifiers: Int = 0)

/** Maps text characters to US-layout HID key strokes. */
object HidKeyMap {
  private const val SHIFT = HidProtocol.MODIFIER_LEFT_SHIFT

  private val unshiftedSymbols: Map<Char, Int> = mapOf(
    '-' to 0x2D, '=' to 0x2E, '[' to 0x2F, ']' to 0x30, '\\' to 0x31,
    ';' to 0x33, '\'' to 0x34, '`' to 0x35, ',' to 0x36, '.' to 0x37, '/' to 0x38,
  )

  private val shiftedSymbols: Map<Char, Int> = mapOf(
    '!' to 0x1E, '@' to 0x1F, '#' to 0x20, '$' to 0x21, '%' to 0x22,
    '^' to 0x23, '&' to 0x24, '*' to 0x25, '(' to 0x26, ')' to 0x27,
    '_' to 0x2D, '+' to 0x2E, '{' to 0x2F, '}' to 0x30, '|' to 0x31,
    ':' to 0x33, '"' to 0x34, '~' to 0x35, '<' to 0x36, '>' to 0x37, '?' to 0x38,
  )

  /** Returns the key stroke for [c], or null if the character can't be typed on a US keyboard. */
  fun strokeFor(c: Char): HidKeyStroke? = when (c) {
    in 'a'..'z' -> HidKeyStroke(0x04 + (c - 'a'))
    in 'A'..'Z' -> HidKeyStroke(0x04 + (c - 'A'), SHIFT)
    in '1'..'9' -> HidKeyStroke(0x1E + (c - '1'))
    '0' -> HidKeyStroke(0x27)
    ' ' -> HidKeyStroke(HidKey.SPACE)
    '\n' -> HidKeyStroke(HidKey.ENTER)
    '\t' -> HidKeyStroke(HidKey.TAB)
    else -> unshiftedSymbols[c]?.let { HidKeyStroke(it) }
      ?: shiftedSymbols[c]?.let { HidKeyStroke(it, SHIFT) }
  }

  /**
   * Computes the minimal edit that turns [old] into [new] for "live typing":
   * the number of backspaces to send followed by the text to type.
   */
  fun diff(old: String, new: String): Pair<Int, String> {
    var prefix = 0
    val max = minOf(old.length, new.length)
    while (prefix < max && old[prefix] == new[prefix]) prefix++
    return (old.length - prefix) to new.substring(prefix)
  }
}

/** Converts gyroscope angular velocity into fractional cursor deltas. */
object GyroMouseMath {
  /** Angular velocity (rad/s) below which motion is ignored to suppress drift and hand tremor. */
  const val DEAD_ZONE_RAD_PER_SEC: Float = 0.03f

  /** Base cursor speed in pixels per radian of rotation, scaled by the sensitivity multiplier. */
  const val PIXELS_PER_RADIAN: Float = 900f

  /**
   * Rotation rate around the world's vertical axis, regardless of how the phone is held.
   *
   * Projects the device-frame angular velocity onto the normalised "up" vector reported by the
   * gravity / accelerometer sensor. Positive values mean turning left (counter-clockwise from above).
   * Falls back to the device Z axis (phone held flat) when no gravity reading is available.
   */
  fun yawRate(
    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    upX: Float,
    upY: Float,
    upZ: Float,
  ): Float {
    val norm = sqrt(upX * upX + upY * upY + upZ * upZ)
    if (norm < 1e-3f) return gyroZ
    return (gyroX * upX + gyroY * upY + gyroZ * upZ) / norm
  }

  /**
   * Maps rotation rates (rad/s) over [dtSeconds] into cursor deltas.
   * Turning the phone left/right ([yawRate]) moves X; tilting the top up/down
   * ([pitchRate], rotation around the device X axis) moves Y.
   */
  fun delta(yawRate: Float, pitchRate: Float, dtSeconds: Float, sensitivity: Float): Pair<Float, Float> {
    val yaw = if (abs(yawRate) < DEAD_ZONE_RAD_PER_SEC) 0f else yawRate
    val pitch = if (abs(pitchRate) < DEAD_ZONE_RAD_PER_SEC) 0f else pitchRate
    val scale = PIXELS_PER_RADIAN * sensitivity * dtSeconds
    return (0f - yaw * scale) to (0f - pitch * scale)
  }
}
