package com.example.toolbox.ui.tools

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
}
