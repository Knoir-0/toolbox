package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
    composeTestRule.onNodeWithContentDescription("Dismiss").performClick()
    assert(dismissClicked)
  }

  @Test
  fun remoteControl_displaysVolumeBarWithDownMuteUp_andNoChannelOrMediaButtons() {
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

    // Media buttons (play/pause, rewind, fast forward) are removed
    composeTestRule.onNodeWithContentDescription("Play/Pause").assertDoesNotExist()
    composeTestRule.onNodeWithContentDescription("Rewind").assertDoesNotExist()
    composeTestRule.onNodeWithContentDescription("Fast forward").assertDoesNotExist()
  }
}
