package com.example.toolbox.ui.tools

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
}
