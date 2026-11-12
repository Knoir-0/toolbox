package com.example.toolbox.ui.tools

import androidx.compose.ui.geometry.Offset
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class BluetoothHidProtocolTest {

  @Test
  fun keyMap_mapsLettersDigitsAndShiftedSymbols() {
    assertEquals(HidKeyStroke(0x04), HidKeyMap.strokeFor('a'))
    assertEquals(HidKeyStroke(0x1D, HidProtocol.MODIFIER_LEFT_SHIFT), HidKeyMap.strokeFor('Z'))
    assertEquals(HidKeyStroke(0x1E), HidKeyMap.strokeFor('1'))
    assertEquals(HidKeyStroke(0x27), HidKeyMap.strokeFor('0'))
    assertEquals(HidKeyStroke(HidKey.SPACE), HidKeyMap.strokeFor(' '))
    assertEquals(HidKeyStroke(0x1F, HidProtocol.MODIFIER_LEFT_SHIFT), HidKeyMap.strokeFor('@'))
    assertEquals(HidKeyStroke(0x37), HidKeyMap.strokeFor('.'))
    assertEquals(HidKeyStroke(0x38, HidProtocol.MODIFIER_LEFT_SHIFT), HidKeyMap.strokeFor('?'))
    assertNull(HidKeyMap.strokeFor('é'))
  }

  @Test
  fun keyMap_diffComputesBackspacesAndInsertion() {
    assertEquals(0 to "c", HidKeyMap.diff("ab", "abc"))
    assertEquals(1 to "", HidKeyMap.diff("abc", "ab"))
    // Autocorrect replacing a word: delete the divergent tail and retype it.
    assertEquals(2 to "he", HidKeyMap.diff("teh", "the"))
    assertEquals(0 to "", HidKeyMap.diff("same", "same"))
  }

  @Test
  fun reports_haveExpectedLayout() {
    val kb = HidProtocol.keyboardReport(HidProtocol.MODIFIER_LEFT_SHIFT, 0x04)
    assertEquals(HidProtocol.KEYBOARD_REPORT_SIZE, kb.size)
    assertEquals(0x02.toByte(), kb[0])
    assertEquals(0x04.toByte(), kb[2])

    val consumer = HidProtocol.consumerReport(ConsumerKey.HOME.usage)
    assertEquals(0x23.toByte(), consumer[0])
    assertEquals(0x02.toByte(), consumer[1])

    val dpadCenter = HidProtocol.consumerReport(ConsumerKey.DPAD_CENTER.usage)
    assertEquals(0x41.toByte(), dpadCenter[0])
    assertEquals(0x00.toByte(), dpadCenter[1])
    assertEquals(0x0042, ConsumerKey.DPAD_UP.usage)
    assertEquals(0x0043, ConsumerKey.DPAD_DOWN.usage)
    assertEquals(0x0044, ConsumerKey.DPAD_LEFT.usage)
    assertEquals(0x0045, ConsumerKey.DPAD_RIGHT.usage)

    val mouse = HidProtocol.mouseReport(HidProtocol.MOUSE_BUTTON_LEFT, 300, -300, 1)
    assertEquals(1.toByte(), mouse[0])
    assertEquals(127.toByte(), mouse[1])
    assertEquals((-127).toByte(), mouse[2])
    assertEquals(1.toByte(), mouse[3])
  }

  @Test
  fun splitMouseMovement_preservesTotalAndFitsInSignedByte() {
    val steps = HidProtocol.splitMouseMovement(400, -50)
    assertEquals(400, steps.sumOf { it.first })
    assertEquals(-50, steps.sumOf { it.second })
    assertTrue(steps.all { it.first in -127..127 && it.second in -127..127 })
    assertTrue(HidProtocol.splitMouseMovement(0, 0).isEmpty())
  }

  @Test
  fun descriptor_collectionsAreBalanced() {
    val bytes = HidProtocol.REPORT_DESCRIPTOR.map { it.toInt() and 0xFF }
    var i = 0
    var depth = 0
    while (i < bytes.size) {
      val prefix = bytes[i]
      val size = when (prefix and 0x03) { 3 -> 4; else -> prefix and 0x03 }
      when (prefix and 0xFC) {
        0xA0 -> depth++
        0xC0 -> depth--
      }
      i += 1 + size
    }
    assertEquals(bytes.size, i)
    assertEquals(0, depth)
  }

  @Test
  fun gyroMath_usesGravityToFindYawAndAppliesDeadZone() {
    // Phone flat: up = +Z, so yaw is the Z rotation.
    assertEquals(1f, GyroMouseMath.yawRate(0f, 0f, 1f, 0f, 0f, 9.8f), 1e-4f)
    // Phone upright: up = +Y, so yaw is the Y rotation.
    assertEquals(0.5f, GyroMouseMath.yawRate(0f, 0.5f, 0.2f, 0f, 9.8f, 0f), 1e-4f)

    val (dx, dy) = GyroMouseMath.delta(yawRate = 1f, pitchRate = -1f, dtSeconds = 0.01f, sensitivity = 1f)
    assertTrue("turning left moves cursor left", dx < 0f)
    assertTrue("tilting down moves cursor down", dy > 0f)

    val (sx, sy) = GyroMouseMath.delta(0.01f, 0.01f, 0.01f, 1f)
    assertEquals(0f, sx)
    assertEquals(0f, sy)
  }

  @Test
  fun classifyDevice_prefersTvs() {
    assertEquals(BtDeviceKind.TV, BluetoothRemoteViewModel.classifyDevice("Living Room TV", null, null))
    assertEquals(BtDeviceKind.TV, BluetoothRemoteViewModel.classifyDevice("SHIELD", null, null))
    assertEquals(BtDeviceKind.OTHER, BluetoothRemoteViewModel.classifyDevice("Thing", null, null))
  }

  @Test
  fun gamepadReport_encodesButtonsDpadJoysticksAndTriggers() {
    val empty = HidProtocol.emptyGamepadReport()
    assertEquals(HidProtocol.GAMEPAD_REPORT_SIZE, empty.size)
    assertTrue(empty.all { it == 0.toByte() })

    val buttons = GamepadButton.A.mask or
      GamepadButton.B.mask or
      GamepadButton.X.mask or
      GamepadButton.Y.mask or
      GamepadButton.LEFT_BUMPER.mask or
      GamepadButton.RIGHT_BUMPER.mask or
      GamepadButton.LEFT_TRIGGER.mask or
      GamepadButton.RIGHT_TRIGGER.mask or
      GamepadButton.MINUS.mask or
      GamepadButton.PLUS.mask or
      GamepadButton.DPAD_UP.mask or
      GamepadButton.DPAD_RIGHT.mask

    val report = HidProtocol.gamepadReport(
      buttons = buttons,
      leftX = -100,
      leftY = 50,
      rightX = 127,
      rightY = -127,
    )
    assertEquals(HidProtocol.GAMEPAD_REPORT_SIZE, report.size)
    // Low byte: A, B, X, Y, LB, RB, LT, RT = 0xFF
    assertEquals(0xFF.toByte(), report[0])
    // High byte: MINUS (bit 0), PLUS (bit 1), DPAD_UP (bit 4), DPAD_RIGHT (bit 7) = 0x93
    assertEquals(0x93.toByte(), report[1])
    // Hat switch: Up + Right -> North-East (2)
    assertEquals(2.toByte(), report[2])
    // Left & right joystick axes
    assertEquals((-100).toByte(), report[3])
    assertEquals(50.toByte(), report[4])
    assertEquals(127.toByte(), report[5])
    assertEquals((-127).toByte(), report[6])
    // Analog triggers driven by LEFT_TRIGGER / RIGHT_TRIGGER button bits
    assertEquals(255.toByte(), report[7])
    assertEquals(255.toByte(), report[8])
  }

  @Test
  fun hatSwitchForDpad_mapsAllCardinalAndDiagonalDirections() {
    assertEquals(0, HidProtocol.hatSwitchForDpad(up = false, down = false, left = false, right = false))
    assertEquals(1, HidProtocol.hatSwitchForDpad(up = true, down = false, left = false, right = false))
    assertEquals(2, HidProtocol.hatSwitchForDpad(up = true, down = false, left = false, right = true))
    assertEquals(3, HidProtocol.hatSwitchForDpad(up = false, down = false, left = false, right = true))
    assertEquals(4, HidProtocol.hatSwitchForDpad(up = false, down = true, left = false, right = true))
    assertEquals(5, HidProtocol.hatSwitchForDpad(up = false, down = true, left = false, right = false))
    assertEquals(6, HidProtocol.hatSwitchForDpad(up = false, down = true, left = true, right = false))
    assertEquals(7, HidProtocol.hatSwitchForDpad(up = false, down = false, left = true, right = false))
    assertEquals(8, HidProtocol.hatSwitchForDpad(up = true, down = false, left = true, right = false))
  }

  @Test
  fun gamepadDpadButtonsForOffset_mapsAll8Equal45DegreeSectorsAndSupportsRolling() {
    val sizePx = 200f
    val center = Offset(100f, 100f)

    // Center dead-zone returns empty
    assertEquals(emptySet<GamepadButton>(), gamepadDpadButtonsForOffset(center, sizePx))

    // Cardinal directions
    assertEquals(setOf(GamepadButton.DPAD_UP), gamepadDpadButtonsForOffset(Offset(100f, 20f), sizePx))
    assertEquals(setOf(GamepadButton.DPAD_DOWN), gamepadDpadButtonsForOffset(Offset(100f, 180f), sizePx))
    assertEquals(setOf(GamepadButton.DPAD_LEFT), gamepadDpadButtonsForOffset(Offset(20f, 100f), sizePx))
    assertEquals(setOf(GamepadButton.DPAD_RIGHT), gamepadDpadButtonsForOffset(Offset(180f, 100f), sizePx))

    // Diagonal directions (45° corners inside the circle)
    assertEquals(
      setOf(GamepadButton.DPAD_UP, GamepadButton.DPAD_LEFT),
      gamepadDpadButtonsForOffset(Offset(45f, 45f), sizePx),
    )
    assertEquals(
      setOf(GamepadButton.DPAD_UP, GamepadButton.DPAD_RIGHT),
      gamepadDpadButtonsForOffset(Offset(155f, 45f), sizePx),
    )
    assertEquals(
      setOf(GamepadButton.DPAD_DOWN, GamepadButton.DPAD_LEFT),
      gamepadDpadButtonsForOffset(Offset(45f, 155f), sizePx),
    )
    assertEquals(
      setOf(GamepadButton.DPAD_DOWN, GamepadButton.DPAD_RIGHT),
      gamepadDpadButtonsForOffset(Offset(155f, 155f), sizePx),
    )

    // Rolling outside the outer circle boundary stays active when allowOutsideBounds = true
    val outsideDownRight = Offset(220f, 220f)
    assertEquals(
      emptySet<GamepadButton>(),
      gamepadDpadButtonsForOffset(outsideDownRight, sizePx, allowOutsideBounds = false),
    )
    assertEquals(
      setOf(GamepadButton.DPAD_DOWN, GamepadButton.DPAD_RIGHT),
      gamepadDpadButtonsForOffset(outsideDownRight, sizePx, allowOutsideBounds = true),
    )

    // Hysteresis keeps current sector right across the 22.5° boundary (+24° from Right = 0°)
    val angle24Rad = Math.toRadians(24.0)
    val boundaryPoint = Offset(
      100f + (80f * kotlin.math.cos(angle24Rad)).toFloat(),
      100f + (80f * kotlin.math.sin(angle24Rad)).toFloat(),
    )
    assertEquals(
      setOf(GamepadButton.DPAD_DOWN, GamepadButton.DPAD_RIGHT),
      gamepadDpadButtonsForOffset(boundaryPoint, sizePx, previousButtons = emptySet()),
    )
    assertEquals(
      setOf(GamepadButton.DPAD_RIGHT),
      gamepadDpadButtonsForOffset(
        boundaryPoint,
        sizePx,
        allowOutsideBounds = true,
        previousButtons = setOf(GamepadButton.DPAD_RIGHT),
      ),
    )
  }
}
