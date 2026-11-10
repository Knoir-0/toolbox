package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Rule
import org.junit.Test

class BluetoothRemoteScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun deviceList_displaysPairedDevicesAndAddFab() {
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
            onScan = {},
            onStopScan = {},
            onConnect = {},
            onCancelConnect = {},
            onDismissMessage = {},
          )
        }
      }
    }

    composeTestRule.onNodeWithTag("bt_device_list").assertIsDisplayed()
    composeTestRule.onNodeWithText("Living Room TV").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_add_device_fab").assertIsDisplayed()
  }

  @Test
  fun clickingAddFab_opensScanModal() {
    var scanStarted = false
    val state = BluetoothRemoteUiState(
      isBluetoothEnabled = true,
      isHidRegistered = true,
      devices = emptyList(),
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
          )
        }
      }
    }

    // Modal is not visible initially
    composeTestRule.onNodeWithText("Add new device").assertDoesNotExist()

    // Click circular + FAB
    composeTestRule.onNodeWithTag("bt_add_device_fab").performClick()
    composeTestRule.waitForIdle()

    // Modal pops up and starts scanning
    composeTestRule.onNodeWithText("Add new device").assertIsDisplayed()
    composeTestRule.onNodeWithText("Nearby devices").assertIsDisplayed()
    composeTestRule.onNodeWithText("Discoverable").assertIsDisplayed()
    assert(scanStarted)
  }

  @Test
  fun connectingState_displayedInsideIndividualDevice() {
    var cancelClicked = false
    val pairedTv = BtDeviceInfo("AA:BB:CC:DD:EE:01", "Living Room TV", isBonded = true, kind = BtDeviceKind.TV)
    val state = BluetoothRemoteUiState(
      isBluetoothEnabled = true,
      isHidRegistered = true,
      devices = listOf(pairedTv),
      connection = RemoteConnection.Connecting(pairedTv),
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
            onScan = {},
            onStopScan = {},
            onConnect = {},
            onCancelConnect = { cancelClicked = true },
            onDismissMessage = {},
          )
        }
      }
    }

    // Connecting text and Cancel button are displayed inside the device card
    composeTestRule.onNodeWithText("Connecting to Living Room TV…").assertIsDisplayed()
    composeTestRule.onNodeWithText("This can take a few seconds.").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()

    // Assert that the connecting info and cancel action are inside the individual device card
    composeTestRule.onNode(
      hasText("Connecting to Living Room TV…") and hasAnyAncestor(hasTestTag("bt_device_${pairedTv.address}")),
    ).assertIsDisplayed()
    composeTestRule.onNode(
      hasText("Cancel") and hasAnyAncestor(hasTestTag("bt_device_${pairedTv.address}")),
    ).assertIsDisplayed()

    // Clicking cancel triggers onCancelConnect
    composeTestRule.onNodeWithText("Cancel").performClick()
    assert(cancelClicked)
  }

  @Test
  fun errorDisplay_displayedInsideIndividualDevice() {
    var dismissClicked = false
    val pairedTv = BtDeviceInfo("AA:BB:CC:DD:EE:01", "Living Room TV", isBonded = true, kind = BtDeviceKind.TV)
    val state = BluetoothRemoteUiState(
      isBluetoothEnabled = true,
      isHidRegistered = true,
      devices = listOf(pairedTv),
      message = "Failed to connect to Living Room TV.",
      messageDeviceAddress = pairedTv.address,
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
            onScan = {},
            onStopScan = {},
            onConnect = {},
            onCancelConnect = {},
            onDismissMessage = { dismissClicked = true },
          )
        }
      }
    }

    // Error text and dismiss button are displayed inside the device card
    composeTestRule.onNodeWithText("Failed to connect to Living Room TV.").assertIsDisplayed()
    composeTestRule.onNode(
      hasText("Failed to connect to Living Room TV.") and hasAnyAncestor(hasTestTag("bt_device_${pairedTv.address}")),
    ).assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Dismiss").performClick()
    assert(dismissClicked)
  }

  @Test
  fun remoteControl_displaysMediaBarAndVolumeBar() {
    val recordedConsumerDown = mutableListOf<ConsumerKey>()
    val recordedConsumerUp = mutableListOf<Unit>()
    val testActions = object : RemoteInputActions {
      override fun keyDown(keyCode: Int) = Unit
      override fun keyUp() = Unit
      override fun tapKey(keyCode: Int) = Unit
      override fun consumerDown(key: ConsumerKey) { recordedConsumerDown.add(key) }
      override fun consumerUp() { recordedConsumerUp.add(Unit) }
      override fun typeText(text: String, backspaces: Int) = Unit
      override fun moveMouse(dx: Float, dy: Float) = Unit
      override fun scroll(amount: Float) = Unit
      override fun mouseButton(button: Int, pressed: Boolean) = Unit
      override fun mouseClick() = Unit
    }

    composeTestRule.setContent {
      ToolboxTheme {
        Surface {
          RemoteControlContent(
            deviceName = "Living Room TV",
            actions = testActions,
            onDisconnect = {},
          )
        }
      }
    }

    // Media bar buttons are present directly above volume bar and have the correct tags / descriptions
    composeTestRule.onNodeWithTag("bt_media_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_media_rewind").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_media_play_pause").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_media_fast_forward").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Rewind").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Play/Pause").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Fast forward").assertIsDisplayed()

    // Test rewind action
    composeTestRule.onNodeWithTag("bt_media_rewind").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.REWIND))

    // Test play/pause action
    composeTestRule.onNodeWithTag("bt_media_play_pause").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.PLAY_PAUSE))

    // Test fast forward action
    composeTestRule.onNodeWithTag("bt_media_fast_forward").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.FAST_FORWARD))

    // Volume bar buttons are present and have the correct tags / descriptions
    composeTestRule.onNodeWithTag("bt_volume_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_volume_down").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_volume_mute").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_volume_up").assertIsDisplayed()

    // Test volume down action
    composeTestRule.onNodeWithTag("bt_volume_down").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.VOLUME_DOWN))

    // Test mute action
    composeTestRule.onNodeWithTag("bt_volume_mute").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.MUTE))

    // Test volume up action
    composeTestRule.onNodeWithTag("bt_volume_up").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.VOLUME_UP))

    // Channel buttons are removed
    composeTestRule.onNodeWithText("CH").assertDoesNotExist()
    composeTestRule.onNodeWithContentDescription("CH up").assertDoesNotExist()
    composeTestRule.onNodeWithContentDescription("CH down").assertDoesNotExist()

    // Search and voice (Assistant) buttons are removed
    composeTestRule.onNodeWithContentDescription("Search").assertDoesNotExist()
    composeTestRule.onNodeWithContentDescription("Assistant").assertDoesNotExist()
  }

  @Test
  fun remoteControl_dpadButtonsTriggerActions() {
    val recordedConsumerDown = mutableListOf<ConsumerKey>()
    val recordedConsumerUp = mutableListOf<Unit>()
    val testActions = object : RemoteInputActions {
      override fun keyDown(keyCode: Int) = Unit
      override fun keyUp() = Unit
      override fun tapKey(keyCode: Int) = Unit
      override fun consumerDown(key: ConsumerKey) { recordedConsumerDown.add(key) }
      override fun consumerUp() { recordedConsumerUp.add(Unit) }
      override fun typeText(text: String, backspaces: Int) = Unit
      override fun moveMouse(dx: Float, dy: Float) = Unit
      override fun scroll(amount: Float) = Unit
      override fun mouseButton(button: Int, pressed: Boolean) = Unit
      override fun mouseClick() = Unit
    }

    composeTestRule.setContent {
      ToolboxTheme {
        Surface {
          RemoteControlContent(
            deviceName = "Living Room TV",
            actions = testActions,
            onDisconnect = {},
          )
        }
      }
    }

    composeTestRule.onNodeWithTag("bt_dpad").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_dpad_ok").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_dpad_up").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_dpad_down").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_dpad_left").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_dpad_right").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Up").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Down").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Left").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Right").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("OK").assertIsDisplayed()

    composeTestRule.onNodeWithTag("bt_dpad_up").performClick()
    composeTestRule.onNodeWithTag("bt_dpad_down").performClick()
    composeTestRule.onNodeWithTag("bt_dpad_left").performClick()
    composeTestRule.onNodeWithTag("bt_dpad_right").performClick()
    composeTestRule.onNodeWithTag("bt_dpad_ok").performClick()

    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_UP))
    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_DOWN))
    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_LEFT))
    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_RIGHT))
    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_CENTER))
  }

  @Test
  fun remoteControl_dpadTouchSurfaceTriggersAllDirectionsAndCenter() {
    val recordedConsumerDown = mutableListOf<ConsumerKey>()
    val recordedConsumerUp = mutableListOf<Unit>()
    val testActions = object : RemoteInputActions {
      override fun keyDown(keyCode: Int) = Unit
      override fun keyUp() = Unit
      override fun tapKey(keyCode: Int) = Unit
      override fun consumerDown(key: ConsumerKey) { recordedConsumerDown.add(key) }
      override fun consumerUp() { recordedConsumerUp.add(Unit) }
      override fun typeText(text: String, backspaces: Int) = Unit
      override fun moveMouse(dx: Float, dy: Float) = Unit
      override fun scroll(amount: Float) = Unit
      override fun mouseButton(button: Int, pressed: Boolean) = Unit
      override fun mouseClick() = Unit
    }

    composeTestRule.setContent {
      ToolboxTheme {
        Surface {
          RemoteControlContent(
            deviceName = "Living Room TV",
            actions = testActions,
            onDisconnect = {},
          )
        }
      }
    }

    // Tap top quadrant of the DPad wheel -> DPAD_UP
    composeTestRule.onNodeWithTag("bt_dpad").performTouchInput {
      click(Offset(width / 2f, 20f))
    }
    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_UP))

    // Tap bottom quadrant of the DPad wheel -> DPAD_DOWN
    composeTestRule.onNodeWithTag("bt_dpad").performTouchInput {
      click(Offset(width / 2f, height - 20f))
    }
    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_DOWN))

    // Tap left quadrant of the DPad wheel -> DPAD_LEFT
    composeTestRule.onNodeWithTag("bt_dpad").performTouchInput {
      click(Offset(20f, height / 2f))
    }
    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_LEFT))

    // Tap right quadrant of the DPad wheel -> DPAD_RIGHT
    composeTestRule.onNodeWithTag("bt_dpad").performTouchInput {
      click(Offset(width - 20f, height / 2f))
    }
    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_RIGHT))

    // Tap center of the DPad wheel -> DPAD_CENTER / OK
    composeTestRule.onNodeWithTag("bt_dpad").performTouchInput {
      click(Offset(width / 2f, height / 2f))
    }
    assert(recordedConsumerDown.contains(ConsumerKey.DPAD_CENTER))

    // consumerUp should have been called on each touch release
    composeTestRule.waitUntil(timeoutMillis = 1000) {
      recordedConsumerUp.size >= 5
    }
  }

  @Test
  fun remoteControl_powerAndNavigationButtonsTriggerActions() {
    val recordedConsumerDown = mutableListOf<ConsumerKey>()
    val recordedConsumerUp = mutableListOf<Unit>()
    val testActions = object : RemoteInputActions {
      override fun keyDown(keyCode: Int) = Unit
      override fun keyUp() = Unit
      override fun tapKey(keyCode: Int) = Unit
      override fun consumerDown(key: ConsumerKey) { recordedConsumerDown.add(key) }
      override fun consumerUp() { recordedConsumerUp.add(Unit) }
      override fun typeText(text: String, backspaces: Int) = Unit
      override fun moveMouse(dx: Float, dy: Float) = Unit
      override fun scroll(amount: Float) = Unit
      override fun mouseButton(button: Int, pressed: Boolean) = Unit
      override fun mouseClick() = Unit
    }

    composeTestRule.setContent {
      ToolboxTheme {
        Surface {
          RemoteControlContent(
            deviceName = "Living Room TV",
            actions = testActions,
            onDisconnect = {},
          )
        }
      }
    }

    composeTestRule.onNodeWithTag("bt_power").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_nav_back").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_nav_home").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bt_nav_menu").assertIsDisplayed()

    composeTestRule.onNodeWithTag("bt_power").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.POWER))

    composeTestRule.onNodeWithTag("bt_nav_back").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.BACK))

    composeTestRule.onNodeWithTag("bt_nav_home").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.HOME))

    composeTestRule.onNodeWithTag("bt_nav_menu").performClick()
    assert(recordedConsumerDown.contains(ConsumerKey.MENU))
  }

  @Test
  fun remoteControl_volumeButtonsHandleTouchAndRepeatOnHold() {
    val recordedConsumerDown = mutableListOf<ConsumerKey>()
    val recordedConsumerUp = mutableListOf<Unit>()
    val testActions = object : RemoteInputActions {
      override fun keyDown(keyCode: Int) = Unit
      override fun keyUp() = Unit
      override fun tapKey(keyCode: Int) = Unit
      override fun consumerDown(key: ConsumerKey) { recordedConsumerDown.add(key) }
      override fun consumerUp() { recordedConsumerUp.add(Unit) }
      override fun typeText(text: String, backspaces: Int) = Unit
      override fun moveMouse(dx: Float, dy: Float) = Unit
      override fun scroll(amount: Float) = Unit
      override fun mouseButton(button: Int, pressed: Boolean) = Unit
      override fun mouseClick() = Unit
    }

    composeTestRule.setContent {
      ToolboxTheme {
        Surface {
          RemoteControlContent(
            deviceName = "Living Room TV",
            actions = testActions,
            onDisconnect = {},
          )
        }
      }
    }

    // Single touch tap on Volume Down
    composeTestRule.onNodeWithTag("bt_volume_down").performTouchInput {
      click()
    }
    composeTestRule.waitUntil(timeoutMillis = 1000) {
      recordedConsumerDown.contains(ConsumerKey.VOLUME_DOWN) && recordedConsumerUp.isNotEmpty()
    }

    // Single touch tap on Volume Up
    composeTestRule.onNodeWithTag("bt_volume_up").performTouchInput {
      click()
    }
    composeTestRule.waitUntil(timeoutMillis = 1000) {
      recordedConsumerDown.contains(ConsumerKey.VOLUME_UP)
    }

    // Press and hold Volume Up to verify repeat
    val initialUpCount = recordedConsumerDown.count { it == ConsumerKey.VOLUME_UP }
    composeTestRule.onNodeWithTag("bt_volume_up").performTouchInput {
      down(center)
      advanceEventTime(700L)
    }
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      recordedConsumerDown.count { it == ConsumerKey.VOLUME_UP } > initialUpCount
    }
    composeTestRule.onNodeWithTag("bt_volume_up").performTouchInput {
      up()
    }
    assert(recordedConsumerDown.count { it == ConsumerKey.VOLUME_UP } >= initialUpCount + 1)
  }
}
