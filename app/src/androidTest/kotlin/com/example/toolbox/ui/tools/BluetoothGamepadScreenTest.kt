package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Surface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.example.toolbox.ui.theme.ToolboxTheme
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class BluetoothGamepadScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun connectionScreen_reusesDeviceListFromBluetoothRemote() {
    var scanStarted = false
    val pairedTv = BtDeviceInfo("AA:BB:CC:DD:EE:01", "Living Room TV", isBonded = true, kind = BtDeviceKind.TV)
    val state = BluetoothRemoteUiState(
      isBluetoothEnabled = true,
      isHidRegistered = true,
      devices = listOf(pairedTv),
    )

    composeTestRule.setContent {
      ToolboxTheme {
        Surface {
          DeviceListContent(
            state = state,
            hasPermissions = true,
            onRequestPermissions = {},
            onEnableBluetooth = {},
            onMakeDiscoverable = {},
            onScan = { scanStarted = true },
            onStopScan = {},
            onConnect = {},
            onCancelConnect = {},
            onDismissMessage = {},
            title = "Bluetooth Gamepad",
            icon = Icons.Default.SportsEsports,
          )
        }
      }
    }

    composeTestRule.onNodeWithTag("bt_device_list").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bluetooth Gamepad").assertIsDisplayed()
    composeTestRule.onNodeWithText("Living Room TV").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_add_device_fab").assertIsDisplayed()

    composeTestRule.onNodeWithTag("bt_add_device_fab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Add new device").assertIsDisplayed()
    assertTrue(scanStarted)
  }

  @Test
  fun gamepadControl_displaysAndTriggersAllButtonsBumpersTriggersAndStartSelect() {
    val pressedButtons = mutableListOf<GamepadButton>()
    val releasedButtons = mutableListOf<GamepadButton>()
    val testActions = object : GamepadInputActions {
      override fun setButton(button: GamepadButton, pressed: Boolean) {
        if (pressed) pressedButtons.add(button) else releasedButtons.add(button)
      }
      override fun setLeftJoystick(x: Float, y: Float) = Unit
      override fun setRightJoystick(x: Float, y: Float) = Unit
    }

    composeTestRule.setContent {
      ToolboxTheme {
        Surface {
          GamepadControlContent(
            deviceName = "Living Room TV",
            actions = testActions,
            onDisconnect = {},
          )
        }
      }
    }

    // Verify Triggers (LT / RT), Bumpers (LB / RB), Minus/Select, Plus/Start, and A/B/X/Y
    composeTestRule.onNodeWithTag("bt_gamepad_lt").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_rt").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_lb").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_rb").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_minus").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_plus").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_a").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_b").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_x").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_y").assertIsDisplayed().performClick()

    assertTrue(pressedButtons.contains(GamepadButton.LEFT_TRIGGER))
    assertTrue(pressedButtons.contains(GamepadButton.RIGHT_TRIGGER))
    assertTrue(pressedButtons.contains(GamepadButton.LEFT_BUMPER))
    assertTrue(pressedButtons.contains(GamepadButton.RIGHT_BUMPER))
    assertTrue(pressedButtons.contains(GamepadButton.MINUS))
    assertTrue(pressedButtons.contains(GamepadButton.PLUS))
    assertTrue(pressedButtons.contains(GamepadButton.A))
    assertTrue(pressedButtons.contains(GamepadButton.B))
    assertTrue(pressedButtons.contains(GamepadButton.X))
    assertTrue(pressedButtons.contains(GamepadButton.Y))
  }

  @Test
  fun gamepadControl_leftDpadAndBothJoysticksTriggerActionsAndArePositionedCorrectly() {
    val pressedButtons = mutableListOf<GamepadButton>()
    val leftStickMoves = mutableListOf<Pair<Float, Float>>()
    val rightStickMoves = mutableListOf<Pair<Float, Float>>()
    val testActions = object : GamepadInputActions {
      override fun setButton(button: GamepadButton, pressed: Boolean) {
        if (pressed) pressedButtons.add(button)
      }
      override fun setLeftJoystick(x: Float, y: Float) {
        leftStickMoves.add(x to y)
      }
      override fun setRightJoystick(x: Float, y: Float) {
        rightStickMoves.add(x to y)
      }
    }

    composeTestRule.setContent {
      ToolboxTheme {
        Surface {
          GamepadControlContent(
            deviceName = "Living Room TV",
            actions = testActions,
            onDisconnect = {},
          )
        }
      }
    }

    val dpadNode = composeTestRule.onNodeWithTag("bt_gamepad_dpad").assertIsDisplayed()
    val abxyNode = composeTestRule.onNodeWithTag("bt_gamepad_abxy").assertIsDisplayed()
    val leftJoystickNode = composeTestRule.onNodeWithTag("bt_gamepad_left_joystick").assertIsDisplayed()
    val rightJoystickNode = composeTestRule.onNodeWithTag("bt_gamepad_right_joystick").assertIsDisplayed()

    // Ensure D-pad and left joystick are on the left side relative to ABXY and right joystick
    val dpadLeft = dpadNode.fetchSemanticsNode().boundsInRoot.left
    val abxyLeft = abxyNode.fetchSemanticsNode().boundsInRoot.left
    val leftJoyLeft = leftJoystickNode.fetchSemanticsNode().boundsInRoot.left
    val rightJoyLeft = rightJoystickNode.fetchSemanticsNode().boundsInRoot.left
    assertTrue(dpadLeft < abxyLeft)
    assertTrue(leftJoyLeft < rightJoyLeft)

    // Test D-pad directions
    composeTestRule.onNodeWithTag("bt_gamepad_dpad_up").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_dpad_down").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_dpad_left").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("bt_gamepad_dpad_right").assertIsDisplayed().performClick()

    assertTrue(pressedButtons.contains(GamepadButton.DPAD_UP))
    assertTrue(pressedButtons.contains(GamepadButton.DPAD_DOWN))
    assertTrue(pressedButtons.contains(GamepadButton.DPAD_LEFT))
    assertTrue(pressedButtons.contains(GamepadButton.DPAD_RIGHT))

    // Test Left and Right Joysticks
    leftJoystickNode.performTouchInput {
      click(Offset(width - 10f, height / 2f))
    }
    rightJoystickNode.performTouchInput {
      click(Offset(10f, height - 10f))
    }

    assertTrue(leftStickMoves.any { it.first > 0.5f })
    assertTrue(leftStickMoves.last() == (0f to 0f))
    assertTrue(rightStickMoves.any { it.first < -0.3f && it.second > 0.3f })
    assertTrue(rightStickMoves.last() == (0f to 0f))
  }
}
